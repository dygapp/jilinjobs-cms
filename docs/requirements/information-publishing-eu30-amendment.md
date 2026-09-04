---
id: requirement-information-publishing-eu30-amendment
title: 信息发布与网站服务需求 — EU-30 轮播修订
type: business-requirement-amendment
status: confirmed
version: "V4.9-EU30"
classification:
  - l1-06
  - l2-28
  - l2-29
relations:
  upstream:
    - docs/requirements/information-publishing.md
  related:
    - docs/specifications/public-site.md
    - docs/specifications/party.md
    - docs/technical/carousel-list-placement.md
created_at: 2026-09-04
updated_at: 2026-09-04
---

# 信息发布与网站服务需求 — EU-30 轮播修订

## 1. 文档作用

本文记录 EU-30 Human Discussion 已确认并进入实现的需求修订。

在 EU-30 轮播范围内，本文对 `docs/requirements/information-publishing.md` V4.8 中以下旧表述形成**定向 supersede**：

- `HOME_CAROUSEL_INTERVAL_SECONDS` 作为 Main-only 首页轮播属性；
- 通用列表项只以自身标题 / URL / `imagePath` 表达目标内容；
- 中心党建仅包含 `gcsy / gzdt / dgdz / llxx` 四个可访问内容栏目；
- Party 轮播固定 5 秒或与 Main 分别维护行为参数。

V4.8 其他未被本文明确修订的需求继续有效。后续对 `information-publishing.md` 做整版升级时，应把本文内容折叠回主需求文档，并删除已经被替代的旧描述。

## 2. 通用列表投放来源

CmsListItem 必须支持：

1. `LINK`：列表项自身维护标题、可选 URL 和按列表图片策略允许的图片；
2. `ARTICLE`：列表项引用已有 Article，用于把文章投放到轮播等展示容器。

ARTICLE 投放必须保持以下边界：

- Article 仍只有一个栏目归属 `columnId`；
- 加入列表不改变栏目、栏目列表归属、文章详情面包屑；
- ARTICLE 只在关联文章处于 `PUBLISHED` 时对公开端有效；
- 文章撤回后，投放自动从公开列表退出；重新发布后可按既有投放关系恢复；
- Main 站内目标由前端生成 `/article/{id}`；Party 站内目标生成 `/party/article/{id}`；
- 不在 CmsList 中复制第二份文章正文，也不通过文章标题推断关联关系。

## 3. 图片数据需求

CmsList `imagePolicy` 继续表示数据要求，不表示视觉布局：

- `NONE`：列表项不使用图片；
- `OPTIONAL`：允许无图；
- `REQUIRED`：公开投放必须形成有效图片。

对于 ARTICLE：

- 可继承文章主题图片；
- 可从正文图片中选择展示图片；
- 可上传列表专用覆盖图片；
- 列表专用覆盖图片以 CMS Resource ID 关联，不修改 Article 主题图片；
- `REQUIRED` 且无法形成有效图片时，Backend / Admin 必须阻止形成无图投放。

`HOME_CAROUSEL` 与 `PARTY_CAROUSEL` 均为 `imagePolicy=REQUIRED`。

## 4. 统一轮播展示属性

Main 与 Party 使用同一组网站属性：

### `CAROUSEL_INTERVAL_SECONDS`

- 分组：`PRESENTATION`；
- 类型：`INTEGER`；
- 默认值：`4`；
- 单位：秒；
- 正常值必须大于 0；
- 缺失、非法或不大于 0 时公开端 fallback 为 4 秒。

`HOME_CAROUSEL_INTERVAL_SECONDS` 不再是现行 Runtime 属性，不得继续作为 Main-only 配置依赖。

### `CAROUSEL_MAX_ITEMS`

- 分组：`PRESENTATION`；
- 类型：`INTEGER`；
- 默认值：`5`；
- 表示单个轮播区域前台最多消费的有效项数量；
- Backend 允许运营维护多于该数量的记录。

## 5. Main / Party 共同行为

所有使用当前公共 Carousel 生命周期的 Main / Party 轮播必须满足：

