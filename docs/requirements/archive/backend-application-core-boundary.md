# Backend Application / Core Boundary Requirement

## Status

- Parent Planning Authority: GitHub Issue #92 / `docs/project/pre-e1e3-convergence-plan.md`
- Related long-term boundary: GitHub Issue #77 / `docs/requirements/cms-site-package-boundary.md`
- Phase: **Phase 2A — Backend Application / Core Boundary Foundation**
- Requirement: **READY**
- Current repository baseline for planning: `main@bfd0983e66d392de1722a703356ce581fd1a41ea`
- Current Ready Execution Unit: **NONE until slice-work + readiness-check are integrated**

## 1. Intent

在不改变当前 CMS 产品行为、Party Canonical Migration 数据语义或 JilinJobs Site Package 生命周期的前提下，把当前单一 Backend application classpath 收敛为明确的 application / shared-core boundary，使普通 CMS Server 与 Historical Content Migration 可以独立启动、独立打包、独立验证，同时只依赖同一套 site-neutral Generic CMS Core capability。

当前 Party migration 通过 `sourceSets["main"].runtimeClasspath` 运行，并以 `CmsApplication` 作为 Spring source；`WebApplicationType.NONE` 只关闭 Web server，不形成独立 application composition。Phase 2A 必须修复这一结构性耦合，但不得提前进入 Generic Migration Engine 或 Main / Party 数据模型泛化。

## 2. Required boundary

### 2.1 Shared Generic CMS Core

1. 共享 Core 必须承载 Server 与 Historical Migration 都真正复用的 site-neutral capability，包括必要的领域模型、业务校验、持久化 / Mapper、transaction、Resource / StaticResource service、Generic Flyway schema resource、Site Package provision/bootstrap/asset capability 与共享配置 metadata。
2. Shared Core 不得依赖 CMS Server application，也不得依赖 Content Migration application。
3. Product-level “Generic CMS Core” 不等于所有 Admin/Public HTTP transport 都必须进入 shared build artifact；Controller、MVC transport、server-only exception handling 与 HTTP startup responsibility 应属于 Server application。
4. Core 内部继续以 package-level modularity 为主；本 Phase 不把每个 CMS domain 机械拆为独立 Gradle module。

### 2.2 CMS Server application

1. CMS Server 必须拥有独立 Spring Boot application lifecycle 与 executable JAR。
2. Server application 负责 Admin/Public HTTP transport、MVC / static HTTP exposure、server-only startup composition 与当前 Runtime server behavior。
3. Server application 可以依赖 Shared Core，但不得依赖 Historical Migration application 或 Party migration implementation。
4. 普通 Server startup 不得扫描、实例化或携带 Party migration service / mapper / launcher 作为其 application responsibility。

### 2.3 Content Migration application

1. Historical Content Migration 必须拥有独立 Spring application composition 与 executable artifact / CLI lifecycle。
2. Migration application 可以依赖 Shared Core，但不得依赖 CMS Server application。
3. Migration startup 不得依赖 `CmsApplication`，不得通过 Server root component scan 间接取得 Controller / MVC / server-only hook。
4. Migration application 不得启动 HTTP server；其成功 / 失败必须以 CLI report 与 process exit semantics 表达。
5. 当前四个 Party migration implementation 文件及其相互兼容关系必须作为一个行为保持单元迁出 Server source ownership：
   - `PartyHistoricalContentMigration.kt`
   - `PartyHistoricalContentMigrationV2.kt`
   - `PartyCarouselMigration.kt`
   - `PartyCarouselMigrationV2.kt`
6. 当前 article / carousel 的独立 import capability 必须继续存在；可以在 Technical Plan 中增加一个 additive executable dispatcher，但不得静默删除现有两类 migration entry capability。

## 3. Persistence / configuration / lifecycle requirements

