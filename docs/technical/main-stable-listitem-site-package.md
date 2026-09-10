# Main Stable ListItem Site Package Technical Plan

## Authority

- `docs/requirements/main-stable-listitem-site-package.md`
- `docs/specifications/main-stable-listitem-site-package.md`
- `docs/project/main-site-formal-content-plan.md`
- `docs/specifications/cms-site-package-boundary.md`
- GitHub Issue #77

## Status

- Technical Plan: **READY**
- Planning baseline: `main@4d5578a2715f8adc0ebca73ee0ae7342f740c8ce`
- Candidate scope: Main Stable ListItem Site Package Adoption
- Execution Unit identity: **not assigned before `slice-work`**

## 1. Current implementation baseline

Current Generic ListItem implementation:

- `backend/modules/cms-core/src/main/kotlin/com/jilinjobs/cms/listing/CmsList.kt`；
- `cms_list_item` has only numeric `id` + mutable content fields；
- ordinary item create/update/delete has no package identity concept；
- `CmsList` itself already has stable `code` and preset deletion protection。

Current Site Package implementation:

- `backend/modules/cms-core/src/main/kotlin/com/jilinjobs/cms/provisioning/SitePackageProvisioning.kt`；
- `SITE_PACKAGE_SCHEMA_VERSION = 1`；
- supported structure types include `lists` but not `list-items`；
- `SitePackageDefinition` has List definitions but no ListItem collection；
- stable structure reconcile executes before one-time bootstrap。

Current bootstrap:

- `sites/jilinjobs/bootstrap/initial-data.sql` creates one `HOME_CAROUSEL` item and five `SITE_RELATED` items；
- the file explicitly declares these rows ordinary operator-managed after bootstrap。

Current Flyway V2 established the reusable pattern for nullable stable identity on Navigation: package rows use a stable code while ordinary rows may keep `NULL`. This Unit reuses that site-neutral pattern rather than introducing a second ownership subsystem.

## 2. Append-only Generic Flyway V4

Add:

`backend/modules/cms-core/src/main/resources/db/migration/V4__stable_list_item_identity.sql`

with the minimum schema evolution:

```sql
ALTER TABLE cms_list_item
    ADD COLUMN code VARCHAR(100) NULL AFTER list_id;

CREATE UNIQUE INDEX uk_cms_list_item_list_code
    ON cms_list_item(list_id, code);
```

MySQL permits multiple NULL values in this unique index, so ordinary operator rows remain unconstrained relative to each other while non-null stable identity is unique within the parent list.

Do not seed JilinJobs rows in Flyway. Do not modify V1/V2/V3. Existing migration mapping tables remain independent from package identity.

Verification must cover both fresh V1→V4 apply and upgrade from the current V1/V2/V3 lineage.

## 3. Generic ListItem persistence changes

Extend internal `CmsListItemRecord` with nullable `code` and update mapper SELECTs to load it.

Rules:

- ordinary `insertItem` leaves code NULL；
- ordinary `updateItem` SQL does not update code；
- public/admin `CmsListItem` response does not need a new stable-code field for this Unit；
- ordinary create/update request contract is unchanged；
- `deleteItem` rejects rows whose code is non-null with a stable-baseline validation error；
- null-code delete behavior remains unchanged。

This keeps stable identity internal to provisioning/ownership and does not expose a new operator configuration surface.

## 4. Site Package v1 model extension

Add structure constant:

```text
LIST_ITEMS = "list-items"
```

and include it in `SUPPORTED_STRUCTURE_TYPES`.

Add a site-neutral model equivalent to:

```kotlin
data class SitePackageListItem(
    val listCode: String = "",
    val code: String = "",
    val sourceType: String = CmsListItemSourceType.LINK.name,
    val title: String = "",
    val url: String? = null,
    val openMode: String = "DEFAULT",
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val adoptionFromFingerprint: String? = null,
)
```

The current package model intentionally exposes only fields required by stable LINK items. Do not add ARTICLE/image/package-resource abstraction without current evidence.

`SitePackageDefinition` adds `listItems`; `objectCount` includes them; loader reads optional `list-items`. Existing packages without that structure entry remain valid under schemaVersion 1.

Validation:

