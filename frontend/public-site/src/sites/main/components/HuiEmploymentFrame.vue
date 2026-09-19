<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

type FrameStatus = 'idle' | 'loading' | 'loaded' | 'failed'

const props = withDefaults(defineProps<{
  src: string
  title: string
  variant?: 'calendar' | 'home-wide' | 'home-compact' | 'page'
  timeoutMs?: number
  testId?: string
}>(), {
  variant: 'page',
  timeoutMs: 20_000,
  testId: undefined,
})

const container = ref<HTMLElement | null>(null)
const active = ref(false)
const attempt = ref(0)
const status = ref<FrameStatus>('idle')
let timeout: ReturnType<typeof setTimeout> | null = null
let observer: IntersectionObserver | null = null

const frameKey = computed(() => `${props.src}:${attempt.value}`)
const busy = computed(() => status.value === 'idle' || status.value === 'loading')

function clearLoadTimeout() {
  if (timeout) clearTimeout(timeout)
  timeout = null
}

function beginLoad() {
  clearLoadTimeout()
  status.value = 'loading'
  timeout = setTimeout(() => {
    status.value = 'failed'
    timeout = null
  }, props.timeoutMs)
}

function activate() {
  if (active.value) return
  active.value = true
  beginLoad()
  observer?.disconnect()
  observer = null
}

function markLoaded() {
  clearLoadTimeout()
  status.value = 'loaded'
}

function markFailed() {
  clearLoadTimeout()
  status.value = 'failed'
}

async function retry() {
  attempt.value += 1
  status.value = 'idle'
  await nextTick()
  beginLoad()
}

onMounted(() => {
  if (!('IntersectionObserver' in window) || !container.value) {
    activate()
    return
  }
  observer = new IntersectionObserver(entries => {
    if (entries.some(entry => entry.isIntersecting)) activate()
  }, { rootMargin: '240px 0px' })
  observer.observe(container.value)
})

watch(() => props.src, () => {
  attempt.value += 1
  if (active.value) beginLoad()
})

onBeforeUnmount(() => {
  clearLoadTimeout()
  observer?.disconnect()
})
</script>

<template>
  <div
    ref="container"
    class="hui-employment-frame"
    :class="`hui-employment-frame--${variant}`"
    :aria-busy="busy"
    :data-testid="testId"
    :data-frame-status="status"
  >
    <iframe
      v-if="active && status !== 'failed'"
      :key="frameKey"
      class="hui-employment-frame__content"
      :src="src"
      :title="`${title}—慧就业`"
      loading="lazy"
      referrerpolicy="strict-origin-when-cross-origin"
      allowfullscreen
      @load="markLoaded"
      @error="markFailed"
    />
    <div v-if="!active || status === 'loading'" class="hui-employment-frame__state" role="status">
      <span class="hui-employment-frame__spinner" aria-hidden="true" />
      <span>正在加载{{ title }}…</span>
    </div>
    <div v-else-if="status === 'failed'" class="hui-employment-frame__state hui-employment-frame__state--failed" role="alert">
      <strong>{{ title }}暂时无法加载</strong>
      <span>慧就业外部内容暂时不可用，吉林就业其他内容仍可正常访问。</span>
      <button type="button" @click="retry">重新加载</button>
    </div>
  </div>
</template>

<style scoped>
.hui-employment-frame{position:relative;width:100%;overflow:hidden;background:#fff;border:1px solid #ebeef2}
.hui-employment-frame--calendar{height:260px}
.hui-employment-frame--home-wide{height:470px}
.hui-employment-frame--home-compact{height:250px}
.hui-employment-frame--page{height:clamp(680px,78vh,980px)}
.hui-employment-frame__content{display:block;width:100%;height:100%;border:0;background:#fff}
.hui-employment-frame__state{position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:10px;padding:24px;background:#f8fbff;color:#617185;text-align:center}
.hui-employment-frame__state--failed{flex-direction:column;color:#515c6b}
.hui-employment-frame__state--failed strong{color:#323b47;font-size:16px}
.hui-employment-frame__state--failed button{margin-top:4px;padding:8px 18px;border:1px solid #005cd4;border-radius:3px;background:#005cd4;color:#fff;cursor:pointer}
.hui-employment-frame__state--failed button:hover,.hui-employment-frame__state--failed button:focus-visible{background:#00439a}
.hui-employment-frame__spinner{width:18px;height:18px;border:2px solid #c8d8ea;border-top-color:#005cd4;border-radius:50%;animation:hui-employment-spin .8s linear infinite}
@keyframes hui-employment-spin{to{transform:rotate(360deg)}}
@media(prefers-reduced-motion:reduce){.hui-employment-frame__spinner{animation:none;border-top-color:#c8d8ea}}
@media(max-width:760px){
  .hui-employment-frame--calendar{height:300px}
  .hui-employment-frame--home-wide{height:420px}
  .hui-employment-frame--home-compact{height:320px}
  .hui-employment-frame--page{height:720px}
}
</style>
