<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listPublicArticles, type PublicArticleSummary } from '../../api/articles'
import { getPublicColumn, type PublicColumn } from '../../api/columns'
import { setPageMeta } from '../../seo'

const route = useRoute()
const router = useRouter()
const column = ref<PublicColumn | null>(null)
const breadcrumbs = ref<PublicColumn[]>([])
const articles = ref<PublicArticleSummary[]>([])
const total = ref(0)
const page = ref(0)
const size = 10
const error = ref('')
const loading = ref(false)

const hasPrevious = computed(() => page.value > 0)
const hasNext = computed(() => (page.value + 1) * size < total.value)

watch(
  () => [route.params.id, route.query.page],
  load,
  { immediate: true },
)

async function load() {
  const id = Number(route.params.id)
  const requestedPage = Math.max(0, Number(route.query.page ?? 0) || 0)
  column.value = null
  breadcrumbs.value = []
  articles.value = []
  total.value = 0
  error.value = ''
  setPageMeta({
    title: '栏目',
    description: '浏览吉林就业中心主站栏目中的已发布信息。',
  })
  if (!Number.isInteger(id) || id <= 0) {
    error.value = '栏目地址无效'
    setPageMeta({ title: '栏目不可用', description: error.value })
    return
  }

  loading.value = true
  try {
    const current = await getPublicColumn(id)
    const trail: PublicColumn[] = [current]
    const seen = new Set<number>([current.id])
    let parentId = current.parentId
    while (parentId != null && !seen.has(parentId)) {
      const parent = await getPublicColumn(parentId)
      trail.unshift(parent)
      seen.add(parent.id)
      parentId = parent.parentId
    }
    const articlePage = await listPublicArticles(id, requestedPage, size)
    column.value = current
    breadcrumbs.value = trail
    articles.value = articlePage.items
    total.value = articlePage.total
    page.value = articlePage.page
    setPageMeta({
      title: current.name,
      description: `浏览吉林就业中心主站“${current.name}”栏目的已发布信息。`,
    })
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '栏目加载失败'
    setPageMeta({ title: '栏目不可用', description: error.value })
  } finally {
    loading.value = false
  }
}

function goToPage(target: number) {
  void router.push({ path: route.path, query: target > 0 ? { page: String(target) } : {} })
}
</script>

<template>
  <main class="public-shell">
    <header class="public-header compact-header">
      <router-link class="brand-block brand-link" to="/">
        <span class="brand-mark">吉林就业</span>
        <strong>吉林省智慧就业云平台</strong>
      </router-link>
    </header>

    <section class="column-entry">
      <nav class="breadcrumb" aria-label="栏目位置">
        <router-link to="/">首页</router-link>
        <template v-for="item in breadcrumbs" :key="item.id">
          <span>/</span>
          <router-link :to="`/columns/${item.id}`">{{ item.name }}</router-link>
        </template>
      </nav>
      <p v-if="loading" class="public-state">正在加载栏目…</p>
      <div v-else-if="column">
        <p class="eyebrow">栏目</p>
        <h1>{{ column.name }}</h1>
        <div class="column-articles" data-testid="column-articles">
          <article v-for="article in articles" :key="article.id" class="column-article-row">
            <router-link :data-testid="`column-article-${article.id}`" :to="`/articles/${article.id}`">{{ article.title }}</router-link>
            <time v-if="article.publishDate">{{ article.publishDate }}</time>
          </article>
          <p v-if="articles.length === 0" class="public-state">当前栏目暂无已发布内容。</p>
        </div>
        <div v-if="total > size" class="pagination" aria-label="栏目分页">
          <button type="button" :disabled="!hasPrevious" @click="goToPage(page - 1)">上一页</button>
          <span>第 {{ page + 1 }} 页</span>
          <button type="button" :disabled="!hasNext" @click="goToPage(page + 1)">下一页</button>
        </div>
      </div>
      <p v-else class="public-state error-text">{{ error }}</p>
    </section>
  </main>
</template>

<style scoped>
.column-articles {
  margin-top: 28px;
  border-top: 1px solid #dbe7ee;
}
.column-article-row {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 16px 0;
  border-bottom: 1px solid #e9f0f4;
}
.column-article-row a {
  color: #203846;
  text-decoration: none;
  font-size: 17px;
}
.column-article-row time {
  flex: 0 0 auto;
  color: #788892;
}
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 28px;
}
.pagination button {
  padding: 8px 14px;
  border: 1px solid #b9ccd8;
  border-radius: 6px;
  background: white;
  cursor: pointer;
}
.pagination button:disabled {
  cursor: default;
  opacity: 0.45;
}

@media (max-width: 720px) {
  .column-article-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }

  .pagination {
    gap: 10px;
  }
}
</style>
