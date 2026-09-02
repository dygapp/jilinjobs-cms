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

每个独立前端工程分别执行 npm 依赖安装、`vue-tsc` 与 Vite build。

前端工程物理拆分后，公开站点与管理端分别形成独立 build artifact 和验证入口；不得用其中一个应用的成功构建替代另一个应用的验证。

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

从当前原网站运行时直接取得必要证据，例如：

- 完整页面截图；
- DOM / computed style；
- 真实静态资源 URL；
- 页面宽度、颜色、间距、图片比例和响应式表现。

不得凭聊天记忆、旧截图印象或实现便利性自行推断视觉事实。

### 7.2 AI Visual Comparison

在 Human Review 前，优先使用 Review Runtime 完整截图与参考截图对照，消除明显的：

- 大面积颜色偏差；
- Header / Nav / Footer 结构偏差；
- 主要区块比例和层级偏差；
- 图片资源错误或缺失；
- 明显裁切、溢出和响应式问题。

AI Visual Comparison 是人工评审前的收敛手段，不替代 Human Visual Review。

### 7.3 Human Visual Review

人工 Review 承担最终视觉判断，包括现网视觉复刻精度、具体间距和字号、图片比例、页面级体验、移动端可读性以及其他难以通过稳定机器阈值表达的差异。

人工结论必须按原始范围记录。例如“基本通过，暂未发现新的阻塞问题”不能扩大为“完全一致”或无条件验收。

低风险视觉 / 交互问题可在人工 Review 后增量修订；数据模型、Scope 和重大用户行为改变仍按 Product Intent 处理。

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

管理端工程分离与功能收敛阶段至少遵循：

1. Backend Verify；
2. Public Site Frontend Verify；
3. Admin Frontend Verify；
4. 集成 Runtime 启动，验证 `/`、公开 canonical URL、`/admin/` 和 `/api/**` 路由；
5. Browser E2E 验证管理端自身核心流程；
6. Browser E2E 验证后台变更到公开站展示的跨边界闭环；
7. 收集当前 Head 的测试报告、trace / screenshot 等必要 Current Evidence；
8. 自动验证后恢复数据库和版本化静态资源基线；
9. 注入明确的 Human Admin Review Fixture，并验证自动测试残留已清除；
10. 验证外部 Review URL 的公开站与 `/admin/` 均可访问；
11. 进入 Human Admin Review；
12. Human Review Finding 按第 6 节分类路由，不因“管理端评审”名称自动决定处理方式。

### 9.1 V4.6 数据契约专项验证

本轮图片数据策略与网站属性元数据必须至少形成以下机器证据：

- Backend 单元测试：SiteProperty metadata groups 能按 order 输出；未知 group 拒绝；INTEGER 校验；`HOME_CAROUSEL_INTERVAL_SECONDS` 拒绝 0/负数；
- Backend 单元测试：Column `NONE` 拒绝站内文章封面；`REQUIRED` 允许无封面 DRAFT 但阻止 publish；PUBLISHED Article update 不得删除 REQUIRED 封面；Public Summary 能返回封面引用；
- Browser/API：HOME_CAROUSEL 定义为 REQUIRED，当前 SITE_LINKS 基线为 NONE；直接 API 对 REQUIRED 缺图与 NONE 带图均拒绝，证明约束不只存在于 UI；
- Admin Browser：栏目 REQUIRED 策略、文章 REQUIRED 提示、列表 NONE/REQUIRED UI、SiteProperty 左侧 PRESENTATION 元数据分组和受控 group Select 可观察；
- Admin Browser：INTEGER 非整数输入在 UI 被阻止；最终正整数约束仍由 Backend 证明；
- Cross-boundary Browser：临时把 `HOME_CAROUSEL_INTERVAL_SECONDS` 改为 1 秒并为 HOME_CAROUSEL 增加第二张有效图片，打开真实公开首页后验证 `data-carousel-item-id` 在时限内实际变化；finally 删除测试项并恢复原配置。

这些策略只验证“数据是否允许/要求图片”和轮播行为参数，不建立“图片策略决定页面布局”的断言；不得把数据契约误写成 `displayMode`。

### 9.2 管理端最终视觉交互专项验证

合并前最终视觉收敛至少验证：

