# AGENTS.md

## 项目定位

`jilinjobs-cms` 是吉林省智慧就业云平台中“信息发布与网站服务”相关能力的独立 Consumer 项目。

当前迭代聚焦**中心主站稳定性，以及中心党建正式页面、真实栏目、内容迁移与视觉收敛**。中心党建基础 Site Entry / Router / Shell 已完成；当前只按本仓库 V4.8 Requirement、Party Building Specification / Technical Plan 和原站直接证据推进，不得从其他项目、其他会话、个人记忆、惯例或实现便利性中推导、补充或扩大本项目的产品范围。现有通用 CMS 已证明足以承担党建栏目和文章时，优先复用，不因独立视觉主题自动新增党建专属后台或多站点模型。

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

`docs/requirements/information-publishing.md` 已由本仓库显式采纳为当前迭代的详细业务需求，但其来源文档中声明的 `relations.upstream` 以及正文引用的 `docs/project/project.md`、`docs/requirements/overview/system-module-boundaries.md` 当前并不存在于本 Consumer Repository。这些引用只保留其来源关系（Provenance / Upstream References），**不构成当前 Consumer Authority，也不得用于扩大或覆盖本仓库已明确的 Goal、Scope 与 Boundary**。只有后续被本仓库显式采纳的上游事实才可成为新的 Consumer Authority。

`README.md` 的当前迭代范围可以有意只选择原始需求的一部分。即使某项能力存在于详细需求文档中，只要 `README.md` 已明确将其排除在当前迭代之外，就不得在本轮自行实现，除非项目负责人正式调整范围。

## 知识边界（Knowledge Boundary）

本项目事实只允许来自：

- 当前 Consumer Repository；
- 明确指定的 `agentic-dev` baseline，但它只作为开发 Method、Operating Guide、Engineering Discipline、Technology Profile 与 Skills 的知识来源；
- 已被有意纳入本仓库的权威需求输入；
- 当前 Runtime / Repository 可以直接观察到的状态和证据。

不得把其他聊天、其他项目、个人记忆或未经声明的领域假设直接作为 Consumer 项目事实。

`agentic-dev` 决定“如何工作”，不决定 `jilinjobs-cms` 的业务事实。

## 开发方法（Development Method）

方法来源：

- Repository：`dygapp/agentic-dev`
- Validation Baseline Branch：`master`
- Validation Baseline Commit：`b80b2b1b7cea38eed0aef9807879e2a0d56afd2f`

该 baseline 延续已有 Project Roadmap、Fresh Context、Consumer-local Method、跨 Repository 授权、异步外部操作、验证证据、Human Review Environment、Human Review Finding 分类、外部媒体真实内容校验、后继提交 Evidence Claim 影响判断、Roadmap / GitHub 集成状态边界、共享资源真实冲突域、配置责任与已有能力复用规则，并新增可复用的 Engineering Discipline Authority、Technology Profile Contract、Vue 3 + TypeScript Technology Profile 与 Verification Profile。

本项目不是在每次开发工作中直接运行 `agentic-dev` 仓库的方法文档，而是将当前采用的方法和 Skills 使用规则固化在 Consumer Repository：

```text
docs/project/development-method.md
```

后续普通开发应优先读取并遵循本仓库 `AGENTS.md`、`README.md`、`docs/project/development-method.md`、`docs/project/project-roadmap.md` 以及与当前工作相关的 Consumer Authority。除非项目负责人明确要求更新 `agentic-dev` baseline，或本仓库尚未固化某个必要的方法问题，否则不要求为普通开发重新跨仓库读取 `agentic-dev`。

当项目负责人明确要求升级 baseline 时，应：

1. 读取指定分支的最新精确 commit；
2. 比较当前 Consumer baseline 到新 baseline 的 Method、Operating Guide、Engineering Discipline、Technology Profile、Contract 与 Skill 变化；
3. 区分跨项目可复用规则与 `agentic-dev` 自身 Project Rule；
4. 只将影响本 Consumer 工作方式、且具有持续约束价值的变化固化到本仓库；
5. 明确处理需要更新、保留或取代的 Consumer-local 规则，并同步更新本节、`docs/project/development-method.md` 和 `docs/project/project-roadmap.md`；
6. 完成升级后恢复以 Consumer-local Authority 为普通开发入口，不继承 `agentic-dev` 自身的 Project Roadmap、Issue、实验状态或项目事实。

当前采用的主要方法原则：

