# Frontend

`frontend/` 包含两个同级 Vue / Vite frontend：

- `admin/`：CMS 管理端；
- `public-site/`：Main / Party Public Renderer；
- `nginx.e2e.conf`：Integrated Browser / E2E 环境的前端路由支持配置。

两个 frontend 共享 Spring Boot CMS Backend，但不因此合并 application responsibility。具体源码、`package.json`、Router / component / build configuration 是当前 implementation evidence，不建立第二份 Product / Domain Authority。

## Current Authority locator

- Product Requirement：`../docs/requirements/information-publishing.md`
- CMS Domain：`../docs/requirements/cms-domain.md`
- 长期 CMS Architecture：`../docs/architecture/cms-architecture.md`
- Admin observable behavior：`../docs/specifications/admin-site.md`
- Public observable behavior：`../docs/specifications/public-site.md`
- Party bounded behavior：`../docs/specifications/party.md`
- Admin Technical Authority：`../docs/technical/admin-frontend.md`
- Public Technical Authority：`../docs/technical/public-site-frontend.md`
- Verification Strategy：`../docs/technical/verification-strategy.md`

本 README 只说明 Frontend subtree ownership 与 Current locator。历史 frontend convergence / replaceability change、完成态 EU / Planning 与旧同名三件套只作为 traceability 定向读取，不参与 ordinary Fresh Context。