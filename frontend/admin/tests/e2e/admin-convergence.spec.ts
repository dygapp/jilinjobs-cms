import { expect, test, type APIRequestContext } from '@playwright/test'

async function createArticle(request: APIRequestContext, columnId: number, title: string) {
  const response = await request.post('/api/admin/articles', { data: { columnId, title, bodyHtml:`<p>${title} 正文</p>`, source:'Admin E2E', articleType:'INTERNAL', externalUrl:null, publishDate:'2026-08-29', pinned:false, recommended:false, sortOrder:0, coverResourceId:null, bodyImageResourceIds:[], attachmentResourceIds:[] } })
  expect(response.ok()).toBeTruthy()
  return await response.json() as { id:number;title:string }
}

test('EU-21：独立管理端 Shell 提供八类 CMS 管理入口', async ({ page }) => {
  await page.goto('/admin/')
  await expect(page.getByRole('heading',{name:'文章管理'})).toBeVisible()
  for (const id of ['articles','columns','navigation','pages','lists','advertisements','site-config','static-resources']) await expect(page.getByTestId(`admin-nav-${id}`)).toBeVisible()
  await page.getByTestId('admin-nav-lists').click()
  await expect(page).toHaveURL(/\/admin\/lists$/)
  await expect(page.getByRole('heading',{name:'列表管理'})).toBeVisible()
})

test('EU-21：导航按位置切换并显示树形主数据', async ({ page }) => {
  await page.goto('/admin/navigation')
  await expect(page.getByTestId('navigation-location-MAIN')).toBeVisible()
  await expect(page.getByTestId('navigation-location-HOME_SHORTCUT')).toBeVisible()
  await page.getByTestId('navigation-location-HOME_SHORTCUT').getByRole('button',{name:'首页快捷入口'}).click()
  await expect(page.getByTestId('navigation-tree-table')).toContainText('就业信息填报')
  await expect(page.getByTestId('navigation-tree-table')).toContainText('举报电话及邮箱')
})

test('EU-21：通用列表和广告位承载首页运营数据', async ({ page }) => {
  await page.goto('/admin/lists')
  await expect(page.getByTestId('cms-list-HOME_CAROUSEL')).toBeVisible()
  await page.getByTestId('cms-list-HOME_CAROUSEL').click()
  await expect(page.getByTestId('cms-list-item-table')).toContainText('这里美得不愿离开')
  await page.getByTestId('cms-list-HOME_CAROUSEL').getByRole('button',{name:'列表操作'}).click()
  await expect(page.getByRole('menuitem',{name:'编辑'})).toBeVisible()
  await expect(page.getByRole('menuitem',{name:'删除'})).toBeVisible()
  await page.keyboard.press('Escape')
  await page.goto('/admin/advertisements')
  await expect(page.getByTestId('ad-slot-HOME_RECRUITMENT_PROMO')).toBeVisible()
  await expect(page.getByTestId('advertisement-table')).toContainText('吉林省高校毕业生招聘活动')
  await page.getByTestId('ad-slot-HOME_RECRUITMENT_PROMO').getByRole('button',{name:'广告位操作'}).click()
  await expect(page.getByRole('menuitem',{name:'编辑'})).toBeVisible()
  await expect(page.getByRole('menuitem',{name:'删除'})).toBeVisible()
  await page.keyboard.press('Escape')
})

test('EU-21：网站属性支持运行时自定义 Key 并阻止非法 JSON', async ({ page, request }) => {
  const key=`E2E_JSON_${Date.now()}`
  const created=await request.post('/api/admin/site-config',{data:{key,name:'E2E JSON 属性',groupCode:'E2E',value:'{}',valueType:'JSON',description:'测试运行时属性',sortOrder:0,required:false,system:false,enabled:true}})
  expect(created.ok()).toBeTruthy()
  await page.goto('/admin/site-config')
  const editor=page.getByTestId(`site-config-${key}`)
  await editor.fill('[{not-json}]')
  await page.getByTestId(`save-site-config-${key}`).click()
  await expect(page.getByText('JSON 属性格式不正确，请修正后再保存',{exact:true})).toBeVisible()
})

test('EU-16：文章筛选分页并保持后台发布到公开站闭环', async ({ page, request }, testInfo) => {
  const columnsResponse=await request.get('/api/admin/columns');expect(columnsResponse.ok()).toBeTruthy()
  const columns=await columnsResponse.json() as Array<{id:number;alias:string}>
  const column=columns.find(item=>item.alias==='notice')??columns[0];expect(column).toBeTruthy()
  const prefix=`Admin分页-${Date.now()}-${testInfo.retry}`,created:Array<{id:number;title:string}>=[]
  for(let index=1;index<=12;index+=1)created.push(await createArticle(request,column.id,`${prefix}-${String(index).padStart(2,'0')}`))
  await page.goto('/admin/articles');await page.getByTestId('article-filter-keyword').fill(prefix)
  await expect(page.getByTestId('article-table').locator('tbody tr')).toHaveCount(10)
  await page.getByTestId('article-pagination').locator('.el-pager li').filter({hasText:'2'}).click();await expect(page.getByTestId('article-table').locator('tbody tr')).toHaveCount(2)
  const published=created[0];expect((await request.post(`/api/admin/articles/${published.id}/publish`)).ok()).toBeTruthy();expect((await request.get(`/api/public/articles/${published.id}`)).ok()).toBeTruthy()
  await page.goto(`/article/${published.id}`);await expect(page.getByRole('heading',{name:published.title})).toBeVisible()
})

test('EU-16：固定页面按 render mode 提供对应编辑字段', async ({ page }) => {
  await page.goto('/admin/pages');await page.getByTestId('add-page').click();await expect(page.getByRole('dialog',{name:'新增固定页面'})).toBeVisible();await expect(page.getByTestId('page-body-editor')).toBeVisible()
  await page.getByTestId('page-render-mode').click();await page.getByRole('option',{name:'外部嵌入占位'}).click();await expect(page.getByTestId('page-embed-url')).toBeVisible();await expect(page.getByTestId('page-placeholder-body')).toBeVisible()
  await page.getByRole('button',{name:'取消'}).click()
})

test('EU-17：静态资源拒绝伪装 PNG 并保护站点关键资源', async ({ page, request }) => {
  const fakeUpload=await request.post('/api/admin/static-resources?path=verification-fake/fake.png&replace=false',{multipart:{file:{name:'fake.png',mimeType:'image/png',buffer:Buffer.from('not a png')}}});expect(fakeUpload.ok()).toBeFalsy();expect((await fakeUpload.json() as {message:string}).message).toContain('实际内容')
  const homeList=await request.get('/api/admin/static-resources?path=home');expect(homeList.ok()).toBeTruthy();const rows=await homeList.json() as Array<{path:string;protectedResource:boolean}>;expect(rows.find(row=>row.path==='home/ncss-logo.png')?.protectedResource).toBeTruthy()
  const deleteResponse=await request.delete('/api/admin/static-resources?path=home%2Fncss-logo.png');expect(deleteResponse.ok()).toBeFalsy();expect((await deleteResponse.json() as {message:string}).message).toContain('关键资源')
  await page.goto('/admin/static-resources');const homeRow=page.getByTestId('static-resource-table').getByRole('row').filter({hasText:'home'});await homeRow.getByRole('button',{name:'进入'}).click();const ncssRow=page.getByTestId('static-resource-table').getByRole('row').filter({hasText:'ncss-logo.png'});await expect(ncssRow).toContainText('关键资源')
})
