# Main Historical Content Collection & Canonical Migration Requirement

## Status

- Parent Planning Authority: GitHub Issue #60 / E3
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Requirement: **READY / ACTIVE**
- Specification: **READY**
- Technical Plan: **READY**
- EU-50 — Main Source Discovery & Article Snapshot Promotion: **COMPLETED / Execute Authority TERMINATED**
- EU-51 — Main Article Import, Runtime Reconciliation & Human Review: **READY / Readiness PASS / Execute baseline PENDING**
- Current Ready Execution Unit: **EU-51**
- Current migration scope: **ARTICLE ONLY**
- Ownership correction: **Main Page and stable ListItem content belong to JilinJobs Site Package**

## 1. Intent

E3 collects and promotes Main historical **Article** content from the current Legacy Source into a Consumer-owned, offline-verifiable Canonical Migration Dataset, then allows a separately authorized downstream Unit to import/reconcile the accepted dataset.

E3 is not the ownership home for Main Page content or stable Main ListItem membership. EU-50 discovered those source surfaces because the same Legacy Source exposes them, but their output is Site Package source handoff evidence rather than Historical Migration input.

## 2. Source boundary

Legacy Source access is allowed only during explicit discovery/collection/retry evidence runs.

EU-50 evidence identified `https://24365.jl.smartedu.cn/` as the reachable Main content source and `cms.jilinjobs.cn` as a redirect/source host. These are source observations, not Runtime contracts.

Stable verification/import consumes frozen repository-owned bytes and must not require Legacy Source network access.

## 3. Discovery scope

The completed EU-50 source stage systematically discovered:

- every configured in-scope Main Column pagination branch;
- INTERNAL / EXTERNAL_LINK Article identities and URLs;
- Article body, image and attachment references;
- duplicate/cross-surface Article identities;
- HTTP failures, retry outcomes, unsupported content and unresolved Article classifications.

The bounded source probe also discovered Main Page/List source surfaces. Those findings remain preserved in a **Site Package handoff report** and are not Historical Migration import units.

## 4. Ownership / classification contract

### Historical Migration

Only Main Column content classified as:

- `INTERNAL` Article; or
- `EXTERNAL_LINK` Article

is an E3 canonical migration candidate.

### Site Package

The following belong to JilinJobs Site Package:

- stable Page identity and accepted Page content;
- stable CmsList definitions;
- stable Main ListItem membership, including the Main homepage/list link membership established as part of the site product definition;
- stable navigation, site config and stable site assets.

The current Site Package already supports stable Page content via `pages`, but stable ListItem provisioning/reconcile is a known capability gap and requires separate authority.

## 5. Error / deferred-review contract

No collection, normalization, promotion or downstream import path may silently repair, discard or infer away a data problem.

For Article resources:

1. Only explicit HTTP `404` / `410` evidence may be classified `SOURCE_RESOURCE_MISSING`.
2. An INTERNAL Article may be excluded when **every blocking issue** is `SOURCE_RESOURCE_MISSING`.
3. Such Articles remain in a separate client-confirmation list with identity/title/source/error evidence.
4. Transport/socket/timeouts are not source-missing evidence.
5. Every other error type remains separately classified and human-reviewable.
6. Approved non-blocking cases remain recorded as evidence rather than silently removed.
7. Current Human Authority defers problem Articles until later separate handling: deferred records are **not current import input**, do **not block the current project sequence**, and must not be guessed/repaired/deleted to advance the accepted subset.

Page/List errors follow the same no-silent-loss rule but are routed to Site Package handoff instead of Article migration review.

## 6. Accepted current Article snapshot

EU-50 has promoted and integrated the accepted **current Article subset** under `data-migrations/main/v1/**`.

Current accepted facts:

- total Article candidates: **3314**;
- current import-eligible Articles: **3078** = 1577 INTERNAL + 1501 EXTERNAL_LINK;
- source-defect Articles excluded pending client confirmation: **6**;
- deferred problem Articles: **230**;
- resource files: **2603**；resource bytes: **450,273,166**；
- dataset digest: `sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`；
- integrated snapshot main: `05dfa604ccde45c8409cf6a456e4f201534dc602`。

Article arithmetic closes as:

