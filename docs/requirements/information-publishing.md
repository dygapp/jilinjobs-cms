---
id: requirement-information-publishing
title: 信息发布与网站服务需求
type: business-requirement
status: confirmed
version: "V4.9"
classification:
  - l1-06
  - l2-28
  - l2-29
relations:
  upstream:
    - docs/project/project.md
    - docs/requirements/overview/system-module-boundaries.md
  related:
    - docs/specifications/party.md
created_at: 2026-07-27
updated_at: 2026-09-07
---

# 信息发布与网站服务需求

## 1. 文档目的

本文是 `jilinjobs-cms` 当前“信息发布与网站服务”单一 canonical 业务需求基线，用于约束中心主站、中心党建公开站、CMS 通用模型、管理端、Site Package、历史迁移与当前产品行为。

V4.9 将 EU-30 已确认并完成 Human Review / implementation / migration promotion 的 Requirement Change 折回主需求，并同步 EU-41 / EU-42 已接受的 Generic Flyway、JilinJobs Site Package 与 stable Site asset ownership。`docs/requirements/information-publishing-eu30-amendment.md` 自 V4.9 起只保留 `SUPERSEDED / TRACEABILITY` 角色，不再需要与本文并行拼接才能恢复 Current Product Authority。

当前网站参照基线为“吉林省高等学校毕业生就业信息网”。`www.jilinjobs.cn` 因上级统一规划跳转到 `24365.jl.smartedu.cn`；在需求分析、页面结构分析、视觉取证和内容采集过程中，两者视为同一原网站。其他域名原则上视为外部网站或外部系统。

V4.8 在 V4.7 已完成中心党建独立 Site Entry / Router / Shell 基础框架后，依据原站 `https://24365.jl.smartedu.cn/dyzj` 的重新取证正式进入党建页面与内容收敛。原站确认“高层声音、工作动态、党规党章、理论学习”四条 PartyHome 固定内容线；“学习园地”是首页对“党规党章 + 理论学习”的视觉分组，而不是独立内容类型。EU-30 后又确认历史 `typeCode=zhutijiaoyu`，新版稳定栏目为 `party-theme-education / 主题教育`，属于 Party 内容作用域但不新增 PartyHome 第五个固定内容区。现有通用 `Column + Article(INTERNAL / EXTERNAL_LINK)` 足以承担这些数据，不新增党建专属 CMS 类型或 Admin Module。中心党建 canonical URL 统一进入 `/party/**`；历史 `plist.html / pdetail.html / detail.html` 及参数变体只作为迁移映射证据，不继续作为新版 canonical URL。

V4.7 确认公开前端按真实 Site / Theme Boundary 划分 Entry：中心主站与中心党建在同一 `frontend/public-site` Vue / Vite 工程内分别拥有独立 App、Router、Shell 和主题样式，当前继续同构建、同部署并复用 Spring Boot CMS Backend。主站普通 `/page/**` 页面不因为页面类型单独维护重复 HTML Entry。

V4.6 曾引入 `INTEGER` SiteProperty、Column/CmsList 图片策略，并以 `HOME_CAROUSEL_INTERVAL_SECONDS` 表达当时 Main-only 轮播间隔；该旧 key 已被 EU-30 / V4.9 **supersede**，仅保留为历史演进说明。Current Runtime 统一使用 `CAROUSEL_INTERVAL_SECONDS` 与 `CAROUSEL_MAX_ITEMS`。

V4.5 确认产品界面将“固定页面”统一称为“单页”，“页面组”称为“单页分组”；文章、单页、列表作为主要内容类型，栏目和导航作为内容结构，宣传展示作为运营展示，网站属性和静态资源归入站点设置。技术层 `Page / PageGroup` 及既有 API 不要求因此破坏性重命名。

V4.4 确认产品界面使用“宣传展示管理”；导航图标成为 NavigationItem 自身可选数据；通用列表不以 `LINK / IMAGE_LINK / TEXT` 组合类型控制视觉；宣传展示、通用列表、导航图标和 `RESOURCE_PATH` SiteProperty 复用统一图片选择/上传能力，CMS Runtime 上传统一进入 `/static/uploads/**`。V4.3 已确认的多图轮动、`NO_LINK` 和有效期规则继续有效。

## 2. 建设目标

中心主站目标仍为：

> **现网视觉与布局复刻 + 必要技术适配 + 可持续运营的通用 CMS 能力。**

中心党建作为同一“信息发布与网站服务”范围内具有独立视觉主题的公开 Site Boundary，在独立 Entry / Shell 基础上承担真实信息架构、栏目列表、文章详情、首页内容区块和红色主题视觉。

具体目标：

1. 以原网站页面结构、主要布局关系、菜单/栏目结构和视觉识别作为新版中心主站及中心党建公开基准；
2. CMS 后台优先提供栏目、文章、单页、单页分组、导航、通用列表、宣传展示、网站属性、静态资源等通用业务能力；
3. 公开站负责稳定页面结构、布局、视觉和稳定工程集成，通过 CMS 业务数据驱动需要运营维护的内容；
4. 公开前端按真实 Site / Theme Boundary 建立清晰源码和 Runtime Entry 边界，不把普通页面类型机械拆成独立 HTML Entry；
5. Main / Party 当前复用工程、技术栈、Backend 和发布链路，但分别持有 App、Router、Banner、内容 Frame 与主题表达，公共 Navigation/Footer 复用 Shared Shell Components；
6. 中心党建正式内容复用通用 CMS；只有出现真实专属工作流/数据模型需求时才新增党建管理能力；
7. 不为了“可配置”而把稳定、一次性、基本不需要运营调整的工程设计强行配置化；
8. 使用 Vue / Spring Boot 重建，不复制旧网站前端代码；
9. Generic CMS Schema、JilinJobs stable structure、Fresh Site one-time defaults 与 stable Site assets 分别具有清晰版本化 owner，测试数据与站点基线分离；
10. 原站历史运营内容及 provenance 通过 `data-migrations/**` 独立处理，不混入 Backend Flyway 或 stable Site assets。

