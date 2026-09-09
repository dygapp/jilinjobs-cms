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
- EU-51 — Main Article Import, Runtime Reconciliation & Human Review: **READY / Readiness PASS / Execute baseline PENDING**
- Current Ready Execution Unit: **EU-51**

## 1. Pipeline

```text
Legacy Main Source
→ bounded discovery / collection / retry                 [EU-50 COMPLETED]
→ full source Evidence Candidate                         [EU-50 COMPLETED]
→ Article-only eligibility + explicit deferred/error classification [EU-50 COMPLETED]
→ Page/List Site Package handoff                         [EU-50 COMPLETED]
→ accepted current Article subset promotion              [EU-50 COMPLETED / INTEGRATED]
→ Generic Article migration                              [EU-51 READY / Execute baseline PENDING]
```

Only source-discovery stages may access Legacy Source. Downstream verification/import must use repository-owned canonical bytes.

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
- were not used to silently modify `sites/jilinjobs/**` during EU-50;
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

## 10. Verification closure / downstream contract

### EU-50 final exact-head evidence

Final Head `8c2fdd6cdbbd4d1faf865d0ba4ca0f40a0096e84`:

- EU-50 Main Source Discovery #64 / run `34352898345` — **PASS**；
- EU-50 Main Import Eligibility #30 / run `34352898410` — **PASS**；
- Canonical Migration Verification #256 / run `34352898301` — **PASS**；
- Generic Content Migration Verification #51 / run `34352898313` — **PASS**；
- EU-30 Migration Upgrade Verification #206 / run `34352898498` — **PASS**；
- CI #944 / run `34352898338` — **PASS**；
- Review Environment #830 / run `34352898415` — **PASS**。

### Integrated-main evidence

Integrated snapshot `main@05dfa604ccde45c8409cf6a456e4f201534dc602`:

- Generic Content Migration Verification #52 / run `34354290017` — **PASS**；
- CI #945 / run `34354289981` — **PASS**，包含 Backend / Admin / Public / Integrated Browser。

EU-50 acceptance obligations are closed.

EU-51 downstream Readiness on `main@fd192460cb481645c1f1af435cbe5451145797d9` additionally confirms:

- all ten Main target Column aliases are stable/enabled Site Package Columns；
- Generic Article migration already provides fail-closed preflight/apply, stable mapping and same-input idempotency；
- Site Package provisioning and Generic import entry points already exist；
- Runtime verification can be composed without a Main-specific migration engine。

EU-51 execution must use the explicit Fresh Runtime chain:

```text
Flyway
→ JilinJobs Site Package stable provisioning
→ Generic Main canonical first import
→ Runtime reconciliation
→ second identical import / idempotency
→ Public/Admin/Integrated Browser
→ bounded Human Review
```

## 11. Slice sequencing

1. **EU-50 — Main Source Discovery & Article Snapshot Promotion**
   - **COMPLETED**；
   - accepted current Article subset integrated；
   - Execute Authority **TERMINATED**。
2. **EU-51 — Main Article Import, Runtime Reconciliation & Human Review**
   - **READY / Readiness PASS**；
   - Readiness baseline `main@fd192460cb481645c1f1af435cbe5451145797d9`；
   - Current Work `docs/work/current/eu51-main-canonical-import-runtime-review.md`；
   - Execute baseline **PENDING until Planning/Readiness integration + Fresh Context recovery**。
3. **Deferred problem Article review**
   - later explicit review/decision work；
   - not EU-51 import input and not a blocker to current progression。
4. **Site Package Page/List follow-up**
   - separate planning gate；
   - owns accepted Page content and stable Main ListItem capability/content；
   - does not inherit EU-50/EU-51 authority。

No Unit inherits Execute Authority from another. EU-51 Readiness PASS does not authorize Runtime import on the Planning/Readiness branch.
