# EU-37 — Site Package Contract & Provisioner Foundation

## 1. Identity

- Identifier：`EU-37`
- Source Candidate：GitHub Issue #77 / Slice A
- Requirement：`docs/requirements/cms-site-package-boundary.md`
- Specification：`docs/specifications/cms-site-package-boundary.md`
- Technical Plan：`docs/technical/cms-site-package-boundary.md`
- Execute baseline：`main@01073b131fef5c2e978060c55a4f50bcbc8b2fc4`
- Planning integration：PR #78
- Planning Post-Integration CI：#731 / run `34015839251`，Backend / Public / Admin / Integrated Browser 全部 PASS
- Implementation PR：#80
- Status：**READY TO INTEGRATE — final exact-head verification required after branch normalization**

## 2. Intent

用一个最小、可独立验证的 Foundation 证明 Site Package 可以脱离 Site-specific Flyway baseline，以版本化数据契约和受控 Provisioner 恢复稳定站点结构，为后续 Slice B 的 current JilinJobs Site baseline migration 提供机制基础。

## 3. Scope

EU-37 只建立 Foundation，不迁移当前正式 Site baseline：

1. Site Package v1 manifest / structure contract；
2. 机器可审计的 manifest / Column JSON schema；
3. 首轮只支持 `Column` 代表性结构，以 stable `alias` 与 `parentAlias` 验证 logical identity / dependency resolution；
4. package path、SHA-256 digest、字段、重复 alias、父引用和 cycle preflight；
5. narrow Site Provisioner：
   - package-owned Column 创建时 `preset=true`；
   - existing `preset=true` 同 alias 对象按 package state reconcile；
   - existing `preset=false` 同 alias 对象视为 ownership conflict，不接管；
   - package 外 Runtime 数据不 broad-delete / broad-reset；
6. CLI / Gradle entry；
7. 真实 MySQL Foundation Verification：只迁移 Flyway V1 Generic Schema，执行 first apply / second apply，并验证 parent relation、preset、幂等性及 package 外普通 Runtime Column 不受影响；
8. Backend full build/test 与 Repository CI regression。

## 4. Explicit non-goals

- 不修改、删除或迁空 `V2__current_preset_data.sql`；
- 不迁移 JilinJobs 正式栏目、导航、Page、List、SiteConfig、Advertisement baseline；
- 不移动 `site-baseline/static/**`；
- 不修改 Public Renderer、Admin 产品行为、Gateway 或 canonical routes；
- 不执行 Party canonical migration compatibility 收敛；
- 不进入 Issue #60 / E1～E3；
- 不拆 Repository，不引入 Git Submodule、plugin framework 或 multi-site runtime framework。

## 5. Readiness Check

### Authority / Intent — PASS

Issue #77 Requirement、Ready Specification 和 Ready Technical Plan 已经由 PR #78 集成到 `main`；Slice A 的目标与 deferred scope 清晰。

### Slice Integrity — PASS

Package contract、preflight、narrow persistence 与 Fresh-schema proof 共同构成一个可独立验收的纵向 Foundation。只建立数据格式不能证明可 provision；只写数据库而没有 package identity / integrity 也不能形成长期 Site Authority，因此保持单一 Unit。

### Current implementation audit — PASS

- V1 已有 `cms_column` generic schema、stable `alias`、parent relation 与 `preset` 字段；
- 当前 Column Admin Service 已保护 preset stable alias / delete boundary；
- EU-37 可使用专用窄 Provisioning 写入，不需要修改普通 Admin create/update contract；
- 无需新增数据库字段、产品 API 或第三方框架。

### Verification feasibility — PASS

可在现有 GitHub Actions MySQL 8.4 环境中，以 `spring.flyway.target=1` 只建立 Generic Schema，再执行 Foundation verifier；现有 CI 可继续承担 full regression。

### Baseline evidence — PASS

PR #78 merge：`01073b131fef5c2e978060c55a4f50bcbc8b2fc4`；Post-Integration CI #731 / run `34015839251` 全部 PASS。

