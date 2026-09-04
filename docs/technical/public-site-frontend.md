# 公开站前端技术计划（Technical Plan）

## 1. 目标架构

`frontend/public-site` 只负责公开页面与固定工程集成，不承载 CMS 管理页面。公开前端采用 **Multi-entry Modular SPA**，Entry 按真实 Theme / Router Boundary 划分，而不是按普通页面类型划分。

当前结构：

```text
frontend/public-site/
├── index.html                 # Main Site Entry
├── party.html                 # Party Entry
└── src/
    ├── shared/
    │   ├── api/
    │   ├── carousel/
    │   │   └── useContentCarousel.ts
    │   ├── components/
    │   │   ├── PublicNavigation.vue
    │   │   └── PublicFooter.vue
    │   ├── styles/
    │   │   └── public-shell.css
    │   └── seo.ts
    └── sites/
        ├── main/
        │   ├── app/
        │   ├── shell/
        │   ├── modules/
        │   │   ├── home/
        │   │   ├── content/
        │   │   ├── page/
        │   │   └── integration/
        │   └── styles/
        └── party/
            ├── app/
            ├── shell/
            ├── modules/
            │   ├── home/
            │   └── content/
            └── styles/
```

中心主站与中心党建当前同 package、同 Vite build、同部署、同 Spring Boot CMS Backend。两者分别拥有 App、Router、Banner、内容 Frame 与页面主题；Navigation/Footer 通过 Shared Shell Components 共享结构、交互和响应式，仅由 theme variables 切换蓝色/红色。EU-30 进一步把已经证明稳定一致的轮播**行为生命周期**抽到 `shared/carousel`，但不共享 Site-specific Carousel DOM / CSS。

不引入 Module Federation；不建立 `frontend/party` 独立工程；EU-30 不引入第三方 Carousel。后续只有出现独立发布/部署、不同团队或技术栈、明显不同生命周期等真实需求时再重新评估工程拆分。

中心党建 Foundation 架构已经完成；正式党建前端实施细节由 `docs/technical/party-frontend.md` 接续，本文件继续承担 Main / Party 两个 Entry 的总体工程边界。

## 2. Entry 与 Runtime 路由

### 2.1 Main Site Entry

Main Site Entry 使用 `index.html`，承载：

- `/`
- `/column/**`
- `/columns/**` 兼容地址
- `/article/**`
- `/articles/**` 兼容地址
- `/page/**`

原 `page.html / page-main.ts` 已删除。`/page/**` 继续保持公开 canonical URL，但不再拥有重复 Vue bootstrap。

Main Router 只组合主站页面路由，各页面组件使用动态 `import()`：

```text
home        -> 首页
content     -> 栏目 + 文章
page        -> 独立单页 + 单页分组
integration -> 稳定外部集成页面
```

### 2.2 Party Entry

Party Entry 使用 `party.html`，Nginx 对 `/party/**` fallback 到该 Entry。正式 Party Router 承载：

- `/party/`：中心党建入口页（`PartyHome`）；
- `/party/column/:alias`：党建栏目列表；
- `/party/article/:id`：党建站内文章详情；
- Party catch-all：保持在 Party Entry 内处理，不回落 Main Router。

Party Entry 拥有独立红色内容主题、Banner 和页面 Frame；Navigation/Footer 复用 Shared Components，不复制 Main DOM，也不依赖 Main 私有 CSS。正式实现细节见 `docs/technical/party-frontend.md`。

### 2.3 Gateway

```text
/api/**      -> Backend
/static/**   -> Backend / static resources
/admin/**    -> Admin frontend
/party/**    -> Public Party Entry
其他公开路径 -> Public Main Site Entry
```

用户不直接访问 `party.html`；公开 URL 与实际 HTML Entry 文件名解耦。

## 3. Shared 与 Site Ownership

`src/shared/` 放跨 Main / Party 两个 Entry 已经证明具有长期复用价值的能力：

- API transport 与 CMS DTO；
- 静态资源 URL helper；
- SEO / metadata helper；
- 无主题通用 utility；
- `useContentCarousel.ts`：轮播有效项、当前项、timer、pause reasons、visibility、reduced-motion、图片失败剔除和最大项数量；
- `PublicNavigation.vue`：统一一级/二级菜单、active、external/newWindow、Desktop/Mobile 交互；
- `PublicFooter.vue`：统一机构信息、备案、事业单位图标、微信公众号二维码和响应式结构；
- `public-shell.css`：共享 Navigation/Footer 结构样式以及蓝/红主题变量。

以下内容继续归各 Entry：

- Main 顶部平台条与 Main Banner；
- Party Banner；
- Page Frame；
- 首页/专题内容布局；
- Main / Party 轮播 DOM、比例、caption、dot 视觉和主题动画样式；
- Site-specific 内容 Theme 和页面模板。

