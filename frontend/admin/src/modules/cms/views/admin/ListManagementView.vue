<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, MoreFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdaptiveImagePreview from '../../components/AdaptiveImagePreview.vue'
import AdminIconAction from '../../components/AdminIconAction.vue'
import AdminPanelToggle from '../../components/AdminPanelToggle.vue'
import ImageResourcePicker from '../../components/ImageResourcePicker.vue'
import { contentImagePolicyLabel, contentImagePolicyOptions, type ContentImagePolicy } from '../../cmsEnums'
import { getArticle, listArticles, resourceContentUrl, uploadResource, type AdminArticleSummary, type CmsArticle } from '../../api/articles'
import {
  createCmsList, createCmsListItem, deleteCmsList, deleteCmsListItem, listCmsListItems, listCmsLists, updateCmsList, updateCmsListItem,
  type CmsListDefinition, type CmsListDraft, type CmsListItem, type CmsListItemDraft,
} from '../../api/lists'

const lists = ref<CmsListDefinition[]>([])
const items = ref<CmsListItem[]>([])
const activeId = ref<number | null>(null)
const sideCollapsed = ref(false)
const loading = ref(false)
const dialog = ref(false)
const itemDialog = ref(false)
const saving = ref(false)
const uploading = ref(false)
const articleSearching = ref(false)
const editingList = ref<number | null>(null)
const editingItem = ref<number | null>(null)
const articleOptions = ref<AdminArticleSummary[]>([])
const selectedArticle = ref<CmsArticle | null>(null)
const listForm = reactive<CmsListDraft>({ code: '', name: '', groupCode: 'GENERAL', imagePolicy: 'OPTIONAL', description: '', sortOrder: 0, enabled: true, system: false })
const itemForm = reactive<CmsListItemDraft>({ sourceType: 'LINK', articleId: null, title: '', subtitle: null, url: null, imagePath: null, imageResourceId: null, openMode: 'DEFAULT', sortOrder: 0, enabled: true, extraJson: null })
const active = computed(() => lists.value.find(item => item.id === activeId.value) || null)
const editingListModel = computed(() => editingList.value == null ? null : lists.value.find(item => item.id === editingList.value) || null)
const asItem = (row: unknown) => row as CmsListItem
const effectiveArticleImageId = computed(() => itemForm.sourceType === 'ARTICLE' ? itemForm.imageResourceId ?? selectedArticle.value?.coverResourceId ?? null : null)
const bodyImageCandidates = computed(() => selectedArticle.value?.bodyImageResourceIds ?? [])

onMounted(refresh)

async function refresh() {
  loading.value = true
  try {
    lists.value = await listCmsLists()
    if (!lists.value.some(item => item.id === activeId.value)) activeId.value = lists.value[0]?.id || null
    await refreshItems()
  } catch (error) { ElMessage.error(message(error)) }
  finally { loading.value = false }
}
async function refreshItems() { items.value = activeId.value ? await listCmsListItems(activeId.value) : [] }
async function select(id: number) { activeId.value = id; await refreshItems() }
function addList() { editingList.value = null; Object.assign(listForm, { code: '', name: '', groupCode: 'GENERAL', imagePolicy: 'OPTIONAL', description: '', sortOrder: 0, enabled: true, system: false }); dialog.value = true }
function editList(row: CmsListDefinition) { editingList.value = row.id; Object.assign(listForm, { code: row.code, name: row.name, groupCode: row.groupCode, imagePolicy: row.imagePolicy, description: row.description, sortOrder: row.sortOrder, enabled: row.enabled, system: row.system }); dialog.value = true }
async function saveList() { saving.value = true; try { const saved = editingList.value ? await updateCmsList(editingList.value, { ...listForm }) : await createCmsList({ ...listForm }); dialog.value = false; activeId.value = saved.id; await refresh() } catch (error) { ElMessage.error(message(error)) } finally { saving.value = false } }
async function removeList(row: CmsListDefinition) { try { await ElMessageBox.confirm(`删除列表“${row.name}”将同时删除列表项，是否继续？`, '删除列表', { type: 'warning' }); await deleteCmsList(row.id); await refresh() } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(message(error)) } }

