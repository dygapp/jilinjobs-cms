<script setup lang="ts">
import PartyBuildingHeader from '../../shell/PartyBuildingHeader.vue'
import PartyBuildingFooter from '../../shell/PartyBuildingFooter.vue'
import { setPageMeta } from '../../../../shared/seo'

setPageMeta({
  title: '中心党建',
  description: '吉林省高等学校毕业生就业指导中心党建信息公开入口。',
})
</script>

<template>
  <div class="party-site-app" data-testid="party-building-site">
    <PartyBuildingHeader />
    <main class="party-main">
      <section class="party-hero">
        <div class="party-width party-hero-inner">
          <p class="party-kicker">党建引领 · 服务发展</p>
          <h1>中心党建</h1>
          <p>中心党建独立站点基础页面已经建立，正式栏目、内容和视觉细节将在后续专项中按确认后的站点基线完善。</p>
        </div>
      </section>

      <section class="party-width party-foundation party-foundation-placeholder" aria-label="中心党建内容状态">
        <article>
          <h2>内容建设中</h2>
          <p>当前仅提供中心党建独立入口和基础页面框架，不提前定义尚未确认的栏目与运营内容。</p>
        </article>
      </section>
    </main>
    <PartyBuildingFooter />
  </div>
</template>
