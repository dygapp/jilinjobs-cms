---
id: method:requirement-baseline-establishment
type: method
status: active
---

# 需求基线建立方法

## 目标与进入条件

本 Method 用于把普通软件 Consumer 中原始、碎片化、不同可信度且可能冲突的 Project Inputs 收敛为可持续维护、具有明确 semantic ownership、可由 Fresh Context Agent 直接消费的 Requirement Baseline。

只有以下 work kind 命中时进入：新项目尚无可靠 Requirement Baseline；legacy modernization / rewrite 需要重建基线；现有长期 Requirement Authority 已碎片化、冲突、重复或 owner 不清；多个当前或预期 Feature 被同一系统性 Requirement gap / conflict / ambiguity 阻塞。

单 Feature 局部歧义、普通 bugfix、小范围 change、已有稳定基线的单事实增量更新，以及 Feature-local HOW 不进入完整 Method。

## 与其他 Method 的关系

```text
Raw / fragmented Requirement inputs
        ↓
Requirement Baseline Establishment
        ↓
Requirement Baseline Ready
        │
        ├─ Architecture Context sufficient
        │       ↓
        │   AI Development
        │
        └─ systemic architecture blocker
                ↓
        Architecture Clarification
                ↓
        Architecture Context Ready
                ↓
        AI Development
```

本 Method 不负责 Feature Specification，也不负责长期 Architecture decision；完成只返回 `Requirement Baseline Ready`。

## 生命周期

```text
Establish Sources & Authority
→ Extract Requirement Facts
→ Structure Requirement Authority
→ Resolve Requirement Unknowns
→ Review Requirement Baseline
→ Requirement Convergence
→ Requirement Baseline Ready
```

stable phase identities：

- Establish Sources & Authority → `establish-requirement-sources`
- Extract Requirement Facts → `extract-requirements`
- Structure Requirement Authority → `structure-requirements`
- Resolve Requirement Unknowns → `resolve-requirements`
- Review Requirement Baseline → `review-requirements`
- Requirement Convergence → `requirement-convergence`

## Establish Sources & Authority

先识别本次基线建立允许使用的 current authoritative input、legacy input、reference input、external dependency、analysis material、conversation / human decision 与 unknown，并确认当前长期 owner 是否存在、是否冲突、是否足够。

材料存在、旧系统行为、历史文档或当前实现都不能直接等同于 Product Requirement。source inventory 只在当前澄清 / review / Fresh Context 恢复确有需要时保留，默认属于 transitional artifact。

退出：后续事实抽取能够判断来源可信度、时间边界与当前 Authority owner。

## Extract Requirement Facts

优先从业务语义提取，而不是从 UI、数据库或代码结构出发。至少检查：business object、business activity / workflow、business rule、state / lifecycle、actor / responsibility、external dependency / input / output，同时识别 goal / scope / out-of-scope、data semantics、failure / exception、quality / compliance、acceptance intent。

页面、菜单、API shape、数据库表、类 / 包、缓存等默认不是 Requirement boundary，除非它们本身属于外部合同或 authoritative constraint。

退出：主要长期业务事实已经能够被识别并进入 ownership / capability 分析。

## Structure Requirement Authority

按 `architecture:requirement-authority` 把事实组织到唯一 durable owner，而不是建立第二套持久业务模型。至少明确：project / system scope、Requirement Capability / business owner、跨 Capability 事实、NFR、Requirement Authority Index / locator、analysis 与 durable Authority 边界。

Capability 边界优先依据核心业务对象、主要业务结果、生命周期、Actor / responsibility、关键规则和独立验收意义；不得按页面、菜单、微服务、数据库表或代码模块机械拆分。

退出：核心事实有唯一 owner，Requirement locator 能确定性找到主要 Authority，不存在明显平行事实源。

## Resolve Requirement Unknowns

未决项至少分类为：Confirmed、Deterministically Derived、Authoritative Default、Provisional Minimal Default、Material Ambiguity、Conflict、External Dependency、Design Item、Unsupported Assumption。

按以下顺序处理：

```text
Derive → Default → Ask → Review
```

1. Authority 已回答 → 直接使用；
2. confirmed facts 可唯一推导 → 自动推导；
3. Requirement Authority 已有适用的 Authoritative Default → 按 scope 使用；
4. Design Item → 推迟到正确责任层；
5. 缺少 Evidence 支持额外业务机制 → 可用 Provisional Minimal Default 形成可 Review 草稿；
6. 只有真实 Material Ambiguity / Conflict 会产生实质不同产品结果并阻塞当前基线时，才 Ask Human；
7. Capability 完成后整体 Review 自动推导与 provisional/default 结果。

Provisional Minimal Default 在 Review 前不是 durable Requirement，不得向其他 Capability 传播为已确认事实。Human conversation 只是输入渠道，长期决定必须 promote 到真实 Requirement owner。

## Review Requirement Baseline

每个主要 Requirement Capability 达到可读状态后执行整体 Human Review：确认范围、推导、defaults、遗漏规则、额外机制与 remaining ambiguity。

以下情况在 Baseline Ready 前必须独立语义复核：legacy / heterogeneous sources 重建基线；多来源冲突合并；大规模 Authority restructuring；批量改变长期状态、权限、数据语义或 acceptance invariant。Review 必须对 source / decision / resulting Authority 做语义核对。

## Requirement Convergence

只有同时满足以下条件才能声明 `Requirement Baseline Ready`：

- Product / Domain goal、scope、主要 actor / business object / lifecycle / rules / data semantics / failure / acceptance 已有 durable owner；
- 主要长期事实没有 competing current owner；
- Human Navigation、Authority locator、Fact owner、Analysis workspace 的责任没有互相复制；
- blocking ambiguity / conflict 已关闭；
- Design Item 未伪装为 Requirement；
- provisional default 已 review、promote、修正或删除；
- 必要 independent semantic review 已通过；
- Fresh Context Agent 可以从有限 Consumer-local Authority 入口恢复基线。

如果仍存在阻塞多个 Feature 的 systemic architecture driver，Requirement Baseline 仍可完成，但下一 work kind 由 local selector 转入 `method:architecture-clarification`。

## Return contract

完成后只返回：

```text
Requirement Baseline Ready
```

它不等于 Architecture 已完成、Specification / Technical Plan / Execution Unit 已创建，也不授予 Execute / Integrate Authority。

## Artifact lifecycle

Durable：Requirement Fact Authority、必要 Human Navigation / Authority Index、长期 terminology / domain owner、明确 owner 的 non-blocking external item。

Transitional / disposable：source inventory、extraction table、ambiguity list、comparison matrix、relationship view、review scratchpad。可以从最终 Authority 唯一再生的派生表达不长期保留。
