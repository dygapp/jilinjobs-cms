---
id: specification-public-site
title: 公开站产品规格
type: specification
status: accepted
version: "V2.2"
relations:
  requirements:
    - docs/requirements/information-publishing.md
    - docs/requirements/cms-domain.md
  architecture:
    - docs/architecture/cms-architecture.md
  related:
    - docs/specifications/party.md
    - docs/specifications/page-content.md
updated_at: 2026-09-15
---

# 公开站产品规格

## 1. Scope

本规格定义中心主站与中心党建公开访问者可以观察到的页面、导航、内容投影、跳转、响应式和失败行为。

CMS business object、identity、lifecycle 与 ownership 由 `docs/requirements/cms-domain.md` 持有；Main / Party Site Boundary 与可替换 Public Renderer 由 `docs/architecture/cms-architecture.md` 持有；具体前端框架、Entry 文件、Router 实现、源码目录和构建配置属于 Technical / implementation。

## 2. Canonical URLs

### 2.1 Main

```text
/                             首页
/column/{alias}               栏目列表
/article/{id}                 站内文章详情
/page/{alias}                 独立单页
/page/{groupAlias}/{alias}    单页分组成员
```

外链 Article 不创建本地正文详情；在公开入口中按其外链语义直接进入真实来源。

### 2.2 Party

```text
/party/                       中心党建入口
/party/column/{alias}         中心党建栏目
/party/article/{id}           中心党建站内文章
```

`/party/**` 保持独立红色主题与内容作用域，但业务上仍是 Main 信息架构下的中心党建专题入口。

### 2.3 URL invariants

- canonical URL 不依赖具体 HTML 文件名、Frontend component 或 bundler；
- direct access / refresh 必须正常；
- breadcrumb 来自业务对象关系，不从 legacy URL、数组位置或某次导航入口猜测；
- Main / Party 不因共享 CMS 表而互相套用错误主题或内容作用域。

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

其中“招聘公告”首页区域只聚合 `recruitment-announcement` 栏目中当前已发布的 `EXTERNAL_LINK` Article，并直接进入其外部来源；这不限制该栏目的普通栏目页只能存在外链 Article。

首页招聘活动宣传展示消费 `HOME_RECRUITMENT_PROMO` 当前有效 Advertisement：0 项时不制造伪内容，1 项时静态展示，2 项及以上按展示顺序轮动；每项继续遵守其 URL / open-mode / `NO_LINK` Domain contract。具体轮动间隔属于当前实现选择，不在本规格固化。

“最新招聘”及招聘 / 宣讲区域当前只是已接受页面结构中的第三方业务集成 seam；真实 iframe / 第三方 Runtime integration 必须由新的 Feature Requirement / Specification 明确授权，不因页面已存在占位区域自动获得实施权限。

Public 不得同时读取两套等价业务来源后再合并，例如 CMS 正式对象与历史 JSON / 前端常量并行成为同一运营数据 Authority。

## 4. Navigation 与 Footer

Main 与 Party 的公共 Navigation / Footer 保持同一业务信息和交互能力：

- 一级 / 二级导航层级；
- active state；
- internal / external target；
- desktop / mobile navigation；
- Footer 中已确认的机构、联系、备案和官方标识信息；
- 响应式重排。

Main 使用蓝色主题，Party 使用红色主题。主题可以改变视觉 token，但不能静默改变菜单层级、Footer 信息架构或公共交互语义。

如果未来确实需要不同的信息架构，必须先形成 Requirement Change，而不是由局部页面自行分叉。

## 5. 栏目与文章

### 5.1 Column list

栏目页至少提供：

- 当前栏目标题与 breadcrumb；
- 当前业务作用域内的已发布内容；
- INTERNAL / EXTERNAL_LINK 的正确目标行为；
- 分页；
- loading / empty / error 状态；
- desktop / mobile 可用布局。

Main 与 Party 二级栏目当前使用一致的主要列表 presentation 与分页能力，同时保持各自 canonical Article URL 和作用域约束。

### 5.2 Article detail

INTERNAL Article detail：

- 只公开已发布内容；
- 展示 accepted title、source/date、Rich body、附件等当前可用内容；
- managed images 使用 Public 可访问资源 contract；
- 不存在、删除、撤回或不属于当前 Site scope 时显示明确不可用状态。

EXTERNAL_LINK Article 从列表 / placement 进入其当前外部目标，不复制外部正文。

## 6. Page

Page 公开行为遵循当前 Page Content Specification：

- Rich Text Page 以 accepted Rich body 呈现；
- Structured Page 由其明确 renderer 呈现；
- PageGroup 成员可以形成数据驱动的公共 Tab；
- unknown / malformed content profile / renderer 必须显示可诊断失败；
- 不因 renderer 不同建立第二套 Page URL。

## 7. CmsList 与内容投放

Public 对 CmsListItem 的消费遵循 Domain source identity：

