---
id: execution-unit:eu72-admin-identity-feedback
type: execution-unit
status: completed
readiness: PASS
base_sha: 7d2682b7608d3d00d28b9758ceb5675a31e2e6d6
branch: codex/eu72-admin-identity-feedback
started_at: 2026-09-30
verified_head_sha: c252a9eb54628aa72c8fa88826e4d53f56e8a549
integrated_sha: c252a9eb54628aa72c8fa88826e4d53f56e8a549
completed_at: 2026-09-30
---

# EU-72 管理端身份反馈与端到端闭环

## 完成结果与证据

- 受保护的 `GET /api/admin/identity` 仅投影可信 `CmsPrincipal` 的来源、用户 ID 和稳定角色；Admin 统一身份 gate / HTTP adapter 支持主体展示、角色导航、deep link / 刷新、全局 `401 / 403` 与网络退出失败的真实反馈。全部既有 Admin adapter 使用同一边界，Public 保持匿名。
- 隔离 Review BootJar 提供测试人员可操作的 `admin` / `super` 登录、退出和主动模拟失效；Server 控制固定 profile，随机 opaque credential、短期 TTL、有界内存 registry 与立即撤销。仅资源内容 GET 可使用 scoped HttpOnly / SameSite Cookie，其他读写仍须 Header。正式产物不包含 Review endpoint / verifier，仍 fail closed。
- 最终实现提交 `c252a9eb54628aa72c8fa88826e4d53f56e8a549` 在干净任务分支取得 exact-head Full Local Docker CI PASS：`.local-ci/evidence/20260930T083124Z-1659690`，完成时间 `2026-09-30T08:43:51Z`。Backend 40 个 task；Review session 4 项、Admin Security 14 项；Public 60 passed / 7 skipped，Admin 53 passed / 1 skipped，restored Review Party 1 passed；`canonical_specialized=true`、`upgrade_specialized=true`。包含 Fresh MySQL V1～V7、EU-70 审计事务 / 资源补偿、EU-71 查询及正式 fail-closed 回归；没有新增 schema 或改变审计状态机。
- 独立 fresh context reviewer `/root/eu72_final_review` 对最终 SHA / diff / Authority / 证据给出限定 Review PASS，无未解决阻塞或 P2。关闭自动化全局 Header 外泄、混合 Admin 场景身份缺失、人工 fixture 匿名请求、fallback 正式 / Review 产物误用及租约 marker 覆盖等 findings；三 Workflow YAML 与 72 段 inline shell 语法 PASS。`.dockerignore` 排除 Review jar 的初步推测撤回；本轮实际执行当前构建上下文，BuildKit 层缓存不被描述为禁缓存构建。
- History Convergence 保留共享规划 `e7dd1dea` 和功能 `32708880`；集成后发现的运行控制修复收敛为一个 logical commit `c252a9eb`。没有 force push、WIP 或按验证轮次堆叠过程性提交；归档另成 docs-only closure。
- 任务分支已推送并回读为最终 SHA；本地 `main` fast-forward 后仍是同一 exact SHA，任务→main diff 为空。因此复用上述同 SHA Full 证据，不声明第二轮 Full Run。Post-Integration Evidence：`.local-ci/evidence/eu72-post-integration-c252a9eb`，默认 `human-review.sh start`、人工 fixture 注入和六项 EU-72 Browser 均 PASS；随后 `reset` 恢复 verified baseline 并重新注入人工 fixture，清除自动测试可写状态与会话，保留可供测试人员操作的本地 Review Runtime。
- 归档 closure 只改变 Work locator、Roadmap、Evolution 和 Guide，不改变 Runtime 输入；通过精确 ancestor→closure diff 与 Runtime fingerprint 等价校验复用实现 SHA 的构建 / Browser 证据。Current Unit 恢复为 NONE，不从本历史 artifact 推导新 Execute Authority。
- 本轮是自动验证与独立 AI 复核，不声称人工观察、远端 GitHub Actions Run PASS、真实身份提供方接入或 Production Deployment。真实宿主协议仍是独立后续方向。

以下保留规划、readiness 与阶段进展的历史，阶段性“尚待”不代表当前未完成。

## 目标

