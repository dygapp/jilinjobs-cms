# AGENTS.md

## 项目定位

`jilinjobs-cms` 是吉林省智慧就业云平台中“信息发布与网站服务”相关能力的独立 Consumer Repository。

本文件只维护稳定的 Repository Governance、Authority Boundary、Knowledge Boundary、Fresh Context / Rule Discovery 启动约束、Human Escalation 与 GitHub operation rules，不缓存某个 Execution Unit 的高频 Current Gate。产品范围、Requirement、Specification、Architecture、Work lifecycle 与 GitHub native state 继续由各自 Consumer-local owner 持有；不得从其他项目、其他会话、个人记忆、旧路线或实现便利性中推导、补充或扩大产品范围。

## Current State ownership 与 locator

Current State 按职责分层，不建立第二套 Current State truth：

- `AGENTS.md`：稳定 Repository rules / Authority Boundary；
- 根 `README.md`：稳定项目入口与范围；
- `docs/README.md`：Documentation Authority Map / 文档 Local Discovery Entry；
- `docs/work/README.md`：Ready / active Execution Unit 的 Entry / Exit / fail-closed lifecycle contract；
- `docs/work/current/README.md`：Repository-owned **Current Execution Lifecycle Locator**；
- `docs/project/project-roadmap.md`：durable milestones、长期边界与 Planning directions；
- GitHub PR / Branch / Actions：各自原生瞬时事实与 Current Evidence；
- controlling Issue：Planning / decision / evidence timeline，不因历史 comment 出现 `PENDING` / `PASS` / `Current Evidence` 就永久拥有 Execute Gate。

Bootstrap / Roadmap surface 不并行维护 `Current Ready Execution Unit`、Readiness `PENDING/PASS`、exact implementation Head 或最近 Actions 等高频 Execute Gate 真值。Fresh Context 在使用 `docs/work/current/README.md` 的 `NONE` 作为安全停止条件前，必须核对当前任务相关 Open execution PR / branch 与必要 Current Evidence；`NONE` 只表示没有 Ready / active Execution Unit，不表示没有 Planning Candidate 或新的用户指定目标。

若 Work locator、active artifact、Readiness Evidence、Open execution work 或关键 GitHub Current Evidence 缺失、冲突或无法消歧，必须 **fail closed**：不得进入、继续或继承 Execute Authority，先恢复一致的 Current State。

## Fresh Context 与本地发现

普通运行按以下顺序恢复：

1. 读取本文件与根 `README.md`；
2. 读取 `docs/README.md`，按当前目标选择最小正确的文档 Authority / owner；
3. state-only 请求读取 `docs/work/current/README.md` 并协调当前 Open execution PR / branch 与必要 GitHub Current Evidence；若已无歧义得到所需状态则停止；
4. Planning / durable route 按需读取 Project Roadmap；方法 / execution routing 按需读取 `docs/project/development-method.md` 与 `docs/project/rule-discovery-method.md`；
5. execution 时只加载当前 primary Authority、对应 `skills/*/SKILL.md` 与当前任务实际触发的 Rule candidates；
6. Rule candidates 只能通过 Consumer-local `tools/rule-discovery/rule_discovery.py` 获得；
7. 当前任务直接相关的 Requirement / Specification / Technical / Architecture / Work / GitHub Current Evidence 按需读取。

`docs/**/archive/**` 与 `docs/work/archive/**` 默认只承担 traceability / historical evidence，不参与普通 Fresh Context Current Authority 恢复，除非当前 Authority、追溯 / 审计或 Verification 明确要求。

### Rule Discovery ordinary-runtime contract

Consumer 当前采用 V4 分布式 Rule Discovery：

```text
Consumer current task / repository facts
→ bounded task signals
→ Consumer-local Rule Discovery
→ candidate {id,path} locators
→ 只读取 candidate Rule bodies
→ semantic applicability confirmation
→ 应用真正适用 Rule
```

必须遵守：

- Rule metadata 与规范正文同文件维护，当前 Rule owner 为 `docs/rules/**`；
- task signals 五维为 `phases / activities / technologies / artifacts / risks`；
- 每个维度：非空数组 = known；`[]` = known-empty；`null` = unknown / unsafe-to-canonicalize；
- 每维最多 6 个 canonical token；不得堆叠近义词、猜 taxonomy、把预期 Rule id / 推荐答案作为 signal；
- Rule Discovery 的 `candidates[]` 是 ordinary runtime 获得 Rule locator 的**唯一入口**；
- ordinary runtime 禁止通过 `rg --files`、`find`、目录树、IDE tree、脚本输出或其他方式枚举未命中 `docs/rules/**` locator；
- 未命中 Rule 的 locator / metadata / body 不进入模型上下文，也不得在 zero-candidate 后读取它们做 calibration；
- discovery 只做 deterministic metadata prefilter；LLM 只对返回候选正文做最终 semantic applicability confirmation；
- metadata、duplicate-id、invalid-state、invalid signals 或 Rule root 异常时 fail closed；
- local discovery 失败不得自动访问 `dygapp/agentic-dev` 修补普通运行。

