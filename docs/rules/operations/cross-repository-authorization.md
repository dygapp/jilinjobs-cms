---
id: rule:cross-repository-authorization
type: rule
status: active
scope:
  phases: [execute, converge]
  activities: [external-operation]
  technologies: []
  artifacts: [repository]
  risks: [cross-repository]
---

# 跨仓库授权边界

同一任务涉及多个 Repository 时，必须分别依据当前 Consumer `AGENTS.md` 的 Repository Operation Boundary 判断可读、可写、可提交 Issue / PR、可运行 Actions 等权限。连接器或凭据具备技术写权限，不等于当前工作已经获得该写权限。

任何跨仓库写操作都必须能追溯到对应 Repository 的明确授权；授权不足时保留本地结果并按 Human Escalation 处理，不得用“同一组织”“同一用户”或工具能力推导权限。
