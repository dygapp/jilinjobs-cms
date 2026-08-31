# CMS Backend 技术计划（Technical Plan）

## 1. 目标

Backend 负责 CMS Core 的业务校验、持久化、公开/管理 API 和静态资源服务。

## 2. Navigation

`cms_navigation_location` 保存运行时导航位置；`cms_navigation.position` 保存位置 code，不使用编译期位置 Enum。

NavigationItem 保存时校验位置存在；父导航必须存在且位置相同；禁止循环。新增可选 `icon_path`，只接受 `/static/**` 路径，并随 Admin/Public API 输出。公开站不再根据列表索引计算图标路径。

现有 HOME_SHORTCUT / HOME_QUICK 业务图标通过后续 Flyway 将版本化 `/static/icons/**` 路径回填到对应导航条目。

## 3. CmsList

表：`cms_list`、`cms_list_item`。

列表 code 唯一。`cms_list` 不再维护 `item_type`；列表项的 title 保持必填，subtitle、URL、imagePath 独立可选。URL 存在时允许站内 `/...` 或 HTTP(S)；imagePath 存在时要求 `/static/**`。Backend 不判断前台是文字、Logo、图片或组合展示。

Admin API 提供列表和列表项 CRUD；Public API 返回启用列表和启用项。具体页面的数据完整性要求由相应前台页面契约和验证负责。

## 4. Advertisement 技术模块 / 宣传展示产品模型

表继续使用：`cms_ad_slot`、`cms_advertisement`；API 路径继续保留 `/advertisements` 兼容。产品界面称“宣传展示位 / 展示内容”。

展示位 code 唯一；公开查询过滤 slot/item enabled 以及可选 start/end 时间。`openMode=NO_LINK` 时保留 URL 但禁止公开站生成点击跳转。

## 5. SiteProperty

继续复用 `cms_site_config` 表名，避免无价值重命名迁移；通过已有列作为通用属性定义：`name/group_code/sort_order/required/system/enabled`。

Service 根据数据库 `value_type` 校验。Admin 支持定义 CRUD；Public 只返回 enabled 属性。RESOURCE_PATH 继续只接受 `/static/**`。

## 6. 后续迁移

在既有 V8 通用模型之后新增 migration：

- `cms_navigation` 增加 `icon_path`；
- 为 HOME_SHORTCUT / HOME_QUICK 回填当前版本化业务图标；
- 删除 `cms_list.item_type`，解除 LINK / IMAGE_LINK / TEXT 组合型字段约束；
- 将 `HOME_RECRUITMENT_PROMO` 产品名称收敛为“首页招聘活动展示位”。

不得回改已执行 V8。

## 7. 静态资源与上传目录

复用既有 StaticResource API，不为列表、宣传展示、导航再次实现上传后端。管理端通用图片控件按照业务上下文写入：

- `uploads/displays/{slotCode}/`；
- `uploads/lists/{listCode}/`；
- `uploads/site-properties/{key}/`；
- `uploads/navigation-icons/`。

实际公开值统一为 `/static/{relativePath}`。上传仍执行扩展名 + 真实文件签名校验，并自动创建父目录。

关键资源集合为：固定 runtime 基线 + enabled RESOURCE_PATH 网站属性 + CmsList 图片 + Advertisement 图片 + Navigation iconPath。仍不扫描所有 CSS/JS/富文本引用。

## 8. Test

至少覆盖：

- NavigationLocation CRUD、跨位置 parent 拒绝、iconPath 正常公开及非法外部图标路径拒绝；
- SiteProperty 自定义 key、JSON/URL/RESOURCE_PATH 校验；
- CmsList code、独立可选 URL/imagePath 与 JSON 校验；
- 宣传展示有效期和 NO_LINK；
- 静态资源真实媒体校验和新引用模型保护无回归。
