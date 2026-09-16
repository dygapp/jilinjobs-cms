---
id: requirement-cms-domain
title: CMS 领域模型与长期业务规则
type: domain-requirement
status: confirmed
version: "V2.0"
relations:
  product:
    - docs/requirements/information-publishing.md
  index:
    - docs/requirements/index.md
  architecture:
    - docs/architecture/cms-architecture.md
updated_at: 2026-09-16
---

# CMS 领域模型与长期业务规则

## 1. 文档责任

本文是 `jilinjobs-cms` 当前 **CMS 领域需求事实的长期 owner**，统一维护 CMS 业务对象、稳定 / 来源身份、状态与生命周期、跨对象关系、内容 ownership、stable / Runtime 生命周期、Historical Migration 领域语义与 fail-closed 业务不变量。

产品目标、用户、范围、Main / Party 产品定位、canonical public contract 与跨 Capability 产品级质量要求由 `docs/requirements/information-publishing.md` 持有；Requirement locator 由 `docs/requirements/index.md` 持有。本文不重新定义这些 Product facts。

本文不定义页面布局、组件、数据库字段、HTTP DTO、Migration 文件、源码目录、framework 或具体实现算法。Feature Observable Behavior 由 Specification 定义；跨 Feature 系统 structure / application boundary 由 Architecture 定义；concrete Site Definition 与 canonical migration records 分别由其 versioned source workspace 持有。

## 2. Domain boundary

CMS Domain 回答：

- 当前有哪些可运营 business object；
- object 之间如何组织、引用与投放；
- stable identity、source identity 与 ordinary editable data 如何区分；
- 什么 state / relation 可以进入 Public projection；
- Page / Rich Text 等 primary content 由谁拥有；
- stable Site Definition、one-time bootstrap、ordinary Runtime 与 Historical Migration 如何保持不同 lifecycle；
- 哪些 business conflict / unsupported state 必须 fail closed。

Main / Party 的视觉 template、Vue / Spring implementation、build / deployment topology 不属于 Domain object。

## 3. Scope、identity 与 ownership 基础语义

### 3.1 Main / Party content scope

Main 与 Party 的产品定位由 Product Requirement 定义。Domain 只规定：两者复用同一组 Column、Article、Page、Navigation、CmsList 等业务对象，不因 Party 主题或 route namespace 创建第二套 Domain model。

object 的 Main / Party scope 必须来自稳定业务关系，例如 Column tree、明确 List identity、Page contract 或其他 accepted relation；不得仅根据 URL、DOM、标题、数组位置或源码目录猜测 scope。

### 3.2 Stable identity

stable alias / code / key / location identity 用于公开定位、结构恢复或长期关系时，ordinary edit 不得静默改变其业务身份。稳定对象可以使用 `preset` 或等价 ownership 标识保护 identity / ordinary delete；preset 不等于所有字段永久不可修改。

ordinary Admin-created object 不自动成为 stable preset；Article、CmsListItem、Advertisement 等 ordinary content 不因 Fresh bootstrap 来源自动获得 preset identity。

### 3.3 Source identity

source identity 表示对象“是什么来源类型”，与 ordinary editable field 分离。创建时确定的 Article `INTERNAL / EXTERNAL_LINK`、CmsListItem `LINK / ARTICLE` 及 ARTICLE relation 等，不得通过普通编辑静默切换成另一种来源语义。

需要改变 source identity 时，使用显式新对象或后续经 Requirement 授权的受控变更流程。

### 3.4 Content ownership

同一具体 primary content 在同一时刻只能有一个业务 owner。operator Runtime content、versioned Site Definition content、engineering implementation 与 external integration 不得同时宣称拥有同一 primary body。

## 4. Column 与 Article

### 4.1 Column

Column 是 Article 的树形内容分类对象：

