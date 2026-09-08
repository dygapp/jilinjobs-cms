import fs from 'node:fs'
import path from 'node:path'
import http from 'node:http'
import https from 'node:https'

const candidateRoot = path.resolve(process.env.MAIN_CANDIDATE_ROOT || 'main/v1/generated-candidate')
const reportRoot = path.join(candidateRoot, 'reports')
const manualReviewPath = path.join(reportRoot, 'manual-review.json')
const sourceConfigPath = path.resolve(process.env.MAIN_SOURCE_CONFIG || 'main/source-surfaces.json')
const outputPath = path.join(reportRoot, 'resource-gap-probe.json')
const markdownPath = path.join(reportRoot, 'resource-gap-probe.md')
const maxTargets = Number(process.env.MAIN_GAP_PROBE_MAX || 1000)
const concurrency = Math.max(1, Math.min(4, Number(process.env.MAIN_GAP_PROBE_CONCURRENCY || 2)))
const timeoutMs = Math.max(1000, Number(process.env.MAIN_GAP_PROBE_TIMEOUT_MS || 15000))
const interRequestDelayMs = Math.max(0, Number(process.env.MAIN_GAP_PROBE_DELAY_MS || 75))

const targetClassifications = new Set([
  'RESOURCE_FETCH_FAILED',
  'RELATIVE_BODY_LINK_REQUIRES_REVIEW',
  'LEGACY_DYNAMIC_ATTACHMENT_LINK_REQUIRES_REVIEW',
  'RESOURCE_COLLECTION_FAILED',
])

function readJson(filePath) {
  return JSON.parse(fs.readFileSync(filePath, 'utf8'))
}

function delay(ms) {
  return ms > 0 ? new Promise(resolve => setTimeout(resolve, ms)) : Promise.resolve()
}

