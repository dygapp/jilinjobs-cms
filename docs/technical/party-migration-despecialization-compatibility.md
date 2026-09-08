# Party Migration De-specialization & Compatibility Technical Plan

## Authority

- `docs/requirements/party-migration-despecialization-compatibility.md`
- `docs/specifications/party-migration-despecialization-compatibility.md`
- `docs/technical/generic-content-migration-application.md`
- `data-migrations/README.md`
- GitHub Issue #92 / Issue #77

## Status

- Phase：**Phase 2C — Party Migration De-specialization & Compatibility**
- Planning baseline：`main@bd5dbd84bafd7b731ca290bbd40fb135806cb086`
- Dependency closure：**COMPLETE**
- Technical Plan：**READY**
- Current Ready Execution Unit：**NONE until slice-work + readiness-check are integrated**
- Architecture choice：**one bounded Party adapter/compatibility layer over the EU-47 Generic Engine; current Party dataset remains canonical authority; no generic policy framework**

## 1. Dependency closure findings

Current inventory confirms：

1. EU-47 Generic Engine already owns neutral Article/List models, file/digest preflight, Runtime target/mapping/dependency preflight, CREATE/SKIP/CONFLICT, Article/List execution and `CONTENT_MIGRATION_REPORT`.
2. `PartyHistoricalContentMigration.kt` and `PartyHistoricalContentMigrationV2.kt` still duplicate Generic Article behavior and hardcode four original aliases plus `party-theme-education` / `zhutijiaoyu` special handling.
3. `PartyCarouselMigrationV2.kt` duplicates Generic List behavior and hardcodes `PARTY_CAROUSEL`, `party-carousel:position:*`, fixed count 4, Party static path, and EU-29/EU-30 position-2 fingerprint transition.
4. Current Party Article canonical shape is semantically compatible with Generic Article model; the adapter only needs Party scope validation and DTO normalization.
5. Current Party carousel shape predates Generic list schema: LINK items may omit `sourceType`; ARTICLE item uses `articleRef`; item/image evidence includes Party provenance fields. A bounded adapter is lower-risk than rewriting accepted canonical bytes solely for serializer compatibility.
6. Current `manifest.json` already owns sourceSystem, contentScope aliases/counts and accepted snapshot/extension facts; current list index/items own current list code, stable identities, order and fingerprints. Kotlin allow-lists duplicate this authority and can be removed.
7. Only the accepted old→current position-2 transition cannot be reconstructed from current canonical files alone. It is currently hardcoded in Kotlin and the EU-30 workflow; this fact must be promoted to Party-owned repository data.
8. The pinned EU-29 baseline is already stable at commit `59c855f55899cd613fdee059b27db762ffa3b092`; workflow materialization proves old position 2 = LINK fingerprint `c2ad...10dc` and current canonical = ARTICLE fingerprint `f8b5...be84`.
9. Existing canonical verification also asserts current LINK carousel images remain under `/static/migrated/party/carousel/**`; changing that path is unnecessary behavior churn. Generic prepared list input therefore needs a neutral optional safe static target so Party can preserve the accepted projection without putting Party literals in Generic code.
10. Existing report labels/tasks are repository consumers and can be preserved by wrappers over Generic results.
11. No DB schema/Flyway/API/frontend/Site Package prerequisite exists. Consumer-local Method is sufficient; no `agentic-dev` baseline upgrade is required.

No unresolved product/architecture decision remains.

## 2. Data authority plan

### 2.1 Keep current canonical bytes authoritative

Do not rewrite 183 Article units or 4 current carousel item evidence merely to match Generic JSON field names. Keep:

- `manifest.json`;
- `index.ndjson` + `articles/**`;
- `lists/PARTY_CAROUSEL/index.json` + `items/**`;
- existing provenance/reports.

This preserves accepted source evidence and reduces a format-only migration diff.

### 2.2 Add bounded compatibility authority

Add:

```text
data-migrations/party/v1/compatibility.json
```

Proposed minimal shape：

```json
{
  "compatibilityVersion": 1,
  "transitions": [
    {
      "kind": "LIST_ITEM",
      "listCode": "PARTY_CAROUSEL",
      "sourceSystem": "legacy-jilinjobs",
      "legacyKey": "party-carousel:position:2",
      "fromFingerprint": "<EU29 accepted>",
      "toFingerprint": "<current canonical>",
      "fromSourceType": "LINK",
      "toSourceType": "ARTICLE",
      "preserveRuntimeId": true,
      "oldStaticTargetPrefix": "migrated/party/carousel"
    }
  ]
}
```

Exact Runtime guard values that are already in canonical data（title/url/order/image digest）should be derived from old/current item files where possible rather than duplicated into compatibility.json. The compatibility file only stores facts unavailable from either current or pinned dataset alone.

## 3. Source ownership plan

Target ownership：

