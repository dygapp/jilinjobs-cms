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
| 管理端工程分离与功能收敛 | 当前 | 双前端物理拆分、通用 CMS 模型与 Admin Modular SPA 架构已完成收敛，继续按明确的 Human Admin Review / 人工指令增量收敛后台页面与管理体验 |
| 公开站点内容与集成收敛 | 后续 | 完善固定页面内容、嵌入内容、网站导航预设基线及剩余公开页面内容与集成 |
| 真实第三方深度集成 / 中心党建 | 条件性后续 | 根据后续 Product Intent、第三方接口与认证条件再进入 |

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

## 当前已固化结果

- `www.jilinjobs.cn` 与 `24365.jl.smartedu.cn` 作为同一原网站取证基线；
- 视觉原则为“现网视觉与布局复刻 + 必要技术适配”，不是现代化改版；
- 原站关键 Logo、Header Banner、轮播图、招聘活动横幅、业务指南 / 快捷入口图标等已纳入版本化初始化静态资源包；
- Header、主导航、Footer 与首页主要区域关系已完成视觉收敛和人工复核；
- 栏目列表、文章详情、普通固定页面、页面组 / Tab 与业务指南已完成当前阶段页面级视觉收敛；
- 页脚已补齐公安备案图标、事业单位标识、微信公众号二维码和动态 Copyright 年份；网站 favicon 已固化；
- 首页“举报电话及邮箱”已转为站内固定页面；
- 文章支持 `INTERNAL` 与 `EXTERNAL_LINK` 两种承载方式；“招聘公告”栏目允许同时发布两类文章，首页招聘公告区域只聚合已发布外链文章；
- 首页整体结构默认不在后续页面内容完善过程中重新设计，除非暴露出明确的公共组件缺陷；
- 栏目、菜单、文章、固定页面、页面组、网站配置和网站静态资源职责已分离；
- `/page/**`、`/column/{alias}`、`/article/{id}` 作为规范公开 URL；
- 页面组后台可维护，业务指南与招聘信息初始化关系来自 Flyway 基线；
- 网站静态资源后台支持目录浏览、上传、显式替换、查看/下载、删除到回收区和恢复，并保留风险提示；
- 管理端当前采用单一 Vue SPA / 单一 Router / 单一构建部署的 Modular SPA：`app/` 负责 Admin Shell 与 Module Registry，`shared/` 承担跨模块管理端 primitives，`modules/cms/` 持有 CMS 业务路由、页面、组件与私有样式；Shell 通过公开 Module Contract 消费模块声明，不直接持有 CMS feature route / selector 知识；
- CMS canonical 管理端路由使用 `/admin/cms/**`，旧 `/admin/<cms-feature>` 仅作为兼容重定向；Module Federation 不属于当前基础设施，只有出现真实独立发布、独立部署、跨团队或跨技术栈需求时再评估 Remote Module 演进；
- 站点初始化基线由数据库基线 + 版本化初始化静态资源包组成；
- Automated Verification 与 Human Review Baseline 已分离：自动 E2E 结束后恢复数据库与静态资源已知基线，再注入明确 Human Review Fixture；
- Review Environment 对同一 PR 使用并发互斥，避免固定 FRP 评审域名被多个 Head 同时占用；
- Visual Fidelity 不由 Functional Browser PASS 单独证明；当前路径使用原站参考证据、AI 截图对照与 Human Visual Review；
- 测试 / Workflow assertion 可能成为 Stale Verification Contract，失败时先核对当前 Authority / Specification，再决定修实现还是修验证层；
- Human Review Finding 需要按观察内容重新对照 Authority 分类，不因“视觉评审”名称自动缩减为视觉微调；
- 外部二进制 / 媒体资源在版本化或 Runtime 消费前按风险验证真实内容类型，不只依赖文件名、扩展名或响应头；
- 后继提交不按 `docs-only` 机械继承祖先 Evidence，只有按具体 Evidence Claim 完成影响判断并记录 commit / compare / claim 映射时才可复用未受影响证据；
- Roadmap 只维护持久路线与可恢复状态，不复制 PR / Merge Commit / 临时分支等 GitHub 瞬时状态；
- 历史内容迁移与初始化基线分离；
- 中心党建当前只保留主菜单占位，第三方深度集成仍属于条件性后续。

