import { expect, test, type APIRequestContext } from '@playwright/test'

type StructuredCard = { title: string; bodyHtml: string }
type StructuredContent = { schemaVersion: number; kind: string; items: StructuredCard[] }
type CmsPage = {
  id: number
  groupId: number | null
  alias: string
  name: string
  bodyHtml: string
  contentModel: string
  rendererKey: string
  contentOwner: string
  structuredContent: StructuredContent | null
  renderMode: string | null
  embedUrl: string | null
  sortOrder: number
  enabled: boolean
  preset: boolean
}
type PageGroup = { id: number; alias: string; name: string }

async function restorePage(request: APIRequestContext, original: CmsPage) {
  const response = await request.put(`/api/admin/pages/${original.id}`, {
    data: {
      groupId: original.groupId,
      alias: original.alias,
      name: original.name,
      bodyHtml: original.bodyHtml,
      contentModel: original.contentModel,
      rendererKey: original.rendererKey,
      contentOwner: original.contentOwner,
      structuredContent: original.structuredContent,
      renderMode: original.renderMode,
      embedUrl: original.embedUrl,
      sortOrder: original.sortOrder,
      enabled: original.enabled,
    },
  })
  expect(response.ok(), 'restore guide/jypq after Admin E2E').toBeTruthy()
}

test('EU-55：Admin 对 guide/jypq 提供单一 Structured 卡片编辑 authority，并可重排保存重载', async ({ page, request }) => {
  const groupsResponse = await request.get('/api/admin/page-groups')
  expect(groupsResponse.ok()).toBeTruthy()
  const groups = await groupsResponse.json() as PageGroup[]
  const guide = groups.find(group => group.alias === 'guide')
  expect(guide).toBeTruthy()

  const pagesResponse = await request.get('/api/admin/pages')
  expect(pagesResponse.ok()).toBeTruthy()
  const pages = await pagesResponse.json() as CmsPage[]
  const original = pages.find(item => item.groupId === guide!.id && item.alias === 'jypq')
  expect(original).toBeTruthy()
  expect(original!.contentModel).toBe('STRUCTURED')
  expect(original!.rendererKey).toBe('JILINJOBS_GUIDE_CARDS')
  expect(original!.contentOwner).toBe('OPERATOR')
  expect(original!.renderMode).toBeNull()
  expect(original!.bodyHtml).toBe('')
  expect(original!.structuredContent?.schemaVersion).toBe(1)
  expect(original!.structuredContent?.kind).toBe('CARD_COLLECTION')
  expect(original!.structuredContent?.items).toHaveLength(3)

  const originalTitles = original!.structuredContent!.items.map(item => item.title)

  try {
    await page.goto('/admin/pages')
    await page.getByTestId(`page-group-${guide!.id}`).click()
    const row = page.getByTestId('page-table').getByRole('row').filter({ hasText: '就业派遣' })
    await expect(row).toContainText('结构化卡片')
    await row.getByTestId(`edit-page-${original!.id}`).click()

    let dialog = page.getByRole('dialog', { name: '编辑单页' })
    await expect(dialog.getByTestId('structured-card-editor')).toBeVisible()
    await expect(dialog.getByTestId('page-render-mode')).toHaveCount(0)
    await expect(dialog.getByTestId('page-body-editor')).toHaveCount(0)
    await expect(dialog).toContainText('Renderer：JILINJOBS_GUIDE_CARDS')
    await expect(dialog.locator('.structured-card-editor-item')).toHaveCount(3)
    await expect(dialog.getByTestId('structured-card-title-0')).toHaveValue(originalTitles[0]!)
    await expect(dialog.getByTestId('structured-card-title-1')).toHaveValue(originalTitles[1]!)

    await dialog.getByTestId('structured-card-0').getByRole('button', { name: '下移' }).click()
    await expect(dialog.getByTestId('structured-card-title-0')).toHaveValue(originalTitles[1]!)
    await expect(dialog.getByTestId('structured-card-title-1')).toHaveValue(originalTitles[0]!)
    await dialog.getByTestId('save-page').click()
    await expect(dialog).toBeHidden()

    await expect(row).toContainText('结构化卡片')
    await row.getByTestId(`edit-page-${original!.id}`).click()
    dialog = page.getByRole('dialog', { name: '编辑单页' })
    await expect(dialog.getByTestId('structured-card-title-0')).toHaveValue(originalTitles[1]!)
    await expect(dialog.getByTestId('structured-card-title-1')).toHaveValue(originalTitles[0]!)
    await expect(dialog.getByTestId('structured-card-title-2')).toHaveValue(originalTitles[2]!)
    await dialog.getByRole('button', { name: '取消' }).click()
  } finally {
    await restorePage(request, original!)
  }
})
