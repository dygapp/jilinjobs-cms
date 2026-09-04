# 公开站规格说明（Specification）

## 1. 目标

本文定义吉林省高等学校毕业生就业信息网公开站及中心党建 Theme / Router Boundary 的总体 WHAT / WHY。共享内容模型以 `docs/specifications/cms-core.md` 为准；中心党建正式页面与内容规格以 `docs/specifications/party.md` 为准；Main / Party 公共导航与 Footer 以 `docs/specifications/public-shared-shell.md` 和 ADR-0003 为准；轮播的数据投放与行为边界以 `docs/technical/carousel-list-placement.md` 为实现解释基线。

中心主站目标仍为现网视觉与布局复刻，并通过 CMS 数据驱动需要持续运营维护的内容；稳定布局、固定集成和无需运营维护的内容保留为工程资产。

中心党建 Party Entry / Router / Theme Foundation 已完成，当前正式进入真实栏目、页面、内容与视觉收敛。Foundation 阶段的占位文案、临时品牌元素和基础 CSS 不构成最终内容/视觉 Authority。

公开前端目标架构为 **Multi-entry Modular SPA**：同一 `frontend/public-site` Vue / Vite 工程内，中心主站与中心党建按真实 Theme / Router Boundary 分别拥有 Entry、App、Router、Banner、内容 Frame 与主题页面样式；当前共用构建部署链路与 Spring Boot CMS Backend，并共享主导航、Footer 以及已经证明稳定复用的无主题轮播生命周期，不引入 Module Federation，也不拆成独立前端工程。

## 2. URL 与页面

### 2.1 中心主站

- 首页：`/`
- 栏目：`/column/{alias}`
- 站内文章：`/article/{id}`
- 独立单页：`/page/{alias}`
- 单页分组成员：`/page/{groupAlias}/{alias}`

上述 URL 全部由 Main Site Entry 承载；`/page/**` 只是业务页面路径，不再对应独立重复 Vue App / HTML Entry。

外链文章从列表直接打开原文；兼容文章地址重定向原文。面包屑来自业务对象关系，不从 URL 或导航入口机械推导。

### 2.2 中心党建

- 入口页：`/party/`
- 栏目：`/party/column/{alias}`
- 站内文章：`/party/article/{id}`
- canonical namespace：`/party/**`

党建栏目与文章的允许作用域、原站内容线、旧 `plist/pdetail/detail` 地址迁移边界以 `docs/specifications/party.md` 为准。

主站 `MAIN` Navigation 中“中心党建”预置条目指向 `/party/`，当前窗口进入。业务定位上中心党建属于主站特殊栏目/专题页面，`/party/**` 独立 Entry 只承担红色主题、路由与内容模板隔离。

## 3. 公开前端边界

### 3.1 Main Site

Main Site 持有：

- 主站 App / Router；
- 主站顶部平台条与 Banner；
- 蓝白内容主题、页面 Frame 与主站页面样式；
- 首页、内容（栏目/文章）、单页、固定集成等页面模块。

Main Site 的页面路由使用 Vue Router 动态 `import()` 进行 route-level lazy loading，避免继续以同步 import 将所有页面代码绑定到首屏 bundle。

### 3.2 Party Site

Party Site 持有：

- 独立 App / Router；
- 中心党建 Banner；
- 独立红色内容主题与页面 Frame；
- `PartyHome` 入口页模块；
- 党建栏目列表与文章详情模块。

Party 不复制 Main Header/Footer/Navigation DOM，也不依赖 Main 私有 CSS。Main / Party 都通过 Shared Shell Components 使用同一 Navigation 与 Footer 结构和交互，只通过 theme variables / modifier class 切换蓝色与红色。

### 3.3 Shared Boundary

`shared/` 承担已经证明跨两个公开 Entry 稳定复用的公共能力：

- API transport / CMS DTO；
- 静态资源 URL 处理；
- SEO / metadata utility；
- 无主题通用工具；
- `PublicNavigation.vue` 与其菜单树、active、Desktop/Mobile 交互；
- `PublicFooter.vue` 与机构信息、备案、官方标识及响应式结构；
- Navigation / Footer 的公共结构样式与主题变量；
- Main / Party 轮播共同的状态、计时器、暂停/恢复、页面可见性、失败图片剔除、最大有效项数量和 reduced-motion 生命周期。

轮播 Shared Boundary **不**包含 Main / Party 的 DOM、标题层、指示器视觉、尺寸比例和主题动画样式。视觉表达仍由各 Site 自己持有。

以下内容仍不得为了“去重复”强行进入 Shared：

- Main 顶部平台条与 Main Banner；
- Party Banner；
- Page Frame；
- 首页/专题内容区块布局；
- Site-specific 内容主题与页面模板。

存在两个 Entry 不等于构建通用多站点平台，也不要求把中心党建拆成独立 Repository / Frontend Project。

