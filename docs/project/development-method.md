# Consumer 开发方法与 Skill 使用规则

本文固化 `jilinjobs-cms` 在持续开发阶段实际采用的 AI Agent 开发方法、Skill 使用规则、Engineering Discipline、Technology Profile 使用边界和上下文恢复规则。

本文是 Consumer-local 规则。后续开发默认优先读取并遵循本仓库 `AGENTS.md`、`README.md`、本文及其他当前 Authority；不需要在每次普通开发工作中重新读取 `dygapp/agentic-dev`。只有项目负责人明确要求升级 `agentic-dev` baseline，或当前 Consumer 文档明确无法回答方法问题时，才重新读取指定 baseline 并将需要长期保留的变化回写到本仓库。

## 1. 方法来源与当前基线

```text
Repository:
dygapp/agentic-dev

Baseline:
master@b80b2b1b7cea38eed0aef9807879e2a0d56afd2f
```

该 baseline 提供 Method、Operating Guide、Engineering Discipline Authority、Technology Profile Contract、Skill Contracts 与 Skills 的来源依据，但不提供本项目的业务事实。

相对上一 Consumer baseline `a82e559cb67cafbcf96265a70a1167a9a75db5ba`，本次选择性吸收的持续规则是：

- **Implementation Minimality & Speculative Complexity Control**：选择当前证据支持的最低必要复杂度；没有当前需求、规格、架构、验证、安全、性能、生命周期或真实多消费者证据支持的额外抽象、配置、依赖、扩展点、框架层和未来分支默认不进入实现。
- **Surgical Change & Diff Scope Control**：最终 Diff 的每个有意义区域必须能追溯到当前 Unit 实现、验证、Authority 同步、必要的行为保持型 preparatory refactor 或其直接 cleanup；相邻 typo、TODO、历史死代码、独立优化与全局格式化默认不进入当前 Diff。
- **Technology Profile Contract**：Technology Profile 提供可复用技术语义、工程默认值、常见误用和 Verification Profile，但不是 Consumer 产品事实；Consumer 当前确认的版本、Architecture / ADR、目录、依赖、组件库、package scripts、tsconfig、实际验证命令和 Human Review 规则具有覆盖权。
- **Vue 3 + TypeScript Technology Profile**：在本项目 Vue / TypeScript 前端代码中采用与当前版本兼容的客观语义约束与工程默认值，并将验证映射到本仓库真实命令和风险层；Research Anchor 不是升级目标。

此前已经固化的 Stale Verification Contract、Visual Fidelity、自动化验证与 Human Review Baseline 隔离、bind mount 可重复恢复、Artifact Evidence、异步 Actions 闭环、Human Review Finding 分类、外部媒体真实内容验证、后继提交 Evidence Claim 影响判断、Roadmap / GitHub 集成状态边界、共享资源真实冲突域、配置责任与已有能力复用规则继续有效。

`agentic-dev` 自身 Project Roadmap、Engineering Capability Foundation v1 状态、Issue / eval / PR 集成状态和研究过程没有被继承为 Consumer 项目事实。

## 2. Consumer 与 agentic-dev 的职责边界

- `agentic-dev` 决定可复用的“如何工作”。
- `jilinjobs-cms` 决定本项目的 Goal、Scope、Requirements、Specification、Architecture、Code、Tests、Verification 与 Integration Policy。
- `agentic-dev/docs/project/*` 中属于其自身项目的状态、路线和实验事实不得复制为 Consumer 项目事实。
- 从 `agentic-dev` baseline 吸收的方法变化，应转化为本仓库可直接执行的本地规则，而不是要求后续 Agent 持续跨仓库读取方法文档。
- 新的 `agentic-dev` 提交不会仅因存在就自动覆盖已固化的 Consumer-local 规则；只有显式 baseline 升级才重新比较并处理更新、保留或取代关系。

### 2.1 Consumer Override Boundary

Technology Profile 的应用顺序为：

1. Vue / TypeScript 等技术本身的客观语义约束；
2. 本 Consumer 已确认的实际版本、Architecture / ADR 与项目技术边界；
3. 本 Consumer 的 Repository / Engineering / Verification Authority；
4. Technology Profile 的 Engineering Default / Conditional Guidance；
5. 普通实现偏好。

