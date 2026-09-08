# Generic Content Migration Application Specification

## Authority

- `docs/requirements/generic-content-migration-application.md`
- `data-migrations/README.md`
- `docs/requirements/backend-application-core-boundary.md`
- GitHub Issue #92
- GitHub Issue #77

## Status

- Phase: **Phase 2B — Generic Content Migration Application**
- Specification: **READY**
- Technical Planning: **REQUIRED**
- Planning baseline: `main@0af483a6e5278ad4ac049f33148144214b40669d`
- Current Ready Execution Unit: **NONE until slice-work + readiness-check are integrated**

## 1. Required runtime topology

Phase 2B 继续复用 EU-46 accepted topology：

```text
cms-server ─────────────→ cms-core
content-migration ──────→ cms-core
```

Generic migration capability必须位于 `content-migration` application内，不建立第三个 migration app、不把 Generic capability放回 Server，也不把 Party-specific code提升进 Core。

Content Migration application内部责任收敛为：

```text
ContentMigrationApplication
├── Generic canonical migration capability
└── Party compatibility/current commands（temporarily coexist until Phase 2C）
```

Generic capability与 Party current implementation允许在 Phase 2B并存；共存不是长期双轨产品设计，而是为了保持 Phase 2C adoption/reconciliation独立可验证。

## 2. Generic canonical dataset contract

### 2.1 Dataset root

Generic command接收一个 local snapshot root。Snapshot root必须是调用方显式提供的 repository-owned / review-frozen directory；普通 Stable CI不得访问 Legacy Source。

Generic loader至少识别：

```text
<snapshot-root>/
├── index.ndjson
├── articles/<stable-id>/article.json
├── articles/<stable-id>/assets/**
└── lists/<list-code>/
    ├── index.json
    └── items/<stable-id>/item.json
```

Phase 2B 不要求每个 dataset同时包含 Article 与 List；empty section允许，但至少必须存在一个可导入 canonical unit。

### 2.2 Article canonical model

Article contract继续采用当前 `data-migrations/schemas/article.schema.json` 的 site-neutral语义：

- `source.system`；
- `source.legacyKey`；
- legacy metadata / source URL；
- `target.columnAlias`；
- `target.articleType`；
- content fields；
- resources；
- `sourceFingerprint`；
- evidence / stable source order。

Generic implementation不得定义 Party alias allow-list，也不得以 `typeCode` 映射出 Party-specific target。

### 2.3 Generic list index / item model

Phase 2B 应新增一个 generic list canonical schema；Party-specific `carousel.schema.json` 保持不变。

Generic list index至少包含：

- `listCode`：Stable CMS List code；
- `sourceSystem`；
- optional source-page/provenance metadata；
- ordered item references；
- 每个 reference 的 `legacyKey`、`path`、`sourceOrder`、`sourceFingerprint`。

Generic list item至少表达：

- stable `legacyKey`；
- `sourceOrder`；
- `sourceType = LINK | ARTICLE`；
- title / optional subtitle；
- LINK URL 或 ARTICLE stable reference；
- open mode；
- enabled state或由 schema明确的稳定默认；
- `sourceFingerprint`；
- optional image/resource evidence；
- provenance/evidence。

ARTICLE reference使用：

```text
sourceSystem + legacyKey
```

不得使用 Runtime article id。

Generic schema不得定义：

- `PARTY_CAROUSEL` const；
- `party-carousel:position:*` pattern；
- fixed item count = 4；
- EU-29/EU-30 accepted fingerprints；
- Party-specific static destination path。

## 3. Preflight contract

Generic import采用明确的 **load → preflight → execute → reconcile/report** 状态链。

### 3.1 Load / structural validation

在 Runtime mutation前必须完成：

1. index/item JSON可解析；
2. relative path normalize后仍位于 snapshot root；
3. referenced files存在且为 regular file；
4. dataset内 sourceSystem + legacyKey identity不重复冲突；
5. sourceFingerprint / resource SHA-256格式有效；
6. resource size / digest与实际 bytes一致；
7. Article body中的 migration resource token / relative reference可完整映射；
8. ListItem reference path / fingerprint与 item file一致。

### 3.2 Runtime target/dependency preflight

在写入前必须进一步确认：

- Column alias存在且enabled；
- List code存在且enabled；
- Article-backed ListItem引用的 article identity在本次计划中或 current accepted mapping中可解析；
- dependency graph无缺失/循环；
- existing mapping classification可确定为 CREATE / SKIP / CONFLICT。

如果 structural/runtime preflight存在 INVALID 或 CONFLICT，Generic dataset execute不得开始写入已知会造成部分成功的其余记录。

该 all-known-errors-before-write contract只约束新的 Generic path，不反向改变 Party current commands在 Phase 2B的既有语义。

## 4. Generic Article import contract

Generic Article importer负责：

1. 根据 stable Column alias解析目标；
2. 根据 sourceSystem + legacyKey查询 legacy mapping；
3. fingerprint相同 → SKIP；不同 → CONFLICT；不存在 → CREATE candidate；
4. 在 upload前验证本 article全部 canonical resources；
5. 通过 Core Resource capability写入 BODY_IMAGE / ATTACHMENT；
6. 把 canonical migration token / relative reference改写为 Runtime URL；
7. 创建并发布 Article；
8. 记录 legacy mapping；
9. 返回统一 Generic result。

当前 accepted Article field semantics保持：

- INTERNAL 不带 external URL；
- EXTERNAL_LINK 不带正文/资源且 URL必须是有效 http/https；
- title/source/url length与当前 Core/accepted canonical限制一致；
- source order保持 Historical ordering evidence。

