# CMS Core / Site Package / Public Renderer 规划状态

## 当前结论

Issue #77 继续承担 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层架构边界 Authority。

已完成并集成的长期基础包括：EU-37～EU-42 Site Package Boundary、Issue #92 Phase 1 EU-43～EU-45 Documentation Authority Convergence、Phase 2A **EU-46 — Backend Application / Core Boundary Foundation**、Phase 2B **EU-47 — Generic Content Migration Application Foundation**，以及 Phase 2C **EU-48 — Party Migration De-specialization & Compatibility**。

EU-47 已建立 site-neutral Generic Canonical Dataset → CMS Runtime capability；EU-48 已把 Party current historical migration收敛为 Party canonical dataset / bounded compatibility authority + Generic Engine consumer，同时保持 accepted old→current compatibility。Phase 3 已进一步完成完整四层链路 compatibility closure，E1～E3 re-entry verdict = **PASS**，且未发现需要新增 Phase 3 implementation Unit 的 gap。

- Current Ready Execution Unit：**NONE**；
- EU-48 Execute Authority：**TERMINATED on completion**；
- Issue #77：**OPEN / long-term four-layer architecture authority**；
- Phase 3 compatibility / re-entry：**COMPLETED / PASS**；
- Issue #60 / E1～E3：**Planning / Requirement Candidates；re-entry allowed, no Execute Authority**；
- Repository Split Readiness Assessment：**deferred / independent**。

跨 Issue 前置演进路线由 GitHub Issue #92 与 `docs/project/pre-e1e3-convergence-plan.md` 承载；该路线已完成 Phase 0～Phase 3 closure。Repository Documentation Authority Convergence 属于 Issue #92 的 Repository Governance / Knowledge Architecture 范围，不并入 Issue #77 产品架构 Requirement。

## 已完成的四层边界基础

### EU-37～EU-39 — Contract / stable structure / Navigation identity

已建立 Site Package manifest、narrow idempotent provisioner，并把 Column、PageGroup、Page、NavigationLocation、SiteConfig、CmsList definition、AdvertisementSlot 与稳定 NavigationItem 表达为具有 stable identity 的 JilinJobs Site Package structure。

### EU-40 — Explicit Runtime composition

Repository Runtime / importer 已可以在 Flyway 后显式执行 `cms.site-package.root` Site Package reconcile；未配置 root 时 Generic CMS context 不隐式启用 Site Package。

### EU-41 — Site bootstrap / Generic Schema baseline separation

```text
Generic CMS Backend
  Flyway Schema-only lineage
        ↓
JilinJobs Site Package
  stable structure reconcile
        ↓
  one-time current-schema bootstrap
        ↓
optional Historical Canonical Migration
        ↓
Runtime
```

Backend active migration target仍为 current Generic V1/V2；JilinJobs data 不占用 Backend migration number。Fresh Site bootstrap完成后其初始运营数据成为 ordinary operator-managed Runtime Data，repeated bootstrap / ordinary restart 不 overwrite 或 resurrect。

### EU-42 — Stable Site asset ownership / Runtime projection

- `sites/jilinjobs/assets/**` 是 stable JilinJobs Site asset 的唯一版本化 source owner；
- manifest 固化 source、公开 `/static/**` target 与 SHA-256；
- Runtime projection 只 create missing targets；
- stable targets 进入 StaticResource protected-path；
- `/static/uploads/**` 与 Historical Canonical Migration assets 不被 stable Site asset ownership接管。

历史完成证据位于 `docs/work/archive/` 与 GitHub Current Evidence；archive record 不重新授予 Execute Authority。

## 当前剩余架构顺序

```text
Phase 2A Backend Application / Core Boundary — EU-46 COMPLETED
        ↓
Phase 2B Generic Content Migration Application — EU-47 COMPLETED
        ↓
Phase 2C Party Migration De-specialization & Compatibility — EU-48 COMPLETED
        ↓
Phase 3 Canonical Migration Compatibility & E1～E3 Re-entry Gate — COMPLETED / PASS
        ↓
Issue #60 / E1～E3 — PLANNING / REQUIREMENT CANDIDATES

Repository Split Readiness Assessment — independent / deferred
```

