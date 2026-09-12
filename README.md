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

Main historical migration execution 当前保持 **FROZEN / explicit reactivation only**：既有 `data-migrations/main/**` canonical evidence 保留，普通开发流程不得自行重新激活 Main migration；Party migration 不在该冻结范围内。

## Repository Authority 与 Fresh Context

Fresh Context 按稳定入口恢复，避免 Bootstrap 文件复制高频 Execute Gate：

1. `AGENTS.md`：Repository Governance、Authority Boundary、Fresh Context 与操作规则；
2. 本 `README.md`：稳定项目入口与范围；
3. `docs/README.md`：Documentation Authority Map；
4. `docs/work/current/README.md` + 当前 Open PR / Branch：解析 Current Execution Lifecycle；
5. `docs/project/project-roadmap.md` 与 `docs/project/development-method.md`：持久路线与 Consumer-local Method；
6. 当前任务直接相关的 Requirement / Specification / Technical / Architecture Authority，以及 controlling Issue / PR / Actions Current Evidence。

`docs/**/archive/**` 与 `docs/work/archive/**` 默认只承担 traceability / historical evidence，不参与 Fresh Context Current Authority 恢复，除非当前 Authority 明确要求读取。

根 README **不维护** `Current Ready Execution Unit`、Readiness `PENDING/PASS`、exact execution Head 或最近 Actions 等高频状态。Current Execution Lifecycle 的 Repository locator 统一为 `docs/work/current/README.md`，其 Entry / Exit / fail-closed 契约由 `docs/work/README.md` 定义。Project Roadmap 只维护 durable milestones、长期边界和 Planning directions，不作为 Execute Gate 缓存。

GitHub PR / Branch / Actions 只对其各自原生瞬时事实负责；这不建立“GitHub 永远高于本地文件”的通用规则。Fresh Context 必须协调 Work locator、当前 Open execution work、Readiness Evidence 与任务 Authority；缺失、冲突或歧义时 fail closed，不得授予或继承 Execute Authority。

当前执行作用域护栏见 `docs/project/execution-scope-guardrails.md`。当用户目标仅为 Repository 状态恢复 / 检查 / 总结，且上述 Current State 协调结果为 `Current Ready / active Execution Unit = NONE` 时，默认在完成只读状态报告后停止；`NONE` 不表示没有 Planning Candidate，也不得因此自动创建新的 Branch / PR / Workflow 生命周期。高成本验证与 Historical Migration 只在当前变更和 Evidence Claim 确实需要时升级执行。

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
- Documentation Authority Map：`docs/README.md`；
- Current execution locator：`docs/work/current/README.md`；
- Work lifecycle：`docs/work/README.md`；
- Consumer-local Development Method：`docs/project/development-method.md`；
- Execution Scope Guardrails：`docs/project/execution-scope-guardrails.md`；
- Project Roadmap：`docs/project/project-roadmap.md`；
- Verification Strategy：`docs/technical/verification-strategy.md`；
- Backend build / ownership entry：`backend/build.gradle.kts`、`backend/settings.gradle.kts`、`backend/README.md`；
- Generic CMS Core：`backend/modules/cms-core/`；
- CMS Server：`backend/apps/cms-server/`；
- Content Migration application：`backend/apps/content-migration/`；
- Admin / Public build 与脚本：分别以 `frontend/admin/package.json`、`frontend/public-site/package.json` 为准。

成功、完成、通过或修复声明必须具有与目标提交匹配的 Current Evidence；GitHub Actions、PR Review 与 Post-Integration Evidence 仍按 Consumer-local Method 执行。
