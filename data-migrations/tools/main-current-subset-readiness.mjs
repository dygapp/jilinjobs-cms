import { readFile, writeFile } from 'node:fs/promises'
import path from 'node:path'

const outputRoot = path.resolve(process.env.MAIN_ELIGIBILITY_OUTPUT || 'main/v1/generated-import-eligibility')
const controlPath = path.resolve(process.env.MAIN_ELIGIBILITY_CONTROL || 'main/v1/import-eligibility-control.json')

const [report, eligible, withheld, sourceDefects, control] = await Promise.all([
  readJson(path.join(outputRoot, 'import-eligibility.json')),
  readJson(path.join(outputRoot, 'import-eligible-index.json')),
  readJson(path.join(outputRoot, 'import-withheld.json')),
  readJson(path.join(outputRoot, 'source-defect-articles.json')),
  readJson(controlPath),
])

const summary = report.summary || {}
const deferred = Array.isArray(withheld.withheldReviewArticles) ? withheld.withheldReviewArticles : []
const excluded = Array.isArray(withheld.excludedSourceDefectArticles) ? withheld.excludedSourceDefectArticles : []
const unscoped = Array.isArray(withheld.unscopedBlockingObservations) ? withheld.unscopedBlockingObservations : []
const eligibleArticles = Array.isArray(eligible.articles) ? eligible.articles : []

if (summary.migrationScope !== 'ARTICLE_ONLY') throw new Error(`unexpected migration scope ${summary.migrationScope}`)
if ('pages' in eligible || 'lists' in eligible) throw new Error('current import eligibility must contain Articles only')
if (Number(summary.totalCandidates) !== Number(summary.eligibleCandidates) + Number(summary.excludedSourceDefectCandidates) + Number(summary.withheldCandidates)) {
  throw new Error('Article eligibility arithmetic mismatch')
}
if (eligibleArticles.length !== Number(summary.eligibleCandidates)) throw new Error('eligible index count mismatch')
if (excluded.length !== Number(summary.excludedSourceDefectCandidates) || sourceDefects.length !== excluded.length) throw new Error('source-defect evidence count mismatch')
if (deferred.length !== Number(summary.withheldCandidates)) throw new Error('deferred problem Article evidence count mismatch')
if (unscoped.length !== Number(summary.unscopedBlockingObservations)) throw new Error('unscoped blocking evidence count mismatch')
if (Number(summary.finalRetry?.retryableTransient || 0) !== 0) throw new Error('retryable targets remain after bounded retry closure')
if (eligibleArticles.length <= 0) throw new Error('no current import-eligible Articles')
if (unscoped.length !== 0) throw new Error('unscoped blocking migration observations remain')
if (control.policy?.deferredProblemArticles !== 'PRESERVE_AS_DURABLE_EVIDENCE_DO_NOT_BLOCK_CURRENT_SUBSET_PROMOTION') {
  throw new Error('deferred Article policy is not authorized for current-subset promotion')
}
if (control.policy?.noSilentRepair !== true || control.policy?.noSilentDiscard !== true) {
  throw new Error('no-silent-repair / no-silent-discard policy missing')
}

const readiness = {
  version: 1,
  migrationScope: 'ARTICLE_ONLY',
  currentSubsetPromotionReady: true,
  allProblemArticlesResolved: deferred.length === 0 && excluded.length === 0,
  counts: {
    totalArticles: Number(summary.totalCandidates),
    currentImportEligibleArticles: eligibleArticles.length,
    sourceDefectExcludedArticles: excluded.length,
    deferredProblemArticles: deferred.length,
    unscopedBlockingObservations: unscoped.length,
  },
  policy: {
    currentImport: 'IMPORT_ELIGIBLE_INDEX_ONLY',
    deferredProblemArticles: control.policy.deferredProblemArticles,
    sourceDefectArticles: control.policy.sourceDefectArticles,
    noSilentRepair: true,
    noSilentDiscard: true,
  },
  provenance: report.provenance || null,
}

await writeFile(path.join(outputRoot, 'current-subset-readiness.json'), JSON.stringify(readiness, null, 2) + '\n', 'utf8')
console.log('EU50_MAIN_CURRENT_SUBSET_READINESS', JSON.stringify(readiness))

async function readJson(file) {
  return JSON.parse(await readFile(file, 'utf8'))
}
