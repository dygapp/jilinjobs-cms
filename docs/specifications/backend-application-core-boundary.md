# Backend Application / Core Boundary Specification

## Authority

- `docs/requirements/backend-application-core-boundary.md`
- `docs/requirements/cms-site-package-boundary.md`
- `docs/specifications/cms-site-package-boundary.md`
- GitHub Issue #92
- GitHub Issue #77

## Status

- Phase: **Phase 2A — Backend Application / Core Boundary Foundation**
- Specification: **READY**
- Technical Planning: **REQUIRED**
- Planning baseline: `main@bfd0983e66d392de1722a703356ce581fd1a41ea`
- Current Ready Execution Unit: **NONE until slice-work + readiness-check are integrated**

## 1. Required topology

Phase 2A 完成后，Backend 必须至少形成三个可独立验证的 build / classpath responsibility：

```text
cms-server application ───────────→ shared cms-core
content-migration application ────→ shared cms-core
```

约束：

- `cms-core` 不依赖两个 application；
- `cms-server` 与 `content-migration` 不互相依赖；
- Server Runtime classpath 不包含 Party migration implementation；
- Migration Runtime classpath 不包含 Server Controller / MVC transport / server-only startup implementation。

物理 Gradle layout 由 Technical Plan 在 multi-project 与 lower-complexity source-set alternative 之间决定；Specification 不以“两个 executable JAR”推导具体 Gradle project 数量。

## 2. Shared Core ownership

Shared Core 负责被两个 application 共同消费的 Generic capability。当前 Repository dependency closure 至少确认以下类别属于 Core candidate：

- Column / Article / Page / Navigation / Listing / Advertisement / SiteConfig / Resource 的 domain、validation、Mapper、repository、service/query capability；
- Generic persistence、transaction 与 MyBatis mapper capability；
- Generic CMS Flyway SQL resource；
- Site Package loader / provisioner / bootstrapper / stable asset manifest/catalog/projector 与其 site-neutral runtime configuration；
- shared `cms-metadata.yml` / configuration metadata；
- current resource / static-resource storage and safety capability。

HTTP Controller、request transport DTO、MVC exception handler、Servlet-based static HTTP exposure 不属于 Shared Core ownership。

`MultipartFile` 当前同时被 Resource/StaticResource/Migration 的 storage adapter 使用。Phase 2A 允许 Shared Core 暂时保留对 Spring `MultipartFile` abstraction 的薄依赖，只要这不会引入 Server application、MVC Controller 或 embedded web-server composition；进一步移除该 abstraction 不属于本 Unit 的必要条件。

## 3. Server application contract

Server application 必须：

1. 提供当前 `CmsApplication` 等价的独立 Spring Boot composition root；
2. 组合 Shared Core + Admin/Public controllers + server-only handler / transport；
3. 保持当前 HTTP port/property contract、Admin/Public endpoints 与 `/static/**` behavior；
4. 保持 Generic Flyway → optional Site Package runtime composition → optional Fresh Site bootstrap 的现有 server startup lifecycle；
5. 产生当前 CI / Review Environment 可继续消费的 Server executable JAR；
6. 不依赖或扫描 Content Migration implementation。

## 4. Content Migration application contract

Migration application 必须：

1. 有独立 Spring composition root，不以 `CmsApplication` 为 source；
2. 以 non-web application lifecycle 启动，不监听 HTTP；
3. 组合 Shared Core + Party migration implementation / migration-only mapper / report / CLI；
4. 保持两类现有 import capability：Historical Articles 与 Party Carousel；
5. 保持现有 task report label：`EU29_IMPORT_REPORT`、`EU29_CAROUSEL_IMPORT_REPORT`；
6. 保持 conflict / invalid 导致 CLI failure 的现有 semantics；
7. 允许新增一个明确的 executable dispatcher 作为 additive interface，但不得借此把 Party-specific schema、alias、fingerprint、compatibility rules 泛化为后续 Generic Engine；
8. 配置 Site Package root 时继续在 canonical import 前获得 stable target identity / stable asset composition；不得执行 one-time bootstrap，除非调用方显式提供现有 bootstrap-on-start property。

## 5. Source separation obligations

当前以下混合文件同时包含 Core service/persistence 与 Server HTTP transport，实施时必须以最小语义变更拆开 transport ownership：

- `CmsList.kt`：Mapper / model / service 留 Core；Admin/Public Controller 与 transport request model 进入 Server；
- `Advertisement.kt`：Mapper / model / service 留 Core；Admin/Public Controller 与 request model 进入 Server；
- `SiteConfig.kt`：Mapper / model / service 留 Core；Admin/Public Controller 与 request model 进入 Server；
- `StaticResource.kt`：storage/service/model/safety 留 Core；Admin/Public HTTP Controller / Servlet response 进入 Server。

