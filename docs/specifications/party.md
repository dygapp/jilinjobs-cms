# 中心党建页面规格说明（Specification）

## 1. 目标与业务定位

本文定义主站“中心党建”特殊栏目/专题页面的 WHAT / WHY。通用 CMS 模型继续以 `docs/requirements/information-publishing.md`、`docs/specifications/cms-core.md` 为准；公开前端共享导航与页脚遵循 ADR-0003 与 `docs/specifications/public-shared-shell.md`；轮播投放与生命周期边界遵循 `docs/specifications/public-site.md` 和 `docs/technical/carousel-list-placement.md`。

**中心党建不是第二个网站，也不是主站之外的独立首页。** 它是主站信息架构下具有独立红色视觉主题的特殊栏目/专题入口。当前 `/party/**`、`party.html`、独立 App/Router 只是为了隔离红色主题、路由和页面模板的技术实现边界，不改变其业务上属于主站的定位。

原站页面名称为“党员之家”，英文业务表述为 **Party Members’ Home**。当前工程技术命名统一使用 `party / Party` 表示中心党建 Site/模块；仅 `/party/` 入口页使用 `party-home / PartyHome`，例如 `PartyHomeView.vue`。技术标识不作为“党员之家”的英文翻译。

本规格依据 2026-09-02～2026-09-03 对原站 `https://24365.jl.smartedu.cn/dyzj` 的 Browser Evidence、2026-09-03 Human Visual Review，以及 EU-30 对轮播第二项与遗漏“主题教育”历史内容的补充取证收敛。凡旧文档把中心党建描述为“独立站点”“党建首页”的业务语义，以本规格和 Human Review 结论为准。

## 2. 原站事实基线

已确认：

- 原站入口：`/dyzj`，页面标题“党员之家”；
- 首页四条主内容线：
  - 高层声音：`typeCode=gcsy`；
  - 工作动态：`typeCode=gzdt`；
  - 党规党章：`typeCode=dgdz`；
  - 理论学习：`typeCode=llxx`；
- “学习园地”只是“党规党章 + 理论学习”的页面分组，不是第五种首页内容类型；
- EU-30 对轮播第二项反向追踪确认原站另有历史栏目 `typeCode=zhutijiaoyu`，原列表标题带“主题教育2023”；新版栏目名称收敛为“主题教育”，它进入 Party 内容作用域和历史迁移，但**不新增为 PartyHome 第五个固定内容区块**；
- `zhutijiaoyu` 完整列表补采共 2 条：1 条站内文章、1 条外链，数量对账一致，`unresolved=0`；
- 四个首页主栏目列表使用 `/plist.html?typeCode=...`，且均存在多页历史内容；主题教育历史列表继续只作为迁移来源证据；
- 当前原站站内详情主要使用 `/pdetail.html?content_id=...`，历史还存在携带 `typeCode` 或 `/detail.html?content_id=...` 的变体；这些只作为迁移映射证据；
- 内容同时存在站内详情与外部权威来源直链；
- 页面顶部左侧存在独立 4 项图片轮播，与右侧“高层声音”并列；轮播不是栏目置顶文章推导结果；
- 轮播第二项实际指向 `content_id=154659859759104` 的主题教育站内文章，其轮播 PNG 与文章内容图片是不同展示资产；新版应表达为 ARTICLE 投放并保留原轮播 PNG 作为列表专用覆盖图；
- 原站 Footer 与主站使用同一机构信息口径；新版 Navigation/Footer 进一步统一为共享组件；
- 原站 CSS 使用固定 1200px 并导致窄屏横向溢出，新版不复制该限制。

### 2.1 Banner 原始资源

原站 Banner：

- URL：`https://24365.jl.smartedu.cn/webfile/theme2/img/party_banner.png`；
- 原始尺寸：3072 × 512；
- SHA-256：`7444d50235d4c87a00d0221ac84551ea083c617bb8a15e58f58d002224bd27a3`；
- 原站文件名扩展名为 `.png`，Reference Evidence 取得的原始文件实际媒体字节为 JFIF/JPEG；不得因为扩展名再次转码。

