# E1～E3 前置 Repository Authority 与 Migration Architecture 收敛规划

## 1. Status

- Planning source：GitHub Issue #92
- Related architecture authority：GitHub Issue #77
- Downstream candidates：GitHub Issue #60 / E1～E3
- Stage：**Planning Authority — ACTIVE / Phase 2A READY**
- Phase 0：**COMPLETED**
- Phase 1 Repository Documentation Authority Convergence：**COMPLETED**
- Phase 2A dependency closure / Requirement / Specification / Technical Planning / slice-work / readiness-check：**COMPLETED**
- Current Ready Execution Unit：**EU-46 — Backend Application / Core Boundary Foundation**
- EU-46 Readiness：**PASS**
- EU-46 Execute：**NOT STARTED**
- Next Gate：**planning/readiness integration → Fresh Context base-drift revalidation → EU-46 Execute**
- Phase 1 closure Authority：`docs/project/documentation-authority-convergence.md`

本文固化 EU-42 之后、Issue #60 / E1～E3 重新进入正式规划之前的总体演进顺序。Phase / Planned Unit 名称只是 Planning identity；只有具体 Unit 经 `slice-work → readiness-check` PASS，并在 readiness change 集成后完成 Fresh Context revalidation，才授予对应 Execute 权限。

```text
Planning / Requirement Candidate
  → Requirement / Specification / 必要 Technical Planning
  → slice-work
  → Candidate Execution Unit
  → readiness-check
  → Ready Execution Unit
  → Fresh-context Execute
```

EU-46 不继承 EU-45；Phase 2B / 2C、Phase 3 与 E1～E3 也不得继承 EU-46。

## 2. Why this planning layer exists

EU-37～EU-42 已完成 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层边界中的 Site Package foundation、stable structure、Navigation stable identity、Runtime composition、one-time bootstrap、Generic Schema separation 与 stable Site asset ownership。

EU-42 后 audit 确认两个不能直接留给 E1～E3 的长期问题：

1. Repository Documentation Authority 需要先完成 Current / superseded / Historical Work 收敛；
2. Party-specific Historical Migration Application 仍与 Backend Server production classpath / Spring composition 共存，而未来 Main migration 需要 site-neutral、可复用且独立运行的 Generic Content Migration Application。

第 1 项已由 Phase 1 / EU-43～EU-45 完成。第 2 项按 Phase 2A → 2B → 2C 分阶段处理，避免在同一个 refactor 中同时改变 application boundary 与 migration domain semantics。

## 3. Overall sequence

```text
Phase 0  Planning Authority Solidification — COMPLETED
        ↓
Phase 1  Repository Documentation Authority Convergence — COMPLETED
        ↓
Phase 2A Backend Application / Core Boundary Foundation — EU-46 READY
        ↓
Phase 2B Generic Content Migration Application — PLANNING CANDIDATE
        ↓
Phase 2C Party Migration De-specialization & Compatibility — PLANNING CANDIDATE
        ↓
Phase 3  Canonical Migration Compatibility & E1～E3 Re-entry Gate
        ↓
Issue #60 / E1～E3

后置：Phase 4 Repository Split Readiness Assessment
```

Phase 2A 当前只有 EU-46 获得 Readiness PASS；后续阶段均没有 Execute Authority。

## 4. Phase 0 — COMPLETED

Phase 0 已完成：

- Issue #92 / #77 / Roadmap / Site Package Planning 总体顺序对齐；
- AR-02 明确两个 executable JAR 不必然推出多个 Gradle project，并要求先冻结 application/core/composition boundary；
- AR-04 blind review再次识别“先 source-set packaging、后 application boundary”的 sequencing flaw；
- lower-cost capable review first、Astra selective escalation 保持 Consumer-local `ADJUST` evidence，不成为默认 Method Gate。

模型 review 只提供 Planning correction evidence，不授予 Execute。

