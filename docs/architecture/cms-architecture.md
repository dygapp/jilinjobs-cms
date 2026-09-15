---
id: architecture-cms
title: CMS 产品与系统长期架构
type: architecture-state
status: active
relations:
  requirements:
    - docs/requirements/information-publishing.md
    - docs/requirements/cms-domain.md
  decisions:
    - docs/architecture/decisions/ADR-0001-admin-frontend-module-integration.md
    - docs/architecture/decisions/ADR-0002-public-site-multi-entry-modular-spa.md
    - docs/architecture/decisions/ADR-0003-public-shared-shell-components.md
    - docs/architecture/decisions/ADR-0004-public-shared-column-page.md
updated_at: 2026-09-15
---

# CMS 产品与系统长期架构

## 1. 文档责任

本文是 `jilinjobs-cms` 当前跨 Feature 长期 Architecture Context / State owner，描述多个 Feature 在可靠 Specification 前共同依赖的系统边界、数据 ownership、应用职责与可替换关系。

本文不保存 Feature-local HOW、Migration 精确编号、源码文件清单、当前 Execution Unit、PR / Actions 或短期 implementation inventory。具体历史决策的背景、替代方案与权衡由 ADR 保留；当前代码事实由 Repository implementation 提供。

## 2. Architecture drivers

当前架构必须同时满足：

1. Generic CMS capability 不硬编码 JilinJobs 具体站点内容；
2. JilinJobs 稳定站点定义能够版本化恢复，但 ordinary operator content 不被持续覆盖；
3. Historical Migration 保持 provenance / fingerprint / idempotency，而不成为 Site Definition；
4. Admin、Public 与 Migration 使用同一 CMS Domain，但不互相吞并 application responsibility；
5. Public Renderer 可以替换，不反向成为 CMS Product/Data Authority；
6. Main 与 Party 保持真实的 Site/Theme/route boundary，同时只共享已经被产品事实证明稳定一致的能力；
7. Page 可以支持 Rich、Structured、Engineering / External 等内容 profile，而不引入隐式 alias/path 特判；
8. configuration、stable assets、Runtime uploads 与 historical resources 有明确 ownership；
9. 架构演进不通过复制 Requirement / Specification / Technical 三套相同事实维持一致性。

## 3. System context

当前主要系统责任：

```text
CMS Operator
    ↓
Admin Application
    ↓
CMS Server / Generic CMS Core
    ↓
Runtime CMS Data
    ↓
Public Contracts
    ↓
Public Renderer

Versioned JilinJobs Site Definition ──→ Generic CMS Core / Runtime composition
Historical Content Migration ────────→ Generic CMS Core / Runtime data
Legacy Sources ──(acquisition only)──→ Canonical Migration Data
```

公开访问者只消费 Public Renderer 与公开 contracts；运营人员通过 Admin 维护 Runtime 内容。Legacy Source 不属于稳定 Runtime dependency。

## 4. Data / authority layers

长期 responsibility chain：

```text
Generic CMS Core
        ↓ capability
JilinJobs Site Definition
        ↓ stable structure / accepted defaults
Historical Content Migration
        ↓ optional canonical import
Runtime CMS Data
        ↓ public contracts
Replaceable Public Renderer
```

这些责任可以物理同仓，但 source ownership 与 lifecycle 必须独立。

### 4.1 Generic CMS Core

Generic CMS Core 提供 site-neutral：

- CMS Domain 与业务校验；
- persistence / transaction；
- Generic schema evolution；
- Admin / Public contract capability；
- stable structure provisioning capability；
- one-time bootstrap completion capability；
- StaticResource / resource protection capability；
- canonical historical migration primitives；
- Page content model / renderer identity / content ownership 的通用表达能力。

Generic Core 不拥有 JilinJobs aliases、站点文案、具体导航成员、具体 ListItem、正式 Page 文案、联系方式、Party dataset 或其他 instance-specific values。

