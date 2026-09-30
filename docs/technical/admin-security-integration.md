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
updated_at: 2026-09-30
---

# CMS 管理身份、授权与审计技术接缝

## 责任与实施状态

本文拥有第一版目标实现中跨 Admin API、身份来源、前端身份反馈和审计持续一致的技术接缝。当前 Server 已有统一主体、凭证验证器接缝、受信角色转换、Spring Security 请求 / 方法授权、管理写操作审计产生和持久化，以及与写入 contract 分离的 `super` 专属审计查询；没有正式身份提供方时 Admin fail closed。隔离 Review 身份只进入独立 Review source set / BootJar，正式 Server 产物不包含该入口。全局管理端身份反馈与可操作 Review 登录已实现，正式身份提供方仍未实现。角色的业务权限归属由 Requirement 持有，用户可观察结果由 Specification 持有，长期结构与框架选型由 Architecture / ADR 持有。下方矩阵只拥有访问与业务审计分类；HTTP 路径、方法及 wire compatibility 的唯一 owner 仍是 `docs/technical/http-interface-contract.md`，本文不复制其响应结构、Controller、SQL 或 Gradle dependency。

## 身份转换

`cms-server` 的认证边界先验证真实凭证的签发者、有效期、目标和完整性，再从可信结果构建统一主体 `CmsPrincipal(identitySource, userId, roles)`。来源标识与用户 ID 均不得由未验证的 HTTP 参数或普通客户端 Header 自行提供；无受信角色映射时 fail closed。映射的输入、可接受来源及变更方式由部署时受控配置确定，不能让公众可编辑的 CMS SiteProperty 持有安全策略。

Spring Security 的 `Authentication` 可承载该主体和映射后的 authorities。应用业务审计只消费 `CmsPrincipal`，不持有外部令牌或用户详情。身份验证方式按实际宿主协议选择：标准 OIDC / JWT 使用对应 Spring Security 能力，专有平台协议在认证适配器内验证；若独立网站未配置真实提供方，正式管理功能不得启动为匿名可写模式。

测试 / Review 身份提供方与其入口只在隔离的测试或 Review 构建中装配。生产配置和正式 BootJar 不包含可启用的模拟凭证默认值，并在没有正式身份适配器时失败关闭。不得使用 Spring Boot 默认随机用户、硬编码超级用户或客户端指定角色来替代测试身份协议。

隔离 Review Browser 登录使用 Server 控制的固定 `admin` / `super` profile 创建内存短期会话。Browser 只提交稳定 profile token，不能提交 user ID、identity source 或角色集合；Server 返回随机 opaque credential，Frontend 仅在当前 tab 的 `sessionStorage` 保存并通过 `X-Cms-Review-Credential` 发送。会话有受控到期时间、最大并发数量和过期清理，退出 / 主动模拟失效立即从 Server registry 删除。Runtime restart 清空全部短期会话；该 registry 不构成账号库、正式 session store 或生产身份实现。原生 `<img>` 无法附加认证 Header，Review 登录同时设置只对 `/api/admin/resources/*/content` 生效的 `HttpOnly`、`SameSite=Strict` 预览 Cookie；Review filter 只允许该 Cookie 认证匹配的 GET 内容请求，任何写操作仍必须提供 Header credential。

Review 自动化 credential 与人工短期会话由同一 Review verifier 转换为 `CmsPrincipal`，但必须分别支持 `admin` 和 `super`，不能继续依赖代理层为所有 Browser 请求静默注入单一超级身份。Review runtime marker 只控制 Admin 是否展示测试入口，不构成 Backend 信任；正式 Server 即使收到 marker 或 Review Header 也仍然失败关闭。

Review 会话的受控配置为 `cms.review-identity.session-ttl`（默认 `PT30M`，大于零且不超过 24 小时）和 `cms.review-identity.max-sessions`（默认 256，范围 1～10000）；双角色自动化 token 必须显式配置且互不相同。创建时清理过期会话，超过容量拒绝创建；验证在到期边界拒绝旧凭证，退出 / 模拟失效不撤销配置的自动化 token。

当前基础接缝由受信的 Server verifier 返回已验证用户 ID 和外部角色；来源标识取自该 verifier，而不是请求提供的来源值。Server 按来源使用受控角色映射，缺少映射、存在未知角色或没有允许角色时拒绝主体构建。隔离测试 verifier 与 Review HTTP 适配分别位于 test / review source set，正式 Server 产物不包含它们；正式适配器尚未接入时，普通 Admin HTTP 请求保持未认证并返回 `401`。

## 授权接缝

