# E1～E3 前置 Repository Authority 与 Migration Architecture 收敛规划

## 1. Status

- Planning source：GitHub Issue #92
- Related architecture authority：GitHub Issue #77
- Downstream candidates：GitHub Issue #60 / E1～E3
- Stage：**Planning Authority — ACTIVE / Phase 2C COMPLETED via EU-48**
- Phase 0：**COMPLETED**
- Phase 1 Repository Documentation Authority Convergence：**COMPLETED**
- Phase 2A Backend Application / Core Boundary Foundation：**COMPLETED via EU-46**
- Phase 2B Generic Content Migration Application：**COMPLETED via EU-47**
- Phase 2C Party Migration De-specialization & Compatibility：**COMPLETED via EU-48**
- Current Ready Execution Unit：**NONE**
- EU-48 Execute Authority：**TERMINATED on completion**
- Next Gate：**Phase 3 — Canonical Migration Compatibility & E1～E3 Re-entry planning / compatibility gate**
- Phase 1 closure Authority：`docs/project/documentation-authority-convergence.md`
- Phase 2A accepted Authority：`docs/requirements/backend-application-core-boundary.md` + `docs/specifications/backend-application-core-boundary.md` + `docs/technical/backend-application-core-boundary.md`
- Phase 2B accepted Authority：`docs/requirements/generic-content-migration-application.md` + `docs/specifications/generic-content-migration-application.md` + `docs/technical/generic-content-migration-application.md` + `docs/work/archive/eu47-generic-content-migration-application-foundation.md`
- Phase 2C accepted Authority：`docs/requirements/party-migration-despecialization-compatibility.md` + `docs/specifications/party-migration-despecialization-compatibility.md` + `docs/technical/party-migration-despecialization-compatibility.md` + `docs/work/archive/eu48-party-migration-despecialization-compatibility.md`

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

EU-46、EU-47、EU-48 均已完成且 Execute Authority终止。Phase 3 不继承任何已完成 Unit 的 Execute Authority；Issue #60 / E1～E3 继续在 Phase 3 re-entry PASS 前 blocked。

## 2. Why this planning layer exists

EU-37～EU-42 已完成 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层边界中的 Site Package foundation、stable structure、Navigation stable identity、Runtime composition、one-time bootstrap、Generic Schema separation 与 stable Site asset ownership。

EU-42 后 audit 确认两个不能直接留给 E1～E3 的长期问题：

1. Repository Documentation Authority 需要先完成 Current / superseded / Historical Work 收敛；
2. Party-specific Historical Migration Application 仍与 Backend Server production classpath / Spring composition 共存，而未来 Main migration 需要 site-neutral、可复用且独立运行的 Generic Content Migration Application。

第 1 项已由 Phase 1 / EU-43～EU-45 完成。第 2 项的 application/core isolation 已由 Phase 2A / EU-46 完成，Generic Content Migration capability 已由 Phase 2B / EU-47 建立，Party-specific de-specialization 与 accepted historical compatibility 已由 Phase 2C / EU-48 收敛。下一步仅允许进入 Phase 3 compatibility / E1～E3 re-entry Gate，重新对账完整链路并决定是否满足 re-entry；不因 Phase 2C 完成自动启动 E1～E3。

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
Phase 2C Party Migration De-specialization & Compatibility — EU-48 COMPLETED
        ↓
Phase 3  Canonical Migration Compatibility & E1～E3 Re-entry Gate — CURRENT NEXT GATE
        ↓
Issue #60 / E1～E3