- 0 个有效项：稳定空态；
- 1 个有效项：静态展示，不启动 timer；
- 2 个及以上有效项：按列表顺序循环自动切换；
- 提供手动页码；
- hover 时暂停；
- focus 位于轮播内部时暂停；
- 页面隐藏时暂停；
- 暂停解除后从当前项继续，不重置到第一项；
- `prefers-reduced-motion: reduce` 时关闭自动播放并关闭/移除切换动画，但手动页码继续可用；
- 图片加载失败项退出本次有效集合，并由后续有效项补位；
- 全部图片失败时稳定进入空态；
- EU-30 不新增 swipe；
- EU-30 不引入第三方 Carousel 依赖。

共享只覆盖状态与生命周期，不要求 Main / Party 共用一个视觉组件。

## 6. 响应式与视觉边界

- Main 轮播保持接近现有 Desktop 视觉尺寸，改为稳定比例表达；当前确认比例为 `8:5`，图片 `object-fit: cover`；
- Party Desktop 保持约 `585×329`，移动端保持 `585:329`；
- Main 与 Party 不要求统一比例、caption 样式、dot 样式或 DOM；
- 切换动画保持轻量 opacity fade；reduced-motion 时无动画。

## 7. 中心党建遗漏历史内容修订

EU-30 对原站轮播第二项进行反向追踪后确认：

- 原站存在 `typeCode=zhutijiaoyu` 历史栏目；
- 旧标题“主题教育2023”不作为新版长期栏目名称；
- 新版增加 `party-theme-education / 主题教育`；
- 该栏目属于 Party Router / Article 作用域；
- 不新增为 PartyHome 第五个固定内容区块；
- 原站完整列表共 2 条：1 INTERNAL + 1 EXTERNAL_LINK，`unresolved=0`。

原轮播 position 2 指向 `content_id=154659859759104` 的主题教育站内文章。新版迁移必须：

1. 先通过稳定 `sourceSystem + legacyKey` 解析迁移文章；
2. 把轮播项落为 ARTICLE 投放；
3. 保留原轮播 PNG 作为列表专用覆盖 Resource；
4. 不使用标题匹配 Runtime 文章；
5. 不长期依赖旧站详情 URL。

## 8. 迁移证据状态

EU-29 已通过 Human Review 的接受基线保持不变：181 篇文章、4 个轮播项及其原证据 provenance 不被 EU-30 重写。

EU-30 新增主题教育 2 条记录属于增量候选：

- 写入 Consumer-owned、版本化 Canonical Workspace；
- Manifest 明确区分 `acceptedSnapshot` 与 `candidateExtension`；
- candidate 当前为 `pending-human-review`；
- 保存采集 Run、Head SHA、legacy identity、fingerprint 和资源 SHA-256；
- Fresh DB import、二次幂等和 Runtime 关联验证通过后，仍需 Human Review 才能把该增量标记为 accepted。

## 9. Acceptance Criteria

- Runtime 不再依赖 `HOME_CAROUSEL_INTERVAL_SECONDS`；
- Main / Party 均使用 `CAROUSEL_INTERVAL_SECONDS` 和 `CAROUSEL_MAX_ITEMS`；
- CmsListItem 可维护 LINK / ARTICLE；
- ARTICLE 投放不改变文章栏目归属；
- ARTICLE 发布状态控制公开可见性；
- REQUIRED ARTICLE 可继承文章图片或使用列表覆盖 Resource；
- Main / Party 满足暂停恢复、visibility、reduced-motion、图片失败补位和手动页码行为；
- Main / Party 保留独立视觉比例与主题样式；
- `party-theme-education / 主题教育` 存在并属于 Party 内容作用域，但不进入 PartyHome 固定四栏目区域；
- 历史轮播 position 2 使用 ARTICLE 稳定关系并保留原 PNG 覆盖图；
- EU-29 acceptedSnapshot 与 EU-30 candidateExtension 的证据状态可独立审计；
- EU-30 Human Review 通过前，不把增量迁移候选提前声明为最终 accepted。
