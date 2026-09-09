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
- Current Execution Unit: **EU-50**
- EU-51: **BLOCKED**

## 1. Pipeline

```text
Legacy Main Source
→ bounded discovery / collection / retry
→ full source Evidence Candidate
→ Article-only eligibility + error classification
→ Page/List Site Package handoff
→ accepted Article snapshot promotion
→ Generic Article migration (downstream only)
```

Only source-discovery stages may access Legacy Source.

## 2. Discovery output

A discovery/retry evidence chain must record:

- source root and redirect observations;
- traversed Article surfaces and complete pagination;
- INTERNAL / EXTERNAL_LINK Article identities;
- source URLs and target Column aliases;
- body/resource references and collection results;
- retry budget/outcomes;
- duplicate/cross-surface evidence;
- every error classification.

Page/List surfaces may be collected in the same bounded source pass for completeness, but they are tagged as Site Package handoff evidence and never become Main migration import units.

## 3. Article canonical root

After promotion, the Main Historical Migration dataset is Article-oriented:

```text
data-migrations/main/v1/
├── manifest.json
├── index.ndjson
├── articles/<stable-id>/article.json
├── articles/<stable-id>/assets/**
├── reports/**
└── source-discovery/**
```

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

The source pass may emit Page/List evidence, but the output contract is:

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
- do not silently modify `sites/jilinjobs/**` during EU-50;
- may later be consumed by a separately authorized Site Package planning/execution unit.

Current capability facts:

- Page `bodyHtml` already has stable Site Package structure/reconcile support;
- stable ListItem membership does not yet have a Site Package v1 structure type/stable reconcile path.

## 6. Error classification

The promotion boundary is fail-closed.

### Source resource missing

`SOURCE_RESOURCE_MISSING` requires explicit HTTP 404/410 evidence.

An INTERNAL Article may move to `EXCLUDE_PENDING_CLIENT_CONFIRMATION` only if every blocking issue on that Article is this classification. The Article remains separately listed with source/error evidence.

### Other errors

- transport/socket/timeout failures do not imply source missing;
- unsupported HTML/attributes/resource type/scheme/redirect/size/etc. remain distinct classifications;
- retry exhaustion remains explicit human review;
- approved non-blocking exceptions remain recorded;
- any unknown/unmapped blocking Article observation keeps Article promotion blocked.

Page/List problems use the same explicit classifications but are emitted in the Site Package handoff rather than migration withholding.

## 7. Article eligibility gate

For Articles:

```text
total Article candidates
= eligible
+ source-defect excluded pending client confirmation
+ human-review withheld
```

`import-eligible-index.json` contains an `articles` collection only.

Article promotion is not ready while:

- any human-review Article remains withheld; or
- an unscoped blocking observation could affect Article correctness.

Page/List Site Package problems are visible in handoff evidence but do not change Article arithmetic.

## 8. Retry / determinism

Collector/retry behavior must:

- use explicit bounded source configuration;
- never re-run unrelated successful records during targeted retry;
- honor the finite per-target retry budget;
- record final retry evidence;
- never infer 404/410 from transport failure;
- preserve frozen successful collection bytes when retrying only failures.

## 9. Promotion / drift

Accepted Article promotion is an explicit repository change. Later source drift must produce an evidence-backed diff and must not silently overwrite accepted canonical fingerprints.

Page/List source drift is handled under Site Package follow-up authority, not by changing Main migration semantics.

## 10. Verification matrix

### Source evidence
- all configured Main Article surfaces traversed;
- pagination termination proven;
- final retry queue closed;
- error classes machine-readable.

### Article eligibility
- Article-only arithmetic closes;
- eligible index has Articles only;
- source-defect exclusions are separately listed;
- remaining Article blockers are explicit;
- no unmapped blocking observation is hidden.

### Site Package handoff
- all discovered Page/List candidates are represented;
- Page/List problems/observations are preserved;
- no handoff record is silently deleted by migration triage;
- stable ListItem capability gap is surfaced rather than bypassed.

### Downstream
EU-50 stops before Runtime import, Public/Admin migration verification and final migrated-content Human Review. Those require later authority.

## 11. Slice sequencing

1. **EU-50 — Main Source Discovery & Article Snapshot Promotion**
   - current Execute unit;
   - owns external source evidence, Article eligibility/promotion, Page/List handoff;
   - does not mutate Runtime CMS product data.
2. **EU-51 — Main Article Import, Runtime Reconciliation & Human Review**
   - downstream candidate only;
   - blocked until EU-50 accepted Article snapshot integration and a fresh readiness decision.
3. **Site Package Page/List follow-up**
   - separate planning gate;
   - owns accepted Page content and stable Main ListItem capability/content;
   - not automatically an EU-50/EU-51 subtask.

No Unit inherits Execute Authority from another.
