# jilinjobs-cms

`jilinjobs-cms` 是“吉林省高等学校毕业生就业信息网”信息发布、公开展示与相关 CMS 能力的 Consumer Repository。

## 当前目标

当前版本以原网站现有结构和视觉关系为主站公开基线，采用 Vue + Spring Boot 重建中心主站，并在同一公开站前端工程中开始建设具有独立红色视觉主题的“中心党建”站点边界。管理端已经完成独立前端工程与 Modular SPA 收敛；公开站下一阶段采用 **Multi-entry Modular SPA**：Entry 只按真实 Site / Theme Boundary 划分，中心主站与中心党建分别拥有独立 App、Router、Shell 和主题样式，但当前继续共用 `frontend/public-site` 工程、Vue/Vite 技术栈、构建发布链路和 Spring Boot CMS Backend。

本轮先完成公开站架构重构与中心党建基础页面框架，不在架构任务中提前发明党建完整栏目、内容、专属后台或最终视觉细节；这些内容在后续独立任务中依据新的 Requirement / Specification 和原站取证继续完善。当前不引入 Module Federation，也不把中心党建提前拆成独立前端工程；只有后续出现真实独立发布、独立部署、不同团队/技术栈或明显不同生命周期时再重新评估工程拆分。

当前权威需求：

- `docs/requirements/information-publishing.md` V4.7

当前 Specification：

- `docs/specifications/cms-core.md`
- `docs/specifications/public-site.md`
- `docs/specifications/admin-site.md`
- `docs/specifications/preset-site-structure.md`

当前 Architecture Decision：

- `docs/architecture/decisions/ADR-0001-admin-frontend-module-integration.md`
- `docs/architecture/decisions/ADR-0002-public-site-multi-entry-modular-spa.md`

当前 Technical Plan / Governance：

- `docs/technical/cms-architecture.md`
- `docs/technical/configuration-governance.md`
- `docs/technical/backend-service.md`
- `docs/technical/public-site-frontend.md`
- `docs/technical/admin-frontend.md`
- `docs/technical/admin-frontend-integration.md`
- `docs/technical/verification-strategy.md`
- `docs/technical/preset-site-structure.md`

当前公开站重构执行单元：

- `docs/work/public-site-multi-entry-execution-units.md`

最近完成的管理端执行单元（阶段追溯）：

- `docs/work/admin-frontend-convergence-execution-units.md`

管理端双前端拆分、通用 CMS 模型和 Admin Modular SPA 已完成当前阶段收敛，后续只按 Human Admin Review / 明确人工指令增量调整；当前主动路线已经切换到公开站 Multi-entry Modular SPA 与中心党建基础框架。

历史阶段文档继续保留用于追溯，但不再作为当前目标架构：

- `docs/specifications/center-main-site-core.md`
- `docs/specifications/admin-frontend-convergence.md`
- `docs/technical/center-main-site-core.md`
- `docs/technical/admin-frontend-convergence.md`
- `docs/work/center-main-site-core-execution-units.md`

项目演进状态：`docs/project/project-roadmap.md`。
项目本地开发方法：`docs/project/development-method.md`。

## Repository Authority

仓库工作首先遵循根目录 `AGENTS.md`。产品事实、当前范围、技术状态和验证结果以本 Consumer Repository 当前权威文件和可观察 GitHub / Runtime Evidence 为准。

`dygapp/agentic-dev` 提供可复用 AI 开发方法与 Skills；普通 Consumer 开发优先使用本仓库已经固化的项目本地规则。

## 当前 CMS 边界

CMS 通用业务对象：

- 栏目、文章；
- 单页、单页分组（技术层继续使用 `Page / PageGroup`）；
- 导航位置、多级导航及可选导航图标；
- 通用列表、列表项；
- 宣传展示位、展示内容；
- 网站属性；
- 网站静态资源。

管理端信息架构按“内容管理 / 内容结构 / 运营展示 / 站点设置”组织：文章、单页、列表属于主要内容管理；栏目和导航属于内容结构；宣传展示属于运营展示；网站属性和静态资源属于站点设置。具有明确“容器 → 成员”关系的管理页优先采用左侧选择组织上下文、右侧维护成员的交互。

公开站和管理端是同级独立 Vue / Vite 前端工程，共享 Spring Boot CMS Backend。公开站工程内部不再把普通页面类型机械映射成独立 HTML Entry；主站 `/`、`/column/**`、`/article/**`、`/page/**` 统一属于 Main Site Entry，中心党建使用独立 Party Building Site Entry 和 `/party/**` URL namespace。两个公开 Site 当前同构建、同部署，但 Shell、Router 与主题样式互不共享所有权。

管理端当前是单一 Vue SPA，但源码按 `app/`、`shared/` 与 `modules/cms/` 分离：Shell 只聚合模块声明，CMS Module 自己声明 routes/navigation，并使用 Vue Router 动态 import 进行路由级懒加载。Module Federation、iframe 或其他运行时微前端机制不属于当前基础设施；未来只有出现真实独立发布、独立部署、跨团队或跨技术栈要求时再单独评估。