2026-09-03 Human Review 已两次证明派生 WebP/AVIF 会在标题文字边缘产生可见清晰度损失。因此正式页面必须使用**原站原始字节的版本化本地副本**：

`/static/party/party-header-banner.jpg`

该文件以正确 JPEG 扩展名保存，必须保持原始 SHA-256，不允许重新编码后冒充原始资源；正式运行不得再依赖原站 URL。

Banner 仅承担视觉展示：必须使用非链接容器 + `<img>`，不得包裹 `<a>`，不得通过点击 Banner 返回 `/party/` 或执行其他导航。

### 2.2 稳定展示资源边界

公开站设计模板使用的稳定图片、图标、二维码、字体等展示资源，必须来自：

1. 本项目版本化 `site-baseline/static/**`；或
2. 受控 CMS 静态资源路径 / Resource。

除开源 JS/CSS 依赖外，模板不得通过 `img/src`、媒体 `src/poster`、CSS `url(http...)`、资源型常量等方式直接依赖第三方静态资源 URL。业务 `<a href>` 外链、文章外链和外部平台入口不属于此限制。

## 3. CMS 复用规格

### 3.1 栏目结构

继续使用通用 Column：

```text
中心党建 (party)
├── 高层声音 (party-voice)              <- legacy gcsy
├── 工作动态 (party-work)               <- legacy gzdt
├── 党规党章 (party-rules)              <- legacy dgdz
├── 理论学习 (party-study)              <- legacy llxx
└── 主题教育 (party-theme-education)     <- legacy zhutijiaoyu
```

父栏目用于 CMS 组织和中心党建作用域识别；“学习园地”只属于页面布局。`party-theme-education` 由 EU-30 为遗漏历史内容与轮播文章关系补齐，属于可访问内容栏目，但不加入 `PartyHome` 四个固定内容查询区。

### 3.2 文章

上述子栏目复用通用 Article：

- `INTERNAL`：本站保存正文，使用中心党建文章详情模板；
- `EXTERNAL_LINK`：保存标题、来源、日期和外部 URL，从中心党建页面/栏目列表直接进入原文。

Article 继续只拥有一个 `columnId`。加入轮播或其他 CmsList 只建立展示投放关系，不改变栏目归属和详情面包屑。

不创建 `PartyArticle`、`PartyCategory` 等重复模型。

### 3.3 中心党建轮播

顶部图片轮播复用通用 CmsList，稳定容器 code 为 **`PARTY_CAROUSEL`**，产品名称为 **“中心党建轮播”**：

- `imagePolicy=REQUIRED`；
- 列表项支持通用 `LINK / ARTICLE` 两种来源；
- LINK 项维护自身标题、图片、URL、打开方式、排序、启停；
- ARTICLE 项通过 `articleId` 引用既有文章；文章必须已发布才进入公开轮播，撤回后自动退出；
- ARTICLE 项默认可继承文章主题图片，也允许选用正文图片或使用独立 CMS Resource 作为轮播覆盖图；覆盖图不修改 Article 主题图片；
- ARTICLE 站内目标由 Party 公开端生成 `/party/article/{id}`，不在列表中长期固化 Runtime Article ID URL；
- 轮播成员属于运营内容，不进入 Flyway；
- 不新增中心党建专属 Carousel 数据表或 Admin Module。

历史 V14 曾初始化 `PARTY_HOME_CAROUSEL / 中心党建首页轮播`。该 Migration 已执行，不修改历史文件；V15 通过原列表 ID 原地重命名为 `PARTY_CAROUSEL / 中心党建轮播`，已有列表成员关系不受影响。

EU-30 历史迁移中，position 2 必须使用稳定 `sourceSystem + legacyKey=zhutijiaoyu:content:154659859759104` 解析 Article Runtime ID，并把原轮播 PNG 作为 `imageResourceId` 覆盖图导入；不得使用文章标题猜测关系，也不得把历史 Runtime URL 继续作为新版站内目标。

### 3.4 轮播行为

Party 轮播使用与 Main 共用的无主题生命周期规则：

