import * as cheerio from 'cheerio'
import { createHash } from 'node:crypto'
import { mkdir, rm, writeFile } from 'node:fs/promises'
import path from 'node:path'

const origin = 'https://24365.jl.smartedu.cn'
const outputRoot = path.resolve(process.env.MIGRATION_OUTPUT || 'party/v1/generated')
const rawRoot = path.join(outputRoot, 'raw')
const assetRoot = path.join(outputRoot, 'assets')
const reportRoot = path.join(outputRoot, 'reports')
const sourceSystem = 'legacy-jilinjobs'
const userAgent = 'jilinjobs-cms-eu29-migration/1.0 (+https://github.com/dygapp/jilinjobs-cms)'

const scopes = [
  { typeCode: 'gcsy', columnAlias: 'party-voice' },
  { typeCode: 'gzdt', columnAlias: 'party-work' },
  { typeCode: 'dgdz', columnAlias: 'party-rules' },
  { typeCode: 'llxx', columnAlias: 'party-study' },
]

const attachmentExtensions = new Set(['pdf', 'doc', 'docx', 'xls', 'xlsx'])
const resourceCache = new Map()
const resourceRecords = new Map()
const issues = []

function sha256(value) {
  return createHash('sha256').update(value).digest('hex')
}

function normalizeText(value) {
  return String(value ?? '').replace(/\s+/g, ' ').trim()
}

function normalizeUrl(value, base = origin) {
  const url = new URL(value, base)
  url.hash = ''
  return url.toString()
}

function safeName(value) {
  return value.replace(/[^a-zA-Z0-9._-]/g, '_').slice(0, 120)
}

async function fetchResponse(url, attempts = 3) {
  let lastError
  for (let attempt = 1; attempt <= attempts; attempt += 1) {
    const controller = new AbortController()
    const timer = setTimeout(() => controller.abort(), 30_000)
    try {
      const response = await fetch(url, {
        headers: { 'user-agent': userAgent, accept: '*/*' },
        redirect: 'follow',
        signal: controller.signal,
      })
      if (!response.ok) throw new Error(`HTTP ${response.status} ${response.statusText}`)
      return response
    } catch (error) {
      lastError = error
      if (attempt < attempts) await new Promise(resolve => setTimeout(resolve, attempt * 750))
    } finally {
      clearTimeout(timer)
    }
  }
  throw lastError
}

async function fetchText(url) {
  const response = await fetchResponse(url)
  return { url: response.url, contentType: response.headers.get('content-type') || '', text: await response.text() }
}

function detectMedia(bytes) {
  const b = Buffer.from(bytes)
  const ascii = (start, end) => b.subarray(start, end).toString('ascii')
  if (b.length >= 8 && b.subarray(0, 8).equals(Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]))) return { contentType: 'image/png', extension: 'png' }
  if (b.length >= 2 && b[0] === 0xff && b[1] === 0xd8) return { contentType: 'image/jpeg', extension: 'jpg' }
  if (ascii(0, 6) === 'GIF87a' || ascii(0, 6) === 'GIF89a') return { contentType: 'image/gif', extension: 'gif' }
  if (ascii(0, 4) === 'RIFF' && ascii(8, 12) === 'WEBP') return { contentType: 'image/webp', extension: 'webp' }
  if (ascii(0, 5) === '%PDF-') return { contentType: 'application/pdf', extension: 'pdf' }
  if (b.length >= 8 && b.subarray(0, 8).equals(Buffer.from([0xd0, 0xcf, 0x11, 0xe0, 0xa1, 0xb1, 0x1a, 0xe1]))) return { contentType: 'application/x-ole-storage', extension: 'bin' }
  if (ascii(0, 2) === 'PK') return { contentType: 'application/zip', extension: 'zip' }
  return { contentType: null, extension: 'bin' }
}

