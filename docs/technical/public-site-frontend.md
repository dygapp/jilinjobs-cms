# 公开站前端技术计划（Technical Plan）

## 1. 目标

`frontend/public-site` 只负责公开页面与固定工程集成，不承载 CMS 管理页面。CMS 提供数据，具体页面展示规则固化在公开站页面实现和验证中。

## 2. 首页数据装配

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

删除旧 JSON merge/fallback 逻辑，避免两个 Authority 同时生效。删除 `top-nav-${index}`、`guide-${index}` 等按数据位置推导业务图标的逻辑。

## 3. 首页主轮播

`HOME_CAROUSEL` 当前基线 `imagePolicy=REQUIRED`。页面仍对返回数据执行 `imagePath` 过滤作为 Runtime 防御，只将有图片的启用项加入 `validCarouselItems`。

状态：

- `activeCarouselIndex`：当前图片 index；
- `carouselIntervalSeconds`：从 SiteProperty 读取，默认 4；
- `carouselTimer`：仅在有效图片数量 > 1 时存在。

挂载后读取 `HOME_CAROUSEL_INTERVAL_SECONDS`，按正整数解析；异常或缺失时回退 4 秒。Backend 正常写入已经强制该系统属性 > 0，前端 fallback 只用于兼容历史/异常数据，不形成第二套可配置 Authority。

多图时 `setInterval` 按列表返回顺序循环修改 active index；单图不启动 timer；组件卸载时 clearInterval。轮播 DOM 暴露测试用途的 `data-carousel-item-id`，便于 Browser E2E 证明实际切换，而不是只检查配置字段存在。

URL 有值时图片按既有 openMode 形成链接；URL 为空只渲染图片。caption、动画、图片尺寸等保持页面工程设计，不由 imagePolicy 或 interval 属性扩展成通用展示配置。

## 4. 列表与文章展示契约

CmsList 不提供 displayMode/itemType。页面代码自行决定消费字段：

- `HOME_CAROUSEL`：消费 imagePath 和可选 URL；
- `SITE_LINKS`：当前基线 imagePolicy=NONE，页面使用 title + URL；未来若确认 Logo 方案，先调整列表图片数据策略和数据，再由页面读取 imagePath。

Public Article Summary 新增可选 `coverResourceId`。它只是可消费数据；当前栏目列表是否展示封面仍按现网页面设计，Column coverPolicy 不参与 DOM 模板分支。

页面显示模式属于前端工程设计，不回写成 CMS 可配置展示模式。

## 5. 工程资产

NCSS 区域继续使用 `/static/home/ncss-logo.png` 和固定目标 URL。其变更视为工程需求变更。

首页布局、区域顺序、Header/Footer、页面 Shell 保持代码实现；CMS 只提供实际需要运营维护的数据。

`/static/icons/**` 可以保存版本化图标文件，但导航条目与图标的对应关系来自 Navigation `iconPath`。

## 6. 回归

保持 `/column/**`、`/article/**`、`/page/**`、SEO、响应式和现网视觉 E2E。新增/保持以下闭环：

- 首页由 Navigation iconPath 驱动业务图标；
- HOME_CAROUSEL 由 CmsList 驱动；
- Browser E2E 临时将 HOME_CAROUSEL_INTERVAL_SECONDS 设为 1 秒并增加第二图片项，验证 active item id 在窗口内真实变化；finally 删除临时 item 并恢复属性；
- SITE_LINKS 当前文字链接基线无回归；
- Public Article Summary 的 coverResourceId API 兼容现有模板。