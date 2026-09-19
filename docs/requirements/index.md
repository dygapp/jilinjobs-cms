---
id: requirement-authority-index
title: 需求权威索引（Requirement Authority Index）
type: requirement-index
status: active
updated_at: 2026-09-19
---

# 需求权威索引（Requirement Authority Index）

## 1. 责任

本文件是 `jilinjobs-cms` 当前 Requirement Authority Index，只回答“某类长期 Requirement 的唯一 fact owner 在哪里”。

它不复制 Requirement 正文，不维护 Current Execution State，不把 Specification / Architecture / Technical / code 提升为 Requirement，也不保存 source inventory、review batch 或临时 analysis。

## 2. 当前 Requirement Capability / owner

| Requirement responsibility | Stable capability / fact boundary | 唯一当前 owner |
|---|---|---|
| Product Goal / Scope / Actors | 产品为什么存在、面向谁、当前包含和排除什么 | `information-publishing.md` §2～§4 |
| CMS content operations | 运营人员长期能够维护哪些内容与运营对象，以及这些能力产生什么业务结果 | `information-publishing.md` §5.1～§5.2 |
| Main public delivery | Main 首页、栏目、文章、Page、固定业务 seam 与稳定公开访问习惯 | `information-publishing.md` §5.3、§6 |
| 慧就业公共网站集成 | 首页就业日历、最新招聘、直播课程，以及招聘信息 / 直播课程二级页面与慧就业页面之间的地址映射、业务编号契约和外部系统边界 | `hui-employment-integration.md` §2～§15；历史已确认但未启用映射见附录 A |
| Party product positioning | “中心党建”的业务定位、内容范围、专题入口与公开边界 | `information-publishing.md` §5.4、§6 |
| Canonical public contract | Main / Party 稳定公开 URL 与 implementation-independent public identity | `information-publishing.md` §6 |
| Product-level quality / NFR | 响应式、基础可访问性、安全呈现、可诊断失败、替换实现时保持产品语义等跨 Capability 要求 | `information-publishing.md` §9 |
| Product acceptance / out-of-scope | 项目级验收不变量、非决策与未来事项边界 | `information-publishing.md` §10～§11 |
| CMS object model | Column、Article、PageGroup / Page、Navigation、CmsList、Advertisement、SiteProperty、StaticResource 等业务对象与关系 | `cms-domain.md` §4～§11 |
| Identity / lifecycle / ownership | stable identity、source identity、publish lifecycle、content ownership、preset protection 与 operator divergence | `cms-domain.md` §3，并按对象读取 §4～§10；跨对象 lifecycle 读取 §13～§17 |
| External-link ownership | Article / Navigation / CmsList / Advertisement / fixed integration 各自拥有外链语义，不建立全局 Link 对象 | `cms-domain.md` §12 |
| Stable Site Definition vs Runtime | stable structure、one-time bootstrap 与 ordinary operator-managed Runtime 的 Domain lifecycle | `cms-domain.md` §13 |
| 历史内容迁移 semantics | provenance、legacy identity、fingerprint、preflight、compatibility、offline stability、Main / Party accepted scope | `cms-domain.md` §14 |
| Rich Text domain invariants | HTML body authority、accepted content semantics、资源关系与 active-content safety 的 Domain 不变量 | `cms-domain.md` §15 |
| Domain failure / acceptance | 业务 identity、content model、resource relation、migration conflict 等 失败关闭 与 Domain acceptance invariants | `cms-domain.md` §16～§17 |

同一 Product / Domain 文件可以承担多个相互一致的 Requirement Capability；唯一 ownership 按上表的 semantic responsibility 与稳定章节确定，不因“一个 capability = 一个文件”的机械规则拆分。

## 3. 不是 Requirement fact owner 的 Current sources

以下来源具有重要证明或执行价值，但不拥有上表 Requirement fact：

| Source | Current role |
|---|---|
| `sites/jilinjobs/**` | JilinJobs Site Definition 的 versioned canonical source；拥有具体稳定结构、bootstrap defaults、assets 与 manifest facts |
| `data-migrations/**` | Historical canonical dataset / provenance / fingerprint / concrete migration evidence |
| `docs/specifications/**` | 当前 Feature / surface 的 可观察行为、失败行为 与 验收 |
| `docs/architecture/**` | 跨 Feature 长期系统结构、责任边界与 ADR / current Architecture State |
| `docs/technical/**` | implementation HOW、interface / runtime contract 与 Verification Strategy |
| Repository code / tests | 当前实现状态与验证 证据；不能反向发明 Product Requirement |
| GitHub Issue / PR / Actions | planning / decision / review / integration 的原生 当前证据；不长期拥有 Requirement fact |
| `docs/requirements/archive/**` | superseded / historical Requirement evidence；默认退出 ordinary Fresh Context |

## 4. Fresh Context 路由

普通 Feature / change 的 Requirement consumption：

```text
Repository Authority
→ 本 Index
→ 一个 primary Product / Domain owner + 最小相关章节
→ 必要的 related Product / Domain owner
→ Specification / Architecture / Technical
```

例如：

- 修改 Article source identity → `cms-domain.md` Article / identity sections；只有需要确认产品范围时再读取 `information-publishing.md`；
- 修改 Party 内容入口或 canonical URL → `information-publishing.md` Party / URL sections，再按需读取 Domain；
- 修改 历史迁移 fingerprint / accepted scope → `cms-domain.md` 历史迁移 section，再读取 `data-migrations/**` 的 concrete canonical evidence；
- 修改跨站响应式 /安全呈现要求 → `information-publishing.md` 产品级质量 section，再进入对应 Specification / Verification。
- 修改慧就业嵌入范围、完整页面地址、业务分类编号 / 业务编号映射或外部系统边界 → `hui-employment-integration.md`；只有需要确认 Main 上层产品范围时再读取 `information-publishing.md`。

如果 Index 不能唯一定位 owner、多个 Current owner 对同一长期事实给出不兼容结论，或真实事实不属于任何现有 owner，必须 失败关闭，返回 `method:requirement-baseline-establishment` / 人工权威，而不是在下游文档中静默选择。

## 5. 分析边界

Requirement extraction、冲突矩阵、capability review batch 与临时 comparison 默认不进入本 Index。只有 durable owner / relation 改变时更新本文件；过程 证据 留在当前 controlling Issue / Review 或显式 non-Authority analysis artifact。
