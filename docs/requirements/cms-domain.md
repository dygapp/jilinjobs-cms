---
id: requirement-cms-domain
title: CMS 领域模型与长期业务规则
type: domain-requirement
status: confirmed
version: "V1.0"
relations:
  product:
    - docs/requirements/information-publishing.md
  architecture:
    - docs/architecture/cms-architecture.md
updated_at: 2026-09-15
---

# CMS 领域模型与长期业务规则

## 1. 文档责任

本文是 `jilinjobs-cms` 当前 CMS 领域语义的长期 owner，统一维护业务对象、稳定身份、来源身份、状态 / 生命周期、跨对象关系、内容 ownership 与 failure invariants。

本文不定义页面布局、组件、数据库字段名、HTTP DTO、Migration 文件、源码目录、框架或具体实现算法。具体 Feature 的可观察交互由 Specification 定义；跨 Feature 系统边界由 Architecture Authority 定义。

## 2. 领域边界

CMS Domain 负责回答：

- 站点有哪些可运营业务对象；
- 对象之间如何组织、引用和投放；
- 哪些身份创建后必须稳定；
- 什么状态可以公开；
- 哪些内容属于运营人员、稳定站点定义、工程实现或外部系统；
- 哪些失败必须显式暴露而不能 silent fallback；
- Stable structure、ordinary Runtime data 与 historical provenance 在业务语义上如何区分。

CMS Domain 不把 Main / Party 的具体视觉模板或当前 Vue / Spring 实现视为领域对象。

## 3. Site 与内容作用域

### 3.1 Main

Main 是中心主站公开内容作用域，拥有首页、栏目、文章、单页及固定业务集成等产品入口。

### 3.2 Party

Party 表示“中心党建”内容作用域。业务上它属于 Main 信息架构下的专题入口，不是第二个独立业务网站。

Party 可以拥有独立主题、路由 namespace 与内容模板，但这些呈现隔离不创造第二套 Column、Article、Navigation、Page 或 CmsList domain model。

### 3.3 Scope invariant

对象被 Main 或 Party 页面消费时，不能仅根据 URL、DOM、标题或实现目录猜测业务作用域；作用域必须来自稳定业务关系，例如栏目树、明确的列表 identity 或已接受页面 contract。

## 4. Column

Column 是 Article 的树形内容分类对象。

长期语义：

- Column 具有稳定公开身份 `alias`；
- Column 可以有父子关系；
- Article 只有一个主栏目归属；
- 父栏目可以作为内容聚合上下文，但不能因此复制 Article；
- Column 可以约束站内 Article 的封面数据要求；
- 稳定站点栏目可以受保护，普通运营创建栏目不自动获得稳定站点身份。

### 4.1 Cover policy

Column 的图片数据策略使用：

- `NONE`：站内文章不维护封面；
- `OPTIONAL`：封面可有可无；
- `REQUIRED`：草稿可以暂时无封面，但进入或保持发布状态时必须有有效封面。

该策略只表达内容数据要求，不表达“图文列表”“纯文字列表”、尺寸、布局或其他页面展示模式。

EXTERNAL_LINK Article 不复制本地正文和封面，因此不受 Column 本地封面必填约束。

## 5. Article

Article 表示具有栏目归属和发布生命周期的资讯内容。

### 5.1 Source identity

Article 的来源身份：

- `INTERNAL`：本站持有正文；
- `EXTERNAL_LINK`：本站持有标题、来源、发布日期等索引信息和外部目标，不复制外部正文、图片或附件作为本地 Article 正文。

`articleType` 属于创建时来源身份。普通编辑不得在 `INTERNAL` 与 `EXTERNAL_LINK` 之间切换。需要改变来源语义时，应显式创建新的业务对象或走后续明确的变更流程。

### 5.2 Publish lifecycle

Article 至少具有：

```text
DRAFT → PUBLISHED → WITHDRAWN → PUBLISHED
```

业务规则：

