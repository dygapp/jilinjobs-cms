# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap。

## 方法基线

```text
dygapp/agentic-dev
Validation Baseline: master@d9fad0da83dbdb61cac5eb9778b0258c6861eef1
Capability Milestone: baseline-2026-09-04-engineering-capability@5be2e6aad29b2be6b8535b3690daf3533ee22a46
```

普通开发优先使用 Consumer-local：`AGENTS.md`、`docs/project/development-method.md`、当前 Requirement / Specification / Technical Plan。当前 Validation Baseline 相对上一 Consumer baseline只增加 1 个 Stable Maintenance 提交；本 Consumer 新选择性固化 Planning Candidate 与 Execution Unit 身份边界：Planning / Requirement Candidate 在 `slice-work` 前不具有 Execution Unit 身份，`slice-work` 在上游 Ready 后形成 Candidate Execution Unit 并可分配稳定 Identifier，只有 `readiness-check` PASS 后才成为 Ready Execution Unit；Identifier、Roadmap 顺序和 Issue 标签均不替代 Readiness 或 Execute 授权。此前固化的 Durable Evidence Promotion、长生命周期 Review Environment owner / lease / stale-run 规则与 Data Access Scope & Boundedness Control 继续有效；没有新增 Method Stage、Engineering Discipline、Technology Profile 或 Task-oriented Skill。`agentic-dev` 自身 Project Roadmap、Foundation / Engineering Discipline Expansion 状态、PR / Issue / Experiment 事实不进入本 Roadmap。

## 总体路线

| 路线 | 状态 | 结果 |
|---|---|---|
| Consumer Repository Bootstrap | 已完成 | 独立 Consumer Authority 与工程骨架 |
| EU-01～EU-06 信息发布核心能力 | 已完成 | 栏目、导航、文章、发布、公开页面、附件、浏览量、响应式 |
| Feature-wide Convergence | 已完成 | 首轮功能整体验证闭环 |
| RC-01 人工集成评审环境 | 已完成 | 临时 Review Environment 与自动/人工证据边界 |
| 站点基线收敛 EU-07～EU-12 | 已完成 | 现网站点结构、页面模型、规范 URL、网站配置、静态资源与初始化基线 |
| 首页与公共视觉基线收敛 | 已完成 | 原站关键视觉资源、蓝白体系、首页结构、Header / Nav / Footer、移动端基础适配与 Human Review |
| 页面细节视觉收敛 | 已完成 | 栏目列表、文章详情、固定页面、页面组 / Tab、业务指南、页脚与外链文章行为 |
| 管理端工程分离与功能收敛 | 已完成当前阶段 | 双前端物理拆分、通用 CMS、Admin Modular SPA / CMS Module Boundary |
| 公开站 Multi-entry Modular SPA 与中心党建基础框架 | 已完成 | Main / Party Theme + Router Boundary，Main 模块化，Party 独立 Entry / Router / Theme |
| 中心党建正式页面与内容收敛 EU-26～EU-29 | 已完成 | 正式栏目、视觉、历史内容迁移、EU-29 accepted canonical dataset |
| EU-30 Carousel Architecture & Behavior Convergence | 已完成 | LINK / ARTICLE 投放模型、统一轮播配置与生命周期、Article/List ownership-placement boundary、主题教育历史增量、position 2 ARTICLE 升级、最终 Human Review 与 accepted-canonical promotion 全部完成 |
| EU-31 Database Migration Baseline Convergence | **已完成（历史 baseline）** | V1～V20 开发期 Flyway transcript 收敛；development DB recreation boundary 与 canonical migration independence 保留；其“Site data 位于 Backend V2”当前由 EU-41 修订 |
| EU-32 List Definition Group Governance | **已完成** | `groupCode` 保持内部结构元数据，ordinary Admin 不再暴露结构分组写权限 |
| EU-33 Admin Guidance & Explanation Responsibility Governance | **已完成** | 管理界面保留操作必要信息，结构身份与实现背景退出普通运营提示 |
| EU-34～EU-35 Rich Text Safety & Shared Authoring | **已完成** | 服务端 HTML safety foundation + Article/Page 共用 Tiptap 富文本编辑能力完成并通过 Post-Integration Verification |
| EU-36 Public Frontend Source Isolation & Managed Resource Projection | **已完成** | Public production source 已移除 Admin endpoint knowledge，managed Article body image 改由 Backend Public projection 暴露 Public Resource URL，source-boundary guard 与全链回归均已闭环 |
| EU-37～EU-38 Site Package Foundation & Stable Structure Migration | **已完成** | Site Package v1 contract / provisioner foundation 已建立；七类具有 stable identity 的 JilinJobs preset structure 已迁入 `sites/jilinjobs/**` |
| EU-39 Navigation Stable Identity & Site Package Reconcile | **已完成** | NavigationItem 获得 provisioning-only stable `code`；40 条正式导航进入 Site Package；Fresh create、Legacy adoption、stable-code reconcile 与集成验证完成 |
| EU-40 Explicit Site Package Runtime Composition Activation | **已完成** | `cms.site-package.root` opt-in lifecycle、Repository Runtime、Canonical / Upgrade importer composition 已显式消费 Site Package；旧 V2 responsibility 有意留待后续分类 |
| EU-41 Site Bootstrap & Generic Schema Baseline Separation | **已完成** | Backend Flyway 已收敛为 Generic Schema-only lineage；七条 Fresh Site operational defaults 已转为无 migration-number 的 one-time Site bootstrap；普通 Runtime / repeated bootstrap 不覆盖或复活 operator data |
| EU-42 Site Asset Package Ownership & Runtime Projection | **已完成** | 31 个稳定 Site assets 已迁入 `sites/jilinjobs/assets/**`；manifest SHA-256 integrity、create-if-missing Runtime projection、StaticResource protected-path 与 CI / Review empty-root composition 均已闭环 |
| Issue #77 CMS Core / Site Package / Public Renderer Boundary | **当前 Planning Priority / 部分完成** | EU-37～EU-42 已完成；当前没有 Ready EU，Slice C 已关闭，剩余 Canonical Migration lifecycle compatibility 与 E1～E3 re-entry，之后再做 Repository Split Readiness Assessment |
| Issue #60 E1～E3 Main Site Formal Content | **前置依赖等待** | 保留为 Planning Candidates；待 Issue #77 Site Package boundary / compatibility 收敛并取得兼容证据后重新进入 Planning |
| 其他 Planning / Requirement Candidates | **规划层保留** | Issues #57 / #59 / #60 的 C1/C2、Browser Compatibility、Public Rendering Architecture 等仍按各自边界保留 |
| 真实第三方深度集成 | 条件性后续 | 根据接口、认证、可靠性与 Product Intent 再进入 Specification / Slice |

