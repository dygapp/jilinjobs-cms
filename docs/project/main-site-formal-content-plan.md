# Main Site Formal Content E1～E3 Planning

## Status

- Parent Planning Authority: GitHub Issue #60
- Architecture Authority: GitHub Issue #77
- Phase 3 re-entry: **PASS**
- E1: **COMPLETED / Authority-only**
- E2 / EU-49: **COMPLETED**
- E3 current Article migration sequence: **COMPLETED through EU-50 / EU-51**
- EU-50 — Main Source Discovery & Article Snapshot Promotion: **COMPLETED / Execute Authority TERMINATED**
- EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review: **COMPLETED / Execute Authority TERMINATED**
- Main Page Site Package follow-up / EU-52: **COMPLETED / Execute Authority TERMINATED**
- stable Main ListItem Site Package follow-up: **Planning Candidate / no Identifier**
- Current Ready Execution Unit: **NONE**
- EU-52 Planning baseline: `main@25e452ee3ce66c2d7da1000ad55e9570d732528a`
- EU-51 implementation integrated main: `fad4bfc17b762ec9612cf1e44a1c0af67e74a307`
- Ownership correction: **2026-09-09 — Main Page and stable ListItem content belong to JilinJobs Site Package**

## 1. Current planning boundary

Issue #60 E1～E3 remains the Main formal-content planning authority, with the accepted product ownership boundary:

```text
Main Article historical content
    → E3 Historical Content Migration

Main Page formal content + stable Page assets
    → JilinJobs Site Package / EU-52

Main stable ListItem membership
    → JilinJobs Site Package / independent later Planning Candidate

Legacy Source Page/List observations
    → source discovery evidence / Site Package handoff only
```

This boundary supersedes earlier planning text that treated Main Page bodies or stable Main list membership as Canonical Migration units.

The accepted Main Article sequence is closed through source discovery/promotion and Runtime import/reconciliation/Human Review. EU-52 has now also completed the independent Page-only Site Package implementation, verification, bounded Human Review and integration lifecycle; its Execute Authority is terminated. Stable ListItem remains unplanned at Execution Unit level.

## 2. Why Page and ListItem now split

Current repository evidence proves the two Site Package follow-ups have different readiness:

- `sites/jilinjobs/structure/pages.json` already owns stable Page identities and create-time `bodyHtml` defaults;
- EU-49 already ensures ordinary Site Package reconcile preserves existing operator-managed `bodyHtml / renderMode / embedUrl`;
- existing Site Package asset manifest/projector already owns stable package assets;
- EU-50 has accepted source handoff for 10 stable `RICH_TEXT` Page targets;
- Issue #77 Human Authority has resolved `budget` Page's 13-PDF ownership and the bounded normalization allowed for 8 legacy absolute-source residues;
- current Site Package v1 does **not** have a `list-items` structure type or stable ListItem identity/reconcile path;
- the six current Main bootstrap ListItems are one-time operator defaults and cannot simply be relabelled stable without defining identity, representation, adoption, reconcile, operator mutation and upgrade semantics.

Therefore `slice-work` forms a Page-only Unit. Combining stable ListItems would couple a ready product slice to unresolved architecture and would violate the independent Planning/Readiness boundary.

## 3. E1 accepted result

E1 continues to define link behavior/ownership:

- `EXTERNAL_LINK` Article owns article title + external target;
- Navigation owns stable navigation label/target/open mode;
- ListItem owns list placement/presentation target;
- fixed integration remains engineering-owned.

For completed E3, only Column content classified as INTERNAL / EXTERNAL_LINK Article is migration content. Main stable list placement is Site Package content.

## 4. E2 / EU-49 historical result

EU-49 established Page operational-content ownership protection and a site-neutral Generic Page migration capability. Its accepted result remains valid:

- missing preset Page may be created from package defaults;
- existing Page mutable content is operator-managed and ordinary Site Package reconcile does not overwrite it;
- Generic Page migration capability remains a generic CMS capability and historical compatibility evidence.

The later ownership correction means Main Page formal content is **not** delivered through Historical Migration. EU-49 is not reopened and its Execute Authority remains terminated.

## 5. E3 completed Article sequence

### EU-50 — Main Source Discovery & Article Snapshot Promotion — COMPLETED

EU-50 completed the explicit external-source stage and integrated the accepted current Article subset through PR #117.

Accepted result:

- total Article candidates: 3314；
- current import-eligible: 3078 = 1577 INTERNAL + 1501 EXTERNAL_LINK；
- source-defect excluded pending client confirmation: 6；
- deferred problem Articles: 230；
- current accepted resources: 2603 files / 450,273,166 bytes；
- canonical dataset: `data-migrations/main/v1/**`；
- dataset digest: `sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`；
- Page/List discoveries preserved as Site Package handoff evidence；
- no Runtime Main import was performed by EU-50。

