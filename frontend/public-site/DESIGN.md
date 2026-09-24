---
version: alpha
name: JilinJobs Public Site
description: 吉林就业公开站 Main 与中心党建的当前视觉设计 Authority
colors:
  primary: "#005CD4"
  primary-hover: "#00439A"
  interactive: "#006AF5"
  accent: "#00AEBD"
  background: "#F5F8FC"
  surface: "#FFFFFF"
  text-primary: "#323B47"
  text-secondary: "#515C6B"
  text-muted: "#8491A1"
  border-neutral: "#EBEEF2"
  soft-blue: "#D3E6FF"
  party-primary: "#D00023"
  party-deep: "#AD001D"
  overlay-dark: "rgba(0, 18, 41, 0.44)"
typography:
  home-card-tab:
    fontFamily: "Microsoft YaHei, PingFang SC, Arial, sans-serif"
    fontSize: 17px
    fontWeight: 600
    lineHeight: 1
  site-navigation-tab:
    fontFamily: "Microsoft YaHei, PingFang SC, Arial, sans-serif"
    fontSize: 16px
    fontWeight: 600
    lineHeight: 1.4
  site-navigation-link:
    fontFamily: "Microsoft YaHei, PingFang SC, Arial, sans-serif"
    fontSize: 15px
    fontWeight: 400
    lineHeight: 1.6
  metadata:
    fontFamily: "Microsoft YaHei, PingFang SC, Arial, sans-serif"
    fontSize: 13px
    fontWeight: 400
    lineHeight: 1.4
  party-section-title:
    fontFamily: "Microsoft YaHei, PingFang SC, Arial, sans-serif"
    fontSize: 24px
    fontWeight: 400
    lineHeight: 32px
  party-carousel-caption:
    fontFamily: "Microsoft YaHei, PingFang SC, Arial, sans-serif"
    fontSize: 14px
    fontWeight: 700
    lineHeight: 20px
rounded:
  none: 0px
  sm: 4px
  pill: 9999px
spacing:
  hairline: 1px
  accent-bar: 3px
  home-tab-height: 32px
  main-navigation-height: 60px
  content-max-width: 1200px
  party-carousel-width: 585px
  party-carousel-height: 329px
components:
  page-canvas:
    backgroundColor: "{colors.background}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.none}"
  main-navigation:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.surface}"
    height: "{spacing.main-navigation-height}"
  main-navigation-hover:
    backgroundColor: "{colors.primary-hover}"
    textColor: "{colors.surface}"
  primary-link:
    textColor: "{colors.interactive}"
  home-card-tab:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.accent}"
    typography: "{typography.home-card-tab}"
    rounded: "{rounded.sm}"
    height: "{spacing.home-tab-height}"
    padding: "0 20px"
  home-card-tab-divider:
    backgroundColor: "{colors.border-neutral}"
    width: "{spacing.hairline}"
    height: "{spacing.home-tab-height}"
  site-navigation-tab:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text-primary}"
    typography: "{typography.site-navigation-tab}"
  site-navigation-link:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text-secondary}"
    typography: "{typography.site-navigation-link}"
  metadata:
    textColor: "{colors.text-muted}"
    typography: "{typography.metadata}"
  soft-blue-surface:
    backgroundColor: "{colors.soft-blue}"
    textColor: "{colors.primary}"
  party-navigation:
    backgroundColor: "{colors.party-primary}"
    textColor: "{colors.surface}"
    height: "{spacing.main-navigation-height}"
  party-navigation-hover:
    backgroundColor: "{colors.party-deep}"
    textColor: "{colors.surface}"
  party-section-title:
    textColor: "{colors.text-primary}"
    typography: "{typography.party-section-title}"
  party-accent-text:
    textColor: "{colors.party-primary}"
  party-carousel:
    backgroundColor: "{colors.surface}"
    width: "{spacing.party-carousel-width}"
    height: "{spacing.party-carousel-height}"
  party-carousel-caption:
    backgroundColor: "{colors.overlay-dark}"
    textColor: "{colors.surface}"
    typography: "{typography.party-carousel-caption}"
    height: 40px
  pill-indicator:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.pill}"
---

# 吉林就业公开站视觉设计系统

## Overview

本文件是 `jilinjobs-cms` 公开站 Main 与中心党建（Party）的当前视觉设计 Authority。它使用 Google `DESIGN.md` 格式：YAML front matter 中的 token 是精确规范值，Markdown 正文解释这些值的设计角色、使用边界与组合规则。

