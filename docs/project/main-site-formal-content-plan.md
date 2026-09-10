# Main Site Formal Content E1～E3 Planning

## Status

- Parent Planning Authority: GitHub Issue #60
- Phase 3 re-entry: **PASS**
- E1: **COMPLETED / Authority-only**
- E2 / EU-49: **COMPLETED**
- E3 current Article migration sequence: **COMPLETED through EU-50 / EU-51**
- EU-50 — Main Source Discovery & Article Snapshot Promotion: **COMPLETED / Execute Authority TERMINATED**
- EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review: **COMPLETED / Execute Authority TERMINATED**
- Current Ready Execution Unit: **NONE**
- EU-51 implementation integrated main: `fad4bfc17b762ec9612cf1e44a1c0af67e74a307`
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

The current accepted Main Article sequence is now closed through source discovery/promotion and Runtime import/reconciliation/Human Review. Any later extension or correction must obtain new Planning/Readiness Authority and cannot inherit EU-50 or EU-51 Execute Authority.

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

The accepted Main ownership decision means E3 does not use that capability to migrate Main Page content. Main Page source evidence discovered by EU-50 is handed to Site Package authority instead.

EU-49 is not reopened and its historical Execute Authority is not inherited.

## 5. E3 completed split

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
- no Runtime Main import was performed by EU-50。

Current Human Authority explicitly defers the 230 problem Articles and the separately excluded source-defect set until later review. They remain durable evidence, are not current import input, and do not block the current project sequence. EU-50 must not be reopened merely to repair/guess those records.

EU-50 completed Work Evidence：`docs/work/archive/eu50-main-source-discovery-promotion.md`。Execute Authority is terminated.

### EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review — COMPLETED

EU-51 consumed only the accepted repository-owned `data-migrations/main/v1/**` Article subset. Its independently recovered Execute baseline was `main@04090a3cc8dd9c85112488f036c4bc1a67003594`.

Completed Work Evidence：

`docs/work/archive/eu51-main-canonical-import-runtime-review.md`

Accepted Runtime closure:

- PR #122 final exact Head：`5adae340edcf605e36779c831fb512c31342eec8`；
- squash integrated main：`fad4bfc17b762ec9612cf1e44a1c0af67e74a307`；
- all ten canonical target Column aliases existed/enabled after Site Package provisioning；
- first Generic import：3078 CREATED，0 conflict / invalid；
- stable mapping / target Column / Article type reconciliation：PASS；
- all 2603 resources / 450,273,166 bytes reconciled to canonical size/SHA-256；
- BODY_IMAGE / ATTACHMENT Runtime projection：PASS；
- EXTERNAL_LINK navigation semantics：PASS；
- second identical import：3078 SKIPPED，0 CREATED / conflict / invalid；
- no Main Page/List migration mapping was produced；
- Public/Admin/Integrated Browser verification：PASS；
- bounded Human Review：PASS；
- Post-Integration CI #958 / run `34417382337`：PASS。

No Main-specific importer was created and Generic migration implementation/canonical bytes did not require changes. EU-51 Execute Authority is terminated after Post-Integration Authority closure.

## 6. Site Package follow-up Gate

The ownership correction creates a separate Site Package planning finding:

1. Page source handoff may later reconcile accepted Legacy Page content into `sites/jilinjobs/structure/pages.json` and required stable Page assets/resources under explicit Site Package authority.
2. Main stable ListItem membership requires a new Site Package capability decision because v1 currently supports `lists` definitions but not stable `list-items` provisioning/reconcile.
3. The existing one-time bootstrap ListItems cannot simply be relabelled stable without defining stable identity, ownership/adoption, reconcile, operator mutation and upgrade semantics.

This finding is independent of completed EU-51 and does not inherit its Execute Authority.

## 7. Source-error boundary

The accepted error policy remains:

- only confirmed HTTP 404/410 resource absence may be classified `SOURCE_RESOURCE_MISSING`;
- INTERNAL Articles whose every blocking issue is that class may be excluded pending client confirmation and must remain separately listed;
- all other Article error types remain separately classified / human-reviewable;
- transport/socket failures are never inferred to be missing-source evidence;
- Page/List errors are preserved in Site Package handoff reports and are not silently repaired, discarded or converted into migration decisions；
- deferred problem Articles remain durable later-review evidence and were excluded from EU-51 import input。

## 8. Current Gate

The completed sequence is:

```text
EU-50 accepted current Article subset — INTEGRATED / COMPLETED
→ EU-51 readiness-check — PASS
→ EU-51 independent Execute baseline
→ Fresh Runtime import / reconciliation / idempotency — PASS
→ Public/Admin/Integrated Browser — PASS
→ bounded Human Review — PASS
→ PR #122 Integration — COMPLETE
→ Post-Integration CI #958 — PASS
→ EU-51 Execute Authority — TERMINATED by closure
```

Current Ready Execution Unit is **NONE**.

The next natural Gate is a new Fresh Context Planning/Readiness decision from current Issue #60 / Issue #77 evidence. The deferred Article/client-confirmation backlog and Site Package Page/List follow-up remain separate candidates and do not gain authority from EU-51 completion.

## 9. Non-goals

- no Page/List migration fallback;
- no speculative Main-specific Runtime importer;
- no CMS Core stable ListItem redesign inside Historical Migration;
- no Public frontend technology change;
- no Party canonical rewrite;
- no `agentic-dev` baseline update;
- no automatic entry into deferred Article review or Site Package Page/List execution after EU-51 completion.