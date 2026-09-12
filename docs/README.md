# Documentation Authority Map

本文件是 `jilinjobs-cms` 的 Documentation Information Architecture 入口，也是 Consumer 普通运行的 **Local Discovery Entry**。它只提供稳定 Authority / resource locator、按需加载边界与本地失败回退，不替代 `AGENTS.md`、产品 Requirement、Roadmap、Work lifecycle、Skill procedure 或 GitHub Current Evidence，也不缓存第二份 Current State truth。

## Local Discovery Entry

普通运行从薄 Bootstrap 到达本文件后，先按当前任务选择最小正确读取路径：

- **state-only**：读取 `docs/work/current/README.md`，并与当前 Open execution PR / branch 及必要 GitHub Current Evidence 协调；只询问 Current Execution State 时，不机械加载完整 Roadmap、Development Method、Skills 或 `agentic-dev` upstream。
- **routing-only**：根据下方 Current Authority 入口定位 **一个 primary responsibility + 最小 supporting locator**；只为判断职责或下一阶段时，不加载完整 Skill procedure。
- **execution**：先定位当前 primary owner，再按 `docs/project/development-method.md` 加载真正执行所需的 primary Skill / Engineering Capability 与必要 supporting authority；不得因为资源存在就全量预加载。

当前 Repository 的稳定入口、资源原生身份与现有 owner 足以支持上述发现路径；普通运行不需要持久化 Reviewed Discovery Map，也不需要 Runtime View。后续只有出现持续的跨资源正规化 / 发现成本证据并经独立治理决定后，才重新评估是否增加派生发现机制。

如果 locator / source 缺失、Current owner 不明确、多个 primary responsibility 无法消歧、override / supersede 关系冲突，或 no-match 但已知 Governance / Verification risk 仍存在，则 **fail closed**：停止依赖当前发现结果，回到本仓库稳定 Authority Entry，按问题扩大最小本地读取并重新定位。普通运行不得自动访问 `dygapp/agentic-dev` 来修补本地发现缺口。

## Fresh Context 恢复顺序

1. 根 `AGENTS.md`：Repository Governance、Authority Boundary、Fresh Context、Human Escalation 与 GitHub 操作授权；
2. 根 `README.md`：稳定项目入口、范围与 Current State locator；
3. 本文件：Local Discovery Entry / Documentation Authority Map；
4. `docs/work/current/README.md` + 当前 Open execution PR / Branch：解析是否存在 Ready / active Execution Unit；
5. 如果当前任务只是 state-only 且已经无歧义得到所需状态，在此停止；否则按当前目标继续读取必要 owner；
6. Planning / durable route 任务按需读取 `docs/project/project-roadmap.md`；方法 / execution routing 任务按需读取 `docs/project/development-method.md`；
7. 当前任务直接相关的 Current Requirement / Specification / Technical / Architecture Authority；
8. 当前 controlling Issue、PR / Actions 与其他 Current Evidence；只有真正执行某项 Skill responsibility 时才加载对应 Skill 语义。

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
| `docs/work/current/README.md` | Current Execution Lifecycle Locator；Ready / active Unit 定位与 `NONE` 语义 | state / execution lifecycle 任务读取 |
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
- 本文件：Local Discovery Entry / Documentation IA / locator；
- Project Roadmap：持久路线、长期边界、durable milestone；
- `docs/work/README.md`：Execution lifecycle contract；
- `docs/work/current/README.md`：Current Execution Lifecycle Locator。

这些稳定 surface 不应并行复制高频变化的 `Current Ready Execution Unit`、Readiness `PENDING/PASS`、exact implementation Head 或 Actions result。Issue body / comment 可以承载 Planning 与 Current Evidence，但旧 comment 只是历史 evidence；它不会因为包含“Current Evidence”字样就永久成为当前 Execute Gate。

GitHub PR / Branch / Actions 对其各自原生瞬时状态负责；这不构成“GitHub 永远高于本地文件”的通用规则。Requirement、Specification、Architecture、Method、Work lifecycle 与 GitHub Current Evidence 仍按各自 Authority 责任协调。若 Current Work locator、active artifact、Readiness Evidence 或 GitHub native state 缺失、冲突或无法消歧，必须 fail closed，不得授予或继承 Execute Authority。

## Current Authority 入口

- Repository governance：根 `AGENTS.md`
- Stable project scope：根 `README.md`
- Requirement：`docs/requirements/README.md`
- Specification：`docs/specifications/README.md`
- Technical：`docs/technical/README.md`
- Verification：`docs/technical/verification-strategy.md`
- Architecture：`docs/architecture/README.md`
- Work lifecycle：`docs/work/README.md`
- Current execution locator：`docs/work/current/README.md`
- Durable Roadmap：`docs/project/project-roadmap.md`
- Consumer-local Method / Skill routing：`docs/project/development-method.md`
- Execution continuity：`docs/project/execution-continuity-guidelines.md`
- Human review feedback cycle：`docs/project/review-feedback-cycle.md`
- Git commit governance：`docs/project/git-commit-guidelines.md`
- Method experiment lifecycle：`docs/project/method-validation-evidence.md`
- Phase 1 classification / convergence authority：`docs/project/documentation-authority-convergence.md`
- Issue #92 completed convergence traceability：`docs/project/pre-e1e3-convergence-plan.md` + closed GitHub Issue #92
- Historical migration current data / provenance workspace：`data-migrations/**`
- GitHub Actions runtime topology：`.github/workflows/**` + GitHub native Actions state

Planning / product Authority 必须按当前任务从 Requirement / Specification / Technical / controlling Issue 重新定位；本 Documentation Map 不缓存某个 Issue 的短期 Current Gate、Ready Unit、最近 implementation SHA 或规则正文摘要。

`data-migrations/**` 是 Historical Content Migration 的 current data/provenance workspace，不属于 documentation archive；“历史内容”不等于 `HISTORICAL_EVIDENCE`。