import { expect, test, type APIRequestContext } from '@playwright/test'

type CreatedArticle = { id: number; title: string }

async function createColumn(request: APIRequestContext, name: string): Promise<{ id: number }> {
  const response = await request.post('/api/admin/columns', {
    data: { parentId: null, name, sortOrder: 10, enabled: true },
  })
  expect(response.ok()).toBeTruthy()
  return response.json() as Promise<{ id: number }>
}

async function baselineColumn(request: APIRequestContext, alias: string): Promise<{ id: number; name: string }> {
  const response = await request.get('/api/admin/columns')
  expect(response.ok()).toBeTruthy()
  const columns = await response.json() as Array<{ id: number; alias: string; name: string }>
  const column = columns.find(item => item.alias === alias)
  expect(column, `缺少基线栏目 ${alias}`).toBeTruthy()
  return column as { id: number; name: string }
}

async function createAndPublishArticle(
  request: APIRequestContext,
  input: { columnId: number; title: string; pinned?: boolean; recommended?: boolean; sortOrder?: number },
): Promise<CreatedArticle> {
  const createResponse = await request.post('/api/admin/articles', {
    data: {
      columnId: input.columnId,
      title: input.title,
      bodyHtml: `<p>${input.title} 正文</p>`,
      source: 'Feature-wide convergence verification',
      publishDate: '2026-08-24',
      pinned: input.pinned ?? false,
      recommended: input.recommended ?? false,
      sortOrder: input.sortOrder ?? 0,
      coverResourceId: null,
      bodyImageResourceIds: [],
      attachmentResourceIds: [],
    },
  })
  expect(createResponse.ok()).toBeTruthy()
  const article = await createResponse.json() as CreatedArticle
  expect((await request.post(`/api/admin/articles/${article.id}/publish`)).ok()).toBeTruthy()
  return article
}

test('Feature-wide closure：栏目内容超过单页容量时可以完整分页浏览', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const columnName = `分页验证栏目-${suffix}`
  const column = await createColumn(request, columnName)
  const articles: CreatedArticle[] = []

  for (let index = 0; index < 11; index += 1) {
    articles.push(await createAndPublishArticle(request, {
      columnId: column.id,
      title: `分页文章-${index}-${suffix}`,
      sortOrder: index,
    }))
  }

  const highest = articles[10]
  const lowest = articles[0]
  await page.goto(`/columns/${column.id}`)
  await expect(page.getByRole('heading', { name: columnName, exact: true })).toBeVisible()
  await expect(page.getByTestId(`column-article-${highest.id}`)).toBeVisible()
  await expect(page.getByTestId(`column-article-${lowest.id}`)).toHaveCount(0)
  await expect(page.getByRole('button', { name: '第 1 页' })).toHaveAttribute('aria-current', 'page')
  await page.getByRole('button', { name: '下一页' }).click()
  await expect(page).toHaveURL(new RegExp(`/columns/${column.id}\\?page=1$`))
  await expect(page.getByRole('button', { name: '第 2 页' })).toHaveAttribute('aria-current', 'page')
  await expect(page.getByTestId(`column-article-${lowest.id}`)).toBeVisible()
  await page.getByRole('button', { name: '上一页' }).click()
  await expect(page.getByRole('button', { name: '第 1 页' })).toHaveAttribute('aria-current', 'page')
  await expect(page.getByTestId(`column-article-${highest.id}`)).toBeVisible()
})

