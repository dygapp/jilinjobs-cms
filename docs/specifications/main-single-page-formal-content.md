# Main Single-page Formal Content Specification

## Authority

- GitHub Issue #60 / E2
- `docs/requirements/main-single-page-formal-content.md`
- `docs/project/main-site-formal-content-plan.md`
- `docs/specifications/public-site.md`
- `docs/specifications/cms-site-package-boundary.md`
- `docs/specifications/generic-content-migration-application.md`

## Status

- Specification: **READY**
- Technical Planning: **REQUIRED**
- Current Ready Execution Unit: **NONE until slice-work + readiness-check**

## 1. Page ownership split

E2 freezes Page responsibility as:

```text
Site Package stable Page target
        ↓ create / structural reconcile
Runtime Page content
        ↓ operator-maintainable after create
Canonical Page content migration
        ↓ explicit guarded first apply only
Runtime Page content
        ↓ returns to operator ownership
Public /page/** projection
```

Site Package is not a continuous Page-body deployment mechanism; Canonical Migration is not a continuous reconciler.

## 2. Site Package reconcile behavior

For a missing package-owned Page, first provision continues to create the full current Page row using package values.

For an existing package-owned Page:

- stable identity resolution / preset ownership remains required;
- current package structural reconciliation remains unchanged outside the three E2 content fields;
- `body_html`, `render_mode`, `embed_url` are **preserved from Runtime** and excluded from ordinary update comparison/write;
- changing those three values in a future `sites/jilinjobs/structure/pages.json` therefore changes only future first-provision defaults, not existing Runtime content.

The focused Site Package verifier must add a regression proving an operator change to these fields survives a second provision/restart-style reconcile.

## 3. Canonical Page dataset contract

Generic snapshots may add an optional Page section:

```text
<snapshot-root>/
├── index.ndjson
├── articles/**
├── lists/**
└── pages/
    ├── index.json
    └── items/<stable-id>/page.json
        └── assets/**   # optional
```

A dataset may contain only Page records as long as at least one canonical unit exists overall.

### 3.1 Page index

Page index contains ordered references with at least:

- `legacyKey`;
- `path`;
- `sourceOrder`;
- `sourceFingerprint`.

Source system may be index-level if all records share one source; implementation may also validate item-level agreement.

### 3.2 Page item

Canonical Page item contains:

- source: `system`, `legacyKey`, source/provenance URL;
- target: optional `groupAlias`, required `pageAlias`;
- content: `bodyHtml`, `renderMode`, optional `embedUrl`;
- `sourceFingerprint`;
- `expectedTargetFingerprint` for first-apply guard;
- optional resources referenced from `bodyHtml`.

`expectedTargetFingerprint` is the SHA-256 of a documented canonical serialization of the target's pre-import `bodyHtml + renderMode + embedUrl`. It is a guard, not the historical source fingerprint.

## 4. Stable target resolution

Generic Page import resolves target only by Site-stable identity:

- standalone: `(groupAlias = null, pageAlias)`;
- grouped: `(groupAlias, pageAlias)`.

The target must resolve to exactly one existing Page. Generic migration does not create Page/PageGroup structure and does not depend on Runtime numeric ID in canonical data.

Missing/ambiguous target is INVALID before mutation.

## 5. Mapping contract

Generic CMS schema adds a site-neutral Page migration mapping analogous to Article/List mapping.

Minimum semantics:

```text
(source_system, legacy_key) UNIQUE
→ page_id
→ source_fingerprint
→ source_url / provenance metadata
```

The exact table/record names are technical details, but ownership is Generic CMS migration support, not Main-specific schema.

Classification:

- no mapping + target precondition matches → APPLY candidate;
- same mapping fingerprint → SKIP;
- same identity + different source fingerprint → CONFLICT;
- mapping points to another Page → CONFLICT;
- no mapping + target precondition mismatch → CONFLICT.

There is no default Page UPDATE-on-new-fingerprint policy.

## 6. Execute semantics

The Generic Page plan is built in the same all-known-errors-before-write dataset preflight as Article/List.

For an APPLY candidate:

1. verify target and expected target fingerprint;
2. verify all referenced canonical resource bytes;
3. prepare safe body rewrites;
4. update only `bodyHtml / renderMode / embedUrl` through a Core Page content boundary that preserves current sanitizer/validation semantics;
5. insert Page migration mapping in the same database transaction as Page DB mutation where practical;
6. project deterministic historical static assets only after static preflight and with existing safe-path protections;
7. return a Generic result with Page kind / runtime Page id.

Known dataset INVALID/CONFLICT must prevent unrelated Page/Article/List mutations from starting.

## 7. Operator edit after import

After successful first apply, Runtime content becomes ordinary operator-managed content.

A second run with the same source fingerprint:

- returns SKIP based on accepted mapping;
- does not compare-and-rewrite the current Page body;
- therefore preserves operator edits made after import.

A newer canonical source fingerprint conflicts by default and requires a future explicit upgrade Requirement if the project ever needs controlled Page re-import.

## 8. Page resource contract

Because current Page RICH_TEXT has no Article-style Resource association, E2 does not introduce one.

Canonical Page body resources use repository-frozen bytes + digest evidence and are projected to a site-neutral deterministic historical static namespace, for example an implementation-equivalent of:

```text
/static/migrated/content/pages/<sha256>.<ext>
```

Exact path naming is frozen by Technical Plan. Requirements:

- path must not encode Main/Party policy;
- resource token/reference rewrite must be deterministic;
- path traversal / symlink escape / digest mismatch are INVALID;
- historical Page assets do not enter `sites/jilinjobs/assets/**`;
- mutable `/static/uploads/**` ownership is not taken over.

If later source evidence proves a richer resource lifecycle is required, that is a separate Requirement change.

## 9. Report / command compatibility

Existing `generic-content <snapshot-root>` and root `importCanonicalContent` remain the generic entry. Page support extends the same command/report rather than adding a Main-specific executable.

Generic report adds `PAGE` as a kind while preserving existing totals/statuses and Article/List compatibility.

Party commands/reports remain unchanged.

## 10. Verification obligations

A synthetic site-neutral Page fixture must prove:

- standalone + grouped stable Page target resolution;
- RICH_TEXT first apply;
- optional body resource validation/rewrite;
- second run SKIP;
- post-import operator edit survives same-input rerun;
- wrong expected target fingerprint CONFLICT/no mutation;
- changed source fingerprint CONFLICT/no mutation;
- missing group/page INVALID;
- duplicate source identity / duplicate path INVALID;
- target mapping mismatch CONFLICT;
- ordinary Site Package second reconcile preserves content fields;
- existing Generic Article/List tests and Party compatibility tests remain PASS;
- no HTTP listener / Server dependency leakage into content-migration app.

## 11. Slice boundary

This Specification is intentionally separable from actual Main content acquisition.

The first E2 implementation slice includes only:

- Site Package Page content no-overwrite reconcile;
- Generic Page canonical schema/loader/preflight/apply/mapping/report;
- focused synthetic verification;
- existing regression verification.

It excludes Main page bytes, accepted Main source count, source scraping, Human Review content approval and E3 Article/List migration.

Technical Planning is required because this slice crosses Generic schema, Core Page update boundary, Site Package reconcile, content-migration application and focused verification while preserving independent lifecycle ownership.