本文件与 `docs/specifications/public-site.md` 并行承担不同语义责任：

- Product Requirement 决定产品为什么存在、范围和跨能力质量要求；
- `public-site.md` 决定页面、业务区域、用户可观察行为、失败行为和具有产品意义的结构关系；
- 本文件决定颜色、字体、尺寸、间距、形状、视觉组件、Main / Party 品牌 presentation 与视觉复刻规则；
- `docs/technical/public-site-frontend.md` 决定这些视觉契约如何进入当前 Public Renderer；
- Vue / CSS 只证明当前实现是什么，不拥有设计事实。

公开站的默认设计方向是**忠实保持原吉林就业网站已经确认的视觉语言**，包括视觉层级、页面密度、主要比例、色彩关系、字体层级、模块组织和 Main / Party 品牌差异。这里的“复刻原站”只指视觉意图与已确认 presentation，不要求复制旧站的技术实现。当前 Requirement / Specification 已明确修订的响应式、可访问性、iframe 滚动、失败状态和安全行为始终优先。

任何只存在于当前 CSS、尚未得到当前 Authority 或可靠原站证据支持的值，都不是因为“已经实现”就自动成为 token。不能唯一确定的视觉事项列入本文“已知视觉缺口”。

## Colors

公开站延续原站蓝、青绿强调色与浅灰蓝背景体系；中心党建在共享页面结构上使用独立红色主题。颜色 token 使用语义名称，不允许页面通过复制任意 hex 值建立第二套视觉体系。

| Token | 值 | 角色 |
| --- | --- | --- |
| `primary` | `#005CD4` | Main 导航、Footer、主品牌蓝和主要蓝色强调 |
| `primary-hover` | `#00439A` | Main 主导航 hover / active 的深蓝层级 |
| `interactive` | `#006AF5` | 普通蓝色链接、按钮或强调交互 |
| `accent` | `#00AEBD` | 首页卡片式 Tab 与青绿色交互强调 |
| `background` | `#F5F8FC` | 公开站页面基础背景 |
| `surface` | `#FFFFFF` | 卡片、正文与主要内容表面 |
| `text-primary` | `#323B47` | 标题与主要正文 |
| `text-secondary` | `#515C6B` | 次级正文和工具性文字 |
| `text-muted` | `#8491A1` | 日期、更多、辅助 metadata |
| `border-neutral` | `#EBEEF2` | 轻量边线与内容分隔 |
| `soft-blue` | `#D3E6FF` | Main 浅蓝功能表面 |
| `party-primary` | `#D00023` | 中心党建主要红色主题 |
| `party-deep` | `#AD001D` | 中心党建 hover / active / Footer 深红层级 |
| `overlay-dark` | `rgba(0, 18, 41, 0.44)` | 图片上的深色 caption 遮罩 |

Main 和 Party 可以覆盖主题色，但共享组件不得因此分叉信息架构、DOM responsibility 或交互语义。跨 Site 共享组件优先消费语义 token，而不是判断 URL、页面名称或站点别名后硬编码颜色。

## Typography

公开站以中文系统无衬线字体为主，保持原站紧凑、信息型公共服务网站的阅读密度。当前规范字体栈为 `Microsoft YaHei, PingFang SC, Arial, sans-serif`；不为了“现代化”擅自切换为明显改变字宽、密度和层级关系的展示字体。

当前已经足以形成精确 token 的角色包括：

- 首页多列小板块卡片式 Tab：`17px / 600`，与 32px Tab 高度共同形成当前人工复评通过的紧凑标题基线；
- 网站导航 Tab：`16px / 600`；
- 网站导航链接：`15px / 400`；
- metadata：`13px / 400`，用于日期、更多等辅助信息；
- Party 大板块标题：`24px / 400 / 32px`；
- Party 轮播 caption：`14px / 700 / 20px`。

尚未完成独立视觉确认的 Main 普通资讯正文、栏目页、文章详情等完整字号层级不在 YAML 中补造 token；它们继续列入“已知视觉缺口”，直到视觉复核后 promotion。

## Layout

桌面端沿用原站 **1200px 内容主轴**作为最大宽度视觉基线，但新版不复制旧站 `min-width: 1200px` 导致的横向溢出。1200px 是 desktop 的 `max-width` 设计锚点；较窄 viewport 必须自然收缩和重排。

