<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PublicColumnPage from '../../../../shared/components/PublicColumnPage.vue'
import { listPublicArticles, type PublicArticleSummary } from '../../../../shared/api/articles'
import { setPageMeta } from '../../../../shared/seo'
import { getPartyColumn, isPartyColumnAlias } from '../../app/partyContext'

const route = useRoute()
const router = useRouter()
const columnName = ref('')
const articles = ref<PublicArticleSummary[]>([])
const total = ref(0)
const page = ref(0)
const size = ref(10)
const loading = ref(false)
const error = ref('')
const allowedSizes = [10, 20, 30]

const sharedBreadcrumbs = computed(() => [
  { label: '中心党建', to: '/party/' },
  ...(columnName.value ? [{ label: columnName.value }] : []),
])

watch(
  () => [route.params.alias, route.query.page, route.query.size],
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
  const alias = route.params.alias
  error.value = ''
  if (!isPartyColumnAlias(alias)) {
    columnName.value = ''
    articles.value = []
    total.value = 0
    page.value = 0
    loading.value = false
    error.value = '党建栏目不可用或不存在'
    return
  }

  const requestedPage = Math.max(0, Number(route.query.page ?? 0) || 0)
  const requestedSize = Number(route.query.size ?? 10)
  const pageSize = allowedSizes.includes(requestedSize) ? requestedSize : 10
  loading.value = true
  try {
    const column = await getPartyColumn(alias)
    if (!isCurrent()) return
    const result = await listPublicArticles(column.id, requestedPage, pageSize)
    if (!isCurrent()) return

    columnName.value = column.name
    articles.value = result.items
    total.value = result.total
    page.value = result.page
    size.value = pageSize
    setPageMeta({ title: column.name, description: `浏览中心党建“${column.name}”栏目已发布信息。` })
  } catch (e) {
    if (!isCurrent()) return
    columnName.value = ''
    articles.value = []
    total.value = 0
    page.value = 0
    error.value = e instanceof Error ? e.message : '党建栏目加载失败'
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
  const alias = route.params.alias
  if (!isPartyColumnAlias(alias)) return
  void router.push({ path: `/party/column/${alias}`, query: queryFor(nextPage) })
}

function changeSize(nextSize: number) {
  const alias = route.params.alias
  if (!isPartyColumnAlias(alias) || !allowedSizes.includes(nextSize)) return
  void router.push({ path: `/party/column/${alias}`, query: queryFor(0, nextSize) })
}
</script>

<template>
  <PublicColumnPage
    :title="columnName"
    :breadcrumbs="sharedBreadcrumbs"
    :articles="articles"
    :total="total"
    :page="page"
    :size="size"
    :loading="loading"
    :error="error"
    article-base-path="/party/article"
    test-id-prefix="party-column"
    @page-change="go"
    @size-change="changeSize"
  />
</template>
