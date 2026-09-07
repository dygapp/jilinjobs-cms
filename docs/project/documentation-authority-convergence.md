# Repository Documentation Authority Convergence

## Status

- Parent Planning Authority: GitHub Issue #92 / `docs/project/pre-e1e3-convergence-plan.md`
- Phase: **Phase 1 — Repository Documentation Authority Convergence — COMPLETED**
- Audit baseline: `main@f8f4083d831b5a1bfffe494b541c7015dbdd3fe0`
- Semantic convergence: **EU-43 / EU-44 COMPLETED**
- Physical Information Architecture: **EU-45 COMPLETED**
- Current Ready Execution Unit: **NONE**
- Next Planning Gate: **Phase 2 — Generic Historical Migration & Backend Application Boundary / Planning Candidate**
- E1～E3: **BLOCKED / DOWNSTREAM**

本文保留 Phase 1 的 Repository Documentation Authority 分类 contract、audit lineage 与最终物理 Information Architecture 结果。它不改变 CMS 产品需求，不改变 Issue #77 已接受的四层产品/技术边界，也不授权 Phase 2 或 Issue #60 / E1～E3 Execute。

## 1. Objective 与完成结果

Phase 1 的目标是让 Fresh Context 可以唯一判断：

1. 哪些文档仍是 Current Authority；
2. 哪些文档仍含有效语义但与当前 Repository 事实存在历史描述，需要作为 `PARTIALLY_CURRENT` 继续消费；
3. 哪些文档已被后继 Authority supersede；
4. 哪些文件只承担历史执行 / 研究 / Verification evidence；
5. Current 与 archive 的物理路径如何反映上述语义角色。

完成顺序：

```text
Phase 1A semantic currentness
  EU-43 Current Authority Semantic Reconciliation
  EU-44 Canonical Product Authority Consolidation
        ↓
Phase 1B physical IA
  EU-45 Documentation Information Architecture & Archive Migration
```

EU-43 / EU-44 先关闭语义 currentness；EU-45 再把已明确 `SUPERSEDED / HISTORICAL_EVIDENCE` 的文件移入 archive、建立 `docs/README.md` Authority Map、Work lifecycle 与 subtree ownership README，并修复 Current locators。三个 Unit 均完成后 Phase 1 关闭。

## 2. Classification Contract

- `CURRENT`：Fresh Context 可以直接作为现行语义 / 技术 / 数据 Authority 消费。
- `PARTIALLY_CURRENT`：仍有有效语义，但包含已被当前 Repository 事实 supersede 的历史描述；不能仅因“partial”机械 archive。
- `SUPERSEDED`：当前设计 / 规划已被更高或更新 Authority 替代；只保留 traceability，不参与 Current Authority 恢复。
- `HISTORICAL_EVIDENCE`：执行、评审、研究或验证记录；用于 traceability / evidence，不定义当前产品 / 架构事实。

分类是语义角色；Phase 1B 只把 Phase 1 已明确为 `SUPERSEDED / HISTORICAL_EVIDENCE` 的文件物理迁入 archive。`data-migrations/**` 的“Historical Migration”是业务数据来源角色，不等于 Documentation `HISTORICAL_EVIDENCE`。

## 3. Final Documentation Information Architecture

Repository-level Documentation Authority Map：`docs/README.md`。

Fresh Context 默认顺序：

1. `AGENTS.md`；
2. Root `README.md`；
3. `docs/README.md`；
4. `docs/project/project-roadmap.md` 与 `docs/project/development-method.md`；
5. 当前任务直接相关的 Requirement / Specification / Technical / Architecture Authority；
6. 当前 controlling Issue、PR / Actions、Ready Execution Unit 与 Current Evidence。

任何 `docs/**/archive/**` 与 `docs/work/archive/**` 默认排除在 Current Authority 恢复之外，除非当前 Authority 明确要求历史审计 / traceability。

### 3.1 Requirements

| Path | Final role |
|---|---|
| `docs/requirements/information-publishing.md` | `CURRENT` — consolidated V4.9 canonical Requirement |
| `docs/requirements/archive/information-publishing-eu30-amendment.md` | `SUPERSEDED / TRACEABILITY` — EU-30 accepted Requirement Change lineage |
| `docs/requirements/cms-site-package-boundary.md` | `PARTIALLY_CURRENT` — 四层边界与 accepted lifecycle 继续有效 |
| `docs/requirements/database-migration-baseline-convergence.md` | `CURRENT` |
| `docs/requirements/admin-guidance-governance.md` | `CURRENT` |
| `docs/requirements/list-definition-group-governance.md` | `CURRENT` |
| `docs/requirements/party-positioning.md` | `CURRENT` |
| `docs/requirements/public-frontend-replaceability.md` | `CURRENT` |
| `docs/requirements/rich-text-authoring.md` | `CURRENT` |

