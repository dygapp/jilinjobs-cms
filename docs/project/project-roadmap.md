# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap。它只维护当前演进状态、长期边界与下一 Gate；详细 Execution Unit、Verification 与阶段追溯由 Current Work artifact、`docs/work/archive/**`、GitHub Issue / PR / Actions Current Evidence 承载。

## 方法基线

```text
dygapp/agentic-dev
Validation Baseline: master@d9fad0da83dbdb61cac5eb9778b0258c6861eef1
Capability Milestone: baseline-2026-09-04-engineering-capability@5be2e6aad29b2be6b8535b3690daf3533ee22a46
```

普通开发优先读取 Consumer-local `AGENTS.md`、`README.md`、`docs/README.md`、`docs/project/development-method.md`、本 Roadmap 与当前 Requirement / Specification / Technical Plan。Planning / Requirement Candidate 在 `slice-work` 前不具有 Execution Unit 身份；只有 `slice-work` 形成 Candidate Execution Unit 且 `readiness-check` PASS 后才允许进入 Execute。Roadmap 顺序、Issue 编号、EU 名称或模型评审结果都不能替代 Readiness。

## 总体路线

| 路线 | 状态 | 当前结果 / Gate |
|---|---|---|
| Consumer Bootstrap ～ EU-36 | 已完成 | 通用 CMS、Admin/Public frontend、Party 正式页面与 historical migration、Rich Text、Public source isolation 已建立 |
| EU-37～EU-42 Site Package Boundary | 已完成 | stable structure、Navigation identity、Runtime composition、one-time bootstrap、Generic Schema separation、stable assets 已闭环 |
| Issue #92 Phase 0～Phase 3 | **已完成 / CLOSED** | EU-43～EU-48 与 Phase 3 compatibility closure 完成；E1～E3 re-entry = PASS |
| Issue #60 / E1 Main External-link Boundary | **Planning / Authority closure** | Requirement / Specification READY；current implementation audit 未发现独立 implementation gap；`slice-work = NO CANDIDATE EXECUTION UNIT` |
| Issue #60 / E2 Main Single-page Formal Content foundation | **COMPLETED** | EU-49 已关闭 Page operational-content ownership gap，并建立 site-neutral Generic Page canonical migration foundation |
| Issue #60 / E3 / EU-50 Main Source Discovery & Accepted Snapshot Promotion | **COMPLETED** | accepted current Article subset 已 repository-owned promotion 并集成；Execute Authority terminated |
| Issue #60 / E3 / EU-51 Main Canonical Import, Runtime Reconciliation & Human Review | **COMPLETED** | 3078 accepted Articles 已完成 Fresh Runtime import / reconciliation / idempotency、Public/Admin/Integrated Browser 与 bounded Human Review；Execute Authority terminated |
| EU-52 Main Page Formal Content Package Adoption | **COMPLETED** | 10 formal Pages + Page-owned assets + guarded Existing-Site adoption 已通过 PR #125、bounded Human Review 与 Post-Integration verification；Execute Authority terminated |
| stable Main ListItem Site Package follow-up | **Planning Candidate** | stable identity / package representation / adoption / reconcile / operator mutation / upgrade semantics仍需独立 Planning；无 Identifier |
| Deferred problem Article review | deferred | 230 篇 problem Article 与 6 篇 source-defect Article 保持独立 evidence / client-review backlog，不阻断当前路线 |
| Repository Split Readiness Assessment | deferred | 四层 boundary 已闭环，但 Assessment 仍独立后置 |
| Issues #57 / #59 / #60 其他候选 | 规划层保留 | C1/C2、Browser Compatibility、Public Rendering Architecture 等保持独立 |

Current Ready Execution Unit：**NONE**。

EU-52 已完成并终止 Execute Authority。下一 Gate 是新的 Fresh Context Planning/Readiness decision；任何后续候选必须独立形成 Readiness，不能继承 EU-52 Execute Authority。

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
- stable Main ListItem membership 仍属于独立 Site Package Planning Candidate，不属于 EU-52 或 Historical Migration fallback。

### 3. Historical Content Migration

