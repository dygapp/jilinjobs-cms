# CMS Backend 技术计划（Technical Plan）

## 1. 目标

Backend 负责 CMS Core 的业务校验、持久化、公开/管理 API 和静态资源服务。

跨模块配置责任统一遵循 `docs/technical/configuration-governance.md`；不得因为存在字面常量就机械增加系统配置。

## 2. Column 与 Article

`cms_column` 通过 V11 增加 `cover_policy VARCHAR(16) NOT NULL DEFAULT 'OPTIONAL'`，模型映射为共享 `ContentImagePolicy`：`NONE / OPTIONAL / REQUIRED`。

ArticleService 约束：

- INTERNAL + `NONE`：拒绝保存非空 `coverResourceId`；
- INTERNAL + `OPTIONAL`：封面可选；
- INTERNAL + `REQUIRED`：草稿创建/编辑可以暂时没有封面；执行 publish 前必须有封面；文章已经 PUBLISHED 时，普通 update 也必须继续满足封面契约，不能通过编辑删除封面或转移到不满足约束的状态；
- EXTERNAL_LINK：继续清空本地正文、封面、正文图片与附件，不按栏目策略要求本地封面。

Public Article Summary 增加可选 `coverResourceId`。`listPublic` 在形成 summary 前补齐 ArticleResourceAssociation，确保公开摘要真正得到封面引用，而不是只增加 DTO 字段。

## 3. Navigation

`cms_navigation_location` 保存运行时导航位置；`cms_navigation.position` 保存位置 code，不使用编译期位置 Enum。

NavigationItem 保存时校验位置存在；父导航必须存在且位置相同；禁止循环。可选 `icon_path` 只接受 `/static/**` 路径，并随 Admin/Public API 输出。公开站不再根据列表索引计算图标路径。

现有 HOME_SHORTCUT / HOME_QUICK 业务图标通过 Flyway 将版本化 `/static/icons/**` 路径回填到对应导航条目。

## 4. CmsList

表：`cms_list`、`cms_list_item`。

列表 code 唯一。`cms_list` 不再维护 `item_type`；V11 增加 `image_policy`，映射为 `ContentImagePolicy`。列表项 title 保持必填，subtitle、URL 等独立可选；URL 存在时允许站内 `/...` 或 HTTP(S)。

图片规则由所属列表 imagePolicy 统一校验：

- NONE：`imagePath` 必须为空；
- OPTIONAL：可空，存在时必须为 `/static/**`；
- REQUIRED：必须提供 `/static/**` 图片路径。

更新列表定义时先检查既有 items：若切换 NONE 而仍存在图片，或切换 REQUIRED 而仍存在缺图项，则拒绝更新。该校验位于 Backend，不能只依赖 Admin 表单。

Admin API 提供列表和列表项 CRUD；Public API 返回启用列表、imagePolicy 和启用项。具体页面如何展示图片由 Public Site 决定。

V11 基线将 `HOME_CAROUSEL` 设为 REQUIRED，将当前 `SITE_RELATED / SITE_REGIONAL_GRADUATES / SITE_JILIN_UNIVERSITIES` 设为 NONE。

## 5. Advertisement 技术模块 / 宣传展示产品模型

表继续使用：`cms_ad_slot`、`cms_advertisement`；API 路径继续保留 `/advertisements` 兼容。产品界面称“宣传展示位 / 展示内容”。

展示位 code 唯一；公开查询过滤 slot/item enabled 以及可选 start/end 时间。`openMode=NO_LINK` 时保留 URL 但禁止公开站生成点击跳转。

## 6. SiteProperty 与 CMS Metadata

继续复用 `cms_site_config` 表名；数据库保存具体 SiteProperty 的 `name/group_code/value/value_type/sort_order/required/system/enabled`，但“允许有哪些属性分组”不再作为 CMS 数据表维护。

新增 `CmsMetadataProperties`，通过 Spring `@ConfigurationProperties(prefix="cms.metadata")` 绑定 `cms-metadata.yml`。`application.yml` 使用 Spring Config Import 加载资源。当前 metadata 提供 `BASIC / BRAND / CONTACT / FOOTER / PRESENTATION / GENERAL` 的名称与排序。

