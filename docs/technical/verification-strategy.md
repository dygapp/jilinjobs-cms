# 验证运行策略（Verification Runtime Strategy）

## 1. 目的

本文定义 `jilinjobs-cms` 当前验证运行规则。

验证必须区分 Implementation Exists、Automated Completion Evidence、AI Visual Observation 与 Human Runtime Observation。没有当前提交对应、且与声明类型匹配的成功证据，不声明完成。

## 2. 分层验证

### 2.1 Backend Verify

独立执行 Java 21、Gradle、Kotlin/Spring Boot 编译、后端自动化测试和 bootJar。

涉及数据库 Migration 的最终验证，在 Runtime 条件允许时至少覆盖一次：

```text
Fresh Database
→ Full Migration Chain
→ Application Startup
```

已有数据库上的增量验证、SQL 文件检查、编译或单元测试不能单独证明新环境初始化可用。

### 2.2 Frontend Verify

每个独立前端工程分别执行 npm 依赖安装、Vue-aware type-check 与 Vite build。

当前 `frontend/public-site` 与 `frontend/admin` 的 `npm run build` 均由 Consumer package script 串联 `vue-tsc --noEmit && vite build`。因此成功的项目 build script 同时提供 Vue SFC type-check 与 bundler build evidence；证据记录仍应区分这两个子层，不能把“Vite transpile / bundle 成功”单独描述为类型验证成功。

前端工程物理拆分后，公开站点与管理端分别形成独立 build artifact 和验证入口；不得用其中一个应用的成功构建替代另一个应用的验证。

不得为了匹配外部 Technology Profile 的 Research Anchor 机械升级 Vue、TypeScript、`vue-tsc`、Vite 或其他依赖；验证以当前 Consumer 实际 package、tsconfig、Architecture 和命令为准。

#### 2.2.1 Vue 3 + TypeScript Verification Profile 映射

前端变更按实际风险选择验证层，不机械运行所有层：

| 变更类型 | 最低当前证据 | 需要追加的证据 |
|---|---|---|
| SFC template、props、emits | Vue-aware type-check | 有运行时行为变化时追加行为测试 |
| reactivity、computed、composable | Vue-aware type-check + 能证明依赖变化后状态正确的行为测试 | 跨页面/跨组件时按实际边界追加 Browser |
| `watch` / lifecycle / async side effect | Vue-aware type-check + 触发/时序/旧工作失效等行为测试 | 涉及 Router / DOM / 用户可见状态时追加 Browser E2E |
| DOM、Router、template ref、用户交互 | Vue-aware type-check + Browser E2E | 有视觉 Acceptance 时追加 AI/Human Visual Evidence |
| build / module / tsconfig | Vue-aware type-check + Vite build | 影响 Entry / Runtime 时追加 Browser / Integration |
| 仅 TypeScript 类型变化 | Vue-aware type-check | 是否追加行为测试取决于 Acceptance 风险 |

Profile 只提供默认风险映射；本仓库的 Requirement、Specification、Architecture、实际命令和现有 Workflow 可以增加、收窄或替代 Engineering Default，但不能违反 Vue / TypeScript 客观语义。

### 2.3 Completion E2E

Backend 和所有当前相关 Frontend Verify 都 PASS 后执行 Playwright / Browser Verification。

E2E 必须消费真实 Flyway 初始化结果和版本化静态资源基线，不允许测试代码重新创建站点基础栏目、主菜单、页面组和固定页面。测试代码只补充当前验证需要的动态测试数据。

Functional Browser Verification 用于证明路由、交互、资源加载和已编码断言；它不能在缺少完整机器可判定容差时单独证明 Visual Fidelity。

### 2.4 分页作用域与异步 UI 完成条件

当一个页面区域在当前 Authority / Specification 中明确绑定到栏目、分类、租户、所有者、状态或其他稳定业务作用域，而 Backend API 已提供对应过滤能力时，默认直接在该作用域内查询和验证。不得用“先读取全局前 N 条 / 第一页，再在前端过滤”代替作用域查询，除非当前数据契约能够证明该窗口完整覆盖目标集合。

验证规则：

