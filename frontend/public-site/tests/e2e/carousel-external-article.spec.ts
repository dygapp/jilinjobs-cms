import { expect, test, type APIRequestContext } from '@playwright/test'

const onePixelPng = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Z0WQAAAAASUVORK5CYII=',
  'base64',
)

type AdminList = { id: number; code: string }
type Placement = { listId: number; itemId: number }

async function listByCode(request: APIRequestContext, code: string): Promise<AdminList> {
  const response = await request.get('/api/admin/lists')
  expect(response.ok()).toBeTruthy()
  const list = (await response.json() as AdminList[]).find(item => item.code === code)
  expect(list, `缺少列表 ${code}`).toBeTruthy()
  return list!
}

async function createPlacement(
  request: APIRequestContext,
  code: string,
  articleId: number,
  imageResourceId: number,
  title: string,
): Promise<Placement> {
  const list = await listByCode(request, code)
  const response = await request.post(`/api/admin/lists/${list.id}/items`, {
    data: {
      sourceType: 'ARTICLE',
      articleId,
      title,
      subtitle: null,
      url: null,
      imagePath: null,
      imageResourceId,
      openMode: 'DEFAULT',
      sortOrder: -300000,
      enabled: true,
      extraJson: null,
    },
  })
  expect(response.ok()).toBeTruthy()
  const item = await response.json() as { id: number; url: string | null; sourceType: string }
  expect(item).toMatchObject({ sourceType: 'ARTICLE', url: null })
  return { listId: list.id, itemId: item.id }
}

test('EU-30：EXTERNAL_LINK ARTICLE 轮播使用文章当前外链而不是列表 URL', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const columnsResponse = await request.get('/api/admin/columns')
  expect(columnsResponse.ok()).toBeTruthy()
  const columns = await columnsResponse.json() as Array<{ id: number; alias: string }>
  const theme = columns.find(item => item.alias === 'party-theme-education')
  expect(theme).toBeTruthy()

  const imageResponse = await request.post('/api/admin/resources', {
    multipart: { file: { name: `external-carousel-${suffix}.png`, mimeType: 'image/png', buffer: onePixelPng } },
  })
  expect(imageResponse.ok()).toBeTruthy()
  const image = await imageResponse.json() as { id: number }

  const title = `外链文章轮播-${suffix}`
  const externalUrl = `https://example.com/eu30-carousel/${suffix}`
  const articleResponse = await request.post('/api/admin/articles', {
    data: {
      columnId: theme!.id,
      title,
      bodyHtml: '',
      source: 'EU-30 E2E',
      articleType: 'EXTERNAL_LINK',
      externalUrl,
      publishDate: '2026-09-05',
      pinned: false,
      recommended: false,
      sortOrder: 0,
      coverResourceId: null,
      bodyImageResourceIds: [],
      attachmentResourceIds: [],
    },
  })
  expect(articleResponse.ok()).toBeTruthy()
  const article = await articleResponse.json() as { id: number }
  expect((await request.post(`/api/admin/articles/${article.id}/publish`)).ok()).toBeTruthy()

  const placements: Placement[] = []
  try {
    placements.push(await createPlacement(request, 'HOME_CAROUSEL', article.id, image.id, title))
    placements.push(await createPlacement(request, 'PARTY_CAROUSEL', article.id, image.id, title))

    await page.emulateMedia({ reducedMotion: 'reduce' })
    await page.goto('/')
    const mainLink = page.getByTestId('home-carousel-active').getByRole('link', { name: new RegExp(title) })
    await expect(mainLink).toHaveAttribute('href', externalUrl)
    await expect(mainLink).toHaveAttribute('target', '_blank')

    await page.goto('/party/')
    const partyItem = page.getByTestId(`party-carousel-item-${placements[1].itemId}`)
    await expect(partyItem).toBeVisible()
    const partyLink = partyItem.getByRole('link', { name: new RegExp(title) })
    await expect(partyLink).toHaveAttribute('href', externalUrl)
    await expect(partyLink).toHaveAttribute('target', '_blank')
  } finally {
    for (const placement of placements) {
      const response = await request.delete(`/api/admin/lists/${placement.listId}/items/${placement.itemId}`)
      expect(response.ok()).toBeTruthy()
    }
    await request.post(`/api/admin/articles/${article.id}/withdraw`)
  }
})
