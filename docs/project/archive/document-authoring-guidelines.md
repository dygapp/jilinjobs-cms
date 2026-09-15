# 项目文档编写规范

## 1. 文档语言

`jilinjobs-cms` 项目所有面向人的 Current / Partially Current 文档**强制使用中文作为主语言**。

适用范围至少包括：

- Requirement；
- Specification；
- Technical Plan；
- Execution Unit；
- Verification Strategy；
- Project Governance；
- ADR；
- 根级 `AGENTS.md`、`README.md` 与 `docs/README.md`；
- 仍参与 Fresh Context 的其他 Markdown Authority / locator。

具体规则：

1. 文档标题、章节标题、状态说明、背景、需求、规格、技术方案、验证说明、结论等叙述性内容以中文为主；
2. 技术标准名称、代码标识符、类 / 方法 / 字段名、文件路径、命令、API / URL、协议、枚举值、框架名称与固定专有名词可以保留英文；
3. 需要精确对应外部 Method / Skill / Contract 时，首次重要出现可采用“中文（English Term）”；
4. `CURRENT / READY / COMPLETED / SUPERSEDED` 等治理状态可以保留原生标识，但必须用中文正文解释，不得以整段英文状态说明替代中文语义；
5. 代码块、JSON / SQL / YAML 示例、CLI 输出不参与中文比例判断；
6. Current 文档不得存在纯英文主体、英文占主导的长篇叙述或纯英文一级标题；发现后必须在同一治理工作中翻译、收敛或明确降级为历史证据；
7. 中文化不得改变 Product Goal、Scope、Business Boundary、User-visible Behavior、Architecture Decision 或技术契约。

`SUPERSEDED / HISTORICAL_EVIDENCE` 文档以证据保真为优先，不因纯语言原因改写历史正文；但它们必须通过 `docs/README.md` 或 archive 入口明确退出 Current Authority。历史文档重新晋升为 Current Authority 前必须先满足本节语言规范。

自动检查由 `scripts/verify-docs-governance.mjs` 与 `.github/workflows/docs-governance.yml` 执行。

## 2. Markdown 文件头

当前项目不要求历史 Markdown 文件一次性补充 YAML Front Matter。

对于新增的长期维护 Authority 文档，建议根据文档生命周期增加结构化文件头。

推荐适用：

- Requirement；
- Specification；
- Technical Plan；
- Execution Unit；
- ADR；
- Verification Strategy。

不强制适用：

- 临时工作记录；
- Issue 讨论记录；
- 原始 Evidence 输出；
- 简单说明文件。

## 3. 历史文档处理

历史 Evidence 不为了语言或格式统一进行破坏证据价值的大规模改写。

对于仍留在非 archive 目录、但已经完成或被后继 Authority 取代的文件，应优先在 `docs/README.md` 明确分类为 `SUPERSEDED / HISTORICAL_EVIDENCE`，从普通 Fresh Context 中退出；只有路径移动有明确收益时再物理归档。

后续修改已有 Current Authority 文档时，必须同时检查：

- 是否仍存在与后继 Authority / 当前实现冲突的旧语义；
- 是否缓存了不属于该文件职责的 Current Gate；
- 本地文档 locator 是否仍有效；
- 是否满足中文主语言规范。

## 4. 规范演进

本规范是 Consumer-local 文档治理 Authority。

其规则只约束 `jilinjobs-cms` 当前 Repository，不自动提升为 `agentic-dev` 通用方法。若后续形成跨 Consumer 可复用证据，只能按当前 Repository Operation Boundary 通过 Issue / Feedback 反馈到 `agentic-dev`。