# CMS Core / JilinJobs Site Package Boundary Technical Plan

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- `docs/specifications/cms-site-package-boundary.md`
- GitHub Issue #77
- GitHub Issue #92（EU-42 后的跨边界 Phase 顺序）
- `docs/technical/cms-architecture.md`
- `docs/technical/public-frontend-replaceability.md`
- `data-migrations/README.md`

## Status

- Technical Planning: **ACTIVE / ACCEPTED**
- Completed Execution Units: **EU-37 / EU-38 / EU-39 / EU-40 / EU-41 / EU-42**
- Current Phase 1 execution: **EU-43 — Current Authority Semantic Reconciliation**
- Issue #77: **OPEN**

## Decision

在 Issue #60 / E1～E3 前保持 **Generic CMS Core + JilinJobs Site Package + Historical Migration + Replaceable Public Renderer** 四层长期责任边界。

EU-41 / EU-42 已把当前 accepted runtime foundation 收敛为：

- Backend Flyway 是 **Generic CMS Schema-only migration lineage**；
- stable JilinJobs site structure 由 `sites/jilinjobs/structure/**` 长期 reconcile；
- Fresh JilinJobs 初始运营默认数据由 `sites/jilinjobs/bootstrap/**` 一次性建立；
- stable JilinJobs assets 的唯一版本化 source owner 为 `sites/jilinjobs/assets/**`，Runtime target 保持 `/static/**`；
- historical/canonical data 继续由 `data-migrations/**` 管理；
- Site Package 与 CMS 通过 Schema / Provisioning capability contract 组合，不共享 Flyway migration order。

EU-37～EU-42 已完成对应实现与 Current Evidence。后续 application/migration boundary 与 final compatibility re-entry 不再以旧 Slice D 直接推进，而按 Issue #92 Phase 1 → Phase 2 → Phase 3 顺序执行。

## 1. Generic CMS Schema lineage

### 1.1 Current accepted baseline

Active Backend Flyway：

```text
backend/src/main/resources/db/migration/
├─ V1__current_cms_schema.sql
└─ V2__site_provisioning_schema_capabilities.sql
```

V1 保持 current Generic CMS schema baseline。

Generic V2 只包含：

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

### 1.2 Controlled baseline replacement / historical context

EU-31 曾建立 development V1 schema + V2 site data baseline，EU-39 曾增加旧 V3 navigation identity。EU-41 因四层 boundary 收敛执行一次受控 development baseline replacement：

- 退休旧 site-data V2；
- 退休旧 navigation V3；
- 使用新的 Generic V2 表达仍需要的通用 Schema capability；
- pre-EU-41 development DB recreate；
- 不提供旧 development history 的 in-place repair。

该选择符合 EU-31 已确认 boundary：当前没有 production / persistent database in-place upgrade requirement，development database 允许重建。历史 V2/V3 只承担 historical implementation / compatibility evidence，不再参与 current runtime responsibility。

## 2. Stable Site Package structure

`sites/jilinjobs/structure/**` 是长期 JilinJobs Site structure Authority。

当前 98 个 stable objects：

- Column；
- PageGroup / Page；
- NavigationLocation / NavigationItem；
- SiteConfig；
- CmsList definitions；
- AdvertisementSlot。

Provisioner：

- 按 stable identity create / reconcile；
- 新建 package-owned row 为 `preset=true`；
- 不接管 `preset=false` operator objects；
- second apply idempotent；
- transactionally rollback failed apply。

Generic Flyway 完成后数据库中没有 JilinJobs rows，Site Package first apply 直接创建全部 98 个 stable objects，包括 40 条 coded NavigationItem。EU-39 Legacy adoption 规则保留为历史实现证据，不再是 current Fresh baseline lifecycle。

## 3. One-time Site bootstrap

### 3.1 Package layout

```text
sites/jilinjobs/bootstrap/
├─ manifest.json
└─ initial-data.sql
```

`manifest.json` 声明独立 `bootstrapId`、SQL artifact path 与 SHA-256 digest。

`initial-data.sql` 精确创建原旧 V2 剩余的七条 operational defaults：

- HOME_CAROUSEL：1；
- SITE_RELATED：5；
- HOME_RECRUITMENT_PROMO Advertisement：1。

