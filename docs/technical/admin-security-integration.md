---
id: technical:admin-security-integration
type: technical-contract
status: active
relations:
  requirements:
    - docs/requirements/cms-admin-identity-and-audit.md
  specifications:
    - docs/specifications/admin-access-audit.md
  architecture:
    - docs/architecture/cms-architecture.md
    - docs/architecture/decisions/ADR-0005-admin-identity-authorization-audit.md
  interface:
    - docs/technical/http-interface-contract.md
updated_at: 2026-09-29
---

# CMS 管理身份、授权与审计技术接缝

## 责任与实施状态

本文拥有第一版目标实现中跨 Admin API、身份来源和审计持续一致的技术接缝。当前 Server 已有统一主体、凭证验证器接缝、受信角色转换以及 Spring Security 请求 / 方法授权基础；没有正式身份提供方时 Admin fail closed。隔离 Review 身份只进入独立 Review source set / BootJar，正式 Server 产物不包含该入口。正式身份提供方、管理端身份反馈和业务操作审计仍未实现。角色的业务权限归属由 Requirement 持有，用户可观察结果由 Specification 持有，长期结构与框架选型由 Architecture / ADR 持有。下方矩阵只拥有访问与业务审计分类；HTTP 路径、方法及 wire compatibility 的唯一 owner 仍是 `docs/technical/http-interface-contract.md`，本文不复制其响应结构、Controller、SQL 或 Gradle dependency。

## 身份转换

`cms-server` 的认证边界先验证真实凭证的签发者、有效期、目标和完整性，再从可信结果构建统一主体 `CmsPrincipal(identitySource, userId, roles)`。来源标识与用户 ID 均不得由未验证的 HTTP 参数或普通客户端 Header 自行提供；无受信角色映射时 fail closed。映射的输入、可接受来源及变更方式由部署时受控配置确定，不能让公众可编辑的 CMS SiteProperty 持有安全策略。

Spring Security 的 `Authentication` 可承载该主体和映射后的 authorities。应用业务审计只消费 `CmsPrincipal`，不持有外部令牌或用户详情。身份验证方式按实际宿主协议选择：标准 OIDC / JWT 使用对应 Spring Security 能力，专有平台协议在认证适配器内验证；若独立网站未配置真实提供方，正式管理功能不得启动为匿名可写模式。

测试 / Review 身份提供方与其入口只在隔离的测试或 Review 构建中装配。生产配置和正式 BootJar 不包含可启用的模拟凭证默认值，并在没有正式身份适配器时失败关闭。不得使用 Spring Boot 默认随机用户、硬编码超级用户或客户端指定角色来替代测试身份协议。

当前基础接缝由受信的 Server verifier 返回已验证用户 ID 和外部角色；来源标识取自该 verifier，而不是请求提供的来源值。Server 按来源使用受控角色映射，缺少映射、存在未知角色或没有允许角色时拒绝主体构建。隔离测试 verifier 与 Review HTTP 适配分别位于 test / review source set，正式 Server 产物不包含它们；正式适配器尚未接入时，普通 Admin HTTP 请求保持未认证并返回 `401`。

## 授权接缝

`SecurityFilterChain` 显式区分 `/api/admin/**`、`/api/public/**`、公开静态资源及其他请求。管理 API 先要求认证，再执行方法级角色 / 操作授权；公开读取维持现行契约，未分类请求不隐式开放。`@EnableMethodSecurity` 必须显式启用，管理操作可使用 `@PreAuthorize` 或等价 `AuthorizationManager`，并以 CMS 自己的权限目录作为角色授予的唯一实现输入。

第一版 `admin` 覆盖现有全部 CMS 管理业务，`super` 继承这些能力并可查询审计。角色授予只决定是否可尝试操作，不跳过 Core 的 Domain 校验。授权切点应位于 Server 的 Admin application / transport 边界；Content Migration 命令调用 shared Core 时不需要伪造 HTTP 用户或绕过 Security 注解。

管理端 Browser route 防护与菜单可见性只反映后端结果，不承担最终授权。管理 API 未认证与已认证无权限分别形成 `401` / `403`；若增加新的审计查询接口，应由 `docs/technical/http-interface-contract.md` 正式拥有 HTTP 路径与响应契约，而非在本文发明 endpoint。