- 保存 / 编辑不自动等于发布；
- 只有 `PUBLISHED` Article 进入正常公开发现范围；
- `WITHDRAWN` Article 从栏目列表、首页聚合和 Article placement 中退出；
- 重新发布后，仍然有效的既有 placement 可以恢复；
- 不存在、删除或非公开状态内容的直接公开访问必须得到明确不可用结果，而不是显示错误旧内容。

### 5.3 Ordering

公开 Article 默认排序语义为：

```text
置顶 DESC
→ 展示顺序 DESC
→ 发布日期 / 实际发布时间 DESC
→ 稳定最后排序键 DESC
```

Article 不维护全局 `recommended` 布尔语义。需要“首页推荐 / 专题推荐 / 人工推荐区”时使用 CmsList + ARTICLE 显式投放。

### 5.4 Rich body

INTERNAL Article 的 Rich Text 正文以 `bodyHtml` 为唯一正文 Authority。正文图片、附件和封面分别承担不同内容关系，不能因编辑器实现而合并成一个不透明 editor state。

## 6. PageGroup 与 Page

### 6.1 PageGroup

PageGroup 是多个 Page 的平级业务组织容器，不建立嵌套 PageGroup。

PageGroup 成员关系可以形成公开 Tab 等可观察组织结构；前端不得维护第二份成员清单作为长期 Authority。

### 6.2 Page stable identity

Page 是具有稳定公开身份和生命周期的单页内容对象：

- 可以独立存在；
- 可以属于一个 PageGroup；
- stable alias / group relation 决定 canonical Page identity；
- 不因为 renderer 不同而建立第二套 Page identity 或第二套 canonical URL。

### 6.3 Primary content authority

每个有效 Page 在同一时刻必须有且只有一个 primary content authority。

禁止长期存在：

- CMS 显示 operator 可编辑 Rich body，但真实页面主体由隐藏 hardcode 决定；
- Rich body 与 structured payload 同时作为 primary body；
- package default 与 operator content 同时宣称拥有当前正文；
- external integration 与任意 Rich body 同时作为主内容；
- renderer 通过 alias、URL、DOM shape 或正文内容猜测 Page 类型。

### 6.4 Content profiles

当前长期 content profile：

- `RICH_TEXT`：whole-page Rich Text 是 primary content；
- `STRUCTURED`：结构化 payload 是 primary content；
- `NONE`：CMS 不持有 whole-page primary body，由明确的 engineering / external profile 承担主要内容或行为。

Content profile、renderer identity 与 content ownership 是正交概念，不得重新压成一个混合字段语义。

Unknown / malformed / unsupported content model、structured schema 或 renderer 必须 fail closed，不静默 fallback 到 Rich Text。

### 6.5 Content ownership

Page primary content ownership 至少区分：

- `OPERATOR`：ordinary Runtime 由运营人员维护；
- `SITE_PACKAGE`：稳定版本化来源拥有当前内容；
- `ENGINEERING`：主要内容 / 行为由受控工程实现承担；
- `EXTERNAL`：主要内容 / 行为来自外部系统。

具体 renderer、持久化与 integration mechanism 属于 Architecture / Technical，不在本文定义。

### 6.6 Operator divergence protection

稳定默认内容不能成为永久 overwrite authority。

当版本化 default 需要演进 Existing Runtime content 时：

- Fresh create 可以使用当前接受 default；
- Existing adoption 必须具有明确且可审计的 prior-baseline precondition；
- 精确匹配 prior baseline 才允许受控 adoption；
- operator-diverged content 必须 preserve + report；
- ordinary reconcile 不得覆盖 operator edit；
- adoption 必须幂等；
- 后续新的 content upgrade 需要新的明确 Authority，不能自动复用旧 fingerprint 规则。

## 7. NavigationLocation 与 NavigationItem

### 7.1 NavigationLocation

NavigationLocation 表示稳定导航容器。它是业务数据对象，不要求用编译期 Enum 穷举所有可能位置。

