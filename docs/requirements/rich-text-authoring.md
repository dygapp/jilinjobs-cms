---
id: requirement-rich-text-authoring
title: 富文本内容编辑 V2 需求
type: business-requirement
status: confirmed
version: "V2.0"
classification:
  - admin-content-authoring
  - cms-resource-integration
  - compatibility
relations:
  upstream:
    - docs/requirements/information-publishing.md
    - docs/specifications/cms-core.md
    - https://github.com/dygapp/jilinjobs-cms/issues/60
  related:
    - docs/specifications/rich-text-authoring.md
    - docs/technical/rich-text-authoring-plan.md
    - docs/work/current/eu54-rich-text-v2-mature-editor-adoption.md
created_at: 2026-09-05
updated_at: 2026-09-11
---

# 富文本内容编辑 V2 需求

## 1. 目标

为 CMS 内部运营人员提供成熟、稳定、中文友好的富文本编辑能力，同时保持 `bodyHtml` 作为 Article / Page 唯一长期正文 Authority。

V2 取代此前“项目自行组合 editor schema / toolbar / paste / undo 行为，再由窄 HTML allow-list 反向约束编辑器输出”的 V1 设计。富文本编辑属于成熟通用能力，本项目只负责成熟开源编辑器集成、CMS Resource 桥接、内容兼容与 Public 输出的最低必要安全边界，不再把编辑器内核本身作为项目核心研发能力。

## 2. 适用范围

### 2.1 Rich Text consumers

本需求只覆盖：

- Article `INTERNAL` 的 `bodyHtml`；
- Page `RICH_TEXT` 的 `bodyHtml`。

两者共用同一个 CMS-local 富文本编辑器 adapter，但消费者仍保留各自业务规则。

Page Content Architecture 是独立设计线。Structured / Engineering / Embed 等特殊单页不因本需求被强制转为富文本；`就业派遣` 等需要特殊显示或交互的业务页不属于本轮富文本实现范围。

### 2.2 成熟编辑器集成

编辑器必须优先采用开源免费、维护活跃的成熟产品，并满足：

- Vue 3 / TypeScript 项目可稳定集成；
- 简体中文界面与 Windows 中文 IME 正常；
- WPS / 常见 Office HTML 粘贴可用；
- 图片、链接、表格、列表、字体、字号、颜色、对齐、undo / redo 等常规后台编辑能力；
- 能通过薄 adapter 接入 CMS Resource，而不要求项目重新实现 editor schema / selection / history / paste / table 等核心机制。

当前经过 Repository PoC 与 Human Review 接受的 Primary 为 **SunEditor 3.3.3**；Jodit 4.15.0 作为已验证 fallback。运行时不同时集成两套编辑器。

## 3. 用户体验原则

富文本编辑器面向网站管理员与内部业务人员，不面向互联网匿名用户。产品优先级为：

1. 内容生产效率和易用性；
2. 中文输入、复制粘贴和撤销/恢复行为稳定；
3. 图片、表格、字体、段落等常规网页内容的可维护性；
4. 与 CMS Resource 的清晰集成；
5. 现有合法 HTML 的语义和展示兼容。

安全策略只承担公开 HTML 输出所需的基础 active-content 防护，不得以“安全”为理由建立新的项目专属窄 HTML 方言或限制普通编辑能力。

## 4. HTML 内容契约

### 4.1 单一正文 Authority

继续只持久化 HTML `bodyHtml`。不得新增 SunEditor 内部状态、Tiptap / ProseMirror JSON、Quill Delta、Markdown 或 HTML+JSON 双写作为第二正文 Authority。

### 4.2 语义与展示优先

编辑器加载、编辑与保存可以进行成熟产品自身的 HTML canonicalization；验收不要求输入输出字节完全相同，但必须保持用户可感知的正文语义与关键展示信息，包括：

- 文本、标题、段落、强调、列表、链接；
- table / row / cell 结构；
- 图片数量、src、alt、显式 width / height 与必要对齐；
- 当前接受内容实际使用的字体、字号、颜色、背景、对齐、宽高、float 等表现；
- 后续继续编辑时内容结构稳定。

### 4.3 代表性兼容事实

至少持续保护以下 Repository-owned corpus：

- Party canonical Article 中 15 / 16 px 行内星标图片；
- Main `teacher-library` Page 中 table、cell size、200×266 image、float 等复杂 HTML；
- 普通 Article / Page 富文本；
- WPS 中文粘贴内容。

