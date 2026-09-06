# jilinjobs-cms

`jilinjobs-cms` 是“吉林省高等学校毕业生就业信息网”信息发布、公开展示与相关 CMS 能力的 Consumer Repository。

## 当前目标

当前版本以原网站现有结构和视觉关系为主站公开基线，采用 Vue + Spring Boot 重建中心主站，并在同一公开站前端工程中建设具有独立红色视觉主题的“中心党建”特殊栏目/专题页面。管理端已经完成独立前端工程与 Modular SPA 收敛；公开站已经完成 **Multi-entry Modular SPA** 基础架构：Entry 只按真实 Theme / Router Boundary 划分，中心主站与中心党建分别拥有独立 App、Router、Banner 与内容主题，但继续共用 `frontend/public-site` 工程、Vue/Vite 技术栈、构建发布链路和 Spring Boot CMS Backend；主导航与 Footer 使用 Shared Shell Components 保持公共区域一致。

中心党建 EU-26～EU-29、EU-30 Carousel Architecture & Behavior Convergence、EU-31 Database Migration Baseline Convergence、Issue #60 / B1～B3 收敛形成的 EU-32～EU-35，以及 Issue #60 / D1 收敛形成的 EU-36 均已完成并收敛到 `main`。**Issue #77 的 Site Package 前置架构收敛已进一步完成 EU-37 — Site Package Contract & Provisioner Foundation、EU-38 — Stable Site Structure Package Migration 与 EU-39 — Navigation Stable Identity & Site Package Reconcile；三者均已集成到 `main` 并通过 Post-Integration Verification。当前没有 Ready Execution Unit。Issue #77 保持 OPEN，剩余 V2/default Runtime composition convergence、Site Asset Ownership & Runtime Composition、Canonical Migration Compatibility & E1～E3 Re-entry 仍为 Planning / Requirement Candidates。** 后续只有重新完成 current audit、Requirement / Specification / 必要 Technical Planning、`slice-work` 与 Readiness Check 的候选才能进入 Execute。Roadmap 顺序、预编号或 Issue 标签本身不构成执行授权。

当前权威需求：

- `docs/requirements/information-publishing.md` V4.8
- `docs/requirements/information-publishing-eu30-amendment.md` V4.9-EU30（EU-30 已完成，保留追溯）
- `docs/requirements/party-positioning.md`
- `docs/requirements/database-migration-baseline-convergence.md`（EU-31 已完成；其 accepted baseline / append-only 边界继续有效）
- `docs/requirements/list-definition-group-governance.md`（EU-32 accepted）
- `docs/requirements/admin-guidance-governance.md`（EU-33 accepted）
- `docs/requirements/rich-text-authoring.md`（EU-34 / EU-35 accepted）
- `docs/requirements/public-frontend-replaceability.md`（EU-36 accepted）
- `docs/requirements/cms-site-package-boundary.md`（Issue #77；EU-37 / EU-38 / EU-39 accepted scope 与后续边界 Authority）

当前 Specification：

- `docs/specifications/cms-core.md`
- `docs/specifications/public-site.md`
- `docs/specifications/party.md`
- `docs/specifications/public-shared-shell.md`
- `docs/specifications/admin-site.md`
- `docs/specifications/preset-site-structure.md`
- `docs/specifications/database-migration-baseline-convergence.md`（EU-31 accepted baseline）
- `docs/specifications/list-definition-group-governance.md`（EU-32 accepted）
- `docs/specifications/admin-guidance-governance.md`（EU-33 accepted）
- `docs/specifications/rich-text-authoring.md`（EU-34 / EU-35 accepted）
- `docs/specifications/public-frontend-replaceability.md`（EU-36 accepted）
- `docs/specifications/cms-site-package-boundary.md`（Issue #77 accepted boundary）

当前 Architecture Decision：

- `docs/architecture/decisions/ADR-0001-admin-frontend-module-integration.md`
- `docs/architecture/decisions/ADR-0002-public-site-multi-entry-modular-spa.md`
- `docs/architecture/decisions/ADR-0003-public-shared-shell-components.md`

