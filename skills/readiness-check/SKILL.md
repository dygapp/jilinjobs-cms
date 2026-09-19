---
name: readiness-check
description: 对 Candidate Execution Unit 做执行前门禁，只在可执行、可验证且 Authority 完整时给出 PASS。
metadata:
  jilinjobs-cms-id: skill:readiness-check
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# readiness-check

## 输入

当前 Consumer 仓库权威、与职责直接相关的 Work / GitHub 当前证据，以及调用该能力所需的最小上下文。

## 过程

1. 核对 Requirement / Specification / Technical Plan、依赖、当前 base 与 Work lifecycle。
2. 检查 验收 Obligation 是否映射到实现责任与验证责任。
3. 发现冲突、缺失或 base drift 无法消歧时 失败关闭。

## 输出与退出

输出 PASS / FAIL 与可复核理由；只有 PASS 才产生 Ready Execution Unit。

若继续执行会实质改变 Product Goal、Scope、User-visible Behavior、Business Boundary、验收 Result、重大 Architecture Direction 或安全 / 隐私边界，则按 `AGENTS.md` Human Escalation 退出，不自行扩大 Authority。
