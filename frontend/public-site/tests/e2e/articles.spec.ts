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
  await expect(page.getByRole('heading', { name: '文章管理' })).toBeVisible()
  await page.getByTestId('add-article').click()

  await page.getByPlaceholder('请输入文章标题').fill(title)
  const createDialog = page.getByRole('dialog', { name: '新增文章草稿' })
  await createDialog.getByTestId('article-column-tree-select').click()
  await page.locator('.el-select-dropdown:visible').getByText(columnName, { exact: true }).click()
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
  await page.getByTestId('save-article').click()
  await expect(page.getByRole('dialog', { name: '新增文章草稿' })).toBeHidden()
  const createdRow = page.getByRole('row').filter({ hasText: title })
  await expect(createdRow.getByRole('cell', { name: title, exact: true })).toBeVisible()
  await expect(createdRow.getByRole('cell', { name: '草稿', exact: true })).toBeVisible()

  const articleListResponse = await request.get(`/api/admin/articles?keyword=${encodeURIComponent(title)}&page=0&size=10`)
  expect(articleListResponse.ok()).toBeTruthy()
  const articlePage = await articleListResponse.json() as {
    items: Array<{ id: number; title: string; status: string }>
    total: number
  }
  const createdSummary = articlePage.items.find((item) => item.title === title)
  expect(articlePage.total).toBe(1)
  expect(createdSummary).toBeTruthy()
  expect(createdSummary!.status).toBe('DRAFT')

  const createdDetailResponse = await request.get(`/api/admin/articles/${createdSummary!.id}`)
  expect(createdDetailResponse.ok()).toBeTruthy()
  const created = await createdDetailResponse.json() as {
    id: number
    title: string
    status: string
    coverResourceId: number | null
    bodyImageResourceIds: number[]
    attachmentResourceIds: number[]
  }
  expect(created.coverResourceId).not.toBeNull()
  expect(created.bodyImageResourceIds).toHaveLength(1)
  expect(created.attachmentResourceIds).toHaveLength(1)

  const coverMetadataResponse = await request.get(`/api/admin/resources/${created.coverResourceId}`)
  expect(coverMetadataResponse.ok()).toBeTruthy()
  const coverMetadata = await coverMetadataResponse.json() as { storageKey: string; originalFilename: string }
  expect(coverMetadata.originalFilename).toBe('cover-image.png')
  expect(coverMetadata.storageKey).not.toContain('cover-image.png')
  expect(coverMetadata.storageKey).not.toMatch(/[\\/]/)

  const coverContentResponse = await request.get(`/api/admin/resources/${created.coverResourceId}/content`)
  expect(coverContentResponse.ok()).toBeTruthy()

  await page.getByTestId(`edit-article-${created.id}`).click()
  await page.getByPlaceholder('请输入文章标题').fill(updatedTitle)
  await page.getByTestId('article-body-editor').fill('更新后的草稿正文')
  await page.getByTestId('save-article').click()
  await expect(page.getByRole('dialog', { name: '编辑文章' })).toBeHidden()
  await expect(page.getByRole('cell', { name: updatedTitle, exact: true })).toBeVisible()

  await page.reload()
  await expect(page.getByRole('cell', { name: updatedTitle, exact: true })).toBeVisible()
  await page.getByTestId(`edit-article-${created.id}`).click()
  await expect(page.getByPlaceholder('请输入文章标题')).toHaveValue(updatedTitle)
  await expect(page.getByTestId('article-body-editor')).toContainText('更新后的草稿正文')
  await expect(page.getByText('guide.pdf', { exact: true })).toBeVisible()
  await expect(page.getByTestId('cover-resource-name')).toHaveText('cover-image.png')

  const articleResponse = await request.get(`/api/admin/articles/${created.id}`)
  expect(articleResponse.ok()).toBeTruthy()
  const persisted = await articleResponse.json() as { status: string; title: string }
  expect(persisted.status).toBe('DRAFT')
  expect(persisted.title).toBe(updatedTitle)

  const publicArticleResponse = await request.get(`/api/public/articles/${created.id}`)
  expect(publicArticleResponse.status()).toBe(404)
  const publicResourceResponse = await request.get(`/api/public/resources/${created.coverResourceId}/content`)
  expect(publicResourceResponse.status()).toBe(404)

  const deleteColumnResponse = await request.delete(`/api/admin/columns/${column.id}`)
  expect(deleteColumnResponse.status()).toBe(400)
  const deleteColumnError = await deleteColumnResponse.json() as { message: string }
  expect(deleteColumnError.message).toBe('栏目存在内容，不能直接删除')
})

