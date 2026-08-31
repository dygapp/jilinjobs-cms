import { expect, test } from '@playwright/test'

const tinyPng=Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=','base64')

test('统一图片选择器可以跨 CMS 模块复用 Runtime 已上传图片', async ({ page, request }) => {
  const name=`shared-${Date.now()}.png`
  const relativePath=`uploads/shared-e2e/${name}`
  const upload=await request.post(`/api/admin/static-resources?path=${encodeURIComponent(relativePath)}&replace=false`,{
    multipart:{file:{name,mimeType:'image/png',buffer:tinyPng}},
  })
  expect(upload.ok()).toBeTruthy()

  await page.goto('/admin/lists')
  await page.getByTestId('cms-list-HOME_CAROUSEL').click()
  const row=page.getByTestId('cms-list-item-table').getByRole('row').filter({hasText:'这里美得不愿离开'})
  await row.getByRole('button',{name:'编辑'}).click()
  const editDialog=page.getByRole('dialog',{name:'编辑列表项'})
  await editDialog.getByRole('button',{name:'选择已有图片'}).click()
  const library=page.getByRole('dialog',{name:'选择已有图片'})
  const option=library.getByRole('button').filter({hasText:name})
  await expect(option).toBeVisible()
  await option.click()
  await expect(editDialog.locator('img[alt="当前图片"]')).toHaveAttribute('src',`/static/${relativePath}`)
  await editDialog.getByRole('button',{name:'取消'}).click()
})
