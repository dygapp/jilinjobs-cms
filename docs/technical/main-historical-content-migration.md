# Main Historical Content Collection & Canonical Migration Technical Plan

## Authority

- GitHub Issue #60 / E3
- `docs/project/main-site-formal-content-plan.md`
- `docs/requirements/main-historical-content-migration.md`
- `docs/specifications/main-historical-content-migration.md`
- `docs/requirements/cms-site-package-boundary.md`
- `data-migrations/README.md`

## Status

- Technical Plan: **READY / ACTIVE**
- Migration scope: **ARTICLE ONLY**
- EU-50 — Main Source Discovery & Article Snapshot Promotion: **COMPLETED / Execute Authority TERMINATED**
- EU-51 — Main Article Import, Runtime Reconciliation & Human Review: **COMPLETED / Execute Authority TERMINATED**
- Current Ready Execution Unit: **NONE**
- Page/List ownership: **JilinJobs Site Package**
- Deferred problem Articles: **later review; excluded from completed EU-51 import and non-blocking to current progression**

## 1. Integrated evidence topology

EU-50 source evidence identified the reachable Main source primarily as `https://24365.jl.smartedu.cn/`, with `cms.jilinjobs.cn` participating as an accepted source/redirect host. These remain collection observations only.

The completed source/promotion/runtime topology is:

```text
Legacy Main Source
    ↓ explicit external-source workflow only
full bounded source evidence
    ↓
finite targeted retry — CLOSED
    ↓
Article-only eligibility
    ├─ 3078 current import-eligible Articles
    ├─ 6 SOURCE_RESOURCE_MISSING exclusions pending client confirmation
    └─ 230 deferred problem Articles (later review, not current import)
    ↓
repository-owned accepted current Article subset — INTEGRATED
    ↓
Fresh Runtime Site Package provisioning + Generic Article import
    ↓
reconciliation + idempotency + Browser + Human Review — COMPLETED

same source evidence
    ↓
Page/List Site Package handoff
    ↓ separate Site Package Planning/Readiness authority
```

The accepted canonical root is `data-migrations/main/v1/**`; stable verification/import consumes repository-owned bytes and does not depend on expiring Actions artifacts or Legacy Source access.

## 2. Why Main migration is Article-only

Current repository capabilities show:

- `SitePackagePage` includes `bodyHtml`, `renderMode`, `embedUrl` and is loaded/reconciled from `sites/jilinjobs/structure/pages.json`;
- CmsList definitions are stable Site Package structure;
- Site Package v1 supported structure types do not include `list-items`;
- `cms_list_item` currently has no package stable identity / preset ownership field;
- current Main bootstrap ListItems are applied once and then intentionally become ordinary operator-managed Runtime data.

Therefore Page/List must not be forced through Generic Content Migration merely because the application can technically mutate those Runtime types. EU-50 preserved Page/List source findings as Site Package handoff rather than Historical Migration import units, and EU-51 produced zero Page/List migration mappings.

## 3. EU-50 implementation surface — completed

The completed implementation/evidence surface includes:

```text
data-migrations/
├── main/source-surfaces.json
├── main/v1/*control.json
├── main/v1/manifest.json
├── main/v1/index.ndjson
├── main/v1/articles/**
├── main/v1/reports/**
├── main/v1/source-discovery/**
├── tools/main-source-*.mjs
├── tools/main-collection-problem-index.mjs
├── tools/main-targeted-retry.mjs
├── tools/main-import-eligibility.mjs
├── tools/main-current-subset-readiness.mjs
├── tools/main-promote-accepted-articles.mjs
└── tools/main-verify-accepted-articles.mjs

.github/workflows/
├── eu50-main-source-discovery.yml
├── eu50-main-targeted-retry.yml
└── eu50-main-import-eligibility.yml
```

EU-50 did not redesign `SitePackageProvisioning`, CMS schema, `cms_list_item`, bootstrap adoption or operator ownership semantics, and did not perform Main Runtime import.

## 4. Collection / retry closure

EU-50 used one bounded full source pass plus targeted retry only for previously identified transient targets under a finite three-attempt total budget.

Accepted rules and closed evidence:

