# CMS Backend 当前技术 Authority

## 1. 目标与应用边界

Backend 负责 Generic CMS Core 的业务校验、持久化、transaction、Site Package capability、Admin/Public transport 与 Historical Migration application capability。

Current physical topology：

```text
backend/
├── modules/
│   └── cms-core
└── apps/
    ├── cms-server
    └── content-migration
```

依赖方向：

```text
cms-server -> cms-core
content-migration -> cms-core
```

- Generic domain / persistence / Flyway / Site Package implementation 位于 `cms-core`；
- Admin/Public HTTP Controller 与 Server-only startup 位于 `cms-server`；
- Generic canonical import command 与 Party compatibility adapter 位于 `content-migration`；
- ordinary Server 不承担 Historical Migration implementation lifecycle；
- Migration application 不依赖 Server application，也不启动 HTTP server。

## 2. Current Flyway / Site composition

Current Flyway directory：

`backend/modules/cms-core/src/main/resources/db/migration/`

Current active migrations：

```text
V1__current_cms_schema.sql
V2__site_provisioning_schema_capabilities.sql
V3__page_content_migration_mapping.sql
V4__page_content_architecture.sql
```

Backend Flyway 只包含 site-neutral Generic CMS schema / capability，不写 JilinJobs instance data。

JilinJobs ownership：

```text
sites/jilinjobs/
├── structure/**   # stable structure / accepted Page defaults
├── bootstrap/**   # one-time ordinary Runtime defaults
└── assets/**      # stable Site assets
```

后续 Generic Schema change 从 V4 之后 append-only 演进。旧 V11/V12/V19、EU-31/EU-41 的 historical lineage 只用于 traceability，不定义 Current migration numbering。

## 3. Column 与 Article

Column 使用 `ContentImagePolicy`：`NONE / OPTIONAL / REQUIRED`。

ArticleService 必须保持：

- INTERNAL + `NONE`：不允许保存封面引用；
- INTERNAL + `OPTIONAL`：封面可选；
- INTERNAL + `REQUIRED`：draft 可暂时无封面，但 publish 前必须具备；已发布文章普通 update 也必须继续满足；
- EXTERNAL_LINK：不复制本地正文、封面、正文图片与附件，不按 Column cover policy 强制本地封面。

`Article.articleType`（`INTERNAL / EXTERNAL_LINK`）是创建时来源身份，普通 update 不允许切换。

Article 不使用全局 `recommended`。公开默认排序继续使用 `pinned → sortOrder → published/actual time → id` 的 accepted contract。独立推荐 / 展示使用 `CmsList + ARTICLE`。

Public Article Summary 的可选 `coverResourceId` 必须来自真实 Article Resource relation。

## 4. Page

Current Page domain 使用正交 Page Content Architecture：

```text
contentModel
rendererKey
contentOwner
bodyHtml
structuredPayload
embedUrl
```

- `RICH_TEXT`：`bodyHtml` 是 primary body；
- `STRUCTURED`：`structuredPayload` 是 primary body，top-level `bodyHtml` 为空；
- `NONE`：不把 CMS content field 当作 whole-page body；
- unknown model / structured schema 必须 fail closed。

Current `guide/jypq` 使用 `STRUCTURED / JILINJOBS_GUIDE_CARDS / OPERATOR / CARD_COLLECTION V1`。

Page stable identity / preset protection、operator divergence 与 Site Package adoption 继续遵循 `docs/requirements/page-content-architecture.md` 与 `docs/technical/cms-site-package-boundary.md`。

## 5. Navigation

`cms_navigation_location` 保存位置；NavigationItem 保存 parent/children、target、sort、enabled、open mode 与 optional `iconPath`。

约束：

- parent 必须存在且属于同 location；
- 禁止循环；
- `iconPath` 只接受 `/static/**`；
- Site Package stable Navigation identity 通过 provisioning-only stable code / preset contract 建立；
- ordinary Admin-created item 不自动成为 package-owned preset；
- Public 不得按数组 index 推导 icon。

## 6. CmsList / CmsListItem

`CmsListItem.sourceType`：

- `LINK`：自身维护 title/subtitle/url/image/openMode/sort/enabled/extra；
- `ARTICLE`：通过 `articleId` 引用 Article，列表项只持有 placement / presentation properties。

来源身份不可变：

- LINK ↔ ARTICLE 普通 update 禁止切换；
- ARTICLE `articleId` 普通 update 禁止更换；
- 需要改变来源时删除并重新创建。

ARTICLE public projection：

