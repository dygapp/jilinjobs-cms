# 项目文档编写规范

## 1. 文档语言

`jilinjobs-cms` 项目长期维护文档默认使用中文。

适用范围：

- Requirement；
- Specification；
- Technical Plan；
- Execution Unit；
- Verification Strategy；
- Project Governance 文档。

技术标准名称、代码标识、文件路径、API 名称、框架名称等可以保留英文。

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

已有文档不进行无价值的大规模格式迁移。

后续修改已有 Authority 文档时，根据实际修改范围逐步补充结构化信息。

## 4. 规范演进

本规范当前属于 Consumer-local Practice。

经过多个 Execution Unit 验证后，再评估是否反馈到 `agentic-dev` 作为通用方法演进候选。