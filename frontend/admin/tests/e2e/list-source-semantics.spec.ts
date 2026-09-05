import { expect, test, type APIRequestContext } from '@playwright/test'

async function createArticle(
  request: APIRequestContext,
  columnId: number,
  title: string,
  articleType: 'INTERNAL' | 'EXTERNAL_LINK',
) {
  const response = await request.post('/api/admin/articles', {
    data: {
      columnId,
      title,
      bodyHtml: articleType === 'INTERNAL' ? `<p>${title} 正文</p>` : '',
      source: 'EU-30 source semantics E2E',
      articleType,
      externalUrl: articleType === 'EXTERNAL_LINK' ? `https://example.com/${encodeURIComponent(title)}` : null,
      publishDate: '2026-09-05',
      pinned: false,
      sortOrder: 0,
      coverResourceId: null,
      bodyImageResourceIds: [],
      attachmentResourceIds: [],
    },
  })
  expect(response.ok()).toBeTruthy()
  return await response.json() as { id: number; title: string }
}

test('EU-30：列表管理区分直接链接、站内文章与外链文章的数据来源语义', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`.replaceAll('-', '_')
  const columnsResponse = await request.get('/api/admin/columns')
  expect(columnsResponse.ok()).toBeTruthy()
  const columns = await columnsResponse.json() as Array<{ id: number }>
  const columnId = columns[0]?.id
  expect(columnId).toBeTruthy()

  const internalArticle = await createArticle(request, columnId!, `站内来源-${suffix}`, 'INTERNAL')
  const externalArticle = await createArticle(request, columnId!, `外链来源-${suffix}`, 'EXTERNAL_LINK')
  const listResponse = await request.post('/api/admin/lists', {
    data: {
      code: `E2E_SOURCE_${suffix}`,
      name: `来源语义-${suffix}`,
      groupCode: 'E2E',
      imagePolicy: 'NONE',
      description: '',
      sortOrder: 996,
      enabled: true,
      system: false,
    },
  })
  expect(listResponse.ok()).toBeTruthy()
  const list = await listResponse.json() as { id: number; code: string }

  try {
    const directLink = await request.post(`/api/admin/lists/${list.id}/items`, {
      data: {
        sourceType: 'LINK', articleId: null, title: `直接链接-${suffix}`, subtitle: null,
        url: 'https://example.com/direct', imagePath: null, imageResourceId: null,
        openMode: 'DEFAULT', sortOrder: 0, enabled: true, extraJson: null,
      },
    })
    expect(directLink.ok()).toBeTruthy()

    for (const [article, sortOrder] of [[internalArticle, 1], [externalArticle, 2]] as const) {
      const response = await request.post(`/api/admin/lists/${list.id}/items`, {
        data: {
          sourceType: 'ARTICLE', articleId: article.id, title: '', subtitle: null,
          url: null, imagePath: null, imageResourceId: null,
          openMode: 'DEFAULT', sortOrder, enabled: true, extraJson: null,
        },
      })
      expect(response.ok()).toBeTruthy()
    }

    await page.goto('/admin/lists')
    await page.getByTestId(`cms-list-${list.code}`).click()
    const table = page.getByTestId('cms-list-item-table')
    await expect(table.getByRole('columnheader', { name: '数据来源' })).toBeVisible()

    const directRow = table.getByRole('row').filter({ hasText: `直接链接-${suffix}` })
    await expect(directRow).toContainText('链接')

    const internalRow = table.getByRole('row').filter({ hasText: internalArticle.title })
    await expect(internalRow).toContainText('站内文章')
    await expect(internalRow).toContainText(`站内文章 #${internalArticle.id}`)

    const externalRow = table.getByRole('row').filter({ hasText: externalArticle.title })
    await expect(externalRow).toContainText('外链文章')
    await expect(externalRow).toContainText(`外链文章 #${externalArticle.id}`)

    await externalRow.getByRole('button', { name: '编辑' }).click()
    const dialog = page.getByRole('dialog', { name: '编辑列表项' })
    await expect(dialog.getByText('数据来源', { exact: true })).toBeVisible()
    await expect(dialog.getByTestId('list-item-source-type')).toContainText('直接链接')
    await expect(dialog.getByTestId('list-item-source-type')).toContainText('引用文章')
    await expect(dialog.getByTestId('list-item-source-type').locator('input').first()).toBeDisabled()
    await expect(dialog.getByTestId('list-item-source-type-immutable-hint')).toBeVisible()
    await expect(dialog.getByTestId('list-item-article')).toContainText('外链文章')
    await expect(dialog.getByText(new RegExp(`当前文章：${externalArticle.title}（外链文章 · 草稿）`))).toBeVisible()
    await dialog.getByRole('button', { name: '取消' }).click()
  } finally {
    await request.delete(`/api/admin/lists/${list.id}`)
  }
})
