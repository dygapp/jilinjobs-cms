---
id: rule:human-facing-content-integrity
type: rule
status: active
scope:
  phases: [planning, technical-planning, execute, converge]
  activities: [documentation, implementation]
  technologies: []
  artifacts: [configuration, code, document]
  risks: []
---

# 面向人的内容完整性

编写或修改当前文档、代码、配置及其面向人的说明时，把语言一致性、机器标识精度与正式概念身份作为同一内容完整性责任处理。

## Current 文档默认自然中文

本 Consumer 面向人的 Current / Partially Current 文档以中文为主述，必要时保留英文精确锚点。历史证据以证据保真优先；重新晋升为 Current Authority 前再满足当前语言规范。

## 精确机器标识保持原样

文件路径、命令、配置键、Branch、Commit SHA、Issue / PR 编号、类名、方法名、字段名、枚举值、API / CLI、URL、协议值以及其他机器可解析标识符必须保持精确，不为语言统一、文案风格或术语美化进行机械翻译或近义替换。

需要中文解释时，在不改变原始标识符的前提下增加说明，而不是重命名机器接口。

## 正式概念身份不得被表达重写改变

中文化、术语整理或表达优化不得改变 Product Goal、Scope、Business Boundary、User-visible Behavior、Architecture Decision、技术契约，也不得把职责或生命周期不同的 Method stage、Gate、Artifact、Skill、Rule、Architecture 等正式对象合并。

当自然语言名称可能对应多个正式对象时，必须使用对象类型或精确标识消除歧义。
