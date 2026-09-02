<script setup lang="ts">
import PublicSiteFooter from '../shell/PublicSiteFooter.vue'
import PublicSiteHeader from '../shell/PublicSiteHeader.vue'
import { provideMainSiteContext } from './siteContext'

provideMainSiteContext()
</script>

<template>
  <PublicSiteHeader />
  <router-view />
  <PublicSiteFooter />
</template>
