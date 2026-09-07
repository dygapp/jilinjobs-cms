---
id: technical-research-rich-text-authoring
title: 富文本编辑器与 HTML 安全技术研究
type: technical-research
status: accepted
version: "V1.1"
relations:
  upstream:
    - docs/requirements/rich-text-authoring.md
    - docs/specifications/rich-text-authoring.md
    - https://github.com/dygapp/jilinjobs-cms/issues/60
  related:
    - docs/technical/rich-text-authoring-plan.md
created_at: 2026-09-05
updated_at: 2026-09-05
---

# 富文本编辑器与 HTML 安全技术研究

## 1. Research Scope / Baseline

本研究为 Issue #60 / B3 提供当前技术输入。最新 integration-point 复核基线为：

`main@dc9fe22ef780c19ccfc9cad4bf4ab21dbfacc2d5`

PR #65 / EU-33 已合并，Article / Page Admin View 的提示与标签收敛已进入该基线。

## 2. Current Consumer Evidence

### 2.1 Admin Article

`ArticleManagementView.vue` 使用手写 `contenteditable`，通过 `editorRef.innerHTML` 载入/保存 HTML，并使用 `document.execCommand` 提供 bold/italic。正文图片调用既有 `/api/admin/resources` 上传，以 managed Resource content URL 写入正文；`bodyImageResourceIds` 会按正文当前仍引用的 Resource URL 收敛。

### 2.2 Admin Page

`PageManagementView.vue` 的 `RICH_TEXT` 同样使用另一套手写 `contenteditable + innerHTML + document.execCommand`。因此 Article 与 Page 当前具有同一 `bodyHtml` 业务概念，却复制 editor lifecycle / toolbar / DOM synchronization 责任；这是 shared editor primitive 的真实复用证据。

### 2.3 Public Rendering / Backend

Main Public Article 使用 `v-html` 渲染 `publicBodyHtml(article)`；该 helper 当前只改写 Resource URL。Main Public Page 对 RICH_TEXT 正文直接 `v-html`。Backend `ArticleService` / `PageService` 当前均不净化 `bodyHtml`，Public DTO 直接返回保存值。

结论：当前存在 stored HTML / XSS safety gap。第三方 editor schema、toolbar 或 paste cleanup 不能替代 Backend security boundary。

## 3. Historical Content Evidence

Party canonical 代表正文包含多层 `p/span/strong`，以及 `font-size`、`font-family`、`color`、`background-color` 等已接受展示语义。新的 safety policy 必须区分 active/executable markup、当前 authoring 需要的低风险 presentational markup 和历史内容需要保留的安全样式；不能简单删除全部 inline style。

## 4. Rich-text Editor Candidates

### 4.1 Tiptap 3.x — Selected

2026-09-05 Current Evidence：`@tiptap/vue-3` latest = `3.31.2`，MIT。选择理由：

- 官方 Vue 3 integration；
- headless，可继续使用 Element Plus 工具栏；
- ProseMirror schema 可显式限制编辑结构；
- HTML 初始内容与 HTML 输出匹配当前 `bodyHtml` contract；
- Image node 与上传服务解耦，适合复用 CMS Resource API；
- 无需 Cloud / Pro / Collaboration。

代价：legacy HTML 进入 schema 时可能规范化；font size/color/table/image 等需要明确 extensions 与 HTML attribute contract。这些风险由 compatibility corpus 与 Browser Evidence 管理。

### 4.2 Quill 2.0.3 — Fallback

BSD-3-Clause，常规能力成熟，但核心以 Delta 为中心，且当前 Consumer 长期 Authority 是 HTML。若发挥其 Delta 优势会引入不必要的第二正文 Authority，因此仅保留为 fallback。

### 4.3 CKEditor 5 / TinyMCE 8 — Not Selected

当前 self-host open-source 路径会带来 GPL 分发义务，商业路径需要单独 license；当前 Consumer 没有对应项目级授权，因此普通 UI Slice 不应隐式引入该 licensing 决策。

## 5. Server-side Sanitizer Candidates

### 5.1 OWASP Java HTML Sanitizer — Selected

2026-09-05 Maven Central Current Evidence：

`com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20260313.1`

License：Apache-2.0。

它专门用于把不可信 HTML 转为可安全嵌入应用的 HTML，支持显式 allow-list `PolicyFactory` / `HtmlPolicyBuilder` 组合，适合当前需要同时控制元素、属性、协议和有限 CSS 的边界。

安全责任仍属于 Consumer-local policy：不能机械启用全部 styling，也不能 raw `allowAttributes("style")`。

### 5.2 jsoup — Not Selected as Primary Sanitizer

jsoup parser / Safelist 适合 DOM inspection 与简单 allow-list，但当前需要安全保留有限 inline style；若自行实现完整 style declaration filter 会重复 OWASP sanitizer 已承担的责任。因此不作为当前主 sanitizer。

## 6. Selected Architecture

### Admin

```text
ArticleManagementView ─┐
                       ├─ RichTextEditor ─ Tiptap
PageManagementView ────┘
```

Shared primitive 持有 editor lifecycle / toolbar / schema / paste；consumer 保留自己的业务表单状态。Article Resource 上传/关联由薄 adapter 保持；Page 不自动获得新 Resource domain。

### Backend

```text
Admin/API payload
      |
      v
RichTextHtmlPolicy --sanitize/canonicalize--> persistence
      |
      +-- defensive sanitize on Public legacy read
```

Article / Page 共用一处 policy；Public defensive sanitize 覆盖 sanitizer 引入前的存量正文，不执行读时回写。

### Persistence

继续只有 `bodyHtml` 一个长期正文 Authority；Tiptap / ProseMirror JSON 只存在于 editor runtime。

## 7. Allowed Markup Strategy

1. 从 Requirement 的编辑能力确定新内容 schema；
2. 从 Party canonical、Page、Article fixtures 提取真实 legacy markup；
3. 取二者安全并集；
4. 分别约束 element / attribute / URL / CSS property-value；
5. hostile corpus 与 legacy corpus 同时验证。

当前已有证据支持保留 `font-size / font-family / color / background-color / text-align`；图片尺寸/对齐只在 editor 真实实现确定后进入有限 policy。不得因历史兼容接受 arbitrary `style/class/data-*` 或 event attribute。

## 8. Risks / Updated Integration Facts

- **Stored HTML**：只做 write sanitizer 不足，Public legacy defense 必须存在；
- **Legacy parsing**：旧正文首次由新 editor 保存会经过 editor schema + Backend policy 两次 canonicalization，必须用真实 corpus 验证；
- **CSS**：policy 太严会损失已接受展示语义，太松会留下 CSS/URL attack surface；
- **Managed images**：Article association 必须与正文实际 managed Resource URL 一致；
- **PR #65 overlap 已解除**：PR #65 已合并为 `main@dc9fe22e...`，Article/Page integration points 已在该基线上重新确认；
- **Licensing**：不引入需要新 GPL/commercial 项目决策的 editor。

## 9. Research Outcome

B3 的技术输入已经足够稳定：Tiptap 3.x + OWASP Java HTML Sanitizer；`bodyHtml` 单一 Authority；write sanitize + Public legacy defense；no bulk rewrite；no Page Builder。由于 Backend safety 与 Admin authoring 是可独立验证且具有明确依赖的两个责任域，后续按 `docs/technical/rich-text-authoring-plan.md` 分为 EU-34 与 EU-35。
