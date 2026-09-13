# 主站外链归属与行为需求

## 状态

- 语义角色：**CURRENT / DURABLE REQUIREMENT**；
- 长期规划来源：GitHub Issue #60；
- 当前执行生命周期：不由本文维护，统一读取 `docs/work/current/README.md`；
- 本需求不授予新的 Planning、Readiness 或 Execute Authority。

## 1. 目标

主站中存在多种“点击后进入外部目标”的业务载体。本文只冻结它们各自的业务归属、公开行为和迁移责任，不把 Article、Navigation、CmsList、Advertisement 与固定工程集成合并为一个通用 Link 模型。

当前已经存在稳定外链行为。除非后续出现新的 Product Requirement，否则保持现有用户可见语义，不新增外链徽标、离站确认、统一中转页或 Article 级 `openMode`。

## 2. 归属契约

### 2.1 EXTERNAL_LINK Article

Article 持有标题、来源/发布日期元数据、`externalUrl`、栏目归属和发布生命周期。公开栏目或首页投放直接使用 Article 的外部 URL，不为外部正文制造站内副本。历史 EXTERNAL_LINK Article 的 provenance、legacy identity 与 fingerprint 属于 Historical Content Migration。

### 2.2 Navigation LINK

Navigation 持有导航文案、稳定导航身份、目标 URL、打开方式、启停状态与 placement。只有站点稳定导航结构需要的 NavigationItem 才属于 Site Package。Navigation LINK 不是 Article，也不得复制已有 Article 的标题/URL Authority。

### 2.3 CmsList LINK / ARTICLE

- LINK ListItem 持有展示标题、URL、打开方式、可选图片与列表 placement；
- ARTICLE ListItem 只承担 placement 和可选展示覆盖，canonical target 继续来自关联 Article；
- ARTICLE 关联 EXTERNAL_LINK Article 时，公开目标仍是 Article 的 `externalUrl`；
- 历史列表 membership 的 provenance、stable legacy identity 与 fingerprint 属于 Historical Content Migration，不属于 Site Package 的 List definition。

### 2.4 Advertisement

Advertisement 持有活动/展示标题、图片、URL、打开方式、启停与排序；AdvertisementSlot 只承担稳定展示容器。不得因为目标是外部 URL 就把 Advertisement 改造成 Article 或 ListItem。

### 2.5 固定外部集成

无需运营维护、与工程页面直接集成的固定第三方 seam 可以继续由 Public Renderer / Site 工程资产持有。一旦该目标需要普通运营人员维护、排序、启停或替换，应通过新的 Requirement Change 进入合适 CMS 对象，不预先把所有固定 URL 配置化。

## 3. 打开行为

保持当前已接受行为：

- EXTERNAL_LINK Article 在主站内容列表/首页聚合中直接打开外部源，当前以新窗口行为为基线；
- Navigation / CmsList / Advertisement 使用各自既有 open-mode contract；
- `DEFAULT` 保持当前公共投影语义，不重新定义跨对象全局策略；
- same-site internal target 使用 canonical Main route；
- cross-entry `/party/**` 保持既有 document navigation 边界。

## 4. 外链提示与安全

- 当前没有 Requirement 要求所有外链增加视觉徽标或离站确认，本需求不新增该行为；
- 新窗口外链继续使用 `noopener noreferrer`；
- URL validity 由各领域既有 validation 负责；
- 不增加外部站点在线探测、定时失效检测或自动禁用；
- disabled / unpublished 对象继续按各领域公开过滤规则退出公开投影。

## 5. 迁移归属

主站历史外部内容按真实来源角色分类：

1. legacy 栏目内容记录 → EXTERNAL_LINK Article canonical unit；
2. legacy 友情链接/网站导航等运营列表成员 → Generic ListItem canonical unit；
3. stable navigation structure → Site Package Navigation；
4. Fresh Site 一次性默认项 → Site Package bootstrap；
5. fixed third-party seam → engineering asset；
6. Advertisement 只有 source evidence 证明存在历史运营 campaign 需要迁移时才进入对应迁移范围。

不得仅因 URL 相同就把不同业务载体机械去重为同一对象。

## 6. 当前验收边界

持续满足以下条件即可视为本 Requirement 未发生漂移：

- Public column/home 对 EXTERNAL_LINK Article 使用 `externalUrl`；
- Navigation external/cross-entry 与 internal route 职责分离；
- CmsList LINK / ARTICLE target responsibility 与当前 Specification 一致；
- Advertisement / fixed integration 未被错误提升为 Article；
- Site Package、bootstrap、Historical Migration 三类 lifecycle 不因外链而混同；
- 本需求本身不要求新增 DB schema、API、Admin form 或 Public visual behavior。

## 7. 非目标

本文不负责历史内容采集、不修改任何外链 URL、不增加外链徽标/确认页、不增加 Article `openMode`、不建立统一 Link domain object，也不改变 Party behavior。
