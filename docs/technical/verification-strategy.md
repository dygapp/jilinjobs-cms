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
  interface:
    - docs/technical/http-interface-contract.md
  design:
    - docs/design/public-site/DESIGN.md
updated_at: 2026-09-23
---

# 验证运行策略

## 1. 文档责任

本文只维护 `jilinjobs-cms` 跨 Feature 长期需要共享的验证分层、运行时组合、证据 claim 与失败分类策略。

本文不缓存当前 Flyway 文件名、具体 migration 编号、固定域名 / proxy 名、当前 Workflow inventory、测试数量或某个 Execution Unit 的验证清单；这些高频事实由 Repository implementation、Workflow、Work lifecycle 与 GitHub 原生 证据 持有。

更细粒度的验证约束通过当前任务的 Consumer-local Rule Discovery 按需激活；本文不复制 discoverable Rule 正文。

## 2. 核心原则

验证必须区分至少四类事实：

- **Implementation Exists**：代码 / 配置 /资源已经存在；
- **Automated Verification 证据**：自动化构建、测试、Runtime 或 Browser 验证已经对目标提交成功；
- **AI Runtime / Visual Observation**：AI 在真实运行环境中观察到目标行为或视觉结果；
- **Human Runtime Observation**：人工在明确目标环境中完成观察与裁决。

一种 证据 不能自动替代另一种。没有与当前 claim、目标提交和运行环境相匹配的证据，不声明相应完成状态。

## 3. 运行时组合

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
- 历史迁移 是独立可选输入，不成为 Site Definition；
- Test fixture 只补充当前验证场景数据，不重新创建长期栏目、页面、站点资产或 canonical historical baseline。

运行时组合 的长期 ownership 以 `docs/architecture/cms-architecture.md` 为准。

## 4. 验证分层

### 4.1 后端

Backend 变更至少按风险选择编译、静态检查、自动化测试、可执行产物构建与必要 Runtime startup。

涉及 schema / initialization / migration 的变化，仅检查 SQL 文件、编译或已有数据库增量运行不能单独证明 全新运行环境 可建立；需要时必须覆盖从空环境开始的真实组合路径。

涉及 Core / Server / Migration boundary 时，验证必须检查**责任性质**而不是只维护已知 class 黑名单：Core 不应暴露 Controller / Servlet / multipart 等 HTTP transport responsibility；Migration application 不应因依赖 Core 获得 ordinary Server transport。新增一个以前不在 inventory 中的 Controller 也必须能被边界验证发现。

### 4.2 HTTP 接口契约

触达 Backend HTTP transport、Admin/Public API adapter、DTO projection、resource transport 或 Backend technology substitution 时，以 `docs/technical/http-interface-contract.md` 为唯一稳定接口 oracle。

验证至少按受影响范围覆盖：

- method + endpoint path / query compatibility；
- request / response JSON field、nullability 与 enum token；
- pagination、create/update/delete status semantics；
- validation / not-found / upload-size 等受控失败的 `{message}` envelope 与 `400 / 404 / 413`；unknown `5xx` 只验证 consumer 可识别失败，不把 provider-specific body 当作稳定 contract；
- multipart field、binary resource content type 与 attachment disposition；
- Public scoped query 不退化为 Admin/full-data projection；
- Admin / Public frontend adapter 与同一个 canonical contract 对齐。

Interface contract test 可以有 provider-specific adapter，但 contract assertion 本身必须能在 Backend implementation 替换后复用；不能把 Java Controller class、Kotlin DTO 或 TypeScript interface 当成唯一 oracle。

Backend technology substitution dry-run 的 PASS 需要在不读取 Java implementation 作为设计输入的前提下，以 当前权威内容 重建 provider 并证明现有 Admin/Public consumer 所需 contract compatibility。

### 4.3 前端

独立前端应用分别拥有自己的 type-check、build 与 Runtime / Browser 证据，不能用一个应用的成功替代另一个应用。

Vue / TypeScript 变更按真实风险选择验证层：

- template、props、emits、类型契约：至少 Vue-aware type-check；
- reactivity、watch、lifecycle、async side effect：追加能证明状态与时序正确的行为测试；
- Router、DOM、用户交互：追加 浏览器验证；
- build / module / tsconfig：追加正式 build；
- 存在视觉 验收：在功能验证之外取得对应 AI / 人工视觉证据。
- 触达 `docs/design/public-site/DESIGN.md`：使用仓库固定的 Google `DESIGN.md` CLI 版本执行格式 / token lint；
- 触达公开站视觉实现：除 build / Browser 外，核对实现是否消费当前 Design Authority；Known Gap 未经 promotion 不得由测试固化成新的视觉事实。

验证以当前 Consumer 实际 package、tsconfig、Workflow 与 仓库权威 为准，不为匹配外部 Technology Profile 机械升级依赖。

### 4.4 浏览器 / E2E

浏览器验证 用于证明路由、交互、资源加载、异步状态与已编码 验收。

对分页、Top-N、作用域过滤与异步页面：

- 有稳定业务作用域时直接验证该作用域，不以“全局第一页再前端过滤”替代；
- 测试数据应能够穿越真实分页 / window boundary，或直接断言正确查询约束；
- 不以固定 `sleep`、DOM 早期存在或 `page.goto()` 返回作为异步装配完成条件；
- Router / query / watcher 驱动页面应覆盖 stale response 不得覆盖当前路由状态的时序风险。

Functional Browser PASS 不自动等于 视觉保真 PASS。