### Phase 2A / EU-46 — Backend Application / Core Boundary — COMPLETED

Accepted Authority：

- `docs/requirements/backend-application-core-boundary.md`；
- `docs/specifications/backend-application-core-boundary.md`；
- `docs/technical/backend-application-core-boundary.md`；
- `docs/work/archive/eu46-backend-application-core-boundary-foundation.md`。

Accepted dependency：

```text
cms-server app ───────────→ cms-core
content-migration app ────→ cms-core
```

EU-46 已通过标准 Gradle multi-project建立：

```text
backend/
├── modules/
│   └── cms-core/
└── apps/
    ├── cms-server/
    └── content-migration/
```

并保持：

- Core 不依赖 app，两个 app 互不依赖；
- Server持有 HTTP/MVC/static HTTP/server-only composition；
- Migration持有独立 non-web migration composition，不依赖或扫描 Server app；
- Generic Flyway、CMS metadata、Site Package lifecycle保持 single shared authority；
- current Server JAR repository consumer path/name保持。

EU-46 Execute Authority已随 integration closure终止。

### Phase 2B / EU-47 — Generic Content Migration Application — COMPLETED

Accepted Authority：

- Requirement：`docs/requirements/generic-content-migration-application.md`；
- Specification：`docs/specifications/generic-content-migration-application.md`；
- Technical Plan：`docs/technical/generic-content-migration-application.md`；
- Completed Work artifact：`docs/work/archive/eu47-generic-content-migration-application-foundation.md`。

EU-47 在 existing `backend/apps/content-migration` 内建立了 Generic boundary，而没有新增 application/module/plugin framework：

```text
Canonical Migration Dataset
        ↓
load / structural+byte preflight
        ↓
Runtime target / mapping / dependency preflight
        ↓
Generic Article / ListItem import
        ↓
CMS Runtime + stable legacy mapping + report
```

Accepted capability包括：

- neutral Article/ListItem legacy mapping ownership；
- site-neutral canonical Article/List models 与 generic list schemas；
- normalized root-contained path、index/item一致性、duplicate identity、resource size/SHA-256、canonical reference、Column/List target、mapping CREATE/SKIP/CONFLICT 与 ARTICLE dependency preflight；
- INTERNAL / EXTERNAL_LINK Article、BODY_IMAGE / ATTACHMENT、LINK / ARTICLE ListItem；
- ARTICLE-backed ListItem通过 stable article migration identity建立关系；
- LINK image使用 deterministic `/static/migrated/content/lists/<LIST_CODE>/<sha256>.<ext>`，ARTICLE image使用 managed Resource；
- `generic-content` dispatcher、root `importCanonicalContent`、`CONTENT_MIGRATION_REPORT`；
- Fresh DB Generic verifier、Generic source purity proof与 non-web application boundary verification。

EU-47 Execute Authority在 Unit completion 后终止，不传递给 Phase 2C。

### Phase 2C / EU-48 — Party Migration De-specialization & Compatibility — COMPLETED

Accepted Authority：

- Requirement：`docs/requirements/party-migration-despecialization-compatibility.md`；
- Specification：`docs/specifications/party-migration-despecialization-compatibility.md`；
- Technical Plan：`docs/technical/party-migration-despecialization-compatibility.md`；
- Completed Work artifact：`docs/work/archive/eu48-party-migration-despecialization-compatibility.md`。

Accepted result：

```text
Party canonical dataset / compatibility authority
        ↓
Party bounded adapter / exact compatibility guard
        ↓
Generic Content Migration capability
        ↓
CMS Runtime
```

