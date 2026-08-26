# jilinjobs-cms

`jilinjobs-cms` 是吉林省智慧就业云平台中“信息发布与网站服务”相关能力的独立 Consumer 项目。

当前项目已经完成中心主站信息发布核心原型的首轮建设、Feature-wide Convergence 和人工集成评审环境建设。后续工作进入已有 Consumer 的持续演进阶段。

## 当前项目目标

当前原型验证中心主站的信息发布核心能力，包括：

- 栏目管理；
- 菜单 / 导航组织；
- 信息内容发布；
- 网站前端三级页面：
  - 首页；
  - 栏目 / 二级页面；
  - 内容详情页面。

## 当前迭代范围（Scope）

### In Scope

- 栏目和分类管理；
- 菜单与导航组织；
- 信息内容发布；
- 中心主站公开访问页面：
  - 首页；
  - 栏目 / 二级页面；
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

详细业务需求范围可能比当前迭代更广。只有属于当前 Scope 的内容才是当前有效实现要求。

## 仓库权威入口

Repository Governance、Authority Boundary、Knowledge Boundary、Human Escalation 和 GitHub 操作授权统一以：

```text
AGENTS.md
```

为最高入口。

当前详细业务需求：

```text
docs/requirements/information-publishing.md
```

该需求文档已经由本仓库显式采纳，作为当前 Scope 内的详细业务依据。其引用但未被本 Consumer Repository 采纳的 upstream references 只保留 provenance，不自动成为本项目 Authority。

当前 Specification：

```text
docs/specifications/center-main-site-core.md
```

当前 Technical Plan 与 Verification Strategy：

```text
docs/technical/center-main-site-core.md
docs/technical/verification-strategy.md
```

## 当前原型技术边界

以下内容已经由当前项目 Authority 确认：

- 当前形态：独立建设的原型应用；
- 前端：Vue 3 + TypeScript + Vite + Vue Router；
- 原型管理端：Element Plus；
- 后端：Spring Boot 模块化单体应用；
- 构建与运行基线：Gradle、Java 21、Kotlin；
- 数据持久化：MySQL + MyBatis；
- 当前阶段不建设认证授权；
- 当前阶段不引入 MQ、MinIO、Redis。

后续完善为正式系统或嵌入 `jilinjobs` 主系统不属于当前已确认范围。

## 开发方法

本项目使用 `dygapp/agentic-dev` 作为 Method、Operating Guide 与 Skills 的上游知识来源。

当前精确 baseline：

```text
master@2ee56a5866d0201977a75b2b18ca2e791a218983
```

但后续普通开发不要求持续跨仓库读取 `agentic-dev`。本项目已经将当前采用的方法和 Skills 使用规则固化在：

```text
docs/project/development-method.md
```

除非项目负责人明确要求更新 `agentic-dev` baseline，后续开发优先遵循 Consumer Repository 自身的规则和权威文档。

## 项目路线图

当前项目阶段、已完成里程碑、当前目标、下一步工作与 Fresh Context 恢复入口统一维护在：

```text
docs/project/project-roadmap.md
```

README 不再并行维护 EU、PR、CI、Artifact 等易变化的详细状态流水；这些证据由 Git 历史、PR、CI 和对应长期 Artifact 保留。

当前总体状态：

```text
Consumer Bootstrap
→ Specification / Technical Planning
→ EU-01 ～ EU-06
→ Feature-wide Convergence
→ CV-01 Verification Closure
→ Feature-wide READY / Integrated
→ RC-01 Human Integration Review Environment
→ Consumer Continuous Evolution
```

## 当前开发原则

- Specification 负责 WHAT / WHY；
- Technical Plan 只在跨 Execution Unit 的长期 HOW 协调具有持续价值时持久化；
- Execution Unit 应纵向、可验证、范围明确并适合 Fresh Context；
- 每项 Acceptance Obligation 必须闭环到实现责任、验证责任、计划证据和 Current Evidence；
- 实现覆盖不等于验证覆盖；
- 没有 Current Evidence，不得声明完成、通过或修复成功；
- 没有新的 Product Intent 时，不自行创造新的产品范围或 Execution Unit。

## GitHub Repository 操作约定

`dygapp/jilinjobs-cms` 已获得项目负责人持续授权。Agent 可以直接进行本项目范围内的正常 Repository 操作，无需重复请求许可。

该授权不包含：

- 改变项目目标或 Scope；
- 替代产品决策；
- 未授权的外部系统操作；
- Production 发布或部署；
- Credentials、Secrets 或其他敏感配置操作。