- 使用渐进式披露（Progressive Disclosure），只加载当前阶段真正需要的 Skills；
- 阶段是工作状态，不为了表示阶段而机械创建 Artifact；
- 规格说明（Specification）聚焦 WHAT / WHY；
- 只有存在跨执行单元（Execution Units）的长期 HOW 协调价值时，才持久化技术计划（Technical Plan）；
- 优先形成纵向、可独立验证、范围明确且 context-fit 的 Execution Unit；
- 在条件允许时使用 Fresh Context；
- 实施时选择当前证据支持的最低必要复杂度；没有当前 Requirement、Specification、Architecture、Verification、安全、性能、生命周期或真实多消费者证据支持的额外抽象、配置项、依赖、扩展点、框架层和未来分支默认不进入实现；必要的失败路径、安全措施、验证能力、行为保持型 preparatory refactor 与薄适配不属于“过度设计”；
- 最终 Diff 的每个有意义区域必须能追溯到当前 Unit 实现、验证、Authority 同步、必要 preparatory refactor 或其直接 cleanup；相邻 typo、TODO、历史死代码、独立优化、全局格式化等默认留在当前 Diff 之外；
- 每项 Acceptance Obligation 必须闭环到实现责任、验证责任、计划证据与已执行的 Current Evidence；
- 实现覆盖不等于验证覆盖；
- 没有 Current Evidence，不得声明完成、通过或修复成功；
- Technology Profile 是可复用默认值而不是 Consumer 产品事实；本仓库当前确认的实际版本、Architecture / ADR、目录、依赖、Element Plus 规则、package scripts、tsconfig 与验证命令优先于 Profile Engineering Default。不得为了匹配 Profile Research Anchor 机械升级 Vue、TypeScript、`vue-tsc` 或其他依赖；
- 当前 Vue 3 + TypeScript 代码遵守 Vue / TypeScript 客观语义约束，并在不与 Consumer Authority 冲突时采用 Profile 默认：保持 props 单向数据流、正确声明 props/emits、生命周期内安全处理模板引用、正确理解 `watch` / `watchEffect` 依赖与异步失效工作、保持 computed 纯计算、避免以 `any` 或无证据断言逃逸类型系统；不为采用 Profile 重写稳定组件或迁移既有 API 风格；
- Vue / TypeScript Verification 按变更风险映射到本仓库真实命令与证据，不机械执行 Profile 中不存在于本仓库的命令。当前公开站与管理端 `npm run build` 都先执行 `vue-tsc --noEmit` 再执行 `vite build`，因此该项目脚本同时提供 Vue-aware type-check 与 bundler build evidence；不能据此反向声称单独的 Vite transpile 等同于 type-check；DOM / Router / watcher / lifecycle / 视觉与多入口行为仍按 `docs/technical/verification-strategy.md` 追加对应 Browser / Visual / Integration Evidence；
- 测试、Workflow assertion、fixture、snapshot 等 Verification Artifact 也可能陈旧；当其与更高优先级 Authority / Specification 冲突时，应分类为 Stale Verification Contract 并修正验证层，而不是修改产品去恢复已被取代的旧行为；
- 证据类型必须与声明类型匹配；Functional Browser PASS 不能单独证明 Visual Fidelity，视觉复刻在缺少完整机器可判定容差时需要参考证据、AI 视觉对照与 Human Visual Review；
- Human Review 的原始结论按实际范围记录，不把“基本通过，暂未发现阻塞问题”扩大为“完全一致”或无条件验收；Human Review 中发现的问题不因评审名称自动归类为视觉问题，应重新读取当前 Authority 与 Product Intent 后区分 Implementation Defect、Product / Requirement Ambiguity、Domain / Architecture Authority Gap 或 Runtime Problem；
- 自动 E2E 与 Human Review 共用 Runtime 时，应在收集自动化证据后恢复已知数据库/静态资源基线，再注入明确的人工评审 Fixture；测试数据不得因环境复用而意外泄漏；
- 容器可写 host bind mount 时，必须显式处理 UID/GID、ownership、permissions、cleanup 与 Reset 的可重复验证；调用清理命令本身不构成 Cleanup Evidence；
- 从外部网站、接口、附件或其他 Repository 取得并准备版本化或交给 Runtime 消费的二进制/媒体资源时，文件名、扩展名、URL 后缀和响应头只能作为线索；当内容类型会影响行为、验证或安全边界时，必须使用内容签名、可靠媒体类型识别或实际解码/解析核对真实格式，必要时规范化后重新验证，禁止只改扩展名伪装格式；
- 已验证提交之后出现新提交时，不按 `docs-only`、文件扩展名或变更数量机械继承祖先证据；只有取得祖先 Evidence Commit 到当前目标提交的精确差异、逐项证明差异不影响具体 Evidence Claim 且相关 Authority / Requirement / Specification / Architecture / Acceptance / Runtime 语义未变时，才可按声明复用未受影响证据，并记录祖先 SHA、当前 SHA、compare range 与 claim 映射；受影响或无法证明不受影响的声明必须重新验证或重新 Review；
- 项目跨多个里程碑或 Fresh Context 持续演进时，维护 Consumer 自己的 `docs/project/project-roadmap.md`；Roadmap 维护持久路线和可恢复状态，不逐项复制 PR open/merged、精确 Merge Commit、临时分支删除等 GitHub 原生瞬时事实；仅当集成结果改变阶段、核心目标、里程碑或已决定下一步时更新路线，避免形成只记录上一 PR 已合并的递归尾部变更；
- 同一任务涉及多个 Repository 时，分别确认每个 Repository 的操作授权，Runtime 工具能力本身不构成授权；
- Workflow、Deployment、远程 Job 等异步外部操作在 `queued` / `pending` / `in_progress` 时仍属于执行闭环中间状态；只要 Runtime 可继续观察且当前目标需要结果，就应在授权范围内有界观察、收集证据、诊断、修复和重试，而不是仅因“仍在运行”就默认交回人工。
- 固定域名、代理名、端口、评审 / 部署槽位、临时数据库或单例服务等共享外部资源的并发边界必须按真实冲突域覆盖所有触发路径；独立工作争用同一资源时默认有界排队，只有新 Run 确实取代旧工作且取消后的资源释放闭环可靠时才使用 cancellation；Run cancellation 与资源释放必须分别验证。
- 实施阶段发现‘硬编码’或准备自行实现通用技术能力时，先按真实变化来源、维护者、稳定性、安全 / 协议约束和生命周期判断责任层；稳定且没有已证明外部维护责任的值默认保留在代码或既有权威载体中，不为消除字面量机械配置化；当前代码、框架、标准库或已引入依赖满足契约时优先复用，并只使用最薄适配层承载项目差异。

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
