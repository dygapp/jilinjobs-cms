<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteStaticResource,
  listStaticResources,
  listStaticTrash,
  restoreStaticResource,
  uploadStaticResource,
  type StaticEntry,
  type TrashEntry,
} from '../../api/staticResources'

const entries = ref<StaticEntry[]>([])
const trash = ref<TrashEntry[]>([])
const currentPath = ref('')
const file = ref<File | null>(null)
const replaceTarget = ref<StaticEntry | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
const loading = ref(false)
const asEntry = (row: unknown) => row as StaticEntry
const asTrash = (row: unknown) => row as TrashEntry
const displayPath = computed(() => currentPath.value || '/')
const canGoUp = computed(() => currentPath.value.length > 0)

onMounted(refresh)

async function refresh() {
  loading.value = true
  try {
    ;[entries.value, trash.value] = await Promise.all([listStaticResources(currentPath.value), listStaticTrash()])
  } catch (error) {
    ElMessage.error(message(error))
  } finally {
    loading.value = false
  }
}
function choose(e: Event) {
  file.value = (e.target as HTMLInputElement).files?.[0] ?? null
}
function clearSelection() {
  file.value = null
  replaceTarget.value = null
  if (fileInput.value) fileInput.value.value = ''
}
async function enter(row: StaticEntry) {
  if (!row.directory) return
  currentPath.value = row.path
  clearSelection()
  await refresh()
}
async function goUp() {
  if (!currentPath.value) return
  const segments = currentPath.value.split('/').filter(Boolean)
  segments.pop()
  currentPath.value = segments.join('/')
  clearSelection()
  await refresh()
}
function publicUrl(path: string) {
  return `/static/${path.split('/').map(encodeURIComponent).join('/')}`
}
async function upload(replace = false) {
  if (!file.value) { ElMessage.warning('请选择文件'); return }
  const path = replace && replaceTarget.value
    ? replaceTarget.value.path
    : [currentPath.value, file.value.name].filter(Boolean).join('/')
  try {
    await uploadStaticResource(path, file.value, replace)
    clearSelection()
    ElMessage.success(replace ? '静态资源已替换' : '静态资源已上传')
    await refresh()
  } catch (error) { ElMessage.error(message(error)) }
}
function prepareReplace(row: StaticEntry) {
  if (row.directory) return
  replaceTarget.value = row
  file.value = null
  if (fileInput.value) fileInput.value.value = ''
  ElMessage.info(row.protectedResource ? `已选择受保护资源：${row.path}` : `请选择新文件以替换：${row.path}`)
}
async function confirmReplace() {
  if (!replaceTarget.value) { ElMessage.warning('请先选择要替换的资源'); return }
  if (!file.value) { ElMessage.warning('请选择新文件'); return }
  const warning = replaceTarget.value.protectedResource
    ? '这是站点关键受保护资源。替换会立即影响当前公开站，且系统不会完整检查 CSS、富文本等全部引用。确认继续？'
    : '替换会立即覆盖当前公开 URL 对应文件，系统不会完整检查引用。是否继续？'
  try {
    await ElMessageBox.confirm(warning, '替换静态资源', { type: 'warning', confirmButtonText: '确认替换' })
    await upload(true)
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(message(error)) }
}
async function remove(row: StaticEntry) {
  if (row.directory) return
  if (row.protectedResource) {
    ElMessage.warning('该资源由站点配置或 Runtime 基线保护，不能普通删除；如需更新请使用替换操作')
    return
  }
  try {
    await ElMessageBox.confirm('系统不会完整检查该资源是否正在被页面、CSS、JS 或静态页面引用。删除后将先进入回收区，是否继续？', '静态资源删除风险', { type: 'warning', confirmButtonText: '移入回收区' })
    await deleteStaticResource(row.path)
    await refresh()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(message(error)) }
}
async function restore(row: TrashEntry) {
  try {
    await restoreStaticResource(row.id)
    await refresh()
    ElMessage.success('资源已恢复')
  } catch (error) { ElMessage.error(message(error)) }
}
function message(error: unknown) {
  return error instanceof Error ? error.message : '操作失败'
}
</script>

<template>
  <main class="admin-shell">
    <header class="page-header">
      <div>
        <p class="eyebrow">高风险站点能力</p>
        <h1>网站静态资源管理</h1>
        <p class="subtitle">浏览和管理指定网站静态目录；上传同时校验扩展名与真实文件内容。</p>
      </div>
    </header>

    <el-alert title="系统会保护 Runtime 基线和网站配置直接引用的关键资源；其他资源仍不提供完整 CSS / JS / 富文本引用扫描。" type="warning" :closable="false" show-icon />

    <el-card shadow="never" style="margin-top:16px">
      <div class="admin-toolbar">
        <div class="path-bar grow">
          <el-button :disabled="!canGoUp" @click="goUp">返回上级</el-button>
          <code data-testid="static-current-path">{{ displayPath }}</code>
        </div>
        <div class="resource-upload">
          <input ref="fileInput" data-testid="static-file-input" type="file" @change="choose">
          <el-button type="primary" @click="upload(false)">上传到当前目录</el-button>
          <el-button v-if="replaceTarget" data-testid="confirm-static-replace" type="warning" @click="confirmReplace">替换所选资源</el-button>
          <el-button v-if="replaceTarget" @click="clearSelection">取消替换</el-button>
        </div>
      </div>
      <el-alert v-if="replaceTarget" :title="`待替换：${replaceTarget.path}${replaceTarget.protectedResource ? '（关键受保护资源）' : ''}`" :type="replaceTarget.protectedResource ? 'warning' : 'info'" :closable="false" style="margin-bottom:12px" />
      <el-table v-loading="loading" :data="entries" data-testid="static-resource-table">
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="path" label="路径" min-width="260" />
        <el-table-column label="类型" width="100"><template #default="scope">{{ scope.row.directory ? '目录' : '文件' }}</template></el-table-column>
        <el-table-column label="保护" width="100"><template #default="scope"><el-tag v-if="scope.row.protectedResource" type="warning" size="small">关键资源</el-tag><span v-else>-</span></template></el-table-column>
        <el-table-column prop="size" label="大小" width="110" />
        <el-table-column label="操作" width="280" fixed="right"><template #default="scope">
          <el-button v-if="scope.row.directory" link type="primary" @click="enter(asEntry(scope.row))">进入</el-button>
          <template v-else>
            <el-link :href="publicUrl(scope.row.path)" target="_blank" type="primary">查看/下载</el-link>
            <el-button link type="warning" @click="prepareReplace(asEntry(scope.row))">替换</el-button>
            <el-button :data-testid="`delete-static-${scope.row.path}`" link type="danger" :disabled="scope.row.protectedResource" @click="remove(asEntry(scope.row))">删除</el-button>
          </template>
        </template></el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" style="margin-top:16px">
      <template #header>回收区</template>
      <el-table :data="trash">
        <el-table-column prop="originalPath" label="原路径" />
        <el-table-column label="操作" width="100"><template #default="scope"><el-button link type="primary" @click="restore(asTrash(scope.row))">恢复</el-button></template></el-table-column>
      </el-table>
    </el-card>
  </main>
</template>
