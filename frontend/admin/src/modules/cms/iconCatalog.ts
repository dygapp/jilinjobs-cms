export interface IconCatalogItem { label:string; path:string }

// 现有站点图标的语义目录。保留原始版本化资源路径，避免为重命名制造无意义二进制 churn；
// 新增自定义导航图标统一进入 /static/uploads/navigation-icons/。
export const navigationIconCatalog:IconCatalogItem[] = [
  { label:'就业信息填报', path:'/static/icons/top-nav-01.png' },
  { label:'学历认证（首页快捷入口）', path:'/static/icons/top-nav-02.png' },
  { label:'全国征兵网', path:'/static/icons/top-nav-03.png' },
  { label:'预决算公开', path:'/static/icons/top-nav-04.png' },
  { label:'举报电话及邮箱', path:'/static/icons/top-nav-05.png' },
  { label:'就业派遣', path:'/static/icons/guide-01.png' },
  { label:'档案管理', path:'/static/icons/guide-02.png' },
  { label:'流动党员', path:'/static/icons/guide-03.png' },
  { label:'学历认证（业务指南）', path:'/static/icons/guide-04.png' },
  { label:'联系我们', path:'/static/icons/guide-05.png' },
  { label:'常见问题', path:'/static/icons/guide-06.png' },
]
