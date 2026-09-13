---
id: rule:integration-state-closure-review
type: rule
status: active
scope:
  phases: [converge]
  activities: [integration, verification]
  technologies: []
  artifacts: [commit, evidence]
  risks: []
---

# 集成状态闭环复核

Implementation merge 不自动等于 Execution Unit closure。只有当前 Work lifecycle 要求的 Post-Integration verification / closure 已在实际集成提交上取得证据，Current Work locator 与 active artifact 已按仓库规则收口，才能声明 Execute Authority 终止。

历史 PR、Issue comment、archive 中的 READY / PASS 或旧 Head evidence 不能替代实际 integrated-main 的闭环证据。
