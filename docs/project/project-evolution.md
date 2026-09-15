---
id: project:evolution
type: project
status: active
---

# Project Evolution

## 角色

本文件只保存 `jilinjobs-cms` 已完成、仍有助于理解当前 Repository 的**稳定演进摘要**。它不拥有 Current Execute Gate，不替代产品 Requirement / Domain / Architecture，也不重复保存详细实施 / 验证 Evidence。

完整历史通过 Git、Issue / PR / Actions、`docs/work/archive/`、`docs/project/archive/` 与 `data-migrations/**` 追溯。

## 产品与工程演进

### Consumer Bootstrap ～ EU-36

完成通用 CMS、Spring Boot Backend、Admin / Public frontend、Party 正式页面与 historical migration、Rich Text 基础能力和 Public source isolation，并形成可独立部署和验证的 Consumer Repository 基础。

### EU-37 ～ EU-42 — Site Package Boundary

建立 Generic CMS Core 与 JilinJobs Site Package 的稳定边界：stable structure、Navigation identity、Runtime composition、one-time bootstrap、Generic Schema separation 与 stable assets 逐步闭环。具体长期产品 / 架构语义在当前 Authority Rebuild 中由真实 Requirement / Architecture owner重新收敛，本 Evolution 只保留已发生的演进事实。

### Issue #92 / EU-43 ～ EU-48 — Authority 与工程基础收敛

完成 Documentation Authority、Backend application core、Generic Content Migration 与 Party migration compatibility 等阶段性 convergence。对应规划和阶段 contract 已退出 Current Authority，历史记录保存在 `docs/project/archive/`、`docs/work/archive/` 与 GitHub Evidence。

### EU-49 ～ EU-53 — Main 内容与 Site Package 交付

- EU-49：关闭 Page operational-content ownership gap，并建立 Generic Page canonical migration foundation；
- EU-50：完成 Main source discovery 与 accepted snapshot promotion；
- EU-51：完成 accepted Main Article canonical import、Fresh Runtime reconciliation / idempotency、Public/Admin/Integrated Browser 与 bounded Human Review；
- EU-52：完成 accepted Main formal Page content / assets 的 Site Package delivery boundary；
- EU-53：完成 Main ListItem one-time bootstrap initialization。

Main Article snapshot 的当前 canonical data / provenance 由 `data-migrations/main/v1/**` 持有；230 篇 problem Article 与 6 篇 source-defect Article仍属于 deferred / customer-confirmation backlog，不因本历史摘要取得新的处理授权。

### EU-54 ～ EU-55 — Authoring 与 Page Content Architecture

- EU-54：通过 thin Consumer adapter 采用成熟 Rich Text editor，并保持既有 Rich HTML safety / compatibility；
- EU-55：建立首个 accepted Structured Page vertical，将 Page content model / renderer identity / content ownership 正交分离，并以显式 renderer registry / fail-closed resolution 支持 structured card rendering，而不引入通用 Page Builder。

这些结果均已完成 Execute / Post-Integration closure；当前长期语义由 Issue #153 后续 Requirement / Architecture Clarification重新归位，不从历史 Work artifact直接恢复新的 Execute Authority。

## Consumer Method / Capability 演进

### V3-08 Track B adoption

Consumer 开始显式区分 upstream reusable capability 与 Consumer-local Project state，并采用 Local Discovery、Skill identity 与 Consumer lifecycle 的关键语义；普通运行继续以本地 owner 为准。

### V3 Closure adoption

采用条件性、临时 Handoff lifecycle 等收敛语义，同时保持 Consumer-local Verification / Discovery / Skills owner；upstream Project closure state不继承。

### Rule Granularity adoption

将 Rule 从过度原子化收敛到任务 / responsibility 级自然边界，并建立 `technology/vue/` 等只服务 Human IA 的目录结构；deterministic Rule Discovery 继续按 Rule metadata 而非目录匹配。

### Clarification / Rule Activation adoption

Evaluated upstream frontier 推进到：

`dygapp/agentic-dev@ed1a4446f0430890e7ad39673ac9c2e341e6a829`

采用 bounded Software Project Clarification、Feature responsibility boundary 与 direct-responsibility Rule Discovery checkpoint；Model Collaboration runtime 未采用、未启用。

对应 upgrade / validation history 已物理归档至 `docs/project/archive/`。当前 capability instance 只由 `docs/project/project-capability-profile.md` 与真实 Method / Architecture / Rule / Skill owner定义。

## 追溯入口

- completed Execution Units：`docs/work/archive/`；
- completed project planning / baseline upgrade / validation records：`docs/project/archive/`；
- Main / Party canonical migration evidence：`data-migrations/**`；
- detailed commit / PR / review / workflow evidence：GitHub native history；
- current future directions：`docs/project/project-roadmap.md`。

若某段历史经验需要再次成为 Current Requirement、Architecture、Method、Rule 或 Skill，必须先由当前 Authority重新裁决并晋升到真实 semantic owner；本 Evolution 只提供背景与 provenance。