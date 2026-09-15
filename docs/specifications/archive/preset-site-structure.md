# 预置站点结构保护 Specification

## 1. 目的

本文固化 CMS 预置站点结构保护规则。其产品保护语义仍源于 `docs/requirements/information-publishing.md`，当前初始化 / ownership lifecycle 进一步遵循 `docs/requirements/cms-site-package-boundary.md` 与 `docs/specifications/cms-site-package-boundary.md`。

该规则解决的是“站点规划基线被运营人员误删导致页面结构失效”的问题，不引入认证、角色、超级管理员或权限体系，也不把 CMS 运营内容整体变成不可修改的系统数据。

历史阶段曾通过 Flyway migration 为当前站点对象引入 / 标记 `preset`，并将中心党建导航从占位迁移为 `/party/`。这些 migration 事实只承担历史实现追溯；从 EU-41 accepted baseline 起，Generic Backend Flyway 不再承担 JilinJobs preset rows，当前 JilinJobs stable structure 由 `sites/jilinjobs/structure/**` provision / reconcile。

## 2. `preset` 语义

`preset=true` 表示该记录由受控 Site Provisioning 建立，属于网站规划稳定结构，是公开 URL、页面结构、固定容器或站点运行契约所依赖的对象。

`preset` 与历史 `system/systemFlag` 语义分离：

- `preset` 只表达“Site Package stable structure ownership / protection”；
- `system/systemFlag` 不作为本规则的删除保护依据；
- 本阶段不因为引入 `preset` 而机械删除或重解释既有 `system/systemFlag`。

`preset` 是 Generic CMS 持久化并只读输出的结构属性。普通管理 API 不接受客户端设置或取消 `preset`；管理员运行期新增对象默认 `preset=false`。具体哪些对象属于 JilinJobs preset 由 Site Package 决定，不由 Generic CMS Core 写死。

## 3. 基本行为

对 `preset=true` 的对象：

1. Backend 必须拒绝删除，不能只依赖管理端隐藏按钮；
2. 具有稳定 `Alias / Code / Key` 的对象不得修改该身份字段；
3. 名称、说明、排序、启停、图片策略以及对象自身允许的其他运营字段仍可按原模型维护；
4. 管理端必须明确显示“预置”标识，并将不可执行的删除入口隐藏或禁用；
5. 管理端编辑预置对象时，对稳定 `Alias / Code / Key` 使用只读/禁用输入并给出稳定身份提示；
6. 直接绕过前端调用 Admin API 时，Backend 仍必须执行相同保护。

`preset` 不表示“完全不可修改”。除稳定身份和删除行为外，不额外锁死结构关系或运营字段，除非后续 Requirement 明确提出更强约束。

## 4. 对象范围

| CMS 对象 | `preset` | 稳定身份保护 | 删除保护 | 说明 |
|---|---|---|---|---|
| Column 栏目 | 支持 | `alias` | 是 | 保护站点规划栏目；Article 不因此成为预置内容 |
| NavigationLocation 导航位置 | 支持 | `code` | 是 | `MAIN / HOME_SHORTCUT / HOME_QUICK` 属于 JilinJobs 正式位置基线 |
| NavigationItem 导航条目 | 支持 | provisioning-only `code` | 是 | `cms_navigation.code` 是 nullable Generic capability；普通 operator-created item 不要求 code，具体 JilinJobs stable code 属 Site Package |
| PageGroup 单页分组 | 支持 | `alias` | 由现有模型无删除 API + preset 语义共同保护 | 分组继续为平级结构 |
| Page 单页 | 支持 | `alias` | 是 | 保护稳定公开 URL；正文和呈现相关运营字段仍可维护 |
| CmsList 列表定义 | 支持 | `code` | 是 | 保护页面依赖的数据容器；CmsListItem 仍是普通运营内容 |
| AdvertisementSlot 展示位 | 支持 | `code` | 是 | 保护页面稳定展示区域；Advertisement 仍是普通运营内容 |
| SiteProperty / SiteConfig 定义 | 支持 | `key` | 是 | 属性值仍可正常维护，包括轮播相关属性 |
| Article | 不使用 | - | 否 | 普通运营内容 |
| CmsListItem | 不使用 | - | 否 | 普通运营内容；Fresh Site 默认项可由 one-time bootstrap 创建，但不成为 preset |
| Advertisement | 不使用 | - | 否 | 普通运营内容；Fresh Site 默认项可由 one-time bootstrap 创建，但不成为 preset |
| StaticResource | 不以 preset 保护 | - | - | stable Site target 与运行时引用使用现有 protected-resource contract |

