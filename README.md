# jilinjobs-cms

`jilinjobs-cms` 是吉林省智慧就业云平台中“信息发布与网站服务”相关能力的独立 Consumer 项目。

当前阶段建设一个**可运行的网站信息发布原型应用**，用于验证中心主站的信息发布核心需求。后续完善为正式系统或嵌入 `jilinjobs` 主系统，不属于当前阶段范围。

## 当前项目目标

本项目当前迭代首先实现中心主站的信息发布核心能力。

目标包括：

- 栏目管理；
- 菜单 / 导航组织；
- 信息发布核心能力；
- 网站前端三级页面：
  - 首页；
  - 二级页面；
  - 内容详情页面。

## 当前迭代范围（Scope）

### In Scope

- 栏目和分类管理；
- 菜单与导航组织；
- 信息内容发布；
- 中心主站公开访问页面：
  - 首页；
  - 栏目/二级页面；
  - 内容详情页面。

### Out of Scope

- 外部内容嵌入；
- 用户与权限管理及认证授权接入；
- “中心党建”二级网站；
- 评论及其他互动能力；
- 复杂统计分析；
- 多站点扩展能力；
- 后续嵌入 `jilinjobs` 主系统；
- MQ、MinIO、Redis 等当前原型不需要的基础设施。

详细业务需求范围可能比当前迭代更广。只有属于当前 Scope 的内容才是本轮有效实现要求。

## 权威业务需求

当前详细业务需求：

```text
docs/requirements/information-publishing.md
```

该需求文档已经由本仓库显式采纳，作为当前迭代 Scope 内的详细业务依据。

该来源文档中引用的 `docs/project/project.md` 和 `docs/requirements/overview/system-module-boundaries.md` 当前不存在于本 Consumer Repository，因此这些 upstream references 只保留来源关系，不构成本项目 Authority。当前需求解释必须同时服从本 README 的迭代 Scope 与 `AGENTS.md` 的 Authority Boundary。

需求事实必须来自明确的 Authority，不得通过实现习惯、其他项目经验或常见做法自行扩展、修改或替换。

## 当前规格说明（Specification）

当前迭代的 WHAT / WHY Specification：

```text
docs/specifications/center-main-site-core.md
```

Specification 只收敛当前 Scope 内的必需行为、边界与验收，不替代更高优先级 Authority，也不提前规定实现 HOW。

当前 Specification 已明确：当前原型后台不建设或接入认证授权机制，后台直接用于验证信息发布核心业务；该简化不代表未来正式系统的安全边界。

## 当前原型技术边界

以下内容已经由人工权威（Human Authority）确认：

- 当前形态：独立建设的原型应用；
- 后续完善为正式系统或嵌入 `jilinjobs`：不属于当前阶段；
- 前端：Vue 3 + TypeScript + Vite + Vue Router；
- 原型管理端：Element Plus；
- 后端：Spring Boot 模块化单体应用；
- 构建与运行基线：Gradle、Java 21、Kotlin；
- 数据持久化：MySQL + MyBatis；
- 当前阶段不考虑认证授权；
- 当前阶段不引入 MQ、MinIO、Redis。

当前技术计划（Technical Plan）：

```text
docs/technical/center-main-site-core.md
```

当前验证运行策略（Verification Runtime Strategy）：

```text
docs/technical/verification-strategy.md
```

Technical Plan 持久化跨执行单元需要长期共享的 HOW：模块化单体边界、前后端契约、数据与文件资源策略、同步事务模型、部署边界和验证责任；验证运行策略补充 Current Evidence 口径、分层 CI、容器化 E2E、超时边界与当前 GitHub Runtime 可观察性策略。普通、低影响、可逆的文件组织、库级细节、精确版本和测试命令仍留给执行阶段即时计划（JIT Plan）或当前 workflow 固定。

## 开发方法来源

本项目采用 `agentic-dev` 作为 AI Agent 驱动开发 Method 与 Skills 的知识来源：

```text
Repository:
dygapp/agentic-dev

Experiment Validation Baseline:
master@c76d2996497bfd9632eb75ead3bc38b7f2d647a9
```

