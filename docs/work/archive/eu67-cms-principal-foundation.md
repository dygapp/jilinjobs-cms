---
id: execution-unit:eu67-cms-principal-foundation
type: execution-unit
status: completed
readiness: PASS
base_sha: 3f0e431ba1f0b3117eba9a939fb2976f6baa8df4
branch: codex/eu67-cms-principal-foundation
started_at: 2026-09-29
verified_head_sha: 8dd1e03f6be2abe0cf0d153633d715cfd2083476
integrated_sha: 8dd1e03f6be2abe0cf0d153633d715cfd2083476
completed_at: 2026-09-29
---

# EU-67 CMS 可信主体与隔离测试身份基础

## 完成结果与证据

- `cms-server` 已建立 `CmsPrincipal(identitySource, userId, roles)`、凭证验证器与服务端受控角色映射接缝；无效、过期、伪造、跨来源凭证及未知角色均失败关闭。
- 随机不透明凭证的测试身份适配器只位于 test source；自动化测试覆盖同 `userId` 跨来源隔离、`admin` / `super` 角色与负向路径，`bootJar` 边界检查排除测试身份类。正式构建没有 Spring Security 默认用户或请求过滤链。
- 未接入真实身份提供方，未开启 Admin 请求 / 方法授权、HTTP `401 / 403`、操作审计或前端身份状态；Admin API 仍未受保护。
- 实现提交 `8dd1e03f6be2abe0cf0d153633d715cfd2083476` 在任务分支及快进后的本地 `main` 均取得 `scripts/local-ci.sh full` PASS；证据分别为 `.local-ci/evidence/20260929T034651Z-842669/` 与 `.local-ci/evidence/20260929T040052Z-872601/`，后者 `subject.txt` / `result.txt` 绑定 `main` 的 exact commit。后端与迁移边界、正式前端构建、Public Browser（60 通过、7 跳过）和 Admin Browser（46 通过、1 跳过）均通过。
- 完成态变更经 `review-change` 无阻断或中级 finding，`converge` 返回 READY TO INTEGRATE；随后以 fast-forward 集成到本地 `main` 并重跑完整验证。归档与路线图更新是文档收口，不扩大本单元实现范围。

## 目标

在 `cms-server` 内建立可替换的可信身份转换接缝，把经过验证且由受信规则映射的外部身份转换为统一 `CmsPrincipal(identitySource, userId, roles)`，并提供只能在隔离测试环境装配的测试身份适配器，为后续 Admin 请求 / 方法授权提供可验证基础。

本单元只证明身份转换基础存在且隔离正确，不宣称 Admin API 已受保护。

## Authority 恢复线索

- Product / Domain Requirement：`docs/requirements/cms-admin-identity-and-audit.md`；
- Observable contract：`docs/specifications/admin-access-audit.md`；
- Architecture state：`docs/architecture/cms-architecture.md` §5、§13；
- Architecture decision：`docs/architecture/decisions/ADR-0005-admin-identity-authorization-audit.md`；
- Technical seam：`docs/technical/admin-security-integration.md` 的身份转换、权限矩阵和验证章节；
- Backend boundary：`docs/technical/backend-service.md`；
- Verification：`docs/technical/verification-strategy.md`；
- Repository / work constraints：根 `AGENTS.md`、`docs/governance/constraints.md`、`docs/work/README.md`。

以上只作为 Fresh Context locator；执行时仍须按当前任务和仓库状态重新判断实际适用的 Current Authority。

## 范围

1. 在 `backend/apps/cms-server` 建立统一 `CmsPrincipal`，以 `identitySource + userId` 标识主体，并只携带经受信映射得到的 CMS 角色集合。
2. 建立“验证外部凭证 / 声明 → 受信身份结果 → CMS 主体”的可替换接缝；未验证的 HTTP 参数、普通客户端 Header、显示名或客户端自报角色不得直接构造可信主体。
3. 建立受信角色映射与失败关闭行为：未知来源、无效 / 伪造 / 过期凭证、未知角色以及没有 `admin` / `super` 的结果均不能晋升为管理主体。
4. 提供隔离测试身份适配器，支持至少两个不同 `identitySource` 使用相同 `userId`，以及 `admin`、`super`、无允许角色、无效和过期凭证场景。
5. 证明正式配置不能装配或启用测试身份入口，且不存在默认模拟凭证、硬编码超级用户或 Spring Boot 默认随机用户回退。
6. 保持身份实现属于 Server application；Generic Core 不接收 HTTP 用户或 Spring Security 请求身份，Content Migration 不依赖、装配或伪造本单元主体。

## 实现边界

- 若本单元需要 Spring Security 类型，只引入不会自动开启 Web 请求保护的最小依赖；不得因自动配置意外生成默认账号、改变当前 Admin / Public HTTP 行为或提前声称请求已授权。
- `identitySource`、`userId` 和角色映射输入都必须来自已经通过适配器验证的结果；原始令牌和用户详情不进入 `CmsPrincipal`。
- 角色集合只表达当前 CMS `admin` / `super` 语义；本单元不新建栏目、站点、组织、租户或数据范围权限。
- 测试适配器的隔离必须由代码结构 / 配置和自动化验证共同证明，不能只依赖部署人员“不要开启”的约定。
- 不把当前 Core 中既有 HTTP / multipart boundary debt 混入本单元；该问题必须在后续方法授权落点实施前按现有 Technical contract 独立处理。

