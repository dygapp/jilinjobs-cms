---
id: rule:high-cost-runtime-activation
type: rule
status: active
scope:
  phases: []
  activities: []
  technologies: []
  artifacts: [github-actions, review-environment, historical-migration]
  risks: []
---

# 高成本验证与 Runtime 激活边界

涉及 GitHub Actions、Review Environment 或 Historical Migration 等高成本 Runtime 时，只激活能够证明当前 Evidence Claim 所需的最小范围；“能力 / workflow 存在”不等于“当前任务必须运行”。

## 最低充分验证

- read-only 状态检查不启动 CI、Review Environment、Migration、Browser 或 Human Review；
- docs-only / Authority-only 变更，在未改变产品行为、Runtime config、Migration、Fixture、Workflow 或版本化资源时，优先使用精确 diff、Authority consistency 与必要 targeted static checks；
- Workflow / Runtime configuration / migration activation 变化先验证 trigger、scope、syntax 与受影响 Runtime，只在 Evidence Claim 需要时升级 Browser / Human Review；
- 产品实现仍必须满足当前 Requirement / Specification 与 `docs/technical/verification-strategy.md`，不得借“最低充分”降低真实 Acceptance。

## Main Historical Migration

Main historical migration execution 当前保持 **FROZEN / explicit reactivation only**：

- `data-migrations/main/**` 的 accepted canonical data / provenance / reports 保留；
- 普通 Fresh Context、Planning、docs / governance maintenance、一般 Feature PR / CI / Review Environment 不得隐式重新执行 Main source discovery、promotion、retry、canonical import / reconcile 或 migration Human Review；
- 只有项目负责人明确要求重新处理 Main migration，并以独立 Main migration responsibility 恢复当时所需 Authority 后，才允许重新激活相关 workflow；
- 修改相邻文件或仅出现 migration artifact 不构成解冻授权。

Party historical migration 不因 Main freeze 被禁用；Party 当前 Authority / workflow 保持自身边界。

## Review Environment

`review-environment.yml` 是 Human Review / Review Runtime 能力，不是每个 PR 的默认 CI。当前稳定激活方式为：

- `workflow_dispatch`；
- PR 显式 `human-review` label。

普通 PR 的 `opened` / `synchronize` / `reopened` 不应被解释成 Human Review 授权。

## 变更本边界

若未来重新设计 Main migration、Review Environment 或高成本验证 topology，必须先恢复对应 Requirement / Planning / Verification Authority，并重新取得与新 topology 匹配的 Evidence；不得只修改本 Rule 文案来假定 Runtime 已改变。