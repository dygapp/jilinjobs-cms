# 吉林智慧就业 CMS（jilinjobs-cms）

jilinjobs-cms 是吉林省智慧就业云平台“信息发布与网站服务”相关能力的 Consumer Repository，负责通用 CMS、中心主站 / 中心党建公开站、JilinJobs Site Package 与历史内容迁移的版本化实现。

## 当前范围

当前已接受的长期边界分为四层：

1. Generic CMS Core：Spring Boot Backend 提供通用 CMS 业务模型、API、Schema evolution 与 site-neutral provisioning capability；
2. JilinJobs Site Package：sites/jilinjobs/** 持有稳定站点结构、Fresh Site one-time bootstrap 与 stable Site assets；
3. Historical Content Migration：data-migrations/** 持有 canonical historical data、provenance、compatibility 与迁移输入；
4. Replaceable Public Renderer：frontend/public-site 是稳定 Public API / URL / Site Data Contract 的一个 Vue / Vite consumer。

管理端位于 frontend/admin，与公开站是同级独立前端工程并共享 Spring Boot CMS Backend。Main / Party 继续保持各自 Site / Theme / Router 边界，同时复用已接受的公共 CMS 与 Shared Shell contract。

当前产品范围、行为与验收标准不得从本 README 的摘要反向扩展；详细 Authority 由 AGENTS.md 与 docs/README.md 指向的 Current documents 决定。

Main historical migration execution 当前保持 FROZEN / explicit reactivation only：既有 data-migrations/main/** canonical evidence 保留，普通开发流程不得自行重新激活 Main migration；Party migration 不在该冻结范围内。

## Fresh Context 恢复

Fresh Context 只需要固定两个入口：

1. AGENTS.md：Repository 身份、Authority 边界、Skills / constraints locator 与稳定权限边界；
2. docs/README.md：Consumer-owned Current Authority 导航。

之后按任务只读取会改变当前判断的最小 owner。正式 active Execution Unit、Branch / PR / Actions、Roadmap、Requirement、Specification、Architecture、Technical 等均从其真实 owner 按需恢复，不在 Bootstrap 中复制。

docs/**/archive/** 与 docs/work/archive/** 默认只承担 traceability / historical evidence，不参与 ordinary Fresh Context，除非当前任务明确需要历史来源。

## agentic-dev 采用

本仓库按 docs/project/agentic-dev.md 记录的 exact version 使用 agentic-dev：

- installed Skills：.agents/skills/**
- install lock / provenance：skills-lock.json
- Consumer-local constraints：docs/governance/constraints.md
- path-scoped frontend constraints：frontend/AGENTS.md

Provider Guides 只在需要方法论导航时按 exact tag 定向读取；普通执行不依赖 Provider docs/**，本仓库也不维护 Provider Method / Rule / Capability runtime。

## 主要目录

- backend/：Spring Boot CMS Backend；
- frontend/admin/：CMS Admin Vue / Vite frontend；
- frontend/public-site/：Main / Party Public Vue / Vite frontend；
- sites/：Site Package schema 与具体 Site packages；
- data-migrations/：Historical Content Migration workspace 与 canonical datasets；
- docs/requirements/：Product / Domain Requirement；
- docs/specifications/：Feature / surface observable contract；
- docs/architecture/：长期系统结构、Requirement ownership 与 ADR；
- docs/design/：设计权威；
- docs/technical/：Implementation / Interface / Verification contract；
- docs/project/：Roadmap、Evolution 与 adoption provenance；
- docs/work/：只有需要正式 Execution Unit 时使用的 current / historical work；
- .agents/skills/：标准安装的 agentic-dev Skills。

## 开发与验证入口

- Repository 工作规则：AGENTS.md
- Documentation / Authority 导航：docs/README.md
- Product / Domain Requirement：docs/requirements/index.md
- CMS Architecture：docs/architecture/cms-architecture.md
- Verification Strategy：docs/technical/verification-strategy.md
- Durable Roadmap：docs/project/project-roadmap.md
- Current formal work locator：docs/work/current/README.md
- Consumer-local constraints：docs/governance/constraints.md
- agentic-dev adoption / provenance：docs/project/agentic-dev.md
- Backend build / ownership：backend/build.gradle.kts、backend/settings.gradle.kts、backend/README.md
- Frontend subtree：frontend/README.md + frontend/AGENTS.md

成功、完成、通过或修复声明必须具有与目标提交和 claim 类型匹配的当前证据。
