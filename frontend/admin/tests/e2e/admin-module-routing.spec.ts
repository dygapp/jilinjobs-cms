import { readFile } from 'node:fs/promises'
import { expect, test } from '@playwright/test'

const canonicalRoutes = [
  ['articles', '/admin/cms/articles'],
  ['pages', '/admin/cms/pages'],
  ['lists', '/admin/cms/lists'],
  ['columns', '/admin/cms/columns'],
  ['navigation', '/admin/cms/navigation'],
  ['advertisements', '/admin/cms/advertisements'],
  ['site-config', '/admin/cms/site-config'],
  ['static-resources', '/admin/cms/static-resources'],
] as const

test('Admin Shell 使用 CMS canonical 路由命名空间', async ({ page }) => {
  await page.goto('/admin/')
  await expect(page).toHaveURL(/\/admin\/cms\/articles$/)

  for (const [id, url] of canonicalRoutes) {
    await expect(page.getByTestId(`admin-nav-${id}`)).toHaveAttribute('href', url)
  }
})

test('旧 CMS 管理路径重定向到 canonical 路由', async ({ page }) => {
  for (const [id, canonical] of canonicalRoutes) {
    await page.goto(`/admin/${id}`)
    await expect(page).toHaveURL(new RegExp(`${canonical.replaceAll('/', '\\/')}$`))
  }
})

test('未知管理端路径回到模块注册表声明的默认入口', async ({ page }) => {
  await page.goto('/admin/not-a-real-module/unknown')
  await expect(page).toHaveURL(/\/admin\/cms\/articles$/)
  await expect(page.getByRole('heading', { name: '文章管理' })).toBeVisible()
})

test('Shell Router 不重新持有 CMS feature 路由知识', async () => {
  const routerSource = await readFile(new URL('../../src/app/router.ts', import.meta.url), 'utf8')
  expect(routerSource).toContain('adminDefaultRoute')
  expect(routerSource).toContain('adminModuleRoutes')
  expect(routerSource).not.toContain('/cms/')
  expect(routerSource).not.toContain("'/articles'")
  expect(routerSource).not.toContain("'/lists'")
})
