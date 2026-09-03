# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap。

## 方法基线

```text
dygapp/agentic-dev
master@b80b2b1b7cea38eed0aef9807879e2a0d56afd2f
```

普通开发优先使用 Consumer-local：`AGENTS.md`、`docs/project/development-method.md`、当前 Requirement / Specification / Technical Plan。外部 baseline 的 Engineering Discipline / Technology Profile 只有经本仓库选择性固化后才成为普通开发规则；`agentic-dev` 自身 Project Roadmap、Foundation 状态、PR / Issue / Experiment 事实不进入本 Roadmap。

## 总体路线

| 路线 | 状态 | 结果 |
|---|---|---|
| Consumer Repository Bootstrap | 已完成 | 独立 Consumer Authority 与工程骨架 |
| EU-01～EU-06 信息发布核心能力 | 已完成 | 栏目、导航、文章、发布、公开页面、附件、浏览量、响应式 |
| Feature-wide Convergence | 已完成 | 首轮功能整体验证闭环 |
| RC-01 人工集成评审环境 | 已完成 | 可手工启动临时 Review Environment |
| 站点基线收敛 EU-07～EU-12 | 已完成 | 现网站点结构、页面模型、规范 URL、网站配置、静态资源与初始化基线完成自动化收敛 |
| 首页与公共视觉基线收敛 | 已完成 | 原站关键视觉资源、蓝白视觉体系、首页主要布局、Header / Nav / Footer、移动端基础适配及人工视觉复核完成 |
| 页面细节视觉收敛 | 已完成 | 栏目列表、文章详情、固定页面、页面组 / Tab、业务指南、页脚与外链文章行为完成自动化和人工视觉复核 |
| 管理端工程分离与功能收敛 | 已完成当前阶段 | 双前端物理拆分、通用 CMS 模型、Admin Modular SPA / CMS Module 边界完成收敛；后续按明确 Review Finding 增量调整 |
| 公开站 Multi-entry Modular SPA 与中心党建基础框架 | 已完成 | Main / Party 真实 Theme / Router Boundary、Party Entry / Router / 红色主题框架已建立 |
| 中心党建正式页面与内容收敛 | **当前** | EU-26～EU-28 已完成；当前进入 EU-29 历史内容迁移与最终 Review，保持真实四栏目、通用 CMS、Party canonical URL 与最终回归责任 |
| 公开站点剩余内容与集成收敛 | 后续 | 完善固定页面正式内容、嵌入内容、网站导航预设基线、剩余公开页面内容与外部聚合数据来源 |
| 真实第三方深度集成 | 条件性后续 | 根据第三方接口、认证、可靠性与 Product Intent 再进入 |

## 已完成里程碑

| 日期 | 里程碑 |
|---|---|
| 2026-08-24 | Feature-wide Convergence 完成 |
| 2026-08-26 | RC-01 Human Integration Review Environment 完成；Consumer-local 方法与 Roadmap 固化 |
| 2026-08-27 | 原站取证与关键 Product Intent 确认；EU-07～EU-12 完成实现与自动化收敛 |
| 2026-08-28 | 首页与公共视觉基线、页面细节视觉收敛及 Human Visual Review 完成 |
| 2026-08-28 | `agentic-dev` baseline 先后更新到 `df4d6a6...`、`bf21c7b...`，固化 Stale Verification Contract、Visual Fidelity、Human Review Finding、媒体验证、Evidence Claim 与 Roadmap / GitHub 状态边界 |
| 2026-09-01 | 管理端独立双前端、通用 CMS、Admin Modular SPA 与配置治理完成当前阶段收敛；baseline 更新到 `a82e559...`，固化共享资源并发、配置责任和已有能力复用 |
| 2026-09-01 | 公开站 Multi-entry Modular SPA 与中心党建基础框架完成，路线切换到党建正式页面与内容收敛 |
| 2026-09-02 | 中心党建第一轮原站重新取证完成，确认四条真实内容线、legacy `content_id/typeCode/detail path` 与 INTERNAL / EXTERNAL_LINK 混合模型，正式阶段切分 EU-26～EU-29 |
| 2026-09-03 | EU-26～EU-28 已完成并进入 `main`；当前 Roadmap 恢复到 EU-29 Historical Content Migration & Final Review |
| 2026-09-03 | `agentic-dev` baseline 更新到 `b80b2b1b7cea38eed0aef9807879e2a0d56afd2f`；Consumer-local Authority 选择性固化 Implementation Minimality、Surgical Diff Scope、Vue 3 + TypeScript Technology Profile、Verification Profile 与 Consumer Override Boundary，不继承 Foundation 项目事实，也不机械升级技术依赖 |

## 当前已固化结果

