---
id: specification-rich-text-authoring
title: 富文本内容编辑与 HTML 安全规格说明
type: specification
status: ready
version: "V1.1"
relations:
  upstream:
    - docs/requirements/rich-text-authoring.md
  related:
    - docs/technical/rich-text-authoring-research.md
    - docs/technical/rich-text-authoring-plan.md
    - docs/work/eu34-rich-text-html-safety-foundation.md
    - docs/work/eu35-shared-rich-text-authoring.md
created_at: 2026-09-05
updated_at: 2026-09-05
---

# 富文本内容编辑与 HTML 安全规格说明

## 1. Scope

本 Specification 定义 Article `INTERNAL` 与 Page `RICH_TEXT` 的统一富文本编辑、HTML 输出、安全净化、历史兼容和验证 contract。它不定义页面布局编辑器，不改变 Article / Page 的公开 URL、状态模型、资源基本 API 或 `bodyHtml` 持久化格式。

## 2. Shared Rich Text Editor Contract

### RT-01 共用 editor primitive

Admin 提供一个 CMS-local `RichTextEditor`，由 Article INTERNAL 和 Page RICH_TEXT 共同消费。它接受 HTML 初始值、输出 HTML、统一 toolbar/selection/paste/undo/redo；不持有 Article status、Column coverPolicy、Page renderMode 等消费者私有规则。图片上传/选择通过 consumer adapter 接入既有 CMS Resource 能力。

### RT-02 编辑 schema

至少支持 `p/br`、`h2/h3/h4`、`strong/b`、`em/i`、`u`、`s/strike`、`blockquote`、`ul/ol/li`、`hr`、`table/thead/tbody/tr/th/td`、`a`、`img`，以及字号、颜色、背景色、字体和对齐所需的受控 `span` / safe style。正文不产生新的页面级 H1。

### RT-03 工具能力

工具栏至少覆盖 undo/redo、段落/H2/H3/H4、bold/italic/underline/strike、列表、blockquote/hr、text align、text color、基础 font size、link set/unset、表格基本操作、image insert。自由 HTML source、script、iframe、任意 embed 不进入工具栏。

## 3. Editor Technology Contract

### RT-04 Tiptap 3.x

当前实现基线使用 Tiptap 3.x 官方 Vue 3 integration：`@tiptap/vue-3`、`@tiptap/pm` 与当前规格所需的开源 extensions。保存使用 HTML 输出，不持久化 ProseMirror JSON；工具栏继续使用项目 Vue / Element Plus UI。实施时按 Current Evidence 固定精确 package patch 到 lockfile，不使用 CDN latest。

### RT-05 图片接入

Tiptap image node 只承担 editor node，不承担上传服务。Article adapter 继续调用现有 `/api/admin/resources` 并使用 managed resource content URL。

Article 保存时：

- `bodyImageResourceIds` 只包含正文当前实际引用的 managed resource；
- 删除 editor 中图片后移除 association，但不删除资源本体；
- 新插入图片禁止 base64 data URI；
- alt 可维护；尺寸/对齐只使用 sanitizer 允许的受控表达。

Page RICH_TEXT 不在本阶段新建 Page Resource association。

## 4. Server-side HTML Safety Contract

### RT-06 单一 Backend Policy

Backend 提供一处共享 `RichTextHtmlPolicy`（代码名称可按现有风格调整），Article 与 Page 不复制 sanitizer rules。使用 OWASP Java HTML Sanitizer allow-list policy；客户端 editor schema 只承担 UX 和早期约束，不能替代该 policy。

### RT-07 write boundary

Article INTERNAL create/update 与 Page RICH_TEXT create/update 在持久化前执行服务端 sanitize + canonicalize，并持久化 sanitizer 输出。External Article 继续固定空正文；非 RICH_TEXT Page 的既有 render-mode contract 不变。

### RT-08 public legacy defense

