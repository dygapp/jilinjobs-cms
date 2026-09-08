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
| Issue #60 / E2 Main Single-page Formal Content | **READY EXECUTION UNIT** | **EU-49 — Page Operational Content Ownership & Migration Foundation** 已完成 Requirement / Specification / Technical Planning、`slice-work` 与 `readiness-check`；进入 Execute 前仍需 Fresh Context revalidation |
| Issue #60 / E3 Main Historical Content Migration | **Downstream Planning Authority** | Requirement / Specification READY；依赖 EU-49 completion；当前不分配 EU Identifier、不允许 source collection / canonical promotion / Execute |
| Repository Split Readiness Assessment | deferred | 四层 boundary 已闭环，但 Assessment 仍独立后置；不自动拆仓，也不阻塞 EU-49 |
| Issues #57 / #59 / #60 其他候选 | 规划层保留 | C1/C2、Browser Compatibility、Public Rendering Architecture 等保持独立 |

## 当前已接受长期边界

### 1. Generic CMS Core

- Generic CMS Schema、site-neutral domain / validation / persistence / API-supporting capability、provisioning capability 属于 Core；
- Backend Flyway 只承担 Generic CMS Schema evolution，不内建 JilinJobs Site instance rows；
- `preset`、stable identity、bootstrap-state、resource / article / page / list 等可以是 Generic capability，具体 Site identity / instance data 不属于 Core；
- EU-41 accepted active Flyway 为 current Generic V1/V2；后续 Generic Schema change 从 V3 append-only；
- shared build owner 为 `backend/modules/cms-core`；`cms-server` 与 `content-migration` 只单向依赖 Core，Core 不依赖任一 app。

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
- Page stable identity / structure 可以由 Site Package 持有，但普通 reconcile 不应持续夺回已经进入 operator-managed lifecycle 的 Page 正文与呈现内容。EU-49 负责关闭当前这一 ownership gap。

### 3. Historical Content Migration

- `data-migrations/**` 承载 canonical historical data、resource、legacy identity / fingerprint、provenance 与 compatibility；
- Historical Migration 不等于 Flyway，也不等于 Site Package structure/bootstrap/assets；
- Canonical Dataset 依赖 stable Site identity，不依赖临时 Runtime DB id 或 Public Renderer internals；
- Historical Migration implementation 位于独立 `backend/apps/content-migration` non-web application，只依赖 `cms-core`；
- Generic Content Migration 已提供 site-neutral Article / ListItem load、preflight、stable mapping、idempotency/conflict/report foundation；EU-49 只在真实 E2 consumer 证明需要的范围内补充 Page canonical migration capability，不引入 Main-specific policy 到 Generic Core。

### 4. Replaceable Public Renderer

- `frontend/public-site` 是稳定 Public API / URL / Site Data Contract 的当前 Vue/Vite consumer；
- Public production source 不依赖 Admin endpoint；
- Renderer 不成为 Site Definition、Canonical Migration、Flyway 或 CMS Domain Authority；
- Main / Party canonical URL、产品 identity 与 accepted behavior 在后续内容建设中保持。

## 当前 Planning / Execute Priority — Issue #60

Planning baseline：`main@f42bacf4ab7719e3291288c77f0685b428b86141`。

Issue #92 的 Phase 0～Phase 3 前置路线已经闭环并关闭；Issue #77 继续承担四层长期 Architecture Authority。Issue #60 现已完成 E1～E3 的第一轮 dependency closure：

```text
E1 External-link Boundary
  └─ Authority closure / no implementation EU

E2 Single-page Formal Content
  └─ EU-49 Page Operational Content Ownership & Migration Foundation — READY
       ↓ completion required
E3 Historical Content Collection & Canonical Migration
  └─ downstream Requirement / Specification READY only
```

### E1 — Main External-link Ownership & Behavior Boundary

Current Authority：

- Requirement：`docs/requirements/main-external-link-boundary.md`；
- Specification：`docs/specifications/main-external-link-boundary.md`。

Repository audit 已确认 EXTERNAL_LINK Article、Navigation、CmsList LINK / ARTICLE、Advertisement / fixed external integration 与 Public Renderer 的 document navigation / open-mode 基础能力已经存在。当前缺口是 ownership / migration classification 的正式收敛，而不是新的 Runtime capability，因此不创建 E1 implementation Unit。

E1 closure 不授予后继 Unit Execute Authority；它只作为 E2 / E3 的 accepted boundary input。

### E2 — EU-49 Page Operational Content Ownership & Migration Foundation

Current Authority：