Main 首页保持两类明确不同的 section：

- **整行大板块**：“最新招聘”“网站导航”等占满首页内容主轴一整行，使用独立 section heading 与左侧竖向视觉标记；
- **多列小板块**：通知公告、就业动态、快速导航、招聘公告等使用卡片式 Tab heading，与内容区域视觉连通。

不得为了代码复用把这两种结构重新统一成同一种标题装饰。

Main 主轮播的当前视觉比例是 **8:5**。Party 轮播使用 **585:329**，对应 YAML 中的 `party-carousel-width` / `party-carousel-height`。比例是视觉契约，不要求实现固定像素宽度；响应式场景应保持比例并避免页面级横向溢出。

首页与二级页面可以根据 viewport 重新排布，但视觉层级应保持：站点身份 → 导航 → 当前页面 / 业务区域 → 内容列表 / 主体 → Footer。移动端不能通过缩小到难以阅读的字号来维持桌面列数。

## Elevation & Depth

公开站总体采用**平面分层**而不是重阴影设计。层级主要由背景、1px 中性边线、品牌色和间距建立。

- 内容卡片以白色 `surface` 与浅色 `border-neutral` 区分；
- 首页卡片式 Tab 通过顶部强调条、左右边线与白色底边形成与正文相连的选中视觉；
- 普通资讯卡片不使用大面积阴影制造“悬浮卡片”效果；
- 导航下拉等确实需要空间层级的浮层可以使用克制阴影，但不能把这种阴影扩散到所有 section；
- 图片 caption 使用 `overlay-dark` 保证文字可读性。

## Shapes

形状语言以接近原站的直角和轻量圆角为主：

- `rounded.none = 0px` 是普通板块、分隔结构和多数公共内容容器的默认基线；
- `rounded.sm = 4px` 只用于当前已经接受的轻量控件或卡片式 Tab 上端，不把全站改造成大圆角卡片体系；
- pill / 圆形仅用于轮播指示器等明确需要的状态元素；
- 不新增与原站视觉语言无关的大圆角、玻璃拟态或高强度阴影。

首页卡片式 Tab 的轻微顶部圆角属于已接受实现，但不是改变结构关系的核心特征；其 32px 高度、17px 标题和局部强调条比圆角更具约束力。

## Components

### Main 公共 Shell

Main 使用 `primary` 蓝色主题。主导航 desktop 高度为 `60px`，hover / active 使用 `primary-hover`。导航的业务结构与 Party 共用，视觉主题可以不同。

Header、Banner、Navigation 与 Footer 组成稳定 Site frame。视觉实现应优先保持原站的横向比例、蓝白层级与公共服务网站密度，同时满足当前响应式 Requirement。

### 首页整行大板块标题

“最新招聘”“网站导航”使用独立 section heading：

- section 独占内容主轴一整行；
- 标题左侧存在竖向视觉标记；
- 不使用小板块 Tab 的 3px 顶部横条；
- 不通过给整个 section 增加通栏顶部强调线来伪装成小板块；
- 当前标题的精确字号与 marker 高度仍处于 Known Gap，不写入 YAML token。

### 首页卡片式 Tab

通知公告、就业动态、快速导航、招聘公告等多列小板块使用统一卡片式 Tab：

- Header / Tab 高度必须使用 `home-tab-height = 32px`；
- 标题使用 `home-card-tab` typography，即 17px；
- 顶部强调条使用 `accent-bar = 3px`，**只覆盖 Tab 自身宽度**；
- Tab 左右使用 `hairline = 1px` 的 `border-neutral` 边线；
- Tab 底边与内容区同为白色，使 Tab 与正文区域视觉连通；
- Header 余下区域继续保留 1px 中性底线；
- 不恢复标题文字下划线；
- 不恢复旧站 40px / 2px 的旧 Tab 几何参数；当前 32px / 3px 是后续人工复评明确形成的新基线。

### 网站导航

“网站导航”是整行大板块，而其内部分类 Tab / 链接使用独立的内容层 typography：

- 分类 Tab：`16px`；
- 链接正文：`15px`；
- 链接保持适合长中文机构名称的可读宽度和换行 / 重排能力；
- 窄屏允许模块内部滚动或重排，但不得造成页面级横向溢出。

