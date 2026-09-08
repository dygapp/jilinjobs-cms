# E1～E3 前置 Repository Authority 与 Migration Architecture 收敛规划

## 1. Status

- Planning source：GitHub Issue #92
- Related architecture authority：GitHub Issue #77
- Downstream candidates：GitHub Issue #60 / E1～E3
- Stage：**Planning Authority — ACTIVE / Phase 2B COMPLETED**
- Phase 0：**COMPLETED**
- Phase 1 Repository Documentation Authority Convergence：**COMPLETED**
- Phase 2A Backend Application / Core Boundary Foundation：**COMPLETED via EU-46**
- Phase 2B Generic Content Migration Application：**COMPLETED via EU-47**
- Current Ready Execution Unit：**NONE**
- Current next gate：**Phase 2C — Party Migration De-specialization & Compatibility Planning Candidate**
- Phase 1 closure Authority：`docs/project/documentation-authority-convergence.md`
- Phase 2A accepted Authority：`docs/requirements/backend-application-core-boundary.md` + `docs/specifications/backend-application-core-boundary.md` + `docs/technical/backend-application-core-boundary.md`
- Phase 2B accepted Authority：`docs/requirements/generic-content-migration-application.md` + `docs/specifications/generic-content-migration-application.md` + `docs/technical/generic-content-migration-application.md` + `docs/work/archive/eu47-generic-content-migration-application-foundation.md`

本文固化 EU-42 之后、Issue #60 / E1～E3 重新进入正式规划之前的总体演进顺序。Phase 名称只承担 Planning identity；只有具体 Unit 经 `slice-work → readiness-check` PASS 才形成 Ready Execution Unit，且后续 Execute 仍需遵守 Fresh Context、base drift 与 Repository Authority Gate。

```text
Planning / Requirement Candidate
  → Requirement / Specification / 必要 Technical Planning
  → slice-work
  → Candidate Execution Unit
  → readiness-check
  → Ready Execution Unit
  → Fresh-context Execute
```

EU-46 与 EU-47 均已完成且 Execute Authority终止。Phase 2C 不继承 EU-47 或更早 Unit 的 Execute Authority。

## 2. Why this planning layer exists

EU-37～EU-42 已完成 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层边界中的 Site Package foundation、stable structure、Navigation stable identity、Runtime composition、one-time bootstrap、Generic Schema separation 与 stable Site asset ownership。

EU-42 后 audit 确认两个不能直接留给 E1～E3 的长期问题：

1. Repository Documentation Authority 需要先完成 Current / superseded / Historical Work 收敛；
2. Historical Migration Application 需要从 Backend Server production classpath / Spring composition中隔离，并形成 site-neutral、可复用、独立运行的 Generic Content Migration capability，再处理 Party-specific compatibility迁移。

第 1 项已由 Phase 1 / EU-43～EU-45 完成。第 2 项中的 application/core isolation 已由 Phase 2A / EU-46 完成，Generic Content Migration foundation 已由 Phase 2B / EU-47 完成；剩余 Party-specific de-specialization / compatibility由 Phase 2C 独立规划，避免把 Generic Engine foundation与 Party compatibility policy重新耦合。

## 3. Overall sequence

```text
Phase 0  Planning Authority Solidification — COMPLETED
        ↓
Phase 1  Repository Documentation Authority Convergence — COMPLETED
        ↓
Phase 2A Backend Application / Core Boundary Foundation — EU-46 COMPLETED
        ↓
Phase 2B Generic Content Migration Application — EU-47 COMPLETED
        ↓
Phase 2C Party Migration De-specialization & Compatibility — CURRENT PLANNING CANDIDATE
        ↓
Phase 3  Canonical Migration Compatibility & E1～E3 Re-entry Gate
        ↓
Issue #60 / E1～E3

后置：Phase 4 Repository Split Readiness Assessment
```

当前 **Ready Execution Unit = NONE**。下一步只允许开始 Phase 2C Planning，不得直接 Execute。

## 4. Phase 0 — COMPLETED

Phase 0 已完成：