## 3. 当前范围

### 3.1 In Scope

- 中心主站公开前台；
- 中心党建独立公开 Site Entry、Router、Header、Footer、页面 Frame 与正式红色主题；
- 中心党建首页真实信息架构与原站已确认内容区块；
- 中心党建 `party-voice / party-work / party-rules / party-study / party-theme-education` 内容栏目；
- PartyHome 固定顶部重点内容、高层声音、工作动态、学习园地结构；主题教育不作为第五个固定首页内容区；
- 中心党建栏目列表页、站内文章详情页，以及外链文章直接进入来源网站的行为；
- 中心党建 `/party/**` canonical URL、直接访问与刷新；
- 主导航“中心党建”作为 Party Site 入口；
- CMS 管理端；
- 栏目、文章、单页、单页分组；
- 导航位置、多级导航条目及可选导航图标；
- 通用列表与 `LINK / ARTICLE` 列表项；
- 宣传展示位与展示内容；
- 网站属性；
- 网站静态资源管理和 CMS 图片资源选择/上传；
- 固定首页模板及数据驱动内容；
- 固定公开 URL、alias 与 canonical URL；
- Generic CMS active schema baseline；
- JilinJobs stable Site structure / one-time bootstrap / stable asset package；
- 现网视觉与布局复刻；
- 响应式、浏览器兼容和基础搜索引擎友好；
- 可识别历史内容地址的迁移映射原则与 canonical migration provenance。

### 3.2 当前暂不实现

- 尚未由原站证据证明需要的党建专属后台模块、独立权限体系或专属内容类型；
- 将历史党建文章、正文图片、附件或历史运营列表成员写入 Backend Flyway / stable Site asset manifest；
- 慧就业招聘信息、直播课程的真实 iframe、第三方认证、故障重试与可用性保障；
- 用户、账号、角色、统一认证与完整权限体系；
- 基于“普通管理员 / 超级管理员”等身份差异限制新增、删除、修改的逻辑；
- 评论、点赞、收藏、投稿、在线留言和工单；
- 站内全文搜索；
- 多级审核；
- 通用可视化 Page Builder；
- 复杂流量统计与用户画像；
- 任意多站点平台能力；Main / Party 两个已确认 Site Boundary 不等于建设通用多站点 CMS；
- MQ、Redis、MinIO；
- 生产发布与正式环境拓扑。

## 4. CMS 建模原则

### 4.1 通用模型与工程资产边界

是否进入 CMS，以“是否存在持续运营维护价值”为核心判断，而不是机械消除硬编码。

优先进入 CMS 的数据包括：经常增删改的数据、数量或顺序会变化的数据、存在上下线周期的数据、需要由运营人员维护的站点属性。

优先保留为工程 / Site Package 资产的内容包括：首页及页面稳定布局、固定 Shell、固定业务组件、基本不会变化的一次性外部平台集成，以及不需要运营维护的稳定视觉资源。

例如首页“国家大学生就业服务平台”区域属于固定工程集成；其 Logo、学生/企业入口和布局不要求提供后台管理。若未来发生真实产品需求变化，再按 Requirement Change 修改工程实现。

Main / Party 的 Banner、内容 Frame、颜色变量和主题模板属于各自公开 Site 工程资产；主导航和 Footer 的公共结构与交互复用 Shared Shell。存在两个视觉主题不自动要求建立两套 CMS 模型，也不自动要求拆成独立 Repository。

### 4.2 数据与展示职责分离

CMS 负责维护“有什么业务数据”，公开站工程负责决定“这些数据在具体页面中如何展示”。不得为了让管理员控制页面视觉表现而将稳定页面设计方案反向建模成通用 CMS 展示模式。

例如 LINK 列表项可以同时具有标题、图片和 URL；某个页面可以只显示标题，另一个页面可以只显示 Logo，也可以显示 Logo + 标题。是否显示名称、图片尺寸、布局、轮播控件等由相应页面设计和前端实现确定，不需要在 CmsList 中设置 `displayMode`。

Column `coverPolicy` 与 CmsList `imagePolicy` 仅属于图片数据契约：`NONE` 表示不使用对应图片，`OPTIONAL` 表示允许但不强制，`REQUIRED` 表示公开所需内容必须形成有效图片。它们不得控制图片尺寸、布局、caption、卡片方向或其他视觉规则。

页面对特定 CMS 数据的必要字段要求属于该页面的数据消费契约。例如轮播要求有效图片，但不要求通用列表模型重新引入“轮播类型”。

### 4.3 避免重复 Authority

同一业务数据只保留一个权威来源。不得同时通过 SiteProperty JSON、导航、前端常量维护同一组可运营数据。

复杂、可排序、可增删的业务集合不应长期存储为 SiteProperty JSON；JSON 仅保留为必要扩展类型，不代替正常 CMS 对象。

Historical Migration 可以引用 stable Site identity，但不能成为 Site Definition；Public Renderer 可以消费 CMS / Site Data Contract，但不能成为产品数据 Authority。

### 4.4 内容类型与管理信息架构