后置：Phase 4 Repository Split Readiness Assessment
```

当前 **Ready Execution Unit = NONE**。下一 Gate只允许从 current Repository Authority / Current Evidence恢复 Phase 3 compatibility/re-entry planning；不得从 EU-48或本阶段完成事实直接继承 Execute Authority。

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

`content-migration`：独立 non-web Spring composition、Historical Migration implementation、migration-only compatibility / CLI/import/report；不得依赖 Server app。

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
- EU-29 accepted → current position-2 compatibility；
- resource bytes / mappings / report labels / failure semantics。

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

不得内建 Party / JilinJobs / EU-29 / EU-30 identity。Party aliases、`PARTY_CAROUSEL`、fixed four items、accepted fingerprints、position-2 LINK→ARTICLE upgrade exception在 EU-47 后仍属于 Party compatibility authority。

### 7.4 Slice / readiness result

`slice-work` 形成单一 Candidate：**EU-47 — Generic Content Migration Application Foundation**。`readiness-check`：**PASS**。

### 7.5 Execute / Verification / Completion result

Fresh Context在 planning/readiness integration 后重新核验 actual Repository state，并以 `main@5b2f128ae8eccba792402836bd1f8f63e798c82e` 建立 EU-47自身 Execute baseline；无 base drift / Authority blocker。

EU-47 implementation已完成：neutral Article/ListItem legacy Mapper shared ownership、Generic canonical Article/List models、loader / preflight / execute / report、INTERNAL / EXTERNAL_LINK Article、LINK / ARTICLE ListItem、stable dependency、Generic command/report与 synthetic verifier均已建立。`data-migrations/party/v1/**`、DB schema/Flyway、API、frontend、Site Package behavior未修改。

最终 pre-integration exact-head evidence由 PR #107 / Actions记录；merge后 Post-Integration Current Evidence由 Issue #92 / #77记录。EU-47完成后 Execute Authority终止，不传递给后续 Phase。

## 8. Phase 2C — Party Migration De-specialization & Compatibility — COMPLETED VIA EU-48

### 8.1 Accepted Authority

- Requirement：`docs/requirements/party-migration-despecialization-compatibility.md`；
- Specification：`docs/specifications/party-migration-despecialization-compatibility.md`；
- Technical Plan：`docs/technical/party-migration-despecialization-compatibility.md`；
- Completed Work Artifact：`docs/work/archive/eu48-party-migration-despecialization-compatibility.md`。

### 8.2 Accepted result

EU-48以独立 Execute baseline `main@4cd1d2ea7e9d1e584561f4b3b40a0d44d74b0169`完成：

```text
Party canonical dataset / compatibility authority
        ↓
Party bounded adapter / exact compatibility guard
        ↓
Generic Content Migration capability
        ↓
CMS Runtime
```

- current Party aliases/counts/current fingerprints、target relation与current image facts继续由 `manifest.json` / Article index / list index-items持有；
- 新增 `data-migrations/party/v1/compatibility.json`，只保存唯一 accepted old position-2 transition的 old-state identity / `fromFingerprint` / source type / preserve-runtime-id requirement；
- Party Article/List accepted on-disk shape由 bounded adapter验证/normalization，并委托 Generic prepared dataset path承担 steady-state CREATE/SKIP/CONFLICT；
- Generic Engine仅增加 site-neutral prepared entry、shared byte/path verifier、source provenance / safe static-target metadata；不获得 Party identity、general UPDATE或 plugin/policy framework；
- compatibility layer只在 exact transition + old Runtime guard + current target dependency全部匹配时原位更新 position 2，保持 list-item Runtime id；任何 drift均 CONFLICT / no update；
- external command、Gradle task、legacy report label与 current 183 Articles / 4 carousel行为保持；
- no DB schema/Flyway/API/frontend/Site Package semantic change。

### 8.3 Verification / completion result

Pre-integration exact-head required evidence全部 PASS：

1. Party Migration De-specialization focused verification，包括真实 MySQL compatibility guard scenarios；
2. Canonical Migration Verification，current 183 Articles + 4 carousel first/second import与 bytes/reconciliation；
3. EU-30 Migration Upgrade Verification，pinned EU-29 → current exact one UPDATE、Runtime id preservation与post-upgrade idempotency；
4. Generic Content Migration Verification；
5. Backend Application Boundary Verification；
6. Site Package Verification；
7. Repository CI / Integrated feature + admin browser；
8. unresolved review threads = 0。

最终 exact-head / merge / Post-Integration run identity由 PR #109、Actions及 Issue #92/#77 Current Evidence记录。

EU-48 completion后 Execute Authority终止；Phase 2C completion不自动授予 Phase 3或 Issue #60 / E1～E3 Execute Authority。

## 9. Phase 3 — Canonical Migration Compatibility & E1～E3 Re-entry Gate

最终链路：

```text
Generic CMS Schema
→ JilinJobs Site Package stable structure
→ one-time Site bootstrap
→ Generic Content Migration Application
→ Party Canonical Dataset / compatibility authority
→ Runtime
→ Replaceable Public Renderer
```

当前只进入 **Planning / Compatibility Gate**。必须基于 Phase 2A～2C accepted Authority 与 Post-Integration Current Evidence重新对账完整链路；全部 obligation闭环后才可记录 compatibility closure / E1～E3 re-entry PASS。发现真实 implementation gap 时才重新 `slice-work`。Phase 3 PASS 前不得进入 Issue #60 / E1～E3 Execute。

## 10. Phase 4 — Repository Split Readiness Assessment

继续 deferred。只有四层 boundary完整闭环后独立评估；不自动拆仓，也不默认阻塞 E1～E3。

## 11. Fresh Context Gate

下一次继续 Issue #92 / #77 时必须重新读取：

1. current `main`、Open PR / Issue、最近相关 Actions；
2. `AGENTS.md`、Root `README.md`、`docs/README.md`；
3. Roadmap / Development Method；
4. Issue #92 / #77；
5. 本总体规划、Phase 2A～2C accepted Authority与 latest Current Evidence。

Current Ready Execution Unit = **NONE**。下一实际入口仅为 Phase 3 Canonical Migration Compatibility & E1～E3 Re-entry planning / compatibility gate。`docs/work/archive/eu48-party-migration-despecialization-compatibility.md`只承担 completed traceability，不构成 Phase 3 Execute Authority。不得因 EU-48完成而提前进入 Issue #60 / E1～E3。