- Issue #92 / #77 / Roadmap / Site Package Planning 总体顺序对齐；
- AR-02 明确两个 executable JAR 不必然推出多个 Gradle project，并要求先冻结 application/core/composition boundary；
- AR-04 blind review再次识别“先 source-set packaging、后 application boundary”的 sequencing flaw；
- lower-cost capable review first、Astra selective escalation 保持 Consumer-local `ADJUST` evidence，不成为默认 Method Gate。

模型 review 只提供 Planning correction evidence，不授予 Execute。

## 5. Phase 1 — COMPLETED

- EU-43 — Current Authority Semantic Reconciliation；
- EU-44 — Canonical Product Authority Consolidation；
- EU-45 — Documentation Information Architecture & Archive Migration。

Phase 1 完成后 Current / archive 的物理边界已稳定，`docs/README.md` 是 Documentation Authority Map；archive 默认不参与 Fresh Context Current Authority 恢复。

## 6. Phase 2A — Backend Application / Core Boundary Foundation — COMPLETED

### 6.1 Accepted Authority

- Requirement：`docs/requirements/backend-application-core-boundary.md`；
- Specification：`docs/specifications/backend-application-core-boundary.md`；
- Technical Plan：`docs/technical/backend-application-core-boundary.md`；
- Completed Work artifact：`docs/work/archive/eu46-backend-application-core-boundary-foundation.md`。

### 6.2 Accepted build / ownership result

EU-46 建立标准 Gradle multi-project：

```text
backend/
├── modules/
│   └── cms-core/
└── apps/
    ├── cms-server/
    └── content-migration/
```

Dependency：

```text
cms-server ───────────→ cms-core
content-migration ────→ cms-core
```

Accepted ownership：

- `cms-core`：site-neutral domain / validation / persistence / Mapper / transaction / Resource / StaticResource service / Generic Flyway / Site Package capability / CMS metadata；
- `cms-server`：`CmsApplication`、Admin/Public HTTP transport、MVC/static HTTP、server-only composition与兼容 maintenance CLI；
- `content-migration`：独立 non-web Spring composition与 migration-only CLI/import/report；不依赖 Server app。

EU-46 保持 Admin/Public API、Generic Flyway schema semantics、Site Package lifecycle、Party current canonical 183 Articles + 4 accepted carousel、idempotency、fingerprint conflict、EU-29→EU-30 compatibility 与 resource/report semantics。其 Execute Authority已终止。

## 7. Phase 2B — Generic Content Migration Application — COMPLETED VIA EU-47

### 7.1 Accepted Authority

- Requirement：`docs/requirements/generic-content-migration-application.md`；
- Specification：`docs/specifications/generic-content-migration-application.md`；
- Technical Plan：`docs/technical/generic-content-migration-application.md`；
- Completed Work artifact：`docs/work/archive/eu47-generic-content-migration-application-foundation.md`。

### 7.2 Accepted Generic boundary

目标链路：

```text
Legacy Source
→ collection / normalization / promotion
→ Canonical Migration Dataset
→ Generic JVM Content Migration Application
→ CMS Runtime
```

Generic capability负责 canonical validation、path/digest safety、stable migration identity/fingerprint、preflight、dependency order、Article/Resource/ListItem import、legacy mapping、idempotency/conflict/report。

不得内建 Party / JilinJobs / EU-29 / EU-30 identity。Party aliases、`PARTY_CAROUSEL`、fixed four items、accepted fingerprints、position-2 LINK→ARTICLE upgrade exception继续属于 Phase 2C Party compatibility authority。

### 7.3 Implementation result

EU-47 在 existing `backend/apps/content-migration` 内完成：

- neutral `ArticleLegacyMappingMapper` / `CmsListItemLegacyMappingMapper` ownership relocation，不改 table/package/SQL semantics；
- site-neutral canonical Article/List Kotlin models与 generic list schemas；
- `load → structural + byte preflight → Runtime target/mapping/dependency preflight → GenericImportPlan → execute → report`；
- root-contained path、index/item一致性、duplicate identity、file size/SHA-256、unresolved canonical reference、Column/List target、CREATE/SKIP/CONFLICT mapping、ARTICLE stable dependency preflight；
- Generic INTERNAL / EXTERNAL_LINK Article、BODY_IMAGE / ATTACHMENT、reference rewrite、create/publish/mapping；
- Generic LINK / ARTICLE ListItem；LINK static image deterministic projection，ARTICLE image managed Resource；
- `generic-content` dispatcher、root `importCanonicalContent`、`CONTENT_MIGRATION_REPORT` 与 non-success conflict/invalid semantics；
- synthetic Fresh DB verifier、Generic source purity proof与 focused workflow。

