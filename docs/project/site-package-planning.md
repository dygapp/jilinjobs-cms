# CMS Core / Site Package / Public Renderer 规划状态

## 当前结论

Issue #77 继续承担 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层架构边界 Authority。

已完成并集成：EU-37～EU-42 Site Package Boundary 与 Issue #92 Phase 1 EU-43～EU-45 Documentation Authority Convergence。

Phase 2A 已基于 current Backend / migration / workflow dependency closure 完成 Requirement、Specification、Technical Planning、`slice-work` 与 `readiness-check`，形成 **EU-46 — Backend Application / Core Boundary Foundation**。

- Current Ready Execution Unit：**EU-46**；
- Readiness：**PASS**；
- Execute：**NOT STARTED**；
- Issue #77：**OPEN**；
- Phase 2B / 2C、Phase 3 与 Issue #60 / E1～E3：**downstream / no Execute Authority**。

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
Phase 2A Backend Application / Core Boundary — EU-46 READY
        ↓
Phase 2B Generic Content Migration Application — Planning Candidate
        ↓
Phase 2C Party Migration De-specialization & Compatibility — Planning Candidate
        ↓
Phase 3 Canonical Migration Compatibility & E1～E3 Re-entry Gate
        ↓
Repository Split Readiness Assessment（独立后置）
```

### Phase 2A / EU-46 — Backend Application / Core Boundary

Current Authority：

- `docs/requirements/backend-application-core-boundary.md`；
- `docs/specifications/backend-application-core-boundary.md`；
- `docs/technical/backend-application-core-boundary.md`；
- `docs/work/current/eu46-backend-application-core-boundary-foundation.md`。

目标依赖：

```text
cms-server app ───────────→ cms-core
content-migration app ────→ cms-core
```

Dependency closure 已确认 current Party migration task 使用完整 Backend `main.runtimeClasspath`，四个 migration implementation 都以 `CmsApplication` 为 Spring source；`WebApplicationType.NONE` 不形成 Server composition isolation。

Technical Planning 已完成 build alternative 对比并选择标准 Gradle multi-project：

```text
backend/
├── modules/
│   └── cms-core/
└── apps/
    ├── cms-server/
    └── content-migration/
```

该选择不是由两个 JAR 数量推出，而是因为 current mixed transport/service source 无论如何都必须拆分，而 source-set alternative 还需额外自定义 classpath/resource/test/BootJar wiring。标准 project dependency能直接证明 Server/Migration classpath isolation。

EU-46 必须保持：

- Core 不依赖 app，两个 app 互不依赖；
- Server持有 HTTP/MVC/static HTTP/server-only composition；
- Migration持有 Party CLI/import/report/compatibility composition，不依赖或扫描 Server app；
- Generic Flyway、CMS metadata、Site Package lifecycle保持 single shared authority；
- current Server JAR repository consumer path/name保持；
- Party 183 Articles、4 carousel、idempotency、fingerprint conflict、EU-29→EU-30 compatibility、resource integrity与report semantics保持；
- Canonical / Upgrade / Site Package / CI / Review workflow随 source move原子同步；
- 不提前执行 Generic Engine / Party de-specialization / repo split。

EU-46 readiness只在 planning/readiness integration 后，经新的 Fresh Context无 base drift revalidation才形成 Execute Authority。

### Phase 2B — Generic Content Migration Application

职责只覆盖 Canonical Dataset → CMS Runtime 的 site-neutral capability。可包含 canonical validation、path/digest safety、stable identity/fingerprint、preflight、transaction/file-side-effect boundary、dependency order、Article/Resource/ListItem import、legacy mapping、idempotency、conflict、reconciliation与report。

Generic Engine 不得内建 Party / JilinJobs / EU-29 / EU-30 identity。EU-46 完成前不得进入本阶段 Planning-to-Execute 状态链。

### Phase 2C — Party Migration De-specialization / Compatibility

Party-specific aliases、accepted fingerprints、carousel legacy transition与upgrade-only policy继续属于 Party migration authority，不得通过改名隐藏进 Generic Engine。

保持义务至少包括：183 Party current Articles、4 accepted carousel、second import idempotency、changed fingerprint conflict、EU-29→EU-30 compatibility、resource integrity与Runtime reconciliation。

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

Current Ready Execution Unit：**EU-46**。

Planning/readiness change 集成后，下一实际 Gate：

1. 从最新 `main` 重新恢复 `AGENTS.md`、README、Roadmap、Development Method、Issue #92 / #77 与 EU-46 Authority；
2. 核验 Open PR / Actions 与 base drift；
3. 只有 EU-46 Readiness仍有效时进入 Execute；
4. 不得继承 EU-42～EU-45历史 Execute Authority；
5. EU-46不得扩展到 Phase 2B / 2C、Phase 3 或 Issue #60 / E1～E3。