test('Feature-wide closure：首页通知公告按置顶推荐和展示顺序组织已发布内容', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const column = await baselineColumn(request, 'notice')
  const low = await createAndPublishArticle(request, { columnId: column.id, title: `普通低排序-${suffix}`, sortOrder: 10 })
  const recommended = await createAndPublishArticle(request, { columnId: column.id, title: `推荐文章-${suffix}`, recommended: true, sortOrder: 0 })
  const high = await createAndPublishArticle(request, { columnId: column.id, title: `普通高排序-${suffix}`, sortOrder: 200 })
  const pinned = await createAndPublishArticle(request, { columnId: column.id, title: `置顶文章-${suffix}`, pinned: true, sortOrder: 0 })

  await page.goto('/')
  const group = page.locator('.notice-panel')
  await expect(group).toBeVisible()
  const texts = await group.locator('li a').allTextContents()
  const selected = texts.filter(text => [pinned.title, recommended.title, high.title, low.title].includes(text))
  expect(selected).toEqual([pinned.title, recommended.title, high.title, low.title])
})

test('Feature-wide closure：招聘公告外链文章只保存基础信息并直接跳转原站', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const column = await baselineColumn(request, 'recruitment-announcement')
  const title = `外链招聘公告-${suffix}`
  const externalUrl = `https://example.com/recruitment/${suffix}`
  const createResponse = await request.post('/api/admin/articles', {
    data: {
      columnId: column.id,
      title,
      bodyHtml: '<p>这段正文不应被保存</p>',
      source: '外部招聘平台',
      articleType: 'EXTERNAL_LINK',
      externalUrl,
      publishDate: '2026-08-28',
      pinned: true,
      recommended: false,
      sortOrder: 9999,
      coverResourceId: null,
      bodyImageResourceIds: [],
      attachmentResourceIds: [],
    },
  })
  expect(createResponse.ok()).toBeTruthy()
  const created = await createResponse.json() as { id: number }
  expect((await request.post(`/api/admin/articles/${created.id}/publish`)).ok()).toBeTruthy()

  const adminResponse = await request.get(`/api/admin/articles/${created.id}`)
  expect(adminResponse.ok()).toBeTruthy()
  const stored = await adminResponse.json() as { articleType: string; externalUrl: string; bodyHtml: string; source: string }
  expect(stored.articleType).toBe('EXTERNAL_LINK')
  expect(stored.externalUrl).toBe(externalUrl)
  expect(stored.bodyHtml).toBe('')
  expect(stored.source).toBe('外部招聘平台')

  await page.goto('/')
  const homeLink = page.getByTestId(`recruitment-external-${created.id}`)
  await expect(homeLink).toHaveAttribute('href', externalUrl)
  await expect(homeLink).toHaveAttribute('target', '_blank')

  await page.goto('/column/recruitment-announcement')
  const columnLink = page.getByTestId(`column-article-${created.id}`)
  await expect(columnLink).toHaveAttribute('href', externalUrl)
  await expect(columnLink).toHaveAttribute('target', '_blank')
})

test('Feature-wide closure：HOME_QUICK 与通用列表进入原站快速导航和网站导航区域', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const quickFirst = `快速入口A-${suffix}`
  const quickSecond = `快速入口B-${suffix}`
  const siteName = `站点导航-${suffix}`

  for (const row of [
    { name: quickSecond, sortOrder: 20, targetUrl: 'https://example.com/service-b' },
    { name: quickFirst, sortOrder: 10, targetUrl: 'https://example.com/service-a' },
  ]) {
    const response = await request.post('/api/admin/navigations', {
      data: {
        parentId: null,
        name: row.name,
        position: 'HOME_QUICK',
        category: null,
        targetType: 'LINK',
        targetColumnId: null,
        targetPageId: null,
        targetUrl: row.targetUrl,
        openMode: 'DEFAULT',
        sortOrder: row.sortOrder,
        enabled: true,
      },
    })
    expect(response.ok()).toBeTruthy()
  }

  const listsResponse = await request.get('/api/admin/lists')
  expect(listsResponse.ok()).toBeTruthy()
  const lists = await listsResponse.json() as Array<{ id: number; code: string; name: string }>
  const siteList = lists.find(item => item.code === 'SITE_RELATED')
  expect(siteList, '缺少 SITE_RELATED 通用列表基线').toBeTruthy()

  const siteItemResponse = await request.post(`/api/admin/lists/${siteList!.id}/items`, {
    data: {
      title: siteName,
      subtitle: null,
      url: 'https://example.com/site',
      imagePath: null,
      openMode: 'DEFAULT',
      sortOrder: 999,
      enabled: true,
      extraJson: null,
    },
  })
  expect(siteItemResponse.ok()).toBeTruthy()

  await page.goto('/')
  const serviceSection = page.locator('.service-panel')
  await expect(serviceSection.getByRole('heading', { name: '快速导航', exact: true })).toBeVisible()
  await expect(serviceSection.getByRole('link', { name: quickFirst, exact: true })).toHaveAttribute('href', 'https://example.com/service-a')
  await expect(serviceSection.getByRole('link', { name: quickSecond, exact: true })).toHaveAttribute('href', 'https://example.com/service-b')

  const siteSection = page.locator('.site-navigation')
  await expect(siteSection.getByRole('heading', { name: '网站导航', exact: true })).toBeVisible()
  await expect(siteSection.getByRole('button', { name: siteList!.name, exact: true })).toHaveClass(/active/)
  await expect(siteSection.getByRole('link', { name: siteName, exact: true })).toHaveAttribute('href', 'https://example.com/site')
})