- `data-migrations/**` 承载 canonical historical data、resource、legacy identity / fingerprint、provenance 与 compatibility；
- Historical Migration 不等于 Flyway，也不等于 Site Package structure/bootstrap/assets；
- Canonical Dataset 依赖 stable Site identity，不依赖临时 Runtime DB id 或 Public Renderer internals；
- Historical Migration implementation 位于独立 `backend/apps/content-migration` non-web application，只依赖 `cms-core`；
- Generic Content Migration 已提供 site-neutral Article / ListItem / Page load、preflight、stable mapping、guarded apply、idempotency/conflict/report foundation；Main-specific source facts / policy不得进入 Generic capability；
- Legacy Source只允许出现在显式 Collect / Discovery 边界；promoted canonical verification与Runtime import必须可离线执行；
- Main current accepted Article subset 已 repository-owned 固化到 `data-migrations/main/v1/**`；EU-51 已证明该 subset 可通过现有 Generic capability 在 Fresh Runtime 确定性导入和幂等重放；deferred/problem Article evidence 与 current import input继续分离；
- Main Page current product delivery不使用 Historical Migration fallback；Generic Page migration capability继续作为通用能力与兼容性事实存在。

### 4. Replaceable Public Renderer

- `frontend/public-site` 是稳定 Public API / URL / Site Data Contract 的当前 Vue/Vite consumer；
- Public production source 不依赖 Admin endpoint；
- Renderer 不成为 Site Definition、Canonical Migration、Flyway 或 CMS Domain Authority；
- Main / Party canonical URL、产品 identity 与 accepted behavior 在后续内容建设中保持。

## Issue #60 — Main Site Remaining Product Content

Initial E1～E3 planning baseline：`main@f42bacf4ab7719e3291288c77f0685b428b86141`。

E3 Planning/Readiness baseline：`main@e6fe7674398ad8c29fa7ff1d62eb500754a66cc8`。

EU-50 integrated accepted snapshot baseline：`main@05dfa604ccde45c8409cf6a456e4f201534dc602`。

EU-51 downstream Readiness baseline：`main@fd192460cb481645c1f1af435cbe5451145797d9`。

EU-51 Execute baseline：`main@04090a3cc8dd9c85112488f036c4bc1a67003594`。

EU-51 implementation integrated main：`main@fad4bfc17b762ec9612cf1e44a1c0af67e74a307`。

EU-52 Planning/Readiness baseline：`main@25e452ee3ce66c2d7da1000ad55e9570d732528a`。

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

EU-49 已完成 Page operational-content ownership、Generic Page canonical migration foundation 与 append-only V3 mapping。Implementation integrated main：`18da735c654c1a5d1310fe6db7e8e98f6f7b0026`；Authority closure integrated main：`e6fe7674398ad8c29fa7ff1d62eb500754a66cc8`。EU-49 Execute Authority 已终止。

EU-50 source handoff 与 Issue #77 Human Authority随后解决了 Main Page 当前产品交付所需的 source/resource 事实。新的 current Authority据此把 Main Page正式内容归入 JilinJobs Site Package，并通过 `slice-work → readiness-check` 形成 **EU-52 — Main Page Formal Content Package Adoption**。EU-52 随后已完成 implementation、verification、bounded Human Review、integration 与 Post-Integration closure，Execute Authority 已终止；其 accepted scope 仅覆盖 10 个 accepted RICH_TEXT Page、stable Page assets 与受保护的一次性 package adoption；stable Main ListItem 从未进入该 Unit，继续保持独立 Planning Candidate。

### E3 — Main Historical Content Collection & Canonical Migration

Current Authority：

- `docs/requirements/main-historical-content-migration.md`
- `docs/specifications/main-historical-content-migration.md`
- `docs/technical/main-historical-content-migration.md`

#### EU-50 — Main Source Discovery & Accepted Snapshot Promotion — COMPLETED

Completed Work Evidence：`docs/work/archive/eu50-main-source-discovery-promotion.md`。

Accepted integrated result：

- PR #117 squash merged to `main@05dfa604ccde45c8409cf6a456e4f201534dc602`；
- 3314 Article candidates闭合为 `3078 current import eligible + 6 source defect excluded + 230 deferred problem`；
- current accepted subset：3078 Articles = 1577 INTERNAL + 1501 EXTERNAL_LINK；
- current accepted resources：2603 files / 450,273,166 bytes；
- dataset digest：`sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`；
- canonical root：`data-migrations/main/v1/**`；
- Page/List source findings preserved as Site Package handoff；
- 6 source-defect Articles 与 230 deferred problem Articles均不进入 current canonical/import input；
- Execute Authority：**TERMINATED**。

#### EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review — COMPLETED

Completed Work Evidence：`docs/work/archive/eu51-main-canonical-import-runtime-review.md`。

Execution / integration result：

