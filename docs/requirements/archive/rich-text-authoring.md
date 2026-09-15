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
    - docs/work/archive/eu54-rich-text-v2-mature-editor-adoption.md
created_at: 2026-09-05
updated_at: 2026-09-13
---

# 富文本内容编辑 V2 需求

## 状态

- Requirement：**CURRENT / ACCEPTED**；
- EU-54 — Rich Text V2 Mature Editor Adoption：**COMPLETED**；
- Work artifact：`docs/work/archive/eu54-rich-text-v2-mature-editor-adoption.md`；
- Execute Authority：**TERMINATED**。

本文保留 EU-54 完成后的长期富文本 contract，不拥有 Current Execution Gate。

## 1. 目标

为 CMS 内部运营人员提供成熟、稳定、中文友好的富文本编辑能力，同时保持 `bodyHtml` 作为 Article / Page Rich Text 的唯一长期正文 Authority。

V2 不再自研 editor schema / toolbar / selection / history / paste / table 等成熟通用能力。项目职责限定为成熟开源编辑器集成、CMS Resource bridge、现有内容兼容与 Public 输出的最低必要 active-content 防护。

## 2. 适用范围

只覆盖：

- Article `INTERNAL` 的 `bodyHtml`；
- Page `RICH_TEXT` 的 `bodyHtml`；
- Structured Page 中 schema 明确允许的 item-level Rich Text body，仍复用同一 Rich Text capability。

Page Content Architecture 独立拥有 `contentModel / rendererKey / contentOwner / structuredPayload`。Rich Text V2 不把 Structured / Engineering / External Page 强制改造成 whole-page Rich HTML。

## 3. Current editor choice

Current Primary：**SunEditor 3.3.3**。

- Vue 3 / TypeScript 通过 CMS-local thin adapter 集成；
- 使用简体中文 `zh_cn`；
- 支持 Windows 中文 IME；
- 支持 WPS / 常见 Office-shaped paste；
- 使用成熟 editor 自带 undo/redo、table、image、link、font、size、color、alignment 等常规能力；
- Runtime 不同时集成两套 editor。

Jodit 4.15.0 只保留为已验证 fallback evidence；只有 SunEditor 命中明确 Stop Condition 并形成新的 Repository / Human Authority，才允许切换。

## 4. 单一 HTML Authority

Admin、API、DB 与 Public 对 Rich Text 继续只持久化 `bodyHtml`。

不得新增 SunEditor internal state、Tiptap / ProseMirror JSON、Quill Delta、Markdown 或 HTML + JSON 双写作为第二正文 Authority。

Editor canonicalization 可以改变等价 HTML bytes，但必须保持用户可感知语义与 accepted presentation：

- paragraphs / headings / emphasis / lists / links；
- table / row / cell / colspan / rowspan；
- image `src / alt / title / width / height` 与必要 alignment；
- accepted font / size / color / background / width / height / float 等表现；
- 保存后重新打开仍可稳定继续编辑。

## 5. Repository compatibility corpus

至少持续保护：

- Party canonical Article 中 15 / 16 px inline star images；
- Main `teacher-library` Page 的 table、cell size、200×266 image、float 等复杂 HTML；
- ordinary Article / Page Rich Text；
- WPS 中文 paste / undo behavior。

V1 窄 `RichTextHtmlPolicy` 曾丢失 image width / height 等合法 presentation；Current V2 不得恢复该行为。

## 6. CMS Resource boundary

### Article

继续复用 `/api/admin/resources` managed image contract：

1. consumer / adapter 上传 image；
2. editor 插入 managed content URL；
3. Article consumer 维护 `bodyImageResourceIds`；
4. save 前按正文实际引用 reconcile association；
5. 删除正文引用不自动删除 Resource 本体；
6. width / height / align / alt 等普通编辑结果可持久化并公开展示。

`attachmentResourceIds` 的独立 lifecycle 保持，不因 editor 更换被强制改造成正文内联附件。

### Page

Page `RICH_TEXT` 当前没有 Article 式 managed Resource association。本需求不创建新的 Page Resource domain / API / DB relation。

Structured Page item body 若使用 Rich Text，仍必须遵循其当前 Page Content Architecture 与 Resource contract，不从本文隐式扩展 ownership。

## 7. HTML safety

Backend / Public 使用成熟 parser-based sanitizer 提供 compatibility-first 防护：

允许标准 CMS formatting / blocks / links / images / tables / accepted styles，同时移除真正 active / document-level content，例如：

- `script`；
- event handler；
- dangerous URL scheme；
- iframe/object/embed/form（除非未来独立 integration Authority 明确允许）；
- executable SVG/MathML；
- meta/base/link 等 document-level controls；
- dangerous CSS/URL behavior。

不得通过 regex 解析 HTML，也不得把 editor toolbar 能生成的内容当作 Backend allow-list 上限。

Public defensive filtering 不自动回写 DB。

## 8. 历史数据

- 不因 editor replacement 批量重写全部 Article / Page `bodyHtml`；
- 管理员实际保存时允许进入 SunEditor canonicalization；
- 不重新激活 Main Historical Migration；
- Party Migration 保持独立 Authority；
- 对已被 V1 sanitizer 永久丢失的信息不得猜测恢复；需要保留特定旧 Runtime DB 时，只允许 exact-baseline bounded repair，并保护 operator divergence。

## 9. Verification

后续触达 Rich Text capability 时至少覆盖：

- `suneditor@3.3.3` lock / Admin build；
- Article + Page 共用一个 thin `RichTextEditor` adapter；
- 中文 IME；
- WPS paste + single-operation Undo；
- common formatting / table / image / link / undo / redo；
- Article managed image association；
- attachment regression；
- Party inline-star / teacher-library real corpus；
- hostile direct API payload active-content filtering；
- Public render 不依赖 SunEditor editor chrome CSS；
- Backend / Admin / Public / Integrated Browser regression。

真实 Microsoft Word paste 只有在具备环境时补充 evidence；不得伪造 PASS。

## 10. 非目标

- 自研 editor core / selection / history / paste / table engine；
- 双 editor Runtime；
- 通用 Page Builder；
- Page Resource domain；
- full-database body rewrite；
- Main Historical Migration reactivation；
- grammar / typo / sensitive-word / AI writing；
- 从本文恢复 EU-54 Execute Authority。