当前公开站前端的实际 Authority 包括但不限于：

- `frontend/public-site/package.json` 当前版本与 scripts；
- `frontend/public-site/tsconfig.json` 当前 TypeScript 配置；
- 当前 Vue Router、多入口结构、Shared Shell、Party Theme 与 CMS API 设计；
- Element Plus 等既有依赖的 Consumer-local 使用方式；
- `docs/technical/verification-strategy.md` 和 GitHub Actions 当前真实验证路径。

不得为了匹配 Technology Profile 的 Research Anchor 机械升级 Vue、TypeScript、`vue-tsc`、Vite、Vue Router、Element Plus 或其他依赖，也不得仅为采用 Profile 迁移稳定组件、重写 API 风格或调整现有 tsconfig。

## 3. 常规 Feature 工作流

当前项目采用以下主流程：

```text
Consumer Authority / Domain Context
        ↓
Clarify Intent（仅在存在实质产品歧义时）
        ↓
Specification
        ↓
Technical Planning?（按需）
        ↓
Slice Work
        ↓
Readiness Check
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
- Execution Unit 应纵向、范围明确、可独立验证、可追溯并适合 Fresh Context。
- 实施文件、具体命令和局部施工步骤优先通过 JIT Plan 在执行时确定。
- 通用 Method 的终点是 `Ready to Integrate`；实际 merge / release / deploy 仍服从本仓库授权和策略。

### 3.1 依赖 PR 与 squash merge

当一个工作被拆成多个存在依赖关系的 PR，而最终集成采用 squash merge 时，必须区分临时 review topology 与最终 integration topology。

- 没有明显并行审查收益时，优先在父 PR 合并后从最新 `main` 创建下一层依赖 PR。
- 使用 stacked PR 时，父层 squash merge 后，child 在 Ready to Integrate 前必须规范化到实际最新 `main`，并重新确认 PR diff 只包含本层职责。
- rebase、rebuild、cherry-pick、force-update 或等价 branch normalization 后，重新读取 PR 实际 Head SHA、base、changed files / diff 和关联 Workflow。
- Head SHA 改变后 Current Evidence 按 Evidence Claim 规则重新取得或逐项判断，不复用与旧 Head 错配的 CI / Review 结果。
- 现有 PR 无法可靠刷新到新 Head 或 diff 与目标树存在歧义时，不继续合并该 PR，应基于最新 `main` 建立干净替代 PR。

## 4. Execute 阶段工程纪律

`execute-unit` 只执行并证明一个当前 Execution Unit；实施前读取当前 Authority、当前代码和真实 Verification Surface，实施后以 Current Evidence 判断完成。Engineering Discipline 由 `execute-unit` 薄消费，不建立新的独立 Skill 或人工 checklist。

### 4.1 Implementation Minimality & Speculative Complexity Control

实施选择的目标不是“最少行数”，而是**满足当前 Authority、验收、验证和工程健康前提下的最低必要复杂度**。

可以证明复杂度合理的来源包括：

- 当前 Requirement / Specification / Execution Unit 的明确行为和 Acceptance；
- 当前 Technical Plan / Architecture / ADR / Contract / Migration；
- 安全、隐私、性能、可靠性、可观察性和可测试性要求；
- 已存在的真实多个消费者、变体或共享语义；
- Repository / Engineering Rule；
- 为复用当前代码、框架、标准库或既有依赖所需的最薄适配；
- 有直接证据支持、范围很小且能降低当前实现风险的 preparatory refactor。

以下内容本身不足以证明额外复杂度：

- “以后可能有更多场景 / 变体”；
- “为了更灵活、企业级、可扩展”；
- “这里可以套一个设计模式”；
- “所有值都应该配置化”；
- “未来可能换框架 / 做插件化”；
- 仅为了与外部 Profile Research Anchor 一致而升级依赖。

失败路径、安全边界、必要验证能力、当前真实多消费者支持、Authority 要求的配置/迁移/兼容性，以及直接需要的行为保持型 preparatory refactor 不属于过度设计。

### 4.2 Surgical Change & Diff Scope Control

在 Unit 完成前执行轻量 Final Diff Scope Check。每个有意义的 Diff 区域必须至少属于：

- 当前 Unit 的产品实现；
- 当前 Unit 的验证；
- 当前变更导致的 Consumer Authority 同步；
- 当前 Unit 必需的行为保持型 preparatory refactor；
- 上述变更直接产生的 cleanup 或确定性机械更新。

默认排除相邻 typo、TODO、历史死代码、邻近但独立的 bug、命名/样式清理、全局格式化和无关优化。Preparatory refactor 只有在当前变更直接需要、行为保持可证明、范围受限且 reviewer 能与产品行为变化区分时才进入当前 Diff。

### 4.3 配置责任与已有能力复用

固定值、硬编码或准备自行实现通用能力时，不根据字面形式直接判定缺陷。至少区分：稳定领域/安全/协议/页面模板/算法常量、管理员运营数据、低频结构元数据、部署实例差异、CI/Review/发布参数。具体分类以 `docs/technical/configuration-governance.md` 为准。

实现通用能力前先检查当前代码、框架、标准库和已引入依赖。已有能力满足当前功能契约及安全、可观察性、性能和生命周期约束时优先复用，仅使用最薄适配层；存在可证明不匹配时允许自有实现，但不得为了复用扩大依赖面、改变产品行为或覆盖 Consumer Architecture Authority。

## 5. Vue 3 + TypeScript Technology Profile

### 5.1 当前 Consumer 技术基线

当前 `frontend/public-site` 以仓库实际文件为准，当前确认：

- Vue `3.5.40`；
- TypeScript `5.9.3`；
- `vue-tsc` `3.3.9`；
- Vite `8.1.5`；
- Vue Router `5.2.0`；
- Element Plus `2.14.4`；
- `tsconfig.json` 使用 `strict: true`、`moduleResolution: Bundler` 等现有设置；
- `npm run build` 实际执行 `vue-tsc --noEmit && vite build`。

这些是当前 Consumer 技术事实；Profile 的 Vue `3.5.42`、TypeScript `7.0.2`、`vue-tsc 3.3.11` 等 Research Anchor 只用于解释 Profile 研究上下文，不构成升级要求。

### 5.2 采用的规则

在当前代码真实适用时：

- props 保持单向数据流，不直接修改 props；
- props / emits 的 runtime declaration 与 type declaration 按 Vue 约束使用，存在运行时校验需求时不为简化类型而丢失校验；
- 模板引用按生命周期视为可能为空，不用无证据的非空断言掩盖生命周期事实；
- `watch` / `watchEffect` 正确处理依赖收集和异步工作；当路由、查询、生命周期或其他响应源改变后旧异步结果可能覆盖新状态时，必须建立 currentness / cleanup 语义；
- computed 保持无副作用；
- 新代码保持 TypeScript inference，避免以 `any`、无证据类型断言或强制 `reactive<T>` 逃逸；不为 Profile 清理未触达的历史类型问题；
- composable 需要可解构响应式返回值时优先保持 ref 语义；
- Vue 3.4+ 的 `defineModel`、Vue 3.5+ 的 `useTemplateRef` 只在当前组件真实受益时使用，不因此重写稳定契约；
- `<script setup>` + Composition API 是当前 SFC 的自然默认，但不为统一形式迁移未触达稳定组件；
- Profile 不定义 Element Plus API，组件库行为继续以 Consumer 当前版本、已有实现和必要官方语义为准。

### 5.3 已知误用边界

- 不把“Vite 能构建”单独等同于 Vue SFC type-safe；
- 不直接修改 props；
- 不在 computed 中放副作用；
- 不允许较慢的旧 async watcher/lifecycle 工作在依赖改变后无条件覆盖新状态；
- 不用模板引用的 `!` 掩盖生命周期空值；
- 不只依据 semver 假设 `vue-tsc` / TypeScript 兼容性，实际以 Consumer build / type-check evidence 为准。

## 6. Verification Profile 与验收闭环

每项 Specification Acceptance Obligation 必须闭环到实现责任、验证责任、计划证据和已实际执行且匹配当前 Head 的 Current Evidence。实现覆盖不等于验证覆盖。

Vue / TypeScript 变更按风险映射验证层：

- SFC template、props、emits：至少 Vue-aware type-check；有运行时行为变化时追加对应行为测试；
- reactivity / computed / composable：type-check + 能证明依赖变化后状态正确的行为测试；
- watcher / lifecycle / async side effect：type-check + 对触发、时序、旧工作失效、mount/unmount 等相关语义的行为测试；
- DOM、Router、template ref、用户交互：type-check + Browser E2E；存在视觉要求时再追加 Visual Evidence；
- build / module / tsconfig：type-check + bundler build，必要时追加 Browser Runtime；
- 仅 TypeScript 类型变化：Vue-aware type-check 为核心，是否追加行为验证取决于 Acceptance 风险。

本仓库不机械执行不存在的 Profile 命令。当前公开站与管理端 `npm run build` 都显式串联 `vue-tsc --noEmit` 与 `vite build`，因此该脚本同时产生 Vue-aware type-check 与 bundler build evidence；记录证据时仍区分两层语义，不能说“Vite 本身证明了类型正确”。Browser / Visual / Integration / Human Evidence 继续以 `docs/technical/verification-strategy.md` 和当前 Workflow 为准。

遇到验证失败时先建立 Authority-backed Expected vs Actual，并至少分类：

```text
Implementation Defect
Stale Verification Contract
Runtime / Environment Problem
External Dependency Problem
```

当 Test / Workflow assertion / fixture / snapshot 与当前更高优先级 Authority 或 Specification 冲突时，修正真正拥有陈旧断言的验证层；不能修改产品去恢复已被取代的旧行为。

## 7. 证据声明、Human Review 与视觉验收边界

- Backend / Frontend Build、Unit / Integration / Browser E2E 只证明实际覆盖行为；
- Functional Browser Verification 可证明路由、交互、资源加载和已编码断言；
- Visual Fidelity 在缺少完整机器可判定容差时，使用“原站/设计参考 → Review Runtime 截图 → AI 明显差异检查 → Human Visual Review”；
- Human Review 原始结论按实际范围记录，不扩大为无条件验收；
- Human Review Finding 重新读取 Authority 与 Product Intent 后分类为 Implementation Defect、Product / Requirement Ambiguity、Domain / Architecture Authority Gap、Runtime Problem 或 Low-risk Visual / Interaction Adjustment；
- Artifact 构成必要 Evidence 时，不只检查 upload step，还重新读取当前 Run Artifact 集合并核对名称、Run 与 Head SHA。

### 7.1 后继提交的 Evidence Claim 影响判断

祖先提交已经获得 Runtime / Human Review Evidence 后，当前目标提交出现后继变更时，默认把祖先证据视为待重新判断的旧证据。只有同时满足以下条件，才按具体 Evidence Claim 复用：

1. Evidence Commit 是当前目标提交祖先，并取得完整精确 diff；
2. 逐项证明差异不改变该 Claim 的行为、环境、数据、资源或人工判断对象；
3. 相关 Authority、Requirement、Specification、Architecture、Acceptance、Workflow、Runtime Configuration、Migration、Fixture 和版本化资源没有影响性变化；
4. 当前 Head 完成自身 targeted checks；
5. 记录 Evidence Commit SHA、Current Target SHA、compare range、原 Run / Review、可复用 Claim 与仍需重跑 Claim。

无法证明不受影响的 Claim 必须重新验证或 Review。

## 8. Human Review Environment 与外部输入

当自动化验证后还要暴露同一 Runtime 给人工评审时：

```text
Automated Verification
→ Collect Current Evidence
→ Recreate / Reset Known Database and Static Baseline
→ Seed Explicit Human Review Fixtures
→ Start / Expose Review Runtime
→ Verify Review Baseline and Access Path
```

自动测试数据不得因环境复用意外进入 Human Review。数据库和静态资源从 Flyway、版本化静态资源包或其他 Authority 可追溯来源恢复。容器写 host bind mount 时显式处理 UID/GID、ownership、permissions、cleanup 和 Reset 可重复性；调用清理命令本身不构成 Cleanup Evidence。

外部二进制/媒体资源进入版本化基线、后台资源或 Runtime 时，文件名、扩展名、URL 后缀和响应头只能作为线索；按风险使用内容签名、可靠媒体类型识别或实际解码/解析核对真实格式，必要时规范化并重新验证。

## 9. 外部操作、共享资源与异步闭环

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

- 写前读取 Source of Truth，写后重新读取验证；Mutation Response 不等于目标状态已成立。
- 多 Repository 分别判断权限；本项目具体矩阵以 `AGENTS.md` 为准。
- Workflow / Deployment / remote job 的 `queued` / `pending` / `in_progress` 都是中间状态；当前目标需要结果且仍可观察时继续有界观察。
- 失败后先取得日志、Job / Step、Artifact 等诊断证据；授权范围内使用 `systematic-debug` 完成最小修复、重跑和复验。
- 固定域名、代理名、端口、Review / Deployment slot、临时数据库等排他资源按真实冲突域设置 concurrency / lock；独立工作默认排队，只有新工作确实 supersede 旧工作且资源释放闭环可靠时才取消。
- Workflow cancellation、进程终止、锁取得或 cleanup 命令成功不能单独证明资源已经释放或归属正确；重新取得资源后核对 owner / Head / 环境与外部目标。

## 10. Project Roadmap

本项目维护 `docs/project/project-roadmap.md` 作为跨里程碑、跨 Fresh Context 的持久路线。

- Roadmap 维护项目级已完成、当前、下一步和条件性后续，不记录单个 Unit 的临时命令。
- PR open / merged、精确 Merge Commit、临时 Branch 删除等瞬时事实由 GitHub 保存。
- Ready to Integrate 前，拟集成版本中的阶段、核心目标和已决定下一步应在合并后仍成立。
- 只有集成结果改变阶段、核心目标、里程碑或已决定下一步时更新长期 Roadmap；避免形成只记录上一 PR 已合并的递归尾部变更。
- README 只提供 Roadmap 入口，不并行维护第二份易变化详细路线。

## 11. Skills

当前核心 Skills：

- `clarify-intent`
- `specify`
- `technical-plan`
- `slice-work`
- `readiness-check`
- `execute-unit`
- `systematic-debug`
- `converge`

平台专项 Skill：`github-actions-verification`。

使用规则：

- 只加载当前工作真正需要的 Skill，不机械走完整 Skill 清单；
- Skill 不得覆盖 Consumer Authority；
- `execute-unit` 在实现前读取适用 Engineering Discipline / Technology Profile，并在完成前执行 Final Diff Scope Check；
- 意外失败使用 `systematic-debug`，先区分实现缺陷、陈旧验证契约、Runtime 和外部依赖，不无证据试错；
- GitHub Actions 的触发、CI 可观察性、Artifact、容器 Runtime、Human Review Baseline、timeout / cancellation、diagnostics 或祖先 Evidence reuse 影响证据可靠性时按需应用 `github-actions-verification`；
- dispatch / rerun 成功只是 Act，不是验证完成；继续核对 Run event、Head SHA、status / conclusion、Jobs / Steps / Logs / Artifacts 与当前 PR / Branch / Commit 的对应关系。

## 12. Fresh Context 恢复顺序

1. `AGENTS.md`；
2. `README.md`；
3. `docs/project/project-roadmap.md`；
4. `docs/project/development-method.md`；
5. 当前工作直接相关的 Requirement / Specification / Technical Plan / Work Artifact；
6. 当前代码、测试、Branch / PR / CI 等 Current Evidence；
7. 只在当前任务真实需要时加载对应 Skill / Consumer-local Engineering / Technology Profile 规则。

不得依赖历史聊天或个人记忆补充未固化的项目事实。

## 13. baseline 升级规则

只有项目负责人明确要求更新 `agentic-dev` baseline 时才执行升级：

1. 读取并记录指定分支的精确 commit；
2. 比较本项目当前 baseline 到新 baseline 的 Method、Operating Guide、Engineering Discipline、Technology Profile、Contract 与 Skill 变化；
3. 区分跨项目可复用资产与 `agentic-dev` 自身 Project Rule；
4. 根据 Consumer 真实需要和现有 Authority 选择性采纳，不机械复制完整文档体系；
5. 将具有持续约束价值的规则固化到 Consumer 可发现 Authority，并显式处理旧规则更新、保留或取代；
6. 同步更新 `AGENTS.md`、本文、Verification Strategy 和 Roadmap 的 baseline / 方法记录；
7. 完成升级后恢复以 Consumer-local Authority 为普通开发入口，不自动继承 `agentic-dev` 自身 Project Roadmap、Foundation 状态、Issue、实验状态或其他项目事实。