# Main Single-page Formal Content Technical Plan

## Authority

- `docs/requirements/main-single-page-formal-content.md`
- `docs/specifications/main-single-page-formal-content.md`
- `docs/project/main-site-formal-content-plan.md`

## Status

- Technical Plan: **READY**
- Planning baseline: `main@f42bacf4ab7719e3291288c77f0685b428b86141`
- Scope: E2 first foundation slice only

## 1. Implementation topology

Keep the accepted application boundary:

```text
cms-server ───────────→ cms-core
content-migration ───→ cms-core
```

No new app/module is created.

Implementation areas:

```text
backend/modules/cms-core/
├── src/main/kotlin/.../page/**
├── src/main/kotlin/.../provisioning/SitePackageProvisioning.kt
└── src/main/resources/db/migration/V3__page_content_migration_mapping.sql

backend/apps/content-migration/
├── src/main/kotlin/.../migration/LegacyMappings.kt
└── src/main/kotlin/.../migration/generic/GenericContentMigration.kt

data-migrations/schemas/
└── generic Page index/item schema additions
```

Focused verification may add a dedicated Gradle task/workflow or extend Generic Content Migration Verification; final choice must preserve one clear Page-specific failure signal without duplicating the entire repository CI.

## 2. Site Package Page reconcile change

Current existing-Page branch in `SitePackageProvisioning.kt` compares and updates:

`group/name/bodyHtml/renderMode/embedUrl/sortOrder/enabled`.

Change only the E2 content fields:

- on create: keep setting `body_html`, `render_mode`, `embed_url` from package;
- on existing Page comparison: ignore these three fields;
- on existing Page UPDATE: do not write these three fields;
- continue current behavior for group/name/sort/enabled/preset in this Unit.

This is deliberately narrow. Broader preset operational-field governance is not part of E2.

Focused Site Package verification adds:

1. first provision creates package defaults;
2. mutate existing preset Page `body_html/render_mode/embed_url`;
3. second provision leaves the mutation unchanged;
4. current structural reconciliation remains PASS.

## 3. Generic schema V3

Add append-only Generic CMS migration:

`V3__page_content_migration_mapping.sql`

Create `cms_page_legacy_mapping` with at least:

- `id` BIGINT PK;
- `source_system` VARCHAR(100) NOT NULL;
- `legacy_key` VARCHAR(255) NOT NULL;
- `source_url` VARCHAR(2000) NOT NULL;
- `source_fingerprint` CHAR(64) NOT NULL;
- `page_id` BIGINT NOT NULL;
- `created_at` timestamp;
- unique `(source_system, legacy_key)`;
- FK `page_id → cms_page(id)` with delete restriction/cascade chosen consistently with current stable Page deletion protection; preferred `ON DELETE RESTRICT` because preset target deletion is already prohibited and mapping must not disappear silently.

Do not modify V1/V2. V3 is Generic schema capability and contains no Main aliases/data.

## 4. Core Page content boundary

Do not make the migration app construct a full `PageDraft` merely to change three content fields.

Add a narrow site-neutral Core capability, implementation-equivalent to:

```kotlin
data class PageContentDraft(
    val bodyHtml: String,
    val renderMode: PageRenderMode,
    val embedUrl: String?,
)

PageService.updateContent(id: Long, draft: PageContentDraft): CmsPage
```

Requirements:

- reuse current `RichTextHtmlPolicy` for RICH_TEXT;
- reuse current renderMode/embedUrl validation;
- do not change alias/group/name/sort/enabled/preset;
- remain usable only as a domain capability; no new Admin/Public endpoint is required by this Unit.

Generic migration service can call this capability inside its transaction.

## 5. Mapping ownership

Extend neutral `migration/LegacyMappings.kt` with `PageLegacyMappingMapper` / record, consistent with Article/List mapping ownership.

The mapper supports:

- find by `(sourceSystem, legacyKey)`;
- insert accepted first-apply mapping.

No update method is added in this Unit because changed source fingerprint defaults to CONFLICT.

## 6. Canonical Page models

Extend `GenericContentMigration.kt` with site-neutral models:

- `CanonicalPageIndex` / `CanonicalPageReference`;
- `CanonicalPageSource`;
- `CanonicalPageTarget(groupAlias?, pageAlias)`;
- `CanonicalPageContent(bodyHtml, renderMode, embedUrl?)`;
- `CanonicalPageResource` if body resources are present;
- `CanonicalPageRecord(sourceFingerprint, expectedTargetFingerprint, ...)`;
- `LoadedPage` / `PagePlan`;
- `GenericMigrationKind.PAGE`.

