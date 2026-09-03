<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listPublicNavigations, type PublicNavigation } from '../../../shared/api/navigation'

const items = ref<PublicNavigation[]>([])
const menuOpen = ref(false)
const headerBanner = '/static/party-building/party-header-banner.avif'

const roots = computed(() => items.value
  .filter(item => item.position === 'MAIN' && item.parentId == null)
  .sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id))

onMounted(async () => {
  try {
    items.value = await listPublicNavigations()
  } catch {
    items.value = []
  }
})

function target(item: PublicNavigation) {
  return item.newWindow ? '_blank' : undefined
}

function isPartyEntry(item: PublicNavigation) {
  return item.href === '/party/' || item.href === '/party' || item.name === '中心党建'
}
</script>

<template>
  <header class="party-header" data-testid="party-building-header">
    <a class="party-banner" href="/party/" :style="{ backgroundImage: `url(${headerBanner})` }" aria-label="吉林省高等学校毕业生就业信息网党员之家">
      <span class="party-sr-only">吉林省高等学校毕业生就业信息网党员之家</span>
    </a>

    <nav class="party-navigation" :class="{ 'is-open': menuOpen }" aria-label="网站主导航">
      <div class="party-mobile-nav-head party-width">
        <span>网站导航</span>
        <button type="button" :aria-expanded="menuOpen" @click="menuOpen = !menuOpen">{{ menuOpen ? '收起' : '展开' }}</button>
      </div>
      <ul class="party-width party-nav-root">
        <li v-for="item in roots" :key="item.id" :class="{ active: isPartyEntry(item) }">
          <a
            v-if="item.clickable"
            :data-testid="`party-main-nav-${item.id}`"
            :href="item.href"
            :target="target(item)"
            :rel="target(item) ? 'noopener noreferrer' : undefined"
          >{{ item.name }}</a>
          <span v-else>{{ item.name }}</span>
        </li>
      </ul>
    </nav>
  </header>
</template>
