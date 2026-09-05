---
id: requirement-information-publishing-eu30-amendment
title: 信息发布与网站服务需求 — EU-30 轮播修订
type: business-requirement-amendment
status: confirmed
version: "V4.9-EU30-HR"
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
updated_at: 2026-09-05
---

# 信息发布与网站服务需求 — EU-30 轮播修订

## 1. 文档作用

本文记录 EU-30 Human Discussion / Human Review 已确认并进入实现的需求修订。

在 EU-30 范围内，本文对 `docs/requirements/information-publishing.md` V4.8 中以下旧表述形成**定向 supersede**：

- `HOME_CAROUSEL_INTERVAL_SECONDS` 作为 Main-only 首页轮播属性；
- 通用列表项只以自身标题 / URL / `imagePath` 表达目标内容；
- 中心党建仅包含 `gcsy / gzdt / dgdz / llxx` 四个可访问内容栏目；
- Party 轮播固定 5 秒或与 Main 分别维护行为参数；
- 文章必须保留独立“推荐”布尔属性，并把“推荐”作为全局公开文章排序层级。

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
- 列表项既有打开方式仍然有效，ARTICLE 生成站内 canonical route 后也必须遵守 `DEFAULT / SAME_WINDOW / NEW_WINDOW`；
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
- 正文图片只是后台选择候选，不允许前台运行时隐式寻找“正文第一张”；
- `REQUIRED` 且无法形成有效图片时，Backend / Admin 必须阻止形成无图投放；已存在的 ARTICLE 后续失去继承图片时必须自动退出 REQUIRED 公开列表，直到重新形成有效图片。

`HOME_CAROUSEL` 与 `PARTY_CAROUSEL` 均为 `imagePolicy=REQUIRED`。

## 4. 统一轮播展示属性

Main 与 Party 使用同一组网站属性：

### `CAROUSEL_INTERVAL_SECONDS`

- 分组：`PRESENTATION`；
- 类型：`INTEGER`；
- 默认值：`4`；
- 单位：秒；
- 正常值必须大于 0；
- Backend 不接受新写入的 0、负数或非整数；
- 缺失、非法历史值或不大于 0 时公开端 fallback 为 4 秒。

`HOME_CAROUSEL_INTERVAL_SECONDS` 不再是现行 Runtime 属性，不得继续作为 Main-only 配置依赖。

### `CAROUSEL_MAX_ITEMS`

- 分组：`PRESENTATION`；
- 类型：`INTEGER`；
- 默认值：`5`；
- 必须大于 0，Backend 不接受新写入的 0、负数或非整数；
- 表示单个轮播区域前台最多消费的有效项数量；
- Backend 允许运营维护多于该数量的记录；
- 缺失、非法历史值或不大于 0 时公开端 fallback 为 5。

## 5. Main / Party 共同行为

所有使用当前公共 Carousel 生命周期的 Main / Party 轮播必须满足：

- 0 个有效项：稳定空态；
- 1 个有效项：静态展示，不启动 timer；
- 2 个及以上有效项：按列表顺序循环自动切换；
- 提供手动页码；
- hover 时暂停；
- focus 位于轮播内部时暂停；
- 页面初始即处于隐藏状态时不得启动自动切换，后续页面隐藏时继续暂停；
- 暂停解除后从当前项继续，不重置到第一项；
- `prefers-reduced-motion: reduce` 时关闭自动播放并关闭/移除切换动画，但手动页码继续可用；
- 图片加载失败项退出本次有效集合，并由后续有效项补位；
- 失败项剔除或最大数量变化时优先按列表项 ID 保持当前正在展示的内容，不因为数组下标位移无意跳转；
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
4. 保留该投放原有 `NEW_WINDOW` 打开方式，但目标改为新系统 Party 文章 canonical route；
5. 不使用标题匹配 Runtime 文章；
6. 不长期依赖旧站详情 URL。

## 8. 迁移证据状态

EU-29 已通过 Human Review 的接受基线保持不变：181 篇文章、4 个轮播项及其原证据 provenance 不被 EU-30 重写。

EU-30 新增主题教育 2 条记录属于增量候选：

- 写入 Consumer-owned、版本化 Canonical Workspace；
- Manifest 明确区分 `acceptedSnapshot` 与 `candidateExtension`；
- candidate 当前为 `pending-human-review`；
- 保存采集 Run、Head SHA、legacy identity、fingerprint 和资源 SHA-256；
- Fresh DB import、二次幂等和 Runtime 关联验证通过后，仍需 Human Review 才能把该增量标记为 accepted。

