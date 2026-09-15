---
id: method:ai-development
type: method
status: active
---

# AI Agent 驱动 Feature 开发方法

## 目标与进入条件

本 Method 面向具体 Feature / change。进入前提是 Consumer 已存在足以判断 Goal、Scope、Observable Behavior 与 Acceptance 的最小长期 Requirement / Domain / Architecture Context。

如果多个当前或预期 Feature 共同依赖的长期 Requirement / Architecture Context 缺失、冲突或需要重建，不在当前 Feature 中局部创造长期事实；返回长期 owner，必要时由 Project Capability Profile 选择 `method:software-project-clarification`。

## 生命周期

```text
Clarify Intent
→ Specification
→ Technical Planning? (conditional)
→ Slice & Ready
→ Execute
→ Converge
→ Ready to Integrate
→ Repository / Human Authority
```

正式阶段六个：

- Clarify Intent → `clarify-intent`
- Specification → `specification`
- Technical Planning → `technical-planning`
- Slice & Ready → `slice-ready`
- Execute → `execute`
- Converge → `converge`

Integration 不是本 Method 的通用阶段。merge、release、deploy 与其他外部副作用由 Repository / Human Authority 决定。

## Clarify Intent

只解决会实质改变 Goal、Scope、User-visible Behavior、Business Boundary、Acceptance 或重大非功能义务的歧义。优先从当前 Requirement / Domain / Architecture Authority 解析；低影响、可逆实现选择不升级到产品意图层。

退出：不存在会改变目标或验收结果的关键未决问题，且长期 Context 足够进入 Specification。

对应 Skill：`clarify-intent`。

## Specification

形成当前 Feature / change 的 WHAT / WHY Authority，至少覆盖 Goal、In/Out Scope、Observable Behavior、Business Rules、Boundary / Failure Behavior、Acceptance Criteria 与必要非功能约束。

Specification 不复制完整 project-level Requirement / Domain baseline。新确认的跨 Feature 长期事实必须提升到真实长期 owner。

退出：Fresh Context Agent 读取 Specification + 最小长期 Context 即可判断做什么、不做什么、什么算完成。

对应 Skill：`specify`。

## Technical Planning

仅在 Specification 无法直接、安全映射到当前系统时进入，例如跨模块、新数据模型、外部集成、迁移、共享契约、部署拓扑或重大架构权衡。

Technical Plan 只保存跨 Execution Unit 仍有协调价值的 HOW；精确文件 / 命令 / 编辑顺序属于 JIT Execution Plan。跨多个 Feature 的长期 architecture driver 返回长期 Architecture owner / Project Clarification；Feature-local 可逆 HOW 留在本阶段。

对应 Skill：`technical-plan`。

## Slice & Ready

`slice-work` 把 Ready Specification 与必要 Technical Plan 切为 context-fit Candidate Execution Units；`readiness-check` 在 Execute 前执行只读门禁。只有 Readiness PASS 才授予该 Unit 的 Execute Authority；不自动授权后续 Unit 或 Integration。

## Execute

每次只执行一个 Ready Execution Unit。重新读取当前 Unit、直接 Authority 与代码事实，形成 JIT plan；在 direct responsibility 首个副作用前执行 Consumer-local Rule Discovery，并按需调用 Skill。

意外失败进入 `systematic-debug`。完成声明必须由与 Completion Conditions 匹配的当前 Evidence 支持。

对应 Skill：`execute-unit`。

## Converge

对当前 Authority、最终实现与当前 Evidence 做整体收敛，区分 Verification、Review 与 Convergence。局部 Feature 缺口返回当前 Feature owner；系统性 Requirement / Architecture gap 返回长期 owner，不在 Converge 中静默重建项目级事实。

退出：无已知 Blocking 缺口，Authority、实现与当前 Evidence 一致，可声明 `Ready to Integrate`。

对应 Skill：`converge`；高影响变更的独立 review 由 Repository policy / Rule 触发 `review-change`。

## Fresh Context 与 Artifact lifecycle

普通上下文只加载 Repository Authority、当前工作对象、最小必要 Requirement / Domain / Architecture、当前 Method、必要 Skill、Rule Discovery 返回的候选及相关 code / tests / Evidence。

长期知识只进入真实 semantic owner；会话推理、source comparison、JIT plan 与阶段流水账默认不持久化。

## Human escalation

改变产品意图 / 范围、产生实质不同用户行为、Authority 冲突、重大难逆架构方向、安全 / 隐私 / 数据风险、超出授权的共享 / 外部副作用，以及 Repository policy 保留给人工的 merge / release / deploy 等必须升级 Human Authority。