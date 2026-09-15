# 文档权威与本地发现入口

本文件是 `jilinjobs-cms` 的 Documentation Information Architecture 入口，也是 Consumer 普通运行的 **Local Discovery Entry**。它只提供稳定 Authority / resource locator、按需加载边界与本地失败回退，不替代 `AGENTS.md`、产品 Requirement、Project Roadmap、Work lifecycle、Method / Skill procedure 或 GitHub Current Evidence，也不缓存第二份 Current State truth。

## Fresh Context 恢复顺序

1. 根 `AGENTS.md`：Repository Governance、Authority Boundary、Fresh Context、Human Escalation 与 GitHub 操作授权；
2. 根 `README.md`：稳定项目范围与入口；
3. 本文件：Local Discovery Entry / Documentation IA；
4. `docs/work/current/README.md` + 当前 Open execution PR / Branch：解析 Current Execution Lifecycle；
5. 如果任务只是 state-only 且状态已无歧义，在此停止；
6. Planning / durable route 按需读取 `docs/project/project-roadmap.md`；Method / execution routing 读取 `docs/project/project-capability-profile.md`，再按 selector 加载单个 canonical Method；
7. 当前任务直接相关的 Requirement / Domain / Architecture / Specification / Technical Authority；
8. 当前 controlling Issue、PR / Actions 与其他 Current Evidence；只有真正执行某项 Skill responsibility 时才加载对应 `SKILL.md`。

普通运行不得自动访问 `dygapp/agentic-dev` 修补本地发现缺口。若 locator / source 缺失、Current owner 不明确、多个 primary responsibility 无法消歧或 Authority 冲突，必须 fail closed，扩大最小本地读取并重新定位。

`archive/**` 默认不参与 Fresh Context。只有当前 Authority、追溯 / 审计任务或 Verification 明确要求时才定向读取。

## 文档区域

| 区域 | 责任 | Fresh Context 默认 |
|---|---|---|
| `docs/project/` | Consumer-local Project Knowledge：Capability Profile、Roadmap、Evolution | 按任务读取 |
| `docs/methods/` | Consumer-local canonical Method | selector 命中后读取一个 |
| `docs/architecture/` | 长期 capability / product Architecture 与 ADR | 按责任读取 |
| `docs/rules/` | Discoverable Consumer-local Rule | 只通过 Rule Discovery 读取命中正文 |
| `skills/` | Consumer-local Skill corpus | 责任明确且需要独立 Procedure 时读取 |
| `docs/requirements/` | 当前 Product / Domain Requirement Authority | 读取当前任务相关 owner |
| `docs/specifications/` | 当前用户可观察行为、Failure Behavior 与 Acceptance | 读取当前任务相关 owner |
| `docs/technical/` | 跨 Feature implementation contract 与 Verification Strategy | 读取当前任务相关 owner |
| `docs/work/current/README.md` | Current Execution Lifecycle Locator | state / execution lifecycle 任务读取 |
| `docs/work/current/*.md` | active work artifact | locator 指向时读取 |
| `docs/work/archive/` | 已完成 Execution Unit / execution evidence | 默认不读取 |
| 各分类 `archive/` | superseded / historical traceability | 默认不读取 |

## 当前产品 Authority 入口

- Product Requirement：`docs/requirements/information-publishing.md`
- CMS Domain：`docs/requirements/cms-domain.md`
- CMS 长期 Architecture：`docs/architecture/cms-architecture.md`
- Current Specifications：`docs/specifications/README.md`
- Backend Technical：`docs/technical/backend-service.md`
- Admin Technical：`docs/technical/admin-frontend.md`
- Public Technical：`docs/technical/public-site-frontend.md`
- Rich Text Technical：`docs/technical/rich-text-authoring.md`
- Verification Strategy：`docs/technical/verification-strategy.md`
- JilinJobs Site Definition workspace：`sites/jilinjobs/**`
- Historical Migration workspace：`data-migrations/**`

Requirement / Domain / Architecture / Specification / Technical 各自只维护自己的 semantic owner；历史同名三件套、完成态 Planning、旧 READY / EU / migration inventory 不参与 ordinary Fresh Context。

## Project / Method / Rule / Skill 入口

- Project Capability Profile：`docs/project/project-capability-profile.md`
- Project Roadmap：`docs/project/project-roadmap.md`
- Project Evolution：`docs/project/project-evolution.md`
- Canonical Methods：`docs/methods/`
- Capability Architecture：`docs/architecture/engineering-capability.md`、`consumer.md`、`method.md`、`project-knowledge.md`、`rule.md`、`rule-discovery.md`、`skill.md`
- Rule Discovery runtime：`docs/architecture/rule-discovery.md` + `tools/rule-discovery/rule_discovery.py`
- Skill root：`skills/`
- Work lifecycle：`docs/work/README.md`
- Current execution locator：`docs/work/current/README.md`

`docs/work/current/README.md` 只拥有 Current Execution Lifecycle locator，不拥有 Planning Candidate 排序或完整 Roadmap。GitHub PR / Branch / Actions 对各自原生瞬时状态负责；这不构成“GitHub 永远高于本地文件”的通用规则。

## 文档完整性

Current 文档遵循 `rule:human-facing-content-integrity`：中文主述、精确机器标识、正式概念身份不被表达重写改变。Historical Evidence 以证据保真优先，重新晋升为 Current 前先完成 semantic reconciliation。

自动约束由 `scripts/verify-docs-governance.mjs` 与 `.github/workflows/docs-governance.yml` 执行。不得通过扩大历史例外列表隐藏仍属于 Current Authority 的问题。
