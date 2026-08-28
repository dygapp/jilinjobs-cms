import { expect, test, type APIRequestContext } from '@playwright/test'

type Article = { id: number; title: string }

async function createArticle(
  request: APIRequestContext,
  input: { columnId: number; title: string; articleType: 'INTERNAL' | 'EXTERNAL_LINK'; externalUrl?: string },
): Promise<Article> {
  const response = await request.post('/api/admin/articles', {
    data: {
      columnId: input.columnId,
      title: input.title,
      bodyHtml: input.articleType === 'INTERNAL' ? `<p>${input.title} 正文</p>` : '',
      source: input.articleType === 'INTERNAL' ? '本站发布' : '外部招聘信息源',
      articleType: input.articleType,
      externalUrl: input.externalUrl ?? null,
      publishDate: '2026-08-28',
      pinned: false,
      recommended: false,
      sortOrder: 100,
      coverResourceId: null,
      bodyImageResourceIds: [],
      attachmentResourceIds: [],
    },
  })
  expect(response.ok()).toBeTruthy()
  const article = await response.json() as Article
  expect((await request.post(`/api/admin/articles/${article.id}/publish`)).ok()).toBeTruthy()
  return article
}

test('招聘公告栏目允许站内文章，首页招聘公告只展示外链来源', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const columnsResponse = await request.get('/api/admin/columns')
  expect(columnsResponse.ok()).toBeTruthy()
  const columns = await columnsResponse.json() as Array<{ id: number; alias: string }>
  const column = columns.find(item => item.alias === 'recruitment-announcement')
  expect(column).toBeTruthy()

  const internal = await createArticle(request, {
    columnId: column!.id,
    title: `站内招聘公告-${suffix}`,
    articleType: 'INTERNAL',
  })
  const externalUrl = `https://example.com/recruitment/${suffix}`
  const external = await createArticle(request, {
    columnId: column!.id,
    title: `外链招聘公告-${suffix}`,
    articleType: 'EXTERNAL_LINK',
    externalUrl,
  })

  await page.goto('/column/recruitment-announcement')
  const internalLink = page.getByTestId(`column-article-${internal.id}`)
  const externalLink = page.getByTestId(`column-article-${external.id}`)
  await expect(internalLink).toHaveAttribute('href', `/article/${internal.id}`)
  await expect(internalLink).not.toHaveAttribute('target', '_blank')
  await expect(externalLink).toHaveAttribute('href', externalUrl)
  await expect(externalLink).toHaveAttribute('target', '_blank')

  await page.goto('/')
  const recruitment = page.locator('.recruitment-panel')
  await expect(recruitment.getByText(external.title, { exact: true })).toBeVisible()
  await expect(recruitment.getByText(internal.title, { exact: true })).toHaveCount(0)
})
