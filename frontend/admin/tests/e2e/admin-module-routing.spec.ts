import { readFile, readdir } from 'node:fs/promises'
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

async function sourceFiles(root: URL): Promise<URL[]> {
  const entries = await readdir(root, { withFileTypes: true })
  const files: URL[] = []
  for (const entry of entries) {
    if (entry.isDirectory()) files.push(...await sourceFiles(new URL(`${entry.name}/`, root)))
    else if (/\.(ts|vue|css)$/.test(entry.name)) files.push(new URL(entry.name, root))
  }
  return files
}

function imports(source: string): string[] {
  return [...source.matchAll(/(?:from\s+|import\s*)['"]([^'"]+)['"]/g)].map(match => match[1])
}

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

test('Shell 样式不持有 CMS 私有选择器', async () => {
  const shellStyles = await readFile(new URL('../../src/app/admin.css', import.meta.url), 'utf8')
  const sharedStyles = await readFile(new URL('../../src/shared/admin-content.css', import.meta.url), 'utf8')
  const cmsStyles = await readFile(new URL('../../src/modules/cms/admin.css', import.meta.url), 'utf8')

  for (const selector of [
    '.article-',
    '.page-management-',
    '.page-group-',
    '.site-config-',
    '.editor-',
    '.rich-editor',
    '.resource-',
    '.attachment-',
    '.upload-button',
    '.path-bar',
    '.config-type',
  ]) expect(shellStyles).not.toContain(selector)

  expect(sharedStyles).toContain('.admin-shell')
  expect(sharedStyles).toContain('.admin-split-layout')
  expect(cmsStyles).toContain('.article-management-layout')
  expect(cmsStyles).toContain('.page-management-layout')
  expect(cmsStyles).toContain('.site-config-value-preview')
})

test('Admin 源码依赖只通过 Registry composition 与 Module Contract 穿越边界', async () => {
  const appFiles = await sourceFiles(new URL('../../src/app/', import.meta.url))
  for (const file of appFiles) {
    if (file.pathname.endsWith('/moduleRegistry.ts')) continue
    const source = await readFile(file, 'utf8')
    expect(imports(source).filter(path => path.includes('/modules/')), `${file.pathname} 不应直接依赖业务模块`).toEqual([])
  }

  const moduleFiles = await sourceFiles(new URL('../../src/modules/', import.meta.url))
  for (const file of moduleFiles) {
    const source = await readFile(file, 'utf8')
    const appImports = imports(source).filter(path => path.includes('/app/'))
    if (file.pathname.endsWith('/module.ts')) {
      expect(appImports, `${file.pathname} 只能依赖公开 Module Contract`).toEqual(['../../app/adminModule'])
    } else {
      expect(appImports, `${file.pathname} 不应依赖 Shell 内部实现`).toEqual([])
    }
  }
})
