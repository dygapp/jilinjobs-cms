# Main Site Formal Content E1～E3 Planning

## Status

- Parent Planning Authority: GitHub Issue #60
- Initial planning baseline: `main@f42bacf4ab7719e3291288c77f0685b428b86141`
- E3 planning/readiness baseline: `main@e6fe7674398ad8c29fa7ff1d62eb500754a66cc8`
- Phase 3 re-entry: **PASS**
- E1: **Planning / Authority closure — NO implementation EU**
- E2: **COMPLETED via EU-49**
- E3 Technical Planning: **READY**
- `slice-work`: **EU-50 + EU-51 formed**
- Current Ready Execution Unit: **EU-50**
- EU-51: **Candidate / Readiness PENDING on EU-50 accepted snapshot**
- Scope: Issue #60 E1 / E2 / E3 only

## 1. Planning intent

Issue #92 Phase 0～Phase 3 已完成前置 Repository / Migration Architecture 收敛。E1 以 Authority-only closure 收口，E2 通过 EU-49 完成 Page foundation；E3 在 EU-49 Post-Integration closure 后重新从 current GitHub Repository恢复真实 source evidence、当前 Authority 与依赖状态，并完成了必要 Technical Planning、`slice-work` 与当前可判定的 `readiness-check`。

本轮并未把整个 E3 一次性授权执行。真实 source evidence 证明 Legacy Main Source 规模大、包含 INTERNAL / EXTERNAL_LINK、正式 Page、资源与运营列表等混合形态，因此 E3 保留两个独立 rollback / verification boundary：EU-50 负责外网 discovery / accepted snapshot promotion；EU-51 只消费已集成 accepted snapshot做离线 Runtime import / reconciliation / Human Review。

## 2. Accepted dependency chain

```text
E1  Main External-link Ownership & Behavior Boundary
    └─ Planning / Authority closure；no implementation EU
          ↓
E2  Main Single-page Formal Content Boundary
    └─ EU-49 Page Operational Content Ownership & Migration Foundation — COMPLETED
          ↓ foundation prerequisite satisfied
E3  Main Historical Content Collection & Canonical Migration
    ├─ EU-50 Main Source Discovery & Accepted Snapshot Promotion
    │    └─ READY after Planning/Readiness integration; Execute baseline still requires Fresh Context
    │          ↓ accepted repository-owned snapshot
    └─ EU-51 Main Canonical Import, Runtime Reconciliation & Human Review
         └─ Candidate / Readiness PENDING until EU-50 integration
```

EU-50 / EU-51 不共享 Execute Authority。任何后继 Unit仍需从 integrated `main`建立自己的 Fresh Context Execute baseline。

## 3. E1 accepted result

E1 Requirement / Specification 已冻结现有多种外链载体的 ownership 与行为边界：

- Article `EXTERNAL_LINK`：Article owns title + source URL；
- Navigation `LINK`：Navigation owns label + target + open mode；
- CmsList `LINK`：ListItem owns presentation title/URL/open mode；
- CmsList `ARTICLE`：Article owns canonical target；ListItem负责 placement；
- Advertisement：Advertisement owns campaign target/open mode；
- fixed integration：工程 owns genuinely fixed third-party seam；
- Historical external content/list members：Historical Content Migration owns provenance/fingerprint/import。

Repository audit 未发现独立 implementation gap，因此 `slice-work = NO CANDIDATE EXECUTION UNIT`。E1 不向 E2/E3 传递 Execute Authority。

Current Authority：

- `docs/requirements/main-external-link-boundary.md`
- `docs/specifications/main-external-link-boundary.md`

## 4. E2 / EU-49 completed result

E2 第一实现 slice：

**EU-49 — Page Operational Content Ownership & Migration Foundation**。

EU-49 已完成：

1. ordinary Site Package reconcile 对 existing Page 停止覆盖 operator-owned `bodyHtml / renderMode / embedUrl`，Fresh create仍保留 package defaults；
2. Core 新增窄的 Page content-only update capability，并复用现有 sanitizer / render-mode / embed validation；
3. Generic Content Migration 新增 stable `(groupAlias? + pageAlias)` Page canonical load/preflight/apply/mapping/report；
4. first apply 使用 exact target-content fingerprint guard；same-source rerun = SKIP；unexpected source / target / mapping drift = CONFLICT；
5. append-only Generic Flyway V3 增加 site-neutral Page migration mapping；
6. Article/List、Party compatibility、Backend boundary、Site Package 与 Repository CI regressions保持 PASS；
7. 没有采集 Main 原站 Page 正文，没有创建 Main canonical dataset，没有进入 E3。

Integration：