`SecurityFilterChain` 显式区分 `/api/admin/**`、`/api/public/**`、公开静态资源及其他请求。管理 API 先要求认证，再执行方法级角色 / 操作授权；公开读取维持现行契约，未分类请求不隐式开放。`@EnableMethodSecurity` 必须显式启用，管理操作可使用 `@PreAuthorize` 或等价 `AuthorizationManager`，并以 CMS 自己的权限目录作为角色授予的唯一实现输入。

第一版 `admin` 覆盖现有全部 CMS 管理业务，`super` 继承这些能力并可查询审计。角色授予只决定是否可尝试操作，不跳过 Core 的 Domain 校验。授权切点应位于 Server 的 Admin application / transport 边界；Content Migration 命令调用 shared Core 时不需要伪造 HTTP 用户或绕过 Security 注解。

管理端 Browser route 防护与菜单可见性只反映后端结果，不承担最终授权。管理 API 未认证与已认证无权限分别形成 `401` / `403`；审计查询接口的 HTTP 路径与响应契约由 `docs/technical/http-interface-contract.md` 正式拥有，本文只约束其安全与集成接缝。

Admin Frontend 使用单一状态化身份边界和共享 Admin HTTP adapter：启动时先查询当前可信主体，成功后才挂载管理工作区；任一 Admin 请求 `401` 都清除 Review 短期 credential 并切换为未认证 / 已失效状态，`403` 保留当前主体并发布统一禁止访问反馈。所有既有 JSON、multipart、binary Admin adapter 都必须经过该边界；Public 请求不附带 Review credential，也不被 Admin 身份状态拦截。导航依据 Server 返回的 CMS role 裁剪，但 direct route 和 API 继续依赖 Server 授权。

共享 adapter 只向同源 `/api/admin/**` 附加当前 tab 的 Review credential，并拒绝重定向以避免凭证跨源转发；旧会话的迟到响应不影响新会话。退出网络失败仍清除页面凭证，但必须明确提示服务端会话失效未获确认、将按 TTL 到期，不得显示“已退出”冒充服务端成功。

## 接口权限矩阵（第一版目标）

下表以 HTTP contract 当前 Admin family 为授权匹配键；路径均相对于 `/api/admin`。每个列出的读、写入口都只允许 `admin` 或 `super` 尝试，且两角色都必须继续通过原有 Domain 校验。无有效身份返回 `401`，已认证但无 CMS 允许角色返回 `403`；拒绝时不得返回管理数据或产生业务写入。`super` 不因角色较高而绕过 preset、发布前置条件、来源身份或资源安全约束。

| 业务族 | 读操作 | 写操作（均纳入业务审计） |
|---|---|---|
| 当前主体 | `GET /identity` | 无 |
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
| 操作审计 | `GET /audit-events`、`GET /audit-events/{auditId}`（仅 `super`） | 无 |

业务审计覆盖上述全部写入口，包括上传、替换、软删除、恢复及文章发布 / 撤回；授权拒绝记为安全事件，不伪造成已执行的业务写操作。当前没有托管资源删除接口，不为矩阵补造。操作审计行已实现独立读取边界：无有效身份 `401`，`admin` 或其他已认证非 `super` 角色 `403`，`super` 只可按 HTTP contract 有界查询；它是只读入口，不进入业务写操作 descriptor inventory。

管理端现有八类日常业务 Browser 入口及独立的安全审计入口如下；`/admin/articles` 等旧路径只重定向到对应 `/admin/cms/**`，不得绕过同一管理身份状态。

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
| `/admin/cms/audit` | 操作审计（仅 `super`） |

匿名可进入待登录 / 无法认证展示，但不得读取管理数据；无允许角色展示禁止访问；`admin`、`super` 可进入现有业务页面，只有 `super` 可进入并读取操作审计页面。直接访问、刷新、身份失效和页面内 API 请求必须同样受控，前端隐藏菜单不能替代服务端拒绝。

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

### 事件模型与数据边界

独立 Spring AOP 切面围绕 Server 管理写操作提取当前 `CmsPrincipal`、业务动作与对象定位。每次准备执行的写操作先形成具有独立 ID 的审计尝试，至少保存：`identitySource`、`userId`、操作时角色快照、稳定动作 token、对象类型、可用的对象标识、开始 / 完成时间、结果及 Server 生成或受控归一化的请求关联标识。

结果生命周期使用 `STARTED`、`SUCCEEDED`、`FAILED`、`ROLLED_BACK`。只有已经与业务提交共同完成的记录可以标记为 `SUCCEEDED`；Domain / input 失败标记为 `FAILED`，事务提交失败或显式回滚标记为 `ROLLED_BACK`。无法安全完成最终结果持久化时保留 `STARTED`，它表示结果未确认，不得被查询层或运维解释为成功。认证 / 授权阶段的 `401 / 403` 拒绝发生在业务切面之前，属于安全事件，不创建伪造的业务操作审计记录。

