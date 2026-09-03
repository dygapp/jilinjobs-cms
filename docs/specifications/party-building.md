# 中心党建公开站规格说明（Specification）

## 1. 目标

本文定义中心党建正式公开站点的 WHAT / WHY。通用 CMS 模型继续以 `docs/requirements/information-publishing.md`、`docs/specifications/cms-core.md` 为准；公开前端总体 Site Boundary 继续遵循 `docs/specifications/public-site.md` 与 ADR-0002。

中心党建不是新的通用多站点平台，也不是独立后台系统。它是同一 `frontend/public-site` 工程中的独立 Party Building Site Entry，复用 Spring Boot CMS Backend 与现有通用 Column / Article / CmsList 能力，同时拥有独立 Router、Shell、页面模板和红色视觉主题。

本阶段依据原站 `https://24365.jl.smartedu.cn/dyzj` 重新取证，不再使用 Foundation 阶段临时文案/卡片作为正式信息架构。

## 2. 原站事实基线

2026-09-02 重新取证确认：

- 原站党建入口：`/dyzj`，页面标题“党员之家”；
- 原站首页明确存在四条内容线：
  - 高层声音：`typeCode=gcsy`；
  - 工作动态：`typeCode=gzdt`；
  - 党规党章：`typeCode=dgdz`；
  - 理论学习：`typeCode=llxx`；
- “学习园地”是首页对“党规党章 + 理论学习”的页面分组，不是第五种内容类型；
- 原站四个栏目列表分别使用 `/plist.html?typeCode=gcsy/gzdt/dgdz/llxx`，且均存在多页历史内容；
- 当前原站生成的站内详情链接使用 `/pdetail.html?content_id=...`；部分可观察历史地址还会携带 `typeCode`，更早历史内容还存在 `/detail.html?content_id=...` 变体，因此 `content_id`、`typeCode` 与详情路径类型应作为迁移映射证据分别保留，不把某一种参数组合误写为所有历史详情 URL 的必需格式；
- 内容同时存在站内详情与外部权威来源直链，外部来源包括共产党员网、政府网站、吉林省政府网站等；
- 原站党建页 Footer 使用与中心主站一致的机构地址、联系电话、备案与版权信息；
- 原站首页顶部左侧为独立的 4 项图片轮播，每项具有图片、标题和跳转目标，并与右侧“高层声音”栏目并列；它不是“高层声音”的置顶文章展示，也不是普通栏目列表；
- 原站移动端并未实现真正响应式：当前 CSS/DOM 证据显示 `html, body` 使用 `min-width: 1200px`、主内容固定 1200px，390px viewport 下仍形成横向溢出。新版只把这一事实作为历史实现证据，不把横向溢出复制为目标行为。

顶部图片轮播属于独立人工编排集合，现有通用 CmsList 的“有序成员 + 图片 + URL + 打开方式”已经足以表达，不新增党建专属 Carousel/Topic 模型。轮播容器属于稳定站点结构，可以预置；轮播成员属于运营内容，不进入 Flyway。

## 3. CMS 复用规格

### 3.1 栏目结构

使用现有 Column 树，不新增 `site` 字段：

```text
中心党建 (party-building)
├── 高层声音 (party-voice)   <- legacy gcsy
├── 工作动态 (party-work)    <- legacy gzdt
├── 党规党章 (party-rules)   <- legacy dgdz
└── 理论学习 (party-study)   <- legacy llxx
```

父栏目仅用于 CMS 组织和党建作用域识别。Party 首页“学习园地”仍是页面布局分组，不等同于父栏目。

### 3.2 文章

四个子栏目复用通用 Article：

- `INTERNAL`：本站保存正文，使用 Party 文章详情模板；
- `EXTERNAL_LINK`：只维护标题、来源、日期和原文地址，从 Party 首页/栏目列表直接打开来源网站。

不创建 `PartyArticle`、`PartyCategory`、`PartyTopic` 等重复模型。

### 3.3 首页图片轮播

原站顶部图片轮播复用通用 CmsList，预置稳定列表容器 `PARTY_HOME_CAROUSEL`：

- 列表容器作为站点结构基线，`imagePolicy=REQUIRED`；
- 列表项使用现有标题、图片、URL、打开方式、排序、启停等通用字段；
- 页面模板负责轮播布局、尺寸、切换、标题位置和响应式行为；
- Flyway 不注入原站历史轮播成员；正式成员由历史内容迁移或后台运营维护；
- 不新增 Party 专属 Carousel 数据表、`displayMode` 或后台模块。

### 3.4 管理端

继续使用现有“栏目管理 + 文章管理 + 列表管理”。文章管理的父栏目聚合后代栏目能力可以直接用于“中心党建”上下文；顶部轮播通过通用列表管理维护。

当前不新增党建专属 Admin Module。若后续出现独立审核流、独立权限、专属字段或完全不同的生命周期，再通过 Requirement Change 评估。

## 4. Canonical URL

新版 Party URL：

