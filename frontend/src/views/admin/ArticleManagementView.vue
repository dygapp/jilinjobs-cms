<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listColumns, type CmsColumn } from '../../api/columns'
import {
  createArticle,
  getArticle,
  getResource,
  listArticles,
  resourceContentUrl,
  updateArticle,
  uploadResource,
  type ArticleDraft,
  type CmsArticle,
} from '../../api/articles'

type ArticleForm = Omit<ArticleDraft, 'columnId'> & { columnId: number | null }

const articles = ref<CmsArticle[]>([])
const columns = ref<CmsColumn[]>([])
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const editorRef = ref<HTMLElement | null>(null)
const resourceNames = reactive<Record<number, string>>({})
const form = reactive<ArticleForm>(emptyForm())

onMounted(refresh)

function emptyForm(): ArticleForm {
  return {
    columnId: null,
    title: '',
    bodyHtml: '',
    source: '',
    publishDate: null,
    pinned: false,
    recommended: false,
    sortOrder: 0,
    coverResourceId: null,
    bodyImageResourceIds: [],
    attachmentResourceIds: [],
  }
}

async function refresh() {
  loading.value = true
  try {
    const [articleRows, columnRows] = await Promise.all([listArticles(), listColumns()])
    articles.value = articleRows
    columns.value = columnRows
  } catch (error) {
    ElMessage.error(toMessage(error))
  } finally {
    loading.value = false
  }
}

async function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  dialogVisible.value = true
  await nextTick()
  if (editorRef.value) editorRef.value.innerHTML = ''
}

async function openEdit(row: CmsArticle) {
  const article = await getArticle(row.id)
  editingId.value = article.id
  Object.assign(form, {
    columnId: article.columnId,
    title: article.title,
    bodyHtml: article.bodyHtml,
    source: article.source,
    publishDate: article.publishDate,
    pinned: article.pinned,
    recommended: article.recommended,
    sortOrder: article.sortOrder,
    coverResourceId: article.coverResourceId,
    bodyImageResourceIds: [...article.bodyImageResourceIds],
    attachmentResourceIds: [...article.attachmentResourceIds],
  })
  await hydrateResourceNames([
    article.coverResourceId,
    ...article.bodyImageResourceIds,
    ...article.attachmentResourceIds,
  ])
  dialogVisible.value = true
  await nextTick()
  if (editorRef.value) editorRef.value.innerHTML = article.bodyHtml
}

