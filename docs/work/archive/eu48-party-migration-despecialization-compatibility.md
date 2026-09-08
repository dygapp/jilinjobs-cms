# EU-48 — Party Migration De-specialization & Compatibility

## Status

- Parent：GitHub Issue #92
- Related architecture authority：GitHub Issue #77
- Phase：**Phase 2C — Party Migration De-specialization & Compatibility**
- Candidate formed by：`slice-work`
- Readiness：**PASS**
- Execute state：**IMPLEMENTED**
- Verification：**PASS — implementation exact-head; final integration head governed by PR #109 Actions**
- Planning baseline：`main@bd5dbd84bafd7b731ca290bbd40fb135806cb086`
- Planning branch：`planning/phase2c-party-migration-despecialization`
- Execute baseline：`main@4cd1d2ea7e9d1e584561f4b3b40a0d44d74b0169`
- Implementation branch：`feature/eu-48-party-migration-despecialization`
- Implementation verification head：`6c78a371f5bb1a3967b940215d19dcb6f8dcd38e`
- Pull Request：GitHub PR #109
- Requirement：`docs/requirements/party-migration-despecialization-compatibility.md`
- Specification：`docs/specifications/party-migration-despecialization-compatibility.md`
- Technical Plan：`docs/technical/party-migration-despecialization-compatibility.md`
- Integration：**PENDING PR #109 merge at this archive snapshot**
- Post-Integration Current Evidence：**PENDING GitHub Issue #92 / #77 record at this archive snapshot**

EU-48 identifier由 Phase 2C `slice-work`形成。它不继承 EU-47 或更早 Unit 的 Execute Authority；planning/readiness integration 后通过 Fresh Context重新核验 integrated `main`、Issue #92 / #77、Readiness、Open PR / Actions 与 base drift，以 `main@4cd1d2ea7e9d1e584561f4b3b40a0d44d74b0169` 独立取得本 Unit Execute Authority。本文归档快照记录 implementation / pre-integration verification；最终 merge 与 Post-Integration 状态以 GitHub PR #109、Actions 与 Issue #92 / #77 Current Evidence 为准，不向 Phase 3 或 Issue #60 / E1～E3 传递 Execute Authority。

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

整个 PR #109 是本 Unit rollback / integration boundary。

## 3. Execute Scope

### 3.1 Party compatibility authority

新增 `data-migrations/party/v1/compatibility.json`，只持有 accepted old→current transition中 current canonical dataset无法表达的最小历史事实：transition identity、accepted old `fromFingerprint` / source type与 preserve-runtime-id / old-state guard requirement。

Current aliases/counts/current fingerprints、target source type、target Article relation与current image digest继续由 manifest/index/items持有，不复制到 Kotlin常量或 compatibility file。

### 3.2 Generic thin extension

在 `migration/generic/**`做 bounded site-neutral refactor：

- raw loader → prepared dataset → existing preflight/execute/report；
- shared root/path/size/SHA verification；
- prepared list optional source provenance URL / safe static target；
- Generic raw behavior/purity保持。

未添加 Party identity / accepted fingerprint / upgrade exception。

### 3.3 Party adapter / facade

以 `migration/party/**`替换 current direct importer Runtime ownership：

- Party manifest/index/item validation；
- Article/List canonical shape normalization；
- delegate steady-state Runtime mutation to Generic Engine；
- existing Party command/Gradle task/report compatibility projection；
- stable Party carousel static projection rule保留在 bounded Party adapter source，不进入 Generic package或 compatibility authority。

### 3.4 Accepted compatibility transition

只允许 compatibility authority列出的 exact position-2 old LINK state进入原位 update：

- exact transition identity + `fromFingerprint` / old source type；
- exact old Runtime guard；
- current target fingerprint / source type / Article stable mapping / image digest全部从 current canonical item读取并验证；
- preserve list-item id；
- any drift → CONFLICT；
- update后 Generic classification = SKIP。

没有增加 general update语义到 Generic Engine。

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

Current Party source content/resource bytes保持；新增 compatibility authority属于 additive migration governance data。

## 5. Acceptance Closure

### A. Authority ownership — PASS

- Party aliases/current identities/current fingerprints来自 dataset；
- accepted old-state transition permission / `fromFingerprint`来自 compatibility authority；current transition target事实来自 current canonical item；
- Generic package无 Party hardcode。

### B. Generic adoption — PASS

- Party Article current path不再直接实现 Article/Resource Runtime mutation；
- Party List current steady-state path不再直接实现 Generic create/skip/conflict；
- Party adapter只做 validation/normalization/compatibility/report projection；
- Generic prepared entry继续复用同一 preflight/execute semantics。

