# EU-37 — Site Package Contract & Provisioner Foundation

## 1. Identity

- Identifier：`EU-37`
- Source Candidate：GitHub Issue #77 / Slice A
- Requirement：`docs/requirements/cms-site-package-boundary.md`
- Specification：`docs/specifications/cms-site-package-boundary.md`
- Technical Plan：`docs/technical/cms-site-package-boundary.md`
- Planning integration：PR #78
- Execute baseline：`main@01073b131fef5c2e978060c55a4f50bcbc8b2fc4`
- Baseline Post-Integration CI：#731 / run `34015839251`，Backend / Public / Admin / Integrated Browser PASS
- Implementation PR：#79
- Verified implementation Head：`0d52343079da5e75dfa88cb79544328e1314907c`
- Implementation CI：#733 / run `34016519207`，Backend / Public / Admin / Integrated Browser PASS
- Status：**READY TO INTEGRATE**

EU-37 由 Issue #77 的 Slice Work 在 Planning Authority 集成后形成。Identifier 只承担追踪；Readiness Check 已在 Issue #77 记录为 PASS，因此本 Unit 获得 Execute 权限。当前实现与 exact-head 验证已闭环，但在 PR 合并并取得 `main` Post-Integration Verification 前不得标记 COMPLETED。

## 2. Intent

建立最小 Site Package contract 与受控 Provisioner，证明具体 Site stable structure 可以在 Generic CMS Schema 上独立恢复，而不需要继续把 Site Definition 与 Flyway schema lifecycle 绑定。

EU-37 是 Foundation proof，不迁移当前完整 JilinJobs Site baseline。

## 3. Scope

1. 建立 Site Package v1 manifest / structure contract 与机器可审计 JSON Schema；
2. v1 Foundation 首轮只实现 Column structure：
   - stable identity = `alias`；
   - parent relationship = `parentAlias`；
   - default mutable fields = name / coverPolicy / sortOrder / enabled；
3. Package preflight：
   - manifest identity / schema version；
   - relative safe path；
   - SHA-256 integrity；
   - duplicate alias；
   - self-parent / parent cycle；
   - supported structure kind；
4. Provisioning：
   - logical parent resolution；
   - absent stable alias → insert with `preset=true`；
   - existing `preset=true` → deterministic reconcile；
   - existing same alias `preset=false` → conflict，不接管运营数据；
   - package 外 Runtime data 不 broad-delete / broad-reset；
   - package 缺项不自动 deprovision；
5. 提供 `provisionSitePackage` CLI / Gradle entry；
6. 在真实 MySQL 上用 `spring.flyway.target=1` 只应用 V1 Generic Schema，然后验证：
   - first apply；
   - parent relationship；
   - `preset=true`；
   - operator-created `preset=false` Column；
   - second apply idempotency；
   - package 外 Runtime Column 保持不变；
7. Backend unit/full build 与全仓 CI 回归。

## 4. Explicit Non-goals

- 不修改、删除或迁空 `V2__current_preset_data.sql`；
- 不创建正式 `sites/jilinjobs/**` 全量 Site Definition；
- 不迁移 Navigation / Page / List / Advertisement / SiteConfig baseline；
- 不移动 `site-baseline/static/**`；
- 不修改 Public Renderer / Admin 产品行为 / Gateway；
- 不修改 Party canonical dataset；
- 不执行 E1～E3；
- 不拆 Repository，不引入 Git Submodule、plugin framework、multi-site / tenant framework；
- 不新增 NavigationItem identity 字段。

## 5. Acceptance Mapping

| Obligation | EU-37 Evidence |
|---|---|
| package identity/version/integrity | manifest + loader preflight + `SitePackageLoaderTest` |
| stable Column identity | alias-based mapper/provisioner |
| dependency resolution | parentAlias → parent runtime id |
| controlled preset creation | insert writes `preset=true` |
| operator data isolation | same-alias non-preset conflict + unrelated runtime preservation |
| idempotent apply | second apply returns unchanged |
| Fresh Generic Schema proof | MySQL 8.4 + Flyway target=1 verification task |
| no V2 responsibility migration | final changed-file / diff audit |
| no renderer/product behavior change | CI #733 Public/Admin build + Integrated Browser PASS |

## 6. Readiness

### Authority / Intent — PASS

Issue #77 Requirement、Specification、Technical Plan 已通过 PR #78 集成到 `main`，Slice A 范围与 deferred architecture 明确。

### Verticality — PASS

Contract、preflight、persistence 与真实 Fresh-schema proof 必须共同存在才能证明 Foundation；拆开后任何半单元都不能独立闭合“Site Definition 可从 Flyway Site SQL 责任中分离”的核心主张。

### Implementation fit — PASS

当前 V1 已提供 generic `cms_column` schema、唯一 alias、parent FK 与 `preset`；现有产品不需要新增字段或依赖即可完成首个 logical identity proof。

### Verification feasibility — PASS

