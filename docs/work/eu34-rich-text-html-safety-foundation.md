# EU-34 — Rich Text HTML Safety Foundation

## 1. Identity

- Identifier：`EU-34`
- Source Candidate：GitHub Issue #60 / B3
- Requirement：`docs/requirements/rich-text-authoring.md`
- Specification：`docs/specifications/rich-text-authoring.md`
- Technical Plan：`docs/technical/rich-text-authoring-plan.md`
- Status：**READY**

本 Unit 由 Ready Specification 经 `slice-work` 形成 Candidate，并在下述 Readiness Gate PASS 后晋升为 Ready Execution Unit。

## 2. Intent

在引入更强的富文本 authoring 之前，先把当前 Article / Page `bodyHtml` 的服务端安全边界补齐，同时保护现有 Party canonical 历史正文的安全展示语义。

## 3. Scope

- Backend 引入 OWASP Java HTML Sanitizer；
- 建立 Article/Page 共用 `RichTextHtmlPolicy`；
- Article INTERNAL create/update 持久化前 sanitize/canonicalize；
- Page RICH_TEXT create/update 持久化前 sanitize/canonicalize；
- Public Article / Public RICH_TEXT Page 返回正文前执行 defensive sanitize，覆盖 legacy stored HTML；
- 建立 hostile HTML tests 与 representative legacy compatibility tests；
- 执行现有 Backend / Public / Admin / Party regression。

## 4. Explicit Non-goals

- 不引入 Tiptap；
- 不修改 Article/Page Admin editor UI；
- 不修改 DB schema / Flyway；
- 不批量重写历史正文；
- 不建立 Page Resource association；
- 不引入 Page Builder/source editor/embed/collaboration。

## 5. Implementation Responsibility

Backend shared policy 应位于 CMS content/page 可共同消费且不形成循环依赖的位置；ArticleService / PageService 只调用共享 policy，不复制 rules。

Public output 使用同一或等价安全 policy；read-side defense 不回写数据库。

Tests 至少覆盖：script、event handler、javascript URL、dangerous image URL、dangerous CSS、iframe/object/embed、允许的安全格式，以及 Party canonical 代表样本。

## 6. Acceptance Mapping

| Spec | EU-34 Responsibility |
|---|---|
| RT-06 | shared Backend HTML policy |
| RT-07 | Article/Page write sanitize |
| RT-08 | Public legacy defensive sanitize |
| RT-09～11 | forbidden tags/attrs/protocols + controlled CSS |
| RT-12～13 | hostile/legacy corpus + no bulk rewrite |
| RT-V01 / V03 / V04 / V05 / V06 | build, API bypass, legacy/public/regression evidence |

RT-01～05、RT-14 的 Admin authoring 责任留给 EU-35。

## 7. Readiness Gate

### Authority / WHAT / WHY — PASS

Requirement / Specification 已 confirmed/ready，stored-XSS gap、历史兼容和 non-goals 均由当前 repository/runtime evidence 支持。

### Technical Planning — PASS

跨 Unit HOW 已固化在 `rich-text-authoring-plan.md`；本 Unit 只承担 server safety foundation。

### Dependency / Unknowns — PASS

- 基线：`main@dc9fe22ef780c19ccfc9cad4bf4ab21dbfacc2d5`；
- PR #65 已集成，且本 Unit 不修改其 Admin View 文件；
- sanitizer artifact/version/licensing 已取得 Current Evidence；
- 不需要新的 Product Intent、DB migration 或外部凭据。

### Verification Completeness — PASS

安全 claims 有 direct API/backend evidence，历史兼容有 canonical corpus，Public safety 有 read-boundary evidence，整体 regression 由 full CI/browser suite 闭环。

## 8. Readiness Verdict

**PASS — EU-34 is a Ready Execution Unit.**

可以进入 Execute。若实施发现必须扩大允许 HTML/CSS 到当前 Requirement 未定义的 active/embed 能力，应回到 Requirement Change，而不是在本 Unit 内扩大范围。

## 9. Completion Gate

- sanitizer dependency 与 shared policy 已锁定；
- hostile direct API cases PASS；
- Article/Page write-side canonicalization PASS；
- Public legacy defense PASS；
- Party canonical representative compatibility PASS；
- Backend/Admin/Public/Integrated Browser regression PASS；
- PR 合并后 main Post-Integration CI PASS。