Generic importer不得根据 Party typeCode决定目标 alias，也不得限制 alias为 Party栏目。

## 5. Generic ListItem import contract

Generic ListItem importer负责：

1. 根据 stable List code解析 List definition；
2. 根据 sourceSystem + legacyKey查询 list-item legacy mapping；
3. fingerprint相同 → SKIP；不同 → CONFLICT；不存在 → CREATE candidate；
4. LINK项验证 URL并创建当前 CMS可表达的 LINK ListItem；
5. ARTICLE项通过 stable article legacy mapping解析 Runtime article relation；
6. optional image/resource必须经过 path/size/digest validation；
7. sort order来自 canonical sourceOrder，不依赖输入遍历顺序；
8. 创建 mapping并返回统一 Generic result。

Phase 2B Generic path只要求 **create / skip / conflict** 基线，不提供“已存在记录允许特定 fingerprint transition后原位 UPDATE”的默认能力。EU-29→EU-30 position-2 update属于 Party compatibility policy，继续留在 Phase 2C。

## 6. Generic mapping ownership

当前两个 mapping table / Mapper语义本身是 site-neutral：

- `cms_article_legacy_mapping`；
- `cms_list_item_legacy_mapping`。

Phase 2B 应把当前定义在 Party implementation文件中的 generic mapping Mapper / record ownership移到中性 migration source文件，使 Generic与 Party current code都可以复用同一 mapping authority。

约束：

- 不新增第二套 mapping table；
- 不复制 Mapper接口导致两个不同 ownership；
- 不改变 DB schema / Flyway；
- relocation / source ownership调整不得改变 Party mapping behavior。

Party-only correction mapper（例如 accepted position-2 upgrade）继续属于 Party compatibility code。

## 7. Generic report / command contract

Content Migration executable dispatcher新增一个明确的 generic command，例如：

```text
java -jar ... generic-content <snapshot-root> [Spring Boot args...]
```

Repository root Gradle entry新增一个与之对应的 generic migration task，例如：

```text
./gradlew importCanonicalContent --args=<snapshot-root>
```

最终命名由 Technical Plan冻结，但必须满足：

- 与 `party-content` / `party-carousel` 区分；
- report label不使用 `EU29_*`；
- report至少包含 total / created / skipped / conflicts / invalid与逐项 identity/status/message；
- conflict/invalid导致 process / task失败；
- no mutation / preflight failure可从 report中辨认。

Existing `party-content` / `party-carousel` dispatcher、root `importPartyHistoricalContent` / `importPartyCarousel`与 `EU29_*_REPORT`在 Phase 2B保持兼容。

## 8. Resource / transaction / side-effect contract

1. Generic Engine在任何 resource upload前完成该 dataset可静态确定的 path/bytes/dependency preflight。
2. Article/ListItem Runtime DB写入仍通过当前 Core service/transaction boundary完成。
3. Managed resource / static file side effect可能先于最终 mapping insert发生；Phase 2B不引入 distributed transaction。
4. Generic report必须能够指出失败 identity；Implementation与verification必须证明已知 invalid/conflict不会在 preflight通过前制造无关 records。
5. filesystem/static path若需要生成，必须基于 site-neutral stable input与安全 path policy，不得硬编码 `migrated/party/**`。

## 9. Verification mapping

### 9.1 Generic focused verification

新增 focused verification，使用 synthetic canonical fixture，明确不得包含 Party/JilinJobs/EU-29/EU-30 identity：

- Generic Fresh DB + target Column/List setup；
- INTERNAL Article + BODY_IMAGE / ATTACHMENT；
- EXTERNAL_LINK Article；
- LINK ListItem；
- ARTICLE ListItem stable reference；
- first import CREATE；
- second import SKIP；
- changed fingerprint CONFLICT；
- digest/size tamper INVALID；
- `../` path traversal INVALID；
- missing target / missing dependency INVALID；
- report + nonzero failure semantics；
- no HTTP listener / no Server transport leakage。

### 9.2 Generic-source purity proof

Verification必须证明 Generic package/source不包含以下 Party-only facts：

- Party aliases；
- `PARTY_CAROUSEL`；
- `party-carousel:position:`；
- EU-29 / EU-30 accepted fingerprint literals；
- position-2 upgrade exception；
- `migrated/party/` destination hardcode。

### 9.3 Regression verification

Final exact-head仍需：

- Repository CI；
- Backend Application Boundary Verification；
- Canonical Migration Verification；
- EU-30 Migration Upgrade Verification；
- Site Package Verification；
- Generic Content Migration focused verification；
- unresolved PR review threads = 0。

Review Environment可作为 supporting evidence，但本 Unit没有页面/视觉产品变化，不作为 required human gate。

## 10. Non-goals

- 不把 Party current dataset/profile切换到 Generic path；
- 不删除 Party importer；
- 不实现 Party accepted update/reconciliation policy；
- 不修改 Party canonical bytes；
- 不开始 Main Site migration data collection；
- 不修改 Generic CMS schema；
- 不改变 Server API / Public Renderer / Site Package behavior；
- 不创建公共 plugin API、script runtime或新的 migration repository；
- 不进入 Phase 2C / Phase 3 / E1～E3。

## 11. Specification readiness

Generic canonical shape、preflight、Article/ListItem semantics、mapping ownership、report/command、side-effect boundary、Generic-vs-Party separation与verification均可测试地定义。

剩余问题属于 HOW：generic package/file map、loader/plan abstraction、具体 CLI/task命名、synthetic fixture实现、schema file布局与workflow wiring。因此本 Specification **READY**，且 Technical Planning **REQUIRED**。