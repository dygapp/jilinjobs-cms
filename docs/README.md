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
- Architecture：`docs/architecture/README.md`
- Work lifecycle：`docs/work/README.md`
- Current Roadmap：`docs/project/project-roadmap.md`
- Consumer-local Method：`docs/project/development-method.md`
- Phase 1 classification / convergence authority：`docs/project/documentation-authority-convergence.md`
- Issue #92 completed convergence traceability：`docs/project/pre-e1e3-convergence-plan.md` + closed GitHub Issue #92

### Current Issue #60 Main Site Formal Content Authority

Issue #60 当前阶段性结果：E1 Planning / Authority closure 已完成；E2 / EU-49 foundation 已完成；E3 / EU-50～EU-51 accepted Article sequence 已完成；EU-52 Main Page Formal Content Package Adoption 与 **EU-53 Main ListItem Bootstrap Completion** 均已完成并终止 Execute Authority。

EU-53 current result：

- Planning / Readiness PR #129 已集成到 `main@26a772928278754f478b38742423fc7b71b1f439`；
- implementation PR #130 final Head：`10449bedf4df38aa2daec99b80d0a9637df2f8db`；
- implementation integrated main：`b1130b110bccdb1565c340a6cce62504ec06a87a`；
- exact-head CI #987、Site Package #109、Backend Boundary #42、EU-51 Runtime #22、EU-51 Browser #18、Review Environment #865：**PASS**；
- Post-Integration CI #988 / run `34484030549`、Site Package #110、Backend Boundary #43：**PASS**；
- accepted Main ListItem result：`HOME_CAROUSEL = 1`，`SITE_RELATED = 5`，`SITE_REGIONAL_GRADUATES = 31`，`SITE_JILIN_UNIVERSITIES = 60`，通过现有 Site Package one-time bootstrap SQL 初始化；
- Party ListItem / `PARTY_CAROUSEL` 未进入 Main bootstrap，继续由 Party migration/current Party Authority 管理；
- EU-53 Completed Work Evidence：`docs/work/archive/eu53-main-listitem-bootstrap-completion.md`；
- 230 deferred + 6 source-defect Articles：独立 later-review / customer-confirmation boundary；
- Current Ready Execution Unit：**NONE**。

Current planning / product Authority：

- `docs/project/main-site-formal-content-plan.md`
- `docs/requirements/main-single-page-formal-content.md`
- `docs/specifications/main-single-page-formal-content.md`
- `docs/technical/main-single-page-formal-content.md`
- `docs/requirements/main-historical-content-migration.md`
- `docs/specifications/main-historical-content-migration.md`
- `docs/technical/main-historical-content-migration.md`
- `docs/requirements/main-stable-listitem-site-package.md`
- `docs/specifications/main-stable-listitem-site-package.md`
- `docs/technical/main-stable-listitem-site-package.md`

下一自然 Gate 是新的 Fresh Context Planning/Readiness decision；不得从 EU-53 completion 自动进入 deferred Article、慧就业 iframe 或其他 successor Unit。

`data-migrations/**` 是 Historical Content Migration 的 current data/provenance workspace，不属于 documentation archive；“历史内容”不等于 `HISTORICAL_EVIDENCE`。