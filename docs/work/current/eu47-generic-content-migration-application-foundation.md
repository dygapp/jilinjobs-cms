# EU-47 — Generic Content Migration Application Foundation

## Status

- Parent: GitHub Issue #92
- Related architecture authority: GitHub Issue #77
- Phase: **Phase 2B — Generic Content Migration Application**
- Candidate formed by: `slice-work`
- Readiness: **PASS**
- Execute state: **NOT STARTED**
- Planning baseline: `main@0af483a6e5278ad4ac049f33148144214b40669d`
- Planning branch: `planning/phase2b-generic-content-migration-application`
- Execute baseline: **TBD after planning/readiness integration + Fresh Context revalidation**
- Requirement: `docs/requirements/generic-content-migration-application.md`
- Specification: `docs/specifications/generic-content-migration-application.md`
- Technical Plan: `docs/technical/generic-content-migration-application.md`
- Implementation branch: **NOT CREATED**
- Pull Request: **NOT CREATED**

EU-47 identifier由 Phase 2B `slice-work`形成。它不继承 EU-46 或更早 Unit 的 Execute Authority；只有本 planning/readiness change 集成到 `main` 后，在新的 Fresh Context重新核验 current `main`、Open PR / Actions、Issue #92 / #77、Readiness与 base drift，才可能取得 EU-47自身 Execute Authority。

## 1. Dependency Closure

基于 `main@0af483a6e5278ad4ac049f33148144214b40669d` 的 current implementation / canonical workspace / workflow inventory，依赖已闭合：

1. EU-46 已建立 `content-migration → cms-core` 独立 non-web application boundary；Phase 2B不再需要解决 Server classpath隔离。
2. `ContentMigrationApplication` 当前只暴露 Party article/carousel dispatcher，Generic command尚不存在。
3. Party Article implementation同时包含 site-neutral identity/fingerprint、Column resolution、path/digest、Resource、Article create/publish、legacy mapping/report，以及 Party alias/typeCode hardcode。
4. Party Carousel implementation同时包含 site-neutral list mapping、path/digest、LINK/ARTICLE stable relation/report，以及 `PARTY_CAROUSEL`、fixed 4 positions、Party static path hardcode。
5. EU-30 V2 carousel的 accepted fingerprints、position-2 LINK→ARTICLE原位 UPDATE与 Runtime-old-state guard是明确 Party compatibility policy，必须留到 Phase 2C。
6. Article/ListItem legacy mapping tables与基础 Mapper语义是 site-neutral；当前 neutral Mapper declarations仅因历史原因定义在 Party source，可在本 Unit迁到 shared migration ownership，不需要 schema change。
7. `data-migrations/README.md` 已是 site-neutral canonical organization authority；`article.schema.json` 已 generic；`carousel.schema.json` 是 Party-specific，因此本 Unit只新增 generic list schemas，不替换 Party schema。
8. Core `CmsListService`证明 LINK image必须使用 `/static/** imagePath`，ARTICLE image可以使用 managed `imageResourceId`；Generic ListItem path必须尊重该真实 Core contract。
9. Core `StaticResourceService` 已提供 path normalization/content validation/protected-path能力，Generic LINK image不需要新 storage abstraction。
10. current Party Canonical / EU-30 Upgrade workflows提供现有 183 Articles、4 carousel、idempotency/conflict/upgrade regression evidence；本 Unit应保留并复用这些 Gate。
11. current DB schema已经包含 Article/ListItem legacy mapping；没有新的 Flyway prerequisite。
12. Consumer-local Method与当前 Repository Authority足够完成本 Unit；不需要 `agentic-dev` baseline upgrade。

没有未决 Goal / Scope / Product behavior / business rule / schema / repo-split决策需要人工介入。

## 2. Slice-work Result

Phase 2B 形成单一 Candidate Execution Unit：**EU-47 — Generic Content Migration Application Foundation**。

不拆成 Generic schemas、loader/preflight、Article importer、List importer、CLI/workflow多个 EU，原因：

