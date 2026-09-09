import { createHash } from 'node:crypto'
import { readFile, writeFile } from 'node:fs/promises'
import path from 'node:path'

const outputRoot = path.resolve(process.env.MAIN_ELIGIBILITY_OUTPUT || 'main/v1/generated-import-eligibility')
const sitePackageRoot = path.resolve(process.env.MAIN_SITE_PACKAGE_ROOT || '../sites/jilinjobs')

const handoffPath = path.join(outputRoot, 'site-package-handoff.json')
const handoff = JSON.parse(await readFile(handoffPath, 'utf8'))
const pages = JSON.parse(await readFile(path.join(sitePackageRoot, 'structure/pages.json'), 'utf8'))
const assetManifest = JSON.parse(await readFile(path.join(sitePackageRoot, 'assets/manifest.json'), 'utf8'))

const budgetResolutionBySource = new Map([
  ['https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2023-02-27/8331fd04-91d2-4365-8576-44a7adb40d44.pdf', '/static/pages/budget/budget-2023.pdf'],
  ['https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2024-03-12/d87173cd-8a3f-4667-a042-ece836a0f0ea.pdf', '/static/pages/budget/budget-2024.pdf'],
  ['https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2025-03-07/ceb5b088-6158-4b73-b446-3cf650194866.pdf', '/static/pages/budget/budget-2025.pdf'],
  ['https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2026-03-17/84773049-6580-4c9d-87ce-ab29f9576157.pdf', '/static/pages/budget/budget-2026.pdf'],
  ['https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2023-09-08/e74779f2-e551-43ad-a5f8-0a91020e8d00.pdf', '/static/pages/budget/final-accounts-2022.pdf'],
  ['https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2024-09-12/f7b003d5-9074-4d26-ac58-827131c02f22.pdf', '/static/pages/budget/final-accounts-2023.pdf'],
  ['https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2025-09-05/4851fa77-f787-4d7f-be10-34490a98df0f.pdf', '/static/pages/budget/final-accounts-2024.pdf'],
  ['https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2026-09-04/75b071b2-c354-4690-bb8e-2bdc25e600bb.pdf', '/static/pages/budget/final-accounts-2025.pdf'],
])

const budgetExpectedTargets = [
  '/static/pages/budget/management-policy.pdf',
  '/static/pages/budget/budget-2021.pdf',
  '/static/pages/budget/budget-2022.pdf',
  '/static/pages/budget/budget-2023.pdf',
  '/static/pages/budget/budget-2024.pdf',
  '/static/pages/budget/budget-2025.pdf',
  '/static/pages/budget/budget-2026.pdf',
  '/static/pages/budget/final-accounts-2020.pdf',
  '/static/pages/budget/final-accounts-2021.pdf',
  '/static/pages/budget/final-accounts-2022.pdf',
  '/static/pages/budget/final-accounts-2023.pdf',
  '/static/pages/budget/final-accounts-2024.pdf',
  '/static/pages/budget/final-accounts-2025.pdf',
]

const sha256 = bytes => createHash('sha256').update(bytes).digest('hex')
const assetByTarget = new Map((assetManifest.assets || []).map(item => [item.target, item]))
const budgetPage = pages.find(item => item.groupAlias == null && item.alias === 'budget')
if (!budgetPage) throw new Error('stable Site Package budget Page is missing')

const hrefs = [...String(budgetPage.bodyHtml || '').matchAll(/href=["']([^"']+)["']/g)].map(match => match[1])
if (hrefs.length !== budgetExpectedTargets.length) {
  throw new Error(`budget Page expected ${budgetExpectedTargets.length} attachment hrefs, got ${hrefs.length}`)
}
if (new Set(hrefs).size !== hrefs.length) throw new Error('budget Page contains duplicate attachment hrefs')
for (const target of budgetExpectedTargets) {
  if (!hrefs.includes(target)) throw new Error(`budget Page stable target missing: ${target}`)
  const asset = assetByTarget.get(target)
  if (!asset) throw new Error(`asset manifest target missing: ${target}`)
  const sourcePath = path.resolve(sitePackageRoot, asset.source)
  const bytes = await readFile(sourcePath)
  if (!bytes.subarray(0, 5).equals(Buffer.from('%PDF-'))) throw new Error(`budget asset is not PDF: ${asset.source}`)
  const actual = sha256(bytes)
  if (actual !== asset.sha256) throw new Error(`budget asset digest mismatch: ${asset.source}`)
}

const budgetHandoff = (handoff.pages || []).find(item => item.legacyKey === 'main-page:budget')
if (!budgetHandoff) throw new Error('budget source handoff record missing')
if ((budgetHandoff.problems || []).length !== budgetResolutionBySource.size) {
  throw new Error(`budget source handoff expected ${budgetResolutionBySource.size} problems, got ${(budgetHandoff.problems || []).length}`)
}

