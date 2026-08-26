# Agent 驱动开发连续执行模式

## 目的

记录多步骤工程任务所需的执行模式，尽量减少不必要的人工介入，并将 RC-01 中验证过的连续执行经验固化为本 Consumer 的长期操作规则。

当前规则已与 `agentic-dev` baseline `master@2ee56a5866d0201977a75b2b18ca2e791a218983` 中的 External Operation Guide 与 `github-actions-verification` 异步闭环规则对齐。

## 预期执行模型

任务应按有状态循环持续处理：

1. 观察当前仓库及外部执行状态；
2. 规划下一个最小动作；
3. 应用变更；
4. 触发验证；
5. 有界观察异步运行状态；
6. 收集与当前提交匹配的证据；
7. 分析失败并取得必要诊断信息；
8. 在当前 Scope 和授权范围内直接修复、重试；
9. 重复执行，直至取得目标证据、出现真实阻塞，或达到有界观察上限。

## 异步外部操作不是完成点

GitHub Actions、Deployment、远程 Job 等异步外部系统不是完成点，而是需要继续观察状态和收集证据的中间状态。

错误模式：

```text
修改
→ 触发工作流
→ 工作流仍在 queued / pending / in_progress
→ 请求人工继续
```

推荐模式：

```text
修改
→ 触发工作流
→ 观察运行状态
→ 收集 Current Evidence
→ 失败时取得日志 / Artifact / 诊断证据
→ 在已授权范围内修复
→ 重新运行
→ 重新观察
→ 完成验证或准确记录真实阻塞
```

dispatch / rerun 的 API 成功响应只证明 `Act` 被接受，不代表 Completion Verification 已完成。只要当前目标要求取得运行结果、Runtime 仍能继续观察，就不得仅因为 Run 尚未结束而默认把任务交回人工。

## 有界观察与证据关联

连续执行不等于无限等待。

- 根据正常运行基线、Job / Step timeout、运行进度和 Repository Policy 选择合理轮询间隔与观察上限；
- 每次观察重新读取当前事实来源，不使用旧状态推断运行已经结束；
- GitHub Actions 验证持续核对 event、Head SHA、status / conclusion、Jobs / Steps / Logs / Artifacts 与当前 PR / Branch / Commit 的关联；
- Artifact 承担必要 Verification Evidence 时，应确认 Artifact 实体存在并与当前 Run / Head SHA 对应，不能只以 upload step 成功替代 Artifact 证据；
- 达到有界观察上限仍未结束时，只能记录 `Executed but not fully verified`，不能声明完成。

## 人工介入边界

只有出现真实需要 Human Authority 或人工判断的情况才请求人工输入，例如：

- 缺少当前操作所需外部权限；
- 需要改变 Product Goal、Scope、Acceptance 或重大架构方向；
- 需要业务验证或最终人工验收测试；
- 涉及 Security / Privacy、Production、不可逆或其他高影响决定；
- Runtime 无法继续取得必要证据，且没有已授权的替代路径。

Run 正常处于 `queued`、`pending`、`in_progress` 且仍可观察，本身不构成人工介入条件。

对于可以根据当前仓库和 Runtime Evidence 诊断、修复并重试的中间失败，不请求人工介入。

## 多 Repository 授权

连续执行只在当前授权范围内进行。同一任务涉及多个 Repository 时，应分别读取并遵守各自授权边界。

当前 Consumer 的具体权限矩阵以根目录 `AGENTS.md` 为准：`dygapp/jilinjobs-cms` 可执行已授权的正常 Repository 操作；`dygapp/agentic-dev` 仅允许读取与 Issue 反馈，不得因为 Runtime 技术能力存在而扩大写权限。

## 退出条件

连续执行闭环可在以下条件之一成立时结束：

1. 当前目标所需 Current Evidence 已取得并核对；
2. 出现需要 Human Authority 的真实阻塞；
3. Runtime 无法继续取得必要 Evidence 且没有已授权替代路径；
4. 达到有界观察上限，并准确保留为 `Executed but not fully verified`。

只有第 1 类结果允许据此声明对应验证目标完成；其余结果必须保留真实状态。

## RC-01 经验

RC-01 证明了工作流、部署、隧道和外部环境失败都应作为证据来源。Agent 应持续完成观察、诊断、修复和重新验证，不能在报告中间状态后停止执行。

这一 Consumer Evidence 已反馈到 `agentic-dev` Issue #33，并在当前 baseline 中沉淀为通用多 Repository 授权与异步验证闭环规则；本文件保留的是这些通用规则在 `jilinjobs-cms` 中的具体长期执行约束。
