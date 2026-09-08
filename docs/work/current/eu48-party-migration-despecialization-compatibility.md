# EU-48 — Party Migration De-specialization & Compatibility

## Status

- Parent：GitHub Issue #92
- Related architecture authority：GitHub Issue #77
- Phase：**Phase 2C — Party Migration De-specialization & Compatibility**
- Candidate formed by：`slice-work`
- Readiness：**PASS**
- Execute state：**NOT STARTED**
- Planning baseline：`main@bd5dbd84bafd7b731ca290bbd40fb135806cb086`
- Planning branch：`planning/phase2c-party-migration-despecialization`
- Requirement：`docs/requirements/party-migration-despecialization-compatibility.md`
- Specification：`docs/specifications/party-migration-despecialization-compatibility.md`
- Technical Plan：`docs/technical/party-migration-despecialization-compatibility.md`

EU-48 identifier由 Phase 2C `slice-work`形成。它不继承 EU-47 或更早 Unit 的 Execute Authority。只有本 planning/readiness change 集成到 `main` 后，在新的 Fresh Context重新核验 integrated `main`、Issue #92 / #77、EU-48 Authority、Open PR / Actions 与 base drift，且没有 Authority change / blocker时，才允许建立 EU-48自身 Execute baseline并进入 Execute。

## 1. Dependency Closure

基于 planning baseline `main@bd5dbd84bafd7b731ca290bbd40fb135806cb086`：

1. EU-46 application/core boundary与 EU-47 Generic Engine均已完成并取得 Post-Integration Current Evidence。
2. Party current Article importer仍复制 Generic Article create/skip/conflict/resource逻辑，并以 Kotlin常量维护 Party alias / theme special case。
3. Party current Carousel importer仍复制 Generic List逻辑，并 hardcode list code、legacy pattern、fixed count、Party static path与 accepted position-2 fingerprints。
4. current Party Article canonical shape已可无损规范化为 Generic Article；carousel shape需要 bounded Party adapter处理 legacy `sourceType` default、`articleRef`与provenance fields。
5. `manifest.json` / Article index / list index/items已经持有 current Party scope、identity与current fingerprints；Kotlin无需重复维护。
6. 唯一无法从 current dataset推导的事实是 pinned EU-29 accepted position-2 old-state transition permission / `fromFingerprint`；应新增 bounded Party compatibility authority，current target fingerprint/type/relation仍由 current canonical item持有。
7. Generic Engine需要的唯一扩展是 site-neutral prepared-dataset entry / shared byte verifier / optional provenance-static-target metadata；不需要 policy framework、new app/module或 DB schema change。
8. pinned EU-29 accepted commit `59c855f55899cd613fdee059b27db762ffa3b092` 与现有 EU-30 Upgrade workflow已经提供可重复的 old-runtime compatibility proof。
9. Canonical / Upgrade / Generic / Boundary / Site Package / Repository CI均已有稳定 verification entry。
10. Consumer-local Method足够；无需 `agentic-dev` baseline upgrade。

没有未决产品事实、架构选择或人工输入 blocker。

## 2. Slice-work Result

Phase 2C形成单一 Candidate：**EU-48 — Party Migration De-specialization & Compatibility**。

保持为一个 Unit，因为：

- Party steady-state adoption依赖 Generic prepared entry；
- 删除 direct Party Runtime importer前必须同时证明 fresh current import与 pinned old upgrade；
- accepted transition policy只有与 current dataset/profile和 upgrade verification一起集成才可验收；
- 拆分会留下无独立价值的双 importer或暂时破坏 upgrade path。

整个未来 EU-48 implementation PR作为 rollback / integration boundary。

## 3. Execute Scope

### 3.1 Party compatibility authority

新增 `data-migrations/party/v1/compatibility.json`，只持有 accepted old→current transition中 current canonical dataset无法表达的最小历史事实：transition identity、accepted old `fromFingerprint` / source type与 preserve-runtime-id / old-state guard requirement。

Current aliases/counts/current fingerprints、target source type、target Article relation与current image digest继续由 manifest/index/items持有，不复制到 Kotlin常量或 compatibility file。

### 3.2 Generic thin extension

允许在 `migration/generic/**`做 bounded site-neutral refactor：

- raw loader → prepared dataset → existing preflight/execute/report；
- shared root/path/size/SHA verification；
- prepared list optional source provenance URL / safe static target；
- Generic raw behavior/purity保持。

不得添加 Party identity / accepted fingerprint / upgrade exception。

### 3.3 Party adapter / facade

以 `migration/party/**`或等价 ownership替换 current direct importer：

- Party manifest/index/item validation；
- Article/List canonical shape normalization；
- delegate steady-state Runtime mutation to Generic Engine；
- existing Party command/Gradle task/report compatibility projection；
- stable Party carousel static projection rule可以保留在 bounded Party adapter source，不进入 Generic package或 compatibility authority。

