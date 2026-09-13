# 单页内容架构规格说明

## Authority

- `docs/requirements/page-content-architecture.md`
- GitHub Issue #77
- GitHub Issue #137（legacy acquisition evidence / planning traceability）
- `docs/requirements/information-publishing.md`
- `docs/specifications/cms-site-package-boundary.md`

## 状态

- Specification：**CURRENT / ACCEPTED**；
- EU-55：**COMPLETED / Execute Authority TERMINATED**；
- Current representative Structured Page：`guide/jypq`；
- 本文件不维护 Current Execution Gate。

## 1. Page contract

Page stable identity、group、canonical URL 与 enabled / publish lifecycle 继续由 CMS Page domain 持有。正文 contract 分为三个正交维度：

- `contentModel`：正文数据形态；
- `rendererKey`：Public Renderer identity；
- `contentOwner`：primary content ownership。

Current Generic CMS values：

```text
PageContentModel = RICH_TEXT | STRUCTURED | NONE
PageContentOwner = OPERATOR | SITE_PACKAGE | ENGINEERING | EXTERNAL
rendererKey      = validated stable string
```

`rendererKey` 不做 Generic Core enum，因为具体 renderer registry 可以是 site / frontend specific；Generic Core 只约束格式和长度。

## 2. Model-owned content representation

任何 Page 在同一时刻只能有一个 primary body representation：

- `RICH_TEXT`：`bodyHtml` 是正文 Authority，`structuredPayload` 必须为空；
- `STRUCTURED`：`structuredPayload` 是正文 Authority，top-level `bodyHtml` 必须为空；
- `NONE`：CMS 不持有 whole-page primary body，其他 integration metadata 按相应 profile 使用。

不得通过 alias/path/DOM shape 恢复第二套隐式 Page type。

## 3. Rich Content profile

普通运营正文使用：

```text
contentModel = RICH_TEXT
rendererKey = RICH_TEXT
contentOwner = OPERATOR
```

- Fresh create / explicit adoption 可以由 Site Package 提供 default；
- ordinary Runtime content 由 operator 维护；
- Admin 继续使用成熟 `RichTextEditor`；
- Public 使用 generic Rich renderer；
- renderer 不根据 Page alias 增加业务特判。

`about` 是代表样本，当前不迁移。

## 4. Structured Page profile

Structured Page 适用于结构本身具有产品语义、但内容仍属于 CMS/operator lifecycle 的页面。

Current first schema：

```json
{
  "schemaVersion": 1,
  "kind": "CARD_COLLECTION",
  "items": [
    {
      "title": "plain text title",
      "bodyHtml": "<p>sanitized item-level rich content</p>"
    }
  ]
}
```

Contract：

- `items` 数组顺序就是呈现顺序 Authority；
- `title` 必填且按 plain text 处理；
- item `bodyHtml` 使用现有 Rich HTML safety policy；
- V1 不引入独立 item ID；
- V1 不引入独立 resource array；资源继续在 item body 中使用现有 managed/static resource contract；
- unknown `kind`、unknown `schemaVersion`、malformed JSON 或无效 item structure 均 fail closed；
- 不得把无效 Structured payload 强制转换为 Rich HTML。

这不是通用 Page Builder schema。新增 block kinds / arbitrary layout 需要新的 Requirement。

## 5. `guide/jypq` Current decision

EU-55 已完成 `guide/jypq` Structured adoption；EU-52 flattened Rich HTML 只保留为 prior-package predecessor evidence。

Current target：

```text
contentModel = STRUCTURED
rendererKey = JILINJOBS_GUIDE_CARDS
contentOwner = OPERATOR
structuredPayload.schemaVersion = 1
structuredPayload.kind = CARD_COLLECTION
```

Current accepted payload 保留 3 个 ordered cards 以及既有 4 张 package image references。Public rendering 必须通过 renderer dispatch 呈现 cards，不得 fallback 到 whole-page `v-html`。

