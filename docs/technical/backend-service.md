# CMS Backend 当前技术 Authority

## 1. 目标与责任边界

Backend 负责 Generic CMS Core 的业务校验、持久化、公开/管理 API、StaticResource 服务，以及 JilinJobs Site Package / Historical Migration 所需的通用 application capability。

跨模块配置责任统一遵循 `docs/technical/configuration-governance.md`；不得因为存在字面常量就机械增加系统配置。

当前 Backend Authority 必须区分：

- Generic Backend Flyway：Generic CMS Schema evolution 与 site-neutral provisioning capability；
- JilinJobs Site Package：stable structure、one-time bootstrap、stable assets；
- CMS Runtime：operator-managed content / configuration / uploads；
- Historical Content Migration：`data-migrations/**` 中的 canonical data、provenance 与 compatibility。

旧 V11 等 migration number 只属于历史实现 / upgrade 追溯，不再定义 Current initialization lifecycle。

## 2. Current Flyway / Site composition

Backend active Flyway 当前只包含：

```text
backend/src/main/resources/db/migration/
├── V1__current_cms_schema.sql
└── V2__site_provisioning_schema_capabilities.sql
```

V1 定义当前 Generic CMS Schema；V2 只增加 site-neutral provisioning capability。Backend Flyway 不写入 `notice`、`party`、`HOME_CAROUSEL` 或其他具体 JilinJobs Site instance rows。

JilinJobs 当前 Site ownership：

```text
sites/jilinjobs/
├── structure/**   # stable Site structure
├── bootstrap/**   # Fresh Site one-time operational defaults
└── assets/**      # stable Site asset source + integrity
```

正常 Runtime composition 必须保持以下边界：

- stable structure 通过 Site Package provision/reconcile 建立并维护稳定 identity；
- bootstrap 只在 Fresh Site 显式执行一次，成功后其数据成为普通 operator-managed Runtime data；普通 restart/reconcile/repeated bootstrap 不得覆盖或 resurrect 后续人工修改/删除；
- stable Site assets 从 `sites/jilinjobs/assets/**` 投影到公开 `/static/**` targets，并按 manifest/catalog 纳入受保护路径；
- Historical Content Migration 继续由 `data-migrations/**` 承担，不并入 Flyway、stable structure、bootstrap 或 stable asset manifest。

下一次 Generic CMS Schema change 从当前 active V2 之后 append-only 演进；不得通过恢复旧历史 migration 文件重新获得 JilinJobs instance data。

## 3. Column 与 Article

Column 使用共享 `ContentImagePolicy`：`NONE / OPTIONAL / REQUIRED`。

ArticleService 约束：

- INTERNAL + `NONE`：拒绝保存非空 `coverResourceId`；
- INTERNAL + `OPTIONAL`：封面可选；
- INTERNAL + `REQUIRED`：草稿创建/编辑可以暂时没有封面；执行 publish 前必须有封面；文章已经 PUBLISHED 时，普通 update 也必须继续满足封面契约；
- EXTERNAL_LINK：不复制本地正文、封面、正文图片与附件，不按栏目策略要求本地封面。

`Article.articleType`（`INTERNAL / EXTERNAL_LINK`）是创建时来源身份。Admin 编辑态只读；Backend update 必须独立拒绝切换 articleType。需要改变来源类型时创建新的 Article，而不是把既有对象改造成另一种来源语义。

Article 不再维护全局 `recommended`。公开文章默认排序使用 `pinned DESC → sortOrder DESC → published/actual time DESC → id DESC`。需要独立推荐/展示投放时使用 `CmsList + ARTICLE`。

Public Article Summary 保留可选 `coverResourceId`；公开查询在形成 summary 前取得必要 Resource association，使该字段来自真实 Article 当前资源关系。

## 4. Navigation

`cms_navigation_location` 保存运行时导航位置；NavigationItem 保存稳定 position/location identity、parent/children、目标、排序、启停、打开方式及可选 `iconPath`。

NavigationItem 保存时校验位置存在；父导航必须存在且位置相同；禁止循环。可选 `iconPath` 只接受 `/static/**` 路径，并随 Admin/Public API 输出。公开站不得根据列表索引计算图标路径。

JilinJobs `MAIN / HOME_SHORTCUT / HOME_QUICK` 等稳定 Navigation structure 与 provisioning code 由 `sites/jilinjobs/structure/**` 持有，不由 Generic Flyway seed。普通运营修改遵循 Site Package stable identity / preset 保护契约。

