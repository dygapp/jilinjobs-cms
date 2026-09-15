---
id: method:method-experiment
type: method
status: active
---

# Consumer-local 方法实验与证据晋升方法

## 目标

管理尚不足以直接修改长期 Consumer Authority、但需要跨多个真实工作样本继续观察的方法 / 工程实践假设。GitHub Issue 可以承载临时 Evidence，但不因此成为长期 Authority 或永久 Evidence Channel。

## 生命周期

```text
Define Hypothesis
→ Open Bounded Evidence Window
→ Collect Evidence / Counterexamples
→ Converge
→ Promote / Reject / Supersede
→ Close Evidence Carrier
```

## Define Hypothesis

只有真实观察到 Method / Engineering Capability / Workflow / Review / Execution 问题，单次事件不足以形成长期结论，并且能够定义 Observation Scope、Evidence Questions、Promotion Criteria 与 Close Criteria 时才进入。

普通 Feature 状态、单次测试日志、临时施工步骤或已有明确 Authority 结论的问题不进入本 Method。

## Evidence window

临时 Issue 可以记录正向实践、反例、Workflow / CI / Review 成本、Human Finding、局部修复效果及与当前 Authority 的冲突。每条关键证据尽量保留 PR / commit / Run / Artifact / Review anchor。

Issue 中的建议、假设或阶段判断不能覆盖 AGENTS、Requirement、Method、Architecture、Specification、Technical 或 Verification Authority。

## Converge

达到 Promotion Criteria、证据明显不再增长或观察窗口结束时，分类为：

- Consumer-specific；
- Cross-project reusable；
- Consumer-specific + Cross-project reusable；
- Rejected / Superseded。

## Promote / Reject

Promotion 只把稳定、可执行、具有长期约束价值的结论写入真实 Consumer-local owner；不是复制 Issue 正文。跨项目结论如有价值，可在仓库授权范围内向 `agentic-dev` 提交 Evidence / Feedback，但 upstream 是否接受由其自身 Authority 决定。

Rejected / Superseded 假设不得固化为长期规则；临时 artifact 应清理或明确退出 current Authority。

## Close

长期结论已进入真实 owner、原始 Evidence 可由 GitHub 对象追溯、未采纳 / 待观察项已有明确处置后，关闭临时 Evidence carrier。新的实验周期默认创建新的 bounded carrier，不把旧 Issue 重新变成长期开口。