- schema/model单独合并会形成没有可执行消费者的第二套 canonical contract；
- importer没有 shared preflight/report就无法证明 known invalid/conflict不会造成部分成功；
- generic code没有 CLI/verification无法证明真实 application composition与可消费性；
- Party regression Gate必须与 neutral mapping ownership调整在同一 exact head证明无漂移；
- Phase 2C需要一个已经完整存在、可独立验证的 Generic boundary作为前置，而不是一组半完成 helper。

本 Unit可以整体回滚，且不会迁移当前 Party dataset到新 path。

## 3. Execute Scope

### 3.1 Neutral mapping ownership

从 Party files中搬出并保留单一 owner：

- `ArticleLegacyMappingMapper` / `ArticleLegacyMappingRecord`；
- `CmsListItemLegacyMappingMapper` / `CmsListItemLegacyMappingRecord`。

保持 table / package / SQL semantics；Party current services继续消费同一 Mapper。EU-30 correction mapper与 accepted-upgrade helper不移动到 Generic ownership。

### 3.2 Generic canonical models / schemas

新增 site-neutral Article/List canonical Kotlin model与 generic list schemas：

- Article contract继续兼容 `data-migrations/schemas/article.schema.json`；
- 新增 generic `list-index.schema.json` / `list-item.schema.json`；
- Generic schema不含 Party list code、fixed count、legacy-key pattern、fingerprint或 upgrade exception。

不得修改 `data-migrations/party/v1/**` accepted bytes。

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

Required preflight：

- normalized root-contained paths；
- JSON/index/item一致性；
- duplicate stable identity；
- resource file/size/SHA-256；
- unresolved canonical token/reference；
- Column/List stable target；
- current mapping CREATE/SKIP/CONFLICT；
- ARTICLE ListItem stable article dependency；
- deterministic dependency order。

Known INVALID / CONFLICT阻止 Generic execute开始。

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
- no in-place source-type UPDATE / fingerprint exception。

LINK image target使用 site-neutral deterministic `/static/migrated/content/lists/<LIST_CODE>/<sha256>.<ext>` ownership；existing target必须验证 same bytes后复用，不盲目 replace。

ARTICLE image继续使用 managed Resource `imageResourceId`。

### 3.6 Generic service / command / report

新增：

```text
ContentMigrationApplication command: generic-content <snapshot-root>
root Gradle task: importCanonicalContent
report label: CONTENT_MIGRATION_REPORT
```

Report至少有 total/created/skipped/conflicts/invalid + per-unit result + preflight/execute phase。conflict/invalid必须让 command/task失败。

Existing：

- `party-content`；
- `party-carousel`；
- `importPartyHistoricalContent`；
- `importPartyCarousel`；
- `EU29_IMPORT_REPORT`；
- `EU29_CAROUSEL_IMPORT_REPORT`；

保持 current semantics。

### 3.7 Verification / workflow

新增：

- root `verifyGenericContentMigration`；
- synthetic generic Fresh DB verifier；
- Generic package Party-hardcode purity assertion；
- `.github/workflows/generic-content-migration-verification.yml`。

必要时最小同步 existing workflow path filters，禁止无真实观察缺口的机械扩张。

## 4. Non-goals

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

如果实现证明必须突破上述边界，EU-47 Readiness立即失效并返回 Planning。

## 5. Acceptance

### A. Generic boundary / purity

1. Generic source位于 `apps/content-migration`并保持 `content-migration → cms-core` only；
2. no new application/module dependency；
3. Generic package不包含 Party aliases / `PARTY_CAROUSEL` / `party-carousel:position:` / EU29/EU30 / accepted fingerprint / `migrated/party/` hardcode；
4. neutral legacy mapping Mapper只有一份 owner，Party/Generic均可消费；
5. no DB schema/Flyway change。

### B. Generic canonical / preflight

1. generic Article schema/path可加载；
2. generic List index/item schema可加载；
3. root containment / path traversal check成立；
4. size/digest validation成立；
5. duplicate identity / index-item mismatch成立；
6. unresolved body reference成立；
7. missing Column/List/dependency成立；
8. any known INVALID/CONFLICT阻止 execute mutation。

