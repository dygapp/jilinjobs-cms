---
name: technical-plan
description: 在确有长期 HOW 协调价值时形成技术方案，并保持与 Requirement / Specification 可追溯。
metadata:
  jilinjobs-cms-id: skill:technical-plan
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# technical-plan

## 输入

当前 Consumer 仓库权威、与职责直接相关的 Work / GitHub 当前证据，以及调用该能力所需的最小上下文。

## 过程

1. 确认 Technical Planning 是否真实需要；局部可逆实现不为形式完整持久化方案。
2. 选择最低必要复杂度，明确 ownership、数据 / 接口边界、迁移与验证责任。
3. 识别跨 Execution Unit 的依赖与可独立验证边界。

## 输出与退出

输出可供 slice-work 消费的 Technical Plan，或记录 NOT REQUIRED。

若继续执行会实质改变 Product Goal、Scope、User-visible Behavior、Business Boundary、验收 Result、重大 Architecture Direction 或安全 / 隐私边界，则按 `AGENTS.md` Human Escalation 退出，不自行扩大 Authority。