function normalizeHeaders(headers) {
  return Object.fromEntries(
    Object.entries(headers || {})
      .filter(([, value]) => value !== undefined)
      .map(([name, value]) => [name.toLowerCase(), Array.isArray(value) ? value.join(', ') : String(value)]),
  )
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

function requestOnce(urlValue, method, allowedHosts, redirectChain = [], redirectBudget = 5) {
  return new Promise(resolve => {
    let parsed
    try {
      parsed = new URL(urlValue)
    } catch (error) {
      resolve({ ok: false, kind: 'INVALID_URL', url: urlValue, error: errorEvidence(error), redirectChain })
      return
    }

    if (!['http:', 'https:'].includes(parsed.protocol)) {
      resolve({ ok: false, kind: 'NON_HTTP_REFERENCE', url: parsed.href, protocol: parsed.protocol, redirectChain })
      return
    }
    if (!allowedHosts.has(parsed.host)) {
      resolve({ ok: false, kind: 'HOST_OUTSIDE_APPROVED_BOUNDARY', url: parsed.href, host: parsed.host, redirectChain })
      return
    }

    const client = parsed.protocol === 'https:' ? https : http
    const headers = {
      'User-Agent': 'jilinjobs-eu50-resource-gap-probe/1.0',
      Accept: '*/*',
      Connection: 'close',
    }
    if (method === 'GET') headers.Range = 'bytes=0-0'

    const request = client.request(parsed, { method, headers, agent: false }, response => {
      const status = Number(response.statusCode || 0)
      const responseHeaders = normalizeHeaders(response.headers)
      const location = responseHeaders.location || null
      const current = {
        url: parsed.href,
        status,
        location,
      }

      if (status >= 300 && status < 400 && location) {
        response.destroy()
        if (redirectBudget <= 0) {
          resolve({ ok: false, kind: 'REDIRECT_LIMIT', url: parsed.href, status, headers: responseHeaders, redirectChain: [...redirectChain, current] })
          return
        }
        let next
        try {
          next = new URL(location, parsed).href
        } catch (error) {
          resolve({ ok: false, kind: 'INVALID_REDIRECT', url: parsed.href, status, headers: responseHeaders, redirectChain: [...redirectChain, current], error: errorEvidence(error) })
          return
        }
        requestOnce(next, method, allowedHosts, [...redirectChain, current], redirectBudget - 1).then(resolve)
        return
      }

      response.destroy()
      resolve({
        ok: true,
        kind: 'HTTP_RESPONSE',
        method,
        url: parsed.href,
        status,
        headers: responseHeaders,
        redirectChain,
      })
    })

    request.setTimeout(timeoutMs, () => request.destroy(Object.assign(new Error(`request timeout after ${timeoutMs}ms`), { code: 'ETIMEDOUT' })))
    request.on('error', error => resolve({ ok: false, kind: 'TRANSPORT_ERROR', method, url: parsed.href, error: errorEvidence(error), redirectChain }))
    request.end()
  })
}

async function probeTarget(target, allowedHosts) {
  const dynamicAttachment = target.classifications.includes('LEGACY_DYNAMIC_ATTACHMENT_LINK_REQUIRES_REVIEW')
  const head = await requestOnce(target.url, 'HEAD', allowedHosts)
  await delay(interRequestDelayMs)

  const headStatus = head.ok ? head.status : null
  const headSuccessful = head.ok && headStatus >= 200 && headStatus < 400
  const needGet = dynamicAttachment || !headSuccessful
  const get = needGet ? await requestOnce(target.url, 'GET', allowedHosts) : null
  if (needGet) await delay(interRequestDelayMs)

  const final = get || head
  const finalStatus = final.ok ? final.status : null
  const missingConfirmed = Boolean(get?.ok && [404, 410].includes(get.status))
  const reachable = Boolean(final.ok && finalStatus >= 200 && finalStatus < 400)
  const transportFailure = final.kind === 'TRANSPORT_ERROR'

  return {
    ...target,
    head,
    get,
    finalStatus,
    finalUrl: final.url || target.url,
    missingConfirmed,
    reachable,
    transportFailure,
    contentType: final.headers?.['content-type'] || null,
    contentLength: final.headers?.['content-length'] || null,
    contentDisposition: final.headers?.['content-disposition'] || null,
  }
}

function targetUrl(issue, sourceRoot) {
  const value = issue.resolvedUrl || issue.rawReference
  if (!value) return null
  try {
    return new URL(value, sourceRoot).href
  } catch {
    return null
  }
}

function collectTargets(manualReview, sourceRoot) {
  const byUrl = new Map()
  const skipped = []

  for (const record of manualReview.records || []) {
    for (const issue of record.issues || []) {
      if (!targetClassifications.has(issue.classification)) continue
      const url = targetUrl(issue, sourceRoot)
      if (!url) {
        skipped.push({
          reason: 'URL_UNRESOLVED',
          legacyKey: record.legacyKey || null,
          classification: issue.classification,
          code: issue.code || null,
          rawReference: issue.rawReference || null,
        })
        continue
      }

      let parsed
      try {
        parsed = new URL(url)
      } catch {
        skipped.push({ reason: 'URL_INVALID', legacyKey: record.legacyKey || null, classification: issue.classification, code: issue.code || null, url })
        continue
      }
      if (!['http:', 'https:'].includes(parsed.protocol)) {
        skipped.push({
          reason: 'NON_HTTP_REFERENCE',
          legacyKey: record.legacyKey || null,
          classification: issue.classification,
          code: issue.code || null,
          rawReference: issue.rawReference || null,
          url: parsed.href,
        })
        continue
      }

      const key = parsed.href
      if (!byUrl.has(key)) {
        byUrl.set(key, {
          url: key,
          classifications: [],
          issueCodes: [],
          evidenceKeys: [],
          rawReferences: [],
          occurrences: 0,
        })
      }
      const entry = byUrl.get(key)
      entry.occurrences += 1
      if (issue.classification && !entry.classifications.includes(issue.classification)) entry.classifications.push(issue.classification)
      if (issue.code && !entry.issueCodes.includes(issue.code)) entry.issueCodes.push(issue.code)
      const evidenceKey = issue.evidenceKey || record.legacyKey
      if (evidenceKey && !entry.evidenceKeys.includes(evidenceKey)) entry.evidenceKeys.push(evidenceKey)
      if (issue.rawReference && !entry.rawReferences.includes(issue.rawReference)) entry.rawReferences.push(issue.rawReference)
    }
  }

  const targets = [...byUrl.values()]
    .map(entry => ({
      ...entry,
      classifications: entry.classifications.sort(),
      issueCodes: entry.issueCodes.sort(),
      evidenceKeys: entry.evidenceKeys.sort(),
      rawReferences: entry.rawReferences.sort(),
    }))
    .sort((a, b) => a.url.localeCompare(b.url))

  return { targets, skipped }
}

async function mapConcurrent(items, limit, mapper) {
  const results = new Array(items.length)
  let cursor = 0
  async function worker() {
    while (true) {
      const index = cursor++
      if (index >= items.length) return
      results[index] = await mapper(items[index], index)
    }
  }
  await Promise.all(Array.from({ length: Math.min(limit, items.length) }, () => worker()))
  return results
}

function countBy(values) {
  const counts = new Map()
  for (const value of values) counts.set(String(value), (counts.get(String(value)) || 0) + 1)
  return Object.fromEntries([...counts.entries()].sort((a, b) => a[0].localeCompare(b[0])))
}

function markdown(report) {
  const lines = [
    '# EU-50 Resource Gap Probe',
    '',
    '> Evidence-only probe. This report does not mutate candidate data or change triage classifications.',
    '',
    `- Generated: ${report.generatedAt}`,
    `- Source root: ${report.sourceRoot}`,
    `- Unique targets: ${report.summary.uniqueTargets}`,
    `- Probed targets: ${report.summary.probedTargets}`,
    `- Confirmed HTTP 404/410 by GET: ${report.summary.missingConfirmed}`,
    `- Reachable HTTP targets: ${report.summary.reachable}`,
    `- Final transport failures: ${report.summary.transportFailures}`,
    `- Skipped non-HTTP/unresolved references: ${report.summary.skippedReferences}`,
    '',
    '## Final HTTP status',
    '',
  ]
  for (const [status, count] of Object.entries(report.summary.byFinalStatus)) lines.push(`- ${status}: ${count}`)
  lines.push('', '## Probe result kind', '')
  for (const [kind, count] of Object.entries(report.summary.byFinalKind)) lines.push(`- ${kind}: ${count}`)
  lines.push('', '## Policy', '', '- Only GET-confirmed HTTP 404/410 is marked `missingConfirmed=true`.', '- Transport errors, redirects outside the approved resource host boundary, malformed URLs, and all other statuses remain evidence only and require their existing classification/decision path.', '')
  return `${lines.join('\n')}\n`
}

if (!fs.existsSync(manualReviewPath)) throw new Error(`manual-review report missing: ${manualReviewPath}`)
if (!fs.existsSync(sourceConfigPath)) throw new Error(`source config missing: ${sourceConfigPath}`)

const manualReview = readJson(manualReviewPath)
const sourceConfig = readJson(sourceConfigPath)
const sourceRoot = new URL(sourceConfig.sourceRoot).href
const allowedHosts = new Set(sourceConfig.allowedResourceHosts || [])
if (!allowedHosts.has(new URL(sourceRoot).host)) throw new Error('sourceRoot host must be explicitly present in allowedResourceHosts')

const { targets, skipped } = collectTargets(manualReview, sourceRoot)
if (targets.length > maxTargets) throw new Error(`resource gap target count ${targets.length} exceeds MAIN_GAP_PROBE_MAX=${maxTargets}`)

const probed = await mapConcurrent(targets, concurrency, target => probeTarget(target, allowedHosts))
const byClassification = new Map()
for (const target of targets) {
  for (const classification of target.classifications) byClassification.set(classification, (byClassification.get(classification) || 0) + 1)
}

const report = {
  generatedAt: new Date().toISOString(),
  purpose: 'EU-50 evidence-only HTTP probe for unresolved legacy resource collection gaps; does not mutate candidate data or triage decisions',
  sourceRoot,
  allowedResourceHosts: [...allowedHosts].sort(),
  policy: {
    missingConfirmation: 'Only an HTTP GET probe returning 404 or 410 sets missingConfirmed=true',
    otherFailures: 'All transport errors, other HTTP statuses, invalid/non-HTTP references, and outside-boundary redirects remain evidence only',
    candidateMutation: 'NONE',
    classificationMutation: 'NONE',
  },
  limits: { maxTargets, concurrency, timeoutMs, interRequestDelayMs },
  summary: {
    uniqueTargets: targets.length,
    probedTargets: probed.length,
    reachable: probed.filter(item => item.reachable).length,
    missingConfirmed: probed.filter(item => item.missingConfirmed).length,
    transportFailures: probed.filter(item => item.transportFailure).length,
    skippedReferences: skipped.length,
    byFinalStatus: countBy(probed.map(item => item.finalStatus === null ? 'NO_HTTP_STATUS' : item.finalStatus)),
    byFinalKind: countBy(probed.map(item => (item.get || item.head).kind)),
    byClassification: Object.fromEntries([...byClassification.entries()].sort((a, b) => a[0].localeCompare(b[0]))),
  },
  skipped,
  targets: probed,
}

fs.mkdirSync(reportRoot, { recursive: true })
fs.writeFileSync(outputPath, `${JSON.stringify(report, null, 2)}\n`)
fs.writeFileSync(markdownPath, markdown(report))
console.log(JSON.stringify({ outputPath, uniqueTargets: report.summary.uniqueTargets, reachable: report.summary.reachable, missingConfirmed: report.summary.missingConfirmed, transportFailures: report.summary.transportFailures, skippedReferences: report.summary.skippedReferences }))
