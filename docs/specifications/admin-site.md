---
id: specification-admin-site
title: CMS 管理端产品规格
type: specification
status: accepted
version: "V2.0"
relations:
  requirements:
    - docs/requirements/information-publishing.md
    - docs/requirements/cms-domain.md
  architecture:
    - docs/architecture/cms-architecture.md
  related:
    - docs/specifications/rich-text-authoring.md
    - docs/specifications/page-content-architecture.md
updated_at: 2026-09-15
---

# CMS 管理端产品规格

## 1. Scope

本规格定义 CMS 运营人员可以观察和操作的管理端行为。

CMS Domain、stable/source identity、state/lifecycle 与数据完整性由 `docs/requirements/cms-domain.md` 持有；Admin application / module architecture 由 `docs/architecture/cms-architecture.md` 持有；具体 Vue / Element Plus / component / API wiring 属于 Technical / implementation。

## 2. 入口与信息架构

管理端 canonical browser namespace 为 `/admin/cms/**`。兼容旧地址可以重定向，但不能成为新的第二套产品导航结构。

一级管理信息架构按业务职责组织，不增加无业务价值的中间点击层：

- 内容管理：文章、单页、列表；
- 内容结构：栏目、导航；
- 运营展示：宣传展示；
- 站点设置：网站属性、静态资源。

界面使用运营人员可理解的业务术语，不暴露数据库表、Frontend module、Migration、Backend class 或其他实现术语。

## 3. 通用管理交互

### 3.1 Container → Member

存在明确容器 / 组织上下文的对象，优先使用“选择上下文 → 管理成员”：

- 栏目 → 文章；
- 单页分组 → 单页；
- 列表 → 列表项；
- 导航位置 → 导航条目；
- 展示位 → 展示内容。

容器自身就是主要维护对象时可以直接维护结构，例如栏目树。

### 3.2 可用空间

主导航和局部组织面板必须允许在当前浏览器会话中收起 / 恢复，使较窄桌面宽度仍能完成主要管理任务。

当前不因此建立用户 Profile、服务端显示偏好或新的 CMS 配置项。

### 3.3 行操作

常规行操作应紧凑且具有明确可访问名称 / Tooltip；复杂容器操作可以使用更多操作菜单。运营人员不能只凭无法解释的图标猜测 destructive action。

## 4. 用户提示与解释责任

Admin 只长期展示完成当前操作所需的信息：

- 必填 / validation failure；
- 发布、撤回、启停等状态后果；
- 删除、替换等高风险操作确认；
- 创建后不可修改的来源 / 稳定身份限制；
- 受保护资源与引用风险；
- EXTERNAL_LINK、`NO_LINK` 等直接改变目标行为的语义。

以下内容不作为运营 UI 的长期说明：

- “由 Backend / Database / Frontend / deployment 负责”；
- Requirement / Method 背景；
- metadata 存储位置；
- “字段不决定页面布局”等纯设计职责说明；
- 控件本身已经自然表达的重复 Alert。

稳定 `alias / code / key` 对运营人员使用“公开标识 / 列表标识 / 位置标识 / 展示位标识 / 属性标识”等可理解名称。

## 5. 文章与栏目

### 5.1 Article management

文章管理提供栏目组织上下文和文章列表：

- 可查看全部文章；
- 选择父栏目时可聚合其后代栏目文章；
- 关键词、状态、Article source type 等作为辅助筛选；
- 在栏目上下文新增文章时默认带入当前栏目，运营人员仍可调整；
- 所属栏目选择必须表达真实层级。

Article source type 创建后只读，不能通过普通编辑 INTERNAL ↔ EXTERNAL_LINK 切换。

### 5.2 Cover policy

文章表单根据当前 Column cover policy 表达：

- `NONE`：不提供本地封面编辑；
- `OPTIONAL`：封面可选；
- `REQUIRED`：草稿可暂存无封面，但发布前必须补齐。

外链文章不要求本地正文 / 封面。

### 5.3 Column management

栏目管理直接维护树形结构、公开标识、排序、状态、封面数据策略与子栏目关系。稳定 / 预置栏目按照 Domain protection 规则显示受保护身份和删除限制。

## 6. 单页

单页管理左侧至少提供：

- 全部单页；
- 独立单页；
- 当前全部单页分组。

选择分组时只显示其成员；在具体分组上下文新增时默认带入该分组，在独立单页上下文新增时默认无分组，但表单允许调整。

不同 Page content profile 使用对应 authoring surface：

