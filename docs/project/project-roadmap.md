# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap，用于记录本项目自身的演进路线、阶段状态、已完成里程碑、当前目标和下一步工作。

本文只描述 Consumer 项目事实，不继承 `dygapp/agentic-dev/docs/project/project-roadmap.md` 中的项目状态。`agentic-dev` 只提供 AI Agent 开发 Method、Operating Guide 和 Skills 知识来源。

## 使用规则

- 路线图记录项目级状态，不替代 Specification、Technical Plan 或 Execution Unit。
- 路线变化通过 Git 历史保留，不在 README、聊天或临时任务中维护第二份长期状态。
- 状态语义：
  - **已完成**：存在当前仓库可验证证据；
  - **当前**：正在推进的阶段或目标；
  - **下一步**：已经确定的近期工作；
  - **条件性后续**：需要新的真实证据后再评估。

## 方法基线

当前 AI Agent 开发方法来源：

```text
Repository:
dygapp/agentic-dev

Baseline:
master@2ee56a5866d0201977a75b2b18ca2e791a218983
```

本次 baseline 从 `b4e5b2027bdbbe97cc0b7153be65c5afb7a0274e` 升级到当前 `master`，共覆盖 3 个上游集成提交。对本 Consumer 有持续价值的变化包括：

- 正式化已有 Consumer 的采用与 baseline 升级闭环，明确 `agentic-dev` 是上游知识源而非普通开发运行依赖；
- 明确同一任务涉及多个 Repository 时分别确认授权，并将持续权限边界固化为 Consumer-local Project Rule；
- 强化异步外部操作和 `github-actions-verification`：`queued` / `pending` / `in_progress` 属于闭环中间状态，应在可观察且获授权时有界持续观察、诊断、修复、重跑和复验。

这些变化已固化到本仓库 `AGENTS.md`、`docs/project/development-method.md` 与相关连续执行规则。`agentic-dev` 自身 Project Roadmap、Issue #33 处理状态与 eval 状态没有进入 Consumer Authority。后续普通开发继续优先使用 Consumer-local 规则。

## 总体路线

| 路线 | 状态 | 结果 |
|---|---|---|
| Consumer Repository Bootstrap | 已完成 | 建立独立 Consumer Authority、Specification、Technical Plan 和验证边界 |
| 信息发布核心能力纵向建设 | 已完成 | 完成栏目、导航、文章、发布、公开页面、附件、浏览量和响应式能力建设 |
| Feature-wide Convergence 验证 | 已完成 | 完成首次功能范围整体验证并关闭已发现验证缺口 |
| 人工集成评审环境 | 已完成 | 建立可手工触发的临时评审环境、AI 测试数据与外部 HTTP 评审入口 |
| Consumer 继续演进 | 当前 | 以当前 Consumer Authority、Project Roadmap 和本地开发方法恢复并推进后续真实工作 |
| 新业务能力扩展 | 条件性后续 | 根据新的 Product Intent 和 Authority 增量定义 |

## 已完成里程碑

| 日期 | 里程碑 | 证据 |
|---|---|---|
| 2026-08-24 | Feature-wide Convergence 完成并固化状态 | PR #11、PR #12、`f848f86fc410f391f69e674b9ab0b3df6992023e` |
| 2026-08-26 | 建立人工集成评审环境 RC-01 | PR #13、`dddbea592e933bb5a3ca3bb9911f645c2a752ea9` |
| 2026-08-26 | 建立 Consumer Project Roadmap 与本地开发方法 | `docs/project/project-roadmap.md`、`docs/project/development-method.md` |
| 2026-08-26 | 将 `agentic-dev` baseline 升级到 `2ee56a5866d0201977a75b2b18ca2e791a218983` 并吸收 Issue #33 后续通用规则 | `AGENTS.md`、`docs/project/development-method.md`、`docs/agentic-dev-continuous-execution-mode.md` |

此前 EU-01～EU-06、CV-01 的详细证据由当前 Git 历史、PR、CI 和 `README.md` 历史版本保留，不在本 Roadmap 重复维护全部流水。

## 当前阶段

当前核心目标：

> 从“首轮功能建设与收敛”切换为“已有 Consumer 的持续演进”，后续 Fresh Context 应只依赖 Consumer Repository Authority、当前代码与当前证据恢复工作，不重复 Greenfield Bootstrap。

当前约束：

1. 产品事实继续由 `AGENTS.md`、`README.md`、权威需求和当前 Specification 决定；
2. 开发方法优先读取 `docs/project/development-method.md`；
3. 没有新的 Product Intent 时，不自行创造 EU-07 或扩大现有 Scope；
4. 新需求进入后，按 Specification → Slice → Readiness → Execute → Converge 推进；
5. 每项 Acceptance Obligation 必须闭环到实现责任、验证责任、计划证据与 Current Evidence；
6. 跨 Repository 操作按 `AGENTS.md` 的具体权限矩阵逐 Repository 判断；
7. 异步 GitHub Actions / 外部 Job 仍可观察时保持有界执行闭环，不把非终态当作完成或默认人工接管点。

## 下一步工作

当前没有由 Repository Authority 定义的新产品切片。

新的真实需求到达后：

1. 判断是否改变当前 Goal / Scope / User-visible Behavior；
2. 更新或新增 Specification；
3. 仅在跨 Execution Unit 的长期 HOW 需要协调时更新 Technical Plan；
4. 形成纵向、可验证、context-fit 的 Execution Unit；
5. 通过 Readiness、Fresh-context Execute 和 Converge 完成闭环；
6. 项目级路线变化时更新本文。

## Fresh Context 恢复入口

新的项目上下文按以下顺序恢复：

1. `AGENTS.md`；
2. `README.md`；
3. `docs/project/project-roadmap.md`；
4. `docs/project/development-method.md`；
5. 当前工作直接相关的 Requirement / Specification / Technical Plan / Work Artifact；
6. 当前代码、测试、Branch / PR / CI 等 Current Evidence。

不得使用其他聊天、其他项目或个人记忆补充未固化的 Consumer 项目事实。

## 更新触发条件

出现以下情况时更新本文：

- 项目阶段或当前核心目标改变；
- 项目级里程碑完成、取消或被替代；
- 已确定的下一步工作改变；
- 新需求进入正式路线；
- 项目负责人明确要求升级 `agentic-dev` baseline；
- 当前路线与 GitHub 当前事实不再一致。

普通局部实现、单个 Execution Unit 内部状态变化或临时实施步骤不要求更新本文。