这里的“原旧 V2”仅说明 provenance；Current ownership 已属于 one-time Site bootstrap，不得据此恢复 Backend Flyway responsibility。

### 3.2 Bootstrap runtime contract

`SitePackageBootstrapper`：

1. 复用 `SitePackageLoader` 验证 package identity / stable package integrity；
2. 解析 `bootstrap/manifest.json`；
3. 检查 bootstrap id、bounded path、SHA-256；
4. 查询 `(package_id, bootstrap_id)` completion state；
5. state 不存在时，在单事务中执行 SQL 并写入 state；
6. state 已存在时返回 `ALREADY_APPLIED`，不再次执行 SQL；
7. SQL 或 state insert 失败时 rollback。

完成状态记录 originally applied digest。Current bootstrap artifact 可以随兼容 CMS Schema 更新；Existing Site 的 originally applied digest 可以与 current digest 不同，但 **同一 bootstrap identity 不重新执行**。

### 3.3 Runtime activation

普通 Runtime：

```text
CMS_SITE_PACKAGE_ROOT=/site-package
```

执行 stable structure reconcile 与 stable asset projection，不执行 one-time bootstrap。

Fresh-install orchestration：

```text
CMS_SITE_PACKAGE_ROOT=/site-package
CMS_SITE_PACKAGE_BOOTSTRAP_ON_START=true
```

Spring lifecycle 的必要顺序：

```text
Generic Flyway initializer
→ stable Site Package composition
→ one-time bootstrap (when explicitly enabled)
→ Runtime
```

bootstrap 必须发生在 stable list definitions / AdvertisementSlot 等依赖已经 provision 后。CLI `bootstrapSitePackage` 同样先 provision stable structure，再执行 bootstrap。

## 4. Operator ownership / no-resurrection

七条 bootstrap rows 初始化后立即变成普通 Runtime Data：

- CmsListItem / Advertisement 无 Site stable identity；
- 不设 `preset` ownership；
- SitePackageProvisioner 不 reconcile；
- ordinary restart 不执行 bootstrap；
- repeated explicit bootstrap 由 completion state 拦截。

因此 operator edit/delete 必须保留；package structure upgrade 不覆盖；bootstrap artifact 后续更新也不使 Existing Site 重放同一 bootstrap identity。

这些 defaults 不需要 Historical Migration provenance / fingerprint / adoption model。

## 5. Historical Canonical Migration boundary

Party / Main Historical Migration 当前组合为：

```text
Generic Schema
→ stable Site Package reconcile
→ canonical importer
```

Canonical importer 只依赖 stable target identities，不接管 JilinJobs Main operational bootstrap，也不接管 stable Site asset manifest。

Current accepted compatibility evidence 已证明：

- Party Fresh canonical import；
- 183 article current Runtime Dataset；
- Party carousel 4 items；
- import idempotency；
- EU-29 accepted → EU-30 current compatibility；
- provenance / fingerprints / resource integrity 保持。

Phase 2 将进一步收敛 Backend Application / Generic Content Migration Application boundary；在该 Phase 完成前，不从当前 Party-specific Gradle task 反向定义长期 Generic architecture。

## 6. Repository Runtime / CI / Review composition

### 6.1 Integrated CI Fresh Runtime

Current Fresh JilinJobs verification lifecycle：

```text
Generic Flyway
→ mounted /site-package stable reconcile
→ bootstrap-on-start=true (Fresh Site scenarios)
→ stable asset projection from the same Site Package root
→ optional canonical import
→ Public/Admin/Browser verification
```

Flyway history 中不得出现 JilinJobs instance data。

### 6.2 Review Environment

Fresh Review startup / reset 必须从同一版本化 Site Package root 取得 structure、bootstrap 与 stable assets。Review reset 后如需要 Party canonical content，再执行独立 canonical import。

不允许测试代码自建 Site stable structure，也不允许复制第二份 static baseline 绕过 asset manifest/projection。

### 6.3 Generic Core proof

专项 verifier 在 Flyway migrate 后、任何 Site Package apply 前检查：

