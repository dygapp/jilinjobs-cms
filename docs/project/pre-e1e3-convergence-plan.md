# E1～E3 前置 Repository Authority 与 Migration Architecture 收敛规划

## 1. Status

- Planning source：GitHub Issue #92
- Related architecture authority：GitHub Issue #77
- Downstream candidates：GitHub Issue #60 / E1～E3
- Stage：**Planning Authority — ACTIVE / Phase 2B COMPLETED**
- Phase 0：**COMPLETED**
- Phase 1 Repository Documentation Authority Convergence：**COMPLETED**
- Phase 2A Backend Application / Core Boundary Foundation：**COMPLETED via EU-46**
- Phase 2B Generic Content Migration Application：**COMPLETED via EU-47**
- Current Ready Execution Unit：**NONE**
- Next Gate：**Phase 2C — Party Migration De-specialization & Compatibility Planning Candidate**
- Phase 1 closure Authority：`docs/project/documentation-authority-convergence.md`
- Phase 2A accepted Authority：`docs/requirements/backend-application-core-boundary.md` + `docs/specifications/backend-application-core-boundary.md` + `docs/technical/backend-application-core-boundary.md`
- Phase 2B accepted Authority：`docs/requirements/generic-content-migration-application.md` + `docs/specifications/generic-content-migration-application.md` + `docs/technical/generic-content-migration-application.md` + `docs/work/archive/eu47-generic-content-migration-application-foundation.md`

本文固化 EU-42 之后、Issue #60 / E1～E3 重新进入正式规划之前的总体演进顺序。Phase / Planned Unit 名称只是 Planning identity；只有具体 Unit 经 `slice-work → readiness-check` PASS 才形成 Ready Execution Unit，且后续 Execute 仍需遵守 Fresh Context、base drift 与 Repository Authority Gate。

```text
Planning / Requirement Candidate
  → Requirement / Specification / 必要 Technical Planning
  → slice-work
  → Candidate Execution Unit
  → readiness-check
  → Ready Execution Unit
  → Fresh-context Execute
```

EU-46 与 EU-47 均已完成且 Execute Authority终止。Phase 2C 不继承 EU-47 或更早 Unit 的 Execute Authority。

## 2. Why this planning layer exists

EU-37～EU-42 已完成 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层边界中的 Site Package foundation、stable structure、Navigation stable identity、Runtime composition、one-time bootstrap、Generic Schema separation 与 stable Site asset ownership。

EU-42 后 audit 确认两个不能直接留给 E1～E3 的长期问题：

1. Repository Documentation Authority 需要先完成 Current / superseded / Historical Work 收敛；
2. Party-specific Historical Migration Application 仍与 Backend Server production classpath / Spring composition 共存，而未来 Main migration 需要 site-neutral、可复用且独立运行的 Generic Content Migration Application。

第 1 项已由 Phase 1 / EU-43～EU-45 完成。第 2 项中的 application/core isolation 已由 Phase 2A / EU-46 完成；Phase 2B 已由 EU-47 建立 Generic Content Migration capability。Party-specific de-specialization仍由 Phase 2C 独立处理，避免把 Generic Engine foundation与 Party compatibility迁移重新耦合到同一个 Unit。

## 3. Overall sequence

```text
Phase 0  Planning Authority Solidification — COMPLETED
        ↓
Phase 1  Repository Documentation Authority Convergence — COMPLETED
        ↓
Phase 2A Backend Application / Core Boundary Foundation — EU-46 COMPLETED
        ↓
Phase 2B Generic Content Migration Application — EU-47 COMPLETED
        ↓
Phase 2C Party Migration De-specialization & Compatibility — CURRENT PLANNING CANDIDATE
        ↓
Phase 3  Canonical Migration Compatibility & E1～E3 Re-entry Gate
        ↓
Issue #60 / E1～E3

后置：Phase 4 Repository Split Readiness Assessment
```

当前 **Ready Execution Unit = NONE**。下一 Gate只允许进入 Phase 2C Planning；必须重新完成 dependency closure、Requirement / Specification / 必要 Technical Planning、`slice-work` 与 `readiness-check`，不得直接 Execute。

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

