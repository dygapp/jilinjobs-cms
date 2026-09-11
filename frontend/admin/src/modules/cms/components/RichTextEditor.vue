<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import suneditor, { plugins } from 'suneditor'
import zhCn from 'suneditor/langs/zh_cn'
import 'suneditor/css/editor'

interface UploadedImage {
  src: string
  alt?: string
}

type LegacyImageAlignment = 'none' | 'left' | 'center' | 'right'

type LegacyImageState = {
  width: string
  height: string
  alignment: LegacyImageAlignment
}

type LegacyTableState = {
  width: string
}

type LegacyTableCellState = {
  width: string
  height: string
}

type FigureInfo = {
  container?: Element | null
}

type SunEditorManagedImageInfo = {
  url: string
  files: { name: string; size: number }
  element?: HTMLImageElement | null
  anchor?: Node | null
  inputWidth: string
  inputHeight: string
  align: string
  isUpdate: boolean
  alt: string
}

type SunEditorImagePlugin = {
  figure?: {
    open: (target: HTMLImageElement, options: {
      nonResizing: boolean
      nonSizeInfo: boolean
      nonBorder: boolean
      figureTarget: boolean
      infoOnly: boolean
    }) => FigureInfo | undefined
  }
  sizeService?: {
    applySize: (width: string, height: string) => void
  }
  uploadService?: {
    urlUpload: (info: SunEditorManagedImageInfo) => void
  }
}

type SunEditorInstance = {
  destroy: () => void
  $: {
    html: {
      get: () => string
      set: (value: string) => void
    }
    plugins?: {
      image?: SunEditorImagePlugin
    }
    pluginManager?: {
      checkFileInfo?: (loaded: boolean) => void
    }
    frameContext?: {
      get?: (key: string) => HTMLElement | null
    }
  }
}

const LEGACY_IMAGE_INDEX_ATTRIBUTE = 'data-jilinjobs-legacy-image-index'
const DEFAULT_EDITOR_FONT_STACK = '"Microsoft YaHei","PingFang SC",Arial,sans-serif'
const EDITOR_FONT_ITEMS = [
  'Microsoft YaHei',
  'SimSun',
  'KaiTi',
  'FangSong',
  'PingFang SC',
  'Noto Sans CJK SC',
  'Source Han Sans SC',
  'Arial',
  'Times New Roman',
]
const IMAGE_FLOAT_CLASSES = [
  '__se__float-none',
  '__se__float-left',
  '__se__float-center',
  '__se__float-right',
] as const

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
let pendingLegacyImageStates: LegacyImageState[] = []
let pendingLegacyTableStates: LegacyTableState[] = []
let pendingLegacyTableCellStates: LegacyTableCellState[] = []
const boundLegacyImageLoads = new WeakSet<HTMLImageElement>()

onMounted(() => {
  if (!target.value) return

  readLegacyPresentationStates(props.modelValue)
  const initialValue = prepareLegacyImagesForEditor(props.modelValue)

  editor = suneditor.create(target.value, {
    value: initialValue,
    plugins,
    lang: zhCn,
    minHeight: '260px',
    width: '100%',
    editorStyle: `font-family:${DEFAULT_EDITOR_FONT_STACK};`,
    buttonList: [
      ['undo', 'redo'],
      ['blockStyle', 'bold', 'underline', 'italic', 'strike'],
      ['font', 'fontSize', 'fontColor', 'backgroundColor'],
      ['align', 'list', 'blockquote', 'hr'],
      ['table', 'link', 'image'],
      ['removeFormat', 'codeView', 'fullScreen'],
    ],
    font: {
      items: EDITOR_FONT_ITEMS,
    },
    image: {
      allowMultiple: false,
      createFileInput: Boolean(props.uploadImage),
      createUrlInput: !props.uploadImage,
    },
    attributeWhitelist: {
      table: 'align|cellpadding|cellspacing',
      img: 'width|height|align',
    },
    tagStyles: {
      table: 'width',
      td: 'width|height',
      img: 'width|height|float',
    },
    events: {
      onChange: ({ data }) => {
        if (applyingExternalValue) return
        const value = normalizeLegacyImagesForApp(data)
        if (value !== props.modelValue) emit('update:modelValue', value)
      },
      onImageLoad: () => {
        requestAnimationFrame(() => applyLegacyImageStatesViaSunEditor())
      },
      onImageUploadBefore: async ({ info }) => {
        if (!props.uploadImage) return true
        const file = info.files?.[0]
        const imagePlugin = editor?.$.plugins?.image
        if (!file || !editor || !imagePlugin?.uploadService) return false

        try {
          const image = await props.uploadImage(file)
          if (!isSafeUrl(image.src)) return false

          imagePlugin.uploadService.urlUpload({
            url: image.src,
            files: { name: file.name, size: file.size },
            element: info.element,
            anchor: info.anchor,
            inputWidth: info.inputWidth || 'auto',
            inputHeight: info.inputHeight || 'auto',
            align: info.align || 'none',
            isUpdate: Boolean(info.isUpdate),
            alt: image.alt || info.alt || file.name,
          })
          emit('update:modelValue', normalizeLegacyImagesForApp(editor.$.html.get()))
        } catch {
          return false
        }

        // undefined tells SunEditor the custom bridge already completed insertion.
        return undefined
      },
    },
  }) as unknown as SunEditorInstance

  applyEditorTestId()
  initializeLegacyPresentationBridge()
})