### LINK

- 使用列表项自身 title / target / allowed image；
- target 为空时不制造伪链接。

### ARTICLE

- 只在关联 Article 当前已发布时公开；
- INTERNAL target 使用当前 Site canonical Article URL；
- EXTERNAL_LINK target 使用 Article 当前外部目标；
- placement 不改变 Article 栏目归属或详情 breadcrumb；
- 页面使用当前 effective image，不在 Runtime 猜测“正文第一张”。

CmsList 的 image policy 只控制数据有效性，不决定 Public 页面布局模式。

## 8. Carousel behavior

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

自动切换间隔和前台最大有效项数量使用当前 CMS 低风险 presentation configuration；具体 key / current default value 由 JilinJobs Site Definition 持有，Public 不另维护第二份配置。

当前已接受的主要视觉比例为：Main 主轮播 `8:5`；Party 轮播 `585:329`。两者可以使用不同 caption、dot 和主题视觉，也不要求共用相同 DOM；改变这些已接受比例属于 Public visual Specification change，不应由局部实现重构静默漂移。

## 9. External links

- EXTERNAL_LINK Article 默认直接进入当前外部来源；
- Navigation、CmsList、Advertisement 使用各自 Domain open-mode contract；
- 新窗口外链使用等价于 `noopener noreferrer` 的安全行为；
- 当前没有 Requirement 要求统一离站确认页或外链徽标；
- same-site internal target 使用 canonical route；
- `/party/**` 作为跨 Site Entry 的站内目标仍保持其 canonical namespace。

## 10. Resources

公开页面只消费已接受的公开资源 contract：

- stable Site assets；
- current Runtime uploads / managed resources；
- Historical Migration 已投影的受控资源。

Public client 不应理解 Admin-only resource endpoint，也不应在页面代码中重新建立资源 ownership。

稳定模板资源不得在 Runtime 直接依赖旧站静态资源 URL；业务 `<a href>` 外链和第三方业务入口不属于此限制。

## 11. Responsive / accessibility behavior

公开站至少满足：

- desktop 与 mobile 可完成主要浏览和导航；
- 不复制旧站固定宽度导致的明显横向溢出；
- Navigation、carousel manual controls、分页等主要交互可以通过键盘合理操作；
- reduced-motion 偏好得到尊重；
- 图片和链接在当前内容 contract 允许时具有合理可访问文本；
- loading / empty / error / unsupported state 可辨识，不通过永久 skeleton 或静默空白掩盖失败。

## 12. Main / Party shared vs site-specific behavior

当前已接受的共享行为：

- Navigation / Footer 的业务结构与交互；
- 二级栏目列表的主要 presentation primitive；
- 无主题 carousel lifecycle；
- common public data / resource / metadata behavior。

保持 Site-specific：

- Main / Party Banner；
- Main 首页与 Party 入口页内容布局；
- 各自内容主题与 brand presentation；
- Site-specific route scope；
- 尚未被证据证明应共享的页面模板。

共享不等于合并 Main / Party 产品身份；Site-specific 不等于复制 Domain model。

## 13. Failure behavior

以下情况必须产生可观察失败或稳定空态，而不是展示错误内容：

- unknown Page renderer / schema；
- content 不属于当前 Site scope；
- requested Article / Page 不公开；
- Public Resource 无法解析；
- scoped query 失败；
- carousel 所有图片失效；
- stale async request 在 route 已变化后才返回。

异步数据必须以当前 route / scope 为准；旧请求不能覆盖新页面的 success、error、loading 或 metadata 状态。

## 14. Acceptance

触达公开站行为时按实际范围至少验证：

- Main / Party canonical direct access + refresh；
- Main 首页招聘公告只聚合已发布 EXTERNAL_LINK Article；
- Main 首页招聘活动宣传展示的 0/1/many 与 open-mode 行为；
- Main 首页招聘日历与当前第三方业务 integration seam 保持已接受结构；
- scope-correct Column / Article / Page data；
- INTERNAL / EXTERNAL_LINK target；
- Main / Party public theme boundary；
- shared Navigation / Footer behavior；
- shared column-page behavior；
- carousel 0/1/many、pause/resume、reduced-motion、failed-image behavior以及当前 Main / Party accepted visual ratio；
- managed Rich Text resources；
- desktop + representative mobile viewport，无明显横向溢出；
- loading / empty / error / unsupported states；
- stale async response 不覆盖当前 route；
- 有视觉 Acceptance 时 automated Browser evidence 后再执行 bounded Human Review。

## 15. Non-goals

- 从本规格固化 Vue / Vite / Router / HTML Entry implementation；
- 选择 SSR / SSG / Hybrid 或新框架；
- 因 Main / Party 主题不同自动拆 Repository / deployment；
- generic page builder；
- 当前未批准的真实第三方 iframe integration；
- 从本规格恢复任何已结束 Execution Unit。