Admin `GET /api/admin/site-config/groups` 返回排序后的 group definitions。SiteConfigService 创建/修改属性定义时必须验证 `groupCode` 已存在于 metadata，防止数据库产生 UI 无法解释的自由分组。

`valueType` 支持 `TEXT / RESOURCE_PATH / JSON / URL / BOOLEAN / INTEGER`。Service 根据数据库 valueType 做最终校验；不得通过编译期 key Enum 限制自定义属性 key。

V11 新增系统属性：

```text
HOME_CAROUSEL_INTERVAL_SECONDS
Group: PRESENTATION
Type: INTEGER
Default: 4
Required: true
```

普通 INTEGER 接受整数；该轮播间隔还有 key-specific 业务约束：必须 `> 0`，单位秒。Public Site 对异常历史值仍可执行 fallback，但正常 Admin/API 写入不能保存 0 或负数。

RESOURCE_PATH 继续只接受 `/static/**`。

## 7. Migration

不得回改已执行 V8/V9/V10。V11 `V11__cms_metadata_and_image_policies.sql` 负责：

- `cms_column` 增加 `cover_policy`；
- `cms_list` 增加 `image_policy`；
- 将 HOME_CAROUSEL 回填 REQUIRED；
- 将当前三个 SITE_LINKS 列表回填 NONE；
- 新增 HOME_CAROUSEL_INTERVAL_SECONDS 网站属性。

最终 Runtime 验证必须从 Fresh Database 执行完整 Flyway chain，不能只通过 SQL 文本检查证明迁移可用。

## 8. 静态资源与上传目录

复用既有 StaticResource API，不为列表、宣传展示、导航再次实现上传后端。管理端通用图片控件按照业务上下文写入：

- `uploads/displays/{slotCode}/`；
- `uploads/lists/{listCode}/`；
- `uploads/site-properties/{key}/`；
- `uploads/navigation-icons/`。

实际公开值统一为 `/static/{relativePath}`。上传仍执行扩展名 + 真实文件签名校验，并自动创建父目录。允许扩展名和文件签名属于安全边界，保持代码契约，不提升为运营配置。

`protectedResource` 统一称“受保护资源”，其集合由两类来源合并：

1. `cms.static.protected-resources`：Spring 外部化配置声明的固定部署 / 工程基线资源；默认值通过 `application.yml` 提供，可由环境变量 `CMS_STATIC_PROTECTED_RESOURCES` 覆盖；
2. 运行时引用：启用的 RESOURCE_PATH 网站属性、CmsList 图片、Advertisement 图片、Navigation iconPath。

StaticResourceService 不再直接写死具体基线文件路径。运行时引用仍必须动态计算，不增加数据库 `protected=true` 人工字段。资源开始被 CMS 引用时自动获得保护，解除引用后自然退出保护集合。

受保护资源的普通 DELETE 由 Backend 最终拒绝；明确 replace 仍允许。该机制仍不扫描所有 CSS / JS / 富文本引用，因此 Admin 对普通资源删除继续给出风险提示。

## 9. Test

至少覆盖：

- Column/Article：NONE 拒绝本地封面；REQUIRED 允许无封面草稿但阻止发布；已发布文章不能编辑为缺少必填封面的状态；Public Summary 能返回封面引用；
- NavigationLocation CRUD、跨位置 parent 拒绝、iconPath 正常公开及非法外部图标路径拒绝；
- SiteProperty：metadata group 顺序、未知 group 拒绝、自定义 key、JSON/URL/RESOURCE_PATH/BOOLEAN/INTEGER 校验，以及 HOME_CAROUSEL_INTERVAL_SECONDS 正整数约束；
- CmsList：NONE / OPTIONAL / REQUIRED item imagePath 约束与策略切换一致性；
- 宣传展示有效期和 NO_LINK；
- Fresh Database 全量 migration + Runtime；
- 静态资源真实媒体校验；
- 配置提供的固定受保护资源与运行时引用资源都返回 `protectedResource=true`；更换测试配置后保护集合随配置变化，证明 Service 不再依赖具体路径硬编码；
- 受保护资源拒绝普通删除但允许明确替换。
