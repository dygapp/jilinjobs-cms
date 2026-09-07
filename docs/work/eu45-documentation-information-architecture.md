# EU-45 — Documentation Information Architecture & Archive Migration

## Status

- Parent: GitHub Issue #92
- Related architecture context: GitHub Issue #77
- Phase: Phase 1B — Documentation Information Architecture & Archive Migration
- Candidate formed by: `slice-work`
- Readiness: **PASS**
- Execute state: **NOT STARTED**
- Planning baseline: `main@9435fc4d2a34f83ec683457d32f326b954b6db76`
- Readiness integration: **PENDING — this planning/readiness change must integrate before Execute**
- Phase 1 authority: `docs/project/documentation-authority-convergence.md`

EU-45 只在本 planning/readiness change 集成后成为当前 Ready Execution Unit。任何 Execute Context 都必须重新读取最新 `main` / Issue #92 / 本文件 / Open PR / Actions，并确认没有 base drift 或 Authority change；EU-44 Execute Authority 不继承到本 Unit。

## Dependency Closure

### Upstream semantic dependencies

Phase 1B 的物理 Information Architecture 依赖 Phase 1A 先关闭语义 currentness，否则 archive 会把仍承担 Current responsibility 的内容机械移走。当前依赖已经闭合：

1. EU-43 已完成 Four-layer / lifecycle Current Authority Semantic Reconciliation；
2. EU-44 已完成 Canonical Product Authority Consolidation，`information-publishing.md` 已成为 consolidated V4.9，EU-30 amendment 已降为 `SUPERSEDED / TRACEABILITY`；
3. EU-43 / EU-44 均已完成 Integration 与 Post-Integration Current Evidence，并终止 Execute Authority；
4. Issue #77 当前四层边界保持不变，Historical Migration workspace `data-migrations/**` 仍是 Current data authority，不属于本 Unit 的 docs archive；
5. 当前 `main@9435fc4d2a34f83ec683457d32f326b954b6db76` 没有 Open PR；main CI #818 PASS；
6. 当前 Consumer-local Method 已足够完成本 Unit，不需要 `agentic-dev` baseline upgrade，也没有发现需要向 `agentic-dev` 提交的新 Method / Contract Evidence。

因此 Phase 1B 不再有未关闭的 product / architecture dependency。

### Root-level documentation closure

`docs/agentic-dev-continuous-execution-mode.md` 没有进入 Phase 1A 的 Requirements / Specifications / Technical / Work 分类表，但 dependency closure 需要显式决定其角色，避免 Phase 1B 迁移时留下未分类根级文档。

Decision：该文件继续承担 **CURRENT SUPPORTING METHOD NOTE**，不是 Product Authority、不是独立 Validation Baseline Authority、也不是 Historical Work Evidence。其连续执行规则已经被当前 `AGENTS.md` 与 `docs/project/development-method.md` 消费；文件中的旧 baseline commit 只记录该规则最初对齐时的 provenance，不覆盖当前 Consumer baseline。Phase 1B 只将其从 `docs/` 根移动到 `docs/project/agentic-dev-continuous-execution-mode.md`，并由 `docs/README.md` 标为 supporting method note；不借目录迁移重写 Consumer-local Method。

`docs/architecture/decisions/**` 保持 Architecture Decision Record 区域，由 `docs/README.md` 显式列入 Authority Map；本 Unit 不机械重排其内容。

### Technical Planning decision

**No additional persistent Technical Plan is required.**

理由：目标目录模型、archive exclusion、Authority Map、Root README responsibility、subtree README gap 与 Current/Historical 分类已经由 `docs/project/pre-e1e3-convergence-plan.md` 和 `docs/project/documentation-authority-convergence.md` 固化；本 Unit 不引入 runtime architecture、schema、API、build 或跨 Execution Unit 技术协调。实现所需的是一次原子的文档路径迁移与引用修复，而不是新的长期 HOW 决策。

## Ready Specification

### Objective

把 Phase 1A 已完成的语义分类落实为可长期恢复的物理 Documentation Information Architecture，使 Fresh Context 默认从 Current Authority 恢复项目，而不需要从同级目录中区分 Current、Superseded 与 Historical Work 状态。

### In Scope

1. **Stable root entry**
   - 压缩根 `README.md` 为稳定项目入口；
   - 保留项目目标、当前 Scope、Authority / Roadmap / Method locator、最小运行与验证入口；
   - 移除长期 EU ledger、逐 PR / Action 历史与会快速陈旧的重复阶段状态。

2. **Repository documentation Authority Map**
   - 新增 `docs/README.md`；
   - 明确 `project/`、`requirements/`、`specifications/`、`technical/`、`architecture/`、`work/` 的职责与 Current Authority 恢复顺序；
   - 明确任何 `archive/**` 默认不参与 Fresh Context Current Authority 恢复，除非 Current Authority 明确要求追溯历史证据。

