# 公开站规格说明（Specification）

## 1. 目标

本文定义吉林省高等学校毕业生就业信息网公开站及中心党建 Site Boundary 的总体 WHAT / WHY。共享内容模型以 `docs/specifications/cms-core.md` 为准；中心党建正式页面与内容规格以 `docs/specifications/party.md` 为准。

中心主站目标仍为现网视觉与布局复刻，并通过 CMS 数据驱动需要持续运营维护的内容；稳定布局、固定集成和无需运营维护的内容保留为工程资产。

中心党建独立 Entry / Router / Shell Foundation 已完成，当前正式进入真实栏目、页面、内容与视觉收敛。Foundation 阶段的占位文案、临时品牌元素和基础 CSS 不构成最终内容/视觉 Authority。

公开前端目标架构为 **Multi-entry Modular SPA**：同一 `frontend/public-site` Vue / Vite 工程内，中心主站与中心党建按真实 Site / Theme Boundary 分别拥有 Entry、App、Router、Shell 和主题样式；当前共用构建部署链路与 Spring Boot CMS Backend，不引入 Module Federation，也不拆成独立前端工程。

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

- 首页：`/party/`
- 栏目：`/party/column/{alias}`
- 站内文章：`/party/article/{id}`
- canonical namespace：`/party/**`

党建栏目与文章的允许作用域、四条原站内容线、旧 `plist/pdetail` 地址迁移边界以 `docs/specifications/party.md` 为准。

主站 `MAIN` Navigation 中“中心党建”预置条目指向 `/party/`，当前窗口进入。

## 3. 公开前端站点边界

### 3.1 Main Site

Main Site 持有：

- 主站 App / Router；
- 主站 Header / Footer / Navigation Layout；
- 蓝白主题、页面 Frame 与主站视觉样式；
- 首页、内容（栏目/文章）、单页、固定集成等页面模块。

主站页面模块采用 route-level lazy loading，避免继续以同步 import 将所有页面代码绑定到首屏 bundle。

### 3.2 Party Building Site

Party Building Site 持有：

- 独立 App / Router；
- 独立 Header / Footer / Navigation Shell；
- 独立红色主题与页面 Frame；
- 正式党建首页模块；
- 党建栏目列表与文章详情模块。

Party Building Site 不复用 Main Site Header/Footer DOM 与主站主题 CSS。当前可以共用无主题的 API transport、CMS DTO、静态资源 URL、SEO/通用工具等技术能力。

### 3.3 Shared Boundary

`shared/` 只放已经证明跨两个公开 Site 稳定复用、且不携带站点主题所有权的能力。不得为了“去重复”提前抽取：

- Header / Footer；
- Navigation Layout；
- 页面 Frame；
- 颜色变量；
- Typography / Theme CSS；
- 首页区块布局。

存在两个 Site Entry 不等于构建通用多站点平台，也不要求把中心党建拆成独立 Repository / Frontend Project。

## 4. 主站首页数据来源

首页固定模板继续复刻现网主要布局。运营数据来源统一为：

- 主导航：`MAIN` Navigation；
- 首屏右侧五个蓝色入口：`HOME_SHORTCUT` Navigation，图标来自导航条目 `iconPath`；
- 业务指南快速入口：`HOME_QUICK` Navigation，图标来自导航条目 `iconPath`；
- 轮播内容：`HOME_CAROUSEL` CmsList；
- 轮播自动切换间隔：`HOME_CAROUSEL_INTERVAL_SECONDS` SiteProperty；
- 通知公告 / 就业动态 / 招聘公告：Column + Article；
- 招聘活动横幅：`HOME_RECRUITMENT_PROMO` 展示位（技术实现仍为 AdvertisementSlot）；
- 网站导航 / 友情链接：`SITE_LINKS` 分组 CmsList；
- 网站名称、电话、备案、版权等：SiteProperty；
- NCSS 区域：固定工程集成，不提供 CMS 管理。

公开站不得再同时读取旧 JSON 配置与新业务对象并合并结果。

## 5. 数据与展示职责

CMS 提供内容数据属性，公开站页面设计决定如何展示。Column `coverPolicy`、CmsList `imagePolicy` 都是数据契约，不向公开站下发展示模式配置。

首页 `HOME_CAROUSEL` 基线 `imagePolicy=REQUIRED`，公开站只消费具有有效 `imagePath` 的启用列表项。只有一张有效图片时静态展示；存在两张及以上时按列表顺序自动切换。切换间隔读取 `HOME_CAROUSEL_INTERVAL_SECONDS`，正常配置必须是大于 0 的整数秒；公开端仍保留安全 fallback，避免异常历史数据阻断首页。

