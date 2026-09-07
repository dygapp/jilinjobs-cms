# Work Lifecycle

`docs/work/` 只承担 Execution Unit / execution evidence 的生命周期组织，不是第二份 Roadmap。

- `current/`：仅放已经通过 `readiness-check`、仍处于 Execute / Verification / Integration / Post-Integration closure 中的 active work artifact；
- `archive/`：已完成 Execution Units、历史预编号计划、incident / verification records 与其他 `HISTORICAL_EVIDENCE`；
- 本目录根级除 README 外不保留 Execution Unit 文件。

Execution Unit 的稳定 Identifier 只承担追踪身份；只有当前 Readiness PASS 才授予 Execute Authority。已完成 artifact 即使包含 `Ready`、`Next Step` 或旧 Execute wording，也不得从 archive 重新获得执行权限。

Fresh Context 先从 `AGENTS.md`、Roadmap、controlling Issue 与 GitHub Current Evidence 判断是否存在 Ready Execution Unit，再读取 `current/`；archive 默认不参与恢复。
