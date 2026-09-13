# agentic-dev V3 Closure Baseline Upgrade Evidence

> **Classification: HISTORICAL_EVIDENCE / UPGRADE-ONLY**  
> 本文件记录 `jilinjobs-cms` 从 `agentic-dev@2fe193035c629f6b8805fd473bd322f70fe6e172` 显式升级到固定候选 `agentic-dev@1c8cdfea9ecf23ef33ffab20eec3c93679fd4578` 的采用判断与治理收敛证据。它不参与普通 Fresh Context，不拥有 Product / Architecture / Current Execution State，也不替代 Consumer-local Development Method。

## 1. Upgrade scope

- Consumer audit base: `main@82d1515377bcd7ea4d42d915a16c3523697be68b`
- Previous evaluated baseline: `2fe193035c629f6b8805fd473bd322f70fe6e172`
- Fixed upstream candidate: `1c8cdfea9ecf23ef33ffab20eec3c93679fd4578`
- Candidate verification: candidate was also current upstream `master` at adoption pre-check time
- Exact compare: **ahead 5 / behind 0**
- Governance authority: GitHub Issue #146
- Consumer-local projection pre-check Head: `2e202a07902f2ef4cd04ff2a69e5ce7f4072f743`

本次 upgrade 明确区分：

```text
evaluated upstream baseline
!= current local asset provenance / semantic owner
!= upgrade-only decision history
```

只有经逐项采用并投射到现有 Consumer owner 的 reusable semantics 才进入 ordinary runtime；上游 Project / Review / fixture / closure state 不继承。

## 2. Exact upstream delta disposition

| Upstream commit | Semantic delta | Disposition | Consumer result |
|---|---|---|---|
| `f3b63ccf6ce023ee39166afbc205289de4ce2927` | V3-08 Consumer verification Gate A | `UPSTREAM PROJECT-ONLY` | 不投射 upstream 项目 Gate / Evidence state |
| `86fe96756c7b3678d7b0bac10f32358a9372e84c` | Artifact lifecycle / Skill inventory convergence | `ADOPT` | 将 Handoff 收敛为条件性临时 runtime transition artifact；不是 current Skill；投射到 `docs/project/execution-continuity-guidelines.md` |
| `ea6802c85f7d9c5549c3fb837c365ece695cbc35` | V3 Independent Review Gate A | `UPSTREAM PROJECT-ONLY` | 不投射 |
| `18dc921952491676f12bee885930dbe1b0363f96` | Independent Review Gate C findings / fixture evidence | `UPSTREAM PROJECT-ONLY` + overlapping reusable semantics `RETAIN / OVERRIDE` | 不复制 Review / fixture state；Consumer-local Verification / Discovery owners 继续有效 |
| `1c8cdfea9ecf23ef33ffab20eec3c93679fd4578` | V3 Closure Decision | `UPSTREAM PROJECT-ONLY` | 仅作为本轮 exact evaluated frontier；不继承 upstream Roadmap / closure state |

### Retained Consumer-local capabilities

以下语义在本 Consumer 已有更具体、持续有效的 current owner，本轮不复制 upstream Guide / Architecture 文本：

- Verification / Current Evidence / exact-head claim matching；
- External Operation / bounded async observe-diagnose-retry-verify；
- Engineering Discipline / implementation minimality / diff scope / bounded data access；
- Vue 3 + TypeScript Technology / Verification Profile；
- Local Discovery Entry 与 state-only / routing-only / execution progressive disclosure；
- ordinary runtime Consumer-local + stale / missing / ambiguity / no-match risk fail-closed；
- 8 个 core Skills：`clarify-intent`、`specify`、`technical-plan`、`slice-work`、`readiness-check`、`execute-unit`、`systematic-debug`、`converge`；
- platform-specific `github-actions-verification`。

### Rejected / not required

- 不复制 `agentic-dev` README / Roadmap / Issue / PR / Independent Review / V3 closure project state；
- 不复制 Research / Eval / self-adoption discovery instance；
- 不新增 Handoff Skill；
- 不新增 Reviewed Discovery Map；
- 不新增 Runtime View；
- 不把 upstream 变成 ordinary runtime 在线依赖。

## 3. Adopted Handoff lifecycle

Consumer current owner: `docs/project/execution-continuity-guidelines.md` §6.1。

Handoff 只在同时存在以下事实时形成：

1. 真实未完成且需要跨 Context / Agent / 时间继续的 transition state；
2. 明确 producer；
3. 明确 downstream consumer；
4. 仅从 Current Repository Authority / GitHub native state 不能以同等可靠性和更低重复成本恢复所需状态。

如果 Repository / Current Work / Issue / PR / Actions / durable evidence 已能恢复，则不创建 Handoff。Handoff 被消费或状态已持久化到正式 owner 后立即失效；不得长期化为 Requirement、Method、Architecture、Current State 或第二份执行真值。

