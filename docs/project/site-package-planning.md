# CMS Core / Site Package / Public Renderer 规划状态

## 当前结论

Issue #77 继续承担 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层架构边界 Authority。

已完成并集成的长期基础包括：EU-37～EU-42 Site Package Boundary、Issue #92 Phase 1 EU-43～EU-45 Documentation Authority Convergence、Phase 2A **EU-46 — Backend Application / Core Boundary Foundation**，以及 Phase 2B **EU-47 — Generic Content Migration Application Foundation**。

EU-47 已建立 site-neutral Generic Canonical Dataset → CMS Runtime capability；Phase 2C 当前已完成 dependency closure、Requirement / Specification / Technical Planning、`slice-work` 与 `readiness-check`，形成 **EU-48 — Party Migration De-specialization & Compatibility**。

- Current Ready Execution Unit：**EU-48 — Party Migration De-specialization & Compatibility**；
- Readiness：**PASS**；Execute：**NOT STARTED**；
- EU-47 Execute Authority：**TERMINATED**；EU-48 不继承 EU-47 或更早 Unit 的 Execute Authority；
- Issue #77：**OPEN**；
- Current next gate：**planning/readiness integration → Fresh Context EU-48 execute revalidation**；
- Phase 3 与 Issue #60 / E1～E3：**downstream / no Execute Authority**。

跨 Issue 总体演进路线由 GitHub Issue #92 与 `docs/project/pre-e1e3-convergence-plan.md` 承载。Repository Documentation Authority Convergence 属于 Issue #92 的 Repository Governance / Knowledge Architecture 范围，不并入 Issue #77 产品架构 Requirement。

## 已完成的四层边界基础

### EU-37～EU-39 — Contract / stable structure / Navigation identity

已建立 Site Package manifest、narrow idempotent provisioner，并把 Column、PageGroup、Page、NavigationLocation、SiteConfig、CmsList definition、AdvertisementSlot 与稳定 NavigationItem 表达为具有 stable identity 的 JilinJobs Site Package structure。

### EU-40 — Explicit Runtime composition

Repository Runtime / importer 已可以在 Flyway 后显式执行 `cms.site-package.root` Site Package reconcile；未配置 root 时 Generic CMS context 不隐式启用 Site Package。

### EU-41 — Site bootstrap / Generic Schema baseline separation

```text
Generic CMS Backend
  Flyway Schema-only lineage
        ↓
JilinJobs Site Package
  stable structure reconcile
        ↓
  one-time current-schema bootstrap
        ↓
optional Historical Canonical Migration
        ↓
Runtime
```

Backend active migration target仍为 current Generic V1/V2；JilinJobs data 不占用 Backend migration number。Fresh Site bootstrap完成后其初始运营数据成为 ordinary operator-managed Runtime Data，repeated bootstrap / ordinary restart 不 overwrite 或 resurrect。

### EU-42 — Stable Site asset ownership / Runtime projection

- `sites/jilinjobs/assets/**` 是 stable JilinJobs Site asset 的唯一版本化 source owner；
- manifest 固化 source、公开 `/static/**` target 与 SHA-256；
- Runtime projection 只 create missing targets；
- stable targets 进入 StaticResource protected-path；
- `/static/uploads/**` 与 Historical Canonical Migration assets 不被 stable Site asset ownership接管。

历史完成证据位于 `docs/work/archive/` 与 GitHub Current Evidence；archive record 不重新授予 Execute Authority。

## 当前剩余架构顺序

```text
Phase 2A Backend Application / Core Boundary — EU-46 COMPLETED
        ↓
Phase 2B Generic Content Migration Application — EU-47 COMPLETED
        ↓
Phase 2C Party Migration De-specialization & Compatibility — EU-48 READY / EXECUTE NOT STARTED
        ↓
Phase 3 Canonical Migration Compatibility & E1～E3 Re-entry Gate
        ↓
Repository Split Readiness Assessment（独立后置）
```

