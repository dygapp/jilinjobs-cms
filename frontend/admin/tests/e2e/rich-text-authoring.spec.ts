import { expect, test } from '@playwright/test'

const ONE_PIXEL_PNG = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Zt9sAAAAASUVORK5CYII=', 'base64')

async function firstColumnId(request: import('@playwright/test').APIRequestContext): Promise<number> {
  const response = await request.get('/api/admin/columns')
  expect(response.ok()).toBeTruthy()
  const columns = await response.json() as Array<{ id: number; alias: string }>
  return (columns.find(item => item.alias === 'notice') ?? columns[0]).id
}

test('EU-35：Article 与 Page 共用完整富文本工具栏且已有 HTML 可保存重开', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const columnId = await firstColumnId(request)
  const articleTitle = `EU35富文本文章-${suffix}`
  const articleResponse = await request.post('/api/admin/articles', { data: {
    columnId, title: articleTitle,
    bodyHtml: '<p><span style="font-size:18px;color:#1f4e79">历史正文</span> <strong>强调</strong></p>',
    source: 'EU-35 E2E', articleType: 'INTERNAL', externalUrl: null, publishDate: '2026-09-05',
    pinned: false, sortOrder: 0, coverResourceId: null, bodyImageResourceIds: [], attachmentResourceIds: [],
  } })
  expect(articleResponse.ok()).toBeTruthy()
  const article = await articleResponse.json() as { id: number }

  await page.goto('/admin/articles')
  await page.getByTestId('article-filter-keyword').fill(articleTitle)
  const articleRow = page.getByTestId('article-table').getByRole('row').filter({ hasText: articleTitle })
  await articleRow.getByRole('button', { name: '编辑' }).click()
  const articleDialog = page.getByRole('dialog', { name: '编辑文章' })
  const articleEditor = articleDialog.getByTestId('article-body-editor')
  await expect(articleEditor).toContainText('历史正文')
  await expect(articleEditor.locator('strong')).toContainText('强调')
  for (const control of ['undo','redo','block','bold','italic','underline','strike','bullet-list','ordered-list','blockquote','hr','align-left','align-center','align-right','font-size','font-family','text-color','background-color','link','unlink','image','table','table-add-row','table-delete-row','table-add-column','table-delete-column','table-delete']) {
    await expect(articleDialog.getByTestId(`article-body-editor-${control}`)).toBeVisible()
  }

  await articleEditor.fill('标题内容')
  await articleEditor.selectText()
  await articleDialog.getByTestId('article-body-editor-block').click()
  await page.getByRole('option', { name: '标题 2' }).click()
  await expect(articleEditor.locator('h2')).toContainText('标题内容')
  await articleEditor.locator('h2').selectText()
  await articleDialog.getByTestId('article-body-editor-bold').click()
  await expect(articleEditor.locator('h2 strong')).toContainText('标题内容')
  await articleDialog.getByTestId('article-body-editor-undo').click()
  await expect(articleEditor.locator('h2 strong')).toHaveCount(0)
  await articleDialog.getByTestId('article-body-editor-redo').click()
  await expect(articleEditor.locator('h2 strong')).toContainText('标题内容')

  await articleDialog.getByTestId('body-image-input').setInputFiles({ name: 'eu35.png', mimeType: 'image/png', buffer: ONE_PIXEL_PNG })
  await expect(articleEditor.locator('img')).toHaveCount(1)
  await articleDialog.getByTestId('save-article').click()
  await expect(articleDialog).toBeHidden()
  const storedAfterImage = await (await request.get(`/api/admin/articles/${article.id}`)).json() as { bodyHtml: string; bodyImageResourceIds: number[] }
  expect(storedAfterImage.bodyHtml).toContain('<h2')
  expect(storedAfterImage.bodyImageResourceIds).toHaveLength(1)
  expect(storedAfterImage.bodyHtml).toContain(`/api/public/resources/${storedAfterImage.bodyImageResourceIds[0]}/content`)

  await page.getByTestId('article-filter-keyword').fill(articleTitle)
  await page.getByTestId('article-table').getByRole('row').filter({ hasText: articleTitle }).getByRole('button', { name: '编辑' }).click()
  const reopenedArticle = page.getByRole('dialog', { name: '编辑文章' })
  await expect(reopenedArticle.getByTestId('article-body-editor').locator('h2 strong')).toContainText('标题内容')
  const image = reopenedArticle.getByTestId('article-body-editor').locator('img')
  await image.click()
  await page.keyboard.press('Backspace')
  await expect(reopenedArticle.getByTestId('article-body-editor').locator('img')).toHaveCount(0)
  await reopenedArticle.getByTestId('save-article').click()
  const storedWithoutImage = await (await request.get(`/api/admin/articles/${article.id}`)).json() as { bodyImageResourceIds: number[] }
  expect(storedWithoutImage.bodyImageResourceIds).toEqual([])

  const pageName = `EU35富文本单页-${suffix}`
  const pageAlias = `eu35-rich-${suffix}`
  const createPage = await request.post('/api/admin/pages', { data: {
    groupId: null, alias: pageAlias, name: pageName,
    bodyHtml: '<p><span style="font-family:SimSun;font-size:18px;color:#800000">历史单页正文</span></p>',
    renderMode: 'RICH_TEXT', embedUrl: null, sortOrder: 999, enabled: true,
  } })
  expect(createPage.ok()).toBeTruthy()
  const savedPage = await createPage.json() as { id: number }

  await page.goto('/admin/pages')
  await page.getByTestId(`edit-page-${savedPage.id}`).click()
  const pageDialog = page.getByRole('dialog', { name: '编辑单页' })
  const pageEditor = pageDialog.getByTestId('page-body-editor')
  await expect(pageEditor).toContainText('历史单页正文')
  await expect(pageEditor.locator('span')).toHaveAttribute('style', /font-family/i)

  await pageEditor.fill('链接文本')
  await pageEditor.selectText()
  page.once('dialog', dialog => dialog.accept('https://example.com/eu35'))
  await pageDialog.getByTestId('page-body-editor-link').click()
  await expect(pageEditor.locator('a')).toHaveAttribute('href', 'https://example.com/eu35')
  await pageDialog.getByTestId('page-body-editor-table').click()
  await expect(pageEditor.locator('table')).toHaveCount(1)
  await pageDialog.getByTestId('page-body-editor-hr').click()
  await expect(pageEditor.locator('hr')).toHaveCount(1)
  await pageDialog.getByTestId('page-body-editor-align-center').click()
  await pageDialog.getByRole('button', { name: '保存' }).click()
  await expect(pageDialog).toBeHidden()

  await page.getByTestId(`edit-page-${savedPage.id}`).click()
  const reopenedPage = page.getByRole('dialog', { name: '编辑单页' })
  await expect(reopenedPage.getByTestId('page-body-editor').locator('a')).toHaveAttribute('href', 'https://example.com/eu35')
  await expect(reopenedPage.getByTestId('page-body-editor').locator('table')).toHaveCount(1)
  await reopenedPage.getByRole('button', { name: '取消' }).click()
})