使用原则：

- 按当前阶段加载需要的 Method / Skill；
- 不为了流程完整性创建无实际价值的文档或目录；
- 规格说明（Specification）负责 WHAT / WHY；
- 技术计划（Technical Plan）只在必要时产生；
- 执行单元（Execution Unit）应适合 Fresh Context 独立执行和验证。

## 当前阶段

```text
Specification Ready
→ Technical Plan Ready
→ Work Slicing & Readiness PASS
→ EU-01 Completed
→ agentic-dev Baseline Updated
→ EU-02 Readiness PASS
→ EU-02 Completed
```

EU-01「栏目管理闭环」已经取得完整 Current Evidence 并完成集成：

- 验证 PR：`#3`；
- 通过验证的 Head：`cf212739beef482d7fe746fdfa0a1e0c1fc57bab`；
- CI：Run `32318100988`（CI #12），Backend Verify、Frontend Verify、Backend Runtime、Frontend Runtime 与 Playwright browser verification 全部 PASS，整个 Run `completed/success`；
- 集成提交：`cdfe90fd9d0b165accf81e1f39dc3abf4ad14f0a`；
- Consumer Issue #1/#2 已记录最终处理结果并关闭；
- 完整实验反馈已提交至 `dygapp/agentic-dev` Experiment Issue #18。

EU-02「导航管理与公开入口」已经取得完整 Current Evidence 并完成集成：

- Readiness：基于 EU-01 的实际实现重新检查后，原 Unit 对“已有公开栏目路由”的假设被识别为隐藏前置条件；已返回 `slice-work`，改由 EU-02 自身建立导航所需的最小公开栏目入口，不提前实现 EU-04 的栏目文章列表、分页等完整二级页面行为；修正后重新执行 `readiness-check` 并 PASS；
- 验证 PR：`#4`；
- 通过验证的 Head：`9d84a8d996e3ce51aaf7d7d3d9dd0abdff6dad74`；
- CI：Run `32327138763`（CI #17），Backend Verify、Frontend Verify 与 Browser verification 全部 PASS，整个 Run `completed/success`，Playwright `2 passed (7.0s)`；
- 集成提交：`32e25b85b6aed893c20da6e2612cd5ac2a196350`；
- Completion Verification 过程中发现成功 E2E 默认 reporter 没有生成可上传报告；当前验证策略与 CI 已补强为成功路径生成 Playwright HTML report，并把缺少预期 evidence 文件视为失败，而不是静默忽略。

当前协调状态：

- EU-02 的产品实现已停止在当前 Unit 边界，没有提前进入文章业务或 EU-04 完整栏目内容列表；
- 本次收尾只更新 Completion Evidence 可观察性与权威阶段记录，不改变 EU-02 产品完成条件；
- `execute-unit` 在 EU-02 完成后停止，不在同一 Fresh Execution Context 中继续执行 EU-03；
- 下一执行单元进入实现前，应基于届时 Repository Authority 与实际 `main` 状态重新执行对应 readiness 判断。

实现阶段不得重新打开已经确认的产品范围或重大架构方向，除非出现新的权威冲突或当前证据证明存在阻塞问题。

## 当前仓库结构

```text
.
├── AGENTS.md
├── README.md
└── docs/
    ├── requirements/
    │   └── information-publishing.md
    ├── specifications/
    │   └── center-main-site-core.md
    ├── technical/
    │   ├── center-main-site-core.md
    │   └── verification-strategy.md
    └── work/
        └── center-main-site-core-execution-units.md
```

项目结构应随着真实开发需要逐步演进。

只有具有 Authority、协调、追踪或长期知识价值的信息，才应形成新的项目产物（Artifact）。

## GitHub Repository 操作约定

`dygapp/jilinjobs-cms` 已获得项目负责人持续授权。

后续 Agent 可以直接通过 GitHub Repository 进行本项目范围内的正常开发操作，不需要重复请求操作许可。

该授权不包含：

- 改变项目目标或 Scope；
- 替代产品决策；
- 未授权的外部系统操作；
- Production 发布或部署。
