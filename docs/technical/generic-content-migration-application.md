# Generic Content Migration Application Technical Plan

## Authority

- `docs/requirements/generic-content-migration-application.md`
- `docs/specifications/generic-content-migration-application.md`
- `data-migrations/README.md`
- `docs/technical/backend-application-core-boundary.md`
- GitHub Issue #92 / Issue #77

## Status

- Phase: **Phase 2B — Generic Content Migration Application**
- Planning baseline: `main@0af483a6e5278ad4ac049f33148144214b40669d`
- Dependency closure: **COMPLETE**
- Technical Plan: **READY**
- Architecture choice: **extend the existing EU-46 `content-migration` application with a bounded generic package; no new application/module/plugin framework**
- Current Ready Execution Unit: **NONE until slice-work + readiness-check are integrated**

## 1. Dependency closure findings

EU-46 后 current Repository 已把 application prerequisite关闭，Phase 2B inventory确认：

1. `backend/apps/content-migration` 已是独立 Spring Boot executable application，只依赖 `:modules:cms-core`，无 Server app dependency。
2. `ContentMigrationApplication` 当前 dispatcher只识别 `party-content` / `party-carousel`，直接获取 Party V2 services。
3. Party Article importer中已经存在 site-neutral capability与 Party policy混合：
   - stable `sourceSystem + legacyKey` mapping；
   - fingerprint SKIP / CONFLICT；
   - Column alias resolution；
   - Article INTERNAL / EXTERNAL_LINK validation；
   - snapshot path containment、size / SHA-256 validation；
   - resource upload与 body reference rewrite；
   - create/publish/mapping/report；
   - 同时又内建 `PARTY_ALIASES`、`party-theme-education` / `zhutijiaoyu` 等 Party facts。
4. Party Carousel importer同样混合 generic capability与 Party policy：
   - list mapping、path/digest、LINK/ARTICLE、stable article reference、report；
   - 同时内建 `PARTY_CAROUSEL`、固定 4 条、`party-carousel:position:*`、`migrated/party/carousel/**`。
5. EU-30 V2 carousel还内建两个 accepted fingerprints与 position-2 LINK→ARTICLE原位 UPDATE exception；这是明确 Party compatibility policy，不能进入 Generic Engine。
6. `ArticleLegacyMappingMapper` / `ArticleLegacyMappingRecord` 与 `CmsListItemLegacyMappingMapper` / record当前声明在 Party文件中，但对应表与字段本身是 site-neutral，可成为 shared migration-only ownership；EU-30 correction mapper仍是 Party-only。
7. `data-migrations/README.md` 已定义长期 site-neutral canonical organization；`article.schema.json` 已是 generic；现有 `carousel.schema.json` 明确是 Party-specific，不能直接作为 Generic List schema。
8. `CmsListService` 对 LINK / ARTICLE有不同 image contract：LINK使用 `/static/**` `imagePath`，ARTICLE可用 managed `imageResourceId` / article cover；Generic ListItem importer必须遵守这一现有 Core contract，而不能假设两类 image storage相同。
9. `StaticResourceService` 已提供 safe normalized path、content-type validation、protected-path与 public resolution；Generic LINK image可以复用该 capability，不需要新 storage framework。
10. current Party workflows已经完整覆盖 183 Articles、4 carousel、idempotency、conflict与 EU-29→EU-30 upgrade；Phase 2B应把它们作为 regression evidence，而不是重写其 expected result。
11. 没有 DB schema gap：两个 legacy mapping table已存在，Generic Engine不需要新 Flyway migration。
12. Consumer-local Method足够完成本 Unit；不存在 `agentic-dev` baseline upgrade prerequisite。

因此 Phase 2B 的最低必要形态是 **在现有 Content Migration app 内增加 Generic Engine + Generic canonical schema/verification，并把中性 mapping ownership从 Party source中拆出；不新增 Gradle module，不在本 Phase迁移 Party current behavior到 Generic Engine。**

## 2. Source ownership / package plan

保持 `backend/apps/content-migration` 为唯一 migration application。

推荐 source map：

