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
| Consumer Bootstrap ～ EU-36 | 已完成 | 通用 CMS、Admin/Public frontend、Party 正式页面 / historical migration、EU-30 current canonical data、Admin governance、Rich Text、Public source isolation 已建立 |
| EU-37～EU-42 Site Package Boundary | 已完成 | stable structure、Navigation identity、Runtime composition、one-time bootstrap、Generic Schema separation、stable assets 已闭环 |
| Issue #92 Phase 0 | 已完成 | Planning Authority 与 bounded architecture-review evidence 已收口 |
| Issue #92 Phase 1 | 已完成 | EU-43 / EU-44 / EU-45 完成 Current Authority semantic convergence 与 Documentation IA/archive closure |
| Issue #92 Phase 2A Backend Application / Core Boundary | **已完成** | **EU-46 — Backend Application / Core Boundary Foundation 完成；Server / Migration application 与 shared Core classpath boundary 已建立** |
| Issue #92 Phase 2B Generic Content Migration | **Ready / current gate** | **EU-47 — Generic Content Migration Application Foundation 已完成 dependency closure、Requirement / Specification / Technical Planning、`slice-work` 与 `readiness-check`；Readiness PASS，Execute NOT STARTED** |
| Issue #92 Phase 2C Party Migration De-specialization | Planning Candidate / downstream | 必须等待 EU-47 完成并重新独立规划，不继承 EU-47 Execute Authority |
| Issue #92 Phase 3 Compatibility / E1～E3 Re-entry | blocked / downstream | 完整链路重新对账后才能决定 re-entry PASS |
| Issue #60 / E1～E3 Main Site Formal Content | blocked / downstream | Phase 3 PASS 前不得进入 Execute |
| Repository Split Readiness Assessment | deferred | 只在四层 boundary 完整闭环后独立评估；不自动拆仓 |
| Issues #57 / #59 / #60 其他候选 | 规划层保留 | C1/C2、Browser Compatibility、Public Rendering Architecture 等保持独立 |

## 当前已接受长期边界

### 1. Generic CMS Core

- Generic CMS Schema、site-neutral domain / validation / persistence / API-supporting capability、provisioning capability 属于 Core；
- Backend Flyway 只承担 Generic CMS Schema evolution，不内建 JilinJobs Site instance rows；
- `preset`、stable identity、bootstrap-state、resource / article / list 等可以是 Generic capability，具体 Site identity / instance data 不属于 Core；
- EU-41 accepted active Flyway 为 current Generic V1/V2；后续 Generic Schema change 从 V3 append-only；
- EU-46 后 shared build owner 为 `backend/modules/cms-core`；`cms-server` 与 `content-migration` 只单向依赖 Core，Core 不依赖任一 app。

### 2. JilinJobs Site Package

```text
sites/jilinjobs/
├── structure/**
├── bootstrap/**
└── assets/**
```

- stable structure 与 operator-managed Runtime Data 分离；
- one-time bootstrap 不 overwrite / resurrect后续 operator changes；
- stable assets 使用 manifest integrity + create-if-missing projection + protected-path；
- `/static/uploads/**` 与 historical canonical assets 不属于 stable Site asset ownership。

### 3. Historical Content Migration

- `data-migrations/**` 承载 canonical historical data、resource、legacy identity / fingerprint、provenance 与 compatibility；
- Historical Migration 不等于 Flyway，也不等于 Site Package structure/bootstrap/assets；
- Party current canonical Runtime Dataset = 183 Articles；EU-29 frozen acceptedSnapshot = 181；4 条 accepted carousel 与 EU-29→EU-30 compatibility 必须持续可验证；
- Canonical Dataset 依赖 stable Site identity，不依赖临时 Runtime DB id 或 Public Renderer internals；
- EU-46 后 Party migration implementation 位于独立 `backend/apps/content-migration` application；
- Phase 2B / EU-47 的 Ready scope只建立 site-neutral Generic Canonical Dataset → CMS Runtime capability，不迁移 Party current dataset/profile/compatibility；Party-specific aliases、accepted fingerprints与 EU-29→EU-30 transition仍属于后续 Phase 2C。

### 4. Replaceable Public Renderer

- `frontend/public-site` 是稳定 Public API / URL / Site Data Contract 的当前 Vue/Vite consumer；
- Public production source 不依赖 Admin endpoint；
- Renderer 不成为 Site Definition、Canonical Migration、Flyway 或 CMS Domain Authority；
- Main / Party canonical URL、产品 identity 与 accepted behavior在后续重构中保持。

## 当前 Planning Priority — Issue #92

总体规划：`docs/project/pre-e1e3-convergence-plan.md`。

Documentation Authority Map：`docs/README.md`。

Current Ready Execution Unit：**EU-47 — Generic Content Migration Application Foundation**。

Readiness：**PASS**。Execute：**NOT STARTED**。

Current next gate：本 planning/readiness change 集成到 `main` 后，以新的 Fresh Context 从 integrated `main` 重新读取 Repository Authority、Issue #92 / #77、EU-47 Requirement / Specification / Technical Plan / Work Artifact、Open PR / Actions 与 base drift；只有 Readiness仍有效且无 drift / blocker 时才允许进入 EU-47 Execute。

Phase 2B Current Authority：

- Requirement：`docs/requirements/generic-content-migration-application.md`；
- Specification：`docs/specifications/generic-content-migration-application.md`；
- Technical Plan：`docs/technical/generic-content-migration-application.md`；
- Ready Work artifact：`docs/work/current/eu47-generic-content-migration-application-foundation.md`。

Phase 2A accepted Authority继续作为长期 application/core contract：

- Requirement：`docs/requirements/backend-application-core-boundary.md`；
- Specification：`docs/specifications/backend-application-core-boundary.md`；
- Technical Plan：`docs/technical/backend-application-core-boundary.md`；
- Completed Work artifact：`docs/work/archive/eu46-backend-application-core-boundary-foundation.md`。

EU-43 / EU-44 / EU-45 / EU-46 的 Execute Authority均已终止。EU-47不继承这些 Unit 的 Execute Authority；EU-47未来取得的 Execute Authority也只覆盖本 Unit，不自动授权 Phase 2C、Phase 3或 Issue #60 / E1～E3。