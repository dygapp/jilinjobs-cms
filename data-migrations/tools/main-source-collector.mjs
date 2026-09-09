import * as cheerio from 'cheerio'
import { createHash } from 'node:crypto'
import { copyFile, mkdir, readFile, readdir, rm, writeFile } from 'node:fs/promises'
import path from 'node:path'

const configPath = path.resolve(process.env.MAIN_SOURCE_CONFIG || 'main/source-surfaces.json')
const inventoryRoot = path.resolve(process.env.MAIN_INVENTORY_ROOT || 'main/v1/generated-inventory')
const outputRoot = path.resolve(process.env.MIGRATION_OUTPUT || 'main/v1/generated-candidate')
const sitePagesPath = path.resolve(process.env.MAIN_SITE_PAGES || '../sites/jilinjobs/structure/pages.json')
const config = JSON.parse(await readFile(configPath, 'utf8'))
const inventory = JSON.parse(await readFile(path.join(inventoryRoot, 'inventory.json'), 'utf8'))
const inventorySummary = JSON.parse(await readFile(path.join(inventoryRoot, 'summary.json'), 'utf8'))
const surfaceReconciliation = JSON.parse(await readFile(path.join(inventoryRoot, 'surface-reconciliation.json'), 'utf8'))
const crossSurface = JSON.parse(await readFile(path.join(inventoryRoot, 'cross-surface.json'), 'utf8'))
const sitePages = JSON.parse(await readFile(sitePagesPath, 'utf8'))
const sourceRoot = new URL(config.sourceRoot)
const sourceSystem = config.sourceSystem
const allowedContentHosts = new Set(config.allowedHosts || [sourceRoot.host])
const allowedResourceHosts = new Set(config.allowedResourceHosts || config.allowedHosts || [sourceRoot.host])
const userAgent = 'jilinjobs-cms-eu50-main-source-collector/1.0 (+https://github.com/dygapp/jilinjobs-cms)'
const issues = []
const observations = []
const detailEvidence = []
const pageEvidence = []
const listEvidence = []
const resourceEvidence = new Map()
const resourcePromises = new Map()
const detailPromises = new Map()
const tempResourceRoot = path.join(outputRoot, '.resource-cache')
const MAX_CANDIDATES = Number(process.env.MAIN_MAX_CANDIDATES || 5000)
const MAX_RESOURCE_COUNT = Number(process.env.MAIN_MAX_RESOURCE_COUNT || 10000)
const MAX_SINGLE_RESOURCE_BYTES = Number(process.env.MAIN_MAX_SINGLE_RESOURCE_BYTES || 50 * 1024 * 1024)
const MAX_TOTAL_RESOURCE_BYTES = Number(process.env.MAIN_MAX_TOTAL_RESOURCE_BYTES || 1024 * 1024 * 1024)
const DETAIL_CONCURRENCY = Number(process.env.MAIN_DETAIL_CONCURRENCY || 6)
let totalDownloadedResourceBytes = 0

