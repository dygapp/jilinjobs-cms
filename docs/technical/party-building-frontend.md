# 中心党建正式前端技术计划（Technical Plan）

## 1. 目标

在既有 Multi-entry Modular SPA 架构内，把 Party Building Site 从 Foundation Shell 扩展为可承载真实栏目、文章和正式首页的独立公开站点。

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
│   └── partyContext.ts          # 如实现证明需要，保存有界 Party 栏目结构快照
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

Party `App.vue` 统一持有 Header / Router View / Footer，避免未来每个 Party 页面重复装配 Shell。页面级模块只负责自身内容。

## 3. Party CMS Scope

### 3.1 结构基线

新 Flyway migration（V14 或当前最新版本之后的下一个版本）只建立：

```text
party-building
├── party-voice
├── party-work
├── party-rules
└── party-study
```

四个子栏目显示名分别为“高层声音、工作动态、党规党章、理论学习”，与 2026-09-02 原站直接证据一致。

全部标记为 preset。子栏目 `coverPolicy` 默认 `OPTIONAL`，避免把原站并未证明的统一封面要求硬编码为发布门槛。

不得修改已执行的 V4 / V12 / V13。

### 3.2 Party Scope 解析

Backend 无需新增 `site` 字段。Party 前端通过稳定父栏目 `party-building` 与四个子栏目识别党建作用域。

实现优先选择一次读取四个已知 alias 的 PublicColumn，并形成 Party Site 生命周期内的有界结构 Context；不得为每个列表行重复查询栏目。

若现有 PublicColumn API 无法一次返回树，可在当前只有 5 个预置节点的规模下使用有界并行查询；只有真实性能证据出现时再新增批量结构 API。

### 3.3 Article 查询

复用：

- `GET /api/public/articles?columnId=...&page=...&size=...`
- `GET /api/public/articles/{id}`

Party 首页对四个栏目分别请求有限条摘要。Party 栏目页按当前栏目分页请求。不要请求全站文章后前端筛选。

Party 文章详情在读取详情后校验 `columnAlias` 属于四个党建 alias；否则显示不可用状态，不渲染主站文章。

## 4. Router

目标路由：

```text
/party/                       -> PartyBuildingHomeView
/party/column/:alias          -> PartyBuildingColumnView
/party/article/:id            -> PartyBuildingArticleView
/party/:pathMatch(.*)*        -> Party 站内 Not Found / 首页（按实现选择，但不得跳到 Main Entry）
```

所有页面使用 route-level lazy import。

对 `EXTERNAL_LINK`：

- 首页/栏目列表直接使用原文 URL；
- 不构造 `/party/article/{id}`；
- 默认遵循 Article 既有外链打开语义。

## 5. Shell 生命周期

当前 Foundation 首页直接渲染 Header/Footer。正式阶段改为 Party App 统一承载 Shell：

```vue
<PartyBuildingHeader />
<router-view />
<PartyBuildingFooter />
```

这样在 `/party/ -> /party/column/** -> /party/article/**` 的同一 SPA 生命周期中不重复销毁/重建 Shell。

Party Header/Footer 可以读取稳定站点联系信息；若复用 Main SiteConfig API，只读取有界站点属性快照，不复制 Main DOM/CSS。

## 6. 首页数据装配

首页四条内容线：

- `party-voice`：有限条摘要；
- `party-work`：有限条摘要；
- `party-rules`：有限条摘要；
- `party-study`：有限条摘要。

“学习园地”只在模板层组合 `party-rules + party-study`。

顶部重点内容区先以 Article 的 pinned / recommended / sortOrder 和可选 coverResourceId 能力实现最小可证明方案；如果进一步视觉证据证明存在独立人工编排集合，再评估 CmsList。不得为了 Foundation 视觉直接发明新的 Party carousel 模型。

首页展示条数使用 Party 页面私有命名常量，例如 `PARTY_HOME_SECTION_SIZE`；不作为 SiteProperty，除非未来有真实运营调节需求。

## 7. 内容迁移

### 7.1 结构与运营内容分离

Flyway：只建立栏目结构。

历史内容迁移：单独脚本/数据文件/执行流程，至少保留：

- legacy content_id；
- legacy typeCode；
- legacy detail path（至少覆盖已观察到的 `pdetail.html` 与更早 `detail.html` 变体）；
- 标题；
- 来源；
- 日期；
- INTERNAL / EXTERNAL_LINK 判断；
- 原文 URL；
- 正文 HTML / 图片（可可靠取得时）。

迁移工具必须可重复运行或具有幂等键，避免重复导入。

### 7.2 Legacy URL 映射

如果当前仓库已有统一历史 URL 映射机制，则复用；若尚无机制，迁移单元先保存 legacy id/typeCode/detail path 与新 Article ID 的映射证据，不在页面组件中硬编码大量旧 ID。

## 8. 视觉与资源

Party Foundation CSS 可以被替换/重构，但不能影响 Main CSS。

稳定 Party 视觉资源进入：

```text
site-baseline/static/party-building/**
```

只有经过真实内容类型验证的原站资源可以版本化；不使用搜索结果中的无关党建图片作为替代。

视觉收敛顺序：

1. 原站 DOM/文本/URL/可取得资源；
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

- `/party/` 四条内容线与“学习园地”分组；
- `/party/column/:alias` 分页；
- INTERNAL 进入 Party 详情；
- EXTERNAL_LINK 直接跳转；
- 非党建文章不能由 Party 详情正常渲染；
- Party Shell 在站内路由切换时保持单实例；
- Main `/`、`/column/**`、`/article/**`、`/page/**` 回归；
- `/admin/` 与通用栏目/文章管理回归。

### 9.3 Visual / Human

正式阶段必须运行 Review Environment。AI Visual 与 Human Review 至少覆盖：

- Party 首页；
- 代表性 Party 栏目列表；
- 代表性 Party 站内文章详情；
- PC + 移动端；
- Main Site 主题隔离检查。
