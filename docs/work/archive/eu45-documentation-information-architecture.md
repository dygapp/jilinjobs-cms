# EU-45 — Documentation Information Architecture & Archive Migration

## Status

- Parent: GitHub Issue #92
- Related architecture context: GitHub Issue #77
- Phase: Phase 1B — Documentation Information Architecture & Archive Migration
- Candidate formed by: `slice-work`
- Readiness: **PASS**
- Execute state: **IMPLEMENTED / VERIFICATION IN PROGRESS**
- Planning baseline: `main@9435fc4d2a34f83ec683457d32f326b954b6db76`
- Readiness integration: `main@054be2a46f669bab4d0a34a5dc668550cddad648`
- Execute baseline: `main@054be2a46f669bab4d0a34a5dc668550cddad648`
- Implementation branch: `docs/eu45-documentation-information-architecture`
- Integration / Post-Integration Evidence: **PENDING**
- Phase 1 authority: `docs/project/documentation-authority-convergence.md`

EU-45 的 Execute Authority 来自 readiness change 集成后的 Fresh Context revalidation，不继承 EU-44。Execute 前已重新确认 `main@054be2a46f669bab4d0a34a5dc668550cddad648`、Open PR = 0、Issue #92 / #77、Readiness 与最近相关 Actions，没有发现 base drift、冲突并行 PR 或新的未分类 work artifact。

## Dependency Closure

Phase 1B 的物理 Information Architecture 依赖 Phase 1A 先关闭语义 currentness。执行前依赖已经闭合：

1. EU-43 已完成 Four-layer / lifecycle Current Authority Semantic Reconciliation；
2. EU-44 已完成 Canonical Product Authority Consolidation，`information-publishing.md` 已成为 consolidated V4.9，EU-30 amendment 已降为 `SUPERSEDED / TRACEABILITY`；
3. EU-43 / EU-44 均已完成 Integration 与 Post-Integration Current Evidence，并终止 Execute Authority；
4. Issue #77 四层边界保持不变，`data-migrations/**` 仍是 Current Historical Migration data authority，不属于本 Unit 的 docs archive；
5. Consumer-local Method 足够完成本 Unit，不需要 `agentic-dev` baseline upgrade。

### Root-level documentation closure

`docs/agentic-dev-continuous-execution-mode.md` 被确认继续承担 **CURRENT SUPPORTING METHOD NOTE**，不是 Product Authority、独立 Validation Baseline Authority 或 Historical Work Evidence。EU-45 将其移动到：

```text
docs/project/agentic-dev-continuous-execution-mode.md
```

其 supporting role 由 `docs/README.md` 标明；当前方法 Authority 仍是 `AGENTS.md` 与 `docs/project/development-method.md`。

`docs/architecture/decisions/**` 保持 Architecture Decision Record 区域，不机械重排。

### Technical Planning decision

**No additional persistent Technical Plan was required.** 目标 IA、archive exclusion、Authority Map、Root README responsibility、subtree README gap 与 Current/Historical 分类已经由 Phase 1 Planning Authority 固化；本 Unit 不引入 runtime architecture、schema、API、build 或跨 EU 技术协调。

## Ready Specification

### Objective

把 Phase 1A 已完成的语义分类落实为可长期恢复的物理 Documentation Information Architecture，使 Fresh Context 默认从 Current Authority 恢复项目，而不需要从同级目录中区分 Current、Superseded 与 Historical Work 状态。

### In Scope

1. 根 `README.md` 收敛为稳定项目入口，保留目标、Scope、Authority / Roadmap / Method locator 与最小运行/验证入口，不再承担详细 EU / PR / Action ledger。
2. 新增 `docs/README.md` 作为唯一 Repository-level Documentation Authority Map，并明确 `archive/**` 默认不参与 Fresh Context Current Authority 恢复。
3. Requirements / Specifications / Technical 建立 current-vs-archive 边界，只归档 Phase 1A 已明确为 `SUPERSEDED / HISTORICAL_EVIDENCE` 的文件：
   - `docs/requirements/archive/information-publishing-eu30-amendment.md`；
   - `docs/specifications/archive/admin-frontend-convergence.md`；
   - `docs/specifications/archive/center-main-site-core.md`；
   - `docs/technical/archive/admin-frontend-convergence.md`；
   - `docs/technical/archive/center-main-site-core.md`；
   - `docs/technical/archive/rich-text-authoring-research.md`。
4. `docs/work/` 建立 `current/` / `archive/` lifecycle；planning baseline 上的历史 work records 与已完成 EU-43 / EU-44 / EU-45 均进入 `docs/work/archive/`。
5. supporting method note 移入 `docs/project/`。
6. 为 `backend/`、`frontend/`、`sites/`、`sites/jilinjobs/` 增加最小 ownership README，不复制全局 Roadmap 或 Authority Map。
7. 修复 Current Authority / project docs / stable entry 中指向被移动文档的 locator；archive 内明确历史上下文可保留当时路径。
8. 同步 `AGENTS.md`、Root README、Roadmap、Issue #92 与 Phase 1 Authority，使 Phase 1 closure 后 Ready EU 返回 `NONE`，下一 Gate 仅为 Phase 2 Planning Candidate。

### Non-goals

