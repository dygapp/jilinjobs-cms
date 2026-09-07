# Repository Documentation Authority Convergence

## Status

- Parent Planning Authority: GitHub Issue #92 / `docs/project/pre-e1e3-convergence-plan.md`
- Phase: **Phase 1 — Repository Documentation Authority Convergence**
- Current stage: **SLICE A / EU-43 COMPLETED — SLICE B / EU-44 READY**
- Audit baseline: `main@f8f4083d831b5a1bfffe494b541c7015dbdd3fe0`
- Current Ready Execution Unit: **EU-44 — Canonical Product Authority Consolidation — READY / NOT STARTED**
- Next Execution Gate: **Fresh Context EU-44 Execute after base-drift revalidation**
- E1～E3: **BLOCKED / DOWNSTREAM**

本文只承担 Repository Documentation Authority 的 current audit、分类规则、语义收敛目标与 Phase 1 slice 边界。它不改变 CMS 产品需求，不改变 Issue #77 已接受的四层产品/技术边界，也不直接授权 Issue #60 / E1～E3 Execute。

## 1. Objective

Phase 1 的目标不是为了目录整洁机械搬文档，而是先让 Fresh Context 能唯一判断：

1. 哪些文档仍是 Current Authority；
2. 哪些文档仍含有效语义但已经与当前 Repository 事实部分冲突；
3. 哪些文档已被后继 Authority supersede；
4. 哪些文件只承担历史执行/验证证据；
5. `PARTIALLY_CURRENT` 中仍有效的语义在进入 archive 前应被吸收回哪个 canonical Authority。

在语义 Currentness 收敛前，不执行大规模目录迁移；物理 Information Architecture / Archive Migration 属后继 Phase 1B。

## 2. Classification Contract

- `CURRENT`：当前 Fresh Context 可以直接作为现行语义/技术/数据 Authority 消费。
- `PARTIALLY_CURRENT`：仍有有效语义，但存在已被当前 Repository 事实 supersede 的描述；必须先 reconciliation，不能直接 archive。
- `SUPERSEDED`：当前设计/规划已被更高或更新 Authority 替代；只保留追溯价值，不再参与 Current Authority 恢复。
- `HISTORICAL_EVIDENCE`：执行、评审、研究或验证记录；用于 traceability/evidence，不定义当前产品/架构事实。

分类是语义角色，不等于物理路径。Phase 1A 先修语义；Phase 1B 才负责目录和 Authority Map。

## 3. Current Audit Findings

以下表格记录 **audit baseline** 上的分类与 finding，用于解释 Slice A/B/C 的形成原因；EU-43 已修订的 Slice A finding 不应继续被 Fresh Context 当作当前未解决事实。当前 Gate 与完成状态以本文 Status、§5～§10、Roadmap 和对应 work artifact 为准。

### 3.1 Fresh Context / Planning Authority

| Path | Classification | Finding / action |
|---|---|---|
| `README.md` | `PARTIALLY_CURRENT` | 当前阶段与多数 Authority locator 正确，但同时承载大量历史阶段索引；语义可继续消费，结构性压缩延后 Phase 1B。 |
| `docs/project/project-roadmap.md` | `CURRENT` | 当前 Issue #92 顺序、Phase 0 结果与四层 boundary 正确；本次 Planning closure 后需要把下一 Gate 更新为 EU-43 Ready。 |
| `docs/project/development-method.md` | `CURRENT` | 当前 Consumer-local workflow / readiness / execute discipline。 |
| `docs/project/site-package-planning.md` | `CURRENT` | 已正确说明 EU-42 后不能直接进入旧 Slice D，并指向 Issue #92。 |
| `docs/project/pre-e1e3-convergence-plan.md` | `PARTIALLY_CURRENT` | Phase 顺序与 Phase 2A planning 仍有效；Architecture Review Experiment 段仍只描述 AR-02 并写“暂不反馈 agentic-dev”，已被 AR-04 与 agentic-dev Issue #71 当前事实 supersede。 |

其余 `docs/project/**` 方法/治理文档继续按各自当前职责消费；Phase 1 不重写 Consumer-local Method，也不触发 `agentic-dev` baseline upgrade。

### 3.2 Requirements

