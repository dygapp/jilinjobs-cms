# Main Historical Content Collection & Canonical Migration Specification

## Authority

- GitHub Issue #60 / E3
- `docs/project/main-site-formal-content-plan.md`
- `docs/requirements/main-historical-content-migration.md`
- `docs/requirements/main-external-link-boundary.md`
- `docs/requirements/cms-site-package-boundary.md`
- `data-migrations/README.md`

## Status

- Specification: **READY / ACTIVE**
- Current migration scope: **ARTICLE ONLY**
- EU-50 — Main Source Discovery & Article Snapshot Promotion: **COMPLETED / Execute Authority TERMINATED**
- EU-51 — Main Article Import, Runtime Reconciliation & Human Review: **COMPLETED / Execute Authority TERMINATED**
- Current Ready Execution Unit: **NONE**

## 1. Pipeline

```text
Legacy Main Source
→ bounded discovery / collection / retry                 [EU-50 COMPLETED]
→ full source Evidence Candidate                         [EU-50 COMPLETED]
→ Article-only eligibility + explicit deferred/error classification [EU-50 COMPLETED]
→ Page/List Site Package handoff                         [EU-50 COMPLETED]
→ accepted current Article subset promotion              [EU-50 COMPLETED / INTEGRATED]
→ Generic Article migration                              [EU-51 COMPLETED]
→ Runtime reconciliation + same-input idempotency        [EU-51 COMPLETED]
→ Public/Admin/Integrated Browser + bounded Human Review  [EU-51 COMPLETED]
```

Only source-discovery stages may access Legacy Source. Downstream verification/import uses repository-owned canonical bytes.

## 2. Discovery output

The EU-50 discovery/retry evidence chain records:

- source root and redirect observations;
- traversed Article surfaces and complete pagination;
- INTERNAL / EXTERNAL_LINK Article identities;
- source URLs and target Column aliases;
- body/resource references and collection results;
- retry budget/outcomes;
- duplicate/cross-surface evidence;
- every error classification.

Page/List surfaces were collected in the same bounded source pass for completeness, but they are tagged as Site Package handoff evidence and never become Main migration import units.

## 3. Article canonical root

The integrated Main Historical Migration dataset is Article-oriented:

```text
data-migrations/main/v1/
├── manifest.json
├── index.ndjson
├── articles/<stable-id>/article.json
├── articles/<stable-id>/assets/**
├── reports/**
└── source-discovery/**
```

Only current import-eligible Articles are present in `index.ndjson` / `articles/**`. Deferred problem Articles and source-defect exclusions remain in durable reports/evidence and are not current import units.

No Main `pages/**` or `lists/**` import units are accepted under the current ownership boundary.

## 4. Article mapping

Each accepted Column content record maps to Generic Article semantics:

- INTERNAL → local canonical body/resources + stable Column alias;
- EXTERNAL_LINK → stable Column alias + externalUrl; external target body is not scraped into local content;
- source identity/provenance/publish metadata are retained when available;
- source fingerprint is deterministic from accepted canonical fields;
- resources use local migration-relative paths, size and SHA-256 where collection is required.

Classification is based on the accepted E1 ownership rules and must fail closed when a source reference cannot be safely attributed.

## 5. Page/List Site Package handoff

The source pass emits Page/List evidence under this contract:

```text
Site Package handoff
├── Page source candidates + resources + problems
├── Main stable ListItem candidates + resources + problems
└── source-only observations / duplicates / unresolved classifications
```

Handoff records:

- are not listed in `import-eligible-index.json`;
- do not become `data-migrations/main/v1/pages/**` or `lists/**` import units;
- preserve all source errors/observations;
- were not used to silently modify `sites/jilinjobs/**` during EU-50/EU-51;
- may later be consumed only by a separately authorized Site Package planning/execution unit.

Current capability facts:

- Page `bodyHtml` already has stable Site Package structure/reconcile support;
- stable ListItem membership does not yet have a Site Package v1 structure type/stable reconcile path.

## 6. Error classification and deferral

The classification boundary is fail-closed with respect to current import membership: unresolved/problem Articles never become import input by inference.

### Source resource missing

`SOURCE_RESOURCE_MISSING` requires explicit HTTP 404/410 evidence.

An INTERNAL Article may move to `EXCLUDE_PENDING_CLIENT_CONFIRMATION` only if every blocking issue on that Article is this classification. The Article remains separately listed with source/error evidence.

### Other errors

- transport/socket/timeout failures do not imply source missing;
- unsupported HTML/attributes/resource type/scheme/redirect/size/etc. remain distinct classifications;
- retry exhaustion remains explicit deferred review evidence;
- approved non-blocking exceptions remain recorded;
- unknown/unmapped blocking observations are never silently mapped into the current import set.

Current Human Authority defers unresolved/problem Articles until later separate handling. These Articles are excluded from current import and do not block the current project sequence, provided they remain separately and durably recorded.

Page/List problems use the same explicit classifications but are emitted in the Site Package handoff rather than migration import membership.

