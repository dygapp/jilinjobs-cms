# jilinjobs-cms

`jilinjobs-cms` 是“吉林省高等学校毕业生就业信息网”信息发布、公开展示与相关 CMS 能力的 Consumer Repository。

## 当前目标

当前版本以原网站现有结构和视觉关系为主站公开基线，采用 Vue + Spring Boot 重建中心主站，并在同一公开站前端工程中建设具有独立红色视觉主题的“中心党建”特殊栏目/专题页面。管理端已经完成独立前端工程与 Modular SPA 收敛；公开站已经完成 **Multi-entry Modular SPA** 基础架构：Entry 只按真实 Theme / Router Boundary 划分，中心主站与中心党建分别拥有独立 App、Router、Banner 与内容主题，但继续共用 `frontend/public-site` 工程、Vue/Vite 技术栈、构建发布链路和 Spring Boot CMS Backend；主导航与 Footer 使用 Shared Shell Components 保持公共区域一致。

中心党建 EU-26～EU-29 已完成正式页面与历史迁移收敛；**EU-30 Carousel Architecture & Behavior Convergence 已于 2026-09-05 完成最终 Human Review、canonical acceptance 与 post-promotion verification。当前没有新的编号 Execution Unit。** 后续工作统一回到 GitHub Issues #59 / #60 的未编号 Planning / Requirement Candidates，只有完成 Intent / Requirement Clarification、Specification、必要 Technical Planning / Research、Slice Work 与 Readiness Check 后才分配新的 EU 编号。

当前权威需求：

- `docs/requirements/information-publishing.md` V4.8
- `docs/requirements/information-publishing-eu30-amendment.md` V4.9-EU30（EU-30 已完成，保留追溯）
- `docs/requirements/party-positioning.md`

当前 Specification：

- `docs/specifications/cms-core.md`
- `docs/specifications/public-site.md`
- `docs/specifications/party.md`
- `docs/specifications/public-shared-shell.md`
- `docs/specifications/admin-site.md`
- `docs/specifications/preset-site-structure.md`

当前 Architecture Decision：

- `docs/architecture/decisions/ADR-0001-admin-frontend-module-integration.md`
- `docs/architecture/decisions/ADR-0002-public-site-multi-entry-modular-spa.md`
- `docs/architecture/decisions/ADR-0003-public-shared-shell-components.md`

当前 Technical Plan / Governance：

- `docs/technical/carousel-list-placement.md`（EU-30 已接受方案，保留追溯）
- `docs/technical/cms-architecture.md`
- `docs/technical/configuration-governance.md`
- `docs/technical/backend-service.md`
- `docs/technical/public-site-frontend.md`
- `docs/technical/party-frontend.md`
- `docs/technical/admin-frontend.md`
- `docs/technical/admin-frontend-integration.md`
- `docs/technical/verification-strategy.md`
- `docs/technical/preset-site-structure.md`

最近完成的执行单元：

- `docs/work/eu30-carousel-convergence.md`（COMPLETED）
- `docs/work/frontend-follow-up-execution-units.md`（历史预编号规划，仅追溯；不再作为后续执行顺序 Authority）

当前后续规划入口：

- GitHub Issue #59：数据库迁移 baseline convergence / Browser Compatibility 等后置规划候选；
- GitHub Issue #60：EU-30 后续需求边界候选池。

中心党建阶段追溯：

- `docs/work/party-convergence-execution-units.md`

最近完成的公开站架构执行单元（阶段追溯）：

- `docs/work/public-site-multi-entry-execution-units.md`

最近完成的管理端执行单元（阶段追溯）：

- `docs/work/admin-frontend-convergence-execution-units.md`

管理端双前端拆分、通用 CMS 模型和 Admin Modular SPA 已完成当前阶段收敛；公开站 Multi-entry Modular SPA、中心党建正式页面与历史内容、EU-30 轮播架构与行为也已完成当前阶段收敛。下一步不得机械沿用旧规划中的“EU-31”，应从 Issues #59 / #60 选择候选并按 Consumer-local Method 形成新的 Ready Execution Unit。

历史阶段文档继续保留用于追溯，但不再作为当前目标架构：

- `docs/specifications/center-main-site-core.md`
- `docs/specifications/admin-frontend-convergence.md`
- `docs/technical/center-main-site-core.md`
- `docs/technical/admin-frontend-convergence.md`
- `docs/work/center-main-site-core-execution-units.md`

项目演进状态：`docs/project/project-roadmap.md`。
项目本地开发方法：`docs/project/development-method.md`。

## Repository Authority

仓库工作首先遵循根目录 `AGENTS.md`。产品事实、当前范围、技术状态和验证结果以本 Consumer Repository 当前权威文件和可观察 GitHub / Runtime Evidence 为准。

`dygapp/agentic-dev` 提供可复用 AI 开发方法与 Skills；普通 Consumer 开发优先使用本仓库已经固化的项目本地规则。

