# CMS Core / JilinJobs Site Package Boundary Technical Plan

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- `docs/specifications/cms-site-package-boundary.md`
- GitHub Issue #77
- `docs/technical/cms-architecture.md`
- `docs/technical/public-frontend-replaceability.md`
- `data-migrations/README.md`

## Status

- Technical Planning: **ACTIVE / ACCEPTED**
- Completed Execution Units:
  - `EU-37 — Site Package Contract & Provisioner Foundation`
  - `EU-38 — Stable Site Structure Package Migration`
  - `EU-39 — Navigation Stable Identity & Site Package Reconcile`
  - `EU-40 — Explicit Site Package Runtime Composition Activation`
- Current Ready Execution Unit: **EU-41 — Site Bootstrap & Generic Schema Baseline Separation（IN EXECUTION）**
- Issue #77: **OPEN**

## Decision

在 Issue #60 / E1～E3 前完成 **Generic CMS Core + JilinJobs Site Package + Historical Migration + Replaceable Public Renderer** 四层长期责任边界。

EU-41 对 EU-40 后的 Operational Seed / V2 candidate 作出关键收敛：

- Backend Flyway 是 **Generic CMS Schema-only migration lineage**；
- stable JilinJobs site structure 由 `sites/jilinjobs/structure/**` 长期 reconcile；
- Fresh JilinJobs 初始运营默认数据由 `sites/jilinjobs/bootstrap/**` 一次性建立，不是 migration；
- historical/canonical data 继续由 `data-migrations/**` 管理；
- Site Package 与 CMS 通过 Schema / Provisioning capability contract 组合，不共享 Flyway migration order。

## 1. Generic CMS Schema lineage

### 1.1 Current EU-41 target

Active Backend Flyway target：

```text
backend/src/main/resources/db/migration/
├─ V1__current_cms_schema.sql
└─ V2__site_provisioning_schema_capabilities.sql
```

V1 保持 current Generic CMS schema baseline。

新的 Generic V2 只包含：

- nullable `cms_navigation.code`；
- `uk_cms_navigation_code`；
- generic `cms_site_bootstrap_state`。

不包含：

- JilinJobs columns / pages / navigation rows；
- SiteConfig instance values；
- CmsList / AdvertisementSlot instances；
- CmsListItem / Advertisement defaults；
- Site-specific URL / contact / resource path。

EU-41 集成后，Backend 下一次 Schema evolution 从 V3 继续 append-only。

### 1.2 Controlled baseline replacement

EU-31 曾建立 V1 schema + V2 site data baseline，并在 EU-39 增加旧 V3 navigation identity。EU-41 因四层 boundary 收敛执行一次受控 development baseline replacement：

- 删除旧 site-data V2；
- 删除旧 navigation V3；
- 使用新的 Generic V2 表达仍需要的通用 Schema capability；
- pre-EU-41 development DB recreate；
- 不提供旧 development history 的 in-place repair。

该选择符合 EU-31 Requirement 的既有 upgrade boundary：当前没有 production / persistent database in-place upgrade requirement，development database 允许重建。

## 2. Stable Site Package structure

`sites/jilinjobs/structure/**` 继续是长期 JilinJobs Site structure Authority。

当前 98 个 stable objects：

- Column；
- PageGroup / Page；
- NavigationLocation / NavigationItem；
- SiteConfig；
- CmsList definitions；
- AdvertisementSlot。

Provisioner 继续：

- 按 stable identity create / reconcile；
- 新建 package-owned row 为 `preset=true`；
- 不接管 `preset=false` operator objects；
- second apply idempotent；
- transactionally rollback failed apply。

EU-41 Fresh path 不再需要 Legacy V2 adoption。Generic Flyway 完成后数据库中没有 JilinJobs rows，Site Package first apply 直接创建全部 98 个 stable objects，包括 40 条 coded NavigationItem。

EU-39 Legacy adoption 规则保留为历史实现证据，但不再是 EU-41 后 active Fresh baseline lifecycle。

