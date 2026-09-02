<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { MAIN_SITE_CONFIG_KEYS, useMainSiteContext } from '../app/siteContext'
import type { PublicNavigation } from '../api/navigation'

const route = useRoute()
const { navigation: items, config } = useMainSiteContext()
const defaultSiteName = '吉林省高等学校毕业生就业信息网'
const defaultPlatformLogoIcon = '/static/brand/smartedu-logo-icon.png'
const defaultPlatformLogoText = '/static/brand/smartedu-logo-text.png'
const defaultHeaderBanner = '/static/home/header-banner.png'
const studentIcon = '/static/icons/student.png'
const arrowIcon = '/static/icons/arrow-down.png'
const open = ref(false)

const siteName = computed(() => config.value[MAIN_SITE_CONFIG_KEYS.SITE_NAME] || defaultSiteName)
const platformLogoIcon = computed(() => config.value[MAIN_SITE_CONFIG_KEYS.PLATFORM_LOGO_ICON_PATH] || defaultPlatformLogoIcon)
const platformLogoText = computed(() => config.value[MAIN_SITE_CONFIG_KEYS.PLATFORM_LOGO_TEXT_PATH]
  || config.value[MAIN_SITE_CONFIG_KEYS.LEGACY_LOGO_PATH]
  || defaultPlatformLogoText)
const headerBanner = computed(() => config.value[MAIN_SITE_CONFIG_KEYS.HEADER_BANNER_PATH] || defaultHeaderBanner)

const roots = computed(() => items.value
  .filter(item => item.position === 'MAIN' && item.parentId == null)
  .sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id))

const children = (id: number) => items.value
  .filter(item => item.parentId === id)
  .sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id)

const isOtherPublicSiteEntry = (item: PublicNavigation) =>
  item.targetType === 'LINK' && (item.href === '/party' || item.href.startsWith('/party/'))

const usesDocumentNavigation = (item: PublicNavigation) => item.external || isOtherPublicSiteEntry(item)

const isActive = (item: PublicNavigation) => {
  if (item.targetType === 'HOME') return route.path === '/'
  if (item.clickable && item.href && item.href !== '#') return route.path === item.href || route.path.startsWith(`${item.href}/`)
  return children(item.id).some(child => child.clickable && child.href && (route.path === child.href || route.path.startsWith(`${child.href}/`)))
}
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
          <img :src="studentIcon" alt="">
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
          <a v-if="usesDocumentNavigation(item)" :data-testid="`public-nav-${item.id}`" :href="item.href" :target="item.newWindow ? '_blank' : undefined" rel="noopener noreferrer">
            <span>{{ item.name }}</span>
            <img v-if="children(item.id).length" class="nav-arrow" :src="arrowIcon" alt="">
          </a>
          <router-link v-else-if="item.clickable" :data-testid="`public-nav-${item.id}`" :to="item.href">
            <span>{{ item.name }}</span>
            <img v-if="children(item.id).length" class="nav-arrow" :src="arrowIcon" alt="">
          </router-link>
          <span v-else class="nav-placeholder">
            <span>{{ item.name }}</span>
            <img v-if="children(item.id).length" class="nav-arrow" :src="arrowIcon" alt="">
          </span>
          <ul v-if="children(item.id).length" class="nav-children">
            <li v-for="child in children(item.id)" :key="child.id">
              <a v-if="usesDocumentNavigation(child)" :href="child.href" :target="child.newWindow ? '_blank' : undefined" rel="noopener noreferrer">{{ child.name }}</a>
              <router-link v-else-if="child.clickable" :to="child.href">{{ child.name }}</router-link>
              <span v-else>{{ child.name }}</span>
            </li>
          </ul>
        </li>
      </ul>
    </nav>
  </header>
</template>
