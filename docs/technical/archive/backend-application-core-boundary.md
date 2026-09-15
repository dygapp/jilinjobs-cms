# Backend Application / Core Boundary Technical Plan

## Authority

- `docs/requirements/backend-application-core-boundary.md`
- `docs/specifications/backend-application-core-boundary.md`
- `docs/technical/backend-service.md`
- `docs/technical/cms-site-package-boundary.md`
- GitHub Issue #92 / Issue #77

## Status

- Phase: **Phase 2A — Backend Application / Core Boundary Foundation**
- Planning baseline: `main@bfd0983e66d392de1722a703356ce581fd1a41ea`
- Dependency closure: **COMPLETE**
- Technical Plan: **READY**
- Architecture choice: **standard Gradle multi-project**
- Current Ready Execution Unit: **NONE until slice-work + readiness-check are integrated**

## 1. Dependency closure findings

Current Repository state proves the boundary cannot be achieved by moving four migration files only:

1. `backend` is currently one Gradle project; `settings.gradle.kts` only names the root project.
2. `importPartyHistoricalContent` and `importPartyCarousel` both run on `sourceSets["main"].runtimeClasspath`.
3. All four Party migration implementations use `SpringApplicationBuilder(CmsApplication::class.java).web(WebApplicationType.NONE)`; `CmsApplication` is a root-package `@SpringBootApplication`, so migration inherits the full Server component scan.
4. Migration code directly consumes shared Article / Column / List / Resource / StaticResource / MyBatis / transaction capability, plus migration-only legacy mapping mapper and Party compatibility logic.
5. `SitePackageRuntimeCompositionConfiguration` is a site-neutral shared lifecycle capability activated by `cms.site-package.root` after Flyway. Current migration tasks rely on the same stable Site identities and asset projection rather than owning a second Site bootstrap model.
6. `CmsList.kt`、`Advertisement.kt`、`SiteConfig.kt`、`StaticResource.kt` each mix shared domain/service/persistence with REST transport, so true classpath separation requires a bounded transport split regardless of Gradle shape.
7. Site Package provision/bootstrap service files contain maintenance CLI entrypoints that import `CmsApplication`; they must be separated from shared Core implementation to avoid Core → Server dependency.
8. Existing Site Package Runtime / Bootstrap verification mains also use `CmsApplication`, showing that server-runtime composition verification and Core service verification need explicit ownership after the split.
9. CI / Review Environment consume the Server JAR at `backend/build/libs/jilinjobs-cms-backend-0.1.0-SNAPSHOT.jar`.
10. Canonical Migration / EU-30 Upgrade / Review Environment invoke the two current migration Gradle tasks and rely on current report labels / accepted data assertions.
11. Canonical / Site Package workflow path filters currently point at the monolithic source directories; moving ownership without synchronizing filters can create verification gaps.

## 2. Gradle shape decision

### 2.1 Chosen shape

Use a standard Gradle multi-project layout:

```text
backend/
├── settings.gradle.kts
├── build.gradle.kts                 # aggregate / compatibility task wiring
├── modules/
│   └── cms-core/
│       ├── build.gradle.kts
│       └── src/
└── apps/
    ├── cms-server/
    │   ├── build.gradle.kts
    │   └── src/
    └── content-migration/
        ├── build.gradle.kts
        └── src/
```

Gradle dependencies:

```text
:apps:cms-server ───────────→ :modules:cms-core
:apps:content-migration ────→ :modules:cms-core
```

No reverse edges and no app-to-app edge.

### 2.2 Why the source-set alternative is rejected for 2A

The accepted Planning Authority required an explicit lower-complexity comparison. Current dependency closure shows a single-project source-set solution is **not lower complexity** for this Repository:

- the same four mixed transport/service files still need semantic split;
- migration/server need isolated runtime classpaths, application resources, BootJar main classes and tests;
- root `main.runtimeClasspath` reuse is explicitly insufficient;
- custom source-set dependency/classpath/resource wiring would be bespoke Gradle infrastructure whose only purpose is to emulate standard project isolation;
- multi-project project dependencies provide a direct, inspectable proof that Server classes are absent from Migration runtime and Migration classes are absent from Server runtime.