- `CAROUSEL_INTERVAL_SECONDS` 默认 4 秒，Main / Party 共用；
- `CAROUSEL_MAX_ITEMS` 默认 5；
- 0 项空态、1 项静态、多项循环自动播放；
- 提供手动页码；hover、focus、页面隐藏暂停，恢复不重置当前项；
- `prefers-reduced-motion: reduce` 关闭自动播放与 opacity transition，但保留手动切换；
- 图片加载失败项退出有效集合并补位；
- EU-30 不实现 swipe，不引入第三方 Carousel。

Party 视觉层仍由 Party 自己持有：Desktop 约 585×329；响应式使用 `585:329` 比例；图片 `object-fit: cover`；不要求与 Main 使用同一比例或 DOM。

### 3.5 管理端

继续使用“栏目管理 + 文章管理 + 列表管理”。没有独立审核流、权限或专属字段的证据，不新增党建专属后台模块。

列表管理对 ARTICLE 投放提供文章选择、图片继承/正文候选/覆盖图上传；这些能力属于通用 CmsListItem，不是 Party 专属表单。

## 4. URL 与技术隔离

Canonical URL：

- 中心党建入口：`/party/`；
- 栏目：`/party/column/{alias}`；
- 站内文章：`/party/article/{id}`。

这里的“入口”不是独立网站首页。`party.html` / Party Router 用于主题与模板隔离；产品信息架构仍把“中心党建”视为主导航中的特殊栏目入口。路由名称使用 `party-home / party-column / party-article`，其中 `party-home` 仅表示 `/party/` 入口页技术角色。

Router 必须验证栏目/文章属于中心党建栏目树，包括 `party-theme-education`；普通主站文章不得通过 `/party/article/{id}` 套用红色主题。

## 5. 页面规格

### 5.1 中心党建入口页（PartyHome）

`/party/` 至少包含：

1. 原站 Banner 的本地原始字节副本（纯展示、不可点击）；
2. 主站共享 Navigation，使用红色主题变量；
3. 中心党建轮播；
4. 高层声音；
5. 工作动态；
6. 学习园地：党规党章、理论学习；
7. 主站共享 Footer，使用红色主题变量。

四个固定首页内容区从 `party-voice / party-work / party-rules / party-study` 查询；轮播从 `PARTY_CAROUSEL` 获取，不从全站文章或前端静态数组拼装。`party-theme-education` 不新增首页固定栏目区，但其 INTERNAL 文章可以通过轮播或直接栏目 URL 进入 Party 详情。

### 5.2 栏目列表

`/party/column/{alias}`：

- 只接受中心党建父栏目下当前已确认的五个子栏目；
- 栏目作用域分页；
- INTERNAL 进入 `/party/article/{id}`；
- EXTERNAL_LINK 直接打开原文；
- 保持中心党建红色内容 Frame；
- 支持直接访问和刷新。

### 5.3 文章详情

`/party/article/{id}`：

- 仅允许已发布且属于中心党建栏目树的站内文章；
- 显示栏目上下文、标题、来源、发布日期、正文、图片和附件；
- 继续复用通用公开资源安全和浏览量规则。

## 6. Shared Shell 与视觉规格

Navigation 与 Footer 不是 Party-owned DOM：

- Main / Party 共同复用 `shared/components/PublicNavigation.vue`；
- Main / Party 共同复用 `shared/components/PublicFooter.vue`；
- DOM、菜单层级、字体、响应式与 Footer 信息结构保持一致；
- Party 只通过 CSS variables/modifier 使用红色主题。

Party-owned 视觉包括 Banner、内容 Frame、轮播 DOM/比例/主题样式和内容区块模板；轮播状态与 timer 生命周期属于 Shared 无主题能力。

Desktop 关键关系：

- Banner 使用版本化 `party-header-banner.jpg` 原始字节副本，按容器裁切展示，不二次有损转码；
- 主导航一级菜单使用原站 16px bold；二级菜单与一级菜单同主题底色、白色粗体文字，hover/active 使用主题深色；
- 中心党建轮播与高层声音约 585×329 并列；
- 工作动态单列；
- 学习园地两栏；
- Footer 与 Main 结构一致，仅颜色不同。

