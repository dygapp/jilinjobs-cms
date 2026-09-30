# Consumer-local 工作约束

## 责任

本文只保存 jilinjobs-cms 自己长期需要的横切执行约束。它不是 Rule Engine、Method selector 或 Provider runtime，也不维护 metadata routing；Agent 根据当前任务语义按需读取适用章节。

Product / Domain / Architecture / Specification / Design / Technical 事实仍由各自 Current Authority 持有。本文不能通过“必须 / 禁止”的写法覆盖这些事实。

## Authority 与状态

- 先从真实 owner 恢复事实，再实施或复核；Code / Tests 可以证明当前实现，但不能单独发明 Requirement。
- 用户只要求只读状态检查时保持只读；观察到的维护项不自动升级为新的修复生命周期。
- docs/work/*.md 的 Execution Unit 文件头是正式工作生命周期状态来源；没有非终态 Unit 仅表示没有正式 active Execution Unit，不表示没有用户授权的直接变更或规划候选。
- Current owner 被替换、退役或归档时，同时检查仍消费其身份、路径或契约的 Current locator、测试、Workflow、Roadmap 与恢复入口；历史材料可以保留旧身份，但不得继续成为 ordinary runtime dependency。

## 实施纪律

- 在满足当前 Authority、验证责任和工程健康的前提下选择最低必要复杂度。
- 新抽象、配置、依赖、扩展点、框架层或未来分支必须能追溯到当前真实责任；不为假想未来需求预埋复杂度。
- 最终 Diff 的每个有意义变化都应能追溯到当前实现、验证、Authority 同步或由本次变化直接产生的必要清理。
- 相邻独立 Bug、无关 TODO、个人风格清理和全局格式化默认不进入同一变更。
- 当前工作已获得足够 Authority 且仍在授权 Scope 时，应连续分析、实施、验证、修复和收敛；单个内部检查点或可自行修复的失败不构成人工停顿边界。

## 数据访问

设计或修改集合型数据访问时，先确认真实 Consumer Scope，并判断集合是稳定有界、持续增长还是无法可靠界定。访问范围、分页 / window、缓存生命周期与验证必须匹配真实边界。

不得默认全量读取可能持续增长的数据，也不得用固定小上限静默截断业务正确集合。

## 外部写操作

任何 Repository、Issue / PR、Workflow、部署或远程服务写操作遵守：

1. 先从当前 Authority 确认目标、范围和授权；
2. 创建具有持续 identity 的对象前，先检查同一目标是否已有可继续使用的对象；
3. 只执行达到当前目标所需的最小变化，优先更可逆的路径；
4. 写操作后重新读取真实目标状态，再进行后续动作；
5. API 返回成功只证明请求被接受或执行，不等于目标 claim 已成立。

涉及多个 Repository 时分别判断授权，不从组织归属、账号身份或连接器能力推导写权限。

## 人工介入

准备请求人工执行操作、提供输入、做出决定或充当系统之间的中转前，先检查当前 connector、API、Git、GitHub、Runtime、Repository Evidence 是否可以在不绕过 Authority 的前提下直接完成。

只有 Product / Domain intent 无法由 Current Authority 唯一确定、重大难逆决定明确保留给人工、缺少不可替代权限 / 凭据 / 受控环境观察，或法规 / 合同 / 安全职责要求人类承担时才升级。人工请求应缩减为最小必要动作或决定。

## Handoff 约束

Fresh Context 是知识边界，不是机械拆分同一授权工作的工具。只有未完成状态确实无法从 Current Authority、Current Work 和 GitHub native state 同等可靠恢复时，才形成最小 Handoff。

Handoff 只保存不可恢复的 locator / unresolved boundary，不复制 Requirement、Specification、Technical、当前 PR / Actions 状态或完整流程。被真实 owner 接管后立即失效。

## Git Commit 约束

完整 Git Commit policy 的 Current owner 为 docs/governance/git-commit-conventions.md。

创建、整理或重写 Commit 前必须读取该 owner；本文不维护第二份 Type / Scope / Message 格式清单，避免与 Current owner 漂移。

## 验证

跨 Feature 验证、证据类型、stale verification contract、跨提交证据复用、浏览器 / 视觉证据、高成本 Runtime 与人工评审环境的长期约束统一由 docs/technical/verification-strategy.md 持有，不在这里建立第二套验证规则。

## 面向人的内容

项目面向人内容默认使用自然、连续的中文。以下对象保持精确身份：

- Skill / 文件 / 路径 / Branch / Commit SHA；
- Issue / PR 编号；
- 代码标识、配置键、字段、API / CLI 与命令；
- 外部产品、协议、标准与正式项目名；
- PASS、FAIL、READY、BLOCKED 等需要跨 surface 稳定识别的状态值。

语言整理不得改变 Product Goal、Scope、业务边界、Architecture Decision、Specification 或技术契约。历史证据以保真为优先，不为了统一表达批量改写。

## Markdown 文件头

当前长期 Authority 正文按职责使用 YAML Front Matter，不把相同字段强加给所有 Markdown：

- `docs/requirements/*.md`（含 `index.md`）、`docs/specifications/*.md`、`docs/architecture/*.md`、`docs/architecture/decisions/ADR-*.md`、`docs/technical/*.md`、`docs/project/*.md` 的非 README 正文统一使用非空且唯一的 `id`、与职责相符的 `type` 和生命周期 `status`。`title`、`version`、`relations`、`updated_at` 仅在有实际用途时保留，不为凑齐字段补值。当前类别的允许值由 `scripts/verify-docs-governance.mjs` 校验。
- `docs/design/public-site/DESIGN.md` 遵循设计工具的 `version`、`name`、`description` 与 token 文件头，由设计专用 lint 校验，不套用 Authority 的 `id/type/status`。
- ADR 使用 `id: ADR-NNNN`、`type: architecture-decision`、`status: accepted` 与决策 `date`；范围、证据及定向 supersede 关系仅在适用时记录。`accepted` 表示决策已接受，不宣称目标架构已经实现。ADR 记录决策历史，当前叠加状态仍由 Architecture 正文持有。
- `docs/work/*.md`（排除 `README.md`）是 Execution Unit working set，统一使用非空且唯一的 `id`、`type: execution-unit` 与 lifecycle `status`；它们不是长期 Authority，允许值和归档边界由 `docs/work/README.md` 持有并由文档治理脚本校验。
- README / 导航索引、Guide、Governance 说明不因目录位置而强制加头；`docs/requirements/index.md` 是带 `requirement-index` 身份的 Authority locator，按第一条校验。
- `docs/**/archive/**` 保留历史原貌，不参与当前文件头补齐或格式校验；重新晋升为 Current 时才按目标职责检查。
