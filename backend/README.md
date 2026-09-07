# Backend

`backend/` 是 Generic CMS Backend 的统一 Gradle 入口。EU-46 将原单体 Spring Boot 工程收敛为一个共享 Core 与两个独立 executable application，同时保留仓库级兼容任务与 Server JAR 外部路径。

- `modules/cms-core/`：Generic CMS domain / service / Mapper / persistence、Generic Flyway、Site Package 与共享 metadata；不包含 HTTP Controller、Server application 或 Party migration；
- `apps/cms-server/`：`CmsApplication`、Admin/Public HTTP transport、Server configuration、兼容 Site Package CLI 与现有 Server/runtime verifier；
- `apps/content-migration/`：独立 non-web `ContentMigrationApplication`、Party historical migration implementation 与 migration-only composition；不依赖 `cms-server`；
- `build.gradle.kts`：保持 `clean`、`test`、`bootJar`、`importParty*`、`provisionSitePackage`、`bootstrapSitePackage` 与既有 `verifySitePackage*` root compatibility tasks；
- Server artifact 继续输出为 `build/libs/jilinjobs-cms-backend-0.1.0-SNAPSHOT.jar`。

Current Backend Technical Authority：`../docs/technical/backend-application-core-boundary.md`。长期产品与 Site Package 边界仍由 `../docs/specifications/cms-core.md`、`../docs/requirements/cms-site-package-boundary.md` 与 `../docs/specifications/cms-site-package-boundary.md` 定义。

本 README 只说明 Backend subtree ownership，不建立新的产品或架构 Authority。具体验证以 `../docs/technical/verification-strategy.md`、EU-46 work artifact 与 Repository CI 为准。