## 6. Phase 2A — Backend Application / Core Boundary Foundation — COMPLETED

### 6.1 Accepted Authority

- Requirement：`docs/requirements/backend-application-core-boundary.md`；
- Specification：`docs/specifications/backend-application-core-boundary.md`；
- Technical Plan：`docs/technical/backend-application-core-boundary.md`；
- Completed Work artifact：`docs/work/archive/eu46-backend-application-core-boundary-foundation.md`。

### 6.2 Dependency closure result

Planning baseline `main@bfd0983e66d392de1722a703356ce581fd1a41ea` 的 inventory确认：

- Backend 原为单 Gradle project；
- `importPartyHistoricalContent` / `importPartyCarousel` 使用完整 `sourceSets["main"].runtimeClasspath`；
- 四个 Party migration implementation 都以 `CmsApplication` 作为 Spring source；
- `WebApplicationType.NONE` 只关闭 Web server，不限制 root component scan；
- Migration 需要共享 Article/Column/List/Resource/StaticResource/MyBatis/transaction/SitePackage capability；
- `CmsList.kt`、`Advertisement.kt`、`SiteConfig.kt`、`StaticResource.kt` 混合 Core service/persistence 与 Server REST transport；
- Site Provisioning / Bootstrap maintenance CLI 与部分 verifier也直接依赖 `CmsApplication`；
- CI / Review Environment稳定消费 `backend/build/libs/jilinjobs-cms-backend-0.1.0-SNAPSHOT.jar`；
- Canonical / Upgrade / Review workflow直接消费 Party import task；
- workflow path filters曾指向 monolithic source path。

因此仅搬 migration 文件或在同一完整 runtime classpath增加第二个 BootJar不能形成 2A boundary。

### 6.3 Accepted build shape

EU-46 实施标准 Gradle multi-project：

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

### 6.4 Accepted ownership contract

`cms-core`：site-neutral domain / validation / persistence / Mapper / transaction / shared Resource / StaticResource service / Generic Flyway SQL / Site Package capability / CMS metadata。

`cms-server`：`CmsApplication`、Admin/Public HTTP transport、MVC/static HTTP、server-only handler/startup，以及与现有运行入口兼容的 maintenance CLI；保持 current Server JAR external path/name。

`content-migration`：独立 non-web Spring composition、四个 Party migration implementation、migration-only compatibility / CLI/import/report；不得依赖 Server app。

Product-level Generic CMS Core 不要求 Controller / MVC transport进入 shared build artifact。

### 6.5 Resource / lifecycle contract

- Generic Flyway V1/V2保持 single Core resource authority；
- `cms-metadata.yml` 保持 single shared authority；
- 两个 app各自组合所需 datasource/MyBatis/Jackson/transaction；
- Migration配置 Site Package root时继续保持 Generic Schema → stable Site Package reconcile / stable asset projection → canonical import；
- one-time Site bootstrap仍只在现有 explicit property启用；普通 canonical import不隐式 bootstrap；
- Database transaction不被夸大为 filesystem/resource side-effect rollback。

### 6.6 Behavior-preservation result

EU-46 保持：

- Admin/Public API / user-visible behavior；
- Generic Flyway schema semantics；
- Site Package structure/bootstrap/assets semantics；
- Party current canonical 183 Articles + 4 accepted carousel；
- first/second import idempotency；
- fingerprint conflict；
- EU-29 accepted → EU-30 current position-2 compatibility；
- resource bytes / mappings / report labels / failure semantics。

四个 Party migration files一起迁出 Server source ownership；Phase 2A 未泛化 Party domain。

### 6.7 Verification result

EU-46 pre-integration exact-head required evidence全部 PASS：

1. Core / Server / Migration build + artifact/dependency proof；
2. Server JAR packaging；
3. Migration non-web context + no Server classpath leakage；
4. Repository CI；
5. Canonical Migration Verification；
6. EU-30 Migration Upgrade Verification；
7. Site Package Verification；
8. workflow path filters / commands对新 source ownership有效；
9. focused Backend Application Boundary Verification。

