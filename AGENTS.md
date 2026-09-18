# AGENTS.md

## 项目定位

`jilinjobs-cms` 是吉林省智慧就业云平台中“信息发布与网站服务”相关能力的独立 Consumer 项目。

本文件只维护 Repository Governance、Authority Boundary、Knowledge Boundary、Development Method adoption、Human Escalation 与 GitHub operation rules，不缓存某个 Execution Unit 的高频 当前门禁（Gate）。已完成里程碑、持久路线与后续 Planning directions 由 Project Roadmap 维护；当前执行生命周期 由 `docs/work/` 的专门 locator / lifecycle contract 管理。Generic CMS Core / JilinJobs Site Definition / Historical Migration / Runtime / Replaceable Public Renderer 等当前长期系统边界由 `docs/architecture/cms-architecture.md` 持有；Issue #77 只保留历史 planning / decision / evidence timeline。Documentation Authority Map 与 Consumer **Local Discovery Entry** 统一由 `docs/README.md` 承载；`docs/**/archive/**` 与 `docs/work/archive/**` 默认不参与 Fresh Context Current Authority 恢复。不得从其他项目、其他会话、个人记忆、旧预编号路线、惯例或实现便利性中推导、补充或扩大本项目的产品范围。

## 当前状态 ownership 与 locator

当前状态 按职责分层，不建立第二套 当前状态 truth：

- `AGENTS.md`：稳定 Repository rules / Authority Boundary；
- 根 `README.md`：稳定项目入口与范围；
- `docs/README.md`：Documentation Authority Map / Local Discovery Entry；
- `docs/work/README.md`：Ready / active Execution Unit 的 Entry / Exit / 失败关闭 lifecycle contract；
- `docs/work/current/README.md`：Repository-owned **当前执行生命周期 Locator**；
- `docs/project/project-roadmap.md`：durable milestones、长期边界与 Planning directions；
- GitHub PR / Branch / Actions：各自原生瞬时事实与 当前证据；
- controlling Issue：Planning / decision / evidence timeline，不因历史 comment 中出现 `PENDING` / `PASS` / `Current Evidence` 字样就永久拥有 Execute Gate。

Bootstrap / Roadmap surface 不并行维护 `Current Ready Execution Unit`、Readiness `PENDING/PASS`、exact implementation Head 或最近 Actions 等高频 Execute Gate 真值。Fresh Context 在使用 `docs/work/current/README.md` 的 `NONE` 作为 state-only 安全停止条件前，必须核对当前任务相关的 Open execution PR / branch 与必要 Readiness / Integration 当前证据；`NONE` 只表示没有 Ready / active Execution Unit，不表示没有 规划候选 或新的用户指定 Planning 目标。

若 当前工作 locator、active artifact、就绪证据、Open execution work 或关键 GitHub 当前证据之间缺失、冲突或无法消歧，必须 **失败关闭**：不得进入、继续或继承 执行授权，先恢复一致的 当前状态。GitHub 原生状态只对其职责内的瞬时事实负责；本规则**不**建立“GitHub 永远高于本地文件”的通用优先级，Requirement / Specification / Architecture / Method / Work lifecycle 继续按 仓库权威 各自拥有其语义责任。

## 本地资源发现（Local Discovery）

普通运行从本文件直接进入 `docs/README.md` Local Discovery Entry，并按当前目标使用最小正确读取集合。根 `README.md` 仍是稳定项目范围 Authority / Human View，但不再作为 ordinary Fresh Context 的固定预读；只有当前任务需要项目范围摘要、人类入口或该 Authority 本身进入责任时才读取：

- **state-only**：定位 当前执行生命周期 与必要 GitHub native state，不机械加载完整 Roadmap、Method、Skills 或 upstream；
- **routing-only**：返回一个 primary responsibility + 最小 supporting locator，不加载完整 Skill；
- **execution**：真正执行某项职责时才加载对应 Method / primary Skill 与当前条件实际触发的 supporting capability。

