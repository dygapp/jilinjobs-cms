---
id: project:roadmap
type: project
status: active
---

# 项目路线图（Project Roadmap）

## 角色

本 Roadmap 只维护 `jilinjobs-cms` 的**当前治理 / 产品演进方向、durable planning boundary 与 deferred direction**。它不拥有产品 Requirement、Domain、Architecture 正文，不保存已完成 Execution Unit 的实施流水，也不缓存 `Current Ready Execution Unit`、Readiness、exact Head、Actions 结果或可由其他 规范语义所有者 唯一恢复的数据 inventory。

当前执行生命周期 统一从 `docs/work/current/README.md` 恢复；已完成里程碑的稳定摘要由 `docs/project/project-evolution.md` 持有；具体历史 Evidence 留在 Git / Issue / PR / Actions、`docs/work/archive/` 与各类 archive。

Roadmap 顺序、Issue 编号或候选名称均不授予 执行授权。Feature 只有经过当前 Method 的 Specification / conditional Technical Planning / Slice & Ready / Readiness Gate 后才能进入 Execute。

## 当前治理里程碑

当前没有 active Project Governance / Validation milestone。

Issue #155 — Full Documentation Authority Rebuild & Regenerability Validation 已完成 G0～G7 并退出 Current Roadmap lifecycle；稳定演进摘要已进入 `docs/project/project-evolution.md`，完整 Gate / Failure Evidence / PR / Actions / upstream feedback 继续由 Issue #155 与 GitHub native history 追溯。该实验没有授予产品 执行授权，也没有激活 Main / Party Historical Migration。

Issue #153 / PR #154 Consumer Authority Foundation Rebuild 也已完成，不再是当前治理进行中状态；其历史与 integration evidence 留在 GitHub / Project Evolution。

## 后续 Planning directions

以下均保持 **规划 / 评审候选**；不得从 Issue 排序、历史 EU 或旧 Roadmap 自动获得执行权。任何方向进入实际工作前，都必须从新的 Fresh Context 按当前 仓库权威 建立对应 Planning / Review lifecycle。

- **Issue #60 C1 — Loading / Skeleton Experience**：用户体验 规划候选。
- **Issue #60 C2 — Mobile Layout 人工评审**：独立 人工评审 / follow-up candidate。
- **Issue #59 — Browser Compatibility & Runtime Guard**：后置兼容性 / runtime governance candidate。
- **Issue #57 — Public Rendering Architecture**：仅在未来 Feature 暴露真实长期 renderer architecture driver 时进入；不因历史 Issue 存在自动设计。
- **Issue #77 — CMS / Site Package / Historical Migration / Public Renderer Architecture**：其有效长期四层边界已经收敛到 `docs/architecture/cms-architecture.md`；Issue #77 继续作为历史 planning / decision / evidence locator，而不是第二份 Current Architecture owner。未来只有新的真实 driver 才重新进入 Requirement / Architecture decision lifecycle。
- **Deferred Article / source-defect review**：当前 Historical Migration workspace 中明确标记为 deferred / source-defect 的 Article 后续复核与客户确认 candidate；精确数量、分类和 digest 由对应 migration manifest / reports 持有，Roadmap 不复制第二份数据清单。不得静默修复、猜测、删除或自动导入。
- **Repository Split Readiness Assessment**：deferred / independent；只有当前边界与真实工程成本产生足够 Evidence 后重新评估。

Issue #137 / EU-55 Page Content Architecture 已完成，不属于开放 规划候选；其稳定历史摘要进入 Project Evolution，当前产品语义已归位到现行 Domain / Architecture / Specification owner。

Issue #176 / EU-56 慧就业公共网站固定 iframe 集成已完成，不再属于开放规划候选；稳定结果进入 Project Evolution，当前产品与技术语义由现行 Requirement、Specification 与 Technical owner 持有。

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
- 可以从其他 规范语义所有者 唯一恢复的重复事实。
