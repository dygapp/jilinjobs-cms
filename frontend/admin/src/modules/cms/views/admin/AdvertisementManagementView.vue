<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, MoreFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdaptiveImagePreview from '../../components/AdaptiveImagePreview.vue'
import AdminIconAction from '../../components/AdminIconAction.vue'
import AdminPanelToggle from '../../components/AdminPanelToggle.vue'
import ImageResourcePicker from '../../components/ImageResourcePicker.vue'
import {
  createAdvertisement, createAdvertisementSlot, deleteAdvertisement, deleteAdvertisementSlot, listAdvertisements, listAdvertisementSlots,
  updateAdvertisement, updateAdvertisementSlot,
  type Advertisement, type AdvertisementDraft, type AdvertisementSlot, type AdvertisementSlotDraft,
} from '../../api/advertisements'

const slots = ref<AdvertisementSlot[]>([])
const ads = ref<Advertisement[]>([])
const activeId = ref<number | null>(null)
const sideCollapsed = ref(false)
const loading = ref(false)
const slotDialog = ref(false)
const adDialog = ref(false)
const saving = ref(false)
const editingSlot = ref<number | null>(null)
const editingAd = ref<number | null>(null)
const slotForm = reactive<AdvertisementSlotDraft>({ code: '', name: '', description: '', sortOrder: 0, enabled: true, system: false })
const adForm = reactive<AdvertisementDraft>({ title: '', imagePath: '', url: null, openMode: 'DEFAULT', startAt: null, endAt: null, sortOrder: 0, enabled: true })
const active = computed(() => slots.value.find(i => i.id === activeId.value) || null)
const editingSlotModel = computed(() => editingSlot.value == null ? null : slots.value.find(i => i.id === editingSlot.value) || null)
const asAd = (row: unknown) => row as Advertisement

onMounted(refresh)
async function refresh() { loading.value = true; try { slots.value = await listAdvertisementSlots(); if (!slots.value.some(i => i.id === activeId.value)) activeId.value = slots.value[0]?.id || null; await refreshAds() } catch (e) { ElMessage.error(msg(e)) } finally { loading.value = false } }
async function refreshAds() { ads.value = activeId.value ? await listAdvertisements(activeId.value) : [] }
async function select(id: number) { activeId.value = id; await refreshAds() }
function addSlot() { editingSlot.value = null; Object.assign(slotForm, { code: '', name: '', description: '', sortOrder: 0, enabled: true, system: false }); slotDialog.value = true }
function editSlot(row: AdvertisementSlot) { editingSlot.value = row.id; Object.assign(slotForm, { code: row.code, name: row.name, description: row.description, sortOrder: row.sortOrder, enabled: row.enabled, system: row.system }); slotDialog.value = true }
async function saveSlot() { saving.value = true; try { const saved = editingSlot.value ? await updateAdvertisementSlot(editingSlot.value, { ...slotForm }) : await createAdvertisementSlot({ ...slotForm }); slotDialog.value = false; activeId.value = saved.id; await refresh() } catch (e) { ElMessage.error(msg(e)) } finally { saving.value = false } }
async function removeSlot(row: AdvertisementSlot) { if (row.preset) return; try { await ElMessageBox.confirm(`删除展示位“${row.name}”将同时删除展示内容，是否继续？`, '删除展示位', { type: 'warning' }); await deleteAdvertisementSlot(row.id); await refresh() } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error(msg(e)) } }
function addAd() { editingAd.value = null; Object.assign(adForm, { title: '', imagePath: '', url: null, openMode: 'DEFAULT', startAt: null, endAt: null, sortOrder: 0, enabled: true }); adDialog.value = true }
function editAd(row: Advertisement) { editingAd.value = row.id; Object.assign(adForm, { title: row.title, imagePath: row.imagePath, url: row.url, openMode: row.openMode, startAt: row.startAt, endAt: row.endAt, sortOrder: row.sortOrder, enabled: row.enabled }); adDialog.value = true }
async function saveAd() { if (!activeId.value) return; saving.value = true; try { editingAd.value ? await updateAdvertisement(activeId.value, editingAd.value, { ...adForm }) : await createAdvertisement(activeId.value, { ...adForm }); adDialog.value = false; await refreshAds() } catch (e) { ElMessage.error(msg(e)) } finally { saving.value = false } }
async function removeAd(row: Advertisement) { if (!activeId.value) return; try { await ElMessageBox.confirm(`确定删除展示内容“${row.title}”吗？`, '删除展示内容', { type: 'warning' }); await deleteAdvertisement(activeId.value, row.id); await refreshAds() } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error(msg(e)) } }
function adStatus(row: Advertisement) { if (!row.enabled) return '已停用'; const now = Date.now(); if (row.startAt && new Date(row.startAt).getTime() > now) return '待生效'; if (row.endAt && new Date(row.endAt).getTime() <= now) return '已过期'; return '展示中' }
function openModeLabel(mode: string) { return mode === 'NO_LINK' ? '不跳转' : mode === 'SAME_WINDOW' ? '当前窗口' : mode === 'NEW_WINDOW' ? '新窗口' : '默认' }
const msg = (e: unknown) => e instanceof Error ? e.message : '操作失败'
</script>