watch(() => props.modelValue, value => {
  if (!editor) return
  if (normalizeLegacyImagesForApp(editor.$.html.get()) === value) return
  setEditorContents(value)
})

onBeforeUnmount(() => {
  editor?.destroy()
  editor = null
})

function setEditorContents(value: string): void {
  if (!editor) return

  readLegacyPresentationStates(value)
  applyingExternalValue = true
  try {
    editor.$.html.set(prepareLegacyImagesForEditor(value))
    initializeLegacyPresentationBridge()
  } finally {
    requestAnimationFrame(() => {
      applyingExternalValue = false
    })
  }
}

function applyEditorTestId(): void {
  editorSurface()?.setAttribute('data-testid', props.testId)
}

function readLegacyPresentationStates(html: string): void {
  pendingLegacyImageStates = readLegacyImageStates(html)
  const tableStates = readLegacyTableStates(html)
  pendingLegacyTableStates = tableStates.tables
  pendingLegacyTableCellStates = tableStates.cells
}

function initializeLegacyPresentationBridge(): void {
  if (!editor) return

  if (pendingLegacyImageStates.length > 0) {
    bindLegacyImageLoads()
    try {
      editor.$.pluginManager?.checkFileInfo?.(true)
    } catch (error) {
      console.warn('[RichTextEditor] SunEditor file-manager compatibility check failed', error)
    }
    applyLegacyImageStatesViaSunEditor()
  }

  applyLegacyTableStates()
  requestAnimationFrame(() => {
    applyLegacyImageStatesViaSunEditor()
    applyLegacyTableStates()
  })
}

function bindLegacyImageLoads(): void {
  const surface = editorSurface()
  if (!surface) return

  surface.querySelectorAll<HTMLImageElement>(`img[${LEGACY_IMAGE_INDEX_ATTRIBUTE}]`).forEach(image => {
    if (boundLegacyImageLoads.has(image)) return
    boundLegacyImageLoads.add(image)
    image.addEventListener('load', () => applyLegacyImageStatesViaSunEditor(), { once: true })
  })
}

function applyLegacyImageStatesViaSunEditor(): void {
  if (!editor || pendingLegacyImageStates.length === 0) return

  const imagePlugin = editor.$.plugins?.image
  const figure = imagePlugin?.figure
  const sizeService = imagePlugin?.sizeService
  const surface = editorSurface()
  if (!figure || !sizeService || !surface) return

  surface.querySelectorAll<HTMLImageElement>(`img[${LEGACY_IMAGE_INDEX_ATTRIBUTE}]`).forEach(image => {
    const index = Number(image.getAttribute(LEGACY_IMAGE_INDEX_ATTRIBUTE))
    const state = Number.isInteger(index) ? pendingLegacyImageStates[index] : undefined
    if (!state) return

    try {
      const info = figure.open(image, {
        nonResizing: true,
        nonSizeInfo: true,
        nonBorder: true,
        figureTarget: false,
        infoOnly: true,
      })
      if (!info?.container) return

      sizeService.applySize(state.width || 'auto', state.height || 'auto')
      applyLegacyImageAlignment(image, state.alignment)
    } catch (error) {
      console.warn('[RichTextEditor] SunEditor legacy-image compatibility bridge failed', error)
    }
  })
}

function applyLegacyImageAlignment(image: HTMLImageElement, alignment: LegacyImageAlignment): void {
  const component = image.closest('.se-component.se-image-container')
  if (!component) return
  component.classList.remove(...IMAGE_FLOAT_CLASSES)
  component.classList.add(`__se__float-${alignment}`)
}

function applyLegacyTableStates(): void {
  const surface = editorSurface()
  if (!surface) return

  surface.querySelectorAll<HTMLTableElement>('table').forEach((table, index) => {
    const state = pendingLegacyTableStates[index]
    if (state?.width) table.style.width = state.width
  })

  surface.querySelectorAll<HTMLTableCellElement>('th, td').forEach((cell, index) => {
    const state = pendingLegacyTableCellStates[index]
    if (!state) return
    if (state.width) cell.style.width = state.width
    if (state.height) cell.style.height = state.height
  })
}

function editorSurface(): HTMLElement | null {
  return editor?.$.frameContext?.get?.('wysiwyg') ?? null
}

