---
id: specification-page-content-architecture
title: 单页内容模型与结构化页面规格
type: specification
status: accepted
version: "V2.0"
relations:
  requirements:
    - docs/requirements/information-publishing.md
    - docs/requirements/cms-domain.md
  architecture:
    - docs/architecture/cms-architecture.md
updated_at: 2026-09-15
---

# 单页内容模型与结构化页面规格

## 1. Scope

本规格只定义 CMS Page 在用户和运营人员可观察层面的内容模型行为，以及当前已接受 Structured Page `guide/jypq` 的产品 contract。

Page 的长期 identity、ownership、fail-closed 原则由 Domain Requirement 持有；renderer / Site Definition / application boundary 由 Architecture 持有；数据库字段、Migration、DTO、registry、源码组件与 adoption 实现属于 Technical / code，不在本规格复制。

## 2. Page content profiles

### 2.1 Rich Text Page

普通说明性、通知性和由运营人员自由维护正文的 Page 使用 Rich Text authoring：

- whole-page Rich Text 是当前 primary body；
- Admin 提供成熟 Rich Text 编辑体验；
- 保存、重新打开和公开展示必须保持 accepted Rich HTML 语义；
- 不因为 Structured capability 存在而强制迁移普通 Rich Page。

### 2.2 Structured Page

当 card、section、step 等结构本身属于产品语义时，Page 可以使用 Structured content：

- Structured payload 是 primary body；
- Admin 使用与当前 schema 对应的结构化编辑 surface；
- 不同时提供第二个 whole-page arbitrary HTML editor；
- Public 根据明确 renderer identity 呈现；
- unknown schema / version / renderer 必须显示明确不支持 / 错误状态，不能静默 fallback 为 Rich HTML。

### 2.3 Engineering / External Page

当页面主要行为由受控工程实现或外部系统承担时，CMS 仍保留 Page identity、canonical URL 与 lifecycle，但不伪装成 operator-owned whole-page Rich body。

具体工程 interaction、第三方 integration 和 delivery mechanism 需要独立 Feature Specification；本规格不自动实现慧就业 iframe 或其他外部系统。

## 3. Structured Card Collection V1

当前第一种 accepted Structured shape 为有序卡片集合。

用户可观察 contract：

- 页面由若干有序 card 组成；
- 每个 card 具有 plain-text title 与可包含 accepted Rich Text 的 body；
- card 顺序是产品语义的一部分；
- Admin 可以新增、删除、编辑和调整 card 顺序；
- 保存并重新打开后，card 数量、顺序、title、body 与内容中的有效资源引用保持；
- invalid / malformed structured data 不通过自动“转成 Rich HTML”掩盖。

V1 不承诺 generic block builder、任意布局 DSL、item-level workflow、独立 card identity 或跨 Page card reuse。

## 4. `guide/jypq` current contract

`/page/guide/jypq` 当前使用 Structured Card Collection，并保持既有稳定 Page identity 与 canonical URL。

当前 accepted behavior：

- 页面展示 3 个有序 cards；
- card 内容、顺序及当前接受的 4 个 package image references 以当前 Site Definition 为内容事实来源；
- Public 必须按 card 结构呈现，不能退回 whole-page raw HTML；
- Admin 只提供 card-aware authoring surface，不提供并行 whole-page Rich editor；
- ordinary operator 对当前 Structured content 的后续编辑必须在正常 reconcile 后继续保留。

本规格不复制 card 的全部文案和图片 bytes，避免 Site Definition 与 Specification 双写同一正式内容。

## 5. Existing content adoption behavior

当版本化 Site Definition 需要把已存在的旧 Page content 升级到新的 Structured target 时，可观察行为必须满足：

1. 当前 content 已是新 target：no-op；
2. 当前 content 精确匹配已声明的 prior accepted baseline：允许受控 adoption；
3. 当前 content 已被 operator 修改：保留当前 content，并产生明确 divergence evidence；
4. adoption 成功后重复运行 no-op；
5. adoption 后 operator 对 Structured content 的修改继续保留。

不得通过 alias、DOM、空值、标题相似或其他 heuristic 猜测“这是旧 baseline”。

## 6. Admin authoring behavior

Admin 根据 Page 当前 content profile 选择 authoring surface：

- Rich Text → whole-body Rich Text editor；
- Structured Card Collection → ordered card editor；
- unsupported profile/schema → blocking diagnostic / safe read-only state。

对预置 / 稳定 Page 的 ordinary content editing，不允许运营人员通过普通表单任意切换 content model、renderer identity 或 ownership。

## 7. Public behavior

- canonical routes 保持 `/page/{alias}` 与 `/page/{groupAlias}/{alias}`；
- 页面 Shell、breadcrumb、group tab 与 Page identity 不因 renderer 不同建立第二套 URL；
- Public renderer 必须使用明确的 renderer identity；
- unknown renderer / schema 显示可诊断失败，不呈现错误或过期的 Rich fallback；
- Structured 页面仍应形成合理 metadata / text summary，不依赖不存在的 whole-page Rich body。

## 8. Failure behavior

以下情况不得静默成功：

- malformed structured payload；
- unknown structured kind / version；
- unknown renderer；
- content profile 与 primary body 不一致；
- existing content 不满足 declared adoption precondition；
- renderer 只能通过 alias/path/DOM heuristic 才能选择。

具体 HTTP status、log、report 或 UI message 由 Technical contract决定，但必须能被自动化和人工验证观察。

## 9. Acceptance

触达本能力的 Feature 至少验证：

- representative Rich Page 正常编辑 / 保存 / 重开 / 公开展示；
- `guide/jypq` 3 cards、accepted order/content 与 4 个当前 Site Definition resource references保持；
- card add / remove / reorder / edit / reload；
- invalid Structured data fail closed；
- unknown renderer fail closed；
- exact prior-baseline adoption成功；
- operator-diverged predecessor preserve + report；
- repeated adoption idempotent；
- adoption 后 operator edit survive ordinary reconcile；
- `/page/guide/jypq` canonical URL 不变；
- 有视觉变化时，自动 Browser evidence 后再执行 bounded Human Review。

## 10. Non-goals

- generic Page Builder / arbitrary block framework；
- 自动把 FAQ 等其他 Page 迁移为 Structured；
- 从 alias/path 推导 Page type；
- 自动实现慧就业 integration；
- 为展示样例创建无真实需求的 Engineering Page；
- 从本规格恢复任何已结束 Execution Unit。