- 对分页、Top-N、窗口截断相关行为，测试数据应能跨越实际分页/窗口边界，或直接断言请求携带正确作用域参数；少量样例数据 PASS 不能单独证明数据量增长后的正确性。
- 如果页面需要从某个作用域中继续筛选子类型，例如只消费某栏目中的 `EXTERNAL_LINK`，应在该作用域内继续分页直到取得所需数量或耗尽数据，不回退到无作用域的固定全局窗口。
- Browser E2E 对异步页面不以 `page.goto()` 完成、DOM 节点早期存在或固定 `sleep` 作为数据装配完成条件；应等待可观察的语义完成信号，例如 loading 状态结束、成功内容容器出现、预期响应完成或等价稳定状态。
- 对 Router / query / watcher 驱动的异步页面，测试应覆盖“较早但较慢的请求在路由已变化后才完成”的场景；旧请求不得覆盖当前路由对应的成功、错误、loading、metadata 或导航副作用。优先通过受控响应顺序和可观察 response / DOM 状态验证，不用固定 sleep 代替完成条件。
- 如果实现从单次请求演进为多个并行/分阶段请求后旧测试出现时序失败，先按第 3 节分类。不能为了迎合测试中的偶然时序恢复错误的数据访问方式；应修正陈旧完成条件，或在实现层补齐本就合理的 loading / empty / error 状态契约。

## 3. 验证失败分类

遇到 Test、Workflow assertion、fixture、snapshot 或 Runtime 验证失败时，先读取当前 Repository Authority / Specification，建立 Expected vs Actual，再至少区分：

```text
Implementation Defect
Stale Verification Contract
Runtime / Environment Problem
External Dependency Problem
```

如果验证 Artifact 与当前更高优先级 Authority / Specification 冲突：

- 将其识别为 Stale Verification Contract；
- 修正拥有过期断言的验证层，不修改产品实现去恢复已被取代的旧行为；
- 如果同一产品语义在多个验证层重复硬编码，识别真正的契约所有者，删除无必要重复或共享同一权威来源；
- 修正后重新运行当前有效验证，确认没有掩盖真实实现缺陷。

Workflow 优先验证构建、运行环境、服务健康、HTTP/API 可达与正式测试套件，不把大量具体产品展示语义重复维护为第二套硬编码契约。

## 4. Current Evidence、Artifact 与后继提交

对目标 PR / commit 的验证必须关联 Event、Head SHA、Run、Job、Step、Conclusion、必要 Logs 与 Artifact。历史 Run 不能自动替代当前 Head 的 Completion Evidence。

如果某个 Artifact 本身构成必要 Verification Evidence：

- upload step `success` 不能单独证明 Artifact 实体存在；
- 必须重新读取当前 Run 的 Artifact 集合；
- 核对 Artifact 名称、Run 和 Head SHA；
- 缺少该 Artifact 应使 Workflow 失败时，使用 `if-no-files-found: error` 或等价机制。

### 4.1 Descendant Commit Evidence Reuse

如果高成本 Runtime 或 Human Review Evidence 来自当前目标提交的祖先提交，不因后续提交看起来是 `docs-only`、文件少或 CI 绿色就自动继承。

只有同时满足以下条件，才可以按具体 Evidence Claim 复用未受影响的祖先证据：

1. 确认 Evidence Commit 是当前目标提交祖先，并取得两者之间完整、精确的 compare diff；
2. 逐项说明差异为什么不会改变该 Claim 所覆盖的行为、环境、数据、资源或人工判断对象；
3. 与该 Claim 相关的 Repository Authority、Requirement、Specification、Architecture、Acceptance、Workflow、Runtime Configuration、Migration、Fixture 和版本化资源没有影响性变化；
4. 当前 Head 已完成其自身需要的 targeted verification；
5. 记录 Evidence Commit SHA、Current Target SHA、compare range、原 Run / Review 引用、可复用 Claim 与仍需重新验证的 Claim。

祖先 Run 可以继续作为未受影响行为的祖先证据，但不能被描述为当前 Head 的 Run。受影响或无法证明不受影响的 Claim 必须重新取得相应验证或 Review。

## 5. Review Environment

Review Environment 使用 MySQL service、Backend runtime、版本化 Frontend artifact + Nginx、Playwright official runtime 和 FRP 临时外部 HTTP 地址。

站点真实结构来自 Flyway + 版本化初始化静态资源包。自动测试数据不承担网站初始化职责。

### 5.1 Automated Verification 与 Human Review Baseline 隔离

