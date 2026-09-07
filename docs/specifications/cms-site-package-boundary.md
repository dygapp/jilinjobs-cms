# CMS Core / Site Package / Public Renderer 边界 Specification

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- GitHub Issue #77
- `docs/specifications/public-frontend-replaceability.md`
- `docs/technical/cms-architecture.md`
- `data-migrations/README.md`
- `docs/specifications/preset-site-structure.md`

## Status

- Specification: **ACCEPTED / ACTIVE**
- Technical Planning: **ACTIVE**
- Completed Execution Units: **EU-37 / EU-38 / EU-39 / EU-40 / EU-41 / EU-42**
- Current Ready Execution Unit: **NONE**
- Issue #77: **OPEN**

## 1. Four-layer boundary

项目长期按以下责任层理解当前系统：

```text
Generic CMS Core
        ↓ provides schema / domain / Admin / Public / provisioning capabilities
JilinJobs Site Package
        ↓ stable structure + one-time fresh-site defaults + stable site assets
Historical Content Migration
        ↓ imports canonical historical operational content
Runtime CMS Data
        ↓ consumed through stable public contracts
Replaceable Public Renderer
```

这些责任当前可以保留在同一个 Git Repository 中；逻辑 / lifecycle ownership 优先于物理拆仓。

## 2. Generic CMS Core contract

Generic CMS Core 包含：

- CMS 数据库 schema；
- Backend Flyway **Schema evolution lineage**；
- Column / Article / Page / Navigation / Listing / Advertisement / SiteConfig / Resource 等通用领域模型；
- Admin API 与 Admin Frontend；
- Public API / DTO / Resource projection；
- `preset` 结构保护能力；
- Site Provisioning 所需的最小通用校验 / 持久化能力；
- site-neutral one-time bootstrap-state capability；
- 通用 Verification Contract。

Generic CMS Core 不内建吉林就业网站专属事实，例如具体栏目 alias/name、导航树、外部业务 URL、联系电话、ICP备案、Logo/Banner 路径、具体 Site list code 或初始运营内容。

`preset` 的语义为：**由受控 Site Provisioning 建立、具有稳定 identity 并受结构保护的对象**。具体哪些对象是 JilinJobs preset 由 Site Package 决定。

`cms_navigation.code` 是 Generic provisioning capability：nullable，普通 operator-created NavigationItem 不要求 code；具体 code value 属于 Site Package。

EU-41 进一步规定 Generic CMS 可以提供 `cms_site_bootstrap_state` 一类通用能力，但其 Schema 不得包含 JilinJobs `packageId` / bootstrap 内容或 Site-specific migration number。

## 3. JilinJobs Site Package contract

Site Package 是吉林就业网站的版本化站点定义与 Fresh Site 安装 Authority，不是 Historical Content Migration。

### 3.1 Stable structure

长期 stable structure 当前包括：

- Columns；
- PageGroups / Pages；
- NavigationLocations / NavigationItems；
- CmsList definitions；
- AdvertisementSlots；
- SiteConfig definitions / accepted values；
- stable site assets（EU-42 accepted owner：`sites/jilinjobs/assets/**` + integrity manifest + Runtime projection）；
- object relationships；
- `preset` expectation；
- package identity / version / integrity metadata。

长期关系不得依赖 Runtime 自增 ID。当前 accepted stable identities：

- Column → `alias`；
- PageGroup → `alias`；
- Page → `groupAlias + alias`；
- NavigationLocation → `code`；
- NavigationItem → provisioning-only `code`；
- CmsList → `code`；
- AdvertisementSlot → `code`；
- SiteConfig → `key` / `config_key`。

EU-38 / EU-39 已将当前 98 个 stable objects 表达在 `sites/jilinjobs/structure/**`。普通 Site Package runtime composition 对这些对象执行 stable identity reconcile。

### 3.2 One-time operational bootstrap

Site Package 还可以包含 **Fresh Site 初始运营默认数据**，但该数据与 stable structure 的 ownership 不同。

当前 JilinJobs bootstrap 定义：

```text
sites/jilinjobs/bootstrap/
├─ manifest.json
└─ initial-data.sql
```

当前 bootstrap 精确包含：

- `HOME_CAROUSEL`：1 条 `CmsListItem`；
- `SITE_RELATED`：5 条 `CmsListItem`；
- `HOME_RECRUITMENT_PROMO`：1 条 `Advertisement`。

Bootstrap contract：

1. 只在 Fresh JilinJobs Site 安装生命周期显式执行；
2. 必须在 Generic CMS Schema ready 且 stable Site Package structure 已 provision 后执行；
3. artifact 有独立 `bootstrapId` 与 SHA-256 integrity metadata；
4. 不使用 Flyway `Vx` migration number；
5. 内容始终面向当前兼容 CMS Schema 维护，Schema 改变时可以直接更新 current bootstrap artifact；
6. 首次成功后记录 `packageId + bootstrapId + applied digest`；
7. 后续普通 Runtime reconcile 不执行 bootstrap；
8. 即使当前 bootstrap artifact 已更新，已完成 bootstrap 的 Existing Site 也不重新执行旧 bootstrap identity；
9. bootstrap 创建的对象初始化后即成为普通 operator-managed Runtime Data，不获得 stable Site identity / preset ownership；
10. operator edit/delete 必须保持，不得被 restart / reconcile / repeated bootstrap overwrite 或 resurrect。

