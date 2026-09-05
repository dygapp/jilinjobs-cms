---
id: requirement-rich-text-authoring
title: 富文本内容编辑与 HTML 安全需求
type: business-requirement
status: confirmed
version: "V1.1"
classification:
  - admin-content-authoring
  - security
relations:
  upstream:
    - docs/requirements/information-publishing.md
    - docs/specifications/cms-core.md
    - https://github.com/dygapp/jilinjobs-cms/issues/60
  related:
    - docs/specifications/rich-text-authoring.md
    - docs/technical/rich-text-authoring-research.md
    - docs/technical/rich-text-authoring-plan.md
created_at: 2026-09-05
updated_at: 2026-09-05
---

# 富文本内容编辑与 HTML 安全需求

## 1. 目标

在保持现有 CMS `bodyHtml` 内容契约、Article / Page 模型和公开页面职责不变的前提下，为运营人员提供可持续维护的常规富文本编辑能力，并建立与公开 `v-html` 渲染相匹配的服务端 HTML 安全边界。

本需求来源于 Issue #60 / B3。它不是 Page Builder，也不引入新的内容模型、审核流程或多站点能力。

## 2. 当前问题

当前 Article `INTERNAL` 与 Page `RICH_TEXT` 都使用各自 View 内的手写 `contenteditable + innerHTML + document.execCommand` 编辑区，只提供极少量格式能力；Article 另有正文图片上传和 `bodyImageResourceIds` 关联逻辑。两处具有同一 `bodyHtml` 业务概念，却维护两套相互独立的编辑器实现。

Article / Page Backend 当前会直接保存管理端提交的 `bodyHtml`；公开 API 直接返回正文，Main Public Article / Page 使用 Vue `v-html` 渲染；Backend 没有统一 HTML sanitizer。因此客户端编辑器不能被视为安全边界，直接 API 调用和 sanitizer 引入前的历史内容均可能绕过客户端限制。

Party canonical 历史文章已包含大量安全且有展示价值的 inline style，例如字号、字体、文字颜色和背景色。安全收敛不得通过简单删除全部 style 破坏已接受历史正文。

## 3. In Scope

### 3.1 统一富文本编辑能力

Article `INTERNAL` 正文与 Page `RICH_TEXT` 正文使用同一 CMS-local 可复用编辑能力和同一 HTML 内容契约。第一阶段至少覆盖：

- 段落与 H2/H3/H4；
- 加粗、斜体、下划线、删除线；
- 基础文字颜色、字号与对齐；
- 有序列表、无序列表；
- 引用、分隔线；
- 表格；
- 链接；
- 图片插入、alt、受控尺寸/对齐；
- 粘贴清理；
- undo / redo；
- 现有 HTML 的加载、保存和重新打开。

Article 正文图片继续复用现有 CMS Resource API；Page 不因共用编辑器自动获得新的 Resource domain。

### 3.2 HTML 安全边界

- `bodyHtml` 一律视为不可信输入；
- Article / Page create/update 不能只依赖浏览器 editor schema 或前端过滤；
- Backend 必须使用共享、显式 allow-list policy 执行 HTML sanitization / normalization；
- Public Article / Page 输出必须对 sanitizer 引入前的存量正文提供服务端 defensive sanitization；
- 禁止当前正文不需要的 active content，包括 `script/style/iframe/object/embed/form`、事件属性、脚本协议、未授权 `data:`、可执行 SVG/MathML 等；
- URL 与 CSS 只允许当前内容模型和历史兼容实际需要的安全子集；
- 不以正则表达式替代 HTML sanitizer。

### 3.3 历史内容兼容

- 不批量 destructive rewrite 现有 Article / Page / Party canonical 正文；
- sanitizer policy 必须使用当前仓库代表性历史正文建立兼容语料；
- 安全的文本结构、段落、强调和必要低风险 inline style 应尽量保留；
- legacy unsafe markup 可在 Public 输出时移除或规范化，但读时不自动回写数据库；
- 历史记录由 Admin 重新保存后可自然进入新的 canonical HTML policy。

## 4. 内容契约

### 4.1 单一正文 Authority

继续只使用 HTML `bodyHtml` 作为 Article / Page 的持久化与 API contract。本阶段不引入 Tiptap / ProseMirror JSON、Quill Delta、Markdown 或 HTML+JSON 双写作为第二正文 Authority。

### 4.2 Article Resource 关系

Article 正文图片继续使用现有 CMS Resource 上传/读取能力。保存时 `bodyImageResourceIds` 必须与正文中仍实际引用的 managed resource 一致；移除正文引用只移除 association，不自动删除 Resource 本体；不允许把任意 base64 data URI 作为新的长期正文图片来源。

### 4.3 Page 图片边界

Page 当前没有 Article 式正文资源关联模型。本需求不顺带新建 Page Resource association。Page RICH_TEXT 的图片能力只能使用当前长期可解析的既有资源边界；若未来需要 Page 专属资源关系，需另行形成 Requirement / Specification。

## 5. 用户体验原则

- 编辑器面向运营人员，不暴露 Tiptap / ProseMirror / HTML schema 技术概念；
- 工具栏只提供当前正文模型允许的格式；
- 不提供自由 HTML source mode 绕过安全策略；
- 粘贴外部内容时保留必要语义，清除不受支持结构和危险属性；
- 编辑区与公开 `.rich-content` 的正文语义应接近，但本阶段不构建完整页面预览器。

## 6. Non-goals

- 可视化 Page Builder / 任意布局组件；
- HTML source editor；
- iframe / script / arbitrary embed；
- 实时协作、修订轨迹、AI 写作；
- Word/PDF import/export；
- Page 新建独立 Resource association 模型；
- 对全部历史正文的一次性数据库重写；
- 因编辑器选型改变 Article / Page / Public API 基本模型。

## 7. 验收边界

1. Article INTERNAL 与 Page RICH_TEXT 使用同一富文本 editor primitive；
2. 规格要求的常规格式、表格、链接、图片、undo/redo 可实际编辑并保存；
3. Article 正文图片仍通过现有 Resource API 建立并维护正文资源关联；
4. 保存后重新打开能够恢复受支持正文结构；
5. Public Article / Page 正确展示受支持 HTML；
6. 直接向 Admin API 提交 hostile HTML 时，危险标签、属性、协议和 CSS 不会进入可执行 Public 输出；
7. 绕过客户端编辑器不能绕过 Backend sanitizer；
8. 代表性 Party canonical 正文经过 policy 后保持文本、段落、强调和必要安全样式，同时不存在 active content；
9. 不新增第二正文 Authority；
10. 现有 Article / Page / Party / Public / Admin regression 和当前 migration runtime 不发生无关退化。
