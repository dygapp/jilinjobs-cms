# Generic Content Migration Application Requirement

## Status

- Parent Planning Authority: GitHub Issue #92 / `docs/project/pre-e1e3-convergence-plan.md`
- Related long-term boundary: GitHub Issue #77 / `docs/requirements/cms-site-package-boundary.md`
- Phase: **Phase 2B — Generic Content Migration Application**
- Requirement: **READY**
- Current repository baseline for planning: `main@0af483a6e5278ad4ac049f33148144214b40669d`
- Phase 2A prerequisite: **COMPLETED via EU-46**
- Current Ready Execution Unit: **NONE until slice-work + readiness-check are integrated**

## 1. Intent

在 EU-46 已建立独立 `content-migration → cms-core` application boundary 的基础上，建立一套真正 site-neutral 的 **Canonical Migration Dataset → CMS Runtime** 导入能力，使后续 Main Site Historical Migration 不需要复制 Party-specific importer，也不需要把 Party / JilinJobs / EU-29 / EU-30 身份写入 Generic Migration Engine。

Phase 2B 只建立 Generic capability 与可独立验证的 generic CLI / service path。当前 Party Article / Carousel commands、183 Articles、4 accepted carousel、EU-29→EU-30 compatibility 与 Party-specific validation 继续保持现状；把 Party dataset/profile/compatibility 切换到 Generic capability 属于后续 Phase 2C。

## 2. Canonical input boundary

1. Generic Migration Application 必须消费 repository-owned、冻结且可离线验证的 Canonical Migration Dataset，不依赖 Legacy Source 在线可用性。
2. Canonical dataset 的长期组织继续以 `data-migrations/README.md` 为基础：stable migration identity、article-level self-contained resources、index + item files、SHA-256/provenance 与稳定 target identity。
3. Generic Article canonical contract 必须保持 site-neutral：source system / legacy key、source fingerprint、target Column alias、Article type、content、resources、evidence；不得限制为 Party aliases 或 JilinJobs-specific typeCode。
4. Generic ListItem canonical contract 必须使用 stable List code、stable legacy identity、source fingerprint、source order 与 LINK / ARTICLE 等当前 CMS 能表达的稳定 source relation；不得固定 `PARTY_CAROUSEL`、固定 4 条、`party-carousel:position:*` 或 EU-29/EU-30 fingerprints。
5. Canonical dataset 不得使用 Runtime DB id 作为长期 identity / dependency；Article-backed ListItem 通过 source system + article legacy key 等稳定 migration identity引用文章。
6. Phase 2B 可以新增 generic list/index JSON schema 与 test fixture contract，但不得替换或重写 `data-migrations/party/v1/**` accepted bytes；Party canonical adoption留到 Phase 2C。

## 3. Generic migration semantics

### 3.1 Stable identity / fingerprint

1. Article 与 ListItem 的 Runtime mapping 必须继续以 `sourceSystem + legacyKey` 为稳定 identity。
2. 不存在 mapping：允许 CREATE。
3. 已存在且 fingerprint 相同：必须 SKIP，保持幂等。
4. 已存在但 fingerprint 不同：默认 CONFLICT，不得静默覆盖。
5. Generic Engine 不得内建任何特定 Party fingerprint 白名单或 upgrade exception。

### 3.2 Canonical validation / preflight

在发生该 dataset 的 Runtime mutation 前，Generic capability 必须完成足够的 preflight：

- JSON / canonical shape 可解析；
- index path 与 item/resource path normalize 后仍位于 snapshot root；
- stable identity / fingerprint 格式有效且 dataset 内无冲突重复；
- resource size / SHA-256 与本地 bytes 一致；
- Article body 中 migration-relative resource reference 可完整解析；
- target Column alias / List code 可通过当前 Generic CMS stable identity找到并处于可导入状态；
- Article-backed ListItem 的 stable article reference存在于本次 dataset 或已接受 mapping 中；
- dependency graph 无缺失引用或无法解析的顺序。

已知 INVALID / CONFLICT 不得在 Generic path 中被静默转成部分成功。Generic report 必须清晰区分 validation/preflight failure 与 execute result。

### 3.3 Article / Resource import

Generic capability 必须支持当前 CMS 已接受的 Historical Article 迁移语义：

- `INTERNAL` 与 `EXTERNAL_LINK`；
- Column stable alias resolution；
- BODY_IMAGE / ATTACHMENT 等当前 canonical resource role；
- resource bytes / size / digest safety；
- migration-relative reference → Runtime managed resource URL rewrite；
- source metadata / publish date / stable source order；
- create + publish；
- legacy mapping记录与 second-import idempotency。

Generic Article importer不得检查 Party alias集合或 `zhutijiaoyu` 等特定 typeCode。

### 3.4 ListItem import

Generic capability 必须至少支持后续 Historical Migration 已有真实证据需要的两类稳定关系：

- LINK ListItem；
- ARTICLE ListItem，按 stable article migration identity解析 Runtime article relation。

Generic path 可消费 canonical image/resource evidence，但不得固定 Party carousel数量、legacy key pattern、固定 list code、固定 static target path或 EU-29→EU-30 LINK→ARTICLE升级例外。

### 3.5 Dependency order / reconciliation / report