## 接口权限矩阵（第一版目标）

下表以 HTTP contract 当前 Admin family 为授权匹配键；路径均相对于 `/api/admin`。每个列出的读、写入口都只允许 `admin` 或 `super` 尝试，且两角色都必须继续通过原有 Domain 校验。无有效身份返回 `401`，已认证但无 CMS 允许角色返回 `403`；拒绝时不得返回管理数据或产生业务写入。`super` 不因角色较高而绕过 preset、发布前置条件、来源身份或资源安全约束。

| 业务族 | 读操作 | 写操作（均纳入业务审计） |
|---|---|---|
| 栏目 | `GET /columns` | `POST /columns`；`PUT/DELETE /columns/{id}` |
| 文章 | `GET /articles`；`GET /articles/{id}` | `POST /articles`；`PUT /articles/{id}`；`POST /articles/{id}/publish`、`/withdraw` |
| 导航位置 | `GET /navigation-locations` | `POST /navigation-locations`；`PUT/DELETE /navigation-locations/{code}` |
| 导航条目 | `GET /navigations` | `POST /navigations`；`PUT/DELETE /navigations/{id}` |
| 单页分组 | `GET /page-groups` | `POST /page-groups`；`PUT /page-groups/{id}` |
| 单页 | `GET /pages` | `POST /pages`；`PUT/DELETE /pages/{id}` |
| 列表定义 | `GET /lists` | `POST /lists`；`PUT/DELETE /lists/{id}` |
| 列表项 | `GET /lists/{id}/items` | `POST /lists/{id}/items`；`PUT/DELETE /lists/{listId}/items/{itemId}` |
| 广告位 | `GET /advertisements/slots` | `POST /advertisements/slots`；`PUT/DELETE /advertisements/slots/{id}` |
| 广告项 | `GET /advertisements/slots/{id}/items` | `POST /advertisements/slots/{id}/items`；`PUT/DELETE /advertisements/slots/{slotId}/items/{adId}` |
| 网站属性 | `GET /site-config`、`/site-config/groups` | `POST /site-config`；`PUT /site-config/{key}`、`/site-config/{key}/definition`；`DELETE /site-config/{key}` |
| 托管资源 | `GET /resources/{id}`、`/resources/{id}/content` | `POST /resources`（multipart 上传） |
| 静态资源 | `GET /static-resources`、`/static-resources/trash` | `POST /static-resources`（上传或明确替换）；`DELETE /static-resources`（入回收区）；`POST /static-resources/restore/{id}` |

业务审计覆盖上述全部写入口，包括上传、替换、软删除、恢复及文章发布 / 撤回；授权拒绝记为安全事件，不伪造成已执行的业务写操作。当前没有托管资源删除接口，不为矩阵补造。审计查询是未来独立的 `super` 专属只读入口：无有效身份 `401`，其他已认证角色 `403`，`super` 只可有界分页查询；在 HTTP contract 正式确定路径和投影前，不把它计入当前 endpoint 清单。

管理端现有八类 Browser 入口及其主要 Admin API consumer 如下；`/admin/articles` 等旧路径只重定向到对应 `/admin/cms/**`，不得绕过同一管理身份状态。

| 页面路由 | 主要 Admin API 族 |
|---|---|
| `/admin/cms/articles` | 文章、栏目、托管资源 |
| `/admin/cms/pages` | 单页、单页分组、托管资源 |
| `/admin/cms/lists` | 列表定义、列表项、文章、托管资源 |
| `/admin/cms/columns` | 栏目 |
| `/admin/cms/navigation` | 导航位置、导航条目、静态资源选择 |
| `/admin/cms/advertisements` | 广告位、广告项、静态资源选择 |
| `/admin/cms/site-config` | 网站属性、静态资源选择 |
| `/admin/cms/static-resources` | 静态资源 |

