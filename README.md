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
2. 本 `README.md`：稳定项目入口与当前 Planning / Execute Gate；
3. `docs/README.md`：Documentation Authority Map；
4. `docs/project/project-roadmap.md` 与 `docs/project/development-method.md`：当前路线与 Consumer-local Method；
5. 当前任务直接相关的 Requirement / Specification / Technical Authority、GitHub Issue / PR / Actions 与 Ready Execution Unit。

`docs/**/archive/**` 与 `docs/work/archive/**` 默认只承担 traceability / historical evidence，不参与 Fresh Context Current Authority 恢复，除非当前 Authority 明确要求读取。

## 当前 Gate

Issue #92 Phase 0～Phase 3、E1 Planning closure、E2 / EU-49、E3 / EU-50～EU-51，以及 EU-52 Main Page Formal Content Package Adoption 均已完成相应当前闭环。EU-52 implementation PR #125 已从 final exact Head `ff4acdc08e9b902d6aa273ff33fae33fb5bfc520` squash 集成到 `main@ccbd9fd8c6048f5b4a96d965b8578f7b7a1d2838`，Post-Integration CI #973 / run `34442174532` PASS。

```text
EU-52 Main Page Formal Content Package Adoption
   └─ COMPLETED / Execute Authority TERMINATED

Main ListItem Site Package bootstrap completion
   └─ current Planning Candidate / Requirement + Specification + Technical Plan READY

Deferred Article review
   └─ 230 problem + 6 source-defect / independent later review
```

当前状态：

- EU-49、EU-50、EU-51、EU-52：**COMPLETED / Execute Authority TERMINATED**；
- EU-52 accepted result：10 个 Main formal Page + 156 个新增 Page-owned assets进入 JilinJobs Site Package，总 package assets = 187；`budget` 13 PDF均 package-owned；Existing Site只在 exact prior-package mutable-content fingerprint match 时一次性 adoption，operator-diverged content保留并报告；
- Main ListItem current plan：Main `HOME_CAROUSEL` 与已审核 `SITE_RELATED=5`、`SITE_REGIONAL_GRADUATES=31`、`SITE_JILIN_UNIVERSITIES=60` 使用现有 Site Package one-time bootstrap SQL；bootstrap 后继续作为普通 operator-managed data；不新增 stable identity / reconcile；
- Party ListItem / `PARTY_CAROUSEL` 继续由 Party migration/current Party Authority 管理，不进入 Main ListItem follow-up；
- 230 篇 deferred problem Articles与 6 篇 source-defect Articles继续作为独立 later-review / customer-confirmation evidence；
- **Current Ready Execution Unit：NONE**。

Current Main formal-content Authority：

- Overall plan：`docs/project/main-site-formal-content-plan.md`；
- Main ListItem Requirement：`docs/requirements/main-stable-listitem-site-package.md`；
- Main ListItem Specification：`docs/specifications/main-stable-listitem-site-package.md`；
- Main ListItem Technical Plan：`docs/technical/main-stable-listitem-site-package.md`；
- Main Page current Requirement：`docs/requirements/main-single-page-formal-content.md`；
- Main Page current Specification：`docs/specifications/main-single-page-formal-content.md`；
- Main Page current Technical Authority：`docs/technical/main-single-page-formal-content.md`；
- EU-52 Completed Work Evidence：`docs/work/archive/eu52-main-page-formal-content-package-adoption.md`；
- EU-49 / EU-50 / EU-51 Completed Work Evidence：`docs/work/archive/`；
- E3 current Article contract：`docs/requirements/main-historical-content-migration.md`、`docs/specifications/main-historical-content-migration.md`、`docs/technical/main-historical-content-migration.md`。

下一自然 Gate 是 Main ListItem 的 **`slice-work → readiness-check`**。只有形成 Candidate Execution Unit 并通过 Readiness 后才能建立新的 Execute Authority；不得继承 EU-52 Execute Authority，也不得顺带进入 Party ListItem、deferred Article 或慧就业 iframe 工作。

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
