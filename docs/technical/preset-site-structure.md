# 预置站点结构保护 Technical Plan

## 1. 技术目标

在不引入权限体系和额外“系统设置”模块的前提下，为网站规划基线增加统一的 `preset` 持久化标识，并由 Backend 作为最终保护边界。

## 2. 数据模型

Flyway `V12__preset_site_structure.sql` 为以下表增加非空布尔列 `preset`，默认 `0`：

- `cms_column`
- `cms_navigation_location`
- `cms_navigation`
- `cms_page_group`
- `cms_page`
- `cms_list`
- `cms_ad_slot`
- `cms_site_config`

V12 只对当前确认的初始化对象执行 `preset=1`。Column、PageGroup、CmsList、AdvertisementSlot、SiteProperty 等使用稳定唯一身份进行定位；Page 按 standalone / group context 定位，避免不同分组 Alias 相同导致误标记。

普通 INSERT 不显式写入 preset，依赖数据库默认值 `0`，从而保证运行期新增数据不是预置数据。

## 3. Backend 约束

各 Mapper 在 Admin 查询中读取 `preset`，并映射到只读响应模型。

各 Service 在写入时执行最终保护：

- Column：preset 不可删除；preset `alias` 不可变化；
- NavigationLocation：preset 不可删除；更新继续固定当前 `code`；
- NavigationItem：preset 不可删除；
- PageGroup：preset `alias` 不可变化，当前没有删除 API；
- Page：preset 不可删除；preset `alias` 不可变化；
- CmsList：preset 不可删除，更新继续固定当前 `code`；
- AdvertisementSlot：preset 不可删除，更新继续固定当前 `code`；
- SiteConfig：preset 定义不可删除；更新路径固定当前 `key`，属性值更新不受影响。

普通 Create / Update Request DTO 不包含 `preset` 字段，防止客户端自行获得或移除保护状态。

## 4. Admin Frontend

Admin API 类型读取 `preset:boolean`，但 Draft 类型不暴露该字段。

管理页面统一采用：

- 预置 Tag；
- 稳定身份字段 disabled；
- 删除按钮隐藏，或容器下拉菜单保留但 disabled；
- 不增加 preset 编辑开关。

这些前端限制只用于清晰交互，不能替代 Backend 拒绝逻辑。

## 5. 验证策略

Backend Unit Tests 覆盖代表性删除和 Alias 保护。

Admin Browser E2E 增加 `preset-protection.spec.ts`，在 Fresh DB Runtime 中验证：

- V12 预置基线的代表性 API 输出；
- 直接 DELETE/PUT 绕过 UI 时 Backend 拒绝；
- 运行期创建对象 `preset=false` 且仍可删除；
- Admin 预置 Tag 和删除入口状态。

现有 CI 继续负责：Backend tests + bootJar、Public/Admin build、Fresh MySQL + 全量 Flyway、Public Browser Regression、Admin Browser E2E 和 Playwright Evidence。

由于 V12 修改数据库模型和管理端交互，祖先 Head 的 Runtime/Human Review Evidence 不直接继承；最终 Head 必须重新取得 Integrated Browser 和 Human Review Environment Current Evidence。

## 6. AI Review 检查点

最终收敛前检查：

1. preset 与 systemFlag 不混用；
2. 普通 DTO 无法写 preset；
3. 删除保护位于 Service/Backend；
4. 稳定 Alias/Code/Key 不可被绕过；
5. Migration 不误标记运行期自定义数据；
6. Article/ListItem/Advertisement 等运营成员没有被过度保护；
7. Public Site 数据契约未被 preset 标记改变；
8. 旧 E2E 若仍假设预置容器可删除，应按当前 Specification 修正为 Stale Verification Contract，而不是削弱 Backend 保护。
