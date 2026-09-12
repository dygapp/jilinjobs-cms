<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getPublicGroupedPage, getPublicPage, type PublicPage } from '../../api/pages'
import { setPageMeta, summarizeHtml } from '../../seo'
import PageBodyRenderer from './PageBodyRenderer.vue'

const route = useRoute()
const item = ref<PublicPage | null>(null)
const loading = ref(false)
const error = ref('')

watch(() => [route.params.group, route.params.alias], load, { immediate: true })

function pageSummarySource(page:PublicPage):string{
  if(page.contentModel==='STRUCTURED'&&page.structuredContent?.kind==='CARD_COLLECTION'){
    return page.structuredContent.items.map(card=>`<h2>${card.title}</h2>${card.bodyHtml}`).join('')
  }
  return page.bodyHtml
}

async function load() {
  loading.value = true
  error.value = ''
  item.value = null
  try {
    const group = typeof route.params.group === 'string' ? route.params.group : null
    const alias = String(route.params.alias || '')
    item.value = group ? await getPublicGroupedPage(group, alias) : await getPublicPage(alias)
    setPageMeta({ title: item.value.name, description: summarizeHtml(pageSummarySource(item.value), item.value.name) })
  } catch (e) {
    error.value = e instanceof Error ? e.message : '页面不可用'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="public-page-shell">
    <div class="site-width public-page-frame">
      <p v-if="loading" class="public-state">正在加载页面…</p>
      <template v-else-if="item">
        <nav class="breadcrumb" aria-label="页面位置">
          <span class="breadcrumb-label">当前位置：</span>
          <template v-for="(crumb, index) in item.breadcrumbs" :key="`${crumb.title}-${index}`">
            <span v-if="index" class="breadcrumb-separator">›</span>
            <router-link v-if="crumb.href" :to="crumb.href">{{ crumb.title }}</router-link>
            <span v-else>{{ crumb.title }}</span>
          </template>
        </nav>

        <template v-if="item.group">
          <nav class="group-tabs" :aria-label="item.group.name">
            <router-link
              v-for="tab in item.group.members"
              :key="tab.href"
              :to="tab.href"
              :class="{ active: tab.alias === item.alias }"
            >{{ tab.name }}</router-link>
          </nav>
          <section class="group-page-content fixed-page-content">
            <PageBodyRenderer :page="item" grouped />
          </section>
        </template>

        <section v-else class="detail-card fixed-detail-card">
          <header class="detail-section-title">
            <h1>{{ item.name }}</h1>
          </header>
          <div class="fixed-page-content fixed-page-body">
            <PageBodyRenderer :page="item" />
          </div>
        </section>
      </template>
      <p v-else class="public-state error-text">{{ error }}</p>
    </div>
  </main>
</template>
