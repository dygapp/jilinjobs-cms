# Main Site Formal Content E1～E3 Planning

## Status

- Parent Planning Authority: GitHub Issue #60
- Architecture Authority: GitHub Issue #77
- Phase 3 re-entry: **PASS**
- E1: **COMPLETED / Authority-only**
- E2 / EU-49: **COMPLETED**
- E3 Article migration sequence: **COMPLETED through EU-50 / EU-51**
- Main Page Site Package follow-up / EU-52: **COMPLETED / Execute Authority TERMINATED**
- stable Main ListItem Site Package follow-up: **Requirement / Specification / Technical Plan READY; no Identifier yet**
- Current Ready Execution Unit: **NONE**
- stable ListItem Planning baseline: `main@4d5578a2715f8adc0ebca73ee0ae7342f740c8ce`
- Ownership correction: **2026-09-09 — Main Page and stable ListItem content belong to JilinJobs Site Package**

## 1. Current planning boundary

Issue #60 remains the Main formal-content planning authority, with the accepted ownership boundary:

```text
Main Article historical content
    → Historical Content Migration / EU-50～EU-51 completed accepted subset

Main Page formal content + stable Page assets
    → JilinJobs Site Package / EU-52 completed

Main stable ListItem membership
    → JilinJobs Site Package / current independent Planning path

Legacy Source Page/List observations
    → source discovery evidence / Site Package handoff only
```

This boundary supersedes earlier planning text that treated Main Page bodies or stable Main list membership as Canonical Migration units. Article, Page and stable ListItem lifecycles remain separate even though EU-50 discovered them in one bounded source run.

## 2. Why ListItem remained incomplete after source discovery

EU-50 correctly established ListItem ownership and completed a bounded content audit, but its Execute Authority was Article-only. PR #117 temporarily contained follow-up ListItem audit/report work; the final convergence deliberately restored the authorized Article-only proposed tree. The final ListItem review remained available in repository history, while Runtime stable provisioning stayed an explicit capability gap.

EU-52 later obtained independent Authority only for Main Page formal content and stable Page assets. It did not inherit or extend to ListItem. Therefore the pre-current state was intentionally:

```text
ownership/content decision = established
stable Runtime lifecycle = not yet authorized/implemented
```

The current Planning path closes that second half instead of redoing source discovery.

## 3. Completed E1 / E2 / E3 context

### E1

External-link ownership/behavior contract is closed as Authority-only work. For Article classification, both INTERNAL and EXTERNAL_LINK Articles remain Historical Migration content; ListItem owns list placement/presentation targets; fixed integrations remain separate engineering-owned behavior.

### E2 / EU-49 + EU-52

EU-49 established Page operational-content ownership protection and Generic Page migration capability. EU-52 then delivered 10 accepted Main formal Pages and their stable assets through JilinJobs Site Package with guarded Existing-Site adoption. EU-52 is completed and its Execute Authority terminated.

### E3 / EU-50～EU-51

Accepted Article result remains:

- 3314 total Article candidates;
- 3078 current import-eligible = 1577 INTERNAL + 1501 EXTERNAL_LINK;
- 6 source-defect Articles excluded pending client confirmation;
- 230 deferred problem Articles;
- 2603 accepted resource files / 450,273,166 bytes;
- accepted dataset digest `sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`;
- canonical root `data-migrations/main/v1/**`;
- EU-51 Fresh Runtime import/reconciliation/idempotency/Public/Admin/Browser/Human Review completed.

The 230 + 6 Article backlog is not part of stable ListItem work.

## 4. Stable Main ListItem accepted content authority

Current stable ListItem Planning consumes the final reviewed repository evidence from PR #117 history rather than reopening Legacy Source collection:

- commit: `b223a1d3a40b510f53a34ea9997926f8f4541a18`;
- file: `sites/jilinjobs/reports/listitem-final-adjustment-report.md`;
- source occurrences audited: **96**;
- adjusted: **72**;
- unchanged after review: **24**.

Accepted current scope:

| List | Stable members |
|---|---:|
| `SITE_RELATED` | 5 |
| `SITE_REGIONAL_GRADUATES` | 31 |
| `SITE_JILIN_UNIVERSITIES` | 60 |
| **Total** | **96** |

This final reviewed report is stronger than intermediate 66-item / 30-regional collection counts. It explicitly closes the duplicate 湖北/四川 observation as two distinct accepted product members. No current Runtime work may replace these accepted decisions with live-network guesses.

