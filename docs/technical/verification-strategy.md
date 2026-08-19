# 验证运行策略（Verification Runtime Strategy）

## 1. 目的与适用范围

本文补充 `docs/technical/center-main-site-core.md` 中的验证责任，固化当前 Consumer 项目在真实 GitHub Runtime 中已经通过 EU-01 暴露并确认的跨执行单元验证 HOW。

本文不改变业务 Specification、Execution Unit 的业务完成条件或产品范围；它只规定如何以更可观察、可复现、成本可控的方式取得当前证据（Current Evidence）。

## 2. 证据口径

验证证据分为两类，职责不同。

### 2.1 自动化完成证据（Automated Completion Evidence）

Execution Unit 声明 Completed 前，必须取得与该 Unit 完成条件匹配、在当前提交上实际执行成功的自动化验证证据。

对于包含前后端协作与浏览器行为的 Unit，最终完成证据至少应覆盖：

- 后端编译/测试通过；
- 前端类型检查/构建通过；
- Unit 所要求的关键浏览器级纵向闭环通过。

Implementation Exists、历史成功结果、计划中的测试或仅有人工观察均不能替代当前自动化 PASS。

### 2.2 人工运行时观察（Human Runtime Observation）

Human 可以提供以下可核对信息作为 Runtime / Tooling 事实证据：

- GitHub Actions Run URL；
- Actions UI 截图；
- Step 状态、可见日志与持续时间；
- 明确的人工取消、重试或异常环境观察。

Human Runtime Observation 可以用于：

- 证明验证基础设施发生异常阻塞或明显超出合理耗时；
- 在 Agent 受 Connector 可观察性限制时补足事实；
- 支持将当前 Run 记录为 Interrupted / Aborted；
- 支持停止无价值等待并切换验证实现；
- 为 Tool / Runtime / Operating Guide 改进提供实验数据。

Human Runtime Observation **不能直接证明 Execution Unit Completed**。切换验证实现后，仍需新的自动化 PASS Current Evidence。

## 3. 分层验证

验证分成 Fast Feedback 与 Completion Evidence 两层，避免每次局部修复都支付最高成本环境准备。

### 3.1 Backend Verify

后端 Job 独立执行：

- Java 21；
- Gradle；
- Kotlin / Spring Boot 编译；
- 后端自动化测试；
- 生成可运行 backend artifact。

后端失败时不启动浏览器 E2E 环境。

### 3.2 Frontend Verify

前端 Job 独立执行：

- Node；
- 依赖安装；
- `vue-tsc`；
- Vite build；
- 生成 frontend artifact。

前端失败时不启动浏览器 E2E 环境。

### 3.3 Completion E2E

只有 Backend Verify 与 Frontend Verify 均 PASS 后才启动 E2E。

E2E 优先消费已经构建完成的 artifacts，不重复承担应用编译职责。

## 4. E2E 运行拓扑

当前项目优先使用预定义 Runtime Containers，而不是在每个 GitHub-hosted runner 中临时安装完整环境：

```text
MySQL service container
        │
        ├─ Java Runtime container + backend artifact
        │
        ├─ Nginx/static runtime container + frontend artifact
        │
        └─ Playwright official container + browser tests
```

长期约束：

- MySQL 使用官方镜像；
- 后端 E2E Runtime 只需要 Java Runtime，不在 E2E 阶段重新安装/运行 Gradle 构建；
- 前端使用已构建静态 artifact，由轻量 HTTP Runtime 提供页面和 `/api` 反向代理；
- Playwright 使用官方预构建镜像，避免每次执行 `playwright install --with-deps`；
- Playwright package 版本必须与官方 Docker image 版本匹配；
- 具体镜像 tag、Node/Gradle/Playwright 精确版本仍属于可逆执行细节，以 workflow 中的当前固定值为准。

如果未来已有官方镜像不能满足固定依赖且准备成本重新显著上升，可在证据支持下建立 Consumer 自有的薄封装 CI 镜像；不预先维护“包含所有工具”的万能镜像。

## 5. GitHub 执行路径与可观察性

当前 ChatGPT GitHub Connector 已观察到：按 commit 自动枚举 `push` 触发的 workflow run 能力不足，而 PR 相关能力更完整。

因此当前 Consumer Runtime 优先采用：

```text
Execution Unit / Task Branch
→ Pull Request
→ pull_request-triggered CI
→ Current Evidence
→ Integration
```

这是一项当前 Runtime 适配策略，不自动上升为所有 Consumer / Runtime 都必须使用 PR 的通用 Method 结论。

相关证据：

- Consumer Issue #1：push-triggered Actions 可观察性；
- Consumer Issue #2：E2E 环境准备异常耗时与分层验证；
- PR #3：EU-01 容器化验证重构。

## 6. 超时与过期运行

CI 不依赖 GitHub Actions 的 6 小时默认 Job 上限作为正常失败边界。

要求：

- 每个主要 Job 配置与正常基线相称的显式 `timeout-minutes`；
- 高成本环境准备、Runtime 拉取和 E2E Step 配置更短的局部超时；
- 同一 PR/ref 的新提交使用 `concurrency.cancel-in-progress` 取消已过期运行；
- 环境准备明显异常时，Human Runtime Observation 可以支持提前中止并切换验证实现；
- 被中止/取消的 Run 记录原因，但不能作为 Completion PASS。

## 7. EU-01 恢复验证决策

EU-01 原 CI #5 在后端测试与前端构建已经 PASS 后，`Install Chromium for Playwright` 在 APT 仓库阶段持续数小时未完成。该 Run 不再作为完成路径继续等待。

EU-01 改由 PR #3 的分层、容器化验证重新取得完整 Current Evidence。

只有新的 Backend Verify、Frontend Verify 和 browser verification 全部 PASS 后，才可以声明 EU-01 Completed。

EU-01 完成后停止执行，不继续 EU-02；先将 Consumer Issue #1/#2 与最终验证结果汇总反馈到 `dygapp/agentic-dev` Experiment Issue #18，等待 agentic-dev 更新后，再基于新基线重新检查和执行 EU-02。