### 3.2 Specifications

| Path | Final role |
|---|---|
| `docs/specifications/archive/admin-frontend-convergence.md` | `SUPERSEDED` |
| `docs/specifications/archive/center-main-site-core.md` | `SUPERSEDED` |
| `docs/specifications/cms-core.md` | `CURRENT` |
| `docs/specifications/cms-site-package-boundary.md` | `PARTIALLY_CURRENT` |
| `docs/specifications/party.md` | `PARTIALLY_CURRENT` |
| `docs/specifications/preset-site-structure.md` | `PARTIALLY_CURRENT` |
| `docs/specifications/database-migration-baseline-convergence.md` | `CURRENT` |
| `docs/specifications/admin-guidance-governance.md` | `CURRENT` |
| `docs/specifications/admin-site.md` | `CURRENT` |
| `docs/specifications/list-definition-group-governance.md` | `CURRENT` |
| `docs/specifications/public-frontend-replaceability.md` | `CURRENT` |
| `docs/specifications/public-shared-shell.md` | `CURRENT` |
| `docs/specifications/public-site.md` | `CURRENT` |
| `docs/specifications/rich-text-authoring.md` | `CURRENT` |

### 3.3 Technical Authority

| Path | Final role |
|---|---|
| `docs/technical/archive/admin-frontend-convergence.md` | `SUPERSEDED` |
| `docs/technical/archive/center-main-site-core.md` | `SUPERSEDED` |
| `docs/technical/archive/rich-text-authoring-research.md` | `HISTORICAL_EVIDENCE` |
| `docs/technical/cms-architecture.md` | `PARTIALLY_CURRENT` |
| `docs/technical/backend-service.md` | `CURRENT` |
| `docs/technical/cms-site-package-boundary.md` | `PARTIALLY_CURRENT` |
| `docs/technical/party-frontend.md` | `PARTIALLY_CURRENT` |
| `docs/technical/preset-site-structure.md` | `PARTIALLY_CURRENT` |
| `docs/technical/verification-strategy.md` | `PARTIALLY_CURRENT` |
| `docs/technical/admin-frontend-integration.md` | `CURRENT` |
| `docs/technical/admin-frontend.md` | `CURRENT` |
| `docs/technical/carousel-list-placement.md` | `CURRENT` |
| `docs/technical/configuration-governance.md` | `CURRENT` |
| `docs/technical/database-migration-baseline-convergence.md` | `CURRENT` |
| `docs/technical/public-frontend-replaceability.md` | `CURRENT` |
| `docs/technical/public-site-frontend.md` | `CURRENT` |
| `docs/technical/rich-text-authoring-plan.md` | `CURRENT`；其 research / completed execution-unit references 已指向 archive |

### 3.4 Work Records

`docs/work/` 采用 lifecycle split：

- `docs/work/current/`：只允许当前已经 Readiness PASS、仍处于 Execute / Verification / Integration / Post-Integration closure 的 active work artifact；
- `docs/work/archive/`：已完成 Execution Unit、历史预编号计划、incident / verification record 与其他 `HISTORICAL_EVIDENCE`；
- `docs/work/` 根级除 README 外不放 work artifact。

Phase 1B 后没有 Ready / active Execution Unit，因此 `docs/work/current/` 为空（仅 README）。以下 audit / completion records 已进入 `docs/work/archive/`：

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
- `eu43-current-authority-semantic-reconciliation.md`
- `eu44-canonical-product-authority-consolidation.md`
- `eu45-documentation-information-architecture.md`
- `frontend-follow-up-execution-units.md`
- `party-column-route-currentness-execution-unit.md`
- `party-convergence-execution-units.md`
- `public-site-multi-entry-execution-units.md`

这些文件可用于 lineage / accepted evidence 审计，但其中的旧 Status / Next Step / Execute wording 不具有 Current Execute Authority。

### 3.5 Historical Migration Workspace

