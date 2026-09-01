import { expect, test } from '@playwright/test'

test('EU-21：网站属性图片使用自适应缩略图并复用 Element Plus Viewer', async ({ page }) => {
  await page.goto('/admin/site-config')
  const table = page.getByTestId('site-config-table')
  const row = table.getByRole('row').filter({ hasText: 'PLATFORM_LOGO_ICON_PATH' })
  const preview = row.getByTestId('adaptive-image-preview')
  await expect(preview).toBeVisible()
  await expect(preview).toHaveAttribute('data-preview-theme', /dark|light|checker/)
  await expect(preview).toHaveAttribute('title', /Element Plus/)

  await row.getByRole('button', { name: '编辑值' }).click()
  const dialog = page.getByTestId('site-config-value-dialog')
  const picker = dialog.getByTestId('image-resource-picker')
  const currentPreview = picker.getByTestId('adaptive-image-preview').first()
  await expect(currentPreview).toBeVisible()
  await expect(currentPreview).toHaveAttribute('data-preview-theme', /dark|light|checker/)

  await currentPreview.locator('.el-image').click()
  await expect(page.locator('.el-image-viewer__wrapper')).toBeVisible()
  await page.keyboard.press('Escape')
  await expect(page.locator('.el-image-viewer__wrapper')).toHaveCount(0)
  await dialog.getByRole('button', { name: '取消' }).click()
})

test('EU-21：列表与宣传展示复用统一图片缩略图', async ({ page }) => {
  await page.goto('/admin/lists')
  await page.getByTestId('cms-list-HOME_CAROUSEL').click()
  const listRow = page.getByTestId('cms-list-item-table').getByRole('row').filter({ hasText: '这里美得不愿离开' })
  await expect(listRow.getByTestId('adaptive-image-preview')).toBeVisible()

  await page.goto('/admin/advertisements')
  const adRow = page.getByTestId('advertisement-table').getByRole('row').filter({ hasText: '吉林省高校毕业生招聘活动' })
  await expect(adRow.getByTestId('adaptive-image-preview')).toBeVisible()
})

test('EU-21：静态资源显示受保护语义并为图片提供页内预览', async ({ page, request }) => {
  const response = await request.get('/api/admin/static-resources?path=health')
  expect(response.ok()).toBeTruthy()
  const entries = await response.json() as Array<{ name: string; protectedResource: boolean }>
  expect(entries.find(item => item.name === 'baseline.png')?.protectedResource).toBe(true)

  await page.goto('/admin/static-resources')
  const healthRow = page.getByTestId('static-resource-table').getByRole('row').filter({ hasText: 'health' })
  await healthRow.getByRole('button', { name: '进入' }).click()
  const baselineRow = page.getByTestId('static-resource-table').getByRole('row').filter({ hasText: 'baseline.png' })
  await expect(baselineRow.getByText('受保护', { exact: true })).toBeVisible()
  const preview = baselineRow.getByTestId('adaptive-image-preview')
  await expect(preview).toBeVisible()
  await preview.locator('.el-image').click()
  await expect(page.locator('.el-image-viewer__wrapper')).toBeVisible()
  await page.keyboard.press('Escape')
})