`HOME_CAROUSEL` is not part of this three-list final audit and remains current one-time bootstrap/operator-managed data. Party ListItems remain under Party authority.

## 5. Ready Requirement / Specification / Technical Plan

Current ListItem Authority:

- Requirement: `docs/requirements/main-stable-listitem-site-package.md` — **READY**;
- Specification: `docs/specifications/main-stable-listitem-site-package.md` — **READY**;
- Technical Plan: `docs/technical/main-stable-listitem-site-package.md` — **READY**;
- four-layer boundary: `docs/specifications/cms-site-package-boundary.md`.

The previously open design questions are now resolved for this scope:

1. stable identity: `(listCode, itemCode)` backed by nullable `cms_list_item.code` and unique `(list_id, code)`;
2. package representation: optional Site Package v1 `list-items` structure;
3. current capability supports stable `LINK` items only; ARTICLE/image expansion is not invented;
4. 96 item codes are explicit package identities and are not recomputed from title/URL/sort order;
5. package owns stable identity/membership and create/adoption defaults; ordinary mutable payload remains operator-maintainable after create/adoption;
6. stable-coded items cannot be deleted through ordinary Admin; ordinary `code = NULL` items remain normal operator data;
7. existing five `SITE_RELATED` bootstrap rows are adopted only by exact declared prior-baseline fingerprint; no heuristic claiming;
8. the five SITE_RELATED bootstrap INSERTs leave bootstrap when stable package structure becomes active; HOME_CAROUSEL remains bootstrap;
9. repeated reconcile is idempotent and preserves operator edits;
10. package omission does not mean deletion; stable-member retirement requires future explicit versioned Authority;
11. Public continues to consume Generic `/api/public/lists/by-group/SITE_LINKS`, with no Main-specific API.

The Generic implementation must remain site-neutral. JilinJobs-specific 96-member content belongs only under `sites/jilinjobs/**`.

## 6. Verification boundary

The planned verification must prove at least:

- append-only V4 fresh/upgrade schema path;
- nullable ordinary ListItem code and unique non-null stable identity;
- package loader/validation for optional `list-items`;
- Fresh Site exact 5 + 31 + 60 stable membership;
- Existing Site exact adoption of old five SITE_RELATED bootstrap defaults without duplication;
- ambiguous/unmatched operator rows fail closed or remain untouched as defined;
- stable item operator edits survive ordinary reconcile;
- stable item ordinary delete rejected; null-code item CRUD preserved;
- no absence-based deletion;
- Public SITE_LINKS exact package membership/title/URL/order;
- HOME_CAROUSEL, Party, Page, Article/migration and full Backend/Admin/Public/Browser regressions remain unaffected.

Because the 96 links are directly visible public content, bounded Human Review follows automated Browser PASS. Third-party site uptime is not a Runtime acceptance dependency; accepted title/URL values are already source-audited.

## 7. Source-error / no-silent-repair boundary

The existing source policy remains unchanged:

- transport/socket/403/429 observations never authorize silent content deletion or replacement;
- the final 96 ListItem decisions are frozen inputs for this Site Package work;
- implementation/runtime verification does not reopen external content audit;
- new implementation-side data inconsistency, digest mismatch, ambiguous adoption or identity conflict must fail closed and be reported;
- 230 deferred + 6 source-defect Articles remain independent later-review/customer-confirmation evidence.

## 8. Current Gate

Current Ready Execution Unit: **NONE**.

Requirement, Specification and Technical Plan for stable Main ListItem are now READY on the Planning branch. The next method Gate is to integrate this Planning Authority, then run `slice-work → readiness-check` against the resulting current `main`. Only after a Candidate Execution Unit is formed and Readiness PASS may a Fresh Context establish Execute baseline/Authority.

No EU Identifier is pre-assigned in this Planning Authority.

## 9. Non-goals

- no implementation before Planning Authority integration + Readiness PASS;
- no HOME_CAROUSEL stable conversion;
- no Party ListItem ownership change;
- no Historical Migration fallback for Page/ListItem;
- no repair/import of 230 deferred or 6 source-defect Articles;
- no Hui Employment iframe work;
- no generic absence-based removal framework;
- no Public frontend technology/API redesign;
- no `agentic-dev` baseline update.
