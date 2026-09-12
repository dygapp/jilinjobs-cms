<script setup lang="ts">
import { computed } from 'vue'
import type { PublicPage } from '../../api/pages'
import GuideCardsRenderer from './GuideCardsRenderer.vue'
import { resolvePageRenderer } from './pageRendererRegistry'

const props=defineProps<{page:PublicPage;grouped?:boolean}>()
const renderer=computed(()=>resolvePageRenderer(props.page.rendererKey))
const structuredCards=computed(()=>{
  if(renderer.value!=='jilinjobs-guide-cards')return null
  const content=props.page.structuredContent
  if(props.page.contentModel!=='STRUCTURED'||!content||content.schemaVersion!==1||content.kind!=='CARD_COLLECTION')return null
  return content
})
</script>

<template>
  <div v-if="renderer==='rich'&&page.contentModel==='RICH_TEXT'" class="rich-content" data-page-renderer="RICH_TEXT" v-html="page.bodyHtml" />

  <div v-else-if="renderer==='embed-placeholder'&&page.contentModel==='NONE'" class="embed-placeholder" data-page-renderer="EMBED_PLACEHOLDER">
    <h1 v-if="grouped">{{ page.name }}</h1>
    <p>该内容由外部平台提供，本轮保留本站页面框架与内容区域，实际嵌入将在后续集成阶段完成。</p>
  </div>

  <div v-else-if="renderer==='internal-static'&&page.contentModel==='NONE'" class="rich-content" data-page-renderer="INTERNAL_STATIC" v-html="page.bodyHtml" />

  <GuideCardsRenderer
    v-else-if="renderer==='jilinjobs-guide-cards'&&structuredCards"
    :content="structuredCards"
    data-page-renderer="JILINJOBS_GUIDE_CARDS"
  />

  <div v-else class="unsupported-page-renderer" role="alert" :data-unsupported-renderer="page.rendererKey">
    当前页面的内容或呈现方式暂不受支持，已停止自动降级渲染。
  </div>
</template>

<style scoped>
.unsupported-page-renderer{padding:24px;border:1px solid #d9d9d9;background:#fafafa;color:#555}
</style>