让 Admin Application 对当前可信主体、未认证、运行中身份失效和禁止访问形成统一反馈，并在隔离 Review 构建提供测试人员可操作的 `admin` / `super` 模拟登录、退出和失效入口，以真实 Browser / Backend / Fresh Runtime 证明前端反馈与服务端授权一致。

## Authority 恢复线索

- Product / Domain：`docs/requirements/cms-admin-identity-and-audit.md`；
- Observable：`docs/specifications/admin-access-audit.md`、`docs/specifications/admin-site.md`；
- Architecture：`docs/architecture/cms-architecture.md`、ADR-0005；
- Technical：`docs/technical/admin-security-integration.md`、`docs/technical/http-interface-contract.md`、`docs/technical/admin-frontend.md`；
- Verification / work：`docs/technical/verification-strategy.md`、`docs/work/README.md`、根 `AGENTS.md`、`frontend/AGENTS.md` 与 Consumer-local constraints。

这些路径只提供 Fresh Context 恢复线索；执行时仍须从 `docs/README.md`、当前任务和实际变更重新判断适用 Current Authority。

## 基线事实与依赖

- EU-69～EU-71 已完成统一 `CmsPrincipal`、Admin 请求 / 方法授权、可信写操作审计和 `super` 专属查询；正式 Server 无身份适配器时所有 Admin 请求 fail closed。
- 当前 Review source set 只接受一个显式 token 并映射为 `super`；E2E Nginx 为所有 Browser 请求静默注入该 token，因此无法人工登录、退出、模拟失效或验证 `admin` 的页面边界。
- Admin 没有全局身份状态、主体展示或 route/shell 反馈；既有业务 adapter 各自调用 `fetch` 并把 `401 / 403` 降格为普通错误，只有审计页面局部区分访问状态。
- 正式身份提供方协议尚未明确。本单元只能建立 provider-neutral 身份反馈与 Review-only 测试闭环，不得假设 OIDC、JWT、智慧就业平台 SSO 或本地密码协议。

## 范围

1. 提供受 Admin 请求与方法授权保护的当前主体只读接口，只返回 `identitySource`、`userId` 和稳定 CMS roles，不返回凭证或用户详情，也不产生业务写审计。
2. 在 Admin Frontend 建立统一身份状态和共享 Admin HTTP adapter；启动检查通过后才呈现工作区，任意 Admin 请求的 `401 / 403` 进入一致反馈，JSON、multipart、binary 请求均保持现有 wire 行为。
3. 全局展示当前主体和角色；按角色裁剪 `super` 专属审计导航，同时保留 Backend 对 direct route / API 的最终授权。
4. 在 Review source set 提供 Server 控制的 `admin` / `super` profile、短期内存会话、随机 opaque credential、到期、退出和主动模拟失效；保留显式配置的双角色自动化 credential。
5. 在隔离 Review Runtime 通过 marker 激活“仅测试”登录入口；移除代理层单一超级身份的 Browser 静默注入。正式 Frontend / Server 组合不得激活或接受 Review 登录。
6. 更新 Local CI、Human Review 启动和操作 Guide，使自动化仍能明确取得 `super` 身份，测试人员可从匿名状态完成登录、刷新、角色切换、退出和失效验证。
7. 建立 Backend unit / MVC / artifact isolation、Frontend build、Browser 和 Fresh Runtime 验证，并回归全部 Admin 业务、审计、Public、Migration 与正式 fail-closed 边界。

## 不在范围

- 智慧就业平台、OIDC、JWT 或其他真实身份提供方适配器；
- CMS 本地账号库、密码登录、注册、找回密码、角色分配或用户管理 UI；
- Production Deployment、Secrets / Credentials 管理或把 Review 会话提升为正式方案；
- 新增 CMS 业务角色、栏目 / 组织 / 租户 / 数据范围授权；
- 修改业务写审计状态机、审计 export / mutation / retention 或外部审计平台；
- agentic-dev 修改。

## 完成条件与验证责任

