<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createPage,
  createPageGroup,
  deletePage,
  listPageGroups,
  listPages,
  updatePage,
  updatePageGroup,
  type CmsPage,
  type CmsPageGroup,
  type PageDraft,
  type PageGroupDraft,
} from '../../api/pages'

const groups = ref<CmsPageGroup[]>([])
const pages = ref<CmsPage[]>([])
const pageVisible = ref(false)
const groupVisible = ref(false)
const editingPage = ref<number | null>(null)
const editingGroup = ref<number | null>(null)
const saving = ref(false)
const loading = ref(false)
const editorRef = ref<HTMLElement | null>(null)
const pageForm = reactive<PageDraft>({ groupId: null, alias: '', name: '', bodyHtml: '', renderMode: 'RICH_TEXT', embedUrl: null, sortOrder: 0, enabled: true })
const groupForm = reactive<PageGroupDraft>({ alias: '', name: '', sortOrder: 0, enabled: true })

const groupName = (id: number | null) => id == null ? '普通固定页' : groups.value.find(g => g.id === id)?.name || `#${id}`
const asPage = (row: unknown) => row as CmsPage
const asGroup = (row: unknown) => row as CmsPageGroup

onMounted(refresh)

async function refresh() {
  loading.value = true
  try {
    ;[groups.value, pages.value] = await Promise.all([listPageGroups(), listPages()])
  } catch (error) {
    ElMessage.error(message(error))
  } finally {
    loading.value = false
  }
}

async function openPage(row?: CmsPage) {
  editingPage.value = row?.id ?? null
  Object.assign(pageForm, row ? {
    groupId: row.groupId,
    alias: row.alias,
    name: row.name,
    bodyHtml: row.bodyHtml,
    renderMode: row.renderMode,
    embedUrl: row.embedUrl,
    sortOrder: row.sortOrder,
    enabled: row.enabled,
  } : { groupId: null, alias: '', name: '', bodyHtml: '', renderMode: 'RICH_TEXT', embedUrl: null, sortOrder: 0, enabled: true })
  pageVisible.value = true
  await nextTick()
  if (editorRef.value) editorRef.value.innerHTML = pageForm.renderMode === 'RICH_TEXT' ? pageForm.bodyHtml : ''
}

function openGroup(row?: CmsPageGroup) {
  editingGroup.value = row?.id ?? null
  Object.assign(groupForm, row ? {
    alias: row.alias, name: row.name, sortOrder: row.sortOrder, enabled: row.enabled,
  } : { alias: '', name: '', sortOrder: 0, enabled: true })
  groupVisible.value = true
}

function syncBody() {
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
}

async function savePage() {
  syncBody()
  if (!pageForm.name.trim()) { ElMessage.warning('请输入页面名称'); return }
  if (!pageForm.alias.trim()) { ElMessage.warning('请输入 Alias'); return }
  if (pageForm.renderMode === 'INTERNAL_STATIC' && pageForm.embedUrl?.trim() && !pageForm.embedUrl.trim().startsWith('/')) {
    ElMessage.warning('站内特殊页面路径必须以 / 开头')
    return
  }
  saving.value = true
  try {
    const draft = { ...pageForm, embedUrl: pageForm.renderMode === 'RICH_TEXT' ? null : pageForm.embedUrl?.trim() || null }
    editingPage.value == null ? await createPage(draft) : await updatePage(editingPage.value, draft)
    pageVisible.value = false
    ElMessage.success('固定页面已保存')
    await refresh()
  } catch (error) {
    ElMessage.error(message(error))
  } finally {
    saving.value = false
  }
}

async function saveGroup() {
  if (!groupForm.name.trim() || !groupForm.alias.trim()) { ElMessage.warning('页面组名称和 Alias 不能为空'); return }
  saving.value = true
  try {
    editingGroup.value == null ? await createPageGroup({ ...groupForm }) : await updatePageGroup(editingGroup.value, { ...groupForm })
    groupVisible.value = false
    ElMessage.success('页面组已保存')
    await refresh()
  } catch (error) {
    ElMessage.error(message(error))
  } finally {
    saving.value = false
  }
}

async function remove(row: CmsPage) {
  try {
    await ElMessageBox.confirm(`确定删除固定页面“${row.name}”吗？该操作可能使现有公开地址不可用。`, '删除固定页面', { type: 'warning', confirmButtonText: '删除' })
    await deletePage(row.id)
    await refresh()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(message(error))
  }
}

function renderModeName(mode: CmsPage['renderMode']) {
  return mode === 'RICH_TEXT' ? '富文本' : mode === 'EMBED_PLACEHOLDER' ? '外部嵌入占位' : '站内特殊页面'
}

function message(error: unknown) {
  return error instanceof Error ? error.message : '操作失败'
}
</script>

