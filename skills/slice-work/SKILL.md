---
name: slice-work
description: 把 Ready Specification 与必要 Technical Plan 切成纵向、可独立验证的 Candidate Execution Units。
metadata:
  jilinjobs-cms-id: skill:slice-work
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# slice-work

## 输入

当前 Consumer Repository Authority、与职责直接相关的 Work / GitHub Current Evidence，以及调用该能力所需的最小上下文。

## 过程

1. 确认输入已经 Ready，未 Ready 的 Planning Candidate 不切入 Execute。
2. 按用户可见责任、依赖和验证闭环形成最小纵向 Unit。
3. 稳定 Identifier 只承担追踪身份，不代表 Readiness PASS。

## 输出与退出

输出 Candidate Execution Unit 集合及依赖，不授予 Execute Authority。

若继续执行会实质改变 Product Goal、Scope、User-visible Behavior、Business Boundary、Acceptance Result、重大 Architecture Direction 或安全 / 隐私边界，则按 `AGENTS.md` Human Escalation 退出，不自行扩大 Authority。