当前 Technical Plan / Governance：

- `docs/technical/carousel-list-placement.md`（EU-30 已接受方案，保留追溯）
- `docs/technical/database-migration-baseline-convergence.md`（EU-31 accepted baseline）
- `docs/technical/rich-text-authoring-plan.md`（EU-34 / EU-35 accepted technical plan）
- `docs/technical/public-frontend-replaceability.md`（EU-36 accepted technical plan）
- `docs/technical/cms-site-package-boundary.md`（EU-37 / EU-38 / EU-39 已完成；Issue #77 剩余 Planning 继续使用）
- `docs/technical/cms-architecture.md`
- `docs/technical/configuration-governance.md`
- `docs/technical/backend-service.md`
- `docs/technical/public-site-frontend.md`
- `docs/technical/party-frontend.md`
- `docs/technical/admin-frontend.md`
- `docs/technical/admin-frontend-integration.md`
- `docs/technical/verification-strategy.md`
- `docs/technical/preset-site-structure.md`

当前 Ready Execution Unit：

当前没有 Ready Execution Unit。

最近完成的执行单元：

- `docs/work/eu39-navigation-stable-identity.md`（COMPLETED）
- `docs/work/eu38-stable-site-structure-package-migration.md`（COMPLETED）
- `docs/work/eu37-site-package-provisioner-foundation.md`（COMPLETED）
- `docs/work/eu36-public-frontend-source-isolation.md`（COMPLETED）
- `docs/work/eu35-shared-rich-text-authoring.md`（COMPLETED）
- `docs/work/eu34-rich-text-html-safety-foundation.md`（COMPLETED）
- `docs/work/eu33-admin-guidance-governance.md`（COMPLETED）
- `docs/work/eu32-list-definition-group-governance.md`（COMPLETED）
- `docs/work/eu31-database-migration-baseline-convergence.md`（COMPLETED）
- `docs/work/eu30-carousel-convergence.md`（COMPLETED）
- `docs/work/frontend-follow-up-execution-units.md`（历史预编号规划，仅追溯；不再作为后续执行顺序 Authority）

当前后续规划入口：

- GitHub Issue #77：当前 Site Package boundary 规划入口；EU-37 / EU-38 / EU-39 已完成，剩余 V2/default Runtime composition、Slice C / Slice D 继续按 current audit / slice / readiness 形成后续候选；
- GitHub Issue #60：EU-30 后续需求边界候选池；D1 / EU-36 已完成，E1～E3 等待 Issue #77 前置 compatibility 收敛，C1/C2 继续独立保留；
- GitHub Issue #59：EU-31 已完成后的 Browser Compatibility 等后置规划候选；
- GitHub Issue #57：公开站导航查询 / 渲染架构相关讨论候选，不自动晋升为执行范围。

中心党建阶段追溯：

- `docs/work/party-convergence-execution-units.md`

最近完成的公开站架构执行单元（阶段追溯）：

- `docs/work/public-site-multi-entry-execution-units.md`

最近完成的管理端执行单元（阶段追溯）：

- `docs/work/admin-frontend-convergence-execution-units.md`

管理端双前端拆分、通用 CMS 模型和 Admin Modular SPA 已完成当前阶段收敛；公开站 Multi-entry Modular SPA、中心党建正式页面与历史内容、EU-30 轮播架构与行为、EU-31 数据库迁移基线、EU-32～EU-35 的列表结构治理、管理端提示责任治理与富文本安全/编辑能力、EU-36 的 Public source ownership / managed resource projection，以及 EU-37～EU-39 的 Site Package contract、provisioner foundation、stable site structure migration 与 Navigation stable identity/reconcile 均已完成当前阶段收敛。当前不直接启动新的 Execution Unit；后续只从剩余 Planning / Requirement Candidates 中按 Consumer-local Method 重新形成 Candidate Execution Unit，并经 Readiness Check PASS 后进入 Execute。

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
