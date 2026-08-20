<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getPublicArticle, publicBodyHtml, type PublicArticleDetail } from '../../api/articles'

const route = useRoute()
const article = ref<PublicArticleDetail | null>(null)
const loading = ref(false)
const error = ref('')
const renderedBody = computed(() => article.value ? publicBodyHtml(article.value) : '')

watch(
  () => route.params.id,
  async (value) => {
    const id = Number(value)
    article.value = null
    error.value = ''
    if (!Number.isInteger(id) || id <= 0) {
      error.value = '文章不可用或不存在'
      return
    }

    loading.value = true
    try {
      article.value = await getPublicArticle(id)
    } catch {
      error.value = '文章不可用或不存在'
    } finally {
      loading.value = false
    }
  },
  { immediate: true },
)
</script>

<template>
  <main class="public-shell">
    <header class="public-header compact-header">
      <router-link class="brand-block brand-link" to="/">
        <span class="brand-mark">吉林就业</span>
        <strong>吉林省智慧就业云平台</strong>
      </router-link>
    </header>

    <article class="article-detail">
      <p v-if="loading" class="public-state">正在加载文章…</p>
      <template v-else-if="article">
        <p class="breadcrumb">
          <router-link to="/">首页</router-link>
          <span>/</span>
          <router-link :to="`/columns/${article.columnId}`">{{ article.columnName }}</router-link>
          <span>/</span>
          <span>正文</span>
        </p>
        <header class="article-heading">
          <h1 data-testid="public-article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <span v-if="article.source">来源：{{ article.source }}</span>
            <time v-if="article.publishDate">发布日期：{{ article.publishDate }}</time>
          </div>
        </header>
        <div class="article-body" data-testid="public-article-body" v-html="renderedBody" />
      </template>
      <p v-else class="public-state error-text" data-testid="public-article-unavailable">{{ error }}</p>
    </article>
  </main>
</template>

<style scoped>
.article-detail {
  max-width: 900px;
  margin: 0 auto;
  padding: 36px 24px 64px;
}
.article-heading {
  padding: 26px 0;
  border-bottom: 1px solid #dce8ef;
  text-align: center;
}
.article-heading h1 {
  margin: 0;
  color: #18313e;
  font-size: 32px;
  line-height: 1.35;
}
.article-meta {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 16px;
  color: #6c7d87;
  font-size: 14px;
}
.article-body {
  padding-top: 30px;
  color: #273d49;
  font-size: 17px;
  line-height: 1.9;
}
.article-body :deep(img) {
  display: block;
  max-width: 100%;
  height: auto;
  margin: 22px auto;
}
</style>
