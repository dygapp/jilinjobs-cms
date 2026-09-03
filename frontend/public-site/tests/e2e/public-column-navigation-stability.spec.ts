import { expect, test, type APIRequestContext } from '@playwright/test'

type PublicColumn = { id: number; name: string; alias: string }

async function publicColumn(request: APIRequestContext, alias: string): Promise<PublicColumn> {
  const response = await request.get(`/api/public/columns/by-alias/${alias}`)
  expect(response.ok()).toBeTruthy()
  return response.json() as Promise<PublicColumn>
}

test('主站栏目切换期间保留当前内容且旧响应不得覆盖返回后的路由状态', async ({ page, request }) => {
  const current = await publicColumn(request, 'policy')
  const delayed = await publicColumn(request, 'typical')

  await page.goto(`/column/${current.alias}`)
  await expect(page.getByRole('heading', { name: current.name, level: 1 })).toBeVisible()

  await page.route('**/api/public/articles?*', async route => {
    const url = new URL(route.request().url())
    if (url.searchParams.get('columnId') === String(delayed.id)) {
      await new Promise(resolve => setTimeout(resolve, 800))
    }
    await route.continue()
  })

  const delayedRequest = page.waitForRequest(requestEvent => {
    const url = new URL(requestEvent.url())
    return url.pathname === '/api/public/articles'
      && url.searchParams.get('columnId') === String(delayed.id)
  })
  const delayedResponse = page.waitForResponse(response => {
    const url = new URL(response.url())
    return url.pathname === '/api/public/articles'
      && url.searchParams.get('columnId') === String(delayed.id)
  })

  await page.locator(`a[href="/column/${delayed.alias}"]`).first().click()
  await expect(page).toHaveURL(new RegExp(`/column/${delayed.alias}$`))
  await delayedRequest

  await expect(page.getByRole('heading', { name: current.name, level: 1 })).toBeVisible()
  await expect(page.getByText('正在加载栏目…', { exact: true })).toHaveCount(0)

  await page.goBack()
  await expect(page).toHaveURL(new RegExp(`/column/${current.alias}$`))
  await expect(page.getByRole('heading', { name: current.name, level: 1 })).toBeVisible()

  await delayedResponse
  await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => resolve())))

  await expect(page.getByRole('heading', { name: current.name, level: 1 })).toBeVisible()
  await expect(page.getByRole('heading', { name: delayed.name, level: 1 })).toHaveCount(0)
})