test('Feature-wide closure：广告位支持多图轮动、NO_LINK 保留 URL 并按有效期过滤', async ({ page, request }, testInfo) => {
  const slotsResponse = await request.get('/api/admin/advertisements/slots')
  expect(slotsResponse.ok()).toBeTruthy()
  const slots = await slotsResponse.json() as Array<{ id:number;code:string }>
  const slot = slots.find(item => item.code === 'HOME_RECRUITMENT_PROMO')
  expect(slot, '缺少 HOME_RECRUITMENT_PROMO 广告位').toBeTruthy()
  const suffix = `${Date.now()}-${testInfo.retry}`
  const createdIds:number[] = []
  const createAd = async (data: Record<string, unknown>) => {
    const response = await request.post(`/api/admin/advertisements/slots/${slot!.id}/items`, { data })
    expect(response.ok()).toBeTruthy()
    const created = await response.json() as { id:number }
    createdIds.push(created.id)
    return created
  }

  try {
    const retainedUrl = `https://example.com/promo/${suffix}`
    const first = await createAd({title:`禁用跳转-${suffix}`,imagePath:'/static/home/recruitment-campaign.png',url:retainedUrl,openMode:'NO_LINK',startAt:null,endAt:null,sortOrder:-200,enabled:true})
    const second = await createAd({title:`轮动广告-${suffix}`,imagePath:'/static/home/recruitment-campaign.png',url:`https://example.com/promo-next/${suffix}`,openMode:'NEW_WINDOW',startAt:null,endAt:null,sortOrder:-190,enabled:true})
    const expired = await createAd({title:`过期广告-${suffix}`,imagePath:'/static/home/recruitment-campaign.png',url:null,openMode:'NO_LINK',startAt:'2020-01-01T00:00:00',endAt:'2020-01-02T00:00:00',sortOrder:-300,enabled:true})

    const publicResponse = await request.get('/api/public/advertisements')
    expect(publicResponse.ok()).toBeTruthy()
    const publicSlots = await publicResponse.json() as Array<{code:string;advertisements:Array<{id:number;sortOrder:number}>}>
    const publicAds = publicSlots.find(item => item.code === 'HOME_RECRUITMENT_PROMO')?.advertisements || []
    expect(publicAds.map(item => item.id)).toContain(first.id)
    expect(publicAds.map(item => item.id)).toContain(second.id)
    expect(publicAds.map(item => item.id)).not.toContain(expired.id)
    expect(publicAds.findIndex(item => item.id === first.id)).toBeLessThan(publicAds.findIndex(item => item.id === second.id))

    await page.goto('/')
    const noLinkVisual = page.getByTestId(`home-promo-ad-${first.id}`)
    await expect(noLinkVisual).toBeVisible()
    await expect(noLinkVisual).not.toHaveAttribute('href')

    const updateResponse = await request.put(`/api/admin/advertisements/slots/${slot!.id}/items/${first.id}`, {
      data: {title:`禁用跳转-${suffix}`,imagePath:'/static/home/recruitment-campaign.png',url:retainedUrl,openMode:'NEW_WINDOW',startAt:null,endAt:null,sortOrder:-200,enabled:true},
    })
    expect(updateResponse.ok()).toBeTruthy()
    const updated = await updateResponse.json() as { url:string;openMode:string }
    expect(updated.url).toBe(retainedUrl)
    expect(updated.openMode).toBe('NEW_WINDOW')

    await page.reload()
    const restoredLink = page.getByTestId(`home-promo-ad-${first.id}`)
    await expect(restoredLink).toHaveAttribute('href', retainedUrl)
    await expect(restoredLink).toHaveAttribute('target', '_blank')
    await expect(page.getByTestId(`home-promo-ad-${second.id}`)).toBeVisible({ timeout: 7000 })
  } finally {
    for (const id of createdIds) await request.delete(`/api/admin/advertisements/slots/${slot!.id}/items/${id}`)
  }
})

