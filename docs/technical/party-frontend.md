# 中心党建前端技术方案（Technical Plan）

本文描述主站“中心党建”特殊栏目/专题页面的前端实现。原站页面英文业务表述为 **Party Members’ Home**；当前工程技术命名统一使用 `party / Party`，仅 `/party/` 入口页使用 `party-home / PartyHome`，例如 `PartyHomeView.vue`。

## 1. 业务边界与技术边界

业务上，中心党建属于主站信息架构下的特殊栏目/专题页面，不是第二个网站，也不定义独立“首页”。

技术上继续保留：

- `party.html` Entry；
- Party App / Router；
- `/party/**` namespace；
- Party Banner、内容 Frame、轮播 DOM 和内容模板；
- 红色主题变量。

这些技术隔离用于避免红色主题与主站内容模板相互污染，不表示产品上存在第二个独立站点。

Main / Party 共同复用：

- API client / CMS DTO；
- SiteConfig / Navigation 数据；
- `shared/carousel/useContentCarousel.ts` 无主题轮播生命周期；
- `shared/components/PublicNavigation.vue`；
- `shared/components/PublicFooter.vue`；
- `shared/styles/public-shell.css`。

当前 Party 源码目录为 `frontend/public-site/src/sites/party/`；入口组件为 `modules/home/PartyHomeView.vue`。不得重新引入 `party-building / PartyBuilding` 当前命名；已执行 Flyway 历史属于例外。

## 2. 数据边界

- 父栏目：`party`；
- Party 允许子栏目：`party-voice / party-work / party-rules / party-study / party-theme-education`；
- PartyHome 固定内容查询：前四个栏目；`party-theme-education` 不新增固定首页内容区；
- 文章：通用 Article，栏目作用域服务端分页；
- 中心党建轮播：通用 CmsList，当前 code **`PARTY_CAROUSEL`**；
- 轮播 item：通用 `LINK / ARTICLE`；
- SiteConfig：`CAROUSEL_INTERVAL_SECONDS / CAROUSEL_MAX_ITEMS` 与 Main 共用；
- 历史内容：EU-29 接受基线 + EU-30 主题教育候选扩展，不写入 Flyway 历史内容 SQL。

V14 已执行且曾创建 `PARTY_HOME_CAROUSEL`。不得修改 V14；V15 原地更新该列表的 code/name/description，保留列表 ID 和已有成员关系。V13/V14 中历史父栏目 alias `party-building` 不回写；V16 原地收敛当前 alias 为 `party`。EU-30 通过新 migration 增加 `party-theme-education` 和轮播通用引用字段，不改写历史 migration。

## 3. 路由

- `/party/`：中心党建入口页，route name `party-home`；
- `/party/column/:alias`：route name `party-column`；
- `/party/article/:id`：route name `party-article`。

入口组件使用 `PartyHomeView.vue`；栏目和文章组件分别使用 `PartyColumnView.vue`、`PartyArticleView.vue`。

Router / Article View 必须确认目标栏目属于中心党建五个允许子栏目集合。Shared Navigation 通过 `siteRoot=/party` 处理当前 Entry 内路由，Main Entry 将 `/party/**` 视为跨 Entry document navigation。

ARTICLE 轮播项不持久化 Party canonical URL：`INTERNAL` 由 PartyHome 根据 `articleId` 生成 `/party/article/{id}`；`EXTERNAL_LINK` 的当前 Article 外链由 Backend 在公开查询时解析到通用 `CmsListItem.url`，Party 前端只消费解析后的 `item.url`，不维护独立 `externalUrl` 列表 DTO 字段。

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

公开站模板所需的稳定图片、图标、二维码、字体等展示资源只能来自版本化 `site-baseline/static/**` 或受控 CMS 静态资源路径 / Resource。

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

`PARTY_HOME_COLUMN_ALIASES` 只包含 `party-voice / party-work / party-rules / party-study`。Party Router 允许集合额外包含 `party-theme-education`，避免把“可访问内容作用域”和“首页固定区块”混为同一数组。

## 7. Party Carousel 实现

`PartyHomeView.vue`：

1. 加载 `PARTY_CAROUSEL`；
2. 加载公开 SiteConfig；
3. 将 `CAROUSEL_INTERVAL_SECONDS`、`CAROUSEL_MAX_ITEMS` 按正整数解析，分别 fallback 4 / 5；
4. 把 list items 交给 `useContentCarousel`；
5. Party 组件只负责路由解析和视觉 DOM。

