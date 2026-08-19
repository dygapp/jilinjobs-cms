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

以下内容已经由人工权威（Human Authority）明确：

- 当前形态：独立建设的原型应用；
- 后续完善为正式系统或嵌入 `jilinjobs`：不属于当前阶段；
- 后端与运行技术基线：Spring Boot、Gradle、Java 21、Kotlin；
- 数据持久化：MySQL + MyBatis；
- 当前阶段不考虑认证授权；
- 当前阶段不引入 MQ、MinIO、Redis。

### 前端方案提案（待人工确认）

Agent 当前选择：

```text
Vue 3 + TypeScript + Vite + Vue Router
```

选择原因：

- 与当前明确的 Vue.js 技术方向一致；
- 原型阶段依赖和运行复杂度较低；
- 适合把公开站点与管理端通过明确路由边界组织在同一前端工程中；
- 与 Spring Boot REST API 保持清晰前后端边界；
- 后续若进入正式系统阶段，仍可基于真实 SEO、部署或主系统集成要求重新评估渲染和集成方式。

该前端选择属于当前 Technical Planning 的最后一项人工确认点。在确认前不把它提升为最终 Technical Plan 决策，也不开始生产代码实现。

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
→ Technical Planning
→ Frontend Proposal Confirmation
```

此前阻塞 Technical Planning 的两项高影响问题已经解除：

1. **目标技术与部署架构基线**：当前作为独立原型应用建设，并已给出 JVM / Spring Boot / Kotlin / Gradle / MySQL / MyBatis 技术基线；
2. **后台认证与授权边界**：当前原型明确不考虑认证授权，只验证网站信息发布核心需求。

当前只剩前端方案需要按人工要求完成确认。确认后，Agent 将：

- 基于已确认架构形成最小必要 Technical Plan；
- 自主决定其余普通、低影响、可逆的实现细节；
- 进入 Work Slicing 与 Readiness Check；
- 只有在执行单元达到就绪条件后才进入生产代码实现。

## 当前仓库结构

```text
.
├── AGENTS.md
├── README.md
└── docs/
    ├── requirements/
    │   └── information-publishing.md
    └── specifications/
        └── center-main-site-core.md
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
