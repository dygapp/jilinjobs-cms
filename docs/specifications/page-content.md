---
id: specification-page-content
title: 单页内容模型与结构化页面规格
type: specification
status: accepted
version: "V3.0"
relations:
  requirements:
    - docs/requirements/information-publishing.md
    - docs/requirements/cms-domain.md
  architecture:
    - docs/architecture/cms-architecture.md
updated_at: 2026-09-16
---

# 单页内容模型与结构化页面规格

## 1. Scope

本规格只定义 CMS Page 在用户和运营人员可观察层面的 content-profile 行为，以及当前已接受 Structured Page `guide/jypq` 的产品 contract。

Page 的长期 identity、ownership、content authority、operator-divergence 与 fail-closed 业务不变量由 Domain Requirement 持有；renderer / Site Definition / application boundary 由 Architecture 与对应 source owner 持有；数据库字段、Migration、DTO、registry、源码组件与 adoption 实现属于 Technical / code，不在本规格复制。

## 2. Page content profiles

### 2.1 Rich Text Page

普通说明性、通知性和由运营人员自由维护正文的 Page 使用 Rich Text authoring：

- whole-page Rich Text 是当前主要可编辑正文；
- Admin 提供成熟 Rich Text 编辑体验；
- 保存、重新打开和公开展示必须保持 accepted Rich HTML 语义；
- 不因为 Structured capability 存在而强制迁移普通 Rich Page。

### 2.2 Structured Page

当 card、section、step 等结构本身属于产品语义时，Page 可以使用 Structured content：

- Admin 使用与当前 schema 对应的结构化编辑 surface；
- 不同时提供第二个 whole-page arbitrary HTML editor；
- Public 根据明确 renderer identity 呈现；
- unknown schema / version / renderer 必须显示明确不支持 / 错误状态，不能静默 fallback 为 Rich HTML。

Structured payload 的 identity / ownership 与 primary-content 规则由 Domain Requirement 持有，本规格不建立第二套表示权威。

### 2.3 Engineering / External Page

当页面主要行为由受控工程实现或外部系统承担时，CMS 仍保留 Page identity、canonical URL 与 lifecycle，但不伪装成 operator-owned whole-page Rich body。

具体工程 interaction、第三方 integration 和 delivery mechanism 需要独立 Feature Specification；本规格不自动实现慧就业 iframe 或其他外部系统。

## 3. Structured Card Collection V1

当前第一种 accepted Structured shape 为有序卡片集合。

用户可观察 contract：

- 页面由若干有序 card 组成；
- 每个 card 具有 plain-text title 与可包含 accepted Rich Text 的 body；
- card 顺序是用户可观察结果的一部分；
- Admin 可以新增、删除、编辑和调整 card 顺序；
- 保存并重新打开后，card 数量、顺序、title、body 与内容中的有效资源引用保持；
- invalid / malformed structured data 不通过自动“转成 Rich HTML”掩盖。

V1 不承诺 generic block builder、任意布局 DSL、item-level workflow、独立 card identity 或跨 Page card reuse。

## 4. `guide/jypq` current contract

`/page/guide/jypq` 当前使用 Structured Card Collection，并保持既有稳定 Page identity 与 canonical URL。

当前 accepted behavior：

- Public 按当前 accepted Site Definition 的 card structure、内容顺序和有效资源引用呈现；
- 页面必须按 card 结构呈现，不能退回 whole-page raw HTML；
- Admin 只提供 card-aware authoring surface，不提供并行 whole-page Rich editor；
- ordinary operator 对 Structured content 的后续编辑在正常 reconcile 后继续保留。

具体 card 数量、完整文案、resource inventory / bytes 属于当前 Site Definition content，不在 Specification 复制第二份 inventory。

## 5. Existing content reconciliation behavior

当版本化 Site Definition 需要让已有 Page content 与新的 accepted Structured target 协调时，运营人员可观察结果必须满足：

- 已经处于当前 accepted target 时不产生无意义变化；
- 满足 Domain 所定义的受控 adoption 条件时可以进入新的 accepted representation；
- 已被运营人员修改的内容不得被 Site Definition 静默覆盖，并应产生可诊断的 divergence result；
- 已完成协调的内容再次经过相同流程时保持稳定；
- adoption / reconcile 后的运营人员修改继续保留。

精确 fingerprint、prior-baseline 判定与 reconcile algorithm 由 Domain / Technical owner 持有，本规格不复制实现判定规则。

## 6. Admin authoring behavior

Admin 根据 Page 当前 content profile 选择 authoring surface：

- Rich Text → whole-body Rich Text editor；
- Structured Card Collection → ordered card editor；
- unsupported profile/schema → blocking diagnostic / safe read-only state。

对预置 / 稳定 Page 的 ordinary content editing，不允许运营人员通过普通表单任意切换 Domain 所定义的 content model、renderer identity 或 ownership。

## 7. Public behavior

- canonical routes 保持 `/page/{alias}` 与 `/page/{groupAlias}/{alias}`；
- 页面 Shell、breadcrumb、group tab 与 Page identity 不因 renderer 不同建立第二套 URL；
- Public 根据当前明确 renderer identity 呈现；
- unknown renderer / schema 显示可诊断失败，不呈现错误或过期的 Rich fallback；
- Structured 页面仍应形成合理 metadata / text summary，不依赖不存在的 whole-page Rich body。

## 8. Failure behavior

以下情况不得静默成功：

- malformed structured payload；
- unknown structured kind / version；
- unknown renderer；
- content profile 与当前 primary content contract 不一致；
- reconcile 会覆盖 Domain 认定的 operator-diverged content。

具体 HTTP status、log、report、fingerprint algorithm 或 UI message 由对应 Technical / implementation contract 决定，但失败必须能被用户或验证证据识别。

## 9. Acceptance

触达本能力时，最终结果至少满足实际涉及的以下 contract：

- representative Rich Page 正常编辑 / 保存 / 重开 / 公开展示；
- `/page/guide/jypq` 保持当前 Site Definition 接受的 card structure、顺序、内容与有效资源引用；
- card add / remove / reorder / edit / reload 后语义保持；
- invalid Structured data fail closed；
- unknown renderer fail closed；
- accepted baseline 的受控 adoption 不损坏内容；
- operator-diverged predecessor 被保留并产生可诊断结果；
- repeated reconcile 保持稳定；
- reconcile 后 operator edit 不被 ordinary process 覆盖；
- `/page/guide/jypq` canonical URL 不变。

验证采用何种 automation / Browser / Human evidence 由当前 Verification Authority 与实际变更风险决定。

## 10. Non-goals

- generic Page Builder / arbitrary block framework；
- 自动把 FAQ 等其他 Page 迁移为 Structured；
- 从 alias/path 推导 Page type；
- 自动实现慧就业 integration；
- 为展示样例创建无真实需求的 Engineering Page。
