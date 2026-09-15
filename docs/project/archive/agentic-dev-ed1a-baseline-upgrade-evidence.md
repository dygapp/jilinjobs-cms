# agentic-dev `ed1a4446` Baseline 升级证据

状态：**HISTORICAL_EVIDENCE / UPGRADE-ONLY**。

本文只记录本次 Existing Consumer 显式 baseline upgrade 的比较、disposition 与验证上下文，不拥有普通开发 Method、产品 Requirement、Current Execution Lifecycle、PR 状态或最终 Integration 状态。当前长期语义由 `AGENTS.md`、`docs/project/development-method.md`、`docs/project/rule-discovery-method.md` 与其他 Consumer-local canonical owner 持有；GitHub PR / Commit / Actions 继续拥有其原生瞬时事实。

## 1. 升级范围

Consumer Repository：`dygapp/jilinjobs-cms`

升级开始时 Consumer `main`：

`91ffb0ce433aef7ccd1cd1908bb20555211da2e7`

Previous Evaluated Baseline：

`dygapp/agentic-dev@8e7e94eff62b958b2044407cf6d85de3dde48ee9`

Candidate Baseline：

`dygapp/agentic-dev@ed1a4446f0430890e7ad39673ac9c2e341e6a829`

GitHub exact compare：

```text
status: ahead
ahead_by: 8
behind_by: 0
total_commits: 8
```

本轮 candidate 固定为上述精确 SHA；candidate 评估期间后续 upstream commit 不自动进入本次范围。

## 2. Consumer Upgrade Method

本轮按 upstream `method:consumer-upgrade` 的语义执行：

```text
Restore Current Consumer
→ Select Candidate Upstream Baseline
→ Evaluate Capability Delta
→ Apply Local Decisions
→ Refresh Local Capability Instance
→ Targeted Revalidation
→ Close New Evaluated Baseline
```

核心边界为：**Project 不传播，Capability 传播**。

因此比较对象是 reusable Method / Architecture / Rule Discovery / runtime contract 的语义变化，而不是把 `agentic-dev` Project Roadmap、Project Capability Profile、Research、Eval 或 self-adoption 状态镜像进 Consumer。

## 3. Upstream delta 分类

`8e7e94ef... -> ed1a4446...` 的 8 个提交可以归入以下语义组：

1. **Model Collaboration Capability & Adoption v1**：新增 reusable Model Collaboration Architecture、Adoption Method 与 supporting guides / research，并形成 upstream 自身 Project instance / milestone；
2. **Software Project Clarification v1**：新增面向普通软件 Consumer 的项目级 Requirement / Architecture Clarification Method，并收紧 `method:ai-development` 的具体 Feature / change 边界；
3. **Rule Discovery runtime activation hardening**：明确 direct-responsibility checkpoint、首次 side effect 前 discovery、职责转换后 rediscovery，以及 CI smoke 不能替代 ordinary runtime invocation；
4. **Project-only closure / milestone / profile changes**：只反映 upstream 自身 capability instance 与 Project evolution；
5. **`noop` + accidental noop removal**：无 reusable semantic delta。

## 4. Per-item disposition

### 4.1 adopt / adapt — Software Project Clarification

Consumer 接受项目级澄清的 reusable boundary，但投射为 Consumer-local Method selection，而不是复制 upstream 文件。

采用语义：

- 常规 AI Development 面向具体 Feature / change；
- 若多个当前或预期 Feature 共同依赖的长期 Requirement / Architecture Context 缺失、冲突或需要重建，不能在单 Feature 内局部创造长期事实；
- 项目级 Software Project Clarification 只有在上述系统性条件成立时才进入；项目大、技术复杂、文档多或单 Feature 局部不确定都不是进入理由；
- 生命周期采用 `Establish Context → Requirement Clarification → Architecture Clarification? → Clarification Convergence`；
- stable phase identities 为 `establish-context`、`requirement-clarification`、`architecture-clarification`、`clarification-convergence`；
- Requirement Clarification 将确认的长期事实提升到真实 Consumer-local Requirement / Domain owner；
- Architecture Clarification 是条件阶段、anti-BDUF，不吸收普通 Feature-specific HOW；
- 完成只返回 `Clarified Project Context Ready`，不产生 Specification、Execution Unit、Execute 或 Integrate authority；
- legacy / heterogeneous rebuild、大规模 Authority 重构、冲突来源实质合并或批量改变长期业务语义时，Ready 前要求独立语义复核；
- 当前不因新增 Method 机械创建 Clarification Skill 或 Rule。

Consumer-local owner：`docs/project/development-method.md`。

### 4.2 adapt — AI Development boundary

普通 Feature workflow 增加最小长期 Context 前提，并明确：

- 局部 Feature ambiguity 返回当前 Feature / Requirement owner；
- 系统性多 Feature Requirement / Architecture gap 返回真实长期 owner 或项目级澄清责任；
- Technical Planning 与 Converge 不静默演变为项目级 Requirement / Architecture 重建阶段。

Consumer-local owner：`docs/project/development-method.md`。

### 4.3 adapt — Rule Discovery responsibility-transition checkpoint

采用以下 ordinary-runtime invariant：

