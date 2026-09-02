import { expect, test } from '@playwright/test'

test('首页按真实业务作用域加载公开内容', async ({ page, request }) => {
  const aliases = ['notice', 'employment-news', 'recruitment-announcement']
  const columns = await Promise.all(aliases.map(async alias => {
    const response = await request.get(`/api/public/columns/by-alias/${encodeURIComponent(alias)}`)
    expect(response.ok()).toBeTruthy()
    return response.json() as Promise<{ id: number; alias: string }>
  }))

  const articleRequests: string[] = []
  const requestPaths: string[] = []
  page.on('request', requestEvent => {
    const url = new URL(requestEvent.url())
    requestPaths.push(url.pathname)
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
    '首页不应使用全站未限定栏目的文章请求',
  ).toBeFalsy()

  const recruitmentColumn = columns.find(column => column.alias === 'recruitment-announcement')!
  expect(
    articleRequests.some(raw => {
      const url = new URL(raw)
      return url.searchParams.get('columnId') === String(recruitmentColumn.id)
        && url.searchParams.get('articleType') === 'EXTERNAL_LINK'
    }),
    '招聘公告应在招聘栏目内直接限定外链文章类型',
  ).toBeTruthy()

  expect(requestPaths).toContain('/api/public/lists/by-code/HOME_CAROUSEL')
  expect(requestPaths).toContain('/api/public/lists/by-group/SITE_LINKS')
  expect(requestPaths).toContain('/api/public/advertisements/slots/HOME_RECRUITMENT_PROMO')
  expect(requestPaths.filter(path => path === '/api/public/lists')).toHaveLength(0)
  expect(requestPaths.filter(path => path === '/api/public/advertisements')).toHaveLength(0)
})
