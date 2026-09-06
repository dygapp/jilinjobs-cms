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
- Current Ready Execution Unit：**NONE**
- Issue #77：**OPEN**，剩余边界继续停留在 Planning / Requirement Candidate 状态，必须重新经过 `slice-work → readiness-check` 才能进入 Execute。

## Decision

在 Issue #60 / E1～E3 前，完成 **Generic CMS Core + JilinJobs Site Package + Historical Migration + Replaceable Public Renderer** 四层边界收敛。

当前不拆 Git Repository，不替换 Public 技术栈，也不把 Site Definition 简单移动到 `data-migrations/`。先在单仓内建立可验证的逻辑 / 数据 ownership；完成后再以实际剩余耦合作为 Repository Split Readiness 的输入。

EU-37 / EU-38 已证明 Site Package contract、narrow provisioner 和一组 stable site structures 可以独立于 Site-specific Flyway SQL 表达并在真实 MySQL 上恢复；但它们**没有**移除 V2 的当前兼容责任，也没有改变默认 Runtime bootstrap，因此 Issue #77 尚未完成。

## 1. Current state after EU-37 / EU-38

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

`preset` 继续是 Generic CMS Core 的通用能力；具体哪些对象属于 JilinJobs preset baseline 则由 Site Package responsibility 表达。

### 1.2 Site Package v1 foundation

EU-37 已建立：

- `sites/jilinjobs/manifest.json` package identity / version / integrity；
- `sites/schema/v1/**` machine-auditable JSON schemas；
- path / SHA-256 / duplicate / field / relation / cycle / ownership preflight；
- narrow `SitePackageProvisioner`；
- create / reconcile / unchanged / `preset=false` ownership conflict；
- Fresh Flyway V1 Generic Schema 上的真实 MySQL verification；
- second apply idempotency。

当前 Site Package schemaVersion 仍为 `1`；EU-38 对 v1 的扩展保持 EU-37 `columns-only` Foundation package 兼容。

### 1.3 Stable structure migrated by EU-38

EU-38 已将以下具有现有 stable logical identity 的 JilinJobs preset structure 写入 `sites/jilinjobs/structure/**`：

- Column → `alias`；
- PageGroup → `alias`；
- Page → `groupAlias + alias`；
- NavigationLocation → `code`；
- SiteConfig → `config_key`；
- CmsList definition → `code`；
- AdvertisementSlot → `code`。

当前物理文件包括：

```text
sites/jilinjobs/
├── manifest.json
└── structure/
    ├── columns.json
    ├── page-groups.json
    ├── pages.json
    ├── navigation-locations.json
    ├── site-config.json
    ├── lists.json
    └── advertisement-slots.json
```

EU-38 targeted verifier 已证明：

```text
Fresh Flyway V1 + Site Package
==
Legacy Flyway V1+V2 + Site Package
```

这里的等价声明只针对 EU-38 已接管的 stable structural snapshot，不包含 NavigationItem、CmsListItem、Advertisement operational members 或 binary assets。

### 1.4 V2 remains compatibility baseline

`backend/src/main/resources/db/migration/V2__current_preset_data.sql` 在 EU-38 中保持 byte-for-byte 未修改。

因此当前仍存在双重事实：

- Site Package 已成为 EU-38 stable domains 的独立、机器可验证表达；
- 默认 Runtime / Fresh DB 仍可通过 V2 获得当前完整站点初始化数据。

这属于有意的过渡兼容状态，不得误写成“V2 responsibility 已移除”。后续要移交默认 Runtime responsibility，必须重新证明 Fresh DB recovery、Existing DB compatibility、Site Package idempotency 和 accepted Runtime behavior。

## 2. Remaining classification boundary

### 2.1 NavigationItem

`cms_navigation` / NavigationItem 当前没有独立 stable code。

EU-38 没有使用不可靠的临时 composite key 强行接管该对象。下一轮 Planning 必须基于 current implementation 重新判断：

1. 现有字段能否形成长期稳定且无歧义的 logical identity；
2. 若不能，是否需要 Requirement / Specification 明确增加 stable identity；
3. 如何证明 parent relation、target semantics、ordering 与 existing operator data 不被误接管。

在该判断完成前，不预设新增数据库字段，也不预设下一个 EU Identifier。

