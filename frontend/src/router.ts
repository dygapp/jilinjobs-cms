import { createRouter, createWebHistory } from 'vue-router'
import ColumnManagementView from './views/admin/ColumnManagementView.vue'
import NavigationManagementView from './views/admin/NavigationManagementView.vue'
import PublicColumnView from './views/public/PublicColumnView.vue'
import PublicHomeView from './views/public/PublicHomeView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'public-home',
      component: PublicHomeView,
    },
    {
      path: '/columns/:id',
      name: 'public-column',
      component: PublicColumnView,
    },
    {
      path: '/admin/columns',
      name: 'admin-columns',
      component: ColumnManagementView,
    },
    {
      path: '/admin/navigation',
      name: 'admin-navigation',
      component: NavigationManagementView,
    },
  ],
})

export default router
