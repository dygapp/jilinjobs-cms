---
id: project:capability-profile
type: project
status: active
---

# 项目能力画像（Project Capability Profile）

## 1. 角色

本文件记录 `jilinjobs-cms` 当前 Repository 如何实例化已采用的工程 Capability contract。它属于 Consumer-local Project Knowledge，只拥有本仓库当前 capability instance、selector 与 locator，不复制 Method、Architecture、Skill、Rule 的规范正文，也不承担 当前执行状态。

Consumer 项目事实、产品 Requirement、Domain、Architecture、代码、验证与集成策略始终由本仓库 Authority 决定；`agentic-dev` 只提供经过显式采用 / 适配的 reusable capability provenance。

当前 evaluated upstream baseline：

`dygapp/agentic-dev@b71783782b97e2033b99014744d1286dd69cb107`

该 SHA 只表示最近完成显式 compare / disposition 的 upstream frontier，不表示 upstream Project state、Research / Eval、Guide instance 或全部 capability 已被本 Consumer 采用。

## 2. Method 选择实例

通用 Method selection contract 由 `docs/architecture/method.md` 定义；本文件只保存当前 Repository 的 `work kind → Method id / locator` 映射。

当前映射：

- 新项目尚无可靠 Requirement Baseline，或多个 Feature 被系统性 Requirement gap / conflict / ownership failure 阻塞，需要建立 / 重建长期 Requirement Authority → `method:requirement-baseline-establishment` → `docs/methods/requirement-baseline-establishment.md`；
- Requirement Baseline 已足够，但多个 Feature 被跨 Feature、长期、高成本难逆的 systemic architecture driver 阻塞 → `method:architecture-clarification` → `docs/methods/architecture-clarification.md`；
- 普通 Feature / change，Requirement Baseline 与所需 Architecture Context 已足够 → `method:ai-development` → `docs/methods/ai-development.md`；
- Existing Consumer 显式评估 / 升级 `agentic-dev` baseline → `method:consumer-upgrade` → `docs/methods/consumer-upgrade.md`；
- 已进入 人工评审，针对同一批 findings 执行分类、修复、定向验证与重新复核 → `method:review-feedback-cycle` → `docs/methods/review-feedback-cycle.md`；
- Consumer-local Method / Engineering practice 需要跨多个真实样本验证后再决定 Promotion / Reject → `method:method-experiment` → `docs/methods/method-experiment.md`。

Requirement Baseline Establishment、Architecture Clarification 与 AI Development 通过各自 Return Contract 自然衔接；当前不保留只负责串联它们的 compatibility super-method。

如果当前 工作类型 不属于任何映射，不得为了获得流程而强行套用最接近的 Method；按 仓库权威 与 直接责任 工作，并在真实 Evidence 支持时再评估是否新增 Method。

本映射不复制 Method stage、Gate、completion condition 或内部 Skill / Rule routing。

## 3. 规则发现实例

当前 Rule Discovery instance：

- Rule Architecture：`docs/architecture/rule.md`；
- Discovery Architecture：`docs/architecture/rule-discovery.md`；
- Rule root：`docs/rules/`；
- Tool：`tools/rule-discovery/rule_discovery.py`；
- cloud transport：`.github/workflows/rule-discovery.yml`；
- 普通运行时 输出：少量 `{id, path}` candidate locator。

当前本地 checkout 调用：

```bash
python3 tools/rule-discovery/rule_discovery.py --repo-root . discover --signals-json '<task-signals-json>'
```

当前云端 task-level invocation 支持以下 transport，真正执行 discovery 的路径都必须携带 exact 40-character commit SHA，并在该 SHA checkout 后调用同一 Consumer-local Tool：

1. GitHub Actions `workflow_dispatch`：输入 `target_sha` 与 `signals_json`；
2. `issue_comment` transport：只有承载该 listener 的 workflow **已经存在于 GitHub 默认分支** 时，仓库 `OWNER` / `MEMBER` / `COLLABORATOR` 才可在 Issue / PR 中发送：

```text
/rule-discovery <40-char-sha> <signals-json>
```

candidate PR 仅在自身 workflow 文件中新增 `issue_comment` trigger，并不使该 listener 在合并前自动生效。当前 Consumer 因此提供一个仅服务**预集成 exact-Head 验证**的 PR-body transport：当 candidate 自身的 Rule Discovery workflow 已在该 PR 的 `pull_request` 事件上运行时，可在 PR body 中临时写入一行隐藏请求：

