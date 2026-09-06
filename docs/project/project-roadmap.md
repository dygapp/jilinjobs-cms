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
| EU-31 Database Migration Baseline Convergence | **已完成** | V1～V20 开发期 Flyway transcript 已收敛为当前 schema + preset curated baseline，历史 Party migration knowledge 保留；后续 schema 恢复 append-only 演进 |
| EU-32 List Definition Group Governance | **已完成** | `groupCode` 保持内部结构元数据，ordinary Admin 不再暴露结构分组写权限 |
| EU-33 Admin Guidance & Explanation Responsibility Governance | **已完成** | 管理界面保留操作必要信息，结构身份与实现背景退出普通运营提示 |
| EU-34～EU-35 Rich Text Safety & Shared Authoring | **已完成** | 服务端 HTML safety foundation + Article/Page 共用 Tiptap 富文本编辑能力完成并通过 Post-Integration Verification |
| EU-36 Public Frontend Source Isolation & Managed Resource Projection | **已完成** | Public production source 已移除 Admin endpoint knowledge，managed Article body image 改由 Backend Public projection 暴露 Public Resource URL，source-boundary guard 与全链回归均已闭环 |
| EU-37～EU-38 Site Package Foundation & Stable Structure Migration | **已完成** | Site Package v1 contract / provisioner foundation 已建立；七类具有 stable identity 的 JilinJobs preset structure 已迁入 `sites/jilinjobs/**` 并证明 Fresh V1 + Package 与 Legacy V1+V2 + Package structural equivalence |
| EU-39 Navigation Stable Identity & Site Package Reconcile | **已完成** | NavigationItem 获得 provisioning-only stable `code`；40 条正式导航进入 Site Package；Fresh create、Legacy V2 原位 adoption、stable-code reconcile、PR #84 Integration 与 `main` Post-Integration Verification 全部 PASS |
| EU-40 Explicit Site Package Runtime Composition Activation | **已完成** | `cms.site-package.root` opt-in lifecycle、Repository Runtime、Canonical / Upgrade importer orchestration 与 Review Environment composition 已显式消费 Site Package；V2 compatibility responsibility 保留 |
| Issue #77 CMS Core / Site Package / Public Renderer Boundary | **当前 Planning Priority / 部分完成** | EU-37～EU-40 已完成；剩余 Operational Seed Classification & V2 Responsibility Retirement、Site Asset ownership/runtime composition、Canonical Migration lifecycle compatibility 与 E1～E3 re-entry 仍需重新 slice / readiness |
| Issue #60 E1～E3 Main Site Formal Content | **前置依赖等待** | 保留为 Planning Candidates；待 Issue #77 的 Site Package boundary / compatibility 收敛并取得兼容证据后重新进入 Planning |
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
| 2026-09-05 | EU-31 将 active Flyway V1～V20 收敛为 `V1__current_cms_schema.sql` + `V2__current_preset_data.sql`；Canonical Migration Verification 与 EU-30 Migration Upgrade Verification PASS，证明 Fresh DB、183 篇 current Runtime Dataset、4 条 Party carousel、幂等导入及 EU-29→EU-30 position 2 兼容知识均保持有效；EU-31 已集成到 `main` |
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
| 2026-09-06 | Issue #77 / Slice B Runtime composition activation 形成 EU-40；PR #86 final Head `b3e3dc8c…` exact-head Site Package #19、CI #768、Canonical #153、Upgrade #103、Review Environment #683 全部 PASS，并合并为 `main@b105e553db1ebbc12a2b6665385b94fb977bea06`；Post-Integration Site Package #20 与 CI #769 全部 PASS。Site Package 已进入正式 Runtime/importer composition，但 V2 compatibility responsibility 仍保留，EU-40 正式关闭 |

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
- 历史运营文章、外链、正文资源、legacy identity / fingerprint 继续由独立 canonical migration dataset / importer 承载；EU-37～EU-39 已把 Site Package stable structure（含 NavigationItem identity）与 Generic Schema / Historical Migration 的长期责任分离，但 V2 当前默认 Runtime compatibility responsibility 尚未移除；
- 历史内容迁移知识不得因数据库 schema / Site Package 调整而删除；EU-31 与 EU-39 compatibility verification 均证明 canonical migration 与 schema / Site Package 演进可以独立维护。

### EU-30 accepted architecture

