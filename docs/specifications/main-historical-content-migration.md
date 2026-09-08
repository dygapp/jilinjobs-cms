# Main Historical Content Collection & Canonical Migration Specification

## Authority

- GitHub Issue #60 / E3
- `docs/requirements/main-historical-content-migration.md`
- `docs/project/main-site-formal-content-plan.md`
- `docs/requirements/main-external-link-boundary.md`
- `docs/requirements/main-single-page-formal-content.md`
- `data-migrations/README.md`

## Status

- Specification: **READY as downstream contract**
- Technical Planning: **REQUIRED before E3 execution slice**
- Upstream implementation dependency: E2 Page migration foundation must be integrated
- Current Ready Execution Unit: **NONE from E3**

## 1. Pipeline

E3 follows:

```text
Legacy Main Source
→ bounded discovery / collection
→ raw evidence candidate
→ completeness reconciliation / classification
→ accepted snapshot promotion
→ Main Canonical Migration Dataset
→ Generic Content Migration
→ Runtime reconciliation
→ Public/Admin verification
→ Human Review
```

Collection and Stable Verification are separate. Only the first stages may access Legacy Source.

## 2. Discovery output

A discovery run must emit machine-readable evidence sufficient to answer:

- which source surfaces were traversed;
- page/pagination coverage;
- discovered content keys and URLs;
- candidate type: INTERNAL Article / EXTERNAL_LINK Article / Page / ListItem / excluded / unresolved;
- discovered resource references;
- duplicates/cross-surface references;
- HTTP/source failure and retry summary;
- completeness totals.

Temporary Actions artifacts are Evidence Candidates only. Long-term accepted evidence must be promoted into repository-owned files or summarized by durable digests/records as Repository Authority requires.

## 3. Canonical Main root

After promotion:

```text
data-migrations/main/v1/
├── manifest.json
├── index.ndjson
├── articles/<stable-id>/article.json
├── articles/<stable-id>/assets/**
├── pages/index.json
├── pages/items/<stable-id>/page.json
├── pages/items/<stable-id>/assets/**
├── lists/<list-code>/index.json
├── lists/<list-code>/items/<stable-id>/item.json
└── reports/**
```

Exact optional report/source-evidence filenames are Technical Planning details. Canonical item schemas stay site-neutral; Main aliases/codes belong to the dataset, not Generic code.

## 4. Article mapping

Each accepted content record from a Main Column maps to the current Generic Article schema.

- INTERNAL: bodyHtml/resources canonicalized and target Column alias recorded;
- EXTERNAL_LINK: target Column alias + externalUrl, no fabricated local body;
- source order/publish date/source URL/provenance retained;
- E1 owns the classification rule.

## 5. Page mapping

Each accepted formal Page source maps to the E2 Generic Page schema.

- target = stable `(groupAlias? + pageAlias)`;
- content does not create Page structure;
- expected target fingerprint is based on accepted pre-import Runtime baseline;
- Page body resources are canonical bytes, not stable Site assets;
- current placeholder/fixed-integration Page is not silently reclassified.

## 6. List mapping

Only source evidence showing historical operational membership is promoted to canonical ListItem records.

Expected current possible targets include, but are not limited to, stable Main list codes already in Site Package such as `HOME_CAROUSEL` and `SITE_LINKS` group lists.

- LINK vs ARTICLE follows E1;
- list order comes from source evidence;
- images/resources include digest evidence;
- bootstrap default rows are not automatically treated as historical source truth.

## 7. Completeness gate

Before snapshot promotion:

```text
discovered unique candidates
= accepted canonical
+ explicitly excluded
+ explicitly deferred/unresolved
```

Promotion to `accepted-canonical` requires unresolved that would affect current product scope to be zero. Any deliberate exclusion/defer must have an evidence-backed reason.

Counts are evidence outputs, not hardcoded Specification constants.

## 8. Collection determinism

Collector must:

- use explicit source root/config;
- traverse every discovered pagination branch until termination according to source evidence;
- preserve raw source identifiers/URLs;
- normalize only fields defined by canonical contracts;
- download referenced resources with size/digest;
- avoid current-time/random values inside item fingerprints except separately recorded collection metadata;
- be rerunnable enough to compare discovery drift.

Collector implementation may be Python/Node/other bounded tooling; no runtime product dependency is implied.

## 9. Promotion and drift

An accepted canonical snapshot is immutable by provenance semantics even though repository files can evolve through Git.

If a later collection changes accepted source:

- produce explicit diff/evidence;
- do not silently rewrite current accepted fingerprint;
- determine whether it is correction, scope extension or new source version;
- any Runtime update semantics beyond current Generic create/skip/conflict and E2 guarded Page first apply require separate authority.

## 10. Import composition

Fresh Runtime verification order:

```text
Generic Flyway schema
→ JilinJobs stable Site Package reconcile
→ optional Fresh bootstrap as scenario requires
→ Main canonical generic-content import
→ Runtime reconciliation
→ Public/Admin verification
```

E3 must not rely on Party command paths.

A mixed Main dataset can contain Article/Page/List units in one Generic preflight so known invalid/conflict blocks mutation before execution.

## 11. Verification matrix

### Discovery
- full known source surface/pagination coverage;
- no duplicate canonical identity;
- accepted/excluded/unresolved arithmetic reconciliation;
- source/resource failures surfaced.

### Canonical
- JSON/schema validity;
- stable identity uniqueness;
- source fingerprint consistency;
- resource path/size/SHA;
- valid stable target aliases/codes/page identities.

### Runtime
- Fresh import expected created/applied totals;
- second import all SKIP;
- source fingerprint tamper CONFLICT;
- target/precondition/resource tamper no unsafe mutation;
- runtime content/resources/list placement reconcile to canonical.

### Public/Admin
- Main homepage/columns/articles/pages/list placements render expected accepted content;
- external links follow E1;
- Page behavior follows E2;
- no Party regression;
- Integrated Browser PASS.

### Human Review
- representative/high-risk content and all exceptional classifications reviewed;
- final accepted snapshot receives explicit Human Review result before closure.

## 12. Slice sequencing

E3 does **not** create a Candidate Execution Unit in the current planning change because its runtime dependency (E2 Page foundation) is not yet integrated and actual source collection technical details should be based on then-current source evidence.

After E2 foundation completion, a Fresh Context should revalidate E3 and normally slice at least:

1. bounded Main source discovery / accepted snapshot promotion;
2. canonical import / reconciliation / Human Review closure;

They may be combined only if actual evidence shows one rollback/verification boundary is safer and small enough.

No E3 identifier is reserved now.
