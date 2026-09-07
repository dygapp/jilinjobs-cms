# Party Canonical Migration Runtime Composition

Party Canonical Migration 继续遵循仓库根 `data-migrations/README.md` 的 Historical Content Migration 边界；本文件只补充 JilinJobs Consumer 当前正式 importer 的 Runtime composition 顺序。

## Runtime preparation

Repository-owned Party importer 统一通过 Backend Gradle 入口执行：

- `importPartyHistoricalContent`
- `importPartyCarousel`

这两个 Consumer orchestration task 在启动 Spring migration context 时显式提供 `CMS_SITE_PACKAGE_ROOT=../sites/jilinjobs`。因此当前正式顺序为：

```text
Generic Backend Flyway current schema
→ JilinJobs stable Site Package reconcile
→ Party Canonical Migration import
```

Site Package apply 复用通用 `SitePackageProvisioner`，Canonical Dataset 只引用稳定 Column alias / List code 等 identity，不接管 Site structure definition、one-time bootstrap 或 stable Site asset ownership。

EU-40 时期曾保留 `V2__current_preset_data.sql` compatibility responsibility；该责任已由 EU-41 controlled development baseline replacement 正式退休。当前 active Backend Flyway 仅为 `V1__current_cms_schema.sql` + `V2__site_provisioning_schema_capabilities.sql`，不再提供任何 JilinJobs instance rows。旧 V2 只可作为历史实现 / upgrade evidence 被引用，不承担 Current Runtime composition responsibility。

Canonical Migration Verification、EU-30 Migration Upgrade Verification 与 Review Environment 中调用上述 Gradle importer 的路径必须复用该顺序，不另建第二套 provisioning 逻辑。Fresh Site 的普通运营默认数据如需建立，由独立 one-time Site bootstrap lifecycle 负责；Canonical historical import 本身不依赖或接管该 bootstrap。
