import { createRouter, createWebHistory } from 'vue-router'
import PublicArticleView from './views/public/PublicArticleView.vue'
import PublicColumnView from './views/public/PublicColumnView.vue'
import PublicHomeView from './views/public/PublicHomeView.vue'
import PublicPageView from './views/public/PublicPageView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'public-home', component: PublicHomeView },
    { path: '/column/:alias', name: 'public-column-alias', component: PublicColumnView },
    { path: '/columns/:id', name: 'public-column-legacy', component: PublicColumnView },
    { path: '/article/:id', name: 'public-article', component: PublicArticleView },
    { path: '/articles/:id', name: 'public-article-legacy', redirect: to => ({ name: 'public-article', params: { id: to.params.id } }) },
    { path: '/page/:group/:alias', name: 'public-group-page', component: PublicPageView },
    { path: '/page/:alias', name: 'public-page', component: PublicPageView },
  ],
})