`LoadedDataset.total` / `GenericImportPlan` / reports include pages without changing Article/List semantics.

## 7. Fingerprint rules

### 7.1 Source fingerprint

Use canonical Page source data serialization defined by schema/fixture builder; source fingerprint remains repository evidence identity and is validated as SHA-256.

### 7.2 Target precondition fingerprint

Implement one shared helper for Runtime Page content fingerprint:

```text
UTF-8 JSON object with fixed key order:
{
  "bodyHtml": <stored current bodyHtml>,
  "renderMode": <enum name>,
  "embedUrl": <string or null>
}
```

SHA-256 of exact UTF-8 bytes is `expectedTargetFingerprint`.

The same helper is used by:

- fixture/canonical validation tests;
- migration preflight Runtime comparison.

Do not include Runtime id, timestamps, name, sort or enabled.

## 8. Page resource projection

Do not introduce Page Resource association.

If a Page fixture contains body resources, reuse the existing Generic file verifier and StaticResource capability. Use deterministic site-neutral target:

```text
migrated/content/pages/<sha256>.<validated-extension>
```

Public URL becomes:

```text
/static/migrated/content/pages/<sha256>.<ext>
```

Rules:

- canonical body uses the existing generic `migration-resource://<sha256>` token where compatible; do not create a Main-specific token;
- every token must resolve exactly one canonical resource;
- digest/size/path and actual image/file bytes are validated before mutation;
- static target create must use existing safe path policy;
- same digest target is reusable/idempotent;
- no target under `/static/uploads/**` or `sites/jilinjobs/assets/**`.

If implementation evidence shows current Generic token helper is Article-coupled, extract the smallest site-neutral rewrite helper rather than duplicating logic.

## 9. Preflight / execute order

Extend current dataset pipeline:

```text
load Article/List/Page
→ structural/file validation
→ Runtime target + mapping classification
→ dependency/precondition validation
→ GenericImportPlan
→ execute
→ report
```

Page classification:

1. resolve stable target;
2. load existing page mapping;
3. mapping same fingerprint + same target → SKIP;
4. mapping changed fingerprint / changed target → CONFLICT;
5. no mapping → compute Runtime target-content fingerprint;
6. mismatch expectedTargetFingerprint → CONFLICT;
7. match → APPLY plan.

Any known INVALID/CONFLICT prevents dataset execute from starting, preserving EU-47 all-known-errors-before-write behavior.

Execute Page APPLY:

- project validated static assets as required;
- call Core `updateContent`;
- insert mapping;
- return PAGE result.

Keep DB mutation + mapping insert in one transaction. Filesystem/static side effects follow the existing Generic side-effect boundary; failed DB execution must remain observable and must not cause a false successful mapping.

## 10. CLI / report

No new Main command.

Existing:

```text
generic-content <snapshot-root>
./gradlew importCanonicalContent ...
```

accepts Page units.

Report adds PAGE kind while preserving:

- total / created(or applied) / skipped / conflicts / invalid counters;
- existing machine-readable `CONTENT_MIGRATION_REPORT` label;
- nonzero failure semantics.

For compatibility with existing status enum, successful first Page APPLY may report `CREATED` even though it updates an existing stable Page row; message/kind makes the semantic explicit. Do not rename global statuses in this Unit.

## 11. Verification

Add a synthetic neutral fixture using aliases such as `docs` / `about`, never Main/Party production aliases.

Focused proof:

- Generic Fresh DB + synthetic Site Package target;
- standalone and grouped Page target;
- first Page apply;
- same input second run SKIP;
- operator edit after import + same input still SKIP/preserved;
- precondition mismatch CONFLICT/no DB mutation;
- source fingerprint drift CONFLICT;
- missing/ambiguous target INVALID;
- resource digest/path/token failure INVALID;
- existing Article/List first/second/conflict tests still PASS;
- Generic source purity still has no Party/Main aliases;
- Site Package Page content no-overwrite test;
- Backend Application Boundary verification;
- Canonical Party + Upgrade verification regression;
- Site Package Verification;
- Repository CI / Integrated Browser.

No Review Environment/Human visual gate is required because this foundation intentionally makes no actual Main content or visual change.

## 12. Rollback / side effects

Rollback boundary is the complete foundation PR.

Schema side effect: additive V3 mapping table only; no current Runtime content rows are seeded by Flyway.

Verification uses Fresh DB/temp static roots. No Main production dataset is imported by this Unit.

## 13. Technical readiness

Current code paths, V3 numbering, Page domain boundary, Generic pipeline and verification entries are all directly inspectable. No unresolved technology choice remains.

Technical Plan **READY** for `slice-work`.