The decision is therefore based on dependency/classpath/composition clarity, not executable-JAR count.

## 3. Project ownership map

### 3.1 `modules/cms-core`

Move current site-neutral shared source while preserving Kotlin package names. Core includes:

- domain/model/validation/service/query/repository/Mapper code for Column, Article, Page, Navigation, Listing, Advertisement, SiteConfig, Resource;
- shared common policy;
- `StaticResourceService` model/storage/safety capability;
- Site Package loader/provisioner/bootstrapper/asset catalog/projector and site-neutral runtime configuration;
- generic persistence / transaction capability;
- `CmsMetadataProperties`;
- `cms-metadata.yml`;
- `db/migration/V1__current_cms_schema.sql` + `V2__site_provisioning_schema_capabilities.sql` as the **single** Flyway SQL resource authority.

Core does not contain:

- `CmsApplication`;
- REST Controllers / HTTP request transport;
- `ApiExceptionHandler`;
- Party migration classes / migration-only mapper;
- executable application launcher.

### 3.2 `apps/cms-server`

Server owns:

- `CmsApplication` and server `main`;
- existing Admin/Public Controller classes;
- REST transport request DTOs split from mixed files;
- `ApiExceptionHandler`;
- Public static-resource Servlet / HTTP projection;
- server-specific `application.yml`;
- existing server-runtime / browser-supporting tests and Site Package runtime composition verifiers that intentionally exercise `CmsApplication`;
- Site Package provision/bootstrap maintenance CLI entrypoints separated from Core service files as 2A compatibility tooling.

The maintenance CLI exception is deliberate: Phase 2A does not introduce a fourth application. These plain CLI entrypoints do not participate in normal server startup and do not enter Content Migration classpath.

### 3.3 `apps/content-migration`

Migration owns:

- all four current Party migration implementation files as one source-responsibility unit;
- migration-only legacy mapping / upgrade mapper and Party compatibility rules currently located in those files;
- `ContentMigrationApplication` independent Spring composition root;
- non-web migration `application.yml`;
- existing article/carousel task entrypoints;
- an additive executable dispatcher if needed to give the BootJar one explicit main class.

The dispatcher may expose explicit Party subcommands, for example:

```text
java -jar jilinjobs-cms-content-migration-0.1.0-SNAPSHOT.jar party-content <snapshot-root> [Spring args...]
java -jar jilinjobs-cms-content-migration-0.1.0-SNAPSHOT.jar party-carousel <snapshot-root> [Spring args...]
```

This is application packaging only. It must delegate to the existing Party services and preserve `EU29_IMPORT_REPORT` / `EU29_CAROUSEL_IMPORT_REPORT`, conflict/invalid exit behavior and all accepted compatibility rules. It is not the Phase 2B Generic Engine.

## 4. Mandatory mixed-file split

Only split transport/CLI from shared logic; do not otherwise rename/restructure domain packages.

| Current file | Core ownership | Server ownership |
|---|---|---|
| `listing/CmsList.kt` | models, Mapper, service, normalization | Admin/Public Controllers + Save request DTOs |
| `advertisement/Advertisement.kt` | models, Mapper, service | Admin/Public Controllers + Save request DTOs |
| `siteconfig/SiteConfig.kt` | models, Mapper, service | Admin/Public Controllers + request DTOs |
| `staticresource/StaticResource.kt` | model/exceptions + `StaticResourceService` | Admin/Public static-resource Controllers / Servlet HTTP response |
| `provisioning/SitePackageProvisioning.kt` | loader/provisioner/shared logic | current provisioning `main` moved to compatibility CLI file |
| `provisioning/SitePackageBootstrap.kt` | bootstrapper + runtime configuration | `SitePackageBootstrapCli` moved to compatibility CLI file |

Existing already-separated Controller files move to Server without semantic rewrite. Existing shared service/mapper files move to Core. Kotlin package names remain current unless a compile-time collision requires a narrowly documented exception.