```text
backend/apps/content-migration/src/main/kotlin/com/jilinjobs/cms/
├── ContentMigrationApplication.kt
└── migration/
    ├── LegacyMappings.kt
    ├── generic/
    │   └── GenericContentMigration.kt
    └── party/
        ├── PartyCanonicalAdapter.kt
        ├── PartyCompatibility.kt
        └── PartyMigrationFacade.kt
```

Exact file count may be collapsed for minimal diff, but boundaries are mandatory：

- `generic/**`：neutral only；
- `party/**`：Party dataset parsing/profile/compatibility/report projection；
- old `PartyHistoricalContentMigration*` / `PartyCarouselMigration*` direct Runtime importer logic is removed or reduced to wrappers with no duplicate Article/List mutation pipeline.

`LegacyMappings.kt` remains shared mapping authority.

## 4. Generic Engine thin extension

Phase 2C may make only evidence-backed neutral changes inside `GenericContentMigration.kt`.

### 4.1 Prepared dataset entry

Refactor current service into：

```text
raw snapshot path
  → CanonicalDatasetLoader.load(...)
  → importPreparedDataset(LoadedDataset)
      → Generic preflight
      → Generic execute
      → Generic report
```

`importPreparedDataset`（name may vary）must remain internal/site-neutral and reuse the exact existing preflight/execute semantics. Party adapter calls this entry after Party normalization.

### 4.2 Shared neutral file verification

Extract or expose narrow helpers for：

- root-contained path resolution；
- regular file existence；
- size/SHA-256 verification；
- safe static target validation。

Party adapter must reuse these helpers or a neutral verifier object; it must not implement a weaker second path/digest contract.

### 4.3 Prepared List provenance

Add neutral optional prepared inputs if required：

- `sourceProvenanceUrl` distinct from Runtime LINK `url`；
- `staticTarget` for LINK image.

Generic raw JSON path does not need to expose these as new public schema fields in Phase 2C; they can be internal prepared-model metadata. If `staticTarget` is absent, keep EU-47 default `migrated/content/lists/<LIST_CODE>/<sha>.<ext>`.

When supplied, validate it as safe relative static path before any write.

Generic mapping insert uses `sourceProvenanceUrl ?: url ?: sourcePage.orEmpty()`.

## 5. Party canonical adapter

### 5.1 Dataset load

`PartyCanonicalAdapter` loads：

- manifest；
- Article index + article JSON/resources；
- carousel index + item JSON/resources；
- optional compatibility authority path.

Use dedicated Party DTOs only for on-disk legacy/current shape; immediately normalize to neutral Generic loaded/prepared models.

### 5.2 Article normalization

For each Article：

- validate sourceSystem against manifest；
- validate target alias against `manifest.contentScope`；
- validate index identity/fingerprint/target fields；
- verify resource path/size/digest with neutral verifier；
- map directly to Generic Article record/loaded resources.

No Party Article importer may call Core Article/Resource services.

### 5.3 List normalization

For each Party carousel item：

- index/item identity/order/fingerprint cross-check；
- `sourceType ?: LINK` for accepted old format；
- `articleRef` → neutral `articleReference`；
- LINK Runtime url remains item `url`；
- ARTICLE Runtime url = null, source provenance url = item `url` or sourcePage；
- image path/size/digest verified by neutral helper；
- LINK `staticTarget` = `<oldStaticTargetPrefix>/<sha>.<ext>` from Party compatibility/profile authority；
- ARTICLE image stays managed Resource via Generic importer.

The adapter validates current/pinned dataset cardinality from each dataset index rather than Kotlin `1..4` hardcode. Current repository tests may still assert 4 as accepted product evidence.

## 6. Party compatibility service

`PartyCompatibilityService` runs before steady-state Generic list preflight only when an existing mapping has a different fingerprint.

Algorithm：

```text
for each normalized current Party list item with different existing fingerprint
  find exact transition in compatibility authority
  if none → leave for Generic CONFLICT
  load current Runtime item + pinned/from canonical evidence as needed
  verify old accepted Runtime guard
  resolve current target Article mapping
  verify current image bytes/digest
  update existing item in place
  update legacy mapping to current canonical fingerprint/provenance
  emit UPDATED compatibility result
then run Generic prepared dataset
  transitioned item now classifies SKIP
  remaining items CREATE/SKIP/CONFLICT normally
merge compatibility UPDATED + Generic results into Party report
```

Required old Runtime guard至少：

- same list code / list item id / mapping identity；
- fromFingerprint exact；
- sourceType LINK；
- articleId null；
- enabled/order/title/url match pinned accepted item；
- legacy static image path/digest match pinned accepted item；
- no managed image resource。

Any mismatch = CONFLICT and no update.

The service may use `CmsListMapper` / mapping correction write directly only for this exact transition. No general UPDATE API is added to Generic Engine.

## 7. Party facade / report compatibility

Create one Party facade used by dispatcher + Gradle tasks.