- `listCode` follows existing structure-code rules and must exist in package List definitions；
- item `code` uses a stable lowercase code rule equivalent to Navigation code (`[a-z0-9][a-z0-9-]{0,99}`)；
- `(listCode, code)` unique；
- only `LINK` accepted in this capability version；
- title nonblank and existing size constraints respected；
- URL uses the same URI/HTTP(S) validity semantics as Generic ListItem LINK validation；
- openMode accepts existing supported modes；
- adoption fingerprint is null or lowercase SHA-256。

## 5. Stable mutable-payload fingerprint

Implement the smallest site-neutral helper local to provisioning unless an existing helper can be reused without changing its accepted contract.

Canonical serialization keys, fixed order:

```text
sourceType
articleId
title
subtitle
url
imagePath
imageResourceId
openMode
sortOrder
enabled
extraJson
```

Then:

```text
fingerprint = SHA-256(UTF-8 canonical JSON)
```

Current package LINK target maps omitted fields to null. Runtime existing row is fingerprinted from stored values exactly; no trimming, redirect resolution or network normalization occurs during adoption.

The fingerprint is used only to prove exact prior package ownership for adoption, not as long-term identity.

## 6. Provisioning algorithm

Provision order becomes:

```text
Columns / PageGroups / Pages
Navigation
SiteConfig
Lists
ListItems
AdvertisementSlots
```

ListItems must run after parent Lists.

For each package item:

```text
parent = resolve CmsList by listCode
require parent preset/package-owned
existing = find row by (parent.id, item.code)

if existing != null:
    require existing.sourceType == target.sourceType
    if mutablePayload(existing) == target defaults:
        unchanged
    else:
        preserve mutable payload
        report protectedListItemContent
else:
    if target.adoptionFromFingerprint != null:
        matches = null-code rows in same list whose exact fingerprint matches baseline
        if matches.size > 1:
            fail STABLE_LIST_ITEM_ADOPTION_AMBIGUOUS
        if matches.size == 1:
            assign stable code
            write current target defaults once
            report adoptedListItems
        else:
            create target with stable code
    else:
        create target with stable code
```

All writes remain in the existing Site Package transaction. No network access is performed by reconcile.

A stable-coded Runtime row with source-type mismatch is a structural conflict and fails closed. Mutable-content difference is not a structural conflict and is preserved/reported.

## 7. No implicit retirement

Do not query for "all stable rows absent from current package" and delete/disable/detach them. Package omission has no removal semantics in this release.

This is deliberate: there is no accepted previous-package retirement precondition or operator-divergence policy that would make absence-based deletion safe. Future retirement must use explicit versioned Authority and may extend the package model then.

## 8. Provisioning report

Extend `SiteProvisioningReport` with deterministic default-empty collections:

```kotlin
val adoptedListItems: List<String> = emptyList()
val protectedListItemContent: List<String> = emptyList()
```

Stable report identity:

```text
<listCode>/<itemCode>
```

Maintain existing counters/report JSON compatibility. Successful adoption may count as updated; protected payload is observable even when no DB write occurs. Structural/ambiguous adoption conflicts continue to fail the provision call.

## 9. JilinJobs package data

Create:

`sites/jilinjobs/structure/list-items.json`

with exactly 96 entries:

- 5 `SITE_RELATED`；
- 31 `SITE_REGIONAL_GRADUATES`；
- 60 `SITE_JILIN_UNIVERSITIES`。

Content source is the final reviewed repository evidence:

`b223a1d3a40b510f53a34ea9997926f8f4541a18:sites/jilinjobs/reports/listitem-final-adjustment-report.md`.

Generate explicit stable codes:

- related `001..005`；
- regional `001..031`；
- university `001..060`。

Use `sortOrder = reviewed source occurrence order * 10` unless current accepted Site Package order evidence already defines another explicit value. The item code remains unchanged if sortOrder is later edited.

For the five `SITE_RELATED` items, compute and persist `adoptionFromFingerprint` from the exact old bootstrap rows currently in `sites/jilinjobs/bootstrap/initial-data.sql`. Before writing the final package, verification must independently recompute all five fingerprints and prove the input bytes/fields match current main baseline.

The accepted 96 titles/URLs are not revalidated against the live internet during Execute; content audit authority has already closed.