| Path | Classification | Finding / action |
|---|---|---|
| `docs/requirements/information-publishing.md` | `PARTIALLY_CURRENT` | 主需求仍是最高产品 Authority，但 V4.8 中仍有 EU-30 前 `HOME_CAROUSEL_INTERVAL_SECONDS`、Flyway 站点初始化、旧静态基线等描述；需吸收 EU-30 Amendment 与 EU-41/EU-42 ownership。 |
| `docs/requirements/information-publishing-eu30-amendment.md` | `PARTIALLY_CURRENT` | 内容仍是有效 confirmed Requirement Change，但作为单独 amendment 与 V4.8 并存；有效语义需折回主需求后再降为 superseded/history。 |
| `docs/requirements/cms-site-package-boundary.md` | `PARTIALLY_CURRENT` | 四层边界与 EU-41/EU-42 accepted facts 正确；`Current Follow-up Direction` 仍把旧 Slice D 作为 Issue #77 直接下一步，需对齐 Issue #92 Phase 1→2→3 路线。 |
| `docs/requirements/database-migration-baseline-convergence.md` | `CURRENT` | 已明确 EU-41 amendment、Generic Flyway schema-only 与 development DB recreation boundary。 |
| `docs/requirements/admin-guidance-governance.md` | `CURRENT` | 未发现与当前阶段冲突。 |
| `docs/requirements/list-definition-group-governance.md` | `CURRENT` | 未发现与当前阶段冲突。 |
| `docs/requirements/party-positioning.md` | `CURRENT` | 未发现与当前 Party positioning 冲突。 |
| `docs/requirements/public-frontend-replaceability.md` | `CURRENT` | EU-36 replaceability boundary 仍是当前 Authority。 |
| `docs/requirements/rich-text-authoring.md` | `CURRENT` | 当前 Rich Text Requirement。 |

### 3.3 Specifications

| Path | Classification | Finding / action |
|---|---|---|
| `docs/specifications/admin-frontend-convergence.md` | `SUPERSEDED` | README 已明确为历史 convergence 文档；当前 Admin Authority 已由后继 specs/requirements 承担。 |
| `docs/specifications/center-main-site-core.md` | `SUPERSEDED` | README 已明确为历史核心阶段文档。 |
| `docs/specifications/cms-core.md` | `PARTIALLY_CURRENT` | 核心对象语义仍有效，但仍描述旧 `HOME_CAROUSEL_INTERVAL_SECONDS` 与 `site-baseline` 资源 ownership，且未完全吸收 EU-30 list ARTICLE/source identity。 |
| `docs/specifications/cms-site-package-boundary.md` | `PARTIALLY_CURRENT` | 四层合同与 EU-41/EU-42 lifecycle 正确；Remaining Obligations 仍保留旧 Slice D 直接路线。 |
| `docs/specifications/party.md` | `PARTIALLY_CURRENT` | Party 产品/页面语义仍有效；稳定展示资源仍指向 `site-baseline/static/**`，与 EU-42 source owner 冲突。 |
| `docs/specifications/preset-site-structure.md` | `PARTIALLY_CURRENT` | `preset` 删除/稳定身份保护仍有效；Flyway V12 初始化、NavigationItem 无 stable code 等实现事实已被 EU-39～EU-41 supersede。 |
| `docs/specifications/database-migration-baseline-convergence.md` | `CURRENT` | 已对齐 EU-41 current baseline。 |
| `docs/specifications/admin-guidance-governance.md` | `CURRENT` | 当前。 |
| `docs/specifications/admin-site.md` | `CURRENT` | 当前。 |
| `docs/specifications/list-definition-group-governance.md` | `CURRENT` | 当前。 |
| `docs/specifications/public-frontend-replaceability.md` | `CURRENT` | 当前。 |
| `docs/specifications/public-shared-shell.md` | `CURRENT` | 当前。 |
| `docs/specifications/public-site.md` | `CURRENT` | 已吸收 EU-30 Carousel / List ARTICLE 当前行为；对 Site assets 只要求版本化公开资源，不建立并行 source owner。 |
| `docs/specifications/rich-text-authoring.md` | `CURRENT` | 当前。 |

### 3.4 Technical Authority

