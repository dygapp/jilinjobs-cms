import { mkdir, readFile, readdir, writeFile } from 'node:fs/promises'
import path from 'node:path'

const candidateRoot = path.resolve(process.env.MAIN_CANDIDATE_ROOT || 'main/v1/generated-candidate')
const outputRoot = path.resolve(process.env.MAIN_ELIGIBILITY_OUTPUT || 'main/v1/generated-import-eligibility')
const controlPath = process.env.MAIN_ELIGIBILITY_CONTROL ? path.resolve(process.env.MAIN_ELIGIBILITY_CONTROL) : null
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
const articleByLegacyKey = new Map(articleIndex.map(item => [item.legacyKey, item]))

const pageIndex = JSON.parse(await readFile(path.join(candidateRoot, 'pages', 'index.json'), 'utf8'))
const pageByLegacyKey = new Map((pageIndex.items || []).map(item => [item.legacyKey, item]))

const listIndexes = new Map()
for (const entry of await readdir(path.join(candidateRoot, 'lists'), { withFileTypes: true })) {
  if (!entry.isDirectory()) continue
  const value = JSON.parse(await readFile(path.join(candidateRoot, 'lists', entry.name, 'index.json'), 'utf8'))
  listIndexes.set(entry.name, value)
}

if (control?.finalRetryAttempt != null && Number(control.finalRetryAttempt) !== Number(finalRetry.attempt)) {
  throw new Error(`final retry attempt mismatch control=${control.finalRetryAttempt} report=${finalRetry.attempt}`)
}
if ((finalRetry.nextRetryQueue || []).length > 0 || Number(finalRetry.summary?.retryableTransient || 0) > 0) {
  throw new Error('final retry evidence is not closed; retryable targets remain')
}

const finalByIdentity = new Map((finalRetry.results || []).map(result => [result.identity, result]))
const blockingByArticle = new Map()
const articleNonBlockingEvidence = []
const unscopedMigrationProblems = []
const pageProblems = new Map()
const listItemProblems = new Map()
const sitePackageObservations = []

function add(map, key, value) {
  if (!map.has(key)) map.set(key, [])
  map.get(key).push(value)
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
    if (
      resolvedNonBlocking.has(final.classification)
      || final.outcome === 'RESOLVED_COLLECTED'
      || final.outcome === 'TERMINAL_NON_BLOCKING'
      || final.nonBlocking === true
    ) {
      return {
        blocking: false,
        classification: final.classification,
        disposition: final.outcome || 'RESOLVED_NON_BLOCKING',
      }
    }
    return {
      blocking: true,
      classification: final.classification || 'RETRY_FINAL_CLASSIFICATION_MISSING_REQUIRES_REVIEW',
      disposition: final.outcome || 'WITHHOLD_FINAL_RETRY_RESULT',
    }
  }
  return { blocking: true, classification: record.classification, disposition: record.disposition }
}

function detail(record, effective) {
  return {
    identity: record.identity || null,
    candidateType: record.candidateType || null,
    legacyKey: record.legacyKey || null,
    title: record.title || null,
    sourceUrl: record.sourceUrl || null,
    classification: effective.classification,
    originalClassification: record.classification || null,
    disposition: effective.disposition || null,
    blocking: effective.blocking,
    code: record.code || null,
    rawReference: record.rawReference || null,
    targetUrl: record.targetUrl || null,
    message: record.message || null,
  }
}

function normalizePageKey(raw) {
  const value = String(raw || '')
  if (!value) return null
  return value.startsWith('main-page:') ? value : `main-page:${value}`
}

function listCodeFromIdentity(identity) {
  const match = /^LIST:([^:]+):/.exec(String(identity || ''))
  return match?.[1] || null
}

function listItemKey(listCode, legacyKey) {
  return `${listCode}\u0000${legacyKey}`
}