EU-47 没有新增 application/module/plugin framework，没有 DB schema/Flyway change，没有切换 Party current dataset/profile到 Generic path。

### 7.4 Verification result

Focused Generic verification证明：

- first import：2 Articles + 2 ListItems CREATE；
- second import：全部 SKIP；
- fingerprint change：CONFLICT 且 CLI/task non-success；
- path traversal、missing file、tampered resource、unresolved reference、missing Column/List/dependency、duplicate identity、index/item mismatch均在 preflight失败，且无 Runtime mutation；
- LINK image落到 neutral `/static/migrated/content/lists/**`，ARTICLE ListItem通过 stable article identity建立关系并可使用 managed image Resource；
- Migration context无 HTTP listener / Server transport leakage。

Repository regression持续由 Canonical Migration、EU-30 Upgrade、Backend Application Boundary、Site Package Verification、Repository CI / Integrated Browser验证；`data-migrations/party/v1/**` bytes与 Party current 183 Articles / 4 carousel compatibility保持。

EU-47 Execute Authority在 completion后终止，不传递给 Phase 2C、Phase 3或 Issue #60 / E1～E3。

## 8. Phase 2C — Party Migration De-specialization & Compatibility — CURRENT PLANNING CANDIDATE

Phase 2C 目标是在已验证 Generic Engine boundary上，将 Party-specific aliases、accepted fingerprints、carousel transition / upgrade-only policy收敛到 Party dataset/profile/compatibility authority，并让 Party current canonical path消费 Generic capability，同时保持 current 183 Articles、4 carousel与 EU-29→EU-30 compatibility。

当前只允许 Planning：

1. 重新基于 integrated `main` 做 dependency closure；
2. 形成 / 更新 Phase 2C Requirement、Specification 与必要 Technical Planning；
3. `slice-work` 后才产生 Candidate Execution Unit；
4. `readiness-check` PASS 后才产生新的 Ready Execution Unit；
5. 后续 Execute仍需 Fresh Context revalidation。

Phase 2C 当前没有 Candidate Execution Unit、Ready Execution Unit 或 Execute Authority；不得从 EU-47 archive、Roadmap phase name或 Issue编号推导 Execute Authority。

## 9. Phase 3 — Canonical Migration Compatibility & E1～E3 Re-entry Gate

最终链路：

```text
Generic CMS Schema
→ JilinJobs Site Package stable structure
→ one-time Site bootstrap
→ Generic Content Migration Application
→ Party Canonical Dataset
→ Runtime
→ Replaceable Public Renderer
```

全部 Current Evidence闭环时记录 compatibility closure / E1～E3 re-entry PASS；发现真实 implementation gap 时才重新 `slice-work`。Phase 3 PASS 前不得进入 Issue #60 / E1～E3 Execute。

## 10. Phase 4 — Repository Split Readiness Assessment

继续 deferred。只有四层 boundary完整闭环后独立评估；不自动拆仓，也不默认阻塞 E1～E3。

## 11. Fresh Context Gate

下一次继续 Issue #92 / #77 时必须重新读取：

1. current `main`、Open PR / Issue、最近相关 Actions；
2. `AGENTS.md`、Root `README.md`、`docs/README.md`；
3. Roadmap / Development Method；
4. Issue #92 / #77 与本总体规划；
5. 当前 Phase 2C Planning Authority / Current Evidence。

`docs/work/archive/eu47-generic-content-migration-application-foundation.md` 只承担 completed traceability，不得作为 Phase 2C Execute Authority。只有 Phase 2C 独立完成 Planning → `slice-work → readiness-check`，形成新的 Ready Execution Unit并在后续 Fresh Context核验无 drift / blocker 时，才允许进入 Execute。不得提前进入 Phase 3 或 Issue #60 / E1～E3。