| Path | Final role |
|---|---|
| `data-migrations/README.md` | `PARTIALLY_CURRENT` — canonical dataset / provenance / idempotency 与当前边界继续有效 |
| `data-migrations/party/README.md` | `PARTIALLY_CURRENT` |
| `data-migrations/party/v1/**` | `CURRENT` canonical migration data authority |
| `data-migrations/schemas/**` | `CURRENT` canonical dataset schema authority |
| `data-migrations/scripts/**` / `tools/**` / `package.json` | `CURRENT` collection / validation / promotion workspace capability |

EU-45 没有移动、归档或修改 `data-migrations/**`。

### 3.6 Supporting method / Architecture / subtree entry

- `docs/project/agentic-dev-continuous-execution-mode.md`：Current supporting Consumer-local method note；不替代 `AGENTS.md`、`docs/project/development-method.md` 或 Validation Baseline locator；
- `docs/architecture/decisions/**`：继续作为 ADR 区域；
- `backend/README.md`、`frontend/README.md`、`sites/README.md`、`sites/jilinjobs/README.md`：只解释 subtree ownership / locator，不建立第二套 global Authority / Roadmap。

## 4. Reconciliation Results

### EU-43 — Current Authority Semantic Reconciliation — COMPLETED

关闭会误导 Issue #92 / #77 与 Phase 2 planning 的 Currentness drift，包括：旧 Generic Flyway Site data responsibility、旧 stable asset source ownership、historical migration number 被描述为 current lifecycle、Issue #77 旧 direct-next-step wording，以及 architecture-review experiment currentness。

EU-43 只修 Current semantics / status / ownership references，不移动目录、不改 code/runtime/data。

### EU-44 — Canonical Product Authority Consolidation — COMPLETED

将 EU-30 confirmed Product Change 与 EU-41 / EU-42 ownership 折回 canonical Product / CMS / Backend Authority：

- `docs/requirements/information-publishing.md` → consolidated V4.9；
- EU-30 amendment → `SUPERSEDED / TRACEABILITY`；
- `docs/specifications/cms-core.md` 与 `docs/technical/backend-service.md` 对齐 current contracts。

EU-44 完成后独立 amendment 才具备安全归档条件。

### EU-45 — Documentation Information Architecture & Archive Migration — COMPLETED

EU-45 在 `main@054be2a46f669bab4d0a34a5dc668550cddad648` 完成 Fresh Context Execute baseline revalidation后执行，范围严格为 documentation-only IA / archive / link repair / gate synchronization：

- Root README 稳定入口；
- `docs/README.md` Authority Map；
- Requirements / Specifications / Technical archive manifest；
- Work current / archive lifecycle；
- supporting method note relocation；
- subtree ownership README；
- Current path / locator repair；
- `AGENTS.md` / Roadmap / Issue #92 / 本文件的 Phase 1 closure sync。

Integration 与 Post-Integration Current Evidence 以 GitHub Issue #92 最新 completion record、对应 PR / Actions 与 `main` 为准；这些 GitHub-native瞬时证据不在本文递归维护精确 run / merge ledger。

## 5. Verification Contract

Phase 1 documentation-only units 的最低 Current Evidence：

- exact-head diff / changed-file review；
- classification-to-archive manifest review；
- old moved-path scan 与 Current Markdown path/link existence review；
- `docs/work/*.md` root leakage review；
- Authority Map / category README / subtree README semantic review；
- explicit no `data-migrations/**` / runtime / product / schema / API behavior change；
- Repository CI exact-head PASS；
- unresolved PR review threads = 0；
- merge 后重新读取 `main` 与 Post-Integration CI，确认 Current Authority locator 无 lifecycle drift。

Browser / Visual / Human Runtime Review 与新的 Architecture Model Eval 不属于 EU-45 documentation-only claim 的最低证明；如果执行意外改变 runtime / product / architecture scope，则应使原 Readiness 失效并重新规划，而不是静默扩大 Verification。

## 6. Phase Closure / Next Gate

Phase 1：**COMPLETED**。

Current Ready Execution Unit：**NONE**。

下一实际 Gate：

1. 新的 Planning Context 重新核验最新 `main`、Open PR / Actions、Issue #92 / #77、本 Roadmap 与 Current Authority；
2. 从 **Phase 2 — Generic Historical Migration & Backend Application Boundary** 的 Planning Candidate 开始 dependency closure / Specification / 必要 Technical Planning；
3. 只有新的 `slice-work` 形成 Candidate Execution Unit 且 `readiness-check` PASS 后才允许 Phase 2 Execute；
4. Phase 3 compatibility / re-entry Gate 前不得进入 Issue #60 / E1～E3；
5. EU-43 / EU-44 / EU-45 Execute Authority 均已终止，不延伸到后继阶段。