import * as cheerio from 'cheerio'
import { createHash } from 'node:crypto'
import { mkdir, readFile, rm, writeFile } from 'node:fs/promises'
import path from 'node:path'

const configPath = path.resolve(process.env.MAIN_SOURCE_CONFIG || 'main/source-surfaces.json')
const outputRoot = path.resolve(process.env.MIGRATION_OUTPUT || 'main/v1/generated-probe')
const config = JSON.parse(await readFile(configPath, 'utf8'))
const sourceRoot = new URL(config.sourceRoot)
const userAgent = 'jilinjobs-cms-eu50-main-source-probe/1.0 (+https://github.com/dygapp/jilinjobs-cms)'
const observations = []
const issues = []

const normalizeText = value => String(value ?? '').replace(/\s+/g, ' ').trim()
const sha256 = value => createHash('sha256').update(value).digest('hex')
const safeName = value => String(value).replace(/[^a-zA-Z0-9._-]/g, '_').slice(0, 160)
const sleep = ms => new Promise(resolve => setTimeout(resolve, ms))

function normalizeUrl(value, base = sourceRoot) {
  const url = new URL(value, base)
  url.hash = ''
  return url.toString()
}
function addIssue(level, code, detail = {}) {
  issues.push({ level, code, ...detail })
}
async function writeJson(file, value) {
  await mkdir(path.dirname(file), { recursive: true })
  await writeFile(file, JSON.stringify(value, null, 2) + '\n', 'utf8')
}
async function writeRaw(category, key, text) {
  const file = path.join(outputRoot, 'raw', category, `${safeName(key)}.html`)
  await mkdir(path.dirname(file), { recursive: true })
  await writeFile(file, text, 'utf8')
  return path.relative(outputRoot, file).replaceAll('\\', '/')
}
async function fetchResponse(url, attempts = 3) {
  let lastError
  for (let attempt = 1; attempt <= attempts; attempt += 1) {
    const controller = new AbortController()
    const timer = setTimeout(() => controller.abort(), 30_000)
    try {
      const response = await fetch(url, {
        headers: { 'user-agent': userAgent, accept: 'text/html,*/*;q=0.8' },
        redirect: 'follow',
        signal: controller.signal,
      })
      if (!response.ok) throw new Error(`HTTP ${response.status} ${response.statusText}`)
      return response
    } catch (error) {
      lastError = error
      observations.push({ kind: 'retry', url: String(url), attempt, message: String(error) })
      if (attempt < attempts) await sleep(attempt * 750)
    } finally {
      clearTimeout(timer)
    }
  }
  throw lastError
}
async function fetchText(url) {
  const response = await fetchResponse(url)
  return { requestedUrl: String(url), finalUrl: response.url, status: response.status, text: await response.text() }
}

function parsePagination(html, key) {
  const $ = cheerio.load(html)
  const text = normalizeText($('.pagination-wrap').first().text() || $('body').text())
  const match = text.match(/共\s*([\d,]+)\s*页[，,]\s*([\d,]+)\s*条记录/)
  if (!match) throw new Error(`无法识别 ${key} 分页总量`)
  return { pageCount: Number(match[1].replaceAll(',', '')), total: Number(match[2].replaceAll(',', '')), summary: match[0] }
}
function parseFirstPageItems(html) {
  const $ = cheerio.load(html)
  return $('ul.default-list > li.list-item > a.list-item-a').map((index, element) => {
    const anchor = $(element)
    const href = anchor.attr('href')
    const title = normalizeText(anchor.find('.title').text() || anchor.text().replace(/\d{4}-\d{2}-\d{2}/g, ''))
    const date = normalizeText(anchor.find('.date').text()) || null
    if (!href || !title) return null
    let url
    try { url = normalizeUrl(href) } catch { return null }
    const parsed = new URL(url)
    return {
      index: index + 1,
      title,
      date,
      href,
      url,
      host: parsed.host,
      pathname: parsed.pathname,
      contentId: parsed.searchParams.get('content_id'),
    }
  }).get().filter(Boolean)
}
function diagnosticBlocks(html, title) {
  const $ = cheerio.load(html)
  const blocks = []
  $('main,article,section,div').each((_, element) => {
    const node = $(element)
    const text = normalizeText(node.text())
    if (text.length < 40) return
    const id = node.attr('id') || null
    const className = node.attr('class') || null
    if (!id && !className) return
    const tag = String(element.tagName || element.name || 'div').toLowerCase()
    const selector = id ? `${tag}#${id}` : `${tag}.${String(className).trim().split(/\s+/).filter(Boolean).join('.')}`
    blocks.push({ selector, textLength: text.length, titleHit: title ? text.includes(title) : false, textSample: text.slice(0, 350) })
  })
  return blocks
    .sort((a, b) => Number(b.titleHit) - Number(a.titleHit) || a.textLength - b.textLength || a.selector.localeCompare(b.selector))
    .filter((entry, index, all) => index === all.findIndex(candidate => candidate.selector === entry.selector))
    .slice(0, 100)
}
function selectorMatches(html, selectors = []) {
  const $ = cheerio.load(html)
  return selectors.map(selector => ({ selector, count: $(selector).length, textLength: normalizeText($(selector).first().text()).length }))
}
function headingAnchors(html) {
  const $ = cheerio.load(html)
  return $('h1,h2,h3,h4,h5,h6').map((_, heading) => {
    const node = $(heading)
    const text = normalizeText(node.text())
    if (!text) return null
    const parent = node.parent()
    return {
      heading: text.slice(0, 200),
      tag: String(heading.tagName || heading.name || '').toLowerCase(),
      parentClass: parent.attr('class') || null,
      parentId: parent.attr('id') || null,
      parentTextLength: normalizeText(parent.text()).length,
    }
  }).get().filter(Boolean).slice(0, 100)
}

