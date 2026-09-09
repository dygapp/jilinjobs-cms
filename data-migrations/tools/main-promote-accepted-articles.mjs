import { copyFile, mkdir, readFile, rm, stat, writeFile } from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'

const candidateRoot = path.resolve(process.env.MAIN_CANDIDATE_ROOT || 'main/v1/eligibility-input/generated-candidate')
const eligibilityRoot = path.resolve(process.env.MAIN_ELIGIBILITY_OUTPUT || 'main/v1/generated-import-eligibility')
const canonicalRoot = path.resolve(process.env.MAIN_CANONICAL_ROOT || 'main/v1')
const controlPath = path.resolve(process.env.MAIN_ELIGIBILITY_CONTROL || 'main/v1/import-eligibility-control.json')

const [eligible, report, withheld, sourceDefects, sitePackageHandoff, readiness, control, candidateManifest] = await Promise.all([
  readJson(path.join(eligibilityRoot, 'import-eligible-index.json')),
  readJson(path.join(eligibilityRoot, 'import-eligibility.json')),
  readJson(path.join(eligibilityRoot, 'import-withheld.json')),
  readJson(path.join(eligibilityRoot, 'source-defect-articles.json')),
  readJson(path.join(eligibilityRoot, 'site-package-handoff.json')),
  readJson(path.join(eligibilityRoot, 'current-subset-readiness.json')),
  readJson(controlPath),
  readJson(path.join(candidateRoot, 'manifest.json')),
])

if (readiness.currentSubsetPromotionReady !== true) throw new Error('current Article subset is not promotion-ready')
if (!Array.isArray(eligible.articles) || 'pages' in eligible || 'lists' in eligible) throw new Error('eligible index must contain Articles only')
if (eligible.articles.length !== readiness.counts.currentImportEligibleArticles) throw new Error('eligible count changed after readiness')
if (control.policy?.deferredProblemArticles !== 'PRESERVE_AS_DURABLE_EVIDENCE_DO_NOT_BLOCK_CURRENT_SUBSET_PROMOTION') throw new Error('deferred Article policy mismatch')

const articlesRoot = path.join(canonicalRoot, 'articles')
const reportsRoot = path.join(canonicalRoot, 'reports')
const discoveryRoot = path.join(canonicalRoot, 'source-discovery')
await Promise.all([
  rm(articlesRoot, { recursive: true, force: true }),
  rm(reportsRoot, { recursive: true, force: true }),
  rm(discoveryRoot, { recursive: true, force: true }),
])
await Promise.all([mkdir(articlesRoot, { recursive: true }), mkdir(reportsRoot, { recursive: true }), mkdir(discoveryRoot, { recursive: true })])

const canonicalEntries = []
const indexEntries = []
let internalArticles = 0
let externalArticles = 0
let resourceFiles = 0
let resourceBytes = 0

const sortedEligible = [...eligible.articles].sort((a, b) => String(a.legacyKey).localeCompare(String(b.legacyKey)))
for (const entry of sortedEligible) {
  validateIndexEntry(entry)
  const sourceArticle = safeResolve(candidateRoot, entry.path, 'eligible Article path')
  const articleBytes = await readFile(sourceArticle)
  const article = JSON.parse(articleBytes.toString('utf8'))
  if (article.source?.legacyKey !== entry.legacyKey) throw new Error(`legacyKey mismatch for ${entry.path}`)
  if (!/^[0-9a-f]{64}$/.test(String(article.sourceFingerprint || ''))) throw new Error(`invalid sourceFingerprint for ${entry.legacyKey}`)

  const destinationArticle = safeResolve(canonicalRoot, entry.path, 'canonical Article path')
  await mkdir(path.dirname(destinationArticle), { recursive: true })
  await writeFile(destinationArticle, articleBytes)
  canonicalEntries.push(await fileDigestEntry(canonicalRoot, destinationArticle))

  const articleType = String(article.target?.articleType || '')
  if (articleType === 'INTERNAL') internalArticles += 1
  else if (articleType === 'EXTERNAL_LINK') externalArticles += 1
  else throw new Error(`unexpected Article type ${articleType} for ${entry.legacyKey}`)

  const resources = Array.isArray(article.resources) ? article.resources : []
  const resourcePaths = new Set()
  for (const resource of resources) {
    const raw = String(resource.snapshotPath || '')
    if (!raw || resourcePaths.has(raw)) throw new Error(`invalid/duplicate resource path ${raw} for ${entry.legacyKey}`)
    resourcePaths.add(raw)
    const sourceResource = safeResolve(path.dirname(sourceArticle), raw, 'source Article resource')
    const destinationResource = safeResolve(path.dirname(destinationArticle), raw, 'canonical Article resource')
    const sourceStat = await stat(sourceResource)
    if (!sourceStat.isFile()) throw new Error(`resource is not a regular file: ${raw}`)
    if (Number(resource.sizeBytes) !== sourceStat.size) throw new Error(`resource size mismatch ${entry.legacyKey} ${raw}`)
    const sourceSha = await sha256File(sourceResource)
    if (sourceSha !== resource.sha256) throw new Error(`resource SHA-256 mismatch ${entry.legacyKey} ${raw}`)
    await mkdir(path.dirname(destinationResource), { recursive: true })
    await copyFile(sourceResource, destinationResource)
    canonicalEntries.push(await fileDigestEntry(canonicalRoot, destinationResource))
    resourceFiles += 1
    resourceBytes += sourceStat.size
  }

  indexEntries.push({ legacyKey: entry.legacyKey, path: entry.path })
}

