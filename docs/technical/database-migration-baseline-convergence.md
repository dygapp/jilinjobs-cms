# Database Migration Baseline Convergence Technical Plan

## Status

- EU-31: **COMPLETED / HISTORICAL BASELINE CONVERGENCE**
- Current baseline amendment: **EU-41 — Site Bootstrap & Generic Schema Baseline Separation**

## Historical EU-31 Decision

EU-31 replaced the V1–V20 development transcript with a curated development baseline and preserved canonical Party migration knowledge outside Flyway. At EU-31 completion the active baseline was:

- `V1__current_cms_schema.sql`: current formal schema;
- `V2__current_preset_data.sql`: then-current preset/site data;
- future schema migrations append after that baseline.

The repository explicitly accepted that development databases based on the retired history could be recreated because no production/persistent in-place upgrade path was required.

## EU-41 Current Amendment

Issue #77 later established that site-instance data must not share the Generic CMS Backend migration lineage. EU-41 therefore performs a controlled final development baseline replacement:

```text
backend/src/main/resources/db/migration/
├─ V1__current_cms_schema.sql
└─ V2__site_provisioning_schema_capabilities.sql
```

The new V2 contains **Generic CMS Schema capabilities only**:

- nullable `cms_navigation.code` + unique index;
- site-neutral `cms_site_bootstrap_state`.

JilinJobs instance data is removed from Backend Flyway and split by responsibility:

```text
sites/jilinjobs/structure/**
    stable Site Package structure / reconcile

sites/jilinjobs/bootstrap/**
    one-time current-schema initial operational defaults

data-migrations/**
    historical canonical content / provenance
```

The next accepted Generic CMS Schema change after EU-41 uses Backend Flyway V3 and resumes append-only evolution.

## Schema construction

V1 creates the current Generic CMS base schema directly. V2 adds generic provisioning capabilities that are independent of any concrete Site Package identity.

The mapping tables required by Party importers remain formal runtime schema and start empty.

Generic Flyway must not create current JilinJobs:

- Columns / Navigation rows;
- Pages / PageGroups;
- CmsList / AdvertisementSlot instances;
- SiteConfig values;
- CmsListItem / Advertisement defaults。

## Site bootstrap construction

The former V2 operational defaults are not renumbered migrations. They are current-schema Fresh Site installation data under `sites/jilinjobs/bootstrap/**`.

Bootstrap execution is guarded by generic `(package_id, bootstrap_id)` completion state independent of `flyway_schema_history`. Once completed, operator edits/deletes are not replayed or reconciled.

## Historical knowledge preservation

Do not remove or rewrite:

- `data-migrations/party/v1/**` canonical corpus, indexes, fingerprints and reports;
- `PartyHistoricalContentMigration.kt` / `PartyHistoricalContentMigrationV2.kt`;
- `PartyCarouselMigration.kt` / `PartyCarouselMigrationV2.kt`;
- fingerprint-gated EU-29 → EU-30 position 2 LINK→ARTICLE upgrade logic;
- canonical verification behavior;
- importer mapping schema required for historical provenance.

Old active Flyway files remain available in Git history; historical business migration knowledge remains in the dedicated canonical corpus/importer layer rather than relying on retired SQL migrations.

## Verification

1. Active migration directory contains only Generic CMS schema migrations.
2. `gradle clean test classes` / repository backend verification.
3. Generic Fresh DB contains zero JilinJobs instance rows.
4. Site Package targeted MySQL verification proves 98 stable objects independently.
5. Site bootstrap targeted verification proves first apply, already-applied guard and no-resurrection.
6. Repository CI validates Fresh JilinJobs Runtime with explicit bootstrap.
7. Canonical Migration Verification validates Fresh Party import and idempotency without Main bootstrap dependency.
8. EU-30 Migration Upgrade Verification preserves EU-29→EU-30 compatibility.
9. Review Environment uses the same explicit Fresh Site composition.
10. Final diff-scope check confirms no product behavior redesign.

## Rollback boundary

Before merge the Feature Branch can be abandoned without affecting `main`.

After EU-41 merge, development databases created from the pre-EU-41 V1/V2/V3 baseline are recreated; in-place repair of that retired development history is intentionally unsupported. Once the EU-41 baseline is accepted, future Generic CMS Schema migration changes are append-only from V3.
