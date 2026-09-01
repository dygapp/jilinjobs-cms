import { createRouter, createWebHistory } from 'vue-router'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'public-home', component: () => import('../modules/home/PublicHomeView.vue') },
    { path: '/column/:alias', name: 'public-column-alias', component: () => import('../modules/content/PublicColumnView.vue') },
    { path: '/columns/:id', name: 'public-column-legacy', component: () => import('../modules/content/PublicColumnView.vue') },
    { path: '/article/:id', name: 'public-article', component: () => import('../modules/content/PublicArticleView.vue') },
    { path: '/articles/:id', name: 'public-article-legacy', redirect: to => ({ name: 'public-article', params: { id: to.params.id } }) },
    { path: '/page/:group/:alias', name: 'public-group-page', component: () => import('../modules/page/PublicPageView.vue') },
    { path: '/page/:alias', name: 'public-page', component: () => import('../modules/page/PublicPageView.vue') },
  ],
})