### 3.3 Site Package dependency contract

Site Package 可以依赖 CMS 提供的 Schema / Provisioning capability，但不得依赖 Backend Flyway migration ordering。

禁止长期 contract：

```text
Core V1 → Site V2 → Core V3
```

接受的 contract：

```text
Generic CMS current compatible schema/capabilities
→ Site stable provision
→ optional one-time Site bootstrap
```

未来如需要显式 compatibility version，应表达 CMS capability/schema contract version，而不是共享 migration sequence。

## 4. Backend Flyway boundary

EU-41 已将 active Backend Flyway 收敛为纯 Generic CMS Schema lineage。

### Current development baseline

```text
V1__current_cms_schema.sql
V2__site_provisioning_schema_capabilities.sql
```

- V1：当前 Generic CMS schema baseline；
- V2：Generic Navigation stable identity + site-neutral bootstrap-state Schema capability；
- JilinJobs columns / navigation rows / SiteConfig / list definitions / operational defaults 不进入 active Backend Flyway；
- EU-41 集成后下一次 Generic CMS Schema change 从 V3 继续 append-only。

该变化属于 development baseline controlled replacement：EU-31 Requirement 已明确当前没有 production / persistent DB in-place upgrade requirement，pre-reset development databases 可以 recreate。EU-41 不为已退休的旧 development V1/V2/V3 history 设计 repair migration。

Site bootstrap 不重新编号为 Backend V2/V3，也不建立自己的 migration number sequence。

## 5. Historical Migration boundary

`data-migrations/**` 保持 Historical Content Migration 语义：

- canonical historical Articles / external links；
- historical resources / attachments；
- legacy identity / fingerprint / provenance；
- historical operational list-member migration；
- importer / reports。

Main / Party Canonical Migration 可以依赖 Site Package stable identities，但必须：

- 不依赖 Runtime numeric ID；
- 不依赖 Vue Router / component / Vite artifact；
- Import 前目标 Site stable identity 已 provision；
- 不把普通 Fresh Site bootstrap default 误当 historical provenance unit。

EU-40 已证明 importer 可以显式组合 Site Package reconcile。EU-41 已进一步验证在 Backend Flyway 不再提供 JilinJobs data 的情况下，Party canonical Fresh import 与 EU-29→EU-30 upgrade 仍可仅依赖 Generic Schema + stable Site Package + Canonical Dataset 成立。

## 6. Static asset boundary

EU-42 已关闭 stable Site asset 的 source ownership 与 Runtime composition：

- 唯一版本化 source owner：`sites/jilinjobs/assets/**`；原 `site-baseline/static/**` 不再是并行 source authority；
- `sites/jilinjobs/assets/manifest.json` 与主 Site Package 使用同一 `packageId`，并声明 schemaVersion、package-root source、公开 `/static/**` target 与 SHA-256；
- manifest loader 必须拒绝 package identity mismatch、source/target duplicate、path traversal / symlink escape、digest mismatch 与 `/static/uploads/**` target；
- `SitePackageAssetProjector` 只 create missing targets；现存普通 target 不由启动覆盖，因此 operator 显式 replace 可跨 projection/restart 保留；
- stable target 纳入 `StaticResourceService` protected-path：普通 delete 拒绝，显式 replace 允许；
- `/static/uploads/**` 继续是 mutable Runtime Store；Historical article/body/carousel migration assets 继续跟随 Canonical Migration unit；
- CI / Review Environment 必须从空 Runtime Static Root 启动，并由同一个 mounted Site Package root 同时提供 structure、bootstrap 与 stable assets；不得重新引入独立 baseline copy。

Public `/static/**` URL、Main / Party visual identity 与 Renderer behavior 均保持不变；force-upgrade / overwrite-existing policy 不属于 EU-42。

## 7. Public Renderer boundary

当前 `frontend/public-site` 是 accepted implementation，但不是 Site Authority。

Replacement-stable dependencies：

- `/api/public/**`；
- `/static/**` / public resources；
- accepted canonical routes；
- SiteConfig / Navigation / List / Page / Article 等 Public response semantics；
- Main / Party identities；
- accepted visual / responsive / SEO obligations。

Vue / Vue Router / Vite / multi-entry build / current Playwright layout 属 implementation-specific details，不进入 Site Package / migration contract。

EU-41 未改变 Public/Admin contracts 或 Product Intent；Fresh JilinJobs Runtime 通过显式 Site bootstrap 恢复当前初始可见数据。

## 8. Provisioning lifecycle

### 8.1 Generic CMS only

```text
Fresh DB
  ↓ Backend Flyway Generic Schema
Generic CMS ready
```

该状态必须包含 **0 个 JilinJobs site-instance rows**。

### 8.2 Fresh JilinJobs Site

