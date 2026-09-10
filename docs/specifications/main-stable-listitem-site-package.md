# Main Stable ListItem Site Package Specification

## Authority

- GitHub Issue #60 / stable Main ListItem Site Package follow-up
- GitHub Issue #77
- `docs/requirements/main-stable-listitem-site-package.md`
- `docs/project/main-site-formal-content-plan.md`
- `docs/specifications/cms-site-package-boundary.md`

## Status

- Specification: **READY**
- Technical Planning: **REQUIRED / defined in companion Technical Plan**
- Planning baseline: `main@4d5578a2715f8adc0ebca73ee0ae7342f740c8ce`
- Execution Unit identity: **not assigned before `slice-work`**

## 1. Accepted projection

The current product projection consists only of the 96 reviewed Main SITE_LINKS occurrences:

```text
SITE_RELATED              5
SITE_REGIONAL_GRADUATES  31
SITE_JILIN_UNIVERSITIES  60
                         --
                         96
```

The accepted title/URL decision for every occurrence is the final repository evidence at:

`b223a1d3a40b510f53a34ea9997926f8f4541a18:sites/jilinjobs/reports/listitem-final-adjustment-report.md`.

This evidence supersedes intermediate 66-item / 30-regional handoff counts for this stable-content decision. The implementation must not re-derive accepted content from current network responses.

`HOME_CAROUSEL` and Party lists are outside this projection.

## 2. Stable package identity

### 2.1 Runtime identity

Add a nullable site-neutral stable code to `cms_list_item`.

Logical identity:

```text
Runtime: (list_id, code)
Package: (listCode, itemCode)
```

Rules:

- `code = NULL` means ordinary operator-created item;
- non-null `code` means stable package membership identity;
- unique within the parent list;
- title, URL, sortOrder, enabled and database id are not identity;
- code is never changed by ordinary Admin update.

### 2.2 Current JilinJobs code set

Current package assigns explicit opaque codes to the frozen reviewed occurrence set:

- `SITE_RELATED`: `related-001` ... `related-005`;
- `SITE_REGIONAL_GRADUATES`: `regional-001` ... `regional-031`;
- `SITE_JILIN_UNIVERSITIES`: `university-001` ... `university-060`.

The ordinal is a one-time package key assignment tied to the frozen reviewed occurrence. It is **not** a Runtime derivation rule and must not be regenerated from later `sortOrder`, title, URL or current source order. Reordering content does not change itemCode.

## 3. Package representation

Extend Site Package v1 with one optional structure type:

```text
list-items
```

represented by `sites/jilinjobs/structure/list-items.json`.

Current item shape:

```json
{
  "listCode": "SITE_RELATED",
  "code": "related-001",
  "sourceType": "LINK",
  "title": "中国高等教育学生信息网",
  "url": "https://www.chsi.com.cn/",
  "openMode": "DEFAULT",
  "sortOrder": 10,
  "enabled": true,
  "adoptionFromFingerprint": "<optional sha256>"
}
```

Current scope intentionally supports package-owned stable `LINK` items only. The 96 accepted SITE_LINKS items have no package-owned ListItem image/resource and no ARTICLE target. Generic ARTICLE/image package representation is not invented without a current consumer requirement.

`listCode` must resolve to an existing package-owned `CmsList` definition. `code` must be valid and unique inside that list. URL/openMode use existing Generic ListItem validation semantics.

The optional `list-items` structure type is additive: packages that do not declare it continue to be valid Site Package schemaVersion 1 packages. Therefore no Site Package schemaVersion bump is required for this unit.

## 4. Package/default vs operator ownership

For a stable-coded ListItem:

Package-owned invariants:

- parent list identity;
- stable item code;
- current supported source type (`LINK`);
- membership existence.

Operator-maintainable payload after create/adoption:

- title;
- subtitle;
- URL;
- open mode;
- sort order;
- enabled state;
- other existing mutable ListItem payload fields that remain valid for the fixed source type.

Ordinary Site Package reconcile must not overwrite operator-maintained payload merely because it differs from package defaults. Stable item code is not exposed as an ordinary editable Admin field.

## 5. Canonical mutable-payload fingerprint

Guarded adoption uses SHA-256 over a fixed UTF-8 JSON serialization of the current ListItem mutable/source payload, excluding Runtime id, parent list id/code and stable item code:

```json
{"sourceType":<enum>,"articleId":<number-or-null>,"title":<string>,"subtitle":<string-or-null>,"url":<string-or-null>,"imagePath":<string-or-null>,"imageResourceId":<number-or-null>,"openMode":<string>,"sortOrder":<number>,"enabled":<boolean>,"extraJson":<string-or-null>}
```

Keys and ordering are fixed as shown. Current package entries are LINK items, so `articleId` and image-resource fields are null.

`adoptionFromFingerprint` is optional and must be lowercase SHA-256. It authorizes adoption of exactly one declared prior package baseline; it is not a heuristic-match flag.

## 6. Reconcile classification

For each package item, resolve the parent list first and then classify:

### 6.1 Stable identity already exists

- source type must match the package structural invariant; mismatch is `STABLE_IDENTITY_CONFLICT` and fails closed;
- mutable payload is preserved even when different from package default;
- target/default equality is unchanged/no-op;
- protected divergence is observable in the provisioning report but does not fail the whole reconcile.

### 6.2 Stable identity does not exist

If `adoptionFromFingerprint` is declared:

