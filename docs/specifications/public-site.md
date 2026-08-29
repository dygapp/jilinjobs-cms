# 公开站规格说明（Specification）

## 1. 目标

本文定义吉林省高等学校毕业生就业信息网公开站的 WHAT / WHY。共享内容模型以 `docs/specifications/cms-core.md` 为准。

公开站目标仍为现网视觉与布局复刻，并通过 CMS 数据驱动需要持续运营维护的内容；稳定布局、固定集成和无需运营维护的内容保留为工程资产。

## 2. URL 与页面

- 首页：`/`
- 栏目：`/column/{alias}`
- 站内文章：`/article/{id}`
- 普通固定页：`/page/{alias}`
- 页面组成员：`/page/{groupAlias}/{alias}`

外链文章从列表直接打开原文；兼容文章地址重定向原文。面包屑来自业务对象关系，不从 URL 或导航入口机械推导。

## 3. 首页数据来源

首页固定模板继续复刻现网主要布局。运营数据来源统一为：

- 主导航：`MAIN` Navigation；
- 首屏右侧五个蓝色入口：`HOME_SHORTCUT` Navigation；
- 业务指南快速入口：`HOME_QUICK` Navigation；
- 轮播：`HOME_CAROUSEL` CmsList；
- 通知公告 / 就业动态 / 招聘公告：Column + Article；
- 招聘活动横幅：`HOME_RECRUITMENT_PROMO` AdvertisementSlot；
- 网站导航 / 友情链接：`SITE_LINKS` 分组 CmsList；
- 网站名称、电话、备案、版权等：SiteProperty；
- NCSS 区域：固定工程集成，不提供 CMS 管理。

公开站不得再同时读取旧 JSON 配置与新业务对象并合并结果。

## 4. 工程资产

以下内容允许固定在公开站工程或版本化静态资产中：

- 首页区域布局、尺寸关系和视觉样式；
- Header / Footer 结构；
- 页面 Shell；
- NCSS Logo、学生入口、企业入口及其固定集成布局；
- 明确无需运营维护的图标、装饰资源与一次性外部平台接缝。

当这些内容未来产生真实运营维护需求时，再通过 Requirement Change 判断是否升级为 CMS 对象。

## 5. 页面基线

栏目列表、文章详情、普通固定页、业务指南页面组继续以现网页面主要版式为复刻基准。页面组 Tab 从 PageGroup 成员数据生成。

中心党建继续只保留菜单占位；招聘信息和直播课程继续保留本站页面框架/占位，不加载真实第三方 iframe。

## 6. Acceptance Criteria

- 首页不再读取 `SERVICE_LINKS`、`HOME_BANNERS`、`SITE_LINK_GROUPS`、`HOME_PROMO_BANNER_PATH`、`HOME_NCSS_LOGO_PATH` 作为运行时内容源；
- 首页五个蓝色入口来自 `HOME_SHORTCUT`；
- 业务指南快捷入口来自 `HOME_QUICK`；
- 轮播来自通用列表；
- 招聘活动横幅来自广告位；
- 网站导航/友情链接来自通用列表；
- 联系电话等站点属性来自 SiteProperty；
- NCSS 固定集成正常；
- 既有公开 URL、文章/栏目/页面行为、视觉主基线无回归。
