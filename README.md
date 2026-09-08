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

Issue #92 — E1～E3 前置 Repository Authority 与 Migration Architecture 收敛路线的 Phase 0～Phase 3 已完成；Phase 3 **Canonical Migration Compatibility & E1～E3 Re-entry = PASS**。Issue #60 / E1～E3 当前状态：

```text
E1 Main External-link Ownership & Behavior Boundary
   └─ Planning / Authority closure；no implementation Unit
        ↓
E2 Main Single-page Formal Content
   └─ EU-49 Page Operational Content Ownership & Migration Foundation — COMPLETED
        ↓ foundation prerequisite satisfied
E3 Main Historical Content Collection & Canonical Migration
   └─ CURRENT PLANNING GATE
      Fresh Context source-evidence recovery → dependency closure → slice-work → readiness-check
```

当前状态：

- E1 Requirement / Specification：**READY / Planning closure**；当前 Repository audit 无 implementation gap，不创建 Execution Unit；
- E2 / **EU-49：COMPLETED**；PR #112 已集成 Page operational-content ownership 与 site-neutral Generic Page canonical migration foundation；
- EU-49 integrated main：`18da735c654c1a5d1310fe6db7e8e98f6f7b0026`；Post-Integration CI #868 / run `34200526862`：**PASS**；
- EU-49 Execute Authority：**TERMINATED**；completed Work Evidence 位于 `docs/work/archive/eu49-page-content-migration-foundation.md`；
- **Current Ready Execution Unit：NONE**；
- E3 Requirement / Specification：**READY as Planning Authority**；EU-49 prerequisite 已满足，但当前没有 Identifier、Candidate / Ready Execution Unit、Execute baseline 或 Execute Authority；
- 当前没有开始 Main source collection / canonical promotion，也没有冻结未经 Repository Evidence 接受的 Main Article / Page / List 数量；
- Issue #77 继续承担 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层长期边界；
- Repository Split Readiness Assessment 继续 deferred，不自动阻塞 E3 Planning。

下一自然 Gate 是 **Issue #60 / E3 Fresh Context Planning / source-evidence recovery**。恢复后只能按照 current Repository Authority 与真实 source evidence推进 dependency closure；只有 `slice-work`形成 Candidate 且 `readiness-check = PASS` 后，才建立新的 Ready Execution Unit并允许后续 Execute。不得继承 EU-49、EU-48、Phase 3 或 E1 的 Execute Authority。

Current Main formal-content Authority：

- Overall plan：`docs/project/main-site-formal-content-plan.md`；
- E1 Requirement：`docs/requirements/main-external-link-boundary.md`；
- E1 Specification：`docs/specifications/main-external-link-boundary.md`；
- E2 Requirement：`docs/requirements/main-single-page-formal-content.md`；
- E2 Specification：`docs/specifications/main-single-page-formal-content.md`；
- E2 Technical Plan：`docs/technical/main-single-page-formal-content.md`；
- EU-49 Completed Work Evidence：`docs/work/archive/eu49-page-content-migration-foundation.md`；
- E3 Requirement：`docs/requirements/main-historical-content-migration.md`；
- E3 Specification：`docs/specifications/main-historical-content-migration.md`。

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
