import { expect, test } from '@playwright/test'

const mainLinkBlue = 'rgb(0, 106, 245)'
const partyRed = 'rgb(208, 0, 35)'

const mockArticles = Array.from({ length: 11 }, (_, index) => ({
  id: 900000 + index,
  columnId: 1,
  columnName: '理论学习',
  columnAlias: 'party-study',
  title: `党建主题交互验证-${index + 1}`,
  source: 'EU-29 Human Review Theme Contract',
  articleType: 'INTERNAL',
  externalUrl: null,
  publishDate: '2026-09-03',
  pinned: false,
  sortOrder: 11 - index,
  coverResourceId: null,
}))

test('EU-29 Human Review：共享栏目交互态遵循 Main / Party 各自主题色', async ({ page }) => {
  await page.goto('/column/notice')
  const mainBreadcrumb = page.locator('.shared-column-breadcrumb a').first()
  await expect(mainBreadcrumb).toBeVisible()
  await mainBreadcrumb.hover()
  await expect(mainBreadcrumb).toHaveCSS('color', mainLinkBlue)

  await page.route('**/api/public/articles?**', async route => {
    const url = new URL(route.request().url())
    const pageIndex = Number(url.searchParams.get('page') ?? '0')
    const size = Number(url.searchParams.get('size') ?? '10')
    const start = pageIndex * size
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        items: mockArticles.slice(start, start + size),
        page: pageIndex,
        size,
        total: mockArticles.length,
      }),
    })
  })

  await page.goto('/party/column/party-study')
  const partyBreadcrumb = page.locator('.shared-column-breadcrumb a').first()
  await expect(partyBreadcrumb).toBeVisible()
  await partyBreadcrumb.hover()
  await expect(partyBreadcrumb).toHaveCSS('color', partyRed)

  const articleLink = page.getByTestId('party-column-article-900000')
  await expect(articleLink).toBeVisible()
  await articleLink.hover()
  await expect(articleLink.locator('.shared-column-list-title')).toHaveCSS('color', partyRed)

  const currentPage = page.getByRole('button', { name: '第 1 页' })
  await expect(currentPage).toHaveCSS('background-color', partyRed)
  await expect(currentPage).toHaveCSS('border-top-color', partyRed)

  const nextPage = page.getByRole('button', { name: '第 2 页' })
  await nextPage.hover()
  await expect(nextPage).toHaveCSS('color', partyRed)
  await expect(nextPage).toHaveCSS('border-top-color', partyRed)
})