```text
<!-- /rule-discovery <40-char-sha> <signals-json> -->
```

该 transport 只接受 `target_sha == current PR Head SHA`，并由该 exact Head 自身的 Tool 产生 locator-only result；请求消费完成后可从 PR body 移除。它不建立新的 Authority，也不替代合并后的 `issue_comment` / `workflow_dispatch` transport。若当前 Runtime 没有可用的本地 exact checkout / Repository Runtime、不可调用 `workflow_dispatch`，且 candidate 也没有可用的 PR-body pre-integration transport，则必须 fail closed；不得把未触发的 comment、固定 smoke、旧 Head Evidence 或“candidate 已声明 listener”当作 task-level discovery 成功。

云端 invocation 必须校验 requested SHA = actual SHA，并把 signals、requested / actual SHA 与 discovery JSON 作为可审计 Evidence；`pull_request` / `push(main)` 触发的同名 workflow 仍只承担 repository lint、deterministic tests 与固定 smoke，不能替代当前 task signals 的 task-level discovery。

调用 Rule Discovery transport 本身是 preflight compute，不授予 Repository / Issue / PR / workflow / deployment 等后续副作用权限。

五维 task signals、三态、bounded-token、matching、locator-only、semantic confirmation、responsibility transition checkpoint 与 失败关闭 由 `docs/architecture/rule-discovery.md` 持有；本 Profile 不复制这些规则，也不维护 Rule inventory、scope metadata 或 Rule → signal 映射。

当前 Consumer Tool 继续不采用 Rule-root Human `README.md` 保留例外；`docs/rules/**` 仍只放 discoverable Rule Markdown。该本地选择与 cloud transport 正交，不因为 upstream Architecture 支持 Human README 就自动改变。

## 4. Skill 发现实例

当前 Skill root：`skills/`。

Skill 类型 contract：`docs/architecture/skill.md`。

ordinary Agent 使用 Agent Skills 原生 discovery，根据 `SKILL.md` 的 `name` / `description` 与当前责任按需加载。Skill corpus 自身是当前 inventory；本 Profile 不复制 Skill 清单和数量。

`external-operation`、`human-review` 与 `review-change` 作为独立 Procedure 保留在 Consumer-local Skill corpus；其存在来自本 Consumer 已确认的 Trigger → Procedure → Output → Exit / Escalation 责任，不是为了与 upstream inventory 数量对齐。`human-review` 服务普通 Consumer 软件项目的内容评审与权威回写，`review-change` 服务独立 Repository 变更复核，二者互不替代。

## 5. Architecture 实例

当前工程 capability 类型与组合边界由以下 Consumer-local 规范语义所有者持有：

- `docs/architecture/engineering-capability.md`
- `docs/architecture/consumer.md`
- `docs/architecture/method.md`
- `docs/architecture/requirement-authority.md`
- `docs/architecture/project-knowledge.md`
- `docs/architecture/rule.md`
- `docs/architecture/rule-discovery.md`
- `docs/architecture/skill.md`

产品 / Domain / CMS Architecture 不由本 Profile 定义。当前产品与领域语义仍由现有 `docs/requirements/**` owners 持有；跨 Feature 的 CMS / Site Definition / Historical Migration / Runtime / Public Renderer 等长期结构边界仍由 `docs/architecture/cms-architecture.md` 持有。本次 capability adoption 不改变任何 Product Requirement，也不预判未来 Requirement IA。

## 6. Model Collaboration 实例

当前 Consumer **不采用、不启用** Model Collaboration runtime：

- status：`disabled / not adopted`；
- persistent runtime config：none；
- concrete model tier mapping：none；
- provider / delegation instance：none。

未来如有明确采用目标，必须建立独立 Consumer-local semantic acceptance / runtime validation lifecycle；不能因为 upstream capability 存在、Guide 有示例或工具支持就推断 enabled。

## 7. 项目知识入口

当前 Project Knowledge 的稳定入口：

- Project Charter equivalent：根 `README.md`；
- Project directory boundary / Human navigation：`docs/project/README.md`；
- Project Capability Profile：本文件；
- Project Roadmap：`docs/project/project-roadmap.md`；
- Project Evolution：`docs/project/project-evolution.md`；
- 当前执行生命周期：`docs/work/README.md` + `docs/work/current/README.md`。

