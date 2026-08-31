<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
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
const selectedGroup = ref<'all' | 'ungrouped' | number>('all')
const saving = ref(false)
const loading = ref(false)
const editorRef = ref<HTMLElement | null>(null)
const pageForm = reactive<PageDraft>({ groupId: null, alias: '', name: '', bodyHtml: '', renderMode: 'RICH_TEXT', embedUrl: null, sortOrder: 0, enabled: true })
const groupForm = reactive<PageGroupDraft>({ alias: '', name: '', sortOrder: 0, enabled: true })

const filteredPages = computed(() => {
  if (selectedGroup.value === 'all') return pages.value
  if (selectedGroup.value === 'ungrouped') return pages.value.filter(page => page.groupId == null)
  return pages.value.filter(page => page.groupId === selectedGroup.value)
})

const selectedGroupName = computed(() => {
  if (selectedGroup.value === 'all') return '全部单页'
  if (selectedGroup.value === 'ungrouped') return '独立单页'
  return groups.value.find(group => group.id === selectedGroup.value)?.name || '单页分组'
})
const editingPageModel = computed(() => editingPage.value == null ? null : pages.value.find(page => page.id === editingPage.value) || null)
const editingGroupModel = computed(() => editingGroup.value == null ? null : groups.value.find(group => group.id === editingGroup.value) || null)

const groupName = (id: number | null) => id == null ? '独立单页' : groups.value.find(g => g.id === id)?.name || `#${id}`
const groupCount = (groupId: number | null) => pages.value.filter(page => page.groupId === groupId).length
const asPage = (row: unknown) => row as CmsPage

onMounted(refresh)

async function refresh() {
  loading.value = true
  try {
    ;[groups.value, pages.value] = await Promise.all([listPageGroups(), listPages()])
    if (typeof selectedGroup.value === 'number' && !groups.value.some(group => group.id === selectedGroup.value)) selectedGroup.value = 'all'
  } catch (error) {
    ElMessage.error(message(error))
  } finally {
    loading.value = false
  }
}

function selectGroup(value: 'all' | 'ungrouped' | number) {
  selectedGroup.value = value
}

async function openPage(row?: CmsPage) {
  editingPage.value = row?.id ?? null
  const defaultGroupId = typeof selectedGroup.value === 'number' ? selectedGroup.value : null
  Object.assign(pageForm, row ? {
    groupId: row.groupId,
    alias: row.alias,
    name: row.name,
    bodyHtml: row.bodyHtml,
    renderMode: row.renderMode,
    embedUrl: row.embedUrl,
    sortOrder: row.sortOrder,
    enabled: row.enabled,
  } : { groupId: defaultGroupId, alias: '', name: '', bodyHtml: '', renderMode: 'RICH_TEXT', embedUrl: null, sortOrder: 0, enabled: true })
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
  if (!pageForm.name.trim()) { ElMessage.warning('请输入单页名称'); return }
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
    ElMessage.success('单页已保存')
    await refresh()
  } catch (error) {
    ElMessage.error(message(error))
  } finally {
    saving.value = false
  }
}

async function saveGroup() {
  if (!groupForm.name.trim() || !groupForm.alias.trim()) { ElMessage.warning('分组名称和 Alias 不能为空'); return }
  saving.value = true
  try {
    const saved = editingGroup.value == null ? await createPageGroup({ ...groupForm }) : await updatePageGroup(editingGroup.value, { ...groupForm })
    if (editingGroup.value == null) selectedGroup.value = saved.id
    groupVisible.value = false
    ElMessage.success('单页分组已保存')
    await refresh()
  } catch (error) {
    ElMessage.error(message(error))
  } finally {
    saving.value = false
  }
}