- 具有 stable public identity `alias`；
- 可以有父子关系；
- Article 只有一个 primary Column；
- parent Column 可以作为聚合上下文，但不复制 Article；
- stable site Column 可以受 preset protection；ordinary Column 不自动获得 stable ownership。

Column 的 Article image data policy：

- `NONE`：INTERNAL Article 不维护 cover；
- `OPTIONAL`：cover 可选；
- `REQUIRED`：DRAFT 可以暂时无 cover，但进入 / 保持 PUBLISHED 时必须有有效 cover。

该 policy 只表达 data validity，不表达 Public layout。EXTERNAL_LINK Article 不持有本地 body / cover，因此不受本地 cover required rule 约束。

### 4.2 Article source identity

Article source identity：

- `INTERNAL`：本站拥有 Rich Text body；
- `EXTERNAL_LINK`：本站拥有 title、source、date、Column、publish lifecycle 与 external target，不复制外部正文、图片或附件作为本地 Article body。

普通编辑不得 `INTERNAL ↔ EXTERNAL_LINK` 切换。

### 4.3 Article publish lifecycle

Article 至少具有：

```text
DRAFT → PUBLISHED → WITHDRAWN → PUBLISHED
```

规则：

- save / edit 不自动 publish；
- 只有 `PUBLISHED` Article 进入 ordinary Public discovery；
- `WITHDRAWN` 从 Column list、homepage aggregation 与 Article placement 退出；
- republish 后仍有效的 existing placement 可以恢复；
- missing、deleted、non-public Article 的 direct public access 必须产生明确 unavailable result，而不是展示旧内容。

### 4.4 Article ordering / placement

普通 Public Article ordering 语义：

```text
置顶 DESC
→ 展示顺序 DESC
→ 发布日期 / 实际发布时间 DESC
→ 稳定最后排序键 DESC
```

Article 不拥有全局 `recommended` boolean。需要首页 / 专题 / 人工推荐时使用 CmsList + ARTICLE placement。

INTERNAL Article 的 Rich Text body 使用 `bodyHtml` 作为唯一 whole-body authority；cover、body image、attachment 是不同业务关系。

## 5. PageGroup 与 Page

### 5.1 PageGroup

PageGroup 是多个 Page 的平级业务组织容器，不建立 nested PageGroup。成员关系可以形成 Public Tab 等可观察结构；Frontend 不维护第二份成员清单作为长期 Authority。

### 5.2 Page identity

Page 可以 standalone 或属于一个 PageGroup。stable alias / group relation 决定 canonical Page identity；renderer 不创建第二套 Page identity 或 URL。

### 5.3 Content profile

Page content model、renderer identity、content ownership 是三个正交概念。

当前 content profile：

- `RICH_TEXT`：whole-page Rich Text 是 primary body；
- `STRUCTURED`：structured payload 是 primary body；
- `NONE`：CMS 不持有 whole-page primary body，由明确 Engineering / External ownership 承担主要内容或行为。

Unknown / malformed / unsupported content model、structured schema 或 renderer 必须 fail closed，不得 fallback 为 arbitrary Rich Text。

### 5.4 Page content ownership

primary content ownership 至少区分：

- `OPERATOR`：ordinary Runtime 由运营人员维护；
- `SITE_PACKAGE`：versioned Site Definition 拥有当前 content；
- `ENGINEERING`：受控 engineering implementation 拥有主要内容 / 行为；
- `EXTERNAL`：external system 拥有主要内容 / 行为。

禁止长期存在：Rich body 与 structured payload 同为 primary body；package default 与 operator content 同时拥有当前正文；external / engineering page 同时伪装成 operator-owned arbitrary body；renderer 通过 alias / URL / DOM / body heuristic 猜测 Page type。

### 5.5 Operator divergence protection

versioned default 不得成为永久 overwrite authority：

- Fresh create 可以使用当前 accepted default；
- Existing adoption 必须有 explicit prior-baseline precondition；
- exact match prior baseline 才允许 bounded adoption；
- operator-diverged content 必须 preserve + report；
- ordinary reconcile 不覆盖 operator edit；
- adoption 必须 idempotent；
- 新一轮 content upgrade 需要新的 Authority，不自动复用旧 fingerprint rule。

