# CMS Core / JilinJobs Site Package Boundary Technical Plan

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- `docs/specifications/cms-site-package-boundary.md`
- GitHub Issue #77
- `docs/technical/cms-architecture.md`
- `docs/technical/public-frontend-replaceability.md`
- `data-migrations/README.md`

## Status

- Technical Planning：**ACTIVE / ACCEPTED**
- Completed Execution Units：
  - `EU-37 — Site Package Contract & Provisioner Foundation`
  - `EU-38 — Stable Site Structure Package Migration`
  - `EU-39 — Navigation Stable Identity & Site Package Reconcile`
  - `EU-40 — Explicit Site Package Runtime Composition Activation`
- Current Ready Execution Unit：**NONE**
- Issue #77：**OPEN**。EU-40 之后的 Operational Seed Classification & V2 Responsibility Retirement、Slice C、Slice D 仍是 Planning / Requirement Candidates，必须重新经过 `slice-work → readiness-check` 才能进入 Execute。

## Decision

在 Issue #60 / E1～E3 前，完成 **Generic CMS Core + JilinJobs Site Package + Historical Migration + Replaceable Public Renderer** 四层边界收敛。

当前不拆 Git Repository，不替换 Public 技术栈，也不把 Site Definition 简单移动到 `data-migrations/`。先在单仓内建立可验证的逻辑 / 数据 ownership；完成后再以实际剩余耦合作为 Repository Split Readiness 的输入。

EU-37～EU-39 已证明 Site Package contract、narrow provisioner、stable site structures 与 Navigation identity 可以独立表达；EU-40 进一步把 Site Package reconcile 激活为正式 Runtime / importer composition lifecycle，并已集成到 `main` 取得 Post-Integration Current Evidence。**V2 compatibility responsibility 仍未移除**，因此 Issue #77 尚未完成。

## 1. Current state after EU-37 / EU-38 / EU-39 / EU-40

### 1.1 Generic CMS Core

当前 Backend 保留通用领域对象与 Admin/Public contracts：

- Column / Article；
- Page / PageGroup；
- Navigation / NavigationLocation；
- CmsList / CmsListItem；
- Advertisement / AdvertisementSlot；
- SiteConfig；
- Resource / StaticResource；
- `preset` 结构保护；
- Public managed-resource projection。

EU-36 已证明 Public production source 可以只消费 Public contracts，不再依赖 Admin endpoint knowledge。

`preset` 继续是 Generic CMS Core 的通用能力；具体哪些对象属于 JilinJobs preset baseline 由 Site Package responsibility 表达。

EU-39 新增的 `cms_navigation.code` 同样属于 Generic CMS Core 的 provisioning capability：

- `VARCHAR(100) NULL`；
- unique index；
- nullable 允许 ordinary operator-created navigation 不加入 Site Package identity；
- schema 不包含 JilinJobs-specific code value。

### 1.2 Site Package v1 foundation

EU-37 已建立：

- `sites/jilinjobs/manifest.json` package identity / version / integrity；
- `sites/schema/v1/**` machine-auditable JSON schemas；
- path / SHA-256 / duplicate / field / relation / cycle / ownership preflight；
- narrow `SitePackageProvisioner`；
- create / reconcile / unchanged / `preset=false` ownership conflict；
- Fresh Generic Schema 上的真实 MySQL verification；
- second apply idempotency。

Site Package `schemaVersion` 仍为 `1`；EU-38 / EU-39 增加可选 structure types，但只声明 `columns` 的 EU-37 Foundation package 继续兼容。

### 1.3 Stable structure migrated by EU-38

EU-38 已将以下 JilinJobs preset structure 写入 `sites/jilinjobs/structure/**`：

- Column → `alias`；
- PageGroup → `alias`；
- Page → `groupAlias + alias`；
- NavigationLocation → `code`；
- SiteConfig → `config_key`；
- CmsList definition → `code`；
- AdvertisementSlot → `code`。

EU-38 targeted verifier 已证明 Fresh V1 + Site Package 与 Legacy V1+V2 + Site Package 在这 58 个 stable structural objects 上等价。

### 1.4 NavigationItem stable identity by EU-39

EU-38 后 current audit 证明 `cms_navigation` 的现有字段不能形成长期 stable composite identity：

- name 可重命名；
- parent/location 可移动；
- sortOrder 可重排；
- target 可在受控 package upgrade 中改变；
- Runtime numeric id 不能进入 Site Package contract。

因此 EU-39 形成并通过 Readiness Check，采用 provisioning-only `code`。

JilinJobs package 当前新增：

```text
sites/jilinjobs/structure/navigation-items.json
sites/schema/v1/navigation-items.schema.json
```

共 40 条正式 NavigationItem，关系使用：

- NavigationItem parent → `parentCode`；
- NavigationLocation → `locationCode`；
- Column target → Column `alias`；
- Page target → `groupAlias + alias`；
- LINK target → URL；
- package row → `preset=true`。

JilinJobs package version 当前为 `0.3.0-navigation-identity`。

### 1.5 V2 remains compatibility baseline

