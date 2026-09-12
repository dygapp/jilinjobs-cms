# AGENTS.md

## 项目定位

`jilinjobs-cms` 是吉林省智慧就业云平台中“信息发布与网站服务”相关能力的独立 Consumer 项目。

本文件只维护 Repository Governance、Authority Boundary、Knowledge Boundary、Development Method adoption、Human Escalation 与 GitHub operation rules，不缓存某个 Execution Unit 的高频 Current Gate。已完成里程碑、持久路线与后续 Planning directions 由 Project Roadmap 维护；Current Execution Lifecycle 由 `docs/work/` 的专门 locator / lifecycle contract 管理。Issue #77 继续承担 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层长期架构边界。Documentation Authority Map 与 Consumer **Local Discovery Entry** 统一由 `docs/README.md` 承载；`docs/**/archive/**` 与 `docs/work/archive/**` 默认不参与 Fresh Context Current Authority 恢复。不得从其他项目、其他会话、个人记忆、旧预编号路线、惯例或实现便利性中推导、补充或扩大本项目的产品范围。

## Current State ownership 与 locator

Current State 按职责分层，不建立第二套 Current State truth：

- `AGENTS.md`：稳定 Repository rules / Authority Boundary；
- 根 `README.md`：稳定项目入口与范围；
- `docs/README.md`：Documentation Authority Map / Local Discovery Entry；
- `docs/work/README.md`：Ready / active Execution Unit 的 Entry / Exit / fail-closed lifecycle contract；
- `docs/work/current/README.md`：Repository-owned **Current Execution Lifecycle Locator**；
- `docs/project/project-roadmap.md`：durable milestones、长期边界与 Planning directions；
- GitHub PR / Branch / Actions：各自原生瞬时事实与 Current Evidence；
- controlling Issue：Planning / decision / evidence timeline，不因历史 comment 中出现 `PENDING` / `PASS` / `Current Evidence` 字样就永久拥有 Execute Gate。

Bootstrap / Roadmap surface 不并行维护 `Current Ready Execution Unit`、Readiness `PENDING/PASS`、exact implementation Head 或最近 Actions 等高频 Execute Gate 真值。Fresh Context 在使用 `docs/work/current/README.md` 的 `NONE` 作为 state-only 安全停止条件前，必须核对当前任务相关的 Open execution PR / branch 与必要 Readiness / Integration Current Evidence；`NONE` 只表示没有 Ready / active Execution Unit，不表示没有 Planning Candidate 或新的用户指定 Planning 目标。

若 Current Work locator、active artifact、Readiness Evidence、Open execution work 或关键 GitHub Current Evidence之间缺失、冲突或无法消歧，必须 **fail closed**：不得进入、继续或继承 Execute Authority，先恢复一致的 Current State。GitHub 原生状态只对其职责内的瞬时事实负责；本规则**不**建立“GitHub 永远高于本地文件”的通用优先级，Requirement / Specification / Architecture / Method / Work lifecycle 继续按 Repository Authority 各自拥有其语义责任。

## 本地资源发现（Local Discovery）

普通运行从本文件与根 `README.md` 到达 `docs/README.md` Local Discovery Entry，并按当前目标使用最小正确读取集合：

- **state-only**：定位 Current Execution Lifecycle 与必要 GitHub native state，不机械加载完整 Roadmap、Method、Skills 或 upstream；
- **routing-only**：返回一个 primary responsibility + 最小 supporting locator，不加载完整 Skill；
- **execution**：真正执行某项职责时才加载对应 Method / primary Skill 与当前条件实际触发的 supporting capability。

当前 Consumer 不维护 Reviewed Discovery Map，也不维护 Runtime View；稳定入口、当前 resource owner 与原生 locator 已足够支撑普通运行。若 source / locator 缺失、semantic owner 不明确、多个 primary 无法消歧、override / supersede 冲突，或 no-match 但已知 Governance / Verification risk 仍存在，则回到本地 Authority **fail closed**，不得自动访问 `dygapp/agentic-dev` 修补普通运行。

## 文档语言与术语表达

本项目面向人的文档以**中文为主导语言**，并采用“中文主述、必要英文精确锚定”的表达原则。

- 中文已有自然稳定表达、且英文有助于与 `agentic-dev` Method、Skill、Contract 或技术概念精确对应时，首次重要出现优先使用“中文（English Term）”；
- 英文本身属于固定名称、状态或精确锚点时，可以使用“English Term（中文解释）”；
- Skill 名称、代码标识符、文件路径、命令、API 参数、协议名和专有名词保持原生形式；
- 不要求每次重复中英对照，避免双语注释成为阅读噪声；
- 术语表达调整不得改变已有产品、方法或技术语义。

