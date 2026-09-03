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
| 公开站 Multi-entry Modular SPA 与中心党建基础框架 | 已完成 | Entry 已从页面类型收敛为真实 Theme / Router Boundary；Main Site 已模块化并移除重复 Page Entry；Party Entry / Router / 红色主题基础框架已建立 |
| 中心党建正式页面与内容收敛 | **当前** | 原站信息架构重新取证已完成第一轮，确认四条真实内容线并确认通用 Column + Article 足够；按 EU-26～EU-29 完成结构、页面、视觉、历史内容迁移与最终 Review |
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
| 2026-09-01 | 项目负责人确认中心党建下一阶段立即进入实现；公开站目标架构调整为按真实 Site / Theme Boundary 划分 Entry 的 Multi-entry Modular SPA，并规划 Main Site 重构与 Party 基础框架 |
| 2026-09-01 | 公开站 Multi-entry Modular SPA 与中心党建基础框架完成：Main Site 模块化、重复 Page Entry 移除、Party Entry / Router / 红色主题基础框架、预置导航切换及 Main / Party / Admin / Backend / Gateway 验证闭环完成；路线切换到中心党建正式页面与内容收敛 |
| 2026-09-02 | 中心党建正式阶段完成第一轮原站重新取证：确认“高层声音 / 工作动态 / 党规党章 / 理论学习”及 legacy `gcsy/gzdt/dgdz/llxx`，确认当前 `pdetail.html`、更早 `detail.html` 与外部权威来源混合；通用 Column + Article 足以承载，正式阶段切分为 EU-26～EU-29 |

## 当前已固化结果

- `www.jilinjobs.cn` 与 `24365.jl.smartedu.cn` 作为同一原网站取证基线；
- 中心主站视觉原则为“现网视觉与布局复刻 + 必要技术适配”，不是现代化改版；
- 原站关键 Logo、Header Banner、轮播图、招聘活动横幅、业务指南 / 快捷入口图标等已纳入版本化初始化静态资源包；
- Header、主导航、Footer 与首页主要区域关系已完成视觉收敛和人工复核；
- 栏目列表、文章详情、普通固定页面、页面组 / Tab 与业务指南已完成当前阶段页面级视觉收敛；
- 页脚已补齐公安备案图标、事业单位标识、微信公众号二维码和动态 Copyright 年份；网站 favicon 已固化；
- 首页“举报电话及邮箱”已转为站内固定页面；
- 文章支持 `INTERNAL` 与 `EXTERNAL_LINK` 两种承载方式；“招聘公告”栏目允许同时发布两类文章，首页招聘公告区域只聚合已发布外链文章；
- 首页通知公告、就业动态与招聘公告已按各自稳定栏目边界加载公开文章，不再依赖“全站前 N 条文章再前端筛选”的全局截断；
- 首页整体结构默认不在后续页面内容完善过程中重新设计，除非暴露出明确的公共组件缺陷；
- 栏目、菜单、文章、固定页面、页面组、网站配置和网站静态资源职责已分离；
- `/page/**`、`/column/{alias}`、`/article/{id}` 作为主站规范公开 URL；
- 页面组后台可维护，业务指南与招聘信息初始化关系来自 Flyway 基线；
- 网站静态资源后台支持目录浏览、上传、显式替换、查看/下载、删除到回收区和恢复，并保留风险提示；
- 管理端当前采用单一 Vue SPA / 单一 Router / 单一构建部署的 Modular SPA：`app/` 负责 Admin Shell 与 Module Registry，`shared/` 承担跨模块管理端 primitives，`modules/cms/` 持有 CMS 业务路由、页面、组件与私有样式；Shell 通过公开 Module Contract 消费模块声明，不直接持有 CMS feature route / selector 知识；
- CMS canonical 管理端路由使用 `/admin/cms/**`，旧 `/admin/<cms-feature>` 仅作为兼容重定向；Module Federation 不属于当前基础设施，只有出现真实独立发布、独立部署、跨团队或跨技术栈需求时再评估 Remote Module 演进；
- 公开前端采用同一 `frontend/public-site` 工程中的 Main + Party 两个真实 Entry；Entry 按 Theme / Router Boundary 而非普通页面类型划分；
- 历史 `page.html / page-main.ts` 重复 Entry 已移除，`/page/**` 与 `/column/**`、`/article/**` 统一由 Main Site Entry 承载并保持直接访问 / 刷新行为；
- Main Site 持有独立 App / Router / 蓝白内容主题，源码已按 `home / content / page` 等职责形成模块边界，路由级页面使用动态 import；
- Party 使用 `/party/**` namespace，持有独立 App / Router / Banner / 内容 Frame / 红色页面主题；Foundation 阶段已经关闭，正式页面/视觉不以 Foundation 占位文案和临时 CSS 为 Authority；
- Main / Party 主导航和 Footer 已抽取为 Shared Shell Components，共用 DOM、菜单层级、交互、响应式和机构信息，仅通过主题变量切换蓝色 / 红色；
- 原站党建正式信息架构确认四条内容线：高层声音 `gcsy`、工作动态 `gzdt`、党规党章 `dgdz`、理论学习 `llxx`；“学习园地”是 PartyHome 入口页对后两者的视觉分组，不是独立 CMS 内容类型；
- 党建正式内容继续复用通用 Column + Article：预置父栏目 `party` 组织 `party-voice / party-work / party-rules / party-study`；同一栏目可混合 INTERNAL / EXTERNAL_LINK；
- Party canonical URL 确认为 `/party/`、`/party/column/{alias}`、`/party/article/{id}`；原站 `plist.html`、当前 `pdetail.html`、更早 `detail.html` 及 `content_id/typeCode` 参数变体只作为历史迁移输入；
- 中心党建不拆成独立前端工程，不引入 Module Federation、`site` 字段或党建专属 Admin Module；只有真实独立生命周期或专属业务模型出现后再评估；
- 党建 Flyway 只固化站点结构，历史文章、外链、正文资源与 legacy id/typeCode/detail path 映射继续通过独立内容迁移/采集机制处理；
- 预置主导航“中心党建”已从 `PLACEHOLDER` 切换为当前窗口进入 `/party/` 的站内 `LINK`；Main 将 `/party/**` 视为跨 Entry document navigation，Party 将 `/party/**` 视为当前 Router 内部导航；
- 当前技术命名统一使用 `party / Party`；仅 `/party/` 入口页使用 `party-home / PartyHome`（如 `PartyHomeView.vue`）。已执行 V13/V14 Migration 和当前 PR 分支名中的 `party-building` 属于历史/兼容标识，不回写历史；
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

