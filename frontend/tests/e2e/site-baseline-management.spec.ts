import { expect, test } from '@playwright/test'

test('EU-08：页面组可以通过后台新增并立即用于固定页面管理', async ({ page }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const name = `测试页面组-${suffix}`
  const alias = `test-group-${suffix}`

  await page.goto('/admin/pages')
  await page.getByTestId('add-page-group').click()
  const dialog = page.getByRole('dialog', { name: '新增页面组' })
  await dialog.getByLabel('名称').fill(name)
  await dialog.getByLabel('Alias').fill(alias)
  await dialog.getByRole('button', { name: '保存' }).click()

  await expect(page.getByTestId('page-group-table')).toContainText(name)
  await expect(page.getByTestId('page-group-table')).toContainText(alias)
})

test('EU-10：初始化静态资源包在干净运行时自动落盘并公开可访问', async ({ request }) => {
  const list = await request.get('/api/admin/static-resources?path=site%2Fplaceholders')
  expect(list.ok()).toBeTruthy()
  const rows = await list.json() as Array<{ path: string; directory: boolean }>
  expect(rows.some(row => row.path === 'site/placeholders/external-content.svg' && !row.directory)).toBeTruthy()

  const publicResource = await request.get('/static/site/placeholders/external-content.svg')
  expect(publicResource.ok()).toBeTruthy()
  expect(await publicResource.text()).toContain('外部内容区域')
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
  await expect(page.getByRole('link', { name: '查看/下载' })).toHaveAttribute('href', `/static/${directory}/${filename}`)

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
