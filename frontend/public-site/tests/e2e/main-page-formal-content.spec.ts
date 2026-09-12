import { expect, test } from '@playwright/test'

const formalPages = [
  { route: '/page/about', marker: '中心主要工作职责' },
  { route: '/page/budget', marker: '2026 年部门预算' },
  { route: '/page/teacher-library', marker: '职业生涯规划与就业指导' },
  { route: '/page/employment-report-contact', marker: 'xxb@jilinjobs.cn' },
  { route: '/page/guide/contact', marker: '金川街151号' },
  { route: '/page/guide/dagl', marker: '高校毕业生档案管理' },
  { route: '/page/guide/faq', marker: '学历认证项目有哪些' },
  { route: '/page/guide/dygl', marker: '党组织关系转出' },
  { route: '/page/guide/jypq', marker: '关于停止毕业生就业报到证业务办理的通告' },
  { route: '/page/guide/xlrz', marker: '网上申请' },
] as const

const budgetPdfNames = [
  'budget-publication-rules.pdf',
  'budget-2021.pdf',
  'budget-2022.pdf',
  'budget-2023.pdf',
  'budget-2024.pdf',
  'budget-2025.pdf',
  'budget-2026.pdf',
  'final-accounts-2020.pdf',
  'final-accounts-2021.pdf',
  'final-accounts-2022.pdf',
  'final-accounts-2023.pdf',
  'final-accounts-2024.pdf',
  'final-accounts-2025.pdf',
] as const

const dispatchCardTitles = [
  '关于停止毕业生就业报到证业务办理的通告',
  '关于做好取消普通高等学校毕业生就业报到证有关衔接工作的通知',
  '国务院办公厅关于进一步做好高校毕业生等青年就业创业工作的通知',
] as const

test('Main formal Pages render accepted package content on all ten stable routes', async ({ page }) => {
  for (const item of formalPages) {
    await page.goto(item.route)
    const content = item.route === '/page/guide/jypq'
      ? page.locator('[data-page-renderer="JILINJOBS_GUIDE_CARDS"]')
      : page.locator('.rich-content')
    await expect(content).toBeVisible()
    await expect(content).toContainText(item.marker)
    await expect(page.locator('.error-text')).toHaveCount(0)

    const html = await content.innerHTML()
    expect(html).not.toContain('migration-resource://')
    expect(html).not.toContain('zhjy.jilinjobs.cn:8080/group1/cms/')
  }
})

test('guide/jypq renders the accepted three-card Structured contract as collapsed interactive cards with centered package images', async ({ page, request }) => {
  await page.goto('/page/guide/jypq')

  const renderer = page.locator('[data-page-renderer="JILINJOBS_GUIDE_CARDS"]')
  await expect(renderer).toBeVisible()
  await expect(renderer).toHaveAttribute('data-page-structured-kind', 'CARD_COLLECTION')
  await expect(renderer).toHaveAttribute('data-page-schema-version', '1')

  const cards = renderer.locator('.guide-card')
  await expect(cards).toHaveCount(3)
  expect(await cards.locator('.guide-card-title').allTextContents()).toEqual([...dispatchCardTitles])

  for (let index = 0; index < 3; index += 1) {
    await expect(cards.nth(index)).toHaveAttribute('data-card-state', 'collapsed')
    await expect(renderer.getByTestId(`guide-card-toggle-${index}`)).toHaveAttribute('aria-expanded', 'false')
    await expect(renderer.getByTestId(`guide-card-body-${index}`)).toBeHidden()
  }

  const firstToggle = renderer.getByTestId('guide-card-toggle-0')
  const firstBody = renderer.getByTestId('guide-card-body-0')
  await firstToggle.click()
  await expect(firstToggle).toHaveAttribute('aria-expanded', 'true')
  await expect(cards.nth(0)).toHaveAttribute('data-card-state', 'expanded')
  await expect(firstBody).toBeVisible()
  await expect(renderer.getByTestId('guide-card-body-1')).toBeHidden()
  await expect(renderer.getByTestId('guide-card-body-2')).toBeHidden()

  await firstToggle.click()
  await expect(firstToggle).toHaveAttribute('aria-expanded', 'false')
  await expect(cards.nth(0)).toHaveAttribute('data-card-state', 'collapsed')
  await expect(firstBody).toBeHidden()

  const secondToggle = renderer.getByTestId('guide-card-toggle-1')
  const secondBody = renderer.getByTestId('guide-card-body-1')
  await secondToggle.click()
  await expect(secondToggle).toHaveAttribute('aria-expanded', 'true')
  await expect(secondBody).toBeVisible()

  const images = secondBody.locator('img[src^="/static/pages/guide/jypq/"]')
  await expect(images).toHaveCount(4)
  const bodyBox = await secondBody.boundingBox()
  expect(bodyBox).not.toBeNull()
  const bodyCenter = bodyBox!.x + bodyBox!.width / 2
  for (let index = 0; index < 4; index += 1) {
    const image = images.nth(index)
    await expect(image).toBeVisible()
    const imageBox = await image.boundingBox()
    expect(imageBox).not.toBeNull()
    const imageCenter = imageBox!.x + imageBox!.width / 2
    expect(Math.abs(imageCenter - bodyCenter)).toBeLessThanOrEqual(2)
  }

  const imageSources = await images.evaluateAll(elements => elements.map(element => element.getAttribute('src') || ''))
  expect(new Set(imageSources).size).toBe(4)
  for (const src of imageSources) {
    const response = await request.get(src)
    expect(response.ok(), `${src} must be served`).toBeTruthy()
  }

  await expect(page.locator('[data-unsupported-renderer]')).toHaveCount(0)
  await expect(page).toHaveURL(/\/page\/guide\/jypq$/)
  const canonicalResponse = await request.get('/api/public/page-groups/guide/jypq')
  expect(canonicalResponse.ok()).toBeTruthy()
  const canonicalPage = await canonicalResponse.json() as { canonicalUrl: string }
  expect(canonicalPage.canonicalUrl).toBe('/page/guide/jypq')
})