- Article = ownership / classification；List = placement / curation；
- `CmsListItem.sourceType` 使用 `LINK / ARTICLE`；
- Admin 将数据来源明确表达为“链接 / 站内文章 / 外链文章”；
- `Article.articleType`、`CmsListItem.sourceType`、ARTICLE `articleId` 创建后不可普通修改；
- EXTERNAL_LINK Article 通过 ARTICLE placement 跟随 Article 当前标题 / external URL；
- Main / Party 共用 `CAROUSEL_INTERVAL_SECONDS`、`CAROUSEL_MAX_ITEMS` 与共享 lifecycle；视觉主题、比例、Caption、DOM composition 分离；
- 原则：**统一行为规则和生命周期，不统一视觉表达**；
- Party carousel canonical = `[LINK, ARTICLE, LINK, LINK]`；position 2 = Article 183 / Resource 188 / 原 image SHA；
- migration-only compatibility 保留 EU-29 canonical position 2 在严格 fingerprint / Runtime identity 匹配时 LINK → ARTICLE 原位升级，普通 API 不开放。

### EU-31 database migration baseline

- EU-31 是在 EU-30 关闭后重新完成上游 Specification / Technical Planning、Slice 与 Readiness 后形成并晋升的 Database Migration Baseline Convergence，不继承历史“EU-31 Browser Compatibility”；
- 当前开发阶段没有必须从 V1～V20 原位升级的生产 / 持久数据库义务，因此允许 development baseline reset；
- active Flyway baseline 为 curated `V1__current_cms_schema.sql` + `V2__current_preset_data.sql`，后续 schema 采用 append-only migration；EU-39 增加 `V3__navigation_stable_identity.sql` 只扩展 Generic CMS Navigation provisioning identity，不包含 JilinJobs 实例导航数据；
- schema 中长期保留 importer 所需的 `cms_article_legacy_mapping`、`cms_list_item_legacy_mapping`，Fresh DB 初始为空；
- `data-migrations/party/v1/**`、stable identity、legacy mapping、fingerprint、canonical reports 与 importer 不由 Flyway baseline 接管，也未在 EU-31 / EU-39 中重写；
- EU-29→EU-30 position 2 LINK→ARTICLE 只保留为 importer migration-only compatibility；
- V2 继续承担当前默认 Runtime compatibility responsibility；Issue #77 后续若调整其长期责任，必须显式处理 baseline authority 与 Existing DB / canonical compatibility。

### EU-32～EU-35 管理端治理与富文本能力

- EU-32：`CmsList.groupCode` 继续作为内部结构元数据；ordinary Admin create 使用 `GENERAL`，update 保留已有结构分组，管理界面不再暴露分组输入；
- EU-33：用户可见提示只承担当前操作所需信息；结构身份、实现原因和 Method/Requirement 解释不再作为普通运营提示重复暴露；
- EU-34：Article/Page 共用服务端 HTML safety policy，写入与公开读取边界均执行安全收敛，legacy 内容不批量改写；
- EU-35：Article/Page 共用 CMS-local Tiptap `RichTextEditor`，保留 Article managed Resource association 与 Page 既有边界；
- EU-34 / EU-35 共同保持 `bodyHtml` 为唯一持久 HTML contract，不引入第二正文 Authority。

### EU-36 Public frontend source isolation

- Public production source `frontend/public-site/src/**` 不再包含 `/api/admin/**` endpoint knowledge；
- Article/Column/Page/static-resource 的 Admin CRUD / maintenance responsibility 已从 Public source移除，Admin ownership 保留在独立 Admin frontend；
- Article managed body image 的 Admin→Public Resource URL translation 由 Backend `ArticleService.getPublic()` 在 Public projection 中按 `bodyImageResourceIds` 精确完成；
- persisted/Admin `bodyHtml` contract 不变，未关联 Resource、外部 URL 与其他 Admin 文本不做泛化改写；
- Public package build 通过 source-boundary guard 防止 Admin endpoint knowledge 回流；
- EU-36 已完成 exact-head CI、Public/Admin/Integrated Browser、Review Runtime 与 `main` Post-Integration Verification。

### Issue #77 four-layer boundary / EU-37～EU-39 progress

四层长期边界保持：

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

