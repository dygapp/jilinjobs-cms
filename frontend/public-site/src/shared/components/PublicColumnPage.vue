<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { PublicArticleSummary } from '../api/articles'

type BreadcrumbItem = {
  label: string
  to?: string
}

const props = withDefaults(defineProps<{
  title: string
  breadcrumbs: BreadcrumbItem[]
  articles: PublicArticleSummary[]
  total: number
  page: number
  size: number
  loading: boolean
  error: string
  articleBasePath: string
  testIdPrefix?: string
  allowedSizes?: number[]
}>(), {
  testIdPrefix: 'column',
  allowedSizes: () => [10, 20, 30],
})

const emit = defineEmits<{
  (event: 'page-change', page: number): void
  (event: 'size-change', size: number): void
}>()

const jumpPage = ref('')
const pageCount = computed(() => Math.max(1, Math.ceil(props.total / props.size)))
const hasPrevious = computed(() => props.page > 0)
const hasNext = computed(() => props.page + 1 < pageCount.value)
const visiblePages = computed(() => {
  const count = pageCount.value
  if (count <= 9) return Array.from({ length: count }, (_, index) => index)
  const start = Math.min(Math.max(props.page - 4, 0), count - 9)
  return Array.from({ length: 9 }, (_, index) => start + index)
})

watch(() => [props.page, props.size], () => {
  jumpPage.value = ''
})

function isExternal(article: PublicArticleSummary) {
  return article.articleType === 'EXTERNAL_LINK' && Boolean(article.externalUrl)
}

function articlePath(article: PublicArticleSummary) {
  const base = props.articleBasePath.endsWith('/') ? props.articleBasePath.slice(0, -1) : props.articleBasePath
  return `${base}/${article.id}`
}

function go(nextPage: number) {
  emit('page-change', Math.min(Math.max(nextPage, 0), pageCount.value - 1))
}

function changeSize(event: Event) {
  const nextSize = Number((event.target as HTMLSelectElement).value)
  if (props.allowedSizes.includes(nextSize)) emit('size-change', nextSize)
}

function jump() {
  const target = Number(jumpPage.value)
  if (!Number.isInteger(target) || target < 1) return
  go(Math.min(target, pageCount.value) - 1)
}
</script>

<template>
  <main
    class="shared-column-page-shell"
    data-component="public-column-page"
    :data-testid="`${testIdPrefix}-page`"
  >
    <div class="shared-column-page-width">
      <nav class="shared-column-breadcrumb" aria-label="栏目位置">
        <span class="shared-column-breadcrumb-label">当前位置：</span>
        <template v-for="(item, index) in breadcrumbs" :key="`${item.label}-${index}`">
          <span v-if="index > 0" class="shared-column-breadcrumb-separator">›</span>
          <router-link v-if="item.to" :to="item.to">{{ item.label }}</router-link>
          <span v-else>{{ item.label }}</span>
        </template>
      </nav>

      <p v-if="loading && !title" class="shared-column-state">正在加载栏目…</p>
      <section
        v-else-if="title"
        class="shared-column-card"
        :aria-busy="loading"
      >
        <header class="shared-column-section-title">
          <h1>{{ title }}</h1>
        </header>
        <div class="shared-column-content">
          <div class="shared-column-list" :data-testid="`${testIdPrefix}-articles`">
            <article v-for="article in articles" :key="article.id">
              <a
                v-if="isExternal(article)"
                class="shared-column-list-link"
                :data-testid="`${testIdPrefix}-article-${article.id}`"
                :href="article.externalUrl!"
                target="_blank"
                rel="noopener noreferrer"
              >
                <span class="shared-column-list-icon" aria-hidden="true" />
                <span class="shared-column-list-copy">
                  <strong class="shared-column-list-title">{{ article.title }}</strong>
                  <time v-if="article.publishDate">{{ article.publishDate }}</time>
                </span>
              </a>
              <router-link
                v-else
                class="shared-column-list-link"
                :data-testid="`${testIdPrefix}-article-${article.id}`"
                :to="articlePath(article)"
              >
                <span class="shared-column-list-icon" aria-hidden="true" />
                <span class="shared-column-list-copy">
                  <strong class="shared-column-list-title">{{ article.title }}</strong>
                  <time v-if="article.publishDate">{{ article.publishDate }}</time>
                </span>
              </router-link>
            </article>
            <p v-if="!articles.length" class="shared-column-empty">当前栏目暂无已发布内容。</p>
          </div>

          <div v-if="total > size" class="shared-column-pagination-wrap">
            <span class="shared-column-pagination-summary">共{{ pageCount }}页，{{ total }}条记录</span>
            <div class="shared-column-pagination" aria-label="栏目分页">
              <button class="shared-column-page-button shared-column-page-arrow" type="button" :disabled="!hasPrevious" aria-label="上一页" @click="go(page - 1)">‹</button>
              <button
                v-for="pageIndex in visiblePages"
                :key="pageIndex"
                class="shared-column-page-button"
                :class="{ active: pageIndex === page }"
                type="button"
                :aria-current="pageIndex === page ? 'page' : undefined"
                :aria-label="`第 ${pageIndex + 1} 页`"
                @click="go(pageIndex)"
              >{{ pageIndex + 1 }}</button>
              <button class="shared-column-page-button shared-column-page-arrow" type="button" :disabled="!hasNext" aria-label="下一页" @click="go(page + 1)">›</button>
              <select class="shared-column-page-size-select" :value="size" aria-label="每页条数" @change="changeSize">
                <option v-for="option in allowedSizes" :key="option" :value="option">{{ option }}条/页</option>
              </select>
              <label class="shared-column-page-jump">
                <span>跳至</span>
                <input v-model="jumpPage" type="number" min="1" :max="pageCount" aria-label="跳转页码" @keyup.enter="jump">
                <span>页</span>
              </label>
              <button class="shared-column-page-jump-submit" type="button" aria-label="跳转" @click="jump">确定</button>
            </div>
          </div>
        </div>
      </section>
      <p v-else class="shared-column-state shared-column-state-error">{{ error }}</p>
    </div>
  </main>
