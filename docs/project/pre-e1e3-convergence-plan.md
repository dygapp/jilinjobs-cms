# E1～E3 前置 Repository Authority 与 Migration Architecture 收敛规划

## 1. Status

- Planning source：GitHub Issue #92
- Related architecture authority：GitHub Issue #77
- Downstream candidates：GitHub Issue #60 / E1～E3
- Stage：**Planning Authority — ACTIVE**
- Current Ready Execution Unit：**NONE**

本文固化 EU-42 之后、Issue #60 / E1～E3 重新进入正式规划之前的总体演进顺序。本文中的 Phase / Planned Unit 名称都只是 Planning identity，不是 Execution Unit Identifier，也不授予 Execute 权限。

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

因此旧的“直接进入 Canonical Migration Compatibility & E1～E3 Re-entry”顺序已经不足，需要先完成 Documentation Authority 与 Historical Migration / Backend Application Boundary 收敛。

## 3. Overall sequence

```text
Phase 0  Planning Authority Solidification
               ↓
Phase 1  Repository Documentation Authority Convergence
               ↓
Phase 2  Generic Historical Migration & Backend Application Boundary
               ↓
Phase 3  Canonical Migration Compatibility & E1～E3 Re-entry Gate
               ↓
        Issue #60 / E1～E3

后置：
Phase 4  Repository Split Readiness Assessment
```

## 4. Phase 0 — Planning Authority Solidification

目标：让后续 Fresh Context 可以只从 GitHub Repository 恢复新的实际路线，而不是从聊天记录恢复。

Phase 0 包含：

- Issue #92 作为跨 Issue 总体规划入口；
- Issue #77 保持四层架构边界 Authority，但修正 Historical Migration 的剩余顺序；
- Project Roadmap 与 `site-package-planning.md` 同步新的实际下一步；
- 固化本计划；
- 增加最小、实验性、可撤销的 High-Capability Architecture Review Eval；
- Eval 只是附加 Review Evidence，不创建新的 Method Stage，不替代 readiness-check。

Phase 0 不形成实现 EU，不修改 CMS 产品行为，不进入 Issue #60 / E1～E3。

## 5. Phase 1 — Repository Documentation Authority Convergence

### Planned Unit 1A — Canonical Authority Audit & Reconciliation

目标：先解决文档语义 Authority，再处理目录外观。

范围：

- Root `README.md`；
- `docs/requirements/**`；
- `docs/specifications/**`；
- `docs/technical/**`；
- `docs/work/**`；
- `data-migrations/**` 中当前边界说明；
- 主要源码 / Site Package 子树 README。

每份文档至少分类为：

```text
CURRENT
PARTIALLY_CURRENT
SUPERSEDED
HISTORICAL_EVIDENCE
```

规则：

- `PARTIALLY_CURRENT` 不能直接归档；必须先把仍有效语义吸收进 canonical Current Authority；
- 修复 EU-41 / EU-42 后的 V2 responsibility、旧 `site-baseline/static/**` ownership、Historical Migration / Site Package responsibility 等 current-document drift；
- Requirement 的 archive 门槛最高，不能因为对应 EU 已完成就自动归档；
- 本单元不机械做全仓目录搬迁。

### Planned Unit 1B — Documentation Information Architecture & Archive Migration

在 1A 完成 Current Authority 接管后再做物理整理。

目标信息架构：

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
- Site Package reconcile；
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

## 9. High-Capability Architecture Review Eval Experiment

本轮把 GPT-6 review 作为一个小型实验穿插在 Phase 0 中，不预设长期保留。

Consumer-local Eval 入口：

```text
evals/architecture/
evals/run_architecture_review.py
```

语料包含：

- `AR-01` Documentation Authority Architecture；
- `AR-02` Backend Application / Gradle Module Boundary；
- `AR-03` Generic Historical Migration Boundary。

### Current experiment evidence

Phase 0 已在 exact Planning Head `52d8d59094eb5f02a2780ccf131363adac08a1dc` 对 AR-02 做一次 bounded paired comparison：