## 已完成里程碑

| 日期 | 里程碑 |
|---|---|
| 2026-08-24 | Feature-wide Convergence 完成 |
| 2026-08-26 | RC-01 Human Integration Review Environment 完成 |
| 2026-08-26 | Consumer-local 方法与 Roadmap 固化 |
| 2026-08-27 | 原站取证、关键 Product Intent 与 EU-07～EU-12 站点基线收敛完成 |
| 2026-08-28 | 首页与公共视觉基线、真实静态资源、AI / Human Review 数据隔离及视觉复核完成 |
| 2026-08-28 | 栏目列表、文章详情、固定页、业务指南、页脚、favicon、举报固定页与外链文章等页面细节收敛完成 |
| 2026-09-01 | 管理端独立双前端、通用 CMS、预置结构保护、配置治理、统一图片预览与静态资源保护收敛 |
| 2026-09-01 | Admin Modular SPA 架构收敛，形成 Admin Shell / Shared / CMS Module Boundary 与 `/admin/cms/**` canonical namespace |
| 2026-09-01 | 公开站 Multi-entry Modular SPA 与 Party Foundation 完成，Main / Party / Admin / Backend / Gateway 验证闭环 |
| 2026-09-02 | 中心党建正式阶段完成原站重新取证并切分 EU-26～EU-29 |
| 2026-09-04 | EU-29 Historical Content Migration & Final Review 关闭；181 篇 acceptedSnapshot、历史资源和 4 条 Party carousel 晋升为仓库 canonical dataset |
| 2026-09-04 | Capability Milestone 更新到 `baseline-2026-09-04-engineering-capability@5be2e6aad29b2be6b8535b3690daf3533ee22a46`，Consumer-local 固化 Data Access Scope & Boundedness Control |
| 2026-09-04 | Validation Baseline 更新到 `agentic-dev master@394d1c3cde04b35940d5e33b7cbcaaf6557678ce`，选择性固化 Durable Evidence Promotion 与 Review Environment owner / lease / stale-run 规则 |
| 2026-09-05 | EU-30 最终 Human Review PASS；`主题教育` 2 条历史增量被接受，当前 Party canonical Runtime Dataset 从 EU-29 provenance 181 篇扩展为 183 篇；EU-29 acceptedSnapshot 原样保留 |
| 2026-09-05 | EU-30 acceptance promotion 后 CI #659、Canonical #123、EU-29→EU-30 Upgrade #71 全部 PASS；PR #58 随后已合并到 `main`，EU-30 canonical acceptance / Authority 正式收口 |
| 2026-09-05 | 撤销原“EU-31 Browser Compatibility”预编号路线；数据库迁移 baseline、浏览器兼容、列表/资源治理与剩余公开站工作统一回到 Issues #59 / #60 的 Planning / Requirement Candidates |
| 2026-09-05 | 数据库迁移 baseline convergence 在 Ready Specification / Technical Planning 后由 Slice Work 形成 Candidate EU-31，并经 Readiness Check PASS 晋升为 Ready Execution Unit；该编号不继承旧 Browser Compatibility 规划 |
| 2026-09-05 | EU-31 将 active Flyway V1～V20 收敛为当时的 `V1__current_cms_schema.sql` + `V2__current_preset_data.sql`；Canonical / Upgrade Verification PASS；其 development DB recreation boundary 与 historical migration independence 继续有效 |
| 2026-09-05 | Validation Baseline 更新到 `agentic-dev master@d9fad0da83dbdb61cac5eb9778b0258c6861eef1`，选择性固化 Planning Candidate → Candidate Execution Unit → Ready Execution Unit 身份边界；Capability Milestone 保持不变 |
| 2026-09-05 | Issue #60 / B1 收敛为 EU-32，完成列表定义内部 `groupCode` 与 ordinary Admin 写权限边界治理并集成 |
| 2026-09-05 | Issue #60 / B2 收敛为 EU-33，完成管理端用户提示、结构身份与实现解释责任治理并集成 |
| 2026-09-05 | Issue #60 / B3 先后形成 EU-34 / EU-35：服务端 Rich Text HTML safety foundation 与 Article/Page shared Tiptap authoring 均完成、集成并通过 Post-Integration CI |
| 2026-09-06 | Issue #60 / D1 的 Public Frontend Replaceability Authority 经 PR #70 / #71 集成并通过 Post-Integration CI #719；`slice-work` 形成 EU-36，Readiness Check PASS，进入 Fresh-context Execute |
| 2026-09-06 | EU-36 完成 Public source isolation、Backend managed Article Resource public projection 与 source-boundary guard；PR #73 exact-head 全链验证、集成及 `main` Post-Integration CI #723 均 PASS，D1 implementation scope 正式关闭 |
| 2026-09-06 | 在进入 Issue #60 / E1～E3 前新增 Issue #77，明确 Generic CMS Core + JilinJobs Site Package + Historical Migration + Replaceable Public Renderer 四层边界为当前 Planning Priority；Repository split / Docs-Code split 保持 deferred |
| 2026-09-06 | Issue #77 / Slice A 形成 EU-37，建立 Site Package v1 manifest/schema、narrow provisioner、Fresh V1 MySQL proof 与 ownership/idempotency contract；PR #80 已集成并通过 Post-Integration CI |
| 2026-09-06 | Issue #77 / Slice B stable-identity portion 形成 EU-38；Column、PageGroup、Page、NavigationLocation、SiteConfig、CmsList definition、AdvertisementSlot 共 58 个 stable preset objects 进入 Site Package；PR #81 exact-head 与 `main` Post-Integration Site Package/Repository CI 全部 PASS |
| 2026-09-06 | Issue #77 / Slice B Navigation identity portion 形成 EU-39；NavigationItem 获得 stable `code`，40 条正式导航进入 Site Package；PR #84 final Head `b95285f…` exact-head Site Package #15、CI #761、Canonical #152、Upgrade #102、Review Environment #678 全部 PASS，并合并为 `main@36276ed65e6f3edbe96ffc18c01cf18ab924837b`；Post-Integration Site Package #16 与 CI #762 全部 PASS，EU-39 正式关闭 |
| 2026-09-06 | Issue #77 / Slice B Runtime composition activation 形成 EU-40；PR #86 final Head `b3e3dc8c…` exact-head Site Package #19、CI #768、Canonical #153、Upgrade #103、Review Environment #683 全部 PASS，并合并为 `main@b105e553db1ebbc12a2b6665385b94fb977bea06`；Post-Integration Site Package #20 与 CI #769 全部 PASS，EU-40 正式关闭 |
| 2026-09-07 | Issue #77 Operational Seed audit 将旧 V2 剩余 6 `CmsListItem` + 1 `Advertisement` 分类为 Fresh Site initial operational defaults；用户明确 Backend Schema migration 与 Site data initialization 必须独立演进；Architecture Amendment 经 `slice-work → readiness-check` 形成 Ready EU-41 |
| 2026-09-07 | EU-41 final Head `a958c39a…` 的 Site Package #32、CI #783、Canonical #165、Upgrade #115、Review #696 全部 PASS；PR #88 合并为 `main@6c88eea1762e8edf465833631cadff1e4c751d36`，Post-Integration Site Package #33 与 CI #784 PASS，Operational Seed / V2 responsibility retirement 正式关闭 |
| 2026-09-07 | EU-42 final Head `37e03c3a…` 的 Site Package #34、CI #787、Review #698 全部 PASS；PR #90 合并为 `main@2c4af15df64342850391bbfe67de99b6404b3280`，Post-Integration Site Package #35 与 CI #788（含 empty-root Integrated Browser）全部 PASS，Slice C 正式关闭 |