- 仅关联 `PUBLISHED` Article 的 enabled item 公开；
- INTERNAL target 由消费 Site 形成 canonical route；
- EXTERNAL_LINK target 使用 Article 当前 external URL；
- placement 不改变 Article `columnId`。

图片：

- LINK 使用 `imagePath`；
- ARTICLE 可用 `imageResourceId` override，否则继承 Article 可用封面；
- `NONE / OPTIONAL / REQUIRED` 由 parent `CmsList.imagePolicy` 约束；
- REQUIRED ARTICLE 如果最终无有效图片则不进入 Public result。

### 6.1 Main ListItem bootstrap

Main `HOME_CAROUSEL` 与 SITE_LINKS 初始化数据由 `sites/jilinjobs/bootstrap/**` 一次性创建。完成后是 ordinary operator-managed Runtime Data；Site Package 不 reconcile / resurrect 它们，也不存在 current stable ListItem membership capability gap。

Party Historical ListItem 继续按 Party migration Authority 处理。

## 7. Advertisement

技术模型继续使用 `AdvertisementSlot / Advertisement`、`cms_ad_*` 与 `/advertisements` API；产品界面称“宣传展示位 / 展示内容”。

Public filtering 继续考虑 slot/item enabled、optional start/end；`NO_LINK` 即使保存 URL 也不得产生点击 target。

JilinJobs stable slot identity 由 Site Package structure 定义，不进入 Generic Flyway seed。

## 8. SiteProperty / CMS metadata

继续复用 `cms_site_config` 表与 SiteProperty product terminology。

Current PRESENTATION properties：

```text
CAROUSEL_INTERVAL_SECONDS
Type: INTEGER
Default: 4
Constraint: > 0

CAROUSEL_MAX_ITEMS
Type: INTEGER
Default: 5
Constraint: > 0
```

`HOME_CAROUSEL_INTERVAL_SECONDS` 只属于 superseded history，不承担 Current Runtime responsibility。

Website property group definitions 来自 `cms-metadata.yml` / Spring `CmsMetadataProperties`，普通 SiteProperty 必须引用已定义 group；不得用 key Enum 限制自定义 property key。

`RESOURCE_PATH` 只接受 `/static/**`。

## 9. StaticResource / Runtime upload / protected path

Runtime upload examples：

- `/static/uploads/displays/{slotCode}/`；
- `/static/uploads/lists/{listCode}/`；
- `/static/uploads/site-properties/{key}/`；
- `/static/uploads/navigation-icons/`。

上传必须执行 extension + real media signature validation 与 safe normalized path 检查。

Current protected-resource 集合至少合并：

1. Spring externalized configuration 声明的 fixed protected paths；
2. Site Package stable asset manifest/catalog 的 `/static/**` targets；
3. CMS Runtime direct references，例如 enabled RESOURCE_PATH SiteProperty、CmsList effective image、Advertisement image、Navigation iconPath。

普通 DELETE 必须拒绝 protected resource；明确 replace 可以按当前 contract 更新。该机制不宣称建立完整 Rich HTML/CSS/JS reference graph。

## 10. Historical Content Migration

`data-migrations/**` 持有 historical canonical data、provenance、legacy identity / fingerprint 与 compatibility。

Current Content Migration application 已使用 site-neutral Generic Engine + bounded Party adapter；旧 EU-46～EU-48 Planning 文档只保留历史 traceability。

Main historical migration 当前冻结，accepted Runtime import scope 为 Article-only；Page / Main bootstrap ListItem 不进入 Main canonical import。

## 11. Verification contract

Backend Current Evidence 按受影响范围至少覆盖：

- Column/Article policy / identity / public projection；
- Page content model / structured payload / renderer contract；
- Navigation integrity / stable identity；
- CmsList LINK/ARTICLE identity、effective image、public filtering；
- SiteProperty typed validation 与 carousel properties；
- Advertisement validity / NO_LINK；
- Fresh MySQL full V1～V4 Flyway chain；
- Site Package stable structure / bootstrap / assets；
- StaticResource real media / safe path / protected-resource；
- Generic/Party Historical Migration（受影响时）；
- `cms-server` / `content-migration` application boundary。

验证必须区分 Generic Fresh DB、JilinJobs Site composition、Historical Migration 与 ordinary Runtime data，不得由测试 fixture 暗中重建另一套站点基线。

## 12. 历史说明

EU-30 前 V11/V12/V19 等 migration、EU-31 / EU-41 的 baseline replacement、以及单体 Backend / Party-specific migration implementation 均只用于解释演进历史。Current technical decisions 必须从 V1～V4 active lineage、multi-project Backend、Current Site Package 与 Generic Migration implementation 恢复，不得从旧编号或旧文件路径反向推导 Current responsibility。