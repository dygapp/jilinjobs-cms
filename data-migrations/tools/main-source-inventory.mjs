import * as cheerio from 'cheerio'
import { createHash } from 'node:crypto'
import { mkdir, readFile, rm, writeFile } from 'node:fs/promises'
import path from 'node:path'

const configPath = path.resolve(process.env.MAIN_SOURCE_CONFIG || 'main/source-surfaces.json')
const outputRoot = path.resolve(process.env.MIGRATION_OUTPUT || 'main/v1/generated-inventory')
const config = JSON.parse(await readFile(configPath, 'utf8'))
const sourceRoot = new URL(config.sourceRoot)
const allowedHosts = new Set(config.allowedHosts || [sourceRoot.host])
const userAgent = 'jilinjobs-cms-eu50-main-source-inventory/1.0 (+https://github.com/dygapp/jilinjobs-cms)'
const issues = []
const observations = []

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
async function writeRaw(surfaceKey, pageNo, text) {
  const file = path.join(outputRoot, 'raw', 'list-pages', `${safeName(surfaceKey)}-page-${String(pageNo).padStart(4, '0')}.html`)
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
function parseListPage(html, surface, pageNo) {
  const $ = cheerio.load(html)
  return $('ul.default-list > li.list-item > a.list-item-a').map((index, element) => {
    const anchor = $(element)
    const href = anchor.attr('href')
    const title = normalizeText(anchor.find('.title').text() || anchor.text().replace(/\d{4}-\d{2}-\d{2}/g, ''))
    const dateText = normalizeText(anchor.find('.date').text())
    if (!href || !title) return null
    let url
    try { url = normalizeUrl(href) } catch { return null }
    const parsed = new URL(url)
    const contentId = parsed.searchParams.get('content_id')
    const internal = allowedHosts.has(parsed.host) && /\/(?:pdetail|detail)\.html$/.test(parsed.pathname) && Boolean(contentId)
    const identity = internal ? `content:${contentId}` : `external:${sha256(url)}`
    const legacyKey = `${surface.typeCode}:${identity}`
    return {
      surfaceKey: surface.key,
      typeCode: surface.typeCode,
      columnAlias: surface.columnAlias,
      pageNo,
      pageIndex: index + 1,
      title,
      publishDate: /^\d{4}-\d{2}-\d{2}$/.test(dateText) ? dateText : null,
      rawDate: dateText || null,
      href,
      url,
      host: parsed.host,
      pathname: parsed.pathname,
      articleType: internal ? 'INTERNAL' : 'EXTERNAL_LINK',
      contentId: internal ? contentId : null,
      sourceIdentity: identity,
      legacyKey,
    }
  }).get().filter(Boolean)
}
function pageUrl(surface, pageNo) {
  const url = new URL(surface.path, sourceRoot)
  url.searchParams.set('pageNo', String(pageNo))
  url.searchParams.set('pageSize', '10')
  return url.toString()
}
async function mapLimit(values, limit, fn) {
  const results = new Array(values.length)
  let cursor = 0
  async function worker() {
    while (true) {
      const index = cursor
      cursor += 1
      if (index >= values.length) return
      results[index] = await fn(values[index], index)
    }
  }
  await Promise.all(Array.from({ length: Math.min(limit, values.length) }, () => worker()))
  return results
}

async function inventorySurface(surface, surfaceOrder) {
  const first = await fetchText(new URL(surface.path, sourceRoot).toString())
  const pagination = parsePagination(first.text, surface.key)
  const pageNos = Array.from({ length: pagination.pageCount }, (_, index) => index + 1)
  const pages = await mapLimit(pageNos, 4, async pageNo => {
    const response = pageNo === 1 ? first : await fetchText(pageUrl(surface, pageNo))
    const rawPath = await writeRaw(surface.key, pageNo, response.text)
    const items = parseListPage(response.text, surface, pageNo)
    return {
      pageNo,
      requestedUrl: response.requestedUrl,
      finalUrl: response.finalUrl,
      rawPath,
      pageFingerprint: sha256(response.text),
      parsedCount: items.length,
      items,
    }
  })
  const fingerprintGroups = new Map()
  for (const page of pages) {
    const group = fingerprintGroups.get(page.pageFingerprint) || []
    group.push(page.pageNo)
    fingerprintGroups.set(page.pageFingerprint, group)
  }
  const repeatedPageFingerprints = [...fingerprintGroups.entries()]
    .filter(([, pageNumbers]) => pageNumbers.length > 1)
    .map(([fingerprint, pageNumbers]) => ({ fingerprint, pageNumbers }))
  if (repeatedPageFingerprints.length > 0) {
    addIssue('error', 'REPEATED_PAGE_HTML', { surfaceKey: surface.key, repeatedPageFingerprints })
  }
  const rows = pages.flatMap(page => page.items).map((item, index) => ({
    ...item,
    surfaceOrder,
    sourceOrder: index + 1,
  }))
  const byLegacyKey = new Map()
  for (const row of rows) {
    const group = byLegacyKey.get(row.legacyKey) || []
    group.push(row)
    byLegacyKey.set(row.legacyKey, group)
  }
  const duplicates = [...byLegacyKey.entries()]
    .filter(([, values]) => values.length > 1)
    .map(([legacyKey, values]) => ({ legacyKey, occurrences: values.map(value => ({ pageNo: value.pageNo, pageIndex: value.pageIndex, url: value.url })) }))
  const unique = [...byLegacyKey.values()].map(values => values[0])
  const reportedTotal = pagination.total
  if (rows.length !== reportedTotal) {
    addIssue('error', 'SURFACE_REPORTED_TOTAL_MISMATCH', { surfaceKey: surface.key, reportedTotal, observedRows: rows.length })
  }
  const expectedPageCount = Math.max(1, Math.ceil(reportedTotal / 10))
  if (pagination.pageCount !== expectedPageCount) {
    addIssue('error', 'SURFACE_PAGE_COUNT_MISMATCH', { surfaceKey: surface.key, reportedTotal, pageCount: pagination.pageCount, expectedPageCount })
  }
  return {
    ...surface,
    surfaceOrder,
    firstRequestedUrl: first.requestedUrl,
    firstFinalUrl: first.finalUrl,
    reportedTotal,
    pageCount: pagination.pageCount,
    observedRows: rows.length,
    uniqueOccurrences: unique.length,
    duplicateOccurrences: rows.length - unique.length,
    internalOccurrences: unique.filter(item => item.articleType === 'INTERNAL').length,
    externalOccurrences: unique.filter(item => item.articleType === 'EXTERNAL_LINK').length,
    repeatedPageFingerprints,
    duplicates,
    pages: pages.map(({ items, ...page }) => page),
    items: unique,
  }
}

function crossSurfaceEvidence(surfaceReports) {
  const internal = new Map()
  const external = new Map()
  for (const surface of surfaceReports) {
    for (const item of surface.items) {
      const targetMap = item.articleType === 'INTERNAL' ? internal : external
      const key = item.articleType === 'INTERNAL' ? item.contentId : item.url
      const group = targetMap.get(key) || []
      group.push({ surfaceKey: surface.key, legacyKey: item.legacyKey, columnAlias: item.columnAlias, title: item.title, url: item.url })
      targetMap.set(key, group)
    }
  }
  const internalGroups = [...internal.entries()]
    .filter(([, values]) => new Set(values.map(value => value.surfaceKey)).size > 1)
    .map(([contentId, occurrences]) => ({ contentId, occurrences }))
  const externalGroups = [...external.entries()]
    .filter(([, values]) => new Set(values.map(value => value.surfaceKey)).size > 1)
    .map(([url, occurrences]) => ({ url, occurrences }))
  return { internalGroups, externalGroups }
}

async function main() {
  await rm(outputRoot, { recursive: true, force: true })
  await mkdir(outputRoot, { recursive: true })
  const surfaces = []
  for (const [index, surface] of config.articleSurfaces.entries()) {
    try {
      surfaces.push(await inventorySurface(surface, index + 1))
    } catch (error) {
      addIssue('error', 'SURFACE_INVENTORY_FAILED', { surfaceKey: surface.key, message: String(error) })
      surfaces.push({ ...surface, surfaceOrder: index + 1, status: 'UNRESOLVED', message: String(error), items: [] })
    }
  }
  const resolved = surfaces.filter(surface => surface.status !== 'UNRESOLVED')
  const crossSurface = crossSurfaceEvidence(resolved)
  const summary = {
    collectedAt: new Date().toISOString(),
    sourceRoot: config.sourceRoot,
    surfaceCount: surfaces.length,
    resolvedSurfaces: resolved.length,
    reportedRecords: resolved.reduce((sum, surface) => sum + surface.reportedTotal, 0),
    observedRows: resolved.reduce((sum, surface) => sum + surface.observedRows, 0),
    uniqueSurfaceOccurrences: resolved.reduce((sum, surface) => sum + surface.uniqueOccurrences, 0),
    duplicateOccurrences: resolved.reduce((sum, surface) => sum + surface.duplicateOccurrences, 0),
    internalOccurrences: resolved.reduce((sum, surface) => sum + surface.internalOccurrences, 0),
    externalOccurrences: resolved.reduce((sum, surface) => sum + surface.externalOccurrences, 0),
    crossSurfaceInternalIdentities: crossSurface.internalGroups.length,
    crossSurfaceExternalTargets: crossSurface.externalGroups.length,
    errors: issues.filter(issue => issue.level === 'error').length,
    warnings: issues.filter(issue => issue.level === 'warning').length,
    promotionReady: false,
  }
  const inventory = resolved.flatMap(surface => surface.items)
  await writeJson(path.join(outputRoot, 'summary.json'), summary)
  await writeJson(path.join(outputRoot, 'surface-reconciliation.json'), surfaces.map(({ items, pages, ...surface }) => surface))
  await writeJson(path.join(outputRoot, 'inventory.json'), inventory)
  await writeJson(path.join(outputRoot, 'cross-surface.json'), crossSurface)
  await writeJson(path.join(outputRoot, 'issues.json'), issues)
  await writeJson(path.join(outputRoot, 'observations.json'), observations)
  console.log(`EU50_MAIN_SOURCE_INVENTORY ${JSON.stringify(summary)}`)
  if (summary.errors > 0) process.exitCode = 1
}

await main()
