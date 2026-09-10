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

test('Main formal Pages render accepted package content on all ten stable routes', async ({ page }) => {
  for (const item of formalPages) {
    await page.goto(item.route)
    const content = page.locator('.rich-content')
    await expect(content).toBeVisible()
    await expect(content).toContainText(item.marker)
    await expect(page.locator('.error-text')).toHaveCount(0)

    const html = await content.innerHTML()
    expect(html).not.toContain('migration-resource://')
    expect(html).not.toContain('zhjy.jilinjobs.cn:8080/group1/cms/')
  }
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
  const dispatchImages = page.locator('.rich-content img[src^="/static/pages/guide/jypq/"]')
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

  const questions = page.locator('.rich-content h2.faq-question')
  await expect(questions).toHaveCount(10)
  await expect(questions.nth(2)).toHaveText('3.可免费申请书面认证报告的高等教育学历证书包括哪些？')
  await expect(questions.nth(3)).toHaveText('4.普通、成人高等教育学历证书认证需要提供哪些材料')
  await expect(questions.nth(4)).toHaveText('5.自学考试学历证书认证提供哪些材料？')

  await expect(page.locator('.rich-content p').filter({ hasText: '1、毕业证原件' }).first()).toBeVisible()
})
