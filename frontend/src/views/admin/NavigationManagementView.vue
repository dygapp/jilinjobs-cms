<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listColumns, type CmsColumn } from '../../api/columns'
import {
  createNavigation,
  deleteNavigation,
  listNavigations,
  updateNavigation,
  type CmsNavigation,
  type NavigationDraft,
  type NavigationPosition,
} from '../../api/navigation'

const items = ref<CmsNavigation[]>([])
const columns = ref<CmsColumn[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)

const form = reactive<NavigationDraft>({
  name: '',
  position: 'MAIN',
  category: null,
  targetType: 'COLUMN',
  targetColumnId: null,
  targetUrl: null,
  sortOrder: 0,
  enabled: true,
})

const enabledColumns = computed(() => columns.value.slice().sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id))

const positionOptions: Array<{ value: NavigationPosition; label: string }> = [
  { value: 'MAIN', label: '主导航' },
  { value: 'SERVICE', label: '服务入口' },
  { value: 'SITE', label: '网站导航' },
]

onMounted(refresh)

async function refresh() {
  loading.value = true
  try {
    const [navigationData, columnData] = await Promise.all([listNavigations(), listColumns()])
    items.value = navigationData
    columns.value = columnData
  } catch (error) {
    ElMessage.error(toMessage(error))
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    name: '',
    position: 'MAIN',
    category: null,
    targetType: 'COLUMN',
    targetColumnId: null,
    targetUrl: null,
    sortOrder: 0,
    enabled: true,
  } satisfies NavigationDraft)
  dialogVisible.value = true
}

function openEdit(row: CmsNavigation) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    position: row.position,
    category: row.category,
    targetType: row.targetType,
    targetColumnId: row.targetColumnId,
    targetUrl: row.targetUrl,
    sortOrder: row.sortOrder,
    enabled: row.enabled,
  } satisfies NavigationDraft)
  dialogVisible.value = true
}

async function save() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入导航名称')
    return
  }
  if (form.targetType === 'COLUMN' && form.targetColumnId == null) {
    ElMessage.warning('请选择目标栏目')
    return
  }
  if (form.targetType === 'LINK' && !form.targetUrl?.trim()) {
    ElMessage.warning('请输入目标地址')
    return
  }

  saving.value = true
  try {
    const draft: NavigationDraft = { ...form }
    if (editingId.value == null) {
      await createNavigation(draft)
      ElMessage.success('导航条目已创建')
    } else {
      await updateNavigation(editingId.value, draft)
      ElMessage.success('导航条目已更新')
    }
    dialogVisible.value = false
    await refresh()
  } catch (error) {
    ElMessage.error(toMessage(error))
  } finally {
    saving.value = false
  }
}

async function toggleEnabled(row: CmsNavigation, enabled: boolean) {
  try {
    await updateNavigation(row.id, {
      name: row.name,
      position: row.position,
      category: row.category,
      targetType: row.targetType,
      targetColumnId: row.targetColumnId,
      targetUrl: row.targetUrl,
      sortOrder: row.sortOrder,
      enabled,
    })
    await refresh()
  } catch (error) {
    ElMessage.error(toMessage(error))
    await refresh()
  }
}

async function remove(row: CmsNavigation) {
  try {
    await ElMessageBox.confirm(
      `确定删除导航“${row.name}”吗？`,
      '删除导航',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await deleteNavigation(row.id)
    ElMessage.success('导航条目已删除')
    await refresh()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error(toMessage(error))
  }
}

function positionLabel(position: NavigationPosition): string {
  return positionOptions.find((item) => item.value === position)?.label ?? position
}

function targetLabel(row: CmsNavigation): string {
  if (row.targetType === 'LINK') {
    return row.targetUrl ?? '-'
  }
  return columns.value.find((item) => item.id === row.targetColumnId)?.name ?? `栏目 #${row.targetColumnId ?? '-'}`
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
        <h1>导航管理</h1>
        <p class="subtitle">维护主导航、服务入口和网站导航的目标、排序与启停状态。</p>
      </div>
      <div class="header-actions">
        <router-link class="text-link" to="/admin/columns">栏目管理</router-link>
        <el-button data-testid="add-navigation" type="primary" @click="openCreate">新增导航</el-button>
      </div>
    </header>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="items" row-key="id">
        <el-table-column prop="name" label="导航名称" min-width="180" />
        <el-table-column label="位置" width="120">
          <template #default="scope">{{ positionLabel(scope.row.position) }}</template>
        </el-table-column>
        <el-table-column prop="category" label="类别" min-width="150">
          <template #default="scope">{{ scope.row.category || '-' }}</template>
        </el-table-column>
        <el-table-column label="目标" min-width="240">
          <template #default="scope">{{ targetLabel(scope.row) }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="状态" width="130">
          <template #default="scope">
            <el-switch
              :model-value="scope.row.enabled"
              :data-testid="`navigation-enabled-${scope.row.id}`"
              active-text="启用"
              inactive-text="停用"
              @change="(value) => toggleEnabled(scope.row, value === true)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button :data-testid="`navigation-edit-${scope.row.id}`" link type="primary" @click="openEdit(scope.row)">编辑</el-button>
            <el-button :data-testid="`navigation-delete-${scope.row.id}`" link type="danger" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增导航' : '编辑导航'" width="560px">
      <el-form label-width="96px">
        <el-form-item label="导航名称" required>
          <el-input v-model="form.name" placeholder="请输入导航名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="展示位置" required>
          <el-select v-model="form.position" placeholder="请选择位置" style="width: 100%">
            <el-option v-for="option in positionOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="导航类别">
          <el-input v-model="form.category" clearable placeholder="可选，例如区域高校就业网站" maxlength="100" />
        </el-form-item>
        <el-form-item label="目标类型" required>
          <el-radio-group v-model="form.targetType">
            <el-radio value="COLUMN">本站栏目</el-radio>
            <el-radio value="LINK">链接地址</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.targetType === 'COLUMN'" label="目标栏目" required>
          <el-select v-model="form.targetColumnId" filterable placeholder="请选择栏目" style="width: 100%">
            <el-option v-for="column in enabledColumns" :key="column.id" :label="column.name" :value="column.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-else label="目标地址" required>
          <el-input v-model="form.targetUrl" placeholder="请输入站内路径或 HTTP(S) 地址" maxlength="1000" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :step="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button data-testid="save-navigation" type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>
