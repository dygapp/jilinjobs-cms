import { expect, test } from '@playwright/test'

test('EU-29 Human Review：Main 与 Party 二级栏目复用公共列表展示组件', async ({ page }) => {
  await page.goto('/column/notice')
  const mainPage = page.getByTestId('column-page')
  await expect(mainPage).toHaveAttribute('data-component', 'public-column-page')
  await expect(page.getByTestId('column-articles')).toBeVisible()
  await expect(page.getByTestId('column-articles')).toHaveClass(/shared-column-list/)

  await page.goto('/party/column/party-work')
  const partyPage = page.getByTestId('party-column-page')
  await expect(partyPage).toHaveAttribute('data-component', 'public-column-page')
  await expect(page.getByTestId('party-column-articles')).toBeVisible()
  await expect(page.getByTestId('party-column-articles')).toHaveClass(/shared-column-list/)
  await expect(page.locator('.party-column-list')).toHaveCount(0)
})
