# Current Work

Current Ready Execution Unit：**NONE**。

E3 — Main Historical Content Collection & Canonical Migration 当前状态：

- E3 Requirement：**READY / ACTIVE**；
- E3 Specification：**READY / ACTIVE**；
- Technical Plan：`../../technical/main-historical-content-migration.md` — **READY / ACTIVE**；
- EU-50 — Main Source Discovery & Accepted Snapshot Promotion：**COMPLETED / Execute Authority TERMINATED**；completed evidence 位于 `../archive/eu50-main-source-discovery-promotion.md`；
- EU-50 accepted current Article subset 已集成到 `data-migrations/main/v1/**`；
- EU-51 — Main Canonical Import, Runtime Reconciliation & Human Review：**Candidate / NOT READY**；EU-50 dependency 已满足，但 post-integration `readiness-check` 尚未运行；
- Main Page / stable ListItem source handoff 属于独立 Site Package Planning/Readiness path；
- 230 篇 deferred problem Articles 与 6 篇 source-defect Articles 保持后置单独处理，不属于 current import input，也不阻断当前项目进程。

`docs/work/current/` 当前不包含 active Execution Unit artifact。新的 Work artifact 只有在当前 Authority 完成必要 dependency closure / `slice-work` 且 `readiness-check` PASS 后才能进入本目录。

下一自然 Gate 是从 integrated `main` Fresh Context 恢复 Issue #60 / #77、E3 Authority、EU-50 archived evidence 与 accepted canonical dataset，重新判断 downstream Planning / Readiness。不得继承 EU-50、EU-49、EU-48、Phase 3 或 E1 的 Execute Authority，也不得因为 canonical dataset 已存在就直接开始 EU-51 Runtime import。

E2 / EU-49 completed evidence继续位于：

`../archive/eu49-page-content-migration-foundation.md`

Issue #77 继续作为四层长期 Architecture Authority；C1 / C2、Issue #57 / #59 与 Repository Split Readiness Assessment 保持独立候选。
