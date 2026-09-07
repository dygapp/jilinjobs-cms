# Backend

`backend/` 是 Generic CMS Spring Boot Backend 的工程入口，负责当前 CMS Core 业务校验、持久化、Public / Admin API、StaticResource 与 Site Package / Historical Migration 所需的通用 application capability。

- 构建配置：`build.gradle.kts`、`settings.gradle.kts`；
- 源码 / resources：`src/`；
- Current Backend Technical Authority：`../docs/technical/backend-service.md`；
- CMS Product contract：`../docs/specifications/cms-core.md`；
- Site Package boundary：`../docs/requirements/cms-site-package-boundary.md`、`../docs/specifications/cms-site-package-boundary.md`。

本 README 只说明 Backend subtree ownership，不建立新的产品或架构 Authority。具体验证以 `../docs/technical/verification-strategy.md` 与 Repository CI 为准。
