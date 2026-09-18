---
name: converge
description: 在实现完成后收敛 验收 Obligations、当前证据、PR 状态与 Integration readiness。
metadata:
  jilinjobs-cms-id: skill:converge
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# converge

## 输入

当前 Consumer 仓库权威、与职责直接相关的 Work / GitHub 当前证据，以及调用该能力所需的最小上下文。

## 过程

1. 从当前任务事实运行 verification Rule Discovery，语义确认适用 Rule。
2. 核对 exact Head / diff / CI / Review / Runtime evidence 与每项 claim。
3. 只在全部当前义务闭环时声明 Ready to Integrate；Integration 后仍服从 Work lifecycle 的 Post-Integration closure。

## 输出与退出

输出 Ready to Integrate 或剩余 gap；不把历史 evidence 当成当前完成证据。

若继续执行会实质改变 Product Goal、Scope、User-visible Behavior、Business Boundary、验收 Result、重大 Architecture Direction 或安全 / 隐私边界，则按 `AGENTS.md` Human Escalation 退出，不自行扩大 Authority。
