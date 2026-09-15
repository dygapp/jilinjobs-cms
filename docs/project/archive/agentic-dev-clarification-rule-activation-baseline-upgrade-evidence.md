# agentic-dev Clarification / Rule Activation baseline 升级证据

状态：**INTEGRATION CANDIDATE / UPGRADE-ONLY EVIDENCE**。

本文件记录 `jilinjobs-cms` 对 `dygapp/agentic-dev@ed1a4446f0430890e7ad39673ac9c2e341e6a829` 的显式 baseline upgrade 判断。它只承担 provenance、capability disposition 与 targeted revalidation contract，不构成产品 Requirement、Planning Authority、Execute Authority、Rule routing table、Model Collaboration runtime config 或 ordinary runtime 依赖。

只有本升级 PR 经 Consumer Repository Integration Gate 合并后，本文件所描述的 projection 才成为 `main` 上已集成状态；PR Head、Checks、Review、mergeability 与最终 integration commit 始终由 GitHub native state 持有。

## 1. Consumer upgrade base

- Consumer Repository：`dygapp/jilinjobs-cms`
- upgrade base：`jilinjobs-cms@91ffb0ce433aef7ccd1cd1908bb20555211da2e7`
- base Current Work：`docs/work/current/README.md` = `Current Ready Execution Unit: NONE`
- base Open PR：0
- Previous Evaluated Baseline：`agentic-dev@8e7e94eff62b958b2044407cf6d85de3dde48ee9`
- Candidate Evaluated Baseline：`agentic-dev@ed1a4446f0430890e7ad39673ac9c2e341e6a829`
- Historical V4 Foundation Projection：`agentic-dev@3e0b2f5a29caeb344da79f8c96ebffbeb5c2b0cb`
- Capability Milestone：`baseline-2026-09-04-engineering-capability@5be2e6aad29b2be6b8535b3690daf3533ee22a46`

GitHub exact compare：

```text
8e7e94eff62b958b2044407cf6d85de3dde48ee9
...
ed1a4446f0430890e7ad39673ac9c2e341e6a829

status: ahead
ahead_by: 8
behind_by: 0
merge_base: 8e7e94eff62b958b2044407cf6d85de3dde48ee9
```

因此本轮是从 Consumer 当前 evaluated frontier 的线性 upgrade，不经过 historical `3e0b2f5a...` projection。

## 2. Upstream delta classification

8 个 upstream commits 的最终语义按 capability owner 分为：

1. **Model Collaboration Capability & Adoption v1**：新增 reusable Collaboration Architecture、Adoption Method、Guide / Codex reference 与 upstream Project instance mapping；
2. **Software Project Clarification v1**：新增 `method:software-project-clarification`，并收紧 `method:ai-development` 的 Feature entry / return boundary；
3. **Rule Discovery activation hardening**：新增 direct-responsibility checkpoint，明确首个 side effect 前 task-level discovery、responsibility transition 后重新发现，以及 CI smoke 不得替代 ordinary runtime invocation；
4. **upstream Project closure / roadmap / research state**：只属于 `agentic-dev` 自身 Project Knowledge；
5. **noop + subsequent removal**：最终 target tree 不形成 reusable semantic delta。

升级比较按 `method:consumer-upgrade` 的原则处理 reusable semantic responsibility，而不是机械同步 changed files。

## 3. Disposition

### 3.1 adopt / adapt — Software Project Clarification

Consumer 接受 bounded project-level clarification semantics，并投射到 `docs/project/development-method.md`：

- 只在多个当前或预期 Feature 共同依赖的长期 Requirement / Architecture Context 缺失、冲突或需要重建，并因此无法安全进入可靠 Specification 时选择；
- 普通单 Feature / bugfix / 小范围变更继续使用现有 Feature workflow；
- lifecycle：`Establish Context → Requirement Clarification → Architecture Clarification? → Clarification Convergence → Clarified Project Context Ready`；
- phase identities：`establish-context` / `requirement-clarification` / `architecture-clarification` / `clarification-convergence`；
- Architecture Clarification 保持 conditional / anti-BDUF；局部可逆 HOW 仍属于 Feature Technical Planning；
- 高风险 legacy / heterogeneous Requirement reconstruction、批量 Authority 变换、跨 Authority 冲突收敛需要 independent semantic review；
- completion 只返回 `Clarified Project Context Ready`，不自动创建 Specification、Execution Unit、Execute / Integrate Authority；
- 当前不为该 Method 预建新的 Skill / Rule。