```text
Fresh DB
  ↓ Backend Flyway Generic Schema
Generic CMS ready
  ↓ JilinJobs stable Site Package reconcile
Stable Site Structure ready
  ↓ one-time Site bootstrap
Initial operational defaults ready
  ↓ optional Canonical Historical Migration
Runtime Content ready
  ↓ Public/Admin/Integration verification
Accepted Runtime
```

### 8.3 Ordinary restart

```text
Backend schema validation/evolution
→ stable Site Package reconcile
→ no bootstrap
→ Runtime
```

Repository Fresh CI / Review Environment 可以显式使用 `CMS_SITE_PACKAGE_BOOTSTRAP_ON_START=true`；普通 runtime 只配置 `cms.site-package.root` 时不得执行 bootstrap。

## 9. Bootstrap completion semantics

当前通用 completion identity：

```text
(package_id, bootstrap_id)
```

记录：

- originally applied content SHA-256；
- applied timestamp。

语义：

- no state → 校验 artifact digest → transactionally execute current bootstrap → insert state；
- state exists → `ALREADY_APPLIED`，不再执行 SQL；
- current artifact digest 与 originally applied digest 可以不同，用于反映 Fresh-install baseline 已演进；Existing Site 不因此被重新初始化；
- bootstrap SQL 与 state insert 位于同一数据库事务边界，失败 rollback；
- state 与 Flyway history 完全独立。

## 10. Verification obligations

EU-41 acceptance 已证明：

1. Backend active migration history 只包含 Generic CMS Schema migrations；
2. Generic CMS Fresh DB 不含 JilinJobs instances；
3. Fresh Site stable provision 创建当前 98 个 objects；
4. first bootstrap 创建 6 ListItems + 1 Advertisement；
5. second bootstrap 不复制；
6. operator delete/edit 后 ordinary runtime reconcile 不 resurrect / overwrite；
7. 再次显式 bootstrap request 仍由 completion state 拦截；
8. bootstrap 不进入 `flyway_schema_history`；
9. Public/Admin/Integrated Browser current behavior 保持；
10. Canonical Migration / EU-30 Upgrade / Review Environment 在新 lifecycle 下可重复。

PR #88 final Head `a958c39a37892cf0fbcb41b8c883b2299d84f561` 的 Site Package Verification #32、CI #783、Canonical Migration Verification #165、EU-30 Migration Upgrade Verification #115 与人工评审环境 #696 全部 PASS；PR #88 已合并为 `main@6c88eea1762e8edf465833631cadff1e4c751d36`，Post-Integration Site Package Verification #33 与 CI #784 全部 PASS。EU-41 Specification acceptance 已闭环。

EU-42 final Head `37e03c3a5d7804804dcb3738a429e57e02b99e31` 的 Site Package Verification #34、CI #787 与 Review Environment #698 全部 PASS；PR #90 已合并为 `main@2c4af15df64342850391bbfe67de99b6404b3280`，Post-Integration Site Package Verification #35 与 CI #788（含 empty-root Integrated Browser）全部 PASS。EU-42 stable asset ownership / Runtime projection acceptance 已闭环。

## 11. Repository / directory boundary

本 Specification 不要求拆 Repository。

当前：

- Generic CMS Backend Schema lineage：`backend/src/main/resources/db/migration/**`；
- JilinJobs stable structure：`sites/jilinjobs/structure/**`；
- JilinJobs one-time current-schema defaults：`sites/jilinjobs/bootstrap/**`；
- Historical canonical data：`data-migrations/**`；
- stable asset source owner：`sites/jilinjobs/assets/**`；Runtime target 继续为 `/static/**`；
- Public Renderer：`frontend/public-site/**`。

四类 source 可以同仓，但 lifecycle / Authority 不共享。

## 12. Completed and remaining obligations

### 已完成并已集成

1. EU-37 Site Package contract / provisioner foundation；
2. EU-38 七类 stable structure representation；
3. EU-39 Navigation stable identity / transition adoption；
4. EU-40 explicit Runtime/importer Site Package composition；
5. EU-41 Backend Schema-only Flyway lineage + one-time current-schema Site bootstrap + operational default no-takeover/no-resurrection；
6. EU-41 exact-head / Integration / Post-Integration evidence 已闭环；
7. EU-42 stable Site asset package ownership、integrity manifest、Runtime projection、StaticResource protection 与 CI / Review empty-root composition；
8. EU-42 exact-head / Integration / Post-Integration evidence 已闭环。

### 当前 Ready Execution Unit

**NONE**。EU-42 的 Execute Authority 已随完成而终止。

### 剩余 Planning Candidates

1. Canonical Migration Compatibility & E1～E3 re-entry（Slice D）；
2. 四层 boundary 完成后的 Repository Split Readiness Assessment。

这些剩余项仍必须重新执行 current audit / `slice-work → readiness-check`，不得自动继承 EU-42 Execute 授权。

## Deferred decisions

- Public 技术栈替换；
- SSR / SSG / Hybrid；
- Repository split / Git Submodule；
- Docs / Code 分仓；
- Multi-repository Workspace implementation；
- Generic multi-site SaaS / tenant model；
- Site Package marketplace / plugin framework；
- 无证据的大规模目录重命名。
