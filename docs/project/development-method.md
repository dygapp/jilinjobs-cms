# Consumer 开发方法与 Skill 使用规则

本文固化 `jilinjobs-cms` 在持续开发阶段实际采用的 AI Agent 开发方法、Skill 使用规则和上下文恢复规则。

本文是 Consumer-local 规则。后续开发默认优先读取并遵循本仓库 `AGENTS.md`、`README.md`、本文及其他当前 Authority；不需要在每次普通开发工作中重新读取 `dygapp/agentic-dev`。只有项目负责人明确要求升级 `agentic-dev` baseline，或当前 Consumer 文档明确无法回答方法问题时，才重新读取指定 baseline 并将需要长期保留的变化回写到本仓库。

## 1. 方法来源与当前基线

```text
Repository:
dygapp/agentic-dev

Baseline:
master@b4e5b2027bdbbe97cc0b7153be65c5afb7a0274e
```

该 baseline 提供 Method、Operating Guide、Skill Contracts 与 Skills 的来源依据，但不提供本项目的业务事实。

## 2. Consumer 与 agentic-dev 的职责边界

- `agentic-dev` 决定可复用的“如何工作”。
- `jilinjobs-cms` 决定本项目的 Goal、Scope、Requirements、Specification、Architecture、Code、Tests、Verification 与 Integration Policy。
- `agentic-dev/docs/project/*` 中属于其自身项目的状态、路线和实验事实不得复制为 Consumer 项目事实。
- 从 `agentic-dev` baseline 吸收的方法变化，应转化为本仓库可直接执行的本地规则，而不是要求后续 Agent 持续跨仓库读取方法文档。

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

## 5. Project Roadmap

本项目需要跨多个里程碑和 Fresh Context 持续演进，因此维护：

```text
docs/project/project-roadmap.md
```

Roadmap 只维护项目级路线：已完成、当前、下一步和条件性后续。单个 Execution Unit、临时命令或局部实施步骤不进入 Roadmap。

README 只提供 Roadmap 入口，不并行维护第二份易变化的详细项目路线。

## 6. Skills

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
- 当 GitHub Actions 的触发、CI 可观察性、Artifact、容器 Runtime、timeout / cancellation 或 diagnostics 会影响证据可靠性时，按需应用 `github-actions-verification` 的规则。

## 7. Fresh Context 恢复顺序

新的开发上下文默认按以下顺序恢复：

1. 读取根目录 `AGENTS.md`；
2. 读取 `README.md`；
3. 读取 `docs/project/project-roadmap.md`，确认当前路线和当前目标；
4. 读取与当前工作直接相关的 Requirement / Specification / Technical Plan / Work Artifact；
5. 读取当前代码、测试、Branch / PR / CI 等 Current Evidence；
6. 只在当前任务真实需要时加载对应 Skill。

不得依赖历史聊天或个人记忆补充未固化的项目事实。

## 8. baseline 升级规则

只有项目负责人明确要求更新 `agentic-dev` baseline 时，才执行 baseline 升级。

升级时：

1. 读取 `agentic-dev` 指定分支最新精确 commit；
2. 对比本项目当前 baseline 到新 baseline 的 Method、Operating Guide、Contract 与 Skill 变化；
3. 只吸收会影响当前 Consumer 工作方式的变化；
4. 将这些变化固化到本仓库对应文档；
5. 同步更新 `AGENTS.md`、本文和 Roadmap 中的 baseline 记录；
6. 不自动继承 `agentic-dev` 自身的 Project Roadmap、Issue、实验状态或其他项目事实。