test('EU-35：粘贴 schema 不保留未知节点且服务端安全边界继续生效', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  await page.goto('/admin/pages')
  await page.getByTestId('add-page').click()
  const dialog = page.getByRole('dialog', { name: '新增单页' })
  await dialog.getByRole('textbox', { name: '单页名称' }).fill(`EU35粘贴-${suffix}`)
  await dialog.getByRole('textbox', { name: '公开标识' }).fill(`eu35-paste-${suffix}`)
  const editor = dialog.getByTestId('page-body-editor')
  await editor.evaluate((node, html) => {
    const data = new DataTransfer()
    data.setData('text/html', String(html))
    node.dispatchEvent(new ClipboardEvent('paste', { clipboardData: data, bubbles: true, cancelable: true }))
  }, '<p class="unknown" data-extra="x" onclick="alert(1)">安全文字 <span style="color:#123456;position:fixed">保留颜色</span></p><script>alert(1)</script><iframe src="https://example.com"></iframe>')
  await expect(editor).toContainText('安全文字')
  await expect(editor.locator('script,iframe')).toHaveCount(0)
  await dialog.getByRole('button', { name: '保存' }).click()
  const pages = await (await request.get('/api/admin/pages')).json() as Array<{ alias: string; bodyHtml: string }>
  const saved = pages.find(item => item.alias === `eu35-paste-${suffix}`)
  expect(saved).toBeTruthy()
  expect(saved!.bodyHtml).not.toMatch(/script|iframe|onclick|position\s*:/i)
  expect(saved!.bodyHtml).toContain('安全文字')
})