如果同一 Workflow 先执行自动 E2E、后暴露给人工评审，采用：

```text
Automated Verification
→ Collect Current Evidence
→ Recreate Database / Restore Versioned Static Baseline
→ Seed Explicit Human Review Fixtures
→ Start / Expose Review Runtime
→ Verify Review Baseline and External Access
```

要求：

- 自动测试创建的菜单、栏目、文章、文件、缓存和会话不能因共用 Runtime 而默认进入 Human Review；
- Human Review Fixture 必须显式准备，只保留确实用于人工观察的数据；
- Reset 后重新验证测试数据已移除、版本化资源已恢复、人工 Fixture 已准备、服务健康且评审地址可访问；
- Review Environment 的固定 FRP proxy / custom domain 是仓库级共享外部资源，所有会使用该资源的 PR 与手工触发路径必须进入同一 concurrency group；不同工作争用时默认排队，不因 ref 不同建立并行实例，也不把 `cancel-in-progress` 当作默认互斥策略。只有新 Run 确实取代旧工作且取消后的 FRP 释放闭环可靠时才允许取消；Run cancellation 与代理实际释放 / 新 Run 归属必须分别验证。

### 5.2 Bind Mount Ownership 与可重复恢复

容器可写 host bind mount 时，显式处理：

- 写入 Runtime 的 UID / GID；
- host 文件 ownership / permissions；
- 清理动作使用的已授权身份；
- Reset 后重新复制或重建版本化基线；
- 相同 Reset 是否可再次执行并得到同一状态。

调用 `rm -rf` 本身不构成 Cleanup Evidence；应重新检查目标路径、预期基线内容和后续 Runtime 结果。

这些清理规则只适用于已授权的临时验证 / 评审环境，不扩展为 Production 或共享数据的破坏性清理授权。

### 5.3 固定 FRP 共享资源并发边界

当前 Review Environment 的 `review.cc-lotus.info` 与 `jilinjobs-review` proxy 属于同一仓库级排他资源。并发治理遵循：

```text
Identify Shared FRP Resource
→ Repository-wide Exclusivity
→ Queue Independent Runs
→ Verify Release / Ownership
→ Verify External Target
```

- PR 与 `workflow_dispatch` 等所有触发路径使用同一 concurrency group；
- 独立 Human Review / PR Review 不互相 supersede，默认有界排队；当前 Workflow 使用 `cancel-in-progress: false`，后触发的独立工作等待共享评审资源，而不是取消正在运行的评审；
- 若未来确实引入 superseding cancellation，必须先证明取消后的 frpc / proxy 释放路径可靠，并重新核对代理 owner、目标 Head 与外部地址；
- `frpc` 进程退出、Workflow cancelled 或重试成功都不能单独证明外部 proxy 已释放，仍需通过外部可达性和目标 Head / 环境证据完成验证。

## 6. Human Review Finding 分类

Human Review Finding 不由评审名称决定类别。视觉评审、管理端评审或其他人工观察都可能暴露：

```text
Implementation Defect
Product / Requirement Ambiguity
Domain / Architecture Authority Gap
Runtime Problem
Low-risk Visual / Interaction Adjustment
```

处理规则：

- 保留人工观察的原始范围和上下文；
- 重新读取当前 Repository Authority、Requirement、Specification 与 Product Intent 后分类；
- 人工观察是重要 Evidence，但不会仅因来自 Human Review 就自动成为新 Requirement；
- 不得因为 Finding 来自 Visual Review 就静默压缩成视觉调整；
- Product / Requirement / Architecture 级歧义回到相应澄清或规划阶段；明确的实现缺陷按当前 Scope 修复并重新验证；Runtime Problem 进入系统化诊断；
- 人工结论只按实际范围声明，不扩大为无条件验收。

## 7. Visual Fidelity 验证

对于“现网视觉与布局复刻 + 必要技术适配”的要求，采用三层证据：

```text
Reference Evidence
→ AI Visual Comparison
→ Human Visual Review
```

### 7.1 Reference Evidence

从当前原网站运行时直接取得必要证据，例如完整页面截图、DOM / computed style、真实静态资源 URL、页面宽度、颜色、间距、图片比例和响应式表现。不得凭聊天记忆、旧截图印象或实现便利性自行推断视觉事实。

