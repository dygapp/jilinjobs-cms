import { computed, onMounted, onUnmounted, ref, watch, type Ref } from 'vue'

export type CarouselIdentity = { id: number }

type ContentCarouselOptions<T extends CarouselIdentity> = {
  items: Ref<T[]>
  intervalSeconds: Ref<number>
  maxItems: Ref<number>
}

export function useContentCarousel<T extends CarouselIdentity>(options: ContentCarouselOptions<T>) {
  const activeIndex = ref(0)
  const failedIds = ref<Set<number>>(new Set())
  const pauseReasons = ref<Set<string>>(new Set())
  const reducedMotion = ref(false)
  let timer: number | undefined
  let media: MediaQueryList | null = null

  const visibleItems = computed(() => {
    const limit = Math.max(1, Math.trunc(options.maxItems.value || 1))
    return options.items.value.filter(item => !failedIds.value.has(item.id)).slice(0, limit)
  })
  const activeItem = computed(() => visibleItems.value[activeIndex.value] ?? null)

  function clearTimer() {
    if (timer !== undefined) window.clearInterval(timer)
    timer = undefined
  }

  function schedule() {
    clearTimer()
    if (reducedMotion.value || pauseReasons.value.size > 0 || visibleItems.value.length <= 1) return
    const seconds = Math.max(1, Math.trunc(options.intervalSeconds.value || 1))
    timer = window.setInterval(() => {
      activeIndex.value = (activeIndex.value + 1) % visibleItems.value.length
    }, seconds * 1000)
  }

  function pause(reason: string) {
    const next = new Set(pauseReasons.value)
    next.add(reason)
    pauseReasons.value = next
    schedule()
  }

  function resume(reason: string) {
    const next = new Set(pauseReasons.value)
    next.delete(reason)
    pauseReasons.value = next
    schedule()
  }

  function select(index: number) {
    if (index < 0 || index >= visibleItems.value.length) return
    activeIndex.value = index
    schedule()
  }

  function markImageFailed(id: number) {
    const next = new Set(failedIds.value)
    next.add(id)
    failedIds.value = next
  }

  function onFocusOut(event: FocusEvent) {
    const current = event.currentTarget as HTMLElement | null
    const next = event.relatedTarget as Node | null
    if (!current || !next || !current.contains(next)) resume('focus')
  }

  function onVisibilityChange() {
    if (document.hidden) pause('visibility')
    else resume('visibility')
  }

  function onMotionChange(event: MediaQueryListEvent | MediaQueryList) {
    reducedMotion.value = event.matches
    schedule()
  }

  watch([visibleItems, options.intervalSeconds, options.maxItems], () => {
    if (activeIndex.value >= visibleItems.value.length) activeIndex.value = Math.max(0, visibleItems.value.length - 1)
    schedule()
  }, { deep: false })

  onMounted(() => {
    document.addEventListener('visibilitychange', onVisibilityChange)
    media = window.matchMedia('(prefers-reduced-motion: reduce)')
    onMotionChange(media)
    media.addEventListener?.('change', onMotionChange)
    schedule()
  })

  onUnmounted(() => {
    clearTimer()
    document.removeEventListener('visibilitychange', onVisibilityChange)
    media?.removeEventListener?.('change', onMotionChange)
  })

  return {
    activeIndex,
    activeItem,
    visibleItems,
    reducedMotion,
    pause,
    resume,
    select,
    markImageFailed,
    onFocusOut,
  }
}
