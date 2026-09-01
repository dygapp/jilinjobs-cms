import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { adminModuleRoutes } from './moduleRegistry'

const legacyCmsRedirects: RouteRecordRaw[] = [
  { path: '/articles', redirect: '/cms/articles' },
  { path: '/columns', redirect: '/cms/columns' },
  { path: '/navigation', redirect: '/cms/navigation' },
  { path: '/pages', redirect: '/cms/pages' },
  { path: '/lists', redirect: '/cms/lists' },
  { path: '/advertisements', redirect: '/cms/advertisements' },
  { path: '/site-config', redirect: '/cms/site-config' },
  { path: '/static-resources', redirect: '/cms/static-resources' },
]

export default createRouter({
  history: createWebHistory('/admin/'),
  routes: [
    { path: '/', redirect: '/cms/articles' },
    ...adminModuleRoutes,
    ...legacyCmsRedirects,
    { path: '/:pathMatch(.*)*', redirect: '/cms/articles' },
  ],
})