## 10. Bootstrap transition

Edit `sites/jilinjobs/bootstrap/initial-data.sql` only to remove the five `SITE_RELATED` INSERT rows and update its explanatory comments as needed.

Keep the single HOME_CAROUSEL ListItem and existing Advertisement row untouched.

Fresh Site verification must prove stable provisioning creates SITE_RELATED before bootstrap and bootstrap no longer duplicates it. Existing Site bootstrap-state is not reset or replayed.

## 11. Verification implementation

Prefer extending existing Site Package / Backend verification rather than creating an unrelated parallel framework.

Required focused cases:

### Schema / Generic Core

- current V1/V2/V3 lineage upgrades to V4；
- fresh V1..V4 apply；
- ordinary code=NULL items remain valid and multiple NULL rows under one list are allowed；
- duplicate non-null `(list_id, code)` rejected。

### Loader / validation

- package without list-items remains valid；
- valid stable LINK list-items load；
- duplicate code, unknown parent list, invalid URL/openMode/fingerprint and unsupported ARTICLE fail closed。

### Fresh Runtime

- provision creates exact 96 stable Main SITE_LINKS items；
- 5/31/60 count arithmetic holds；
- second provision creates no duplicate and is idempotent；
- bootstrap leaves exactly one HOME_CAROUSEL current default and does not duplicate SITE_RELATED。

### Existing Site transition

Seed a pre-V4/current bootstrap-like site with the five exact unkeyed SITE_RELATED rows:

- first stable provision adopts exactly five；
- three accepted content corrections are applied once；
- two unchanged rows gain identity；
- a deliberately operator-diverged unkeyed row is not adopted/overwritten；
- duplicate exact baseline candidates produce a fail-closed ambiguity；
- subsequent operator edit to an adopted stable item survives reconcile；
- ordinary stable-item delete is rejected；ordinary null-code item CRUD remains valid。

### Package/data integrity

- exact stable key set is 5 + 31 + 60；
- every title/URL corresponds to the accepted final-report decision；
- no historical source URL is silently substituted for an adjusted accepted target；
- Site Package manifest digest is correct；
- Generic package code contains no JilinJobs-specific constants/data。

### Public / compatibility

- `/api/public/lists/by-group/SITE_LINKS` exposes three lists with accepted enabled members；
- Main Public Renderer shows all three groups with expected title/href/order in a Fresh Runtime；
- no new Main-specific endpoint；
- existing HOME_CAROUSEL, Party, Page, Article and migration regression suites remain PASS；
- Backend/Admin/Public builds and Integrated Browser gate remain PASS。

Because the final 96-item projection is user-visible, run bounded Human Review only after automated Browser PASS. Human Review evaluates grouping/layout and representative target presentation; it does not make external-site uptime an acceptance dependency.

## 12. Expected repository changes during Execute

Expected implementation areas are bounded to:

- append-only Generic Flyway V4；
- Generic ListItem internal persistence/delete protection；
- Generic Site Package loader/validator/reconcile/report；
- `sites/jilinjobs/structure/list-items.json` + manifest version/digest；
- removal of five SITE_RELATED rows from bootstrap；
- focused tests/workflow assertions and current Work evidence。

No Generic Historical Migration implementation, canonical Article bytes, Page content/assets, Public API redesign, iframe work or Party ownership change is required.

## 13. Rollback / side effects

Planning changes are documentation only.

Execute rollback is the complete implementation PR. Schema evolution itself remains append-only; rollback of application behavior does not rewrite historical Flyway migrations.

The adoption algorithm mutates existing rows only after exact prior-bootstrap fingerprint match. Operator-diverged/null-code rows are preserved. Stable rows cannot be removed through ordinary Admin after adoption; this is the intended membership-protection behavior.

## 14. Technical readiness

Current schema, Generic ListItem service, Site Package loader/reconcile code, bootstrap data, final 96-item reviewed evidence and Public SITE_LINKS contract are all directly inspectable. The design reuses existing stable-code and Page-style guarded-adoption patterns and introduces no unresolved product decision.

Technical Plan is **READY**. The next method stage is `slice-work`; only after a Candidate Execution Unit is formed and `readiness-check` PASS may Execute Authority be established in a Fresh Context.
