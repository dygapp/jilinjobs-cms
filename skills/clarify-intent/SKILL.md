---
name: clarify-intent
description: 在存在实质产品歧义时收敛用户意图，不在证据充分时制造额外确认。
metadata:
  jilinjobs-cms-id: skill:clarify-intent
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# clarify-intent

## 输入

当前 Consumer 仓库权威、与职责直接相关的 Work / GitHub 当前证据，以及调用该能力所需的最小上下文。

## 过程

1. 读取当前 Requirement、Specification 与直接 当前证据，列出真正会改变产品结果的歧义。
2. 能由 Authority 或低影响可逆工程判断解决的事项直接收敛，不升级人工。
3. 只对会改变 Product Goal、Scope、User-visible Behavior、Business Boundary 或验收结果的问题请求人工决定。

## 输出与退出

输出已收敛意图、剩余高影响决策或明确的无歧义结论。

若继续执行会实质改变 Product Goal、Scope、User-visible Behavior、Business Boundary、验收 Result、重大 Architecture Direction 或安全 / 隐私边界，则按 `AGENTS.md` Human Escalation 退出，不自行扩大 Authority。
