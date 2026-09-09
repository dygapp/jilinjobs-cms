# Main Site Formal Content E1～E3 Planning

## Status

- Parent Planning Authority: GitHub Issue #60
- Phase 3 re-entry: **PASS**
- E1: **COMPLETED / Authority-only**
- E2 / EU-49: **COMPLETED**
- E3: **ACTIVE**
- EU-50 — Main Source Discovery & Article Snapshot Promotion: **COMPLETED / Execute Authority TERMINATED**
- EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review: **READY / Readiness PASS / Execute baseline PENDING**
- Current Ready Execution Unit: **EU-51**
- EU-51 Readiness baseline: `main@fd192460cb481645c1f1af435cbe5451145797d9`
- Ownership correction: **2026-09-09 — Main Page and stable ListItem content belong to JilinJobs Site Package**

## 1. Current planning boundary

Issue #60 E1～E3 remains the Main formal-content planning authority, but the current product ownership boundary is:

```text
Main Article historical content
    → E3 Historical Content Migration

Main Page content
Main stable ListItem membership
    → JilinJobs Site Package

Legacy Source Page/List observations
    → source discovery evidence / Site Package handoff only
```

This boundary supersedes earlier E3 planning text that treated Main Page bodies or Main list membership as Canonical Migration units.

## 2. Why the boundary changed

Current repository implementation proves that Page and ListItem have different long-term lifecycle semantics from historical Articles:

- `sites/jilinjobs/structure/pages.json` already owns stable Page identities and `bodyHtml` defaults;
- `SitePackageProvisioning` already loads/reconciles `pages` as stable Site Package structure;
- stable CmsList definitions already belong to `sites/jilinjobs/structure/lists.json`;
- current Site Package v1 does **not** yet have a `list-items` structure type or stable ListItem identity/reconcile path;
- the six current Main bootstrap ListItems are one-time operator defaults, which no longer matches the accepted ownership for stable Main list membership.

Therefore Page/List content must not be forced through Historical Migration merely because the Generic Migration application can technically mutate those Runtime types.

## 3. E1 accepted result

E1 continues to define link behavior/ownership:

- `EXTERNAL_LINK` Article owns article title + external target;
- Navigation owns stable navigation label/target/open mode;
- ListItem owns list placement/presentation target;
- fixed integration remains engineering-owned.

For E3, only Column content classified as INTERNAL / EXTERNAL_LINK Article is migration content. Main stable list placement is Site Package content.

## 4. E2 / EU-49 historical result

EU-49 established a site-neutral Generic Page migration capability and Page content conflict guard. That capability remains valid Generic CMS capability and compatibility evidence.

The latest Main ownership decision means **E3 no longer uses that capability to migrate Main Page content**. Main Page source evidence discovered by EU-50 is handed to Site Package authority instead.

EU-49 is not reopened and its historical Execute Authority is not inherited.

## 5. E3 current split

### EU-50 — Main Source Discovery & Article Snapshot Promotion — COMPLETED

EU-50 completed the explicit external-source stage and integrated the accepted current Article subset through PR #117.

Accepted result:

- total Article candidates: 3314；
- current import-eligible: 3078 = 1577 INTERNAL + 1501 EXTERNAL_LINK；
- source-defect excluded pending client confirmation: 6；
- deferred problem Articles: 230；
- current accepted resources: 2603 files / 450,273,166 bytes；
- canonical dataset: `data-migrations/main/v1/**`；
- dataset digest: `sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`；
- Page/List discoveries preserved as Site Package handoff evidence；
- no Runtime Main import was performed。

Current Human Authority explicitly defers the 230 problem Articles and the separately excluded source-defect set until later review. They remain durable evidence, are not current import input, and do not block the current project sequence. EU-50 must not be reopened merely to repair/guess those records.

EU-50 completed Work Evidence：`docs/work/archive/eu50-main-source-discovery-promotion.md`。Execute Authority is terminated.

### EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review — READY

EU-51 was already the downstream Candidate formed by E3 `slice-work`. Its only blocking dependency was the accepted EU-50 Article snapshot. That dependency is now satisfied and the Fresh Context downstream `readiness-check` has passed on `main@fd192460cb481645c1f1af435cbe5451145797d9`.

Current Work：

`docs/work/current/eu51-main-canonical-import-runtime-review.md`

Readiness evidence confirms:

- the accepted repository-owned Main dataset is stable/offline-verifiable and contains exactly 3078 current import Articles;
- all ten canonical target Column aliases exist as enabled JilinJobs Site Package Columns;
- existing Generic Content Migration supports Article validation/preflight/apply, stable mapping, fail-closed conflict/invalid handling and same-input idempotency;
- existing repository tasks expose Site Package provisioning and site-neutral canonical import;
- existing CI / Review Environment / browser infrastructure can carry Runtime and Human Review evidence;
- deferred/source-defect Articles and Page/List Site Package work remain explicitly out of scope.

Required Runtime ordering is:

```text
Fresh MySQL
→ Generic Flyway V1/V2/V3
→ JilinJobs Site Package stable provisioning
→ Generic Main canonical import
→ Runtime reconciliation + second-run idempotency
→ Public/Admin/Integrated Browser verification
→ bounded Human Review
```

EU-51 must not:

- inherit EU-50 Execute Authority；
- import deferred/source-defect Articles；
- import Main Page/List content through Historical Migration merely to bypass the Site Package boundary；
- contact Legacy Source during stable import/verification；
- begin Runtime mutation before its independent Execute baseline is established from an integrated Planning/Readiness state。

## 6. Site Package follow-up Gate

The ownership correction creates a separate Site Package planning finding:

1. Page source handoff must eventually reconcile accepted Legacy Page content into `sites/jilinjobs/structure/pages.json` (and required stable Page assets/resources) under explicit Site Package authority.
2. Main stable ListItem membership requires a new Site Package capability decision because v1 currently supports `lists` definitions but not stable `list-items` provisioning/reconcile.
3. The existing one-time bootstrap ListItems cannot simply be relabelled stable without defining stable identity, ownership/adoption, reconcile, operator mutation and upgrade semantics.

This finding is not an EU-51 dependency and does not gain Execute Authority from EU-51 readiness.

## 7. Source-error boundary

The accepted error policy remains:

- only confirmed HTTP 404/410 resource absence may be classified `SOURCE_RESOURCE_MISSING`;
- INTERNAL Articles whose every blocking issue is that class may be excluded pending client confirmation and must remain separately listed;
- all other Article error types remain separately classified / human-reviewable;
- transport/socket failures are never inferred to be missing-source evidence;
- Page/List errors are preserved in Site Package handoff reports and are not silently repaired, discarded or converted into migration decisions；
- deferred problem Articles remain durable later-review evidence and are excluded from EU-51 import input。

## 8. Current Gate

The current sequence is now:

```text
EU-50 accepted current Article subset — INTEGRATED
→ EU-51 readiness-check — PASS
→ integrate EU-51 Planning/Readiness Authority
→ Fresh Context Execute-baseline recovery
→ only then may Runtime Main import begin
```

The deferred Article/client-confirmation backlog is processed separately after the current task sequence and does not block this Gate. Site Package Page/List follow-up remains an independent Planning/Readiness path.

EU-51 Readiness PASS does not by itself establish Execute Authority on the Planning/Readiness branch.

## 9. Non-goals

- no Page/List migration fallback;
- no speculative Main-specific Runtime importer;
- no CMS Core stable ListItem redesign inside Historical Migration;
- no Public frontend technology change;
- no Party canonical rewrite;
- no `agentic-dev` baseline update.
