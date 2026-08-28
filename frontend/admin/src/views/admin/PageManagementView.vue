<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
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
const pageForm = reactive<PageDraft>({ groupId: null, alias: '', name: '', bodyHtml: '', renderMode: 'RICH_TEXT', embedUrl: null, sortOrder: 0, enabled: true })
const groupForm = reactive<PageGroupDraft>({ alias: '', name: '', sortOrder: 0, enabled: true })

const groupName = (id: number | null) => id == null ? '普通固定页' : groups.value.find(g => g.id === id)?.name || `#${id}`
const asPage = (row: unknown) => row as CmsPage
const asGroup = (row: unknown) => row as CmsPageGroup

onMounted(refresh)
async function refresh() {
  ;[groups.value, pages.value] = await Promise.all([listPageGroups(), listPages()])
}
function openPage(row?: CmsPage) {
  editingPage.value = row?.id ?? null
  Object.assign(pageForm, row ? {
    groupId: row.groupId, alias: row.alias, name: row.name, bodyHtml: row.bodyHtml,
    renderMode: row.renderMode, embedUrl: row.embedUrl, sortOrder: row.sortOrder, enabled: row.enabled,
  } : { groupId: null, alias: '', name: '', bodyHtml: '', renderMode: 'RICH_TEXT', embedUrl: null, sortOrder: 0, enabled: true })
  pageVisible.value = true
}
function openGroup(row?: CmsPageGroup) {
  editingGroup.value = row?.id ?? null
  Object.assign(groupForm, row ? {
    alias: row.alias, name: row.name, sortOrder: row.sortOrder, enabled: row.enabled,
  } : { alias: '', name: '', sortOrder: 0, enabled: true })
  groupVisible.value = true
}
async function savePage() {
  saving.value = true
  try {
    editingPage.value == null ? await createPage({ ...pageForm }) : await updatePage(editingPage.value, { ...pageForm })
    pageVisible.value = false
    ElMessage.success('固定页面已保存')
    await refresh()
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '保存失败') } finally { saving.value = false }
}
async function saveGroup() {
  saving.value = true
  try {
    editingGroup.value == null ? await createPageGroup({ ...groupForm }) : await updatePageGroup(editingGroup.value, { ...groupForm })
    groupVisible.value = false
    ElMessage.success('页面组已保存')
    await refresh()
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '保存失败') } finally { saving.value = false }
}
async function remove(row: CmsPage) {
  try {
    await ElMessageBox.confirm(`确定删除固定页面“${row.name}”吗？`, '删除固定页面', { type: 'warning', confirmButtonText: '删除' })
    await deletePage(row.id)
    await refresh()
  } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error(e instanceof Error ? e.message : '删除失败') }
}
</script>

<template>
  <main class="admin-shell">
    <header class="page-header">
      <div><p class="eyebrow">网站内容</p><h1>固定页面管理</h1><p class="subtitle">维护页面组及其成员页面，公开 URL 由稳定 alias 生成。</p></div>
      <div><el-button data-testid="add-page-group" @click="openGroup()">新增页面组</el-button><el-button data-testid="add-page" type="primary" @click="openPage()">新增页面</el-button></div>
    </header>

    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><strong>页面组</strong></template>
      <el-table :data="groups" data-testid="page-group-table">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="alias" label="Alias" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column label="状态" width="100"><template #default="s">{{ s.row.enabled ? '启用' : '停用' }}</template></el-table-column>
        <el-table-column label="操作" width="100"><template #default="s"><el-button link type="primary" @click="openGroup(asGroup(s.row))">编辑</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never">
      <template #header><strong>固定页面</strong></template>
      <el-table :data="pages">
        <el-table-column prop="name" label="页面名称" />
        <el-table-column label="页面组"><template #default="s">{{ groupName(s.row.groupId) }}</template></el-table-column>
        <el-table-column prop="alias" label="Alias" />
        <el-table-column prop="renderMode" label="呈现方式" />
        <el-table-column label="操作"><template #default="s"><el-button link type="primary" @click="openPage(asPage(s.row))">编辑</el-button><el-button link type="danger" @click="remove(asPage(s.row))">删除</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="groupVisible" :title="editingGroup == null ? '新增页面组' : '编辑页面组'" width="560px">
      <el-form label-width="90px">
        <el-form-item label="名称"><el-input v-model="groupForm.name" /></el-form-item>
        <el-form-item label="Alias"><el-input v-model="groupForm.alias" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="groupForm.sortOrder" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="groupForm.enabled" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="groupVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveGroup">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="pageVisible" :title="editingPage == null ? '新增固定页面' : '编辑固定页面'" width="720px">
      <el-form label-width="100px">
        <el-form-item label="页面名称"><el-input v-model="pageForm.name" /></el-form-item>
        <el-form-item label="页面组"><el-select v-model="pageForm.groupId" clearable style="width:100%"><el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" /></el-select></el-form-item>
        <el-form-item label="Alias"><el-input v-model="pageForm.alias" /></el-form-item>
        <el-form-item label="呈现方式"><el-select v-model="pageForm.renderMode" style="width:100%"><el-option label="富文本" value="RICH_TEXT" /><el-option label="外部嵌入占位" value="EMBED_PLACEHOLDER" /><el-option label="站内特殊页面" value="INTERNAL_STATIC" /></el-select></el-form-item>
        <el-form-item label="正文"><el-input v-model="pageForm.bodyHtml" type="textarea" :rows="10" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="pageForm.sortOrder" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="pageForm.enabled" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="pageVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="savePage">保存</el-button></template>
    </el-dialog>
  </main>
</template>