## 明确不在范围

- 正式 OIDC、JWT、智慧就业平台专有协议或独立网站身份提供方接入；
- 登录、退出、密码、CMS 用户库、用户资料同步或角色管理 UI；
- `SecurityFilterChain`、`@EnableMethodSecurity`、`@PreAuthorize` 或其他 Admin 请求 / 方法授权；
- `401 / 403` HTTP 行为、Admin Router / 菜单身份状态与 Browser 验证；
- 业务操作审计、审计表、审计切面、审计查询 API 或页面；
- Core HTTP / multipart transport 边界重构；
- Production Deployment、Secrets / Credentials 操作与任何真实外部身份系统变更。

## 依赖与后续关系

- 本单元依赖当前已 accepted 的 Requirement、Specification、ADR-0005、Technical seam 与 `main@3f0e431b`，无外部身份协议 blocker。
- 后续“管理请求与方法授权”单元依赖本单元的统一主体与隔离测试身份，但不会因本单元完成自动获得 Execute Authority。
- 真实宿主接入仍等待具体协议和部署环境明确；它不阻塞本单元，也不能由测试身份替代。

## 验收与验证责任

| 验收义务 | 最低验证责任 |
| --- | --- |
| 两个来源的相同 `userId` 不混淆 | Server 单元测试断言主体 identity / equality / audit-ready identity 组合包含 `identitySource` |
| 只有受信验证和映射可产生主体 | 覆盖有效、伪造、无效、过期、未知来源、未知角色与无允许角色；负向路径均失败关闭 |
| `admin` / `super` 角色不能由客户端自报晋升 | 适配器 / mapper 测试证明角色来自受控映射，不接受普通请求值直接构造 |
| 测试身份只在隔离测试环境可用 | 测试配置正向装配 + 正式配置负向装配；正式配置不存在测试身份入口、默认用户或默认凭证 |
| 未提前改变 HTTP 授权行为 | Server application context / dependency 验证证明没有默认 Security 登录或隐式 `SecurityFilterChain`；既有接口 contract 不变 |
| Core / Migration 边界保持 | 结构验证证明身份与测试适配器不进入 Core / Migration；`verifyContentMigrationBoundary` 与 application boundary verification PASS |
| Backend 变更可构建且回归通过 | 受影响 Server / Migration tests、Backend application boundary、`bootJar` 按当前 Gradle implementation PASS |
| Work / 文档治理一致 | `scripts/verify-docs-governance.mjs` 与 `git diff --check` PASS |

本单元不需要 Browser / 视觉验证；它没有被授权改变用户可观察页面或 HTTP 授权结果。若实现触达这些边界，必须停止并返回 `slice-work`，不能扩大本单元完成声明。

## 完成条件

1. 上述主体、可信转换接缝、受信角色映射与隔离测试身份适配器均已实现；
2. 正负向自动化测试覆盖同 ID 跨来源隔离、角色映射、伪造 / 无效 / 过期凭证及正式配置排除测试入口；
3. Core / Server / Migration 依赖方向和 non-web Migration composition 保持，相关边界验证通过；
4. 当前 Admin / Public HTTP contract、数据库 Schema、前端和审计能力没有被本单元改变；
5. 完成声明绑定目标 exact Head；若工作树为 dirty，只能声明对应 `HEAD + worktree fingerprint` 的阶段性结果；
6. 收敛复核确认没有把后续授权、审计、正式登录或真实宿主协议责任隐含进本单元。

## 就绪门禁

**PASS**（2026-09-29，base `3f0e431ba1f0b3117eba9a939fb2976f6baa8df4`）。

- Requirement、Specification、Architecture / ADR 与 Technical seam 对本单元目标、非目标和失败关闭边界一致；
- 真实宿主协议尚未确定，但当前 Authority 明确允许先建立可替换接缝和隔离测试身份，因此不是 blocker；
- 当前 Repository 为单仓，目标改动限于 Backend Server 与必要验证；根目录以下没有额外 Backend path-scoped `AGENTS.md`；
- Gate 检查时 `main` 与 `origin/main` 均指向 base SHA，工作树干净，`docs/work/current/README.md` 为 `NONE`，GitHub Open PR 为 0，未发现同目标远端分支；
- 当前实现没有 Spring Security / 统一主体；Server 与 Content Migration 已是独立 application module，足以在不污染 Core / Migration 的前提下完成本单元；
- Scope、依赖、完成条件和验证责任均可由 Fresh Context 恢复，没有已知阻塞 finding。

该 PASS 只授予本单元进入 Execute / Verification 生命周期；不授予后续单元、merge、release 或 deploy 权限。