当前 Consumer 不维护 Reviewed Discovery Map，也不维护 运行时视图；稳定入口、当前 resource owner 与原生 locator 已足够支撑普通运行。若 source / locator 缺失、语义所有者 不明确、多个 primary 无法消歧、override / supersede 冲突，或 no-match 但已知 Governance / Verification risk 仍存在，则回到本地 Authority **失败关闭**，不得自动访问 `dygapp/agentic-dev` 修补普通运行。

execution 中的横切 Rule 由 Consumer-local `tools/rule-discovery/rule_discovery.py` 从当前任务事实提取 bounded signals 做确定性候选发现；详细 contract 由 `docs/architecture/rule-discovery.md` 持有。当前 Rule 默认按“一个可独立发现的具体任务或责任所需的有界规范语义集合”形成自然边界，不采用“一条 assertion = 一个 Rule 文件”的机械拆分；同一任务中通常共同发现、共同消费的 policy 优先聚合，只有独立 discovery 能实际减少无关加载 / 错误激活，或存在不同 technology / artifact / risk / lifecycle / 语义所有者 时才拆分。Technology Rule 子目录只服务人类维护，不参与 runtime matching。普通运行必须满足：

- task signals 使用 `phases / activities / technologies / artifacts / risks` 五维；非空数组表示 known，`[]` 表示 known-empty，`null` 表示 unknown / unsafe-to-canonicalize；每维最多 6 个 canonical token；
- 首次向人工输出包含项目事实、状态判断、方案、复核结论或其他实质内容前，建立一次 `communication` responsibility checkpoint；若本次 task-level discovery 已经覆盖 `communication` + `human-facing-content`，不重复调用；仅表示“正在恢复 / 正在读取”、且不承载项目事实或判断的短进度消息可以先行；
- 当前 直接责任 建立后，在该责任的**首个受 Rule 约束的实质动作前必须完成本次 task-level discovery**；有副作用动作始终属于这一边界；只读 Authority / Repository fact 恢复可以先于 discovery，但只读工作不能因此绕过适用于其输出或复核责任的 Rule；
- 直接责任 切换，或 phase / activity / technology / artifact / risk 等关键事实实质变化时，旧 candidate set 不跨职责继续生效；必须在下一次受 Rule 约束的实质动作前重新构造 signals 并发现；
- Rule Discovery CI lint、deterministic tests 与固定 smoke 只证明 Tool / corpus contract，**这些通过结果不能替代当前 task signals 的 task-level discovery**，也不能证明当前 Agent 的实时责任已经完成 discovery；
- Rule Discovery 本身属于 preflight infrastructure invocation，只计算候选，不修改项目语义或授予后续操作权限，因此不要求为调用 discovery 再递归执行一轮 discovery；当前执行面没有本地 shell 时，可以使用 Project Capability Profile 声明的 exact-SHA 云端 transport，但不得因此跳过 discovery；
- Rule metadata 与规范正文同文件维护在 `docs/rules/**`；
- discovery 返回的 candidate `{id,path}` locator 是 普通运行时 获得 Rule locator 的唯一入口；只读取返回候选的 Rule body，再做 semantic applicability confirmation；
- 不得通过 `rg --files`、`find`、目录树或其他方式枚举未命中的 Rule locator，也不得在 zero-candidate 后读取未命中 Rule 做 calibration；
- 不建立 Reviewed Discovery Map、Activation Manifest、Runtime Catalog、`rule-index` 或其他需要与 Rule 正文同步维护的中心路由资产；
- local discovery / metadata / ambiguity 失败一律 Consumer-local 失败关闭，不自动读取 upstream 当前状态；
- 准备请求人工执行动作、提供输入、作出决定或充当系统 / 工具之间的中转时，视为新的 human escalation responsibility checkpoint；发出请求前用 `activities=[human-escalation]`、`risks=[human-intervention]` 重新执行 Rule Discovery，并只在自动化 / Evidence 路径确实不足时请求最小必要人工动作；
- Skill 与 Rule 分离；`skills/*/SKILL.md` 只物理化本仓库已采用的稳定执行能力，不因物理文件存在扩大 Planning / Readiness / 执行授权。

