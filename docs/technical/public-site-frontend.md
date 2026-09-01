# 公开站前端技术计划（Technical Plan）

## 1. 目标架构

`frontend/public-site` 只负责公开页面与固定工程集成，不承载 CMS 管理页面。公开前端采用 **Multi-entry Modular SPA**，Entry 按真实 Site / Theme Boundary 划分，而不是按普通页面类型划分。

当前目标：

```text
frontend/public-site/
├── index.html                 # Main Site Entry
├── party.html                 # Party Building Site Entry
└── src/
    ├── shared/
    │   ├── api/
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
        └── party-building/
            ├── app/
            ├── shell/
            ├── modules/
            │   └── home/
            └── styles/
```

中心主站与中心党建当前同 package、同 Vite build、同部署、同 Spring Boot CMS Backend，但分别拥有 App、Router、Shell 与主题样式所有权。

不引入 Module Federation；不建立 `frontend/party-building` 独立工程。后续只有出现独立发布/部署、不同团队或技术栈、明显不同生命周期等真实需求时再重新评估工程拆分。

## 2. Entry 与 Runtime 路由

### 2.1 Main Site Entry

Main Site Entry 使用 `index.html`，承载：

- `/`
- `/column/**`
- `/columns/**` 兼容地址
- `/article/**`
- `/articles/**` 兼容地址
- `/page/**`

原 `page.html / page-main.ts` 删除。`/page/**` 继续保持公开 canonical URL，但不再拥有重复 Vue bootstrap。

Main Router 只组合主站页面路由，各页面组件使用动态 `import()`：

```text
home     -> 首页
content  -> 栏目 + 文章
page     -> 独立单页 + 单页分组
integration -> 后续稳定外部集成页面
```

### 2.2 Party Building Entry

Party Building Entry 使用 `party.html`，Nginx 对 `/party/**` fallback 到该 Entry。Party Router 当前只要求：

- `/party/`：党建基础首页框架；
- catch-all：回到 `/party/` 或显示同一基础框架，不提前发明后续业务 URL。

Party Building Site 拥有独立红色主题 Shell，不能依赖 Main Site Header/Footer 或主站 CSS 才能正常显示。

### 2.3 Gateway

```text
/api/**     -> Backend
/static/**  -> Backend / static resources
/admin/**   -> Admin frontend
/party/**   -> Public Party Building Entry
其他公开路径 -> Public Main Site Entry
```

用户不直接访问 `party.html`；公开 URL 与实际 HTML Entry 文件名解耦。

## 3. Shared 与 Site Ownership

`src/shared/` 只放明确无主题、跨 Main / Party 两个 Site 都具有长期复用价值的能力：

- API transport 与 CMS DTO；
- 静态资源 URL helper；
- SEO / metadata helper；
- 无主题通用 utility。

以下内容默认归 Site，不进入 Shared：

- Header / Footer；
- Navigation Layout；
- Page Frame；
- 颜色、字体、间距等 Theme tokens；
- 首页/专题区块布局；
- Site-specific responsive rules。

共享的判断依据是稳定技术职责，不是简单代码相似。

## 4. Main Site 数据装配

首页并行加载 Navigation、Article、SiteProperty、CmsList、Advertisement。

映射规则：

- `MAIN` → Header 主导航；
- `HOME_SHORTCUT` → 首屏右侧快捷入口，并直接使用 Navigation `iconPath`；
- `HOME_QUICK` → 快速导航，并直接使用 Navigation `iconPath`；
- `HOME_CAROUSEL` → 主轮播数据；
- `HOME_CAROUSEL_INTERVAL_SECONDS` → 主轮播自动切换间隔；
- `SITE_LINKS` group → 网站导航 Tab；
- `HOME_RECRUITMENT_PROMO` → 招聘活动宣传展示；
- CONTACT_PHONE 等 → 网站属性；
- NCSS → 本地固定常量 + 版本化静态资源。

删除旧 JSON merge/fallback 逻辑，避免两个 Authority 同时生效。不得恢复 `top-nav-${index}`、`guide-${index}` 等按数据位置推导业务图标的逻辑。

Main Navigation 中既有“中心党建”预置项通过后续 Flyway migration 从 `PLACEHOLDER` 更新为 `LINK /party/`，默认当前窗口进入 Party Building Entry。

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

Public Article Summary 的可选 `coverResourceId` 只是可消费数据；当前栏目列表是否展示封面仍按现网页面设计，Column coverPolicy 不参与 DOM 模板分支。

页面显示模式属于前端工程设计，不回写成 CMS 可配置展示模式。

## 7. 工程资产与 Theme

Main Site：

- 继续使用现有蓝白主题和视觉基线；
- NCSS 区域继续使用固定工程集成；
- Header、Footer、页面 Shell、首页区域布局属于 Main Site 工程资产。

Party Building Site：

- 本轮建立独立红色主题变量、Header、Footer、基础导航和 Page Frame；
- 只要求形成明显独立于主站的视觉骨架，不以当前基础框架冒充最终原站视觉复刻；
- 真实 Logo、Banner、栏目、图片和具体页面布局在后续专项重新取证后补齐。

`/static/icons/**` 可以保存版本化图标文件，但导航条目与图标的对应关系仍来自 Navigation `iconPath`。

## 8. 构建与验证

### 8.1 Public Frontend Build

一次 `npm run build` 必须同时验证 Main 与 Party Building 两个 Entry 可成功构建。Vite multi-input 仅保留真实 Site Entry：

```text
main  -> index.html
party -> party.html
```

不得恢复以普通页面类型建立 HTML Entry 的模式。

### 8.2 Main Site Regression

保持 `/`、`/column/**`、`/article/**`、`/page/**`、SEO、响应式和既有视觉 E2E。至少证明：

- `/page/**` 删除专用 Entry 后直接访问和刷新仍正常；
- 主站 Header / Nav / Footer 及首页主视觉无架构重构回归；
- Main Router 页面改为 lazy import 后核心页面仍正常；
- 首页现有 Navigation / CmsList / Advertisement / SiteProperty 消费契约无回归。

### 8.3 Party Building Foundation Verification

至少证明：

- `/party/` 通过独立 Entry 可直接访问和刷新；
- Party App / Router 不依赖 Main Router；
- Party Shell 使用独立红色主题基础样式；
- 主站页面不存在党建主题样式污染；
- Main Navigation “中心党建”可进入 `/party/`；
- `/admin/`、`/api/**`、`/static/**` Gateway 无回归。

当前验证只声明“党建基础框架可用”，不声明真实内容、最终视觉或专属后台能力完成。

## 9. 实施顺序

按 `docs/work/public-site-multi-entry-execution-units.md` 分步：

1. Authority / Specification / ADR / Roadmap 固化；
2. Main Site 源码模块化并移除重复 Page Entry；
3. Party Building 独立 Site Entry / Shell / Theme 基础框架与导航入口；
4. 对每个目标提交取得对应 Frontend / Backend（涉及 migration 时）/ Integrated Browser Current Evidence；
5. 基础框架完成后，另起中心党建正式页面与内容收敛任务。