`backend/src/main/resources/db/migration/V2__current_preset_data.sql` 在 EU-38～EU-40 都保持未修改。

因此当前仍是有意的过渡状态：

- Site Package 已成为 58 个 EU-38 structures + 40 个 EU-39 NavigationItems 的独立 Authority 表达；
- EU-40 已让配置了 `cms.site-package.root` 的 Repository Runtime / importer 在 Flyway 后显式 reconcile Site Package；
- V2 仍可提供 compatibility initialization / operational seed，尚未完成 responsibility retirement；
- V3 只增加 Navigation stable identity schema，不把站点导航实例值写入 Flyway。

不得误写成“V2 responsibility 已移除”。下一候选需要先分类 operational seed 的长期 Authority，再决定 V2 responsibility retirement，并重新取得受影响的 Fresh / Existing / canonical / Public/Admin / Review Runtime 证据。

## 2. Navigation adoption / reconciliation contract

### 2.1 Fresh path

Generic Schema 已包含 `cms_navigation.code` capability、但不包含 JilinJobs navigation rows 时：

1. 先 provision stable Column / Page / NavigationLocation dependencies；
2. parent-before-child 排序应用 NavigationItem；
3. 使用 package code 创建 preset navigation；
4. second apply 按 code 定位并全部 unchanged。

EU-39 verifier 对正式 package 的 Fresh first apply 结果为 98 created，其中 NavigationItem 40；second apply 98 unchanged。

### 2.2 Legacy V2 adoption

Existing V1+V2 database 上存在 40 条 `preset=true / code=NULL` legacy navigation。认领流程：

1. 按 package stable identities 解析 parentId / targetColumnId / targetPageId / location；
2. 只在 `preset=true AND code IS NULL` legacy candidates 中比较 accepted baseline 完整语义；
3. 恰好 1 条 match 才原位写入目标 code；
4. 0 条或多条 match 均抛出 `SitePackageValidationException`；
5. 整个 apply 位于单事务中，失败即 rollback；
6. 完成 adoption 后，后续 apply 只按 code reconcile。

EU-39 verifier 已证明：

- 40 条 legacy navigation 全部原位 adoption；
- row count 不增加；
- second apply 98 unchanged；
- operator-created `preset=false / code=NULL` navigation 不变；
- Legacy preset 若被改到无法唯一识别，apply 失败且 coded count 保持 0。

### 2.3 Stable-code upgrade

stable code 建立后，package upgrade 可以在 identity 不变的情况下更新：

- name；
- parent；
- location；
- target semantics；
- open mode / icon；
- sort order / enabled。

代表性 `main-policy` mutation 已证明 rename / move / retarget / reorder 可以由正式 package 恢复，且只更新目标 row。

### 2.4 Target validation

NavigationItem loader 明确拒绝半残 target contract：

- HOME / PLACEHOLDER 不允许 Column / Page / URL / stray PageGroup target；
- COLUMN 必须且只能有 Column alias；
- PAGE 必须有 Page alias，可选 group alias，不允许 Column / URL；
- LINK 必须且只能有 URL；
- parent relation 必须无 cycle；
- referenced stable identity 必须在 package 或现有 preset structure 中可解析。

## 3. Operational member boundary

以下对象继续不是 stable preset structure：

- `cms_list_item`；
- `cms_advertisement`。

它们属于运营成员。EU-38 / EU-39 verifier 与 EU-40 Runtime composition verifier 均保持相关 Legacy operational snapshot / count；显式 Site Package composition 不把它们迁入 stable Site Package structure。

若未来 E1～E3 或 Runtime composition 对其出现新的初始化需求，必须重新区分“stable provisioning seed”与“historical / operational content”，不得仅因 V2 当前含有数据就升级为 preset ownership。

## 4. Historical Migration boundary

`data-migrations/**` 继续只承担 historical operational migration authority：

- canonical historical Articles / external links；
- body resources / attachments；
- legacy identity / fingerprint；
- operational list-member migration knowledge；
- importer / report / provenance。

Site Definition 不并入 `data-migrations/**`。

EU-40 final implementation Head `b3e3dc8c4855c9e17b2dfa2f190a84d0305162e2`：

- Site Package Verification #19：PASS；
- CI #768：Backend / EU-40 Runtime composition / Public / Admin / Integrated Public Browser / Integrated Admin Browser 全部 PASS；
- Canonical #153：PASS；
- EU-30 Upgrade #103：PASS；
- 人工评审环境 #683：PASS。

PR #86 合并后的 `main@b105e553db1ebbc12a2b6665385b94fb977bea06`：

- Site Package Verification #20：PASS；
- CI #769：Backend / EU-40 Runtime composition / Public / Admin / Site Package-enabled Web Runtime / Integrated Public Browser / Integrated Admin Browser 全部 PASS。

EU-40 不包含视觉 / Product Intent 变化，上述 Current Evidence 与 final diff/Authority review 足以关闭本 Unit。

## 9. Completed slices

### Slice A — Site Package Contract & Provisioner Foundation

EU-37：**COMPLETED**。

