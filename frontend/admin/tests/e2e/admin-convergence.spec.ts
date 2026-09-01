import { expect, test, type APIRequestContext } from '@playwright/test'

async function createArticle(request: APIRequestContext, columnId: number, title: string) {
  const response = await request.post('/api/admin/articles', { data: { columnId, title, bodyHtml:`<p>${title} 正文</p>`, source:'Admin E2E', articleType:'INTERNAL', externalUrl:null, publishDate:'2026-08-29', pinned:false, recommended:false, sortOrder:0, coverResourceId:null, bodyImageResourceIds:[], attachmentResourceIds:[] } })
  expect(response.ok()).toBeTruthy()
  return await response.json() as { id:number;title:string }
}

test('EU-21：独立管理端 Shell 按内容职责组织八类 CMS 管理入口', async ({ page }) => {
  await page.goto('/admin/')
  await expect(page.getByRole('heading',{name:'文章管理'})).toBeVisible()
  for (const section of ['内容管理','内容结构','运营展示','站点设置']) await expect(page.getByTestId(`admin-nav-section-${section}`)).toBeVisible()
  for (const id of ['articles','pages','lists','columns','navigation','advertisements','site-config','static-resources']) await expect(page.getByTestId(`admin-nav-${id}`)).toBeVisible()
  await expect(page.getByTestId('admin-nav-pages')).toContainText('单页管理')
  await expect(page.getByTestId('admin-nav-advertisements')).toContainText('宣传展示')

  const shell=page.locator('.admin-app')
  await page.getByTestId('admin-sidebar-toggle').click()
  await expect(shell).toHaveClass(/sidebar-collapsed/)
  await expect(page.getByTestId('admin-sidebar-toggle')).toHaveAttribute('aria-label','展开主导航')
  await expect(page.getByTestId('admin-nav-pages').locator('.admin-nav-label')).toBeHidden()
  await page.getByTestId('admin-sidebar-toggle').click()
  await expect(shell).not.toHaveClass(/sidebar-collapsed/)

  await page.getByTestId('admin-nav-lists').click()
  await expect(page).toHaveURL(/\/admin\/lists$/)
  await expect(page.getByRole('heading',{name:'列表管理'})).toBeVisible()
})

test('EU-21：局部导航可收起且紧凑操作保留 Tooltip 与可访问名称', async ({ page }) => {
  await page.goto('/admin/articles')
  await expect(page.getByTestId('article-column-tree')).toBeVisible()
  await page.getByRole('button',{name:'收起栏目导航'}).click()
  await expect(page.getByTestId('article-column-tree')).toBeHidden()
  await page.getByRole('button',{name:'展开栏目导航'}).click()
  await expect(page.getByTestId('article-column-tree')).toBeVisible()

  const editAction=page.getByTestId('article-table').getByRole('button',{name:'编辑'}).first()
  await expect(editAction).toBeVisible()
  await editAction.hover()
  await expect(page.getByRole('tooltip',{name:'编辑'})).toBeVisible()

  await page.goto('/admin/site-config')
  await expect(page.getByTestId('site-property-group-all')).toBeVisible()
  await page.getByRole('button',{name:'收起属性分组'}).click()
  await expect(page.getByTestId('site-property-group-all')).toBeHidden()
  await page.getByRole('button',{name:'展开属性分组'}).click()
  await expect(page.getByTestId('site-property-group-all')).toBeVisible()
})

