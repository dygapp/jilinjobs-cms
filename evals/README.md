# Consumer-local Architecture Review Evals

## Status

本目录是 Issue #92 / Phase 0 引入的**实验性**高能力架构评审机制。

它不是 `jilinjobs-cms` 的常规 Development Method，也不新增 Method Stage、Skill、Readiness Gate 或 Integration Gate。

Phase 0 首个真实 paired scenario（AR-02）已经完成，当前实验结论为：

```text
ADJUST
```

具体含义：

- 不把 GPT-6 review 变成普通开发默认步骤；
- 不要求每个 Execution Unit 都执行高能力模型评审；
- 只在高返工成本且普通 review 后仍存在真实 unresolved architecture ambiguity 时，选择一个 bounded scenario 做第二视角挑战；
- 本轮不继续 AR-03 GPT-6；AR-01 / AR-03 保留为 dormant corpus，需要真实争议时再启用；
- 当前一个 Consumer / 一个 paired scenario 的证据不足以形成跨项目 Method 结论，暂不向 `dygapp/agentic-dev` 提交正式反馈。

如果后续第二个真实场景再次证明稳定增量价值，或暴露具有复用价值的 model-isolation / evidence-lifecycle 问题，再重新评估 `RETAIN / ADJUST / DROP` 并决定是否向 `agentic-dev` 提交 Evidence。

## Purpose

本实验只回答一个问题：

> 对少数高代价、返工成本高的架构判断，是否值得使用一个受限 Fresh Context 的高能力模型进行第二视角挑战评审，并把结果保存为可审计 Review Evidence？

它不回答：

- 当前 Requirement 是否 Ready；
- Candidate Execution Unit 是否 Readiness PASS；
- PR 是否可以自动合并；
- GPT-6 是否拥有 Repository Authority；
- 是否应该把普通开发工作整体切换到昂贵模型。

## Corpus

当前语料：

```text
evals/architecture/pre-e1e3-convergence-review.json
```

场景：

- `AR-01` — Documentation Authority Architecture；
- `AR-02` — Backend Application / Gradle Module Boundary；
- `AR-03` — Generic Historical Migration Boundary。

当前使用策略：

1. 默认不自动升级高能力模型；
2. 先由普通 Planning / review 恢复真实 unresolved question；
3. 如果问题具有明显高返工成本，并且现有证据仍不足以稳定决策，再选择**单一** bounded scenario；
4. 若需要模型对照，先运行 lower-cost baseline，再以相同 exact Head / context / prompt 运行高能力模型；
5. 不运行 `--all` 式高能力评审，也不为了“完成 corpus”机械运行未触发的场景。

## Phase 0 AR-02 evidence

首个 paired scenario：`AR-02`。

共同 evidence：

- Planning Head：`52d8d59094eb5f02a2780ccf131363adac08a1dc`
- Codex CLI：`0.153.4`
- Context digest：`sha256:de1504125236bcea7154602ac60ed7b627a06eaa818302191bbb477e0d7abec1`
- Prompt digest：`sha256:bf8c2f357102652fea4367f7ead18c42b6e1963407ad8f341c9ce0bf8b88c5d9`
- lower-cost request：`gpt-5.6-sol` / medium
- high-capability request：`gpt-6-astra` / high
- 两次 return code：0
- 两次 stderr：empty
- Human Semantic Verdict：两次均 `SUPPORTED_WITH_CHANGES`
- AR-02 assertions：两次均全部 PASS

当前 Codex JSONL 没有暴露可独立确认的 actual runtime model / reasoning effort 字段，因此上述模型信息只能表述为 **requested model / effort**；不得声称 runtime trace 已二次证明实际模型。

完整原始 archive digest、usage、assertion grading、GPT-6 incremental value 与 Planning Impact 见 Issue #92 的 AR-02 Evidence comment。长期项目 Authority 不依赖本地 results 目录。

## Isolation

运行器：

```text
evals/run_architecture_review.py
```

每个 scenario：

- 独立 `codex exec --ephemeral --json`；
- 在 Repository 外部临时目录执行；
- 只复制语料声明的 `context_paths`；
- Runtime 看不到 corpus 文件本身，因此看不到 `assertions`；
- Runtime 不读取历史结果；
- Runtime 使用 read-only sandbox；
- 题面明确禁止 Web research、仓库外搜索、文件修改与重新规划整个项目。

如果 Runtime 实际获得 assertions / expected behavior / 历史结果，Run 判为：

