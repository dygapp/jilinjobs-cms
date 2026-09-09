# EU-50 — Main Source Discovery & Article Snapshot Promotion

## Status

- Parent: GitHub Issue #60 / E3
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Requirement: `docs/requirements/main-historical-content-migration.md`
- Specification: `docs/specifications/main-historical-content-migration.md`
- Technical Plan: `docs/technical/main-historical-content-migration.md`
- Identifier: **EU-50**
- Readiness: **PASS**
- Execute state: **ACTIVE in PR #117**
- Current migration scope: **ARTICLE ONLY**
- Page/List ownership correction: **Site Package**
- EU-51: **BLOCKED / no Execute Authority**

## 1. Goal

Turn bounded Legacy Main source observations into a complete, reviewable, Consumer-owned **Article** snapshot and durable evidence, while preserving Page/List source findings as explicit Site Package handoff evidence.

EU-50 stops before Runtime Main import/final migrated-content review.

## 2. In scope

- source root/redirect discovery;
- complete Main Article surface/pagination inventory;
- INTERNAL / EXTERNAL_LINK Article classification;
- Article body/image/attachment collection;
- deterministic identities/fingerprints/resource digests;
- finite targeted retry and terminal error classification;
- Article-only eligibility and accepted snapshot promotion;
- separate source-defect Article list for client confirmation;
- separate human-review Article blockers;
- Page/List source discovery only as Site Package handoff evidence;
- preservation of Page/List errors/duplicates/unsupported findings without silent repair/drop;
- explicit Actions workflows for external-source collection/retry and offline eligibility processing.

## 3. Out of scope

- Runtime Main import;
- EU-51 execution;
- Main Page migration;
- Main stable ListItem migration;
- changing Site Package Page content in `sites/jilinjobs/**` during this Unit;
- adding stable ListItem structure/reconcile capability to CMS Core/Site Package;
- changing Generic Flyway, Public renderer technology or Party canonical data.

## 4. Ownership correction

Earlier EU-50 planning included Page/PageGroup and historical ListItem candidates as migration units. Current Product/Repository Authority supersedes that boundary:

```text
Article → Historical Migration / EU-50
Page → JilinJobs Site Package
Main stable ListItem → JilinJobs Site Package
```

EU-50 may retain Page/List bytes in source artifacts because they were already discovered by the bounded probe, but it must never promote them through Main migration eligibility.

## 5. Current implementation facts

- Site Package v1 already loads/reconciles Page `bodyHtml` as stable structure.
- Site Package v1 owns CmsList definitions.
- Site Package v1 has no `list-items` structure type/stable ListItem reconcile path.
- current Main bootstrap creates six ListItems once, after which they become ordinary operator-managed Runtime data.

The last point is now a Site Package capability gap, not permission to keep Main ListItems in Historical Migration.

## 6. Source/retry evidence contract

The current collection model is:

1. one bounded full source pass;
2. preserve all successful records and raw issues;
3. classify a targeted retry queue;
4. retry only prior transient targets under the finite three-iteration total budget;
5. close the budget with attempt-3 evidence;
6. build Article-only eligibility from the frozen evidence chain.

No new automatic retry is authorized after the recorded closure unless new evidence/authority reopens it.

## 7. Error policy

### Confirmed source resource missing

Only HTTP 404/410 establishes `SOURCE_RESOURCE_MISSING`.

An INTERNAL Article can be temporarily excluded only when every blocking issue is this class. It must remain separately listed as `EXCLUDE_PENDING_CLIENT_CONFIRMATION`.

### All other errors

Transport failures, unsupported HTML/attributes/resources, redirect/type/scheme anomalies, retry exhaustion and any other newly discovered issue remain explicit classifications. They are not silently repaired or discarded.

Approved non-blocking cases remain recorded as evidence.

### Page/List errors

Page/List errors are preserved in the Site Package handoff and do not become migration decisions. They do not block Article eligibility solely because Site Package follow-up remains pending.

## 8. Current promotion tooling

`data-migrations/tools/main-import-eligibility.mjs` is the current Article promotion-boundary classifier.

Expected outputs:

- `import-eligibility.json`;
- `import-eligible-index.json` — Articles only;
- `import-withheld.json`;
- `source-defect-articles.json`;
- `site-package-handoff.json`;
- matching Markdown summaries.

The prior mixed destructive `main-source-triage.mjs` is retired/fail-closed because it deleted blocked Page/List candidates and mixed their state into `promotionReady`.

## 9. Article promotion Gate

Article arithmetic must close:

```text
total Articles
= eligible Articles
+ SOURCE_RESOURCE_MISSING exclusions pending client confirmation
+ human-review withheld Articles
```

Promotion is not Ready while any human-review Article or unscoped blocking migration observation remains.

Page/List handoff counts/problems are reported independently.

## 10. Verification Strategy

Before EU-50 integration prove:

1. exact closed retry provenance is valid;
2. Article-only eligibility workflow PASS;
3. eligible index contains no Page/List units;
4. Article arithmetic closes;
5. source-defect exclusions remain separately listed;
6. other Article classifications remain explicit;
7. Page/List handoff preserves all discovered candidate refs and problems;
8. old destructive mixed triage cannot be used for current promotion;
9. PR/CI has no unresolved correctness regression;
10. no Runtime import / EU-51 execution occurred.

If Article blockers remain after this non-human work, EU-50 stays Draft/open at a Human Review Gate rather than inventing classifications.

## 11. Site Package follow-up finding

EU-50 hands off, but does not implement:

- accepted Page content/resource projection into Site Package;
- stable Main ListItem identity/reconcile capability;
- migration/adoption of current bootstrap ListItems into any future stable membership model.

These require separate Planning/Readiness under Site Package Authority (Issue #77 / current project authority).

## 12. Downstream

EU-51 remains blocked until an accepted **Article-only** snapshot is integrated and a Fresh Context re-runs its readiness check. Site Package Page/List work is a separate branch of work and does not gain Execute Authority from EU-50.