</template>

<style>
.shared-column-page-shell{min-height:520px;padding:0 0 30px;background:#f5f8fc}
.shared-column-page-width{width:min(1200px,calc(100% - 32px));min-width:0;margin:0 auto}
.shared-column-breadcrumb{min-height:57px;display:flex;align-items:center;flex-wrap:wrap;gap:8px;margin:0;color:#6c757d;font-size:13px;line-height:1.4}
.shared-column-breadcrumb-label{color:#6c757d}
.shared-column-breadcrumb a{color:#515c6b;text-decoration:none}
.shared-column-breadcrumb a:hover{color:#005cd4}
.shared-column-breadcrumb-separator{color:#9aa7b5}
.shared-column-card{background:#fff;padding:20px 0;min-width:0}
.shared-column-section-title{height:19px;display:flex;align-items:center;padding:0 30px;margin:0}
.shared-column-section-title::after{content:"";height:1px;flex:1;margin-left:16px;background:#ebeef2}
.shared-column-section-title h1{flex:none;margin:0;color:#323b47;font-size:16px;font-weight:700;line-height:19px}
.shared-column-content{padding:30px}
.shared-column-list{border:1px solid #ebeef2;border-bottom:0}
.shared-column-list article{height:81px;margin:0;padding:0;border-bottom:1px solid #ebeef2}
.shared-column-list-link{height:80px;display:flex;align-items:center;padding:14px 28px;color:#323b47;text-decoration:none;overflow:hidden}
.shared-column-list-link:hover{color:#005cd4;background:#fbfdff}
.shared-column-list-icon{flex:0 0 52px;width:52px;height:52px;margin-right:18px;background:url('/static/icons/list-item.png') center/52px 52px no-repeat}
.shared-column-list-copy{min-width:0;display:flex;flex-direction:column;justify-content:center;gap:3px}
.shared-column-list-title{display:block;max-width:100%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:#323b47;font-size:16px;font-weight:700;line-height:24px}
.shared-column-list-link:hover .shared-column-list-title{color:#005cd4}
.shared-column-list time{color:#515c6b;font-size:14px;line-height:18px}
.shared-column-empty{min-height:80px;display:grid;place-items:center;margin:0;border-bottom:1px solid #ebeef2;color:#8491a1}
.shared-column-pagination-wrap{display:flex;align-items:center;justify-content:space-between;gap:24px;margin-top:30px;min-height:32px;color:#515c6b;font-size:14px}
.shared-column-pagination-summary{flex:0 0 auto;color:#8491a1}
.shared-column-pagination{display:flex;align-items:center;justify-content:flex-end;gap:8px;flex-wrap:wrap;margin:0}
.shared-column-page-button{width:32px;height:32px;padding:0;border:1px solid #cbd7e3;border-radius:4px;background:#fff;color:#515c6b;cursor:pointer}
.shared-column-page-button:hover:not(:disabled){border-color:#006af5;color:#006af5}
.shared-column-page-button.active{border-color:#006af5;background:#006af5;color:#fff}
.shared-column-page-button:disabled{opacity:.45;cursor:default}
.shared-column-page-arrow{font-size:20px;line-height:28px}
.shared-column-page-size-select{height:32px;padding:0 28px 0 10px;border:1px solid #cbd7e3;border-radius:4px;background:#fff;color:#515c6b}
.shared-column-page-jump{height:32px;display:flex;align-items:center;gap:6px;white-space:nowrap}
.shared-column-page-jump input{width:42px;height:32px;padding:0 5px;border:1px solid #cbd7e3;border-radius:4px;text-align:center;color:#515c6b}
.shared-column-page-jump-submit{height:32px;padding:0 9px;border:1px solid #cbd7e3;border-radius:4px;background:#fff;color:#515c6b;cursor:pointer}
.shared-column-page-jump-submit:hover{border-color:#006af5;color:#006af5}
.shared-column-state{min-height:200px;display:grid;place-items:center;margin:0;padding:40px 30px;background:#fff;color:#8491a1;text-align:center}
.shared-column-state-error{color:#b33}
@media(max-width:760px){
  .shared-column-page-shell{padding-bottom:20px}
  .shared-column-page-width{width:min(100% - 24px,1200px)}
  .shared-column-breadcrumb{min-height:48px;padding:8px 0;font-size:12px}
  .shared-column-card{padding-top:16px}
  .shared-column-section-title{height:auto;min-height:19px;padding:0 16px}
  .shared-column-section-title h1{font-size:15px}
  .shared-column-content{padding:16px}
  .shared-column-list article{height:auto;min-height:81px}
  .shared-column-list-link{height:auto;min-height:80px;padding:12px 14px}
  .shared-column-list-icon{flex-basis:44px;width:44px;height:44px;margin-right:12px;background-size:44px 44px}
  .shared-column-list-title{font-size:15px;white-space:normal;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:2}
  .shared-column-list time{font-size:12px}
  .shared-column-pagination-wrap{align-items:flex-start;flex-direction:column;gap:12px}
  .shared-column-pagination{justify-content:flex-start;gap:6px}
  .shared-column-page-button{width:30px;height:30px}
  .shared-column-page-size-select,.shared-column-page-jump,.shared-column-page-jump input,.shared-column-page-jump-submit{height:30px}
}
</style>
