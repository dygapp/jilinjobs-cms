# Database Migration Baseline Convergence Specification

## Authority

- `docs/requirements/database-migration-baseline-convergence.md`
- Issue #59 — historical EU-31 baseline candidate
- Issue #77 — current CMS Core / Site Package boundary
- `docs/specifications/cms-site-package-boundary.md`
- `data-migrations/party/v1/**` canonical Party migration assets

## Status

EU-31 is completed historical baseline work. EU-41 amends the **current active baseline shape** so Backend Flyway is a Generic CMS Schema-only lineage and JilinJobs instance data is restored by Site Package lifecycle rather than Flyway.

## Current Baseline Contract

### Generic CMS Fresh schema

Backend Flyway creates only the current formal Generic CMS schema/capabilities, including the current forms of:

- `cms_column`
- `cms_navigation_location`
- `cms_navigation`
- `cms_page_group`
- `cms_page`
- `cms_site_config`
- `cms_article`
- `cms_resource`
- `cms_article_resource`
- `cms_list`
- `cms_list_item`
- `cms_ad_slot`
- `cms_advertisement`
- `cms_article_legacy_mapping`
- `cms_list_item_legacy_mapping`
- `cms_site_bootstrap_state`

The schema includes current columns, indexes, foreign keys and generic preset/provisioning capabilities directly. It does **not** create any JilinJobs instance rows.

Current active Backend migration target under EU-41:

```text
V1__current_cms_schema.sql
V2__site_provisioning_schema_capabilities.sql
```

V2 contains Generic Navigation stable-identity capability plus site-neutral bootstrap-state capability. The next Generic Schema change after this accepted baseline is V3 and resumes append-only Flyway evolution.

### Stable Site data

The current stable Main / Party Site structures and definitions are not Flyway seed data. They are versioned under `sites/jilinjobs/structure/**` and are restored through `SitePackageProvisioner` using stable identities.

This includes current:

- Columns / Party tree;
- Pages / PageGroups;
- NavigationLocations / NavigationItems;
- CmsList definitions;
- AdvertisementSlots;
- SiteConfig values.

### Fresh Site initial operational defaults

Current ordinary initial defaults are stored under `sites/jilinjobs/bootstrap/**` and applied once after stable Site structure provisioning.

They are not Flyway migrations, do not use `Vx` numbering, and do not become reconciled preset objects after creation. Their source artifact is maintained against the current compatible CMS Schema.

### Historical migration boundary

Flyway and Site bootstrap seed no Party historical articles and no Party historical carousel members. Legacy-mapping tables start empty on a Generic Fresh database and are populated only by migration importers.

`data-migrations/party/v1/**`, `PartyHistoricalContentMigration*`, `PartyCarouselMigration*`, canonical reports and canonical verification workflows remain authoritative migration assets. EU-29 frozen acceptedSnapshot provenance remains 181 articles; the post-EU-30 current canonical Runtime Dataset is 183 articles. The EU-29 → EU-30 position 2 fingerprint-gated LINK→ARTICLE upgrade remains available only for an already-imported EU-29 runtime state.

## Development reset boundary

The repository still has no requirement to in-place upgrade production/persistent databases from retired development migration histories. Development databases based on the pre-EU-41 active V1/V2/V3 history are recreated.

This is a controlled baseline replacement, not a general permission to rewrite accepted migrations indefinitely. After EU-41 acceptance, Generic CMS Schema migrations return to append-only discipline starting at V3.

## Acceptance

1. `gradle clean test classes` succeeds against the current Generic Flyway baseline.
2. Generic Flyway creates zero JilinJobs site-instance rows.
3. Site Package stable provisioning creates current 98 stable objects independently of Flyway site data.
4. One-time Site bootstrap creates the accepted 6 `CmsListItem` + 1 `Advertisement`, and repeated bootstrap does not duplicate or resurrect operator-deleted data.
5. Existing CI succeeds with Fresh JilinJobs Runtime explicitly composing stable Site Package + bootstrap.
6. Canonical Migration Verification creates a fresh MySQL database from Generic Schema + stable Site Package and imports the full current canonical Party dataset successfully without requiring Main operational bootstrap.
7. First canonical import creates the current 183-article Runtime Dataset and 4 Party carousel items with zero conflict/invalid results, while preserving EU-29 acceptedSnapshot 181 as provenance; re-import is idempotent.
8. Party carousel position 2 resolves as ARTICLE directly on fresh import; no Flyway or Site bootstrap seed creates an old Party LINK member.
9. No files under `data-migrations/party/v1/**` are removed or rewritten by the baseline reset.
10. Review Environment reproduces the Fresh lifecycle using explicit Site Package composition.
11. Future Backend Flyway changes append after the EU-41 Generic baseline rather than editing it.
