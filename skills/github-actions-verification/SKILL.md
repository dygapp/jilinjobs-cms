---
name: github-actions-verification
description: 对 GitHub Actions 执行、观察和证据归属进行有界验证。
metadata:
  jilinjobs-cms-id: skill:github-actions-verification
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# github-actions-verification

## 输入

当前 Consumer 仓库权威、与职责直接相关的 Work / GitHub 当前证据，以及调用该能力所需的最小上下文。

## 过程

1. 确认目标 workflow、event、branch / PR、Head SHA 与所需 claim。
2. 触发或观察后持续核对 Run、Jobs、Steps、status / conclusion 和必要 logs；queued / in_progress 不是完成。
3. 失败时先取得诊断证据，再进入最小修复 / 重试；只使用与目标提交真实关联的结果。

## 输出与退出

输出与 exact commit 对应的 Actions 证据，或明确尚未完成。

若继续执行会实质改变 Product Goal、Scope、User-visible Behavior、Business Boundary、验收 Result、重大 Architecture Direction 或安全 / 隐私边界，则按 `AGENTS.md` Human Escalation 退出，不自行扩大 Authority。
