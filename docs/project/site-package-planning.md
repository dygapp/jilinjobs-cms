# CMS Core / Site Package / Public Renderer 规划状态

## 当前结论

Issue #77 继续承担 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层架构边界 Authority。

已完成并集成：

- Slice A：**EU-37 — Site Package Contract & Provisioner Foundation**；
- Slice B stable structure：**EU-38 — Stable Site Structure Package Migration**；
- Slice B Navigation identity：**EU-39 — Navigation Stable Identity & Site Package Reconcile**；
- Slice B Runtime composition：**EU-40 — Explicit Site Package Runtime Composition Activation**；
- Slice B operational bootstrap / schema separation：**EU-41 — Site Bootstrap & Generic Schema Baseline Separation**；
- Slice C stable asset ownership / runtime projection：**EU-42 — Site Asset Package Ownership & Runtime Projection**。

当前没有 Ready / executing Execution Unit。Issue #77 保持 OPEN。

EU-42 后 Repository audit 已确认：旧的“Slice D — Canonical Migration Compatibility & E1～E3 Re-entry”不能再被视为无前置工作的直接下一步。Historical Migration / Backend Runtime 尚需先形成独立 application / artifact boundary，并把 Party-specific migration knowledge 从 production Runtime 中退出。

跨 Issue 总体演进路线由：

- GitHub Issue #92；
- `docs/project/pre-e1e3-convergence-plan.md`

共同承载。Repository Documentation Authority Convergence 属于 Issue #92 的 Repository Governance / Knowledge Architecture 范围，不并入 Issue #77 的产品架构 Requirement。

## 已完成的四层边界基础

### EU-37～EU-39 — Contract / stable structure / Navigation identity

已建立 Site Package manifest、narrow idempotent provisioner，并把 Column、PageGroup、Page、NavigationLocation、SiteConfig、CmsList definition、AdvertisementSlot 与 40 条 NavigationItem 表达为具有 stable identity 的 JilinJobs Site Package structure。

### EU-40 — Explicit Runtime composition

正式 Repository Runtime / importer 已可以在 Flyway 后显式执行 `cms.site-package.root` Site Package reconcile；未配置 root 时 Generic CMS context 不隐式启用 Site Package。

### EU-41 — Site bootstrap / Generic Schema baseline separation

EU-41 已接受：

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

Backend active migration target：

```text
backend/src/main/resources/db/migration/
├─ V1__current_cms_schema.sql
└─ V2__site_provisioning_schema_capabilities.sql
```

新的 V2 只承担 Generic Schema capabilities。JilinJobs data 不再占用 Backend migration number；下一次 Generic Schema change 从 V3 继续 append-only。

Site bootstrap：

```text
sites/jilinjobs/bootstrap/
├─ manifest.json
└─ initial-data.sql
```

七条 initial operational defaults 初始化后成为 ordinary operator-managed Runtime Data；repeated bootstrap / ordinary restart 不 overwrite 或 resurrect。

### EU-42 — Stable Site asset ownership / Runtime projection

EU-42 accepted boundary：

- `sites/jilinjobs/assets/**` 是稳定 JilinJobs Site asset 的唯一版本化 source owner；
- `assets/manifest.json` 固化 source、公开 `/static/**` target 与 SHA-256 integrity；
- Runtime projection 只 create missing targets；
- stable targets 自动进入 StaticResource protected-path；
- `/static/uploads/**` 与 Historical Canonical Migration assets 不被 Site Package stable asset ownership 接管；
- CI / Review Environment 从空 Runtime Static Root 启动并由同一 Site Package root 投影稳定资源。

EU-41 / EU-42 的 exact-head、Integration 与 Post-Integration Evidence 均已完成；详细证据见对应 `docs/work/eu41-...`、`docs/work/eu42-...` 与 Issue #77 comments。

## 当前剩余架构顺序

Issue #77 当前剩余 Planning Candidate 不再沿用旧 Slice D 的单步描述，而按以下顺序重新规划：

```text
Historical Migration / Backend Application Boundary
        ↓
Generic Content Migration Application
        ↓
Party Migration De-specialization & Compatibility
        ↓
Canonical Migration Compatibility & E1～E3 Re-entry Gate
        ↓
Repository Split Readiness Assessment（独立后置）
```

这些都不是当前 Ready Execution Unit，也不继承 EU-42 Execute Authority。

### Candidate A — Backend Application / Core Boundary

需要冻结的长期边界是：CMS Server 与 Content Migration 具有独立 application lifecycle / Spring composition / deliverable artifact，同时共享 site-neutral CMS Domain / persistence capability，并保持单向依赖：

```text
cms-server app ──────────→ cms-core
content-migration app ───→ cms-core
```

