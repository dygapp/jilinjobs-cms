# 正式工作生命周期

`docs/work/` 只在当前变化确实需要独立恢复、依赖协调或独立验收的正式 Execution Unit 时使用。局部、低风险、可逆且 Authority / Acceptance 已清楚的直接变更不需要为了形式完整创建 Unit。

## Working set 与历史归档

- `docs/work/*.md`（排除本 README）是 Execution Unit working set；目录位置不表示 Unit 是否 active，生命周期以各 Unit 文件头为准。
- `docs/work/archive/` 是历史冷存储，只承担 provenance / traceability，不定义当前 Execute Authority。
- Unit 完成后不要求立即移动到 `archive/`；已完成 Unit 可以继续留在 working set，后续由人工或有界 Repository maintenance 批量归档。
- Archive relocation 是 housekeeping，不是 Unit closure、Integration 或 Post-Integration 的组成部分；不得仅因为一个 Unit 完成就机械创建独立归档 Commit。
- 根目录除本 README 外只保存 Execution Unit，不混入 review draft、临时 evidence、scratchpad 或其他工作类型。

## Execution Unit 文件头

Working set 中每个 Unit 必须使用 YAML Front Matter，并至少包含：

```yaml
---
id: execution-unit:euNN-example
type: execution-unit
status: ready
---
```

`status` 允许：

- `planned`：已形成有界 Unit，但尚未通过 readiness；
- `ready`：Readiness 已满足，可进入执行；
- `active`：存在需要跨 Context 持续恢复的执行中状态；仅在确有用途时使用，不为记录执行步骤机械改写；
- `blocked`：存在阻断当前 Unit 继续执行的真实 blocker；
- `completed`：Unit 范围内的实现已经收敛到最终 working-state candidate，当前没有已知需要继续执行的 Unit 工作；它是待 exact-head Verification / Convergence 证明的候选状态，不等于 `READY TO INTEGRATE`、已集成或 Post-Integration PASS。

`readiness`、`base_sha`、`branch`、`started_at`、`completed_at` 等只在有实际恢复价值时记录。不要为了完整感缓存可以从 Git / GitHub / CI 唯一恢复的瞬时事实。

特别是 `verified_head_sha`、`integrated_sha`、push 状态、Actions Run 与 Post-Integration 结果不属于新 Unit 的必备 metadata。既有 archive 中已经保存这些字段的历史记录保持原貌，不批量重写。

## 完成与 Repository integration 的边界

Execution Unit lifecycle 与 Git / Repository integration lifecycle 分离：

```text
Unit implementation / scoped working-state verification
→ status: completed
→ Candidate Commit
→ exact-head Verification / Convergence
→ Ready to Integrate
→ Repository integration / push / post-integration verification
```

当 Unit 范围内的实现已经收敛、没有已知需要继续执行的 Unit 工作时，应在形成最终逻辑 Candidate Commit 前把 `status` 一并收敛为 `completed`。该状态是候选声明，后续 exact-head Verification 或 Convergence 若发现实现缺口，继续在同一 Working State / Candidate 上修复，并按事实恢复为 `active` 或 `blocked`；Convergence 返回 `READY TO INTEGRATE` 后不再为了记录该结果修改 Unit。

Unit 状态收敛本身属于同一逻辑变化的 Working State，不应形成独立 closure Commit。任何 amend / rebase 导致 Candidate SHA 变化后，都必须按 Git Commit 与 Verification policy 重新判断受影响 Evidence；只有能够证明不受差异影响的 claim 才可按既有 Evidence reuse 规则复用。

若 Repository policy 要求 integration 后验证，该验证仍必须完成，但它证明 Repository / integration claim，不延长 Unit artifact 的写入生命周期。

## Fresh Context 恢复

只有当前任务涉及正式 Execution Unit 时才：

1. 读取本 README；
2. 扫描 `docs/work/*.md`（排除 `README.md`）文件头；
3. 根据当前任务定位相关 `planned / ready / active / blocked` Unit；`completed` Unit 仅作为近期 historical work，不授予新的 Execute Authority；
4. 重新核验相关 Repository、Branch / HEAD / working tree、必要的 PR / Actions / Runtime evidence。

没有非终态 Unit 不表示没有用户明确授权的新工作、直接有界变更或 planning candidate。Unit metadata、Repository native state 或必要 Evidence 无法协调且影响当前 claim 时，只失败关闭受影响责任。

是否需要 Unit、何时执行 slice-work / readiness-check / execute-unit / converge 等 procedure，由当前事实与 installed Skill Trigger 决定，本文件不复制 Skill procedure。
