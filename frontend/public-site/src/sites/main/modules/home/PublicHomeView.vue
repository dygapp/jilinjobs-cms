<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useContentCarousel } from '../../../../shared/carousel/useContentCarousel'
import { MAIN_SITE_CONFIG_KEYS, useMainSiteContext } from '../../app/siteContext'
import { getPublicAdvertisementSlot, type Advertisement } from '../../api/advertisements'
import { listPublicArticles, type PublicArticleSummary } from '../../api/articles'
import { getPublicColumnByAlias } from '../../api/columns'
import { getPublicCmsListByCode, listPublicCmsListsByGroup, publicListImageUrl, type CmsListItem } from '../../api/lists'
import { setPageMeta } from '../../seo'

type SiteLinkGroup = { name: string; links: CmsListItem[] }

const HOME_NEWS_ITEM_LIMIT = 7
const DEFAULT_CAROUSEL_INTERVAL_SECONDS = 4
const DEFAULT_CAROUSEL_MAX_ITEMS = 5
const PROMO_ROTATION_INTERVAL_MS = 4000
const NOTICE_COLUMN_ALIAS = 'notice'
const EMPLOYMENT_NEWS_COLUMN_ALIAS = 'employment-news'
const RECRUITMENT_COLUMN_ALIAS = 'recruitment-announcement'
const HOME_CAROUSEL_LIST_CODE = 'HOME_CAROUSEL'
const SITE_LINKS_GROUP_CODE = 'SITE_LINKS'
const HOME_RECRUITMENT_PROMO_SLOT_CODE = 'HOME_RECRUITMENT_PROMO'
const HOME_SHORTCUT_POSITION = 'HOME_SHORTCUT'
const HOME_QUICK_POSITION = 'HOME_QUICK'

const { navigation: items, config: siteConfig, error: shellError, ready: shellReady } = useMainSiteContext()
const noticeArticles = ref<PublicArticleSummary[]>([])
const employmentArticles = ref<PublicArticleSummary[]>([])
const recruitmentArticles = ref<PublicArticleSummary[]>([])
const siteGroups = ref<SiteLinkGroup[]>([])
const carouselItems = ref<CmsListItem[]>([])
const carouselIntervalSeconds = ref(DEFAULT_CAROUSEL_INTERVAL_SECONDS)
const carouselMaxItems = ref(DEFAULT_CAROUSEL_MAX_ITEMS)
const promoAds = ref<Advertisement[]>([])
const activePromoIndex = ref(0)
const contactPhone = ref('')
const activeSiteGroup = ref(0)
const loading = ref(true)
const error = ref('')
const ncssLogo = '/static/home/ncss-logo.png'
const phoneIcon = '/static/icons/phone.png'
let promoTimer: ReturnType<typeof setInterval> | null = null