1. Generic Flyway SQL 必须继续只有一份 Current resource authority；application 拆分不得复制两套 migration lineage。
2. 当前行为保持要求两个 application 都能取得同一 Generic CMS schema / Mapper / transaction / Jackson / configuration capability；不得因为 build boundary 产生隐式 mapper/resource/config 缺失。
3. Historical Migration 在配置 Site Package root 时，必须继续满足当前 lifecycle dependency：Generic Schema ready → stable Site Package reconcile / stable asset projection → canonical import。不得把 Site Package 或 Historical Migration 混回 Flyway。
4. `cms.site-package.bootstrap-on-start` 的 one-time bootstrap 语义不得因 Migration application 拆分而被扩大；普通 canonical import 继续不隐式执行 Site bootstrap。
5. 当前 external property contracts（DB、storage、static root、Site Package root）必须保持兼容。
6. Database transaction rollback 不得被描述为能够自动 rollback 已产生的 filesystem / resource side effect；本 Phase 只保持现有语义并让该边界显式，不新增跨 DB/filesystem distributed transaction。

## 4. Behavior-preservation requirements

Phase 2A 必须保持：

- 当前 Admin / Public API 路径与 response semantics；
- Public/Admin browser-visible behavior；
- Generic Fresh DB active Flyway lineage；
- JilinJobs stable structure / one-time bootstrap / stable asset projection contract；
- Party current canonical dataset：183 Articles + 4 accepted carousel items；
- first import / second import idempotency；
- legacy identity / source fingerprint conflict semantics；
- EU-29 accepted → EU-30 current upgrade compatibility，包括 carousel position 2 LINK → ARTICLE accepted transition；
- canonical resource integrity与 Runtime file bytes；
- current report labels / failure-on-conflict-or-invalid semantics for existing Party import tasks。

## 5. Verification requirements

进入 Integration 前至少证明：

1. Shared Core、CMS Server、Content Migration 三个 build boundary 独立编译 / 测试成立；
2. Server executable JAR 单独运行并保持当前 API、Site Package / Flyway 与 browser verification；
3. Content Migration executable / CLI 可以在不依赖 Server application artifact 的情况下运行，且不监听 HTTP；
4. classpath / dependency proof 显示 `cms-server → core`、`content-migration → core`，无 reverse dependency、无 migration → server；
5. Migration application context 中不存在 Server Controller / server-only application bean；普通 Server context 中不存在 Party migration service / mapper；
6. Generic Flyway / Mapper / transaction / Jackson / config / Site Package lifecycle 在两个 application 中按各自需要正确组合；
7. Canonical Migration Verification 与 EU-30 Upgrade Verification 在新 boundary 下完整 PASS；
8. 当前 Site Package verifiers、Backend tests、Public/Admin build 与 Integrated Browser regression PASS；
9. changed-file scope 只包含实现该 application/core boundary 与其 build/workflow/verification wiring 所需的最小变更。

## 6. Non-goals

Phase 2A 不做：

- 不把 Party migration schema / aliases / fingerprints / accepted upgrade policy 泛化为 Generic Migration Engine；该工作属于后续 Phase 2B / 2C；
- 不开始 Main Site E3 canonical migration；
- 不修改 `data-migrations/party/v1/**` accepted dataset；
- 不改变产品 API、页面、视觉或运营行为；
- 不引入 domain-per-module、Clean Architecture、plugin framework 或新的 public SPI；
- 不拆 Git Repository、不引入 submodule；
- 不选择新的 Public Renderer；
- 不进入 Phase 3 或 Issue #60 / E1～E3；
- 不更新 `agentic-dev` baseline。

## 7. Requirement readiness

Goal、Scope、行为保持义务、application/core dependency direction、Product / Historical Migration / Site Package boundary 与 Acceptance dimensions 已由 Issue #92、Issue #77 和当前 Repository state 闭合。

本 Requirement **READY**。后续必须先形成 Ready Specification 与必要 Technical Plan，再执行 `slice-work → readiness-check`；本文件本身不授予 Execute Authority。