- Readiness baseline：`main@fd192460cb481645c1f1af435cbe5451145797d9`；
- Execute baseline：`main@04090a3cc8dd9c85112488f036c4bc1a67003594`；
- final PR #122 Head：`5adae340edcf605e36779c831fb512c31342eec8`；
- squash integrated main：`fad4bfc17b762ec9612cf1e44a1c0af67e74a307`；
- Fresh Runtime first import：3078 CREATED，0 conflict / invalid；
- second identical import：3078 SKIPPED，0 CREATED / conflict / invalid；
- stable mappings、ten target Columns、1577 INTERNAL + 1501 EXTERNAL_LINK 与全部 2603 resources / 450,273,166 bytes完成确定性 reconciliation；
- no Page/List migration mapping产生；
- exact-head EU-51 Runtime #8、Browser #4、Canonical #264、EU-30 #214、CI #957、Standard Review #840均 **PASS**；
- bounded Human Review：**PASS**；
- Post-Integration CI #958 / run `34417382337`：**PASS**，包含 Backend / Admin / Public / Integrated Browser；
- no Generic migration implementation or canonical bytes were changed；
- Execute Authority：**TERMINATED**。

### EU-52 — Main Page Formal Content Package Adoption — COMPLETED

EU-52 completed the Page-only Site Package delivery boundary:

- final implementation Head：`ff4acdc08e9b902d6aa273ff33fae33fb5bfc520`；
- PR #125 squash integrated main：`ccbd9fd8c6048f5b4a96d965b8578f7b7a1d2838`；
- 10 accepted Main formal Pages are package create-time defaults；
- 156 new Page-owned assets were integrated, total Site Package assets = 187；
- `budget` 13 PDFs are package-owned under `/static/pages/budget/**`；
- Existing Site guarded adoption only occurs on exact declared prior-package content fingerprint；operator-diverged content is preserved/reported；adoption is idempotent and later operator edits remain protected；
- exact-head automated gates and bounded Human Review PASS；the `guide/faq` heading hierarchy finding was corrected within the same Unit and reverified；
- Post-Integration CI #973 / run `34442174532` plus Site Package #104 / Backend Boundary #41 / Generic #64 / Party #19 all PASS；
- no Main Page Historical Migration mapping/input or Generic Flyway/schema change was introduced；
- Execute Authority：**TERMINATED**；Completed Work Evidence：`docs/work/archive/eu52-main-page-formal-content-package-adoption.md`。

### Stable Main ListItem follow-up

Stable Main ListItem membership remains an independent Site Package Planning Candidate. Current package owns `lists` definitions but not stable membership instances; stable identity, representation/schema evolution, adoption, reconcile, operator mutation and upgrade/removal semantics must be defined before `slice-work` can form any ListItem Execution Unit.

### Deferred Article / client-confirmation backlog

- 230 篇 problem Article继续作为 durable deferred evidence；
- 6 篇 source-defect Article继续等待客户确认；
- 二者均不属于已完成 EU-51 import input，也不属于 EU-52；
- 后续只能通过新的显式 Planning / Review Authority处理，不得静默修复、猜测、删除或自动导入。

## 当前 Next Gate

**Fresh Context Planning/Readiness decision**。

Current Ready Execution Unit = **NONE**。EU-52 Execute Authority 已终止，stable Main ListItem、230 deferred problem Articles、6 source-defect Articles及其他 Issue #57/#59/#60 candidates均保持独立；不得自动选择、编号或授权任何 successor Unit。

## 其他开放方向

- stable Main ListItem Site Package follow-up：独立 Planning Candidate；
- Issue #77：继续作为 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层长期 Architecture Authority；
- Issue #57：Public Rendering Architecture future discussion；
- Issue #59：Browser Compatibility & Runtime Guard 后置候选；
- Issue #60 C1：Loading / Skeleton Experience Planning Candidate；
- Issue #60 C2：Mobile Layout Human Review Candidate；
- Repository Split Readiness Assessment：deferred / independent。

上述候选均不从任何已完成 Unit或 EU-52 Readiness继承 Execute Authority。

## 历史收敛追溯

- Issue #92 Phase 0～Phase 3：`docs/project/pre-e1e3-convergence-plan.md` + closed Issue #92；
- Phase 1 Documentation Authority：EU-43 / EU-44 / EU-45 archive records；
- Backend / Migration foundation：EU-46 / EU-47 / EU-48 archive records；
- E2 Page migration foundation：EU-49 archive record；
- E3 source discovery / accepted Article snapshot：EU-50 archive record + `data-migrations/main/v1/**`；
- E3 Runtime import / reconciliation / Human Review：EU-51 archive record；
- Main Page Site Package delivery：EU-52 completed Work Evidence；
- Party canonical migration：对应 `data-migrations/party/**`、PR / Actions / Issue Current Evidence；
- 详细 historical implementation / verification 不在本 Roadmap 重复维护。