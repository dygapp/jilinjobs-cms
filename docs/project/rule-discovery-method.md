# Consumer-local Rule Discovery 方法扩展

## 1. 状态与来源

本文件是 `jilinjobs-cms` Consumer-local Development Method 的 Rule Discovery 扩展。它只拥有 Rule / Skill 发现及显式 upstream upgrade 后的本地运行语义，不改变产品 Requirement、Specification、四层 CMS Architecture、Work lifecycle 或 GitHub native state。

- Previous Evaluated Baseline：`dygapp/agentic-dev@8e7e94eff62b958b2044407cf6d85de3dde48ee9`
- Current Evaluated Baseline：`dygapp/agentic-dev@ed1a4446f0430890e7ad39673ac9c2e341e6a829`
- 本次 exact upstream transition 为 `8e7e94ef... -> ed1a4446...`，GitHub compare = ahead 8 / behind 0。
- 本次与 Rule Discovery 直接相关的 reusable delta 是 direct-responsibility runtime checkpoint：首次有副作用动作前必须完成 task-level discovery；direct responsibility 或关键 task facts 实质变化后，在下一次有副作用动作前重新发现；CI lint / deterministic test / 固定 smoke 不能替代 ordinary runtime invocation。
- `dygapp/agentic-dev@3e0b2f5a29caeb344da79f8c96ebffbeb5c2b0cb` 继续只作为 historical Foundation projection，不是当前 evaluated frontier。
- upstream Project Roadmap、Issue / PR state、Research / Eval 与 self-adoption instance 不传播为 Consumer Authority；只投射本 Consumer 明确接受的 reusable capability。

本次升级的逐项 disposition 与验证证据见：

`docs/project/archive/agentic-dev-ed1a-baseline-upgrade-evidence.md`

该文件是 upgrade-only historical evidence，普通运行不默认读取。

## 2. Consumer-local ownership

当前职责如下：

- `AGENTS.md`：稳定 Bootstrap、Rule Discovery ordinary-runtime checkpoint、Repository / Human Authority；
- `docs/project/development-method.md`：生命周期、Method selection、Planning / Readiness / Execute / Converge 核心方法与 evaluated baseline；
- 本文件：Rule / Skill discovery 运行扩展、Rule granularity、responsibility-transition checkpoint 与 explicit-upgrade decoupling；
- `skills/*/SKILL.md`：Consumer 已采用九项独立执行能力的 Consumer-local procedure；
- `docs/rules/**`：可发现横切 Rule；每条 Rule 的 metadata 与规范正文同文件；
- `tools/rule-discovery/rule_discovery.py`：确定性 metadata prefilter 与 lint；
- `.github/workflows/rule-discovery.yml`：Rule Discovery corpus / tool contract 的 Consumer-local CI。

Rule 文件自身是该 Rule 的唯一规范正文 owner。路径、测试、Workflow、Issue、Evidence 文件都不得成为第二份 Rule 语义或人工同步路由表。

## 3. Rule 粒度

Rule 默认面向**一个可独立发现的具体任务或责任所需的有界规范语义集合**，不是“一条 assertion = 一个文件”。

同一任务中的 policy 在以下情况应优先聚合：

- task facts 相同或高度重叠；
- ordinary runtime 中通常共同发现、共同消费；
- Agent 完成该责任时通常需要一起知道；
- 合并后仍可整体判断“是否值得加载 / 是否适用”；
- 没有把明显不同的 technology、artifact、risk、lifecycle 或 semantic owner 强行绑在一起。

只有独立 discovery 具有真实收益时才继续拆分，例如独立 metadata 能稳定减少无关加载或错误激活，或确有不同 technology / artifact / risk / lifecycle / semantic owner。仅仅能够把两条 policy 分别表述，不构成拆分理由。

Agent Skills Specification 的 Progressive Disclosure `<5000 tokens / <500 lines` 只作为单文件上限复核参照，不是 Rule 目标大小或机械拆分阈值。若 Rule 接近该上限，应先检查是否混入多个独立任务、长教程或研究材料。

## 4. Technology Rule 信息架构

目录只服务人类维护，不参与 runtime matching。当前 Consumer 有真实 Vue Rule，因此使用：

```text
docs/rules/technology/
└── vue/
```

当前没有独立 Consumer-local TypeScript Rule，因此不创建空 `technology/typescript/`。后续只有真实规则存在时才建立新的技术子目录。

Rule Discovery 继续只依赖 Rule Front Matter 与 task signals；目录名、目录层级和文件名都不是 routing signal。

## 5. bounded task signals

ordinary runtime 只从当前 Consumer direct responsibility 与 repository 可观察事实提取五个维度：

```json
{
  "phases": [],
  "activities": [],
  "technologies": [],
  "artifacts": [],
  "risks": []
}
```

每个维度使用三态：

