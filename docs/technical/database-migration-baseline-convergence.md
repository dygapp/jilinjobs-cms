# Database Migration Baseline Convergence Technical Plan

## Decision

Use a curated development baseline reset rather than concatenating V1–V20.

- `V1__current_cms_schema.sql`: current formal schema only.
- `V2__current_preset_data.sql`: current stable preset/site data only.
- Future migrations start at V3 and are append-only after EU-31 acceptance.

No ADR is required because the existing MySQL + Flyway architecture and migration ownership remain unchanged; EU-31 only resets development history under the Issue #59 no-persistent-upgrade constraint.

## Schema construction

V1 creates final tables in dependency order and includes all current indexes/FKs directly. Historical ALTER/DROP steps are removed. The mapping tables required by the Party importers are part of the formal runtime schema but start empty.

## Preset construction

V2 is curated from the final state represented by V4–V19, not copied as an upgrade transcript. It creates current preset records directly and excludes compatibility-only data. Historical Party articles and carousel members remain outside Flyway.

## Historical knowledge preservation

Do not modify:

- `data-migrations/party/v1/**` canonical corpus, indexes, fingerprints and reports;
- `PartyHistoricalContentMigration.kt` / `PartyHistoricalContentMigrationV2.kt`;
- `PartyCarouselMigration.kt` / `PartyCarouselMigrationV2.kt`;
- fingerprint-gated EU-29 → EU-30 position 2 LINK→ARTICLE upgrade logic;
- canonical verification behavior.

The old V1–V20 SQL files are removed from the active Flyway location. Their repository history remains available in Git; historical business migration knowledge is retained in the dedicated canonical corpus/importer layer rather than depending on old Flyway files.

## Verification

1. Static audit: active migration directory contains only the curated V1/V2 baseline.
2. Backend compilation/unit tests: `gradle clean test classes --no-daemon`.
3. CI workflow.
4. `Canonical Migration Verification` on MySQL 8.4, including fresh import and idempotent re-import.
5. Confirm canonical corpus/reports/importer files are unchanged by the PR diff.
6. Final diff-scope check: no frontend/product behavior changes.

## Rollback boundary

Before merge the Feature Branch can be abandoned without affecting `main`. After merge, development databases created from V1–V20 must be recreated; in-place upgrade from the retired development history is intentionally unsupported by this EU.