<template>
  <main class="admin-shell">
    <header class="page-header">
      <div>
        <p class="eyebrow">网站内容</p>
        <h1>固定页面管理</h1>
        <p class="subtitle">维护页面组、固定页面和呈现模式；公开 URL 由稳定 Alias 生成。</p>
      </div>
      <div class="header-actions">
        <el-button data-testid="add-page-group" @click="openGroup()">新增页面组</el-button>
        <el-button data-testid="add-page" type="primary" @click="openPage()">新增页面</el-button>
      </div>
    </header>

    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><strong>页面组</strong></template>
      <el-table v-loading="loading" :data="groups" data-testid="page-group-table">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="alias" label="Alias" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column label="状态" width="100"><template #default="scope"><el-tag :type="scope.row.enabled ? 'success' : 'info'" size="small">{{ scope.row.enabled ? '启用' : '停用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="100"><template #default="scope"><el-button link type="primary" @click="openGroup(asGroup(scope.row))">编辑</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never">
      <template #header><strong>固定页面</strong></template>
      <el-table v-loading="loading" :data="pages" data-testid="page-table">
        <el-table-column prop="name" label="页面名称" min-width="170" />
        <el-table-column label="页面组" min-width="140"><template #default="scope">{{ groupName(scope.row.groupId) }}</template></el-table-column>
        <el-table-column prop="alias" label="Alias" min-width="140" />
        <el-table-column label="呈现方式" min-width="150"><template #default="scope">{{ renderModeName(scope.row.renderMode) }}</template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="scope">{{ scope.row.enabled ? '启用' : '停用' }}</template></el-table-column>
        <el-table-column label="操作" width="150" fixed="right"><template #default="scope"><el-button :data-testid="`edit-page-${scope.row.id}`" link type="primary" @click="openPage(asPage(scope.row))">编辑</el-button><el-button link type="danger" @click="remove(asPage(scope.row))">删除</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="groupVisible" :title="editingGroup == null ? '新增页面组' : '编辑页面组'" width="560px">
      <el-form label-width="90px">
        <el-form-item label="名称" required><el-input v-model="groupForm.name" /></el-form-item>
        <el-form-item label="Alias" required><el-input v-model="groupForm.alias" placeholder="仅小写字母、数字和连字符" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="groupForm.sortOrder" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="groupForm.enabled" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="groupVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveGroup">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="pageVisible" :title="editingPage == null ? '新增固定页面' : '编辑固定页面'" width="820px" destroy-on-close>
      <el-form label-width="110px">
        <el-form-item label="页面名称" required><el-input v-model="pageForm.name" /></el-form-item>
        <el-form-item label="页面组"><el-select v-model="pageForm.groupId" clearable style="width:100%"><el-option v-for="group in groups" :key="group.id" :label="group.name" :value="group.id" /></el-select></el-form-item>
        <el-form-item label="Alias" required><el-input v-model="pageForm.alias" placeholder="稳定公开地址标识" /></el-form-item>
        <el-form-item label="呈现方式" required>
          <el-select v-model="pageForm.renderMode" data-testid="page-render-mode" style="width:100%" @change="changeRenderMode">
            <el-option label="富文本" value="RICH_TEXT" />
            <el-option label="外部嵌入占位" value="EMBED_PLACEHOLDER" />
            <el-option label="站内特殊页面" value="INTERNAL_STATIC" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="pageForm.renderMode === 'RICH_TEXT'" label="正文">
          <div class="editor-shell">
            <div class="editor-toolbar"><el-button size="small" @click="formatBody('bold')"><strong>加粗</strong></el-button><el-button size="small" @click="formatBody('italic')"><em>斜体</em></el-button></div>
            <div ref="editorRef" data-testid="page-body-editor" class="rich-editor" contenteditable="true" @input="syncBody" />
          </div>
        </el-form-item>

        <template v-else>
          <el-form-item :label="pageForm.renderMode === 'INTERNAL_STATIC' ? '站内实现路径' : '嵌入地址'">
            <el-input v-model="pageForm.embedUrl" data-testid="page-embed-url" :placeholder="pageForm.renderMode === 'INTERNAL_STATIC' ? '/special/page-path' : 'https://外部内容地址（当前仍可只保留占位）'" />
          </el-form-item>
          <el-form-item label="占位说明"><el-input v-model="pageForm.bodyHtml" data-testid="page-placeholder-body" type="textarea" :rows="6" placeholder="当前页面对外展示的占位或说明内容" /></el-form-item>
          <el-alert v-if="pageForm.renderMode === 'INTERNAL_STATIC'" title="站内特殊页面是随前端工程部署的实现资产；这里只维护本站路径接缝，不允许上传任意 HTML/JS。" type="info" :closable="false" show-icon />
          <el-alert v-else title="当前 Requirement 仍不接入真实第三方 iframe；可维护后续接入地址接缝，但公开页面继续按占位规则展示。" type="info" :closable="false" show-icon />
        </template>

        <el-form-item label="排序"><el-input-number v-model="pageForm.sortOrder" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="pageForm.enabled" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="pageVisible = false">取消</el-button><el-button data-testid="save-page" type="primary" :loading="saving" @click="savePage">保存</el-button></template>
    </el-dialog>
  </main>
</template>
