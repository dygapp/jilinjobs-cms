---
title: Execution Continuity 规范
status: 试行
scope: Consumer-local Practice
---

# Execution Continuity 规范

## 1. 目的

本规范用于解决长链计划任务在执行过程中被机械拆分为大量细碎人工交互单元的问题。

项目允许 AI 为了控制复杂度、上下文和验证范围，在一个 Execution Unit、Review Feedback Batch 或其他已授权工作内部划分 Slice、JIT Plan、检查点和局部修复步骤；但这些内部执行单元默认不是人工沟通边界，也不应因为单个内部步骤完成就停止执行或输出一份完整结论报告。

目标：

- 保持长链任务的连续推进；
- 降低项目负责人处理过程性信息的负担；
- 避免“任务执行更快，但人工阅读和继续指令更多”的伪效率；
- 让人工主要参与 Product / Scope / Architecture / Security 等高影响决策、Human Review 和最终集成，而不是充当 AI 工作流调度员；
- 与 `docs/project/review-feedback-cycle.md` 配合，在不降低验证质量的前提下同时缩短执行等待时间和人工沟通时间。

本规范当前属于 Consumer-local Practice，在经过多个 Execution Unit 验证前，不作为 `agentic-dev` 通用方法。

## 2. 核心原则

### 2.1 内部 Slice 不等于人工任务

Execution Unit 可以内部拆分为多个 Slice，例如：

```text
Execution Unit
  ├─ Authority / State Recovery
  ├─ Implementation Slice A
  ├─ Implementation Slice B
  ├─ Targeted Verification
  ├─ Convergence
  └─ Integration Readiness
```

这些 Slice 用于 AI 的规划、范围控制和验证，不默认形成如下人工交互：

```text
完成 Slice A → 输出完整报告 → 等待“继续”
完成 Slice B → 输出完整报告 → 等待“继续”
完成验证     → 输出完整报告 → 等待“继续”
```

默认模式应是：

```text
获得授权目标
   ↓
连续分析 / 实施 / 验证 / 修复
   ↓
自然收口点或真正人工决策点
   ↓
一次性提供必要结论
```

### 2.2 不因局部完成而停止

以下情况本身不是停止条件：

- 普通文件读取完成；
- 一个内部 Slice 完成；
- 一个 Commit 完成；
- 一次可自行处理的测试失败；
- 一次可逆的小范围实现选择完成；
- 一个 Review Finding 修复完成；
- 一次 GitHub Actions 正常重试完成；
- 已获得中间证据但当前工作尚未达到 Readiness / Acceptance。

如果 Authority 已足够，应继续推进后续相关步骤，而不是请求人工发送“继续”。

### 2.3 只在有意义的边界形成完整汇报

完整阶段报告默认只在以下边界产生：

1. Execution Unit / Review Batch 已完成当前授权范围并达到明确收口状态；
2. 已达到 Ready for Review / Ready to Integrate 等需要人工接手的状态；
3. 出现无法从现有 Authority 解决的 Product Goal / Scope / User-visible Behavior / Major Architecture / Security 等高影响决策；
4. 缺失权限、外部凭据、外部事实或必须由人工提供的信息；
5. 用户明确要求阶段总结或检查点报告。

## 3. 执行沟通等级

为减少过程性噪音，项目采用以下沟通等级。

### L0 — 内部执行

默认等级。

包括：

- 读取文件和 Repository 状态；
- JIT Plan；
- 代码或文档修改；
- 普通测试；
- 可自行判断的修复；
- 常规 GitHub Actions 重试；
- 内部 Slice 切换。

不因为这些动作单独输出完整结论。

### L1 — 简短进度状态

仅当任务持续时间较长、状态发生重要变化或需要让人工知道仍在持续推进时使用。

内容应压缩为：

```text
已完成：关键阶段
进行中：当前阶段
阻塞：无 / 简述
```

L1 不是阶段总结，不重复文件清单、Commit 清单、完整测试结果或已经汇报过的背景。

### L2 — 人工决策 / 阻塞

只有真正需要人工输入时使用。

必须说明：

- 已确认事实；
- 无法自行继续的原因；
- 对 Scope / Behavior / Architecture / Security / 权限的影响；
- 最小必要决策问题。

不得把普通技术选择、可逆实现判断或正常测试失败升级为 L2。

