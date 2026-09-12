<script setup lang="ts">
import { computed, ref } from 'vue'
import type { PageStructuredContent } from '../../api/pages'

const props = defineProps<{ content: PageStructuredContent }>()
const supported = computed(() => props.content.schemaVersion === 1 && props.content.kind === 'CARD_COLLECTION')
const expandedCards = ref<number[]>([])

function isExpanded(index: number): boolean {
  return expandedCards.value.includes(index)
}

function toggleCard(index: number) {
  expandedCards.value = isExpanded(index)
    ? expandedCards.value.filter(item => item !== index)
    : [...expandedCards.value, index]
}
</script>

<template>
  <div
    v-if="supported"
    class="guide-card-list"
    data-page-structured-kind="CARD_COLLECTION"
    data-page-schema-version="1"
  >
    <article
      v-for="(card, index) in content.items"
      :key="`${index}-${card.title}`"
      class="guide-card"
      :data-card-index="index"
      :data-card-state="isExpanded(index) ? 'expanded' : 'collapsed'"
    >
      <header class="guide-card-header" :class="{ expanded: isExpanded(index) }">
        <h2>
          <button
            type="button"
            class="guide-card-toggle"
            :aria-expanded="isExpanded(index)"
            :aria-controls="`guide-card-panel-${index}`"
            :data-testid="`guide-card-toggle-${index}`"
            @click="toggleCard(index)"
          >
            <span class="guide-card-title">{{ card.title }}</span>
            <span class="guide-card-indicator" aria-hidden="true">{{ isExpanded(index) ? '−' : '＋' }}</span>
          </button>
        </h2>
      </header>
      <div
        v-show="isExpanded(index)"
        :id="`guide-card-panel-${index}`"
        class="guide-card-body rich-content"
        :data-testid="`guide-card-body-${index}`"
        v-html="card.bodyHtml"
      />
    </article>
  </div>
  <div v-else class="unsupported-page-content" role="alert">
    当前页面内容版本暂不受支持，已停止降级渲染。
  </div>
</template>

<style scoped>
.guide-card-list{display:grid;gap:12px}
.guide-card{overflow:hidden;border:1px solid #e3e8ef;border-radius:4px;background:#fff}
.guide-card-header{background:#f4f7fb}
.guide-card-header.expanded{border-bottom:1px solid #e3e8ef}
.guide-card-header h2{margin:0;font-size:18px;line-height:1.6;font-weight:600;color:#163e72}
.guide-card-toggle{display:flex;width:100%;align-items:center;justify-content:space-between;gap:16px;padding:14px 20px;border:0;background:transparent;color:inherit;font:inherit;text-align:left;cursor:pointer}
.guide-card-toggle:focus-visible{outline:2px solid currentColor;outline-offset:-3px}
.guide-card-title{min-width:0}
.guide-card-indicator{flex:0 0 auto;font-size:22px;line-height:1;font-weight:400}
.guide-card-body{padding:20px}
.guide-card-body :deep(img){display:block;max-width:100%;height:auto;margin-left:auto;margin-right:auto}
.unsupported-page-content{padding:24px;border:1px solid #d9d9d9;background:#fafafa;color:#555}
</style>
