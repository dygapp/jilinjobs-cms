# 中心党建前端技术方案（Technical Plan）

本文描述主站“中心党建”特殊栏目/专题页面的前端实现。原站页面英文业务表述为 **Party Members’ Home**；当前工程技术命名统一使用 `party / Party`，仅 `/party/` 入口页使用 `party-home / PartyHome`，例如 `PartyHomeView.vue`。

## 1. 业务边界与技术边界

业务上，中心党建属于主站信息架构下的特殊栏目/专题页面，不是第二个网站，也不定义独立“首页”。

技术上继续保留：

- `party.html` Entry；
- Party App / Router；
- `/party/**` namespace；
- Party Banner、内容 Frame、轮播和内容模板；
- 红色主题变量。

这些技术隔离用于避免红色主题与主站内容模板相互污染，不表示产品上存在第二个独立站点。

Main / Party 共同复用：

- API client / CMS DTO；
- SiteConfig / Navigation 数据；
- `shared/components/PublicNavigation.vue`；
- `shared/components/PublicFooter.vue`；
- `shared/styles/public-shell.css`。

当前 Party 源码目录为 `frontend/public-site/src/sites/party/`；入口组件为 `modules/home/PartyHomeView.vue`。不得重新引入 `party-building / PartyBuilding` 当前命名；已执行 Flyway 历史和当前未改名 PR 分支属于例外。

## 2. 数据边界

- 父栏目：`party`；
- 子栏目：`party-voice / party-work / party-rules / party-study`；
- 文章：通用 Article，栏目作用域服务端分页；
- 中心党建轮播：通用 CmsList，当前 code **`PARTY_CAROUSEL`**；
- SiteConfig / Navigation：通用公开 API；
- 历史内容：EU-29 独立迁移，不写入 Flyway 历史内容 SQL。

V14 已执行且曾创建 `PARTY_HOME_CAROUSEL`。不得修改 V14；V15 原地更新该列表的 code/name/description，保留列表 ID 和已有成员关系。V13/V14 中历史父栏目 alias `party-building` 不回写；V16 原地收敛当前 alias 为 `party`。

## 3. 路由

- `/party/`：中心党建入口页，route name `party-home`；
- `/party/column/:alias`：route name `party-column`；
- `/party/article/:id`：route name `party-article`。

入口组件使用 `PartyHomeView.vue`；栏目和文章组件分别使用 `PartyColumnView.vue`、`PartyArticleView.vue`。

Router / Article View 必须确认目标栏目属于中心党建子栏目集合。Shared Navigation 通过 `siteRoot=/party` 处理当前 Entry 内路由，Main Entry 将 `/party/**` 视为跨 Entry document navigation。

## 4. Banner 与静态资源

原站 Banner 证据：

- 原站 URL：`https://24365.jl.smartedu.cn/webfile/theme2/img/party_banner.png`；
- 尺寸：3072×512；
- SHA-256：`7444d50235d4c87a00d0221ac84551ea083c617bb8a15e58f58d002224bd27a3`；
- 文件名虽为 `.png`，原始媒体字节实际为 JFIF/JPEG。

正式运行使用版本化本地资源：

`/static/party/party-header-banner.jpg`

仓库文件 `site-baseline/static/party/party-header-banner.jpg` 必须与原站证据 byte-for-byte 一致。不得重新编码、转 WebP/AVIF、重采样或在运行时直接访问原站 Banner URL。

Header 使用 `<div class="party-banner"><img ...></div>`：

- 不包裹 `<a>`；
- Banner 点击不产生导航；
- Desktop 容器保持 320px 高，图片 `object-fit: cover`；
- Mobile 按容器缩放并保持无横向溢出。

### 4.1 外部静态资源契约

公开站模板所需的稳定图片、图标、二维码、字体等展示资源只能来自版本化 `site-baseline/static/**` 或受控 CMS 静态资源路径。

