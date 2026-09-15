# 预置站点结构保护 Technical Plan

## 1. 技术目标

在不引入权限体系和额外“系统设置”模块的前提下，为 Site Package stable structure 提供统一的 `preset` 持久化标识，并由 Backend 作为最终保护边界。

`preset` 属于 Generic CMS capability；具体 JilinJobs stable objects 由 `sites/jilinjobs/structure/**` 决定。历史 Flyway V12 是该能力早期实现与迁移证据，不再是当前 Fresh Site 初始化 Authority。

## 2. 数据模型与当前 ownership

当前 Generic CMS Schema 保留以下表的 `preset` 能力：

- `cms_column`
- `cms_navigation_location`
- `cms_navigation`
- `cms_page_group`
- `cms_page`
- `cms_list`
- `cms_ad_slot`
- `cms_site_config`

普通 INSERT 不显式写入 `preset` 时保持默认 `0`，从而保证运行期新增数据不是预置数据。

JilinJobs stable structure 当前由 `sites/jilinjobs/structure/**` 通过 Site Package provision / reconcile 建立；package-owned row 使用 `preset=true`。普通 operator-created row 保持 `preset=false`。

`cms_navigation.code` 是 EU-39/EU-41 已接受的 provisioning-only nullable stable identity capability：Site Package 对当前 JilinJobs NavigationItem 使用具体 stable code；普通 Admin 创建的 NavigationItem 不要求 code。

### 2.1 Historical migration context

历史 `V12__preset_site_structure.sql` 曾为上述表增加 `preset` 并标记当时确认的初始化对象；后继 migration 也曾在保持 `preset=true` 的前提下调整 Party navigation。历史 migration 文件不回改。

从 EU-41 accepted current baseline 起，Backend active Flyway 为：

```text
V1__current_cms_schema.sql
V2__site_provisioning_schema_capabilities.sql
```

它只承担 Generic CMS Schema / site-neutral provisioning capability，不创建 JilinJobs preset rows。Fresh Generic DB → Site Package first apply 才建立当前 JilinJobs stable structure。旧 V12/V2/V3 development lineage 只用于历史追溯，不承担 Current Runtime lifecycle。

## 3. Backend 约束

各 Mapper 在 Admin 查询中读取 `preset`，并映射到只读响应模型。

各 Service 在写入时执行最终保护：

- Column：preset 不可删除；preset `alias` 不可变化；
- NavigationLocation：preset 不可删除；稳定 `code` 不可变化；
- NavigationItem：preset 不可删除；package-owned stable `code` 不由普通 Admin 改写；
- PageGroup：preset `alias` 不可变化，当前没有删除 API；
- Page：preset 不可删除；preset `alias` 不可变化；
- CmsList：preset 不可删除；稳定 `code` 不可变化；
- AdvertisementSlot：preset 不可删除；稳定 `code` 不可变化；
- SiteConfig：preset 定义不可删除；稳定 `key` 不可变化，属性值更新不受影响。

普通 Create / Update Request DTO 不暴露可切换 `preset` 的能力，防止客户端自行获得或移除保护状态。Navigation stable code 的 package provisioning identity 也不等于普通运营字段。

## 4. Admin Frontend

Admin API 类型读取 `preset:boolean`，但 Draft 类型不暴露该字段。

管理页面统一采用：

- 预置 Tag；
- 稳定身份字段 disabled；
- 删除按钮隐藏，或容器下拉菜单保留但 disabled；
- 不增加 preset 编辑开关。

这些前端限制只用于清晰交互，不能替代 Backend 拒绝逻辑。

## 5. 验证策略

Backend Unit Tests 覆盖代表性删除和 stable identity 保护。

Admin Browser E2E 的 `preset-protection.spec.ts` 在当前 Fresh Runtime lifecycle 中验证：

```text
Generic Backend Flyway
→ JilinJobs Site Package stable structure reconcile
→ optional one-time bootstrap
→ Admin / Public Runtime
```

至少验证：

- Generic Fresh DB 在 Site Package apply 前没有 JilinJobs preset rows；
- Site Package first apply 后代表性预置对象 API 输出 `preset=true`；
- 直接 DELETE/PUT 绕过 UI 时 Backend 拒绝；
- 运行期创建对象 `preset=false` 且仍可删除；
- NavigationItem 的 package stable code 与普通 operator-created item boundary；
- Admin 预置 Tag 和删除入口状态；
- bootstrap-created CmsListItem / Advertisement 仍是普通 operator-managed Runtime Data。

现有 CI 继续负责 Backend tests + bootJar、Public/Admin build、Fresh MySQL Generic Flyway、Site Package composition、Public Browser Regression、Admin Browser E2E 和 Playwright Evidence。

由于 Site Package / Schema ownership 变化会影响 Fresh Runtime 组合，相关变更不能机械继承旧 V12-era Runtime/Human Review Evidence；最终 Head 必须按 `docs/technical/verification-strategy.md` 取得与当前 claim 匹配的 Current Evidence。

## 6. AI Review 检查点

最终收敛前检查：

1. preset 与 systemFlag 不混用；
2. 普通 DTO 无法写 preset；
3. 删除保护位于 Service/Backend；
4. 稳定 Alias/Code/Key 不可被绕过；
5. Site Package 不接管 operator-created data；
6. Article/ListItem/Advertisement 等运营成员没有被过度保护；
7. Public Site 数据契约未被 preset 标记改变；
8. 旧 E2E 若仍假设预置容器可删除或依赖旧 Flyway seed，应按当前 Specification 修正为 Stale Verification Contract，而不是削弱 Backend 保护或恢复旧 lifecycle。
