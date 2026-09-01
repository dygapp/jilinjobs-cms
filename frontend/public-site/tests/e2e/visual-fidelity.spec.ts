import { expect, test } from '@playwright/test'

const BASELINE_ROOTS = ['网站首页', '中心党建', '招聘信息', '业务指南', '政策法规', '就业指导', '典型事迹', '预决算公开', '关于我们']

test('视觉基线：原站关键静态资源与蓝色公共框架可用', async ({ page, request }) => {
  for (const resource of [
    '/static/brand/smartedu-logo-icon.png',
    '/static/brand/smartedu-logo-text.png',
    '/static/home/header-banner.png',
    '/static/home/carousel-01.jpg',
    '/static/home/recruitment-campaign.png',
    '/static/home/ncss-logo.png',
    '/static/icons/guide-01.png',
    '/static/icons/top-nav-01.png',
    '/static/icons/list-item.png',
  ]) {
    const response = await request.get(resource)
    expect(response.ok(), `${resource} 应来自版本化静态资源包`).toBeTruthy()
    expect((await response.body()).length).toBeGreaterThan(100)
  }

  const navigationResponse = await request.get('/api/public/navigations')
  expect(navigationResponse.ok()).toBeTruthy()
  const navigation = await navigationResponse.json() as Array<{ parentId: number | null; position: string; name: string }>
  const rootNames = navigation.filter(item => item.position === 'MAIN' && item.parentId == null).map(item => item.name)
  for (const name of BASELINE_ROOTS) expect(rootNames).toContain(name)

  await page.goto('/')
  await expect(page.locator('.platform-logo-icon')).toHaveAttribute('src', '/static/brand/smartedu-logo-icon.png')
  await expect(page.locator('.platform-logo-text')).toHaveAttribute('src', '/static/brand/smartedu-logo-text.png')
  await expect(page.locator('.site-hero')).toHaveCSS('background-image', /header-banner\.png/)
  await expect(page.locator('.home-carousel img')).toHaveAttribute('src', '/static/home/carousel-01.jpg')
  await expect(page.locator('.home-promo-banner img')).toHaveAttribute('src', '/static/home/recruitment-campaign.png')
  const shortcutImages = page.locator('.service-shortcuts img')
  expect(await shortcutImages.count()).toBeGreaterThanOrEqual(6)
  for (const image of await shortcutImages.all()) await expect(image).toHaveAttribute('src', /^\/static\//)

  expect(await page.locator('.platform-bar').evaluate(el => getComputedStyle(el).backgroundColor)).toBe('rgb(3, 86, 202)')
  expect(await page.locator('.site-nav').evaluate(el => getComputedStyle(el).backgroundColor)).toBe('rgb(0, 92, 212)')
  expect(await page.locator('.home-main').evaluate(el => getComputedStyle(el).backgroundColor)).toBe('rgb(245, 248, 252)')
})

test('视觉基线：桌面首页维持原站三列首屏与关键图片区块', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 1000 })
  await page.goto('/')

  const primary = page.locator('.home-primary-row')
  await expect(primary).toBeVisible()
  const columns = await primary.locator(':scope > *').evaluateAll(elements => elements.map(el => Math.round(el.getBoundingClientRect().width)))
  expect(columns).toHaveLength(3)
  expect(columns[0]).toBeGreaterThanOrEqual(390)
  expect(columns[2]).toBeGreaterThanOrEqual(240)

  await expect(page.getByRole('heading', { name: '通知公告', exact: true })).toBeVisible()
  await expect(page.getByRole('heading', { name: '就业动态', exact: true })).toBeVisible()
  await expect(page.getByRole('heading', { name: '快速导航', exact: true })).toBeVisible()
  await expect(page.getByRole('heading', { name: '最新招聘', exact: true })).toBeVisible()
  await expect(page.getByRole('heading', { name: '招聘公告', exact: true })).toBeVisible()
  await expect(page.getByRole('heading', { name: '网站导航', exact: true })).toBeVisible()

  const overflow = await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth)
  expect(overflow).toBeFalsy()
})

