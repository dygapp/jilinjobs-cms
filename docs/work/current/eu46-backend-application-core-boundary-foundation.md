# EU-46 — Backend Application / Core Boundary Foundation

## Status

- Parent: GitHub Issue #92
- Related architecture authority: GitHub Issue #77
- Phase: **Phase 2A — Backend Application / Core Boundary Foundation**
- Candidate formed by: `slice-work`
- Readiness: **PASS**
- Execute state: **NOT STARTED**
- Planning baseline: `main@bfd0983e66d392de1722a703356ce581fd1a41ea`
- Planning branch: `planning/phase2a-backend-application-core-boundary`
- Requirement: `docs/requirements/backend-application-core-boundary.md`
- Specification: `docs/specifications/backend-application-core-boundary.md`
- Technical Plan: `docs/technical/backend-application-core-boundary.md`
- Execute Authority: **PENDING planning/readiness integration + Fresh Context base-drift revalidation**

EU-46 的 identifier 来自本次 Phase 2A `slice-work`。它不继承 EU-45 或更早 Unit 的 Execute Authority；当前 Planning branch 只形成并验证 Ready Unit，不能在 readiness change 集成前实施代码。

## 1. Dependency Closure

基于 `main@bfd0983e66d392de1722a703356ce581fd1a41ea` 的 current implementation / workflow inventory，依赖已闭合：

1. Backend 当前是单 Gradle project，两个 Party import task 使用完整 `main.runtimeClasspath`；
2. 四个 Party migration implementation 都以 `CmsApplication` 为 Spring source，`WebApplicationType.NONE` 不隔离 root component scan；
3. Party migration 真实依赖 Article / Column / List / Resource / StaticResource、MyBatis / transaction、legacy mapping 与 Site Package stable identity，不可仅移动四个 main 函数；
4. `CmsList.kt`、`Advertisement.kt`、`SiteConfig.kt`、`StaticResource.kt` 混合 shared service/persistence 与 REST transport，必须做最小 transport split 才能形成 artifact boundary；
5. Site Package provision/bootstrap service 文件中 maintenance CLI 直接引用 `CmsApplication`，必须从 shared service ownership 分离；
6. Generic Flyway V1/V2、`cms-metadata.yml`、Site Package runtime configuration 必须保持 single shared resource/capability authority；
7. Canonical Migration / EU-30 Upgrade / Site Package / CI / Review Environment 的 command、path filter、Server JAR consumer 已逐一盘点；
8. current CI / Review Environment 对 Server JAR 的稳定消费路径是 `backend/build/libs/jilinjobs-cms-backend-0.1.0-SNAPSHOT.jar`；没有改变该路径的必要性；
9. source-set alternative 已按 Issue #92 要求比较：由于 transport split 无法避免，额外自定义 source-set classpath/resource/test/BootJar wiring 不构成更低复杂度方案；标准 Gradle multi-project 被选择；
10. Consumer-local Method 足够完成本 Unit，不需要 `agentic-dev` baseline upgrade。

没有未决 Goal / Scope / Product behavior / Acceptance / Security / major architecture question需要人工介入。multi-project 是 Phase 2 Authority 已列出的推荐 architecture candidate，本次选择基于 current dependency closure，不改变四层产品边界或 Git Repository boundary。

## 2. Slice-work Result

Phase 2A 形成单一 Candidate Execution Unit：**EU-46 — Backend Application / Core Boundary Foundation**。

不拆成 Core extraction、Server app、Migration app、workflow migration 等多个 EU，原因：

- Core extraction 单独合并会让当前 Server / Migration source ownership失配；
- Server split 单独合并不能证明 Migration 独立 composition；
- Migration app 单独合并若仍依赖旧 main classpath 就是假隔离；
- source move 与 workflow path filter 必须原子同步，否则可能出现验证未触发的假绿；
- rollback 需要恢复完整的单-project application composition，而不是跨多个已集成中间态反向拼接。

因此本 Unit 是一个可独立验证、可整体回滚的 architecture foundation vertical slice。

## 3. Execute Scope

### 3.1 Gradle topology

在 `backend/` 建立：

```text
modules/cms-core
apps/cms-server
apps/content-migration
```

并形成：

```text
cms-server → cms-core
content-migration → cms-core
```

无 reverse / app-to-app dependency。

Root `backend/` 继续作为 repository build/command entry，并必须保持现有 root task capability：

