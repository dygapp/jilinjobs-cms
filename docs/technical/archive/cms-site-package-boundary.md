# CMS Core / JilinJobs Site Package 边界技术方案

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- `docs/specifications/cms-site-package-boundary.md`
- GitHub Issue #77
- `docs/technical/cms-architecture.md`
- `data-migrations/README.md`

## 状态

- Technical Authority：**CURRENT / ACCEPTED**；
- Foundation：EU-37～EU-42 已完成；
- Cross-boundary convergence：EU-43～EU-48 已完成；
- Main Page formal-content adoption：EU-52 已完成；
- Main ListItem bootstrap completion：EU-53 已完成；
- Page Content Architecture：EU-55 已完成。

本文不维护 Current Execution Gate。

## 1. Current Runtime composition

```text
Generic CMS Flyway V1～V4
→ JilinJobs stable Site Package reconcile
→ stable Site asset projection
→ optional one-time bootstrap
→ optional Historical Canonical Migration
→ Runtime / public contracts
```

Flyway 只承担 site-neutral Generic CMS schema / capability evolution，不写具体 JilinJobs instance rows。

## 2. Site Package stable structure

`backend/modules/cms-core` 中的 Site Package capability 当前处理：

- columns；
- page-groups；
- pages；
- navigation-locations；
- navigation-items；
- site-config；
- lists；
- advertisement-slots。

具体 package definitions 位于 `sites/jilinjobs/structure/**`；stable assets 位于 `sites/jilinjobs/assets/**`。

`CmsList` definition 属于 stable structure；`CmsListItem` membership 当前不属于 stable structure。

## 3. Page Content Architecture

EU-55 已把 Page 从单一 `renderMode` 表达收敛为显式内容模型 / renderer / ownership：

- `contentModel`；
- `rendererKey`；
- `contentOwner`；
- `bodyHtml`；
- `structuredPayload`；
- `embedUrl`。

数据库演进由 `V4__page_content_architecture.sql` 完成；Site Package Page definition 使用同一语义。

Current package 中 `guide/jypq` 已采用：

```text
contentModel = STRUCTURED
rendererKey = JILINJOBS_GUIDE_CARDS
contentOwner = OPERATOR
structuredPayload.kind = CARD_COLLECTION
structuredPayload.schemaVersion = 1
```

不得再从 EU-52 历史文档恢复为 flattened `RICH_TEXT`。

### 3.1 Content ownership

- `OPERATOR`：运行期允许普通内容维护；
- `ENGINEERING`：由工程 renderer / implementation 主导；
- `EXTERNAL`：主要内容来自外部集成；
- 具体允许值与校验以 Current Page domain implementation / Page Content Architecture 为准。

Site Package create-time default 不等于永久 overwrite authority。普通 reconcile 必须继续保护 operator divergence；显式 package 内容升级必须有明确 precondition / adoption authority。

## 4. Main ListItem 当前实现

EU-53 已确认 Main ListItem 不需要 stable membership reconcile。

Current `sites/jilinjobs/bootstrap/initial-data.sql` 负责 Main 一次性初始化：

- `HOME_CAROUSEL`；
- `SITE_RELATED`；
- `SITE_REGIONAL_GRADUATES`；
- `SITE_JILIN_UNIVERSITIES`；
- 同一 bootstrap artifact 中明确接受的其他普通 defaults。

`SitePackageBootstrapper` 通过 `cms_site_bootstrap_state` 保证相同 bootstrap identity 只执行一次。

执行后：

- rows 是普通 `CmsListItem`；
- operator 可以按现有 Admin contract 维护；
- Site Package stable reconcile 不更新或恢复这些 rows；
- repeated bootstrap 返回 already-applied，不重新插入。

因此以下历史规划已经失效：

- 新增 `cms_list_item` package stable code；
- 新增 Site Package `list-items` structure type；
- 为 Main ListItem 建立 stable reconcile / adoption / delete protection / resurrection。

如果未来出现 Existing Site 在线补种需求，应建立独立、版本化的数据升级方案，不得修改 ordinary bootstrap 语义来绕过 lifecycle。

## 5. Stable asset projection

`sites/jilinjobs/assets/**` + manifest/catalog 是 stable asset source。

Technical contract：

- source / target path 必须安全、确定；
- source bytes 必须满足声明 SHA-256；
- stable `/static/**` target 纳入 protected-resource 计算；
- projection 不把 `/static/uploads/**` 纳入 stable ownership；
- ordinary mutable upload 不通过 Site Package reconcile；
- Historical migration resources 不自动进入 stable asset manifest。

## 6. Historical Migration boundary

`data-migrations/**` 保存 canonical historical content / provenance / compatibility。

Generic migration application 与 Party adapter 已完成 application / core de-specialization；这些完成事实不重新赋予旧 Phase 2 Planning 文档 Current Gate。

Main Historical Migration 当前只处理 Article；Page 与 Main bootstrap ListItem 不属于 Main canonical import units。

## 7. Backend application boundary

Current Backend 已是 Gradle multi-project：

```text
backend/
├── modules/cms-core
└── apps/
    ├── cms-server
    └── content-migration
```

`cms-server → cms-core`，`content-migration → cms-core`；两个 application 不互相依赖。Site Package 的通用 loader / provisioner / bootstrap / asset capability 属于 shared Core，HTTP transport 属于 Server application，Historical Migration command / adapter 属于 Content Migration application。

## 8. Flyway 当前 lineage

Current migration directory：

`backend/modules/cms-core/src/main/resources/db/migration/`

当前 active lineage：

```text
V1__current_cms_schema.sql
V2__site_provisioning_schema_capabilities.sql
V3__page_content_migration_mapping.sql
V4__page_content_architecture.sql
```

后续 Generic CMS Schema evolution 必须从当前 V4 之后 append-only；不得继续引用 EU-41 时期“下一次从 V3 开始”的历史说明。

## 9. 验证

涉及本边界的变更按影响至少验证：

- Fresh Generic Flyway 完整 V1～V4 chain；
- Site Package first / repeated reconcile；
- Page content model / renderer / owner / payload；
- Main one-time bootstrap first apply / already-applied / no-resurrection；
- stable asset integrity / projection / protection；
- Historical Migration idempotency / conflict / compatibility；
- Server / Content Migration application boundary；
- Public/Admin/Browser regression（当行为边界受影响时）。

不得用旧 migration transcript、旧 Site bootstrap 分类或历史 Execution Unit 文档替代 Current implementation / Authority 验证。