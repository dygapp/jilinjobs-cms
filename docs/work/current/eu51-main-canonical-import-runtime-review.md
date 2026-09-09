# EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review Closure

## Status

- Parent: GitHub Issue #60 / E3
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Requirement: `docs/requirements/main-historical-content-migration.md`
- Specification: `docs/specifications/main-historical-content-migration.md`
- Technical Plan: `docs/technical/main-historical-content-migration.md`
- Identifier: **EU-51**
- Candidate formed by: prior E3 `slice-work`
- Readiness: **PASS**
- Readiness baseline: `main@fd192460cb481645c1f1af435cbe5451145797d9`
- Execute baseline: **PENDING — establish only after this Planning/Readiness state is integrated**
- Execute state: **NOT STARTED**
- Current migration scope: **ARTICLE ONLY / accepted current subset only**
- EU-50 dependency: **SATISFIED / Execute Authority TERMINATED**
- Deferred problem Articles: **later separate review; not EU-51 import input**
- Main Page / stable ListItem: **separate Site Package Planning/Readiness path**

## 1. Goal

Import the already integrated, repository-owned Main Article canonical dataset into a Fresh Runtime through the existing site-neutral Generic Content Migration capability, prove deterministic reconciliation/idempotency/resource behavior, verify Public/Admin/Integrated Browser behavior, and complete bounded Human Review for the imported accepted subset.

EU-51 is a Runtime application/verification closure unit. It does not reopen Legacy Source collection and does not repair or absorb deferred problem Articles.

## 2. Accepted input

The only authorized Main migration input is:

`data-migrations/main/v1/**`

Accepted identity:

- migration id: `main-v1`;
- status: `accepted-current-subset`;
- 3078 current import-eligible Articles;
- 1577 INTERNAL + 1501 EXTERNAL_LINK;
- 2603 local resource files / 450,273,166 bytes;
- 6 source-defect Articles excluded pending client confirmation;
- 230 deferred problem Articles preserved as later-review evidence;
- unscoped blocking observations: 0;
- dataset digest: `sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`.

No Actions artifact or Legacy Source response is a downstream stable input.

## 3. Dependency / target reconciliation

EU-50 accepted-snapshot dependency is satisfied.

The Main Article source contract contains ten target Column aliases:

- `notice`;
- `employment-news`;
- `recruitment-announcement`;
- `policy-month`;
- `policy-outside`;
- `policy-jilin`;
- `policy-national`;
- `typical-grassroots`;
- `typical-startup`;
- `typical-military`.

Current `sites/jilinjobs/structure/columns.json` contains all ten aliases as stable, enabled Site Package Columns. Generic migration preflight resolves Article targets by alias and fails closed when a target Column is missing or disabled.

## 4. Required Runtime ordering

A Fresh DB does not imply that Site Package rows already exist. EU-51 must preserve this explicit order:

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

Repository capabilities already exist for this chain:

- `provisionSitePackage` — stable JilinJobs structure reconciliation;
- `importCanonicalContent` / Generic `generic-content` entry — site-neutral canonical import;
- Generic migration preflight / execute / mapping / conflict / integrity behavior;
- existing CI, Review Environment and browser-verification infrastructure.

EU-51 may add the thinnest Main-specific orchestration/verification needed to compose those existing capabilities. It must not create a Main-specific migration engine when the Generic capability already satisfies the contract.

## 5. In scope

- offline validation of the integrated `main-v1` canonical root before Runtime mutation;
- Fresh DB schema initialization and JilinJobs Site Package provisioning;
- first Generic Article import of exactly the 3078 accepted Articles;
- fail-closed verification that first import has zero conflict/invalid outcomes;
- second identical import proving idempotent SKIP behavior;
- stable legacy mapping / Runtime Article identity reconciliation;
- per-target-Column Runtime count reconciliation against canonical input;
- INTERNAL / EXTERNAL_LINK semantics reconciliation;
- local Article resource import/projection and digest/path/reference verification;
- Public Article/list/detail behavior verification across the imported Main scope;
- Admin visibility/operability verification without granting Admin ownership over migration identity;
- Integrated Browser regression verification;
- bounded Human Review covering representative/high-risk accepted records;
- explicit evidence/reporting for any discovered accepted-subset defect without silent repair.

## 6. Human Review scope

Human Review is performed only against the **3078 accepted current-subset Articles imported by EU-51**.

The review set must be deterministic and evidence-backed, and cover at least:

