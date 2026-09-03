# ADR-0003：公开站 Navigation / Footer 采用跨 Site Shared Shell Components

- Status: Accepted
- Date: 2026-09-03
- Scope: `frontend/public-site`
- Supersedes: ADR-0002 §2.3 中对 Navigation Layout / Footer 一律禁止进入 Shared 的默认约束；ADR-0002 其余 Multi-entry Site Boundary 决策继续有效。

## 1. Context

Main Site 与 Party Members’ Home 当前属于同一公开站产品和同一 `frontend/public-site` 工程。党员之家是主站“中心党建”入口下采用红色主题和子网站布局的 Site Entry，并不是拥有独立站点治理规则的第二套产品。

EU-28 Human Visual Review 暴露出一个结构性问题：主站与党员之家分别实现 Navigation / Footer 后，即使两边读取同一 Navigation / SiteConfig 数据，也会持续发生能力和视觉结构漂移，例如：

- 导航字体、一级/二级菜单、active、移动端层级行为不一致；
- Footer 的内容、对齐方式、事业单位图标和微信公众号二维码需要人工重复同步；
- 主站公共区域后续修改不能自然传递到党员之家。

这些差异不是业务 Site Boundary，而是重复实现造成的维护漂移。

## 2. Decision

公开站稳定公共 Shell 中的 **Navigation 与 Footer 抽取到 `src/shared/` 统一复用**。

```text
frontend/public-site/src/shared/
├── components/
│   ├── PublicNavigation.vue
│   └── PublicFooter.vue
└── styles/
    └── public-shell.css
```

### 2.1 PublicNavigation

Main Site 与 Party Members’ Home 必须复用同一个 Navigation DOM 与交互实现，包括：

- 一级/二级菜单层级；
- 排序；
- clickable / placeholder；
- internal / external / newWindow；
- 父子 active；
- Desktop hover / focus 展开；
- Mobile 展开及二级层级；
- 字体、字号、字重、间距、宽度分配和响应式断点。

Site Entry 只提供：

- Navigation 数据；
- 当前 Site Root / Cross-entry Root；
- theme 标识；
- 必要的 test-id / aria 元数据。

颜色通过 Shared CSS theme variables / modifier class 覆盖。不得在 `sites/main/**` 或 `sites/party-building/**` 再复制一套 Navigation DOM、菜单树算法或响应式布局。

### 2.2 PublicFooter

Main Site 与 Party Members’ Home 必须复用同一个 Footer DOM 与响应式结构，包括：

- 办公地址；
- 公交线路；
- 咨询电话与办公时间；
- Copyright；
- 公安备案与 ICP；
- 事业单位图标；
- 微信公众号二维码；
- Desktop 左信息 / 右官方标识布局；
- Mobile 上下重排。

Site Entry 只提供 SiteConfig 数据与 theme 标识。颜色通过 Shared CSS theme variables / modifier class 覆盖。

### 2.3 仍属于 Site Boundary 的内容

以下内容仍不进入本次 Shared Shell：

- Main Site 顶部吉林智慧教育平台条；
- Main Site Banner；
- Party Members’ Home Banner；
- Site-specific Page Frame；
- 首页内容区块；
- 栏目 / 文章模板的主题视觉；
- Party 红色内容主题与 Main 蓝白内容主题。

因此本 ADR 不撤销 Multi-entry Site Boundary，只把已经被产品事实证明完全一致的公共 Shell 收敛为共享组件。

## 3. Theme Boundary

Shared Navigation / Footer 的**结构、排版、交互、响应式行为必须一致**。Theme 只允许覆盖视觉 token，例如：

- Navigation background；
- Navigation active / hover；
- submenu border / shadow / hover color；
- Mobile submenu background；
- Footer background / foreground。

若未来某个 Site 需要不同菜单层级、不同 Footer 信息架构或不同响应式行为，应先形成 Requirement Change；不得通过 Site-local CSS/DOM 静默分叉 Shared Component。

## 4. Consequences

正向影响：

- 主站公共区域修改可自动同步到党员之家；
- 消除 Navigation / Footer 双实现漂移；
- Browser Verification 可以直接验证两个 Entry 渲染同一 Shared Component；
- Site Boundary 更聚焦于真实的品牌 Banner、页面 Frame 和内容主题差异。

代价：

- Shared Shell 成为两个 Site Entry 的共同回归边界；
- 修改 Shared Navigation / Footer 必须同时验证 Main 与 Party；
- Theme CSS 必须保持 token 化，避免通过高特异性选择器重新制造结构分叉。

## 5. Verification

每次 Shared Shell 修改至少验证：

1. Main / Party 均渲染 `PublicNavigation` 与 `PublicFooter`；
2. 两个 Site 的 Navigation 层级、字体、布局和响应式行为一致；
3. Main 使用蓝色 theme，Party 使用红色 theme；
4. Footer 内容与布局一致；
5. Party Banner / Main Header 等 Site-owned 区域保持隔离；
6. Main / Party Desktop + Mobile Browser Regression 通过。
