# CMS 核心规格说明（Specification）

## 1. 目标

本文定义 `jilinjobs-cms` 的共享 CMS 产品模型。公开站和管理端都是该模型的消费者，不分别定义第二套栏目、导航、列表、宣传展示或网站属性规则。

Authority：`docs/requirements/information-publishing.md` V4.9。

## 2. 核心对象

CMS 第一阶段核心对象包括：

- Column：树形栏目；
- Article：站内文章 / 外链文章；
- Page / PageGroup：技术模型名称，产品界面称“单页 / 单页分组”；
- NavigationLocation / NavigationItem：导航位置与多级导航；
- CmsList / CmsListItem：通用列表与列表项；
- AdvertisementSlot / Advertisement：技术层沿用既有标识，产品界面称“宣传展示位 / 展示内容”；
- SiteProperty：网站属性；
- StaticResource：站点公共静态资源。

这些对象应保持业务语义独立，不为了减少表数量把多个概念塞入 JSON 配置。

产品信息架构将文章、单页、列表视为主要内容类型；栏目和导航承担内容结构；宣传展示承担运营展示；网站属性和静态资源属于站点设置。技术对象名称不要求与产品菜单逐字一致。

具有明确“容器 → 成员”关系的 CMS 模型，在管理端优先采用左侧选择容器/组织上下文、右侧管理成员的方式；容器本身就是主要管理对象时可以保留直接结构视图。

Column `coverPolicy` 与 CmsList `imagePolicy` 统一使用 `NONE / OPTIONAL / REQUIRED` 图片数据策略。该策略只描述对应内容数据是否允许或要求图片，不是页面展示模式；不得用它决定列表布局、卡片方向、图片尺寸、caption 或其他视觉规则。

## 3. 导航

导航位置为运行时数据，至少具有 `code/name/description/sortOrder/enabled/system`。导航条目必须关联一个位置，支持 parent/children、排序、启停、HOME/COLUMN/PAGE/LINK/PLACEHOLDER 目标、打开方式及可选 `iconPath`。

`iconPath` 是导航条目的内容属性，使用 `/static/**` 资源路径。公开站不得按导航当前数组下标、排序号或位置序号推导业务图标；未配置图标的导航允许按页面设计不显示图标。

同一父子链必须属于同一位置；禁止循环；有子项时禁止直接删除父项。

JilinJobs 的 `MAIN`、`HOME_SHORTCUT`、`HOME_QUICK` 等稳定 Navigation identity 由 `sites/jilinjobs/structure/**` 定义并通过 Site Package provision；现有业务图标引用稳定 Site assets 或 Runtime uploads，不由 Generic CMS Flyway 注入具体站点实例值。

## 4. 栏目、文章与单页

Column 至少具有 `id/parentId/name/alias/sortOrder/enabled/coverPolicy`。`coverPolicy` 语义：

- `NONE`：站内文章不保存封面引用；
- `OPTIONAL`：封面可有可无；
- `REQUIRED`：草稿可以先无封面保存，但发布前必须存在封面；已发布文章后续编辑也不得使其失去必填封面。

外链文章不复制本地封面、正文图片和附件，因此不按 Column `coverPolicy` 强制本地封面。

Article `articleType` 使用 `INTERNAL / EXTERNAL_LINK` 并属于创建时来源身份：创建后普通编辑不得在两种来源语义之间切换，Admin 编辑态只读，Backend 必须独立拒绝绕过 UI 的修改。栏目、标题、正文、来源、日期、图片、置顶和展示顺序等继续按各自既有运营规则维护。

Article 不再具有全局 `recommended` 运营语义。公开文章默认排序使用 `置顶 DESC → 展示顺序 DESC → 发布日期/实际发布时间 DESC → id DESC`；若需要首页推荐、专题推荐或其他独立人工展示，应使用 `CmsList + ARTICLE` 明确投放，而不是恢复 Article 全局推荐布尔值。

Public Article Summary 保留可选 `coverResourceId`，使公开栏目页面在自身模板设计需要图片时可以消费该数据；是否展示封面不由 Column 策略直接控制。

技术模型继续使用 `Page / PageGroup`，产品界面统一使用“单页 / 单页分组”。单页具有稳定 Alias 和页面身份，可以不属于分组，也可以属于一个平级单页分组；不建立嵌套 PageGroup。

独立单页公开 URL 为 `/page/{alias}`，分组成员为 `/page/{groupAlias}/{alias}`。分组成员可以在公开页面形成由数据驱动的公共 Tab，前端不得重复维护分组成员清单。