async function collectResource(rawReference, baseUrl, role) {
  const sourceUrl = normalizeUrl(rawReference, baseUrl)
  if (!/^https?:/.test(sourceUrl)) return null
  const cacheKey = `${role}:${sourceUrl}`
  if (resourceCache.has(cacheKey)) return resourceCache.get(cacheKey)

  try {
    const response = await fetchResponse(sourceUrl)
    const bytes = Buffer.from(await response.arrayBuffer())
    const detected = detectMedia(bytes)
    const declared = (response.headers.get('content-type') || '').split(';')[0].trim() || null
    const hash = sha256(bytes)
    const extension = detected.extension === 'bin'
      ? path.extname(new URL(sourceUrl).pathname).slice(1).toLowerCase() || 'bin'
      : detected.extension
    const relativePath = `assets/${role === 'BODY_IMAGE' ? 'body' : 'attachments'}/${hash}.${safeName(extension)}`
    const outputPath = path.join(outputRoot, relativePath)
    await mkdir(path.dirname(outputPath), { recursive: true })
    await writeFile(outputPath, bytes)

    const record = {
      role,
      sourceUrl,
      originalReference: rawReference,
      token: `${role === 'BODY_IMAGE' ? 'migration-resource' : 'migration-attachment'}://${hash}`,
      snapshotPath: relativePath,
      sha256: hash,
      contentType: detected.contentType || declared,
      declaredContentType: declared,
      sizeBytes: bytes.length,
    }
    resourceCache.set(cacheKey, record)
    if (!resourceRecords.has(hash)) resourceRecords.set(hash, record)
    if (detected.contentType && declared && declared !== detected.contentType && !(declared === 'image/jpg' && detected.contentType === 'image/jpeg')) {
      issues.push({ level: 'warning', code: 'RESOURCE_MEDIA_MISMATCH', sourceUrl, declared, detected: detected.contentType })
    }
    return record
  } catch (error) {
    issues.push({ level: 'error', code: 'RESOURCE_FETCH_FAILED', role, sourceUrl, message: String(error) })
    return null
  }
}

function parsePagination(html, typeCode) {
  const $ = cheerio.load(html)
  const text = normalizeText($('.pagination-wrap').first().text())
  const match = text.match(/共\s*(\d+)\s*页[，,]\s*(\d+)\s*条记录/)
  if (!match) throw new Error(`无法识别 ${typeCode} 分页总量：${text}`)
  return { pageCount: Number(match[1]), total: Number(match[2]), text }
}

function parseListPage(html, typeCode, pageNo) {
  const $ = cheerio.load(html)
  return $('ul.default-list > li.list-item > a.list-item-a').map((_, element) => {
    const anchor = $(element)
    const href = anchor.attr('href')
    const title = normalizeText(anchor.find('.title').text())
    const dateText = normalizeText(anchor.find('.date').text())
    if (!href || !title) return null
    let url
    try { url = normalizeUrl(href) } catch { return null }
    return {
      typeCode,
      pageNo,
      title,
      publishDate: /^\d{4}-\d{2}-\d{2}$/.test(dateText) ? dateText : null,
      rawDate: dateText || null,
      href,
      url,
    }
  }).get().filter(Boolean)
}

function classifyListItem(item, columnAlias) {
  const url = new URL(item.url)
  const legacyDetail = url.origin === origin && /\/(pdetail|detail)\.html$/.test(url.pathname)
  const contentId = legacyDetail ? url.searchParams.get('content_id') : null
  if (legacyDetail && contentId) {
    return {
      ...item,
      columnAlias,
      articleType: 'INTERNAL',
      contentId,
      detailPath: url.pathname,
      legacyKey: `${item.typeCode}:content:${contentId}`,
    }
  }
  const canonical = url.toString()
  return {
    ...item,
    columnAlias,
    articleType: 'EXTERNAL_LINK',
    contentId: null,
    detailPath: url.pathname,
    legacyKey: `${item.typeCode}:external:${sha256(canonical)}`,
  }
}