## 6. Navigation

### 6.1 NavigationLocation

NavigationLocation 是稳定导航容器，不要求用 compile-time Enum 穷举全部未来位置。JilinJobs 当前 stable locations 至少包括 `MAIN`、`HOME_SHORTCUT`、`HOME_QUICK`。

### 6.2 NavigationItem

NavigationItem 属于一个 NavigationLocation，可有 parent / child，并持有 target、open mode、ordering、enabled state 与 optional icon。

长期规则：

- parent / child 必须属于同一 location；
- 禁止 cycle；
- 有 child 时 ordinary delete 不能造成 orphan；
- icon 属于 item 自身，不能按 array index / sort order 推导；
- reorder / insert / delete 不得使 icon 与业务语义错位；
- stable item identity 可以受保护，ordinary Runtime item 不自动成为 preset。

Navigation target 至少表达 HOME、COLUMN、PAGE、LINK、PLACEHOLDER 等 accepted semantics；HTTP representation 属于 Technical contract。

## 7. CmsList 与 CmsListItem

CmsList 是可排序、可启停的 content placement container；不拥有 Public layout mode。

### 7.1 CmsList identity / image policy

CmsList 具有 stable `code`。受控 `groupCode` 可以作为 structure metadata，但不是独立 business object；ordinary Runtime List 默认进入 ordinary group，stable group 由 Site Definition 管理。

image data policy：`NONE`、`OPTIONAL`、`REQUIRED`。它只定义 data validity，不定义尺寸、card direction、caption 或页面 layout。

### 7.2 CmsListItem source identity

- `LINK`：item 自己拥有 title、optional URL、optional image 等内容；
- `ARTICLE`：引用 existing Article 进行 placement，不复制 Article body。

`sourceType` 与 ARTICLE relation 是 source identity；ordinary edit 不得 LINK ↔ ARTICLE，也不得把 ARTICLE placement 改成另一篇 Article。需要改变来源时删除旧 placement 并新建。

### 7.3 ARTICLE placement

ARTICLE placement 不改变 Article primary Column、publish lifecycle 或 detail breadcrumb。

Public effective condition：

- related Article 必须 `PUBLISHED`；
- INTERNAL target 使用当前 Site canonical Article route；
- EXTERNAL_LINK target 使用 Article current external target；
- Article withdrawn 后 placement 退出，republish 后可恢复；
- placement 可以持有 list-specific presentation image override，但不能修改 Article body / cover。

### 7.4 Effective image

LINK 使用自身 image。

ARTICLE 可以继承 Article 当前有效 cover、使用 explicit list-specific Resource override，或把从 body 中选择的 image 固化为 explicit Resource relation。Public Runtime 不得每次猜测“正文第一张”。

`REQUIRED` ARTICLE 无法形成有效 image 时，必须在 write boundary 被拒绝或退出 Public list，不能形成 known-invalid public item。

## 8. AdvertisementSlot 与 Advertisement

AdvertisementSlot 是 stable presentation container；Advertisement 是 ordinary operational content，至少具有 title、image、optional URL、open mode、ordering、enabled state 与 optional validity period。

规则：

- 一个 Slot 可以有多条 Advertisement；
- Public 只消费当前 effective item；
- `NO_LINK` 表示当前只展示、不点击，即使保留 URL 也不得跳转；
- validity ended 不自动 delete record；
- Slot stable identity 与 Advertisement Runtime lifecycle 分离；
- target 指向 external URL 不会把 Advertisement 改造成 Article / CmsListItem。

## 9. SiteProperty

SiteProperty 表示少量需要 Runtime 运营维护的 site-level information 或 low-risk behavior parameter。

长期语义：

