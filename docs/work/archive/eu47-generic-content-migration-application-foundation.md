# EU-47 — Generic Content Migration Application Foundation

## Status

- Parent: GitHub Issue #92
- Related architecture authority: GitHub Issue #77
- Phase: **Phase 2B — Generic Content Migration Application**
- Candidate formed by: `slice-work`
- Readiness: **PASS**
- Execute state: **IMPLEMENTED**
- Verification: **PASS — implementation exact-head; final integration head governed by PR #107 Actions**
- Planning baseline: `main@0af483a6e5278ad4ac049f33148144214b40669d`
- Planning branch: `planning/phase2b-generic-content-migration-application`
- Execute baseline: `main@5b2f128ae8eccba792402836bd1f8f63e798c82e`
- Implementation branch: `feature/eu-47-generic-content-migration-foundation`
- Implementation verification head: `c0ac2e90c5b8e8b6c750dfc0b879c06acd22dded`
- Pull Request: GitHub PR #107
- Requirement: `docs/requirements/generic-content-migration-application.md`
- Specification: `docs/specifications/generic-content-migration-application.md`
- Technical Plan: `docs/technical/generic-content-migration-application.md`
- Integration: **PENDING PR #107 merge at this archive snapshot**
- Post-Integration Current Evidence: **PENDING GitHub Issue #92 / #77 record at this archive snapshot**

EU-47 identifier由 Phase 2B `slice-work`形成。它不继承 EU-46 或更早 Unit 的 Execute Authority；planning/readiness integration 后通过 Fresh Context重新核验 `main`、Issue #92 / #77、Readiness、Open PR / Actions 与 base drift，以 `main@5b2f128ae8eccba792402836bd1f8f63e798c82e` 独立取得本 Unit Execute Authority。本文归档快照记录 implementation / pre-integration verification；最终 merge 与 Post-Integration 状态以 GitHub PR #107、Actions 与 Issue #92 / #77 Current Evidence 为准，不向 Phase 2C、Phase 3 或 Issue #60 / E1～E3 传递 Execute Authority。

## 1. Dependency Closure

基于 planning baseline `main@0af483a6e5278ad4ac049f33148144214b40669d` 的 current implementation / canonical workspace / workflow inventory，依赖闭合结论为：

1. EU-46 已建立 `content-migration → cms-core` 独立 non-web application boundary；Phase 2B不再解决 Server classpath隔离。
2. Party Article implementation同时包含 site-neutral identity/fingerprint、Column resolution、path/digest、Resource、Article create/publish、legacy mapping/report，以及 Party alias/typeCode hardcode。
3. Party Carousel implementation同时包含 site-neutral list mapping、path/digest、LINK/ARTICLE stable relation/report，以及 `PARTY_CAROUSEL`、fixed 4 positions、Party static path hardcode。
4. EU-30 V2 carousel accepted fingerprints、position-2 LINK→ARTICLE原位 UPDATE与 Runtime-old-state guard属于 Party compatibility policy，留给 Phase 2C。
5. Article/ListItem legacy mapping tables与基础 Mapper语义 site-neutral，可迁到 shared migration ownership，无需 schema change。
6. `data-migrations/README.md` 已定义 site-neutral canonical organization；`article.schema.json` 已 generic，现有 `carousel.schema.json` 明确 Party-specific。
7. Core `CmsListService`、`ResourceService`、`StaticResourceService` 已提供 Generic Article、LINK/ARTICLE ListItem 与 resource projection所需能力。
8. current Party Canonical / EU-30 Upgrade workflows可以持续承担 183 Articles、4 carousel、idempotency/conflict/upgrade regression evidence。
9. current DB schema已有 Article/ListItem legacy mapping，无 Flyway prerequisite。
10. Consumer-local Method足够完成本 Unit，不需要 `agentic-dev` baseline upgrade。

没有未决 Goal / Scope / Product behavior / business rule / schema / repo-split决策需要人工介入。

## 2. Slice-work Result

Phase 2B 形成单一 Execution Unit：**EU-47 — Generic Content Migration Application Foundation**。

没有拆成 Generic schemas、loader/preflight、Article importer、List importer、CLI/workflow多个 EU，因为：

- schema/model单独合并会形成没有可执行消费者的第二套 canonical contract；
- importer没有 shared preflight/report就无法证明 known invalid/conflict不会造成部分成功；
- Generic code没有 CLI/verification无法证明真实 application composition与可消费性；
- Party regression Gate必须与 neutral mapping ownership调整在同一 integration boundary证明无漂移；
- Phase 2C需要一个已经完整存在、可独立验证的 Generic boundary作为前置。

整个 PR #107 是本 Unit rollback / integration boundary。

## 3. Execute Scope

### 3.1 Neutral mapping ownership

从 Party files搬出并保留单一 owner：

