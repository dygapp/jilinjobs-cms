<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import type { PublicNavigation } from '../api/navigation'
import '../styles/public-shell.css'

const props = withDefaults(defineProps<{
  items: PublicNavigation[]
  siteRoot: string
  theme: 'main' | 'party'
  crossEntryRoots?: string[]
  navigationId?: string
  ariaLabel?: string
  testIdPrefix?: string
}>(), {
  crossEntryRoots: () => [],
  navigationId: 'public-navigation',
  ariaLabel: '主导航',
  testIdPrefix: 'public-nav',
})

const route = useRoute()
const open = ref(false)
const arrowIcon = '/static/icons/arrow-down.png'

const normalizePath = (value: string) => {
  const path = value.split(/[?#]/, 1)[0] || '/'
  if (path === '/') return '/'
  return path.replace(/\/+$/, '')
}

const normalizedSiteRoot = computed(() => normalizePath(props.siteRoot))

const roots = computed(() => props.items
  .filter(item => item.position === 'MAIN' && item.parentId == null)
  .sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id))

const children = (id: number) => props.items
  .filter(item => item.parentId === id)
  .sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id)

const isCrossEntryHref = (href: string) => {
  const path = normalizePath(href)
  return props.crossEntryRoots.some(root => {
    const normalizedRoot = normalizePath(root)
    return path === normalizedRoot || path.startsWith(`${normalizedRoot}/`)
  })
}

const isInternalHref = (href: string) => {
  if (!href.startsWith('/') || href === '#') return false
  if (isCrossEntryHref(href)) return false
  const path = normalizePath(href)
  const root = normalizedSiteRoot.value
  if (root === '/') return true
  return path === root || path.startsWith(`${root}/`)
}

const usesDocumentNavigation = (item: PublicNavigation) =>
  item.external || !isInternalHref(item.href)

const matchesRoute = (item: PublicNavigation) => {
  if (!item.clickable || !item.href || !isInternalHref(item.href)) return false
  const current = normalizePath(route.path)
  const target = normalizePath(item.href)
  if (item.targetType === 'HOME') return current === normalizedSiteRoot.value
  if (target === '/') return current === '/'
  return current === target || current.startsWith(`${target}/`)
}

const isActive = (item: PublicNavigation) =>
  matchesRoute(item) || children(item.id).some(matchesRoute)

const legacyClass = computed(() => props.theme === 'main' ? 'site-nav' : 'party-navigation')
</script>

<template>
  <nav
    :id="navigationId"
    class="shared-public-navigation"
    :class="[legacyClass, `shared-public-navigation--${theme}`, { 'is-open': open }]"
    :aria-label="ariaLabel"
    data-component="public-navigation"
    :data-theme="theme"
  >
    <div class="shared-public-nav-mobile shared-public-shell-width">
      <span>网站导航</span>
      <button
        type="button"
        class="shared-public-nav-toggle"
        :aria-controls="navigationId"
        :aria-expanded="open"
        @click="open = !open"
      >
        {{ open ? '收起导航' : '展开导航' }}
      </button>
    </div>

    <ul class="shared-public-shell-width shared-public-nav-root">
      <li
        v-for="item in roots"
        :key="item.id"
        class="shared-public-nav-item"
        :class="{ active: isActive(item) }"
      >
        <a
          v-if="item.clickable && usesDocumentNavigation(item)"
          class="shared-public-nav-link"
          :data-testid="`${testIdPrefix}-${item.id}`"
          :href="item.href"
          :target="item.newWindow ? '_blank' : undefined"
          :rel="item.newWindow ? 'noopener noreferrer' : undefined"
        >
          <span>{{ item.name }}</span>
          <img v-if="children(item.id).length" class="shared-public-nav-arrow" :src="arrowIcon" alt="">
        </a>
        <RouterLink
          v-else-if="item.clickable"
          class="shared-public-nav-link"
          :data-testid="`${testIdPrefix}-${item.id}`"
          :to="item.href"
        >
          <span>{{ item.name }}</span>
          <img v-if="children(item.id).length" class="shared-public-nav-arrow" :src="arrowIcon" alt="">
        </RouterLink>
        <span v-else class="shared-public-nav-link shared-public-nav-placeholder">
          <span>{{ item.name }}</span>
          <img v-if="children(item.id).length" class="shared-public-nav-arrow" :src="arrowIcon" alt="">
        </span>

        <ul v-if="children(item.id).length" class="shared-public-nav-children">
          <li
            v-for="child in children(item.id)"
            :key="child.id"
            :class="{ active: matchesRoute(child) }"
          >
            <a
              v-if="child.clickable && usesDocumentNavigation(child)"
              :href="child.href"
              :target="child.newWindow ? '_blank' : undefined"
              :rel="child.newWindow ? 'noopener noreferrer' : undefined"
            >{{ child.name }}</a>
            <RouterLink v-else-if="child.clickable" :to="child.href">{{ child.name }}</RouterLink>
            <span v-else>{{ child.name }}</span>
          </li>
        </ul>
      </li>
    </ul>
  </nav>
</template>