test('EU-04 发布撤回重新发布驱动公开三级页面可见性', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const title = `待发布文章-${suffix}`
  const updatedTitle = `已编辑发布文章-${suffix}`

  const columnsResponse = await request.get('/api/admin/columns')
  expect(columnsResponse.ok()).toBeTruthy()
  const columns = await columnsResponse.json() as Array<{ id: number; name: string; alias: string }>
  const noticeColumn = columns.find((item) => item.alias === 'notice')
  expect(noticeColumn).toBeTruthy()
  const column = noticeColumn!
  const columnName = column.name

  const imageResponse = await request.post('/api/admin/resources', {
    multipart: {
      file: {
        name: 'public-body.png',
        mimeType: 'image/png',
        buffer: Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Z0WQAAAAASUVORK5CYII=', 'base64'),
      },
    },
  })
  expect(imageResponse.ok()).toBeTruthy()
  const image = await imageResponse.json() as { id: number }

  const articleResponse = await request.post('/api/admin/articles', {
    data: {
      columnId: column.id,
      title,
      bodyHtml: `<p>这是公开正文</p><p><img src="/api/admin/resources/${image.id}/content" alt="正文图片"></p>`,
      source: '吉林就业公开来源',
      publishDate: '2026-08-20',
      pinned: true,
      sortOrder: 50,
      coverResourceId: null,
      bodyImageResourceIds: [image.id],
      attachmentResourceIds: [],
    },
  })
  expect(articleResponse.ok()).toBeTruthy()
  const article = await articleResponse.json() as { id: number; status: string }
  expect(article.status).toBe('DRAFT')

  expect((await request.get(`/api/public/articles/${article.id}`)).status()).toBe(404)
  expect((await request.get(`/api/public/resources/${image.id}/content`)).status()).toBe(404)

  await page.goto('/admin/articles')
  const articleRow = page.getByRole('row').filter({ hasText: title })
  await expect(articleRow.getByRole('cell', { name: '草稿', exact: true })).toBeVisible()
  await page.getByTestId(`publish-article-${article.id}`).click()
  await expect(page.getByRole('row').filter({ hasText: title }).getByRole('cell', { name: '已发布', exact: true })).toBeVisible()

  const homeArticles = page.waitForResponse((response) => response.url().includes('/api/public/articles?') && response.ok())
  await page.goto('/')
  await homeArticles
  await expect(page.getByTestId(`public-article-${article.id}`)).toHaveText(title)

  const columnArticles = page.waitForResponse((response) => response.url().includes(`/api/public/articles?`) && response.url().includes(`columnId=${column.id}`) && response.ok())
  await page.goto(`/columns/${column.id}`)
  await columnArticles
  await expect(page.getByRole('navigation', { name: '栏目位置' })).toContainText(columnName)
  await expect(page.getByTestId(`column-article-${article.id}`).locator('.column-list-title')).toHaveText(title)
  await expect(page.getByText('2026-08-20', { exact: true })).toBeVisible()

  await page.goto(`/articles/${article.id}`)
  await expect(page.getByTestId('public-article-title')).toHaveText(title)
  await expect(page.getByText('信息来源：吉林就业公开来源', { exact: true })).toBeVisible()
  await expect(page.getByText('发布时间：2026-08-20', { exact: true })).toBeVisible()
  await expect(page.getByTestId('public-article-body')).toContainText('这是公开正文')
  await expect(page.getByTestId('public-article-body').locator('img')).toHaveAttribute('src', `/api/public/resources/${image.id}/content`)
  expect((await request.get(`/api/public/resources/${image.id}/content`)).ok()).toBeTruthy()

  const editResponse = await request.put(`/api/admin/articles/${article.id}`, {
    data: {
      columnId: column.id,
      title: updatedTitle,
      bodyHtml: `<p>编辑后的公开正文</p><p><img src="/api/admin/resources/${image.id}/content" alt="正文图片"></p>`,
      source: '吉林就业公开来源',
      publishDate: '2026-08-20',
      pinned: true,
      sortOrder: 50,
      coverResourceId: null,
      bodyImageResourceIds: [image.id],
      attachmentResourceIds: [],
    },
  })
  expect(editResponse.ok()).toBeTruthy()
  const edited = await editResponse.json() as { status: string }
  expect(edited.status).toBe('PUBLISHED')
  const editedPublic = await request.get(`/api/public/articles/${article.id}`)
  expect(editedPublic.ok()).toBeTruthy()
  expect((await editedPublic.json() as { title: string }).title).toBe(updatedTitle)

  await page.goto('/admin/articles')
  await expect(page.getByRole('row').filter({ hasText: updatedTitle }).getByRole('cell', { name: '已发布', exact: true })).toBeVisible()
  await page.getByTestId(`withdraw-article-${article.id}`).click()
  await expect(page.getByRole('row').filter({ hasText: updatedTitle }).getByRole('cell', { name: '已撤回', exact: true })).toBeVisible()

  expect((await request.get(`/api/public/articles/${article.id}`)).status()).toBe(404)
  expect((await request.get(`/api/public/resources/${image.id}/content`)).status()).toBe(404)

  const withdrawnHomeArticles = page.waitForResponse((response) => response.url().includes('/api/public/articles?') && response.ok())
  await page.goto('/')
  await withdrawnHomeArticles
  await expect(page.getByText(updatedTitle, { exact: true })).toHaveCount(0)

  const withdrawnColumnArticles = page.waitForResponse((response) => response.url().includes('/api/public/articles?') && response.url().includes(`columnId=${column.id}`) && response.ok())
  await page.goto(`/columns/${column.id}`)
  await withdrawnColumnArticles
  await expect(page.getByText(updatedTitle, { exact: true })).toHaveCount(0)

  await page.goto(`/articles/${article.id}`)
  await expect(page.getByTestId('public-article-unavailable')).toHaveText('文章不可用或不存在')

  await page.goto('/admin/articles')
  await page.getByTestId(`publish-article-${article.id}`).click()
  await expect(page.getByRole('row').filter({ hasText: updatedTitle }).getByRole('cell', { name: '已发布', exact: true })).toBeVisible()

  await page.goto(`/articles/${article.id}`)
  await expect(page.getByTestId('public-article-title')).toHaveText(updatedTitle)
  expect((await request.get(`/api/public/resources/${image.id}/content`)).ok()).toBeTruthy()
})

