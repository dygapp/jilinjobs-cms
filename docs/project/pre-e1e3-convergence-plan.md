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

### Planned Unit 2A — Backend Minimal Multi-Module Foundation

当前 requirement 已明确 CMS Server 与 Migration Tooling 需要两个独立 Spring Boot executable JAR，因此 application lifecycle 应使用 Gradle module boundary，而 CMS Core 内部领域暂不机械升级为一领域一个 Gradle module。

目标形态：

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

约束：

- `cms-server` 与 `content-migration` 分别产生 executable Spring Boot JAR；
- `cms-core` 内 `content / column / listing / resource / navigation / ...` 继续采用 package-level modularity；
- 不为了 multi-module 顺带改造成 Clean Architecture 或一领域一 module；
- 本单元只完成行为保持型 application/core boundary，不同时泛化 Party migration behavior；
- 当前 Public/Admin/API/Site Package/Flyway behavior 必须保持。

### Planned Unit 2B — Generic Content Migration Application

职责：只负责 `Canonical Migration Dataset → CMS Runtime`。

Generic capability 至少覆盖：

- canonical manifest / index / schema validation；
- path traversal / bounds safety；
- size / SHA-256 integrity；
- stable migration identity / fingerprint；
- preflight before writes；
- transaction boundary；
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

首轮 bounded scenarios：

- `AR-01` Documentation Authority Architecture；
- `AR-02` Backend Application / Gradle Module Boundary；
- `AR-03` Generic Historical Migration Boundary。

默认策略：

1. 先使用较低成本模型执行 dry-run，验证场景题面、context isolation 与评分 assertions 本身可用；
2. 首轮 GPT-6 优先只运行 `AR-02`、`AR-03`；
3. `AR-01` 只有在普通 review 暴露真实争议时再升级高能力模型；
4. 每个 scenario 独立 `codex exec --ephemeral --json`；
5. Runtime 只获得声明的 `context_paths`，不得读取 assertions / expected behavior / 历史结果；
6. 禁止 Web research、仓库外搜索、代码修改与重新规划整个项目；
7. process exit `0` 不等于 PASS；人工逐条评分；
8. GPT-6 输出只构成 Review Evidence，不替代 Repository Authority 与 readiness-check。

Evidence 至少保存：

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
- 是否导致 Planning Authority 实际修订。

实验结论分为：

```text
RETAIN
ADJUST
DROP
```

只有在本 Consumer 取得实际可复用证据后，才考虑向 `dygapp/agentic-dev` 提交 Issue / Evidence。Consumer 会话不得直接修改 `agentic-dev` Repository 文件、Branch、PR 或 Workflow。

## 10. Fresh Context and gates

Phase 0 合并后，后续默认一个 Ready Execution Unit 使用一个新的 Fresh Context。

新会话提示词只承担 Locator / Handoff：声明 Fresh Context、目标 Repository、必要的前置读取与当前 Planning Issue；任务状态、Execution Unit、Readiness 与下一实际步骤都必须从 GitHub 当前 Authority 恢复，不能复制本计划的状态摘要进提示词。

Phase 0 Exit Gate：

1. Issue #92、Issue #77 与 Project Planning 文档表达同一实际路线；
2. 旧 Slice D 不再被描述成无前置工作的直接下一步；
3. High-Capability Review Eval 的 scope / isolation / evidence / grading 已固化；
4. Current Ready Execution Unit 仍为 NONE；
5. 下一 Fresh Context 应从 Phase 1 的 current audit / Requirement / Specification / Technical Planning / slice-work 实际状态继续，而不是从聊天历史推断。