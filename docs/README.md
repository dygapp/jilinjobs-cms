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

Issue #60 当前 dependency closure：

- **E1 Main External-link Ownership & Behavior Boundary**：Planning / Authority closure；Requirement / Specification READY；无 implementation Unit；
- **E2 Main Single-page Formal Content**：已形成 **EU-49 — Page Operational Content Ownership & Migration Foundation**，Readiness = PASS；
- **E3 Main Historical Content Collection & Canonical Migration**：downstream Requirement / Specification READY，依赖 EU-49 completion，当前没有 EU Identifier / Execute Authority。

Current planning authority：

- `docs/project/main-site-formal-content-plan.md`

E1：

- `docs/requirements/main-external-link-boundary.md`
- `docs/specifications/main-external-link-boundary.md`

E2 / EU-49：

- `docs/requirements/main-single-page-formal-content.md`
- `docs/specifications/main-single-page-formal-content.md`
- `docs/technical/main-single-page-formal-content.md`
- `docs/work/current/eu49-page-content-migration-foundation.md`

E3 downstream：

- `docs/requirements/main-historical-content-migration.md`
- `docs/specifications/main-historical-content-migration.md`

Fresh Context 若继续 EU-49，必须重新核验 integrated `main`、Issue #60 / #77、上述 EU-49 Authority、Open PR / Actions、Readiness 与 base drift；Readiness PASS 本身不等于已经建立 Execute baseline。不得提前进入 E3 source collection / canonical promotion。

`data-migrations/**` 是 Historical Content Migration 的 current data/provenance workspace，不属于 documentation archive；“历史内容”不等于 `HISTORICAL_EVIDENCE`。