- JilinJobs Columns = 0；
- JilinJobs Navigation = 0；
- CmsList / AdvertisementSlot site instances = 0；
- CmsListItem / Advertisement operational defaults = 0。

## 7. Verification strategy

`verifyStableSiteStructure` 至少覆盖：

- Generic Fresh Schema has no Site rows；
- Site first apply creates 98；
- second apply unchanged；
- operator navigation preserved；
- stable-code mutation restored；
- operational rows remain 0。

`verifyRuntimeSitePackageComposition` 至少覆盖：

- Generic Flyway creates no Site data；
- root-only Runtime creates/reconciles stable objects；
- root-only Runtime never bootstraps operational data；
- second Runtime idempotent；
- no-root Generic context enables neither Site composition nor bootstrap。

`verifySiteBootstrapBaselineSeparation` 至少覆盖：

- Flyway history contains only Generic V1/V2；
- Fresh bootstrap creates 6 ListItems + 1 Advertisement；
- repeated bootstrap = ALREADY_APPLIED；
- operator delete/edit 后 ordinary Runtime preserves modifications；
- explicit repeated bootstrap also preserves modifications；
- state row remains one。

EU-42 asset verification 至少覆盖：

- `sites/jilinjobs/assets/**` manifest package identity、bounded source/target 与 SHA-256；
- empty Runtime Static Root create-if-missing projection；
- existing regular target no overwrite；
- stable target protected delete / explicit replace；
- `/static/uploads/**` exclusion；
- CI / Review 由同一 Site Package root composition。

Exact-head / Integration / Post-Integration evidence 由各 EU work artifact、PR 和 GitHub Actions 保存；历史 PASS 不自动替代未来 Head 的 Current Evidence。

## 8. Static asset boundary

Current technical boundary：

```text
sites/jilinjobs/assets/**
  ↓ assets/manifest.json: packageId + source + /static target + SHA-256
SitePackageAssetManifestLoader
  ↓ bounded / integrity validation
SitePackageAssetProjector
  ↓ create-if-missing only
configured CMS_STATIC_ROOT
  ↓ Backend /static/**
Public Renderer / Admin StaticResource
```

关键策略：

- stable source bytes 只存在于 `sites/jilinjobs/assets/**`；
- 原 `site-baseline/static/**` 只属于 historical source path，不再是 Current owner；
- projector 不覆盖 existing regular target，保留 operator explicit replace；
- stable targets 进入 StaticResource protected-path，ordinary delete fail-fast；
- `/static/uploads/**` 明确排除，继续作为 mutable Runtime Store；
- Historical canonical assets 不进入 stable Site asset manifest；
- CI / Review Environment 不复制第二份 baseline，而是从 empty Runtime Static Root 观察 package projection；
- 当前没有 force-upgrade semantics；如未来出现受控 asset upgrade requirement，必须重新规划 overwrite / conflict / rollback contract。

## 9. Rollback / compatibility boundary

EU-41 以后 accepted boundary：

- pre-EU-41 development DB recreate；
- current Fresh install 只使用 Generic V1/V2 + Site Package；
- Repository 不再支持旧 development V1/V2/V3 active lineage；
- 后续 Generic Backend migration 从 V3 恢复 append-only；
- Existing Runtime operator data 不通过 bootstrap replay 升级。

## 10. Current execution / remaining sequence

EU-37～EU-42 已完成。当前 Issue #92 Phase 1 正在执行 **EU-43 — Current Authority Semantic Reconciliation**；其 scope 仅为 documentation currentness，不改变上述 Runtime 技术基线。

EU-43 完成后，后继工作仍需独立规划，顺序为：

```text
Phase 1  remaining documentation convergence
→ Phase 2  Generic Historical Migration & Backend Application Boundary
→ Phase 3  Canonical Migration Compatibility & E1～E3 Re-entry Gate
→ Issue #60 / E1～E3
```

旧 “Slice D — direct next step” 已被 Issue #92 supersede。Repository Split Readiness Assessment 继续保持四层 boundary 完整闭环后的独立 Planning Candidate。

任何后继 Unit 都必须重新经过 `slice-work → readiness-check`；EU-42 / EU-43 identifier 或历史 PASS 不授予后继 Execute Authority。