如果未来 evidence 证明 card model 无法覆盖真实 repository-owned interaction / state，必须 Stage Return 到新的 Specification / Human Review，而不是在实现中加入 alias/path special case。

## 6. Renderer resolution

Current renderer contract：

```text
Page.rendererKey
        ↓
Public renderer registry / resolver
        ↓
registered renderer consumes allowed content model
```

必须满足：

- `RICH_TEXT` → generic Rich renderer；
- `JILINJOBS_GUIDE_CARDS` → JilinJobs card renderer；
- existing external / internal renderer identities 按 current compatibility contract 解析；
- unknown key → explicit unsupported/error state + diagnosable evidence；
- unknown key 不得 fallback 到 Rich renderer；
- alias、path、group、DOM shape、body heuristic 均不得参与长期 dispatch。

## 7. Content ownership 与 Site Package

`guide/jypq` 的长期 Runtime lifecycle：

```text
Site Package accepted Structured default
→ Fresh create 或 exact-prior-baseline adoption
→ Runtime contentOwner=OPERATOR
→ operator-managed Structured payload
→ Public structured renderer
```

Site Package default 是 create/adoption source，不是 ordinary Runtime 的第二份 body authority。

## 8. Adoption compatibility

Legacy Rich predecessor 的 adoption precondition 必须同时满足：

```text
contentModel = RICH_TEXT
rendererKey = RICH_TEXT
contentOwner = OPERATOR
structuredPayload = null
legacy content fingerprint = package declared prior baseline
```

只在精确匹配时，才允许一个 transaction 原子更新：

```text
contentModel
rendererKey
contentOwner
bodyHtml          -> empty
structuredPayload -> CARD_COLLECTION V1
embedUrl          -> null
```

任何 precondition mismatch 都必须 preserve complete current content contract 并报告 protected divergence。

成功 adoption 后：

- rerun no-op；
- operator Structured edits survive ordinary reconcile；
- future package mismatch 继续保护 operator content；
- 不调用 Main Historical Migration fallback。

## 9. Admin authoring

Admin authoring 根据 `contentModel + structured schema` 选择 surface：

- `RICH_TEXT`：whole-body mature Rich Text editor；
- `STRUCTURED + CARD_COLLECTION V1`：ordered card editor；
- card title + item-level Rich Text body；
- 支持 card add / remove / reorder；
- 不提供并行 whole-page arbitrary HTML editor；
- preset Page 普通内容编辑不得切换 `contentModel / rendererKey / contentOwner`；
- unsupported schema/version 显示阻塞诊断。

## 10. Admin / Public API

Current Page DTO 应显式提供：

```text
contentModel
rendererKey
contentOwner
structuredContent
```

Existing identity、group、breadcrumbs、canonical URL 与 current compatibility fields 按现有 contract 保持。

`structuredContent` 在 HTTP boundary 输出 validated JSON data，而不是未经验证的 opaque JSON string。

## 11. URL 与 Replaceable Public Renderer

Current canonical routes：

```text
/page/{alias}
/page/{groupAlias}/{alias}
```

不因 renderer 新增第二套路由或 Page directory。Public Renderer 只执行声明的 contract，不成为 Site Definition / Page Product Authority。

## 12. Verification contract

至少覆盖：

- Current Flyway Page schema；
- Rich regression；
- Structured parse / validation / sanitization；
- renderer registry 与 unknown fail-closed；
- Admin card authoring / reorder / save / reload；
- Fresh Structured create；
- exact Rich baseline adoption；
- operator-diverged Rich preserve + report；
- adoption idempotency；
- post-adoption operator edit protection；
- `guide/jypq` 3 cards、accepted order / titles / body、4 package images；
- canonical `/page/guide/jypq`；
- automated Browser verification 后执行 bounded Human Review。

## 13. 非决策

本规格不自动授权 FAQ Structured migration、Hui Employment integration、Engineering Page sample、generic Page Builder、Main migration reactivation 或新的 Execution Unit。