## 5. CmsList / CmsListItem

表：`cms_list`、`cms_list_item`。

列表 code 唯一；`cms_list` 不维护 display mode 或旧组合型 item type。`image_policy` 映射为 `ContentImagePolicy`。

`CmsListItem.sourceType` 只使用：

- `LINK`：自身维护标题、副标题、可选 URL、图片、打开方式、排序、启停和扩展数据；
- `ARTICLE`：通过 `articleId` 关联已有 Article，列表项保存展示/投放属性，不复制文章正文。

### 5.1 来源身份不可变

`sourceType` 属于创建时身份；ARTICLE 的 `articleId` 同样属于创建时来源身份。Backend `updateItem` 必须拒绝：

- LINK → ARTICLE；
- ARTICLE → LINK；
- ARTICLE 更换 `articleId`。

Admin 编辑态必须禁用这些身份字段。需要改变来源类型或关联 Article 时删除原项并创建新项。

### 5.2 ARTICLE relation / public projection

ARTICLE placement 不改变 Article 唯一栏目归属。公开列表只返回本身 enabled 且关联 Article 当前处于 `PUBLISHED` 的 ARTICLE 项；撤回 Article 后投放自动退出，重新发布后可按既有关系恢复。

Public projection 使用当前 Article 标题/状态：

- INTERNAL + Main：canonical target `/article/{id}`；
- INTERNAL + Party：canonical target `/party/article/{id}`；
- EXTERNAL_LINK：使用 Article 当前 external URL。

以上 target 继续服从列表项 `DEFAULT / SAME_WINDOW / NEW_WINDOW` `openMode`。

### 5.3 图片策略 / effective image

LINK 项按列表 `imagePolicy` 验证 `imagePath`。ARTICLE 可通过 `image_resource_id` 保存列表专用覆盖 Resource；为空时按当前 Article 封面计算 effective image，非空时使用显式覆盖。

后台可以把正文图片作为候选，但用户选中后必须把 Resource ID 固化为列表项覆盖；Public Runtime 不得隐式扫描“正文第一张”。覆盖图片不修改 Article 自身封面/正文。

`NONE` 不允许列表图片/ARTICLE override；`OPTIONAL` 允许最终无图；`REQUIRED` 必须形成有效图片。REQUIRED ARTICLE 后续失去继承封面且无有效 override 时，Backend Public query 必须排除该投放，直到重新形成有效图片。

更新列表定义时先检查既有 items，防止切换到 NONE / REQUIRED 后立即产生违反策略的已有数据。

## 6. Advertisement 技术模块 / 宣传展示产品模型

表继续使用 `cms_ad_slot`、`cms_advertisement`；API 路径保留 `/advertisements` 兼容。产品界面称“宣传展示位 / 展示内容”。

展示位 code 唯一；公开查询过滤 slot/item enabled 以及可选 start/end 时间。`openMode=NO_LINK` 时保留 URL 但禁止公开站生成点击跳转。

JilinJobs `HOME_RECRUITMENT_PROMO` 等 stable slot identity 属于 `sites/jilinjobs/structure/**`，不属于 Generic Flyway instance seed。

## 7. SiteProperty 与 CMS Metadata

继续复用 `cms_site_config` 表名；数据库保存 SiteProperty 的 `name/group_code/value/value_type/sort_order/required/system/enabled`，但“允许有哪些属性分组”不作为 CMS 数据表维护。

`CmsMetadataProperties` 通过 Spring `@ConfigurationProperties(prefix="cms.metadata")` 绑定 CMS metadata；当前至少提供 `BASIC / BRAND / CONTACT / FOOTER / PRESENTATION / GENERAL` 的名称与排序。

Admin `GET /api/admin/site-config/groups` 返回排序后的 group definitions。SiteConfigService 创建/修改属性定义时验证 `groupCode` 已存在，防止数据库产生 UI 无法解释的自由分组。

`valueType` 支持 `TEXT / RESOURCE_PATH / JSON / URL / BOOLEAN / INTEGER`。Service 根据数据库 valueType 做最终校验；不得通过编译期 key Enum 限制自定义属性 key。

当前统一轮播属性：

```text
CAROUSEL_INTERVAL_SECONDS
Group: PRESENTATION
Type: INTEGER
Default: 4
Constraint: > 0

CAROUSEL_MAX_ITEMS
Group: PRESENTATION
Type: INTEGER
Default: 5
Constraint: > 0
```