管理端单页组织至少包含“全部单页”“独立单页”和所有单页分组。选择具体分组只展示该组成员；选择“独立单页”只展示 `groupId = null` 的成员；在具体分组上下文新增单页时默认带入当前分组，但表单仍允许调整。

## 5. 通用列表

列表定义至少具有 `code/name/groupCode/imagePolicy/description/sortOrder/enabled/system`。列表项来源身份只使用：

- `LINK`：列表项自身维护标题、副标题、可选 URL、图片、打开方式、排序、启停和扩展数据；
- `ARTICLE`：通过 `articleId` 引用已有 Article，用于把文章投放到轮播或其他列表容器。

列表不保存 `displayMode`，也不使用旧 `LINK / IMAGE_LINK / TEXT` 组合型 `itemType` 决定字段能力。页面如何呈现文字、图片、Logo、caption 或布局继续由公开站模板负责。

### 5.1 来源身份与公开有效性

`CmsListItem.sourceType` 是创建时身份；ARTICLE 的 `articleId` 同样属于创建时来源身份。普通编辑不得 LINK↔ARTICLE 转换，也不得把 ARTICLE 改为引用另一篇文章；需要改变来源时删除旧项并创建新项。Admin 编辑态禁用这些字段，Backend 独立执行相同约束。

ARTICLE placement 不改变 Article 的唯一 `columnId`、栏目列表归属或详情面包屑。关联文章只有处于 `PUBLISHED` 时 placement 才能公开；撤回后自动退出公开列表，重新发布后可按原投放恢复。

INTERNAL ARTICLE 的站内 URL 由当前 Public Site 生成 canonical route：Main `/article/{id}`，Party `/party/article/{id}`；EXTERNAL_LINK ARTICLE 使用文章当前 external URL。两者都继续遵守 CmsListItem `DEFAULT / SAME_WINDOW / NEW_WINDOW` 的 `openMode`。

### 5.2 图片策略与 effective image

`imagePolicy` 只约束列表项图片数据：

- `NONE`：LINK 不保存图片，ARTICLE 不保存图片覆盖；
- `OPTIONAL`：LINK 图片可选；ARTICLE 可继承文章封面或使用显式覆盖 Resource，也允许最终无图；
- `REQUIRED`：公开有效项必须形成有效图片。

ARTICLE 可继承文章当前封面；后台也可把正文图片作为选择候选，或上传/选择新的 CMS Resource 作为列表专用覆盖。正文图片一旦选中必须把对应 Resource ID 固化为列表项覆盖；公开端不得在 Runtime 隐式寻找“正文第一张”。覆盖图片不修改 Article 自身封面或正文。

ARTICLE 的可选覆盖使用 `imageResourceId` 关联 CMS Resource；公开 API 计算并返回 effective image。`imagePolicy=REQUIRED` 的 ARTICLE 如果后来失去继承图片且没有有效覆盖，必须退出公开列表，直到重新形成有效图片。

修改列表定义时不得制造既有 items 与新策略冲突：切换 `NONE` 前清除不允许的图片/覆盖；切换 `REQUIRED` 前确保既有项目能满足有效图片契约。Backend 对 Admin UI 与直接 API 执行同一校验。

当前稳定列表包括：

- `HOME_CAROUSEL`：`imagePolicy=REQUIRED`；
- `PARTY_CAROUSEL`：`imagePolicy=REQUIRED`；
- `SITE_LINKS` 分组下的站点链接列表：当前基线按文字链接消费，`imagePolicy=NONE`。

## 6. 宣传展示

产品界面统一使用“宣传展示管理 / 展示位 / 展示内容”。为避免无价值数据库/API 重命名，技术实现可继续使用 `AdvertisementSlot / Advertisement`、`cms_ad_*` 和 `/advertisements` 等既有标识。

展示位具有 `code/name/description/sortOrder/enabled/system`。展示内容具有 `slotId/title/imagePath/url/openMode/startAt/endAt/sortOrder/enabled`。

同一展示位允许存在多条展示内容。公开查询只返回展示位启用、内容启用且当前时间落在可选有效期内的数据，并按展示内容 `sortOrder,id` 输出。公开模板对一个展示位只取得 1 条有效内容时静态展示，存在 2 条及以上有效内容时按返回顺序轮动；当前运营建议同一位置通常不超过 3 张，但第一阶段不设置全局硬上限。