Agent Skills Specification 的 Progressive Disclosure `<5000 tokens / <500 lines` 只作为 Rule 单文件上限复核参照，不是拆分阈值或目标大小。

## 文档语言与术语表达

本项目所有面向人的 Current / Partially Current 内容默认使用自然、完整、连续的中文。动作、判断、因果、结论以及标题、表格列名、流程节点等结构性标签都应以中文表达；不得为了显得专业、便于检索或制造“术语感”机械保留普通英文。该要求也适用于 Agent 面向人工展示的进度、分析、方案、风险、阻塞、验证和复核结论。

以下对象应保持可精确匹配的原始形式：

- Skill / Rule / Method 的精确 id 或调用名；
- 文件路径、Branch、Commit SHA、Issue / PR 编号；
- 代码标识符、配置键、数据库字段、API / CLI、命令与参数、协议值；
- 真实日志、错误信息以及外部产品、框架、协议、标准和官方项目名称；
- 跨 Gate / Verification / Review 使用的稳定状态值，例如 `PASS`、`FAIL`、`READY`、`BLOCKED`、`PENDING`、`HOLD`。

稳定状态值可以保持原样，但其原因、判断和结论仍使用自然中文；不得为了“纯中文”再建立一套与 `PASS` / `PENDING` 等并行的长期状态表示。

正式概念拥有英文 identity，不等于普通中文叙述默认保留英文名称。当前句子只需要表达含义、类型或状态而不需要逐字识别正式 identity 时，应使用自然中文或当前稳定中文表达，例如“稳定基线”“当前方法”“规则发现已完成”“人工评审”；只有确实需要消歧、引用精确 id / 路径或跨文档稳定身份时才保留原文。

语言整理不得合并或改变职责不同的正式对象。尤其保持以下区分：

- 方法阶段不等于同名 / 近义 Skill；
- Gate 不等于 Skill；
- artifact 不等于 lifecycle stage；
- 仓库权威 不等于 人工权威；
- 当前项目状态不等于 reusable capability；
- Human View 不等于 canonical Authority；
- Consumer-local adaptation 不等于 upstream canonical semantics；
- evaluated baseline 不等于已经启用的本地能力；
- upstream Project state 不等于 Consumer Project state。

同一 Current canonical concept 已有稳定中文表达时，后续面向人内容应沿用，不自行制造新的中文别名，也不恢复已经退出 Current model 的历史别名。业务 / 产品术语继续由真实 Requirement / Domain owner 持有，不迁移到通用语言 Rule、Guide 或 README。

`SUPERSEDED` / `HISTORICAL_EVIDENCE` 文档以证据保真为优先，不因纯语言原因批量改写历史正文；它们必须退出 ordinary Fresh Context Current Authority。历史文档如果重新晋升为 Current Authority，先完成 semantic reconciliation，再满足当前语言规范。

语言治理不得改变 Product Goal、Scope、Business Boundary、User-visible Behavior、Architecture Decision、Specification 或技术契约。若表达整理暴露真实语义冲突，返回对应 规范语义所有者 解决，而不是通过翻译选择隐式覆盖事实。

Current 文档的中文主语言、结构标题和本地引用完整性由 `scripts/verify-docs-governance.mjs` 与对应 GitHub Actions 持续约束；不得通过扩大例外列表规避真实 Current 文档问题。

## 仓库权威（仓库权威）

项目事实或规则发生冲突时，按以下优先级判断：

1. `AGENTS.md`：Repository Governance、Authority Boundary 与工作规则；
2. `README.md`：稳定项目目标、项目边界摘要与入口；
3. `docs/requirements/information-publishing.md` 与 `docs/requirements/cms-domain.md`：当前 Product / Domain Requirement Authority；
4. 后续依据以上权威正式形成的 Specification、Architecture、Decision、Project Roadmap、Consumer-local Method 等项目产物（Artifact）；
5. Code 与 Tests：用于证明当前实现状态，不得反向发明产品需求；
6. 会话历史（Conversation History）、临时计划和 Agent reasoning：均不构成项目权威。

