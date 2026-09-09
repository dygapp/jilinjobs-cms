import * as cheerio from 'cheerio'
import { createHash } from 'node:crypto'
import { copyFile, mkdir, readFile, writeFile } from 'node:fs/promises'
import path from 'node:path'

const configPath = path.resolve(process.env.MAIN_SOURCE_CONFIG || 'main/source-surfaces.json')
const candidateRoot = path.resolve(process.env.MAIN_CANDIDATE_ROOT || 'main/v1/generated-candidate')
const reportsRoot = path.join(candidateRoot, 'reports')
const problemIndexPath = path.resolve(process.env.MAIN_PROBLEM_INDEX || path.join(reportsRoot, 'collection-problem-index.json'))
const requestedAttempt = Number(process.env.MAIN_RETRY_ATTEMPT || 2)
const concurrency = Math.max(1, Math.min(4, Number(process.env.MAIN_RETRY_CONCURRENCY || 2)))
const timeoutMs = Math.max(1_000, Number(process.env.MAIN_RETRY_TIMEOUT_MS || 20_000))
const maxBytes = Math.max(1_024, Number(process.env.MAIN_MAX_SINGLE_RESOURCE_BYTES || 50 * 1024 * 1024))

const config = JSON.parse(await readFile(configPath, 'utf8'))
const problemIndex = JSON.parse(await readFile(problemIndexPath, 'utf8'))
const internalHosts = new Set(config.allowedHosts || [new URL(config.sourceRoot).host])
const maxAttempts = Number(problemIndex.maxAttempts || 3)
const attempt = Math.max(2, requestedAttempt)
if (attempt > maxAttempts) throw new Error(`retry attempt ${attempt} exceeds maxAttempts=${maxAttempts}`)

const inputReportPath = attempt === 2
  ? null
  : path.resolve(process.env.MAIN_RETRY_INPUT || path.join(reportsRoot, `targeted-retry-attempt-${attempt - 1}.json`))
const inputReport = inputReportPath ? JSON.parse(await readFile(inputReportPath, 'utf8')) : null
const inputQueue = attempt === 2 ? (problemIndex.retryQueue || []) : (inputReport.nextRetryQueue || [])

const indexRows = (await readFile(path.join(candidateRoot, 'index.ndjson'), 'utf8'))
  .split(/\r?\n/).filter(Boolean).map(line => JSON.parse(line))
const articlePathByLegacyKey = new Map(indexRows.map(row => [row.legacyKey, row.path]))
const cacheRoot = path.join(candidateRoot, '.targeted-retry-cache', `attempt-${attempt}`)

const attachmentExtensions = new Set(['pdf', 'doc', 'docx', 'xls', 'xlsx', 'rar'])
const pageResourceExtensions = new Set(['png', 'jpg', 'jpeg', 'gif', 'webp', 'ico', 'pdf', 'doc', 'docx', 'xls', 'xlsx', 'rar'])
const sha256 = value => createHash('sha256').update(value).digest('hex')
const sha256Bytes = bytes => createHash('sha256').update(bytes).digest('hex')

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
function detectMedia(bytes, url, declaredContentType, contentDisposition = null) {
  const b = Buffer.from(bytes)
  const ascii = (start, end) => b.subarray(start, end).toString('ascii')
  const urlExt = sourceExtension(url)
  const dispositionExt = contentDispositionExtension(contentDisposition)
  const ext = attachmentExtensions.has(urlExt) || pageResourceExtensions.has(urlExt) ? urlExt : dispositionExt || urlExt
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

async function readResponseBounded(response) {
  const declared = Number(response.headers.get('content-length') || 0)
  if (declared > maxBytes) return { error: `content-length ${declared} exceeds max ${maxBytes}` }
  const chunks = []
  let size = 0
  if (!response.body) return { bytes: Buffer.alloc(0) }
  const reader = response.body.getReader()
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    size += value.byteLength
    if (size > maxBytes) {
      await reader.cancel()
      return { error: `resource bytes ${size} exceeds max ${maxBytes}` }
    }
    chunks.push(Buffer.from(value))
  }
  return { bytes: Buffer.concat(chunks) }
}

function errorEvidence(error) {
  const cause = error?.cause
  return {
    name: error?.name || null,
    code: error?.code || cause?.code || null,
    message: error?.message || String(error),
    causeCode: cause?.code || null,
    causeMessage: cause?.message || null,
  }
}

