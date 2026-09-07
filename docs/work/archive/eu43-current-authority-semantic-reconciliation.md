# EU-43 — Current Authority Semantic Reconciliation

## Status

- Parent: GitHub Issue #92
- Phase: Phase 1A — Canonical Authority Audit & Reconciliation
- Candidate formed by: `slice-work`
- Readiness: **PASS**
- Execute state: **COMPLETED**
- Planning baseline: `main@f8f4083d831b5a1bfffe494b541c7015dbdd3fe0`
- Execute baseline: `main@8848d74967a638f9cda749085c16257326888d4e`
- Implementation branch: `docs/eu-43-current-authority-reconciliation`
- Implementation PR: **#97**
- Final implementation Head: `f9c8bef213b0a4b3b34d1054b7e42a71b1a4cdde`
- Integration commit: `main@459e6c6a2030badf46b9146b2158816dea07358d`
- Specification / audit: `docs/project/documentation-authority-convergence.md`

## Objective

修复 EU-41 / EU-42 与 Phase 0 完成后仍存在于 Current Authority 链中的语义漂移，使后续 Fresh Context 不再从当前文档推导出旧 Flyway Site Data responsibility、旧 `site-baseline` asset ownership、旧 Slice D 直接路线或过期 Architecture Review experiment 状态。

本 EU 只做 Current Authority semantic reconciliation，不做目录迁移、不做产品行为修改、不做代码重构。

## In Scope

仅允许按 current Repository facts 修订：

- `docs/project/pre-e1e3-convergence-plan.md`
- `docs/requirements/cms-site-package-boundary.md`
- `docs/specifications/cms-site-package-boundary.md`
- `docs/specifications/preset-site-structure.md`
- `docs/specifications/party.md`
- `docs/technical/cms-architecture.md`
- `docs/technical/cms-site-package-boundary.md`
- `docs/technical/preset-site-structure.md`
- `docs/technical/party-frontend.md`
- `docs/technical/verification-strategy.md`
- `data-migrations/README.md`
- `data-migrations/party/README.md`
- 为 Current Gate 回写所必需的 `docs/project/project-roadmap.md` 与 Issue #92 evidence/state。

## Required Reconciliation

1. Generic Backend Flyway current responsibility = Schema / site-neutral capability only；current active baseline 为 `V1__current_cms_schema.sql` + `V2__site_provisioning_schema_capabilities.sql`。
2. JilinJobs stable structure = `sites/jilinjobs/structure/**`；one-time Fresh Site defaults = `sites/jilinjobs/bootstrap/**`；stable Site assets = `sites/jilinjobs/assets/**`。
3. Public asset target 仍为 `/static/**`；`/static/uploads/**` 仍是 mutable Runtime Store。
4. Historical migration = `data-migrations/**`，保持 provenance / legacy identity / fingerprint / idempotency，不接管 Site stable structure/bootstrap/assets。
5. `preset` 继续是 Generic CMS capability + Site Package stable ownership semantics；历史 V12 / old Flyway seed 只能作为 historical implementation context，不得描述为 current initialization lifecycle。
6. NavigationItem current stable provisioning identity 使用 nullable `cms_navigation.code` capability；具体 JilinJobs codes 属 Site Package。
7. Party stable banner/favicon/other stable bytes 的 versioned owner 使用 `sites/jilinjobs/assets/**`；Runtime/Public URL 不变。
8. Verification / Review Fresh lifecycle 使用 Generic Flyway + explicit Site Package composition，而不是测试代码或旧 static baseline 重建 Site structure/assets。
9. Issue #77 remaining work 按 Issue #92：Phase 1 docs convergence → Phase 2 migration/backend application boundary → Phase 3 canonical compatibility/E1-E3 re-entry gate；旧 Slice D 不是直接下一步。
10. `pre-e1e3-convergence-plan.md` experiment evidence 必须包含 AR-04 current result，并记录跨项目 evidence 已提交 agentic-dev Issue #71；不得继续写“当前不提交反馈 Issue”。

## Non-goals

