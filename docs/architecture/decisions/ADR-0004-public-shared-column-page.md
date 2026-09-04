# ADR-0004：Main / Party 二级栏目列表采用 Shared Column Page

- Status: Accepted
- Date: 2026-09-04
- Scope: `frontend/public-site`
- Evidence: EU-29 Human Review
- Supersedes: ADR-0003 §2.3 中“栏目模板主题视觉一律属于 Site Boundary”的默认判断，仅限二级栏目列表页；文章详情、首页内容区块和 Site-owned Frame/Branding 仍按 ADR-0003 保持隔离。

## 1. Context

EU-29 使用真实党建历史 Snapshot 进入人工评审后发现：原网站中心党建二级栏目列表并没有采用一套独立的红色栏目模板，而是与主站栏目列表保持相同的页面结构、列表行、图标、标题/日期和分页视觉。

当前实现中 `PublicColumnView.vue` 与 `PartyColumnView.vue` 各自维护一套列表 DOM、分页和 CSS。Party 版本使用红色标题、虚线列表和简化分页，已经形成与原站证据不一致的视觉漂移，也造成两套重复实现的能力差异。

该差异不是业务 Site Boundary，而是重复实现造成的维护漂移。

## 2. Decision

Main / Party 二级栏目列表统一复用 `src/shared/components/PublicColumnPage.vue`。

Shared Component 负责：

- 栏目页内容 Frame；
- Breadcrumb 的稳定结构和排版；
- 栏目标题；
- INTERNAL / EXTERNAL_LINK 列表 DOM；
- 原站列表图标、标题、日期布局；
- 空状态；
- 完整分页、页码、每页条数与跳页；
- Desktop / Mobile 响应式视觉；
- 初始 loading / error 的稳定展示。

Site Entry 仍负责：

- 栏目数据加载与作用域约束；
- Main / Party canonical route；
- 内部文章 URL 前缀；
- Breadcrumb 文案与目标；
- SEO metadata；
- Party 栏目 alias 白名单与非党建内容隔离。

因此共享的是**已经由原站证据证明一致的 presentation primitive**，不是合并 Main / Party Router 或 Site Entry。

## 3. Boundary

以下内容仍保持 Site-owned：

- Main Header / Banner；
- Party Banner 与红色首页主题；
- Main / Party 首页内容区块；
- Party 文章详情视觉；
- Main 文章详情视觉；
- Site-specific Router、Context 与 SEO 规则。

若后续证据证明文章详情也应统一，应单独形成新的 Authority / ADR，不通过本 ADR 推断扩展。

## 4. Consequences

正向影响：

- Main / Party 二级栏目列表不会再发生 DOM、分页能力和响应式结构漂移；
- Party 历史真实长标题、空日期和分页数据可直接使用与 Main 一致的成熟展示层；
- 后续栏目列表视觉调整只需维护一处；
- Multi-entry Site Boundary 仍保持不变。

代价：

- Shared Column Page 成为 Main / Party 的共同回归边界；
- 修改公共栏目列表必须同时验证两个 Entry；
- 不允许通过 Party-local CSS 静默重新覆盖为第二套栏目模板。

## 5. Verification

每次 Shared Column Page 修改至少验证：

1. Main `/column/**` 渲染 `data-component="public-column-page"`；
2. Party `/party/column/**` 渲染同一 Shared Component；
3. Main / Party 保持各自 canonical article URL；
4. INTERNAL / EXTERNAL_LINK 行为不变；
5. Party scoped pagination 与直接刷新仍通过；
6. 390px 无横向溢出；
7. Main / Party Browser Regression 通过。
