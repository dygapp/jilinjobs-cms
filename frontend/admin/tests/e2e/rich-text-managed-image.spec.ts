import { expect, test } from '@playwright/test'

const ONE_PIXEL_PNG = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Zt9sAAAAASUVORK5CYII=',
  'base64',
)

async function firstColumnId(request: import('@playwright/test').APIRequestContext): Promise<number> {
  const response = await request.get('/api/admin/columns')
  expect(response.ok()).toBeTruthy()
  const columns = await response.json() as Array<{ id: number; alias: string }>
  return (columns.find(item => item.alias === 'notice') ?? columns[0]).id
}

test('EU-54：Article 正文图片继续通过 managed Resource bridge 保存', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const columnId = await firstColumnId(request)
  const title = `EU54托管图片-${suffix}`
  const create = await request.post('/api/admin/articles', { data: {
    columnId,
    title,
    bodyHtml: '<p>图片前正文</p>',
    source: 'EU-54 managed image bridge',
    articleType: 'INTERNAL',
    externalUrl: null,
    publishDate: '2026-09-11',
    pinned: false,
    sortOrder: 0,
    coverResourceId: null,
    bodyImageResourceIds: [],
    attachmentResourceIds: [],
  } })
  expect(create.ok()).toBeTruthy()
  const article = await create.json() as { id: number }

  await page.goto('/admin/articles')
  await page.getByTestId('article-filter-keyword').fill(title)
  const row = page.getByTestId('article-table').getByRole('row').filter({ hasText: title })
  await row.getByRole('button', { name: '编辑' }).click()

  const dialog = page.getByRole('dialog', { name: '编辑文章' })
  const root = dialog.getByTestId('article-body-editor')
  const surface = root.locator('.se-wrapper-wysiwyg[contenteditable="true"]')
  await expect(surface).toBeVisible()

  await root.locator('[data-command="image"]').click()
  // SunEditor mounts its carrier/modal under document.body instead of inside the Vue adapter root.
  const fileInput = page.locator('input.__se__file_input[type="file"]')
  await expect(fileInput).toHaveCount(1)
  await fileInput.setInputFiles({ name: 'eu54-managed.png', mimeType: 'image/png', buffer: ONE_PIXEL_PNG })
  const imageModalSubmit = page.locator('.se-modal-content:visible .se-btn-primary')
  await expect(imageModalSubmit).toHaveCount(1)
  await imageModalSubmit.click()

  const insertedImage = surface.locator('img').last()
  await expect(insertedImage).toBeVisible()
  await expect(insertedImage).toHaveAttribute('src', /\/api\/admin\/resources\/\d+\/content/)
  await dialog.getByTestId('save-article').click()
  await expect(dialog).toBeHidden()

  const storedResponse = await request.get(`/api/admin/articles/${article.id}`)
  expect(storedResponse.ok()).toBeTruthy()
  const stored = await storedResponse.json() as { bodyHtml: string; bodyImageResourceIds: number[] }
  expect(stored.bodyImageResourceIds).toHaveLength(1)
  expect(stored.bodyHtml).toContain(`/api/admin/resources/${stored.bodyImageResourceIds[0]}/content`)
})
