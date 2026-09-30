<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  AdminAuditRequestError,
  getAuditEvent,
  listAuditEvents,
  type AdminAuditAction,
  type AdminAuditEvent,
  type AdminAuditObjectType,
  type AdminAuditQuery,
  type AdminAuditResult,
} from '../../api/audit'

const rows = ref<AdminAuditEvent[]>([])
const loading = ref(false)
const detailLoading = ref(false)
const detail = ref<AdminAuditEvent | null>(null)
const detailVisible = ref(false)
const accessState = ref<'unauthenticated' | 'forbidden' | 'error' | null>(null)
const accessMessage = ref('')
const cursors = ref<Array<string | null>>([null])
const pageIndex = ref(0)
const nextCursor = ref<string | null>(null)
const filters = reactive({
  identitySource: '', userId: '', action: '' as AdminAuditAction | '', objectType: '' as AdminAuditObjectType | '',
  objectId: '', result: '' as AdminAuditResult | '', requestCorrelationId: '', startedFrom: '', startedBefore: '',
})

const actions: Array<{ value: AdminAuditAction; label: string }> = [
  ['CREATE', '创建'], ['UPDATE', '更新'], ['DELETE', '删除'], ['PUBLISH', '发布'], ['WITHDRAW', '撤回'],
  ['UPLOAD', '上传'], ['REPLACE', '替换'], ['TRASH', '移入回收区'], ['RESTORE', '恢复'],
].map(([value, label]) => ({ value: value as AdminAuditAction, label }))
const objectTypes: Array<{ value: AdminAuditObjectType; label: string }> = [
  ['COLUMN', '栏目'], ['ARTICLE', '文章'], ['NAVIGATION_LOCATION', '导航位置'], ['NAVIGATION_ITEM', '导航条目'],
  ['PAGE_GROUP', '单页分组'], ['PAGE', '单页'], ['CMS_LIST', '列表'], ['CMS_LIST_ITEM', '列表项'],
  ['ADVERTISEMENT_SLOT', '展示位'], ['ADVERTISEMENT_ITEM', '展示内容'], ['SITE_PROPERTY', '网站属性'],
  ['MANAGED_RESOURCE', '托管资源'], ['STATIC_RESOURCE', '静态资源'],
].map(([value, label]) => ({ value: value as AdminAuditObjectType, label }))
const results: Array<{ value: AdminAuditResult; label: string }> = [
  { value: 'STARTED', label: '结果未确认' }, { value: 'SUCCEEDED', label: '成功' },
  { value: 'FAILED', label: '失败' }, { value: 'ROLLED_BACK', label: '已回滚' },
]

onMounted(() => loadPage(0))

function query(cursor: string | null): AdminAuditQuery {
  return {
    identitySource: filters.identitySource,
    userId: filters.userId,
    action: filters.action,
    objectType: filters.objectType,
    objectId: filters.objectId,
    result: filters.result,
    requestCorrelationId: filters.requestCorrelationId,
    startedFrom: toInstant(filters.startedFrom),
    startedBefore: toInstant(filters.startedBefore),
    cursor,
    limit: 20,
  }
}

async function loadPage(index: number) {
  loading.value = true
  accessState.value = null
  try {
    const page = await listAuditEvents(query(cursors.value[index] ?? null))
    rows.value = page.items
    pageIndex.value = index
    nextCursor.value = page.nextCursor
    cursors.value = cursors.value.slice(0, index + 1)
    if (page.nextCursor) cursors.value.push(page.nextCursor)
  } catch (error) {
    rows.value = []
    const requestError = error instanceof AdminAuditRequestError ? error : null
    accessState.value = requestError?.status === 401 ? 'unauthenticated' : requestError?.status === 403 ? 'forbidden' : 'error'
    accessMessage.value = requestError?.message ?? '操作审计加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  if (Boolean(filters.identitySource.trim()) !== Boolean(filters.userId.trim())) {
    ElMessage.warning('身份来源和用户 ID 必须同时填写')
    return
  }
  if (filters.objectId.trim() && !filters.objectType) {
    ElMessage.warning('按对象标识筛选时必须选择对象类型')
    return
  }
  cursors.value = [null]
  loadPage(0)
}