已有独立 Controller 文件（Column / Article / Navigation / Page / Resource 等）直接属于 Server application；其 domain/service/mapper 文件属于 Core candidate。

`ApiExceptionHandler.kt` 属 Server-only transport。

Current Site Package service files中依赖 `CmsApplication` 的 maintenance CLI entrypoint 必须与 shared service implementation 拆开，避免 Core → Server dependency。Phase 2A 不新增第四个 application；这些既有 provision/bootstrap maintenance CLI 可以作为 Server-side compatibility tooling 保留，只要它们不进入 Content Migration classpath，也不改变普通 Server startup。

## 6. Resource / configuration contract

1. Generic Flyway SQL 只有一份版本化 resource owner，并可由两个 application 的 classpath 读取。
2. Shared CMS metadata 只有一份 Current resource owner。
3. Server `application.yml` 负责 server-specific HTTP / multipart / server port configuration，同时保留 current DB/storage/static/Site Package external property contract。
4. Migration application 使用独立 non-web configuration，但必须保留同名 DB/storage/static/Site Package properties；不得复制 Flyway SQL 或 Site Package instance data。
5. Mapper discovery 必须覆盖 Core mapper 与 migration-only mapper；Server 不应发现 migration-only mapper。
6. Site Package runtime configuration 可以作为 site-neutral Core capability被两个 application 显式/受控组合；不能靠 Server classpath leakage 获得。

## 7. Build and command compatibility

Phase 2A 必须保持以下 repository-level operational capability：

- 从 `backend/` 执行完整 build/test；
- 构建 Server executable JAR；
- 执行 `importPartyHistoricalContent`；
- 执行 `importPartyCarousel`；
- 执行既有 Site Package verifier；
- Review Environment 与 Integrated Browser 可以继续启动相同 Server Runtime behavior。

Technical Plan 可以通过 root compatibility tasks 或 workflow command normalization 实现上述能力；不得留下“代码已拆但 Canonical/Upgrade workflow 不再触发或不再实际验证”的假通过状态。

Server JAR 的 repository consumer contract 当前为：

```text
backend/build/libs/jilinjobs-cms-backend-0.1.0-SNAPSHOT.jar
```

Phase 2A 应保持该路径 / 名称，除非证明必须改变并原子修复所有消费者；当前 dependency closure 没有发现改变它的必要性。

## 8. Verification mapping

### 8.1 Build / dependency

- Shared Core build PASS；
- Server app build + bootJar PASS；
- Migration app build + bootJar PASS；
- dependency/classpath check 证明两个 app 只单向依赖 Core；
- Server artifact / classpath 不含 Party migration implementation；
- Migration artifact / runtime context 不含 Server controllers / handler。

### 8.2 Server runtime

- current Server JAR 通过 `java -jar` 启动；
- Generic Flyway current V1/V2；
- Site Package stable structure / bootstrap / assets current lifecycle；
- Public/Admin/API tests与 Integrated Browser PASS。

### 8.3 Migration runtime

- migration executable以 non-web mode 独立启动；
- Historical article first import = 183 created on Fresh current dataset；
- carousel first import = 4 current accepted items；
- second import idempotency；
- fingerprint/conflict failure；
- EU-29 accepted → EU-30 current compatibility；
- resource bytes / mappings / Runtime reconciliation；
- no Server Controller bean / no HTTP listener。

### 8.4 Workflow coverage

以下 workflow 的 path filters / commands 必须与新 source ownership 原子同步：

- `.github/workflows/ci.yml`；
- `.github/workflows/canonical-migration-verify.yml`；
- `.github/workflows/eu30-migration-upgrade-verify.yml`；
- `.github/workflows/review-environment.yml`；
- `.github/workflows/site-package-verification.yml`。

## 9. Non-goals

- 不改变 Party canonical dataset、migration schema或兼容规则；
- 不将 migration-only models提升为新的 public Generic SPI；
- 不执行 Phase 2B Generic Content Migration Engine；
- 不执行 Phase 2C Party de-specialization；
- 不进入 Phase 3 / Issue #60 / E1～E3；
- 不重构 Public Renderer；
- 不新增第四个 Backend application；
- 不拆 Git Repository或引入 Clean Architecture / domain-per-module。

## 10. Specification readiness

Required topology、ownership、behavior preservation、resource/config lifecycle、workflow coverage 与 verification evidence 已可测试地定义；剩余问题是 **HOW**：Gradle project layout、文件搬迁 map、composition roots、resource placement与 compatibility task wiring。

因此本 Specification **READY**，且 Technical Planning **REQUIRED**。Technical Plan 完成前不得执行 `slice-work`。
