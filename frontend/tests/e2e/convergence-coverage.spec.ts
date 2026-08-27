import { expect, test, type APIRequestContext } from '@playwright/test'

type CreatedArticle = {
  id: number
  title: string
}

async function createColumn(request: APIRequestContext, name: string): Promise<{ id: number }> {
  const response = await request.post('/api/admin/columns', {
    data: {
      parentId: null,
      name,
      sortOrder: 10,
      enabled: true,
    },
  })
  expect(response.ok()).toBeTruthy()
  return response.json() as Promise<{ id: number }>
}

async function createAndPublishArticle(
  request: APIRequestContext,
  input: {
    columnId: number
    title: string
    pinned?: boolean
    recommended?: boolean
    sortOrder?: number
  },
): Promise<CreatedArticle> {
  const createResponse = await request.post('/api/admin/articles', {
    data: {
      columnId: input.columnId,
      title: input.title,
      bodyHtml: `<p>${input.title} 正文</p>`,
      source: 'Feature-wide convergence verification',
      publishDate: '2026-08-24',
      pinned: input.pinned ?? false,
      recommended: input.recommended ?? false,
      sortOrder: input.sortOrder ?? 0,
      coverResourceId: null,
      bodyImageResourceIds: [],
      attachmentResourceIds: [],
    },
  })
  expect(createResponse.ok()).toBeTruthy()
  const article = await createResponse.json() as CreatedArticle

  const publishResponse = await request.post(`/api/admin/articles/${article.id}/publish`)
  expect(publishResponse.ok()).toBeTruthy()
  return article
}

test('Feature-wide closure：栏目内容超过单页容量时可以完整分页浏览', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const columnName = `分页验证栏目-${suffix}`
  const column = await createColumn(request, columnName)
  const articles: CreatedArticle[] = []

  for (let index = 0; index < 11; index += 1) {
    articles.push(await createAndPublishArticle(request, {
      columnId: column.id,
      title: `分页文章-${index}-${suffix}`,
      sortOrder: index,
    }))
  }

  const highest = articles[10]
  const lowest = articles[0]

  await page.goto(`/columns/${column.id}`)
  await expect(page.getByRole('heading', { name: columnName, exact: true })).toBeVisible()
  await expect(page.getByTestId(`column-article-${highest.id}`)).toBeVisible()
  await expect(page.getByTestId(`column-article-${lowest.id}`)).toHaveCount(0)
  await expect(page.getByText('第 1 页', { exact: true })).toBeVisible()

  await page.getByRole('button', { name: '下一页' }).click()
  await expect(page).toHaveURL(new RegExp(`/columns/${column.id}\\?page=1$`))
  await expect(page.getByText('第 2 页', { exact: true })).toBeVisible()
  await expect(page.getByTestId(`column-article-${lowest.id}`)).toBeVisible()
  await expect(page.getByTestId(`column-article-${highest.id}`)).toHaveCount(0)

  await page.getByRole('button', { name: '上一页' }).click()
  await expect(page).toHaveURL(new RegExp(`/columns/${column.id}$`))
  await expect(page.getByTestId(`column-article-${highest.id}`)).toBeVisible()
})

test('Feature-wide closure：首页按置顶推荐和展示顺序组织多篇已发布内容', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const columnName = `首页排序栏目-${suffix}`
  const column = await createColumn(request, columnName)

  const low = await createAndPublishArticle(request, {
    columnId: column.id,
    title: `普通低排序-${suffix}`,
    sortOrder: 10,
  })
  const recommended = await createAndPublishArticle(request, {
    columnId: column.id,
    title: `推荐文章-${suffix}`,
    recommended: true,
    sortOrder: 0,
  })
  const high = await createAndPublishArticle(request, {
    columnId: column.id,
    title: `普通高排序-${suffix}`,
    sortOrder: 200,
  })
  const pinned = await createAndPublishArticle(request, {
    columnId: column.id,
    title: `置顶文章-${suffix}`,
    pinned: true,
    sortOrder: 0,
  })

  await page.goto('/')
  const group = page.locator('.news-column').filter({ hasText: columnName })
  await expect(group).toBeVisible()

  const orderedTitles = await group.locator('li a').allTextContents()
  expect(orderedTitles).toEqual([
    pinned.title,
    recommended.title,
    high.title,
    low.title,
  ])
})

test('Feature-wide closure：SERVICE 与 SITE 导航进入公开首页对应分区', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const serviceFirst = `服务入口A-${suffix}`
  const serviceSecond = `服务入口B-${suffix}`
  const siteCategory = `省级平台-${suffix}`
  const siteName = `站点导航-${suffix}`

  const serviceRows = [
    { name: serviceSecond, sortOrder: 20, targetUrl: 'https://example.com/service-b' },
    { name: serviceFirst, sortOrder: 10, targetUrl: 'https://example.com/service-a' },
  ]
  for (const row of serviceRows) {
    const response = await request.post('/api/admin/navigations', {
      data: {
        name: row.name,
        position: 'SERVICE',
        category: null,
        targetType: 'LINK',
        targetColumnId: null,
        targetUrl: row.targetUrl,
        sortOrder: row.sortOrder,
        enabled: true,
      },
    })
    expect(response.ok()).toBeTruthy()
  }

  const siteResponse = await request.post('/api/admin/navigations', {
    data: {
      name: siteName,
      position: 'SITE',
      category: siteCategory,
      targetType: 'LINK',
      targetColumnId: null,
      targetUrl: 'https://example.com/site',
      sortOrder: 10,
      enabled: true,
    },
  })
  expect(siteResponse.ok()).toBeTruthy()

  await page.goto('/')

  const serviceSection = page.locator('.service-panel')
  await expect(serviceSection.getByRole('heading', { name: '公共服务', exact: true })).toBeVisible()
  const serviceFirstLink = serviceSection.getByRole('link', { name: serviceFirst, exact: true })
  const serviceSecondLink = serviceSection.getByRole('link', { name: serviceSecond, exact: true })
  await expect(serviceFirstLink).toHaveAttribute('href', 'https://example.com/service-a')
  await expect(serviceSecondLink).toHaveAttribute('href', 'https://example.com/service-b')
  const serviceTexts = await serviceSection.locator('a').allTextContents()
  expect(serviceTexts.indexOf(serviceFirst)).toBeLessThan(serviceTexts.indexOf(serviceSecond))

  const siteSection = page.locator('.site-navigation')
  await expect(siteSection.getByRole('heading', { name: '网站导航', exact: true })).toBeVisible()
  await expect(siteSection.getByText(siteCategory, { exact: true })).toBeVisible()
  await expect(siteSection.getByRole('link', { name: siteName, exact: true })).toHaveAttribute('href', 'https://example.com/site')
})
