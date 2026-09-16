---
id: requirement-information-publishing
title: 信息发布与网站服务产品需求
type: business-requirement
status: confirmed
version: "V6.0"
classification:
  - l1-06
  - l2-28
  - l2-29
relations:
  domain:
    - docs/requirements/cms-domain.md
  index:
    - docs/requirements/index.md
  architecture:
    - docs/architecture/cms-architecture.md
  related:
    - docs/specifications/admin-site.md
    - docs/specifications/public-site.md
    - docs/specifications/page-content.md
    - docs/specifications/rich-text-authoring.md
    - docs/specifications/content-migration.md
updated_at: 2026-09-16
---

# 信息发布与网站服务产品需求

## 1. 文档责任

本文是 `jilinjobs-cms` 当前信息发布与网站服务的 **Product Requirement fact owner**，回答“产品为什么存在、面向谁、当前提供哪些长期业务能力、哪些产品级边界和验收不变量必须持续成立”。

CMS business object、stable/source identity、state / lifecycle、content ownership、Historical Migration fingerprint / compatibility 等领域事实统一由 `docs/requirements/cms-domain.md` 持有；本文件只引用这些 Domain 结果，不复制 Domain 状态机或 identity policy。Requirement 路由由 `docs/requirements/index.md` 持有。

本文不维护数据库 migration 编号、源码目录、前端组件、框架装配、Site Package 物理格式、Historical canonical record inventory、Execution Unit、PR / Actions 或其他实现状态。Feature 的具体 Observable Behavior / Failure Behavior / Acceptance 由 Specification 持有；跨 Feature 的长期系统结构由 Architecture 持有。

## 2. 产品目标

吉林省高等学校毕业生就业信息网的信息发布与网站服务需要同时满足：

1. 面向社会公众稳定提供中心主站的信息发布、政策与服务内容；
2. 在主站信息架构下提供“中心党建”专题入口及独立红色视觉表达；
3. 为运营人员提供可持续维护内容、内容结构、运营展示、网站属性和资源的 CMS 能力；
4. 在不复制旧站前端实现的前提下，保持已经确认的主要信息架构、视觉识别与公共访问习惯，并完成必要现代化适配；
5. 让稳定工程设计、版本化站点定义、持续运营数据与历史数据具有可区分责任，不为了“可配置”把所有页面结构、视觉或固定集成都变成 CMS 数据；
6. 让正式内容、稳定站点定义、历史数据与公开呈现可以独立演进，不把某个具体 Frontend / Backend implementation 变成产品事实来源。

## 3. 用户与责任主体

### 3.1 公开访问者

公开访问者可以浏览 Main 与中心党建公开内容，通过稳定 URL 访问栏目、文章、单页和专题入口，并按照业务对象语义进入站内详情或外部权威来源。

### 3.2 CMS 运营人员

运营人员通过管理端维护当前接受的 CMS 内容、内容结构、运营展示、网站属性和受控资源。当前阶段不依赖完整用户、角色和权限体系；未来认证授权属于独立 Requirement，不能由临时 UI identity 假设提前成为产品事实。

### 3.3 项目维护者

项目维护者负责稳定页面设计、工程集成、版本化 Site Definition、Historical Migration 与运行环境。维护者不能为了实现便利改变公开访问者或运营人员已经接受的产品语义。

## 4. 产品范围

### 4.1 当前范围

当前信息发布与网站服务包含：

- 中心主站公开前台；
- 主站“中心党建”专题入口、栏目、文章与独立红色视觉主题；
- CMS 管理端；
- 栏目、文章、单页与单页分组；
- 导航位置与导航条目；
- 通用列表与列表项；
- 宣传展示位与展示内容；
- 网站属性；
- 静态资源；
- 稳定公开 URL、站内跳转与外链行为；
- Rich Text 内容编辑与安全呈现；
- 结构本身具有产品语义时的 bounded Structured Page capability；
- Fresh Site 所需稳定结构、稳定资源与必要一次性初始运营数据；
- 在具备明确 Authority 时的 Historical Content Migration 与可追溯导入；
- 面向公开访问的响应式、基础可访问性、基础浏览器兼容与基础搜索引擎友好体验。

### 4.2 当前不在范围

当前阶段不自动建设：

- 完整用户、账号、角色、统一认证与权限体系；
- 多级内容审核；
- 评论、点赞、收藏、投稿、留言、工单；
- 通用全文搜索；
- 通用可视化 Page Builder / arbitrary block framework；
- 任意多租户 / 任意多站点 CMS 平台；
- MQ、Redis、MinIO 等没有当前真实需求的基础设施扩张；
- Production deployment topology、正式发布平台或部署体系重构；
- 没有证据支撑的 Party-specific CMS model 或独立后台；
- 因未来可能替换前端而预先建设无证据的插件框架、Repository split 或独立部署；
- 未经独立 Feature Requirement / Specification 授权的真实第三方 iframe / Runtime integration。