## 已完成阶段：公开站 Multi-entry Modular SPA 与中心党建基础框架

本阶段依据 `ADR-0002-public-site-multi-entry-modular-spa.md` 和 `docs/work/public-site-multi-entry-execution-units.md` 完成 EU-23～EU-25。

### 已完成目标

1. 将中心党建基础公开页面从“占位”推进为独立 Theme / Router Boundary；
2. 把公开前端 Entry Boundary 从“普通页面类型”改为“真实 Theme / Router Boundary”；
3. 将 Main Site 从平铺结构重构为 `app / shell / modules`，并使用 route-level lazy loading；
4. 删除 `page.html / page-main.ts` 重复 Entry，同时保持 `/page/**` canonical URL 与直接刷新行为；
5. 建立 Party 独立 `party.html` Entry、App、Router、红色主题基础框架；
6. 将预置主导航“中心党建”切换为当前窗口进入 `/party/` 的站内 `LINK`；
7. 保持一个 `frontend/public-site` 工程、一个 package、一个 build/deploy unit；未引入 Module Federation，也未创建独立党建前端工程；
8. Main / Party / Admin / Backend / Gateway 已取得与目标实现匹配的 Current Evidence；
9. 架构基础阶段关闭，正式党建阶段重新取得原站证据并建立新的 Requirement / Specification / Technical Plan。

### 已完成执行顺序

```text
EU-23 Public Frontend Authority & Architecture Convergence：已完成
→ EU-24 Main Site Modularization & Page Entry Removal：已完成
→ EU-25 Party Site Entry & Foundation Shell：已完成
```

## 当前阶段：中心党建正式页面与内容收敛

第一轮原站重新取证和 CMS 复用判断已经完成，当前持久执行路线以 `docs/work/party-convergence-execution-units.md` 为准：

```text
EU-26 Party Evidence & Authority Convergence
→ EU-27 Party CMS Structure & Content Routing
→ EU-28 Party Home & Visual Fidelity Convergence
→ EU-29 Party Historical Content Migration & Final Review
```

当前阶段约束：

1. 原站 `/dyzj` 已确认“高层声音、工作动态、党规党章、理论学习”四条正式内容线；实现不得恢复 Foundation 虚构栏目；
2. 复用 Column + Article，父栏目 `party` 只承担 CMS 组织和 Party 作用域识别；
3. `学习园地` 只作为 PartyHome 入口页固定布局分组；
4. Party 列表/详情 canonical route 使用 `/party/column/{alias}`、`/party/article/{id}`；
5. 新 Flyway 只建立栏目结构，历史运营文章和资源走独立迁移；
6. EU-27 先完成功能/结构闭环，EU-28 再依据更强视觉证据收敛最终红色页面；
7. EU-29 建立幂等内容迁移和 legacy URL 映射证据，最终 Human Review 通过后关闭党建阶段；
8. Foundation 红色 Theme 不能替代最终视觉复刻证据。

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
- 公开站 Multi-entry Modular SPA 与中心党建基础框架阶段已经关闭，Main / Party Boundary 作为后续恢复基线；
- 当前主动路线为中心党建正式页面 / 内容 / 视觉 / 历史迁移专项；
- 党建完整内容和最终视觉不得因基础框架已经完成而提前声明完成；
- EU-29 Human Review 关闭后，Roadmap 切换到“公开站点剩余内容与集成收敛”；
- 后续如调整 Main Site Router / Entry / Gateway，既有 Human Visual Review Evidence 不机械继承，应按具体 Evidence Claim 重新取得必要自动化和视觉证据；
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