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
          <p>当前页面用于承载中心党建独立站点入口、红色视觉主题和后续页面模块。正式栏目、内容与原站视觉细节将在下一阶段专项完善。</p>
        </div>
      </section>

      <section class="party-width party-foundation" aria-label="中心党建基础框架">
        <article>
          <span>01</span>
          <h2>党建动态</h2>
          <p>预留党建信息发布内容区域，后续根据原站结构和正式需求接入 CMS 数据。</p>
        </article>
        <article>
          <span>02</span>
          <h2>学习教育</h2>
          <p>预留学习教育内容区域，本阶段不提前定义具体栏目或运营数据。</p>
        </article>
        <article>
          <span>03</span>
          <h2>组织建设</h2>
          <p>预留组织建设内容区域，为后续真实页面设计提供稳定扩展位置。</p>
        </article>
      </section>
    </main>
    <PartyBuildingFooter />
  </div>
</template>