## 7. Integrated current Article subset

Accepted arithmetic:

```text
3314 total Article candidates
= 3078 current import eligible
+ 6 source-defect excluded pending client confirmation
+ 230 deferred problem Articles
```

Current accepted subset:

- INTERNAL: 1577；
- EXTERNAL_LINK: 1501；
- local resource files: 2603；
- resource bytes: 450,273,166；
- dataset digest: `sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`；
- integrated snapshot main: `05dfa604ccde45c8409cf6a456e4f201534dc602`。

`import-eligible-index.json` contains an `articles` collection only. The 6 source-defect and 230 deferred records remain outside the current canonical Article tree/index and remain durable evidence.

## 8. Retry / determinism

The completed EU-50 collector/retry path:

- used explicit bounded source configuration;
- never re-ran unrelated successful records during targeted retry;
- honored the finite per-target retry budget;
- recorded final retry evidence;
- never inferred 404/410 from transport failure;
- preserved frozen successful collection bytes when retrying only failures.

The accepted promotion is deterministic from the frozen evidence chain. Exact-head replay returned `promotion_changed=false` and the same dataset digest.

## 9. Promotion / drift

Accepted current Article subset promotion is an explicit repository-owned dataset change. It freezes only the eligible Article units and their referenced local resources, retains deterministic provenance/integrity, and keeps deferred/source-defect records outside the current import index.

Later source drift or later approval of deferred Articles must produce an evidence-backed diff and must not silently overwrite accepted canonical fingerprints.

Page/List source drift is handled under Site Package follow-up authority, not by changing Main migration semantics.

## 10. Verification closure

### EU-50

Final Head `8c2fdd6cdbbd4d1faf865d0ba4ca0f40a0096e84` passed EU-50 Main Source Discovery #64、EU-50 Main Import Eligibility #30、Canonical Migration #256、Generic Content Migration #51、EU-30 Upgrade #206、CI #944 and Review Environment #830. Integrated snapshot `main@05dfa604ccde45c8409cf6a456e4f201534dc602` additionally passed Generic Content Migration #52 and CI #945.

### EU-51

EU-51 independently established Execute baseline `main@04090a3cc8dd9c85112488f036c4bc1a67003594` and executed this Fresh Runtime chain:

```text
Flyway
→ JilinJobs Site Package stable provisioning
→ Generic Main canonical first import
→ Runtime reconciliation
→ second identical import / idempotency
→ Public/Admin/Integrated Browser
→ bounded Human Review
```

Final exact Head `5adae340edcf605e36779c831fb512c31342eec8` proved:

- first import: 3078 CREATED / 0 conflict / invalid;
- 3078 stable mappings and ten target Column counts reconcile one-to-one;
- all 2603 resources / 450,273,166 bytes reconcile by size/SHA-256 and Runtime references;
- INTERNAL / EXTERNAL_LINK behavior remains exact;
- second import: 3078 SKIPPED / 0 CREATED / conflict / invalid with no resource/mapping growth;
- no Page/List mapping enters the Runtime import;
- imported-content Public/Admin/Integrated Browser passes;
- bounded Human Review returns **PASS** with no accepted-subset correctness blocker.

Exact-head evidence:

- EU-51 Main Canonical Runtime Verification #8 / run `34414803518` — **PASS**；
- EU-51 Main Imported Browser Verification #4 / run `34414803509` — **PASS**；
- Canonical Migration Verification #264 / run `34414803507` — **PASS**；
- EU-30 Migration Upgrade Verification #214 / run `34414803594` — **PASS**；
- CI #957 / run `34414803593` — **PASS**；
- Standard Review Environment #840 / run `34414803462` — **PASS**；
- Human Review — **PASS**；
- Browser artifact `10128741919`, digest `sha256:83df3a2d37634e30403eba4777c5554c71723deca82f08ffad71330e1a6ca070`。

PR #122 squash integrated to `main@fad4bfc17b762ec9612cf1e44a1c0af67e74a307`. Post-Integration CI #958 / run `34417382337` — **PASS**, including Backend / Admin / Public / Integrated Browser.

## 11. Slice sequencing / current state

1. **EU-50 — Main Source Discovery & Article Snapshot Promotion** — **COMPLETED / Execute Authority TERMINATED**；completed evidence：`docs/work/archive/eu50-main-source-discovery-promotion.md`。
2. **EU-51 — Main Article Import, Runtime Reconciliation & Human Review** — **COMPLETED / Execute Authority TERMINATED**；completed evidence：`docs/work/archive/eu51-main-canonical-import-runtime-review.md`。
3. **Deferred problem Article review** — later explicit review/decision work；not inherited from EU-51 and not a blocker to current progression。
4. **Site Package Page/List follow-up** — separate planning gate；owns accepted Page content and stable Main ListItem capability/content；does not inherit EU-50/EU-51 authority。

Current Ready Execution Unit is **NONE**. Any successor must be formed and pass Readiness from current Repository Authority; no Unit inherits Execute Authority from another.