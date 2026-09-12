# Consumer 开发方法与 Skill 使用规则

本文固化 `jilinjobs-cms` 在持续开发阶段实际采用的 AI Agent 开发方法、Skill 使用规则和上下文恢复规则。

本文是 Consumer-local 规则。后续开发默认优先读取并遵守本仓库 `AGENTS.md`、`README.md`、`docs/README.md`、本文及其他当前 Authority；不需要在每次普通开发工作中重新读取 `dygapp/agentic-dev`。只有项目负责人明确要求升级 `agentic-dev` baseline、当前 Repository Authority 明确要求读取 / 比较 upstream，或当前任务本身是显式 `agentic-dev` Consumer experiment / validation 时，才重新读取固定 upstream ref；本地文档缺失、stale / ambiguity 或 Agent uncertainty 只触发本地 fail closed，不单独授权 upstream re-entry。

## 1. 方法来源、evaluated baseline 与 provenance

```text
Repository:
dygapp/agentic-dev

Previous Evaluated Baseline:
d9fad0da83dbdb61cac5eb9778b0258c6861eef1

Current Evaluated Baseline:
2fe193035c629f6b8805fd473bd322f70fe6e172

Capability Milestone Tag:
baseline-2026-09-04-engineering-capability

Capability Milestone Commit:
5be2e6aad29b2be6b8535b3690daf3533ee22a46
```

当前 evaluated baseline 是 `dygapp/agentic-dev@2fe193035c629f6b8805fd473bd322f70fe6e172`。它只回答“本 Consumer 最近完整比较并完成采用判断到哪个精确 upstream baseline”，**不表示本仓库所有方法、规则、Skill 或文档都采用自该 commit**。

从 previous evaluated baseline `d9fad0da83dbdb61cac5eb9778b0258c6861eef1` 到 candidate / current evaluated baseline `2fe193035c629f6b8805fd473bd322f70fe6e172` 的 GitHub exact compare 为 ahead 34 / behind 0。V3-08 Track B 对该 delta 执行逐项分类后，Consumer 只投射具有持续本地价值的长期语义：

- **adopt — Consumer Lifecycle**：显式 baseline upgrade、逐项 adoption、采用验证、普通运行 local-only、显式 re-entry upstream；
- **adopt — Agent Resource Model**：规范正文 / 资源固有结构 / 派生发现投影分层；真实 semantic owner 继续拥有正文，不建立第二 current truth；
- **adopt — Resource Discovery Architecture**：薄 Local Discovery Entry、一个 primary responsibility + 最小 supporting locator、state-only / routing-only / execution 的渐进式加载、本地 fail-closed；
- **adopt — Skill identity / admission / supporting-resource boundary**：Skill 只拥有稳定独立执行过程，不能因为“重要、可复用、有步骤、位于 skills 目录”就自动 Skill 化；supporting resource 不因物理目录取得平级所有权；
- **retain / override — Consumer-local Verification / Evidence / External Operation / Engineering Discipline / Technology Profile**：上游变化与本仓库现有长期 owner 语义一致或更通用时，继续由 `docs/technical/verification-strategy.md`、本文、`AGENTS.md`、`docs/project/execution-continuity-guidelines.md`、`docs/project/review-feedback-cycle.md` 等 Consumer-local owner 承载，不复制 upstream Guide；
- **reject / not applicable — agentic-dev project-only / Research / Eval / historical / V3 Project state**：`agentic-dev` 自身 `AGENTS.md`、README、Roadmap、Issue / PR / milestone、Research / Eval、tasks/plans、自采用实例均不成为 Consumer current authority；
- **reject physical copy — upstream discovery instance**：不复制 `agentic-dev/docs/discovery/**`；Consumer 以 `docs/README.md` 作为自己的 Local Discovery Entry；
- **Reviewed Discovery Map = NOT REQUIRED**：当前稳定入口、资源原生身份与 owner inventory 足以可靠发现；没有持续跨资源正规化成本证据；
- **Runtime View = NOT REQUIRED**：没有真实运行成本证据支持增加纯生成运行投影。

本次 upgrade-only disposition / validation history 记录在 `docs/project/agentic-dev-v3-08-track-b-evidence.md`。该 evidence 用于 provenance 和下一次显式 upgrade，不属于普通 Fresh Context 的默认读取集合。

必须长期分离三类状态：

1. **evaluated upstream baseline**：最近完整比较到哪个精确 upstream commit；
2. **current local asset provenance / owner**：每项 Consumer-local 当前能力实际由哪个本地 semantic owner 承载、来源于何处或已经如何本地演进；
3. **upgrade-only decision history**：`adopt / retain / reject / supersede` 判断及比较证据。

不得用一个 baseline SHA 代替后两者，也不得因为 upstream 后续有新提交就在普通运行中自动改变 Consumer 行为。

### 1.1 此前已采用能力继续有效

previous baseline 及更早 Capability Milestone 已选择性固化并继续保留：