for (const record of problemIndex.records || []) {
  const effective = effectiveProblem(record)
  const value = detail(record, effective)

  if (record.candidateType === 'ARTICLE') {
    if (!record.legacyKey || !articleByLegacyKey.has(record.legacyKey)) {
      if (effective.blocking) unscopedMigrationProblems.push(value)
      else articleNonBlockingEvidence.push(value)
      continue
    }
    if (effective.blocking) add(blockingByArticle, record.legacyKey, value)
    else articleNonBlockingEvidence.push(value)
    continue
  }

  if (record.candidateType === 'PAGE') {
    const key = normalizePageKey(record.legacyKey)
    if (key && pageByLegacyKey.has(key)) add(pageProblems, key, value)
    else sitePackageObservations.push({ ...value, handoffScope: 'PAGE_UNMAPPED' })
    continue
  }

  if (record.candidateType === 'LIST_ITEM') {
    const listCode = listCodeFromIdentity(record.identity)
    if (listCode && record.legacyKey && listIndexes.has(listCode)) {
      const exists = (listIndexes.get(listCode).items || []).some(item => item.legacyKey === record.legacyKey)
      if (exists) {
        add(listItemProblems, listItemKey(listCode, record.legacyKey), value)
        continue
      }
    }
    sitePackageObservations.push({ ...value, handoffScope: 'LIST_ITEM_UNMAPPED', listCode })
    continue
  }

  if (record.candidateType === 'OBSERVATION') {
    const pageKey = normalizePageKey(record.legacyKey)
    if (pageKey && pageByLegacyKey.has(pageKey)) {
      add(pageProblems, pageKey, value)
      continue
    }
    const listCode = listCodeFromIdentity(record.identity)
    if (listCode && listIndexes.has(listCode)) {
      sitePackageObservations.push({ ...value, handoffScope: 'LIST_OBSERVATION', listCode })
      continue
    }
    if (effective.blocking) unscopedMigrationProblems.push({ ...value, handoffScope: 'UNSCOPED_BLOCKING_OBSERVATION' })
    else articleNonBlockingEvidence.push(value)
    continue
  }

  if (effective.blocking) unscopedMigrationProblems.push(value)
  else articleNonBlockingEvidence.push(value)
}

const eligibleArticles = []
const excludedSourceDefectArticles = []
const withheldReviewArticles = []
for (const item of articleIndex) {
  const problems = blockingByArticle.get(item.legacyKey) || []
  if (problems.length === 0) {
    eligibleArticles.push(item)
    continue
  }
  const sourceMissingOnly = problems.every(problem => problem.classification === 'SOURCE_RESOURCE_MISSING')
  const internalIdentity = /:content:\d+$/.test(String(item.legacyKey || ''))
  if (sourceMissingOnly && internalIdentity) {
    excludedSourceDefectArticles.push({
      candidateType: 'ARTICLE',
      legacyKey: item.legacyKey,
      path: item.path,
      title: problems.find(problem => problem.title)?.title || null,
      sourceUrl: problems.find(problem => problem.sourceUrl)?.sourceUrl || null,
      decision: 'EXCLUDE_PENDING_CLIENT_CONFIRMATION',
      reason: 'SOURCE_RESOURCE_MISSING',
      problems,
    })
  } else {
    withheldReviewArticles.push({
      candidateType: 'ARTICLE',
      legacyKey: item.legacyKey,
      path: item.path,
      title: problems.find(problem => problem.title)?.title || null,
      sourceUrl: problems.find(problem => problem.sourceUrl)?.sourceUrl || null,
      decision: 'HUMAN_REVIEW_REQUIRED',
      problems,
    })
  }
}

const totalArticles = articleIndex.length
if (totalArticles !== eligibleArticles.length + excludedSourceDefectArticles.length + withheldReviewArticles.length) {
  throw new Error('Article-only eligibility arithmetic mismatch')
}

const handoffPages = (pageIndex.items || []).map(item => ({
  candidateType: 'PAGE',
  legacyKey: item.legacyKey,
  path: item.path,
  disposition: 'SITE_PACKAGE_SOURCE_HANDOFF',
  problems: pageProblems.get(item.legacyKey) || [],
}))

const handoffListItems = []
for (const [listCode, index] of [...listIndexes.entries()].sort((a, b) => a[0].localeCompare(b[0]))) {
  for (const item of index.items || []) {
    handoffListItems.push({
      candidateType: 'LIST_ITEM',
      listCode,
      legacyKey: item.legacyKey,
      path: item.path,
      disposition: 'SITE_PACKAGE_SOURCE_HANDOFF',
      problems: listItemProblems.get(listItemKey(listCode, item.legacyKey)) || [],
    })
  }
}

