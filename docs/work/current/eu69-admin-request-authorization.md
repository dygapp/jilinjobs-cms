---
id: execution-unit:eu69-admin-request-authorization
type: execution-unit
status: active
readiness: PASS
base_sha: 463982ab341dd51c5796f1cce690011f693d4c21
branch: codex/eu69-admin-request-authorization
started_at: 2026-09-29
---

# EU-69 管理请求与方法授权

## 目标

在现有可信主体转换基础上，为 CMS Server 建立第一版请求级与方法级授权边界，使全部现有 Admin API 默认要求可信管理身份，Public 读取保持匿名可用，Content Migration 继续独立于 HTTP 身份。

本单元不选择真实宿主身份协议；真实 OIDC、JWT 或平台专有适配器仍属于后续“真实宿主接入”。

## Authority 恢复线索

- Product / Domain：`docs/requirements/cms-admin-identity-and-audit.md`；
- Observable：`docs/specifications/admin-access-audit.md`；
- Architecture：`docs/architecture/cms-architecture.md`、ADR-0005；
- Technical：`docs/technical/admin-security-integration.md`、`docs/technical/http-interface-contract.md`；
- Verification / work：`docs/technical/verification-strategy.md`、`docs/work/README.md`、根 `AGENTS.md` 与 Consumer-local constraints。

## 基线事实与依赖

- EU-67 已建立 `CmsPrincipal`、`CredentialVerifier`、`CmsIdentityConverter` 与仅存在于 test source 的隔离凭证验证器；正式 Server 尚无身份提供方。
- EU-68 已把 Admin HTTP transport 和 multipart 责任收敛到 `cms-server`；Generic Core / Content Migration 不持有管理 HTTP 身份。
- 当前 `cms-server` 尚未依赖 Spring Security，全部 Admin API 仍未受保护。
- Technical 权限矩阵要求实际注册的每个 `/api/admin/**` handler 恰好纳入管理授权；Public GET 与 `/static/**` 保持匿名读取，其他未分类请求不隐式开放。

## 范围

1. 在 `cms-server` 引入 Spring Security，显式建立 Admin / Public / static / uncategorized 请求边界，并启用方法级 Spring AOP 授权。
2. 将经验证的 `CmsPrincipal` 投影为 Spring Security `Authentication` / authorities；认证适配保持可替换，不把真实宿主协议、用户详情或客户端自报角色固化进 CMS。
3. 为现有 Admin transport/application 入口建立统一方法授权，`admin` 与 `super` 可访问现有管理业务；后续审计查询仍保留 `super` 专属能力。
4. 用 test source verifier 与隔离 Review source set / BootJar 的认证适配器验证两个身份来源、角色映射、无效 / 过期凭证、无允许角色、正式产物拒绝 Review 身份及 `401 / 403`。
5. 双向核对实际 Spring MVC handler 与当前 Admin 权限矩阵，确保新增或遗漏的 Admin handler 能被验证发现。
6. 回归 Public GET、静态公开资源以及 Content Migration / Core application boundary。

## 不在范围

- 真实 OIDC / JWT / 智慧就业平台或独立网站身份提供方；
- Admin 前端登录、失效提示、菜单 / 路由反馈；
- 业务操作审计切面、审计表、审计查询 HTTP API；
- CMS 本地账号、密码、用户详情、角色管理 UI；
- Product / Domain 权限模型扩展、Production Deployment。

## 完成条件与验证责任

- 无身份访问任一 Admin handler 返回 `401`，且不返回管理数据、不执行写操作。
- 已认证但无 `admin` / `super` 权限的请求返回 `403`。
- `admin` 与 `super` 可通过相同现有 Domain 校验访问全部当前 Admin 业务；方法级授权独立于仅靠 URL matcher。
- Public GET 与 `/static/**` 保持匿名可用；不因 Security 默认配置引入登录页、默认用户或扩大 Public 写权限。
- 正式 Server 产物不包含测试凭证入口或硬编码管理用户；没有正式身份适配器时 Admin 保持 fail closed。
- 实际注册的 Admin handler 与 Technical 权限矩阵双向一致；Core / Migration 仍不依赖 Spring Security 请求主体。
- Backend tests / build / BootJar、application boundary、受影响 HTTP contract 验证和最终 Local Docker CI 对目标 exact Head PASS。
- 最终变更完成独立 review 与 History Convergence 后再进入本地集成；本单元不提前实现下一阶段审计或真实宿主登录。

## 就绪门禁

**PASS**（2026-09-29，基线 `463982ab341dd51c5796f1cce690011f693d4c21`）。

- Requirement、Specification、ADR-0005、Admin Security Technical contract 与 HTTP contract 对本 Unit 的目标一致；真实宿主凭证协议明确留给后续接入，不阻塞 Server 授权能力。
- EU-67 的主体转换和 test-source-only verifier、EU-68 的 Server/Core/Migration transport 分界均可从当前代码恢复；正式 Server 当前没有 Security dependency 或认证回退。
- 当前 HTTP contract 与权限矩阵均列出 13 个 Admin capability family；实施必须以实际 Spring MVC handler inventory 双向守卫，不能仅按文件名或 URL 前缀推测覆盖。
- 本 Unit 为单 Repository 安全边界变化，已从干净 `main` 创建独立任务分支；验证责任覆盖 `401 / 403`、`admin / super`、Public 匿名回归、正式配置 fail-closed、测试身份不进入 BootJar 以及 Core / Migration 隔离。

此 PASS 只授予 EU-69 执行，不授予业务审计、Admin 前端身份反馈、真实宿主接入、merge、release 或 deploy。
