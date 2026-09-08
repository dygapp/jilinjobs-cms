# Main Site Formal Content E1～E3 Planning

## Status

- Parent Planning Authority: GitHub Issue #60
- Initial planning baseline: `main@f42bacf4ab7719e3291288c77f0685b428b86141`
- Phase 3 re-entry: **PASS**
- E1: **Planning / Authority closure — NO implementation EU**
- E2: **COMPLETED via EU-49**
- Current Ready Execution Unit: **NONE**
- E3: **Requirement / Specification READY as Planning Authority; no Identifier / Ready / Execute Authority**
- Scope: Issue #60 E1 / E2 / E3 only

## 1. Planning intent

Issue #92 Phase 0～Phase 3 已完成前置 Repository / Migration Architecture 收敛。E1～E3 已完成第一轮 dependency closure：E1 以 Authority-only closure 收口，E2 形成并完成 EU-49，E3 当前进入下一轮 Planning recovery，而不是自动进入 Execute。

任何后续 E3 工作都必须重新从 current GitHub Repository 恢复真实 source evidence、当前 Authority 与依赖状态，再按 Consumer-local Method执行必要的 dependency closure、`slice-work` 与 `readiness-check`。EU-49 completion只满足 foundation prerequisite，不授予 E3 Identifier、Ready 状态或 Execute Authority。

## 2. Accepted dependency chain

```text
E1  Main External-link Ownership & Behavior Boundary
    └─ Planning / Authority closure；no implementation EU
          ↓
E2  Main Single-page Formal Content Boundary
    └─ EU-49 Page Operational Content Ownership & Migration Foundation — COMPLETED
          ↓ foundation prerequisite satisfied
E3  Main Historical Content Collection & Canonical Migration
    └─ Fresh Context Planning / source-evidence recovery
          ↓
       dependency closure → slice-work → readiness-check
          ↓
       only then may a new Ready Execution Unit enter Execute
```

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

Completed evidence：

`docs/work/archive/eu49-page-content-migration-foundation.md`

EU-49 Execute Authority 已终止。

## 5. E3 current planning boundary

E3 Current Authority：

- Requirement：`docs/requirements/main-historical-content-migration.md`
- Specification：`docs/specifications/main-historical-content-migration.md`

E3 继续冻结后续需要证明的 contract：

- source discovery completeness / unresolved classification；
- accepted source snapshot / provenance；
- Main historical Article / `EXTERNAL_LINK` Article；
- Article resources / attachments / body references；
- Main formal Page content；
- historical operational ListItem（仅在 source evidence证明属于迁移范围时）；
- canonical organization与 stable target identity；
- Fresh DB import、second-run idempotency、fingerprint conflict、Runtime reconciliation；
- Human Review。

EU-49 completion 后，E3 的 foundation prerequisite 已满足，但以下仍**没有发生**：

- 没有为 E3 分配 EU Identifier；
- 没有形成 Candidate / Ready Execution Unit；
- 没有 Execute baseline / Execute Authority；
- 没有开始 Main source collection；
- 没有 promotion 临时 evidence 为 canonical dataset；
- 没有冻结未经 repository evidence接受的 Article / Page / List 数量。

## 6. Next gate

下一自然 Gate：**E3 Fresh Context Planning / source-evidence recovery**。

恢复时至少重新确认：

1. current `main`、Open PR / Issue 与最近相关 Actions；
2. `AGENTS.md`、Root `README.md`、`docs/README.md`、Roadmap、Development Method；
3. Issue #60 / #77；
4. E3 Requirement / Specification；
5. current Main stable targets、Generic migration capability与实际 source-evidence availability；
6. 是否存在必须先形成 source discovery/evidence-promotion slice，再形成 canonical migration/import slice的真实 rollback / verification boundary。

完成 recovery 后，只能按当前证据执行必要 planning。`slice-work` 形成 Candidate 后仍需 `readiness-check = PASS` 才能成为新的 Ready Execution Unit；不得从 EU-49、E1、Phase 3 或 Issue #60 编号继承 Execute Authority。

## 7. Non-goals

- 不修改 Party accepted content / compatibility；
- 不修改 Public frontend technology；
- 不进入 C1 / C2 / Issue #57 / #59；
- 不执行 Repository split；
- 不把 Main historical data塞回 Flyway、Site stable structure或one-time bootstrap；
- 不为“统一外链”新增通用 Link实体；
- 不在没有 source evidence时预设 Main item count；
- 不更新 `agentic-dev` baseline。
