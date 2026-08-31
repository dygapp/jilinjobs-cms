<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listColumns, type CmsColumn } from '../../api/columns'
import {
  createArticle,
  getArticle,
  getResource,
  listArticles,
  publishArticle,
  resourceContentUrl,
  updateArticle,
  uploadResource,
  withdrawArticle,
  type ArticleDraft,
  type ArticleStatus,
  type ArticleType,
  type CmsArticle,
} from '../../api/articles'

type ArticleForm = Omit<ArticleDraft, 'columnId'> & { columnId: number | null }
type StatusFilter = 'ALL' | ArticleStatus
type TypeFilter = 'ALL' | ArticleType
type ColumnTreeNode = CmsColumn & { children: ColumnTreeNode[] }
type ColumnSelectNode = { value: number; label: string; children: ColumnSelectNode[] }

const articles = ref<CmsArticle[]>([])
const columns = ref<CmsColumn[]>([])
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const statusChangingId = ref<number | null>(null)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const editorRef = ref<HTMLElement | null>(null)
const columnTreeRef = ref<{ setCurrentKey: (key: number | null) => void } | null>(null)
const resourceNames = reactive<Record<number, string>>({})
const form = reactive<ArticleForm>(emptyForm())

const keyword = ref('')
const selectedColumnId = ref<number | null>(null)
const filterStatus = ref<StatusFilter>('ALL')
const filterType = ref<TypeFilter>('ALL')
const currentPage = ref(1)
const pageSize = 10

const columnTree = computed<ColumnTreeNode[]>(() => {
  const nodes = new Map<number, ColumnTreeNode>()
  columns.value.forEach((item) => nodes.set(item.id, { ...item, children: [] }))
  const roots: ColumnTreeNode[] = []

  nodes.forEach((node) => {
    if (node.parentId != null && nodes.has(node.parentId)) {
      nodes.get(node.parentId)!.children.push(node)
    } else {
      roots.push(node)
    }
  })

  const sortNodes = (items: ColumnTreeNode[]) => {
    items.sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id)
    items.forEach((item) => sortNodes(item.children))
  }
  sortNodes(roots)
  return roots
})

const columnSelectTree = computed<ColumnSelectNode[]>(() => {
  const toSelectNodes = (items: ColumnTreeNode[]): ColumnSelectNode[] =>
    items.map((item) => ({
      value: item.id,
      label: item.name,
      children: toSelectNodes(item.children),
    }))
  return toSelectNodes(columnTree.value)
})

const selectedColumnIds = computed<Set<number> | null>(() => {
  if (selectedColumnId.value == null) return null
  const descendants = new Set<number>([selectedColumnId.value])
  const childrenByParent = new Map<number, number[]>()

  columns.value.forEach((item) => {
    if (item.parentId == null) return
    const children = childrenByParent.get(item.parentId) ?? []
    children.push(item.id)
    childrenByParent.set(item.parentId, children)
  })

  const queue = [selectedColumnId.value]
  while (queue.length > 0) {
    const parentId = queue.shift()!
    for (const childId of childrenByParent.get(parentId) ?? []) {
      if (descendants.has(childId)) continue
      descendants.add(childId)
      queue.push(childId)
    }
  }
  return descendants
})

const currentColumnName = computed(() =>
  selectedColumnId.value == null ? '全部文章' : columnName(selectedColumnId.value),
)

const filteredArticles = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  const scopedColumnIds = selectedColumnIds.value
  return articles.value.filter((article) => {
    if (text && !`${article.title} ${article.source}`.toLowerCase().includes(text)) return false
    if (scopedColumnIds != null && !scopedColumnIds.has(article.columnId)) return false
    if (filterStatus.value !== 'ALL' && article.status !== filterStatus.value) return false
    if (filterType.value !== 'ALL' && article.articleType !== filterType.value) return false
    return true
  })
})

const pagedArticles = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredArticles.value.slice(start, start + pageSize)
})

