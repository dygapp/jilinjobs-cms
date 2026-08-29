import { expect, test } from '@playwright/test'

test('导航可维护并只把启用条目暴露为正确公开入口', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const columnName = `政策法规-${suffix}`
  const internalName = `政策入口-${suffix}`
  const externalName = `外部服务-${suffix}`

  const columnResponse = await request.post('/api/admin/columns', {
    data: {
      parentId: null,
      name: columnName,
      sortOrder: 10,
      enabled: true,
    },
  })
  expect(columnResponse.ok()).toBeTruthy()
  const column = await columnResponse.json() as { id: number }

  await page.goto('/admin/navigation')
  await expect(page.getByRole('heading', { name: '导航管理' })).toBeVisible()

  await page.getByTestId('add-navigation').click()
  const dialog = page.getByRole('dialog', { name: '新增导航' })
  await dialog.locator('.el-form-item').filter({ hasText: '导航名称' }).locator('input').fill(internalName)
  await page.getByTestId('navigation-column-select').click()
  await page.getByRole('option', { name: columnName, exact: true }).click()
  await page.getByTestId('save-navigation').click()
  await expect(dialog).toBeHidden()
  await expect(page.getByRole('cell', { name: internalName, exact: true })).toBeVisible()

  const externalResponse = await request.post('/api/admin/navigations', {
    data: {
      parentId: null,
      name: externalName,
      position: 'MAIN',
      category: null,
      targetType: 'LINK',
      targetColumnId: null,
      targetPageId: null,
      targetUrl: 'https://example.com/service',
      openMode: 'DEFAULT',
      sortOrder: 20,
      enabled: true,
    },
  })
  expect(externalResponse.ok()).toBeTruthy()

  const navigationResponse = await request.get('/api/admin/navigations')
  expect(navigationResponse.ok()).toBeTruthy()
  const navigations = await navigationResponse.json() as Array<{ id: number; name: string }>
  const internal = navigations.find((item) => item.name === internalName)
  expect(internal).toBeTruthy()

  await page.goto('/')
  const internalLink = page.getByRole('link', { name: internalName, exact: true })
  await expect(internalLink).toBeVisible()
  const externalLink = page.getByRole('link', { name: externalName, exact: true })
  await expect(externalLink).toHaveAttribute('href', 'https://example.com/service')
  await expect(externalLink).toHaveAttribute('target', '_blank')

  await internalLink.click()
  await expect(page).toHaveURL(new RegExp(`/columns/${column.id}$`))
  await expect(page.getByRole('heading', { name: columnName, exact: true })).toBeVisible()

  await page.goto('/admin/navigation')
  const row = page.getByTestId('navigation-tree-table').getByRole('row').filter({ hasText: internalName })
  const switchInput = row.getByRole('switch')
  await expect(switchInput).toBeChecked()
  await row.locator('.el-switch').click()
  await expect(switchInput).not.toBeChecked()

  await page.goto('/')
  await expect(page.getByRole('link', { name: internalName, exact: true })).toHaveCount(0)
  await expect(page.getByRole('link', { name: externalName, exact: true })).toBeVisible()
})