- current direct responsibility 建立后，在该责任首个有副作用动作前必须完成 task-level discovery；
- read-only fact recovery 可以先于 discovery；
- direct responsibility 切换，或 `phase / activity / technology / artifact / risk` 等关键事实实质变化后，在下一次有副作用动作前重新构造 signals 并 discovery；
- 旧 candidate set 不跨职责永久有效；
- CI Rule Discovery lint、deterministic test 与固定 smoke scenario 只验证 tool / corpus contract，不能证明当前 Agent 已完成实时 task-level discovery，也不能替代 ordinary runtime invocation。

Consumer-local owners：`AGENTS.md`、`docs/project/rule-discovery-method.md`、`docs/project/development-method.md`。

定向回归：`tools/rule-discovery/tests/test_runtime_activation.py`。

### 4.4 retain / reject activation — Model Collaboration

Consumer 已完成 reusable semantic evaluation，但本轮**不启用 Model Collaboration runtime instance**。

理由：

- 当前用户目标是 baseline upgrade，不是建立 multi-model runtime；
- 当前 Consumer 没有已接受的 local collaboration strategy、tier mapping、runtime/provider mapping、writer policy extension 或 current delegation smoke evidence；
- upstream 明确要求 semantic acceptance 与 local runtime activation 分离，且 local activation 应由独立 Adoption lifecycle 建立并验证；
- 为“跟上 upstream”复制 `.codex`、具体模型名、provider 配置、upstream Project profile 或 enabled state 会制造未经验证的 Consumer instance。

因此本轮不新增 Model Collaboration config / Rule / Project profile pointer，不声称 enabled / conditional。未来若有明确需求，必须重新从 Consumer-local Authority 形成独立 semantic / adoption decision，并验证 actual runtime capability、delegation、single-writer、observability 与 single-agent fallback。

### 4.5 retain — 现有 Consumer-local capability

本轮保持：

- 13 条 discoverable Rule；
- 9 个物理 Skill；
- Consumer-local deterministic five-dimension Rule Discovery Tool；
- Vue 3 + TypeScript Technology / Verification Profile；
- Git commit governance；
- Repository operation boundary；
- Verification Strategy 与既有 local specialization。

没有证据要求为了 upstream 新 inventory 改变这些 owner。

### 4.6 reject / not applicable — upstream Project state

以下内容不传播为 Consumer Current Authority：

- upstream Project Roadmap / Project Evolution；
- upstream Project Capability Profile 的 concrete instance mapping；
- Research / Eval corpus；
- upstream self-adoption / milestone state；
- Guide 中的具体 runtime example；
- `noop` 与其删除提交。

## 5. Consumer-local projection

本轮 projection 仅限方法与治理资产：

- `AGENTS.md`；
- `docs/project/development-method.md`；
- `docs/project/rule-discovery-method.md`；
- `docs/project/project-roadmap.md`；
- `tools/rule-discovery/tests/test_runtime_activation.py`；
- 本 upgrade-only evidence。

不修改产品 Requirement / Specification、四层 CMS Architecture、应用代码、Site Package、Migration input 或 Current Work lifecycle。

## 6. Targeted revalidation

本轮需要验证的 claim：

1. Consumer baseline pointer 在稳定 owners 中一致指向 `ed1a4446f0430890e7ad39673ac9c2e341e6a829`，Previous 指向 `8e7e94eff62b958b2044407cf6d85de3dde48ee9`；
2. Software Project Clarification 的进入条件、阶段、Return Contract 与 ordinary Feature boundary 在 Consumer-local Method 中可由 Fresh Context 独立恢复；
3. Rule Discovery responsibility-transition checkpoint 已进入 Bootstrap / Method owner；
4. regression test 能约束“首个 side effect 前 discovery”“CI 不能替代 runtime invocation”“old candidate set 不跨职责”；
5. 现有 Rule corpus 仍为 13、Skills 仍为 9，deterministic discovery / lint contract 不发生无意变化；
6. Model Collaboration 没有被误标为 Consumer enabled instance；
7. upstream Project state 未泄漏进 Consumer Current Execution State 或产品 Authority；
8. ordinary runtime 在升级完成后恢复 `upstream access = 0`；
9. Consumer side `main` / PR base drift 在 Integration Gate 前重新核验。

本轮不因为 baseline upgrade 自动重跑所有历史产品 E2E / Human Review；若实际 diff 未触及产品实现或其 Acceptance owner，这些旧产品验证不属于本次新 claim 的必要证明。

## 7. Integration contract

候选分支中的 `Current Evaluated Baseline = ed1a4446...` 表达的是**拟集成后的 Consumer-local canonical state**。在 PR 尚未由 Human Authority 授权合并前：

- `main` 上已集成的 baseline 仍以 `main` 当前 Authority 为准；
- PR exact-head checks 只证明 candidate 是否满足 Integration Gate；
- 不得把 open PR 状态表述成 integrated main 事实。

只有 Human-authorized merge 完成，并在实际 integration commit 上取得 Repository 所需 Post-Integration Evidence 后，本轮 baseline upgrade 才完成 integrated closure。合并授权不由本文提供。

升级完成后普通运行必须只依赖 Consumer-local Authority；不得为了使用本轮采用的 Method / Rule Discovery contract 在线读取 upstream current state。
