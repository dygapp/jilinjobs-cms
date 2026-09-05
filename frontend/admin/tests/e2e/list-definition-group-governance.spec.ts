import { expect, test } from '@playwright/test'

type CmsListDefinition = {
  id: number
  code: string
  name: string
  groupCode: string
  imagePolicy: 'NONE' | 'OPTIONAL' | 'REQUIRED'
  description: string
  sortOrder: number
  enabled: boolean
  system: boolean
}

test('EU-32：普通列表创建与编辑不能改写内部分组', async ({ request }, testInfo) => {
  const suffix = `${Date.now()}_${testInfo.retry}`
  const listsResponse = await request.get('/api/admin/lists')
  expect(listsResponse.ok()).toBeTruthy()
  const lists = await listsResponse.json() as CmsListDefinition[]
  const related = lists.find(item => item.code === 'SITE_RELATED')
  expect(related?.groupCode).toBe('SITE_LINKS')

  const preservePreset = await request.put(`/api/admin/lists/${related!.id}`, {
    data: {
      code: related!.code,
      name: related!.name,
      groupCode: 'CLIENT_OVERRIDE',
      imagePolicy: related!.imagePolicy,
      description: related!.description,
      sortOrder: related!.sortOrder,
      enabled: related!.enabled,
      system: related!.system,
    },
  })
  expect(preservePreset.ok()).toBeTruthy()
  expect((await preservePreset.json() as CmsListDefinition).groupCode).toBe('SITE_LINKS')

  const createResponse = await request.post('/api/admin/lists', {
    data: {
      code: `EU32_GROUP_${suffix}`,
      name: `EU-32 分组治理 ${suffix}`,
      groupCode: 'invalid group !',
      imagePolicy: 'OPTIONAL',
      description: '',
      sortOrder: 998,
      enabled: true,
      system: false,
    },
  })
  expect(createResponse.ok()).toBeTruthy()
  const created = await createResponse.json() as CmsListDefinition
  expect(created.groupCode).toBe('GENERAL')

  try {
    const updateResponse = await request.put(`/api/admin/lists/${created.id}`, {
      data: {
        code: created.code,
        name: `${created.name}（已编辑）`,
        groupCode: 'still invalid !',
        imagePolicy: created.imagePolicy,
        description: created.description,
        sortOrder: created.sortOrder,
        enabled: created.enabled,
        system: created.system,
      },
    })
    expect(updateResponse.ok()).toBeTruthy()
    const updated = await updateResponse.json() as CmsListDefinition
    expect(updated.name).toContain('已编辑')
    expect(updated.groupCode).toBe('GENERAL')
  } finally {
    await request.delete(`/api/admin/lists/${created.id}`)
  }
})

test('EU-32：列表管理不暴露分组输入且 SITE_LINKS 公开契约保持有效', async ({ page, request }) => {
  await page.goto('/admin/lists')
  await page.getByTestId('add-cms-list').click()
  const addDialog = page.getByRole('dialog', { name: '新增列表' })
  await expect(addDialog.getByText('分组', { exact: true })).toHaveCount(0)
  await addDialog.getByRole('button', { name: '取消' }).click()

  await page.getByTestId('cms-list-SITE_RELATED').getByRole('button', { name: '列表操作' }).click()
  await page.getByRole('menuitem', { name: '编辑' }).click()
  const editDialog = page.getByRole('dialog', { name: '编辑列表' })
  await expect(editDialog.getByText('分组', { exact: true })).toHaveCount(0)
  await editDialog.getByRole('button', { name: '取消' }).click()

  const publicResponse = await request.get('/api/public/lists/by-group/SITE_LINKS')
  expect(publicResponse.ok()).toBeTruthy()
  const publicLists = await publicResponse.json() as Array<{ code: string; groupCode: string }>
  expect(publicLists.map(item => item.code)).toEqual([
    'SITE_RELATED',
    'SITE_REGIONAL_GRADUATES',
    'SITE_JILIN_UNIVERSITIES',
  ])
  expect(publicLists.every(item => item.groupCode === 'SITE_LINKS')).toBe(true)

  await page.goto('/')
  await expect(page.getByText('中国高等教育学生信息网', { exact: true }).first()).toBeVisible()
})
