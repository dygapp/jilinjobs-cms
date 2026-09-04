---
id: technical-carousel-list-placement
title: 轮播与列表内容投放技术方案
type: technical-plan
status: confirmed
version: "V1.0"
relations:
  upstream:
    - docs/requirements/information-publishing.md
  related:
    - docs/specifications/public-site.md
    - docs/specifications/party.md
    - docs/work/frontend-follow-up-execution-units.md
created_at: 2026-09-04
updated_at: 2026-09-04
---

# 轮播与列表内容投放技术方案

## 1. 目的

本文固化 EU-30「Carousel Architecture & Behavior Convergence」中经人工讨论确认的列表内容投放与轮播行为方案。核心目标不是引入新的 Carousel CMS 类型，而是在既有 `CmsList` 通用模型上补齐“链接型列表项”和“文章引用型列表项”两种稳定数据来源，并统一 Main / Party 轮播的行为生命周期。

## 2. 内容归属与展示投放

栏目负责回答“文章属于哪里”，列表负责回答“内容还需要在哪里被展示”。文章继续保持单一 `columnId`，不因为加入首页轮播、党建轮播或其他运营列表而改变所属栏目，也不把轮播重新建模为特殊栏目。

`CmsListItem.sourceType` 只使用以下两个值：

- `LINK`：独立链接型列表项；
- `ARTICLE`：引用 CMS 文章的列表项。

`LINK` 描述数据语义，不使用 `MANUAL` 这类录入方式名称。

## 3. LINK 列表项

LINK 项继续维护标题、副标题、目标 URL、静态图片路径、打开方式、排序、启停和扩展数据。图片是否允许或必填由所属 `CmsList.imagePolicy` 决定。

LINK 项不关联 `cms_article`，不存在文章状态、栏目归属或文章资源继承。

## 4. ARTICLE 列表项

ARTICLE 项通过 `articleId` 关联 `cms_article`。Runtime 数据库保存实际外键 ID；公开输出时标题和文章状态以当前文章为准，站内文章 URL 由具体公开 Site 根据自身 canonical route 生成，不要求管理员手工复制文章 URL。

ARTICLE 项不改变文章所属栏目。文章撤回或未发布时，该项仍可在后台保留，但不得进入公开列表；重新发布后，只要列表项本身仍启用即可恢复公开资格。

历史迁移数据不得保存未来 Runtime `articleId`。Canonical Migration Dataset 使用 `sourceSystem + legacyKey` 作为文章稳定引用，Importer 在文章导入并建立 `cms_article_legacy_mapping` 后再解析成实际 `articleId`。

## 5. 通用图片处理流程

将内容加入列表时，首先读取列表 `imagePolicy`，而不是为轮播写特殊分支。

### 5.1 NONE

列表项不使用图片。LINK 项不得保存 `imagePath`；ARTICLE 项不得保存图片覆盖资源。

### 5.2 OPTIONAL

图片可选。

对于 LINK 项，可以不设置图片，也可以使用现有静态图片选择/上传流程。

对于 ARTICLE 项：

1. 文章有主题/封面图片时，默认继承文章封面，不创建覆盖；
2. 文章没有封面但正文存在图片时，后台把正文图片作为候选，默认建议第一张；
3. 用户选择正文图片后，应把所选 `CmsResource` ID 固化为列表项图片覆盖，不能在前台运行时动态寻找“正文第一张”；
4. 用户也可以上传新的 CMS 图片资源作为列表项覆盖；
5. 用户可以选择不使用图片。

### 5.3 REQUIRED

最终公开列表项必须具有有效图片。

LINK 项必须具有有效 `imagePath`。

ARTICLE 项按以下顺序辅助用户完成选择：

1. 有文章封面：默认继承；
2. 无封面但有正文图片：默认建议正文第一张，并允许改选其他正文图片；保存后固化所选 Resource ID；
3. 无可用文章图片：必须上传/选择新的图片资源后才能保存。

ARTICLE 项的覆盖资源只影响该列表投放，不修改文章自身封面或正文。

## 6. 图片 Runtime 表达

ARTICLE 项数据库保存可选 `image_override_resource_id`。为空表示按文章当前封面计算有效图片；非空表示显式使用该资源。公开 API 可返回 `effectiveImageResourceId`，前端通过公开 Resource URL 读取图片。

资源公开访问必须满足至少一种公开引用关系：作为已发布文章的封面/正文图片，或作为启用 ARTICLE 列表项的图片覆盖且其关联文章已发布。不得因为获得 Resource ID 就把任意 CMS 上传文件公开。

## 7. 轮播站点配置

Main / Party 使用同一组 PRESENTATION 网站属性：

