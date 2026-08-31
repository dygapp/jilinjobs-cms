<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MoreFilled } from '@element-plus/icons-vue'
import AdaptiveImagePreview from '../../components/AdaptiveImagePreview.vue'
import ImageResourcePicker from '../../components/ImageResourcePicker.vue'
import { navigationIconCatalog } from '../../iconCatalog'
import { listColumns, type CmsColumn } from '../../api/columns'
import { listPages, type CmsPage } from '../../api/pages'
import {
  createNavigation, createNavigationLocation, deleteNavigation, deleteNavigationLocation,
  listNavigationLocations, listNavigations, updateNavigation, updateNavigationLocation,
  type CmsNavigation, type NavigationDraft, type NavigationLocation, type NavigationLocationDraft, type NavigationTargetType,
} from '../../api/navigation'

const items = ref<CmsNavigation[]>([])
const columns = ref<CmsColumn[]>([])
const pages = ref<CmsPage[]>([])
const locations = ref<NavigationLocation[]>([])
const activePosition = ref('MAIN')
const loading = ref(false)
const dialogVisible = ref(false)
const locationDialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number|null>(null)
const editingLocationCode = ref<string|null>(null)
const form = reactive<NavigationDraft>({name:'',position:'MAIN',category:null,targetType:'COLUMN',targetColumnId:null,targetPageId:null,targetUrl:null,parentId:null,openMode:'DEFAULT',iconPath:null,sortOrder:0,enabled:true})
const locationForm = reactive<NavigationLocationDraft>({code:'',name:'',description:'',sortOrder:0,enabled:true,system:false})
const targetOptions:Array<{value:NavigationTargetType;label:string}>=[{value:'HOME',label:'网站首页'},{value:'COLUMN',label:'本站栏目'},{value:'PAGE',label:'单页'},{value:'LINK',label:'链接地址'},{value:'PLACEHOLDER',label:'占位菜单'}]
const asNavigation=(row:unknown)=>row as CmsNavigation

const currentLocation = computed(()=>locations.value.find(i=>i.code===activePosition.value))
const currentItems = computed(()=>items.value.filter(i=>i.position===activePosition.value))
const parentOptions = computed(()=>currentItems.value.filter(i=>i.id!==editingId.value))
const treeData = computed(()=>{
  const map = new Map<number,CmsNavigation & {children?:CmsNavigation[]}>()
  currentItems.value.forEach(item=>map.set(item.id,{...item,children:[]}))
  const roots:Array<CmsNavigation & {children?:CmsNavigation[]}> = []
  map.forEach(item=>{
    if(item.parentId && map.has(item.parentId)) map.get(item.parentId)!.children!.push(item)
    else roots.push(item)
  })
  return roots
})

