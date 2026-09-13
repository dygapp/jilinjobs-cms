---
id: rule:async-operation-bounded-observation
type: rule
status: active
scope:
  phases: [execute, converge]
  activities: [external-operation, verification]
  technologies: []
  artifacts: [workflow]
  risks: [async-operation]
---

# 异步外部操作的有界观察

GitHub Actions、远程 Job、部署或其他异步外部操作只有在目标 Run / Job 的 event、Head SHA、状态和结论与当前 claim 对应时，才能成为有效证据。

触发成功只是 Act，不是 Completion。对仍可观察的 queued / pending / in_progress 状态继续进行有界观察；失败时先取得可诊断证据，再决定最小修复、重试或升级，不以无限轮询替代失败处理。