## 当前已固化结果

### 主站与公共 CMS

- `www.jilinjobs.cn` 与 `24365.jl.smartedu.cn` 作为同一原网站取证基线；
- 中心主站视觉原则为“现网视觉与布局复刻 + 必要技术适配”，不是现代化改版；
- 原站关键 Logo、Header Banner、轮播图、招聘活动横幅、业务指南 / 快捷入口图标等已纳入版本化初始化静态资源包；
- Header、主导航、Footer、首页主要区域以及栏目 / 详情 / 固定页 / 页面组 / 业务指南已经完成当前阶段视觉收敛；
- `/page/**`、`/column/{alias}`、`/article/{id}` 为 Main canonical URL；
- Article 支持 `INTERNAL` / `EXTERNAL_LINK`；
- 首页通知公告、就业动态、招聘公告使用真实业务 scope，不以全站前 N 条客户端筛选替代业务作用域；
- 集合型数据访问遵循 Data Access Scope & Boundedness Control：scope 先于 window / pagination；固定 `LIMIT/OFFSET`、页面展示数量或客户端过滤不得替代成员资格边界。

### 管理端与前端架构

- `frontend/public-site` 与 `frontend/admin` 为独立前端构建单元，共享 Spring Boot CMS Backend；
- Admin 当前为单一 Vue SPA / Router / Build 的 Modular SPA：`app/`、`shared/`、`modules/cms/` 所有权明确；
- CMS canonical Admin namespace 为 `/admin/cms/**`，旧路径仅兼容重定向；
- Module Federation 不是当前基础设施，只有出现真实独立发布 / 部署 / 跨团队 / 跨技术栈需求后再评估；
- Public Site 在同一工程中持有 Main + Party 两个真实 Entry；Entry 按 Theme / Router Boundary 划分；
- Main Site 持有独立 App / Router / 蓝白主题；Party 使用 `/party/**`、独立 App / Router / 红色主题；
- Main / Party Header / Navigation / Footer 可共享 Shell 结构，但 Site 内容视觉表达保持独立。

