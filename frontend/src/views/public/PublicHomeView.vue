<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listPublicArticles, type PublicArticleSummary } from '../../api/articles'
import { listPublicNavigations, type PublicNavigation } from '../../api/navigation'
import { listPublicSiteConfig } from '../../api/siteConfig'
import PublicSiteHeader from '../../components/PublicSiteHeader.vue'
import PublicSiteFooter from '../../components/PublicSiteFooter.vue'
import { setPageMeta } from '../../seo'

type SiteLink = { name: string; url: string }
type SiteLinkGroup = { name: string; links: SiteLink[] }
type HomeBanner = { image: string; title?: string; url?: string }

const items = ref<PublicNavigation[]>([])
const articles = ref<PublicArticleSummary[]>([])
const serviceLinks = ref<SiteLink[]>([])
const configuredSiteGroups = ref<SiteLinkGroup[]>([])
const banners = ref<HomeBanner[]>([])
const promoBanner = ref('/static/home/recruitment-campaign.png')
const ncssLogo = ref('/static/home/ncss-logo.png')
const phoneIcon = '/static/icons/phone.png'
const activeSiteGroup = ref(0)
const loading = ref(true)
const error = ref('')

setPageMeta({ description: '吉林省高等学校毕业生就业信息网，提供就业资讯、政策法规、业务指南和公共服务入口。' })

const parseJson = <T,>(value: string | undefined, fallback: T): T => {
  if (!value) return fallback
  try { return JSON.parse(value) as T } catch { return fallback }
}

const serviceItems = computed(() => items.value.filter(item => item.position === 'SERVICE'))
const navigationSiteGroups = computed<SiteLinkGroup[]>(() => {
  const groups = new Map<string, SiteLink[]>()
  items.value.filter(item => item.position === 'SITE').forEach(item => {
    const name = item.category || '相关网站服务'
    const links = groups.get(name) || []
    links.push({ name: item.name, url: item.href })
    groups.set(name, links)
  })
  return Array.from(groups, ([name, links]) => ({ name, links }))
})
const siteGroups = computed(() => {
  const groups = configuredSiteGroups.value.map(group => ({ ...group, links: [...group.links] }))
  for (const extra of navigationSiteGroups.value) {
    const existing = groups.find(group => group.name === extra.name)
    if (existing) existing.links.push(...extra.links)
    else groups.push(extra)
  }
  return groups
})

const articlesFor = (alias: string) => articles.value.filter(article => article.columnAlias === alias).slice(0, 7)
const isExternalArticle = (article: PublicArticleSummary) => article.articleType === 'EXTERNAL_LINK' && Boolean(article.externalUrl)
const noticeArticles = computed(() => articlesFor('notice'))
const employmentArticles = computed(() => articlesFor('employment-news'))
const recruitmentArticles = computed(() => articles.value
  .filter(article => article.columnAlias === 'recruitment-announcement' && isExternalArticle(article))
  .slice(0, 7))

