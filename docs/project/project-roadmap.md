# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap。它只维护当前演进状态、长期边界与下一 Planning Gate；详细 Execution Unit、Verification 与阶段追溯由 `docs/work/archive/**`、GitHub Issue / PR / Actions Current Evidence 承载。

## 方法基线

```text
dygapp/agentic-dev
Validation Baseline: master@d9fad0da83dbdb61cac5eb9778b0258c6861eef1
Capability Milestone: baseline-2026-09-04-engineering-capability@5be2e6aad29b2be6b8535b3690daf3533ee22a46
```

普通开发优先读取 Consumer-local `AGENTS.md`、`README.md`、`docs/README.md`、`docs/project/development-method.md`、本 Roadmap 与当前 Requirement / Specification / Technical Plan。Planning / Requirement Candidate 在 `slice-work` 前不具有 Execution Unit 身份；只有 `slice-work` 形成 Candidate Execution Unit 且 `readiness-check` PASS 后才允许进入 Execute。Roadmap 顺序、Issue 编号、未来 EU 名称或模型评审结果都不能替代 Readiness。

## 总体路线

| 路线 | 状态 | 当前结果 / Gate |
|---|---|---|
| Consumer Repository Bootstrap ～ Public / Admin 基础能力 | 已完成 | 通用 CMS、独立 Admin / Public frontend、Main / Party Multi-entry、Review / Verification 基础已建立 |
| 中心党建正式页面与历史内容 EU-26～EU-29 | 已完成 | Party 正式栏目、视觉、181 篇 EU-29 acceptedSnapshot 与 canonical migration 基础 |
| EU-30 Carousel Architecture & Behavior Convergence | 已完成 | Party current canonical Runtime Dataset = 183；4 条 accepted carousel；EU-29→EU-30 upgrade compatibility 已接受 |
| EU-31 Database Migration Baseline Convergence | 已完成（历史 baseline） | development DB recreation boundary 与 canonical migration independence 保留；active schema shape 后由 EU-41 修订 |
| EU-32～EU-35 Admin Governance / Rich Text | 已完成 | List definition governance、Admin guidance responsibility、HTML safety、shared Tiptap authoring |
| EU-36 Public Frontend Source Isolation | 已完成 | Public production source 退出 Admin endpoint knowledge，managed resource projection 由 Backend Public contract 承担 |
| EU-37～EU-42 Site Package Boundary | 已完成当前 accepted scope | Site Package contract、stable structure、Navigation identity、Runtime composition、one-time bootstrap、Generic Schema separation、stable asset ownership 均闭环 |
| Issue #92 Phase 0～1 Repository Authority Convergence | **已完成** | Phase 0 Planning Authority 与 Phase 1 EU-43 / EU-44 / EU-45 均完成；Current Ready Execution Unit = **NONE** |
| Issue #92 Phase 2 Generic Historical Migration & Backend Application Boundary | **下一 Planning Candidate** | 尚未形成 Candidate / Ready Execution Unit；必须独立完成 Planning / `slice-work → readiness-check` |
| Issue #60 / E1～E3 Main Site Formal Content | 前置依赖等待 | 等待 Issue #92 Phase 2 与 Phase 3 final compatibility / re-entry gate |
| Repository Split Readiness Assessment | 后置 Planning Candidate | 只有四层 boundary 与 compatibility 收口后独立评估；不自动拆仓，不默认阻塞 E1～E3 |
| Issues #57 / #59 / #60 其他候选 | 规划层保留 | C1/C2、Browser Compatibility、Public Rendering Architecture 等保持独立，不因 Issue #92 自动扩大 |

## 当前已接受长期边界

### 1. Generic CMS Core

- Backend Flyway 只承担 Generic CMS Schema evolution 与 site-neutral provisioning capability；
- Generic CMS 不要求 `notice`、`party`、`guide`、`HOME_CAROUSEL` 或其他 JilinJobs-specific identity 才能成立；
- `preset`、stable identity、bootstrap-state、resource / article / list 等能力可以属于 Generic CMS，但具体 Site 实例值不属于 Core；
- 从 EU-41 accepted baseline 起，下一次 Generic CMS Schema change 从 Backend V3 继续 append-only。

