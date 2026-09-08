# Current Work

当前 Ready Execution Unit：**EU-50 — Main Source Discovery & Accepted Snapshot Promotion**。

E3 — Main Historical Content Collection & Canonical Migration 已在 `main@e6fe7674398ad8c29fa7ff1d62eb500754a66cc8` 完成 Fresh Context Planning / source-evidence recovery、必要 Technical Planning、`slice-work` 与当前可判定的 `readiness-check`：

- E3 Requirement：**READY**；
- E3 Specification：**READY**；
- Technical Plan：`../../technical/main-historical-content-migration.md` — **READY**；
- 当前 source evidence 证明 Legacy Main Source 可达、规模大且包含 internal/external、Page 与资源等混合形态，因此保留两个独立 rollback / verification boundary；
- `slice-work` 形成两个 Candidate Execution Units：EU-50 与 EU-51；
- EU-50 `readiness-check`：**PASS**；
- EU-51 `readiness-check`：**PENDING / blocked by EU-50 accepted snapshot**。

当前 Unit：

- `eu50-main-source-discovery-promotion.md` — **READY**；
- `eu51-main-canonical-import-review.md` — **CANDIDATE / NOT READY**。

EU-50 只负责 bounded Legacy Source discovery、completeness/classification、accepted snapshot promotion 与 offline verification；不承担 Runtime import 或最终 Human Review。EU-51 只有在 EU-50 的 repository-owned accepted Main canonical snapshot 集成后，才能基于实际 accepted counts/digests/exception set重新进行 Fresh Context `readiness-check`。

**EU-50 当前尚未建立 Execute baseline。** 本 Planning/Readiness 状态必须先完成 PR Integration；随后从 integrated `main` 重新核验 Issue #60 / #77、Open PR / Actions、本 Unit Authority 与 base drift，才能建立 EU-50 独立 Execute baseline并开始 Main source collection / canonical promotion。不得继承 EU-49、EU-48、Phase 3 或 E1 的 Execute Authority。

E2 / EU-49 completed evidence继续位于：

`../archive/eu49-page-content-migration-foundation.md`

Issue #77 继续作为四层长期 Architecture Authority；C1 / C2、Issue #57 / #59 与 Repository Split Readiness Assessment 保持独立候选。