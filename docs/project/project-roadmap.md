# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap。它只维护 **durable milestones、长期边界与 Planning directions**；不缓存 `Current Ready Execution Unit`、Readiness `PENDING/PASS`、exact implementation Head 或最近 Actions 等高频 Execute Gate。Current Execution Lifecycle 统一从 `docs/work/current/README.md` 恢复，其 Entry / Exit / fail-closed 契约见 `docs/work/README.md`；GitHub PR / Branch / Actions 继续承担各自原生瞬时事实与 Current Evidence。

详细 Execution Unit、Verification 与阶段追溯由 Current / archived Work artifact、GitHub Issue / PR / Actions Current Evidence 承载。本 Roadmap 中的已完成结果属于 durable milestone / traceability，不因为出现 EU 编号、历史 baseline 或 PASS 记录就重新授予 Execute Authority。

## 方法基线

```text
dygapp/agentic-dev
Previous Evaluated Baseline: d9fad0da83dbdb61cac5eb9778b0258c6861eef1
Current Evaluated Baseline: 2fe193035c629f6b8805fd473bd322f70fe6e172
Capability Milestone: baseline-2026-09-04-engineering-capability@5be2e6aad29b2be6b8535b3690daf3533ee22a46
```

Evaluated Baseline 只记录本 Consumer 最近完成 exact compare / adoption verification 的 upstream frontier，不表示对应 upstream Project state、Research / Eval、self-adoption instance 或全部资源被 Consumer 采用。当前长期资产的真实 owner / provenance 继续由 Consumer-local Authority 单点承担；V3-08 Track B 的 upgrade-only disposition / validation history 见 `docs/project/agentic-dev-v3-08-track-b-evidence.md`，普通 Fresh Context 不默认读取该 evidence。

普通开发优先从 Consumer-local `AGENTS.md`、根 `README.md` 到达 `docs/README.md` Local Discovery Entry，再按 state-only / routing-only / execution 选择最小必要 owner。普通运行不自动访问 `agentic-dev` upstream；本地 locator / owner / supersede / ambiguity 失败时先 local fail closed。Planning / Requirement Candidate 在 `slice-work` 前不具有 Execution Unit 身份；只有 `slice-work` 形成 Candidate Execution Unit 且 `readiness-check` PASS 后才允许进入 Execute。Roadmap 顺序、Issue 编号、EU 名称或模型评审结果都不能替代 Readiness。

## 总体路线

| 路线 | 状态 | Durable result / Planning boundary |
|---|---|---|
| Consumer Bootstrap ～ EU-36 | 已完成 | 通用 CMS、Admin/Public frontend、Party 正式页面与 historical migration、Rich Text、Public source isolation 已建立 |
| EU-37～EU-42 Site Package Boundary | 已完成 | stable structure、Navigation identity、Runtime composition、one-time bootstrap、Generic Schema separation、stable assets 已闭环 |
| Issue #92 Phase 0～Phase 3 | **已完成 / CLOSED** | EU-43～EU-48 与 Phase 3 compatibility closure 完成；E1～E3 re-entry = PASS |
| Issue #60 / E1 Main External-link Boundary | **Planning / Authority closure** | Requirement / Specification READY；current implementation audit 未发现独立 implementation gap；`slice-work = NO CANDIDATE EXECUTION UNIT` |
| Issue #60 / E2 Main Single-page Formal Content foundation | **COMPLETED** | EU-49 已关闭 Page operational-content ownership gap，并建立 site-neutral Generic Page canonical migration foundation |
| Issue #60 / E3 / EU-50 Main Source Discovery & Accepted Snapshot Promotion | **COMPLETED** | accepted current Article subset 已 repository-owned promotion 并集成；Execute Authority terminated |
| Issue #60 / E3 / EU-51 Main Canonical Import, Runtime Reconciliation & Human Review | **COMPLETED** | 3078 accepted Articles 已完成 Fresh Runtime import / reconciliation / idempotency、Public/Admin/Integrated Browser 与 bounded Human Review；Execute Authority terminated |
| EU-52 Main Page Formal Content Package Adoption | **COMPLETED** | 10 formal Pages + Page-owned assets + guarded Existing-Site adoption 已完成并通过 Post-Integration closure；Execute Authority terminated |
| EU-53 Main ListItem Bootstrap Completion | **COMPLETED** | Main `HOME_CAROUSEL=1` 与 96 条已审核 SITE_LINKS 已通过现有一次性 bootstrap SQL 固化并完成 Post-Integration closure；Execute Authority terminated |
| EU-54 Rich Text V2 Mature Editor Adoption | **COMPLETED** | Article `INTERNAL` / Page `RICH_TEXT` 已采用 `suneditor@3.3.3` thin adapter；P1/P2 compatibility、HTML safety、Review Environment、bounded Windows + WPS Human Review 与 Post-Integration closure 已完成；Execute Authority terminated |
| agentic-dev V3-08 Track B baseline adoption | **durable method upgrade** | evaluated upstream frontier 从 `d9fad0da...` 推进到 `2fe19303...`；采用 Consumer Lifecycle / Resource Model / Local Discovery / Skill identity 语义，现有 Consumer owner 保留，Reviewed Discovery Map / Runtime View 不建立；不授予产品 Execute Authority |
| Deferred problem Article review | deferred | 230 篇 problem Article 与 6 篇 source-defect Article 保持独立 evidence / client-review backlog，不阻断其他路线 |
| Repository Split Readiness Assessment | deferred | 四层 boundary 已闭环，但 Assessment 仍独立后置 |
| Issues #57 / #59 / #60 / #137 其他候选 | 规划层保留 | Public Rendering、Browser Compatibility、Loading / Mobile Review、Page Content Architecture 等保持独立 Planning / Review 边界 |

