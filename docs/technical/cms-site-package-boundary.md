# CMS Core / JilinJobs Site Package Boundary Technical Plan

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- `docs/specifications/cms-site-package-boundary.md`
- GitHub Issue #77
- `docs/technical/cms-architecture.md`
- `docs/technical/public-frontend-replaceability.md`
- `data-migrations/README.md`

## Status

- Technical Planning: Ready for slice-work after integration
- Execution Units: not yet created

## Decision

在 Issue #60 / E1～E3 前，先完成 **Generic CMS Core + JilinJobs Site Package + Historical Migration + Replaceable Public Renderer** 四层边界收敛。

当前不拆 Git Repository，不替换 Public 技术栈，也不把 Site Definition 简单移动到 `data-migrations/`。先在单仓内建立可验证的逻辑 / 数据 ownership；完成后再以实际剩余耦合作为 Repository Split Readiness 的输入。

## 1. Current state audit

### 1.1 Generic CMS Core 已具备的基础

当前 Backend 已有通用领域对象与 Admin/Public contracts：

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

### 1.2 当前 Site-specific ownership leakage

`backend/src/main/resources/db/migration/V2__current_preset_data.sql` 当前把两类职责放在同一个 Flyway migration：

- CMS Core 数据库初始化能力；
- 吉林就业网站当前正式 Site Definition / initial operational values。

已确认属于 Site-specific 的数据类别包括：

- Main / Party 栏目树；
- PageGroup / Page 基线；
- `MAIN / HOME_SHORTCUT / HOME_QUICK` 导航位置与导航树；
- JilinJobs SiteConfig values，包括站点名称、品牌资源、联系信息、备案 / Footer；
- `HOME_CAROUSEL / SITE_* / PARTY_CAROUSEL` 等当前 Site list definitions；
- 当前首页 carousel / website-link list members；
- `HOME_RECRUITMENT_PROMO` 展示位及当前展示内容；
- 这些对象引用的 `site-baseline/static/**` 稳定站点资源。

Technical implementation 开始前仍必须输出逐记录 classification audit，确认是否存在真正应留在 Generic Core 或应进入 Historical Migration 的例外。

### 1.3 Existing asset boundary

`site-baseline/static/**` 已经与 Public Renderer 源码分离，并由 Backend `CMS_STATIC_ROOT` 暴露 `/static/**`。它在语义上属于 JilinJobs Site Package；物理目录是否移动不是第一优先级。

`data-migrations/**` 已承担 Party canonical historical content，并定义 Main 后续迁移规则。该目录保持 Historical Content Migration 语义。

## 2. Proposed in-repository composition

首轮实现优先采用最小、显式、可审计的结构，不提前设计插件框架。

推荐逻辑结构：

```text
backend/                         # Generic CMS Backend / schema
frontend/admin/                  # Generic CMS Admin
frontend/public-site/            # Current replaceable renderer
sites/
└── jilinjobs/
    ├── manifest.json            # package identity / version / integrity
    ├── structure/               # stable site definition
    │   ├── columns.json
    │   ├── pages.json
    │   ├── navigation.json
    │   ├── lists.json
    │   ├── advertisements.json
    │   └── site-config.json
    └── static/                  # stable site assets
        └── ...
data-migrations/
├── party/
└── main/                        # future E3
```

该目录只是当前 Technical Plan 的 preferred proposal。Slice Work 可以在不破坏四层 contract 的前提下，将 `site-baseline/static/**` 暂时保留原路径并先建立 manifest / structure；若一次移动所有静态资源只制造路径 churn，应推迟物理迁移。

## 3. Site Package format

### 3.1 Manifest

最小 `manifest.json` 应包含：

- `packageId`: stable string，例如 `jilinjobs`；
- `schemaVersion`: Site Package 格式版本；
- `version`: 当前 Site Definition 版本；
- structure file list / digest；
- static root / integrity metadata；
- compatibility: 最低支持的 CMS Site Provisioning contract version（只有实现确实需要时才增加）。

不加入 tenant、plugin dependency graph、marketplace metadata 等未被当前 Consumer 需要证明的字段。

### 3.2 Structure representation

结构文件采用人可审计、机器可校验的 JSON。优先按领域拆分，避免一个巨大 SQL / JSON 文件成为新的 V2。