### 4.2 JilinJobs Site Definition

JilinJobs Site Definition 是版本化产品基线，当前 Repository owner 位于 `sites/jilinjobs/**`。

责任分为：

```text
stable structure
one-time initial Runtime defaults
stable site assets
```

架构语义：

- stable structure 可以按稳定 identity reconcile；
- stable default content 只在 Fresh create 或明确 adoption precondition 下进入 Existing Runtime；
- one-time defaults 成功初始化后成为 ordinary operator-managed Runtime Data，不因普通 restart/reconcile 重放；
- stable asset 可以投影到 Runtime public namespace并受到保护；
- Site Definition 不吸收 Historical Article provenance。

具体文件格式和当前 manifest 字段属于 Technical / implementation contract。

### 4.3 Historical Content Migration

Historical Migration owner 位于 `data-migrations/**` 及对应 migration application capability。

职责：

- canonical historical data；
- source provenance；
- legacy stable identity；
- fingerprint；
- resource integrity；
- explicit compatibility；
- import / idempotency / conflict reporting。

长期运行不依赖 Legacy Source 网络。Acquisition 与 stable import 是两个阶段；只有显式 source-discovery / retry / reactivation activity 可以重新访问 Legacy Source。

Generic migration capability 不持有 Main / Party 具体 dataset facts。Party accepted transition 等 site-specific compatibility 继续由对应 migration scope ownership 管理。

### 4.4 Runtime CMS Data

Runtime CMS Data 是当前运营状态，包括 operator-created/edited content、普通配置和 mutable uploads。

Runtime 不是 Repository baseline 的镜像：

- operator divergence 必须受保护；
- one-time bootstrap rows 初始化后由 operator lifecycle 持有；
- stable structure reconcile 不得顺带 resurrect ordinary Runtime rows；
- Historical Migration 只能按 canonical identity / fingerprint contract受控写入。

### 4.5 Replaceable Public Renderer

Public Renderer 只消费稳定 Public contracts 与 Runtime-visible resources。

它不拥有：

- CMS business object definition；
- Site Definition source；
- Historical Migration canonical data；
- Admin workflow；
- migration identity / fingerprint。

替换 Public implementation 时必须保持当前 Product Requirement、Domain semantics 与 canonical URL，除非新的 Requirement / Architecture Decision明确改变它们。

## 5. Backend application boundary

当前 Backend 采用 shared core + two application responsibilities：

```text
CMS Server ──────────→ Generic CMS Core
Content Migration ──→ Generic CMS Core
```

### 5.1 Generic CMS Core

承担两个 application 共同需要的 domain、persistence、transaction、schema/resource/provisioning/migration primitives 与 shared configuration capability。

Core 不依赖任何具体 application。

### 5.2 CMS Server

承担：

- Admin/Public HTTP transport；
- ordinary server lifecycle；
- Runtime static exposure；
- server-only composition。

Server 不拥有 Historical Migration command lifecycle。

### 5.3 Content Migration application

承担：

- canonical migration command / CLI lifecycle；
- site-neutral migration engine usage；
- bounded site-specific adapter / compatibility orchestration。

Migration application 不依赖 CMS Server application，也不通过 Server root composition间接取得 Controller / MVC responsibility。

当前是否为 executable JAR、具体 Gradle module path、command name 与 process wiring由 implementation持有；Architecture 只约束依赖方向和责任边界。

## 6. Schema 与 initialization boundary

Generic schema evolution 与 JilinJobs instance initialization 必须分离。

长期顺序：

```text
Generic schema ready
→ stable Site Definition composition
→ stable asset projection
→ optional one-time initial Runtime defaults
→ optional Historical Canonical Migration
→ ordinary Runtime
```

Architecture 只要求：

- Generic schema 不写 JilinJobs instance rows；
- schema evolution 在当前 accepted baseline 后保持 append-only；
- exact active migration file list由 Repository migration corpus 自己拥有；
- tests / docs 不应维护第二套 active migration inventory；
- Historical content不进入 Generic schema migration；
- Site one-time bootstrap 不借用 Generic schema migration numbering。

