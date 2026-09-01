<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminIconAction from '../../components/AdminIconAction.vue'
import AdminPanelToggle from '../../components/AdminPanelToggle.vue'
import ImageResourcePicker from '../../components/ImageResourcePicker.vue'
import {
  createSiteConfig, deleteSiteConfig, listSiteConfig, listSitePropertyGroups, updateSiteConfig, updateSiteConfigDefinition,
  type SiteConfigDraft, type SiteConfigItem, type SitePropertyGroupDefinition, type SitePropertyType,
} from '../../api/siteConfig'

const items = ref<SiteConfigItem[]>([])
const groups = ref<SitePropertyGroupDefinition[]>([])
const selectedGroup = ref<string | null>(null)
const sideCollapsed = ref(false)
const loading = ref(false)
const saving = ref('')
const dialog = ref(false)
const valueDialog = ref(false)
const editingKey = ref<string | null>(null)
const valueEditingKey = ref<string | null>(null)
const valueDraft = ref('')
const form = reactive<SiteConfigDraft>({ key: '', name: '', groupCode: 'GENERAL', value: '', valueType: 'TEXT', description: '', sortOrder: 0, required: false, system: false, enabled: true })
const types: Array<{ value: SitePropertyType; label: string }> = [
  { value: 'TEXT', label: '文本' }, { value: 'INTEGER', label: '整数' }, { value: 'RESOURCE_PATH', label: '静态资源路径' }, { value: 'JSON', label: 'JSON' }, { value: 'URL', label: 'URL' }, { value: 'BOOLEAN', label: '布尔值' },
]

const filteredItems = computed(() => selectedGroup.value == null ? items.value : items.value.filter(item => item.groupCode === selectedGroup.value))
const currentGroupName = computed(() => selectedGroup.value == null ? '全部属性' : groups.value.find(group => group.code === selectedGroup.value)?.name || selectedGroup.value)
const editingItem = computed(() => editingKey.value == null ? null : items.value.find(item => item.key === editingKey.value) || null)
const valueEditingItem = computed(() => valueEditingKey.value == null ? null : items.value.find(item => item.key === valueEditingKey.value) || null)
const asConfig = (row: unknown) => row as SiteConfigItem

onMounted(refresh)
async function refresh() { loading.value = true; try { ;[items.value, groups.value] = await Promise.all([listSiteConfig(), listSitePropertyGroups()]); if (selectedGroup.value != null && !groups.value.some(group => group.code === selectedGroup.value)) selectedGroup.value = null } catch (error) { ElMessage.error(message(error)) } finally { loading.value = false } }
function selectGroup(code: string | null) { selectedGroup.value = code }
function groupCount(code: string) { return items.value.filter(item => item.groupCode === code).length }
function groupName(code: string) { return groups.value.find(group => group.code === code)?.name || code }
function add() { editingKey.value = null; Object.assign(form, { key: '', name: '', groupCode: selectedGroup.value || 'GENERAL', value: '', valueType: 'TEXT', description: '', sortOrder: 0, required: false, system: false, enabled: true }); dialog.value = true }
function edit(row: SiteConfigItem) { editingKey.value = row.key; Object.assign(form, { key: row.key, name: row.name, groupCode: row.groupCode, value: row.value, valueType: row.valueType, description: row.description, sortOrder: row.sortOrder, required: row.required, system: row.system, enabled: row.enabled }); dialog.value = true }
function openValueEditor(row: SiteConfigItem) { valueEditingKey.value = row.key; valueDraft.value = row.value; valueDialog.value = true }
async function saveDefinition() { if (!validateValue(form.valueType, form.value)) return; saving.value = form.key || 'new'; try { editingKey.value ? await updateSiteConfigDefinition(editingKey.value, { ...form }) : await createSiteConfig({ ...form }); dialog.value = false; await refresh(); ElMessage.success('网站属性已保存') } catch (error) { ElMessage.error(message(error)) } finally { saving.value = '' } }
async function saveValue() { const row = valueEditingItem.value; if (!row || !validateValue(row.valueType, valueDraft.value)) return; saving.value = row.key; try { await updateSiteConfig(row.key, valueDraft.value); valueDialog.value = false; await refresh(); ElMessage.success('属性值已保存') } catch (error) { ElMessage.error(message(error)) } finally { saving.value = '' } }
async function remove(row: SiteConfigItem) { try { await ElMessageBox.confirm(`确定删除网站属性“${row.name}（${row.key}）”吗？`, '删除网站属性', { type: 'warning' }); await deleteSiteConfig(row.key); await refresh() } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(message(error)) } }
function validateValue(type: SitePropertyType, value: string) { const normalized = value.trim(); if (type === 'JSON' && normalized) { try { const parsed = JSON.parse(normalized); if (parsed == null || typeof parsed !== 'object') throw new Error() } catch { ElMessage.warning('JSON 属性格式不正确，请修正后再保存'); return false } } if (type === 'RESOURCE_PATH' && normalized && !normalized.startsWith('/static/')) { ElMessage.warning('静态资源属性必须使用 /static/ 路径'); return false } if (type === 'BOOLEAN' && normalized && !['true', 'false'].includes(normalized.toLowerCase())) { ElMessage.warning('布尔属性必须填写 true 或 false'); return false } if (type === 'INTEGER' && normalized && !/^-?\d+$/.test(normalized)) { ElMessage.warning('整数属性必须填写整数'); return false } return true }
function typeName(type: SitePropertyType) { return types.find(item => item.value === type)?.label || type }
const message = (error: unknown) => error instanceof Error ? error.message : '操作失败'
</script>

