# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap。

## 方法基线

```text
dygapp/agentic-dev
master@df4d6a607597eeb3684279e269cb073fcb398f83
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
| 首页与公共视觉基线收敛 | 已完成 | 原站关键视觉资源、蓝白视觉体系、首页主要布局、Header / Nav / Footer、移动端基础适配及人工视觉复核完成 |
| 页面细节视觉收敛 | 当前 | 继续收敛栏目列表页、文章详情页、固定页面、页面组 / Tab、业务指南等页面级视觉与交互细节 |
| 真实第三方集成 / 中心党建 | 条件性后续 | 根据后续 Product Intent 再进入 |

## 已完成里程碑

| 日期 | 里程碑 |
|---|---|
| 2026-08-24 | Feature-wide Convergence 完成 |
| 2026-08-26 | RC-01 Human Integration Review Environment 完成 |
| 2026-08-26 | Consumer-local 方法与 Roadmap 固化 |
| 2026-08-27 | 完成原站取证与关键 Product Intent 人工确认，EU-07～EU-12 完成实现与自动化收敛 |
| 2026-08-27 | PR #15 合并，站点结构、页面模型、后台闭环、静态资源和初始化基线进入 `main` |
| 2026-08-28 | PR #16 合并，首页与公共视觉基线、真实原站静态资源、AI/Human Review 数据隔离及视觉复核进入 `main` |
| 2026-08-28 | `agentic-dev` baseline 更新到 `df4d6a607597eeb3684279e269cb073fcb398f83`，同步验证证据、Stale Verification Contract、Visual Fidelity 与 Human Review Environment 边界 |

## 当前阶段

当前核心目标：

> 在不重新设计已通过人工视觉复核的首页整体结构前提下，继续对栏目列表页、文章详情页、固定页面、页面组 / Tab 与业务指南等页面进行运行时取证和视觉细节收敛，并保持当前验证与人工评审闭环。

当前已固化结果：

- `www.jilinjobs.cn` 与 `24365.jl.smartedu.cn` 作为同一原网站取证基线；
- 视觉原则为“现网视觉与布局复刻 + 必要技术适配”，不是现代化改版；
- 原站关键 Logo、Header Banner、轮播图、招聘活动横幅、业务指南 / 快捷入口图标等已纳入版本化初始化静态资源包；
- Header、主导航、Footer 与首页主要区域关系已完成首轮视觉收敛并通过人工视觉复核；
- 首页整体结构默认不在下一轮重新设计，除非页面细节工作暴露出明确的公共组件缺陷；
- 栏目、菜单、文章、固定页面、页面组、网站配置和网站静态资源职责已分离；
- `/page/**`、`/column/{alias}`、`/article/{id}` 作为规范公开 URL；
- 页面组后台可维护，业务指南与招聘信息初始化关系来自 Flyway 基线；
- 网站静态资源后台支持目录浏览、上传、显式替换、查看/下载、删除到回收区和恢复，并保留风险提示；
- 站点初始化基线由数据库基线 + 版本化初始化静态资源包组成；
- Automated Verification 与 Human Review Baseline 已分离：自动 E2E 结束后恢复数据库与静态资源已知基线，再注入明确 Human Review Fixture；
- Review Environment 对同一 PR 使用并发互斥，避免固定 FRP 评审域名被多个 Head 同时占用；
- Visual Fidelity 不由 Functional Browser PASS 单独证明；当前路径使用原站参考证据、AI 截图对照与 Human Visual Review；
- 测试 / Workflow assertion 可能成为 Stale Verification Contract，失败时先核对当前 Authority / Specification，再决定修实现还是修验证层；
- 历史内容迁移与初始化基线分离；
- 中心党建当前只保留主菜单占位，慧就业真实 iframe 仍属于条件性后续。

## 下一步

1. 从当前 `main` 创建新的页面细节视觉收敛分支；
2. 从原网站选择具有代表性的栏目列表页、文章详情页、普通固定页面和页面组 / Tab 页面，重新取得运行时截图、DOM 和资源证据；
3. 与 Consumer 当前实现逐项比较页面宽度、Breadcrumb、标题、列表行、日期、分页、文章元数据、正文排版、Tab、间距、图片比例及移动端表现；
4. 直接修复已有 Authority 能明确判断的视觉偏差，不为低风险可逆细节反复请求人工确认；
5. 补齐与当前页面行为匹配的 Browser E2E / Visual Evidence，并保持 Automated Verification → Clean Human Review Baseline → Human Visual Review 闭环；
6. 完成自动化收敛后再进入下一轮人工视觉复核。

## Fresh Context 恢复入口

1. `AGENTS.md`
2. `README.md`
3. `docs/project/project-roadmap.md`
4. `docs/project/development-method.md`
5. `docs/requirements/information-publishing.md`
6. `docs/specifications/center-main-site-core.md`
7. `docs/technical/center-main-site-core.md`
8. `docs/technical/verification-strategy.md`
9. `docs/work/center-main-site-core-execution-units.md`
10. 当前 Branch / PR / CI / Runtime Evidence

不得使用其他聊天或其他项目状态补充未固化的 Consumer 产品事实。
