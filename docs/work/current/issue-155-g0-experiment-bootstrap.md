---
id: work:issue-155-g0-experiment-bootstrap
type: work
status: active
---

# Issue #155 — G0 实验启动与基线升级

## 目标

本单元只完成 Full Documentation Authority Rebuild & Regenerability Validation 的实验启动与 prerequisite capability upgrade，不进入 G1 文档治理。

协调入口：Issue #155。

起始 Consumer `main`：

`ef5709709b3f9f8bb16e28146c4618def8b822b2`

本单元允许只读核验的 upstream validation provenance：`dygapp/agentic-dev#140`；待评估固定 baseline：

`dygapp/agentic-dev@ce28ec748f28a58b2bb65bf75db353f5f50f772d`

## Direct Responsibility

1. 从 Consumer-local Authority 恢复当前 Repository / Method / Rule / Work contract；
2. 对 `ed1a4446f0430890e7ad39673ac9c2e341e6a829 → ce28ec748f28a58b2bb65bf75db353f5f50f772d` 的本实验直接 semantic delta 做显式 disposition；
3. adopt / adapt / reject `method:requirement-baseline-establishment`、`method:architecture-clarification`、`architecture:requirement-authority` 与 `method:ai-development` return-contract delta；
4. 让 Consumer-local Method selector 与接受结果一致，并使 retired `method:software-project-clarification` 退出 Current Authority；
5. 建立 Issue #155 的 G0～G7 lifecycle 与 Failure Evidence capture protocol；
6. 完成必要静态检查、documentation governance、Rule Discovery validation、Method/selector consistency、Review、PR/Integration 与 Post-Integration closure；
7. 完成后恢复 ordinary runtime `upstream access = 0`，并只留下 G1 的 Repository locator / 启动条件。

## Scope Boundary

本单元允许修改：

- Consumer-local Method / engineering capability Architecture；
- Project Capability Profile / Method selector；
- Method / Architecture Human navigation；
- Current Work / Roadmap / experiment coordination 所需治理 locator；
- 为本次 Method / capability migration 保持 current 的 governance verification contract。

本单元禁止修改：

- `docs/requirements/**`；
- `docs/specifications/**`；
- Product Architecture；
- `docs/technical/**`；
- Product Verification contract；
- 任何 JilinJobs Product Requirement / Product behavior。

Human Guide 只可用于 adoption 理解，不得成为 Consumer runtime Authority。

## Rule Discovery Evidence

在首次外部副作用前，已按当前 Consumer-local selector 对当前 `main` 的 17 条 Rule Front Matter 运行 task-level Rule Discovery。当前 G0 signals 命中：

- `rule:async-operation-bounded-observation`
- `rule:cross-repository-authorization`
- `rule:evidence-claim-reuse-across-commits`
- `rule:evidence-type-must-match-claim`
- `rule:execution-continuity`
- `rule:git-commit-governance`
- `rule:high-cost-runtime-activation`
- `rule:human-facing-content-integrity`
- `rule:integration-state-closure-review`
- `rule:read-only-state-inspection`
- `rule:safe-external-write`
- `rule:verification-contract-currentness`

`read-only-state-inspection` 不阻止本单元，因为当前 Human Authority 已明确授权新的治理责任；跨仓库边界保持 Consumer 可写、`agentic-dev` 只读。

当责任切换到 verification-contract repair 与 documentation-governance convergence 时，已分别在下一次副作用前重新执行 task-level Rule Discovery，并按新 signals 读取命中的 current Rules。

## Semantic Disposition Target

目标关系：

```text
Raw / fragmented Requirement inputs
        ↓
method:requirement-baseline-establishment
        ↓
Requirement Baseline Ready
        │
        ├─ Architecture Context sufficient
        │       ↓
        │   method:ai-development
        │
        └─ systemic architecture blocker
                ↓
        method:architecture-clarification
                ↓
        Architecture Context Ready
                ↓
        method:ai-development
```

不得长期保留只负责串联上述 Method 的 compatibility super-method。

## Completion Gate

只有同时满足以下条件才关闭 G0：

- Consumer state、upstream Issue #140 与目标 baseline 已重新核验；
- 四类 direct capability delta 已显式 adopt / adapt / reject；
- Method selector 与采用结果一致；
- retired super-method 不再构成 competing Current Authority；
- 没有修改 Product Requirement / Specification / Product Architecture / Technical / Product Verification contract；
- Issue #155 已持久化 G0～G7、Behavioral Regenerability、Technology Substitutability 与 Failure Evidence protocol；
- targeted static / governance / selector consistency / Review 全部通过；
- G0 PR 已 Integration，实际 integrated `main` 已完成 Post-Integration verification；
- ordinary runtime 恢复 `upstream access = 0`；
- 下一单元只有 `G1 — Source & Authority Establishment`，且当前会话不进入 G1。

## Next Gate

G0 完成后，G1 只能在新的 Fresh Context 中从：

1. 当前 Consumer Repository Authority；
2. Issue #155；
3. 当时重新建立的 Current Work locator；

恢复并启动。G0 不预生成 G1 分析结论。
