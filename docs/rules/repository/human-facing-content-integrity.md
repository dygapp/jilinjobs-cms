---
id: rule:human-facing-content-integrity
type: rule
status: active
scope:
  phases: []
  activities: [documentation, communication, review, implementation]
  technologies: []
  artifacts: [human-facing-content, machine-identifier, authority, document]
  risks: []
---

# 面向人的内容完整性

编写或修改文档、Issue、PR、Review、状态说明以及其他面向人的内容时，同时保持自然中文表达、精确标识、稳定状态值、正式概念身份与 Consumer 文档生命周期。

## 默认使用自然中文

本 Consumer 的面向人内容默认使用自然、完整、连续的中文。动作、判断、因果和结论应以中文表达，不为了显得专业、方便检索或制造“术语感”机械保留普通英文，也不把中英文并列作为默认模板。

标题、表格标题与列名、列表中的动作说明、流程节点以及其他面向人的结构性标签默认使用中文。结构项本身就是机器标识、稳定状态值、外部正式名称或必须精确对照的正式 identity 时可以保持原样。

Agent 面向用户展示的分析、进度更新、规划、风险、阻塞、验证结果与复核结论遵守同一规则。输入材料、工具输出或旧文档大量使用英文，不构成切换叙述语言的理由。

## 精确标识、稳定状态值和外部正式名称保持原样

Skill / Rule / Method 的精确 id 或调用名、文件名与路径、Branch、Commit SHA、Issue / PR 编号、代码标识符、配置键、数据库字段、API / CLI、命令与参数、协议值、真实日志和错误信息必须保持可精确匹配的原始形式。

跨 Gate / Verification / Review 使用的稳定状态值，例如 `PASS`、`FAIL`、`READY`、`BLOCKED`、`PENDING`、`HOLD`，可以保持原样；不得为了“纯中文”制造第二套长期状态表示。状态值周围的原因、判断和结论仍使用自然中文。

外部产品、框架、协议、标准、规范和官方项目名称在翻译会损害识别或对照能力时保留正式名称。

## 正式概念身份不得被语言重写改变

中文化、术语整理、格式治理或表达优化只能改变面向人的表达，不得改变 Product Goal、Scope、Business Boundary、User-visible Behavior、Architecture Decision、Specification、技术契约，也不得把职责或生命周期不同的 Method stage、Gate、artifact、Skill、Rule、Architecture、Repository Authority、Human Authority 等正式对象合并。

正式概念拥有英文 identity，不等于普通中文叙述默认保留英文名称。当前句子不需要精确识别正式 identity 时，优先使用自然中文或当前稳定中文表达，例如“稳定基线”“当前方法”“规则发现已完成”“人工评审”；只有确需消歧、引用精确 id / 路径或跨文档稳定身份时才保留原文。

同一 Current canonical concept 已形成稳定中文表达时，后续面向人内容应沿用，不自行制造同义中文，也不恢复已经退出 Current model 的历史别名。业务术语仍由真实 Requirement / Domain owner 持有，本 Rule 不建立中央中英文术语表。

## Current Authority 与 Front Matter

新增或实质重写长期维护的 canonical Authority 时，应遵循该资源类型已经采用的结构化 identity / status 约定；不要为了格式统一给历史 Evidence 批量补 metadata，也不要通过 Front Matter 建立第二份 Current State truth。

修改已有 Current Authority 时同时检查：

- semantic owner 是否仍正确；
- 是否残留被后继 Authority 取代的旧语义；
- 是否缓存了不属于该文件职责的 Current Gate / runtime state；
- 本地 locator 是否仍有效；
- 中文主述、结构标签与机器标识是否仍满足本 Rule。

## Historical Evidence 保真

`SUPERSEDED / HISTORICAL_EVIDENCE` 以证据保真优先，不因语言、格式或 metadata 风格统一而破坏性改写历史正文。它们必须退出 ordinary Fresh Context Current Authority；若未来重新晋升为 Current Authority，先完成 semantic reconciliation，再满足当前文档规范。

历史内容的物理归档必须建立在明确 lifecycle 判定上，不按年龄、编号或“看起来旧”机械移动。

## Consumer 本地边界

本 Rule 只约束 `jilinjobs-cms`。如果后续形成跨 Consumer 的可复用证据，只能通过显式 feedback / upgrade lifecycle 反馈 `agentic-dev`，不会自动改变 upstream capability。
