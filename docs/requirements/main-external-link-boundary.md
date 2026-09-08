# Main External-link Ownership & Behavior Requirement

## Status

- Parent Planning Authority: GitHub Issue #60 / E1
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Planning baseline: `main@f42bacf4ab7719e3291288c77f0685b428b86141`
- Requirement: **READY**
- Current Ready Execution Unit: **NONE**

## 1. Intent

E1 的目标是统一解释 Main Site 多种“点击后进入外部目标”的业务载体各自负责什么，而不是把 Article、Navigation、CmsList、Advertisement 与固定工程集成合并成一个通用 Link 模型。

当前产品已经存在稳定外链行为。E1 默认保持现有用户可见语义，只消除 ownership / migration / route responsibility 的歧义；没有新的原站证据或 Product Requirement 时，不新增外链徽标、确认弹窗、统一中转页或 Article open-mode字段。

## 2. Ownership contract

### 2.1 EXTERNAL_LINK Article

- Article owns：title、source / publish metadata、externalUrl、column membership、publish lifecycle；
- Public column/home placement直接使用 Article的外部 URL，不为该 Article制造站内正文副本；
- Article详情 canonical route只承担已有兼容行为，不成为外部内容的新 Authority；
- Historical EXTERNAL_LINK Article 的 provenance / legacy identity / fingerprint属于 E3 Canonical Migration Dataset。

### 2.2 Navigation LINK

- Navigation owns：navigation label、stable navigation identity、target URL、open mode、enabled state与placement；
- Site Package只承载被站点稳定导航结构真正依赖的 NavigationItem；
- Navigation LINK不是 Article，也不为已有 Article复制标题/URL Authority。

### 2.3 CmsList LINK / ARTICLE

- LINK ListItem owns presentation title、URL、open mode、optional image与list placement；
- ARTICLE ListItem只承担placement / optional presentation override；canonical target继续来自关联 Article；
- ARTICLE关联到 EXTERNAL_LINK Article 时，公开目标仍是 Article externalUrl；
- historical list membership 的 provenance / stable legacy identity / fingerprint属于 E3 migration，而不是 Site Package list definition。

### 2.4 Advertisement

- Advertisement owns campaign/display title、image、URL、open mode、enabled/order；
- AdvertisementSlot仅承担稳定展示容器；
- 当前 E1 不把 Advertisement改造成 Article/ListItem。

### 2.5 Fixed external integration

真正无需运营维护、与工程页面直接集成的第三方 seam（例如 accepted NCSS固定入口）可以继续由 Public Renderer / Site工程资产持有。

一旦该目标需要普通运营人员维护、排序、启停或替换，应通过 Requirement Change进入合适CMS对象；E1不预先把所有固定URL配置化。

## 3. Opening behavior

E1保持 current accepted behavior：

- EXTERNAL_LINK Article在Main内容列表/首页聚合中直接打开外部源，当前以新窗口行为为基线；
- Navigation / CmsList / Advertisement继续使用各自现有 open-mode contract；
- `DEFAULT` 的当前公共投影语义保持，不在E1重新定义为另一套跨对象全局策略；
- same-site internal target继续使用 canonical Main route；cross-entry `/party/**`保持既有 document navigation边界。

E1不新增 Article-level openMode，也不要求所有外部对象使用完全相同的数据字段。

## 4. External indication / safety

- 当前没有 accepted Requirement要求在每个外链标题旁增加视觉“外链”徽标；E1不新增该用户可见行为；
- 新窗口外链继续使用当前 `noopener noreferrer` safety；
- URL validity继续由各领域现有validation承担；E1不增加外部站点在线探测、定时失效检测或自动禁用；
- disabled / unpublished对象继续按各领域current public filtering规则退出公开投影。

如果未来要求统一视觉标识、离站确认或在线健康检查，应作为独立Product Requirement评估。

## 5. Migration ownership

Main历史外部内容按真实来源分类：

- legacy内容条目本质为栏目内容 → EXTERNAL_LINK Article canonical unit；
- legacy友情链接/网站导航等运营列表成员 → Generic ListItem canonical unit；
- stable navigation结构本身 → Site Package Navigation；
- one-time Fresh Site默认项 → Site bootstrap；
- fixed third-party seam → engineering asset；
- Advertisement只有在source evidence证明存在历史运营campaign需要迁移时进入对应后续migration scope。

不得因为目标 URL 相同就把不同业务载体机械去重为同一对象；是否重复由业务placement / ownership决定。

## 6. Verification requirements

E1 Planning closure至少证明：

1. Public column/home对EXTERNAL_LINK Article使用externalUrl；
2. Navigation external/cross-entry与internal route职责分离；
3. CmsList LINK / ARTICLE target responsibility与current spec一致；
4. Advertisement / fixed integration未被错误提升为Article；
5. Site Package、bootstrap、Canonical Migration三类lifecycle不因外链而混同；
6. no new DB schema/API/Admin/Public visual behavior is required by E1 itself。

## 7. Non-goals

- 不采集Main历史内容；
- 不修改任何外链URL；
- 不增加外链徽标/确认页；
- 不增加Article openMode；
- 不建立统一Link domain object；
- 不修改Party behavior；
- 不进入E2/E3 implementation。

## 8. Requirement readiness

Current Repository已同时暴露Article、Navigation、CmsList、Advertisement和fixed integration的稳定model与Public consumer behavior，足以冻结ownership与migration classification；没有未决Product Intent。

本 Requirement **READY**。对应 Specification可以直接以current behavior形成；若 Specification audit仍无implementation gap，E1应以Planning/Authority closure完成，不创建Execution Unit。