数据目标：

- LINK：使用列表项自身解析后的 `item.url / openMode`；
- ARTICLE INTERNAL：`/party/article/{articleId}`；
- ARTICLE EXTERNAL_LINK：Backend 依据关联 Article 当前 `externalUrl` 解析并写入公开 DTO 的通用 `item.url`；Party 不读取不存在的 `item.externalUrl`；
- 图片：优先 `effectiveImageResourceId -> /api/public/resources/{id}/content`，否则使用 `imagePath`。

交互：

- mouseenter `pause('hover')`，mouseleave `resume('hover')`；
- focusin `pause('focus')`；focusout 只有真正离开容器才 resume；
- visibility 和 reduced-motion 由 composable 统一处理；
- dots 使用原生 `<button>`，`aria-label="查看第 N 项：标题"`；
- pause/resume 不重置 active index；
- 图片 error 调用 `markImageFailed(id)`。

视觉：

- `.party-carousel` Desktop `height:329px`；
- `<=900px` 使用 `height:auto; aspect-ratio:585/329`；
- item absolute overlap + `.35s opacity`；
- `prefers-reduced-motion:reduce` 时 transition none；
- 图片 `object-fit:cover`；
- EU-30 不实现 swipe。

## 8. 历史 position 2 ARTICLE 修订

Canonical `party-carousel:position:2`：

- `sourceType=ARTICLE`；
- `articleRef.sourceSystem=legacy-jilinjobs`；
- `articleRef.legacyKey=zhutijiaoyu:content:154659859759104`；
- 迁移时通过 `cms_article_legacy_mapping` 解析实际 `article_id`；
- 原轮播 PNG 通过 `ResourceService` 导入并写入 `image_resource_id`；
- migration mapping 继续保留 legacy URL / source fingerprint / image SHA-256；
- Runtime 不使用 legacyKey、标题或旧站 URL 查 Article。

position 1 / 3 / 4 保持 LINK 语义和 migrated static image path。

## 9. 验证

最终 Head 至少验证：

- Fresh Flyway 后父栏目当前 alias 为 `party`，存在五个允许 Party 子栏目，其中 `party-theme-education` 名称为“主题教育”；
- PartyHome 固定四栏目关系不变，不额外出现第五个首页固定区；
- Fresh Flyway 后存在 `PARTY_CAROUSEL / 中心党建轮播`，不存在当前 `PARTY_HOME_CAROUSEL`；
- Main / Party 使用相同 Navigation/Footer component marker 和 `useContentCarousel` 生命周期；
- `CAROUSEL_INTERVAL_SECONDS=4 / CAROUSEL_MAX_ITEMS=5` 为 Fresh DB 基线，旧 Main-only key 不存在；
- reduced-motion 下超过 interval 不自动切换，dot 仍可手动切换；
- ARTICLE 投放进入 `/party/article/{id}`，且 Article columnId 保持不变；
- EXTERNAL_LINK ARTICLE 修改 Article 外链后，公开轮播下一次查询解析出的 `CmsListItem.url` 随 Article 当前地址变化；
- Article withdraw 后 ARTICLE item 从公开 `PARTY_CAROUSEL` 消失，覆盖 Resource 不再公开；
- Canonical Fresh DB import 当前导入 183 篇，EU-29 accepted 181 与 EU-30 candidate 2 可独立审计；
- position 2 的 article legacy identity、Runtime article_id 和 Resource bytes SHA-256 一致；
- 二次 articles / carousel import 幂等；
- Desktop Main/Party 一级菜单均为 16px/700；二级菜单分别使用蓝/红主题底色、白色粗体和深色 hover/active；
- `/static/party/party-header-banner.jpg` 可从版本化静态基线读取，Browser natural size 为 3072×512；
- Banner `<img>` 使用本地 `/static/**` 路径，`.party-banner` 内无 `<a>`；
- WebP/AVIF 派生 Banner 不再作为正式资源；
- `/static/brand/site-favicon.png` 对 Main/Party Entry 均以 `image/png` 正常返回，并存在有效 favicon link 声明；
- 外部静态资源契约扫描无违规项；
- 390px 无横向溢出；
- Main / Admin 无功能回归；
- Current Screenshot + AI Visual + Human Review 共同完成 EU-30 最终收敛。