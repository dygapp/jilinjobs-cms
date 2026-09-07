# E1～E3 前置 Repository Authority 与 Migration Architecture 收敛规划

## 1. Status

- Planning source：GitHub Issue #92
- Related architecture authority：GitHub Issue #77
- Downstream candidates：GitHub Issue #60 / E1～E3
- Stage：**Planning Authority — ACTIVE / Phase 1 IN PROGRESS**
- Phase 0：**COMPLETED**
- Phase 1 current audit / Authority Clarification：**COMPLETED**
- Phase 1 Slice A / EU-43：**COMPLETED**
- Phase 1 Slice B / EU-44：**READY / NOT STARTED**
- Current execution：**EU-44 — Canonical Product Authority Consolidation**
- Next Gate：**Fresh Context EU-44 Execute after base-drift revalidation**
- Phase 1 audit / Ready Specification：`docs/project/documentation-authority-convergence.md`

本文固化 EU-42 之后、Issue #60 / E1～E3 重新进入正式规划之前的总体演进顺序。本文中的 Phase / Planned Unit 名称都只是 Planning identity；只有经过 `slice-work → readiness-check` 的具体 Execution Unit 才授予 Execute 权限。

后续仍严格遵守：

```text
Planning / Requirement Candidate
  → Requirement / Specification / 必要 Technical Planning
  → slice-work
  → Candidate Execution Unit
  → readiness-check
  → Ready Execution Unit
  → Fresh-context Execute
```

## 2. Why this planning layer exists

EU-37～EU-42 已完成 Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层边界中的 Site Package foundation、stable structure、Navigation stable identity、Runtime composition、one-time bootstrap、Generic Schema separation 与 stable Site asset ownership。

EU-42 后 current audit 进一步确认两个不能直接留给 E1～E3 的长期问题：

1. Repository 文档中 Current Authority、阶段性 Amendment、历史 Work Record 与已 superseded 状态并列存在，Root README 也承担了过多 Roadmap / Authority index / history 职责；
2. Party-specific Historical Migration Application 仍位于 Backend production `src/main`，而未来 Main migration 需要一个 site-neutral、可复用且独立运行的 Generic Content Migration Application。

因此旧的“直接进入 Canonical Migration Compatibility & E1～E3 Re-entry”顺序已经被 supersede，需要先完成 Documentation Authority 与 Historical Migration / Backend Application Boundary 收敛。

## 3. Overall sequence

```text
Phase 0  Planning Authority Solidification — COMPLETED
               ↓
Phase 1  Repository Documentation Authority Convergence — IN PROGRESS
               ↓
Phase 2  Generic Historical Migration & Backend Application Boundary
               ↓
Phase 3  Canonical Migration Compatibility & E1～E3 Re-entry Gate
               ↓
        Issue #60 / E1～E3

后置：
Phase 4  Repository Split Readiness Assessment
```

## 4. Phase 0 — Planning Authority Solidification — COMPLETED

目标：让后续 Fresh Context 可以只从 GitHub Repository 恢复新的实际路线，而不是从聊天记录恢复。

Phase 0 已完成：

- Issue #92 成为跨 Issue 总体规划入口；
- Issue #77 保持四层架构边界 Authority，但 Historical Migration remaining path 对齐 Issue #92；
- Project Roadmap 与 `site-package-planning.md` 同步新的实际路线；
- 本计划进入 Repository；
- 建立最小、实验性、可撤销的 bounded architecture-review eval；
- AR-02 / AR-04 current evidence 已形成，模型输出只作为附加 Review Evidence，不创建新的 Method Stage，不替代 readiness-check。

Phase 0 没有形成实现 EU，也没有进入 Issue #60 / E1～E3。

## 5. Phase 1 — Repository Documentation Authority Convergence — IN PROGRESS

Phase 1 的 current audit、分类规则、dependency closure 与 slice result 以：

```text
docs/project/documentation-authority-convergence.md
```

为当前 Authority。

### Unit 1A — Canonical Authority Audit & Reconciliation

Current audit 已覆盖：

- Root `README.md`；
- `docs/requirements/**`；
- `docs/specifications/**`；
- `docs/technical/**`；
- `docs/work/**`；
- `data-migrations/**` 中当前边界说明；
- 必要 subtree README gap。

每份文档按以下角色分类：

```text
CURRENT
PARTIALLY_CURRENT
SUPERSEDED
HISTORICAL_EVIDENCE
```