async function addItem() {
  editingItem.value = null
  selectedArticle.value = null
  Object.assign(itemForm, { sourceType: 'LINK', articleId: null, title: '', subtitle: null, url: null, imagePath: null, imageResourceId: null, openMode: 'DEFAULT', sortOrder: 0, enabled: true, extraJson: null })
  articleOptions.value = []
  itemDialog.value = true
}

async function editItem(row: CmsListItem) {
  editingItem.value = row.id
  Object.assign(itemForm, {
    sourceType: row.sourceType,
    articleId: row.articleId,
    title: row.title,
    subtitle: row.subtitle,
    url: row.url,
    imagePath: row.imagePath,
    imageResourceId: row.imageResourceId,
    openMode: row.openMode,
    sortOrder: row.sortOrder,
    enabled: row.enabled,
    extraJson: row.extraJson,
  })
  selectedArticle.value = null
  articleOptions.value = []
  if (row.sourceType === 'ARTICLE' && row.articleId != null) {
    try {
      selectedArticle.value = await getArticle(row.articleId)
      articleOptions.value = [articleSummary(selectedArticle.value)]
    } catch (error) { ElMessage.error(message(error)) }
  }
  itemDialog.value = true
}

async function searchArticles(keyword: string) {
  articleSearching.value = true
  try {
    const page = await listArticles({ keyword, page: 0, size: 50 })
    articleOptions.value = page.items
  } catch (error) { ElMessage.error(message(error)) }
  finally { articleSearching.value = false }
}

async function articleChanged(articleId: number | null) {
  selectedArticle.value = null
  itemForm.imageResourceId = null
  if (articleId == null) return
  try {
    const article = await getArticle(articleId)
    selectedArticle.value = article
    if (!article.coverResourceId && article.bodyImageResourceIds.length) {
      itemForm.imageResourceId = article.bodyImageResourceIds[0]
    }
  } catch (error) { ElMessage.error(message(error)) }
}

function sourceTypeChanged() {
  if (itemForm.sourceType === 'LINK') {
    itemForm.articleId = null
    itemForm.imageResourceId = null
    selectedArticle.value = null
  } else {
    itemForm.url = null
    itemForm.imagePath = null
    if (!articleOptions.value.length) void searchArticles('')
  }
}

function useArticleCover() {
  if (!selectedArticle.value?.coverResourceId) return
  itemForm.imageResourceId = null
}

async function uploadArticleImage(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const resource = await uploadResource(file)
    if (!resource.contentType?.startsWith('image/')) {
      ElMessage.warning('请选择图片文件')
      return
    }
    itemForm.imageResourceId = resource.id
    ElMessage.success('列表展示图片已上传')
  } catch (error) { ElMessage.error(message(error)) }
  finally { input.value = ''; uploading.value = false }
}

async function saveItem() {
  if (!activeId.value || !active.value) return
  if (itemForm.sourceType === 'LINK') {
    if (!itemForm.title.trim()) { ElMessage.warning('请输入标题'); return }
    if (active.value.imagePolicy === 'REQUIRED' && !itemForm.imagePath) { ElMessage.warning('请为列表项设置图片'); return }
    if (active.value.imagePolicy === 'NONE') itemForm.imagePath = null
    itemForm.articleId = null
    itemForm.imageResourceId = null
  } else {
    if (itemForm.articleId == null || !selectedArticle.value) { ElMessage.warning('请选择关联文章'); return }
    itemForm.title = selectedArticle.value.title
    itemForm.url = null
    itemForm.imagePath = null
    if (active.value.imagePolicy === 'NONE') itemForm.imageResourceId = null
    if (active.value.imagePolicy === 'REQUIRED' && effectiveArticleImageId.value == null) {
      ElMessage.warning('当前列表要求图片；文章没有主题图片，请选择正文图片或上传新的展示图片')
      return
    }
  }
  saving.value = true
  try {
    editingItem.value ? await updateCmsListItem(activeId.value, editingItem.value, { ...itemForm }) : await createCmsListItem(activeId.value, { ...itemForm })
    itemDialog.value = false
    await refreshItems()
  } catch (error) { ElMessage.error(message(error)) }
  finally { saving.value = false }
}

