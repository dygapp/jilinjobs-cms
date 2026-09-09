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
- EU-51: **BLOCKED**
- Page/List ownership: **JilinJobs Site Package**

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
    ├─ eligible Articles
    ├─ SOURCE_RESOURCE_MISSING exclusions pending client confirmation
    └─ human-review withheld Articles
    ↓
accepted Article snapshot promotion

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
└── tools/main-import-eligibility.mjs

.github/workflows/
├── eu50-main-source-discovery.yml
├── eu50-main-targeted-retry.yml
└── eu50-main-import-eligibility.yml
```

EU-50 may not use this ownership correction as authority to redesign `SitePackageProvisioning`, CMS schema, `cms_list_item`, bootstrap adoption, or operator ownership semantics.

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
- transport exhaustion becomes explicit review, not inferred missing;
- successful unrelated records are never recollected just to retry a failed target.

The current retry control records the closed attempt-3 evidence. Once closed, automatic retry is not re-enabled without new authority/evidence.

## 6. Article-only eligibility implementation

`main-import-eligibility.mjs` is the current promotion-boundary classifier.

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

- no blocking issue → eligible Article;
- INTERNAL Article + every blocker is HTTP 404/410 `SOURCE_RESOURCE_MISSING` → excluded pending client confirmation;
- any other blocking Article issue → withheld for human review;
- unknown/unmapped blocking observation that could affect migration → fail closed / promotion not ready.

### 6.2 Site Package handoff outcomes

All Page/List candidate refs remain represented in the handoff. Their errors/observations stay attached or separately listed. They are not copied into `import-eligible-index.json` and do not affect Article arithmetic.

## 7. Retired destructive triage

The earlier `main-source-triage.mjs` mixed Article/Page/List promotion and physically removed blocked Page/List candidates from a candidate tree.

That behavior is now unsafe because Page/List are source handoff evidence. The helper is retired/fail-closed. It must not be used for current accepted-snapshot promotion.

## 8. Article canonical organization

Current accepted migration organization is:

```text
data-migrations/main/v1/
├── manifest.json
├── index.ndjson
├── articles/<stable-id>/article.json
├── articles/<stable-id>/assets/**
├── reports/**
└── source-discovery/**
```

Any Page/List files carried by an ephemeral collection artifact are discovery evidence only. They are not promoted as Main migration units.

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
7. other Article blockers remain distinct/human-reviewable;
8. Page/List handoff contains all discovered candidate refs plus problems/observations;
9. old destructive mixed triage cannot run as current promotion logic;
10. no Runtime mutation or EU-51 execution.

A later accepted Article snapshot promotion must additionally freeze deterministic repository-owned bytes/digests and pass offline canonical verification.

## 11. Side effects / rollback

EU-50 accepted side effects are limited to source tooling/workflows, bounded evidence controls/reports and eventually promoted Article canonical bytes.

No CMS Core schema/provisioning mutation and no persistent Main Runtime import is authorized.

Rollback is the EU-50 repository diff plus expiry/discard of ephemeral Actions artifacts.

## 12. Downstream Gate

After Article blockers are resolved and EU-50 promotes/integrates its accepted Article snapshot:

- EU-51 may be re-evaluated from a Fresh Context for **Article import only**;
- Page/List follow-up remains a separate Site Package planning gate;
- neither downstream path inherits EU-50 Execute Authority.