### Party historical content

- EU-29 frozen acceptedSnapshot：181 Articles（120 INTERNAL / 61 EXTERNAL_LINK），accepted artifact digest `sha256:230ac0df997b3dc913ed38503a8289eae30d8bb0a455fd858e388ddc27066148`；
- 四条 EU-29 正式内容线：高层声音 `gcsy`、工作动态 `gzdt`、党规党章 `dgdz`、理论学习 `llxx`；
- EU-30 新增并接受 `party-theme-education / 主题教育`（legacy `zhutijiaoyu / 主题教育2023`）2 条历史记录；它属于 Party 内容作用域，但不成为 PartyHome 第五个固定内容区；
- `data-migrations/party/v1/manifest.json` 当前 status = `accepted-canonical`；current canonical Runtime Dataset = 183；EU-29 acceptedSnapshot 181 与 digest 保持原样作为 provenance；
- Party canonical URL 为 `/party/`、`/party/column/{alias}`、`/party/article/{id}`；legacy detail path 只作为迁移输入；
- 历史运营文章、外链、正文资源、legacy identity / fingerprint 继续由独立 canonical migration dataset / importer 承载；Site Package stable structure 与 Generic Schema / Historical Migration 的长期责任已分离；
- 历史内容迁移知识不得因数据库 schema / Site Package 调整而删除；EU-41 final Canonical #165、Upgrade #115 与 Review #696 已证明 canonical migration 只依赖 Generic Schema + stable Site identities，不依赖 Main Fresh operational bootstrap。