- 匿名进入、直接 deep link 和刷新不会加载或短暂展示管理数据；正式环境显示等待所属平台认证，Review 环境显示明确的仅测试入口。
- 测试人员可分别以 `admin`、`super` 登录；页面显示的主体和角色来自受保护的 Backend 当前主体接口，不能由 runtime marker、query、local storage 或 profile payload直接伪造。
- `admin` 可使用全部日常管理页面且不显示审计导航；直达审计页面 / API 为 `403`。`super` 可使用审计页面；Public 不要求管理身份。
- 任意页面内 Admin 请求 `401` 后全局进入身份失效状态，`403` 保留主体并显示禁止访问；退出和主动失效使旧短期 credential 立即不可用，刷新不能恢复已退出会话。
- Review 会话有有界 TTL、随机 opaque credential 与有界清理；凭证不进入 DOM、URL、日志、业务审计或持久化。正式 BootJar 不包含 Review Controller / registry / verifier，正式 Server 拒绝 Review Header。
- Admin API inventory 与 HTTP contract 双向一致；当前主体和 Review session endpoint 的成功 / 失败 / role / expiry contract 有自动化覆盖，既有 38 个写入口审计 descriptor 和拒绝无副作用回归保持通过。
- Admin build / Browser 覆盖匿名、登录、deep link、刷新、`admin` / `super`、退出、模拟失效和统一 `401 / 403`；Fresh integrated Runtime 与 Human Review 操作路径可复现，Public Browser / API 不受影响。
- 最终目标 commit 在干净工作树取得 exact-head Full Local Docker CI PASS；完成独立安全 / 隐私 /事务与前端状态复核、History Convergence、本地集成、Post-Integration Evidence、归档和授权范围内的远端推送闭环。

## 依赖与顺序

本单元依赖 EU-69 的授权边界、EU-70 的审计保证和 EU-71 的角色差异页面。实施按一个纵向 Unit 完成：身份 projection 与 Review session → 统一 Frontend 边界 → Browser / Runtime / artifact isolation → 独立复核与集成。该顺序只表达内部依赖，不拆出新的隐含 Execute Authority。

## 就绪门禁

**PASS**（2026-09-30，基线 `7d2682b7608d3d00d28b9758ceb5675a31e2e6d6`）。

- Requirement 与 Specification 已明确全局身份反馈、Review-only 可操作模拟登录、角色、退出 / 失效、正式产物隔离和 Public 不受影响；没有遗留会改变 Product Scope 或验收的高影响歧义。
- Technical owner 已固定当前主体接口、短期 opaque credential、Server-controlled profile、共享 Admin HTTP adapter、runtime marker 与正式 / Review artifact 边界；真实身份协议继续作为独立后续工作，不阻塞 provider-neutral 前端状态与 Review 闭环。
- Scope、Out of Scope、依赖和 Completion Conditions 覆盖 Backend、Frontend、Browser、Fresh Runtime、artifact isolation、敏感凭证与 exact-head / post-integration 证据；一个纵向 Unit 不隐藏跨 Unit 交付责任。
- 本地 `main`、`origin/main` 和 Unit base 均为 `7d2682b7608d3d00d28b9758ceb5675a31e2e6d6`；工作树仅有本单元 Authority / work artifact，目标 local / remote branch 不存在。宿主无 `gh` CLI，不能直接枚举 GitHub PR / Actions，但远端 ref 与本地状态没有发现并发 ownership 冲突，且本仓库日常本地集成不以 PR 为默认机制。
- `node scripts/verify-docs-governance.mjs` 与 `git diff --check` 均 PASS。

此 PASS 只授予 `codex/eu72-admin-identity-feedback` 上的 EU-72 实施与验证，不授予真实身份提供方、CMS 本地账号 / 密码、Production Deployment、审计扩展或 agentic-dev 修改。

## 实施与验证进展

- 已完成受保护的当前主体投影、Review-only 双角色短期会话与人工登录 / 退出 / 模拟失效、同源共享 Admin HTTP adapter、启动身份 gate、全局主体反馈和角色导航裁剪；移除 Browser 代理静默超级身份注入。
- 已完成全部 Admin adapter 接入、正式产物隔离检查、Review runtime / Human Review 启动及 Guide 更新。未改动 schema 或 EU-70 审计事务状态机。
- Dirty full Local Docker CI：`.local-ci/evidence/20260930T034418Z-1469557`，绑定 `e7dd1dea1e715acb10d654fc3f89ffbe534acf40` + fingerprint `191bcccc621635344c169b46925fd0f3fbc408876f7b0f398ec65bea7c8de438`，PASS；Public 60 passed / 7 skipped，Admin 48 passed / 1 skipped，Review 1 passed。此证据不是 exact-head，也不覆盖之后补充的测试与修复。
- 最终补充自然到期 / 容量 / 配置 fail-closed、旧 credential 拒绝、普通管理员写入审计归属、Cookie 仅内容 GET、断网退出及正式 marker 缺失的验证；目标提交和集成后证据尚待生成。

