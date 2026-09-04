import { expect, test, type APIRequestContext } from '@playwright/test'
import { normalizePublicSiteConfigItem, type SiteConfigItem } from '../../src/shared/api/siteConfig'

type AdminList = { id: number; code: string }
type SeededItem = { listId: number; id: number }

async function adminListByCode(request: APIRequestContext, code: string): Promise<AdminList> {
  const response = await request.get('/api/admin/lists')
  expect(response.ok()).toBeTruthy()
  const lists = await response.json() as AdminList[]
  const list = lists.find(item => item.code === code)
  expect(list, `后台列表不存在：${code}`).toBeTruthy()
  return list!
}

async function createLinkItem(
  request: APIRequestContext,
  listId: number,
  title: string,
  imagePath: string,
  sortOrder: number,
): Promise<SeededItem> {
  const response = await request.post(`/api/admin/lists/${listId}/items`, {
    data: {
      sourceType: 'LINK',
      articleId: null,
      title,
      subtitle: null,
      url: null,
      imagePath,
      imageResourceId: null,
      openMode: 'DEFAULT',
      sortOrder,
      enabled: true,
      extraJson: null,
    },
  })
  expect(response.ok()).toBeTruthy()
  return { listId, id: (await response.json() as { id: number }).id }
}

async function removeItems(request: APIRequestContext, items: SeededItem[]) {
  for (const item of items) {
    const response = await request.delete(`/api/admin/lists/${item.listId}/items/${item.id}`)
    expect(response.ok()).toBeTruthy()
  }
}

function configItem(key: string, value: string): SiteConfigItem {
  return { key, name: key, groupCode: 'PRESENTATION', value, valueType: 'INTEGER', description: '', sortOrder: 0, required: true, system: true, enabled: true }
}

test('EU-30：统一轮播属性拒绝非正整数且稳定定义受 preset 保护', async ({ request }) => {
  const interval = await request.put('/api/admin/site-config/CAROUSEL_INTERVAL_SECONDS', { data: { value: '0' } })
  expect(interval.status()).toBe(400)

  const maxItems = await request.put('/api/admin/site-config/CAROUSEL_MAX_ITEMS', { data: { value: '-1' } })
  expect(maxItems.status()).toBe(400)

  const response = await request.get('/api/admin/site-config')
  expect(response.ok()).toBeTruthy()
  const config = await response.json() as Array<{ key: string; value: string; valueType: string; preset: boolean }>
  for (const key of ['CAROUSEL_INTERVAL_SECONDS', 'CAROUSEL_MAX_ITEMS']) {
    const item = config.find(entry => entry.key === key)
    expect(item).toMatchObject({ valueType: 'INTEGER', preset: true })
    expect(Number(item?.value)).toBeGreaterThan(0)
    const deleteResponse = await request.delete(`/api/admin/site-config/${key}`)
    expect(deleteResponse.status()).toBe(400)
  }
})

test('EU-30：公开轮播配置严格拒绝可被 parseInt 截断的历史非法值', () => {
  for (const value of ['1.5', '5abc', '1e2', ' 5', '0', '-1', '']) {
    expect(normalizePublicSiteConfigItem(configItem('CAROUSEL_MAX_ITEMS', value)).value).toBe('')
  }
  expect(normalizePublicSiteConfigItem(configItem('CAROUSEL_MAX_ITEMS', '05')).value).toBe('05')
  expect(normalizePublicSiteConfigItem(configItem('CAROUSEL_INTERVAL_SECONDS', '4')).value).toBe('4')
  expect(normalizePublicSiteConfigItem(configItem('CONTACT_PHONE', '5abc')).value).toBe('5abc')
})

test('EU-30：隐藏图片失败后补位且保持当前轮播项身份', async ({ page, request }, testInfo) => {
  const list = await adminListByCode(request, 'PARTY_CAROUSEL')
  const suffix = `${Date.now()}-${testInfo.retry}`
  const seeded: SeededItem[] = []
  try {
    for (let index = 0; index < 6; index += 1) {
      seeded.push(await createLinkItem(
        request,
        list.id,
        `EU30图片失败补位-${suffix}-${index + 1}`,
        index === 1 ? `/static/health/eu30-missing-${suffix}.png` : '/static/health/baseline.png',
        -400000 + index,
      ))
    }

    await page.emulateMedia({ reducedMotion: 'reduce' })
    await page.goto('/party/')
    const carousel = page.getByTestId('party-carousel')
    await expect(carousel).toBeVisible()

    const first = page.getByTestId(`party-carousel-item-${seeded[0].id}`)
    const failed = page.getByTestId(`party-carousel-item-${seeded[1].id}`)
    const backfill = page.getByTestId(`party-carousel-item-${seeded[5].id}`)

    await expect(failed).toHaveCount(0)
    await expect(backfill).toHaveCount(1)
    await expect(carousel.locator('.party-carousel-item')).toHaveCount(5)
    await expect(first).toHaveClass(/active/)
  } finally {
    await removeItems(request, seeded)
  }
})
