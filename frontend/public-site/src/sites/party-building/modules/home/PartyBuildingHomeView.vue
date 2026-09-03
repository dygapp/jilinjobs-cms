<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listPublicArticles, type PublicArticleSummary } from '../../../../shared/api/articles'
import { getPublicCmsListByCode, type CmsListItem } from '../../../../shared/api/lists'
import { setPageMeta } from '../../../../shared/seo'
import {
  PARTY_COLUMN_ALIASES,
  PARTY_HOME_CAROUSEL_CODE,
  loadPartyColumns,
  type PartyColumnAlias,
} from '../../app/partyContext'

const SECTION_SIZE = 6
const sectionArticles = ref<Record<PartyColumnAlias, PublicArticleSummary[]>>({
  'party-voice': [],
  'party-work': [],
  'party-rules': [],
  'party-study': [],
})
const carouselItems = ref<CmsListItem[]>([])
const loading = ref(true)
const error = ref('')

const sectionTitles: Record<PartyColumnAlias, string> = {
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
    const columns = await loadPartyColumns()
    const [carousel, ...articlePages] = await Promise.all([
      getPublicCmsListByCode(PARTY_HOME_CAROUSEL_CODE),
      ...PARTY_COLUMN_ALIASES.map(alias => listPublicArticles(columns[alias].id, 0, SECTION_SIZE)),
    ])
    carouselItems.value = carousel.items.filter(item => item.enabled)
    PARTY_COLUMN_ALIASES.forEach((alias, index) => {
      sectionArticles.value[alias] = articlePages[index].items
    })
  } catch (e) {
    error.value = e instanceof Error ? e.message : '党建内容加载失败'
  } finally {
    loading.value = false
  }
}

function isExternal(article: PublicArticleSummary) {
  return article.articleType === 'EXTERNAL_LINK' && Boolean(article.externalUrl)
}

function itemTarget(item: CmsListItem) {
  return item.openMode === 'NEW_WINDOW' ? '_blank' : undefined
}
</script>

<template>
  <main class="party-main">
    <section class="party-hero">
      <div class="party-width party-hero-inner">
        <p class="party-kicker">党建引领 · 服务发展</p>
        <h1>中心党建</h1>
      </div>
    </section>

    <div class="party-width party-content-home">
      <p v-if="loading" class="party-state">正在加载党建内容…</p>
      <p v-else-if="error" class="party-state party-state-error">{{ error }}</p>
      <template v-else>
        <section class="party-home-top" aria-label="党建首页重点内容">
          <div class="party-carousel" data-testid="party-home-carousel">
            <template v-if="carouselItems.length">
              <article v-for="item in carouselItems" :key="item.id" class="party-carousel-item" :data-testid="`party-carousel-item-${item.id}`">
                <a v-if="item.url" :href="item.url" :target="itemTarget(item)" :rel="itemTarget(item) ? 'noopener noreferrer' : undefined">
                  <img v-if="item.imagePath" :src="item.imagePath" :alt="item.title">
                  <strong>{{ item.title }}</strong>
                </a>
                <div v-else>
                  <img v-if="item.imagePath" :src="item.imagePath" :alt="item.title">
                  <strong>{{ item.title }}</strong>
                </div>
              </article>
            </template>
            <p v-else class="party-empty">暂无轮播内容</p>
          </div>

          <section class="party-home-section party-home-voice" data-testid="party-section-party-voice">
            <header class="party-section-heading">
              <h2>{{ sectionTitles['party-voice'] }}</h2>
              <router-link to="/party/column/party-voice">更多</router-link>
            </header>
            <ul class="party-article-list">
              <li v-for="article in sectionArticles['party-voice']" :key="article.id">
                <a v-if="isExternal(article)" :href="article.externalUrl!" target="_blank" rel="noopener noreferrer">{{ article.title }}</a>
                <router-link v-else :to="`/party/article/${article.id}`">{{ article.title }}</router-link>
                <time v-if="article.publishDate">{{ article.publishDate }}</time>
              </li>
              <li v-if="!sectionArticles['party-voice'].length" class="party-empty">暂无已发布内容</li>
            </ul>
          </section>
        </section>

        <section class="party-home-section" data-testid="party-section-party-work">
          <header class="party-section-heading">
            <h2>{{ sectionTitles['party-work'] }}</h2>
            <router-link to="/party/column/party-work">更多</router-link>
          </header>
          <ul class="party-article-list">
            <li v-for="article in sectionArticles['party-work']" :key="article.id">
              <a v-if="isExternal(article)" :href="article.externalUrl!" target="_blank" rel="noopener noreferrer">{{ article.title }}</a>
              <router-link v-else :to="`/party/article/${article.id}`">{{ article.title }}</router-link>
              <time v-if="article.publishDate">{{ article.publishDate }}</time>
            </li>
            <li v-if="!sectionArticles['party-work'].length" class="party-empty">暂无已发布内容</li>
          </ul>
        </section>

        <section class="party-study-garden" aria-labelledby="party-study-garden-title">
          <h2 id="party-study-garden-title" class="party-garden-title">学习园地</h2>
          <div class="party-study-grid">
            <section v-for="alias in ['party-rules', 'party-study'] as PartyColumnAlias[]" :key="alias" class="party-home-section" :data-testid="`party-section-${alias}`">
              <header class="party-section-heading">
                <h3>{{ sectionTitles[alias] }}</h3>
                <router-link :to="`/party/column/${alias}`">更多</router-link>
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
