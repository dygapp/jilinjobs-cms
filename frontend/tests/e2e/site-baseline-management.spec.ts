import { expect, test } from '@playwright/test'

test('EU-08：页面组由Flyway初始化且后台可维护', async ({ page, request }) => {
  const response = await request.get('/api/admin/page-groups')
  expect(response.ok()).toBeTruthy()
  const groups = await response.json() as Array<{ id: number; alias: string; name: string; sortOrder: number; enabled: boolean }>
  const guide = groups.find(group => group.alias === 'guide')
  const jobs = groups.find(group => group.alias === 'jobs')
  expect(guide).toBeTruthy()
  expect(jobs).toBeTruthy()

  const update = await request.put(`/api/admin/page-groups/${guide!.id}`, {
    data: { alias: guide!.alias, name: guide!.name, sortOrder: guide!.sortOrder, enabled: guide!.enabled },
  })
  expect(update.ok()).toBeTruthy()

  await page.goto('/admin/pages')
  await expect(page.getByTestId('page-group-table')).toContainText('业务指南')
  await expect(page.getByTestId('page-group-table')).toContainText('招聘信息')
  await page.getByTestId('add-page-group').click()
  await expect(page.getByRole('dialog', { name: '新增页面组' })).toBeVisible()
  await page.getByRole('button', { name: '取消' }).click()
})

test('EU-10：版本化初始化静态资源包在干净运行时挂载并公开可访问', async ({ request }) => {
  const list = await request.get('/api/admin/static-resources?path=health')
  expect(list.ok()).toBeTruthy()
  const rows = await list.json() as Array<{ path: string; directory: boolean }>
  expect(rows.some(row => row.path === 'health/baseline.png' && !row.directory)).toBeTruthy()

  const publicResource = await request.get('/static/health/baseline.png')
  expect(publicResource.ok()).toBeTruthy()
  expect((await publicResource.body()).length).toBeGreaterThan(0)
})

test('EU-10：静态资源支持目录浏览、上传、替换、删除和恢复', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const directory = `verification-${suffix}`
  const filename = 'sample.png'
  const path = `${directory}/${filename}`

  let response = await request.post(`/api/admin/static-resources?path=${encodeURIComponent(path)}&replace=false`, {
    multipart: { file: { name: filename, mimeType: 'image/png', buffer: Buffer.from('version-one') } },
  })
  expect(response.ok()).toBeTruthy()

  await page.goto('/admin/static-resources')
  const row = page.getByTestId('static-resource-table').getByRole('row').filter({ hasText: directory })
  await expect(row).toBeVisible()
  await row.getByRole('button', { name: '进入' }).click()
  await expect(page.getByTestId('static-current-path')).toHaveText(directory)
  const fileRow = page.getByRole('row').filter({ hasText: filename })
  await expect(fileRow.getByRole('link', { name: '查看/下载' })).toHaveAttribute('href', `/static/${directory}/${filename}`)
  await fileRow.getByRole('button', { name: '替换' }).click()
  await expect(page.getByText(`待替换：${path}`)).toBeVisible()

  response = await request.post(`/api/admin/static-resources?path=${encodeURIComponent(path)}&replace=true`, {
    multipart: { file: { name: filename, mimeType: 'image/png', buffer: Buffer.from('version-two') } },
  })
  expect(response.ok()).toBeTruthy()
  const replaced = await request.get(`/static/${directory}/${filename}`)
  expect(await replaced.body()).toEqual(Buffer.from('version-two'))

  const removed = await request.delete(`/api/admin/static-resources?path=${encodeURIComponent(path)}`)
  expect(removed.ok()).toBeTruthy()
  const trash = await removed.json() as { id: string; originalPath: string }
  expect(trash.originalPath).toBe(path)
  expect((await request.get(`/static/${directory}/${filename}`)).status()).toBe(404)

  const restored = await request.post(`/api/admin/static-resources/restore/${trash.id}`)
  expect(restored.ok()).toBeTruthy()
  expect((await request.get(`/static/${directory}/${filename}`)).ok()).toBeTruthy()
})
