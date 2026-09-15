# 执行作用域与高成本验证护栏

本文定义 `jilinjobs-cms` 在 Fresh Context、Repository 状态检查、验证升级和 Historical Content Migration 执行方面的 Consumer-local 护栏。其目标是避免只读状态恢复被无意扩张成新的开发 / 修复生命周期，也避免与当前任务无关的高成本迁移或 Runtime 验证作为副作用执行。

## 1. Fresh Context 状态检查停止规则

当用户当前目标只是恢复、检查、复核或总结 Repository 状态时，该工作默认是 **read-only status inspection**。

如果恢复结果为：

```text
Current Ready Execution Unit = NONE
```

则默认在完成以下事项后停止：

1. 核对当前 Repository Authority、Roadmap、Current Work、Open PR / Issue 与必要 Current Evidence；
2. 准确报告当前状态和下一自然 Planning / Readiness Gate；
3. 不自动创建新的 Planning Candidate、Execution Unit、Branch、Commit、PR、Workflow Run 或其他修复生命周期。

状态检查期间发现的文档陈旧、描述漂移或可改进项必须先分类：

- **Blocking Authority Drift**：已经使当前 Authority 无法被一致解释，记录为 blocker；只有当前用户目标已授权修复，或用户随后明确授权修复时才进入修改流程；
- **Non-blocking Drift / Maintenance Finding**：报告并保留为后续 maintenance finding，不把“顺手修一下”升级成新的 PR 生命周期。

一个已完成并终止 Execute Authority 的 Execution Unit 只允许在新的工作明确依赖其集成结果时作为 dependency evidence 被读取；不得把 dependency revalidation 解释为重新打开该 Unit。

## 2. Verification Scope Selection

验证按照当前变更和 Evidence Claim 选择最低充分范围，不把“存在 workflow”解释为“每次都要运行该 workflow”。

### 2.1 状态检查与 docs-only

仅涉及 Repository 状态读取时，不运行 CI、Review Environment、Migration、Browser 或 Human Review。

仅修改 Markdown / Authority / Work 状态文档，且没有改变产品行为、Runtime configuration、Migration、Fixture、Workflow 或版本化资源时：

- 默认不运行 Backend / Frontend build、Integrated Browser 或 Review Environment；
- 使用精确 diff、Authority consistency 和必要的 targeted static checks 作为当前变更证据；
- 祖先 Runtime / Human Review Evidence 是否可复用继续服从 `docs/project/development-method.md` 的 Evidence Claim 影响判断，不把 docs-only 自动等同于所有祖先证据均可复用。

### 2.2 Workflow / Runtime 变更

修改 Workflow、Runtime configuration、Migration activation 或 Review Environment 时，只升级到受影响的验证层：

- 先验证 workflow trigger / scope / syntax 与精确 diff；
- 只有实际 Evidence Claim 需要时才运行对应 Runtime / Browser / Human Review；
- 不因一次 workflow policy 修订重新执行与其无关的 Historical Content Migration。

### 2.3 Product implementation

产品实现仍按 `docs/technical/verification-strategy.md` 和当前 Requirement / Specification 的风险映射选择 Backend、Frontend、Browser、Runtime 与 Human Review，不因本护栏降低真实 Acceptance 所要求的验证。

## 3. Main Historical Migration 临时冻结

截至本规则生效时，Main historical Article migration 已完成 EU-50 / EU-51 的既有闭环；后续 230 篇 deferred problem Articles 与 6 篇 source-defect Articles仍属于独立 later-review / customer-confirmation backlog。

当前对 **Main historical migration execution** 采用临时冻结：

- `data-migrations/main/**` 已接受 canonical data、provenance、reports 与 migration implementation 保留原样，不删除、不重写；
- 原 Main EU-50 / EU-51 Actions workflow 原样保存在 `.github/frozen-workflows/main-migration/`，不位于 `.github/workflows/`，因此不参与普通 GitHub Actions trigger；
- 普通 Fresh Context、Planning、docs / governance maintenance、一般 Feature PR、CI 和通用 Review Environment 均不得自动执行 Main source discovery、eligibility promotion、targeted retry、canonical import / reconcile、imported browser 或 Main migration Human Review；
- 只有项目负责人明确要求重新处理 Main migration，并把该工作限定为独立 Main migration process 后，才允许按当时 Repository Authority 重新激活必要 workflow；不得因修改 `data-migrations/main/**` 或其他相邻文件而隐式解冻。

冻结的是 **Main migration execution capability 的默认激活**，不是删除 Main canonical evidence，也不是改变已经接受的产品数据事实。

## 4. Party Migration 保留

Party historical migration 不在本次冻结范围内：

- `data-migrations/party/**`、Party canonical evidence 与 Party migration compatibility 继续保持当前 Authority；
- Party 专项 Actions workflow 保持可用，并继续只按其已有 Party / Generic migration 相关触发边界工作；
- 通用 Review Environment 只有在显式请求 Human Review 时才允许启动，因此其中的 Party canonical import 不再作为普通 PR 创建 / 同步的副作用执行；
- 后续如果需要进一步拆分 Party Human Review，可在独立 Party 工作中处理，本次不以冻结 Main migration 为理由削弱 Party 已接受能力。

## 5. Review Environment 激活边界

`review-environment.yml` 是 Human Review / Review Runtime 能力，不是每个 PR 的默认 CI。

允许的激活方式：

- `workflow_dispatch`；
- 对 PR 显式添加 `human-review` label。

普通 PR 的 `opened`、`synchronize`、`reopened` 不再自动启动 Review Environment。这样可以继续保留 Party Human Review、FRP lease 和完整 Review Runtime，同时避免无人工评审需求的状态检查、文档修订和普通 PR 被动执行长链 Runtime / migration workload。

## 6. 后续调整原则

如果未来需要恢复 Main migration、重新设计 Review Environment 或增加新的高成本验证：

1. 先确认当前 Requirement / Planning Authority；
2. 把高成本流程限制到能证明其必要性的变更范围或显式 trigger；
3. 保留 Party 与 Main 各自边界，不因一方冻结或恢复隐式改变另一方；
4. 新规则具有长期价值时回写 Consumer Authority，不依赖 handoff prompt 维持。
