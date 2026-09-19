---
id: technical:http-interface-contract
type: technical-contract
status: active
relations:
  requirements:
    - docs/requirements/information-publishing.md
    - docs/requirements/cms-domain.md
  specifications:
    - docs/specifications/admin-site.md
    - docs/specifications/public-site.md
    - docs/specifications/page-content.md
    - docs/specifications/rich-text-authoring.md
  architecture:
    - docs/architecture/cms-architecture.md
  technical:
    - docs/technical/backend-service.md
    - docs/technical/admin-frontend.md
    - docs/technical/public-site-frontend.md
  verification:
    - docs/technical/verification-strategy.md
updated_at: 2026-09-16
---

# CMS HTTP 接口兼容契约

## 1. 文档责任

本文是 `jilinjobs-cms` 当前 **Backend ↔ Admin/Public Frontend HTTP compatibility** 的唯一 Current Technical owner，回答“替换 Backend implementation 时，现有 Admin / Public consumer 仍需要哪些稳定 HTTP surface、wire projection 与失败语义”。

Product / Domain Requirement 拥有业务对象、identity、lifecycle 与产品级质量；Specification 拥有用户可观察行为、失败行为 与 验收；Architecture 拥有 Core / Server / Migration、Admin / Public 与 replaceability boundary。本文只拥有 transport compatibility，不反向成为业务事实来源。

Controller class、TypeScript adapter 文件、Spring annotation、Node framework、package path、当前测试文件和代码生成方式都不是本文的长期事实。代码可以作为当前实现 证据；一旦与本文冲突，必须判断 implementation defect 或 contract change，不能让某个 adapter / Controller 自动成为第二接口 Authority。

## 2. Namespace 与 transport 基线

当前稳定 namespace：

```text
/api/admin/**    CMS authoring / management HTTP contract
/api/public/**   Public read projection / managed public resource contract
/static/**       CMS-managed public static resource namespace
```

这些 API path 与 Product Requirement / Public Specification 中的 canonical browser URL 是不同层次：`/article/{id}`、`/page/**`、`/party/**` 等 browser route 不因为 Backend framework 替换而变成 `/api/**` contract。

JSON contract 基线：

- JSON property 使用当前 camelCase wire name；
- numeric identity 以 JSON number 传递；
- nullable relation / optional target 使用 `null`，不使用伪造的 `0` / empty object 表示“不存在”；
- enum / identity token 保持当前 Domain 定义的稳定字符串，例如 `INTERNAL`、`EXTERNAL_LINK`、`DRAFT`、`PUBLISHED`、`WITHDRAWN`、`RICH_TEXT`、`STRUCTURED`、`NONE`；
- `LocalDate` compatible value 使用 `YYYY-MM-DD`；date-time value 保持 ISO-compatible string；
- validation、not-found、upload-size 等受控 JSON failure 提供至少 `{ "message": string }` 的可诊断错误 envelope；
- Domain / input validation 映射为 `400`，当前资源不存在映射为 `404`，上传体超过 Server 限制映射为 `413`；
- 未受控的 provider / platform `5xx` 只要求 consumer 能识别请求失败，不把 Spring 或其他 provider 的默认 error body 提升为稳定 wire contract；
- 普通 create endpoint 在当前 contract 中返回 `201` + created representation；普通 update / read 返回 `200`；明确无 response body 的 delete 返回 `204`；
- binary resource 返回真实 content type；attachment projection 使用下载语义并保留原文件名。

具体错误中文文案只有在 Specification 明确要求时才属于稳定用户 contract；Backend replacement 必须保持上述受控失败的可诊断语义和 envelope，不要求逐字复制实现消息，也不要求模拟旧 provider 的未知 `5xx` body。

## 3. 管理端 endpoint 族

下表拥有当前 Admin consumer 需要的稳定 HTTP family。`{id}` 为 numeric object identity，`{code}` / `{key}` 使用 URL encoding。

