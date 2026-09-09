# Historical Content Migration Workspace

`data-migrations/` owns historical content whose lifecycle requires source provenance, canonical fingerprints, offline validation and controlled Runtime import. It is separate from Generic Flyway, JilinJobs Site Package and Runtime databases.

## 1. Long-term boundary

- Backend Flyway → Generic CMS schema/capability only.
- `sites/jilinjobs/structure/**` → stable JilinJobs site structure/content.
- `sites/jilinjobs/bootstrap/**` → truly one-time ordinary Fresh Site defaults only.
- `sites/jilinjobs/assets/**` → stable Site asset source owner.
- `data-migrations/**` → historical migration units/evidence explicitly authorized for a site/scope.
- Runtime DB → imported/operated state, never canonical source authority.

Generic migration capability may support Article/Page/List records, but **product ownership for a concrete scope decides which types belong here**.

## 2. Current Main rule

For Main E3, Historical Migration is **Article-only**:

- INTERNAL Articles;
- EXTERNAL_LINK Articles;
- Article body/resources/attachments;
- source identity/fingerprint/provenance;
- collection/retry/error/reconciliation evidence.

Main Page content and stable Main ListItem membership belong to `sites/jilinjobs/**` Site Package authority.

A Main source collector may still discover Page/List surfaces for completeness, but those records are **Site Package source handoff evidence**. They must not appear in Main import eligibility or be silently discarded.

This Main-specific ownership correction does not retroactively invalidate Party canonical ListItem data or another scope whose accepted authority explicitly treats a list placement as Historical Migration.

## 3. Data flow

```text
Legacy Website / Export / API
        ↓
Raw Evidence Candidate
        ↓
classification / retry / review
        ↓
Canonical Migration Dataset
        ↓
offline validation / reconciliation
        ↓
Generic Content Migration
        ↓
Runtime CMS Data
```

External source access is limited to explicit Collect/Discovery workflows. Stable CI/import must consume frozen repository/evidence bytes.

Actions artifacts/ZIPs are transport/evidence candidates, not long-lived canonical authority by themselves.

## 4. Canonical dataset principles

Maintain one canonical dataset per accepted migration scope. Git history expresses later additions/corrections; do not accumulate permanent full-snapshot ZIP generations.

Every migration unit must have:

- stable migration identity independent of Runtime numeric IDs;
- source system / source URL;
- deterministic source fingerprint;
- stable target identity (for Main Article: Column alias);
- local resource paths where resources must be imported;
- resource size + SHA-256;
- explicit source/error state.

Missing fields are not guessed.

## 5. Article self-contained unit

Article is the Main historical migration unit:

```text
data-migrations/main/v1/
├── manifest.json
├── index.ndjson
├── articles/
│   └── <stable-id>/
│       ├── article.json
│       └── assets/**
├── reports/**
└── source-discovery/**
```

`article.json` owns normalized Article data, source provenance/fingerprint and resource manifest. Body/attachment resources required for local migration remain inside the Article unit and are verified by size/SHA.

The canonical body must not depend on a Legacy Source URL when the resource is supposed to be locally migrated.

## 6. Identity / idempotency

Importer behavior remains:

- unknown migration identity → create;
- same identity + same fingerprint → SKIP/idempotent;
- same identity + different fingerprint or invalid target/precondition → CONFLICT/no silent overwrite.

Deletion or disappearance from a later source scan is not automatically a Runtime delete.

## 7. Error policy

Migration tooling is fail-closed and evidence-preserving.

For current Main:

- HTTP 404/410 alone may establish `SOURCE_RESOURCE_MISSING`;
- INTERNAL Article with only that blocking class may be excluded pending client confirmation and must remain separately listed;
- transport/socket/timeout is not inferred missing;
- unsupported HTML/attributes/media/schemes/redirects and every new error class remain explicit review classifications unless Authority accepts another disposition;
- current Human Authority defers unresolved/problem Articles until later separate handling; they remain durable evidence, are not current import input, and do not block the current project sequence;
- approved non-blocking exceptions remain recorded as evidence;
- no silent repair/drop/discard.

Page/List problems found by the Main collector are written to the Site Package handoff rather than migration withholding.

## 8. Page/List capability note

The Generic migration engine may still contain site-neutral Page/List support because earlier/other consumers use it. That technical capability does not make Main Page/List data migration-owned.

For Main:

- Page stable content already has Site Package representation/reconcile capability;
- stable Main ListItem membership requires a separate Site Package capability/ownership Unit;
- Historical Migration must not be used as a temporary fallback for that missing capability.

## 9. Collect / Review / Stable CI

### Collect
May contact Legacy Source and emit full source evidence.

### Retry / Classification
May retry only explicitly authorized transient targets under a finite budget; all terminal classifications remain recorded.

### Review / Eligibility
Uses frozen source/retry evidence. For Main, `import-eligible-index.json` contains Articles only and a separate `site-package-handoff.json` preserves Page/List findings.

### Stable CI / downstream import
Must not contact Legacy Source and must verify canonical bytes, fingerprints/resources, first import, second-import idempotency, reconciliation and accepted conflict behavior.

## 10. Party compatibility

Party was the first real Canonical Migration consumer and historically includes accepted carousel/ListItem data. Those accepted Party semantics remain governed by Party authority and compatibility evidence.

Do not generalize Party list ownership into Main. Likewise, do not rewrite Party merely because Main now classifies stable list membership as Site Package content.

## 11. Main current state after EU-50

EU-50 is **COMPLETED** and its Execute Authority is **TERMINATED**.

The integrated current Main canonical dataset under `main/v1/**` contains:

- 3078 current import-eligible Articles = 1577 INTERNAL + 1501 EXTERNAL_LINK;
- 2603 local resource files / 450,273,166 bytes;
- dataset digest `sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`.

Separately preserved evidence contains:

- 6 source-defect Articles excluded pending client confirmation;
- 230 deferred problem Articles for later separate review;
- Page/List Site Package source handoff.

Those excluded/deferred records are not current import input. EU-50 did not perform Runtime Main import.

Current Ready Execution Unit is **NONE**. EU-51 remains a Candidate whose accepted-snapshot dependency is satisfied but whose downstream `readiness-check` has not yet run after EU-50 integration. No Runtime import may start merely because the canonical dataset now exists. Main Page/List follow-up remains a separate Site Package Planning/Readiness path.

The old mixed destructive Main triage path remains retired because it could delete Page/List evidence and mix ownership domains.