Fresh Context prompt 继续是 **Prompt-as-Locator**，不是 Handoff Artifact 或 Authority snapshot。

## 4. Consumer-local governance findings and resolution

本轮 baseline upgrade 同时对 Consumer current governance 做系统性审计。未发现 Blocking；识别并关闭以下 Medium：

| Finding | Resolution |
|---|---|
| M1 `documentation-authority-convergence.md` 仍像 current classification owner，并携带旧 Phase 2 / E1～E3 state | 文件明确降级为 `SUPERSEDED / HISTORICAL_EVIDENCE`；`docs/README.md` 成为 current Classification / Documentation IA / Local Discovery owner |
| M2 `main-site-formal-content-plan.md` completed plan 仍维护 current-looking gate | 明确为 `HISTORICAL_EVIDENCE / COMPLETED PLANNING RECORD`；状态段改为 closure snapshot；current state 回到 Current Work / Roadmap / GitHub native state |
| M3 `agentic-dev-continuous-execution-mode.md` 与当前 Method 重复并带旧 upstream provenance | 明确 `SUPERSEDED / HISTORICAL_EVIDENCE`；current semantics 只由 Development Method + Execution Continuity 承担 |
| M4 Issue #60 仍声称 E3 为 current / next Planning Gate | Issue #60 收敛为 candidate-pool-only；E1～E3 标记 historical closure；不得授予 current Planning / Readiness / Execute Authority；清理 stale labels |
| M5 Issue #77 长期 Architecture owner 混入高频 execution / next-gate state | 保留四层 long-lived Architecture ownership；移除 current execution responsibility；高频状态交回 Current Work / Roadmap / GitHub native state |
| M6 Project Roadmap 仍把已完成的 Issue #137 / Page Content Architecture 列为开放候选 | Roadmap 增加 EU-55 durable milestone，并将 Issue #137 收敛为 completed architecture traceability；开放 candidate 列表只保留真实未完成方向 |

Phase 1 exact diff at `2e202a07902f2ef4cd04ff2a69e5ce7f4072f743`：仅 5 个 Markdown governance/method records；Consumer `main` 与 upstream fixed candidate 均无 side drift；`docs/work/current/README.md` 仍为 `Current Ready Execution Unit = NONE`。

Phase 1 AI Review：

- Blocking: **0**
- Medium: **0**
- Scope drift: **none found**
- Product / runtime behavior change: **none**
- Adoption pre-check: **PASS**

M6 在 baseline-owner / Roadmap 同步阶段发现并立即关闭，不改变 Phase 1 Handoff projection verdict。详细 pre-check 与 follow-up evidence 记录在 Issue #146。

## 5. Baseline progression gate

只有在上述 Consumer-local projection 已完成并通过 adoption pre-check 后，才允许 current baseline owners 从升级前的：

```text
Previous evaluated: d9fad0da83dbdb61cac5eb9778b0258c6861eef1
Current evaluated:  2fe193035c629f6b8805fd473bd322f70fe6e172
```

推进为：

```text
Previous evaluated: 2fe193035c629f6b8805fd473bd322f70fe6e172
Current evaluated:  1c8cdfea9ecf23ef33ffab20eec3c93679fd4578
```

推进 baseline pointer **不**重新归因已存在的 Consumer-local assets；V3-08 Track B 的 `d9fad0da... -> 2fe193...` evidence 继续由 `docs/project/agentic-dev-v3-08-track-b-evidence.md` 原样保留。

Current baseline owner / durable locator 同步范围：

- `AGENTS.md`；
- `docs/project/development-method.md`；
- `docs/project/project-roadmap.md`；
- `docs/README.md` 只负责把 upgrade history 标为非普通入口，不复制 baseline pointer。

## 6. Ordinary-runtime return condition

本轮 candidate 只有在最终 exact-head governance review 继续满足以下条件时，才可进入 Human Integration Decision：

- current baseline owners 一致指向 `1c8cdfea...`；
- upgrade-only evidence 与 current owner 分离；
- Local Discovery 仍然 Consumer-local / progressive / fail-closed；
- Skills inventory 仍为 8 core + `github-actions-verification`，Handoff 不成为 Skill；
- Issue #60 只作为 candidate pool；Issue #77 只拥有长期四层 Architecture Boundary；Issue #137 已退出开放 Planning Candidate；
- no Product / Runtime / Migration / Architecture direction drift；
- Blocking = 0；
- Medium = 0。

即使 candidate 通过，未经 Human Integration Decision 也不得 merge。合并后只有重新读取 integrated `main` 并完成 Post-Integration governance verification，才算 baseline upgrade 真正进入 Consumer ordinary runtime。