### EU-30 accepted architecture

- Article = ownership / classification；List = placement / curation；
- `CmsListItem.sourceType` 使用 `LINK / ARTICLE`；
- Admin 将数据来源明确表达为“链接 / 站内文章 / 外链文章”；
- `Article.articleType`、`CmsListItem.sourceType`、ARTICLE `articleId` 创建后不可普通修改；
- EXTERNAL_LINK Article 通过 ARTICLE placement 跟随 Article 当前标题 / external URL；
- Main / Party 共用 `CAROUSEL_INTERVAL_SECONDS`、`CAROUSEL_MAX_ITEMS` 与共享 lifecycle；视觉主题、比例、Caption、DOM composition 分离；
- 原则：**统一行为规则和生命周期，不统一视觉表达**；
- Party carousel canonical = `[LINK, ARTICLE, LINK, LINK]`；position 2 使用 accepted Article/Resource state；
- migration-only compatibility 保留 EU-29 canonical position 2 在严格 fingerprint / Runtime identity 匹配时 LINK → ARTICLE 原位升级，普通 API 不开放。

### Database migration baseline — EU-31 history / EU-41 current amendment

- EU-31 是在 EU-30 关闭后重新完成上游 Specification / Technical Planning、Slice 与 Readiness 后形成并晋升的 Database Migration Baseline Convergence，不继承历史“EU-31 Browser Compatibility”；
- EU-31 明确当前开发阶段没有必须从旧 transcript 原位升级的生产 / 持久数据库义务，因此允许 development baseline reset；该 upgrade boundary 继续有效；
- EU-31 当时建立 `V1__current_cms_schema.sql` + `V2__current_preset_data.sql`，EU-39 后增加旧 `V3__navigation_stable_identity.sql`；
- Issue #77 / EU-41 已修订 active baseline：Backend Flyway 为 `V1__current_cms_schema.sql` + `V2__site_provisioning_schema_capabilities.sql`，只承担 Generic CMS Schema / provisioning capability；
- JilinJobs stable structure 位于 `sites/jilinjobs/structure/**`；Fresh Site operational defaults 位于 `sites/jilinjobs/bootstrap/**`，无 Flyway migration number；
- 从 EU-41 accepted integration baseline 起，下一次 Generic CMS Schema change 从 Backend V3 继续 append-only；
- `data-migrations/party/v1/**`、legacy mapping、fingerprint、canonical reports 与 importer 不由 Flyway baseline 或 Site bootstrap 接管。

### EU-32～EU-35 管理端治理与富文本能力

- EU-32：`CmsList.groupCode` 继续作为内部结构元数据；ordinary Admin create 使用 `GENERAL`，update 保留已有结构分组，管理界面不再暴露分组输入；
- EU-33：用户可见提示只承担当前操作所需信息；结构身份、实现原因和 Method/Requirement 解释不再作为普通运营提示重复暴露；
- EU-34：Article/Page 共用服务端 HTML safety policy，写入与公开读取边界均执行安全收敛，legacy 内容不批量改写；
- EU-35：Article/Page 共用 CMS-local Tiptap `RichTextEditor`，保留 Article managed Resource association 与 Page 既有边界；
- EU-34 / EU-35 共同保持 `bodyHtml` 为唯一持久 HTML contract，不引入第二正文 Authority。

### EU-36 Public frontend source isolation

