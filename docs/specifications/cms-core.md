# CMS 核心规格说明（Specification）

## 1. 目标

本文定义 `jilinjobs-cms` 的共享 CMS 产品模型。公开站和管理端都是该模型的消费者，不分别定义第二套栏目、导航、列表、宣传展示或网站属性规则。

Authority：`docs/requirements/information-publishing.md` V4.4。

## 2. 核心对象

CMS 第一阶段核心对象包括：

- Column：树形栏目；
- Article：站内文章 / 外链文章；
- Page / PageGroup：固定页面与页面组；
- NavigationLocation / NavigationItem：导航位置与多级导航；
- CmsList / CmsListItem：通用列表与列表项；
- AdvertisementSlot / Advertisement：技术层沿用既有标识，产品界面称“宣传展示位 / 展示内容”；
- SiteProperty：网站属性；
- StaticResource：站点公共静态资源。

这些对象应保持业务语义独立，不为了减少表数量把多个概念塞入 JSON 配置。

## 3. 导航

导航位置为运行时数据，至少具有 `code/name/description/sortOrder/enabled/system`。导航条目必须关联一个位置，支持 parent/children、排序、启停、HOME/COLUMN/PAGE/LINK/PLACEHOLDER 目标、打开方式及可选 `iconPath`。

`iconPath` 是导航条目的内容属性，使用 `/static/**` 资源路径。公开站不得按导航当前数组下标、排序号或位置序号推导业务图标；未配置图标的导航允许按页面设计不显示图标。

同一父子链必须属于同一位置；禁止循环；有子项时禁止直接删除父项。

初始化位置至少包括 `MAIN`、`HOME_SHORTCUT`、`HOME_QUICK`。现有 `HOME_SHORTCUT` 和 `HOME_QUICK` 图标以版本化站点图标作为初始化值，后续可由管理员选择内置导航图标或上传自定义图标。

## 4. 通用列表

列表定义至少具有 `code/name/groupCode/description/sortOrder/enabled/system`。列表项至少具有 `title/subtitle/url/imagePath/openMode/sortOrder/enabled/extraJson`。

列表不保存 `displayMode`，也不再使用 `LINK / IMAGE_LINK / TEXT` 之类组合型 `itemType` 决定字段能力。`title` 保持必填，作为管理识别名称和可访问性基础信息；`subtitle/url/imagePath` 独立可选，存在 URL 时执行 URL 校验，存在图片时必须使用 `/static/**`。

具体公开页面根据自身设计契约决定消费哪些字段、哪些字段在该页面语境中必须存在以及如何展示。同一列表数据允许在不同页面按文字、图片、Logo 或图片+文字等方式消费，而不要求修改 CMS 模型。

初始化：

- `HOME_CAROUSEL`：首页轮播；公开首页使用其图片，URL 可选，有 URL 时可点击，无 URL 时仅展示图片；
- `SITE_LINKS` 分组下的若干列表：网站导航/友情链接数据，可在未来页面设计中使用可选 Logo，而无需扩展列表类型。

## 5. 宣传展示

产品界面统一使用“宣传展示管理 / 展示位 / 展示内容”。为避免无价值数据库/API 重命名，技术实现可继续使用 `AdvertisementSlot / Advertisement`、`cms_ad_*` 和 `/advertisements` 等既有标识。

展示位具有 `code/name/description/sortOrder/enabled/system`。展示内容具有 `slotId/title/imagePath/url/openMode/startAt/endAt/sortOrder/enabled`。

同一展示位允许存在多条展示内容。公开查询只返回展示位启用、内容启用且当前时间落在可选有效期内的数据，并按展示内容 `sortOrder,id` 输出。公开模板对一个展示位只取得 1 条有效内容时静态展示，存在 2 条及以上有效内容时按返回顺序轮动；当前运营建议同一位置通常不超过 3 张，但第一阶段不设置全局硬上限。

目标 URL 与点击行为分离。`url` 可为空；`openMode` 第一阶段支持 `DEFAULT`、`SAME_WINDOW`、`NEW_WINDOW`、`NO_LINK`。`NO_LINK` 优先级最高，即使 `url` 已保存，公开站也只显示图片而不产生点击跳转；管理员切回其他打开方式后，可继续使用原有 URL，无需重新录入。