CMS 主要内容类型按“文章 / 单页 / 列表”组织。栏目与导航承担内容结构，宣传展示承担页面稳定展示位中的运营内容，网站属性与静态资源属于站点设置。

管理端侧边栏按以下职责分组展示，但分组本身不增加点击层级：

- 内容管理：文章管理、单页管理、列表管理；
- 内容结构：栏目管理、导航管理；
- 运营展示：宣传展示；
- 站点设置：网站属性、静态资源。

具有明确“容器 → 成员”关系的管理页面优先采用左侧选择容器/组织上下文、右侧维护成员的方式，包括栏目 → 文章、单页分组 → 单页、列表 → 列表项、导航位置 → 导航条目、展示位 → 展示内容。容器自身就是主要管理对象的场景不要求重复增加左侧导航，例如栏目管理继续直接维护栏目树。

“站点设置”只是现有管理端信息架构分组，不新增独立“系统设置”业务模块。数据库连接、上传限制、环境地址等基础设施参数继续由工程/部署配置维护；只有确有运营维护价值的低风险站点属性进入 SiteProperty。

中心党建继续复用通用 CMS。stable Site structure 使用父栏目 `party` 组织 `party-voice / party-work / party-rules / party-study / party-theme-education` 五个子栏目；文章仍由通用文章管理维护。不同 Public Site 的主题和 URL 由前端 Site Boundary 决定，不新增 `site` 字段、多站点 CMS 或 Party Admin Module。

## 5. 内容模型

### 5.1 栏目与文章

Column 承担“栏目 → 内容列表 → 文章详情”的传统信息发布结构，至少支持名称、稳定 alias、父子层级、排序、启停以及文章封面数据策略 `coverPolicy`。

`coverPolicy` 支持：

- `NONE`：该栏目站内文章不保存封面引用；
- `OPTIONAL`：站内文章可以有封面，也可以无封面；
- `REQUIRED`：站内文章草稿允许先无封面暂存，但发布前必须设置封面；已经处于发布状态的文章后续编辑也必须继续满足该要求。

外链文章只维护来源站点的基础链接信息，不保存本地正文、封面、正文图片或附件，因此不按 Column `coverPolicy` 强制本地图片。

主站栏目公开 URL：`/column/{alias}`。

Article 至少支持标题、主栏目、富文本正文、来源、发布日期、封面/缩略图、附件、置顶、展示顺序、草稿/已发布/已撤回、实际发布时间和浏览量。公开 Article Summary 保留可选封面资源引用，是否展示以及展示尺寸仍由页面模板决定。

Article 区分 `INTERNAL / EXTERNAL_LINK`；`articleType` 是创建时来源身份，创建后不得通过普通编辑切换。Admin 编辑态只读，Backend 必须独立拒绝绕过 UI 的来源类型修改。外链文章不复制外部正文、图片和附件；公开列表直接打开原文，默认新窗口。

Article 不再维护全局 `recommended` 布尔属性。公开文章默认排序为：`置顶 DESC → 展示顺序 DESC → 发布日期/实际发布时间 DESC → id DESC`。如未来出现“首页推荐 / 专题推荐 / 人工推荐区”等独立展示需求，使用 `CmsList + ARTICLE` 明确投放，不恢复全局推荐状态。

主站站内文章 canonical URL 为 `/article/{id}`。

“招聘公告”栏目允许同时包含站内文章和外链文章；首页“招聘公告”区域只聚合已发布外链文章。

中心党建 stable 栏目：

- 父栏目：`中心党建`，alias `party`，只承担后台组织和 Party 作用域识别；
- `高层声音`，alias `party-voice`，legacy `typeCode=gcsy`；
- `工作动态`，alias `party-work`，legacy `typeCode=gzdt`；
- `党规党章`，alias `party-rules`，legacy `typeCode=dgdz`；
- `理论学习`，alias `party-study`，legacy `typeCode=llxx`；
- `主题教育`，alias `party-theme-education`，legacy `typeCode=zhutijiaoyu`。

这些栏目允许同时包含 `INTERNAL` 与 `EXTERNAL_LINK` Article。Party canonical 栏目 URL 为 `/party/column/{alias}`；Party INTERNAL Article canonical URL 为 `/party/article/{id}`。Party 前端只把 Party 栏目树中的文章作为 Party 站内详情渲染，普通主站文章不得由 `/party/article/**` 套用 Party 模板。

`学习园地` 是 PartyHome 对“党规党章 / 理论学习”的固定视觉分组，不新增 Column、CmsList 或 displayMode；`主题教育` 是正常可访问内容栏目，但不新增 PartyHome 第五个固定内容区。

### 5.2 单页与单页分组

单页具有稳定页面身份和公开 URL，不是“没有发布时间的文章”。技术模型继续使用 `Page / PageGroup`，产品和管理界面统一称为“单页 / 单页分组”。独立单页为 `/page/{alias}`；单页分组成员为 `/page/{groupAlias}/{alias}`。

单页分组表达多个单页共享的业务组织和公共 Tab。Tab 必须由单页分组成员数据生成，不在前端重复硬编码成员清单。单页分组当前为平级组织对象，不建设嵌套分组层级。

单页管理左侧必须提供“全部单页”“独立单页”和所有单页分组：选择“全部单页”查看全部；选择“独立单页”只查看 `groupId = null` 的成员；选择具体分组只查看该组成员。在具体分组上下文新增单页时默认带入当前分组，在独立单页上下文新增时默认不分组，但编辑表单允许重新选择分组。

`guide` 单页分组至少保持：`jypq`、`dagl`、`dygl`、`xlrz`、`contact`、`faq`；`jobs` 单页分组保持在招职位、招聘简章、双选会、现场宣讲、留省就业占位。