async function collectInternal(item) {
  const response = await fetchText(item.url)
  const rawName = `${item.typeCode}-${item.contentId}.html`
  await writeFile(path.join(rawRoot, 'details', rawName), response.text, 'utf8')
  const $ = cheerio.load(response.text)
  const title = normalizeText($('.detail-content-title').first().text())
  const tips = $('.detail-content-title-tips > div').map((_, element) => normalizeText($(element).text())).get()
  const sourceText = tips.find(value => value.startsWith('信息来源：')) || ''
  const dateText = tips.find(value => value.startsWith('发布时间：')) || ''
  const source = normalizeText(sourceText.replace(/^信息来源：/, ''))
  const detailDateValue = normalizeText(dateText.replace(/^发布时间：/, ''))
  const detailDate = /^\d{4}-\d{2}-\d{2}$/.test(detailDateValue) ? detailDateValue : null
  const rich = $('.rich-text-wrap').first()
  if (!title || !rich.length) {
    issues.push({ level: 'error', code: 'INTERNAL_DETAIL_STRUCTURE_MISSING', legacyKey: item.legacyKey, url: item.url })
    return null
  }
  if (normalizeText(item.title) !== title) {
    issues.push({ level: 'error', code: 'TITLE_CONFLICT', legacyKey: item.legacyKey, listTitle: item.title, detailTitle: title })
  }
  if (item.publishDate && detailDate && item.publishDate !== detailDate) {
    issues.push({ level: 'error', code: 'DATE_CONFLICT', legacyKey: item.legacyKey, listDate: item.publishDate, detailDate })
  }

  const resources = []
  for (const image of rich.find('img[src]').toArray()) {
    const node = $(image)
    const rawReference = node.attr('src')
    if (!rawReference || rawReference.startsWith('data:')) continue
    const resource = await collectResource(rawReference, response.url, 'BODY_IMAGE')
    if (resource) {
      resources.push(resource)
      node.attr('src', resource.token)
    }
  }
  for (const anchor of rich.find('a[href]').toArray()) {
    const node = $(anchor)
    const rawReference = node.attr('href')
    if (!rawReference) continue
    let parsed
    try { parsed = new URL(rawReference, response.url) } catch { continue }
    const extension = path.extname(parsed.pathname).slice(1).toLowerCase()
    if (!attachmentExtensions.has(extension)) continue
    const resource = await collectResource(rawReference, response.url, 'ATTACHMENT')
    if (resource) {
      resources.push(resource)
      node.attr('href', resource.token)
    }
  }

  const bodyHtml = rich.html() || ''
  const content = {
    title,
    source,
    publishDate: detailDate ?? item.publishDate,
    bodyHtml,
    externalUrl: null,
  }
  const fingerprint = sha256(JSON.stringify({
    identity: item.legacyKey,
    target: item.columnAlias,
    content,
    resources: resources.map(resource => [resource.role, resource.sha256]).sort(),
  }))
  return {
    source: {
      system: sourceSystem,
      legacyKey: item.legacyKey,
      contentId: item.contentId,
      typeCode: item.typeCode,
      detailPath: item.detailPath,
      url: response.url,
    },
    target: { columnAlias: item.columnAlias, articleType: 'INTERNAL' },
    content,
    resources: resources.map(({ declaredContentType, ...resource }) => resource),
    sourceFingerprint: fingerprint,
    evidence: {
      listPage: item.pageNo,
      listTitle: item.title,
      listPublishDate: item.publishDate,
      detailPublishDate: detailDate,
      rawDetailPath: `raw/details/${rawName}`,
    },
  }
}

function collectExternal(item) {
  const content = {
    title: item.title,
    source: '',
    publishDate: item.publishDate,
    bodyHtml: '',
    externalUrl: item.url,
  }
  const fingerprint = sha256(JSON.stringify({ identity: item.legacyKey, target: item.columnAlias, content }))
  return {
    source: {
      system: sourceSystem,
      legacyKey: item.legacyKey,
      contentId: null,
      typeCode: item.typeCode,
      detailPath: item.detailPath,
      url: item.url,
    },
    target: { columnAlias: item.columnAlias, articleType: 'EXTERNAL_LINK' },
    content,
    resources: [],
    sourceFingerprint: fingerprint,
    evidence: {
      listPage: item.pageNo,
      listTitle: item.title,
      listPublishDate: item.publishDate,
    },
  }
}

async function collectHomeDiscovery() {
  const { text, url } = await fetchText(`${origin}/dyzj`)
  await writeFile(path.join(rawRoot, 'party-home.html'), text, 'utf8')
  const $ = cheerio.load(text)
  const candidates = $('img').map((_, image) => {
    const node = $(image)
    const parent = node.closest('a[href]')
    return {
      src: node.attr('src') ? normalizeUrl(node.attr('src'), url) : null,
      alt: normalizeText(node.attr('alt')) || null,
      parentHref: parent.attr('href') ? normalizeUrl(parent.attr('href'), url) : null,
      className: node.attr('class') || '',
      parentClassName: parent.attr('class') || '',
    }
  }).get()
  await writeFile(path.join(reportRoot, 'home-image-candidates.json'), JSON.stringify(candidates, null, 2), 'utf8')
  return { imageCandidates: candidates.length }
}

