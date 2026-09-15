---
id: project:roadmap
type: project
status: active
---

# Project Roadmap

## 角色

本 Roadmap 只维护 `jilinjobs-cms` 的**当前治理 / 产品演进方向、durable planning boundary 与 deferred direction**。它不拥有产品 Requirement、Domain、Architecture 正文，不保存已完成 Execution Unit 的实施流水，也不缓存 `Current Ready Execution Unit`、Readiness、exact Head 或 Actions 结果。

Current Execution Lifecycle 统一从 `docs/work/current/README.md` 恢复；已完成里程碑的稳定摘要由 `docs/project/project-evolution.md` 持有；具体历史 Evidence 留在 Git / Issue / PR / Actions、`docs/work/archive/` 与各类 archive。

Roadmap 顺序、Issue 编号或候选名称均不授予 Execute Authority。Feature 只有经过当前 Method 的 Specification / conditional Technical Planning / Slice & Ready / Readiness Gate 后才能进入 Execute。

## 当前治理里程碑

### Issue #153 — Consumer Authority Foundation Rebuild

状态：**治理进行中；不授予产品 Execute Authority。**

目标是在不机械复制 upstream、也不盲目继承历史 Consumer 文档的前提下，重建可独立运行的 Consumer-local Authority Foundation：

- 一等 Method / Architecture / Rule / Skill owner 与 Project Capability Profile；
- 清晰的 Project Knowledge 边界；
- 基于 current / legacy / implementation / human decision Evidence 的 Requirement / Domain Clarification；
- 条件性的长期 Architecture Clarification；
- Requirement → Feature Specification → conditional Technical Plan → Work 的可持续链路；
- 清理被取代、重复或仅有过程价值的中间 Markdown；
- Independent Semantic Review；
- Consumer-local Fresh Context / ordinary runtime validation，并恢复普通运行 `upstream access = 0`。

本治理允许按 Issue #153 的 Bootstrap Governance Authority 只读使用固定 `agentic-dev` reference；该权限只服务 Foundation Rebuild，不能传播为普通运行依赖，也不能决定 JilinJobs 产品事实。

## 后续 Planning directions

以下均保持 **Planning / Review Candidate**，需要在 Issue #153 收敛后的新 Authority 上重新确认优先级、Requirement context 与 Readiness；不得从历史 EU、Issue 排序或旧 Roadmap 自动获得执行权。

- **Hui Employment iframe integration**：独立 Main integration candidate；进入前恢复 / 重建 current Requirement / Specification Authority，再完成 Planning / Readiness。
- **Issue #60 C1 — Loading / Skeleton Experience**：用户体验 Planning Candidate。
- **Issue #60 C2 — Mobile Layout Human Review**：独立 Human Review / follow-up candidate。
- **Issue #59 — Browser Compatibility & Runtime Guard**：后置兼容性 / runtime governance candidate。
- **Issue #57 — Public Rendering Architecture**：仅在未来 Feature 暴露真实长期 renderer architecture driver 时进入；不因历史 Issue 存在自动设计。
- **Issue #77 — CMS / Site Package / Historical Migration / Public Renderer Architecture**：现有四层长期边界在本轮 Product Authority Clarification 完成前继续作为 current evidence / authority locus；Issue #153 必须将其有效长期语义收敛到新的 canonical Architecture owner，而不是在 Roadmap 重复正文。
- **Deferred Article / source-defect review**：230 篇 problem Article 与 6 篇 source-defect Article 的独立 later-review / customer-confirmation candidate；不得静默修复、猜测、删除或自动导入。
- **Repository Split Readiness Assessment**：deferred / independent；只有当前边界与真实工程成本产生足够 Evidence 后重新评估。

Issue #137 / EU-55 Page Content Architecture 已完成，不属于开放 Planning Candidate；其稳定历史摘要进入 Project Evolution，当前产品语义在本轮 Authority Rebuild 中作为 Evidence 重新归位。

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