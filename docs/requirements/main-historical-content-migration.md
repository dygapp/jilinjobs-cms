# Main Historical Content Collection & Canonical Migration Requirement

## Status

- Parent Planning Authority: GitHub Issue #60 / E3
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Requirement: **READY / ACTIVE**
- Specification: **READY**
- Technical Plan: **READY**
- Current Execution Unit: **EU-50**
- EU-51: **BLOCKED pending accepted snapshot integration + fresh Readiness**
- Current migration scope: **ARTICLE ONLY**
- Ownership correction: **Main Page and stable ListItem content belong to JilinJobs Site Package**

## 1. Intent

E3 collects and promotes Main historical **Article** content from the current Legacy Source into a Consumer-owned, offline-verifiable Canonical Migration Dataset.

E3 is not the ownership home for Main Page content or stable Main ListItem membership. EU-50 may discover those source surfaces because the same Legacy Source exposes them, but their output is Site Package source handoff evidence rather than Historical Migration input.

## 2. Source boundary

Legacy Source access is allowed only during explicit discovery/collection/retry evidence runs.

Current evidence identifies `https://24365.jl.smartedu.cn/` as the reachable Main content source and `cms.jilinjobs.cn` as a redirect/source host. These are source observations, not Runtime contracts.

Stable verification/import must consume frozen repository/evidence bytes and must not require Legacy Source network access.

## 3. Discovery scope

EU-50 must systematically discover:

- every in-scope Main Column pagination branch;
- INTERNAL / EXTERNAL_LINK Article identities and URLs;
- Article body, image and attachment references;
- duplicate/cross-surface Article identities;
- HTTP failures, retry outcomes, unsupported content and unresolved Article classifications.

The bounded source probe may also discover:

- Main Page bodies/resources;
- Main carousel / site-link ListItem membership;
- Page/List duplicates, broken resources or unsupported source shapes.

Those Page/List findings must be preserved in a **Site Package handoff report** and must not enter Article migration eligibility.

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

No collection or normalization path may silently repair, discard or infer away a data problem.

For Article resources:

1. Only explicit HTTP `404` / `410` evidence may be classified `SOURCE_RESOURCE_MISSING`.
2. An INTERNAL Article may be excluded from current import when **every blocking issue** is `SOURCE_RESOURCE_MISSING`.
3. Such Articles must remain in a separate client-confirmation list with identity/title/source/error evidence.
4. Transport/socket/timeouts are not source-missing evidence.
5. Every other unresolved error type remains separately classified and reviewable.
6. Current Human Authority (2026-09-09) defers those problem Articles until the broader current task sequence is complete. They remain durable evidence, are not current import input, and do not block promotion/integration of the clean or explicitly approved subset.
7. Deferral is not repair: no unresolved Article may be guessed, rewritten, silently dropped or imported merely to advance the current subset.
8. Approved non-blocking cases must remain recorded as evidence rather than silently removed.

Page/List errors follow the same no-silent-loss rule but are routed to Site Package handoff instead of Article migration review.

## 6. Accepted current Article snapshot

Raw discovery output is Evidence Candidate only. Accepted migration input must be explicitly promoted.

The Article promotion boundary must freeze, as applicable:

- source root / redirect provenance;
- source collection/retry run/artifact identity and digest;
- complete Article surface/pagination reconciliation;
- current-import / excluded-source-defect / deferred-review Article counts;
- stable Article migration identity and target Column alias;
- source fingerprint / source URL / publish metadata;
- canonical body/resource bytes and SHA-256/size;
- explicit error/human decisions.

Article arithmetic must satisfy:

```text
Article candidates
= current import-eligible Articles
+ source-defect Articles excluded pending client confirmation
+ deferred problem Articles
```

The existence of deferred problem Articles does not block the accepted current subset. Promotion remains blocked only when the current subset cannot be proven complete/deterministic, an unscoped blocking observation could affect Article correctness, or deferred/excluded records are not preserved separately.

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

Only current import-eligible Articles enter the promoted canonical Article tree/index. Deferred problem Articles and source-defect exclusions remain in durable reports/evidence outside the current import index.

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

E3 Runtime import, once a downstream Unit is Ready, uses Generic Content Migration for **the integrated current Article subset only**.

Deferred problem Articles remain outside Runtime import until a later explicit review/decision promotes them.

The fact that Generic Content Migration can technically support Page/List mutations does not authorize Main Page/List migration. Product ownership takes precedence over generic technical capability.

EU-50 itself does not perform Runtime import.

## 10. Verification requirements

Before EU-50 integration, prove at least:

1. complete known Article surface/pagination traversal;
2. INTERNAL / EXTERNAL_LINK ownership classification is deterministic and evidence-backed;
3. bounded retry budget is closed;
4. Article-only arithmetic closes;
5. source-defect Articles are separately retained for client confirmation;
6. deferred problem Articles remain explicitly classified and excluded from current import;
7. Page/List source findings and their errors are preserved in a Site Package handoff artifact;
8. `import-eligible-index.json` contains Articles only;
9. no deferred/source-defect/Page/List record is silently deleted by the promotion path;
10. the promoted repository-owned canonical tree/index contains exactly the current import-eligible subset;
11. stable/offline validation does not contact Legacy Source;
12. no Runtime import or EU-51 execution occurs.

## 11. Non-goals

- no resolution of deferred problem Articles during current EU-50 closure;
- no Main Page migration;
- no Main stable ListItem migration;
- no Site Package ListItem capability implementation inside EU-50;
- no Main data in Flyway;
- no Main-specific Runtime importer;
- no Public frontend technology change;
- no `agentic-dev` baseline update.

## 12. Readiness

The Article discovery/collection/retry boundary, Generic Article canonical contract and stable Column targets are available. Page/List ownership and deferred Article handling no longer block current-subset promotion because they are outside the current import input and remain separately preserved.

This Requirement remains **READY** for EU-50 Article-only execution. EU-51 remains blocked until EU-50 integrates the accepted current Article snapshot and a fresh downstream readiness decision is made.
