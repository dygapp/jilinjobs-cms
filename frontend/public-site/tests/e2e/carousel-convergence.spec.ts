import { expect, test, type APIRequestContext } from '@playwright/test'

const onePixelPng = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Z0WQAAAAASUVORK5CYII=',
  'base64',
)

type PublicListItem = {
  id: number
  articleId: number | null
  sourceType: string
  title: string
  effectiveImageResourceId: number | null
}

type PublicList = { code: string; items: PublicListItem[] }

async function siteConfig(request: APIRequestContext) {
  const response = await request.get('/api/admin/site-config')
  expect(response.ok()).toBeTruthy()
  return response.json() as Promise<Array<{ key: string; value: string }>>
}

async function setSiteConfig(request: APIRequestContext, key: string, value: string) {
  const response = await request.put(`/api/admin/site-config/${key}`, { data: { value } })
  expect(response.ok()).toBeTruthy()
}

async function publicListByCode(request: APIRequestContext, code: string): Promise<PublicList> {
  const response = await request.get('/api/public/lists')
  expect(response.ok()).toBeTruthy()
  const lists = await response.json() as PublicList[]
  const list = lists.find(item => item.code === code)
  expect(list, `公开列表不存在：${code}`).toBeTruthy()
  return list!
}

test('EU-30：Main 与 Party 共用轮播展示参数并在 reduced-motion 下保留手动切换', async ({ page, request }) => {
  const config = await siteConfig(request)
  expect(config.find(item => item.key === 'HOME_CAROUSEL_INTERVAL_SECONDS')).toBeUndefined()
  expect(config.find(item => item.key === 'CAROUSEL_INTERVAL_SECONDS')?.value).toBe('4')
  expect(config.find(item => item.key === 'CAROUSEL_MAX_ITEMS')?.value).toBe('5')

  const originalInterval = config.find(item => item.key === 'CAROUSEL_INTERVAL_SECONDS')?.value || '4'
  const originalMaxItems = config.find(item => item.key === 'CAROUSEL_MAX_ITEMS')?.value || '5'
  try {
    await setSiteConfig(request, 'CAROUSEL_INTERVAL_SECONDS', '1')
    await setSiteConfig(request, 'CAROUSEL_MAX_ITEMS', '2')
    await page.emulateMedia({ reducedMotion: 'reduce' })

    await page.goto('/')
    const mainCarousel = page.getByTestId('home-carousel-active')
    await expect(mainCarousel).toBeVisible()
    await expect(mainCarousel.locator('.home-carousel-dots button')).toHaveCount(2)
    const mainInitialId = await mainCarousel.getAttribute('data-carousel-item-id')
    expect(mainInitialId).toBeTruthy()
    await page.waitForTimeout(1400)
    await expect(mainCarousel).toHaveAttribute('data-carousel-item-id', mainInitialId!)
    await mainCarousel.locator('.home-carousel-dots button').nth(1).click()
    await expect(mainCarousel).not.toHaveAttribute('data-carousel-item-id', mainInitialId!)

    await page.goto('/party/')
    const partyCarousel = page.getByTestId('party-carousel')
    await expect(partyCarousel).toBeVisible()
    await expect(partyCarousel.locator('.party-carousel-dots button')).toHaveCount(2)
    const partyActiveBefore = await partyCarousel.locator('.party-carousel-item.active').getAttribute('data-testid')
    expect(partyActiveBefore).toBeTruthy()
    await page.waitForTimeout(1400)
    await expect(partyCarousel.locator('.party-carousel-item.active')).toHaveAttribute('data-testid', partyActiveBefore!)
    await partyCarousel.locator('.party-carousel-dots button').nth(1).click()
    await expect(partyCarousel.locator('.party-carousel-item.active')).not.toHaveAttribute('data-testid', partyActiveBefore!)
  } finally {
    await setSiteConfig(request, 'CAROUSEL_INTERVAL_SECONDS', originalInterval)
    await setSiteConfig(request, 'CAROUSEL_MAX_ITEMS', originalMaxItems)
  }
})