### 4.5 快速反馈与完整验证

高频开发反馈可以使用比最终完成验证更小的作用域，但必须明确其证据责任，不能把快速反馈的 PASS 改称目标提交已经完成完整验证。

长期原则：

- 快速验证按实际变化范围选择 type-check、build、Runtime smoke、targeted Browser 等必要层级；
- 快速验证可以复用具有明确 provenance 的已验证 immutable artifact，以减少无关重复构建；
- 复用 artifact 时必须记录当前 Target、artifact identity / provenance 与仍未被当前运行重新证明的 claim；
- supporting Runtime 可以为了反馈速度复用构建产物，但 Database / fixture / session 等测试状态必须按当前验证需要保持隔离或可重建；
- 最终 Completion / Ready to Integrate 声明仍必须由当前 Authority 所要求、与目标 exact Head 匹配的完整证据支持；
- GitHub Actions、Local Runtime、self-hosted Runner 等不同运行环境可以采用不同的快速验证实现，不能把某一种运行时优化方式提升为通用 Verification Requirement。

当前 GitHub Actions / GitHub-hosted Runner 下的具体实现由 `docs/technical/ci-verification-runtime.md` 持有；本文不复制 GHCR、Workflow trigger、fingerprint 或 Job 编排细节。

### 4.6 人工评审 / 评审环境

需要人工运行时观察的工作必须使用与目标 claim 对应的可复现 Review Runtime。

如果同一环境先执行自动 E2E、后供人工评审，必须在两者之间显式恢复干净 baseline，避免自动测试数据、缓存、会话或临时资源泄漏到 人工评审。

评审环境 的共享外部资源、并发互斥、临时代理、bind mount ownership 与清理方式属于 Repository / Workflow implementation responsibility；验证只要求它们满足：

- 不让独立工作错误共享或覆盖同一外部资源；
- 清理后可从版本化 Authority 恢复目标 baseline；
- 外部访问、内部 target、Run ownership 与释放状态分别可验证；
- 临时验证授权不扩张为 Production / shared data 的破坏性操作授权。

### 4.7 跨层语义与可再生性复核

当 claim 涉及 Documentation Authority completeness、Fresh Context reconstruction、technology substitution 或 code-holdout regeneration 时，不能只检查文档存在、字段名称一致或单层测试通过；必须对当前受影响语义做双向 traceability challenge：

- Product / Domain Requirement 中具有用户可观察后果的长期事实，应能进入唯一 Current Specification 的 Observable / Failure / 验收 contract；
- Specification 中依赖 Backend / external boundary 的行为，应能在对应 Interface / Technical owner 中恢复必要的 transport semantics，不只恢复 endpoint / field shape，还要覆盖 query scope、lifecycle transition、failure 与 resource projection 等会改变 consumer 行为的含义；
- Architecture 的长期 seam 应有相应 Technical consumer / provider contract 与 Verification oracle，不由某个当前 implementation file 代替；
- Verification 不应维护第二份 Requirement / Specification / Interface truth，也不应因 implementation 当前“碰巧这样做”就自动把未授权行为晋升为产品事实。

Repository implementation 可以在 Review 中作为 **证据** 暴露 当前权威内容 的遗漏、冲突或 stale contract：先建立 Expected vs Actual，再判断应修复 Authority、verification 还是 implementation。若 implementation 只是证明某个已经存在的上层 Requirement 在下游缺少可恢复 projection，可以把该已授权语义 promotion 到正确 owner；不得从 implementation convenience 反向发明新 Requirement。

进入真正 code-holdout / technology-substitution design 后，replacement Agent 必须以 当前权威内容 为设计输入，不再通过读取被替换 implementation 来补齐缺失 contract；旧 implementation 只在结果比较阶段按实验协议作为对照 证据 使用。

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

如果同一产品语义在多个测试层重复硬编码，应回到真正的 Requirement / Specification / Architecture / Interface owner，并减少第二套契约。

## 6. 证据契约

对目标 PR / commit 的 Completion claim 应能关联：

- target Head / commit；
- Event / Run / Job / Step；
- conclusion；
- claim 所需的 logs / artifact / runtime observation。

历史 Run 不能自动成为当前 Head 的 Run。

如果 Artifact 本身是必要 证据，上传步骤成功不能单独证明 Artifact 实体存在；需要重新读取目标 Run 的 Artifact 并核对其归属。

祖先提交的高成本 证据 只有在完成精确 compare、逐项证明当前差异不会影响对应 claim，并对当前 Head 执行必要 targeted verification 后，才能按 claim 复用。复用时必须保留 证据 Commit、Current Target、compare range 与仍需重新验证的 claim；不得把祖先 Run 改称当前 Run。

## 7. Verification 与 Authority 的关系

验证证明实现是否满足当前 Authority，不反向创造 Product Requirement、Architecture 或 Interface Contract。

当测试、Workflow assertion 或 fixture 与当前 Requirement / Specification / Architecture / Interface Contract 冲突时，先判断是否为 stale verification contract；只有 Authority 本身存在真实歧义 / 缺口时才回到对应 owner。

验证策略不维护第二份：

- active migration inventory；
- HTTP Controller / endpoint / DTO inventory；
- Site Package 文件清单；
- Runtime resource count；
- Workflow / job 名称清单；
- 固定 评审环境 endpoint inventory；
- 当前 Execution Unit checklist。

这些事实应从其真实 Repository / Technical / Work / GitHub owner 恢复。