展示内容的 `sortOrder` 表示同一展示位内的展示/轮动顺序；展示位自身的 `sortOrder` 表示多个展示位之间的管理/输出顺序。有效期不改变内容记录本身：未到 `startAt` 时不公开，达到 `endAt` 后停止公开，过期数据仍保留在管理端。管理端根据 `enabled/startAt/endAt` 计算并展示“已停用 / 待生效 / 展示中 / 已过期”状态。

初始化 `HOME_RECRUITMENT_PROMO`。

## 6. 网站属性

网站属性具有 `key/name/groupCode/value/valueType/description/sortOrder/required/system/enabled`。

第一版类型：`TEXT`、`RESOURCE_PATH`、`JSON`、`URL`、`BOOLEAN`。后端按数据库中的 `valueType` 校验；不得通过 Kotlin/Java 枚举白名单限制可定义 key。

当前管理端允许直接维护属性定义和属性值。`RESOURCE_PATH` 图片属性复用统一静态图片资源选择/上传能力。未来普通管理员/超级管理员差异只记录规划，本阶段不实现认证授权。

## 7. 工程资产与运行时资源边界

稳定布局、固定 Shell、一次性外部集成和无需运营维护的内容可以保留为代码或版本化静态资产。NCSS 首页区域属于此类，不建立 CMS 配置项。

已有 CMS 业务数据不得在公开站再维护重复常量；工程资产例外必须能说明其不具备持续运营维护价值。

静态资源分为：

- 工程基线资源：`/static/home/**`、`/static/brand/**`、`/static/footer/**`、`/static/icons/**` 等，由 Git / `site-baseline` 版本化管理；
- CMS 运行时上传：统一进入 `/static/uploads/**`，其中宣传展示、列表、网站属性、导航自定义图标使用稳定业务目录。

管理端图片字段复用统一 `ImageResourcePicker`：允许上传新图片、选择适用的已有/内置图片并预览当前值；不要求管理员先进入静态资源页再复制路径。上传成功只改变当前表单引用，不自动删除旧资源。

现有站点业务图标建立语义目录/目录清单供导航选择；为避免二进制重命名 churn，第一阶段允许保留既有 `/static/icons/top-nav-*`、`guide-*` 文件路径，但业务含义不再通过序号推导。

## 8. 静态资源

静态资源继续提供浏览、上传、查看/下载、替换、回收、恢复。扩展名与真实内容必须一致；防止目录穿越。关键资源保护继续存在，并覆盖启用的网站属性资源、列表图片、宣传展示图片及导航图标，但不宣称完整引用关系分析。

## 9. 当前权限边界

本阶段不实现登录、账号、角色、权限点、超级管理员判断或前端假权限。危险操作通过业务校验、确认提示和关键资源保护控制。

未来权限体系接入时，可对不同 CMS 对象及定义级操作设置独立权限。

## 10. Acceptance Criteria

- 导航位置不再是编译期枚举；导航图标是可选条目属性，首页快捷入口/快速导航不按数组下标推导图标；
- 首页快捷入口只来源于导航；
- 通用列表不再以 `itemType` 控制图片/URL 字段组合，页面展示方式由公开站工程决定；
- 首页轮播图片由通用列表提供，URL 可以为空；友情链接可保存可选 Logo 而不增加列表类型；
- 招聘活动宣传图只来源于 `HOME_RECRUITMENT_PROMO` 展示位；
- 同一展示位多条当前有效内容能够按展示顺序轮动；
- `NO_LINK` 能在保留 URL 的情况下禁止图片点击，切回跳转模式后 URL 可继续使用；
- 展示开始/结束时间能够控制公开可见性，过期记录仍保留在管理端；
- 广告、列表、导航图标、RESOURCE_PATH 图片属性复用统一图片资源选择/上传能力，运行时上传进入 `/static/uploads/**`；
- 网站属性可新增、编辑、删除定义并按类型校验；
- `HOME_BANNERS`、`SERVICE_LINKS`、`SITE_LINK_GROUPS`、`HOME_PROMO_BANNER_PATH`、`HOME_NCSS_LOGO_PATH` 不再作为 CMS 运行时主配置；
- 既有栏目、文章、页面、静态资源行为无回归。