- **Planning Candidate 与 Execution Unit 身份边界**：Roadmap / Backlog / Issue 中的 Future Work、Planning Candidate、Requirement Candidate 在进入 `slice-work` 前不具有 Execution Unit 身份；只有 Specification Ready 且必要 Technical Planning 已完成或确认不需要后，`slice-work` 才形成 Candidate Execution Unit；stable Identifier 只承担追踪 / 依赖身份，不构成 Readiness PASS；只有 `readiness-check` PASS 后才成为 Ready Execution Unit。
- **Ephemeral Evidence Promotion**：Workflow Artifact、远程输出、临时 Snapshot 等执行证据，经适当 Authority 接受并成为后续稳定迁移、评审或运行输入时，必须显式晋升为 Consumer-owned 持久输入，保留 provenance / integrity，并重新取得受影响 Current Evidence。
- **Long-lived Review Environment Lifecycle**：长生命周期单实例 Review Environment 具有可观察 owner、lease 取得 / 续期 / 到期 / 释放与 stale-run 策略；自动 Verification 与 Human Review 可以具有不同租约生命周期。
- **Data Access Scope & Boundedness Control**：本 Consumer 继续以 `docs/technical/verification-strategy.md` §2.4 的作用域查询、分页窗口和边界验证语义作为项目级验证实现。
- **Implementation Minimality & Speculative Complexity Control**、**Surgical Change & Diff Scope Control**、**Technology Profile Contract**、**Vue 3 + TypeScript Technology / Verification Profile**。
- Stale Verification Contract、Visual Fidelity、自动验证与 Human Review Baseline 隔离、bind mount 可重复恢复、Artifact Evidence、异步 Actions 闭环、Human Review Finding 分类、外部媒体真实内容验证、后继提交 Evidence Claim 影响判断与 Roadmap / GitHub 集成状态边界。
- 共享外部资源并发边界按真实冲突域治理；配置责任与已有能力复用基于真实 ownership / lifecycle 判断，不为消除硬编码机械配置化或为复用扩大依赖面。

这些能力的真实 current owner 是本仓库对应 Consumer-local 文档与代码 / Workflow，不因 evaluated baseline 前进而被重新归因到 `2fe193...`。

## 2. Consumer 与 agentic-dev 的职责边界

- `agentic-dev` 决定可复用的“如何工作”。
- `jilinjobs-cms` 决定本项目的 Goal、Scope、Requirements、Specification、Architecture、Code、Tests、Verification 与 Integration Policy。
- `agentic-dev/docs/project/*` 中属于其自身项目的状态、路线和实验事实不得复制为 Consumer 项目事实。
- 从 `agentic-dev` baseline 吸收的方法变化，应转化为本仓库可直接执行的本地规则，而不是要求后续 Agent 持续跨仓库读取方法文档。
- 新的 `agentic-dev` 提交不会仅因存在就自动覆盖已固化的 Consumer-local 规则；只有显式 baseline 升级才重新比较并处理更新、保留或取代关系。
- 显式允许访问 upstream 也不等于允许采用；只读比较 / 研究 / V3 验证默认只形成 Evidence，只有明确进入 adoption / upgrade lifecycle 才允许更新本地 current owner 或 evaluated baseline。

### 2.1 Consumer Override Boundary

Technology Profile 的适用顺序为：

1. Vue / TypeScript 等技术本身的客观语义约束；
2. 本 Consumer 已确认的实际版本、Architecture / ADR 与项目技术边界；
3. 本 Consumer 的 Repository / Engineering / Verification Authority；
4. Technology Profile 的 Engineering Default / Conditional Guidance；
5. 普通实现偏好。

当前实际 package versions、package scripts、tsconfig、Vue Router / Multi-entry Architecture、Element Plus 使用规则和 `docs/technical/verification-strategy.md` 均属于 Consumer-local Authority。不得为了匹配 Technology Profile Research Anchor 机械升级 Vue、TypeScript、`vue-tsc`、Vite、Vue Router、Element Plus 或其他依赖，也不得仅为采用 Profile 迁移稳定组件或重写既有 API 风格。

### 2.2 Consumer Lifecycle 与 upstream re-entry

普通运行默认只依赖本仓库当前状态。允许重新进入 `agentic-dev` upstream 的条件仅包括：

- 项目负责人或当前 Repository Authority 明确启动新的 baseline adoption / upgrade；
- 本地必要可复用能力经确认缺失，并且 Repository Authority 明确允许从 upstream 解析 / 采用；
- 当前任务被明确声明为 `agentic-dev` 实验 / 验证；
- Repository Authority 明确要求读取 / 比较 upstream；
- 新 Consumer 正在执行首次采用。

upstream 有新 commit、Local Discovery Entry 出现 stale / missing / ambiguity、Agent 尚不确定、或某条 upstream 规则“看起来更新”，都不能单独触发自动 upstream 访问。普通运行发现失败先回到本地 Authority fail closed。

一次 baseline upgrade 的语义路径是：

```text
Previous evaluated baseline
→ fixed candidate baseline
→ exact delta classification
→ per-item disposition
→ Consumer-local projection candidate
→ adoption verification
→ Consumer integration gate
→ evaluated baseline progression
→ local-only ordinary runtime
```

任一关键门禁失败、中断或出现未解决 Blocking / Medium 时，不推进 evaluated baseline，已有 Consumer current owner 继续有效。

### 2.3 Resource ownership 与 Local Discovery

Consumer 资源按三层理解：

```text
规范 / 事实正文
→ 资源固有结构
→ 可选派生发现投影
```

