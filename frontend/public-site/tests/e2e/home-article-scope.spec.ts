import { expect, test } from '@playwright/test'

test('首页资讯区域按各自栏目独立加载公开文章', async ({ page, request }) => {
  const aliases = ['notice', 'employment-news', 'recruitment-announcement']
  const columns = await Promise.all(aliases.map(async alias => {
    const response = await request.get(`/api/public/columns/by-alias/${encodeURIComponent(alias)}`)
    expect(response.ok()).toBeTruthy()
    return response.json() as Promise<{ id: number; alias: string }>
  }))

  const articleRequests: string[] = []
  page.on('request', requestEvent => {
    const url = new URL(requestEvent.url())
    if (url.pathname === '/api/public/articles') articleRequests.push(requestEvent.url())
  })

  await page.goto('/')
  await expect(page.getByTestId('public-content')).toBeVisible()

  for (const column of columns) {
    expect(
      articleRequests.some(raw => new URL(raw).searchParams.get('columnId') === String(column.id)),
      `首页应按栏目 ${column.alias} 独立请求文章`,
    ).toBeTruthy()
  }

  expect(
    articleRequests.some(raw => !new URL(raw).searchParams.has('columnId')),
    '首页不应再使用全站未限定栏目的文章请求',
  ).toBeFalsy()
})
