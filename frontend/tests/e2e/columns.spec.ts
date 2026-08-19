import { expect, test } from '@playwright/test'

test('栏目可以通过管理界面维护且父栏目删除约束生效', async ({ page, request }) => {
  await page.goto('/admin/columns')
  await expect(page.getByRole('heading', { name: '栏目管理' })).toBeVisible()

  await page.getByTestId('add-column').click()
  await page.getByPlaceholder('请输入栏目名称').fill('就业资讯')
  await page.getByTestId('save-column').click()
  await expect(page.getByText('就业资讯')).toBeVisible()

  await page.reload()
  await expect(page.getByText('就业资讯')).toBeVisible()

  const listResponse = await request.get('/api/admin/columns')
  expect(listResponse.ok()).toBeTruthy()
  const columns = await listResponse.json() as Array<{ id: number; name: string }>
  const parent = columns.find((item) => item.name === '就业资讯')
  expect(parent).toBeTruthy()

  await page.getByTestId(`edit-${parent!.id}`).click()
  await page.getByPlaceholder('请输入栏目名称').fill('就业资讯更新')
  await page.getByTestId('save-column').click()
  await expect(page.getByText('就业资讯更新')).toBeVisible()

  const childResponse = await request.post('/api/admin/columns', {
    data: {
      parentId: parent!.id,
      name: '政策动态',
      sortOrder: 10,
      enabled: true,
    },
  })
  expect(childResponse.ok()).toBeTruthy()

  await page.reload()
  await expect(page.getByText('政策动态')).toBeVisible()

  await page.getByTestId(`delete-${parent!.id}`).click()
  await page.getByRole('button', { name: '删除', exact: true }).click()
  await expect(page.getByText('栏目存在下级栏目，不能直接删除')).toBeVisible()

  await page.getByTestId(`enabled-${parent!.id}`).click()
  await expect(page.getByText('停用')).toBeVisible()
})
