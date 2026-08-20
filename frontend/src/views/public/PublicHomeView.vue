<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listPublicNavigations, type PublicNavigation } from '../../api/navigation'

const items = ref<PublicNavigation[]>([])
const loading = ref(true)
const error = ref('')

const mainItems = computed(() => items.value.filter((item) => item.position === 'MAIN'))
const serviceItems = computed(() => items.value.filter((item) => item.position === 'SERVICE'))
const siteGroups = computed(() => {
  const groups = new Map<string, PublicNavigation[]>()
  items.value
    .filter((item) => item.position === 'SITE')
    .forEach((item) => {
      const key = item.category || '网站导航'
      const current = groups.get(key) ?? []
      current.push(item)
      groups.set(key, current)
    })
  return Array.from(groups, ([name, links]) => ({ name, links }))
})

onMounted(async () => {
  try {
    items.value = await listPublicNavigations()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '导航加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <main class="public-shell">
    <header class="public-header">
      <div class="brand-block">
        <span class="brand-mark">吉林就业</span>
        <strong>吉林省智慧就业云平台</strong>
      </div>
      <nav class="main-navigation" aria-label="主导航">
        <template v-for="item in mainItems" :key="item.id">
          <a
            v-if="item.external"
            :data-testid="`public-nav-${item.id}`"
            :href="item.href"
            target="_blank"
            rel="noopener noreferrer"
          >{{ item.name }}</a>
          <router-link v-else :data-testid="`public-nav-${item.id}`" :to="item.href">{{ item.name }}</router-link>
        </template>
      </nav>
    </header>

    <section class="public-hero">
      <p class="eyebrow">中心主站</p>
      <h1>就业信息与公共服务入口</h1>
      <p>当前原型先验证可配置导航与公开入口，内容发布能力将在后续执行单元继续接入。</p>
    </section>

    <p v-if="loading" class="public-state">正在加载导航…</p>
    <p v-else-if="error" class="public-state error-text">{{ error }}</p>

    <section v-if="serviceItems.length" class="public-section">
      <div class="section-heading">
        <span>服务入口</span>
        <h2>常用服务</h2>
      </div>
      <div class="service-grid">
        <template v-for="item in serviceItems" :key="item.id">
          <a v-if="item.external" :href="item.href" target="_blank" rel="noopener noreferrer">{{ item.name }}</a>
          <router-link v-else :to="item.href">{{ item.name }}</router-link>
        </template>
      </div>
    </section>

    <section v-if="siteGroups.length" class="public-section site-navigation">
      <div class="section-heading">
        <span>网站导航</span>
        <h2>相关站点</h2>
      </div>
      <div class="site-groups">
        <div v-for="group in siteGroups" :key="group.name" class="site-group">
          <h3>{{ group.name }}</h3>
          <div class="site-links">
            <template v-for="item in group.links" :key="item.id">
              <a v-if="item.external" :href="item.href" target="_blank" rel="noopener noreferrer">{{ item.name }}</a>
              <router-link v-else :to="item.href">{{ item.name }}</router-link>
            </template>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>