const classificationCounts = new Map()
for (const item of withheldReviewArticles) {
  for (const problem of item.problems) {
    classificationCounts.set(problem.classification, (classificationCounts.get(problem.classification) || 0) + 1)
  }
}
for (const problem of unscopedMigrationProblems) {
  classificationCounts.set(problem.classification, (classificationCounts.get(problem.classification) || 0) + 1)
}

const pageProblemCount = handoffPages.reduce((sum, item) => sum + item.problems.length, 0)
const listItemProblemCount = handoffListItems.reduce((sum, item) => sum + item.problems.length, 0)
const sitePackageBlockingProblems = [
  ...handoffPages.flatMap(item => item.problems),
  ...handoffListItems.flatMap(item => item.problems),
  ...sitePackageObservations,
].filter(problem => problem.blocking !== false).length

const report = {
  generatedAt: new Date().toISOString(),
  purpose: 'EU-50 Article-only Main migration eligibility; Page/ListItem source findings are handed off to Site Package authority and are never silently discarded',
  provenance: control ? {
    sourceRunId: control.sourceRunId,
    sourceArtifactId: control.sourceArtifactId,
    sourceArtifactName: control.sourceArtifactName,
    sourceArtifactDigest: control.sourceArtifactDigest,
    sourceHead: control.sourceHead,
    finalRetryAttempt: control.finalRetryAttempt,
  } : null,
  policy: {
    migrationScope: 'ARTICLE_ONLY',
    pageOwnership: 'SITE_PACKAGE_STABLE_CONTENT',
    mainListItemOwnership: 'SITE_PACKAGE_STABLE_CONTENT',
    pageAndListDiscovery: 'SOURCE_EVIDENCE_HANDOFF_ONLY_NOT_MIGRATION_INPUT',
    defaultArticleDecision: 'WITHHOLD_ANY_ARTICLE_WITH_UNRESOLVED_OR_UNAPPROVED_BLOCKING_ERROR',
    sourceMissingArticleDecision: 'INTERNAL Article with only HTTP 404/410 SOURCE_RESOURCE_MISSING is excluded pending client confirmation',
    explicitlyApprovedNonBlockingClassifications: [...explicitlyApprovedNonBlocking].sort(),
    resolvedRetryResult: 'RETRY_RESOLVED_COLLECTED_IS_CLEAN_NOT_AN_EXCEPTION',
    fullSourceSnapshotPreserved: true,
    runtimeImportPerformedHere: false,
  },
  summary: {
    migrationScope: 'ARTICLE_ONLY',
    totalCandidates: totalArticles,
    eligibleCandidates: eligibleArticles.length,
    excludedSourceDefectCandidates: excludedSourceDefectArticles.length,
    withheldCandidates: withheldReviewArticles.length,
    unscopedBlockingObservations: unscopedMigrationProblems.length,
    articleNonBlockingEvidenceObservations: articleNonBlockingEvidence.length,
    finalRetry: finalRetry.summary || null,
    withheldProblemClassifications: Object.fromEntries([...classificationCounts.entries()].sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))),
    articlePromotionReady: withheldReviewArticles.length === 0 && unscopedMigrationProblems.length === 0 && eligibleArticles.length > 0,
    sitePackageHandoff: {
      pages: handoffPages.length,
      listItems: handoffListItems.length,
      pageProblemObservations: pageProblemCount,
      listItemProblemObservations: listItemProblemCount,
      otherObservations: sitePackageObservations.length,
      blockingProblemObservations: sitePackageBlockingProblems,
    },
  },
}

const eligibleIndex = {
  generatedAt: report.generatedAt,
  sourceSystem: 'legacy-jilinjobs',
  policy: 'ARTICLE_ONLY_IMPORT_ELIGIBILITY',
  pathResolution: 'Article paths resolve against the provenance source artifact generated-candidate root.',
  articles: eligibleArticles,
}