const normalizeText = value => String(value ?? '').replace(/\s+/g, ' ').trim()
const sha256 = value => createHash('sha256').update(value).digest('hex')
const sha256Bytes = bytes => createHash('sha256').update(bytes).digest('hex')
const safeName = value => String(value).replace(/[^a-zA-Z0-9._-]/g, '_').slice(0, 180)
const sleep = ms => new Promise(resolve => setTimeout(resolve, ms))
const attachmentExtensions = new Set(['pdf', 'doc', 'docx', 'xls', 'xlsx', 'rar'])
const pageResourceExtensions = new Set(['png', 'jpg', 'jpeg', 'gif', 'webp', 'ico', 'pdf', 'doc', 'docx', 'xls', 'xlsx', 'rar'])

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
function escapeHtml(value) {
  return String(value).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;')
}
function sourceExtension(url) {
  try { return path.extname(new URL(url).pathname).slice(1).toLowerCase() } catch { return '' }
}
function contentDispositionExtension(value) {
  if (!value) return ''
  const encoded = /filename\*\s*=\s*(?:UTF-8'')?([^;]+)/i.exec(value)?.[1]
  const plain = /filename\s*=\s*(?:"([^"]+)"|([^;]+))/i.exec(value)
  const raw = encoded || plain?.[1] || plain?.[2] || ''
  let filename = raw.trim().replace(/^"|"$/g, '')
  try { filename = decodeURIComponent(filename) } catch {}
  return path.extname(filename).slice(1).toLowerCase()
}
function isLegacyDynamicAttachment(parsed) {
  return /\/common\/downloadfile\.aspx$/i.test(parsed.pathname) && parsed.searchParams.has('fileid')
}
function describeError(error) {
  const base = String(error)
  const cause = error?.cause
  if (!cause) return base
  const code = cause.code || cause.name || 'unknown'
  const message = cause.message || String(cause)
  return `${base}; cause=${code}: ${message}`
}
function validateFetchHost(url, allowlist, label) {
  const parsed = new URL(url)
  if (!['http:', 'https:'].includes(parsed.protocol)) throw new Error(`${label} scheme 不允许：${parsed.protocol}`)
  if (!allowlist.has(parsed.host)) throw new Error(`${label} host 未在 allowlist：${parsed.host}`)
}
async function fetchResponse(url, attempts = 3, resource = false) {
  let lastError
  for (let attempt = 1; attempt <= attempts; attempt += 1) {
    const controller = new AbortController()
    const timer = setTimeout(() => controller.abort(), 45_000)
    try {
      validateFetchHost(url, resource ? allowedResourceHosts : allowedContentHosts, resource ? 'Resource' : 'Content')
      const response = await fetch(url, {
        headers: { 'user-agent': userAgent, accept: resource ? '*/*' : 'text/html,*/*;q=0.8' },
        redirect: 'follow',
        signal: controller.signal,
      })
      if (!response.ok) throw new Error(`HTTP ${response.status} ${response.statusText}`)
      if (resource) validateFetchHost(response.url, allowedResourceHosts, 'Resource redirect')
      else validateFetchHost(response.url, allowedContentHosts, 'Content redirect')
      return response
    } catch (error) {
      const message = describeError(error)
      lastError = new Error(message)
      observations.push({ kind: 'retry', resource, url: String(url), attempt, message })
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
async function readResponseBounded(response, maxBytes) {
  const declared = Number(response.headers.get('content-length') || 0)
  if (declared > maxBytes) throw new Error(`resource content-length ${declared} exceeds max ${maxBytes}`)
  const chunks = []
  let size = 0
  if (!response.body) return Buffer.alloc(0)
  const reader = response.body.getReader()
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    size += value.byteLength
    if (size > maxBytes) {
      await reader.cancel()
      throw new Error(`resource bytes ${size} exceeds max ${maxBytes}`)
    }
    chunks.push(Buffer.from(value))
  }
  return Buffer.concat(chunks)
}
function detectMedia(bytes, url, declaredContentType, contentDisposition = null) {
  const b = Buffer.from(bytes)
  const ascii = (start, end) => b.subarray(start, end).toString('ascii')
  const ext = sourceExtension(url) || contentDispositionExtension(contentDisposition)
  if (b.length >= 8 && b.subarray(0, 8).equals(Buffer.from([0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a]))) return { contentType: 'image/png', extension: 'png' }
  if (b.length >= 2 && b[0] === 0xff && b[1] === 0xd8) return { contentType: 'image/jpeg', extension: ext === 'jpeg' ? 'jpeg' : 'jpg' }
  if (ascii(0, 6) === 'GIF87a' || ascii(0, 6) === 'GIF89a') return { contentType: 'image/gif', extension: 'gif' }
  if (ascii(0, 4) === 'RIFF' && ascii(8, 12) === 'WEBP') return { contentType: 'image/webp', extension: 'webp' }
  if (b.length >= 4 && b.subarray(0, 4).equals(Buffer.from([0x00,0x00,0x01,0x00]))) return { contentType: 'image/x-icon', extension: 'ico' }
  if (ascii(0, 5) === '%PDF-') return { contentType: 'application/pdf', extension: 'pdf' }
  if (b.length >= 7 && b.subarray(0, 7).equals(Buffer.from([0x52,0x61,0x72,0x21,0x1a,0x07,0x00]))) return { contentType: 'application/vnd.rar', extension: 'rar' }
  if (b.length >= 8 && b.subarray(0, 8).equals(Buffer.from([0x52,0x61,0x72,0x21,0x1a,0x07,0x01,0x00]))) return { contentType: 'application/vnd.rar', extension: 'rar' }
  if (b.length >= 8 && b.subarray(0, 8).equals(Buffer.from([0xd0,0xcf,0x11,0xe0,0xa1,0xb1,0x1a,0xe1]))) {
    if (ext === 'doc') return { contentType: 'application/msword', extension: 'doc' }
    if (ext === 'xls') return { contentType: 'application/vnd.ms-excel', extension: 'xls' }
    if (declaredContentType === 'application/msword') return { contentType: declaredContentType, extension: 'doc' }
    if (declaredContentType === 'application/vnd.ms-excel') return { contentType: declaredContentType, extension: 'xls' }
    return { contentType: declaredContentType || 'application/x-ole-storage', extension: null }
  }
  if (ascii(0, 2) === 'PK') {
    if (ext === 'docx') return { contentType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', extension: 'docx' }
    if (ext === 'xlsx') return { contentType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', extension: 'xlsx' }
    if (declaredContentType === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document') return { contentType: declaredContentType, extension: 'docx' }
    if (declaredContentType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') return { contentType: declaredContentType, extension: 'xlsx' }
    return { contentType: declaredContentType || 'application/zip', extension: null }
  }
  return { contentType: declaredContentType || null, extension: null }
}
async function fetchResource(rawReference, baseUrl) {
  let sourceUrl
  try { sourceUrl = normalizeUrl(rawReference, baseUrl) } catch (error) { throw new Error(`resource URL invalid: ${rawReference}: ${error}`) }
  if (resourcePromises.has(sourceUrl)) return resourcePromises.get(sourceUrl)
  if (resourcePromises.size >= MAX_RESOURCE_COUNT) throw new Error(`resource count exceeds ${MAX_RESOURCE_COUNT}`)
  const promise = (async () => {
    const response = await fetchResponse(sourceUrl, 3, true)
    const bytes = await readResponseBounded(response, MAX_SINGLE_RESOURCE_BYTES)
    if (bytes.length === 0) throw new Error(`empty resource: ${sourceUrl}`)
    if (totalDownloadedResourceBytes + bytes.length > MAX_TOTAL_RESOURCE_BYTES) throw new Error(`total resource bytes exceed ${MAX_TOTAL_RESOURCE_BYTES}`)
    totalDownloadedResourceBytes += bytes.length
    const declaredContentType = (response.headers.get('content-type') || '').split(';')[0].trim() || null
    const contentDisposition = response.headers.get('content-disposition') || null
    const media = detectMedia(bytes, response.url, declaredContentType, contentDisposition)
    if (!media.extension) throw new Error(`unsupported resource media: ${sourceUrl} declared=${declaredContentType || '<none>'}`)
    const hash = sha256Bytes(bytes)
    const cachePath = path.join(tempResourceRoot, `${hash}.${media.extension}`)
    await mkdir(tempResourceRoot, { recursive: true })
    await writeFile(cachePath, bytes)
    const value = { sourceUrl: response.url, requestedUrl: sourceUrl, sha256: hash, contentType: media.contentType, contentDisposition, sizeBytes: bytes.length, extension: media.extension, cachePath }
    resourceEvidence.set(sourceUrl, { ...value, cachePath: undefined })
    return value
  })()
  resourcePromises.set(sourceUrl, promise)
  return promise
}
async function materializeResource(rawReference, baseUrl, unitDir, role, page = false) {
  const fetched = await fetchResource(rawReference, baseUrl)
  if (page && !pageResourceExtensions.has(fetched.extension)) throw new Error(`Page resource extension unsupported: ${fetched.extension}`)
  if (role === 'BODY_IMAGE' && !fetched.contentType?.startsWith('image/')) throw new Error(`BODY_IMAGE is not image: ${fetched.sourceUrl}`)
  if (role === 'LIST_IMAGE' && !fetched.contentType?.startsWith('image/')) throw new Error(`LIST_IMAGE is not image: ${fetched.sourceUrl}`)
  const relativePath = `assets/${fetched.sha256}.${fetched.extension}`
  const target = path.join(unitDir, relativePath)
  await mkdir(path.dirname(target), { recursive: true })
  await copyFile(fetched.cachePath, target)
  if (role === 'LIST_IMAGE') return { sourceUrl: fetched.sourceUrl, snapshotPath: relativePath, sha256: fetched.sha256, contentType: fetched.contentType, sizeBytes: fetched.sizeBytes }
  const token = page || role === 'BODY_IMAGE' ? `migration-resource://${fetched.sha256}` : `migration-attachment://${fetched.sha256}`
  return { role, sourceUrl: fetched.sourceUrl, originalReference: rawReference, token, snapshotPath: relativePath, sha256: fetched.sha256, contentType: fetched.contentType, sizeBytes: fetched.sizeBytes }
}
function cleanRichNode($, node, evidenceKey) {
  const forbidden = node.find('script,iframe,object,embed,form,input,button,textarea,select')
  if (forbidden.length > 0) addIssue('error', 'UNSUPPORTED_BODY_ELEMENT', { evidenceKey, count: forbidden.length })
  forbidden.remove()

  const removedEventAttributes = []
  node.find('*').addBack().each((_, element) => {
    const current = $(element)
    for (const name of Object.keys(element.attribs || {}).filter(name => name.toLowerCase().startsWith('on'))) {
      removedEventAttributes.push({ tagName: String(element.tagName || element.name || '').toLowerCase() || null, attribute: name })
      current.removeAttr(name)
    }
  })
  if (removedEventAttributes.length > 0) {
    addIssue('error', 'EVENT_HANDLER_ATTRIBUTE_REMOVED', {
      evidenceKey,
      count: removedEventAttributes.length,
      attributes: removedEventAttributes,
    })
  }
}
async function localizeBodyResources($, node, baseUrl, unitDir, evidenceKey, page = false) {
  const resources = []
  for (const image of node.find('img').toArray()) {
    const current = $(image)
    const rawReference = current.attr('src') || current.attr('data-src')
    if (!rawReference) { addIssue('error', 'BODY_IMAGE_REFERENCE_MISSING', { evidenceKey }); continue }
    if (rawReference.startsWith('data:')) { addIssue('error', 'DATA_URI_RESOURCE_UNSUPPORTED', { evidenceKey }); continue }
    try {
      const resource = await materializeResource(rawReference, baseUrl, unitDir, 'BODY_IMAGE', page)
      resources.push(resource)
      current.attr('src', resource.token)
      current.removeAttr('data-src')
    } catch (error) {
      addIssue('error', 'BODY_IMAGE_FETCH_FAILED', { evidenceKey, rawReference, message: String(error) })
    }
  }
  for (const anchor of node.find('a[href]').toArray()) {
    const current = $(anchor)
    const rawReference = current.attr('href')
    if (!rawReference || rawReference.startsWith('#') || rawReference.startsWith('mailto:') || rawReference.startsWith('tel:') || rawReference.startsWith('javascript:')) continue
    let parsed
    try { parsed = new URL(rawReference, baseUrl) } catch { continue }
    const extension = path.extname(parsed.pathname).slice(1).toLowerCase()
    const dynamicAttachment = isLegacyDynamicAttachment(parsed)
    if (!attachmentExtensions.has(extension) && !dynamicAttachment) continue
    try {
      const resource = await materializeResource(rawReference, baseUrl, unitDir, 'ATTACHMENT', page)
      resources.push(resource)
      current.attr('href', resource.token)
    } catch (error) {
      addIssue('error', 'ATTACHMENT_FETCH_FAILED', { evidenceKey, rawReference, message: String(error) })
    }
  }
  return [...new Map(resources.map(resource => [`${resource.role}:${resource.sha256}`, resource])).values()]
}
function parseDetailMetadata($) {
  const tips = $('.detail-content-title-tips > div').map((_, element) => normalizeText($(element).text())).get()
  const source = normalizeText((tips.find(value => value.startsWith('信息来源：')) || '').replace(/^信息来源：/, ''))
  const dateValue = normalizeText((tips.find(value => value.startsWith('发布时间：')) || '').replace(/^发布时间：/, ''))
  return { source, publishDate: /^\d{4}-\d{2}-\d{2}$/.test(dateValue) ? dateValue : null, rawDate: dateValue || null }
}
async function fetchDetail(item) {
  const cacheKey = item.contentId
  if (detailPromises.has(cacheKey)) return detailPromises.get(cacheKey)
  const promise = (async () => {
    const response = await fetchText(item.url)
    const $ = cheerio.load(response.text)
    const title = normalizeText($('.detail-content-title').first().text() || $('h1').first().text())
    const rich = $('.rich-text-wrap').first()
    const metadata = parseDetailMetadata($)
    if (!title || !rich.length) throw new Error(`detail structure missing title=${Boolean(title)} rich=${rich.length}`)
    cleanRichNode($, rich, `content:${item.contentId}`)
    return { requestedUrl: response.requestedUrl, finalUrl: response.finalUrl, rawHtmlSha256: sha256(response.text), title, metadata, $, rich }
  })()
  detailPromises.set(cacheKey, promise)
  return promise
}
async function mapLimit(values, limit, fn) {
  const results = new Array(values.length)
  let cursor = 0
  async function worker() {
    while (true) {
      const index = cursor++
      if (index >= values.length) return
      results[index] = await fn(values[index], index)
    }
  }
  await Promise.all(Array.from({ length: Math.min(limit, values.length) }, () => worker()))
  return results
}
function articleStableId(item) {
  return safeName(item.legacyKey.replaceAll(':', '-'))
}
async function buildExternalArticle(item) {
  const stableId = articleStableId(item)
  const unitDir = path.join(outputRoot, 'articles', stableId)
  await mkdir(unitDir, { recursive: true })
  const content = { title: item.title, source: '', publishDate: item.publishDate, bodyHtml: '', externalUrl: item.url }
  const sourceFingerprint = sha256(JSON.stringify({ identity: item.legacyKey, target: item.columnAlias, content }))
  const record = {
    source: { system: sourceSystem, legacyKey: item.legacyKey, contentId: null, typeCode: item.typeCode, detailPath: item.pathname, url: item.url },
    target: { columnAlias: item.columnAlias, articleType: 'EXTERNAL_LINK' },
    content,
    resources: [],
    sourceFingerprint,
    evidence: { listPage: item.pageNo, listTitle: item.title, listPublishDate: item.publishDate, sourceOrder: item.sourceOrder },
  }
  await writeJson(path.join(unitDir, 'article.json'), record)
  detailEvidence.push({ legacyKey: item.legacyKey, articleType: 'EXTERNAL_LINK', sourceUrl: item.url, sourceOrder: item.sourceOrder })
  return { legacyKey: item.legacyKey, path: `articles/${stableId}/article.json`, articleType: 'EXTERNAL_LINK', columnAlias: item.columnAlias, sourceFingerprint }
}
async function buildInternalArticle(item) {
  const stableId = articleStableId(item)
  const unitDir = path.join(outputRoot, 'articles', stableId)
  await mkdir(unitDir, { recursive: true })
  try {
    const detail = await fetchDetail(item)
    if (normalizeText(item.title) !== detail.title) addIssue('error', 'ARTICLE_TITLE_CONFLICT', { legacyKey: item.legacyKey, listTitle: item.title, detailTitle: detail.title })
    if (item.publishDate && detail.metadata.publishDate && item.publishDate !== detail.metadata.publishDate) addIssue('error', 'ARTICLE_DATE_CONFLICT', { legacyKey: item.legacyKey, listDate: item.publishDate, detailDate: detail.metadata.publishDate })
    const cloned = detail.rich.clone()
    const resources = await localizeBodyResources(detail.$, cloned, detail.finalUrl, unitDir, item.legacyKey, false)
    const bodyHtml = cloned.html() || ''
    const content = { title: detail.title, source: detail.metadata.source, publishDate: detail.metadata.publishDate ?? item.publishDate, bodyHtml, externalUrl: null }
    const sourceFingerprint = sha256(JSON.stringify({ identity: item.legacyKey, target: item.columnAlias, content, resources: resources.map(resource => [resource.role, resource.sha256]).sort() }))
    const record = {
      source: { system: sourceSystem, legacyKey: item.legacyKey, contentId: item.contentId, typeCode: item.typeCode, detailPath: new URL(detail.finalUrl).pathname, url: detail.finalUrl },
      target: { columnAlias: item.columnAlias, articleType: 'INTERNAL' },
      content,
      resources,
      sourceFingerprint,
      evidence: { listPage: item.pageNo, listTitle: item.title, listPublishDate: item.publishDate, sourceOrder: item.sourceOrder, detailPublishDate: detail.metadata.publishDate },
    }
    await writeJson(path.join(unitDir, 'article.json'), record)
    detailEvidence.push({ legacyKey: item.legacyKey, contentId: item.contentId, articleType: 'INTERNAL', requestedUrl: detail.requestedUrl, finalUrl: detail.finalUrl, rawHtmlSha256: detail.rawHtmlSha256, sourceOrder: item.sourceOrder, resources: resources.length })
    return { legacyKey: item.legacyKey, path: `articles/${stableId}/article.json`, articleType: 'INTERNAL', columnAlias: item.columnAlias, sourceFingerprint }
  } catch (error) {
    addIssue('error', 'ARTICLE_DETAIL_UNRESOLVED', { legacyKey: item.legacyKey, contentId: item.contentId, url: item.url, message: String(error) })
    return null
  }
}
function pageTargetKey(target) { return `${target.groupAlias || ''}\u0000${target.pageAlias ?? target.alias}` }
function targetPageDefinition(target) {
  return sitePages.find(page => pageTargetKey(page) === pageTargetKey(target)) || null
}
function pageTargetFingerprint(definition) {
  return sha256(JSON.stringify({ bodyHtml: definition.bodyHtml, renderMode: definition.renderMode, embedUrl: definition.embedUrl ?? null }))
}
async function buildPage(surface, sourceOrder) {
  const stableId = safeName(`main-page-${surface.target.groupAlias || 'root'}-${surface.target.pageAlias}`)
  const unitDir = path.join(outputRoot, 'pages', 'items', stableId)
  await mkdir(unitDir, { recursive: true })
  try {
    const response = await fetchText(new URL(surface.path, sourceRoot).toString())
    const $ = cheerio.load(response.text)
    const selector = surface.selectors.find(candidate => $(candidate).length > 0)
    if (!selector) throw new Error(`no configured selector matched ${surface.key}`)
    const selected = $(selector).first()
    if (surface.contentMode === 'RICH_TEXT') {
      const cloned = selected.clone()
      cleanRichNode($, cloned, surface.key)
      const resources = await localizeBodyResources($, cloned, response.finalUrl, unitDir, surface.key, true)
      const bodyHtml = cloned.html() || ''
      const definition = targetPageDefinition(surface.target)
      if (!definition) throw new Error(`stable Page target missing ${pageTargetKey(surface.target)}`)
      const content = { bodyHtml, renderMode: 'RICH_TEXT', embedUrl: null }
      const sourceFingerprint = sha256(JSON.stringify({ identity: `main-page:${surface.key}`, target: surface.target, content, resources: resources.map(resource => resource.sha256).sort() }))
      const record = { source: { system: sourceSystem, legacyKey: `main-page:${surface.key}`, url: response.finalUrl }, target: surface.target, content, resources, sourceFingerprint, expectedTargetFingerprint: pageTargetFingerprint(definition) }
      await writeJson(path.join(unitDir, 'page.json'), record)
      pageEvidence.push({ key: surface.key, legacyKey: record.source.legacyKey, sourceOrder, finalUrl: response.finalUrl, rawHtmlSha256: sha256(response.text), selector, contentMode: surface.contentMode, resources: resources.length })
      return { legacyKey: record.source.legacyKey, path: `items/${stableId}/page.json`, sourceOrder, sourceFingerprint }
    }
    if (surface.contentMode === 'GUIDE_CARDS') {
      const cards = selected.find('.card').toArray()
      if (cards.length === 0) throw new Error(`GUIDE_CARDS has no .card for ${surface.key}`)
      const fragments = []
      const allResources = []
      for (const [index, cardElement] of cards.entries()) {
        const card = $(cardElement)
        const title = normalizeText(card.find('.card-header .title').first().text()) || `${surface.title} ${index + 1}`
        const body = card.find('.card-body').first().clone()
        if (!body.length) throw new Error(`GUIDE_CARDS missing card-body ${surface.key}#${index + 1}`)
        cleanRichNode($, body, `${surface.key}#${index + 1}`)
        const resources = await localizeBodyResources($, body, response.finalUrl, unitDir, `${surface.key}#${index + 1}`, true)
        allResources.push(...resources)
        if (cards.length === 1) fragments.push(body.html() || '')
        else fragments.push(`<section><h2>${escapeHtml(title)}</h2>${body.html() || ''}</section>`)
      }
      const resources = [...new Map(allResources.map(resource => [resource.sha256, resource])).values()]
      const bodyHtml = fragments.join('\n')
      const definition = targetPageDefinition(surface.target)
      if (!definition) throw new Error(`stable Page target missing ${pageTargetKey(surface.target)}`)
      const content = { bodyHtml, renderMode: 'RICH_TEXT', embedUrl: null }
      const sourceFingerprint = sha256(JSON.stringify({ identity: `main-page:${surface.key}`, target: surface.target, content, resources: resources.map(resource => resource.sha256).sort() }))
      const record = { source: { system: sourceSystem, legacyKey: `main-page:${surface.key}`, url: response.finalUrl }, target: surface.target, content, resources, sourceFingerprint, expectedTargetFingerprint: pageTargetFingerprint(definition) }
      await writeJson(path.join(unitDir, 'page.json'), record)
      pageEvidence.push({ key: surface.key, legacyKey: record.source.legacyKey, sourceOrder, finalUrl: response.finalUrl, rawHtmlSha256: sha256(response.text), selector, contentMode: surface.contentMode, cards: cards.length, resources: resources.length })
      return { legacyKey: record.source.legacyKey, path: `items/${stableId}/page.json`, sourceOrder, sourceFingerprint }
    }
    throw new Error(`unsupported contentMode ${surface.contentMode}`)
  } catch (error) {
    addIssue('error', 'PAGE_COLLECTION_UNRESOLVED', { surfaceKey: surface.key, message: String(error) })
    return null
  }
}
function openMode(anchor) { return String(anchor.attr('target') || '').toLowerCase() === '_blank' ? 'NEW_WINDOW' : 'SAME_WINDOW' }
async function buildListItem(listCode, legacyKey, sourceOrder, title, url, mode, unitDir, imageReference = null, baseUrl = null) {
  let image = null
  if (imageReference) {
    try { image = await materializeResource(imageReference, baseUrl, unitDir, 'LIST_IMAGE', false) }
    catch (error) { addIssue('error', 'LIST_IMAGE_UNRESOLVED', { listCode, legacyKey, imageReference, message: String(error) }) }
  }
  const record = {
    legacyKey,
    sourceOrder,
    sourceType: 'LINK',
    title,
    subtitle: null,
    url,
    articleReference: null,
    openMode: mode,
    enabled: true,
    sourceFingerprint: sha256(JSON.stringify({ legacyKey, sourceOrder, title, url, openMode: mode, imageSha256: image?.sha256 || null })),
    image,
  }
  await writeJson(path.join(unitDir, 'item.json'), record)
  return record
}
async function buildHomeLists() {
  const response = await fetchText(new URL(config.home.path, sourceRoot).toString())
  const $ = cheerio.load(response.text)
  const result = []
  const carouselDefinition = config.home.carousel
  const carouselSelector = carouselDefinition.selectors.find(selector => $(selector).length > 0)
  if (!carouselSelector) addIssue('error', 'HOME_CAROUSEL_SELECTOR_UNRESOLVED', { listCode: carouselDefinition.listCode })
  else {
    const listCode = carouselDefinition.listCode
    const listRoot = path.join(outputRoot, 'lists', listCode)
    const references = []
    for (const [index, element] of $(carouselSelector).toArray().entries()) {
      const anchor = $(element)
      const title = normalizeText(anchor.find('.swiper-msg h6').first().text() || anchor.attr('title') || anchor.text())
      const href = anchor.attr('href')
      const imageReference = anchor.find('img[src]').first().attr('src')
      const legacyKey = `main-home-carousel:position:${index + 1}`
      if (!title || !href || !imageReference) { addIssue('error', 'HOME_CAROUSEL_ITEM_STRUCTURE_MISSING', { legacyKey, title: Boolean(title), href: Boolean(href), image: Boolean(imageReference) }); continue }
      let url
      try { url = normalizeUrl(href, response.finalUrl) } catch (error) { addIssue('error', 'HOME_CAROUSEL_URL_INVALID', { legacyKey, href, message: String(error) }); continue }
      const stableId = safeName(legacyKey.replaceAll(':', '-'))
      const unitDir = path.join(listRoot, 'items', stableId)
      await mkdir(unitDir, { recursive: true })
      const record = await buildListItem(listCode, legacyKey, index + 1, title, url, openMode(anchor), unitDir, imageReference, response.finalUrl)
      references.push({ legacyKey, path: `items/${stableId}/item.json`, sourceOrder: record.sourceOrder, sourceFingerprint: record.sourceFingerprint })
      listEvidence.push({ listCode, legacyKey, sourceOrder: record.sourceOrder, sourceUrl: url, sourceTarget: anchor.attr('target') || null, imageReference })
    }
    await writeJson(path.join(listRoot, 'index.json'), { listCode, sourceSystem, sourcePage: response.finalUrl, items: references })
    result.push({ listCode, count: references.length })
  }
  for (const definition of config.home.siteLinkLists || []) {
    const selector = definition.selectors.find(candidate => $(candidate).length > 0)
    if (!selector) { addIssue('error', 'HOME_SITE_LINK_SELECTOR_UNRESOLVED', { listCode: definition.listCode }); continue }
    const listCode = definition.listCode
    const listRoot = path.join(outputRoot, 'lists', listCode)
    const references = []
    const seenUrls = new Set()
    for (const [index, element] of $(selector).toArray().entries()) {
      const anchor = $(element)
      const title = normalizeText(anchor.text() || anchor.attr('title'))
      const href = anchor.attr('href')
      if (!title || !href) { addIssue('error', 'HOME_SITE_LINK_STRUCTURE_MISSING', { listCode, index: index + 1 }); continue }
      let url
      try { url = normalizeUrl(href, response.finalUrl) } catch (error) { addIssue('error', 'HOME_SITE_LINK_URL_INVALID', { listCode, href, message: String(error) }); continue }
      if (seenUrls.has(url)) { addIssue('error', 'HOME_SITE_LINK_DUPLICATE_URL', { listCode, url }); continue }
      seenUrls.add(url)
      const legacyKey = `main-${listCode.toLowerCase().replaceAll('_', '-')}:url:${sha256(url)}`
      const stableId = safeName(legacyKey.replaceAll(':', '-'))
      const unitDir = path.join(listRoot, 'items', stableId)
      await mkdir(unitDir, { recursive: true })
      const record = await buildListItem(listCode, legacyKey, index + 1, title, url, openMode(anchor), unitDir)
      references.push({ legacyKey, path: `items/${stableId}/item.json`, sourceOrder: record.sourceOrder, sourceFingerprint: record.sourceFingerprint })
      listEvidence.push({ listCode, legacyKey, sourceOrder: record.sourceOrder, sourceUrl: url, sourceTarget: anchor.attr('target') || null })
    }
    await writeJson(path.join(listRoot, 'index.json'), { listCode, sourceSystem, sourcePage: response.finalUrl, items: references })
    result.push({ listCode, count: references.length })
  }
  return { sourcePage: response.finalUrl, rawHtmlSha256: sha256(response.text), lists: result }
}
async function listFiles(root, prefix = '') {
  const entries = await readdir(root, { withFileTypes: true })
  const files = []
  for (const entry of entries) {
    if (entry.name === '.resource-cache') continue
    const absolute = path.join(root, entry.name)
    const relative = prefix ? `${prefix}/${entry.name}` : entry.name
    if (entry.isDirectory()) files.push(...await listFiles(absolute, relative))
    else if (entry.isFile()) files.push({ absolute, relative: relative.replaceAll('\\', '/') })
  }
  return files
}
async function canonicalDatasetDigest() {
  const prefixes = ['index.ndjson', 'articles/', 'pages/', 'lists/']
  const files = (await listFiles(outputRoot)).filter(file => prefixes.some(prefix => file.relative === prefix || file.relative.startsWith(prefix)))
  const rows = []
  for (const file of files.sort((a, b) => a.relative.localeCompare(b.relative))) rows.push(`${file.relative}\u0000${sha256Bytes(await readFile(file.absolute))}`)
  return { digest: `sha256:${sha256(rows.join('\n'))}`, files: rows.length }
}
async function main() {
  if (!Number.isInteger(MAX_CANDIDATES) || MAX_CANDIDATES <= 0 || inventory.length > MAX_CANDIDATES) throw new Error(`inventory candidates ${inventory.length} exceeds max ${MAX_CANDIDATES}`)
  if (inventorySummary.errors !== 0 || inventorySummary.resolvedSurfaces !== inventorySummary.surfaceCount) throw new Error('inventory evidence is not clean')
  await rm(outputRoot, { recursive: true, force: true })
  await mkdir(outputRoot, { recursive: true })

  const articleRefs = await mapLimit(inventory, DETAIL_CONCURRENCY, async (item, index) => {
    process.stdout.write(`article ${index + 1}/${inventory.length} ${item.legacyKey}\n`)
    return item.articleType === 'INTERNAL' ? buildInternalArticle(item) : buildExternalArticle(item)
  })
  const acceptedArticleRefs = articleRefs.filter(Boolean)
  await writeFile(path.join(outputRoot, 'index.ndjson'), acceptedArticleRefs.map(reference => JSON.stringify({ legacyKey: reference.legacyKey, path: reference.path })).join('\n') + (acceptedArticleRefs.length ? '\n' : ''), 'utf8')

  const pageRefs = []
  for (const [index, surface] of config.pageSurfaces.entries()) {
    const reference = await buildPage(surface, index + 1)
    if (reference) pageRefs.push(reference)
  }
  await mkdir(path.join(outputRoot, 'pages'), { recursive: true })
  await writeJson(path.join(outputRoot, 'pages', 'index.json'), { sourceSystem, items: pageRefs })

  const home = await buildHomeLists()
  const listCount = home.lists.reduce((sum, item) => sum + item.count, 0)
  const errors = issues.filter(issue => issue.level === 'error')
  const warnings = issues.filter(issue => issue.level === 'warning')
  const discoveredUniqueCandidates = inventory.length + config.pageSurfaces.length + listCount
  const acceptedCanonical = acceptedArticleRefs.length + pageRefs.length + listCount
  const unresolved = errors.length
  const arithmeticClosed = discoveredUniqueCandidates === acceptedCanonical && unresolved === 0

  await mkdir(path.join(outputRoot, 'reports'), { recursive: true })
  await mkdir(path.join(outputRoot, 'source-discovery'), { recursive: true })
  await writeJson(path.join(outputRoot, 'reports', 'issues.json'), issues)
  await writeJson(path.join(outputRoot, 'reports', 'detail-evidence.json'), detailEvidence)
  await writeJson(path.join(outputRoot, 'reports', 'page-evidence.json'), pageEvidence)
  await writeJson(path.join(outputRoot, 'reports', 'list-evidence.json'), listEvidence)
  await writeJson(path.join(outputRoot, 'reports', 'resource-evidence.json'), [...resourceEvidence.values()].sort((a, b) => a.sourceUrl.localeCompare(b.sourceUrl)))
  await writeJson(path.join(outputRoot, 'source-discovery', 'inventory-summary.json'), inventorySummary)
  await writeJson(path.join(outputRoot, 'source-discovery', 'surface-reconciliation.json'), surfaceReconciliation)
  await writeJson(path.join(outputRoot, 'source-discovery', 'cross-surface.json'), crossSurface)
  await writeJson(path.join(outputRoot, 'source-discovery', 'scope-exclusions.json'), config.excludedStablePages || [])
  await writeJson(path.join(outputRoot, 'source-discovery', 'source-surfaces.json'), config)
  await writeJson(path.join(outputRoot, 'source-discovery', 'collection-observations.json'), observations)

  await rm(tempResourceRoot, { recursive: true, force: true })
  const integrity = await canonicalDatasetDigest()
  const reconciliation = {
    collectedAt: new Date().toISOString(),
    sourceSystem,
    sourceRoot: config.sourceRoot,
    inventoryReportedRows: inventorySummary.reportedRecords,
    inventoryUniqueSurfaceOccurrences: inventory.length,
    withinSurfaceDuplicateOccurrences: inventorySummary.duplicateOccurrences,
    crossSurfaceInternalIdentities: inventorySummary.crossSurfaceInternalIdentities,
    articles: { discovered: inventory.length, accepted: acceptedArticleRefs.length, internal: acceptedArticleRefs.filter(item => item.articleType === 'INTERNAL').length, external: acceptedArticleRefs.filter(item => item.articleType === 'EXTERNAL_LINK').length },
    pages: { discovered: config.pageSurfaces.length, accepted: pageRefs.length },
    lists: { discovered: listCount, accepted: listCount, byCode: home.lists },
    resources: { uniqueDownloaded: resourceEvidence.size, downloadedBytes: totalDownloadedResourceBytes },
    discoveredUniqueCandidates,
    acceptedCanonical,
    excluded: 0,
    unresolved,
    warnings: warnings.length,
    arithmeticClosed,
    datasetDigest: integrity.digest,
    datasetFiles: integrity.files,
    promotionReady: arithmeticClosed && acceptedCanonical > 0,
  }
  await writeJson(path.join(outputRoot, 'reports', 'reconciliation.json'), reconciliation)
  await writeJson(path.join(outputRoot, 'manifest.json'), {
    version: 1,
    site: 'main',
    status: 'candidate',
    sourceSystem,
    sourceRoot: config.sourceRoot,
    collectedAt: reconciliation.collectedAt,
    acceptedSnapshot: null,
    candidate: {
      articles: reconciliation.articles.accepted,
      internalArticles: reconciliation.articles.internal,
      externalArticles: reconciliation.articles.external,
      pages: reconciliation.pages.accepted,
      listItems: reconciliation.lists.accepted,
      resources: reconciliation.resources.uniqueDownloaded,
      unresolved: reconciliation.unresolved,
      datasetDigest: reconciliation.datasetDigest,
      datasetFiles: reconciliation.datasetFiles,
    },
  })
  console.log(`EU50_MAIN_COLLECTION ${JSON.stringify(reconciliation)}`)
  if (!reconciliation.promotionReady) process.exitCode = 1
}

await main()
