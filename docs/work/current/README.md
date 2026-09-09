# Current Work

Current Ready Execution Unit：**EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review Closure**。

E3 — Main Historical Content Collection & Canonical Migration 当前状态：

- E3 Requirement：**READY / ACTIVE**；
- E3 Specification：**READY / ACTIVE**；
- Technical Plan：`../../technical/main-historical-content-migration.md` — **READY / ACTIVE**；
- EU-50 — Main Source Discovery & Accepted Snapshot Promotion：**COMPLETED / Execute Authority TERMINATED**；completed evidence 位于 `../archive/eu50-main-source-discovery-promotion.md`；
- EU-50 accepted current Article subset 已集成到 `data-migrations/main/v1/**`；
- EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review：**READY / Readiness PASS**；Current Work：`eu51-main-canonical-import-runtime-review.md`；
- EU-51 Execute baseline：**PENDING**，只能在本 Planning/Readiness 状态集成后由新的 Fresh Context 建立；
- Main Page / stable ListItem source handoff 属于独立 Site Package Planning/Readiness path；
- 230 篇 deferred problem Articles 与 6 篇 source-defect Articles 保持后置单独处理，不属于 EU-51 import input，也不阻断当前项目进程。

EU-51 Readiness baseline：

`main@fd192460cb481645c1f1af435cbe5451145797d9`

Readiness 已确认：

- accepted current dataset = 3078 Articles / 2603 resources；
- dataset digest = `sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`；
- ten Main target Column aliases全部存在于 JilinJobs Site Package 且 enabled；
- Generic Content Migration、`provisionSitePackage`、`importCanonicalContent`、CI / Review Environment / browser verification 能力均已存在；
- Fresh Runtime chain必须显式保持 `Flyway → Site Package provisioning → Main generic import`；
- no Legacy Source access、no Page/List migration、no deferred/source-defect Article import。

下一自然 Gate：先集成本 Planning/Readiness 状态；随后从 integrated `main` Fresh Context 重新核验 Issue #60 / #77、Open PR / Actions、E3 Authority、EU-51 Work、canonical manifest 与 base drift，并建立 EU-51 独立 Execute baseline。不得在 Planning/Readiness branch直接开始 Runtime import，也不得继承 EU-50、EU-49、EU-48、Phase 3 或 E1 Execute Authority。

E2 / EU-49 completed evidence继续位于：

`../archive/eu49-page-content-migration-foundation.md`

Issue #77 继续作为四层长期 Architecture Authority；Main Page / stable ListItem follow-up、C1 / C2、Issue #57 / #59 与 Repository Split Readiness Assessment 保持独立候选。
