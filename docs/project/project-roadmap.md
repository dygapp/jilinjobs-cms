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
| 站点基线收敛 EU-07～EU-12 | 已完成 | 现网站点结构、页面模型、规范 URL、网站配置、静态资源与初始化基线完成自动化收敛 |
| 人工页面评审与集成决策 | 当前 | 在 Review Environment 中检查视觉精度与低风险交互，并决定是否集成当前 PR |
| 真实第三方集成 / 中心党建 | 条件性后续 | 根据后续 Product Intent 再进入 |

## 已完成里程碑

| 日期 | 里程碑 |
|---|---|
| 2026-08-24 | Feature-wide Convergence 完成 |
| 2026-08-26 | RC-01 Human Integration Review Environment 完成 |
| 2026-08-26 | Consumer-local 方法与 Roadmap 固化 |
| 2026-08-26 | agentic-dev baseline 更新到 `2ee56a5866d0201977a75b2b18ca2e791a218983` |
| 2026-08-27 | 完成原站取证与 26 项关键 Product Intent 人工确认，进入站点收敛实施 |
| 2026-08-27 | EU-07～EU-12 完成实现与自动化收敛；页面组后台、静态资源目录/替换/恢复、版本化初始化静态资源包及 Review Environment 均形成当前证据 |

## 当前阶段

当前核心目标：

> 对已经完成自动化收敛的现网站点基线版本进行人工页面评审和集成判断；视觉精度与低风险交互问题允许在人工 Review 后增量调整。

当前已固化结果：

- `www.jilinjobs.cn` 与 `24365.jl.smartedu.cn` 作为同一原网站取证基线；
- 视觉原则为“现网视觉与布局复刻 + 必要技术适配”；
- 中心党建本轮只保留主菜单占位；
- 慧就业本轮只保留页面组/页面占位；
- 栏目、菜单、文章、固定页面、页面组、网站配置和网站静态资源职责已分离；
- `/page/**`、`/column/{alias}`、`/article/{id}` 作为规范公开 URL；
- 页面组后台可维护，业务指南与招聘信息初始化关系来自 Flyway 基线；
- 网站静态资源后台支持目录浏览、上传、显式替换、查看/下载、删除到回收区和恢复，并保留风险提示；
- 站点初始化基线由数据库基线 + 版本化初始化静态资源包组成，Review Runtime 明确挂载并验证该资源包；
- 测试只把临时数据用于具体 CRUD/发布行为验证，不承担 guide/jobs 等基础站点结构初始化；
- 历史内容迁移与初始化基线分离；
- Review Environment 对同一 PR 使用并发互斥，避免固定 FRP 评审域名被多个 Head 同时占用。

## 已完成执行单元

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

1. 使用当前 PR 的 Review Environment 进行人工页面评审；
2. 重点检查现网视觉复刻精度、首页区域关系、字号/间距/图片比例、业务指南 Tab、固定页面呈现、菜单跳转以及静态资源管理易用性；
3. 低风险视觉/交互问题可在评审后增量调整；如无阻塞问题，则进入当前 PR 的集成决策；
4. 慧就业真实 iframe、中心党建主题和历史内容迁移按后续 Product Intent / 迁移阶段单独推进。

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