async function probeRedirects() {
  const result = []
  for (const requestedUrl of config.redirectProbes || []) {
    try {
      const response = await fetchResponse(requestedUrl)
      result.push({ requestedUrl, status: response.status, finalUrl: response.url })
    } catch (error) {
      result.push({ requestedUrl, status: 'UNREACHABLE', message: String(error) })
      addIssue('warning', 'REDIRECT_PROBE_UNREACHABLE', { requestedUrl, message: String(error) })
    }
  }
  return result
}
async function probeArticleSurfaces() {
  const reports = []
  for (const surface of config.articleSurfaces) {
    try {
      const response = await fetchText(new URL(surface.path, sourceRoot).toString())
      const rawPath = await writeRaw('article-surfaces', surface.key, response.text)
      const pagination = parsePagination(response.text, surface.key)
      const items = parseFirstPageItems(response.text)
      reports.push({
        ...surface,
        status: 'OK',
        requestedUrl: response.requestedUrl,
        finalUrl: response.finalUrl,
        rawPath,
        ...pagination,
        firstPageParsed: items.length,
        firstPageFingerprint: sha256(JSON.stringify(items.map(item => [item.title, item.date, item.url]))),
        firstPageItems: items,
      })
      if (items.length === 0) addIssue('error', 'ARTICLE_SURFACE_EMPTY_FIRST_PAGE', { surfaceKey: surface.key })
    } catch (error) {
      reports.push({ ...surface, status: 'UNRESOLVED', message: String(error) })
      addIssue('error', 'ARTICLE_SURFACE_UNRESOLVED', { surfaceKey: surface.key, path: surface.path, message: String(error) })
    }
  }
  return reports
}
async function probePages() {
  const reports = []
  for (const surface of config.pageSurfaces) {
    try {
      const response = await fetchText(new URL(surface.path, sourceRoot).toString())
      const rawPath = await writeRaw('pages', surface.key, response.text)
      const selectorResults = selectorMatches(response.text, surface.selectors)
      const matched = selectorResults.find(result => result.count > 0 && result.textLength >= 20) || null
      const report = {
        ...surface,
        status: matched ? 'SELECTOR_MATCHED' : 'SELECTOR_REQUIRED',
        requestedUrl: response.requestedUrl,
        finalUrl: response.finalUrl,
        rawPath,
        selectorResults,
        matchedSelector: matched?.selector || null,
        blocks: diagnosticBlocks(response.text, surface.title),
        headings: headingAnchors(response.text),
      }
      reports.push(report)
      if (!matched) addIssue('error', 'PAGE_SELECTOR_UNRESOLVED', { surfaceKey: surface.key, finalUrl: response.finalUrl })
    } catch (error) {
      reports.push({ ...surface, status: 'UNRESOLVED', message: String(error) })
      addIssue('error', 'PAGE_SURFACE_UNRESOLVED', { surfaceKey: surface.key, path: surface.path, message: String(error) })
    }
  }
  return reports
}
async function probeHome() {
  const response = await fetchText(new URL(config.home.path, sourceRoot).toString())
  const rawPath = await writeRaw('home', 'main-home', response.text)
  const definitions = [config.home.carousel, ...(config.home.siteLinkLists || [])]
  const selectors = definitions.map(definition => ({ listCode: definition.listCode, selectorResults: selectorMatches(response.text, definition.selectors) }))
  for (const definition of selectors) {
    if (!definition.selectorResults.some(result => result.count > 0)) addIssue('error', 'HOME_LIST_SELECTOR_UNRESOLVED', { listCode: definition.listCode })
  }
  return {
    requestedUrl: response.requestedUrl,
    finalUrl: response.finalUrl,
    rawPath,
    selectors,
    blocks: diagnosticBlocks(response.text, '网站导航'),
    headings: headingAnchors(response.text),
  }
}

async function main() {
  await rm(outputRoot, { recursive: true, force: true })
  await mkdir(outputRoot, { recursive: true })
  const redirects = await probeRedirects()
  const articleSurfaces = await probeArticleSurfaces()
  const pages = await probePages()
  const home = await probeHome()
  const summary = {
    collectedAt: new Date().toISOString(),
    sourceRoot: config.sourceRoot,
    redirectProbes: redirects,
    articleSurfaceCount: articleSurfaces.length,
    articleSurfaceResolved: articleSurfaces.filter(item => item.status === 'OK').length,
    observedArticleRecords: articleSurfaces.filter(item => item.status === 'OK').reduce((sum, item) => sum + item.total, 0),
    pageSurfaceCount: pages.length,
    pageSelectorsResolved: pages.filter(item => item.status === 'SELECTOR_MATCHED').length,
    homeListSelectorsResolved: home.selectors.filter(item => item.selectorResults.some(result => result.count > 0)).length,
    errors: issues.filter(item => item.level === 'error').length,
    warnings: issues.filter(item => item.level === 'warning').length,
    promotionReady: false,
  }
  await writeJson(path.join(outputRoot, 'redirects.json'), redirects)
  await writeJson(path.join(outputRoot, 'article-surfaces.json'), articleSurfaces)
  await writeJson(path.join(outputRoot, 'page-surfaces.json'), pages)
  await writeJson(path.join(outputRoot, 'home.json'), home)
  await writeJson(path.join(outputRoot, 'issues.json'), issues)
  await writeJson(path.join(outputRoot, 'observations.json'), observations)
  await writeJson(path.join(outputRoot, 'summary.json'), summary)
  console.log(`EU50_MAIN_SOURCE_PROBE ${JSON.stringify(summary)}`)
}

await main()
