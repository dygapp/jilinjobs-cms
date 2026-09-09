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
- Current Execution Unit: **EU-50**
- Migration scope: **ARTICLE ONLY**
- EU-51: **BLOCKED pending accepted snapshot integration + fresh Readiness**
- Page/List ownership: **JilinJobs Site Package**
- Deferred problem Articles: **later review; excluded from current import and non-blocking to current-subset progression**

## 1. Current evidence topology

Current source evidence identifies the reachable Main source primarily as `https://24365.jl.smartedu.cn/`, with `cms.jilinjobs.cn` participating as an accepted source/redirect host. These are collection observations only.

The current technical topology is:

```text
Legacy Main Source
    ↓ explicit external-source workflow only
full bounded source evidence
    ↓
finite targeted retry
    ↓
Article-only eligibility
    ├─ current import-eligible Articles
    ├─ SOURCE_RESOURCE_MISSING exclusions pending client confirmation
    └─ deferred problem Articles (later review, not current import)
    ↓
accepted current Article subset promotion

same source evidence
    ↓
Page/List Site Package handoff
    ↓ separate Site Package Planning/Execute authority
```

## 2. Why Main migration is Article-only

Current repository capabilities show:

- `SitePackagePage` includes `bodyHtml`, `renderMode`, `embedUrl` and is loaded/reconciled from `sites/jilinjobs/structure/pages.json`;
- CmsList definitions are stable Site Package structure;
- Site Package v1 supported structure types do not include `list-items`;
- `cms_list_item` currently has no package stable identity / preset ownership field;
- current Main bootstrap ListItems are applied once and then intentionally become ordinary operator-managed Runtime data.

Therefore forcing Page/List through Generic Content Migration would solve a technical mutation problem by violating the accepted product ownership boundary. EU-50 must instead expose the ListItem capability gap.

## 3. EU-50 implementation surface

Primary implementation/evidence areas:

```text
data-migrations/
├── main/source-surfaces.json
├── main/v1/*control.json
├── tools/main-source-*.mjs
├── tools/main-collection-problem-index.mjs
├── tools/main-targeted-retry.mjs
├── tools/main-import-eligibility.mjs
└── tools/main-promote-accepted-articles.mjs

.github/workflows/
├── eu50-main-source-discovery.yml
├── eu50-main-targeted-retry.yml
└── eu50-main-import-eligibility.yml
```

EU-50 may not use this ownership correction or deferred-Article decision as authority to redesign `SitePackageProvisioning`, CMS schema, `cms_list_item`, bootstrap adoption, operator ownership semantics, or Runtime import.

## 4. Collection contract

The full pass may continue collecting Article/Page/List source evidence because one bounded pass gives useful product-source completeness.

Collection output remains Evidence Candidate and must preserve:

- Article detail/body/resources;
- Page body/resources;
- ListItem membership/resources;
- all raw issue classifications and source observations.

The full-pass artifact is never treated as migration authority merely because it contains Page/List-shaped records.

## 5. Retry contract

Targeted retry is bounded to previously identified transient internal resource failures.

Rules:

- full pass is attempt 1;
- at most two later targeted iterations under the current three-attempt budget;
- one network attempt per target per targeted iteration;
- recognized terminal classifications stop retry;
- HTTP 404/410 alone establishes `SOURCE_RESOURCE_MISSING`;
- transport exhaustion becomes explicit deferred review, not inferred missing;
- successful unrelated records are never recollected just to retry a failed target.

The current retry control records the closed attempt-3 evidence. Once closed, automatic retry is not re-enabled without new authority/evidence.

## 6. Article-only eligibility implementation

`main-import-eligibility.mjs` is the current classification boundary.

It consumes:

- frozen full-pass candidate bytes;
- `collection-problem-index.json`;
- final `targeted-retry-attempt-3.json`;
- repository-owned provenance control.

It emits:

```text
generated-import-eligibility/
├── import-eligibility.json
├── import-eligible-index.json       # Articles only
├── import-withheld.json
├── source-defect-articles.json
├── site-package-handoff.json
├── import-eligibility.md
└── site-package-handoff.md
```

### 6.1 Article outcomes

