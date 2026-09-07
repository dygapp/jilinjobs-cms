# 中心主站站点收敛技术计划（Technical Plan）

## 1. 目的

本文把 `docs/specifications/center-main-site-core.md` 当前站点收敛规格映射到跨执行单元具有长期价值的 HOW。

当前继续使用 Vue 3 + TypeScript + Vite + Vue Router、Element Plus、Spring Boot 模块化单体、Kotlin / Java 21 / Gradle、MySQL + MyBatis、本地文件系统和 REST API。

不引入 MQ、Redis、MinIO、SSR/SSG 或新的认证系统。

## 2. 总体架构

后端增加 `page`（固定页面与页面组）、`siteconfig`（网站配置）、`staticresource`（网站静态目录管理）。已有 `column`、`navigation`、`content`、`resource` 继续保留原边界。

当前实现阶段前端仍为一个 Vite 工程，公开端按“可演进多 Shell”组织：默认/Home Shell、Page Shell，后续按需要增加 Guide / Jobs / 页面专用 Shell；同一工程当前也承载 `/admin/**` 原型管理端路由。

第一版允许 `page.html` 统一处理 `/page/**`，不要求立即创建所有专用 Entry。

> Roadmap 已明确下一阶段将公开网站与管理端拆分为两个独立前端工程。本文以下内容记录 PR #18 收口时的现状技术基线；前端物理拆分后的目录、构建、共享代码和部署边界应在下一阶段重新形成 Technical Plan，不应直接把本节“一个 Vite 工程”的现状描述当作长期目标架构。

## 3. URL 与前端 Entry

### 3.1 Canonical URL

```text
/                         首页
/column/{alias}           栏目
/article/{id}             文章
/page/{alias}             普通固定页
/page/{group}/{alias}     页面组固定页
/static/**                网站静态资源
/assets/**                前端构建资源
/api/**                   后端 API
/admin/**                 当前原型管理端
```

### 3.2 Shell fallback

页面 URL 与 HTML Entry 解耦。长期规则：

```text
/page/{group}/{page}
→ 最具体页面 Shell（如存在）
→ group Shell（如存在）
→ page Shell
→ 默认 Shell
```

第一版实现可以让 Nginx 对 `/page/**` 统一 fallback `page.html`；后续新增 `guide.html` / `jobs.html` 不改变公开 URL。

Nginx 不实现无限层级递归查找；当前公开页面业务层级控制在 `/page/{page}` 与 `/page/{group}/{page}` 两级。

## 4. 数据模型

### 4.1 栏目

`cms_column` 增加稳定 `alias`，唯一约束。公开查询支持按 alias 获取。

### 4.2 固定页面组

新增 `cms_page_group`：id、alias、name、sort_order、enabled。

### 4.3 固定页面

新增 `cms_page`：id、group_id（可空）、alias、name、body_html、render_mode、embed_url（可空）、sort_order、enabled、timestamps。

同一 group 内 alias 唯一；无 group 的普通固定页 alias 全局唯一。

`render_mode` 第一版包含 `RICH_TEXT`、`EMBED_PLACEHOLDER`、`INTERNAL_STATIC`。第一版完整实现 RICH_TEXT 与 EMBED_PLACEHOLDER； INTERNAL_STATIC 保留工程接缝，不允许后台上传 HTML/JS。

### 4.4 导航

`cms_navigation` 增加 `parent_id`、`target_page_id`、`open_mode`。

目标类型扩展为 HOME、COLUMN、PAGE、LINK、PLACEHOLDER。

`open_mode`：DEFAULT、SAME_WINDOW、NEW_WINDOW。

DEFAULT 解析：外部 LINK → NEW_WINDOW；其他本站目标 → SAME_WINDOW。

### 4.5 网站配置

新增 `cms_site_config`：config_key、config_value、value_type、description、updated_at。

表结构可以通用，但允许的 key 由后端注册表/枚举定义，管理 API 只暴露已定义配置项，避免变成自由配置中心。

链接组第一版可以使用 JSON 值承载结构化数组，后端校验基本格式；后续需要复杂查询时再拆表。

## 5. 初始化数据

新增 Flyway migration，不修改 V1～V3。

迁移负责 column alias、新页面/页面组/配置表、导航扩展，以及插入确认的栏目、页面组、固定页面、导航、站点配置。

初始化 SQL 使用稳定 alias 和可阅读的显式 INSERT。

初始化资源文件不写入 SQL。前端仓库或受控资源目录提供少量默认静态资源；部署/Review Environment 将其复制到 `CMS_STATIC_ROOT`。

## 6. 网站静态资源

新增配置：

```text
cms.static.root=${CMS_STATIC_ROOT:./data/static}
```

