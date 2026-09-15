---
id: method:review-feedback-cycle
type: method
status: active
---

# Review Feedback Cycle

## 目标

本 Consumer-local Method 管理已经进入 Human Review 的变更在收到 Finding 后的批量修复、定向验证与 Human Re-review，避免每个 Finding 都机械重跑完整验证链，同时不降低最终 Acceptance。

## 生命周期

```text
Human Review Finding
→ Feedback Batch
→ Impact Classification
→ Targeted Fix
→ Targeted Verification
→ Human Re-review
→ Ready to return / Integration Gate
```

本 Method 不替代 `method:ai-development` 的 Feature lifecycle，也不授予 merge / deploy 权限。

## Feedback Batch

除单个高优先级 blocker 外，同一次 Human Review 的 Findings 优先形成 Batch。对每项 finding 判断：

- 是否属于当前 Authority / Scope；
- 影响 Requirement / Specification / Architecture / code / verification 的哪一层；
- 是否需要返回上游 owner；
- 最低充分验证范围；
- 是否改变 Human Review baseline。

Authority 已明确的 Finding 应在同一 Batch 内连续修复和定向验证，不因单项完成机械停下等待“继续”。

## Verification tier

最低充分范围按实际 claim 选择：

- L0：文档 / 文案 / 非运行语义静态检查；
- L1：局部页面、样式、资源、单接口等定向验证；
- L2：Execution Unit 核心行为、数据结构、业务逻辑的相关 workflow / integration evidence；
- L3：重大边界或 Unit 收口所需完整验证；
- L4：用户可见流程 / 视觉等 Human Integration Review。

当前稳定 Verification Strategy、PR policy 或 workflow topology 要求更高层时服从更高要求；本分层不能用于绕过 Acceptance。

## Return / escalation

- Finding 需要改变 Product Goal、Scope、Acceptance、长期 Architecture 或 Security / Privacy → 返回真实 Authority / Human decision；
- 修复后仍需人工观察的内容 → Human Re-review；
- Batch 已无未解决 blocker / medium finding 且验证充分 → 返回原 lifecycle 的下一 Repository / Human Gate。

旧 Review Evidence 只有在能够证明不受后续修改影响时才复用。