import { expect, test, type APIRequestContext } from '@playwright/test'

async function partyWorkColumn(request: APIRequestContext) {
  const response = await request.get('/api/admin/columns')
  expect(response.ok()).toBeTruthy()
  const columns = await response.json() as Array<{ id:number; alias:string }>
  const column = columns.find(item => item.alias === 'party-work')
  expect(column).toBeTruthy()
  return column!
}

async function createPublishedArticle(request: APIRequestContext, columnId: number, title: string, sortOrder: number) {
  const response = await request.post('/api/admin/articles', {
    data: {
      columnId,
      title,
      bodyHtml: `<p>${title} 正文</p>`,
      source: 'Party Human Review Regression',
      articleType: 'INTERNAL',
      externalUrl: null,
      publishDate: '2026-09-05',
      pinned: false,
      sortOrder,
      coverResourceId: null,
      bodyImageResourceIds: [],
      attachmentResourceIds: [],
    },
  })
  expect(response.ok()).toBeTruthy()
  const article = await response.json() as { id:number; title:string }
  expect((await request.post(`/api/admin/articles/${article.id}/publish`)).ok()).toBeTruthy()
  return article
}

test('EU-30 Human Review：党建栏目与详情面包屑一致且分页下拉使用红色主题', async ({ page, request }, testInfo) => {
  const column = await partyWorkColumn(request)
  const suffix = `${Date.now()}-${testInfo.retry}`
  const created:Array<{id:number;title:string}> = []

  try {
    for (let index = 0; index < 11; index += 1) {
      created.push(await createPublishedArticle(request, column.id, `党建分页主题-${index}-${suffix}`, 2000 + index))
    }

    await page.goto('/party/column/party-work')
    await expect(page.getByRole('heading', { name: '工作动态', exact: true })).toBeVisible()
    const columnBreadcrumb = page.locator('.shared-column-breadcrumb')
    const columnMetrics = await columnBreadcrumb.evaluate(element => {
      const style = getComputedStyle(element)
      return { minHeight: style.minHeight, fontSize: style.fontSize, color: style.color, gap: style.gap }
    })

    const sizeTrigger = page.getByTestId('party-column-page-size-trigger')
    await expect(sizeTrigger).toBeVisible()
    await sizeTrigger.click()
    const sizeMenu = page.getByTestId('party-column-page-size-menu')
    await expect(sizeMenu).toBeVisible()
    const selected = page.getByTestId('party-column-page-size-option-10')
    await expect(selected).toHaveCSS('background-color', 'rgb(208, 0, 35)')
    await expect(selected).toHaveCSS('color', 'rgb(255, 255, 255)')
    await expect(sizeTrigger).toHaveCSS('border-color', 'rgb(208, 0, 35)')
    await page.getByTestId('party-column-page-size-option-20').click()
    await expect(page).toHaveURL(/\/party\/column\/party-work\?size=20$/)

    await page.goto(`/party/article/${created[0].id}`)
    const detailBreadcrumb = page.locator('.party-breadcrumb')
    await expect(detailBreadcrumb).toBeVisible()
    const detailMetrics = await detailBreadcrumb.evaluate(element => {
      const style = getComputedStyle(element)
      return { minHeight: style.minHeight, fontSize: style.fontSize, color: style.color, gap: style.gap }
    })
    expect(detailMetrics).toEqual(columnMetrics)
    await expect(detailBreadcrumb).toContainText('中心党建')
    await expect(detailBreadcrumb).toContainText('工作动态')
    await expect(detailBreadcrumb).toContainText('详情')
  } finally {
    for (const article of created) {
      await request.post(`/api/admin/articles/${article.id}/withdraw`)
    }
  }
})
