<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminIconAction from '../../components/AdminIconAction.vue'
import {
  createColumn,
  deleteColumn,
  listColumns,
  updateColumn,
  type CmsColumn,
  type ColumnDraft,
  type ContentImagePolicy,
} from '../../api/columns'

type Node = CmsColumn & { children: Node[] }

const columns = ref<CmsColumn[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<ColumnDraft>({ parentId: null, name: '', alias: '', coverPolicy: 'OPTIONAL', sortOrder: 0, enabled: true })

const treeRows = computed<Node[]>(() => {
  const map = new Map<number, Node>()
  columns.value.forEach(item => map.set(item.id, { ...item, children: [] }))
  const roots: Node[] = []
  map.forEach(node => {
    if (node.parentId != null && map.has(node.parentId)) map.get(node.parentId)!.children.push(node)
    else roots.push(node)
  })
  const sort = (items: Node[]) => {
    items.sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id)
    items.forEach(item => sort(item.children))
  }
  sort(roots)
  return roots
})

const parentOptions = computed(() => columns.value.filter(item => item.id !== editingId.value))
const editingColumn = computed(() => editingId.value == null ? null : columns.value.find(item => item.id === editingId.value) || null)
const asColumn = (row: unknown) => row as CmsColumn

onMounted(refresh)

async function refresh() {
  loading.value = true
  try { columns.value = await listColumns() }
  catch (error) { ElMessage.error(message(error)) }
  finally { loading.value = false }
}

function openCreate(parentId: number | null = null) {
  editingId.value = null
  Object.assign(form, { parentId, name: '', alias: '', coverPolicy: 'OPTIONAL', sortOrder: 0, enabled: true })
  dialogVisible.value = true
}

function openEdit(row: CmsColumn) {
  editingId.value = row.id
  Object.assign(form, { parentId: row.parentId, name: row.name, alias: row.alias, coverPolicy: row.coverPolicy, sortOrder: row.sortOrder, enabled: row.enabled })
  dialogVisible.value = true
}

async function save() {
  if (!form.name.trim()) { ElMessage.warning('请输入栏目名称'); return }
  saving.value = true
  try {
    editingId.value == null ? await createColumn({ ...form }) : await updateColumn(editingId.value, { ...form })
    dialogVisible.value = false
    await refresh()
  } catch (error) { ElMessage.error(message(error)) }
  finally { saving.value = false }
}

async function toggle(row: CmsColumn, enabled: boolean) {
  try {
    await updateColumn(row.id, { parentId: row.parentId, name: row.name, alias: row.alias, coverPolicy: row.coverPolicy, sortOrder: row.sortOrder, enabled })
    await refresh()
  } catch (error) { ElMessage.error(message(error)) }
}

async function remove(row: CmsColumn) {
  try {
    await ElMessageBox.confirm(`确定删除栏目“${row.name}”吗？`, '删除栏目', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    await deleteColumn(row.id)
    await refresh()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(message(error))
  }
}

function policyName(policy: ContentImagePolicy) {
  return policy === 'NONE' ? '不使用' : policy === 'REQUIRED' ? '必填' : '可选'
}

const message = (error: unknown) => error instanceof Error ? error.message : '操作失败'
</script>

<template>
  <main class="admin-shell">
    <header class="page-header">
      <div>
        <p class="eyebrow">内容结构</p>
        <h1>栏目管理</h1>
        <p class="subtitle">维护栏目层级、名称、文章封面要求、排序和启停状态。</p>
      </div>
      <el-button data-testid="add-column" type="primary" @click="openCreate()">新增栏目</el-button>
    </header>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="treeRows" row-key="id" default-expand-all>
        <el-table-column label="栏目名称" min-width="220"><template #default="scope"><span>{{ asColumn(scope.row).name }}</span><el-tag v-if="asColumn(scope.row).preset" :data-testid="`preset-column-${asColumn(scope.row).id}`" size="small" type="info" style="margin-left:8px">预置</el-tag></template></el-table-column>
        <el-table-column prop="alias" label="公开标识" min-width="180" />
        <el-table-column label="文章封面" width="110"><template #default="scope">{{ policyName(asColumn(scope.row).coverPolicy) }}</template></el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="状态" width="130">
          <template #default="scope">
            <el-switch :model-value="scope.row.enabled" :data-testid="`enabled-${scope.row.id}`" active-text="启用" inactive-text="停用" @change="value => toggle(asColumn(scope.row), value === true)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="124" fixed="right">
          <template #default="scope"><div class="admin-table-actions">
            <AdminIconAction label="新增子栏目" :icon="Plus" @click="openCreate(scope.row.id)" />
            <AdminIconAction :testid="`edit-${scope.row.id}`" label="编辑" :icon="Edit" @click="openEdit(asColumn(scope.row))" />
            <AdminIconAction v-if="!asColumn(scope.row).preset" :testid="`delete-${scope.row.id}`" label="删除" :icon="Delete" type="danger" @click="remove(asColumn(scope.row))" />
          </div></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增栏目' : '编辑栏目'" width="540px">
      <el-form label-width="100px">
        <el-form-item label="栏目名称" required><el-input v-model="form.name" placeholder="请输入栏目名称" /></el-form-item>
        <el-form-item label="公开标识"><el-input v-model="form.alias" :disabled="Boolean(editingColumn?.preset)" placeholder="留空时自动生成" /><div v-if="editingColumn?.preset" data-testid="preset-column-alias-hint" style="color:#909399;font-size:12px">预置栏目的公开标识不可修改。</div></el-form-item>
        <el-form-item label="上级栏目"><el-select v-model="form.parentId" clearable style="width:100%"><el-option v-for="item in parentOptions" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="文章封面">
          <el-select v-model="form.coverPolicy" data-testid="column-cover-policy" style="width:100%">
            <el-option label="不使用封面" value="NONE" />
            <el-option label="封面可选" value="OPTIONAL" />
            <el-option label="发布时必须有封面" value="REQUIRED" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.enabled" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button data-testid="save-column" type="primary" :loading="saving" @click="save">保存</el-button></template>
    </el-dialog>
  </main>
</template>