稳定引用使用 logical identity：

- Column alias；
- PageGroup alias + Page alias/context；
- NavigationLocation code；
- List code；
- AdvertisementSlot code；
- SiteConfig key；
- Navigation target 通过上述 logical identity 表达；
- Static asset 通过 Site-relative path / public path mapping 表达。

### 3.3 Navigation identity

当前 NavigationItem 没有独立 code。首轮实现不应为了 Site Package 美学立即增加新数据库字段。

优先以 `position + parent logical path + sort/order + target semantic identity` 等现有稳定事实形成 provisioning key，并在 preflight 中检测歧义。只有 audit 证明无法稳定 reconcile 时，才返回 Requirement / Specification 判断是否需要稳定 NavigationItem identity 字段。

## 4. Provisioning mechanism

Preferred mechanism：独立 **Site Provisioner**，由 Repository-owned data files 驱动，复用 Backend domain/service/mapper 或专用窄写入层完成受控初始化。

要求：

1. Flyway 先建立 Generic CMS schema；
2. Site Provisioner 校验 package；
3. 按 logical identity upsert/reconcile stable site structure；
4. Site Package 建立的结构对象写入 `preset=true`；
5. 第二次 apply 必须幂等；
6. 与 existing preset object identity 冲突但内容不可安全收敛时失败并报告，不静默复制；
7. Runtime operator-created `preset=false` 数据不被 package broad-delete / broad-reset；
8. package 中不再存在的 preset object 如何处理，首轮默认 **不自动删除**，除非 future package upgrade requirement 明确定义 deprovision semantics；
9. Provisioning 完成后输出 deterministic verification summary。

### Why not Flyway site SQL

继续用 Flyway 维护 Site Definition 会让具体站点 identity 与 CMS schema lifecycle 绑定，无法达到 Generic Core 目标。

### Why not Historical Migration importer

Historical Migration 解决 legacy operational content provenance、fingerprint 和 import；Site Provisioning 解决稳定站点结构。两者生命周期与冲突语义不同，不能用同一数据契约混合。

### Why not generic plugin framework

当前只有一个真实 Site Package；通过一个最小 provisioner + explicit package format 已足以验证边界。没有证据支持插件 lifecycle、动态 discovery 或多租户 runtime framework。

## 5. Migration from current V2 baseline

首轮代码迁移必须保护 EU-31 accepted Fresh DB baseline 语义。

目标不是直接删除 V2 后让旧数据库失效，而是建立新的 clean baseline / upgrade path：

- 对当前仍处于开发期、允许 baseline convergence 的事实，需要在 Slice Work 中重新确认 EU-31 append-only boundary 与当前 Repository Authority；
- 若当前 Authority 不允许再次重写 active V2，则通过 append-only migration + Site Provisioner compatibility 把职责迁出；
- 只有 Requirement / Database Migration Authority 明确允许新的 baseline convergence，才重写 V2；不得仅因为 Site Package 更干净而破坏已接受 schema migration contract。

无论采用哪种路径，最终需要证明：

```text
Fresh DB + CMS schema + Site Package
== accepted current formal site structure
```

并证明已有数据库升级不会丢失当前 preset / Runtime 数据。

## 6. Static assets

分两阶段处理：

### Phase A — ownership first

- 文档 / manifest 将 `site-baseline/static/**` 声明为 JilinJobs Site Package owned assets；
- Provisioning / verification 引用 stable public paths；
- 不因目录名不理想强制移动所有二进制资源。

### Phase B — physical relocation only if justified

若完成 Provisioning 后 `site-baseline/` 名称持续制造 ownership ambiguity，才把文件移动到 `sites/jilinjobs/static/**`，同步 Backend / CI / Review Environment adapter。

物理 relocation 必须保持 `/static/**` runtime URL 不变。

## 7. Historical content compatibility

Party `data-migrations/party/v1` 的 canonical dataset 不重写 provenance。

Site Package 收敛需要证明：

- Party target column aliases / list codes 在 Site Package apply 后存在；
- canonical importer 不依赖 V2 Runtime numeric IDs；
- 183 篇 current Runtime Dataset 与 Party carousel reconciliation 保持；
- EU-29 acceptedSnapshot provenance 不改变。

