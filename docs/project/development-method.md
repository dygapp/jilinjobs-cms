# Consumer 开发方法与 Skill 使用规则

本文固化 `jilinjobs-cms` 在持续开发阶段实际采用的 AI Agent 开发方法、Skill 使用规则和上下文恢复规则。

本文是 Consumer-local 规则。后续开发默认优先读取并遵循本仓库 `AGENTS.md`、`README.md`、本文及其他当前 Authority；不需要在每次普通开发工作中重新读取 `dygapp/agentic-dev`。只有项目负责人明确要求升级 `agentic-dev` baseline，或当前 Consumer 文档明确无法回答方法问题时，才重新读取指定 baseline 并将需要长期保留的变化回写到本仓库。

## 1. 方法来源与当前基线

```text
Repository:
dygapp/agentic-dev

Baseline:
master@2ee56a5866d0201977a75b2b18ca2e791a218983
```

该 baseline 提供 Method、Operating Guide、Skill Contracts 与 Skills 的来源依据，但不提供本项目的业务事实。

相对上一 baseline `b4e5b2027bdbbe97cc0b7153be65c5afb7a0274e`，本次实际吸收的新增规则是：

- 已有 Consumer 将 `agentic-dev` 视为上游知识源而非日常运行依赖，并采用“精确 baseline → 选择性采纳 → Consumer-local 固化 → 恢复本地 Authority”的升级闭环；
- 同一工作涉及多个 Repository 时，逐 Repository 判断具体操作授权，技术工具能力不能替代 Human Authority；
- Workflow / Deployment / 远程 Job 等异步外部操作属于执行闭环中间状态，等待、观察、失败诊断、授权内修复和重试必须保持有界并持续到取得目标证据或出现真实阻塞；
- `github-actions-verification` 对 `queued` / `pending` / `in_progress` Run 明确不再视为默认退出条件，并要求重新核对 event、Head SHA、status / conclusion、Jobs / Logs / Artifacts 与当前目标提交的关联。

`agentic-dev` 自身 Project Roadmap、Issue #33 处理状态、eval 维护状态等没有被继承为 Consumer 项目事实。

## 2. Consumer 与 agentic-dev 的职责边界

- `agentic-dev` 决定可复用的“如何工作”。
- `jilinjobs-cms` 决定本项目的 Goal、Scope、Requirements、Specification、Architecture、Code、Tests、Verification 与 Integration Policy。
- `agentic-dev/docs/project/*` 中属于其自身项目的状态、路线和实验事实不得复制为 Consumer 项目事实。
- 从 `agentic-dev` baseline 吸收的方法变化，应转化为本仓库可直接执行的本地规则，而不是要求后续 Agent 持续跨仓库读取方法文档。
- 新的 `agentic-dev` 提交不会仅因存在就自动覆盖已固化的 Consumer-local 规则；只有显式 baseline 升级才重新比较并处理更新、保留或取代关系。

## 3. 常规 Feature 工作流

当前项目采用以下主流程：

```text
Consumer Authority / Domain Context
        ↓
Clarify Intent（仅在存在实质产品歧义时）
        ↓
Specification
        ↓
Technical Planning?（按需）
        ↓
Slice Work
        ↓
Readiness Check
        ↓
Fresh-context Execute
        ↓
Converge
        ↓
Ready to Integrate
        ↓
Repository Policy / Human Authority
```

规则：

- 阶段是工作状态，不要求每个阶段都创建文档。
- Specification 聚焦 WHAT / WHY。
- Technical Plan 只在跨 Execution Unit 的长期 HOW 协调具有持续价值时持久化。
- Execution Unit 应纵向、范围明确、可独立验证、可追溯并适合 Fresh Context。
- 实施文件、具体命令和局部施工步骤优先通过 JIT Plan 在执行时确定。
- 通用 Method 的终点是 `Ready to Integrate`；实际 merge / release / deploy 仍服从本仓库授权和策略。

## 4. 验收与验证闭环

每项 Specification Acceptance Obligation 必须能够闭环到：

1. 明确的实现责任；
2. 明确的验证责任；
3. 计划取得的 Verification Evidence；
4. 已实际执行并与当前实现匹配的 Current Evidence。

实现覆盖不等于验证覆盖。没有与声明匹配的 Current Evidence，不得声明完成、通过或修复成功。

当 Feature-wide `converge` 暴露的缺口只是已有 Acceptance Obligation 缺少验证覆盖时，应优先补齐 Verification Coverage，不得因此发明新的产品范围。

## 5. 外部操作与异步执行闭环

外部 Repository / GitHub / Workflow 操作遵循：

```text
Analyze
→ Act
→ Observe
→ Collect Current Evidence
→ Diagnose
→ Fix / Retry when authorized
→ Verify
→ Report
```

规则：