Current Human Authority explicitly defers the 230 problem Articles and the separately excluded 6 source-defect Articles until later review/customer confirmation. They remain durable evidence, are not current import input, and are not part of EU-52.

EU-50 completed Work Evidence：`docs/work/archive/eu50-main-source-discovery-promotion.md`。Execute Authority is terminated.

### EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review — COMPLETED

EU-51 consumed only the accepted repository-owned `data-migrations/main/v1/**` Article subset. Completed Work Evidence：

`docs/work/archive/eu51-main-canonical-import-runtime-review.md`

Accepted Runtime closure:

- PR #122 final exact Head：`5adae340edcf605e36779c831fb512c31342eec8`；
- squash integrated main：`fad4bfc17b762ec9612cf1e44a1c0af67e74a307`；
- first Generic import：3078 CREATED，0 conflict / invalid；
- all stable mappings / target Columns / 2603 resources reconciled；
- second identical import：3078 SKIPPED，0 CREATED / conflict / invalid；
- no Main Page/List migration mapping produced；
- Public/Admin/Integrated Browser verification：PASS；
- bounded Human Review：PASS；
- Post-Integration CI #958 / run `34417382337`：PASS。

EU-51 Execute Authority is terminated.

## 6. EU-52 — Main Page Formal Content Package Adoption — COMPLETED

Completed Work Evidence：`docs/work/archive/eu52-main-page-formal-content-package-adoption.md`。

Accepted result：

- implementation PR #125 final exact Head `ff4acdc08e9b902d6aa273ff33fae33fb5bfc520`；squash integrated main `ccbd9fd8c6048f5b4a96d965b8578f7b7a1d2838`；
- 10 accepted Main `RICH_TEXT` Page formal bodies became JilinJobs Site Package defaults；
- 156 new Page-owned assets integrated, total package assets = 187；`budget` all 13 PDFs are package-owned；
- exact prior-package fingerprint guarded adoption, operator divergence reporting, idempotency and post-adoption operator-edit protection are implemented；
- exact-head CI / Site Package / compatibility regressions PASS；bounded Human Review PASS；
- manual review finding on `guide/faq` top-level question hierarchy was corrected and reverified by the final Integrated Browser gate；
- Post-Integration CI #973 / run `34442174532` and Site Package #104 / Backend Boundary #41 / Generic #64 / Party #19 all PASS；
- no Main Page Historical Migration mapping, Generic Flyway/schema expansion, stable ListItem implementation or Article backlog handling was introduced；
- Execute Authority：**TERMINATED**。

## 7. Stable Main ListItem follow-up — independent Planning Candidate

Stable Main ListItem membership remains a separate Site Package Planning Candidate because the current package owns `lists` definitions but not stable membership instances.

Before any ListItem Candidate Execution Unit can be formed, Planning must define at least:

- stable ListItem identity;
- package representation / schema evolution;
- Runtime adoption from one-time bootstrap or operator-maintained state;
- reconcile semantics and non-destructive operator mutation boundary;
- upgrade/removal ordering and conflict behavior;
- verification for link/article targets and Public list rendering.

EU-52 does not answer these questions and cannot grant authority to that path.

## 8. Source-error boundary

The accepted error policy remains:

- only confirmed HTTP 404/410 resource absence may be classified `SOURCE_RESOURCE_MISSING`;
- INTERNAL Articles whose every blocking issue is that class may be excluded pending client confirmation and must remain separately listed;
- all other Article error types remain separately classified / human-reviewable;
- transport/socket failures are never inferred to be missing-source evidence;
- Page/List errors are preserved in Site Package handoff reports and are not silently repaired, discarded or converted into migration decisions；
- 230 deferred problem Articles and 6 source-defect Articles remain independent later-review/customer-confirmation evidence and are outside EU-52。

For EU-52 Page source scope, artifact/resource path, size, SHA-256 or newly discovered source anomalies fail closed for the affected accepted Page. No silent HTML repair, resource deletion or guessed replacement is allowed.

## 9. Current Gate

Current Ready Execution Unit：**NONE**。

EU-52 已完成并终止 Execute Authority。下一自然 Gate 是新的 Fresh Context Planning/Readiness decision。stable Main ListItem、230 deferred problem Articles、6 source-defect Articles及其他候选都必须保持独立，只有重新完成相应 Authority / `slice-work` / `readiness-check` 后才能进入 Execute；不得从 EU-52 自动继承或推导 successor authority。

## 10. Non-goals

- no stable ListItem implementation in EU-52;
- no Page/List Historical Migration fallback;
- no repair/import of 230 deferred or 6 source-defect Articles;
- no speculative Main-specific Runtime importer;
- no Public frontend technology change;
- no Party canonical rewrite;
- no `agentic-dev` baseline update;
- no automatic successor nomination after EU-52 future completion.
