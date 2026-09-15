---
title: Review Feedback Cycle 规范
status: Consumer-local 已采用
scope: Consumer-local Method
evidence_lifecycle: docs/project/method-validation-evidence.md
---

# Review Feedback Cycle 规范

## 1. 目的

当前项目在 Execution Unit 开发过程中，人工验证反馈是质量保证的重要环节。

为了避免每次人工反馈都重新执行完整 EU 验证流程，导致反馈周期过长，本项目建立分层 Review Feedback Cycle。

目标：

- 缩短人工反馈后的修复验证时间；
- 保证必要验证不会被跳过；
- 区分局部修复、功能验证和最终集成验证；
- 在后续实践中继续校正 Verification tier 与实际 Workflow trigger topology 的对应关系。

本规范已完成首轮多 Execution Unit 的 Consumer-local 实践验证，继续作为本项目采用的方法规则。若后续需要针对其中某个假设开展新的正式观察期，应按 `docs/project/method-validation-evidence.md` 创建**新的临时 Evidence Issue**；不得把已经完成收敛的历史 Issue 继续作为永久证据渠道。

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

不要求仅因 L0 变化主动增加完整 CI 或 Review Environment；如果 Repository 当前 Workflow topology 仍自动触发这些验证，其结果可以作为额外 Current Evidence，但不得反向把自动触发事实解释为所有 L0 变化的方法要求。

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

Review Environment 不作为每次修改后的方法级默认验证要求。

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

Repository 当前实际 Workflow 是否自动启动 Review Environment，属于 `docs/technical/verification-strategy.md` 与 GitHub Actions topology 的治理事实；不得仅根据本节自行跳过当前稳定 Authority 要求的验证。

## 6. 验证升级原则

默认采用最低必要验证等级，而不是每次修改主动执行最高等级验证。

|变化|最低等级|
|-|-|
|文档|L0|
|样式/资源|L1|
|页面行为|L1/L2|
|API|L2|
|数据库|L2/L3|
|架构变化|L3|
|最终验收|L4|

最低等级只描述方法上的最小 Evidence Claim；如果当前稳定 Verification Authority、PR Policy 或 Workflow topology 对具体变化要求更高等级，仍应遵守更高优先级 Repository Authority。后续若要降低稳定自动触发成本，应先完成 Authority 与 Workflow 的显式对账，而不是只修改本表。

## 7. 与 Execution Unit 的关系

Review Feedback Cycle 不改变 EU Acceptance。

原则：

- EU Acceptance 必须完成与当前 Verification Strategy 匹配的最终验证；
- Review Feedback 只降低修复反馈循环成本；
- 不允许通过降低验证等级绕过最终验收；
- Readiness、Execute、Integration 与 Post-Integration 的状态语义继续服从 `docs/project/development-method.md`。

## 8. 后续方法实验

本规范不再绑定任何固定 GitHub Issue 作为长期证据渠道。

如果未来需要继续验证例如：

- L0～L4 与实际 CI / Review Environment trigger topology 的进一步映射；
- Fast Review / Full Review 的稳定自动化实现；
- 某类局部修复是否存在漏检反例；
- 新的 Review 成本优化机制；

应按 `docs/project/method-validation-evidence.md`：

1. 创建本轮独立的临时 Evidence Issue；
2. 明确假设、观察范围、Promotion Criteria 与 Close Criteria；
3. 在多个真实工作样本中积累正向证据与反例；
4. 完成 Consumer-local 固化 / `agentic-dev` 反馈 / Reject 或 Supersede；
5. 写 Final Promotion Record 后关闭该 Issue。

历史实验 Issue 只作为 provenance 保留，不继续承担新的活跃观察职责。
