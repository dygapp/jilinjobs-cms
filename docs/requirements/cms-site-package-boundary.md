# CMS Core / Site Package / Public Renderer 边界需求

## Status

- Candidate source: GitHub Issue #77
- Stage: Requirement Authority — ACTIVE / PARTIALLY IMPLEMENTED
- Completed implementation: EU-37 / EU-38 / EU-39 / EU-40 / EU-41
- Current Ready Execution Unit: **NONE**
- Remaining scope after EU-41: Slice C / Slice D 继续保持 Planning / Requirement Candidate；四层 boundary 完成后再做 Repository Split Readiness Assessment
- Scope: E1～E3 前置的 CMS 通用化、站点实例数据所有权与 Public Renderer 可替换边界

## Intent

在继续 Issue #60 / E1～E3 主站正式内容建设前，项目需要先把当前“CMS 能力”与“吉林就业网站实例”从长期责任上分离。

目标不是为了目录形式而搬文件，也不是立即拆分 Git Repository，而是让以下四类长期事实具有清晰且可独立演进的 Authority：

1. **Generic CMS Core**：可复用的 CMS 数据模型、Schema、Backend Domain、Admin / Public API、Admin UI、`preset` 等通用能力；
2. **JilinJobs Site Package / Provisioning**：吉林就业网站自身的稳定栏目、导航、Page/PageGroup、List / Advertisement Slot 定义、Site Config 实例值、Fresh Site 初始运营默认数据及稳定站点资源；
3. **Historical Content Migration**：Main / Party 历史文章、外链、正文资源、附件、历史运营列表成员等 Canonical Migration Dataset；
4. **Replaceable Public Renderer**：当前 Vue/Vite Public Site 只是稳定 `/api/public/**`、公开 URL 和 Site 数据契约的一个呈现实现，未来可以由其他技术栈替换。

该边界应保证未来增加 Main Site 正式内容时，新增内容进入正确的数据 / Site Authority，而不是继续扩大 CMS Core 对吉林就业网站实例的内建假设。

## Requirements

### 1. Generic CMS Core

1. CMS Core 的 Schema、Backend Domain、Admin 能力和 Public API 不应要求某个具体站点必须存在 `notice`、`party`、`guide`、`MAIN`、`HOME_CAROUSEL` 或吉林就业相关配置值才能成立。
2. `preset` 继续作为通用 CMS 能力保留：它表达“由当前 Site Provisioning 建立并受稳定身份 / 删除保护的结构对象”，而不是“吉林就业专属”能力。
3. Backend Flyway **只承担 Generic CMS Schema evolution 与真正通用的数据库级能力**；具体站点的栏目名称、导航树、联系方式、备案号、具体外链、运营默认数据或视觉资源路径不得占用 / 参与 Backend migration lineage。
4. CMS Core 仍可以提供 Site Provisioning 所需的通用导入、校验、稳定 identity、保护、查询和一次性 bootstrap-state 等能力；这些能力必须 site-neutral，不得内建 JilinJobs package identity 或实例值。
5. Backend Schema migration numbering 必须可以独立演进；Site Package 不得依赖“必须位于 Backend V1 与 V3 之间”等 migration-order contract。

### 2. JilinJobs Site Package / Provisioning

1. 原 `V2__current_preset_data.sql` 中属于吉林就业网站实例的责任必须退出 Backend Flyway，并由独立 Site Package / Provisioning Authority 承载。
2. Site Package 至少能够表达当前站点所需的稳定结构 identity、父子 / 引用关系、默认运营字段、`preset` 身份及必要站点配置值。
3. `site-baseline/static/**` 中稳定视觉资源应归属于具体 Site，而不是归属于 Vue Renderer 或通用 CMS Domain；其最终目录可以调整，但数据所有权必须明确。
4. Site Provisioning 必须可在 Fresh Database 上重复、可验证地恢复当前正式站点稳定结构，并需要定义幂等 / 升级 / 冲突策略，不能依赖一次性人工 SQL 操作。
5. Site Package 应与 Runtime 运营数据区分：管理员后续创建的普通对象、文章、列表成员、展示内容等不因 Site Package 存在而自动变成版本化 preset。
6. **Fresh Site 初始运营默认数据属于 Site bootstrap，不属于 Backend Flyway migration，也不属于长期 stable-structure reconcile。** Bootstrap artifact 应面向当前 CMS Schema 维护，不建立与 Backend migration number 对应的 V1/V2/V3 序列。
7. Site bootstrap 必须具有明确的一次性安装语义：完成后相关 `CmsListItem` / `Advertisement` 成为普通 operator-managed Runtime Data；管理员修改不得被后续 reconcile 覆盖，管理员删除不得被后续启动或显式重复 bootstrap resurrect。
8. 一次性状态应通过 Generic CMS 提供的 site-neutral bootstrap-state capability 记录，而不是借用 `flyway_schema_history`；Site Package 与 CMS 的依赖应表达为 Schema / Provisioning capability compatibility，而不是共享迁移顺序。

