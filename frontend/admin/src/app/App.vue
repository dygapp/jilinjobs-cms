<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Expand, Fold } from '@element-plus/icons-vue'
import { adminNavigationSections } from './moduleRegistry'
import {
  adminIdentityState,
  dismissAccessDenied,
  expireReviewIdentity,
  initializeAdminIdentity,
  loginWithReviewProfile,
  logoutReviewIdentity,
} from './adminIdentity'

const route = useRoute()
const sidebarCollapsed = ref(false)
const sections = computed(() => adminNavigationSections
  .map(section => ({
    ...section,
    items: section.items.filter(item => !item.requiredRole || adminIdentityState.identity?.roles.includes(item.requiredRole)),
  }))
  .filter(section => section.items.length > 0))
const roleLabel = computed(() => adminIdentityState.identity?.roles.map(role => role === 'super' ? '超级管理员' : '内容管理员').join('、') ?? '')

watch(() => route.fullPath, dismissAccessDenied)
onMounted(initializeAdminIdentity)
</script>

<template>
  <div v-if="adminIdentityState.phase !== 'authenticated'" class="admin-identity-gate">
    <section class="admin-identity-card" aria-live="polite">
      <div class="admin-identity-brand"><strong>吉林就业 CMS</strong><span>内容管理后台</span></div>
      <template v-if="adminIdentityState.phase === 'checking'">
        <el-icon class="admin-identity-spinner" :size="32"><span class="identity-spinner-dot">●</span></el-icon>
        <h1>正在确认管理身份</h1>
        <p>身份确认完成前不会加载管理数据。</p>
      </template>
      <template v-else-if="adminIdentityState.phase === 'forbidden'">
        <h1>当前身份无访问权限</h1>
        <p>{{ adminIdentityState.message || '当前身份没有 CMS 管理权限，请联系所属平台管理员。' }}</p>
        <el-button data-testid="identity-retry" type="primary" @click="initializeAdminIdentity">重新检查</el-button>
      </template>
      <template v-else-if="adminIdentityState.phase === 'error'">
        <h1>暂时无法确认身份</h1>
        <p>{{ adminIdentityState.message }}</p>
        <el-button data-testid="identity-retry" type="primary" @click="initializeAdminIdentity">重试</el-button>
      </template>
      <template v-else>
        <h1>{{ adminIdentityState.message.includes('失效') ? '管理身份已失效' : '需要管理身份' }}</h1>
        <p>{{ adminIdentityState.message || '请先通过所属管理平台完成认证，然后重新检查。' }}</p>
        <div v-if="adminIdentityState.reviewEnabled" class="review-login" data-testid="review-login">
          <el-tag type="warning" effect="plain">仅限隔离测试环境</el-tag>
          <p>请选择本次测试使用的管理员身份。</p>
          <div class="review-login-actions">
            <el-button data-testid="review-login-admin" :loading="adminIdentityState.busy" @click="loginWithReviewProfile('admin')">以内容管理员登录</el-button>
            <el-button data-testid="review-login-super" type="primary" :loading="adminIdentityState.busy" @click="loginWithReviewProfile('super')">以超级管理员登录</el-button>
          </div>
        </div>
        <el-button v-else data-testid="identity-retry" type="primary" @click="initializeAdminIdentity">重新检查</el-button>
      </template>
      <a href="/" class="identity-public-link">返回公开站</a>
    </section>
  </div>

  <div v-else class="admin-app" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
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
          <el-button class="admin-sidebar-toggle" data-testid="admin-sidebar-toggle" text circle :aria-label="sidebarCollapsed ? '展开主导航' : '收起主导航'" :icon="sidebarCollapsed ? Expand : Fold" @click="sidebarCollapsed = !sidebarCollapsed" />
        </el-tooltip>
        <div class="admin-topbar-main">
          <strong>内容管理后台</strong>
          <span>通用 CMS 模型与公开站独立构建</span>
        </div>
        <div class="admin-identity-summary" data-testid="admin-identity-summary">
          <span><strong>{{ adminIdentityState.identity?.userId }}</strong> · {{ roleLabel }}</span>
          <small>{{ adminIdentityState.identity?.identitySource }}</small>
          <template v-if="adminIdentityState.reviewEnabled">
            <el-button data-testid="review-expire" size="small" text :loading="adminIdentityState.busy" @click="expireReviewIdentity">模拟身份失效</el-button>
            <el-button data-testid="review-logout" size="small" text :loading="adminIdentityState.busy" @click="logoutReviewIdentity">退出</el-button>
          </template>
        </div>
      </header>
      <el-alert v-if="adminIdentityState.deniedMessage" class="admin-access-alert" data-testid="admin-access-forbidden" type="error" :title="adminIdentityState.deniedMessage" show-icon closable @close="dismissAccessDenied" />
      <router-view />
    </div>
  </div>
</template>