规则：

- `PARTIALLY_CURRENT` 不能直接归档；必须先把仍有效语义吸收进 canonical Current Authority；
- Requirement 的 archive 门槛最高，不能因为对应 EU 已完成就自动归档；
- 不为目录整洁机械做全仓搬迁；
- current audit 识别出的 high-risk drift 包括 EU-41 后旧 Flyway Site Data responsibility、EU-42 后旧 `site-baseline/static/**` ownership、historical migration lifecycle 被描述为 Current、旧 Slice D direct-next-step，以及过期 architecture-review experiment status；这些已由 EU-43 关闭；EU-30 Amendment consolidation 由 EU-44 承担。

`slice-work` 将 1A 拆为 dependency-ordered slices：

1. **EU-43 — Current Authority Semantic Reconciliation — COMPLETED**：已修复四层 boundary / lifecycle / migration / asset / verification currentness；
2. **EU-44 — Canonical Product Authority Consolidation — READY / NOT STARTED**：原 Slice B；将 EU-30 confirmed Amendment 与 EU-41/EU-42 ownership 折回 canonical product/CMS/Backend Authority，精确 scope / acceptance / verification / non-goals 见 `docs/work/eu44-canonical-product-authority-consolidation.md`；
3. Phase 1B 只有在前述 semantic currentness 完成后才进入。

EU-43 Execute Authority 已随完成终止。EU-44 基于当前 Repository state 独立通过 Readiness；其 Execute Authority 只覆盖 canonical Authority consolidation，不授权 Phase 1B / Phase 2 / E1～E3。

### Planned Unit 1B — Documentation Information Architecture & Archive Migration

在 1A 完成 Current Authority 接管后再做物理整理。

目标信息架构候选：

```text
docs/
├── README.md
├── requirements/
│   ├── README.md
│   ├── <current canonical requirements>
│   └── archive/
├── specifications/
│   ├── README.md
│   ├── <current canonical specifications>
│   └── archive/
├── technical/
│   ├── README.md
│   ├── <current canonical technical authority>
│   └── archive/
└── work/
    ├── README.md
    ├── current/
    └── archive/
```

同时：

- Root README 收敛为稳定项目入口，不再复制 Roadmap / EU history；
- `docs/README.md` 成为 Authority Map；
- 为 `backend/`、`frontend/`、`sites/`、`sites/jilinjobs/` 等真实语义边界增加必要 README；
- 更新 Current Authority 链接；
- `archive/**` 默认不参与 Fresh Context Current Authority 恢复；
- Git history 与 Historical Work / Archive 负责追溯，不长期维护多份并列 Current 状态。

1B 当前仍是 Planning Candidate，不继承 EU-44 Execute Authority。

## 6. Phase 2 — Generic Historical Migration & Backend Application Boundary

### Planned Unit 2A — Backend Application / Core Boundary Foundation

2A 的目标不是预先选择某种目录形式，而是先建立三个长期可验证的工程边界：

1. CMS Server 与 Historical / Generic Content Migration 使用不同 application lifecycle 与 Spring composition root；
2. 两个 application 共享 site-neutral CMS Domain / persistence capability，但 compile/runtime dependency 只能单向指向共享 Core；
3. Server application 不再包含 Party migration runtime responsibility，Migration application 也不再通过完整 Server application composition 获得依赖。

当前**推荐 Architecture Candidate**是最小 Gradle multi-project：

```text
backend/
├── modules/
│   └── cms-core/
└── apps/
    ├── cms-server/
    └── content-migration/
```

依赖方向：

```text
cms-server app ──────────→ cms-core
content-migration app ───→ cms-core
```

推荐 multi-project 的理由是标准 project dependency、独立 compile/runtime classpath、独立测试/打包任务与清晰 composition ownership，而不是“存在两个 executable JAR 就逻辑必然要求三个 Gradle project”。

Technical Planning 必须同时显式评估一个较低复杂度替代方案：在单 Gradle project 内使用共享 core source set + 独立 server / migration source sets，并分别配置依赖、resources、tests 与 `BootJar.mainClass`。如果该方案可以用少量、清晰、长期可维护的 Gradle wiring 满足同等 dependency / classpath / composition isolation，则允许选择它；只在同一个完整 `main.runtimeClasspath` 上增加多个 BootJar / main class 不构成有效替代，因为它只分离启动入口，不分离责任与运行时装配。

