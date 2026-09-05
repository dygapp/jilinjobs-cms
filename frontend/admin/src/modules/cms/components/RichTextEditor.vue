<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
import Image from '@tiptap/extension-image'
import { TableKit } from '@tiptap/extension-table'
import TextAlign from '@tiptap/extension-text-align'
import { TextStyleKit } from '@tiptap/extension-text-style'
import StarterKit from '@tiptap/starter-kit'
import { EditorContent, useEditor } from '@tiptap/vue-3'

interface UploadedImage {
  src: string
  alt?: string
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
const fileInput = ref<HTMLInputElement | null>(null)
const blockType = ref<'paragraph' | '2' | '3' | '4'>('paragraph')
const fontSize = ref('16px')
const fontFamily = ref('Microsoft YaHei')

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit.configure({
      heading: { levels: [2, 3, 4] },
      link: { openOnClick: false, autolink: false, defaultProtocol: 'https' },
    }),
    Image.configure({ allowBase64: false }),
    TextAlign.configure({ types: ['heading', 'paragraph'] }),
    TextStyleKit,
    TableKit,
  ],
  editorProps: {
    attributes: {
      class: 'rich-editor',
      'data-testid': props.testId,
    },
  },
  onUpdate: ({ editor: current }) => emit('update:modelValue', current.getHTML()),
})

watch(() => props.modelValue, value => {
  const current = editor.value
  if (!current || current.getHTML() === value) return
  current.commands.setContent(value || '', { emitUpdate: false })
})

onBeforeUnmount(() => editor.value?.destroy())

function setBlock(value: 'paragraph' | '2' | '3' | '4') {
  blockType.value = value
  if (value === 'paragraph') editor.value?.chain().focus().setParagraph().run()
  else editor.value?.chain().focus().setHeading({ level: Number(value) as 2 | 3 | 4 }).run()
}

function setFontSize(value: string) {
  fontSize.value = value
  editor.value?.chain().focus().setFontSize(value).run()
}

function setFontFamily(value: string) {
  fontFamily.value = value
  editor.value?.chain().focus().setFontFamily(value).run()
}

function setTextColor(value: string | null) {
  if (value) editor.value?.chain().focus().setColor(value).run()
}

function setBackgroundColor(value: string | null) {
  if (value) editor.value?.chain().focus().setBackgroundColor(value).run()
}

function safeUrl(value: string): boolean {
  return value.startsWith('/') || /^https?:\/\//i.test(value)
}

function editLink() {
  const current = editor.value
  if (!current) return
  const existing = String(current.getAttributes('link').href || '')
  const raw = window.prompt('链接地址', existing || 'https://')
  if (raw == null) return
  const url = raw.trim()
  if (!url) {
    current.chain().focus().extendMarkRange('link').unsetLink().run()
    return
  }
  if (!safeUrl(url)) return
  current.chain().focus().extendMarkRange('link').setLink({ href: url }).run()
}

function requestImage() {
  if (props.uploadImage) {
    const current = editor.value
    if (current) current.chain().focus().setTextSelection(current.state.selection.to).run()
    fileInput.value?.click()
    return
  }
  const raw = window.prompt('图片地址', 'https://')
  if (raw == null) return
  const src = raw.trim()
  if (!safeUrl(src)) return
  editor.value?.chain().focus().setImage({ src }).run()
}

async function handleImageFile(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !props.uploadImage) return
  const image = await props.uploadImage(file)
  if (!safeUrl(image.src)) return
  editor.value?.chain().focus().setImage({ src: image.src, alt: image.alt || '' }).run()
}
</script>