```text
INFRASTRUCTURE_INVALID / CONTAMINATED
```

不能作为 Review Evidence。

## Exact-head evidence

运行器要求 Git worktree 对 tracked / untracked 变化都 clean；被 `.gitignore` 排除的 ephemeral results 不影响该 Gate。

运行器记录：

- `git rev-parse HEAD`；
- Codex CLI version；
- requested model；
- requested reasoning effort；
- scenario id；
- context file list；
- 每个 context file SHA-256；
- 聚合 context digest；
- prompt digest；
- exact command prefix；
- JSONL stdout；
- stderr；
- return code；
- JSONL 中可观察到的 model / effort hints（best effort）。

如果当前 Codex version 的 JSONL 无法暴露实际 runtime model / effort，则必须保留“requested but not independently observed”的事实，不得声称已经通过 runtime trace 二次证明。

## Running

先列出场景：

```bash
python3 evals/run_architecture_review.py --list
```

### Lower-cost bounded review

只运行当前真实争议对应的一个 scenario，例如：

```bash
python3 evals/run_architecture_review.py \
  --scenario AR-03 \
  --model <lower-cost-review-model> \
  --reasoning-effort medium
```

### Optional high-capability paired review

只有 lower-cost / ordinary review 后仍存在真实 unresolved question 时，才显式 opt-in：

```bash
python3 evals/run_architecture_review.py \
  --scenario AR-03 \
  --model gpt-6-astra \
  --reasoning-effort high \
  --allow-high-capability
```

这里的 AR-03 只是命令形状示例，不表示当前 Phase 0 待执行任务。

运行器通过独立 CLI 进程请求 model，并使用 Codex config override 请求 `model_reasoning_effort`；不要使用 resumed session 或 parent/subagent inheritance 作为本实验的模型隔离机制。

如果当前 Codex CLI 不支持请求的 model / effort，或运行 trace 暴露实际值与请求值不一致，该次结果不能被当成目标模型 Evidence，应先分类为 Eval Infrastructure / Runtime Availability 问题。

## Evidence location

原始运行结果写入：

```text
evals/results/architecture/<scenario>/
```

这些结果是 ephemeral execution evidence，不默认提交 Git。

长期 Promotion 方式：

- 在对应 Planning PR / Issue comment 中记录 exact Planning Head；
- 记录 scenario、requested model / effort、Codex version、context digest；
- 人工逐条记录 assertions 的 PASS / FAIL / NOT OBSERVABLE；
- 摘要 Blocking Findings 与实际 Planning revision；
- 明确 actual runtime model / effort 是否 independently observable；
- 如原始结果需要交接，可单独打包保存，但长期 Authority 不依赖本地临时结果目录。

## Semantic grading

**Process exit code `0` 只说明 Codex 进程结束，不表示 Review PASS。**

必须人工读取最终输出和必要 JSONL trace，逐条检查 corpus 中对应 scenario 的 `assertions`。

最小评分记录：

```text
Planning Head: <sha>
Scenario: <id>
Codex: <version>
Requested Model: <model>
Requested Effort: <effort>
Context Digest: <sha256>
Infrastructure: VALID | INVALID | NOT OBSERVABLE
Verdict: SUPPORTED | SUPPORTED_WITH_CHANGES | BLOCKING_CONCERN
Assertions:
- <assertion>: PASS | FAIL | NOT OBSERVABLE
Planning Impact:
- <none / concrete authority change>
```

模型的 `SUPPORTED` 也不自动等于 Human semantic grading PASS；如果输出遗漏关键 assertion、依赖未提供事实、读取了不允许的上下文或提出 scope expansion，应按实际证据评分。

## Experiment retention decision

### RETAIN

仅当多个真实场景持续证明：

- 高能力 review 找到普通 review 明显遗漏的真实高代价问题，或显著提高关键决策信心；
- 场景边界与 token / quota 成本可控；
- Evidence 可重复且不会产生第二套 Authority。

### ADJUST — CURRENT

当前 AR-02 表明机制具有有限增量价值，但不足以成为常规 Gate，因此只保留为按需 bounded second opinion。

### DROP

当后续证据显示：

- 结果与普通 review 基本重复；
- 高能力模型主要制造 speculative complexity；
- 成本 / quota 与收益不匹配；
- 运行环境无法可靠支撑所需 Evidence Claim；
- 维护 eval corpus 本身开始超过实际工程价值。
