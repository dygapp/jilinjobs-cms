# 公开站前端技术计划（Technical Plan）

## 1. 目标

`frontend/public-site` 只负责公开页面与固定工程集成，不承载 CMS 管理页面。

## 2. 首页数据装配

首页并行加载：Navigation、Article、SiteProperty、CmsList、Advertisement。

映射规则：

- `MAIN` → Header 主导航；
- `HOME_SHORTCUT` → 首屏右侧快捷入口；
- `HOME_QUICK` → 快速导航；
- `HOME_CAROUSEL` → 轮播；
- `SITE_LINKS` group → 网站导航 Tab；
- `HOME_RECRUITMENT_PROMO` → 招聘活动横幅；
- CONTACT_PHONE 等 → 网站属性；
- NCSS → 本地固定常量 + 版本化静态资源。

删除旧 JSON merge/fallback 逻辑，避免两个 Authority 同时生效。

## 3. 工程资产

NCSS 区域继续使用 `/static/home/ncss-logo.png` 和固定目标 URL。其变更视为工程需求变更。

首页布局、区域顺序、Header/Footer、页面 Shell 保持代码实现；CMS 只提供实际需要运营维护的数据。

## 4. 回归

保持 `/column/**`、`/article/**`、`/page/**`、SEO、响应式和现网视觉 E2E。新增断言证明首页可由新 CMS API 数据驱动。
