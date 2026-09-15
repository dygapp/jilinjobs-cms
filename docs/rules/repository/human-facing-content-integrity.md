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

编写或修改当前文档、代码、配置及其面向人的说明时，把语言一致性、机器标识精度、正式概念身份与文档生命周期作为同一内容完整性责任处理。

## Current 文档默认自然中文

本 Consumer 面向人的 Current / Partially Current 文档以中文为主述，必要时保留英文精确锚点。标题、状态、需求、规格、技术方案、验证说明和结论不得形成英文占主导的长期正文。

技术标准、代码标识、路径、命令、API / URL、协议、枚举、框架与固定专名可以保持原生形式。需要与外部 Method / Skill / Contract 精确对应时，首次重要出现可以使用“中文（English Term）”。

## 精确机器标识保持原样

文件路径、命令、配置键、Branch、Commit SHA、Issue / PR 编号、类名、方法名、字段名、枚举值、API / CLI、URL、协议值以及其他机器可解析标识符必须保持精确，不为语言统一、文案风格或术语美化进行机械翻译或近义替换。

需要中文解释时，在不改变原始标识符的前提下增加说明，而不是重命名机器接口。

## 正式概念身份不得被表达重写改变

中文化、术语整理、格式治理或表达优化不得改变 Product Goal、Scope、Business Boundary、User-visible Behavior、Architecture Decision、技术契约，也不得把职责或生命周期不同的 Method stage、Gate、Artifact、Skill、Rule、Architecture 等正式对象合并。

当自然语言名称可能对应多个正式对象时，必须使用对象类型或精确标识消除歧义。

## 长期 Authority 与 Front Matter

新增或实质重写长期维护的 canonical Authority 时，应遵循该资源类型已经采用的结构化 identity / status 约定；不要为了格式统一给历史 Evidence 批量补 metadata，也不要通过 Front Matter 建立第二份 Current State truth。

修改已有 Current Authority 时同时检查：

- semantic owner 是否仍正确；
- 是否残留被后继 Authority 取代的旧语义；
- 是否缓存了不属于该文件职责的 Current Gate / runtime state；
- 本地 locator 是否仍有效；
- 中文主述与机器标识是否仍满足本 Rule。

## Historical Evidence 保真

`SUPERSEDED / HISTORICAL_EVIDENCE` 以证据保真优先，不因语言、格式或 metadata 风格统一而破坏性改写历史正文。它们必须退出 ordinary Fresh Context Current Authority；若未来重新晋升为 Current Authority，先完成 semantic reconciliation，再满足当前文档规范。

历史内容的物理归档必须建立在明确 lifecycle 判定上，不按年龄、编号或“看起来旧”机械移动。

## Consumer-local 边界

本 Rule 只约束 `jilinjobs-cms`。如果后续形成跨 Consumer 的可复用证据，只能通过显式 feedback / upgrade lifecycle 反馈 `agentic-dev`，不会自动改变 upstream capability。