目标 URL 与点击行为分离。`url` 可为空；`openMode` 第一阶段支持 `DEFAULT`、`SAME_WINDOW`、`NEW_WINDOW`、`NO_LINK`。`NO_LINK` 优先级最高，即使 `url` 已保存，公开站也只显示图片而不产生点击跳转；管理员切回其他打开方式后，可继续使用原有 URL，无需重新录入。

展示内容的 `sortOrder` 表示同一展示位内的展示/轮动顺序；展示位自身的 `sortOrder` 表示多个展示位之间的管理/输出顺序。有效期不改变内容记录本身：未到 `startAt` 时不公开，达到 `endAt` 后停止公开，过期数据仍保留在管理端。管理端根据 `enabled/startAt/endAt` 计算并展示“已停用 / 待生效 / 展示中 / 已过期”状态。

JilinJobs `HOME_RECRUITMENT_PROMO` 等稳定展示位 identity 由 Site Package structure 定义，而不是由 Generic Backend Flyway 注入站点实例数据。

## 7. 网站属性与轮播行为参数

网站属性具有 `key/name/groupCode/value/valueType/description/sortOrder/required/system/enabled`。

网站属性分组不是新的 CMS 业务对象。可选分组由 Spring 可外部化配置资源中的 CMS metadata 定义，至少包含 `BASIC / BRAND / CONTACT / FOOTER / PRESENTATION / GENERAL`，并带名称和排序。SiteProperty 只能引用已定义分组；Admin 通过 `/api/admin/site-config/groups` 获取元数据用于分组导航和受控选择。

第一版类型：`TEXT`、`RESOURCE_PATH`、`JSON`、`URL`、`BOOLEAN`、`INTEGER`。后端按数据库中的 `valueType` 校验；不得通过 Kotlin/Java key Enum 白名单限制可定义 key。

Main / Party 当前统一使用以下 `PRESENTATION` 系统属性：

- `CAROUSEL_INTERVAL_SECONDS`：`INTEGER`，默认 `4` 秒，必须为正整数；缺失、非法历史值或不大于 0 时 Public fallback 为 4 秒；
- `CAROUSEL_MAX_ITEMS`：`INTEGER`，默认 `5`，必须为正整数；表示单个轮播区域最多消费的有效项数，不限制后台可维护记录数；缺失、非法历史值或不大于 0 时 Public fallback 为 5。

Backend 不接受以上 key 的 0、负数或非整数新值。旧 `HOME_CAROUSEL_INTERVAL_SECONDS` 仅属于 superseded EU-30 前历史配置，不承担 Current Runtime responsibility。

当前管理端允许直接维护属性定义和属性值。`RESOURCE_PATH` 图片属性复用统一静态图片资源选择/上传能力。未来普通管理员/超级管理员差异只记录规划，本阶段不实现认证授权。

网站属性承担少量运营可调整的站点级行为参数，但不因此新增“系统设置”模块；数据库连接、上传安全限制、运行环境地址等仍属于工程/部署配置。

## 8. Site Package、工程资产与 Runtime 资源边界

Generic CMS 提供结构 provisioning、bootstrap state 与 StaticResource 等通用 capability，但不通过 Backend Flyway 持有具体 JilinJobs Site instance data。当前责任如下：

- `sites/jilinjobs/structure/**`：JilinJobs stable Site structure 的版本化 owner；
- `sites/jilinjobs/bootstrap/**`：Fresh Site one-time operational defaults 的版本化 owner；成功 bootstrap 后这些数据成为普通 operator-managed Runtime data，后续 restart/reconcile/repeated bootstrap 不得覆盖或 resurrect；
- `sites/jilinjobs/assets/**`：JilinJobs stable Site assets 的唯一版本化 source owner；
- `/static/home/**`、`/static/brand/**`、`/static/footer/**`、`/static/icons/**`、`/static/party/**` 等：stable assets 的 Runtime public targets；
- `/static/uploads/**`：CMS Runtime uploads，不属于 stable Site asset ownership；
- `data-migrations/**`：历史运营文章、外链、正文资源、附件、历史列表成员与 provenance，不并入 Flyway、Site stable structure、bootstrap 或 stable assets。

稳定布局、固定 Shell、一次性外部集成和无需运营维护的内容可以保留为代码或 stable Site assets。NCSS 首页区域属于此类，不建立 CMS 配置项。

