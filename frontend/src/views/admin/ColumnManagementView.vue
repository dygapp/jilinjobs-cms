<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createColumn,
  deleteColumn,
  listColumns,
  updateColumn,
  type CmsColumn,
  type ColumnDraft,
} from '../../api/columns'

type ColumnNode = CmsColumn & { children: ColumnNode[] }

const columns = ref<CmsColumn[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<ColumnDraft>({
  parentId: null,
  name: '',
  sortOrder: 0,
  enabled: true,
})

const treeRows = computed<ColumnNode[]>(() => {
  const nodes = new Map<number, ColumnNode>()
  columns.value.forEach((item) => nodes.set(item.id, { ...item, children: [] }))

  const roots: ColumnNode[] = []
  nodes.forEach((node) => {
    if (node.parentId != null && nodes.has(node.parentId)) {
      nodes.get(node.parentId)?.children.push(node)
    } else {
      roots.push(node)
    }
  })

  const sortRecursively = (items: ColumnNode[]) => {
    items.sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id)
    items.forEach((item) => sortRecursively(item.children))
  }
  sortRecursively(roots)
  return roots
})

const parentOptions = computed(() => columns.value.filter((item) => item.id !== editingId.value))

onMounted(refresh)

async function refresh() {
  loading.value = true
  try {
    columns.value = await listColumns()
  } catch (error) {
    ElMessage.error(toMessage(error))
  } finally {
    loading.value = false
  }
}

function openCreate(parentId: number | null = null) {
  editingId.value = null
  Object.assign(form, {
    parentId,
    name: '',
    sortOrder: 0,
    enabled: true,
  })
  dialogVisible.value = true
}

function openEdit(row: CmsColumn) {
  editingId.value = row.id
  Object.assign(form, {
    parentId: row.parentId,
    name: row.name,
    sortOrder: row.sortOrder,
    enabled: row.enabled,
  })
  dialogVisible.value = true
}

async function save() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入栏目名称')
    return
  }

  saving.value = true
  try {
    if (editingId.value == null) {
      await createColumn({ ...form })
      ElMessage.success('栏目已创建')
    } else {
      await updateColumn(editingId.value, { ...form })
      ElMessage.success('栏目已更新')
    }
    dialogVisible.value = false
    await refresh()
  } catch (error) {
    ElMessage.error(toMessage(error))
  } finally {
    saving.value = false
  }
}

async function toggleEnabled(row: CmsColumn, enabled: boolean) {
  try {
    await updateColumn(row.id, {
      parentId: row.parentId,
      name: row.name,
      sortOrder: row.sortOrder,
      enabled,
    })
    await refresh()
  } catch (error) {
    ElMessage.error(toMessage(error))
    await refresh()
  }
}

async function remove(row: CmsColumn) {
  try {
    await ElMessageBox.confirm(
      `确定删除栏目“${row.name}”吗？`,
      '删除栏目',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await deleteColumn(row.id)
    ElMessage.success('栏目已删除')
    await refresh()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error(toMessage(error))
  }
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
        <h1>栏目管理</h1>
        <p class="subtitle">维护中心主站栏目层级、排序与启停状态。</p>
      </div>
      <el-button data-testid="add-column" type="primary" @click="openCreate()">新增栏目</el-button>
    </header>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="treeRows"
        row-key="id"
        default-expand-all
      >
        <el-table-column prop="name" label="栏目名称" min-width="260" />
        <el-table-column prop="sortOrder" label="排序" width="110" />
        <el-table-column label="状态" width="130">
          <template #default="scope">
            <el-switch
              :model-value="scope.row.enabled"
              :data-testid="`enabled-${scope.row.id}`"
              active-text="启用"
              inactive-text="停用"
              @change="(value: boolean) => toggleEnabled(scope.row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openCreate(scope.row.id)">新增子栏目</el-button>
            <el-button :data-testid="`edit-${scope.row.id}`" link type="primary" @click="openEdit(scope.row)">编辑</el-button>
            <el-button :data-testid="`delete-${scope.row.id}`" link type="danger" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增栏目' : '编辑栏目'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="栏目名称" required>
          <el-input v-model="form.name" placeholder="请输入栏目名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="上级栏目">
          <el-select v-model="form.parentId" clearable placeholder="作为一级栏目" style="width: 100%">
            <el-option
              v-for="item in parentOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
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
        <el-button data-testid="save-column" type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>