| Capability | Current HTTP surface |
|---|---|
| Column | `GET/POST /api/admin/columns`；`PUT/DELETE /api/admin/columns/{id}` |
| Article | `GET/POST /api/admin/articles`；`GET/PUT /api/admin/articles/{id}`；`POST /api/admin/articles/{id}/publish`；`POST /api/admin/articles/{id}/withdraw` |
| NavigationLocation | `GET/POST /api/admin/navigation-locations`；`PUT/DELETE /api/admin/navigation-locations/{code}` |
| NavigationItem | `GET/POST /api/admin/navigations`；`PUT/DELETE /api/admin/navigations/{id}` |
| PageGroup | `GET/POST /api/admin/page-groups`；`PUT /api/admin/page-groups/{id}` |
| Page | `GET/POST /api/admin/pages`；`PUT/DELETE /api/admin/pages/{id}` |
| CmsList | `GET/POST /api/admin/lists`；`PUT/DELETE /api/admin/lists/{id}` |
| CmsListItem | `GET/POST /api/admin/lists/{id}/items`；`PUT/DELETE /api/admin/lists/{listId}/items/{itemId}` |
| AdvertisementSlot | `GET/POST /api/admin/advertisements/slots`；`PUT/DELETE /api/admin/advertisements/slots/{id}` |
| Advertisement | `GET/POST /api/admin/advertisements/slots/{id}/items`；`PUT/DELETE /api/admin/advertisements/slots/{slotId}/items/{adId}` |
| SiteProperty | `GET/POST /api/admin/site-config`；`GET /api/admin/site-config/groups`；`PUT/DELETE /api/admin/site-config/{key}`；`PUT /api/admin/site-config/{key}/definition` |
| Managed Resource | `POST /api/admin/resources` multipart field `file`；`GET /api/admin/resources/{id}`；`GET /api/admin/resources/{id}/content` |
| StaticResource | `GET /api/admin/static-resources?path=...`；`POST /api/admin/static-resources?path=...&replace=...` multipart field `file`；`DELETE /api/admin/static-resources?path=...`；`GET /api/admin/static-resources/trash`；`POST /api/admin/static-resources/restore/{id}` |

Article list query 支持当前 Admin filtering / paging contract：`keyword`、`columnId`、`status`、`articleType`、`page`、`size`；未指定 page / size 时保持零基页码与当前默认 page size 语义。

Admin Article query 的 `columnId` 表示**所选栏目子树**：结果包含该 Column 自身以及当前全部 descendant Column 中满足其余过滤条件的 Article。该语义对应 Admin Specification 的“选择父栏目时聚合其后代栏目文章”，不是 exact-column filter。`columnId = null` 表示不按栏目限制。

Article mutation endpoint 只负责把 Domain lifecycle 投影为稳定 HTTP action：

- `POST /api/admin/articles` 创建后返回 `DRAFT` Article；
- ordinary `PUT /api/admin/articles/{id}` 更新内容但保持当前 publish status，不隐式 publish / withdraw；
- `POST /api/admin/articles/{id}/publish` 将当前允许发布的 `DRAFT` 或 `WITHDRAWN` Article 转为 `PUBLISHED`；
- `POST /api/admin/articles/{id}/withdraw` 只对当前 `PUBLISHED` Article 成功，并转为 `WITHDRAWN`；
- 不满足 Domain transition / publication precondition 时返回受控 validation failure，而不是静默改写成其他状态。

这些规则是 Domain lifecycle 在现有 HTTP surface 上的 compatibility projection；Domain 仍由 `docs/requirements/cms-domain.md` 持有。

仅在 frontend adapter 中声明、但没有当前 Backend projection / active consumer 证据的 helper endpoint **不自动进入本 contract**。例如 endpoint 增删必须以真实 Current consumer + provider contract 为依据，而不是因为某个 TypeScript function 曾存在就永久保留。

## 4. 公开端 endpoint 族

Public frontend 只消费 read / resource projection，不依赖 Admin mutation contract：

| Capability | Current HTTP surface |
|---|---|
| Column | `GET /api/public/columns/{id}`；`GET /api/public/columns/by-alias/{alias}` |
| Article | `GET /api/public/articles?columnId=&articleType=&page=&size=`；`GET /api/public/articles/{id}` |
| Navigation | `GET /api/public/navigations` |
| Page | `GET /api/public/pages/{alias}`；`GET /api/public/page-groups/{groupAlias}`；`GET /api/public/page-groups/{groupAlias}/{alias}` |
| CmsList | `GET /api/public/lists`；`GET /api/public/lists/by-code/{code}`；`GET /api/public/lists/by-group/{groupCode}` |
| Advertisement | `GET /api/public/advertisements`；`GET /api/public/advertisements/slots/{code}` |
| SiteProperty | `GET /api/public/site-config` |
| Managed Resource | `GET /api/public/resources/{id}/content`；`GET /api/public/resources/{id}/attachment` |
| StaticResource | `GET /static/{path...}` |