| Path | Classification | Finding / action |
|---|---|---|
| `docs/technical/admin-frontend-convergence.md` | `SUPERSEDED` | README 已明确为历史 convergence Technical Plan。 |
| `docs/technical/center-main-site-core.md` | `SUPERSEDED` | README 已明确为历史 core Technical Plan。 |
| `docs/technical/rich-text-authoring-research.md` | `HISTORICAL_EVIDENCE` | Research evidence；当前实现计划由 `rich-text-authoring-plan.md` 承担。 |
| `docs/technical/cms-architecture.md` | `PARTIALLY_CURRENT` | CMS 长期分层仍有效；Flyway wording 未体现 EU-41 controlled reset，静态资源仍写“工程基线”而未声明 `sites/jilinjobs/assets/**` Site owner。 |
| `docs/technical/backend-service.md` | `PARTIALLY_CURRENT` | 大量 V11 / `HOME_CAROUSEL_INTERVAL_SECONDS` / Flyway site seed 描述是已完成阶段实现，当前 Backend contract 已被 EU-30、EU-41、EU-42 扩展。 |
| `docs/technical/cms-site-package-boundary.md` | `PARTIALLY_CURRENT` | 当前 lifecycle 正确；Remaining after EU-42 仍指向旧 Slice D。 |
| `docs/technical/party-frontend.md` | `PARTIALLY_CURRENT` | Party implementation 仍当前，但 `site-baseline/static/**` source 与 Fresh Flyway assertions 已被 EU-41/EU-42 supersede。 |
| `docs/technical/preset-site-structure.md` | `PARTIALLY_CURRENT` | Backend preset protection 仍有效；V12/Flyway seed 技术方案已是历史实现。 |
| `docs/technical/verification-strategy.md` | `PARTIALLY_CURRENT` | 通用 Current Evidence discipline 当前；Completion/Review baseline 仍描述“Flyway + versioned static baseline”，需对齐 Generic Flyway + Site Package structure/bootstrap/assets composition。 |
| `docs/technical/admin-frontend-integration.md` | `CURRENT` | 当前。 |
| `docs/technical/admin-frontend.md` | `CURRENT` | 当前。 |
| `docs/technical/carousel-list-placement.md` | `CURRENT` | EU-30 current Carousel / CmsList placement technical contract。 |
| `docs/technical/configuration-governance.md` | `CURRENT` | 当前。 |
| `docs/technical/database-migration-baseline-convergence.md` | `CURRENT` | 已对齐 EU-41 current baseline。 |
| `docs/technical/public-frontend-replaceability.md` | `CURRENT` | 当前。 |
| `docs/technical/public-site-frontend.md` | `CURRENT` | 当前 Public implementation boundary。 |
| `docs/technical/rich-text-authoring-plan.md` | `CURRENT` | 当前。 |

### 3.5 Work Records

`docs/work/**` 在 audit baseline 没有正在执行的 EU；EU-30～EU-42 已完成，pre-numbered execution-unit documents 也只承担历史计划/追溯。因此下列文件统一分类为 `HISTORICAL_EVIDENCE`，Phase 1B 再决定物理 archive / history 结构：

- `admin-frontend-convergence-execution-units.md`
- `center-main-site-core-execution-units.md`
- `eu30-carousel-convergence.md`
- `eu30-migration-upgrade-verification.md`
- `eu30-post-incident-review.md`
- `eu31-database-migration-baseline-convergence.md`
- `eu32-list-definition-group-governance.md`
- `eu33-admin-guidance-governance.md`
- `eu34-rich-text-html-safety-foundation.md`
- `eu35-shared-rich-text-authoring.md`
- `eu36-public-frontend-source-isolation.md`
- `eu37-site-package-provisioner-foundation.md`
- `eu38-stable-site-structure-package-migration.md`
- `eu39-navigation-stable-identity.md`
- `eu40-explicit-site-package-runtime-composition.md`
- `eu41-site-bootstrap-generic-schema-baseline-separation.md`
- `eu42-site-asset-runtime-projection.md`
- `frontend-follow-up-execution-units.md`
- `party-column-route-currentness-execution-unit.md`
- `party-convergence-execution-units.md`
- `public-site-multi-entry-execution-units.md`

这些文件仍可用于验证 lineage / accepted evidence，但 Fresh Context 不应把其中的旧 Status / Next Step 当当前 Execute Authority。EU-43 在 audit 后形成并现已完成；其 work artifact 继续承担当前完成证据，物理 archive 角色留给 Phase 1B。EU-44 在 Slice B planning 中形成，当前承担下一 Ready Execute Authority。

### 3.6 Historical Migration Workspace

| Path | Classification | Finding / action |
|---|---|---|
| `data-migrations/README.md` | `PARTIALLY_CURRENT` | Canonical Dataset / provenance / idempotency 规则仍有效；边界仍写 Flyway 承担 preset 结构、`site-baseline/static/**` 承担稳定视觉资源，需对齐 EU-41/EU-42。 |
| `data-migrations/party/README.md` | `PARTIALLY_CURRENT` | Runtime composition 基本顺序仍有效；仍声明 V2 current preset compatibility responsibility，已被 EU-41 retirement supersede。 |
| `data-migrations/party/v1/**` | `CURRENT` | 当前 Party canonical migration data authority；“历史内容”是业务来源，不表示文件本身只是 Historical Evidence。 |
| `data-migrations/schemas/**` | `CURRENT` | Canonical dataset schema authority。 |
| `data-migrations/scripts/**` / `tools/**` / `package.json` | `CURRENT` | 当前 collection/validation/promotion workspace capability；Phase 2 才重新划分 application boundary。 |

