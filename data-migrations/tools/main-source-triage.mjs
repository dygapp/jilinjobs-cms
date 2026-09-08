import { createHash } from 'node:crypto'
import { mkdir, readFile, readdir, rm, writeFile } from 'node:fs/promises'
import path from 'node:path'

const inventoryRoot = path.resolve(process.env.MAIN_INVENTORY_ROOT || 'main/v1/generated-inventory')
const outputRoot = path.resolve(process.env.MIGRATION_OUTPUT || 'main/v1/generated-candidate')

const sha256 = value => createHash('sha256').update(value).digest('hex')
const sha256Bytes = bytes => createHash('sha256').update(bytes).digest('hex')

async function readJson(file) {
  return JSON.parse(await readFile(file, 'utf8'))
}
async function writeJson(file, value) {
  await mkdir(path.dirname(file), { recursive: true })
  await writeFile(file, JSON.stringify(value, null, 2) + '\n', 'utf8')
}
function csvCell(value) {
  const text = String(value ?? '')
  return /[",\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text
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
function canonicalIssue(issue) {
  return { ...issue, classification: issueClassification(issue) }
}
function articleMetadata(item) {
  return {
    candidateType: 'ARTICLE',
    legacyKey: item.legacyKey,
    contentId: item.contentId ?? null,
    articleType: item.articleType,
    columnAlias: item.columnAlias,
    title: item.title,
    publishDate: item.publishDate ?? null,
    sourceUrl: item.url,
  }
}
function pageMetadata(surface) {
  return {
    candidateType: 'PAGE',
    legacyKey: `main-page:${surface.key}`,
    surfaceKey: surface.key,
    title: surface.title,
    sourcePath: surface.path,
    target: surface.target,
  }
}
function issueRecordTargets(issue, inventoryByLegacyKey, inventoryByContentId, pageSurfaceByKey) {
  const targets = []
  if (issue.legacyKey && inventoryByLegacyKey.has(issue.legacyKey)) {
    targets.push({ type: 'ARTICLE', key: issue.legacyKey })
    return targets
  }
  const evidenceKey = String(issue.evidenceKey || '')
  if (evidenceKey && inventoryByLegacyKey.has(evidenceKey)) {
    targets.push({ type: 'ARTICLE', key: evidenceKey })
    return targets
  }
  const contentMatch = evidenceKey.match(/(?:^|:)content:(\d+)/)
  if (contentMatch) {
    for (const item of inventoryByContentId.get(contentMatch[1]) || []) targets.push({ type: 'ARTICLE', key: item.legacyKey })
    if (targets.length > 0) return targets
  }
  const pageKey = issue.surfaceKey || evidenceKey.split('#')[0]
  if (pageKey && pageSurfaceByKey.has(pageKey)) {
    targets.push({ type: 'PAGE', key: `main-page:${pageKey}` })
    return targets
  }
  if (issue.listCode && issue.legacyKey) {
    targets.push({ type: 'LIST_ITEM', key: `${issue.listCode}\u0000${issue.legacyKey}` })
    return targets
  }
  return targets
}
async function readArticleIndex() {
  const text = await readFile(path.join(outputRoot, 'index.ndjson'), 'utf8')
  return text.split(/\r?\n/).filter(Boolean).map(line => JSON.parse(line))
}
async function writeArticleIndex(items) {
  await writeFile(
    path.join(outputRoot, 'index.ndjson'),
    items.map(item => JSON.stringify({ legacyKey: item.legacyKey, path: item.path })).join('\n') + (items.length ? '\n' : ''),
    'utf8',
  )
}
async function readListIndexes() {
  const listsRoot = path.join(outputRoot, 'lists')
  const entries = await readdir(listsRoot, { withFileTypes: true })
  const values = []
  for (const entry of entries) {
    if (!entry.isDirectory()) continue
    const file = path.join(listsRoot, entry.name, 'index.json')
    const index = await readJson(file)
    values.push({ listCode: entry.name, file, root: path.dirname(file), index })
  }
  return values
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
  for (const file of files.sort((a, b) => a.relative.localeCompare(b.relative))) {
    rows.push(`${file.relative}\u0000${sha256Bytes(await readFile(file.absolute))}`)
  }
  return { digest: `sha256:${sha256(rows.join('\n'))}`, files: rows.length }
}
function issueSummary(values) {
  const counts = new Map()
  for (const issue of values) counts.set(issue.classification, (counts.get(issue.classification) || 0) + 1)
  return [...counts.entries()].sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0])).map(([classification, count]) => ({ classification, count }))
}
function markdownTable(rows) {
  if (rows.length === 0) return '_None_\n'
  const header = '| Type | Title / Identity | Source | Error |\n|---|---|---|---|\n'
  const lines = rows.map(row => {
    const title = String(row.title || row.legacyKey || row.surfaceKey || row.identity || '').replaceAll('|', '\\|').replaceAll('\n', ' ')
    const source = String(row.sourceUrl || row.sourcePath || '').replaceAll('|', '\\|')
    const error = [...new Set((row.issues || []).map(issue => issue.classification))].join(', ').replaceAll('|', '\\|')
    return `| ${row.candidateType || 'OBSERVATION'} | ${title} | ${source} | ${error} |`
  })
  return header + lines.join('\n') + '\n'
}

