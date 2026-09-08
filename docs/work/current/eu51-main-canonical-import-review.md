# EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review

## Status

- Parent: GitHub Issue #60 / E3
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Requirement: `docs/requirements/main-historical-content-migration.md`
- Specification: `docs/specifications/main-historical-content-migration.md`
- Technical Plan: `docs/technical/main-historical-content-migration.md`
- Candidate formed by: `slice-work`
- Identifier: **EU-51**
- Readiness: **PENDING / BLOCKED**
- Blocking dependency: **EU-50 integrated accepted Main canonical snapshot**
- Execute baseline: **NONE**
- Execute state: **NOT STARTED**
- Execute Authority: **NONE**

## 1. Goal

Consume the Consumer-owned Main canonical snapshot produced and integrated by EU-50, import it through the existing site-neutral Generic Content Migration application, reconcile Runtime state, prove idempotency/conflict safety and close the final Main historical-content Human Review obligations.

## 2. In scope after dependency closure

- Fresh Generic schema + JilinJobs Site Package target setup;
- `generic-content data-migrations/main/v1` import;
- expected Article / EXTERNAL_LINK Article / Page / ListItem reconciliation from accepted EU-50 evidence;
- second import SKIP/idempotency;
- source fingerprint conflict/no silent overwrite;
- Page target/precondition drift conflict;
- resource digest/path/tamper rejection;
- Runtime identity/count/resource/list-placement reconciliation;
- Main Public/Admin/Integrated Browser verification;
- representative/high-risk content and exceptional-classification Human Review;
- Party and Generic regression evidence;
- final E3 closure evidence.

## 3. Out of scope

- new Legacy Source collection as a stable verification dependency;
- silently changing the EU-50 accepted source snapshot during Runtime verification;
- Main-specific Runtime importer or duplicated Generic mutation pipeline;
- changes to Site Package stable structure solely to fit migrated content;
- Public renderer technology replacement;
- C1/C2, Issue #57/#59, Repository Split or `agentic-dev` baseline work.

## 4. Required input from EU-50

EU-51 Readiness requires an integrated repository-owned accepted snapshot that supplies at least:

- accepted dataset root and deterministic integrity record;
- exact accepted Article/Page/List item identities and counts;
- source fingerprints;
- target Column aliases / List codes / Page identities;
- accepted resource bytes, sizes and SHA-256 values;
- accepted/excluded reconciliation;
- zero unresolved current-scope classifications;
- durable provenance/source-discovery reports;
- any exceptional human classification decisions.

These are not planning constants and must not be fabricated before EU-50 integration.

## 5. Expected execution topology

```text
Fresh Generic Flyway schema
→ JilinJobs Site Package reconcile
→ optional Fresh bootstrap when required by scenario
→ Generic Content Migration: data-migrations/main/v1
→ Runtime reconciliation
→ second import
→ conflict/tamper scenarios
→ Public/Admin/Integrated Browser
→ Human Review
```

No stable EU-51 verification workflow may contact the Legacy Source.

## 6. Expected verification obligations

Once the blocking input exists, `readiness-check` must confirm exact expectations for:

1. first import total/created/applied results derived from accepted snapshot;
2. second import all expected units SKIP;
3. changed source fingerprint = CONFLICT/no silent overwrite;
4. Page target/precondition drift = CONFLICT/no unsafe mutation;
5. resource tamper/path/digest failure rejected;
6. Runtime Article/Page/List stable identities and counts reconcile exactly;
7. external targets follow E1 ownership/opening behavior;
8. Page content follows E2 ownership contract;
9. Main homepage/columns/articles/pages/list placements render accepted content;
10. representative/high-risk items and all exceptional classifications pass Human Review;
11. Party and Generic migration regressions remain PASS;
12. full repository-required CI / Integrated Browser and PR review gates pass.

## 7. Readiness Check

- Requirement: READY.
- Specification: READY.
- Technical Plan: READY.
- Generic Runtime capability: available.
- Stable Main targets: available.
- **Accepted Main canonical snapshot: NOT YET INTEGRATED.**
- Exact accepted dataset counts/digests/exception set: NOT YET AVAILABLE as durable Authority.

### Verdict

**PENDING / BLOCKED — EU-51 is a Candidate Execution Unit, not a Ready Execution Unit.**

EU-50 completion does not automatically grant EU-51 Execute Authority. After EU-50 integrates, EU-51 must run a Fresh Context dependency recovery and `readiness-check` against the actual accepted snapshot. Only a later PASS may establish EU-51 as Ready and allow its independent Execute baseline.
