---
id: project:capability-profile
type: project
status: active
---

# Project Capability Profile

## 1. 角色

本文件记录 `jilinjobs-cms` 当前 Repository 如何实例化已采用的工程 Capability contract。它属于 Consumer-local Project Knowledge，只拥有本仓库当前 capability instance、selector 与 locator，不复制 Method、Architecture、Skill、Rule 的规范正文，也不承担 Current Execution State。

Consumer 项目事实、产品 Requirement、Domain、Architecture、代码、验证与集成策略始终由本仓库 Authority 决定；`agentic-dev` 只提供经过显式采用 / 适配的 reusable capability provenance。

当前 evaluated upstream baseline：

`dygapp/agentic-dev@ed1a4446f0430890e7ad39673ac9c2e341e6a829`

该 SHA 只表示最近完成显式 compare / disposition 的 upstream frontier，不表示 upstream Project state、Research / Eval、Guide instance 或全部 capability 已被本 Consumer 采用。

## 2. Method Selection Instance

通用 Method selection contract 由 `docs/architecture/method.md` 定义；本文件只保存当前 Repository 的 `work kind → Method id / locator` 映射。

当前映射：

- 普通 Feature / change，从意图澄清到实现收敛 → `method:ai-development` → `docs/methods/ai-development.md`；
- 多个当前或预期 Feature 因共享的长期 Requirement / Domain / Architecture Context 缺失、冲突或需要重建而无法安全进入可靠 Specification → `method:software-project-clarification` → `docs/methods/software-project-clarification.md`；
- Existing Consumer 显式评估 / 升级 `agentic-dev` baseline → `method:consumer-upgrade` → `docs/methods/consumer-upgrade.md`；
- 已进入 Human Review，针对同一批 findings 执行分类、修复、定向验证与重新复核 → `method:review-feedback-cycle` → `docs/methods/review-feedback-cycle.md`；
- Consumer-local Method / Engineering practice 需要跨多个真实样本验证后再决定 Promotion / Reject → `method:method-experiment` → `docs/methods/method-experiment.md`。

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

本轮 Foundation Rebuild 重新裁决后，`external-operation` 与 `review-change` 作为独立 Procedure 进入 Consumer-local Skill corpus；这不是为了与 upstream 数量对齐，而是因为现有 Rules、`github-actions-verification` 与 `review-feedback-cycle` 均不能单独拥有其完整 Trigger → Procedure → Output → Exit / Escalation 语义。

## 5. Architecture Instance

当前工程 capability 类型与组合边界由以下 Consumer-local canonical owners 持有：

- `docs/architecture/engineering-capability.md`
- `docs/architecture/consumer.md`
- `docs/architecture/method.md`
- `docs/architecture/project-knowledge.md`
- `docs/architecture/rule.md`
- `docs/architecture/rule-discovery.md`
- `docs/architecture/skill.md`

产品 / Domain / CMS Architecture 不由本 Profile 定义。本轮 Foundation Rebuild 后续会分别重建其真实长期 owner；在完成前仍按当前 Repository Authority 与现有 Current documents fail closed，不从本 Profile 发明产品事实。

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
- Project Capability Profile：本文件；
- Project Roadmap：`docs/project/project-roadmap.md`；
- Current Execution Lifecycle：`docs/work/README.md` + `docs/work/current/README.md`；
- Project Evolution：本轮 Foundation Rebuild 后续从当前 Roadmap / historical planning 中提炼独立 owner；在完成前不得把 Roadmap 中的历史流水直接视为新的 capability / product Authority。

Project Knowledge / Capability 边界由 `docs/architecture/project-knowledge.md` 持有。

## 8. Ordinary Runtime

治理收口后的目标运行路径：

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

## 9. Foundation Rebuild 临时边界

Issue #153 的 Bootstrap Governance Authority 只为本轮历史 ownership 重建提供临时 upstream read-only comparison 能力，不属于 ordinary runtime instance，也不授予产品范围扩张、merge、release 或 deploy 权限。

当本轮治理完成并通过 Fresh Context / ordinary runtime validation 后，该临时授权终止，本 Profile 继续作为 Consumer-local capability instance owner。