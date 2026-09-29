---
id: execution-unit:eu68-core-http-boundary
type: execution-unit
status: active
readiness: pending
base_sha: ccf89912dfa262fd4edbf7b2fe52cbd5c3ff99ee
branch: codex/eu68-core-http-boundary
started_at: 2026-09-29
---

# EU-68 Core HTTP 责任与上传输入边界收敛

## 目标

在管理请求 / 方法授权落地前，把当前残留于 `cms-core` 的 HTTP Controller 与 `MultipartFile` 服务输入移到 `cms-server` transport 接缝，并让 `content-migration` 只向 Core 传递 framework-neutral 上传输入。现有 Admin / Public HTTP 行为、迁移结果和资源安全规则保持不变。

本单元只关闭 Backend application boundary debt，不宣称 Admin API 已受保护。

## Authority 恢复线索

- Product / Domain：`docs/requirements/cms-admin-identity-and-audit.md`、`docs/requirements/cms-domain.md`；
- Observable：`docs/specifications/admin-access-audit.md` 与既有 Admin / Public 规格；
- Architecture：`docs/architecture/cms-architecture.md`、ADR-0005；
- Technical：`docs/technical/backend-service.md`、`docs/technical/admin-security-integration.md`、`docs/technical/http-interface-contract.md`；
- Verification / work：`docs/technical/verification-strategy.md`、`docs/work/README.md` 与根 `AGENTS.md`、`docs/governance/constraints.md`。

这些只是 Fresh Context locator；执行前须按实际变更重新判断 Current Authority 和代码事实。

## 范围

1. 将 Core 中现有导航位置 Admin Controller、公开列表与广告查询 Controller 移交 `cms-server`；其 URI、HTTP method、参数、返回投影与失败语义不变，Core 保留共享 service / mapper / domain capability。
2. 将 Core 的托管资源与静态资源上传服务、存储接口从 `MultipartFile` 改为可重复打开流的 framework-neutral metadata + content 输入；Server 在现有 multipart Controller 内完成适配，不放松文件签名、路径、替换、发布引用等规则。
3. 将 Content Migration 中为调用 Core 而伪造的 `MultipartFile` 移除，改由 canonical 文件路径构造相同中立输入；保持 source identity、content type、fingerprint、导入结果与重放语义。
4. 让 application boundary verification 按责任性质检查 Core 不再包含 MVC Controller、Servlet / HTTP 或 multipart transport 输入，Migration source 不再伪造 multipart；继续检查两个 BootJar 的独立 application composition。
5. 覆盖中立输入的空文件、签名、Office 文档、替换、存储失败清理，以及原有 Admin/Public HTTP 和 Fresh Migration 回归。

## 不在范围

- Spring Security、请求 / 方法授权、`401 / 403` 或默认拒绝切换；
- 正式身份提供方、测试 HTTP 登录入口、Admin 前端身份状态或业务操作审计；
- HTTP endpoint、wire DTO、数据库 Schema、Domain 规则、canonical dataset 或站点业务映射变更；
- Main / Party source discovery、snapshot promotion、历史数据修复或 Production Deployment。

## 依赖与后续关系

- 依赖 `main@ccf89912dfa262fd4edbf7b2fe52cbd5c3ff99ee`、已完成 EU-67 的可信主体基础，以及现行 Backend / Admin Security / HTTP Technical contract；当前不需要新的产品或长期架构决定。
- 后续管理请求 / 方法授权单元应复用本单元的 Server/Core/Migration 分界，并独立解决正式配置 fail-closed、隔离测试 HTTP 身份装配与既有 Admin Browser 回归；本单元不提前授予其 Execute Authority。

## 验收与验证责任

| 完成义务 | 验证责任 |
| --- | --- |
| Core 不再持有 HTTP transport / multipart | Core source 与编译产物结构检查；`verifyBackendApplicationBoundary` 且不能仅依赖已知类黑名单 |
| Server 保持全部既有 HTTP contract | 原有 Admin / Public Browser 回归，必要的 Controller / interface contract 测试 |
| 托管与静态资源上传行为不变 | Server / Core 单元及资源 Browser 测试覆盖内容验证、替换、空文件、失败路径与 binary response |
| Migration 不伪造 web upload 且数据语义不变 | Migration source / BootJar 边界检查，受影响的 canonical / compatibility / page Fresh Runtime 与幂等验证 |
| 没有意外开启管理授权 | Server composition、dependency、Admin Browser 回归；文档不得声称 `401 / 403` 已实现 |
| 目标提交可恢复并可集成 | Backend clean build / tests / BootJars、Local Docker CI full、文档治理与 diff 检查绑定 exact Head |

## 完成条件

上述边界、适配、结构守卫与行为回归均在目标 exact Head 有证据；对最终变更执行 `review-change` 与 feature convergence 后，只按仓库本地集成流程进入 `main`。实施集成后仍须取得与完成声明相符的证据，再归档本 artifact；不把本单元的通过解释为 Admin API 已受保护。
