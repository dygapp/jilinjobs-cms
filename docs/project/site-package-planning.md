# CMS Core / Site Package / Public Renderer 规划状态

## 当前结论

Issue #77 继续承担 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层架构边界 Authority。

已完成并集成的长期基础包括：EU-37～EU-42 Site Package Boundary、Issue #92 Phase 1 EU-43～EU-45 Documentation Authority Convergence、Phase 2A **EU-46 — Backend Application / Core Boundary Foundation**，以及 Phase 2B **EU-47 — Generic Content Migration Application Foundation** 的 implementation / verification / integration closure candidate。

EU-47 已建立 site-neutral Generic Canonical Dataset → CMS Runtime capability，并保持 Party current dataset/profile/compatibility path 不变。

- Current Ready Execution Unit：**NONE**；
- EU-47 Execute Authority：**TERMINATED on completion**；
- Issue #77：**OPEN**；
- Current next gate：**Phase 2C — Party Migration De-specialization & Compatibility Planning Candidate**；
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
Phase 2C Party Migration De-specialization & Compatibility — CURRENT PLANNING CANDIDATE
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

### Phase 2C — Party Migration De-specialization / Compatibility — CURRENT PLANNING CANDIDATE

下一阶段只能从 Planning 开始。目标是在 EU-47 Generic Engine boundary 已成立的前提下，将 Party-specific aliases、accepted fingerprints、carousel legacy transition与upgrade-only policy收敛到 Party dataset/profile/compatibility authority，并让 Party current canonical path消费已验证的 Generic capability。

保持义务至少包括：183 Party current Articles、4 accepted carousel、second import idempotency、changed fingerprint conflict、EU-29→EU-30 compatibility、resource integrity与Runtime reconciliation。

Phase 2C 当前没有 Candidate Execution Unit、Ready Execution Unit 或 Execute Authority。必须重新完成自身 dependency closure / Requirement / Specification / 必要 Technical Planning / `slice-work` / `readiness-check`，不得继承 EU-47 Execute Authority。

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

Current Ready Execution Unit：**NONE**。

下一实际 Gate仅为 Issue #92 / #77 下的 **Phase 2C Planning Candidate**：

1. 从最新 `main` 重新恢复 `AGENTS.md`、README、Roadmap、Development Method、Issue #92 / #77 与当前 Planning Authority；
2. 重新执行 Phase 2C dependency closure，并形成 Requirement / Specification / 必要 Technical Planning；
3. 只有 `slice-work` 形成 Candidate Execution Unit 且 `readiness-check` PASS 后，才可能取得新的 Execute Authority；
4. `docs/work/archive/eu47-generic-content-migration-application-foundation.md` 只承担历史完成证据，不授予 Phase 2C Execute；
5. 不得提前进入 Phase 3 或 Issue #60 / E1～E3。
