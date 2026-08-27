import { expect, test } from '@playwright/test'

test('站点基线：页面组由Flyway初始化且后台可维护', async ({ page, request }) => {
  const response = await request.get('/api/admin/page-groups')
  expect(response.ok()).toBeTruthy()
  const groups = await response.json() as Array<{ id: number; alias: string; name: string; sortOrder: number; enabled: boolean }>
  const guide = groups.find(group => group.alias === 'guide')
  const jobs = groups.find(group => group.alias === 'jobs')
  expect(guide).toBeTruthy()
  expect(jobs).toBeTruthy()

  const updateResponse = await request.put(`/api/admin/page-groups/${guide!.id}`, {
    data: { alias: guide!.alias, name: guide!.name, sortOrder: guide!.sortOrder, enabled: guide!.enabled },
  })
  expect(updateResponse.ok()).toBeTruthy()

  await page.goto('/admin/pages')
  await expect(page.getByRole('heading', { name: '固定页面管理' })).toBeVisible()
  await expect(page.getByTestId('page-group-table')).toContainText('业务指南')
  await expect(page.getByTestId('page-group-table')).toContainText('招聘信息')
  await page.getByTestId('add-page-group').click()
  await expect(page.getByRole('dialog', { name: '新增页面组' })).toBeVisible()
  await page.getByRole('button', { name: '取消' }).click()
})

test('站点基线：初始化静态资源包可见，上传替换删除恢复形成闭环', async ({ page, request }, testInfo) => {
  const baselineResponse = await request.get('/static/health/baseline.png')
  expect(baselineResponse.ok()).toBeTruthy()

  const suffix = `${Date.now()}-${testInfo.retry}`
  const path = `e2e/${suffix}.png`
  const firstContent = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=', 'base64')
  const secondContent = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Z0WQAAAAASUVORK5CYII=', 'base64')

  const uploadResponse = await request.post(`/api/admin/static-resources?path=${encodeURIComponent(path)}&replace=false`, {
    multipart: { file: { name: `${suffix}.png`, mimeType: 'image/png', buffer: firstContent } },
  })
  expect(uploadResponse.ok()).toBeTruthy()

  const replaceResponse = await request.post(`/api/admin/static-resources?path=${encodeURIComponent(path)}&replace=true`, {
    multipart: { file: { name: `${suffix}.png`, mimeType: 'image/png', buffer: secondContent } },
  })
  expect(replaceResponse.ok()).toBeTruthy()
  expect((await request.get(`/static/${path}`)).ok()).toBeTruthy()

  await page.goto('/admin/static-resources')
  await expect(page.getByRole('heading', { name: '网站静态资源管理' })).toBeVisible()
  await expect(page.getByText('删除或替换静态资源可能破坏正在使用该 URL 的页面')).toBeVisible()
  const e2eRow = page.getByRole('row').filter({ hasText: 'e2e' })
  await expect(e2eRow).toBeVisible()
  await e2eRow.getByRole('button', { name: '进入' }).click()
  await expect(page.getByTestId('static-current-path')).toHaveText('e2e')
  const fileRow = page.getByRole('row').filter({ hasText: `${suffix}.png` })
  await expect(fileRow).toBeVisible()
  await expect(fileRow.getByRole('link', { name: '查看/下载' })).toHaveAttribute('href', `/static/${path}`)
  await fileRow.getByRole('button', { name: '替换' }).click()
  await expect(page.getByText(`待替换：${path}`)).toBeVisible()

  const deleteResponse = await request.delete(`/api/admin/static-resources?path=${encodeURIComponent(path)}`)
  expect(deleteResponse.ok()).toBeTruthy()
  const trash = await deleteResponse.json() as { id: string; originalPath: string }
  expect(trash.originalPath).toBe(path)
  expect((await request.get(`/static/${path}`)).status()).toBe(404)

  const restoreResponse = await request.post(`/api/admin/static-resources/restore/${trash.id}`)
  expect(restoreResponse.ok()).toBeTruthy()
  expect((await request.get(`/static/${path}`)).ok()).toBeTruthy()
})
