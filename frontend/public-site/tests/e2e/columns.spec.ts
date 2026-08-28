import { expect, test } from '@playwright/test'

test('栏目可以通过管理界面维护且父栏目删除约束生效', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const parentName = `就业资讯-${suffix}`
  const updatedName = `就业资讯更新-${suffix}`
  const childName = `政策动态-${suffix}`

  await page.goto('/admin/columns')
  await expect(page.getByRole('heading', { name: '栏目管理' })).toBeVisible()

  await page.getByTestId('add-column').click()
  await page.getByPlaceholder('请输入栏目名称').fill(parentName)
  await page.getByTestId('save-column').click()
  await expect(page.getByRole('dialog', { name: '新增栏目' })).toBeHidden()
  await expect(page.getByRole('cell', { name: parentName, exact: true })).toBeVisible()

  await page.reload()
  await expect(page.getByRole('cell', { name: parentName, exact: true })).toBeVisible()

  const listResponse = await request.get('/api/admin/columns')
  expect(listResponse.ok()).toBeTruthy()
  const columns = await listResponse.json() as Array<{ id: number; name: string }>
  const parent = columns.find((item) => item.name === parentName)
  expect(parent).toBeTruthy()

  await page.getByTestId(`edit-${parent!.id}`).click()
  await page.getByPlaceholder('请输入栏目名称').fill(updatedName)
  await page.getByTestId('save-column').click()
  await expect(page.getByRole('dialog', { name: '编辑栏目' })).toBeHidden()
  await expect(page.getByRole('cell', { name: updatedName, exact: true })).toBeVisible()

  const childResponse = await request.post('/api/admin/columns', {
    data: {
      parentId: parent!.id,
      name: childName,
      sortOrder: 10,
      enabled: true,
    },
  })
  expect(childResponse.ok()).toBeTruthy()

  await page.reload()
  await expect(page.getByRole('cell', { name: childName, exact: true })).toBeVisible()

  await page.getByTestId(`delete-${parent!.id}`).click()
  await page.getByRole('dialog').getByRole('button', { name: '删除', exact: true }).click()
  await expect(page.getByText('栏目存在下级栏目，不能直接删除', { exact: true })).toBeVisible()

  const enabledSwitchRoot = page.getByTestId(`enabled-${parent!.id}`)
  const enabledSwitchInput = enabledSwitchRoot.getByRole('switch')
  await expect(enabledSwitchInput).toBeChecked()
  await enabledSwitchRoot.click()
  await expect(enabledSwitchInput).not.toBeChecked()
})