<template>
  <div class="rich-text-editor-shell" :data-testid="`${testId}-shell`">
    <div v-if="editor" class="rich-text-editor-toolbar" :data-testid="`${testId}-toolbar`">
      <el-button-group>
        <el-button size="small" :data-testid="`${testId}-undo`" @click="editor.chain().focus().undo().run()">撤销</el-button>
        <el-button size="small" :data-testid="`${testId}-redo`" @click="editor.chain().focus().redo().run()">重做</el-button>
      </el-button-group>

      <el-select :model-value="blockType" size="small" class="toolbar-select block-select" :data-testid="`${testId}-block`" @change="setBlock">
        <el-option label="正文" value="paragraph" />
        <el-option label="标题 2" value="2" />
        <el-option label="标题 3" value="3" />
        <el-option label="标题 4" value="4" />
      </el-select>

      <el-button-group>
        <el-button size="small" :type="editor.isActive('bold') ? 'primary' : 'default'" :data-testid="`${testId}-bold`" @click="editor.chain().focus().toggleBold().run()"><strong>B</strong></el-button>
        <el-button size="small" :type="editor.isActive('italic') ? 'primary' : 'default'" :data-testid="`${testId}-italic`" @click="editor.chain().focus().toggleItalic().run()"><em>I</em></el-button>
        <el-button size="small" :type="editor.isActive('underline') ? 'primary' : 'default'" :data-testid="`${testId}-underline`" @click="editor.chain().focus().toggleUnderline().run()"><u>U</u></el-button>
        <el-button size="small" :type="editor.isActive('strike') ? 'primary' : 'default'" :data-testid="`${testId}-strike`" @click="editor.chain().focus().toggleStrike().run()"><s>S</s></el-button>
      </el-button-group>

      <el-button-group>
        <el-button size="small" :data-testid="`${testId}-bullet-list`" @click="editor.chain().focus().toggleBulletList().run()">项目符号</el-button>
        <el-button size="small" :data-testid="`${testId}-ordered-list`" @click="editor.chain().focus().toggleOrderedList().run()">编号</el-button>
        <el-button size="small" :data-testid="`${testId}-blockquote`" @click="editor.chain().focus().toggleBlockquote().run()">引用</el-button>
        <el-button size="small" :data-testid="`${testId}-hr`" @click="editor.chain().focus().setHorizontalRule().run()">分隔线</el-button>
      </el-button-group>

      <el-button-group>
        <el-button size="small" :data-testid="`${testId}-align-left`" @click="editor.chain().focus().setTextAlign('left').run()">左</el-button>
        <el-button size="small" :data-testid="`${testId}-align-center`" @click="editor.chain().focus().setTextAlign('center').run()">中</el-button>
        <el-button size="small" :data-testid="`${testId}-align-right`" @click="editor.chain().focus().setTextAlign('right').run()">右</el-button>
      </el-button-group>

      <el-select :model-value="fontSize" size="small" class="toolbar-select font-size-select" :data-testid="`${testId}-font-size`" @change="setFontSize">
        <el-option v-for="size in ['14px','16px','18px','20px','24px','28px']" :key="size" :label="size" :value="size" />
      </el-select>
      <el-select :model-value="fontFamily" size="small" class="toolbar-select font-family-select" :data-testid="`${testId}-font-family`" @change="setFontFamily">
        <el-option label="微软雅黑" value="Microsoft YaHei" />
        <el-option label="宋体" value="SimSun" />
        <el-option label="Arial" value="Arial" />
      </el-select>
      <span class="color-control">文字<el-color-picker size="small" :data-testid="`${testId}-text-color`" @change="setTextColor" /></span>
      <span class="color-control">底色<el-color-picker size="small" :data-testid="`${testId}-background-color`" @change="setBackgroundColor" /></span>

      <el-button-group>
        <el-button size="small" :data-testid="`${testId}-link`" @click="editLink">链接</el-button>
        <el-button size="small" :data-testid="`${testId}-unlink`" @click="editor.chain().focus().unsetLink().run()">移除链接</el-button>
        <el-button size="small" :data-testid="`${testId}-image`" :loading="uploading" @click="requestImage">{{ uploadImage ? '插入图片' : '图片地址' }}</el-button>
      </el-button-group>

      <el-button-group>
        <el-button size="small" :data-testid="`${testId}-table`" @click="editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()">插入表格</el-button>
        <el-button size="small" :data-testid="`${testId}-table-add-row`" @click="editor.chain().focus().addRowAfter().run()">加行</el-button>
        <el-button size="small" :data-testid="`${testId}-table-delete-row`" @click="editor.chain().focus().deleteRow().run()">删行</el-button>
        <el-button size="small" :data-testid="`${testId}-table-add-column`" @click="editor.chain().focus().addColumnAfter().run()">加列</el-button>
        <el-button size="small" :data-testid="`${testId}-table-delete-column`" @click="editor.chain().focus().deleteColumn().run()">删列</el-button>
        <el-button size="small" :data-testid="`${testId}-table-delete`" @click="editor.chain().focus().deleteTable().run()">删表</el-button>
      </el-button-group>
    </div>

    <input v-if="uploadImage" ref="fileInput" data-testid="body-image-input" class="hidden-file-input" type="file" accept="image/*" :disabled="uploading" @change="handleImageFile">
    <EditorContent :editor="editor" class="rich-text-editor-content" />
  </div>
</template>

<style scoped>
.rich-text-editor-shell { width: 100%; border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden; background: #fff; }
.rich-text-editor-toolbar { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; padding: 8px; border-bottom: 1px solid #ebeef5; background: #f8f9fb; }
.toolbar-select { width: 104px; }
.font-family-select { width: 118px; }
.color-control { display: inline-flex; align-items: center; gap: 4px; color: #606266; font-size: 12px; }
.hidden-file-input { display: none; }
.rich-text-editor-content :deep(.rich-editor) { min-height: 240px; padding: 14px 16px; outline: none; line-height: 1.75; word-break: break-word; }
.rich-text-editor-content :deep(.rich-editor p) { margin: 0 0 0.8em; }
.rich-text-editor-content :deep(.rich-editor h2),
.rich-text-editor-content :deep(.rich-editor h3),
.rich-text-editor-content :deep(.rich-editor h4) { margin: 1em 0 0.55em; line-height: 1.4; }
.rich-text-editor-content :deep(.rich-editor blockquote) { margin: 0.8em 0; padding-left: 12px; border-left: 3px solid #dcdfe6; color: #606266; }
.rich-text-editor-content :deep(.rich-editor img) { max-width: 100%; height: auto; }
.rich-text-editor-content :deep(.rich-editor table) { width: 100%; border-collapse: collapse; margin: 0.8em 0; }
.rich-text-editor-content :deep(.rich-editor th),
.rich-text-editor-content :deep(.rich-editor td) { border: 1px solid #dcdfe6; padding: 6px 8px; vertical-align: top; }
</style>