## 安全 / 隐私 / 事务复核与 History Convergence

实现者自查与独立复核分别进行；按 `review-change` 在不继承作者结论的 fresh context 重新恢复 Authority 与 exact diff，没有人工 reviewer。

- 检查了主体来源、方法与请求授权、敏感字段、短期凭证 / Cookie 范围、公开边界、Review BootJar 隔离、审计归属与未触碰的事务边界。发现并修复跨源附加凭证、迟到响应影响新身份和断网退出误报成功风险；补充相应防护及回归。
- 独立 reviewer 对候选 `ab3d1f284a51d5c85366fb58e0a47c0d3c7bc1f4` 发现 P2：Playwright 全局 Header 会将 Review credential 发往外部 iframe，并污染 Public 匿名证据。已停止该轮 CI（`.local-ci/evidence/20260930T071736Z-1514404`，没有 Full PASS），移除全局 Header，改为同源 Admin-only fixture 请求；Admin Browser 从受控当前 tab 身份发送请求，Public / 外部保持匿名。补充 fixture 同源 / 跨源 / redirect、Browser Public / 外部无凭证及业务页面 401 回归。最终修复 diff 的独立复核与 exact-head 全量验证仍为必要完成条件。
- 修复后的独立复核发现 Public 套件中四个混合 Admin UI 场景需要显式启用其 Browser 测试身份；仅这些文件开启同源 `/admin` tab 身份，其余 Public Browser 保持匿名。第二轮候选 CI 因此在 Browser 前暂停，没有 Full PASS；随后继续复核最终 diff。
- Docker 输入检查将 scoped fixture helper 归位各独立 Frontend 应用内，避免单应用 `/work` 挂载下跨包导入失败；实际 CI Playwright 镜像的隔离挂载发现检查通过（Public 67 / Admin 54）。运行 `.local-ci/evidence/20260930T073728Z-1556455` 在 Backend 就绪检查失败；loopback 对照为默认 curl 502、显式直连 200，确认宿主代理路由问题，后续本地验证设置 `NO_PROXY=127.0.0.1,localhost,::1` 与 `no_proxy`，不修改产品认证或授权来迎合该环境。
- 已共享的规划 commit `e7dd1dea1e715acb10d654fc3f89ffbe534acf40` 保留；所有本单元实现、测试、debug 修复和 owner 同步收敛为一个功能 candidate，不为中间运行另建提交。归档为独立 docs-only closure，不改写已共享历史。
- 功能提交 `32708880b996d83712fa09caee821bf5445647c2` 的 exact-head Full Local Docker CI（`.local-ci/evidence/20260930T075404Z-1594255`）及独立复核通过后，已推送任务分支并在本地 fast-forward 到 `main`。集成后默认 Human Review 启动发现 fixture 脚本仍匿名请求 Admin，收到 `401`；显式 `super` 对照为 `200`，确认是移除代理静默身份后的运行控制漏项。闭环重新打开，最低修复为内部脚本显式认证、同步 GitHub fallback 调用并补充 Local CI 回归；该运行控制变化必须重新取得 exact-head Full CI。已共享功能提交保持不变，修复另成逻辑提交。
- 独立复核进一步发现 fallback 仍消费正式 Backend jar / image，不能接受 Review 凭证；已补齐独立 Review image / artifact producer、双角色配置、Browser marker 与 restored Review 消费链，baseline 继续使用正式产物验证 fail closed。本地候选 `bc7699a9a624c227c36c9ab0a6d4cc2cdff6bd25` 的 CI（`.local-ci/evidence/20260930T082006Z-1625298`）在 Backend 阶段主动取消，没有 Full PASS；未共享候选按同一修复目的收敛。远端 Workflow 只取得 YAML / inline shell 与链路复核，不宣称远端 Actions 已运行。
