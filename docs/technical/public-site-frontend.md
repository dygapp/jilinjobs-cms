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

中心主站与中心党建当前同 package、同 Vite build、同部署、同 Spring Boot CMS Backend。两者分别拥有 App、Router、Banner、内容 Frame 与页面主题；Navigation/Footer 通过 Shared Shell Components 共享结构、交互和响应式，仅由 theme variables 切换蓝色/红色。

不引入 Module Federation；不建立 `frontend/party` 独立工程。后续只有出现独立发布/部署、不同团队或技术栈、明显不同生命周期等真实需求时再重新评估工程拆分。

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
- `PublicNavigation.vue`：统一一级/二级菜单、active、external/newWindow、Desktop/Mobile 交互；
- `PublicFooter.vue`：统一机构信息、备案、事业单位图标、微信公众号二维码和响应式结构；
- `public-shell.css`：共享 Navigation/Footer 结构样式以及蓝/红主题变量。

以下内容继续归各 Entry：

- Main 顶部平台条与 Main Banner；
- Party Banner；
- Page Frame；
- 首页/专题内容布局；
- Site-specific 内容 Theme 和页面模板。

共享的判断依据是稳定产品/技术职责，不是简单代码相似。若未来某 Entry 要求不同的菜单层级、Footer 信息架构或响应式行为，应先形成 Requirement Change，不通过 Site-local DOM/CSS 静默分叉 Shared Shell。

## 4. Main Site 数据装配

Main Site App 统一装配有界 Navigation + SiteProperty 快照；首页再按真实业务作用域加载 Article、CmsList、Advertisement，不恢复全站前 N 条后前端过滤。

映射规则：

- `MAIN` → Shared Navigation；
- `HOME_SHORTCUT` → 首屏右侧快捷入口，并直接使用 Navigation `iconPath`；
- `HOME_QUICK` → 快速导航，并直接使用 Navigation `iconPath`；
- `HOME_CAROUSEL` → 主轮播数据；
- `HOME_CAROUSEL_INTERVAL_SECONDS` → 主轮播自动切换间隔；
- `SITE_LINKS` group → 网站导航 Tab；
- `HOME_RECRUITMENT_PROMO` → 招聘活动宣传展示；
- CONTACT_PHONE 等 → 网站属性；
- NCSS → 本地固定常量 + 版本化静态资源。

删除旧 JSON merge/fallback 逻辑，避免两个 Authority 同时生效。不得恢复 `top-nav-${index}`、`guide-${index}` 等按数据位置推导业务图标的逻辑。

Main Navigation 中“中心党建”预置项通过 V13 为 `LINK /party/`，默认当前窗口进入 Party Entry。

## 5. Main Site 主轮播

`HOME_CAROUSEL` 当前基线 `imagePolicy=REQUIRED`。页面仍对返回数据执行 `imagePath` 过滤作为 Runtime 防御，只将有图片的启用项加入 `validCarouselItems`。

状态：

- `activeCarouselIndex`：当前图片 index；
- `carouselIntervalSeconds`：从 SiteProperty 读取，默认 4；
- `carouselTimer`：仅在有效图片数量 > 1 时存在。

挂载后读取 `HOME_CAROUSEL_INTERVAL_SECONDS`，按正整数解析；异常或缺失时回退 4 秒。Backend 正常写入已经强制该系统属性 > 0，前端 fallback 只用于兼容历史/异常数据，不形成第二套可配置 Authority。

多图时 `setInterval` 按列表返回顺序循环修改 active index；单图不启动 timer；组件卸载时 clearInterval。URL 有值时图片按既有 openMode 形成链接；URL 为空只渲染图片。caption、动画、图片尺寸等保持页面工程设计。

## 6. 列表与文章展示契约

CmsList 不提供 displayMode/itemType。页面代码自行决定消费字段：

- `HOME_CAROUSEL`：消费 imagePath 和可选 URL；
- `SITE_LINKS`：当前基线 imagePolicy=NONE，页面使用 title + URL；未来若确认 Logo 方案，先调整列表图片数据策略和数据，再由页面读取 imagePath。

Public Article Summary 的可选 `coverResourceId` 只是可消费数据；具体栏目列表是否展示封面按页面设计决定，Column coverPolicy 不参与 DOM 模板分支。

中心党建四条正式内容线同样复用 Public Article Summary / Detail；Party 页面按自身 alias 作用域和模板消费数据，不新增 PartyArticle DTO。

页面显示模式属于前端工程设计，不回写成 CMS 可配置展示模式。

## 7. 工程资产与 Theme

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

## 8. 构建与验证

### 8.1 Public Frontend Build

一次 `npm run build` 必须同时验证 Main 与 Party 两个 Entry 可成功构建。Vite multi-input 仅保留真实 Entry：

```text
main  -> index.html
party -> party.html
```

不得恢复以普通页面类型建立 HTML Entry 的模式。

### 8.2 Main Site Regression

保持 `/`、`/column/**`、`/article/**`、`/page/**`、SEO、响应式和既有视觉 E2E。至少证明：

- `/page/**` 直接访问和刷新仍正常；
- 主站顶部 Header、Shared Navigation/Footer 及首页主视觉无党建改造回归；
- Main Router route-level lazy loading 正常；
- Main Site Context 的站点级装配与首页 scoped data 查询无回退。

### 8.3 Party Formal Verification

按 `docs/technical/party-frontend.md` 至少证明：

- `/party/`、`/party/column/**`、`/party/article/**` 直接访问和刷新；
- Party App / Router / Banner / Content Theme 不依赖 Main Router 或 Main 私有 DOM/CSS；
- Main / Party 使用同一 Navigation/Footer component marker，且只存在蓝/红主题差异；
- 四个预置党建栏目与通用 Article 闭环；
- INTERNAL / EXTERNAL_LINK 行为；
- 非党建文章不能由 Party 详情正常呈现；
- PartyHome/列表/详情 Browser E2E；
- Main / Party favicon 使用同一版本化 PNG 并在运行时正常加载；
- `/admin/`、`/api/**`、`/static/**` Gateway 无回归；
- AI Visual + Human Review 用于最终视觉声明。

## 9. 实施顺序

`docs/work/public-site-multi-entry-execution-units.md` 的 EU-23～EU-25 已完成并转为追溯。

当前按照 `docs/work/party-convergence-execution-units.md` 执行：

1. EU-26：原站证据与 Authority 收敛；
2. EU-27：党建 CMS 结构与 Party 内容路由；
3. EU-28：PartyHome 与视觉精度收敛；
4. EU-29：历史内容迁移与最终 Review。

每个目标提交取得对应 Backend / Public / Admin / Integrated Browser Current Evidence；最终视觉声明必须额外满足 AI Visual / Human Review。