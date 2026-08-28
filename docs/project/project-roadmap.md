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
| 页面细节视觉收敛 | 已完成 | 栏目列表、文章详情、固定页面、页面组 / Tab、业务指南、页脚与外链文章行为完成自动化和人工视觉复核 |
| 管理端工程分离与功能收敛 | 下一阶段 | 将公开网站与管理端拆分为独立前端工程，并对管理页面、后台功能和管理流程进行人工复核与完善 |
| 公开站点内容与集成收敛 | 后续 | 完善固定页面内容、嵌入内容、网站导航预设基线及剩余公开页面内容与集成 |
| 真实第三方深度集成 / 中心党建 | 条件性后续 | 根据后续 Product Intent、第三方接口与认证条件再进入 |

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
| 2026-08-28 | PR #18 完成栏目列表、文章详情、固定页、业务指南、页脚、favicon、举报固定页与外链文章等页面细节收敛，并通过最终人工复核，达到合并条件 |

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
- 站点初始化基线由数据库基线 + 版本化初始化静态资源包组成；
- Automated Verification 与 Human Review Baseline 已分离：自动 E2E 结束后恢复数据库与静态资源已知基线，再注入明确 Human Review Fixture；
- Review Environment 对同一 PR 使用并发互斥，避免固定 FRP 评审域名被多个 Head 同时占用；
- Visual Fidelity 不由 Functional Browser PASS 单独证明；当前路径使用原站参考证据、AI 截图对照与 Human Visual Review；
- 测试 / Workflow assertion 可能成为 Stale Verification Contract，失败时先核对当前 Authority / Specification，再决定修实现还是修验证层；
- 历史内容迁移与初始化基线分离；
- 中心党建当前只保留主菜单占位，第三方深度集成仍属于条件性后续。

## 下一阶段：管理端工程分离与功能收敛

当前前端仍是**一个 Vite / Vue 前端工程**：同一 `frontend/package.json`、同一 Router 同时承载公开网站路由与 `/admin/**` 管理路由。该结构适合前期原型和功能验证，但不再作为后续正式管理端收敛的目标结构。

下一阶段优先执行：

1. 重构前端工程，将公开网站与管理端拆分为两个独立前端工程，各自拥有独立入口、路由、依赖边界、构建产物和测试入口；具体目录名称、共享代码方式和部署路径在该阶段 Technical Plan 中确定，不在本 Roadmap 预设实现细节；
2. 保持后端 REST API 和现有业务模型稳定，先完成前端物理边界拆分，再根据实际问题调整 API；
3. 对栏目管理、菜单管理、文章管理、固定页面 / 页面组管理、网站配置管理、网站静态资源管理等现有管理页面进行完整人工复核；
4. 完善管理端页面布局、表单、校验、状态反馈、列表筛选/分页、危险操作提示、异常处理和可用性；
5. 复核站内文章 / 外链文章、页面渲染模式、菜单目标与打开方式等新模型在管理端的编辑体验，避免底层模型已扩展但管理界面仍停留在原型状态；
6. 检查管理 API 是否存在仅为早期原型服务的缺口或不一致，并补齐 Backend / Frontend / Browser Verification；
7. 管理端完成自动化收敛后建立独立的人工作业评审入口，再执行 Human Review。

## 后续公开站点内容与集成收敛

PR #18 关闭的是“当前页面结构与视觉细节收敛”，不是公开站点全部内容建设完成。后续仍需至少处理：

1. **完善固定页面建设与内容填充**：逐页核对关于我们、预决算公开、就业创业师资库、联系我们、常见问题、业务指南各页及其他固定页面，补齐正式内容、图片、附件和必要的专用布局；避免长期保留占位文本或只完成页面框架；
2. **完成嵌入内容建设**：在产品边界和第三方条件明确后，完成招聘信息各页面、就业指导直播课程等嵌入型内容的实际加载方案，包括嵌入地址配置、加载状态、失败提示、尺寸适配、移动端行为和必要的安全限制；第三方认证、接口联动或故障保障超出单纯页面嵌入时，单独进入第三方集成任务；
3. **固化网站导航预设内容**：核对现网网站导航 / 友情链接各分类、名称、排序和目标地址，将确认后的预设内容固化到可重复初始化的数据库基线中，同时保持后台可维护，不依赖 Review Fixture 或人工临时录入；
4. **补齐公开页面的正式初始化内容**：继续识别仍使用占位数据、测试数据或缺少正式初始内容的首页固定入口、业务指南快捷项、Banner / 专题入口等，并按“数据库基线 + 版本化静态资源包”原则固化；
5. **完成外部聚合内容的数据来源闭环**：招聘公告等已支持 `EXTERNAL_LINK` 的区域后续需要明确抓取 / 同步机制、来源字段、去重、更新与失效策略；页面模型与展示能力已经具备，但数据采集本身不属于 PR #18；
6. **继续历史内容与旧 URL 迁移准备**：完整历史文章、附件、图片和可识别旧地址映射继续与 Flyway 初始化基线分离，在上线前通过专项迁移任务处理；
7. **完成剩余响应式和真实内容回归**：正式内容与真实嵌入接入后重新检查桌面端、移动端、微信内置浏览器等主要场景，防止占位内容阶段无法暴露的高度、溢出、图片比例和长文本问题；
8. **按条件推进中心党建**：当前仍只保留主导航占位，主题视觉、栏目内容和专属能力需在 Product Intent 明确后作为独立阶段处理。

上述公开站点后续任务原则上不回退已经人工确认的首页总体视觉结构；如真实内容或嵌入暴露公共组件缺陷，只针对明确问题增量调整。

## 阶段切换原则

- PR #18 人工复核通过后即可合并，页面细节视觉收敛阶段关闭；
- 下一工作会话优先进入“管理端工程分离与功能收敛”；
- 公开站点剩余内容与嵌入任务保留在本 Roadmap，后续按独立阶段继续，不要求在 PR #18 中实现；
- 新阶段开始时先基于 `main` 和本 Roadmap 重新形成 Technical Plan / Execution Plan，不直接沿用旧 PR 分支。

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
