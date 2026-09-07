# Consumer-local Architecture Review Evals

## Status

本目录是 Issue #92 / Phase 0 引入的**实验性**高能力架构评审机制。

它当前不是 `jilinjobs-cms` 的长期 Development Method，也不新增 Method Stage、Skill、Readiness Gate 或 Integration Gate。是否保留，取决于本轮实际收益；本轮结束后结论可以是：

```text
RETAIN
ADJUST
DROP
```

如果该机制在 Consumer 中取得明确的跨项目复用价值，再把证据提交到 `dygapp/agentic-dev` Issue；本 Consumer 不直接修改 `agentic-dev` 仓库文件或 Workflow。

## Purpose

本实验只回答一个问题：

> 对少数高代价、返工成本高的架构判断，是否值得使用一个受限 Fresh Context 的高能力模型进行第二视角挑战评审，并把结果保存为可审计 Review Evidence？

它不回答：

- 当前 Requirement 是否 Ready；
- Candidate Execution Unit 是否 Readiness PASS；
- PR 是否可以自动合并；
- GPT-6 是否拥有 Repository Authority；
- 是否应该把所有普通开发工作切换到昂贵模型。

## Corpus

当前语料：

```text
evals/architecture/pre-e1e3-convergence-review.json
```

场景：

- `AR-01` — Documentation Authority Architecture；
- `AR-02` — Backend Application / Gradle Module Boundary；
- `AR-03` — Generic Historical Migration Boundary。

首轮策略：

1. 先使用较低成本模型 dry-run，确认 prompt、context boundary 和 assertions 可用；
2. 高能力模型首轮优先只运行 `AR-02`、`AR-03`；
3. `AR-01` 只有在普通 review 出现真实争议时再升级；
4. 不运行 `--all` 式大规模高能力评审。

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

运行器要求 tracked worktree clean，并记录：

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

### Lower-cost dry-run

模型名以当前 Codex 实际可用 model catalog 为准，例如：

```bash
python3 evals/run_architecture_review.py \
  --scenario AR-02 \
  --scenario AR-03 \
  --model <lower-cost-review-model> \
  --reasoning-effort medium
```

### GPT-6 targeted review

GPT-6 运行必须显式 opt-in，防止误触发高成本场景：

```bash
python3 evals/run_architecture_review.py \
  --scenario AR-02 \
  --scenario AR-03 \
  --model gpt-6-astra \
  --reasoning-effort high \
  --allow-high-capability
```

运行器通过独立 CLI 进程请求 model，并使用 Codex config override 请求 `model_reasoning_effort`；不要使用 resumed session 或 parent/subagent inheritance 作为本实验的模型隔离机制。

如果当前 Codex CLI 不支持请求的 model / effort，或运行 trace 暴露实际值与请求值不一致，该次结果不能被当成目标模型 Evidence，应先分类为 Eval Infrastructure / Runtime Availability 问题。

## Evidence location

原始运行结果写入：

```text
evals/results/architecture/<scenario>/
```

这些结果是 ephemeral execution evidence，不默认提交 Git。

Phase 0 的长期 Promotion 方式：

- 在 Planning PR / Issue #92 comment 中记录 exact Planning Head；
- 记录 scenario、model、effort、Codex version、context digest；
- 人工逐条记录 assertions 的 PASS / FAIL / NOT OBSERVABLE；
- 摘要 Blocking Findings 与实际 Planning revision；
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

Phase 0 / 本轮路线结束前评估：

### RETAIN

仅当：

- 高能力 review 找到普通 review 明显遗漏的真实高代价问题，或显著提高关键决策信心；
- 场景边界与 token / quota 成本可控；
- Evidence 可重复且不会产生第二套 Authority。

### ADJUST

当机制有价值，但需要缩小 context、改变 prompt、减少场景或修改 evidence lifecycle。

### DROP

当：

- 结果与普通 review 基本重复；
- 高能力模型主要制造 speculative complexity；
- 成本 / quota 与收益不匹配；
- 运行环境无法可靠证明目标模型 / effort；
- 维护 eval corpus 本身开始超过实际工程价值。
