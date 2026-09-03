import { expect, test, type APIRequestContext } from '@playwright/test'

type Column = { id: number; alias: string }
type Article = { id: number; title: string }
type CarouselItem = { id: number; title: string; url: string | null }

async function partyColumns(request: APIRequestContext) {
  const response = await request.get('/api/admin/columns')
  expect(response.ok()).toBeTruthy()
  const columns = await response.json() as Column[]
  return Object.fromEntries(columns.filter(item => item.alias.startsWith('party-')).map(item => [item.alias, item])) as Record<string, Column>
}

async function createArticle(request: APIRequestContext, columnId: number, title: string): Promise<Article> {
  const response = await request.post('/api/admin/articles', {
    data: {
      columnId,
      title,
      bodyHtml: `<p>${title} 正文。用于中心党建正式页面视觉复核。</p><p>坚持以服务毕业生就业工作高质量发展为目标，持续加强理论学习和基层党建工作。</p>`,
      source: '吉林省高等学校毕业生就业指导中心',
      articleType: 'INTERNAL',
      externalUrl: null,
      publishDate: '2026-09-03',
      pinned: false,
      recommended: false,
      sortOrder: 900,
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

async function seedVisualContent(request: APIRequestContext, suffix: string) {
  const columns = await partyColumns(request)
  const articles: Record<string, Article> = {}
  for (const [alias, title] of [
    ['party-voice', '高层声音'],
    ['party-work', '工作动态'],
    ['party-rules', '党规党章'],
    ['party-study', '理论学习'],
  ] as const) {
    articles[alias] = await createArticle(request, columns[alias].id, `${title}代表性内容-${suffix}`)
  }

  const listsResponse = await request.get('/api/admin/lists')
  expect(listsResponse.ok()).toBeTruthy()
  const lists = await listsResponse.json() as Array<{ id: number; code: string }>
  const carousel = lists.find(item => item.code === 'PARTY_HOME_CAROUSEL')
  expect(carousel).toBeTruthy()
  const aliases = ['party-voice', 'party-work', 'party-rules', 'party-study']
  const carouselItems: CarouselItem[] = []
  for (let index = 0; index < 4; index += 1) {
    const response = await request.post(`/api/admin/lists/${carousel!.id}/items`, {
      data: {
        title: `党建轮播代表内容 ${index + 1}-${suffix}`,
        subtitle: null,
        url: `/party/column/${aliases[index]}`,
        imagePath: '/static/health/baseline.png',
        openMode: 'DEFAULT',
        sortOrder: index * 10,
        enabled: true,
        extraJson: null,
      },
    })
    expect(response.ok()).toBeTruthy()
    carouselItems.push(await response.json() as CarouselItem)
  }
  return { articles, carouselItems }
}

test('EU-28：党建稳定原站视觉资源可访问且 Header / Nav / Footer 使用正式视觉基线', async ({ page, request }) => {
  for (const path of [
    '/static/party-building/party-header-banner.webp',
    '/static/party-building/ic-title-yellow.png',
    '/static/party-building/section-marker.png',
    '/static/footer/public-security-record.png',
  ]) {
    const response = await request.get(path)
    expect(response.ok(), `${path} 应由版本化静态基线提供`).toBeTruthy()
  }

  await page.setViewportSize({ width: 1440, height: 1000 })
  await page.goto('/party/')
  const banner = page.locator('.party-banner')
  await expect(banner).toHaveCSS('background-image', /party-header-banner\.webp/)
  await expect(banner).toHaveCSS('height', '320px')
  await expect(page.locator('.party-navigation')).toHaveCSS('background-color', 'rgb(208, 0, 35)')
  const firstNavigation = page.locator('.party-nav-root > li').first()
  await expect(firstNavigation).toHaveCSS('width', '120px')
  await expect(firstNavigation.locator('a, span').first()).toHaveCSS('min-height', '60px')
  await expect(page.locator('.party-red-tab').first()).toHaveCSS('min-height', '40px')
  await expect(page.locator('.party-voice-list li').first()).toHaveCSS('min-height', '20px')
  await expect(page.locator('.party-work-list')).toHaveCSS('display', 'block')
  await expect(page.locator('.party-footer')).toHaveCSS('background-color', 'rgb(173, 0, 29)')
  await expect(page.locator('.party-footer-inner')).toHaveCSS('text-align', 'left')
  await expect(page.locator('.party-public-security-record img')).toBeVisible()
  await expect(page.locator('.party-emblem')).toHaveCount(0)
  await expect(page.locator('.party-hero')).toHaveCount(0)
})

test('EU-28：党建 Desktop / Mobile 当前视觉截图形成 Current Evidence 且轮播可跳转', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const { articles, carouselItems } = await seedVisualContent(request, suffix)

  await page.setViewportSize({ width: 1440, height: 1000 })
  await page.goto('/party/')
  await expect(page.getByText(articles['party-voice'].title, { exact: true })).toBeVisible()
  const firstSeededCarousel = page.getByTestId(`party-carousel-item-${carouselItems[0].id}`).locator('a')
  await expect(firstSeededCarousel).toHaveAttribute('href', '/party/column/party-voice')
  await firstSeededCarousel.click({ force: true })
  await expect(page).toHaveURL(/\/party\/column\/party-voice$/)
  await page.goto('/party/')
  await page.waitForTimeout(200)
  await testInfo.attach('party-home-desktop-current.png', {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png',
  })

  await page.goto('/party/column/party-work')
  await expect(page.getByRole('heading', { name: '工作动态', exact: true })).toBeVisible()
  await testInfo.attach('party-column-desktop-current.png', {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png',
  })

  await page.goto(`/party/article/${articles['party-study'].id}`)
  await expect(page.getByTestId('party-article-title')).toBeVisible()
  await testInfo.attach('party-article-desktop-current.png', {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png',
  })

  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/party/')
  const dimensions = await page.evaluate(() => ({ viewport: innerWidth, documentWidth: document.documentElement.scrollWidth }))
  expect(dimensions.documentWidth).toBeLessThanOrEqual(dimensions.viewport)
  await testInfo.attach('party-home-mobile-current.png', {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png',
  })
})