- Requirement：`docs/requirements/main-single-page-formal-content.md`；
- Specification：`docs/specifications/main-single-page-formal-content.md`；
- Technical Plan：`docs/technical/main-single-page-formal-content.md`；
- Current Work：`docs/work/current/eu49-page-content-migration-foundation.md`。

Readiness：**PASS**。

EU-49 的 bounded目标：

1. 保持 Page stable identity / preset structure 由 Site Package 管理；
2. 让 ordinary Site Package reconcile 对 existing Page 停止持续覆盖 `bodyHtml / renderMode / embedUrl` 等 operator-managed内容字段；
3. 为 Generic Content Migration 增加 site-neutral Page canonical record / stable Page target / mapping / precondition fingerprint / CREATE-APPLY-SKIP-CONFLICT 语义；
4. 使用 append-only Generic Flyway V3 增加 Page migration mapping schema；
5. 保持现有 Admin / Public Page URL、Rich Text safety、Party migration、Article/List migration、Site Package 与 Public Renderer行为；
6. 本 Unit 不采集 Main 原站正式 Page 正文，不创建 Main canonical dataset，不提前进入 E3。

Planning / Readiness integration 后，EU-49 仍必须通过新的 Fresh Context 从 integrated `main` 重新核验：

- Current Repository Authority；
- Issue #60 / #77；
- EU-49 Requirement / Specification / Technical Plan / Current Work；
- Open PR / Issue 与最近相关 Actions；
- integrated planning Head 与 current `main` 的 base drift。

只有上述复核仍 PASS 且没有 blocker，才能建立 **EU-49 自身 Execute baseline**。不得继承 EU-48、Issue #92 Phase 3、E1 或 planning branch 的 Execute Authority。

### E3 — Main Historical Content Collection & Canonical Migration

Current downstream Authority：

- Requirement：`docs/requirements/main-historical-content-migration.md`；
- Specification：`docs/specifications/main-historical-content-migration.md`。

E3 当前只冻结后续 source discovery、evidence promotion、accepted snapshot、Article / Page / List canonical organization、provenance、Fresh DB import / idempotency / reconciliation 与 Human Review contract。

在 EU-49 completion 前：

- 不给 E3 分配 EU Identifier；
- 不开始 Main source collection；
- 不 promotion 临时 evidence 为 canonical dataset；
- 不声明 E3 Ready / Execute；
- 不预先锁定 Main Article / Page / List 数量。

EU-49 完成后，E3 必须基于届时真实 source evidence 再执行 `slice-work`，决定 source discovery / evidence promotion 与 canonical import / reconciliation / Human Review 是否需要一个或多个 Execution Units。

## 其他开放方向

- Issue #77：继续作为 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层长期 Architecture Authority；
- Issue #57：Public Rendering Architecture future discussion；
- Issue #59：Browser Compatibility & Runtime Guard 后置候选；
- Issue #60 C1：Loading / Skeleton Experience Planning Candidate；
- Issue #60 C2：Mobile Layout Human Review Candidate；
- Repository Split Readiness Assessment：deferred / independent。

上述候选均不改变 EU-49 当前 Readiness，也不从 EU-49 自动继承 Execute Authority。

## 历史收敛追溯

- Issue #92 Phase 0～Phase 3：`docs/project/pre-e1e3-convergence-plan.md` + closed Issue #92；
- Phase 1 Documentation Authority：EU-43 / EU-44 / EU-45 archive records；
- Backend / Migration foundation：EU-46 / EU-47 / EU-48 archive records；
- Party canonical migration：对应 `data-migrations/party/**`、PR / Actions / Issue Current Evidence；
- 详细 historical implementation / verification 不在本 Roadmap 重复维护。

## Fresh Context 恢复

新的开发会话至少：

1. 读取当前 `main`、Open PR / Issue 与最近相关 Actions；
2. 完整读取 `AGENTS.md`、Root `README.md`、`docs/README.md`；
3. 读取本 Roadmap 与 `docs/project/development-method.md`；
4. 读取 Issue #60、Issue #77 与当前 Ready Unit 的直接 Authority；
5. 当前入口若仍为 EU-49，则完整读取 `docs/requirements/main-single-page-formal-content.md`、`docs/specifications/main-single-page-formal-content.md`、`docs/technical/main-single-page-formal-content.md`、`docs/work/current/eu49-page-content-migration-foundation.md`；
6. 重新核验 EU-49 Readiness、planning integration结果与 base drift；只有仍 PASS 才建立本 Unit Execute baseline并进入 Execute；
7. E3 继续保持 downstream Planning Authority，直到 EU-49 completion 后重新 slice/readiness。

当前 Ready Execution Unit：**EU-49 — Page Operational Content Ownership & Migration Foundation**。Readiness PASS 不等于已经建立 Execute baseline。