onMounted(refresh)

function emptyForm(): ArticleForm {
  return {
    columnId: null,
    title: '',
    bodyHtml: '',
    source: '',
    articleType: 'INTERNAL',
    externalUrl: null,
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
    if (selectedColumnId.value != null && !columns.value.some((item) => item.id === selectedColumnId.value)) {
      selectedColumnId.value = null
      columnTreeRef.value?.setCurrentKey(null)
    }
    normalizePage()
  } catch (error) {
    ElMessage.error(toMessage(error))
  } finally {
    loading.value = false
  }
}

function selectColumn(row: unknown) {
  selectedColumnId.value = (row as ColumnTreeNode).id
  currentPage.value = 1
}

function selectAllColumns() {
  selectedColumnId.value = null
  currentPage.value = 1
  columnTreeRef.value?.setCurrentKey(null)
}

function resetFilters() {
  keyword.value = ''
  filterStatus.value = 'ALL'
  filterType.value = 'ALL'
  selectAllColumns()
}

function filterChanged() {
  currentPage.value = 1
}

function normalizePage() {
  const max = Math.max(1, Math.ceil(filteredArticles.value.length / pageSize))
  if (currentPage.value > max) currentPage.value = max
}

async function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm(), { columnId: selectedColumnId.value })
  dialogVisible.value = true
  await nextTick()
  if (editorRef.value) editorRef.value.innerHTML = ''
}

