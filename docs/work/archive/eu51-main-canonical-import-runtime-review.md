# EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review Closure

## Status

- Parent: GitHub Issue #60 / E3
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Requirement: `docs/requirements/main-historical-content-migration.md`
- Specification: `docs/specifications/main-historical-content-migration.md`
- Technical Plan: `docs/technical/main-historical-content-migration.md`
- Identifier: **EU-51**
- Readiness: **PASS**
- Readiness baseline: `main@fd192460cb481645c1f1af435cbe5451145797d9`
- Execute baseline: `main@04090a3cc8dd9c85112488f036c4bc1a67003594`
- Execute state: **COMPLETED**
- Execute Authority: **TERMINATED**
- Implementation PR: **#122**
- Final implementation Head: `5adae340edcf605e36779c831fb512c31342eec8`
- Implementation integrated main: `fad4bfc17b762ec9612cf1e44a1c0af67e74a307`
- Current migration scope: **ARTICLE ONLY / accepted current subset only**
- Deferred problem Articles: **later separate review; not imported by EU-51**
- Main Page / stable ListItem: **separate Site Package Planning/Readiness path**

## 1. Goal and accepted input

EU-51 imported the already integrated, repository-owned Main Article canonical dataset into a Fresh Runtime through the existing site-neutral Generic Content Migration capability, proved deterministic reconciliation/idempotency/resource behavior, verified Public/Admin/Integrated Browser behavior, and closed bounded Human Review for the accepted current subset.

The only authorized Main migration input was `data-migrations/main/v1/**`:

- migration id: `main-v1`;
- status: `accepted-current-subset`;
- 3078 current import-eligible Articles;
- 1577 INTERNAL + 1501 EXTERNAL_LINK;
- 2603 local resource files / 450,273,166 bytes;
- dataset digest: `sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`.

The 6 source-defect Articles excluded pending client confirmation and 230 deferred problem Articles were never EU-51 import input. No Legacy Source response or expiring Actions artifact became a stable downstream input.

## 2. Runtime composition proved

EU-51 preserved and verified this exact Fresh Runtime chain:

```text
Fresh MySQL
→ Generic Flyway V1/V2/V3
→ JilinJobs Site Package stable provisioning
→ Main generic canonical import
→ Runtime reconciliation
→ second import / idempotency
→ Public/Admin/Integrated Browser verification
→ bounded Human Review
```

Existing repository capabilities were reused:

- `provisionSitePackage` for stable JilinJobs structure reconciliation;
- `importCanonicalContent` / Generic Content Migration for site-neutral canonical import;
- Generic validation/preflight/apply/mapping/conflict/idempotency behavior;
- existing CI, Review Environment and browser verification infrastructure.

No Main-specific migration engine was introduced and Generic migration implementation did not require modification.

## 3. Runtime acceptance evidence

Final exact Head `5adae340edcf605e36779c831fb512c31342eec8` proved:

- offline canonical validation against the integrated `main-v1` input;
- all ten Main target Column aliases present and enabled after Site Package provisioning;
- first import: `total=3078`, `created=3078`, `skipped=0`, `conflicts=0`, `invalid=0`;
- 3078 stable legacy mappings reconcile one-to-one with accepted canonical identities/fingerprints;
- Runtime Article counts reconcile to 1577 INTERNAL + 1501 EXTERNAL_LINK and the ten target Columns;
- all 2603 Runtime resource files reconcile to canonical size/SHA-256, totaling 450,273,166 bytes;
- BODY_IMAGE and ATTACHMENT references resolve to Runtime resource endpoints;
- EXTERNAL_LINK records preserve external navigation semantics and do not gain inferred local body/resources;
- no Main Page/List migration mapping is produced;
- second identical import: `created=0`, `skipped=3078`, `conflicts=0`, `invalid=0`, with no resource/mapping growth.

Exact-head workflows:

- EU-51 Main Canonical Runtime Verification #8 / run `34414803518` — **PASS**;
- EU-51 Main Imported Browser Verification #4 / run `34414803509` — **PASS**;
- Canonical Migration Verification #264 / run `34414803507` — **PASS**;
- EU-30 Migration Upgrade Verification #214 / run `34414803594` — **PASS**;
- CI #957 / run `34414803593` — **PASS**;
- Standard Review Environment #840 / run `34414803462` — **PASS**.

Browser evidence artifact:

- artifact id: `10128741919`;
- digest: `sha256:83df3a2d37634e30403eba4777c5554c71723deca82f08ffad71330e1a6ca070`.

## 4. Browser verification and resolved harness findings

Imported-content Browser verification exercised the real 3078-Article Fresh Runtime rather than a synthetic Main migration fixture. Deterministic samples covered all ten target Columns, INTERNAL / EXTERNAL_LINK behavior, resource-bearing and long/rich INTERNAL Articles, attachment/body-image cases, oldest/newest boundaries, and Admin lookup/open behavior.

An earlier Browser run exposed only a test-harness assertion defect: Playwright `not.toBeEmpty()` treated an image-only rich body as text-empty although the DOM contained the expected Runtime `<img>` resources and those endpoints returned HTTP 200. The assertion was narrowed to non-empty `innerHTML`, while individual Runtime image/attachment verification remained. The final exact-head Browser #4 passed.

An earlier Standard Review Environment run failed while downloading `frpc`; the next exact-head Review #840 passed download, tunnel, external URL verification and lease lifecycle, establishing the earlier failure as transient infrastructure rather than a product/import defect.

## 5. Human Review closure

Bounded Human Review was completed on the same exact implementation Head and returned **PASS**.

The review set covered:

- all ten target Column aliases;
- INTERNAL and EXTERNAL_LINK behavior;
- resource-rich INTERNAL content;
- BODY_IMAGE and ATTACHMENT behavior;
- long/rich body content;
- oldest/newest boundary samples;
- Public list/detail/external-link presentation;
- Admin INTERNAL / EXTERNAL_LINK lookup/open behavior.

No accepted-subset correctness blocker was found.

Durable Human Review evidence:

- Issue #60 Current Evidence comment `5610212404`;
- PR #122 review `5160941774`;
- Browser evidence artifact `10128741919`.

## 6. Integration and Post-Integration evidence

PR #122 was squash merged from verified exact Head `5adae340edcf605e36779c831fb512c31342eec8`.

Implementation integration commit:

`main@fad4bfc17b762ec9612cf1e44a1c0af67e74a307`

Post-Integration CI #958 / run `34417382337` on that exact main commit: **PASS**, including Backend / Admin / Public / Integrated Browser.

Issue #60 implementation-integration evidence comment: `5610294608`.

## 7. Explicitly excluded / unresolved-by-design work

EU-51 did not authorize or perform:

- Legacy Source network access or another collection/retry pass;
- importing or repairing the 230 deferred problem Articles;
- importing the 6 source-defect Articles;
- Main Page migration;
- Main stable ListItem migration/capability;
- changing accepted Page content in `sites/jilinjobs/**`;
- new Main-specific Flyway data migrations;
- a Main-specific replacement for Generic Content Migration;
- Public frontend technology changes;
- `agentic-dev` baseline updates.

The 230 deferred problem Articles remain durable later-review evidence. The 6 source-defect Articles remain excluded pending client confirmation. Main Page / stable ListItem source handoff remains an independent JilinJobs Site Package Planning/Readiness path.

## 8. Closure decision

All EU-51 implementation, verification, Human Review, integration and Post-Integration acceptance obligations are closed. The implementation is reproducible from repository-owned canonical bytes and existing Generic/Site Package capabilities, with no unresolved accepted-subset correctness blocker.

EU-51 is therefore **COMPLETED** and its Execute Authority is **TERMINATED** when this Post-Integration Authority closure is integrated.

No downstream candidate inherits EU-51 Execute Authority. After closure, Current Ready Execution Unit returns to **NONE**. The next natural Gate is an independent Fresh Context Planning/Readiness decision from current Issue #60 / Issue #77 Authority; deferred Articles and Site Package Page/List follow-up remain separate candidates and must not be entered automatically.