const {
  activeItem: carouselItem,
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

setPageMeta({ description: '吉林省高等学校毕业生就业信息网，提供就业资讯、政策法规、业务指南和公共服务入口。' })

const shortcutItems = computed(() => items.value.filter(item => item.position === HOME_SHORTCUT_POSITION))
const quickItems = computed(() => items.value.filter(item => item.position === HOME_QUICK_POSITION))
const isExternalArticle = (article: PublicArticleSummary) => article.articleType === 'EXTERNAL_LINK' && Boolean(article.externalUrl)
const newWindow = (mode: string, url: string | null | undefined) => mode === 'NEW_WINDOW' || (mode === 'DEFAULT' && Boolean(url?.startsWith('http')))
const activePromo = computed(() => promoAds.value[activePromoIndex.value] || null)
const promoLinked = computed(() => Boolean(activePromo.value?.url) && activePromo.value?.openMode !== 'NO_LINK')

function startPromoRotation() {
  if (promoTimer) clearInterval(promoTimer)
  promoTimer = null
  activePromoIndex.value = 0
  if (promoAds.value.length > 1) {
    promoTimer = setInterval(() => {
      activePromoIndex.value = (activePromoIndex.value + 1) % promoAds.value.length
    }, PROMO_ROTATION_INTERVAL_MS)
  }
}

function positiveInteger(value: string | undefined, fallback: number) {
  const parsed = Number.parseInt(value || '', 10)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback
}

function carouselInternalRoute(item: CmsListItem) {
  return item.sourceType === 'ARTICLE' && item.articleType === 'INTERNAL' && item.articleId != null
    ? `/article/${item.articleId}`
    : null
}

function carouselHref(item: CmsListItem) {
  if (carouselInternalRoute(item)) return null
  if (item.sourceType === 'ARTICLE' && item.articleType === 'EXTERNAL_LINK') return item.externalUrl
  return item.url
}

function carouselImage(item: CmsListItem) {
  return publicListImageUrl(item) || ''
}

const calendar = computed(() => {
  const today = new Date()
  const year = today.getFullYear()
  const month = today.getMonth()
  const start = new Date(year, month, 1).getDay()
  const days = new Date(year, month + 1, 0).getDate()
  const cells: Array<number | null> = Array.from({ length: start }, () => null)
  for (let day = 1; day <= days; day += 1) cells.push(day)
  while (cells.length % 7) cells.push(null)
  return { year, month: month + 1, today: today.getDate(), cells }
})

onMounted(async () => {
  try {
    const carouselPromise = getPublicCmsListByCode(HOME_CAROUSEL_LIST_CODE)
    const siteLinksPromise = listPublicCmsListsByGroup(SITE_LINKS_GROUP_CODE)
    const promoPromise = getPublicAdvertisementSlot(HOME_RECRUITMENT_PROMO_SLOT_CODE)
    const noticeColumnPromise = getPublicColumnByAlias(NOTICE_COLUMN_ALIAS)
    const employmentColumnPromise = getPublicColumnByAlias(EMPLOYMENT_NEWS_COLUMN_ALIAS)
    const recruitmentColumnPromise = getPublicColumnByAlias(RECRUITMENT_COLUMN_ALIAS)

    await shellReady
    if (shellError.value) throw new Error(shellError.value)

    const [carouselList, siteLinkLists, promoSlot, noticeColumn, employmentColumn, recruitmentColumn] = await Promise.all([
      carouselPromise,
      siteLinksPromise,
      promoPromise,
      noticeColumnPromise,
      employmentColumnPromise,
      recruitmentColumnPromise,
    ])
    const [noticePage, employmentPage, recruitmentPage] = await Promise.all([
      listPublicArticles(noticeColumn.id, 0, HOME_NEWS_ITEM_LIMIT),
      listPublicArticles(employmentColumn.id, 0, HOME_NEWS_ITEM_LIMIT),
      listPublicArticles(recruitmentColumn.id, 0, HOME_NEWS_ITEM_LIMIT, 'EXTERNAL_LINK'),
    ])

    noticeArticles.value = noticePage.items
    employmentArticles.value = employmentPage.items
    recruitmentArticles.value = recruitmentPage.items
    contactPhone.value = siteConfig.value[MAIN_SITE_CONFIG_KEYS.CONTACT_PHONE] || ''
    carouselIntervalSeconds.value = positiveInteger(
      siteConfig.value[MAIN_SITE_CONFIG_KEYS.CAROUSEL_INTERVAL_SECONDS],
      DEFAULT_CAROUSEL_INTERVAL_SECONDS,
    )
    carouselMaxItems.value = positiveInteger(
      siteConfig.value[MAIN_SITE_CONFIG_KEYS.CAROUSEL_MAX_ITEMS],
      DEFAULT_CAROUSEL_MAX_ITEMS,
    )
    carouselItems.value = carouselList.items
    siteGroups.value = siteLinkLists.map(list => ({ name: list.name, links: list.items }))
    promoAds.value = promoSlot.advertisements
    startPromoRotation()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '公开内容加载失败'
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  if (promoTimer) clearInterval(promoTimer)
})
</script>

<template>
  <main class="home-main">
    <p v-if="loading" class="public-state">正在加载公开内容…</p>
    <p v-else-if="error" class="public-state error-text">{{ error }}</p>

    <div v-if="!loading && !error" class="site-width home-content" data-testid="public-content">
      <section class="home-primary-row">
        <div
          class="home-carousel"
          data-testid="home-carousel-active"
          :data-carousel-item-id="carouselItem?.id || ''"
          @mouseenter="pauseCarousel('hover')"
          @mouseleave="resumeCarousel('hover')"
          @focusin="pauseCarousel('focus')"
          @focusout="carouselFocusOut"
        >
          <Transition name="home-carousel-fade" mode="out-in">
            <div v-if="carouselItem" :key="carouselItem.id" class="home-carousel-slide">
              <router-link
                v-if="carouselInternalRoute(carouselItem)"
                :to="carouselInternalRoute(carouselItem)!"
                :target="newWindow(carouselItem.openMode, carouselInternalRoute(carouselItem)) ? '_blank' : undefined"
                :rel="newWindow(carouselItem.openMode, carouselInternalRoute(carouselItem)) ? 'noopener noreferrer' : undefined"
              >
                <img :src="carouselImage(carouselItem)" :alt="carouselItem.title || '首页轮播图'" @error="markCarouselImageFailed(carouselItem.id)">
                <span v-if="carouselItem.title" class="carousel-caption">{{ carouselItem.title }}</span>
              </router-link>
              <a v-else-if="carouselHref(carouselItem)" :href="carouselHref(carouselItem)!" :target="newWindow(carouselItem.openMode, carouselHref(carouselItem)) ? '_blank' : undefined" rel="noopener noreferrer">
                <img :src="carouselImage(carouselItem)" :alt="carouselItem.title || '首页轮播图'" @error="markCarouselImageFailed(carouselItem.id)">
                <span v-if="carouselItem.title" class="carousel-caption">{{ carouselItem.title }}</span>
              </a>
              <div v-else class="home-carousel-static">
                <img :src="carouselImage(carouselItem)" :alt="carouselItem.title || '首页轮播图'" @error="markCarouselImageFailed(carouselItem.id)">
                <span v-if="carouselItem.title" class="carousel-caption">{{ carouselItem.title }}</span>
              </div>
            </div>
            <div v-else key="empty" class="visual-empty">轮播图</div>
          </Transition>
          <div v-if="visibleCarouselItems.length > 1" class="home-carousel-dots" aria-label="轮播页码">
            <button
              v-for="(item, index) in visibleCarouselItems"
              :key="`home-carousel-dot-${item.id}`"
              type="button"
              :class="{ active: item.id === carouselItem?.id }"
              :aria-label="`查看第 ${index + 1} 项：${item.title}`"
              @click="selectCarousel(index)"
            />
          </div>
        </div>

        <section class="home-panel notice-panel news-column">
          <header><h2>通知公告</h2><router-link to="/column/notice">更多 &gt;</router-link></header>
          <ul>
            <li v-for="article in noticeArticles" :key="article.id">
              <a v-if="isExternalArticle(article)" :data-testid="`public-article-${article.id}`" :href="article.externalUrl!" target="_blank" rel="noopener noreferrer">{{ article.title }}</a>
              <router-link v-else :data-testid="`public-article-${article.id}`" :to="`/article/${article.id}`">{{ article.title }}</router-link>
              <time v-if="article.publishDate">{{ article.publishDate }}</time>
            </li>
            <li v-if="!noticeArticles.length" class="empty-item">暂无已发布内容</li>
          </ul>
        </section>

        <aside class="home-top-shortcuts">
          <template v-for="link in shortcutItems" :key="link.id">
            <a v-if="link.external" :href="link.href" :target="link.newWindow ? '_blank' : undefined" rel="noopener noreferrer"><img v-if="link.iconPath" :src="link.iconPath" alt=""><span>{{ link.name }}</span></a>
            <router-link v-else :to="link.href"><img v-if="link.iconPath" :src="link.iconPath" alt=""><span>{{ link.name }}</span></router-link>
          </template>
        </aside>
      </section>

      <section class="home-secondary-row">
        <div class="home-calendar" aria-label="招聘日历">
          <div class="calendar-selects"><span>{{ calendar.year }}年</span><span>{{ calendar.month }}月</span></div>
          <div class="calendar-week"><span v-for="name in ['一','二','三','四','五','六','日']" :key="name">{{ name }}</span></div>
          <div class="calendar-days"><span v-for="(day, index) in calendar.cells" :key="index" :class="{ today: day === calendar.today, empty: day == null }">{{ day || '' }}</span></div>
        </div>

        <section class="home-panel employment-panel news-column">
          <header><h2>就业动态</h2><router-link to="/column/employment-news">更多 &gt;</router-link></header>
          <ul>
            <li v-for="article in employmentArticles" :key="article.id">
              <a v-if="isExternalArticle(article)" :href="article.externalUrl!" target="_blank" rel="noopener noreferrer">{{ article.title }}</a>
              <router-link v-else :to="`/article/${article.id}`">{{ article.title }}</router-link>
              <time v-if="article.publishDate">{{ article.publishDate }}</time>
            </li>
            <li v-if="!employmentArticles.length" class="empty-item">暂无已发布内容</li>
          </ul>
        </section>

        <aside class="service-panel">
          <h2>快速导航</h2>
          <p class="service-phone"><img :src="phoneIcon" alt="">咨询电话：<strong>{{ contactPhone }}</strong></p>
          <div class="service-shortcuts">
            <template v-for="item in quickItems" :key="item.id">
              <a v-if="item.external" :href="item.href" :target="item.newWindow ? '_blank' : undefined" rel="noopener noreferrer"><img v-if="item.iconPath" :src="item.iconPath" alt=""><span>{{ item.name }}</span></a>
              <router-link v-else :to="item.href"><img v-if="item.iconPath" :src="item.iconPath" alt=""><span>{{ item.name }}</span></router-link>
            </template>
          </div>
        </aside>
      </section>

      <template v-if="activePromo">
        <a v-if="promoLinked" class="home-promo-banner" :data-testid="`home-promo-ad-${activePromo.id}`" :href="activePromo.url!" :target="newWindow(activePromo.openMode, activePromo.url) ? '_blank' : undefined" rel="noopener noreferrer"><img :src="activePromo.imagePath" :alt="activePromo.title"></a>
        <div v-else class="home-promo-banner" :data-testid="`home-promo-ad-${activePromo.id}`"><img :src="activePromo.imagePath" :alt="activePromo.title"></div>
      </template>

      <section class="home-section original-section latest-recruitment"><header class="section-title"><h2>最新招聘</h2></header><div class="external-placeholder iframe-placeholder"><span>慧就业招聘信息区域</span></div></section>

      <section class="home-recruitment-row">
        <div class="external-placeholder recruitment-stream"><span>招聘与宣讲内容区域</span></div>
        <section class="home-panel recruitment-panel news-column">
          <header><h2>招聘公告</h2><router-link to="/column/recruitment-announcement">更多 &gt;</router-link></header>
          <ul><li v-for="article in recruitmentArticles" :key="article.id"><a :data-testid="`recruitment-external-${article.id}`" :href="article.externalUrl!" target="_blank" rel="noopener noreferrer">{{ article.title }}</a><time v-if="article.publishDate">{{ article.publishDate }}</time></li><li v-if="!recruitmentArticles.length" class="empty-item">暂无已发布内容</li></ul>
        </section>
      </section>

      <section class="ncss-entrance">
        <a href="https://jilinbys.ncss.cn/student/index.html" target="_blank" rel="noopener noreferrer"><img :src="ncssLogo" alt="国家大学生就业服务平台"></a>
        <div class="ncss-login-buttons">
          <a href="https://account.chsi.com.cn/passport/login?service=https://jilinbys.ncss.cn/student/connect/chsi&entrytype=stu" target="_blank" rel="noopener noreferrer">学生登录</a>
          <a href="https://account.chsi.com.cn/passport/login?service=https://jilinbys.ncss.cn/corp/connect/chsi&entrytype=corp" target="_blank" rel="noopener noreferrer">企业登录</a>
        </div>
      </section>

      <section class="home-section site-navigation">
        <header class="section-title"><h2>网站导航</h2></header>
        <div class="site-navigation-card">
          <div class="site-navigation-tabs" role="tablist"><button v-for="(group, index) in siteGroups" :key="group.name" type="button" :class="{ active: activeSiteGroup === index }" @click="activeSiteGroup = index">{{ group.name }}</button></div>
          <div v-if="siteGroups[activeSiteGroup]" class="site-link-group">
            <a v-for="link in siteGroups[activeSiteGroup].links" :key="link.id" :href="link.url || '#'" :target="newWindow(link.openMode, link.url) ? '_blank' : undefined" rel="noopener noreferrer">{{ link.title }}</a>
            <span v-if="!siteGroups[activeSiteGroup].links.length" class="empty-item">相关链接将在后续内容整理中补充</span>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
.home-carousel {
  height: auto;
  aspect-ratio: 8 / 5;
  position: relative;
  overflow: hidden;
}
.home-carousel-slide,
.home-carousel-slide > a,
.home-carousel-static,
.home-carousel .visual-empty {
  display: block;
  width: 100%;
  height: 100%;
}
.home-carousel-slide img,
.home-carousel-static img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.home-carousel-dots {
  position: absolute;
  z-index: 3;
  right: 12px;
  bottom: 10px;
  display: flex;
  gap: 7px;
}
.home-carousel-dots button {
  width: 10px;
  height: 10px;
  padding: 0;
  border: 1px solid rgba(255,255,255,.9);
  border-radius: 50%;
  background: rgba(0,0,0,.35);
  cursor: pointer;
}
.home-carousel-dots button.active {
  background: #fff;
}
.home-carousel-fade-enter-active,
.home-carousel-fade-leave-active {
  transition: opacity .25s ease;
}
.home-carousel-fade-enter-from,
.home-carousel-fade-leave-to {
  opacity: 0;
}
@media (prefers-reduced-motion: reduce) {
  .home-carousel-fade-enter-active,
  .home-carousel-fade-leave-active {
    transition: none;
  }
}
</style>