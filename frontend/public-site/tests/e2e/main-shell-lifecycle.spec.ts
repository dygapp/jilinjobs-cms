import { expect, test } from '@playwright/test'

test('Main Site Shell 在同一 SPA 生命周期内只装配一次导航与站点属性', async ({ page }) => {
  const requestPaths: string[] = []
  page.on('request', request => {
    const path = new URL(request.url()).pathname
    if (path === '/api/public/navigations' || path === '/api/public/site-config') requestPaths.push(path)
  })

  await page.goto('/')
  await expect(page.getByTestId('public-content')).toBeVisible()
  await expect(page.locator('.site-header')).toHaveCount(1)
  await expect(page.locator('.site-footer')).toHaveCount(1)

  expect(requestPaths.filter(path => path === '/api/public/navigations')).toHaveLength(1)
  expect(requestPaths.filter(path => path === '/api/public/site-config')).toHaveLength(1)

  await page.locator('.notice-panel a[href="/column/notice"]').click()
  await expect(page).toHaveURL(/\/column\/notice$/)
  await expect(page.getByRole('heading', { name: '通知公告' })).toBeVisible()
  await expect(page.locator('.site-header')).toHaveCount(1)
  await expect(page.locator('.site-footer')).toHaveCount(1)

  expect(requestPaths.filter(path => path === '/api/public/navigations')).toHaveLength(1)
  expect(requestPaths.filter(path => path === '/api/public/site-config')).toHaveLength(1)
})
