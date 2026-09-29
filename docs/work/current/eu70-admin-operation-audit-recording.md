---
id: execution-unit:eu70-admin-operation-audit-recording
type: execution-unit
status: in_progress
readiness: PASS
base_sha: 2705a5f43794cebcb12af08d973641b926654ea5
branch: codex/eu70-admin-operation-audit-recording
started_at: 2026-09-29
---

# EU-70 管理操作审计记录

## 当前执行状态

EU-70 的实现与分层验证已在目标 branch 工作树完成；独立安全 / 隐私 / 事务复核、History Convergence、目标 exact-head Local Docker CI、本地集成、Post-Integration Evidence、归档与推送闭环仍须依次完成。本段只记录执行进度，不构成 Completion / Integration 声明。

## 目标

在现有可信 `CmsPrincipal`、Admin 请求 / 方法授权与 Server/Core/Migration 边界之上，建立第一版可信管理写操作审计事件产生和持久化能力，使“谁在何时对什么对象执行了什么动作、结果如何”可以由后续查询能力可靠消费。

本单元只负责审计证据产生与保存，不提供查询 API / UI，不接入真实身份提供方。

## Authority 恢复线索

- Product / Domain：`docs/requirements/cms-admin-identity-and-audit.md`；
- Observable：`docs/specifications/admin-access-audit.md`；
- Architecture：`docs/architecture/cms-architecture.md`、ADR-0005；
- Technical：`docs/technical/admin-security-integration.md`、`docs/technical/http-interface-contract.md`；
- Verification / work：`docs/technical/verification-strategy.md`、`docs/work/README.md`、根 `AGENTS.md` 与 Consumer-local constraints。

这些路径只是恢复线索；执行时仍须从 `docs/README.md` 和当前任务重新判断实际适用的 Current Authority。

## 基线事实与依赖

- EU-67 已建立 `CmsPrincipal(identitySource, userId, roles)` 与隔离 test / Review 身份；正式身份提供方尚未接入。
- EU-68 已把 Admin HTTP transport 收敛到 `cms-server`，Generic Core / Content Migration 不持有管理 HTTP 身份。
- EU-69 已为全部现有 Admin handler 建立请求级认证和 `@CmsAdminAccess` 方法授权；无身份 `401`、无 CMS 权限 `403`，Public / static 匿名读取保持不变。
- 当前 13 个 Admin capability family 的全部写入口由 `docs/technical/admin-security-integration.md` 与实际 Spring MVC handler inventory 双向定位；当前没有业务审计表、审计切面或查询入口。
- Core 写服务广泛使用 Spring transaction；托管资源和静态资源还包含文件系统副作用，因此不能把“方法正常返回”机械等同于事务已提交或副作用可回滚。

## 范围

1. 建立 framework-neutral 的审计记录模型、持久化 contract 与 append-only schema migration；字段至少覆盖审计 ID、请求关联标识、`identitySource`、`userId`、角色快照、动作、对象类型 / 可用对象标识、开始 / 完成时间和结果。
2. 在 `cms-server` 建立显式管理写操作 descriptor 与 Spring AOP / transaction boundary；可信基础设施从当前 `CmsPrincipal` 和实际返回 / 路径对象提取审计数据，客户端不能自报主体、角色或结果。
3. 按 Technical owner 固定的 `STARTED → SUCCEEDED / FAILED / ROLLED_BACK` 语义处理正常提交、Domain / input 失败、rollback-only、提交阶段异常与未确认结果。
4. 实现审计写入失败策略：初始 `STARTED` 写入失败时不执行业务；成功终态与数据库业务变化共同提交；失败终态独立持久化，写入失败时保留 `STARTED`、传播原异常并输出有界诊断。
5. 为 Managed Resource / Static Resource 写操作建立必要的暂存 / 补偿边界；可逆失败恢复原可见状态，无法确认的文件系统结果保持 `STARTED` 且调用失败，不误记成功。
6. 覆盖当前全部 Admin 写 handler，包括上传、明确替换、软删除、恢复、文章发布 / 撤回；用实际 handler 与 descriptor inventory 双向守卫新增 / 遗漏入口。
7. 验证敏感字段排除：不持久化凭证、密码、完整请求体、文件内容、Rich Text 正文、异常堆栈或任意异常消息。
8. 回归 `401 / 403`、Public / static 匿名读取、Core / Content Migration boundary、正式 BootJar 排除 Review 身份以及现有业务不变量。

## 不在范围

