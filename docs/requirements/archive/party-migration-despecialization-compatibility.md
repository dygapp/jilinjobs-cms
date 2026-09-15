# Party Migration De-specialization & Compatibility Requirement

## Status

- Parent Planning Authority：GitHub Issue #92 / `docs/project/pre-e1e3-convergence-plan.md`
- Related architecture authority：GitHub Issue #77 / `docs/project/site-package-planning.md`
- Phase：**Phase 2C — Party Migration De-specialization & Compatibility**
- Requirement：**READY**
- Planning baseline：`main@bd5dbd84bafd7b731ca290bbd40fb135806cb086`
- Phase 2A / EU-46：**COMPLETED**
- Phase 2B / EU-47：**COMPLETED**
- Current Ready Execution Unit：**NONE until slice-work + readiness-check are integrated**

## 1. Intent

在 EU-47 已建立 site-neutral Generic Content Migration capability 后，把当前 Party historical migration 从“Party-specific Runtime importer 自己实现 Article/List 导入”收敛为“Party dataset/profile/compatibility authority + Generic Engine consumer”。

Phase 2C 必须同时保持两类已接受事实：

1. current Party canonical steady-state：183 Articles、4 个 `PARTY_CAROUSEL` items、resource integrity、second-import idempotency 与 changed-fingerprint conflict；
2. pinned EU-29 accepted runtime → current EU-30 canonical 的唯一 accepted compatibility transition：carousel position 2 从 accepted LINK state 原位升级为 ARTICLE state，并保持既有 list-item identity。

Generic Engine 不得吸收 Party alias、固定 carousel 数量、Party legacy-key pattern、EU-29/EU-30 accepted fingerprint 或 upgrade-only exception。

## 2. Party authority ownership

### 2.1 Dataset-owned current facts

Party current canonical事实继续由 `data-migrations/party/v1/**` 持有：

- `manifest.json`：source system、content scope / Column aliases、accepted snapshot / extension provenance；
- `index.ndjson` + `articles/**`：183 current Article canonical units与各自 stable identity / fingerprint / resources；
- `lists/PARTY_CAROUSEL/index.json` + `items/**`：current list code、4 个 item identity/order/current fingerprint与 source relation；
- existing reports/source-discovery：traceability / provenance。

Kotlin Party code不得再以常量列表复制 `manifest.json` 已持有的 Column aliases，也不得把 current carousel item fingerprints作为代码常量维护。

### 2.2 Compatibility-owned transition facts

Phase 2C 应增加一个 Party-owned、repository-versioned compatibility authority（例如 `data-migrations/party/v1/compatibility.json`），只承载 **current canonical dataset无法表达、但执行 accepted historical transition必须知道** 的最小事实：

- transition stable identity（list code / source system / legacy key）；
- accepted old `fromFingerprint`；
- accepted old source type；
- preserved Runtime item identity requirement；
- 必要的 old-state guard分类 / transition version。

Current `toFingerprint`、to source type、target Article stable relation、current image digest/order/title等已经属于 current canonical item，不应在 compatibility authority再维护第二份值；执行 transition时必须直接从 current canonical item读取并交叉验证。Party-specific steady-state static projection path属于 bounded Party adapter实现责任，不作为历史 transition事实塞入 compatibility authority。

该文件是 Party compatibility authority，不是 Generic schema，也不允许演化成跨站点 plugin policy language。

## 3. Generic adoption requirements

1. `party-content` / `importPartyHistoricalContent` 的 current Runtime写入必须消费 EU-47 Generic Article capability；不得继续由 Party importer直接调用 `ArticleService` / `ResourceService`复制 Generic create/skip/conflict逻辑。
2. `party-carousel` / `importPartyCarousel` 的 current steady-state create/skip/conflict必须消费 Generic ListItem capability。
3. Party-specific canonical shape若与 Generic canonical model存在字段差异，可以由 bounded Party adapter做 shape normalization；adapter只负责 Party validation / provenance / normalization，不复制 Generic Runtime mutation pipeline。
4. Generic capability可以增加由真实 Party consumer证明必要的 site-neutral薄能力，例如：
   - prepared/validated dataset entry；
   - separate source provenance URL；
   - safe optional static target override以保持 accepted public resource projection；
   但不得引入 Party name、alias、fingerprint、固定 list count或 public plugin SPI。
5. Party adapter必须从 dataset / compatibility authority读取 scope与transition facts，不从 Kotlin literals维护第二份可变事实来源；稳定且属于 Party adapter自身责任的 projection规则可以作为 bounded implementation constant保留，不得提升为 Generic policy。
6. Existing root Gradle tasks与 executable dispatcher命令保持兼容；Phase 2C不是命令行接口重命名工作。

## 4. Compatibility transition requirements

