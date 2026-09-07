# EU-40 — Explicit Site Package Runtime Composition Activation

## 1. Identity

- Identifier：`EU-40`
- Source：GitHub Issue #77 / Slice B remainder current audit
- Requirement：`docs/requirements/cms-site-package-boundary.md`
- Specification：`docs/specifications/cms-site-package-boundary.md`
- Technical Plan：`docs/technical/cms-site-package-boundary.md`
- Execute baseline：`main@39836f720ae15c88c7bb8af333e1fcd18f642c95`
- Implementation PR：#86
- Merge commit：`b105e553db1ebbc12a2b6665385b94fb977bea06`
- Status：**COMPLETED**

## 2. Readiness decision

Issue #77 已基于最新 main 完成 current audit / slice-work / readiness-check，结论：

- 直接退掉 `V2__current_preset_data.sql` 当前不 Ready；
- V2 仍承载默认 Runtime compatibility 与 `CmsListItem` / `Advertisement` initial operational seed；
- Existing DB Flyway lineage 也要求 V2 retirement 单独处理；
- 现有 `SitePackageProvisioner` 已具备 98-object Site Package reconcile，不需要第二套 provisioning engine；
- 当前真实缺口是正式 Runtime / importer / Review lifecycle 未显式消费 Site Package。

因此形成 EU-40，并完成 Readiness Check：**PASS**。

## 3. Intent

把 Site Package provisioning 建立为一个 opt-in、可观察的 Spring Runtime composition capability，并让 Repository-owned formal runtime / importer / Review flows 显式启用它。

长期顺序在本 EU 中只前进到：

```text
Flyway current schema / compatibility baseline
→ configured Site Package runtime reconcile
→ runtime / importer consumers
```

本 EU 不宣称 V2 responsibility 已移除。

## 4. Included changes

1. 增加 `cms.site-package.root` opt-in Spring lifecycle capability；
2. capability 显式依赖 Flyway initializer，并复用现有 `SitePackageProvisioner`；
3. 未配置 root 时 Generic CMS context 保持原行为；
4. 增加真实 MySQL targeted verifier：
   - V1+V2+V3 current baseline；
   - Legacy 40 navigation in-place stable-code adoption；
   - 98-object second-start idempotency；
   - `CmsListItem` / `Advertisement` operational count preservation；
   - 未配置 root 时 composition bean 不存在；
5. Repository CI Integrated Runtime 显式挂载 / 配置 `sites/jilinjobs`；
6. Canonical Migration Verification、EU-30 Migration Upgrade Verification、Review Environment 的 Spring Runtime / importer paths 显式提供同一 Site Package root。

## 5. Explicit non-goals

- 不修改 / 删除 / 迁空 V2；
- 不宣称 Fresh Runtime 已脱离 V2；
- 不迁移 `CmsListItem` / `Advertisement` 到 Site Package；
- 不定义 operational initial seed 的长期 Authority；
- 不移动 `site-baseline/static/**`；
- 不修改 Public/Admin product behavior 或公开 contract；
- 不进入 Slice C / Slice D / E1～E3；
- 不引入 plugin / tenant / multi-site framework；
- 不拆 Repository。

## 6. Acceptance obligations

- `cms.site-package.root` 未配置时不启用 runtime composition；
- 配置后必须在 Flyway 后执行 Site Package apply；
- 具体 JilinJobs 路径只存在于 Consumer orchestration，不硬编码进 Generic CMS Domain；
- current V1+V2+V3 首次 runtime composition：40 NavigationItem 原位 adoption、58 existing structures unchanged；
- 第二次 context：98 objects unchanged；
- V2 operational seed 保持；
- existing standalone `provisionSitePackage` CLI 保持可用；
- Backend tests / targeted MySQL verification PASS；
- Canonical Migration Verification PASS；
- EU-30 Migration Upgrade Verification PASS；
- Repository CI Public/Admin/Integrated Browser PASS；
- Review Environment reproducibility PASS；
- final diff / Authority audit PASS；
- PR Integration + main Post-Integration Verification PASS。

## 7. Exact-head implementation evidence

Final implementation Head：`b3e3dc8c4855c9e17b2dfa2f190a84d0305162e2`。

- Site Package Verification #19 / run `34040583733`：**PASS**；
- CI #768 / run `34040583771`：Backend / EU-40 Runtime composition / Public / Admin / Integrated Public Browser / Integrated Admin Browser **PASS**；
- Canonical Migration Verification #153 / run `34040583631`：**PASS**；
- EU-30 Migration Upgrade Verification #103 / run `34040583802`：**PASS**；
- 人工评审环境 #683 / run `34040583621`：**PASS**；
- final diff / Authority audit：**PASS**。

专项验证证明 current V1+V2+V3 首次 composition 原位 adoption 40 条 Legacy navigation、58 existing structures unchanged；第二次 context 98 objects unchanged；V2 `CmsListItem` / `Advertisement` operational seed 保持；未配置 root 时 composition 不启用。

## 8. Integration and Post-Integration evidence

PR #86 已从锁定 final Head 合并，merge commit：

`b105e553db1ebbc12a2b6665385b94fb977bea06`

`main@b105e553db1ebbc12a2b6665385b94fb977bea06` Post-Integration：

- Site Package Verification #20 / run `34041563058`：**PASS**；
- CI #769 / run `34041563060`：Backend / EU-40 Runtime composition / Public / Admin / Site Package-enabled Web Runtime / Integrated Public Browser / Integrated Admin Browser **PASS**。

因此 EU-40 的实现、Integration 与 `main` Current Evidence 已闭环，状态正式晋升为 **COMPLETED**。

## 9. Remaining boundary after EU-40

即使 EU-40 完成，Issue #77 仍保持 OPEN。后续至少包括：

1. Operational Seed Classification & V2 Responsibility Retirement；
2. Slice C — Site Asset Ownership & Runtime Composition；
3. Slice D — Canonical Migration Compatibility & E1～E3 Re-entry；
4. 四层边界完成后的 Repository Split Readiness Assessment。

这些后续候选不继承 EU-40 Execute 授权。
