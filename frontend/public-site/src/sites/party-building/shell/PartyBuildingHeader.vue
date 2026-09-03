<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listPublicNavigations, type PublicNavigation } from '../../../shared/api/navigation'
import SharedPublicNavigation from '../../../shared/components/PublicNavigation.vue'

const items = ref<PublicNavigation[]>([])
const headerBanner = '/static/party-building/party-header-banner.avif'

onMounted(async () => {
  try {
    items.value = await listPublicNavigations()
  } catch {
    items.value = []
  }
})
</script>

<template>
  <header class="party-header" data-testid="party-building-header">
    <a class="party-banner" href="/party/" :style="{ backgroundImage: `url(${headerBanner})` }" aria-label="吉林省高等学校毕业生就业信息网党员之家">
      <span class="party-sr-only">吉林省高等学校毕业生就业信息网党员之家</span>
    </a>

    <SharedPublicNavigation
      :items="items"
      site-root="/party"
      theme="party"
      navigation-id="party-main-navigation"
      aria-label="网站主导航"
      test-id-prefix="party-main-nav"
    />
  </header>
</template>
