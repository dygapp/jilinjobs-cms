import { expect, test } from '@playwright/test'

const PNG_SIGNATURE = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a])

test('视觉基线：页脚备案、事业单位、公众号与 favicon 使用标准 PNG 资源', async ({ page, request }, testInfo) => {
  for (const resource of [
    '/static/footer/public-security-record.png',
    '/static/footer/public-institution.png',
    '/static/footer/wechat-qr.png',
    '/static/brand/site-favicon.png',
  ]) {
    const response = await request.get(resource)
    expect(response.ok(), `${resource} 应来自版本化静态资源包`).toBeTruthy()
    const body = await response.body()
    expect(body.length).toBeGreaterThan(100)
    expect(body.subarray(0, PNG_SIGNATURE.length).equals(PNG_SIGNATURE), `${resource} 应为真实 PNG 编码`).toBeTruthy()
  }

  const configResponse = await request.get('/api/public/site-config')
  expect(configResponse.ok()).toBeTruthy()
  const configs = await configResponse.json() as Array<{ key: string; value: string }>
  const configuredCopyright = configs.find(item => item.key === 'FOOTER_COPYRIGHT')?.value
  expect(configuredCopyright).toBeTruthy()

  await page.setViewportSize({ width: 1440, height: 1000 })
  await page.goto('/')

  await expect(page.locator('link[rel="icon"]')).toHaveAttribute('href', '/static/brand/site-favicon.png')
  const footer = page.locator('.site-footer')
  await footer.scrollIntoViewIfNeeded()
  await expect(page.locator('.public-security-record')).toContainText('吉公网安备 22010702000243号')
  await expect(page.locator('.public-security-record img')).toHaveAttribute('src', '/static/footer/public-security-record.png')
  await expect(page.locator('.public-institution-badge img')).toHaveAttribute('src', '/static/footer/public-institution.png')
  await expect(page.locator('.wechat-entry img')).toHaveAttribute('src', '/static/footer/wechat-qr.png')
  await expect(page.locator('.wechat-entry')).toContainText('吉林省大学生就业创业')
  await expect(page.locator('.public-institution-badge')).not.toHaveAttribute('href')
  await expect(page.locator('.wechat-entry')).not.toHaveAttribute('href')
  await expect(footer.locator('strong')).toHaveText(configuredCopyright!)

  expect(Math.round((await page.locator('.public-security-record img').boundingBox())?.width ?? 0)).toBe(20)
  expect(Math.round((await page.locator('.public-institution-badge img').boundingBox())?.width ?? 0)).toBe(96)
  expect(Math.round((await page.locator('.wechat-entry img').boundingBox())?.width ?? 0)).toBe(92)

  await footer.screenshot({ path: testInfo.outputPath('homepage-footer-desktop.png') })
})

test('视觉基线：首页举报入口打开站内固定页并保留原站正文', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 1440, height: 1000 })
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
  await page.screenshot({ path: testInfo.outputPath('report-contact-desktop.png'), fullPage: true })
})