- one network attempt per target per targeted iteration;
- recognized terminal classifications stop retry;
- HTTP 404/410 alone establishes `SOURCE_RESOURCE_MISSING`;
- transport exhaustion remains explicit deferred review, never inferred missing;
- unrelated successful records are not recollected solely to retry failures;
- attempt-3 retry evidence is closed with zero retryable transient targets;
- automatic retry is not re-enabled without new authority/evidence.

Frozen provenance:

- source run: `34303771704`;
- source artifact: `10086056781`;
- artifact name: `eu50-main-retry-attempt-3-a96cee22449508f92f3c89789f99aad477286a66`;
- artifact digest: `sha256:66118e4f21bf7644db1c97e2a631eee5d4902410f167606286e1280293620494`;
- source Head: `a96cee22449508f92f3c89789f99aad477286a66`.

## 5. Eligibility / deferral implementation

`main-import-eligibility.mjs` remains the classification boundary. It consumes frozen source/retry evidence and repository-owned provenance control and emits Article-only eligibility, source-defect/deferred reports and Site Package handoff evidence.

Article outcomes:

- no blocking issue → current import-eligible Article;
- INTERNAL + every blocker = HTTP 404/410 `SOURCE_RESOURCE_MISSING` → excluded pending client confirmation;
- any other blocking Article issue → deferred problem Article, excluded from current import;
- unknown/unmapped blocking observation that cannot be attributed to preserved evidence → fail closed.

Current Human Authority defers problem Articles until later separate handling. Deferral never changes their classification and never authorizes repair, guessing or deletion.

All Page/List candidates remain represented in Site Package handoff and do not affect Article import membership arithmetic.

## 6. Integrated accepted current subset

The accepted current subset was promoted from frozen evidence into `data-migrations/main/v1/**` and integrated through PR #117.

Accepted facts:

- total candidates: **3314**;
- current import eligible: **3078** = 1577 INTERNAL + 1501 EXTERNAL_LINK;
- source-defect excluded: **6**;
- deferred problem Articles: **230**;
- local resource files: **2603**;
- local resource bytes: **450,273,166**;
- dataset digest: `sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`;
- integrated snapshot main: `05dfa604ccde45c8409cf6a456e4f201534dc602`.

Promotion rules remain valid for future evidence-backed extension:

- consume exact provenance-controlled evidence;
- copy only eligible Article units and referenced resources;
- verify Article paths and every local resource size/SHA-256;
- use deterministic stable index ordering and dataset digest;
- preserve source-defect/deferred evidence separately;
- never copy Page/List units into the migration canonical tree;
- never contact Legacy Source during promotion/verification;
- same evidence chain must be idempotent.

## 7. Retired destructive triage

The earlier `main-source-triage.mjs` mixed Article/Page/List promotion and physically removed blocked Page/List candidates. That behavior remains retired/fail-closed and must not be revived as current promotion logic.

## 8. Site Package capability handoff

### Page

Site Package v1 already expresses stable Page content. A later Site Package Unit may decide how accepted Legacy Page source bytes/resources replace current placeholders and how stable Page resources are packaged.

### ListItem

A real capability gap remains. A separate planning unit must decide at least:

- stable ListItem identity;
- Site Package schema/manifest representation;
- relationship to stable CmsList code;
- reconcile/adoption rules;
- operator mutation protection/override semantics;
- transition from current one-time bootstrap defaults;
- upgrade/idempotency/conflict verification.

Neither path was an EU-51 prerequisite and neither inherits Historical Migration Execute Authority.

## 9. EU-51 runtime composition — completed

EU-51 reused existing repository capabilities rather than introducing a Main-specific migration engine:

- `provisionSitePackage` reconciles versioned JilinJobs stable structure against an initialized schema；
- `importCanonicalContent` invokes the site-neutral Generic Content Migration entry point；
- Generic preflight resolves Article targets by stable Column alias and fails closed when a target is absent/disabled；
- Generic import validates stable identities, source fingerprints, paths, URLs and resource size/SHA-256；
- Generic mapping/apply supports first-create, same-input SKIP/idempotency and explicit conflict/invalid refusal；
- existing CI / Review Environment / browser infrastructure provides Runtime verification surfaces。

