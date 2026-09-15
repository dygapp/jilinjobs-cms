# 技术权威文档（Technical Authority）

本目录只保存当前仍需要跨 Execution Unit 协调的长期 Technical HOW、Verification Strategy，以及 Foundation Rebuild 期间尚未移除的过渡 locator。

Technical Authority 不拥有 Product Requirement、Domain semantics 或长期 Architecture State，也不因历史上曾存在 Requirement / Specification / Technical 同名三件套而自动保留对应正文。

当前治理原则：

- Feature / change 的 Scope、Observable Behavior、Failure Behavior 与 Acceptance 由 `docs/specifications/` 的对应 Specification 持有；
- 多 Feature 长期共享的系统边界、ownership 与不可轻易逆转的结构性约束由 `docs/architecture/` 持有；
- 只有确实需要跨 Execution Unit 持续协调的 HOW 才保留为根级 Technical Authority；
- 能由 Architecture、Specification、Repository implementation 或 GitHub 原生状态唯一恢复的实现快照、migration inventory、完成计划和验证日志不在本目录长期重复维护；
- `verification-strategy.md` 只维护跨 Feature 的验证分层、Runtime composition、Evidence contract 与失败分类，不缓存当前 Workflow / migration inventory；
- 标记为 `transitional-locator` 的文件只用于本轮 Issue #153 治理期间兼容旧引用，不构成第二份 Current Authority。

`archive/` 保存已明确退出 Current Authority、但仍有证据追溯价值的历史 Technical / Planning 资产。archive 默认不参与 Fresh Context。