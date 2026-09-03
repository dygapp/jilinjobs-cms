# 中心党建正式前端技术计划（Technical Plan）

## 1. 目标

在既有 Multi-entry Modular SPA 架构内，把 Party Building Site 从 Foundation Shell 扩展为可承载真实栏目、文章、顶部轮播和正式首页的独立公开站点。

保持：

- 同一 `frontend/public-site` package / Vite build；
- `party.html` 作为 Party Entry；
- `/party/**` Gateway fallback；
- Spring Boot 通用 CMS Backend；
- Main / Party 独立 App、Router、Shell、Theme；
- 无 Module Federation、无独立党建前端工程。

## 2. 目标源码结构

```text
src/sites/party-building/
├── app/
│   ├── App.vue
│   ├── main.ts
│   ├── router.ts
│   └── partyContext.ts
├── shell/
│   ├── PartyBuildingHeader.vue
│   └── PartyBuildingFooter.vue
├── modules/
│   ├── home/
│   │   └── PartyBuildingHomeView.vue
│   └── content/
│       ├── PartyBuildingColumnView.vue
│       └── PartyBuildingArticleView.vue
└── styles/
    └── party-building.css
```

Party `App.vue` 统一持有 Header / Router View / Footer。页面级模块只负责自身内容。

## 3. Party CMS Scope

### 3.1 结构基线

新 Flyway migration（V14 或当前最新版本之后的下一个版本）只建立稳定结构：

```text
party-building
├── party-voice
├── party-work
├── party-rules
└── party-study

PARTY_HOME_CAROUSEL (CmsList, imagePolicy=REQUIRED)
```

四个子栏目显示名分别为“高层声音、工作动态、党规党章、理论学习”。栏目与轮播容器全部标记为 preset。子栏目 `coverPolicy` 默认 `OPTIONAL`。

Flyway 不注入历史文章、轮播成员或正文资源。不得修改已执行的 V4 / V12 / V13。

### 3.2 Party Scope 解析

Backend 无需新增 `site` 字段。Party 前端通过稳定父栏目 `party-building` 与四个子栏目识别党建作用域。

实现优先一次读取四个已知 alias 的 PublicColumn，并形成 Party Site 生命周期内的有界结构 Context；不得为每个列表行重复查询栏目。当前只有 5 个稳定节点时可使用有界并行查询，不因形式统一新增批量结构 API。

### 3.3 Article 查询

复用：

- `GET /api/public/articles?columnId=...&page=...&size=...`
- `GET /api/public/articles/{id}`

Party 首页对四个栏目分别请求有限条摘要；Party 栏目页按当前栏目分页请求。禁止请求全站文章后前端筛选。

Party 文章详情读取后校验 `columnAlias` 属于四个党建 alias；否则显示不可用状态。

### 3.4 顶部轮播查询

复用现有 Public CmsList API，根据稳定 code `PARTY_HOME_CAROUSEL` 获取当前启用列表项。轮播成员使用通用标题、图片、URL、打开方式和排序字段。

不得从 Article pinned/recommended 结果推导顶部轮播，也不得新增 Party 专属 Carousel API 或数据表。

## 4. Router

```text
/party/                       -> PartyBuildingHomeView
/party/column/:alias          -> PartyBuildingColumnView
/party/article/:id            -> PartyBuildingArticleView
/party/:pathMatch(.*)*        -> Party 站内 Not Found / 首页
```

所有页面使用 route-level lazy import。

对 `EXTERNAL_LINK`：

- 首页/栏目列表直接使用原文 URL；
- 不构造 `/party/article/{id}`；
- 默认遵循 Article 既有外链打开语义。

## 5. Shell 生命周期

正式阶段由 Party App 统一承载：

```vue
<PartyBuildingHeader />
<router-view />
<PartyBuildingFooter />
```

在 `/party/ -> /party/column/** -> /party/article/**` 的同一 SPA 生命周期中不重复销毁/重建 Shell。

Party Header/Footer 可读取稳定站点联系信息；若复用 SiteConfig API，只读取有界站点属性快照，不复制 Main DOM/CSS。

## 6. 首页数据装配

首页数据来源：

- `PARTY_HOME_CAROUSEL`：顶部独立图片轮播；
- `party-voice`：有限条摘要；
- `party-work`：有限条摘要；
- `party-rules`：有限条摘要；
- `party-study`：有限条摘要。

“学习园地”只在模板层组合 `party-rules + party-study`。

原站运行时证据已经证明顶部区域是独立 4 项图片轮播，与“高层声音”并列，因此不再使用 Article pinned / recommended / sortOrder 作为轮播主数据。CmsList 容器属于稳定站点结构；成员属于运营内容。

首页展示条数和轮播布局参数使用 Party 页面/模块私有命名常量，不机械提升为 SiteProperty。

## 7. 内容迁移

### 7.1 结构与运营内容分离

Flyway：只建立栏目结构和 `PARTY_HOME_CAROUSEL` 容器。

历史内容迁移：单独脚本/数据文件/执行流程，至少保留：

- legacy content_id；
- legacy typeCode；
- legacy detail path；
- 标题；
- 来源；
- 日期；
- INTERNAL / EXTERNAL_LINK 判断；
- 原文 URL；
- 正文 HTML / 图片；
- 可迁移的历史轮播成员与图片。

迁移工具必须可重复运行或具有幂等键，避免重复导入。

### 7.2 Legacy URL 映射

如仓库已有统一历史 URL 映射机制则复用；否则迁移单元先保存 legacy id/typeCode/detail path 与新 Article ID 的映射证据，不在页面组件中硬编码大量旧 ID。

## 8. 视觉与资源

Party Foundation CSS 可以被替换/重构，但不能影响 Main CSS。

稳定 Party 视觉资源进入：

```text
site-baseline/static/party-building/**
```

只有经过真实内容类型验证的原站资源可以版本化。历史轮播图片和正文图片属于运营内容迁移，不因为页面复刻需要而批量并入工程基线。

原站 Desktop 证据使用固定 1200px 主内容；Mobile 证据仍保持 `min-width: 1200px` 并产生横向溢出。新版不得复制这一旧实现缺陷，应保留可证明的视觉层级和比例关系，同时按现行响应式 Requirement 在窄屏正常重排。

视觉收敛顺序：

1. 原站截图 / DOM / computed styles / CSS / 资源；
2. Party runtime screenshot；
3. AI Visual comparison；
4. Human Review finding；
5. 对 Finding 按 Authority 分类后修复。

## 9. Verification

### 9.1 Build / Backend

- Backend test + bootJar；
- Flyway fresh MySQL chain；
- Public Site build 同时生成 Main + Party Entry；
- Admin build。

### 9.2 Browser

至少新增：

- `/party/` 顶部轮播、四条内容线与“学习园地”分组；
- 轮播从 `PARTY_HOME_CAROUSEL` 获取，且不依赖 Article 全站窗口；
- `/party/column/:alias` scoped pagination；
- INTERNAL 进入 Party 详情；
- EXTERNAL_LINK 直接跳转；
- 非党建文章不能由 Party 详情正常渲染；
- Party Shell 在站内路由切换时保持单实例；
- Desktop / Mobile 无固定 1200px 横向溢出回归；
- Main `/`、`/column/**`、`/article/**`、`/page/**` 回归；
- `/admin/` 与通用栏目/文章/列表管理回归。

### 9.3 Visual / Human

正式阶段必须运行 Review Environment。AI Visual 与 Human Review 至少覆盖：

- Party 首页；
- 代表性 Party 栏目列表；
- 代表性 Party 站内文章详情；
- PC + 移动端；
- Main Site 主题隔离检查。
