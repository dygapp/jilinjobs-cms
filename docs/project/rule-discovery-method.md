# V4 Rule Discovery Consumer-local 方法扩展

## 1. 状态与来源

本文件是 `jilinjobs-cms` Consumer-local Development Method 的 **V4 Rule Discovery 扩展**。它只拥有 Rule / Skill 发现与 V4 adoption 的本地运行语义，不改变产品 Requirement、Specification、四层 CMS Architecture、Work lifecycle 或 GitHub native state。

- 既有 `Current Evaluated Baseline`：`dygapp/agentic-dev@1c8cdfea9ecf23ef33ffab20eec3c93679fd4578`
- V4-08 本次显式采用的精确候选：`dygapp/agentic-dev@3e0b2f5a29caeb344da79f8c96ebffbeb5c2b0cb`
- 上游 `3e0b2f5a...` 是 V4-07 已完成 Token Scaling Gate 的精确 runtime candidate。
- 本次采用不进入 `agentic-dev` V4-09，也不把 upstream Roadmap / Issue / PR 状态变成 Consumer Authority。
- 在 `agentic-dev` 对 V4-08 独立裁决并由后续显式 baseline replacement lifecycle 处理前，旧 `Current Evaluated Baseline` 字段不被本次实验机械改写；本文件记录的是 Consumer 已采用并正在验证的 V4 Foundation candidate。

## 2. Consumer-local ownership

采用后职责如下：

- `AGENTS.md`：稳定 Bootstrap、Rule Discovery ordinary-runtime contract、Repository / Human Authority；
- `docs/project/development-method.md`：既有生命周期、Planning / Readiness / Execute / Converge 核心方法；
- 本文件：V4 Rule / Skill discovery 运行扩展与 explicit-upgrade decoupling；
- `skills/*/SKILL.md`：Consumer 已有九项独立执行能力的物理化 Consumer-local procedure；
- `docs/rules/**`：可独立发现的横切 Rule；每条 Rule 的 metadata 与规范正文同文件；
- `tools/rule-discovery/rule_discovery.py`：确定性 metadata prefilter 与 lint；
- `.github/workflows/rule-discovery.yml`：Rule Discovery contract 的 Consumer-local CI 证据。

Rule 文件自身是该 Rule 的唯一规范正文 owner。路径、测试、Workflow、Issue、Evidence 文件都不得成为第二份 Rule 语义或人工同步路由表。

## 3. bounded task signals

ordinary runtime 只从当前 Consumer task / repository 可观察事实提取五个维度：

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

## 4. Rule Discovery ordinary runtime

执行横切规则发现时：

```text
Consumer current facts
→ bounded task signals
→ Consumer-local Rule Discovery
→ candidate {id,path} locators
→ 只读取 candidate Rule bodies
→ LLM semantic applicability confirmation
→ 应用真正适用 Rule
```

必须遵守：

1. `tools/rule-discovery/rule_discovery.py` 只读取 `docs/rules/**` Front Matter 做确定性初筛；
2. `candidates[]` 是 ordinary runtime 获得 Rule locator 的唯一入口；
3. ordinary runtime 禁止通过 `rg --files`、`find`、目录树、IDE tree、脚本输出或其他方式枚举未命中 `docs/rules/**` locator；
4. 未命中 Rule 的 locator、metadata、body 不进入模型上下文，也不得用于 false-negative calibration；
5. discovery 只返回 `{id,path}`；不返回全量 metadata、未命中清单、score、摘要或推荐答案；
6. metadata / duplicate id / signals / root 异常时 fail closed，不自动回到 upstream；
7. Tool 负责候选初筛，LLM 仍负责 candidate body 的最终语义适用性确认。

目录分类只用于人类信息架构，不承担隐藏 routing 语义。

## 5. Skill discovery

Skill 与 Rule 分离：

- Skill 是稳定、可独立执行的输入→过程→输出闭环；
- Rule 是执行过程中条件性适用的约束 / 默认 / 不变量 / completion requirement；
- `skills/*/SKILL.md` 使用原生 Skill metadata，并由 Consumer-local 文件持有 procedure；
- Rule Discovery 不返回 Skill，也不把 supporting Rule 提升为 Skill。

当前物理 Skill 只是 `docs/project/development-method.md` 已经采用的九项能力的 Consumer-local 投射，不因物理化扩大原有 stage 或 Execute Authority。

## 6. 不建立中心同步资产

本 Consumer 明确不建立，也不把以下内容作为 ordinary runtime 依赖：

- Reviewed Discovery Map；
- Activation Manifest；
- Runtime Catalog；
- `rule-index`；
- `rule → signals` 人工映射；
- 需要在 Rule 正文变更时同步维护的第二份 routing table。

测试可以对具体场景断言候选结果；这属于可执行验证，不是 ordinary runtime 路由资产。

## 7. explicit upgrade 与 ordinary-runtime 解耦

本次 adoption 完成后，普通运行默认：

```text
upstream access = 0
```

`agentic-dev` 后续 branch、Roadmap、Issue、PR、master 或“latest baseline”变化不会自动改变 Consumer。只有项目负责人或 Consumer Authority 明确启动下一次 baseline upgrade 时，才重新读取一个固定 upstream commit，做 exact compare、逐项 disposition、Consumer-local projection 与 targeted verification。

本地 discovery 缺失、stale、ambiguity 或 zero-candidate 只触发 Consumer-local fail closed，不自动访问 upstream。

## 8. Evidence 与 Rule evolution

V4-08 Consumer-side Evidence 记录在：

`docs/project/agentic-dev-v4-08-consumer-validation-evidence.md`

该文件只承担 upgrade / experiment evidence，不是 ordinary-runtime Rule Map。

Rule 后续自然新增或修改时，只维护该 Rule 自身以及必要 lint / deterministic tests。若没有真实自然 Rule evolution，不为了实验制造规则变化；Evidence 必须保留“尚未自然发生”的真实状态，由 upstream V4-08 独立判断是否构成 Gate blocker。