async function openEdit(row: CmsArticle) {
  try {
    const article = await getArticle(row.id)
    editingId.value = article.id
    Object.assign(form, {
      columnId: article.columnId,
      title: article.title,
      bodyHtml: article.bodyHtml,
      source: article.source,
      articleType: article.articleType,
      externalUrl: article.externalUrl,
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
  } catch (error) {
    ElMessage.error(toMessage(error))
  }
}

async function save() {
  if (form.articleType === 'INTERNAL') syncBody()
  if (!form.title.trim()) { ElMessage.warning('请输入文章标题'); return }
  if (form.columnId == null) { ElMessage.warning('请选择所属栏目'); return }
  if (form.articleType === 'EXTERNAL_LINK' && !form.externalUrl?.trim()) { ElMessage.warning('请输入原文链接'); return }

  saving.value = true
  try {
    const draft: ArticleDraft = {
      columnId: form.columnId,
      title: form.title.trim(),
      bodyHtml: form.articleType === 'INTERNAL' ? form.bodyHtml : '',
      source: form.source.trim(),
      articleType: form.articleType,
      externalUrl: form.articleType === 'EXTERNAL_LINK' ? form.externalUrl?.trim() || null : null,
      publishDate: form.publishDate || null,
      pinned: form.pinned,
      recommended: form.recommended,
      sortOrder: form.sortOrder,
      coverResourceId: form.articleType === 'INTERNAL' ? form.coverResourceId : null,
      bodyImageResourceIds: form.articleType === 'INTERNAL' ? [...form.bodyImageResourceIds] : [],
      attachmentResourceIds: form.articleType === 'INTERNAL' ? [...form.attachmentResourceIds] : [],
    }
    if (editingId.value == null) {
      await createArticle(draft)
      ElMessage.success('文章草稿已创建')
    } else {
      await updateArticle(editingId.value, draft)
      ElMessage.success('文章内容已保存，发布状态保持不变')
    }
    dialogVisible.value = false
    await refresh()
  } catch (error) {
    ElMessage.error(toMessage(error))
  } finally {
    saving.value = false
  }
}

async function changeStatus(row: CmsArticle) {
  statusChangingId.value = row.id
  try {
    if (row.status === 'PUBLISHED') {
      await withdrawArticle(row.id)
      ElMessage.success('文章已撤回')
    } else {
      await publishArticle(row.id)
      ElMessage.success(row.status === 'WITHDRAWN' ? '文章已重新发布' : '文章已发布')
    }
    await refresh()
  } catch (error) {
    ElMessage.error(toMessage(error))
  } finally {
    statusChangingId.value = null
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
    editorRef.value?.insertAdjacentHTML('beforeend', `<p><img src="${resourceContentUrl(resource.id)}" alt="${escapeHtml(resource.originalFilename)}" style="max-width:100%"></p>`)
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

function statusActionName(status: CmsArticle['status']): string {
  return status === 'PUBLISHED' ? '撤回' : status === 'WITHDRAWN' ? '重新发布' : '发布'
}

function resourceName(id: number): string {
  return resourceNames[id] ?? `资源 #${id}`
}

function escapeHtml(value: string): string {
  const entities: Record<string, string> = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }
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
        <p class="eyebrow">网站内容</p>
        <h1>文章管理</h1>
        <p class="subtitle">按栏目结构维护站内文章和外链文章；父栏目同时查看全部下级栏目内容。</p>
      </div>
      <el-button data-testid="add-article" type="primary" @click="openCreate">新增文章</el-button>
    </header>

    <div class="article-management-layout">
      <aside class="article-column-panel">
        <el-card shadow="never">
          <div class="article-column-heading">
            <strong>栏目导航</strong>
            <span>父栏目包含全部下级栏目</span>
          </div>
          <button
            type="button"
            class="article-column-all"
            :class="{ active: selectedColumnId == null }"
            data-testid="article-column-all"
            @click="selectAllColumns"
          >
            <span>全部文章</span>
            <small>{{ articles.length }}</small>
          </button>
          <el-tree
            ref="columnTreeRef"
            class="article-column-tree"
            data-testid="article-column-tree"
            :data="columnTree"
            :props="{ children: 'children', label: 'name' }"
            node-key="id"
            default-expand-all
            highlight-current
            :expand-on-click-node="false"
            @node-click="selectColumn"
          >
            <template #default="{ data }">
              <span class="article-column-tree-node" :data-testid="`article-column-node-${data.id}`">
                <span class="article-column-tree-name">{{ data.name }}</span>
                <small v-if="!data.enabled" class="article-column-disabled">停用</small>
              </span>
            </template>
          </el-tree>
        </el-card>
      </aside>

      <section class="article-list-panel">
        <el-card shadow="never" class="admin-filter-card">
          <div class="admin-toolbar">
            <el-input
              v-model="keyword"
              data-testid="article-filter-keyword"
              class="grow"
              clearable
              placeholder="按标题或来源筛选"
              @input="filterChanged"
            />
            <el-select v-model="filterStatus" data-testid="article-filter-status" style="width:130px" @change="filterChanged">
              <el-option label="全部状态" value="ALL" />
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已发布" value="PUBLISHED" />
              <el-option label="已撤回" value="WITHDRAWN" />
            </el-select>
            <el-select v-model="filterType" data-testid="article-filter-type" style="width:130px" @change="filterChanged">
              <el-option label="全部类型" value="ALL" />
              <el-option label="站内文章" value="INTERNAL" />
              <el-option label="外链文章" value="EXTERNAL_LINK" />
            </el-select>
            <el-button @click="resetFilters">重置</el-button>
          </div>
        </el-card>

        <el-card shadow="never">
          <div class="article-list-context" data-testid="article-column-context">
            <div>
              <strong>{{ currentColumnName }}</strong>
              <span v-if="selectedColumnId != null">包含当前栏目及全部子栏目文章</span>
              <span v-else>显示所有栏目文章</span>
            </div>
            <small>共 {{ filteredArticles.length }} 篇</small>
          </div>
          <el-table v-loading="loading" :data="pagedArticles" row-key="id" data-testid="article-table">
            <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip />
            <el-table-column label="栏目" min-width="150">
              <template #default="scope">{{ columnName(scope.row.columnId) }}</template>
            </el-table-column>
            <el-table-column label="类型" width="95">
              <template #default="scope">
                <el-tag :type="scope.row.articleType === 'EXTERNAL_LINK' ? 'warning' : 'info'" size="small">
                  {{ scope.row.articleType === 'EXTERNAL_LINK' ? '外链' : '站内' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="95">
              <template #default="scope">{{ statusName(scope.row.status) }}</template>
            </el-table-column>
            <el-table-column prop="source" label="来源" min-width="130" show-overflow-tooltip />
            <el-table-column prop="publishDate" label="发布日期" width="120" />
            <el-table-column prop="viewCount" label="浏览量" width="90" />
            <el-table-column label="操作" width="210" fixed="right">
              <template #default="scope">
                <el-button :data-testid="`edit-article-${scope.row.id}`" link type="primary" @click="openEdit(asCmsArticle(scope.row))">编辑</el-button>
                <el-button
                  :data-testid="`${scope.row.status === 'PUBLISHED' ? 'withdraw' : 'publish'}-article-${scope.row.id}`"
                  link
                  :type="scope.row.status === 'PUBLISHED' ? 'danger' : 'success'"
                  :loading="statusChangingId === scope.row.id"
                  @click="changeStatus(asCmsArticle(scope.row))"
                >
                  {{ statusActionName(scope.row.status) }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="admin-pagination">
            <el-pagination
              data-testid="article-pagination"
              background
              layout="total, prev, pager, next"
              :page-size="pageSize"
              :total="filteredArticles.length"
              :current-page="currentPage"
              @current-change="value => currentPage = value"
            />
          </div>
        </el-card>
      </section>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增文章草稿' : '编辑文章'" width="860px" destroy-on-close>
      <el-form label-width="95px">
        <el-form-item label="文章标题" required>
          <el-input v-model="form.title" data-testid="article-title" placeholder="请输入文章标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="所属栏目" required>
          <el-tree-select
            v-model="form.columnId"
            data-testid="article-column-tree-select"
            :data="columnSelectTree"
            check-strictly
            default-expand-all
            placeholder="请选择所属栏目"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="内容类型" required>
          <el-radio-group v-model="form.articleType" data-testid="article-type">
            <el-radio-button value="INTERNAL">站内文章</el-radio-button>
            <el-radio-button value="EXTERNAL_LINK">外链文章</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容来源">
          <el-input v-model="form.source" data-testid="article-source" placeholder="请输入内容来源" maxlength="200" />
        </el-form-item>
        <el-form-item v-if="form.articleType === 'EXTERNAL_LINK'" label="原文链接" required>
          <el-input v-model="form.externalUrl" data-testid="article-external-url" placeholder="https://来源网站/..." maxlength="2000" />
        </el-form-item>
        <el-form-item label="发布日期">
          <el-date-picker v-model="form.publishDate" data-testid="article-publish-date" type="date" value-format="YYYY-MM-DD" placeholder="选择发布日期" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="form.articleType === 'INTERNAL'" label="正文">
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
        <el-form-item v-if="form.articleType === 'INTERNAL'" label="封面图片">
          <div class="resource-row">
            <label class="upload-button">
              <span>{{ form.coverResourceId == null ? '上传封面' : '更换封面' }}</span>
              <input data-testid="cover-input" type="file" accept="image/*" :disabled="uploading" @change="uploadCover">
            </label>
            <span v-if="form.coverResourceId != null" data-testid="cover-resource-name">{{ resourceName(form.coverResourceId) }}</span>
          </div>
        </el-form-item>
        <el-form-item v-if="form.articleType === 'INTERNAL'" label="附件">
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
        <el-alert
          v-if="form.articleType === 'EXTERNAL_LINK'"
          title="外链文章只保存标题、日期、来源和原文链接等基础信息，公开访问时直接跳转来源网站。"
          type="info"
          :closable="false"
          show-icon
        />
        <el-alert v-else title="新建文章固定保存为草稿；普通编辑不会改变当前发布状态。" type="info" :closable="false" show-icon />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button data-testid="save-article" type="primary" :loading="saving" :disabled="uploading" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>
