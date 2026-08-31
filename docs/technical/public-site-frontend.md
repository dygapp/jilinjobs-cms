# 公开站前端技术计划（Technical Plan）

## 1. 目标

`frontend/public-site` 只负责公开页面与固定工程集成，不承载 CMS 管理页面。CMS 提供数据，具体页面展示规则固化在公开站页面实现和验证中。

## 2. 首页数据装配

首页并行加载：Navigation、Article、SiteProperty、CmsList、Advertisement。

映射规则：

- `MAIN` → Header 主导航；
- `HOME_SHORTCUT` → 首屏右侧快捷入口，并直接使用 Navigation `iconPath`；
- `HOME_QUICK` → 快速导航，并直接使用 Navigation `iconPath`；
- `HOME_CAROUSEL` → 轮播数据，页面消费 imagePath，URL 可选；
- `SITE_LINKS` group → 网站导航 Tab；
- `HOME_RECRUITMENT_PROMO` → 招聘活动宣传展示；
- CONTACT_PHONE 等 → 网站属性；
- NCSS → 本地固定常量 + 版本化静态资源。

删除旧 JSON merge/fallback 逻辑，避免两个 Authority 同时生效。删除 `top-nav-${index}`、`guide-${index}` 等按数据位置推导业务图标的逻辑。

## 3. 列表展示契约

CmsList 不提供 displayMode/itemType。页面代码自行决定消费字段：

- `HOME_CAROUSEL`：选择有 imagePath 的有效项；URL 有值时生成链接，没有 URL 时只渲染图片；标题当前作为 caption/alt 数据使用；
- `SITE_LINKS`：当前页面使用 title + URL；如未来设计改为 Logo 或 Logo+标题，直接读取已有可选 imagePath，不改变 CMS 列表定义。

页面显示模式属于前端工程设计，不回写成 CMS 可配置展示模式。

## 4. 工程资产

NCSS 区域继续使用 `/static/home/ncss-logo.png` 和固定目标 URL。其变更视为工程需求变更。

首页布局、区域顺序、Header/Footer、页面 Shell 保持代码实现；CMS 只提供实际需要运营维护的数据。

`/static/icons/**` 可以保存版本化图标文件，但导航条目与图标的对应关系来自 Navigation `iconPath`。

## 5. 回归

保持 `/column/**`、`/article/**`、`/page/**`、SEO、响应式和现网视觉 E2E。新增断言证明首页由新 CMS API 图标/列表/宣传展示数据驱动。
