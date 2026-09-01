import { expect, test, type Page } from '@playwright/test'

const tinyPng = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=', 'base64')

async function openCarouselItemEditor(page: Page) {
  await page.goto('/admin/lists')
  await page.getByTestId('cms-list-HOME_CAROUSEL').click()
  const row = page.getByTestId('cms-list-item-table').getByRole('row').filter({ hasText: '这里美得不愿离开' })
  await row.getByRole('button', { name: '编辑' }).click()
  return page.getByRole('dialog', { name: '编辑列表项' })
}

test('统一图片选择器可以跨 CMS 模块复用 Runtime 已上传图片', async ({ page, request }) => {
  const name = `shared-${Date.now()}.png`
  const relativePath = `uploads/shared-e2e/${name}`
  const upload = await request.post(`/api/admin/static-resources?path=${encodeURIComponent(relativePath)}&replace=false`, {
    multipart: { file: { name, mimeType: 'image/png', buffer: tinyPng } },
  })
  expect(upload.ok()).toBeTruthy()

  const editDialog = await openCarouselItemEditor(page)
  await editDialog.getByRole('button', { name: '选择已有图片' }).click()
  const library = page.getByRole('dialog', { name: '选择已有图片' })
  const option = library.getByRole('button').filter({ hasText: name })
  await expect(option).toBeVisible()
  await option.click()
  await expect(editDialog.locator('img[alt="当前图片"]')).toHaveAttribute('src', `/static/${relativePath}`)
  await editDialog.getByRole('button', { name: '取消' }).click()
})

test('图片选择器使用上传图片按钮且允许上传超过 1MB 的图片', async ({ page }) => {
  const editDialog = await openCarouselItemEditor(page)
  await expect(editDialog.getByTestId('image-resource-upload-trigger')).toHaveText('上传图片')
  await expect(editDialog.getByTestId('image-resource-upload')).toBeHidden()

  const mediumPng = Buffer.concat([tinyPng, Buffer.alloc(2 * 1024 * 1024)])
  await editDialog.getByTestId('image-resource-upload').setInputFiles({
    name: 'over-one-megabyte.png',
    mimeType: 'image/png',
    buffer: mediumPng,
  })
  await expect(page.getByText('图片已上传并选中')).toBeVisible()
  await editDialog.getByRole('button', { name: '取消' }).click()
})

test('图片超过 20MB 时在前端直接提示明确的大小限制', async ({ page }) => {
  const editDialog = await openCarouselItemEditor(page)
  const tooLargePng = Buffer.concat([tinyPng, Buffer.alloc(20 * 1024 * 1024)])
  await editDialog.getByTestId('image-resource-upload').setInputFiles({
    name: 'too-large.png',
    mimeType: 'image/png',
    buffer: tooLargePng,
  })
  await expect(page.getByText('图片大小不能超过 20MB')).toBeVisible()
  await editDialog.getByRole('button', { name: '取消' }).click()
})
