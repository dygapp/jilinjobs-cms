import type { RouteRecordRaw } from 'vue-router'

export interface AdminNavigationItem {
  to: string
  label: string
  icon: string
  requiredRole?: 'super'
}

export interface AdminNavigationSection {
  label: string
  items: AdminNavigationItem[]
}

export interface AdminModule {
  id: string
  defaultEntry?: boolean
  landingRoute: string
  routes: RouteRecordRaw[]
  compatibilityRoutes?: RouteRecordRaw[]
  navigationSections: AdminNavigationSection[]
}