- `CAROUSEL_INTERVAL_SECONDS`：自动切换间隔，整数秒，默认 `4`，必须大于 0；
- `CAROUSEL_MAX_ITEMS`：单个轮播区域前台最多展示的有效项数，默认 `5`，必须大于 0。

`CAROUSEL_MAX_ITEMS` 只限制前台展示，不限制后台列表项数量。前台按既有排序和有效性过滤后取前 N 项；超出的启用记录继续保留，可以通过调整排序进入展示范围。

旧 `HOME_CAROUSEL_INTERVAL_SECONDS` 通过后续 migration 收敛为统一属性，不回改既有 Flyway。

## 8. 轮播行为

Main / Party 统一以下稳定行为，视觉 DOM、尺寸和主题仍由各 Site 自己负责：

- 0 项：稳定空状态，不启动 timer；
- 1 项：静态展示，不启动 timer，不显示无意义的分页控件；
- 2 项及以上：按统一间隔循环自动切换；
- 提供可访问的分页点用于手动切换；
- 鼠标 hover 暂停，离开后从当前项继续，禁止重置到第一项；
- 轮播内部获得键盘 focus 时暂停，focus 离开后继续；
- 页面 `document.hidden` 时暂停，重新可见后从当前项继续；
- `prefers-reduced-motion: reduce` 时关闭自动轮播和非必要切换动画，仍保留手动分页；
- 本轮不增加 touch swipe 手势；
- 使用简单短时 opacity fade，不引入水平滑动状态机；
- 图片加载失败的项从本次有效轮播中移除，后续排序项可补位直到达到展示上限；全部失败时进入稳定空状态。

EU-30 继续采用 Vue 自实现，不引入第三方 Carousel 依赖。共享边界是 timer、暂停/恢复、visibility、reduced-motion、有效项和最大数量等行为逻辑；Main / Party 的视觉结构、比例、caption 和主题样式保持独立。

## 9. 响应式

Party 轮播继续以 `585:329` 为稳定比例。Main 不再使用桌面固定 `250px` / 移动固定 `220px` 两套无语义高度，收敛为稳定比例并保持当前桌面视觉接近原实现。图片统一使用 `object-fit: cover`。

## 10. EU-29 历史迁移兼容修订

新系统新增中心党建子栏目：

- 名称：`主题教育`；
- alias：`party-theme-education`；
- legacy typeCode：`zhutijiaoyu`；
- 原站历史显示名称 `主题教育2023` 只作为来源证据，不继续作为新版业务名称；
- 该栏目可正常访问栏目列表和文章详情，但不要求加入 Party 首页主导航。

EU-29 Canonical Dataset 以增量方式补采该栏目完整分页和文章，不重新采集已冻结的其他四个栏目。党建轮播第二项不再作为独立外链处理，而改为 ARTICLE 项，引用 legacy article `zhutijiaoyu:content:154659859759104`。

Canonical 列表项通过稳定引用表达：

```json
{
  "sourceType": "ARTICLE",
  "articleRef": {
    "sourceSystem": "legacy-jilinjobs",
    "legacyKey": "zhutijiaoyu:content:154659859759104"
  },
  "imageRef": {
    "sha256": "a00db48e094e24b778374ae621b6f150e235625c62f17efc860391223fac830b"
  }
}
```

Importer 采用依赖顺序：

1. 先导入文章和文章资源，并建立 `cms_article_legacy_mapping`；
2. 再导入列表；ARTICLE 项通过 `sourceSystem + legacyKey` 查出 Runtime `articleId`；
3. 轮播覆盖图片通过 Canonical SHA-256 校验并导入 CMS Resource，取得 Runtime Resource ID；
4. 最终 Runtime 只保存普通外键 ID，不把 legacy identity / hash 查询泄漏到公开请求路径。

第二轮播项继续使用 EU-29 已保存的原站首页轮播 PNG 作为覆盖图片，不因关联文章拥有正文图片而改选其他图片。

## 11. 验证要求

EU-30 至少验证：

- Flyway Fresh DB；
- LINK / ARTICLE 两种列表项 CRUD 与 `imagePolicy` 三分支校验；
- 草稿/撤回 ARTICLE 项不公开；
- 文章封面继承与正文图片/上传图片覆盖；
- Main / Party 统一 interval/max 配置；
- 0/1/N 项、手动分页、hover/focus/visibility/reduced-motion；
- 图片失败补位和最大项数；
- Main / Party build + Browser E2E；
- EU-29 Canonical Dataset Fresh DB import、二次导入幂等、主题教育栏目和第二轮播项 ARTICLE 关联对账。