test('EU-30：ARTICLE 列表投放保持文章单一栏目归属并随发布状态控制公开轮播', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const config = await siteConfig(request)
  const originalMaxItems = config.find(item => item.key === 'CAROUSEL_MAX_ITEMS')?.value || '5'
  await setSiteConfig(request, 'CAROUSEL_MAX_ITEMS', '50')

  try {
    const columnsResponse = await request.get('/api/admin/columns')
    expect(columnsResponse.ok()).toBeTruthy()
    const columns = await columnsResponse.json() as Array<{ id: number; parentId: number | null; alias: string; name: string; preset: boolean }>
    const party = columns.find(item => item.alias === 'party')
    const theme = columns.find(item => item.alias === 'party-theme-education')
    expect(party).toBeTruthy()
    expect(theme).toMatchObject({ name: '主题教育', parentId: party!.id, preset: true })

    const imageResponse = await request.post('/api/admin/resources', {
      multipart: {
        file: { name: `carousel-override-${suffix}.png`, mimeType: 'image/png', buffer: onePixelPng },
      },
    })
    expect(imageResponse.ok()).toBeTruthy()
    const image = await imageResponse.json() as { id: number }

    const title = `主题教育轮播文章-${suffix}`
    const articleResponse = await request.post('/api/admin/articles', {
      data: {
        columnId: theme!.id,
        title,
        bodyHtml: `<p>${title} 正文</p>`,
        source: 'EU-30 E2E',
        articleType: 'INTERNAL',
        externalUrl: null,
        publishDate: '2026-09-04',
        pinned: false,
        recommended: false,
        sortOrder: 0,
        coverResourceId: null,
        bodyImageResourceIds: [],
        attachmentResourceIds: [],
      },
    })
    expect(articleResponse.ok()).toBeTruthy()
    const article = await articleResponse.json() as { id: number; columnId: number; title: string }
    expect(article.columnId).toBe(theme!.id)
    expect((await request.post(`/api/admin/articles/${article.id}/publish`)).ok()).toBeTruthy()

    const listsResponse = await request.get('/api/admin/lists')
    expect(listsResponse.ok()).toBeTruthy()
    const lists = await listsResponse.json() as Array<{ id: number; code: string }>
    const carousel = lists.find(item => item.code === 'PARTY_CAROUSEL')
    expect(carousel).toBeTruthy()

    const placementResponse = await request.post(`/api/admin/lists/${carousel!.id}/items`, {
      data: {
        sourceType: 'ARTICLE',
        articleId: article.id,
        title,
        subtitle: null,
        url: null,
        imagePath: null,
        imageResourceId: image.id,
        openMode: 'DEFAULT',
        sortOrder: 999,
        enabled: true,
        extraJson: null,
      },
    })
    expect(placementResponse.ok()).toBeTruthy()
    const placement = await placementResponse.json() as { id: number; articleId: number; sourceType: string; imageResourceId: number }
    expect(placement).toMatchObject({ articleId: article.id, sourceType: 'ARTICLE', imageResourceId: image.id })

    const publicList = await publicListByCode(request, 'PARTY_CAROUSEL')
    expect(publicList.items.find(item => item.id === placement.id)).toMatchObject({
      articleId: article.id,
      sourceType: 'ARTICLE',
      title,
      effectiveImageResourceId: image.id,
    })
    expect((await request.get(`/api/public/resources/${image.id}/content`)).ok()).toBeTruthy()

    await page.goto('/party/')
    const partyCarousel = page.getByTestId('party-carousel')
    const carouselItem = page.getByTestId(`party-carousel-item-${placement.id}`)
    await expect(carouselItem).toHaveCount(1)
    await partyCarousel.getByRole('button', { name: new RegExp(title) }).click()
    await expect(carouselItem).toBeVisible()
    await expect(carouselItem.getByRole('link')).toHaveAttribute('href', `/party/article/${article.id}`)
    await carouselItem.getByRole('link').click()
    await expect(page).toHaveURL(new RegExp(`/party/article/${article.id}$`))
    await expect(page.getByTestId('party-article-title')).toHaveText(title)

    const persistedResponse = await request.get(`/api/admin/articles/${article.id}`)
    expect(persistedResponse.ok()).toBeTruthy()
    const persisted = await persistedResponse.json() as { columnId: number }
    expect(persisted.columnId).toBe(theme!.id)

    expect((await request.post(`/api/admin/articles/${article.id}/withdraw`)).ok()).toBeTruthy()
    const withdrawn = await publicListByCode(request, 'PARTY_CAROUSEL')
    expect(withdrawn.items.some(item => item.id === placement.id)).toBeFalsy()
    expect((await request.get(`/api/public/resources/${image.id}/content`)).status()).toBe(404)
  } finally {
    await setSiteConfig(request, 'CAROUSEL_MAX_ITEMS', originalMaxItems)
  }
})