EU-51 preserved this exact order:

```text
Fresh MySQL
→ Generic Flyway V1/V2/V3
→ JilinJobs Site Package stable provisioning
→ Main `data-migrations/main/v1` Generic import
→ Runtime identity/count/resource reconciliation
→ second identical import / idempotency
→ Public/Admin/Integrated Browser verification
→ bounded Human Review
```

All ten canonical Main Article target aliases were verified present/enabled: `notice`, `employment-news`, `recruitment-announcement`, `policy-month`, `policy-outside`, `policy-jilin`, `policy-national`, `typical-grassroots`, `typical-startup`, `typical-military`.

## 10. EU-51 verification closure

EU-51 proved all planned obligations:

1. integrated canonical manifest/dataset digest/counts remained exact before Runtime mutation；
2. import set contained exactly 3078 accepted Articles and excluded 230 deferred + 6 source-defect records；
3. Site Package provisioning established all ten required enabled target Columns；
4. first import reported `total=3078`, `created=3078`, `conflicts=0`, `invalid=0`；
5. second identical import reported `created=0`, `skipped=3078`, `conflicts=0`, `invalid=0`；
6. legacy mappings reconciled one-to-one with imported Article identities；
7. Runtime counts reconciled by target Column / Article type；
8. all 2603 imported local resources / 450,273,166 bytes reconciled to canonical size/SHA-256 and Runtime references；
9. EXTERNAL_LINK records retained external semantics and did not acquire inferred local body/resources；
10. Public/Admin/Integrated Browser verification passed with imported Main content；
11. bounded Human Review covered all target Columns plus representative/high-risk accepted records and returned **PASS**；
12. stable execution performed no Legacy Source access；
13. no accepted-subset defect remained unresolved；
14. Page/List and deferred/source-defect Article work remained outside the EU-51 diff/import set。

## 11. Verification history / integration

EU-51 Readiness baseline: `main@fd192460cb481645c1f1af435cbe5451145797d9`.

EU-51 Execute baseline: `main@04090a3cc8dd9c85112488f036c4bc1a67003594`.

Final implementation Head `5adae340edcf605e36779c831fb512c31342eec8` exact-head evidence:

- EU-51 Main Canonical Runtime Verification #8 / run `34414803518` — **PASS**；
- EU-51 Main Imported Browser Verification #4 / run `34414803509` — **PASS**；
- Canonical Migration Verification #264 / run `34414803507` — **PASS**；
- EU-30 Migration Upgrade Verification #214 / run `34414803594` — **PASS**；
- CI #957 / run `34414803593` — **PASS**；
- Standard Review Environment #840 / run `34414803462` — **PASS**；
- bounded Human Review — **PASS**。

Browser evidence artifact `10128741919`, digest `sha256:83df3a2d37634e30403eba4777c5554c71723deca82f08ffad71330e1a6ca070`.

PR #122 squash integrated to `main@fad4bfc17b762ec9612cf1e44a1c0af67e74a307`.

Post-Integration CI #958 / run `34417382337` on the integration commit — **PASS**, including Backend / Admin / Public / Integrated Browser.

Completed Work Evidence: `docs/work/archive/eu51-main-canonical-import-runtime-review.md`.

## 12. Side effects / rollback

EU-50 persistent side effects remain source tooling/workflows, evidence controls/reports and repository-owned current-subset Article canonical bytes.

EU-51 persistent implementation side effects are limited to verification/orchestration and deterministic review tooling. Runtime verification uses controlled Fresh Runtime instances reproducible from repository-owned bytes; no production Runtime database is stored as repository authority. Generic migration implementation and accepted canonical bytes were not modified by EU-51.

Future canonical correction or extension remains evidence-backed and must not silently overwrite accepted fingerprints or discard deferred/source-defect evidence.

## 13. Downstream Gate

EU-50 and EU-51 are **COMPLETED** and their Execute Authorities are **TERMINATED**.

Current Ready Execution Unit is **NONE**.

Deferred problem Articles remain later-review evidence. Page/List follow-up remains a separate Site Package Planning/Readiness Gate. Any next unit must be formed from current Planning Authority and pass its own Readiness; no successor inherits EU-51 Execute Authority.