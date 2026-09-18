# 架构权威（Architecture Authority）

`docs/architecture/` 保存 Consumer-local 长期 Architecture Context / State、reusable engineering capability architecture 与 ADR。

## 产品架构

- `cms-architecture.md` — `jilinjobs-cms` 当前跨 Feature 的 CMS / Site Definition / Historical Migration / Runtime / Public Renderer、Backend application、Page Content、configuration/resource ownership 与验证组合边界。

普通 Feature 只有在需要恢复多个 Feature 共同依赖的长期边界时才读取该文件；Feature-local、低风险、可逆 HOW 留给 Technical Planning。

## 工程能力架构

以下文件定义本 Consumer 本地采用的可复用工程能力结构，不承担产品 CMS Architecture：

- `engineering-capability.md`
- `consumer.md`
- `method.md`
- `requirement-authority.md` — Requirement Human Navigation / Authority Index / Fact Authority / Analysis Workspace 的 semantic ownership 与 lifecycle；
- `project-knowledge.md`
- `rule.md`
- `rule-discovery.md`
- `skill.md`

## 架构决策记录

`decisions/` 保存值得长期追溯的决策背景、候选方案、主要权衡与 supersede 关系。

ADR 不承担当前 Architecture State 的全量恢复；`cms-architecture.md` 描述当前叠加后的状态。只有当核心 decision / trade-off 真正改变时才新增或 supersede ADR，不为普通实现调整机械创建 ADR。

## 边界

Architecture 文档不得：

- 成为第二份 Product / Domain Requirement；
- 保存当前 Ready Execution Unit、PR / Actions 等高频状态；
- 长期复制 migration file list、resource counts 或可从代码直接恢复的 implementation inventory；
- 把 Feature-local HOW 提前上升为项目级架构；
- 通过实现便利发明新的产品事实。