if (internalArticles + externalArticles !== indexEntries.length) throw new Error('Article type arithmetic mismatch')

const indexText = indexEntries.map(item => JSON.stringify(item)).join('\n') + '\n'
await writeFile(path.join(canonicalRoot, 'index.ndjson'), indexText, 'utf8')
canonicalEntries.push(await fileDigestEntry(canonicalRoot, path.join(canonicalRoot, 'index.ndjson')))

const normalizedEligibility = {
  migrationScope: 'ARTICLE_ONLY',
  currentSubsetPromotionReady: true,
  allProblemArticlesResolved: readiness.allProblemArticlesResolved,
  counts: readiness.counts,
  finalRetry: report.summary?.finalRetry || null,
  deferredProblemClassifications: report.summary?.withheldProblemClassifications || {},
  policy: readiness.policy,
  provenance: readiness.provenance,
}
const normalizedWithheld = {
  migrationScope: 'ARTICLE_ONLY',
  policy: 'DEFERRED_NOT_CURRENT_IMPORT',
  excludedSourceDefectArticles: withheld.excludedSourceDefectArticles || [],
  deferredProblemArticles: withheld.withheldReviewArticles || [],
  unscopedBlockingObservations: withheld.unscopedBlockingObservations || [],
}
const normalizedHandoff = stripGeneratedAt(sitePackageHandoff)

await writeJson(path.join(reportsRoot, 'import-eligibility.json'), normalizedEligibility)
await writeJson(path.join(reportsRoot, 'import-withheld.json'), normalizedWithheld)
await writeJson(path.join(reportsRoot, 'source-defect-articles.json'), sourceDefects)
await writeJson(path.join(reportsRoot, 'site-package-handoff.json'), normalizedHandoff)

const provenance = {
  sourceSystem: candidateManifest.sourceSystem,
  sourceRoot: candidateManifest.sourceRoot,
  candidateDatasetDigest: candidateManifest.candidate?.datasetDigest || null,
  candidateCounts: candidateManifest.candidate || null,
  closedRetry: {
    sourceRunId: control.sourceRunId,
    sourceArtifactId: control.sourceArtifactId,
    sourceArtifactName: control.sourceArtifactName,
    sourceArtifactDigest: control.sourceArtifactDigest,
    sourceHead: control.sourceHead,
    finalRetryAttempt: control.finalRetryAttempt,
  },
  policyDecision: {
    date: '2026-09-09',
    issue: 60,
    issueCommentId: 5601906947,
    deferredProblemArticles: 'PRESERVE_AS_DURABLE_EVIDENCE_DO_NOT_BLOCK_CURRENT_SUBSET_PROMOTION',
  },
}
await writeJson(path.join(discoveryRoot, 'provenance.json'), provenance)

for (const file of [
  path.join(reportsRoot, 'import-eligibility.json'),
  path.join(reportsRoot, 'import-withheld.json'),
  path.join(reportsRoot, 'source-defect-articles.json'),
  path.join(reportsRoot, 'site-package-handoff.json'),
  path.join(discoveryRoot, 'provenance.json'),
]) canonicalEntries.push(await fileDigestEntry(canonicalRoot, file))

canonicalEntries.sort((a, b) => a.path.localeCompare(b.path))
const datasetDigest = sha256Text(canonicalEntries.map(item => `${item.path}\u0000${item.sha256}\u0000${item.sizeBytes}`).join('\n'))

