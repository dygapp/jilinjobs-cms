# Consumer-local Architecture Review Evals

## Status

本目录是 Issue #92 / Phase 0 引入的**实验性**架构评审机制。

它不是 `jilinjobs-cms` 的常规 Development Method，也不新增 Method Stage、Skill、Readiness Gate 或 Integration Gate。

当前实验结论仍为：

```text
ADJUST
```

经过 AR-02 与 AR-04 两个 paired scenario 后，当前含义进一步收敛为：

- 不把 GPT-6 / Astra review 变成普通开发默认步骤；
- 对高返工成本架构问题，如确有必要，可使用 bounded Fresh Context + hidden assertions 保存可审计 Review Evidence；
- **先运行 lower-cost capable model**；只有第一轮仍存在 unresolved ambiguity、冲突证据，或明确需要额外独立 second opinion 时，才升级 Astra；
- 不要求每个 Execution Unit 执行高能力模型评审；
- 不把模型输出直接提升为 Repository Authority、Readiness 或 Integration Gate；
- AR-01 / AR-03 保持 dormant，需要真实争议时再启用。

AR-04 的 retrospective blind test 已证明：已知的 Content Migration sequencing flaw 并不需要 Astra 才能发现；`gpt-5.6-sol` / medium 与 `gpt-6-astra` / high 均独立识别核心缺口并通过全部隐藏 assertions。因此当前证据更支持 **Sol-first / selective escalation**，而不是 high-capability-by-default。

## Purpose

本实验只回答一个问题：

> 对少数高代价、返工成本高的架构判断，是否值得使用受限 Fresh Context 的独立模型评审，并把结果保存为可审计 Review Evidence？

它不回答：

- 当前 Requirement 是否 Ready；
- Candidate Execution Unit 是否 Readiness PASS；
- PR 是否可以自动合并；
- 某个模型是否拥有 Repository Authority；
- 是否应该把普通开发工作整体切换到昂贵模型。

## Corpus

常规 bounded review corpus：

```text
evals/architecture/pre-e1e3-convergence-review.json
```

包含：

- `AR-01` — Documentation Authority Architecture；
- `AR-02` — Backend Application / Gradle Module Boundary；
- `AR-03` — Generic Historical Migration Boundary。

Retrospective blind discovery corpus：

```text
evals/architecture/ar04-migration-sequencing-review.json
```

包含：

- `AR-04` — Content Migration Extraction Sequencing Blind Review。

AR-04 使用冻结候选方案和缺陷发现前仍保持一致的 Backend build/application/migration evidence；当前 Phase 0 修订结论、AR-02 输出、provenance、assertions 与历史结果均不进入 Runtime `context_paths`。

## Paired evidence

### AR-02 — Backend Application / Gradle Module Boundary

- Planning Head：`52d8d59094eb5f02a2780ccf131363adac08a1dc`
- Codex CLI：`0.153.4`
- Context digest：`sha256:de1504125236bcea7154602ac60ed7b627a06eaa818302191bbb477e0d7abec1`
- Prompt digest：`sha256:bf8c2f357102652fea4367f7ead18c42b6e1963407ad8f341c9ce0bf8b88c5d9`
- lower-cost request：`gpt-5.6-sol` / medium
- high-capability request：`gpt-6-astra` / high
- Human Semantic Verdict：两次均 `SUPPORTED_WITH_CHANGES`
- assertions：两次均全部 PASS

AR-02 中 Astra 提供了有限的边界细节增量，但没有推翻 lower-cost review 的总体判断。

### AR-04 — Content Migration Extraction Sequencing Blind Review

- Eval Head：`809717cb1772f8aa2736d151f409e92aeaa4c1a7`
- Codex CLI：`0.153.4`
- Context digest：`sha256:764d57420d40a8ac0d8e4543157db6466a6be179f76f5feca437034a30f2a27a`
- Prompt digest：`sha256:25af530799b8a842641dc81989c9190bd79ef60361542080ba991d078ace0b7e`
- paired archive SHA-256：`8470e1265533e3bd14942ae24a5d9da1a80fa3c4d740ab41be5b44137c0f2441`
- lower-cost request：`gpt-5.6-sol` / medium
- high-capability request：`gpt-6-astra` / high
- 两次 model verdict：`BLOCKING_CONCERN`
- Human discovery grading：两次均 **DETECTED**
- hidden assertions：两次均 **8/8 PASS**

核心 blind discovery：Stage 1 `contentMigration -> main` 不能证明独立 Spring application/composition boundary；必须先冻结 `cms-server`、`content-migration` 与 shared CMS capability 的依赖 / composition 边界，再选择 source set 或 multi-project 等 Gradle 实现。

Reported usage：

| Run | input | cached input | output | reasoning output |
| --- | ---: | ---: | ---: | ---: |
| Sol / medium | 95,654 | 60,800 | 3,534 | 1,213 |
| Astra / high | 119,153 | 77,952 | 2,394 | 264 |

