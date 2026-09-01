import { expect, test } from '@playwright/test'

test('列表图片要求使用简洁文案且无图片时不显示额外提示', async ({ page }) => {
  await page.goto('/admin/lists')

  await page.getByTestId('cms-list-HOME_CAROUSEL').click()
  await expect(page.getByTestId('active-list-image-requirement')).toHaveText('图片：必填')

  await page.getByTestId('cms-list-SITE_RELATED').click()
  await expect(page.getByTestId('active-list-image-requirement')).toHaveText('图片：无')
  await page.getByTestId('add-cms-list-item').click()

  const itemDialog = page.getByRole('dialog', { name: '新增列表项' })
  await expect(itemDialog.getByText('当前列表定义为不使用图片，列表项不保存图片数据。')).toHaveCount(0)
  await expect(itemDialog.getByTestId('image-resource-picker')).toHaveCount(0)
  await itemDialog.getByRole('button', { name: '取消' }).click()
})

test('列表定义以图片要求的无、可选、必填表达数据约束', async ({ page }) => {
  await page.goto('/admin/lists')
  await page.getByTestId('cms-list-SITE_RELATED').getByRole('button', { name: '列表操作' }).click()
  await page.getByRole('menuitem', { name: '编辑' }).click()

  const dialog = page.getByRole('dialog', { name: '编辑列表' })
  await expect(dialog.getByText('图片要求', { exact: true })).toBeVisible()
  await dialog.getByTestId('list-image-policy').click()
  await expect(page.getByRole('option', { name: '无', exact: true })).toBeVisible()
  await expect(page.getByRole('option', { name: '可选', exact: true })).toBeVisible()
  await expect(page.getByRole('option', { name: '必填', exact: true })).toBeVisible()
})