- `bootJar` 产生 current Server JAR；
- `importPartyHistoricalContent --args=...`；
- `importPartyCarousel --args=...`；
- `provisionSitePackage --args=...`；
- `bootstrapSitePackage --args=...`；
- `verifySitePackageFoundation`；
- `verifyStableSiteStructure`；
- `verifyRuntimeSitePackageComposition`；
- `verifySiteBootstrapBaselineSeparation`；
- `verifySitePackageAssets`。

本 EU 不采用“只改 workflow、删除 root capability”的替代方案。

### 3.2 Core move / transport split

Move shared domain/service/persistence/provisioning/resource source into `modules/cms-core` while preserving package names and semantics.

必须拆分：

- `listing/CmsList.kt`；
- `advertisement/Advertisement.kt`；
- `siteconfig/SiteConfig.kt`；
- `staticresource/StaticResource.kt`；
- `provisioning/SitePackageProvisioning.kt` CLI tail；
- `provisioning/SitePackageBootstrap.kt` CLI object。

Core 不包含 Controller、`CmsApplication`、`ApiExceptionHandler`、Party migration implementation或 executable app launcher。

### 3.3 Server app

Move Server composition / HTTP transport to `apps/cms-server` and preserve:

- current Admin/Public API behavior；
- `/static/**` HTTP behavior；
- Generic Flyway / Site Package / optional bootstrap server lifecycle；
- current Server executable JAR path/name：`backend/build/libs/jilinjobs-cms-backend-0.1.0-SNAPSHOT.jar`。

Provision/bootstrap maintenance CLI entrypoint may remain server-side compatibility tooling after being separated from Core service source; it does not enter Migration classpath or ordinary Server startup.

### 3.4 Content Migration app

Move all four Party migration files together into `apps/content-migration` and add an independent non-web Spring composition root.

Preserve current article / carousel root Gradle tasks and report / exit semantics. An additive Party-specific executable dispatcher is allowed and expected for BootJar verification, but it must delegate to current services and must not generalize Party schema/alias/fingerprint/upgrade policy.

### 3.5 Shared resources / configuration

- Generic Flyway V1/V2 → one Core resource owner；
- `cms-metadata.yml` → one Core resource owner；
- Server and Migration have app-specific `application.yml` responsibility；
- Migration retains current DB/storage/static/Site Package property names and non-web lifecycle；
- Site Package runtime configuration remains site-neutral shared capability；
- normal migration import does not implicitly enable bootstrap。

### 3.6 Verification / workflow wiring

Add focused application-boundary verification and atomically update:

- `.github/workflows/ci.yml`；
- `.github/workflows/canonical-migration-verify.yml`；
- `.github/workflows/eu30-migration-upgrade-verify.yml`；
- `.github/workflows/review-environment.yml`；
- `.github/workflows/site-package-verification.yml`。

Path filters must observe new Core / Server / Migration ownership, Gradle settings/build files and shared Flyway resources.

## 4. Non-goals

- 不改变 `data-migrations/party/v1/**` bytes / manifest / canonical semantics；
- 不改变 DB schema / Flyway semantics；
- 不改变 Admin/Public API or user-visible behavior；
- 不执行 Phase 2B Generic Content Migration Engine；
- 不执行 Phase 2C Party de-specialization；
- 不开始 Main E1/E2/E3；
- 不创建第四个 Backend application；
- 不做 domain-per-module / Clean Architecture / plugin SPI；
- 不拆 Git Repository；
- 不更新 Public Renderer architecture；
- 不更新 `agentic-dev` baseline。

发现必须修改上述边界才能完成实现时，本 Readiness 立即失效并返回 Planning。

## 5. Acceptance

### A. Build / dependency boundary

1. `:modules:cms-core` 独立 build/test PASS；
2. `:apps:cms-server` build + BootJar PASS；
3. `:apps:content-migration` build + BootJar PASS；
4. dependency inspection 证明 `server → core`、`migration → core`，无 reverse / app-to-app dependency；
5. Server artifact 不包含 `Party*Migration*` implementation classes；
6. Migration artifact / runtime classpath 不包含 Admin/Public Controller、`CmsApplication` 或 `ApiExceptionHandler`；
7. root compatibility task names按 Execute Scope 保持可用。

### B. Server behavior

