<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getPublicColumn, type PublicColumn } from '../../api/columns'

const route = useRoute()
const column = ref<PublicColumn | null>(null)
const error = ref('')
const loading = ref(false)

watch(
  () => route.params.id,
  async (value) => {
    const id = Number(value)
    column.value = null
    error.value = ''
    if (!Number.isInteger(id) || id <= 0) {
      error.value = '栏目地址无效'
      return
    }

    loading.value = true
    try {
      column.value = await getPublicColumn(id)
    } catch (reason) {
      error.value = reason instanceof Error ? reason.message : '栏目加载失败'
    } finally {
      loading.value = false
    }
  },
  { immediate: true },
)
</script>

<template>
  <main class="public-shell">
    <header class="public-header compact-header">
      <router-link class="brand-block brand-link" to="/">
        <span class="brand-mark">吉林就业</span>
        <strong>吉林省智慧就业云平台</strong>
      </router-link>
    </header>

    <section class="column-entry">
      <p class="breadcrumb"><router-link to="/">首页</router-link><span>/</span>栏目</p>
      <p v-if="loading" class="public-state">正在加载栏目…</p>
      <div v-else-if="column">
        <p class="eyebrow">栏目入口</p>
        <h1>{{ column.name }}</h1>
        <div class="column-placeholder" aria-label="栏目内容区域"></div>
      </div>
      <p v-else class="public-state error-text">{{ error }}</p>
    </section>
  </main>
</template>