onMounted(refresh)
async function refresh(){loading.value=true;try{const [nav,cols,pgs,locs]=await Promise.all([listNavigations(),listColumns(),listPages(),listNavigationLocations()]);items.value=nav;columns.value=cols;pages.value=pgs;locations.value=locs;if(!locs.some(i=>i.code===activePosition.value))activePosition.value=locs[0]?.code||'MAIN'}catch(e){ElMessage.error(msg(e))}finally{loading.value=false}}
function selectLocation(code:string){activePosition.value=code}
function openCreate(){editingId.value=null;Object.assign(form,{name:'',position:activePosition.value,category:null,targetType:'COLUMN',targetColumnId:null,targetPageId:null,targetUrl:null,parentId:null,openMode:'DEFAULT',iconPath:null,sortOrder:0,enabled:true});dialogVisible.value=true}
function openEdit(row:CmsNavigation){editingId.value=row.id;Object.assign(form,{name:row.name,position:row.position,category:row.category,targetType:row.targetType,targetColumnId:row.targetColumnId,targetPageId:row.targetPageId,targetUrl:row.targetUrl,parentId:row.parentId,openMode:row.openMode,iconPath:row.iconPath,sortOrder:row.sortOrder,enabled:row.enabled});dialogVisible.value=true}
async function save(){saving.value=true;try{form.position=activePosition.value;editingId.value==null?await createNavigation({...form}):await updateNavigation(editingId.value,{...form});dialogVisible.value=false;await refresh()}catch(e){ElMessage.error(msg(e))}finally{saving.value=false}}
async function toggle(row:CmsNavigation,enabled:boolean){try{await updateNavigation(row.id,{name:row.name,position:row.position,category:row.category,targetType:row.targetType,targetColumnId:row.targetColumnId,targetPageId:row.targetPageId,targetUrl:row.targetUrl,parentId:row.parentId,openMode:row.openMode,iconPath:row.iconPath,sortOrder:row.sortOrder,enabled});await refresh()}catch(e){ElMessage.error(msg(e));await refresh()}}
async function remove(row:CmsNavigation){try{await ElMessageBox.confirm(`确定删除导航“${row.name}”吗？`,'删除导航',{type:'warning'});await deleteNavigation(row.id);await refresh()}catch(e){if(e!=='cancel'&&e!=='close')ElMessage.error(msg(e))}}
function openCreateLocation(){editingLocationCode.value=null;Object.assign(locationForm,{code:'',name:'',description:'',sortOrder:0,enabled:true,system:false});locationDialogVisible.value=true}
function openEditLocation(row:NavigationLocation){editingLocationCode.value=row.code;Object.assign(locationForm,{code:row.code,name:row.name,description:row.description,sortOrder:row.sortOrder,enabled:row.enabled,system:row.system});locationDialogVisible.value=true}
async function saveLocation(){saving.value=true;try{const saved=editingLocationCode.value?await updateNavigationLocation(editingLocationCode.value,{...locationForm}):await createNavigationLocation({...locationForm});locationDialogVisible.value=false;activePosition.value=saved.code;await refresh()}catch(e){ElMessage.error(msg(e))}finally{saving.value=false}}
async function removeLocation(row:NavigationLocation){try{await ElMessageBox.confirm(`确定删除导航位置“${row.name}”吗？仅空位置可以删除。`,'删除导航位置',{type:'warning'});await deleteNavigationLocation(row.code);await refresh()}catch(e){if(e!=='cancel'&&e!=='close')ElMessage.error(msg(e))}}
function target(row:CmsNavigation){if(row.targetType==='HOME')return'网站首页';if(row.targetType==='PLACEHOLDER')return'占位';if(row.targetType==='LINK')return row.targetUrl||'-';if(row.targetType==='PAGE')return pages.value.find(p=>p.id===row.targetPageId)?.name||`单页 #${row.targetPageId}`;return columns.value.find(c=>c.id===row.targetColumnId)?.name||`栏目 #${row.targetColumnId}`}
const msg=(e:unknown)=>e instanceof Error?e.message:'操作失败'
</script>

