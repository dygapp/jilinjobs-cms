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

本文拥有第一版目标实现中跨 Admin API、身份来源和审计持续一致的技术接缝；当前实现尚未落地。角色的业务权限归属由 Requirement 持有，用户可观察结果由 Specification 持有，长期结构与框架选型由 Architecture / ADR 持有。本文不复制 Controller、SQL、Gradle dependency 或每个 endpoint 的第二清单。

## 身份转换

`cms-server` 的认证边界先验证真实凭证的签发者、有效期、目标和完整性，再从可信结果构建统一主体 `CmsPrincipal(identitySource, userId, roles)`。来源标识与用户 ID 均不得由未验证的 HTTP 参数或普通客户端 Header 自行提供；无受信角色映射时 fail closed。映射的输入、可接受来源及变更方式由部署时受控配置确定，不能让公众可编辑的 CMS SiteProperty 持有安全策略。

Spring Security 的 `Authentication` 可承载该主体和映射后的 authorities。应用业务审计只消费 `CmsPrincipal`，不持有外部令牌或用户详情。身份验证方式按实际宿主协议选择：标准 OIDC / JWT 使用对应 Spring Security 能力，专有平台协议在认证适配器内验证；若独立网站未配置真实提供方，正式管理功能不得启动为匿名可写模式。

测试身份提供方与其入口只在隔离的测试配置下装配。生产配置不包含可启用的模拟凭证默认值，并在错误配置时失败关闭。不得使用 Spring Boot 默认随机用户、硬编码超级用户或客户端指定角色来替代测试身份协议。

## 授权接缝

`SecurityFilterChain` 显式区分 `/api/admin/**`、`/api/public/**`、公开静态资源及其他请求。管理 API 先要求认证，再执行方法级角色 / 操作授权；公开读取维持现行契约，未分类请求不隐式开放。`@EnableMethodSecurity` 必须显式启用，管理操作可使用 `@PreAuthorize` 或等价 `AuthorizationManager`，并以 CMS 自己的权限目录作为角色授予的唯一实现输入。

第一版 `admin` 覆盖现有全部 CMS 管理业务，`super` 继承这些能力并可查询审计。角色授予只决定是否可尝试操作，不跳过 Core 的 Domain 校验。授权切点应位于 Server 的 Admin application / transport 边界；Content Migration 命令调用 shared Core 时不需要伪造 HTTP 用户或绕过 Security 注解。

管理端 Browser route 防护与菜单可见性只反映后端结果，不承担最终授权。管理 API 未认证与已认证无权限分别形成 `401` / `403`；若增加新的审计查询接口，应由 `docs/technical/http-interface-contract.md` 正式拥有 HTTP 路径与响应契约，而非在本文发明 endpoint。

## 操作审计接缝

独立 Spring AOP 切面围绕 Server 管理写操作提取业务动作、对象定位与当前主体；持久化只记录必要字段：`identitySource`、`userId`、角色快照、动作、对象、时间、结果、请求关联标识。不记录令牌、口令、完整请求体或 Rich Text 正文。

成功审计与业务事务的提交结果必须一致。实施时明确事务边界，确保业务提交成功却丢失成功审计不会被静默视为完成；失败 / 回滚的记录若需独立持久化，应与已回滚业务事务隔离并正确标记失败。认证 / 授权阶段的拒绝作为安全事件记录，不伪造成已执行写操作。

切点不得依赖类内自调用触发；Kotlin final / AOP proxy、异常、返回值和事务 advice 顺序均须用实际管理入口验证。审计查询必须分页 / 有界过滤并仅对 `super` 开放；具体存储和生命周期由实施时的 schema 与运维约束细化。

## 验证与切换

实施需建立管理接口权限矩阵与自动化测试：逐类读 / 写入口、无身份、无允许角色、`admin`、`super`、两来源相同用户 ID、跨来源角色映射、凭证过期、业务校验失败、事务回滚、审计写入失败、Public 回归及正式配置排除测试入口。Browser 测试覆盖管理页面直接访问、刷新、过期与拒绝状态。

只有受影响的后端、接口、前端及运行时证据对应同一目标提交，才能声明该目标已经保护 Admin API；文档先行不构成现有系统安全已升级的证明。