## 5. 长期产品能力

### 5.1 CMS 内容运营

CMS 必须让运营人员以业务语言维护 Article、Page、CmsList 等正式内容，并在 Column、PageGroup、NavigationLocation、CmsList、AdvertisementSlot 等明确上下文中完成日常运营。

产品层只要求运营人员能够维护“什么内容、所属业务上下文、顺序、启停、发布/有效状态、目标和必要资源”；具体 object identity、source identity、状态转换和关系约束由 Domain Authority 定义。

运营界面使用“栏目、文章、单页、单页分组、列表、位置标识、属性标识、资源”等运营人员可理解的业务术语；数据库表名、Migration、Frontend / Backend module、内部类名等实现术语不得成为操作语义。

### 5.2 站点结构、运营展示与站点属性

栏目与导航承担公开内容结构；通用列表与宣传展示承担稳定页面区域中的运营投放；网站属性承担少量需要 Runtime 维护的站点级信息和低风险行为参数；静态资源能力承担受控资源的上传、选择、替换和清理。

CMS 维护“数据是什么”；页面布局、视觉尺寸、主题组合与无需运营维护的固定工程 seam 不因为存在 CMS 而自动成为可配置业务对象。

### 5.3 Main 公开体验

Main 继续以吉林省高等学校毕业生就业信息网已经确认的信息架构和主要视觉识别为业务参照，长期提供：

- 首页；
- 栏目列表；
- INTERNAL Article 详情；
- Page 与 PageGroup member；
- 固定第三方业务集成入口 / seam；
- 主导航、快捷入口、轮播、资讯区域、招聘日历、招聘活动宣传、招聘公告、业务指南、网站链接与 Footer 等已确认页面能力。

需要持续运营的数据由 CMS 正式业务对象提供；无需运营维护的固定页面结构、装饰和第三方 seam 可以继续由工程实现承担。一旦真实需求要求运营维护，必须通过 Requirement Change 决定是否进入 CMS，而不是从已有页面结构反推配置需求。

### 5.4 中心党建（Party）

“中心党建”是 Main 信息架构下具有独立红色视觉主题的专题入口，不是第二个独立业务网站。

当前长期产品事实：

- Main 主导航可以进入 `/party/`；
- `/party/**` 保持独立主题和内容作用域；
- 高层声音、工作动态、党规党章、理论学习、主题教育属于已确认 Party 内容范围；
- “学习园地”是党规党章与理论学习的页面分组，不新增独立内容类型；
- 主题教育可通过其栏目、投放等进入 Party 内容体验，但不因此成为入口页第五个固定内容区；
- Party 继续复用通用 CMS Domain，不因红色主题新增第二套 Column / Article / Page / List model；
- Main / Party 的公共 Navigation / Footer 保持相同业务信息，同时允许不同主题与页面设计。

### 5.5 Historical content onboarding

产品允许在具有明确 Authority、provenance 与可审计边界时，把接受的 Legacy content 转换为当前 Runtime content；该能力属于离线、受控的数据进入过程，不是 Public Runtime dependency。

具体 legacy identity、fingerprint、preflight、compatibility、Main / Party accepted migration scope 与冲突语义由 `cms-domain.md` 的 Historical Migration owner 统一定义。Concrete canonical records、digest 与 source evidence 由 `data-migrations/**` 持有，不在 Product Requirement 复制。

未解决、缺失、source-defect、deferred 或待客户确认的历史数据不得为了“全绿”而静默猜测、删除、覆盖或自动导入；是否接受、修正、延期或重新获取必须由对应 Authority / Human decision 明确建立。

## 6. 公开身份与 canonical URL

公开 URL 是产品 contract，不由 HTML entry、Vue component、Router file、framework 或 bundler 决定。

当前 canonical URL：

```text
Main
/                             首页
/column/{alias}               栏目
/article/{id}                 INTERNAL Article
/page/{alias}                 独立 Page
/page/{groupAlias}/{alias}    PageGroup member

Party
/party/                       中心党建入口
/party/column/{alias}         Party 栏目
/party/article/{id}           Party INTERNAL Article
```

EXTERNAL_LINK Article 不建立本地正文详情，其目标责任由 Domain Authority 定义。Main / Party 的作用域必须来自稳定业务关系，不能由 Frontend path heuristic、DOM、数组位置或历史 URL 猜测。

## 7. 内容与 ownership 产品边界