共享的判断依据是稳定产品/技术职责，不是简单代码相似。若未来某 Entry 要求不同的菜单层级、Footer 信息架构或轮播基础行为，应先形成 Requirement Change，不通过 Site-local 逻辑静默分叉。

## 4. Main Site 数据装配

Main Site App 统一装配有界 Navigation + SiteProperty 快照；首页再按真实业务作用域加载 Article、CmsList、Advertisement，不恢复全站前 N 条后前端过滤。

映射规则：

- `MAIN` → Shared Navigation；
- `HOME_SHORTCUT` → 首屏右侧快捷入口，并直接使用 Navigation `iconPath`；
- `HOME_QUICK` → 快速导航，并直接使用 Navigation `iconPath`；
- `HOME_CAROUSEL` → 主轮播 CmsList；
- `CAROUSEL_INTERVAL_SECONDS` → Main / Party 共用轮播自动切换间隔，默认 4 秒；
- `CAROUSEL_MAX_ITEMS` → 单个轮播区域前台最大有效项，默认 5；
- `SITE_LINKS` group → 网站导航 Tab；
- `HOME_RECRUITMENT_PROMO` → 招聘活动宣传展示；
- CONTACT_PHONE 等 → 网站属性；
- NCSS → 本地固定常量 + 版本化静态资源。

`HOME_CAROUSEL_INTERVAL_SECONDS` 已由 V19 原地收敛为 `CAROUSEL_INTERVAL_SECONDS`，Main 不再读取旧 key。删除旧 JSON merge/fallback 逻辑，避免两个 Authority 同时生效。不得恢复 `top-nav-${index}`、`guide-${index}` 等按数据位置推导业务图标的逻辑。

Main Navigation 中“中心党建”预置项通过 V13 为 `LINK /party/`，默认当前窗口进入 Party Entry。

## 5. Shared Carousel 生命周期

`useContentCarousel<T extends { id:number }>` 接收：

- `items: Ref<T[]>`；
- `intervalSeconds: Ref<number>`；
- `maxItems: Ref<number>`。

内部状态：

- `activeIndex`：当前有效项 index；
- `failedIds`：本次页面生命周期已加载失败图片 ID；
- `pauseReasons`：允许 `hover / focus / visibility` 等原因叠加；
- `reducedMotion`：由 `matchMedia('(prefers-reduced-motion: reduce)')` 驱动；
- `timer`：仅在有效项 > 1、无暂停理由且非 reduced-motion 时存在。

`visibleItems` 先剔除 failed IDs，再按 `CAROUSEL_MAX_ITEMS` 截断。有效项变化时必须 clamp `activeIndex`，避免失败当前项后落到越界 index。

行为：

- 0 项：`activeItem=null`，不启动 timer；
- 1 项：静态，不启动 timer；
- 多项：`setInterval` 循环切换；
- `select(index)` 手动切换后重建 timer，但不改变其他 pause reason；
- `pause(reason)` / `resume(reason)` 使用 Set，解除一个原因不得覆盖其他仍有效的暂停原因；
- hover / focus / visibility 恢复时保留当前 index；
- `focusout` 只有 focus 真正离开轮播容器才解除 focus pause；
- 页面 `visibilitychange` hidden/visible 控制 visibility pause；
- reduced-motion 直接不 schedule autoplay；
- `markImageFailed(id)` 把失败项排除并由 watch 重新计算有效集合；
- unmount 必须清 timer、visibility listener、MediaQuery listener。

当前最低 interval Runtime 防御值为 1 秒；Backend 正常配置要求大于 0，默认 4 秒。

## 6. 列表与文章展示契约

CmsList 不提供 displayMode。CmsListItem 采用两种来源：

### LINK

- 使用列表项自身 `title / subtitle / url / imagePath`；
- `HOME_CAROUSEL` LINK URL 为空时渲染静态图片，不产生伪链接。

### ARTICLE

- 使用 `articleId` 建立展示投放关系；
- Backend 公开查询只输出关联 `PUBLISHED` Article 的有效项；
- title、articleType、externalUrl 等有效公开数据来自关联文章当前值；
- Main INTERNAL 目标由前端生成 `/article/{id}`；
- Party INTERNAL 目标由 Party 生成 `/party/article/{id}`；
- EXTERNAL_LINK 使用 Article 外部 URL；
- 列表覆盖图片以 `imageResourceId` 表达，公开端使用 `effectiveImageResourceId`，没有覆盖图时可继承文章可用图片；
- 投放不修改 Article `columnId`。

页面消费：

- `HOME_CAROUSEL`：消费有效 LINK / ARTICLE + 有效图片；
- `PARTY_CAROUSEL`：同一 DTO / 生命周期，使用 Party route/theme；
- `SITE_LINKS`：当前 imagePolicy=NONE，页面使用 title + URL。

Public Article Summary 的可选 `coverResourceId` 只是可消费数据；具体栏目列表是否展示封面按页面设计决定，Column coverPolicy 不参与 DOM 模板分支。

