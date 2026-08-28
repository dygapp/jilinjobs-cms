import { expect, test } from '@playwright/test'

const pngOne = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Zl3sAAAAASUVORK5CYII=', 'base64')
const pngTwo = Buffer.concat([pngOne, Buffer.from([0])])

test('EU-08：页面组由 Flyway 初始化且公开上下文可读取', async ({ request }) => {
  const response = await request.get('/api/admin/page-groups')
  expect(response.ok()).toBeTruthy()
  const groups = await response.json() as Array<{ id: number; alias: string; name: string; sortOrder: number; enabled: boolean }>
  expect(groups.some(group => group.alias === 'guide')).toBeTruthy()
  expect(groups.some(group => group.alias === 'jobs')).toBeTruthy()

  const publicGuide = await request.get('/api/public/page-groups/guide')
  expect(publicGuide.ok()).toBeTruthy()
  const guide = await publicGuide.json() as { members: Array<{ alias: string }> }
  expect(guide.members.map(member => member.alias)).toContain('dagl')
})

test('EU-10：版本化初始化静态资源包在干净运行时挂载并公开可访问', async ({ request }) => {
  const list = await request.get('/api/admin/static-resources?path=health')
  expect(list.ok()).toBeTruthy()
  const rows = await list.json() as Array<{ path: string; directory: boolean; protectedResource: boolean }>
  const baseline = rows.find(row => row.path === 'health/baseline.png' && !row.directory)
  expect(baseline).toBeTruthy()
  expect(baseline!.protectedResource).toBeTruthy()

  const publicResource = await request.get('/static/health/baseline.png')
  expect(publicResource.ok()).toBeTruthy()
  expect((await publicResource.body()).length).toBeGreaterThan(0)
})

test('EU-10：普通静态资源支持真实 PNG 上传、替换、删除和恢复', async ({ request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const directory = `verification-${suffix}`
  const filename = 'sample.png'
  const path = `${directory}/${filename}`

  let response = await request.post(`/api/admin/static-resources?path=${encodeURIComponent(path)}&replace=false`, {
    multipart: { file: { name: filename, mimeType: 'image/png', buffer: pngOne } },
  })
  expect(response.ok()).toBeTruthy()

  response = await request.post(`/api/admin/static-resources?path=${encodeURIComponent(path)}&replace=true`, {
    multipart: { file: { name: 'replacement.png', mimeType: 'image/png', buffer: pngTwo } },
  })
  expect(response.ok()).toBeTruthy()

  const replaced = await request.get(`/static/${directory}/${filename}`)
  expect(replaced.ok()).toBeTruthy()
  expect(await replaced.body()).toEqual(pngTwo)

  const removed = await request.delete(`/api/admin/static-resources?path=${encodeURIComponent(path)}`)
  expect(removed.ok()).toBeTruthy()
  const trash = await removed.json() as { id: string; originalPath: string }
  expect(trash.originalPath).toBe(path)
  expect((await request.get(`/static/${directory}/${filename}`)).status()).toBe(404)

  const restored = await request.post(`/api/admin/static-resources/restore/${trash.id}`)
  expect(restored.ok()).toBeTruthy()
  expect((await request.get(`/static/${directory}/${filename}`)).ok()).toBeTruthy()
})