async function main() {
  const issuesPath = path.join(outputRoot, 'reports', 'issues.json')
  const reconciliationPath = path.join(outputRoot, 'reports', 'reconciliation.json')
  const manifestPath = path.join(outputRoot, 'manifest.json')
  const sourceSurfacesPath = path.join(outputRoot, 'source-discovery', 'source-surfaces.json')
  const [rawIssues, reconciliation, manifest, inventory, sourceSurfaces] = await Promise.all([
    readJson(issuesPath),
    readJson(reconciliationPath),
    readJson(manifestPath),
    readJson(path.join(inventoryRoot, 'inventory.json')),
    readJson(sourceSurfacesPath),
  ])
  const issues = rawIssues.map(canonicalIssue)
  const inventoryByLegacyKey = new Map(inventory.map(item => [item.legacyKey, item]))
  const inventoryByContentId = new Map()
  for (const item of inventory) {
    if (item.contentId == null) continue
    const key = String(item.contentId)
    if (!inventoryByContentId.has(key)) inventoryByContentId.set(key, [])
    inventoryByContentId.get(key).push(item)
  }
  const pageSurfaceByKey = new Map((sourceSurfaces.pageSurfaces || []).map(surface => [surface.key, surface]))

  const articleIndex = await readArticleIndex()
  const pageIndexPath = path.join(outputRoot, 'pages', 'index.json')
  const pageIndex = await readJson(pageIndexPath)
  const listIndexes = await readListIndexes()
  const listRefByKey = new Map()
  for (const list of listIndexes) for (const item of list.index.items || []) listRefByKey.set(`${list.listCode}\u0000${item.legacyKey}`, { list, item })

  const recordIssues = new Map()
  const observationIssues = []
  for (const issue of issues) {
    const targets = issueRecordTargets(issue, inventoryByLegacyKey, inventoryByContentId, pageSurfaceByKey)
    if (targets.length === 0) {
      observationIssues.push(issue)
      continue
    }
    for (const target of targets) {
      const id = `${target.type}\u0000${target.key}`
      if (!recordIssues.has(id)) recordIssues.set(id, { ...target, issues: [] })
      recordIssues.get(id).issues.push(issue)
    }
  }

  const excludedArticles = []
  const manualRecords = []
  const blockedArticleKeys = new Set()
  const blockedPageKeys = new Set()
  const blockedListKeys = new Set()

  for (const record of recordIssues.values()) {
    if (record.type === 'ARTICLE') {
      const item = inventoryByLegacyKey.get(record.key)
      const allMissing = record.issues.length > 0 && record.issues.every(issue => issue.classification === 'SOURCE_RESOURCE_MISSING')
      if (allMissing && item?.articleType === 'INTERNAL') {
        excludedArticles.push({
          ...articleMetadata(item),
          decision: 'EXCLUDE_PENDING_CLIENT_CONFIRMATION',
          reason: 'SOURCE_RESOURCE_MISSING',
          issues: record.issues,
        })
        blockedArticleKeys.add(record.key)
      } else {
        manualRecords.push({
          ...(item ? articleMetadata(item) : { candidateType: 'ARTICLE', legacyKey: record.key }),
          decision: 'HUMAN_REVIEW_REQUIRED',
          issues: record.issues,
        })
        blockedArticleKeys.add(record.key)
      }
      continue
    }
    if (record.type === 'PAGE') {
      const surfaceKey = record.key.replace(/^main-page:/, '')
      const surface = pageSurfaceByKey.get(surfaceKey)
      manualRecords.push({
        ...(surface ? pageMetadata(surface) : { candidateType: 'PAGE', legacyKey: record.key, surfaceKey }),
        decision: 'HUMAN_REVIEW_REQUIRED',
        issues: record.issues,
      })
      blockedPageKeys.add(record.key)
      continue
    }
    if (record.type === 'LIST_ITEM') {
      const ref = listRefByKey.get(record.key)
      manualRecords.push({
        candidateType: 'LIST_ITEM',
        legacyKey: ref?.item.legacyKey || record.key.split('\u0000')[1],
        listCode: ref?.list.listCode || record.key.split('\u0000')[0],
        decision: 'HUMAN_REVIEW_REQUIRED',
        issues: record.issues,
      })
      blockedListKeys.add(record.key)
    }
  }

  const keptArticleRefs = []
  for (const ref of articleIndex) {
    if (!blockedArticleKeys.has(ref.legacyKey)) {
      keptArticleRefs.push(ref)
      continue
    }
    await rm(path.join(outputRoot, path.dirname(ref.path)), { recursive: true, force: true })
  }
  await writeArticleIndex(keptArticleRefs)

  const keptPageRefs = []
  for (const ref of pageIndex.items || []) {
    if (!blockedPageKeys.has(ref.legacyKey)) {
      keptPageRefs.push(ref)
      continue
    }
    await rm(path.join(outputRoot, 'pages', path.dirname(ref.path)), { recursive: true, force: true })
  }
  pageIndex.items = keptPageRefs
  await writeJson(pageIndexPath, pageIndex)

  for (const list of listIndexes) {
    const kept = []
    for (const ref of list.index.items || []) {
      const key = `${list.listCode}\u0000${ref.legacyKey}`
      if (!blockedListKeys.has(key)) {
        kept.push(ref)
        continue
      }
      await rm(path.join(list.root, path.dirname(ref.path)), { recursive: true, force: true })
    }
    list.index.items = kept
    await writeJson(list.file, list.index)
  }

  const manualObservations = observationIssues.map(issue => ({
    candidateType: 'OBSERVATION',
    decision: 'HUMAN_REVIEW_REQUIRED',
    identity: issue.listCode || issue.surfaceKey || issue.evidenceKey || issue.code,
    issues: [issue],
  }))

  excludedArticles.sort((a, b) => a.legacyKey.localeCompare(b.legacyKey))
  manualRecords.sort((a, b) => `${a.candidateType}:${a.legacyKey || a.surfaceKey || ''}`.localeCompare(`${b.candidateType}:${b.legacyKey || b.surfaceKey || ''}`))

  const allManualIssues = [...manualRecords.flatMap(record => record.issues), ...manualObservations.flatMap(record => record.issues)]
  const errorSummary = {
    generatedAt: new Date().toISOString(),
    policy: {
      automaticExclusion: 'INTERNAL Article only when every blocking issue is SOURCE_RESOURCE_MISSING (HTTP 404/410)',
      clientConfirmation: 'excluded source-defect articles remain listed for later customer confirmation',
      allOtherErrors: 'HUMAN_REVIEW_REQUIRED and promotion-blocking',
    },
    sourceDefectArticles: excludedArticles.length,
    manualReviewRecords: manualRecords.length,
    manualReviewObservations: manualObservations.length,
    byClassification: issueSummary(issues),
    manualByClassification: issueSummary(allManualIssues),
  }
  await writeJson(path.join(outputRoot, 'reports', 'error-summary.json'), errorSummary)
  await writeJson(path.join(outputRoot, 'reports', 'source-defect-articles.json'), excludedArticles)
  await writeJson(path.join(outputRoot, 'reports', 'manual-review.json'), { summary: errorSummary, records: manualRecords, observations: manualObservations })

  const defectMarkdown = [
    '# 源站缺失资源文章清单（待客户确认）',
    '',
    '以下文章的全部阻断错误均属于已确认的源站资源缺失（HTTP 404/410）。当前迁移候选先排除这些文章，并保留清单供后续客户确认。',
    '',
    markdownTable(excludedArticles),
  ].join('\n')
  await writeFile(path.join(outputRoot, 'reports', 'source-defect-articles.md'), defectMarkdown, 'utf8')
  const csvRows = ['legacyKey,contentId,columnAlias,title,publishDate,sourceUrl,missingResourceCount']
  for (const row of excludedArticles) csvRows.push([
    row.legacyKey, row.contentId, row.columnAlias, row.title, row.publishDate, row.sourceUrl, row.issues.length,
  ].map(csvCell).join(','))
  await writeFile(path.join(outputRoot, 'reports', 'source-defect-articles.csv'), csvRows.join('\n') + '\n', 'utf8')

  const manualMarkdown = [
    '# Main 历史内容迁移人工确认清单',
    '',
    `待确认候选记录：${manualRecords.length}`,
    '',
    `待确认观察项：${manualObservations.length}`,
    '',
    '以下错误不自动视为源站资源缺失；在形成明确人工结论前保持 unresolved，并持续阻断 accepted snapshot promotion。',
    '',
    markdownTable(manualRecords),
    '## Review-only observations',
    '',
    markdownTable(manualObservations),
  ].join('\n')
  await writeFile(path.join(outputRoot, 'reports', 'manual-review.md'), manualMarkdown, 'utf8')

  const keptListCount = listIndexes.reduce((sum, list) => sum + (list.index.items || []).length, 0)
  const unresolvedCandidateCount = manualRecords.length
  const acceptedCanonical = keptArticleRefs.length + keptPageRefs.length + keptListCount
  const excluded = excludedArticles.length
  const discoveredUniqueCandidates = reconciliation.discoveredUniqueCandidates
  const arithmeticClosed = discoveredUniqueCandidates === acceptedCanonical + excluded + unresolvedCandidateCount
  const integrity = await canonicalDatasetDigest()

  reconciliation.articles = {
    discovered: inventory.length,
    accepted: keptArticleRefs.length,
    internal: keptArticleRefs.filter(ref => inventoryByLegacyKey.get(ref.legacyKey)?.articleType === 'INTERNAL').length,
    external: keptArticleRefs.filter(ref => inventoryByLegacyKey.get(ref.legacyKey)?.articleType === 'EXTERNAL_LINK').length,
  }
  reconciliation.pages = { ...reconciliation.pages, accepted: keptPageRefs.length }
  reconciliation.lists = { ...reconciliation.lists, accepted: keptListCount, byCode: listIndexes.map(list => ({ listCode: list.listCode, count: (list.index.items || []).length })).sort((a, b) => a.listCode.localeCompare(b.listCode)) }
  reconciliation.acceptedCanonical = acceptedCanonical
  reconciliation.excluded = excluded
  reconciliation.unresolved = unresolvedCandidateCount
  reconciliation.manualReviewObservations = manualObservations.length
  reconciliation.errorIssueCount = issues.length
  reconciliation.arithmeticClosed = arithmeticClosed
  reconciliation.datasetDigest = integrity.digest
  reconciliation.datasetFiles = integrity.files
  reconciliation.promotionReady = arithmeticClosed && unresolvedCandidateCount === 0 && manualObservations.length === 0 && acceptedCanonical > 0
  await writeJson(reconciliationPath, reconciliation)

  manifest.candidate = {
    ...manifest.candidate,
    articles: reconciliation.articles.accepted,
    internalArticles: reconciliation.articles.internal,
    externalArticles: reconciliation.articles.external,
    pages: reconciliation.pages.accepted,
    listItems: reconciliation.lists.accepted,
    excluded,
    unresolved: unresolvedCandidateCount,
    manualReviewObservations: manualObservations.length,
    datasetDigest: reconciliation.datasetDigest,
    datasetFiles: reconciliation.datasetFiles,
  }
  await writeJson(manifestPath, manifest)

  console.log(`EU50_MAIN_TRIAGE ${JSON.stringify({
    sourceDefectArticles: excludedArticles.length,
    manualReviewRecords: manualRecords.length,
    manualReviewObservations: manualObservations.length,
    acceptedCanonical,
    excluded,
    unresolved: unresolvedCandidateCount,
    arithmeticClosed,
    promotionReady: reconciliation.promotionReady,
  })}`)
  if (!reconciliation.promotionReady) process.exitCode = 1
}

await main()