E3 Main Migration 只有在该兼容性验证完成后进入执行规划。

## 8. Public Renderer / CI boundary

首轮不修改 Public Framework。

CI 允许继续在当前 Repository：

- build Backend；
- build Public Renderer；
- build Admin；
- apply Flyway + Site Package + canonical migrations；
- run Public/Admin/Integration Browser tests。

但验证数据准备顺序必须从隐含的“Flyway V2 自带 Site”改成显式：

```text
Flyway schema
→ JilinJobs Site Provisioning
→ optional canonical historical migration
→ runtime verification
```

这一步是未来多 Repository composition 的必要前置证据，但本轮不实现跨 Repo checkout / manifest orchestration。

## 9. Candidate slice decomposition

Technical Plan 完成集成后，`slice-work` 应从以下语义候选中选择纵向、可独立验证的 Candidate Units；以下名称只是 planning slices，**不是 EU Identifier，也不构成 Execute 授权**。

### Slice A — Site Package Contract & Provisioner Foundation

目标：建立 package manifest / schema、structure representation、preflight + idempotent provisioner 和 targeted verification；先用最小代表性站点对象证明 contract，不立即迁空 V2。

Acceptance focus：

- Fresh schema 上 provision；
- second apply idempotent；
- stable identity / relationship resolution；
- `preset=true`；
- operator-created runtime data 不受影响。

### Slice B — Current Site Baseline Migration

目标：完成 V2 classification audit，把当前 JilinJobs Site Definition 的正式 Authority 迁到 Site Package，并建立 Fresh / upgrade compatibility。

Acceptance focus：

- current formal site structure equivalence；
- navigation / page / list / site config / advertisement baseline；
- Admin preset protection；
- Public behavior unchanged；
- current DB upgrade path。

### Slice C — Site Asset Ownership & Runtime Composition

目标：让版本化 Site assets 在 manifest / runtime bootstrap / CI 中由 Site Package 显式拥有；只有必要时才物理迁目录。

Acceptance focus：

- stable `/static/**` URLs；
- clean runtime bootstrap；
- no Public Renderer source ownership regression；
- Review Environment / CI reproducibility。

### Slice D — Canonical Migration Compatibility & E1～E3 Re-entry

目标：证明 Party canonical migration 与新 Site Package contract 完整兼容，并更新 Main E1～E3 的 Planning Preconditions。

Acceptance focus：

- Party canonical verification；
- 183 runtime articles / accepted carousel state；
- identity/fingerprint compatibility；
- E1/E2/E3 only depend on Site Package + CMS/Public contract。

Slice Work 可以根据 repository audit 合并相邻候选，但不得形成一个“全仓库通用化”巨型 EU。

## 10. Deferred architecture assessment

完成 A～D 的长期边界后，再单独进行 **Repository Split Readiness Assessment**，回答：

- Public Renderer 是否还存在必须与 CMS Core 同 commit 的 source/data coupling；
- Site Package 应留在产品 Authority repo、CMS repo 还是独立 repo；
- integration workspace 谁拥有 exact commit composition；
- cross-repo API contract / Evidence 如何绑定；
- Docs / Code 分仓是否降低还是增加 AI Fresh Context version skew；
- Git Submodule、manifest checkout 或其他 composition 机制哪个最小。

该 Assessment 默认只产出 Architecture / Method evidence，不自动拆仓。

## 11. Verification ladder

每个后续 Slice 按风险取得 Current Evidence，但整体收敛至少需要：

1. package schema / preflight tests；
2. Fresh DB provision；
3. second provision idempotency；
4. representative upgrade / conflict tests；
5. Backend tests；
6. Admin preset behavior；
7. Public build + Public Browser；
8. Party canonical migration verification；
9. Integrated Browser / Review Environment；
10. final diff / Authority audit。

## 12. Rollback boundary

在 Site Package 尚未接管 current baseline 前，Foundation 可以通过放弃 feature branch 回滚。

一旦 current V2 Site Definition 被迁出，集成前必须同时拥有：

- Fresh DB recovery evidence；
- existing DB upgrade evidence；
- Site Package idempotency evidence；
- current Runtime equivalence evidence。

没有这些证据不得移除原 baseline responsibility。
