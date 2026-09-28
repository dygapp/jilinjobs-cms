# 正式工作生命周期

docs/work/ 只在当前变化确实需要独立恢复、依赖协调或独立验收的正式 Execution Unit 时使用。局部、低风险、可逆且 Authority / Acceptance 已清楚的直接变更不需要为了形式完整创建 Unit。

## Current locator 说明

docs/work/current/README.md 只定位仍处于正式执行 / 验证 / 集成闭环中的 active Unit。

- current/：正式 active Unit 的可恢复 work artifact；
- archive/：已完成 Unit 与历史 execution / verification evidence；
- 根目录除本 README 外不保存 Unit artifact。

current locator 的 NONE 只表示当前没有正式 active Unit，不表示没有用户明确授权的新工作、直接有界变更或 planning candidate。

## 正式 Unit 的本地约束

当工作选择使用正式 Unit 时：

1. 在进入实质执行前，Unit 必须具有可恢复 Scope、Authority inputs、Dependencies、Completion Conditions 与 Verification responsibility；
2. Unit 已进入执行生命周期后，把其 artifact 放入 current/ 并更新 current/README.md；
3. implementation integration 不自动等于 closure；需要的 post-integration evidence 完成后，artifact 才移入 archive/，locator 恢复为 NONE；
4. 历史 PR、Issue comment、archive 中的 READY / PASS 或旧 Head evidence 不重新授予执行权。

是否需要 Unit、何时执行 slice-work / readiness-check / execute-unit 等 procedure，由当前事实与 installed Skill 的 Trigger 决定，本文件不复制 Skill procedure。

## Fresh Context 恢复

只有当前任务涉及正式 active Unit 时，Fresh Context 才需要协调：

- docs/work/current/README.md；
- active work artifact；
- 任务相关 Open Branch / PR；
- 与当前 claim 对应的 Actions / Review / runtime evidence。

locator、active artifact、GitHub 状态或必要证据无法协调时失败关闭该正式 Unit；这不把 unrelated repository maintenance 自动变成 blocker。
