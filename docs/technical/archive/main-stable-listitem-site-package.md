# Main ListItem Site Package Technical Plan

## Authority

- `docs/requirements/main-stable-listitem-site-package.md`
- `docs/specifications/main-stable-listitem-site-package.md`
- `docs/project/main-site-formal-content-plan.md`
- GitHub Issue #77

## Status

- Technical Plan: **READY**
- Planning baseline: `main@4d5578a2715f8adc0ebca73ee0ae7342f740c8ce`
- Candidate: Main ListItem Site Package Bootstrap Completion

## 1. Current implementation baseline

The repository already has the required mechanism:

- `sites/jilinjobs/bootstrap/initial-data.sql` stores one-time Main site initialization data;
- `sites/jilinjobs/bootstrap/manifest.json` stores the SQL SHA-256;
- `SitePackageBootstrapper` verifies the digest, executes the SQL transactionally, and records `(packageId, bootstrapId)` in `cms_site_bootstrap_state`;
- a previously applied bootstrap is returned as `ALREADY_APPLIED` and is not replayed.

No Generic CMS capability change is required.

## 2. Implementation

Edit only the Main Site Package bootstrap data path plus focused verification/documentation.

### 2.1 SQL

Update `sites/jilinjobs/bootstrap/initial-data.sql`:

- preserve Main `HOME_CAROUSEL` initialization;
- replace the current five `SITE_RELATED` values with the final reviewed accepted values;
- append all 31 `SITE_REGIONAL_GRADUATES` rows;
- append all 60 `SITE_JILIN_UNIVERSITIES` rows;
- keep current Main Advertisement initialization unchanged;
- do not add Party ListItems.

Use existing `CmsList.code` subqueries for `list_id`. All 96 site-link records use `source_type='LINK'`, existing open-mode semantics and deterministic sort order based on the reviewed occurrence order.

### 2.2 Content authority

The source for the 96 site-link title/URL decisions is:

`b223a1d3a40b510f53a34ea9997926f8f4541a18:sites/jilinjobs/reports/listitem-final-adjustment-report.md`

That report records 96 audited occurrences, 72 accepted adjustments and 24 unchanged values. Implementation copies those accepted decisions; it does not perform a new external-link audit.

### 2.3 Bootstrap manifest

After SQL is final, compute SHA-256 of the exact UTF-8 file bytes and update:

`sites/jilinjobs/bootstrap/manifest.json`

No change to `bootstrapId` is required for the Fresh Site initialization baseline.

## 3. Explicitly unnecessary changes

Do not modify:

- Generic Flyway migrations;
- `cms_list_item` schema;
- Generic ListItem persistence/API;
- `SitePackageProvisioning.kt` structure types;
- Site Package `manifest.json` schemaVersion;
- runtime reconcile/adoption/delete logic;
- Party migration/list data.

## 4. HOME_CAROUSEL and Party boundary

Main `HOME_CAROUSEL` uses the same bootstrap lifecycle as the other Main ListItems and remains in `initial-data.sql`.

Party carousel/list items are already covered by Party migration/current Party Authority and are not duplicated into the Main Site Package bootstrap.

## 5. Verification

Prefer the smallest extension of existing Site Package/bootstrap tests.

Required assertions:

1. bootstrap SQL digest equals `bootstrap/manifest.json.sha256`;
2. Fresh Site bootstrap applies successfully;
3. Main carousel initialization exists after bootstrap;
4. `SITE_RELATED` has exactly 5 rows;
5. `SITE_REGIONAL_GRADUATES` has exactly 31 rows;
6. `SITE_JILIN_UNIVERSITIES` has exactly 60 rows;
7. representative and adjusted values match the final report, including the accepted 湖北/四川 split and reviewed renamed university entries;
8. a second bootstrap call is `ALREADY_APPLIED` and row counts do not increase;
9. existing Public list endpoint can expose enabled `SITE_LINKS` data;
10. Party migration/ListItem regression remains unchanged.

No network request to any external target is required by this verification.

## 6. Expected repository change set

Expected Execute diff is small:

- `sites/jilinjobs/bootstrap/initial-data.sql`;
- `sites/jilinjobs/bootstrap/manifest.json`;
- focused existing test(s) if current tests do not already verify the required counts/content;
- Work/Current Evidence required by the development method.

## 7. Technical readiness

There is no unresolved architecture decision. The existing bootstrap mechanism exactly matches the requested lifecycle. Technical Plan is **READY** for `slice-work`.
