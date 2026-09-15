# 文档权威与本地发现入口

本文件是 `jilinjobs-cms` 的 Documentation Information Architecture 入口，也是 Consumer 普通运行的 **Local Discovery Entry**。它只提供稳定 Authority / resource locator、按需加载边界与本地失败回退，不替代 `AGENTS.md`、产品 Requirement、Project Roadmap、Work lifecycle、Method / Skill procedure 或 GitHub Current Evidence，也不缓存第二份 Current State truth。

## 本地发现入口

普通运行从薄 Bootstrap 到达本文件后，先按当前任务选择最小正确读取路径：

- **state-only**：读取 `docs/work/current/README.md`，并与当前 Open execution PR / branch 及必要 GitHub Current Evidence 协调；只询问 Current Execution State 时，不机械加载完整 Roadmap、Method、Skills 或 `agentic-dev` upstream。
- **routing-only**：读取 `docs/project/project-capability-profile.md`，根据当前 work kind 定位一个 canonical Method 或 direct responsibility，再加载最小 supporting locator；只为判断职责或下一阶段时，不加载完整 Skill procedure。
- **execution**：先定位当前 primary owner，再读取被选中的 `docs/methods/*.md`、当前责任真正需要的 Architecture / Skill，以及 Consumer-local Rule Discovery 返回的候选 Rule；不得因为资源存在就全量预加载。

当前 Repository 的稳定入口、资源原生身份与现有 owner 足以支持上述发现路径；普通运行不需要 Reviewed Discovery Map 或 Runtime View。若 locator / source 缺失、Current owner 不明确、多个 primary responsibility 无法消歧、override / supersede 关系冲突，或 no-match 但已知 Governance / Verification risk 仍存在，则 **fail closed**：回到本仓库稳定 Authority Entry，按问题扩大最小本地读取并重新定位。普通运行不得自动访问 `dygapp/agentic-dev` 修补本地发现缺口。

## Fresh Context 恢复顺序

1. 根 `AGENTS.md`：Repository Governance、Authority Boundary、Fresh Context、Human Escalation 与 GitHub 操作授权；
2. 根 `README.md`：稳定项目入口与范围；
3. 本文件：Local Discovery Entry / Documentation IA；
4. `docs/work/current/README.md` + 当前 Open execution PR / Branch：解析是否存在 Ready / active Execution Unit；
5. 如果当前任务只是 state-only 且已经无歧义得到所需状态，在此停止；否则按当前目标继续；
6. Planning / durable route 任务按需读取 `docs/project/project-roadmap.md`；Method / execution routing 读取 `docs/project/project-capability-profile.md`，再按 selector 加载单个 canonical Method；
7. 当前任务直接相关的 Current Requirement / Domain / Architecture / Specification / Technical Authority；
8. 当前 controlling Issue、PR / Actions 与其他 Current Evidence；只有真正执行某项 Skill responsibility 时才加载对应 `SKILL.md`。

`docs/work/current/README.md` 只拥有 **Current Execution Lifecycle** 的 Repository locator 语义，不拥有 Planning Candidate 排序或完整 Roadmap。其 `NONE` 只有在完成 Open execution PR / branch 与必要 Current Evidence 协调后，才能用于 state-only 安全停止；不得从 `NONE` 推导“没有 Planning Candidate”。详细 Entry / Exit / fail-closed 契约见 `docs/work/README.md`。

`archive/**` 默认不在 Fresh Context Current Authority 恢复集合中。只有当前 Authority、追溯 / 审计任务或 Verification 明确要求时才读取 archive。

## 文档语言规范

Current / Partially Current 文档必须遵循 `rule:human-facing-content-integrity`：中文主述、精确机器标识、正式概念身份不被表达重写改变。Historical Evidence 以证据保真优先，重新晋升为 Current 前先完成 semantic reconciliation。

自动约束由 `scripts/verify-docs-governance.mjs` 与 `.github/workflows/docs-governance.yml` 执行。不得通过扩大历史例外列表隐藏仍属于 Current Authority 的问题。

## 文档区域