Project Knowledge / Capability 边界由 `docs/architecture/project-knowledge.md` 持有。Roadmap 只拥有 future directions / durable governance milestones；Evolution 只拥有稳定历史摘要；两者都不取得产品 Requirement / Architecture 或 Current Execute Gate 的第二所有权。

## 8. 普通运行时

治理收口后的普通运行路径：

```text
AGENTS.md
→ docs/README.md Local Discovery Entry
→ 按当前责任读取 Project Capability Profile / README.md / Current Work / 其他 Repository facts
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

本地 selector、locator、metadata 或 owner 异常只允许 Consumer-local 失败关闭；不得自动访问 `agentic-dev` 在线补流程。

## 9. 最近一次显式上游处置

Issue #172 / PR #173 对 `ce28ec748f28a58b2bb65bf75db353f5f50f772d → b71783782b97e2033b99014744d1286dd69cb107` 的 20-commit reusable capability delta 作出以下 Consumer-local disposition：

- **adapt — Authority-chain review**：吸收高影响 Authority 变更的 owner transition、single semantic owner、下游可观察投影、replaceability seam 与 bounded regenerability challenge，落到 `skills/review-change/SKILL.md` 与 `rule:authoritative-artifact-lifecycle-review`；不把 upstream Review Project state 带入 Consumer。
- **adopt + adapt — Human Review capability**：新增 `architecture:human-review` 与 `skill:human-review`，服务普通 Consumer 软件项目的结构化 Markdown 评审、反馈分类与真实 owner 回写；不复制 upstream Human Guide，也不把人工评审与 independent `review-change` 合并。
- **adapt — Requirement terminology governance**：将跨 Capability 术语 identity / alias / source-role 边界吸收到 `architecture:requirement-authority`；当前没有真实跨 owner terminology pressure，因此**不建立中央中英文术语表**，业务术语继续由 Product / Domain Requirement owner 持有。
- **reject as duplicate owner — generic Data Migration Architecture**：本 Consumer 的长期迁移语义已经分别由 `docs/requirements/cms-domain.md`、`docs/specifications/content-migration.md`、`docs/architecture/cms-architecture.md` 与对应 Technical / Verification owner 持有；不再增加并行通用 migration owner。
- **adapt — Rule Discovery / Bootstrap**：首次实质性人工输出前增加 `communication + human-facing-content` checkpoint；采用 exact-SHA cloud task discovery、scan completeness / symlink fail-closed，并将根 `README.md` 从固定 Bootstrap 改为按需加载。Rule Discovery 仍只返回 locator，Human Guide 不进入固定 Bootstrap。
- **adopt — human intervention necessity**：新增 `rule:human-intervention-necessity`，请求人工前先验证当前 connector / API / Actions / Evidence 是否存在可替代自动化路径；不削弱 Human Authority。
- **adapt — safe external write**：`rule:safe-external-write` 增加创建前查询 / 复用已有远程对象与写后回读，降低重试、会话切换或不确定返回造成的重复 Issue / PR / resource。
- **retain Consumer-local technology Rules**：upstream 在该区间退出通用 Vue / TypeScript technical Rules，但本 Consumer 的 `docs/rules/technology/vue/**` 已是由真实 Vue3 / TypeScript 责任和本地工程事实驱动的 Consumer-local policy，因此不跟随删除，也不反向复制回 upstream。
- **adapt — human-facing content integrity**：普通动作、判断、原因、结论与结构标签默认使用自然中文；稳定状态值、路径、id、SHA、API / CLI、协议值、真实日志与外部正式名称保持精确身份；语言整理不得合并 Method stage、Gate、artifact、Skill、Rule、Authority 等不同正式对象。
- **not adopted — upstream project-only / research / eval / guide state**：upstream Roadmap、Project Capability Profile、Research、Eval result、Guide instance 与 self-adoption 状态都不传播进 Consumer current state。仅用于理解 reusable semantic delta 的 upstream evidence 不成为普通运行依赖。

该 baseline 表示**截至 `b717837…` 已完成显式评估**，不表示该区间每项 upstream asset 都已采用。真正启用的能力只以本文件指向的 Consumer-local Method / Architecture / Rule / Skill canonical owner 为准。

完成本轮 upgrade 后普通运行继续满足：

```text
Consumer-local current state sufficient
upstream ordinary-runtime dependency = 0
```
