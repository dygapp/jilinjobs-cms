# Database Migration Baseline Convergence Requirements

## Status

- Candidate source: Issue #59 / Issue #60
- EU-31: **COMPLETED / HISTORICAL BASELINE DECISION**
- Scope: development-time Flyway baseline only
- Current amendment: Issue #77 / EU-41 supersedes the EU-31 site-data placement while preserving the development-DB recreation boundary and append-only discipline after the new accepted baseline.

## Historical Intent

EU-31 converged the former V1–V20 development transcript so a fresh database could start from the then-current formal runtime model. Its accepted baseline initially combined current CMS schema and current stable preset/site data.

EU-41 later identified that keeping site-instance data inside the Backend Flyway lineage conflicts with the accepted Generic CMS / JilinJobs Site Package ownership boundary. Therefore the **current baseline shape is amended**, while the following EU-31 decisions remain authoritative:

- development history may be reset while there is no production/persistent in-place upgrade requirement;
- historical Party migration knowledge stays outside Flyway;
- subsequent Generic CMS Schema evolution returns to append-only migrations after the accepted reset.

## Current Requirements

1. A fresh MySQL database shall create the current Generic CMS runtime schema directly. Deprecated/transitional schema shall not be created and later removed.
2. Backend Flyway shall contain **Generic CMS Schema responsibility only**. JilinJobs site structure, configuration instance values and operational defaults shall not occupy Backend migration versions.
3. JilinJobs stable site structure shall be restored by Site Package provisioning; Fresh Site operational defaults shall be restored by the separate one-time current-schema Site bootstrap lifecycle.
4. Historical Party business content shall not be converted into Flyway or Fresh Site bootstrap seed data. `data-migrations/party/v1/**` remains the canonical migration corpus.
5. The runtime migration-support schema used by canonical importers shall remain available, including article/list-item legacy identity and fingerprint mappings.
6. Stable identity, legacy mapping, fingerprints, source evidence, canonical reports, importer behavior and idempotency shall survive the baseline reset unchanged.
7. The EU-29 → EU-30 Party carousel position 2 LINK→ARTICLE compatibility path remains migration-only behavior in the importer. A fresh database shall not first create the old LINK state merely to exercise that compatibility path.
8. Existing production or other persistent databases are not required to upgrade in place from the retired development histories. Development databases based on a retired baseline may be recreated.
9. After EU-41 baseline acceptance, subsequent **Generic CMS Schema** evolution returns to normal append-only Flyway migrations from the new Backend baseline.
10. Site bootstrap does not use Flyway migration numbering and may be updated to the current compatible CMS Schema for future Fresh installations; an already completed bootstrap identity is not replayed on Existing Sites.
11. The change shall not alter user-visible CMS behavior, Party canonical content, public routes, admin workflows, or product scope.

## Current baseline target under EU-41

```text
Backend Flyway
V1__current_cms_schema.sql
V2__site_provisioning_schema_capabilities.sql

JilinJobs Site Package
structure/**          # stable reconcile authority
bootstrap/**          # one-time current-schema operational defaults

data-migrations/**    # historical canonical migration authority
```

The next accepted Generic CMS Schema change after EU-41 uses Backend Flyway V3.

## Non-goals

- Re-designing the CMS data model.
- Re-running or rewriting the accepted Party canonical dataset.
- Removing migration importers or migration evidence.
- Giving Site bootstrap its own migration-number sequence.
- General editorial workflow work from Issue #59.
- Deployment/production migration planning beyond the explicit development-only reset boundary.