### C. Current steady-state — PASS

- Fresh DB current Party first import = 183 Articles + 4 carousel created；
- second import = all skip；
- unexpected current fingerprint drift = conflict；
- resource bytes / Runtime reconciliation保持。

### D. Pinned upgrade compatibility — PASS

- current code可导入 pinned EU-29 accepted 181 + 4 old baseline；
- current canonical只对 position 2产生一个 accepted UPDATE；
- Runtime list-item id保持；
- final ARTICLE relation/image/current fingerprint正确；
- subsequent current run全 SKIP；
- wrong fromFingerprint、Runtime source type/order/url/image path/digest、missing transition、target Article dependency drift均拒绝 update且不改变 Runtime。

### E. Repository regression — PASS at implementation verification head

Implementation verification head `6c78a371f5bb1a3967b940215d19dcb6f8dcd38e` 已取得：

- Party Migration De-specialization Verification #2 / run `34185473620` — **PASS**；
- Canonical Migration Verification #188 / run `34185473590` — **PASS**；
- EU-30 Migration Upgrade Verification #138 / run `34185473584` — **PASS**；
- Generic Content Migration Verification #16 / run `34185473576` — **PASS**；
- Backend Application Boundary Verification #19 / run `34185473613` — **PASS**；
- Site Package Verification #61 / run `34185473607` — **PASS**；
- Repository CI #856 / run `34185473564` — **PASS**，包含 Backend / Public / Admin / Integrated existing-feature + admin browser；
- unresolved review threads：**0**。

Review Environment只作为 supporting evidence，不替代 required exact-head Gate。

## 6. Rollback / side effects

Rollback boundary：整个 EU-48 implementation PR #109。

本 Unit没有 schema migration；数据变更只增加 repository-versioned compatibility authority。Focused runtime verifier只使用 Fresh MySQL、temp storage/static/snapshot roots。Compatibility conflict paths验证 no in-place Runtime update；successful accepted transition仅存在于 verifier和实际 authorized historical upgrade path。

## 7. Readiness Decision

**PASS — EU-48 became the Ready Execution Unit after planning/readiness integration and Fresh Context revalidation.**

Execute baseline为 `main@4cd1d2ea7e9d1e584561f4b3b40a0d44d74b0169`；implementation在该 baseline无 drift前提下完成。

## 8. Execute / Verification Record

### 8.1 Implementation result

- 新增 Party bounded adapter / facade 与 exact compatibility service；
- Party steady-state Article/List Runtime mutation委托 Generic prepared dataset pipeline；
- 新增 `data-migrations/party/v1/compatibility.json` bounded old-state authority；
- Generic Engine增加 prepared dataset/shared verifier/provenance/static-target薄能力，保持 site-neutral purity；
- 旧 Party direct importer source收敛为 accepted on-disk DTO / facade compatibility，不再维护第二套 Article/List mutation pipeline；
- existing Party dispatcher / Gradle task / report labels保持；
- DB schema/Flyway、frontend、Site Package、current canonical Article/list/resource bytes均未改变。

Actions收敛期间增加了真实 MySQL compatibility runtime guard focused verification，覆盖成功 transition 与多个 drift拒绝场景；该测试增强仍完全属于 EU-48 Acceptance / Verification scope。

### 8.2 Integration boundary

PR #109 是整个 EU-48 rollback / integration boundary。合并前必须确认：

- current `main` 仍与 Execute baseline无 base drift；
- unresolved review threads = 0；
- final PR head required Actions全部 PASS；
- final compare只包含 Phase 2C implementation / compatibility authority / focused verification / Authority closure；
- no DB schema/Flyway、frontend或 Site Package product change。

Authority closure文档加入 final Head 后，所有受影响 required exact-head Gate必须重新取得 Current Evidence；不能机械复用 implementation verification head。

合并后必须在 integrated `main`取得实际 push-trigger Post-Integration evidence，并向 Issue #92 / #77写入 Current Evidence。

### 8.3 Authority termination / next gate

EU-48 integration closure后：

- Issue #92 Phase 2C：**COMPLETED via EU-48**；
- Current Ready Execution Unit：**NONE**；
- EU-48 Execute Authority：**TERMINATED**；
- 下一 Gate：**Phase 3 — Canonical Migration Compatibility & E1～E3 Re-entry planning / compatibility gate only**；
- Phase 3不得继承 EU-48或更早 Unit的 Execute Authority；
- Issue #60 / E1～E3继续 blocked，Phase 3 re-entry PASS 前不得进入 Execute；
- 本 archive artifact只承担 historical evidence / traceability，不授予后续 Execute Authority。