详细 SHA / Run evidence由 PR #105、Actions与 `docs/work/archive/eu46-backend-application-core-boundary-foundation.md` 记录；最终 Post-Integration evidence由 Issue #92 Current Evidence承担。

Review Environment可作为 supporting runtime evidence，但没有视觉产品变更，因此不替代 required evidence。

### 6.8 Completion result

EU-46 是 Phase 2A 唯一 Ready/Execute Unit，已完成 implementation、required pre-integration verification与 integration closure。其 Execute Authority在完成后终止。

Phase 2A completion只建立 application / shared-core boundary；**不代表 Generic Content Migration Engine 已存在，也不代表 Party-specific migration semantics 已去专用化。**

## 7. Phase 2B — Generic Content Migration Application — COMPLETED VIA EU-47

### 7.1 Accepted Authority

- Requirement：`docs/requirements/generic-content-migration-application.md`；
- Specification：`docs/specifications/generic-content-migration-application.md`；
- Technical Plan：`docs/technical/generic-content-migration-application.md`；
- Completed Work artifact：`docs/work/archive/eu47-generic-content-migration-application-foundation.md`。

### 7.2 Dependency closure result

基于 EU-46 integration `main@0af483a6e5278ad4ac049f33148144214b40669d` 的 current inventory确认：

- `content-migration` 已是独立 non-web application，仅依赖 `cms-core`；
- Party Article / Carousel implementation中已存在可复用的 stable identity/fingerprint、path/digest、Article/Resource/ListItem、legacy mapping与report语义，但与 Party alias、固定 list code/count、EU-29/EU-30 compatibility hardcode混合；
- `ArticleLegacyMappingMapper` 与 `CmsListItemLegacyMappingMapper` 对应的 table/field语义本身 site-neutral，可成为单一 shared migration-only ownership；
- `data-migrations/README.md` 已定义 site-neutral canonical organization，`article.schema.json` 已 generic，而现有 `carousel.schema.json` 明确是 Party-specific；
- Core `CmsListService` / `ResourceService` / `StaticResourceService` 已提供 Generic Article、LINK/ARTICLE ListItem与安全 resource projection所需能力；
- current DB schema已有两个 legacy mapping table，不需要新 Flyway migration；
- Canonical / EU-30 Upgrade workflows可以继续作为 Party current behavior regression evidence。

因此最低必要实现不是新建 app/module/plugin framework，而是在 existing `backend/apps/content-migration` 内增加 bounded `migration/generic/**` capability，同时保持 Party current commands在 Phase 2B不迁移。

### 7.3 Accepted Generic boundary

目标链路：

```text
Legacy Source
→ collection / normalization / promotion
→ Canonical Migration Dataset
→ Generic JVM Content Migration Application
→ CMS Runtime
```

Generic capability负责 canonical validation、path/digest safety、stable migration identity/fingerprint、preflight、transaction/file-side-effect boundary、dependency order、Article/Resource/ListItem import、legacy mapping、idempotency/conflict/reconciliation/report。

不得内建 Party / JilinJobs / EU-29 / EU-30 identity。Party aliases、`PARTY_CAROUSEL`、fixed four items、accepted fingerprints、position-2 LINK→ARTICLE upgrade exception继续属于 Phase 2C Party compatibility authority。

### 7.4 Slice / readiness result

`slice-work` 形成单一 Candidate：**EU-47 — Generic Content Migration Application Foundation**。不拆成 schema/helper/importer/CLI多个 EU，因为 Generic schema必须与可执行 consumer、共同 preflight/report、focused verification及 Party regression在同一原子边界闭合，才能成为 Phase 2C可靠前置。

`readiness-check`：**PASS**。

EU-47 Execute scope包括：