最终 build shape 必须在 2A 的 Technical Planning / readiness 前冻结；当前 Planning 不把 multi-project 当成 Requirement 事实。

#### 2A ownership contract

`cms-core` 应是普通 reusable library，候选责任包括：

- site-neutral domain model / validation；
- Article / Column / List / Resource 等可复用查询与写入服务；
- Generic persistence mapper / adapter 与 transaction capability；
- Resource safety / storage 等两个 application 都真实需要的能力；
- Generic CMS Schema 的唯一资源 Authority，以及经现有 Authority 接受的 site-neutral provisioning capability。

`cms-server` 持有：

- Server application composition root；
- HTTP Controller；
- MVC / static-resource / Web-only configuration；
- Public / Admin API transport composition；
- 普通 Server runtime startup hooks。

`content-migration` 持有：

- Migration application composition root；
- CLI / command dispatch；
- import orchestration、report、exit semantics；
- migration-only compatibility behavior；
- 2A 期间完整保留当前 Party historical / carousel migration 行为，待 2B / 2C 再泛化与去专用化。

“Generic CMS 产品能力属于 Core”不等于 Controller / MVC / transport class 必须进入 `modules/cms-core` build artifact；产品边界与 build artifact ownership 必须区分。

#### 2A Spring / persistence / resource contract

当前 Party migration 使用：

```text
SpringApplicationBuilder(CmsApplication::class.java)
    .web(WebApplicationType.NONE)
```

`WebApplicationType.NONE` 只关闭 Web server，不隔离根包 component scan、Server configuration 或 startup side effect。因此 2A 不允许仅移动源文件后继续依赖相同 `CmsApplication` composition 并声称边界完成。

2A Technical Planning 至少必须冻结：

- 两个 app 的独立 application / composition root；
- core / server / migration configuration 的显式导入或受限 scan；
- `@MapperScan` / mapper XML / mapper resource discovery；
- configuration properties / Jackson / transaction manager ownership；
- Generic Flyway SQL 的**单一** Authority，不允许两个 app 维护复制 migration lineage；
- Server 与 Migration 分别是执行 Flyway、validate，还是要求外部 Schema ready；
- `Generic Schema → Site Package reconcile → optional one-time bootstrap → Historical Migration` 的生命周期顺序；
- shared `application.yml` / resource 的加载与覆盖规则；
- Site Package root 等现有相对路径、Gradle `JavaExec` / verification task 路径在项目移动后的重新定位；
- domain transaction 与 migration 文件写入的不同回滚语义：数据库 `@Transactional` 不自动回滚已写入文件，preflight / compensation / failure semantics 必须按真实副作用定义。

允许暂时保留 migration 对 `MultipartFile` 等 Spring Web abstraction 的薄类型依赖，只要不因此加载完整 server Web composition；是否进一步抽出更薄 stream/input contract 只在拆分确有必要时处理，不借机扩大架构重构。

#### 2A behavior-preservation boundary

2A 是 preparatory refactor，不是 Generic Engine 实现：

- 当前四个 `Party*Migration*` 文件及其 V1/V2 historical / carousel compatibility 语义先整体迁入 Migration application responsibility；
- 必须保留当前多个 migration entry / task 能力；是否收敛成单一 launcher + subcommand 需要 Technical Planning 明确，不能在搬迁中隐式丢失入口；
- 不改变 canonical schema、legacy identity / fingerprint、report、conflict、idempotency、LINK→ARTICLE upgrade-only policy；
- 不为了 module 整洁放宽 ordinary Admin/API edit contract；
- 不改 content / column / listing / resource 等领域 package 结构，除非是建立 application/core dependency boundary 的最低必要移动；
- 不新增 persistence/API/SPI 子模块、plugin framework、Clean Architecture 或 Git Repository split。

#### 2A verification obligations

至少验证：

- shared Core library 与两个 application artifact 均可独立构建；
- `cms-server` executable 正常 `java -jar` 启动并保持 Public/Admin/API/Site Package/Flyway 当前行为；
- `content-migration` executable / CLI 可以独立启动、无 HTTP server、正常退出；
- Migration runtime 不发现 Server Controller / server-only startup hooks；
- mapper、transaction、configuration properties、Jackson、Flyway / schema policy 与 shared resources 实际装配正确；
- dependency direction / classpath 不允许 core 反向依赖 app，也不允许 migration 依赖 server；
- Fresh DB 与既有 compatible DB 的 schema / Site Package lifecycle 不发生无意变化；
- current canonical import、second import idempotency、changed-fingerprint conflict、183 Articles、4 carousel、EU-29→EU-30 compatibility 与 resource integrity 保持；
- 受影响 Public/Admin/API / browser regression 重新取得 Current Evidence。