<template>
  <main class="admin-shell">
    <header class="page-header"><div><p class="eyebrow">站点设置</p><h1>网站属性</h1><p class="subtitle">维护站点身份信息与少量低风险站点级行为参数；预置属性定义不可删除，属性值仍可正常维护。</p></div><el-button data-testid="add-site-property" type="primary" @click="add">新增属性</el-button></header>
    <el-alert title="网站属性用于运营可维护的站点级数据；预置只保护稳定属性定义，不限制运营值修改。数据库、上传安全限制等仍由工程/部署配置负责。" type="info" :closable="false" show-icon />

    <div class="page-management-layout" :class="{ 'side-panel-collapsed': sideCollapsed }" style="margin-top:16px">
      <el-card class="page-group-panel" shadow="never">
        <div class="page-group-heading"><strong>属性分组</strong><span>分组来自 CMS 资源元数据</span></div>
        <button class="page-group-item" :class="{ active: selectedGroup == null }" data-testid="site-property-group-all" type="button" @click="selectGroup(null)"><span>全部属性</span><small>{{ items.length }}</small></button>
        <button v-for="group in groups" :key="group.code" class="page-group-item" :class="{ active: selectedGroup === group.code }" :data-testid="`site-property-group-${group.code}`" type="button" @click="selectGroup(group.code)"><span>{{ group.name }}</span><small>{{ groupCount(group.code) }}</small></button>
      </el-card>

      <el-card class="page-list-panel" shadow="never">
        <div class="page-list-context" data-testid="site-property-group-context"><div><AdminPanelToggle :collapsed="sideCollapsed" label="属性分组" @toggle="sideCollapsed = !sideCollapsed" /><strong>{{ currentGroupName }}</strong><span>{{ selectedGroup == null ? '查看全部网站属性' : '当前属性分组' }}</span></div><small>共 {{ filteredItems.length }} 项</small></div>
        <el-table v-loading="loading" :data="filteredItems" row-key="key" data-testid="site-config-table">
          <el-table-column label="属性名称" min-width="150"><template #default="scope"><span>{{asConfig(scope.row).name}}</span><el-tag v-if="asConfig(scope.row).preset" :data-testid="`preset-site-config-${asConfig(scope.row).key}`" size="small" type="info" style="margin-left:8px">预置</el-tag></template></el-table-column>
          <el-table-column prop="key" label="Key" min-width="190" /><el-table-column label="分组" width="130"><template #default="scope">{{ groupName(asConfig(scope.row).groupCode) }}</template></el-table-column><el-table-column label="类型" width="100"><template #default="scope">{{ typeName(asConfig(scope.row).valueType) }}</template></el-table-column>
          <el-table-column label="属性值" min-width="220"><template #default="scope"><div class="site-config-value-preview"><template v-if="asConfig(scope.row).valueType === 'RESOURCE_PATH' && asConfig(scope.row).value"><img class="site-config-value-thumb" :src="asConfig(scope.row).value" alt=""><code :title="asConfig(scope.row).value">{{asConfig(scope.row).value}}</code></template><el-tag v-else-if="asConfig(scope.row).valueType === 'BOOLEAN'" size="small" :type="asConfig(scope.row).value.toLowerCase() === 'true' ? 'success' : 'info'">{{asConfig(scope.row).value.toLowerCase() === 'true' ? '是' : '否'}}</el-tag><span v-else class="site-config-value-text" :title="asConfig(scope.row).value">{{asConfig(scope.row).value || '—'}}</span></div></template></el-table-column>
          <el-table-column label="操作" width="118" fixed="right"><template #default="scope"><div class="admin-table-actions"><AdminIconAction :testid="`edit-site-config-value-${asConfig(scope.row).key}`" label="编辑值" :icon="Edit" @click="openValueEditor(asConfig(scope.row))" /><AdminIconAction label="定义" :icon="Setting" @click="edit(asConfig(scope.row))" /><AdminIconAction v-if="!asConfig(scope.row).preset" label="删除" :icon="Delete" type="danger" @click="remove(asConfig(scope.row))" /></div></template></el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="valueDialog" :title="`编辑属性值${valueEditingItem ? `：${valueEditingItem.name}` : ''}`" width="620px" data-testid="site-config-value-dialog">
      <el-form v-if="valueEditingItem" label-width="100px">
        <el-form-item label="属性"><el-input :model-value="`${valueEditingItem.name}（${valueEditingItem.key}）`" disabled /></el-form-item>
        <el-form-item label="类型"><el-input :model-value="typeName(valueEditingItem.valueType)" disabled /></el-form-item>
        <el-form-item label="属性值"><el-switch v-if="valueEditingItem.valueType === 'BOOLEAN'" :model-value="valueDraft.toLowerCase() === 'true'" @change="value => valueDraft = value === true ? 'true' : 'false'" /><ImageResourcePicker v-else-if="valueEditingItem.valueType === 'RESOURCE_PATH'" :model-value="valueDraft" :upload-directory="`uploads/site-properties/${valueEditingItem.key}`" @update:model-value="value => valueDraft = value || ''" /><el-input v-else v-model="valueDraft" :data-testid="`site-config-${valueEditingItem.key}`" :type="valueEditingItem.valueType === 'JSON' ? 'textarea' : valueEditingItem.valueType === 'INTEGER' ? 'number' : 'text'" :rows="valueEditingItem.valueType === 'JSON' ? 6 : undefined" /></el-form-item>
        <el-alert v-if="valueEditingItem.description" :title="valueEditingItem.description" type="info" :closable="false" />
      </el-form>
      <template #footer><el-button @click="valueDialog=false">取消</el-button><el-button v-if="valueEditingItem" :data-testid="`save-site-config-${valueEditingItem.key}`" type="primary" :loading="saving === valueEditingItem.key" @click="saveValue">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="dialog" :title="editingKey ? '编辑网站属性' : '新增网站属性'" width="680px">
      <el-form label-width="100px">
        <el-form-item label="Key"><el-input v-model="form.key" :disabled="Boolean(editingKey)" placeholder="例如 SUPPORT_EMAIL" /><div v-if="editingItem?.preset" data-testid="preset-site-config-key-hint" style="color:#909399;font-size:12px">预置网站属性的 Key 是稳定站点身份，不允许修改或删除。</div></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分组"><el-select v-model="form.groupCode" data-testid="site-property-group-select" style="width:100%"><el-option v-for="group in groups" :key="group.code" :label="group.name" :value="group.code" /></el-select><div style="color:#909399;font-size:12px">分组定义来自部署资源配置，不在 CMS 数据库中单独维护。</div></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.valueType" style="width:100%"><el-option v-for="item in types" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item v-if="!editingKey" label="初始值"><ImageResourcePicker v-if="form.valueType === 'RESOURCE_PATH'" :model-value="form.value" :upload-directory="`uploads/site-properties/${form.key || 'NEW_PROPERTY'}`" @update:model-value="value => form.value = value || ''" /><el-input v-else v-model="form.value" :type="form.valueType === 'JSON' ? 'textarea' : form.valueType === 'INTEGER' ? 'number' : 'text'" :rows="5" /></el-form-item>
        <el-alert v-else title="属性值请使用列表中的“编辑值”操作维护；这里仅调整属性定义。修改类型前应确保当前值符合新类型。" type="info" :closable="false" style="margin-bottom:18px" />
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" /></el-form-item><el-form-item label="排序"><el-input-number v-model="form.sortOrder" /></el-form-item><el-form-item label="必填"><el-switch v-model="form.required" /></el-form-item><el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" :loading="Boolean(saving)" @click="saveDefinition">保存</el-button></template>
    </el-dialog>
  </main>
</template>