当前 active Flyway：

```text
backend/src/main/resources/db/migration/
├── V1__current_cms_schema.sql
└── V2__site_provisioning_schema_capabilities.sql
```

### 2. JilinJobs Site Package / Provisioning

```text
sites/jilinjobs/
├── structure/**   # stable Site structure reconcile
├── bootstrap/**   # one-time Fresh Site operational defaults
└── assets/**      # stable Site assets + integrity manifest
```

- stable structure 与 operator-managed Runtime Data 分离；
- one-time bootstrap 完成后普通 Runtime / repeated bootstrap 不 overwrite 或 resurrect operator data；
- stable Site assets 使用 manifest SHA-256 + create-if-missing projection + protected-path contract；
- `/static/uploads/**` 不属于 stable Site asset ownership。

### 3. Historical Content Migration

- `data-migrations/**` 承载 Main / Party 历史运营内容、资源、外链、legacy identity / fingerprint 与 provenance；
- Historical Migration 不等同于 Flyway，也不等同于 Site Package stable structure / one-time bootstrap；
- Canonical Dataset 应依赖 stable Site identity，不依赖临时 Runtime DB id 或 Public Renderer 内部实现；
- Party current canonical Runtime Dataset = 183 Articles；EU-29 frozen acceptedSnapshot = 181；4 条 accepted carousel 与 EU-29→EU-30 upgrade-only compatibility 必须长期保持可验证。

### 4. Replaceable Public Renderer

- 当前 `frontend/public-site` 是稳定 Public API / URL / Site Data Contract 的一个 Vue/Vite consumer；
- Public production source 不依赖 `/api/admin/**`；
- Public Renderer 不成为 Site Definition、Canonical Migration、Flyway 或 CMS Domain Authority；
- 当前 Main / Party canonical URL、产品 identity 与已接受行为在后续边界重构中保持。

## 当前 Planning Priority — Issue #92

总体规划：`docs/project/pre-e1e3-convergence-plan.md`。

Documentation Authority Map：`docs/README.md`。

Phase 1 audit / classification / closure Authority：`docs/project/documentation-authority-convergence.md`。

Current Ready Execution Unit：**NONE**。

Issue #77 继续承担四层产品 / 技术边界；Issue #92 承担跨 Repository Documentation Governance、Historical Migration / Backend Application Boundary 与 Issue #60 re-entry 的总体顺序。

### Phase 0 — Planning Authority Solidification — COMPLETED

Phase 0 已完成 Issue #92 / #77 / Roadmap / Site Package Planning 的总体路线对齐，并通过 AR-02 / AR-04 bounded architecture review 校正 Phase 2A planning。模型 Review 只构成 Review Evidence，不替代 Repository Authority / Readiness；跨项目方法候选已通过 `dygapp/agentic-dev` Issue #71 反馈，不构成本 Consumer 的额外 Method Authority。

### Phase 1 — Repository Documentation Authority Convergence — COMPLETED

Phase 1 已完成从语义 Currentness 到物理 Documentation Information Architecture 的完整闭环：

1. **EU-43 — Current Authority Semantic Reconciliation — COMPLETED**：修复四层 lifecycle / ownership / currentness drift；
2. **EU-44 — Canonical Product Authority Consolidation — COMPLETED**：`information-publishing.md` 成为 consolidated V4.9，EU-30 amendment 降为 `SUPERSEDED / TRACEABILITY`，CMS Core / Backend Authority 对齐 current Site/Schema ownership；
3. **EU-45 — Documentation Information Architecture & Archive Migration — COMPLETED**：Root README 收敛为稳定入口，`docs/README.md` 成为 Authority Map，Requirements / Specifications / Technical 与 Work 建立 Current / archive 物理边界，supporting method note 移入 `docs/project/`，Current links / subtree README / gate locator 同步收口。

Phase 1 分类 contract 继续有效：`CURRENT / PARTIALLY_CURRENT / SUPERSEDED / HISTORICAL_EVIDENCE`。只有 Phase 1 已明确为 `SUPERSEDED / HISTORICAL_EVIDENCE` 的文档进入 archive；`PARTIALLY_CURRENT` 且仍承担当前语义的文档继续留在 Current 根级。