async function fetchTarget(targetUrl, redirectBudget = 5, redirectChain = []) {
  let parsed
  try { parsed = new URL(targetUrl) }
  catch (error) { return { kind: 'INVALID_URL', classification: 'RESOURCE_URL_INVALID_REQUIRES_REVIEW', error: errorEvidence(error), redirectChain } }
  if (!['http:', 'https:'].includes(parsed.protocol)) return { kind: 'NON_HTTP', classification: 'RESOURCE_SCHEME_UNSUPPORTED_REQUIRES_REVIEW', finalUrl: parsed.href, redirectChain }
  if (!internalHosts.has(parsed.host)) return { kind: 'OUTSIDE_INTERNAL_HOST', classification: 'EXTERNAL_ABSOLUTE_RESOURCE_REFERENCE', finalUrl: parsed.href, nonBlocking: true, redirectChain }

  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), timeoutMs)
  try {
    const response = await fetch(parsed, {
      method: 'GET',
      headers: { 'user-agent': 'jilinjobs-cms-eu50-targeted-retry/1.0', accept: '*/*' },
      redirect: 'manual',
      signal: controller.signal,
    })
    const status = response.status
    const location = response.headers.get('location')
    if (status >= 300 && status < 400 && location) {
      if (redirectBudget <= 0) return { kind: 'REDIRECT_LIMIT', classification: 'RESOURCE_REDIRECT_LIMIT_REQUIRES_REVIEW', status, finalUrl: parsed.href, redirectChain }
      const next = new URL(location, parsed)
      const hop = { url: parsed.href, status, location: next.href }
      if (!internalHosts.has(next.host)) {
        return { kind: 'REDIRECT_OUTSIDE_INTERNAL_HOST', classification: 'INTERNAL_RESOURCE_REDIRECTS_EXTERNAL_REQUIRES_REVIEW', status, finalUrl: next.href, redirectChain: [...redirectChain, hop] }
      }
      return fetchTarget(next.href, redirectBudget - 1, [...redirectChain, hop])
    }
    if ([404, 410].includes(status)) return { kind: 'SOURCE_MISSING', classification: 'SOURCE_RESOURCE_MISSING', status, finalUrl: response.url || parsed.href, redirectChain }
    if (status < 200 || status >= 300) return { kind: 'HTTP_STATUS', classification: 'RESOURCE_HTTP_STATUS_REQUIRES_REVIEW', status, finalUrl: response.url || parsed.href, redirectChain }

    const bounded = await readResponseBounded(response)
    if (bounded.error) return { kind: 'RESOURCE_LIMIT', classification: 'RESOURCE_SIZE_LIMIT_REQUIRES_REVIEW', status, finalUrl: response.url || parsed.href, message: bounded.error, redirectChain }
    if (!bounded.bytes || bounded.bytes.length === 0) return { kind: 'EMPTY_RESOURCE', classification: 'EMPTY_RESOURCE_RESPONSE_REQUIRES_REVIEW', status, finalUrl: response.url || parsed.href, redirectChain }
    const declaredContentType = (response.headers.get('content-type') || '').split(';')[0].trim() || null
    const contentDisposition = response.headers.get('content-disposition') || null
    const media = detectMedia(bounded.bytes, response.url || parsed.href, declaredContentType, contentDisposition)
    if (!media.extension) return { kind: 'UNSUPPORTED_MEDIA', classification: 'RESOURCE_TYPE_MISMATCH', status, finalUrl: response.url || parsed.href, declaredContentType, contentDisposition, redirectChain }
    const hash = sha256Bytes(bounded.bytes)
    await mkdir(cacheRoot, { recursive: true })
    const cachePath = path.join(cacheRoot, `${hash}.${media.extension}`)
    await writeFile(cachePath, bounded.bytes)
    return {
      kind: 'COLLECTED',
      classification: 'RETRY_RESOLVED_COLLECTED',
      status,
      finalUrl: response.url || parsed.href,
      sha256: hash,
      contentType: media.contentType,
      contentDisposition,
      extension: media.extension,
      sizeBytes: bounded.bytes.length,
      cachePath,
      redirectChain,
    }
  } catch (error) {
    return { kind: 'TRANSPORT_ERROR', classification: attempt >= maxAttempts ? 'UNCLASSIFIED_RETRY_EXHAUSTED_REQUIRES_REVIEW' : 'RETRYABLE_TRANSIENT', error: errorEvidence(error), finalUrl: parsed.href, redirectChain }
  } finally {
    clearTimeout(timer)
  }
}

async function mapConcurrent(items, limit, mapper) {
  const values = new Array(items.length)
  let cursor = 0
  async function worker() {
    while (true) {
      const index = cursor++
      if (index >= items.length) return
      values[index] = await mapper(items[index], index)
    }
  }
  await Promise.all(Array.from({ length: Math.min(limit, Math.max(1, items.length)) }, () => worker()))
  return values
}

function resolveReference(raw, baseUrl) {
  try { return new URL(raw, baseUrl).href } catch { return null }
}

