# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap。

## 方法基线

```text
dygapp/agentic-dev
baseline-2026-09-04-engineering-capability@5be2e6aad29b2be6b8535b3690daf3533ee22a46
```

普通开发优先使用 Consumer-local：`AGENTS.md`、`docs/project/development-method.md`、当前 Requirement / Specification / Technical Plan。外部 baseline 的 Engineering Discipline / Technology Profile 只有经本仓库选择性固化后才成为普通开发规则；`agentic-dev` 自身 Project Roadmap、Foundation / Engineering Discipline Expansion 状态、PR / Issue / Experiment 事实不进入本 Roadmap。

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
| 中心党建正式页面与内容收敛 | 已完成 | EU-26～EU-29 全部关闭；真实四栏目、历史文章/资源/轮播已固化为仓库 canonical migration dataset，并完成最终 Human Review 与 Fresh DB 验证 |
| EU-30 轮播图方案与实现收敛 | **当前** | 专项复核 Main / Party 轮播方案、交互、响应式、数据边界与实现复杂度；不预设必须重构或引入第三方组件 |
| EU-31 浏览器兼容性与 Runtime Guard 收敛 | 后续已规划 | 在 EU-30 最终实现基础上建立 Public / Admin 双标准浏览器兼容契约、兼容构建目标、启动前检测与提示，并使用独立手工 Browser Compatibility Workflow 取得专项证据 |
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
| 2026-09-03 | EU-26～EU-28 已完成并进入 `main`；当前 Roadmap 恢复到 EU-29 Historical Content Migration & Final Review |
| 2026-09-03 | `agentic-dev` baseline 更新到 `b80b2b1b7cea38eed0aef9807879e2a0d56afd2f`；Consumer-local Authority 选择性固化 Implementation Minimality、Surgical Diff Scope、Vue 3 + TypeScript Technology Profile、Verification Profile 与 Consumer Override Boundary，不继承 Foundation 项目事实，也不机械升级技术依赖 |
| 2026-09-04 | `agentic-dev` baseline 更新到 `a0aece02414aa36ca7421db391cb3124ad0780f2`；相对 `b80b2b1b7cea38eed0aef9807879e2a0d56afd2f` 的新增提交仅涉及 `agentic-dev` 自身 Foundation v1 Closure、Project Roadmap、Consumer Adoption Handoff 与实验收尾记录，Consumer-local Method / Verification 语义无变化，仅更新精确 baseline 引用，不继承其项目状态 |
| 2026-09-04 | `agentic-dev` baseline 更新到 Tag `baseline-2026-09-04-engineering-capability`（commit `5be2e6aad29b2be6b8535b3690daf3533ee22a46`）；相对 `a0aece02414aa36ca7421db391cb3124ad0780f2` 新增 Data Access Scope & Boundedness Control Engineering Discipline，并完成 `agentic-dev` 自身 Engineering Discipline Expansion v1 Closure。Consumer-local Method 正式固化该数据访问纪律；现有 Verification Strategy §2.4 已覆盖对应作用域、分页窗口与边界验证语义，因此不重复建立第二套验证契约，也不继承 `agentic-dev` 项目状态 |
| 2026-09-04 | 固化 EU-29 之后的前端后续路线：先执行 EU-30 轮播图方案与实现收敛，再执行 EU-31 浏览器兼容性与 Runtime Guard 收敛；EU-30 当前只保留占位级规划，EU-31 已记录 Public / Admin 双标准、Public IE11 备用路径与独立兼容 Workflow 边界 |
| 2026-09-04 | EU-29 历史内容迁移与最终 Human Review 关闭：181 篇历史文章（120 INTERNAL / 61 EXTERNAL_LINK）、184 个历史资源和 4 条中心党建轮播从已验收 Snapshot 无损晋升为 `data-migrations/party/v1` canonical dataset；Fresh DB 首次导入、二次幂等、资源 SHA 与 Runtime reconciliation 通过，Review Environment 改为长期直接消费仓库 canonical 数据 |

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
- 集合型数据访问遵循 Data Access Scope & Boundedness Control：业务 scope 决定成员资格时先形成 scope 再 window / pagination；页面展示数量、固定 `LIMIT/OFFSET` 或客户端过滤不能替代业务作用域；稳定有界的结构性共享 snapshot 可按当前生命周期完整读取，不为形式统一机械分页；
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
- EU-29 已将 Human Review 接受的历史迁移数据固化为仓库 `data-migrations/party/v1` canonical dataset；固定 Snapshot Run #7 / Artifact 仅作为 provenance，稳定导入、Canonical Verification 与 Review Environment 不再依赖其可用期；
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

