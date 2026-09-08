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
| Issue #92 Phase 2B Generic Content Migration | **已完成** | **EU-47 — Generic Content Migration Application Foundation 完成 Generic Canonical Dataset → CMS Runtime foundation** |
| Issue #92 Phase 2C Party Migration De-specialization | **已完成** | **EU-48 — Party Migration De-specialization & Compatibility 完成 Party dataset/profile/compatibility authority + Generic Engine consumer 收敛，并保持 accepted EU-29→current transition** |
| Issue #92 Phase 3 Compatibility / E1～E3 Re-entry | **Planning / compatibility gate — current next gate** | 完整链路重新对账后才能决定 re-entry PASS；当前无 Ready Execution Unit / Execute Authority |
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
- Party current canonical Runtime Dataset = 183 Articles；EU-29 frozen acceptedSnapshot = 181；4 条 accepted carousel 与 EU-29→current compatibility 必须持续可验证；
- Canonical Dataset 依赖 stable Site identity，不依赖临时 Runtime DB id 或 Public Renderer internals；
- EU-46 后 Historical Migration implementation 位于独立 `backend/apps/content-migration` application；
- EU-47 在同一 application 内建立 site-neutral Generic canonical load/preflight、Article/Resource/ListItem import、stable legacy mapping、dependency order、idempotency/conflict/report foundation；
- EU-48 将 Party current path收敛为 Party canonical dataset / bounded compatibility authority → Party adapter / exact compatibility guard → Generic Engine → CMS Runtime；Party current aliases/current fingerprints继续 dataset-owned，accepted old `fromFingerprint`由 bounded compatibility authority持有，Generic package不吸收 Party policy。

### 4. Replaceable Public Renderer

- `frontend/public-site` 是稳定 Public API / URL / Site Data Contract 的当前 Vue/Vite consumer；
- Public production source 不依赖 Admin endpoint；
- Renderer 不成为 Site Definition、Canonical Migration、Flyway 或 CMS Domain Authority；
- Main / Party canonical URL、产品 identity 与 accepted behavior在后续重构中保持。

## 当前 Planning Priority — Issue #92

总体规划：`docs/project/pre-e1e3-convergence-plan.md`。

Documentation Authority Map：`docs/README.md`。

Current Ready Execution Unit：**NONE**。

Current next gate：**Phase 3 — Canonical Migration Compatibility & E1～E3 Re-entry planning / compatibility gate**。Phase 3 必须从 current Repository Authority、Issue #92 / #77、Phase 2A～2C accepted Authority 与 Current Evidence独立恢复并完成 compatibility closure / re-entry verdict；不得从 EU-48 或更早 Unit 继承 Execute Authority。Issue #60 / E1～E3 在 re-entry PASS 前继续 blocked。

Phase 2C accepted Authority：

- Requirement：`docs/requirements/party-migration-despecialization-compatibility.md`；
- Specification：`docs/specifications/party-migration-despecialization-compatibility.md`；
- Technical Plan：`docs/technical/party-migration-despecialization-compatibility.md`；
- Completed Work artifact：`docs/work/archive/eu48-party-migration-despecialization-compatibility.md`。

Phase 2B accepted Authority：

- Requirement：`docs/requirements/generic-content-migration-application.md`；
- Specification：`docs/specifications/generic-content-migration-application.md`；
- Technical Plan：`docs/technical/generic-content-migration-application.md`；
- Completed Work artifact：`docs/work/archive/eu47-generic-content-migration-application-foundation.md`。

Phase 2A accepted Authority继续作为长期 application/core contract：

- Requirement：`docs/requirements/backend-application-core-boundary.md`；
- Specification：`docs/specifications/backend-application-core-boundary.md`；
- Technical Plan：`docs/technical/backend-application-core-boundary.md`；
- Completed Work artifact：`docs/work/archive/eu46-backend-application-core-boundary-foundation.md`。

EU-43 / EU-44 / EU-45 / EU-46 / EU-47 / EU-48 的 Execute Authority均已终止。任何后续 Planning / Compatibility Gate 不继承这些 Unit 的 Execute Authority。

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
- Migration 持有独立 non-web migration composition；
- Generic Flyway V1/V2、`cms-metadata.yml`、Site Package lifecycle保持 single Core resource/capability authority；
- root `backend` build/command compatibility与现有 Server JAR path保持；
- Party 183 Articles、4 carousel、idempotency、fingerprint conflict、EU-29→current compatibility 与 resource integrity保持；
- focused Backend Application Boundary Verification证明 packaged ownership 与 Migration non-web composition。

EU-46 Execute Authority已终止。

### Phase 2B — Generic Content Migration Application — COMPLETED VIA EU-47

EU-47 accepted Authority：

- Requirement：`docs/requirements/generic-content-migration-application.md`；
- Specification：`docs/specifications/generic-content-migration-application.md`；
- Technical Plan：`docs/technical/generic-content-migration-application.md`；
- Completed Work artifact：`docs/work/archive/eu47-generic-content-migration-application-foundation.md`。

Accepted chain：

```text
Legacy Source
→ collection / normalization / promotion
→ Canonical Migration Dataset
→ Generic JVM Content Migration Application
→ CMS Runtime
```

EU-47 implementation result：

