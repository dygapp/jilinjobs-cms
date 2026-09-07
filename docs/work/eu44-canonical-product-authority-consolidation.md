# EU-44 — Canonical Product Authority Consolidation

## Status

- Parent: GitHub Issue #92
- Phase: Phase 1A — Canonical Authority Audit & Reconciliation
- Candidate formed by: `slice-work`
- Readiness: **PASS**
- Execute state: **IMPLEMENTED / IN REVIEW**
- Planning baseline: `main@ec53716d1fcf26a2b6c1752c00fb4d81b62d66df`
- Readiness integration: `main@c7f1f395e83f9793630c834581aa6a821a6c2b98`
- Execute baseline: `main@c7f1f395e83f9793630c834581aa6a821a6c2b98`
- Execute branch: `docs/eu-44-canonical-product-authority-consolidation`
- Phase 1 authority: `docs/project/documentation-authority-convergence.md`

## Objective

把已经确认并完成实现的 EU-30 产品变更，以及 EU-41 / EU-42 已接受的 Site / Schema ownership，折回当前 canonical Product / CMS / Backend Authority，使 Fresh Context 不再需要同时拼接 V4.8 主需求、EU-30 amendment、旧 CMS Core 规格和旧阶段型 Backend 技术计划才能得到当前事实。

本 EU 只做 documentation authority consolidation，不新增产品行为、不修改实现、不执行目录迁移。

## In Scope

Execute target 仅包括以下 4 份 Authority：

1. `docs/requirements/information-publishing.md`
2. `docs/requirements/information-publishing-eu30-amendment.md`
3. `docs/specifications/cms-core.md`
4. `docs/technical/backend-service.md`

以及 EU-44 自身 work artifact 与在 integration evidence 完成后进行 Current Gate / Fresh Context closure 所必需的：

- `AGENTS.md`
- `README.md`
- `docs/project/project-roadmap.md`
- `docs/project/documentation-authority-convergence.md`
- Issue #92 Current Evidence / Gate。

## Required Consolidation

### 1. Canonical Requirement

`docs/requirements/information-publishing.md` 必须成为 EU-30 后的单一 current Requirement：

- 将 confirmed EU-30 Amendment 的有效语义折回主需求；
- current consolidated version = `V4.9`；
- `HOME_CAROUSEL_INTERVAL_SECONDS` 不承担 Current Runtime responsibility，只可在明确 superseded / historical context 中出现；
- Main / Party 当前统一使用 `CAROUSEL_INTERVAL_SECONDS`（默认 4 秒、正整数）与 `CAROUSEL_MAX_ITEMS`（默认 5、正整数）；
- `CmsListItem` 当前来源为 `LINK / ARTICLE`，ARTICLE placement 不改变 Article 唯一栏目归属；
- ARTICLE placement 的公开有效性、canonical route、`openMode`、图片继承 / override 与 `imagePolicy` 规则与 EU-30 accepted contract 一致；
- `Article.articleType`、`CmsListItem.sourceType`、ARTICLE `articleId` 是创建后不可普通编辑的来源身份；
- Article 不存在全局 `recommended` 运营语义；独立推荐 / 展示投放使用 `CmsList + ARTICLE`；
- `party-theme-education / 主题教育` 与 EU-30 accepted Party historical extension 语义进入 canonical Requirement；
- EU-29 acceptedSnapshot 与 EU-30 current canonical Runtime / upgrade compatibility 的追溯语义保持，不把历史迁移数据并入 Flyway / Site Package。

### 2. Current Site / Resource Ownership

主需求、CMS Core 与 Backend Technical Authority 必须对齐 EU-41 / EU-42：

