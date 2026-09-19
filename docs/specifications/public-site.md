---
id: specification-public-site
title: 公开站产品规格
type: specification
status: accepted
version: "V3.1"
relations:
  requirements:
    - docs/requirements/information-publishing.md
    - docs/requirements/cms-domain.md
  architecture:
    - docs/architecture/cms-architecture.md
  related:
    - docs/specifications/page-content.md
    - docs/specifications/rich-text-authoring.md
updated_at: 2026-09-16
---

# 公开站产品规格

## 1. 范围

本规格是 Main 与中心党建（Party）公开访问行为的唯一 Current public-surface Specification owner，定义访问者可以观察到的页面、导航、内容投影、跳转、作用域、响应式与失败行为。

Main / Party 的长期产品定位与内容范围由 `docs/requirements/information-publishing.md` 持有；CMS business object、identity、lifecycle 与 ownership 由 `docs/requirements/cms-domain.md` 持有；Site / Theme / replaceability boundary 由 `docs/architecture/cms-architecture.md` 持有。具体前端框架、Entry、Router、源码目录、build 与 delivery mechanism 属于 Technical / implementation。

## 2. 规范 URL（Canonical URL）

### 2.1 主站（Main）

```text
/                             首页
/column/{alias}               栏目列表
/article/{id}                 站内文章详情
/page/{alias}                 独立单页
/page/{groupAlias}/{alias}    单页分组成员
```

外链 Article 不创建本地正文详情；在公开入口中按其外链语义进入真实来源。

### 2.2 党建站（Party）

```text
/party/                       中心党建入口
/party/column/{alias}         中心党建栏目
/party/article/{id}           中心党建站内文章
```

`/party/**` 保持独立红色主题与内容作用域，但业务上仍是 Main 信息架构下的中心党建专题入口。

### 2.3 URL 不变量

- canonical URL 不依赖具体 HTML 文件名、Frontend component 或 bundler；
- direct access / refresh 必须正常；
- breadcrumb 来自业务对象关系，不从 legacy URL、数组位置或某次导航入口猜测；
- Main / Party 不因共享 CMS Domain 而互相套用错误主题或内容作用域。

## 3. Main 首页

Main 首页继续保持已接受的固定页面结构与主要视觉识别，并由 CMS 数据驱动需要持续运营的内容。

当前稳定内容职责至少包括：

- 主导航；
- 首页快捷入口；
- 主轮播；
- 通知公告、就业动态等普通资讯区；
- 招聘日历；
- 招聘活动宣传展示；
- “最新招聘”与招聘 / 宣讲等固定第三方业务集成 seam；
- 招聘公告；
- 业务指南快捷入口；
- 网站导航 / 友情链接；
- 网站名称、联系方式、备案、版权等站点信息；
- NCSS 等无需运营维护的固定工程集成。

其中“招聘公告”首页区域只聚合当前招聘公告业务栏目中已发布的 `EXTERNAL_LINK` Article，并直接进入其外部来源；这不限制该栏目的普通栏目页只能存在外链 Article。具体稳定栏目 identity 由其真实 Domain / Site Definition owner 持有，不在本规格复制内部 alias。

首页招聘活动宣传展示消费当前招聘活动展示位中的有效 Advertisement：0 项时不制造伪内容，1 项时静态展示，2 项及以上按展示顺序轮动；每项继续遵守其 URL / open-mode / `NO_LINK` Domain contract。具体展示位 identity 与轮动参数由其真实 source / configuration owner 持有，不在本规格复制内部 key 或 default value。

招聘日历当前承担首页日期定位能力：展示当前日期所在年份 / 月份，以周一到周日的七列周视图排列当月日期，并对当前日期提供可辨识状态。当前没有 Authority 把某一天与招聘活动 dataset、详情跳转或第三方事件接口建立绑定；不能仅因为区域名称为“招聘日历”就在 regeneration 中发明尚未授权的事件数据集成。

“最新招聘”及招聘 / 宣讲区域当前只是已接受页面结构中的第三方业务集成 seam；真实 iframe / 第三方 Runtime integration 必须由新的 Feature Requirement / Specification 明确授权，不因页面已存在占位区域自动获得实施权限。

## 4. 中心党建入口与内容体验

`/party/` 是 Main 信息架构下的专题入口，保持明确红色视觉主题与独立内容作用域，不建立第二套 CMS Domain。

当前已接受的 Party 内容范围包括高层声音、工作动态、党规党章、理论学习与主题教育。其中“学习园地”是党规党章与理论学习的页面视觉分组，不是独立内容类型；主题教育可以通过栏目、轮播或其他明确投放进入 Party 体验，但不因此成为入口页第五个固定内容区。

Party 入口页至少形成以下可观察内容线：

1. 已接受的中心党建 Banner；
2. 与 Main 相同业务结构的公共 Navigation；
3. 中心党建轮播；
4. 高层声音；
5. 工作动态；
6. 学习园地；
7. 与 Main 相同业务信息的 Footer。

