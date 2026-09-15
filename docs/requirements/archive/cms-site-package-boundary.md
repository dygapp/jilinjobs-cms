# CMS Core / Site Package / Public Renderer 边界需求

## 状态

- 来源：GitHub Issue #77；
- 角色：**CURRENT / ACCEPTED ARCHITECTURE BOUNDARY**；
- Foundation：EU-37～EU-42 已完成；
- Repository / migration convergence：EU-43～EU-48 已完成；
- Main Page 正式内容：EU-52 已完成；
- Main ListItem bootstrap：EU-53 已完成；
- Page Content Architecture：EU-55 已完成。

本文只定义长期责任边界，不拥有 Current Execution Gate。

## 1. 四层长期边界

当前长期模型为：

```text
Generic CMS Core
        ↓ generic schema / domain / API / provisioning capability
JilinJobs Site Package
        ↓ stable site structure + stable page defaults + one-time bootstrap + stable assets
Historical Content Migration
        ↓ canonical historical content / provenance / compatibility
Runtime CMS Data
        ↓ public contracts
Replaceable Public Renderer
```

各层当前可以同仓，但逻辑 ownership 与 lifecycle 不因物理位置合并。

## 2. Generic CMS Core

Generic CMS Core 负责：

- site-neutral database schema 与 append-only Flyway evolution；
- Article / Page / Navigation / CmsList / Advertisement / SiteProperty / Resource 等通用领域能力；
- Admin / Public API；
- site-neutral provisioning、bootstrap completion-state 与 migration primitives；
- Page Content Architecture 的通用能力，包括 `contentModel / rendererKey / contentOwner / structuredPayload`。

Generic Core 不得硬编码 JilinJobs aliases、列表 code、导航文案、Page 正文、联系方式或站点资产值。

当前 active Generic Flyway lineage 已演进为 V1～V4；具体 Current migration owner 以 `docs/technical/cms-architecture.md` 与 Repository migration directory 为准。

## 3. JilinJobs Site Package

`sites/jilinjobs/**` 是 JilinJobs 站点产品定义的版本化 owner。

### 3.1 Stable structure

`sites/jilinjobs/structure/**` 当前拥有：

- Columns；
- PageGroups / Pages；
- NavigationLocations / NavigationItems；
- CmsList definitions；
- AdvertisementSlots；
- SiteProperty definitions / accepted stable values；
- 其他已明确纳入 package 的 stable structure。

Stable structure 使用 repository-owned stable identity，不依赖 Runtime numeric id，并按当前 preset / reconcile contract 建立和维护。

### 3.2 Page content

Page 的长期通用内容模型由 Page Content Architecture 定义。当前 Page 可组合：

- `contentModel`：内容形态，例如 `RICH_TEXT / STRUCTURED / NONE`；
- `rendererKey`：稳定 renderer identity；
- `contentOwner`：`OPERATOR / ENGINEERING / EXTERNAL` 等 ownership；
- `bodyHtml`：富文本内容；
- `structuredPayload`：结构化 payload；
- `embedUrl`：需要时的外部集成地址。

Main 正式 Page 默认内容与 stable Page assets 属于 Site Package。EU-52 的历史 formal-content adoption contract 继续作为来源与一次性 adoption 追溯，但其中把 `guide/jypq` 视为普通 `RICH_TEXT` 的表达已被 EU-55 后继事实取代；当前 `guide/jypq` 使用 `STRUCTURED + JILINJOBS_GUIDE_CARDS`。

普通运营编辑与 package reconcile 必须继续遵守 Page ownership guard，不得把 Site Package default 解释为永久覆盖 operator content 的授权。

### 3.3 Main ListItem

Main ListItem 的当前长期结论是 **Site Package one-time bootstrap data**，不是 stable runtime reconcile structure。

当前 Main bootstrap 至少包含：

