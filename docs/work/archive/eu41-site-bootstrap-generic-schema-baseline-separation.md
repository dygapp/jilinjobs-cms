# EU-41 — Site Bootstrap & Generic Schema Baseline Separation

## Authority

- Parent Planning Candidate: GitHub Issue #77 — CMS Core / Site Package / Public Renderer 边界收敛
- Readiness amendment: Issue #77 comment `#issuecomment-5560955644`
- Execution base: `main@b20dd7a22daa5270692d3273d1c7309bfc050a69`
- Requirement: `docs/requirements/cms-site-package-boundary.md`
- Specification: `docs/specifications/cms-site-package-boundary.md`
- Technical Plan: `docs/technical/cms-site-package-boundary.md`
- Database baseline history: `docs/requirements/database-migration-baseline-convergence.md` / `docs/technical/database-migration-baseline-convergence.md`

## Status

- Execution Unit: **EU-41**
- Readiness: **PASS**
- Implementation: **COMPLETED**
- Implementation Branch: `feature/eu-41-site-bootstrap-baseline-separation`
- Pull Request: #88 — MERGED
- Final implementation Head: `a958c39a37892cf0fbcb41b8c883b2299d84f561`
- Integration commit: `main@6c88eea1762e8edf465833631cadff1e4c751d36`

## Intent

彻底解除 Generic CMS Backend Flyway 与 JilinJobs Site 实例数据之间的共享迁移序列依赖。

EU-41 将数据库生命周期明确拆成两条独立演进路线：

1. Backend Flyway 只承担 Generic CMS Schema evolution；
2. JilinJobs Site Package 以 current-schema stable structure provisioning + one-time operational bootstrap 建立站点实例。

Site Package 依赖的是 CMS Schema / Provisioning capability contract，而不是 Backend Flyway migration number。

## Scope

### Generic CMS Schema lineage

- 删除 Backend Flyway 中的 JilinJobs site-instance data migration；
- 保留 V1 current Generic CMS schema；
- 将 Navigation stable identity 与通用 Site bootstrap-state capability 收敛为新的 Generic CMS V2；
- Backend 后续 Schema evolution 从 V3 继续 append-only；
- `flyway_schema_history` 不记录任何 JilinJobs bootstrap data execution。

### Stable Site Package structure

以下长期稳定站点对象继续由 `sites/jilinjobs/structure/**` 管理并 reconcile：

- Column；
- PageGroup / Page；
- NavigationLocation / NavigationItem；
- SiteConfig；
- CmsList definition；
- AdvertisementSlot。

### One-time Site bootstrap

原 V2 中剩余的七条运营默认数据迁入 `sites/jilinjobs/bootstrap/**`：

- `HOME_CAROUSEL` 1 条；
- `SITE_RELATED` 5 条；
- `HOME_RECRUITMENT_PROMO` Advertisement 1 条。

这些数据：

- 仅在 Fresh JilinJobs Site 安装时初始化一次；
- 初始化后成为普通 operator-managed runtime data；
- 不具有 Site Package stable identity；
- 不参加普通 Runtime reconcile；
- 管理员修改后不得被覆盖；
- 管理员删除后不得被重新创建；
- 不进入 Historical Canonical Migration provenance。

### Generic bootstrap-state capability

Generic CMS Schema 提供 site-neutral `cms_site_bootstrap_state`：

- identity: `package_id + bootstrap_id`；
- 保存首次 applied content digest 与时间；
- 不依赖 `flyway_schema_history`；
- 当前 bootstrap artifact 可以随当前 CMS Schema 调整；
- 已初始化站点即使当前 bootstrap 内容发生变化，也保持已完成状态，不重新执行。

## Lifecycle

### Generic CMS only

```text
Backend Flyway current Generic Schema
→ Generic CMS Runtime
```

不得创建任何 JilinJobs Columns / Navigation / Lists / operational defaults。

### Fresh JilinJobs Site

```text
Backend Flyway current Generic Schema
→ JilinJobs stable structure provision/reconcile
→ JilinJobs one-time operational bootstrap
→ optional canonical historical migration
→ Runtime / Public / Admin
```

### Ordinary JilinJobs restart

```text
Backend Flyway schema validation/evolution
→ JilinJobs stable structure reconcile
→ no operational bootstrap
→ Runtime
```

只有明确的 fresh-install orchestration 才启用 bootstrap。Repository CI / Review Environment 的 Fresh runtime 使用 `CMS_SITE_PACKAGE_BOOTSTRAP_ON_START=true`；普通 Runtime composition 不启用该开关。

## Controlled development baseline replacement

