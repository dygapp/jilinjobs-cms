# CMS 管理端前端技术计划（Technical Plan）

## 1. 目标

`frontend/admin` 维护通用 CMS 对象，继续使用 Vue 3 + TypeScript + Element Plus 和 `/admin/` base。

## 2. 统一图片资源选择器

新增可复用 `ImageResourcePicker`，作为所有 `/static/**` 图片属性的优先编辑方式。它负责：

- 当前图片预览和路径展示；
- 通过既有 StaticResource multipart API 上传；
- 生成稳定、不依赖用户原文件名的运行时文件名；
- 浏览当前业务上传目录已有图片；
- 在导航图标等场景合并内置候选；
- 清除可选图片值。

Backend 继续负责真实媒体校验。控件不自行删除旧资源。

## 3. 导航管理

页面加载 NavigationLocation 和 NavigationItem。左侧显示位置并采用整行选择 + `...` 菜单；右侧将当前位置的 flat items 转换为 tree table。新增/编辑条目时 position 固定为当前 code，parent 只从当前位置选择。

导航条目新增可选 iconPath。表单通过 ImageResourcePicker 从语义化导航图标目录选择现有站点图标，或上传到 `uploads/navigation-icons/`。列表显示图标预览。

## 4. 列表管理

`/lists` 左侧选择列表定义，右侧维护列表项。列表定义不再编辑 itemType。条目编辑器统一显示 title、subtitle、可选 imagePath、可选 URL、openMode、sortOrder、enabled；图片上传到 `uploads/lists/{code}/`。

是否需要图片、是否显示名称、Logo 如何布局等属于具体 Public Site 页面设计，不由 CMS 表单配置展示模式。

## 5. 宣传展示管理

技术路由保持 `/advertisements`，界面统一使用“宣传展示管理 / 展示位 / 展示内容”。先选择展示位，再维护内容。支持图片、URL、openMode、startAt/endAt、展示顺序和 enabled。图片上传到 `uploads/displays/{slotCode}/`。

## 6. 网站属性

保留兼容 URL `/site-config`，页面名称“网站属性”。支持属性定义新增/编辑/删除和值维护。表单根据 valueType 提供 JSON textarea、URL、BOOLEAN 等输入；RESOURCE_PATH 改用 ImageResourcePicker，上传到 `uploads/site-properties/{key}/`。

当前不实现权限差异，新增/删除按钮不基于用户身份隐藏。

## 7. Admin Shell

导航至少包含文章、栏目、导航、固定页面、列表、宣传展示、网站属性、静态资源。内部稳定 route/testid 可继续沿用 advertisements，用户可见 label 使用“宣传展示”。

## 8. Browser Verification

更新 E2E：

- Shell “宣传展示”入口可达；
- 导航位置切换 + 树形数据 + 图标属性/选择器；
- 通用列表不依赖 itemType，并至少验证列表图片通过统一选择器上传到约定目录；
- 宣传展示至少验证首页招聘展示位、图片选择器和 NO_LINK；
- 网站属性创建自定义 JSON 属性并验证非法 JSON 被前端阻止；
- 已有文章发布、Page render mode、静态资源安全回归继续执行。