## 已完成阶段：中心党建正式页面与内容收敛

第一轮原站重新取证和 CMS 复用判断已经完成，本阶段持久执行记录以 `docs/work/party-convergence-execution-units.md` 为准：

```text
EU-26 Party Evidence & Authority Convergence：已完成
→ EU-27 Party CMS Structure & Content Routing：已完成
→ EU-28 Party Home & Visual Fidelity Convergence：已完成
→ EU-29 Party Historical Content Migration & Final Review：已完成
```

本阶段关闭结论：

1. 原站 `/dyzj` 已确认“高层声音、工作动态、党规党章、理论学习”四条正式内容线；实现未恢复 Foundation 虚构栏目；
2. 复用 Column + Article，父栏目 `party` 只承担 CMS 组织和 Party 作用域识别；
3. `学习园地` 只作为 PartyHome 入口页固定布局分组；
4. Party 列表/详情 canonical route 使用 `/party/column/{alias}`、`/party/article/{id}`；
5. Flyway 只建立栏目结构，历史运营文章和资源由独立 canonical migration dataset 承载；
6. EU-26～EU-29 已完成，党建正式页面与历史内容收敛阶段关闭；
7. EU-29 已建立幂等内容迁移、legacy identity/fingerprint、canonical dataset 与最终 Human Review 证据；后续历史内容增量直接维护 canonical dataset；
8. 最终视觉与真实历史 Runtime 数据均已取得对应 Human Review / Browser Evidence。

## 当前及后续阶段：EU-30～EU-31

EU-29 已关闭；当前先完成两个已确定的横向前端单元，再进入一般“公开站点剩余内容与集成收敛”。详细规划见 `docs/work/frontend-follow-up-execution-units.md`。

```text
EU-30 Carousel Architecture & Behavior Convergence：当前
→ EU-31 Browser Compatibility & Runtime Guard Convergence
```

- **EU-30** 当前开始：专项讨论 Main / Party 轮播的实现、数据、交互、响应式、无障碍与依赖方案，再决定是否需要重构；EU-29 已接受的历史轮播数据继续作为内容输入，不在此处改写迁移事实。
- **EU-31** 在 EU-30 的最终实现基础上执行：Public Site 面向公众，尽可能向更低版本浏览器兼容；Admin 也采用显式较低版本兼容路线，但允许高于 Public。两端都建立 Pre-bootstrap Compatibility Guard 与低版本提示。
- 完整 Browser Compatibility Matrix 使用独立、人工触发的专项 Workflow，不进入每次 PR / push 的默认 CI；日常 CI 只保留兼容 target 构建、Guard 测试、Chromium 功能回归等低成本守护。
- 当前不要求 IE11。若系统上线后明确要求 IE11，只对 Public Site 启动新的兼容 Architecture / Requirement / Technical Plan，届时可评估 Vue 降级、独立 Legacy Public Frontend、SSR/static fallback 或其他有效方案；Admin 即使届时也不要求 IE11。

## 后续公开站点内容与集成收敛

EU-30、EU-31 完成后，除党建专项外仍需：

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
- 中心党建正式页面与内容收敛 EU-26～EU-29 已关闭；
- 当前主动路线为 EU-30 轮播图方案与实现收敛；
- EU-30 关闭后切换到 EU-31 浏览器兼容性与 Runtime Guard 收敛；
- EU-31 关闭后再切换到“公开站点剩余内容与集成收敛”；
- EU-30 若改变轮播 DOM、CSS、交互、依赖或数据模型，EU-31 必须基于最终实现重新取得受影响的浏览器兼容证据；
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
12. `docs/work/frontend-follow-up-execution-units.md`
13. `docs/technical/verification-strategy.md`
14. 当前 Branch / PR / CI / Runtime Evidence

不得使用其他聊天或其他项目状态补充未固化的 Consumer 产品事实。