上述优先级用于解决同一语义责任内的 Authority 冲突；对于 当前执行生命周期、GitHub native status、Requirement、Specification 等不同职责，先按本文件定义的 owner / locator 读取对应事实，再处理真正的语义冲突，不把优先级表机械解释成一个 surface 可以替代其他 surface 的职责。

根 `README.md` 是 Project Charter equivalent 与稳定范围摘要，详细 Product / Domain 事实分别由当前 canonical Requirement owner 持有。README 摘要没有逐项重复某个已确认 Product fact，不构成对该事实的隐式排除；Requirement 的详细事实也不能反向扩大已经由更高层 Human / 仓库权威 明确改变的项目边界。若这些 owner 出现真实冲突，必须显式澄清并同步对应 Authority，不允许依靠摘要缺失或历史来源引用长期维持双重解释。

## 知识边界（Knowledge Boundary）

本项目事实只允许来自：

- 当前 Consumer Repository；
- 明确指定的 `agentic-dev` baseline，但它只作为开发 Method、Operating Guide、Engineering Discipline、Technology Profile、Consumer Lifecycle / Resource Discovery 与 Skills 的知识来源；
- 已被有意纳入本仓库的权威需求输入；
- 当前 Runtime / Repository 可以直接观察到的状态和证据。

不得把其他聊天、其他项目、个人记忆或未经声明的领域假设直接作为 Consumer 项目事实。

`agentic-dev` 决定“如何工作”，不决定 `jilinjobs-cms` 的业务事实。即使 upstream baseline 被评估，也只有显式采纳并投射到本地 owner 的语义才进入 Consumer 普通运行时；upstream project-only / Research / Eval / self-adoption 状态不继承。

完成一次显式 adoption / upgrade 后，普通运行时 默认 `upstream access = 0`。上游 branch、Roadmap、Issue、PR 或后续 commit 不会自动改变 Consumer；本地 Rule Discovery 失败、缺失或歧义只触发本地 失败关闭，只有下一次显式 baseline upgrade / validation 才可重新读取固定 upstream commit。

## 开发方法（Development Method）

当前 evaluated upstream baseline、Consumer-local capability instance 与 Method selector 的唯一 Project Knowledge owner 是 `docs/project/project-capability-profile.md`。本文件只维护稳定的 upstream / local Knowledge Boundary、adoption discipline 与 baseline upgrade procedure，不复制 exact baseline SHA、previous baseline、historical projection / tag 或 upgrade-only decision history。已完成 upgrade 的 provenance / evidence 按需从 `docs/project/archive/` 与 GitHub history 定向恢复。

当前采用的方法不在普通开发中直接运行 upstream 文档，而是固化为 Consumer-local 一等资源：

- 需求基线建立 / 重建：`docs/methods/requirement-baseline-establishment.md`；
- 系统性架构澄清：`docs/methods/architecture-clarification.md`；
- 普通 Feature / change：`docs/methods/ai-development.md`；
- Consumer baseline 显式升级：`docs/methods/consumer-upgrade.md`；
- Review feedback：`docs/methods/review-feedback-cycle.md`；
- Method 实验：`docs/methods/method-experiment.md`；
- Method selector 与本地 capability instance：`docs/project/project-capability-profile.md`；
- Rule Discovery contract：`docs/architecture/rule-discovery.md`；
- 已采用的稳定执行能力：`skills/*/SKILL.md`。

后续普通开发固定从本仓库 `AGENTS.md` 与 `docs/README.md` 恢复；`docs/work/current/README.md`、`docs/project/project-capability-profile.md`、当前 selector 命中的 Consumer-local Method、`docs/project/project-roadmap.md`、根 `README.md` 以及其他 Consumer Authority 都按当前责任最小加载。除非项目负责人明确要求更新 `agentic-dev` baseline、本仓库 Authority 明确要求 upstream 比较，或当前任务本身就是显式 `agentic-dev` Consumer validation，否则不要求为普通开发跨仓库读取 `agentic-dev`。