轮播项 URL 存在时图片可点击，URL 为空时仅显示图片。标题当前可作为 caption/alt 数据使用；是否显示 caption、指示器、切换动画、图片尺寸等属于页面工程设计，不由 CmsList imagePolicy 或 SiteProperty 控制。

当前 `SITE_LINKS` 相关列表基线 `imagePolicy=NONE`，页面按 title + URL 输出文字链接。未来若视觉设计确认需要 Logo，应先把对应列表数据策略调整为 `OPTIONAL / REQUIRED` 并补充数据，再由公开页面设计消费 `imagePath`；不得通过新增 `displayMode` 控制布局。

Public Article Summary 可以包含可选 `coverResourceId`，为带图模板提供可消费数据；具体页面是否展示封面仍由页面基线决定。栏目 `coverPolicy` 不直接决定 DOM 布局。

首页快捷入口和快速导航直接使用 Navigation `iconPath`；不得使用数组下标拼接 `top-nav-01`、`guide-01` 等路径。

中心党建正式内容已经确认复用现有 Column + Article；四条真实内容线、INTERNAL / EXTERNAL_LINK 行为和 Party 作用域由 `docs/specifications/party.md` 约束。独立视觉主题本身不构成新增党建专属 CMS 模型或 Admin Module 的理由。

## 6. 工程资产

以下内容允许固定在公开站工程或版本化静态资产中：

- 主站首页区域布局、尺寸关系和视觉样式；
- 各 Site 自己的 Header / Footer / Navigation Layout；
- 各 Site 自己的页面 Shell / Theme；
- NCSS Logo、学生入口、企业入口及其固定集成布局；
- 明确无需运营维护的装饰图标与一次性外部平台接缝。

站点导航业务图标虽然文件可以来自版本化 `/static/icons/**`，但“某条导航使用哪个图标”属于 Navigation 数据，不属于前端数组顺序规则。

当工程资产未来产生真实运营维护需求时，再通过 Requirement Change 判断是否升级为 CMS 对象。

## 7. 页面基线

主站栏目列表、文章详情、独立单页、业务指南单页分组继续以现网页面主要版式为复刻基准。单页分组 Tab 从 PageGroup 成员数据生成。

中心党建 Foundation 已结束；正式首页、栏目列表、文章详情、红色视觉和真实静态资源依据 `docs/specifications/party.md` 与原站证据收敛，不再以 Foundation 页面作为验收基线。

招聘信息和直播课程继续保留本站页面框架/占位，不加载真实第三方 iframe。

## 8. Acceptance Criteria

- Main Site 的 `/`、`/column/**`、`/article/**`、`/page/**` 由同一 Main Site Entry 承载，既有 canonical URL 与视觉主基线无回归；
- Main Site 源码保持 `app / shell / modules` 所有权边界和 route-level lazy loading；
- `/party/**` 由独立 Party Building HTML Entry、Vue App、Router、Shell 与红色主题样式承载，直接访问与刷新正常；
- Party 首页/栏目/文章正式行为符合 `docs/specifications/party.md`；
- 主站与党建主题 CSS 互不污染，党建不得依赖主站 Header/Footer DOM 才能正常显示；
- `MAIN` Navigation 中“中心党建”指向 `/party/`；
- 当前公开站仍为一个 `frontend/public-site` 工程、一个 build/deploy 单元；不引入 Module Federation 或独立党建前端工程；
- 首页不再读取 `SERVICE_LINKS`、`HOME_BANNERS`、`SITE_LINK_GROUPS`、`HOME_PROMO_BANNER_PATH`、`HOME_NCSS_LOGO_PATH` 作为运行时内容源；
- 首页五个蓝色入口来自 `HOME_SHORTCUT`，图标来自导航条目而非排序下标；
- 业务指南快捷入口来自 `HOME_QUICK`，图标来自导航条目；
- `HOME_CAROUSEL` 使用图片必填数据契约；单图静态展示，多图按列表顺序自动切换；
- 自动切换间隔来自正整数 SiteProperty `HOME_CAROUSEL_INTERVAL_SECONDS`；
- 轮播项 URL 可选，没有 URL 时不产生伪链接；
- `SITE_LINKS` 当前以不使用图片的文字链接基线输出，未来 Logo 需求不引入 displayMode；
- Public Article Summary 能提供可选封面引用，但页面是否展示由模板设计决定；
- 招聘活动横幅来自宣传展示位；
- 联系电话等站点属性来自 SiteProperty；
- NCSS 固定集成正常。