Astra 在该 blind scenario 中没有发现 Sol 漏掉的 blocking flaw。其有价值增量主要是更紧凑地区分 application/composition boundary 与 Gradle layout，并补充 product/JAR content contract、legacy mapping/schema ownership 等表述；Sol 对 migration ordering、partial-success semantics、DB transaction 与 file/resource side effects 的具体风险至少同样充分。

完整 assertion grading、usage 与 Planning Impact 见 Issue #92 的对应 Evidence comments。长期项目 Authority 不依赖本地 results 目录。

## Model observability limitation

当前 Codex JSONL 没有暴露可独立确认的 actual runtime model / reasoning effort 字段。

因此所有 Evidence 必须表述为：

```text
requested model / requested effort
```

不得声称 runtime trace 已二次证明实际模型。

## Isolation

主运行器：

```text
evals/run_architecture_review.py
```

AR-04 wrapper：

```text
evals/run_ar04_review.py
```

每个 scenario：

- 独立 `codex exec --ephemeral --json`；
- 在 Repository 外部临时目录执行；
- 只复制 corpus 声明的 `context_paths`；
- Runtime 看不到 corpus 文件本身，因此看不到 `assertions` / `discovery_grading`；
- Runtime 不读取 provenance 或历史结果；
- Runtime 使用 read-only sandbox；
- 题面禁止 Web research、仓库外搜索和文件修改。

如果 Runtime 实际获得 assertions / expected behavior / 历史结果，Run 判为：

```text
INFRASTRUCTURE_INVALID / CONTAMINATED
```

不能作为 Review Evidence。

## Exact-head evidence

运行器要求 Git worktree 对 tracked / untracked 变化都 clean；`.gitignore` 排除的 ephemeral results、artifacts 与 Python cache 不影响该 Gate。

运行器记录：

- `git rev-parse HEAD`；
- Codex CLI version；
- requested model / reasoning effort；
- scenario id；
- context file list 与逐文件 SHA-256；
- 聚合 context digest；
- prompt digest；
- exact command prefix；
- JSONL stdout；
- stderr；
- return code；
- JSONL 中可观察到的 model / effort hints（best effort）。

## Running policy

先列出场景，不调用模型：

```bash
python3 evals/run_architecture_review.py --list
```

普通 bounded review 默认先使用 lower-cost capable model：

```bash
python3 evals/run_architecture_review.py \
  --scenario <scenario> \
  --model <lower-cost-review-model> \
  --reasoning-effort medium
```

只有第一轮仍存在真实 unresolved question，或明确需要第二个独立意见时，才显式 opt-in Astra：

```bash
python3 evals/run_architecture_review.py \
  --scenario <scenario> \
  --model gpt-6-astra \
  --reasoning-effort high \
  --allow-high-capability
```

不运行 `--all` 式高能力评审，也不为了“完成 corpus”机械运行未触发场景。

## Evidence location

原始结果写入：

```text
evals/results/architecture/<scenario>/
```

本地证据 archive 建议写入：

```text
evals/artifacts/
```

两者均为 ephemeral execution evidence，不默认提交 Git。

长期 Promotion 方式：

- 在对应 Planning PR / Issue comment 中记录 exact Head；
- 记录 scenario、requested model / effort、Codex version、context / prompt digest；
- 人工逐条记录 assertions 的 PASS / FAIL / NOT OBSERVABLE；
- 对 discovery scenario 记录 `DETECTED / PARTIALLY_DETECTED / MISSED`；
- 摘要实际 Planning Impact；
- 明确 actual runtime model / effort 是否 independently observable。

## Semantic grading

**Process exit code `0` 只说明 Codex 进程结束，不表示 Review PASS。**

必须人工读取最终输出和必要 JSONL trace，按隐藏 assertions 评分。

模型输出中的 `SUPPORTED` / `BLOCKING_CONCERN` 也不自动等于 Human semantic grading；如果遗漏关键 assertion、依赖未提供事实、读取不允许的上下文或产生 scope expansion，应按实际 Evidence 判定。

## Experiment retention decision

### RETAIN

只有多个真实场景持续证明高能力 review 找到 lower-cost review 明显遗漏的高代价问题，或显著提高关键决策信心，同时成本与 Evidence lifecycle 可控，才考虑把高能力 second opinion 提升为更稳定的推荐模式。

### ADJUST — CURRENT

AR-02 显示有限增量价值；AR-04 blind test 则显示 lower-cost Sol/medium 已能独立发现已知高代价 sequencing flaw，Astra 没有新增 blocking discovery。

因此当前策略是：

```text
bounded eval when justified
→ lower-cost capable review first
→ escalate only if unresolved / conflicting / deliberate second opinion
```

### DROP

当后续证据显示结果持续重复、昂贵模型主要制造 speculative complexity、成本收益不匹配，或维护 eval corpus 本身超过工程价值时，删除该实验模式而不是把它固化为项目常规方法。