## 当前阶段：管理端工程分离与功能收敛

当前 `frontend/public-site` 与 `frontend/admin` 已完成物理拆分，分别拥有独立 Vue / Vite 入口、Router、依赖和构建产物，并共享同一个 Spring Boot CMS Backend。管理端内部也已完成 Modular SPA 架构收敛：Admin Shell、Shared Layer 与 CMS Module 的源码和样式所有权边界已经明确，CMS 通过 Module Contract / Registry 接入 Shell，并使用 `/admin/cms/**` canonical route namespace；当前仍保持单 SPA、单 Router、单构建和单部署，不引入 Module Federation。管理端通用 CMS 模型、预置结构保护、网站属性 / 静态资源治理和第一轮 Human Admin Review 也已进入 `main`；本阶段后续只在这一已固化架构基线上针对明确的管理端页面与交互问题做增量收敛，不回退双前端或 Modular SPA 边界，也不在人工下一步范围明确前扩大产品范围。

本阶段已确认目标结构与执行边界：

1. 将当前 `frontend` 拆分为 `frontend/public-site` 与 `frontend/admin` 两个独立 Vue / Vite 工程，各自拥有独立入口、Router、依赖边界、构建产物和 Browser Verification 入口；
2. 保持后端 Spring Boot 单工程、现有 REST API 与业务模型边界稳定，先完成前端物理拆分，再只针对真实管理能力缺口调整 API；
3. Review / Runtime Gateway 继续使用同一 Nginx / FRP 地址：`/admin/**` 路由到管理端静态产物，其余公开路径路由到公开站点静态产物；
4. 暂不预先建立大型共享前端 package；只有拆分后出现稳定、明确且值得长期维护的 transport 类型或通用实现重复时，再抽取最小共享包；
5. 建立统一 Admin Application Shell 和管理导航，使管理端从一组独立 `/admin/**` 页面收敛为完整管理应用；Admin 内部采用 Modular SPA，Shell 仅通过 Module Contract / Registry 组合业务模块，当前 CMS 模块持有自身 `/cms/**` canonical route、导航、兼容路由和私有样式；
6. 对栏目、导航、文章、固定页面 / 页面组、网站配置、网站静态资源六类管理能力进行功能与可用性收敛；
7. 补齐文章筛选/分页、固定页面 render mode 对应编辑字段、类型化网站配置、静态资源真实媒体内容校验、关键资源保护和新后台模块定向测试；
8. 认证、账号、角色、统一权限体系仍按当前 Requirement 保持 Out of Scope；高风险网站配置和静态资源只保留明确的后续权限边界；
9. 自动验证闭环完成后建立独立 Human Admin Review 入口和示例数据，进入人工管理端复核；
10. Module Federation、Remote Module 与独立前端部署不作为当前阶段目标；只有出现真实独立发布 / 部署、跨团队或跨技术栈需求时，才基于 ADR-0001 评估演进。

### 当前执行顺序

```text
EU-13～EU-18 独立管理端第一轮收敛：已完成
→ EU-19 CMS Authority & document boundary convergence：已完成
→ EU-20 General CMS model convergence：已完成
→ EU-21 Admin & Public consumption convergence：已完成
→ EU-22 Feature-wide re-verification & Human Review：已完成本轮集成前闭环
→ Admin Modular SPA / CMS Module architecture convergence：已完成
→ 后续管理端页面重构 / 细节收敛：等待人工明确下一轮范围后按需形成新的 Execution Units
```

