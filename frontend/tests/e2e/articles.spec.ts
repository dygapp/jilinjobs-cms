import { expect, test } from '@playwright/test'

test('文章草稿、文件资源与栏目内容依赖形成管理端闭环', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const columnName = `文章栏目-${suffix}`
  const title = `草稿文章-${suffix}`
  const updatedTitle = `草稿文章更新-${suffix}`

  const columnResponse = await request.post('/api/admin/columns', {
    data: {
      parentId: null,
      name: columnName,
      sortOrder: 30,
      enabled: true,
    },
  })
  expect(columnResponse.ok()).toBeTruthy()
  const column = await columnResponse.json() as { id: number }

  await page.goto('/admin/articles')
  await expect(page.getByRole('heading', { name: '文章草稿管理' })).toBeVisible()
  await page.getByTestId('add-article').click()

  await page.getByPlaceholder('请输入文章标题').fill(title)
  await page.getByTestId('article-column').click()
  await page.getByRole('option', { name: columnName, exact: true }).click()
  await page.getByPlaceholder('请输入内容来源').fill('吉林就业测试来源')
  await page.getByTestId('article-body-editor').fill('这是草稿正文')

  await page.getByTestId('body-image-input').setInputFiles({
    name: 'body-image.png',
    mimeType: 'image/png',
    buffer: Buffer.from('body-image-content'),
  })
  await expect(page.getByText('正文图片已上传', { exact: true })).toBeVisible()

  await page.getByTestId('cover-input').setInputFiles({
    name: 'cover-image.png',
    mimeType: 'image/png',
    buffer: Buffer.from('cover-image-content'),
  })
  await expect(page.getByTestId('cover-resource-name')).toHaveText('cover-image.png')

  await page.getByTestId('attachment-input').setInputFiles([
    {
      name: 'guide.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('attachment-content'),
    },
  ])
  await expect(page.getByText('guide.pdf', { exact: true })).toBeVisible()

  await page.getByTestId('article-pinned').click()
  await page.getByTestId('article-recommended').click()
  await page.getByTestId('save-article').click()
  await expect(page.getByRole('dialog', { name: '新增文章草稿' })).toBeHidden()
  const createdRow = page.getByRole('row').filter({ hasText: title })
  await expect(createdRow.getByRole('cell', { name: title, exact: true })).toBeVisible()
  await expect(createdRow.getByRole('cell', { name: '草稿', exact: true })).toBeVisible()

  const articleListResponse = await request.get('/api/admin/articles')
  expect(articleListResponse.ok()).toBeTruthy()
  const articleList = await articleListResponse.json() as Array<{
    id: number
    title: string
    status: string
    coverResourceId: number | null
    bodyImageResourceIds: number[]
    attachmentResourceIds: number[]
  }>
  const created = articleList.find((item) => item.title === title)
  expect(created).toBeTruthy()
  expect(created!.status).toBe('DRAFT')
  expect(created!.coverResourceId).not.toBeNull()
  expect(created!.bodyImageResourceIds).toHaveLength(1)
  expect(created!.attachmentResourceIds).toHaveLength(1)

  const coverMetadataResponse = await request.get(`/api/admin/resources/${created!.coverResourceId}`)
  expect(coverMetadataResponse.ok()).toBeTruthy()
  const coverMetadata = await coverMetadataResponse.json() as { storageKey: string; originalFilename: string }
  expect(coverMetadata.originalFilename).toBe('cover-image.png')
  expect(coverMetadata.storageKey).not.toContain('cover-image.png')
  expect(coverMetadata.storageKey).not.toMatch(/[\\/]/)

  const coverContentResponse = await request.get(`/api/admin/resources/${created!.coverResourceId}/content`)
  expect(coverContentResponse.ok()).toBeTruthy()

  await page.getByTestId(`edit-article-${created!.id}`).click()
  await page.getByPlaceholder('请输入文章标题').fill(updatedTitle)
  await page.getByTestId('article-body-editor').fill('更新后的草稿正文')
  await page.getByTestId('save-article').click()
  await expect(page.getByRole('dialog', { name: '编辑文章草稿' })).toBeHidden()
  await expect(page.getByRole('cell', { name: updatedTitle, exact: true })).toBeVisible()

  await page.reload()
  await expect(page.getByRole('cell', { name: updatedTitle, exact: true })).toBeVisible()
  await page.getByTestId(`edit-article-${created!.id}`).click()
  await expect(page.getByPlaceholder('请输入文章标题')).toHaveValue(updatedTitle)
  await expect(page.getByTestId('article-body-editor')).toContainText('更新后的草稿正文')
  await expect(page.getByText('guide.pdf', { exact: true })).toBeVisible()
  await expect(page.getByTestId('cover-resource-name')).toHaveText('cover-image.png')

  const articleResponse = await request.get(`/api/admin/articles/${created!.id}`)
  expect(articleResponse.ok()).toBeTruthy()
  const persisted = await articleResponse.json() as { status: string; title: string }
  expect(persisted.status).toBe('DRAFT')
  expect(persisted.title).toBe(updatedTitle)

  const publicArticleResponse = await request.get(`/api/public/articles/${created!.id}`)
  expect(publicArticleResponse.status()).toBe(404)
  const publicResourceResponse = await request.get(`/api/public/resources/${created!.coverResourceId}/content`)
  expect(publicResourceResponse.status()).toBe(404)

  const deleteColumnResponse = await request.delete(`/api/admin/columns/${column.id}`)
  expect(deleteColumnResponse.status()).toBe(400)
  const deleteColumnError = await deleteColumnResponse.json() as { message: string }
  expect(deleteColumnError.message).toBe('栏目存在内容，不能直接删除')
})