- Generic Backend Flyway 当前只承担 schema / site-neutral capability；active baseline = `V1__current_cms_schema.sql` + `V2__site_provisioning_schema_capabilities.sql`；
- JilinJobs stable structure owner = `sites/jilinjobs/structure/**`；
- one-time Fresh Site operational defaults owner = `sites/jilinjobs/bootstrap/**`；
- stable Site asset source owner = `sites/jilinjobs/assets/**`；
- stable assets 仍投影到公开 `/static/**`；
- CMS Runtime 上传继续进入 `/static/uploads/**`；
- Historical Content Migration 继续由 `data-migrations/**` 承担，不并入 Backend Flyway 或 Site Package stable assets。

### 3. EU-30 Amendment Lifecycle

`docs/requirements/information-publishing-eu30-amendment.md`：

- 保留原 accepted Requirement Change 与 Human Review 追溯；
- 在主需求完成折叠后改为 **SUPERSEDED / TRACEABILITY**；
- 明确其有效语义由 consolidated `information-publishing.md` V4.9 承担；
- 本 EU 不移动 / 删除该文件，物理 archive 归 Phase 1B。

### 4. CMS Core Specification

`docs/specifications/cms-core.md` 必须：

- Authority 指向 consolidated `information-publishing.md` V4.9；
- 把 `CmsListItem` 从旧 title/url/imagePath-only 模型收敛为 current `LINK / ARTICLE` placement model；
- 表达 current ARTICLE image override / effective image、published filtering、canonical route 与 source identity immutable contract；
- 表达 current carousel SiteProperty keys，而不是 Main-only legacy key；
- 表达 Article 无全局 `recommended`、独立投放使用 CmsList ARTICLE；
- 不再把 `site-baseline/static/**` 作为 current stable Site asset owner；
- 保留 `/static/uploads/**` 的 Runtime upload 责任与 StaticResource 通用能力。

### 5. Backend Technical Authority

`docs/technical/backend-service.md` 必须从旧阶段型 V11 施工计划收敛为 Current Backend contract：

- 当前 active Flyway 与 Generic schema responsibility 对齐 EU-41；
- 旧 V11 / historical migration number 只能作为历史实现上下文，不再定义 Current initialization lifecycle；
- CmsList Service / API current contract 覆盖 LINK / ARTICLE、source identity immutable、有效图片、Published filtering 与 article relation；
- SiteProperty current contract 使用 `CAROUSEL_INTERVAL_SECONDS / CAROUSEL_MAX_ITEMS`；
- Site Package structure / bootstrap / assets 与 Backend Generic capability 的责任边界清晰；
- StaticResource / Runtime upload / protected path 表达与 EU-42 current ownership 一致；
- 不把 current Site instance data seed 回写进 Backend migration lineage。

## Non-goals

- 不新增、删除或改变任何用户可见产品行为；
- 不修改 Backend / Frontend / Flyway SQL / Site Package / Canonical Dataset / Workflow；
- 不重新执行 EU-30 产品决策或 Human Review；
- 不改变 Party current canonical Runtime Dataset、EU-29 acceptedSnapshot 或 EU-29→EU-30 upgrade compatibility；
- 不移动 / 删除 amendment、Requirements、Specifications、Technical 或 `docs/work/**` 文件；
- 不创建 `docs/README.md`、archive / history 目录或 subtree README；这些属于 Phase 1B；
- 不进入 Phase 2 Historical Migration / Backend Application Boundary；
- 不进入 Issue #60 / E1～E3；
- 不更新 `agentic-dev` baseline。

## Acceptance

