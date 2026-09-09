import { mkdir, readFile, readdir, writeFile } from 'node:fs/promises'
import path from 'node:path'

const candidateRoot = path.resolve(process.env.MAIN_CANDIDATE_ROOT || 'main/v1/generated-candidate')
const outputRoot = path.resolve(process.env.MAIN_ELIGIBILITY_OUTPUT || 'main/v1/generated-import-eligibility')
const controlPath = process.env.MAIN_ELIGIBILITY_CONTROL
  ? path.resolve(process.env.MAIN_ELIGIBILITY_CONTROL)
  : null
const reportsRoot = path.join(candidateRoot, 'reports')
const problemIndex = JSON.parse(await readFile(path.join(reportsRoot, 'collection-problem-index.json'), 'utf8'))
const finalRetry = JSON.parse(await readFile(path.join(reportsRoot, 'targeted-retry-attempt-3.json'), 'utf8'))
const control = controlPath ? JSON.parse(await readFile(controlPath, 'utf8')) : null

const explicitlyApprovedNonBlocking = new Set([
  'EXTERNAL_ABSOLUTE_RESOURCE_REFERENCE',
  'LEGACY_ATTACHMENT_MIGRATION_RESIDUE',
])
const resolvedNonBlocking = new Set([
  ...explicitlyApprovedNonBlocking,
  'RETRY_RESOLVED_COLLECTED',
])

const articleIndex = (await readFile(path.join(candidateRoot, 'index.ndjson'), 'utf8'))
  .split(/\r?\n/)
  .filter(Boolean)
  .map(line => JSON.parse(line))
const pageIndex = JSON.parse(await readFile(path.join(candidateRoot, 'pages', 'index.json'), 'utf8'))
const pageKeys = new Set((pageIndex.items || []).map(item => item.legacyKey))

const listIndexes = new Map()
for (const entry of await readdir(path.join(candidateRoot, 'lists'), { withFileTypes: true })) {
  if (!entry.isDirectory()) continue
  const file = path.join(candidateRoot, 'lists', entry.name, 'index.json')
  const value = JSON.parse(await readFile(file, 'utf8'))
  listIndexes.set(entry.name, value)
}

if (control?.finalRetryAttempt != null && Number(control.finalRetryAttempt) !== Number(finalRetry.attempt)) {
  throw new Error(`final retry attempt mismatch control=${control.finalRetryAttempt} report=${finalRetry.attempt}`)
}
if ((finalRetry.nextRetryQueue || []).length > 0 || Number(finalRetry.summary?.retryableTransient || 0) > 0) {
  throw new Error('final retry evidence is not closed; retryable targets remain')
}

const finalByIdentity = new Map((finalRetry.results || []).map(result => [result.identity, result]))
const blockingByScope = new Map()
const nonBlockingEvidence = []
const unresolvedScopes = []

function addBlocking(scope, detail) {
  if (!blockingByScope.has(scope)) blockingByScope.set(scope, [])
  blockingByScope.get(scope).push(detail)
}

function listCodeFromIdentity(identity) {
  const match = /^LIST:([^:]+):/.exec(String(identity || ''))
  return match?.[1] || null
}

function candidateScope(record) {
  if (record.candidateType === 'ARTICLE' && record.legacyKey) {
    return { scope: `ARTICLE:${record.legacyKey}`, candidateType: 'ARTICLE', legacyKey: record.legacyKey }
  }
  if (record.candidateType === 'PAGE' && record.legacyKey) {
    const key = record.legacyKey.startsWith('main-page:') ? record.legacyKey : `main-page:${record.legacyKey}`
    if (pageKeys.has(key)) return { scope: `PAGE:${key}`, candidateType: 'PAGE', legacyKey: key }
  }
  if (record.candidateType === 'OBSERVATION' && record.legacyKey) {
    const key = record.legacyKey.startsWith('main-page:') ? record.legacyKey : `main-page:${record.legacyKey}`
    if (pageKeys.has(key)) return { scope: `PAGE:${key}`, candidateType: 'PAGE', legacyKey: key }
  }
  if (record.candidateType === 'LIST_ITEM') {
    const listCode = listCodeFromIdentity(record.identity)
    if (!listCode || !listIndexes.has(listCode)) return null
    if (record.legacyKey) {
      const items = listIndexes.get(listCode).items || []
      if (items.some(item => item.legacyKey === record.legacyKey)) {
        return {
          scope: `LIST_ITEM:${listCode}:${record.legacyKey}`,
          candidateType: 'LIST_ITEM',
          listCode,
          legacyKey: record.legacyKey,
        }
      }
    }
    return { scope: `LIST:${listCode}`, candidateType: 'LIST', listCode, legacyKey: null }
  }
  return null
}

