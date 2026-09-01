# Consumer 开发方法与 Skill 使用规则

本文固化 `jilinjobs-cms` 在持续开发阶段实际采用的 AI Agent 开发方法、Skill 使用规则和上下文恢复规则。

本文是 Consumer-local 规则。后续开发默认优先读取并遵循本仓库 `AGENTS.md`、`README.md`、本文及其他当前 Authority；不需要在每次普通开发工作中重新读取 `dygapp/agentic-dev`。只有项目负责人明确要求升级 `agentic-dev` baseline，或当前 Consumer 文档明确无法回答方法问题时，才重新读取指定 baseline 并将需要长期保留的变化回写到本仓库。

## 1. 方法来源与当前基线

```text
Repository:
dygapp/agentic-dev

Baseline:
master@a82e559cb67cafbcf96265a70a1167a9a75db5ba
```

该 baseline 提供 Method、Operating Guide、Skill Contracts 与 Skills 的来源依据，但不提供本项目的业务事实。

相对上一 baseline `bf21c7bcd711fd667c43007a72fae65750d1af09`，本次实际吸收的新增规则是：

- 共享外部资源的并发边界必须匹配真实冲突域，而不是逻辑 PR / Branch / ref 标识；固定域名、代理名、端口、评审 / 部署槽位、临时数据库或单例服务等资源被不同触发路径共同使用时，所有争用者进入同一排他边界；独立工作默认有界排队，只有新工作确实取代旧工作且取消后的资源释放闭环可靠时才取消；Run cancellation、进程终止、锁取得与外部资源实际释放 / 归属正确必须分别验证。
- 实施判断不把‘硬编码’或‘自行实现’脱离上下文直接视为缺陷：先根据真实变化来源、维护者、稳定性、安全 / 协议约束和生命周期判断代码常量、CMS 运营数据、结构元数据、Spring 外部化配置或 CI / Deployment Variables 的责任归属；没有已证明外部维护责任的稳定常量默认保留在代码或既有权威载体中，不为消除字面量机械配置化。
- 实现通用技术能力前先检查当前代码库、框架、标准库与已引入依赖；已有能力满足功能契约及安全、可观察性、性能和生命周期约束时优先复用，用最薄适配层承载项目差异；存在可证明的不匹配时可以采用自有实现，但不得为了复用扩大依赖面、改变产品行为或覆盖 Consumer Architecture Authority。

此前 baseline 已固化的 Stale Verification Contract、Visual Fidelity、自动化验证与 Human Review Baseline 隔离、bind mount 可重复恢复、Artifact Evidence、异步 Actions 闭环、Human Review Finding 分类、外部媒体真实内容验证、后继提交 Evidence Claim 影响判断与 Roadmap / GitHub 集成状态边界继续有效。

`agentic-dev` 自身 Project Roadmap、Issue / eval / PR 集成状态等没有被继承为 Consumer 项目事实。

## 2. Consumer 与 agentic-dev 的职责边界

- `agentic-dev` 决定可复用的“如何工作”。
- `jilinjobs-cms` 决定本项目的 Goal、Scope、Requirements、Specification、Architecture、Code、Tests、Verification 与 Integration Policy。
- `agentic-dev/docs/project/*` 中属于其自身项目的状态、路线和实验事实不得复制为 Consumer 项目事实。
- 从 `agentic-dev` baseline 吸收的方法变化，应转化为本仓库可直接执行的本地规则，而不是要求后续 Agent 持续跨仓库读取方法文档。
- 新的 `agentic-dev` 提交不会仅因存在就自动覆盖已固化的 Consumer-local 规则；只有显式 baseline 升级才重新比较并处理更新、保留或取代关系。

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

## 10. Skills

当前 baseline 的核心 Skills：

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

使用规则：

- 只加载当前工作真正需要的 Skill；
- 不要求每个工作都走完整 Skill 清单；
- Skill 不得覆盖 Consumer Authority；
- 遇到实现阶段的意外失败时，使用系统化调试路径，而不是无证据试错；
- 当失败来自 Test / Workflow assertion 等 Verification Artifact 时，`systematic-debug` 必须先核对其与当前 Authority 的一致性，允许并要求在证据支持时识别 Stale Verification Contract；
- 当 GitHub Actions 的触发、CI 可观察性、Artifact、容器 Runtime、Human Review Baseline、timeout / cancellation、diagnostics 或祖先 Evidence reuse 会影响证据可靠性时，按需应用 `github-actions-verification`；
- 如果调用要求实际完成 GitHub Actions 验证，dispatch / rerun 成功只是 `Act`，不是 Skill 退出条件；仍可观察的 `queued` / `pending` / `in_progress` Run 必须继续有界观察；
- 观察中持续核对 Run event、Head SHA、status / conclusion、Jobs / Steps / Logs / Artifacts 与当前 PR / Branch / Commit 的对应关系，只使用与当前目标提交真实关联的 Evidence；
- Run 失败且修复已获授权时，取得诊断证据后进入 `systematic-debug`，完成最小修复、重跑和复验；
- 如果调用只要求设计或优化验证路径而不要求实际执行，应返回 Evidence Retrieval Plan，并明确实际 Completion Evidence 尚未取得，不把计划写成已执行结果。

## 11. Fresh Context 恢复顺序

新的开发上下文默认按以下顺序恢复：

1. 读取根目录 `AGENTS.md`；
2. 读取 `README.md`；
3. 读取 `docs/project/project-roadmap.md`，确认当前路线和当前目标；
4. 读取 `docs/project/development-method.md`；
5. 读取与当前工作直接相关的 Requirement / Specification / Technical Plan / Work Artifact；
6. 读取当前代码、测试、Branch / PR / CI 等 Current Evidence；
7. 只在当前任务真实需要时加载对应 Skill。

不得依赖历史聊天或个人记忆补充未固化的项目事实。

## 12. baseline 升级规则

只有项目负责人明确要求更新 `agentic-dev` baseline 时，才执行 baseline 升级。

升级时：

1. 读取并记录 `agentic-dev` 指定分支最新精确 commit；
2. 对比本项目当前 baseline 到新 baseline 的 Method、Operating Guide、Contract 与 Skill 变化；
3. 区分跨项目可复用资产与 `agentic-dev` 自身 Project Rule；
4. 根据 Consumer 真实需要和现有 Authority 选择性采纳，不机械复制完整文档体系；
5. 将具有持续约束价值的已采纳规则固化到 Consumer 可发现的 Authority 中，并显式处理旧规则的更新、保留或取代；
6. 同步更新 `AGENTS.md`、本文、Verification Strategy 和 Roadmap 的 baseline / 方法记录；
7. 完成升级后恢复以 Consumer-local Authority 为普通开发入口，不自动继承 `agentic-dev` 自身 Project Roadmap、Issue、实验状态或其他项目事实。