- search only `code = NULL` rows in the same parent list;
- exactly one fingerprint match -> adopt that row by assigning code; if prior baseline differs from current package target, update the mutable payload once to the accepted target default;
- more than one match -> fail closed as ambiguous adoption;
- zero matches -> create the package target as a new stable item.

If no adoption fingerprint is declared, create the package target directly. Existing null-code operator rows are never guessed as the target.

### 6.3 Missing package entry

Absence from a later package is **not a deletion instruction**. Ordinary reconcile leaves previously stable-coded Runtime rows unchanged. Any future stable-member retirement requires separate explicit versioned authority.

## 7. Current Existing-Site transition

Only the five historical `SITE_RELATED` bootstrap defaults have a known prior package baseline in current repository code. Their package entries declare the exact prior-bootstrap fingerprints so Existing Sites can adopt them safely.

On successful adoption:

- stable code is assigned;
- the three reviewed corrections are applied once where the exact old bootstrap baseline proves package ownership;
- the two unchanged rows acquire stable identity without unnecessary content mutation;
- repeated reconcile is idempotent;
- subsequent operator edits are protected.

The 31 regional + 60 university members were not current bootstrap defaults, so they are created as stable package rows. Any unrelated pre-existing null-code rows remain operator-owned.

## 8. Bootstrap transition

Remove the five `SITE_RELATED` inserts from `sites/jilinjobs/bootstrap/initial-data.sql` when `list-items` becomes active, preventing Fresh Site duplication.

Keep:

- current `HOME_CAROUSEL` bootstrap ListItem;
- current Advertisement bootstrap row;
- bootstrap one-time state semantics.

Fresh composition remains:

```text
Generic Flyway schema
→ Site Package stable structure including list-items
→ one-time bootstrap for remaining ordinary defaults
→ stable asset projection
→ optional Historical Article migration
```

## 9. Admin contract

No public/admin request field is added for stable code.

Generic ListItem persistence may carry the nullable code internally. Ordinary create always leaves code NULL; ordinary update preserves existing code. Delete behavior:

- non-null code -> reject deletion as stable website baseline membership;
- null code -> existing delete behavior unchanged.

The current source-type immutability rules remain in force.

## 10. Provisioning report

Extend the existing machine-readable `SITE_PACKAGE_PROVISION_REPORT` with deterministic ListItem-specific observations sufficient to distinguish safe adoption and protected operator divergence, for example:

```text
adoptedListItems: ["SITE_RELATED/related-001", ...]
protectedListItemContent: ["SITE_RELATED/related-002", ...]
```

Equivalent naming is acceptable if deterministic and identity-specific. Ambiguous/structural conflicts fail closed rather than being hidden in generic counters.

Existing report fields and Page adoption/protection evidence remain backward compatible.

## 11. Public contract

No new Main-specific endpoint is introduced. Existing `/api/public/lists/by-group/{groupCode}` remains the source for `SITE_LINKS`.

Verification must prove that a Fresh Site exposes all three lists and exactly their 96 accepted stable members in package order, subject only to existing `enabled` filtering after operator mutation.

The renderer does not consume stable item code as product UI data unless an existing Generic response already exposes it; stable code is primarily provisioning identity.

## 12. Verification contract

Required automated evidence:

- V1/V2/V3 -> append-only V4 migration and fresh V1..V4 schema apply;
- multiple null-code items remain legal; duplicate non-null code within one list is rejected;
- loader accepts package without `list-items` and package with valid `list-items`;
- invalid/duplicate code, missing parent list, unsupported source type and invalid URL fail closed;
- Fresh Site exactly 96 accepted SITE_LINKS stable rows;
- old SITE_RELATED bootstrap baseline adopts exactly five without duplicate rows;
- ambiguous baseline adoption fails closed;
- unrelated operator null-code rows survive;
- ordinary edit of stable row survives reconcile;
- ordinary delete of stable row is rejected; null-code row delete succeeds;
- repeated reconcile is idempotent;
- package omission does not delete existing stable rows;
- Public by-group output and Main renderer contain the accepted three-group membership/title/URL/order;
- HOME_CAROUSEL/Party/Article/Page behavior remains unaffected.

Automated Browser verification precedes bounded Human Review. Human Review checks rendered grouping/layout and representative navigation presentation, not live availability of every external third-party endpoint.

## 13. Scope boundary

Included:

- Generic nullable ListItem stable identity and delete protection;
- Generic Site Package stable LINK ListItem loader/validation/reconcile/adoption/report capability;
- append-only schema evolution;
- the accepted 96 Main SITE_LINKS package entries;
- SITE_RELATED bootstrap transition;
- focused Backend/Site Package/Public/Admin compatibility verification.

Excluded:

- HOME_CAROUSEL stable conversion;
- ARTICLE/image ListItem package expansion;
- Party ownership changes;
- Article exception backlog;
- Hui Employment iframe;
- absence-based removal framework;
- frontend technology/API redesign;
- agentic-dev baseline update.

## 14. Slice readiness

Schema identity, Generic package primitive, Existing-Site adoption, bootstrap transition, concrete 96-item package data and runtime verification are one atomic vertical delivery boundary. Shipping any subset alone either leaves stable membership unrepresentable or produces duplicated/stale Runtime data.

This Specification is **READY**. Technical implementation details are frozen in the companion Technical Plan; `slice-work` may form a single Candidate Execution Unit after that plan is READY.