### 7.2 AI Visual Comparison

在 Human Review 前，优先使用 Review Runtime 完整截图与参考截图对照，消除明显的大面积颜色偏差、Header / Nav / Footer 结构偏差、主要区块比例和层级偏差、图片资源错误或缺失，以及裁切、溢出和响应式问题。AI Visual Comparison 不替代 Human Visual Review。

### 7.3 Human Visual Review

人工 Review 承担最终视觉判断，包括现网视觉复刻精度、具体间距和字号、图片比例、页面级体验、移动端可读性及其他难以稳定机器化表达的差异。人工结论按原始范围记录，不扩大为“完全一致”或无条件验收。

## 8. 外部媒体与二进制输入验证

如果外部站点、接口、附件或其他 Repository 提供的二进制/媒体资源将进入版本化站点基线、后台静态资源或目标 Runtime，应按当前风险核对真实内容：

```text
Acquire
→ Verify Content Signature / Media Type
→ Decode or Parse when relevant
→ Normalize when needed
→ Version / Persist
→ Verify in Target Runtime
```

文件名、扩展名、URL 后缀和响应头不能单独证明内容类型。图片等资源在相关时应验证实际解码，并核对影响当前声明的尺寸/透明度等属性。格式不匹配时更正命名、转换或拒绝输入，禁止只改扩展名。规范化后重新验证生成物。

## 9. 当前管理端收敛验证路径

管理端工程分离与功能收敛阶段至少遵循：Backend Verify、Public Site Frontend Verify、Admin Frontend Verify、集成 Runtime、Browser E2E 管理端核心流程与跨边界闭环、Current Evidence 收集、自动验证后的 baseline reset、Human Admin Review Fixture、外部 Review URL 验证和 Human Admin Review Finding 分类。

### 9.1 V4.6 数据契约专项验证

既有 V4.6 图片数据策略与网站属性元数据验证要求继续有效，包括 Backend 约束、HOME_CAROUSEL / SITE_LINKS 图片策略、Admin Browser 元数据与整数校验，以及真实公开首页轮播行为的 cross-boundary Browser Evidence。

### 9.2 管理端最终视觉交互专项验证

既有管理端视觉交互专项要求继续有效，包括侧边栏/上下文面板收起展开、图标操作可访问名称与 Tooltip、网站属性值编辑 Dialog、INTEGER/JSON 校验、定义和值编辑职责分离，以及不得引入与当前无账号基线冲突的持久化 UI 偏好。

### 9.3 图片预览与配置治理专项验证

既有图片预览与配置治理专项要求继续有效：工程基线保护路径使用 Spring 外部化配置，Runtime 当前引用由 Backend 动态保护；Admin 使用“受保护”语义；图片辨识位置复用统一自适应图片组件；Viewer 复用 Element Plus `el-image`；配置候选先按 `configuration-governance.md` 分类责任，不因存在字面常量机械配置化。

## 10. AI Implementation Review

最终 Completion Evidence 前，对本轮实现差异执行 Authority-oriented AI Review。除当前 Unit 特定 Acceptance 外，继续检查：

- 未新增越界能力；
- Backend / Frontend 职责没有因实现便利被反转；
- 既有 Migration 不因无关调整被回改；
- 当前硬编码/配置候选已按 `configuration-governance.md` 判断责任层；
- 已有框架、标准库、依赖和公共组件满足契约时优先复用，未为复用扩大依赖面；
- Implementation Minimality 没有引入无当前证据支持的抽象、配置、依赖、扩展点或未来设计；
- Final Diff Scope 中每个有意义区域能追溯到当前 Unit、验证、Authority 同步、必要 preparatory refactor 或其直接 cleanup；
- 无无关文件、临时文件、调试入口或测试残留进入 PR。

发现 Implementation Defect 时在授权范围内修复，并重新取得新 Head Evidence；不得用“AI Review 已完成”替代修复后的测试。

## 11. 异步 Actions 观察

Actions 中 queued / pending / in_progress 均为中间状态。当结果仍可通过当前授权路径观察时，应有界持续观察，直到成功并取得证据；失败并完成诊断/授权内修复/重跑；出现真实权限、业务、架构或 Runtime 阻塞；或达到有界观察退出条件并明确记录 `Executed but not fully verified`。Dispatch / rerun API 返回成功不等于验证完成。