- stable `key`、name、group、value、type、description、ordering、required、enabled 等；
- value type 至少覆盖 TEXT、RESOURCE_PATH、JSON、URL、BOOLEAN、INTEGER；
- type constraint 必须在业务 write boundary 最终验证，不能只靠 UI；
- group 是 controlled metadata，不自动成为新 business object；
- DB connection、credential、安全限制、服务地址、CI / deployment parameter 不进入 SiteProperty。

使用 SiteProperty 承载低风险 presentation behavior 不等于建立 generic system settings center。

## 10. StaticResource

StaticResource 表示 CMS managed resource 及 metadata。

Domain rules：

- business object 对 Resource 的 relation 是显式业务关系；
- 被 stable Site asset 或 current CMS direct reference 使用的 Resource 必须受 ordinary delete protection；
- 修改业务对象 reference 不自动 physical delete old file；
- unreferenced Resource 由 explicit cleanup / recycle lifecycle 处理；
- 当前 Domain 不宣称拥有 Rich HTML / CSS / external content 的完整 reference graph，因此 ordinary delete / replace 必须允许上层表达 residual reference risk。

path、extension、media、active-content 等 cross-capability safety outcome 由 Product Requirement 持有，具体 parser / storage / endpoint 属于 Technical。

## 11. Cross-object placement / relation invariants

- placement 不能篡改 source object 的 primary ownership / lifecycle；
- presentation policy 不能反向创建新的 content identity；
- stable structure container 与 ordinary member content 生命周期必须可区分；
- Public projection 只消费符合 object lifecycle、relation validity 与 scope 的当前 data。

## 12. External-link ownership model

不建立全局 Link domain object；external URL 由真实业务载体拥有：

- EXTERNAL_LINK Article：Article 拥有 title、source/date、Column、publish lifecycle 与 external target；
- Navigation LINK：NavigationItem 拥有 label、placement、target、open mode；
- CmsList LINK：CmsListItem 拥有 presentation content、target、open mode 与 optional image；
- Advertisement：Advertisement 拥有 campaign / presentation content 与 optional target；
- Fixed integration：无需运营维护的固定第三方 seam 可以属于 engineering implementation；出现真实运营维护、ordering、enabled / replacement 需求后再通过 Requirement Change 进入合适 CMS object。

即使 URL 相同，也不因“可去重”机械合并不同业务载体。

## 13. Stable Site Definition 与 ordinary Runtime lifecycle

Domain 必须区分：

### 13.1 Stable structure / versioned definition

用于保证 public structure、stable identity、fixed container、accepted stable content / asset 与必要 default 可以从受控 versioned source 恢复。stable object 可以按 stable identity reconcile，但不能因此把全部 operational content 升级为 immutable system data。

### 13.2 One-time initial default → Runtime

某些 ordinary Runtime data 可以在 Fresh Site / explicit adoption 时由 one-time bootstrap 建立；成功后即进入 ordinary operator-managed lifecycle：

```text
one-time initial default
→ completion recorded
→ ordinary operator-managed Runtime data
→ ordinary restart / stable reconcile 不覆盖、不 resurrect
```

因此 ordinary Article / ListItem / Advertisement 不因 bootstrap origin 自动获得 permanent stable membership / delete protection。

## 14. Historical Content Migration domain

Historical Migration 把需要 provenance、legacy identity、fingerprint 与 auditable import lifecycle 的 accepted legacy data 转化为当前 Runtime；它不是 Generic schema migration，也不是 Site Definition / ordinary bootstrap。

### 14.1 Stable migration identity / fingerprint

Historical object 使用稳定 source identity（例如 `sourceSystem + legacyKey`），不以 Runtime numeric ID 作为 canonical identity。

默认 semantics：

```text
no mapping                         → CREATE
same identity + same fingerprint  → SKIP
same identity + changed fingerprint → CONFLICT
invalid target / dependency / canonical bytes → INVALID / fail closed
```

changed fingerprint 不默认 overwrite。

