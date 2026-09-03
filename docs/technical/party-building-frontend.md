# 中心党建前端技术方案（Technical Plan）

本文描述党员之家（**Party Members’ Home**）前端正式实现方案。`party-building` 为 EU-27 已落地的兼容性技术标识，不再作为“党员之家”的正式英文翻译；新的英文逻辑命名应使用 `party-members-home`。对外 canonical URL 继续使用 `/party/`。

本方案遵循 ADR-0002 的 Multi-entry Site Boundary，并按 ADR-0003 / `docs/specifications/public-shared-shell.md` 将 Navigation 与 Footer 收敛为跨 Site Shared Shell Components。

## 1. 工程边界

党员之家继续位于同一 `frontend/public-site` Vite/Vue 工程，通过独立 `party.html` Entry、App、Router 和内容主题实现。

Site-owned：

- Party Banner；
- Party Router；
- Party Page Frame；
- Home / Column / Article 模板；
- 红色内容主题。

Shared：

- API client / CMS DTO；
- SiteConfig / Navigation 数据；
- `shared/components/PublicNavigation.vue`；
- `shared/components/PublicFooter.vue`；
- `shared/styles/public-shell.css`。

Main 与 Party 不再分别维护 Navigation / Footer DOM 与响应式算法。

## 2. 数据边界

- 栏目：继续使用 `party-building` 兼容性父栏目及 `party-voice / party-work / party-rules / party-study` 四个子栏目；
- 文章：通用 Article，栏目作用域服务端分页；
- 首页轮播：通用 `PARTY_HOME_CAROUSEL` CmsList；
- SiteConfig / Navigation：复用通用公开 API；
- 历史内容：EU-29 独立迁移，不进入 Flyway。

## 3. 路由

- `/party/`
- `/party/column/:alias`
- `/party/article/:id`

Router / Article View 必须确认目标栏目属于上述党员之家子栏目集合；不得通过党员之家 URL 展示主站文章。

Shared Navigation 通过 `siteRoot=/party` 确认 `/party/**` 属于当前 Entry；Main Entry 将 `/party/**` 视为跨 Entry document navigation。菜单树、active、placeholder、external/newWindow、Desktop/Mobile 行为均由同一个 Shared Component 实现。

## 4. Shared Navigation / Footer

### 4.1 Navigation

Main / Party 使用同一 `PublicNavigation.vue`：

- 同一一级/二级 DOM；
- 同一字体、间距、宽度分配和 60px Desktop 高度；
- 同一 Desktop hover/focus 与 Mobile 展开规则；
- 同一 active 计算；
- Party 不再保留 120px 固定导航项等独立结构规则。

主题仅通过 Shared CSS variables 切换：

- Main：蓝色；
- Party：`#D00023` 主导航、`#AD001D` active/hover。

### 4.2 Footer

Main / Party 使用同一 `PublicFooter.vue`：

- 左侧地址、公交、电话、办公时间、版权、公安备案、ICP；
- 右侧事业单位图标、微信公众号二维码；
- Desktop / Mobile 布局完全一致；
- 稳定资源完全一致。

主题仅通过 CSS variables 切换 Main 蓝色 / Party `#AD001D` 红色。

## 5. Party Site-owned Visual

稳定 Banner 使用版本化 3072×512 资源覆盖 Desktop 展示，不允许先降至低分辨率再放大。窄屏按响应式规则缩放，不复制原站固定 1200px 横向溢出。

首页 Desktop：

- 585×329 轮播；
- 585×329 高层声音；
- 工作动态单列；
- 学习园地两栏（党规党章、理论学习）。

高层声音使用独立密度覆盖，不继承通用列表额外最小行高。

`PARTY_HOME_CAROUSEL` 使用通用 CmsList URL 字段。当前已确认：轮播项对应本站新闻时，URL 指向该新闻 `/party/article/{id}`，点击进入对应新闻详情；更完整的轮播数据关系、编辑方式和其他跳转类型留待后续专项讨论。

## 6. 验证

- Main / Party 都存在 `data-component="public-navigation"`；
- Main / Party 都存在 `data-component="public-footer"`；
- 两边 Navigation DOM/层级/字体/响应式一致，只允许 theme color 不同；
- 两边 Footer 内容/布局/响应式一致，只允许 theme color 不同；
- Party Banner 媒体源尺寸固定为 3072×512；
- 高层声音列表密度；
- 轮播项实际点击进入对应 `/party/article/{id}`；
- 390px 无横向溢出；
- Main / Admin 无功能回归；
- Current Screenshot + AI Visual + Human Review 共同完成视觉收敛。