## 7. Page Content Architecture

Page Content Architecture 由三个正交维度构成：

```text
content model
renderer identity
content ownership
```

### 7.1 Content model

用于表达 primary body/data shape，例如 Rich Text、Structured 或 CMS 不持有 whole-page body 的 profile。

### 7.2 Renderer identity

Public Renderer 通过显式稳定 identity 选择 renderer。Renderer resolution 不允许依赖 Page alias、URL、DOM shape 或正文 heuristic。

Unknown renderer 必须进入可诊断 unsupported state，不 fallback 到 arbitrary Rich renderer。

### 7.3 Content ownership

用于表达 primary content authority，例如 operator、versioned Site Definition、engineering implementation 或 external integration。

Site default 与 Runtime operator content不能同时成为当前 primary body owner。

### 7.4 Structured content

Structured schema 只在结构本身属于产品语义时使用。当前已接受的 card collection 是一个 bounded schema，不因此建立 generic Page Builder / arbitrary block framework。

Schema 的具体 JSON 字段、当前 representative Page、DTO 与 renderer registry implementation属于 Feature Specification / Technical / code，不在 Architecture Context 重复维护。

## 8. Admin architecture

依据 ADR-0001，Admin 当前采用模块化 SPA，而不是 runtime microfrontend。

Architecture state：

- 一个 Admin Application / Shell；
- 业务模块拥有自己的 route / navigation / feature implementation；
- Shell 只消费明确 module contract；
- shared primitives 与 module-private UI 分离；
- Module Federation 不是当前依赖；
- 只有出现独立发布 / 部署、跨团队或跨技术栈等真实 driver 时重新评估。

当前 CMS 是 Admin 中已实现的业务模块；不存在当前 Authority 支撑的其他业务模块名称时，不预先发明它们。

## 9. Public architecture

依据 ADR-0002～ADR-0004，当前 Public Architecture 是按真实 Site / Theme Boundary 划分的 Multi-entry Modular SPA。

### 9.1 Main / Party boundary

Main 与 Party 当前：

- 同一 Public application package / build lifecycle；
- 不同 Site Entry / Router namespace / theme；
- 共用同一 Backend public contracts；
- 不使用 runtime Module Federation；
- Party 的独立呈现边界不改变其业务上属于 Main 信息架构专题入口的 Requirement。

### 9.2 Shared public capability

共享边界只接收已由产品事实证明稳定一致的 responsibility。

当前 accepted shared responsibility 包括：

- API transport / common public DTO primitives；
- resource / metadata utility；
- Navigation / Footer shared shell；
- Main / Party 二级栏目列表的 shared presentation primitive；
- 已确认完全一致的无主题行为 lifecycle，例如共享 carousel state machine。

Site-specific Header/Banner、首页区块、内容主题、route scope 与仍有真实差异的页面保持各自 owner。

Shared 不是“代码相似就抽取”。若产品信息架构或交互出现真实差异，先 Requirement Change，不通过局部 CSS / DOM hack 静默分叉。

### 9.3 Replaceability

Public source不能包含 Admin CRUD responsibility，也不能要求 Public client理解 Admin-only resource route。

Formal CMS data、historical content、canonical URL 与 resource identity不能依赖 Vue component name、Router internal、Vite bundle shape等当前 implementation details。

SSR / SSG / Hybrid / replacement framework仍是未来 architecture decision，不由当前 replaceability原则预选。

## 10. Configuration ownership

长期配置责任按变化来源划分：

### Code / domain constants

稳定领域 identity、安全 / protocol rules、页面模板 contract、没有运营价值的算法参数。

### CMS Runtime data / SiteProperty

运营人员需要维护的数据和低风险站点行为参数。

### CMS metadata