- 非空数组：`known`，只填写当前事实能够稳定支持的 canonical token；
- `[]`：`known-empty`，明确知道该维度没有 token；
- `null`：`unknown / unsafe-to-canonicalize`，不得为了缩小候选而猜 token。

每个维度最多 6 个 token。不得把预期 Rule 名称、候选 id、推荐答案、近义词堆叠或从未命中 Rule 反向学习到的 metadata 填入 signals。

若当前 selected Method 定义了稳定 phase identity，`phases` 只能消费该 Method canonical owner 定义的 token；无法安全确定时使用 `null`。不同 Method 不自动共享 phase token。

## 6. Rule Discovery ordinary runtime

执行横切规则发现时：

```text
Consumer current responsibility / facts
→ bounded task signals
→ Consumer-local Rule Discovery
→ candidate {id,path} locators
→ 只读取 candidate Rule bodies
→ LLM semantic applicability confirmation
→ 应用真正适用 Rule
→ continue current responsibility
```

必须遵守：

1. `tools/rule-discovery/rule_discovery.py` 只读取 `docs/rules/**` Front Matter 做确定性初筛；
2. `candidates[]` 是 ordinary runtime 获得 Rule locator 的唯一入口；
3. ordinary runtime 禁止通过 `rg --files`、`find`、目录树、IDE tree、脚本输出或其他方式枚举未命中 `docs/rules/**` locator；
4. 未命中 Rule 的 locator、metadata、body 不进入模型上下文，也不得用于 false-negative calibration；
5. discovery 只返回 `{id,path}`；不返回全量 metadata、未命中清单、score、摘要或推荐答案；
6. metadata、duplicate id、signals 或 root 异常时 fail closed，不自动回到 upstream；
7. Tool 负责候选初筛，LLM 仍负责 candidate body 的最终语义适用性确认；
8. 当前 direct responsibility 建立后，在执行该责任的**首个有副作用动作前**必须完成一次 task-level discovery；为恢复事实而进行的只读读取可以先于 discovery；
9. direct responsibility 切换，或 phase / activity / technology / artifact / risk 等关键事实发生足以改变候选集合的实质变化时，旧 candidate set 不再作为新责任的充分依据，必须在下一次有副作用动作前重新构造 signals 并执行 discovery；
10. CI 中的 Rule Discovery lint、deterministic test 或固定 smoke scenario 只证明 tool / corpus contract；它们不携带当前 Agent 的实时 task signals，因此**不能替代 ordinary runtime invocation**；
11. 旧 candidate set 不跨职责永久有效；`status=ok` 但候选为空，也不得枚举未命中 Rule 做反向校准。

这里的 checkpoint 只规定“何时必须重新发现”。task signals、matching、locator-only 输出和最终 semantic applicability 仍由本文件其他条款统一定义，不建立第二套路由语义。

## 7. Skill discovery

Skill 与 Rule 分离：

- Skill 是稳定、可独立执行的输入→过程→输出闭环；
- Rule 是执行过程中条件性适用的约束 / 默认 / 不变量 / completion requirement；
- `skills/*/SKILL.md` 使用原生 Skill metadata，并由 Consumer-local 文件持有 procedure；
- Rule Discovery 不返回 Skill，也不把 supporting Rule 提升为 Skill。

当前物理 Skill 只是 `docs/project/development-method.md` 已经采用的九项能力的 Consumer-local 投射，不因物理化扩大原有 stage 或 Execute Authority。项目级 Software Project Clarification 当前没有独立 Skill；不得仅因新增 Method stage 名称就预建 `requirement-analysis`、`architecture-framing` 等 Skill。

## 8. 不建立中心同步资产

本 Consumer 明确不建立，也不把以下内容作为 ordinary runtime 依赖：

- Reviewed Discovery Map；
- Activation Manifest；
- Runtime Catalog；
- `rule-index`；
- `rule → signals` 人工映射；
- 需要在 Rule 正文变更时同步维护的第二份 routing table。

测试可以对具体场景断言候选结果；这属于可执行验证，不是 ordinary runtime 路由资产。

## 9. explicit upgrade 与 ordinary-runtime 解耦

一次 baseline upgrade 完成后，普通运行默认：

```text
upstream access = 0
```

`agentic-dev` 后续 branch、Roadmap、Issue、PR、master 或“latest baseline”变化不会自动改变 Consumer。只有项目负责人或 Consumer Authority 明确启动下一次 baseline upgrade 时，才重新读取一个固定 upstream commit，做 exact compare、逐项 disposition、Consumer-local projection 与 targeted verification。

本地 discovery 缺失、stale、ambiguity 或 zero-candidate 只触发 Consumer-local fail closed，不自动访问 upstream。

## 10. Rule evolution

Rule 后续自然新增、拆分、合并或修改时，先判断真实任务 / 责任边界与独立 discovery 价值，只维护真实 semantic owner、必要 lint 与 deterministic tests。不得为了匹配 upstream inventory、历史指标或目录整齐制造空目录、micro-rules 或重复 Rule。