### 2.2 Operational members

以下对象继续不是 stable preset structure：

- `cms_list_item`；
- `cms_advertisement`。

它们属于运营成员；EU-38 verifier 已证明 Legacy path apply Site Package 后这些成员 snapshot 保持不变。

若未来 E1～E3 或 Site Package Runtime composition 对其有新的初始化需求，必须按当时 Requirement 区分“stable provisioning seed”与“historical / operational content”，不得仅因 V2 当前含有数据就把成员升级为 preset ownership。

### 2.3 Historical Migration

`data-migrations/**` 继续只承担历史运营内容 migration authority：

- canonical historical Articles / external links；
- body resources / attachments；
- legacy identity / fingerprint；
- operational list-member migration knowledge；
- importer / report / provenance。

Site Definition 不并入 `data-migrations/**`。

## 3. Provisioning contract

当前 accepted Site Provisioner contract：

1. Flyway Generic Schema 必须先存在；
2. loader 校验 package manifest、路径、digest 与 structure fields；
3. stable logical identity 用于 create / reconcile；
4. Site Package 建立和接管的结构对象保持 `preset=true`；
5. second apply 必须幂等；
6. existing `preset=false` 同 identity 对象属于 ownership conflict，必须失败而不是接管；
7. package 外 Runtime 数据不得 broad-delete / broad-reset；
8. package 中删除一个既有 preset definition 的 deprovision semantics 当前未定义，默认不自动删除；
9. provision 完成后必须提供 deterministic verification evidence。

当前没有 tenant、plugin dependency graph、dynamic discovery、marketplace metadata 或 multi-site runtime framework。

## 4. Static asset boundary

`site-baseline/static/**` 已经与 Public Renderer 源码分离，并由 Backend `CMS_STATIC_ROOT` 暴露稳定 `/static/**` URL。

语义上这些版本化稳定资源属于 JilinJobs Site Package ownership，但 EU-37 / EU-38 没有移动二进制目录，也没有改变 runtime mount。

### Ownership first

下一步优先证明：

- Site Package / manifest 能明确引用稳定 Site assets；
- Runtime bootstrap、CI、Review Environment 知道这些 assets 属于哪一 Site Package；
- `/static/**` URL contract 不变；
- Public Renderer 不重新拥有这些文件。

### Physical relocation only if justified

只有完成上述 composition 后，`site-baseline/` 的物理位置仍持续制造真实 ownership ambiguity，才考虑迁移到 `sites/jilinjobs/static/**`。

物理 relocation 不是四层边界成立的必要条件，也不能单独成为“通用化”目标。

## 5. Runtime composition boundary

当前默认 Runtime composition 尚未改为显式 Site Package bootstrap；V2 compatibility path 继续有效。

长期目标仍是：

```text
Flyway Generic Schema
→ JilinJobs Site Provisioning
→ optional canonical historical migration
→ runtime verification
→ Replaceable Public Renderer / Admin consumers
```

下一轮若要改变默认启动 / CI 数据准备顺序，必须同时覆盖：

- Fresh DB；
- Existing V1+V2 DB；
- Site Package second apply；
- current Public/Admin behavior；
- canonical historical import compatibility；
- Review Environment reproducibility。

在这些证据闭环前不得删除 V2 当前责任。

## 6. Historical content compatibility

Party `data-migrations/party/v1` 的 canonical dataset 不重写 provenance。

后续 Slice D 需要证明：

- Party target Column aliases / List codes 由 Site Package stable contract 提供；
- canonical importer 不依赖 V2 Runtime numeric IDs；
- 183 篇 current Runtime Dataset 保持；
- accepted Party carousel state 保持；
- EU-29 acceptedSnapshot provenance 不改变；
- E3 Main Migration 可以只依赖 Site Package + CMS/Public contracts，而不是当前 Vue source 或 V2 numeric IDs。

E3 只有在上述 compatibility evidence 完成后重新进入执行规划。

## 7. Public Renderer / CI boundary

当前 Vue/Vite Public Site 继续是 accepted renderer implementation，但不成为 Site Definition / Migration Authority。

CI 可以继续在当前 monorepo 中：