上述边界在 `slice-work` / `readiness-check` 前仍需基于最新代码完成 dependency closure 与 resource inventory；AR-02 review 不是 Readiness PASS。

### Planned Unit 2B — Generic Content Migration Application

职责：只负责 `Canonical Migration Dataset → CMS Runtime`。

Generic capability 至少覆盖：

- canonical manifest / index / schema validation；
- path traversal / bounds safety；
- size / SHA-256 integrity；
- stable migration identity / fingerprint；
- preflight before writes；
- transaction / file-side-effect boundary；
- Article / Resource / ListItem 等依赖顺序；
- legacy mapping；
- first import；
- second import idempotency；
- changed fingerprint conflict；
- reconciliation / report；
- site-neutral fixture verification。

Generic Engine 不得内建 Party / JilinJobs / EU-29 / EU-30 identity。

辅助工具边界：

```text
Legacy Source
   ↓
Python / Node collection / normalization / promotion
   ↓
Canonical Migration Dataset
   ↓
JVM Generic Content Migration Application
   ↓
CMS Runtime
```

Python / Node 可以负责 source discovery、HTML/JSON normalization、candidate merge、promotion、schema validation、hash / fingerprint 与 reports；不得通过直接数据库写入重新实现 CMS Domain semantics。

### Planned Unit 2C — Party Migration De-specialization & Compatibility

当前 Backend production main 的：

- `PartyCarouselMigration.kt`
- `PartyCarouselMigrationV2.kt`
- `PartyHistoricalContentMigration.kt`
- `PartyHistoricalContentMigrationV2.kt`

应退出 `cms-server` Runtime responsibility。

Party-specific aliases、carousel identity、EU-29 / EU-30 accepted fingerprints、LINK→ARTICLE migration-only transition 等必须保持明确的 site/history-specific responsibility，可由 `data-migrations/party/**` 的 dataset / profile / compatibility metadata 承载，不能通过改名隐藏进 Generic Engine。

保持义务：

- 183 篇 Party current Runtime Dataset；
- 4 条 accepted carousel；
- current canonical import；
- second import idempotency；
- changed-fingerprint conflict；
- EU-29 → EU-30 upgrade compatibility；
- resource integrity；
- Runtime / Browser reconciliation。

## 7. Phase 3 — Canonical Migration Compatibility & E1～E3 Re-entry Gate

最终验证链：

```text
Generic CMS Schema
  → JilinJobs Site Package stable structure
  → one-time Site bootstrap
  → Generic Content Migration Application
  → Party Canonical Dataset
  → Runtime
  → Replaceable Public Renderer
```

Gate 需要覆盖：

- Fresh DB；
- empty Runtime static root；
- Site Package reconcile / stable asset projection；
- one-time bootstrap；
- canonical import；
- repeated import；
- accepted upgrade compatibility；
- current accepted counts/state；
- browser behavior；
- Public Renderer 不依赖 migration implementation knowledge。

如果所有 obligation 已由 Current Evidence 闭环，则直接记录 compatibility closure 与 E1～E3 re-entry PASS，不为形式制造新的 implementation EU；如果存在具体 gap，再由 `slice-work` 形成最小 Candidate Execution Unit。

## 8. Phase 4 — Repository Split Readiness Assessment

保持独立、后置。只有四层边界完整闭环后评估：

- CMS Core 是否具有独立 Repository 价值；
- Site Package / Public Renderer / Docs 是否适合进一步拆分；
- multi-repo CI / Review / release composition 成本；
- split 的实际消费者、发布与团队证据。

Assessment 不等于自动拆仓，也不默认阻塞 E1～E3。

## 9. Architecture Review Eval Experiment

Consumer-local Eval 入口：

```text
evals/README.md
evals/architecture/pre-e1e3-convergence-review.json
evals/architecture/ar04-migration-sequencing-review.json
evals/run_architecture_review.py
evals/run_ar04_review.py
```

### Current experiment evidence

当前已有两个真实 paired scenario：

#### AR-02 — Backend Application / Gradle Module Boundary

