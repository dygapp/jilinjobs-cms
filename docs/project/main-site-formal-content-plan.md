# Main Site Formal Content E1～E3 Planning

> **Classification: HISTORICAL_EVIDENCE / COMPLETED PLANNING RECORD**  
> 本文件保留 E1～E3、EU-49～EU-53 的已完成 Planning / implementation lineage。下文出现的 `Current Ready Execution Unit`、`Current Gate`、baseline 与“下一自然 Gate”均是对应 closure 时点的历史快照，不再拥有当前 Planning / Execute state。当前状态必须从 `docs/work/current/README.md`、Project Roadmap 与 GitHub native state 恢复。

## Closure Snapshot（historical）

- Parent Planning Authority: GitHub Issue #60
- Architecture Authority: GitHub Issue #77
- Phase 3 re-entry: **PASS**
- E1: **COMPLETED / Authority-only**
- E2 / EU-49: **COMPLETED**
- E3 Article migration sequence: **COMPLETED through EU-50 / EU-51**
- Main Page Site Package follow-up / EU-52: **COMPLETED / Execute Authority TERMINATED**
- Main ListItem Site Package follow-up / EU-53: **COMPLETED / Execute Authority TERMINATED**
- Current Ready Execution Unit: **NONE**
- EU-53 Planning/Readiness baseline: `main@4d5578a2715f8adc0ebca73ee0ae7342f740c8ce`
- EU-53 Execute baseline: `main@26a772928278754f478b38742423fc7b71b1f439`
- EU-53 integrated main: `b1130b110bccdb1565c340a6cce62504ec06a87a`
- Completed Work Evidence: `docs/work/archive/eu53-main-listitem-bootstrap-completion.md`

## 1. Accepted ownership boundary

```text
Main Article historical content
    → Historical Content Migration

Main Page formal content + stable Page assets
    → JilinJobs Site Package / EU-52 completed

Main ListItem initial site data
    → JilinJobs Site Package bootstrap SQL / EU-53 completed

Party ListItems / Party carousel
    → Party migration authority; not part of EU-53
```

EU-50 source discovery already established that Main ListItem data does not belong to Article Historical Migration. EU-53 has now completed the remaining concrete initialization-data gap by writing the reviewed Main ListItem values into the existing Site Package one-time bootstrap SQL.

## 2. Why ListItem remained incomplete before EU-53

EU-50 had Article-only Execute Authority. PR #117 temporarily carried ListItem review/report work, but that work was removed when the PR was restored to the authorized Article-only tree. EU-52 later completed only Main Page formal content.

This did **not** invalidate the earlier ownership decision. It only left the concrete Main ListItem initialization data incomplete until EU-53.

The earlier Planning attempt that expanded this into a new stable ListItem identity/reconcile subsystem was unnecessary for the current product requirement and is superseded by the accepted bootstrap-data design.

## 3. Accepted ListItem delivery model

Main ListItems use the same Site Package initialization-data model already used by current bootstrap data:

```text
Site Package stable structure provision
→ Site Package bootstrap SQL applied once
→ ListItem rows become ordinary operator-managed Runtime data
```

`SitePackageBootstrapper` records `(packageId, bootstrapId)` in `cms_site_bootstrap_state`; an applied bootstrap is not replayed on every startup. No new ListItem stable identity, Flyway column, package `list-items` structure type, adoption algorithm, delete protection or runtime reconcile capability is required.

## 4. EU-53 completed result

The Main initialization SQL now contains:

- `HOME_CAROUSEL`: 1 Main homepage carousel item;
- `SITE_RELATED`: 5 reviewed link items;
- `SITE_REGIONAL_GRADUATES`: 31 reviewed link items;
- `SITE_JILIN_UNIVERSITIES`: 60 reviewed link items;
- existing Main advertisement bootstrap data remains present.

The three `SITE_LINKS` groups consume the final reviewed ListItem evidence from repository history:

- commit `b223a1d3a40b510f53a34ea9997926f8f4541a18`;
- `sites/jilinjobs/reports/listitem-final-adjustment-report.md`;
- 96 audited occurrences = 72 adjusted + 24 unchanged.

No live-network re-audit was required. Accepted titles/URLs from that report were the implementation input.

`HOME_CAROUSEL` is Main Site Package bootstrap data and uses the same initialization mechanism. `PARTY_CAROUSEL` and all other Party ListItems remain explicitly excluded because Party data migration/current Party Authority owns them.

## 5. Implementation boundary

EU-53 stayed intentionally small. PR #130 changed only:

1. `sites/jilinjobs/bootstrap/initial-data.sql`;
2. `sites/jilinjobs/bootstrap/manifest.json`;
3. `backend/apps/cms-server/src/test/kotlin/com/jilinjobs/cms/provisioning/SiteBootstrapBaselineSeparationVerification.kt`.

Final bootstrap digest:

`sha256:45e8ea98ecf82c06e14617874f0b2fb58a4ddc681d84042cd5731afff6c6c15e`

No Generic CMS schema or Site Package provisioner change was introduced.

## 6. Verification result

Exact implementation Head `10449bedf4df38aa2daec99b80d0a9637df2f8db` passed:

- CI #987 / run `34483084921`;
- Site Package Verification #109 / run `34483084966`;
- Backend Application Boundary Verification #42 / run `34483084916`;
- EU-51 Main Canonical Runtime Verification #22 / run `34483085548`;
- EU-51 Main Imported Browser Verification #18 / run `34483084972`;
- Review Environment #865 / run `34483084943`.

Post-Integration on `main@b1130b110bccdb1565c340a6cce62504ec06a87a` also passed:

- CI #988 / run `34484030549`;
- Site Package Verification #110 / run `34484030699`;
- Backend Application Boundary Verification #43 / run `34484030679`.

Focused verification proves Fresh bootstrap Main counts `1 / 5 / 31 / 60`, second apply is `ALREADY_APPLIED`, Party carousel is not inserted by Main bootstrap, and later operator changes are not resurrected by ordinary Runtime or repeated guarded bootstrap evaluation.

External third-party availability is not part of Runtime acceptance; the links were already reviewed in EU-50 evidence.

## 7. Non-goals retained

- no `cms_list_item.code` schema change;
- no Flyway V4 for ListItem identity;
- no Site Package `list-items.json`;
- no ListItem reconcile/adoption/removal/delete-protection framework;
- no Party ListItem or `PARTY_CAROUSEL` changes;
- no Article migration changes;
- no Hui Employment iframe changes in EU-53;
- no `agentic-dev` baseline update.

## 8. Closure Gate Snapshot（historical）

EU-53 is **COMPLETED** and Execute Authority is **TERMINATED**. Current Ready Execution Unit returns to **NONE**.

The next natural Gate is a new **Fresh Context Planning/Readiness decision** based on current Repository Authority. Deferred Articles, Hui Employment iframe and other Issue #57/#59/#60 candidates remain independent; none inherits EU-53 Execute Authority.
