# V4-08 Consumer 验证证据（agentic-dev）

状态：**IN PROGRESS / UPGRADE-ONLY EVIDENCE**。

本文件记录 `dygapp/agentic-dev` Issue #122 — V4-08 在真实 Consumer `dygapp/jilinjobs-cms` 中的可复核证据。它不构成产品 Requirement、Planning Authority、Execute Authority、Rule routing table 或普通运行依赖。

## 1. 精确基线

- Consumer adoption base：`b5590246f2a548e9734b3c3a7f60dae9376c8f39`
- Consumer base 状态：Open PR = 0；`docs/work/current/README.md` 的 Current Execution Lifecycle locator 结果为 `NONE`
- base CI：`CI` / `文档治理检查` 均 SUCCESS
- Consumer 既有 Current Evaluated Baseline：`agentic-dev@1c8cdfea9ecf23ef33ffab20eec3c93679fd4578`
- V4-08 adopted upstream exact candidate：`agentic-dev@3e0b2f5a29caeb344da79f8c96ebffbeb5c2b0cb`
- 选择理由：该 SHA 是 Issue #122 已完成 V4-07 Token Scaling Gate 的精确 runtime candidate；PR #123 后继 Head 相比它仅包含 upstream Project Roadmap 状态提交，未作为 Consumer ordinary-runtime 能力输入。

## 2. Adoption ownership mapping

本次只采用 / 适配跨项目可复用的 V4 Foundation 语义，不复制 upstream 项目状态：

- `AGENTS.md`：保留既有 Consumer Authority，仅增加 V4 local-discovery 稳定入口与 baseline provenance；
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

初始 adoption commit `f23d7baef4bd09fdba895a6cb0ce56600974f48a` 的 compare 只包含 `AGENTS.md`、Method / Evidence、Rules、Skills、Rule Discovery tool / tests 与对应 Workflow。后续差异复核发现该 commit 对 `AGENTS.md` 的改写范围过宽，因此已在后继 commit 恢复既有长期 Consumer 语义，仅保留必要 V4 增量；这是 scope correction，不改变产品语义。

## 4. Local deterministic evidence

初始 adoption Head：`f23d7baef4bd09fdba895a6cb0ce56600974f48a`。

- `Rule Discovery` Run：`34765074305`
- Job：`103744482336`
- Head SHA：精确匹配 `f23d7bae...`
- conclusion：`success`
- deterministic tests：7 / 7 PASS
- lint：`{"status":"ok","rules":15,"skills":9}`
- generation discovery step：SUCCESS
- verification discovery step：SUCCESS

该证据证明 Rule / Skill metadata、三态 task signals、每维 6-token 上限、duplicate-id fail-closed、locator-only output 与两条真实 Consumer discovery trace 均能在 GitHub Actions 中执行。

## 5. Ordinary generation discovery trace

真实 responsibility：实现本次 Consumer-local Rule Discovery / governance adoption。

Task signals 来自当前 Consumer 实现责任，不从预期 Rule id 或未命中 metadata 反向构造：

```json
{
  "phases": ["execute"],
  "activities": ["implementation"],
  "technologies": ["python"],
  "artifacts": ["code", "configuration", "test"],
  "risks": []
}
```

实际 local discovery：

```json
{
  "status": "ok",
  "scanned": 15,
  "candidate_count": 3,
  "candidates": [
    {"id": "rule:exact-machine-identifiers", "path": "docs/rules/repository/exact-machine-identifiers.md"},
    {"id": "rule:implementation-minimality", "path": "docs/rules/generation/implementation-minimality.md"},
    {"id": "rule:surgical-change", "path": "docs/rules/generation/surgical-change.md"}
  ]
}
```

Semantic applicability confirmation：三条均适用。

- `exact-machine-identifiers`：本次实现涉及路径、SHA、JSON fields、CLI 参数与 Rule / Skill ids，必须保持机器标识精确；
- `implementation-minimality`：本地 Tool 采用 Python stdlib、小型 Front Matter subset 与选择性 Rule projection，没有复制 upstream 全量 taxonomy / assets；
- `surgical-change`：最终差异必须限制在本次 governance / discovery adoption 与验证责任。

