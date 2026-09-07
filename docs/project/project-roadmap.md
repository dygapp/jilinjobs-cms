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
| Issue #92 Phase 2A Backend Application / Core Boundary | **READY** | **EU-46 — Backend Application / Core Boundary Foundation；Readiness PASS；Execute NOT STARTED** |
| Issue #92 Phase 2B Generic Content Migration | Planning Candidate / downstream | 必须等待 EU-46 完成后重新 Planning / slice / readiness |
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
- EU-41 accepted active Flyway 为 current Generic V1/V2；后续 Generic Schema change 从 V3 append-only。

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
- Canonical Dataset 依赖 stable Site identity，不依赖临时 Runtime DB id 或 Public Renderer internals。

### 4. Replaceable Public Renderer

- `frontend/public-site` 是稳定 Public API / URL / Site Data Contract 的当前 Vue/Vite consumer；
- Public production source 不依赖 Admin endpoint；
- Renderer 不成为 Site Definition、Canonical Migration、Flyway 或 CMS Domain Authority；
- Main / Party canonical URL、产品 identity 与 accepted behavior在后续重构中保持。

## 当前 Planning Priority — Issue #92

总体规划：`docs/project/pre-e1e3-convergence-plan.md`。

Documentation Authority Map：`docs/README.md`。

Current Ready Execution Unit：**EU-46 — Backend Application / Core Boundary Foundation**。

Current EU-46 Authority：

- Requirement：`docs/requirements/backend-application-core-boundary.md`；
- Specification：`docs/specifications/backend-application-core-boundary.md`；
- Technical Plan：`docs/technical/backend-application-core-boundary.md`；
- Work artifact：`docs/work/current/eu46-backend-application-core-boundary-foundation.md`。

EU-46 Readiness：**PASS**；Execute：**NOT STARTED**。只有本 readiness change 集成后，在新的 Fresh Context 中重新读取最新 `main`、Issue #92 / #77、Open PR / Actions 与上述 Authority，并确认无 base drift / Authority change，才能进入 Execute。

### Phase 0 — Planning Authority Solidification — COMPLETED

Issue #92 / #77 / Roadmap / Site Package Planning 的总体路线已对齐。AR-02 / AR-04 证明 application/core boundary 必须先于 Gradle implementation choice冻结；模型 Review 只构成 Review Evidence，不替代 Repository Authority / readiness-check。

### Phase 1 — Repository Documentation Authority Convergence — COMPLETED

- EU-43 Current Authority Semantic Reconciliation：COMPLETED；
- EU-44 Canonical Product Authority Consolidation：COMPLETED；
- EU-45 Documentation Information Architecture & Archive Migration：COMPLETED。

Phase 1 classification contract `CURRENT / PARTIALLY_CURRENT / SUPERSEDED / HISTORICAL_EVIDENCE` 继续有效；archive 默认不参与 Fresh Context Current Authority 恢复。历史 work records 位于 `docs/work/archive/`。

### Phase 2A — Backend Application / Core Boundary Foundation — EU-46 READY

目标依赖：

```text
cms-server app ───────────→ cms-core
content-migration app ────→ cms-core
```

Dependency closure 已确认当前两个 Party import task 共用完整 `main.runtimeClasspath`，四个 migration implementation 都以 `CmsApplication` 为 Spring source；`WebApplicationType.NONE` 不能隔离 Server component scan。

Technical Planning 已按 Phase 0 obligation比较两个 build candidate，并选择标准 Gradle multi-project：

```text
backend/
├── modules/
│   └── cms-core/
└── apps/
    ├── cms-server/
    └── content-migration/
```

选择依据不是 executable JAR 数量，而是 current code 已存在 mixed transport/service files，任何方案都必须做最小 transport split；单-project source-set alternative 还需要额外自定义 classpath/resource/test/BootJar wiring，并不更低复杂度。标准 project dependency 能直接证明 migration 不含 server classpath。

EU-46 只做 behavior-preserving application/core foundation：

- Core 不依赖任一 app，两个 app 互不依赖；
- Server 持有 HTTP/MVC/static HTTP/server-only composition；
- Migration 持有 Party migration CLI/import/report/compatibility composition，并使用独立 non-web Spring root；
- Generic Flyway SQL、CMS metadata、Site Package lifecycle保持 single shared authority；
- Server JAR current repository consumer path/name保持；
- Party 183 Articles、4 carousel、idempotency、fingerprint conflict、EU-29→EU-30 compatibility 与 resource integrity保持；
- Canonical / Upgrade / Site Package / CI / Review workflow path/command wiring必须与 source move 原子同步；
- 不泛化 Party migration、不进入 2B/2C、不拆 Git Repository。

Readiness 已映射到 build/dependency proof、Server runtime、Migration non-web context、Canonical Migration Verification、EU-30 Upgrade Verification、Site Package Verification、Repository CI 与 Post-Integration Fresh Context evidence。

### Phase 2B — Generic Content Migration Application — PLANNING CANDIDATE

在 EU-46 完成后才能重新规划。目标链路：

```text
Legacy Source
→ collection / normalization / promotion
→ Canonical Migration Dataset
→ Generic JVM Content Migration Application
→ CMS Runtime
```

Generic Engine 负责 canonical validation、path/digest safety、stable migration identity/fingerprint、preflight、transaction/file-side-effect boundary、dependency order、import/reconciliation/report，但不得内建 Party / JilinJobs / EU-29 / EU-30 identity。

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
4. 读取 Issue #92、Issue #77 与 EU-46 Requirement / Specification / Technical Plan / Work artifact；
5. 重新确认 EU-46 Readiness、Execute baseline 与 base drift；
6. 只有 Readiness仍有效时才进入 EU-46 Execute。

当前不得从 Roadmap、历史 EU、Phase 名称或 Issue 编号推导 Phase 2B / 2C、Phase 3 或 E1～E3 Execute Authority。