低频结构 definition，需要受控但不需要独立 DB lifecycle。

### Deployment configuration

DB、port、storage root、instance address、安全环境参数等部署差异。

### Site Definition

JilinJobs 版本化稳定产品定义与 stable assets。

### CI / deployment variables

Actions、Review Environment、proxy/domain 与 runner/runtime-specific参数。

“出现 literal”本身不构成配置化理由。只有变化来源 / owner 与当前持有位置不一致时才需要迁移。

## 11. Resource ownership

Resource ownership 分为三类：

```text
stable Site assets
mutable Runtime uploads
Historical Migration resources
```

Architecture invariants：

- stable Site assets 由 versioned Site Definition持有并可投影到 Runtime public namespace；
- mutable uploads 由 CMS Runtime持有，不进入 stable asset reconcile；
- historical resources跟随 canonical migration provenance，不因被某个页面引用就自动提升为 stable asset；
- direct Runtime reference 与 stable asset ownership都应参与 protected-resource calculation；
- ordinary reference change不自动 physical delete旧资源。

具体 Runtime path、catalog format与 storage implementation属于 Technical / deployment contract。

## 12. Public/Admin contract boundary

Admin 与 Public 可以消费同一 Domain，但 contract responsibility不同：

- Admin contract 支持 authoring、management、validation feedback与受控 mutation；
- Public contract 只暴露公开呈现需要的数据；
- Public contract不得泄漏 Admin-only endpoint responsibility；
- Public client不负责修复 Backend public projection缺口；
- Frontend DTO / adapter不能成为第二份 Domain Authority。

## 13. Authentication / authorization boundary

当前 CMS Runtime 尚没有完整统一认证授权体系。

Architecture 不通过虚构当前用户或角色填补该空白。Future auth/permission属于独立 Requirement / Architecture工作；当前 preset protection、immutable identity、resource safety等是 domain/data integrity，不等同于 role-based authorization。

## 14. Verification architecture

验证必须按真实 ownership组合 Runtime：

```text
Generic schema
→ Site Definition
→ stable assets
→ optional one-time defaults
→ optional canonical migration
→ application / browser verification
```

Test fixture只建立测试场景数据，不重建第二份站点 baseline。

验证策略可以规定风险层与 evidence contract，但不长期复制当前 migration file list、具体 resource counts或其他能从 Repository直接恢复的高频 inventory。

## 15. Architecture invariants

未来 Feature / refactor 不得在没有新的 Requirement / Architecture Authority 时破坏：

1. Generic Core 与 JilinJobs-specific definition 分离；
2. Site Definition、ordinary Runtime、Historical Migration lifecycle 分离；
3. CMS Server 与 Content Migration application都只依赖 shared Core，不互相依赖；
4. Public Renderer可以替换但不拥有 CMS Domain / Site Definition / migration data；
5. Main / Party Site boundary不因代码去重消失，也不因主题不同机械拆成独立 Repository；
6. shared public capability必须来自真实稳定共同责任，不从偶然代码相似推导；
7. Page renderer dispatch显式、fail-closed，不依赖 alias/path heuristic；
8. operator content不被 ordinary reconcile覆盖；
9. Generic migration capability不吸收具体 site dataset / compatibility facts；
10. Product / Domain Authority不缓存 active implementation inventory。

## 16. ADR relationship

当前 ADR 保留历史决策与权衡：

- ADR-0001：Admin modular SPA / 可演进微前端边界；
- ADR-0002：Main / Party Multi-entry Modular SPA；
- ADR-0003：Navigation / Footer 进入 shared shell，定向修正 ADR-0002 的默认 shared boundary；
- ADR-0004：Main / Party 二级栏目列表进入 shared presentation primitive，定向修正 ADR-0003 的默认判断。

本文描述这些决策叠加后的**当前 Architecture State**。后续若改变这些 decision 的核心 trade-off，应新增或 supersede ADR，而不是只修改本文抹去历史原因。