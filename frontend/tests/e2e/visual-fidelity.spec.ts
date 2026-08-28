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
  await expect(page.locator('.service-shortcuts img')).toHaveCount(6)

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