中心党建 Banner 是稳定专题视觉，本身不可点击；在 desktop / mobile 下应保持可辨识的标题视觉，不因资源优化产生明显清晰度退化。具体 asset 文件名、digest 与投影位置由真实 Site Definition / implementation owner 持有，不在本规格复制。

非 Party 内容不能通过 `/party/column/**` 或 `/party/article/**` 获得 Party 模板；Party scope 不得由 URL 文本、历史 typeCode 或页面 DOM heuristic 猜测。

## 5. Navigation 与 Footer

Main 与 Party 的公共 Navigation / Footer 保持同一业务信息和交互能力：

- 一级 / 二级导航层级；
- active state；
- internal / external target；
- desktop / mobile navigation；
- Footer 中已确认的机构、联系、备案和官方标识信息；
- 响应式重排。

Main 使用蓝色主题，Party 使用红色主题。主题可以改变视觉 token，但不能静默改变菜单层级、Footer 信息架构或公共交互语义。

如果未来确实需要不同的信息架构，必须先形成 Requirement Change，而不是由局部页面自行分叉。

## 6. 栏目与文章

### 6.1 栏目列表

栏目页至少提供：

- 当前栏目标题与 breadcrumb；
- 当前业务作用域内的已发布内容；
- INTERNAL / EXTERNAL_LINK 的正确目标行为；
- 分页；
- loading / empty / error 状态；
- desktop / mobile 可用布局。

Main 与 Party 二级栏目当前使用一致的主要列表 presentation 与分页能力，同时保持各自 canonical Article URL、主题与作用域约束。

### 6.2 文章详情

INTERNAL Article detail：

- 只公开已发布内容；
- 展示 accepted title、source/date、Rich body、附件等当前可用内容；
- managed images 使用 Public 可访问资源 contract；
- 不存在、删除、撤回或不属于当前 Site scope 时显示明确不可用状态。

Party detail 只接受属于 Party 内容作用域的 INTERNAL Article，并保持 Party 主题与 breadcrumb 信息层级。EXTERNAL_LINK Article 从列表 / placement 进入其当前外部目标，不复制外部正文，也不进入本地 Party detail。

## 7. 页面

Page 公开行为遵循当前 Page Content Specification：

- Rich Text Page 以 accepted Rich body 呈现；
- Structured Page 由其明确 renderer 呈现；
- PageGroup 成员可以形成数据驱动的公共 Tab；
- unknown / malformed content profile / renderer 必须显示可诊断失败；
- 不因 renderer 不同建立第二套 Page URL。

## 8. CmsList 与内容投放

Public 对 CmsListItem 的消费遵循 Domain source identity 的当前投影：

### LINK

- 使用列表项自身 title / target / allowed image；
- target 为空时不制造伪链接。

### ARTICLE

- 只在关联 Article 当前已发布时公开；
- INTERNAL target 使用当前 Site canonical Article URL；
- EXTERNAL_LINK target 使用 Article 当前外部目标；
- placement 不改变 Article 栏目归属或详情 breadcrumb；
- 页面使用当前 effective image，不在 Runtime 猜测“正文第一张”。

CmsList 的 image policy 只控制数据有效性，不决定 Public 页面布局模式。Party 轮播中的有效项需要具备当前 accepted image；无有效图片的项不应作为可用轮播项展示。

## 9. 轮播行为

Main / Party 轮播共享以下用户可观察 lifecycle：

- 0 个有效项：稳定空态；
- 1 个有效项：静态展示，不自动切换；
- 2 个及以上有效项：按列表顺序循环；
- 提供可操作的手动分页控制；
- hover 时暂停；
- focus 位于轮播内部时暂停；
- 页面隐藏时暂停；
- 暂停解除后从当前项继续，不重置到第一项；
- `prefers-reduced-motion: reduce` 时关闭自动播放和非必要切换动画，手动控制仍可用；
- 图片加载失败项退出当前有效集合，并由后续有效项补位；
- 全部有效图片失败时进入稳定空态；
- 有效集合变化时优先保持当前 item identity，避免仅因数组下标变化无意跳转。

自动切换间隔和前台最大有效项数量使用当前低风险 presentation configuration；具体 key / default value 由其真实配置 owner 持有，Public Specification 不另维护第二份配置。

当前已接受的主要视觉比例为：Main 主轮播 `8:5`；Party 轮播 `585:329`。两者可以使用不同 caption、dot 和主题视觉，也不要求共用相同 DOM；改变这些已接受比例属于 Public visual Specification change，不应由局部实现重构静默漂移。

## 10. 外部链接

- Main 的 EXTERNAL_LINK Article 从内容列表、首页聚合或其他不带独立 open-mode 的 Article 入口进入当前外部来源时，以新窗口作为已接受基线；Article 不因此获得独立 `openMode` 字段；
- Navigation、CmsList、Advertisement 使用各自 Domain open-mode contract；其中 ARTICLE 型 CmsListItem 继续遵守 placement 自身 open-mode contract；
- 新窗口外链使用等价于 `noopener noreferrer` 的安全行为；
- 当前没有 Requirement 要求统一离站确认页或外链徽标；
- same-site internal target 使用 canonical route；
- `/party/**` 作为跨 Site Entry 的站内目标仍保持其 canonical namespace。

