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

**G3 — Specification Convergence 已完成。** G3 当时将 Admin、Main / Party Public、Page Content、Rich Text Authoring 的 Current Specification 责任收敛到清晰 owner，并使原独立 Party Specification 退出 Current Authority；后续 Gate 可以在新的真实跨层缺口出现时增补新的 Specification owner，而不把 G3 当时的 owner 数量冻结成长期 inventory。Specification 持续只维护 Scope、Observable / Failure Behavior、Acceptance 与必要 feature-level quality obligation，不并行持有 Requirement / Domain、Architecture、Technical / Verification 或 concrete Site Definition inventory。

**G4 — Architecture & Technical Contract Convergence 已完成。** 当前 CMS Architecture State 保持既有 Generic Core / CMS Server / Content Migration / Replaceable Public Renderer 等长期边界，不因实现漂移新增无 driver 的架构方案或 ADR；Backend ↔ Admin/Public Frontend 的稳定 HTTP compatibility 已由 `docs/technical/http-interface-contract.md` 建立唯一 Current Technical owner，Backend/Admin/Public Technical 与 Verification Strategy 改为消费该 owner，不复制 endpoint / DTO 第二事实源。G4 发现的 Core 内 HTTP Controller、`MultipartFile` transport leakage 与 boundary verification inventory blind spot 继续作为 implementation / verification evidence 保留，不反向改写 Architecture Authority，也不在本 Gate 顺手重构现有代码，从而保持 G6 code-holdout / technology substitution 的验证 subject。

**G5 — Cross-layer Independent Semantic Review 已完成。** 独立 Review 已对 Requirement ↔ Specification ↔ Architecture ↔ Technical / Interface ↔ Verification 做双向 traceability 与 regenerability challenge，并关闭发现的跨层缺口：Admin Article lifecycle 的可观察投影、Admin/Public Article `columnId` scope 与 lifecycle HTTP compatibility、Main 招聘日历最小 observable contract、基础 SEO observable contract、Historical Content Migration maintainer-visible Specification orphan，以及 Backend Public projection 与 Main / Party Renderer site-scope 的责任歧义。Historical Migration 已恢复最小 Current Specification owner，但该 owner 不授予 migration execute authority，也不解除 Main migration freeze；Verification Strategy 已加入可复用的跨层语义 / code-holdout regenerability review contract。G5 Exit 时 blocking semantic finding 为 0。

**G6 — Regenerability & Technology Substitution Validation 已完成。** 独立 Fresh Context 先以 Current Authority、正式 Site Definition 与 canonical migration/data assets 冻结 implementation-independent design input，再执行 Product Reconstruction、Article representative vertical reconstruction、code-holdout comparison、Node.js Backend substitution dry-run、HTML-first / Progressive Enhancement Public Renderer substitution dry-run与 Fresh Context Minimality。R4 / R5 只证明 replacement design / contract completeness，不冒充已构建、已运行的新 implementation evidence。G6 发现并关闭了 Party canonical migration workspace 将 stale Flyway inventory 与 Spring / Gradle / class / task wiring混入 Current入口的问题；修复后 bounded Fresh Context不再需要旧 implementation、archive、upstream或治理会话隐式知识来补齐设计语义。G6 Exit 时 R1～R6 均 PASS，blocking finding 为 0；本 Gate 未获得或授予任何产品 Execute Authority，也未激活 Main / Party Historical Migration。

下一实验 Gate 为 **G7 — Review Evidence Synthesis**，当前状态 **NOT STARTED**。G7 必须在新的 Fresh Context 中从 Issue #155、当前 Repository Authority 与 G1～G6 已记录 Failure Evidence重新恢复，只负责汇总真实失败并形成 Review Rule Evidence Pack；不得把单个 Consumer failure自动提升为上游 Rule，也不得从 G6 dry-run取得产品 Execute Authority。

Issue #153 / PR #154 Consumer Authority Foundation Rebuild 已完成并作为本轮起始 baseline，不再是当前治理进行中状态；其历史与 integration evidence 留在 GitHub / Project Evolution，不在 Roadmap 继续维护过程状态。

## 后续 Planning directions

以下均保持 **Planning / Review Candidate**；不得从 Issue 排序、历史 EU 或旧 Roadmap 自动获得执行权。Issue #155 的后续 G7 由其 umbrella lifecycle 单独协调，同样必须在新的 Fresh Context 中按当前 Repository Authority 建立实际工作入口。

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
