# Main Historical Content Collection & Canonical Migration Requirement

## Status

- Parent Planning Authority: GitHub Issue #60 / E3
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Requirement: **READY / ACTIVE**
- Specification: **READY**
- Technical Plan: **READY**
- EU-50 — Main Source Discovery & Article Snapshot Promotion: **COMPLETED / Execute Authority TERMINATED**
- EU-51 — Main Article Import, Runtime Reconciliation & Human Review: **COMPLETED / Execute Authority TERMINATED**
- Current Ready Execution Unit: **NONE**
- Current migration scope: **ARTICLE ONLY**
- Ownership correction: **Main Page and stable ListItem content belong to JilinJobs Site Package**

## 1. Intent

E3 collects and promotes Main historical **Article** content from the current Legacy Source into a Consumer-owned, offline-verifiable Canonical Migration Dataset, then allows a separately authorized downstream Unit to import/reconcile the accepted dataset.

The current accepted Article sequence has completed through EU-50 source promotion and EU-51 Runtime import/reconciliation/Human Review. This Requirement remains current for later evidence-backed correction or extension, but no active Execution Unit exists after EU-51 closure.

E3 is not the ownership home for Main Page content or stable Main ListItem membership. EU-50 discovered those source surfaces because the same Legacy Source exposes them, but their output is Site Package source handoff evidence rather than Historical Migration input.

## 2. Source boundary

Legacy Source access is allowed only during explicit discovery/collection/retry evidence runs.

EU-50 evidence identified `https://24365.jl.smartedu.cn/` as the reachable Main content source and `cms.jilinjobs.cn` as a redirect/source host. These are source observations, not Runtime contracts.

Stable verification/import consumes frozen repository-owned bytes and must not require Legacy Source network access. EU-51 satisfied this requirement without contacting Legacy Source.

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

EU-50 promoted and integrated the accepted **current Article subset** under `data-migrations/main/v1/**`.

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

## 9. Runtime import boundary and completed application

Runtime import is authorized only through a downstream Execution Unit that has passed Readiness and independently established Execute Authority from integrated current Authority.

EU-51 satisfied that lifecycle independently:

- Readiness baseline: `main@fd192460cb481645c1f1af435cbe5451145797d9`;
- Execute baseline: `main@04090a3cc8dd9c85112488f036c4bc1a67003594`;
- implementation PR #122 final Head: `5adae340edcf605e36779c831fb512c31342eec8`;
- implementation integrated main: `fad4bfc17b762ec9612cf1e44a1c0af67e74a307`.

EU-51 imported only the integrated 3078-Article accepted current subset through the existing Generic Content Migration capability after JilinJobs Site Package provisioning. It proved first-import correctness, stable mapping/resource reconciliation, same-input idempotency, Public/Admin/Integrated Browser behavior and bounded Human Review.

The fact that Generic Content Migration can technically support Page/List mutations does not authorize Main Page/List migration. Product ownership takes precedence over generic technical capability.

## 10. Verification / acceptance closure

EU-50 acceptance proved:

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

EU-51 acceptance additionally proved on exact Head `5adae340edcf605e36779c831fb512c31342eec8`:

1. all ten canonical target Column aliases are present/enabled after Site Package provisioning;
2. first Generic import = 3078 CREATED, 0 conflict / invalid;
3. 3078 stable mappings reconcile one-to-one with canonical identities/fingerprints;
4. Runtime counts reconcile to 1577 INTERNAL + 1501 EXTERNAL_LINK and all ten target Columns;
5. all 2603 resource files / 450,273,166 bytes reconcile to canonical size/SHA-256 and Runtime references;
6. EXTERNAL_LINK semantics remain external and no local content is inferred;
7. second identical import = 3078 SKIPPED, 0 CREATED / conflict / invalid, with no resource/mapping growth;
8. no Page/List migration mapping is produced;
9. Public/Admin/Integrated Browser verification passes against actual imported Main content;
10. bounded Human Review returns **PASS** with no accepted-subset correctness blocker;
11. no Legacy Source access or Main-specific migration engine is required.

Exact-head workflow evidence:

- EU-51 Main Canonical Runtime Verification #8 / run `34414803518` — **PASS**；
- EU-51 Main Imported Browser Verification #4 / run `34414803509` — **PASS**；
- Canonical Migration Verification #264 / run `34414803507` — **PASS**；
- EU-30 Migration Upgrade Verification #214 / run `34414803594` — **PASS**；
- CI #957 / run `34414803593` — **PASS**；
- Standard Review Environment #840 / run `34414803462` — **PASS**。

Browser evidence artifact `10128741919`, digest `sha256:83df3a2d37634e30403eba4777c5554c71723deca82f08ffad71330e1a6ca070`.

Post-Integration CI #958 / run `34417382337` on `main@fad4bfc17b762ec9612cf1e44a1c0af67e74a307` — **PASS**, including Backend / Admin / Public / Integrated Browser.

Completed Work Evidence:

- EU-50: `docs/work/archive/eu50-main-source-discovery-promotion.md`;
- EU-51: `docs/work/archive/eu51-main-canonical-import-runtime-review.md`.

## 11. Non-goals

- no Main Page migration through Historical Migration;
- no Main stable ListItem migration through Historical Migration;
- no speculative Main-specific Runtime importer;
- no Main data in Flyway;
- no Public frontend technology change;
- no `agentic-dev` baseline update;
- no forced repair of deferred problem Articles in the current task sequence.

## 12. Downstream state

EU-50 and EU-51 are both **COMPLETED** and their Execute Authorities are **TERMINATED**.

Current Ready Execution Unit is **NONE**. No successor inherits E3 execution authority.

The 230 deferred problem Articles and 6 source-defect Articles remain separate later/client-review evidence. Site Package Page/List follow-up remains a separate Planning Gate. Any future work must start from a new Fresh Context Planning/Readiness decision against current Repository Authority and Current Evidence.