### Scope / rollback — PASS

EU-37 不移除任何 accepted V2 responsibility，不改变正式 Runtime 数据准备顺序；失败可直接放弃 feature branch 回滚。

### Readiness Result — PASS

`EU-37` 已晋升为 **Ready Execution Unit** 并允许从上述 baseline Execute。

## 6. Stop / Return-to-planning conditions

若实现发现必须提前：

- 迁空 / 重写 V2；
- 新增 NavigationItem stable identity 字段；
- 搬迁全部 Site binary assets；
- 修改 Public Framework / Gateway；
- 引入 generic plugin / tenant / multi-site framework；

则停止扩大 EU-37，返回 Issue #77 Planning。

## 7. Completion Gate

- Site Package v1 contract 与 schema 可审计；
- package manifest / structure path 与 digest preflight 生效；
- duplicate / invalid parent / cycle / ownership conflict 被拒绝；
- Fresh V1 schema 可以 provision parent + child representative Columns；
- package-owned对象为 `preset=true`；
- second apply 不产生重复对象且报告为 unchanged；
- package 外 `preset=false` Runtime Column 保持存在且不被修改；
- CLI / Gradle entry 可运行；
- `V2__current_preset_data.sql` 与正式 Site baseline 不变；
- Backend tests/build PASS；
- PR exact-head CI 的 Backend / Public / Admin / Integrated Browser PASS；
- final diff 不包含 Slice B/C/D 或无关重构；
- 合并后 `main` Post-Integration CI PASS。

## 8. Execute / Convergence Evidence

在进入最终 branch normalization 前，PR #80 Head `07fce04d133692644e5d5aef3981d99e13369fd2` 已取得 CI #738 / run `34017844631` Current Evidence：

- Backend `clean test bootJar`：PASS；
- EU-37 `verifySitePackageFoundation`：PASS；
- verifier 在 MySQL 8.4 Fresh Database 上使用 `spring.flyway.target=1`，日志证明只应用 `V1__current_cms_schema.sql`，随后输出 `EU37_SITE_PACKAGE_FOUNDATION_VERIFY PASS`；
- 仓库真实 `sites/jilinjobs/manifest.json` / empty `structure/columns.json` 被 loader 校验，证明 EU-37 package shell 自身 path / digest contract 有效且没有提前搬入正式 V2 Column baseline；
- representative package first apply 创建 parent + child preset Columns，second apply 报告 unchanged；package definition update 可 reconcile existing preset；
- operator-created 同 alias ownership conflict、cycle、duplicate alias、missing parent、digest mismatch、path traversal 均被拒绝；
- package 外 `preset=false` Runtime Column 未被删除、改写或接管；
- Public build：PASS；Admin build：PASS；Integrated Public Browser：PASS；Integrated Admin Browser：PASS。

首次 CI #735 暴露的是 verifier raw-string fixture 把 `\n` 写成 JSON 尾部字面字符；Flyway V1-only schema 已在该次运行成功建立。修正仅作用于 test fixture，随后 CI #736、#738 均证明 Foundation 行为与全链回归通过。

## 9. Final Diff Boundary

最终实施只允许包含：

- Site Package v1 schema / `sites/jilinjobs` empty foundation shell；
- narrow Site Package loader / Column provisioner；
- CLI / Gradle entry；
- Foundation verification 与 CI MySQL service；
- 本 Work Authority。

不得包含 V2 baseline rewrite、正式 JilinJobs Site data migration、Site asset relocation、Public/Admin product behavior change、Party canonical migration、E1～E3 或 Repository split。

Branch normalization 后 Head SHA 会改变，因此 CI #738 不替代最终 exact-head verification。只有规范化后的 PR #80 exact Head 再次取得 Backend / Foundation / Public / Admin / Integrated Browser PASS，才满足 Ready to Integrate 的最终 Current Evidence；合并后仍需 `main` Post-Integration CI PASS 才能声明 EU-37 COMPLETED。