## 9. Human Review 内容运营收敛

### 9.1 文章排序属性

Human Review 重新评估“置顶 / 推荐 / 展示顺序”后确认：`推荐` 没有独立推荐专区、推荐标识、推荐工作流或其他独立消费场景，只是在全局文章查询中重复增加一级排序优先级。为避免多个近义排序开关造成运营歧义：

- Article 不再维护全局 `recommended` 布尔属性；
- 公开文章默认排序收敛为：`置顶 DESC → 展示顺序 DESC → 发布日期/实际发布时间 DESC → id DESC`；
- `置顶`表达明确的栏目优先语义，`sortOrder`承担同级内容人工排序；
- 若未来出现“首页推荐 / 专题推荐 / 人工推荐区”等独立展示需求，应使用 `CmsList + ARTICLE` 做明确投放，不重新给 Article 增加全局推荐状态。

数据库通过后续 Flyway migration 删除历史 `recommended` 字段和对应索引维度；历史 migration 文件保持不可变。

### 9.2 创建后不可修改的来源身份

Human Review 确认以下字段决定对象的来源语义，创建后不得通过普通编辑改写：

- Article：`articleType`（`INTERNAL / EXTERNAL_LINK`）；
- CmsListItem：`sourceType`（`LINK / ARTICLE`）；
- `sourceType=ARTICLE` 的 CmsListItem：`articleId`。

管理端必须把上述字段在编辑态显示为只读/禁用；Backend 必须独立拒绝绕过 UI 的修改请求。若需要把列表项由 LINK 改为 ARTICLE、或把 ARTICLE 投放替换为另一篇文章，应删除原列表项并新建新的投放记录，使来源身份和审计语义清晰。

本规则不机械扩展到普通可运营配置。文章栏目、标题、正文、图片、排序、打开方式、页面 `renderMode`、导航目标等仍按各自既有需求维护，除非后续 Requirement Change 明确把某字段提升为不可变身份。

### 9.3 Party 内容页面主题一致性

- Party 栏目列表页与文章详情页的 breadcrumb 使用同一字号、间距、颜色与交互主题；
- Party 栏目分页控件的页码、跳转、每页条数下拉等全部交互态必须使用 Party 红色主题，不得泄漏 Main 蓝色主题；
- 每页条数选择器应使用可主题化的共享控件，不依赖浏览器/操作系统不可控的原生 `<select>` 弹层选中色。

## 10. Acceptance Criteria

- Runtime 不再依赖 `HOME_CAROUSEL_INTERVAL_SECONDS`；
- Main / Party 均使用 `CAROUSEL_INTERVAL_SECONDS` 和 `CAROUSEL_MAX_ITEMS`，管理端拒绝非正整数；
- CmsListItem 可维护 LINK / ARTICLE；
- ARTICLE 投放不改变文章栏目归属；
- ARTICLE 发布状态控制公开可见性；
- ARTICLE canonical route 继续遵守列表项 `openMode`；
- Article `articleType` 创建后不可修改；CmsListItem `sourceType` 与 ARTICLE `articleId` 创建后不可修改，UI 与 Backend 同时强制；
- Article 不再存在全局 `recommended` 属性，公开排序使用置顶、展示顺序和发布日期；独立推荐场景使用 CmsList ARTICLE 投放；
- REQUIRED ARTICLE 可继承文章图片或使用列表覆盖 Resource，失去有效图片后不继续公开；
- Main / Party 满足暂停恢复、初始及后续 visibility、reduced-motion、图片失败补位、当前项 identity 保持和手动页码行为；
- Main / Party 保留独立视觉比例与主题样式；
- Party 栏目/详情 breadcrumb 一致，栏目分页及每页条数选择器不存在 Main 蓝色主题泄漏；
- `party-theme-education / 主题教育` 存在并属于 Party 内容作用域，但不进入 PartyHome 固定四栏目区域；
- 历史轮播 position 2 使用 ARTICLE 稳定关系并保留原 PNG 覆盖图和 `NEW_WINDOW` 语义；
- EU-29 acceptedSnapshot 与 EU-30 candidateExtension 的证据状态可独立审计；
- EU-30 Human Review 通过前，不把增量迁移候选提前声明为最终 accepted。