1. `backend/build/libs/jilinjobs-cms-backend-0.1.0-SNAPSHOT.jar` 仍可 `java -jar`；
2. current Generic Flyway V1/V2 only；
3. Generic Fresh DB / Site Package stable structure / bootstrap / stable assets contracts保持；
4. Backend tests + Site Package verifiers PASS；
5. Repository CI Admin/Public/Integrated Browser PASS。

### C. Migration behavior

1. Migration BootJar以 non-web mode 独立运行且无 HTTP listener；
2. Migration context 可取得所需 Core Mapper/service/Flyway/Jackson/transaction/SitePackage capability；
3. Migration context 无 Server Controller / server-only startup bean；
4. current root `importPartyHistoricalContent` first import / second idempotent behavior保持；
5. current root `importPartyCarousel` first import / second idempotent behavior保持；
6. current 183 Articles + 4 accepted carousel Runtime结果保持；
7. legacy fingerprint conflict path继续 fail；
8. EU-29 accepted → EU-30 current position-2 upgrade compatibility保持；
9. canonical resource / carousel bytes integrity保持；
10. `EU29_IMPORT_REPORT` / `EU29_CAROUSEL_IMPORT_REPORT` labels与 conflict/invalid exit behavior保持。

### D. Workflow / evidence

1. final changed-file set符合本 Unit scope，无 dataset/product/frontend semantic change；
2. new ownership path filters覆盖 Core / Server / Migration / Gradle / Flyway changes；
3. exact-head Repository CI：PASS；
4. exact-head Canonical Migration Verification：PASS；
5. exact-head EU-30 Migration Upgrade Verification：PASS；
6. exact-head Site Package Verification：PASS；
7. unresolved PR review threads = 0；
8. merge 前再次确认 base drift；
9. merge 后 `main` Post-Integration CI PASS，并按触发范围确认 Canonical / Upgrade / Site Package verification evidence；
10. Fresh Context locators重新读取，确认 EU-46 完成后下一 Gate 只进入 Phase 2B Planning Candidate，不自动授予 Phase 2B / 2C / Phase 3 / E1～E3 Execute Authority。

Review Environment 可以作为 supporting runtime evidence运行，但本 Unit没有视觉/人工产品验收变化，因此它不是替代上述 exact-head required evidence 的唯一 Gate。

## 6. Rollback

Rollback boundary：整个 EU-46 PR。

本 Unit 不迁移业务数据、不修改 accepted dataset、不引入新的 schema migration。若实现失败，可整体回退到当前单-project source/classpath/composition；任何测试产生的 DB / Runtime file side effect 都必须位于 ephemeral verification environment。

## 7. Readiness Check

### Goal / Scope

**PASS** — 独立 Server/Migration application lifecycle与 shared Core dependency direction已明确；Phase 2B/2C 与产品工作明确排除。

### Requirement / Specification

**PASS** — Requirement READY；Specification READY；Acceptance可直接映射 build/classpath/runtime/migration evidence。

### Technical Planning

**PASS** — multi-project vs source-set 已基于 current code比较；Gradle topology、ownership map、composition root、resource/config、Flyway/Site Package、test/workflow impact已冻结。

### Dependencies

**PASS** — current source、migration mains、mixed transport files、Site Package composition/CLI/verifier、CI/Canonical/Upgrade/Review/Site Package workflows与 Server JAR consumers均已盘点；无未知前置 integration dependency。

### Verification

**PASS** — repository CI + targeted Canonical/Upgrade/SitePackage workflows + focused classpath/composition proof覆盖主要 failure modes；Post-Integration gate明确。

### Rollback / side effects

**PASS** — source/build refactor可整 PR rollback；不改变 schema/dataset；verification使用 ephemeral state。

### Human escalation

**PASS / NOT REQUIRED** — 未出现新的 Goal、Scope、User behavior、Business Boundary、Acceptance、安全或 Repo-split 决策；multi-project 是已接受 Phase 2 architecture candidate中的技术选择，并已完成 required alternative comparison。

## 8. Readiness Decision

**PASS — EU-46 is a Candidate Ready Execution Unit for planning/readiness integration.**

当前仍不能 Execute。只有本 planning/readiness change 完成 exact-head verification、合并到 `main`、取得 Post-Integration Current Evidence，并在新的 Fresh Context 中确认 EU-46 Readiness仍有效且无 base drift 后，EU-46 才获得 Execute Authority。