test('EU-21：导航位置、条目图标与树形主数据形成管理闭环', async ({ page, request }) => {
  await page.goto('/admin/navigation')
  await expect(page.getByTestId('navigation-location-MAIN')).toBeVisible()
  await expect(page.getByTestId('navigation-location-HOME_SHORTCUT')).toBeVisible()
  await expect(page.getByTestId('navigation-location-HOME_QUICK')).toBeVisible()
  await expect(page.getByTestId('navigation-location-SERVICE')).toHaveCount(0)
  await expect(page.getByTestId('navigation-location-SITE')).toHaveCount(0)

  const locationsResponse=await request.get('/api/admin/navigation-locations')
  expect(locationsResponse.ok()).toBeTruthy()
  const locations=await locationsResponse.json() as Array<{code:string}>
  expect(locations.map(item=>item.code)).toEqual(['MAIN','HOME_SHORTCUT','HOME_QUICK'])

  await page.getByTestId('navigation-location-HOME_SHORTCUT').click()
  await expect(page.getByTestId('navigation-tree-table')).toContainText('就业信息填报')
  await expect(page.getByTestId('navigation-tree-table')).toContainText('举报电话及邮箱')

  const publicResponse=await request.get('/api/public/navigations')
  expect(publicResponse.ok()).toBeTruthy()
  const publicItems=await publicResponse.json() as Array<{name:string;position:string;iconPath:string|null}>
  expect(publicItems.find(item=>item.position==='HOME_SHORTCUT'&&item.name==='就业信息填报')?.iconPath).toBe('/static/icons/top-nav-01.png')

  const firstRow=page.getByTestId('navigation-tree-table').getByRole('row').filter({hasText:'就业信息填报'})
  await expect(firstRow.getByTestId('adaptive-image-preview')).toHaveAttribute('data-preview-theme','dark')
  await firstRow.getByRole('button',{name:'编辑'}).click()
  const editDialog=page.getByRole('dialog',{name:'编辑导航'})
  await expect(editDialog.getByTestId('image-resource-picker')).toBeVisible()
  await expect(editDialog.locator('img[alt="当前图片"]')).toHaveAttribute('src','/static/icons/top-nav-01.png')
  await expect(editDialog.getByTestId('adaptive-image-preview').first()).toHaveAttribute('data-preview-theme','dark')
  await expect(editDialog.getByTestId('adaptive-image-preview').first()).toHaveAttribute('title',/悬停可切换对比背景/)
  await editDialog.getByRole('button',{name:'选择已有图片'}).click()
  const libraryDialog=page.getByRole('dialog',{name:'选择已有图片'})
  const knownIcon=libraryDialog.getByRole('button',{name:/就业信息填报/})
  await expect(knownIcon.getByTestId('adaptive-image-preview')).toHaveAttribute('data-preview-theme','dark')
  await page.keyboard.press('Escape')
  await editDialog.getByRole('button',{name:'取消'}).click()

  await page.getByTestId('navigation-location-HOME_SHORTCUT').getByRole('button',{name:'导航位置操作'}).click()
  await expect(page.getByRole('menuitem',{name:'编辑'})).toBeVisible()
  await expect(page.getByRole('menuitem',{name:'删除'})).toBeVisible()
  await page.keyboard.press('Escape')
})