<template>
  <main class="admin-shell">
    <header class="page-header"><div><p class="eyebrow">宣传与专题展示</p><h1>宣传展示管理</h1><p class="subtitle">按展示位维护宣传图片、跳转方式和有效时间。</p></div><el-button data-testid="add-ad-slot" type="primary" @click="addSlot">新增展示位</el-button></header>

    <div class="admin-split-layout" :class="{ 'side-panel-collapsed': sideCollapsed }">
      <el-card class="admin-side-panel" shadow="never">
        <div class="admin-side-list">
          <div v-for="slot in slots" :key="slot.id" class="admin-side-row" :class="{ active: activeId === slot.id }" :data-testid="`ad-slot-${slot.code}`" @click="select(slot.id)">
            <span class="admin-side-row-main">{{slot.name}}<el-tag v-if="slot.preset" :data-testid="`preset-ad-slot-${slot.code}`" size="small" type="info" style="margin-left:6px">预置</el-tag></span>
            <span @click.stop><el-dropdown trigger="click" @command="command => command === 'edit' ? editSlot(slot) : removeSlot(slot)"><el-button text circle aria-label="展示位操作"><el-icon><MoreFilled /></el-icon></el-button><template #dropdown><el-dropdown-menu><el-dropdown-item command="edit">编辑</el-dropdown-item><el-dropdown-item command="delete" divided :disabled="slot.preset">删除</el-dropdown-item></el-dropdown-menu></template></el-dropdown></span>
          </div>
        </div>
      </el-card>

      <el-card class="admin-main-panel" shadow="never">
        <template #header><div class="admin-card-header"><div class="admin-card-header-title"><AdminPanelToggle :collapsed="sideCollapsed" label="展示位导航" @toggle="sideCollapsed = !sideCollapsed" /><div><strong>{{active?.name || '请选择展示位'}}</strong><span v-if="active" style="margin-left:8px;color:#909399">{{active.code}} · {{ads.length}} 张</span><el-tag v-if="active?.preset" data-testid="active-ad-slot-preset" size="small" type="info" style="margin-left:8px">预置</el-tag></div></div><el-button data-testid="add-advertisement" type="primary" :disabled="!active" @click="addAd">新增展示内容</el-button></div></template>
        <el-table v-loading="loading" :data="ads" row-key="id" data-testid="advertisement-table">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column label="图片" min-width="230"><template #default="s"><div style="display:flex;align-items:center;gap:10px"><AdaptiveImagePreview :src="asAd(s.row).imagePath" :alt="asAd(s.row).title" adaptive style="width:80px;height:48px;flex:none" /><code style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap" :title="asAd(s.row).imagePath">{{asAd(s.row).imagePath}}</code></div></template></el-table-column>
          <el-table-column prop="url" label="目标地址" min-width="190" show-overflow-tooltip />
          <el-table-column label="点击行为" width="100"><template #default="s">{{openModeLabel(asAd(s.row).openMode)}}</template></el-table-column>
          <el-table-column prop="sortOrder" label="展示顺序" width="90" />
          <el-table-column label="状态" width="90"><template #default="s">{{adStatus(asAd(s.row))}}</template></el-table-column>
          <el-table-column label="操作" width="92" fixed="right"><template #default="s"><div class="admin-table-actions"><AdminIconAction label="编辑" :icon="Edit" @click="editAd(asAd(s.row))" /><AdminIconAction label="删除" :icon="Delete" type="danger" @click="removeAd(asAd(s.row))" /></div></template></el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="slotDialog" :title="editingSlot ? '编辑展示位' : '新增展示位'" width="560px"><el-form label-width="100px"><el-form-item label="展示位标识"><el-input v-model="slotForm.code" :disabled="Boolean(editingSlot)" /><div v-if="editingSlotModel?.preset" data-testid="preset-ad-slot-code-hint" style="color:#909399;font-size:12px">预置展示位的标识不可修改。</div></el-form-item><el-form-item label="名称"><el-input v-model="slotForm.name" /></el-form-item><el-form-item label="说明"><el-input v-model="slotForm.description" type="textarea" /></el-form-item><el-form-item label="展示位排序"><el-input-number v-model="slotForm.sortOrder" /></el-form-item><el-form-item label="启用"><el-switch v-model="slotForm.enabled" /></el-form-item></el-form><template #footer><el-button @click="slotDialog=false">取消</el-button><el-button type="primary" :loading="saving" @click="saveSlot">保存</el-button></template></el-dialog>

    <el-dialog v-model="adDialog" :title="editingAd ? '编辑展示内容' : '新增展示内容'" width="680px"><el-form label-width="110px"><el-form-item label="标题"><el-input v-model="adForm.title" /></el-form-item><el-form-item label="图片"><ImageResourcePicker :model-value="adForm.imagePath" :upload-directory="`uploads/displays/${active?.code || 'GENERAL'}`" adaptive-preview @update:model-value="value => adForm.imagePath = value || ''" /></el-form-item><el-form-item label="目标地址"><el-input v-model="adForm.url" placeholder="可选；不跳转时仍会保留" /></el-form-item><el-form-item label="点击行为"><el-select v-model="adForm.openMode" style="width:100%" data-testid="advertisement-open-mode"><el-option label="不跳转，仅展示图片" value="NO_LINK" /><el-option label="按目标默认" value="DEFAULT" /><el-option label="当前窗口" value="SAME_WINDOW" /><el-option label="新窗口" value="NEW_WINDOW" /></el-select><div v-if="adForm.openMode === 'NO_LINK'" style="margin-top:6px;color:#909399;font-size:12px">目标地址会保留；重新启用跳转后继续使用。</div></el-form-item><el-form-item label="开始时间"><el-date-picker v-model="adForm.startAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" clearable /></el-form-item><el-form-item label="结束时间"><el-date-picker v-model="adForm.endAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" clearable /></el-form-item><el-form-item label="展示顺序"><el-input-number v-model="adForm.sortOrder" /></el-form-item><el-form-item label="启用"><el-switch v-model="adForm.enabled" /></el-form-item></el-form><template #footer><el-button @click="adDialog=false">取消</el-button><el-button type="primary" :loading="saving" @click="saveAd">保存</el-button></template></el-dialog>
  </main>
</template>
