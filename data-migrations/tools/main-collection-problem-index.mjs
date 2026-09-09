import { mkdir, readFile, writeFile } from 'node:fs/promises'
import path from 'node:path'

const configPath = path.resolve(process.env.MAIN_SOURCE_CONFIG || 'main/source-surfaces.json')
const inventoryRoot = path.resolve(process.env.MAIN_INVENTORY_ROOT || 'main/v1/generated-inventory')
const candidateRoot = path.resolve(process.env.MIGRATION_OUTPUT || 'main/v1/generated-candidate')
const reportsRoot = path.join(candidateRoot, 'reports')
const attempt = Math.max(1, Number(process.env.MAIN_COLLECTION_ATTEMPT || 1))
const maxAttempts = Math.max(1, Number(process.env.MAIN_MAX_TARGET_ATTEMPTS || 3))

const config = JSON.parse(await readFile(configPath, 'utf8'))
const inventory = JSON.parse(await readFile(path.join(inventoryRoot, 'inventory.json'), 'utf8'))
const rawIssues = JSON.parse(await readFile(path.join(reportsRoot, 'issues.json'), 'utf8'))
const internalHosts = new Set(config.allowedHosts || [new URL(config.sourceRoot).host])
const inventoryByLegacyKey = new Map(inventory.map(item => [item.legacyKey, item]))

async function readOptionalJson(file, fallback) {
  try { return JSON.parse(await readFile(file, 'utf8')) } catch { return fallback }
}

function issueClassification(issue) {
  const message = String(issue.message || '')
  if (
    ['BODY_IMAGE_FETCH_FAILED', 'ATTACHMENT_FETCH_FAILED', 'LIST_IMAGE_UNRESOLVED'].includes(issue.code)
    && /HTTP (?:404|410)\b/.test(message)
  ) return 'SOURCE_RESOURCE_MISSING'
  if (/host 未在 allowlist/.test(message)) return 'RESOURCE_HOST_OUTSIDE_APPROVED_BOUNDARY'
  if (/is not image/.test(message)) return 'RESOURCE_TYPE_MISMATCH'
  if (issue.code === 'DATA_URI_RESOURCE_UNSUPPORTED') return 'EMBEDDED_DATA_RESOURCE_REQUIRES_REVIEW'
  if (issue.code === 'UNSUPPORTED_BODY_ELEMENT') return 'UNSUPPORTED_HTML_REQUIRES_REVIEW'
  if (issue.code === 'PAGE_COLLECTION_UNRESOLVED') return 'PAGE_COLLECTION_REQUIRES_REVIEW'
  if (issue.code === 'HOME_SITE_LINK_DUPLICATE_URL') return 'DUPLICATE_LIST_LINK_REQUIRES_REVIEW'
  if (/fetch failed/i.test(message)) return 'RESOURCE_FETCH_FAILED'
  if (['BODY_IMAGE_FETCH_FAILED', 'ATTACHMENT_FETCH_FAILED', 'LIST_IMAGE_UNRESOLVED'].includes(issue.code)) return 'RESOURCE_COLLECTION_FAILED'
  return issue.code
}

function articleItem(issue) {
  const direct = issue.legacyKey || issue.evidenceKey
  if (direct && inventoryByLegacyKey.has(direct)) return inventoryByLegacyKey.get(direct)
  const match = String(issue.evidenceKey || '').match(/(?:^|:)content:(\d+)/)
  if (!match) return null
  return inventory.find(item => String(item.contentId || '') === match[1]) || null
}

