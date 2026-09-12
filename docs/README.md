# Documentation Authority Map

本文件是 `jilinjobs-cms` 的 Documentation Information Architecture 入口。它说明各文档区的责任与 Fresh Context 恢复顺序，但不替代 `AGENTS.md`、产品 Requirement、Roadmap、Work lifecycle 或 GitHub Current Evidence。

## Fresh Context 恢复顺序

1. 根 `AGENTS.md`：Repository Governance、Authority Boundary、Fresh Context、Human Escalation 与 GitHub 操作授权；
2. 根 `README.md`：稳定项目入口、范围与 Current State locator；
3. `docs/work/current/README.md` + 当前 Open PR / Branch：解析是否存在 Ready / active Execution Unit；
4. `docs/project/project-roadmap.md`：恢复持久路线、长期边界与 Planning directions；
5. `docs/project/development-method.md`：Consumer-local Development Method；
6. 当前任务直接相关的 Current Requirement / Specification / Technical / Architecture Authority；
7. 当前 controlling Issue、PR / Actions 与其他 Current Evidence。

`docs/work/current/README.md` 只拥有 **Current Execution Lifecycle** 的 Repository locator 语义，不拥有 Planning Candidate 排序或完整 Roadmap。其 `NONE` 只有在完成 Open execution PR / branch 与必要 Current Evidence 协调后，才能用于 state-only 安全停止；不得从 `NONE` 推导“没有 Planning Candidate”。详细 Entry / Exit / fail-closed 契约见 `docs/work/README.md`。

`archive/**` 默认不在 Fresh Context Current Authority 恢复集合中。只有当前 Authority、追溯/审计任务或 Verification 明确要求时才读取 archive。

## 文档区域

| 区域 | 责任 | Fresh Context 默认 |
|---|---|---|
| `docs/project/` | Roadmap、Development Method、Planning Authority、长期项目治理与 supporting method notes | 读取当前任务相关项 |
| `docs/requirements/` | Current / Partially Current Product Requirement Authority | 读取当前任务相关项 |
| `docs/specifications/` | Current / Partially Current WHAT / WHY contracts | 读取当前任务相关项 |
| `docs/technical/` | Current / Partially Current Technical Authority、plans、verification strategy | 读取当前任务相关项 |
| `docs/architecture/` | 长期 Architecture Decisions / ADR | 按任务读取 |
| `docs/work/current/README.md` | Current Execution Lifecycle Locator；Ready / active Unit 定位与 `NONE` 语义 | Fresh Context 必读 |
| `docs/work/current/*.md` | 已通过 Readiness、仍处于 Execute / Verification / Integration / Post-Integration closure 的 active work artifact | locator 指向或 Open execution work 存在时读取 |
| `docs/work/archive/` | 已完成 Execution Units、历史计划、执行与验证 evidence | 默认不读取 |
| 各分类 `archive/` | `SUPERSEDED` / `HISTORICAL_EVIDENCE` 文档 | 默认不读取 |

## Classification 与物理位置

Phase 1 使用四类语义角色：

- `CURRENT`：可直接作为现行 Authority 消费；
- `PARTIALLY_CURRENT`：仍含有效当前语义，必须保留在 Current 区并由后续有 Authority 的 reconciliation 处理；
- `SUPERSEDED`：已被后继 Authority 取代，只保留 traceability；
- `HISTORICAL_EVIDENCE`：研究、执行、评审或验证记录，不定义当前产品/架构事实。

物理 archive 只接收已明确为 `SUPERSEDED` / `HISTORICAL_EVIDENCE` 的文档。不得按年龄、EU 编号或“看起来旧”把 `PARTIALLY_CURRENT` 文件机械归档。

## Stable Authority 与 high-frequency Current Evidence

Bootstrap / long-lived surface 只维护稳定职责与 locator：

- `AGENTS.md`：Repository Governance / Authority / operation boundary；
- 根 `README.md`：稳定项目入口与 scope；
- 本文件：Documentation IA / locator；
- Project Roadmap：持久路线、长期边界、durable milestone；
- `docs/work/README.md`：Execution lifecycle contract；
- `docs/work/current/README.md`：Current Execution Lifecycle Locator。

这些稳定 surface 不应并行复制高频变化的 `Current Ready Execution Unit`、Readiness `PENDING/PASS`、exact implementation Head 或 Actions result。Issue body / comment 可以承载 Planning 与 Current Evidence，但旧 comment 只是历史 evidence；它不会因为包含“Current Evidence”字样就永久成为当前 Execute Gate。

GitHub PR / Branch / Actions 对其各自原生瞬时状态负责；这不构成“GitHub 永远高于本地文件”的通用规则。Requirement、Specification、Architecture、Method、Work lifecycle 与 GitHub Current Evidence 仍按各自 Authority 责任协调。若 Current Work locator、active artifact、Readiness Evidence 或 GitHub native state 缺失、冲突或无法消歧，必须 fail closed，不得授予或继承 Execute Authority。

## Current Authority 入口

- Requirement：`docs/requirements/README.md`
- Specification：`docs/specifications/README.md`
- Technical：`docs/technical/README.md`
- Architecture：`docs/architecture/README.md`
- Work lifecycle：`docs/work/README.md`
- Current execution locator：`docs/work/current/README.md`
- Current Roadmap：`docs/project/project-roadmap.md`
- Consumer-local Method：`docs/project/development-method.md`
- Phase 1 classification / convergence authority：`docs/project/documentation-authority-convergence.md`
- Issue #92 completed convergence traceability：`docs/project/pre-e1e3-convergence-plan.md` + closed GitHub Issue #92

Planning / product Authority 必须按当前任务从 Requirement / Specification / Technical / controlling Issue 重新定位；本 Documentation Map 不再缓存某个 Issue 的短期 Current Gate、Ready Unit 或最近 implementation SHA。

`data-migrations/**` 是 Historical Content Migration 的 current data/provenance workspace，不属于 documentation archive；“历史内容”不等于 `HISTORICAL_EVIDENCE`。
