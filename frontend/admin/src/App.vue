<script setup lang="ts">
const items = [
  { to: '/articles', label: '文章管理', icon: '文' },
  { to: '/columns', label: '栏目管理', icon: '栏' },
  { to: '/navigation', label: '导航管理', icon: '导' },
  { to: '/pages', label: '固定页面', icon: '页' },
  { to: '/lists', label: '列表管理', icon: '列' },
  { to: '/advertisements', label: '广告管理', icon: '广' },
  { to: '/site-config', label: '网站属性', icon: '属' },
  { to: '/static-resources', label: '静态资源', icon: '资' },
]
</script>

<template>
  <div class="admin-app">
    <aside class="admin-sidebar">
      <div class="admin-brand">
        <strong>吉林就业 CMS</strong>
        <span>中心主站内容管理</span>
      </div>
      <nav class="admin-nav" aria-label="内容管理导航">
        <router-link v-for="item in items" :key="item.to" :to="item.to" class="admin-nav-item" :data-testid="`admin-nav-${item.to.slice(1)}`">
          <span class="admin-nav-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
      <a class="public-site-link" href="/" target="_blank" rel="noopener">查看公开站 ↗</a>
    </aside>
    <div class="admin-workspace">
      <header class="admin-topbar">
        <div>
          <strong>内容管理后台</strong>
          <span>通用 CMS 模型与公开站独立构建</span>
        </div>
      </header>
      <router-view />
    </div>
  </div>
</template>
