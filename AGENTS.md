# AGENTS.md

## 仓库身份

dygapp/jilinjobs-cms 是吉林省智慧就业云平台“信息发布与网站服务”相关能力的 Consumer Repository。

本仓库自己拥有 Product、Requirement、Architecture、Specification、Design、Technical、current work、技术政策、授权与项目约束。agentic-dev 只提供已安装的通用 Skills 与按需 Guide，不拥有本项目事实。

## Fresh Context 恢复

开始新的工作时按最小上下文恢复：

1. 明确定位当前 jilinjobs-cms Repository / Runtime，并读取本文件；
2. 读取 docs/README.md，按当前任务找到最小必要的 Current Authority；
3. 只有任务涉及正在进行的正式工作时，才读取 docs/work/current/README.md 并重新核验相关 Branch / PR / Actions；
4. 需要通用执行 procedure 时，通过宿主原生 Agent Skills discovery 使用 .agents/skills/**；
5. 读取当前任务实际适用的 Consumer-local constraints：全局入口为 docs/governance/constraints.md，path-scoped 约束由对应 subtree 的 AGENTS.md 持有；
6. 只有当前任务是 agentic-dev adoption / upgrade，或明确需要方法论导航时，才读取 docs/project/agentic-dev.md 指向的 exact-version Guide。

不得把其他聊天、个人记忆、其他 Repository、其他 checkout 或未经纳入本仓库的领域假设当作当前项目事实。

## Repository Authority 边界

不同责任先读取其真实 owner，不建立单一中央文档替代所有事实：

- README.md：稳定项目目标、范围与主要目录入口；
- docs/requirements/index.md：当前 Product / Domain Requirement owner 的索引；
- docs/specifications/**：具体 Feature / surface 的可观察行为与验收；
- docs/architecture/cms-architecture.md、docs/architecture/decisions/**：长期系统结构与 ADR；
- docs/architecture/requirement-authority.md：本项目 Requirement ownership / lifecycle；
- docs/design/**：当前设计权威；
- docs/technical/**：长期 implementation / interface / verification contract；
- docs/project/project-roadmap.md：durable planning direction；
- docs/project/project-evolution.md：稳定历史摘要；
- docs/work/current/README.md：仅在使用正式 Execution Unit 时定位当前 active work；
- Code / Tests / Runtime：证明当前实现状态，不反向发明 Product Requirement；
- GitHub Branch / PR / Issue / Actions：拥有各自原生瞬时状态，不取代长期 Repository Authority。

多个 Current owner 对同一语义给出无法消歧的不兼容结论时失败关闭，并返回对应 owner 或人工权威处理。

## agentic-dev 采用边界

当前 adopted version、不可变版本绑定、安装 provenance 与 exact-version Guide locator 统一记录在 docs/project/agentic-dev.md。

Consumer runtime 只安装 .agents/skills/**。普通工作：

- 使用宿主原生 Skill discovery；
- 不维护 Provider Method / Rule / Capability / Release / Gate runtime；
- 不在线读取 Provider docs/** 来补齐 Skill procedure；
- 不继承 upstream Roadmap、Issue、PR、Research、Eval 或 self-adoption 状态；
- Skill 的存在不扩大 Scope、执行授权、merge / release / deploy 权限。

## 工作粒度

目标、Scope、Acceptance 与适用 Authority 已清楚，且修改局部、低风险、可逆时，可以直接实施并按影响范围验证；不因为修改代码或文档就机械创建 Method artifact、Execution Unit 或 Gate。

只有工作确实需要独立恢复、依赖协调或独立验收生命周期时，才形成正式 Execution Unit。docs/work/current/README.md 为 NONE 只表示当前没有正式 active Unit，不阻止用户明确授权的新工作或有界直接变更。

## 本地开发分支与集成

在本地或受控 WebCodex Repository Runtime 中执行日常开发时，默认不使用 Pull Request 作为集成机制：

- 简单任务：修改局部、低风险、可逆且无需独立恢复时，允许直接在 main 修改；完成适用验证后直接 commit，并按授权 push main；
- 复杂任务：跨多个责任区、涉及架构 / 数据迁移 / CI 治理、需要长链路验证、独立回滚或独立恢复时，先创建独立 task branch；完成验证后在本地集成回 main，并按授权 push main；
- 只有用户明确要求远程 Review、多人协作确实需要 PR，或外部仓库规则强制要求时才创建 PR。

未提交工作树允许进入本地 Docker 验证。验证对象必须绑定实际 tracked / modified / untracked 且未被 ignore 的输入 bytes；dirty worktree 的结果只能声明为“当前 HEAD + worktree fingerprint”，不能冒充 exact-HEAD PASS。需要 Integration / Completion 级 exact-Head 证据时，先形成目标 commit，再在干净工作树对该 exact Head 重新执行要求的验证。

## Repository 操作与权限边界

项目负责人已持续授权 dygapp/jilinjobs-cms 的日常 Repository 操作，包括读取和修改文件、创建 Branch / Commit / Issue / PR、Push 已验证变更、运行或观察 GitHub Actions，以及在满足本仓库验证和集成要求时执行正常仓库内操作。

该授权不自动包括 Production Deployment、Secrets / Credentials 操作、与当前任务无关的外部副作用，或明显破坏性且难以恢复的操作。

涉及 dygapp/agentic-dev 时：

- 允许读取 Repository、Tag、Commit、Issue、PR 与文件；
- 允许在需要时创建或追加反馈 Issue；
- 禁止修改其文件；
- 禁止创建或更新其 Branch、Commit、PR；
- 禁止运行、重试或改变其 GitHub Actions 状态。

涉及其他 Repository 时必须分别确认授权，工具具备写权限不等于任务已获授权。

## 人工升级边界

普通、低影响、可逆的工程选择由 Agent 自主处理并连续推进。只有决定会实质改变以下内容，或缺少不可替代的人类输入 / 权限时才请求人工：

- Product Goal / Scope；
- User-visible Behavior / Business Boundary；
- Acceptance Result 或重大非功能义务；
- Major Architecture Direction；
- Security / Privacy sensitive 行为；
- 难逆、破坏性或超出当前授权的外部状态。

请求人工前先检查当前 Repository、GitHub、Runtime 与已授权工具是否已经能够取得事实或完成动作。

## 验证与沟通

成功、完成、修复、PASS 或 Ready 类声明必须由与声明类型和目标提交匹配的当前证据支持。长期验证原则见 docs/technical/verification-strategy.md。

面向人的项目文档、Issue / PR / Review 与状态说明默认使用自然中文；精确路径、id、SHA、API / CLI、协议值、稳定状态值和外部正式名称保持原样。