Backend 管理写入必须拒绝 0、负数和非整数。Public 对缺失、非法历史值或不大于 0 的数据分别 fallback 到 4 / 5。`CAROUSEL_MAX_ITEMS` 限制单个轮播前台最多消费的有效项，不限制后台列表记录数量。

`HOME_CAROUSEL_INTERVAL_SECONDS` 只属于 EU-30 前 superseded history，不是 Current Runtime property。

`RESOURCE_PATH` 继续只接受 `/static/**`。

## 8. StaticResource / Runtime upload / protected path

复用统一 StaticResource API，不为列表、宣传展示、导航再次实现上传后端。CMS Runtime uploads 使用：

- `/static/uploads/displays/{slotCode}/`；
- `/static/uploads/lists/{listCode}/`；
- `/static/uploads/site-properties/{key}/`；
- `/static/uploads/navigation-icons/`。

Runtime uploads 不属于 `sites/jilinjobs/assets/**` stable Site asset ownership。

上传执行扩展名 + 真实文件签名校验并自动创建父目录。允许扩展名和文件签名属于安全边界，保持代码契约，不提升为运营配置。

`protectedResource` 集合由 Backend 合并：

1. Spring 外部化配置声明的固定部署受保护资源；
2. Site Package stable asset manifest/catalog 自动提供的 stable `/static/**` targets；
3. Runtime 引用：启用的 RESOURCE_PATH SiteProperty、CmsList effective/override image、Advertisement image、Navigation iconPath 等当前直接引用。

受保护状态不是管理员人工维护的重要性等级，不增加 `protected=true` 运营字段。普通 DELETE 必须拒绝受保护资源；明确 replace 仍允许。该机制不宣称扫描所有 CSS / JS / 富文本引用，因此 Admin 对普通资源删除继续给出风险提示。

## 9. Historical Content Migration boundary

`data-migrations/**` 继续承担历史文章、外链、正文资源、附件、历史运营列表项、legacy identity / fingerprint 与 provenance。

Canonical Dataset 可以引用 Site Package 已 provision 的 Column alias / List code 等 stable identity；不得依赖临时 Runtime DB id，也不得把具体 JilinJobs / Party identity内建成 Generic CMS capability。

当前 Party accepted lineage 保持：EU-29 frozen acceptedSnapshot = 181 Articles；EU-30 promotion 后 current canonical Runtime Dataset = 183 Articles、4 accepted carousel items，并保留 EU-29 → EU-30 upgrade-only compatibility。该历史内容与兼容责任不回写进 Backend Flyway 或 stable Site asset manifest。

Phase 2 将另行收敛 Generic Content Migration Application / Backend application boundary；本文件当前只描述已接受的责任边界，不预先执行该后继架构工作。

## 10. Verification contract

Backend Current Evidence 至少按受影响范围覆盖：

- Column/Article：NONE/OPTIONAL/REQUIRED；REQUIRED draft/publish/edit；Article `articleType` immutable；Public Summary cover relation；公开排序不依赖 `recommended`；
- Navigation：location/parent/cycle/iconPath 与 stable identity provisioning；
- CmsList：LINK / ARTICLE create/update identity；ARTICLE published filtering；canonical target + openMode；effective image / override；NONE/OPTIONAL/REQUIRED policy transitions；
- SiteProperty：metadata group、自定义 key、typed value，以及两个 carousel key 的严格正整数约束；
- Advertisement：有效期与 `NO_LINK`；
- Generic Fresh Database active V1/V2 migration；
- Site Package stable structure / one-time bootstrap / stable assets composition；
- StaticResource 真实媒体、Runtime upload path、stable asset / config / Runtime reference protection；
- 受保护资源拒绝普通删除但允许明确替换。

验证必须区分 Generic Fresh DB、JilinJobs Site provision/bootstrap、Historical Migration 与 Runtime data，不得由测试 fixture 暗中重建另一套站点基线。

## 11. Historical implementation trace

EU-30 之前的 V11 `V11__cms_metadata_and_image_policies.sql` 曾承担 Column/CmsList image policy 与 `HOME_CAROUSEL_INTERVAL_SECONDS` 等阶段性 migration；后续 EU-30 又通过 append-only migration 完成 LINK / ARTICLE、carousel properties、recommended removal 等演进。EU-41 controlled development baseline replacement 后，这些历史 migration 不再位于 active Flyway lineage。

该历史只用于解释已完成实现和 upgrade evidence：不得从 V11 编号、旧 seed SQL 或旧 property name 反向推断 Current Backend initialization responsibility。