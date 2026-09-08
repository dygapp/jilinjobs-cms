# EU-49 — Page Operational Content Ownership & Migration Foundation

## Status

- Parent: GitHub Issue #60 / E2
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Requirement: `docs/requirements/main-single-page-formal-content.md`
- Specification: `docs/specifications/main-single-page-formal-content.md`
- Technical Plan: `docs/technical/main-single-page-formal-content.md`
- Candidate formed by: `slice-work`
- Candidate identifier: **EU-49**
- Identifier collision check: no current repository file / PR match for `EU-49` at planning time
- Readiness: **PASS**
- Planning baseline: `main@f42bacf4ab7719e3291288c77f0685b428b86141`
- Execute state: **NOT STARTED**
- Execute Authority: **PENDING planning integration + Fresh Context revalidation**

## 1. Dependency closure

Current repository audit establishes:

1. E1 external-link behavior/ownership is already implemented and can close as Authority-only; no E1 implementation dependency remains.
2. Main stable Page/PageGroup identities already exist in Site Package.
3. Current `PageService` intentionally allows preset Page content (`bodyHtml/renderMode/embedUrl`) to be edited.
4. Current Site Package existing-Page reconcile nevertheless overwrites those same three fields from `pages.json`.
5. Therefore actual formal Page content cannot safely be delivered by canonical migration while ordinary reconcile remains a continuous competing owner.
6. EU-47 Generic Content Migration only handles Article/ListItem; Page targets need an explicit site-neutral guarded apply/mapping capability.
7. Current active Generic Flyway ends at V2; accepted architecture explicitly reserves future Generic schema changes from V3 append-only.
8. The content-migration app already owns neutral legacy mappings and Generic load/preflight/execute/report, so no new app/module is required.
9. Page RICH_TEXT has shared sanitizer but no Page Resource association; deterministic historical static projection is sufficient for canonical Page body resources without changing product model.
10. Actual Main Page bytes/source evidence are not required to validate this foundation and are deliberately excluded.

No unresolved Product Goal, user-visible layout, external integration or Main content decision blocks the foundation.

## 2. Slice-work result

E2 first foundation forms one Candidate:

**EU-49 — Page Operational Content Ownership & Migration Foundation**

Keep it atomic because:

- Site Package no-overwrite without an explicit Page content delivery path leaves E2 unable to converge formal content;
- Page migration support without Site Package no-overwrite is not stable across restart/reconcile;
- the V3 mapping, Core content boundary, Generic Page preflight/apply and no-overwrite regression must be verified together to prove ownership transfer;
- actual Main content collection has a different evidence/human-review rollback boundary and must remain downstream.

## 3. Execute scope

### 3.1 Site Package no-content-overwrite

`SitePackageProvisioning.kt`:

- Fresh Page creation retains current package `bodyHtml/renderMode/embedUrl` defaults;
- existing Page reconcile excludes these three fields from comparison and UPDATE;
- no broader Site Package/preset operational-field refactor.

### 3.2 Generic schema

Add append-only:

`V3__page_content_migration_mapping.sql`

with site-neutral `cms_page_legacy_mapping`, unique source identity, source fingerprint/provenance and FK to target Page.

No data seed and no Main identity in Flyway.

### 3.3 Core Page content capability

Add a narrow content-only Page update boundary that:

- updates only bodyHtml/renderMode/embedUrl;
- reuses current sanitizer/render-mode/embed validation;
- does not alter Page identity/group/name/order/enabled/preset;
- creates no new HTTP API.

### 3.4 Generic Page canonical capability

Extend existing `generic-content` pipeline with:

- Page index/item canonical models;
- stable target `(groupAlias? + pageAlias)` resolution;
- `expectedTargetFingerprint` first-apply guard;
- neutral Page mapping classification;
- Page body resource verification / deterministic historical static projection;
- Page plan/apply/report kind;
- same-source rerun SKIP preserving post-import operator edits;
- changed source / target precondition drift CONFLICT.

No Main/Party alias or policy enters Generic source.

### 3.5 Verification

Use synthetic site-neutral fixture and focused tests to prove:

- first Page apply;
- second SKIP;
- post-import operator edit preserved;
- target-precondition conflict no mutation;
- source fingerprint conflict no mutation;
- missing target/path/resource invalid no mutation;
- Site Package second reconcile preserves Page content fields;
- Article/List Generic regressions, Party canonical/upgrade, Backend boundary, Site Package and Repository CI remain PASS.

## 4. Explicit non-goals

- no Main source access / scraping;
- no Main canonical Page bytes;
- no E3 Article/List collection/import;
- no Page publish/version model;
- no Page Resource association;
- no new Public/Admin route or visual change;
- no change to placeholders' product meaning;
- no Party dataset/compatibility change;
- no repository split;
- no `agentic-dev` baseline update.

## 5. Acceptance criteria

EU-49 is accepted only if all are true at exact implementation Head:

1. existing preset Page content edits survive ordinary Site Package reconcile;
2. Fresh Page provisioning remains deterministic;
3. Generic Page fixture applies only when exact target precondition matches;
4. successful first apply records mapping and produces expected sanitized/re-written Runtime content;
5. same input rerun SKIPs and does not overwrite later operator edit;
6. changed source fingerprint / wrong target precondition / mapping-target drift rejects without Page mutation;
7. Page resource path/digest/token safety is enforced when resources exist;
8. V3 is site-neutral and append-only; V1/V2 unchanged;
9. Generic Article/List and Party consumers regressions PASS;
10. content-migration continues to depend only on cms-core and remains non-web;
11. no actual Main data or visible Page content changes are part of the diff;
12. unresolved PR review threads = 0 and final required Actions PASS.

## 6. Rollback / side effects

Rollback boundary: complete EU-49 implementation PR.

Persistent schema side effect is only the additive empty Page migration mapping table. The implementation itself imports no Main content. Focused runtime verification must use Fresh DB and temporary static roots.

## 7. Readiness check

### Goal / acceptance clarity — PASS

Requirement and Specification define exactly what ownership conflict is fixed and how safe first apply/idempotency behave.

### Dependency closure — PASS

E1 contract is closed at planning level; stable Page targets, Generic app, Core Page model and V3 slot already exist. E3/source evidence is deliberately downstream, not a prerequisite.

### Scope / rollback — PASS

One coherent foundation PR; real Main content is excluded.

### Verification strategy — PASS

Focused synthetic Page tests plus existing Generic/Party/SitePackage/Boundary/CI gates can verify every material contract without Legacy Source access.

### Risk / side-effect control — PASS

No bulk content update. First apply has exact target precondition; same-input rerun never takes back operator edits; schema is additive.

### Human escalation — PASS / NOT REQUIRED

The Unit resolves a contradiction in accepted current ownership semantics and adds a generic delivery capability; it does not choose new Product Goal, visual behavior or external integration.

## 8. Readiness decision

**PASS — EU-49 is a valid Ready Execution Unit candidate once this Planning/Readiness change is integrated.**

Before Execute, a Fresh Context must re-read integrated `main`, Issue #60, this Requirement/Specification/Technical Plan/Work Authority, Open PR/Actions and confirm no base drift or Authority change. Only then does EU-49 receive its own Execute baseline. No earlier EU/Phase authority is inherited.

E3 remains downstream Planning and receives no identifier or Execute Authority from this decision.