- `ArticleLegacyMappingMapper` / `ArticleLegacyMappingRecord`；
- `CmsListItemLegacyMappingMapper` / `CmsListItemLegacyMappingRecord`。

保持 table / package / SQL semantics；Party current services与 Generic capability消费同一 Mapper。EU-30 correction mapper与 accepted-upgrade helper不移动到 Generic ownership。

### 3.2 Generic canonical models / schemas

新增 site-neutral Article/List canonical Kotlin model与 generic list schemas：

- Article contract继续兼容 `data-migrations/schemas/article.schema.json`；
- 新增 generic `list-index.schema.json` / `list-item.schema.json`；
- Generic schema不含 Party list code、fixed count、legacy-key pattern、accepted fingerprint或 upgrade exception。

`data-migrations/party/v1/**` accepted bytes未修改。

### 3.3 Loader / preflight / import plan

实现：

```text
load
→ structural + byte preflight
→ Runtime target/mapping/dependency preflight
→ GenericImportPlan
→ execute
→ report
```

Preflight覆盖：

- normalized root-contained paths；
- JSON/index/item一致性；
- duplicate stable identity；
- resource file/size/SHA-256；
- unresolved canonical token/reference；
- Column/List stable target；
- current mapping CREATE/SKIP/CONFLICT；
- ARTICLE ListItem stable article dependency；
- deterministic Article → List dependency order。

Known INVALID / CONFLICT在 execute mutation开始前阻断。

### 3.4 Generic Article importer

使用 Core `ColumnQuery` / `ArticleService` / `ResourceService`与 neutral Article mapping：

- INTERNAL / EXTERNAL_LINK；
- BODY_IMAGE / ATTACHMENT；
- migration-relative reference rewrite；
- create + publish + mapping；
- CREATE/SKIP/CONFLICT；
- no Party alias/typeCode validator。

### 3.5 Generic ListItem importer

使用 Core `CmsListService` / `StaticResourceService` / `ResourceService`与 neutral mappings：

- LINK；
- ARTICLE stable article reference；
- optional canonical image；
- deterministic source order；
- CREATE/SKIP/CONFLICT；
- no in-place source-type UPDATE / accepted fingerprint exception。

LINK image target使用 site-neutral deterministic `/static/migrated/content/lists/<LIST_CODE>/<sha256>.<ext>` ownership；existing target必须验证 same bytes后复用，不盲目 replace。ARTICLE image使用 managed Resource `imageResourceId`。

### 3.6 Generic service / command / report

新增：

```text
ContentMigrationApplication command: generic-content <snapshot-root>
root Gradle task: importCanonicalContent
report label: CONTENT_MIGRATION_REPORT
```

Report包含 total/created/skipped/conflicts/invalid、per-unit result与 preflight/execute phase。conflict/invalid产生 non-success command/task semantics。

Existing `party-content`、`party-carousel`、`importPartyHistoricalContent`、`importPartyCarousel`、`EU29_IMPORT_REPORT`、`EU29_CAROUSEL_IMPORT_REPORT` 保持 current semantics。

### 3.7 Verification / workflow

新增：

- root `verifyGenericContentMigration`；
- synthetic generic Fresh DB verifier；
- Generic package Party-hardcode purity assertion；
- `.github/workflows/generic-content-migration-verification.yml`。

Existing Canonical Migration、EU-30 Upgrade、Backend Application Boundary、Site Package与 Repository CI继续作为 regression Gate。

## 4. Non-goals / Preserved Boundary

EU-47 未执行：

- 不把 Party current dataset/profile切换到 Generic path；
- 不删除 Party importer / V2 compatibility code；
- 不修改 Party aliases、accepted fingerprints、fixed carousel count或 position-2 update policy；
- 不修改 `data-migrations/party/v1/**` bytes；
- 不开始 Main Site Historical Content collection/import；
- 不修改 DB schema / Flyway semantics；
- 不改变 Admin/Public API / browser / Site Package behavior；
- 不创建新的 Backend application或 Gradle domain module；
- 不设计 public plugin SPI / script framework；
- 不进入 Phase 2C / Phase 3 / Issue #60 / E1～E3；
- 不拆 Git Repository；
- 不更新 `agentic-dev` baseline。

## 5. Acceptance Closure

### A. Generic boundary / purity — PASS

- Generic source位于 `apps/content-migration`并保持 `content-migration → cms-core` only；
- no new application/module dependency；
- Generic package purity workflow禁止 Party aliases / `PARTY_CAROUSEL` / `party-carousel:position:` / EU29/EU30 / `migrated/party/` hardcode；
- neutral legacy mapping Mapper只有一份 owner，Party/Generic均消费同一 ownership；
- no DB schema/Flyway change。

### B. Generic canonical / preflight — PASS