```text
3314 Article candidates
= 3078 current import-eligible Articles
+ 6 source-defect Articles excluded pending client confirmation
+ 230 deferred problem Articles
```

The accepted current canonical dataset contains only the 3078 current import-eligible Articles. The excluded/deferred records remain durable reports/evidence, not hidden losses and not current import input.

## 7. Canonical organization

The accepted Main migration root is Article-oriented:

```text
data-migrations/main/v1/
├── manifest.json
├── index.ndjson
├── articles/**
├── reports/**
└── source-discovery/**
```

Page/List source handoff evidence may be retained in bounded reports/evidence, but `pages/**` and `lists/**` are not Main migration import units under the current authority.

## 8. Stable identity / fingerprint

For migration Articles:

- source identity uses `sourceSystem + legacyKey`;
- INTERNAL identity is content-id based; external identity is URL-fingerprint based;
- target uses stable Column alias, never Runtime numeric ID;
- canonical record includes source fingerprint;
- local resource records include size + SHA-256.

Page/List target identities remain Site Package identities and are only referenced by handoff evidence.

## 9. Runtime import boundary

E3 Runtime import is authorized only through a downstream Execution Unit that has passed Readiness and then established its own Execute baseline from integrated Authority.

EU-51 has now passed Readiness on `main@fd192460cb481645c1f1af435cbe5451145797d9`, but its Execute baseline remains pending until the Planning/Readiness state is integrated and a new Fresh Context verifies current `main`, Authority, Actions and base drift.

EU-51 imports only the integrated 3078-Article accepted current subset. Deferred problem Articles remain outside Runtime import until a later explicit review/decision promotes them.

The fact that Generic Content Migration can technically support Page/List mutations does not authorize Main Page/List migration. Product ownership takes precedence over generic technical capability.

## 10. Verification / acceptance closure

EU-50 acceptance has proved:

1. complete known Article surface/pagination traversal;
2. INTERNAL / EXTERNAL_LINK ownership classification is deterministic and evidence-backed;
3. bounded retry budget is closed;
4. Article-only eligibility arithmetic closes;
5. source-defect Articles are separately retained for client confirmation;
6. deferred Article blockers remain explicitly classified and excluded from current canonical/import input;
7. Page/List source findings and their errors are preserved in a Site Package handoff artifact;
8. `import-eligible-index.json` contains Articles only;
9. no Page/List source finding is silently deleted by the promotion path;
10. stable/offline canonical verification does not contact Legacy Source;
11. no Runtime Main import or EU-51 execution occurred during EU-50.

Final PR Head `8c2fdd6cdbbd4d1faf865d0ba4ca0f40a0096e84` passed EU-50 Source Discovery #64、EU-50 Import Eligibility #30、Canonical Migration #256、Generic Content Migration #51、EU-30 Upgrade #206、CI #944 and Review Environment #830. Integrated main additionally passed Generic Content Migration #52 and CI #945.

EU-51 Readiness additionally proves that all ten canonical target Column aliases exist/enabled in the JilinJobs Site Package and that the existing Generic importer, Site Package provisioning, CI/Review/browser capabilities can satisfy the downstream execution contract without inventing a Main-specific migration engine.

## 11. Non-goals

- no Main Page migration through Historical Migration;
- no Main stable ListItem migration through Historical Migration;
- no speculative Main-specific Runtime importer;
- no Main data in Flyway;
- no Public frontend technology change;
- no `agentic-dev` baseline update;
- no forced repair of deferred problem Articles in the current task sequence.

## 12. Downstream readiness

EU-50 accepted-snapshot dependency is **SATISFIED** and EU-50 Execute Authority is **TERMINATED**.

EU-51 `readiness-check = PASS` on `main@fd192460cb481645c1f1af435cbe5451145797d9` and Current Ready Execution Unit is **EU-51**. Current Work：`docs/work/current/eu51-main-canonical-import-runtime-review.md`。

EU-51 Readiness PASS does not establish Execute baseline on the Planning/Readiness branch. After this state is integrated, a new Fresh Context must establish EU-51's independent Execute baseline before any Runtime Main import. Site Package Page/List follow-up remains a separate Planning Gate. Neither path inherits EU-50 Execute Authority.
