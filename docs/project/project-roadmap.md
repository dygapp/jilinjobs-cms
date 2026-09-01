# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap。

## 方法基线

```text
dygapp/agentic-dev
master@a82e559cb67cafbcf96265a70a1167a9a75db5ba
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
| 页面细节视觉收敛 | 已完成 | 栏目列表、文章详情、固定页面、页面组 / Tab、业务指南、页脚与外链文章行为完成自动化和人工视觉复核 |
| 管理端工程分离与功能收敛 | 已完成当前阶段 | 双前端物理拆分、通用 CMS 模型、Admin Modular SPA / CMS Module 边界完成收敛；后续只按 Human Admin Review / 明确人工指令增量调整 |
| 公开站 Multi-entry Modular SPA 与中心党建基础框架 | **当前** | 将公开前端 Entry 从页面类型改为真实 Site / Theme Boundary；主站源码模块化并移除重复 Page Entry；建立中心党建独立 Entry、Router、红色主题 Shell 与基础页面框架 |
| 中心党建正式页面与内容收敛 | 下一阶段 | 基于原站重新取证，完成党建栏目/页面/内容/资源与最终视觉，并判断通用 CMS 是否足够 |
| 公开站点剩余内容与集成收敛 | 后续 | 完善固定页面正式内容、嵌入内容、网站导航预设基线、剩余公开页面内容与外部聚合数据来源 |
| 真实第三方深度集成 | 条件性后续 | 根据第三方接口、认证、可靠性与 Product Intent 再进入 |

## 已完成里程碑

| 日期 | 里程碑 |
|---|---|
| 2026-08-24 | Feature-wide Convergence 完成 |
| 2026-08-26 | RC-01 Human Integration Review Environment 完成 |
| 2026-08-26 | Consumer-local 方法与 Roadmap 固化 |
| 2026-08-27 | 完成原站取证与关键 Product Intent 人工确认，EU-07～EU-12 完成实现与自动化收敛 |
| 2026-08-27 | 站点结构、页面模型、后台闭环、静态资源和初始化基线进入 `main` |
| 2026-08-28 | 首页与公共视觉基线、真实原站静态资源、AI/Human Review 数据隔离及视觉复核进入 `main` |
| 2026-08-28 | `agentic-dev` baseline 更新到 `df4d6a607597eeb3684279e269cb073fcb398f83`，同步验证证据、Stale Verification Contract、Visual Fidelity 与 Human Review Environment 边界 |
| 2026-08-28 | 栏目列表、文章详情、固定页、业务指南、页脚、favicon、举报固定页与外链文章等页面细节收敛完成；最终 Human Visual Review 已通过，页面细节视觉收敛阶段关闭 |
| 2026-08-28 | `agentic-dev` baseline 更新到 `bf21c7bcd711fd667c43007a72fae65750d1af09`，新增 Human Review Finding 分类、外部媒体真实内容验证、后继提交 Evidence Claim 影响判断和 Roadmap / GitHub 集成状态边界 |
| 2026-09-01 | 管理端独立双前端、通用 CMS 模型、预置结构保护、配置治理、统一图片预览与静态资源保护第一轮收敛进入 `main`；`agentic-dev` baseline 更新到 `a82e559cb67cafbcf96265a70a1167a9a75db5ba`，同步共享外部资源并发 / 释放边界与实施判断 / 已有能力复用规则 |
| 2026-09-01 | 管理端 Modular SPA 架构收敛完成，形成 Admin Shell / Shared / CMS Module 源码边界、Module Contract / Registry 集成机制与 `/admin/cms/**` canonical route namespace；Module Federation 仅保留为满足真实独立发布 / 部署需求后的条件性演进路径 |
| 2026-09-01 | 项目负责人确认中心党建下一阶段立即进入实现；公开站目标架构调整为按真实 Site / Theme Boundary 划分 Entry 的 Multi-entry Modular SPA，并规划 Main Site 重构与 Party Building 基础框架 |

## 当前已固化结果

- `www.jilinjobs.cn` 与 `24365.jl.smartedu.cn` 作为同一原网站取证基线；
- 中心主站视觉原则为“现网视觉与布局复刻 + 必要技术适配”，不是现代化改版；
- 原站关键 Logo、Header Banner、轮播图、招聘活动横幅、业务指南 / 快捷入口图标等已纳入版本化初始化静态资源包；
- Header、主导航、Footer 与首页主要区域关系已完成视觉收敛和人工复核；
- 栏目列表、文章详情、普通固定页面、页面组 / Tab 与业务指南已完成当前阶段页面级视觉收敛；
- 页脚已补齐公安备案图标、事业单位标识、微信公众号二维码和动态 Copyright 年份；网站 favicon 已固化；
- 首页“举报电话及邮箱”已转为站内固定页面；
- 文章支持 `INTERNAL` 与 `EXTERNAL_LINK` 两种承载方式；“招聘公告”栏目允许同时发布两类文章，首页招聘公告区域只聚合已发布外链文章；
- 首页整体结构默认不在后续页面内容完善过程中重新设计，除非暴露出明确的公共组件缺陷；
- 栏目、菜单、文章、固定页面、页面组、网站配置和网站静态资源职责已分离；
- `/page/**`、`/column/{alias}`、`/article/{id}` 作为主站规范公开 URL；
- 页面组后台可维护，业务指南与招聘信息初始化关系来自 Flyway 基线；
- 网站静态资源后台支持目录浏览、上传、显式替换、查看/下载、删除到回收区和恢复，并保留风险提示；
- 管理端当前采用单一 Vue SPA / 单一 Router / 单一构建部署的 Modular SPA：`app/` 负责 Admin Shell 与 Module Registry，`shared/` 承担跨模块管理端 primitives，`modules/cms/` 持有 CMS 业务路由、页面、组件与私有样式；Shell 通过公开 Module Contract 消费模块声明，不直接持有 CMS feature route / selector 知识；
- CMS canonical 管理端路由使用 `/admin/cms/**`，旧 `/admin/<cms-feature>` 仅作为兼容重定向；Module Federation 不属于当前基础设施，只有出现真实独立发布、独立部署、跨团队或跨技术栈需求时再评估 Remote Module 演进；
- 公开前端现状是 Vite `index.html + page.html` Multi-entry，但两个 Entry 实际加载相同 Vue App / Router；该结构已经识别为待收敛的历史实现，不再作为目标架构；
- 公开前端目标为同一 `frontend/public-site` 工程内的 Main Site + Party Building Site 两个真实 Site Entry；Entry 按 Site / Theme Boundary 而非页面类型划分；
- Main Site 将持有主站 App / Router / Shell / 蓝白主题，并按 `home / content / page / integration` 等职责模块化，路由级组件使用动态 import；
- Party Building Site 使用 `/party/**` namespace，持有独立 App / Router / Shell / 红色主题；当前只建设基础框架，真实党建内容和最终视觉留后续专项；
- `shared/` 只允许无主题 API transport、CMS DTO、SEO / resource utility 等稳定技术能力，Main / Party Header、Footer、Page Frame 与 Theme 不为形式复用进入 Shared；
- 中心党建当前不拆成独立前端工程，不引入 Module Federation；只有真实独立发布 / 部署、团队、技术栈或生命周期出现后再评估拆分；
- 中心党建公开 Site Boundary 不自动要求新的 Admin Module，后续先评估复用现有通用 CMS 对象；
- 站点初始化基线由数据库基线 + 版本化初始化静态资源包组成；
- Automated Verification 与 Human Review Baseline 已分离：自动 E2E 结束后恢复数据库与静态资源已知基线，再注入明确 Human Review Fixture；
- Review Environment 对同一 PR 使用并发互斥，避免固定 FRP 评审域名被多个 Head 同时占用；
- Visual Fidelity 不由 Functional Browser PASS 单独证明；当前路径使用原站参考证据、AI 截图对照与 Human Visual Review；
- 测试 / Workflow assertion 可能成为 Stale Verification Contract，失败时先核对当前 Authority / Specification，再决定修实现还是修验证层；
- Human Review Finding 需要按观察内容重新对照 Authority 分类，不因“视觉评审”名称自动缩减为视觉微调；
- 外部二进制 / 媒体资源在版本化或 Runtime 消费前按风险验证真实内容类型，不只依赖文件名、扩展名或响应头；
- 后继提交不按 `docs-only` 机械继承祖先 Evidence，只有按具体 Evidence Claim 完成影响判断并记录 commit / compare / claim 映射时才可复用未受影响证据；
- Roadmap 只维护持久路线与可恢复状态，不复制 PR / Merge Commit / 临时分支等 GitHub 瞬时状态；
- 历史内容迁移与初始化基线分离。

## 已完成阶段：管理端工程分离与功能收敛

`frontend/public-site` 与 `frontend/admin` 已完成物理拆分，分别拥有独立 Vue / Vite 依赖、构建产物和 Browser Verification 入口，并共享 Spring Boot CMS Backend。管理端内部已完成 Modular SPA 架构收敛：Admin Shell、Shared Layer 与 CMS Module 的源码和样式所有权边界明确，CMS 通过 Module Contract / Registry 接入 Shell，并使用 `/admin/cms/**` canonical route namespace；当前保持单 SPA、单 Router、单构建部署，不引入 Module Federation。

管理端后续页面或交互调整继续在这一架构上增量完成，不因当前路线切换到公开站而回退或重构既有 Admin Boundary。

`docs/work/admin-frontend-convergence-execution-units.md` 保留 EU-13～EU-22 阶段追溯；长期决策由 `ADR-0001-admin-frontend-module-integration.md` 承载。

## 当前阶段：公开站 Multi-entry Modular SPA 与中心党建基础框架

当前阶段依据 `ADR-0002-public-site-multi-entry-modular-spa.md` 和 `docs/work/public-site-multi-entry-execution-units.md` 推进。

### 阶段目标

1. 修正当前 Authority / Specification，使中心党建基础公开站点从“占位”进入当前范围，同时明确完整党建内容和最终视觉仍为下一阶段；
2. 把公开前端 Entry Boundary 从“普通页面类型”改为“真实 Site / Theme Boundary”；
3. 将 Main Site 从平铺 `views/components/api/css` 重构为 `app / shell / modules`，并使用 route-level lazy loading；
4. 删除 `page.html / page-main.ts` 重复 Entry，同时保持 `/page/**` canonical URL 与直接刷新行为；
5. 建立 Party Building Site 独立 `party.html` Entry、App、Router、红色主题 Shell 和基础首页框架；
6. 将现有预置主导航“中心党建”从 `PLACEHOLDER` 切换为当前窗口进入 `/party/` 的站内 `LINK`；
7. 保持一个 `frontend/public-site` 工程、一个 package、一个 build/deploy unit；不引入 Module Federation，也不创建独立党建前端工程；
8. 对 Main / Party / Admin / Backend / Gateway 取得与目标提交匹配的 Current Evidence；
9. 完成本阶段后，另起中心党建正式页面与内容收敛任务，重新取得原站视觉与资源证据。

### 当前执行顺序

```text
EU-23 Public Frontend Authority & Architecture Convergence
→ EU-24 Main Site Modularization & Page Entry Removal
→ EU-25 Party Building Site Entry & Foundation Shell
→ 中心党建正式页面 / 内容 / 视觉专项
```

当前计划按三个 PR 分层执行：Authority/ADR、Main Site 重构、Party Building 基础框架。这样每层都可以独立审查、独立验证和回滚；后续 PR 可以基于前序目标分支形成 stacked dependency，但最终集成仍按 Repository Authority 执行。

## 后续：中心党建正式页面与内容收敛

基础框架完成后，至少重新执行：

1. 原站党建运行时截图、DOM、computed style、静态资源和 URL 取证；
2. 确认党建首页、栏目、文章、专题及其他页面范围和 navigation structure；
3. 评估现有 Column / Article / Page / Navigation / CmsList / Advertisement / SiteProperty 是否足以承载党建数据；
4. 只有存在真实专属管理业务时才新增 Admin Module；
5. 补齐版本化党建静态资源与必要初始化数据；
6. 完成 Desktop / Mobile 自动化、AI Visual Comparison 和 Human Visual Review；
7. 基础红色 Theme 不能替代最终视觉复刻证据。

## 后续公开站点内容与集成收敛

除党建专项外仍需：

1. 完善关于我们、预决算公开、就业创业师资库、联系我们、常见问题、业务指南等正式内容、图片、附件和必要专用布局；
2. 在第三方条件明确后完成招聘信息、直播课程等嵌入型内容实际加载方案；
3. 固化网站导航 / 友情链接各分类、名称、排序和目标地址的预设内容；
4. 补齐仍使用占位数据或缺少正式初始内容的首页/公开页面数据；
5. 明确招聘公告等外部聚合内容的抓取 / 同步、去重、更新与失效策略；
6. 继续历史文章、附件、图片和旧 URL 迁移准备；
7. 正式内容与真实嵌入接入后重新执行桌面端、移动端和主要浏览器回归。

上述任务原则上不回退已经人工确认的主站首页总体视觉结构；如真实内容或嵌入暴露公共组件缺陷，只针对明确问题增量调整。

## 阶段切换原则

- 管理端工程分离、通用 CMS 与 Admin Modular SPA 当前阶段已经关闭，后续按明确 Review Finding 增量维护；
- 当前主动路线切换为公开站 Multi-entry Modular SPA 与中心党建基础框架；
- EU-23～EU-25 完成后，架构基础阶段关闭并切换到中心党建正式页面 / 内容 / 视觉专项；
- 党建完整内容和最终视觉不得在仅完成基础框架时提前声明完成；
- 公开站架构重构影响 Main Site Router / Entry / Gateway 时，现有 Human Visual Review Evidence 不机械继承，应按具体 Evidence Claim 重新取得必要自动化和视觉证据；
- 最终 PR 不自动合并。

## Fresh Context 恢复入口

1. `AGENTS.md`
2. `README.md`
3. `docs/project/project-roadmap.md`
4. `docs/project/development-method.md`
5. `docs/requirements/information-publishing.md`
6. `docs/specifications/public-site.md`
7. `docs/architecture/decisions/ADR-0002-public-site-multi-entry-modular-spa.md`
8. `docs/technical/public-site-frontend.md`
9. `docs/work/public-site-multi-entry-execution-units.md`
10. `docs/technical/verification-strategy.md`
11. 当前 Branch / PR / CI / Runtime Evidence

不得使用其他聊天或其他项目状态补充未固化的 Consumer 产品事实。