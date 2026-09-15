---
name: external-operation
description: 对已授权的 GitHub、CI、Review Environment、远程 API 或其他外部可变状态执行最小变更，并通过重新读取与有界观察验证真实结果；只读检查或纯本地修改不使用本 Skill。
metadata:
  jilinjobs-cms-id: skill:external-operation
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# external-operation

## 输入

- 目标外部对象与期望状态；
- 当前 Repository / Human Authority；
- 可用工具与认证边界；
- 当前外部状态；
- 当前责任下已发现并确认适用的 Rules。

## 过程

1. 重新读取目标外部对象的当前事实，确认请求目标、作用域与授权。
2. 在首个有副作用动作前完成当前 direct responsibility 的 Rule Discovery，并应用真实适用 Rule。
3. 选择达到目标所需的最小必要、优先可逆操作，不顺带修改无关状态。
4. 执行外部写操作。
5. 重新读取事实来源验证目标状态，不把 API 成功响应当作完成证据。
6. 对 GitHub Actions、部署、远程 Job 等异步操作执行有界观察；失败时取得诊断证据，在当前 Authority 内修复 / 重试。
7. 只汇报当前证据支持的状态；未验证结果保持未验证。

## 输出与退出

输出已执行操作、重新读取后的真实状态、必要诊断证据与剩余 blocker。

以下任一情况退出：

- 目标状态已经由当前证据确认；
- 当前 Authority / 凭据 / 工具无法继续；
- 异步观察达到有界上限并准确保留为未完全验证；
- 操作属于 merge、release、deploy、破坏性远程变化或其他当前 Authority 明确保留给人工的事项。

本 Skill 不授予任何外部写权限。