- `preset` 保留为 Generic CMS 能力；
- `data-migrations/**` 继续只承载 historical operational content；
- 当前 Vue/Vite Public Site 继续是 accepted renderer implementation，但不成为 Site / Migration Authority；
- EU-37 已建立 Site Package v1 contract、schema、narrow provisioner、Fresh proof 与 idempotent / ownership conflict contract；
- EU-38 已将 Column、PageGroup、Page、NavigationLocation、SiteConfig、CmsList definition、AdvertisementSlot 七类 stable preset structure 纳入 `sites/jilinjobs/**`；
- EU-39 已为 NavigationItem 增加 provisioning-only nullable stable `code`，40 条正式 JilinJobs NavigationItem 已进入 Site Package；Fresh create、Legacy V2 原位无歧义 adoption、stable-code reconcile、ambiguous adoption rollback 与 operator data non-takeover 均已通过专项验证；
- EU-39 final Head `b95285f…` exact-head Site Package #15、CI #761、Canonical #152、EU-30 Upgrade #102 与 Review Environment #678 全部 PASS；PR #84 已合并为 `main@36276ed65e6f3edbe96ffc18c01cf18ab924837b`，Post-Integration Site Package #16 与 CI #762 全部 PASS；
- V2 当前默认 Runtime compatibility baseline、stable Site Assets 与最终 Runtime/canonical composition 尚未完成 responsibility convergence；
- Repository split、Git Submodule、Docs / Code 分仓与 multi-repo Workspace 暂不实施，待 Site Package boundary / compatibility 形成后单独做 Readiness Assessment。

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

最终执行与证据以 `docs/work/eu30-carousel-convergence.md` 为准。

最终状态：

1. LINK / ARTICLE placement model ACCEPTED；
2. Main / Party shared carousel behavior + separate visual expression ACCEPTED；
3. Article / List ownership-placement boundary ACCEPTED；
4. Party `主题教育` 2 条历史增量 ACCEPTED CANONICAL；
5. Party position 2 ARTICLE migration upgrade ACCEPTED；
6. EU-29 acceptedSnapshot provenance PRESERVED；
7. Human Review #595 PASS；
8. acceptance promotion 后 CI #659 / Canonical #123 / Upgrade #71 PASS；
9. PR #58 已合并到 `main`，EU-30 canonical acceptance / Authority 关闭。

## 已完成阶段：EU-31 Database Migration Baseline Convergence

EU-30 合并后没有机械继承旧预编号路线。Issues #59 / #60 经重新分析后，Database Migration Baseline Convergence 按以下职责链形成并执行：

```text
Intent / Requirement Clarification
→ Ready Specification
→ Technical Planning / migration lineage audit
→ Slice Work forms Candidate EU-31
→ Readiness Check PASS
→ Ready EU-31
→ Execute / Converge / Integration
```

追溯 Authority：

- `docs/requirements/database-migration-baseline-convergence.md`
- `docs/specifications/database-migration-baseline-convergence.md`
- `docs/technical/database-migration-baseline-convergence.md`
- `docs/work/eu31-database-migration-baseline-convergence.md`
- Issue #59 / #60

最终状态：实现与验证均已完成并集成到 `main`；Canonical Migration Verification、EU-30 Migration Upgrade Verification 与 CI 均提供了当前阶段所需证据。该阶段现已关闭，不再作为 Fresh Context 的当前 Execution Unit。

## 当前阶段：Issue #77 Site Package Boundary — EU-40 已完成

在开始 Issue #60 / E1～E3 前，当前仍优先处理 Issue #77，但不得把它恢复成“尚未开始”的 Planning 状态。

Planning / implementation Authority：

- `docs/project/site-package-planning.md`
- `docs/requirements/cms-site-package-boundary.md`
- `docs/specifications/cms-site-package-boundary.md`
- `docs/technical/cms-site-package-boundary.md`
- `docs/work/eu37-site-package-provisioner-foundation.md`
- `docs/work/eu38-stable-site-structure-package-migration.md`
- `docs/work/eu39-navigation-stable-identity.md`

已完成：

1. Slice A → EU-37：Site Package Contract & Provisioner Foundation；
2. Slice B stable-identity portion → EU-38：Stable Site Structure Package Migration；
3. Slice B Navigation identity portion → EU-39：Navigation Stable Identity & Site Package Reconcile；PR #84 Integration 与 `main` Post-Integration Verification 已 PASS。
4. Slice B Runtime composition activation → EU-40：Explicit Site Package Runtime Composition Activation；PR #86 Integration 与 `main` Post-Integration Verification 已 PASS，V2 compatibility responsibility 保留。

当前没有 Ready Execution Unit。

EU-40 后剩余 Planning：

1. Slice B remainder：V2 / default Runtime composition responsibility convergence；
2. Slice C：Site Asset Ownership & Runtime Composition；
3. Slice D：Canonical Migration Compatibility & E1～E3 Re-entry。

Issue #60 / E1、E2、E3 在本阶段保持 Planning Candidate，不提前执行。C1 / C2、Issue #59 Browser Compatibility 与 Issue #57 Public Rendering Architecture 继续独立保留，不因 Issue #77 自动扩大范围。

