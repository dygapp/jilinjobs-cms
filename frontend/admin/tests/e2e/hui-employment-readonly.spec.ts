import { expect, test } from '@playwright/test'

test('固定慧就业 Page 在后台显示只读诊断且不开放地址或正文编辑', async ({ page, request }) => {
  const response = await request.get('/api/admin/pages')
  expect(response.ok()).toBeTruthy()
  const pages = await response.json() as Array<{ id: number; alias: string; rendererKey: string }>
  const liveCourse = pages.find(item => item.alias === 'live-course')
  expect(liveCourse?.rendererKey).toBe('HUI_EMPLOYMENT_LIVE_COURSES')

  await page.goto('/admin/cms/pages')
  await page.getByTestId(`edit-page-${liveCourse!.id}`).click()

  await expect(page.getByTestId('fixed-external-page-readonly')).toBeVisible()
  await expect(page.getByText('Renderer：HUI_EMPLOYMENT_LIVE_COURSES')).toBeVisible()
  await expect(page.getByTestId('page-embed-url')).toHaveCount(0)
  await expect(page.getByTestId('page-placeholder-body')).toHaveCount(0)
  await expect(page.getByTestId('save-page')).toHaveCount(0)
  await expect(page.getByRole('button', { name: '关闭' })).toBeVisible()
})
