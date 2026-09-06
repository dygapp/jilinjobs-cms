---
title: Consumer-local 方法实验与临时证据 Issue 生命周期
status: 已固化
scope: Consumer-local Method
---

# Consumer-local 方法实验与临时证据 Issue 生命周期

## 1. 目的

本规范用于管理尚不足以直接修改长期 Consumer Authority、也尚不足以直接反馈 `dygapp/agentic-dev` 的方法实验、工程实践和运行观察。

这类证据可以暂存于 GitHub Issue，但该 Issue 默认是**临时实验载体**，不是新的长期 Authority，也不是永久 Evidence Channel。实验成熟后必须完成结论收敛、证据晋升（Promotion）或拒绝（Reject / Supersede），随后关闭对应 Issue。

目标：

- 避免方法实验只存在于聊天记录、会话记忆或临时上下文；
- 避免尚未充分验证的观察直接污染长期 Development Method；
- 避免一个长期开放 Issue 演化为无边界的“方法证据垃圾桶”；
- 让 Consumer-local 固化、跨项目反馈和实验终止都有清晰生命周期；
- 保证 Fresh Context 可以仅依赖 Repository / GitHub 状态恢复实验目的、证据和最终结论。

## 2. 何时创建临时 Evidence Issue

只有同时满足以下条件时才建议创建：

1. 已观察到真实的 Method / Engineering Capability / Workflow / Review / Execution 问题或改进机会；
2. 单次事件不足以证明应立即修改长期 Consumer Authority；
3. 需要跨多个 Execution Unit、PR、Review Batch、Workflow Run 或不同类型工作继续观察；
4. 证据具有明确主题，可以定义验证假设和结束条件。

不应为了普通 Feature 状态、单次测试日志、临时施工步骤或已经有明确 Authority 结论的问题机械创建 Evidence Issue。

## 3. 创建时的最小信息

临时 Evidence Issue 至少应说明：

- **Validation Theme**：本轮要验证的方法或工程主题；
- **Current Hypothesis**：当前假设或待验证结论，不得写成已经确认的长期规则；
- **Observation Scope**：计划观察哪些 EU / PR / Review / Workflow / Runtime 场景；
- **Evidence Questions**：什么事实可以支持或反驳该假设；
- **Promotion Criteria**：达到什么条件后可以固化到 Consumer-local Authority 或反馈 `agentic-dev`；
- **Close Criteria**：什么情况下本轮实验应结束并关闭 Issue。

Issue 标题和正文应明确其临时实验属性，避免被误读为长期项目规划入口或稳定 Authority。

## 4. 证据积累规则

实验期间可以通过 Issue Comment 追加：

- 正向实践证据；
- 反例和失败样本；
- Workflow / CI / Review 成本；
- Human Review Finding；
- 不同 Execution Unit 的重复观察；
- 已采取的 Consumer-local 局部修复及其效果；
- 与当前稳定 Authority、Verification Strategy 或 Workflow topology 的冲突。

每条证据应尽量保留可追溯锚点，例如 PR、commit、Workflow Run、Issue Comment、Artifact、Verification Result 或最终合并状态。

临时 Evidence Issue 只记录证据和实验判断，**不能因为 Issue 中出现某个建议，就自动覆盖 `AGENTS.md`、`README.md`、Development Method、Requirement、Specification、Technical Plan 或 Verification Authority。**

## 5. 实验收敛与分类

达到 Promotion Criteria、证据明显不再增长，或当前阶段结束时，必须执行一次 Convergence。最终结论至少分类为以下之一：

### 5.1 Consumer-specific

结论只适用于 `jilinjobs-cms` 的产品、仓库结构、Workflow topology、部署或团队约束。

处理：

- 将稳定规则固化到相应 Consumer-local Authority；
- 记录固化文件 / PR / merge evidence；
- 不因为已经验证就机械反馈到 `agentic-dev`。

### 5.2 Cross-project reusable

证据显示问题或模式具有跨项目 Method / Engineering Capability 价值。

处理：

- 提炼掉 Consumer 产品细节；
- 保留必要 provenance；
- 按仓库权限向 `dygapp/agentic-dev` 的现有反馈渠道提交 Issue / Comment；
- Consumer 不预设 `agentic-dev` 最终是否修改 Method、Guide、Skill 或 Capability。

### 5.3 Consumer-specific + Cross-project reusable

同一实验同时产生本地稳定规则和可复用上游证据。

处理：

- Consumer-local Authority 与上游反馈分别完成；
- 不要求两边采用相同文案或相同实现机制。

### 5.4 Rejected / Superseded

证据不支持原假设，或后续 Authority / Architecture 已使该实验失效。

处理：

- 在 Issue 中记录为什么不采纳；
- 不将失败假设固化成长期规则；
- 如已有临时文档，应清理或标记 superseded。

## 6. Promotion 要求

Promotion 不是“把 Issue 内容复制到文档”。应只固化已经稳定、可执行且具有长期约束价值的结论。

Promotion 结束前应确认：

1. Consumer-local 需要长期保留的规则已进入正确 Authority；
2. 跨项目可复用证据如有需要，已反馈到 `agentic-dev`；
3. 原始证据仍可通过 Issue / PR / Run 等 GitHub 对象追溯；
4. 未被采纳、仍待观察或被 supersede 的部分已经明确标记；
5. Promotion 后没有长期消费者继续依赖某个临时 Issue 作为唯一规则来源。

## 7. 关闭规则

临时 Evidence Issue 在完成 Convergence 后应关闭，不作为永久开放的证据总线。

关闭前必须追加 Final Promotion Record，至少包含：

- 实验窗口和主要证据范围；
- 最终分类结论；
- 已固化到哪些 Consumer-local 文档 / PR；
- 已反馈到哪个 `agentic-dev` Issue / Comment（如适用）；
- 哪些问题仍未解决，但应进入新的 Planning / Governance / Experiment，而不是继续占用本 Issue；
- 关闭原因。

满足以上条件后，以 `completed` 关闭。

## 8. 后续实验与重新打开

默认规则是：**新的实验周期创建新的临时 Evidence Issue。**

不要因为主题相近，就把已经完成 Promotion 的旧 Issue 重新变成长期开口。只有发现旧实验的最终结论本身需要被纠正，并且直接修改原结论的审计价值明显高于新开实验时，才考虑重新打开。

例如：

- “Verification tier 与实际 Workflow trigger topology 的进一步对账”如果未来进入新的正式试验，应创建新的 Evidence Issue；
- 旧 Issue 可以作为历史 provenance 引用，但不继续承担活跃观察职责。

## 9. 与 `agentic-dev` 的关系

临时 Evidence Issue 属于 Consumer Repository 的实验治理机制。

- 它不构成 `agentic-dev` 项目事实；
- 创建 Issue 不意味着上游必须修改；
- 只有达到跨项目 Promotion Criteria 后才反馈上游；
- 上游如何分类、吸收或拒绝，由 `agentic-dev` 自身 Authority 决定；
- Consumer 后续仍以本仓库已固化的 Development Method 为普通开发入口。

## 10. 与其他 Consumer-local Practice 的关系

`docs/project/review-feedback-cycle.md`、`docs/project/execution-continuity-guidelines.md` 等 Consumer-local Method 如需开展新的观察期，均使用本生命周期管理对应临时 Evidence Issue。

任何具体 Issue 编号都只能是一次实验的历史载体，不应写成这些长期规范永久依赖的固定 Evidence Channel。
