---
name: specify
description: 把已确认意图形成 WHAT / WHY 规格，不把实现偏好伪装成产品需求。
metadata:
  jilinjobs-cms-id: skill:specify
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# specify

## 输入

当前 Consumer 仓库权威、与职责直接相关的 Work / GitHub 当前证据，以及调用该能力所需的最小上下文。

## 过程

1. 读取当前 Requirement 与相关 Architecture，确认语义 owner。
2. 定义行为、边界、失败语义与可验证 acceptance obligations。
3. 把长期 HOW 留给 Technical Planning；没有产品 Authority 的能力不得写入规格。

## 输出与退出

输出 Ready Specification 或明确指出阻塞其 Ready 的产品决策。

若继续执行会实质改变 Product Goal、Scope、User-visible Behavior、Business Boundary、验收 Result、重大 Architecture Direction 或安全 / 隐私边界，则按 `AGENTS.md` Human Escalation 退出，不自行扩大 Authority。
