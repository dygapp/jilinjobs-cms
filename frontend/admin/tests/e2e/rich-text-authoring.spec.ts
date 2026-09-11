import { expect, test } from '@playwright/test'
import { existsSync, readFileSync } from 'node:fs'
import path from 'node:path'

const REPO_ROOT = process.env.EU54_REPO_ROOT || path.resolve(process.cwd(), '../..')
const PARTY_PATH = path.join(REPO_ROOT, 'data-migrations/party/v1/articles/zhutijiaoyu-content-154659859759104/article.json')
const PAGES_PATH = path.join(REPO_ROOT, 'sites/jilinjobs/structure/pages.json')
const REAL_CORPUS_AVAILABLE = existsSync(PARTY_PATH) && existsSync(PAGES_PATH)
const PARTY_MARKER = 'EU54_PARTY_ROUNDTRIP_MARKER'
const TEACHER_MARKER = 'EU54_TEACHER_ROUNDTRIP_MARKER'
const EXPECTED_EDITOR_FONTS = [
  'Microsoft YaHei',
  'SimSun',
  'KaiTi',
  'FangSong',
  'PingFang SC',
  'Noto Sans CJK SC',
  'Source Han Sans SC',
  'Arial',
  'Times New Roman',
]

function readRealCorpus() {
  const party = JSON.parse(readFileSync(PARTY_PATH, 'utf8')) as { content: { bodyHtml: string } }
  const pages = JSON.parse(readFileSync(PAGES_PATH, 'utf8')) as unknown
  const teacher = findObject(pages, value => value?.alias === 'teacher-library' && typeof value?.bodyHtml === 'string')
  if (!party.content?.bodyHtml || !teacher?.bodyHtml) throw new Error('EU-54 real corpus is unavailable')
  return { partyHtml: party.content.bodyHtml, teacherHtml: String(teacher.bodyHtml) }
}

function findObject(value: unknown, predicate: (candidate: any) => boolean): any | null {
  if (value && typeof value === 'object') {
    if (predicate(value)) return value
    for (const child of (Array.isArray(value) ? value : Object.values(value))) {
      const found = findObject(child, predicate)
      if (found) return found
    }
  }
  return null
}

async function firstColumnId(request: import('@playwright/test').APIRequestContext): Promise<number> {
  const response = await request.get('/api/admin/columns')
  expect(response.ok()).toBeTruthy()
  const columns = await response.json() as Array<{ id: number; alias: string }>
  return (columns.find(item => item.alias === 'notice') ?? columns[0]).id
}

async function appendMarker(surface: import('@playwright/test').Locator, marker: string) {
  await surface.evaluate(node => {
    const range = document.createRange()
    range.selectNodeContents(node)
    range.collapse(false)
    const selection = window.getSelection()
    selection?.removeAllRanges()
    selection?.addRange(range)
    ;(node as HTMLElement).focus()
  })
  await surface.press('Enter')
  await surface.page().keyboard.insertText(marker)
  await expect(surface).toContainText(marker)
}