test('ordinary Rich Page remains on the explicit Rich renderer', async ({ page }) => {
  await page.goto('/page/about')
  const content = page.locator('[data-page-renderer="RICH_TEXT"]')
  await expect(content).toBeVisible()
  await expect(content).toContainText('中心主要工作职责')
  await expect(page.locator('[data-unsupported-renderer]')).toHaveCount(0)
})

test('unknown renderer identity fails closed without rendering bodyHtml', async ({ page }) => {
  await page.route('**/api/public/pages/about', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        id: 999999,
        alias: 'about',
        name: 'Unsupported renderer test',
        bodyHtml: '<p>UNSAFE_FALLBACK_MARKER</p>',
        contentModel: 'RICH_TEXT',
        rendererKey: 'UNSUPPORTED_EU55_TEST',
        contentOwner: 'OPERATOR',
        structuredContent: null,
        renderMode: null,
        embedUrl: null,
        canonicalUrl: '/page/about',
        group: null,
        breadcrumbs: [{ title: '首页', href: '/' }, { title: 'Unsupported renderer test', href: null }],
      }),
    })
  })

  await page.goto('/page/about')
  await expect(page.locator('[data-unsupported-renderer="UNSUPPORTED_EU55_TEST"]')).toBeVisible()
  await expect(page.getByText('当前页面的内容或呈现方式暂不受支持，已停止自动降级渲染。')).toBeVisible()
  await expect(page.getByText('UNSAFE_FALLBACK_MARKER')).toHaveCount(0)
})

test('budget Page exposes all thirteen package-owned PDFs and every target serves PDF bytes', async ({ page, request }) => {
  await page.goto('/page/budget')
  const links = page.locator('.rich-content a[href^="/static/pages/budget/"]')
  await expect(links).toHaveCount(13)

  const hrefs = (await links.evaluateAll(elements => elements.map(element => element.getAttribute('href') || ''))).sort()
  const expected = budgetPdfNames.map(name => `/static/pages/budget/${name}`).sort()
  expect(hrefs).toEqual(expected)

  for (const href of hrefs) {
    const response = await request.get(href)
    expect(response.ok(), `${href} must be served`).toBeTruthy()
    expect(response.headers()['content-type'] || '').toContain('application/pdf')
    const bytes = await response.body()
    expect(bytes.subarray(0, 5).toString()).toBe('%PDF-')
  }
})

test('image-heavy formal Pages use projected package assets that are actually retrievable', async ({ page, request }) => {
  await page.goto('/page/teacher-library')
  const teacherImages = page.locator('.rich-content img[src^="/static/pages/teacher-library/"]')
  await expect(teacherImages).toHaveCount(138)
  const teacherSample = await teacherImages.first().getAttribute('src')
  expect(teacherSample).toBeTruthy()
  const teacherResponse = await request.get(teacherSample!)
  expect(teacherResponse.ok()).toBeTruthy()

  await page.goto('/page/guide/jypq')
  const dispatchImages = page.locator('.guide-card-body img[src^="/static/pages/guide/jypq/"]')
  await expect(dispatchImages).toHaveCount(4)
  for (const src of await dispatchImages.evaluateAll(elements => elements.map(element => element.getAttribute('src') || ''))) {
    const response = await request.get(src)
    expect(response.ok(), `${src} must be served`).toBeTruthy()
  }

  await page.goto('/page/guide/xlrz')
  const certificationImages = page.locator('.rich-content img[src^="/static/pages/guide/xlrz/"]')
  await expect(certificationImages).toHaveCount(1)
  const certificationSrc = await certificationImages.first().getAttribute('src')
  const certificationResponse = await request.get(certificationSrc!)
  expect(certificationResponse.ok()).toBeTruthy()
})

test('FAQ Page promotes its ten top-level questions above answer numbering', async ({ page }) => {
  await page.goto('/page/guide/faq')

  const questions = page.locator('.rich-content > h2')
  await expect(questions).toHaveCount(10)
  await expect(questions.nth(2)).toHaveText('3.可免费申请书面认证报告的高等教育学历证书包括哪些？')
  await expect(questions.nth(3)).toHaveText('4.普通、成人高等教育学历证书认证需要提供哪些材料')
  await expect(questions.nth(4)).toHaveText('5.自学考试学历证书认证提供哪些材料？')

  const answer = page.locator('.rich-content > p').filter({ hasText: '1、毕业证原件' }).first()
  await expect(answer).toBeVisible()
  await expect(questions.nth(2)).toHaveCSS('font-weight', '700')

  const questionFontSize = await questions.nth(2).evaluate(element => Number.parseFloat(getComputedStyle(element).fontSize))
  const answerFontSize = await answer.evaluate(element => Number.parseFloat(getComputedStyle(element).fontSize))
  expect(questionFontSize).toBeGreaterThan(answerFontSize)
})