function readLegacyImageStates(html: string): LegacyImageState[] {
  if (!html) return []
  const template = document.createElement('template')
  template.innerHTML = html
  return Array.from(template.content.querySelectorAll<HTMLImageElement>('img'), image => ({
    width: legacyDimensionStyle(image.style.width) || legacyDimensionStyle(image.getAttribute('width')) || '',
    height: legacyDimensionStyle(image.style.height) || legacyDimensionStyle(image.getAttribute('height')) || '',
    alignment: legacyImageAlignment(image.style.float || image.getAttribute('align')),
  }))
}

function readLegacyTableStates(html: string): { tables: LegacyTableState[]; cells: LegacyTableCellState[] } {
  if (!html) return { tables: [], cells: [] }
  const template = document.createElement('template')
  template.innerHTML = html

  const tables = Array.from(template.content.querySelectorAll<HTMLTableElement>('table'), table => ({
    width: legacyDimensionStyle(table.style.width) || '',
  }))
  const cells = Array.from(template.content.querySelectorAll<HTMLTableCellElement>('th, td'), cell => ({
    width: legacyDimensionStyle(cell.style.width) || '',
    height: legacyDimensionStyle(cell.style.height) || '',
  }))

  return { tables, cells }
}

function prepareLegacyImagesForEditor(html: string): string {
  if (!html) return ''
  const template = document.createElement('template')
  template.innerHTML = html

  template.content.querySelectorAll<HTMLImageElement>('img').forEach((image, index) => {
    image.setAttribute(LEGACY_IMAGE_INDEX_ATTRIBUTE, String(index))
    if (image.closest('.se-component.se-image-container')) return

    const wrapper = document.createElement('span')
    const alignment = legacyImageAlignment(image.style.float || image.getAttribute('align'))
    wrapper.className = `se-component se-inline-component se-image-container __se__float-${alignment}`
    image.replaceWith(wrapper)
    wrapper.appendChild(image)
  })

  return template.innerHTML
}

function normalizeLegacyImagesForApp(html: string): string {
  if (!html) return ''
  const template = document.createElement('template')
  template.innerHTML = html

  template.content.querySelectorAll<HTMLImageElement>(`img[${LEGACY_IMAGE_INDEX_ATTRIBUTE}]`).forEach(image => {
    const component = image.closest('.se-component.se-image-container')
    const clone = image.cloneNode(true) as HTMLImageElement
    const dataSize = (clone.getAttribute('data-se-size') || '').split(',')
    const width = legacyDimensionStyle(clone.style.width)
      || legacyDimensionStyle(dataSize[0])
      || legacyDimensionStyle(clone.getAttribute('width'))
    const height = legacyDimensionStyle(clone.style.height)
      || legacyDimensionStyle(dataSize[1])
      || legacyDimensionStyle(clone.getAttribute('height'))

    if (width) clone.setAttribute('width', dimensionAttributeValue(width))
    else clone.removeAttribute('width')
    if (height) clone.setAttribute('height', dimensionAttributeValue(height))
    else clone.removeAttribute('height')

    for (const name of clone.getAttributeNames()) {
      if (name.startsWith('data-se-') || name === LEGACY_IMAGE_INDEX_ATTRIBUTE) clone.removeAttribute(name)
    }

    const originalFloat = clone.style.float
    clone.style.removeProperty('width')
    clone.style.removeProperty('height')
    clone.style.removeProperty('float')

    if (component?.classList.contains('__se__float-left')) clone.style.float = 'left'
    else if (component?.classList.contains('__se__float-right')) clone.style.float = 'right'
    else if (originalFloat === 'left' || originalFloat === 'right') clone.style.float = originalFloat

    if (!clone.getAttribute('style')) clone.removeAttribute('style')

    if (component) component.replaceWith(clone)
    else image.replaceWith(clone)
  })

  return template.innerHTML
}

function legacyDimensionStyle(value: string | null | undefined): string | null {
  const normalized = value?.trim() || ''
  if (/^\d+(?:\.\d+)?$/.test(normalized)) return `${normalized}px`
  if (/^\d+(?:\.\d+)?(?:px|%)$/i.test(normalized)) return normalized
  return null
}

function dimensionAttributeValue(value: string): string {
  return value.endsWith('px') ? value.slice(0, -2) : value
}

function legacyImageAlignment(value: string | null | undefined): LegacyImageAlignment {
  const normalized = value?.trim().toLowerCase()
  if (normalized === 'left' || normalized === 'center' || normalized === 'right') return normalized
  return 'none'
}

function isSafeUrl(value: string): boolean {
  return value.startsWith('/') || /^https?:\/\//i.test(value)
}
</script>

<template>
  <div class="rich-text-editor-shell" :class="{ 'is-uploading': uploading }" :data-testid="`${testId}-shell`">
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
