# E1～E3 前置 Repository Authority 与 Migration Architecture 收敛规划

## 1. Status

- Planning source：GitHub Issue #92
- Related architecture authority：GitHub Issue #77
- Downstream candidates：GitHub Issue #60 / E1～E3
- Stage：**Planning Authority — ACTIVE / Phase 2 PLANNING CANDIDATE**
- Phase 0：**COMPLETED**
- Phase 1 Repository Documentation Authority Convergence：**COMPLETED**
- Current Ready Execution Unit：**NONE**
- Next Gate：**Phase 2 dependency closure / Specification / necessary Technical Planning；尚无 Execute Authority**
- Phase 1 closure Authority：`docs/project/documentation-authority-convergence.md`

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

1. Repository Documentation Authority 需要先从并列 Current / superseded / Historical Work 状态收敛成可由 Fresh Context 稳定恢复的 Information Architecture；
2. Party-specific Historical Migration Application 仍位于 Backend production `src/main`，而未来 Main migration 需要一个 site-neutral、可复用且独立运行的 Generic Content Migration Application。

第 1 项已由 Phase 1 / EU-43～EU-45 完成；当前只允许转入第 2 项所属的 Phase 2 Planning。旧的“直接进入 Canonical Migration Compatibility & E1～E3 Re-entry”顺序继续保持 superseded。

## 3. Overall sequence

```text
Phase 0  Planning Authority Solidification — COMPLETED
               ↓
Phase 1  Repository Documentation Authority Convergence — COMPLETED
               ↓
Phase 2  Generic Historical Migration & Backend Application Boundary — PLANNING CANDIDATE
               ↓
Phase 3  Canonical Migration Compatibility & E1～E3 Re-entry Gate
               ↓
        Issue #60 / E1～E3

后置：
Phase 4  Repository Split Readiness Assessment
```

## 4. Phase 0 — Planning Authority Solidification — COMPLETED

Phase 0 已完成：

- Issue #92 成为跨 Issue 总体规划入口；
- Issue #77 保持四层架构边界 Authority，但 Historical Migration remaining path 对齐 Issue #92；
- Project Roadmap 与 `site-package-planning.md` 同步新的实际路线；
- 本计划进入 Repository；
- 建立最小、实验性、可撤销的 bounded architecture-review eval；
- AR-02 / AR-04 current evidence 已形成，模型输出只作为附加 Review Evidence，不创建新的 Method Stage，不替代 readiness-check。

Phase 0 没有形成实现 EU，也没有进入 Issue #60 / E1～E3。

## 5. Phase 1 — Repository Documentation Authority Convergence — COMPLETED

Phase 1 的 classification、audit lineage、最终 IA 与 completion gate 以：

```text
docs/project/documentation-authority-convergence.md
docs/README.md
```

为当前入口。

Phase 1 已完成：

1. **EU-43 — Current Authority Semantic Reconciliation**：关闭四层 boundary / lifecycle / migration / asset / verification currentness drift；
2. **EU-44 — Canonical Product Authority Consolidation**：将 EU-30 confirmed change 与 EU-41/EU-42 ownership 折回 canonical Product / CMS / Backend Authority；
3. **EU-45 — Documentation Information Architecture & Archive Migration**：落实 Root README stable entry、`docs/README.md` Authority Map、Requirements / Specifications / Technical archive、Work lifecycle、supporting method note relocation、Current link repair 与 subtree README。

Completed work records 位于 `docs/work/archive/`。`docs/**/archive/**` 与 `docs/work/archive/**` 默认不参与 Fresh Context Current Authority 恢复；已完成 Unit 的旧 Status / Next Step 不重新授予 Execute Authority。

Phase 1 closure 后 Current Ready Execution Unit = **NONE**。EU-43 / EU-44 / EU-45 Execute Authority 均已终止。

## 6. Phase 2 — Generic Historical Migration & Backend Application Boundary

Phase 2 当前只是 Planning Candidate。下面的 Planned Unit 名称用于组织 Requirement / Specification / Technical Planning，不是 Candidate Execution Unit，也没有 Readiness PASS。

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

当前已有两个真实 paired scenario：

- **AR-02 — Backend Application / Gradle Module Boundary**：Sol / medium 与 Astra / high 均 `SUPPORTED_WITH_CHANGES`；共同 findings 已用于修订 2A，Astra 没有独占 blocking discovery；
- **AR-04 — Migration Sequencing Blind Discovery**：两模型均独立 `DETECTED` 已知 sequencing / application-responsibility flaw，hidden assertions 均 `8/8 PASS`；Astra 未发现 Sol/medium 漏掉的 blocking flaw。

当前实验结论：**ADJUST**。

```text
bounded eval when justified
→ lower-cost capable review first
→ escalate only if unresolved / conflicting / deliberate second opinion
```

规则：

1. High-capability review 不进入普通 Development Method、Readiness Gate 或每个 Execution Unit 的默认步骤；
2. 只在真实高返工成本架构问题确有独立挑战价值时使用 bounded eval；
3. 默认先使用当前足够能力且成本较低的模型；第一轮仍有 unresolved ambiguity、冲突证据或明确需要独立 second opinion 时再升级 Astra；
4. 不为了“完成 corpus”机械执行 AR-01 / AR-03；
5. 每个 scenario 独立 Fresh / ephemeral run，只复制显式 context paths；assertions / expected behavior / provenance / historical results 不进入 runtime workspace；
6. 模型输出只构成 Review Evidence，不替代 Repository Authority 与 readiness-check。

完整 evidence lifecycle、requested model / effort observability limitation 与 paired results 见 `evals/README.md` 和 Issue #92。跨项目 Evidence 已提交 `dygapp/agentic-dev` Issue #71；它不自动修改本 Consumer Method，也不授权当前 Consumer 修改 `agentic-dev` 文件、Branch、PR 或 Workflow。

## 10. Fresh Context and gates

默认一个 Ready Execution Unit 使用一个新的 Fresh Context。新会话提示词只承担 Locator / Handoff：声明 Fresh Context、目标 Repository、必要的前置读取与当前 Planning Issue；任务状态、Execution Unit、Readiness 与下一实际步骤都必须从 GitHub 当前 Authority 恢复。

Current Gate：

1. Phase 0：**COMPLETED**；
2. Phase 1：**COMPLETED**；EU-43 / EU-44 / EU-45 均已终止 Execute Authority；
3. Current Ready Execution Unit：**NONE**；
4. 下一实际步骤是在新的 Planning Context 重新核验最新 `main`、Issue #92 / #77、Roadmap、Open PR / Actions 与当前 code/resource boundary，并从 **Phase 2 Planning Candidate** 的 dependency closure / Specification / necessary Technical Planning 开始；
5. 不得从 Planned Unit 2A / 2B / 2C 名称推断 Candidate Unit、Readiness PASS 或 Execute Authority；
6. Phase 3 compatibility Gate 前不得进入 Issue #60 / E1～E3；Repository Split Assessment 继续后置。