function effectiveProblem(record) {
  if (resolvedNonBlocking.has(record.classification) || record.blocking === false) {
    return { blocking: false, classification: record.classification, disposition: record.disposition }
  }
  if (record.disposition === 'RETRYABLE_TRANSIENT' || record.retry === true) {
    const final = finalByIdentity.get(record.identity)
    if (!final) {
      return {
        blocking: true,
        classification: 'RETRY_FINAL_EVIDENCE_MISSING_REQUIRES_REVIEW',
        disposition: 'WITHHOLD_RETRY_EVIDENCE_MISSING',
      }
    }
    if (resolvedNonBlocking.has(final.classification) || final.nonBlocking === true) {
      return { blocking: false, classification: final.classification, disposition: final.outcome || 'RESOLVED_NON_BLOCKING' }
    }
    return { blocking: true, classification: final.classification, disposition: final.outcome || 'WITHHOLD_FINAL_RETRY_RESULT' }
  }
  return { blocking: true, classification: record.classification, disposition: record.disposition }
}

for (const record of problemIndex.records || []) {
  const effective = effectiveProblem(record)
  const detail = {
    identity: record.identity,
    classification: effective.classification,
    originalClassification: record.classification,
    disposition: effective.disposition,
    code: record.code || null,
    rawReference: record.rawReference || null,
    targetUrl: record.targetUrl || null,
    message: record.message || null,
  }
  if (!effective.blocking) {
    nonBlockingEvidence.push(detail)
    continue
  }
  const scope = candidateScope(record)
  if (!scope) {
    unresolvedScopes.push({ ...detail, candidateType: record.candidateType || null, legacyKey: record.legacyKey || null })
    continue
  }
  addBlocking(scope.scope, detail)
}

if (unresolvedScopes.length > 0) {
  throw new Error(`blocking problems could not be mapped to candidate scope: ${JSON.stringify(unresolvedScopes.slice(0, 10))}`)
}

const withheld = []
const eligibleArticles = []
for (const item of articleIndex) {
  const problems = blockingByScope.get(`ARTICLE:${item.legacyKey}`) || []
  if (problems.length > 0) {
    withheld.push({ candidateType: 'ARTICLE', legacyKey: item.legacyKey, path: item.path, problems })
  } else {
    eligibleArticles.push(item)
  }
}

const eligiblePages = []
for (const item of pageIndex.items || []) {
  const problems = blockingByScope.get(`PAGE:${item.legacyKey}`) || []
  if (problems.length > 0) {
    withheld.push({ candidateType: 'PAGE', legacyKey: item.legacyKey, path: item.path, problems })
  } else {
    eligiblePages.push(item)
  }
}

const eligibleLists = {}
let totalListItems = 0
let eligibleListItems = 0
for (const [listCode, index] of [...listIndexes.entries()].sort((a, b) => a[0].localeCompare(b[0]))) {
  const listProblems = blockingByScope.get(`LIST:${listCode}`) || []
  const values = []
  totalListItems += (index.items || []).length
  for (const item of index.items || []) {
    const itemProblems = blockingByScope.get(`LIST_ITEM:${listCode}:${item.legacyKey}`) || []
    const problems = [...listProblems, ...itemProblems]
    if (problems.length > 0) {
      withheld.push({ candidateType: 'LIST_ITEM', listCode, legacyKey: item.legacyKey, path: item.path, problems })
    } else {
      values.push(item)
      eligibleListItems += 1
    }
  }
  eligibleLists[listCode] = values
}

const totalCandidates = articleIndex.length + (pageIndex.items || []).length + totalListItems
const eligibleCandidates = eligibleArticles.length + eligiblePages.length + eligibleListItems
const withheldCandidates = withheld.length
if (totalCandidates !== eligibleCandidates + withheldCandidates) {
  throw new Error(`eligibility arithmetic mismatch ${totalCandidates} != ${eligibleCandidates} + ${withheldCandidates}`)
}

const classificationCounts = new Map()
for (const item of withheld) {
  for (const problem of item.problems || []) {
    classificationCounts.set(problem.classification, (classificationCounts.get(problem.classification) || 0) + 1)
  }
}

