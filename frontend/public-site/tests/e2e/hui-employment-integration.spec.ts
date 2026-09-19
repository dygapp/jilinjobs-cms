import { expect, test } from '@playwright/test'

const HUI_HOST = 'https://student.hjiuye.com'

const pageTargets = [
  { route: '/page/jobs/positions', tab: '在招职位', renderer: 'HUI_EMPLOYMENT_POSITIONS', url: `${HUI_HOST}/moreActivities/2685/4/22` },
  { route: '/page/jobs/recruitment', tab: '招聘简章', renderer: 'HUI_EMPLOYMENT_RECRUITMENT', url: `${HUI_HOST}/moreActivities/2685/4/23` },
  { route: '/page/jobs/jobfair', tab: '双选会', renderer: 'HUI_EMPLOYMENT_JOB_FAIR', url: `${HUI_HOST}/moreActivities/2685/4/24` },
  { route: '/page/jobs/presentation', tab: '现场宣讲', renderer: 'HUI_EMPLOYMENT_PRESENTATION', url: `${HUI_HOST}/moreActivities/2685/4/25` },
  { route: '/page/jobs/jilin', tab: '留省就业', renderer: 'HUI_EMPLOYMENT_JILIN', url: `${HUI_HOST}/moreActivities/2685/4/30` },
] as const

test.beforeEach(async ({ page }) => {
  await page.route(`${HUI_HOST}/**`, async route => {
    await route.fulfill({
      status: 200,
      contentType: 'text/html',
      body: '<!doctype html><html><body><main>慧就业验证内容</main></body></html>',
    })
  })
})

test('首页三个独立慧就业区域使用当前完整地址，直播课程更多进入本站二级页', async ({ page }) => {
  await page.goto('/')

  const homeFrames = [
    { testId: 'hui-employment-home-calendar', title: '就业日历', url: `${HUI_HOST}/calendarEd/2685` },
    { testId: 'hui-employment-home-latest-recruitment', title: '最新招聘', url: `${HUI_HOST}/college/2685/4` },
    { testId: 'hui-employment-home-live-courses', title: '直播课程', url: `${HUI_HOST}/college/2685/5` },
  ] as const

  for (const target of homeFrames) {
    const container = page.getByTestId(target.testId)
    await container.scrollIntoViewIfNeeded()
    const frame = container.locator('iframe')
    await expect(frame).toHaveAttribute('src', target.url)
    await expect(frame).toHaveAttribute('title', `${target.title}—慧就业`)
    await expect(container).toHaveAttribute('data-frame-status', 'loaded')
  }

  await page.locator('.home-integrated-heading').getByRole('link', { name: '更多 >' }).click()
  await expect(page).toHaveURL(/\/page\/live-course$/)
  await expect(page.getByRole('heading', { name: '直播课程', exact: true })).toBeVisible()
  const livePage = page.getByTestId('hui-employment-page-HUI_EMPLOYMENT_LIVE_COURSES')
  await expect(livePage.locator('iframe')).toHaveAttribute('src', `${HUI_HOST}/moreActivities/2685/5/27`)
})

test('招聘信息五个二级页面保持本站框架、当前 Tab 与精确业务映射一致', async ({ page }) => {
  for (const target of pageTargets) {
    await page.goto(target.route)
    await expect(page.locator('.platform-bar')).toBeVisible()
    await expect(page.locator('.site-footer')).toBeVisible()
    await expect(page.locator('.group-tabs a.active')).toHaveText(target.tab)
    const container = page.getByTestId(`hui-employment-page-${target.renderer}`)
    await expect(container.locator('iframe')).toHaveAttribute('src', target.url)
    await expect(container.locator('iframe')).toHaveAttribute('title', `${target.tab}—慧就业`)
    await expect(page.locator('[data-unsupported-renderer]')).toHaveCount(0)
  }
})

test('单个慧就业区域失败后保留本站内容并可原位重试', async ({ page }) => {
  await page.goto('/')
  const calendar = page.getByTestId('hui-employment-home-calendar')
  await calendar.scrollIntoViewIfNeeded()
  const frame = calendar.locator('iframe')
  await expect(frame).toHaveAttribute('src', `${HUI_HOST}/calendarEd/2685`)
  await frame.evaluate(element => element.dispatchEvent(new Event('error')))

  await expect(calendar).toHaveAttribute('data-frame-status', 'failed')
  await expect(calendar.getByText('就业日历暂时无法加载')).toBeVisible()
  await expect(page.getByRole('heading', { name: '就业动态' })).toBeVisible()
  await expect(page.locator('.platform-bar')).toBeVisible()
  await expect(page.locator('.site-footer')).toBeVisible()

  await calendar.getByRole('button', { name: '重新加载' }).click()
  await expect(calendar.locator('iframe')).toHaveAttribute('src', `${HUI_HOST}/calendarEd/2685`)
  await expect(calendar).toHaveAttribute('data-frame-status', 'loaded')
})