async function remove(row: CmsPage) {
  try {
    await ElMessageBox.confirm(`确定删除单页“${row.name}”吗？该操作可能使现有公开地址不可用。`, '删除单页', { type: 'warning', confirmButtonText: '删除' })
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
        <p class="eyebrow">内容管理</p>
        <h1>单页管理</h1>
        <p class="subtitle">维护独立单页、单页分组和呈现模式；预置单页保留稳定公开 Alias，且不可删除。</p>
      </div>
      <div class="header-actions">
        <el-button data-testid="add-page-group" @click="openGroup()">新增分组</el-button>
        <el-button data-testid="add-page" type="primary" @click="openPage()">新增单页</el-button>
      </div>
    </header>

    <div class="page-management-layout">
      <el-card class="page-group-panel" shadow="never">
        <div class="page-group-heading">
          <strong>单页组织</strong>
          <span>选择分组后维护其中的单页</span>
        </div>
        <button class="page-group-item" :class="{ active: selectedGroup === 'all' }" data-testid="page-group-all" type="button" @click="selectGroup('all')"><span>全部单页</span><small>{{ pages.length }}</small></button>
        <button class="page-group-item" :class="{ active: selectedGroup === 'ungrouped' }" data-testid="page-group-ungrouped" type="button" @click="selectGroup('ungrouped')"><span>独立单页</span><small>{{ groupCount(null) }}</small></button>
        <div class="page-group-divider">单页分组</div>
        <div v-for="group in groups" :key="group.id" class="page-group-row" :class="{ active: selectedGroup === group.id }" :data-testid="`page-group-${group.id}`" role="button" tabindex="0" @click="selectGroup(group.id)" @keydown.enter="selectGroup(group.id)">
          <span class="page-group-name">{{ group.name }}<el-tag v-if="group.preset" :data-testid="`preset-page-group-${group.id}`" size="small" type="info" style="margin-left:6px">预置</el-tag></span>
          <span v-if="!group.enabled" class="page-group-disabled">停用</span>
          <small>{{ groupCount(group.id) }}</small>
          <el-button link type="primary" size="small" aria-label="编辑单页分组" @click.stop="openGroup(group)">编辑</el-button>
        </div>
      </el-card>

      <el-card class="page-list-panel" shadow="never">
        <div class="page-list-context" data-testid="page-group-context">
          <div><strong>{{ selectedGroupName }}</strong><span>{{ selectedGroup === 'all' ? '查看全部单页' : selectedGroup === 'ungrouped' ? '不属于任何分组的独立单页' : '当前分组成员' }}</span></div><small>共 {{ filteredPages.length }} 项</small>
        </div>
        <el-table v-loading="loading" :data="filteredPages" data-testid="page-table">
          <el-table-column label="单页名称" min-width="170"><template #default="scope"><span>{{asPage(scope.row).name}}</span><el-tag v-if="asPage(scope.row).preset" :data-testid="`preset-page-${asPage(scope.row).id}`" size="small" type="info" style="margin-left:8px">预置</el-tag></template></el-table-column>
          <el-table-column label="单页分组" min-width="140"><template #default="scope">{{ groupName(scope.row.groupId) }}</template></el-table-column>
          <el-table-column prop="alias" label="Alias" min-width="140" />
          <el-table-column label="呈现方式" min-width="150"><template #default="scope">{{ renderModeName(scope.row.renderMode) }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template #default="scope">{{ scope.row.enabled ? '启用' : '停用' }}</template></el-table-column>
          <el-table-column label="操作" width="150" fixed="right"><template #default="scope"><el-button :data-testid="`edit-page-${scope.row.id}`" link type="primary" @click="openPage(asPage(scope.row))">编辑</el-button><el-button v-if="!asPage(scope.row).preset" link type="danger" @click="remove(asPage(scope.row))">删除</el-button></template></el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="groupVisible" :title="editingGroup == null ? '新增单页分组' : '编辑单页分组'" width="560px">
      <el-form label-width="90px">
        <el-form-item label="名称" required><el-input v-model="groupForm.name" /></el-form-item>
        <el-form-item label="Alias" required><el-input v-model="groupForm.alias" :disabled="Boolean(editingGroupModel?.preset)" placeholder="仅小写字母、数字和连字符" /><div v-if="editingGroupModel?.preset" data-testid="preset-page-group-alias-hint" style="color:#909399;font-size:12px">预置单页分组的 Alias 是稳定站点身份，不允许修改。</div></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="groupForm.sortOrder" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="groupForm.enabled" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="groupVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveGroup">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="pageVisible" :title="editingPage == null ? '新增单页' : '编辑单页'" width="820px" destroy-on-close>
      <el-form label-width="110px">
        <el-form-item label="单页名称" required><el-input v-model="pageForm.name" /></el-form-item>
        <el-form-item label="单页分组"><el-select v-model="pageForm.groupId" clearable data-testid="page-group-select" style="width:100%" placeholder="独立单页"><el-option v-for="group in groups" :key="group.id" :label="group.name" :value="group.id" /></el-select></el-form-item>
        <el-form-item label="Alias" required><el-input v-model="pageForm.alias" :disabled="Boolean(editingPageModel?.preset)" placeholder="稳定公开地址标识" /><div v-if="editingPageModel?.preset" data-testid="preset-page-alias-hint" style="color:#909399;font-size:12px">预置单页的 Alias 是稳定站点身份，不允许修改。</div></el-form-item>
        <el-form-item label="呈现方式" required><el-select v-model="pageForm.renderMode" data-testid="page-render-mode" style="width:100%" @change="changeRenderMode"><el-option label="富文本" value="RICH_TEXT" /><el-option label="外部嵌入占位" value="EMBED_PLACEHOLDER" /><el-option label="站内特殊页面" value="INTERNAL_STATIC" /></el-select></el-form-item>

        <el-form-item v-if="pageForm.renderMode === 'RICH_TEXT'" label="正文"><div class="editor-shell"><div class="editor-toolbar"><el-button size="small" @click="formatBody('bold')"><strong>加粗</strong></el-button><el-button size="small" @click="formatBody('italic')"><em>斜体</em></el-button></div><div ref="editorRef" data-testid="page-body-editor" class="rich-editor" contenteditable="true" @input="syncBody" /></div></el-form-item>

        <template v-else>
          <el-form-item :label="pageForm.renderMode === 'INTERNAL_STATIC' ? '站内实现路径' : '嵌入地址'"><el-input v-model="pageForm.embedUrl" data-testid="page-embed-url" :placeholder="pageForm.renderMode === 'INTERNAL_STATIC' ? '/special/page-path' : 'https://外部内容地址（当前仍可只保留占位）'" /></el-form-item>
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