Public Article Detail 与 Public RICH_TEXT Page 在把正文交给 `v-html` 前，必须保证正文经过同一或等价的 server-side safety policy。Public defensive sanitize 不自动回写数据库；已符合 policy 的存量正文应稳定输出。

### RT-09 forbidden content

至少移除/拒绝：`script/style/iframe/object/embed/form` 与表单控件、`on*` 事件属性、`javascript:`/`vbscript:`、未允许的 `data:` URL、可执行 SVG/MathML、meta/base/link 等 document-level control，以及 policy 未允许的 arbitrary CSS / URL-bearing style。

### RT-10 links

允许本站相对 URL 与 `http/https`；只有现有正文需求有证据时允许 `mailto`。外链不得注入 event handler、脚本协议或不安全 target；若使用 `_blank`，同时提供安全 `rel`。

### RT-11 styles

不得通过 raw `style` passthrough 接受任意 CSS。允许样式必须经 CSS policy，限于当前编辑器与历史兼容实际需要的低风险属性，例如 `font-size`、`font-family`、`color`、`background-color`、`text-align` 以及经验证的有限图片尺寸/对齐属性。

## 5. Historical Compatibility Contract

### RT-12 compatibility corpus

验证至少包含：现有 Article 简单 paragraph/strong/image HTML、Page RICH_TEXT fixture、Party canonical 中含多层 `span style`/`strong`/长中文段落的代表样本、当前可得正文图片样本以及 hostile XSS corpus。不得只用新编辑器生成 HTML 验证 sanitizer。

### RT-13 no bulk rewrite

不执行无证据的全库正文 UPDATE。存量数据由 Public read defense 获得安全边界；只有 Admin 重新保存时自然进入 write-side canonical policy。

## 6. Paste Contract

### RT-14 paste normalization

编辑器粘贴 HTML 只保留 editor schema 能表达的内容；未知标签、class、data attribute 和 unsupported styles 不原样保留。Paste cleanup 只是 UX，最终提交仍必须通过 Backend policy。

## 7. Verification Obligations

### RT-V01 Build / Type

Admin `npm run build`、Backend test/package、Public build PASS。

### RT-V02 Editor Behavior

Browser Evidence 覆盖 Article 与 Page：已有 HTML 初始化；格式、标题、列表、引用、表格、链接、图片、undo/redo；保存、重开、再次编辑；Article managed image association 增删一致。

### RT-V03 Security

Backend/API tests 直接提交 hostile HTML，至少覆盖 `<script>`、event handler、`javascript:` link、dangerous image URL、dangerous CSS、iframe/object/embed，并验证 persistence/admin read/public output 的安全结果。

### RT-V04 Bypass

不启动 Admin UI，直接调用 create/update API 的 payload 同样受 policy 保护。

### RT-V05 Legacy Compatibility

代表性 Party canonical HTML 经过 policy 后文本完整、段落/强调结构可读、允许的安全字体/字号/颜色等必要样式保留，且不存在 active content；Public Party Article regression PASS。

### RT-V06 Existing Regression

最终 Head 重新执行 Article/Page/Party/Public/Admin browser suite。旧测试若只断言被新 editor 替代的 DOM 实现细节，应按 Stale Verification Contract 修正验证层，不恢复旧 `contenteditable` 实现。

## 8. Slice Result

基于 `main@dc9fe22ef780c19ccfc9cad4bf4ab21dbfacc2d5` 的 integration-point 复核，本 Specification 由 `slice-work` 形成两个 Candidate Execution Units：

1. `EU-34 — Rich Text HTML Safety Foundation`：先建立 Backend shared policy、write sanitize、Public legacy defense 与 hostile/legacy evidence；
2. `EU-35 — Shared Rich Text Authoring`：在 EU-34 已集成的安全边界上引入 Tiptap shared editor，并完成 Article/Page integration 与 managed image association。

`EU-34` 经当前 Readiness Check PASS 为 Ready Execution Unit；`EU-35` 已有稳定 Candidate Identifier，但依赖 EU-34 完成并集成，在该依赖满足前不获得 Execute 权限。