- build Backend；
- build Public Renderer；
- build Admin；
- run Site Package targeted MySQL verification；
- run canonical migration verification；
- run Public/Admin/Integrated Browser tests。

EU-38 新增的 `.github/workflows/site-package-verification.yml` 只在 provisioning / migration / `sites/**` 等相关路径变化时运行，与 Repository CI 互补而不替代全链回归。

## 8. Completed slices

### Slice A — Site Package Contract & Provisioner Foundation

已形成 EU-37，状态 **COMPLETED**。

完成结果：package manifest/schema、preflight、narrow provisioner、Fresh V1 MySQL proof、idempotency、ownership conflict 与 Repository regression。

### Slice B — Current Site Baseline Migration / stable-identity portion

Slice B 经 current classification audit 后没有整体形成一个巨型 EU。

其中具有现有 stable identity 的部分形成 EU-38，状态 **COMPLETED**：

- Columns；
- PageGroups；
- Pages；
- NavigationLocations；
- SiteConfig；
- CmsList definitions；
- AdvertisementSlots。

Slice B 剩余 NavigationItem identity 与 V2/default Runtime composition responsibility 继续回到 Planning。

## 9. Remaining planning slices

以下名称仍只是 planning slices，**不是 EU Identifier，也不构成 Execute 授权**。

### Slice B remainder — Navigation identity & V2/runtime compatibility

目标：解决 NavigationItem stable identity / reconcile contract，并判断 Site Package 何时、如何接管默认 Site bootstrap responsibility。

Acceptance focus 至少包括：

- Navigation logical identity 无歧义；
- existing preset / operator data ownership 安全；
- Fresh / existing DB compatibility；
- V2 responsibility 显式收敛而非静默删除；
- Public/Admin behavior unchanged。

### Slice C — Site Asset Ownership & Runtime Composition

目标：让版本化 Site assets 在 manifest / runtime bootstrap / CI / Review Environment 中由 Site Package 显式拥有；只有必要时才物理迁目录。

Acceptance focus：

- stable `/static/**` URLs；
- explicit site-owned asset composition；
- no Public Renderer source ownership regression；
- CI / Review Environment reproducibility。

### Slice D — Canonical Migration Compatibility & E1～E3 Re-entry

目标：证明 Party canonical migration 与 Site Package contract 完整兼容，并更新 Main E1～E3 的 Planning Preconditions。

Acceptance focus：

- Party canonical verification；
- 183 runtime Articles / accepted carousel state；
- identity / fingerprint compatibility；
- E1/E2/E3 only depend on Site Package + CMS/Public contract。

Slice Work 可以根据最新 Repository audit 合并或重新切分相邻候选，但不得形成一个“全仓库通用化”巨型 EU。

## 10. Deferred architecture assessment

完成四层长期边界与 Runtime/canonical compatibility 后，再单独进行 **Repository Split Readiness Assessment**，回答：

- Public Renderer 是否还存在必须与 CMS Core 同 commit 的 source/data coupling；
- Site Package 应留在产品 Authority repo、CMS repo 还是独立 repo；
- integration workspace 谁拥有 exact commit composition；
- cross-repo API contract / Evidence 如何绑定；
- Docs / Code 分仓是否降低还是增加 AI Fresh Context version skew；
- Git Submodule、manifest checkout 或其他 composition 机制哪个最小。

该 Assessment 默认只产出 Architecture / Method evidence，不自动拆仓。

## 11. Verification ladder

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

已有 EU-37 / EU-38 Evidence 只证明其已完成范围；后续改变 Runtime composition、Navigation identity、assets 或 canonical migration consumer 时，应重新取得受影响 Current Evidence。

## 12. Rollback / responsibility boundary

当前 V2 仍保留 accepted compatibility responsibility，因此 EU-37 / EU-38 的 Site Package 数据可以在不破坏默认 Runtime 的前提下独立验证。

未来一旦开始移除 V2 的 Site-specific responsibility，集成前必须同时拥有：

- Fresh DB recovery evidence；
- existing DB compatibility evidence；
- Site Package idempotency evidence；
- current Runtime equivalence evidence；
- canonical migration compatibility evidence（若该变更影响其 target identities）；
- Public/Admin regression evidence。

没有这些证据不得移除原 baseline responsibility。
