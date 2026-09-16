---
id: specification-party
title: 中心党建公开体验规格
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
    - docs/specifications/public-site.md
updated_at: 2026-09-15
---

# 中心党建公开体验规格

## 1. Scope 与业务定位

本规格定义主站“中心党建”专题入口的公开可观察行为、内容范围和视觉体验。

“中心党建”属于 Main 信息架构下的专题入口，不是第二个独立业务网站。`/party/**` 的独立 namespace 和红色主题用于保证专题呈现与内容作用域，不改变这一业务定位。

CMS Domain、Page/List/Article identity 与 lifecycle 由 Domain Requirement 持有；Public Site 技术隔离、共享组件与当前实现方式由 Architecture / Technical 持有。

## 2. 内容范围

当前已接受的 Party 内容栏目：

- 高层声音：`party-voice`；
- 工作动态：`party-work`；
- 党规党章：`party-rules`；
- 理论学习：`party-study`；
- 主题教育：`party-theme-education`。

业务规则：

- “学习园地”是党规党章 + 理论学习的页面视觉分组，不是独立内容类型；
- 主题教育是可访问内容栏目，但不因此成为 Party 入口页第五个固定内容区；
- Party 内容复用通用 Column + Article；
- INTERNAL 使用 Party 站内详情；
- EXTERNAL_LINK 直接进入当前外部来源；
- 普通 Main 非 Party 内容不能通过 `/party/article/**` 或 `/party/column/**` 套用 Party 模板。

## 3. Canonical URLs

```text
/party/                       中心党建入口
/party/column/{alias}         Party 栏目
/party/article/{id}           Party INTERNAL Article
```

要求：

- direct access / refresh 正常；
- Main 主导航“中心党建”进入 `/party/`；
- legacy `plist.html` / `pdetail.html` / `detail.html` 等旧地址只属于 migration / provenance，不继续作为新版 canonical URL；
- breadcrumb 来自当前业务对象关系，不从 legacy typeCode 或旧 URL 猜测。

## 4. 入口页

`/party/` 至少包含：

1. 已接受的中心党建 Banner；
2. 与 Main 一致业务结构的公共 Navigation；
3. 中心党建轮播；
4. 高层声音；
5. 工作动态；
6. 学习园地，其中包含党规党章与理论学习；
7. 与 Main 一致业务信息的 Footer。

`party-theme-education` 不增加入口页第五个固定内容区，但其文章可以通过栏目 URL、轮播或其他明确投放进入 Party 内容体验。

## 5. Banner

中心党建 Banner 是稳定专题视觉，不承担导航行为：

- Banner 自身不可点击；
- 正式运行使用项目受控的版本化本地资源，不以旧站静态资源 URL 作为 Runtime dependency；
- 不为格式优化而接受肉眼可见的标题文字清晰度退化；
- Banner 的具体版本化文件名、digest 与投影路径属于 Site Definition / implementation evidence，不在本规格复制。

## 6. Party carousel

中心党建轮播使用稳定内容容器 identity `PARTY_CAROUSEL`，并遵循通用 CmsList ARTICLE / LINK Domain semantics。

当前产品行为：

- 图片是有效公开项的必要内容；
- LINK 项使用自身 target；
- ARTICLE 项引用已有 Article，不复制正文；
- Article 必须已发布才进入轮播；
- INTERNAL Article target 使用 `/party/article/{id}`；
- EXTERNAL_LINK 使用 Article 当前外部目标；
- ARTICLE 可以使用明确的列表展示覆盖图，不反向修改 Article 正文或封面；
- open mode 继续有效；
- 历史轮播 provenance / compatibility 不进入普通运营模型。

轮播交互遵循 Public Site 共享 lifecycle：0 项空态、1 项静态、多项循环、手动控制、hover/focus/visibility 暂停、reduced-motion、失败图片剔除与稳定恢复。

Party 可以保持自己的尺寸比例、dot、caption 和红色主题，不要求与 Main 使用相同视觉表达。

## 7. 栏目列表

`/party/column/{alias}`：

- 只接受当前 Party 内容作用域内的栏目；
- 只展示当前栏目作用域的公开内容；
- 提供 breadcrumb、标题、分页、loading / empty / error；
- INTERNAL 进入 Party article detail；
- EXTERNAL_LINK 直接打开原文；
- 二级栏目列表当前与 Main 使用一致的主要 presentation primitive和分页能力；
- Party route scope 与 canonical URL保持独立。

分页的页码、跳转、每页条数等可操作状态不得泄漏错误的 Main 蓝色主题。

## 8. Article detail

`/party/article/{id}`：

- 只允许已发布且属于 Party 栏目树的 INTERNAL Article；
- 展示 Party 专题内容主题；
- breadcrumb 与栏目列表保持一致的信息层级；
- Rich Text、正文图片和附件遵循当前公共内容 / Resource contract；
- 不存在、撤回、删除或非 Party Article 显示明确不可用状态；
- EXTERNAL_LINK 不进入本地 Party detail。

## 9. Navigation 与 Footer

Party 与 Main 共用相同业务结构和交互的 Navigation / Footer：

- 菜单层级和内容一致；
- Footer 机构 / 联系 / 备案 / 官方标识信息一致；
- Party 使用红色主题表达；
- Main/Party 公共区域后续变化不得依靠两份手工复制内容同步。

Party-specific 的 Banner、入口页布局、内容主题和仍有真实差异的详情视觉保持 Party 自己的产品表达。

## 10. Visual / responsive behavior

中心党建公开体验应保持明确红色专题视觉，同时避免把“红色主题”扩大成第二套 CMS Domain。

要求：

- desktop 与 mobile 均可浏览；
- 不复制旧站固定宽度造成明显横向溢出；
- Banner、入口页、栏目和详情保持一致 Party branding；
- 公共 Navigation / Footer 在结构不变的前提下使用 Party theme；
- 栏目列表与分页交互保持统一主题；
- 有明确视觉 Fidelity 变更时必须经过 automated browser evidence + bounded Human Review。

## 11. Failure behavior

以下情况不得显示错误作用域内容或静默 fallback：

- 非 Party alias 请求 Party column route；
- 非 Party Article 请求 Party article route；
- Article 非公开；
- Party carousel ARTICLE 关联内容无效；
- Resource 无法公开解析；
- scoped query 失败；
- stale async response 覆盖已经变化的 Party route。

## 12. Acceptance

触达 Party 体验时按范围至少验证：

- `/party/` direct access / refresh；
- accepted Banner 可见且不可点击；
- 入口页固定内容线正确，主题教育不成为第五个固定区；
- Party 五个栏目 scope 正确；
- INTERNAL / EXTERNAL_LINK 行为；
- `PARTY_CAROUSEL` LINK / ARTICLE、open mode、图片与共享 lifecycle；
- shared Navigation / Footer 的结构、交互和 Party theme；
- shared column-page behavior与 Party canonical routes；
- Party Article scope guard；
- representative desktop / mobile viewport无明显横向溢出；
- loading / empty / error / unavailable states；
- 有视觉变化时 automated evidence 后执行 bounded Human Review。

## 13. Non-goals

- 新增 Party-specific CMS model / Admin Module；
- 独立用户 /权限体系；
- 因红色主题拆成独立 Repository / deployment；
- 把旧站 URL 当作 Runtime dependency；
- 把 legacy typeCode 暴露为新系统产品 identity；
- 从本规格恢复 Party Historical Migration 执行权限或已结束 Execution Unit。