### 3.7 Subtree README Gap

当前 `backend/`、`frontend/`、`sites/` / `sites/jilinjobs/` 没有长期 subtree README；这不是 Phase 1A semantic defect。是否新增以及内容边界由 Phase 1B Information Architecture 统一决定，避免在当前 audit 中提前形成第二套 Authority Map。

## 4. Reconciliation Dependencies

Phase 1A 的 `PARTIALLY_CURRENT` 不适合一次性“大 PR”处理。按 dependency / rollback / review 边界切分：

### Slice A — Four-layer / lifecycle Currentness — COMPLETED

优先修复会直接误导 Issue #92/#77 Fresh Context 与后续 Phase 2 planning 的当前性冲突：

- `docs/project/pre-e1e3-convergence-plan.md`
- `docs/requirements/cms-site-package-boundary.md`
- `docs/specifications/cms-site-package-boundary.md`
- `docs/specifications/preset-site-structure.md`
- `docs/specifications/party.md`
- `docs/technical/cms-architecture.md`
- `docs/technical/cms-site-package-boundary.md`
- `docs/technical/preset-site-structure.md`
- `docs/technical/party-frontend.md`
- `docs/technical/verification-strategy.md`
- `data-migrations/README.md`
- `data-migrations/party/README.md`

此 Slice 只修 Current semantics / status / ownership references，不移动目录、不改代码、不改产品行为。EU-43 已完成该 Slice 并取得 exact-head、Integration 与 Post-Integration Current Evidence。

### Slice B — Canonical Product Requirement Consolidation — EU-44 READY / NOT STARTED

在 Slice A 后处理：

- `docs/requirements/information-publishing.md`
- `docs/requirements/information-publishing-eu30-amendment.md`
- `docs/specifications/cms-core.md`
- `docs/technical/backend-service.md`

目标是把 EU-30 confirmed Amendment 与 EU-41/EU-42 ownership 折回 canonical product/CMS Authority，并把单独 amendment 降为可归档状态。

本 Slice 已重新执行 dependency closure、`slice-work → readiness-check`，形成 **EU-44 — Canonical Product Authority Consolidation — READY / NOT STARTED**。精确 scope / acceptance / verification / non-goals 见 `docs/work/eu44-canonical-product-authority-consolidation.md`。EU-44 不继承 EU-43 已终止的 Execute Authority，而是基于当前 Repository state 独立通过 Readiness。

### Slice C — Information Architecture / Archive Migration

只有 Slice A/B 语义 currentness 完成后，才执行 Phase 1B：Root README compact entry、`docs/README.md` Authority Map、current/history/archive 物理结构、链接修复与 subtree README。

## 5. Slice-work Result

Phase 1A 当前 slice result：

1. **EU-43 — Current Authority Semantic Reconciliation — COMPLETED**；
2. **EU-44 — Canonical Product Authority Consolidation — READY / NOT STARTED**。

EU-44 作为单一纵向 Unit 而不继续拆成 Requirement / Specification / Technical 子 PR，原因是四份 Authority 必须原子一致地切换到同一 canonical state；继续拆分会制造“主需求已折叠但 CMS/Backend 仍旧”或相反的临时 Current Authority 冲突。

Current Ready Execution Unit：**EU-44**。Phase 1B / Slice C 仍是 Planning Candidate，不获得 EU-44 Execute Authority。

## 6. Readiness Check — EU-43

### Inputs

- Product / architecture intent：Issue #92 + Issue #77 + current Roadmap；
- Accepted implementation facts：EU-39～EU-42 current `main` state and current Site Package files；
- Current migration baseline：Generic `V1__current_cms_schema.sql` + `V2__site_provisioning_schema_capabilities.sql`；
- Current Site ownership：`sites/jilinjobs/{structure,bootstrap,assets}/**`；
- Historical migration authority：`data-migrations/**`；
- Current review experiment facts：AR-02 + AR-04 + agentic-dev Issue #71 evidence routing record。

### Decision

**PASS — EU-43 was Ready. Execution result: COMPLETED.**

理由：

1. objective / exact file scope / non-goals 已明确；
2. 不需要新的产品、业务或用户可见行为决策；
3. 不改变 code/runtime/data，属于可回滚 documentation-only reconciliation；
4. Current Repository facts 已足以判断正确语义，不依赖外部 baseline upgrade；
5. acceptance 可通过 diff-scope、Current locator review、stale-reference scan 与 Repository CI 证明；
6. 没有 Repository Authority 定义的 human escalation condition。

