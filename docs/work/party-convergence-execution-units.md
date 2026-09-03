# 中心党建正式页面与内容收敛执行单元

本文记录中心党建 Foundation 之后的正式页面、内容、视觉和历史迁移切分。中心党建在业务上是**主站下的特殊栏目/专题页面**；`party.html`、Party Router 与 `/party/**` 是红色主题和模板隔离的技术实现，不代表第二个网站或独立首页。

中文原站页面名称沿用“党员之家”，英文业务表述为 **Party Members’ Home**。当前工程技术命名使用 `party / Party`，仅 `/party/` 入口页使用 `party-home / PartyHome`。

**状态：进行中（2026-09-03），当前 EU-28 Human Visual Review 修正中。**

## 证据基线

原站：`https://24365.jl.smartedu.cn/dyzj`

| 原站业务线 | legacy typeCode | 新预置 alias |
|---|---|---|
| 高层声音 | `gcsy` | `party-voice` |
| 工作动态 | `gzdt` | `party-work` |
| 党规党章 | `dgdz` | `party-rules` |
| 理论学习 | `llxx` | `party-study` |

“学习园地”只组合党规党章与理论学习。原站四栏目均存在多页内容，正式实现和迁移必须使用栏目作用域分页。

页面顶部存在独立 4 项图片轮播，与高层声音并列。当前稳定 CmsList code 为 **`PARTY_CAROUSEL`**，产品名称为“中心党建轮播”；历史 V14 中的 `PARTY_HOME_CAROUSEL / 中心党建首页轮播` 由 V15 原地重命名，不修改已执行 Migration。

原站 Banner 证据地址为 `https://24365.jl.smartedu.cn/webfile/theme2/img/party_banner.png`，3072×512，SHA-256 `7444d50235d4c87a00d0221ac84551ea083c617bb8a15e58f58d002224bd27a3`。原始媒体实际为 JPEG/JFIF，当前版本化基线使用正确扩展名 `site-baseline/static/party/party-header-banner.jpg`，保持原始字节不变。正式运行只使用本地 `/static/party/party-header-banner.jpg`，不得直接依赖原站资源 URL。Human Review 已否决 WebP/AVIF 二次有损派生作为正式 Banner。

---

## EU-26 — Party Evidence & Authority Convergence

### Goal

固化原站截图、DOM、CSS、静态资源、内容线和迁移映射证据。

### Decisions

- 复用 Column + Article；
- `party` 父栏目组织四个真实子栏目；
- “学习园地”为页面分组；
- 顶部图片轮播复用 CmsList；
- canonical URL 使用 `/party/`、`/party/column/{alias}`、`/party/article/{id}`；
- legacy `plist/pdetail/detail` 只作为迁移输入；
- 原站 Mobile 固定 1200px 横向溢出不作为新版目标。

---

## EU-27 — Party CMS Structure & Content Routing

### Goal

建立可重复初始化的中心党建栏目与轮播容器结构，完成入口页、栏目、详情真实内容路由闭环。

### Scope

- Flyway 建立父栏目与四个子栏目；
- CmsList 轮播容器 `imagePolicy=REQUIRED`；
- Party App/Router 内容主题隔离；
- `PartyHomeView / PartyColumnView / PartyArticleView`；
- 入口页从四个 Column 加载 Article 摘要；
- 入口页从 CmsList 加载轮播；
- INTERNAL / EXTERNAL_LINK；
- 栏目 scoped pagination；
- 非党建文章隔离。

### Human Review semantic correction

V14 初始使用 `PARTY_HOME_CAROUSEL / 中心党建首页轮播`。2026-09-03 Human Review 明确中心党建不是独立首页，而是主站特殊栏目/专题页面，因此 V15 原地更新为 `PARTY_CAROUSEL / 中心党建轮播`。列表 ID 和成员关系保持不变。

---

## EU-28 — Party Visual Fidelity Convergence

### Goal

依据原站证据完成中心党建 Banner、共享 Navigation/Footer、轮播、高层声音、工作动态、学习园地、栏目和文章详情的正式视觉收敛，并完成当前技术命名重构。

### Scope

- Party-owned：Banner、内容 Frame、轮播、栏目/详情内容主题；
- Shared：Navigation/Footer DOM、交互、字体、响应式与稳定机构信息；
- Desktop / Mobile；
- `party / Party` 与 `party-home / PartyHome` 技术命名收敛；
- Current Screenshot Evidence；
- AI Visual + Human Visual Review。

