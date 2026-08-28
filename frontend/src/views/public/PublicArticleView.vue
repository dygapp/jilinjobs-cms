<script setup lang="ts">
import QRCode from 'qrcode'
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getPublicArticle, publicAttachmentUrl, publicBodyHtml, type PublicArticleDetail } from '../../api/articles'
import PublicSiteHeader from '../../components/PublicSiteHeader.vue'
import PublicSiteFooter from '../../components/PublicSiteFooter.vue'
import { setPageMeta, summarizeHtml } from '../../seo'

const route = useRoute()
const article = ref<PublicArticleDetail | null>(null)
const loading = ref(false)
const error = ref('')
const articleUrl = ref('')
const qrCodeUrl = ref('')
const copyMessage = ref('')
const renderedBody = computed(() => article.value ? publicBodyHtml(article.value) : '')

watch(() => route.params.id, async value => {
  const id = Number(value)
  article.value = null
  error.value = ''
  copyMessage.value = ''
  if (!Number.isInteger(id) || id <= 0) {
    error.value = '文章不可用或不存在'
    return
  }
  loading.value = true
  try {
    const item = await getPublicArticle(id)
    if (item.articleType === 'EXTERNAL_LINK' && item.externalUrl) {
      window.location.replace(item.externalUrl)
      return
    }
    article.value = item
    articleUrl.value = window.location.origin + `/article/${id}`
    qrCodeUrl.value = await QRCode.toDataURL(articleUrl.value, { width: 180, margin: 1 })
    setPageMeta({ title: item.title, description: summarizeHtml(item.bodyHtml, [item.source, item.publishDate].filter(Boolean).join(' · ')) })
  } catch {
    error.value = '文章不可用或不存在'
  } finally {
    loading.value = false
  }
}, { immediate: true })

async function copy() {
  try {
    await navigator.clipboard.writeText(articleUrl.value)
    copyMessage.value = '链接已复制'
  } catch {
    copyMessage.value = '复制失败，请手动复制浏览器地址'
  }
}

function size(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1048576).toFixed(1)} MB`
}
</script>

<template>
  <PublicSiteHeader />
  <main class="public-page-shell">
    <div class="site-width public-page-frame">
      <p v-if="loading" class="public-state">正在加载文章…</p>
      <template v-else-if="article">
        <nav class="breadcrumb" aria-label="文章位置">
          <span class="breadcrumb-label">当前位置：</span>
          <router-link to="/">网站首页</router-link>
          <span class="breadcrumb-separator">›</span>
          <router-link :to="article.columnAlias ? `/column/${article.columnAlias}` : `/columns/${article.columnId}`">{{ article.columnName }}</router-link>
          <span class="breadcrumb-separator">›</span>
          <span>详情</span>
        </nav>

        <article class="detail-card article-detail-card">
          <header class="detail-section-title">
            <h2>{{ article.columnName }}</h2>
          </header>
          <div class="article-content">
            <header class="article-heading">
              <h1 data-testid="public-article-title">{{ article.title }}</h1>
              <div class="article-meta">
                <span v-if="article.source">信息来源：{{ article.source }}</span>
                <time v-if="article.publishDate">发布时间：{{ article.publishDate }}</time>
              </div>
            </header>

            <div class="article-body rich-content" data-testid="public-article-body" v-html="renderedBody" />

            <section v-if="article.attachments.length" class="article-attachments" data-testid="public-attachments">
              <h2>附件下载</h2>
              <a v-for="file in article.attachments" :key="file.id" :data-testid="`public-attachment-${file.id}`" :href="publicAttachmentUrl(file.id)">
                <span>{{ file.originalFilename }}</span>
                <small>{{ size(file.sizeBytes) }}</small>
              </a>
            </section>

            <section class="article-share" aria-label="文章分享">
              <div class="article-share-action">
                <button data-testid="copy-article-link" type="button" @click="copy">复制链接</button>
                <p v-if="copyMessage" role="status" aria-live="polite">{{ copyMessage }}</p>
              </div>
              <figure v-if="qrCodeUrl">
                <img data-testid="article-qrcode" :src="qrCodeUrl" alt="当前页面访问二维码">
                <figcaption>扫码访问当前页面</figcaption>
              </figure>
            </section>
          </div>
        </article>
      </template>
      <p v-else class="public-state error-text" data-testid="public-article-unavailable">{{ error }}</p>
    </div>
  </main>
  <PublicSiteFooter />
</template>
