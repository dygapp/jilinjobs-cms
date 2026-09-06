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
- Status：**EXECUTING**

EU-37 由 Issue #77 的 Slice Work 在 Planning Authority 集成后形成。Identifier 只承担追踪；Readiness Check 已在 Issue #77 记录为 PASS，因此本 Unit 获得 Execute 权限。

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
| package identity/version/integrity | manifest + loader preflight |
| stable Column identity | alias-based mapper/provisioner |
| dependency resolution | parentAlias → parent runtime id |
| controlled preset creation | insert writes `preset=true` |
| operator data isolation | same-alias non-preset conflict + unrelated runtime preservation |
| idempotent apply | second apply returns unchanged |
| Fresh Generic Schema proof | MySQL + Flyway target=1 verification task |
| no V2 responsibility migration | final diff audit |
| no renderer/product behavior change | full repository CI / Integrated Browser |

## 6. Readiness

### Authority / Intent — PASS

Issue #77 Requirement、Specification、Technical Plan 已通过 PR #78 集成到 `main`，Slice A 范围与 deferred architecture 明确。

### Verticality — PASS

Contract、preflight、persistence 与真实 Fresh-schema proof 必须共同存在才能证明 Foundation；拆开后任何半单元都不能独立闭合“Site Definition 可从 Flyway Site SQL 责任中分离”的核心主张。

### Implementation fit — PASS

当前 V1 已提供 generic `cms_column` schema、唯一 alias、parent FK 与 `preset`；现有产品不需要新增字段或依赖即可完成首个 logical identity proof。

### Verification feasibility — PASS

CI 可为 Backend job 提供独立 MySQL database，并通过 `spring.flyway.target=1` 保证验证数据库只包含 Generic Schema；测试 fixture 不进入正式 Runtime Authority。

### Baseline — PASS

`main@01073b131fef5c2e978060c55a4f50bcbc8b2fc4` 的 Post-Integration CI #731 / run `34015839251` 四层验证全部 PASS。

### Scope / rollback — PASS

EU-37 不接管 V2 当前正式 baseline responsibility，不做数据删除或 binary relocation；失败可直接放弃 feature branch，不影响 accepted Runtime recovery path。

## 7. Completion Gate

- manifest / columns schemas 与 runtime preflight 一致；
- invalid path/digest/duplicate/cycle 会失败；
- Fresh V1-only schema 能 provision parent/child fixture；
- package-created Column 为 `preset=true`；
- second apply 无 insert/update；
- same stable alias 的 `preset=false` object 不被接管；
- unrelated operator-created Column second apply 后保持原值与 `preset=false`；
- Backend unit tests PASS；
- real MySQL Foundation verification PASS；
- Backend full test + bootJar PASS；
- Public/Admin builds 与 Integrated Browser regression PASS；
- final diff 不包含 V2 baseline migration、正式 Site content、asset relocation、Public framework change 或 deferred architecture；
- PR exact-head Current Evidence 完整；
- 合并后需要 `main` Post-Integration Verification 才能标记 COMPLETED。