- Public production source `frontend/public-site/src/**` 不再包含 `/api/admin/**` endpoint knowledge；
- Article/Column/Page/static-resource 的 Admin CRUD / maintenance responsibility 已从 Public source移除，Admin ownership 保留在独立 Admin frontend；
- Article managed body image 的 Admin→Public Resource URL translation 由 Backend Public projection 完成；
- persisted/Admin `bodyHtml` contract 不变，未关联 Resource、外部 URL 与其他 Admin 文本不做泛化改写；
- Public package build 通过 source-boundary guard 防止 Admin endpoint knowledge 回流；
- EU-36 已完成 exact-head CI、Public/Admin/Integrated Browser、Review Runtime 与 `main` Post-Integration Verification。

### Issue #77 four-layer boundary / EU-37～EU-41 progress

四层长期边界：

```text
Generic CMS Core
        ↓
JilinJobs Site Package / Provisioning
        ↓
Historical Content Migration
        ↓
Runtime CMS Data
        ↓
Replaceable Public Renderer
```

当前 ownership：

```text
backend/src/main/resources/db/migration/**
    Generic CMS Schema-only Flyway lineage

sites/jilinjobs/structure/**
    stable Site Package reconcile Authority

sites/jilinjobs/bootstrap/**
    one-time current-schema Fresh Site operational defaults

data-migrations/**
    historical canonical migration / provenance

frontend/public-site/**
    replaceable accepted renderer implementation
```

- `preset` 保留为 Generic CMS 能力；
- EU-37 建立 Site Package contract / provisioner foundation；
- EU-38 / EU-39 将当前 98 个 stable site objects 纳入 Site Package，并建立 Navigation stable identity；
- EU-40 激活正式 Runtime / importer Site Package composition；
- EU-41 将旧 V2 剩余 6 ListItems + 1 Advertisement 分类为 Fresh Site initial operational defaults，并通过 generic bootstrap-state + no-version Site bootstrap 取代 shared Flyway data seed；
- 普通 Runtime reconcile 与 repeated explicit bootstrap 均不覆盖或复活 operator data；
- `V2__current_preset_data.sql` 已退出 active Backend Flyway；
- stable Site Asset 已由 EU-42 迁入 `sites/jilinjobs/assets/**`，并由 integrity manifest + create-if-missing Runtime projection + protected-path contract 管理；原 `site-baseline/static/**` ownership 已退出；
- final canonical lifecycle / E1～E3 re-entry 仍待 Slice D；
- Repository split、Git Submodule、Docs / Code 分仓与 multi-repo Workspace 暂不实施，待四层 boundary 完成后单独评估。

## 已完成阶段：管理端工程分离与功能收敛

`frontend/public-site` 与 `frontend/admin` 已完成物理拆分；管理端已完成 Modular SPA Boundary 收敛。历史执行追溯见 `docs/work/admin-frontend-convergence-execution-units.md`，长期架构决策见 `docs/architecture/decisions/ADR-0001-admin-frontend-module-integration.md`。

## 已完成阶段：公开站 Multi-entry Modular SPA 与中心党建基础框架

依据 `ADR-0002-public-site-multi-entry-modular-spa.md` 与 `docs/work/public-site-multi-entry-execution-units.md`，EU-23～EU-25 已完成：

```text
EU-23 Public Frontend Authority & Architecture Convergence：已完成
→ EU-24 Main Site Modularization & Page Entry Removal：已完成
→ EU-25 Party Site Entry & Foundation Shell：已完成
```

## 已完成阶段：中心党建正式页面与内容收敛

历史执行记录以 `docs/work/party-convergence-execution-units.md` 为准：

```text
EU-26 Party Evidence & Authority Convergence：已完成
→ EU-27 Party CMS Structure & Content Routing：已完成
→ EU-28 Party Home & Visual Fidelity Convergence：已完成
→ EU-29 Party Historical Content Migration & Final Review：已完成
```

## 已完成阶段：EU-30 Carousel Architecture & Behavior Convergence

最终执行与证据以 `docs/work/eu30-carousel-convergence.md` 为准。EU-30 已完成 Human Review、canonical acceptance、post-promotion verification 与集成。

## 已完成阶段：EU-31 Database Migration Baseline Convergence

EU-31 的历史 Authority：

- `docs/requirements/database-migration-baseline-convergence.md`
- `docs/specifications/database-migration-baseline-convergence.md`
- `docs/technical/database-migration-baseline-convergence.md`
- `docs/work/eu31-database-migration-baseline-convergence.md`

EU-31 本身已经完成；上述文档当前已明确标记 EU-41 对 active baseline shape 的后续修订，避免把 EU-31 当时的 Site-data-in-V2 形状误认为当前长期 Authority。

