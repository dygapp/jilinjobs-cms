<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listSiteConfig, updateSiteConfig, type SiteConfigItem } from '../../api/siteConfig'

const items = ref<SiteConfigItem[]>([])
const loading = ref(false)
const saving = ref('')
const asConfig = (row: unknown) => row as SiteConfigItem

onMounted(refresh)

async function refresh() {
  loading.value = true
  try {
    items.value = await listSiteConfig()
  } catch (error) {
    ElMessage.error(message(error))
  } finally {
    loading.value = false
  }
}

async function save(item: SiteConfigItem) {
  if (item.valueType === 'JSON') {
    try {
      const parsed = JSON.parse(item.value)
      if (parsed == null || (typeof parsed !== 'object')) throw new Error('root')
    } catch {
      ElMessage.warning('JSON 配置格式不正确，请修正后再保存')
      return
    }
  }
  if (item.valueType === 'RESOURCE_PATH' && item.value.trim() && !item.value.trim().startsWith('/static/')) {
    ElMessage.warning('静态资源配置必须使用 /static/ 路径')
    return
  }
  saving.value = item.key
  try {
    await updateSiteConfig(item.key, item.value)
    ElMessage.success('网站配置已保存')
  } catch (error) {
    ElMessage.error(message(error))
  } finally {
    saving.value = ''
  }
}

function formatJson(item: SiteConfigItem) {
  try {
    item.value = JSON.stringify(JSON.parse(item.value), null, 2)
  } catch {
    ElMessage.warning('当前内容不是合法 JSON')
  }
}

function typeName(type: string) {
  if (type === 'JSON') return '结构化 JSON'
  if (type === 'RESOURCE_PATH') return '静态资源路径'
  return '文本'
}

function message(error: unknown) {
  return error instanceof Error ? error.message : '操作失败'
}
</script>

<template>
  <main class="admin-shell">
    <header class="page-header">
      <div>
        <p class="eyebrow">站点级配置</p>
        <h1>网站配置管理</h1>
        <p class="subtitle">只维护系统预定义的网站公共配置；JSON 与静态资源路径会在前后端双重校验。</p>
      </div>
    </header>

    <el-alert title="这些配置会直接影响公开站。静态资源路径使用 /static/ 开头；JSON 保存前必须通过真实语法解析。" type="info" :closable="false" show-icon />

    <el-card shadow="never" style="margin-top:16px">
      <el-table v-loading="loading" :data="items" row-key="key" data-testid="site-config-table">
        <el-table-column prop="description" label="配置项" min-width="190" />
        <el-table-column prop="key" label="Key" min-width="220" />
        <el-table-column label="类型" width="130">
          <template #default="scope"><span class="config-type">{{ typeName(scope.row.valueType) }}</span></template>
        </el-table-column>
        <el-table-column label="配置值" min-width="380">
          <template #default="scope">
            <el-input
              v-model="scope.row.value"
              :data-testid="`site-config-${scope.row.key}`"
              :type="scope.row.valueType === 'JSON' ? 'textarea' : 'text'"
              :rows="scope.row.valueType === 'JSON' ? 5 : undefined"
              :placeholder="scope.row.valueType === 'RESOURCE_PATH' ? '/static/...' : '请输入配置值'"
            />
            <el-button v-if="scope.row.valueType === 'JSON'" link type="primary" style="margin-top:6px" @click="formatJson(asConfig(scope.row))">格式化 JSON</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="scope">
            <el-button :data-testid="`save-site-config-${scope.row.key}`" type="primary" link :loading="saving === scope.row.key" @click="save(asConfig(scope.row))">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </main>
</template>