当项目负责人明确要求升级 baseline 时，应：

1. 重新恢复 Consumer 当前 Authority / 当前工作 / GitHub evidence，并确认没有冲突的 active lifecycle；
2. 精确确认 previous evaluated baseline 与固定 candidate baseline，执行 exact compare；
3. 对 reusable delta 逐项分类 `adopt / adapt / retain / replace / reject / not-applicable`；
4. 区分跨项目可复用规则与 `agentic-dev` project-only / Research / Eval / historical / self-adoption 实例；
5. 优先投射到 Consumer 现有 语义所有者，不机械复制完整文档、Map、Catalog、运行时视图 或 Skill supporting resources；
6. 分开维护 evaluated baseline、current local asset provenance / owner、upgrade-only decision history；
7. 验证 state-only / routing-only / execution progressive loading、普通运行时 local-only、stale / missing / ambiguity / coverage / supersede 等 失败关闭 与 当前状态 ownership；
8. 只有 adoption verification 完成且 Blocking=0、Medium=0 时才推进 evaluated baseline；失败 / 中断保持 previous baseline；
9. 完成升级后恢复以 Consumer-local Authority 为普通开发入口，不继承 upstream Project Roadmap、Issue、实验状态或项目事实。

当前采用的主要方法原则：

- 使用渐进式披露（Progressive Disclosure），只加载当前职责真正需要的 Authority、Skill 与 supporting capability；
- 一次 discovery / routing 决策只确定一个 primary responsibility + 最小 supporting locator；routing-only 不加载完整 Skill，真正 execution 才加载 primary Skill；
- 普通 Feature / change 只在当前 Repository 已具备足以支持 Goal、Scope、Observable Behavior 与 Acceptance 的最小长期 Requirement / Domain / Architecture Context 时进入常规 Feature Method；单个 Feature 的局部歧义留在对应 owner 内解决；多个 Feature 共同受系统性 Requirement Baseline gap / conflict / ownership failure 阻塞时，由 `docs/project/project-capability-profile.md` 的 selector 进入 `docs/methods/requirement-baseline-establishment.md`；Requirement Baseline 已足够但存在阻塞多个 Feature 的长期高成本架构 driver 时，由 selector 进入 `docs/methods/architecture-clarification.md`；
- 阶段是工作状态，不为了表示阶段而机械创建 Artifact；
- 规格说明（Specification）聚焦 WHAT / WHY；
- 只有存在跨执行单元（Execution Units）的长期 HOW 协调价值时，才持久化技术计划（Technical Plan）；
- Planning / Requirement Candidate 在 `slice-work` 前保持规划身份；`slice-work` 只在上游 Ready 后形成 Candidate Execution Unit，可分配稳定 Identifier；Identifier 不等于 Readiness 或 Execute 授权，只有 `readiness-check` PASS 后才成为 Ready Execution Unit；
- 优先形成纵向、可独立验证、范围明确且 context-fit 的 Execution Unit；
- 在条件允许时使用 Fresh Context；
- Fresh Context 的会话切换提示词只承担 **Locator** 职责，不并行维护第二份项目 Authority；凡可从当前 GitHub Repository、Roadmap、Consumer-local Method、Requirement / Specification / Technical Plan、Issue / PR / Actions 或 Runtime Evidence 恢复的事实、规则、状态和执行步骤，不得为了“交接完整”再次复制进提示词；真正 Handoff 按 `docs/rules/repository/execution-continuity.md` 的条件与边界执行；
- 实施时选择当前证据支持的最低必要复杂度；没有当前 Requirement、Specification、Architecture、Verification、安全、性能、生命周期或真实多消费者证据支持的额外抽象、配置项、依赖、扩展点、框架层和未来分支默认不进入实现；必要的失败路径、安全措施、验证能力、行为保持型 preparatory refactor 与薄适配不属于“过度设计”；
- 最终 Diff 的每个有意义区域必须能追溯到当前 Unit 实现、验证、Authority 同步、必要 preparatory refactor 或其直接 cleanup；相邻 typo、TODO、历史死代码、独立优化、全局格式化等默认留在当前 Diff 之外；
- 当 Unit 涉及集合、列表或 snapshot 数据访问时，先确认真实 Consumer Scope、集合的稳定有界性或增长特征、Lifecycle / Freshness，再决定过滤、稳定排序、window / pagination、representation 与复用方式；页面最终展示数量、现有 `LIMIT/OFFSET` 或客户端过滤不能替代业务作用域；
- 每项 Acceptance Obligation 必须闭环到实现责任、验证责任、计划证据与已执行的 当前证据；
- 实现覆盖不等于验证覆盖；没有 当前证据，不得声明完成、通过或修复成功；
- Technology Profile 是可复用默认值而不是 Consumer 产品事实；本仓库当前确认的实际版本、Architecture / ADR、目录、依赖、Element Plus 规则、package scripts、tsconfig 与验证命令优先于 Profile Engineering Default；
- 当前 Vue 3 + TypeScript 代码遵守 Vue / TypeScript 客观语义约束，并在不与 Consumer Authority 冲突时采用 Profile 默认；不为采用 Profile 重写稳定组件或迁移既有 API 风格；
- Vue / TypeScript Verification 按变更风险映射到本仓库真实命令与证据，不机械执行 Profile 中不存在于本仓库的命令；
- 测试、Workflow assertion、fixture、snapshot 等 Verification Artifact 也可能陈旧；与更高优先级 Authority / Specification 冲突时分类为 Stale Verification Contract 并修正验证层；
- 证据类型必须与声明类型匹配；Functional Browser PASS 不能单独证明 Visual Fidelity；
- 人工评审 原始结论按实际范围记录，并按真实问题分类，不因评审名称自动压缩成视觉问题；
- 自动 E2E 与 人工评审 共用 Runtime 时，应在收集自动化证据后恢复已知数据库/静态资源基线，再注入明确的人工评审 Fixture；
- 长生命周期单实例 Review Environment 定义 owner、lease 取得 / 续期 / 到期 / 释放和 stale 判定；真实共享资源排他边界与环境生命周期分开治理；
- 容器可写 host bind mount 时显式处理 UID/GID、ownership、permissions、cleanup 与 Reset 的可重复验证；
- 外部二进制 / 媒体资源在内容类型会影响当前行为、验证或安全边界时验证真实格式，不只信扩展名 / URL / header；
- 临时 Workflow Artifact / Snapshot 经 Authority 接受成为稳定输入时执行持久化 Promotion，保留必要 provenance / integrity，并重新取得受影响 当前证据；
- 后继提交不按 `docs-only`、扩展名或变更数量机械继承祖先 evidence；只能按 exact diff + claim impact 复用未受影响声明；
- Roadmap 维护持久路线和 durable milestone，不逐项复制 PR / exact merge SHA / 临时 Branch / Actions 等 GitHub native transient state；
- 同一任务涉及多个 Repository 时分别确认操作授权，Runtime 工具能力本身不构成授权；
- Workflow、Deployment、远程 Job 等异步外部操作保持有界 observe / diagnose / retry / verify 闭环；
- 固定域名、代理名、端口、评审 / 部署槽位、临时数据库或单例服务等共享资源按真实冲突域治理；Run cancellation 与资源释放分别验证；
- 实施阶段发现“硬编码”或准备自行实现通用能力时先按真实变化来源、维护者、稳定性、安全 / 协议约束和 lifecycle 判断 responsibility；已有代码、框架、标准库或依赖满足契约时优先复用最薄适配。

