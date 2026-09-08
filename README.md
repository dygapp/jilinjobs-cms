# jilinjobs-cms

`jilinjobs-cms` 是吉林省智慧就业云平台“信息发布与网站服务”相关能力的 Consumer Repository，负责通用 CMS、中心主站 / 中心党建公开站、JilinJobs Site Package 与历史内容迁移的版本化实现。

## 当前范围

当前已接受的长期边界分为四层：

1. **Generic CMS Core**：Spring Boot Backend 提供通用 CMS 业务模型、API、Schema evolution 与 site-neutral provisioning capability；
2. **JilinJobs Site Package**：`sites/jilinjobs/**` 持有稳定站点结构、Fresh Site one-time bootstrap 与 stable Site assets；
3. **Historical Content Migration**：`data-migrations/**` 持有 canonical historical data、provenance、compatibility 与迁移输入；
4. **Replaceable Public Renderer**：`frontend/public-site` 是稳定 Public API / URL / Site Data Contract 的一个 Vue / Vite consumer。

管理端位于 `frontend/admin`，与公开站是同级独立前端工程并共享 Spring Boot CMS Backend。Main / Party 继续保持各自 Site / Theme / Router 边界，同时复用已接受的公共 CMS 与 Shared Shell contract。

当前产品范围、行为与验收标准不得从本 README 的摘要反向扩展；详细 Authority 由 `AGENTS.md` 与 `docs/README.md` 指向的 Current documents 决定。

## Repository Authority 与 Fresh Context

Fresh Context 按以下入口恢复，避免把历史执行记录当作当前 Authority：

1. `AGENTS.md`：Repository Governance、Authority Boundary、Fresh Context 与操作规则；
2. 本 `README.md`：稳定项目入口与当前 Planning Gate；
3. `docs/README.md`：Documentation Authority Map；
4. `docs/project/project-roadmap.md` 与 `docs/project/development-method.md`：当前路线与 Consumer-local Method；
5. 当前任务直接相关的 Requirement / Specification / Technical Authority、GitHub Issue / PR / Actions 与 Ready Execution Unit。

`docs/**/archive/**` 与 `docs/work/archive/**` 默认只承担 traceability / historical evidence，不参与 Fresh Context Current Authority 恢复，除非当前 Authority 明确要求读取。

## 当前 Planning Gate

当前 Planning Priority：**GitHub Issue #92 — E1～E3 前置 Repository Authority 与 Migration Architecture 收敛路线**。

- Phase 0 已完成；
- Phase 1 Repository Documentation Authority Convergence 已完成；
- Phase 2A **EU-46 — Backend Application / Core Boundary Foundation** 已完成并归档；
- Phase 2B **EU-47 — Generic Content Migration Application Foundation** 已完成并归档；
- Phase 2C **EU-48 — Party Migration De-specialization & Compatibility** 已完成 implementation / verification，并在本 integration boundary 归档；
- Current Ready Execution Unit：**NONE**；
- EU-48 Execute Authority：**TERMINATED on completion**；
- 下一 Gate 仅为：**Phase 3 — Canonical Migration Compatibility & E1～E3 Re-entry planning / compatibility gate**；必须从当前 Repository Authority 与 Current Evidence 独立恢复和判断，不继承 EU-48 或更早 Unit 的 Execute Authority；
- Issue #60 / E1～E3 继续 **blocked / downstream**；Phase 3 re-entry PASS 前不得进入 Execute；
- Issue #77 继续承担 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层边界。

Phase 2C accepted Authority：

- Requirement：`docs/requirements/party-migration-despecialization-compatibility.md`；
- Specification：`docs/specifications/party-migration-despecialization-compatibility.md`；
- Technical Plan：`docs/technical/party-migration-despecialization-compatibility.md`；
- Completed Work Artifact：`docs/work/archive/eu48-party-migration-despecialization-compatibility.md`。

Phase 2B accepted Authority继续作为 Generic Migration长期 contract：

- `docs/requirements/generic-content-migration-application.md`；
- `docs/specifications/generic-content-migration-application.md`；
- `docs/technical/generic-content-migration-application.md`；
- `docs/work/archive/eu47-generic-content-migration-application-foundation.md`。

Phase 2A accepted Authority继续说明长期 application/core contract：

- `docs/requirements/backend-application-core-boundary.md`；
- `docs/specifications/backend-application-core-boundary.md`；
- `docs/technical/backend-application-core-boundary.md`；
- `docs/work/archive/eu46-backend-application-core-boundary-foundation.md`。

已完成 Execution Unit 的详细记录位于 `docs/work/archive/`；完成记录中的旧 Status / Next Step 不构成新的 Execute Authority。

## 主要目录

- `backend/`：Spring Boot CMS Backend；
- `frontend/admin/`：CMS Admin Vue / Vite frontend；
- `frontend/public-site/`：Main / Party Public Vue / Vite frontend；
- `sites/`：Site Package schema 与具体 Site packages；
- `data-migrations/`：Historical Content Migration workspace 与 canonical datasets；
- `docs/`：Current Authority Map、Requirements、Specifications、Technical / Architecture、Project governance 与 Work lifecycle。

各主要 subtree 的职责入口见对应 `README.md`；这些 subtree README 只解释局部 ownership，不建立第二套全局 Authority Map。

## 开发与验证入口

- Repository 工作规则：`AGENTS.md`；
- Consumer-local Development Method：`docs/project/development-method.md`；
- Current Roadmap：`docs/project/project-roadmap.md`；
- Verification Strategy：`docs/technical/verification-strategy.md`；
- Backend build / ownership entry：`backend/build.gradle.kts`、`backend/settings.gradle.kts`、`backend/README.md`；
- Generic CMS Core：`backend/modules/cms-core/`；
- CMS Server：`backend/apps/cms-server/`；
- Content Migration application：`backend/apps/content-migration/`；
- Admin / Public build 与脚本：分别以 `frontend/admin/package.json`、`frontend/public-site/package.json` 为准。

成功、完成、通过或修复声明必须具有与目标提交匹配的 Current Evidence；GitHub Actions、PR Review 与 Post-Integration Evidence 仍按 Consumer-local Method 执行。