## 5. Dependency and framework configuration

### 5.1 Core dependencies

Core should use ordinary library dependencies/BOM management, not the Spring Boot executable plugin. It may depend on the Spring / MyBatis / Flyway / Jackson libraries required by shared services/configuration.

`MultipartFile` can remain a thin shared Spring Web abstraction in 2A. Core must not require `spring-boot-starter-webmvc`, embedded server runtime or Server application project solely because of it.

### 5.2 Server dependencies

Server applies Spring Boot and Kotlin Spring plugins, depends on Core, and owns `spring-boot-starter-webmvc` plus server runtime dependencies.

Server `bootJar` must preserve the current external repository consumer contract:

```text
backend/build/libs/jilinjobs-cms-backend-0.1.0-SNAPSHOT.jar
```

The subproject BootJar can explicitly target the root build/libs directory and fixed archive name so CI / Review environment runtime topology does not change.

### 5.3 Migration dependencies

Migration applies Spring Boot/Kotlin Spring, depends on Core, and does **not** depend on `:apps:cms-server` or webmvc starter. Runtime MySQL/Flyway support must be present so current Fresh DB behavior is preserved.

Migration BootJar output remains app-owned, e.g.:

```text
backend/apps/content-migration/build/libs/jilinjobs-cms-content-migration-0.1.0-SNAPSHOT.jar
```

No current external consumer depends on this path; exact path is verified in 2A rather than promoted to long-term product contract.

## 6. Spring composition roots

### 6.1 Server

`CmsApplication` remains root package `com.jilinjobs.cms`; its module classpath contains Server + Core but not Migration. Root component scan therefore sees current server transport and shared Core capability only.

### 6.2 Migration

Create a distinct `ContentMigrationApplication` composition root in package `com.jilinjobs.cms` or an equivalent explicit scan/import configuration. Its classpath contains Migration + Core but not Server.

Migration must use `WebApplicationType.NONE` / equivalent non-web Boot configuration and must not start an HTTP listener.

Because Server project is absent from its classpath, a package-root scan can discover Core + migration-only beans without discovering Server controllers. Verification must still assert this property instead of assuming it from layout.

### 6.3 Mapper / configuration ownership

- Core `@Mapper` interfaces are visible to both apps.
- migration-only legacy mapping mappers are visible only to Migration.
- Server does not discover migration mapper/service.
- `CmsMetadataProperties` and `cms-metadata.yml` remain single shared owners in Core.
- Jackson / transaction manager / datasource / MyBatis are application autoconfiguration consuming Core definitions, not duplicate hand-built stacks.

## 7. Flyway / Site Package / resource lifecycle

### 7.1 Flyway

Move current V1/V2 SQL resources to Core resources. Both executable apps include Core on runtime classpath, so `classpath:db/migration` resolves the same single authority.

Preserve current behavior: both Server and Migration application contexts allow Spring Boot Flyway to bring a Fresh DB to current Generic schema. No duplicate copy of V1/V2 is created in either app.

### 7.2 Site Package

Keep site-neutral `SitePackageRuntimeCompositionConfiguration` in Core. With `cms.site-package.root` configured:

```text
Flyway initializer
→ stable Site Package reconcile
→ stable asset projection
→ application-specific work
```

Migration Gradle tasks continue setting the JilinJobs Site Package root so stable target identities exist before canonical import. Bootstrap remains opt-in through the existing property and is not enabled by normal migration task wiring.

### 7.3 Application configuration

Server `application.yml` keeps current HTTP/multipart/server port plus DB/storage/static external properties.

Migration receives a dedicated non-web `application.yml` with the same DB/storage/static/Site Package property names and shared `cms-metadata.yml` import, but no server-port or MVC-specific responsibility.

Do not duplicate Site Package files or Flyway SQL.

## 8. Build compatibility facade

Keep `backend/` as the user/workflow Gradle entry. Root build provides explicit aggregate / compatibility tasks so existing repository commands stay meaningful while implementation is multi-project.

Required root behavior:

