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
master@9ae3f4e73ef1e4b27a30f7ac791ae4b079dee269
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
→ EU-03 Readiness PASS
→ EU-03 Completion Verification PASS
→ EU-03 Integrated
→ agentic-dev Baseline Updated
→ EU-04 Readiness PASS
→ EU-04 Completion Verification PASS
→ EU-04 Integrated
→ agentic-dev Baseline Updated
→ EU-05 Readiness PASS
→ EU-05 Completion Verification PASS
→ EU-05 Completed / Awaiting Integration
```

EU-01「栏目管理闭环」已经取得完整 Current Evidence 并完成集成：

- 验证 PR：`#3`；
- 通过验证的 Head：`cf212739beef482d7fe746fdfa0a1e0c1fc57bab`；
- CI：Run `32318100988`（CI #12），Backend Verify、Frontend Verify、Backend Runtime、Frontend Runtime 与 Playwright browser verification 全部 PASS，整个 Run `completed/success`；
- 集成提交：`cdfe90fd9d0b165accf81e1f39dc3abf4ad14f0a`；
- Consumer Issue #1/#2 已记录最终处理结果并关闭；
- 完整实验反馈已提交至 `dygapp/agentic-dev` Experiment Issue #18。

EU-02「导航管理与公开入口」已经取得与最终实现匹配的 Completion Evidence 并完成集成：

- 验证 PR：`#4`；
- 通过验证的 Head：`9d84a8d996e3ce51aaf7d7d3d9dd0abdff6dad74`；
- CI：Run `32327138763`（CI #17），Backend verify、Frontend verify 与 Browser verification 全部 PASS，整个 Run `completed/success`；
- Browser verification 中 Playwright 纵向验证结果为 `2 passed (7.0s)`；
- 集成提交：`32e25b85b6aed893c20da6e2612cd5ac2a196350`。

EU-03「文章草稿与文件资源维护」已经取得与实现 Head 匹配的 Completion Evidence 并完成集成：

- 验证 PR：`#6`；
- 实现验证 Head：`8b0abb89cb86b939e7e6595f32916ece727c8260`；
- CI：Run `32331905285`（CI #22），Backend verify、Frontend verify 与 Browser verification 全部 PASS，整个 Run `completed/success`；
- Browser verification 中 Playwright 纵向验证结果为 `3 passed (11.6s)`，包含 EU-03 的文章草稿、文件资源与栏目内容依赖闭环；
- Artifact Collection 已确认存在 `backend-jar`（ID `9393319564`）、`frontend-dist`（ID `9393308838`）与 `playwright-evidence`（ID `9393352016`），三个 Artifact 都属于 Run `32331905285` 且绑定实现验证 Head `8b0abb89cb86b939e7e6595f32916ece727c8260`；
- 集成提交：`b86824a3db6ca33e6ff8b20598bdf75215792447`；
- EU-03 未实现发布/撤回操作或公开文章详情，相关能力仍属于后续 Execution Unit Scope；
- EU-03 本轮没有产生新的 `agentic-dev` Consumer Experiment Evidence。

EU-04「发布状态与公开三级页面」已经取得与实现 Head 匹配的 Completion Evidence 并完成集成：

- 验证 PR：`#7`；
- 实现验证 Head：`cc030c9da79a3d415682983e0a0164c6ea08a0f6`；
- CI：Run `32338802376`（CI #25），Backend verify、Frontend verify 与 Browser verification 全部 PASS，整个 Run `completed/success`；
- Browser verification 中 Playwright 结果为 `4 passed (12.6s)`，其中 EU-04 纵向用例验证 `DRAFT → PUBLISHED → WITHDRAWN → PUBLISHED`、首页/栏目/详情可见性、已发布编辑保持状态与正文图片公开访问；
- Artifact Collection 已确认存在 `backend-jar`（ID `9395597129`）、`frontend-dist`（ID `9395585376`）与 `playwright-evidence`（ID `9395638292`），三个 Artifact 都属于 Run `32338802376` 且绑定实现验证 Head `cc030c9da79a3d415682983e0a0164c6ea08a0f6`；
- 集成提交：`e143c8c1ef1ee5240279590bb7cddd4fc7de4122`；
- EU-04 只公开正文图片，不实现附件公开下载、复制链接、二维码或浏览量统计，这些能力仍保留在后续 Execution Unit；
- EU-04 执行中没有发现新的 `agentic-dev` Consumer Experiment Evidence。

EU-05「详情增强、附件与浏览量」已经取得与实现 Head 匹配的 Completion Evidence，当前等待集成：

- 验证 PR：`#8`；
- 实现验证 Head：`3489ecc21dfd34cf180f4f00999aaca560463e77`；
- CI：Run `32372151197`（CI #28），Backend verify、Frontend verify 与 Browser verification 全部 PASS，整个 Run `completed/success`；
- Browser verification 中 Playwright 结果为 `5 passed (13.8s)`，其中 EU-05 纵向用例验证草稿与撤回附件不可公开访问、发布后附件下载、稳定地址复制、二维码生成、MySQL 浏览量递增及管理端查看；
- Artifact Collection 已确认存在 `backend-jar`（ID `9407570112`）、`frontend-dist`（ID `9407561886`）与 `playwright-evidence`（ID `9407626646`），三个 Artifact 都属于 Run `32372151197` 且绑定实现验证 Head `3489ecc21dfd34cf180f4f00999aaca560463e77`；
- EU-05 没有实现复杂统计、来源分析、第三方分享接口或绕过受控资源边界的公开目录。

当前协调状态：

- 当前 `agentic-dev` validation baseline 为 `9ae3f4e73ef1e4b27a30f7ac791ae4b079dee269`，从 EU-05 起生效，不追溯重解释或重跑 EU-04；
- EU-02 readiness 曾返回 `slice-work` 修正“已有公开栏目路由”的隐藏前置条件，修正后由 EU-02 自身建立最小公开栏目入口且不侵入 EU-04；
- EU-03 readiness 在先修正 Consumer baseline Authority 漂移后 PASS；EU-03 已完成实现、Completion Verification 与集成；
- EU-04 readiness PASS，既有 Technical Plan 足以支撑本 Unit，实现过程中未形成新的跨 Feature 长期架构决策，因此未触发新的 Technical Planning 或 ADR；
- EU-04 已取得 Completion Evidence 并通过 PR #7 集成；
- EU-05 readiness-check PASS：Specification、既有 Technical Plan、依赖、受控资源边界和验证策略足以支撑本 Unit；未发现 Domain / Architecture Authority 或 ADR Gap；
- EU-05 已完成实现与 Completion Verification，当前 PR #8 等待集成；不得自动进入 EU-06；
- GitHub Actions 仍是后续跨前后端 Execution Unit 的重要 Completion Evidence 来源；按 `execute-unit` 从当前 Repository Rules 与实际仓库状态发现并执行验证机制。

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