1. Generic mapping默认语义继续是 CREATE / SKIP / CONFLICT；Phase 2C 不修改 Generic Engine使任意 fingerprint变化可 UPDATE。
2. 只有 Party compatibility authority显式列出的 transition可以执行原位 update。
3. transition开始前必须验证：
   - current mapping identity与 compatibility authority 的 `fromFingerprint`匹配；
   - Runtime ListItem仍是 accepted old source type / list / stable id / enabled/order/title/url/image projection等必要 guard；
   - current canonical item提供的 current fingerprint、target Article stable identity、image digest等 current target事实自洽；
   - target Article mapping已经可解析；
   - 任何 guard drift均返回 CONFLICT，不覆盖。
4. accepted transition完成后：
   - list-item Runtime id保持不变；
   - mapping fingerprint更新为 current canonical fingerprint；
   - source type / Article relation / image projection与 current canonical状态一致；
   - 再次运行 current import必须全部 SKIP。
5. compatibility update只允许处理已接受的历史迁移状态，不成为 Admin/API普通编辑绕过 source identity immutable contract的通道。

## 5. Behavior-preservation requirements

Phase 2C 必须保持：

- current 183 Party Articles及各 Column scope count；
- current 4 carousel items、stable order与source relation；
- current canonical source bytes / resource SHA-256与provenance意义；
- first fresh import成功，second import idempotent；
- unexpected fingerprint drift仍为 conflict / non-success；
- pinned EU-29 accepted baseline仍可建立 accepted old Runtime；
- EU-29 → current transition只更新 position 2且保持原 list-item id；
- Party public page / Admin / Public API / Browser behavior；
- EU-46 `content-migration → cms-core` application boundary；
- EU-47 Generic package Party-purity；
- Generic Flyway / Site Package / bootstrap/assets semantics。

Phase 2C 不得把 accepted compatibility解释为普通 operator data overwrite policy。

## 6. Report / command compatibility

1. Existing `party-content` / `party-carousel` dispatcher与 root `importPartyHistoricalContent` / `importPartyCarousel` 保持可用。
2. Existing `EU29_IMPORT_REPORT` / `EU29_CAROUSEL_IMPORT_REPORT` machine-readable labels保持，避免破坏现有 repository verification consumer。
3. Party report可以由 Generic result + compatibility transition result投影得到，但必须继续暴露 total / created / updated（carousel）/ skipped / conflicts / invalid / per-item result。
4. conflict / invalid仍必须产生 non-success task/process semantics。
5. Generic `CONTENT_MIGRATION_REPORT` 与 `generic-content` command继续保持 site-neutral，不使用 Party compatibility report。

## 7. Verification requirements

进入 Integration 前至少证明：

1. source inspection证明 Party steady-state importer不再直接复制 Generic Article/List Runtime mutation logic；Generic package继续无 Party hardcode；
2. Party scope aliases / current list identities / current fingerprints来自 repository dataset；accepted old `fromFingerprint` / old-state transition guard来自 Party compatibility authority，current transition target事实来自 current canonical item；
3. Fresh DB current Party import得到 183 Articles + 4 carousel，first CREATE、second全 SKIP；
4. current Party resource bytes与Runtime projection完整，Public/Admin browser regression PASS；
5. current changed fingerprint仍 CONFLICT且不会被 compatibility policy误接受；
6. pinned EU-29 accepted commit仍可由 current importer/adapter建立 181 Articles + 4 old carousel；
7. current dataset在同一 Runtime上只执行一个 accepted position-2 UPDATE，list-item id不变，最终 ARTICLE relation / image bytes / current fingerprint正确；
8. post-upgrade second run = 183 Articles SKIP + 4 carousel SKIP；
9. 任意 fromFingerprint、old Runtime guard、target Article relation或compatibility authority drift均拒绝 update；
10. Canonical Migration Verification、EU-30 Migration Upgrade Verification、Generic Content Migration Verification、Backend Application Boundary、Site Package Verification与 Repository CI / Integrated Browser在 exact head持续 PASS；
11. no DB schema/Flyway/API/frontend/Site Package product semantic change；
12. unresolved review threads = 0，final diff只包含 Phase 2C implementation / canonical compatibility authority / verification / Authority同步。

## 8. Non-goals

Phase 2C 不做：

- 不开始 Main Site historical collection/import；
- 不进入 Phase 3 compatibility re-entry decision；
- 不进入 Issue #60 / E1～E3；
- 不修改 Generic CMS DB schema / Flyway；
- 不创建新的 Backend application / Gradle domain module；
- 不建立通用 policy DSL、plugin framework、script runtime或跨项目 migration SDK；
- 不改变 Site Package stable structure/bootstrap/assets ownership；
- 不重写 Public Renderer或Admin功能；
- 不拆 Repository；
- 不更新 `agentic-dev` baseline。

## 9. Requirement readiness

EU-47 已关闭 Generic Engine prerequisite；current Repository已直接暴露 Party hardcode、current canonical dataset、pinned EU-29 baseline verification与唯一 accepted position-2 transition。Goal、preservation obligations、Party-vs-Generic ownership与failure boundary均可由当前 evidence定义，无未决产品决策。

本 Requirement **READY**。后续必须形成 Ready Specification与必要 Technical Plan，再执行 `slice-work → readiness-check`；本文件不授予 Execute Authority。