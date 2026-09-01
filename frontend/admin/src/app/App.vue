<script setup lang="ts">
import { ref } from 'vue'
import { Expand, Fold } from '@element-plus/icons-vue'
import { adminNavigationSections as sections } from './moduleRegistry'

const sidebarCollapsed = ref(false)
</script>

<template>
  <div class="admin-app" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <aside class="admin-sidebar">
      <div class="admin-brand">
        <strong>{{ sidebarCollapsed ? 'CMS' : '吉林就业 CMS' }}</strong>
        <span v-if="!sidebarCollapsed">中心主站内容管理</span>
      </div>
      <nav class="admin-nav" aria-label="内容管理导航">
        <section v-for="section in sections" :key="section.label" class="admin-nav-section" :data-testid="`admin-nav-section-${section.label}`">
          <div class="admin-nav-section-title">{{ section.label }}</div>
          <router-link v-for="item in section.items" :key="item.to" :to="item.to" class="admin-nav-item" :data-testid="`admin-nav-${item.to.split('/').pop()}`" :title="sidebarCollapsed ? item.label : undefined">
            <span class="admin-nav-icon">{{ item.icon }}</span>
            <span class="admin-nav-label">{{ item.label }}</span>
          </router-link>
        </section>
      </nav>
      <a class="public-site-link" href="/" target="_blank" rel="noopener" title="查看公开站"><span class="public-site-label">查看公开站</span><span>↗</span></a>
    </aside>
    <div class="admin-workspace">
      <header class="admin-topbar">
        <el-tooltip :content="sidebarCollapsed ? '展开主导航' : '收起主导航'" placement="bottom" :show-after="250">
          <el-button
            class="admin-sidebar-toggle"
            data-testid="admin-sidebar-toggle"
            text
            circle
            :aria-label="sidebarCollapsed ? '展开主导航' : '收起主导航'"
            :icon="sidebarCollapsed ? Expand : Fold"
            @click="sidebarCollapsed = !sidebarCollapsed"
          />
        </el-tooltip>
        <div class="admin-topbar-main">
          <strong>内容管理后台</strong>
          <span>通用 CMS 模型与公开站独立构建</span>
        </div>
      </header>
      <router-view />
    </div>
  </div>
</template>