匿名可进入待登录 / 无法认证展示，但不得读取管理数据；无允许角色展示禁止访问；`admin`、`super` 可进入现有业务页面。当前没有审计查询页面。直接访问、刷新、身份失效和页面内 API 请求必须同样受控，前端隐藏菜单不能替代服务端拒绝。

Public 边界以下 17 个当前 GET 投影保持匿名可读；它们不获得 Admin 写入或完整管理数据。

| 公开族 | 匿名可读入口 |
|---|---|
| 栏目 | `/api/public/columns/{id}`、`/api/public/columns/by-alias/{alias}` |
| 文章 | `/api/public/articles`、`/api/public/articles/{id}` |
| 导航 | `/api/public/navigations` |
| 单页 | `/api/public/pages/{alias}`、`/api/public/page-groups/{groupAlias}`、`/api/public/page-groups/{groupAlias}/{alias}` |
| 列表 | `/api/public/lists`、`/api/public/lists/by-code/{code}`、`/api/public/lists/by-group/{groupCode}` |
| 宣传展示 | `/api/public/advertisements`、`/api/public/advertisements/slots/{code}` |
| 网站属性 | `/api/public/site-config` |
| 托管资源 | `/api/public/resources/{id}/content`、`/api/public/resources/{id}/attachment` |
| 静态资源 | `/static/**` |

其他 HTTP 方法和未分类路径不因属于 `/api/public/**` 或 `/static/**` 而自动开放。管理端的资源预览 `GET /api/admin/resources/{id}/content` 仍需管理身份，公开正文中的图片须由 Public projection 转为公开资源 URL，不能依赖 Admin 路径。

Content Migration 是独立 non-web application，不经过 Admin HTTP 身份，也不伪造管理主体；其既有受控导入、报告和 Core Domain 约束继续适用。授权切点位于 Server 的管理入口，不能通过给 shared Core service 添加请求身份依赖或角色注解来封锁 Migration。Core 中原有 HTTP Controller 已归位 Server，Core / Migration 的上传输入已退出 multipart transport 依赖；后续方法授权仍须按 Server 实际注册路由逐一核对，不能因为类所在模块或 URL 前缀推测该入口已经安全。

矩阵的覆盖验证以 Server 实际注册的全部 handler method 与 HTTP contract 双向比对：每条 Admin 映射恰好归入一行，新增或迁移入口不得漏出；现有 13 个业务族的读、写及资源特殊动作分别验证匿名、无允许角色、`admin`、`super` 的结果，另验证过期身份、业务校验失败与无副作用拒绝。Public GET 与 `/static/**` 回归匿名读取和 Admin 数据隔离；Migration 以 non-web 启动及无 Server HTTP transport 依赖验证。此处的目标矩阵不构成当前 Runtime 已受保护的证据。

## 操作审计接缝

独立 Spring AOP 切面围绕 Server 管理写操作提取业务动作、对象定位与当前主体；持久化只记录必要字段：`identitySource`、`userId`、角色快照、动作、对象、时间、结果、请求关联标识。不记录令牌、口令、完整请求体或 Rich Text 正文。

成功审计与业务事务的提交结果必须一致。实施时明确事务边界，确保业务提交成功却丢失成功审计不会被静默视为完成；失败 / 回滚的记录若需独立持久化，应与已回滚业务事务隔离并正确标记失败。认证 / 授权阶段的拒绝作为安全事件记录，不伪造成已执行写操作。

切点不得依赖类内自调用触发；Kotlin final / AOP proxy、异常、返回值和事务 advice 顺序均须用实际管理入口验证。审计查询必须分页 / 有界过滤并仅对 `super` 开放；具体存储和生命周期由实施时的 schema 与运维约束细化。

## 验证与切换

实施需建立管理接口权限矩阵与自动化测试：逐类读 / 写入口、无身份、无允许角色、`admin`、`super`、两来源相同用户 ID、跨来源角色映射、凭证过期、业务校验失败、事务回滚、审计写入失败、Public 回归及正式配置排除测试入口。Browser 测试覆盖管理页面直接访问、刷新、过期与拒绝状态。

只有受影响的后端、接口、前端及运行时证据对应同一目标提交，才能声明该目标已经保护 Admin API；文档先行不构成现有系统安全已升级的证明。