### 3. Historical Content Migration

1. `data-migrations/**` 继续只承担历史运营内容迁移，不机械接收栏目、导航、PageGroup、Site Config 定义或 Fresh Site 普通默认运营数据。
2. Main / Party Canonical Migration Dataset 应引用稳定 Site identity（例如栏目 alias、list code），不得依赖临时 Runtime 数据库 ID 或当前 Vue 组件 / Router 内部实现。
3. 更换 Public Renderer 不得要求重新迁移已经接受的 Canonical Content，仅因为框架、构建产物或组件结构发生变化。
4. Fresh Site bootstrap 与 Historical Canonical Migration 必须是不同 lifecycle：前者建立当前站点的初始普通运营默认值，后者承载具有 provenance / fingerprint / source evidence 的历史内容迁移。

### 4. Replaceable Public Renderer

1. EU-36 已接受的 Public source ownership / `/api/public/**` / managed Resource projection 边界继续有效。
2. 当前 `frontend/public-site` 可以继续作为实现模块，但不得成为 Site Definition、Canonical Migration 或 CMS Domain Authority。
3. Public Renderer 可以依赖稳定公开 API、公开 URL、站点数据和站点静态资源；不得要求 Admin API、Flyway 中的站点 SQL 形状或 Vue/Vite 内部结构成为长期内容契约。
4. Main / Party 的产品 identity、Canonical URL、主题和已接受 Public behavior 必须在本次边界收敛中保持不变。

### 5. Integration / Verification

1. Fresh Runtime 验证需要能够证明：Generic CMS Core → JilinJobs stable Site Provisioning → one-time Site bootstrap 可以恢复当前正式 Fresh Site 状态，再叠加 Party / Main Canonical Migration 后得到预期 Runtime 数据。
2. Generic CMS-only Fresh Database 必须可独立成立且不包含任何 JilinJobs Site instance rows。
3. Site bootstrap 必须证明 first apply、repeated apply、ordinary restart、operator mutation/deletion 后 no-overwrite / no-resurrection。
4. 迁移站点 baseline 所产生的最终状态必须与当前已接受 `main` 行为对账；不得把“数据所有权移动”扩大为用户可见产品改版。
5. CI / Review Environment 可以继续由当前单 Repository 编排；是否跨 Repository 组合属于后续独立架构决策。

## Current Evidence

当前 Repository 已取得以下阶段性证据：