### Phase 2A / EU-46 — Backend Application / Core Boundary — COMPLETED

Accepted Authority：

- `docs/requirements/backend-application-core-boundary.md`；
- `docs/specifications/backend-application-core-boundary.md`；
- `docs/technical/backend-application-core-boundary.md`；
- `docs/work/archive/eu46-backend-application-core-boundary-foundation.md`。

Accepted dependency：

```text
cms-server app ───────────→ cms-core
content-migration app ────→ cms-core
```

EU-46 已通过标准 Gradle multi-project建立：

```text
backend/
├── modules/
│   └── cms-core/
└── apps/
    ├── cms-server/
    └── content-migration/
```

并保持：

- Core 不依赖 app，两个 app 互不依赖；
- Server持有 HTTP/MVC/static HTTP/server-only composition；
- Migration持有独立 non-web migration composition，不依赖或扫描 Server app；
- Generic Flyway、CMS metadata、Site Package lifecycle保持 single shared authority；
- current Server JAR repository consumer path/name保持。

EU-46 Execute Authority已随 integration closure终止。

### Phase 2B / EU-47 — Generic Content Migration Application — COMPLETED

Accepted Authority：

- Requirement：`docs/requirements/generic-content-migration-application.md`；
- Specification：`docs/specifications/generic-content-migration-application.md`；
- Technical Plan：`docs/technical/generic-content-migration-application.md`；
- Completed Work artifact：`docs/work/archive/eu47-generic-content-migration-application-foundation.md`。

EU-47 在 existing `backend/apps/content-migration` 内建立了 Generic boundary，而没有新增 application/module/plugin framework：

```text
Canonical Migration Dataset
        ↓
load / structural+byte preflight
        ↓
Runtime target / mapping / dependency preflight
        ↓
Generic Article / ListItem import
        ↓
CMS Runtime + stable legacy mapping + report
```

Accepted capability包括：

- neutral Article/ListItem legacy mapping ownership；
- site-neutral canonical Article/List models 与 generic list schemas；
- normalized root-contained path、index/item一致性、duplicate identity、resource size/SHA-256、canonical reference、Column/List target、mapping CREATE/SKIP/CONFLICT 与 ARTICLE dependency preflight；
- INTERNAL / EXTERNAL_LINK Article、BODY_IMAGE / ATTACHMENT、LINK / ARTICLE ListItem；
- ARTICLE-backed ListItem通过 stable article migration identity建立关系；
- LINK image使用 deterministic `/static/migrated/content/lists/<LIST_CODE>/<sha256>.<ext>`，ARTICLE image使用 managed Resource；
- `generic-content` dispatcher、root `importCanonicalContent`、`CONTENT_MIGRATION_REPORT`；
- Fresh DB Generic verifier、Generic source purity proof与 non-web application boundary verification。

Phase 2B 同时保持：

- `data-migrations/party/v1/**` accepted bytes不变；
- current Party article/carousel commands、183 Articles、4 accepted items、second-import idempotency、fingerprint conflict、EU-29→EU-30 compatibility与 `EU29_*_REPORT` semantics不变；
- Party aliases、`PARTY_CAROUSEL`、fixed four items、accepted fingerprints与 position-2 compatibility exception未迁入 Generic Engine；
- no DB schema / Flyway / API / frontend / Site Package semantic change。

EU-47 Execute Authority在 Unit completion 后终止，不传递给 Phase 2C。

### Phase 2C / EU-48 — Party Migration De-specialization & Compatibility — READY

Current Authority：

- Requirement：`docs/requirements/party-migration-despecialization-compatibility.md`；
- Specification：`docs/specifications/party-migration-despecialization-compatibility.md`；
- Technical Plan：`docs/technical/party-migration-despecialization-compatibility.md`；
- Ready Work artifact：`docs/work/current/eu48-party-migration-despecialization-compatibility.md`。