- `clean` / `test` cover affected subprojects;
- `bootJar` continues to produce the current Server JAR contract;
- `importPartyHistoricalContent --args=...` executes the Migration app runtime classpath and V2 article main/dispatcher;
- `importPartyCarousel --args=...` executes Migration app runtime classpath;
- Site Package provision/bootstrap CLI capability remains callable;
- existing `verifySitePackage*` task names remain callable and run the Server/Core verifier classpath chosen by their actual responsibility.

If Gradle DSL makes an exact root JavaExec compatibility task materially more complex than updating a repository-owned workflow, the implementation may normalize a command to an explicit subproject task **only if all current repository consumers are updated atomically and the work artifact records the deviation**. Default is to preserve current root task names.

## 9. Test / verifier placement

- Existing tests may initially remain in Server test source if they intentionally exercise `CmsApplication`; avoid a broad test taxonomy rewrite.
- Pure Core tests can move with their source when straightforward.
- `RuntimeSitePackageCompositionVerification` and `SiteBootstrapBaselineSeparationVerification` intentionally test Server `CmsApplication` composition and should remain Server-side verification unless rewritten to an explicit app-agnostic composition is required for the boundary.
- Add focused migration context verification proving:
  - no Server Controller / `ApiExceptionHandler` class/bean is present;
  - no HTTP listener starts;
  - Core service / Mapper / Flyway / SitePackage capabilities required by import are present.
- Add dependency/artifact inspection sufficient to prove Server JAR does not contain Party migration classes and Migration JAR does not contain Server Controller classes.

## 10. Workflow normalization

Update path filters so file moves do not suppress relevant verification:

### `ci.yml`

- backend build must cover Core + both apps;
- uploaded Server artifact remains current filename/path;
- Integrated Browser runtime command remains unchanged.

### `canonical-migration-verify.yml`

Trigger on new Core/Migration source, Gradle build/settings, Generic Flyway resources and workflow itself. Continue full Fresh DB first import + second import + resource/runtime checks.

### `eu30-migration-upgrade-verify.yml`

Trigger on new Core/Migration/listing ownership, Gradle build/settings, Flyway resources and workflow itself. Preserve EU-29 pinned accepted baseline → EU-30 current upgrade proof.

### `review-environment.yml`

Build new Backend topology but consume unchanged Server JAR path/name. Party import commands must resolve to new Migration application rather than Server classpath.

### `site-package-verification.yml`

Trigger on new Core provisioning/static-resource locations, Server runtime verifiers and Gradle build/settings; preserve existing verifier task semantics.

## 11. Execution sequence

Recommended implementation order inside one EU branch:

1. establish Gradle projects/dependencies and preserve root compatibility/build entry;
2. move shared source/resources to Core without semantic edits;
3. split four mixed HTTP transport files and provision/bootstrap CLI entrypoints;
4. move Server application/controllers/config/tests;
5. move all four Party migration files together and create Migration composition root/config/BootJar;
6. wire root migration / verifier tasks;
7. add focused classpath/composition verification;
8. atomically normalize workflow path filters/build wiring;
9. run static diff/dependency checks, targeted Gradle tests, Canonical/Upgrade/Site Package exact-head workflows, repository CI and review convergence.

Do not leave a merged intermediate state where Server and Migration both own the same classes/resources or where workflows stop observing moved source.

## 12. Rollback and side effects

Implementation is source/build/composition refactoring. Rollback boundary is the entire EU PR.

No migration dataset bytes or DB schema semantics are intentionally changed. Verification uses Fresh/ephemeral DBs and repository test runtime roots. Any unexpected change to canonical counts, fingerprint behavior, API contract, Site Package lifecycle or user-visible behavior invalidates Readiness and returns the work to Planning rather than being accepted as incidental refactor fallout.

## 13. Technical readiness

The build choice, file ownership map, composition roots, resource ownership, lifecycle, compatibility wiring, verifier placement and workflow impact are now explicit enough for slicing.

Technical Plan: **READY**. Next step is `slice-work`; this document does not itself create an Execution Unit or Execute Authority.
