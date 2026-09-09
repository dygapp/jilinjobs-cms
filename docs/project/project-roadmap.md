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
| Issue #60 / E2 Main Single-page Formal Content | **COMPLETED** | EU-49 已关闭 Page operational-content ownership gap，并建立 site-neutral Generic Page canonical migration foundation |
| Issue #60 / E3 / EU-50 Main Source Discovery & Accepted Snapshot Promotion | **COMPLETED** | accepted current Article subset 已 repository-owned promotion 并集成；Execute Authority terminated |
| Issue #60 / E3 / EU-51 Main Canonical Import, Runtime Reconciliation & Human Review | **READY / Readiness PASS** | accepted snapshot dependency 已满足；Current Work 已形成；Planning/Readiness state 需先集成，随后 Fresh Context 建立 Execute baseline |
| Main Page / stable ListItem Site Package follow-up | **Planning Candidate** | EU-50 已形成 source handoff；Page content 与 stable ListItem capability/content 仍需独立 Site Package Planning / Readiness |
| Deferred problem Article review | deferred | 230 篇 problem Article 与 6 篇 source-defect Article 保持独立 evidence / client-review backlog，不阻断当前路线 |
| Repository Split Readiness Assessment | deferred | 四层 boundary 已闭环，但 Assessment 仍独立后置 |
| Issues #57 / #59 / #60 其他候选 | 规划层保留 | C1/C2、Browser Compatibility、Public Rendering Architecture 等保持独立 |

Current Ready Execution Unit：**EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review Closure**。

EU-51 Readiness baseline：`main@fd192460cb481645c1f1af435cbe5451145797d9`。Readiness PASS 只建立 Ready Execution Unit 身份，不在 Planning/Readiness branch 上建立 Execute baseline。下一 Gate 是先集成该状态，再由新的 Fresh Context 从 integrated `main` 建立 EU-51 独立 Execute baseline。

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
- Main Page source handoff 与 stable Main ListItem membership 仍属于 Site Package 后续 Planning，不属于 Historical Migration fallback。

### 3. Historical Content Migration

- `data-migrations/**` 承载 canonical historical data、resource、legacy identity / fingerprint、provenance 与 compatibility；
- Historical Migration 不等于 Flyway，也不等于 Site Package structure/bootstrap/assets；
- Canonical Dataset 依赖 stable Site identity，不依赖临时 Runtime DB id 或 Public Renderer internals；
- Historical Migration implementation 位于独立 `backend/apps/content-migration` non-web application，只依赖 `cms-core`；
- Generic Content Migration 已提供 site-neutral Article / ListItem / Page load、preflight、stable mapping、guarded apply、idempotency/conflict/report foundation；Main-specific source facts / policy不得进入 Generic capability；
- Legacy Source只允许出现在显式 Collect / Discovery 边界；promoted canonical verification与Runtime import必须可离线执行；
- Main current accepted Article subset 已 repository-owned 固化到 `data-migrations/main/v1/**`；deferred/problem Article evidence 与 current import input 分离。

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

### E1 — Main External-link Ownership & Behavior Boundary

Current Authority：

- `docs/requirements/main-external-link-boundary.md`
- `docs/specifications/main-external-link-boundary.md`

Repository audit 已确认 EXTERNAL_LINK Article、Navigation、CmsList LINK / ARTICLE、Advertisement / fixed external integration 与 Public Renderer 的 document navigation / open-mode 基础能力已经存在。当前没有独立 Runtime implementation gap，因此 E1 以 Planning / Authority closure结束，不创建 implementation EU，也不向后续工作传递 Execute Authority。

### E2 — Main Single-page Formal Content / EU-49

Current accepted contract：

- `docs/requirements/main-single-page-formal-content.md`
- `docs/specifications/main-single-page-formal-content.md`
- `docs/technical/main-single-page-formal-content.md`
- completed Work Evidence：`docs/work/archive/eu49-page-content-migration-foundation.md`

EU-49 已完成 Page operational-content ownership、Generic Page canonical migration foundation 与 append-only V3 mapping。Implementation integrated main：`18da735c654c1a5d1310fe6db7e8e98f6f7b0026`；Authority closure integrated main：`e6fe7674398ad8c29fa7ff1d62eb500754a66cc8`。EU-49 Execute Authority 已终止。

### E3 — Main Historical Content Collection & Canonical Migration

Current Authority：

- `docs/requirements/main-historical-content-migration.md`
- `docs/specifications/main-historical-content-migration.md`
- `docs/technical/main-historical-content-migration.md`

#### EU-50 — Main Source Discovery & Accepted Snapshot Promotion — COMPLETED

Completed Work Evidence：

`docs/work/archive/eu50-main-source-discovery-promotion.md`

Accepted integrated result：

