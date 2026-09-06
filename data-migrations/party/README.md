# Party Canonical Migration Runtime Composition

Party Canonical Migration 继续遵循仓库根 `data-migrations/README.md` 的 Historical Content Migration 边界；本文件只补充 JilinJobs Consumer 当前正式 importer 的 Runtime composition 顺序。

## Runtime preparation

Repository-owned Party importer 统一通过 Backend Gradle 入口执行：

- `importPartyHistoricalContent`
- `importPartyCarousel`

这两个 Consumer orchestration task 在启动 Spring migration context 时显式提供 `CMS_SITE_PACKAGE_ROOT=../sites/jilinjobs`。因此当前正式顺序为：

```text
Flyway current schema / compatibility baseline
→ JilinJobs Site Package reconcile
→ Party Canonical Migration import
```

Site Package apply 复用通用 `SitePackageProvisioner`，Canonical Dataset 只引用稳定 Column alias / List code 等 identity，不接管 Site structure definition。

EU-40 仍保留 `V2__current_preset_data.sql` 的当前 compatibility responsibility；上述显式 composition **不表示 V2 已退场**，也不把 `CmsListItem` / `Advertisement` 等运营成员提升为 stable Site Package structure。

Canonical Migration Verification、EU-30 Migration Upgrade Verification 与 Review Environment 中调用上述 Gradle importer 的路径必须复用该顺序，不另建第二套 provisioning 逻辑。
