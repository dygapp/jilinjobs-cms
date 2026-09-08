# Main Historical Content Collection & Canonical Migration Technical Plan

## Authority

- GitHub Issue #60 / E3
- `docs/requirements/main-historical-content-migration.md`
- `docs/specifications/main-historical-content-migration.md`
- `docs/project/main-site-formal-content-plan.md`
- `data-migrations/README.md`
- Issue #77 four-layer Architecture Authority

## Status

- Technical Plan: **READY**
- Planning baseline: `main@e6fe7674398ad8c29fa7ff1d62eb500754a66cc8`
- Source-evidence recovery date: 2026-09-08
- `slice-work`: **EU-50 + EU-51 formed**
- Current Ready Execution Unit from E3: **EU-50 only**
- EU-51 Readiness: **PENDING / blocked by EU-50 accepted snapshot**

## 1. Current source-evidence recovery

Fresh Context recovery confirmed that the current reachable Main legacy content is primarily served from:

`https://24365.jl.smartedu.cn/`

Current observations also show:

- `cms.jilinjobs.cn` redirects to the reachable source above;
- `www.jilinjobs.cn` was not a reliable collection endpoint in the current probe;
- `typeCode=notice` currently reports 56 pages / 559 records;
- `typeCode=jydt` currently reports 196 pages / 1,957 records;
- source surfaces contain a mixture of INTERNAL detail content and external targets;
- real formal Page content and resource/link-heavy Page surfaces are reachable.

These numbers and endpoints are **source observations only**. They are not accepted migration totals or permanent Runtime contracts. EU-50 must rediscover and reconcile all in-scope Main surfaces before promotion; accepted counts only come from the promoted repository evidence.

## 2. Accepted topology

Keep the accepted four-layer boundary:

```text
Legacy Main Source
        ↓  external network allowed only here
EU-50 bounded discovery / collection
        ↓
Raw Evidence Candidate
        ↓
classification + completeness reconciliation
        ↓
accepted snapshot promotion
        ↓
data-migrations/main/v1/**
        ↓  stable/offline from here
EU-51 Generic Content Migration
        ↓
Runtime reconciliation
        ↓
Public/Admin/Integrated Browser + Human Review
```

No new Runtime application or Main-specific mutation pipeline is introduced.

Generic Runtime migration continues through:

```text
backend/apps/content-migration
        ↓
backend/modules/cms-core
```

The existing `generic-content` command remains the Runtime import entry for Main Article / ListItem / Page records.

## 3. Why E3 is split into two Execution Units

Current evidence proves two materially different rollback / verification boundaries.

### EU-50 — Main Source Discovery & Accepted Snapshot Promotion

Owns the only stage allowed to access the Legacy Source. It establishes a complete, reviewable, repository-owned accepted snapshot.

Rollback is limited to collector / source-evidence / canonical-data / verification files. It does not mutate Runtime CMS data and does not claim final visual acceptance.

### EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review

Consumes only the accepted repository-owned snapshot. It proves Generic import, idempotency, conflict behavior, Runtime reconciliation and final product review without contacting Legacy Source.

EU-51 cannot be Ready before EU-50 integrates because its exact dataset digest, accepted identities/counts, exceptional classifications and resource set do not yet exist as durable Consumer Authority.

Combining the two would couple an unstable external-source boundary to Runtime mutation and final review, weaken rollback clarity, and make stable CI depend on collection timing. Therefore the Specification's normal two-slice structure is retained.

## 4. EU-50 implementation topology

Preferred bounded implementation areas:

```text
data-migrations/
├── package.json                         # add collect:main only as needed
├── tools/
│   └── main-collector.mjs              # bounded Main source collector
└── main/
    └── v1/
        ├── manifest.json
        ├── index.ndjson
        ├── articles/**
        ├── pages/**
        ├── lists/**
        ├── reports/**
        └── source-discovery/**          # only promoted durable evidence

.github/workflows/
└── eu50-main-source-discovery.yml       # manual external-source collection/evidence
```

Exact filenames can vary during Execute if the same ownership and evidence contract is preserved. Do not duplicate Party-specific formats or assumptions.

The existing EU-29 collector/workflow may be used as implementation evidence for HTTP/DOM/resource handling, but EU-50 must not inherit:

- Party aliases;
- Party fixed counts;
- Party-specific commands;
- the old combined "collect + Runtime import" workflow topology.

## 5. Discovery configuration and surface inventory

The collector must use explicit source-root/config rather than embed a permanent production contract in Generic code.

At minimum, the collector must establish and persist a machine-readable source-surface inventory covering every current Main product surface supported by evidence, including:

- Main Column list surfaces and every pagination branch;
- Article/detail surfaces and external targets;
- Main formal Page/PageGroup source surfaces;
- homepage operational memberships such as carousel, only when source evidence supports migration ownership;
- website-link operational memberships, only when they are historical ListItem truth rather than stable Navigation structure;
- body images, attachments and Page body resources;
- duplicate/cross-surface references;
- unsupported/unresolved candidates;
- HTTP failures, retries and terminal collection errors.

Known Site Package stable targets used for classification include current Main Column aliases, `HOME_CAROUSEL`, the three `SITE_LINKS` list codes and existing Page aliases. Target identity remains repository-owned; Legacy URL/router/component names are not target identity.

## 6. Classification boundary

Use E1/E2 Authority; do not invent a second mapping policy.

- Column content → INTERNAL or EXTERNAL_LINK Article.
- Historical operational membership → CmsListItem.
- Stable Navigation structure → excluded from E3 as Site Package ownership.
- Fixed integration → excluded from E3 as engineering ownership.
- Formal source-derived Page body → canonical Page content targeting stable `(groupAlias? + pageAlias)`.
- Current `EMBED_PLACEHOLDER` targets remain placeholders unless separate product evidence authorizes a real integration.
- Ambiguous role → unresolved evidence; never silently promote.

Main-specific aliases/codes/source rules belong to dataset/collector configuration and evidence, never to Generic Runtime migration code.

## 7. Raw Evidence Candidate

Collection output is not automatically accepted canonical data.

A run must produce machine-readable evidence sufficient to reconstruct:

- collection timestamp;
- requested source root and observed redirects/final source root;
- traversed surfaces and pagination termination;
- discovered stable source identities and URLs;
- candidate classifications;
- resource references and download results;
- duplicate/cross-reference decisions;
- failures/retries;
- discovered/accepted/excluded/unresolved arithmetic;
- candidate item/resource fingerprints and digests.

GitHub Actions artifacts may transport this candidate evidence but remain ephemeral. A run/artifact ID is provenance, not long-lived migration authority.

## 8. Promotion contract

Promotion is an explicit repository change from Evidence Candidate to Consumer-owned accepted input.

Before promotion:

```text
discovered unique candidates
= accepted canonical
+ explicitly excluded
+ explicitly deferred/unresolved
```

Promotion requires in-scope unresolved = 0. An item may be excluded/deferred only with a durable evidence-backed reason.

Promoted `data-migrations/main/v1/**` must freeze, as applicable:

- source root / redirect evidence;
- collection timestamp;
- surface/pagination completeness report;
- accepted/excluded counts and reasons;
- exact stable target identity;
- source identity and source fingerprint;
- source URL/detail/list provenance;
- publish/order metadata when reliably available;
- canonical item fingerprints;
- resource path / size / SHA-256;
- human classification decisions for ambiguous cases;
- accepted dataset/manifest digest or equivalent deterministic integrity record.

Do not commit a growing series of full snapshot ZIPs. Git history expresses future changes.

## 9. Canonical organization

Use the existing site-neutral Generic contracts:

```text
data-migrations/main/v1/
├── manifest.json
├── index.ndjson
├── articles/<stable-id>/article.json
├── articles/<stable-id>/assets/**
├── pages/index.json
├── pages/items/<stable-id>/page.json
├── pages/items/<stable-id>/assets/**
├── lists/<list-code>/index.json
├── lists/<list-code>/items/<stable-id>/item.json
├── reports/**
└── source-discovery/**
```

Do not create a Main-specific canonical schema unless actual source evidence proves the accepted Generic Article/List/Page contracts cannot represent an in-scope content type. Any such gap is a new planning finding, not permission to bypass Generic boundaries.

## 10. Offline verification boundary

Stable verification must not access the Legacy Source.

EU-50 must add or extend an offline verifier that can prove from repository-owned bytes alone:

- schema/JSON validity;
- stable source identity uniqueness;
- source fingerprint/index consistency;
- stable target alias/code/Page identity validity;
- resource path safety;
- resource size/SHA-256 integrity;
- accepted/excluded/unresolved arithmetic;
- unresolved current-scope items = 0;
- no canonical body/resource dependency on Legacy URLs when local canonical bytes are required.

The manual collection workflow may use the network; ordinary CI and later EU-51 verification may not.

## 11. EU-50 verification obligations

Before EU-50 integration, prove at least:

1. current source root/redirect is explicitly recorded rather than assumed;
2. all known Main source surfaces are inventoried;
3. pagination traversal reaches evidence-defined termination rather than first-page/Top-N shortcuts;
4. rerun on unchanged source is deterministic enough to compare identities/fingerprints;
5. discovered arithmetic reconciles;
6. all in-scope unresolved classifications are closed before promotion;
7. promoted canonical Article/Page/List units validate through current site-neutral contracts;
8. resource bytes/digests validate;
9. stable/offline verifier passes with network access unnecessary;
10. Party canonical dataset and Generic migration regressions remain unchanged;
11. no Runtime CMS rows, Site Package stable structure, Flyway, Public renderer behavior or visible Main content are changed by EU-50 itself.

Because EU-50 promotes real content bytes/evidence, its final PR diff must be reviewed for unintended secrets/private data and for evidence boundedness before integration.

## 12. EU-51 technical boundary

After EU-50 is integrated, EU-51 Fresh Context consumes the accepted snapshot and establishes its own Execute baseline.

Expected implementation/verification composition:

```text
Fresh Generic Flyway schema
→ JilinJobs Site Package reconcile
→ bootstrap only when the scenario requires it
→ generic-content data-migrations/main/v1
→ runtime reconciliation
→ second import
→ conflict/tamper scenarios
→ Public/Admin/Integrated Browser
→ Human Review
```

EU-51 must prove:

- first import expected total/created/applied behavior;
- second import SKIP/idempotency;
- source fingerprint tamper = CONFLICT/no silent overwrite;
- Page target/precondition drift = CONFLICT/no unsafe mutation;
- resource tamper/path failure is rejected;
- Runtime Article/Page/List identities/counts/resources reconcile to the accepted snapshot;
- EXTERNAL_LINK behavior follows E1;
- Page behavior follows E2;
- Main public rendering and representative/high-risk content pass Human Review;
- Party and existing Generic compatibility remain PASS;
- no EU-51 stable workflow contacts Legacy Source.

Any exact expected totals in EU-51 must come from EU-50's accepted repository evidence, not this Technical Plan.

## 13. Side effects and rollback

### EU-50

- External reads from Legacy Source are allowed only in explicit collection runs.
- Repository side effects are bounded to collector/workflow/verifier and promoted historical migration evidence/data.
- No Runtime DB mutation is accepted as EU-50 product side effect.
- Rollback is the EU-50 repository change; ephemeral source artifacts have no authority after expiration.

### EU-51

- Uses Fresh verification databases/static roots for automated verification.
- Runtime mutation is through the accepted Generic Content Migration application only.
- No Main-specific Runtime mutation path is added unless separately authorized by new evidence.

## 14. Non-goals

- no Main data in Flyway;
- no historical members in stable Site structure/bootstrap;
- no Party dataset rewrite;
- no Public frontend technology change;
- no C1/C2, Issue #57/#59 or Repository Split work;
- no hardcoded accepted item counts in Generic code or planning Authority;
- no `agentic-dev` baseline update.

## 15. Technical readiness

The upstream E2 foundation is integrated; stable Site targets exist; Generic Content Migration already supports Article/ListItem/Page; the current Legacy Source is reachable and demonstrably large/mixed; and the external-source versus offline-runtime boundary is directly testable.

Technical Planning is therefore **READY**.

`slice-work` result:

1. **EU-50 — Main Source Discovery & Accepted Snapshot Promotion** — Candidate formed; current dependencies are closed and `readiness-check = PASS`.
2. **EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review** — Candidate formed, but `readiness-check` remains **PENDING** until EU-50 integrates an accepted snapshot.

EU-50's stable Identifier is for tracking only; its Execute baseline must still be established from the integrated Planning/Readiness state in a subsequent Fresh Context before collection/promotion Execute begins.