中心党建当前五个允许内容栏目同样复用 Public Article Summary / Detail；PartyHome 固定内容区仍只查询 `party-voice / party-work / party-rules / party-study`，`party-theme-education` 只进入 Party 内容路由/历史迁移和可选投放。

页面显示模式属于前端工程设计，不回写成 CMS 可配置展示模式。

## 7. Site-specific Carousel 视觉

Main：

- `HOME_CAROUSEL` `imagePolicy=REQUIRED`；
- 容器使用稳定 `aspect-ratio: 8 / 5`，使既有 Desktop 约 400×250 的比例自然延伸到移动端；
- 图片 `object-fit: cover`；
- 使用短 opacity fade；
- dots 保持低侵入视觉，不增加大面积左右箭头；
- reduced-motion media query 下 transition 为 none。

Party：

- `PARTY_CAROUSEL` `imagePolicy=REQUIRED`；
- Desktop 约 585×329；
- `<=900px` 使用 `aspect-ratio:585/329`；
- opacity fade 和 Party dot 样式由 Party CSS 持有；
- reduced-motion 下关闭 transition。

EU-30 不实现 touch swipe。移动端使用现有可聚焦 dots 手动选择。

## 8. 工程资产与 Theme

Main：

- 继续使用现有蓝白主题和视觉基线；
- NCSS 区域继续使用固定工程集成；
- 顶部平台条、Main Banner、页面 Frame、首页区域布局属于 Main 工程资产。

Party：

- Foundation 红色 Theme 已证明 Theme / Router 隔离；
- 正式阶段按原站证据重构 Banner、PartyHome、栏目和详情视觉；
- Foundation 占位文案、伪品牌元素和临时 CSS 不作为最终 Authority；
- 可靠取得并验证的党建稳定视觉资源进入 `site-baseline/static/party/**`；
- 历史文章正文图片属于内容迁移，不混入工程静态基线。

Main / Party 公共 Navigation/Footer 属于 Shared Shell 工程资产。两者结构和交互保持一致，Main 使用蓝色 theme，Party 使用红色 theme。

`/static/icons/**` 可以保存版本化图标文件，但导航条目与图标的对应关系仍来自 Navigation `iconPath`。

## 9. 构建与验证

### 9.1 Public Frontend Build

一次 `npm run build` 必须同时验证 Main 与 Party 两个 Entry 可成功构建。Vite multi-input 仅保留真实 Entry：

```text
main  -> index.html
party -> party.html
```

不得恢复以普通页面类型建立 HTML Entry 的模式。

### 9.2 EU-30 Browser Verification

在既有 Main/Party Regression 上增加：

- Fresh DB 中只有 `CAROUSEL_INTERVAL_SECONDS / CAROUSEL_MAX_ITEMS`，无旧 Main-only key；
- Main / Party 都受同一 `CAROUSEL_MAX_ITEMS` 控制；
- reduced-motion 下等待超过 interval 不自动切换，但手动 dot 仍能切换；
- ARTICLE 投放可进入 Main / Party canonical article route；
- ARTICLE 加入列表后 Article `columnId` 不变；
- Article withdraw 后对应公开列表项消失；
- 列表专用 Resource 只在有效公开 ARTICLE 投放时公开；
- Main / Party responsive ratio 与无横向溢出保持；
- 图片失败、单项、零项等生命周期边界至少通过针对性单元/Browser evidence 或代码路径验证。

### 9.3 Migration Verification

Canonical Verification 必须：

- 保持 EU-29 `acceptedSnapshot` 181 篇原值与 provenance；
- 单独识别 EU-30 `candidateExtension` 2 条；
- Fresh DB 当前 Runtime dataset 导入 183 篇；
- 第二次导入 183 篇全部 SKIPPED；
- PARTY_CAROUSEL position 2 为 ARTICLE；
- `sourceSystem + legacyKey` 解析到 Runtime article_id；
- 原轮播 PNG 写为 `image_resource_id`，实际 storage bytes SHA-256 与 Canonical 一致；
- 其他 LINK 项继续使用 static migrated path；
- 二次 Carousel import 全部 SKIPPED。

## 10. 实施顺序

EU-23～EU-29 已完成并转为追溯；EU-30 对历史内容发现形成定向修订，不重新打开整个 EU-29。

当前顺序：

1. EU-30：Carousel Architecture & Behavior Convergence；
2. EU-30 Current Evidence：Backend / Main / Party / Admin / Canonical / Browser；
3. EU-30 Human Review：轮播视觉、交互、主题教育增量内容与历史 position 2 ARTICLE 关系；
4. Human Review PASS 后收敛 migration candidate 状态、Roadmap 和 PR；
5. EU-31：Browser Compatibility & Runtime Guard Convergence。

最终视觉和历史增量接受声明必须额外满足 AI Visual / Human Review；PR #58 在人工合并指令前保持未合并。
