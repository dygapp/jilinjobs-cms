# CMS 核心规格说明（Specification）

## 1. 目标

本文定义 `jilinjobs-cms` 的共享 CMS 产品模型。公开站和管理端都是该模型的消费者，不分别定义第二套栏目、导航、列表、广告或网站属性规则。

Authority：`docs/requirements/information-publishing.md` V4.3。

## 2. 核心对象

CMS 第一阶段核心对象包括：

- Column：树形栏目；
- Article：站内文章 / 外链文章；
- Page / PageGroup：固定页面与页面组；
- NavigationLocation / NavigationItem：导航位置与多级导航；
- CmsList / CmsListItem：通用列表与列表项；
- AdvertisementSlot / Advertisement：广告位与广告内容；
- SiteProperty：网站属性；
- StaticResource：站点公共静态资源。

这些对象应保持业务语义独立，不为了减少表数量把多个概念塞入 JSON 配置。

## 3. 导航

导航位置为运行时数据，至少具有 `code/name/description/sortOrder/enabled/system`。导航条目必须关联一个位置，支持 parent/children、排序、启停、HOME/COLUMN/PAGE/LINK/PLACEHOLDER 目标与打开方式。

同一父子链必须属于同一位置；禁止循环；有子项时禁止直接删除父项。

初始化位置至少包括 `MAIN`、`HOME_SHORTCUT`、`HOME_QUICK`。

## 4. 通用列表

列表定义至少具有 `code/name/groupCode/itemType/description/sortOrder/enabled/system`；列表项至少具有 `title/subtitle/url/imagePath/openMode/sortOrder/enabled/extraJson`。

第一版 `itemType`：`LINK`、`IMAGE_LINK`、`TEXT`。

初始化：

- `HOME_CAROUSEL`：首页轮播；
- `SITE_LINKS` 分组下的若干链接列表：网站导航/友情链接展示。

## 5. 广告位

广告位具有 `code/name/description/sortOrder/enabled/system`。广告内容具有 `slotId/title/imagePath/url/openMode/startAt/endAt/sortOrder/enabled`。

同一广告位允许存在多条广告内容。公开查询只返回广告位启用、广告启用且当前时间落在可选有效期内的数据，并按广告内容 `sortOrder,id` 输出。公开模板对一个广告位只取得 1 条有效广告时静态展示，存在 2 条及以上有效广告时按返回顺序轮动；当前运营建议同一位置通常不超过 3 张，但第一阶段不设置全局硬上限。

广告目标 URL 与点击行为分离。`url` 可为空；`openMode` 第一阶段支持 `DEFAULT`、`SAME_WINDOW`、`NEW_WINDOW`、`NO_LINK`。`NO_LINK` 优先级最高，即使 `url` 已保存，公开站也只显示图片而不产生点击跳转；管理员切回其他打开方式后，可继续使用原有 URL，无需重新录入。

广告内容的 `sortOrder` 表示同一广告位内的展示/轮动顺序；广告位自身的 `sortOrder` 表示多个广告位之间的管理/输出顺序。

有效期不改变广告记录本身：未到 `startAt` 时不公开，达到 `endAt` 后停止公开，过期数据仍保留在管理端。管理端应根据 `enabled/startAt/endAt` 计算并展示“已停用 / 待生效 / 展示中 / 已过期”状态。

初始化 `HOME_RECRUITMENT_PROMO`。

## 6. 网站属性

网站属性具有 `key/name/groupCode/value/valueType/description/sortOrder/required/system/enabled`。

第一版类型：`TEXT`、`RESOURCE_PATH`、`JSON`、`URL`、`BOOLEAN`。后端按数据库中的 `valueType` 校验；不得通过 Kotlin/Java 枚举白名单限制可定义 key。

当前管理端允许直接维护属性定义和属性值。未来普通管理员/超级管理员差异只记录规划，本阶段不实现认证授权。

## 7. 工程资产边界

稳定布局、固定 Shell、一次性外部集成和无需运营维护的内容可以保留为代码或版本化静态资产。NCSS 首页区域属于此类，不建立 CMS 配置项。

已有 CMS 业务数据不得在公开站再维护重复常量；工程资产例外必须能说明其不具备持续运营维护价值。

## 8. 静态资源

静态资源继续提供浏览、上传、查看/下载、替换、回收、恢复。扩展名与真实内容必须一致；防止目录穿越。关键资源保护继续存在，但不宣称完整引用关系分析。

## 9. 当前权限边界

本阶段不实现登录、账号、角色、权限点、超级管理员判断或前端假权限。危险操作通过业务校验、确认提示和关键资源保护控制。

未来权限体系接入时，可对不同 CMS 对象及定义级操作设置独立权限。

## 10. Acceptance Criteria

- 导航位置不再是编译期枚举；
- 首页快捷入口只来源于导航；
- 轮播/友情链接只来源于通用列表；
- 招聘活动横幅只来源于广告位；
- 同一广告位多条当前有效广告能够按展示顺序轮动；
- `NO_LINK` 能在保留 URL 的情况下禁止图片点击，切回跳转模式后 URL 可继续使用；
- 广告开始/结束时间能够控制公开可见性，过期记录仍保留在管理端；
- 网站属性可新增、编辑、删除定义并按类型校验；
- `HOME_BANNERS`、`SERVICE_LINKS`、`SITE_LINK_GROUPS`、`HOME_PROMO_BANNER_PATH`、`HOME_NCSS_LOGO_PATH` 不再作为 CMS 运行时主配置；
- 既有栏目、文章、页面、静态资源行为无回归。