- neutral legacy mapping ownership relocation，不改 table/SQL semantics；
- generic canonical models + generic list schemas；
- load → structural/byte preflight → Runtime target/mapping/dependency preflight → execute → report；
- Generic Article INTERNAL/EXTERNAL_LINK + BODY_IMAGE/ATTACHMENT；
- Generic ListItem LINK/ARTICLE stable relation；
- `generic-content` dispatcher、root `importCanonicalContent`、`CONTENT_MIGRATION_REPORT`；
- synthetic Generic verification、Generic package purity proof与 focused workflow；
- exact-head Canonical / EU-30 / Boundary / Site Package / CI regression。

Phase 2B 明确不修改 `data-migrations/party/v1/**` accepted bytes，不切换 Party current commands到 Generic Engine，不实现 Party accepted fingerprint transition，不开始 Main Site canonical data collection/import，不修改 schema/API/frontend/Site Package，不升级 `agentic-dev` baseline。

### 7.5 Execute / Verification / Completion result

Fresh Context在 planning/readiness integration 后重新核验 actual Repository state，并以 `main@5b2f128ae8eccba792402836bd1f8f63e798c82e` 建立 EU-47自身 Execute baseline；无 base drift / Authority blocker。

EU-47 implementation已完成：

- neutral Article/ListItem legacy Mapper迁到 shared migration ownership；
- Generic canonical Article/List models与 list schemas已建立；
- Generic loader / structural+byte preflight / Runtime target+mapping+dependency preflight / execute / report已建立；
- Generic INTERNAL / EXTERNAL_LINK Article、BODY_IMAGE / ATTACHMENT、LINK / ARTICLE ListItem与 stable article dependency已建立；
- LINK image使用 site-neutral deterministic static target，ARTICLE image使用 managed Resource；
- `generic-content`、`importCanonicalContent`、`CONTENT_MIGRATION_REPORT`与 conflict/invalid non-success semantics已建立；
- synthetic Fresh DB verifier与 Generic source purity workflow已建立；
- `data-migrations/party/v1/**`、DB schema/Flyway、API、frontend、Site Package behavior未修改。

Focused verification覆盖 first CREATE、second-import全 SKIP、changed fingerprint conflict、path traversal、missing/tampered resource、unresolved reference、missing Column/List/dependency、duplicate identity、index/item mismatch与 no-mutation preflight；同时验证 non-web composition、LINK static image与 ARTICLE managed image/Core projection contract。Canonical Migration、EU-30 Upgrade、Backend Boundary、Site Package与 Repository CI / Integrated Browser继续承担 Party / repository regression Gate。

最终 pre-integration exact-head evidence由 PR #107 / Actions记录；merge后 Post-Integration Current Evidence由 Issue #92 / #77记录。EU-47完成后 Execute Authority终止，不传递给后续 Phase。

## 8. Phase 2C — Party Migration De-specialization & Compatibility — CURRENT PLANNING CANDIDATE

在 EU-47 Generic Engine boundary已完成后，Party-specific aliases、accepted fingerprints、carousel transition / upgrade-only policy收敛到 Party dataset/profile/compatibility authority，并通过同一 Generic Application保持 current 183 Articles、4 carousel与EU-29→EU-30 compatibility。

Phase 2C 当前没有 Candidate Execution Unit、Ready Execution Unit 或 Execute Authority。必须重新完成 dependency closure、Requirement / Specification / 必要 Technical Planning、`slice-work` 与 `readiness-check`，不得继承 EU-47 的 Execute Authority。

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

下一次继续 Issue #92 / #77 时必须重新读取：

1. current `main`、Open PR / Issue、最近相关 Actions；
2. `AGENTS.md`、Root `README.md`、`docs/README.md`；
3. Roadmap / Development Method；
4. Issue #92 / #77；
5. 本总体规划与当前 Phase 2C Planning Authority / Current Evidence。

`docs/work/archive/eu47-generic-content-migration-application-foundation.md` 只承担 completed traceability，不得作为 Phase 2C Execute Authority。只有 Phase 2C 独立完成 Planning → `slice-work → readiness-check`，形成新的 Ready Execution Unit并在后续 Fresh Context确认 integrated baseline / Authority / Open PR / Actions / base drift仍有效且无 blocker时，才允许 Execute。不得提前进入 Phase 3 或 Issue #60 / E1～E3。
