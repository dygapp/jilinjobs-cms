import { expect, test } from '@playwright/test'

const PARTY_BANNER = '/static/party-building/party-header-banner.jpg'

test('中心党建作为主站特殊栏目入口并保持独立红色主题', async ({ page, request }) => {
  const navigationResponse = await request.get('/api/public/navigations')
  expect(navigationResponse.ok()).toBeTruthy()
  const navigations = await navigationResponse.json() as Array<{
    name: string
    targetType: string
    href: string
    newWindow: boolean
    clickable: boolean
  }>
  const partyNavigation = navigations.find(item => item.name === '中心党建')
  expect(partyNavigation).toMatchObject({
    targetType: 'LINK',
    href: '/party/',
    newWindow: false,
    clickable: true,
  })

  expect((await request.get(PARTY_BANNER)).ok()).toBeTruthy()

  await page.goto('/')
  await expect(page.getByTestId('public-content')).toBeVisible()
  const partyLink = page.getByRole('link', { name: '中心党建', exact: true })
  await expect(partyLink).toHaveAttribute('href', '/party/')
  await expect(partyLink).not.toHaveAttribute('target', '_blank')
  await partyLink.click()

  await expect(page).toHaveURL(/\/party\/$/)
  await expect(page.getByTestId('party-building-site')).toBeVisible()
  await expect(page.getByTestId('party-building-header')).toBeVisible()
  const banner = page.locator('.party-banner')
  await expect(banner).toBeVisible()
  await expect(banner.locator('a')).toHaveCount(0)
  await expect(banner.locator('.party-banner-image')).toHaveAttribute('src', PARTY_BANNER)
  await expect(page.locator('.party-navigation')).toBeVisible()
  await expect(page.locator('.party-hero')).toHaveCount(0)
  await expect(page.locator('.site-header')).toHaveCount(0)
  await expect(page.getByTestId('party-building-header')).toHaveCSS('background-color', 'rgb(255, 255, 255)')

  await page.reload()
  await expect(page.getByTestId('party-building-site')).toBeVisible()
  await expect(page.locator('.party-banner')).toBeVisible()

  await page.goto('/')
  await expect(page.getByTestId('public-content')).toBeVisible()
  await expect(page.getByTestId('party-building-site')).toHaveCount(0)
})