async function removeItem(row: CmsListItem) { if (!activeId.value) return; try { await ElMessageBox.confirm(`确定删除列表项“${row.title}”吗？`, '删除列表项', { type: 'warning' }); await deleteCmsListItem(activeId.value, row.id); await refreshItems() } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(message(error)) } }
function legacyPolicyName(policy: ContentImagePolicy) { return policy === 'NONE' ? '不使用图片' : policy === 'REQUIRED' ? '图片必填' : '图片可选' }
function articleStatusLabel(status: string | null) { return status === 'PUBLISHED' ? '已发布' : status === 'WITHDRAWN' ? '已撤回' : '草稿' }
function articleSummary(article: CmsArticle): AdminArticleSummary { return { id: article.id, columnId: article.columnId, title: article.title, source: article.source, articleType: article.articleType, publishDate: article.publishDate, status: article.status, viewCount: article.viewCount, updatedAt: article.updatedAt } }
const message = (error: unknown) => error instanceof Error ? error.message : '操作失败'
</script>

<template>
  <main class="admin-shell">
    <header class="page-header"><div><p class="eyebrow">可复用展示数据</p><h1>列表管理</h1><p class="subtitle">列表维护链接或文章引用；图片数据要求由列表统一约束，公开页面决定最终展示方式。</p></div><el-button data-testid="add-cms-list" type="primary" @click="addList">新增列表</el-button></header>

    <div class="admin-split-layout" :class="{ 'side-panel-collapsed': sideCollapsed }">
      <el-card class="admin-side-panel" shadow="never">
        <div class="admin-side-list">
          <div v-for="list in lists" :key="list.id" class="admin-side-row" :class="{ active: activeId === list.id }" :data-testid="`cms-list-${list.code}`" @click="select(list.id)">
            <span class="admin-side-row-main">{{list.name}}<el-tag v-if="list.preset" :data-testid="`preset-cms-list-${list.code}`" size="small" type="info" style="margin-left:6px">预置</el-tag></span>
            <span @click.stop><el-dropdown trigger="click" @command="command => command === 'edit' ? editList(list) : removeList(list)"><el-button text circle aria-label="列表操作"><el-icon><MoreFilled /></el-icon></el-button><template #dropdown><el-dropdown-menu><el-dropdown-item command="edit">编辑</el-dropdown-item><el-dropdown-item command="delete" divided :disabled="list.preset">删除</el-dropdown-item></el-dropdown-menu></template></el-dropdown></span>
          </div>
        </div>
      </el-card>

      <el-card class="admin-main-panel" shadow="never">
        <template #header><div class="admin-card-header"><div class="admin-card-header-title"><AdminPanelToggle :collapsed="sideCollapsed" label="列表导航" @toggle="sideCollapsed = !sideCollapsed" /><div><strong>{{active?.name || '请选择列表'}}</strong><span v-if="active" style="margin-left:8px;color:#909399">{{active.code}}</span><el-tag v-if="active?.preset" data-testid="active-list-preset" size="small" type="info" style="margin-left:8px">预置</el-tag><el-tag v-if="active" data-testid="active-list-image-requirement" size="small" style="margin-left:8px">图片：{{contentImagePolicyLabel(active.imagePolicy)}}</el-tag><span v-if="active" data-testid="active-list-image-policy" style="display:none">{{legacyPolicyName(active.imagePolicy)}}</span></div></div><el-button data-testid="add-cms-list-item" type="primary" :disabled="!active" @click="addItem">新增列表项</el-button></div></template>
        <el-table v-loading="loading" :data="items" row-key="id" data-testid="cms-list-item-table">
          <el-table-column label="类型" width="90"><template #default="scope"><el-tag size="small" :type="asItem(scope.row).sourceType === 'ARTICLE' ? 'success' : 'info'">{{asItem(scope.row).sourceType === 'ARTICLE' ? '文章' : '链接'}}</el-tag></template></el-table-column>
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="目标" min-width="220" show-overflow-tooltip><template #default="scope"><span v-if="asItem(scope.row).sourceType === 'ARTICLE'">文章 #{{asItem(scope.row).articleId}} · {{articleStatusLabel(asItem(scope.row).articleStatus)}}</span><span v-else>{{asItem(scope.row).url || '—'}}</span></template></el-table-column>
          <el-table-column v-if="active?.imagePolicy !== 'NONE'" label="图片" min-width="220"><template #default="scope"><div v-if="asItem(scope.row).sourceType === 'LINK' && asItem(scope.row).imagePath" style="display:flex;align-items:center;gap:10px"><AdaptiveImagePreview :src="asItem(scope.row).imagePath || ''" :alt="asItem(scope.row).title" adaptive style="width:68px;height:42px;flex:none" /><code style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{asItem(scope.row).imagePath}}</code></div><div v-else-if="asItem(scope.row).sourceType === 'ARTICLE' && asItem(scope.row).effectiveImageResourceId" style="display:flex;align-items:center;gap:10px"><AdaptiveImagePreview :src="resourceContentUrl(asItem(scope.row).effectiveImageResourceId!)" :alt="asItem(scope.row).title" adaptive style="width:68px;height:42px;flex:none" /><span>{{asItem(scope.row).imageResourceId ? '列表覆盖图片' : '继承文章主题图片'}}</span></div><span v-else>—</span></template></el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="状态" width="80"><template #default="scope">{{asItem(scope.row).enabled ? '启用' : '停用'}}</template></el-table-column>
          <el-table-column label="操作" width="92" fixed="right"><template #default="scope"><div class="admin-table-actions"><AdminIconAction label="编辑" :icon="Edit" @click="editItem(asItem(scope.row))" /><AdminIconAction label="删除" :icon="Delete" type="danger" @click="removeItem(asItem(scope.row))" /></div></template></el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="dialog" :title="editingList ? '编辑列表' : '新增列表'" width="580px"><el-form label-width="110px"><el-form-item label="Code"><el-input v-model="listForm.code" :disabled="Boolean(editingList)" /><div v-if="editingListModel?.preset" data-testid="preset-list-code-hint" style="color:#909399;font-size:12px">预置列表的 Code 是稳定站点身份，不允许修改。</div></el-form-item><el-form-item label="名称"><el-input v-model="listForm.name" /></el-form-item><el-form-item label="分组"><el-input v-model="listForm.groupCode" /></el-form-item><el-form-item label="图片要求"><el-select v-model="listForm.imagePolicy" data-testid="list-image-policy" style="width:100%"><el-option v-for="option in contentImagePolicyOptions" :key="option.value" :label="option.label" :value="option.value" /></el-select><div style="color:#909399;font-size:12px">约束列表项是否需要图片，不决定公开页面如何展示。</div></el-form-item><el-form-item label="说明"><el-input v-model="listForm.description" type="textarea" /></el-form-item><el-form-item label="排序"><el-input-number v-model="listForm.sortOrder" /></el-form-item><el-form-item label="启用"><el-switch v-model="listForm.enabled" /></el-form-item></el-form><template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" :loading="saving" @click="saveList">保存</el-button></template></el-dialog>

    <el-dialog v-model="itemDialog" :title="editingItem ? '编辑列表项' : '新增列表项'" width="760px">
      <el-form label-width="110px">
        <el-form-item label="数据类型" required><el-radio-group v-model="itemForm.sourceType" data-testid="list-item-source-type" @change="sourceTypeChanged"><el-radio-button value="LINK">链接</el-radio-button><el-radio-button value="ARTICLE">文章</el-radio-button></el-radio-group></el-form-item>

        <template v-if="itemForm.sourceType === 'LINK'">
          <el-form-item label="标题" required><el-input v-model="itemForm.title" /><div style="color:#909399;font-size:12px">标题作为后台识别名称保留；前台是否显示由具体页面设计决定。</div></el-form-item>
          <el-form-item label="副标题"><el-input v-model="itemForm.subtitle" /></el-form-item>
          <el-form-item v-if="active?.imagePolicy !== 'NONE'" label="图片" :required="active?.imagePolicy === 'REQUIRED'"><ImageResourcePicker v-model="itemForm.imagePath" :upload-directory="`uploads/lists/${active?.code || 'GENERAL'}`" adaptive-preview /></el-form-item>
          <el-form-item label="目标地址"><el-input v-model="itemForm.url" placeholder="可选；是否需要链接由具体页面使用场景决定" /></el-form-item>
        </template>

        <template v-else>
          <el-form-item label="关联文章" required>
            <el-select v-model="itemForm.articleId" data-testid="list-item-article" filterable remote clearable :remote-method="searchArticles" :loading="articleSearching" placeholder="输入标题搜索文章" style="width:100%" @change="articleChanged">
              <el-option v-for="article in articleOptions" :key="article.id" :label="`${article.title}（${articleStatusLabel(article.status)}）`" :value="article.id" />
            </el-select>
            <div style="color:#909399;font-size:12px">文章仍只属于原栏目；加入列表只是展示投放，不改变栏目和面包屑。</div>
          </el-form-item>
          <el-alert v-if="selectedArticle" :title="`当前文章：${selectedArticle.title}（${articleStatusLabel(selectedArticle.status)}）`" type="info" :closable="false" show-icon />
          <el-form-item label="副标题"><el-input v-model="itemForm.subtitle" /></el-form-item>

          <el-form-item v-if="active?.imagePolicy !== 'NONE'" label="展示图片" :required="active?.imagePolicy === 'REQUIRED'">
            <div v-if="selectedArticle" style="display:grid;gap:10px;width:100%">
              <div v-if="selectedArticle.coverResourceId" style="display:flex;align-items:center;gap:10px"><AdaptiveImagePreview :src="resourceContentUrl(selectedArticle.coverResourceId)" :alt="selectedArticle.title" adaptive style="width:120px;height:74px" /><el-button :type="itemForm.imageResourceId == null ? 'primary' : 'default'" @click="useArticleCover">使用文章主题图片</el-button></div>
              <div v-if="bodyImageCandidates.length"><div style="margin-bottom:6px;color:#606266">正文图片候选</div><div style="display:flex;gap:8px;flex-wrap:wrap"><button v-for="resourceId in bodyImageCandidates" :key="resourceId" type="button" :aria-label="`使用正文图片 ${resourceId}`" :style="{padding:'3px',border:itemForm.imageResourceId===resourceId?'2px solid var(--el-color-primary)':'1px solid #dcdfe6',background:'#fff',cursor:'pointer'}" @click="itemForm.imageResourceId=resourceId"><AdaptiveImagePreview :src="resourceContentUrl(resourceId)" :alt="`正文图片 ${resourceId}`" adaptive style="width:90px;height:58px" /></button></div></div>
              <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap"><label class="upload-button"><span>{{uploading ? '上传中…' : '上传其他展示图片'}}</span><input type="file" accept="image/*" :disabled="uploading" @change="uploadArticleImage"></label></div>
              <div v-if="effectiveArticleImageId" style="display:flex;align-items:center;gap:10px"><span>当前有效图片：</span><AdaptiveImagePreview :src="resourceContentUrl(effectiveArticleImageId)" :alt="selectedArticle.title" adaptive style="width:120px;height:74px" /><small>{{itemForm.imageResourceId ? '已固化列表覆盖图片' : '继承文章主题图片'}}</small></div>
              <el-alert v-else-if="active?.imagePolicy === 'REQUIRED'" title="当前文章没有主题图片；请选择正文图片或上传新的展示图片。" type="warning" :closable="false" show-icon />
            </div>
            <span v-else style="color:#909399">选择文章后显示可用图片。</span>
          </el-form-item>
        </template>

        <el-form-item label="打开方式"><el-select v-model="itemForm.openMode" style="width:100%"><el-option label="按目标默认" value="DEFAULT" /><el-option label="当前窗口" value="SAME_WINDOW" /><el-option label="新窗口" value="NEW_WINDOW" /></el-select></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="itemForm.sortOrder" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="itemForm.enabled" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="itemDialog=false">取消</el-button><el-button type="primary" :loading="saving" :disabled="uploading" @click="saveItem">保存</el-button></template>
    </el-dialog>
  </main>
</template>