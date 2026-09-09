import fs from 'node:fs'
import path from 'node:path'
import { expect, test, type Page, type TestInfo } from '@playwright/test'

type ReviewResource = {
  role: 'BODY_IMAGE' | 'ATTACHMENT'
  sortOrder: number
  runtimeId: number
  sizeBytes: number
  sha256: string
  contentType: string | null
  sourceUrl: string
}

type ReviewArticle = {
  runtimeId: number
  legacyKey: string
  columnAlias: string
  articleType: 'INTERNAL' | 'EXTERNAL_LINK'
  title: string
  source: string
  publishDate: string | null
  externalUrl: string | null
  sortOrder: number
  sourceOrder: number
  bodyLength: number
  bodyImages: ReviewResource[]
  attachments: ReviewResource[]
  resourceCount: number
  columnArticleCount?: number
}

type ReviewEvidence = {
  migrationId: string
  datasetDigest: string
  acceptedArticles: number
  targetAliases: string[]
  columnSamples: ReviewArticle[]
  riskSamples: {
    resourceRichInternal: ReviewArticle
    bodyImageInternal: ReviewArticle | null
    attachmentInternal: ReviewArticle | null
    longBodyInternal: ReviewArticle
    externalLink: ReviewArticle
    oldestPublished: ReviewArticle
    newestPublished: ReviewArticle
  }
}

const enabled = process.env.MAIN_MIGRATION_RUNTIME === 'true'
const samplePath = process.env.MAIN_MIGRATION_REVIEW_SAMPLES || path.resolve('main-runtime-review-samples.json')
let evidence: ReviewEvidence

test.describe('EU-51 imported Main canonical Runtime', () => {
  test.skip(!enabled, 'Only runs against an imported EU-51 Main canonical Runtime')

  test.beforeAll(() => {
    evidence = JSON.parse(fs.readFileSync(samplePath, 'utf8')) as ReviewEvidence
    expect(evidence.migrationId).toBe('main-v1')
    expect(evidence.acceptedArticles).toBe(3078)
    expect(evidence.targetAliases).toHaveLength(10)
    expect(evidence.columnSamples).toHaveLength(10)
  })

  test('all ten Main target Columns render their deterministic first accepted Article', async ({ page }, testInfo) => {
    test.setTimeout(120_000)
    for (const sample of evidence.columnSamples) {
      await gotoReviewPage(page, `/column/${sample.columnAlias}`)
      await expect(page.getByTestId('column-page')).toBeVisible()
      const article = page.getByTestId(`column-article-${sample.runtimeId}`)
      await expect(article).toBeVisible()
      await expect(article.locator('.column-list-title')).toHaveText(sample.title)
      if (sample.articleType === 'EXTERNAL_LINK') {
        await expect(article).toHaveAttribute('href', sample.externalUrl!)
        await expect(article).toHaveAttribute('target', '_blank')
        await expect(article).toHaveAttribute('rel', /noopener/)
        await expect(article).toHaveAttribute('rel', /noreferrer/)
      } else {
        await expect(article).toHaveAttribute('href', `/article/${sample.runtimeId}`)
      }
      await screenshot(page, testInfo, `column-${sample.columnAlias}`)
    }
  })

  test('resource-bearing and rich INTERNAL samples render through public Runtime resources', async ({ page, request }, testInfo) => {
    test.setTimeout(120_000)
    const candidates = uniqueSamples([
      evidence.riskSamples.resourceRichInternal,
      evidence.riskSamples.bodyImageInternal,
      evidence.riskSamples.attachmentInternal,
      evidence.riskSamples.longBodyInternal,
    ])
    for (const sample of candidates) {
      expect(sample.articleType).toBe('INTERNAL')
      await gotoReviewPage(page, `/article/${sample.runtimeId}`)
      await expect(page.getByTestId('public-article-title')).toHaveText(sample.title)
      const body = page.getByTestId('public-article-body')
      await expect(body).toBeVisible()
      if (sample.bodyLength > 0) {
        expect((await body.evaluate((element) => element.innerHTML)).trim().length).toBeGreaterThan(0)
      }

      for (const image of sample.bodyImages) {
        const response = await request.get(`/api/public/resources/${image.runtimeId}/content`)
        expect(response.ok()).toBeTruthy()
        expect((await response.body()).byteLength).toBe(image.sizeBytes)
        await expect(body.locator(`img[src="/api/public/resources/${image.runtimeId}/content"]`)).toBeVisible()
      }

      for (const attachment of sample.attachments) {
        const link = page.getByTestId(`public-attachment-${attachment.runtimeId}`)
        await expect(link).toBeVisible()
        await expect(link).toHaveAttribute('href', `/api/public/resources/${attachment.runtimeId}/attachment`)
        const response = await request.get(`/api/public/resources/${attachment.runtimeId}/attachment`)
        expect(response.ok()).toBeTruthy()
        expect((await response.body()).byteLength).toBe(attachment.sizeBytes)
      }
      await screenshot(page, testInfo, `internal-${safe(sample.legacyKey)}`)
    }
  })

  test('oldest/newest boundary samples remain publicly addressable with accepted metadata', async ({ page, request }, testInfo) => {
    test.setTimeout(90_000)
    for (const [role, sample] of [
      ['oldest', evidence.riskSamples.oldestPublished],
      ['newest', evidence.riskSamples.newestPublished],
    ] as const) {
      const response = await request.get(`/api/public/articles/${sample.runtimeId}`)
      expect(response.ok()).toBeTruthy()
      const runtime = await response.json() as { title: string; publishDate: string | null; articleType: string; externalUrl: string | null }
      expect(runtime.title).toBe(sample.title)
      expect(runtime.publishDate).toBe(sample.publishDate)
      expect(runtime.articleType).toBe(sample.articleType)
      expect(runtime.externalUrl).toBe(sample.externalUrl)

      await gotoReviewPage(page, `/column/${sample.columnAlias}`)
      await expect(page.getByTestId('column-page')).toBeVisible()
      await screenshot(page, testInfo, `boundary-${role}-${sample.columnAlias}`)
    }
  })

  test('Admin can locate and open imported INTERNAL and EXTERNAL_LINK Articles without changing migration identity', async ({ page }, testInfo) => {
    test.setTimeout(90_000)
    const internal = evidence.riskSamples.resourceRichInternal
    const external = evidence.riskSamples.externalLink

    await verifyAdminArticle(page, internal)
    await screenshot(page, testInfo, `admin-internal-${safe(internal.legacyKey)}`)
    await closeAdminDialog(page)

    await verifyAdminArticle(page, external)
    await expect(page.getByTestId('article-external-url')).toHaveValue(external.externalUrl!)
    await screenshot(page, testInfo, `admin-external-${safe(external.legacyKey)}`)
    await closeAdminDialog(page)
  })
})

