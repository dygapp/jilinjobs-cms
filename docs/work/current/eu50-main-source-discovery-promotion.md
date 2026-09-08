# EU-50 — Main Source Discovery & Accepted Snapshot Promotion

## Status

- Parent: GitHub Issue #60 / E3
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Requirement: `docs/requirements/main-historical-content-migration.md`
- Specification: `docs/specifications/main-historical-content-migration.md`
- Technical Plan: `docs/technical/main-historical-content-migration.md`
- Candidate formed by: `slice-work`
- Identifier: **EU-50**
- Readiness: **PASS**
- Planning baseline: `main@e6fe7674398ad8c29fa7ff1d62eb500754a66cc8`
- Execute baseline: **`main@0c38bb0ca3d962b2b79b6c01d318ee292c680715`**
- Execute state: **IN PROGRESS**
- Execute Authority: **ACTIVE — Fresh Context revalidation passed on integrated Planning/Readiness state**
- Implementation branch: `feature/eu-50-main-source-discovery-promotion`

Fresh-context Execute recovery revalidated the integrated `main`, Open PR state, Post-Integration CI #879, Issue #60 / #77 Current Evidence, E3 Authority and this Work Artifact. No base drift or Authority blocker was found. This Execute Authority is limited to EU-50; it does not authorize EU-51 Runtime import/reconciliation/review.

## 1. Goal

Build the bounded Main legacy-source discovery and evidence-promotion boundary required to turn live external source observations into a complete, reviewable, Consumer-owned accepted canonical migration snapshot.

EU-50 is not the Runtime import/final review unit. It stops when the accepted Main canonical snapshot and durable evidence are integrated and can be verified offline.

## 2. In scope

- current Main source root/redirect discovery and explicit collector configuration;
- complete inventory of in-scope Main source surfaces;
- full pagination traversal according to source evidence;
- INTERNAL / EXTERNAL_LINK Article discovery and normalization candidates;
- formal Page/PageGroup source content candidates;
- historical operational ListItem candidates only where source evidence proves migration ownership;
- body image, attachment and Page resource collection;
- duplicate/cross-surface detection;
- unsupported/unresolved classification evidence;
- HTTP failure/retry/termination evidence;
- deterministic stable identities/fingerprints/resource digests;
- repository-owned accepted snapshot promotion under `data-migrations/main/v1/**`;
- durable completeness/reconciliation/provenance reports;
- an offline stable verifier for promoted bytes/evidence;
- manual external-source collection workflow as needed.

## 3. Out of scope

- importing Main canonical data into Runtime CMS as the product result;
- final Runtime Article/Page/List reconciliation;
- final Public/Admin/Integrated Browser verification of migrated Main content;
- final Human Review closure;
- Main-specific Runtime importer/mutation pipeline;
- changes to Generic Flyway, stable Site structure/bootstrap/assets ownership, Public frontend technology or Party canonical data;
- C1/C2, Issue #57/#59, Repository Split or `agentic-dev` baseline work.

## 4. Source evidence available at Readiness

Fresh Context source recovery on 2026-09-08 established sufficient evidence to execute a bounded collector:

- reachable current source root: `https://24365.jl.smartedu.cn/`;
- `cms.jilinjobs.cn` currently redirects to that source;
- `www.jilinjobs.cn` was not a reliable collection endpoint in the current probe;
- `notice` currently exposes 56 pages / 559 records;
- `jydt` currently exposes 196 pages / 1,957 records;
- observed surfaces contain mixed internal/external content and real formal Page/resource shapes.

These values are discovery observations, not accepted snapshot totals. EU-50 must recompute/reconcile the authoritative accepted counts from its actual collection evidence.

## 5. Stable target dependencies

Dependencies are already available from current Repository Authority:

- Main Column aliases in `sites/jilinjobs/structure/columns.json`;
- `HOME_CAROUSEL` and the three Main `SITE_LINKS` list codes in `sites/jilinjobs/structure/lists.json`;
- stable Main Page identities in `sites/jilinjobs/structure/pages.json`;
- E1 external-link classification Authority;
- E2 Page ownership/migration foundation completed by EU-49;
- site-neutral Article/List/Page canonical contracts and Generic migration validation.

No Runtime database ID is a dependency.

## 6. Execution boundary

Expected implementation surface is bounded to historical migration tooling/data/evidence and its manual collection verification, primarily:

- `data-migrations/package.json`;
- `data-migrations/tools/**`;
- `data-migrations/main/v1/**`;
- focused migration schemas/verifiers only if current Generic schema coverage requires them;
- one explicit manual source-discovery workflow if needed.

If Execute discovers that an in-scope source shape cannot be represented by the accepted Generic Article/List/Page contracts, stop that classification as unresolved and return to Planning rather than adding a speculative Main-specific Runtime path.

## 7. Promotion Gate

Before accepted promotion:

```text
discovered unique candidates
= accepted canonical
+ explicitly excluded
+ explicitly deferred/unresolved
```

Readiness permits Execute to discover the exact counts; it does not permit promotion with unresolved current-scope classifications.

Promotion requires:

- in-scope unresolved = 0;
- every excluded/deferred item has a durable evidence-backed reason;
- stable identities and targets are unique/valid;
- item/source fingerprints are deterministic;
- resource bytes/size/SHA-256 reconcile;
- source root, redirects, surface inventory, pagination termination and errors/retries are recorded;
- accepted snapshot and reports are repository-owned and offline-verifiable.

## 8. Verification Strategy

EU-50 verification must include:

1. collector/config tests or equivalent deterministic checks;
2. explicit source collection evidence from current Legacy Source;
3. complete known-surface/pagination reconciliation;
4. accepted/excluded/unresolved arithmetic;
5. canonical schema/stable identity/fingerprint/resource integrity checks;
6. offline verifier PASS without Legacy Source access;
7. Generic/Party migration compatibility checks appropriate to any touched shared validation contract;
8. repository CI required by the final diff;
9. PR review with no unresolved threads;
10. diff review for accidental secrets/private data and unbounded source bytes.

No final Main Runtime visual Human Review is claimed by EU-50; that belongs to EU-51.

## 9. Rollback / side effects

EU-50 may read external public Legacy Source and may create temporary GitHub Actions artifacts. Those artifacts remain Evidence Candidates until promoted.

Accepted repository side effects are limited to:

- collector/manual workflow/verifier changes;
- promoted historical migration data/resources/evidence/reports.

EU-50 must not intentionally mutate a persistent Main Runtime CMS database as its product side effect.

## 10. Readiness Check

### Requirement / Specification

- E3 Requirement: READY.
- E3 Specification: READY.
- Required Technical Planning: READY.

### Dependency closure

- E1 classification boundary: satisfied.
- E2 / EU-49 Page foundation: integrated and completed.
- stable Main Column/List/Page targets: present.
- Generic Article/List/Page canonical migration capability: present.
- current reachable Legacy Source evidence: present.
- source exact counts: deliberately an Execute evidence output, not a Readiness blocker.

### Boundedness / verifiability

- one clear external-source boundary;
- one clear accepted-snapshot promotion boundary;
- no Runtime product mutation required for completion;
- offline acceptance evidence is definable before Execute;
- rollback is independent from downstream Runtime import/review.

### Verdict

**PASS — EU-50 is the Current Ready Execution Unit.**

Fresh Context Execute revalidation has now passed on integrated `main@0c38bb0ca3d962b2b79b6c01d318ee292c680715`; this exact commit is the EU-50 Execute baseline.

## 11. Downstream

EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review is a separate Candidate Execution Unit. It has no Execute Authority and cannot pass Readiness until EU-50 integrates the accepted snapshot and exact dataset evidence required by its verification expectations.