- Rich Text → whole-body Rich Text authoring；
- Structured Card Collection → card-aware authoring；
- unsupported profile/schema → blocking diagnostic / safe read-only state。

ordinary content edit 不允许随意改变稳定 Page identity、content model、renderer identity 或 ownership。

## 7. 导航

导航管理先选择 NavigationLocation，再维护该位置的树形 NavigationItem。

要求：

- 主数据区域只显示当前所选位置的条目；
- parent candidate 只来自同一位置；
- 稳定位置 / 条目明确显示受保护身份；
- NavigationItem 图标是条目自身数据，可选择或上传受控图片；
- 调整排序不得改变图标与业务语义的对应关系。

当前稳定位置至少包括 `MAIN`、`HOME_SHORTCUT`、`HOME_QUICK`。

## 8. 通用列表

列表管理先选择 CmsList，再维护其 CmsListItem。

### 8.1 List definition

List definition 可以维护业务名称、列表标识、图片数据策略、说明、排序与启停。普通运营创建 / 编辑不直接维护内部稳定 `groupCode`；稳定结构分组由受控站点定义持有。

### 8.2 ListItem

ListItem authoring 根据 source identity：

- LINK：维护自身 title / target / open mode / allowed image；
- ARTICLE：选择既有 Article 并维护 placement / presentation override。

source type 创建后不可切换；ARTICLE relation 创建后不能普通改成另一篇文章。

### 8.3 Image policy

根据父列表：

- `NONE`：不提供图片输入；
- `OPTIONAL`：图片可选；
- `REQUIRED`：必须形成有效图片。

该策略不在 Admin 中解释成具体 Public layout mode。

## 9. 宣传展示

宣传展示管理先选择展示位，再维护展示内容。

展示内容至少支持：

- 标题；
- 图片；
- 可选 URL；
- open mode；
- 排序；
- 启停；
- 可选有效期。

`NO_LINK` 时保留 URL 但当前不产生点击，界面以简洁用户语言表达这一行为。

## 10. 网站属性

网站属性按受控分组浏览，属性定义与日常值编辑分离。

- 定义编辑维护属性标识、名称、分组、类型、说明等；
- 值编辑根据 value type 提供合适控件；
- UNKNOWN group / invalid typed value 必须被拒绝；
- RESOURCE_PATH 等图片属性复用统一资源选择体验；
- UI 不解释 metadata 存储位置、Database 或 deployment ownership。

## 11. 静态资源

静态资源管理至少支持：

- 浏览资源；
- 图片预览；
- 上传 / 明确替换；
- ordinary delete；
- 显示受保护状态。

受保护资源普通删除必须明确拒绝。系统不能证明全部 Rich HTML / CSS / JS 引用时，普通资源删除仍应提示残余引用风险。

改变业务对象引用不自动物理删除旧文件。

## 12. 图片编辑体验

Admin 中需要运营人员辨识图片的场景，应提供一致的预览与选择体验，包括：

- 当前图片可见；
- 上传新图片；
- 从允许范围选择既有图片；
- 可选值可以清除；
- 透明 / 浅色图标仍可辨识；
- 普通浏览场景可以查看原图。

该体验不能改变 Public image bytes 或把 Admin preview metadata写回业务数据。

## 13. Failure behavior

Admin 必须显式呈现：

- domain validation failure；
- immutable identity modification；
- preset/stable object delete rejection；
- required image/content missing；
- invalid typed SiteProperty；
- protected resource delete；
- unsupported Page content profile/schema；
- Backend conflict / fail-closed result。

不得通过前端默默修正成另一种业务语义来“让保存成功”。

## 14. Acceptance

触达管理端行为时根据实际范围至少验证：

- `/admin/cms/**` canonical entry 与必要 compatibility redirect；
- 四个业务分组和八类正式入口；
- 主侧栏 / 局部组织面板收起与恢复；
- Article 栏目上下文、source identity 与 cover policy；
- Page 分组与 profile-specific authoring；
- NavigationLocation 上下文、tree integrity、图标；
- CmsList source identity 与 image policy；
- Advertisement `NO_LINK` / valid period；
- SiteProperty group / typed value；
- StaticResource preview / protected delete；
- 用户界面不长期展示 Backend / Database / deployment / Requirement 实现说明；
- 必要风险和 validation 信息没有因“简化提示”而消失；
- Backend / Admin / relevant Integrated Browser regression。

## 15. Non-goals

- 当前阶段的用户 / 角色 /权限实现；
- 通用系统设置中心；
- generic Page Builder；
- 通过 Specification 固化具体 Vue component / Element Plus control；
- 从本规格授予新的 Execute Authority。