- `HOME_CAROUSEL` 初始化成员；
- `SITE_RELATED` 5 条；
- `SITE_REGIONAL_GRADUATES` 31 条；
- `SITE_JILIN_UNIVERSITIES` 60 条；
- 其他由同一 bootstrap artifact 明确接受的普通初始化数据。

生命周期：

```text
stable Site structure ready
→ sites/jilinjobs/bootstrap/** 一次性执行
→ cms_site_bootstrap_state 记录完成
→ 后续启动不重放
→ 数据成为普通 operator-managed Runtime Data
```

因此当前**不存在 stable ListItem identity / package membership / runtime reconcile capability gap**。不得从 EU-50～EU-52 阶段的旧规划重新推导 `list-items` structure type、stable code、adoption fingerprint、delete protection 或 resurrection 需求。

如果未来出现“已执行旧 bootstrap 的生产环境需要在线补种 / 升级”的真实需求，必须单独形成数据升级 Authority；不得把该问题隐式转化为永久 reconcile。

## 4. One-time bootstrap

One-time bootstrap 只承担普通初始运营数据，不承担 stable structure reconcile。

要求：

- 同一 `(packageId, bootstrapId)` 成功后不得重复执行；
- bootstrap-created rows 默认是普通 operator-managed Runtime Data；
- operator 后续修改 / 删除不得因 ordinary restart 或 Site Package reconcile 被自动恢复；
- bootstrap 不使用 Flyway `Vx` 编号；
- 任何 bootstrap 内容升级都必须依据当前 schema 与明确 Authority 重新判断 compatibility。

## 5. Stable assets

`sites/jilinjobs/assets/**` 是 JilinJobs stable Site asset 的版本化 source owner。

- manifest / catalog 负责 source、target、SHA-256 与 protected-path 关系；
- projection 只按当前 contract 恢复 stable `/static/**` targets；
- `/static/uploads/**` 是 mutable Runtime Store，不属于 stable asset ownership；
- Historical Article resources 继续属于 `data-migrations/**`，除非单独 Authority 将特定资源提升为 stable Site asset。

## 6. Historical Content Migration

`data-migrations/**` 负责需要 source provenance、fingerprint、canonical validation 与 import lifecycle 的历史内容。

Main 当前 Historical Migration ownership 为 Article-only：

- INTERNAL Article；
- EXTERNAL_LINK Article；
- Article body images / attachments；
- migration evidence / provenance / legacy mappings。

Main Page 与 Main bootstrap ListItem 不属于 Main Historical Migration input。

Party 等其他 scope 可以依据自身已接受 Authority 保留 Historical ListItem canonical data；Main 的当前结论不追溯改写 Party accepted history。

## 7. Replaceable Public Renderer

Public Renderer 只消费 Public API、Site Package / Runtime 投影与稳定 URL contract，不成为 CMS Product Data Authority。

Public implementation 可替换，但不得因为 renderer 技术变化改变 Article / Page / List 等业务 ownership。当前 renderer 仍按已接受 ADR 与 Public Technical Authority 运行，未来替换需单独 Architecture Decision。

## 8. 验证义务

后续触达本边界时至少验证：

1. Generic Flyway 不写入具体 JilinJobs instance rows；
2. Site Package stable structure 可从 repository versioned source 重建并幂等 reconcile；
3. one-time bootstrap first apply / already-applied / no-resurrection 语义保持；
4. Main ListItem 不被误升级为 stable reconcile object；
5. Page Content Architecture 的 content model / renderer / owner / payload 与 Site Package 定义一致；
6. stable assets 可从空 Runtime static root 投影并保持 integrity；
7. Historical Migration 与 Site Package ownership 不交叉；
8. Public Renderer 只消费公开 contract，不反向拥有产品数据。

## 9. 非目标

本文不自动授权：

- 新建 stable ListItem reconcile framework；
- 重新设计 Page renderer；
- 重新打开 Main Historical Migration；
- 选择新的 Public rendering 技术；
- 修改当前产品 Scope 或 User-visible Behavior；
- 创建新的 Execution Unit。
