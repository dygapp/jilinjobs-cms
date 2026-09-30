# 架构权威

docs/architecture/ 只保存 jilinjobs-cms 自己长期需要的 Architecture Context / State 与 ADR，不承载 Provider Method / Rule / Capability runtime。

## 当前架构 owner

- cms-architecture.md：CMS / Site Definition / Historical Migration / Runtime / Public Renderer、Backend application、Page Content 与 configuration / resource ownership 的跨 Feature 长期边界；
- requirement-authority.md：本项目长期 Requirement 的 semantic ownership、Authority Index / fact owner / analysis workspace 与 lifecycle；
- decisions/**：值得长期追溯的决策背景、候选方案、主要权衡与 supersede relation。

普通 Feature 只在需要恢复多个 Feature 共同依赖的长期边界时读取相应 Architecture；局部、低风险、可逆 HOW 不为了形式完整提升成项目级 Architecture。

## 边界

Architecture 文档不得：

- 成为第二份 Product / Domain Requirement；
- 保存具体 Execution Unit lifecycle、PR / Actions 等高频状态；
- 长期复制 migration file list、resource counts 或可从 implementation 唯一恢复的 inventory；
- 通过技术便利发明新的产品事实；
- 复制 agentic-dev Provider 的 Method / Rule / Capability architecture。