Completed work artifacts 位于：

```text
docs/work/archive/eu43-current-authority-semantic-reconciliation.md
docs/work/archive/eu44-canonical-product-authority-consolidation.md
docs/work/archive/eu45-documentation-information-architecture.md
```

`docs/**/archive/**` 与 `docs/work/archive/**` 默认不参与 Fresh Context Current Authority 恢复。EU-43 / EU-44 / EU-45 Execute Authority 均已随完成终止，不延伸到 Phase 2 或 E1～E3。

### Phase 2 — Generic Historical Migration & Backend Application Boundary — PLANNING CANDIDATE

Phase 2 目前只有 Planning Authority，没有 Candidate / Ready Execution Unit。下一会话必须先重新核验最新 `main`、Issue #92 / #77、Open PR / Actions 与当前 Authority，再从 Phase 2 dependency closure / Specification / Technical Planning 的实际下一步开始；不得把下述 Planned Unit 名称当成 Execute Authority。

#### Planned Unit 2A — Backend Application / Core Boundary Foundation

2A 需要冻结 application lifecycle、Spring composition、compile/runtime dependency 与 artifact responsibility，不预先把某个 Gradle 目录形态当成 Requirement。

目标依赖：

```text
cms-server app ──────────→ cms-core
content-migration app ───→ cms-core
```

当前推荐 Architecture Candidate：

```text
backend/
├── modules/
│   └── cms-core/
└── apps/
    ├── cms-server/
    └── content-migration/
```

但两个 executable JAR 本身不逻辑必然要求三个 Gradle project。Technical Planning 必须同时比较 `shared core source set + isolated server/migration source sets + independent BootJar` 的较低复杂度替代；如果能以少量、清晰、长期可维护的 wiring 达到同等 dependency / classpath / Spring composition / resource / test isolation，则允许选择。只在同一个完整 runtime classpath 上增加多个 main / BootJar 不满足长期边界。

无论最终 build shape 如何，都必须：

- `cms-core` 不依赖任一 app，两个 app 互不依赖；
- `cms-server` 持有 HTTP / MVC / static-resource / server-only startup composition；
- `content-migration` 持有 CLI / import / report / migration compatibility composition；
- migration app 不再通过完整 `CmsApplication` 根包扫描获得 Server composition；
- 显式冻结 MyBatis mapper / resources、configuration properties、Jackson、transaction、Generic Flyway 单一 Authority、Site Package lifecycle 与 resource path；
- 区分数据库 transaction 与 migration 文件副作用的 failure / rollback semantics；
- 2A 只做行为保持型 application/core boundary，当前 Party migration entry / V1/V2 compatibility 不在本单元提前泛化；
- CMS Core 内 content / column / listing / resource / navigation 等继续使用 package-level modularity；
- 不扩展为一领域一 Gradle module、Clean Architecture、plugin framework 或 Git Repository split。

详细 ownership、composition、behavior-preservation 与 verification obligations 以 `docs/project/pre-e1e3-convergence-plan.md` 为准。

#### Planned Unit 2B — Generic Content Migration Application

```text
Legacy Source
  → Python / Node collection / normalization / promotion
  → Canonical Migration Dataset
  → JVM Generic Content Migration Application
  → CMS Runtime
```

JVM application 负责 canonical format validation、path / digest safety、stable migration identity / fingerprint、preflight、transaction / file-side-effect boundary、dependency order、Article / Resource / ListItem import、legacy mapping、idempotency、conflict、reconciliation 与 report。

Generic Engine 不得内建 Party / JilinJobs / EU-29 / EU-30 identity。

#### Planned Unit 2C — Party Migration De-specialization & Compatibility

- 当前 Backend production main 的 `Party*Migration*` 应退出 `cms-server` Runtime responsibility；
- Party-specific aliases、accepted fingerprints、carousel legacy transition / upgrade-only policy 继续由 Party dataset / profile / compatibility authority 承载；
- 同一 Generic Migration Application 必须保持 183 Articles、4 carousel、二次幂等、conflict、resource integrity 与 EU-29→EU-30 compatibility。