- Party current aliases/current identities/current fingerprints继续来自 `data-migrations/party/v1/manifest.json` / Article index / list index-items，而不是 Kotlin allow-list或 fingerprint常量；
- `data-migrations/party/v1/compatibility.json`只保存 current canonical无法表达的 accepted old-state transition最小事实：exact identity、old `fromFingerprint`、old source type、preserve Runtime id；current target facts继续 dataset-owned；
- Party Article/List adapter只承担 Party-specific shape validation/provenance/normalization，steady-state Runtime mutation委托 EU-47 Generic prepared-dataset path；
- Generic extension保持 site-neutral：prepared entry、shared path/size/SHA verification、source provenance与 safe static target；无 Party identity、general update、plugin/policy framework；
- exact Party compatibility guard只允许 accepted position-2 LINK→ARTICLE原位 transition，保持 Runtime list-item id；fingerprint、Runtime source type/order/url/image path/digest或 target Article mapping任一 drift都 CONFLICT/no update；
- `party-content` / `party-carousel`、root Gradle tasks 与 `EU29_*_REPORT` compatibility保持；
- current 183 Articles、4 carousel、fresh first import、second idempotency、changed fingerprint conflict、pinned EU-29→current upgrade与 resource integrity均保持；
- no DB schema/Flyway/API/frontend/Site Package behavior change。

Pre-integration exact-head Party focused、Canonical Migration、EU-30 Upgrade、Generic、Backend Boundary、Site Package 与 Repository CI/Integrated Browser全部取得 Current Evidence；final exact head / merge / Post-Integration evidence由 PR #109 / Actions / Issue #92/#77记录。

EU-48 Execute Authority在 completion 后终止，不传递给 Phase 3。

### Phase 3 — Compatibility & E1～E3 Re-entry — COMPLETED / PASS

Phase 3 在 EU-48 integrated boundary 上重新对账：

```text
Generic CMS Schema
→ JilinJobs Site Package stable structure
→ one-time Site bootstrap
→ Generic Content Migration Application
→ Party Canonical Dataset / compatibility authority
→ Runtime
→ Replaceable Public Renderer
```

Compatibility closure：

- Generic Schema / app-core topology：PASS；
- stable Site structure / one-time bootstrap / stable asset lifecycle：PASS；
- Generic Content Migration site-neutral consumer path：PASS；
- Party bounded adapter + accepted compatibility as real Generic consumer：PASS；
- Runtime / Public Renderer stable contract与 Integrated Browser：PASS；
- Main future migration targets：stable Main Column aliases、List codes、Page/PageGroup identities已存在，`data-migrations/README.md`已定义 Main复用 canonical organization：PASS。

Final PR Head与 squash-integrated main tree一致；Post-Integration Party / Generic / Backend Boundary / Site Package / CI均 PASS，Canonical / EU-30 Upgrade exact-tree evidence可按 claim-level Evidence reuse继续成立。精确 run identity由 Issue #92/#77承担。

**Verdict：Phase 3 re-entry PASS。** 没有 implementation gap，因此不形成新 Candidate EU。Issue #60 / E1～E3可以恢复正常 Planning / Requirement流程，但没有 Ready Unit或 Execute Authority。

## Repository Split Readiness Assessment

继续 deferred。四层 boundary 已完整闭环，因此可以在未来独立评估；Assessment 不等于自动拆仓，也不默认阻塞 E1～E3。

## 当前 Gate

Current Ready Execution Unit：**NONE**。

下一实际 Gate：

1. Issue #92 Phase 0～Phase 3前置路线已经完成；
2. Issue #77继续作为四层长期架构 Authority，不因 Phase 3 PASS关闭；
3. Main formal content工作如继续，应从 Issue #60 / E1～E3 Planning / Requirement Candidates恢复当前产品边界与依赖；
4. Phase 3 PASS只解除 planning block，不预先决定 E1/E2/E3顺序、scope、Specification、slice或EU编号；
5. 只有具体候选完成 Requirement / Specification / 必要 Technical Planning、`slice-work`形成 Candidate Execution Unit且`readiness-check` PASS后，才允许进入对应 Fresh-context Execute；
6. `docs/work/archive/eu48-party-migration-despecialization-compatibility.md`只承担历史完成证据，不构成后续 Execute Authority。
