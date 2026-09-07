# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap。它只维护当前演进状态、长期边界与下一 Planning Gate；详细 EU 历史、Verification 与阶段追溯由对应 `docs/work/**`、Issue / PR / Actions Evidence 承载。

## 方法基线

```text
dygapp/agentic-dev
Validation Baseline: master@d9fad0da83dbdb61cac5eb9778b0258c6861eef1
Capability Milestone: baseline-2026-09-04-engineering-capability@5be2e6aad29b2be6b8535b3690daf3533ee22a46
```

普通开发优先读取 Consumer-local `AGENTS.md`、`README.md`、`docs/project/development-method.md`、本 Roadmap 与当前 Requirement / Specification / Technical Plan。Planning / Requirement Candidate 在 `slice-work` 前不具有 Execution Unit 身份；只有 `slice-work` 形成 Candidate Execution Unit 且 `readiness-check` PASS 后才允许进入 Execute。Roadmap 顺序、Issue 编号、未来 EU 名称或模型评审结果都不能替代 Readiness。

## 总体路线

| 路线 | 状态 | 当前结果 / Gate |
|---|---|---|
| Consumer Repository Bootstrap ～ Public / Admin 基础能力 | 已完成 | 通用 CMS、独立 Admin / Public frontend、Main / Party Multi-entry、Review / Verification 基础已建立 |
| 中心党建正式页面与历史内容 EU-26～EU-29 | 已完成 | Party 正式栏目、视觉、181 篇 EU-29 acceptedSnapshot 与 canonical migration 基础 |
| EU-30 Carousel Architecture & Behavior Convergence | 已完成 | Party current canonical Runtime Dataset = 183；4 条 accepted carousel；EU-29→EU-30 upgrade compatibility 已接受 |
| EU-31 Database Migration Baseline Convergence | 已完成（历史 baseline） | development DB recreation boundary 与 canonical migration independence 保留；active schema shape 后由 EU-41 修订 |
| EU-32～EU-35 Admin Governance / Rich Text | 已完成 | List definition governance、Admin guidance responsibility、HTML safety、shared Tiptap authoring |
| EU-36 Public Frontend Source Isolation | 已完成 | Public production source 退出 Admin endpoint knowledge，managed resource projection 由 Backend Public contract 承担 |
| EU-37～EU-42 Site Package Boundary | 已完成当前 accepted scope | Site Package contract、stable structure、Navigation identity、Runtime composition、one-time bootstrap、Generic Schema separation、stable asset ownership 均闭环 |
| Issue #92 E1～E3 前置 Repository Authority / Migration Architecture Convergence | **当前 Planning Priority / Phase 1 IN PROGRESS** | Phase 0 已完成；Phase 1 current audit、Authority Clarification 与 EU-43 semantic reconciliation 已完成；Current Ready Execution Unit = NONE；下一 Gate 为 Slice B Planning Candidate |
| Issue #60 / E1～E3 Main Site Formal Content | 前置依赖等待 | 等待 Issue #92 / #77 的 Documentation Authority、Historical Migration / Backend Application Boundary 与 final compatibility re-entry gate |
| Repository Split Readiness Assessment | 后置 Planning Candidate | 只有四层 boundary 完成后独立评估；不自动拆仓，不默认阻塞 E1～E3 |
| Issues #57 / #59 / #60 其他候选 | 规划层保留 | C1/C2、Browser Compatibility、Public Rendering Architecture 等保持独立，不因 Issue #92 自动扩大 |

## 当前已接受长期边界

### 1. Generic CMS Core

- Backend Flyway 只承担 Generic CMS Schema evolution 与 site-neutral provisioning capability；
- Generic CMS 不要求 `notice`、`party`、`guide`、`HOME_CAROUSEL` 或其他 JilinJobs-specific identity 才能成立；
- `preset`、stable identity、bootstrap-state、resource / article / list 等能力可以属于 Generic CMS，但具体 Site 实例值不属于 Core；
- 从 EU-41 accepted baseline 起，下一次 Generic CMS Schema change 从 Backend V3 继续 append-only。

