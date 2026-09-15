# 单页内容架构技术方案

## Authority

- `docs/requirements/page-content-architecture.md`
- `docs/specifications/page-content-architecture.md`
- `docs/technical/cms-site-package-boundary.md`
- `docs/technical/verification-strategy.md`
- GitHub Issue #77
- GitHub Issue #137（legacy structure evidence only）

## 状态

- Technical Authority：**CURRENT / ACCEPTED**；
- Architecture implementation：**COMPLETED via EU-55**；
- Current Flyway：V1～V4；
- Current representative Structured Page：`guide/jypq`；
- Execute Authority：**TERMINATED**。

本文保留完成后的长期 HOW，不拥有 Current Execution Gate。

## 1. Current persistence

EU-55 已完成 Page persistence 从混合 `render_mode` 表达向正交 content contract 的迁移。Current migration：

`backend/modules/cms-core/src/main/resources/db/migration/V4__page_content_architecture.sql`

Current `cms_page` content fields：

```text
body_html
content_model
renderer_key
content_owner
structured_payload
embed_url
```

V4 将旧 `render_mode` rename 为 `renderer_key`，并按旧值 deterministic backfill：

```text
RICH_TEXT          -> contentModel=RICH_TEXT, rendererKey=RICH_TEXT,         contentOwner=OPERATOR
EMBED_PLACEHOLDER  -> contentModel=NONE,      rendererKey=EMBED_PLACEHOLDER, contentOwner=EXTERNAL
INTERNAL_STATIC    -> contentModel=NONE,      rendererKey=INTERNAL_STATIC,   contentOwner=ENGINEERING
```

Migration 不重写 existing operator body bytes。

## 2. Generic domain values

Generic Core 当前语义：

```text
PageContentModel = RICH_TEXT | STRUCTURED | NONE
PageContentOwner = OPERATOR | SITE_PACKAGE | ENGINEERING | EXTERNAL
```

`rendererKey` 是 validated string，不是 Generic enum。具体 renderer registration 属于 replaceable Public Renderer。

Validation 保证一个 Page 只有一个 primary content representation：

- `RICH_TEXT` → `bodyHtml`；
- `STRUCTURED` → `structuredPayload`，top-level `bodyHtml` 为空；
- `NONE` → 不把 CMS content field 当作 whole-page primary body。

## 3. Structured Card Collection V1

Current first structured schema：

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

实现要求：

- preserve item order；
- title 必填、plain text；
- item `bodyHtml` 复用现有 Rich HTML sanitize policy；
- 不引入独立 card entity / ID；
- 不引入第二套 Resource model；
- unknown kind/version、malformed JSON、invalid item fail closed；
- canonical parse / validation 必须在 persistence / API / Public consumption 前完成。

当前需求没有 SQL-level card query、cross-page reuse、independent workflow 或 item-level identity，因此不引入 normalized `page_section/page_item` entities。

## 4. Renderer registry

Main Public Renderer 使用小型静态 registry / resolver：

```text
rendererKey -> renderer component
```

Current bindings 至少包括：

```text
RICH_TEXT               -> generic Rich renderer
JILINJOBS_GUIDE_CARDS   -> Structured card renderer
EMBED_PLACEHOLDER       -> current external placeholder/integration behavior
INTERNAL_STATIC         -> current engineering/internal behavior
```

Unknown key 必须显示 explicit unsupported state 并产生 diagnosable evidence；禁止 fallback 到 Rich `v-html`。

禁止用 alias、path、group、DOM shape 或 body content 作为 renderer selector。

## 5. Site Package representation

Site Package Page entries 使用相同 generic contract：

```json
{
  "groupAlias": "guide",
  "alias": "jypq",
  "name": "就业派遣",
  "contentModel": "STRUCTURED",
  "rendererKey": "JILINJOBS_GUIDE_CARDS",
  "contentOwner": "OPERATOR",
  "bodyHtml": "",
  "structuredPayload": {
    "schemaVersion": 1,
    "kind": "CARD_COLLECTION",
    "items": []
  },
  "embedUrl": null,
  "contentAdoptionFromFingerprint": "<accepted prior Rich baseline>"
}
```

Current `sites/jilinjobs/structure/pages.json` 已按此语义存储 `guide/jypq`。JilinJobs-specific content / renderer binding 留在 `sites/jilinjobs/**` 与 Main Public renderer；Generic Core 不知道 `jypq` alias 或 card 文案。

## 6. Rich → Structured adoption

Current adoption safety 继承 EU-49 / EU-52 ownership guard。

对 `guide/jypq`，只有 Existing Page 精确满足 legacy Rich predecessor 时允许转换：