const report = {
  generatedAt: new Date().toISOString(),
  purpose: 'EU-50 import eligibility boundary for current testing/function evolution; this does not perform Runtime import',
  provenance: control ? {
    sourceRunId: control.sourceRunId,
    sourceArtifactId: control.sourceArtifactId,
    sourceArtifactName: control.sourceArtifactName,
    sourceArtifactDigest: control.sourceArtifactDigest,
    sourceHead: control.sourceHead,
    finalRetryAttempt: control.finalRetryAttempt,
  } : null,
  policy: {
    default: 'WITHHOLD_ANY_CANDIDATE_WITH_UNRESOLVED_OR_UNAPPROVED_BLOCKING_ERROR',
    explicitlyApprovedNonBlockingClassifications: [...explicitlyApprovedNonBlocking].sort(),
    resolvedRetryResult: 'RETRY_RESOLVED_COLLECTED_IS_CLEAN_NOT_AN_EXCEPTION',
    fullSnapshotPreserved: true,
    currentImport: 'ELIGIBLE_INDEX_ONLY',
    runtimeImportPerformedHere: false,
  },
  summary: {
    totalCandidates,
    eligibleCandidates,
    withheldCandidates,
    articles: { total: articleIndex.length, eligible: eligibleArticles.length, withheld: articleIndex.length - eligibleArticles.length },
    pages: { total: (pageIndex.items || []).length, eligible: eligiblePages.length, withheld: (pageIndex.items || []).length - eligiblePages.length },
    listItems: { total: totalListItems, eligible: eligibleListItems, withheld: totalListItems - eligibleListItems },
    nonBlockingEvidenceObservations: nonBlockingEvidence.length,
    explicitlyApprovedNonBlockingEvidenceObservations: nonBlockingEvidence.filter(item => explicitlyApprovedNonBlocking.has(item.classification)).length,
    finalRetry: finalRetry.summary || null,
    withheldProblemClassifications: Object.fromEntries([...classificationCounts.entries()].sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))),
  },
}

const eligibleIndex = {
  generatedAt: report.generatedAt,
  sourceSystem: 'legacy-jilinjobs',
  policy: 'IMPORT_ONLY_THIS_ELIGIBLE_INDEX_AT_CURRENT_STAGE',
  pathResolution: 'Candidate paths resolve against the provenance source artifact generated-candidate root.',
  articles: eligibleArticles,
  pages: eligiblePages,
  lists: eligibleLists,
}

await mkdir(outputRoot, { recursive: true })
await writeFile(path.join(outputRoot, 'import-eligibility.json'), JSON.stringify(report, null, 2) + '\n', 'utf8')
await writeFile(path.join(outputRoot, 'import-eligible-index.json'), JSON.stringify(eligibleIndex, null, 2) + '\n', 'utf8')
await writeFile(path.join(outputRoot, 'import-withheld.json'), JSON.stringify({
  generatedAt: report.generatedAt,
  count: withheld.length,
  policy: 'RETAIN_IN_FULL_SNAPSHOT_BUT_DO_NOT_IMPORT_UNTIL_RESOLVED_OR_EXPLICITLY_APPROVED',
  records: withheld,
}, null, 2) + '\n', 'utf8')

const lines = [
  '# EU-50 Import Eligibility',
  '',
  '> Current-stage whitelist only. Full collection data remains preserved; this report does not perform Runtime import.',
  '',
  `- Total candidates: ${totalCandidates}`,
  `- Import eligible: ${eligibleCandidates}`,
  `- Withheld: ${withheldCandidates}`,
  `- Articles: ${eligibleArticles.length}/${articleIndex.length} eligible`,
  `- Pages: ${eligiblePages.length}/${(pageIndex.items || []).length} eligible`,
  `- List items: ${eligibleListItems}/${totalListItems} eligible`,
  `- Non-blocking evidence observations: ${nonBlockingEvidence.length}`,
  `- Explicitly approved non-blocking observations: ${nonBlockingEvidence.filter(item => explicitlyApprovedNonBlocking.has(item.classification)).length}`,
  '',
  '## Policy',
  '',
  '- Import only candidates in `import-eligible-index.json` at the current stage.',
  '- Any unresolved/unapproved blocking error withholds that candidate from import.',
  '- Explicitly approved external absolute Article resources and legacy `Common/DownLoadFile.aspx` residue remain non-blocking.',
  '- Withheld records remain in the immutable/full snapshot and `import-withheld.json` for later remediation or human decision.',
  '- This EU-50 output does not execute Runtime import or enter EU-51.',
  '',
]
await writeFile(path.join(outputRoot, 'import-eligibility.md'), lines.join('\n') + '\n', 'utf8')
console.log(`EU50_MAIN_IMPORT_ELIGIBILITY ${JSON.stringify(report.summary)}`)