## 当前 CMS 边界

CMS 通用业务对象：

- 栏目、文章；
- 单页、单页分组（技术层继续使用 `Page / PageGroup`）；
- 导航位置、多级导航及可选导航图标；
- 通用列表、列表项；
- 宣传展示位、展示内容；
- 网站属性；
- 网站静态资源。

管理端信息架构按“内容管理 / 内容结构 / 运营展示 / 站点设置”组织：文章、单页、列表属于主要内容管理；栏目和导航属于内容结构；宣传展示属于运营展示；网站属性和静态资源属于站点设置。具有明确“容器 → 成员”关系的管理页优先采用左侧选择组织上下文、右侧维护成员的交互。

公开站和管理端是同级独立 Vue / Vite 前端工程，共享 Spring Boot CMS Backend。公开站工程内部不把普通页面类型机械映射成独立 HTML Entry；主站 `/`、`/column/**`、`/article/**`、`/page/**` 统一属于 Main Site Entry，中心党建使用 Party Site Entry 和 `/party/**` URL namespace。两个公开 Entry 当前同构建、同部署；App、Router、Banner、内容 Frame 与主题内容模板按 Entry 隔离，Navigation/Footer 则复用 Shared Shell Components，通过主题变量切换主站蓝色与 Party 红色。

中心党建正式内容继续复用通用 CMS：预置父栏目 `party` 组织 `party-voice / party-work / party-rules / party-study / party-theme-education` 五个子栏目；这些内容线都使用通用 Article，并允许 INTERNAL / EXTERNAL_LINK 混合。`学习园地` 只是 PartyHome 入口页对党规党章与理论学习的固定视觉分组，不新增 CMS 类型；`party-theme-education / 主题教育` 是正常 Party 内容栏目，但不新增 PartyHome 第五个固定内容区。Party canonical URL 为 `/party/`、`/party/column/{alias}`、`/party/article/{id}`。原站 `plist.html`、当前 `pdetail.html` 和更早 `detail.html` 地址及其 `content_id/typeCode` 参数变体只作为历史迁移映射输入，不延续为新版 Router 模型。

EU-29 `acceptedSnapshot` 保持 181 篇冻结 provenance；EU-30 接受 `主题教育` 2 条历史增量后，`data-migrations/party/v1` 当前 status = `accepted-canonical`，current Runtime Dataset = 183 篇。EU-29 原 accepted artifact digest 和 acceptedSnapshot 不因 EU-30 promotion 被重写。

当前工程技术命名统一使用 `party / Party` 表示中心党建 Site/模块，`party-home / PartyHome` 只用于 `/party/` 入口页（如 `PartyHomeView.vue`）。已执行 V13/V14 Migration 和历史 PR / 分支中的 `party-building` 属于历史/兼容标识，不回写历史；当前源码目录、组件、测试、静态基线和 Party 专项 Authority 不再使用 `PartyBuilding / party-building` 作为现行命名。

管理端当前是单一 Vue SPA，但源码按 `app/`、`shared/` 与 `modules/cms/` 分离：Shell 只聚合模块声明，CMS Module 自己声明 routes/navigation，并使用 Vue Router 动态 import 进行路由级懒加载。Module Federation、iframe 或其他运行时微前端机制不属于当前基础设施；未来只有出现真实独立发布、独立部署、跨团队或跨技术栈要求时再单独评估。

公开站共享 Shell 规则：Main / Party 的主导航与 Footer 只保留一份结构和交互实现，分别由 `shared/components/PublicNavigation.vue`、`shared/components/PublicFooter.vue` 与 `shared/styles/public-shell.css` 承担；各 Entry 仅通过主题变量覆盖颜色。Main/Party 自有的 Banner、内容 Frame、轮播及页面模板继续留在对应 Site 下。API transport、CMS DTO、资源 URL、SEO/通用工具等稳定技术能力同样由 `shared/` 承担。

首页 NCSS 区域属于主站固定工程集成，不要求后台管理。需要持续运营维护的数据优先使用 CMS 对象，避免在 SiteProperty JSON、导航和 Vue 常量中维护重复数据来源。中心党建已证明可以复用现有栏目/文章模型，不因公开前端存在独立 Entry 就预设新的党建专属 Admin Module。

通用列表项支持 `LINK / ARTICLE`：LINK 保存标题、图片、URL 等自身数据；ARTICLE 引用已有文章与可选覆盖 Resource，不改变文章单一栏目归属。`ARTICLE + INTERNAL` 在管理端表达为“站内文章”，`ARTICLE + EXTERNAL_LINK` 表达为“外链文章”；EXTERNAL_LINK Article 通过 ARTICLE 投放时继续跟随 Article 当前标题与 external URL，不重复维护 URL。两种来源都保留打开方式、排序等投放属性；前台具体页面根据自身设计决定消费哪些属性以及如何展示，不由 CMS 列表定义控制视觉模式。导航条目可维护可选图标，避免前台按排序位置推导图标。