### L3 — 收口报告

在当前授权工作达到自然收口点时使用。

重点只包含：

- 最终完成了什么；
- 当前验证 / Acceptance 状态；
- 尚存风险或未完成项；
- 是否需要人工 Review / Merge / Product Decision；
- 下一步的唯一主要动作。

不把执行过程中每个内部 Slice 的日志重新展开为长篇流水账。

## 4. 长链任务的持续执行规则

一个已经通过 Readiness 并被授权执行的 Execution Unit，默认采用连续执行：

```text
Fresh-context Recovery
        ↓
Implementation
        ↓
Targeted Verification
        ↓
Defect Fix / Re-verify
        ↓
Converge
        ↓
Final Verification
        ↓
Authority / Roadmap Closure
        ↓
Ready for Human / Integration
```

在该链路中，只要：

- 当前 Authority 足够；
- 修改仍处于授权 Scope；
- 风险属于可逆、常规工程判断；
- 所需权限存在；

就应持续推进。

不得因为方法上存在 Slice、阶段名、Commit、Workflow 或子检查点，就把长链任务变成需要人工逐步调度的小任务序列。

## 5. 与 Review Feedback Cycle 的配合

Human Review 发现的问题优先形成 Review Feedback Batch。

Batch 内：

1. 统一分类和判断影响范围；
2. 按 `review-feedback-cycle.md` 选择最低必要验证等级；
3. AI 连续修复同一批次中 Authority 已明确的问题；
4. 每个 Finding 的 Targeted Verification 不形成单独人工交互；
5. Batch 达到可复核状态后，再统一请求 Human Re-review。

因此目标流程是：

```text
Human Review
   ↓
Review Feedback Batch
   ↓
连续修复 + 分层验证
   ↓
一次 Human Re-review
```

而不是：

```text
Finding 1 → 修复 → 完整汇报 → 人工继续
Finding 2 → 修复 → 完整汇报 → 人工继续
Finding 3 → 修复 → 完整汇报 → 人工继续
```

## 6. 与 Fresh Context 的关系

Fresh Context 用于控制知识边界和恢复事实来源，不等于缩短单次执行链。

规则：

- 新 Execution Unit 或明确需要隔离的重大工作可以使用 Fresh Context；
- 同一 Execution Unit / PR / Review Batch 内，不因内部 Slice 完成机械切换 Fresh Context；
- 如果必须切换 Context，应把持续执行所需 Authority、当前状态和未完成工作固化到 Repository，而不是要求人工重新组织大量交接信息；
- Fresh Context 恢复完成后，应重新进入连续执行，不把恢复步骤本身作为独立人工任务。

## 7. 与验证和安全边界的关系

Execution Continuity 只减少无必要停顿和汇报，不降低验证要求，也不覆盖 Repository Authority。

以下规则继续有效：

- Acceptance 必须具有与声明匹配的 Current Evidence；
- Feature PR 不得在没有项目负责人明确授权时自行合并；
- 高影响 Product / Scope / Architecture / Security 决策必须人工介入；
- 不得为了连续执行扩大当前 EU / Review Batch Scope；
- 无法自行解决的权限或外部信息缺口必须显式报告。

## 8. 人工信息负担控制

最终输出优先回答项目负责人真正需要知道的问题，而不是证明 AI 做过多少步骤。

默认不重复：

- 已在前文确认且未变化的 Repository 背景；
- 每个读取文件的结果；
- 每个 Commit 的逐项说明；
- 每次成功测试的完整日志；
- 每个内部 Slice 的独立总结。

如果大量细节已经固化到 PR、Issue、Roadmap、Execution Unit 或 Verification Evidence，应以 Repository 为持久载体，人工沟通只提供必要摘要。

## 9. 试行评估

后续至少通过多个不同类型的 Execution Unit / Review Batch 观察：

- 单个 EU 的人工“继续”指令次数是否下降；
- 人工需要阅读的阶段报告数量和长度是否下降；
- 从 Finding 到可复核状态的总时间是否下降；
- 是否出现因减少过程汇报而导致的 Scope 漂移、验证遗漏或决策越权；
- Fresh Context / Repository Authority 是否仍能保证可恢复性和审计性。

只有在实践证明连续执行规则具有跨项目稳定价值后，再考虑向 `agentic-dev` 提交方法演进证据。