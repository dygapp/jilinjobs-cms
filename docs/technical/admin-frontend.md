# CMS 管理端前端技术计划（Technical Plan）

## 1. 目标

`frontend/admin` 维护通用 CMS 对象，继续使用 Vue 3 + TypeScript + Element Plus 和 `/admin/` base。

## 2. 统一图片资源选择与预览

`ImageResourcePicker` 作为 `/static/**` 图片属性的优先编辑方式，负责当前图片预览、既有 StaticResource multipart 上传、稳定 Runtime 文件名、浏览 `/static/uploads/**` 共享图片库、导航语义候选和清除可选值。

Backend 继续负责真实媒体校验。控件不自行删除旧资源。新文件仍只能上传到当前业务上下文约定目录；共享图片库只用于复用已有 Runtime 图片。

`AdaptiveImagePreview` 是 Admin 的统一图片缩略图基础组件。凡后台界面需要让运营人员辨识图片内容，应优先复用该组件，不再在各页面直接创建普通 `<img>` 缩略图。组件默认启用透明/浅色图片的可辨识背景：根据图像亮度和透明度选择浅色、深色或棋盘格背景，并允许 Hover 切换对比背景。该逻辑只影响 Admin DOM/CSS，不写入 CMS 数据、不修改图片文件，也不参与 Public Site 渲染。

放大查看不自研 Dialog / Viewer。`AdaptiveImagePreview` 内部复用 Element Plus `el-image` 的 `preview-src-list` / Viewer 能力，缩放、旋转、关闭等通用交互由 Element Plus 负责。图片选择库中的候选卡片点击职责是“选择图片”，因此候选缩略图可以关闭 Viewer；当前已选图片、列表缩略图等普通浏览场景默认允许点击查看原图。

当前统一接入范围包括导航图标、网站属性 RESOURCE_PATH、通用列表图片、宣传展示图片、静态资源图片，以及后续新增的同类 Admin 图片内容。新增页面若需要图片缩略图，不应复制新的背景判断或大图 Dialog。

## 3. 内容管理信息架构

Admin Shell 按业务职责分为四个无额外点击层级的导航分组：

- 内容管理：文章、单页、列表；
- 内容结构：栏目、导航；
- 运营展示：宣传展示；
- 站点设置：网站属性、静态资源。

技术路由继续保持 `/articles`、`/pages`、`/lists`、`/columns`、`/navigation`、`/advertisements`、`/site-config`、`/static-resources`。不新增独立“系统设置”路由或菜单。

对于“容器 → 成员”模型，页面优先采用左侧容器导航 + 右侧成员列表；栏目管理本身仍直接维护栏目树，不重复放置栏目导航树。

### 3.1 可折叠导航与紧凑操作

`App.vue` 以本地 `ref<boolean>` 控制主侧边栏展开/收起，并通过 `.sidebar-collapsed` 切换 220px 与紧凑宽度；收起时仍保留各入口图标和 title，顶部始终保留重新展开按钮。该状态当前不写入 Database、SiteProperty、localStorage 或用户 Profile。

文章、单页、列表、导航、宣传展示、网站属性等 Master–Detail 页面以本地 `sideCollapsed` 控制左侧组织面板。公共 CSS 使用 `.side-panel-collapsed` 将两列布局切换为单列；`AdminPanelToggle` 始终位于右侧上下文/Header 中，因此左侧隐藏后仍可恢复。

常规 Table 行操作复用 `AdminIconAction`：Element Plus icon + `el-tooltip` + `aria-label`，固定操作列宽度按动作数量压缩。复杂容器操作继续使用 `MoreFilled` 下拉菜单。`AdminIconAction` 的可访问名称继续支持 Browser E2E 使用 role/name 定位，避免视觉收敛破坏已有验证契约。

本阶段不增加“紧凑/文字操作/图标操作”等显示风格系统配置，也不建立用户个人 UI 偏好持久化。待未来真实用户/账号能力建立且出现稳定个性化需求后再评估。

## 4. 文章与栏目