除项目允许的开源 JS/CSS 依赖外，不允许在设计模板中直接使用：

- 外部 `img/src`、媒体 `src/poster`；
- CSS `url(http://...)` / `url(https://...)`；
- Banner / Logo / Icon / Image / Background / Font 等资源常量的外部 URL。

业务 `<a href>` 外链、文章外链、第三方业务平台入口不属于静态资源依赖，可以继续使用外部 URL。

`frontend/public-site/tests/e2e/public-external-resource-contract.spec.ts` 对 `src/**/*.{vue,ts,css}` 与两个 HTML Entry 执行自动扫描，防止模板重新引入外部展示资源依赖。

### 4.2 Favicon

Main 与 Party 两个 HTML Entry 使用同一个版本化 favicon：

`/static/brand/site-favicon.png`

两个 Entry 都显式声明 `rel="icon"`（`sizes="128x128"`）和 `rel="shortcut icon"`。验证不能只检查仓库文件存在，还必须验证运行时 HTTP 返回 `image/png`、PNG signature 正确且两个 Entry 的 HTML link 声明可见。

## 5. Shared Navigation / Footer

Main / Party 使用同一 `PublicNavigation.vue`：

- 同一一级/二级 DOM；
- 同一字体、间距、宽度分配和响应式；
- 同一 active、placeholder、external/newWindow 逻辑；
- Party 只覆盖红色主题变量。

Navigation Desktop 视觉按原站证据统一：

- 一级菜单 16px、bold、白字；
- Main 一级/二级背景 `#005CD4`，hover/active `#00439A`，二级分隔 `#004EB4`；
- Party 一级/二级背景 `#D00023`，hover/active 与二级分隔 `#AD001D`；
- 二级菜单文字保持白色粗体，子项约 50px 高；
- 不使用白底深色字的通用浮层风格。

Main / Party 使用同一 `PublicFooter.vue`：

- 同一机构信息、备案、事业单位图标、微信公众号二维码；
- 同一 Desktop / Mobile 布局；
- Party 只覆盖红色主题变量。

## 6. 中心党建入口页（PartyHome）

Desktop：

- 中心党建轮播约 585×329；
- 高层声音约 585×329；
- 工作动态单列；
- 学习园地两栏（党规党章、理论学习）。

入口页组件命名为 `PartyHomeView.vue`，route name 为 `party-home`。页面内部布局 class 可继续采用 `party-content-entry / party-entry-top / party-section-panel / party-carousel`，不要求为了组件名称机械改为 `party-home-*`。

`PARTY_CAROUSEL` 使用 CmsList URL 字段；轮播项对应本站文章时进入 `/party/article/{id}`。

## 7. 验证

最终 Head 至少验证：

- Fresh Flyway 后父栏目当前 alias 为 `party`，四个子栏目关系不变；
- Fresh Flyway 后存在 `PARTY_CAROUSEL / 中心党建轮播`，不存在当前 `PARTY_HOME_CAROUSEL`；
- Main / Party 使用相同 Navigation/Footer component marker；
- Desktop Main/Party 一级菜单均为 16px/700；二级菜单分别使用蓝/红主题底色、白色粗体和深色 hover/active；
- `/static/party/party-header-banner.jpg` 可从版本化静态基线读取，Browser natural size 为 3072×512；
- Banner `<img>` 使用本地 `/static/**` 路径，`.party-banner` 内无 `<a>`；
- WebP/AVIF 派生 Banner 不再作为正式资源；
- `/static/brand/site-favicon.png` 对 Main/Party Entry 均以 `image/png` 正常返回，并存在有效 favicon link 声明；
- 外部静态资源契约扫描无违规项；
- 四栏目与轮播真实加载；
- 轮播项点击进入对应详情；
- 390px 无横向溢出；
- Main / Admin 无功能回归；
- Current Screenshot + AI Visual + Human Review 共同完成视觉收敛。
