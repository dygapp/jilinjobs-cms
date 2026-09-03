<script setup lang="ts">
import { computed } from 'vue'
import { MAIN_SITE_CONFIG_KEYS, useMainSiteContext } from '../app/siteContext'
import SharedPublicNavigation from '../../../shared/components/PublicNavigation.vue'

const { navigation: items, config } = useMainSiteContext()
const defaultSiteName = '吉林省高等学校毕业生就业信息网'
const defaultPlatformLogoIcon = '/static/brand/smartedu-logo-icon.png'
const defaultPlatformLogoText = '/static/brand/smartedu-logo-text.png'
const defaultHeaderBanner = '/static/home/header-banner.png'
const studentIcon = '/static/icons/student.png'

const siteName = computed(() => config.value[MAIN_SITE_CONFIG_KEYS.SITE_NAME] || defaultSiteName)
const platformLogoIcon = computed(() => config.value[MAIN_SITE_CONFIG_KEYS.PLATFORM_LOGO_ICON_PATH] || defaultPlatformLogoIcon)
const platformLogoText = computed(() => config.value[MAIN_SITE_CONFIG_KEYS.PLATFORM_LOGO_TEXT_PATH]
  || config.value[MAIN_SITE_CONFIG_KEYS.LEGACY_LOGO_PATH]
  || defaultPlatformLogoText)
const headerBanner = computed(() => config.value[MAIN_SITE_CONFIG_KEYS.HEADER_BANNER_PATH] || defaultHeaderBanner)
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

    <SharedPublicNavigation
      :items="items"
      site-root="/"
      :cross-entry-roots="['/party']"
      theme="main"
      navigation-id="main-navigation"
      aria-label="主导航"
      test-id-prefix="public-nav"
    />
  </header>
</template>
