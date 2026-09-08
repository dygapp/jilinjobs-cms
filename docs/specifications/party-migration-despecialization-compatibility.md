# Party Migration De-specialization & Compatibility Specification

## Authority

- `docs/requirements/party-migration-despecialization-compatibility.md`
- `docs/requirements/generic-content-migration-application.md`
- `docs/specifications/generic-content-migration-application.md`
- `data-migrations/README.md`
- GitHub Issue #92 / Issue #77

## Status

- Phase：**Phase 2C — Party Migration De-specialization & Compatibility**
- Specification：**READY**
- Technical Planning：**REQUIRED**
- Planning baseline：`main@bd5dbd84bafd7b731ca290bbd40fb135806cb086`
- Current Ready Execution Unit：**NONE until slice-work + readiness-check are integrated**

## 1. Required responsibility split

Phase 2C 的最终责任模型必须是：

```text
Party canonical dataset / compatibility authority
        ↓
Party bounded adapter / compatibility guard
        ↓
Generic Content Migration capability
        ↓
CMS Runtime
```

其中：

- Party dataset/profile负责 Party facts；
- Party adapter负责读取 Party-specific shape、验证 Party scope、转换为 neutral canonical model并应用唯一 accepted compatibility transition；
- Generic capability继续负责 site-neutral preflight、CREATE/SKIP/CONFLICT、Article/List Runtime mutation、resource handling、legacy mapping与report primitives；
- Generic package不得读取 Party compatibility file或知道 Party identity。

## 2. Party dataset/profile contract

### 2.1 Current scope

Current Party scope必须来自 repository-owned dataset：

- `manifest.sourceSystem`；
- `manifest.contentScope[*].columnAlias` / counts；
- Article index/item stable identity / current fingerprint；
- list index `listCode` / sourceSystem / items；
- list item legacyKey / sourceOrder / current fingerprint / source relation / image digest。

Party code可以验证这些字段之间一致，但不得用 Kotlin allow-list重新定义它们。

### 2.2 Compatibility authority

新增 `data-migrations/party/v1/compatibility.json`，格式保持 Party-specific、bounded、machine-readable。至少表达：

```text
compatibilityVersion
transitions[]
  kind = LIST_ITEM
  listCode
  sourceSystem
  legacyKey
  fromFingerprint
  toFingerprint
  fromSourceType
  toSourceType
  preserveRuntimeId
  requiredOldRuntimeState
```

当前只允许一个 accepted transition：EU-29 accepted position 2 LINK → current ARTICLE。没有 transition entry的 fingerprint变化全部保持 Generic CONFLICT。

`compatibility.json` 不定义 arbitrary expression / scripting / plugin hook。

## 3. Party Article normalization

Current Party Article JSON 与 Generic Article语义已经等价。Party adapter应：

1. 读取 manifest + `index.ndjson` + article units；
2. 验证 target alias属于 manifest contentScope；
3. 验证 index/article identity、fingerprint、alias/type一致；
4. 转换为 Generic canonical Article model；
5. 由 Generic preflight / importer执行 Runtime mutation。

Party adapter不得继续自行调用 `ArticleService` / `ResourceService` / Article mapping Mapper实现 CREATE/SKIP/CONFLICT。

Pinned EU-29 accepted dataset同样通过这一 neutral Article path导入；current 2 条 theme education只由 current dataset内容区别，不需要 `party-theme-education` / `zhutijiaoyu` Kotlin special importer。

## 4. Party List normalization

Current / pinned Party carousel canonical shape可以继续保留现有 evidence字段与 `articleRef` legacy field。Party adapter负责把它规范化为 Generic List model：

- missing `sourceType`按 accepted old dataset semantics解释为 LINK；
- `articleRef`映射为 Generic `articleReference`；
- legacy provenance URL与 Runtime LINK URL分离；ARTICLE relation不把 legacy URL误当 Runtime URL；
- image source URL / snapshot path / SHA-256 / content type / size进入 Generic image model；
- Party-specific evidence只用于 Party validation / provenance，不进入 Generic policy。

Adapter必须验证：

- list code / source system与 dataset一致；
- sourceOrder / identity / fingerprint与 index一致；
- current list item count/order来自 current index；
- old pinned dataset count/order来自其自身 index；
- ARTICLE stable reference可解析。

## 5. Generic prepared-dataset contract

为避免 Party adapter复制 Generic Runtime pipeline，Generic capability可以增加一个 neutral internal entry，接受已经经过 canonical file/path/byte validation的 prepared/loaded dataset，并复用同一 Runtime preflight + execute pipeline。

约束：

1. prepared entry必须是 site-neutral Kotlin contract，不含 Party types；
2. Generic raw snapshot loader与 Party adapter必须共享必要的 neutral file containment / digest verification helper，避免两套安全语义漂移；
3. prepared dataset进入 Generic runtime preflight后，CREATE/SKIP/CONFLICT与dependency semantics必须与 `generic-content` raw snapshot path一致；
4. Generic package purity Gate继续禁止 Party / EU29 / EU30 identity。

## 6. Provenance / static projection preservation

Party current behavior应尽量保持已有 migration provenance与Runtime image projection。

Generic list prepared model允许由 caller提供两个 site-neutral optional inputs：

