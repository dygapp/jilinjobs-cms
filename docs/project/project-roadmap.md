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
| Issue #92 Phase 2B Generic Content Migration | **Planning Candidate / current next gate** | 必须重新 dependency closure、Requirement / Specification / Technical Planning、`slice-work`、`readiness-check`；当前无 Ready EU |
| Issue #92 Phase 2C Party Migration De-specialization | Planning Candidate / downstream | 必须等待 2B boundary 成立后独立规划 |
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
- EU-46 后 Party migration implementation 当前位于独立 `backend/apps/content-migration` application；这只是 application boundary，不等于 Phase 2B Generic Migration Engine 或 Phase 2C Party de-specialization 已完成。

### 4. Replaceable Public Renderer

- `frontend/public-site` 是稳定 Public API / URL / Site Data Contract 的当前 Vue/Vite consumer；
- Public production source 不依赖 Admin endpoint；
- Renderer 不成为 Site Definition、Canonical Migration、Flyway 或 CMS Domain Authority；
- Main / Party canonical URL、产品 identity 与 accepted behavior在后续重构中保持。

## 当前 Planning Priority — Issue #92

总体规划：`docs/project/pre-e1e3-convergence-plan.md`。

Documentation Authority Map：`docs/README.md`。

Current Ready Execution Unit：**NONE**。

Current next gate：**Phase 2B — Generic Content Migration Application Planning Candidate**。

Phase 2B 当前没有 Candidate Execution Unit、Ready Execution Unit 或 Execute Authority。必须重新完成 dependency closure、Requirement / Specification / 必要 Technical Planning，之后由 `slice-work` 形成 Candidate EU，并经 `readiness-check` PASS 后才可能进入 Execute。

Phase 2A accepted Authority继续作为长期 application/core contract：

- Requirement：`docs/requirements/backend-application-core-boundary.md`；
- Specification：`docs/specifications/backend-application-core-boundary.md`；
- Technical Plan：`docs/technical/backend-application-core-boundary.md`；
- Completed Work artifact：`docs/work/archive/eu46-backend-application-core-boundary-foundation.md`。

EU-43 / EU-44 / EU-45 / EU-46 的 Execute Authority均已终止。任何后续 Planning Candidate 不继承这些 Unit 的 Execute Authority。

### Phase 0 — Planning Authority Solidification — COMPLETED

Issue #92 / #77 / Roadmap / Site Package Planning 的总体路线已对齐。AR-02 / AR-04 证明 application/core boundary 必须先于 Gradle implementation choice冻结；模型 Review 只构成 Review Evidence，不替代 Repository Authority / readiness-check。

### Phase 1 — Repository Documentation Authority Convergence — COMPLETED

- EU-43 Current Authority Semantic Reconciliation：COMPLETED；
- EU-44 Canonical Product Authority Consolidation：COMPLETED；
- EU-45 Documentation Information Architecture & Archive Migration：COMPLETED。

Phase 1 classification contract `CURRENT / PARTIALLY_CURRENT / SUPERSEDED / HISTORICAL_EVIDENCE` 继续有效；archive 默认不参与 Fresh Context Current Authority 恢复。历史 work records 位于 `docs/work/archive/`。

### Phase 2A — Backend Application / Core Boundary Foundation — COMPLETED

EU-46 established：

```text
cms-server app ───────────→ cms-core
content-migration app ────→ cms-core
```

Accepted physical topology：

```text
backend/
├── modules/
│   └── cms-core/
└── apps/
    ├── cms-server/
    └── content-migration/
```

Phase 2A implementation result：