- 写操作前读取当前 Source of Truth，写后重新读取验证；Mutation Response 不等于目标状态已成立。
- 同一任务涉及多个 Repository 时，对读取、Issue / Evidence、文件 / Branch / Commit / PR、Workflow、Merge / Release / Deploy 等权限分别判断，不把一个 Repository 的授权推导到另一个 Repository。
- 持续有效的权限边界应固化为 Consumer Project Rule；本项目的具体权限矩阵以 `AGENTS.md` 为准。
- Workflow、Deployment、远程 Job 等异步操作进入 `queued`、`pending` 或 `in_progress` 时，只表示执行仍处在闭环中间状态；当当前目标要求取得结果且 Runtime 仍可观察时，不因“仍在运行”而默认请求人工继续。
- 异步观察必须有界：按正常运行基线设置合理轮询间隔、观察上限、timeout 与 cancellation 策略，避免无限等待。
- 失败后先取得日志、Job / Step、Artifact 或其他诊断证据；修复属于当前 Scope 且已授权时，使用 `systematic-debug` 定位 Root Cause、执行最小修复、重跑并重新观察。
- 闭环可在三类结果下结束：取得并核对目标证据；出现真实 Human / Runtime 阻塞；达到有界观察上限并准确保留 `Executed but not fully verified`。后两者均不能声明完成。

## 6. Project Roadmap

本项目需要跨多个里程碑和 Fresh Context 持续演进，因此维护：

```text
docs/project/project-roadmap.md
```

Roadmap 只维护项目级路线：已完成、当前、下一步和条件性后续。单个 Execution Unit、临时命令或局部实施步骤不进入 Roadmap。

README 只提供 Roadmap 入口，不并行维护第二份易变化的详细项目路线。

## 7. Skills

当前 baseline 的核心 Skills：

- `clarify-intent`
- `specify`
- `technical-plan`
- `slice-work`
- `readiness-check`
- `execute-unit`
- `systematic-debug`
- `converge`

平台专项 Skill：

- `github-actions-verification`

使用规则：

- 只加载当前工作真正需要的 Skill；
- 不要求每个工作都走完整 Skill 清单；
- Skill 不得覆盖 Consumer Authority；
- 遇到实现阶段的意外失败时，使用系统化调试路径，而不是无证据试错；
- 当 GitHub Actions 的触发、CI 可观察性、Artifact、容器 Runtime、timeout / cancellation 或 diagnostics 会影响证据可靠性时，按需应用 `github-actions-verification`；
- 如果调用要求实际完成 GitHub Actions 验证，dispatch / rerun 成功只是 `Act`，不是 Skill 退出条件；仍可观察的 `queued` / `pending` / `in_progress` Run 必须继续有界观察；
- 观察中持续核对 Run event、Head SHA、status / conclusion、Jobs / Steps / Logs / Artifacts 与当前 PR / Branch / Commit 的对应关系，只使用与当前目标提交真实关联的 Evidence；
- Run 失败且修复已获授权时，取得诊断证据后进入 `systematic-debug`，完成最小修复、重跑和复验；
- 如果调用只要求设计或优化验证路径而不要求实际执行，应返回 Evidence Retrieval Plan，并明确实际 Completion Evidence 尚未取得，不把计划写成已执行结果。

## 8. Fresh Context 恢复顺序

新的开发上下文默认按以下顺序恢复：

1. 读取根目录 `AGENTS.md`；
2. 读取 `README.md`；
3. 读取 `docs/project/project-roadmap.md`，确认当前路线和当前目标；
4. 读取 `docs/project/development-method.md`；
5. 读取与当前工作直接相关的 Requirement / Specification / Technical Plan / Work Artifact；
6. 读取当前代码、测试、Branch / PR / CI 等 Current Evidence；
7. 只在当前任务真实需要时加载对应 Skill。

不得依赖历史聊天或个人记忆补充未固化的项目事实。

## 9. baseline 升级规则

只有项目负责人明确要求更新 `agentic-dev` baseline 时，才执行 baseline 升级。

升级时：

1. 读取并记录 `agentic-dev` 指定分支最新精确 commit；
2. 对比本项目当前 baseline 到新 baseline 的 Method、Operating Guide、Contract 与 Skill 变化；
3. 区分跨项目可复用资产与 `agentic-dev` 自身 Project Rule；
4. 根据 Consumer 真实需要和现有 Authority 选择性采纳，不机械复制完整文档体系；
5. 将具有持续约束价值的已采纳规则固化到 Consumer 可发现的 Authority 中，并显式处理旧规则的更新、保留或取代；
6. 同步更新 `AGENTS.md`、本文和 Roadmap 的 baseline 记录；
7. 完成升级后恢复以 Consumer-local Authority 为普通开发入口，不自动继承 `agentic-dev` 自身 Project Roadmap、Issue、实验状态或其他项目事实。