Main / Party 轮播统一使用 `CAROUSEL_INTERVAL_SECONDS`、`CAROUSEL_MAX_ITEMS` 与共享 lifecycle，统一 hover/focus/visibility pause、reduced-motion、失败图片补位和 active identity 行为，但不统一 DOM 视觉组合、主题、Caption 或比例。长期原则为“统一行为规则和生命周期，不统一视觉表达”。

网站规划基线中的关键结构对象使用只读 `preset` 标识保护：预置栏目、导航位置/条目、单页分组/单页、列表容器、宣传展示位和稳定网站属性定义不能被误删；具有稳定 Alias/Code/Key 的预置对象不能修改该身份字段。`preset` 不等于完全只读，名称、排序、启停以及正常运营字段仍按各自模型维护；Article、CmsListItem、Advertisement 等运营成员不因此变成预置内容。普通 Admin API 新增对象默认 `preset=false`，客户端不能自行设置或取消该标识。

工程基线静态资源继续位于 `/static/home`、`/static/brand`、`/static/footer`、`/static/icons` 等版本化目录；CMS 运行时上传统一进入 `/static/uploads/**`，由宣传展示/列表/导航图标/RESOURCE_PATH 网站属性等管理界面复用统一图片资源选择与上传能力。中心党建可可靠取得并验证的稳定视觉资源进入 `site-baseline/static/party/**`；历史党建文章正文资源继续属于独立内容迁移范围。公开站设计模板不得直接热链第三方图片、图标、字体等稳定展示资源（开源 JS/CSS 依赖和业务外链除外）。

静态资源“受保护”状态由 Backend 负责：固定部署/工程基线来自 Spring 外部化配置，当前网站属性、列表、宣传展示和导航直接引用的资源由 Runtime 动态加入保护集合；该状态不是管理员人工维护的重要性等级。普通删除必须拒绝，明确替换仍允许。

配置责任长期遵循 `docs/technical/configuration-governance.md`：稳定领域/安全/页面模板契约保留代码常量；运营可维护数据进入 CMS / 网站属性；低频结构定义进入 CMS 资源元数据；部署实例差异进入 Spring 外部化配置；CI、FRP 和 Review 环境参数属于 CI / Deployment Variables。存在字面硬编码本身不构成缺陷，禁止为了“消除硬编码”机械增加系统配置。

当前阶段明确不实现用户、账号、角色、登录和权限控制。未来“普通管理员 / 超级管理员”差异只作为规划边界，不进入当前代码和验收条件；`preset` 保护、删除确认、路径安全、真实媒体校验和受保护资源等业务安全措施仍继续执行。

## 前端工程

目标结构：

```text
frontend/
├── public-site/
│   └── src/
│       ├── shared/
│       └── sites/
│           ├── main/
│           │   ├── app/
│           │   ├── shell/
│           │   └── modules/
│           └── party/
│               ├── app/
│               ├── shell/
│               └── modules/
│                   ├── home/
│                   └── content/
└── admin/
    └── src/
        ├── app/
        ├── shared/
        └── modules/cms/
```

- Main Public Site base：`/`
- Party Site base：`/party/`
- PartyHome route：`/party/`（route name `party-home`）
- Party column：`/party/column/{alias}`
- Party article：`/party/article/{id}`
- Admin Site base：`/admin/`
- CMS Admin canonical routes：`/admin/cms/**`
- Backend API：`/api/**`
- Public static assets：`/static/**`

## 验证原则

完成状态必须由与目标提交和具体 Evidence Claim 匹配的 Current Evidence 支持。Backend、Public Site、Admin Site、Integrated Browser 与 Review Environment 的实时结果由 GitHub Actions 保存；README 不复制具体 Run 编号。

中心党建正式收敛已经证明：真实 Party 内容结构进入站点基线；PartyHome 入口、栏目、文章 canonical route 正常；INTERNAL / EXTERNAL_LINK 行为正确；非党建文章不能由 Party 详情套用党建模板；Main/Party 共享主导航和 Footer 结构无漂移且只存在主题表达差异；主站现有 canonical URL 和蓝白视觉无回归；历史运营文章未被误塞入 Flyway。Visual Fidelity 仍必须由原站参考证据、AI Visual 与 Human Review 共同支持，不能由 Functional Browser PASS 单独声明。

祖先提交的 Runtime / Human Review Evidence 不机械继承。发生 CMS 模型、数据库、API 或公开站数据源/Entry/Router 调整后，应重新取得受影响 Evidence；仅对未改变相关 Runtime / content bytes 的纯 Authority / acceptance metadata 后继变更，才可按 `docs/technical/verification-strategy.md` 的 Evidence Impact 判断复用未受影响 Claim，并对发生变化的最终状态重新取得对应 Current Evidence。