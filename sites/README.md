# Site Packages

`sites/` 保存 Generic Site Package schema 与具体 Site 的版本化 package source。

- `schema/`：Site Package machine-readable schema；
- `jilinjobs/`：当前 JilinJobs Site Package，具体 workspace contract 见 `jilinjobs/README.md`。

## Current Authority locator

- Product Requirement：`../docs/requirements/information-publishing.md`
- CMS Domain / stable identity / content ownership：`../docs/requirements/cms-domain.md`
- Site Definition / Runtime / Historical Migration 长期边界：`../docs/architecture/cms-architecture.md`
- Backend implementation contract：`../docs/technical/backend-service.md`
- Historical Migration workspace：`../data-migrations/**`

`sites/jilinjobs/**` 对当前 manifest、稳定结构、bootstrap defaults 与 stable assets 等 versioned Site Definition 具体事实承担 bounded canonical responsibility，但不因此拥有 Product Requirement。Historical Content Migration 继续位于 `../data-migrations/**`；历史 Requirement Change、旧 Specification / Technical Plan 与 Issue timeline 只在当前 Authority 要求 lineage / audit 时定向读取。

本 README 只说明 subtree ownership 与 locator，不建立第二套 global Authority、Roadmap 或 Current Gate。