- every one of the ten target Column aliases;
- INTERNAL and EXTERNAL_LINK behavior where present;
- representative resource-bearing INTERNAL Articles;
- long/rich body content and attachment/body-image behavior where present;
- representative oldest/newest or otherwise boundary-position records available from canonical metadata;
- Public list/detail navigation and external-link opening behavior;
- Admin lookup/editability behavior needed by current product Authority.

Human Review does not require reviewing all 3078 records one by one unless new evidence establishes that need.

If Human Review finds a correctness defect in an **accepted** canonical record, EU-51 must preserve evidence and stop/route the finding according to current Authority; it must not silently rewrite canonical data or Runtime output merely to pass review.

## 7. Explicitly out of scope

- any Legacy Source network access;
- another source collection/retry pass;
- importing or repairing the 230 deferred problem Articles;
- importing the 6 source-defect Articles;
- Main Page migration;
- Main stable ListItem migration;
- Site Package stable ListItem identity/reconcile capability;
- changing accepted Page content in `sites/jilinjobs/**`;
- new Main-specific Flyway data migrations;
- a Main-specific replacement for Generic Content Migration;
- Public frontend technology changes;
- `agentic-dev` baseline update.

## 8. Verification obligations

Before Ready to Integrate, EU-51 must prove at least:

1. canonical identity/digest/count contract matches integrated `main-v1`;
2. no deferred/source-defect/Page/List record enters the Runtime import set;
3. all ten target Column aliases are present/enabled after Site Package provisioning;
4. first import total = 3078, created = 3078, conflicts = 0, invalid = 0 on a Fresh Runtime;
5. second identical import total = 3078, created = 0, skipped = 3078, conflicts = 0, invalid = 0;
6. Runtime legacy mappings reconcile one-to-one with imported Article identities;
7. Runtime Article counts reconcile to the canonical target aliases and Article types;
8. resource bytes/path/reference behavior matches canonical size/SHA-256 and contains no unsafe/missing projection;
9. EXTERNAL_LINK records retain external-navigation semantics and do not gain local body/resources by inference;
10. Public/Admin/Integrated Browser verification passes with imported Main content;
11. bounded Human Review produces explicit evidence and no unresolved correctness blocker for the accepted subset;
12. no Legacy Source access occurs during stable import/verification;
13. no Page/List Site Package follow-up or deferred Article handling is smuggled into the diff.

## 9. Expected implementation boundary

Expected touched areas are limited to the smallest necessary combination of:

- a dedicated EU-51 workflow / verification harness;
- Main canonical runtime reconciliation/report tooling if existing Generic reports are insufficient;
- browser/review sample preparation that consumes imported Runtime data;
- verification tests/scripts;
- authority/current-evidence sync.

Changes to Generic migration code are allowed only if execution proves a real site-neutral capability defect against current Requirement/Specification. Main-specific policy must remain outside the Generic package.

## 10. Readiness decision

`readiness-check = PASS` on `main@fd192460cb481645c1f1af435cbe5451145797d9` because:

1. E3 Requirement / Specification / Technical Plan are Ready;
2. EU-51 already exists as the downstream Candidate formed by E3 `slice-work`;
3. its only blocking dependency, EU-50 accepted Article snapshot integration, is satisfied;
4. the canonical dataset is repository-owned, deterministic, offline-verifiable and has zero unscoped blocking observations;
5. all ten canonical target Column aliases are represented as enabled stable Site Package Columns;
6. the Generic importer already provides Article load/validation/preflight/apply, stable mapping, fail-closed conflict/invalid handling and idempotency behavior;
7. repository tasks already expose Site Package provisioning and Generic canonical import entry points;
8. existing CI / Review Environment / browser infrastructure can prove the downstream behavior;
9. deferred Article handling and Page/List Site Package work are explicitly separable and do not block this unit;
10. the unit is bounded to one accepted Article dataset and is suitable for Fresh-context execution.

## 11. Execute Gate

This Work artifact establishes **Readiness only**. It does not establish an Execute baseline on the Planning/Readiness branch.

After this state is integrated, a Fresh Context must re-read actual `main`, Issue #60 / #77, Open PR / Actions, E3 Authority, this Work artifact and the canonical manifest; it must verify no base/authority drift and then establish EU-51's independent Execute baseline.

Until that recovery succeeds, no Runtime Main import is authorized. EU-51 does not inherit EU-50, EU-49, EU-48, Phase 3 or E1 Execute Authority.