Mobile 必须正常响应式，无固定 1200px 横向溢出；轮播保持约 `585:329` 比例。

## 7. 数据与历史迁移

Flyway 只负责稳定结构：父栏目、当前五个子栏目、轮播容器及必要的稳定结构迁移；历史文章、正文图片、附件和轮播成员由独立迁移机制处理。

EU-29 已接受 Canonical 基线继续保持其来源与 Human Review 语义：181 篇文章、4 个轮播项及对应资源不因 EU-30 被改写为新的“原始 EU-29 证据”。

EU-30 补采的 `zhutijiaoyu` 2 条记录作为增量候选写入同一版本化 Canonical Workspace，并保留 `sourceWorkflowRunId / sourceHeadSha / stable legacyKey / fingerprint / resource SHA-256`。在 EU-30 Human Review 完成前，Manifest 必须明确标记该层为 `candidate-extension / pending-human-review`，同时保留 EU-29 `acceptedSnapshot` 原值。

历史迁移必须保留可识别的 `content_id / typeCode / detail path` 证据并落入通用 Column / Article / CmsList / Resource 模型。长期 Runtime 关联使用数据库 ID；迁移解析使用稳定 `sourceSystem + legacyKey`，不得依赖过期 workflow artifact、标题匹配或旧 Runtime ID。

已执行 V13/V14 的 Migration 文件名和历史 SQL 中可保留 `party_building / party-building` 作为不可改写历史；V16 将当前父栏目 alias 原地收敛为 `party`。当前源码、目录、测试和现行 Authority 不再使用 `PartyBuilding / party-building` 作为技术命名。

## 8. Acceptance Criteria

- 中心党建在业务上明确属于主站特殊栏目/专题页面，不再称为独立网站或党建首页；
- `/party/**` 独立 Entry/Router 仅作为主题与模板隔离技术实现；
- 当前技术命名以 `party / Party` 为通用标识，入口页使用 `party-home / PartyHome`；
- 四个固定首页内容子栏目及其 legacy 映射明确；
- `party-theme-education / 主题教育` 作为第五个 Party 内容栏目存在，但不新增 PartyHome 固定区块；
- 主题教育历史补采数量为 2、`unresolved=0`，在 Human Review 前保持 candidate-extension 状态；
- CmsList 稳定 code 为 `PARTY_CAROUSEL`，名称为“中心党建轮播”，旧 `PARTY_HOME_CAROUSEL` 不再作为当前运行时 code；
- `PARTY_CAROUSEL` 支持 LINK / ARTICLE；ARTICLE 投放不改变文章单一栏目归属；
- 历史轮播 position 2 解析到主题教育文章并保留原轮播 PNG 作为列表覆盖 Resource；
- ARTICLE 撤回后自动退出公开轮播，重新发布后可按既有投放关系恢复；
- Main / Party 共用 `CAROUSEL_INTERVAL_SECONDS` 和 `CAROUSEL_MAX_ITEMS`，不存在 Party 专属硬编码 5 秒行为；
- hover/focus/页面隐藏暂停后恢复不重置，reduced-motion 关闭自动播放和动画，手动页码仍有效；
- Banner 使用版本化 `/static/party/party-header-banner.jpg`，其字节 SHA-256 与原站一致；正式运行不访问原站 Banner URL；
- Banner DOM 不含 `<a>`，不可点击；
- 公开站设计模板不存在未经允许的外部静态资源直接引用；
- Main / Party Navigation 与 Footer 使用同一 Shared Components，仅主题色不同；
- 主导航一级/二级菜单视觉符合原站主题规则：16px bold、主题底色、白字、深色 hover/active；
- Main / Party Entry 的 favicon 均从版本化 `/static/brand/site-favicon.png` 正常加载；
- 中心党建入口页、五个允许栏目路由、文章功能和响应式通过 Browser Verification；
- Canonical Fresh DB import、二次幂等、Runtime articleRef/resource 关联验证通过；
- AI Visual Review 无未处理的 Authority-backed 高优先级差异；
- EU-30 Human Review 通过后，方可把增量迁移候选从 pending-human-review 收敛为接受状态并结束 EU-30。