test('EU-54：SunEditor 3.3.3 生产适配器保持 P1/P2 real corpus 并可继续编辑保存', async ({ page, request }, testInfo) => {
  test.skip(!REAL_CORPUS_AVAILABLE, 'P1/P2 exact corpus runs in the dedicated EU-54 repository-mounted verification path')
  const { partyHtml, teacherHtml } = readRealCorpus()
  const suffix = `${Date.now()}-${testInfo.retry}`
  const columnId = await firstColumnId(request)

  const articleTitle = `EU54-P1-${suffix}`
  const articleResponse = await request.post('/api/admin/articles', { data: {
    columnId, title: articleTitle, bodyHtml: partyHtml, source: 'EU-54 P1 real corpus', articleType: 'INTERNAL', externalUrl: null,
    publishDate: '2026-09-11', pinned: false, sortOrder: 0, coverResourceId: null, bodyImageResourceIds: [], attachmentResourceIds: [],
  } })
  expect(articleResponse.ok()).toBeTruthy()
  const article = await articleResponse.json() as { id: number }

  await page.goto('/admin/articles')
  await page.getByTestId('article-filter-keyword').fill(articleTitle)
  await page.getByTestId('article-table').getByRole('row').filter({ hasText: articleTitle }).getByRole('button', { name: '编辑' }).click()
  const articleDialog = page.getByRole('dialog', { name: '编辑文章' })
  const articleSurface = articleDialog.getByTestId('article-body-editor')
  await expect(articleSurface).toBeVisible()
  await expect(articleSurface).toContainText('中共中央政治局召开会议')
  await expect(articleSurface.locator('img')).toHaveCount(23)
  const firstStar = articleSurface.locator('img').first()
  await expect(firstStar).toHaveAttribute('width', '15')
  await expect(firstStar).toHaveAttribute('height', '15')
  await expect(firstStar.locator('xpath=ancestor::strong[1]')).toHaveCount(1)
  const starRect = await firstStar.boundingBox()
  expect(Math.round(starRect?.width ?? 0)).toBe(15)
  expect(Math.round(starRect?.height ?? 0)).toBe(15)
  await appendMarker(articleSurface, PARTY_MARKER)
  await articleDialog.getByTestId('save-article').click()
  await expect(articleDialog).toBeHidden()
  const storedArticle = await (await request.get(`/api/admin/articles/${article.id}`)).json() as { bodyHtml: string }
  expect(storedArticle.bodyHtml).toContain(PARTY_MARKER)
  expect((storedArticle.bodyHtml.match(/<img\b/gi) || [])).toHaveLength(23)
  expect(storedArticle.bodyHtml).toMatch(/<img[^>]*height="15"[^>]*width="15"|<img[^>]*width="15"[^>]*height="15"/i)

  const pageName = `EU54-P2-${suffix}`
  const pageAlias = `eu54-teacher-${suffix}`
  const pageResponse = await request.post('/api/admin/pages', { data: {
    groupId: null, alias: pageAlias, name: pageName, bodyHtml: teacherHtml,
    renderMode: 'RICH_TEXT', embedUrl: null, sortOrder: 999, enabled: true,
  } })
  expect(pageResponse.ok()).toBeTruthy()
  const savedPage = await pageResponse.json() as { id: number }

  await page.goto('/admin/pages')
  await page.getByTestId(`edit-page-${savedPage.id}`).click()
  const pageDialog = page.getByRole('dialog', { name: '编辑单页' })
  const pageSurface = pageDialog.getByTestId('page-body-editor')
  await expect(pageSurface).toContainText('方占仁')
  await expect(pageSurface).toContainText('李军凯')
  const table = pageSurface.locator('table').first()
  await expect(table).toHaveAttribute('align', 'center')
  await expect(table).toHaveAttribute('cellpadding', '1')
  await expect(table).toHaveAttribute('cellspacing', '1')
  await expect(table).toHaveAttribute('style', /width:\s*1170px/i)
  const firstTd = pageSurface.locator('td').first()
  await expect(firstTd).toHaveAttribute('style', /height:\s*325px/i)
  await expect(firstTd).toHaveAttribute('style', /width:\s*200px/i)
  const teacherImage = pageSurface.locator('img').first()
  await expect(teacherImage).toHaveAttribute('width', '200')
  await expect(teacherImage).toHaveAttribute('height', '266')
  await expect(teacherImage).toHaveAttribute('style', /float:\s*left/i)
  const teacherRect = await teacherImage.boundingBox()
  expect(Math.round(teacherRect?.width ?? 0)).toBe(200)
  expect(Math.round(teacherRect?.height ?? 0)).toBe(266)
  await appendMarker(pageSurface, TEACHER_MARKER)
  await pageDialog.getByRole('button', { name: '保存' }).click()
  await expect(pageDialog).toBeHidden()
  const storedPages = await (await request.get('/api/admin/pages')).json() as Array<{ id: number; bodyHtml: string }>
  const storedPage = storedPages.find(item => item.id === savedPage.id)
  expect(storedPage?.bodyHtml).toContain(TEACHER_MARKER)
  expect(storedPage?.bodyHtml).toContain('width:1170px')
  expect(storedPage?.bodyHtml).toContain('float:left')
})