本表只声明 durable milestone / Planning boundary。**当前是否存在 Ready / active Execution Unit 不在本表判断**；Fresh Context 必须读取 `docs/work/current/README.md` 并协调当前 Open execution PR / branch 与必要 Readiness / Integration Current Evidence。

## 当前已接受长期边界

### 1. Generic CMS Core

- Generic CMS Schema、site-neutral domain / validation / persistence / API-supporting capability、provisioning capability 属于 Core；
- Backend Flyway 只承担 Generic CMS Schema evolution，不内建 JilinJobs Site instance rows；
- `preset`、stable identity、bootstrap-state、resource / article / page / list 等可以是 Generic capability，具体 Site identity / instance data 不属于 Core；
- shared build owner 为 `backend/modules/cms-core`；`cms-server` 与 `content-migration` 只单向依赖 Core，Core 不依赖任一 app；
- accepted active Generic Flyway lineage 现为 V1/V2/V3；后续 schema evolution继续 append-only。

### 2. JilinJobs Site Package

```text
sites/jilinjobs/
├── structure/**
├── bootstrap/**
└── assets/**
```

- stable structure 与 operator-managed Runtime Data 分离；
- one-time bootstrap 不 overwrite / resurrect 后续 operator changes；
- stable assets 使用 manifest integrity + create-if-missing projection + protected-path；
- `/static/uploads/**` 与 historical canonical assets 不属于 stable Site asset ownership；
- Page stable identity / required structure由 Site Package持有；EU-49 已完成 ownership transfer，使 ordinary reconcile 不再覆盖 existing Page 的 operator-managed `bodyHtml / renderMode / embedUrl`；
- EU-52 已实现并验证 accepted Main Page formal body 作为 package create-time default、accepted stable Page resource作为 package asset；Existing Site只在 exact prior-package baseline match 时显式一次性 adoption，operator divergence保留并报告；
- EU-53 已完成 Main ListItem 初始数据固化：Main `HOME_CAROUSEL` 以及 EU-50 已审核的 `SITE_RELATED` / `SITE_REGIONAL_GRADUATES` / `SITE_JILIN_UNIVERSITIES` 均由 Site Package one-time bootstrap SQL 初始化；bootstrap 后按普通 operator-managed Runtime data 生命周期运行，不引入新的 ListItem stable identity/reconcile；
- Party ListItem / `PARTY_CAROUSEL` 继续由 Party migration/current Party Authority 管理，不进入 Main bootstrap。

### 3. Historical Content Migration