## 6. 导航模型

### 6.1 导航位置

导航位置是独立 CMS 业务对象，而不是编译期 Enum。至少维护 code、名称、说明、排序、启停和系统标识。

JilinJobs stable Site structure 至少包含：

- `MAIN`：主导航；
- `HOME_SHORTCUT`：首页首屏右侧快捷入口；
- `HOME_QUICK`：首页快速导航。

后续可增加 Footer、移动端、专题导航等位置，而不要求修改后端枚举并重新发布程序。

### 6.2 导航条目

导航条目属于一个导航位置，支持父子层级、排序、启停、打开方式、可选图标及目标类型。目标至少支持 HOME、COLUMN、PAGE、LINK、PLACEHOLDER。

Navigation `iconPath` 是导航条目自身的可选业务数据，使用 `/static/**`。公开站不得根据导航数组下标、排序序号或类似 `top-nav-01 / guide-01` 的位置约定推导业务图标；调整排序、插入或删除导航不得导致图标与语义错位。

CMS 管理端允许选择稳定 Site icons，也允许上传自定义导航图标。稳定资产物理文件名可以保留，但业务含义必须来自结构/语义映射，不依赖文件序号。

父子导航必须处于同一位置；禁止循环；有下级菜单时不得直接删除父级。

主导航按现网结构建立，包括网站首页、中心党建、招聘信息、业务指南、政策法规、就业指导、典型事迹、预决算公开、关于我们及确认的二级菜单。“空中宣讲”不进入基线。中心党建条目以站内 `LINK` 指向 `/party/` 并在当前窗口进入 Party Entry。

Party 内部栏目入口由 Party 页面结构和 Column 数据驱动，不要求复制到 Main `MAIN` 导航位置，也不新增第二套全局 Navigation Authority。

首页首屏右侧“就业信息填报、学历认证、全国征兵网、预决算公开、举报电话及邮箱”由 `HOME_SHORTCUT` 维护；业务指南快捷入口由 `HOME_QUICK` 维护；二者图标均由 NavigationItem 自身数据维护。

## 7. 通用列表与内容投放

CMS 提供通用列表与列表项，用于维护不属于导航、栏目归属或宣传展示位自身的可排序展示集合。

列表至少维护 code、名称、分组、`imagePolicy`、说明、排序、启停和系统标识。列表不控制前台展示模式，不设置 `displayMode`，也不使用旧 `LINK / IMAGE_LINK / TEXT` 组合型 `itemType`。

CmsListItem 的来源身份只使用：

1. `LINK`：列表项自身维护业务数据；
2. `ARTICLE`：引用已有 Article，用于把文章投放到轮播等展示容器。

LINK 至少满足：标题必填，作为后台管理识别名称并可供页面作为可访问性文本/替代文本；副标题可选；URL 可选且存在时必须是合法站内路径或 HTTP(S) 地址；图片是否允许/必填由所属列表 `imagePolicy` 决定；同时保留打开方式、排序、启停和必要扩展数据。页面可以按设计不显示标题或图片，但不得因此改变 CMS 数据契约。

`sourceType` 是创建时身份；ARTICLE 的 `articleId` 同样是创建时来源身份。Admin 编辑态必须只读/禁用，Backend 必须独立拒绝 LINK↔ARTICLE 切换或 ARTICLE 更换关联文章。需要改变来源时删除原项并创建新项。

ARTICLE placement 保持以下边界：

- Article 仍只有一个 `columnId`；投放不改变栏目归属、栏目列表或详情面包屑；
- 只有关联 Article 当前 `PUBLISHED` 时 placement 才公开；撤回后自动退出，重新发布后可按既有投放恢复；
- Main INTERNAL target 为 `/article/{id}`；Party INTERNAL target 为 `/party/article/{id}`；EXTERNAL_LINK 使用 Article 当前 external URL；
- target 继续遵守 CmsListItem `DEFAULT / SAME_WINDOW / NEW_WINDOW` `openMode`；
- 不复制第二份文章正文，不通过文章标题推断关系。

`imagePolicy` 支持：

- `NONE`：列表项不使用图片；
- `OPTIONAL`：允许最终无图；
- `REQUIRED`：公开投放必须形成有效图片。

LINK 使用自身图片。ARTICLE 可以继承 Article 当前封面，也可以由后台从正文图片候选中选择或上传/选择 CMS Resource 形成列表专用覆盖；选中的正文图片必须把 Resource ID 固化为覆盖，不允许 Public Runtime 隐式寻找“正文第一张”。覆盖图片不修改 Article 封面/正文。

REQUIRED ARTICLE 在保存时必须形成有效图片；既有 ARTICLE 后续因文章封面被移除且自身没有有效覆盖时，必须自动退出 REQUIRED 公开列表，直到重新形成有效图片。修改列表 imagePolicy 时不得制造已有 item 与新策略冲突；切换到 `NONE` 前先清除不允许的既有图片/覆盖，切换到 `REQUIRED` 前必须先确保既有项能形成有效图片。Backend 对直接 API 调用执行同一校验。

`HOME_CAROUSEL` 与 `PARTY_CAROUSEL` 均为 `imagePolicy=REQUIRED`。`SITE_LINKS` 分组当前按文字链接展示、`imagePolicy=NONE`；未来需要 Logo 时通过数据策略和内容数据调整，不新增 display mode。

不得继续使用 `HOME_BANNERS`、`SITE_LINK_GROUPS` SiteProperty JSON 作为运行时主数据源。

## 8. 宣传展示