EU-31 完成后曾恢复 append-only migration discipline，但 EU-31 Requirement 同时明确：当前阶段无 production / persistent database in-place upgrade requirement，旧 development database 可以 recreate。

EU-41 使用该既有边界执行一次受控 baseline replacement：

- 旧 Backend V2 site data migration 退出 active Flyway lineage；
- 旧 Backend V3 navigation schema migration被新的 Generic V2 取代；
- pre-EU-41 development database 必须 recreate；
- EU-41 集成后，Backend migration 再次恢复 append-only，从下一 Generic Schema change 使用 V3。

这不是把 Site migration 重新编号；Site bootstrap 根本不属于 Flyway migration sequence。

## Explicit non-goals

- 不迁移 Main / Party 新历史内容；
- 不为七条默认数据设计 provenance / fingerprint migration system；
- 不为 Site bootstrap 建立 V1/V2/V3 migration sequence；
- 不移动 `site-baseline/static/**`；
- 不修改 Public/Admin 产品设计；
- 不进入 Issue #60 / E1～E3；
- 不拆 Repository 或引入 multi-site framework。

## Acceptance

1. Backend Flyway active SQL migrations 只包含 Generic CMS Schema responsibility。
2. Generic CMS Fresh DB 中 JilinJobs site-instance rows 为 0。
3. Fresh JilinJobs installation provision 98 个 stable Site objects，并一次性创建 6 `CmsListItem` + 1 `Advertisement`。
4. 第二次 bootstrap 返回 already-applied，不复制数据。
5. bootstrap 完成后删除一条运营默认数据、修改另一条，普通 Runtime reconcile 不 resurrect / overwrite。
6. 即使再次显式请求 bootstrap，completion state 仍保护 operator-modified/deleted state。
7. Site bootstrap 不出现在 `flyway_schema_history`；Backend migration numbering 不受 Site data 占位。
8. Canonical Party import / EU-30 upgrade / Public / Admin / Integrated Browser / Review Environment 在新 lifecycle 下保持可重复。
9. 无 User-visible Product Intent change。

## Verification and Integration Evidence

### Final exact-head

Final implementation Head：`a958c39a37892cf0fbcb41b8c883b2299d84f561`。

该 exact Head 已取得：

- Site Package Verification #32 — **PASS**；
- Repository CI #783 — **PASS**，包含 Backend verify、Site Package foundation、ordinary Runtime composition、EU-41 bootstrap separation、Public browser 与 Admin browser；
- Canonical Migration Verification #165 — **PASS**；
- EU-30 Migration Upgrade Verification #115 — **PASS**；
- Review Environment #696 — **PASS**，包含 Fresh Site Package + bootstrap Runtime、AI/Public/Admin Browser、clean reset、Party canonical import/runtime、外部 Public/Party/Admin 访问与 lease lifecycle；
- unresolved review threads — **NONE**；
- Integration 前 `main` 保持 `b20dd7a22daa5270692d3273d1c7309bfc050a69`，无 base drift。

### Integration

PR #88 已按 exact Head `a958c39a…` 合并，Integration commit：

`main@6c88eea1762e8edf465833631cadff1e4c751d36`

### Post-Integration

该 Integration commit 已取得：

- Site Package Verification #33 — **PASS**；
- Repository CI #784 — **PASS**，包含 Backend、Public、Admin 与 Integrated Browser。

因此 EU-41 的 implementation、integration 与 Post-Integration Current Evidence 均已闭环，状态为 **COMPLETED**。

## Accepted Result

EU-41 完成后，当前长期责任边界为：

```text
Backend Flyway
  = Generic CMS Schema evolution only

JilinJobs Site Package / structure
  = stable site structure / config definitions

JilinJobs Site Package / bootstrap
  = Fresh install one-time operational defaults

Historical Canonical Migration
  = historical operational content / provenance
```

`V2__current_preset_data.sql` 已退出 active Backend Flyway lineage。原七条初始运营数据不再占用 Backend migration version，也不被错误提升为 Historical Migration provenance；它们在 Fresh Site bootstrap 后立即进入普通运营生命周期，并由 bootstrap-state 保证不会在后续 restart 或再次显式 bootstrap 时覆盖 / resurrect operator state。

## Rollback boundary

EU-41 已完成 Integration。由于当前仍处于明确允许 development DB recreation 的基线阶段，pre-EU-41 development databases 应重建；不提供旧 V1/V2/V3 development history 的 in-place repair。

从 `main@6c88eea1762e8edf465833631cadff1e4c751d36` 起，Backend Generic CMS Schema migration 再次恢复 append-only discipline；下一次 Generic Schema change 使用 V3 或更高后续版本。