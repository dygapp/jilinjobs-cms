<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { listPublicNavigations, type PublicNavigation } from '../../../shared/api/navigation'

const route = useRoute()
const items = ref<PublicNavigation[]>([])
const menuOpen = ref(false)
const headerBanner = '/static/party-building/party-header-banner.avif'
const arrowIcon = '/static/icons/arrow-down.png'

const roots = computed(() => items.value
  .filter(item => item.position === 'MAIN' && item.parentId == null)
  .sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id))

const children = (id: number) => items.value
  .filter(item => item.parentId === id)
  .sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id)

const isPartyRoute = (item: PublicNavigation) =>
  item.clickable && item.href !== '#' && (item.href === '/party' || item.href.startsWith('/party/'))

const usesDocumentNavigation = (item: PublicNavigation) => item.external || !isPartyRoute(item)

const matchesRoute = (item: PublicNavigation) =>
  item.clickable && item.href && item.href !== '#'
    && (route.path === item.href || route.path.startsWith(`${item.href.replace(/\/$/, '')}/`))

const isActive = (item: PublicNavigation) => matchesRoute(item) || children(item.id).some(matchesRoute)

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

    <nav id="party-main-navigation" class="party-navigation" :class="{ 'is-open': menuOpen }" aria-label="网站主导航">
      <div class="party-mobile-nav-head party-width">
        <span>网站导航</span>
        <button type="button" aria-controls="party-main-navigation" :aria-expanded="menuOpen" @click="menuOpen = !menuOpen">
          {{ menuOpen ? '收起导航' : '展开导航' }}
        </button>
      </div>
      <ul class="party-width party-nav-root">
        <li v-for="item in roots" :key="item.id" class="party-nav-item" :class="{ active: isActive(item) }">
          <a
            v-if="item.clickable && usesDocumentNavigation(item)"
            class="party-nav-link"
            :data-testid="`party-main-nav-${item.id}`"
            :href="item.href"
            :target="item.newWindow ? '_blank' : undefined"
            :rel="item.newWindow ? 'noopener noreferrer' : undefined"
          >
            <span>{{ item.name }}</span>
            <img v-if="children(item.id).length" class="party-nav-arrow" :src="arrowIcon" alt="">
          </a>
          <router-link
            v-else-if="item.clickable"
            class="party-nav-link"
            :data-testid="`party-main-nav-${item.id}`"
            :to="item.href"
          >
            <span>{{ item.name }}</span>
            <img v-if="children(item.id).length" class="party-nav-arrow" :src="arrowIcon" alt="">
          </router-link>
          <span v-else class="party-nav-link party-nav-placeholder">
            <span>{{ item.name }}</span>
            <img v-if="children(item.id).length" class="party-nav-arrow" :src="arrowIcon" alt="">
          </span>

          <ul v-if="children(item.id).length" class="party-nav-children">
            <li v-for="child in children(item.id)" :key="child.id" :class="{ active: matchesRoute(child) }">
              <a
                v-if="child.clickable && usesDocumentNavigation(child)"
                :href="child.href"
                :target="child.newWindow ? '_blank' : undefined"
                :rel="child.newWindow ? 'noopener noreferrer' : undefined"
              >{{ child.name }}</a>
              <router-link v-else-if="child.clickable" :to="child.href">{{ child.name }}</router-link>
              <span v-else>{{ child.name }}</span>
            </li>
          </ul>
        </li>
      </ul>
    </nav>
  </header>
</template>

<style scoped>
.party-nav-item {
  position: relative;
}

.party-nav-root .party-nav-link {
  gap: 7px;
  font-family: "Microsoft YaHei", "PingFang SC", Arial, sans-serif;
  font-size: 14px;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
}

.party-nav-arrow {
  width: 16px;
  height: 16px;
  object-fit: contain;
  flex: none;
}

.party-nav-children {
  display: none;
  position: absolute;
  z-index: 50;
  top: 60px;
  left: 0;
  min-width: 160px;
  margin: 0;
  padding: 5px 0;
  list-style: none;
  background: #fff;
  color: #323b47;
  border: 1px solid #ead9dc;
  box-shadow: 0 8px 18px rgba(120, 0, 20, .18);
  text-align: left;
}

.party-nav-item:hover > .party-nav-children,
.party-nav-item:focus-within > .party-nav-children {
  display: block;
}

.party-nav-children li {
  width: auto;
  min-width: 160px;
  text-align: left;
}

.party-nav-children a,
.party-nav-children span {
  display: block;
  padding: 11px 18px;
  color: #323b47;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.5;
  text-decoration: none;
  white-space: nowrap;
}

.party-nav-children a:hover,
.party-nav-children li.active > a {
  background: #fff0f2;
  color: #ad001d;
}

@media (max-width: 900px) {
  .party-navigation.is-open .party-nav-root {
    display: block;
  }

  .party-nav-root .party-nav-item {
    width: 100%;
    text-align: left;
  }

  .party-nav-root .party-nav-link {
    min-height: 42px;
    justify-content: flex-start;
    padding: 0 12px;
    font-size: 14px;
    font-weight: 400;
  }

  .party-nav-children {
    position: static;
    display: block;
    min-width: 0;
    margin: 0 0 4px;
    padding: 0 0 0 18px;
    border: 0;
    box-shadow: none;
    background: rgba(130, 0, 22, .18);
    color: #fff;
  }

  .party-nav-children li {
    min-width: 0;
    width: 100%;
  }

  .party-nav-children a,
  .party-nav-children span {
    padding: 9px 12px;
    color: #fff;
    white-space: normal;
  }

  .party-nav-children a:hover,
  .party-nav-children li.active > a {
    background: rgba(255,255,255,.12);
    color: #fff;
  }
}
</style>
