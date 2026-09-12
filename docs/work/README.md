# Work Lifecycle

`docs/work/` 只承担 Execution Unit / execution evidence 的生命周期组织，不是第二份 Roadmap，也不承担 Planning Candidate 排序。

## Current execution lifecycle owner

`docs/work/current/README.md` 是 **Repository-owned Current Execution Lifecycle Locator**：只回答当前是否存在已经通过 `readiness-check`、仍处于 Execute / Verification / Integration / Post-Integration closure 生命周期中的 Ready / active Execution Unit，以及其对应的 current work artifact 在哪里。

- `current/`：只放已经通过 `readiness-check`、仍处于 Execute / Verification / Integration / Post-Integration closure 中的 active work artifact；
- `archive/`：已完成 Execution Units、历史预编号计划、incident / verification records 与其他 `HISTORICAL_EVIDENCE`；
- 本目录根级除 README 外不保留 Execution Unit 文件。

Execution Unit 的稳定 Identifier 只承担追踪身份；只有当前 Readiness PASS 才授予 Execute Authority。Planning / Requirement Candidate、`PENDING` Readiness 或仅有未来 EU 名称的工作不得进入 `current/`。已完成 artifact 即使包含 `Ready`、`Next Step` 或旧 Execute wording，也不得从 archive 重新获得执行权限。

## Entry / exit lifecycle

进入规则：

1. `slice-work` 形成 Candidate Execution Unit 后仍不进入 `current/`；
2. `readiness-check = PASS` 后，执行分支 / PR 必须在继续实质 Execute 前把该 Unit 的 current work artifact 放入 `docs/work/current/`，并同步更新 `docs/work/current/README.md` 指向该 active Unit；
3. Open PR / Branch 是 GitHub 原生瞬时事实。Fresh Context 在判断 `main` 上的 `NONE` 是否可安全停止前，必须先核对是否存在当前任务相关的 Open execution PR / branch；如果存在，应读取其 current work locator / artifact 与相应 Readiness Current Evidence，而不是把 `main` 的 `NONE` 当作全局无活动工作的证明。

退出规则：

1. implementation Integration 后，Unit 在完成其要求的 Post-Integration verification / closure 前仍属于 active lifecycle；
2. closure 将 completed work artifact 移入 `docs/work/archive/`，并把 `docs/work/current/README.md` 恢复为 `NONE`；
3. closure 后旧 Issue comment、旧 PR body、archive 中的 `READY` / `PENDING` / Execute wording只保留历史证据语义，不再拥有 Current Execute Gate。

## Current-state resolution and fail-closed

Bootstrap / Roadmap surface（`AGENTS.md`、根 `README.md`、`docs/README.md`、Project Roadmap）只维护稳定 Authority、职责与 locator，不重复缓存高频变化的 `Current Ready Execution Unit`、Readiness `PENDING/PASS`、exact execution Head 或类似 Execute Gate 真值。

GitHub PR / Branch / Actions 只对其各自原生瞬时状态负责；这不是“GitHub 永远高于本地文件”的通用优先级规则。Requirement / Specification / Architecture / Consumer-local Method 与本 Work lifecycle 仍按 Repository Authority 各自承担责任。Issue body / comment 可以承载 Planning 或 Current Evidence，但历史 comment 不自动成为当前 Gate owner。

Fresh Context 遇到以下任一情况时必须 **fail closed**，不得进入或继续 Execute：

- `docs/work/current/README.md` 缺失或无法解析；
- locator 与 `current/` active artifact 不一致；
- 同时出现多个未被当前 Authority 明确允许的 active Unit；
- Readiness Evidence、Open execution PR / branch 与 current work lifecycle 无法协调；
- 关键 Current Evidence 缺失、冲突或存在无法消除的歧义。

`NONE` 只表示在完成上述 Current Evidence 协调后，没有 Ready / active Execution Unit；它**不表示没有 Planning Candidate、backlog 或新的用户指定 Planning 目标**。当用户目标仅为 Repository 状态恢复 / 检查 / 总结，并且 Current State 协调后为 `NONE` 时，可以安全报告并停止，不得因此自动选择新 Candidate 或创建新的 Execute 生命周期。
