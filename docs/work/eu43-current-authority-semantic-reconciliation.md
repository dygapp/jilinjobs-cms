# EU-43 — Current Authority Semantic Reconciliation

## Status

- Parent: GitHub Issue #92
- Phase: Phase 1A — Canonical Authority Audit & Reconciliation
- Candidate formed by: `slice-work`
- Readiness: **PASS**
- Execute state: **READY / NOT STARTED**
- Planning baseline: `main@f8f4083d831b5a1bfffe494b541c7015dbdd3fe0`
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
- Merge 后 `main` post-integration CI PASS，且 Current Authority locator 重新读取无 drift。

## Verification

### Static / semantic

- exact changed-file list；
- compare against planning base；
- stale responsibility scan：`V2__current_preset_data.sql`、`site-baseline/static`、“直接 Slice D”、AR-02-only/no-feedback wording；
- current path verification：`sites/jilinjobs/structure`、`bootstrap`、`assets`、Generic active Flyway files。

允许在明确的 **historical context** 中保留旧文件名/旧 migration number，但必须显式标注为 historical / superseded，不能让其承担 Current lifecycle。

### Repository evidence

- PR exact-head CI；
- unresolved review threads = 0；
- merge 后 main CI；
- Issue #92 / Roadmap current state 回写。

## Rollback

全部变更为 documentation / issue state。PR 合并前可直接放弃 feature branch；合并后如发现语义错误，使用 normal documentation follow-up 修订，不需要数据/Runtime rollback。

## Handoff after completion

EU-43 完成后 Current Ready Execution Unit 回到 **NONE**。后继 Slice B 必须重新执行 `slice-work → readiness-check`；不得继承 EU-43 Execute Authority。