当前 active Flyway：

```text
backend/src/main/resources/db/migration/
├── V1__current_cms_schema.sql
└── V2__site_provisioning_schema_capabilities.sql
```

### 2. JilinJobs Site Package / Provisioning

```text
sites/jilinjobs/
├── structure/**   # stable Site structure reconcile
├── bootstrap/**   # one-time Fresh Site operational defaults
└── assets/**      # stable Site assets + integrity manifest
```

- stable structure 与 operator-managed Runtime Data 分离；
- one-time bootstrap 完成后普通 Runtime / repeated bootstrap 不 overwrite 或 resurrect operator data；
- stable Site assets 使用 manifest SHA-256 + create-if-missing projection + protected-path contract；
- `/static/uploads/**` 不属于 stable Site asset ownership。

### 3. Historical Content Migration

- `data-migrations/**` 承载 Main / Party 历史运营内容、资源、外链、legacy identity / fingerprint 与 provenance；
- Historical Migration 不等同于 Flyway，也不等同于 Site Package stable structure / one-time bootstrap；
- Canonical Dataset 应依赖 stable Site identity，不依赖临时 Runtime DB id 或 Public Renderer 内部实现；
- Party current canonical Runtime Dataset = 183 Articles；EU-29 frozen acceptedSnapshot = 181；4 条 accepted carousel 与 EU-29→EU-30 upgrade-only compatibility 必须长期保持可验证。

### 4. Replaceable Public Renderer

- 当前 `frontend/public-site` 是稳定 Public API / URL / Site Data Contract 的一个 Vue/Vite consumer；
- Public production source 不依赖 `/api/admin/**`；
- Public Renderer 不成为 Site Definition、Canonical Migration、Flyway 或 CMS Domain Authority；
- 当前 Main / Party canonical URL、产品 identity 与已接受行为在后续边界重构中保持。

## 当前 Planning Priority — Issue #92

总体规划文档：

```text
docs/project/pre-e1e3-convergence-plan.md
```

Phase 1 current audit / Ready Specification：

```text
docs/project/documentation-authority-convergence.md
```

Issue #77 继续承担四层产品 / 技术边界；Issue #92 承担跨 Repository Documentation Governance、Historical Migration / Backend Application Boundary 与 Issue #60 re-entry 的总体顺序。

### Phase 0 — Planning Authority Solidification — COMPLETED

Phase 0 已完成；当时没有形成实现 EU，也没有进入 Issue #60 / E1～E3。

已完成结果：

- Issue #92、Issue #77、本 Roadmap、`site-package-planning.md` 与总体规划已统一到新的 E1～E3 前置路线；
- PR #93 已完成 Planning Authority 固化并集成；
- AR-02 完成 `gpt-5.6-sol` / medium 与 `gpt-6-astra` / high paired review，两次 Verdict 均为 `SUPPORTED_WITH_CHANGES`，共同 findings 已修订 Phase 2A；
- AR-04 使用缺陷发现前的冻结两阶段 migration extraction candidate 做 retrospective blind discovery；Sol 与 Astra 均为 `BLOCKING_CONCERN`、Human discovery = `DETECTED`、隐藏 assertions `8/8 PASS`；
- AR-04 证明该已知 sequencing flaw 不需要 Astra 才能发现，当前实验策略收敛为 lower-cost capable review first、仅在 unresolved / conflicting / deliberate second opinion 时选择性升级 Astra；
- PR #94 已固化 AR-04 corpus / evidence lifecycle 并集成；
- 具有跨项目复用价值的模型路由 / blind paired eval Evidence 已提交到 `dygapp/agentic-dev` Issue #71；该 Issue 只是外部 Evidence，不构成本 Consumer 或 `agentic-dev` 的新 Method Authority。

