# Database Migration Baseline Convergence Requirements

## Status

- Candidate source: Issue #59 / Issue #60
- Readiness: clarified and ready for specification
- Scope: development-time Flyway baseline only

## Intent

The repository has reached a stable CMS runtime model after EU-30, while `backend/src/main/resources/db/migration/` still contains V1–V20 development history. A fresh database currently recreates transitional schema/data and then removes or rewrites parts of it. The development baseline shall be converged so a fresh database starts from the current formal model.

## Requirements

1. A fresh MySQL database shall create the current runtime schema directly. Deprecated columns, transitional navigation locations, compatibility-only structures, and already-removed fields such as `cms_article.recommended` shall not be created and later removed.
2. The baseline shall preserve the current formal preset/site data and stable runtime definitions required by Main Site, Admin and Party Site.
3. Historical Party business content shall not be converted into Flyway seed data. `data-migrations/party/v1/**` remains the canonical migration corpus.
4. The runtime migration-support schema used by the canonical importer shall remain available, including article/list-item legacy identity and fingerprint mappings.
5. Stable identity, legacy mapping, fingerprints, source evidence, canonical reports, importer behavior and idempotency shall survive the Flyway reset unchanged.
6. The EU-29 → EU-30 Party carousel position 2 LINK→ARTICLE compatibility path remains migration-only behavior in the importer. A fresh database shall not first create the old LINK state in order to exercise that compatibility path.
7. Existing production or other persistent databases are not required to upgrade in place from the V1–V20 development history. Development databases based on the retired history may be recreated.
8. After this reset is accepted, subsequent schema evolution returns to normal append-only Flyway migrations from the new baseline.
9. The change shall not alter user-visible CMS behavior, Party canonical content, public routes, admin workflows, or product scope.

## Non-goals

- Re-designing the CMS data model.
- Re-running or rewriting the accepted Party canonical dataset.
- Removing migration importers or migration evidence.
- General editorial workflow work from Issue #59.
- Deployment/production migration planning beyond the explicit development-only reset boundary.
