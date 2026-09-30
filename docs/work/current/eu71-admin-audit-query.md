---
id: execution-unit:eu71-admin-audit-query
type: execution-unit
status: ready
readiness: PASS
base_sha: 58a97ac2e1ae71dd1df5db7f959c174a4999d949
branch: codex/eu71-admin-audit-query
started_at: 2026-09-30
---

# EU-71 管理操作审计查询

## 当前执行状态

EU-71 已由当前 Requirement / Specification / Technical contract 切分，并在基线 `58a97ac2e1ae71dd1df5db7f959c174a4999d949` 通过独立 Readiness Gate；当前获得本文范围内的 Execute Authority。

## 目标

在 EU-70 已完成的可信审计事件与 append-only 持久化边界上，向 `super` 提供有界、只读、可筛选的审计列表 / 详情 API 与 Admin 操作审计页面，使已保存的操作者、动作、对象、时间、角色和结果可以被可靠追溯。

## Authority 恢复线索

- Product / Domain：`docs/requirements/cms-admin-identity-and-audit.md`；
- Observable：`docs/specifications/admin-access-audit.md`、`docs/specifications/admin-site.md`；
- Architecture：`docs/architecture/cms-architecture.md`、ADR-0005；
- Technical：`docs/technical/admin-security-integration.md`、`docs/technical/http-interface-contract.md`、`docs/technical/admin-frontend.md`；
- Verification / work：`docs/technical/verification-strategy.md`、`docs/work/README.md`、根 `AGENTS.md`、`frontend/AGENTS.md` 与 Consumer-local constraints。

这些路径只提供 Fresh Context 恢复线索；执行时仍须从 `docs/README.md`、当前任务和实际变更重新判断适用 Current Authority。

## 基线事实与依赖

- EU-70 已完成 `cms_admin_audit_event` / role snapshot schema、可信写入状态机、全部 Admin 写 handler descriptor、事务 / 文件补偿和敏感字段排除，并以实现提交 `4dfbec06e667367e5cd8f72119005a7cb77484ae` 集成。
- 当前 `AdminAuditPersistence` 明确只暴露 start / terminal transition，没有读取 contract；Server 没有审计查询 Controller，Admin 没有审计 route、adapter 或页面。
- 当前 `/api/admin/**` 请求级认证已实现，日常管理 handler 使用 `admin | super` 方法授权；审计查询必须另以 `super` 专属方法授权保护。
- 审计集合会持续增长；当前 V6 已有操作者、对象、请求关联与结果索引，但无通用开始时间 / 动作顺序索引。查询不能依赖全量读取、无界 count 或任意 offset 扫描。
- 正式身份提供方仍未接入；隔离 Review identity 当前只映射 `admin`，本单元需要在 review source set 内提供 `super` 证据而不改变正式产物边界。

## 范围

1. 建立与写入 persistence 分离的 framework-neutral 审计只读 query contract、事件 projection、filter / cursor value object 和 JDBC 实现；保持审计记录不可通过该 contract 修改或删除。
2. 追加必要的 schema index migration，支持 `startedAt + auditId` 稳定倒序，以及当前 contract 的操作者、动作、对象、结果与请求关联过滤；不得改写 V6。
3. 实现 `GET /api/admin/audit-events` cursor 分页查询与 `GET /api/admin/audit-events/{auditId}` 详情，严格遵守 HTTP contract 的字段、过滤组合、时间边界、`limit` 和失败语义。
4. 对查询 Controller 建立 `cms:super` 专属方法授权；无身份 `401`、`admin` / 其他非 `super` 主体 `403`，且任何拒绝或成功查询都不产生业务写操作审计。
5. 在 Admin CMS 模块增加 `/admin/cms/audit` canonical route、必要 compatibility redirect、独立“安全审计”导航和只读页面；展示历史角色快照与四种结果，支持过滤、下一页 / 本次浏览上一页、详情及明确的未认证 / 禁止访问 / 加载失败状态。
6. 在隔离 Review source set 提供 `super` 查询身份以形成 Browser / Runtime 证据；正式 BootJar 继续排除 Review 身份，且本单元不接真实身份提供方。
7. 建立 Core query、SQL / migration、HTTP contract、安全授权、Admin adapter / route / UI、Browser 和敏感字段负向验证，并回归 EU-70 写入、现有 Admin / Public、Core / Migration boundary 与正式 / Review artifact。

## 不在范围

