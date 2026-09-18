---
name: systematic-debug
description: 对真实失败做证据驱动诊断，避免无证据试错和掩盖陈旧验证契约。
metadata:
  jilinjobs-cms-id: skill:systematic-debug
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# systematic-debug

## 输入

当前 Consumer 仓库权威、与职责直接相关的 Work / GitHub 当前证据，以及调用该能力所需的最小上下文。

## 过程

1. 稳定复现并取得最小失败证据，区分 implementation、verification contract、runtime / environment、external dependency。
2. 沿数据流和最近相关变化缩小根因，不并行堆叠猜测修复。
3. 应用最小修复并重新取得受影响证据。

## 输出与退出

输出根因、最小修复、复验证据或明确 blocker。

若继续执行会实质改变 Product Goal、Scope、User-visible Behavior、Business Boundary、验收 Result、重大 Architecture Direction 或安全 / 隐私边界，则按 `AGENTS.md` Human Escalation 退出，不自行扩大 Authority。
