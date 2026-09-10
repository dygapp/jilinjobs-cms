# EU-53 — Main ListItem Bootstrap Completion

## Status

- Parent Planning Authority: GitHub Issue #60
- Architecture Authority: GitHub Issue #77
- Requirement: `docs/requirements/main-stable-listitem-site-package.md`
- Specification: `docs/specifications/main-stable-listitem-site-package.md`
- Technical Plan: `docs/technical/main-stable-listitem-site-package.md`
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Candidate formed by: **slice-work**
- Identifier: **EU-53**
- Planning baseline: `main@4d5578a2715f8adc0ebca73ee0ae7342f740c8ce`
- Readiness: **PASS**
- Execute state: **NOT STARTED**
- Execute Authority: **READY AFTER PLANNING INTEGRATION / FRESH-CONTEXT BASELINE RECOVERY**

## 1. slice-work result

Current Requirement / Specification / Technical Plan resolve Main ListItem delivery to one existing mechanism: JilinJobs Site Package one-time bootstrap SQL. No new Generic CMS schema or provisioning capability is required.

One Candidate Execution Unit is therefore formed:

> **EU-53 — Main ListItem Bootstrap Completion**

The Unit is intentionally small and data-oriented.

## 2. In scope

1. keep Main `HOME_CAROUSEL` initialization in `sites/jilinjobs/bootstrap/initial-data.sql`;
2. replace `SITE_RELATED` with the final reviewed five values;
3. add the final reviewed 31 `SITE_REGIONAL_GRADUATES` values;
4. add the final reviewed 60 `SITE_JILIN_UNIVERSITIES` values;
5. preserve existing Main advertisement bootstrap data;
6. update `sites/jilinjobs/bootstrap/manifest.json` SHA-256;
7. verify Fresh Site bootstrap, second-apply `ALREADY_APPLIED`, exact Main counts/content, Public SITE_LINKS behavior and Party non-regression.

Accepted SITE_LINKS content authority is repository history commit `b223a1d3a40b510f53a34ea9997926f8f4541a18`, `sites/jilinjobs/reports/listitem-final-adjustment-report.md`: 96 audited = 72 adjusted + 24 unchanged.

## 3. Explicit exclusions

- no `cms_list_item.code` or Flyway change;
- no Site Package `list-items` structure type;
- no runtime reconcile/adoption/delete-protection framework;
- no Party ListItem / `PARTY_CAROUSEL` change;
- no Article migration changes;
- no Hui Employment iframe work;
- no `agentic-dev` baseline update.

Party carousel/ListItems remain under Party migration/current Party Authority.

## 4. Readiness check

| Dimension | Result | Evidence |
|---|---|---|
| Authority / Intent | **PASS** | Human clarification + Issue #60/#77 boundary: Main ListItem is Site Package initialization data |
| Requirement | **PASS** | Main scope and Party exclusion are explicit |
| Specification | **PASS** | Existing bootstrap SQL representation and lifecycle are sufficient |
| Technical Planning | **PASS** | Change is limited to SQL + digest + focused verification; no new framework |
| Source Evidence | **PASS** | EU-50 frozen audit + final adjustment report provide accepted 96 values |
| Slice Integrity | **PASS** | one homogeneous Main bootstrap-data delivery |
| Dependency Closure | **PASS** | parent Lists already exist in Site Package structure; bootstrap mechanism already integrated |
| Verification Feasibility | **PASS** | SitePackageBootstrapper, Fresh Runtime and Public list APIs already exist |
| Scope / Rollback | **PASS** | bounded Site Package data-only change |
| Human/Product Ambiguity | **PASS** | Main `HOME_CAROUSEL` included; Party carousel explicitly excluded |

Overall Readiness: **PASS**.

## 5. Execute verification contract

Before Integration, exact-head evidence must prove:

- bootstrap manifest SHA-256 matches `initial-data.sql`;
- Fresh Site contains Main `HOME_CAROUSEL` initialization;
- `SITE_RELATED = 5`, `SITE_REGIONAL_GRADUATES = 31`, `SITE_JILIN_UNIVERSITIES = 60`;
- the 96 SITE_LINKS title/URL values equal the accepted final report decisions;
- no Party ListItem appears in Main bootstrap SQL;
- second bootstrap evaluation is `ALREADY_APPLIED` and does not duplicate rows;
- existing Public SITE_LINKS endpoint exposes the initialized values;
- relevant Site Package / Backend / Public / Party regression gates remain PASS.

Third-party link availability is not an acceptance dependency.

## 6. Next Gate

Planning Authority and this Readiness result must first integrate to `main`. A subsequent Fresh Context must recover the integrated `main`, this Work artifact and current repository state before establishing EU-53 Execute baseline. No previous EU Execute Authority is inherited.