同一文档中的术语应保持稳定。后续新增或实际触达的文档按上述原则逐步收敛，不为了形式统一制造大规模纯语言 diff。

## 仓库权威（Repository Authority）

项目事实或规则发生冲突时，按以下优先级判断：

1. `AGENTS.md`：Repository Governance、Authority Boundary 与工作规则；
2. `README.md`：当前项目目标、当前迭代范围与稳定项目入口；
3. `docs/requirements/information-publishing.md`：当前迭代范围内行为的详细权威业务需求；
4. 后续依据以上权威正式形成的 Specification、Architecture、Decision、Project Roadmap、Consumer-local Development Method 等项目产物（Artifact）；
5. Code 与 Tests：用于证明当前实现状态，不得反向发明产品需求；
6. 会话历史（Conversation History）、临时计划和 Agent reasoning：均不构成项目权威。

上述优先级用于解决同一语义责任内的 Authority 冲突；对于 Current Execution Lifecycle、GitHub native status、Requirement、Specification 等不同职责，先按本文件定义的 owner / locator 读取对应事实，再处理真正的语义冲突，不把优先级表机械解释成一个 surface 可以替代其他 surface 的职责。

`docs/requirements/information-publishing.md` 已由本仓库显式采纳为当前迭代的详细业务需求，但其来源文档中声明的 `relations.upstream` 以及正文引用的 `docs/project/project.md`、`docs/requirements/overview/system-module-boundaries.md` 当前并不存在于本 Consumer Repository。这些引用只保留其来源关系（Provenance / Upstream References），**不构成当前 Consumer Authority，也不得用于扩大或覆盖本仓库已明确的 Goal、Scope 与 Boundary**。只有后续被本仓库显式采纳的上游事实才可成为新的 Consumer Authority。

`README.md` 的当前迭代范围可以有意只选择原始需求的一部分。即使某项能力存在于详细需求文档中，只要 `README.md` 已明确将其排除在当前迭代之外，就不得在本轮自行实现，除非项目负责人正式调整范围。

## 知识边界（Knowledge Boundary）

本项目事实只允许来自：

- 当前 Consumer Repository；
- 明确指定的 `agentic-dev` baseline，但它只作为开发 Method、Operating Guide、Engineering Discipline、Technology Profile、Consumer Lifecycle / Resource Discovery 与 Skills 的知识来源；
- 已被有意纳入本仓库的权威需求输入；
- 当前 Runtime / Repository 可以直接观察到的状态和证据。

不得把其他聊天、其他项目、个人记忆或未经声明的领域假设直接作为 Consumer 项目事实。

`agentic-dev` 决定“如何工作”，不决定 `jilinjobs-cms` 的业务事实。即使 upstream baseline 被评估，也只有显式采纳并投射到本地 owner 的语义才进入 Consumer ordinary runtime；upstream project-only / Research / Eval / self-adoption 状态不继承。

## 开发方法（Development Method）

方法来源：

- Repository：`dygapp/agentic-dev`
- Previous Evaluated Baseline：`d9fad0da83dbdb61cac5eb9778b0258c6861eef1`
- Current Evaluated Baseline：`2fe193035c629f6b8805fd473bd322f70fe6e172`
- Capability Milestone Tag：`baseline-2026-09-04-engineering-capability` → `5be2e6aad29b2be6b8535b3690daf3533ee22a46`

Current Evaluated Baseline 只表示本 Consumer 已经完成 exact upstream compare 与 adoption verification 到哪个精确 commit，不表示 `2fe193...` 中所有文件、Project 状态或规则均被采用。当前 local asset 的真实 semantic owner / provenance 与 upgrade-only disposition history 必须与 baseline 分离；V3-08 Track B 的逐项记录见 `docs/project/agentic-dev-v3-08-track-b-evidence.md`，普通运行不默认读取该升级历史。

从 previous baseline 到当前 baseline 的可复用变化选择性固化为：Consumer Lifecycle、Agent Resource Model、Resource Discovery Architecture、Skill identity / admission / supporting-resource boundary；Verification / Evidence、External Operation、Engineering Discipline、Technology Profile 与既有 Consumer-local owner 一致的部分继续由本仓库现有 owner 承载，不复制 upstream Guide。`agentic-dev` 自身 `AGENTS.md` / README / Roadmap、V3 Project / Issue / PR 状态、Research / Eval、tasks/plans、自采用 `docs/discovery/**` 实例均不成为 Consumer current authority。

