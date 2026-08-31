# CMS 管理端前端技术计划（Technical Plan）

## 1. 目标

`frontend/admin` 维护通用 CMS 对象，继续使用 Vue 3 + TypeScript + Element Plus 和 `/admin/` base。

## 2. 统一图片资源选择器

新增可复用 `ImageResourcePicker`，作为所有 `/static/**` 图片属性的优先编辑方式。它负责：

- 当前图片预览和路径展示；
- 通过既有 StaticResource multipart API 上传；
- 生成稳定、不依赖用户原文件名的运行时文件名；
- 浏览 `/static/uploads/**` 共享 Runtime 图片库，允许跨 CMS 模块复用已经上传的图片；
- 在导航图标等场景合并经过语义整理的内置候选；
- 清除可选图片值。

Backend 继续负责真实媒体校验。控件不自行删除旧资源。新文件仍只能上传到当前业务上下文约定目录；“共享图片库”只用于复用已有 Runtime 图片，不改变上传目录治理。

新增 `AdaptiveImagePreview` 作为预览层公共组件。默认普通图片使用白色背景；图标场景显式启用 adaptive 模式。adaptive 模式在浏览器端用小尺寸 Canvas 采样有效透明像素，按亮度分布选择深色、浅色或中性棋盘背景；采样失败或明暗混合时回退棋盘背景。深/浅背景在 hover 时切换到相反对比背景，棋盘背景 hover 时切换深色。该逻辑只影响 Admin DOM/CSS，不写入 CMS 数据、不修改图片文件，也不参与 Public Site 渲染。

## 3. 导航管理

页面加载 NavigationLocation 和 NavigationItem。左侧显示位置并采用整行选择 + `...` 菜单；右侧将当前位置的 flat items 转换为 tree table。新增/编辑条目时 position 固定为当前 code，parent 只从当前位置选择。

正式初始化只保留 `MAIN`、`HOME_SHORTCUT`、`HOME_QUICK` 三个内置导航位置。V8 曾为模型迁移加入的 `SERVICE`、`SITE` 通过后续 Flyway 迁移从升级数据库中清理；如果旧库仍残留这些位置下的兼容导航条目，先解除父子引用并删除条目，再删除位置。网站导航/友情链接继续由通用列表承担。

导航条目新增可选 iconPath。表单通过 ImageResourcePicker 从语义化导航图标目录选择现有站点图标，或上传到 `uploads/navigation-icons/`；同时可按统一规则复用 Runtime 图片。导航表格和图标选择器均启用 AdaptiveImagePreview，保证淡色透明图标在管理端白色 Shell 中仍清晰可见。

## 4. 列表管理

`/lists` 左侧选择列表定义，右侧维护列表项。列表定义不再编辑 itemType。条目编辑器统一显示 title、subtitle、可选 imagePath、可选 URL、openMode、sortOrder、enabled；新图片上传到 `uploads/lists/{code}/`，也可从共享 Runtime 图片库选择已有图片。

是否需要图片、是否显示名称、Logo 如何布局等属于具体 Public Site 页面设计，不由 CMS 表单配置展示模式。普通列表照片/Logo 目前保持常规图片预览；若后续某特定页面的管理体验需要图标型自适应背景，应由调用场景显式开启，而不是通过数据属性控制。

## 5. 宣传展示管理

技术路由保持 `/advertisements`，界面统一使用“宣传展示管理 / 展示位 / 展示内容”。先选择展示位，再维护内容。支持图片、URL、openMode、startAt/endAt、展示顺序和 enabled。新图片上传到 `uploads/displays/{slotCode}/`，也可复用共享 Runtime 图片。

## 6. 网站属性

保留兼容 URL `/site-config`，页面名称“网站属性”。支持属性定义新增/编辑/删除和值维护。表单根据 valueType 提供 JSON textarea、URL、BOOLEAN 等输入；RESOURCE_PATH 改用 ImageResourcePicker，新图片上传到 `uploads/site-properties/{key}/`，也可复用共享 Runtime 图片。

当前不实现权限差异，新增/删除按钮不基于用户身份隐藏。

## 7. Admin Shell

导航至少包含文章、栏目、导航、固定页面、列表、宣传展示、网站属性、静态资源。内部稳定 route/testid 可继续沿用 advertisements，用户可见 label 使用“宣传展示”。

## 8. Browser Verification

E2E 必须覆盖：

- Shell “宣传展示”入口可达；
- 导航位置接口和左侧列表均不再出现 `SERVICE`、`SITE`，并保留 `MAIN`、`HOME_SHORTCUT`、`HOME_QUICK`；
- 导航位置切换 + 树形数据 + 图标属性/选择器；
- 已知淡色透明导航图标在表格、当前值预览和内置图标库中均解析为高对比深色背景，并保留 hover 对比切换提示；
- 通用列表不依赖 itemType，并至少验证列表图片通过统一选择器上传到约定目录；
- 统一图片选择器能够跨 CMS 模块从 `/static/uploads/**` 复用 Runtime 已上传图片；
- 宣传展示至少验证首页招聘展示位、图片选择器和 NO_LINK；
- 网站属性创建自定义 JSON 属性并验证非法 JSON 被前端阻止；
- 已有文章发布、Page render mode、静态资源安全回归继续执行。