```text
contentModel = RICH_TEXT
rendererKey = RICH_TEXT
contentOwner = OPERATOR
structuredPayload = null
legacy content fingerprint = declared prior-package fingerprint
```

成功分支在一个 DB transaction 中原子更新完整 content contract：

```text
contentModel = STRUCTURED
rendererKey = JILINJOBS_GUIDE_CARDS
contentOwner = OPERATOR
bodyHtml = empty
structuredPayload = accepted CARD_COLLECTION V1
embedUrl = null
```

不匹配时：

- preserve Runtime content；
- report protected divergence；
- 不通过 alias/DOM/source heuristic 强制认领；
- 不调用 Main Historical Migration fallback。

Adoption 后 repeated reconcile no-op；operator 修改 Structured payload 后 ordinary reconcile 继续保护其修改。

## 7. Admin authoring

Admin 只根据 content contract 选择 authoring UI：

- `RICH_TEXT` → current mature `RichTextEditor`；
- `STRUCTURED + CARD_COLLECTION V1` → ordered card editor；
- card body 继续使用 item-level `RichTextEditor`；
- add/remove/reorder 保持 atomic Page save；
- 不提供平行 whole-page Rich HTML editor；
- preset Page 普通 edit 不允许切换 content model / renderer / owner；
- unsupported schema/version → blocking diagnostic / read-only safety state。

不建立第二套 rich-text engine。

## 8. Admin / Public API

Current Admin/Public Page DTO 显式表达：

```text
contentModel
rendererKey
contentOwner
structuredContent
```

Existing Page identity、group、breadcrumbs、canonical URL 与仍在 compatibility window 内的 legacy projection 按当前 implementation contract 保持。

Structured payload 在 HTTP boundary 输出 validated typed JSON data；Public client 不消费未经验证的 opaque string。

## 9. Public routing / SEO

Routes 保持：

```text
/page/{alias}
/page/{groupAlias}/{alias}
```

`PublicPageView` 继续拥有 route / shell / group tab / breadcrumb responsibility，再把 body 委托给 renderer resolver。

Structured Page 的 SEO / summary 从 validated structured contract 提取文本，不依赖空 top-level `bodyHtml`。

## 10. Historical Migration boundary

Page Content Architecture evolution 使用 Generic Flyway + Site Package create/adoption；Main Historical Migration 不参与 fallback。

`cms_page_legacy_mapping` 只承担已有 historical provenance / mapping 责任，不成为 Page schema evolution 的第二套内容来源。

## 11. Current verification

后续修改本能力时按风险至少覆盖：

### Persistence / domain

- Fresh DB V1～V4 complete chain；
- upgrade fixture 对 legacy renderer deterministic mapping；
- Rich create/read/update；
- Structured parse / validation / canonical serialization；
- unknown model / owner / kind / version fail closed；
- one-primary-body validation。

### Public renderer

- Rich renderer regression；
- `JILINJOBS_GUIDE_CARDS` dispatch；
- unknown renderer explicit failure；
- no alias/path/DOM fallback。

### Admin

- `about` mature Rich editor；
- `guide/jypq` card editor only；
- card title/body edit + reorder + reload；
- item-level Rich resource behavior；
- invalid payload visible blocking diagnostics。

### Site Package / adoption

- Fresh create Structured target；
- exact legacy Rich baseline adoption；
- operator-diverged predecessor preserve + report；
- already-current no-op；
- rerun idempotency；
- post-adoption operator Structured edits survive ordinary reconcile。

### Product contract

- `guide/jypq` exactly 3 accepted cards；
- accepted order / titles / body；
- existing 4 package image URLs / bytes remain manifest-backed；
- canonical `/page/guide/jypq`；
- browser renders through Structured renderer；
- automated evidence precedes bounded Human Review when visual acceptance is affected。

## 12. Stage Return

出现以下任一新 Current Evidence 时，必须回到 Requirement / Specification，而不是实现隐藏 special case：

- card model 无法表达真实 required interaction/state；
- Structured item 需要独立 workflow / identity / query lifecycle；
- operator-owned Structured data 与 Product ownership 冲突；
- 必须同时依赖 Structured payload 与 top-level Rich/renderer hardcode 才能工作；
- safe prior-baseline adoption 无法成立；
- renderer 必须通过 alias/path/DOM dispatch 才能满足产品行为。

## 13. 非目标

- generic Page Builder；
- normalized card entities without new evidence；
- FAQ automatic Structured migration；
- Hui Employment production integration；
- Engineering Page sample；
- arbitrary operator switching among model / renderer / owner；
- Structured → Structured implicit package overwrite protocol；
- new Page Resource relation；
- Public frontend technology replacement；
- Main Historical Migration reactivation；
- Production Deployment / Release；
- 从本文恢复任何已结束的 EU-55 Execute Authority。