当前 Consumer **不维护** Reviewed Discovery Map、Activation Manifest、Runtime Catalog、`rule-index`、`rule → signals` 人工映射或其他需要与 Rule 正文同步维护的中心路由资产。测试可以对具体场景断言候选集合；测试不是 ordinary-runtime routing source。

Skill 与 Rule 分离。`skills/*/SKILL.md` 持有稳定独立执行 procedure；Rule Discovery 不发现 Skill，也不得把 supporting Rule 当作 Skill。物理 Skill 只投射 `docs/project/development-method.md` 已采用的 Consumer 能力，不因为文件存在自动扩大 Planning / Readiness / Execute Authority。

详细 V4 本地运行契约见 `docs/project/rule-discovery-method.md`。

## 文档语言与术语表达

本项目所有面向人的 Current / Partially Current 文档强制以中文为主语言，采用“中文主述、必要英文精确锚定”：

- 标题、状态说明、背景、需求、规格、技术方案、验证说明、结论等叙述性内容使用中文；
- Skill 名称、代码标识符、类 / 方法 / 字段名、文件路径、命令、API、URL、协议 / 标准、枚举值、SHA 与固定技术专名保持原生形式；
- `READY`、`COMPLETED`、`CURRENT`、`SUPERSEDED` 等精确状态可以保留，但由中文正文解释；
- Current 文档不得形成纯英文主体；
- `SUPERSEDED` / `HISTORICAL_EVIDENCE` 以证据保真优先；重新晋升为 Current Authority 前再满足当前语言规范；
- 语言治理不得改变 Product Goal、Scope、Business Boundary、User-visible Behavior、Architecture Decision 或技术契约。

Current 文档语言与引用完整性继续由 `scripts/verify-docs-governance.mjs` 和对应 GitHub Actions 检查。

## 仓库权威（Repository Authority）

发生冲突时，先判断真正 semantic owner，再在同一责任内按以下顺序处理：

1. `AGENTS.md`：Repository Governance、Authority Boundary 与工作规则；
2. 根 `README.md`：当前项目目标、当前迭代范围与稳定项目入口；
3. `docs/requirements/information-publishing.md`：当前迭代范围内行为的详细权威业务需求；
4. 后续依据上述权威正式形成的 Specification、Architecture、Decision、Project Roadmap、Consumer-local Development Method 等 Artifact；
5. Code 与 Tests：证明当前实现状态，不得反向发明产品需求；
6. Conversation、临时计划和 Agent reasoning：不构成项目权威。

对于 Current Execution Lifecycle、GitHub native status、Requirement、Specification、Architecture、Method、Rule 等不同职责，先读取各自 owner，再处理真正冲突；不得把优先级表机械解释成一个 surface 可以替代其他 owner。

`docs/requirements/information-publishing.md` 中指向当前 Consumer Repository 不存在的 upstream 文档，只保留 provenance，不构成 Consumer Authority。根 `README.md` 明确排除的范围不得仅因详细需求曾出现就自行实现。

## 知识边界

本项目事实只允许来自：

- 当前 Consumer Repository；
- 当前 Runtime / Repository 可直接观察到的状态和证据；
- 已被本仓库有意纳入的权威需求输入；
- 在**显式 baseline adoption / upgrade / Consumer validation** 中固定到精确 commit 的 `dygapp/agentic-dev`，且它只提供 Method、Skill、Rule、Guide / supporting architecture 知识。

`agentic-dev` 决定可复用的“如何工作”，不决定 `jilinjobs-cms` 的产品事实。upstream Project Roadmap、Issue / PR Gate、Research、Eval、self-adoption instance 不自动进入 Consumer。

ordinary runtime 默认：

```text
upstream access = 0
```

上游后续 commit、branch、Roadmap、Issue、PR 或“latest baseline”不会自动改变 Consumer。只有项目负责人或 Consumer Authority 明确启动下一次 baseline upgrade 时，才重新读取一个固定 upstream commit 并逐项 disposition。本地 stale / missing / ambiguity 只触发 local fail closed，不单独授权 upstream re-entry。

## 开发方法与 V4 adoption 状态

方法来源 Repository：`dygapp/agentic-dev`。

