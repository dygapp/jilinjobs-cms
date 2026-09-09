# Main Site Formal Content E1～E3 Planning

## Status

- Parent Planning Authority: GitHub Issue #60
- Phase 3 re-entry: **PASS**
- E1: **COMPLETED / Authority-only**
- E2 / EU-49: **COMPLETED**
- E3: **ACTIVE**
- Current Execution Unit: **EU-50 — Main Source Discovery & Article Snapshot Promotion**
- EU-51: **BLOCKED / no Execute Authority**
- Ownership correction: **2026-09-09 — Main Page and stable ListItem content belong to JilinJobs Site Package**

## 1. Current planning boundary

Issue #60 E1～E3 remains the Main formal-content planning authority, but the current product ownership boundary is now:

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

### EU-50 — Main Source Discovery & Article Snapshot Promotion

EU-50 may contact the Legacy Source only in explicit collection/evidence runs. It owns:

- complete Main Article surface/pagination discovery;
- INTERNAL / EXTERNAL_LINK Article classification;
- Article body/resource collection;
- retry/error classification and no-silent-repair evidence;
- Article-only import eligibility / accepted snapshot promotion;
- Page/List discovery only as bounded Site Package source handoff evidence.

Page/List source findings must be retained and classified, but they do not enter `data-migrations/main/v1` Article import eligibility and do not block Article eligibility merely because Site Package follow-up is still pending.

### EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review

EU-51 remains blocked until EU-50 produces an accepted **Article-only** snapshot and its own Fresh Context readiness is established.

EU-51 must not import Main Page/List content through Historical Migration merely to bypass the Site Package boundary.

## 6. Site Package follow-up Gate

The ownership correction creates a separate Site Package planning finding:

1. Page source handoff must eventually reconcile accepted Legacy Page content into `sites/jilinjobs/structure/pages.json` (and required stable Page assets/resources) under explicit Site Package authority.
2. Main stable ListItem membership requires a new Site Package capability decision because v1 currently supports `lists` definitions but not stable `list-items` provisioning/reconcile.
3. The existing one-time bootstrap ListItems cannot simply be relabelled stable without defining stable identity, ownership/adoption, reconcile, operator mutation and upgrade semantics.

This finding does **not** expand EU-50 Execute Authority into CMS Core/Site Package provisioning implementation.

## 7. Source-error boundary

EU-50 keeps the explicit error policy:

- only confirmed HTTP 404/410 resource absence may be classified `SOURCE_RESOURCE_MISSING`;
- INTERNAL Articles whose every blocking issue is that class may be excluded pending client confirmation and must remain separately listed;
- all other Article error types remain separately classified / human-reviewable;
- transport/socket failures are never inferred to be missing-source evidence;
- Page/List errors are preserved in Site Package handoff reports and are not silently repaired, discarded or converted into migration decisions.

## 8. Current Gate

The immediate work sequence is:

```text
EU-50 frozen source/retry evidence
→ Article-only eligibility + Page/List Site Package handoff
→ resolve remaining Article human-review classifications
→ Article accepted snapshot promotion / EU-50 integration
→ separate Site Package Planning for Page/List content capability
→ only then re-evaluate EU-51 readiness from integrated Article snapshot
```

No step in this plan grants EU-51 Execute Authority.

## 9. Non-goals

- no Page/List migration fallback;
- no speculative Main-specific Runtime importer;
- no CMS Core stable ListItem redesign inside EU-50;
- no Public frontend technology change;
- no Party canonical rewrite;
- no `agentic-dev` baseline update.
