<script setup lang="ts">
import { computed } from 'vue'
import HuiEmploymentFrame from '../../components/HuiEmploymentFrame.vue'
import { resolveHuiEmploymentPageTarget } from '../../integrations/huiEmployment'

const props = defineProps<{ rendererKey: string }>()
const target = computed(() => resolveHuiEmploymentPageTarget(props.rendererKey))
</script>

<template>
  <HuiEmploymentFrame
    v-if="target"
    :src="target.url"
    :title="target.title"
    :height="target.height"
    variant="page"
    :test-id="`hui-employment-page-${rendererKey}`"
    data-page-renderer="HUI_EMPLOYMENT"
  />
  <div v-else class="unsupported-page-renderer" role="alert" :data-unsupported-renderer="rendererKey">
    当前慧就业页面映射不存在，已停止加载外部内容。
  </div>
</template>
