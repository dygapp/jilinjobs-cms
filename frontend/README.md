# Frontend

`frontend/` 包含两个同级独立 Vue / Vite frontend：

- `admin/`：CMS 管理端；
- `public-site/`：Main / Party Public Renderer；
- `nginx.e2e.conf`：Integrated Browser / E2E 环境的前端路由支持配置。

两个 frontend 共享 Spring Boot CMS Backend，但不因此合并应用责任。Public Renderer 的 replaceability / source isolation contract 由 `../docs/requirements/public-frontend-replaceability.md` 与对应 Specification / Technical Authority 约束。

具体构建脚本以各工程 `package.json` 为准；本 README 不建立第二套产品 Authority。