## 5. Phase 1 — COMPLETED

- EU-43 — Current Authority Semantic Reconciliation；
- EU-44 — Canonical Product Authority Consolidation；
- EU-45 — Documentation Information Architecture & Archive Migration。

Phase 1 完成后 Current / archive 的物理边界已稳定，`docs/README.md` 是 Documentation Authority Map；archive 默认不参与 Fresh Context Current Authority 恢复。

## 6. Phase 2A — Backend Application / Core Boundary Foundation

### 6.1 Current Authority

- Requirement：`docs/requirements/backend-application-core-boundary.md`；
- Specification：`docs/specifications/backend-application-core-boundary.md`；
- Technical Plan：`docs/technical/backend-application-core-boundary.md`；
- Ready Work artifact：`docs/work/current/eu46-backend-application-core-boundary-foundation.md`。

### 6.2 Dependency closure result

Current baseline `main@bfd0983e66d392de1722a703356ce581fd1a41ea` 的 inventory 确认：

- Backend 仍是单 Gradle project；
- `importPartyHistoricalContent` / `importPartyCarousel` 使用 `sourceSets["main"].runtimeClasspath`；
- 四个 Party migration implementation 都以 `CmsApplication` 作为 Spring source；
- `WebApplicationType.NONE` 只关闭 Web server，不限制 root component scan；
- Migration 需要共享 Article/Column/List/Resource/StaticResource/MyBatis/transaction/SitePackage capability；
- `CmsList.kt`、`Advertisement.kt`、`SiteConfig.kt`、`StaticResource.kt` 混合 Core service/persistence 与 Server REST transport；
- Site Provisioning / Bootstrap maintenance CLI 与部分 verifier也直接依赖 `CmsApplication`；
- CI / Review Environment稳定消费 `backend/build/libs/jilinjobs-cms-backend-0.1.0-SNAPSHOT.jar`；
- Canonical / Upgrade / Review workflow直接消费当前 Party import task；
- workflow path filters仍指向 monolithic source path。

因此仅搬 migration 文件或在同一完整 runtime classpath增加第二个 BootJar都不能形成 2A boundary。

### 6.3 Build-shape decision

Technical Planning 已完成 multi-project 与 lower-complexity source-set alternative 比较，选择：

```text
backend/
├── modules/
│   └── cms-core/
└── apps/
    ├── cms-server/
    └── content-migration/
```

Dependency：

```text
cms-server ───────────→ cms-core
content-migration ────→ cms-core
```

选择 multi-project 的理由是 dependency / runtime classpath / Spring composition isolation 可以通过标准 Gradle project dependency直接证明。source-set alternative无法避免 mixed transport split，还需额外维护 bespoke classpath/resource/test/BootJar wiring，因此对当前 Repository并不更低复杂度。

### 6.4 2A ownership contract

`cms-core`：site-neutral domain / validation / persistence / Mapper / transaction / shared Resource / StaticResource service / Generic Flyway SQL / Site Package capability / CMS metadata。

`cms-server`：`CmsApplication`、Admin/Public HTTP transport、MVC/static HTTP、server-only handler/startup；保持 current Server JAR external path/name。

`content-migration`：独立 non-web Spring composition、四个 Party migration implementation、migration-only mapper/compatibility、CLI/import/report；不得依赖 Server app。

Product-level Generic CMS Core 不要求 Controller / MVC transport进入 shared build artifact。

### 6.5 Resource / lifecycle contract

- Generic Flyway V1/V2保持 single Core resource authority；
- `cms-metadata.yml` 保持 single shared authority；
- 两个 app各自组合 datasource/MyBatis/Jackson/transaction；
- Migration配置 Site Package root时继续保持 Generic Schema → stable Site Package reconcile / stable asset projection → canonical import；
- one-time Site bootstrap仍只在现有 explicit property启用；普通 canonical import不隐式 bootstrap；
- Database transaction不被夸大为 filesystem/resource side-effect rollback。

