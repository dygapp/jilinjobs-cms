import { expect, test, type APIRequestContext } from '@playwright/test'

async function createArticle(request: APIRequestContext, columnId: number, title: string) {
  const response = await request.post('/api/admin/articles', {
    data: {
      columnId,
      title,
      bodyHtml: `<p>${title} 正文</p>`,
      source: 'Admin Query E2E',
      articleType: 'INTERNAL',
      externalUrl: null,
      publishDate: '2026-09-02',
      pinned: false,
      recommended: false,
      sortOrder: 0,
      coverResourceId: null,
      bodyImageResourceIds: [],
      attachmentResourceIds: [],
    },
  })
  expect(response.ok()).toBeTruthy()
  return await response.json() as { id: number; title: string }
}

test('文章管理 API 在服务端完成筛选分页且列表只返回摘要', async ({ request }, testInfo) => {
  const columnsResponse = await request.get('/api/admin/columns')
  expect(columnsResponse.ok()).toBeTruthy()
  const columns = await columnsResponse.json() as Array<{ id: number; alias: string }>
  const column = columns.find(item => item.alias === 'notice') ?? columns[0]
  expect(column).toBeTruthy()

  const prefix = `Admin服务端分页-${Date.now()}-${testInfo.retry}`
  for (let index = 1; index <= 12; index += 1) {
    await createArticle(request, column.id, `${prefix}-${String(index).padStart(2, '0')}`)
  }

  const firstResponse = await request.get(`/api/admin/articles?keyword=${encodeURIComponent(prefix)}&page=0&size=10`)
  expect(firstResponse.ok()).toBeTruthy()
  const first = await firstResponse.json() as { items: Array<Record<string, unknown>>; page: number; size: number; total: number }
  expect(first.total).toBe(12)
  expect(first.items).toHaveLength(10)
  expect(first.page).toBe(0)
  expect(first.size).toBe(10)
  expect(first.items[0]).not.toHaveProperty('bodyHtml')
  expect(first.items[0]).not.toHaveProperty('bodyImageResourceIds')
  expect(first.items[0]).not.toHaveProperty('attachmentResourceIds')

  const secondResponse = await request.get(`/api/admin/articles?keyword=${encodeURIComponent(prefix)}&page=1&size=10`)
  expect(secondResponse.ok()).toBeTruthy()
  const second = await secondResponse.json() as { items: Array<Record<string, unknown>>; total: number }
  expect(second.total).toBe(12)
  expect(second.items).toHaveLength(2)
})

test('文章管理 API 的父栏目作用域包含全部子栏目文章', async ({ request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const parentResponse = await request.post('/api/admin/columns', {
    data: { parentId: null, name: `查询父栏目-${suffix}`, alias: `query-parent-${suffix}`, coverPolicy: 'OPTIONAL', sortOrder: 980, enabled: true },
  })
  expect(parentResponse.ok()).toBeTruthy()
  const parent = await parentResponse.json() as { id: number }

  const childResponse = await request.post('/api/admin/columns', {
    data: { parentId: parent.id, name: `查询子栏目-${suffix}`, alias: `query-child-${suffix}`, coverPolicy: 'OPTIONAL', sortOrder: 0, enabled: true },
  })
  expect(childResponse.ok()).toBeTruthy()
  const child = await childResponse.json() as { id: number }

  const title = `父栏目范围文章-${suffix}`
  await createArticle(request, child.id, title)

  const response = await request.get(`/api/admin/articles?columnId=${parent.id}&keyword=${encodeURIComponent(title)}&page=0&size=10`)
  expect(response.ok()).toBeTruthy()
  const page = await response.json() as { items: Array<{ title: string; columnId: number }>; total: number }
  expect(page.total).toBe(1)
  expect(page.items).toEqual([expect.objectContaining({ title, columnId: child.id })])
})