Fresh DB verifier覆盖 generic Article/List load、root containment/path traversal、size/digest、duplicate identity/index-item mismatch、unresolved reference、missing Column/List/dependency，以及 known INVALID/CONFLICT no-mutation。

### C. Generic execute behavior — PASS

Synthetic verification证明：

- INTERNAL Article + BODY_IMAGE/ATTACHMENT CREATE；
- EXTERNAL_LINK Article CREATE；
- LINK ListItem CREATE；
- ARTICLE ListItem通过 stable article identity CREATE；
- deterministic Article → List dependency order；
- second import全 SKIP；
- changed fingerprint CONFLICT；
- `CONTENT_MIGRATION_REPORT` machine-readable；
- conflict/invalid CLI/task non-success；
- no HTTP listener / no Server transport leakage；
- LINK image使用 neutral static target；ARTICLE ListItem遵守 Core projection并使用 managed image Resource。

### D. Party / repository regression — PASS at implementation verification head

Implementation verification head `c0ac2e90c5b8e8b6c750dfc0b879c06acd22dded` 已取得：

- Generic Content Migration Verification **#4** — PASS；
- Backend Application Boundary Verification **#7** — PASS。

同一 implementation line的前一 exact heads已证明 Canonical Migration、EU-30 Upgrade、Site Package、Repository CI / Integrated Browser均保持；closure documentation进入 PR #107 后，final integration head必须重新取得 PR diff触发的 required exact-head evidence，不能机械继承祖先 evidence。

`data-migrations/party/v1/**` changed bytes = NONE；final compare必须继续证明该边界。

### E. Integration / gate

- final diff必须符合本 Unit scope；
- merge前 re-check `main` / base drift / Open PR / unresolved review threads；
- merge后按实际 push trigger取得 Post-Integration CI / Generic / Boundary / Site Package evidence；
- Fresh Context locators同步为 EU-47 completed，下一 Gate仅 Phase 2C Planning Candidate；
- EU-47 Execute Authority不传给 Phase 2C / Phase 3 / E1～E3。

Review Environment只作为 supporting evidence，不替代 required exact-head Gate。

## 6. Rollback / side effects

Rollback boundary：整个 EU-47 implementation PR #107。

本 Unit不转换 Party canonical data，不增加 schema migration。Generic verifier只使用 Fresh DB、temp static/storage/snapshot root。Known preflight failure验证无 Runtime data mutation；unexpected execute failure的 resource/filesystem side effect局限于 ephemeral verifier scope。

## 7. Readiness Decision

**PASS — EU-47 became the Ready Execution Unit after planning/readiness integration and Fresh Context revalidation.**

Execute baseline为 `main@5b2f128ae8eccba792402836bd1f8f63e798c82e`；implementation在该 baseline无 drift前提下完成。

## 8. Execute / Verification Record

### 8.1 Implementation result

- 新增 `migration/generic/**` Generic Engine与 Generic canonical models；
- neutral legacy Mapper从 Party source relocation为 shared migration ownership；
- 新增 generic list schemas；
- 新增 Generic dispatcher / Gradle import / verifier / workflow；
- 保持 Party current command / dataset / compatibility；
- 保持 DB schema、API、frontend、Site Package behavior；
- final implementation compare到 Execute baseline显示 source changes限定于 EU-47 implementation / verification / Authority closure范围。

真实 Actions收敛过程中修复了两个验证层问题：

1. Kotlin public Spring method暴露 file-private plan type，修正为适当内部可见性；
2. focused verifier最初假设 ARTICLE ListItem保留 canonical input title，但 Core `CmsListService`会规范化为关联 Article title；按 current Core contract修正 verifier，未修改产品实现。

这些修复没有扩大 EU-47 scope。

### 8.2 Integration boundary

PR #107 是整个 EU-47 rollback / integration boundary。合并前必须确认：

- current `main` 仍与 Execute baseline无 base drift；
- PR #107 是唯一相关 Open implementation PR；
- unresolved review threads = 0；
- final PR head required Actions全部 PASS；
- final compare无 `data-migrations/party/v1/**`、Flyway、frontend或 Site Package byte change。

合并后必须在 integrated `main` 取得实际 push-trigger Post-Integration evidence，并向 Issue #92 / #77写入 Current Evidence。

### 8.3 Authority termination / next gate

EU-47 integration closure后：

- Current Ready Execution Unit：**NONE**；
- EU-47 Execute Authority：**TERMINATED**；
- 下一 Gate：**Phase 2C — Party Migration De-specialization & Compatibility Planning Candidate only**；
- Phase 2C 必须重新 dependency closure、Requirement / Specification / 必要 Technical Planning、`slice-work` 与 `readiness-check`；
- 本 archive artifact只承担 historical evidence / traceability，不授予 Phase 2C Execute；
- Phase 3 与 Issue #60 / E1～E3继续 blocked。
