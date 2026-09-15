# CMS Core / Site Package / Public Renderer 边界规格说明

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- GitHub Issue #77
- `docs/requirements/information-publishing.md`
- `docs/specifications/public-site.md`
- `data-migrations/README.md`

## 状态

- 规格：**CURRENT / ACCEPTED**；
- Foundation：EU-37～EU-42 已完成；
- Cross-boundary convergence：EU-43～EU-48 已完成；
- Main Page formal content：EU-52 已完成；
- Main ListItem bootstrap：EU-53 已完成；
- Page Content Architecture：EU-55 已完成。

本文是长期 WHAT / WHY contract，不拥有 Current Execution Gate。

## 1. 四层 contract

```text
Generic CMS Core
        ↓
JilinJobs Site Package
        ↓
Historical Content Migration
        ↓
Runtime CMS Data
        ↓
Replaceable Public Renderer
```

物理同仓不改变各层 source ownership 与 lifecycle。

## 2. Generic CMS Core

Core 提供 site-neutral schema、domain、Admin/Public API、provisioning、bootstrap completion-state、migration primitives 与 Page Content Architecture capability。

Current Page 通用字段包括：

- `contentModel`：内容形态；
- `rendererKey`：稳定 renderer identity；
- `contentOwner`：内容 ownership；
- `bodyHtml`：富文本内容；
- `structuredPayload`：结构化 payload；
- `embedUrl`：外部集成地址。

Core 不持有 JilinJobs aliases、List codes、文案、Page 正文、联系方式或具体 stable asset values。

## 3. JilinJobs Site Package

`sites/jilinjobs/` 是 JilinJobs 产品定义的版本化 owner：

```text
manifest.json
structure/**     # stable structure / accepted page defaults
bootstrap/**     # one-time site initialization data
assets/**        # stable site assets
```

### 3.1 Stable structure

Current stable structure 包括 Column、PageGroup / Page、Navigation、CmsList definitions、AdvertisementSlots 与 SiteProperty definitions / accepted values 等 package-owned objects。

这些对象通过 stable identity 与 preset / reconcile contract 建立和维护。

### 3.2 Page

EU-52 已将接受的 Main Page 正式默认内容和 stable resources 纳入 Site Package；EU-55 在此基础上完成 site-neutral Page Content Architecture。

当前 `guide/jypq` 不再是 flattened `RICH_TEXT` Page，而是：

```text
contentModel = STRUCTURED
rendererKey = JILINJOBS_GUIDE_CARDS
contentOwner = OPERATOR
structuredPayload.kind = CARD_COLLECTION
structuredPayload.schemaVersion = 1
```

其他 Page 按各自当前 package definition 使用 `RICH_TEXT / STRUCTURED / NONE` 等内容形态。任何未来 Page 内容升级都必须继续遵守 ownership / adoption precondition，不得把 package default 当作永久 overwrite flag。

### 3.3 Main ListItem

Main ListItem completion 已由 EU-53 采用 existing Site Package bootstrap SQL 完成，不引入新 Generic Core capability。

当前 Main bootstrap 包含：

- `HOME_CAROUSEL` 初始化成员；
- `SITE_RELATED`：5 条；
- `SITE_REGIONAL_GRADUATES`：31 条；
- `SITE_JILIN_UNIVERSITIES`：60 条。

完成 bootstrap 后，这些 rows 成为普通 `CmsListItem` Runtime Data，继续使用现有 Admin/Public contract；Site Package 不 reconcile、不 resurrect、不为它们增加 stable package membership identity。

因此以下内容不是当前 contract：

- `cms_list_item` stable code；
- Site Package `list-items` structure type；
- ListItem runtime reconcile；
- adoption fingerprint；
- preset/delete protection；
- absence-based removal。

## 4. One-time bootstrap

Lifecycle：

```text
stable structure provision
→ explicit one-time bootstrap
→ cms_site_bootstrap_state records completion
→ ordinary operator-managed Runtime Data
```

同一 bootstrap identity 不重放。未来旧环境补种 / 数据升级若成为真实需求，必须独立建模，不得改变当前 one-time contract。

## 5. Stable assets

Stable Site assets 来源于 `sites/jilinjobs/assets/**`，通过 manifest/catalog 投影到 `/static/**`。

- stable target 纳入 protected-resource contract；
- mutable `/static/uploads/**` 不属于 Site Package stable asset ownership；
- Historical Article resources 留在 migration ownership；
- Page accepted stable resources 可以由明确 Authority 提升为 Site Package asset。

## 6. Historical Migration

Historical Migration 持有 canonical historical content、resource evidence、legacy identity、fingerprint、compatibility 与 import lifecycle。

Main current migration scope 为 Article-only。Main Page 和 Main bootstrap ListItem 不属于 Historical Migration input。

Party accepted canonical data / compatibility 不因 Main ownership 结论被追溯改写。

## 7. Runtime composition

Current composition：

```text
Generic Flyway V1～V4
→ Site Package stable structure reconcile
→ stable asset projection
→ explicit one-time bootstrap（仅需要 Fresh Site defaults 的场景）
→ optional Historical Canonical Migration
→ Runtime / Public contracts
```

具体 Flyway file list 以 Repository migration directory 与 `docs/technical/cms-architecture.md` 为 Current owner。

## 8. Replaceable Public Renderer

Public Renderer 只消费 public contracts，不拥有 Site Package 或 Historical Migration source definition。Current Main / Party renderer 继续遵循当前 ADR 和 Public Specification；替换 renderer 不改变上述 ownership。

## 9. 验收边界

后续触达本规格时必须保持：

- Site-neutral Core 与 JilinJobs-specific values 分离；
- stable structure 与 one-time runtime defaults 分离；
- Main ListItem bootstrap 语义不被误解释为 stable reconcile；
- Page structured / rich-text content model 与 Site Package definition 一致；
- stable asset / Runtime upload / Historical resource 三种 ownership 分离；
- Public Renderer 不依赖 Admin-only contract；
- 当前行为变化必须来自新的 Requirement / Specification，而不是旧历史 planning 文档。
