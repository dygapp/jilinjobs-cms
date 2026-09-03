import { createRouter, createWebHistory } from 'vue-router'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/party/', name: 'party-home', component: () => import('../modules/entry/PartyHomeView.vue') },
    { path: '/party/column/:alias', name: 'party-column', component: () => import('../modules/content/PartyColumnView.vue') },
    { path: '/party/article/:id', name: 'party-article', component: () => import('../modules/content/PartyArticleView.vue') },
    { path: '/party/:pathMatch(.*)*', redirect: '/party/' },
  ],
})
