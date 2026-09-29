---
id: ADR-0005
type: architecture-decision
status: accepted
date: 2026-09-29
scope: CMS 管理访问与操作审计
---

# ADR-0005：CMS 管理身份转换、方法授权与操作审计边界

## 背景

当前 CMS Server 同时暴露 Admin 和 Public API，管理端已是模块化 SPA，但 Runtime 尚没有统一认证授权。CMS 需要同时接入未来智慧就业平台自己的管理后台，也能服务独立网站。采用宿主的用户表或整个若依 / JEECG 后台作为 CMS 内部依赖，会把 CMS 业务授权绑到某个外部产品；自建完整用户管理又超出第一版目标。

候选安全框架为 Spring Security 与 Apache Shiro。Shiro 3 支持 Spring Boot 4 和方法级注解；两者都能满足两个角色的第一版授权。当前 Backend 基于 Spring Boot 4，后续身份来源可能采用 OIDC、Bearer Token 或受信平台协议。Spring Security 在现有应用中提供请求级安全、方法级 AOP 授权和标准协议集成，因此作为 CMS Server 的实现选择。选型不要求外部宿主使用同一安全框架。

## 决策

1. CMS 内部只消费统一的可信管理主体（身份来源、稳定用户 ID、CMS 角色），不建立第一版用户资料或账号库。认证适配器负责验证外部凭证并转换主体；测试身份适配器仅在隔离测试环境可用。
2. CMS 角色 / 业务权限属于 CMS 自身。外部角色或声明只能经受信映射进入 CMS，不直接成为授权事实。第一版 `admin` 持有当前业务管理权限，`super` 覆盖它并拥有审计查询权限。
3. `cms-server` 使用 Spring Security：请求边界保护 Admin API，方法级授权通过 Spring Security 的 Spring AOP 机制保护管理操作。管理业务入口的授权不进入由 non-web Content Migration 共享的 Generic Core；Public API 与静态公开资源保持各自公开契约。
4. 业务审计使用独立 Spring AOP 切面围绕管理写操作采集主体和动作，并按事务实际提交 / 失败结果持久化。认证拒绝发生在业务切面之前时，按安全事件处理，不伪造成已执行的业务操作。
5. 审计记录以操作时的身份与角色为准，不依赖事后查询用户详情。审计的存取能力可由 Core 提供 framework-neutral contract，但 Core 不消费 Servlet、Spring Security `Authentication` 或当前 HTTP 请求。
6. CMS 管理页面可作为独立应用使用，或由外部管理平台提供入口；前端组合方式不改变 CMS 后端身份验证、授权和审计责任。真实独立部署必须配置正式身份来源。

## 影响与验证关注

Spring Security 的默认登录和随机用户不构成 CMS 正式或测试身份设计；必须显式配置 Admin/Public 请求边界。方法级授权需显式启用，且代理式 AOP 的类内自调用、Kotlin final 方法及与事务切面的顺序必须在实现和测试中处理。

审计成功事件不能仅凭被拦截方法正常返回判断；已回滚操作不能留下成功记录。业务审计持久化失败如何影响业务提交，需要在实施前按第一版“不丢失成功审计”的要求确定具体事务策略与失败处理，不能静默放行。

本 ADR 只确定目标结构与选择理由，不宣称现有代码已具备认证授权、审计或正式独立登录能力。管理端可观察验收由 Specification 持有，精确实现接缝由 Technical 文档持有。