- Core 不依赖任一 app，两个 app 互不依赖；
- Server 持有 `CmsApplication`、HTTP/MVC/static HTTP 与 server-only composition；
- Migration 持有四个 Party migration implementation、独立 non-web Spring composition 与 CLI/import/report compatibility；
- Generic Flyway V1/V2、`cms-metadata.yml`、Site Package lifecycle保持 single Core resource/capability authority；
- root `backend` build/command compatibility与现有 Server JAR path保持；
- Party 183 Articles、4 carousel、idempotency、fingerprint conflict、EU-29→EU-30 compatibility 与 resource integrity保持；
- Canonical / EU-30 Upgrade / Site Package path filters 已迁移到新 ownership；
- focused Backend Application Boundary Verification证明 packaged ownership 与 Migration non-web composition；
- 没有改变 `data-migrations/**`、Generic Schema semantics、Admin/Public API、frontend / Site Package bytes或产品行为。

Pre-integration exact-head evidence由 PR #105 / Actions 记录；Post-Integration evidence由 Issue #92 Current Evidence承担。EU-46 完成不自动授权 Phase 2B / 2C。

### Phase 2B — Generic Content Migration Application — PLANNING CANDIDATE

现在只允许进入 Planning，不允许直接 Execute。目标链路：

```text
Legacy Source
→ collection / normalization / promotion
→ Canonical Migration Dataset
→ Generic JVM Content Migration Application
→ CMS Runtime
```

Generic Engine 负责 canonical validation、path/digest safety、stable migration identity/fingerprint、preflight、transaction/file-side-effect boundary、dependency order、import/reconciliation/report，但不得内建 Party / JilinJobs / EU-29 / EU-30 identity。

Phase 2B 在形成 Requirement / Specification / Technical Authority、完成 dependency closure 后，必须通过 `slice-work` 与 `readiness-check` 独立形成新的 Ready Execution Unit；Roadmap 中的 Phase 名称不授予 Identifier 或 Execute Authority。

### Phase 2C — Party Migration De-specialization & Compatibility — PLANNING CANDIDATE

在 Generic Engine boundary成立后，Party-specific aliases、accepted fingerprints、carousel legacy transition与upgrade-only policy由 Party dataset/profile/compatibility authority承载；同一 Generic Application必须保持 current 183 Articles、4 carousel及 accepted compatibility。

### Phase 3 — Canonical Migration Compatibility & E1～E3 Re-entry Gate

最终重新对账：

```text
Generic CMS Schema
→ JilinJobs Site Package stable structure
→ one-time Site bootstrap
→ Generic Content Migration Application
→ Party Canonical Dataset
→ Runtime
→ Replaceable Public Renderer
```

只有 Current Evidence 闭环全部 obligation 后才可记录 E1～E3 re-entry PASS；发现具体 implementation gap 时再通过 `slice-work` 形成新的 Candidate EU。Phase 3 PASS 前不得进入 Issue #60 / E1～E3 Execute。

### Phase 4 — Repository Split Readiness Assessment

继续 deferred。Assessment 不等于自动拆仓，也不默认阻塞 E1～E3。

## Architecture Review Eval Experiment

Consumer-local `evals/architecture/**` 当前结论保持 **ADJUST**：真实高返工成本架构问题可以使用 bounded independent review；默认 lower-cost capable review first，仅在 unresolved / conflicting / deliberate second opinion 时升级 Astra。模型 review 不覆盖 Repository Authority / readiness-check。

## Fresh Context 恢复

新的开发会话至少：

1. 读取当前 `main`、Open PR / Issue 与最近相关 Actions；
2. 完整读取 `AGENTS.md`、Root `README.md`、`docs/README.md`；
3. 读取本 Roadmap 与 `docs/project/development-method.md`；
4. 读取 Issue #92、Issue #77 与 `docs/project/pre-e1e3-convergence-plan.md` 当前状态；
5. 若目标是继续 Issue #92，则从 Phase 2B **Planning Candidate** 恢复 dependency closure 与 Planning Authority，不读取 archive work artifact作为 Execute Authority；
6. 只有新的 Candidate 经 `slice-work → readiness-check` PASS 形成 Ready Execution Unit 后，才允许进入后续 Execute。

当前不得从 Roadmap、EU-46 完成事实、Phase 名称或 Issue 编号推导 Phase 2B / 2C、Phase 3 或 E1～E3 Execute Authority。