持久化不保存外部令牌、口令、完整请求体、文件内容、Rich Text 正文、异常堆栈或任意异常消息。可选失败分类只能来自受控枚举；对象标识只保存追溯所需的 ID、稳定 code / key 或受控资源 path，不复制对象正文。历史角色快照不通过当前主体或用户详情回填。

### 事务与审计写入失败策略

执行管理写操作前，以独立事务持久化 `STARTED`；该写入失败时拒绝执行业务操作并返回失败，不能在没有审计尝试记录的情况下继续。随后由 Server 审计边界开启覆盖实际管理入口的外层事务，现有 Core `@Transactional` 写服务加入该事务；数据库业务变化与 `SUCCEEDED` 终态更新共同提交，任一写入或提交失败都不得留下成功记录。

业务方法抛出受控失败时，先结束 / 回滚业务事务，再以独立事务把原审计尝试终结为 `FAILED`；事务提交失败、rollback-only 或提交阶段异常终结为 `ROLLED_BACK`。失败终态写入自身失败时保留原 `STARTED`，继续传播原业务 / 提交异常，并用审计 ID 与请求关联标识记录可诊断的运行错误；不得用审计异常覆盖原始失败或把结果改成成功。

文件系统写操作同样必须先有 `STARTED`。Managed Resource 与 Static Resource 的上传、替换、软删除和恢复在最终 `SUCCEEDED` 之前使用明确的暂存 / 补偿边界：可逆失败恢复原可见状态；进程或 I/O 故障导致结果无法确定时保留 `STARTED` 并向调用方返回失败，不伪造 `SUCCEEDED`。该策略不声称文件系统与数据库形成分布式原子事务，而是保证成功声明有证据、失败不被误记成功且不确定结果仍可追溯。

### 切点、覆盖与查询边界

审计注解 / descriptor 只声明稳定动作、对象类型与受控对象定位规则；身份、角色、时间、结果和关联标识由可信 Server 基础设施生成，调用方不能通过请求体或普通 Header 伪造。切点不得依赖类内自调用触发；Kotlin final / AOP proxy、返回值对象 ID、无返回值删除、异常以及事务 advice 顺序均须用实际管理入口验证。

实际注册的全部 Admin 写 handler 必须与审计 descriptor inventory 双向一致，覆盖栏目、文章、导航位置、导航条目、单页分组、单页、列表定义、列表项、广告位、广告项、网站属性、托管资源及静态资源；新增写 handler 未声明审计时验证必须失败。Content Migration 继续不构造 `CmsPrincipal`，也不进入 Server 业务审计切面。

审计查询使用与 `AdminAuditPersistence` 分离的 framework-neutral read contract，避免普通业务写服务取得审计记录 mutation 能力。数据集合持续增长，查询按 `startedAt + auditId` 稳定倒序并使用 opaque cursor、`limit + 1` 判定下一页，不执行全量读取或 total count；持久化索引必须支持无过滤时间顺序以及操作者、动作、对象、结果、请求关联标识等已声明过滤维度。角色快照只对当前页 / 单条详情批量读取，避免逐行查询。

Server 只在 Admin transport 层公开 `docs/technical/http-interface-contract.md` 拥有的 query / detail surface，并以 `cms:super` 方法授权独立保护；请求级认证仍由统一 `/api/admin/**` 边界承担。查询 handler 不携带 `AdminAuditOperation`，读取行为不产生新的业务写审计。Admin 页面仅渲染持久化 projection、cursor 翻页和页面自身的 `401 / 403 / failure` 状态，不通过查询当前业务对象或用户目录“补全”历史。

隔离 Review 构建必须提供可验证 `super` 查询路径，以完成真实 Browser / Runtime 证据，但该能力仍只能存在于 review source set / Review BootJar，不能进入正式产物或替代真实身份提供方。第一版保留期限、导出、外部审计平台、篡改防护增强和自动处理长期 `STARTED` 记录仍属于后续独立要求。

## 验证与切换

实施需建立管理接口权限矩阵与自动化测试：逐类读 / 写入口、无身份、无允许角色、`admin`、`super`、两来源相同用户 ID、跨来源角色映射、凭证过期、业务校验失败、事务回滚、审计写入失败、Public 回归及正式配置排除测试入口。Browser 测试覆盖管理页面直接访问、刷新、过期与拒绝状态。

只有受影响的后端、接口、前端及运行时证据对应同一目标提交，才能声明该目标已经保护 Admin API；文档先行不构成现有系统安全已升级的证明。