- 不修改 Product Goal、Scope、User-visible Behavior、CMS contract、API、schema、Flyway、runtime、frontend、Site Package bytes、Canonical Dataset 或 migration behavior；
- 不重新做 EU-43 / EU-44 semantic reconciliation；
- 不把仍承担 Current semantics 的 `PARTIALLY_CURRENT` 文档机械归档；
- 不移动、归档或重写 `data-migrations/**`；
- 不进入 Phase 2 Backend Application / Core Boundary、Generic Content Migration Application 或 Party Migration De-specialization；
- 不进入 Issue #60 / E1～E3；
- 不进行 Repository split；
- 不更新 `agentic-dev` baseline；
- 不借 README / link repair 引入新的产品、架构或方法规则。

## Slice-work Result

Phase 1B 形成单一 Candidate Execution Unit：**EU-45 — Documentation Information Architecture & Archive Migration**。

未拆成 README、archive move、work move、subtree README 或 link-repair 子 EU，因为 Current/archive 目录切换与引用修复必须原子一致；Root README、`docs/README.md`、category README 与 subtree README 共同定义同一 IA，拆开会形成可合并但不可稳定恢复的中间状态。

Rollback boundary 是整个 documentation-only PR；不涉及 runtime/data side effect。

## Acceptance

1. 根 `README.md` 是稳定入口，不再承担详细 EU / PR / Actions 历史 ledger。
2. `docs/README.md` 成为唯一 Repository-level Documentation Authority Map，并明确 `archive/**` 的 Fresh Context exclusion。
3. Requirements / Specifications / Technical 根级不再混放已明确 `SUPERSEDED / HISTORICAL_EVIDENCE` 文件；archive manifest 与 Phase 1A classification 一致。
4. `PARTIALLY_CURRENT` 且仍承担 Current semantics 的文档保留在 Current 根级。
5. `docs/work/` 有清晰 current / archive lifecycle；Phase 1B 完成后没有已完成 EU 冒充 Current Execute Authority。
6. supporting method note 不再作为未分类 docs-root 文件。
7. `backend/`、`frontend/`、`sites/`、`sites/jilinjobs/` README 只承担 subtree responsibility / locator。
8. 所有 Current links / locators 指向存在路径；没有 Current Authority 继续把被移动文件旧路径当作当前 locator。
9. `data-migrations/**`、product/runtime/code/schema/API behavior 不发生变化。
10. 最终 changed-file set 只包含本 Unit 的 docs IA、README、link repair 与必要 Current Gate synchronization。
11. Exact-head Repository CI PASS，PR unresolved review threads = 0；合并后 `main` Post-Integration CI PASS，并重新读取 Fresh Context locators 确认 Phase 1 closure 无 lifecycle drift。

## Execute Record

### Implemented structure

Execute 已在独立分支 `docs/eu45-documentation-information-architecture` 实施：

- Root README stable-entry reconstruction；
- `docs/README.md` Authority Map；
- `docs/{requirements,specifications,technical}/README.md` 与对应 `archive/README.md`；
- Phase 1A manifest 指定的 Requirement / Specification / Technical archive move；
- `docs/work/{README.md,current/README.md,archive/README.md}` 与历史 work-record archive move；
- EU-45 自身最终归入 `docs/work/archive/`；
- supporting method note relocation；
- `backend/README.md`、`frontend/README.md`、`sites/README.md`、`sites/jilinjobs/README.md`；
- Current planning / architecture gate synchronization；
- moved-path locator repair，包括 canonical Requirement 的 EU-30 amendment archive locator 与 rich-text plan research archive locator。

### Execute baseline / drift evidence

- Execute baseline：`main@054be2a46f669bab4d0a34a5dc668550cddad648`；
- Execute 开始前与继续执行时均重新读取 `main`，未发生 base drift；
- Open PR = 0；
- 未发现新的未分类 `docs/work/**`；
- Issue #77 没有授予额外 runtime / Phase 2 Execute Authority；
- 没有执行 `agentic-dev` baseline upgrade。

### Static verification contract

最终 PR 前必须完成：

- exact changed-file compare against Execute baseline；
- archive manifest 与 Phase 1A classification 对照；
- old moved-path Current locator 定点扫描；
- `docs/work/*.md` root leakage 检查；
- Current Authority Map / category README / subtree README semantic review；
- explicit no-runtime/code/data change review；
- Repository CI 提供 docs / whitespace / repository checks。

Historical text inside `archive/**` 可以保留当时 Status / path 作为明确 evidence；不得由 Current Authority 将其消费为现行责任。

## Repository Evidence

- Implementation PR: **PENDING**
- Final implementation Head: **PENDING**
- Exact-head CI: **PENDING**
- Unresolved review threads: **PENDING**
- Integration: **PENDING**
- Post-Integration CI: **PENDING**
- Issue #92 Post-Integration Current Evidence: **PENDING**

EU-45 不需要 Browser / Visual / Human Runtime Review 或 Architecture Model Eval；若执行意外改变 runtime/product/architecture scope，则当前 Readiness 失效并必须回到 Planning，而不是扩大本 Unit。

## Readiness Decision

**PASS — EU-45 became a Ready Execution Unit after the readiness change integrated into `main`, and Fresh Context base-drift revalidation confirmed the Execute Authority remained valid.**

该 Execute Authority 只覆盖本文件定义的 Documentation IA / archive / link-repair / Current Gate synchronization。Integration 与 Post-Integration Current Evidence 完成后，EU-45 Execute Authority 终止，Phase 1 关闭，Current Ready Execution Unit 返回 `NONE`；Phase 2 只能作为新的 Planning Candidate 重新开始状态链。