1. Generic Engine 必须显式处理依赖顺序，Article-backed ListItem 不得依赖调用顺序碰巧成功。
2. 首次导入、第二次幂等、conflict、invalid、missing target / dependency 必须进入稳定 machine-readable report。
3. Generic CLI 在 report含 conflict / invalid 时必须以失败 exit semantics结束；不得只打印错误后返回成功。
4. Generic report label / schema必须与 Party现有 `EU29_*_REPORT` 分离，避免把 Generic capability伪装成 EU-29 command。
5. Generic reconciliation只确认 canonical stable identity/fingerprint与当前 Runtime mapping/result是否一致；不得在 Phase 2B发明自动覆盖 operator changes或 Party-specific accepted upgrade policy。

## 4. Application / lifecycle requirements

1. Generic capability继续位于 EU-46 已接受的 `backend/apps/content-migration` application，不创建新的 Backend application或新的 repository。
2. `content-migration` 保持 non-web lifecycle，只依赖 `cms-core`，不得重新依赖 `cms-server`。
3. Generic command必须可从 repository `backend/` entry调用，并可从 Content Migration executable dispatcher调用；具体命令名由 Technical Plan冻结。
4. Generic Flyway / CMS metadata / Site Package capability继续由 `cms-core` single authority提供；Phase 2B不复制 SQL / metadata / Site Package instance data。
5. 配置 `cms.site-package.root` 时，Generic import继续依赖稳定 Site identity已 reconcile；普通 Generic import不得隐式启用 one-time Site bootstrap。
6. DB transaction不被描述为可以 rollback filesystem/resource side effect。Generic implementation必须先尽可能 preflight，再执行 bounded Runtime mutation，并对 residual side effects保持可观察。

## 5. Behavior-preservation requirements

Phase 2B 必须保持：

- Admin / Public API 与浏览器行为不变；
- EU-46 application/core dependency boundary不变；
- Generic Flyway current lineage与 schema semantics不变；
- JilinJobs Site Package structure/bootstrap/assets lifecycle不变；
- `data-migrations/party/v1/**` accepted canonical dataset bytes / provenance不变；
- current Party historical article command与 183 Articles结果不变；
- current Party carousel command与 4 accepted items结果不变；
- Party second-import idempotency / fingerprint conflict不变；
- EU-29 accepted → EU-30 current position-2 LINK→ARTICLE compatibility不变；
- current Party report labels与 failure semantics不变。

Generic capability可以与 Party-specific implementation在 Phase 2B暂时共存。该暂时重复必须有明确边界：Generic path不得调用 Party-specific validator/constant，Party current path也不要求在本 Phase切换到 Generic path。

## 6. Verification requirements

进入 Integration 前至少证明：

1. Generic package / service / CLI 在 `content-migration` app内独立编译、测试并保持 non-web composition；
2. 使用不含 `party` / `jilinjobs` / `EU29` / `EU30` identity 的 synthetic canonical fixture，在 Fresh Generic CMS DB上完成 generic first import；
3. 同一 fixture second import为 idempotent SKIP；
4. changed fingerprint产生 CONFLICT且不静默覆盖；
5. path traversal、missing file、size/digest tamper、unresolved resource reference、missing target identity、missing article dependency均得到 INVALID / failed preflight；
6. Generic Article INTERNAL / EXTERNAL_LINK、BODY_IMAGE / ATTACHMENT与 stable Column alias成立；
7. Generic ListItem LINK / ARTICLE stable-reference dependency order成立；
8. Generic report machine-readable且 conflict/invalid导致 CLI非成功；
9. source/code inspection证明 Generic implementation不包含 Party alias、`PARTY_CAROUSEL`、position-2 accepted fingerprints或 EU-29/EU-30 upgrade exception；
10. existing Canonical Migration Verification与 EU-30 Upgrade Verification在 exact head继续 PASS，证明 Party current behavior未漂移；
11. Backend Application Boundary、Site Package Verification、Repository CI与 Integrated Browser继续 PASS；
12. changed-file scope只包含 Generic capability、generic schema/test fixture、必要 build/workflow/verification wiring与 Authority同步。

## 7. Non-goals

Phase 2B 不做：

- 不把 Party current commands切换到 Generic Engine；
- 不删除/重写 Party-specific alias、fingerprint、carousel position或 upgrade compatibility logic；
- 不修改 `data-migrations/party/v1/**` accepted canonical bytes；
- 不开始 Main Site canonical data collection/import；
- 不修改 DB schema / Flyway semantics；
- 不改变 Admin/Public API、页面、视觉或运营行为；
- 不设计跨项目 public plugin SPI、脚本插件系统或新的 migration framework repository；
- 不创建新的 Backend application / Gradle domain module；
- 不进入 Phase 2C、Phase 3 或 Issue #60 / E1～E3；
- 不拆 Git Repository；
- 不更新 `agentic-dev` baseline。

## 8. Requirement readiness

EU-46 已关闭 application/classpath prerequisite；`data-migrations/README.md` 已给出长期 canonical organization；当前 Party implementation真实暴露了可复用的 identity/fingerprint、path/digest、Article/Resource/ListItem、dependency/report语义，同时 Party-specific hardcode边界清晰可识别。

Goal、Scope、行为保持义务、Generic / Party边界与 Acceptance dimensions 已闭合。本 Requirement **READY**。后续必须形成 Ready Specification与必要 Technical Plan，再执行 `slice-work → readiness-check`；本文件本身不授予 Execute Authority。