ordinary runtime 只读取上述 candidate Rule bodies；未通过 candidate output 得到的 Rule locator 未用于本次 generation 决策，也未访问 upstream current state 作为 fallback。

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

实际 local discovery：

```json
{
  "status": "ok",
  "scanned": 15,
  "candidate_count": 3,
  "candidates": [
    {"id": "rule:evidence-claim-reuse-across-commits", "path": "docs/rules/verification/evidence-claim-reuse-across-commits.md"},
    {"id": "rule:evidence-type-must-match-claim", "path": "docs/rules/verification/evidence-type-must-match-claim.md"},
    {"id": "rule:integration-state-closure-review", "path": "docs/rules/repository/integration-state-closure-review.md"}
  ]
}
```

Semantic applicability confirmation：三条均适用。

- ancestor evidence 只能按 exact diff + claim impact 复用；后继 Head 的受影响 claim 需要重新运行；
- PASS / integration claim 必须由对应类型的 Current Evidence 支持；
- PR merge 后仍需在 integrated `main` 做 Post-adoption / Post-integration closure，不能把 PR Head success 扩大为 integrated-main success。

本次真实验证保留了 evidence gap：初始 Head 的 `文档治理检查` Run `34765074269` 因 Evidence 一级标题英文占主导而 FAILURE，没有把 Rule Discovery SUCCESS 扭曲成整个 PR PASS。该缺陷通过修改真实文档修复，不扩大文档治理例外。

## 7. Rule evolution

当前 adoption 之后**尚未自然发生 Rule 新增 / 修改**。初始验证暴露的是 Evidence 文档标题与 `AGENTS.md` diff scope 问题，不是 Rule metadata / body defect，因此不能伪装成 Rule evolution。

状态：**NOT YET NATURALLY OBSERVED**。

本项不通过人为制造产品或工程规则取得证据；是否构成 V4-08 Gate blocker 由 `agentic-dev` 独立判断。

## 8. Post-adoption decoupling

在 PR integration 完成后，必须在 Consumer integrated `main` 上再次执行 ordinary runtime。该段验证开始后只允许依赖 Consumer-local Authority / Method / Skills / Rules / Rule Discovery 与 GitHub Consumer Evidence：

- upstream current branch / Roadmap / Issue / PR access = 0；
- online latest-baseline lookup = 0；
- Rule candidate locator 只来自 Consumer-local discovery；
- upstream 后续变化不自动改变 Consumer ordinary runtime。

实际 integrated-main trace 将固化到 Consumer PR #150 的 GitHub Current Evidence surface；本文件不通过递归修改自身来制造“记录最后一次记录提交的 Run”的无限尾部更新。

## 9. Central synchronization asset check

当前 adoption changed-file set 未建立 Reviewed Discovery Map、Activation Manifest、Runtime Catalog、rule-index 或 `rule → signals` 人工映射。Rule routing 只依赖 Rule 文件自身 metadata + deterministic local discovery；场景 tests 只承担验证，不是 runtime routing source。

最终 integration 前再次以 PR changed-file set 复核。

## 10. Consumer integration / CI

- PR：`dygapp/jilinjobs-cms#150`
- 初始 Head `f23d7bae...`：Rule Discovery SUCCESS；文档治理因标题真实失败；全量 CI 在该 Evidence 记录形成时仍在运行。
- 后继修复只收敛 `AGENTS.md` scope 与 Evidence 文档治理问题；所有受影响 workflow 必须绑定新的 exact Head 重新取得 Current Evidence后方可集成。
- merge 后仍需 integrated-main Actions 与 post-adoption local-only trace。

本文件完成后只证明 Consumer-side validation 的事实；`agentic-dev` Issue #122 仍需独立判断 V4-08 PASS / FAIL。本 Consumer 不进入 V4-09。