产品必须保持以下责任可区分：

```text
Versioned stable Site Definition
→ optional one-time initial Runtime defaults
→ ordinary operator-managed Runtime content

Legacy Source / authorized evidence
→ Historical canonical data
→ controlled import
→ ordinary Runtime content
```

稳定站点定义可以拥有 stable structure、accepted stable content / asset 和 Fresh Site initial default，但不能因为 Repository 中存在 default 就永久覆盖运营人员已经维护的 ordinary Runtime content。

Public Renderer 只负责呈现当前正式数据；它不能成为 CMS 内容、Site Definition 或 Historical canonical data 的第二业务 Authority。

Article / Page / ListItem 等具体 identity、publish lifecycle、content profile、content ownership 与 operator-divergence 规则由 `cms-domain.md` 统一定义。

## 8. Rich Text 与结构化内容产品边界

运营人员需要成熟、稳定、中文友好的 Rich Text authoring。Rich Text 的长期正文 authority 与安全 / compatibility Domain 不变量由 `cms-domain.md` 定义，具体 authoring Observable Behavior 由 `docs/specifications/rich-text-authoring.md` 定义。

当 card、section、step 等结构本身属于产品语义时，Page 可以使用 Structured content；这不建立通用 Page Builder，也不要求普通说明性 Page 从 Rich Text 迁移为 Structured。

Engineering / External Page 可以保留 Page identity 与 canonical URL，但其主要内容 / 行为必须由明确 ownership 承担，不能伪装成 operator-owned arbitrary Rich body。

## 9. 跨 Capability 产品级质量要求

以下要求在具体框架、数据库或前端实现替换后仍持续成立：

- Main / Party canonical URL 可以直接访问和刷新；
- desktop 与 representative mobile viewport 能完成主要公开浏览和导航，不复制旧站固定宽度造成明显横向溢出；
- Navigation、carousel manual control、pagination 等主要公开交互具备合理 keyboard accessibility；
- `prefers-reduced-motion` 等已经接受的基础可访问性意图在相关交互中得到尊重；
- 内容 loading、empty、error、unsupported / unavailable state 可诊断，不能用静默 fallback、永久 skeleton 或错误旧内容伪装成功；
- CMS Resource 必须受到安全路径、允许类型与真实媒体内容校验；运营人员上传内容不能获得任意执行 HTML / JavaScript 资产的能力；
- 新窗口外链必须使用等价于 `noopener noreferrer` 的安全行为；当前没有统一离站确认页或外链徽标 Requirement；
- Admin save / reopen / Public render 之间必须保持 accepted content semantics；
- implementation replacement 不能改变业务对象 identity、content ownership、canonical URL 或已接受用户行为；
- 具有视觉 Acceptance 的变更，自动行为验证不能替代必要的 bounded Human Review。

当前没有 Authority 支持具体性能 SLA、固定首屏毫秒数、任意浏览器全兼容或完整 WCAG 等级；这些不能从“现代网站通常需要”反向补入 Baseline。真实新增要求必须通过后续 Requirement Change 建立。

## 10. 项目级验收不变量

未来 Feature / change 若触达本产品范围，至少不得破坏：

1. 运营数据只有一个长期业务 Authority，不在 CMS、Frontend 常量、Site Definition default 与 migration data 间并行双写同一事实；
2. Main / Party 的业务定位、内容作用域和 canonical URL 不因工程重构漂移；
3. CMS business object 的 stable/source identity、lifecycle 与 operator ownership 不被实现捷径绕过；
4. stable Site Definition、ordinary Runtime data 与 Historical Migration provenance 保持可区分 lifecycle；
5. Public Renderer 可以替换，但不能反向拥有正式 CMS content 或 historical source fact；
6. Product Requirement 不因数据库、框架、目录、migration number 或 package layout 变化而失效；
7. unsupported / conflict / ambiguity 不通过默认值、静默 fallback 或实现 convenience 伪装成功；
8. 真实产品歧义必须返回 Requirement / Human Authority，不在实现中静默选择。

## 11. 非决策

本文不决定：

- 具体 Frontend framework、SPA / SSR / SSG / Hybrid；
- Backend module 数量、源码目录与 application packaging；
- 当前 Flyway 文件编号；
- Site Package / migration / Runtime 的具体物理目录和文件格式；
- Rich Text editor 品牌、版本或 wrapper；
- Review Environment / CI topology；
- 当前 Ready Execution Unit、Planning Candidate 排序、PR、release 或 deployment state。

后续候选、优先级与当前 Gate 由 Project Roadmap / GitHub Current Evidence 持有，不在 Product Requirement 缓存第二份规划状态。
