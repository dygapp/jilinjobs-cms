# Backend

`backend/` 是 Generic CMS Backend 的统一 Gradle 入口。EU-46 将原单体 Spring Boot 工程收敛为一个共享 Core 与两个独立 executable application；EU-47 在独立 Content Migration application 内建立了 site-neutral Canonical Dataset → CMS Runtime capability，同时保持 Party compatibility path 独立。

- `modules/cms-core/`：Generic CMS domain / service / Mapper / persistence、Generic Flyway、Site Package 与共享 metadata；不包含 HTTP Controller、Server application 或 migration application composition；
- `apps/cms-server/`：`CmsApplication`、Admin/Public HTTP transport、Server configuration、兼容 Site Package CLI 与现有 Server/runtime verifier；
- `apps/content-migration/`：独立 non-web `ContentMigrationApplication`；持有 site-neutral Generic canonical loader/preflight、Article/ListItem importer、stable legacy mapping/report capability，以及仍由后续 Phase 2C 管理的 Party historical migration / compatibility implementation；不依赖 `cms-server`；
- `build.gradle.kts`：保持 `clean`、`test`、`bootJar`、`importParty*`、`provisionSitePackage`、`bootstrapSitePackage` 与既有 `verifySitePackage*` root compatibility tasks，并提供 `importCanonicalContent` / `verifyGenericContentMigration`；
- Server artifact 继续输出为 `build/libs/jilinjobs-cms-backend-0.1.0-SNAPSHOT.jar`。

Current Backend Technical Authority：

- application/core boundary：`../docs/technical/backend-application-core-boundary.md`；
- Generic Content Migration：`../docs/technical/generic-content-migration-application.md`。

长期产品与 Site Package 边界仍由 `../docs/specifications/cms-core.md`、`../docs/requirements/cms-site-package-boundary.md` 与 `../docs/specifications/cms-site-package-boundary.md` 定义。EU-47 完成不代表 Party current dataset/profile 已迁到 Generic path；Party aliases、accepted fingerprints 与 EU-29→EU-30 compatibility 仍属于 Phase 2C Planning Candidate。

本 README 只说明 Backend subtree ownership，不建立新的产品或架构 Authority。具体验证以 `../docs/technical/verification-strategy.md`、当前 Technical Authority、`../docs/work/archive/eu46-backend-application-core-boundary-foundation.md`、`../docs/work/archive/eu47-generic-content-migration-application-foundation.md` 与 Repository Current Evidence 为准。
