import { expect, test, type APIRequestContext } from '@playwright/test'

type Column = { id: number; parentId: number | null; alias: string; name: string; preset: boolean }
type Article = { id: number; title: string }

async function columns(request: APIRequestContext): Promise<Column[]> {
  const response = await request.get('/api/admin/columns')
  expect(response.ok()).toBeTruthy()
  return response.json() as Promise<Column[]>
}

async function partyColumn(request: APIRequestContext, alias: string): Promise<Column> {
  const item = (await columns(request)).find(column => column.alias === alias)
  expect(item, `缺少党建预置栏目 ${alias}`).toBeTruthy()
  return item!
}

async function createArticle(
  request: APIRequestContext,
  input: { columnId: number; title: string; articleType?: 'INTERNAL' | 'EXTERNAL_LINK'; externalUrl?: string; sortOrder?: number },
): Promise<Article> {
  const articleType = input.articleType ?? 'INTERNAL'
  const response = await request.post('/api/admin/articles', {
    data: {
      columnId: input.columnId,
      title: input.title,
      bodyHtml: articleType === 'INTERNAL' ? `<p>${input.title} 正文</p>` : '',
      source: articleType === 'INTERNAL' ? '中心党建测试' : '外部权威来源',
      articleType,
      externalUrl: input.externalUrl ?? null,
      publishDate: '2026-09-03',
      pinned: false,
      recommended: false,
      sortOrder: input.sortOrder ?? 0,
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

test('EU-27：党建栏目树与首页轮播容器来自 Fresh Flyway 基线', async ({ request }) => {
  const allColumns = await columns(request)
  const parent = allColumns.find(item => item.alias === 'party-building')
  expect(parent).toMatchObject({ name: '中心党建', parentId: null, preset: true })

  for (const [alias, name] of [
    ['party-voice', '高层声音'],
    ['party-work', '工作动态'],
    ['party-rules', '党规党章'],
    ['party-study', '理论学习'],
  ] as const) {
    const child = allColumns.find(item => item.alias === alias)
    expect(child).toMatchObject({ name, parentId: parent!.id, preset: true })
  }

  const listsResponse = await request.get('/api/admin/lists')
  expect(listsResponse.ok()).toBeTruthy()
  const lists = await listsResponse.json() as Array<{ id: number; code: string; imagePolicy: string; preset: boolean }>
  expect(lists.find(item => item.code === 'PARTY_HOME_CAROUSEL')).toMatchObject({ imagePolicy: 'REQUIRED', preset: true })
})

test('EU-27：首页按业务 scope 加载四栏目，顶部轮播复用 CmsList', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const voice = await partyColumn(request, 'party-voice')
  const work = await partyColumn(request, 'party-work')
  const voiceArticle = await createArticle(request, { columnId: voice.id, title: `高层声音-${suffix}`, sortOrder: 500 })
  const workArticle = await createArticle(request, { columnId: work.id, title: `工作动态-${suffix}`, sortOrder: 500 })
  const externalUrl = `https://example.com/party/${suffix}`
  const externalArticle = await createArticle(request, {
    columnId: work.id,
    title: `外链工作动态-${suffix}`,
    articleType: 'EXTERNAL_LINK',
    externalUrl,
    sortOrder: 600,
  })

  const listsResponse = await request.get('/api/admin/lists')
  const lists = await listsResponse.json() as Array<{ id: number; code: string }>
  const carousel = lists.find(item => item.code === 'PARTY_HOME_CAROUSEL')
  expect(carousel).toBeTruthy()
  const carouselResponse = await request.post(`/api/admin/lists/${carousel!.id}/items`, {
    data: {
      title: `党建轮播-${suffix}`,
      subtitle: null,
      url: `https://example.com/carousel/${suffix}`,
      imagePath: '/static/health/baseline.png',
      openMode: 'NEW_WINDOW',
      sortOrder: 10,
      enabled: true,
      extraJson: null,
    },
  })
  expect(carouselResponse.ok()).toBeTruthy()
  const carouselItem = await carouselResponse.json() as { id: number }

  await page.goto('/party/')
  await expect(page.getByTestId('party-home-carousel')).toBeVisible()
  await expect(page.getByTestId(`party-carousel-item-${carouselItem.id}`)).toContainText(`党建轮播-${suffix}`)
  await expect(page.getByTestId('party-section-party-voice').getByText(voiceArticle.title, { exact: true })).toBeVisible()
  await expect(page.getByTestId('party-section-party-work').getByText(workArticle.title, { exact: true })).toBeVisible()
  const external = page.getByTestId('party-section-party-work').getByText(externalArticle.title, { exact: true })
  await expect(external).toHaveAttribute('href', externalUrl)
  await expect(external).toHaveAttribute('target', '_blank')
  await expect(page.getByRole('heading', { name: '学习园地', exact: true })).toBeVisible()
  await expect(page.getByTestId('party-section-party-rules')).toBeVisible()
  await expect(page.getByTestId('party-section-party-study')).toBeVisible()
})

test('EU-27：党建栏目使用真实 scoped pagination 并支持直接刷新', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const column = await partyColumn(request, 'party-rules')
  const created: Article[] = []
  for (let index = 0; index < 11; index += 1) {
    created.push(await createArticle(request, {
      columnId: column.id,
      title: `党规分页-${index}-${suffix}`,
      sortOrder: 1000 + index,
    }))
  }

  await page.goto('/party/column/party-rules')
  await expect(page.getByRole('heading', { name: '党规党章', exact: true })).toBeVisible()
  await expect(page.getByTestId(`party-column-article-${created[10].id}`)).toBeVisible()
  await expect(page.getByTestId(`party-column-article-${created[0].id}`)).toHaveCount(0)
  await page.getByRole('button', { name: '下一页' }).click()
  await expect(page).toHaveURL(/\/party\/column\/party-rules\?page=1$/)
  await expect(page.getByTestId(`party-column-article-${created[0].id}`)).toBeVisible()
  await page.reload()
  await expect(page.getByTestId(`party-column-article-${created[0].id}`)).toBeVisible()
})

test('EU-27：Party 详情只接受党建站内文章，非党建文章被隔离', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const party = await partyColumn(request, 'party-study')
  const internal = await createArticle(request, { columnId: party.id, title: `理论学习详情-${suffix}` })

  await page.goto(`/party/article/${internal.id}`)
  await expect(page.getByTestId('party-article-title')).toHaveText(internal.title)
  await expect(page.getByTestId('party-article-body')).toContainText(`${internal.title} 正文`)
  await page.reload()
  await expect(page.getByTestId('party-article-title')).toHaveText(internal.title)

  const notice = (await columns(request)).find(item => item.alias === 'notice')
  expect(notice).toBeTruthy()
  const mainArticle = await createArticle(request, { columnId: notice!.id, title: `主站文章-${suffix}` })
  await page.goto(`/party/article/${mainArticle.id}`)
  await expect(page.getByTestId('party-article-unavailable')).toHaveText('文章不可用或不存在')
  await expect(page.getByTestId('party-article-page')).toHaveCount(0)
})

test('EU-27：Party Shell 在内容路由间保持隔离且移动端不产生 1200px 横向溢出', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/party/')
  await expect(page.getByTestId('party-building-header')).toBeVisible()
  await page.goto('/party/column/party-work')
  await expect(page.getByTestId('party-building-header')).toBeVisible()
  await expect(page.locator('.site-header')).toHaveCount(0)
  const metrics = await page.evaluate(() => ({ viewport: innerWidth, documentWidth: document.documentElement.scrollWidth }))
  expect(metrics.viewport).toBe(390)
  expect(metrics.documentWidth).toBeLessThanOrEqual(390)

  await page.goto('/')
  await expect(page.getByTestId('public-content')).toBeVisible()
  await expect(page.getByTestId('party-building-site')).toHaveCount(0)
})
