import type { AdminModule } from '../../app/adminModule'

export const cmsAdminModule: AdminModule = {
  id: 'cms',
  navigationSections: [
    {
      label: '内容管理',
      items: [
        { to: '/cms/articles', label: '文章管理', icon: '文' },
        { to: '/cms/pages', label: '单页管理', icon: '页' },
        { to: '/cms/lists', label: '列表管理', icon: '列' },
      ],
    },
    {
      label: '内容结构',
      items: [
        { to: '/cms/columns', label: '栏目管理', icon: '栏' },
        { to: '/cms/navigation', label: '导航管理', icon: '导' },
      ],
    },
    {
      label: '运营展示',
      items: [
        { to: '/cms/advertisements', label: '宣传展示', icon: '展' },
      ],
    },
    {
      label: '站点设置',
      items: [
        { to: '/cms/site-config', label: '网站属性', icon: '属' },
        { to: '/cms/static-resources', label: '静态资源', icon: '资' },
      ],
    },
  ],
  routes: [
    { path: '/cms/articles', name: 'admin-cms-articles', component: () => import('./views/admin/ArticleManagementView.vue') },
    { path: '/cms/columns', name: 'admin-cms-columns', component: () => import('./views/admin/ColumnManagementView.vue') },
    { path: '/cms/navigation', name: 'admin-cms-navigation', component: () => import('./views/admin/NavigationManagementView.vue') },
    { path: '/cms/pages', name: 'admin-cms-pages', component: () => import('./views/admin/PageManagementView.vue') },
    { path: '/cms/lists', name: 'admin-cms-lists', component: () => import('./views/admin/ListManagementView.vue') },
    { path: '/cms/advertisements', name: 'admin-cms-advertisements', component: () => import('./views/admin/AdvertisementManagementView.vue') },
    { path: '/cms/site-config', name: 'admin-cms-site-config', component: () => import('./views/admin/SiteConfigManagementView.vue') },
    { path: '/cms/static-resources', name: 'admin-cms-static-resources', component: () => import('./views/admin/StaticResourceManagementView.vue') },
  ],
}