## 4. 主站首页数据来源

首页固定模板继续复刻现网主要布局。运营数据来源统一为：

- 主导航：`MAIN` Navigation；
- 首屏右侧五个蓝色入口：`HOME_SHORTCUT` Navigation，图标来自导航条目 `iconPath`；
- 业务指南快速入口：`HOME_QUICK` Navigation，图标来自导航条目 `iconPath`；
- 轮播内容：`HOME_CAROUSEL` CmsList；
- Main / Party 共用轮播自动切换间隔：`CAROUSEL_INTERVAL_SECONDS` SiteProperty；
- 单个轮播区域前台最大有效项数量：`CAROUSEL_MAX_ITEMS` SiteProperty；
- 通知公告 / 就业动态 / 招聘公告：Column + Article；
- 招聘活动横幅：`HOME_RECRUITMENT_PROMO` 展示位（技术实现仍为 AdvertisementSlot）；
- 网站导航 / 友情链接：`SITE_LINKS` 分组 CmsList；
- 网站名称、电话、备案、版权等：SiteProperty；
- NCSS 区域：固定工程集成，不提供 CMS 管理。

公开站不得再同时读取旧 JSON 配置与新业务对象并合并结果。

## 5. 数据与展示职责

CMS 提供内容数据属性，公开站页面设计决定如何展示。Column `coverPolicy`、CmsList `imagePolicy` 都是数据契约，不向公开站下发展示模式配置。

### 5.1 CmsListItem 投放来源

CmsListItem 支持两种稳定的数据来源：

- `LINK`：列表项自身维护标题、可选 URL，以及由列表图片策略允许的图片；
- `ARTICLE`：列表项通过 `articleId` 引用既有 Article，属于“展示投放”关系，不改变 Article 的唯一 `columnId`、栏目列表归属、详情面包屑或发布生命周期。

ARTICLE 项公开时只在关联文章处于 `PUBLISHED` 状态时有效。文章被撤回后，对应投放自动退出公开列表；重新发布后按现有投放关系恢复，无需复制文章。

ARTICLE 项标题和目标地址以关联文章当前数据为准。站内文章由消费 Site 生成自己的 canonical 详情路由：Main 使用 `/article/{id}`，Party 使用 `/party/article/{id}`；外链文章仍使用文章自身外部 URL。

### 5.2 图片契约

`imagePolicy=NONE` 时列表投放不使用图片；`OPTIONAL / REQUIRED` 时：

- LINK 项使用列表项自身图片；
- ARTICLE 项优先允许继承文章主题图片，也允许从正文图片中选择，或上传独立的列表展示覆盖图；
- 列表专用覆盖图以 CMS Resource ID 关联，不反向修改 Article 的主题图片；
- `REQUIRED` 必须在公开投放时形成有效图片，不能通过空字段绕过。

首页 `HOME_CAROUSEL` 和中心党建 `PARTY_CAROUSEL` 基线均为 `imagePolicy=REQUIRED`。

### 5.3 轮播行为

Main / Party 轮播共同遵循以下基础行为：

- 0 个有效项：稳定空态；
- 1 个有效项：静态展示，不创建自动切换 timer；
- 2 个及以上有效项：按列表顺序循环自动切换，并提供可聚焦的手动页码控制；
- 自动切换间隔读取 `CAROUSEL_INTERVAL_SECONDS`，默认 4 秒；配置缺失、非法或不大于 0 时公开端 fallback 为 4 秒；
- 每个轮播区域最多消费 `CAROUSEL_MAX_ITEMS` 个有效项，默认 5；Backend 允许维护更多记录；
- hover、轮播内部 focus、浏览器页面隐藏都会暂停；恢复后继续当前项，不重置为第一项；
- `prefers-reduced-motion: reduce` 时关闭自动切换，并去除/关闭轮播切换动画，但保留手动页码切换；
- 当前 EU-30 不实现触摸 swipe；移动端仍可使用页码控制；
- 图片加载失败的项退出本次有效轮播集合并自动补位；全部图片失败时进入稳定空态；
- 不引入第三方 Carousel 依赖。

Main 轮播使用稳定 `8:5` 比例并保持 `object-fit: cover`；Party 桌面保持约 `585×329`，响应式阶段保持 `585:329` 比例。两站不要求使用相同视觉比例。

当前 `SITE_LINKS` 相关列表基线 `imagePolicy=NONE`，页面按 title + URL 输出文字链接。未来若视觉设计确认需要 Logo，应先把对应列表数据策略调整为 `OPTIONAL / REQUIRED` 并补充数据，再由公开页面设计消费图片；不得通过新增 `displayMode` 控制布局。

Public Article Summary 可以包含可选 `coverResourceId`，为带图模板提供可消费数据；具体页面是否展示封面仍由页面基线决定。栏目 `coverPolicy` 不直接决定 DOM 布局。