Public Article query 的 `columnId` 是**exact-column** filter：只返回 primary Column 等于该 id 且当前可公开的 Article；它不会像 Admin query 一样自动扩展到 descendant Column。`columnId = null` 表示不按栏目限制。`articleType` 是对当前公开 Article source type 的 exact filter。

当前 HTTP surface 不引入单独的 `site=MAIN|PARTY` query 来决定浏览器 route / template scope。Main / Party 的 route / theme / template scope 继续由 Public Renderer 的 Site-specific responsibility 持有，但其判断必须使用当前 Authority 接受的 stable business relation / Site Definition identity 并 失败关闭，不能通过 URL 文本、DOM 或历史 typeCode heuristic 猜测。Backend Public projection仍负责 Domain publish lifecycle、endpoint-defined query scope 与 effective-content filtering；Renderer 不能通过读取 Admin/full-data projection来补偿这些 Backend contract。

Public list / advertisement 的 by-code / by-group projection 是 Main homepage 等当前运行 consumer 的稳定依赖；Backend replacement 不得只实现“全量列表再让前端过滤”来改变已有 scope contract。

## 5. 稳定传输投影

以下 projection 名称只是本文中的 contract locator；Backend implementation 不要求使用相同 class / TypeScript interface 名称，但 JSON wire shape 必须兼容现有 consumer。

### 5.1 栏目（`Column`）

```text
CmsColumn
  id, parentId, name, sortOrder, enabled, alias, coverPolicy, preset

PublicColumn
  id, parentId, name, alias, coverPolicy
```

### 5.2 文章 / 资源（`Article` / `Resource`）

```text
CmsArticle
  id, columnId, title, bodyHtml, source, articleType, externalUrl,
  publishDate, pinned, sortOrder, status, actualPublishedAt, viewCount,
  updatedAt, coverResourceId, bodyImageResourceIds, attachmentResourceIds

AdminArticleSummary
  id, columnId, title, source, articleType, publishDate, status, viewCount, updatedAt

AdminArticlePage
  items, page, size, total

PublicArticleSummary
  id, columnId, columnName, columnAlias, title, source, articleType,
  externalUrl, publishDate, pinned, sortOrder, coverResourceId

PublicArticleDetail
  id, columnId, columnName, columnAlias, title, bodyHtml, source,
  articleType, externalUrl, publishDate, bodyImageResourceIds, attachments

PublicArticleAttachment
  id, originalFilename, contentType, sizeBytes

CmsResource
  id, storageKey, originalFilename, contentType, sizeBytes
```

Article write payload 保持：`columnId`、`title`、`bodyHtml`、`source`、`articleType`、`externalUrl`、`publishDate`、`pinned`、`sortOrder`、`coverResourceId`、`bodyImageResourceIds`、`attachmentResourceIds`。

### 5.3 导航

```text
NavigationLocation
  id, code, name, description, sortOrder, enabled, system, preset

CmsNavigation
  id, parentId, name, position, category, targetType,
  targetColumnId, targetPageId, targetUrl, openMode,
  sortOrder, enabled, iconPath, preset

PublicNavigation
  id, parentId, name, position, category, sortOrder, targetType,
  href, external, newWindow, clickable, iconPath
```

Navigation write payload 保持上述 editable fields；Public projection 直接提供 resolved `href / external / newWindow / clickable`，Public Renderer 不重新实现 target resolution 业务规则。

### 5.4 页面

```text
CmsPageGroup
  id, alias, name, sortOrder, enabled, preset

CmsPage
  id, groupId, alias, name, bodyHtml,
  contentModel, rendererKey, contentOwner, structuredContent,
  renderMode, embedUrl, sortOrder, enabled, preset

PageStructuredContent
  schemaVersion, kind, items[]
PageStructuredCard
  title, bodyHtml

PublicPageGroup
  alias, name, members[] { alias, name, href, sortOrder }

PublicPage
  id, alias, name, bodyHtml,
  contentModel, rendererKey, contentOwner, structuredContent,
  renderMode, embedUrl, canonicalUrl, group,
  breadcrumbs[] { title, href }
```

`renderMode / embedUrl` 是当前 legacy compatibility projection；它们不是新的 Domain owner，但在 consumer 尚未退出前属于 wire compatibility。移除必须作为显式 contract migration，不得在 Backend technology substitution 中顺带删除。

### 5.5 列表 / 广告（`CmsList` / `Advertisement`）

