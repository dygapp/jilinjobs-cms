<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PublicColumnPage from '../../../../shared/components/PublicColumnPage.vue'
import { listPublicArticles, type PublicArticleSummary } from '../../api/articles'
import { getPublicColumn, getPublicColumnByAlias, type PublicColumn } from '../../api/columns'
import { setPageMeta } from '../../seo'

const route = useRoute()
const router = useRouter()
const column = ref<PublicColumn | null>(null)
const breadcrumbs = ref<PublicColumn[]>([])
const articles = ref<PublicArticleSummary[]>([])
const total = ref(0)
const page = ref(0)
const size = ref(10)
const error = ref('')
const loading = ref(false)
const allowedSizes = [10, 20, 30]

const sharedBreadcrumbs = computed(() => [
  { label: '网站首页', to: '/' },
  ...breadcrumbs.value.map(item => ({
    label: item.name,
    to: item.alias ? `/column/${item.alias}` : `/columns/${item.id}`,
  })),
])

watch(
  () => [route.params.alias, route.params.id, route.query.page, route.query.size],
  (_value, _oldValue, onCleanup) => {
    let current = true
    onCleanup(() => {
      current = false
    })
    void load(() => current)
  },
  { immediate: true },
)

async function load(isCurrent: () => boolean) {
  const alias = typeof route.params.alias === 'string' ? route.params.alias : null
  const id = Number(route.params.id)
  const requestedPage = Math.max(0, Number(route.query.page ?? 0) || 0)
  const requestedSize = Number(route.query.size ?? 10)
  const pageSize = allowedSizes.includes(requestedSize) ? requestedSize : 10

  error.value = ''
  loading.value = true
  try {
    const nextColumn = alias ? await getPublicColumnByAlias(alias) : await getPublicColumn(id)
    if (!isCurrent()) return

    const trail = [nextColumn]
    const seen = new Set([nextColumn.id])
    let parent = nextColumn.parentId
    while (parent != null && !seen.has(parent)) {
      const item = await getPublicColumn(parent)
      if (!isCurrent()) return
      trail.unshift(item)
      seen.add(item.id)
      parent = item.parentId
    }

    const articlePage = await listPublicArticles(nextColumn.id, requestedPage, pageSize)
    if (!isCurrent()) return

    column.value = nextColumn
    breadcrumbs.value = trail
    articles.value = articlePage.items
    total.value = articlePage.total
    page.value = articlePage.page
    size.value = pageSize
    setPageMeta({ title: nextColumn.name, description: `浏览“${nextColumn.name}”栏目已发布信息。` })
  } catch (e) {
    if (!isCurrent()) return
    column.value = null
    breadcrumbs.value = []
    articles.value = []
    total.value = 0
    error.value = e instanceof Error ? e.message : '栏目加载失败'
  } finally {
    if (isCurrent()) loading.value = false
  }
}

function queryFor(nextPage: number, nextSize = size.value) {
  return {
    ...(nextPage > 0 ? { page: String(nextPage) } : {}),
    ...(nextSize !== 10 ? { size: String(nextSize) } : {}),
  }
}

function go(nextPage: number) {
  void router.push({ path: route.path, query: queryFor(nextPage) })
}

function changeSize(nextSize: number) {
  if (allowedSizes.includes(nextSize)) void router.push({ path: route.path, query: queryFor(0, nextSize) })
}
</script>

<template>
  <PublicColumnPage
    :title="column?.name || ''"
    :breadcrumbs="sharedBreadcrumbs"
    :articles="articles"
    :total="total"
    :page="page"
    :size="size"
    :loading="loading"
    :error="error"
    article-base-path="/article"
    test-id-prefix="column"
    @page-change="go"
    @size-change="changeSize"
  />
</template>
