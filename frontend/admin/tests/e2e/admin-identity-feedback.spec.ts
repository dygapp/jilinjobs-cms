import { expect, test } from '@playwright/test'

// Session credentials are ephemeral test inputs and must not be recorded in traces.
test.use({ extraHTTPHeaders: {}, trace: 'off' })

test('EU-72：测试人员可完成 admin/super 登录、刷新、禁止访问、退出与身份失效闭环', async ({ browser }) => {
  const context = await browser.newContext({
    baseURL: 'http://127.0.0.1:5173',
    extraHTTPHeaders: {},
  })
  const page = await context.newPage()
  const adminRequests: string[] = []
  page.on('request', request => {
    if (new URL(request.url()).pathname.startsWith('/api/admin/')) adminRequests.push(request.url())
  })

  try {
    await page.goto('/admin/cms/audit')
    await expect(page.getByTestId('review-login')).toBeVisible()
    await expect(page.getByRole('heading', { name: '需要管理身份' })).toBeVisible()
    expect(adminRequests.filter(url => new URL(url).pathname !== '/api/admin/identity')).toEqual([])

    const invalidProfile = await context.request.post('/api/review/identity/sessions', {
      data: { profile: 'owner' },
    })
    expect(invalidProfile.status()).toBe(400)

    const adminLoginResponse = page.waitForResponse(response => response.url().endsWith('/api/review/identity/sessions') && response.request().method() === 'POST')
    await page.getByTestId('review-login-admin').click()
    const adminSession = await (await adminLoginResponse).json() as { credential: string }
    await expect(page.getByTestId('admin-identity-summary')).toContainText('local-review-admin')
    await expect(page.getByTestId('admin-identity-summary')).toContainText('内容管理员')
    await expect(page.getByTestId('admin-nav-audit')).toHaveCount(0)
    await expect(page.getByTestId('audit-forbidden')).toBeVisible()

    const columnResponse = await context.request.post('/api/admin/columns', {
      headers: { 'X-Cms-Review-Credential': adminSession.credential },
      data: { parentId: null, name: `EU72 身份审计-${Date.now()}`, sortOrder: 10, enabled: true },
    })
    expect(columnResponse.ok()).toBeTruthy()
    const column = await columnResponse.json() as { id: number }
    const uploadResponse = await context.request.post('/api/admin/resources', {
      headers: { 'X-Cms-Review-Credential': adminSession.credential },
      multipart: { file: { name: 'review-preview.png', mimeType: 'image/png', buffer: Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aZ1sAAAAASUVORK5CYII=', 'base64') } },
    })
    expect(uploadResponse.ok()).toBeTruthy()
    const resource = await uploadResponse.json() as { id: number }
    expect((await context.request.get(`/api/admin/resources/${resource.id}/content`)).ok()).toBeTruthy()
    // The preview Cookie cannot authenticate identity reads or business writes.
    expect((await context.request.get('/api/admin/identity')).status()).toBe(401)
    expect((await context.request.post('/api/admin/columns', { data: {} })).status()).toBe(401)

    await page.reload()
    await expect(page.getByTestId('admin-identity-summary')).toContainText('local-review-admin')
    await expect(page.getByTestId('audit-forbidden')).toBeVisible()

    await page.getByTestId('review-logout').click()
    await expect(page.getByTestId('review-login')).toBeVisible()
    await expect(page.getByText('已退出测试身份。')).toBeVisible()
    expect((await context.request.get('/api/admin/identity', {
      headers: { 'X-Cms-Review-Credential': adminSession.credential },
    })).status()).toBe(401)
    await page.reload()
    await expect(page.getByTestId('review-login')).toBeVisible()

    const superLoginResponse = page.waitForResponse(response => response.url().endsWith('/api/review/identity/sessions') && response.request().method() === 'POST')
    await page.getByTestId('review-login-super').click()
    const superSession = await (await superLoginResponse).json() as { credential: string }
    await expect(page.getByTestId('admin-identity-summary')).toContainText('local-review-super')
    await expect(page.getByTestId('admin-identity-summary')).toContainText('超级管理员')
    await expect(page.getByTestId('admin-nav-audit')).toBeVisible()
    await expect(page.getByRole('heading', { name: '操作审计' })).toBeVisible()
    const auditResponse = await context.request.get(`/api/admin/audit-events?objectType=COLUMN&objectId=${column.id}&action=CREATE`, {
      headers: { 'X-Cms-Review-Credential': superSession.credential },
    })
    expect(auditResponse.ok()).toBeTruthy()
    const auditPage = await auditResponse.json() as { items: Array<{ identitySource: string; userId: string; roles: string[]; result: string }> }
    expect(auditPage.items).toHaveLength(1)
    expect(auditPage.items[0]).toMatchObject({ identitySource: 'local-review', userId: 'local-review-admin', roles: ['admin'], result: 'SUCCEEDED' })
    expect((await context.request.delete(`/api/admin/columns/${column.id}`, {
      headers: { 'X-Cms-Review-Credential': superSession.credential },
    })).ok()).toBeTruthy()

    await page.getByTestId('review-expire').click()
    await expect(page.getByRole('heading', { name: '管理身份已失效' })).toBeVisible()
    await expect(page.getByTestId('review-login')).toBeVisible()
    expect((await context.request.get('/api/admin/identity', {
      headers: { 'X-Cms-Review-Credential': superSession.credential },
    })).status()).toBe(401)
    await page.reload()
    await expect(page.getByTestId('review-login')).toBeVisible()

    const anonymousAdmin = await context.request.get('/api/admin/identity')
    expect(anonymousAdmin.status()).toBe(401)
    const publicResponse = await context.request.get('/api/public/site-config')
    expect(publicResponse.ok()).toBeTruthy()
  } finally {
    await context.close()
  }
})

test('EU-72：退出请求网络失败时清除页面身份并明确显示服务端结果未确认', async ({ browser }) => {
  const context = await browser.newContext({ baseURL: 'http://127.0.0.1:5173', extraHTTPHeaders: {} })
  try {
    const page = await context.newPage()
    await page.goto('/admin/cms/columns')
    await page.getByTestId('review-login-admin').click()
    await expect(page.getByTestId('admin-identity-summary')).toBeVisible()
    await page.route('**/api/review/identity/sessions/current', route => route.abort())
    await page.getByTestId('review-logout').click()
    await expect(page.getByTestId('review-login')).toBeVisible()
    await expect(page.getByText('已清除当前页面中的测试身份；服务端会话未能确认退出，将在到期后失效。')).toBeVisible()
    await page.reload()
    await expect(page.getByTestId('review-login')).toBeVisible()
  } finally {
    await context.close()
  }
})

test('EU-72：缺少 Review marker 时匿名深链接只展示正式身份反馈', async ({ browser }) => {
  const context = await browser.newContext({ baseURL: 'http://127.0.0.1:5173', extraHTTPHeaders: {} })
  try {
    const page = await context.newPage()
    await page.route('**/review-environment.json', route => route.fulfill({ status: 404 }))
    await page.goto('/admin/cms/articles')
    await expect(page.getByRole('heading', { name: '需要管理身份' })).toBeVisible()
    await expect(page.getByTestId('review-login')).toHaveCount(0)
    await expect(page.getByTestId('identity-retry')).toBeVisible()
    await expect(page.getByTestId('admin-identity-summary')).toHaveCount(0)
  } finally {
    await context.close()
  }
})

test('EU-72：业务页面请求收到 401 时全局清除已失效身份', async ({ browser }) => {
  const context = await browser.newContext({ baseURL: 'http://127.0.0.1:5173' })
  try {
    const page = await context.newPage()
    await page.goto('/admin/cms/columns')
    const loginResponse = page.waitForResponse(response => response.url().endsWith('/api/review/identity/sessions') && response.request().method() === 'POST')
    await page.getByTestId('review-login-admin').click()
    const session = await (await loginResponse).json() as { credential: string }
    await expect(page.getByTestId('admin-identity-summary')).toBeVisible()
    expect((await context.request.post('/api/review/identity/sessions/current/expire', {
      headers: { 'X-Cms-Review-Credential': session.credential },
    })).status()).toBe(204)
    await page.getByTestId('admin-nav-articles').click()
    await expect(page.getByRole('heading', { name: '管理身份已失效' })).toBeVisible()
    await expect(page.getByTestId('review-login')).toBeVisible()
    await expect(page.getByTestId('admin-identity-summary')).toHaveCount(0)
    expect(await page.evaluate(() => sessionStorage.getItem('cms.review.identity.credential'))).toBeNull()
  } finally {
    await context.close()
  }
})