test('EU-05 详情附件复制二维码与浏览量形成公开闭环', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const title = `详情增强文章-${suffix}`
  const attachmentName = `办事指南-${suffix}.txt`

  const columnResponse = await request.post('/api/admin/columns', {
    data: { parentId: null, name: `详情栏目-${suffix}`, sortOrder: 10, enabled: true },
  })
  expect(columnResponse.ok()).toBeTruthy()
  const column = await columnResponse.json() as { id: number }

  const attachmentResponse = await request.post('/api/admin/resources', {
    multipart: {
      file: {
        name: attachmentName,
        mimeType: 'text/plain',
        buffer: Buffer.from('吉林就业附件内容'),
      },
    },
  })
  expect(attachmentResponse.ok()).toBeTruthy()
  const attachment = await attachmentResponse.json() as { id: number; sizeBytes: number }

  const articleResponse = await request.post('/api/admin/articles', {
    data: {
      columnId: column.id,
      title,
      bodyHtml: '<p>详情增强正文</p>',
      source: '吉林就业',
      publishDate: '2026-08-20',
      pinned: false,
      sortOrder: 10,
      coverResourceId: null,
      bodyImageResourceIds: [],
      attachmentResourceIds: [attachment.id],
    },
  })
  expect(articleResponse.ok()).toBeTruthy()
  const article = await articleResponse.json() as { id: number }

  const publicAttachmentUrl = `/api/public/resources/${attachment.id}/attachment`
  expect((await request.get(publicAttachmentUrl)).status()).toBe(404)

  const publishResponse = await request.post(`/api/admin/articles/${article.id}/publish`)
  expect(publishResponse.ok()).toBeTruthy()

  const firstDetailResponse = await request.get(`/api/public/articles/${article.id}`)
  expect(firstDetailResponse.ok()).toBeTruthy()
  const firstDetail = await firstDetailResponse.json() as {
    attachments: Array<{ id: number; originalFilename: string; sizeBytes: number }>
  }
  expect(firstDetail.attachments).toEqual([
    expect.objectContaining({
      id: attachment.id,
      originalFilename: attachmentName,
      sizeBytes: attachment.sizeBytes,
    }),
  ])

  const firstAdminResponse = await request.get(`/api/admin/articles/${article.id}`)
  expect(firstAdminResponse.ok()).toBeTruthy()
  expect((await firstAdminResponse.json() as { viewCount: number }).viewCount).toBe(1)

  await page.context().grantPermissions(['clipboard-read', 'clipboard-write'], {
    origin: 'http://127.0.0.1:5173',
  })
  await page.goto(`/articles/${article.id}`)
  await expect(page.getByTestId('public-article-title')).toHaveText(title)
  const attachmentLink = page.getByTestId(`public-attachment-${attachment.id}`)
  await expect(attachmentLink).toContainText(attachmentName)

  await page.getByTestId('copy-article-link').click()
  await expect(page.getByRole('status')).toHaveText('链接已复制')
  const copiedUrl = await page.evaluate(() => navigator.clipboard.readText())
  expect(copiedUrl).toBe(page.url())

  const qrCode = page.getByTestId('article-qrcode')
  await expect(qrCode).toBeVisible()
  await expect(qrCode).toHaveAttribute('src', /^data:image\/png;base64,/)

  const downloadPromise = page.waitForEvent('download')
  await attachmentLink.click()
  const download = await downloadPromise
  expect(download.suggestedFilename()).toBe(attachmentName)

  const secondAdminResponse = await request.get(`/api/admin/articles/${article.id}`)
  expect(secondAdminResponse.ok()).toBeTruthy()
  expect((await secondAdminResponse.json() as { viewCount: number }).viewCount).toBe(2)

  await page.goto('/admin/articles')
  const adminRow = page.getByRole('row').filter({ hasText: title })
  await expect(adminRow.getByRole('cell', { name: '2', exact: true })).toBeVisible()

  const withdrawResponse = await request.post(`/api/admin/articles/${article.id}/withdraw`)
  expect(withdrawResponse.ok()).toBeTruthy()
  expect((await request.get(publicAttachmentUrl)).status()).toBe(404)
})