function resetFilters() {
  Object.assign(filters, { identitySource: '', userId: '', action: '', objectType: '', objectId: '', result: '', requestCorrelationId: '', startedFrom: '', startedBefore: '' })
  applyFilters()
}

async function openDetail(row: AdminAuditEvent) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await getAuditEvent(row.auditId)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '审计详情加载失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

function toInstant(value: string) { return value ? new Date(value).toISOString() : undefined }
function dateTime(value: string | null) { return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '—' }
function actionLabel(value: AdminAuditAction) { return actions.find(item => item.value === value)?.label ?? value }
function objectTypeLabel(value: AdminAuditObjectType) { return objectTypes.find(item => item.value === value)?.label ?? value }
function resultLabel(value: AdminAuditResult) { return results.find(item => item.value === value)?.label ?? value }
function resultType(value: AdminAuditResult) { return value === 'SUCCEEDED' ? 'success' : value === 'STARTED' ? 'warning' : value === 'FAILED' ? 'danger' : 'info' }
const asAudit = (row: unknown) => row as AdminAuditEvent
</script>

<template>
  <main class="admin-shell audit-management">
    <header class="page-header"><div><p class="eyebrow">安全审计</p><h1>操作审计</h1><p class="subtitle">按可信操作者、业务对象与执行结果追溯管理写操作。</p></div></header>

    <el-alert v-if="accessState === 'unauthenticated'" data-testid="audit-unauthenticated" title="管理身份未认证，无法查看操作审计。" type="warning" :closable="false" show-icon />
    <el-alert v-else-if="accessState === 'forbidden'" data-testid="audit-forbidden" title="当前管理身份没有操作审计查看权限。" type="error" :closable="false" show-icon />
    <el-alert v-else-if="accessState === 'error'" data-testid="audit-load-error" :title="accessMessage" type="error" :closable="false" show-icon />

    <el-card v-if="accessState == null" shadow="never" class="audit-filter-card">
      <el-form label-position="top" class="audit-filter-grid" @submit.prevent="applyFilters">
        <el-form-item label="身份来源"><el-input v-model="filters.identitySource" data-testid="audit-filter-source" clearable /></el-form-item>
        <el-form-item label="用户 ID"><el-input v-model="filters.userId" data-testid="audit-filter-user" clearable /></el-form-item>
        <el-form-item label="动作"><el-select v-model="filters.action" clearable data-testid="audit-filter-action"><el-option v-for="item in actions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="对象类型"><el-select v-model="filters.objectType" clearable data-testid="audit-filter-object-type"><el-option v-for="item in objectTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="对象标识"><el-input v-model="filters.objectId" data-testid="audit-filter-object-id" clearable /></el-form-item>
        <el-form-item label="结果"><el-select v-model="filters.result" clearable data-testid="audit-filter-result"><el-option v-for="item in results" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="开始时间（含）"><el-input v-model="filters.startedFrom" type="datetime-local" data-testid="audit-filter-started-from" /></el-form-item>
        <el-form-item label="开始时间（不含）"><el-input v-model="filters.startedBefore" type="datetime-local" data-testid="audit-filter-started-before" /></el-form-item>
        <el-form-item label="请求关联标识" class="audit-correlation-filter"><el-input v-model="filters.requestCorrelationId" data-testid="audit-filter-correlation" clearable /></el-form-item>
        <div class="audit-filter-actions"><el-button @click="resetFilters">重置</el-button><el-button type="primary" data-testid="audit-apply-filters" @click="applyFilters">查询</el-button></div>
      </el-form>
    </el-card>

    <el-card v-if="accessState == null" shadow="never">
      <el-table v-loading="loading" :data="rows" row-key="auditId" data-testid="audit-table" empty-text="没有符合条件的审计记录">
        <el-table-column label="开始时间" width="180"><template #default="scope">{{ dateTime(asAudit(scope.row).startedAt) }}</template></el-table-column>
        <el-table-column label="操作者" min-width="190"><template #default="scope"><strong>{{ asAudit(scope.row).userId }}</strong><small class="audit-secondary">{{ asAudit(scope.row).identitySource }}</small></template></el-table-column>
        <el-table-column label="动作" width="110"><template #default="scope">{{ actionLabel(asAudit(scope.row).action) }}</template></el-table-column>
        <el-table-column label="对象" min-width="180"><template #default="scope">{{ objectTypeLabel(asAudit(scope.row).objectType) }}<small class="audit-secondary">{{ asAudit(scope.row).objectId ?? '对象标识不可用' }}</small></template></el-table-column>
        <el-table-column label="结果" width="120"><template #default="scope"><el-tag :type="resultType(asAudit(scope.row).result)">{{ resultLabel(asAudit(scope.row).result) }}</el-tag></template></el-table-column>
        <el-table-column label="角色快照" min-width="130"><template #default="scope"><el-tag v-for="role in asAudit(scope.row).roles" :key="role" size="small" type="info" class="audit-role">{{ role }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="90" fixed="right"><template #default="scope"><el-button text type="primary" :data-testid="`audit-detail-${asAudit(scope.row).auditId}`" @click="openDetail(asAudit(scope.row))">详情</el-button></template></el-table-column>
      </el-table>
      <div class="audit-pagination" data-testid="audit-pagination"><span>第 {{ pageIndex + 1 }} 页，每页最多 20 条</span><div><el-button :disabled="pageIndex === 0 || loading" @click="loadPage(pageIndex - 1)">上一页</el-button><el-button type="primary" plain :disabled="!nextCursor || loading" @click="loadPage(pageIndex + 1)">下一页</el-button></div></div>
    </el-card>

    <el-drawer v-model="detailVisible" title="审计记录详情" size="560px" data-testid="audit-detail-drawer">
      <div v-loading="detailLoading">
        <el-descriptions v-if="detail" :column="1" border>
          <el-descriptions-item label="审计 ID"><code>{{ detail.auditId }}</code></el-descriptions-item>
          <el-descriptions-item label="请求关联标识"><code>{{ detail.requestCorrelationId }}</code></el-descriptions-item>
          <el-descriptions-item label="操作者">{{ detail.identitySource }} / {{ detail.userId }}</el-descriptions-item>
          <el-descriptions-item label="角色快照"><el-tag v-for="role in detail.roles" :key="role" size="small" type="info" class="audit-role">{{ role }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="动作">{{ actionLabel(detail.action) }}</el-descriptions-item>
          <el-descriptions-item label="对象">{{ objectTypeLabel(detail.objectType) }} / {{ detail.objectId ?? '对象标识不可用' }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ dateTime(detail.startedAt) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ dateTime(detail.completedAt) }}</el-descriptions-item>
          <el-descriptions-item label="结果"><el-tag :type="resultType(detail.result)">{{ resultLabel(detail.result) }}</el-tag></el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </main>
</template>

<style scoped>
.audit-filter-card{margin-bottom:18px}.audit-filter-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:4px 16px;align-items:end}.audit-filter-grid :deep(.el-form-item){margin-bottom:10px}.audit-filter-grid :deep(.el-select){width:100%}.audit-correlation-filter{grid-column:span 2}.audit-filter-actions{display:flex;justify-content:flex-end;gap:8px;padding-bottom:10px}.audit-secondary{display:block;color:#8491a1;margin-top:3px}.audit-role{margin-right:4px}.audit-pagination{display:flex;align-items:center;justify-content:space-between;color:#606266;padding-top:16px}.audit-pagination>div{display:flex;gap:8px}.audit-management code{overflow-wrap:anywhere}@media(max-width:1100px){.audit-filter-grid{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:720px){.audit-filter-grid{grid-template-columns:1fr}.audit-correlation-filter{grid-column:auto}.audit-pagination{align-items:flex-start;flex-direction:column;gap:12px}}
</style>