test('Feature-wide closure：首页举报电话及邮箱使用站内固定页面', async ({ page }) => {
  await page.goto('/')
  const link = page.locator('.home-top-shortcuts').getByRole('link', { name: '举报电话及邮箱', exact: true })
  await expect(link).toHaveAttribute('href', '/page/employment-report-contact')
  await expect(link).not.toHaveAttribute('target', '_blank')
  await link.click()
  await expect(page).toHaveURL(/\/page\/employment-report-contact$/)
  await expect(page.getByRole('heading', { name: '举报电话及邮箱', exact: true })).toBeVisible()
  const body = page.locator('.fixed-page-body')
  await expect(body).toContainText('0431-84657570')
  await expect(body).toContainText('0431-84657571')
  await expect(body).toContainText('xxb@jilinjobs.cn')
})

test('Feature-wide closure：页脚备案图标、非链接官方标识、配置版权与 favicon 可用', async ({ page, request }) => {
  for (const resource of [
    '/static/footer/public-security-record.png',
    '/static/footer/public-institution.png',
    '/static/footer/wechat-qr.png',
    '/static/brand/site-favicon.png',
  ]) {
    const response = await request.get(resource)
    expect(response.ok(), `${resource} 应来自版本化静态资源包`).toBeTruthy()
    expect((await response.body()).length).toBeGreaterThan(100)
  }

  const configResponse = await request.get('/api/public/site-config')
  expect(configResponse.ok()).toBeTruthy()
  const config = await configResponse.json() as Array<{ key: string; value: string }>
  const copyright = config.find(item => item.key === 'FOOTER_COPYRIGHT')?.value
  expect(copyright, '缺少 FOOTER_COPYRIGHT 网站属性').toBeTruthy()

  await page.goto('/')
  await expect(page.locator('link[rel="icon"]')).toHaveAttribute('href', '/static/brand/site-favicon.png')
  await expect(page.locator('.public-security-record img')).toHaveAttribute('src', '/static/footer/public-security-record.png')
  await expect(page.locator('.public-institution-badge img')).toHaveAttribute('src', '/static/footer/public-institution.png')
  await expect(page.locator('.wechat-entry img')).toHaveAttribute('src', '/static/footer/wechat-qr.png')
  await expect(page.locator('.public-security-record')).toContainText('吉公网安备 22010702000243号')
  await expect(page.locator('.public-institution-badge')).not.toHaveAttribute('href')
  await expect(page.locator('.wechat-entry')).not.toHaveAttribute('href')
  await expect(page.locator('.site-footer')).toContainText(copyright!)

  const footerLayout = await page.locator('.site-footer-layout').evaluate(el => getComputedStyle(el).display)
  expect(footerLayout).toBe('flex')
})
