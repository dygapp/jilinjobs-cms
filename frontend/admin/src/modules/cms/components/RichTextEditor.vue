<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import suneditor, { plugins } from 'suneditor'
import zhCn from 'suneditor/langs/zh_cn'
import 'suneditor/css/editor'

interface UploadedImage {
  src: string
  alt?: string
}

type SunEditorInstance = {
  destroy: () => void
  $: {
    html: {
      get: () => string
      set: (value: string) => void
      insertHTML: (value: string) => void
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
      onChange: ({ data }) => {
        if (!applyingExternalValue) emit('update:modelValue', data)
      },
      onImageUploadBefore: async ({ info }) => {
        if (!props.uploadImage) return true
        const file = info.files?.[0]
        if (!file || !editor) return false

        try {
          const image = await props.uploadImage(file)
          if (!isSafeUrl(image.src)) return false
          const src = escapeAttribute(image.src)
          const alt = escapeAttribute(image.alt || file.name)
          editor.$.html.insertHTML(`<img src="${src}" alt="${alt}">`)
          emit('update:modelValue', editor.$.html.get())
        } catch {
          return false
        }

        return undefined
      },
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

function isSafeUrl(value: string): boolean {
  return value.startsWith('/') || /^https?:\/\//i.test(value)
}

function escapeAttribute(value: string): string {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('"', '&quot;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
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
