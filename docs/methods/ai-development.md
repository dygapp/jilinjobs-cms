---
id: method:ai-development
type: method
status: active
---

# AI Agent 驱动 Feature 开发方法

## 目标与进入条件

本 Method 面向具体 Feature / change。进入前提是 Consumer 已存在足以判断 Goal、Scope、Observable Behavior 与 Acceptance 的 Requirement Baseline，以及当前 Feature 真正需要的最小 Architecture Context。

如果项目尚无可靠 Requirement Baseline，或多个当前 / 预期 Feature 共同被系统性 Requirement gap / conflict / ownership failure 阻塞，不在当前 Feature 中局部创造长期事实；返回真实 Requirement owner，必要时由 Project Capability Profile 选择 `method:requirement-baseline-establishment`。

如果 Requirement Baseline 已足够，但多个当前 / 预期 Feature 共同依赖尚未解决、长期、高成本难逆并阻塞可靠开发的 architecture driver，则返回真实 Architecture owner，必要时由 local selector 进入 `method:architecture-clarification`。

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

Integration 不是本 Method 的通用阶段。merge、release、deploy 与其他外部副作用由 Repository / 人工权威 决定。

## 意图澄清（Clarify Intent）

只解决会实质改变 Goal、Scope、User-visible Behavior、Business Boundary、Acceptance 或重大非功能义务的当前 Feature 歧义。优先从当前 Requirement / Domain / Architecture Authority 解析；低影响、可逆实现选择不升级到产品意图层。

局部 Feature 缺口返回当前 Feature owner；系统性 Requirement Baseline gap 返回 Requirement owner / `method:requirement-baseline-establishment`；systemic architecture driver 返回 Architecture owner / `method:architecture-clarification`。不要在当前 Specification 中创建新的项目级 Requirement 或 Architecture 事实。

退出：不存在会改变目标或验收结果的关键未决问题，且长期 Context 足够进入 Specification。

对应 Skill：`clarify-intent`。

## 功能规格（Specification）

形成当前 Feature / change 的 WHAT / WHY Authority，至少覆盖 Goal、In/Out Scope、Observable Behavior、Business Rules、Boundary / Failure Behavior、Acceptance Criteria 与必要非功能约束。

Specification 不复制完整 project-level Requirement / Domain baseline。新确认且具有跨 Feature 长期价值的业务事实必须提升到真实 Requirement / Domain owner。

退出：Fresh Context Agent 读取 Specification + 最小长期 Context 即可判断做什么、不做什么、什么算完成。

对应 Skill：`specify`。

## 技术规划（Technical Planning）

仅在 Specification 无法直接、安全映射到当前系统时进入，例如跨模块、新数据模型、外部集成、迁移、共享契约、部署拓扑或重大架构权衡。

Technical Plan 只保存跨 Execution Unit 仍有协调价值的 HOW；精确文件 / 命令 / 编辑顺序属于 JIT Execution Plan。Feature-local 可逆 HOW 留在本阶段；只有多个 Feature 共同依赖且必须提前解决的长期 structural driver 才升级到 `method:architecture-clarification`，并更新同一个长期 Architecture owner。

对应 Skill：`technical-plan`。

## 切片与就绪（Slice & Ready）

`slice-work` 把 Ready Specification 与必要 Technical Plan 切为 context-fit Candidate Execution Units；`readiness-check` 在 Execute 前执行只读门禁。只有 Readiness PASS 才授予该 Unit 的 执行授权；不自动授权后续 Unit 或 Integration。

## 执行（Execute）

每次只执行一个 Ready Execution Unit。重新读取当前 Unit、直接 Authority 与代码事实，形成 JIT plan；在 直接责任 首个副作用前执行 Consumer-local Rule Discovery，并按需调用 Skill。

意外失败进入 `systematic-debug`。完成声明必须由与 Completion Conditions 匹配的当前 Evidence 支持。

对应 Skill：`execute-unit`。

## 收敛（Converge）

对当前 Authority、最终实现与当前 Evidence 做整体收敛，区分 Verification、Review 与 Convergence。发现缺口时返回拥有责任的上游层：局部 Feature 缺口返回当前 Feature owner；系统性 Requirement Baseline gap 返回 Requirement owner / `method:requirement-baseline-establishment`；systemic architecture gap 返回 Architecture owner / `method:architecture-clarification`。

退出：无已知 Blocking 缺口，Authority、实现与当前 Evidence 一致，可声明 `Ready to Integrate`。

对应 Skill：`converge`；高影响变更的独立 review 由 Repository policy / Rule 触发 `review-change`。

## Fresh Context 与 Artifact lifecycle

普通上下文只加载 仓库权威、当前工作对象、Requirement locator / 当前 Feature 直接相关 Requirement owner、必要 Specification / Technical / Architecture / Domain / Project Authority、Rule Discovery 返回的候选、当前需要的 Skill，以及相关 code / tests / Evidence。

长期知识只进入真实 语义所有者；会话推理、source comparison、JIT plan 与阶段流水账默认不持久化。Requirement Authority 的 ownership / locator contract 由 `architecture:requirement-authority` 持有。

## 人工升级

改变产品意图 / 范围、产生实质不同用户行为、Authority 冲突、重大难逆架构方向、安全 / 隐私 / 数据风险、超出授权的共享 / 外部副作用，以及 Repository policy 保留给人工的 merge / release / deploy 等必须升级 人工权威。
