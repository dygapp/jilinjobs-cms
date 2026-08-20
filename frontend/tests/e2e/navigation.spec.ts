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
  await page.getByPlaceholder('请输入导航名称').fill(internalName)
  await page.getByPlaceholder('请选择栏目').click()
  await page.getByRole('option', { name: columnName, exact: true }).click()
  await page.getByTestId('save-navigation').click()
  await expect(page.getByRole('dialog', { name: '新增导航' })).toBeHidden()
  await expect(page.getByRole('cell', { name: internalName, exact: true })).toBeVisible()

  const externalResponse = await request.post('/api/admin/navigations', {
    data: {
      name: externalName,
      position: 'MAIN',
      category: null,
      targetType: 'LINK',
      targetColumnId: null,
      targetUrl: 'https://example.com/service',
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
  const switchRoot = page.getByTestId(`navigation-enabled-${internal!.id}`)
  const switchInput = switchRoot.getByRole('switch')
  await expect(switchInput).toBeChecked()
  await switchRoot.click()
  await expect(switchInput).not.toBeChecked()

  await page.goto('/')
  await expect(page.getByRole('link', { name: internalName, exact: true })).toHaveCount(0)
  await expect(page.getByRole('link', { name: externalName, exact: true })).toBeVisible()
})
