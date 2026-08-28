<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { listPublicNavigations, type PublicNavigation } from '../api/navigation'
import { listPublicSiteConfig } from '../api/siteConfig'

const route = useRoute()
const items = ref<PublicNavigation[]>([])
const siteName = ref('吉林省高等学校毕业生就业信息网')
const platformLogoIcon = ref('/static/brand/smartedu-logo-icon.png')
const platformLogoText = ref('/static/brand/smartedu-logo-text.png')
const headerBanner = ref('/static/home/header-banner.png')
const open = ref(false)

const roots = computed(() => items.value
  .filter(item => item.position === 'MAIN' && item.parentId == null)
  .sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id))

const children = (id: number) => items.value
  .filter(item => item.parentId === id)
  .sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id)

const isActive = (item: PublicNavigation) => {
  if (item.targetType === 'HOME') return route.path === '/'
  if (item.clickable && item.href && item.href !== '#') return route.path === item.href || route.path.startsWith(`${item.href}/`)
  return children(item.id).some(child => child.clickable && child.href && (route.path === child.href || route.path.startsWith(`${child.href}/`)))
}

onMounted(async () => {
  const [navigation, config] = await Promise.all([listPublicNavigations(), listPublicSiteConfig()])
  items.value = navigation
  const values = Object.fromEntries(config.map(item => [item.key, item.value]))
  siteName.value = values.SITE_NAME || siteName.value
  platformLogoIcon.value = values.PLATFORM_LOGO_ICON_PATH || platformLogoIcon.value
  platformLogoText.value = values.PLATFORM_LOGO_TEXT_PATH || values.LOGO_PATH || platformLogoText.value
  headerBanner.value = values.HEADER_BANNER_PATH || headerBanner.value
})
</script>

<template>
  <header class="site-header">
    <div class="platform-bar">
      <div class="site-width platform-bar-inner">
        <a class="platform-brand" href="https://www.jl.smartedu.cn/" target="_blank" rel="noopener noreferrer" aria-label="吉林智慧教育平台">
          <img :src="platformLogoIcon" alt="" class="platform-logo-icon">
          <img :src="platformLogoText" alt="吉林智慧教育平台" class="platform-logo-text">
        </a>
        <a class="student-entry" href="https://zhjy.jilinjobs.cn" target="_blank" rel="noopener noreferrer">
          <img src="/static/icons/student.png" alt="">
          <span>我是学生</span>
        </a>
      </div>
    </div>

    <router-link class="site-hero" to="/" :style="{ backgroundImage: `url(${headerBanner})` }">
      <span class="sr-only">{{ siteName }}</span>
    </router-link>

    <nav id="main-navigation" class="site-nav" :class="{ 'is-open': open }" aria-label="主导航">
      <div class="mobile-nav-head site-width">
        <span>网站导航</span>
        <button class="navigation-toggle" aria-controls="main-navigation" :aria-expanded="open" @click="open = !open">
          {{ open ? '收起导航' : '展开导航' }}
        </button>
      </div>
      <ul class="site-width nav-root">
        <li v-for="item in roots" :key="item.id" class="nav-item" :class="{ active: isActive(item) }">
          <a v-if="item.external" :data-testid="`public-nav-${item.id}`" :href="item.href" :target="item.newWindow ? '_blank' : undefined" rel="noopener noreferrer">
            <span>{{ item.name }}</span>
            <img v-if="children(item.id).length" class="nav-arrow" src="/static/icons/arrow-down.png" alt="">
          </a>
          <router-link v-else-if="item.clickable" :data-testid="`public-nav-${item.id}`" :to="item.href">
            <span>{{ item.name }}</span>
            <img v-if="children(item.id).length" class="nav-arrow" src="/static/icons/arrow-down.png" alt="">
          </router-link>
          <span v-else class="nav-placeholder">
            <span>{{ item.name }}</span>
            <img v-if="children(item.id).length" class="nav-arrow" src="/static/icons/arrow-down.png" alt="">
          </span>
          <ul v-if="children(item.id).length" class="nav-children">
            <li v-for="child in children(item.id)" :key="child.id">
              <a v-if="child.external" :href="child.href" :target="child.newWindow ? '_blank' : undefined" rel="noopener noreferrer">{{ child.name }}</a>
              <router-link v-else-if="child.clickable" :to="child.href">{{ child.name }}</router-link>
              <span v-else>{{ child.name }}</span>
            </li>
          </ul>
        </li>
      </ul>
    </nav>
  </header>
</template>