const resolvedPageNormalizations = []
const unresolvedPageProblems = []
for (const page of handoff.pages || []) {
  for (const problem of page.problems || []) {
    if (page.legacyKey === 'main-page:budget') {
      const source = problem.rawReference || problem.targetUrl || null
      const target = budgetResolutionBySource.get(source)
      if (
        target
        && problem.classification === 'RESOURCE_HOST_OUTSIDE_APPROVED_BOUNDARY'
        && problem.code === 'ATTACHMENT_FETCH_FAILED'
      ) {
        resolvedPageNormalizations.push({
          legacyKey: page.legacyKey,
          originalClassification: problem.classification,
          resolutionClassification: 'SITE_PACKAGE_PAGE_ATTACHMENT_NORMALIZED',
          sourceReference: source,
          stableTarget: target,
          decision: 'PACKAGE_OWNED_STABLE_ASSET',
          blocking: false,
        })
        continue
      }
    }
    unresolvedPageProblems.push({ legacyKey: page.legacyKey, ...problem })
  }
}

if (resolvedPageNormalizations.length !== budgetResolutionBySource.size) {
  throw new Error(`expected ${budgetResolutionBySource.size} resolved budget Page normalizations, got ${resolvedPageNormalizations.length}`)
}
if (unresolvedPageProblems.length !== 0) {
  throw new Error(`unresolved Page source problems remain: ${JSON.stringify(unresolvedPageProblems.slice(0, 5))}`)
}

const unresolvedListItemProblems = (handoff.listItems || []).flatMap(item =>
  (item.problems || []).filter(problem => problem.blocking !== false).map(problem => ({
    listCode: item.listCode,
    legacyKey: item.legacyKey,
    ...problem,
  })),
)
const unresolvedObservations = (handoff.observations || []).filter(problem => problem.blocking !== false)

const report = {
  generatedAt: new Date().toISOString(),
  purpose: 'Resolve Site Package-owned Main Page/ListItem source findings against current repository-owned Site Package content without altering Historical Migration Article eligibility.',
  policy: {
    ownership: 'SITE_PACKAGE',
    budgetPageAssets: 'PACKAGE_OWNED_STABLE_ASSETS',
    budgetPublicRoot: '/static/pages/budget/',
    legacyAbsoluteStorageLayoutRetained: false,
    articleMigrationPolicyChanged: false,
  },
  summary: {
    pageCandidates: (handoff.pages || []).length,
    listItemCandidates: (handoff.listItems || []).length,
    pageSourceProblemObservations: resolvedPageNormalizations.length + unresolvedPageProblems.length,
    pageResolvedNormalizations: resolvedPageNormalizations.length,
    pageUnresolvedProblems: unresolvedPageProblems.length,
    listItemUnresolvedProblems: unresolvedListItemProblems.length,
    otherUnresolvedObservations: unresolvedObservations.length,
    unresolvedTotal: unresolvedPageProblems.length + unresolvedListItemProblems.length + unresolvedObservations.length,
    sitePackageContentReady: unresolvedPageProblems.length === 0 && unresolvedListItemProblems.length === 0 && unresolvedObservations.length === 0,
  },
  resolvedPageNormalizations,
  unresolvedPageProblems,
  unresolvedListItemProblems,
  unresolvedObservations,
}

await writeFile(path.join(outputRoot, 'site-package-handoff-resolution.json'), JSON.stringify(report, null, 2) + '\n', 'utf8')
const lines = [
  '# Main Site Package Handoff Resolution',
  '',
  `- Page candidates: ${report.summary.pageCandidates}`,
  `- ListItem candidates: ${report.summary.listItemCandidates}`,
  `- Resolved Page normalizations: ${report.summary.pageResolvedNormalizations}`,
  `- Unresolved Page problems: ${report.summary.pageUnresolvedProblems}`,
  `- Unresolved ListItem problems: ${report.summary.listItemUnresolvedProblems}`,
  `- Other unresolved observations: ${report.summary.otherUnresolvedObservations}`,
  `- Site Package content ready: ${report.summary.sitePackageContentReady}`,
  '',
  '## Resolved Page decision',
  '',
  '- `main-page:budget`: 8 Legacy absolute attachment URLs normalized into 13-package-wide coherent `/static/pages/budget/**` attachment ownership. The complete Page attachment set is verified through the Site Package asset manifest and SHA-256.',
  '',
  '## Remaining observations',
  '',
  ...unresolvedObservations.map(item => `- ${item.classification || item.originalClassification || item.code}: ${item.listCode || item.legacyKey || item.identity || '<unknown>'} ${item.rawReference || item.targetUrl || ''}`.trim()),
  '',
]
await writeFile(path.join(outputRoot, 'site-package-handoff-resolution.md'), lines.join('\n') + '\n', 'utf8')
console.log(`EU50_MAIN_SITE_PACKAGE_HANDOFF_RESOLUTION ${JSON.stringify(report.summary)}`)