async function verifyAdminArticle(page: Page, sample: ReviewArticle) {
  await gotoReviewPage(page, '/admin/articles')
  await expect(page.getByRole('heading', { name: '文章管理' })).toBeVisible()
  await page.getByTestId('article-filter-keyword').fill(sample.title)
  const row = page.getByRole('row').filter({ hasText: sample.title })
  await expect(row.getByRole('cell', { name: sample.title, exact: true })).toBeVisible()
  await expect(row).toContainText('已发布')
  await page.getByTestId(`edit-article-${sample.runtimeId}`).click()
  const dialog = page.getByRole('dialog', { name: '编辑文章' })
  await expect(dialog).toBeVisible()
  await expect(page.getByTestId('article-title')).toHaveValue(sample.title)
  await expect(page.getByTestId('article-source')).toHaveValue(sample.source)
  if (sample.articleType === 'INTERNAL') {
    await expect(page.getByTestId('article-body-editor')).toBeVisible()
  } else {
    await expect(page.getByTestId('article-type')).toContainText('外链文章')
  }
}

async function closeAdminDialog(page: Page) {
  const dialog = page.getByRole('dialog', { name: '编辑文章' })
  await dialog.getByRole('button', { name: '取消' }).click()
  await expect(dialog).toBeHidden()
}

async function gotoReviewPage(page: Page, url: string) {
  // Accepted Legacy content may intentionally retain non-blocking external absolute
  // resources. Review the Runtime application as soon as its DOM is available;
  // locator assertions below prove the target UI instead of waiting for every
  // third-party subresource to finish loading.
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 45_000 })
}

function uniqueSamples(samples: Array<ReviewArticle | null>): ReviewArticle[] {
  const values = new Map<number, ReviewArticle>()
  for (const sample of samples) if (sample) values.set(sample.runtimeId, sample)
  return [...values.values()]
}

async function screenshot(page: Page, testInfo: TestInfo, name: string) {
  await page.screenshot({ path: testInfo.outputPath(`${name}.png`), fullPage: true })
}

function safe(value: string) {
  return value.replace(/[^a-zA-Z0-9_-]+/g, '-')
}