Planning baseline `main@bd5dbd84bafd7b731ca290bbd40fb135806cb086` 的 dependency closure确认：

- current Party Article path仍复制 Generic Article Runtime mutation，并维护 aliases/theme special-case hardcode；
- current Party Carousel path仍复制 Generic List mutation，并维护 `PARTY_CAROUSEL`、legacy-key/fixed-count/static-path及 accepted position-2 fingerprints；
- current manifest/index/items已经持有 Party current scope、identity/current fingerprints；
- pinned EU-29 accepted commit与 EU-30 Upgrade workflow证明唯一 accepted position-2 LINK→ARTICLE historical transition；
- Phase 2C只需要 Party-owned compatibility authority、bounded Party canonical adapter以及 Generic prepared-dataset/shared-verifier/site-neutral metadata薄扩展；无需 DB schema、new app/module或 generic policy framework。

Accepted target：

```text
Party canonical dataset / compatibility authority
        ↓
Party bounded adapter / exact compatibility guard
        ↓
Generic Content Migration capability
        ↓
CMS Runtime
```

`slice-work`形成单一 **EU-48 — Party Migration De-specialization & Compatibility**，`readiness-check` = **PASS**。

EU-48必须保持：

- current 183 Party Articles + 4 carousel；
- fresh first import / second idempotency / unexpected fingerprint conflict；
- resource integrity与Runtime reconciliation；
- pinned EU-29 181 + 4 old baseline可导入；
- current upgrade只允许 accepted position-2原位 UPDATE，preserve list-item id；
- post-upgrade current rerun全 SKIP；
- Generic package继续无 Party identity / fingerprint / upgrade exception；
- existing Party commands/tasks/report labels保持；
- no DB schema/API/frontend/Site Package behavior change。

EU-48 Execute = **NOT STARTED**。本 planning/readiness change集成后，必须由新的 Fresh Context重新核验 integrated `main`、Issue #92 / #77、EU-48 Authority、Open PR / Actions 与 base drift；无 Authority change / blocker时才建立独立 Execute baseline。不得从 EU-47或本 planning branch继承 Execute Authority。

### Phase 3 — Compatibility & E1～E3 Re-entry

```text
Generic CMS Schema
→ JilinJobs Site Package stable structure
→ one-time Site bootstrap
→ Generic Content Migration Application
→ Party Canonical Dataset
→ Runtime
→ Replaceable Public Renderer
```

全部 Current Evidence闭环时可以记录 compatibility closure / E1～E3 re-entry PASS；只有存在具体 implementation gap 时才形成新的 Candidate EU。Phase 3 PASS 前不得进入 Issue #60 / E1～E3 Execute。

## Repository Split Readiness Assessment

继续 deferred。只有四层 boundary 与 final compatibility 完成后才独立评估；Assessment 不等于自动拆仓，也不默认阻塞 E1～E3。

## 当前 Gate

Current Ready Execution Unit：**EU-48 — Party Migration De-specialization & Compatibility**。

Readiness：**PASS**；Execute：**NOT STARTED**。

下一实际 Gate：

1. 先完成本 Phase 2C planning/readiness change 的 Integration 与 Post-Integration Current Evidence；
2. 新的 Fresh Context从 integrated `main`重新恢复 `AGENTS.md`、README、Roadmap、Development Method、Issue #92 / #77 与 EU-48 Requirement / Specification / Technical Plan / Work Artifact；
3. revalidate EU-48 Readiness、Open PR / Actions 与 base drift；
4. 只有无 Authority change / blocker时才允许建立 EU-48自身 Execute baseline并进入 Execute；
5. `docs/work/archive/eu47-generic-content-migration-application-foundation.md` 只承担历史完成证据，不授予 EU-48 Execute；
6. 不得提前进入 Phase 3 或 Issue #60 / E1～E3。