### Human Review Findings

截至 2026-09-03，本轮新增并必须关闭的 Finding：

1. **Banner 清晰度与资源归属**：WebP/AVIF 二次有损编码会使标题文字边缘出现毛刺。正式基线保存原站原始 JPEG 字节为 `party-header-banner.jpg`，运行时只引用本地 `/static/**` 路径，不直接依赖原站资源地址。
2. **Banner 点击语义**：Banner 不属于导航入口。Header 使用纯展示 `<div> + <img>`，不得含 `<a>`。
3. **“首页”业务语义错误**：中心党建属于主站特殊栏目/专题页面。CmsList 与文档使用“中心党建轮播”；`PartyHome / party-home` 只表示 `/party/` 入口页技术角色。
4. **公共 Shell**：Main / Party Navigation 与 Footer 使用同一 Shared Components，仅主题色不同。
5. **导航视觉**：一级菜单恢复原站 16px bold；二级菜单必须与一级菜单同主题底色、白色粗体，并使用主题深色 hover/active。
6. **Favicon**：Main / Party Entry 都必须从版本化 `/static/brand/site-favicon.png` 实际加载 favicon，不能只验证文件存在。
7. **外部展示资源依赖**：公开站设计模板所需稳定图片、图标、二维码、字体等必须本地版本化或来自受控 CMS 静态资源；业务外链除外。新增自动契约扫描模板中的外部媒体 `src/poster`、CSS `url(http...)` 和资源型常量。
8. **技术命名**：当前源码、目录、测试、静态基线和 Party 专项 Authority 统一从 `party-building / PartyBuilding` 收敛为 `party / Party`；入口组件使用 `PartyHomeView.vue`。已执行 V13/V14 与当前 PR 分支名保留历史/兼容标识，不改写历史。

### Acceptance

- 本地 `party-header-banner.jpg` 与原站证据保持相同 3072×512 与 SHA-256；Browser natural size 验证通过；
- Banner DOM 无 `<a>`；
- 当前 CmsList 为 `PARTY_CAROUSEL / 中心党建轮播`；
- 父栏目当前 alias 为 `party`；
- 源码目录为 `src/sites/party/`，入口组件为 `modules/home/PartyHomeView.vue`，route name 为 `party-home`；
- 除历史 V13/V14 和当前未改名 PR 分支外，不再使用 `party-building / PartyBuilding` 当前技术命名；
- 公开站模板外部静态资源契约扫描通过；
- Main / Party Shared Navigation/Footer 无结构漂移；
- Main / Party Desktop 一级导航均为 16px/700，二级菜单为主题底色 + 白色粗体 + 深色 hover/active；
- Main / Party favicon 运行时 HTTP 与 HTML Entry 契约通过；
- 390px 无横向溢出；
- Backend + Public + Admin + Integrated Browser 成功；
- AI Visual Review 无未处理高优先级差异；
- Human Visual Review 通过后方可合并 PR #48。

---

## EU-29 — Party Historical Content Migration & Final Review

### Goal

建立与 Flyway 分离的历史内容迁移机制，导入足以支持正式运营/复核的原站文章、外链、正文资源和中心党建轮播成员，关闭本阶段。

### Scope

- 迁移输入格式与幂等键；
- 保留 legacy `content_id / typeCode / detail path`；
- 映射四个子栏目；
- INTERNAL / EXTERNAL_LINK；
- 可可靠取得的正文图片/附件进入 Resource；
- 可可靠取得的历史轮播成员映射为 `PARTY_CAROUSEL` 列表项；
- 无法迁移项形成明确报告；
- 如真实产品需要，再实现 legacy URL 重定向；
- 最终 AI + Human Review。

### Non-goals

- 不把历史运营内容写进 Flyway；
- 不为旧 URL 保留旧模板；
- 不因历史特殊样式扩展通用 Page Builder。

### Acceptance

- 迁移幂等，不重复创建同一 legacy 内容；
- 四类栏目有代表性真实内容；
- INTERNAL / EXTERNAL_LINK 映射正确；
- 轮播成员进入 `PARTY_CAROUSEL`；
- 无法自动迁移项有报告；
- 中心党建入口、栏目、详情与 Main/Admin 回归通过；
- Human Review 通过后 Roadmap 才可切换到后续阶段。