稳定 JilinJobs 位置至少包括：

- `MAIN`；
- `HOME_SHORTCUT`；
- `HOME_QUICK`。

### 7.2 NavigationItem

NavigationItem 属于一个 NavigationLocation，可以有父子层级，并持有目标、打开方式、排序、启停和可选图标。

长期规则：

- 父子必须属于同一位置；
- 禁止循环；
- 有子项时不能通过普通删除造成悬空结构；
- 图标属于 NavigationItem 自身数据，不能按数组 index / sort order 推导业务图标；
- 调整排序、插入或删除条目不能使图标与业务语义错位；
- 稳定 Navigation identity 可以受保护，普通 Runtime-created item 不自动成为 stable preset。

Navigation target 至少可以表达 HOME、COLUMN、PAGE、LINK、PLACEHOLDER 等当前接受语义；具体 HTTP representation 属于 Technical contract。

## 8. CmsList 与 CmsListItem

CmsList 表示可排序、可启停的内容投放容器。它不定义页面布局模式。

### 8.1 List identity

CmsList 具有稳定 `code`。内部结构分组可以存在，但普通运营人员不需要获得任意创建稳定结构分组的权限。

`groupCode` 属于受控结构元数据，不是独立业务对象。普通 Runtime List 默认进入 ordinary group；稳定分组由明确站点定义维护。

### 8.2 Image policy

CmsList 图片数据策略使用：

- `NONE`：列表项不使用图片；
- `OPTIONAL`：允许无图；
- `REQUIRED`：公开有效项必须形成有效图片。

该策略不控制图片尺寸、卡片方向、caption、Logo 展示或页面布局。

### 8.3 ListItem source identity

CmsListItem 来源身份：

- `LINK`：列表项自己拥有 title / optional URL / optional image 等内容；
- `ARTICLE`：引用既有 Article，用于展示投放，不复制 Article 正文。

`sourceType` 属于创建时身份；ARTICLE 的 article relation 同样属于来源身份。普通编辑不得 LINK ↔ ARTICLE 切换，也不得把一个 ARTICLE placement 改为引用另一篇 Article。需要改变来源时删除旧 placement 并新建。

### 8.4 ARTICLE placement

ARTICLE placement 不改变 Article 的唯一栏目归属、发布生命周期和详情面包屑。

公开有效性：

- 关联 Article 必须 `PUBLISHED`；
- INTERNAL target 由当前公开 Site 的 canonical Article route 决定；
- EXTERNAL_LINK target 来自 Article 当前 external target；
- Article 撤回后 placement 自动退出，重新发布后可恢复；
- placement 可持有列表专用展示图片覆盖，但不能反向修改 Article 正文或封面。

### 8.5 Effective image

LINK 使用自身图片。

ARTICLE 可以：

- 继承 Article 当前有效封面；
- 使用显式列表专用 Resource 作为覆盖；
- 从正文图片中选择时，必须把选择结果固化为显式 Resource relation，Public Runtime 不得每次猜测“正文第一张”。

`REQUIRED` ARTICLE 如果无法形成有效图片，必须退出公开列表或在写入边界被拒绝，不能形成已知无效公开项。

## 9. AdvertisementSlot 与 Advertisement

AdvertisementSlot 表示稳定展示容器；Advertisement 表示其中的运营展示内容。

Advertisement 至少具有标题、图片、可选 URL、打开方式、启停、排序和可选有效期。

长期规则：

- 一个 Slot 可以有多条内容；
- 公开只消费当前有效内容；
- `NO_LINK` 表示当前只展示、不点击，即使保留 URL 也不得产生跳转；
- 有效期结束不自动删除记录；
- Slot 的稳定身份与 Advertisement ordinary Runtime content 生命周期分离；
- 不因目标 URL 指向外部就把 Advertisement 改造成 Article / ListItem。

## 10. SiteProperty