3. **Requirements / Specifications / Technical current-vs-archive split**
   - 新增 `docs/requirements/README.md`、`docs/specifications/README.md`、`docs/technical/README.md`；
   - 根级默认保留 `CURRENT` 以及 Phase 1A 已完成 currentness reconciliation 后仍承担 Current semantics 的文档；
   - 仅把 Phase 1A 已明确分类为 `SUPERSEDED` / `HISTORICAL_EVIDENCE` 的文件移动到对应 `archive/`：
     - `docs/requirements/information-publishing-eu30-amendment.md` → `docs/requirements/archive/`；
     - `docs/specifications/admin-frontend-convergence.md`、`center-main-site-core.md` → `docs/specifications/archive/`；
     - `docs/technical/admin-frontend-convergence.md`、`center-main-site-core.md`、`rich-text-authoring-research.md` → `docs/technical/archive/`。
   - 不因文件名、年龄或 EU 完成状态归档任何其他 Requirement / Specification / Technical Authority。

4. **Work lifecycle split**
   - 新增 `docs/work/README.md`、`docs/work/current/README.md`、`docs/work/archive/`；
   - planning baseline 上 EU-45 之前的所有 `docs/work/*.md` 已由 Phase 1A 归类为 Historical Evidence，EU-43 / EU-44 也已完成；它们进入 `docs/work/archive/`；
   - EU-45 planning artifact 在 Execute 期间按 work lifecycle 可进入 `current/`；完成与 Current Evidence closure 后必须进入 `archive/`，不能在 Phase 1B 完成后继续以 Current EU 身份留在根级或 `current/`；
   - 若 Execute baseline 出现新的未分类 work artifact，必须视为 base drift，先重新分类，不能按 glob 机械归档。

5. **Supporting method note placement**
   - `docs/agentic-dev-continuous-execution-mode.md` → `docs/project/agentic-dev-continuous-execution-mode.md`；
   - 其 Authority role 由 `docs/README.md` 标明为 supporting Consumer-local method note，不替代 `AGENTS.md` / `docs/project/development-method.md` / 当前 baseline locator。

6. **Subtree ownership README**
   - 为 `backend/`、`frontend/`、`sites/`、`sites/jilinjobs/` 增加最小 README；
   - 只说明该 subtree 的稳定责任、关键入口和上游 Authority locator；
   - 不复制全局 Roadmap、Current EU、临时 Actions、产品需求全文或第二套 Authority Map。

7. **Atomic link repair**
   - 修复 Current Authority、Root README、project docs、code comments / workflow docs 中所有指向被移动文档的 Current links；
   - archive 内部历史文本允许保留当时路径作为明确 historical context，但任何仍被 Current Authority 直接引用的 link 必须解析到新路径；
   - 所有新 README link 必须存在且角色描述与 Phase 1A classification 一致。

8. **Phase closure synchronization**
   - 在 implementation / evidence closure 中同步 `AGENTS.md`、Root `README.md`、Roadmap、Issue #92 与仍承担 Current planning locator 的 Phase 1 Authority；
   - Phase 1B 完成后 Current Ready Execution Unit 返回 `NONE`，下一 Gate 才允许转入 Phase 2 Planning Candidate；
   - Integration / Post-Integration Current Evidence 未闭环前不得声明 Phase 1 完成。

### Non-goals

- 不修改 Product Goal、Scope、User-visible Behavior、CMS contract、API、schema、Flyway、runtime、frontend、Site Package、Canonical Dataset 或 migration behavior；
- 不重新做 EU-43 / EU-44 semantic reconciliation；
- 不把 `PARTIALLY_CURRENT` 文档仅因分类名包含“partial”而归档；这些文档在 EU-43 后仍承担 Current semantics；
- 不移动、归档或重写 `data-migrations/**`；“Historical Migration”是业务数据来源角色，不等于 Documentation Historical Evidence；
- 不进入 Phase 2 Backend Application / Core Boundary、Generic Content Migration Application 或 Party Migration De-specialization；
- 不进入 Issue #60 / E1～E3；
- 不进行 Repository split；
- 不更新 `agentic-dev` baseline；
- 不借 README / link repair 引入新的产品、架构或方法规则。

## Slice-work Result

### Candidate Execution Unit

**EU-45 — Documentation Information Architecture & Archive Migration**

Phase 1B 形成单一 Candidate Execution Unit，不进一步拆成 README、archive move、work move、subtree README 或 link-repair 子 EU。

原因：

- Current / archive 目录切换与引用修复必须原子一致；
- 把物理 move 与 link repair 拆开会产生可合并但不可恢复的中间状态；
- Root README、`docs/README.md`、category README 与 subtree README 共同定义同一 Information Architecture；
- 所有变化均为 documentation-only、范围有界且可用同一静态验证合同证明，单 Unit 仍满足 Fresh Context fit。

