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
const replaceTarget = ref<string | null>(null)
const asEntry = (row: unknown) => row as StaticEntry
const asTrash = (row: unknown) => row as TrashEntry
const displayPath = computed(() => currentPath.value || '/')
const canGoUp = computed(() => currentPath.value.length > 0)

onMounted(refresh)
async function refresh() {
  ;[entries.value, trash.value] = await Promise.all([listStaticResources(currentPath.value), listStaticTrash()])
}
function choose(e: Event) {
  file.value = (e.target as HTMLInputElement).files?.[0] ?? null
  replaceTarget.value = null
}
async function enter(row: StaticEntry) {
  if (!row.directory) return
  currentPath.value = row.path
  await refresh()
}
async function goUp() {
  if (!currentPath.value) return
  const segments = currentPath.value.split('/').filter(Boolean)
  segments.pop()
  currentPath.value = segments.join('/')
  await refresh()
}
function publicUrl(path: string) {
  return `/static/${path.split('/').map(encodeURIComponent).join('/')}`
}
async function upload(replace = false) {
  if (!file.value) { ElMessage.warning('请选择文件'); return }
  const path = replace && replaceTarget.value
    ? replaceTarget.value
    : [currentPath.value, file.value.name].filter(Boolean).join('/')
  try {
    await uploadStaticResource(path, file.value, replace)
    file.value = null
    replaceTarget.value = null
    ElMessage.success(replace ? '静态资源已替换' : '静态资源已上传')
    await refresh()
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '上传失败') }
}
async function prepareReplace(row: StaticEntry) {
  if (row.directory) return
  replaceTarget.value = row.path
  file.value = null
  ElMessage.info(`请选择新文件以替换：${row.path}`)
}
async function confirmReplace() {
  if (!replaceTarget.value) { ElMessage.warning('请先选择要替换的资源'); return }
  if (!file.value) { ElMessage.warning('请选择新文件'); return }
  try {
    await ElMessageBox.confirm('替换会立即覆盖当前公开 URL 对应文件，系统不会完整检查引用。是否继续？', '替换静态资源', { type: 'warning', confirmButtonText: '确认替换' })
    await upload(true)
  } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error(e instanceof Error ? e.message : '替换失败') }
}
async function remove(row: StaticEntry) {
  if (row.directory) return
  try {
    await ElMessageBox.confirm('系统不会完整检查该资源是否正在被页面、CSS、JS 或静态页面引用。删除后将先进入回收区，是否继续？', '静态资源删除风险', { type: 'warning', confirmButtonText: '移入回收区' })
    await deleteStaticResource(row.path)
    await refresh()
  } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error(e instanceof Error ? e.message : '删除失败') }
}
async function restore(row: TrashEntry) {
  try {
    await restoreStaticResource(row.id)
    await refresh()
    ElMessage.success('资源已恢复')
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '恢复失败') }
}
</script>

<template>
  <main class="admin-shell">
    <header class="page-header"><div><p class="eyebrow">高风险站点能力</p><h1>网站静态资源管理</h1><p class="subtitle">浏览和管理指定网站静态目录。系统不提供完整资源引用关系检查。</p></div></header>
    <el-alert title="删除或替换静态资源可能破坏正在使用该 URL 的页面；当前系统不会完整扫描所有引用。" type="warning" :closable="false" show-icon />

    <el-card shadow="never" style="margin-top:16px">
      <div class="toolbar">
        <div class="path-bar"><el-button :disabled="!canGoUp" @click="goUp">返回上级</el-button><code data-testid="static-current-path">{{ displayPath }}</code></div>
        <div class="resource-upload"><input type="file" @change="choose"><el-button type="primary" @click="upload(false)">上传到当前目录</el-button><el-button v-if="replaceTarget" type="warning" @click="confirmReplace">替换所选资源</el-button></div>
      </div>
      <el-alert v-if="replaceTarget" :title="`待替换：${replaceTarget}`" type="info" :closable="false" style="margin-bottom:12px" />
      <el-table :data="entries" data-testid="static-resource-table">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="path" label="路径" />
        <el-table-column label="类型" width="100"><template #default="s">{{ s.row.directory ? '目录' : '文件' }}</template></el-table-column>
        <el-table-column prop="size" label="大小" width="120" />
        <el-table-column label="操作" width="260"><template #default="s">
          <el-button v-if="s.row.directory" link type="primary" @click="enter(asEntry(s.row))">进入</el-button>
          <template v-else>
            <el-link :href="publicUrl(s.row.path)" target="_blank" type="primary">查看/下载</el-link>
            <el-button link type="warning" @click="prepareReplace(asEntry(s.row))">替换</el-button>
            <el-button link type="danger" @click="remove(asEntry(s.row))">删除</el-button>
          </template>
        </template></el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" style="margin-top:16px"><template #header>回收区</template><el-table :data="trash"><el-table-column prop="originalPath" label="原路径" /><el-table-column label="操作" width="100"><template #default="s"><el-button link type="primary" @click="restore(asTrash(s.row))">恢复</el-button></template></el-table-column></el-table></el-card>
  </main>
</template>

<style scoped>
.toolbar{display:flex;flex-direction:column;gap:12px;margin-bottom:16px}.path-bar,.resource-upload{display:flex;align-items:center;gap:12px}.path-bar code{padding:6px 10px;background:#f5f7fa;border-radius:4px}.resource-upload{flex-wrap:wrap}
</style>