正文始终由真实 semantic owner 持有。路径、metadata、索引、Map、Catalog、Runtime View、Issue comment 或当前会话都不能取得第二份规范正文所有权。

`docs/README.md` 是本仓库 Local Discovery Entry。当前无需额外 Reviewed Discovery Map / Runtime View。发现决策只形成临时的：

```text
one primary responsibility
+ minimal supporting locator
+ stage / lifecycle action
```

Current resource set 由真实 owner、Repository Authority、资源原生状态与 supersede / disable 关系确定；不得维护脱离 owner 的第二 `active/current` 真值。

## 3. 常规 Feature 工作流

当前项目采用以下主流程：

```text
Consumer Authority / Domain Context
        ↓
Planning / Requirement Candidate
        ↓
Clarify Intent（仅在存在实质产品歧义时）
        ↓
Ready Specification
        ↓
Technical Planning?（按需，完成或确认不需要）
        ↓
Slice Work
        ↓
Candidate Execution Unit(s) + optional stable Identifier
        ↓
Readiness Check
        ↓
Ready Execution Unit
        ↓
Fresh-context Execute
        ↓
Converge
        ↓
Ready to Integrate
        ↓
Repository Policy / Human Authority
```

规则：

- 阶段是工作状态，不要求每个阶段都创建文档。
- Specification 聚焦 WHAT / WHY。
- Technical Plan 只在跨 Execution Unit 的长期 HOW 协调具有持续价值时持久化。
- Planning / Requirement Candidate 在 `slice-work` 前不具有 Execution Unit 身份；Roadmap 顺序、预编号、Issue 标签或名称不能替代上游 Readiness。
- `slice-work` 只在 Specification Ready 且必要 Technical Planning 已完成或确认不需要后形成 Candidate Execution Unit；Candidate Unit 可以获得稳定 Identifier，但该 Identifier 不构成 Readiness PASS 或 Execute 授权。
- `readiness-check` 对 Candidate Unit 执行统一 Gate；只有 PASS 后才成为 Ready Execution Unit。
- Execution Unit 应纵向、范围明确、可独立验证、可追溯并适合 Fresh Context。
- 实施文件、具体命令和局部施工步骤优先通过 JIT Plan 在执行时确定。
- 通用 Method 的终点是 `Ready to Integrate`；实际 merge / release / deploy 仍服从本仓库授权和策略。

### 3.1 依赖 PR 与 squash merge

当一个工作被拆成多个存在依赖关系的 PR，而最终集成采用 squash merge 时，必须区分临时的 **review topology** 与最终的 **integration topology**。stacked PR 可以用于提前审查和并行准备，但它的 Git ancestry 不能自动视为最终 `main` ancestry。

规则：

- 如果没有明显的并行审查或提前验证收益，优先在父 PR 合并后从最新 `main` 创建下一层依赖 PR，减少不必要的 ancestry 重写。
- 如果使用 stacked PR，父层 squash merge 后，child 在进入 Ready to Integrate 前必须规范化到实际最新 `main`，并重新确认 PR diff 只包含本层职责，不把已合并父层再次带入 diff。
- rebase、rebuild、cherry-pick、force-update 或等价 branch normalization 后，必须重新读取 GitHub PR 的实际 Head SHA、base、changed files / diff 和关联 Workflow；branch ref 更新成功不能单独证明 PR snapshot 与 Actions 已经刷新。
- 规范化导致 Head SHA 改变时，Current Evidence 继续按当前 Evidence Claim 规则重新取得或逐项判断，不复用与旧 Head 错配的 CI / Review 结果。
- 如果现有 PR 在 branch rewrite 后无法可靠刷新到新 Head，或 PR snapshot / diff 与实际目标树存在歧义，不得继续合并该 PR；应保留历史记录并创建基于最新 `main` 的干净替代 PR。

该规则不禁止 stacked PR；目标是在 squash merge 仓库中避免把 review convenience 误当作无需重新收敛的最终集成结构。

## 4. 验收、调试与验证闭环

每项 Specification Acceptance Obligation 必须能够闭环到：

1. 明确的实现责任；
2. 明确的验证责任；
3. 计划取得的 Verification Evidence；
4. 已实际执行并与当前实现匹配的 Current Evidence。

实现覆盖不等于验证覆盖。没有与声明匹配的 Current Evidence，不得声明完成、通过或修复成功。

当 Feature-wide `converge` 暴露的缺口只是已有 Acceptance Obligation 缺少验证覆盖时，应优先补齐 Verification Coverage，不得因此发明新的产品范围。

遇到验证失败时，不默认“测试永远正确”，先建立 Authority-backed Expected vs Actual，并至少区分：

```text
Implementation Defect
Stale Verification Contract
Runtime / Environment Problem
External Dependency Problem
```

当 Test / Workflow assertion / fixture / snapshot 与当前更高优先级 Authority 或 Specification 冲突时：

- 把问题归类为 Stale Verification Contract；
- 修正真正拥有陈旧断言的验证层；
- 如果同一产品语义在多个验证层重复维护，识别契约所有者，删除无必要重复或共享同一权威来源；
- 修正后重新运行当前有效验证，确认新契约能证明当前 Expected Behavior，同时不掩盖真实实现缺陷。

### 4.1 实施判断：配置责任与已有能力复用

