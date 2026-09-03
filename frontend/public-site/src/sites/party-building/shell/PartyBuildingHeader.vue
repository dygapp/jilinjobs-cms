<template>
  <header class="party-header" data-testid="party-building-header">
    <div class="party-topbar">
      <div class="party-width party-topbar-inner">
        <span>吉林省高等学校毕业生就业指导中心</span>
        <a href="/">返回就业信息网</a>
      </div>
    </div>
    <div class="party-brand party-width">
      <router-link class="party-brand-link" to="/party/" aria-label="中心党建首页">
        <span class="party-emblem" aria-hidden="true">党</span>
        <span class="party-brand-copy">
          <strong>中心党建</strong>
          <small>党员之家</small>
        </span>
      </router-link>
    </div>
    <nav class="party-navigation" aria-label="中心党建导航">
      <div class="party-width">
        <router-link to="/party/">党建首页</router-link>
        <router-link to="/party/column/party-voice">高层声音</router-link>
        <router-link to="/party/column/party-work">工作动态</router-link>
        <router-link to="/party/column/party-rules">党规党章</router-link>
        <router-link to="/party/column/party-study">理论学习</router-link>
      </div>
    </nav>
  </header>
</template>