- 审计记录修改、删除、批量处置、补写、重放或长期 `STARTED` 自动修复；
- 导出、报表、统计图、total count、保留期限、归档清理、外部审计 / SIEM 平台；
- 用户详情、显示名、组织 / 部门查询，或通过当前用户 / 当前业务对象改写历史 projection；
- 全局管理端登录、身份失效 / 退出交互或角色驱动的全站菜单裁剪；页面只处理本查询 surface 的 `401 / 403`；
- 真实 OIDC / JWT / 智慧就业平台身份适配器、CMS 本地账号或角色管理；
- 新的业务写操作、审计事件产生语义或 EU-70 事务状态机改造；
- Production Deployment 或 agentic-dev 修改。

## 完成条件与验证责任

- query contract 与 `AdminAuditPersistence` 分离，普通业务服务不能通过查询能力修改审计；列表和详情只返回 HTTP contract 声明的字段，角色为操作时快照。
- cursor 分页默认 20、最大 100，按 `startedAt DESC, auditId DESC` 稳定推进；新记录进入、相同时间戳和翻页往返不产生既有记录重复 / 遗漏，末页 `nextCursor = null`。
- 完整操作者、动作、对象、结果、时间和请求关联过滤可独立及组合工作；非法 cursor、枚举、limit、操作者 / 对象组合和时间范围均受控返回 `400`，未知 audit ID 返回 `404`。
- `super` 可读取列表 / 详情；`admin`、无允许角色与匿名主体分别得到正确 `403 / 401` 且无数据泄漏；实际 handler inventory 与 HTTP contract 双向一致，查询 handler 不带写审计 descriptor。
- Admin 页面 canonical route、导航、过滤、cursor 翻页、详情、四种状态和失败状态可观察；详情不查询用户目录或当前业务对象，不提供 mutation / export UI。
- 响应、页面、日志与自动化证据不包含 token、password、完整请求体、Rich Text 正文、文件 bytes、异常消息或堆栈；`STARTED` 明确呈现为结果未确认。
- Fresh DB 与 V1→最新版本升级均成功，索引与 query SQL 在真实 MySQL 验证；EU-70 事务 / 文件补偿和 38 个写 descriptor 回归保持通过。
- Backend clean tests / build、Admin build / Browser、Public regression、Core / Migration boundary、正式与 Review BootJar 及最终 Local Docker CI 对目标 exact Head PASS。
- 最终变更完成独立安全 / 隐私 / 查询边界复核与 History Convergence 后本地集成；Post-Integration Evidence 完成后归档本 Unit 并将 Current locator 恢复为 NONE 或另一个独立 Ready Unit。

## 依赖与后续交接

EU-71 依赖 EU-70 已集成的事件模型和持久化语义。管理端全局身份反馈与真实宿主接入仍是独立后续方向；EU-71 的完成不授予这些方向 Execute Authority。

## 就绪门禁

**PASS**（2026-09-30，基线 `58a97ac2e1ae71dd1df5db7f959c174a4999d949`）。

- Requirement 明确 `super` 独占审计读取，Specification 已固定列表 / 详情、过滤、历史快照、四种结果、拒绝与敏感字段行为；不存在需要人工补充的 Product / Domain 决定。
- HTTP contract 已确定 endpoint、query、cursor、limit、时间边界、wire projection 与 `400 / 401 / 403 / 404` 语义；Technical owner 已固定独立只读 contract、索引、批量角色读取、无 count 和 Review-only `super` 证据边界。
- EU-70 schema 与写入状态机已集成；当前代码明确没有 query contract / Controller / Admin 页面，V6 现有索引与新增通用时间 / 动作索引责任可区分，不需要改写历史 migration。
- 本地 `main` 与 `origin/main` 均为基线 SHA，工作树只包含 EU-71 Authority 与切分变更；目标 local / remote branch 不存在，无 Open PR、无 queued / in-progress Actions，没有并发 ownership 冲突。
- 验证责任覆盖 Core/JDBC、真实 MySQL Fresh/upgrade、HTTP/Security、写 descriptor 负向守卫、Admin adapter/build/browser、Public 与 Migration boundary、正式/Review BootJar、敏感字段、exact-head Local Docker CI、独立复核和集成后证据。

此 PASS 只授予 `codex/eu71-admin-audit-query` 上的 EU-71 实施与验证，不授予真实身份接入、全局身份反馈、审计 mutation / export / retention、Production Deployment 或其他 Roadmap 方向。