### 首页资讯列表

标题、正文、日期和 hover 色彩在同一首页应保持统一视觉体系。当前颜色角色已经进入 token，但 Main 普通资讯正文的精确字号、行高和垂直密度尚未完成独立视觉裁决，因此实现不得把当前某个 selector 的数值反向当作长期规范；见“已知视觉缺口”。

### 轮播

Main 与 Party 共享轮播 lifecycle，但视觉比例和主题可以不同：

- Main：8:5；
- Party：585:329；
- caption 覆盖在图片底部，需保证白色文字可读；
- indicator 应明显表达当前项，但不得压过主内容；
- reduced-motion 由 Product Specification 控制，视觉设计不得通过动画要求覆盖该行为。

### Party 主题

Party 在共享 Navigation / Footer 信息结构上使用独立红色品牌 presentation：

- 主红色：`party-primary`；
- 深红 hover / active：`party-deep`；
- Party Banner 使用版本化稳定视觉资产，不依赖原站远程资源；
- 大板块标题保持 24px 与左侧 Party marker；
- Party 轮播保持 585:329；
- 黄/金色历史图形资产只在已有稳定 Party visual asset responsibility 中使用，不扩张为全站 accent。

### 栏目列表与文章详情

Main / Party 二级栏目列表共享主要 presentation primitive，不能重新分叉出两套不同 DOM / 分页能力。视觉设计应保持原站列表图标、标题 / 日期层级与清晰分页关系，同时保留 Main / Party route 与 brand scope。

当前栏目页和文章详情仍有若干精确 typography / spacing 未完成 Design token 化；现有 Vue / CSS 只作为实现证据，不能单独填补这些缺口。

### 慧就业 iframe

慧就业 iframe 内部 DOM / CSS 属于跨源外部系统，不进入本 Design Authority。本站只设计 iframe **宿主区域**：

- 首页嵌入区与相邻本站模块的外部边界、留白和标题关系应协调；
- 首页“直播课程”不重复增加宿主同名标题；
- 二级页面只保留宿主页面纵向滚动；
- 加载 / 失败 / 重试状态使用本站颜色、字体和 surface token；
- 不通过 CSS hack 假装可以长期控制跨源 iframe 内部视觉。

## Do's and Don'ts

### 应做

- 优先从本文件 token 与组件规则生成新视觉实现；
- 需要新视觉值时，先判断它是否已有原站证据、现行 Specification 或人工视觉结论；
- 保持 Main 蓝色、Party 红色与青绿色局部强调的既有关系；
- 保持公共服务网站的信息密度，不为了“现代感”任意放大间距和字号；
- 视觉修复后同时取得与变更范围匹配的 Browser / AI / Human visual evidence；
- 若实现只是恢复既有 token，不必修改 Product Requirement；若改变页面结构或用户行为，同时返回 `public-site.md`。

### 不应做

- 不从当前 CSS 的任意数值自动生成新的长期 token；
- 不把原站固定最小宽度、双滚动、过时交互等旧实现恢复为 Requirement；
- 不把首页整行大板块和多列小板块再次统一成一种标题样式；
- 不用大圆角、重阴影、玻璃拟态或全新字体重设计网站；
- 不让 Party 红色主题泄漏到 Main，也不让 Main 蓝色覆盖 Party 品牌区域；
- 不尝试跨源修改慧就业 iframe 内部样式；
- 不把视觉自动化断言当成人工视觉裁决的替代品。

## 响应式行为

响应式目标是**保留视觉身份并改变布局，不是把桌面版压缩到手机宽度**。

- Desktop 以 1200px 内容主轴作为视觉锚点；
- 中等宽度可以减少列数、增加换行并调整卡片编排；
- Mobile 优先保证导航、标题、正文、列表和主要操作可读；
- 不复制原站 `html, body { min-width: 1200px }`；
- 页面不得产生明显横向溢出；
- 精确 breakpoint 属于当前 renderer 的低风险实现参数，除非后续人工视觉裁决证明某个 breakpoint 本身具有长期设计意义，否则不提升为 DESIGN token。

## 原站复刻与证据

“原站复刻”是本文件的设计来源原则，不代表 Legacy Source 自动拥有当前 Authority。2026-09-23 本单元重新核对了以下公开来源：

