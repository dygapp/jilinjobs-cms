import { expect, test } from '@playwright/test'

test('中心党建使用独立公开站入口并与主站主题隔离', async ({ page, request }) => {
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

  await page.goto('/')
  await expect(page.getByTestId('public-content')).toBeVisible()
  const partyLink = page.getByRole('link', { name: '中心党建', exact: true })
  await expect(partyLink).toHaveAttribute('href', '/party/')
  await expect(partyLink).not.toHaveAttribute('target', '_blank')
  await partyLink.click()

  await expect(page).toHaveURL(/\/party\/$/)
  await expect(page.getByTestId('party-building-site')).toBeVisible()
  await expect(page.getByTestId('party-building-header')).toBeVisible()
  await expect(page.locator('.party-banner')).toBeVisible()
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