const sitePackageHandoff = {
  generatedAt: report.generatedAt,
  purpose: 'Legacy Main Page/ListItem discovery evidence for Site Package content reconciliation/planning; not a Historical Migration Dataset',
  policy: {
    ownership: 'SITE_PACKAGE',
    migrationImportEligible: false,
    preserveSourceEvidence: true,
    unresolvedProblemsRequireExplicitClassification: true,
    stableListItemProvisioningCapability: 'GAP_REQUIRES_SEPARATE_SITE_PACKAGE_AUTHORITY',
  },
  summary: report.summary.sitePackageHandoff,
  pages: handoffPages,
  listItems: handoffListItems,
  observations: sitePackageObservations,
}

await mkdir(outputRoot, { recursive: true })
await writeFile(path.join(outputRoot, 'import-eligibility.json'), JSON.stringify(report, null, 2) + '\n', 'utf8')
await writeFile(path.join(outputRoot, 'import-eligible-index.json'), JSON.stringify(eligibleIndex, null, 2) + '\n', 'utf8')
await writeFile(path.join(outputRoot, 'import-withheld.json'), JSON.stringify({
  generatedAt: report.generatedAt,
  migrationScope: 'ARTICLE_ONLY',
  excludedSourceDefectArticles,
  withheldReviewArticles,
  unscopedBlockingObservations: unscopedMigrationProblems,
}, null, 2) + '\n', 'utf8')
await writeFile(path.join(outputRoot, 'source-defect-articles.json'), JSON.stringify(excludedSourceDefectArticles, null, 2) + '\n', 'utf8')
await writeFile(path.join(outputRoot, 'site-package-handoff.json'), JSON.stringify(sitePackageHandoff, null, 2) + '\n', 'utf8')

const eligibilityLines = [
  '# EU-50 Article-only Import Eligibility',
  '',
  '> Main Historical Migration now promotes Articles only. Page and Main stable ListItem discoveries are preserved in `site-package-handoff.json` and do not enter migration eligibility.',
  '',
  `- Article candidates: ${totalArticles}`,
  `- Article import eligible: ${eligibleArticles.length}`,
  `- Source-defect Articles excluded pending client confirmation: ${excludedSourceDefectArticles.length}`,
  `- Articles withheld for human review: ${withheldReviewArticles.length}`,
  `- Unscoped blocking observations: ${unscopedMigrationProblems.length}`,
  `- Article promotion ready: ${report.summary.articlePromotionReady}`,
  '',
  '## Policy',
  '',
  '- Only `articles` appears in `import-eligible-index.json`.',
  '- Confirmed HTTP 404/410 source-resource-missing INTERNAL Articles are separately excluded and retained for client confirmation.',
  '- Other Article error classes remain explicit human-review blockers; no silent repair/discard is allowed.',
  '- Page/ListItem source findings are Site Package handoff evidence, not migration input.',
  '- This output does not perform Runtime import and does not enter EU-51.',
  '',
]
await writeFile(path.join(outputRoot, 'import-eligibility.md'), eligibilityLines.join('\n') + '\n', 'utf8')

const handoffLines = [
  '# Main Page / ListItem Site Package Source Handoff',
  '',
  '> These records were discovered while probing the Legacy Main source. They belong to JilinJobs Site Package content authority, not Historical Content Migration.',
  '',
  `- Page candidates: ${handoffPages.length}`,
  `- ListItem candidates: ${handoffListItems.length}`,
  `- Page problem observations: ${pageProblemCount}`,
  `- ListItem problem observations: ${listItemProblemCount}`,
  `- Other Site Package observations: ${sitePackageObservations.length}`,
  `- Blocking Site Package problem observations: ${sitePackageBlockingProblems}`,
  '',
  '## Current capability note',
  '',
  '- Page bodyHtml already has stable Site Package structure/reconcile support.',
  '- Stable Main ListItem membership does not yet have a Site Package v1 structure type/stable reconcile path; this is a separate Site Package capability gap.',
  '- Source errors remain preserved in `site-package-handoff.json` for explicit follow-up; they are not silently repaired or discarded.',
  '',
]
await writeFile(path.join(outputRoot, 'site-package-handoff.md'), handoffLines.join('\n') + '\n', 'utf8')

console.log(`EU50_MAIN_ARTICLE_ELIGIBILITY ${JSON.stringify(report.summary)}`)
