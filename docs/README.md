# 文档权威与本地发现入口

本文件是 `jilinjobs-cms` 的 Documentation Information Architecture 入口，也是 Consumer 普通运行的 **Local Discovery Entry**。它只提供稳定 Authority / resource locator、按需加载边界与本地失败回退，不替代 `AGENTS.md`、产品 Requirement、Project Roadmap、Work lifecycle、Method / Skill procedure 或 GitHub Current Evidence，也不缓存第二份 Current State truth。

## Fresh Context 恢复顺序

1. 根 `AGENTS.md`：Repository Governance、Authority Boundary、Fresh Context、Human Escalation 与 GitHub 操作授权；
2. 根 `README.md`：稳定项目范围与入口；
3. 本文件：Local Discovery Entry / Documentation IA；
4. `docs/work/current/README.md` + 当前 Open execution PR / Branch：解析 Current Execution Lifecycle；
5. 如果任务只是 state-only 且状态已无歧义，在此停止；
6. Planning / durable route 按需读取 `docs/project/project-roadmap.md`；Method / execution routing 读取 `docs/project/project-capability-profile.md`，再按 selector 加载单个 canonical Method；
7. Requirement 任务先读取 `docs/requirements/index.md` 定位唯一 fact owner；其他任务读取当前责任直接相关的 Domain / Architecture / Specification / Technical Authority；
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
| `docs/requirements/` | 当前 Product / Domain Requirement fact owner、Human Navigation 与 Authority Index | 先 Index，再读取命中 owner |
| `docs/specifications/` | 当前用户可观察行为、Failure Behavior 与 Acceptance | 读取当前任务相关 owner |
| `docs/technical/` | 跨 Feature implementation / HTTP Interface Contract 与 Verification Strategy | 读取当前任务相关 owner |
| `docs/work/current/README.md` | Current Execution Lifecycle Locator | state / execution lifecycle 任务读取 |
| `docs/work/current/*.md` | active work artifact | locator 指向时读取 |
| `docs/work/archive/` | 已完成 Execution Unit / execution evidence | 默认不读取 |
| 各分类 `archive/` | superseded / historical traceability | 默认不读取 |

## 当前产品 Authority 入口

- Requirement Human Navigation：`docs/requirements/README.md`
- Requirement Authority Index：`docs/requirements/index.md`
- Product Requirement：`docs/requirements/information-publishing.md`
- CMS Domain Requirement：`docs/requirements/cms-domain.md`
- CMS 长期 Architecture：`docs/architecture/cms-architecture.md`
- Current Specifications：`docs/specifications/README.md`
- Backend Technical：`docs/technical/backend-service.md`
- HTTP Interface Contract：`docs/technical/http-interface-contract.md`
- Admin Technical：`docs/technical/admin-frontend.md`
- Public Technical：`docs/technical/public-site-frontend.md`
- Rich Text Technical：`docs/technical/rich-text-authoring.md`
- Verification Strategy：`docs/technical/verification-strategy.md`
- JilinJobs Site Definition workspace：`sites/jilinjobs/**`
- Historical Migration workspace：`data-migrations/**`

Requirement Index 只拥有 locator / relation；长期 Product / Domain fact 仍由两个 canonical fact owner 持有。历史同名三件套、完成态 Planning、旧 READY / EU / migration inventory 不参与 ordinary Fresh Context。

## 来源与证据角色

来源可信度不使用脱离语义责任的全局分数。判断一个事实能否作为当前依据时，必须同时看 **source role、provenance、适用范围、currentness 与 semantic owner**；同一来源在不同 claim 上可以具有不同证明力。

| 来源角色 | 当前事实地位 | 适用边界 |
|---|---|---|
| Current canonical Authority | 对其声明的 semantic responsibility 具有当前权威 | 只在 owner 的 Product / Domain / Architecture / Specification / Technical / Project / Method 等责任内成立 |
| Formal Decision / ADR | Accepted decision 的背景、候选、权衡与 supersede lineage | 用于解释为什么形成当前架构；当前叠加后的 Architecture State 仍以 `docs/architecture/cms-architecture.md` 等 current state owner 为准 |
| Versioned canonical source | 对自身明确拥有的稳定 Site Definition、canonical migration dataset、manifest / digest / provenance 等具体事实具有 bounded authority | `sites/jilinjobs/**` 不拥有 Product Requirement；`data-migrations/**` 不因保存 legacy-derived data 就成为普通 Runtime 或产品需求 owner |
| External authoritative source | 对合同、法规、外部接口或外部系统自身事实提供有 provenance 的输入 | 必须保留来源、时间 / 版本与适用范围；外部事实不会自动覆盖 Consumer 已确认的其他 semantic owner |
| Explicit Human Authority / decision | 对 Human 明确裁决的当前 bounded decision 具有授权价值 | 只覆盖明确问题与范围；若结论需要跨上下文长期成立，必须 promotion 到对应 canonical semantic owner，而不是让聊天或 Issue comment 永久替代它 |
| GitHub-native Current Evidence | 对 Branch、PR、Commit、Actions、Review、Issue timeline 等原生瞬时状态具有当前证据价值 | 不长期拥有 Product / Domain / Architecture 事实；durable semantic decision 应进入真实 canonical owner |
| Repository implementation evidence | Code、tests、configuration、schema、build / runtime behavior 可证明“当前实现是什么” | 不能仅凭实现存在反向发明 Requirement；与 Current Authority 不一致时先判定 implementation defect、stale Authority 或 stale verification contract |
| Historical / Legacy material | `archive/**`、旧规划 / EU、Legacy Source、历史 Snapshot、closed change lineage 等只提供追溯与历史证据 | 默认退出 ordinary Fresh Context；“历史上如此”不等于“当前仍要求如此” |
| Analysis / conversation material | source inventory、comparison、scratchpad、未作明确裁决的会话推理与未 promotion 的分析结论 | 默认非 Authority；若形成 durable fact，必须 promotion 到真实 semantic owner，否则在任务结束后退出 Current runtime |
| Unknown / unresolved | provenance、scope、currentness 或 owner 尚不能确定 | 不得被提升为当前事实；若会实质改变产品行为或验收，按当前 Method / Human Authority 处理 |

发生冲突时不按“文件更新日期”或“代码比文档真实”机械裁决。先回到该事实的 semantic owner 与适用范围：Current owner 与 Historical / Legacy 冲突时，Historical 只保留 lineage；ADR 与 current Architecture State 表达不同阶段时，以当前 state owner 解释现状、ADR 保留决策历史；implementation / verification 与 Authority 不一致时，必须先判定哪一侧 stale。多个 Current primary owner 对同一语义给出不兼容结论且无法由责任边界消歧时，fail closed 并升级到对应 Method / Human Authority。

阶段性 source inventory、Requirement extraction / comparison、冲突矩阵与 review batch 默认保留在当前 controlling Issue / Review Evidence 中，不因为“有用”就进入长期 Project Knowledge 或建立永久 source catalog。

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