当前核心 Skills、Engineering Disciplines 与 Technology Profile 的 Consumer-local 使用入口由 `docs/project/project-capability-profile.md` 提供；具体规范分别由对应 Method / Architecture / Rule / Skill / profile owner 持有，不在本文件重复维护 Skill 级细节。

## 人工升级（Human Escalation）

普通、低影响、可逆的实现选择由 Agent 自主处理并继续推进。

只有当决定会实质改变以下内容时，才升级给人工：

- Product Goal；
- Scope；
- User-visible Behavior；
- Business Boundary；
- Acceptance Result；
- Significant Non-functional Obligation；
- Major Architecture Direction；
- Security / Privacy Sensitive 行为；
- 超出当前授权范围的破坏性或难以恢复的外部状态变化。

不得把普通命名、文件组织、库级选择、测试组织方式等低影响可逆问题交给人工决定，除非它们已经演变为上述高影响事项。

## 验证与 GitHub 操作授权

任何成功、完成、通过或修复声明，都必须有与声明相匹配的 当前证据。

项目负责人已对 `dygapp/jilinjobs-cms` 的日常 GitHub Repository 操作给予**持续默认授权**。后续 Agent 不得为了以下正常项目操作反复询问“是否允许操作 GitHub”：

- 读取 Repository、Branch、Commit、Issue、Pull Request 和文件状态；
- 在本项目范围内创建或更新文件；
- 创建和更新 Branch、Commit、Issue、Pull Request；
- Push 已验证的项目变更；
- 运行、观察或按当前项目规则重试 GitHub Actions；
- 在当前工作已经满足相应 Verification / Convergence 要求时执行本仓库内的正常集成操作。