| 来源 | 2026-09-23 SHA-256 | 当前角色 |
| --- | --- | --- |
| `https://24365.jl.smartedu.cn/webfile/theme2/css/reset.css` | `253d977433cb11d07c6d4d64879c7782beffe54b4bc4295e0b4a7da5d64a4165` | Legacy typography / reset evidence |
| `https://24365.jl.smartedu.cn/webfile/theme2/css/style/global.css?param=76525541` | `ff098e2a54cd482ac02ebd97264c3bd8252ef1c456d7a1db0ffc03a7dfce9a5e` | Legacy shell、颜色、Tab、列表 evidence |
| `https://24365.jl.smartedu.cn/webfile/theme2/css/style/index.css` | `7c53dc78b279649e7bee7f607cf0c776ddd80bc3f382d15115dbe3bad90e1918` | Legacy 首页 / Party layout evidence |

Party 稳定图片、marker 等已入库视觉资源的来源继续由 `sites/jilinjobs/assets/party/PROVENANCE.md` 持有。

这些外部来源只能证明“原站在该证据版本如何呈现”。只有被 YAML token、上述 component rule 或当前 Product / Specification 明确接受的部分才构成本项目当前设计 Authority。后续原站发生变化不会自动改变本文件。

## 已知视觉缺口

以下事项当前有实现或 Legacy evidence，但尚不足以写成 normative token。它们是后续视觉收敛的输入，不是本单元偷偷作出的新设计决定：

1. **Main 整行大板块标题 typography / marker 高度**：原站 `.part-title` 为 24px、marker 约 4×16px；当前实现标题为 20px、marker 高度 18px。现行 Specification 只确认“大板块 + 左侧竖向视觉标记”的结构，没有裁决精确字号 / 高度。
2. **Main 普通资讯列表字号与垂直密度**：原站绿色 Tab 内容列表使用 15px、约 17px 行高并以 14px item 间距组织；当前实现多处使用 14px 与固定 30px 行高。现行 Specification 只要求首页资讯体系一致，未裁决哪套精确密度。
3. **Main Shell 细节**：桌面 Header / Banner / Navigation 的主要比例已能由原站和当前实现恢复，但移动端 Banner 高度、导航文字密度、dropdown 间距仍主要由实现持有，尚缺独立视觉裁决。
4. **栏目列表 / 文章详情**：结构、作用域和响应式行为已有 Specification，具体 title、正文、metadata、分页的完整 visual scale 仍未形成 Design token。
5. **Party 内部卡片细节**：红色主题、稳定 Banner、24px 大标题与 585:329 轮播已有较强 evidence；部分 Tab、列表间距和详情页 typography 仍主要来自 Legacy / implementation evidence，需要后续定向复核。
6. **首页各模块整体节奏**：当前 grid gap、section gap、card padding 中仍存在 implementation-only 数值；在没有人工视觉结论前不统一提升为 spacing scale。
7. **青绿色标题对比度**：官方 DESIGN lint 对 `accent = #00AEBD` 在白底卡片式 Tab 上的当前组合报告 2.70:1，对普通文字低于其 WCAG AA 4.5:1 提示阈值。该组合来自原站色彩与已接受首页视觉基线，本单元不擅自换色；后续需在视觉忠实、字号 / 字重与基础可访问性之间做定向复核后再决定是否调整 token。

处理 Known Gap 时，先形成 Expected vs Actual 并取得必要视觉证据；确认后的长期视觉事实进入本文件，随后再修改实现。不得先改 CSS 再反向把结果登记成设计规范。

## Authority 与变更规则

- 只改变 CSS / Vue 以恢复本文件既有 token：属于 implementation fix，不需要制造新的 Design Requirement。
- 新增或改变颜色、字号、间距、形状、视觉比例、组件视觉状态：先更新本文件，并取得与影响范围匹配的视觉验证。
- 改变页面是否存在、业务区域关系、导航语义、失败行为或用户交互：返回 `docs/specifications/public-site.md`；如果同时改变视觉，再同步本文件。
- 改变跨 Feature 共享组件 / renderer 的实现责任：返回 Architecture / Technical owner，不在 DESIGN.md 中描述源码结构。
- 原站证据与当前 Requirement / Specification 冲突时，当前 Authority 优先；原站只保留 provenance。
- 当前实现与本文件冲突时，不默认认为 DESIGN stale；先判断 implementation defect、stale Design Authority 或未解决 Known Gap，再修改真实 owner。