- neutral Article/ListItem legacy mapping Mapper 从 Party source relocation 到 shared migration ownership，table/package/SQL semantics保持；
- 新增 site-neutral canonical Article/List models与 generic list schemas；
- 建立 `load → structural + byte preflight → Runtime target/mapping/dependency preflight → GenericImportPlan → execute → report`；
- preflight覆盖 root containment、index/item一致性、duplicate stable identity、file/size/SHA-256、canonical token/reference、Column/List stable target、mapping CREATE/SKIP/CONFLICT、ARTICLE stable dependency；known INVALID / CONFLICT 在 execute mutation前阻断；
- Generic Article支持 INTERNAL / EXTERNAL_LINK、BODY_IMAGE / ATTACHMENT、reference rewrite、create/publish/mapping；
- Generic ListItem支持 LINK / ARTICLE stable reference；LINK image使用 deterministic `/static/migrated/content/lists/<LIST_CODE>/<sha256>.<ext>`，ARTICLE image使用 managed Resource；
- 新增 `generic-content` dispatcher、root `importCanonicalContent`、`CONTENT_MIGRATION_REPORT` 与 failed CLI semantics；
- 新增 synthetic Fresh DB `verifyGenericContentMigration` 与 Generic Party-hardcode purity workflow；
- 保持 `content-migration → cms-core` only，不引入 Server transport、new app/module 或 DB schema change。

EU-47 completion只建立 Generic foundation；其 Execute Authority在 completion后终止。

### Phase 2C — Party Migration De-specialization & Compatibility — COMPLETED VIA EU-48

Accepted Authority：

- Requirement：`docs/requirements/party-migration-despecialization-compatibility.md`；
- Specification：`docs/specifications/party-migration-despecialization-compatibility.md`；
- Technical Plan：`docs/technical/party-migration-despecialization-compatibility.md`；
- Completed Work artifact：`docs/work/archive/eu48-party-migration-despecialization-compatibility.md`。

Accepted responsibility split：

```text
Party canonical dataset / compatibility authority
        ↓
Party bounded adapter / exact compatibility guard
        ↓
Generic Content Migration capability
        ↓
CMS Runtime
```

EU-48 implementation result：

- Party manifest / Article index / list index-items继续持有 current sourceSystem、scope aliases/counts、stable identities、order与 current fingerprints；Kotlin不再复制 alias allow-list/current fingerprint authority；
- 新增 bounded `data-migrations/party/v1/compatibility.json`，只保存 accepted old position-2 transition的 identity、old `fromFingerprint`、old source type与 preserve-runtime-id requirement；current target fingerprint/type/article relation/image digest仍直接来自 current canonical item；
- Party Article/List on-disk accepted shape由 `migration/party/**` bounded adapter验证和 normalization；steady-state CREATE/SKIP/CONFLICT委托 EU-47 Generic prepared-dataset pipeline；
- Generic Engine只增加 site-neutral prepared entry、shared file/path/digest verification、source provenance与 safe static-target override；未引入 Party identity、general update语义或 policy/plugin framework；
- accepted compatibility service仅对 compatibility authority授权且 old Runtime guard完全匹配的 position-2 LINK→ARTICLE transition做原位 update，保持 Runtime list-item id；任何 fingerprint、source type、order/url/image path/digest或 target Article dependency drift均 CONFLICT/no update；
- existing `party-content` / `party-carousel`、root Gradle task names、`EU29_IMPORT_REPORT` / `EU29_CAROUSEL_IMPORT_REPORT`保持；
- no DB schema/Flyway/API/frontend/Site Package behavior change。

Pre-integration exact-head verification覆盖 Party focused compatibility runtime guards、Canonical current 183+4 first/second import、pinned EU-29→current single UPDATE/id preservation/post-upgrade idempotency、Generic purity/behavior、Backend application boundary、Site Package与 Repository CI/Integrated Browser。最终 merge与 Post-Integration Current Evidence由 PR #109、Actions及 Issue #92/#77记录。

EU-48 completion后 Execute Authority终止；Phase 2C completion只使四层历史迁移边界满足进入 Phase 3 compatibility/re-entry planning gate 的前置，不自动给 Phase 3 或 Issue #60 / E1～E3 Execute Authority。

### Phase 3 — Canonical Migration Compatibility & E1～E3 Re-entry Gate

最终重新对账：

```text
Generic CMS Schema
→ JilinJobs Site Package stable structure
→ one-time Site bootstrap
→ Generic Content Migration Application
→ Party Canonical Dataset / compatibility authority
→ Runtime
→ Replaceable Public Renderer
```

当前仅进入 **Planning / Compatibility Gate**。必须基于 Phase 2A～2C accepted Authority 与实际 Current Evidence重新检查完整链路的 compatibility obligations；全部闭环后才可记录 E1～E3 re-entry PASS。发现具体 implementation gap 时才重新通过 `slice-work` 形成 Candidate EU。Phase 3 re-entry PASS 前不得进入 Issue #60 / E1～E3 Execute。

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
5. 若目标是继续下一 Gate，则从 **Phase 3 Canonical Migration Compatibility & E1～E3 Re-entry planning / compatibility gate** 恢复，并读取 Phase 2A～2C accepted Authority与最新 Current Evidence；已归档 EU-48 work artifact只用于 traceability；
6. 只有 Phase 3 compatibility/re-entry自身完成 Repository Authority要求的 Planning / Verification Gate 并明确形成后续 Ready Execution Unit / re-entry PASS 时，才允许进入对应后续动作。

当前不得从 EU-48完成事实、Phase名称、Issue编号或 Roadmap顺序推导 Phase 3 / Issue #60 / E1～E3 Execute Authority。