CMS 提供宣传展示位和展示内容模型。展示位代表公开站稳定布局中预留的宣传、专题或活动图片区域。产品界面统一使用“宣传展示管理 / 展示位 / 展示内容”；技术实现可继续保留 Advertisement 相关内部标识。

展示内容支持标题、图片、目标 URL、打开方式、启停、展示顺序以及可选开始/结束时间。

同一展示位允许维护多条展示内容。公开站只消费当前有效内容并按展示顺序输出：1 条时静态展示，2 条及以上时可以轮动；当前运营通常建议不超过 3 张，但不作为全局硬限制。

URL 与点击行为相互独立。URL 可以为空；打开方式支持默认、当前窗口、新窗口以及 `NO_LINK`。`NO_LINK` 表示只展示、不跳转，即使已经保存 URL 也不得点击，同时不得清空 URL；恢复其他打开方式后继续使用原目标。

展示内容 sortOrder 用于同一展示位成员顺序；展示位 sortOrder 只用于多个展示位之间的顺序。开始/结束时间控制公开有效期，过期不自动删除；管理端应表达“已停用 / 待生效 / 展示中 / 已过期”等状态。

首页“招聘活动横幅”使用 `HOME_RECRUITMENT_PROMO`。首页主轮播使用 CmsList，不与 Advertisement 合并为一个业务对象。

## 9. 网站属性与统一轮播参数

后台提供通用 SiteProperty 管理。属性至少维护 key、名称、分组、值、值类型、说明、排序、必填、系统标识和启停状态。

属性分组属于工程 metadata，不作为另一个可运营 CMS 对象。可选分组由 Spring 外部化配置资源定义并排序，当前至少包括 `BASIC / BRAND / CONTACT / FOOTER / PRESENTATION / GENERAL`；SiteProperty 只能引用已声明分组，Admin 不允许自由输入不存在的 group code。

值类型至少支持 `TEXT`、`RESOURCE_PATH`、`JSON`、`URL`、`BOOLEAN`、`INTEGER`。Backend 根据属性自身 `valueType` 验证，不使用编译期 key Enum 白名单限制可维护 key。

Main / Party 当前统一使用：

### `CAROUSEL_INTERVAL_SECONDS`

- `PRESENTATION` / `INTEGER`；
- 默认 `4` 秒；
- 新写入必须为大于 0 的整数；
- 缺失、非法历史值或不大于 0 时 Public fallback 为 4 秒。

### `CAROUSEL_MAX_ITEMS`

- `PRESENTATION` / `INTEGER`；
- 默认 `5`；
- 新写入必须为大于 0 的整数；
- 表示单个轮播区域最多消费的有效项数，不限制 Backend 可维护记录数；
- 缺失、非法历史值或不大于 0 时 Public fallback 为 5。

旧 `HOME_CAROUSEL_INTERVAL_SECONDS` 已被 supersede，不承担 Current Runtime responsibility。

SiteProperty 用于站点名称/简称、品牌资源、页头页脚公共信息、地址/电话/办公时间、备案/版权，以及少量确有运营调整价值的低风险站点行为参数。`RESOURCE_PATH` 图片属性复用统一图片资源选择/上传能力。

`HOME_BANNERS`、`SERVICE_LINKS`、`SITE_LINK_GROUPS`、`HOME_PROMO_BANNER_PATH` 不再属于 Current SiteProperty 主数据；`HOME_NCSS_LOGO_PATH` 不使用 CMS 配置，NCSS 区域作为固定工程集成。

当前阶段管理端可直接维护属性定义和值，不根据用户身份实施差异限制。

## 10. 静态资源、Site Package 与 Runtime 上传

网站提供 StaticResource 浏览、上传、查看/下载、替换、回收、恢复及受保护资源安全能力。

### 10.1 Stable Site assets 与 Runtime target

JilinJobs stable Site asset 的唯一版本化 source owner 是：

```text
sites/jilinjobs/assets/**
```

Stable assets 通过 Site Package manifest/catalog 投影到既有公开 Runtime 路径，例如：

```text
/static/
├── brand/
├── footer/
├── health/
├── home/
├── icons/
└── party/
```

公开 `/static/**` URL contract 不因 source owner 收敛而改变。稳定 Party Logo、背景、装饰图等进入 `sites/jilinjobs/assets/party/**` 等语义目录；历史文章正文图片仍属于 Historical Content Migration。

### 10.2 Runtime uploads

CMS Runtime 上传统一进入 `/static/uploads/**`，不属于 stable Site asset ownership：

- `/static/uploads/displays/{slotCode}/`；
- `/static/uploads/lists/{listCode}/`；
- `/static/uploads/site-properties/{key}/`；
- `/static/uploads/navigation-icons/`。

上传文件名由系统生成稳定名称。宣传展示、列表项、Navigation icon、`RESOURCE_PATH` SiteProperty 等图片字段复用同一选择/上传交互：当前预览、上传新图片、选择适用已有/内置图片、清除可选值。

日常业务管理员不应被迫先进入 StaticResource 页面上传，再复制 `/static/...` 路径回业务表单。StaticResource 管理继续作为全局资源浏览、显式替换、回收和清理入口。

改变 CMS 对象图片引用不得自动物理删除旧图片；停止引用后由 StaticResource 管理按实际情况清理。

### 10.3 安全与引用保护

上传与替换继续验证路径安全、扩展名、真实媒体内容。站内工程资产不允许 CMS 用户上传任意 HTML/JavaScript 并执行。

受保护状态由 Backend 计算，而不是管理员人工维护：