SiteProperty 表示少量需要 Runtime 运营维护的站点级信息或低风险行为参数。

长期语义：

- 具有稳定 `key`、名称、分组、值、类型、说明、排序、必填、启停等属性；
- value type 至少覆盖 TEXT、RESOURCE_PATH、JSON、URL、BOOLEAN、INTEGER 等当前 accepted semantics；
- 类型约束必须在业务写入边界最终验证，不能只靠 UI；
- SiteProperty group 是受控 metadata，不自动成为新的可运营业务对象；
- DB connection、credentials、安全限制、服务地址、CI / deployment parameter 不进入 SiteProperty。

当前轮播等低风险 presentation behavior 可以使用 SiteProperty；“使用 SiteProperty”不等于建立通用系统设置中心。

## 11. StaticResource

StaticResource 表示 CMS 可管理的资源文件及其元数据。

长期规则：

- 上传必须校验安全路径、扩展名和真实媒体类型；
- 业务对象引用 Resource 时，删除行为不能只由 UI 决定；
- 被稳定站点资源或当前 CMS direct reference 使用的 Resource 必须受保护；
- 修改业务对象的引用不会自动物理删除旧文件；
- 未引用资源由显式资源清理流程处理；
- 当前保护机制不宣称已拥有完整 Rich HTML / CSS / JS 全引用关系图，普通删除应保留残余风险提示。

## 12. 外链责任模型

外部 URL 根据业务载体归属，不建立全局 Link domain object。

### 12.1 EXTERNAL_LINK Article

Article 拥有标题、来源、日期、栏目、发布生命周期和 external target。

### 12.2 Navigation LINK

NavigationItem 拥有导航文案、placement、target 和 open mode。

### 12.3 CmsList LINK

CmsListItem 拥有展示标题、target、open mode、可选图片和 placement。

### 12.4 Advertisement

Advertisement 拥有活动展示内容和可选 target。

### 12.5 Fixed integration

无需运营维护的固定第三方 seam 可以属于工程实现；一旦出现真实运营维护、排序、启停或替换需求，再通过 Requirement Change 进入合适 CMS 对象。

业务载体即使 URL 相同，也不能因此机械去重成同一对象。

## 13. Stable structure 与 ordinary Runtime data

Domain 必须区分两类长期 ownership：

### 13.1 Stable structure

用于保证站点公开结构、stable identity、固定容器和必要 default 可以从受控版本化来源恢复。

Stable structure 可以受保护并按稳定 identity reconcile，但不能因此把所有运营内容都升级为不可变系统数据。

### 13.2 Ordinary Runtime data

由运营人员在 Runtime 维护。某些 ordinary Runtime data 可以在 Fresh Site 时由一次性 bootstrap 建立；初始化完成后即进入普通运营生命周期。

长期不变量：

```text
one-time initial default
→ completion recorded
→ ordinary operator-managed Runtime data
→ ordinary restart / stable reconcile 不覆盖、不 resurrect
```

因此 Main ListItem 等一次性初始化数据不自动获得 stable membership identity / permanent reconcile / delete protection。

## 14. Historical migration domain

Historical Migration 用于把旧系统中需要 provenance、fingerprint 和可审计导入生命周期的数据转化为当前 CMS Runtime。

### 14.1 Stable migration identity

历史对象使用稳定 `sourceSystem + legacyKey` 等来源身份，不以 Runtime numeric id 作为长期 canonical identity。

### 14.2 Fingerprint semantics

默认导入语义：

```text
no mapping              → CREATE
same identity + same fingerprint → SKIP
same identity + changed fingerprint → CONFLICT
```

不得把 changed fingerprint 默认为 overwrite。

### 14.3 Preflight

在产生对应 Runtime mutation 前，应验证足够的 canonical shape、path、resource integrity、stable target identity、dependency 和 duplicate/conflict condition。已知 INVALID / CONFLICT 不应被静默转换成部分成功。

### 14.4 Compatibility