公开站固定布局、Header/Footer、页面 Shell，以及基本不会变化的一次性集成可以作为工程资产。主站和中心党建的 Header、Footer、Navigation Layout、页面 Frame、颜色变量和主题 CSS 默认由各自 Site 持有，不为了形式复用强行进入 `shared/`；`shared/` 只承担明确无主题的 API transport、CMS DTO、资源 URL、SEO/通用工具等稳定技术能力。

首页 NCSS 区域属于主站固定工程集成，不要求后台管理。需要持续运营维护的数据优先使用 CMS 对象，避免在 SiteProperty JSON、导航和 Vue 常量中维护重复数据来源。中心党建后续优先评估复用现有 CMS 通用模型，不因公开前端存在独立 Site Entry 就预设新的党建专属 Admin Module。

通用列表只保存标题、副标题、图片、URL、打开方式、排序等数据属性；前台具体页面根据自身设计决定消费哪些属性以及如何展示，不由 CMS 列表定义控制视觉模式。导航条目可维护可选图标，避免前台按排序位置推导图标。

网站规划基线中的关键结构对象使用只读 `preset` 标识保护：预置栏目、导航位置/条目、单页分组/单页、列表容器、宣传展示位和稳定网站属性定义不能被误删；具有稳定 Alias/Code/Key 的预置对象不能修改该身份字段。`preset` 不等于完全只读，名称、排序、启停以及正常运营字段仍按各自模型维护；Article、CmsListItem、Advertisement 等运营成员不因此变成预置内容。普通 Admin API 新增对象默认 `preset=false`，客户端不能自行设置或取消该标识。

工程基线静态资源继续位于 `/static/home`、`/static/brand`、`/static/footer`、`/static/icons` 等版本化目录；CMS 运行时上传统一进入 `/static/uploads/**`，由宣传展示/列表/导航图标/RESOURCE_PATH 网站属性等管理界面复用统一图片资源选择与上传能力。Admin 中需要辨识图片内容的缩略图统一使用自适应背景，并复用 Element Plus 原生 Viewer 查看原图，不重复实现大图预览器。

静态资源“受保护”状态由 Backend 负责：固定部署/工程基线来自 Spring 外部化配置，当前网站属性、列表、宣传展示和导航直接引用的资源由 Runtime 动态加入保护集合；该状态不是管理员人工维护的重要性等级。普通删除必须拒绝，明确替换仍允许。

配置责任长期遵循 `docs/technical/configuration-governance.md`：稳定领域/安全/页面模板契约保留代码常量；运营可维护数据进入 CMS / 网站属性；低频结构定义进入 CMS 资源元数据；部署实例差异进入 Spring 外部化配置；CI、FRP 和 Review 环境参数属于 CI / Deployment Variables。存在字面硬编码本身不构成缺陷，禁止为了“消除硬编码”机械增加系统配置。

当前阶段明确不实现用户、账号、角色、登录和权限控制。未来“普通管理员 / 超级管理员”差异只作为规划边界，不进入当前代码和验收条件；`preset` 保护、删除确认、路径安全、真实媒体校验和受保护资源等业务安全措施仍继续执行。

## 前端工程

目标结构：

```text
frontend/
├── public-site/
│   └── src/
│       ├── shared/
│       └── sites/
│           ├── main/
│           │   ├── app/
│           │   ├── shell/
│           │   └── modules/
│           └── party-building/
│               ├── app/
│               ├── shell/
│               └── modules/
└── admin/
    └── src/
        ├── app/
        ├── shared/
        └── modules/cms/
```

- Main Public Site base：`/`
- Party Building Site base：`/party/`
- Admin Site base：`/admin/`
- CMS Admin canonical routes：`/admin/cms/**`
- Backend API：`/api/**`
- Public static assets：`/static/**`

## 验证原则

完成状态必须由与目标提交和具体 Evidence Claim 匹配的 Current Evidence 支持。Backend、Public Site、Admin Site、Integrated Browser 与 Review Environment 的实时结果由 GitHub Actions 保存；README 不复制具体 Run 编号。

公开站架构重构至少证明：主站既有 canonical URL 与视觉主基线无回归；`/page/**` 不再依赖无业务价值的独立 HTML Entry；主站 route-level lazy loading 可工作；`/party/` 能通过独立 Entry / Router / Shell 直接访问和刷新；主站与党建主题样式不互相污染；主导航“中心党建”从占位切换到党建入口；完整党建内容与最终视觉精度不由基础框架验证冒充完成。

祖先提交的 Runtime / Human Review Evidence 不机械继承。发生 CMS 模型、数据库、API 或公开站数据源/Entry/Router 调整后，应重新取得受影响 Evidence；Human Review 在自动验证和干净基线恢复后重新执行。