CI 为 Backend job 提供独立 MySQL database，并通过 `spring.flyway.target=1` 保证验证数据库只包含 Generic Schema；测试 fixture 不进入正式 Runtime Authority。

### Baseline — PASS

`main@01073b131fef5c2e978060c55a4f50bcbc8b2fc4` 的 Post-Integration CI #731 / run `34015839251` 四层验证全部 PASS。

### Scope / rollback — PASS

EU-37 不接管 V2 当前正式 baseline responsibility，不做数据删除或 binary relocation；失败可直接放弃 feature branch，不影响 accepted Runtime recovery path。

## 7. Execute / Convergence Evidence

### 7.1 Site Package contract 与 preflight — PASS

Repository 新增：

- `sites/schemas/manifest-v1.schema.json`；
- `sites/schemas/columns-v1.schema.json`；
- `SitePackageLoader`；
- Foundation test fixture。

`SitePackageLoaderTest` 自动验证：

1. 合法 package + digest 可加载；
2. 即便外部文件 digest 正确，`../` path traversal 仍被拒绝；
3. digest mismatch 被拒绝；
4. duplicate stable alias 被拒绝；
5. cyclic parent relationship 被拒绝。

CI #733 Backend `clean test bootJar` PASS，证明上述 preflight tests 与全量 Backend tests 同时通过。

### 7.2 Controlled Provisioning — PASS

`SitePackageProvisioner` 使用窄 `SitePackageColumnMapper`：

- `alias` 是首轮 stable identity；
- `parentAlias` 在 apply 前解析为 Runtime parent ID；
- 新 package object 写入 `preset=true`；
- existing preset 只 reconcile mutable fields，不改变 stable alias；
- same alias 的 `preset=false` object 返回 conflict，不被 package 接管；
- package 外 Runtime object 不执行 broad delete / reset；
- package 缺失项不触发自动 deprovision。

`SitePackageProvisionerTest` 覆盖 parent/child、second apply idempotency、non-preset collision 与 preset reconcile。

### 7.3 Real MySQL V1-only Foundation Verification — PASS

CI #733 Backend job 使用 MySQL 8.4 独立数据库 `jilinjobs_site_package`，执行：

```text
spring.flyway.target=1
→ V1__current_cms_schema.sql
→ Site Package Foundation first apply
→ ordinary ColumnService creates operator fixture
→ Site Package Foundation second apply
```

该验证没有应用 V2 Site baseline。CI 日志证明 Flyway 从 Empty Schema 只迁移到 `v1`。

Foundation summary：

```text
first apply:
  inserted = 2
  updated = 0
  unchanged = 0
  conflicts = 0

second apply:
  inserted = 0
  updated = 0
  unchanged = 2
  conflicts = 0

operatorPreset = false
```

由此证明：Generic Schema 可以独立承载 Site Package provision；parent relationship 正确；package structure 获得 preset protection；second apply 幂等；普通 Runtime data 不被误提升为 preset。

### 7.4 Repository Regression — PASS

PR #79 implementation Head `0d52343079da5e75dfa88cb79544328e1314907c` 的 CI #733 / run `34016519207`：

- Backend full tests + bootJar：PASS；
- EU-37 real MySQL Foundation verification：PASS；
- Public frontend build：PASS；
- Admin frontend build：PASS；
- Public Browser regression：PASS；
- Admin Browser regression：PASS；
- Integrated Browser verification：PASS。

因此 EU-37 Foundation 没有改变当前 V2 正式站点 Runtime、Public Renderer、Admin 产品行为或 Gateway contract。

### 7.5 Final Scope Audit — PASS

PR #79 在 verified implementation Head 上的 changed-file audit 只包含：

- Site Package production Foundation；
- Site Package unit / real-DB verification；
- test fixture；
- generic Site Package JSON schemas / README；
- Backend Gradle / CI verification entry；
- 本 EU Work Authority。

确认未修改：

- `V2__current_preset_data.sql`；
- 正式 JilinJobs Site baseline 数据；
- `frontend/public-site/**` production source；
- `frontend/admin/**` production source；
- `site-baseline/static/**`；
- `data-migrations/party/**` canonical dataset；
- E1～E3 产品内容；
- Repository split / Submodule / multi-site / plugin framework 等 deferred architecture。

## 8. Integration Gate

EU-37 implementation 已完成并取得 current implementation evidence，当前状态为 **READY TO INTEGRATE**。

合并 PR #79 前必须继续满足：

1. PR 最终 exact Head 的 Current CI PASS；
2. 若仅新增本 Work Evidence 造成 Head 前移，必须重新取得该最终 Head 的 CI；
3. final diff scope 继续不扩大；
4. 按 Repository Authority 由人工决定是否合并。

PR 合并后必须取得新 `main` Post-Integration CI，并同步 Roadmap / Issue #77；只有届时 EU-37 才可标记 **COMPLETED**，随后才能从最新 `main` 推进 Slice B。
