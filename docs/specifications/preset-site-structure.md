# 预置站点结构保护 Specification

## 1. 目的

本文固化 2026-08-31 人工确认的 CMS 预置站点结构保护规则，作为 `docs/requirements/information-publishing.md` V4.6 的后续增量 Specification。

该规则解决的是“站点规划基线被运营人员误删导致页面结构失效”的问题，不引入认证、角色、超级管理员或权限体系，也不把 CMS 运营内容整体变成不可修改的系统数据。

## 2. `preset` 语义

`preset=true` 表示该记录属于网站规划或初始化基线的一部分，是公开 URL、页面结构、固定容器或站点运行契约所依赖的预置对象。

`preset` 与历史 `system/systemFlag` 语义分离：

- `preset` 只表达“站点规划基线保护”；
- `system/systemFlag` 不作为本规则的删除保护依据；
- 本阶段不因为引入 `preset` 而机械删除或重解释既有 `system/systemFlag`。

`preset` 是后端持久化并只读输出的结构属性。普通管理 API 不接受客户端设置或取消 `preset`；管理员运行期新增对象默认 `preset=false`。

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
| NavigationLocation 导航位置 | 支持 | `code` | 是 | `MAIN / HOME_SHORTCUT / HOME_QUICK` 属于正式位置基线 |
| NavigationItem 导航条目 | 支持 | 无独立 Alias/Code | 是 | 预置导航可继续修改名称、目标、排序、启停和图标 |
| PageGroup 单页分组 | 支持 | `alias` | 由现有模型无删除 API + preset 语义共同保护 | 分组继续为平级结构 |
| Page 单页 | 支持 | `alias` | 是 | 保护稳定公开 URL；正文和呈现相关运营字段仍可维护 |
| CmsList 列表定义 | 支持 | `code` | 是 | 保护页面依赖的数据容器；CmsListItem 仍是普通运营内容 |
| AdvertisementSlot 展示位 | 支持 | `code` | 是 | 保护页面稳定展示区域；Advertisement 仍是普通运营内容 |
| SiteProperty / SiteConfig 定义 | 支持 | `key` | 是 | 属性值仍可正常维护，包括首页轮播间隔 |
| Article | 不使用 | - | 否 | 普通运营内容 |
| CmsListItem | 不使用 | - | 否 | 普通运营内容 |
| Advertisement | 不使用 | - | 否 | 普通运营内容 |
| StaticResource | 不以 preset 保护 | - | - | 继续使用现有关键资源保护机制 |

## 5. 初始化基线

Flyway 只把已经由当前站点规划明确建立的记录标记为 `preset=true`。不得使用“当前表中所有数据”之类的宽泛迁移规则，把既有运行期自定义数据整体升级为预置数据。

预置单页必须按“独立单页 / 具体 PageGroup”上下文标记，不能仅根据可能在不同分组重复出现的 Alias 进行宽泛匹配。

当前正式导航位置：

- `MAIN`
- `HOME_SHORTCUT`
- `HOME_QUICK`

当前页面稳定依赖的列表、展示位和站点属性定义按现有初始化基线逐项标记，不据此推导未来新增业务对象自动成为 preset。

## 6. 管理端交互

管理端采用一致的轻量表达：

- 记录名称附近显示“预置” Tag；
- 对预置容器，删除菜单可显示为 disabled，以明确说明该能力存在但当前对象受保护；
- 表格直接操作场景可以隐藏预置对象的删除按钮；
- 编辑对话框中稳定 Alias/Code/Key 禁止修改；
- 不增加新的“预置管理”“系统设置”菜单，也不提供普通管理员切换 preset 的开关。

## 7. Acceptance Obligations

必须至少验证：

1. Fresh Database 从 V1 完整迁移到包含 preset 的当前版本成功；
2. 当前规划基线中的代表性 Column、NavigationLocation、NavigationItem、PageGroup、Page、CmsList、AdvertisementSlot、SiteProperty 返回 `preset=true`；
3. 直接调用 Backend 删除上述预置对象时被拒绝；
4. 直接调用 Backend 修改预置 Column/PageGroup/Page 的 Alias 时被拒绝；
5. 普通管理 API 新增的结构对象返回 `preset=false`，并可按原规则删除；
6. Article、CmsListItem、Advertisement 等运营内容仍保持正常 CRUD；
7. 管理端能够识别预置对象，删除入口被禁用/移除且稳定身份字段不可编辑；
8. Public Site 既有行为和视觉结构不得因 `preset` 标记本身发生变化。