该授权只代表 Repository 操作权限，不代表 Agent 可以自行改变 Product Intent 或 Scope。产品决策仍必须遵守上面的 Human Escalation 规则。

### 仓库操作边界（Repository Operation Boundary）

当前 Consumer 工作对两个 Repository 的操作权限明确区分：

`dygapp/jilinjobs-cms`：

- 允许读取和修改仓库文件；
- 允许创建或更新 Branch、Commit、Issue、Pull Request；
- 允许 Push 已验证的项目变更；
- 允许运行、观察或按项目规则重试 GitHub Actions；
- 允许在当前 Consumer Authority 与验证要求满足时执行本仓库内正常集成操作。

`dygapp/agentic-dev`：

- 允许读取 Repository、Branch、Commit、Issue、Pull Request 和文件内容；
- 允许创建新的 Experiment / Feedback Issue，或向现有相关 Issue 追加、更新反馈；
- **禁止修改该仓库文件；**
- **禁止创建或更新该仓库 Branch、Commit、Pull Request；**
- **禁止运行、重试或以其他方式改变该仓库 GitHub Actions 状态；**
- 即使 Consumer Evidence 暴露 Method、Contract、Skill 或 Guide 改进候选，也只能通过 Issue 反馈，不得由当前 Consumer 工作直接实施到 `agentic-dev`。

上述边界属于 人工权威 明确授权，优先于工具本身可能具备的技术写权限。工具可执行某项操作不等于当前工作已获授权执行该操作。该本地权限矩阵是 `agentic-dev` 通用“多 Repository 分别确认授权”原则在本 Consumer 的具体实例，不应反向推导为其他 Consumer 的统一权限策略。

除上述对 `dygapp/agentic-dev` 明确开放的只读与 Issue 反馈权限外，以下事项不因本授权自动放开：

- 其他 `dygapp/jilinjobs-cms` 之外的 Repository 或外部系统写操作；
- Production Deployment / Release 到真实运行环境；
- Credentials、Secrets 或其他敏感配置操作；
- 与当前项目工作无关的外部副作用；
- 明显破坏性、不可逆且无法由当前项目规则安全判断的操作。

## 实验边界（Experiment Boundary）

本项目当前同时是 `agentic-dev` Consumer Experiment / Validation 的真实 Consumer，但实验跟踪事实不得成为 Consumer Product Authority。

只回传有意义的 Evidence 与 Classification Candidate，不记录：

- 完整 Conversation；
- Private Reasoning；
- 无价值的 Skill 调用流水；
- Consumer Repository 中已经存在的整份业务文档副本。

Consumer Agent 可以提出 Classification Candidate，但不得自行把实验观察提升为 `agentic-dev` 的 Method / Contract 结论。
