# Documentation Authority Map

本文件是 `jilinjobs-cms` 的 Documentation Information Architecture 入口。它说明各文档区的责任与 Fresh Context 恢复顺序，但不替代 `AGENTS.md`、产品 Requirement、Roadmap 或 GitHub Current Evidence。

## Fresh Context 恢复顺序

1. 根 `AGENTS.md`：Repository Governance、Authority Boundary、Fresh Context、Human Escalation 与 GitHub 操作授权；
2. 根 `README.md`：稳定项目入口、当前 Planning / Execute Gate；
3. `docs/project/project-roadmap.md`：当前阶段、长期边界与下一 Gate；
4. `docs/project/development-method.md`：Consumer-local Development Method；
5. 当前任务直接相关的 Current Requirement / Specification / Technical / Architecture Authority；
6. 当前控制 Issue、Open PR / Actions、Ready Execution Unit 与 Current Evidence。

`archive/**` 默认不在 Fresh Context Current Authority 恢复集合中。只有当前 Authority、追溯/审计任务或 Verification 明确要求时才读取 archive。

## 文档区域

| 区域 | 责任 | Fresh Context 默认 |
|---|---|---|
| `docs/project/` | Roadmap、Development Method、Planning Authority、长期项目治理与 supporting method notes | 读取当前任务相关项 |
| `docs/requirements/` | Current / Partially Current Product Requirement Authority | 读取当前任务相关项 |
| `docs/specifications/` | Current / Partially Current WHAT / WHY contracts | 读取当前任务相关项 |
| `docs/technical/` | Current / Partially Current Technical Authority、plans、verification strategy | 读取当前任务相关项 |
| `docs/architecture/` | 长期 Architecture Decisions / ADR | 按任务读取 |
| `docs/work/current/` | 当前已通过 Readiness、仍处于 Execute / Verification / Integration 生命周期的 work artifact | 存在 Ready / active unit 时读取 |
| `docs/work/archive/` | 已完成 Execution Units、历史计划、执行与验证 evidence | 默认不读取 |
| 各分类 `archive/` | `SUPERSEDED` / `HISTORICAL_EVIDENCE` 文档 | 默认不读取 |

## Classification 与物理位置

Phase 1 使用四类语义角色：

- `CURRENT`：可直接作为现行 Authority 消费；
- `PARTIALLY_CURRENT`：仍含有效当前语义，必须保留在 Current 区并由后续有 Authority 的 reconciliation 处理；
- `SUPERSEDED`：已被后继 Authority 取代，只保留 traceability；
- `HISTORICAL_EVIDENCE`：研究、执行、评审或验证记录，不定义当前产品/架构事实。

物理 archive 只接收已明确为 `SUPERSEDED` / `HISTORICAL_EVIDENCE` 的文档。不得按年龄、EU 编号或“看起来旧”把 `PARTIALLY_CURRENT` 文件机械归档。

## Current Authority 入口

- Requirement：`docs/requirements/README.md`
- Specification：`docs/specifications/README.md`
- Technical：`docs/technical/README.md`
- Work lifecycle：`docs/work/README.md`
- Current Roadmap：`docs/project/project-roadmap.md`
- Consumer-local Method：`docs/project/development-method.md`
- Phase 1 classification / convergence authority：`docs/project/documentation-authority-convergence.md`
- Issue #92 completed convergence traceability：`docs/project/pre-e1e3-convergence-plan.md` + closed GitHub Issue #92

### Current Issue #60 Main Site Formal Content Authority

Issue #60 当前状态：

- **E1 Main External-link Ownership & Behavior Boundary**：Planning / Authority closure；Requirement / Specification READY；无 implementation Unit；
- **E2 Main Single-page Formal Content**：**EU-49 COMPLETED**；Page operational-content ownership 与 site-neutral Generic Page migration foundation 已集成，Execute Authority已终止；
- **E3 Main Historical Content Collection & Canonical Migration**：Requirement / Specification / Technical Plan READY；EU-50 accepted current Article snapshot 已完成并集成。

Current Ready Execution Unit：**NONE**。

EU-50：**COMPLETED / Execute Authority TERMINATED**。Completed Work Evidence：`docs/work/archive/eu50-main-source-discovery-promotion.md`。

EU-50 integrated accepted current subset：

- 3078 篇 current import-eligible Article；
- 6 篇 source-defect Article 单独排除并等待客户确认；
- 230 篇 problem Article 作为 durable deferred evidence 保留，不属于当前 import input，也不阻断当前进程；
- canonical dataset 位于 `data-migrations/main/v1/**`；
- dataset digest：`sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`。

Downstream Candidate：**EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review**。EU-50 dependency 已满足，但 EU-51 Readiness 仍为 **PENDING / NOT READY**；它只由 Technical Plan / Roadmap 跟踪，在 Readiness PASS 前不进入 `docs/work/current/`，也不具有 Execute Authority。

Main Page / stable ListItem source findings属于 JilinJobs Site Package handoff，保持独立 Planning Gate；它们不因 EU-50 completion 自动获得 Execute Authority。

Current planning authority：

- `docs/project/main-site-formal-content-plan.md`

E1：

- `docs/requirements/main-external-link-boundary.md`
- `docs/specifications/main-external-link-boundary.md`

E2 accepted current contract：

- `docs/requirements/main-single-page-formal-content.md`
- `docs/specifications/main-single-page-formal-content.md`
- `docs/technical/main-single-page-formal-content.md`
- completed Work Evidence：`docs/work/archive/eu49-page-content-migration-foundation.md`

E3 current Authority：

- `docs/requirements/main-historical-content-migration.md`
- `docs/specifications/main-historical-content-migration.md`
- `docs/technical/main-historical-content-migration.md`
- EU-50 completed Work Evidence：`docs/work/archive/eu50-main-source-discovery-promotion.md`
- accepted current canonical dataset：`data-migrations/main/v1/**`

下一自然 Gate 是从 integrated `main` Fresh Context 恢复 Issue #60 / #77、E3 Authority、EU-50 archived evidence 与 canonical dataset，重新判断 downstream dependency / Readiness。EU-50 completion 不自动提供 EU-51 Readiness PASS 或 Execute Authority；deferred problem Articles 保持后置单独处理。

`data-migrations/**` 是 Historical Content Migration 的 current data/provenance workspace，不属于 documentation archive；“历史内容”不等于 `HISTORICAL_EVIDENCE`。
