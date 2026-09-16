# Backend

`backend/` 是 Generic CMS Backend 的统一 Gradle workspace。当前 Repository implementation 由共享 Core 与两个 application responsibility 组成：

- `modules/cms-core/`：Generic CMS domain、service、persistence、Generic schema / Site Package / migration primitives 与共享 metadata；
- `apps/cms-server/`：Admin / Public HTTP transport、ordinary server lifecycle 与 server-side composition；
- `apps/content-migration/`：Canonical Historical Migration 的 non-web application composition；
- `build.gradle.kts`、`settings.gradle.kts`：当前 Gradle project / task composition 的 implementation evidence。

以上目录与 task 是当前实现事实，不因此成为 Product / Domain / Architecture Authority。

## Current Authority locator

- Product Requirement：`../docs/requirements/information-publishing.md`
- CMS Domain：`../docs/requirements/cms-domain.md`
- 长期 CMS Architecture：`../docs/architecture/cms-architecture.md`
- Backend Technical Authority：`../docs/technical/backend-service.md`
- Verification Strategy：`../docs/technical/verification-strategy.md`
- JilinJobs Site Definition：`../sites/jilinjobs/**`
- Historical Migration workspace：`../data-migrations/**`

Feature 可观察行为按需进入 `../docs/specifications/README.md` 定位当前 Specification。

本 README 只说明 Backend subtree ownership 与 Current locator，不缓存已完成 EU、历史 Phase、Planning Candidate、exact migration inventory、Workflow 状态或旧 Requirement / Specification / Technical 三件套。需要历史实现 lineage 时，从当前 Authority 明确要求的 `docs/**/archive/**`、`docs/work/archive/**` 或 GitHub native Evidence 定向恢复。