### 7.1 `party-content`

- load/normalize Articles only；
- call Generic prepared import；
- project Generic results to existing `PartySnapshotImportReport` JSON shape；
- print `EU29_IMPORT_REPORT`；
- no compatibility update path.

### 7.2 `party-carousel`

- load/normalize Lists only；
- apply exact compatibility transition if applicable；
- call Generic prepared import；
- merge `UPDATED` with Generic CREATED/SKIPPED/CONFLICT/INVALID；
- print `EU29_CAROUSEL_IMPORT_REPORT`。

Current root task names and dispatcher command names remain.

## 8. Legacy EU-29 baseline verification support

The upgrade workflow continues to materialize pinned accepted commit `59c855f...` into `eu29-accepted/`.

Because that snapshot predates `compatibility.json`：

- ordinary old baseline import needs no transition authority；the adapter reads its own manifest/index/items and uses Generic create path；
- upgrading to current dataset must load current repository `data-migrations/party/v1/compatibility.json` explicitly；
- do not copy compatibility.json into the pinned archive and present it as historical EU-29 data；it is current transition authority applied to a pinned old state.

Implementation may use an environment/property such as `PARTY_MIGRATION_COMPATIBILITY_FILE` for explicit workflow wiring. Current snapshot default resolves `<snapshot-root>/compatibility.json` when present.

## 9. Verification plan

### 9.1 New focused verifier

Add root task：

```text
verifyPartyMigrationDespecialization
```

Test source dynamically uses current/pinned-like small fixtures or repository dataset metadata to prove：

1. manifest aliases drive Party Article scope；no Kotlin alias allow-list；
2. Party Article adapter delegates to Generic prepared path；
3. Party list adapter handles old missing `sourceType`, current ARTICLE `articleRef`, provenance URL and legacy static target；
4. compatibility file exact transition loads；
5. wrong fromFingerprint / wrong Runtime sourceType / order / url / image path / digest / missing target article → conflict/no update；
6. transition preserves Runtime id and becomes Generic SKIP afterward；
7. deleting transition produces conflict；
8. Generic package purity stays clean；
9. Party source inspection contains no copied accepted alias/fingerprint list except fixture/profile parsing labels.

### 9.2 Existing workflows

Update existing workflows only where actual command/result semantics require：

- `canonical-migration-verify.yml`：keep 183 + 4 first/second import and bytes/reconciliation; retain accepted legacy static path assertion if preserved；
- `eu30-migration-upgrade-verify.yml`：pass current compatibility authority explicitly; keep pinned commit and exact old→current assertions；
- `generic-content-migration-verification.yml`：also trigger on neutral Generic prepared-entry changes；
- `backend-application-boundary.yml` / `site-package-verification.yml` path filters already cover app/core changes unless actual new paths escape them；
- Repository CI unchanged unless required for new task wiring.

Add focused workflow：

```text
.github/workflows/party-migration-despecialization-verification.yml
```

Paths：content-migration app, relevant `data-migrations/party/v1/compatibility.json`, generic/Party schemas if touched, root build, workflow itself.

### 9.3 Final exact-head gates

1. Party Migration De-specialization Verification；
2. Canonical Migration Verification；
3. EU-30 Migration Upgrade Verification；
4. Generic Content Migration Verification；
5. Backend Application Boundary Verification；
6. Site Package Verification；
7. Repository CI including Integrated Browser；
8. unresolved review threads = 0。

Review Environment is supporting only; no intended visual change.

## 10. Expected implementation diff

Allowed implementation surfaces：

- `backend/apps/content-migration/**`；
- `backend/build.gradle.kts`；
- `data-migrations/party/v1/compatibility.json`；
- existing migration verification workflows + one focused workflow；
- focused test/verifier code；
- Authority / Work lifecycle locators。

Expected no-change surfaces：

- DB Flyway/schema；
- `frontend/**` product code；
- `sites/jilinjobs/**` bytes；
- current Party Article/list content/resource bytes other than new compatibility authority；
- Generic CMS APIs。

If implementation requires changing 183/4 accepted source content, creating a policy framework, adding schema fields to product tables, or changing Public/Admin behavior, stop and return to Planning.

## 11. Rollback boundary

One Phase 2C implementation PR is the rollback boundary. Data change is additive compatibility authority plus code/workflow refactor; no irreversible DB migration. Focused verification uses Fresh DB/temp filesystem.

## 12. Slice recommendation

Technical dependency graph is atomic：Party adapter cannot be accepted without Generic prepared entry; current fresh adoption cannot be accepted while breaking pinned EU-29 upgrade; compatibility policy cannot be separated from the exact workflows that prove it.

Therefore recommend one Candidate Execution Unit covering Party authority promotion + Generic adoption + accepted upgrade compatibility + verification. Splitting would create an intermediate dual-importer or broken-upgrade state with no independent product value.

Technical Plan **READY** for `slice-work`。