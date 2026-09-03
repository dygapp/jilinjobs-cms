<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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

const pageCount = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const hasPrevious = computed(() => page.value > 0)
const hasNext = computed(() => page.value + 1 < pageCount.value)

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

function isExternal(article: PublicArticleSummary) {
  return article.articleType === 'EXTERNAL_LINK' && Boolean(article.externalUrl)
}

function go(nextPage: number) {
  const alias = route.params.alias
  if (!isPartyColumnAlias(alias)) return
  const query: Record<string, string> = {}
  if (nextPage > 0) query.page = String(nextPage)
  if (size.value !== 10) query.size = String(size.value)
  void router.push({ path: `/party/column/${alias}`, query })
}

function changeSize(event: Event) {
  const nextSize = Number((event.target as HTMLSelectElement).value)
  const alias = route.params.alias
  if (!isPartyColumnAlias(alias) || !allowedSizes.includes(nextSize)) return
  void router.push({
    path: `/party/column/${alias}`,
    query: nextSize === 10 ? {} : { size: String(nextSize) },
  })
}
</script>

<template>
  <main class="party-main party-page">
    <div class="party-width party-page-frame">
      <nav class="party-breadcrumb" aria-label="当前位置">
        <router-link to="/party/">中心党建</router-link>
        <span>›</span>
        <span v-if="columnName">{{ columnName }}</span>
      </nav>

      <p v-if="loading && !columnName" class="party-state">正在加载栏目…</p>
      <p v-else-if="error" class="party-state party-state-error" data-testid="party-column-unavailable">{{ error }}</p>
      <section v-else class="party-content-card" data-testid="party-column-page" :aria-busy="loading">
        <header class="party-content-title"><h1>{{ columnName }}</h1></header>
        <ul class="party-column-list" data-testid="party-column-articles">
          <li v-for="article in articles" :key="article.id">
            <a
              v-if="isExternal(article)"
              :data-testid="`party-column-article-${article.id}`"
              :href="article.externalUrl!"
              target="_blank"
              rel="noopener noreferrer"
            >
              <span>{{ article.title }}</span><time v-if="article.publishDate">{{ article.publishDate }}</time>
            </a>
            <router-link
              v-else
              :data-testid="`party-column-article-${article.id}`"
              :to="`/party/article/${article.id}`"
            >
              <span>{{ article.title }}</span><time v-if="article.publishDate">{{ article.publishDate }}</time>
            </router-link>
          </li>
          <li v-if="!articles.length" class="party-empty">当前栏目暂无已发布内容。</li>
        </ul>

        <div v-if="total > size" class="party-pagination" aria-label="栏目分页">
          <button type="button" :disabled="!hasPrevious" aria-label="上一页" @click="go(page - 1)">上一页</button>
          <span>第 {{ page + 1 }} / {{ pageCount }} 页，共 {{ total }} 条</span>
          <button type="button" :disabled="!hasNext" aria-label="下一页" @click="go(page + 1)">下一页</button>
          <select :value="size" aria-label="每页条数" @change="changeSize">
            <option v-for="option in allowedSizes" :key="option" :value="option">{{ option }}条/页</option>
          </select>
        </div>
      </section>
    </div>
  </main>
</template>
