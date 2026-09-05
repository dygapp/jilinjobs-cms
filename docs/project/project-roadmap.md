# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap。

## 方法基线

```text
dygapp/agentic-dev
Validation Baseline: master@394d1c3cde04b35940d5e33b7cbcaaf6557678ce
Capability Milestone: baseline-2026-09-04-engineering-capability@5be2e6aad29b2be6b8535b3690daf3533ee22a46
```

普通开发优先使用 Consumer-local：`AGENTS.md`、`docs/project/development-method.md`、当前 Requirement / Specification / Technical Plan。当前 Validation Baseline 相对正式 Capability Milestone 只增加 Stable Maintenance；本 Consumer 已选择性固化 Durable Evidence Promotion 与长生命周期 Review Environment owner / lease / stale-run 规则，没有新增 Method Stage、Engineering Discipline、Technology Profile 或 Task-oriented Skill。`agentic-dev` 自身 Project Roadmap、Foundation / Engineering Discipline Expansion 状态、PR / Issue / Experiment 事实不进入本 Roadmap。

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
| EU-30 Carousel Architecture & Behavior Convergence | **已完成** | LINK / ARTICLE 投放模型、统一轮播配置与生命周期、Article/List ownership-placement boundary、主题教育历史增量、position 2 ARTICLE 升级、最终 Human Review 与 accepted-canonical promotion 全部完成 |
| EU-30 后续 Planning / Requirement Candidates | **当前规划层** | GitHub Issues #59 / #60；尚未切分 Execution Unit，不预分配 EU 编号 |
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
| 2026-09-05 | EU-30 acceptance promotion 后 CI #659、Canonical #123、EU-29→EU-30 Upgrade #71 全部 PASS；EU-30 关闭，PR #58 进入 Ready for Review |
| 2026-09-05 | 撤销原“EU-31 Browser Compatibility”预编号路线；数据库迁移 baseline、浏览器兼容、列表/资源治理与剩余公开站工作统一回到 Issues #59 / #60 的未编号 Planning / Requirement Candidates |

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
- Flyway 只负责站点结构；历史运营文章、外链、正文资源、legacy identity / fingerprint 继续由独立 canonical migration dataset / importer 承载；
- 历史内容迁移知识不得因后续数据库 schema baseline squash 而删除。

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

当前执行与最终证据以 `docs/work/eu30-carousel-convergence.md` 为准。

最终状态：

1. LINK / ARTICLE placement model ACCEPTED；
2. Main / Party shared carousel behavior + separate visual expression ACCEPTED；
3. Article / List ownership-placement boundary ACCEPTED；
4. Party `主题教育` 2 条历史增量 ACCEPTED CANONICAL；
5. Party position 2 ARTICLE migration upgrade ACCEPTED；
6. EU-29 acceptedSnapshot provenance PRESERVED；
7. Human Review #595 PASS；
8. acceptance promotion 后 CI #659 / Canonical #123 / Upgrade #71 PASS；
9. PR #58 已 Ready for Review；最终合并仍需项目负责人明确指令。

## 当前阶段：未编号 Planning / Requirement Candidates

EU-30 已关闭。**当前没有新的编号 Execution Unit。**

后续候选统一由以下 GitHub Issues 持久化：

- Issue #59：数据库迁移 baseline convergence 与 Browser Compatibility / Runtime Guard 等后置规划；
- Issue #60：EU-30 后续需求边界候选池，包括列表 / Resource 管理体验、正式公开内容、移动端复核、前端可替换性、主站历史内容与其他跨切面事项。

原 `docs/work/frontend-follow-up-execution-units.md` 中“EU-30 → EU-31 Browser Compatibility”的预编号路线已经被当前 Method / Issues #59 / #60 取代；该文件只保留历史规划追溯价值，不再决定后续 EU 编号或执行顺序。

任何候选进入实现前必须按 Consumer-local Method 完成：

```text
Intent / Requirement Clarification
→ Specification
→ 必要的 Technical Planning / Research
→ Slice Work
→ Readiness Check
→ 分配新的 Execution Unit 编号
```

不得因为候选已存在于 Roadmap、Issue、旧 Execution Plan 或聊天记录中，就直接把它当作下一个 EU。

## 后续公开站点内容与集成候选

当前候选范围包括但不限于：

1. 数据库开发期 Flyway baseline convergence / reset；
2. Public / Admin Browser Compatibility & Runtime Guard；
3. 通用 CMS Resource 浏览 / 复用能力与列表模型治理；
4. 关于我们、预决算公开、就业创业师资库、联系我们、常见问题、业务指南等正式内容、图片、附件与必要专用布局；
5. 招聘信息、直播课程等嵌入型内容的真实加载方案；
6. 网站导航 / 友情链接预设内容；
7. 招聘公告等外部聚合内容抓取 / 同步 / 去重 / 更新 / 失效策略；
8. Main historical content、附件、图片与旧 URL 迁移；
9. 正式内容进入 Runtime 后的桌面、移动端与主要浏览器回归。

这些都是 Planning / Requirement Candidates，不是已批准的 Implementation Scope。

## 阶段切换原则

- 管理端工程分离、通用 CMS、Admin Modular SPA 阶段已关闭；
- Public Multi-entry Modular SPA 与 Party Foundation 阶段已关闭；
- Party EU-26～EU-29 已关闭；
- EU-30 已完成最终 Human Review、canonical acceptance 与 post-promotion verification，执行阶段关闭；
- EU-30 后不自动切换到原预编号“EU-31”；当前回到未编号 Planning / Requirement Candidates；
- 只有完成 Intent / Specification / Slice / Readiness 的候选才可生成新的 Execution Unit；
- 若未来 Browser Compatibility 候选被正式切分，必须基于届时 current implementation 重新取得兼容证据，不继承 EU-30 的旧 DOM / CSS / dependency 假设；
- 若未来执行数据库 migration baseline convergence，schema history 可按批准方案收敛，但 canonical historical dataset、legacy mapping、fingerprint、Importer、EU-29→EU-30 upgrade knowledge 与证据必须保留；
- 后续调整 Main / Party Router / Entry / Gateway / Visual 时，既有 Human Review Evidence 不机械继承，应按 Evidence Claim 重新取得受影响证据；
- 最终 PR 不自动合并。

## Fresh Context 恢复入口

1. `AGENTS.md`
2. `README.md`
3. `docs/project/project-roadmap.md`
4. `docs/project/development-method.md`
5. 当前 Requirement / Specification / Technical Plan
6. 若追溯 EU-30：`docs/requirements/information-publishing-eu30-amendment.md`
7. 若追溯 EU-30：`docs/technical/carousel-list-placement.md`
8. 若追溯 EU-30：`docs/work/eu30-carousel-convergence.md`
9. `docs/technical/verification-strategy.md`
10. GitHub Issues #59 / #60（当前未编号 Planning / Requirement Candidates）
11. 当前 Branch / PR / CI / Runtime Evidence

不得使用其他聊天或其他项目状态补充未固化的 Consumer 产品事实。