import { createRouter, createWebHistory } from 'vue-router'
import { adminDefaultRoute, adminModuleRoutes } from './moduleRegistry'

export default createRouter({
  history: createWebHistory('/admin/'),
  routes: [
    { path: '/', redirect: adminDefaultRoute },
    ...adminModuleRoutes,
    { path: '/:pathMatch(.*)*', redirect: adminDefaultRoute },
  ],
})
