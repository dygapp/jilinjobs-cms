# CMS Core / Site Package / Public Renderer 边界需求

## Status

- Candidate source: GitHub Issue #77
- Stage: Requirement Authority — ACTIVE / PARTIALLY IMPLEMENTED
- Completed implementation: EU-37 / EU-38 / EU-39 / EU-40
- Current Ready Execution Unit: NONE
- Remaining scope after EU-40: Operational Seed Classification & V2 Responsibility Retirement / Slice C / Slice D 继续保持 Planning / Requirement Candidate
- Scope: E1～E3 前置的 CMS 通用化、站点实例数据所有权与 Public Renderer 可替换边界

## Intent

在继续 Issue #60 / E1～E3 主站正式内容建设前，项目需要先把当前“CMS 能力”与“吉林就业网站实例”从长期责任上分离。

目标不是为了目录形式而搬文件，也不是立即拆分 Git Repository，而是让以下四类长期事实具有清晰且可独立演进的 Authority：

1. **Generic CMS Core**：可复用的 CMS 数据模型、Schema、Backend Domain、Admin / Public API、Admin UI、`preset` 等通用能力；
2. **JilinJobs Site Package / Provisioning**：吉林就业网站自身的稳定栏目、导航、Page/PageGroup、List / Advertisement Slot 定义、Site Config 实例值及稳定站点资源；
3. **Historical Content Migration**：Main / Party 历史文章、外链、正文资源、附件、运营列表成员等 Canonical Migration Dataset；
4. **Replaceable Public Renderer**：当前 Vue/Vite Public Site 只是稳定 `/api/public/**`、公开 URL 和 Site 数据契约的一个呈现实现，未来可以由其他技术栈替换。

该边界应保证未来增加 Main Site 正式内容时，新增内容进入正确的数据 / Site Authority，而不是继续扩大 CMS Core 对吉林就业网站实例的内建假设。

## Requirements

### 1. Generic CMS Core

1. CMS Core 的 Schema、Backend Domain、Admin 能力和 Public API 不应要求某个具体站点必须存在 `notice`、`party`、`guide`、`MAIN`、`HOME_CAROUSEL` 或吉林就业相关配置值才能成立。
2. `preset` 继续作为通用 CMS 能力保留：它表达“由当前 Site Provisioning 建立并受稳定身份 / 删除保护的结构对象”，而不是“吉林就业专属”能力。
3. Flyway 应主要承担 CMS Schema 与真正属于通用 CMS Core 的数据库级稳定基线，不应把具体站点的栏目名称、导航树、联系方式、备案号、具体外链或视觉资源路径当作 CMS Domain 固有事实。
4. CMS Core 仍可以提供 Site Provisioning 所需的通用导入、校验、稳定 identity、保护和查询能力；不得因为抽象为通用 CMS 而削弱当前产品已经接受的行为和安全边界。

### 2. JilinJobs Site Package / Provisioning

1. 当前 `V2__current_preset_data.sql` 中属于吉林就业网站实例的站点结构和配置，应由独立 Site Package / Provisioning Authority 承载，而不是永久与 CMS Schema baseline 绑定。
2. Site Package 至少能够表达当前站点所需的稳定结构 identity、父子 / 引用关系、默认运营字段、`preset` 身份及必要站点配置值。
3. `site-baseline/static/**` 中稳定视觉资源应归属于具体 Site，而不是归属于 Vue Renderer 或通用 CMS Domain；其最终目录可以调整，但数据所有权必须明确。
4. Site Provisioning 必须可在 Fresh Database 上重复、可验证地恢复当前正式站点结构，并需要定义幂等 / 升级 / 冲突策略，不能依赖一次性人工 SQL 操作。
5. Site Package 应与 Runtime 运营数据区分：管理员后续创建的普通对象、文章、列表成员、展示内容等不因 Site Package 存在而自动变成版本化 preset。

### 3. Historical Content Migration

1. `data-migrations/**` 继续只承担历史运营内容迁移，不机械接收栏目、导航、PageGroup、Site Config 定义等 Site Provisioning 数据。
2. Main / Party Canonical Migration Dataset 应引用稳定 Site identity（例如栏目 alias、list code），不得依赖临时 Runtime 数据库 ID 或当前 Vue 组件 / Router 内部实现。
3. 更换 Public Renderer 不得要求重新迁移已经接受的 Canonical Content，仅因为框架、构建产物或组件结构发生变化。

### 4. Replaceable Public Renderer

1. EU-36 已接受的 Public source ownership / `/api/public/**` / managed Resource projection 边界继续有效。
2. 当前 `frontend/public-site` 可以继续作为实现模块，但不得成为 Site Definition、Canonical Migration 或 CMS Domain Authority。
3. Public Renderer 可以依赖稳定公开 API、公开 URL、站点数据和站点静态资源；不得要求 Admin API、Flyway 中的站点 SQL 形状或 Vue/Vite 内部结构成为长期内容契约。
4. Main / Party 的产品 identity、Canonical URL、主题和已接受 Public behavior 必须在本次边界收敛中保持不变。

### 5. Integration / Verification