### Phase 1 — Repository Documentation Authority Convergence — IN PROGRESS

Phase 1 已完成 current audit、Authority Clarification、Ready Specification，以及 Slice A / EU-43 的 `slice-work → readiness-check → Execute → Integration → Post-Integration` 闭环。完整分类、dependency closure 与 slice result 以 `docs/project/documentation-authority-convergence.md` 为当前 Authority。当前 Ready Execution Unit：**NONE**。

#### Unit 1A — Canonical Authority Audit & Reconciliation

Current audit 已按 `CURRENT / PARTIALLY_CURRENT / SUPERSEDED / HISTORICAL_EVIDENCE` 分类 Root README、Requirements、Specifications、Technical、Work、`data-migrations/**` 与必要 subtree README gap。

审计确认至少存在以下需要 semantic reconciliation 的 Current drift：

- EU-41 后旧 `V2__current_preset_data.sql` / Flyway Site Data responsibility；
- EU-42 后旧 `site-baseline/static/**` stable Site asset source ownership；
- historical V12/V14/V15/V16 等实现历史被写成 Current lifecycle；
- Issue #77 旧 Slice D direct-next-step wording；
- `pre-e1e3-convergence-plan.md` AR-02-only / “暂不反馈 agentic-dev” experiment wording；
- EU-30 Amendment 尚未折回主 Requirement。

其中前五类四层 boundary / lifecycle / migration / asset / experiment currentness drift 已由 **EU-43 — Current Authority Semantic Reconciliation** 关闭；EU-30 Amendment consolidation 仍属于后继 Slice B。

`slice-work` 将 1A 拆为小型 dependency-ordered slices：

1. **EU-43 — Current Authority Semantic Reconciliation — COMPLETED**：已关闭四层 boundary / lifecycle / migration / asset / verification 的 currentness drift；
2. **Slice B — Canonical Product Requirement Consolidation — NEXT PLANNING GATE / Planning Candidate**：下一步折叠 `information-publishing-eu30-amendment.md` 与相关 CMS current docs，必须重新执行 `slice-work → readiness-check`；
3. Phase 1B Information Architecture / Archive Migration 必须等待前述 semantic currentness 完成。

EU-43 work artifact：

```text
docs/work/eu43-current-authority-semantic-reconciliation.md
```

EU-43 Execute Authority 已随完成终止；Slice B、Phase 1B、Phase 2 与 E1～E3 均不得继承。

#### Planned Unit 1B — Documentation Information Architecture & Archive Migration

- Root README 收敛为稳定项目入口；
- `docs/README.md` 成为 Authority Map；
- Requirements / Specifications / Technical 根级默认只保留 Current canonical Authority；
- `work/` 区分 current 与 historical execution evidence；
- `archive/**` 默认不参与 Fresh Context Current Authority 恢复；
- 更新全部 Current Authority 链接与必要子树 README。

1B 当前仍是 Planning Candidate，不获得 EU-43 Execute Authority。

### Phase 2 — Generic Historical Migration & Backend Application Boundary

#### Planned Unit 2A — Backend Application / Core Boundary Foundation

2A 需要冻结的是 application lifecycle、Spring composition、compile/runtime dependency 与 artifact responsibility，不预先把某个 Gradle 目录形态当成 Requirement。

目标依赖：

```text
cms-server app ──────────→ cms-core
content-migration app ───→ cms-core
```

当前推荐 Architecture Candidate：

```text
backend/
├── modules/
│   └── cms-core/
└── apps/
    ├── cms-server/
    └── content-migration/
```

但两个 executable JAR 本身不逻辑必然要求三个 Gradle project。Technical Planning 必须同时比较 `shared core source set + isolated server/migration source sets + independent BootJar` 的较低复杂度替代；如果能以少量、清晰、长期可维护的 wiring 达到同等 dependency / classpath / Spring composition / resource / test isolation，则允许选择。只在同一个完整 runtime classpath 上增加多个 main / BootJar 不满足长期边界。

