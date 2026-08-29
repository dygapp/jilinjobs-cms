import { createRouter, createWebHistory } from 'vue-router'
import AdvertisementManagementView from './views/admin/AdvertisementManagementView.vue'
import ArticleManagementView from './views/admin/ArticleManagementView.vue'
import ColumnManagementView from './views/admin/ColumnManagementView.vue'
import ListManagementView from './views/admin/ListManagementView.vue'
import NavigationManagementView from './views/admin/NavigationManagementView.vue'
import PageManagementView from './views/admin/PageManagementView.vue'
import SiteConfigManagementView from './views/admin/SiteConfigManagementView.vue'
import StaticResourceManagementView from './views/admin/StaticResourceManagementView.vue'

export default createRouter({
  history: createWebHistory('/admin/'),
  routes: [
    { path: '/', redirect: '/articles' },
    { path: '/articles', name: 'admin-articles', component: ArticleManagementView },
    { path: '/columns', name: 'admin-columns', component: ColumnManagementView },
    { path: '/navigation', name: 'admin-navigation', component: NavigationManagementView },
    { path: '/pages', name: 'admin-pages', component: PageManagementView },
    { path: '/lists', name: 'admin-lists', component: ListManagementView },
    { path: '/advertisements', name: 'admin-advertisements', component: AdvertisementManagementView },
    { path: '/site-config', name: 'admin-site-config', component: SiteConfigManagementView },
    { path: '/static-resources', name: 'admin-static-resources', component: StaticResourceManagementView },
    { path: '/:pathMatch(.*)*', redirect: '/articles' },
  ],
})