V1 `RichTextHtmlPolicy` 已证明会丢失图片 `width` / `height` 等合法表现信息，V2 不得延续该行为。

## 5. CMS Resource 边界

### 5.1 Editor adapter

编辑器内核不感知 CMS Resource ID、Article association 或 Backend domain。项目通过薄 adapter 向 editor 提供当前消费者可用的资源操作。

### 5.2 Article

本轮至少保持现有 Article managed image upload contract：

- 图片仍通过 `/api/admin/resources` 创建；
- editor 插入 managed content URL；
- Article consumer 继续维护 `bodyImageResourceIds`；
- 正文删除图片后移除 association，但不自动删除 Resource 本体；
- image width / height / align / alt 等普通编辑结果可保存并公开展示。

现有 `attachmentResourceIds` 与附件独立展示生命周期必须保持，不因编辑器替换回归。本轮不强制把附件改为正文内联 file link。

### 5.3 Page

Page `RICH_TEXT` 当前没有 Article 式 Resource association。本需求不顺带创建 Page Resource domain、资源选择 API 或数据库关系。shared editor adapter 应保持未来扩展能力，但当前 Page consumer 不伪造不存在的 resource ownership。

## 6. Public HTML 基础防护

`bodyHtml` 仍可能通过 API 或历史数据绕过编辑器，因此 Backend / Public 需要成熟 sanitizer 提供最低必要防护。

V2 的原则是 compatibility-first：

- 基于成熟 HTML sanitizer 的通用 formatting / blocks / links / images / tables / styles 能力；
- 保留当前接受内容所需的普通 HTML attributes / styles；
- 移除真正可执行或文档级控制内容，例如 script、事件属性、危险 URL scheme、iframe/object/embed/form、可执行 SVG/MathML、meta/base/link 等；
- 不依赖正则表达式处理 HTML；
- 不以编辑器 toolbar 可生成的内容作为 Backend 允许内容的上限；
- Public defensive filtering 不自动回写数据库。

## 7. 历史数据与迁移

- 不因编辑器替换批量重写 Article / Page `bodyHtml`；
- 现有内容只有在管理员实际保存时才自然进入 SunEditor canonicalization；
- 不重新激活被冻结的 Main historical migration；
- Party migration 仍保持独立 Authority；
- 已经被 V1 sanitizer 永久丢失的属性不能凭猜测自动恢复。Fresh Runtime / canonical re-import 必须证明 V2 不再造成该损失；需要保留现有 Runtime DB 时，只允许 exact-baseline bounded repair，不得覆盖 operator-diverged 内容。

## 8. Non-goals

- 自研富文本 editor core、selection、history、paste engine、table model；
- 双编辑器运行时或自动在 SunEditor / Jodit 间切换；
- Page Builder、Structured / Engineering Page、iframe / special-page renderer；
- 新建 Page Resource association；
- 全库正文批量 rewrite；
- 重新开启 Main historical migration；
- grammar / typo / sensitive-word / AI writing / automatic style adjustment；
- Microsoft Word 专项能力作为当前 Release Gate。真实 Word 粘贴可在有环境时补测，但当前 WPS + Office-shaped automated evidence 足以进入 Planning。

## 9. 验收边界

1. Article INTERNAL 与 Page RICH_TEXT 使用同一薄 `RichTextEditor` adapter，编辑器核心由成熟 SunEditor 提供；
2. 现有自组 Tiptap editor core / extensions / 自建 toolbar 被移除，不保留双实现；
3. Windows 中文 IME 正常，且 integration 不低于已完成 PoC 的人工体验；
4. WPS 中文内容可粘贴并继续编辑，一次完整粘贴应能通过一次 Undo 整体回退；
5. Party 15 / 16 px 星标与 `teacher-library` 复杂 HTML 的关键语义/展示经 editor + Backend policy + Public render 后保持；
6. Article managed image upload / association 一致，image width / height / align / alt 等常规表现可维护；
7. 现有 Article attachment lifecycle 不回归；
8. Backend API 绕过 editor 时，active content 不能成为可执行 Public 输出；
9. Public renderer 不依赖 SunEditor 编辑器 UI/chrome CSS 才能正确展示标准正文语义；
10. 不新增第二正文 Authority，不扩展 Page Resource domain，不改变非 RICH_TEXT Page contract；
11. full Backend / Admin / Public / Integrated Browser regression PASS。