当前 `docs/work/admin-frontend-convergence-execution-units.md` 保留 EU-13～EU-22 的已完成阶段追溯；管理端 Modular SPA 的长期架构决策由 `docs/architecture/decisions/ADR-0001-admin-frontend-module-integration.md` 承载，Roadmap 只记录其已经成为当前恢复基线。新的管理端页面收敛单元只在下一轮范围明确且确有持久协调价值时新增。上一阶段 `center-main-site-core` Technical Plan 中“一个 Vite 工程同时承载公开端与管理端”的描述仅代表历史实现基线，不再作为目标架构。

## 后续公开站点内容与集成收敛

当前公开站点页面结构与视觉细节阶段已经关闭，但公开站点全部内容建设尚未完成。后续仍需至少处理：

1. **完善固定页面建设与内容填充**：逐页核对关于我们、预决算公开、就业创业师资库、联系我们、常见问题、业务指南各页及其他固定页面，补齐正式内容、图片、附件和必要的专用布局；避免长期保留占位文本或只完成页面框架；
2. **完成嵌入内容建设**：在产品边界和第三方条件明确后，完成招聘信息各页面、就业指导直播课程等嵌入型内容的实际加载方案，包括嵌入地址配置、加载状态、失败提示、尺寸适配、移动端行为和必要的安全限制；第三方认证、接口联动或故障保障超出单纯页面嵌入时，单独进入第三方集成任务；
3. **固化网站导航预设内容**：核对现网网站导航 / 友情链接各分类、名称、排序和目标地址，将确认后的预设内容固化到可重复初始化的数据库基线中，同时保持后台可维护，不依赖 Review Fixture 或人工临时录入；
4. **补齐公开页面的正式初始化内容**：继续识别仍使用占位数据、测试数据或缺少正式初始内容的首页固定入口、业务指南快捷项、Banner / 专题入口等，并按“数据库基线 + 版本化静态资源包”原则固化；
5. **完成外部聚合内容的数据来源闭环**：招聘公告等已支持 `EXTERNAL_LINK` 的区域后续需要明确抓取 / 同步机制、来源字段、去重、更新与失效策略；页面模型与展示能力已经具备，但数据采集本身不属于当前管理端阶段；
6. **继续历史内容与旧 URL 迁移准备**：完整历史文章、附件、图片和可识别旧地址映射继续与 Flyway 初始化基线分离，在上线前通过专项迁移任务处理；
7. **完成剩余响应式和真实内容回归**：正式内容与真实嵌入接入后重新检查桌面端、移动端、微信内置浏览器等主要场景；
8. **按条件推进中心党建**：当前仍只保留主导航占位，主题视觉、栏目内容和专属能力需在 Product Intent 明确后作为独立阶段处理。

上述公开站点后续任务原则上不回退已经人工确认的首页总体视觉结构；如真实内容或嵌入暴露公共组件缺陷，只针对明确问题增量调整。

## 阶段切换原则

- 页面细节视觉收敛阶段已经关闭；
- 当前仍处于“管理端工程分离与功能收敛”路线，双前端拆分、第一轮 CMS 模型 / 管理能力收敛以及 Admin Modular SPA / CMS Module 架构收敛均已完成；后续只按明确的人工 Review Finding 或下一步指令在现有架构基线上形成增量工作；
- 公开站点剩余内容与嵌入任务保留在本 Roadmap，后续按独立阶段继续；
- 后续每轮管理端增量收敛仍先取得自动化 Current Evidence，再恢复干净 Human Admin Review Baseline 并交由人工复核；Human Review Finding 按 Consumer-local Method 分类路由；
- 最终 PR 不自动合并。

## Fresh Context 恢复入口

1. `AGENTS.md`
2. `README.md`
3. `docs/project/project-roadmap.md`
4. `docs/project/development-method.md`
5. `docs/requirements/information-publishing.md`
6. 当前阶段 Specification / Technical Plan / Execution Units
7. `docs/technical/verification-strategy.md`
8. 当前 Branch / PR / CI / Runtime Evidence

不得使用其他聊天或其他项目状态补充未固化的 Consumer 产品事实。