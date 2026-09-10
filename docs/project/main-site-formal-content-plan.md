# Main Site Formal Content E1～E3 Planning

## Status

- Parent Planning Authority: GitHub Issue #60
- Architecture Authority: GitHub Issue #77
- Phase 3 re-entry: **PASS**
- E1: **COMPLETED / Authority-only**
- E2 / EU-49: **COMPLETED**
- E3 Article migration sequence: **COMPLETED through EU-50 / EU-51**
- Main Page Site Package follow-up / EU-52: **COMPLETED / Execute Authority TERMINATED**
- Main ListItem Site Package follow-up / EU-53: **READY / bootstrap-data completion**
- Current Ready Execution Unit: **EU-53**
- Planning/Readiness baseline: `main@4d5578a2715f8adc0ebca73ee0ae7342f740c8ce`
- Work Authority: `docs/work/current/eu53-main-listitem-bootstrap-completion.md`

## 1. Current ownership boundary

```text
Main Article historical content
    → Historical Content Migration

Main Page formal content + stable Page assets
    → JilinJobs Site Package / EU-52 completed

Main ListItem initial site data
    → JilinJobs Site Package bootstrap SQL / EU-53

Party ListItems / Party carousel
    → Party migration authority; not part of EU-53
```

EU-50 source discovery already established that Main ListItem data does not belong to Article Historical Migration. The remaining gap is only that the reviewed Main ListItem values were not fully written into the Site Package initialization SQL.

## 2. Why ListItem remained incomplete

EU-50 had Article-only Execute Authority. PR #117 temporarily carried ListItem review/report work, but that work was removed when the PR was restored to the authorized Article-only tree. EU-52 later completed only Main Page formal content.

This did **not** invalidate the earlier ownership decision. It only left the concrete Main ListItem initialization data incomplete.

The earlier Planning attempt on this branch incorrectly expanded this into a new stable ListItem identity/reconcile subsystem. That is unnecessary for the current product requirement and is superseded by this plan.

## 3. Current ListItem delivery model

Main ListItems use the same Site Package initialization-data model already used by current bootstrap data:

```text
Site Package stable structure provision
→ Site Package bootstrap SQL applied once
→ ListItem rows become ordinary operator-managed Runtime data
```

`SitePackageBootstrapper` already records `(packageId, bootstrapId)` in `cms_site_bootstrap_state`; an applied bootstrap is not replayed on every startup. No new ListItem stable identity, Flyway column, package `list-items` structure type, adoption algorithm, delete protection or runtime reconcile capability is required.

## 4. EU-53 scope

The Main initialization SQL must contain:

- `HOME_CAROUSEL`: Main homepage carousel initialization data, using the same bootstrap-SQL lifecycle;
- `SITE_RELATED`: 5 reviewed link items;
- `SITE_REGIONAL_GRADUATES`: 31 reviewed link items;
- `SITE_JILIN_UNIVERSITIES`: 60 reviewed link items;
- existing Main advertisement bootstrap data remains unchanged unless separately required.

The three `SITE_LINKS` groups consume the final reviewed ListItem evidence from repository history:

- commit `b223a1d3a40b510f53a34ea9997926f8f4541a18`;
- `sites/jilinjobs/reports/listitem-final-adjustment-report.md`;
- 96 audited occurrences = 72 adjusted + 24 unchanged.

No live-network re-audit is required. Accepted titles/URLs from that report are the implementation input.

`HOME_CAROUSEL` is Main Site Package bootstrap data and uses the same initialization mechanism. `PARTY_CAROUSEL` and all other Party ListItems are explicitly excluded because Party data migration already owns them.

## 5. Implementation boundary

EU-53 is intentionally small:

1. keep Generic CMS schema and Site Package provisioner unchanged;
2. expand `sites/jilinjobs/bootstrap/initial-data.sql` with the accepted Main ListItem rows;
3. keep the Main `HOME_CAROUSEL` row in the same SQL;
4. update `sites/jilinjobs/bootstrap/manifest.json` SHA-256 for the changed SQL;
5. add or update focused verification that a Fresh Site receives the expected Main ListItem counts/content and that Party data is unaffected.

This work does not create a new long-lived ListItem ownership subsystem.

## 6. Verification boundary

Verification must prove at least:

- bootstrap manifest digest matches `initial-data.sql`;
- Fresh Site bootstrap succeeds;
- Main `HOME_CAROUSEL` initialization remains present;
- `SITE_RELATED = 5`;
- `SITE_REGIONAL_GRADUATES = 31`;
- `SITE_JILIN_UNIVERSITIES = 60`;
- reviewed title/URL corrections are represented exactly in SQL;
- second bootstrap evaluation returns the existing one-time/`ALREADY_APPLIED` behavior rather than duplicating data;
- no Party ListItem/migration data is changed;
- existing Site Package / Backend / Public checks remain green.

External third-party availability is not part of Runtime acceptance; the links were already reviewed in EU-50 evidence.

## 7. Non-goals

- no `cms_list_item.code` schema change;
- no Flyway V4 for ListItem identity;
- no Site Package `list-items.json`;
- no ListItem reconcile/adoption/removal/delete-protection framework;
- no Party ListItem or `PARTY_CAROUSEL` changes;
- no Article migration changes;
- no Hui Employment iframe changes in EU-53;
- no `agentic-dev` baseline update.

## 8. Current Gate

`slice-work` has formed **EU-53 — Main ListItem Bootstrap Completion** and `readiness-check` is **PASS**. Planning/Readiness Authority must integrate first; then Execute recovery revalidates the integrated `main` and establishes the EU-53 Execute baseline. No previous EU Execute Authority is inherited.