- `data-migrations/**` 承载 canonical historical data、resource、legacy identity / fingerprint、provenance 与 compatibility；
- Historical Migration 不等于 Flyway，也不等于 Site Package structure/bootstrap/assets；
- Canonical Dataset 依赖 stable Site identity，不依赖临时 Runtime DB id 或 Public Renderer internals；
- Historical Migration implementation 位于独立 `backend/apps/content-migration` non-web application，只依赖 `cms-core`；
- Generic Content Migration 已提供 site-neutral Article / ListItem / Page load、preflight、stable mapping、guarded apply、idempotency/conflict/report foundation；Main-specific source facts / policy不得进入 Generic capability；
- Legacy Source只允许出现在显式 Collect / Discovery 边界；promoted canonical verification与Runtime import必须可离线执行；
- Main current accepted Article subset 已 repository-owned 固化到 `data-migrations/main/v1/**`；EU-51 已证明该 subset 可通过现有 Generic capability 在 Fresh Runtime 确定性导入和幂等重放；deferred/problem Article evidence 与 current import input继续分离；
- Main Page current product delivery不使用 Historical Migration fallback；Generic Page migration capability继续作为通用能力与兼容性事实存在；
- Main ListItem current product delivery使用 Site Package bootstrap SQL，不作为 Main Historical Article Migration 输入；Party ListItem migration保持既有 Party Authority；
- Main historical migration execution保持 **FROZEN / explicit reactivation only**；Party migration 不在冻结范围内。

### 4. Replaceable Public Renderer

- `frontend/public-site` 是稳定 Public API / URL / Site Data Contract 的当前 Vue/Vite consumer；
- Public production source 不依赖 Admin endpoint；
- Renderer 不成为 Site Definition、Canonical Migration、Flyway 或 CMS Domain Authority；
- Main / Party canonical URL、产品 identity 与 accepted behavior 在后续内容建设中保持。

## Issue #60 — Main Site Remaining Product Content durable trace

以下 baseline / result 只承担已完成工作的 durable traceability，不是 Current Execute Gate：

- Initial E1～E3 planning baseline：`main@f42bacf4ab7719e3291288c77f0685b428b86141`；
- E3 Planning/Readiness baseline：`main@e6fe7674398ad8c29fa7ff1d62eb500754a66cc8`；
- EU-50 integrated accepted snapshot baseline：`main@05dfa604ccde45c8409cf6a456e4f201534dc602`；
- EU-51 downstream Readiness baseline：`main@fd192460cb481645c1f1af435cbe5451145797d9`；
- EU-51 Execute baseline：`main@04090a3cc8dd9c85112488f036c4bc1a67003594`；
- EU-51 implementation integrated main：`main@fad4bfc17b762ec9612cf1e44a1c0af67e74a307`；
- EU-52 Planning/Readiness baseline：`main@25e452ee3ce66c2d7da1000ad55e9570d732528a`；
- EU-53 Planning/Readiness integrated main：`main@26a772928278754f478b38742423fc7b71b1f439`；
- EU-53 implementation integrated main：`main@b1130b110bccdb1565c340a6cce62504ec06a87a`。

### E1 — Main External-link Ownership & Behavior Boundary

Current Authority：

- `docs/requirements/main-external-link-boundary.md`
- `docs/specifications/main-external-link-boundary.md`

Repository audit 已确认 EXTERNAL_LINK Article、Navigation、CmsList LINK / ARTICLE、Advertisement / fixed external integration 与 Public Renderer 的 document navigation / open-mode 基础能力已经存在。当前没有独立 Runtime implementation gap，因此 E1 以 Planning / Authority closure结束，不创建 implementation EU，也不向后续工作传递 Execute Authority。

### E2 — Main Single-page Formal Content

Current accepted contract：

- `docs/requirements/main-single-page-formal-content.md`
- `docs/specifications/main-single-page-formal-content.md`
- `docs/technical/main-single-page-formal-content.md`
- EU-52 Completed Work Evidence：`docs/work/archive/eu52-main-page-formal-content-package-adoption.md`
- EU-49 completed Work Evidence：`docs/work/archive/eu49-page-content-migration-foundation.md`

EU-49 已完成 Page operational-content ownership、Generic Page canonical migration foundation 与 append-only V3 mapping。EU-50 source handoff 与 Issue #77 Human Authority随后解决了 Main Page 当前产品交付所需的 source/resource 事实；EU-52 随后完成 accepted Main Page formal content 的 Site Package delivery boundary。各 completed Unit 的实现 / 验证细节由 archived Work Evidence 与 GitHub traceability 承载，不在本 Roadmap 作为 Current Gate 重复维护。

### E3 — Main Historical Content Collection & Canonical Migration

Current Authority：

- `docs/requirements/main-historical-content-migration.md`
- `docs/specifications/main-historical-content-migration.md`
- `docs/technical/main-historical-content-migration.md`

EU-50 / EU-51 已完成 accepted Main Article snapshot promotion、Fresh Runtime import / reconciliation / idempotency、Public/Admin/Integrated Browser 与 bounded Human Review；completed Work Evidence 分别位于：