```text
CmsListDefinition
  id, code, name, groupCode, imagePolicy, description,
  sortOrder, enabled, system, preset

CmsListItem
  id, listId, sourceType, articleId, articleType, articleStatus,
  title, subtitle, url, imagePath, imageResourceId,
  effectiveImageResourceId, openMode, sortOrder, enabled, extraJson

PublicCmsList
  id, code, name, groupCode, imagePolicy, items

AdvertisementSlot
  id, code, name, description, sortOrder, enabled, system, preset

Advertisement
  id, slotId, title, imagePath, url, openMode,
  startAt, endAt, sortOrder, enabled

PublicAdvertisementSlot
  id, code, name, advertisements
```

Public projection 必须已经应用当前 Domain lifecycle / effective-item rules；Frontend 不通过获取 Admin 全量数据后自行修复 publish / effective semantics。

### 5.6 站点属性 / 静态资源（`SiteProperty` / `StaticResource`）

```text
SiteConfigItem
  key, name, groupCode, value, valueType, description,
  sortOrder, required, system, enabled, preset   # Admin projection

Public SiteConfigItem
  key, name, groupCode, value, valueType, description,
  sortOrder, required, system, enabled

SitePropertyGroupDefinition
  code, name, order

StaticEntry
  path, name, directory, size, modifiedAt, protectedResource

TrashEntry
  id, originalPath
```

SiteProperty typed business validity 仍由 Domain / Backend write boundary保证；Public frontend 可以对 presentation parameter 做 defensive interpretation，但不能因此改变 persisted value contract。

## 6. 资源 / multipart 兼容性

Managed Resource 与 StaticResource upload 使用 `multipart/form-data`，当前 file part 名为 `file`。HTTP transport 层负责把 multipart request 转换为 Core 可消费的 framework-neutral file input；`MultipartFile`、Servlet request 或其他 Spring Web type 不属于 Generic Core contract。

Public managed image：`/api/public/resources/{id}/content`；Public attachment：`/api/public/resources/{id}/attachment`。正文中由 Admin resource URL 写入的 managed image 在 Public projection/render chain 中必须能够解析为公开资源，不要求 Public 加载 Admin-only route。

`/static/**` 是 CMS-managed static public namespace；其 path safety、允许类型与真实媒体校验由 Product / Domain / Backend enforcement 共同约束，但具体 filesystem / object-storage implementation 可替换。

## 7. 后端技术替换接缝

Backend implementation 可以从 Java / Spring 替换为 Node.js 或其他技术，只要：

1. 不改变 Requirement / Domain / Specification / Architecture；
2. 保持本文 endpoint、method、query、status、JSON field、nullability、enum token、pagination、error envelope、multipart 与 binary resource compatibility；
3. Admin / Public frontend 不需要因为 Backend language/framework 替换而改变业务 adapter；
4. Core-equivalent domain capability 与 HTTP transport adaptation 保持分离，transport framework type 不泄漏为 shared domain/service contract；
5. Backend Public projection继续执行 Domain publish lifecycle、本文 endpoint 定义的 query scope 与 effective-content filtering；Main / Party route / theme / template scope 继续由 Public Renderer 的 Site-specific responsibility 按稳定关系执行，Renderer 不通过 Admin/full-data fallback 修复 Backend projection；
6. deployment / process / persistence implementation可以不同，只要产品与接口 contract成立。

Backend technology substitution dry-run 应把本文作为稳定 HTTP input，而不是读取 Java Controller / Kotlin model 后反推 contract。

## 8. 契约演进 / 失败关闭

以下变化属于 Interface Contract change：

- 删除或改名当前 active consumer endpoint；
- method、query meaning、status semantics 发生不兼容变化；
- 删除 / 改名 active consumer 所需 wire field；
- nullable / enum / pagination / binary / multipart语义变化；
- Admin-only data 泄漏进 Public contract，或 Public consumer 被迫依赖 Admin route；
- Backend replacement 要求 frontend 理解新的 provider-specific mechanism。

新增 consumer 不使用的 response field通常可以是 compatible additive change；是否需要晋升本文取决于它是否成为跨 implementation 持续需要的稳定 seam。

发现代码、client adapter、测试与本文不一致时，先建立 Expected vs Actual 并按 Verification Strategy 分类；不得静默选择其中一个作为新事实。真实产品语义歧义返回 Requirement / Specification；结构边界歧义返回 Architecture；纯 transport compatibility 在本文收敛。
