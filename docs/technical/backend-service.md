# CMS Backend 技术计划（Technical Plan）

## 1. 目标

Backend 负责 CMS Core 的业务校验、持久化、公开/管理 API 和静态资源服务。

## 2. Navigation

新增 `cms_navigation_location`。`cms_navigation.position` 继续保存位置 code，以兼容已有表结构，但 Kotlin 模型改为字符串 code，不使用 `NavigationPosition` enum。

NavigationLocation API：

- Admin list/create/update/delete；
- Public 只通过 NavigationItem 查询消费启用位置的数据。

NavigationItem 保存时校验位置存在；父导航必须存在且位置相同；禁止循环。

## 3. CmsList

新增：

- `cms_list`
- `cms_list_item`

列表 code 唯一；itemType 校验 `LINK/IMAGE_LINK/TEXT`。列表项 URL 允许站内 `/...` 或 HTTP(S)；IMAGE_LINK 要求图片路径使用 `/static/**`。

Admin API 提供列表和列表项 CRUD；Public API 返回启用列表和启用项。

## 4. Advertisement

新增：

- `cms_ad_slot`
- `cms_advertisement`

广告位 code 唯一；公开查询过滤 slot/ad enabled 以及可选 start/end 时间。

## 5. SiteProperty

继续复用 `cms_site_config` 表名，避免无价值重命名迁移；通过新增列升级为通用属性定义：`name/group_code/sort_order/required/system/enabled`。

移除 `SiteConfigKey` 编译期枚举。Service 根据数据库 `value_type` 校验。Admin 支持定义 CRUD；Public 只返回 enabled 属性。

从运行时属性中迁出：`HOME_BANNERS`、`SERVICE_LINKS`、`SITE_LINK_GROUPS`、`HOME_PROMO_BANNER_PATH`、`HOME_NCSS_LOGO_PATH`。

## 6. 初始化迁移

新增单一后续 Flyway migration：

- 创建导航位置、列表、广告表；
- 升级网站属性表；
- 将现有首页五个快捷入口迁移到 `HOME_SHORTCUT`；
- 将 guide 页面成员建立为 `HOME_QUICK`；
- 将现有轮播迁移到 `HOME_CAROUSEL`；
- 将网站链接组迁移到 `SITE_LINKS` 分组列表；
- 将招聘横幅迁移到 `HOME_RECRUITMENT_PROMO`；
- 删除已迁出的旧 site-config 行；
- 保留 NCSS 静态资源文件，但不再保留 CMS 配置引用。

## 7. 静态资源保护

关键资源集合改为：固定 runtime 基线 + enabled RESOURCE_PATH 网站属性 + CmsList IMAGE_LINK 图片 + Advertisement 图片。仍不扫描所有 CSS/JS/富文本引用。

## 8. Test

至少覆盖：

- NavigationLocation CRUD 与跨位置 parent 拒绝；
- SiteProperty 自定义 key、JSON/URL/RESOURCE_PATH 校验；
- CmsList code/type/item 校验；
- Advertisement 有效期过滤；
- 静态资源保护引用新模型后无回归。