- 不折叠 `information-publishing-eu30-amendment.md`；该工作属于 Phase 1A 后继 Slice B。
- 不移动 `docs/work/**`、Specifications、Technical 或 Requirements 文件。
- 不创建 `docs/README.md` / archive IA；属于 Phase 1B。
- 不修改 Backend / Frontend / Flyway / Site Package / Canonical Dataset / workflow code。
- 不进入 Phase 2 application/module implementation。
- 不进入 Issue #60 / E1～E3。
- 不更新 `agentic-dev` baseline。

## Acceptance

- Slice scope 内没有文档继续把旧 `V2__current_preset_data.sql` 作为 current runtime/site responsibility。
- Slice scope 内没有文档继续把 `site-baseline/static/**` 作为 current stable JilinJobs Site asset source owner。
- Current preset / Party / verification semantics 与 EU-39～EU-42 accepted lifecycle 一致。
- Issue #77/Issue #92 follow-up 顺序一致。
- Architecture Review experiment status 与 AR-02 + AR-04 + agentic-dev Issue #71 current evidence 一致。
- Canonical Migration responsibility 没有被移入 Site Package/Flyway。
- Diff 中没有 code/runtime/data changes。
- Exact-head Repository CI PASS。
- Merge 后 `main` post-integration CI PASS，且 Current Authority locator 重新读取无 unresolved lifecycle drift。

## Verification

### Static / semantic

- exact changed-file list；
- compare against execute baseline；
- stale responsibility scan：`V2__current_preset_data.sql`、`site-baseline/static`、“直接 Slice D”、AR-02-only/no-feedback wording；
- current path verification：`sites/jilinjobs/structure`、`bootstrap`、`assets`、Generic active Flyway files。

允许在明确的 **historical context** 中保留旧文件名/旧 migration number，但必须显式标注为 historical / superseded，不能让其承担 Current lifecycle。

### Final implementation evidence

- Execute branch 从 exact `main@8848d74967a638f9cda749085c16257326888d4e` 创建；Execute 前 Open PR = 0，base drift = none；
- PR #97 final Head：`f9c8bef213b0a4b3b34d1054b7e42a71b1a4cdde`；changed files = 13 documentation files only（12 个 scoped Current Authority + 本 work artifact），无 code/runtime/data/Flyway/Site Package/Canonical Dataset/workflow 变更；
- PR #97 exact-head CI #808：**PASS**，包含 Backend、Public、Admin 与 empty-static-root Integrated Browser；
- 自动触发的 Canonical Migration Verification #166、EU-30 Migration Upgrade Verification #116、Review Environment #713：**PASS**；这些是支持性 evidence，不扩大 EU-43 的最低验证合同；
- unresolved review threads：**0**；
- PR #97 已合并为 `main@459e6c6a2030badf46b9146b2158816dea07358d`；
- Post-Integration CI #809：**PASS**，Backend、Public、Admin、Site Package foundation/runtime composition、EU-41 bootstrap separation、EU-42 asset projection 与 Integrated Browser 全部通过；
- Integration 后重新读取 Fresh Context locator，仅发现 AGENTS / README / Roadmap / Phase 1 planning documents / 本 work artifact 仍保留 EU-43 Ready/In Review 的状态标签；该差异属于完成态同步，不改变 EU-43 已接受语义、产品行为或 Runtime。Closure state 同步将这些入口统一为 EU-43 COMPLETED、Current Ready Execution Unit = NONE、Slice B = 下一 Planning Gate / Planning Candidate。

### Repository evidence result

- PR exact-head CI：**PASS**；
- unresolved review threads：**0**；
- merge 后 main CI：**PASS**；
- Current Authority semantic reconciliation：**PASS**；
- completion-state locator synchronization：由 EU-43 closure state update 收口，不产生新的 Execute Authority。

## Rollback

全部变更为 documentation / issue state。PR 合并前可直接放弃 feature branch；合并后如发现语义错误，使用 normal documentation follow-up 修订，不需要数据/Runtime rollback。

## Handoff after completion

Current Ready Execution Unit：**NONE**。

后继 **Slice B — Canonical Product Requirement Consolidation** 仍是 Planning Candidate，必须在新的 Current Repository state 上重新执行 `slice-work → readiness-check`；不得继承 EU-43 Execute Authority。Slice B 完成后才允许进入 Phase 1B，Phase 1 全部收口后才进入 Phase 2；Issue #60 / E1～E3 继续保持 downstream。