test('EU-30：REQUIRED ARTICLE 继承图片失效后不再作为有效公开投放', async ({ request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const columnsResponse = await request.get('/api/admin/columns')
  expect(columnsResponse.ok()).toBeTruthy()
  const columns = await columnsResponse.json() as Array<{ id: number; alias: string }>
  const theme = columns.find(item => item.alias === 'party-theme-education')
  expect(theme).toBeTruthy()

  const listsResponse = await request.get('/api/admin/lists')
  expect(listsResponse.ok()).toBeTruthy()
  const lists = await listsResponse.json() as Array<{ id: number; code: string }>
  const carousel = lists.find(item => item.code === 'PARTY_CAROUSEL')
  expect(carousel).toBeTruthy()

  const imageResponse = await request.post('/api/admin/resources', {
    multipart: {
      file: { name: `carousel-cover-${suffix}.png`, mimeType: 'image/png', buffer: onePixelPng },
    },
  })
  expect(imageResponse.ok()).toBeTruthy()
  const image = await imageResponse.json() as { id: number }

  const title = `继承图片失效-${suffix}`
  const draft = {
    columnId: theme!.id,
    title,
    bodyHtml: `<p>${title} 正文</p>`,
    source: 'EU-30 E2E',
    articleType: 'INTERNAL',
    externalUrl: null,
    publishDate: '2026-09-04',
    pinned: false,
    recommended: false,
    sortOrder: 0,
    coverResourceId: image.id,
    bodyImageResourceIds: [],
    attachmentResourceIds: [],
  }
  const articleResponse = await request.post('/api/admin/articles', { data: draft })
  expect(articleResponse.ok()).toBeTruthy()
  const article = await articleResponse.json() as { id: number }
  expect((await request.post(`/api/admin/articles/${article.id}/publish`)).ok()).toBeTruthy()

  const placementResponse = await request.post(`/api/admin/lists/${carousel!.id}/items`, {
    data: {
      sourceType: 'ARTICLE',
      articleId: article.id,
      title,
      subtitle: null,
      url: null,
      imagePath: null,
      imageResourceId: null,
      openMode: 'DEFAULT',
      sortOrder: 1000,
      enabled: true,
      extraJson: null,
    },
  })
  expect(placementResponse.ok()).toBeTruthy()
  const placement = await placementResponse.json() as { id: number; effectiveImageResourceId: number | null }
  expect(placement.effectiveImageResourceId).toBe(image.id)

  let publicList = await publicListByCode(request, 'PARTY_CAROUSEL')
  expect(publicList.items.some(item => item.id === placement.id)).toBeTruthy()
  expect((await request.get(`/api/public/resources/${image.id}/content`)).ok()).toBeTruthy()

  const removeCover = await request.put(`/api/admin/articles/${article.id}`, {
    data: { ...draft, coverResourceId: null },
  })
  expect(removeCover.ok()).toBeTruthy()

  publicList = await publicListByCode(request, 'PARTY_CAROUSEL')
  expect(publicList.items.some(item => item.id === placement.id)).toBeFalsy()
  expect((await request.get(`/api/public/resources/${image.id}/content`)).status()).toBe(404)

  expect((await request.delete(`/api/admin/lists/${carousel!.id}/items/${placement.id}`)).ok()).toBeTruthy()
  expect((await request.post(`/api/admin/articles/${article.id}/withdraw`)).ok()).toBeTruthy()
})