1. Fresh Runtime 验证需要能够证明：Generic CMS Core + JilinJobs Site Package 可以恢复当前正式站点结构，再叠加 Party / Main Canonical Migration 后得到预期 Runtime 数据。
2. 迁移站点 baseline 所产生的最终状态必须与当前已接受 `main` 行为对账；不得把“数据所有权移动”扩大为用户可见产品改版。
3. CI / Review Environment 可以继续由当前单 Repository 编排；是否跨 Repository 组合属于后续独立架构决策。

## Current Evidence

当前 Repository 已取得以下阶段性证据：

- EU-36 已让 Public production source 退出 `/api/admin/**` endpoint knowledge，并明确 Public Renderer 是可替换实现；
- `frontend/public-site` 已有独立 package、build 和 Browser Tests；
- EU-37 已建立 Site Package v1 manifest/schema、narrow provisioner、Fresh V1 Generic Schema MySQL proof、second apply idempotency 与 ownership conflict contract；
- EU-38 已将 Column、PageGroup、Page、NavigationLocation、SiteConfig、CmsList definition、AdvertisementSlot 七类具有 stable identity 的 JilinJobs preset structure 表达进 `sites/jilinjobs/**`；
- EU-38 已证明 Fresh `V1 + Site Package` 与 Legacy `V1+V2 + Site Package` 在上述 stable structural scope 上等价；
- EU-39 current audit 已证明 NavigationItem 的 `name`、parent/location、sort 与 target 都属于可变字段，现有字段组合不能形成长期稳定且无歧义的 logical identity，因此正式引入 provisioning-only nullable stable `code`；
- EU-39 已把当前 40 条正式 preset NavigationItem 表达为 `sites/jilinjobs/structure/navigation-items.json`，并证明 Fresh create、Legacy V2 原位无歧义 adoption、second apply idempotency、stable-code rename/move/reorder/retarget restore 与 ambiguous-adoption transaction rollback；
- EU-39 final implementation Head `b95285f5424d4df0b9f9943395e80332296754f7` 已通过 Site Package Verification #15、CI #761（Backend / Public / Admin / Integrated Browser）、Canonical #152、EU-30 Upgrade #102 与人工评审环境 #678；
- PR #84 已合并为 `main@36276ed65e6f3edbe96ffc18c01cf18ab924837b`；Post-Integration Site Package Verification #16 与 CI #762（Backend / Public / Admin / Integrated Public/Admin Browser）全部 PASS，EU-39 正式 COMPLETED；
- EU-40 已把 Site Package reconcile 激活为 `cms.site-package.root` opt-in Spring lifecycle，并让 Repository Runtime、Canonical / Upgrade importer 与 Review Environment 显式消费同一 JilinJobs Site Package；
- EU-40 final Head `b3e3dc8c4855c9e17b2dfa2f190a84d0305162e2` 的 Site Package #19、CI #768、Canonical #153、Upgrade #103、Review #683 全部 PASS；PR #86 合并为 `main@b105e553db1ebbc12a2b6665385b94fb977bea06` 后，Post-Integration Site Package #20 与 CI #769 全部 PASS，EU-40 正式 COMPLETED；
- operator-created NavigationItem 继续保持 `preset=false / code=NULL`，CmsListItem / Advertisement operational members 未被 Site Package 接管；
- `V2__current_preset_data.sql` 在 EU-40 仍保持不变；正式 Runtime / importer 已可显式组合 Site Package，但 V2 compatibility responsibility **尚未移除**，operational seed 的长期 Authority 仍待分类；
- `site-baseline/static/**` 与完整 canonical migration compatibility / E1～E3 re-entry 仍属于 Issue #77 后续边界；
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
- 不把 Site Provisioning 错误并入 Historical Content Migration；
- 不在本 Requirement 中执行 Issue #60 / E1、E2、E3 的正式内容建设；
- 不顺带处理 C1 / C2、Browser Compatibility 或 Issue #57 的 Public Rendering Architecture 讨论；
- 不在没有第二个真实 Site / Renderer Consumer 证据时引入过度通用的插件框架、部署框架或多站点产品模型。

## Current Follow-up Direction

EU-37 / EU-38 / EU-39 / EU-40 已完成 Site Package Foundation、stable structure、Navigation stable identity / reconcile 与 explicit Runtime composition activation，并均已集成到 `main` 取得 Post-Integration Current Evidence。当前没有 Ready Execution Unit。

Issue #77 在 EU-40 后剩余工作必须重新经过 current audit / slice-work / readiness-check，重点包括：

1. V2 中仍存在的 `CmsListItem` / `Advertisement` 等 operational seed 应如何分类长期 Authority，并在不破坏 Existing/Fresh/canonical behavior 的前提下收敛 V2 Site-specific compatibility responsibility；
2. `site-baseline/static/**` 的 Site Package ownership 如何在 manifest / Runtime / CI / Review Environment 中显式组合，物理移动仅在有真实必要时执行；
3. Party canonical migration、183 篇 current Runtime Dataset 与 accepted carousel state 如何证明对最终 Site Package lifecycle contract 完整兼容；
4. 完成上述 compatibility 后，如何解除 Issue #60 / E1～E3 的前置等待并重新进入 Planning；
5. 四层 boundary 完成后再单独执行 Repository Split Readiness Assessment。

这些剩余方向当前都不是 Ready Execution Unit，也不继承 EU-40 的 Identifier 或 Execute 授权。