Repository split、Git Submodule、Docs / Code 分仓、multi-repo Workspace composition 只作为后续独立 Architecture / Method Assessment；Site Package boundary / Runtime / canonical compatibility 形成并取得真实 Evidence 前不实施。

这些剩余候选不是已批准的 Implementation Scope，也不因 Roadmap 顺序、Issue 编号或未来可能使用的 `EU-xx` 名称获得 Execution Unit 身份。进入实现前仍必须完成：

```text
Planning / Requirement Candidate
→ Intent / Requirement Clarification（按需）
→ Ready Specification / existing Authority re-check
→ 必要的 Technical Planning / Research（完成或确认不需要）
→ Slice Work forms Candidate Execution Unit(s)，可分配稳定 Identifier
→ Readiness Check
→ Ready Execution Unit
→ Execute
```

## 阶段切换原则

- 管理端工程分离、通用 CMS、Admin Modular SPA 阶段已关闭；
- Public Multi-entry Modular SPA 与 Party Foundation 阶段已关闭；
- Party EU-26～EU-29 已关闭；
- EU-30 已完成 Human Review、canonical acceptance、post-promotion verification 并合并；
- EU-31 Database Migration Baseline Convergence 已完成并集成，不是历史预编号 Browser Compatibility；
- EU-32～EU-35 已按 Issue #60 / B1～B3 的实际 Requirement / Specification / Slice / Readiness 链完成并集成；
- EU-36 已按 Issue #60 / D1 的 Requirement / Specification / Technical Plan / Slice / Readiness 链完成 Public source isolation 与 managed resource projection，并通过 Post-Integration Verification；
- EU-37 / EU-38 已按 Issue #77 的 Planning Authority、current audit、Slice / Readiness 链完成 Site Package Foundation 与 stable structure migration，并通过 Post-Integration Verification；
- EU-39 已按 Issue #77 Slice B remainder current audit、Requirement/Specification decision、Slice / Readiness 链完成 Navigation stable identity / Site Package reconcile，PR #84 与 `main` Post-Integration Verification 均 PASS，状态为 COMPLETED；
- EU-40 已在重新 current audit / slice / readiness 后完成 explicit Site Package Runtime composition activation，PR #86 与 `main` Post-Integration Verification 均 PASS，状态为 COMPLETED；
- 当前没有 Ready Execution Unit；Issue #77 剩余项不得自动继承 EU-40 Execute 授权；
- Issue #60 / E1～E3 等待 Issue #77 的 Site Package boundary / compatibility 收敛后重新进入 Planning；
- 未来候选可以在 `slice-work` 形成 Candidate Execution Unit 时获得稳定 Identifier，但只有 `readiness-check` PASS 后才能成为 Ready Execution Unit；
- 若未来 Browser Compatibility 候选被正式切分，必须基于届时 current implementation 重新取得兼容证据，不继承 EU-30 的旧 DOM / CSS / dependency 假设；
- EU-31 accepted Flyway baseline 继续有效；EU-39 没有删除或重写 `V2__current_preset_data.sql`，未来若调整其长期责任必须显式处理 baseline authority、Existing DB compatibility 与 canonical compatibility；
- canonical historical dataset、legacy mapping、fingerprint、Importer、EU-29→EU-30 upgrade knowledge 与证据长期独立保留；
- 后续调整 Main / Party Router / Entry / Gateway / Visual 时，既有 Human Review Evidence 不机械继承，应按 Evidence Claim 重新取得受影响证据；
- 最终 PR 不自动合并，必须满足 Repository Authority 的 Integration Gate。

## Fresh Context 恢复入口

1. `AGENTS.md`
2. `README.md`
3. `docs/project/project-roadmap.md`
4. `docs/project/development-method.md`
5. 当前阶段优先读取 `docs/project/site-package-planning.md`、`docs/requirements/cms-site-package-boundary.md`、`docs/specifications/cms-site-package-boundary.md`、`docs/technical/cms-site-package-boundary.md`
6. `docs/technical/verification-strategy.md`
7. GitHub Issue #77；Issue #60 用于 E1～E3 / C1/C2 等后续候选，Issues #57 / #59 继续保留各自讨论 / 后置候选
8. EU-37 / EU-38 / EU-39 追溯分别读取 `docs/work/eu37-site-package-provisioner-foundation.md`、`docs/work/eu38-stable-site-structure-package-migration.md`、`docs/work/eu39-navigation-stable-identity.md`；其他已完成 EU 只在相关工作需要时读取
9. 当前 Branch / PR / CI / Runtime Evidence

不得使用其他聊天或其他项目状态补充未固化的 Consumer 产品事实。