const collectionIssueCodes = new Set(['BODY_IMAGE_FETCH_FAILED', 'ATTACHMENT_FETCH_FAILED'])
const occurrenceByIdentity = new Map()
for (const record of problemIndex.records || []) {
  if (!collectionIssueCodes.has(record.code)) continue
  if (!occurrenceByIdentity.has(record.identity)) occurrenceByIdentity.set(record.identity, [])
  occurrenceByIdentity.get(record.identity).push(record)
}

async function applyCollected(target, fetched) {
  const occurrences = occurrenceByIdentity.get(target.identity) || []
  const byArticle = new Map()
  for (const occurrence of occurrences) {
    if (!occurrence.legacyKey || !occurrence.rawReference) continue
    if (!byArticle.has(occurrence.legacyKey)) byArticle.set(occurrence.legacyKey, [])
    byArticle.get(occurrence.legacyKey).push(occurrence)
  }
  let appliedOccurrences = 0
  const affectedArticles = []
  const failures = []

  for (const [legacyKey, articleOccurrences] of byArticle) {
    const relativeArticlePath = articlePathByLegacyKey.get(legacyKey)
    if (!relativeArticlePath) {
      failures.push({ legacyKey, reason: 'ARTICLE_NOT_PRESENT_IN_FULL_SNAPSHOT' })
      continue
    }
    const articlePath = path.join(candidateRoot, relativeArticlePath)
    const article = JSON.parse(await readFile(articlePath, 'utf8'))
    const articleDir = path.dirname(articlePath)
    const sourceUrl = article.source?.url || articleOccurrences[0].sourceUrl || config.sourceRoot
    const $ = cheerio.load(String(article.content?.bodyHtml || ''), null, false)
    const tokenByRole = {
      BODY_IMAGE: `migration-resource://${fetched.sha256}`,
      ATTACHMENT: `migration-attachment://${fetched.sha256}`,
    }
    const matched = []

    for (const occurrence of articleOccurrences) {
      const role = occurrence.code === 'BODY_IMAGE_FETCH_FAILED' ? 'BODY_IMAGE' : 'ATTACHMENT'
      const token = tokenByRole[role]
      const rawReference = occurrence.rawReference
      if (role === 'BODY_IMAGE') {
        for (const element of $('img').toArray()) {
          const current = $(element)
          const currentRaw = current.attr('src') || current.attr('data-src')
          if (!currentRaw || resolveReference(currentRaw, sourceUrl) !== target.targetUrl) continue
          current.attr('src', token)
          current.removeAttr('data-src')
          matched.push({ role, rawReference })
        }
      } else {
        for (const element of $('a[href]').toArray()) {
          const current = $(element)
          const currentRaw = current.attr('href')
          if (!currentRaw || resolveReference(currentRaw, sourceUrl) !== target.targetUrl) continue
          current.attr('href', token)
          matched.push({ role, rawReference })
        }
      }
    }

    if (matched.length === 0) {
      failures.push({ legacyKey, reason: 'BODY_REFERENCE_NOT_FOUND', targetUrl: target.targetUrl })
      continue
    }

    const relativeAssetPath = `assets/${fetched.sha256}.${fetched.extension}`
    await mkdir(path.join(articleDir, 'assets'), { recursive: true })
    await copyFile(fetched.cachePath, path.join(articleDir, relativeAssetPath))
    article.content.bodyHtml = $.html()
    article.resources ||= []
    for (const match of matched) {
      const token = tokenByRole[match.role]
      const duplicate = article.resources.some(resource => resource.role === match.role && resource.token === token && resource.originalReference === match.rawReference)
      if (duplicate) continue
      article.resources.push({
        role: match.role,
        sourceUrl: fetched.finalUrl,
        originalReference: match.rawReference,
        token,
        snapshotPath: relativeAssetPath,
        sha256: fetched.sha256,
        contentType: fetched.contentType,
        sizeBytes: fetched.sizeBytes,
      })
    }
    article.sourceFingerprint = sha256(JSON.stringify({
      identity: legacyKey,
      target: article.target?.columnAlias || null,
      content: article.content,
      resources: article.resources.map(resource => [resource.role, resource.sha256]).sort(),
    }))
    await writeFile(articlePath, `${JSON.stringify(article, null, 2)}\n`, 'utf8')
    appliedOccurrences += matched.length
    affectedArticles.push(legacyKey)
  }

  return { occurrenceCount: occurrences.length, appliedOccurrences, affectedArticles: [...new Set(affectedArticles)].sort(), applyFailures: failures }
}

const fetchedRows = await mapConcurrent(inputQueue, concurrency, async target => ({ target, fetched: await fetchTarget(target.targetUrl) }))
const results = []
const nextRetryQueue = []

