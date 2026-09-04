<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listPublicArticles, type PublicArticleSummary } from '../../../../shared/api/articles'
import { getPublicCmsListByCode, publicListImageUrl, type CmsListItem } from '../../../../shared/api/lists'
import { listPublicSiteConfig } from '../../../../shared/api/siteConfig'
import { useContentCarousel } from '../../../../shared/carousel/useContentCarousel'
import { setPageMeta } from '../../../../shared/seo'
import {
  PARTY_CAROUSEL_CODE,
  PARTY_HOME_COLUMN_ALIASES,
  loadPartyColumns,
  type PartyHomeColumnAlias,
} from '../../app/partyContext'

const SECTION_SIZE = 6
const DEFAULT_CAROUSEL_INTERVAL_SECONDS = 4
const DEFAULT_CAROUSEL_MAX_ITEMS = 5
const CAROUSEL_INTERVAL_KEY = 'CAROUSEL_INTERVAL_SECONDS'
const CAROUSEL_MAX_ITEMS_KEY = 'CAROUSEL_MAX_ITEMS'
const studyAliases: PartyHomeColumnAlias[] = ['party-rules', 'party-study']
const sectionArticles = ref<Record<PartyHomeColumnAlias, PublicArticleSummary[]>>({
  'party-voice': [],
  'party-work': [],
  'party-rules': [],
  'party-study': [],
})
const carouselItems = ref<CmsListItem[]>([])
const carouselIntervalSeconds = ref(DEFAULT_CAROUSEL_INTERVAL_SECONDS)
const carouselMaxItems = ref(DEFAULT_CAROUSEL_MAX_ITEMS)
const loading = ref(true)
const error = ref('')

const {
  activeIndex: activeSlide,
  activeItem: activeCarouselItem,
  visibleItems: visibleCarouselItems,
  pause: pauseCarousel,
  resume: resumeCarousel,
  select: selectCarousel,
  markImageFailed: markCarouselImageFailed,
  onFocusOut: carouselFocusOut,
} = useContentCarousel({
  items: carouselItems,
  intervalSeconds: carouselIntervalSeconds,
  maxItems: carouselMaxItems,
})

const sectionTitles: Record<PartyHomeColumnAlias, string> = {
  'party-voice': '高层声音',
  'party-work': '工作动态',
  'party-rules': '党规党章',
  'party-study': '理论学习',
}

setPageMeta({
  title: '中心党建',
  description: '吉林省高等学校毕业生就业指导中心党建信息公开入口。',
})

onMounted(load)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const columnsPromise = loadPartyColumns()
    const [carousel, properties, columns] = await Promise.all([
      getPublicCmsListByCode(PARTY_CAROUSEL_CODE),
      listPublicSiteConfig(),
      columnsPromise,
    ])
    const config = Object.fromEntries(properties.map(item => [item.key, item.value]))
    const articlePages = await Promise.all(
      PARTY_HOME_COLUMN_ALIASES.map(alias => listPublicArticles(columns[alias].id, 0, SECTION_SIZE)),
    )
    carouselIntervalSeconds.value = positiveInteger(config[CAROUSEL_INTERVAL_KEY], DEFAULT_CAROUSEL_INTERVAL_SECONDS)
    carouselMaxItems.value = positiveInteger(config[CAROUSEL_MAX_ITEMS_KEY], DEFAULT_CAROUSEL_MAX_ITEMS)
    carouselItems.value = carousel.items
    PARTY_HOME_COLUMN_ALIASES.forEach((alias, index) => {
      sectionArticles.value[alias] = articlePages[index].items
    })
  } catch (e) {
    error.value = e instanceof Error ? e.message : '党建内容加载失败'
  } finally {
    loading.value = false
  }
}

function positiveInteger(value: string | undefined, fallback: number) {
  const parsed = Number.parseInt(value || '', 10)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback
}

function isExternal(article: PublicArticleSummary) {
  return article.articleType === 'EXTERNAL_LINK' && Boolean(article.externalUrl)
}

function carouselInternalRoute(item: CmsListItem) {
  return item.sourceType === 'ARTICLE' && item.articleType === 'INTERNAL' && item.articleId != null
    ? `/party/article/${item.articleId}`
    : null
}

function carouselHref(item: CmsListItem) {
  return carouselInternalRoute(item) ? null : item.url
}

function carouselImage(item: CmsListItem) {
  return publicListImageUrl(item) || ''
}

function itemTarget(item: CmsListItem) {
  const href = carouselHref(item)
  return item.openMode === 'NEW_WINDOW' || (item.openMode === 'DEFAULT' && Boolean(href?.startsWith('http'))) ? '_blank' : undefined
}
</script>