`/articles` 同时加载 Article 和 Column。左侧把 flat columns 转换为层级树，父栏目筛选在前端收集全部后代 id；右侧文章列表继续叠加关键词、状态、文章类型筛选。TreeSelect 用同一栏目树数据生成。

Column API Type 增加 `coverPolicy: 'NONE' | 'OPTIONAL' | 'REQUIRED'`。栏目管理整页树表的新增/编辑 Dialog 提供“文章封面”策略 Select，并随 create/update 请求提交。

文章表单根据当前 `form.columnId` 计算 `formCoverPolicy`：

- NONE：不渲染封面上传控件；保存 draft 时强制发送 `coverResourceId=null`；
- OPTIONAL：正常显示可选封面；
- REQUIRED：显示封面字段和“草稿可暂存，发布前必须设置封面”说明。

前端提示不是最终约束。发布和已发布文章编辑的契约由 Backend 保证。外链文章继续不提交本地封面。

## 5. 单页管理

`/pages` 同时加载 PageGroup 和 Page。左侧组织区域包含全部单页、独立单页和所有 PageGroup；右侧 Table 根据左侧上下文过滤成员。选择具体分组后新增 Page 时默认 current group id，其他上下文默认为 null；表单仍可调整。

PageGroup 当前为平级对象，左侧使用普通分组列表而不是树。技术 API/数据库继续使用 Page/PageGroup。

## 6. 导航管理

页面加载 NavigationLocation 和 NavigationItem。左侧显示位置并采用整行选择 + `...` 菜单；右侧将当前位置 flat items 转换为 tree table。新增/编辑条目时 position 固定为当前 code，parent 只从当前位置选择。

正式初始化只保留 `MAIN`、`HOME_SHORTCUT`、`HOME_QUICK` 三个内置导航位置。V8 的 SERVICE/SITE 已由后续 migration 清理。

导航条目 iconPath 通过 ImageResourcePicker 编辑；导航表格和图标选择器均复用统一 AdaptiveImagePreview，并允许在普通浏览场景通过 Element Plus Viewer 查看原图。

## 7. 列表管理

`/lists` 左侧选择列表定义，右侧维护列表项。列表定义不再编辑 itemType，新增 `imagePolicy` 选择：

- NONE：列表项 Dialog 不显示 ImageResourcePicker，并在提交前清空 imagePath；
- OPTIONAL：显示可选图片；
- REQUIRED：图片字段标记必填，前端在没有 imagePath 时先给出提示。

Backend 仍是最终校验层，因此直接 API 调用无法绕过 NONE/REQUIRED 规则。列表策略变更时的既有数据冲突由 Backend 拒绝，前端展示服务端错误即可。

图片策略不控制页面显示模式。列表中存在 imagePath 时，Admin Table 使用统一 AdaptiveImagePreview + 截断路径显示；是否在 Public Site 显示名称、Logo 如何布局等仍属于 Public Site 设计。

`HOME_CAROUSEL` 基线应在 UI 显示“图片必填”；当前 SITE_LINKS 列表显示“不使用图片”。

## 8. 宣传展示管理

技术路由保持 `/advertisements`，界面统一使用“宣传展示管理 / 展示位 / 展示内容”。先选择展示位，再维护内容。支持图片、URL、openMode、startAt/endAt、展示顺序和 enabled。新图片上传到 `uploads/displays/{slotCode}/`，也可复用共享 Runtime 图片。

展示内容 Table 的图片列使用统一 AdaptiveImagePreview + 路径摘要，编辑 Dialog 继续使用 ImageResourcePicker。宣传图片即使通常为照片，也统一获得透明/浅色内容的可辨识背景和 Element Plus 原图 Viewer。

## 9. 网站属性

保留兼容 URL `/site-config`，页面名称“网站属性”。页面并行请求：

- `/api/admin/site-config` 获取属性；
- `/api/admin/site-config/groups` 获取 Spring CMS metadata 定义的分组。

左侧复用现有 Master–Detail 样式显示“全部属性 + metadata groups”，右侧只显示当前分组属性。新增属性默认使用当前选中分组；定义 Dialog 的 groupCode 改为受控 Select，不允许任意输入未知分组。