- `docs/work/archive/eu50-main-source-discovery-promotion.md`
- `docs/work/archive/eu51-main-canonical-import-runtime-review.md`

Accepted durable facts：3314 Article candidates闭合为 `3078 current import eligible + 6 source defect excluded + 230 deferred problem`；3078 accepted Articles = 1577 INTERNAL + 1501 EXTERNAL_LINK；accepted resources = 2603 files / 450,273,166 bytes；canonical root = `data-migrations/main/v1/**`。6 source-defect 与 230 deferred problem 不进入 current canonical/import input。

### EU-52 — Main Page Formal Content Package Adoption — COMPLETED

EU-52 completed the Page-only Site Package delivery boundary：10 accepted Main formal Pages作为 package create-time defaults，accepted Page-owned assets进入 Site Package；Existing Site adoption受 exact prior-package baseline保护，operator divergence保留并报告；Execute Authority已终止。Completed Work Evidence：`docs/work/archive/eu52-main-page-formal-content-package-adoption.md`。

### EU-53 — Main ListItem Bootstrap Completion — COMPLETED

EU-53 completed the remaining Main ListItem initialization gap using the existing Site Package one-time bootstrap mechanism：Main `HOME_CAROUSEL = 1`、`SITE_RELATED = 5`、`SITE_REGIONAL_GRADUATES = 31`、`SITE_JILIN_UNIVERSITIES = 60`；Party ListItems / `PARTY_CAROUSEL`保持在 Party migration/current Party Authority；不引入 Generic schema、Flyway、stable ListItem identity或runtime reconcile扩展。Execute Authority已终止。Completed Work Evidence：`docs/work/archive/eu53-main-listitem-bootstrap-completion.md`。

### Deferred Article / client-confirmation backlog

- 230 篇 problem Article继续作为 durable deferred evidence；
- 6 篇 source-defect Article继续等待客户确认；
- 二者均不属于已完成 EU-51 import input，也不属于 EU-52 / EU-53；
- 后续只能通过新的显式 Planning / Review Authority处理，不得静默修复、猜测、删除或自动导入。

## Current Execution locator 与 Planning directions

本 Roadmap **不拥有 Current Execute Gate**。Fresh Context 必须读取 `docs/work/current/README.md`，并协调当前任务相关的 Open execution PR / branch 与必要 Readiness / Integration Current Evidence。若 locator、active artifact 或 GitHub Current Evidence缺失、冲突或歧义，按 `docs/work/README.md` fail closed；不得从本 Roadmap 的候选顺序、历史 EU 编号或 completed milestone推导 Execute Authority。

长期开放方向包括：

- Issue #77：Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层长期 Architecture Authority；
- Issue #57：Public Rendering Architecture future discussion；
- Issue #59：Browser Compatibility & Runtime Guard 后置候选；
- Issue #60 C1：Loading / Skeleton Experience Planning Candidate；
- Issue #60 C2：Mobile Layout Human Review Candidate；
- Issue #137：Page Content Architecture & Special Page Rendering planning capture；
- Deferred Article / source-defect review：独立 later-review / customer-confirmation candidate；
- Hui Employment iframe：独立 Main integration candidate，需从 current Requirement / Specification Authority 重新恢复并完成 Planning / Readiness；
- Repository Split Readiness Assessment：deferred / independent。

上述候选均不从任何已完成 Unit继承 Execute Authority，也不会仅因列在 Roadmap 中而自动成为下一 Ready Execution Unit。

## 历史收敛追溯

- Issue #92 Phase 0～Phase 3：`docs/project/pre-e1e3-convergence-plan.md` + closed Issue #92；
- Phase 1 Documentation Authority：EU-43 / EU-44 / EU-45 archive records；
- Backend / Migration foundation：EU-46 / EU-47 / EU-48 archive records；
- E2 Page migration foundation：EU-49 archive record；
- E3 source discovery / accepted Article snapshot：EU-50 archive record + `data-migrations/main/v1/**`；
- E3 Runtime import / reconciliation / Human Review：EU-51 archive record；
- Main Page Site Package delivery：EU-52 completed Work Evidence；
- Main ListItem Site Package bootstrap：EU-53 completed Work Evidence；
- Rich Text V2 mature editor adoption：`docs/work/archive/eu54-rich-text-v2-mature-editor-adoption.md`；
- Party canonical migration：对应 `data-migrations/party/**`、PR / Actions / Issue Current Evidence；
- 详细 historical implementation / verification 不在本 Roadmap 重复维护。