- 首页：`/party/`
- 栏目：`/party/column/{alias}`
- 站内文章：`/party/article/{id}`

Party Router 必须验证栏目/文章属于 `party-building` 栏目树，不能让普通主站文章通过 `/party/article/{id}` 套用党建主题。

旧 `/plist.html?typeCode=...`、`/pdetail.html?content_id=...`、`/detail.html?content_id=...` 及其可观察历史参数变体只作为历史迁移映射输入，不作为新版 canonical URL。

## 5. 页面规格

### 5.1 Party 首页

至少包含：

1. Party Header / 品牌与站点入口；
2. 4 项图片轮播；
3. 高层声音；
4. 工作动态；
5. 学习园地；
   - 党规党章；
   - 理论学习；
6. Party Footer。

四个内容线均从对应 Column + Article 查询，不在组件内维护静态文章数组。顶部图片轮播从 `PARTY_HOME_CAROUSEL` 读取当前启用成员，不从全站文章、栏目置顶结果或前端静态数组拼装。

首页每个内容组显示有限条最新/排序后内容，并提供进入对应 Party 栏目列表的“更多”入口。具体条数属于页面模板常量，不构成 CMS 全局配置。

### 5.2 栏目列表

`/party/column/{alias}`：

- 只接受四个已确认党建子栏目；
- 显示栏目名、文章列表、发布日期、分页；
- `INTERNAL` 进入 `/party/article/{id}`；
- `EXTERNAL_LINK` 直接进入原文；
- 保持 Party Header / Footer / 红色页面 Frame；
- 支持直接访问和刷新。

### 5.3 文章详情

`/party/article/{id}`：

- 仅允许已发布且属于党建栏目树的站内文章；
- 显示栏目上下文、标题、来源、发布日期、正文、正文图片和附件；
- 继续使用通用公开资源安全规则和浏览量规则；
- 不存在、撤回、非党建文章统一显示 Party 站点内的不可用状态。

## 6. 视觉规格

Party Foundation 阶段的红色 CSS 仅是架构隔离证明，不是最终视觉 Authority。

正式视觉必须依据原站证据收敛：

- 红色党建主题和原站视觉识别；
- Header / 内容宽度 / 首页区块关系；
- 顶部轮播与高层声音并列的桌面结构；
- 栏目列表与文章详情的层级、留白、边框、字体和状态；
- Footer 与原站机构联系信息；
- Desktop / Mobile 响应式。

原站固定 1200px 与移动端横向溢出属于旧实现限制。新版应保留可证明的视觉层级、比例和内容关系，但按现行 Requirement 实现可用的移动端响应式布局，不复制 `min-width: 1200px` 行为。

若原站关键 Logo、背景、装饰图等可以可靠取得并验证真实媒体类型，应进入版本化 `site-baseline/static/party-building/**`。无法可靠取得的资源不得用无关网络图片替代。轮播成员图片属于运营内容，不因为轮播容器是稳定工程结构就整体纳入版本化站点资产。

Functional Browser PASS 只能证明功能，最终 Visual Fidelity 需要 AI Visual + Human Review。

## 7. 数据与历史迁移

Flyway 只负责稳定站点结构：父栏目、四个子栏目，以及 `PARTY_HOME_CAROUSEL` 列表容器；不负责历史文章或轮播成员。

历史党建文章、外链、正文图片、轮播成员、legacy `content_id` / `typeCode` / 详情路径变体映射由独立迁移/采集工作处理。迁移结果必须仍落入通用 Column / Article / CmsList / Resource 模型，不建立第二套长期内容库。

在完整历史迁移完成前，自动验证可使用明确 E2E fixture；Human Review 环境可注入代表性党建内容，但 Fixture 不得冒充正式历史迁移完成。

## 8. Acceptance Criteria

- `party-building` 父栏目与四个子栏目形成预置站点结构；
- `PARTY_HOME_CAROUSEL` 作为通用 CmsList 预置容器存在，轮播成员不由 Flyway 批量注入；
- `/party/` 不再显示 Foundation “内容建设中”文案；
- Party 首页按原站四条内容线组织数据，其中“学习园地”仅作为视觉分组；
- Party 首页顶部由独立 CmsList 图片轮播驱动，不把栏目置顶文章冒充轮播数据；
- `/party/column/{alias}` 支持分页和内链/外链混合内容；
- `/party/article/{id}` 只展示党建站内文章，并拒绝非党建文章；
- Party Shell 与 Main Shell / CSS 隔离；
- 新版 Mobile 不复制原站 1200px 固定宽度横向溢出；
- Main `/`、`/column/**`、`/article/**`、`/page/**` 无回归；
- 管理端继续使用通用栏目/文章/列表能力，无新增党建专属模块；
- Flyway fresh chain 正常，且不批量注入历史文章或轮播成员；
- Public / Admin / Backend / Integrated Browser Current Evidence 全部成功；
- 最终视觉通过原站证据、AI Visual 和 Human Review 收敛。
