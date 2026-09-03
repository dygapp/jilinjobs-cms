import { createRouter, createWebHistory } from 'vue-router'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/party/', name: 'party-building-entry', component: () => import('../modules/entry/PartyBuildingView.vue') },
    { path: '/party/column/:alias', name: 'party-building-column', component: () => import('../modules/content/PartyBuildingColumnView.vue') },
    { path: '/party/article/:id', name: 'party-building-article', component: () => import('../modules/content/PartyBuildingArticleView.vue') },
    { path: '/party/:pathMatch(.*)*', redirect: '/party/' },
  ],
})