```text
backend/apps/content-migration/src/main/kotlin/com/jilinjobs/cms/
├── ContentMigrationApplication.kt
└── migration/
    ├── LegacyMappings.kt
    ├── generic/
    │   ├── CanonicalMigrationModels.kt
    │   ├── CanonicalDatasetLoader.kt
    │   ├── CanonicalMigrationPreflight.kt
    │   ├── GenericArticleImporter.kt
    │   ├── GenericListItemImporter.kt
    │   └── GenericContentMigrationService.kt
    ├── PartyHistoricalContentMigration.kt
    ├── PartyHistoricalContentMigrationV2.kt
    ├── PartyCarouselMigration.kt
    └── PartyCarouselMigrationV2.kt
```

Exact file count可以按实现最小性合并，但 ownership必须保持：

- `migration/generic/**` 不含 Party identity / accepted compatibility constants；
- Party files继续拥有 Party validator / alias / fixed-count / upgrade policy；
- generic mapping Mapper只保留一份 owner。

### 2.1 Neutral legacy mapping relocation

从 Party files中仅搬出中性声明：

- `ArticleLegacyMappingMapper`；
- `ArticleLegacyMappingRecord`；
- `CmsListItemLegacyMappingMapper`；
- `CmsListItemLegacyMappingRecord`。

保持 Kotlin package可优先继续为 `com.jilinjobs.cms.migration`，减少 Party source semantic diff。Party services与 Generic services都引用同一 Mapper owner。

`Eu30CarouselMappingMapper` 及 accepted-upgrade helper仍留 Party V2 source。

## 3. Generic canonical model

### 3.1 Article

Generic Kotlin model直接映射 current `article.schema.json`语义。可以使用中性名称：

- `CanonicalArticleSource`；
- `CanonicalArticleTarget`；
- `CanonicalArticleContent`；
- `CanonicalMigrationResource`；
- `CanonicalArticleEvidence`；
- `CanonicalArticleRecord`；
- `CanonicalArticleIndexEntry`。

不得复用 `PartyMigration*` type作为 Generic API，因为 type name / validator会继续把 Party ownership渗入新 capability。

### 3.2 Generic List

在 `data-migrations/schemas/` 增加：

```text
list-index.schema.json
list-item.schema.json
```

Generic List model至少包含：

```text
CanonicalListIndex
CanonicalListItemReference
CanonicalListItemRecord
CanonicalArticleReference
CanonicalListImage
```

`CanonicalListItemRecord.sourceType`仅支持 current Core已有 `LINK | ARTICLE`。

Generic List schema不修改 `carousel.schema.json`；Phase 2C再决定 Party canonical格式如何无损映射/升级到 Generic contract。

## 4. Load / preflight architecture

不要让 Generic importer边读边写。采用两阶段：

```text
load canonical files
    ↓
structural + byte preflight
    ↓
runtime target + mapping + dependency preflight
    ↓
GenericImportPlan
    ↓
execute
    ↓
reconcile/report
```

### 4.1 Structural / byte preflight

`CanonicalDatasetLoader` 负责：

- snapshot root absolute/normalize；
- resolve helper必须 `resolved.startsWith(root)`；
- parse article index / list indexes / item files；
- index identity/path/fingerprint与 item file交叉一致；
- resource file existence / regular-file；
- size / SHA-256；
- migration token / relative reference可解析；
- dataset identity duplicate detection；
- schema-level invariant。

Loader只构建 immutable loaded dataset，不调用 Runtime write service。

### 4.2 Runtime preflight

`CanonicalMigrationPreflight` 负责读取 current Runtime：

- resolve Column alias；
- resolve List code；
- query article/list legacy mappings；
- classify CREATE / SKIP / CONFLICT；
- build Article stable identity map；
- verify ARTICLE ListItem dependency存在于 CREATE/SKIP article plan或 current mapping；
- reject missing/cyclic/unresolvable dependency。

若任何 loaded unit为 INVALID或 mapping为 CONFLICT，返回 failed preflight report，不进入 execute。这样 Generic path不会因为第 N 条已知问题让前 N-1 条先写入。