1. `information-publishing.md` 明确为 consolidated current Requirement V4.9，并完整吸收 EU-30 confirmed amendment 的 current product semantics。
2. 四个 scoped Authority 不把 `HOME_CAROUSEL_INTERVAL_SECONDS` 当 Current Runtime property；如保留该字符串，仅用于明确 superseded / historical 说明。
3. Current carousel contract 统一为 `CAROUSEL_INTERVAL_SECONDS` + `CAROUSEL_MAX_ITEMS`。
4. Current CmsListItem contract 明确 `LINK / ARTICLE`、ARTICLE published filtering、image policy / override、canonical route / openMode 和 immutable source identity。
5. Current Article contract 不定义全局 `recommended`；独立展示投放使用 `CmsList + ARTICLE`。
6. Current stable Site structure / bootstrap / asset source owner 分别为 `sites/jilinjobs/{structure,bootstrap,assets}/**`，Runtime upload 仍为 `/static/uploads/**`。
7. Backend Current Flyway responsibility 只表达 Generic schema / site-neutral capability，active baseline 为 V1 + V2；旧 V11 等只可作为明确 historical context。
8. `information-publishing-eu30-amendment.md` 降为 superseded / traceability，且明确 Current authority 已折回主需求；文件不移动。
9. Historical Migration 继续属于 `data-migrations/**`，不被并入 Flyway / Site stable asset ownership。
10. 最终 diff 不包含 code/runtime/data changes，也不进入 Phase 1B / Phase 2 / E1～E3。
11. Exact-head Repository CI PASS，unresolved review threads = 0；合并后 `main` Post-Integration CI PASS，并重新读取 Current Authority locator 无 lifecycle drift。

## Verification

### Static / semantic

- exact changed-file list / compare against execute baseline；
- stale current-authority scan：`HOME_CAROUSEL_INTERVAL_SECONDS`、`site-baseline/static`、V11 current seed wording、old LINK-only CmsList model；
- current contract scan：`CAROUSEL_INTERVAL_SECONDS`、`CAROUSEL_MAX_ITEMS`、`LINK / ARTICLE`、immutable source identity、`sites/jilinjobs/{structure,bootstrap,assets}`、`/static/uploads/**`、Generic active Flyway V1/V2；
- amendment → canonical requirement traceability review；
- no product/code/runtime/data diff review。

允许旧 property / migration number / path 在明确的 historical / superseded context 中保留，但不得让其承担 Current responsibility。

### Repository evidence

- exact-head CI；
- PR diff / review threads；
- merge 后 Post-Integration CI；
- merge 后 Fresh Context locator re-read。

本 Unit 为 documentation-only canonicalization，不要求新的 Human Runtime Review、Visual Review 或 Architecture Model Eval；Repository 自动触发的现有 workflows 可作为支持性 evidence，但不改变最低验证合同。

## Readiness Check

### Inputs

- Parent Planning Authority：Issue #92 / `docs/project/documentation-authority-convergence.md`；
- confirmed Requirement Change：`docs/requirements/information-publishing-eu30-amendment.md`；
- accepted EU-30 technical / public contract：`docs/technical/carousel-list-placement.md`、`docs/specifications/public-site.md`；
- accepted Site / Schema boundary：`docs/requirements/cms-site-package-boundary.md` + EU-41 / EU-42 current Repository facts；
- exact target docs 与 non-goals 如上。

### Decision

**PASS — Execute Authority remained valid at `main@c7f1f395e83f9793630c834581aa6a821a6c2b98`.**

Fresh Context revalidation confirmed no new commit after PR #99 integration, no Open PR, post-readiness CI PASS, and no changed Product / Architecture decision. EU-44 therefore entered Execute from this exact main baseline.

## Execution Result — Implementation Stage

The execution branch has completed the authorized canonicalization only:

- `information-publishing.md` is now V4.9 and carries current EU-30 + EU-41/EU-42 product/ownership semantics;
- EU-30 amendment is `superseded` and explicitly traceability-only;
- `cms-core.md` consumes V4.9 and expresses the current CMS product contract;
- `backend-service.md` now describes the Current Backend contract rather than V11 as the current lifecycle;
- no Backend / Frontend / Flyway SQL / Site Package / Canonical Dataset / Workflow change is part of the implementation scope.

EU-44 remains **IMPLEMENTED / IN REVIEW** until exact-head Repository CI, PR review-thread check, integration and post-integration CI are complete. Only then may the separate closure-state change mark EU-44 `COMPLETED` and return Current Ready Execution Unit to `NONE`; that closure does not grant Phase 1B Execute Authority.