实施阶段发现固定值、硬编码或准备自行实现通用技术能力时，不根据字面形式直接判定缺陷，先读取当前 Consumer Authority、实际代码、依赖与 Runtime 边界。

配置责任至少区分：

- 稳定领域 / 安全 / 协议 / 页面模板 / 算法常量：保留在版本控制代码或相应权威载体；
- 管理员持续维护的运营数据：进入现有 CMS / 网站属性；
- 低频结构元数据：进入 Consumer 已选择的结构化元数据权威；
- 部署实例差异：进入 Spring 外部化配置；
- CI、Review、FRP、发布过程参数：进入 Repository / Environment / Deployment Variables。

具体项目分类继续以 `docs/technical/configuration-governance.md` 为准。不得只为消除字面量新增数据库字段、设置页面、环境变量或配置框架。

实现通用能力前必须检查当前代码库、框架、标准库和已引入依赖；已有能力满足当前功能契约及安全、可观察性、性能和生命周期约束时优先复用，仅使用最薄适配层承载项目特有差异。已有能力与真实需求不匹配时允许自有实现，但需基于当前证据说明差异，不以‘框架优先’替代工程判断。

### 4.2 Implementation Minimality 与 Diff Scope

实施选择的目标不是最少行数，而是在满足当前 Authority、Acceptance、Verification 与工程健康前提下使用最低必要复杂度。失败路径、安全措施、当前验证需要、真实多个消费者、Authority 要求的配置 / 迁移 / 兼容性，以及小范围行为保持型 preparatory refactor 可以构成必要复杂度；“未来可能需要”“为了更灵活/企业级/可扩展”“可以套设计模式”“所有值都应配置化”等不能单独构成证据。

Unit 完成前执行轻量 Final Diff Scope Check。每个有意义的 Diff 区域必须属于当前 Unit 产品实现、当前验证、当前变更导致的 Authority 同步、必要 preparatory refactor 或其直接 cleanup；邻近但独立的 bug、TODO、历史死代码、样式清理、全局格式化和无关优化默认不进入当前 Diff。

### 4.3 Data Access Scope & Boundedness Control

集合型数据访问首先保护正确的数据集合边界，其次才是性能优化。当前 Unit 涉及列表、集合、Top-N、分页、snapshot 或批量读取时，按实际风险确认：

1. 当前页面、API、Job 或模块真正消费的业务作用域，例如栏目、parent、tenant、状态、组织或其他 membership boundary；
2. 集合是有当前 Authority 支持的稳定有界集合，还是会持续增长 / 无法可靠界定；
3. 数据需要的 lifecycle / freshness，是 request-local、page-local、稳定 application snapshot，还是需要持续刷新；
4. 定义成员资格和业务顺序的过滤 / 排序是否在 window / pagination 前形成；
5. 是否需要 page、cursor、chunk、Top-N，以及 continuation 是否具有稳定排序；
6. 列表是否只需要 summary/basic fields，detail 是否需要完整表示；不因存在两个消费者就机械创建第二套 DTO；
7. 验证是否真正越过 page / Top-N / scope 边界，并包含足以暴露截断问题的 competing records。

规则：

- 页面最终只展示 N 条，不能证明先读“全局前 N 条”再客户端过滤是正确的数据访问边界；
- 当业务 scope 决定集合成员资格时，优先在数据访问契约 / 查询层形成 scope，再执行 window / pagination；只有 Specification / Domain Authority 明确定义 global Top-N / ranking 本身属于业务语义时例外；
- “当前 fixture 只有少量数据”不能证明集合稳定有界；持续增长或无可靠上界的数据默认不能依赖永久全量加载；
- 导航树、受控站点配置等由 Authority 能解释长期小规模上界的结构数据，可以完整获取并在合适生命周期复用，不为了接口形式统一机械分页；
- 不为了避免重复请求机械创建全局 cache / registry / 新状态层，新增复用机制继续受 Implementation Minimality 约束；
- 本纪律不固定 page size、cursor 技术、数据库方案，也不要求所有接口都分页或所有列表都创建 Summary DTO。

本 Consumer 的机器验证映射继续以 `docs/technical/verification-strategy.md` §2.4 为准。该节现有“作用域查询、分页窗口、异步 UI 完成条件”规则已覆盖本 Discipline 当前需要的验证边界，不需要建立第二套重复契约。

### 4.4 Vue 3 + TypeScript Technology Profile

当前 `frontend/public-site` 已确认使用 Vue `3.5.40`、TypeScript `5.9.3`、`vue-tsc 3.3.9`、Vite `8.1.5`、Vue Router `5.2.0` 与 Element Plus `2.14.4`；`npm run build` 实际执行 `vue-tsc --noEmit && vite build`。这些 Consumer 事实覆盖 Profile Research Anchor。

在当前代码真实适用时采用以下默认：props 保持单向数据流；正确声明 props / emits；模板引用按生命周期视为可能为空；computed 保持无副作用；`watch` / `watchEffect` 正确处理依赖与异步失效工作，旧异步结果不得在依赖改变后无条件覆盖新状态；新代码优先保持 TypeScript inference，不以 `any` 或无证据断言逃逸类型系统；`defineModel`、`useTemplateRef` 等版本能力只在当前组件真实受益时使用，不为统一风格重写稳定组件。