test('EU-54：普通中文编辑、中文字体、撤销重做与共享 Article/Page adapter 可用', async ({ page }) => {
  await page.goto('/admin/pages')
  await page.getByTestId('add-page').click()
  const dialog = page.getByRole('dialog', { name: '新增单页' })
  const root = dialog.getByTestId('page-body-editor-shell')
  const surface = dialog.getByTestId('page-body-editor')
  await expect(surface).toBeVisible()
  for (const command of ['undo', 'redo', 'bold', 'italic', 'underline', 'strike', 'font', 'align', 'list', 'table', 'link', 'image']) {
    await expect(root.locator(`[data-command="${command}"]`)).toBeVisible()
  }
  await expect(root.getByLabel('字号')).toBeVisible()

  const defaultFontFamily = await surface.evaluate(node => getComputedStyle(node).fontFamily)
  expect(defaultFontFamily).toContain('Microsoft YaHei')
  expect(defaultFontFamily).toContain('PingFang SC')
  expect(defaultFontFamily).not.toContain('Helvetica Neue')
  for (const font of EXPECTED_EDITOR_FONTS) {
    await expect(root.locator(`.se-list-font-family [data-command="${font}"]`)).toHaveCount(1)
  }

  await surface.fill('吉林省高校毕业生就业服务')
  await expect(surface).toContainText('吉林省高校毕业生就业服务')
  const editorHtml = await surface.evaluate(node => node.innerHTML)
  expect(editorHtml).not.toContain('Microsoft YaHei')
  await root.locator('[data-command="undo"]').click()
  await expect(surface).not.toContainText('吉林省高校毕业生就业服务')
  await root.locator('[data-command="redo"]').click()
  await expect(surface).toContainText('吉林省高校毕业生就业服务')
})

test('EU-54：粘贴 hostile HTML 后服务端共享安全边界继续生效', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  await page.goto('/admin/pages')
  await page.getByTestId('add-page').click()
  const dialog = page.getByRole('dialog', { name: '新增单页' })
  await dialog.getByRole('textbox', { name: '单页名称' }).fill(`EU54安全粘贴-${suffix}`)
  await dialog.getByRole('textbox', { name: '公开标识' }).fill(`eu54-paste-${suffix}`)
  const surface = dialog.getByTestId('page-body-editor')
  await surface.evaluate((node, html) => {
    const data = new DataTransfer()
    data.setData('text/html', String(html))
    data.setData('text/plain', '安全文字 保留颜色')
    node.dispatchEvent(new ClipboardEvent('paste', { clipboardData: data, bubbles: true, cancelable: true }))
  }, '<p class="unknown" data-extra="x" onclick="alert(1)">安全文字 <span style="color:#123456;position:fixed">保留颜色</span></p><script>alert(1)</script><iframe src="https://example.com"></iframe>')
  await expect(surface).toContainText('安全文字')
  await dialog.getByRole('button', { name: '保存' }).click()
  await expect(dialog).toBeHidden()
  const pages = await (await request.get('/api/admin/pages')).json() as Array<{ alias: string; bodyHtml: string }>
  const saved = pages.find(item => item.alias === `eu54-paste-${suffix}`)
  expect(saved).toBeTruthy()
  expect(saved!.bodyHtml).not.toMatch(/script|iframe|onclick|position\s*:/i)
  expect(saved!.bodyHtml).toContain('安全文字')
  expect(saved!.bodyHtml).toContain('color')
})