管理端图片字段复用统一 `ImageResourcePicker`：允许上传新图片、选择适用的已有/内置图片并预览当前值；不要求管理员先进入静态资源页再复制路径。上传成功只改变当前表单引用，不自动删除旧资源。

## 9. 静态资源

StaticResource 提供浏览、上传、查看/下载、替换、回收、恢复。扩展名与真实内容必须一致；防止目录穿越。

受保护资源集合由 Backend 负责：Site Package stable asset manifest/catalog 的 targets、部署固定受保护路径，以及当前网站属性、列表、宣传展示、导航等 Runtime 引用共同形成保护集合。普通删除受保护资源必须拒绝，明确 replace 仍允许。该机制不宣称完整扫描 CSS / JS / 富文本等全部潜在引用。

## 10. 当前 Flyway 与权限边界

Backend active Flyway 只承担 Generic CMS Schema evolution 与 site-neutral provisioning capability，当前 active baseline 为：

```text
V1__current_cms_schema.sql
V2__site_provisioning_schema_capabilities.sql
```

JilinJobs stable structure、one-time defaults、stable assets 和 Historical Content Migration 不进入该 active Flyway lineage。EU-30 前后的 V11 等历史 migration number 只承担历史实现 / upgrade 追溯，不定义 Current initialization lifecycle。

本阶段不实现登录、账号、角色、权限点、超级管理员判断或前端假权限。危险操作通过业务校验、确认提示和受保护资源等安全措施控制。

未来权限体系接入时，可对不同 CMS 对象及定义级操作设置独立权限。

## 11. Acceptance Criteria

- 管理端信息架构按“内容管理 / 内容结构 / 运营展示 / 站点设置”组织现有 CMS 能力，不新增独立“系统设置”模块；
- 产品界面使用“单页 / 单页分组”，技术层 `Page / PageGroup` 保持兼容；
- 单页管理通过“全部单页 / 独立单页 / 单页分组”组织成员，并在分组上下文新增时默认带入当前分组；
- Column 支持 `NONE / OPTIONAL / REQUIRED` 封面数据策略，`REQUIRED` 允许草稿暂存但阻止无封面发布，已发布文章编辑必须继续满足策略；
- Article `articleType` 是创建时身份，不得普通编辑切换；Article 不存在全局 `recommended` 语义，独立推荐使用 `CmsList + ARTICLE`；
- Public Article Summary 提供可选 `coverResourceId`；
- 导航位置不再是编译期枚举；导航图标是可选条目属性，首页快捷入口/快速导航不按数组下标推导图标；
- CmsListItem source model 为 `LINK / ARTICLE`；ARTICLE 不改变 Article 栏目归属，未发布/撤回 Article 不公开；
- CmsListItem `sourceType` 与 ARTICLE `articleId` 是创建时身份，不得普通编辑切换/替换；
- ARTICLE effective image 支持继承文章封面与显式 Resource override，REQUIRED 失去有效图片后退出公开列表；
- INTERNAL ARTICLE 由 Main/Party 生成各自 canonical route，并继续遵守列表项 `openMode`；
- `HOME_CAROUSEL` / `PARTY_CAROUSEL` 基线图片必填；`SITE_LINKS` 当前基线不使用图片，页面展示方式仍由公开站工程决定；
- Main / Party 当前轮播参数统一为 `CAROUSEL_INTERVAL_SECONDS` + `CAROUSEL_MAX_ITEMS`，Backend 拒绝非正整数新值；
- 招聘活动宣传图只来源于 `HOME_RECRUITMENT_PROMO` 展示位；同一展示位多条当前有效内容能够按展示顺序轮动；
- `NO_LINK` 能在保留 URL 的情况下禁止图片点击，切回跳转模式后 URL 可继续使用；展示有效期控制公开可见性，过期记录仍保留在管理端；
- 宣传展示、列表、导航图标、RESOURCE_PATH 图片属性复用统一图片资源选择/上传能力，Runtime 上传进入 `/static/uploads/**`；
- 网站属性分组由 Spring CMS metadata 提供，SiteProperty 不允许引用不存在的分组；
- stable Site structure / one-time bootstrap / stable assets 分别由 `sites/jilinjobs/{structure,bootstrap,assets}/**` 持有；Historical Content Migration 继续属于 `data-migrations/**`；
- Generic Backend active Flyway 仅为 V1 schema + V2 site-neutral provisioning capability，不承担 JilinJobs Site instance rows；
- 既有栏目、文章、单页、静态资源和公开 URL 行为无回归。