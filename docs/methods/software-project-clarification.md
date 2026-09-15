---
id: method:software-project-clarification
type: method
status: active
---

# 软件项目澄清方法

## 目标

本 Method 用于普通软件 Consumer 项目的项目级或重大范围前置澄清：当多个当前或预期 Feature 无法安全进入 Specification，因为它们共同依赖的长期 Requirement / Architecture Context 缺失、冲突或需要重建时，建立可持续使用的 Consumer-local Context。

它不替代 `method:ai-development`，也不因项目大、技术复杂、文档多、单 Feature 局部歧义或小范围可逆变更而自动进入。

## 生命周期

```text
Establish Context
→ Requirement Clarification
→ Architecture Clarification? (conditional)
→ Clarification Convergence
→ Clarified Project Context Ready
```

stable phase identities：

- Establish Context → `establish-context`
- Requirement Clarification → `requirement-clarification`
- Architecture Clarification → `architecture-clarification`
- Clarification Convergence → `clarification-convergence`

## Establish Context

先识别本次澄清的产品 / 项目范围与 Evidence / Authority 边界：区分 current、legacy、reference、analysis、conversation、unknown；确认现有 semantic owner 是否存在、是否冲突、是否足够；不把历史材料、聊天、实现便利或派生分析自动提升为 Authority。

退出：Evidence / Authority 边界已足以支持 Requirement Clarification。

## Requirement Clarification

只处理长期 Problem / Behavior / Constraint / Acceptance 与 Domain semantics，不生成具体实现方案。

重点包括：product / domain goal、actor / stakeholder、business object / data semantics、workflow、business rule、state / lifecycle、failure / exception、quality / compliance、project-level acceptance invariant。

未知至少区分 confirmed / derivable fact、gap、conflict、ambiguity、unsupported assumption、external dependency、design item。只有当前 Authority 无法唯一裁决，且不同合理答案会改变产品行为、长期边界或验收时，才升级 Human Authority。

确认的长期事实必须进入真实 Requirement / Domain owner；分析表、comparison matrix、候选清单不是长期 Authority。

## Architecture Clarification

这是条件阶段，不是 Big Design Up Front。只有多个 Feature 在进入可靠 Specification 前共同依赖尚未解决的长期 architecture driver 时进入，例如 shared contract、core data/system boundary、security/integration/deployment topology 或高成本难逆结构决定。

形成 / 更新长期 Architecture Context / State 与 capability boundaries；只有背景、替代关系和主要权衡具有长期历史价值时才形成 ADR。Feature-local、低风险、可逆 HOW 留给 `method:ai-development` 的 Technical Planning / JIT execution。

Architecture 发现新的业务多解或 Requirement conflict 时必须返回 Requirement Clarification，不自行发明产品事实。

## Clarification Convergence

只有同时满足以下关键条件才声明 `Clarified Project Context Ready`：

- 多 Feature 依赖的 product / domain boundary 有明确 Consumer-local owner；
- 会阻止可靠 Specification 的长期 conflict / ambiguity 已关闭；
- 确认事实已进入 Requirement / Domain Authority；
- design choice 未被静默提升为 Requirement；
- 必须提前解决的 architecture driver 已进入同一长期 Architecture owner；
- Feature-local HOW 未被提前吸收；
- semantic owner 缺失 / 冲突已重建；
- 强制独立语义复核条件命中时 review 已通过；
- 剩余 open item 均为 non-blocking 且有 owner；
- Fresh Context Feature Agent 可仅从 Consumer-local Authority 恢复最小 Context。

## Independent semantic review

从 legacy / heterogeneous sources 重建 Requirement baseline、大规模 Authority 重构 / 迁移、多来源冲突合并、批量改变长期业务状态 / 生命周期 / 权限 / 数据语义 /验收不变量时，在 Ready 前必须执行独立语义复核。Review 必须核对 source / decision / resulting Authority，而不只是格式或链接。

## Artifact lifecycle

默认 durable：Requirement Authority、必要的 Domain / Terminology Authority、Architecture Context / State、条件性 ADR、明确 owner 的 non-blocking external/open items。

默认 transitional / disposable：source inventory、ambiguity list、comparison matrix、relationship view、extraction table、review scratchpad。能够从最终 Authority 唯一再生的派生表达不长期保留。

## Return contract

本 Method 完成只返回 `Clarified Project Context Ready`，不等于 Specification / Execution Unit 已创建，也不授予 Execute / Integrate Authority。