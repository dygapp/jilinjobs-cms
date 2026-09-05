# Database Migration Baseline Convergence Specification

## Authority

- `docs/requirements/database-migration-baseline-convergence.md`
- Issue #59 — Database Migration Baseline Convergence candidate
- Issue #60 — next executable slice review
- `data-migrations/party/v1/**` canonical Party migration assets

## Baseline Contract

### Fresh schema

Flyway shall create only the current formal CMS schema. The new baseline includes the current forms of:

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

The schema must include current columns, indexes, foreign keys and preset flags directly. It must not contain the retired `cms_article.recommended` column, retired `cms_list.item_type`, or the transitional `SERVICE` / `SITE` navigation-location baseline.

### Preset data

The baseline shall seed the current stable Main Site / Party Site structures and system definitions, including current columns, pages, navigation locations/navigation, generic lists, advertising slot, site properties, Party column tree and `PARTY_CAROUSEL` definition.

The seed shall represent current names/aliases/semantics directly, including:

- Party parent alias `party` rather than the retired `party-building` alias;
- list code `PARTY_CAROUSEL` rather than `PARTY_HOME_CAROUSEL`;
- Main navigation entry `中心党建` as `/party/` SAME_WINDOW rather than a placeholder;
- shared carousel properties `CAROUSEL_INTERVAL_SECONDS` and `CAROUSEL_MAX_ITEMS` rather than the retired Main-only property name;
- no retired `HOME_BANNERS`, `SERVICE_LINKS`, `SITE_LINK_GROUPS`, `HOME_PROMO_BANNER_PATH` or `HOME_NCSS_LOGO_PATH` configuration definitions.

### Historical migration boundary

Flyway seeds no Party historical articles and no Party historical carousel members. The two legacy-mapping tables start empty on a fresh database and are populated only by migration importers.

`data-migrations/party/v1/**`, `PartyHistoricalContentMigration*`, `PartyCarouselMigration*`, canonical reports and canonical verification workflows remain authoritative migration assets. EU-29 frozen acceptedSnapshot provenance remains 181 articles; the post-EU-30 current canonical Runtime Dataset is 183 articles. The EU-29 → EU-30 position 2 fingerprint-gated LINK→ARTICLE upgrade remains available only for an already-imported EU-29 runtime state.

## Acceptance

1. `gradle clean test classes` succeeds against the new Flyway baseline.
2. Existing CI succeeds.
3. Canonical Migration Verification creates a fresh MySQL 8.4 database from the new baseline and imports the full current canonical Party dataset successfully.
4. First canonical import creates the current 183-article Runtime Dataset and 4 Party carousel items with zero conflict/invalid results, while preserving EU-29 acceptedSnapshot 181 as provenance; re-import is idempotent.
5. Party carousel position 2 resolves as ARTICLE directly on fresh import; no Flyway seed creates an old LINK member.
6. No files under `data-migrations/party/v1/**` are removed or rewritten by the baseline reset.
7. Future Flyway changes append after the accepted baseline rather than editing it.
