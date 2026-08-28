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

独立执行 npm ci、`vue-tsc`、Vite build。

重点验证多入口 HTML 构建、公开 URL、后台页面和公开页面共享布局不因当前变更破坏。

### 2.3 Completion E2E

Backend 和 Frontend 都 PASS 后执行 Playwright。

E2E 必须消费真实 Flyway 初始化结果和版本化静态资源基线，不允许测试代码重新创建站点基础栏目、主菜单、页面组和固定页面。测试代码只补充当前验证需要的动态测试数据。

Functional Browser Verification 用于证明路由、交互、资源加载和已编码断言；它不能在缺少完整机器可判定容差时单独证明 Visual Fidelity。

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

## 4. Current Evidence 与 Artifact

对目标 PR / commit 的验证必须关联 Event、Head SHA、Run、Job、Step、Conclusion、必要 Logs 与 Artifact。历史 Run 不能替代当前 Head 的 Completion Evidence。

如果某个 Artifact 本身构成必要 Verification Evidence：

- upload step `success` 不能单独证明 Artifact 实体存在；
- 必须重新读取当前 Run 的 Artifact 集合；
- 核对 Artifact 名称、Run 和 Head SHA；
- 缺少该 Artifact 应使 Workflow 失败时，使用 `if-no-files-found: error` 或等价机制。

## 5. Review Environment

Review Environment 采用 MySQL service、Backend runtime、Frontend artifact + Nginx、Playwright official runtime 和 FRP 临时外部 HTTP 地址。

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
- Review Environment 对同一 PR/ref 使用 concurrency / cancellation 避免固定 FRP 域名被旧 Head 占用。

### 5.2 Bind Mount Ownership 与可重复恢复

容器可写 host bind mount 时，显式处理：

- 写入 Runtime 的 UID / GID；
- host 文件 ownership / permissions；
- 清理动作使用的已授权身份；
- Reset 后重新复制或重建版本化基线；
- 相同 Reset 是否可再次执行并得到同一状态。

调用 `rm -rf` 本身不构成 Cleanup Evidence；应重新检查目标路径、预期基线内容和后续 Runtime 结果。

这些清理规则只适用于已授权的临时验证 / 评审环境，不扩展为 Production 或共享数据的破坏性清理授权。

## 6. Visual Fidelity 验证

对于当前“现网视觉与布局复刻 + 必要技术适配”的要求，采用三层证据：

```text
Reference Evidence
→ AI Visual Comparison
→ Human Visual Review
```

### 6.1 Reference Evidence

从当前原网站运行时直接取得必要证据，例如：

- 完整页面截图；
- DOM / computed style；
- 真实静态资源 URL；
- 页面宽度、颜色、间距、图片比例和响应式表现。

不得凭聊天记忆、旧截图印象或实现便利性自行推断视觉事实。

### 6.2 AI Visual Comparison

在 Human Review 前，优先使用 Review Runtime 完整截图与参考截图对照，消除明显的：

- 大面积颜色偏差；
- Header / Nav / Footer 结构偏差；
- 主要区块比例和层级偏差；
- 图片资源错误或缺失；
- 明显裁切、溢出和响应式问题。

AI Visual Comparison 是人工评审前的收敛手段，不替代 Human Visual Review。

### 6.3 Human Visual Review

人工 Review 承担最终视觉判断，包括：

- 现网视觉复刻精度；
- 具体间距、字号、图片比例；
- 栏目列表、文章详情、固定页面、页面组 / Tab 等页面级体验；
- 移动端可读性和必要技术适配；
- 其他难以通过稳定机器阈值表达的视觉差异。

人工结论必须按原始范围记录。例如“基本通过，暂未发现新的阻塞问题”不能扩大为“完全一致”或无条件验收。

低风险视觉 / 交互问题可在人工 Review 后增量修订；数据模型、Scope 和重大用户行为改变仍按 Product Intent 处理。

## 7. 当前页面细节收敛验证路径

下一轮栏目、文章、固定页与页面组细节收敛至少遵循：

1. 从原站选择代表页面并取得运行时参考证据；
2. 在当前 Consumer Runtime 取得对应页面截图和行为证据；
3. 修复 Authority 已明确的视觉 / 交互偏差；
4. 执行 Backend / Frontend targeted verification；
5. 执行 Completion Browser E2E；
6. 对需要视觉判断的页面执行 AI Visual Comparison；
7. 自动验证结束后恢复 Clean Human Review Baseline；
8. 验证外部 Review URL；
9. 进入 Human Visual Review；
10. 只有当前 Head 的功能证据、视觉证据和实际 Human Review 结论共同支持时，才声明相应视觉收敛完成。

## 8. 异步 Actions 观察

Actions 中 queued / pending / in_progress 均为中间状态。

当结果仍可通过当前授权路径观察时，应进行有界持续观察，直到：

- 成功并取得所需证据；
- 失败并完成诊断 / 授权内修复 / 重跑；
- 出现真实权限、业务、架构或 Runtime 阻塞；
- 达到有界观察退出条件并明确记录 `Executed but not fully verified`。

Dispatch / rerun API 返回成功不等于验证完成。