无论最终 build shape 如何，都必须：

- `cms-core` 不依赖任一 app，两个 app 互不依赖；
- `cms-server` 持有 HTTP / MVC / static-resource / server-only startup composition；
- `content-migration` 持有 CLI / import / report / migration compatibility composition；
- migration app 不再通过完整 `CmsApplication` 根包扫描获得 Server composition；
- 显式冻结 MyBatis mapper / resources、configuration properties、Jackson、transaction、Generic Flyway 单一 Authority、Site Package lifecycle 与 resource path；
- 区分数据库 transaction 与 migration 文件副作用的 failure / rollback semantics；
- 2A 只做行为保持型 application/core boundary，当前 Party migration entry / V1/V2 compatibility 不在本单元提前泛化；
- CMS Core 内 content / column / listing / resource / navigation 等继续使用 package-level modularity；
- 不扩展为一领域一 Gradle module、Clean Architecture、plugin framework 或 Git Repository split。

详细 ownership、composition、behavior-preservation 与 verification obligations 以 `docs/project/pre-e1e3-convergence-plan.md` 为准。

#### Planned Unit 2B — Generic Content Migration Application

```text
Legacy Source
  → Python / Node collection / normalization / promotion
  → Canonical Migration Dataset
  → JVM Generic Content Migration Application
  → CMS Runtime
```

JVM application 负责 canonical format validation、path / digest safety、stable migration identity / fingerprint、preflight、transaction / file-side-effect boundary、dependency order、Article / Resource / ListItem import、legacy mapping、idempotency、conflict、reconciliation 与 report。

Generic Engine 不得内建 Party / JilinJobs / EU-29 / EU-30 identity。

#### Planned Unit 2C — Party Migration De-specialization & Compatibility

- 当前 Backend production main 的 `Party*Migration*` 应退出 `cms-server` Runtime responsibility；
- Party-specific aliases、accepted fingerprints、carousel legacy transition / upgrade-only policy 继续由 Party dataset / profile / compatibility authority 承载；
- 同一 Generic Migration Application 必须保持 183 Articles、4 carousel、二次幂等、conflict、resource integrity 与 EU-29→EU-30 compatibility。

### Phase 3 — Canonical Migration Compatibility & E1～E3 Re-entry Gate

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

若 Current Evidence 已闭环全部 obligation，则直接记录 compatibility closure / E1～E3 re-entry PASS；只有发现具体 implementation gap 才通过 `slice-work` 形成新的 Candidate Execution Unit。

### Phase 4 — Repository Split Readiness Assessment

继续 deferred，只有四层 boundary 完成后独立评估。Assessment 不等于自动拆仓。

## Architecture Review Eval Experiment

Consumer-local 实验入口：

```text
evals/README.md
evals/architecture/pre-e1e3-convergence-review.json
evals/architecture/ar04-migration-sequencing-review.json
evals/run_architecture_review.py
evals/run_ar04_review.py
```

当前实验结论：**ADJUST**。

当前执行原则：

- 只在真实高返工成本架构问题确有独立挑战价值时使用 bounded eval；
- 默认先运行 lower-cost capable model；
- 只有第一轮仍存在 unresolved ambiguity、冲突证据，或明确需要额外独立 second opinion 时才升级 Astra；
- 每个 scenario 独立 Fresh / ephemeral run，只复制显式 context paths；
- assertions / expected behavior / provenance / historical results 不进入 runtime workspace；
- process exit 0 不等于 PASS，必须人工语义评分；
- 模型 review 只构成 Review Evidence，不覆盖 Repository Authority / readiness-check；
- 不为了“完成 corpus”机械运行 AR-01 / AR-03，也不把 Astra 变成关键问题默认评审模型。

当前证据包含两个真实 paired scenario：