<template>
  <main class="admin-shell">
    <header class="page-header"><div><p class="eyebrow">内容结构</p><h1>导航管理</h1><p class="subtitle">先选择导航位置，再以树形结构维护多级菜单；预置位置和导航属于网站规划基线，不可删除。</p></div><el-button data-testid="add-navigation" type="primary" :disabled="!currentLocation" @click="openCreate">新增导航</el-button></header>
    <div style="display:grid;grid-template-columns:230px minmax(0,1fr);gap:16px;align-items:start">
      <el-card shadow="never">
        <template #header><div style="display:flex;align-items:center;justify-content:space-between"><strong>导航位置</strong><el-button link type="primary" data-testid="add-navigation-location" @click="openCreateLocation">新增</el-button></div></template>
        <div style="display:flex;flex-direction:column">
          <div v-for="location in locations" :key="location.code" :data-testid="`navigation-location-${location.code}`" :style="{display:'flex',alignItems:'center',minHeight:'44px',padding:'0 6px 0 12px',borderRadius:'6px',cursor:'pointer',background:activePosition===location.code?'var(--el-fill-color-light)':'transparent'}" @click="selectLocation(location.code)">
            <span style="flex:1;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{location.name}}<el-tag v-if="location.preset" :data-testid="`preset-navigation-location-${location.code}`" size="small" type="info" style="margin-left:6px">预置</el-tag></span>
            <span @click.stop><el-dropdown trigger="click" @command="command=>command==='edit'?openEditLocation(location):removeLocation(location)"><el-button text circle aria-label="导航位置操作"><el-icon><MoreFilled/></el-icon></el-button><template #dropdown><el-dropdown-menu><el-dropdown-item command="edit">编辑</el-dropdown-item><el-dropdown-item v-if="!location.preset" command="delete" divided>删除</el-dropdown-item></el-dropdown-menu></template></el-dropdown></span>
          </div>
        </div>
      </el-card>
      <el-card shadow="never">
        <template #header><div><strong>{{currentLocation?.name||'请选择导航位置'}}</strong><span v-if="currentLocation" style="margin-left:8px;color:#909399">{{currentLocation.code}}</span><el-tag v-if="currentLocation?.preset" size="small" type="info" style="margin-left:8px">预置</el-tag></div></template>
        <el-table v-loading="loading" :data="treeData" row-key="id" default-expand-all :tree-props="{children:'children'}" data-testid="navigation-tree-table">
          <el-table-column label="导航名称" min-width="180"><template #default="s"><span>{{asNavigation(s.row).name}}</span><el-tag v-if="asNavigation(s.row).preset" :data-testid="`preset-navigation-${asNavigation(s.row).id}`" size="small" type="info" style="margin-left:8px">预置</el-tag></template></el-table-column>
          <el-table-column label="图标" width="76"><template #default="s"><AdaptiveImagePreview v-if="asNavigation(s.row).iconPath" :src="asNavigation(s.row).iconPath || ''" :adaptive="true" alt="" style="width:32px;height:32px" /></template></el-table-column>
          <el-table-column label="目标" min-width="220"><template #default="s">{{target(asNavigation(s.row))}}</template></el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="80"/>
          <el-table-column label="状态" width="100"><template #default="s"><el-switch :model-value="s.row.enabled" @change="v=>toggle(asNavigation(s.row),v===true)"/></template></el-table-column>
          <el-table-column label="操作" width="150"><template #default="s"><el-button link type="primary" @click="openEdit(asNavigation(s.row))">编辑</el-button><el-button v-if="!asNavigation(s.row).preset" link type="danger" @click="remove(asNavigation(s.row))">删除</el-button></template></el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId==null?'新增导航':'编辑导航'" width="680px">
      <el-form label-width="100px">
        <el-form-item label="导航位置"><el-input :model-value="currentLocation?.name||activePosition" disabled/></el-form-item>
        <el-form-item label="导航名称"><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="图标"><ImageResourcePicker v-model="form.iconPath" upload-directory="uploads/navigation-icons" :preset-options="navigationIconCatalog" adaptive-preview/></el-form-item>
        <el-form-item label="上级菜单"><el-select v-model="form.parentId" clearable style="width:100%"><el-option v-for="i in parentOptions" :key="i.id" :label="i.name" :value="i.id"/></el-select></el-form-item>
        <el-form-item label="目标类型"><el-select v-model="form.targetType" style="width:100%"><el-option v-for="o in targetOptions" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item v-if="form.targetType==='COLUMN'" label="目标栏目"><el-select data-testid="navigation-column-select" v-model="form.targetColumnId" filterable style="width:100%"><el-option v-for="c in columns" :key="c.id" :label="c.name" :value="c.id"/></el-select></el-form-item>
        <el-form-item v-if="form.targetType==='PAGE'" label="目标单页"><el-select v-model="form.targetPageId" filterable style="width:100%"><el-option v-for="p in pages" :key="p.id" :label="p.name" :value="p.id"/></el-select></el-form-item>
        <el-form-item v-if="form.targetType==='LINK'" label="目标地址"><el-input v-model="form.targetUrl" placeholder="站内路径或 HTTP(S) 地址"/></el-form-item>
        <el-form-item label="打开方式"><el-select v-model="form.openMode" style="width:100%"><el-option label="按目标默认" value="DEFAULT"/><el-option label="当前窗口" value="SAME_WINDOW"/><el-option label="新窗口" value="NEW_WINDOW"/></el-select></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder"/></el-form-item><el-form-item label="状态"><el-switch v-model="form.enabled"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button data-testid="save-navigation" type="primary" :loading="saving" @click="save">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="locationDialogVisible" :title="editingLocationCode?'编辑导航位置':'新增导航位置'" width="560px">
      <el-form label-width="100px"><el-form-item label="Code"><el-input v-model="locationForm.code" :disabled="Boolean(editingLocationCode)" placeholder="例如 FOOTER"/><div v-if="editingLocationCode&&currentLocation?.preset" data-testid="preset-navigation-location-code-hint" style="color:#909399;font-size:12px">预置导航位置的 Code 是稳定站点身份，不允许修改。</div></el-form-item><el-form-item label="名称"><el-input v-model="locationForm.name"/></el-form-item><el-form-item label="说明"><el-input v-model="locationForm.description" type="textarea"/></el-form-item><el-form-item label="排序"><el-input-number v-model="locationForm.sortOrder"/></el-form-item><el-form-item label="启用"><el-switch v-model="locationForm.enabled"/></el-form-item></el-form>
      <template #footer><el-button @click="locationDialogVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="saveLocation">保存</el-button></template>
    </el-dialog>
  </main>
</template>
