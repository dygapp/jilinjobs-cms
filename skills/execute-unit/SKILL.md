---
name: execute-unit
description: 执行已通过 Readiness 的单个 Execution Unit，保持最小差异并闭合当前验证责任。
metadata:
  jilinjobs-cms-id: skill:execute-unit
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# execute-unit

## 输入

当前 Consumer 仓库权威、与职责直接相关的 Work / GitHub 当前证据，以及调用该能力所需的最小上下文。

## 过程

1. 重新确认 Ready Authority、当前 base 和目标文件。
2. 先从任务事实提取 bounded signals，使用 Consumer-local Rule Discovery，只读取候选 Rule 正文并做语义适用性确认。
3. 实施最低必要复杂度；失败进入 systematic-debug；完成前执行 scoped verification 与 final diff scope check。

## 输出与退出

输出当前实现、验证证据与 Stage Return，不越过 Integration / 人工权威。

若继续执行会实质改变 Product Goal、Scope、User-visible Behavior、Business Boundary、验收 Result、重大 Architecture Direction 或安全 / 隐私边界，则按 `AGENTS.md` Human Escalation 退出，不自行扩大 Authority。