- PR #112；
- final Head `a59fef41ab5061175be276df66048d3dbcdac320`；
- integrated main `18da735c654c1a5d1310fe6db7e8e98f6f7b0026`；
- final/integrated tree `8b34416e20936f141ccbebaed5fa6df6439f5e19`；
- Post-Integration CI #868 / run `34200526862` = **PASS**。

Post-Integration Authority closure integrated at `main@e6fe7674398ad8c29fa7ff1d62eb500754a66cc8`; CI #870 / run `34204439580` = **PASS**。

Completed evidence：

`docs/work/archive/eu49-page-content-migration-foundation.md`

EU-49 Execute Authority 已终止。

## 5. E3 source-evidence recovery result

Current Authority：

- Requirement：`docs/requirements/main-historical-content-migration.md`
- Specification：`docs/specifications/main-historical-content-migration.md`
- Technical Plan：`docs/technical/main-historical-content-migration.md`

Fresh Context source recovery on 2026-09-08 confirmed:

- current reachable legacy content is primarily served from `https://24365.jl.smartedu.cn/`;
- `cms.jilinjobs.cn` currently redirects to that source;
- `www.jilinjobs.cn` was not a reliable collection endpoint in the current probe;
- `notice` currently reports 56 pages / 559 records;
- `jydt` currently reports 196 pages / 1,957 records;
- current source shapes mix INTERNAL / external targets and include real formal Page/resource content.

These counts are only source observations. They are deliberately **not** promoted to accepted migration totals by Planning. EU-50 Execute must establish complete surface/pagination reconciliation and only then promote repository-owned accepted counts/digests/identities.

Current repository capability audit also confirmed:

- stable Main Column aliases are already provided by Site Package;
- `HOME_CAROUSEL` and Main `SITE_LINKS` list codes exist as stable targets;
- Main Page identities exist as stable Site Package targets;
- Generic Content Migration already supports Article / ListItem / Page in the `generic-content` path;
- no Main-specific Runtime importer is justified by current evidence.

## 6. Slice-work / Readiness result

### EU-50 — Main Source Discovery & Accepted Snapshot Promotion

Current Work Authority：

`docs/work/current/eu50-main-source-discovery-promotion.md`

Scope is bounded to Legacy Source discovery/collection, completeness/classification, resource evidence, accepted snapshot promotion under `data-migrations/main/v1/**` and offline verification. It stops before Runtime product import/final Human Review.

`readiness-check = PASS` because Requirement / Specification / Technical Plan are Ready; E1/E2 dependencies and stable Main targets are closed; Generic canonical contracts exist; and current reachable source evidence is sufficient to execute discovery without needing final counts in advance.

EU-50 **does not yet have an Execute baseline on the planning branch**. The Planning/Readiness change must integrate first, then Fresh Context must revalidate integrated `main`, Issue #60/#77, Open PR/Actions, EU-50 Authority and base drift before Execute.

### EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review

EU-51 is a stable downstream Candidate formed by `slice-work`, tracked by this Planning Authority, the E3 Technical Plan and Roadmap. Because its `readiness-check` is PENDING / BLOCKED, it does not have a `docs/work/current/` artifact yet.

`readiness-check = PENDING / BLOCKED` because EU-50 has not yet integrated the accepted canonical snapshot. Exact accepted identities/counts/digests/resources/exception set are required to define EU-51's verification expectations and cannot be invented in Planning.

EU-50 completion will satisfy a dependency only; it will not automatically grant EU-51 Ready or Execute Authority.

## 7. Next gate

After this Planning/Readiness state is integrated, the next natural Gate is:

**EU-50 Fresh Context Execute-baseline recovery**.

At minimum revalidate:

1. integrated `main` and exact planning/readiness integration result;
2. Open PR / Issue and recent relevant Actions;
3. Issue #60 / #77;
4. E3 Requirement / Specification / Technical Plan;
5. `docs/work/current/eu50-main-source-discovery-promotion.md`;
6. current source reachability only as Execute evidence, not as a permanent Runtime dependency;
7. base drift and any new blocker.

Only after that recovery may EU-50 establish an Execute baseline and begin source collection / canonical promotion. EU-51 remains blocked until EU-50 accepted snapshot integration and its own Fresh Context readiness-check.

## 8. Non-goals

- 不修改 Party accepted content / compatibility；
- 不修改 Public frontend technology；
- 不进入 C1 / C2 / Issue #57 / #59；
- 不执行 Repository split；
- 不把 Main historical data塞回 Flyway、Site stable structure或one-time bootstrap；
- 不为“统一外链”新增通用 Link实体；
- 不在 Planning Authority 中把 current source observation count写成 accepted count；
- 不更新 `agentic-dev` baseline。
