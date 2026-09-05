import { expect, test, type Locator } from '@playwright/test'

async function expectFormLabel(dialog: Locator, label: string) {
  await expect(dialog.getByText(label, { exact: true }).first()).toBeVisible()
}

test('EU-33：结构身份使用业务标识且实现说明退出运营界面', async ({ page }) => {
  await page.goto('/admin/columns')
  await page.getByTestId('add-column').click()
  let dialog = page.getByRole('dialog', { name: '新增栏目' })
  await expectFormLabel(dialog, '公开标识')
  await expect(dialog.getByText(/不决定公开页面/)).toHaveCount(0)
  await dialog.getByRole('button', { name: '取消' }).click()

  await page.goto('/admin/pages')
  await page.getByTestId('add-page-group').click()
  dialog = page.getByRole('dialog', { name: '新增单页分组' })
  await expectFormLabel(dialog, '公开标识')
  await dialog.getByRole('button', { name: '取消' }).click()

  await page.getByTestId('add-page').click()
  dialog = page.getByRole('dialog', { name: '新增单页' })
  await expectFormLabel(dialog, '公开标识')
  await page.getByTestId('page-render-mode').click()
  await page.getByRole('option', { name: '站内特殊页面' }).click()
  await expect(dialog.getByText(/前端工程|HTML\/JS|Requirement/)).toHaveCount(0)
  await page.getByTestId('page-render-mode').click()
  await page.getByRole('option', { name: '外部嵌入占位' }).click()
  await expect(dialog.getByText('当前只保存嵌入地址和占位说明，不直接加载第三方内容。')).toBeVisible()
  await expect(dialog.getByText(/Requirement/)).toHaveCount(0)
  await dialog.getByRole('button', { name: '取消' }).click()

  await page.goto('/admin/lists')
  await page.getByTestId('add-cms-list').click()
  dialog = page.getByRole('dialog', { name: '新增列表' })
  await expectFormLabel(dialog, '列表标识')
  await expect(dialog.getByText(/不决定公开页面/)).toHaveCount(0)
  await dialog.getByRole('button', { name: '取消' }).click()

  await page.goto('/admin/navigation')
  await page.getByTestId('add-navigation-location').click()
  dialog = page.getByRole('dialog', { name: '新增导航位置' })
  await expectFormLabel(dialog, '位置标识')
  await dialog.getByRole('button', { name: '取消' }).click()
  await page.getByTestId('add-navigation').click()
  dialog = page.getByRole('dialog', { name: '新增导航' })
  await expect(dialog.getByTestId('navigation-location-context')).toBeVisible()
  await expect(dialog.locator('.el-form-item').filter({ hasText: '导航位置' }).locator('input')).toHaveCount(0)
  await dialog.getByRole('button', { name: '取消' }).click()

  await page.goto('/admin/advertisements')
  await page.getByTestId('add-ad-slot').click()
  dialog = page.getByRole('dialog', { name: '新增展示位' })
  await expectFormLabel(dialog, '展示位标识')
  await dialog.getByRole('button', { name: '取消' }).click()

  await page.goto('/admin/site-config')
  await expect(page.getByText(/CMS 资源元数据|部署资源配置|CMS 数据库|工程\/部署/)).toHaveCount(0)
  await page.getByTestId('add-site-property').click()
  dialog = page.getByRole('dialog', { name: '新增网站属性' })
  await expectFormLabel(dialog, '属性标识')
  await expect(dialog.getByText(/部署资源配置|CMS 数据库/)).toHaveCount(0)
  await dialog.getByRole('button', { name: '取消' }).click()
})

test('EU-33：冗余提示消失且必要操作与风险信息继续存在', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const noneResponse = await request.post('/api/admin/columns', {
    data: { parentId: null, name: `EU33 无封面 ${suffix}`, alias: `e2e-eu33-none-${suffix}`, coverPolicy: 'NONE', sortOrder: 980, enabled: true },
  })
  expect(noneResponse.ok()).toBeTruthy()
  const noneColumn = await noneResponse.json() as { id: number }

  const requiredResponse = await request.post('/api/admin/columns', {
    data: { parentId: null, name: `EU33 必填封面 ${suffix}`, alias: `e2e-eu33-required-${suffix}`, coverPolicy: 'REQUIRED', sortOrder: 981, enabled: true },
  })
  expect(requiredResponse.ok()).toBeTruthy()
  const requiredColumn = await requiredResponse.json() as { id: number }

  try {
    await page.goto('/admin/articles')
    await page.getByTestId(`article-column-node-${noneColumn.id}`).click()
    await page.getByTestId('add-article').click()
    let dialog = page.getByRole('dialog', { name: '新增文章草稿' })
    await expect(dialog.getByTestId('article-cover-disabled')).toHaveCount(0)
    await expect(dialog.getByTestId('cover-input')).toHaveCount(0)
    await expect(dialog.getByText('新建文章固定保存为草稿；普通编辑不会改变当前发布状态。')).toBeVisible()
    await dialog.getByRole('button', { name: '取消' }).click()

    await page.getByTestId(`article-column-node-${requiredColumn.id}`).click()
    await page.getByTestId('add-article').click()
    dialog = page.getByRole('dialog', { name: '新增文章草稿' })
    await expect(dialog.getByTestId('article-cover-required')).toHaveText('草稿可暂存，发布前必须设置封面。')
    await expect(dialog.getByText('公开排序优先级：置顶 → 展示顺序 → 发布日期。')).toBeVisible()
    await dialog.getByRole('button', { name: '取消' }).click()

    await page.goto('/admin/static-resources')
    await expect(page.getByText(/受保护资源不能直接删除/).first()).toBeVisible()
    await expect(page.getByText(/系统不会自动检查所有引用/).first()).toBeVisible()
    await expect(page.getByText(/CSS|JS|富文本/)).toHaveCount(0)
  } finally {
    await request.delete(`/api/admin/columns/${noneColumn.id}`)
    await request.delete(`/api/admin/columns/${requiredColumn.id}`)
  }
})