- PR #117 squash merged to `main@05dfa604ccde45c8409cf6a456e4f201534dc602`；
- 3314 Article candidates闭合为 `3078 current import eligible + 6 source defect excluded + 230 deferred problem`；
- current accepted subset：3078 Articles = 1577 INTERNAL + 1501 EXTERNAL_LINK；
- current accepted resources：2603 files / 450,273,166 bytes；
- dataset digest：`sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`；
- canonical root：`data-migrations/main/v1/**`；
- Page/List source findings preserved as Site Package handoff；
- 6 source-defect Articles 与 230 deferred problem Articles均不进入 current canonical/import input；
- no Runtime Main import / EU-51 execution occurred；
- Execute Authority：**TERMINATED**。

Final PR Head `8c2fdd6cdbbd4d1faf865d0ba4ca0f40a0096e84` exact-head evidence：EU-50 Source Discovery #64、EU-50 Import Eligibility #30、Canonical Migration #256、Generic Content Migration #51、EU-30 Upgrade #206、CI #944、Review Environment #830 均 **PASS**。

Integrated main evidence：Generic Content Migration #52 / run `34354290017` = **PASS**；CI #945 / run `34354289981` = **PASS**，包含 Backend / Admin / Public / Integrated Browser。

#### EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review — READY

Current Work：

`docs/work/current/eu51-main-canonical-import-runtime-review.md`

`readiness-check = PASS` on `main@fd192460cb481645c1f1af435cbe5451145797d9`。

Readiness closure confirms:

- EU-50 accepted snapshot dependency is satisfied；
- the integrated canonical dataset is repository-owned/offline-verifiable and has zero unscoped blocking observations；
- all ten Main Article target Column aliases exist in JilinJobs Site Package and are enabled；
- Generic Content Migration already provides Article load/validation/preflight/apply, stable mapping, fail-closed conflict/invalid handling and idempotency；
- repository tasks already expose `provisionSitePackage` and `importCanonicalContent`；
- existing CI / Review Environment / browser infrastructure can carry downstream evidence；
- 230 deferred problem Articles、6 source-defect Articles以及 Page/List Site Package follow-up均明确不进入 EU-51 import scope。

Required Fresh Runtime ordering is explicit:

```text
Fresh MySQL
→ Generic Flyway V1/V2/V3
→ JilinJobs Site Package stable provisioning
→ Generic Main canonical import
→ reconciliation + second-run idempotency
→ Public/Admin/Integrated Browser
→ bounded Human Review
```

EU-51 Execute baseline remains **PENDING** until this Planning/Readiness state is integrated and a new Fresh Context revalidates actual `main` / Authority / Actions / base drift. No Runtime Main import is authorized on the Planning/Readiness branch.

#### Site Package Page/List follow-up

EU-50 source handoff 已证明 Page/List 属于独立产品 ownership：

- accepted Page source content/resource projection 需在 Site Package Authority 下规划；
- stable Main ListItem membership 仍缺少 package stable identity / reconcile / adoption semantics；
- 该路径与 EU-51 并行但相互不继承 Execute Authority。

## 当前 Next Gate

**EU-51 Planning/Readiness Integration → Fresh Context Execute-baseline recovery**。

新的 Execute Context 至少：

1. 重新确认 integrated `main`、Open PR / Issue 与最近相关 Actions；
2. 完整读取 `AGENTS.md`、Root `README.md`、`docs/README.md`；
3. 读取本 Roadmap 与 `docs/project/development-method.md`；
4. 读取 Issue #60、Issue #77；
5. 完整读取 E3 Requirement / Specification / Technical Plan 与 EU-51 Current Work；
6. 核验 `data-migrations/main/v1/manifest.json` 的 3078/2603/digest contract；
7. 确认 230 deferred problem Articles 与 6 source-defect Articles仍保持后置、非 EU-51 import input；
8. 确认 no base/authority drift 后建立 EU-51 独立 Execute baseline。

Current Ready Execution Unit：**EU-51**。Readiness PASS 不等于 Execute baseline 已建立；不得继承 EU-50、EU-49、EU-48、Phase 3 或 E1 的 Execute Authority，也不得在 Planning/Readiness branch直接开始 Runtime import。

## 其他开放方向

- Issue #77：继续作为 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层长期 Architecture Authority；
- Issue #57：Public Rendering Architecture future discussion；
- Issue #59：Browser Compatibility & Runtime Guard 后置候选；
- Issue #60 C1：Loading / Skeleton Experience Planning Candidate；
- Issue #60 C2：Mobile Layout Human Review Candidate；
- Repository Split Readiness Assessment：deferred / independent。

上述候选均不从任何已完成 Unit继承 Execute Authority。

## 历史收敛追溯

- Issue #92 Phase 0～Phase 3：`docs/project/pre-e1e3-convergence-plan.md` + closed Issue #92；
- Phase 1 Documentation Authority：EU-43 / EU-44 / EU-45 archive records；
- Backend / Migration foundation：EU-46 / EU-47 / EU-48 archive records；
- E2 Page migration foundation：EU-49 archive record；
- E3 source discovery / accepted Article snapshot：EU-50 archive record + `data-migrations/main/v1/**`；
- E3 Runtime import / reconciliation / Human Review：EU-51 current Work；
- Party canonical migration：对应 `data-migrations/party/**`、PR / Actions / Issue Current Evidence；
- 详细 historical implementation / verification 不在本 Roadmap 重复维护。