完成 package manifest/schema、preflight、narrow provisioner、Fresh proof、idempotency、ownership conflict 与 Repository regression。

### Slice B — stable structure portion

EU-38：**COMPLETED**。

完成：

- Columns；
- PageGroups；
- Pages；
- NavigationLocations；
- SiteConfig；
- CmsList definitions；
- AdvertisementSlots。

### Slice B remainder — Navigation identity portion

EU-39：**COMPLETED**。

完成：

- Navigation provisioning-only stable code；
- 40 条 JilinJobs NavigationItem Site Package representation；
- Fresh create；
- Legacy V2 unambiguous in-place adoption；
- stable-code reconcile；
- ambiguous adoption rollback；
- operator data non-takeover；
- Canonical / Upgrade / Repository regression；
- PR #84 Integration 与 `main` Post-Integration Verification。

### Slice B Runtime composition activation

EU-40：**COMPLETED**。

完成：

- `cms.site-package.root` opt-in lifecycle；
- Flyway 后 Site Package reconcile；
- Repository Web Runtime explicit composition；
- Canonical / Upgrade importer explicit composition；
- Review Environment reproducibility；
- operational seed preservation；
- PR #86 Integration 与 `main` Post-Integration Verification。

## 10. Remaining planning slices

以下仍只是 planning slices，**不是 EU Identifier，也不构成 Execute 授权**。

### Operational Seed Classification & V2 Responsibility Retirement

目标：对 V2 中仍存在的 `CmsListItem` / `Advertisement` 等 initial operational seed 进行长期 Authority 分类，并在证据支持时收敛 / 移除 V2 Site-specific compatibility responsibility。

Acceptance focus 至少包括：

- stable provisioning seed 与 historical / operational content 的明确分类；
- Fresh / Existing DB recovery；
- Site Package second apply；
- V2 responsibility 显式收敛而非静默删除；
- canonical import / 183 Articles / carousel compatibility；
- Public/Admin/Integrated Browser unchanged；
- Review Environment reproducibility。

该候选不自动获得新的 EU Identifier；必须重新 current audit / slice / readiness。

### Slice C — Site Asset Ownership & Runtime Composition

目标：让版本化 Site assets 在 manifest / runtime bootstrap / CI / Review Environment 中由 Site Package 显式拥有；只有必要时才物理迁目录。

Acceptance focus：

- stable `/static/**` URLs；
- explicit site-owned asset composition；
- no Public Renderer source ownership regression；
- CI / Review Environment reproducibility。

### Slice D — Canonical Migration Compatibility & E1～E3 Re-entry

目标：在最终 Runtime composition 下证明 Party canonical migration 与 Site Package contract 完整兼容，并更新 Main E1～E3 Planning Preconditions。

Acceptance focus：

- Party canonical verification；
- 183 runtime Articles / accepted carousel state；
- identity / fingerprint compatibility；
- E1/E2/E3 only depend on Site Package + CMS/Public contract。

Slice Work 可以根据最新 Repository audit 合并或重新切分相邻候选，但不得形成一个“全仓库通用化”巨型 EU。

## 11. Deferred architecture assessment

完成四层长期边界与 Runtime/canonical compatibility 后，再单独进行 **Repository Split Readiness Assessment**，回答：

- Public Renderer 是否还存在必须与 CMS Core 同 commit 的 source/data coupling；
- Site Package 应留在产品 Authority repo、CMS repo 还是独立 repo；
- integration workspace 谁拥有 exact commit composition；
- cross-repo API contract / Evidence 如何绑定；
- Docs / Code 分仓是否降低还是增加 AI Fresh Context version skew；
- Git Submodule、manifest checkout 或其他 composition 机制哪个最小。

该 Assessment 默认只产出 Architecture / Method evidence，不自动拆仓。

## 12. Verification ladder

剩余每个 Slice 按风险取得 Current Evidence。整体收敛至少需要按实际声明覆盖：

1. package schema / preflight；
2. Fresh DB provision；
3. second provision idempotency；
4. representative upgrade / conflict；
5. Backend tests；
6. Admin preset behavior；
7. Public build + Public Browser；
8. Party canonical migration verification；
9. Integrated Browser / Review Environment；
10. final diff / Authority audit。

EU-37～EU-40 Evidence 只证明其各自范围；后续改变 V2 responsibility、assets 或 canonical migration re-entry 时，应重新取得受影响 Current Evidence。

## 13. Rollback / responsibility boundary

当前 V2 仍保留 accepted compatibility responsibility；EU-40 已证明显式 Site Package Runtime / importer composition 可以在不破坏当前 Runtime 的前提下独立集成，但这不等于 V2 retirement。

未来一旦开始移除 V2 的 Site-specific responsibility，集成前必须同时拥有：

- Fresh DB recovery evidence；
- existing DB compatibility evidence；
- Site Package idempotency evidence；
- current Runtime equivalence evidence；
- canonical migration compatibility evidence；
- Public/Admin regression evidence；
- Review Environment composition evidence（若默认环境路径改变）。

没有这些证据不得移除原 baseline responsibility。