Readiness Decision 只记录当时 Execute Authority 的成立条件；EU-43 完成后该 Execute Authority 已终止，不能被后继 Slice 复用。

## 7. EU-43 Acceptance

EU-43 必须同时满足：

1. 上述 Slice A 文档不再把旧 `V2__current_preset_data.sql` 作为 current responsibility；
2. 不再把 `site-baseline/static/**` 描述为稳定 JilinJobs Site assets 的当前 source owner；
3. `preset` 当前保护语义保留，并以 Site Package stable identity / Generic CMS capability 表达，不把历史 V12 seed 当 Current lifecycle；
4. Party stable assets 指向 `sites/jilinjobs/assets/**`，公开 `/static/**` URL 不变；
5. Verification Strategy 表达 Generic Flyway → Site stable provision → optional one-time bootstrap → asset projection / migration / runtime 的当前组合，不让测试自建站点基线；
6. Issue #77 follow-up 不再绕过 Issue #92 Phase 1/2/3 直接进入旧 Slice D；
7. `pre-e1e3-convergence-plan.md` 的 experiment status 与 AR-04 / agentic-dev Issue #71 当前事实一致；
8. `data-migrations/**` 继续保持 canonical provenance / fingerprint / idempotency 语义，不被错误并入 Site Package；
9. 不修改 CMS code、Flyway SQL、Site Package bytes、Canonical Dataset 或 Public/Admin behavior；
10. 最终 diff 只包含该 Slice 的 Current Authority reconciliation 与必要 Roadmap/Issue evidence 回写。

上述 Acceptance 已由 EU-43 work artifact 记录的 exact-head、review、Integration 与 post-integration evidence 闭环。

## 8. Verification Strategy

EU-43 为 documentation-only unit，最低 Current Evidence：

- exact-head diff / changed-file review；
- 对 Slice A 文档执行旧责任语义扫描与人工语义复核；
- 验证 current links / referenced paths 实际存在；
- Repository CI exact-head PASS；
- merge 后重新读取 `main` 与 post-integration CI，确认 Current Authority locator 无 base drift。

不需要 Human Runtime Review、Browser Review 或新的 architecture model eval；这些不能为纯文档 Currentness 提供额外有效产品证据。Repository 自动触发的现有 Runtime workflows 可以作为支持性 evidence，但不改变该最低验证合同。

## 9. Readiness Check — EU-44

### Inputs

- Parent Planning Authority：Issue #92 / 本文；
- confirmed Requirement Change：`docs/requirements/information-publishing-eu30-amendment.md`；
- accepted EU-30 technical / public contract：`docs/technical/carousel-list-placement.md`、`docs/specifications/public-site.md`；
- accepted Site / Schema boundary：`docs/requirements/cms-site-package-boundary.md` 与 EU-41 / EU-42 Current Repository facts；
- exact target files、non-goals、acceptance 与 verification：`docs/work/eu44-canonical-product-authority-consolidation.md`。

### Decision

**PASS — EU-44 READY / NOT STARTED.**

理由：

1. Slice B objective 与 exact 4-file Authority scope 已明确；
2. 所需产品语义均已由 EU-30 confirmed Amendment / accepted Human Review 决定，不需要新的 Product Decision；
3. EU-41 / EU-42 ownership 已是 accepted Current Repository fact，不需要新的 Architecture Decision；
4. 四份 Authority 原子一致收敛比继续拆分更安全，且 rollback/review boundary 清晰；
5. Unit 为 documentation-only，不改变 code/runtime/data/Canonical Dataset；
6. Acceptance 与 Verification 可独立检查；
7. 没有 Repository Authority 定义的 Human Escalation condition。

## 10. Next Gate

当前 Ready Execution Unit：**EU-44 — Canonical Product Authority Consolidation — READY / NOT STARTED**。

下一实际 Gate：

1. 在新的 Fresh Context 重新核验最新 `main`、Issue #92、本文件、EU-44 work artifact、Open PR / Actions 与 base drift；
2. 若 Readiness 仍有效且无冲突，则 Execute EU-44；
3. EU-44 完成前不得进入 Phase 1B Information Architecture / Archive Migration；
4. Phase 1 全部收口后才进入 Phase 2 Generic Historical Migration & Backend Application Boundary；
5. Phase 3 compatibility Gate 前不得进入 Issue #60 / E1～E3。

EU-44 Execute Authority 只覆盖 `docs/work/eu44-canonical-product-authority-consolidation.md` 定义的 canonical Product / CMS / Backend Authority consolidation，不延伸到后继 Phase。