右侧 Table 不再直接渲染可编辑 `el-input`、`el-switch` 或 `ImageResourcePicker`。值列只渲染紧凑只读摘要：RESOURCE_PATH 使用统一 AdaptiveImagePreview 缩略图和截断路径，布尔值显示状态 Tag，其余值使用单行截断文本。点击“编辑值”图标后使用独立 Dialog，根据 `valueType` 渲染类型化编辑控件；保存仍调用既有 `PUT /api/admin/site-config/{key}`。

属性定义 Dialog 与值编辑分离：新建定义时仍可录入初始值；编辑已有定义时不重复提供日常值编辑控件，并提示从列表“编辑值”进入。定义更新继续携带当前值以保持既有 API contract，类型变化仍先验证现有值是否满足新类型。

valueType 支持 `TEXT / INTEGER / RESOURCE_PATH / JSON / URL / BOOLEAN`。INTEGER 使用 number 输入并在保存前用整数正则校验；Backend 再做最终类型校验。`HOME_CAROUSEL_INTERVAL_SECONDS` 在 PRESENTATION 分组显示为整数属性，正常值为正整数秒。

RESOURCE_PATH 的值编辑 Dialog 使用 ImageResourcePicker，新图片上传到 `uploads/site-properties/{key}/`，并显式使用统一自适应预览。当前不实现权限差异。

## 10. 静态资源管理

静态资源列表使用 Backend 返回的 `protectedResource` 判断是否允许普通删除。界面术语统一使用“受保护资源”，Tooltip 说明“当前由站点基线或 CMS 数据引用保护，不能直接删除”；不再使用容易被理解为人工重要性等级的“关键资源”。

图片类型静态资源在列表中提供统一 AdaptiveImagePreview。普通点击使用 Element Plus Viewer 页内查看；“打开原文件”动作继续保留，用于浏览器直接访问或下载原始资源。非图片文件不强行生成预览。

## 11. Browser Verification

E2E 必须覆盖：

- Shell 四个导航分组及八类入口可达，没有独立“系统设置”；
- 主侧边栏可以收起/展开且收起后入口仍存在；
- 至少覆盖文章栏目导航和网站属性分组面板的收起/恢复，证明局部面板隐藏后右侧仍保留恢复入口；
- 紧凑图标操作保持 `aria-label`，Hover 可展示 Tooltip，既有 role/name 操作定位继续有效；
- 单页管理组织导航和上下文新增；
- 文章栏目树父子聚合和层级 TreeSelect；
- Column REQUIRED 策略可维护；无封面草稿能创建但发布被 Backend 阻止；Article Dialog 显示 REQUIRED 提示；
- 正式导航位置和导航图标属性/自适应预览；
- RESOURCE_PATH、列表图片、宣传展示图片和静态资源图片均复用 AdaptiveImagePreview；至少一个普通缩略图点击后出现 Element Plus `.el-image-viewer__wrapper`；
- 静态资源“受保护”标签与 Backend `protectedResource=true` 一致；
- HOME_CAROUSEL imagePolicy=REQUIRED、SITE_LINKS 当前 NONE；NONE UI 隐藏图片，Backend 直接 API 对 NONE/REQUIRED 都执行约束；
- SiteProperty group endpoint 返回 metadata 定义顺序；未知 group 直接 API 被拒绝；Admin 左侧 PRESENTATION 分组及受控 group Select 正常；
- SiteProperty 整数和 JSON 值通过独立值编辑 Dialog 修改，非法值仍被前端校验拒绝；
- INTEGER 非整数值前端拒绝，Backend 单元测试覆盖最终整数与正整数间隔约束；
- 公开首页通过真实 SiteProperty=1 秒 + 临时第二轮播项验证 active carousel id 实际发生切换，再清理 fixture 并恢复属性值；
- 统一图片选择器跨模块复用、宣传展示、文章发布、单页 render mode、静态资源安全等既有回归继续执行。
