<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listStaticResources, uploadStaticResource } from '../api/staticResources'
import AdaptiveImagePreview from './AdaptiveImagePreview.vue'

interface ImageResourceOption { label: string; path: string }

const props = withDefaults(defineProps<{
  modelValue?: string | null
  uploadDirectory: string
  presetOptions?: ImageResourceOption[]
  disabled?: boolean
  adaptivePreview?: boolean
}>(), {
  modelValue: null,
  presetOptions: () => [],
  disabled: false,
  adaptivePreview: true,
})
const emit = defineEmits<{ (event: 'update:modelValue', value: string | null): void }>()

const dialogVisible = ref(false)
const loading = ref(false)
const uploading = ref(false)
const choices = ref<ImageResourceOption[]>([])
const current = computed(() => props.modelValue?.trim() || '')
const imageExtensions = new Set(['png', 'jpg', 'jpeg', 'gif', 'webp', 'ico'])

function extension(name: string) {
  return name.includes('.') ? name.split('.').pop()!.toLowerCase() : ''
}

function generatedName(file: File) {
  const ext = extension(file.name)
  return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}.${ext}`
}

async function upload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!imageExtensions.has(extension(file.name))) {
    ElMessage.warning('请选择 PNG、JPG、GIF、WEBP 或 ICO 图片')
    return
  }
  uploading.value = true
  try {
    const path = `${props.uploadDirectory.replace(/^\/+|\/+$/g, '')}/${generatedName(file)}`
    const saved = await uploadStaticResource(path, file, false)
    emit('update:modelValue', `/static/${saved.path}`)
    ElMessage.success('图片已上传并选中')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '图片上传失败')
  } finally {
    uploading.value = false
  }
}

async function collectRuntimeImages(path: string, depth = 0): Promise<ImageResourceOption[]> {
  if (depth > 6) return []
  let entries
  try {
    entries = await listStaticResources(path)
  } catch {
    return []
  }
  const result: ImageResourceOption[] = []
  for (const entry of entries) {
    if (entry.directory) {
      result.push(...await collectRuntimeImages(entry.path, depth + 1))
    } else if (imageExtensions.has(extension(entry.name))) {
      result.push({ label: entry.path, path: `/static/${entry.path}` })
    }
  }
  return result
}

async function openLibrary() {
  loading.value = true
  dialogVisible.value = true
  const map = new Map<string, ImageResourceOption>()
  props.presetOptions.forEach(item => map.set(item.path, item))
  const runtimeImages = await collectRuntimeImages('uploads')
  runtimeImages.forEach(item => map.set(item.path, item))
  choices.value = [...map.values()]
  loading.value = false
}

function select(path: string) {
  emit('update:modelValue', path)
  dialogVisible.value = false
}
</script>

<template>
  <div class="image-resource-picker" data-testid="image-resource-picker">
    <div v-if="current" style="display:flex;align-items:center;gap:12px;margin-bottom:8px">
      <AdaptiveImagePreview :src="current" alt="当前图片" :adaptive="adaptivePreview" style="width:96px;height:60px;flex:none" />
      <el-input :model-value="current" readonly />
    </div>
    <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap">
      <label>
        <input data-testid="image-resource-upload" type="file" accept=".png,.jpg,.jpeg,.gif,.webp,.ico" :disabled="disabled || uploading" style="max-width:230px" @change="upload">
      </label>
      <el-button :disabled="disabled" @click="openLibrary">选择已有图片</el-button>
      <el-button v-if="current" :disabled="disabled" @click="emit('update:modelValue', null)">清除</el-button>
    </div>
    <p style="margin:6px 0 0;color:#909399;font-size:12px">新上传文件自动保存到 /static/{{ uploadDirectory }}/；“选择已有图片”可复用 /static/uploads/** 共享 Runtime 图片。</p>

    <el-dialog v-model="dialogVisible" title="选择已有图片" width="720px" append-to-body>
      <div v-loading="loading" style="display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;min-height:120px">
        <button v-for="item in choices" :key="item.path" type="button" style="border:1px solid #dcdfe6;background:#fff;padding:8px;border-radius:6px;cursor:pointer" @click="select(item.path)">
          <AdaptiveImagePreview :src="item.path" :alt="item.label" :adaptive="adaptivePreview" :preview="false" style="width:100%;height:84px" />
          <span style="display:block;margin-top:6px;font-size:12px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ item.label }}</span>
        </button>
        <el-empty v-if="!loading && choices.length===0" description="当前没有可选图片" style="grid-column:1/-1" />
      </div>
    </el-dialog>
  </div>
</template>
