# 公开站公共 Shell 规格说明

## 1. 目标

定义 Main Site 与 Party Members’ Home 必须共享的公开站公共 Shell。本文与 ADR-0003 配套，并对 `docs/specifications/party-building.md` 中此前“Navigation / Footer 必须 Party-owned DOM/CSS”的描述形成后续覆盖。

党员之家仍保留独立红色主题、Banner、页面 Frame 和内容模板；但它作为主站“中心党建”入口，不维护第二套公共 Navigation / Footer 产品规则。

## 2. Shared Navigation

Main Site 与 Party Members’ Home 使用同一公共 Navigation Component。

必须保持一致：

- 数据来源：`/api/public/navigations`；
- 一级 / 二级菜单；
- sortOrder / id 排序；
- clickable / placeholder；
- external / newWindow；
- active 与父级联动；
- Desktop hover / focus；
- Mobile 展开与二级层级；
- 字体族、字号、字重；
- 导航项宽度分配、间距、高度；
- 响应式断点与布局。

允许的 Site 差异仅为主题颜色，以及各 Entry 的内部路由作用域：

- Main：蓝色主题，`/party/**` 视为跨 Entry；
- Party：红色主题，`/party/**` 视为当前 Entry 内部路由。

## 3. Shared Footer

Main Site 与 Party Members’ Home 使用同一公共 Footer Component。

两者必须展示同一信息结构：

1. 办公地址；
2. 公交线路；
3. 咨询电话；
4. 办公时间；
5. Copyright；
6. 公安备案；
7. ICP；
8. 事业单位图标；
9. 微信公众号二维码。

Desktop 统一采用左侧站点信息 + 右侧官方标识；Mobile 统一采用同一上下重排和备案换行规则。

允许差异仅为主题颜色：

- Main：蓝色 Footer；
- Party：红色 Footer。

## 4. Site-owned Shell

以下仍由各 Site Entry 独立实现：

- Main 顶部平台条和 Main Banner；
- Party Banner；
- Page Frame；
- 首页与内容模板；
- 非公共主题视觉。

## 5. Acceptance Criteria

- `src/shared/components/PublicNavigation.vue` 被 Main / Party 同时复用；
- `src/shared/components/PublicFooter.vue` 被 Main / Party 同时复用；
- 不存在两套 Navigation 菜单树 / active / responsive 实现；
- 不存在两套 Footer DOM / responsive 实现；
- 两个 Site 只通过 theme class / CSS variables 切换蓝色与红色；
- Browser Test 同时断言 Main / Party 使用相同 shared component marker；
- 主站既有导航、Footer 能力无回归；
- Party 的红色主题、Banner 和内容布局无回归。
