# 管理端工程分离与功能收敛规格说明（Specification）

## 1. 目标

本规格定义中心主站 CMS 下一阶段“管理端工程分离与功能收敛”的 WHAT / WHY。

当前公开站点与管理页面共同位于一个 Vue / Vite 工程。上一阶段已证明栏目、导航、文章、固定页面、页面组、网站配置和网站静态资源等核心业务能力可运行；本阶段不重新设计公开站产品模型，而是把管理端从原型页面集合收敛为可独立构建、可持续维护、可进行完整人工管理评审的独立前端应用。

## 2. Authority

依据：

1. `AGENTS.md`
2. `README.md`
3. `docs/requirements/information-publishing.md` V4.1
4. `docs/project/project-roadmap.md`

上一阶段 `docs/specifications/center-main-site-core.md` 中仍有效的产品规则继续适用；与本阶段已经确认的前端工程分离方向冲突的“单一 Vite 工程承载公开端与管理端”只代表历史实现状态，不再作为目标架构。

## 3. In Scope

- 公开站点与管理端拆分为两个独立 Vue / Vite 前端工程；
- 两个工程独立入口、Router、依赖边界、构建产物和 Browser Verification 入口；
- 同一 Review Runtime 下保持公开 canonical URL 不变，并通过 `/admin/**` 进入管理端；
- 管理端统一 Application Shell、管理导航和页面容器；
- 栏目管理、导航管理、文章管理、固定页面 / 页面组管理、网站配置管理、网站静态资源管理的可用性收敛；
- 列表筛选/分页、表单校验、状态反馈、危险操作提示和异常反馈；
- 站内文章 / 外链文章、Page Render Mode、菜单目标 / 打开方式等已存在模型的管理体验收敛；
- 网站配置真实 JSON 校验和类型化编辑体验；
- 网站静态资源真实媒体内容校验与关键资源保护；
- Page / SiteConfig / StaticResource 等新后台能力必要 Backend / Frontend / Browser Verification；
- 自动验证完成后建立 Human Admin Review Runtime。

## 4. Out of Scope

- 统一账号、登录、角色、权限系统；
- 多级审核、工作流、内容审批链；
- 通用 Page Builder；
- Dashboard / 统计驾驶舱；
- CMS 多站点；
- 中心党建主题实现；
- 慧就业等真实第三方 iframe / 认证集成；
- 生产部署拓扑拆分；
- 为了工程拆分而重构稳定后端业务模型或公共站视觉结构。

## 5. Required Behaviors

### 5.1 独立前端工程

公开站与管理端必须能够分别安装依赖、类型检查、构建和产生静态 Artifact。

公开站 Router 不注册 `/admin/**` 页面；管理端 Router 不注册公开站 canonical 页面。

公开站继续使用 `/`、`/column/{alias}`、`/article/{id}`、`/page/**` 等既有 URL。管理端以 `/admin/` 为应用入口，并保持已有 `/admin/columns`、`/admin/navigation`、`/admin/articles`、`/admin/pages`、`/admin/site-config`、`/admin/static-resources` 地址可访问。

### 5.2 Admin Application Shell

管理端应提供统一 Shell，至少包括：

- 产品/模块标题；
- 管理导航；
- 当前路由高亮；
- 页面内容容器；
- 可返回公开站的入口；
- 桌面管理场景下稳定的布局。

### 5.3 栏目与导航管理

保留现有树形栏目和菜单目标能力，并补齐明显的表单校验、操作反馈和可读状态表达。删除等高影响操作继续明确确认。

### 5.4 文章管理

文章列表应支持至少按标题关键字、栏目、状态、内容类型进行筛选，并提供管理端分页体验。

站内文章与外链文章的编辑区域按内容类型显示正确字段。站内文章继续支持正文、封面、正文图片和附件；外链文章不要求维护本站正文资源。

发布、撤回和重新发布必须为显式动作，编辑不改变发布状态。

### 5.5 固定页面与页面组

页面组与固定页面继续统一管理。

固定页面编辑应按 `renderMode` 显示正确字段：

- `RICH_TEXT`：可用富文本编辑区域；
- `EMBED_PLACEHOLDER`：显示占位正文并允许维护后续嵌入地址接缝；
- `INTERNAL_STATIC`：允许维护站内实现路径接缝，但不得上传任意 HTML/JS 执行。

已有 `embedUrl` 模型必须能够通过管理端正确编辑，而不能只存在于后端模型。

### 5.6 网站配置

后台仍只维护系统注册的预定义配置项。

`JSON` 配置必须在后端执行真实 JSON parse 校验；管理端保存前也应给出可读格式错误提示。`RESOURCE_PATH`、`TEXT` 与 `JSON` 类型应有明确的编辑和类型提示。

### 5.7 网站静态资源

继续支持目录浏览、上传、替换、查看/下载、删除到回收区和恢复。

上传文件必须同时满足：

- 允许扩展名；
- 实际媒体/二进制内容能够被允许类型验证，不得仅凭扩展名放行。

当前第一版关键资源保护规则：

- `health/baseline.png` 属于 Runtime 基线保护资源；
- 当前 `RESOURCE_PATH` 类型网站配置实际引用的 `/static/**` 文件属于受保护资源；
- `HOME_BANNERS` 中实际引用的 `/static/**` 图片属于受保护资源；
- 受保护资源禁止通过普通“删除”入口删除；
- 受保护资源允许通过明确“替换”操作更新，管理端必须强化风险提示；
- 系统不承诺完整扫描所有 CSS / JS / 富文本 / 人工静态页面引用，其他资源仍按已有风险提示处理。

### 5.8 管理端验证闭环

本阶段完成时必须分别取得：

- Backend Current Evidence；
- Public Site Frontend Current Evidence；
- Admin Frontend Current Evidence；
- 管理端自身 Browser E2E；
- 后台修改到公开站展示的跨边界 Browser E2E；
- Clean Human Admin Review Baseline 和可访问的外部 `/admin/` 入口。

## 6. Acceptance Criteria

1. 两个前端工程可独立安装、类型检查、构建。
2. 公开站构建产物不包含管理路由，管理端构建产物不承担公开站页面。
3. Review Runtime 中 `/` 与既有公开 canonical URL 正常；`/admin/` 和六类管理 URL 正常。
4. 管理端具有统一 Shell 和可导航的六类管理入口。
5. 栏目和导航管理既有 CRUD / 状态能力无回归。
6. 文章列表支持关键字、栏目、状态、类型筛选及分页；编辑和发布状态规则正确。
7. 固定页面按 render mode 编辑，`embedUrl` 管理能力不再缺失。
8. 网站配置 JSON 使用真实 JSON 解析校验，错误值不能保存。
9. 静态资源伪装扩展名上传被拒绝；受保护资源不能普通删除；替换和普通资源回收/恢复仍工作。
10. 新增/调整 Backend 行为有定向测试。
11. Browser E2E 覆盖后台文章发布到公开页面、固定页/配置或其他至少一个后台→公开站闭环。
12. 自动验证后清理测试状态并准备明确 Human Admin Review Fixture。
13. 外部 Review URL 的公开站和 `/admin/` 都可访问。
14. 最终 PR 不自动合并，由 Human Authority 决定集成。
