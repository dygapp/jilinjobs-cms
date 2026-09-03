<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listPublicSiteConfig, type SiteConfigItem } from '../../../shared/api/siteConfig'
import SharedPublicFooter from '../../../shared/components/PublicFooter.vue'

const items = ref<SiteConfigItem[]>([])
const config = computed(() => Object.fromEntries(items.value.filter(item => item.enabled).map(item => [item.key, item.value])))

onMounted(async () => {
  try {
    items.value = await listPublicSiteConfig()
  } catch {
    items.value = []
  }
})
</script>

<template>
  <SharedPublicFooter :config="config" theme="party" test-id="party-footer" />
</template>