- lower-cost baseline：requested `gpt-5.6-sol` / medium；
- high-capability review：requested `gpt-6-astra` / high；
- 两次使用相同 context digest / prompt digest，stderr empty，return code 0；
- 当前 Codex JSONL 不暴露可独立确认的实际 runtime model / effort，因此只能记录 requested model / effort，不能声称 runtime trace 已二次证明；
- 两次 Human Semantic Verdict 均为 `SUPPORTED_WITH_CHANGES`，AR-02 八条 assertions 均 PASS；
- 共同 findings 已用于修订本文件 2A；
- GPT-6 提供少量有价值的增量细化，但没有发现 lower-cost review 完全遗漏的 blocking architecture flaw。

完整人工评分与原始 evidence digest 见 Issue #92 的 AR-02 Evidence comment。

当前实验结论：**ADJUST**。

因此 Phase 0 后续策略调整为：

1. High-capability review 不进入普通 Development Method、Readiness Gate 或每个 Execution Unit 的默认步骤；
2. 只在高返工成本且普通 review 后仍存在真实 unresolved architecture ambiguity 时，选择单一 bounded scenario 做第二视角挑战；
3. 本轮不为了实验形式继续执行 AR-03 GPT-6；AR-03 只在后续 Generic Migration Planning 真正出现争议时重新启用；
4. `AR-01` 同样只在 Documentation Authority review 出现实际争议时升级；
5. 每个 scenario 仍必须独立 `codex exec --ephemeral --json`，Runtime 只获得声明的 `context_paths`，不得读取 assertions / expected behavior / 历史结果；
6. 禁止 Web research、仓库外搜索、代码修改与重新规划整个项目；
7. process exit `0` 不等于 PASS，必须人工逐条语义评分；
8. GPT-6 输出只构成 Review Evidence，不替代 Repository Authority 与 readiness-check。

Evidence contract 继续至少保存：

- exact Planning Head；
- scenario id；
- requested model / reasoning effort；
- Codex CLI version；
- context path list + digest；
- exact command argv；
- stdout JSONL；
- stderr；
- return code；
- 人工 assertion grading；
- 是否导致 Planning Authority 实际修订；
- 如果 runtime trace 没有暴露 model / effort，则明确记录 `NOT OBSERVABLE`。

实验生命周期结论仍使用：

```text
RETAIN
ADJUST
DROP
```

当前只有一个 Consumer / 一个真实 scenario 的对照证据，不足以形成跨项目 Method 结论，因此暂不向 `dygapp/agentic-dev` 提交正式反馈 Issue。若后续第二个真实场景再次证明稳定增量价值，或暴露可复用的 model-isolation / evidence-lifecycle 方法问题，再通过 Issue / Evidence 反馈；Consumer 会话不得直接修改 `agentic-dev` Repository 文件、Branch、PR 或 Workflow。

## 10. Fresh Context and gates

Phase 0 合并后，后续默认一个 Ready Execution Unit 使用一个新的 Fresh Context。

新会话提示词只承担 Locator / Handoff：声明 Fresh Context、目标 Repository、必要的前置读取与当前 Planning Issue；任务状态、Execution Unit、Readiness 与下一实际步骤都必须从 GitHub 当前 Authority 恢复，不能复制本计划的状态摘要进提示词。

Phase 0 Exit Gate：

1. Issue #92、Issue #77 与 Project Planning 文档表达同一实际路线；
2. 旧 Slice D 不再被描述成无前置工作的直接下一步；
3. High-Capability Review Eval 的 scope / isolation / evidence / grading 已固化，并已对首个真实 paired scenario 完成人工语义评分与 Planning correction；
4. Current Ready Execution Unit 仍为 NONE；
5. 下一 Fresh Context 应从 Phase 1 的 current audit / Requirement / Specification / Technical Planning / slice-work 实际状态继续，而不是从聊天历史推断。