| 区域 | 责任 | Fresh Context 默认 |
|---|---|---|
| `docs/project/` | Consumer-local Project Knowledge：Capability Profile、Roadmap / Evolution 等稳定项目级 owner | 按任务读取 |
| `docs/methods/` | Consumer-local canonical Method | selector 命中后读取一个 |
| `docs/architecture/` | 长期 capability / product Architecture 与 ADR | 按责任读取 |
| `docs/rules/` | Discoverable Consumer-local Rule | 只通过 Rule Discovery 读取命中正文 |
| `skills/` | Consumer-local Skill corpus | 责任明确且需要独立 Procedure 时读取 |
| `docs/requirements/` | Current / Partially Current Product / Domain Requirement Authority | 读取当前任务相关项 |
| `docs/specifications/` | Current / Partially Current Feature / change WHAT / WHY contracts | 读取当前任务相关项 |
| `docs/technical/` | Current / Partially Current Feature Technical Authority、verification strategy 与当前待治理的历史技术文档 | 读取当前任务相关项 |
| `docs/work/current/README.md` | Current Execution Lifecycle Locator | state / execution lifecycle 任务读取 |
| `docs/work/current/*.md` | 已通过 Readiness、仍处于 Execute / Verification / Integration / Post-Integration closure 的 active work artifact | locator 指向时读取 |
| `docs/work/archive/` | 已完成 Execution Unit / execution evidence | 默认不读取 |
| 各分类 `archive/` | `SUPERSEDED` / `HISTORICAL_EVIDENCE` | 默认不读取 |

## 文档分类与物理位置

当前四类语义角色：

- `CURRENT`：可直接作为现行 Authority 消费；
- `PARTIALLY_CURRENT`：仍含有效当前语义，必须由后续有 Authority 的 reconciliation 处理；
- `SUPERSEDED`：已被后继 Authority 取代，只保留 traceability；
- `HISTORICAL_EVIDENCE`：研究、执行、评审或验证记录，不定义当前产品 / 架构事实。

物理 archive 只接收已经明确判定为 `SUPERSEDED` / `HISTORICAL_EVIDENCE` 的文档；不得按年龄、EU 编号或“看起来旧”机械归档 `PARTIALLY_CURRENT` 资产。

## 稳定 Authority 与高频 Current Evidence

Bootstrap / long-lived surface 只维护稳定职责与 locator：

- `AGENTS.md`：Repository Governance / Authority / operation boundary；
- 根 `README.md`：Project Charter equivalent / 稳定 scope；
- 本文件：Local Discovery Entry / Documentation IA；
- `docs/project/project-capability-profile.md`：Repository-local capability instance 与 Method selector；
- `docs/project/project-roadmap.md`：持久路线与 Planning directions；
- `docs/work/README.md`：Execution lifecycle contract；
- `docs/work/current/README.md`：Current Execution Lifecycle Locator。

这些稳定 surface 不并行复制 `Current Ready Execution Unit`、Readiness `PENDING/PASS`、exact implementation Head 或 Actions result。Issue body / comment 可以承载 Planning 与 Current Evidence，但旧 comment 只是历史 evidence。

GitHub PR / Branch / Actions 对其各自原生瞬时状态负责；这不构成“GitHub 永远高于本地文件”的通用规则。Requirement、Architecture、Method、Work lifecycle 与 GitHub Current Evidence 按各自 Authority 责任协调；冲突或无法消歧时 fail closed。

## Current Authority 入口

