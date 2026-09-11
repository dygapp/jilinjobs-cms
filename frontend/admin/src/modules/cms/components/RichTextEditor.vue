<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import suneditor, { plugins } from 'suneditor'
import zhCn from 'suneditor/langs/zh_cn'
import 'suneditor/dist/css/suneditor.min.css'

interface UploadedImage {
  src: string
  alt?: string
}

type SunUploadHandler = (response?: unknown) => void

type SunEditorInstance = {
  destroy: () => void
  $: {
    html: {
      get: () => string
      set: (value: string) => void
    }
  }
}

const props = withDefaults(defineProps<{
  modelValue: string
  testId: string
  uploadImage?: (file: File) => Promise<UploadedImage>
  uploading?: boolean
}>(), {
  modelValue: '',
  uploading: false,
})

const emit = defineEmits<{ (event: 'update:modelValue', value: string): void }>()
const target = ref<HTMLTextAreaElement | null>(null)
let editor: SunEditorInstance | null = null
let applyingExternalValue = false

function onImageUploadBefore(...args: any[]) {
  if (!props.uploadImage) return true
  const files = args[0] as File[] | undefined
  const uploadHandler = args.at(-1) as SunUploadHandler | undefined
  const file = files?.[0]
  if (!file || typeof uploadHandler !== 'function') return false
  void uploadManagedImage(file, uploadHandler)
  return undefined
}

onMounted(() => {
  if (!target.value) return

  editor = suneditor.create(target.value, {
    value: props.modelValue,
    plugins,
    lang: zhCn,
    minHeight: '260px',
    width: '100%',
    buttonList: [
      ['undo', 'redo'],
      ['blockStyle', 'bold', 'underline', 'italic', 'strike'],
      ['font', 'fontSize', 'fontColor', 'backgroundColor'],
      ['align', 'list', 'blockquote', 'hr'],
      ['table', 'link', 'image'],
      ['removeFormat', 'codeView', 'fullScreen'],
    ],
    image: {
      allowMultiple: false,
      createFileInput: Boolean(props.uploadImage),
      createUrlInput: !props.uploadImage,
    },
    attributeWhitelist: { table: 'align|cellpadding|cellspacing' },
    tagStyles: { td: 'width|height' },
    events: {
      onChange: contents => {
        if (!applyingExternalValue) emit('update:modelValue', contents)
      },
      onImageUploadBefore,
    },
  }) as unknown as SunEditorInstance
})

watch(() => props.modelValue, value => {
  if (!editor) return
  const normalized = value || ''
  if (editor.$.html.get() === normalized) return
  applyingExternalValue = true
  try {
    editor.$.html.set(normalized)
  } finally {
    applyingExternalValue = false
  }
})

onBeforeUnmount(() => {
  editor?.destroy()
  editor = null
})

async function uploadManagedImage(file: File, uploadHandler: SunUploadHandler) {
  try {
    const image = await props.uploadImage!(file)
    uploadHandler({
      result: [{
        url: image.src,
        name: image.alt || file.name,
        size: file.size,
      }],
    })
  } catch (error) {
    uploadHandler(error instanceof Error ? error.message : '图片上传失败')
  }
}
</script>

<template>
  <div class="rich-text-editor-shell" :class="{ 'is-uploading': uploading }" :data-testid="testId">
    <textarea ref="target" />
  </div>
</template>

<style scoped>
.rich-text-editor-shell { width: 100%; min-width: 0; }
.rich-text-editor-shell :deep(.sun-editor) { border-color: #dcdfe6; border-radius: 4px; }
.rich-text-editor-shell :deep(.se-toolbar) { border-bottom-color: #ebeef5; background: #f8f9fb; }
.rich-text-editor-shell :deep(.se-wrapper-wysiwyg) { line-height: 1.75; word-break: break-word; }
.rich-text-editor-shell.is-uploading :deep(.se-btn[data-command='image']) { opacity: 0.55; pointer-events: none; }
</style>