## 当前阶段：Issue #77 Site Package Boundary — EU-41 COMPLETED

当前 Planning / implementation Authority：

- `docs/project/site-package-planning.md`
- `docs/requirements/cms-site-package-boundary.md`
- `docs/specifications/cms-site-package-boundary.md`
- `docs/technical/cms-site-package-boundary.md`
- `docs/work/eu37-site-package-provisioner-foundation.md`
- `docs/work/eu38-stable-site-structure-package-migration.md`
- `docs/work/eu39-navigation-stable-identity.md`
- `docs/work/eu40-explicit-site-package-runtime-composition.md`
- `docs/work/eu41-site-bootstrap-generic-schema-baseline-separation.md`
- GitHub Issue #77。

已完成：

1. EU-37：Site Package Contract & Provisioner Foundation；
2. EU-38：Stable Site Structure Package Migration；
3. EU-39：Navigation Stable Identity & Site Package Reconcile；
4. EU-40：Explicit Site Package Runtime Composition Activation；
5. EU-41：Site Bootstrap & Generic Schema Baseline Separation。

EU-41 final Head `a958c39a37892cf0fbcb41b8c883b2299d84f561` 已通过 Site Package #32、CI #783、Canonical #165、Upgrade #115、Review #696；PR #88 合并为 `main@6c88eea1762e8edf465833631cadff1e4c751d36`，Post-Integration Site Package #33 与 CI #784 PASS。

当前没有 Ready Execution Unit。

Issue #77 剩余 Planning：

1. Slice D：Canonical Migration Compatibility & E1～E3 Re-entry；
2. 四层 boundary 完成后的 Repository Split Readiness Assessment。

这些候选必须基于最新 `main` 重新执行 current audit / slice-work / readiness-check；不得自动继承 EU-42 Execute Authority。

Issue #60 / E1、E2、E3 在本阶段保持 Planning Candidate，不提前执行。C1 / C2、Issue #59 Browser Compatibility 与 Issue #57 Public Rendering Architecture 继续独立保留，不因 Issue #77 自动扩大范围。

## 阶段切换原则

- 管理端工程分离、Public Multi-entry、Party EU-26～EU-29、EU-30、EU-31、EU-32～EU-42 均已完成各自 accepted scope；
- EU-31 的 development DB recreation boundary 继续有效，但其当时 active migration shape 已由 EU-41 accepted Authority 修订；
- EU-41 已完成 current audit / architecture clarification / slice-work / readiness-check / Execute / exact-head Verification / Integration / Post-Integration Verification；
- EU-42 已完成 Slice C 的 current audit / readiness / Execute / exact-head Verification / Integration / Post-Integration Verification；
- 当前没有 Ready Execution Unit；Issue #77 后续 Slice D 不自动继承 EU-42 Execute Authority；
- Issue #60 / E1～E3 等待 Issue #77 compatibility/re-entry gate；
- 未来候选只有在 `readiness-check` PASS 后才能成为 Ready Execution Unit；
- canonical historical dataset、legacy mapping、fingerprint、Importer、EU-29→EU-30 upgrade knowledge 与证据长期独立保留；
- 后续调整 Main / Party Router / Entry / Gateway / Visual 时，既有 Human Review Evidence 不机械继承，应按 Evidence Claim 重新取得受影响证据；
- 最终 PR 不自动合并，必须满足 Repository Authority 的 Integration Gate。

## Fresh Context 恢复入口

1. `AGENTS.md`
2. `README.md`
3. `docs/project/project-roadmap.md`
4. `docs/project/development-method.md`
5. 当前阶段读取 `docs/project/site-package-planning.md`、`docs/requirements/cms-site-package-boundary.md`、`docs/specifications/cms-site-package-boundary.md`、`docs/technical/cms-site-package-boundary.md`
6. 最近完成 EU 读取 `docs/work/eu41-site-bootstrap-generic-schema-baseline-separation.md`；当前没有 Ready EU
7. `docs/technical/verification-strategy.md`
8. GitHub Issue #77；Issue #60 用于 E1～E3 / C1/C2 等后续候选，Issues #57 / #59 继续保留各自讨论 / 后置候选
9. 当前 Branch / PR / CI / Runtime Evidence

不得使用其他聊天或其他项目状态补充未固化的 Consumer 产品事实。