---
id: technical:verification-strategy
type: technical-strategy
status: active
relations:
  architecture:
    - docs/architecture/cms-architecture.md
  requirements:
    - docs/requirements/information-publishing.md
    - docs/requirements/cms-domain.md
updated_at: 2026-09-15
---

# 验证运行策略

## 1. 文档责任

本文只维护 `jilinjobs-cms` 跨 Feature 长期需要共享的验证分层、Runtime composition、Evidence claim 与失败分类策略。

本文不缓存当前 Flyway 文件名、具体 migration 编号、固定域名 / proxy 名、当前 Workflow inventory、测试数量或某个 Execution Unit 的验证清单；这些高频事实由 Repository implementation、Workflow、Work lifecycle 与 GitHub 原生 Evidence 持有。

更细粒度的验证约束通过当前任务的 Consumer-local Rule Discovery 按需激活；本文不复制 discoverable Rule 正文。

## 2. 核心原则

验证必须区分至少四类事实：

- **Implementation Exists**：代码 / 配置 /资源已经存在；
- **Automated Verification Evidence**：自动化构建、测试、Runtime 或 Browser 验证已经对目标提交成功；
- **AI Runtime / Visual Observation**：AI 在真实运行环境中观察到目标行为或视觉结果；
- **Human Runtime Observation**：人工在明确目标环境中完成观察与裁决。

一种 Evidence 不能自动替代另一种。没有与当前 claim、目标提交和运行环境相匹配的证据，不声明相应完成状态。

## 3. Runtime composition

验证环境必须消费真实产品 ownership，而不是由 Test fixture 重建第二份产品基线。

当前长期组合语义为：

```text
Generic schema ready
→ JilinJobs Site Definition composition
→ stable asset projection
→ optional one-time Runtime defaults
→ optional Historical Canonical Migration
→ Application / Browser / Human Review Runtime
```

约束：

- Generic schema evolution 不负责创建 JilinJobs instance data；
- Site Definition 提供 stable structure / accepted defaults / stable assets；
- one-time defaults 只在明确需要的 Fresh / adoption 场景执行；
- Historical Migration 是独立可选输入，不成为 Site Definition；
- Test fixture 只补充当前验证场景数据，不重新创建长期栏目、页面、站点资产或 canonical historical baseline。

Runtime composition 的长期 ownership 以 `docs/architecture/cms-architecture.md` 为准。

## 4. 验证分层

### 4.1 Backend

Backend 变更至少按风险选择编译、静态检查、自动化测试、可执行产物构建与必要 Runtime startup。

涉及 schema / initialization / migration 的变化，仅检查 SQL 文件、编译或已有数据库增量运行不能单独证明 Fresh Runtime 可建立；需要时必须覆盖从空环境开始的真实组合路径。

### 4.2 Frontend

独立前端应用分别拥有自己的 type-check、build 与 Runtime / Browser Evidence，不能用一个应用的成功替代另一个应用。

Vue / TypeScript 变更按真实风险选择验证层：

- template、props、emits、类型契约：至少 Vue-aware type-check；
- reactivity、watch、lifecycle、async side effect：追加能证明状态与时序正确的行为测试；
- Router、DOM、用户交互：追加 Browser Verification；
- build / module / tsconfig：追加正式 build；
- 存在视觉 Acceptance：在功能验证之外取得对应 AI / Human Visual Evidence。

验证以当前 Consumer 实际 package、tsconfig、Workflow 与 Repository Authority 为准，不为匹配外部 Technology Profile 机械升级依赖。

### 4.3 Browser / E2E

Browser Verification 用于证明路由、交互、资源加载、异步状态与已编码 Acceptance。

对分页、Top-N、作用域过滤与异步页面：

- 有稳定业务作用域时直接验证该作用域，不以“全局第一页再前端过滤”替代；
- 测试数据应能够穿越真实分页 / window boundary，或直接断言正确查询约束；
- 不以固定 `sleep`、DOM 早期存在或 `page.goto()` 返回作为异步装配完成条件；
- Router / query / watcher 驱动页面应覆盖 stale response 不得覆盖当前路由状态的时序风险。

Functional Browser PASS 不自动等于 Visual Fidelity PASS。

### 4.4 Human Review / Review Environment

需要人工运行时观察的工作必须使用与目标 claim 对应的可复现 Review Runtime。

如果同一环境先执行自动 E2E、后供人工评审，必须在两者之间显式恢复干净 baseline，避免自动测试数据、缓存、会话或临时资源泄漏到 Human Review。

Review Environment 的共享外部资源、并发互斥、临时代理、bind mount ownership 与清理方式属于 Repository / Workflow implementation responsibility；验证只要求它们满足：

- 不让独立工作错误共享或覆盖同一外部资源；
- 清理后可从版本化 Authority 恢复目标 baseline；
- 外部访问、内部 target、Run ownership 与释放状态分别可验证；
- 临时验证授权不扩张为 Production / shared data 的破坏性操作授权。

## 5. 失败分类

遇到 Test、Workflow、fixture、snapshot 或 Runtime failure，先建立 Expected vs Actual，再至少区分：

```text
Implementation Defect
Stale Verification Contract
Runtime / Environment Problem
External Dependency Problem
```

处理原则：

- Implementation Defect 修复实现；
- Stale Verification Contract 修复过期测试 / workflow assertion，不恢复已经被新 Authority 取代的旧产品行为；
- Runtime / Environment Problem 修复环境或组合链，不把环境失败伪装成产品通过；
- External Dependency Problem 明确记录外部依赖与阻断范围，不用本地 mock 证明真实外部集成已经完成。

如果同一产品语义在多个测试层重复硬编码，应回到真正的 Requirement / Specification / Architecture owner，并减少第二套契约。

## 6. Evidence contract

对目标 PR / commit 的 Completion claim 应能关联：

- target Head / commit；
- Event / Run / Job / Step；
- conclusion；
- claim 所需的 logs / artifact / runtime observation。

历史 Run 不能自动成为当前 Head 的 Run。

如果 Artifact 本身是必要 Evidence，上传步骤成功不能单独证明 Artifact 实体存在；需要重新读取目标 Run 的 Artifact 并核对其归属。

祖先提交的高成本 Evidence 只有在完成精确 compare、逐项证明当前差异不会影响对应 claim，并对当前 Head 执行必要 targeted verification 后，才能按 claim 复用。复用时必须保留 Evidence Commit、Current Target、compare range 与仍需重新验证的 claim；不得把祖先 Run 改称当前 Run。

## 7. Verification 与 Authority 的关系

验证证明实现是否满足当前 Authority，不反向创造 Product Requirement。

当测试、Workflow assertion 或 fixture 与当前 Requirement / Specification / Architecture 冲突时，先判断是否为 stale verification contract；只有 Authority 本身存在真实歧义 / 缺口时才回到对应 owner。

验证策略不维护第二份：

- active migration inventory；
- Site Package 文件清单；
- Runtime resource count；
- Workflow / job 名称清单；
- 固定 Review Environment endpoint inventory；
- 当前 Execution Unit checklist。

这些事实应从其真实 Repository / Work / GitHub owner 恢复。