Unexpected execute-time failure仍可能产生 bounded resource/file side effect；report必须暴露，不宣称 distributed rollback。

## 5. Generic execute plan

### 5.1 Execution order

固定为：

```text
Articles
  ↓
ListItems LINK / ARTICLE
```

同类 records按 canonical source order + stable identity确定 deterministic顺序；不依赖 filesystem enumeration顺序。

### 5.2 Article execution

`GenericArticleImporter` 使用：

- `ColumnQuery`；
- `ArticleService`；
- `ResourceService`；
- neutral `ArticleLegacyMappingMapper`。

CREATE时：

1. 使用 preflight verified resource descriptor；
2. upload managed resources；
3. rewrite body token / relative reference；
4. create + publish Article；
5. insert mapping。

SKIP不产生 resource/file writes。

Importer本身不再重复读取 Party alias/typeCode rules。

### 5.3 ListItem execution

`GenericListItemImporter` 使用：

- `CmsListService` / necessary read Mapper；
- `StaticResourceService`；
- `ResourceService`（ARTICLE image override）；
- neutral article/list legacy mappings。

LINK image遵守 current Core requirement `imagePath=/static/**`：

- generic deterministic target使用 site-neutral path，例如 `migrated/content/lists/<LIST_CODE>/<sha256>.<ext>`；
- path segment来自 normalized CMS List code + digest，不使用 source URL/path直接拼接；
- target已存在时先验证 bytes/digest相同后复用；不同则 CONFLICT/INVALID，不盲目 replace；
- target不存在时通过 `StaticResourceService.upload(..., replace=false)` 创建。

ARTICLE image使用 managed `ResourceService` 并写 `imageResourceId`，保持 current `CmsListService` validation。

Phase 2B Generic importer不调用 `CmsListMapper.updateItem`绕过 source-type immutability，也不实现 accepted fingerprint transition。existing different fingerprint一律 CONFLICT。

## 6. Report model

新增中性：

```text
GenericMigrationStatus = CREATED | SKIPPED | CONFLICT | INVALID
GenericMigrationResult
GenericContentMigrationReport
```

Report至少包含：

- total；
- created；
- skipped；
- conflicts；
- invalid；
- per-unit kind / sourceSystem / legacyKey / status / runtimeId / message；
- phase：`PREFLIGHT` 或 `EXECUTE`（或等价字段），用于区分未开始 mutation与 execute-time failure。

Generic report label冻结为：

```text
CONTENT_MIGRATION_REPORT
```

若 `conflicts > 0 || invalid > 0`，CLI必须非成功结束。

## 7. Command / Gradle compatibility

### 7.1 Executable dispatcher

扩展 current `ContentMigrationApplication`：

```text
generic-content <snapshot-root>
party-content <snapshot-root>
party-carousel <snapshot-root>
```

`generic-content` 获取 `GenericContentMigrationService`。

Existing Party branches保持调用 current Party V2 services与 `EU29_*_REPORT` labels。

### 7.2 Root Gradle task

在 `backend/build.gradle.kts` 新增：

```text
importCanonicalContent
```

它使用 `:apps:content-migration` runtimeClasspath并调用 Generic dispatcher/service entry。Existing：

- `importPartyHistoricalContent`；
- `importPartyCarousel`；

保持不变。

Generic task是 repository engineering interface，不在 Phase 2B提升为跨项目 public SPI。

## 8. Schema / data-migrations impact

Allowed Phase 2B changes：

- 新增 generic list schema；
- 如实现需要，对 `data-migrations/README.md` 增加 Generic JVM command/schema locator说明；
- 不改 `party/v1/**` canonical bytes / manifest / accepted snapshots；
- 不改 Party `carousel.schema.json` semantics。

Synthetic verification fixture不进入 `data-migrations/<site>/` current canonical workspace。优先由 test/verifier在 temp directory动态生成，避免测试数据被 Fresh Context误解为真实 migration authority。

## 9. Focused verification

新增 root verifier task：

```text
verifyGenericContentMigration
```

对应 test/main verification可位于 `apps/content-migration` test source。

Verifier使用 Fresh MySQL / temp static root / temp storage root，动态创建 generic canonical fixture与 generic stable target Column/List。Fixture identity示例必须中性，例如：

