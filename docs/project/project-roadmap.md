---
id: project:roadmap
type: project
status: active
---

# 项目路线图（Project Roadmap）

## 角色

本 Roadmap 只维护 `jilinjobs-cms` 的**当前治理 / 产品演进方向、durable planning boundary 与 deferred direction**。它不拥有产品 Requirement、Domain、Architecture 正文，不保存已完成 Execution Unit 的实施流水，也不缓存 `Current Ready Execution Unit`、Readiness、exact Head、Actions 结果或可由其他 canonical owner 唯一恢复的数据 inventory。

Current Execution Lifecycle 统一从 `docs/work/current/README.md` 恢复；已完成里程碑的稳定摘要由 `docs/project/project-evolution.md` 持有；具体历史 Evidence 留在 Git / Issue / PR / Actions、`docs/work/archive/` 与各类 archive。

Roadmap 顺序、Issue 编号或候选名称均不授予 Execute Authority。Feature 只有经过当前 Method 的 Specification / conditional Technical Planning / Slice & Ready / Readiness Gate 后才能进入 Execute。

## 当前治理里程碑

### Issue #155 — Full Documentation Authority Rebuild & Regenerability Validation

状态：**active governance / validation umbrella；不授予产品 Execute Authority。**

本里程碑验证当前 Consumer 文档 Authority 是否能够在保持 single semantic ownership 与可恢复 traceability 的同时，支撑 Behavioral Regenerability 与 Technology Substitutability。完整 G0～G7 lifecycle、Gate 结果和 Evidence locator 由 Issue #155 协调；当前执行状态只从 `docs/work/current/README.md` 恢复。

治理对象最终覆盖：

```text
Requirement
→ Specification
→ Architecture
→ Technical / Interface Contract
→ Verification
→ Work / Code
```

**G0 — Experiment Bootstrap & Baseline Upgrade 已完成。** Consumer 已完成本实验 prerequisite reusable capability upgrade，精确 evaluated upstream baseline 与 adopted / adapted capability disposition 由 `docs/project/project-capability-profile.md` 持有；G0 完成后 ordinary runtime 已恢复 `upstream access = 0`。

**G1 — Source & Authority Establishment 已完成。** Consumer 已建立 Current Authority、Formal Decision / ADR、versioned canonical source、external / Human input、GitHub-native evidence、Repository implementation evidence、Historical / Legacy material、analysis 与 unknown source 的 provenance / scope / currentness 消费边界，并修复会把 historical / dead locator 重新带入 ordinary Fresh Context 的 subtree 入口。G1 的过程性 source inventory 与 conflict record 继续由 Issue #155 / Review Evidence 持有，不晋升为第二份长期 Authority。

**G2 — Requirement Baseline Rebuild 已完成。** Consumer 已建立 Requirement Human Navigation 与 Requirement Authority Index，并将长期 Requirement 收敛为 Product Requirement 与 CMS Domain Requirement 两个清晰的 canonical fact owner；Capability 通过 Index 定位到唯一 owner / stable section，过程性 extraction / comparison / review 不进入 durable Requirement Authority。当前 Baseline 不包含未复核 provisional default，也没有未关闭的 material Requirement ambiguity。

**G3 — Specification Convergence 已完成。** Current Specification 已收敛为 Admin、Main / Party Public、Page Content、Rich Text Authoring 四个清晰 owner；原独立 Party Specification 的仍有效公开可观察行为已折叠到统一 Public owner，历史原文退出 Current Authority 并归档。Specification 只维护 Scope、Observable / Failure Behavior、Acceptance 与必要 feature-level quality obligation，不并行持有 Requirement / Domain、Architecture、Technical / Verification 或 concrete Site Definition inventory；当前没有未关闭的 material Specification ambiguity 或 parallel Current owner。

下一实验 Gate 为 **G4 — Architecture & Technical Contract Convergence**，当前状态 **NOT STARTED**。G4 必须在新的 Fresh Context 中从当时最新 Repository Authority、Issue #155 与重新建立的真实 work locator 启动；不得从 G3 的 transitional analysis、PR Review、完成态 Specification 收敛或本 Gate 的 implicit context 自动继承 Architecture / Technical 结论、Execute Authority 或当前状态。

Issue #153 / PR #154 Consumer Authority Foundation Rebuild 已完成并作为本轮起始 baseline，不再是当前治理进行中状态；其历史与 integration evidence 留在 GitHub / Project Evolution，不在 Roadmap 继续维护过程状态。

## 后续 Planning directions

以下均保持 **Planning / Review Candidate**；不得从 Issue 排序、历史 EU 或旧 Roadmap 自动获得执行权。Issue #155 的后续 G4～G7 由其 umbrella lifecycle 单独协调，同样必须在各 Gate 的新 Fresh Context 中按当前 Repository Authority 建立实际工作入口。

- **Hui Employment iframe integration**：独立 Main integration candidate；进入前建立 current feature-specific Requirement / Specification Authority，再完成 Planning / Readiness。
- **Issue #60 C1 — Loading / Skeleton Experience**：用户体验 Planning Candidate。
- **Issue #60 C2 — Mobile Layout Human Review**：独立 Human Review / follow-up candidate。
- **Issue #59 — Browser Compatibility & Runtime Guard**：后置兼容性 / runtime governance candidate。
- **Issue #57 — Public Rendering Architecture**：仅在未来 Feature 暴露真实长期 renderer architecture driver 时进入；不因历史 Issue 存在自动设计。
- **Issue #77 — CMS / Site Package / Historical Migration / Public Renderer Architecture**：其有效长期四层边界已经收敛到 `docs/architecture/cms-architecture.md`；Issue #77 继续作为历史 planning / decision / evidence locator，而不是第二份 Current Architecture owner。未来只有新的真实 driver 才重新进入 Requirement / Architecture decision lifecycle。
- **Deferred Article / source-defect review**：当前 Historical Migration workspace 中明确标记为 deferred / source-defect 的 Article 后续复核与客户确认 candidate；精确数量、分类和 digest 由对应 migration manifest / reports 持有，Roadmap 不复制第二份数据清单。不得静默修复、猜测、删除或自动导入。
- **Repository Split Readiness Assessment**：deferred / independent；只有当前边界与真实工程成本产生足够 Evidence 后重新评估。

Issue #137 / EU-55 Page Content Architecture 已完成，不属于开放 Planning Candidate；其稳定历史摘要进入 Project Evolution，当前产品语义已归位到现行 Domain / Architecture / Specification owner。

## Roadmap 维护规则

只在以下情况修改本文件：

- 新增、关闭、延期或重新排序长期 Planning direction；
- 当前 Repository 的治理里程碑发生稳定状态变化；
- 某个已完成方向需要退出 Roadmap 并晋升到 Project Evolution；
- 长期候选被明确 reject / supersede。

不得在本文件维护：

- Current Execute Gate / exact branch Head / workflow result；
- Feature Requirement / Acceptance 正文；
- Architecture contract 正文；
- Method / Rule / Skill 正文；
- 已完成 EU 的逐项验证或 commit 流水；
- 可以从其他 canonical owner 唯一恢复的重复事实。