本次 baseline upgrade **不启动**项目级澄清 lifecycle；它只建立可选择的 Consumer-local Method contract。

### 3.2 adapt — Feature Development responsibility boundary

现有常规 Feature Method 保持原生命周期，但补充边界：

- 进入 Feature workflow 前，当前 Repository 应已有足以支持当前 Feature 判断 Goal / Scope / Observable Behavior / Acceptance 的最小长期 Requirement / Domain / Architecture Context；
- 单 Feature 局部 Requirement ambiguity 返回当前 Feature owner；
- 多 Feature / core-domain systemic Requirement gap 返回长期 owner / project clarification；
- Feature-specific architecture HOW 继续属于 Technical Planning；只有跨多个 Feature 且阻塞可靠 Specification 的长期 architecture driver 才升级到 project clarification；
- Converge 不承担项目级 Requirement / Architecture reconstruction。

该变化不改变现有 Planning Candidate / `slice-work` / Readiness / Execute / Integration Authority 边界。

### 3.3 adopt — direct-responsibility Rule Discovery checkpoint

Consumer 接受 latest Rule Discovery activation semantics，并投射到 `AGENTS.md` 与 `docs/project/rule-discovery-method.md`：

- 当前 direct responsibility 建立后，首个有副作用动作前必须完成 task-level discovery；
- 为恢复 Authority / Repository facts 的只读操作可以先于 discovery；
- direct responsibility 或关键 phase / activity / technology / artifact / risk facts 实质变化后，在下一次 side effect 前重新构造 signals 并 discovery；
- 旧 candidate set 不跨职责永久有效；
- CI Rule Discovery lint / deterministic tests / fixed smoke 只证明 Tool 与 corpus contract，不携带当前 Agent 的实时 task signals，因此不得替代 ordinary runtime invocation。

本次**不改变**：

- `tools/rule-discovery/rule_discovery.py` 算法；
- 五维 signals 与 known / known-empty / unknown；
- 每维最多 6 tokens；
- locator-only `{id,path}` output；
- candidate body semantic confirmation；
- local-only / fail-closed；
- Rule granularity / Technology human IA；
- Consumer Rule count = 13、Skill count = 9。

### 3.4 retain — Consumer-local capability instance

继续保留既有 Consumer-local owner：

- `docs/project/git-commit-guidelines.md`；
- Vue 3 + TypeScript Technology / Verification Profile；
- 13 条 Consumer-local Rules 与本地 specialization；
- 9 个 Consumer-local Skills；
- Current Work / Roadmap / Requirement / Specification / Architecture owners；
- Local Discovery Entry 与 ordinary runtime upstream decoupling。

### 3.5 reject / not-applicable — Model Collaboration current adoption

本轮不把 upstream Model Collaboration 投射为 Consumer current capability，原因：

- 当前 Consumer 没有已验证的 subagent / multi-model local runtime instance；
- 没有 Consumer-local model / effort tier mapping、delegation / writer ownership config、validation evidence 或 fallback instance；
- 当前用户目标是 baseline upgrade，不是 collaboration runtime activation；
- upstream 明确区分 reusable semantic acceptance 与 local runtime activation，不能从 capability 存在反推 Consumer 已启用。

因此本轮：

- 不新增 Model Collaboration Architecture owner；
- 不新增 Model Collaboration Adoption Method instance；
- 不新增 `.codex` / agent profile / model routing；
- 不新增 collaboration Rule；
- 不声称任何 multi-model enablement / efficiency / preferred-default claim。

未来若明确要求采用，必须通过新的 Consumer-local semantic acceptance 与真实 runtime validation 决定，不继承本次 reject 状态之外的 upstream runtime 假设。

### 3.6 reject / not-applicable — upstream Project state

以下只保留 comparison provenance，不进入 Consumer current Authority：