test('视觉基线：栏目、文章、固定页与业务指南匹配原站内容页主结构', async ({ page, request }, testInfo) => {
  const columnsResponse = await request.get('/api/admin/columns')
  expect(columnsResponse.ok()).toBeTruthy()
  const columns = await columnsResponse.json() as Array<{ id: number; alias: string }>
  const notice = columns.find(item => item.alias === 'notice')
  expect(notice).toBeTruthy()

  const created: Array<{ id: number }> = []
  for (let index = 0; index < 11; index += 1) {
    const response = await request.post('/api/admin/articles', {
      data: {
        columnId: notice!.id,
        title: `页面视觉取证示例文章 ${String(index + 1).padStart(2, '0')}`,
        bodyHtml: '<p>这是用于页面视觉取证的代表性正文。正文用于观察标题、元数据、段落宽度、字号与行距。</p><p>第二段用于确认较长内容在原站内容主轴内保持稳定排版。</p><p>第三段用于确认页面底部留白与页脚之间的视觉关系。</p>',
        source: '页面视觉验证数据',
        publishDate: `2026-08-${String(28 - index).padStart(2, '0')}`,
        pinned: false,
        recommended: false,
        sortOrder: 100 - index,
        coverResourceId: null,
        bodyImageResourceIds: [],
        attachmentResourceIds: [],
      },
    })
    expect(response.ok()).toBeTruthy()
    const article = await response.json() as { id: number }
    expect((await request.post(`/api/admin/articles/${article.id}/publish`)).ok()).toBeTruthy()
    created.push(article)
  }

  await page.setViewportSize({ width: 1440, height: 1000 })
  await page.goto('/column/notice')
  const frameBox = await page.locator('.public-page-frame').boundingBox()
  expect(frameBox?.width).toBe(1200)
  await expect(page.getByRole('navigation', { name: '栏目位置' })).toHaveCSS('min-height', '57px')
  await expect(page.locator('.detail-card')).toHaveCSS('background-color', 'rgb(255, 255, 255)')
  const firstRow = page.locator('.column-list article').first()
  expect(Math.round((await firstRow.boundingBox())?.height ?? 0)).toBe(81)
  await expect(firstRow.locator('.column-list-icon')).toHaveCSS('background-image', /list-item\.png/)
  await expect(firstRow.locator('.column-list-title')).toHaveCSS('font-size', '16px')
  await expect(firstRow.locator('.column-list-title')).toHaveCSS('font-weight', '700')
  await expect(page.getByRole('button', { name: '第 1 页' })).toHaveAttribute('aria-current', 'page')
  await page.screenshot({ path: testInfo.outputPath('page-detail-column-desktop.png'), fullPage: true })

  await page.goto(`/article/${created[0].id}`)
  await expect(page.locator('.detail-section-title')).toContainText('通知公告')
  await expect(page.getByTestId('public-article-title')).toHaveCSS('font-size', '20px')
  await expect(page.getByText('信息来源：页面视觉验证数据', { exact: true })).toBeVisible()
  await expect(page.getByText('发布时间：2026-08-28', { exact: true })).toBeVisible()
  await page.screenshot({ path: testInfo.outputPath('page-detail-article-desktop.png'), fullPage: true })

  await page.goto('/page/about')
  await expect(page.getByRole('heading', { name: '关于我们', exact: true })).toBeVisible()
  await expect(page.locator('.fixed-detail-card')).toHaveCSS('background-color', 'rgb(255, 255, 255)')
  await page.screenshot({ path: testInfo.outputPath('page-detail-about-desktop.png'), fullPage: true })

  await page.goto('/page/guide/dagl')
  const activeGuide = page.locator('.group-tabs a.active')
  await expect(activeGuide).toHaveText('档案管理')
  await expect(activeGuide).toHaveCSS('background-color', 'rgb(0, 106, 245)')
  expect(Math.round((await activeGuide.boundingBox())?.width ?? 0)).toBe(138)
  await page.screenshot({ path: testInfo.outputPath('page-detail-guide-desktop.png'), fullPage: true })

  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/page/guide/dagl')
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBeTruthy()
  await page.screenshot({ path: testInfo.outputPath('page-detail-guide-mobile.png'), fullPage: true })
})