- Repository governance：根 `AGENTS.md`
- Project Charter equivalent / stable project scope：根 `README.md`
- Local capability instance / Method selector：`docs/project/project-capability-profile.md`
- Canonical Methods：`docs/methods/`
- Capability Architecture：`docs/architecture/engineering-capability.md`、`consumer.md`、`method.md`、`project-knowledge.md`、`rule.md`、`rule-discovery.md`、`skill.md`
- Requirement：`docs/requirements/README.md`
- Specification：`docs/specifications/README.md`
- Technical：`docs/technical/README.md`
- Verification：`docs/technical/verification-strategy.md`
- Architecture Decisions：`docs/architecture/decisions/`
- Work lifecycle：`docs/work/README.md`
- Current execution locator：`docs/work/current/README.md`
- Durable Roadmap：`docs/project/project-roadmap.md`
- Execution continuity：`docs/rules/repository/execution-continuity.md`
- Read-only state inspection：`docs/rules/repository/read-only-state-inspection.md`
- High-cost runtime activation：`docs/rules/verification/high-cost-runtime-activation.md`
- Git commit governance：`docs/rules/repository/git-commit-governance.md`
- Human-facing content / document authoring integrity：`docs/rules/repository/human-facing-content-integrity.md`
- Human review feedback lifecycle：`docs/methods/review-feedback-cycle.md`
- Method experiment lifecycle：`docs/methods/method-experiment.md`
- Rule Discovery runtime：`docs/architecture/rule-discovery.md` + `tools/rule-discovery/rule_discovery.py`
- Skill root：`skills/`
- Historical migration current data / provenance workspace：`data-migrations/**`
- GitHub Actions runtime topology：`.github/workflows/**` + GitHub native Actions state

当前产品 Architecture 仍处于 Foundation Rebuild 的后续治理范围。在新的长期 owner 经 Requirement / Architecture Clarification 建立前，现有 CMS / Site Package / Page Content Authority 继续按当前 Repository Authority 与对应 Requirement / Specification / Technical 组合解释；不得从本 Documentation Map 提前发明新产品语义。

## Issue #153 过渡 Locator

以下旧路径已经退出 canonical ownership，只为本轮治理中的尚未迁移引用提供临时定位：

- `docs/project/development-method.md`
- `docs/project/rule-discovery-method.md`
- `docs/project/review-feedback-cycle.md`
- `docs/project/method-validation-evidence.md`
- `docs/project/execution-continuity-guidelines.md`
- `docs/project/execution-scope-guardrails.md`
- `docs/project/git-commit-guidelines.md`
- `docs/project/document-authoring-guidelines.md`

它们必须在 Foundation ownership / locator 迁移完成后删除，不能进入最终长期结构。

## Project historical evidence

下列文件已完成或被后继 Authority 取代，物理归档到 `docs/project/archive/`，默认不参与 Fresh Context：

- `documentation-authority-convergence.md`
- `agentic-dev-continuous-execution-mode.md`
- `main-site-formal-content-plan.md`
- `site-package-planning.md`
- `pre-e1e3-convergence-plan.md`
- `agentic-dev-v3-08-track-b-evidence.md`
- `agentic-dev-v3-closure-baseline-upgrade-evidence.md`
- `agentic-dev-v4-08-consumer-validation-evidence.md`
- `agentic-dev-rule-granularity-baseline-upgrade-evidence.md`
- `agentic-dev-clarification-rule-activation-baseline-upgrade-evidence.md`

这些文件只保留 provenance / audit value，不拥有 current Method、Architecture、Roadmap、Product 或 Execute semantics。

## 已完成 Product / Engineering contract 的当前治理分类

以下文件目前仍留在 Requirement / Specification / Technical 的非 archive 区，但已经被现有 Authority Map 判定为 `HISTORICAL_EVIDENCE / COMPLETED` 或 `SUPERSEDED`。Issue #153 后续必须在重建长期 Requirement / Domain / Architecture owner 时逐项验证语义已安全接管后再物理归档；本轮不因文件名或历史状态机械移动。

### Engineering / migration convergence

- `backend-application-core-boundary` 三件套；
- `generic-content-migration-application` 三件套；
- `party-migration-despecialization-compatibility` 三件套。

### Main 内容 / Site Package

- `main-single-page-formal-content` 三件套；
- `main-stable-listitem-site-package` 三件套。

### Public replaceability

- `public-frontend-replaceability` 三件套。

### Flyway baseline convergence

- `database-migration-baseline-convergence` 三件套。

其中任何历史 `READY`、`Technical Planning REQUIRED`、`Current Ready Execution Unit` 或旧 migration 编号都只表达当时阶段，不重新授予 Execute Authority，也不得覆盖当前实现与 Current owner。

`data-migrations/**` 是 Historical Content Migration 的 current data / provenance workspace，不属于 documentation archive；“历史内容”不等于 `HISTORICAL_EVIDENCE`。