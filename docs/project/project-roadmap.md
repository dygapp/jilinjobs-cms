# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap。

## 方法基线

```text
dygapp/agentic-dev
master@2ee56a5866d0201977a75b2b18ca2e791a218983
```

普通开发优先使用 Consumer-local：`AGENTS.md`、`docs/project/development-method.md`、当前 Requirement / Specification / Technical Plan。

## 总体路线

| 路线 | 状态 | 结果 |
|---|---|---|
| Consumer Repository Bootstrap | 已完成 | 独立 Consumer Authority 与工程骨架 |
| EU-01～EU-06 信息发布核心能力 | 已完成 | 栏目、导航、文章、发布、公开页面、附件、浏览量、响应式 |
| Feature-wide Convergence | 已完成 | 首轮功能整体验证闭环 |
| RC-01 人工集成评审环境 | 已完成 | 可手工启动临时 Review Environment |
| 站点基线收敛 EU-07～EU-12 | 当前 | 复刻现网站结构/视觉，补齐固定页面、页面组、网站配置、静态资源和初始化基线 |
| 真实第三方集成 / 中心党建 | 条件性后续 | 根据后续 Product Intent 再进入 |

## 已完成里程碑

| 日期 | 里程碑 |
|---|---|
| 2026-08-24 | Feature-wide Convergence 完成 |
| 2026-08-26 | RC-01 Human Integration Review Environment 完成 |
| 2026-08-26 | Consumer-local 方法与 Roadmap 固化 |
| 2026-08-26 | agentic-dev baseline 更新到 `2ee56a5866d0201977a75b2b18ca2e791a218983` |
| 2026-08-27 | 完成原站取证与 26 项关键 Product Intent 人工确认，进入站点收敛实施 |

## 当前阶段

当前核心目标：

> 将首轮“信息发布核心原型”收敛为能够反映原网站真实菜单、页面类型、固定页面、页面组、首页结构和静态资源管理方式的可人工评审版本。

当前阶段关键结论：

- `www.jilinjobs.cn` 与 `24365.jl.smartedu.cn` 作为同一原网站取证基线；
- 视觉从“布局现代化”改为“现网视觉与布局复刻 + 必要技术适配”；
- 中心党建本轮只保留主菜单占位；
- 慧就业本轮只保留页面组/页面占位；
- 新增固定页面、页面组、网站配置、网站静态资源管理；
- 新增 `/page/**`、`/column/{alias}`、`/article/{id}` 规范 URL；
- 站点初始化基线包含数据库数据和初始化静态资源包；
- 测试数据不再承担基础站点初始化；
- 历史内容迁移与初始化基线分离。

## 当前执行单元

```text
EU-07 站点结构与初始化基线
EU-08 固定页面与页面组闭环
EU-09 菜单、栏目 URL 与页面上下文收敛
EU-10 网站配置与静态资源管理
EU-11 现网主站前台复刻
EU-12 站点收敛验证与人工评审准备
```

详见 `docs/work/center-main-site-core-execution-units.md`。

## 下一步

连续推进 EU-07～EU-12，直到当前 Head 自动化验证通过、Review Environment 从当前 Head 成功启动并可以进入人工页面评审，或遇到真实权限、运行时或必须人工决定的架构阻塞。

人工评审阶段优先处理视觉精确度、首页区域细节、具体字号/间距/图片比例、业务指南 Tab 体验、固定页面呈现和低风险菜单/交互微调。

## Fresh Context 恢复入口

1. `AGENTS.md`
2. `README.md`
3. `docs/project/project-roadmap.md`
4. `docs/project/development-method.md`
5. `docs/requirements/information-publishing.md`
6. `docs/specifications/center-main-site-core.md`
7. `docs/technical/center-main-site-core.md`
8. `docs/work/center-main-site-core-execution-units.md`
9. 当前 Branch / PR / CI / Runtime Evidence

不得使用其他聊天或其他项目状态补充未固化的 Consumer 产品事实。
