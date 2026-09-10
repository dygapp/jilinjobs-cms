# Main ListItem Site Package Specification

## Authority

- GitHub Issue #60
- GitHub Issue #77
- `docs/requirements/main-stable-listitem-site-package.md`
- `docs/project/main-site-formal-content-plan.md`

## Status

- Specification: **READY**
- Planning baseline: `main@4d5578a2715f8adc0ebca73ee0ae7342f740c8ce`
- Candidate: Main ListItem Site Package Bootstrap Completion

## 1. Delivery model

Main ListItem data is delivered through the existing Site Package bootstrap SQL, not through Historical Migration and not through a new stable-runtime reconcile primitive.

```text
Site Package stable structure
→ bootstrap/initial-data.sql
→ ordinary operator-managed CmsListItem rows
```

`SitePackageBootstrapper` already guarantees one-time application per `(packageId, bootstrapId)` through `cms_site_bootstrap_state`.

## 2. Included Main ListItems

The bootstrap SQL contains Main-site ListItem initialization only:

- `HOME_CAROUSEL`: current Main carousel initialization rows;
- `SITE_RELATED`: 5 reviewed LINK rows;
- `SITE_REGIONAL_GRADUATES`: 31 reviewed LINK rows;
- `SITE_JILIN_UNIVERSITIES`: 60 reviewed LINK rows.

The 96 `SITE_LINKS` values come from the final reviewed EU-50 evidence at commit `b223a1d3a40b510f53a34ea9997926f8f4541a18`, `sites/jilinjobs/reports/listitem-final-adjustment-report.md`.

## 3. Excluded Party data

`PARTY_CAROUSEL` and all Party ListItems remain owned by Party migration/current Party Authority. Main bootstrap must not duplicate them.

## 4. SQL representation

Each link row uses the existing schema and parent-list lookup pattern:

```sql
INSERT INTO cms_list_item(list_id, source_type, title, url, open_mode, sort_order, enabled)
VALUES ((SELECT id FROM cms_list WHERE code='<LIST_CODE>'), 'LINK', '<TITLE>', '<URL>', 'DEFAULT', <SORT>, 1);
```

For `HOME_CAROUSEL`, existing image-path fields remain valid and unchanged unless source evidence explicitly requires a content correction.

No fixed Runtime list id is allowed. Sort order follows the accepted reviewed source ordering in deterministic increments.

## 5. Lifecycle

After bootstrap:

- rows are ordinary CmsListItem records;
- existing Admin update/delete semantics continue to apply;
- Site Package stable composition does not reconcile these rows;
- second bootstrap evaluation with the same bootstrap state does not execute the SQL again.

No stable code, ownership flag, preset flag, adoption fingerprint or package membership identity is introduced.

## 6. Manifest

Any change to `bootstrap/initial-data.sql` requires recomputing and updating `bootstrap/manifest.json.sha256`.

No Site Package schemaVersion or structure manifest change is required.

## 7. Verification

Automated verification must prove:

- manifest SHA-256 matches the SQL;
- Fresh Site bootstrap completes;
- Main carousel initialization remains present;
- exact `SITE_LINKS` counts are 5 / 31 / 60;
- accepted 96 titles/URLs are represented exactly;
- second bootstrap evaluation is `ALREADY_APPLIED` and creates no duplicates;
- Public `SITE_LINKS` returns the initialized values under existing API behavior;
- Party ListItem/migration behavior is unchanged.

Live availability of third-party URLs is not a Runtime acceptance condition.

## 8. Scope boundary

Included: documentation correction, Main bootstrap SQL data, bootstrap digest, focused verification.

Excluded: schema migration, `cms_list_item.code`, Site Package `list-items.json`, runtime reconcile/adoption/delete protection, Party ListItems, Article migration, Hui Employment iframe.

## 9. Slice readiness

This is a single small data-baseline change. The expected execution unit is bounded to Main Site Package bootstrap data + digest + focused verification. Specification is **READY** for `slice-work`.
