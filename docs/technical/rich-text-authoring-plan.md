---
id: technical-plan-rich-text-authoring
title: 富文本编辑与 HTML 安全技术计划
type: technical-plan
status: ready
version: "V1.0"
relations:
  upstream:
    - docs/requirements/rich-text-authoring.md
    - docs/specifications/rich-text-authoring.md
    - docs/technical/rich-text-authoring-research.md
  execution_units:
    - docs/work/eu34-rich-text-html-safety-foundation.md
    - docs/work/eu35-shared-rich-text-authoring.md
created_at: 2026-09-05
updated_at: 2026-09-05
---

# 富文本编辑与 HTML 安全技术计划

## 1. Why this Technical Plan exists

B3 在 `slice-work` 后形成两个 Execution Unit，二者共享 `bodyHtml`、允许 HTML 子集、历史兼容 corpus 与验证边界，并存在明确先后依赖。该跨 Unit HOW 协调具有持续价值，因此需要持久化 Technical Plan。

## 2. Unit Sequence

### EU-34 — Rich Text HTML Safety Foundation

先建立服务端安全边界：

- 引入并锁定 OWASP Java HTML Sanitizer；
- 建立共享 `RichTextHtmlPolicy`；
- Article INTERNAL / Page RICH_TEXT write boundary sanitize + canonicalize；
- Public Article / RICH_TEXT Page legacy defensive sanitize；
- hostile API corpus 与 Party canonical compatibility corpus；
- 不改 Admin editor，不改 DB schema。

### EU-35 — Shared Rich Text Authoring

EU-34 集成后再升级 authoring：

- 引入并锁定 Tiptap 3.x Vue 3 packages / extensions；
- 建立 CMS-local `RichTextEditor`；
- Article / Page 共同消费；
- Article 保持 managed Resource upload / `bodyImageResourceIds` association；
- Page 不新增 Resource domain；
- 完成 editor Browser Evidence 与 legacy reopen/save evidence。

依赖：`EU-35 -> EU-34 integrated main`。

## 3. Cross-unit Contracts

### 3.1 Persistence

`bodyHtml` 始终是唯一长期正文 Authority。任何 Unit 都不得引入 JSON/Delta/Markdown 双写。

### 3.2 HTML schema alignment

EU-35 editor 能生成的标签、属性、URL 和 style 必须是 EU-34 `RichTextHtmlPolicy` 明确允许的子集。若 editor 需要新增表达，先更新安全 policy + tests，再开放对应 toolbar/node；不得让客户端输出依赖 sanitizer “碰运气保留”。

### 3.3 Historical content

两 Unit 共用代表性 Party canonical / Article / Page compatibility corpus。EU-34 证明 Public safety 与 history preservation；EU-35 追加“新 editor 打开并保存 legacy HTML”证据。

### 3.4 Article managed images

现有 Resource API、content URL 与 `bodyImageResourceIds` 是稳定 contract。EU-34 只允许安全 URL；EU-35 负责 editor 插入/删除后的 association 一致性。移除引用不自动删除 Resource。

## 4. Dependency / Version Strategy

- Backend sanitizer：`com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20260313.1`，由 Gradle 固定；
- Admin editor：Research Current Evidence 为 Tiptap `3.31.2`；EU-35 Execute 开始时再次核验 current patch，并由 `package-lock.json` 固定；
- 不使用 CDN latest；
- 不新增数据库迁移；
- 不引入 GPL/commercial editor dependency。

## 5. Verification Strategy

EU-34：Backend tests + direct API hostile cases + Public read safety + Party canonical compatibility + full CI/browser regression。

EU-35：Admin build/type + targeted Article/Page editor Browser tests + managed image association + save/reopen + full CI/browser regression；若旧 E2E 只绑定旧 `contenteditable` DOM，则按 Stale Verification Contract 修正验证层。

每个 Unit 的 Evidence 必须对应其精确 PR Head；EU-35 不继承 EU-34 的 implementation evidence，只继承已集成的安全 contract。

## 6. Exit

B3 只有 EU-34 与 EU-35 都完成并集成后才可在 Issue #60 标记为完成。EU-34 单独完成只表示 HTML safety foundation 已收敛，不等于富文本编辑升级完成。