- Site Package stable asset manifest/catalog targets；
- Spring 外部化配置声明的固定部署受保护资源；
- 当前 SiteProperty、CmsList、Advertisement、Navigation 等可识别 Runtime 引用。

受保护资源普通删除必须拒绝，明确 replace 仍允许。第一阶段不宣称建立完整 CSS/JS/富文本引用关系图，Admin 对普通资源删除继续提示该限制。

## 11. 首页、轮播与公开站

首页采用固定模板，不建设通用 Page Builder。布局、尺寸关系、组件结构和主要视觉层级由 Public Site 工程按现网复刻。

主站首页运营数据优先来自 CMS：普通资讯来自 Column/Article；主导航与快捷入口来自 Navigation；业务图标来自 Navigation `iconPath`；轮播内容来自 `HOME_CAROUSEL`；统一行为参数来自 `CAROUSEL_INTERVAL_SECONDS / CAROUSEL_MAX_ITEMS`；友情链接来自 CmsList；招聘活动横幅来自 Advertisement；站点名称、联系方式等来自 SiteProperty；固定 NCSS 集成保留为工程资产。

主站首页第一版继续复刻 Header、主导航及二级菜单、轮播、通知公告、固定业务入口、招聘日历、就业动态、业务指南快捷入口、专题/宣传 Banner、招聘相关区域、招聘公告、NCSS 固定集成、网站导航/友情链接和 Footer。EU-44 不改变这些既有页面范围。

Public Site 不得重新硬编码 CMS 已经提供的业务数据；明确属于稳定工程设计的内容可以保留代码 / stable Site asset 实现。通用列表是否显示图片/Logo/名称、宣传展示区域尺寸、Navigation icon 尺寸等由页面设计固化，不作为 CMS 可配置视觉参数。

### 11.1 Main / Party 共享 Carousel lifecycle

Main / Party 统一以下行为规则，但不统一 DOM、主题、caption 或比例：

- 0 个有效项：稳定空态；
- 1 个有效项：静态展示，不启动 timer；
- 2 个及以上：按列表顺序循环自动切换；
- 提供手动分页；
- hover 暂停；
- focus 位于轮播内部时暂停；
- 页面初始隐藏时不得启动，后续 visibility hidden 时继续暂停；
- 暂停解除后从当前项继续，不重置到第一项；
- `prefers-reduced-motion: reduce` 时关闭自动播放和切换动画，手动分页仍可用；
- 图片加载失败项退出当前有效集合，后续有效项补位；全部失败进入稳定空态；
- 有效集合或 max-items 变化时优先按 item ID 保持当前内容，只有当前项失效才自然切换；
- EU-30 不新增 swipe，不引入第三方 Carousel 依赖。

`CAROUSEL_MAX_ITEMS` 在有效性和失败项处理后限制当前最多展示项数；Backend 允许维护更多记录，通过排序决定哪些项进入前 N。

Main 当前稳定比例 `8:5`，图片 `object-fit: cover`；Party 保持 `585:329` 视觉比例。两者视觉主题、caption、dot 和 DOM 可以独立；切换动画为轻量 opacity fade，reduced-motion 时无动画。

### 11.2 Party 内容页面主题

PartyHome 固定呈现顶部重点内容、高层声音、工作动态、学习园地；学习园地内部包含党规党章与理论学习。`party-theme-education` 是正常 Party 内容栏目，但不新增固定首页第五区。

Party 栏目列表与文章详情 breadcrumb 使用一致字号、间距、颜色和交互主题；栏目分页、跳转、每页条数等交互态使用 Party 红色主题，不泄漏 Main 蓝色主题。每页条数选择器使用可主题化控件，不依赖不可控的原生 `<select>` 弹层选中色。

Party 正式视觉继续以原站证据 + AI Visual + Human Review 收敛，不以 Foundation CSS 作为最终视觉 Authority。

## 12. URL、页面上下文与模板

公开 URL 与具体 HTML Entry 解耦。Main 普通栏目 `/column/{alias}`，INTERNAL Article `/article/{id}`，独立单页 `/page/{alias}`，单页分组成员 `/page/{groupAlias}/{alias}`；这些 URL 全部属于同一 Main Site Entry，`/page/**` 不再维持无业务价值的重复 Vue App / HTML Entry。

Main Site 源码继续保持当前 `app / shell / modules` 所有权边界；页面模块按 home / content / page / integration 等真实职责组织，并使用 route-level lazy loading，避免将所有页面同步绑定到首屏 bundle。

Party canonical namespace：

- 首页：`/party/`；
- 栏目：`/party/column/{alias}`；
- INTERNAL Article：`/party/article/{id}`。

Party 栏目列表和文章详情由 Party Entry / Router / Shell 承载，保持红色主题和直接访问/刷新能力。Party EXTERNAL_LINK 从列表直接打开原文，不进入本地详情模板。

原站 `/plist.html?typeCode=...`、`/pdetail.html?content_id=...`、`/detail.html?content_id=...` 及已观察到的参数变体只作为 Historical Migration mapping input，不作为新版 canonical URL。迁移可保存 legacy id/typeCode/detail path 映射，但新版 Router 不依赖旧 query-string 页面模型。

面包屑来自 Column / PageGroup / Page / Article 业务关系，不从 URL、legacy typeCode 或某个 Navigation 入口机械推导。Main 普通栏目列表、Article 详情、独立单页与业务指南单页分组继续以现网页面主要版式为复刻基准；Party 栏目与 Article 使用 Party 专属内容模板。

## 13. Generic Schema、Site provisioning 与 Historical Migration

当前站点构成必须按职责分离：