- `sourceProvenanceUrl`：仅用于 legacy mapping/provenance，不等同于 Runtime LINK URL；
- safe normalized `staticTarget`：仅用于 LINK image projection；未提供时继续使用 EU-47 generic deterministic default。

`staticTarget`必须经过 Generic safe relative-path validation，不允许 absolute path、`..`、empty segment或跳出 configured static root。

Party compatibility authority/adapter可以为 accepted carousel LINK image提供现有 `migrated/party/carousel/<sha256>.<ext>` target，从而保持 accepted path；该字面值不得进入 `migration/generic/**`。

## 7. Steady-state import contract

### 7.1 Articles

Current `party-content`：

- normalizes all current Party Articles；
- delegates to Generic Article preflight/execute；
- first Fresh DB = 183 CREATED；
- second = 183 SKIPPED；
- unexpected fingerprint drift = CONFLICT；
- wrapper映射为 existing `EU29_IMPORT_REPORT` shape/label。

### 7.2 Carousel

Current `party-carousel` 在没有 accepted old transition需要执行时：

- normalizes 4 current items；
- delegates create/skip/conflict to Generic List capability；
- Fresh DB = 4 CREATED；
- second = 4 SKIPPED；
- unexpected current mapping fingerprint drift = CONFLICT；
- wrapper投影 existing carousel report，其中 `updated=0`。

## 8. Accepted upgrade-only contract

当 current `party-carousel`检测到 mapping fingerprint与 current canonical不一致时：

1. 先查找 compatibility authority是否存在 exact identity + fromFingerprint + toFingerprint transition；
2. 不存在 → CONFLICT；
3. 存在 → 验证 old Runtime guard；
4. 验证 current target Article mapping与current canonical image bytes；
5. 所有 guard PASS才允许原位 update；
6. update必须保持 existing list-item id；
7. mapping/provenance更新到 current canonical；
8. transition结果在 Party report中为 `UPDATED`；
9. subsequent Generic steady-state classification必须为 SKIP。

Compatibility update可以直接使用必要 Core read/write Mapper，但只能存在于 Party compatibility source中，并且只能由 exact profile transition授权。

## 9. Legacy baseline compatibility

EU-30 Upgrade Verification继续固定使用 accepted commit：

`59c855f55899cd613fdee059b27db762ffa3b092`

Current Phase 2C code必须能够读取该 pinned dataset建立 accepted EU-29 Runtime：181 Articles + 4 LINK carousel。由于 pinned dataset早于新的 compatibility file，upgrade verifier应显式提供 current repository的 `compatibility.json`作为 transition authority；不得修改 pinned archive bytes或把它复制成新的长期 baseline。

## 10. Command/report contract

External repository engineering interfaces保持：

```text
party-content <snapshot-root>
party-carousel <snapshot-root>
./gradlew importPartyHistoricalContent
./gradlew importPartyCarousel
EU29_IMPORT_REPORT
EU29_CAROUSEL_IMPORT_REPORT
```

Party wrapper report字段保持 current verification consumer兼容。Generic command/report继续是：

```text
generic-content
importCanonicalContent
CONTENT_MIGRATION_REPORT
```

Phase 2C 不要求公开新的用户-facing CLI。

## 11. Source ownership result

Phase 2C完成后：

```text
migration/
├── LegacyMappings.kt
├── generic/**                  # site-neutral only
└── party/** or equivalent      # dataset adapter + compatibility guard + wrapper report
```

原 `PartyHistoricalContentMigration*` / `PartyCarouselMigration*` 中重复的 Runtime importer职责应删除或收敛为薄 compatibility/adaptation layer；不得保留一套长期“Party direct importer”与 Generic importer并行执行相同 steady-state写入。

## 12. Verification mapping

Final exact-head至少需要：

1. **Party de-specialization focused verification**：
   - current dataset facts来自 manifest/index/compatibility file；
   - Article/List current path委托 Generic pipeline；
   - Generic package source purity持续 PASS；
   - adapter path/digest/provenance normalization负例；
2. **Canonical Migration Verification**：current 183 + 4 Fresh DB first/second import、resource/runtime reconciliation；
3. **EU-30 Migration Upgrade Verification**：pinned EU-29 → current exact one UPDATE + post-upgrade idempotency + drift rejection；
4. **Generic Content Migration Verification**：raw generic path未被 Party adoption破坏；
5. **Backend Application Boundary Verification**；
6. **Site Package Verification**；
7. **Repository CI**，包括 Integrated feature/admin browser；
8. unresolved review threads = 0。

Focused verification还必须证明：

- 删除/修改 compatibility transition → expected conflict；
- wrong fromFingerprint / old runtime source type / list id / image digest / target article mapping → conflict / invalid，无 silent update；
- Generic raw changed fingerprint仍不可 update。

## 13. Non-goals

- 不进入 Main Site migration；
- 不执行 Phase 3 re-entry verdict；
- 不修改 DB schema/API/frontend/Site Package product behavior；
- 不设计通用 migration policy engine；
- 不改变 operator normal edit contract；
- 不拆仓、不升级 `agentic-dev` baseline。

## 14. Specification readiness

Current dataset、old accepted commit、Generic Engine与existing upgrade workflow提供了完整可观测输入。Party authority、normalization、prepared Generic entry、compatibility guard、report preservation与verification均已定义为可测试 contract。

本 Specification **READY**，且 Technical Planning **REQUIRED**。