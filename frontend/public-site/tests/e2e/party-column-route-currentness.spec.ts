import { expect, test, type APIRequestContext } from '@playwright/test'

type Column = { id: number; alias: string }
type Article = { id: number; title: string }

async function partyColumn(request: APIRequestContext, alias: string): Promise<Column> {
  const response = await request.get('/api/admin/columns')
  expect(response.ok()).toBeTruthy()
  const columns = await response.json() as Column[]
  const item = columns.find(column => column.alias === alias)
  expect(item, `缺少党建预置栏目 ${alias}`).toBeTruthy()
  return item!
}

async function createArticle(
  request: APIRequestContext,
  columnId: number,
  title: string,
  sortOrder: number,
): Promise<Article> {
  const response = await request.post('/api/admin/articles', {
    data: {
      columnId,
      title,
      bodyHtml: `<p>${title} 正文</p>`,
      source: '中心党建竞态验证',
      articleType: 'INTERNAL',
      externalUrl: null,
      publishDate: '2026-09-03',
      pinned: false,
      recommended: false,
      sortOrder,
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

test('EU-29：较慢的旧分页响应不得覆盖浏览器返回后的当前栏目状态', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const column = await partyColumn(request, 'party-rules')
  const created: Article[] = []

  try {
    for (let index = 0; index < 11; index += 1) {
      created.push(await createArticle(request, column.id, `党建竞态-${index}-${suffix}`, -2000 + index))
    }

    await page.route('**/api/public/articles?*', async route => {
      const url = new URL(route.request().url())
      const isDelayedSecondPage = url.searchParams.get('columnId') === String(column.id)
        && url.searchParams.get('page') === '1'
      if (isDelayedSecondPage) {
        await new Promise(resolve => setTimeout(resolve, 350))
      }
      await route.continue()
    })

    await page.goto('/party/column/party-rules')
    await expect(page.getByTestId(`party-column-article-${created[10].id}`)).toBeVisible()

    const delayedResponse = page.waitForResponse(response => {
      const url = new URL(response.url())
      return url.pathname === '/api/public/articles'
        && url.searchParams.get('columnId') === String(column.id)
        && url.searchParams.get('page') === '1'
    })

    await page.getByRole('button', { name: '下一页' }).click()
    await expect(page).toHaveURL(/\/party\/column\/party-rules\?page=1$/)
    await page.goBack()
    await expect(page).toHaveURL(/\/party\/column\/party-rules$/)
    await expect(page.getByTestId(`party-column-article-${created[10].id}`)).toBeVisible()

    await delayedResponse
    await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => resolve())))

    await expect(page.getByTestId(`party-column-article-${created[10].id}`)).toBeVisible()
    await expect(page.getByTestId(`party-column-article-${created[0].id}`)).toHaveCount(0)
  } finally {
    for (const article of created) {
      expect((await request.post(`/api/admin/articles/${article.id}/withdraw`)).ok()).toBeTruthy()
    }
  }
})