```text
sourceSystem = verification-source
columnAlias = verification-news
listCode = VERIFY_FEATURED
```

不得使用 Party/JilinJobs/EU-29/EU-30常量。

Required cases：

1. INTERNAL article + body image + attachment CREATE；
2. EXTERNAL_LINK article CREATE；
3. LINK ListItem CREATE；
4. ARTICLE ListItem stable reference CREATE；
5. first report expected counts；
6. second run all SKIP；
7. changed fingerprint → preflight CONFLICT / no execute；
8. path traversal → INVALID；
9. digest/size tamper → INVALID；
10. unresolved body token → INVALID；
11. missing Column/List → INVALID；
12. missing article dependency → INVALID；
13. generic CLI report label / failed exit semantics；
14. non-web / no Server transport继续由 boundary verifier证明。

对 known preflight failure，verifier必须断言 Runtime article/list mapping counts未变化；filesystem/storage仅允许 verifier自己创建的 temp scope。

## 10. Generic purity verification

增加一个轻量 source/package assertion（可在 root Gradle verifier或脚本中）：

`migration/generic/**` 不得包含：

- `party-` alias literals；
- `PARTY_CAROUSEL`；
- `party-carousel:position:`；
- `EU29` / `EU30`；
- accepted fingerprint literals；
- `migrated/party/`。

该检查只针对 Generic package，不要求 Party compatibility files删除这些必要事实。

## 11. Workflow plan

新增 focused workflow：

```text
.github/workflows/generic-content-migration-verification.yml
```

触发范围至少覆盖：

- `backend/apps/content-migration/**`；
- `backend/modules/cms-core/**`；
- `backend/build.gradle.kts` / `settings.gradle.kts` / app build files；
- `data-migrations/schemas/**` / `data-migrations/README.md`；
- workflow自身。

Run `verifyGenericContentMigration` + Generic purity / boundary assertions。

Final exact-head required evidence：

1. Generic Content Migration Verification；
2. Backend Application Boundary Verification；
3. Canonical Migration Verification；
4. EU-30 Migration Upgrade Verification；
5. Site Package Verification；
6. Repository CI（含 Integrated Browser）；
7. unresolved review threads = 0。

`canonical-migration-verify.yml` / `eu30-migration-upgrade-verify.yml` path filters若已覆盖 `content-migration/**`则不因 Phase 2B机械扩张；只有真实新 path未被观察时才做最小同步。

## 12. Execution sequence

在单一 EU branch内：

1. relocate neutral mapping ownership，保持 Party behavior；
2. add generic canonical models + list schemas；
3. implement loader / structural+byte preflight；
4. implement runtime preflight / import plan；
5. implement Generic Article importer；
6. implement Generic ListItem importer；
7. add Generic service / report；
8. add `generic-content` dispatcher + `importCanonicalContent` root task；
9. add focused verifier / purity proof / workflow；
10. run local/static checks与全部 exact-head required workflows；
11. review convergence、base drift check、integration与 Post-Integration evidence。

不得在中间合并“只有 Generic models/schema、没有 executable/verification”或“Party已部分切换但 compatibility尚未闭合”的状态。

## 13. Rollback / side effects

Rollback boundary：整个 Phase 2B EU PR。

本 Unit不改 schema、不改 Party accepted dataset、不改产品 API。Generic verification使用 Fresh DB与 temp filesystem/storage。Implementation rollback删除 Generic source/schema/task/workflow并恢复 neutral mapping relocation即可；Party commands在整个 Unit内保持可回归，因此 rollback不需要业务数据转换。

## 14. Technical readiness

Current code、Core validation、mapping tables、static/resource storage、canonical layout与workflow dependencies均已盘点；无需新 schema、app/module、public SPI或人工产品决策。

Generic package边界、canonical model、preflight/execute sequencing、LINK/ARTICLE image projection、mapping ownership、CLI/task、verification与rollback已经明确到可切分程度。

Technical Plan：**READY**。下一步是 `slice-work`；本文本身不创建 Execution Unit或 Execute Authority。