function extractResourceReferences(issue) {
  const raw = String(issue.rawReference || issue.imageReference || '').trim()
  if (!raw) return []
  if (issue.code === 'CSS_RESOURCE_REFERENCE_REQUIRES_REVIEW') {
    return [...raw.matchAll(/url\s*\(\s*(["']?)(.*?)\1\s*\)/gi)].map(match => match[2].trim()).filter(Boolean)
  }
  if (/^<[^>]+>$/.test(raw)) return []
  return [raw]
}

function resolveNetworkReference(raw, baseUrl) {
  if (!raw) return null
  const absolute = /^[a-zA-Z][a-zA-Z\d+.-]*:/.test(raw) || raw.startsWith('//')
  if (!absolute) return null
  try {
    const parsed = new URL(raw, baseUrl || config.sourceRoot)
    if (!['http:', 'https:'].includes(parsed.protocol)) return null
    return parsed
  } catch {
    return null
  }
}

function resolvedTargetUrl(issue, item) {
  const raw = issue.resolvedUrl || issue.rawReference || issue.imageReference
  if (!raw) return item?.url || null
  try { return new URL(raw, item?.url || config.sourceRoot).href } catch { return item?.url || null }
}

function transportLike(issue, classification) {
  if (classification === 'RESOURCE_FETCH_FAILED') return true
  const message = String(issue.message || '')
  return /fetch failed|UND_ERR|ECONNRESET|ECONNREFUSED|ETIMEDOUT|socket|other side closed|request timeout|network/i.test(message)
}

function problemIdentity(issue, item) {
  const url = resolvedTargetUrl(issue, item)
  if (url && (issue.rawReference || issue.imageReference || issue.resolvedUrl)) return `RESOURCE:${url}`
  if (item) return `ARTICLE:${item.legacyKey}`
  if (issue.surfaceKey) return `PAGE:${issue.surfaceKey}`
  if (issue.listCode) return `LIST:${issue.listCode}:${issue.legacyKey || issue.evidenceKey || issue.code}`
  return `OBSERVATION:${issue.evidenceKey || issue.legacyKey || issue.code}`
}

const legacyResidue = await readOptionalJson(path.join(reportsRoot, 'legacy-attachment-residue.json'), { records: [] })
const records = []
const retryByIdentity = new Map()

for (const issue of rawIssues) {
  const item = articleItem(issue)
  const baseClassification = issueClassification(issue)
  const references = extractResourceReferences(issue)
  const externalRefs = item
    ? references.map(raw => ({ raw, parsed: resolveNetworkReference(raw, item.url) })).filter(entry => entry.parsed && !internalHosts.has(entry.parsed.host))
    : []
  const allReferencesExternal = references.length > 0 && externalRefs.length === references.length
  const identity = problemIdentity(issue, item)
  let classification = baseClassification
  let disposition = 'TERMINAL_CLASSIFIED_REVIEW'
  let retry = false
  let blocking = true

  if (item && allReferencesExternal) {
    classification = 'EXTERNAL_ABSOLUTE_RESOURCE_REFERENCE'
    disposition = 'TERMINAL_NON_BLOCKING'
    blocking = false
  } else if (baseClassification === 'SOURCE_RESOURCE_MISSING') {
    disposition = 'TERMINAL_CLIENT_CONFIRMATION'
  } else if (transportLike(issue, baseClassification)) {
    if (attempt >= maxAttempts) {
      classification = 'UNCLASSIFIED_RETRY_EXHAUSTED_REQUIRES_REVIEW'
      disposition = 'TERMINAL_RETRY_EXHAUSTED_REVIEW'
    } else {
      disposition = 'RETRYABLE_TRANSIENT'
      retry = true
    }
  } else if (issue.code === 'RELATIVE_BODY_LINK_REQUIRES_REVIEW') {
    disposition = 'RELATED_TO_INTERNAL_RESOURCE_STATE'
  }

  const record = {
    identity,
    candidateType: item ? 'ARTICLE' : (issue.surfaceKey ? 'PAGE' : issue.listCode ? 'LIST_ITEM' : 'OBSERVATION'),
    legacyKey: item?.legacyKey || issue.legacyKey || issue.evidenceKey || null,
    title: item?.title || issue.title || null,
    sourceUrl: item?.url || issue.sourceUrl || null,
    classification,
    originalClassification: baseClassification,
    disposition,
    blocking,
    retry,
    attempt,
    maxAttempts,
    remainingAttempts: retry ? maxAttempts - attempt : 0,
    rawReference: issue.rawReference || issue.imageReference || null,
    targetUrl: resolvedTargetUrl(issue, item),
    code: issue.code || null,
    message: issue.message || null,
    externalHosts: [...new Set(externalRefs.map(entry => entry.parsed.host))].sort(),
  }
  records.push(record)

  if (retry) {
    const previous = retryByIdentity.get(identity)
    if (!previous) {
      retryByIdentity.set(identity, {
        identity,
        candidateType: record.candidateType,
        legacyKey: record.legacyKey,
        sourceUrl: record.sourceUrl,
        targetUrl: record.targetUrl,
        attempt,
        maxAttempts,
        remainingAttempts: maxAttempts - attempt,
        classifications: [baseClassification],
        issueCodes: [record.code].filter(Boolean),
        rawReferences: [record.rawReference].filter(Boolean),
        evidence: [{ code: record.code, message: record.message }],
      })
    } else {
      if (!previous.classifications.includes(baseClassification)) previous.classifications.push(baseClassification)
      if (record.code && !previous.issueCodes.includes(record.code)) previous.issueCodes.push(record.code)
      if (record.rawReference && !previous.rawReferences.includes(record.rawReference)) previous.rawReferences.push(record.rawReference)
      previous.evidence.push({ code: record.code, message: record.message })
    }
  }
}

for (const residue of legacyResidue.records || []) {
  records.push({
    identity: `LEGACY_ATTACHMENT:${residue.legacyKey || ''}:${residue.rawReference || ''}`,
    candidateType: 'ARTICLE',
    legacyKey: residue.legacyKey || null,
    title: residue.title || null,
    sourceUrl: residue.sourceUrl || null,
    classification: 'LEGACY_ATTACHMENT_MIGRATION_RESIDUE',
    originalClassification: 'LEGACY_ATTACHMENT_MIGRATION_RESIDUE',
    disposition: 'TERMINAL_NON_BLOCKING',
    blocking: false,
    retry: false,
    attempt,
    maxAttempts,
    remainingAttempts: 0,
    rawReference: residue.rawReference || null,
    targetUrl: residue.resolvedUrl || null,
    code: null,
    message: residue.note || null,
    externalHosts: [],
  })
}

const retryIdentities = new Set(retryByIdentity.keys())
for (const record of records) {
  if (record.classification !== 'RELATIVE_BODY_LINK_REQUIRES_REVIEW') continue
  if (!retryIdentities.has(record.identity)) continue
  record.disposition = 'RELATED_TO_RETRYABLE_TARGET'
  record.blocking = false
}

const retryQueue = [...retryByIdentity.values()].map(item => ({
  ...item,
  classifications: item.classifications.sort(),
  issueCodes: item.issueCodes.sort(),
  rawReferences: item.rawReferences.sort(),
})).sort((a, b) => a.identity.localeCompare(b.identity))
records.sort((a, b) => `${a.classification}:${a.identity}`.localeCompare(`${b.classification}:${b.identity}`))

function countBy(values, key) {
  const counts = new Map()
  for (const value of values) {
    const name = String(value[key] ?? '<none>')
    counts.set(name, (counts.get(name) || 0) + 1)
  }
  return Object.fromEntries([...counts.entries()].sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0])))
}

const report = {
  generatedAt: new Date().toISOString(),
  purpose: 'EU-50 full-pass problem index and bounded targeted-retry queue',
  sourceRoot: config.sourceRoot,
  internalHosts: [...internalHosts].sort(),
  policy: {
    fullPassFirst: true,
    recognizedProblemsAreTerminal: true,
    externalAbsoluteArticleResources: 'PRESERVE_ORIGINAL_REFERENCE_NON_BLOCKING_NO_PROBE',
    legacyDownloadResidue: 'PRESERVE_ORIGINAL_REFERENCE_NON_BLOCKING_NO_RETRY',
    sourceMissing: 'HTTP_404_OR_410_ONLY_NO_RETRY_CLIENT_CONFIRMATION',
    retryBudget: `${maxAttempts} collection iterations total per target; initial full pass is attempt 1`,
    retryExhausted: 'UNCLASSIFIED_RETRY_EXHAUSTED_REQUIRES_REVIEW',
  },
  attempt,
  maxAttempts,
  summary: {
    observations: records.length,
    retryTargets: retryQueue.length,
    terminalNonBlocking: records.filter(record => record.disposition === 'TERMINAL_NON_BLOCKING').length,
    terminalClientConfirmation: records.filter(record => record.disposition === 'TERMINAL_CLIENT_CONFIRMATION').length,
    terminalReview: records.filter(record => record.disposition.startsWith('TERMINAL_') && record.blocking).length,
    relatedToRetry: records.filter(record => record.disposition === 'RELATED_TO_RETRYABLE_TARGET').length,
    byClassification: countBy(records, 'classification'),
    byDisposition: countBy(records, 'disposition'),
  },
  retryQueue,
  records,
}

await mkdir(reportsRoot, { recursive: true })
await writeFile(path.join(reportsRoot, 'collection-problem-index.json'), `${JSON.stringify(report, null, 2)}\n`, 'utf8')
const markdown = [
  '# EU-50 Collection Problem Index',
  '',
  `- Collection attempt: ${attempt}/${maxAttempts}`,
  `- Problem observations: ${report.summary.observations}`,
  `- Automatic retry targets: ${report.summary.retryTargets}`,
  `- Terminal non-blocking observations: ${report.summary.terminalNonBlocking}`,
  `- Terminal client-confirmation observations: ${report.summary.terminalClientConfirmation}`,
  `- Terminal review observations: ${report.summary.terminalReview}`,
  '',
  '## Retry policy',
  '',
  '- Only unresolved transient/transport targets enter automatic retry.',
  `- Maximum collection iterations per target: ${maxAttempts}; the full pass is attempt 1.`,
  '- Recognized error types are classified once and skipped by automatic retry.',
  '- Exhausted unknown failures become `UNCLASSIFIED_RETRY_EXHAUSTED_REQUIRES_REVIEW`.',
  '- HTTP 404/410 remains the only automatic evidence for `SOURCE_RESOURCE_MISSING`.',
  '',
  '## Retry queue',
  '',
  '| Identity | Candidate | Target | Attempt | Remaining | Classifications |',
  '|---|---|---|---:|---:|---|',
  ...retryQueue.map(row => `| ${row.identity.replaceAll('|', '\\|')} | ${row.legacyKey || row.candidateType} | ${(row.targetUrl || '').replaceAll('|', '\\|')} | ${row.attempt} | ${row.remainingAttempts} | ${row.classifications.join(', ')} |`),
  '',
].join('\n')
await writeFile(path.join(reportsRoot, 'collection-problem-index.md'), `${markdown}\n`, 'utf8')
console.log(`EU50_MAIN_PROBLEM_INDEX ${JSON.stringify(report.summary)}`)