回收区为 `${CMS_STATIC_ROOT}/.trash`。

管理 API 每次读取实际目录，而不是依赖数据库登记，因此人工复制进去的文件也可见。第一版不建立静态资源表。

所有管理路径必须为相对路径，normalize 后仍位于 static root 内，禁止 `..` 目录穿越；上传只允许白名单静态类型；禁止 HTML、JS 等可执行页面资源上传；文件大小遵循当前 multipart 限制。

删除将文件移动到 `.trash`；恢复时目标已存在则返回冲突。永久删除不是第一版常用操作，可不提供。

## 7. 页面上下文与面包屑

后端公开查询提供同构页面上下文：canonicalUrl、pageType、title、breadcrumbs、navigation/page-group context。

面包屑由业务关系计算，不从 URL 拆分。第一版可以分别在 column/article/page 查询中生成同构 DTO，不要求立即抽象复杂公共框架。

## 8. 首页

首页模板由前端代码固定。公开 API 提供主导航树、栏目/文章数据、页面组快捷入口和网站配置。首页不建复制内容表。

视觉实现优先复刻原网站主要结构。无法自动采集的图片/尺寸细节使用已确认素材和合理默认值，进入人工 Review Environment 后再微调。

## 9. 前端页面组织

PR #18 收口时，前端仍在单一 `frontend` Vite 工程中同时承载公开站点和管理端：公开视图位于 `views/public`，管理视图位于 `views/admin`，统一 Router 同时注册公开路由和 `/admin/**` 路由。

公开端已增加 Page 入口和共享公共组件；现有公开视图包括首页、栏目、文章、固定页面 / 页面组以及外部嵌入占位区域。

现有管理视图包括栏目、菜单、文章、固定页面、网站配置、网站静态资源等管理页面。

下一阶段按 Roadmap 执行前端工程物理拆分：公开站点与管理端分别形成独立前端工程、构建产物、路由和测试入口。共享类型/API 客户端/通用组件是否抽取为共享包，应依据实际重复度和部署边界决定，避免在拆分前过早抽象。

## 10. API

新增：

```text
GET    /api/public/pages/{alias}
GET    /api/public/page-groups/{group}/{alias}
GET    /api/public/page-groups/{group}

GET    /api/admin/pages
POST   /api/admin/pages
PUT    /api/admin/pages/{id}
DELETE /api/admin/pages/{id}

GET    /api/admin/page-groups
POST   /api/admin/page-groups
PUT    /api/admin/page-groups/{id}

GET    /api/admin/site-config
PUT    /api/admin/site-config/{key}

GET    /api/admin/static-resources
POST   /api/admin/static-resources
DELETE /api/admin/static-resources
GET    /api/admin/static-resources/trash
POST   /api/admin/static-resources/restore
```

现有 `/api/public/columns/{id}` 可保留兼容，但新增 alias 查询并让新前端使用 alias。

## 11. 测试策略

Backend：Flyway V4 在 MySQL 上通过；column alias 唯一；page group/page alias 校验；页面公开过滤；导航 target/open mode；site config key 白名单；static path traversal；upload type；trash/restore；既有文章发布状态回归。

Frontend：`vue-tsc`、Vite 多入口 build、`/column/{alias}`、`/article/{id}`、`/page/about`、`/page/guide/dagl`、guide Tab、jobs placeholder、管理端 Page / Config / Static Resource 主流程。

E2E：Review Environment 不再依赖测试脚本创建栏目/主菜单/页面组；Playwright 验证 Flyway 初始化基线已经存在；测试只创建动态文章等场景数据；完成后上传 Playwright evidence。

下一阶段拆分公开站点 / 管理端工程后，应分别建立前端构建与 Browser Verification，同时保留必要的端到端跨边界验证；不得因物理拆分而降低现有公开站点回归覆盖。

## 12. 风险

1. 多入口 Shell 过度设计：第一版只建立 `page.html` 必要接缝，专用 Shell 按真实需求增量加入。
2. 静态资源误删：通过高风险入口、警告、回收区降低风险，不建设虚假的完整引用检测。
3. 现网视觉自动复刻不完整：视觉精确值作为人工 Review Environment 校正项，不阻塞结构和数据模型收敛。
4. 初始化数据可维护性：稳定 alias 与 Flyway 版本化，禁止依赖自增 ID 作为长期外部契约。
5. 原型无认证：高风险权限仅固化要求和入口边界，正式系统接入统一权限后实施角色限制。
6. 前端工程拆分引入重复与漂移：下一阶段先明确公开端 / 管理端职责边界、共享代码最小集合和独立验证策略，再实施物理迁移；不得通过复制两套 API 模型和基础类型形成长期分叉。
