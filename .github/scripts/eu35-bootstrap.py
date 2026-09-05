from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ADMIN = ROOT / "frontend/admin"


def replace_once(path: Path, old: str, new: str) -> None:
    text = path.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{path}: expected exactly one match, found {count}: {old[:80]!r}")
    path.write_text(text.replace(old, new, 1), encoding="utf-8")


package_path = ADMIN / "package.json"
package = json.loads(package_path.read_text(encoding="utf-8"))
package["dependencies"].update(
    {
        "@tiptap/extension-image": "3.31.2",
        "@tiptap/extension-table": "3.31.2",
        "@tiptap/extension-text-align": "3.31.2",
        "@tiptap/extension-text-style": "3.31.2",
        "@tiptap/pm": "3.31.2",
        "@tiptap/starter-kit": "3.31.2",
        "@tiptap/vue-3": "3.31.2",
    }
)
package_path.write_text(json.dumps(package, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

component = r'''<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
import Image from '@tiptap/extension-image'
import { TableKit } from '@tiptap/extension-table'
import TextAlign from '@tiptap/extension-text-align'
import { TextStyleKit } from '@tiptap/extension-text-style'
import StarterKit from '@tiptap/starter-kit'
import { EditorContent, useEditor } from '@tiptap/vue-3'

interface UploadedImage {
  src: string
  alt?: string
}

const props = withDefaults(defineProps<{
  modelValue: string
  testId: string
  uploadImage?: (file: File) => Promise<UploadedImage>
  uploading?: boolean
}>(), {
  modelValue: '',
  uploading: false,
})

const emit = defineEmits<{ (event: 'update:modelValue', value: string): void }>()
const fileInput = ref<HTMLInputElement | null>(null)
const blockType = ref<'paragraph' | '2' | '3' | '4'>('paragraph')
const fontSize = ref('16px')
const fontFamily = ref('Microsoft YaHei')

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit.configure({
      heading: { levels: [2, 3, 4] },
      link: { openOnClick: false, autolink: false, defaultProtocol: 'https' },
    }),
    Image.configure({ allowBase64: false }),
    TextAlign.configure({ types: ['heading', 'paragraph'] }),
    TextStyleKit,
    TableKit,
  ],
  editorProps: {
    attributes: {
      class: 'rich-editor',
      'data-testid': props.testId,
    },
  },
  onUpdate: ({ editor: current }) => emit('update:modelValue', current.getHTML()),
})

watch(() => props.modelValue, value => {
  const current = editor.value
  if (!current || current.getHTML() === value) return
  current.commands.setContent(value || '', { emitUpdate: false })
})

onBeforeUnmount(() => editor.value?.destroy())

function setBlock(value: 'paragraph' | '2' | '3' | '4') {
  blockType.value = value
  if (value === 'paragraph') editor.value?.chain().focus().setParagraph().run()
  else editor.value?.chain().focus().setHeading({ level: Number(value) as 2 | 3 | 4 }).run()
}

function setFontSize(value: string) {
  fontSize.value = value
  editor.value?.chain().focus().setFontSize(value).run()
}

function setFontFamily(value: string) {
  fontFamily.value = value
  editor.value?.chain().focus().setFontFamily(value).run()
}

function setTextColor(value: string | null) {
  if (value) editor.value?.chain().focus().setColor(value).run()
}

function setBackgroundColor(value: string | null) {
  if (value) editor.value?.chain().focus().setBackgroundColor(value).run()
}

function safeUrl(value: string): boolean {
  return value.startsWith('/') || /^https?:\/\//i.test(value)
}

function editLink() {
  const current = editor.value
  if (!current) return
  const existing = String(current.getAttributes('link').href || '')
  const raw = window.prompt('链接地址', existing || 'https://')
  if (raw == null) return
  const url = raw.trim()
  if (!url) {
    current.chain().focus().extendMarkRange('link').unsetLink().run()
    return
  }
  if (!safeUrl(url)) return
  current.chain().focus().extendMarkRange('link').setLink({ href: url }).run()
}

function requestImage() {
  if (props.uploadImage) {
    fileInput.value?.click()
    return
  }
  const raw = window.prompt('图片地址', 'https://')
  if (raw == null) return
  const src = raw.trim()
  if (!safeUrl(src)) return
  editor.value?.chain().focus().setImage({ src }).run()
}

async function handleImageFile(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !props.uploadImage) return
  const image = await props.uploadImage(file)
  if (!safeUrl(image.src)) return
  editor.value?.chain().focus().setImage({ src: image.src, alt: image.alt || '' }).run()
}
</script>

<template>
  <div class="rich-text-editor-shell" :data-testid="`${testId}-shell`">
    <div v-if="editor" class="rich-text-editor-toolbar" :data-testid="`${testId}-toolbar`">
      <el-button-group>
        <el-button size="small" :data-testid="`${testId}-undo`" @click="editor.chain().focus().undo().run()">撤销</el-button>
        <el-button size="small" :data-testid="`${testId}-redo`" @click="editor.chain().focus().redo().run()">重做</el-button>
      </el-button-group>

      <el-select :model-value="blockType" size="small" class="toolbar-select block-select" :data-testid="`${testId}-block`" @change="setBlock">
        <el-option label="正文" value="paragraph" />
        <el-option label="标题 2" value="2" />
        <el-option label="标题 3" value="3" />
        <el-option label="标题 4" value="4" />
      </el-select>

      <el-button-group>
        <el-button size="small" :type="editor.isActive('bold') ? 'primary' : 'default'" :data-testid="`${testId}-bold`" @click="editor.chain().focus().toggleBold().run()"><strong>B</strong></el-button>
        <el-button size="small" :type="editor.isActive('italic') ? 'primary' : 'default'" :data-testid="`${testId}-italic`" @click="editor.chain().focus().toggleItalic().run()"><em>I</em></el-button>
        <el-button size="small" :type="editor.isActive('underline') ? 'primary' : 'default'" :data-testid="`${testId}-underline`" @click="editor.chain().focus().toggleUnderline().run()"><u>U</u></el-button>
        <el-button size="small" :type="editor.isActive('strike') ? 'primary' : 'default'" :data-testid="`${testId}-strike`" @click="editor.chain().focus().toggleStrike().run()"><s>S</s></el-button>
      </el-button-group>

      <el-button-group>
        <el-button size="small" :data-testid="`${testId}-bullet-list`" @click="editor.chain().focus().toggleBulletList().run()">项目符号</el-button>
        <el-button size="small" :data-testid="`${testId}-ordered-list`" @click="editor.chain().focus().toggleOrderedList().run()">编号</el-button>
        <el-button size="small" :data-testid="`${testId}-blockquote`" @click="editor.chain().focus().toggleBlockquote().run()">引用</el-button>
        <el-button size="small" :data-testid="`${testId}-hr`" @click="editor.chain().focus().setHorizontalRule().run()">分隔线</el-button>
      </el-button-group>

      <el-button-group>
        <el-button size="small" :data-testid="`${testId}-align-left`" @click="editor.chain().focus().setTextAlign('left').run()">左</el-button>
        <el-button size="small" :data-testid="`${testId}-align-center`" @click="editor.chain().focus().setTextAlign('center').run()">中</el-button>
        <el-button size="small" :data-testid="`${testId}-align-right`" @click="editor.chain().focus().setTextAlign('right').run()">右</el-button>
      </el-button-group>

      <el-select :model-value="fontSize" size="small" class="toolbar-select font-size-select" :data-testid="`${testId}-font-size`" @change="setFontSize">
        <el-option v-for="size in ['14px','16px','18px','20px','24px','28px']" :key="size" :label="size" :value="size" />
      </el-select>
      <el-select :model-value="fontFamily" size="small" class="toolbar-select font-family-select" :data-testid="`${testId}-font-family`" @change="setFontFamily">
        <el-option label="微软雅黑" value="Microsoft YaHei" />
        <el-option label="宋体" value="SimSun" />
        <el-option label="Arial" value="Arial" />
      </el-select>
      <span class="color-control">文字<el-color-picker size="small" :data-testid="`${testId}-text-color`" @change="setTextColor" /></span>
      <span class="color-control">底色<el-color-picker size="small" :data-testid="`${testId}-background-color`" @change="setBackgroundColor" /></span>

      <el-button-group>
        <el-button size="small" :data-testid="`${testId}-link`" @click="editLink">链接</el-button>
        <el-button size="small" :data-testid="`${testId}-unlink`" @click="editor.chain().focus().unsetLink().run()">取消链接</el-button>
        <el-button size="small" :data-testid="`${testId}-image`" :loading="uploading" @click="requestImage">{{ uploadImage ? '插入图片' : '图片地址' }}</el-button>
      </el-button-group>

      <el-button-group>
        <el-button size="small" :data-testid="`${testId}-table`" @click="editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()">插入表格</el-button>
        <el-button size="small" :data-testid="`${testId}-table-add-row`" @click="editor.chain().focus().addRowAfter().run()">加行</el-button>
        <el-button size="small" :data-testid="`${testId}-table-delete-row`" @click="editor.chain().focus().deleteRow().run()">删行</el-button>
        <el-button size="small" :data-testid="`${testId}-table-add-column`" @click="editor.chain().focus().addColumnAfter().run()">加列</el-button>
        <el-button size="small" :data-testid="`${testId}-table-delete-column`" @click="editor.chain().focus().deleteColumn().run()">删列</el-button>
        <el-button size="small" :data-testid="`${testId}-table-delete`" @click="editor.chain().focus().deleteTable().run()">删表</el-button>
      </el-button-group>
    </div>

    <input v-if="uploadImage" ref="fileInput" data-testid="body-image-input" class="hidden-file-input" type="file" accept="image/*" :disabled="uploading" @change="handleImageFile">
    <EditorContent :editor="editor" class="rich-text-editor-content" />
  </div>
</template>

<style scoped>
.rich-text-editor-shell { width: 100%; border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden; background: #fff; }
.rich-text-editor-toolbar { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; padding: 8px; border-bottom: 1px solid #ebeef5; background: #f8f9fb; }
.toolbar-select { width: 104px; }
.font-family-select { width: 118px; }
.color-control { display: inline-flex; align-items: center; gap: 4px; color: #606266; font-size: 12px; }
.hidden-file-input { display: none; }
.rich-text-editor-content :deep(.rich-editor) { min-height: 240px; padding: 14px 16px; outline: none; line-height: 1.75; word-break: break-word; }
.rich-text-editor-content :deep(.rich-editor p) { margin: 0 0 0.8em; }
.rich-text-editor-content :deep(.rich-editor h2),
.rich-text-editor-content :deep(.rich-editor h3),
.rich-text-editor-content :deep(.rich-editor h4) { margin: 1em 0 0.55em; line-height: 1.4; }
.rich-text-editor-content :deep(.rich-editor blockquote) { margin: 0.8em 0; padding-left: 12px; border-left: 3px solid #dcdfe6; color: #606266; }
.rich-text-editor-content :deep(.rich-editor img) { max-width: 100%; height: auto; }
.rich-text-editor-content :deep(.rich-editor table) { width: 100%; border-collapse: collapse; margin: 0.8em 0; }
.rich-text-editor-content :deep(.rich-editor th),
.rich-text-editor-content :deep(.rich-editor td) { border: 1px solid #dcdfe6; padding: 6px 8px; vertical-align: top; }
</style>
'''
(ADMIN / "src/modules/cms/components/RichTextEditor.vue").write_text(component, encoding="utf-8")

article = ADMIN / "src/modules/cms/views/admin/ArticleManagementView.vue"
replace_once(article, "import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'", "import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'")
replace_once(article, "import AdminPanelToggle from '../../components/AdminPanelToggle.vue'", "import AdminPanelToggle from '../../components/AdminPanelToggle.vue'\nimport RichTextEditor from '../../components/RichTextEditor.vue'")
replace_once(article, "const editorRef = ref<HTMLElement | null>(null)\n", "")
replace_once(article, """async function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm(), { columnId: selectedColumnId.value })
  dialogVisible.value = true
  await nextTick()
  if (editorRef.value) editorRef.value.innerHTML = ''
}""", """async function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm(), { columnId: selectedColumnId.value })
  dialogVisible.value = true
}""")
replace_once(article, """    await hydrateResourceNames([article.coverResourceId, ...article.bodyImageResourceIds, ...article.attachmentResourceIds])
    dialogVisible.value = true
    await nextTick()
    if (editorRef.value) editorRef.value.innerHTML = article.bodyHtml""", """    await hydrateResourceNames([article.coverResourceId, ...article.bodyImageResourceIds, ...article.attachmentResourceIds])
    dialogVisible.value = true""")
replace_once(article, "  if (form.articleType === 'INTERNAL') syncBody()", "  if (form.articleType === 'INTERNAL') form.bodyImageResourceIds = form.bodyImageResourceIds.filter(id => form.bodyHtml.includes(resourceContentUrl(id)))")
replace_once(article, """function syncBody() {
  const html = editorRef.value?.innerHTML ?? ''
  form.bodyHtml = html
  form.bodyImageResourceIds = form.bodyImageResourceIds.filter(id => html.includes(resourceContentUrl(id)))
}

function formatBody(command: 'bold' | 'italic') { editorRef.value?.focus(); document.execCommand(command); syncBody() }

async function uploadBodyImage(event: Event) {
  const file = firstFile(event)
  if (!file) return
  uploading.value = true
  try {
    const resource = await uploadResource(file)
    resourceNames[resource.id] = resource.originalFilename
    form.bodyImageResourceIds.push(resource.id)
    editorRef.value?.insertAdjacentHTML('beforeend', `<p><img src="${resourceContentUrl(resource.id)}" alt="${escapeHtml(resource.originalFilename)}" style="max-width:100%"></p>`)
    syncBody()
    ElMessage.success('正文图片已上传')
  } catch (error) { ElMessage.error(toMessage(error)) }
  finally { resetInput(event); uploading.value = false }
}""", """async function uploadBodyImage(file: File): Promise<{ src: string; alt: string }> {
  uploading.value = true
  try {
    const resource = await uploadResource(file)
    resourceNames[resource.id] = resource.originalFilename
    if (!form.bodyImageResourceIds.includes(resource.id)) form.bodyImageResourceIds.push(resource.id)
    ElMessage.success('正文图片已上传')
    return { src: resourceContentUrl(resource.id), alt: resource.originalFilename }
  } catch (error) {
    ElMessage.error(toMessage(error))
    throw error
  } finally { uploading.value = false }
}""")
replace_once(article, "function escapeHtml(value: string): string { const entities: Record<string, string> = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '\"': '&quot;' }; return value.replace(/[&<>\"]/g, char => entities[char] ?? char) }\n", "")
replace_once(article, """        <el-form-item v-if="form.articleType === 'INTERNAL'" label="正文">
          <div class="editor-shell">
            <div class="editor-toolbar"><el-button size="small" @click="formatBody('bold')"><strong>加粗</strong></el-button><el-button size="small" @click="formatBody('italic')"><em>斜体</em></el-button><label class="upload-button"><span>{{ uploading ? '上传中…' : '插入图片' }}</span><input data-testid="body-image-input" type="file" accept="image/*" :disabled="uploading" @change="uploadBodyImage"></label></div>
            <div ref="editorRef" data-testid="article-body-editor" class="rich-editor" contenteditable="true" @input="syncBody" />
          </div>
        </el-form-item>""", """        <el-form-item v-if="form.articleType === 'INTERNAL'" label="正文">
          <RichTextEditor v-model="form.bodyHtml" test-id="article-body-editor" :upload-image="uploadBodyImage" :uploading="uploading" />
        </el-form-item>""")

page = ADMIN / "src/modules/cms/views/admin/PageManagementView.vue"
replace_once(page, "import { computed, nextTick, onMounted, reactive, ref } from 'vue'", "import { computed, onMounted, reactive, ref } from 'vue'")
replace_once(page, "import AdminPanelToggle from '../../components/AdminPanelToggle.vue'", "import AdminPanelToggle from '../../components/AdminPanelToggle.vue'\nimport RichTextEditor from '../../components/RichTextEditor.vue'")
replace_once(page, "const editorRef = ref<HTMLElement | null>(null)\n", "")
replace_once(page, """  pageVisible.value = true
  await nextTick()
  if (editorRef.value) editorRef.value.innerHTML = pageForm.renderMode === 'RICH_TEXT' ? pageForm.bodyHtml : ''""", "  pageVisible.value = true")
replace_once(page, """function syncBody() {
  if (pageForm.renderMode === 'RICH_TEXT') pageForm.bodyHtml = editorRef.value?.innerHTML ?? ''
}

function formatBody(command: 'bold' | 'italic') {
  editorRef.value?.focus()
  document.execCommand(command)
  syncBody()
}

async function changeRenderMode() {
  if (pageForm.renderMode === 'RICH_TEXT') {
    pageForm.embedUrl = null
    await nextTick()
    if (editorRef.value) editorRef.value.innerHTML = pageForm.bodyHtml
  }
}""", """function changeRenderMode() {
  if (pageForm.renderMode === 'RICH_TEXT') pageForm.embedUrl = null
}""")
replace_once(page, "  syncBody()\n", "")
replace_once(page, """        <el-form-item v-if="pageForm.renderMode === 'RICH_TEXT'" label="正文"><div class="editor-shell"><div class="editor-toolbar"><el-button size="small" @click="formatBody('bold')"><strong>加粗</strong></el-button><el-button size="small" @click="formatBody('italic')"><em>斜体</em></el-button></div><div ref="editorRef" data-testid="page-body-editor" class="rich-editor" contenteditable="true" @input="syncBody" /></div></el-form-item>""", """        <el-form-item v-if="pageForm.renderMode === 'RICH_TEXT'" label="正文"><RichTextEditor v-model="pageForm.bodyHtml" test-id="page-body-editor" /></el-form-item>""")

test = r'''import { expect, test } from '@playwright/test'

const ONE_PIXEL_PNG = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Zt9sAAAAASUVORK5CYII=', 'base64')

async function firstColumnId(request: import('@playwright/test').APIRequestContext): Promise<number> {
  const response = await request.get('/api/admin/columns')
  expect(response.ok()).toBeTruthy()
  const columns = await response.json() as Array<{ id: number; alias: string }>
  return (columns.find(item => item.alias === 'notice') ?? columns[0]).id
}

test('EU-35：Article 与 Page 共用完整富文本工具栏且已有 HTML 可保存重开', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  const columnId = await firstColumnId(request)
  const articleTitle = `EU35富文本文章-${suffix}`
  const articleResponse = await request.post('/api/admin/articles', { data: {
    columnId, title: articleTitle,
    bodyHtml: '<p><span style="font-size:18px;color:#1f4e79">历史正文</span> <strong>强调</strong></p>',
    source: 'EU-35 E2E', articleType: 'INTERNAL', externalUrl: null, publishDate: '2026-09-05',
    pinned: false, sortOrder: 0, coverResourceId: null, bodyImageResourceIds: [], attachmentResourceIds: [],
  } })
  expect(articleResponse.ok()).toBeTruthy()
  const article = await articleResponse.json() as { id: number }

  await page.goto('/admin/articles')
  await page.getByTestId('article-filter-keyword').fill(articleTitle)
  const articleRow = page.getByTestId('article-table').getByRole('row').filter({ hasText: articleTitle })
  await articleRow.getByRole('button', { name: '编辑' }).click()
  const articleDialog = page.getByRole('dialog', { name: '编辑文章' })
  const articleEditor = articleDialog.getByTestId('article-body-editor')
  await expect(articleEditor).toContainText('历史正文')
  await expect(articleEditor.locator('strong')).toContainText('强调')
  for (const control of ['undo','redo','block','bold','italic','underline','strike','bullet-list','ordered-list','blockquote','hr','align-left','align-center','align-right','font-size','font-family','text-color','background-color','link','unlink','image','table','table-add-row','table-delete-row','table-add-column','table-delete-column','table-delete']) {
    await expect(articleDialog.getByTestId(`article-body-editor-${control}`)).toBeVisible()
  }

  await articleEditor.fill('标题内容')
  await articleEditor.selectText()
  await articleDialog.getByTestId('article-body-editor-block').click()
  await page.getByRole('option', { name: '标题 2' }).click()
  await expect(articleEditor.locator('h2')).toContainText('标题内容')
  await articleEditor.locator('h2').selectText()
  await articleDialog.getByTestId('article-body-editor-bold').click()
  await expect(articleEditor.locator('h2 strong')).toContainText('标题内容')
  await articleDialog.getByTestId('article-body-editor-undo').click()
  await expect(articleEditor.locator('h2 strong')).toHaveCount(0)
  await articleDialog.getByTestId('article-body-editor-redo').click()
  await expect(articleEditor.locator('h2 strong')).toContainText('标题内容')

  await articleDialog.getByTestId('body-image-input').setInputFiles({ name: 'eu35.png', mimeType: 'image/png', buffer: ONE_PIXEL_PNG })
  await expect(articleEditor.locator('img')).toHaveCount(1)
  await articleDialog.getByTestId('save-article').click()
  await expect(articleDialog).toBeHidden()
  const storedAfterImage = await (await request.get(`/api/admin/articles/${article.id}`)).json() as { bodyHtml: string; bodyImageResourceIds: number[] }
  expect(storedAfterImage.bodyHtml).toContain('<h2')
  expect(storedAfterImage.bodyImageResourceIds).toHaveLength(1)
  expect(storedAfterImage.bodyHtml).toContain(`/api/public/resources/${storedAfterImage.bodyImageResourceIds[0]}/content`)

  await page.getByTestId('article-filter-keyword').fill(articleTitle)
  await page.getByTestId('article-table').getByRole('row').filter({ hasText: articleTitle }).getByRole('button', { name: '编辑' }).click()
  const reopenedArticle = page.getByRole('dialog', { name: '编辑文章' })
  await expect(reopenedArticle.getByTestId('article-body-editor').locator('h2 strong')).toContainText('标题内容')
  const image = reopenedArticle.getByTestId('article-body-editor').locator('img')
  await image.click()
  await page.keyboard.press('Backspace')
  await expect(reopenedArticle.getByTestId('article-body-editor').locator('img')).toHaveCount(0)
  await reopenedArticle.getByTestId('save-article').click()
  const storedWithoutImage = await (await request.get(`/api/admin/articles/${article.id}`)).json() as { bodyImageResourceIds: number[] }
  expect(storedWithoutImage.bodyImageResourceIds).toEqual([])

  const pageName = `EU35富文本单页-${suffix}`
  const pageAlias = `eu35-rich-${suffix}`
  const createPage = await request.post('/api/admin/pages', { data: {
    groupId: null, alias: pageAlias, name: pageName,
    bodyHtml: '<p><span style="font-family:SimSun;font-size:18px;color:#800000">历史单页正文</span></p>',
    renderMode: 'RICH_TEXT', embedUrl: null, sortOrder: 999, enabled: true,
  } })
  expect(createPage.ok()).toBeTruthy()
  const savedPage = await createPage.json() as { id: number }

  await page.goto('/admin/pages')
  await page.getByTestId(`edit-page-${savedPage.id}`).click()
  const pageDialog = page.getByRole('dialog', { name: '编辑单页' })
  const pageEditor = pageDialog.getByTestId('page-body-editor')
  await expect(pageEditor).toContainText('历史单页正文')
  await expect(pageEditor.locator('span')).toHaveAttribute('style', /font-family/i)

  await pageEditor.fill('链接文本')
  await pageEditor.selectText()
  page.once('dialog', dialog => dialog.accept('https://example.com/eu35'))
  await pageDialog.getByTestId('page-body-editor-link').click()
  await expect(pageEditor.locator('a')).toHaveAttribute('href', 'https://example.com/eu35')
  await pageDialog.getByTestId('page-body-editor-table').click()
  await expect(pageEditor.locator('table')).toHaveCount(1)
  await pageDialog.getByTestId('page-body-editor-hr').click()
  await expect(pageEditor.locator('hr')).toHaveCount(1)
  await pageDialog.getByTestId('page-body-editor-align-center').click()
  await pageDialog.getByRole('button', { name: '保存' }).click()
  await expect(pageDialog).toBeHidden()

  await page.getByTestId(`edit-page-${savedPage.id}`).click()
  const reopenedPage = page.getByRole('dialog', { name: '编辑单页' })
  await expect(reopenedPage.getByTestId('page-body-editor').locator('a')).toHaveAttribute('href', 'https://example.com/eu35')
  await expect(reopenedPage.getByTestId('page-body-editor').locator('table')).toHaveCount(1)
  await reopenedPage.getByRole('button', { name: '取消' }).click()
})

test('EU-35：粘贴 schema 不保留未知节点且服务端安全边界继续生效', async ({ page, request }, testInfo) => {
  const suffix = `${Date.now()}-${testInfo.retry}`
  await page.goto('/admin/pages')
  await page.getByTestId('add-page').click()
  const dialog = page.getByRole('dialog', { name: '新增单页' })
  await dialog.getByRole('textbox', { name: '单页名称' }).fill(`EU35粘贴-${suffix}`)
  await dialog.getByRole('textbox', { name: '公开标识' }).fill(`eu35-paste-${suffix}`)
  const editor = dialog.getByTestId('page-body-editor')
  await editor.evaluate((node, html) => {
    const data = new DataTransfer()
    data.setData('text/html', String(html))
    node.dispatchEvent(new ClipboardEvent('paste', { clipboardData: data, bubbles: true, cancelable: true }))
  }, '<p class="unknown" data-extra="x" onclick="alert(1)">安全文字 <span style="color:#123456;position:fixed">保留颜色</span></p><script>alert(1)</script><iframe src="https://example.com"></iframe>')
  await expect(editor).toContainText('安全文字')
  await expect(editor.locator('script,iframe')).toHaveCount(0)
  await dialog.getByRole('button', { name: '保存' }).click()
  const pages = await (await request.get('/api/admin/pages')).json() as Array<{ alias: string; bodyHtml: string }>
  const saved = pages.find(item => item.alias === `eu35-paste-${suffix}`)
  expect(saved).toBeTruthy()
  expect(saved!.bodyHtml).not.toMatch(/script|iframe|onclick|position\s*:/i)
  expect(saved!.bodyHtml).toContain('安全文字')
})
'''
(ADMIN / "tests/e2e/rich-text-authoring.spec.ts").write_text(test, encoding="utf-8")

print("EU-35 source generation completed")