<template>
  <main class="party-main">
    <div class="party-width party-content-entry">
      <p v-if="loading" class="party-state">正在加载党建内容…</p>
      <p v-else-if="error" class="party-state party-state-error">{{ error }}</p>
      <template v-else>
        <section class="party-entry-top" aria-label="中心党建重点内容">
          <div
            class="party-carousel"
            data-testid="party-carousel"
            @mouseenter="pauseCarousel('hover')"
            @mouseleave="resumeCarousel('hover')"
            @focusin="pauseCarousel('focus')"
            @focusout="carouselFocusOut"
          >
            <template v-if="visibleCarouselItems.length">
              <article
                v-for="(item, index) in visibleCarouselItems"
                :key="item.id"
                class="party-carousel-item"
                :class="{ active: index === activeSlide }"
                :aria-hidden="index !== activeSlide"
                :data-testid="`party-carousel-item-${item.id}`"
              >
                <router-link v-if="carouselInternalRoute(item)" :to="carouselInternalRoute(item)!">
                  <img v-if="carouselImage(item)" :src="carouselImage(item)" :alt="item.title" @error="markCarouselImageFailed(item.id)">
                  <strong>{{ item.title }}</strong>
                </router-link>
                <a v-else-if="carouselHref(item)" :href="carouselHref(item)!" :target="itemTarget(item)" :rel="itemTarget(item) ? 'noopener noreferrer' : undefined">
                  <img v-if="carouselImage(item)" :src="carouselImage(item)" :alt="item.title" @error="markCarouselImageFailed(item.id)">
                  <strong>{{ item.title }}</strong>
                </a>
                <div v-else>
                  <img v-if="carouselImage(item)" :src="carouselImage(item)" :alt="item.title" @error="markCarouselImageFailed(item.id)">
                  <strong>{{ item.title }}</strong>
                </div>
              </article>
              <div v-if="visibleCarouselItems.length > 1" class="party-carousel-dots" aria-label="轮播页码">
                <button
                  v-for="(item, index) in visibleCarouselItems"
                  :key="`dot-${item.id}`"
                  type="button"
                  :class="{ active: item.id === activeCarouselItem?.id }"
                  :aria-label="`查看第 ${index + 1} 项：${item.title}`"
                  @click="selectCarousel(index)"
                />
              </div>
            </template>
            <p v-else class="party-empty">暂无轮播内容</p>
          </div>

          <section class="party-section-panel party-voice-panel" data-testid="party-section-party-voice">
            <header class="party-red-tab">
              <h2>{{ sectionTitles['party-voice'] }}</h2>
              <router-link to="/party/column/party-voice">更多 »</router-link>
            </header>
            <ul class="party-article-list party-voice-list">
              <li v-for="article in sectionArticles['party-voice']" :key="article.id">
                <a v-if="isExternal(article)" :href="article.externalUrl!" target="_blank" rel="noopener noreferrer">{{ article.title }}</a>
                <router-link v-else :to="`/party/article/${article.id}`">{{ article.title }}</router-link>
                <time v-if="article.publishDate">{{ article.publishDate }}</time>
              </li>
              <li v-if="!sectionArticles['party-voice'].length" class="party-empty">暂无已发布内容</li>
            </ul>
          </section>
        </section>

        <section class="party-block" data-testid="party-section-party-work">
          <header class="party-block-title">
            <h2>{{ sectionTitles['party-work'] }}</h2>
          </header>
          <div class="party-section-panel">
            <header class="party-red-tab">
              <h3>工作动态</h3>
              <router-link to="/party/column/party-work">更多 »</router-link>
            </header>
            <ul class="party-article-list party-work-list">
              <li v-for="article in sectionArticles['party-work']" :key="article.id">
                <a v-if="isExternal(article)" :href="article.externalUrl!" target="_blank" rel="noopener noreferrer">{{ article.title }}</a>
                <router-link v-else :to="`/party/article/${article.id}`">{{ article.title }}</router-link>
                <time v-if="article.publishDate">{{ article.publishDate }}</time>
              </li>
              <li v-if="!sectionArticles['party-work'].length" class="party-empty">暂无已发布内容</li>
            </ul>
          </div>
        </section>

        <section class="party-block party-study-garden" aria-labelledby="party-study-garden-title">
          <header class="party-block-title">
            <h2 id="party-study-garden-title">学习园地</h2>
          </header>
          <div class="party-study-grid">
            <section v-for="alias in studyAliases" :key="alias" class="party-section-panel" :data-testid="`party-section-${alias}`">
              <header class="party-red-tab">
                <h3>{{ sectionTitles[alias] }}</h3>
                <router-link :to="`/party/column/${alias}`">更多 »</router-link>
              </header>
              <ul class="party-article-list">
                <li v-for="article in sectionArticles[alias]" :key="article.id">
                  <a v-if="isExternal(article)" :href="article.externalUrl!" target="_blank" rel="noopener noreferrer">{{ article.title }}</a>
                  <router-link v-else :to="`/party/article/${article.id}`">{{ article.title }}</router-link>
                  <time v-if="article.publishDate">{{ article.publishDate }}</time>
                </li>
                <li v-if="!sectionArticles[alias].length" class="party-empty">暂无已发布内容</li>
              </ul>
            </section>
          </div>
        </section>
      </template>
    </div>
  </main>
</template>

<style scoped>
@media (prefers-reduced-motion: reduce) {
  .party-carousel-item {
    transition: none;
  }
}
</style>
