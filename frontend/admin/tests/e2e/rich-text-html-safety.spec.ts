import { expect, test } from '@playwright/test'

type Article = {
  id: number
  columnId: number
  title: string
  bodyHtml: string
  source: string
  articleType: 'INTERNAL' | 'EXTERNAL_LINK'
  externalUrl: string | null
  publishDate: string | null
  pinned: boolean
  sortOrder: number
  status: 'DRAFT' | 'PUBLISHED' | 'WITHDRAWN'
  coverResourceId: number | null
  bodyImageResourceIds: number[]
  attachmentResourceIds: number[]
}

type CmsColumn = { id: number; coverPolicy: 'NONE' | 'OPTIONAL' | 'REQUIRED' }
type CmsPage = {
  id: number
  groupId: number | null
  alias: string
  name: string
  bodyHtml: string
  renderMode: 'RICH_TEXT' | 'EMBED_PLACEHOLDER' | 'INTERNAL_STATIC'
  embedUrl: string | null
  sortOrder: number
  enabled: boolean
}

const hostileHtml = `
  <h2>EU-34 安全正文</h2>
  <p onclick="alert(1)" style="text-align:center;color:#333;position:fixed;background-image:url(javascript:alert(2))">
    <strong>允许内容</strong><script>alert(3)</script>
  </p>
  <a href="javascript:alert(4)" onmouseover="alert(5)">危险链接</a>
  <img src="data:image/svg+xml,<svg onload=alert(6)></svg>" onerror="alert(7)">
  <iframe src="https://example.com"></iframe>
  <object data="https://example.com"></object>
  <embed src="https://example.com">
  <form><input value="unsafe"></form>
`

function expectSafe(html: string) {
  expect(html).toContain('EU-34 安全正文')
  expect(html).toContain('允许内容')
  expect(html).toContain('text-align')
  expect(html).toContain('color')
  expect(html).not.toContain('<script')
  expect(html).not.toContain('onclick')
  expect(html).not.toContain('onmouseover')
  expect(html).not.toContain('onerror')
  expect(html).not.toContain('javascript:')
  expect(html).not.toContain('data:image')
  expect(html).not.toContain('position:')
  expect(html).not.toContain('background-image')
  expect(html).not.toContain('<iframe')
  expect(html).not.toContain('<object')
  expect(html).not.toContain('<embed')
  expect(html).not.toContain('<form')
  expect(html).not.toContain('<input')
}

test('EU-34：绕过 Admin UI 的 Article API 仍执行写入与公开输出安全策略', async ({ request }, testInfo) => {
  const columnsResponse = await request.get('/api/admin/columns')
  expect(columnsResponse.ok()).toBeTruthy()
  const columns = await columnsResponse.json() as CmsColumn[]
  const column = columns.find(item => item.coverPolicy !== 'REQUIRED')
  expect(column).toBeTruthy()

  const createResponse = await request.post('/api/admin/articles', {
    data: {
      columnId: column!.id,
      title: `EU-34 HTML Safety ${Date.now()}-${testInfo.retry}`,
      bodyHtml: hostileHtml,
      source: 'EU-34 verification',
      articleType: 'INTERNAL',
      externalUrl: null,
      publishDate: '2026-09-05',
      pinned: false,
      sortOrder: 999,
      coverResourceId: null,
      bodyImageResourceIds: [],
      attachmentResourceIds: [],
    },
  })
  expect(createResponse.ok()).toBeTruthy()
  const created = await createResponse.json() as Article
  let published = false

  try {
    expectSafe(created.bodyHtml)

    const publishResponse = await request.post(`/api/admin/articles/${created.id}/publish`)
    expect(publishResponse.ok()).toBeTruthy()
    published = true

    const publicResponse = await request.get(`/api/public/articles/${created.id}`)
    expect(publicResponse.ok()).toBeTruthy()
    const publicArticle = await publicResponse.json() as { bodyHtml: string }
    expectSafe(publicArticle.bodyHtml)
  } finally {
    if (published) {
      const withdrawResponse = await request.post(`/api/admin/articles/${created.id}/withdraw`)
      expect(withdrawResponse.ok()).toBeTruthy()
    }
  }
})

test('EU-34：绕过 Admin UI 的 RICH_TEXT Page API 仍执行写入与公开输出安全策略', async ({ request }, testInfo) => {
  const alias = `eu34-html-safety-${Date.now()}-${testInfo.retry}`
  const createResponse = await request.post('/api/admin/pages', {
    data: {
      groupId: null,
      alias,
      name: 'EU-34 HTML Safety',
      bodyHtml: hostileHtml,
      renderMode: 'RICH_TEXT',
      embedUrl: null,
      sortOrder: 999,
      enabled: true,
    },
  })
  expect(createResponse.ok()).toBeTruthy()
  const created = await createResponse.json() as CmsPage

  try {
    expectSafe(created.bodyHtml)

    const publicResponse = await request.get(`/api/public/pages/${alias}`)
    expect(publicResponse.ok()).toBeTruthy()
    const publicPage = await publicResponse.json() as CmsPage
    expectSafe(publicPage.bodyHtml)
  } finally {
    const deleteResponse = await request.delete(`/api/admin/pages/${created.id}`)
    expect(deleteResponse.ok()).toBeTruthy()
  }
})
