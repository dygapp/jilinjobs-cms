import { access, readFile, stat } from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'

const canonicalRoot = path.resolve(process.env.MAIN_CANONICAL_ROOT || 'main/v1')
const manifest = await readJson(path.join(canonicalRoot, 'manifest.json'))
const withheld = await readJson(path.join(canonicalRoot, 'reports', 'import-withheld.json'))
const sourceDefects = await readJson(path.join(canonicalRoot, 'reports', 'source-defect-articles.json'))
const promotionSummary = await readJson(path.join(canonicalRoot, 'reports', 'promotion-summary.json'))

if (manifest.migrationId !== 'main-v1' || manifest.status !== 'accepted-current-subset') throw new Error('Main manifest is not accepted-current-subset')
if (manifest.acceptance?.currentSubset !== 'PASS') throw new Error('Main current subset acceptance is not PASS')
if (manifest.acceptance?.noSilentRepair !== true || manifest.acceptance?.noSilentDiscard !== true) throw new Error('no-silent policy missing from accepted manifest')
if (manifest.acceptance?.deferredProblemArticles !== 'LATER_REVIEW_NOT_CURRENT_IMPORT') throw new Error('deferred Article acceptance policy mismatch')

for (const forbidden of ['pages', 'lists']) {
  try {
    await access(path.join(canonicalRoot, forbidden))
    throw new Error(`forbidden Main migration unit directory exists: ${forbidden}`)
  } catch (error) {
    if (error?.code !== 'ENOENT') throw error
  }
}

const indexText = await readFile(path.join(canonicalRoot, 'index.ndjson'), 'utf8')
const entries = indexText.split(/\r?\n/).filter(Boolean).map(line => JSON.parse(line))
if (entries.length !== Number(manifest.acceptedSnapshot?.currentImportEligibleArticles)) throw new Error('manifest/index Article count mismatch')
if (new Set(entries.map(item => item.legacyKey)).size !== entries.length) throw new Error('duplicate Article legacyKey in accepted index')
if (new Set(entries.map(item => item.path)).size !== entries.length) throw new Error('duplicate Article path in accepted index')

let internalArticles = 0
let externalArticles = 0
let resourceFiles = 0
let resourceBytes = 0
const digestEntries = []

for (const entry of entries) {
  if (typeof entry.legacyKey !== 'string' || !entry.legacyKey) throw new Error('accepted index legacyKey missing')
  if (typeof entry.path !== 'string' || !/^articles\/[^/]+\/article\.json$/.test(entry.path)) throw new Error(`unsafe accepted Article path ${entry.path}`)
  const articlePath = safeResolve(canonicalRoot, entry.path, 'accepted Article path')
  const article = await readJson(articlePath)
  if (article.source?.legacyKey !== entry.legacyKey) throw new Error(`accepted legacyKey mismatch ${entry.legacyKey}`)
  if (!/^[0-9a-f]{64}$/.test(String(article.sourceFingerprint || ''))) throw new Error(`accepted sourceFingerprint invalid ${entry.legacyKey}`)
  digestEntries.push(await fileDigestEntry(canonicalRoot, articlePath))

  const articleType = String(article.target?.articleType || '')
  if (articleType === 'INTERNAL') internalArticles += 1
  else if (articleType === 'EXTERNAL_LINK') externalArticles += 1
  else throw new Error(`unexpected accepted Article type ${articleType}`)

  const resources = Array.isArray(article.resources) ? article.resources : []
  const seen = new Set()
  for (const resource of resources) {
    const raw = String(resource.snapshotPath || '')
    if (!raw || seen.has(raw)) throw new Error(`invalid/duplicate accepted resource path ${raw}`)
    seen.add(raw)
    const resourcePath = safeResolve(path.dirname(articlePath), raw, 'accepted Article resource')
    const s = await stat(resourcePath)
    if (!s.isFile()) throw new Error(`accepted resource is not regular file ${raw}`)
    if (s.size !== Number(resource.sizeBytes)) throw new Error(`accepted resource size mismatch ${entry.legacyKey} ${raw}`)
    const sha = await sha256File(resourcePath)
    if (sha !== resource.sha256) throw new Error(`accepted resource SHA-256 mismatch ${entry.legacyKey} ${raw}`)
    digestEntries.push({ path: path.relative(canonicalRoot, resourcePath).split(path.sep).join('/'), sizeBytes: s.size, sha256: sha })
    resourceFiles += 1
    resourceBytes += s.size
  }
}

digestEntries.push(await fileDigestEntry(canonicalRoot, path.join(canonicalRoot, 'index.ndjson')))
for (const relative of [
  'reports/import-eligibility.json',
  'reports/import-withheld.json',
  'reports/source-defect-articles.json',
  'reports/site-package-handoff.json',
  'source-discovery/provenance.json',
]) digestEntries.push(await fileDigestEntry(canonicalRoot, path.join(canonicalRoot, relative)))

digestEntries.sort((a, b) => a.path.localeCompare(b.path))
const datasetDigest = `sha256:${sha256Text(digestEntries.map(item => `${item.path}\u0000${item.sha256}\u0000${item.sizeBytes}`).join('\n'))}`
if (datasetDigest !== manifest.acceptedSnapshot?.datasetDigest) throw new Error(`accepted dataset digest mismatch expected=${manifest.acceptedSnapshot?.datasetDigest} actual=${datasetDigest}`)
if (datasetDigest !== promotionSummary.datasetDigest) throw new Error('promotion summary dataset digest mismatch')

const deferred = Array.isArray(withheld.deferredProblemArticles) ? withheld.deferredProblemArticles : []
if (deferred.length !== Number(manifest.acceptedSnapshot?.deferredProblemArticles)) throw new Error('deferred problem Article count mismatch')
if (sourceDefects.length !== Number(manifest.acceptedSnapshot?.sourceDefectExcludedArticles)) throw new Error('source-defect Article count mismatch')
if (internalArticles !== Number(manifest.acceptedSnapshot?.internalArticles) || externalArticles !== Number(manifest.acceptedSnapshot?.externalArticles)) throw new Error('accepted Article type count mismatch')
if (resourceFiles !== Number(manifest.acceptedSnapshot?.resourceFiles) || resourceBytes !== Number(manifest.acceptedSnapshot?.resourceBytes)) throw new Error('accepted resource count/bytes mismatch')

const result = {
  migrationId: manifest.migrationId,
  status: manifest.status,
  currentImportEligibleArticles: entries.length,
  internalArticles,
  externalArticles,
  sourceDefectExcludedArticles: sourceDefects.length,
  deferredProblemArticles: deferred.length,
  resourceFiles,
  resourceBytes,
  datasetDigest,
  offlineVerification: 'PASS',
}
console.log('EU50_MAIN_ACCEPTED_SUBSET_VERIFICATION', JSON.stringify(result))

function safeResolve(root, raw, label) {
  const normalizedRoot = path.resolve(root)
  const resolved = path.resolve(root, raw)
  if (!(resolved === normalizedRoot || resolved.startsWith(normalizedRoot + path.sep))) throw new Error(`${label} escapes root: ${raw}`)
  return resolved
}
async function readJson(file) { return JSON.parse(await readFile(file, 'utf8')) }
async function sha256File(file) { return crypto.createHash('sha256').update(await readFile(file)).digest('hex') }
function sha256Text(value) { return crypto.createHash('sha256').update(value, 'utf8').digest('hex') }
async function fileDigestEntry(root, file) {
  const s = await stat(file)
  return { path: path.relative(root, file).split(path.sep).join('/'), sizeBytes: s.size, sha256: await sha256File(file) }
}
