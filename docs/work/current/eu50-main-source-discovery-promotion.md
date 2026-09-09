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
- Deferred problem Articles: **not current import input / do not block current progression**
- EU-51: **BLOCKED until accepted Article snapshot integration and fresh Readiness**

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
- separate deferred-review Article evidence;
- Page/List source discovery only as Site Package handoff evidence;
- preservation of Page/List errors/duplicates/unsupported findings without silent repair/drop;
- explicit Actions workflows for external-source collection/retry, offline eligibility and repository-owned promotion.

## 3. Out of scope

- Runtime Main import;
- EU-51 execution;
- resolving/fixing deferred problem Articles during current EU-50 promotion;
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

An INTERNAL Article can be excluded from current import when every blocking issue is this class. It must remain separately listed as `EXCLUDE_PENDING_CLIENT_CONFIRMATION` for later client handling.

### Deferred problem Articles

Transport failures, unsupported HTML/attributes/resources, redirect/type/scheme anomalies, retry exhaustion and any other unresolved Article issue remain explicit classifications. They are not silently repaired or discarded.

Current Human Authority (2026-09-09) explicitly defers those problem Articles until the broader current task sequence is complete:

- they remain in durable problem/review evidence;
- they are **not current import input**;
- they do **not block promotion/integration of the clean or explicitly approved current subset**;
- EU-50 must not guess, rewrite, repair or delete them merely to advance the current subset;
- later handling requires its own explicit review/decision context.

Approved non-blocking cases remain recorded as evidence.

### Page/List errors

Page/List errors are preserved in the Site Package handoff and do not become migration decisions. They do not block Article eligibility solely because Site Package follow-up remains pending.

## 8. Current promotion tooling

`data-migrations/tools/main-import-eligibility.mjs` is the current Article eligibility classifier.

Expected outputs:

- `import-eligibility.json`;
- `import-eligible-index.json` — Articles only;
- `import-withheld.json`;
- `source-defect-articles.json`;
- `site-package-handoff.json`;
- matching Markdown summaries.

`import-withheld.json` is durable deferred evidence, not current import input.

The prior mixed destructive `main-source-triage.mjs` is retired/fail-closed because it deleted blocked Page/List candidates and mixed their state into promotion readiness.

## 9. Current-subset promotion Gate

Article arithmetic must close:

```text
total Articles
= current import-eligible Articles
+ SOURCE_RESOURCE_MISSING exclusions pending client confirmation
+ deferred problem Articles
```

Current-subset promotion is Ready when all of the following hold:

- the arithmetic closes;
- bounded retry is terminal/closed;
- no unscoped blocking migration observation remains;
- current import-eligible Article count is non-zero;
- source-defect and deferred problem Articles remain separately and durably recorded;
- current import index contains Articles only;
- promoted canonical bytes contain only the current import-eligible Article subset.

The existence of deferred problem Articles alone is **not** a promotion or progression blocker.

Page/List handoff counts/problems are reported independently.

## 10. Verification Strategy

Before EU-50 integration prove:

1. exact closed retry provenance is valid;
2. Article-only eligibility workflow PASS;
3. eligible index contains no Page/List units;
4. Article arithmetic closes;
5. source-defect exclusions remain separately listed;
6. deferred Article classifications remain explicit and excluded from current canonical/import input;
7. Page/List handoff preserves all discovered candidate refs and problems;
8. old destructive mixed triage cannot be used for current promotion;
9. repository-owned canonical Article bytes/index contain exactly the eligible subset and deterministic integrity/provenance;
10. PR/CI has no unresolved correctness regression;
11. no Runtime import / EU-51 execution occurred.

## 11. Site Package follow-up finding

EU-50 hands off, but does not implement:

- accepted Page content/resource projection into Site Package;
- stable Main ListItem identity/reconcile capability;
- migration/adoption of current bootstrap ListItems into any future stable membership model.

These require separate Planning/Readiness under Site Package Authority (Issue #77 / current project authority).

## 12. Downstream

Once the accepted **current Article subset** snapshot is integrated, EU-50's accepted-snapshot dependency is satisfied even though deferred problem Articles remain outside the current import set. EU-51 still receives no automatic Execute Authority: a Fresh Context must re-run downstream readiness against the integrated snapshot and current Repository Authority.

Deferred problem Articles remain a later review backlog and must not be silently folded into EU-51 import input. Site Package Page/List work is a separate branch of work and does not gain Execute Authority from EU-50.