- Previous Evaluated Baseline：`2fe193035c629f6b8805fd473bd322f70fe6e172`
- Current Evaluated Baseline：`1c8cdfea9ecf23ef33ffab20eec3c93679fd4578`
- Capability Milestone：`baseline-2026-09-04-engineering-capability@5be2e6aad29b2be6b8535b3690daf3533ee22a46`
- **V4-08 Adopted Foundation Candidate**：`3e0b2f5a29caeb344da79f8c96ebffbeb5c2b0cb`

`Current Evaluated Baseline` 与 `V4-08 Adopted Foundation Candidate` 暂时分离是有意行为：本次 Consumer 已显式采用并验证 V4 Foundation candidate，但 `agentic-dev` Issue #122 仍需独立裁决 V4-08，且本工作**不得提前进入 V4-09 Baseline Replacement**。因此不得把 upstream V4 project-state commit 或浮动 PR Head 机械写成 Consumer 已关闭的 evaluated frontier。

V4 candidate 的 Consumer-local current owner / provenance：

- Core lifecycle / Planning / Readiness / Execute / Converge：`docs/project/development-method.md`；
- V4 Rule / Skill discovery extension：`docs/project/rule-discovery-method.md`；
- Rules：`docs/rules/**`；
- Skills：`skills/*/SKILL.md`；
- deterministic discovery / lint：`tools/rule-discovery/rule_discovery.py`；
- upgrade / validation evidence：`docs/project/agentic-dev-v4-08-consumer-validation-evidence.md`，仅作 evidence，不参与 ordinary routing。

显式 baseline upgrade 继续执行：恢复 Consumer Authority / Current Work → 固定 exact candidate → exact compare → `adopt / retain-or-override / reject-or-not-applicable / supersede-or-remove` → 投射到 Consumer-local owner → targeted verification → ordinary-runtime local-only 验证。未解决 Blocking / Medium 时不得推进 evaluated frontier。

当前核心 Feature 生命周期、Execution Unit、Verification / Evidence、Technology Profile、Review Environment、Data Access、External Operation 等详细规则由 `docs/project/development-method.md`、`docs/technical/verification-strategy.md`、当前 `docs/rules/**` 与直接 Authority 按职责持有，本文件不复制完整 procedure。

## Human Escalation

普通、低影响、可逆的实现选择由 Agent 自主处理并继续推进。只有决定会实质改变以下内容时才升级人工：

- Product Goal；
- Scope；
- User-visible Behavior；
- Business Boundary；
- Acceptance Result；
- Significant Non-functional Obligation；
- Major Architecture Direction；
- Security / Privacy Sensitive 行为；
- 超出当前授权范围的破坏性或难以恢复的外部状态变化。

普通命名、文件组织、库级选择、测试组织方式等低影响可逆事项不得机械升级人工，除非已经演变成上述高影响决定。

## 验证与 GitHub 操作授权

任何成功、完成、PASS、修复或 Ready 声明都必须有与 claim 类型和目标提交匹配的 Current Evidence。

项目负责人已对 `dygapp/jilinjobs-cms` 的日常 GitHub Repository 操作给予持续默认授权，包括读取、Branch / Commit / Issue / PR 创建更新、Push 已验证变更、运行 / 观察 / 按规则重试 Actions，以及满足验证要求后的正常 Consumer 集成。该授权不允许 Agent 自行改变 Product Intent / Scope，也不自动开放 Production Deployment、Secrets 或无关外部副作用。

### Repository Operation Boundary

`dygapp/jilinjobs-cms`：

- 允许读取和修改仓库文件；
- 允许创建 / 更新 Branch、Commit、Issue、PR；
- 允许 Push 已验证项目变更；
- 允许运行、观察或按 Consumer 规则重试 GitHub Actions；
- 允许在当前 Authority 与验证要求满足时执行正常 Consumer integration。

`dygapp/agentic-dev`：

- 允许读取 Repository、Branch、Commit、Issue、PR 与文件；
- 允许创建 Experiment / Feedback Issue，或向现有相关 Issue 追加 / 更新反馈；
- **禁止修改仓库文件；**
- **禁止创建或更新 Branch、Commit、PR；**
- **禁止运行、重试或改变该仓库 Actions 状态。**

工具具备技术能力不等于 Human Authority 已授权。其他 Repository / 外部系统写操作、Production Deployment、Credentials / Secrets 与明显破坏性不可逆操作不因上述授权自动放开。

## Experiment Boundary

本项目可以作为 `agentic-dev` Consumer Experiment / Validation 的真实 Consumer，但实验跟踪事实不得成为 Consumer Product Authority。

只回传有意义的 Evidence 与 Classification Candidate，不记录完整 Conversation、Private Reasoning、无价值 Skill 调用流水或 Consumer 已存在的整份业务文档副本。Consumer Agent 可以提出 upstream Classification Candidate，但不得自行把实验观察提升为 `agentic-dev` Method / Contract 结论。