- AR-02：两模型均 `SUPPORTED_WITH_CHANGES`，Astra 有有限增量但无独占 blocking discovery；
- AR-04：两模型均独立 `DETECTED` 已知 migration sequencing flaw，隐藏 assertions 均 `8/8 PASS`；Astra 未发现 Sol/medium 漏掉的 blocking flaw。

因此当前 model-routing 候选策略为：

```text
bounded eval when justified
→ lower-cost capable review first
→ escalate only if unresolved / conflicting / deliberate second opinion
```

跨项目 Evidence 已提交 `dygapp/agentic-dev` Issue #71，由该仓库独立决定是否具备长期方法价值。完整 evidence lifecycle、paired results、usage 与 model observability limitation 见 `evals/README.md` 和 Issue #92。

## 已完成里程碑摘要

| 日期 | 里程碑 |
|---|---|
| 2026-08-24～2026-09-01 | Feature-wide、Review Environment、主站视觉、Admin / Public frontend architecture 当前阶段完成 |
| 2026-09-02～2026-09-04 | Party EU-26～EU-29 完成，181 篇 acceptedSnapshot 与历史资源进入 canonical authority |
| 2026-09-05 | EU-30 完成；Party current canonical Runtime Dataset = 183，4 carousel 与 upgrade compatibility 接受 |
| 2026-09-05 | EU-31～EU-35 完成 Database baseline、List/Admin governance、Rich Text safety / authoring |
| 2026-09-06 | EU-36 完成 Public source isolation；Issue #77 成为 E1～E3 前置四层边界入口 |
| 2026-09-06～2026-09-07 | EU-37～EU-42 完成 Site Package contract、stable structure、Runtime composition、Schema/bootstrap separation 与 stable assets |
| 2026-09-07 | Issue #92 / PR #93 完成 E1～E3 前置总体路线固化；AR-02 修订 Phase 2A；PR #94 完成 AR-04 blind paired eval 并将跨项目 Evidence 提交 agentic-dev Issue #71；Phase 0 收口 |
| 2026-09-07 | Phase 1 current audit / Authority Clarification 完成；EU-43 完成 Current Authority Semantic Reconciliation 并取得集成后 Current Evidence；Current Ready Unit 回到 NONE，Slice B 成为下一 Planning Gate |

详细执行、exact-head、Integration 与 Post-Integration Evidence 继续以对应 `docs/work/**`、Issue comments、PR 与 Actions 为准。

## 其他 Planning Candidates

- Issue #60：C1 Loading / Skeleton、C2 Mobile Layout Human Review、E1～E3 Main formal content；
- Issue #59：Browser Compatibility 等后置候选；
- Issue #57：Public Rendering Architecture 讨论候选。

上述候选不因 Issue #92 自动扩张或晋升。

## Fresh Context 恢复入口

默认按当前 Repository Authority 恢复，而不是从聊天提示词复制状态：

1. `AGENTS.md`
2. `README.md`
3. `docs/project/project-roadmap.md`
4. `docs/project/development-method.md`
5. `docs/project/pre-e1e3-convergence-plan.md`
6. `docs/project/documentation-authority-convergence.md`
7. 当前阶段直接相关 Requirement / Specification / Technical Plan / Execution Unit
8. GitHub Issue #92；涉及四层架构时同时读取 Issue #77；Issue #60 只用于 E1～E3 / C1/C2 等 downstream candidates
9. 当前 Branch / PR / Actions / Runtime Evidence

当前 Ready Execution Unit：**NONE**。

Phase 0 已完成；Phase 1 audit 与 Slice A / EU-43 已完成。下一实际步骤是从最新 `main` 重新恢复 Issue #92 与 Phase 1 Authority，对 **Slice B — Canonical Product Requirement Consolidation** 重新执行 `slice-work → readiness-check`。在形成新的 Ready Execution Unit 前不得进入 Slice B Execute、Phase 1B、Phase 2 或 Issue #60 / E1～E3。
