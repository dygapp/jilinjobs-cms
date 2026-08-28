import { expect, test, type APIRequestContext } from '@playwright/test'

async function createArticle(request: APIRequestContext, columnId: number, title: string) {
  const response = await request.post('/api/admin/articles', {
    data: {
      columnId,
      title,
      bodyHtml: `<p>${title} 正文</p>`,
      source: 'Admin E2E',
      articleType: 'INTERNAL',
      externalUrl: null,
      publishDate: '2026-08-28',
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

test('EU-15：独立管理端 Shell 提供六类管理入口', async ({ page }) => {
  await page.goto('/admin/')
  await expect(page.getByRole('heading', { name: '文章管理' })).toBeVisible()
  await expect(page.getByTestId('admin-nav-articles')).toBeVisible()
  await expect(page.getByTestId('admin-nav-columns')).toBeVisible()
  await expect(page.getByTestId('admin-nav-navigation')).toBeVisible()
  await expect(page.getByTestId('admin-nav-pages')).toBeVisible()
  await expect(page.getByTestId('admin-nav-site-config')).toBeVisible()
  await expect(page.getByTestId('admin-nav-static-resources')).toBeVisible()

  await page.getByTestId('admin-nav-pages').click()
  await expect(page).toHaveURL(/\/admin\/pages$/)
  await expect(page.getByRole('heading', { name: '固定页面管理' })).toBeVisible()
})

test('EU-16：文章筛选分页并保持后台发布到公开站闭环', async ({ page, request }, testInfo) => {
  const columnsResponse = await request.get('/api/admin/columns')
  expect(columnsResponse.ok()).toBeTruthy()
  const columns = await columnsResponse.json() as Array<{ id: number; alias: string }>
  const column = columns.find(item => item.alias === 'notice') ?? columns[0]
  expect(column).toBeTruthy()

  const prefix = `Admin分页-${Date.now()}-${testInfo.retry}`
  const created: Array<{ id: number; title: string }> = []
  for (let index = 1; index <= 12; index += 1) {
    created.push(await createArticle(request, column.id, `${prefix}-${String(index).padStart(2, '0')}`))
  }

  await page.goto('/admin/articles')
  await page.getByTestId('article-filter-keyword').locator('input').fill(prefix)
  await expect(page.getByTestId('article-table').locator('tbody tr')).toHaveCount(10)
  await page.getByTestId('article-pagination').locator('.el-pager li').filter({ hasText: '2' }).click()
  await expect(page.getByTestId('article-table').locator('tbody tr')).toHaveCount(2)

  await page.getByTestId('article-filter-status').click()
  await page.getByRole('option', { name: '草稿' }).click()
  await expect(page.getByTestId('article-table')).toContainText(prefix)

  const published = created[0]
  const publishResponse = await request.post(`/api/admin/articles/${published.id}/publish`)
  expect(publishResponse.ok()).toBeTruthy()
  const publicResponse = await request.get(`/api/public/articles/${published.id}`)
  expect(publicResponse.ok()).toBeTruthy()

  await page.goto(`/article/${published.id}`)
  await expect(page.getByRole('heading', { name: published.title })).toBeVisible()
})

test('EU-16：固定页面按 render mode 提供对应编辑字段', async ({ page }) => {
  await page.goto('/admin/pages')
  await page.getByTestId('add-page').click()
  await expect(page.getByRole('dialog', { name: '新增固定页面' })).toBeVisible()
  await expect(page.getByTestId('page-body-editor')).toBeVisible()

  await page.getByTestId('page-render-mode').click()
  await page.getByRole('option', { name: '外部嵌入占位' }).click()
  await expect(page.getByTestId('page-embed-url')).toBeVisible()
  await expect(page.getByTestId('page-placeholder-body')).toBeVisible()

  await page.getByTestId('page-render-mode').click()
  await page.getByRole('option', { name: '站内特殊页面' }).click()
  await expect(page.getByTestId('page-embed-url')).toHaveAttribute('placeholder', '/special/page-path')
  await page.getByRole('button', { name: '取消' }).click()
})

test('EU-17：网站配置在前端阻止非法 JSON', async ({ page }) => {
  await page.goto('/admin/site-config')
  const editor = page.getByTestId('site-config-HOME_BANNERS').locator('textarea')
  await editor.fill('[{not-json}]')
  await page.getByTestId('save-site-config-HOME_BANNERS').click()
  await expect(page.getByText('JSON 配置格式不正确，请修正后再保存', { exact: true })).toBeVisible()
})

test('EU-17：静态资源拒绝伪装 PNG 并保护站点关键资源', async ({ page, request }) => {
  const fakeUpload = await request.post('/api/admin/static-resources?path=verification-fake/fake.png&replace=false', {
    multipart: { file: { name: 'fake.png', mimeType: 'image/png', buffer: Buffer.from('not a png') } },
  })
  expect(fakeUpload.ok()).toBeFalsy()
  expect((await fakeUpload.json() as { message: string }).message).toContain('实际内容')

  const healthList = await request.get('/api/admin/static-resources?path=health')
  expect(healthList.ok()).toBeTruthy()
  const rows = await healthList.json() as Array<{ path: string; protectedResource: boolean }>
  const baseline = rows.find(row => row.path === 'health/baseline.png')
  expect(baseline?.protectedResource).toBeTruthy()

  const deleteResponse = await request.delete('/api/admin/static-resources?path=health%2Fbaseline.png')
  expect(deleteResponse.ok()).toBeFalsy()
  expect((await deleteResponse.json() as { message: string }).message).toContain('关键资源')

  await page.goto('/admin/static-resources')
  const healthRow = page.getByTestId('static-resource-table').getByRole('row').filter({ hasText: 'health' })
  await healthRow.getByRole('button', { name: '进入' }).click()
  const baselineRow = page.getByTestId('static-resource-table').getByRole('row').filter({ hasText: 'baseline.png' })
  await expect(baselineRow).toContainText('关键资源')
  await expect(baselineRow.getByRole('button', { name: '删除' })).toBeDisabled()
})