验证按风险映射到 Consumer 实际命令：SFC / 类型契约至少需要 Vue-aware type-check；reactivity / watcher / lifecycle 需要能证明依赖变化和旧工作失效的行为测试；DOM / Router / 用户交互增加 Browser E2E；视觉 Acceptance 再增加 Visual Evidence；build / module / tsconfig 需要 type-check + bundler build，并按 Runtime 风险追加 Browser / Integration。具体执行以 `docs/technical/verification-strategy.md` 为准。

## 5. 证据声明、Human Review 与视觉验收边界

证据类型必须与声明类型匹配：

- Backend / Frontend Build、Unit / Integration / Browser E2E 只证明其实际覆盖的行为；
- Functional Browser Verification 可证明路由、交互、资源加载和已编码断言；
- 对视觉复刻、设计稿还原、品牌一致性等 Visual Fidelity Requirement，在没有完整机器可判定容差时，Functional Browser PASS 不能单独替代视觉判断；
- Visual Fidelity 应使用可追溯参考证据，优先形成“原站/设计参考 → Review Runtime 截图 → AI 明显差异检查 → Human Visual Review”的组合路径；
- AI 视觉检查用于提前消除明显差异，Human Visual Review 负责最终人工判断边界；
- Human Review 原始结论必须按实际范围记录，例如“基本通过，暂未发现新的阻塞问题”不能扩大为“完全一致”或无条件验收。

Human Review Finding 需要按观察内容而不是评审名称分类：

```text
Human Observation
→ Re-read Current Authority / Product Intent
→ Classify
   - Implementation Defect
   - Product / Requirement Ambiguity
   - Domain / Architecture Authority Gap
   - Runtime Problem
   - Low-risk Visual / Interaction Adjustment
→ Route to matching work stage
```

人工观察是 Evidence，但不会仅因为来自 Human Review 就自动成为 Requirement；同样，也不能因为来自 Visual Review 就把产品或架构缺口静默压缩成 CSS 微调。

如果 Artifact 本身是必要 Verification Evidence，除了确认 upload step 成功，还应重新读取当前 Run 的 Artifact 集合并核对 Artifact 名称、Run 和 Head SHA；必要 Artifact 缺失时 Workflow 应配置为失败。

### 5.1 后继提交的 Evidence Claim 影响判断

祖先提交已经获得 Runtime / Human Review Evidence 后，如果当前目标提交又产生新变更，默认先把祖先证据视为待重新判断的旧证据，不根据 `docs-only`、文件扩展名、文件数量或“CI 仍为绿色”自动继承。

只有同时满足以下条件，才可以按具体 Evidence Claim 复用未受影响证据：

1. 确认 Evidence Commit 是当前目标提交祖先，并取得两者完整、精确差异；
2. 逐项说明差异为什么不会改变该 Evidence Claim 所覆盖的行为、环境、数据、资源或人工判断对象；
3. 与该 Claim 相关的 Repository Authority、Requirement、Specification、Architecture、Acceptance、Workflow、Runtime Configuration、Migration、Fixture 和版本化资源均未发生影响性变化；
4. 当前 Head 完成其自身需要的 targeted checks；
5. 记录 Evidence Commit SHA、Current Target SHA、compare range、原 Run / Review 引用、可复用 Claim 与需要重新验证的 Claim。

Evidence reuse 是按声明的，不是给整个提交一次性盖章。祖先 Run 可以继续作为未受影响行为的祖先证据，但不得描述为当前 Head 的 Run；如果无法证明某项 Claim 不受影响，就重新取得该项验证或 Review。

## 6. Human Review Environment 状态隔离

当自动化验证后还需要暴露同一 Runtime 供人工评审时，默认采用：

```text
Automated Verification
→ Collect Current Evidence
→ Recreate / Reset Known Database and Static Baseline
→ Seed Explicit Human Review Fixtures
→ Start / Expose Review Runtime
→ Verify Review Baseline and Access Path
```

规则：

- 自动测试数据、测试导航、临时文件、缓存和会话不得因共用环境而默认进入 Human Review；
- Human Review Fixture 必须显式定义，只保留确实用于人工观察的数据；
- 数据库和静态资源应从 Flyway、版本化静态资源包或其他 Consumer Authority 可追溯来源恢复；
- Reset 后重新验证测试数据已移除、版本化资源已恢复、人工 Fixture 已准备、服务健康和外部评审地址可访问；
- 容器向 host bind mount 写文件时，明确 UID/GID、ownership、permissions 与清理身份；普通 runner 无权删除 root-owned 文件时，不得把普通 `rm -rf` 当作可靠清理方案；
- Cleanup / Reset 必须可重复执行并得到同一已知状态；该规则仅适用于已授权的临时验证/评审环境，不授予 Production 或共享数据的破坏性清理权限。
- 长生命周期单实例 Review Environment 还必须维护可观察 owner / lease：明确自动 Verification 与 Human Review 的租约取得、续期、到期和释放条件，定义 stale-run 判定与接管规则；新 Head 不因“更新”这一事实自动取得摧毁有效 Human Review lease 的权利。

## 7. 外部输入与媒体资源验证

从外部站点、API、附件或其他 Repository 取得的二进制/媒体资源，如果准备进入版本化站点基线、后台可管理资源或目标 Runtime，应按当前风险执行：

