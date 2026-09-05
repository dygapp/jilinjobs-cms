---
title: Review Feedback Cycle 规范
status: 试行
scope: Consumer-local Practice
evidence: GitHub Issue #62
---

# Review Feedback Cycle 规范

## 1. 目的

当前项目在 Execution Unit 开发过程中，人工验证反馈是质量保证的重要环节。

为了避免每次人工反馈都重新执行完整 EU 验证流程，导致反馈周期过长，本项目建立分层 Review Feedback Cycle。

目标：

- 缩短人工反馈后的修复验证时间；
- 保证必要验证不会被跳过；
- 区分局部修复、功能验证和最终集成验证；
- 为后续是否沉淀到通用工程方法提供实践依据。

本规范属于 Consumer-local Practice，在经过多个 EU 验证前，不作为 agentic-dev 通用方法。长期实践证据统一追加到 GitHub Issue #62，不依赖聊天记录或会话记忆。

## 2. Review 生命周期

```text
Implementation Complete
        |
        v
AI Verification
        |
        v
Human Review
        |
        v
Review Feedback Batch
        |
        v
Targeted Fix
        |
        v
Targeted Verification
        |
        v
Human Re-review
        |
        v
Ready for Integration
```

## 3. Review Feedback Batch

人工验证过程中发现的问题，不默认按单问题立即进入完整验证循环。

除高优先级阻塞问题外，应优先形成 Review Feedback Batch。

AI 根据 Batch 统一分析：

- 问题分类；
- 修改范围；
- 验证等级；
- 是否影响当前 EU Acceptance。

同一 Batch 中 Authority 已明确的问题应连续修复和定向验证，不因单个 Finding 完成机械停止等待“继续”；沟通边界遵循 `docs/project/execution-continuity-guidelines.md`。

## 4. 验证等级

### L0 - 快速验证

适用：

- 文档修改；
- 文案调整；
- Markdown；
- 配置说明；
- 非功能性内容。

验证：

- 文件检查；
- 格式检查；
- 必要静态检查。

不执行完整 CI 或 Review Environment。

### L1 - 定向验证

适用：

- 单页面调整；
- CSS；
- 图片资源；
- 单接口修改；
- 单模块行为调整。

验证：

- 相关测试；
- 局部构建；
- AI 页面检查。

必要时使用 Fast Review Environment。

### L2 - EU Slice Verification

适用：

- Execution Unit 内核心功能变化；
- 数据结构变化；
- 业务逻辑变化。

执行：

- 相关 workflow；
- 定向 integration test；
- acceptance evidence。

### L3 - Full EU Verification

适用：

- EU 完成；
- PR Ready for Review 前；
- 重大边界变化。

执行：

- 全量 CI；
- EU Verification Strategy；
- Migration Verification；
- Acceptance Evidence。

### L4 - Human Integration Review

适用：

- 用户可见行为；
- 页面视觉；
- 完整业务流程。

执行：

- Review Environment；
- 人工验证。

## 5. Review Environment 使用规则

Review Environment 不作为每次修改后的默认验证方式。

### Fast Review

适用：

- 前端静态资源；
- 样式；
- 图片；
- 页面布局。

目标：分钟级刷新。

### Full Review

适用：

- 后端变化；
- 数据库变化；
- 配置变化；
- 部署变化。

执行完整部署流程。

## 6. 验证升级原则

默认采用最低必要验证等级，而不是每次修改执行最高等级验证。

|变化|最低等级|
|-|-|
|文档|L0|
|样式/资源|L1|
|页面行为|L1/L2|
|API|L2|
|数据库|L2/L3|
|架构变化|L3|
|最终验收|L4|

## 7. 与 Execution Unit 的关系

Review Feedback Cycle 不改变 EU Acceptance。

原则：

- EU Acceptance 必须完成完整 Verification Strategy；
- Review Feedback 只降低修复反馈循环成本；
- 不允许通过降低验证等级绕过最终验收。

## 8. 后续评估

该规范首先作为 Consumer-local Practice。

经过多个 EU 验证后，根据实际效果评估：

- 是否需要调整；
- 是否具有跨项目复用价值；
- 是否提交到 agentic-dev 作为方法演进候选。

所有试行证据、反例与 Workflow 成本观察统一持久化到 GitHub Issue #62。在验证完成前，不修改 agentic-dev。