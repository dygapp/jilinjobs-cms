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

stable Main ListItem Site Package follow-up
   └─ independent Planning Candidate / no Identifier

Deferred Article review
   └─ 230 problem + 6 source-defect / independent later review
```

当前状态：

- EU-49、EU-50、EU-51、EU-52：**COMPLETED / Execute Authority TERMINATED**；
- EU-52 accepted result：10 个 Main formal Page + 156 个新增 Page-owned assets进入 JilinJobs Site Package，总 package assets = 187；`budget` 13 PDF均 package-owned；Existing Site只在 exact prior-package mutable-content fingerprint match 时一次性 adoption，operator-diverged content保留并报告；
- PR #125 exact-head CI #972 / Site Package #103 / Backend Boundary #40 / Generic #63 / Canonical #273 / Party #18 / EU-30 #223 / EU-51 Runtime #17 / Imported Browser #13均 PASS；bounded Human Review PASS；
- 人工验收发现的 `guide/faq` 顶层问题层级已在同一 Unit 内修正并由 final Browser verification证明；
- Post-Integration on `main@ccbd9fd8c6048f5b4a96d965b8578f7b7a1d2838`：CI #973、Site Package #104、Backend Boundary #41、Generic #64、Party #19均 **PASS**；
- stable Main ListItem继续作为独立 Planning Candidate，不具有 Identifier / Readiness / Execute Authority；
- 230 篇 deferred problem Articles与 6 篇 source-defect Articles继续作为独立 later-review / customer-confirmation evidence；EU-52未采用“每栏目仅迁移最新20条”的 Article 策略；
- **Current Ready Execution Unit：NONE**。

Current Main formal-content Authority：

- Overall plan：`docs/project/main-site-formal-content-plan.md`；
- Main Page current Requirement：`docs/requirements/main-single-page-formal-content.md`；
- Main Page current Specification：`docs/specifications/main-single-page-formal-content.md`；
- Main Page current Technical Authority：`docs/technical/main-single-page-formal-content.md`；
- EU-52 Completed Work Evidence：`docs/work/archive/eu52-main-page-formal-content-package-adoption.md`；
- EU-49 / EU-50 / EU-51 Completed Work Evidence：`docs/work/archive/`；
- E3 current Article contract：`docs/requirements/main-historical-content-migration.md`、`docs/specifications/main-historical-content-migration.md`、`docs/technical/main-historical-content-migration.md`。

下一自然 Gate 是新的 **Fresh Context Planning/Readiness decision**。任何后续 Candidate 都必须从 current Repository Authority 重新完成必要的 Requirement / Specification / Technical Planning、`slice-work` 与 `readiness-check`；不得自动进入 stable ListItem、deferred Article 或其他候选，也不得继承 EU-52 Execute Authority。

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