test('EU-21：通用列表只维护数据属性、图片契约并复用统一图片上传', async ({ page, request }) => {
  const response=await request.get('/api/admin/lists');expect(response.ok()).toBeTruthy()
  const definitions=await response.json() as Array<{id:number;code:string;imagePolicy:string}>
  expect(definitions.find(item=>item.code==='HOME_CAROUSEL')?.imagePolicy).toBe('REQUIRED')
  expect(definitions.find(item=>item.code==='SITE_RELATED')?.imagePolicy).toBe('NONE')

  await page.goto('/admin/lists')
  await expect(page.getByTestId('cms-list-HOME_CAROUSEL')).toBeVisible()
  await page.getByTestId('cms-list-HOME_CAROUSEL').click()
  await expect(page.getByTestId('active-list-image-policy')).toContainText('图片必填')
  await expect(page.getByTestId('cms-list-item-table')).toContainText('这里美得不愿离开')
  await page.getByTestId('cms-list-HOME_CAROUSEL').getByRole('button',{name:'列表操作'}).click()
  await expect(page.getByRole('menuitem',{name:'编辑'})).toBeVisible()
  await expect(page.getByRole('menuitem',{name:'删除'})).toBeVisible()
  await page.keyboard.press('Escape')

  const carouselRow=page.getByTestId('cms-list-item-table').getByRole('row').filter({hasText:'这里美得不愿离开'})
  await carouselRow.getByRole('button',{name:'编辑'}).click()
  const dialog=page.getByRole('dialog',{name:'编辑列表项'})
  await expect(dialog.getByText('标题作为后台识别名称保留')).toBeVisible()
  await dialog.getByTestId('image-resource-upload').setInputFiles({
    name:'e2e-list-image.png',
    mimeType:'image/png',
    buffer:Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=','base64'),
  })
  await expect(dialog.locator('img[alt="当前图片"]')).toHaveAttribute('src',/\/static\/uploads\/lists\/HOME_CAROUSEL\//)
  await dialog.getByRole('button',{name:'取消'}).click()

  await page.getByTestId('cms-list-SITE_RELATED').click()
  await expect(page.getByTestId('active-list-image-policy')).toContainText('不使用图片')
  await page.getByTestId('add-cms-list-item').click()
  await expect(page.getByRole('dialog',{name:'新增列表项'}).getByTestId('list-image-disabled')).toBeVisible()
  await page.getByRole('dialog',{name:'新增列表项'}).getByRole('button',{name:'取消'}).click()
})

test('EU-21：列表图片策略在服务端约束列表项数据', async ({ request }, testInfo) => {
  const suffix=`${Date.now()}-${testInfo.retry}`
  const requiredResponse=await request.post('/api/admin/lists',{data:{code:`E2E_REQUIRED_${suffix.replaceAll('-','_')}`,name:'E2E 图片必填',groupCode:'E2E',imagePolicy:'REQUIRED',description:'',sortOrder:990,enabled:true,system:false}})
  expect(requiredResponse.ok()).toBeTruthy()
  const requiredList=await requiredResponse.json() as {id:number}
  const missingImage=await request.post(`/api/admin/lists/${requiredList.id}/items`,{data:{title:'无图项目',subtitle:null,url:null,imagePath:null,openMode:'DEFAULT',sortOrder:0,enabled:true,extraJson:null}})
  expect(missingImage.ok()).toBeFalsy()
  expect((await missingImage.json() as {message:string}).message).toContain('要求每个列表项设置图片')

  const noneResponse=await request.post('/api/admin/lists',{data:{code:`E2E_NONE_${suffix.replaceAll('-','_')}`,name:'E2E 不使用图片',groupCode:'E2E',imagePolicy:'NONE',description:'',sortOrder:991,enabled:true,system:false}})
  expect(noneResponse.ok()).toBeTruthy()
  const noneList=await noneResponse.json() as {id:number}
  const unexpectedImage=await request.post(`/api/admin/lists/${noneList.id}/items`,{data:{title:'错误带图项目',subtitle:null,url:null,imagePath:'/static/home/carousel-01.jpg',openMode:'DEFAULT',sortOrder:0,enabled:true,extraJson:null}})
  expect(unexpectedImage.ok()).toBeFalsy()
  expect((await unexpectedImage.json() as {message:string}).message).toContain('不使用图片')
})

test('EU-21：宣传展示承载首页运营数据并保留 NO_LINK 行为', async ({ page }) => {
  await page.goto('/admin/advertisements')
  await expect(page.getByRole('heading',{name:'宣传展示管理'})).toBeVisible()
  await expect(page.getByTestId('ad-slot-HOME_RECRUITMENT_PROMO')).toBeVisible()
  await expect(page.getByTestId('advertisement-table')).toContainText('吉林省高校毕业生招聘活动')
  await expect(page.getByTestId('advertisement-table')).toContainText('展示中')
  const promoRow=page.getByTestId('advertisement-table').getByRole('row').filter({hasText:'吉林省高校毕业生招聘活动'})
  await promoRow.getByRole('button',{name:'编辑'}).click()
  await expect(page.getByRole('dialog',{name:'编辑展示内容'})).toBeVisible()
  await expect(page.getByRole('dialog',{name:'编辑展示内容'}).getByTestId('image-resource-picker')).toBeVisible()
  await page.getByTestId('advertisement-open-mode').click()
  await expect(page.getByRole('option',{name:'不跳转，仅展示图片'})).toBeVisible()
  await page.keyboard.press('Escape')
  await page.getByRole('dialog',{name:'编辑展示内容'}).getByRole('button',{name:'取消'}).click()
  await page.getByTestId('ad-slot-HOME_RECRUITMENT_PROMO').getByRole('button',{name:'展示位操作'}).click()
  await expect(page.getByRole('menuitem',{name:'编辑'})).toBeVisible()
  await expect(page.getByRole('menuitem',{name:'删除'})).toBeVisible()
  await page.keyboard.press('Escape')
})

test('EU-21：网站属性使用资源分组、整数展示参数并阻止非法值', async ({ page, request }) => {
  const groupsResponse=await request.get('/api/admin/site-config/groups');expect(groupsResponse.ok()).toBeTruthy()
  const groups=await groupsResponse.json() as Array<{code:string;name:string;order:number}>
  expect(groups.map(group=>group.code)).toEqual(['BASIC','BRAND','CONTACT','FOOTER','PRESENTATION','GENERAL'])

  const key=`E2E_JSON_${Date.now()}`
  const created=await request.post('/api/admin/site-config',{data:{key,name:'E2E JSON 属性',groupCode:'GENERAL',value:'{}',valueType:'JSON',description:'测试运行时属性',sortOrder:0,required:false,system:false,enabled:true}})
  expect(created.ok()).toBeTruthy()
  const unknownGroup=await request.post('/api/admin/site-config',{data:{key:`E2E_UNKNOWN_${Date.now()}`,name:'未知分组属性',groupCode:'UNKNOWN',value:'x',valueType:'TEXT',description:'',sortOrder:0,required:false,system:false,enabled:true}})
  expect(unknownGroup.ok()).toBeFalsy()
  expect((await unknownGroup.json() as {message:string}).message).toContain('网站属性分组不存在')

  await page.goto('/admin/site-config')
  await expect(page.getByTestId('site-property-group-PRESENTATION')).toContainText('展示设置')
  await page.getByTestId('site-property-group-PRESENTATION').click()
  await expect(page.getByTestId('site-property-group-context')).toContainText('展示设置')
  await page.getByTestId('edit-site-config-value-HOME_CAROUSEL_INTERVAL_SECONDS').click()
  const intervalDialog=page.getByTestId('site-config-value-dialog')
  const intervalEditor=intervalDialog.getByTestId('site-config-HOME_CAROUSEL_INTERVAL_SECONDS')
  await expect(intervalEditor).toHaveValue('4')
  await intervalEditor.fill('4.5')
  await intervalDialog.getByTestId('save-site-config-HOME_CAROUSEL_INTERVAL_SECONDS').click()
  await expect(page.getByText('整数属性必须填写整数',{exact:true})).toBeVisible()
  await intervalDialog.getByRole('button',{name:'取消'}).click()

  await page.getByTestId('site-property-group-all').click()
  await page.getByTestId(`edit-site-config-value-${key}`).click()
  const jsonDialog=page.getByTestId('site-config-value-dialog')
  const editor=jsonDialog.getByTestId(`site-config-${key}`)
  await editor.fill('[{not-json}]')
  await jsonDialog.getByTestId(`save-site-config-${key}`).click()
  await expect(page.getByText('JSON 属性格式不正确，请修正后再保存',{exact:true})).toBeVisible()
  await jsonDialog.getByRole('button',{name:'取消'}).click()

  await page.getByTestId('add-site-property').click()
  const definitionDialog=page.getByRole('dialog',{name:'新增网站属性'})
  await expect(definitionDialog.getByTestId('site-property-group-select')).toBeVisible()
  await definitionDialog.getByTestId('site-property-group-select').click()
  await expect(page.getByRole('option',{name:'展示设置'})).toBeVisible()
  await page.keyboard.press('Escape')
  await definitionDialog.getByRole('button',{name:'取消'}).click()
})

test('EU-16：文章管理以栏目树组织内容且父栏目包含子栏目文章', async ({ page, request }, testInfo) => {
  const suffix=`${Date.now()}-${testInfo.retry}`
  const parentName=`栏目树父级-${suffix}`
  const childName=`栏目树子级-${suffix}`

  const parentResponse=await request.post('/api/admin/columns',{data:{parentId:null,name:parentName,alias:`e2e-tree-parent-${suffix}`,coverPolicy:'OPTIONAL',sortOrder:900,enabled:true}})
  expect(parentResponse.ok()).toBeTruthy()
  const parent=await parentResponse.json() as {id:number}

  const childResponse=await request.post('/api/admin/columns',{data:{parentId:parent.id,name:childName,alias:`e2e-tree-child-${suffix}`,coverPolicy:'OPTIONAL',sortOrder:0,enabled:true}})
  expect(childResponse.ok()).toBeTruthy()
  const child=await childResponse.json() as {id:number}

  const articleTitle=`栏目树文章-${suffix}`
  await createArticle(request,child.id,articleTitle)

  await page.goto('/admin/articles')
  await expect(page.getByTestId('article-column-tree')).toBeVisible()
  await expect(page.getByTestId('article-column-all')).toBeVisible()
  await page.getByTestId('article-filter-keyword').fill(articleTitle)
  await expect(page.getByTestId('article-table')).toContainText(articleTitle)

  await page.getByTestId(`article-column-node-${parent.id}`).click()
  await expect(page.getByTestId('article-column-context')).toContainText(parentName)
  await expect(page.getByTestId('article-column-context')).toContainText('包含当前栏目及全部子栏目文章')
  await expect(page.getByTestId('article-table')).toContainText(articleTitle)

  await page.getByTestId('add-article').click()
  const dialog=page.getByRole('dialog',{name:'新增文章草稿'})
  await expect(dialog.getByTestId('article-column-tree-select')).toBeVisible()
  await expect(dialog.getByTestId('article-column-tree-select')).toContainText(parentName)
  await dialog.getByRole('button',{name:'取消'}).click()

  await page.getByTestId(`article-column-node-${child.id}`).click()
  await expect(page.getByTestId('article-column-context')).toContainText(childName)
  await expect(page.getByTestId('article-table')).toContainText(articleTitle)

  await page.getByTestId('article-column-all').click()
  await expect(page.getByTestId('article-column-context')).toContainText('全部文章')
})

test('EU-16：栏目封面策略允许草稿暂存并在发布时强制 REQUIRED', async ({ page, request }, testInfo) => {
  const suffix=`${Date.now()}-${testInfo.retry}`
  const name=`封面必填栏目-${suffix}`
  const columnResponse=await request.post('/api/admin/columns',{data:{parentId:null,name,alias:`e2e-cover-required-${suffix}`,coverPolicy:'REQUIRED',sortOrder:920,enabled:true}})
  expect(columnResponse.ok()).toBeTruthy()
  const column=await columnResponse.json() as {id:number;coverPolicy:string}
  expect(column.coverPolicy).toBe('REQUIRED')

  const article=await createArticle(request,column.id,`封面策略草稿-${suffix}`)
  const publish=await request.post(`/api/admin/articles/${article.id}/publish`)
  expect(publish.ok()).toBeFalsy()
  expect((await publish.json() as {message:string}).message).toContain('补充封面后才能发布')

  await page.goto('/admin/columns')
  await page.getByTestId(`edit-${column.id}`).click()
  await expect(page.getByRole('dialog',{name:'编辑栏目'}).getByTestId('column-cover-policy')).toContainText('发布时必须有封面')
  await page.getByRole('dialog',{name:'编辑栏目'}).getByRole('button',{name:'取消'}).click()

  await page.goto('/admin/articles')
  await page.getByTestId(`article-column-node-${column.id}`).click()
  await page.getByTestId('add-article').click()
  await expect(page.getByRole('dialog',{name:'新增文章草稿'}).getByTestId('article-cover-required')).toBeVisible()
  await page.getByRole('dialog',{name:'新增文章草稿'}).getByRole('button',{name:'取消'}).click()
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

test('EU-21：首页轮播按网站属性中的切换间隔运行', async ({ page, request }, testInfo) => {
  const configResponse=await request.get('/api/admin/site-config');expect(configResponse.ok()).toBeTruthy()
  const config=await configResponse.json() as Array<{key:string;value:string}>
  const originalInterval=config.find(item=>item.key==='HOME_CAROUSEL_INTERVAL_SECONDS')?.value || '4'
  const listsResponse=await request.get('/api/admin/lists');expect(listsResponse.ok()).toBeTruthy()
  const lists=await listsResponse.json() as Array<{id:number;code:string}>
  const carousel=lists.find(item=>item.code==='HOME_CAROUSEL');expect(carousel).toBeTruthy()
  const suffix=`${Date.now()}-${testInfo.retry}`
  let createdId:number|null=null
  try {
    expect((await request.put('/api/admin/site-config/HOME_CAROUSEL_INTERVAL_SECONDS',{data:{value:'1'}})).ok()).toBeTruthy()
    const created=await request.post(`/api/admin/lists/${carousel!.id}/items`,{data:{title:`轮播切换验证-${suffix}`,subtitle:null,url:null,imagePath:'/static/home/carousel-01.jpg',openMode:'DEFAULT',sortOrder:999,enabled:true,extraJson:null}})
    expect(created.ok()).toBeTruthy()
    createdId=(await created.json() as {id:number}).id
    await page.goto('/')
    const active=page.getByTestId('home-carousel-active')
    await expect(active).toBeVisible()
    const firstId=await active.getAttribute('data-carousel-item-id')
    expect(firstId).toBeTruthy()
    await expect(active).not.toHaveAttribute('data-carousel-item-id',firstId!,{timeout:3500})
  } finally {
    if(createdId!=null)await request.delete(`/api/admin/lists/${carousel!.id}/items/${createdId}`)
    await request.put('/api/admin/site-config/HOME_CAROUSEL_INTERVAL_SECONDS',{data:{value:originalInterval}})
  }
})

test('EU-16：单页以左侧分组组织并按 render mode 提供编辑字段', async ({ page, request }, testInfo) => {
  const suffix=`${Date.now()}-${testInfo.retry}`
  const groupsResponse=await request.get('/api/admin/page-groups');expect(groupsResponse.ok()).toBeTruthy()
  const groups=await groupsResponse.json() as Array<{id:number;name:string;alias:string}>
  const group=groups.find(item=>item.alias==='guide')??groups[0];expect(group).toBeTruthy()

  const groupedName=`分组单页-${suffix}`
  const grouped=await request.post('/api/admin/pages',{data:{groupId:group.id,alias:`e2e-grouped-${suffix}`,name:groupedName,bodyHtml:'<p>分组单页</p>',renderMode:'RICH_TEXT',embedUrl:null,sortOrder:900,enabled:true}});expect(grouped.ok()).toBeTruthy()
  const independentName=`独立单页-${suffix}`
  const independent=await request.post('/api/admin/pages',{data:{groupId:null,alias:`e2e-independent-${suffix}`,name:independentName,bodyHtml:'<p>独立单页</p>',renderMode:'RICH_TEXT',embedUrl:null,sortOrder:901,enabled:true}});expect(independent.ok()).toBeTruthy()

  await page.goto('/admin/pages')
  await expect(page.getByRole('heading',{name:'单页管理'})).toBeVisible()
  await expect(page.getByTestId('page-group-all')).toBeVisible()
  await expect(page.getByTestId('page-group-ungrouped')).toBeVisible()
  await expect(page.getByTestId('page-table')).toContainText(groupedName)
  await expect(page.getByTestId('page-table')).toContainText(independentName)

  await page.getByTestId(`page-group-${group.id}`).click()
  await expect(page.getByTestId('page-group-context')).toContainText(group.name)
  await expect(page.getByTestId('page-table')).toContainText(groupedName)
  await expect(page.getByTestId('page-table')).not.toContainText(independentName)

  await page.getByTestId('add-page').click()
  const dialog=page.getByRole('dialog',{name:'新增单页'})
  await expect(dialog.getByTestId('page-group-select')).toContainText(group.name)
  await expect(dialog.getByTestId('page-body-editor')).toBeVisible()
  await dialog.getByTestId('page-render-mode').click();await page.getByRole('option',{name:'外部嵌入占位'}).click();await expect(dialog.getByTestId('page-embed-url')).toBeVisible();await expect(dialog.getByTestId('page-placeholder-body')).toBeVisible()
  await dialog.getByRole('button',{name:'取消'}).click()

  await page.getByTestId('page-group-ungrouped').click()
  await expect(page.getByTestId('page-group-context')).toContainText('独立单页')
  await expect(page.getByTestId('page-table')).toContainText(independentName)
  await expect(page.getByTestId('page-table')).not.toContainText(groupedName)
})

test('EU-17：静态资源拒绝伪装 PNG 并保护站点受保护资源', async ({ page, request }) => {
  const fakeUpload=await request.post('/api/admin/static-resources?path=verification-fake/fake.png&replace=false',{multipart:{file:{name:'fake.png',mimeType:'image/png',buffer:Buffer.from('not a png')}}});expect(fakeUpload.ok()).toBeFalsy();expect((await fakeUpload.json() as {message:string}).message).toContain('实际内容')
  const homeList=await request.get('/api/admin/static-resources?path=home');expect(homeList.ok()).toBeTruthy();const rows=await homeList.json() as Array<{path:string;protectedResource:boolean}>;expect(rows.find(row=>row.path==='home/ncss-logo.png')?.protectedResource).toBeTruthy()
  const deleteResponse=await request.delete('/api/admin/static-resources?path=home%2Fncss-logo.png');expect(deleteResponse.ok()).toBeFalsy();expect((await deleteResponse.json() as {message:string}).message).toContain('受保护资源')
  await page.goto('/admin/static-resources');const homeRow=page.getByTestId('static-resource-table').getByRole('row').filter({hasText:'home'});await homeRow.getByRole('button',{name:'进入'}).click();const ncssRow=page.getByTestId('static-resource-table').getByRole('row').filter({hasText:'ncss-logo.png'});await expect(ncssRow).toContainText('受保护')
})
