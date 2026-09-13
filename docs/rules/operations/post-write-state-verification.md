---
id: rule:post-write-state-verification
type: rule
status: active
scope:
  phases: [execute, converge]
  activities: [external-operation]
  technologies: []
  artifacts: [repository, workflow]
  risks: []
---

# 写操作后的真实状态复核

创建 Branch、Commit、Pull Request、Issue、Workflow Run 或修改外部状态后，成功返回只证明写请求被接受。继续依赖该状态前，应重新读取对应系统的真实结果，核对目标 ref、Head、changed files、status / conclusion 或其他与当前声明直接相关的字段。

后续步骤不得仅根据预期状态、客户端缓存或前一次响应推断外部系统已经达到目标状态。