### C. Generic execute behavior

1. synthetic INTERNAL Article + resources CREATE；
2. synthetic EXTERNAL_LINK Article CREATE；
3. LINK ListItem CREATE；
4. ARTICLE ListItem通过 stable article identity CREATE；
5. deterministic Article → List dependency order；
6. second import全 SKIP；
7. changed fingerprint CONFLICT；
8. Generic report machine-readable；
9. conflict/invalid CLI/task non-success；
10. no HTTP listener / no Server transport leakage。

### D. Party / repository regression

1. `data-migrations/party/v1/**` changed bytes = NONE；
2. Canonical Migration Verification exact-head PASS；
3. EU-30 Migration Upgrade Verification exact-head PASS；
4. current 183 Articles + 4 accepted carousel保持；
5. Party second import/conflict/report labels保持；
6. Backend Application Boundary Verification PASS；
7. Site Package Verification PASS；
8. Repository CI + Integrated Browser PASS；
9. Generic Content Migration Verification PASS；
10. unresolved PR review threads = 0。

### E. Integration / gate

1. final diff符合本 Unit scope；
2. merge前 re-check `main` / base drift / Open PR；
3. merge后按实际 push trigger取得 Post-Integration CI / Generic / Boundary / Site Package evidence；
4. Fresh Context locators更新为 EU-47 completed后下一 Gate仅 Phase 2C Planning Candidate；
5. EU-47 Execute Authority不得传给 Phase 2C / Phase 3 / E1～E3。

Review Environment可作为 supporting evidence，不替代 required exact-head Gate。

## 6. Rollback / side effects

Rollback boundary：整个 EU-47 implementation PR。

本 Unit不转换 Party canonical data，不增加 schema migration。Generic verifier只使用 Fresh DB、temp static/storage/snapshot root。Known preflight failure必须验证无 Runtime data mutation；unexpected execute failure的 resource/filesystem side effect必须局限于 ephemeral verifier scope并可观察。

## 7. Readiness Check

### Goal / Scope

**PASS** — 目标限定为建立 site-neutral Canonical Dataset → Runtime capability；Party adoption / compatibility、Main migration、产品行为均明确排除。

### Requirement / Specification

**PASS** — Requirement READY；Specification READY；canonical shape、preflight、Article/List、mapping、report、Generic/Party boundary均可测试。

### Technical Planning

**PASS** — current code inventory支持在 existing Content Migration app内最小扩展；source ownership、mapping relocation、schema、preflight/execute plan、image projection、CLI/task、workflow已冻结；无需新 app/module/plugin framework。

### Dependencies

**PASS** — EU-46 prerequisite complete；current Party files、Core List/Resource/StaticResource contracts、mapping tables、canonical schemas/data organization与 regression workflows均已盘点，无未知 prerequisite。

### Verification

**PASS** — synthetic Generic verifier + purity proof + existing Party Canonical/Upgrade + Boundary + Site Package + CI覆盖主要 generic correctness与 regression failure modes。

### Rollback / side effects

**PASS** — source/schema/task/workflow change可整 PR rollback；无 current data/schema migration；verification state为 ephemeral。

### Human escalation

**PASS / NOT REQUIRED** — 未出现新的产品 Goal/Scope/Business Boundary/Acceptance、安全、schema或 Repository split决策。Generic Engine位置与 sequencing已由 Issue #92/#77接受；本次只是对 current implementation做 dependency closure后的 HOW收敛。

## 8. Readiness Decision

**PASS — EU-47 is a Ready Execution Unit once this planning/readiness change is integrated to `main`.**

在 planning branch上不允许直接 Execute。Integration后必须使用新的 Fresh Context重新核验 actual `main`、Issue #92/#77、Open PR / Actions与 base drift；无 drift且 Authority仍有效时，才能以 integration commit作为 EU-47 Execute baseline进入实现。