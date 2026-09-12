<script setup lang="ts">
import type { PageStructuredContent } from '../../api/pages'

const props=defineProps<{content:PageStructuredContent}>()
const supported=props.content.schemaVersion===1&&props.content.kind==='CARD_COLLECTION'
</script>

<template>
  <div v-if="supported" class="guide-card-list" data-page-structured-kind="CARD_COLLECTION" data-page-schema-version="1">
    <article v-for="(card,index) in content.items" :key="`${index}-${card.title}`" class="guide-card" :data-card-index="index">
      <header class="guide-card-header">
        <h2>{{ card.title }}</h2>
      </header>
      <div class="guide-card-body rich-content" v-html="card.bodyHtml" />
    </article>
  </div>
  <div v-else class="unsupported-page-content" role="alert">
    当前页面内容版本暂不受支持，已停止降级渲染。
  </div>
</template>

<style scoped>
.guide-card-list{display:grid;gap:20px}
.guide-card{overflow:hidden;border:1px solid #e3e8ef;border-radius:4px;background:#fff}
.guide-card-header{padding:14px 20px;background:#f4f7fb;border-bottom:1px solid #e3e8ef}
.guide-card-header h2{margin:0;font-size:18px;line-height:1.6;font-weight:600;color:#163e72}
.guide-card-body{padding:20px}
.unsupported-page-content{padding:24px;border:1px solid #d9d9d9;background:#fafafa;color:#555}
</style>