- `www.jilinjobs.cn` 与 `24365.jl.smartedu.cn` 作为同一原网站取证基线；
- 中心主站视觉原则为“现网视觉与布局复刻 + 必要技术适配”，不是现代化改版；
- 原站关键品牌、Banner、轮播、业务指南/快捷入口等稳定资源已纳入版本化初始化静态资源包；
- 主站 Header、Navigation、Footer、首页主要区域、栏目/详情/固定页/页面组/业务指南均已完成当前阶段视觉和人工复核；
- 栏目、菜单、文章、固定页面、页面组、网站配置和网站静态资源职责已分离；
- `/page/**`、`/column/{alias}`、`/article/{id}` 为 Main canonical URL；
- 管理端使用单一 Vue SPA / Router / build-deploy unit 的 Modular SPA，`app/` / `shared/` / `modules/cms/` 边界已建立；CMS canonical Admin 路由为 `/admin/cms/**`；Module Federation 仅保留条件性后续；
- 公开前端在同一 `frontend/public-site` 工程中使用 Main + Party 两个真实 Entry；Entry 按 Theme / Router Boundary 划分；Main / Party Shared Navigation / Footer 复用同一 DOM 与交互，通过主题变量切换；
- Party 使用 `/party/**` namespace；正式四栏目为高层声音 `party-voice/gcsy`、工作动态 `party-work/gzdt`、党规党章 `party-rules/dgdz`、理论学习 `party-study/llxx`；“学习园地”只是后两者的入口页视觉分组；
- 党建内容复用通用 Column + Article；同一栏目支持 INTERNAL / EXTERNAL_LINK；父栏目 `party` 只承担 CMS 组织和 Party scope 识别；
- Party canonical URL 为 `/party/`、`/party/column/{alias}`、`/party/article/{id}`；旧 `plist.html` / `pdetail.html` / `detail.html` 与 legacy 参数只作为历史迁移输入，不恢复旧 query-string 页面模型；
- Party Flyway 只固化稳定站点结构；历史文章、外链、正文资源、附件、轮播成员和 legacy 映射由独立迁移机制处理；
- 中心党建不拆独立前端工程、不引入 `site` 字段或党建专属 Admin Module；
- Automated Verification 与 Human Review Baseline 分离；Review Environment 共享 FRP 资源按真实冲突域排队；
- Visual Fidelity 不由 Functional Browser PASS 单独证明；测试/Workflow assertion 可成为 Stale Verification Contract；Human Review Finding 按 Authority 分类；外部媒体按真实内容验证；后继提交 Evidence 只按具体 Claim 复用；
- Vue / TypeScript Engineering Default 当前受 Consumer 实际 package、tsconfig、Architecture、Element Plus、scripts 与 Verification Authority 覆盖；Research Anchor 不触发依赖升级；
- Roadmap 只维护持久路线与可恢复状态，不复制 PR open/merged、精确 Merge Commit、临时 Branch 删除等 GitHub 瞬时事实。

## 当前阶段：中心党建正式页面与内容收敛

持久执行路线以 `docs/work/party-convergence-execution-units.md` 为准：

```text
EU-26 Party Evidence & Authority Convergence：已完成
→ EU-27 Party CMS Structure & Content Routing：已完成
→ EU-28 Party Home & Visual Fidelity Convergence：已完成
→ EU-29 Party Historical Content Migration & Final Review：当前
```

当前约束：

1. 只使用原站确认的四条内容线，不恢复 Foundation 虚构栏目；
2. 复用 Column + Article，`party` 只承担 CMS 组织与 Party scope；
3. `学习园地` 只作为 PartyHome 固定布局分组；
4. Party 列表/详情 canonical route 保持 `/party/column/{alias}`、`/party/article/{id}`；
5. 历史运营内容和资源使用独立迁移机制，不进入 Flyway；
6. EU-29 必须保持幂等迁移、legacy 证据、四栏目真实内容、INTERNAL / EXTERNAL_LINK、正文资源/附件、`PARTY_CAROUSEL` 成员、不可迁移报告，以及 Party / Main / Admin 回归；
7. legacy URL redirect 只有在真实产品需要得到证据支持时才实现，不因为存在旧路径就机械增加兼容层；
8. EU-29 最终 Human Review 关闭后才切换到“公开站点剩余内容与集成收敛”。

## 后续公开站点内容与集成收敛

党建专项关闭后再按当前 Authority 推进：

1. 关于我们、预决算公开、就业创业师资库、联系我们、常见问题、业务指南等正式内容、图片、附件和必要专用布局；
2. 第三方条件明确后的招聘信息、直播课程等嵌入型内容；
3. 网站导航 / 友情链接预设内容；
4. 首页/公开页面剩余正式初始内容；
5. 招聘公告等外部聚合内容的同步、去重、更新与失效策略；
6. 其他历史文章、附件、图片和旧 URL 迁移准备；
7. 正式内容接入后的桌面端、移动端和主要浏览器回归。

不得回退已人工确认的主站首页总体视觉结构；真实内容暴露公共组件缺陷时只针对明确问题增量修复。

## 阶段切换原则

- 管理端、公开站 Multi-entry 基础架构阶段均已关闭，后续只按真实 Finding 增量维护；
- 当前主动路线是 EU-29 中心党建历史内容迁移与最终 Review；
- 党建完整内容和最终验收不得因基础框架已完成而提前声明；
- EU-29 Human Review 关闭后切换后续公开站点内容阶段；
- 调整 Main Router / Entry / Gateway 时，既有 Human Visual Review Evidence 不机械继承，按具体 Evidence Claim 重新判断；
- 最终 PR 不自动合并。

## Fresh Context 恢复入口

1. `AGENTS.md`
2. `README.md`
3. `docs/project/project-roadmap.md`
4. `docs/project/development-method.md`
5. `docs/requirements/information-publishing.md`
6. `docs/specifications/public-site.md`
7. `docs/specifications/party.md`
8. `docs/architecture/decisions/ADR-0002-public-site-multi-entry-modular-spa.md`
9. `docs/technical/public-site-frontend.md`
10. `docs/technical/party-frontend.md`
11. `docs/work/party-convergence-execution-units.md`
12. `docs/technical/verification-strategy.md`
13. 当前 Branch / PR / CI / Runtime Evidence

不得使用其他聊天或其他项目状态补充未固化的 Consumer 产品事实。