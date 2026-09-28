# 文档与 Authority 导航

## Fresh Context 最小入口

普通 Fresh Context 固定读取：

1. 根 AGENTS.md；
2. 本文件。

然后只按当前责任读取会改变判断的最小 Current Authority。不要把完整文档树、全部 Skills、Roadmap、历史 Evidence 或 GitHub 状态机械预加载。

## Current Authority 导航

| 责任 | 当前 owner / locator |
|---|---|
| 稳定项目范围摘要 | 根 README.md |
| Product / Domain Requirement | docs/requirements/index.md 指向的 canonical owner |
| Feature / surface observable contract | docs/specifications/** |
| CMS / Site / Migration / Renderer 长期架构 | docs/architecture/cms-architecture.md |
| Requirement ownership / lifecycle | docs/architecture/requirement-authority.md |
| 架构决策历史 | docs/architecture/decisions/** |
| 设计权威 | docs/design/** |
| HTTP / frontend / migration / verification 等长期技术契约 | docs/technical/** |
| Durable planning direction | docs/project/project-roadmap.md |
| 稳定演进摘要 | docs/project/project-evolution.md |
| agentic-dev adopted version / provenance | docs/project/agentic-dev.md |
| 正式 active Execution Unit locator | docs/work/current/README.md |
| Consumer-local 横切约束 | docs/governance/constraints.md |
| Git Commit 规范 | docs/governance/git-commit-conventions.md |
| Frontend path-scoped constraints | frontend/AGENTS.md |
| 通用执行 procedure | .agents/skills/**，由宿主原生 discovery 按需选择 |

Requirement Index 只拥有 locator / relation；长期 Product / Domain 事实由它指向的 canonical fact owner 持有。Specification、Architecture、Technical、Design 只拥有各自语义责任，不通过“更新较晚”或“更接近代码”覆盖其他 owner。

## 按任务恢复

### 产品 / 业务变化

从 docs/requirements/index.md 找到直接 owner，再按需读取受影响 Specification、Architecture、Design、Technical 与验证契约。

### 实现 / 修复

先确认当前 observable contract 与必要 Architecture / Technical owner。局部、低风险、可逆变更可以直接实施并按范围验证；只有确需独立恢复、依赖协调或独立验收时才使用正式 Execution Unit。

### 规划

读取 docs/project/project-roadmap.md 和当前目标直接相关的 Authority。Roadmap 只拥有 durable direction，不授予 Execute Authority。

### 正式 Execution Unit

读取 docs/work/README.md 与 docs/work/current/README.md，并重新核验任务相关 Open Branch / PR / Actions。NONE 只表示没有正式 active Unit。

### agentic-dev 使用 / 升级

读取 docs/project/agentic-dev.md。普通执行从 .agents/skills/** 原生发现 Skill；只有需要“下一步如何选择”等方法论导航时才按 adopted exact tag 定向读取 Provider Guide。

本仓库不再复制 Provider Method、Rule、Capability、Release、Gate、Project Knowledge 或 runtime routing engine。

## 来源与证据角色

| 来源 | 作用 |
|---|---|
| Current canonical Authority | 在自身 semantic responsibility 内拥有当前长期真值 |
| ADR | 保存重要决策的背景、候选、权衡与 supersede lineage；当前叠加状态仍由 current Architecture owner 表达 |
| Versioned canonical source | sites/jilinjobs/**、data-migrations/** 等只拥有自己明确声明的版本化结构 / 数据 / provenance 事实 |
| External authoritative source | 对合同、法规或外部系统自身事实提供带版本 / 时间 / scope 的输入 |
| 明确人工决定 | 对明确问题和范围具有授权价值；需要跨 Context 持续成立时必须写回真实 Repository owner |
| GitHub native state | Branch、PR、Issue、Actions、Review 等瞬时事实 |
| Code / Tests / Runtime | 证明当前实现是什么；不能单独反向发明 Requirement |
| Historical / Legacy material | 提供 provenance / traceability，默认退出 ordinary Current Authority |
| Analysis / conversation material | 临时分析，除非显式 promotion 到真实 owner，否则不是长期 Authority |

冲突时先按 semantic owner 与适用范围判断。Current owner 与 Historical material 冲突时，历史只保留 lineage；implementation / verification 与 Current Authority 不一致时，先区分 implementation defect、stale Authority 或 stale verification contract。

## 历史与临时材料

- docs/**/archive/**、docs/work/archive/**：Historical Evidence；
- Git / Issue / PR / Actions：详细过程与原生状态；
- 临时 review draft、comparison、scratchpad、source inventory：默认不进入长期 Current Authority；
- 可由 Current Authority 唯一再生的派生视图不建立长期双向同步副本。

## 文档完整性

Current 文档默认使用自然中文叙述，精确机器标识和外部正式名称保持原样。自动检查由 scripts/verify-docs-governance.mjs 与 .github/workflows/docs-governance.yml 承担。

历史 Evidence 以证据保真优先，不为了语言或格式统一批量重写；重新晋升为 Current 前先完成 semantic reconciliation。