- 审计查询 Repository 的公开读取 contract、HTTP API、Admin 页面、导出或报表；
- `super` 查询授权和管理端查询交互；
- 普通运行日志 / access log / 安全事件平台建设；`401 / 403` 只验证不伪造成业务审计；
- 真实 OIDC / JWT / 智慧就业平台身份适配器或独立网站正式登录；
- 用户详情、账号库、角色管理、数据范围权限；
- 保留期限、归档 / 清理、外部审计平台、Production Deployment。

## 依赖与后续交接

EU-70 依赖 EU-67～EU-69 已集成能力。EU-71 只能消费 EU-70 最终完成的记录模型与持久化边界；EU-70 不提前创建查询 endpoint、DTO 或页面，也不授予 EU-71 Execute Authority。

## 完成条件与验证责任

- schema 可从空数据库建立并从当前 baseline append-only 升级；审计记录不随业务对象普通删除消失，普通 Admin 写接口不能修改 / 删除审计记录。
- 两个不同 `identitySource` 使用相同 `userId` 时形成不同操作者身份；角色保存操作时快照，后续主体变化不改写历史。
- 每个当前 Admin 写 handler 恰好具有一个稳定 descriptor；读 handler、Public、static read 与 Content Migration 不进入业务审计。
- create / update / delete、publish / withdraw、upload / replace / trash / restore 的成功结果均形成 `SUCCEEDED`，并能取得可用对象标识；失败前尚无对象 ID 时允许为空，但不得复制请求正文。
- Domain / input 失败形成 `FAILED`，rollback-only / commit failure 形成 `ROLLED_BACK`；二者都不能留下 `SUCCEEDED`。
- `401 / 403` 不执行写操作，也不产生伪造业务审计；安全拒绝仍由安全边界处理。
- 初始审计写入失败时业务不执行；成功审计写入 / 提交失败时数据库业务回滚；失败终态写入失败时原 `STARTED` 可追溯且原业务异常不被掩盖。
- 文件系统写入口验证正常、补偿成功和无法确认三类结果；任何非成功路径都不留下虚假 `SUCCEEDED`。
- 自动化测试明确断言审计表不含 token、password、完整 body、Rich Text、文件 bytes、异常堆栈 / 原始消息等敏感数据。
- Backend clean verification、Fresh DB / migration、受影响 HTTP / Security / application boundary、正式与 Review BootJar 及最终 Local Docker CI 对目标 exact Head PASS。
- 最终变更完成独立安全 / 隐私 / 事务复核与 History Convergence 后再集成；Post-Integration Evidence 完成后归档本 Unit 并将 Current locator 恢复为 NONE 或下一个独立 Ready Unit。

## 就绪门禁

**PASS**（2026-09-29，基线 `2705a5f43794cebcb12af08d973641b926654ea5`）。

- Requirement、Specification、ADR-0005 与更新后的 Admin Security Technical contract 对本 Unit 的目标、结果语义、敏感数据边界和查询非目标一致；没有需要返回 Product / Specification / Architecture 决策的阻塞项。
- 当前实际 Admin handler 与 Technical 权限矩阵共同给出 13 个 capability family 的写入口 inventory；Core 写服务的 Spring transaction 和 Managed / Static Resource 文件系统副作用已纳入 completion / verification，未把方法返回误当成提交成功。
- 技术接缝已固定 `STARTED → SUCCEEDED / FAILED / ROLLED_BACK`、初始审计失败时 fail closed、成功终态与数据库业务共同提交、失败终态独立持久化及文件副作用补偿 / 不确定结果策略，不再把关键事务选择留给执行时猜测。
- 初始核验时本地 `main`、`origin/main` 与 GitHub 当前目标均指向同一基线，工作树干净、无活动 WebCodex Job、无 Open PR；本轮工作树变化仅为 EU-70 / EU-71 拆分、Technical contract 与 locator / Roadmap 同步。
- 验证责任覆盖 Fresh DB / migration、事务提交与回滚、审计写入故障、文件补偿、敏感字段、实际 handler 双向 inventory、`401 / 403`、Public / static、Core / Migration、正式 / Review BootJar、独立安全复核和 exact-head Local Docker CI。
- 当前规划文档已通过 `scripts/verify-docs-governance.mjs`；规划提交本身只需要 docs-only evidence，EU-70 实现完成声明仍必须取得目标 exact Head 的完整证据。

此 PASS 只授予 EU-70 在 `codex/eu70-admin-operation-audit-recording` 上执行，不授予 EU-71、真实身份接入、Production Deployment 或其他后续方向。