const guideItems = [
  { name: '就业派遣', to: '/page/guide/jypq', icon: '/static/icons/guide-01.png' },
  { name: '档案管理', to: '/page/guide/dagl', icon: '/static/icons/guide-02.png' },
  { name: '流动党员', to: '/page/guide/dygl', icon: '/static/icons/guide-03.png' },
  { name: '学历认证', to: '/page/guide/xlrz', icon: '/static/icons/guide-04.png' },
  { name: '联系我们', to: '/page/guide/contact', icon: '/static/icons/guide-05.png' },
  { name: '常见问题', to: '/page/guide/faq', icon: '/static/icons/guide-06.png' },
]
const quickIcon = (index: number) => `/static/icons/top-nav-${String(index + 1).padStart(2, '0')}.png`

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
    const [navigation, articlePage, config] = await Promise.all([
      listPublicNavigations(),
      listPublicArticles(null, 0, 50),
      listPublicSiteConfig(),
    ])
    items.value = navigation
    articles.value = articlePage.items
    const values = Object.fromEntries(config.map(item => [item.key, item.value]))
    serviceLinks.value = parseJson<SiteLink[]>(values.SERVICE_LINKS, [])
    configuredSiteGroups.value = parseJson<SiteLinkGroup[]>(values.SITE_LINK_GROUPS, [])
    banners.value = parseJson<HomeBanner[]>(values.HOME_BANNERS, [])
    promoBanner.value = values.HOME_PROMO_BANNER_PATH || promoBanner.value
    ncssLogo.value = values.HOME_NCSS_LOGO_PATH || ncssLogo.value
  } catch (e) {
    error.value = e instanceof Error ? e.message : '公开内容加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <PublicSiteHeader />
  <main class="home-main">
    <p v-if="loading" class="public-state">正在加载公开内容…</p>
    <p v-else-if="error" class="public-state error-text">{{ error }}</p>

    <div class="site-width home-content" data-testid="public-content">
      <section class="home-primary-row">
        <div class="home-carousel">
          <a v-if="banners[0]" :href="banners[0].url || '#'" :target="banners[0].url?.startsWith('http') ? '_blank' : undefined" rel="noopener noreferrer">
            <img :src="banners[0].image" :alt="banners[0].title || '首页轮播图'">
            <span v-if="banners[0].title" class="carousel-caption">{{ banners[0].title }}</span>
          </a>
          <div v-else class="visual-empty">轮播图</div>
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
          <template v-for="(link, index) in serviceLinks.slice(0, 5)" :key="link.name">
            <a :href="link.url" :target="link.url.startsWith('http') ? '_blank' : undefined" rel="noopener noreferrer">
              <img :src="quickIcon(index)" alt="">
              <span>{{ link.name }}</span>
            </a>
          </template>
        </aside>
      </section>

      <section class="home-secondary-row">
        <div class="home-calendar" aria-label="招聘日历">
          <div class="calendar-selects"><span>{{ calendar.year }}年</span><span>{{ calendar.month }}月</span></div>
          <div class="calendar-week"><span v-for="name in ['一','二','三','四','五','六','日'] :key="name">{{ name }}</span></div>
          <div class="calendar-days">
            <span v-for="(day, index) in calendar.cells" :key="index" :class="{ today: day === calendar.today, empty: day == null }">{{ day || '' }}</span>
          </div>
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
          <p class="service-phone"><img :src="phoneIcon" alt="">咨询电话：<strong>0431-84657570</strong></p>
          <div class="service-shortcuts">
            <router-link v-for="item in guideItems" :key="item.name" :to="item.to">
              <img :src="item.icon" alt="">
              <span>{{ item.name }}</span>
            </router-link>
            <template v-for="item in serviceItems" :key="item.id">
              <a v-if="item.external" :href="item.href" :target="item.newWindow ? '_blank' : undefined" rel="noopener noreferrer"><span>{{ item.name }}</span></a>
              <router-link v-else :to="item.href"><span>{{ item.name }}</span></router-link>
            </template>
          </div>
        </aside>
      </section>

      <a class="home-promo-banner" href="https://24365.jl.smartedu.cn/" target="_blank" rel="noopener noreferrer">
        <img :src="promoBanner" alt="吉林省高校毕业生招聘活动">
      </a>

      <section class="home-section original-section latest-recruitment">
        <header class="section-title"><h2>最新招聘</h2></header>
        <div class="external-placeholder iframe-placeholder"><span>慧就业招聘信息区域</span></div>
      </section>

      <section class="home-recruitment-row">
        <div class="external-placeholder recruitment-stream"><span>招聘与宣讲内容区域</span></div>
        <section class="home-panel recruitment-panel news-column">
          <header><h2>招聘公告</h2><router-link to="/column/recruitment-announcement">更多 &gt;</router-link></header>
          <ul>
            <li v-for="article in recruitmentArticles" :key="article.id">
              <a :data-testid="`recruitment-external-${article.id}`" :href="article.externalUrl!" target="_blank" rel="noopener noreferrer">{{ article.title }}</a>
              <time v-if="article.publishDate">{{ article.publishDate }}</time>
            </li>
            <li v-if="!recruitmentArticles.length" class="empty-item">暂无已发布内容</li>
          </ul>
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
          <div class="site-navigation-tabs" role="tablist">
            <button v-for="(group, index) in siteGroups" :key="group.name" type="button" :class="{ active: activeSiteGroup === index }" @click="activeSiteGroup = index">{{ group.name }}</button>
          </div>
          <div v-if="siteGroups[activeSiteGroup]" class="site-link-group">
            <a v-for="link in siteGroups[activeSiteGroup].links" :key="link.name" :href="link.url" :target="link.url.startsWith('http') ? '_blank' : undefined" rel="noopener noreferrer">{{ link.name }}</a>
            <span v-if="!siteGroups[activeSiteGroup].links.length" class="empty-item">相关链接将在后续内容整理中补充</span>
          </div>
        </div>
      </section>
    </div>
  </main>
  <PublicSiteFooter />
</template>