- no blocking issue → current import-eligible Article;
- INTERNAL Article + every blocker is HTTP 404/410 `SOURCE_RESOURCE_MISSING` → excluded pending client confirmation;
- any other blocking Article issue → deferred problem Article, excluded from current import but non-blocking to current-subset progression;
- unknown/unmapped blocking observation that cannot be attributed to a preserved deferred record → fail closed / current-subset promotion not ready.

Deferral does not change classification and does not authorize repair. The original problem/evidence remains durable.

### 6.2 Site Package handoff outcomes

All Page/List candidate refs remain represented in the handoff. Their errors/observations stay attached or separately listed. They are not copied into `import-eligible-index.json` and do not affect Article arithmetic.

## 7. Retired destructive triage

The earlier `main-source-triage.mjs` mixed Article/Page/List promotion and physically removed blocked Page/List candidates from a candidate tree.

That behavior is now unsafe because Page/List are source handoff evidence. The helper is retired/fail-closed. It must not be used for current accepted-snapshot promotion.

## 8. Accepted current-subset promotion

`main-promote-accepted-articles.mjs` promotes only the frozen current import-eligible Article units from the closed evidence artifact into the Consumer-owned canonical root:

```text
data-migrations/main/v1/
├── manifest.json
├── index.ndjson
├── articles/<stable-id>/article.json
├── articles/<stable-id>/assets/**
├── reports/
│   ├── import-eligibility.json
│   ├── import-withheld.json
│   ├── source-defect-articles.json
│   └── promotion-summary.json
└── source-discovery/
```

Promotion rules:

- consume the exact provenance-controlled closed retry artifact;
- consume the matching current eligibility result;
- copy only Article directories referenced by `import-eligible-index.json`;
- verify every referenced article path exists;
- verify each copied local resource size/SHA-256 against `article.json`;
- generate `index.ndjson` from the eligible index in deterministic stable order;
- generate manifest/report digests deterministically from frozen source/provenance and promoted bytes;
- retain source-defect + deferred problem evidence separately;
- never copy Page/List units into the migration canonical tree;
- never contact Legacy Source;
- be idempotent for the same evidence chain.

The accepted canonical tree may be large; repository promotion is intentional because downstream stable verification/import must not depend on expiring Actions artifacts.

## 9. Site Package capability handoff

### Page

No new Generic capability is required merely to own Page content: Site Package v1 already expresses stable Page content. A later Site Package Unit can decide how accepted Legacy Page source bytes/resources replace current placeholders and how stable Page resources are packaged.

### ListItem

A real capability gap exists. A separate planning unit must decide at least:

- stable ListItem identity;
- Site Package schema/manifest representation;
- relationship to stable CmsList code;
- reconcile/adoption rules;
- operator mutation protection/override semantics;
- transition from current one-time bootstrap defaults;
- upgrade/idempotency/conflict verification.

EU-50 must not invent these semantics.

## 10. Verification

EU-50 current verification requires:

1. eligibility tooling syntax PASS;
2. exact provenance points to the closed final-retry artifact;
3. final retry queue is closed;
4. Article arithmetic closes;
5. `import-eligible-index.json` contains Articles only;
6. source-defect Articles remain separately listed;
7. deferred problem Articles remain distinct, durable and absent from current import/canonical tree;
8. Page/List handoff contains all discovered candidate refs plus problems/observations;
9. old destructive mixed triage cannot run as current promotion logic;
10. repository-owned accepted Article index/tree count equals current import-eligible count;
11. all promoted Article resources satisfy path/size/SHA-256 integrity;
12. canonical verification is offline and deterministic;
13. no Runtime mutation or EU-51 execution.

## 11. Side effects / rollback

EU-50 accepted side effects are limited to source tooling/workflows, bounded evidence controls/reports and promoted current-subset Article canonical bytes.

No CMS Core schema/provisioning mutation and no persistent Main Runtime import is authorized.

Rollback is the EU-50 repository diff plus expiry/discard of ephemeral Actions artifacts. Deferred problem evidence remains independently discoverable and is not deleted as part of rollback/cleanup.

## 12. Downstream Gate

After EU-50 promotes/integrates the accepted current Article subset:

- EU-51 may be re-evaluated from a Fresh Context for **the integrated current Article subset only**;
- deferred problem Articles remain a later explicit review backlog and do not enter EU-51 by default;
- Page/List follow-up remains a separate Site Package planning gate;
- neither downstream path inherits EU-50 Execute Authority.
