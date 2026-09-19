---
id: rule:handoff-continuity
type: rule
status: active
scope:
  phases: []
  activities: [handoff]
  technologies: []
  artifacts: []
  risks: []
---

# Handoff 最小化与连续性

Fresh Context 用于控制知识边界，不等于把同一授权工作机械拆成多个短任务。只有确有未完成状态需要跨 Context / Agent / 时间传递，且单靠仓库权威与 GitHub native state 无法以同等可靠性恢复时，才形成最小临时 Handoff。

## 最小 Handoff

Handoff 只保存下一执行者无法从 Current Authority、Current Work locator 与 GitHub 原生状态可靠恢复的最小信息。不得为了“交接完整”复制 Requirement、Specification、Technical Plan、Rule 正文、当前 PR / Actions 状态或其他已有真实 owner 的内容。

会话切换提示词只承担 locator / unresolved boundary；它不获得第二套 Current State、Planning Authority、Execute Authority 或 Rule routing 责任。

## 生命周期

Handoff 被消费、正式 owner 已接管，或相关未完成状态已经进入可恢复的仓库 / GitHub owner 后立即失效。临时 Handoff 不升级为长期 Project Knowledge，也不与 Current Authority 双向同步。

生成 Handoff 不自动结束当前 Method / Review Batch，也不授予下一 Context 新的产品、执行、集成或外部操作权限；下一执行者仍必须从当前仓库事实重新恢复 Authority 与责任。
