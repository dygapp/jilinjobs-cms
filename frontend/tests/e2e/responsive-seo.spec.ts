import { expect, test } from '@playwright/test'

test('EU-06 公开三级页面具备响应式、直接访问与基础页面信息', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.project.name}-${testInfo.retry}`
  const columnName = `响应式栏目-${suffix}`
  const navigationName = `移动导航-${suffix}`
  const title = `响应式与搜索引擎友好文章-${suffix}`
  const descriptionText = '这是一段用于验证详情页摘要信息、响应式正文和稳定直接访问的公开内容。'

  const columnResponse = await request.post('/api/admin/columns', {
    data: { parentId: null, name: columnName, sortOrder: 10, enabled: true },
  })
  expect(columnResponse.ok()).toBeTruthy()
  const column = await columnResponse.json() as { id: number }

  const navigationResponse = await request.post('/api/admin/navigations', {
    data: {
      name: navigationName,
      position: 'MAIN',
      category: null,
      targetType: 'COLUMN',
      targetColumnId: column.id,
      targetUrl: null,
      sortOrder: 10,
      enabled: true,
    },
  })
  expect(navigationResponse.ok()).toBeTruthy()

  const imageResponse = await request.post('/api/admin/resources', {
    multipart: {
      file: {
        name: `responsive-${suffix}.png`,
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
      bodyHtml: `<p>${descriptionText}</p><p><img width="1200" alt="响应式图片" src="/api/admin/resources/${image.id}/content"></p>`,
      source: '吉林就业',
      publishDate: '2026-08-21',
      pinned: false,
      recommended: true,
      sortOrder: 10,
      coverResourceId: null,
      bodyImageResourceIds: [image.id],
      attachmentResourceIds: [],
    },
  })
  expect(articleResponse.ok()).toBeTruthy()
  const article = await articleResponse.json() as { id: number }
  expect((await request.post(`/api/admin/articles/${article.id}/publish`)).ok()).toBeTruthy()

  const directColumnResponse = await request.get(`/columns/${column.id}`)
  expect(directColumnResponse.ok()).toBeTruthy()
  expect(await directColumnResponse.text()).toContain('<div id="app"></div>')

  const directArticleResponse = await request.get(`/articles/${article.id}`)
  expect(directArticleResponse.ok()).toBeTruthy()
  expect(await directArticleResponse.text()).toContain('<div id="app"></div>')

  const viewports = [
    { name: '桌面', width: 1280, height: 800 },
    { name: '平板', width: 834, height: 1112 },
    { name: '手机', width: 390, height: 844 },
  ]

  for (const viewport of viewports) {
    await test.step(`${viewport.name}主要页面 smoke`, async () => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height })
      await page.goto('/')
      await expect(page).toHaveTitle('吉林就业信息发布原型')
      await expect(page.locator('meta[name="description"]')).toHaveAttribute('content', /吉林省智慧就业云平台中心主站/)

      const navigationToggle = page.getByRole('button', { name: '展开导航' })
      const navigationLink = page.getByRole('link', { name: navigationName, exact: true })
      if (viewport.width <= 720) {
        await expect(navigationToggle).toBeVisible()
        await expect(navigationLink).toBeHidden()
        await navigationToggle.click()
        await expect(page.getByRole('button', { name: '收起导航' })).toHaveAttribute('aria-expanded', 'true')
        await expect(navigationLink).toBeVisible()
      } else {
        await expect(navigationToggle).toBeHidden()
        await expect(navigationLink).toBeVisible()
      }

      await navigationLink.click()
      await expect(page).toHaveURL(new RegExp(`/columns/${column.id}$`))
      await expect(page).toHaveTitle(`${columnName} - 吉林就业信息发布原型`)
      await expect(page.locator('meta[name="description"]')).toHaveAttribute('content', new RegExp(columnName))
      await expect(page.getByTestId(`column-article-${article.id}`)).toBeVisible()

      await page.getByTestId(`column-article-${article.id}`).click()
      await expect(page).toHaveURL(new RegExp(`/articles/${article.id}$`))
      await expect(page).toHaveTitle(`${title} - 吉林就业信息发布原型`)
      await expect(page.locator('meta[name="description"]')).toHaveAttribute('content', descriptionText)
      await expect(page.getByTestId('public-article-body').locator('img')).toBeVisible()

      const hasHorizontalOverflow = await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth)
      expect(hasHorizontalOverflow).toBeFalsy()

      await page.goBack()
      await expect(page).toHaveURL(new RegExp(`/columns/${column.id}$`))
      await page.goForward()
      await expect(page).toHaveURL(new RegExp(`/articles/${article.id}$`))
    })
  }

  await page.goto(`/articles/${article.id}`)
  await expect(page.getByTestId('public-article-title')).toHaveText(title)
})
