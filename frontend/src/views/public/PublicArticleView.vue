<script setup lang="ts">
import QRCode from 'qrcode'
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  getPublicArticle,
  publicAttachmentUrl,
  publicBodyHtml,
  type PublicArticleDetail,
} from '../../api/articles'
import { setPageMeta, summarizeHtml } from '../../seo'

const route = useRoute()
const article = ref<PublicArticleDetail | null>(null)
const loading = ref(false)
const error = ref('')
const articleUrl = ref('')
const qrCodeUrl = ref('')
const copyMessage = ref('')
const renderedBody = computed(() => article.value ? publicBodyHtml(article.value) : '')

watch(
  () => route.params.id,
  async (value) => {
    const id = Number(value)
    article.value = null
    articleUrl.value = ''
    qrCodeUrl.value = ''
    copyMessage.value = ''
    error.value = ''
    setPageMeta({
      title: '文章',
      description: '浏览吉林就业中心主站已发布信息。',
    })
    if (!Number.isInteger(id) || id <= 0) {
      error.value = '文章不可用或不存在'
      setPageMeta({ title: '文章不可用', description: error.value })
      return
    }

    loading.value = true
    try {
      const current = await getPublicArticle(id)
      article.value = current
      articleUrl.value = window.location.href
      qrCodeUrl.value = await QRCode.toDataURL(articleUrl.value, { width: 180, margin: 1 })
      setPageMeta({
        title: current.title,
        description: summarizeHtml(
          current.bodyHtml,
          [current.source, current.publishDate].filter(Boolean).join(' · '),
        ),
      })
    } catch {
      error.value = '文章不可用或不存在'
      setPageMeta({ title: '文章不可用', description: error.value })
    } finally {
      loading.value = false
    }
  },
  { immediate: true },
)

async function copyArticleLink() {
  try {
    await navigator.clipboard.writeText(articleUrl.value)
    copyMessage.value = '链接已复制'
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = articleUrl.value
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    const copied = document.execCommand('copy')
    textarea.remove()
    copyMessage.value = copied ? '链接已复制' : '复制失败，请手动复制浏览器地址'
  }
}

function formatFileSize(sizeBytes: number): string {
  if (sizeBytes < 1024) return `${sizeBytes} B`
  if (sizeBytes < 1024 * 1024) return `${(sizeBytes / 1024).toFixed(1)} KB`
  return `${(sizeBytes / 1024 / 1024).toFixed(1)} MB`
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
        <section v-if="article.attachments.length" class="article-attachments" data-testid="public-attachments">
          <h2>附件下载</h2>
          <a
            v-for="attachment in article.attachments"
            :key="attachment.id"
            :data-testid="`public-attachment-${attachment.id}`"
            :href="publicAttachmentUrl(attachment.id)"
          >
            <span>{{ attachment.originalFilename }}</span>
            <small>{{ formatFileSize(attachment.sizeBytes) }}</small>
          </a>
        </section>
        <section class="article-share" aria-label="内容分享">
          <div>
            <button data-testid="copy-article-link" type="button" @click="copyArticleLink">复制链接</button>
            <p v-if="copyMessage" class="copy-message" role="status">{{ copyMessage }}</p>
          </div>
          <figure v-if="qrCodeUrl">
            <img data-testid="article-qrcode" :src="qrCodeUrl" alt="当前页面访问二维码">
            <figcaption>扫码访问当前页面</figcaption>
          </figure>
        </section>
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
  overflow-wrap: anywhere;
}
.article-body :deep(img) {
  display: block;
  max-width: 100%;
  height: auto;
  margin: 22px auto;
}
.article-body :deep(video),
.article-body :deep(iframe) {
  max-width: 100%;
}
.article-body :deep(pre) {
  max-width: 100%;
  overflow-x: auto;
}
.article-body :deep(table) {
  display: block;
  max-width: 100%;
  overflow-x: auto;
}
.article-attachments {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #dce8ef;
}
.article-attachments h2 {
  margin: 0 0 14px;
  color: #18313e;
  font-size: 20px;
}
.article-attachments a {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #eef7fa;
  color: #176d88;
  text-decoration: none;
}
.article-attachments small {
  color: #6c7d87;
}
.article-share {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 28px;
  margin-top: 36px;
  padding: 22px;
  border-radius: 12px;
  background: #f5f9fb;
}
.article-share button {
  padding: 10px 18px;
  border: 0;
  border-radius: 6px;
  background: #147b95;
  color: white;
  cursor: pointer;
}
.copy-message {
  margin: 10px 0 0;
  color: #287047;
}
.article-share figure {
  margin: 0;
  text-align: center;
}
.article-share img {
  display: block;
  width: 150px;
  height: 150px;
}
.article-share figcaption {
  margin-top: 6px;
  color: #6c7d87;
  font-size: 13px;
}

@media (max-width: 720px) {
  .article-detail {
    padding: 20px 12px 48px;
  }

  .article-heading {
    padding-top: 12px;
    text-align: left;
  }

  .article-heading h1 {
    font-size: 26px;
  }

  .article-meta,
  .article-share {
    align-items: flex-start;
    flex-direction: column;
  }

  .article-meta {
    gap: 6px;
  }

  .article-attachments a {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
    overflow-wrap: anywhere;
  }

  .article-share figure {
    align-self: center;
  }
}
</style>