### Dependency / rollback boundary

- dependency：仅依赖已完成的 EU-43 / EU-44 semantic currentness；
- rollback：整个 docs IA commit / PR 可独立回滚，不涉及 runtime/data side effect；
- review：一次 review 可以同时判断 archive manifest、Authority Map 与 link consistency；
- downstream：EU-45 完成并取得 Post-Integration Current Evidence 后，Phase 1 才允许关闭并进入 Phase 2 Planning。

## Acceptance

1. 根 `README.md` 是稳定入口，不再承担详细 EU / PR / Actions 历史 ledger。
2. `docs/README.md` 成为唯一 Repository-level Documentation Authority Map，并明确 `archive/**` 的 Fresh Context exclusion。
3. Requirements / Specifications / Technical 根级不再混放已明确 `SUPERSEDED` / `HISTORICAL_EVIDENCE` 文件；archive manifest 与 Phase 1A classification 一致。
4. `PARTIALLY_CURRENT` 但已完成 currentness reconciliation、仍承担 Current semantics 的文档保留在 Current 根级。
5. `docs/work/` 有清晰 current / archive lifecycle；Phase 1B 完成后没有已完成 EU 继续冒充 Current Execute Authority。
6. `docs/agentic-dev-continuous-execution-mode.md` 不再作为未分类 docs-root 文件；其 supporting method role 在新位置与 Authority Map 中明确。
7. `backend/`、`frontend/`、`sites/`、`sites/jilinjobs/` README 只承担 subtree responsibility / locator，不形成第二套 global Authority / Roadmap。
8. 所有 Current links 指向存在路径；没有 Current Authority 继续引用被移动文件的旧路径。
9. `data-migrations/**`、product/runtime/code/schema/API behavior 不发生变化。
10. 最终 changed-file set 只包含本 Unit 的 docs IA、README、link repair 与必要 Current Gate synchronization。
11. Exact-head Repository CI PASS，PR unresolved review threads = 0；合并后 `main` Post-Integration CI PASS，并重新读取 Fresh Context locators 确认 Phase 1 closure 无 lifecycle drift。

## Verification

### Planning / readiness evidence

- `main@9435fc4d2a34f83ec683457d32f326b954b6db76` dependency re-read；
- Issue #92 / #77 / Roadmap / Development Method / Phase 1 Authority current-state review；
- Open PR / Issue / recent Actions review；
- Phase 1A classification-to-move manifest review；
- root-level unclassified documentation review；
- Technical Planning necessity review。

### Execute static verification

- exact changed-file list / compare against Execute baseline；
- archive manifest review against Phase 1A classifications；
- repository scan for old moved paths；
- repository scan for `docs/work/*.md` root leakage after migration；
- Markdown link/path existence validation for Current docs；
- Authority Map / category README / subtree README semantic review；
- `git diff --check` equivalent and repository docs checks available in CI；
- explicit no-runtime/code/data change review。

Historical text inside `archive/**` may preserve old statuses or paths when they are clearly evidence; the verification target is that Current Authority never consumes them as current responsibility and all Current links resolve.

### Repository evidence

- exact-head CI on implementation PR；
- PR changed-files / diff / unresolved review threads；
- merge after required checks；
- Post-Integration CI on `main`；
- post-merge Fresh Context locator re-read.

EU-45 is documentation-only Information Architecture. It does not require Browser / Visual / Human Runtime Review or Architecture Model Eval unless execution unexpectedly changes runtime/product/architecture scope, which would invalidate this Readiness and require replanning instead of silently expanding verification.

## Readiness Check

### Input completeness

- Parent Planning Authority: **READY** — Issue #92 / Phase 1 Authority are current and Phase 1A dependencies are completed.
- Ready Specification: **READY** — objective, exact archive roles, root/supporting doc classification, scope, non-goals and acceptance are explicit above.
- Technical Planning: **NOT REQUIRED** — existing Phase 1 Authority already fixes the long-term IA; no new cross-EU HOW is introduced.
- `slice-work`: **COMPLETED** — one atomic Candidate EU formed.
- Verification contract: **READY** — static + repository evidence is sufficient for the documentation-only claims.
- Human/Product ambiguity: **NONE** — file organization / archive placement is low-impact and reversible; no Product / Scope / Architecture direction changes.
- Baseline upgrade: **NOT REQUIRED**.

### Decision

**PASS — EU-45 is a Ready Execution Unit once this planning/readiness change is integrated into `main`.**

This PASS does not itself authorize work on the unmerged planning branch. Fresh-context Execute must use the integrated `main` commit as its Execute baseline and revalidate base drift, Open PR, Issue #92 and Actions first. Any new unclassified document, changed Phase 1 Authority, product/runtime scope change, or conflicting parallel PR invalidates the assumptions above and returns the Unit to Planning / Readiness before implementation.
