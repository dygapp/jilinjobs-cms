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
| Issue #60 / E3 Main Historical Content Migration | **CURRENT PLANNING GATE** | Requirement / Specification READY；EU-49 prerequisite 已满足；下一步为 Fresh Context source-evidence recovery、dependency closure、`slice-work` / `readiness-check`；当前无 Identifier / Ready / Execute Authority |
| Repository Split Readiness Assessment | deferred | 四层 boundary 已闭环，但 Assessment 仍独立后置；不自动拆仓，也不阻塞 E3 Planning |
| Issues #57 / #59 / #60 其他候选 | 规划层保留 | C1/C2、Browser Compatibility、Public Rendering Architecture 等保持独立 |

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
- Page stable identity / required structure由 Site Package持有；EU-49 已完成 ownership transfer，使 ordinary reconcile 不再覆盖 existing Page 的 operator-managed `bodyHtml / renderMode / embedUrl`。

### 3. Historical Content Migration

- `data-migrations/**` 承载 canonical historical data、resource、legacy identity / fingerprint、provenance 与 compatibility；
- Historical Migration 不等于 Flyway，也不等于 Site Package structure/bootstrap/assets；
- Canonical Dataset 依赖 stable Site identity，不依赖临时 Runtime DB id 或 Public Renderer internals；
- Historical Migration implementation 位于独立 `backend/apps/content-migration` non-web application，只依赖 `cms-core`；
- Generic Content Migration 已提供 site-neutral Article / ListItem / Page load、preflight、stable mapping、guarded apply、idempotency/conflict/report foundation；Main-specific source facts / policy不得进入 Generic capability。

### 4. Replaceable Public Renderer

- `frontend/public-site` 是稳定 Public API / URL / Site Data Contract 的当前 Vue/Vite consumer；
- Public production source 不依赖 Admin endpoint；
- Renderer 不成为 Site Definition、Canonical Migration、Flyway 或 CMS Domain Authority；
- Main / Party canonical URL、产品 identity 与 accepted behavior 在后续内容建设中保持。

## Issue #60 — Main Site Remaining Product Content

Initial E1～E3 planning baseline：`main@f42bacf4ab7719e3291288c77f0685b428b86141`。

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

EU-49 已通过 PR #112 完成：

1. existing Page ordinary reconcile 不再覆盖 operator-owned content fields；
2. Fresh Page provisioning保持 package defaults；
3. Core 提供窄的 Page content-only update boundary；
4. Generic Content Migration增加 stable Page target / mapping / guarded first apply / SKIP / CONFLICT / resource projection；
5. append-only Generic Flyway V3增加 site-neutral Page migration mapping；
6. Generic / Party / Site Package / Backend boundary / Repository CI regressions保持 PASS。

Integrated main：`18da735c654c1a5d1310fe6db7e8e98f6f7b0026`；Post-Integration CI #868 / run `34200526862` = **PASS**。EU-49 Execute Authority 已终止。

### E3 — Main Historical Content Collection & Canonical Migration

Current Authority：

- `docs/requirements/main-historical-content-migration.md`
- `docs/specifications/main-historical-content-migration.md`

EU-49 foundation prerequisite 已满足。E3 下一步不是直接 Execute，而是基于 current Repository 与真实 source evidence重新恢复 Planning：

- source discovery completeness / unresolved classification；
- accepted source snapshot / provenance；
- Article / EXTERNAL_LINK Article / resources；
- formal Page content；
- historical ListItem（仅当 source evidence证明属于迁移范围）；
- canonical organization；
- Fresh DB import、idempotency、conflict、reconciliation；
- Human Review。

当前明确：

- **Current Ready Execution Unit：NONE**；
- E3 没有 Identifier；
- E3 没有 Candidate / Ready Execution Unit；
- E3 没有 Execute baseline / Execute Authority；
- 没有开始 Main source collection / canonical promotion；
- 没有冻结未经 repository evidence接受的 item count。

下一自然 Gate：**E3 Fresh Context Planning / source-evidence recovery → necessary dependency closure → `slice-work` → `readiness-check`**。只有形成新的 Ready Execution Unit 后才能进入 Execute；不得继承 EU-49 或更早 Unit 的 Execute Authority。

## 其他开放方向

- Issue #77：继续作为 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层长期 Architecture Authority；
- Issue #57：Public Rendering Architecture future discussion；
- Issue #59：Browser Compatibility & Runtime Guard 后置候选；
- Issue #60 C1：Loading / Skeleton Experience Planning Candidate；
- Issue #60 C2：Mobile Layout Human Review Candidate；
- Repository Split Readiness Assessment：deferred / independent。

上述候选均不自动改变 E3 当前 Planning Gate，也不从任何已完成 Unit继承 Execute Authority。

## 历史收敛追溯

- Issue #92 Phase 0～Phase 3：`docs/project/pre-e1e3-convergence-plan.md` + closed Issue #92；
- Phase 1 Documentation Authority：EU-43 / EU-44 / EU-45 archive records；
- Backend / Migration foundation：EU-46 / EU-47 / EU-48 archive records；
- E2 Page migration foundation：EU-49 archive record；
- Party canonical migration：对应 `data-migrations/party/**`、PR / Actions / Issue Current Evidence；
- 详细 historical implementation / verification 不在本 Roadmap 重复维护。

## Fresh Context 恢复

新的开发会话至少：

1. 读取当前 `main`、Open PR / Issue 与最近相关 Actions；
2. 完整读取 `AGENTS.md`、Root `README.md`、`docs/README.md`；
3. 读取本 Roadmap 与 `docs/project/development-method.md`；
4. 读取 Issue #60、Issue #77 与当前 Planning Gate 的直接 Authority；
5. 当前入口若仍为 E3，则完整读取 `docs/requirements/main-historical-content-migration.md` 与 `docs/specifications/main-historical-content-migration.md`；
6. 重新核验 source-evidence availability、dependency closure 与实际 Repository 状态；
7. 只有在 `slice-work`形成 Candidate 且 `readiness-check = PASS` 后，才建立新的 Ready Execution Unit与后续 Execute baseline。

当前 Ready Execution Unit：**NONE**。