```text
Acquire
→ Verify Content Signature / Media Type
→ Decode or Parse when relevant
→ Normalize when needed
→ Version / Persist
→ Verify in Target Runtime
```

规则：

- 文件名、扩展名、URL 后缀和响应头只能作为线索，不能单独证明真实内容类型；
- 图片等资源在需要时同时核对是否可实际解码，以及尺寸、透明度等会影响当前声明的属性；
- 实际格式与扩展名或目标 Runtime 要求不一致时，应更正命名、转换格式或拒绝输入，禁止只改扩展名；
- 转换或规范化后重新验证生成物，不能把转换命令退出成功当作 Runtime 可用；
- 普通文本或与当前行为无关的低风险输入不要求无差别执行昂贵媒体分析。

该规则同时约束后台静态资源管理的服务端校验：如果后台宣称接收图片等受控媒体类型，应验证实际内容与允许类型一致，而不能只依据文件扩展名放行。

## 8. 外部操作与异步执行闭环

外部 Repository / GitHub / Workflow 操作遵循：

```text
Analyze
→ Act
→ Observe
→ Collect Current Evidence
→ Diagnose
→ Fix / Retry when authorized
→ Verify
→ Report
```

规则：

- 写操作前读取当前 Source of Truth，写后重新读取验证；Mutation Response 不等于目标状态已成立。
- 同一任务涉及多个 Repository 时，对读取、Issue / Evidence、文件 / Branch / Commit / PR、Workflow、Merge / Release / Deploy 等权限分别判断，不把一个 Repository 的授权推导到另一个 Repository。
- 持续有效的权限边界应固化为 Consumer Project Rule；本项目的具体权限矩阵以 `AGENTS.md` 为准。
- Workflow、Deployment、远程 Job 等异步操作进入 `queued`、`pending` 或 `in_progress` 时，只表示执行仍处在闭环中间状态；当当前目标要求取得结果且 Runtime 仍可观察时，不因“仍在运行”而默认请求人工继续。
- 异步观察必须有界：按正常运行基线设置合理轮询间隔、观察上限、timeout 与 cancellation 策略，避免无限等待。
- 失败后先取得日志、Job / Step、Artifact 或其他诊断证据；修复属于当前 Scope 且已授权时，使用 `systematic-debug` 定位 Root Cause、执行最小修复、重跑并重新观察。
- 闭环可在三类结果下结束：取得并核对目标证据；出现真实 Human / Runtime 阻塞；达到有界观察上限并准确保留 `Executed but not fully verified`。后两者均不能声明完成。
- 外部操作使用固定域名、代理名、端口、评审 / 部署槽位、临时数据库或其他排他资源时，必须按真实共享资源确定 concurrency / lock 范围，覆盖所有会争用该资源的触发路径；独立工作默认排队，不把取消当作默认互斥策略；只有新工作确实 supersede 旧工作且取消后的释放闭环可靠时才取消。
- Workflow cancellation、进程终止、锁取得或清理命令成功不能单独证明共享资源已经释放或归属正确；重新取得资源后必须核对 owner / Head / 环境，并验证目标地址或服务实际对应当前 Run。
- 当临时 Workflow Artifact / Snapshot / 远程输出已经被 Authority 接受为后续稳定迁移、评审或运行输入时，必须完成持久化 Promotion、保留 provenance / integrity，并把后续长期消费者切换到持久输入；不能继续把会过期的 Artifact 当作长期事实来源。

## 9. Project Roadmap

本项目需要跨多个里程碑和 Fresh Context 持续演进，因此维护：

```text
docs/project/project-roadmap.md
```

Roadmap 只维护项目级持久路线：已完成、当前、下一步和条件性后续。单个 Execution Unit、临时命令或局部实施步骤不进入 Roadmap。

Roadmap 与 GitHub 原生集成状态职责分离：

- PR 是否 open / merged、精确 Merge Commit、临时 Branch 是否删除等瞬时事实由 PR、Issue、Commit History 等 Source of Truth 保存；
- 在工作进入 Ready to Integrate 前，应让拟集成版本中的阶段、核心目标和已决定下一步在合并后仍然成立，不留下“等待当前 PR 合并”一类马上陈旧的长期路线；
- 合并后如果没有改变阶段、核心目标、里程碑状态或已决定的下一步，不仅为了记录“刚刚合并”创建新的 Repository Change；
- 如果合并确实改变长期路线，优先并入紧随其后的实质工作；只有陈旧 Roadmap 会立即阻塞或误导 Fresh Context 时才单独修复；
- 状态修复本身不能继续触发另一个只记录该修复已合并的递归尾部变更。

README 只提供 Roadmap 入口，不并行维护第二份易变化的详细项目路线。

## 10. Skills 与 supporting capabilities

当前 evaluated baseline 的核心 Skills：

- `clarify-intent`
- `specify`
- `technical-plan`
- `slice-work`
- `readiness-check`
- `execute-unit`
- `systematic-debug`
- `converge`

平台专项 Skill：

- `github-actions-verification`

