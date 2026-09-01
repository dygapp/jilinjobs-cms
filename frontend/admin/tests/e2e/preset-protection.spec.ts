import { expect, test, type APIRequestContext } from '@playwright/test'

async function expectPresetRejected(response: Awaited<ReturnType<APIRequestContext['delete']>>) {
  expect(response.ok()).toBeFalsy()
  const body = await response.json() as { message?: string }
  expect(body.message || '').toContain('预置')
}

test('EU-21：V12 预置基线由 Backend 阻止删除且稳定 Alias 不可改写', async ({ request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`

  const columnsResponse = await request.get('/api/admin/columns')
  expect(columnsResponse.ok()).toBeTruthy()
  const columns = await columnsResponse.json() as Array<{ id:number; parentId:number|null; alias:string; name:string; coverPolicy:string; sortOrder:number; enabled:boolean; preset:boolean }>
  const notice = columns.find(item => item.alias === 'notice')
  expect(notice?.preset).toBe(true)
  await expectPresetRejected(await request.delete(`/api/admin/columns/${notice!.id}`))
  const renameColumn = await request.put(`/api/admin/columns/${notice!.id}`, { data: { parentId:notice!.parentId, alias:`notice-${suffix}`, name:notice!.name, coverPolicy:notice!.coverPolicy, sortOrder:notice!.sortOrder, enabled:notice!.enabled } })
  expect(renameColumn.ok()).toBeFalsy()
  expect((await renameColumn.json() as {message:string}).message).toContain('预置')

  const groupsResponse = await request.get('/api/admin/page-groups')
  expect(groupsResponse.ok()).toBeTruthy()
  const groups = await groupsResponse.json() as Array<{ id:number; alias:string; name:string; sortOrder:number; enabled:boolean; preset:boolean }>
  const guide = groups.find(item => item.alias === 'guide')
  expect(guide?.preset).toBe(true)
  const renameGroup = await request.put(`/api/admin/page-groups/${guide!.id}`, { data: { alias:`guide-${suffix}`, name:guide!.name, sortOrder:guide!.sortOrder, enabled:guide!.enabled } })
  expect(renameGroup.ok()).toBeFalsy()
  expect((await renameGroup.json() as {message:string}).message).toContain('预置')

  const pagesResponse = await request.get('/api/admin/pages')
  expect(pagesResponse.ok()).toBeTruthy()
  const pages = await pagesResponse.json() as Array<{ id:number; groupId:number|null; alias:string; name:string; bodyHtml:string; renderMode:string; embedUrl:string|null; sortOrder:number; enabled:boolean; preset:boolean }>
  const about = pages.find(item => item.groupId == null && item.alias === 'about')
  expect(about?.preset).toBe(true)
  await expectPresetRejected(await request.delete(`/api/admin/pages/${about!.id}`))
  const renamePage = await request.put(`/api/admin/pages/${about!.id}`, { data: { groupId:about!.groupId, alias:`about-${suffix}`, name:about!.name, bodyHtml:about!.bodyHtml, renderMode:about!.renderMode, embedUrl:about!.embedUrl, sortOrder:about!.sortOrder, enabled:about!.enabled } })
  expect(renamePage.ok()).toBeFalsy()
  expect((await renamePage.json() as {message:string}).message).toContain('预置')

  const locationsResponse = await request.get('/api/admin/navigation-locations')
  expect(locationsResponse.ok()).toBeTruthy()
  const locations = await locationsResponse.json() as Array<{ code:string; preset:boolean }>
  expect(locations.find(item => item.code === 'MAIN')?.preset).toBe(true)
  await expectPresetRejected(await request.delete('/api/admin/navigation-locations/MAIN'))

  const navigationsResponse = await request.get('/api/admin/navigations')
  expect(navigationsResponse.ok()).toBeTruthy()
  const navigations = await navigationsResponse.json() as Array<{ id:number; name:string; position:string; preset:boolean }>
  const home = navigations.find(item => item.position === 'MAIN' && item.name === '网站首页')
  expect(home?.preset).toBe(true)
  await expectPresetRejected(await request.delete(`/api/admin/navigations/${home!.id}`))

  const listsResponse = await request.get('/api/admin/lists')
  expect(listsResponse.ok()).toBeTruthy()
  const lists = await listsResponse.json() as Array<{ id:number; code:string; preset:boolean }>
  const carousel = lists.find(item => item.code === 'HOME_CAROUSEL')
  expect(carousel?.preset).toBe(true)
  await expectPresetRejected(await request.delete(`/api/admin/lists/${carousel!.id}`))

  const slotsResponse = await request.get('/api/admin/advertisements/slots')
  expect(slotsResponse.ok()).toBeTruthy()
  const slots = await slotsResponse.json() as Array<{ id:number; code:string; preset:boolean }>
  const promo = slots.find(item => item.code === 'HOME_RECRUITMENT_PROMO')
  expect(promo?.preset).toBe(true)
  await expectPresetRejected(await request.delete(`/api/admin/advertisements/slots/${promo!.id}`))

  const propertiesResponse = await request.get('/api/admin/site-config')
  expect(propertiesResponse.ok()).toBeTruthy()
  const properties = await propertiesResponse.json() as Array<{ key:string; preset:boolean }>
  expect(properties.find(item => item.key === 'SITE_NAME')?.preset).toBe(true)
  await expectPresetRejected(await request.delete('/api/admin/site-config/SITE_NAME'))
})

test('EU-21：管理员运行期新增结构默认不是预置数据并可正常删除', async ({ request }, testInfo) => {
  const suffix = `${Date.now()}_${testInfo.retry}`

  const columnResponse = await request.post('/api/admin/columns', { data: { parentId:null, alias:`e2e-preset-column-${suffix.replaceAll('_','-')}`, name:'E2E 普通栏目', coverPolicy:'OPTIONAL', sortOrder:999, enabled:true } })
  expect(columnResponse.ok()).toBeTruthy()
  const column = await columnResponse.json() as { id:number; preset:boolean }
  expect(column.preset).toBe(false)
  expect((await request.delete(`/api/admin/columns/${column.id}`)).ok()).toBeTruthy()

  const pageResponse = await request.post('/api/admin/pages', { data: { groupId:null, alias:`e2e-preset-page-${suffix.replaceAll('_','-')}`, name:'E2E 普通单页', bodyHtml:'<p>E2E</p>', renderMode:'RICH_TEXT', embedUrl:null, sortOrder:999, enabled:true } })
  expect(pageResponse.ok()).toBeTruthy()
  const page = await pageResponse.json() as { id:number; preset:boolean }
  expect(page.preset).toBe(false)
  expect((await request.delete(`/api/admin/pages/${page.id}`)).ok()).toBeTruthy()

  const listResponse = await request.post('/api/admin/lists', { data: { code:`E2E_PRESET_LIST_${suffix}`, name:'E2E 普通列表', groupCode:'E2E', imagePolicy:'OPTIONAL', description:'', sortOrder:999, enabled:true, system:false } })
  expect(listResponse.ok()).toBeTruthy()
  const list = await listResponse.json() as { id:number; preset:boolean }
  expect(list.preset).toBe(false)
  expect((await request.delete(`/api/admin/lists/${list.id}`)).ok()).toBeTruthy()

  const slotResponse = await request.post('/api/admin/advertisements/slots', { data: { code:`E2E_PRESET_SLOT_${suffix}`, name:'E2E 普通展示位', description:'', sortOrder:999, enabled:true, system:false } })
  expect(slotResponse.ok()).toBeTruthy()
  const slot = await slotResponse.json() as { id:number; preset:boolean }
  expect(slot.preset).toBe(false)
  expect((await request.delete(`/api/admin/advertisements/slots/${slot.id}`)).ok()).toBeTruthy()

  const propertyResponse = await request.post('/api/admin/site-config', { data: { key:`E2E_PRESET_PROP_${suffix}`, name:'E2E 普通属性', groupCode:'GENERAL', value:'x', valueType:'TEXT', description:'', sortOrder:999, required:false, system:false, enabled:true } })
  expect(propertyResponse.ok()).toBeTruthy()
  const property = await propertyResponse.json() as { key:string; preset:boolean }
  expect(property.preset).toBe(false)
  expect((await request.delete(`/api/admin/site-config/${property.key}`)).ok()).toBeTruthy()
})

test('EU-21：管理端明确标识预置结构并禁用或移除删除入口', async ({ page }) => {
  await page.goto('/admin/columns')
  const noticeRow = page.getByRole('row').filter({ hasText:'通知公告' }).first()
  await expect(noticeRow.getByText('预置', { exact:true })).toBeVisible()
  await expect(noticeRow.getByRole('button', { name:'删除' })).toHaveCount(0)

  await page.goto('/admin/navigation')
  await expect(page.getByTestId('preset-navigation-location-MAIN')).toBeVisible()
  await page.getByTestId('navigation-location-MAIN').getByRole('button', { name:'导航位置操作' }).click()
  const navigationDelete = page.getByRole('menuitem', { name:'删除' })
  await expect(navigationDelete).toBeVisible()
  await expect(navigationDelete).toHaveClass(/is-disabled/)
  await page.keyboard.press('Escape')

  await page.goto('/admin/lists')
  await expect(page.getByTestId('preset-cms-list-HOME_CAROUSEL')).toBeVisible()
  await page.getByTestId('cms-list-HOME_CAROUSEL').getByRole('button', { name:'列表操作' }).click()
  const listDelete = page.getByRole('menuitem', { name:'删除' })
  await expect(listDelete).toBeVisible()
  await expect(listDelete).toHaveClass(/is-disabled/)
  await page.keyboard.press('Escape')

  await page.goto('/admin/advertisements')
  await expect(page.getByTestId('preset-ad-slot-HOME_RECRUITMENT_PROMO')).toBeVisible()
  await page.getByTestId('ad-slot-HOME_RECRUITMENT_PROMO').getByRole('button', { name:'展示位操作' }).click()
  const slotDelete = page.getByRole('menuitem', { name:'删除' })
  await expect(slotDelete).toBeVisible()
  await expect(slotDelete).toHaveClass(/is-disabled/)
})