## 5. 当前 Provisioning 基线

当前 JilinJobs stable structure 的版本化 Authority：

```text
sites/jilinjobs/structure/**
```

Site Package 只将其明确声明的 stable objects 建立 / reconcile 为 `preset=true`。不得使用“当前表中所有数据”之类宽泛规则，把既有运行期自定义数据整体升级为预置数据。

预置单页必须按“独立单页 / 具体 PageGroup”上下文使用稳定 identity，不能仅根据可能在不同分组重复出现的 Alias 进行宽泛匹配。

当前正式导航位置包括：

- `MAIN`
- `HOME_SHORTCUT`
- `HOME_QUICK`

NavigationItem 当前具有 provisioning-only nullable `code`；JilinJobs Site Package 使用具体 stable code 建立 / reconcile 当前正式导航。普通 Admin API 创建的 NavigationItem 不因 Generic Schema 支持 `code` 而自动成为 package-owned preset。

历史 V12 以及 V12 之后的 Party 导航 migration 不回改；它们只说明旧 development lineage 如何得到过同类最终行为。从 EU-41 current baseline 起，Fresh Generic CMS database 不包含 JilinJobs preset rows；Site Package first apply 创建当前 stable structure。Fresh Site 的 CmsListItem / Advertisement 初始默认数据由 `sites/jilinjobs/bootstrap/**` 一次性建立，初始化后属于 operator-managed Runtime Data。

## 6. 管理端交互

管理端采用一致的轻量表达：

- 记录名称附近显示“预置” Tag；
- 对预置容器，删除菜单可显示为 disabled，以明确说明该能力存在但当前对象受保护；
- 表格直接操作场景可以隐藏预置对象的删除按钮；
- 编辑对话框中稳定 Alias/Code/Key 禁止修改；
- 不增加新的“预置管理”“系统设置”菜单，也不提供普通管理员切换 preset 的开关。

这些前端限制只用于清晰交互，不能替代 Backend 拒绝逻辑。

## 7. Acceptance Obligations

当前必须至少验证：

1. Generic Fresh Database 只建立 Generic CMS Schema / site-neutral provisioning capability，不包含 JilinJobs site-instance rows；
2. JilinJobs Site Package first apply 能建立当前 stable structure，代表性 Column、NavigationLocation、NavigationItem、PageGroup、Page、CmsList、AdvertisementSlot、SiteProperty 返回 `preset=true`；
3. second Site Package apply 幂等，并按 stable identity reconcile package-owned structure；
4. 直接调用 Backend 删除上述预置对象时被拒绝；
5. 直接调用 Backend 修改预置 Column/PageGroup/Page 以及其他具有 stable Alias/Code/Key 对象的身份字段时被拒绝；
6. 普通管理 API 新增的结构对象返回 `preset=false`，并可按原规则删除；普通 NavigationItem 不要求 provisioning code；
7. Article、CmsListItem、Advertisement 等运营内容仍保持正常 CRUD，bootstrap-created members 也不获得 preset ownership；
8. 管理端能够识别预置对象，删除入口被禁用/移除且稳定身份字段不可编辑；
9. 当前中心党建预置导航由 Site Package stable identity 建立 / reconcile 为 `/party/`，并保持删除保护；
10. Public Site 既有行为和视觉结构不得因 `preset` 标记或 ownership 迁移本身发生变化。