本项目不是在每次开发工作中直接运行 `agentic-dev` 仓库的方法文档，而是将当前采用的方法和 Skills 使用规则固化在 Consumer Repository：

```text
docs/project/development-method.md
```

后续普通开发应优先读取并遵守本仓库 `AGENTS.md`、`README.md`、`docs/README.md`、`docs/project/development-method.md`、`docs/project/project-roadmap.md` 以及与当前工作相关的 Consumer Authority。除非项目负责人明确要求更新 `agentic-dev` baseline、本仓库 Authority 明确要求 upstream 比较，或当前任务本身就是显式 `agentic-dev` Consumer validation，否则不要求为普通开发跨仓库读取 `agentic-dev`。

当项目负责人明确要求升级 baseline 时，应：

1. 重新恢复 Consumer 当前 Authority / Current Work / GitHub evidence，并确认没有冲突的 active lifecycle；
2. 精确确认 previous evaluated baseline 与固定 candidate baseline，执行 exact compare；
3. 对 reusable delta 逐项分类 `adopt / retain-or-override / reject-or-not-applicable / supersede-or-remove`；
4. 区分跨项目可复用规则与 `agentic-dev` project-only / Research / Eval / historical / self-adoption 实例；
5. 优先投射到 Consumer 现有 semantic owner，不机械复制完整文档、Map、Catalog、Runtime View 或 Skill supporting resources；
6. 分开维护 evaluated baseline、current local asset provenance / owner、upgrade-only decision history；
7. 验证 state-only / routing-only / execution progressive loading、ordinary runtime local-only、stale / missing / ambiguity / coverage / supersede 等 fail-closed 与 Current State ownership；
8. 只有 adoption verification 完成且 Blocking=0、Medium=0 时才推进 evaluated baseline；失败 / 中断保持 previous baseline；
9. 完成升级后恢复以 Consumer-local Authority 为普通开发入口，不继承 upstream Project Roadmap、Issue、实验状态或项目事实。

当前采用的主要方法原则：