## 3. One-time Site bootstrap

### 3.1 Package layout

```text
sites/jilinjobs/bootstrap/
├─ manifest.json
└─ initial-data.sql
```

`manifest.json` 当前声明：

- `bootstrapId = initial-operational-data`；
- current SQL artifact path；
- SHA-256 digest。

`initial-data.sql` 精确创建原 V2 剩余的七条 operational defaults：

- HOME_CAROUSEL：1；
- SITE_RELATED：5；
- HOME_RECRUITMENT_PROMO Advertisement：1。

### 3.2 Bootstrap runtime contract

`SitePackageBootstrapper`：

1. 先复用 `SitePackageLoader` 验证 package identity / stable package integrity；
2. 解析 `bootstrap/manifest.json`；
3. 检查 bootstrap id、bounded path、SHA-256；
4. 查询 `(package_id, bootstrap_id)` completion state；
5. state 不存在时，在单事务中执行 SQL 并写入 state；
6. state 已存在时返回 `ALREADY_APPLIED`，不再次执行 SQL；
7. SQL 或 state insert 失败时 rollback。

完成状态记录 originally applied digest。Current bootstrap artifact 以后可以随 CMS Schema 更新；Existing Site 的 originally applied digest 可以与 current digest 不同，但 **同一 bootstrap identity 不重新执行**。

该语义确保 current-schema bootstrap 能演进，又不会把 operator data 变成 reconciled package data。

### 3.3 Runtime activation

普通 Runtime：

```text
CMS_SITE_PACKAGE_ROOT=/site-package
```

只执行 stable structure reconcile。

Fresh-install orchestration：

```text
CMS_SITE_PACKAGE_ROOT=/site-package
CMS_SITE_PACKAGE_BOOTSTRAP_ON_START=true
```

Spring bean ordering：

```text
Flyway initializer
→ sitePackageRuntimeComposition
→ sitePackageBootstrapRuntime
```

bootstrap bean 依赖 `sitePackageRuntimeComposition`，因此 list definitions / AdvertisementSlot 等 stable dependencies 必须先存在。

CLI `bootstrapSitePackage` 同样先 provision stable structure，再执行 bootstrap。

## 4. Operator ownership / no-resurrection

七条 bootstrap rows 初始化后立即变成普通 Runtime Data：

- CmsListItem 无 Site stable identity；
- Advertisement 无 Site stable identity；
- 不设 `preset` ownership；
- SitePackageProvisioner 不 reconcile；
- ordinary restart 不执行 bootstrap；
- repeated explicit bootstrap 由 completion state 拦截。

因此：

- operator edit → 保留；
- operator delete → 保留删除；
- package structure upgrade → 不覆盖；
- bootstrap artifact 为后续 Fresh Schema 更新 → Existing Site 不重放。

不需要为这七条 defaults 建立 Historical Migration provenance / fingerprint / adoption model。

## 5. Historical Canonical Migration boundary

Party / Main Historical Migration 继续保持：

```text
Generic Schema
→ stable Site Package reconcile
→ canonical importer
```

Canonical importer 只需要 stable target identities，不需要执行 JilinJobs Main operational bootstrap。

EU-41 必须重新证明：

- Party Fresh canonical import；
- 183 article current Runtime Dataset；
- Party carousel 4 items；
- import idempotency；
- EU-29 accepted → EU-30 candidate upgrade；
- provenance / fingerprints unchanged。

Canonical / Upgrade Gradle tasks 继续通过 Repository-owned `CMS_SITE_PACKAGE_ROOT` 显式 provision stable structure；不设置 bootstrap-on-start。

## 6. Repository Runtime / CI / Review composition

### 6.1 Integrated CI Fresh Runtime

CI Backend container：

```text
Generic Flyway
→ mounted /site-package stable reconcile
→ bootstrap-on-start=true
→ Public/Admin Browser verification
```

这样 Fresh CI 仍拥有 accepted homepage defaults，但 Flyway history 中不再有 JilinJobs data。

