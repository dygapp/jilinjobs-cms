---
id: project:capability-profile
type: project
status: active
---

# 项目能力画像（Project Capability Profile）

## 1. 角色

本文件记录 `jilinjobs-cms` 当前 Repository 如何实例化已采用的工程 Capability contract。它属于 Consumer-local Project Knowledge，只拥有本仓库当前 capability instance、selector 与 locator，不复制 Method、Architecture、Skill、Rule 的规范正文，也不承担 Current Execution State。

Consumer 项目事实、产品 Requirement、Domain、Architecture、代码、验证与集成策略始终由本仓库 Authority 决定；`agentic-dev` 只提供经过显式采用 / 适配的 reusable capability provenance。

当前 evaluated upstream baseline：

`dygapp/agentic-dev@ce28ec748f28a58b2bb65bf75db353f5f50f772d`

该 SHA 只表示最近完成显式 compare / disposition 的 upstream frontier，不表示 upstream Project state、Research / Eval、Guide instance 或全部 capability 已被本 Consumer 采用。

## 2. Method Selection Instance

通用 Method selection contract 由 `docs/architecture/method.md` 定义；本文件只保存当前 Repository 的 `work kind → Method id / locator` 映射。

当前映射：

- 新项目尚无可靠 Requirement Baseline，或多个 Feature 被系统性 Requirement gap / conflict / ownership failure 阻塞，需要建立 / 重建长期 Requirement Authority → `method:requirement-baseline-establishment` → `docs/methods/requirement-baseline-establishment.md`；
- Requirement Baseline 已足够，但多个 Feature 被跨 Feature、长期、高成本难逆的 systemic architecture driver 阻塞 → `method:architecture-clarification` → `docs/methods/architecture-clarification.md`；
- 普通 Feature / change，Requirement Baseline 与所需 Architecture Context 已足够 → `method:ai-development` → `docs/methods/ai-development.md`；
- Existing Consumer 显式评估 / 升级 `agentic-dev` baseline → `method:consumer-upgrade` → `docs/methods/consumer-upgrade.md`；
- 已进入 Human Review，针对同一批 findings 执行分类、修复、定向验证与重新复核 → `method:review-feedback-cycle` → `docs/methods/review-feedback-cycle.md`；
- Consumer-local Method / Engineering practice 需要跨多个真实样本验证后再决定 Promotion / Reject → `method:method-experiment` → `docs/methods/method-experiment.md`。

Requirement Baseline Establishment、Architecture Clarification 与 AI Development 通过各自 Return Contract 自然衔接；当前不保留只负责串联它们的 compatibility super-method。

如果当前 work kind 不属于任何映射，不得为了获得流程而强行套用最接近的 Method；按 Repository Authority 与 direct responsibility 工作，并在真实 Evidence 支持时再评估是否新增 Method。

本映射不复制 Method stage、Gate、completion condition 或内部 Skill / Rule routing。

## 3. Rule Discovery Instance

当前 Rule Discovery instance：

- Rule Architecture：`docs/architecture/rule.md`；
- Discovery Architecture：`docs/architecture/rule-discovery.md`；
- Rule root：`docs/rules/`；
- Tool：`tools/rule-discovery/rule_discovery.py`；
- ordinary runtime 输出：少量 `{id, path}` candidate locator。

普通运行调用：

```bash
python3 tools/rule-discovery/rule_discovery.py --repo-root . discover --signals-json '<task-signals-json>'
```

五维 task signals、三态、bounded-token、matching、locator-only、semantic confirmation、responsibility transition checkpoint 与 fail-closed 由 `docs/architecture/rule-discovery.md` 持有；本 Profile 不复制这些规则，也不维护 Rule inventory、scope metadata 或 Rule → signal 映射。

当前 Consumer Tool 尚未实现 upstream 后续 Rule-root Human `README.md` 保留例外，因此 `docs/rules/**` 当前仍只放 discoverable Rule Markdown；不能因为 upstream Architecture 已有该能力就推断本地 Tool 已支持。

## 4. Skill Discovery Instance

当前 Skill root：`skills/`。

Skill 类型 contract：`docs/architecture/skill.md`。

ordinary Agent 使用 Agent Skills 原生 discovery，根据 `SKILL.md` 的 `name` / `description` 与当前责任按需加载。Skill corpus 自身是当前 inventory；本 Profile 不复制 Skill 清单和数量。

