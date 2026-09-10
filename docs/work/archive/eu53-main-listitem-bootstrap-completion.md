# EU-53 — Main ListItem Bootstrap Completion

## Status

- Parent Planning Authority: GitHub Issue #60
- Architecture Authority: GitHub Issue #77
- Requirement: `docs/requirements/main-stable-listitem-site-package.md`
- Specification: `docs/specifications/main-stable-listitem-site-package.md`
- Technical Plan: `docs/technical/main-stable-listitem-site-package.md`
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Planning / Readiness PR: #129
- Readiness: **PASS**
- Execute baseline: `main@26a772928278754f478b38742423fc7b71b1f439`
- Implementation PR: #130
- Final implementation Head: `10449bedf4df38aa2daec99b80d0a9637df2f8db`
- Integrated main: `b1130b110bccdb1565c340a6cce62504ec06a87a`
- Execute state: **COMPLETED**
- Execute Authority: **TERMINATED**

## 1. Completed scope

EU-53 completed Main ListItem initialization through the existing JilinJobs Site Package one-time bootstrap SQL. No new stable ListItem Runtime subsystem was introduced.

Accepted Main bootstrap result:

- `HOME_CAROUSEL`: 1 Main homepage carousel item;
- `SITE_RELATED`: 5 reviewed LINK items;
- `SITE_REGIONAL_GRADUATES`: 31 reviewed LINK items;
- `SITE_JILIN_UNIVERSITIES`: 60 reviewed LINK items;
- Main ListItem total after Fresh bootstrap: **97**;
- existing Main advertisement bootstrap data retained.

The 96 SITE_LINKS values consume the accepted EU-50 final ListItem review decisions from historical commit `b223a1d3a40b510f53a34ea9997926f8f4541a18` (`72 adjusted + 24 unchanged`). `PARTY_CAROUSEL` and all Party ListItems remain under Party migration/current Party Authority and were not copied into Main bootstrap data.

## 2. Implementation diff

PR #130 changed only:

1. `sites/jilinjobs/bootstrap/initial-data.sql`;
2. `sites/jilinjobs/bootstrap/manifest.json`;
3. `backend/apps/cms-server/src/test/kotlin/com/jilinjobs/cms/provisioning/SiteBootstrapBaselineSeparationVerification.kt`.

The final bootstrap manifest digest is:

`sha256:45e8ea98ecf82c06e14617874f0b2fb58a4ddc681d84042cd5731afff6c6c15e`

The focused bootstrap verification now checks Fresh Site counts, grouped Main ListItem counts, `PARTY_CAROUSEL = 0`, second-apply `ALREADY_APPLIED`, and preservation of later operator changes without resurrection.

## 3. Explicit non-changes

EU-53 did not introduce:

- `cms_list_item.code` or any Flyway change;
- Site Package `list-items` structure type;
- stable ListItem identity / ownership flags;
- runtime reconcile / adoption / delete-protection semantics;
- Party ListItem or Party migration changes;
- Article migration changes;
- Hui Employment iframe changes;
- `agentic-dev` baseline changes.

## 4. Exact-head verification

Exact implementation Head `10449bedf4df38aa2daec99b80d0a9637df2f8db` passed:

- CI #987 / run `34483084921` — **PASS**;
- Site Package Verification #109 / run `34483084966` — **PASS**;
- Backend Application Boundary Verification #42 / run `34483084916` — **PASS**;
- EU-51 Main Canonical Runtime Verification #22 / run `34483085548` — **PASS**;
- EU-51 Main Imported Browser Verification #18 / run `34483084972` — **PASS**;
- Review Environment #865 / run `34483084943` — **PASS**.

The implementation PR was squash merged as `b1130b110bccdb1565c340a6cce62504ec06a87a`.

## 5. Post-Integration verification

On integrated `main@b1130b110bccdb1565c340a6cce62504ec06a87a`:

- CI #988 / run `34484030549` — **PASS**;
- Site Package Verification #110 / run `34484030699` — **PASS**;
- Backend Application Boundary Verification #43 / run `34484030679` — **PASS**.

Post-Integration evidence matches the integrated implementation commit and confirms no regression in the affected repository boundaries.

## 6. Final product boundary

Main ListItem initialization is now complete as Site Package bootstrap data:

```text
stable Site structure
→ one-time Main bootstrap SQL
→ ordinary operator-managed CmsListItem Runtime data
```

This is intentionally the same class of initialization mechanism used for other site bootstrap data. It is not Historical Article Migration and does not require a permanent ListItem reconcile framework.

Party carousel/ListItems remain independently owned by Party migration/current Party Authority.

## 7. Closure

EU-53 is **COMPLETED** and its Execute Authority is **TERMINATED**.

Current Ready Execution Unit returns to **NONE**. No subsequent Planning Candidate inherits EU-53 authority. Deferred Articles, Hui Employment iframe and other Issue #57/#59/#60 candidates remain independent future Planning work and require a new Planning / Readiness decision before Execute.