## 11. 资源

公开页面只消费当前已接受的公开资源 contract，包括 stable Site assets、允许公开的 managed resources 与受控 historical resources。

这些资源在正常页面中应按当前内容语义可访问；资源不可用时进入本规格定义的可观察失败状态，而不是依靠第二份内容来源或静默替换掩盖问题。

## 12. 响应式 / 无障碍行为

公开站至少满足：

- desktop 与 mobile 可完成主要浏览和导航；
- 不复制旧站固定宽度导致的明显横向溢出；
- Navigation、carousel manual controls、分页等主要交互可以通过键盘合理操作；
- reduced-motion 偏好得到尊重；
- 图片和链接在当前内容 contract 允许时具有合理可访问文本；
- loading / empty / error / unsupported state 可辨识，不通过永久 skeleton 或静默空白掩盖失败；
- Party Banner、入口页、栏目、分页与详情在不同 viewport 下保持一致 Party branding，不泄漏错误的 Main 蓝色主题。

## 13. 基础搜索引擎友好行为

成功加载的公开页面必须提供与当前 Site / 内容语义相符的基础页面 metadata，而不是长期保留入口 HTML 的通用占位信息：

- Main / Party 入口能够表达各自站点 / 专题 identity；
- Column 页面能够以当前栏目名称表达页面 title，并提供与栏目浏览语义相符的 description；
- INTERNAL Article 页面能够以当前文章标题表达页面 title，并从当前正文或 source/date 等 accepted content 形成合理 description；
- Page 能够以当前 Page 名称表达页面 title，并从 Rich / Structured accepted content 形成合理 description；
- 成功切换到新的公开内容后，metadata 必须随当前内容更新，不能继续代表上一条已成功加载的内容。

本能力只定义最小可观察结果，不要求特定 DOM API、Vue helper、SSR / SSG，也不自动新增 canonical link element、structured data、sitemap 或完整 SEO 平台 Requirement。

## 14. Main / Party 一致与差异行为

Main 与 Party 当前保持一致的用户可观察行为包括：

- Navigation / Footer 的业务结构与交互；
- 二级栏目列表的主要 presentation 与分页能力；
- carousel lifecycle；
- common public content / resource / metadata behavior。

当前保持差异的用户可观察行为包括：

- Main / Party Banner；
- Main 首页与 Party 入口页内容布局；
- 各自内容主题与 brand presentation；
- Site-specific route scope；
- 尚未被新的产品事实要求统一的页面呈现。

行为一致不改变 Main / Party 的产品身份；视觉和布局差异也不产生第二套 CMS Domain。

## 15. 失败行为

以下情况必须产生可观察失败或稳定空态，而不是展示错误内容：

- unknown Page renderer / schema；
- content 不属于当前 Site scope；
- 非 Party 内容请求 Party column / article route；
- requested Article / Page 不公开；
- Public Resource 无法解析；
- scoped query 失败；
- carousel 所有图片失效；
- stale async request 在 route 已变化后才返回。

异步数据必须以当前 route / scope 为准；旧请求不能覆盖新页面的 success、error、loading 或 metadata 状态。

## 16. 验收

触达公开站行为时，最终结果至少满足实际涉及的以下 contract：

- Main / Party canonical direct access + refresh；
- Main 首页招聘公告只聚合已发布 EXTERNAL_LINK Article；
- Main EXTERNAL_LINK Article 在无独立 open-mode 的内容入口使用新窗口并保持安全 rel 行为；
- Main 首页招聘活动宣传展示的 0/1/many 与 open-mode 行为；
- Main 首页招聘日历展示当前年月、周一至周日日期网格与可辨识的今日状态，且不因名称自行发明未授权招聘事件数据；
- `/party/` accepted Banner 可见且不可点击；
- Party 入口页内容线正确，“主题教育”不被静默增加为第五个固定内容区；
- scope-correct Column / Article / Page data，以及 Party route scope guard；
- INTERNAL / EXTERNAL_LINK target；
- Main / Party public theme boundary；
- Navigation / Footer 与 column-page 的当前一致行为；
- carousel 0/1/many、pause/resume、reduced-motion、failed-image behavior 与当前 Main / Party accepted visual ratio；
- managed Rich Text resources；
- Main / Party 成功加载的入口、Column、Article、Page 按当前内容提供合理 title / description metadata；
- desktop + representative mobile viewport 无明显横向溢出；
- loading / empty / error / unsupported states；
- stale async response 不覆盖当前 route。

需要哪些自动化、Browser 或 Human visual evidence 由当前 Verification Authority 与变更风险决定，本规格只拥有应满足的可观察结果。

## 17. 非目标

- 新增 Party-specific CMS model / Admin module；
- generic page builder；
- 当前未批准的真实第三方 iframe integration。
