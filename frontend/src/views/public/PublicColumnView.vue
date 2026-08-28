<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listPublicArticles, type PublicArticleSummary } from '../../api/articles'
import { getPublicColumn, getPublicColumnByAlias, type PublicColumn } from '../../api/columns'
import PublicSiteHeader from '../../components/PublicSiteHeader.vue'
import PublicSiteFooter from '../../components/PublicSiteFooter.vue'
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
const jumpPage = ref('')
const allowedSizes = [10, 20, 30]

const pageCount = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const hasPrevious = computed(() => page.value > 0)
const hasNext = computed(() => page.value + 1 < pageCount.value)
const visiblePages = computed(() => {
  const count = pageCount.value
  if (count <= 9) return Array.from({ length: count }, (_, index) => index)
  const start = Math.min(Math.max(page.value - 4, 0), count - 9)
  return Array.from({ length: 9 }, (_, index) => start + index)
})

watch(() => [route.params.alias, route.params.id, route.query.page, route.query.size], load, { immediate: true })

async function load() {
  const alias = typeof route.params.alias === 'string' ? route.params.alias : null
  const id = Number(route.params.id)
  const requestedPage = Math.max(0, Number(route.query.page ?? 0) || 0)
  const requestedSize = Number(route.query.size ?? 10)
  size.value = allowedSizes.includes(requestedSize) ? requestedSize : 10
  column.value = null
  breadcrumbs.value = []
  articles.value = []
  error.value = ''
  loading.value = true
  try {
    const current = alias ? await getPublicColumnByAlias(alias) : await getPublicColumn(id)
    const trail = [current]
    const seen = new Set([current.id])
    let parent = current.parentId
    while (parent != null && !seen.has(parent)) {
      const item = await getPublicColumn(parent)
      trail.unshift(item)
      seen.add(item.id)
      parent = item.parentId
    }
    const articlePage = await listPublicArticles(current.id, requestedPage, size.value)
    column.value = current
    breadcrumbs.value = trail
    articles.value = articlePage.items
    total.value = articlePage.total
    page.value = articlePage.page
    jumpPage.value = ''
    setPageMeta({ title: current.name, description: `浏览“${current.name}”栏目已发布信息。` })
  } catch (e) {
    error.value = e instanceof Error ? e.message : '栏目加载失败'
  } finally {
    loading.value = false
  }
}

function queryFor(nextPage: number, nextSize = size.value) {
  return {
    ...(nextPage > 0 ? { page: String(nextPage) } : {}),
    ...(nextSize !== 10 ? { size: String(nextSize) } : {}),
  }
}

function go(nextPage: number) {
  void router.push({ path: route.path, query: queryFor(Math.min(Math.max(nextPage, 0), pageCount.value - 1)) })
}

function changeSize(event: Event) {
  const nextSize = Number((event.target as HTMLSelectElement).value)
  if (allowedSizes.includes(nextSize)) void router.push({ path: route.path, query: queryFor(0, nextSize) })
}

function jump() {
  const target = Number(jumpPage.value)
  if (!Number.isInteger(target) || target < 1) return
  go(Math.min(target, pageCount.value) - 1)
}
</script>

<template>
  <PublicSiteHeader />
  <main class="public-page-shell">
    <div class="site-width public-page-frame">
      <nav class="breadcrumb" aria-label="栏目位置">
        <span class="breadcrumb-label">当前位置：</span>
        <router-link to="/">网站首页</router-link>
        <template v-for="item in breadcrumbs" :key="item.id">
          <span class="breadcrumb-separator">›</span>
          <router-link :to="item.alias ? `/column/${item.alias}` : `/columns/${item.id}`">{{ item.name }}</router-link>
        </template>
      </nav>

      <p v-if="loading" class="public-state">正在加载栏目…</p>
      <template v-else-if="column">
        <section class="detail-card column-detail-card">
          <header class="detail-section-title">
            <h1>{{ column.name }}</h1>
          </header>
          <div class="column-content">
            <div class="column-list" data-testid="column-articles">
              <article v-for="article in articles" :key="article.id">
                <router-link
                  class="column-list-link"
                  :data-testid="`column-article-${article.id}`"
                  :to="`/article/${article.id}`"
                >
                  <span class="column-list-icon" aria-hidden="true" />
                  <span class="column-list-copy">
                    <strong class="column-list-title">{{ article.title }}</strong>
                    <time v-if="article.publishDate">{{ article.publishDate }}</time>
                  </span>
                </router-link>
              </article>
              <p v-if="!articles.length" class="empty-item">当前栏目暂无已发布内容。</p>
            </div>

            <div v-if="total > size" class="pagination-wrap">
              <span class="pagination-summary">共{{ pageCount }}页，{{ total }}条记录</span>
              <div class="pagination" aria-label="栏目分页">
                <button class="page-button page-arrow" type="button" :disabled="!hasPrevious" aria-label="上一页" @click="go(page - 1)">‹</button>
                <button
                  v-for="pageIndex in visiblePages"
                  :key="pageIndex"
                  class="page-button"
                  :class="{ active: pageIndex === page }"
                  type="button"
                  :aria-current="pageIndex === page ? 'page' : undefined"
                  :aria-label="`第 ${pageIndex + 1} 页`"
                  @click="go(pageIndex)"
                >{{ pageIndex + 1 }}</button>
                <button class="page-button page-arrow" type="button" :disabled="!hasNext" aria-label="下一页" @click="go(page + 1)">›</button>
                <select class="page-size-select" :value="size" aria-label="每页条数" @change="changeSize">
                  <option v-for="option in allowedSizes" :key="option" :value="option">{{ option }}条/页</option>
                </select>
                <label class="page-jump">
                  <span>跳至</span>
                  <input v-model="jumpPage" type="number" min="1" :max="pageCount" aria-label="跳转页码" @keyup.enter="jump">
                  <span>页</span>
                </label>
                <button class="page-jump-submit" type="button" aria-label="跳转" @click="jump">确定</button>
              </div>
            </div>
          </div>
        </section>
      </template>
      <p v-else class="public-state error-text">{{ error }}</p>
    </div>
  </main>
  <PublicSiteFooter />
</template>
