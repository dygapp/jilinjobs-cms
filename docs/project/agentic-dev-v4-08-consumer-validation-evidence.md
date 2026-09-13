# agentic-dev V4-08 Consumer Validation Evidence

状态：**IN PROGRESS / UPGRADE-ONLY EVIDENCE**。

本文件记录 `dygapp/agentic-dev` Issue #122 — V4-08 在真实 Consumer `dygapp/jilinjobs-cms` 中的可复核证据。它不构成产品 Requirement、Planning Authority、Execute Authority、Rule routing table 或普通运行依赖。

## 1. 精确基线

- Consumer adoption base：`b5590246f2a548e9734b3c3a7f60dae9376c8f39`
- Consumer base 状态：Open PR = 0；`docs/work/current/README.md` = `Current Ready Execution Unit: NONE`
- base CI：`CI` / `文档治理检查` 均 SUCCESS
- Consumer 既有 Current Evaluated Baseline：`agentic-dev@1c8cdfea9ecf23ef33ffab20eec3c93679fd4578`
- V4-08 adopted upstream exact candidate：`agentic-dev@3e0b2f5a29caeb344da79f8c96ebffbeb5c2b0cb`
- 选择理由：该 SHA 是 Issue #122 已完成 V4-07 Token Scaling Gate 的精确 runtime candidate；PR #123 后继 Head 相比它仅包含 upstream Project Roadmap 状态提交，未作为 Consumer ordinary-runtime 能力输入。

## 2. Adoption ownership mapping

本次只采用 / 适配跨项目可复用的 V4 Foundation 语义，不复制 upstream 项目状态：

- `AGENTS.md`：Consumer Bootstrap + locator-only Rule Discovery contract；
- `docs/project/rule-discovery-method.md`：Consumer-local V4 method extension；
- `skills/*/SKILL.md`：Consumer 已采用九项 Skill 的物理化 procedure；
- `docs/rules/**`：Consumer-local 原子 Rule，metadata 与正文同文件；
- `tools/rule-discovery/rule_discovery.py`：deterministic Front Matter discovery / lint；
- `tools/rule-discovery/tests/**`：三态、6-token、locator-only、fail-closed 与真实 Consumer 查询的 deterministic tests；
- `.github/workflows/rule-discovery.yml`：Consumer-local CI。

明确拒绝 / 不采用：

- upstream Project Roadmap / V4 Gate state；
- Research / Eval corpus / scaling fixture；
- Reviewed Discovery Map / Activation Manifest / Runtime Catalog / rule-index；
- `rule → signals` 中心人工映射；
- upstream 全量 45 Rules 的机械复制。

## 3. 产品语义影响

本 adoption 不修改 Backend、Admin/Public 产品实现、Site Package、Migration data、Page content、产品 Requirement / Specification 或四层长期 Architecture；只改变 Consumer-local AI engineering governance / discovery 能力。

## 4. Local deterministic evidence

待当前 adoption Head 的 Rule Discovery CI 完成后回写 exact Head、Run、job 与结果。

## 5. Ordinary generation discovery trace

真实 responsibility：实现本次 Consumer-local Rule Discovery / governance adoption。

Task signals 来自当前仓库事实，不从 upstream rule 名称推导：

```json
{
  "phases": ["execute"],
  "activities": ["implementation"],
  "technologies": ["python"],
  "artifacts": ["code", "configuration", "test"],
  "risks": []
}
```

待当前 branch 上本地 discovery 实际运行后回写 `candidates[]` 与最终 semantic applicability confirmation。运行期间只读取返回 candidate bodies。

## 6. Ordinary verification discovery trace

真实 responsibility：验证本次 adoption exact Head 的 deterministic contract / CI / completion claim。

```json
{
  "phases": ["converge"],
  "activities": ["verification"],
  "technologies": [],
  "artifacts": ["commit", "evidence"],
  "risks": ["evidence-reuse"]
}
```

待实际运行后回写 `candidates[]`、语义确认与对应 Current Evidence。

## 7. Rule evolution

当前尚未出现 adoption 之后自然发生的低风险 Rule 新增 / 修改。不得为了实验制造产品或工程规则；若本轮真实 CI / runtime validation 暴露 metadata / Rule defect，将按 Rule 自身 + 必要 tests 修正并作为自然 evolution evidence。若没有发生，则最终 Evidence 保留 **NOT YET NATURALLY OBSERVED**。

## 8. Post-adoption decoupling

待 adoption integration 后，在 Consumer `main` 上只依赖 Consumer-local Authority / Method / Skills / Rules / Rule Discovery 再执行 ordinary runtime，并记录：

- upstream current branch / Roadmap / Issue / PR access = 0；
- online latest-baseline lookup = 0；
- candidate locators only from Consumer-local Rule Discovery；
- upstream 后续变化不会自动改变 Consumer ordinary runtime。

## 9. Central synchronization asset check

目标结论：不存在需要与 Rule 正文同步维护的 Reviewed Map / Manifest / Catalog / rule-index。待 branch diff 与 integration 后复核。

## 10. Consumer integration / CI

待 PR、Actions、integration 与 integrated-main verification 后回写。

本文件完成后只证明 Consumer-side validation 的事实；`agentic-dev` Issue #122 仍需独立判断 V4-08 PASS / FAIL。本 Consumer 不进入 V4-09。