async function main() {
  await rm(outputRoot, { recursive: true, force: true })
  await mkdir(path.join(rawRoot, 'lists'), { recursive: true })
  await mkdir(path.join(rawRoot, 'details'), { recursive: true })
  await mkdir(assetRoot, { recursive: true })
  await mkdir(reportRoot, { recursive: true })

  const listItems = []
  const scopeReport = []
  for (const scope of scopes) {
    const firstUrl = `${origin}/plist.html?typeCode=${scope.typeCode}`
    const first = await fetchText(firstUrl)
    const pagination = parsePagination(first.text, scope.typeCode)
    const seenPageFingerprints = new Set()
    const scoped = []
    for (let pageNo = 1; pageNo <= pagination.pageCount; pageNo += 1) {
      const pageUrl = `${origin}/plist.html?typeCode=${scope.typeCode}&pageNo=${pageNo}&pageSize=10`
      const page = pageNo === 1 ? first : await fetchText(pageUrl)
      const rawPath = path.join(rawRoot, 'lists', `${scope.typeCode}-page-${String(pageNo).padStart(2, '0')}.html`)
      await writeFile(rawPath, page.text, 'utf8')
      const pageFingerprint = sha256(page.text)
      if (seenPageFingerprints.has(pageFingerprint)) {
        issues.push({ level: 'error', code: 'REPEATED_PAGE_HTML', typeCode: scope.typeCode, pageNo })
      }
      seenPageFingerprints.add(pageFingerprint)
      const parsed = parseListPage(page.text, scope.typeCode, pageNo).map(item => classifyListItem(item, scope.columnAlias))
      scoped.push(...parsed)
    }
    const unique = new Map(scoped.map(item => [item.legacyKey, item]))
    if (scoped.length !== pagination.total || unique.size !== pagination.total) {
      issues.push({
        level: 'error',
        code: 'SCOPE_COUNT_MISMATCH',
        typeCode: scope.typeCode,
        reportedTotal: pagination.total,
        parsedCount: scoped.length,
        uniqueCount: unique.size,
      })
    }
    listItems.push(...unique.values())
    scopeReport.push({
      typeCode: scope.typeCode,
      columnAlias: scope.columnAlias,
      reportedTotal: pagination.total,
      pageCount: pagination.pageCount,
      parsedCount: scoped.length,
      uniqueCount: unique.size,
      internal: [...unique.values()].filter(item => item.articleType === 'INTERNAL').length,
      external: [...unique.values()].filter(item => item.articleType === 'EXTERNAL_LINK').length,
    })
  }

  const records = []
  for (const [index, item] of listItems.entries()) {
    process.stdout.write(`collect ${index + 1}/${listItems.length} ${item.legacyKey}\n`)
    const record = item.articleType === 'INTERNAL' ? await collectInternal(item) : collectExternal(item)
    if (record) records.push(record)
  }

  const home = await collectHomeDiscovery()
  await writeFile(path.join(outputRoot, 'articles.ndjson'), records.map(record => JSON.stringify(record)).join('\n') + '\n', 'utf8')
  await writeFile(path.join(outputRoot, 'resources.ndjson'), [...resourceRecords.values()].map(record => JSON.stringify(record)).join('\n') + (resourceRecords.size ? '\n' : ''), 'utf8')

  const errors = issues.filter(issue => issue.level === 'error')
  const reconciliation = {
    generatedAt: new Date().toISOString(),
    sourceSystem,
    sourceOrigin: origin,
    scopes: scopeReport,
    listedRecords: listItems.length,
    normalizedRecords: records.length,
    internal: records.filter(record => record.target.articleType === 'INTERNAL').length,
    external: records.filter(record => record.target.articleType === 'EXTERNAL_LINK').length,
    resources: resourceRecords.size,
    unresolved: errors.length,
    warnings: issues.length - errors.length,
    home,
  }
  await writeFile(path.join(reportRoot, 'reconciliation.json'), JSON.stringify(reconciliation, null, 2), 'utf8')
  await writeFile(path.join(reportRoot, 'issues.json'), JSON.stringify(issues, null, 2), 'utf8')
  console.log(`EU29_COLLECTION_SUMMARY ${JSON.stringify(reconciliation)}`)

  if (scopeReport.some(scope => scope.reportedTotal !== scope.uniqueCount)) {
    throw new Error('栏目分页采集数量与原站报告总量不一致')
  }
}

await main()