### Phase 3 — Canonical Migration Compatibility & E1～E3 Re-entry Gate

最终链路：

```text
Generic CMS Schema
  → JilinJobs Site Package stable structure
  → one-time Site bootstrap
  → Generic Content Migration Application
  → Party Canonical Dataset
  → Runtime
  → Replaceable Public Renderer
```

若 Current Evidence 已闭环全部 obligation，则直接记录 compatibility closure / E1～E3 re-entry PASS；只有发现具体 implementation gap 才通过 `slice-work` 形成新的 Candidate Execution Unit。Phase 3 PASS 前不得进入 Issue #60 / E1～E3 Execute。

### Phase 4 — Repository Split Readiness Assessment

继续 deferred，只有四层 boundary 与 final compatibility 完成后独立评估。Assessment 不等于自动拆仓。

## Architecture Review Eval Experiment

Consumer-local 实验入口仍位于 `evals/architecture/**`。当前结论保持 **ADJUST**：只在真实高返工成本架构问题具有独立挑战价值时使用 bounded eval；默认 lower-cost capable review first，只有 unresolved / conflicting / deliberate second opinion 时才升级 Astra。模型 review 只构成 Review Evidence，不覆盖 Repository Authority / readiness-check。

AR-02 与 AR-04 paired results、usage、model observability limitation 与跨项目反馈 lineage 见 `evals/README.md`、Issue #92 与 `dygapp/agentic-dev` Issue #71。

## 已完成里程碑摘要

| 日期 | 里程碑 |
|---|---|
| 2026-08-24～2026-09-01 | Feature-wide、Review Environment、主站视觉、Admin / Public frontend architecture 当前阶段完成 |
| 2026-09-02～2026-09-04 | Party EU-26～EU-29 完成，181 篇 acceptedSnapshot 与历史资源进入 canonical authority |
| 2026-09-05 | EU-30 完成；Party current canonical Runtime Dataset = 183，4 carousel 与 upgrade compatibility 接受 |
| 2026-09-05 | EU-31～EU-35 完成 Database baseline、List/Admin governance、Rich Text safety / authoring |
| 2026-09-06 | EU-36 完成 Public source isolation；Issue #77 成为 E1～E3 前置四层边界入口 |
| 2026-09-06～2026-09-07 | EU-37～EU-42 完成 Site Package contract、stable structure、Runtime composition、Schema/bootstrap separation 与 stable assets |
| 2026-09-07 | Issue #92 Phase 0 完成；总体路线与 bounded architecture-review evidence 收口 |
| 2026-09-07 | Phase 1 EU-43 / EU-44 / EU-45 完成 Repository Documentation Authority semantic + physical IA 收敛；Current Ready Execution Unit 返回 NONE |

## 其他 Planning Candidates

- Issue #60：C1 Loading / Skeleton、C2 Mobile Layout Human Review、E1～E3 Main formal content；
- Issue #59：Browser Compatibility 等后置候选；
- Issue #57：Public Rendering Architecture 讨论候选。

上述候选不因 Issue #92 自动扩张或晋升。

## Fresh Context 恢复入口

默认按当前 Repository Authority 恢复，而不是从聊天提示词复制状态：

1. `AGENTS.md`
2. `README.md`
3. `docs/README.md`
4. `docs/project/project-roadmap.md`
5. `docs/project/development-method.md`
6. `docs/project/pre-e1e3-convergence-plan.md`
7. 当前阶段直接相关 Requirement / Specification / Technical Plan / Execution Unit
8. GitHub Issue #92；涉及四层架构时同时读取 Issue #77；Issue #60 只用于 E1～E3 / C1/C2 等 downstream candidates
9. 当前 Branch / PR / Actions / Runtime Evidence

Current Ready Execution Unit：**NONE**。

下一实际步骤：从最新 `main` 重新恢复 Issue #92 / #77 与 Phase 2 Planning Authority，确认没有新的 drift 后，进入 **Phase 2 Planning Candidate** 的 dependency closure / Specification / Technical Planning。不得继承 EU-45 Execute Authority，也不得直接进入 Phase 2 Execute 或 Issue #60 / E1～E3。