const promotionSummary = {
  migrationId: 'main-v1',
  status: 'accepted-current-subset',
  currentImportEligibleArticles: indexEntries.length,
  internalArticles,
  externalArticles,
  sourceDefectExcludedArticles: readiness.counts.sourceDefectExcludedArticles,
  deferredProblemArticles: readiness.counts.deferredProblemArticles,
  resourceFiles,
  resourceBytes,
  canonicalFilesBeforeManifest: canonicalEntries.length,
  datasetDigest: `sha256:${datasetDigest}`,
  sourceArtifactDigest: control.sourceArtifactDigest,
  policyDecisionIssueCommentId: 5601906947,
}
await writeJson(path.join(reportsRoot, 'promotion-summary.json'), promotionSummary)

const manifest = {
  migrationId: 'main-v1',
  formatVersion: 1,
  status: 'accepted-current-subset',
  sourceSystem: candidateManifest.sourceSystem || 'legacy-jilinjobs',
  sourceOrigin: candidateManifest.sourceRoot || null,
  acceptedSnapshot: {
    acceptedAt: '2026-09-09',
    currentImportEligibleArticles: indexEntries.length,
    internalArticles,
    externalArticles,
    resourceFiles,
    resourceBytes,
    sourceDefectExcludedArticles: readiness.counts.sourceDefectExcludedArticles,
    deferredProblemArticles: readiness.counts.deferredProblemArticles,
    unscopedBlockingObservations: readiness.counts.unscopedBlockingObservations,
    datasetDigest: `sha256:${datasetDigest}`,
    digestScope: 'index.ndjson + articles/** + reports/import-eligibility.json + reports/import-withheld.json + reports/source-defect-articles.json + reports/site-package-handoff.json + source-discovery/provenance.json',
    sourceWorkflowRunId: control.sourceRunId,
    sourceArtifactId: control.sourceArtifactId,
    sourceArtifactName: control.sourceArtifactName,
    sourceArtifactDigest: control.sourceArtifactDigest,
    sourceHeadSha: control.sourceHead,
  },
  acceptance: {
    currentSubset: 'PASS',
    deferredProblemArticles: 'LATER_REVIEW_NOT_CURRENT_IMPORT',
    sourceDefectArticles: 'EXCLUDED_PENDING_CLIENT_CONFIRMATION',
    authorityIssue: 60,
    authorityIssueCommentId: 5601906947,
    noSilentRepair: true,
    noSilentDiscard: true,
  },
}
await writeJson(path.join(canonicalRoot, 'manifest.json'), manifest)

console.log('EU50_MAIN_ACCEPTED_SUBSET_PROMOTION', JSON.stringify(promotionSummary))

function validateIndexEntry(entry) {
  if (!entry || typeof entry.legacyKey !== 'string' || !entry.legacyKey) throw new Error('eligible Article legacyKey missing')
  if (typeof entry.path !== 'string' || !/^articles\/[^/]+\/article\.json$/.test(entry.path)) throw new Error(`unsafe Article path ${entry.path}`)
}

function safeResolve(root, raw, label) {
  const normalizedRoot = path.resolve(root)
  const resolved = path.resolve(root, raw)
  if (!(resolved === normalizedRoot || resolved.startsWith(normalizedRoot + path.sep))) throw new Error(`${label} escapes root: ${raw}`)
  return resolved
}

async function fileDigestEntry(root, file) {
  const s = await stat(file)
  return { path: path.relative(root, file).split(path.sep).join('/'), sizeBytes: s.size, sha256: await sha256File(file) }
}

async function sha256File(file) {
  const bytes = await readFile(file)
  return crypto.createHash('sha256').update(bytes).digest('hex')
}

function sha256Text(value) {
  return crypto.createHash('sha256').update(value, 'utf8').digest('hex')
}

async function readJson(file) {
  return JSON.parse(await readFile(file, 'utf8'))
}

async function writeJson(file, value) {
  await mkdir(path.dirname(file), { recursive: true })
  await writeFile(file, JSON.stringify(value, null, 2) + '\n', 'utf8')
}

function stripGeneratedAt(value) {
  if (Array.isArray(value)) return value.map(stripGeneratedAt)
  if (value && typeof value === 'object') {
    const out = {}
    for (const [key, nested] of Object.entries(value)) {
      if (key === 'generatedAt') continue
      out[key] = stripGeneratedAt(nested)
    }
    return out
  }
  return value
}
