<script setup lang="ts">
import { ref, watch } from 'vue'

type PreviewTheme = 'plain' | 'analyzing' | 'dark' | 'light' | 'checker'

const props = withDefaults(defineProps<{
  src: string
  alt?: string
  adaptive?: boolean
}>(), {
  alt: '',
  adaptive: false,
})

const theme = ref<PreviewTheme>(props.adaptive ? 'analyzing' : 'plain')

watch(() => `${props.src}|${props.adaptive}`, () => {
  theme.value = props.adaptive ? 'analyzing' : 'plain'
})

function analyse(event: Event) {
  if (!props.adaptive) {
    theme.value = 'plain'
    return
  }
  const image = event.currentTarget as HTMLImageElement
  if (!image.naturalWidth || !image.naturalHeight) {
    theme.value = 'checker'
    return
  }
  try {
    const size = 32
    const canvas = document.createElement('canvas')
    canvas.width = size
    canvas.height = size
    const context = canvas.getContext('2d', { willReadFrequently: true })
    if (!context) {
      theme.value = 'checker'
      return
    }
    context.clearRect(0, 0, size, size)
    context.drawImage(image, 0, 0, size, size)
    const pixels = context.getImageData(0, 0, size, size).data
    let weight = 0
    let luminanceSum = 0
    let darkWeight = 0
    let lightWeight = 0

    for (let index = 0; index < pixels.length; index += 4) {
      const alpha = pixels[index + 3] / 255
      if (alpha < 0.12) continue
      const luminance = (0.2126 * pixels[index] + 0.7152 * pixels[index + 1] + 0.0722 * pixels[index + 2]) / 255
      weight += alpha
      luminanceSum += luminance * alpha
      if (luminance <= 0.28) darkWeight += alpha
      if (luminance >= 0.72) lightWeight += alpha
    }

    if (weight < 2) {
      theme.value = 'checker'
      return
    }

    const average = luminanceSum / weight
    const darkRatio = darkWeight / weight
    const lightRatio = lightWeight / weight
    if (darkRatio >= 0.2 && lightRatio >= 0.2) theme.value = 'checker'
    else if (average >= 0.5) theme.value = 'dark'
    else if (average <= 0.36) theme.value = 'light'
    else theme.value = 'checker'
  } catch {
    // Cross-origin or unreadable images fall back to a neutral checkerboard.
    theme.value = 'checker'
  }
}

function failed() {
  theme.value = props.adaptive ? 'checker' : 'plain'
}
</script>

<template>
  <div
    class="adaptive-image-preview"
    :class="`theme-${theme}`"
    :data-preview-theme="theme"
    data-testid="adaptive-image-preview"
    :title="adaptive ? '预览背景会根据图标明暗自动调整；悬停可切换对比背景' : undefined"
  >
    <img :src="src" :alt="alt" @load="analyse" @error="failed">
  </div>
</template>

<style scoped>
.adaptive-image-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: background .16s ease, border-color .16s ease;
}
.adaptive-image-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}
.theme-plain { background: #fff; }
.theme-dark { background: #334155; }
.theme-light { background: #f5f7fa; }
.theme-analyzing,
.theme-checker {
  background-color: #eef1f5;
  background-image:
    linear-gradient(45deg, rgba(148,163,184,.24) 25%, transparent 25%),
    linear-gradient(-45deg, rgba(148,163,184,.24) 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, rgba(148,163,184,.24) 75%),
    linear-gradient(-45deg, transparent 75%, rgba(148,163,184,.24) 75%);
  background-size: 14px 14px;
  background-position: 0 0, 0 7px, 7px -7px, -7px 0;
}
.theme-dark:hover {
  background: #f5f7fa;
  border-color: #94a3b8;
}
.theme-light:hover {
  background: #334155;
  border-color: #64748b;
}
.theme-checker:hover,
.theme-analyzing:hover {
  background: #334155;
  border-color: #64748b;
}
</style>