- exact Planning Head：`52d8d59094eb5f02a2780ccf131363adac08a1dc`；
- lower-cost baseline requested `gpt-5.6-sol` / medium；
- high-capability review requested `gpt-6-astra` / high；
- 相同 bounded context / prompt；
- 两次 Human Semantic Verdict 均为 `SUPPORTED_WITH_CHANGES`；
- 八条 assertions 均 PASS；
- 共同 findings 已用于修订 2A；
- Astra 提供少量增量细化，但没有独占 blocking discovery；
- Codex JSONL 未暴露可独立确认的 runtime model / effort，因此只记录 requested model / effort，不把它夸大为 runtime trace 已二次证明。

#### AR-04 — Migration Sequencing Blind Discovery

- 使用已知缺陷被发现前冻结的两阶段 migration extraction candidate，避免把答案或 assertions 泄漏给 runtime reviewer；
- `gpt-5.6-sol` / medium 与 `gpt-6-astra` / high 均独立给出 `BLOCKING_CONCERN`；
- Human discovery = `DETECTED`；
- hidden assertions 两次均 `8/8 PASS`；
- 两个模型都发现了 sequencing / application responsibility flaw，Astra 未发现 Sol/medium 漏掉的 blocking flaw。

AR-04 证明该已知高返工成本缺陷**不需要** Astra 才能被发现，因此当前实验结论保持：**ADJUST**。

### Current routing candidate

```text
bounded eval when justified
→ lower-cost capable review first
→ escalate only if unresolved / conflicting / deliberate second opinion
```

规则：

1. High-capability review 不进入普通 Development Method、Readiness Gate 或每个 Execution Unit 的默认步骤；
2. 只在真实高返工成本架构问题确有独立挑战价值时使用 bounded eval；
3. 默认先使用当前足够能力且成本较低的模型；第一轮仍有 unresolved ambiguity、冲突证据或明确需要独立 second opinion 时再升级 Astra；
4. 不为了“完成 corpus”机械执行 AR-01 / AR-03；真实 planning 问题出现时才重新启用；
5. 每个 scenario 独立 Fresh / ephemeral run，只复制显式 context paths；assertions / expected behavior / provenance / historical results 不进入 runtime workspace；
6. 禁止模型 review 修改仓库或重规划整个项目；
7. process exit `0` 不等于 PASS，必须人工逐条语义评分；
8. 模型输出只构成 Review Evidence，不替代 Repository Authority 与 readiness-check。

Evidence contract 继续至少保存：exact Planning Head、scenario id、requested model / effort、CLI version、context path list + digest、command argv、stdout JSONL、stderr、return code、人工 assertion grading、Planning Impact，以及 runtime model/effort 不可观察时的 `NOT OBSERVABLE` 标记。

具有跨项目复用价值的 model-routing / blind paired-eval evidence 已提交到 `dygapp/agentic-dev` Issue #71。该反馈只构成外部 Evidence Candidate，不自动修改本 Consumer Method，也不授权当前 Consumer 修改 `agentic-dev` 文件、Branch、PR 或 Workflow。

## 10. Fresh Context and gates

默认一个 Ready Execution Unit 使用一个新的 Fresh Context。

新会话提示词只承担 Locator / Handoff：声明 Fresh Context、目标 Repository、必要的前置读取与当前 Planning Issue；任务状态、Execution Unit、Readiness 与下一实际步骤都必须从 GitHub 当前 Authority 恢复，不能复制本计划的状态摘要进提示词。

Current Phase 1 Gate：

1. Phase 0 已完成，Issue #92、Issue #77 与 Project Planning 文档表达同一总体路线；
2. 旧 Slice D 不再被描述成 EU-42 后无前置工作的直接下一步；
3. Phase 1 current audit 与 Slice A / EU-43 已完成；Slice B 已通过 `slice-work → readiness-check` 形成 **EU-44 — Canonical Product Authority Consolidation — READY / NOT STARTED**；
4. 当前唯一 Ready Execution Unit = **EU-44**；下一实际步骤是在新的 Fresh Context 重新核验 `main`、Issue #92、EU-44 work artifact、Open PR / Actions 与 base drift，若 Readiness 仍有效则 Execute EU-44；
5. EU-44 完成后才允许进入 Phase 1B Information Architecture / Archive Migration；
6. Phase 1 全部收口前不得进入 Phase 2，Phase 3 compatibility Gate 前不得进入 Issue #60 / E1～E3。