```text
Generic Backend Flyway Schema
→ JilinJobs stable Site structure
→ optional Fresh Site one-time bootstrap
→ stable Site asset projection
→ Historical Content Migration when required
→ operator-managed Runtime
→ Public Renderer
```

### 13.1 Generic Backend Flyway

Backend active Flyway 当前仅：

```text
V1__current_cms_schema.sql
V2__site_provisioning_schema_capabilities.sql
```

只承担 Generic CMS Schema evolution 与 site-neutral provisioning capability，不注入 JilinJobs instance rows。EU-30 前 V11、Party V13/V14 等历史 migration number 只属于历史实现 / upgrade evidence，不定义 Current initialization lifecycle。

### 13.2 JilinJobs Site Package

- `sites/jilinjobs/structure/**`：stable Site structure；
- `sites/jilinjobs/bootstrap/**`：Fresh Site one-time operational defaults；
- `sites/jilinjobs/assets/**`：stable Site assets。

Stable structure 可 reconcile；one-time bootstrap 成功后数据成为普通 operator-managed Runtime data，后续 restart/reconcile/repeated bootstrap 不得 overwrite 或 resurrect 管理员修改/删除。

测试 fixture 只建立测试场景数据，不维护第二套站点基线。

### 13.3 Historical Content Migration

`data-migrations/**` 承担历史 Article、EXTERNAL_LINK、正文资源、附件、历史列表成员、legacy identity/fingerprint 和 provenance。Canonical Dataset 使用 stable Site identity，不依赖临时 Runtime DB id 或 Public Renderer 内部实现。

当前 Party migration authority：

- EU-29 frozen `acceptedSnapshot` = 181 Articles，原 artifact/provenance 不因后续 promotion 重写；
- EU-30 接受 `party-theme-education` 2 条增量后，current canonical Runtime Dataset = 183 Articles；
- 当前 4 个 carousel items 已接受；
- 原轮播 position 2 通过 stable `sourceSystem + legacyKey` 解析主题教育 Article，使用 ARTICLE placement，保留原 PNG 作为列表专用覆盖 Resource 和 `NEW_WINDOW` 语义，target 为新版 Party canonical route；
- EU-29 → EU-30 upgrade-only compatibility 保持可验证；
- Historical Migration 不并入 Flyway、Site stable structure、bootstrap 或 stable asset manifest。

## 14. 权限规划边界

后台最终复用智慧就业云平台统一账号和权限体系。未来可为 Column、Navigation、Article、Page、CmsList、Advertisement、SiteProperty、StaticResource 分别提供权限点，并规划普通管理员 / 超级管理员的差异。

**上述内容当前只作为未来规划，不属于当前阶段实现或验收条件。** 当前阶段不得为此引入临时登录、角色、超级管理员标识或前端假权限。

`preset` 保护、删除确认、路径安全、真实媒体校验、来源身份 immutable 和受保护资源属于产品/数据安全契约，不等于当前已经实现用户权限体系。

## 15. 发布与公开规则

Article 状态保持：`草稿 → 已发布 → 已撤回 → 已发布`。编辑不自动改变状态；只有 `PUBLISHED` 进入正常公开发现范围；撤回后从首页、栏目列表和 ARTICLE placements 的公开结果退出；重新发布后既有有效 placement 可以恢复。不存在、已删除或已撤回的内容直接访问时显示统一不可用提示。

Column `coverPolicy=REQUIRED` 时，无封面草稿不得发布；已发布 Article 也不得通过普通编辑变成缺少必填封面的状态。

Party EXTERNAL_LINK 公开时直接进入来源网站；INTERNAL 只有属于 Party 栏目树时允许由 `/party/article/{id}` 呈现。Main / Party 不因共用 Article 数据表而互相改变主题或 canonical URL。

Navigation、CmsListItem、Advertisement、SiteProperty 等公开数据只消费启用且满足自身有效条件的数据。ARTICLE placement 还必须满足关联 Article `PUBLISHED` 与列表 imagePolicy；Advertisement 若配置开始/结束时间，只在当前有效期公开；`NO_LINK` 只影响点击行为，不改变当前有效内容身份。

## 16. 验收要点

当前 canonical Requirement 至少要求持续满足：