只有明确、版本化且属于具体 migration scope 的 compatibility authority 才能允许 accepted historical transition 原位更新。Compatibility 不能成为普通 Admin/API 绕过来源身份 immutable 规则的后门，也不能被 Generic migration capability默认为任意 fingerprint update policy。

### 14.5 Offline stability

稳定 verification / import 消费 repository-owned canonical bytes，不依赖 Legacy Source 在线可用性。Legacy Source 网络访问只属于显式 acquisition / retry / reactivation activity。

## 15. Main historical migration current boundary

Main 当前已接受 Historical Migration scope 为 Article-only：

- INTERNAL Article；
- EXTERNAL_LINK Article；
- Article body images / attachments；
- provenance、legacy identity、fingerprint 与 import evidence。

Main Page 与 Main ordinary bootstrap ListItem 不属于 Main Historical Migration input。

当前 accepted Article dataset、source-defect / deferred counts 与 digest 属于 migration data/provenance workspace，不在本 Domain 文档复制维护。

## 16. Party migration current boundary

Party historical migration 可以包含 Article 与已接受的 historical ListItem，因为它有独立来源证据和兼容历史。

Party-specific alias、accepted dataset、fingerprint 和历史 transition 事实属于 Party migration authority；Generic migration capability 不得硬编码这些 site-specific facts。

## 17. Rich Text domain invariants

Rich Text capability服务 INTERNAL Article、RICH_TEXT Page 以及 schema 明确允许的 item-level Rich Text。

长期不变量：

- `bodyHtml` 是 Rich Text 唯一正文 Authority；
- 不双写 editor-internal JSON / Delta / Markdown 与 HTML；
- accepted paragraph / heading / emphasis / list / link / table / image / presentation semantics 在保存—再编辑—公开链路中保持；
- active / document-level dangerous content 必须被阻止；
- sanitization 以 compatibility-first 为前提，不能把某个 editor toolbar 当成完整业务 allow-list；
- Public 正常显示不能要求加载 editor chrome 才能成立；
- editor 品牌和具体版本不是 Domain fact。

## 18. Stable identity 与 preset protection

稳定站点结构对象可以具有 `preset` 或等价的 stable ownership 标识。

业务含义：

- stable alias / code / key 等身份不能被 ordinary edit 改写；
- ordinary delete 必须拒绝；
- 名称、说明、排序、启停和明确可运营字段仍可按对象 contract 维护；
- preset 不等于“对象完全不可修改”；
- ordinary Admin-created object 不自动成为 preset；
- Article、CmsListItem、Advertisement 等 ordinary content 不因 Fresh bootstrap 来源自动获得 preset identity。

## 19. Failure invariants

以下情况默认 fail closed，不允许 silent fallback：

- unknown / malformed Page content model、schema 或 renderer；
- stable identity 冲突；
- illegal source identity change；
- REQUIRED image contract 无法满足；
- invalid resource path / media；
- protected resource ordinary delete；
- historical migration fingerprint conflict；
- unresolved migration dependency / target identity；
- 现有 operator content 与 package adoption precondition 不匹配。

Fail closed 的具体用户提示、HTTP status、CLI exit 或 report format由对应 Specification / Technical 定义。

## 20. Domain acceptance invariants

未来变更不得在没有新的 Requirement Authority 时破坏：

1. 同一业务数据只有一个长期 owner；
2. 来源身份、稳定身份和 ordinary editable field 明确区分；
3. 发布状态与 placement 不互相篡改 ownership；
4. 页面展示配置不反向污染内容数据模型；
5. Site stable structure 不吞并 ordinary operator content；
6. Historical Migration 不吞并 Site Definition / ordinary bootstrap；
7. Public Renderer 不反向成为 CMS Domain Authority；
8. Generic capability 不吸收 Party / Main 的具体 dataset facts；
9. operator divergence 不被 ordinary reconcile 静默覆盖；
10. unsupported state / conflict / ambiguity 不通过默认值伪装成功。