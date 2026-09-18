---
id: rule:human-intervention-necessity
type: rule
status: active
scope:
  phases: []
  activities: [human-escalation]
  technologies: []
  artifacts: []
  risks: [human-intervention]
---

# 人工介入前必须验证其必要性

当 Agent 准备请求人工执行操作、提供输入、做出决定或充当工具 / 系统之间的中转时，必须先验证该人工介入是否确实不可替代。

本 Rule 的目标是减少不必要的人工桥接，不削弱 人工权威。工具可用不等于自动获得授权；merge、release、deploy、破坏性操作、产品决策、架构决策、权限与安全边界继续由各自 Authority 决定。

## 先验证可自动完成的路径

发出人工请求前，至少检查当前实际相关的以下路径：

- 当前是否已有 connector、API、standard git / GitHub、container、Python、browser、Actions 或其他已授权工具可以直接完成；
- 主路径受限时，是否存在结果等价、可验证且风险不更高的替代执行路径；
- 所需事实是否已存在于 Repository、Issue / PR、Actions、日志、文件、当前 Authority 或其他可信 证据 中，可以直接恢复；
- 当前请求是否只是让人工复制粘贴 deterministic tool output、运行无需判断的命令、下载再上传，或在两个可访问系统之间搬运数据。

如果存在可由 Agent 完成且不会绕过 Authority 的等价路径，应优先由 Agent 完成。无需穷举所有理论工具；只需覆盖当前环境真实可用、与目标相关且风险不更高的合理路径。

## 只有不可替代的人类责任才升级

人工介入通常只有在至少存在以下一种真实 blocker 时才成立：

- Product / Domain intent 无法由当前 Authority 唯一确定；
- 高影响、难逆的 business / architecture / integration 决定明确保留给 人工权威；
- merge、release、deploy、破坏性远程操作或其他动作被 Repository / organization policy 明确保留给人工批准；
- 当前 Agent 缺少必要 credential / permission，且不存在已授权替代路径；
- 必须观察 Agent 当前不可访问的本地、物理或受控环境，且该环境事实对当前 claim 是实质性的；
- 法规、合同、安全或组织职责明确要求人类承担该动作或决定。

不得为了减少交互而猜测 Product / Domain intent，也不得用“自动化优先”绕过权限、凭证、安全或 人工权威。

## 人工请求必须最小化

确认人工确实不可替代后，请求必须说明：

- 为什么当前自动化 / cloud / tool 路径不足；
- 需要人工承担的最小动作或最小决定；
- 人工需要返回什么 证据 / decision；
- 收到输入后哪些后续步骤仍由 Agent 自动继续完成。

除非本地环境本身就是不可替代的验证对象，否则不得默认要求人工“在本地执行命令并把输出粘贴回来”。

## 完成判据

发出人工请求前，必须能明确回答：

1. 已检查哪些当前可用的自动化 / 证据 路径；
2. 为什么它们不足以完成当前责任；
3. 剩余 blocker 属于哪一种不可替代的人类责任；
4. 请求是否已经缩减到最小必要人工动作。