### 14.2 Preflight / compatibility

产生 Runtime mutation 前应验证足够的 canonical shape、path / resource integrity、stable target identity、dependency 与 duplicate / conflict condition。known INVALID / CONFLICT 不得静默变成 partial success。

只有 explicit、versioned、scope-bounded compatibility authority 才能允许 accepted historical transition 原位更新；compatibility 不能成为 ordinary Admin / API 绕过 source identity immutable rule 的后门，也不能推广为 Generic arbitrary overwrite policy。

### 14.3 Offline stability

stable verification / import 消费 Repository-owned canonical bytes 或其他 authorized frozen evidence，不依赖 Legacy Source 在线可用。Legacy Source access 只属于 explicit acquisition / retry / reactivation activity。

### 14.4 Main accepted scope

Main 当前 accepted Historical Migration scope 为 Article-only：

- INTERNAL Article；
- EXTERNAL_LINK Article；
- Article body resources / attachments；
- Article provenance、legacy identity、fingerprint 与 import evidence。

Main Page、stable Site Definition 与 ordinary bootstrap ListItem 不属于 Main historical import。Historical Migration 是否在某一时刻允许重新执行，由当前治理 / execution Authority 决定，不属于本文的长期 Domain fact。

### 14.5 Party accepted scope

Party Historical Migration 可以包含 Article 与已经 accepted 的 historical ListItem / carousel semantics，因为它具有独立 source evidence / compatibility history。

Party-specific alias、dataset cardinality、accepted fingerprint / transition 属于 concrete migration source / evidence，不进入 Generic Domain rule；Main Article-only scope 不能反向覆盖 Party，Party historical ListItem 也不能反向推广为 Main rule。

## 15. Rich Text domain invariants

Rich Text capability 服务 INTERNAL Article、RICH_TEXT Page 与 schema 明确允许的 item-level Rich Text。

长期 Domain invariants：

- `bodyHtml` 是 Rich Text 唯一 whole-body Authority；
- 不双写 editor-internal JSON / Delta / Markdown 与 HTML；
- accepted paragraph / heading / emphasis / list / link / table / image / presentation semantics 在 save → reopen → public chain 中保持；
- active / document-level dangerous content 必须被阻止；
- sanitization 以 content compatibility 为前提，不能把当前 editor toolbar 当成完整业务 allow-list；
- Public normal rendering 不能要求加载 editor chrome 才能成立；
- editor brand / version 不是 Domain fact。

Article managed image、attachment 与 body image 是不同 relation；从 body 删除引用不等于立即 physical delete Resource。

## 16. Domain failure invariants

以下业务条件默认 fail closed，不允许 silent fallback：

- unknown / malformed Page content model、schema 或 renderer；
- stable identity conflict；
- illegal source identity change；
- `REQUIRED` image contract 无法满足；
- invalid typed SiteProperty；
- protected Resource ordinary delete；
- Historical Migration fingerprint conflict / unresolved target dependency；
- existing operator content 与 package adoption precondition 不匹配；
- cross-scope content 被错误投影到 Main / Party。

具体 user message、HTTP status、CLI exit / report format属于 Specification / Technical。

## 17. Domain acceptance invariants

未来 change 不得在没有新的 Requirement Authority 时破坏：

1. 同一 business data / primary content 只有一个长期 owner；
2. stable identity、source identity 与 ordinary editable field 明确区分；
3. publish lifecycle 与 placement 不互相篡改 ownership；
4. presentation configuration 不反向污染 content object identity；
5. Site stable structure 不吞并 ordinary operator content；
6. Historical Migration 不吞并 Site Definition / ordinary bootstrap；
7. Public Renderer 不反向成为 CMS Domain Authority；
8. Generic capability 不吸收 Main / Party concrete dataset fact；
9. operator divergence 不被 ordinary reconcile 静默覆盖；
10. unsupported state / conflict / ambiguity 不通过 fallback / default 伪装成功。
