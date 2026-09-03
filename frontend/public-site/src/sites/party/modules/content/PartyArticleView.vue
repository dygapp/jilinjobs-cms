<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  getPublicArticle,
  publicAttachmentUrl,
  publicBodyHtml,
  type PublicArticleDetail,
} from '../../../../shared/api/articles'
import { setPageMeta, summarizeHtml } from '../../../../shared/seo'
import { isPartyColumnAlias } from '../../app/partyContext'

const route = useRoute()
const article = ref<PublicArticleDetail | null>(null)
const loading = ref(false)
const error = ref('')
const renderedBody = computed(() => article.value ? publicBodyHtml(article.value) : '')

watch(() => route.params.id, load, { immediate: true })

async function load(value: unknown) {
  const id = Number(value)
  article.value = null
  error.value = ''
  if (!Number.isInteger(id) || id <= 0) {
    error.value = '文章不可用或不存在'
    return
  }

  loading.value = true
  try {
    const item = await getPublicArticle(id)
    if (!isPartyColumnAlias(item.columnAlias)) {
      error.value = '文章不可用或不存在'
      return
    }
    if (item.articleType === 'EXTERNAL_LINK' && item.externalUrl) {
      window.location.replace(item.externalUrl)
      return
    }
    article.value = item
    setPageMeta({
      title: item.title,
      description: summarizeHtml(item.bodyHtml, [item.source, item.publishDate].filter(Boolean).join(' · ')),
    })
  } catch {
    error.value = '文章不可用或不存在'
  } finally {
    loading.value = false
  }
}

function size(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1048576).toFixed(1)} MB`
}
</script>

<template>
  <main class="party-main party-page">
    <div class="party-width party-page-frame">
      <p v-if="loading" class="party-state">正在加载文章…</p>
      <template v-else-if="article">
        <nav class="party-breadcrumb" aria-label="当前位置">
          <router-link to="/party/">中心党建</router-link>
          <span>›</span>
          <router-link :to="`/party/column/${article.columnAlias}`">{{ article.columnName }}</router-link>
          <span>›</span><span>详情</span>
        </nav>

        <article class="party-content-card party-article-card" data-testid="party-article-page">
          <header class="party-article-heading">
            <h1 data-testid="party-article-title">{{ article.title }}</h1>
            <div class="party-article-meta">
              <span v-if="article.source">信息来源：{{ article.source }}</span>
              <time v-if="article.publishDate">发布时间：{{ article.publishDate }}</time>
            </div>
          </header>
          <div class="party-article-body" data-testid="party-article-body" v-html="renderedBody" />
          <section v-if="article.attachments.length" class="party-attachments">
            <h2>附件下载</h2>
            <a v-for="file in article.attachments" :key="file.id" :href="publicAttachmentUrl(file.id)">
              <span>{{ file.originalFilename }}</span><small>{{ size(file.sizeBytes) }}</small>
            </a>
          </section>
        </article>
      </template>
      <p v-else class="party-state party-state-error" data-testid="party-article-unavailable">{{ error }}</p>
    </div>
  </main>
</template>