### 6.6 Behavior-preservation contract

EU-46 不改变：

- Admin/Public API / user-visible behavior；
- Generic Flyway schema semantics；
- Site Package structure/bootstrap/assets semantics；
- Party current canonical 183 Articles + 4 accepted carousel；
- first/second import idempotency；
- fingerprint conflict；
- EU-29 accepted → EU-30 current position-2 compatibility；
- resource bytes / mappings / report labels / failure semantics。

四个 Party migration files必须一起迁出 Server source ownership；不在 2A 泛化 Party domain。

### 6.7 Verification Gate

EU-46 required exact-head evidence：

1. Core / Server / Migration build + artifact/dependency proof；
2. Server JAR normal runtime；
3. Migration non-web context、no Server controller/classpath leakage；
4. Repository CI；
5. Canonical Migration Verification；
6. EU-30 Migration Upgrade Verification；
7. Site Package Verification；
8. workflow path filters / commands对新 source ownership有效；
9. PR unresolved review threads = 0；
10. Post-Integration CI / affected targeted workflow evidence + Fresh Context locator revalidation。

Review Environment可作为 supporting runtime evidence，但没有视觉产品变更，因此不替代上述 required evidence。

### 6.8 Readiness result

`slice-work` 形成单一 EU-46，而不是分别拆 Core / Server / Migration / workflow EU，因为这些边界必须原子一致；独立合并任一部分都会产生重复类、class path leakage 或 verification blind spot。

Readiness：**PASS**。

本 planning/readiness change 集成前仍不得 Execute。集成后必须在新的 Fresh Context 中重新确认 base drift / Authority / Open PR / Actions。

## 7. Phase 2B — Generic Content Migration Application — PLANNING CANDIDATE

只有 EU-46 完成后才允许重新开始 Planning。

目标链路：

```text
Legacy Source
→ collection / normalization / promotion
→ Canonical Migration Dataset
→ Generic JVM Content Migration Application
→ CMS Runtime
```

Generic capability 可负责 canonical validation、path/digest safety、stable migration identity/fingerprint、preflight、transaction/file-side-effect boundary、dependency order、Article/Resource/ListItem import、legacy mapping、idempotency/conflict/reconciliation/report。

不得内建 Party / JilinJobs / EU-29 / EU-30 identity。

## 8. Phase 2C — Party Migration De-specialization & Compatibility — PLANNING CANDIDATE

后续把 Party-specific aliases、accepted fingerprints、carousel transition / upgrade-only policy收敛到 Party dataset/profile/compatibility authority，并通过同一 Generic Application保持 current 183 Articles、4 carousel与EU-29→EU-30 compatibility。

## 9. Phase 3 — Canonical Migration Compatibility & E1～E3 Re-entry Gate

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

全部 Current Evidence闭环时记录 compatibility closure / E1～E3 re-entry PASS；发现真实 implementation gap 时才重新 `slice-work`。Phase 3 PASS 前不得进入 Issue #60 / E1～E3 Execute。

## 10. Phase 4 — Repository Split Readiness Assessment

继续 deferred。只有四层 boundary完整闭环后独立评估；不自动拆仓，也不默认阻塞 E1～E3。

## 11. Fresh Context Gate

EU-46 readiness integration 后，新的 Execute 会话必须重新读取：

1. current `main`、Open PR / Issue、最近相关 Actions；
2. `AGENTS.md`、Root `README.md`、`docs/README.md`；
3. Roadmap / Development Method；
4. Issue #92 / #77；
5. EU-46 Requirement / Specification / Technical Plan / Work artifact；
6. Planning/readiness integration evidence。

只有 EU-46 Readiness仍有效且没有 base drift / blocking parallel work时才能 Execute。不得把该 Authority扩展到 Phase 2B / 2C、Phase 3 或 Issue #60 / E1～E3。
