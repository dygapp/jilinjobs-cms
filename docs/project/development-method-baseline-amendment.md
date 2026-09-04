# Consumer Method Baseline Amendment — 2026-09-04

## Status

**CURRENT**

本文是 `docs/project/development-method.md` §1 的定向 baseline 修订；普通开发仍以 `AGENTS.md` 和 Consumer-local 方法正文为主。

## Validation Baseline

```text
Repository:
dygapp/agentic-dev

Validation Ref:
master

Validation Commit:
394d1c3cde04b35940d5e33b7cbcaaf6557678ce
```

正式 tag `baseline-2026-09-04-engineering-capability` 仍停留在：

`5be2e6aad29b2be6b8535b3690daf3533ee22a46`

当前 Consumer 显式采用 `master@394d1c3...`，因为它比该 tag 多 1 个已复核的 Stable Maintenance commit；这次升级**不改变 Method Stage、Core Principles、Engineering Discipline Set、Technology Profile 或 Skill Contract**。

`docs/project/development-method.md` 中仍显示旧 Tag/Commit 的 baseline block 由本文定向 supersede；其余 Consumer-local 方法正文继续有效。后续安全整版编辑该大文档时，应把本文折叠回 §1。

## Adopted Stable Maintenance

### 1. Ephemeral Evidence Promotion

Workflow Artifact、临时 Snapshot、外部输出等执行期证据，如果经过验证并被接受为后续迁移、回放、评审或 Runtime 的长期输入，必须显式晋升为 Consumer-owned durable input：

- 进入本仓库版本控制或其他明确长期 Authority；
- 保留来源 Run / Head / Artifact 与必要 digest；
- 长期消费者切换到 durable input，不继续依赖会过期 Artifact；
- 晋升改变 Current Evidence 输入时，重新取得受影响验证。

EU-29 canonical migration dataset 与 EU-30 theme-education candidate extension 继续按该规则管理。

### 2. Review Environment Ownership / Lease

单实例、长生命周期 Human Review Environment 必须具有可观察生命周期：

- owner；
- lease acquire / renew / expiry / release；
- stale-run policy；
- automated verification 与 Human Review 可采用不同生命周期；
- 不机械套用 latest-head-wins；优先级由 Consumer Review Policy 决定；
- takeover / release 必须可审计且最小权限；
- 新自动化运行不得无条件销毁仍处于有效 Human Review lease 的环境。

## Non-adopted Project Facts

本次 baseline 更新不继承 `agentic-dev` 自身 Roadmap、Foundation 状态、eval、Issue、PR 或实验结论为 `jilinjobs-cms` 项目事实。
