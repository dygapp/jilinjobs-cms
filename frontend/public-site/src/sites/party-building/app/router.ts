import { createRouter, createWebHistory } from 'vue-router'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/party/', name: 'party-building-home', component: () => import('../modules/home/PartyBuildingHomeView.vue') },
    { path: '/party/:pathMatch(.*)*', redirect: '/party/' },
  ],
})