当前推荐 build candidate 是：

```text
backend/
├── modules/
│   └── cms-core/
└── apps/
    ├── cms-server/
    └── content-migration/
```

但 AR-02 paired architecture review 已确认：两个 executable JAR 本身不逻辑必然要求三个 Gradle project。Technical Planning 必须同时比较“shared core source set + isolated server/migration source sets + independent BootJar”这一较低复杂度替代；如果它能以少量明确 wiring 达到同等 compile/runtime classpath、Spring composition、resource 与 test isolation，则允许选择。只在同一个完整 runtime classpath 上增加多个 main/BootJar 不满足该边界。

无论最终 build shape 如何，都必须满足：

- `cms-core` 不依赖任何 app，两个 app 互不依赖；
- `cms-server` 持有 HTTP/MVC/static-resource 与 server-only startup composition；
- `content-migration` 持有 CLI/import/report/compatibility composition，并在 2A 行为保持阶段完整承接当前 Party migration responsibility；
- migration app 不再通过 `CmsApplication` 根包扫描获得完整 Server composition；
- MyBatis mapper、configuration properties、Jackson、transaction、Flyway/schema policy、Site Package lifecycle 与 resource ownership 显式定义；
- Generic Flyway SQL 保持单一 Authority，不在两个 app 复制 lineage；
- 数据库 transaction 不被错误扩大为文件副作用的自动 rollback guarantee；
- 移动 Gradle project/source set 后重新验证现有相对 Site Package path、JavaExec / verification task 与资源加载路径；
- CMS Core 内部 content / column / listing / resource / navigation 等继续以 package-level modularity 为主；
- 不为了形式统一拆成一领域一个 Gradle module，不自动扩展为 Clean Architecture 或 Repository split。

2A 的完整 ownership、composition、行为保持与 verification obligations 以 `docs/project/pre-e1e3-convergence-plan.md` 为当前总体 Planning Authority；AR-02 Review Evidence 见 Issue #92。

### Candidate B — Generic Content Migration Application

职责只覆盖：

```text
Canonical Migration Dataset → CMS Runtime
```

Generic capability 包括 canonical format validation、path/digest safety、stable migration identity / fingerprint、preflight、transaction / file-side-effect boundary、dependency order、Article / Resource / ListItem import、legacy mapping、idempotency、conflict、reconciliation 与 report。

辅助工具边界：

```text
Legacy Source
  → Python / Node collection / normalization / promotion
  → Canonical Migration Dataset
  → JVM Generic Content Migration Application
  → CMS Runtime
```

Generic Engine 不得内建 Party / JilinJobs / EU-29 / EU-30 identity。

### Candidate C — Party migration de-specialization / compatibility

当前 production Backend 中的 `Party*Migration*` 类需要退出 `cms-server` Runtime responsibility。

Party-specific aliases、accepted fingerprints、carousel legacy transition 与 upgrade-only policy 继续作为 Party historical migration authority，不得通过改名隐藏进 Generic Engine。

保持义务至少包括：

- 183 篇 Party current Runtime Dataset；
- 4 条 accepted carousel；
- current canonical import；
- second import idempotency；
- changed fingerprint conflict；
- EU-29 → EU-30 upgrade compatibility；
- resource integrity；
- Runtime / Browser reconciliation。

### Candidate D — Canonical Migration Compatibility & E1～E3 Re-entry Gate

在最终边界上验证：

```text
Generic CMS Schema
  → JilinJobs Site Package stable structure
  → one-time Site bootstrap
  → Generic Content Migration Application
  → Party Canonical Dataset
  → Runtime
  → Replaceable Public Renderer
```

如果所有 current obligations 已由 Current Evidence 闭环，可以直接记录 compatibility closure / E1～E3 re-entry PASS；只有存在具体实现 gap 时才形成新的 Candidate Execution Unit。

## Repository Split Readiness Assessment

继续 deferred。只有四层 boundary 与 final compatibility 完成后才独立评估；Assessment 不等于自动拆仓，也不默认阻塞 E1～E3。

## 当前 Planning Gate

当前完整跨 Issue 顺序以 Issue #92 / `docs/project/pre-e1e3-convergence-plan.md` 为准：

1. 先完成 Repository Documentation Authority Convergence；
2. 再进入本文件上述 Historical Migration / Backend Application Boundary；
3. 再完成 final compatibility / E1～E3 re-entry gate。

Current Ready Execution Unit：**NONE**。

下一步不得直接进入代码 Execute。应先在最新 `main` 上恢复 Issue #92，并从 Phase 1 的 Requirement / Specification / Technical Planning / `slice-work` 实际状态继续。
