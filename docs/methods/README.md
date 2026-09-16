---
id: guide:methods-navigation
type: guide
status: active
---

# Methods 导航

本目录保存 `jilinjobs-cms` 当前采用的一等 Consumer-local Method。这里的 README 只服务 Human View；Agent 的 work kind → Method 选择由 `docs/project/project-capability-profile.md` 持有，Method 生命周期语义由各自 canonical Method 文件持有。

当前长期 Method：

- `requirement-baseline-establishment.md`：新项目或重大 Requirement Authority 重建，把 raw / fragmented inputs 收敛为 `Requirement Baseline Ready`；
- `architecture-clarification.md`：存在跨多个 Feature、长期、高成本难逆且阻塞可靠开发的 systemic architecture driver 时，条件性收敛为 `Architecture Context Ready`；
- `ai-development.md`：Requirement / Architecture Context 已足够时的普通 Feature / change；
- `consumer-upgrade.md`：Existing Consumer 显式评估新的 `agentic-dev` baseline；
- `review-feedback-cycle.md`：Human Review Finding 的批量修复、定向验证与 re-review；
- `method-experiment.md`：Consumer-local 方法 / 工程实践实验与 Evidence promotion lifecycle。

Requirement Baseline Establishment、Architecture Clarification 与 AI Development 是不同 work kind，通过 Return Contract 自然衔接；当前不保留只负责串联 Requirement 与 Architecture 的 `method:software-project-clarification` super-method。

未列出的 upstream Method 不因存在于 `agentic-dev` 就自动成为本 Consumer current capability。