`external-operation` 与 `review-change` 作为独立 Procedure 保留在 Consumer-local Skill corpus；其存在来自本 Consumer 已确认的 Trigger → Procedure → Output → Exit / Escalation 责任，不是为了与 upstream inventory 数量对齐。

## 5. Architecture Instance

当前工程 capability 类型与组合边界由以下 Consumer-local canonical owners 持有：

- `docs/architecture/engineering-capability.md`
- `docs/architecture/consumer.md`
- `docs/architecture/method.md`
- `docs/architecture/requirement-authority.md`
- `docs/architecture/project-knowledge.md`
- `docs/architecture/rule.md`
- `docs/architecture/rule-discovery.md`
- `docs/architecture/skill.md`

产品 / Domain / CMS Architecture 不由本 Profile 定义。当前产品与领域语义仍由现有 `docs/requirements/**` owners 持有；跨 Feature 的 CMS / Site Definition / Historical Migration / Runtime / Public Renderer 等长期结构边界仍由 `docs/architecture/cms-architecture.md` 持有。本次 capability adoption 不改变任何 Product Requirement，也不预判后续 G2 的 Requirement IA。

## 6. Model Collaboration Instance

当前 Consumer **不采用、不启用** Model Collaboration runtime：

- status：`disabled / not adopted`；
- persistent runtime config：none；
- concrete model tier mapping：none；
- provider / delegation instance：none。

未来如有明确采用目标，必须建立独立 Consumer-local semantic acceptance / runtime validation lifecycle；不能因为 upstream capability 存在、Guide 有示例或工具支持就推断 enabled。

## 7. Project Knowledge Entry

当前 Project Knowledge 的稳定入口：

- Project Charter equivalent：根 `README.md`；
- Project directory boundary / Human navigation：`docs/project/README.md`；
- Project Capability Profile：本文件；
- Project Roadmap：`docs/project/project-roadmap.md`；
- Project Evolution：`docs/project/project-evolution.md`；
- Current Execution Lifecycle：`docs/work/README.md` + `docs/work/current/README.md`。

Project Knowledge / Capability 边界由 `docs/architecture/project-knowledge.md` 持有。Roadmap 只拥有 future directions / durable governance milestones；Evolution 只拥有稳定历史摘要；两者都不取得产品 Requirement / Architecture 或 Current Execute Gate 的第二所有权。

## 8. Ordinary Runtime

治理收口后的普通运行路径：

```text
AGENTS.md + README.md
→ docs/README.md Local Discovery Entry
→ Project Capability Profile + current Repository facts
→ Method selection（如当前 work kind 命中）
→ current Method stage / direct responsibility
    ├─→ relevant Consumer-local Architecture
    ├─→ Skill discovery / invocation（如需要）
    └─→ Consumer-local Rule Discovery
→ execute / verify / return
```

普通运行默认：

```text
upstream access = 0
```

本地 selector、locator、metadata 或 owner 异常只允许 Consumer-local fail closed；不得自动访问 `agentic-dev` 在线补流程。

## 9. Latest Explicit Upstream Disposition

Issue #155 G0 对 `ed1a4446f0430890e7ad39673ac9c2e341e6a829 → ce28ec748f28a58b2bb65bf75db353f5f50f772d` 的 direct semantic delta 作出以下 Consumer-local disposition：

- **adopt** `method:requirement-baseline-establishment`，本地 canonical owner：`docs/methods/requirement-baseline-establishment.md`；
- **adopt** `method:architecture-clarification`，本地 canonical owner：`docs/methods/architecture-clarification.md`；
- **adapt** `architecture:requirement-authority` 到本 Consumer，保留 semantic ownership / lifecycle contract，但不在 G0 预先改造 Product Requirement IA；本地 owner：`docs/architecture/requirement-authority.md`；
- **adapt** `method:ai-development` 的 return-contract delta，使 systemic Requirement / Architecture gap 分别返回真实 owner / 新 Method；
- **replace / retire** `method:software-project-clarification`，不再作为 Current Method 或 selector target；
- Human Guide 未进入 Consumer runtime Authority。

该记录只说明 reusable capability adoption，不把 `agentic-dev` Project Knowledge、Roadmap、Research 或 Consumer-independent Guide 内容变成 JilinJobs 事实。完成显式 upgrade 后 ordinary runtime 继续保持 `upstream access = 0`。