for (const { target, fetched } of fetchedRows) {
  let apply = null
  let outcome = 'TERMINAL_CLASSIFIED'
  let classification = fetched.classification
  if (fetched.kind === 'COLLECTED') {
    apply = await applyCollected(target, fetched)
    if (apply.applyFailures.length === 0 && apply.appliedOccurrences > 0) {
      outcome = 'RESOLVED_COLLECTED'
    } else {
      outcome = 'TERMINAL_CLASSIFIED'
      classification = 'RETRY_COLLECTED_APPLY_MISMATCH_REQUIRES_REVIEW'
    }
  } else if (fetched.kind === 'TRANSPORT_ERROR' && attempt < maxAttempts) {
    outcome = 'RETRYABLE_TRANSIENT'
    nextRetryQueue.push({ ...target, attempt, remainingAttempts: maxAttempts - attempt })
  } else if (fetched.kind === 'SOURCE_MISSING') {
    outcome = 'TERMINAL_SOURCE_MISSING'
  } else if (fetched.kind === 'OUTSIDE_INTERNAL_HOST') {
    outcome = 'TERMINAL_NON_BLOCKING'
  } else if (fetched.kind === 'TRANSPORT_ERROR') {
    outcome = 'TERMINAL_RETRY_EXHAUSTED'
  }
  results.push({
    identity: target.identity,
    candidateType: target.candidateType,
    legacyKey: target.legacyKey,
    targetUrl: target.targetUrl,
    attempt,
    maxAttempts,
    outcome,
    classification,
    fetched: { ...fetched, cachePath: fetched.cachePath ? path.relative(candidateRoot, fetched.cachePath).replaceAll('\\', '/') : null },
    apply,
  })
}

function countBy(values, key) {
  const counts = new Map()
  for (const value of values) counts.set(String(value[key] ?? '<none>'), (counts.get(String(value[key] ?? '<none>')) || 0) + 1)
  return Object.fromEntries([...counts.entries()].sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0])))
}

const report = {
  generatedAt: new Date().toISOString(),
  purpose: 'EU-50 bounded targeted retry; retries only prior transient internal resource failures and never re-runs the full collection',
  attempt,
  maxAttempts,
  inputTargets: inputQueue.length,
  policy: {
    perTargetNetworkAttemptThisIteration: 1,
    internalHosts: [...internalHosts].sort(),
    recognizedErrorsStopRetry: true,
    sourceMissing: 'HTTP 404/410 only',
    exhaustedClassification: 'UNCLASSIFIED_RETRY_EXHAUSTED_REQUIRES_REVIEW',
    fullCollectionRerun: false,
  },
  summary: {
    inputTargets: inputQueue.length,
    resolvedCollected: results.filter(row => row.outcome === 'RESOLVED_COLLECTED').length,
    retryableTransient: results.filter(row => row.outcome === 'RETRYABLE_TRANSIENT').length,
    terminalSourceMissing: results.filter(row => row.outcome === 'TERMINAL_SOURCE_MISSING').length,
    terminalNonBlocking: results.filter(row => row.outcome === 'TERMINAL_NON_BLOCKING').length,
    terminalRetryExhausted: results.filter(row => row.outcome === 'TERMINAL_RETRY_EXHAUSTED').length,
    terminalClassified: results.filter(row => row.outcome === 'TERMINAL_CLASSIFIED').length,
    byClassification: countBy(results, 'classification'),
    byOutcome: countBy(results, 'outcome'),
  },
  nextRetryQueue: nextRetryQueue.sort((a, b) => a.identity.localeCompare(b.identity)),
  results: results.sort((a, b) => a.identity.localeCompare(b.identity)),
}
await writeFile(path.join(reportsRoot, `targeted-retry-attempt-${attempt}.json`), `${JSON.stringify(report, null, 2)}\n`, 'utf8')
const md = [
  `# EU-50 Targeted Retry Attempt ${attempt}`,
  '',
  `- Input targets: ${report.summary.inputTargets}`,
  `- Resolved by collection: ${report.summary.resolvedCollected}`,
  `- Remaining transient retry targets: ${report.summary.retryableTransient}`,
  `- Source missing (404/410): ${report.summary.terminalSourceMissing}`,
  `- Terminal classified: ${report.summary.terminalClassified}`,
  `- Retry exhausted: ${report.summary.terminalRetryExhausted}`,
  '',
  '> This iteration does not re-run source inventory or successful collection records. Each retry target receives one network attempt in this iteration.',
  '',
].join('\n')
await writeFile(path.join(reportsRoot, `targeted-retry-attempt-${attempt}.md`), `${md}\n`, 'utf8')
console.log(`EU50_MAIN_TARGETED_RETRY ${JSON.stringify(report.summary)}`)