async function save() {
  syncBody()
  if (!form.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (form.columnId == null) {
    ElMessage.warning('请选择所属栏目')
    return
  }

  saving.value = true
  try {
    const draft: ArticleDraft = {
      columnId: form.columnId,
      title: form.title,
      bodyHtml: form.bodyHtml,
      source: form.source,
      publishDate: form.publishDate || null,
      pinned: form.pinned,
      recommended: form.recommended,
      sortOrder: form.sortOrder,
      coverResourceId: form.coverResourceId,
      bodyImageResourceIds: [...form.bodyImageResourceIds],
      attachmentResourceIds: [...form.attachmentResourceIds],
    }
    if (editingId.value == null) {
      await createArticle(draft)
      ElMessage.success('文章草稿已创建')
    } else {
      await updateArticle(editingId.value, draft)
      ElMessage.success('文章草稿已保存')
    }
    dialogVisible.value = false
    await refresh()
  } catch (error) {
    ElMessage.error(toMessage(error))
  } finally {
    saving.value = false
  }
}

function syncBody() {
  const html = editorRef.value?.innerHTML ?? ''
  form.bodyHtml = html
  form.bodyImageResourceIds = form.bodyImageResourceIds.filter((id) => html.includes(resourceContentUrl(id)))
}

function formatBody(command: 'bold' | 'italic') {
  editorRef.value?.focus()
  document.execCommand(command)
  syncBody()
}

async function uploadBodyImage(event: Event) {
  const file = firstFile(event)
  if (!file) return
  uploading.value = true
  try {
    const resource = await uploadResource(file)
    resourceNames[resource.id] = resource.originalFilename
    form.bodyImageResourceIds.push(resource.id)
    editorRef.value?.insertAdjacentHTML(
      'beforeend',
      `<p><img src="${resourceContentUrl(resource.id)}" alt="${escapeHtml(resource.originalFilename)}" style="max-width:100%"></p>`,
    )
    syncBody()
    ElMessage.success('正文图片已上传')
  } catch (error) {
    ElMessage.error(toMessage(error))
  } finally {
    resetInput(event)
    uploading.value = false
  }
}

async function uploadCover(event: Event) {
  const file = firstFile(event)
  if (!file) return
  uploading.value = true
  try {
    const resource = await uploadResource(file)
    resourceNames[resource.id] = resource.originalFilename
    form.coverResourceId = resource.id
    ElMessage.success('封面已上传')
  } catch (error) {
    ElMessage.error(toMessage(error))
  } finally {
    resetInput(event)
    uploading.value = false
  }
}

async function uploadAttachments(event: Event) {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  if (files.length === 0) return
  uploading.value = true
  try {
    for (const file of files) {
      const resource = await uploadResource(file)
      resourceNames[resource.id] = resource.originalFilename
      form.attachmentResourceIds.push(resource.id)
    }
    ElMessage.success('附件已上传')
  } catch (error) {
    ElMessage.error(toMessage(error))
  } finally {
    input.value = ''
    uploading.value = false
  }
}

function removeAttachment(id: number) {
  form.attachmentResourceIds = form.attachmentResourceIds.filter((item) => item !== id)
}

async function hydrateResourceNames(ids: Array<number | null>) {
  const uniqueIds = [...new Set(ids.filter((id): id is number => id != null))]
  await Promise.all(uniqueIds.map(async (id) => {
    if (resourceNames[id]) return
    try {
      const resource = await getResource(id)
      resourceNames[id] = resource.originalFilename
    } catch {
      resourceNames[id] = `资源 #${id}`
    }
  }))
}

function firstFile(event: Event): File | null {
  const input = event.target as HTMLInputElement
  return input.files?.[0] ?? null
}

function resetInput(event: Event) {
  ;(event.target as HTMLInputElement).value = ''
}

function asCmsArticle(row: unknown): CmsArticle {
  return row as CmsArticle
}

function columnName(columnId: number): string {
  return columns.value.find((item) => item.id === columnId)?.name ?? `栏目 #${columnId}`
}

function statusName(status: CmsArticle['status']): string {
  return status === 'DRAFT' ? '草稿' : status === 'PUBLISHED' ? '已发布' : '已撤回'
}

function resourceName(id: number): string {
  return resourceNames[id] ?? `资源 #${id}`
}

function escapeHtml(value: string): string {
  const entities: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
  }
  return value.replace(/[&<>"]/g, (char) => entities[char] ?? char)
}

function toMessage(error: unknown): string {
  return error instanceof Error ? error.message : '操作失败'
}
</script>

<template>
  <main class="admin-shell">
    <header class="page-header">
      <div>
        <p class="eyebrow">jilinjobs-cms prototype</p>
        <h1>文章草稿管理</h1>
        <p class="subtitle">维护文章草稿、正文图片、封面和附件；保存不会自动发布。</p>
      </div>
      <el-button data-testid="add-article" type="primary" @click="openCreate">新增文章</el-button>
    </header>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="articles" row-key="id">
        <el-table-column prop="title" label="标题" min-width="260" />
        <el-table-column label="栏目" min-width="160">
          <template #default="scope">{{ columnName(scope.row.columnId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">{{ statusName(scope.row.status) }}</template>
        </el-table-column>
        <el-table-column prop="source" label="来源" min-width="140" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="scope">
            <el-button :data-testid="`edit-article-${scope.row.id}`" link type="primary" @click="openEdit(asCmsArticle(scope.row))">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增文章草稿' : '编辑文章草稿'" width="860px" destroy-on-close>
      <el-form label-width="95px">
        <el-form-item label="文章标题" required>
          <el-input v-model="form.title" data-testid="article-title" placeholder="请输入文章标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="所属栏目" required>
          <el-select v-model="form.columnId" data-testid="article-column" placeholder="请选择所属栏目" style="width: 100%">
            <el-option v-for="item in columns" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容来源">
          <el-input v-model="form.source" data-testid="article-source" placeholder="请输入内容来源" maxlength="200" />
        </el-form-item>
        <el-form-item label="发布日期">
          <el-date-picker v-model="form.publishDate" data-testid="article-publish-date" type="date" value-format="YYYY-MM-DD" placeholder="选择发布日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="正文">
          <div class="editor-shell">
            <div class="editor-toolbar">
              <el-button size="small" @click="formatBody('bold')"><strong>加粗</strong></el-button>
              <el-button size="small" @click="formatBody('italic')"><em>斜体</em></el-button>
              <label class="upload-button">
                <span>{{ uploading ? '上传中…' : '插入图片' }}</span>
                <input data-testid="body-image-input" type="file" accept="image/*" :disabled="uploading" @change="uploadBodyImage">
              </label>
            </div>
            <div ref="editorRef" data-testid="article-body-editor" class="rich-editor" contenteditable="true" @input="syncBody" />
          </div>
        </el-form-item>
        <el-form-item label="封面图片">
          <div class="resource-row">
            <label class="upload-button">
              <span>{{ form.coverResourceId == null ? '上传封面' : '更换封面' }}</span>
              <input data-testid="cover-input" type="file" accept="image/*" :disabled="uploading" @change="uploadCover">
            </label>
            <span v-if="form.coverResourceId != null" data-testid="cover-resource-name">{{ resourceName(form.coverResourceId) }}</span>
          </div>
        </el-form-item>
        <el-form-item label="附件">
          <div class="attachments">
            <label class="upload-button">
              <span>上传附件</span>
              <input data-testid="attachment-input" type="file" multiple :disabled="uploading" @change="uploadAttachments">
            </label>
            <div v-for="id in form.attachmentResourceIds" :key="id" class="attachment-chip" :data-testid="`attachment-${id}`">
              <span>{{ resourceName(id) }}</span>
              <el-button link type="danger" @click="removeAttachment(id)">移除</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="运营属性">
          <el-checkbox v-model="form.pinned" data-testid="article-pinned">置顶</el-checkbox>
          <el-checkbox v-model="form.recommended" data-testid="article-recommended">推荐</el-checkbox>
          <span class="sort-label">展示顺序</span>
          <el-input-number v-model="form.sortOrder" data-testid="article-sort-order" :step="1" />
        </el-form-item>
        <el-alert title="新建文章固定保存为草稿；普通编辑不会改变发布状态。" type="info" :closable="false" show-icon />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button data-testid="save-article" type="primary" :loading="saving" :disabled="uploading" @click="save">保存草稿</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped>
.editor-shell {
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  overflow: hidden;
}
.editor-toolbar {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 8px;
  background: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color);
}
.rich-editor {
  min-height: 220px;
  padding: 12px;
  outline: none;
  line-height: 1.7;
}
.rich-editor :deep(img) {
  max-width: 100%;
}
.upload-button {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  cursor: pointer;
  background: white;
}
.upload-button input {
  display: none;
}
.resource-row,
.attachments {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}
.attachment-chip {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  padding: 3px 8px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
}
.sort-label {
  margin-left: 24px;
  margin-right: 8px;
  color: var(--el-text-color-regular);
}
</style>
