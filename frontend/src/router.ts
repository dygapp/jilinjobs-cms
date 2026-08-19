import { createRouter, createWebHistory } from 'vue-router'
import ColumnManagementView from './views/admin/ColumnManagementView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/admin/columns',
    },
    {
      path: '/admin/columns',
      name: 'admin-columns',
      component: ColumnManagementView,
    },
  ],
})

export default router
