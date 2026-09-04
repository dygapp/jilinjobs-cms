import { expect, test } from '@playwright/test'

const enabled = process.env.PARTY_MIGRATION_RUNTIME === 'true'
const carouselTitles = [
  '纪念中国人民抗日战争暨世界反法西斯战争胜利80周年大会在京隆重举行 习近平发表重要讲话并检阅受阅部队',
  '吉林省高等学校毕业生就业指导中心学习贯彻习近平新时代中国特色社会主义思想主题教育',
  '习近平在广东考察时强调 坚定不移全面深化改革扩大高水平对外开放 在推进中国式现代化建设中走在前列',
  '扎实抓好主题教育 为奋进新征程凝心聚力',
]
const representativeWorkTitle = '吉林省高等学校毕业生就业指导中心组织开展“忆党史峥嵘 铸信仰丰碑”观影主题党日活动'

test.skip(!enabled, '仅在 EU-29 真实迁移 Human Review Runtime 中执行')

test('EU-29：真实历史文章、正文资源与四条轮播在 Runtime 可用', async ({ page, request }, testInfo) => {
  const carouselResponse = await request.get('/api/public/lists/by-code/PARTY_CAROUSEL')
  expect(carouselResponse.ok()).toBeTruthy()
  const carousel = await carouselResponse.json() as {
    items: Array<{ title: string; url: string | null; imagePath: string | null; openMode: string }>
  }
  expect(carousel.items).toHaveLength(4)
  expect(carousel.items.map(item => item.title)).toEqual(carouselTitles)
  for (const item of carousel.items) {
    expect(item.openMode).toBe('NEW_WINDOW')
    expect(item.imagePath).toMatch(/^\/static\/migrated\/party\/carousel\/[0-9a-f]{64}\.(?:jpg|png|gif|webp)$/)
    expect((await request.get(item.imagePath!)).ok()).toBeTruthy()
  }

  await page.goto('/party/')
  await expect(page.getByTestId('party-site')).toBeVisible()
  await expect(page.getByTestId('party-carousel')).toBeVisible()
  for (const title of carouselTitles) {
    await expect(page.getByTestId('party-carousel').getByText(title, { exact: true })).toHaveCount(1)
  }

  const workSection = page.getByTestId('party-section-party-work')
  const workLink = workSection.getByText(representativeWorkTitle, { exact: true })
  await expect(workLink).toBeVisible()
  await workLink.click()
  await expect(page.getByTestId('party-article-title')).toHaveText(representativeWorkTitle)
  const body = page.getByTestId('party-article-body')
  await expect(body).toBeVisible()
  const bodyImages = body.locator('img')
  expect(await bodyImages.count()).toBeGreaterThan(0)
  const firstImage = bodyImages.first()
  await expect(firstImage).toBeVisible()
  const imageSrc = await firstImage.getAttribute('src')
  expect(imageSrc).toBeTruthy()
  expect(imageSrc).not.toContain('migration-resource://')
  expect((await request.get(imageSrc!)).ok()).toBeTruthy()

  await page.screenshot({ path: testInfo.outputPath('eu29-party-migrated-article.png'), fullPage: true })
  await page.goto('/party/')
  await page.screenshot({ path: testInfo.outputPath('eu29-party-migrated-home.png'), fullPage: true })
})