### 6.2 Review Environment

Review Environment 的两个 Fresh database startup 均必须：

- read-only mount `sites/jilinjobs` as `/site-package`；
- set `CMS_SITE_PACKAGE_ROOT=/site-package`；
- set `CMS_SITE_PACKAGE_BOOTSTRAP_ON_START=true`。

Review reset 后再执行 Party canonical import，因此 Review lifecycle 与正式 Fresh lifecycle 一致。

### 6.3 Generic Core proof

专项 verifier 在 Flyway migrate 后、任何 Site Package apply 前检查：

- JilinJobs Columns = 0；
- JilinJobs Navigation = 0；
- CmsList / AdvertisementSlot site instances = 0；
- CmsListItem / Advertisement operational defaults = 0。

这是 EU-41 最关键的 CMS/Site physical data ownership evidence。

## 7. Verification strategy

### Targeted MySQL

`verifyStableSiteStructure`：

- Generic Fresh Schema has no Site rows；
- Site first apply creates 98；
- second apply 98 unchanged；
- operator navigation preserved；
- stable-code mutation restored；
- operational rows remain 0。

`verifyRuntimeSitePackageComposition`：

- Generic Flyway creates no Site data；
- root-only Runtime creates/reconciles 98 stable objects；
- root-only Runtime never bootstraps operational data；
- second Runtime idempotent；
- no-root Generic context enables neither Site composition nor bootstrap。

`verifySiteBootstrapBaselineSeparation`：

- Flyway history contains only Generic V1/V2；
- Fresh bootstrap creates 6 ListItems + 1 Advertisement；
- repeated bootstrap = ALREADY_APPLIED；
- delete one ListItem / edit Advertisement；
- ordinary Runtime preserves modifications；
- explicit repeated bootstrap also preserves modifications；
- state row remains one。

Initial PR #88 Head `830f620cbe22c474ef27045db49aa9cc27e030c2` 的 Site Package Verification #21 已 PASS。

### Full gates before integration

- Site Package Verification；
- Repository CI Backend / Public / Admin / Integrated Browser；
- Canonical Migration Verification；
- EU-30 Migration Upgrade Verification；
- Review Environment；
- final diff-scope / Authority review。

## 8. Static asset boundary

EU-41 不处理 Slice C。

`initial-data.sql` 仍引用 accepted `/static/home/carousel-01.jpg` 与 `/static/home/recruitment-campaign.png` URL。URL contract 可以跨 lifecycle 使用，但 binary asset 的 Site Package manifest/runtime ownership 仍由 Slice C 单独收敛。

不得因为 bootstrap 引用了 `/static/**` 就让 operational bootstrap 取得 asset binary ownership。

## 9. Rollback / compatibility boundary

Feature Branch 合并前可以直接放弃。

EU-41 合并后：

- pre-EU-41 development DB recreate；
- current Fresh install 只使用新 Generic V1/V2 + Site Package；
- Repository 不再支持旧 development V1/V2/V3 active lineage；
- 后续 Generic Backend migration 从 V3 恢复 append-only；
- Existing Runtime operator data 不通过 bootstrap replay 升级。

## 10. Execution state

- Ready evidence：Issue #77 `#issuecomment-5560955644`；
- Work artifact：`docs/work/eu41-site-bootstrap-generic-schema-baseline-separation.md`；
- Branch：`feature/eu-41-site-bootstrap-baseline-separation`；
- Draft PR：#88；
- Initial targeted evidence：Site Package Verification #21 PASS；
- final exact-head / integration evidence：pending。

## 11. Remaining after EU-41

EU-41 完成后 Issue #77 继续保留：

1. Slice C — Site Asset Ownership & Runtime Composition；
2. Slice D — Canonical Migration Compatibility & E1～E3 re-entry；
3. 四层 boundary 完成后的 Repository Split Readiness Assessment。

这些均需重新经过 `slice-work → readiness-check`，不自动继承 EU-41 identifier / execute authority。