Skill identity 只适用于具有稳定触发 / 不触发条件、稳定输入、可重复且足够独立的执行过程、可观察输出、明确退出 / Stage Return / Escalation、可独立组合，并且复杂度值得按需加载的能力。以下事实单独存在均不足以创建 Skill：重要、可复用、有若干步骤、会被多个 Skill 消费、Agent 需要读取、或文件位于 `skills/`。

Engineering Discipline、Technology / Verification Profile、Consumer Lifecycle、Repository-local Policy、Guide 与派生发现导航继续由各自 owner 承载，不为了激活方便 Skill 化。Skill supporting resource 只有在形成独立 semantic owner 或被多个职责独立消费时才重新分类；物理上位于 Skill 目录不取得平级所有权。

使用规则：

- routing-only 只返回一个 primary responsibility + minimal supporting locator，不加载完整 Skill；
- 真正执行职责时才加载 primary Skill 与必需 supporting capabilities；
- 只加载当前工作真正需要的 Skill，不要求每个工作走完整 Skill 清单；
- Skill 不得覆盖 Consumer Authority；
- `slice-work` 只消费 Ready Specification 与必要 Technical Plan；它形成 Candidate Execution Units，并可分配稳定 Identifier，但不得自行给出 Readiness PASS；
- `readiness-check` 只读检查 Specification、Candidate Unit Set 与相关 Authority；编号、Roadmap 顺序或 Issue 标签不能替代可检查输入，也不能替代 Gate Verdict；
- `execute-unit` 只执行已经通过 Readiness 的 Ready Execution Unit；在实现前按需读取当前适用的 Consumer-local Engineering Discipline / Technology Profile，而不是复制其完整正文进 Skill；完成前执行 Final Diff Scope Check；涉及集合、列表、Top-N、分页或 snapshot 时，同时应用 Data Access Scope & Boundedness Control；
- 遇到实现阶段的意外失败时，使用系统化调试路径，而不是无证据试错；
- 当失败来自 Test / Workflow assertion 等 Verification Artifact 时，`systematic-debug` 必须先核对其与当前 Authority 的一致性，允许并要求在证据支持时识别 Stale Verification Contract；
- 当 GitHub Actions 的触发、CI 可观察性、Artifact、容器 Runtime、Human Review Baseline、timeout / cancellation、diagnostics 或祖先 Evidence reuse 会影响证据可靠性时，按需应用 `github-actions-verification`；
- 如果调用要求实际完成 GitHub Actions 验证，dispatch / rerun 成功只是 `Act`，不是 Skill 退出条件；仍可观察的 `queued` / `pending` / `in_progress` Run 必须继续有界观察；
- 观察中持续核对 Run event、Head SHA、status / conclusion、Jobs / Steps / Logs / Artifacts 与当前 PR / Branch / Commit 的对应关系，只使用与当前目标提交真实关联的 Evidence；
- Run 失败且修复已获授权时，取得诊断证据后进入 `systematic-debug`，完成最小修复、重跑和复验；
- 如果调用只要求设计或优化验证路径而不要求实际执行，应返回 Evidence Retrieval Plan，并明确实际 Completion Evidence 尚未取得，不把计划写成已执行结果。

## 11. Fresh Context 与渐进式本地发现

新的上下文先通过 `AGENTS.md`、根 `README.md` 到达 `docs/README.md` Local Discovery Entry，再按当前任务选择最小正确读取集：

### 11.1 state-only

只询问 Current Execution State 时：

1. `AGENTS.md`；
2. 根 `README.md`；
3. `docs/README.md`；
4. `docs/work/current/README.md`；
5. 当前 Open execution PR / branch 与必要 GitHub Current Evidence。

如果这些事实能够无歧义回答 state-only 请求，则停止；不机械读取完整 Roadmap、本文、所有 Skills 或 upstream。

### 11.2 routing-only

只需要判断“当前由谁负责 / 下一阶段是什么 / 应读取什么”时：

1. 从 `docs/README.md` 的 stable locator 找到一个 primary owner；
2. 只读取足以消歧的最小 supporting owner / Current Evidence；
3. 返回一个 primary responsibility + minimal supporting locator + stage / lifecycle action；
4. 不为了 routing 加载完整 Skill procedure。

### 11.3 execution

真正进入 Planning / Readiness / Execute / Debug / Converge / GitHub Actions 等执行职责时，才读取：

- 当前 primary Authority；
- 本文中相应 Method contract；
- primary Skill / process；
- 当前条件真实触发的 Engineering Discipline、Technology / Verification Profile、External Operation 或 GitHub Actions supporting capability；
- 当前 Work / code / tests / PR / Actions Evidence。

不得依赖历史聊天或个人记忆补充未固化的项目事实。ordinary runtime 默认 local-only；本地 stale / missing / ambiguous 先按 `docs/README.md` fail closed，不自动访问 upstream。

### 11.4 Fresh Context Handoff Prompt Contract（Prompt-as-Locator）

Fresh Context 的 handoff prompt 是**定位器（Locator）**，不是第二份项目权威（Authority）、状态快照或方法副本。它的目标是让新会话知道“从哪个 Consumer Repository、哪个当前工作入口开始恢复”，而不是替 Repository 重新讲述项目。

生成新的会话切换提示词时，默认只允许携带：