- 主侧边栏默认展开，点击后进入紧凑状态并保留全部八类路由入口，再次点击可恢复；
- 文章栏目导航与网站属性分组面板至少各验证一次收起/展开；收起后右侧内容仍可操作且恢复按钮可见；
- 表格图标操作必须保留 `aria-label`，Browser role/name 定位继续通过；至少对一个操作执行 hover 并验证 Tooltip 文本；
- 网站属性 Table 不再包含常驻可编辑输入/图片上传器；点击“编辑值”后才出现类型化 Dialog；
- `HOME_CAROUSEL_INTERVAL_SECONDS` 在值编辑 Dialog 中仍拒绝非整数；动态 JSON 属性在值编辑 Dialog 中仍拒绝非法 JSON；
- SiteProperty 定义 Dialog 与值编辑职责分离，新建定义仍允许初始值；
- 不增加“后台显示风格”系统属性、用户配置 API、Profile 数据或其他与当前无账号基线冲突的持久化能力。

### 9.3 图片预览与配置治理专项验证

统一图片预览与受保护资源配置整改至少验证：

- `cms.static.protected-resources` 的默认值能够保护工程基线资源，并允许通过 `CMS_STATIC_PROTECTED_RESOURCES` 进行部署覆盖；固定工程保护路径不得再次散落写入 `StaticResourceService`；
- 当前启用的网站属性 `RESOURCE_PATH`、列表图片、宣传展示图片和导航图标仍由 Backend Runtime 动态加入受保护集合，不改为管理员人工维护的 `protected=true` 数据字段；
- Admin 静态资源列表使用“受保护”语义，受保护资源普通删除入口禁用且 Backend 最终拒绝删除，明确替换行为仍可使用；
- 网站属性、列表、宣传展示、导航和静态资源等需要辨识图片内容的位置复用统一自适应图片组件，不再各自维护白底 `<img>` 预览；
- 自适应图片组件负责浅色、深色、透明图片的可辨识背景，放大、缩放、旋转和 Viewer 生命周期直接复用 Element Plus `el-image`，不得另建重复的大图预览器；
- `ImageResourcePicker` 当前值预览默认启用自适应背景；图片库中的选择卡片保持“点击选择”语义，不因 Viewer 抢占选择操作；
- 对发现的固定路径、时间间隔、外部地址、稳定 Code/Alias、分页值和安全白名单按 `configuration-governance.md` 先分类责任，不以存在字面常量作为必须配置化的判据。

## 10. AI Implementation Review

最终 Completion Evidence 前，必须对本轮实现差异执行 Authority-oriented AI Review，至少检查：

- Requirement V4.6、CMS Core / Admin / Public Specifications 与 Technical Plan 是否一致；
- 未新增“系统设置”模块、认证/角色/权限或其他越界能力；
- `ContentImagePolicy` 没有演变成展示模式；
- Backend 是图片必填/禁用、属性分组、整数值等最终约束层；
- 已发布 Article 不存在通过普通编辑绕开 REQUIRED 封面的状态漏洞；
- Public Summary 真正补齐 cover resource，而不只是 DTO 字段存在；
- HOME_CAROUSEL interval 的配置、读取、实际 timer 行为和 cleanup fixture 一致；
- 管理端收起状态仅为前端界面状态，没有引入新的系统/用户配置模型；
- 图标化操作保留可访问名称，网站属性值编辑从列表内联迁移到 Dialog 后没有削弱原有类型校验和 Backend 约束；
- 固定工程受保护资源由 Spring 外部化配置拥有，CMS 当前引用资源继续由 Backend 动态计算，不能把两类保护来源混成管理员维护字段；
- 图片缩略图统一复用自适应公共组件，Element Plus `el-image` 负责通用 Viewer 能力，没有重复实现预览 Dialog / Viewer；
- 新发现的硬编码候选已按 `configuration-governance.md` 判断其责任层；稳定领域、安全和页面模板契约不得为了消除常量而错误配置化；
- CI / Review / FRP 环境参数不得进入 CMS 网站属性；只有在确有环境差异需求时才迁移为 Repository / Environment / Deployment Variables；
- V11/V12 等既有 migration 没有因纯管理端视觉或配置治理调整被回改；
- 无无关文件、临时文件、调试入口或测试残留进入 PR。

发现 Implementation Defect 时直接在当前授权范围修复，并重新取得新 Head Evidence；不得用“AI Review 已完成”代替修复后的测试。

## 11. 异步 Actions 观察

Actions 中 queued / pending / in_progress 均为中间状态。

当结果仍可通过当前授权路径观察时，应进行有界持续观察，直到：

- 成功并取得所需证据；
- 失败并完成诊断 / 授权内修复 / 重跑；
- 出现真实权限、业务、架构或 Runtime 阻塞；
- 达到有界观察退出条件并明确记录 `Executed but not fully verified`。

Dispatch / rerun API 返回成功不等于验证完成。