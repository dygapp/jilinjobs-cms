# 中心党建前端技术方案（Technical Plan）

本文描述党员之家（**Party Members’ Home**）前端正式实现方案。`party-building` 为 EU-27 已落地的兼容性技术标识，不再作为“党员之家”的正式英文翻译；新的英文逻辑命名应使用 `party-members-home`。对外 canonical URL 继续使用 `/party/`。

## 1. 工程边界

党员之家继续位于同一 `frontend/public-site` Vite/Vue 工程，通过独立 `party.html` Entry、App、Router、Shell 和样式域实现。共享能力限定为 API client、通用 CMS 数据模型、SiteConfig、Navigation 等数据能力；不得直接复用 Main Header/Footer DOM 或主站主题 CSS。

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

## 4. Shell 与视觉

党员之家拥有独立 Header/Footer/Page Frame 和红色主题。Header 使用原站证据确认的 Banner 与主导航数据；Footer 与主站保持机构地址、电话、办公时间、版权、ICP备案和公安备案的同一信息口径，并复用稳定的公安备案图标资源，但继续使用党员之家自有红色 DOM/CSS，不导入 Main Footer 组件。

稳定 Banner 使用版本化 3072×512 资源覆盖 320px Desktop 展示，不允许先降至低分辨率再放大。窄屏按响应式规则缩放，不复制原站固定 1200px 横向溢出。

## 5. 首页

Desktop 维持 1200px 内容宽度：

- 585×329 轮播；
- 585×329 高层声音；
- 工作动态单列；
- 学习园地两栏（党规党章、理论学习）。

高层声音使用独立密度覆盖，不继承通用列表 36px 最小行高造成额外空隙。轮播项存在 URL 时整项可点击，并遵循 CmsList `openMode`；无 URL 时仅展示。

## 6. 验证

- 静态资源可访问；
- Banner 媒体源尺寸固定验证为 3072×512；
- Desktop 主结构尺寸/红色视觉基线；
- 高层声音列表密度；
- 轮播 URL 实际点击进入目标页面；
- 公安备案图标可见；
- 390px 无横向溢出；
- Main / Admin 无主题和功能回归；
- Reference Evidence + Current Screenshot + AI Visual + Human Review 共同完成视觉收敛。