- EU-36 已让 Public production source 退出 `/api/admin/**` endpoint knowledge，并明确 Public Renderer 是可替换实现；
- `frontend/public-site` 已有独立 package、build 和 Browser Tests；
- EU-37 已建立 Site Package v1 manifest/schema、narrow provisioner、Fresh V1 Generic Schema MySQL proof、second apply idempotency 与 ownership conflict contract；
- EU-38 已将 Column、PageGroup、Page、NavigationLocation、SiteConfig、CmsList definition、AdvertisementSlot 七类具有 stable identity 的 JilinJobs preset structure 表达进 `sites/jilinjobs/**`；
- EU-39 已为 NavigationItem 引入 provisioning-only nullable stable `code`，把当前 40 条正式 preset NavigationItem 表达为 `sites/jilinjobs/structure/navigation-items.json` 并完成 Legacy adoption / stable-code reconcile 过渡；
- EU-39 final implementation Head `b95285f5424d4df0b9f9943395e80332296754f7` 已通过 Site Package Verification #15、CI #761、Canonical #152、EU-30 Upgrade #102 与人工评审环境 #678；PR #84 已合并为 `main@36276ed65e6f3edbe96ffc18c01cf18ab924837b`，Post-Integration Site Package #16 与 CI #762 全部 PASS；
- EU-40 已把 Site Package reconcile 激活为 `cms.site-package.root` opt-in Spring lifecycle，并让 Repository Runtime、Canonical / Upgrade importer 与 Review Environment 显式消费同一 JilinJobs Site Package；
- EU-40 final Head `b3e3dc8c4855c9e17b2dfa2f190a84d0305162e2` 的 Site Package #19、CI #768、Canonical #153、Upgrade #103、Review #683 全部 PASS；PR #86 合并为 `main@b105e553db1ebbc12a2b6665385b94fb977bea06` 后，Post-Integration Site Package #20 与 CI #769 全部 PASS，EU-40 正式 COMPLETED；
- EU-40 后 audit 已确认原 V2 剩余 6 条 `CmsListItem` + 1 条 `Advertisement` 是 **initial operational defaults**：属于 Fresh JilinJobs site 初始状态，但初始化后由运营管理，不是 stable preset structure，也不是 Historical Canonical Migration；
- Issue #77 comment `#issuecomment-5560955644` 已按该分类修订 Architecture，EU-41 Readiness PASS；
- EU-41 final Head `a958c39a37892cf0fbcb41b8c883b2299d84f561` 的 Site Package #32、CI #783、Canonical #165、Upgrade #115 与 Review #696 全部 PASS；
- PR #88 已合并为 `main@6c88eea1762e8edf465833631cadff1e4c751d36`；Post-Integration Site Package #33 与 CI #784 全部 PASS；
- Backend active Flyway 已为 `V1__current_cms_schema.sql` + `V2__site_provisioning_schema_capabilities.sql`，仅承担 Generic CMS Schema / provisioning capabilities；`V2__current_preset_data.sql` 已退出 active lineage；
- 七条 initial operational defaults 已进入 `sites/jilinjobs/bootstrap/**`，由 `cms_site_bootstrap_state` 提供一次性 completion state；targeted real-MySQL evidence 已证明 repeat guard 与 no-overwrite / no-resurrection；
- Canonical #165、Upgrade #115 与 Review #696 已证明 183 篇 Party current canonical Runtime Dataset、4 条 accepted carousel、幂等导入与 EU-29→EU-30 upgrade knowledge 在新 lifecycle 下保持独立兼容；
- `site-baseline/static/**` 与最终 asset composition / E1～E3 re-entry 仍属于 Issue #77 后续边界；
- `data-migrations/README.md` 已明确 Historical Content Migration 与 Flyway / Site Baseline 分离。

## Non-goals / Deferred

本 Requirement 当前明确不做：

- 不选择新的 Public Frontend 技术栈；
- 不重写 Vue Public Site；
- 不改变 ADR-0002 的当前 Multi-entry SPA 决策；
- 不为了形式统一做全仓库目录大搬迁；
- 不预设必须拆成多个 Git Repository；
- 不立即引入 Git Submodule；
- 不把文档、CMS Core、Site Package、Public Renderer 机械拆成独立 Repository；
- 不把 Site Provisioning 或 Fresh Site bootstrap 错误并入 Historical Content Migration；
- 不为 Site bootstrap 创建独立 migration version sequence；
- 不在本 Requirement 中执行 Issue #60 / E1、E2、E3 的正式内容建设；
- 不顺带处理 C1 / C2、Browser Compatibility 或 Issue #57 的 Public Rendering Architecture 讨论；
- 不在没有第二个真实 Site / Renderer Consumer 证据时引入过度通用的插件框架、部署框架或多站点产品模型。

## Current Follow-up Direction

EU-37 / EU-38 / EU-39 / EU-40 / EU-41 已完成 Site Package Foundation、stable structure、Navigation stable identity / reconcile、explicit Runtime composition activation 与 Site bootstrap / Generic Schema baseline separation，并均已集成到 `main` 取得 Post-Integration Current Evidence。

当前没有 Ready Execution Unit。Operational Seed Classification & V2 Responsibility Retirement 已由 EU-41 关闭。

Issue #77 当前剩余 Planning Candidate 重点为：

1. **Slice C — Site Asset Ownership & Runtime Composition**：`site-baseline/static/**` 的 Site Package ownership 如何在 manifest / Runtime / CI / Review Environment 中显式组合，物理移动仅在有真实必要时执行；
2. **Slice D — Canonical Migration Compatibility & E1～E3 Re-entry**：Party canonical migration、183 篇 current Runtime Dataset 与 accepted carousel state 如何在最终 Site Package + asset lifecycle 下关闭完整 compatibility，并判断 Issue #60 / E1～E3 是否解除前置等待；
3. **Repository Split Readiness Assessment**：仅在四层 boundary 完成后独立执行，不自动拆仓。

上述方向当前都不是 Ready Execution Unit，也不继承 EU-41 的 Execute 授权。下一步必须基于最新 `main` 重新执行 current audit / slice-work / readiness-check。