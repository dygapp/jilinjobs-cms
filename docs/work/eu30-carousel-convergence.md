# EU-30 — Carousel Architecture & Behavior Convergence

## Status

**CURRENT — implementation and verification convergence**

工作分支：`feature/eu-30-carousel-convergence`

Draft PR：#58 `feat: 收敛 EU-30 轮播架构与行为`

本文是 `docs/work/frontend-follow-up-execution-units.md` 中 EU-30 占位规划启动后的执行态补充。占位文档继续提供 EU-31 后续路线；EU-30 当前事实、已确认方案、实现范围和退出条件以本文为准。

## 1. Goal

把 Main Site 与中心党建 Party 的轮播从两个局部实现收敛为：

- 通用 `CmsListItem` 可表达 LINK / ARTICLE 展示投放；
- Main / Party 共用稳定的无主题轮播生命周期；
- Main / Party 继续持有自己的视觉 DOM / CSS / 比例；
- 统一站点级轮播行为属性；
- 修复 Party 历史轮播第二项与遗漏主题教育内容的真实关系；
- 建立可重复、可审计的 Fresh DB / Browser / Human Review 证据。

不以“抽象更多”作为目标，不引入第三方 Carousel，不提前执行 EU-31 完整浏览器兼容工作。

## 2. Confirmed Decisions

### 2.1 CmsListItem

来源：

- `LINK`
- `ARTICLE`

ARTICLE 只是展示投放，不修改 Article 单一 `columnId`。

ARTICLE 只在 `PUBLISHED` 时公开有效；撤回自动退出公开列表。

### 2.2 Image Policy

列表 `imagePolicy` 继续是通用数据契约：

- NONE
- OPTIONAL
- REQUIRED

ARTICLE 可继承文章主题图片、选择正文图片或使用列表专用 Resource 覆盖图。

Main `HOME_CAROUSEL` 与 Party `PARTY_CAROUSEL` 均为 REQUIRED。

### 2.3 SiteProperty

统一：

- `CAROUSEL_INTERVAL_SECONDS=4`
- `CAROUSEL_MAX_ITEMS=5`

旧 `HOME_CAROUSEL_INTERVAL_SECONDS` 不再是 Runtime 依赖。

### 2.4 Shared Behavior

- 0 项稳定空态；
- 1 项静态；
- 多项自动循环；
- 手动 dots；
- hover pause；
- focus pause；
- visibility pause；
- resume 不重置；
- reduced-motion 关闭 autoplay 和 transition；
- 图片失败退出有效集合并补位；
- EU-30 不实现 swipe；
- 不引入第三方 Carousel。

### 2.5 Visual Ownership

Main：稳定 `8:5`，`object-fit:cover`，轻量 fade。

Party：Desktop 约 `585×329`，Mobile `585:329`，Party-owned dots / caption / theme。

共享 composable，不共享视觉组件。

## 3. Party Historical Correction

EU-30 对 Party 历史轮播 position 2 反向取证后确认：

- 原站存在 `typeCode=zhutijiaoyu`；
- 新版栏目为 `party-theme-education / 主题教育`；
- 完整历史列表 2 条：1 INTERNAL + 1 EXTERNAL_LINK；
- `unresolved=0`；
- 不新增 PartyHome 第五个固定内容区；
- position 2 指向 `content_id=154659859759104` 的 INTERNAL 文章；
- 原轮播 PNG 保留为该 ARTICLE 投放的列表专用覆盖图。

EU-29 已接受 181 篇基线保持原 provenance；EU-30 的 2 条记录保留为 `candidateExtension / pending-human-review`，直到本 EU Human Review 通过。

## 4. Implemented Scope

当前分支已经实现：

- V19 数据模型与统一 SiteProperty migration；
- `CmsListItem.sourceType / articleId / imageResourceId`；
- public ARTICLE 过滤、有效图片和公开 Resource 权限；
- Admin LINK / ARTICLE 选择、文章图片继承 / 正文图片候选 / 覆盖图上传；
- `shared/carousel/useContentCarousel.ts`；
- Main / Party 切换到共享生命周期；
- `party-theme-education` Party Router scope；
- EU-30 theme collector 与 durable candidate merge；
- Historical Import V2；
- Party Carousel V2 `sourceSystem + legacyKey -> article_id` 解析；
- position 2 ARTICLE + Resource override；
- Canonical Verification 支持 acceptedSnapshot + candidateExtension 双层证据；
- Admin stale verification contract 已按 EU-30 更新；
- Public `carousel-convergence.spec.ts` 专项 E2E。

## 5. Verification Obligations

### Build / Static

- Backend tests + classes / package；
- Admin `vue-tsc + vite build`；
- Public `vue-tsc + vite build`；
- Main / Party CSS reduced-motion / responsive ratio review。

### Browser

- Main / Party 共用 interval/max-items；
- reduced-motion 不自动播放；
- manual dot 仍有效；
- ARTICLE Party canonical route；
- ARTICLE 不改变 columnId；
- withdraw 后从公开 list 退出；
- list override Resource 公开权限随有效 ARTICLE 状态；
- 既有 Main / Party / Admin Browser Regression 无回归。

### Canonical Fresh DB

- EU-29 acceptedSnapshot 保持 181；
- EU-30 candidateExtension 为 2；
- Runtime dataset 当前 183；
- first import 183 CREATED；
- second import 183 SKIPPED；
- carousel first import 4 CREATED；
- second import 4 SKIPPED；
- position 2 `source_type=ARTICLE`；
- article legacy identity -> Runtime article_id；
- override Resource bytes SHA-256 一致；
- remaining LINK carousel items static bytes 一致。

### Human Review

最终需要人工确认：

- Main / Party Desktop + Mobile 轮播视觉没有不可接受回归；
- dots / caption / pause / manual behavior 可接受；
- Party position 2 正确进入主题教育站内文章；
- 主题教育 2 条增量历史内容与原站证据一致；
- 没有需要继续调整的 Authority-backed 高优先级问题。

## 6. Current Evidence Status

- EU-30 theme collection Run `33880672887`：PASS；2 条，1 INTERNAL + 1 EXTERNAL_LINK，unresolved=0；已晋升为仓库 durable candidate。
- 早期 CI 已证明 Backend / Main / Admin build 可通过；旧 Admin Browser 失败被分类为 Stale Verification Contract 并已更新。
- 当前最终 Head 的 CI / Canonical / Review Environment 必须在所有文档与测试提交完成后重新取得；旧 Head 结果不能代替最终 Current Evidence。

## 7. Exit Condition

EU-30 仅在以下条件全部满足时关闭：

1. Requirement / Specification / Technical Plan 与实现一致；
2. 最新 Head CI PASS；
3. 最新 Head Canonical Migration Verification PASS；
4. Review Environment 基于最新 Head 成功部署；
5. AI / Browser Evidence 无未处理高优先级问题；
6. Human Review PASS；
7. Human Review 后将 EU-30 candidateExtension 的最终接受状态与 provenance 回写；
8. Roadmap 更新为 EU-30 完成、EU-31 CURRENT / NEXT；
9. PR #58 整理到 Ready for Review，但**不在没有项目负责人明确指令时合并**。