- 使用渐进式披露（Progressive Disclosure），只加载当前职责真正需要的 Authority、Skill 与 supporting capability；
- 一次 discovery / routing 决策只确定一个 primary responsibility + 最小 supporting locator；routing-only 不加载完整 Skill，真正 execution 才加载 primary Skill；
- 阶段是工作状态，不为了表示阶段而机械创建 Artifact；
- 规格说明（Specification）聚焦 WHAT / WHY；
- 只有存在跨执行单元（Execution Units）的长期 HOW 协调价值时，才持久化技术计划（Technical Plan）；
- Planning / Requirement Candidate 在 `slice-work` 前保持规划身份；`slice-work` 只在上游 Ready 后形成 Candidate Execution Unit，可分配稳定 Identifier；Identifier 不等于 Readiness 或 Execute 授权，只有 `readiness-check` PASS 后才成为 Ready Execution Unit；
- 优先形成纵向、可独立验证、范围明确且 context-fit 的 Execution Unit；
- 在条件允许时使用 Fresh Context；
- Fresh Context 的会话切换提示词只承担 **Locator / Handoff** 职责，不并行维护第二份项目 Authority；凡可从当前 GitHub Repository、Roadmap、Consumer-local Method、Requirement / Specification / Technical Plan、Issue / PR / Actions 或 Runtime Evidence 恢复的事实、规则、状态和执行步骤，不得为了“交接完整”再次复制进提示词；完整规则见 `docs/project/development-method.md`；
- 实施时选择当前证据支持的最低必要复杂度；没有当前 Requirement、Specification、Architecture、Verification、安全、性能、生命周期或真实多消费者证据支持的额外抽象、配置项、依赖、扩展点、框架层和未来分支默认不进入实现；必要的失败路径、安全措施、验证能力、行为保持型 preparatory refactor 与薄适配不属于“过度设计”；
- 最终 Diff 的每个有意义区域必须能追溯到当前 Unit 实现、验证、Authority 同步、必要 preparatory refactor 或其直接 cleanup；相邻 typo、TODO、历史死代码、独立优化、全局格式化等默认留在当前 Diff 之外；
- 当 Unit 涉及集合、列表或 snapshot 数据访问时，先确认真实 Consumer Scope、集合的稳定有界性或增长特征、Lifecycle / Freshness，再决定过滤、稳定排序、window / pagination、representation 与复用方式；页面最终展示数量、现有 `LIMIT/OFFSET` 或客户端过滤不能替代业务作用域；
- 每项 Acceptance Obligation 必须闭环到实现责任、验证责任、计划证据与已执行的 Current Evidence；
- 实现覆盖不等于验证覆盖；没有 Current Evidence，不得声明完成、通过或修复成功；
- Technology Profile 是可复用默认值而不是 Consumer 产品事实；本仓库当前确认的实际版本、Architecture / ADR、目录、依赖、Element Plus 规则、package scripts、tsconfig 与验证命令优先于 Profile Engineering Default；
- 当前 Vue 3 + TypeScript 代码遵守 Vue / TypeScript 客观语义约束，并在不与 Consumer Authority 冲突时采用 Profile 默认；不为采用 Profile 重写稳定组件或迁移既有 API 风格；
- Vue / TypeScript Verification 按变更风险映射到本仓库真实命令与证据，不机械执行 Profile 中不存在于本仓库的命令；
- 测试、Workflow assertion、fixture、snapshot 等 Verification Artifact 也可能陈旧；与更高优先级 Authority / Specification 冲突时分类为 Stale Verification Contract 并修正验证层；
- 证据类型必须与声明类型匹配；Functional Browser PASS 不能单独证明 Visual Fidelity；
- Human Review 原始结论按实际范围记录，并按真实问题分类，不因评审名称自动压缩成视觉问题；
- 自动 E2E 与 Human Review 共用 Runtime 时，应在收集自动化证据后恢复已知数据库/静态资源基线，再注入明确的人工评审 Fixture；
- 长生命周期单实例 Review Environment 定义 owner、lease 取得 / 续期 / 到期 / 释放和 stale 判定；真实共享资源排他边界与环境生命周期分开治理；
- 容器可写 host bind mount 时显式处理 UID/GID、ownership、permissions、cleanup 与 Reset 的可重复验证；
- 外部二进制 / 媒体资源在内容类型会影响当前行为、验证或安全边界时验证真实格式，不只信扩展名 / URL / header；
- 临时 Workflow Artifact / Snapshot 经 Authority 接受成为稳定输入时执行持久化 Promotion，保留必要 provenance / integrity，并重新取得受影响 Current Evidence；
- 后继提交不按 `docs-only`、扩展名或变更数量机械继承祖先 evidence；只能按 exact diff + claim impact 复用未受影响声明；
- Roadmap 维护持久路线和 durable milestone，不逐项复制 PR / exact merge SHA / 临时 Branch / Actions 等 GitHub native transient state；
- 同一任务涉及多个 Repository 时分别确认操作授权，Runtime 工具能力本身不构成授权；
- Workflow、Deployment、远程 Job 等异步外部操作保持有界 observe / diagnose / retry / verify 闭环；
- 固定域名、代理名、端口、评审 / 部署槽位、临时数据库或单例服务等共享资源按真实冲突域治理；Run cancellation 与资源释放分别验证；
- 实施阶段发现“硬编码”或准备自行实现通用能力时先按真实变化来源、维护者、稳定性、安全 / 协议约束和 lifecycle 判断 responsibility；已有代码、框架、标准库或依赖满足契约时优先复用最薄适配。

当前核心 Skills、Engineering Disciplines、Vue 3 + TypeScript Profile 的 Consumer-local 使用边界统一记录在 `docs/project/development-method.md`，不在本文件重复维护 Skill 级细节。

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

任何成功、完成、通过或修复声明，都必须有与声明相匹配的 Current Evidence。

项目负责人已对 `dygapp/jilinjobs-cms` 的日常 GitHub Repository 操作给予**持续默认授权**。后续 Agent 不得为了以下正常项目操作反复询问“是否允许操作 GitHub”：

- 读取 Repository、Branch、Commit、Issue、Pull Request 和文件状态；
- 在本项目范围内创建或更新文件；
- 创建和更新 Branch、Commit、Issue、Pull Request；
- Push 已验证的项目变更；
- 运行、观察或按当前项目规则重试 GitHub Actions；
- 在当前工作已经满足相应 Verification / Convergence 要求时执行本仓库内的正常集成操作。

该授权只代表 Repository 操作权限，不代表 Agent 可以自行改变 Product Intent 或 Scope。产品决策仍必须遵守上面的 Human Escalation 规则。

### Repository Operation Boundary

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

上述边界属于 Human Authority 明确授权，优先于工具本身可能具备的技术写权限。工具可执行某项操作不等于当前工作已获授权执行该操作。该本地权限矩阵是 `agentic-dev` 通用“多 Repository 分别确认授权”原则在本 Consumer 的具体实例，不应反向推导为其他 Consumer 的统一权限策略。

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