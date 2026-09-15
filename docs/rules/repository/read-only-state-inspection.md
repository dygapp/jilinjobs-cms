---
id: rule:read-only-state-inspection
type: rule
status: active
scope:
  phases: []
  activities: [status-inspection]
  technologies: []
  artifacts: [repository]
  risks: []
---

# 只读 Repository 状态检查边界

当用户当前目标只是恢复、检查、复核或总结 Repository 状态时，该责任默认保持只读；不得把观察到的可改进项自动升级成新的 Planning / Execute / Repair lifecycle。

## 安全停止

如果协调 `docs/work/current/README.md`、当前 Open execution PR / branch 与必要 Current Evidence 后得到：

```text
Current Ready / active Execution Unit = NONE
```

则在完成以下事项后停止：

1. 核对当前 Repository Authority、Roadmap、Current Work 与必要 GitHub native state；
2. 准确报告当前状态与下一自然 Planning / Readiness Gate；
3. 不自动创建新的 Candidate、Execution Unit、Branch、Commit、PR、Workflow Run 或修复生命周期。

`NONE` 只表示没有 Ready / active Execution Unit，不表示没有 Planning Candidate。

## 状态检查中的 Finding

观察到的问题先分类：

- **Blocking Authority Drift**：已经使当前 Authority 无法一致解释，准确报告 blocker；只有用户当前目标已授权修复或随后明确授权时才进入写操作；
- **Non-blocking Drift / Maintenance Finding**：作为后续 maintenance finding 保留，不在状态检查中“顺手修复”。

已完成并终止 Execute Authority 的 Execution Unit 只能在新的当前工作明确依赖其结果时作为 dependency evidence 读取；dependency revalidation 不等于重新打开旧 Unit。

本 Rule 不阻止用户明确要求的治理、Planning 或修复工作；一旦目标从状态检查切换为新的 direct responsibility，必须重新执行 Rule Discovery 并按新的 Authority 工作。