- upstream Project Roadmap / Evolution；
- upstream Project Capability Profile instance；
- Model Collaboration Guide / Codex concrete reference config；
- Research / Eval / self-adoption state；
- upstream 当前 Issue / PR / Actions；
- noop commit 与其 cleanup history。

## 4. Consumer-local owner changes

本轮只允许治理 / Method / verification contract 变化，不修改产品实现：

- `AGENTS.md`：baseline、accepted delta 与 live Rule Discovery checkpoint；
- `docs/project/development-method.md`：baseline、Software Project Clarification selector、Feature boundary、Model Collaboration disposition；
- `docs/project/rule-discovery-method.md`：baseline 与 direct-responsibility checkpoint；
- `docs/project/project-roadmap.md`：durable method-upgrade milestone；
- 本 evidence：upgrade-only provenance / disposition / validation contract；
- `tools/rule-discovery/tests/test_runtime_activation.py`：Consumer-local activation / Method projection regression。

`docs/work/current/README.md` 保持 `NONE`；本轮不创建新的产品 Requirement / Specification / Technical Plan / Execution Unit。

## 5. Targeted revalidation contract

Candidate PR 必须以 exact PR Head 的 Current Evidence 证明：

1. Consumer current baseline surfaces 一致指向 `ed1a4446f0430890e7ad39673ac9c2e341e6a829`，previous baseline 为 `8e7e94eff62b958b2044407cf6d85de3dde48ee9`；
2. Software Project Clarification selector、四个 phase identity、conditional Architecture Clarification 与 `Clarified Project Context Ready` return boundary 存在于 Consumer-local Method；
3. 常规 Feature workflow 明确保持单 Feature responsibility，不把 systemic Requirement / Architecture reconstruction 吸收到 Feature Specification / Technical Planning / Converge；
4. `AGENTS.md` 与 Rule Discovery Method 明确要求 current direct responsibility 的首个 side effect 前 task-level discovery；
5. direct responsibility / key task facts transition 后需要在下一 side effect 前 rediscovery；
6. CI Rule Discovery PASS 明确不能替代 ordinary runtime invocation；
7. Consumer-local regression 能在 `phases: null` 时按本地 metadata 发现 `rule:integration-state-closure-review`，避免从 upstream test signals 机械复制错误 taxonomy；
8. Rule Discovery unit tests / lint PASS，Rule count 保持 13、Skill count 保持 9；
9. 既有 generation / Vue / verification discovery traces 继续 deterministic PASS；
10. Model Collaboration 没有被意外建立为 Consumer current runtime / config / Rule / Skill；
11. `docs/work/current/README.md` 仍为 `NONE`，没有新的产品 Execute Authority；
12. 文档治理与 Repository 当前要求的 CI 在 exact PR Head 上 PASS；
13. PR diff 只包含 baseline upgrade 所需治理 / Method / regression assets，无产品代码变化；
14. 完成前重新核对 Consumer `main`、base drift、Open execution lifecycle 与 PR mergeability。

其中 CI / deterministic regression 只能验证 contract 已正确固化；**不能把固定 CI smoke 描述成当前 Agent 的 ordinary-runtime live discovery evidence**。后续 ordinary runtime 必须按新的 local contract 在每个 direct responsibility 自己执行 task-level discovery。

## 6. Integration boundary

本升级：

- 不授予新的 Product Planning / Readiness / Execute Authority；
- 不启动 Software Project Clarification lifecycle；
- 不启动 Model Collaboration Adoption；
- 不修改 `dygapp/agentic-dev`；
- 不允许 ordinary runtime fallback 到 upstream；
- 不改变 Consumer 13 Rules / 9 Skills 的 corpus identity；
- 不因 upstream Project state / Guide / Research 变化同步 Consumer Project facts；
- 在本次 PR 完成 targeted verification、Blocking = 0、Medium = 0 且进入 Human Integration Gate 前，不把 candidate projection 描述为 integrated-main closure。

只有 Human Integration Gate 完成并在实际 integration commit 上取得必要 Post-Integration Evidence 后，`ed1a4446f0430890e7ad39673ac9c2e341e6a829` 才成为已集成 `main` 的 Current Evaluated Baseline。