### 3.4 Accepted compatibility transition

只允许 compatibility authority列出的 exact position-2 old LINK state进入原位 update：

- exact transition identity + `fromFingerprint` / old source type；
- exact old Runtime guard；
- current target fingerprint / source type / Article stable mapping / image digest全部从 current canonical item读取并验证；
- preserve list-item id；
- any drift → CONFLICT；
- update后 Generic classification = SKIP。

不增加 general update语义到 Generic Engine。

### 3.5 Verification

新增 focused `verifyPartyMigrationDespecialization` + focused workflow，并保持：

- Canonical Migration Verification；
- EU-30 Migration Upgrade Verification；
- Generic Content Migration Verification；
- Backend Application Boundary Verification；
- Site Package Verification；
- Repository CI / Integrated Browser；
- unresolved review threads = 0。

## 4. Preserved Boundary / Non-goals

- 不修改 DB schema / Flyway；
- 不改变 Admin/Public API、页面或视觉；
- 不修改 Site Package semantics；
- 不开始 Main Site historical migration；
- 不进入 Phase 3 re-entry verdict或 Issue #60 / E1～E3；
- 不建立 generic policy DSL / plugin framework；
- 不拆 Repository；
- 不更新 `agentic-dev` baseline。

Current Party source content/resource bytes应保持；新增 compatibility authority属于 additive migration governance data。

## 5. Acceptance Obligations

### A. Authority ownership

- Party aliases/current identities/current fingerprints来自 dataset；
- accepted old-state transition permission / `fromFingerprint`来自 compatibility authority；current transition target事实来自 current canonical item；
- Generic package无 Party hardcode。

### B. Generic adoption

- Party Article current path不再直接实现 Article/Resource Runtime mutation；
- Party List current steady-state path不再直接实现 Generic create/skip/conflict；
- Party adapter只做 validation/normalization/compatibility/report projection。

### C. Current steady-state

- Fresh DB current Party first import = 183 Articles + 4 carousel created；
- second import = all skip；
- current unexpected fingerprint drift = conflict；
- resource bytes / Runtime reconciliation保持。

### D. Pinned upgrade compatibility

- current code可导入 pinned EU-29 accepted 181 + 4 old baseline；
- current canonical只对 position 2产生一个 accepted UPDATE；
- Runtime list-item id保持；
- final ARTICLE relation/image/current fingerprint正确；
- subsequent current run全 SKIP；
- wrong fingerprint/runtime guard/target dependency拒绝 update。

### E. Repository regression

Generic / Boundary / Site Package / Repository CI / Integrated Browser exact-head PASS；no schema/API/frontend product change。

## 6. Readiness Check

- **Authority / Intent：PASS** — Phase 2C goal与 non-goals由 Issue #92/#77/Roadmap、Ready Requirement/Specification清晰定义。
- **Specification：PASS** — Party authority、normalization、Generic adoption、accepted transition与report contract均可测试。
- **Technical Planning：PASS** — current code/data/workflows已经确认具体 ownership与最低复杂度实现路径。
- **Slice Integrity：PASS** — 单一 Unit原子闭合 steady-state adoption + pinned upgrade compatibility；无合理独立半单元。
- **Dependency Closure：PASS** — EU-46/EU-47 prerequisites completed；无需 schema、new app/module、external service或 baseline upgrade。
- **Verification Feasibility：PASS** — existing Fresh DB Canonical、pinned EU-29 Upgrade、Generic/Boundary/Site/CI gates加一个 focused verifier即可闭环。
- **Scope / Rollback：PASS** — one implementation PR、additive compatibility authority、no irreversible schema migration。
- **Current baseline evidence：PASS** — `main@bd5dbd84...` 是 EU-47 integration；Post-Integration Generic #14、Boundary #17、Site Package #59、CI #848（含 Integrated feature/admin browser）全部 success，且 Issue #92/#77已记录 closure Current Evidence。

**Readiness Decision：PASS — EU-48 is a Ready Execution Unit once this planning/readiness change is integrated.**

## 7. Execute Gate

Planning/Readiness integration本身不建立可继承 Execute baseline。后续新的 Fresh Context必须：

1. 重新读取 current `main`、Open PR / Issue、最近相关 Actions；
2. 读取 `AGENTS.md`、`README.md`、`docs/README.md`、Roadmap、Development Method；
3. 读取 Issue #92 / #77、本 Requirement / Specification / Technical Plan / Work Artifact；
4. revalidate EU-48 Readiness与base drift；
5. 无 Authority change / blocker时才建立 EU-48自身 Execute baseline。

EU-48不得向 Phase 3或 Issue #60 / E1～E3传递 Execute Authority。