<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listPublicArticles, type PublicArticleSummary } from '../../api/articles'
import { listPublicNavigations, type PublicNavigation } from '../../api/navigation'

const items = ref<PublicNavigation[]>([])
const articles = ref<PublicArticleSummary[]>([])
const loading = ref(true)
const error = ref('')

const mainItems = computed(() => items.value.filter((item) => item.position === 'MAIN'))
const serviceItems = computed(() => items.value.filter((item) => item.position === 'SERVICE'))
const siteGroups = computed(() => {
  const groups = new Map<string, PublicNavigation[]>()
  items.value
    .filter((item) => item.position === 'SITE')
    .forEach((item) => {
      const key = item.category || '网站导航'
      const current = groups.get(key) ?? []
      current.push(item)
      groups.set(key, current)
    })
  return Array.from(groups, ([name, links]) => ({ name, links }))
})
const articleGroups = computed(() => {
  const groups = new Map<number, { columnId: number; columnName: string; articles: PublicArticleSummary[] }>()
  articles.value.forEach((article) => {
    const current = groups.get(article.columnId) ?? {
      columnId: article.columnId,
      columnName: article.columnName,
      articles: [],
    }
    current.articles.push(article)
    groups.set(article.columnId, current)
  })
  return Array.from(groups.values())
})

onMounted(async () => {
  try {
    const [navigationRows, articlePage] = await Promise.all([
      listPublicNavigations(),
      listPublicArticles(null, 0, 20),
    ])
    items.value = navigationRows
    articles.value = articlePage.items
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '公开内容加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <main class="public-shell">
    <header class="public-header">
      <div class="brand-block">
        <span class="brand-mark">吉林就业</span>
        <strong>吉林省智慧就业云平台</strong>
      </div>
      <nav class="main-navigation" aria-label="主导航">
        <template v-for="item in mainItems" :key="item.id">
          <a
            v-if="item.external"
            :data-testid="`public-nav-${item.id}`"
            :href="item.href"
            target="_blank"
            rel="noopener noreferrer"
          >{{ item.name }}</a>
          <router-link v-else :data-testid="`public-nav-${item.id}`" :to="item.href">{{ item.name }}</router-link>
        </template>
      </nav>
    </header>

    <section class="public-hero">
      <p class="eyebrow">中心主站</p>
      <h1>就业信息与公共服务入口</h1>
      <p>浏览已发布的就业信息，并通过栏目和服务导航进入所需内容。</p>
    </section>

    <p v-if="loading" class="public-state">正在加载公开内容…</p>
    <p v-else-if="error" class="public-state error-text">{{ error }}</p>

    <section v-if="articleGroups.length" class="public-section" data-testid="public-content">
      <div class="section-heading">
        <span>信息发布</span>
        <h2>最新发布</h2>
      </div>
      <div class="article-groups">
        <section v-for="group in articleGroups" :key="group.columnId" class="article-group">
          <h3><router-link :to="`/columns/${group.columnId}`">{{ group.columnName }}</router-link></h3>
          <ul>
            <li v-for="article in group.articles" :key="article.id">
              <router-link :data-testid="`public-article-${article.id}`" :to="`/articles/${article.id}`">{{ article.title }}</router-link>
              <time v-if="article.publishDate">{{ article.publishDate }}</time>
            </li>
          </ul>
        </section>
      </div>
    </section>

    <section v-if="serviceItems.length" class="public-section">
      <div class="section-heading">
        <span>服务入口</span>
        <h2>常用服务</h2>
      </div>
      <div class="service-grid">
        <template v-for="item in serviceItems" :key="item.id">
          <a v-if="item.external" :href="item.href" target="_blank" rel="noopener noreferrer">{{ item.name }}</a>
          <router-link v-else :to="item.href">{{ item.name }}</router-link>
        </template>
      </div>
    </section>

    <section v-if="siteGroups.length" class="public-section site-navigation">
      <div class="section-heading">
        <span>网站导航</span>
        <h2>相关站点</h2>
      </div>
      <div class="site-groups">
        <div v-for="group in siteGroups" :key="group.name" class="site-group">
          <h3>{{ group.name }}</h3>
          <div class="site-links">
            <template v-for="item in group.links" :key="item.id">
              <a v-if="item.external" :href="item.href" target="_blank" rel="noopener noreferrer">{{ item.name }}</a>
              <router-link v-else :to="item.href">{{ item.name }}</router-link>
            </template>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.article-groups {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}
.article-group {
  padding: 20px;
  border: 1px solid #d9e8f5;
  border-radius: 12px;
  background: white;
}
.article-group h3 {
  margin: 0 0 14px;
}
.article-group h3 a {
  color: #156f8f;
  text-decoration: none;
}
.article-group ul {
  list-style: none;
  margin: 0;
  padding: 0;
}
.article-group li {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 10px 0;
  border-top: 1px solid #edf3f7;
}
.article-group li a {
  color: #1f3340;
  text-decoration: none;
}
.article-group time {
  flex: 0 0 auto;
  color: #72828c;
  font-size: 13px;
}
</style>
