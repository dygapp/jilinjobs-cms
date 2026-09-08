# CMS Core / Site Package / Public Renderer 规划状态

## 当前结论

Issue #77 继续承担 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层架构边界 Authority。

已完成并集成：EU-37～EU-42 Site Package Boundary、Issue #92 Phase 1 EU-43～EU-45 Documentation Authority Convergence，以及 Phase 2A **EU-46 — Backend Application / Core Boundary Foundation**。

Phase 2B 已基于 EU-46 后 current Content Migration / canonical workspace / workflow dependency closure完成 Requirement、Specification、Technical Planning、`slice-work` 与 `readiness-check`，形成 **EU-47 — Generic Content Migration Application Foundation**。

- Current Ready Execution Unit：**EU-47**；
- Readiness：**PASS**；
- Execute：**NOT STARTED**；
- Issue #77：**OPEN**；
- Phase 2C / Phase 3 与 Issue #60 / E1～E3：**downstream / no Execute Authority**。

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
Phase 2B Generic Content Migration Application — EU-47 READY / EXECUTE NOT STARTED
        ↓
Phase 2C Party Migration De-specialization & Compatibility — Planning Candidate
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
- Migration持有独立 non-web Party CLI/import/report/compatibility composition，不依赖或扫描 Server app；
- Generic Flyway、CMS metadata、Site Package lifecycle保持 single shared authority；
- current Server JAR repository consumer path/name保持；
- Party 183 Articles、4 carousel、idempotency、fingerprint conflict、EU-29→EU-30 compatibility、resource integrity与report semantics保持；
- Canonical / Upgrade / Site Package / CI workflow随 source move原子同步。

EU-46 Execute Authority已随 integration closure终止。

### Phase 2B / EU-47 — Generic Content Migration Application — READY

Current Authority：

- Requirement：`docs/requirements/generic-content-migration-application.md`；
- Specification：`docs/specifications/generic-content-migration-application.md`；
- Technical Plan：`docs/technical/generic-content-migration-application.md`；
- Ready Work artifact：`docs/work/current/eu47-generic-content-migration-application-foundation.md`。

Phase 2B dependency closure确认 current Party migration implementation同时包含两类语义：

1. 可复用的 site-neutral capability：canonical load/validation、path/digest safety、stable identity/fingerprint、preflight、Article/Resource/ListItem import、legacy mapping、idempotency/conflict/report；
2. 必须留给 Party compatibility authority 的事实：Party aliases、`PARTY_CAROUSEL`、固定 4 条、`party-carousel:position:*`、accepted fingerprints、EU-29→EU-30 position-2 LINK→ARTICLE upgrade exception。

EU-47 因此只建立 Existing `backend/apps/content-migration` 内的 Generic boundary，不新增 application/module/plugin framework：

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

Generic Engine不得内建 Party / JilinJobs / EU-29 / EU-30 identity；Article-backed ListItem必须以 stable article migration identity建立关系，不依赖 Runtime DB id。

EU-47 Ready scope还包括 neutral legacy mapping ownership、generic list schemas、`generic-content` dispatcher、root `importCanonicalContent`、`CONTENT_MIGRATION_REPORT`、synthetic Generic verifier与 purity proof。

Phase 2B 明确保持：

- `data-migrations/party/v1/**` accepted bytes不变；
- current Party article/carousel commands、183 Articles、4 accepted items、second-import idempotency、fingerprint conflict、EU-29→EU-30 compatibility与 `EU29_*_REPORT` semantics不变；
- no DB schema / Flyway / API / frontend / Site Package semantic change。

Readiness：**PASS**。Execute：**NOT STARTED**。只有本 planning/readiness change集成后，在新的 Fresh Context确认 EU-47 integrated baseline、Authority、Open PR / Actions与 base drift仍有效，才允许进入 Execute。

### Phase 2C — Party Migration De-specialization / Compatibility

EU-47 Generic Engine完成后，Party-specific aliases、accepted fingerprints、carousel legacy transition与upgrade-only policy收敛到 Party dataset/profile/compatibility authority，并让 Party current canonical path消费已验证的 Generic capability。

保持义务至少包括：183 Party current Articles、4 accepted carousel、second import idempotency、changed fingerprint conflict、EU-29→EU-30 compatibility、resource integrity与Runtime reconciliation。

Phase 2C 必须重新完成自身 dependency closure / Requirement / Specification / Technical Planning / `slice-work` / `readiness-check`，不得继承 EU-47 Execute Authority。

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

全部 Current Evidence闭环时可以记录 compatibility closure / E1～E3 re-entry PASS；只有存在具体 implementation gap 时才形成新的 Candidate EU。

## Repository Split Readiness Assessment

继续 deferred。只有四层 boundary 与 final compatibility 完成后才独立评估；Assessment 不等于自动拆仓，也不默认阻塞 E1～E3。

## 当前 Gate

Current Ready Execution Unit：**EU-47 — Generic Content Migration Application Foundation**。

Planning/readiness change 集成后，下一实际 Gate：

1. 从最新 `main` 重新恢复 `AGENTS.md`、README、Roadmap、Development Method、Issue #92 / #77 与 EU-47 Current Authority；
2. 核验 Open PR / Actions 与 base drift；
3. 只有 EU-47 Readiness仍有效且没有 Authority change / blocker 时进入 Execute；
4. 不得继承 EU-46 或更早历史 Execute Authority；
5. EU-47不得扩展到 Phase 2C、Phase 3 或 Issue #60 / E1～E3。