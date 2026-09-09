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
- EU-51 — Main Article Import, Runtime Reconciliation & Human Review: **Candidate / NOT READY / no Execute Authority**
- Current Ready Execution Unit: **NONE**
- Page/List ownership: **JilinJobs Site Package**
- Deferred problem Articles: **later review; excluded from current import and non-blocking to current progression**

## 1. Integrated evidence topology

EU-50 source evidence identified the reachable Main source primarily as `https://24365.jl.smartedu.cn/`, with `cms.jilinjobs.cn` participating as an accepted source/redirect host. These remain collection observations only.

The completed source/promotion topology is:

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

same source evidence
    ↓
Page/List Site Package handoff
    ↓ separate Site Package Planning/Readiness authority
```

The accepted canonical root is now `data-migrations/main/v1/**`; downstream stable verification/import must consume repository-owned bytes and must not depend on expiring Actions artifacts or Legacy Source access.

## 2. Why Main migration is Article-only

Current repository capabilities show:

- `SitePackagePage` includes `bodyHtml`, `renderMode`, `embedUrl` and is loaded/reconciled from `sites/jilinjobs/structure/pages.json`;
- CmsList definitions are stable Site Package structure;
- Site Package v1 supported structure types do not include `list-items`;
- `cms_list_item` currently has no package stable identity / preset ownership field;
- current Main bootstrap ListItems are applied once and then intentionally become ordinary operator-managed Runtime data.

Therefore Page/List must not be forced through Generic Content Migration merely because the application can technically mutate those Runtime types. EU-50 correctly preserved Page/List source findings as Site Package handoff rather than Historical Migration import units.

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
- integrated main: `05dfa604ccde45c8409cf6a456e4f201534dc602`.

Promotion rules remain valid for future evidence-backed extension:

- consume exact provenance-controlled evidence;
- copy only eligible Article units and referenced resources;
- verify Article paths and every local resource size/SHA-256;
- use deterministic stable index ordering and dataset digest;
- preserve source-defect/deferred evidence separately;
- never copy Page/List units into the migration canonical tree;
- never contact Legacy Source during promotion/verification;
- same evidence chain must be idempotent.

Final exact-head replay proved `promotion_changed=false` with the same dataset digest.

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

Neither path inherits EU-50 Execute Authority.

## 9. Verification closure

Final EU-50 Head `8c2fdd6cdbbd4d1faf865d0ba4ca0f40a0096e84` passed:

- EU-50 Main Source Discovery #64 / run `34352898345`;
- EU-50 Main Import Eligibility #30 / run `34352898410`;
- Canonical Migration Verification #256 / run `34352898301`;
- Generic Content Migration Verification #51 / run `34352898313`;
- EU-30 Migration Upgrade Verification #206 / run `34352898498`;
- CI #944 / run `34352898338`, including Backend / Admin / Public / Integrated Browser;
- Review Environment #830 / run `34352898415`, including AI/Browser, external route verification, lease lifecycle and cleanup.

Integrated `main@05dfa604ccde45c8409cf6a456e4f201534dc602` additionally passed:

- Generic Content Migration Verification #52 / run `34354290017`;
- CI #945 / run `34354289981`, including Backend / Admin / Public / Integrated Browser.

This closes EU-50 implementation, verification and integration obligations. No Main Runtime import or EU-51 execution occurred.

## 10. Side effects / rollback

EU-50 accepted persistent side effects are limited to source tooling/workflows, evidence controls/reports and repository-owned current-subset Article canonical bytes.

No CMS Core schema/provisioning mutation and no persistent Main Runtime import was introduced.

Future correction or extension must be evidence-backed and preserve provenance/integrity; it must not silently overwrite current accepted fingerprints or discard deferred/source-defect evidence.

## 11. Downstream Gate

EU-50 accepted-snapshot dependency is **SATISFIED** and EU-50 Execute Authority is **TERMINATED**.

Current Ready Execution Unit is **NONE**.

From a Fresh Context:

- EU-51 may be re-evaluated for the **integrated current Article subset only**;
- deferred problem Articles remain later-review evidence and do not enter EU-51 by default;
- Page/List follow-up remains a separate Site Package Planning/Readiness Gate;
- neither downstream path inherits EU-50 Execute Authority;
- no Runtime mutation begins until a new `readiness-check` passes.