1. 管理端侧边栏按“内容管理 / 内容结构 / 运营展示 / 站点设置”组织现有 CMS 能力，不新增独立“系统设置”模块；
2. 产品界面使用“单页管理 / 单页 / 单页分组”，技术层 `Page / PageGroup` 和既有 API 保持兼容；
3. 单页管理左侧提供“全部单页 / 独立单页 / 单页分组”，右侧按当前组织上下文展示成员；具体分组上下文新增时默认带入分组；
4. Article 管理左侧 Column tree 作为组织上下文，父栏目可聚合后代文章；列表使用服务端摘要分页查询，不要求浏览器下载全部正文后筛选；Column 管理自身继续直接维护栏目树；
5. Column 支持 `NONE / OPTIONAL / REQUIRED` coverPolicy；REQUIRED 允许无封面 draft，但 publish/已发布 edit 必须满足封面契约；Public Article Summary 可返回可选封面引用；
6. `Article.articleType` 创建后不可普通编辑；Article 不存在全局 `recommended`，独立推荐使用 `CmsList + ARTICLE`；
7. NavigationLocation 独立维护；管理列表只展示当前选中位置的 Navigation tree；NavigationItem 图标属于自身数据，排序变化不得造成图标语义错位；
8. `HOME_SHORTCUT` 与 `HOME_QUICK` 驱动对应 Main 首页入口及图标，前端不重复硬编码成员/图标关系；
9. CmsList 不使用 displayMode 或旧组合型 itemType，图片策略保持 `NONE / OPTIONAL / REQUIRED`；LINK 标题必填且 URL 执行站内路径 / HTTP(S) 校验；
10. CmsListItem current source model 为 `LINK / ARTICLE`；ARTICLE placement 不改变 Article 唯一栏目归属；
11. `CmsListItem.sourceType` 与 ARTICLE `articleId` 创建后不可普通编辑，Admin 与 Backend 同时强制；
12. ARTICLE 仅在关联 Article `PUBLISHED` 时公开；INTERNAL target 由 Main / Party 生成各自 canonical route，EXTERNAL_LINK 使用当前外链，并继续遵守 placement `openMode`；
13. ARTICLE image 可继承 Article 封面或使用显式 CMS Resource override；正文图片选择必须固化 Resource ID，Public 不隐式取正文第一张；
14. REQUIRED ARTICLE 失去 effective image 后不得继续公开；列表 imagePolicy 变更不得制造已有数据冲突；
15. `HOME_CAROUSEL / PARTY_CAROUSEL` 均为 REQUIRED；`SITE_LINKS` 当前按文字链接消费；
16. Main / Party 统一使用 `CAROUSEL_INTERVAL_SECONDS`（默认 4）与 `CAROUSEL_MAX_ITEMS`（默认 5），Backend 拒绝非正整数新值；
17. Carousel 0/1/多项、manual paging、hover/focus/visibility pause、resume、reduced-motion、failed-image backfill、current item identity 行为符合 §11.1；
18. Main 轮播保持 `8:5`，Party 保持 `585:329`；共享 lifecycle 不强制共享视觉 DOM/主题；
19. 管理端用户可见名称使用“宣传展示管理 / 展示位 / 展示内容”；首页招聘活动横幅由 `HOME_RECRUITMENT_PROMO` AdvertisementSlot 驱动，多条当前有效内容按顺序轮动；
20. Advertisement `NO_LINK` 保留 URL 但禁止点击，恢复其他 openMode 后 URL 继续可用；有效期控制公开可见性且过期记录保留；
21. 宣传展示、CmsList、Navigation icon、RESOURCE_PATH SiteProperty 复用统一图片选择/上传，Runtime 上传进入 `/static/uploads/**`；
22. SiteProperty group 来自 Spring metadata，支持 INTEGER 等 typed validation，不依赖编译期 key Enum；
23. NCSS 首页区域使用固定工程集成，不提供 CMS 管理项；
24. 当前阶段不存在基于用户角色/权限的功能限制实现；
25. Main `/`、Column、Article、Page、PageGroup canonical URL 与蓝白视觉主基线无回归；`/page/**` 不维持重复 HTML Entry；Main 源码保持当前 ownership boundary 与 route-level lazy loading；
26. `/party/`、`/party/column/{alias}`、`/party/article/{id}` 由 Party Entry / Router / Shell / 红色主题承载并支持直接访问/刷新；Main / Party Navigation 与 Footer 继续复用 Shared Shell Components，Site-specific 内容主题互不污染；
27. Party stable structure 包含父栏目 `party` 与五个子栏目 `party-voice / party-work / party-rules / party-study / party-theme-education`；
28. Party 五个栏目复用通用 Column + Article，允许 INTERNAL / EXTERNAL_LINK；不新增 Party 专属 CMS 类型、`site` 字段、Admin Module 或第二套 Article 模型；
29. PartyHome 固定顶部重点内容、高层声音、工作动态、学习园地；学习园地含党规党章/理论学习；主题教育不新增第五个固定首页区；
30. Party EXTERNAL_LINK 直接打开原文；Party INTERNAL 使用 Party 详情模板并拒绝非 Party Article；不存在/已删除/已撤回内容直接访问使用统一不可用提示；
31. Party breadcrumb、分页、每页条数等内容页交互保持 Party 红色主题，不泄漏 Main 蓝色；
32. Generic Backend active Flyway 仅 V1 schema + V2 site-neutral provisioning capability，不承担具体 JilinJobs instance rows；
33. JilinJobs stable structure / one-time bootstrap / stable assets 分别由 `sites/jilinjobs/{structure,bootstrap,assets}/**` 持有；
34. one-time bootstrap 后 operator data 不被普通 restart/reconcile/repeated bootstrap 覆盖或 resurrect；
35. stable Site asset source 是 `sites/jilinjobs/assets/**`，公开 target 继续 `/static/**`；CMS Runtime uploads 继续 `/static/uploads/**`；
36. Historical Content Migration 继续属于 `data-migrations/**`，不进入 Flyway 或 stable Site asset ownership；
37. Party current canonical Runtime Dataset = 183 Articles，EU-29 frozen acceptedSnapshot = 181，4 个 accepted carousel items 与 EU-29→EU-30 compatibility 可独立审计；
38. 原站 `plist / pdetail / detail` 与 `content_id / typeCode` 变体只作为迁移输入，新版 canonical URL 不依赖旧 Router 模型；
39. StaticResource 真实媒体验证、路径安全、受保护资源与回收恢复无回归；stable manifest targets 和当前 CMS 引用能够进入保护集合；
40. Party 正式视觉继续由原站证据、自动功能验证、AI Visual 与 Human Review 共同支撑，Functional Browser PASS 单独不等于 Visual Fidelity；
41. Backend、Public Site、Admin Frontend、Site Package 与 Integrated Browser 的 Current Evidence 必须与实际目标提交和 Evidence Claim 匹配；
42. Human Review Environment 在需要人工视觉/内容复核时能够同时访问 Main、Party 与 `/admin/`，且评审基线与自动测试数据隔离。