首页快捷入口和快速导航直接使用 Navigation `iconPath`；不得使用数组下标拼接 `top-nav-01`、`guide-01` 等路径。

中心党建正式内容复用现有 Column + Article；INTERNAL / EXTERNAL_LINK 行为和 Party 作用域由 `docs/specifications/party.md` 约束。独立视觉主题本身不构成新增党建专属 CMS 模型或 Admin Module 的理由。

## 6. 工程资产

以下内容允许固定在公开站工程或版本化静态资产中：

- 主站首页区域布局、尺寸关系和视觉样式；
- Main 顶部平台条与 Banner、Party Banner；
- 各 Entry 自己的 Page Frame / 内容 Theme；
- Shared Navigation / Footer 的稳定结构和主题变量；
- NCSS Logo、学生入口、企业入口及其固定集成布局；
- 明确无需运营维护的装饰图标与一次性外部平台接缝。

站点导航业务图标虽然文件可以来自版本化 `/static/icons/**`，但“某条导航使用哪个图标”属于 Navigation 数据，不属于前端数组顺序规则。

当工程资产未来产生真实运营维护需求时，再通过 Requirement Change 判断是否升级为 CMS 对象。

## 7. 页面基线

主站栏目列表、文章详情、独立单页、业务指南单页分组继续以现网页面主要版式为复刻基准。单页分组 Tab 从 PageGroup 成员数据生成。

中心党建 Foundation 已结束；正式入口页、栏目列表、文章详情、红色视觉和真实静态资源依据 `docs/specifications/party.md` 与原站证据收敛，不再以 Foundation 页面作为验收基线。

招聘信息和直播课程继续保留本站页面框架/占位，不加载真实第三方 iframe。

## 8. Acceptance Criteria

- Main Site 的 `/`、`/column/**`、`/article/**`、`/page/**` 由同一 Main Site Entry 承载，既有 canonical URL 与视觉主基线无回归；
- Main Site 源码保持 `app / shell / modules` 所有权边界和 route-level lazy loading；
- `/party/**` 由独立 Party HTML Entry、Vue App、Router 与红色内容主题承载，直接访问与刷新正常；
- PartyHome 入口页、栏目、文章正式行为符合 `docs/specifications/party.md`；
- Main / Party Navigation 与 Footer 复用同一 Shared Components，仅主题颜色不同；
- Main / Party 轮播复用同一无主题生命周期逻辑，但保留独立 DOM、尺寸和主题样式；
- 主站与党建内容主题 CSS 互不污染，Party Banner 与内容 Frame 不依赖 Main 私有 DOM/CSS；
- `MAIN` Navigation 中“中心党建”指向 `/party/`；
- 当前公开站仍为一个 `frontend/public-site` 工程、一个 build/deploy 单元；不引入 Module Federation 或独立党建前端工程；
- 首页不再读取 `SERVICE_LINKS`、`HOME_BANNERS`、`SITE_LINK_GROUPS`、`HOME_PROMO_BANNER_PATH`、`HOME_NCSS_LOGO_PATH` 作为运行时内容源；
- 首页五个蓝色入口来自 `HOME_SHORTCUT`，图标来自导航条目而非排序下标；
- 业务指南快捷入口来自 `HOME_QUICK`，图标来自导航条目；
- `HOME_CAROUSEL` 与 `PARTY_CAROUSEL` 使用图片必填数据契约；
- CmsListItem 可使用 LINK 或 ARTICLE，ARTICLE 投放不改变文章单一栏目归属；
- ARTICLE 只在关联文章已发布时进入公开列表，撤回文章会同步退出公开轮播；
- Main / Party 自动切换间隔统一来自正整数 SiteProperty `CAROUSEL_INTERVAL_SECONDS`，默认 4 秒；不存在 `HOME_CAROUSEL_INTERVAL_SECONDS` 运行时依赖；
- `CAROUSEL_MAX_ITEMS` 控制单个轮播前台最大有效项数量，默认 5；
- 多项轮播支持手动页码、hover/focus/页面隐藏暂停且恢复不重置当前项；
- reduced-motion 下关闭自动播放和切换动画但保留手动控制；
- 失败图片从有效项中剔除并补位；单项静态、零项稳定空态；
- 当前 EU-30 不引入第三方 Carousel，也不实现 swipe；
- LINK 项 URL 可选，没有 URL 时不产生伪链接；
- ARTICLE 内链按 Main / Party 各自 canonical 路由生成；
- `SITE_LINKS` 当前以不使用图片的文字链接基线输出，未来 Logo 需求不引入 displayMode；
- Public Article Summary 能提供可选封面引用，但页面是否展示由模板设计决定；
- 招聘活动横幅来自宣传展示位；
- 联系电话等站点属性来自 SiteProperty；
- NCSS 固定集成正常。