- `Fresh Context` 声明；
- Consumer Repository 标识；
- 要求以当前 GitHub Repository 为唯一项目事实来源，并按 Repository Authority 恢复上下文；
- 当前工作入口，例如某个 Issue、用户刚刚指定的目标或尚未固化但本次必须延续的最小意图；
- Repository 尚无法推导、且项目负责人在当前会话新增加的会话级约束。

以下内容只要能够从当前 Repository / GitHub 重新恢复，就**不得为了“交接完整”“防止遗漏”或“作为定位线索”再次复制进 handoff prompt**：

- Repository Authority / Knowledge Boundary / GitHub 权限矩阵；
- Consumer 当前 `agentic-dev` baseline、Capability Milestone 或 baseline upgrade 规则；
- Development Method、Skill 流程、`slice-work` / `readiness-check` / Execute / Integration 规则；
- Roadmap 当前状态、已完成 EU、当前 Ready EU、Planning Candidate 列表；
- PR / Branch / Commit SHA / Actions Run / CI 结果等 GitHub 原生状态；
- Requirement / Specification / Technical Plan / ADR / Work Artifact 的内容摘要；
- 已经固化的 V2、Migration、Runtime、Architecture、Verification、E1～E3 prerequisite 等项目约束；
- 下一步的详细执行步骤，只要这些步骤应由当前 Roadmap / Method / Authority 在新上下文中重新推导。

生成 prompt 前执行一个删除测试：

> **如果新会话通过读取当前 GitHub Repository 和 Current Evidence 能得到这句话，就从 handoff prompt 删除。**

规则补充：

- 不通过“这些历史状态只作为线索、需要重新核验”来为重复复制开例外；可恢复事实即使标注为线索，仍然会制造冗余和潜在双重 Authority。
- 如果当前会话产生了一个**持续有效且后续必须依赖**的新规则、状态或决策，应优先先把它固化到适当 Consumer Authority，再生成最小 handoff prompt；不能长期依赖 prompt 保存它。
- 如果新的用户意图只是下一会话的一次性目标、尚无必要进入长期 Authority，可以在 prompt 中最小携带；新会话确认它具有持续约束价值后，再按正常 Method 固化。
- 不要求 handoff prompt 复述 Fresh Context 的完整恢复顺序；提示词只需要求读取 `AGENTS.md` / `README.md` 并按 Consumer-local Fresh Context 规则恢复，具体顺序由本节 Authority 负责。
- 当项目负责人要求“生成新会话提示词”时，Agent 应以本契约为生成 Gate，而不是默认输出完整 conversation handoff summary。

推荐的最小形态是：

```text
这是一个 Fresh Context。不要依赖其他聊天、历史会话或个人记忆；GitHub Repository 是唯一项目事实来源。

继续：<consumer-repository>

先读取当前 AGENTS.md、README.md，并按 Consumer-local Fresh Context 规则从 GitHub 恢复当前状态。

当前工作入口：<issue / user-specified goal>。

恢复后按当前 Repository Authority 与 Project Roadmap 继续。
```

模板只是结构示例，不要求逐字复制；如果其中某一项在当前场景没有必要，应继续删除，而不是为了模板完整保留。

## 12. baseline 升级规则

只有项目负责人明确要求更新 `agentic-dev` baseline 时，才执行 baseline 升级。

升级时：

1. 重新读取当前 Consumer Authority、Local Discovery Entry、Current Work / GitHub evidence，并确认没有冲突的 active lifecycle；
2. 精确确认 previous evaluated baseline 与固定 candidate baseline；previous baseline 缺失时不得猜测，按显式 baseline recovery 处理；
3. 使用 exact compare 识别真实 delta，candidate 评估期间 upstream 新 commit 不自动进入范围；
4. 区分 Core Method / Principle、Skill / Contract、Engineering Discipline、Technology / Verification Profile、Consumer Lifecycle / Discovery、External Operation、`agentic-dev` project-only、Research / Eval / historical；
5. 对 Consumer 可能持续消费的变化逐项给出 `adopt / retain-or-override / reject-or-not-applicable / supersede-or-remove`；
6. 优先更新现有 Consumer semantic owner；不机械复制 upstream 完整文档、Project state、Research / Eval 或 self-adoption 实例；
7. 分别维护 evaluated baseline、current local asset provenance / owner、upgrade-only decision history；baseline SHA 不代表所有 upstream 资源已经采用；
8. 按实际变化同步检查 `AGENTS.md`、本文件、`docs/README.md`、Verification Strategy、Roadmap 与其他受影响 owner；已一致的 Consumer-local 契约不为制造 diff 重复改写；
9. 验证 state-only / routing-only / execution progressive loading、ordinary runtime local-only、owner / locator / supersede / ambiguity / no-match risk fail-closed、Track A Current State ownership 与相关 Consumer currentness；
10. 在完成前重新检查 Consumer side drift；只重验受影响 disposition，无法安全判断影响范围时 fail closed；
11. 只有 Consumer-local projection 验证完成、Blocking=0、Medium=0、Repository integration gate 满足且普通运行能够 local-only 后，才推进 evaluated baseline；
12. 完成后 ordinary runtime 不默认读取 upgrade history、upstream delta、rejected items 或 `agentic-dev` Project state。

任一关键门禁失败、中断或存在未解决 Blocking / Medium：保持 previous evaluated baseline，不通过更新一个 baseline pointer 掩盖部分完成。