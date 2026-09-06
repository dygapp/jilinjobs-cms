# CMS Core / Site Package / Public Renderer 规划状态

## 当前结论

Issue #77 仍是 Issue #60 / E1～E3 主站正式内容工作前的前置架构收敛入口。

已集成：

- Slice A：**EU-37 — Site Package Contract & Provisioner Foundation**；
- Slice B stable structure：**EU-38 — Stable Site Structure Package Migration**；
- Slice B Navigation identity：**EU-39 — Navigation Stable Identity & Site Package Reconcile**；
- Slice B Runtime composition：**EU-40 — Explicit Site Package Runtime Composition Activation**。

当前 Ready / executing Unit：

- **EU-41 — Site Bootstrap & Generic Schema Baseline Separation**；
- Readiness Authority：Issue #77 `#issuecomment-5560955644`；
- Branch：`feature/eu-41-site-bootstrap-baseline-separation`；
- Draft PR：#88；
- Execution base：`main@b20dd7a22daa5270692d3273d1c7309bfc050a69`。

四层长期边界保持：Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer。

## 已完成

### EU-37～EU-39 — Contract / stable structure / Navigation identity

已建立 Site Package manifest、narrow idempotent provisioner，并把 Column、PageGroup、Page、NavigationLocation、SiteConfig、CmsList definition、AdvertisementSlot 与 40 条 NavigationItem 表达为具有 stable identity 的 JilinJobs Site Package structure。

### EU-40 — Explicit Runtime composition

正式 Repository Runtime / importer 已可以在 Flyway 后显式执行 `cms.site-package.root` Site Package reconcile；未配置 root 时 Generic CMS context 不隐式启用 Site Package。

EU-40 final / post-integration evidence 已完成并集成。EU-40 有意保留旧 V2 compatibility baseline，为后续 operational seed classification 留出边界。

## EU-41 — 当前执行范围

EU-40 后 audit 已完成七条剩余 operational seed 分类：

- 6 条 `CmsListItem`；
- 1 条 `Advertisement`；
- 均为 **Fresh Site initial operational defaults**；
- 初始化后属于 operator-managed Runtime Data；
- 不是 stable preset structure；
- 不是 Historical Canonical Migration provenance unit。

用户进一步明确：Backend Schema migration 与 Site data initialization 必须走独立演进路线，不能通过共享 Flyway V1/V2/V3 排号继续形成隐性耦合。

因此 EU-41 采用：

```text
Generic CMS Backend
  Flyway Schema-only lineage
        ↓
JilinJobs Site Package
  stable structure reconcile
        ↓
  one-time current-schema bootstrap
        ↓
optional Historical Canonical Migration
        ↓
Runtime
```

### Backend migration target

```text
backend/src/main/resources/db/migration/
├─ V1__current_cms_schema.sql
└─ V2__site_provisioning_schema_capabilities.sql
```

新的 V2 只承担 Generic Schema capabilities：Navigation stable identity + site-neutral bootstrap-state。JilinJobs data 不再占用 Backend migration number。

EU-41 集成后下一次 Generic Schema change 从 V3 继续 append-only。

### Site bootstrap target

```text
sites/jilinjobs/bootstrap/
├─ manifest.json
└─ initial-data.sql
```

Bootstrap 没有 migration version sequence；artifact 始终适配 current compatible CMS Schema。

完成状态通过 generic `(packageId, bootstrapId)` state 记录，不使用 `flyway_schema_history`。完成后普通 Runtime reconcile 不再执行 bootstrap，因此 operator edit/delete 不会被 overwrite / resurrect。

## EU-41 Verification Gate

必须同时通过：

1. Generic CMS Fresh DB 无 JilinJobs instance data；
2. stable Site Package first apply = current 98 objects；
3. first bootstrap = 6 ListItems + 1 Advertisement；
4. repeated bootstrap = already applied / no duplicate；
5. operator edit/delete 后 ordinary restart 与 repeated explicit bootstrap 均 no overwrite / no resurrection；
6. Site bootstrap 不进入 Backend Flyway history；
7. Repository CI Public/Admin/Integrated Browser 行为保持；
8. Canonical Migration Verification；
9. EU-30 Migration Upgrade Verification；
10. Review Environment Fresh lifecycle；
11. final diff scope / Authority consistency。

PR #88 initial Head `830f620cbe22c474ef27045db49aa9cc27e030c2` 的 Site Package Verification #21 已 PASS。Final promotion / integration 仍以 PR final Head evidence 为准。

## EU-41 后剩余 Planning 范围

EU-41 完成后 Issue #77 剩余：

1. **Slice C — Site Asset Ownership & Runtime Composition**：让稳定 Site assets 的 package/manifest/runtime ownership 显式化；是否移动 `site-baseline/static/**` 只根据真实 ambiguity 决定。
2. **Slice D — Canonical Migration Compatibility & E1～E3 Re-entry**：在最终 Site Package + asset lifecycle 上关闭 Party/Main canonical compatibility，并判断 Issue #60 / E1～E3 是否解除前置等待。
3. **Repository Split Readiness Assessment**：仅在四层 boundary 完成后独立评估，不自动拆仓。

上述均不是当前 Ready EU，不继承 EU-41 execute authority。

## 规划边界

继续 deferred：

- Public 技术栈替换；
- Repository split / Git Submodule；
- multi-site / plugin framework；
- 无证据的大规模目录搬迁；
- Issue #60 / E1～E3 正式内容工作，直到 Issue #77 re-entry gate 完成。
