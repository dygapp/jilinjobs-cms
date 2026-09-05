# EU-35 — Shared Rich Text Authoring

## 1. Identity

- Identifier：`EU-35`
- Source Candidate：GitHub Issue #60 / B3
- Requirement：`docs/requirements/rich-text-authoring.md`
- Specification：`docs/specifications/rich-text-authoring.md`
- Technical Plan：`docs/technical/rich-text-authoring-plan.md`
- Dependency：EU-34 已完成并集成到 `main`
- Status：**CANDIDATE — dependency not yet satisfied**

Identifier 只承担稳定追踪。当前 Unit 已由 `slice-work` 形成 Candidate，但在 EU-34 完成并以最新 `main` 重新执行 Readiness Check 前，不授予 Execute 权限。

## 2. Intent

在服务端 HTML safety foundation 已稳定后，用一个 CMS-local Tiptap `RichTextEditor` 替换 Article / Page 各自的手写 `contenteditable + execCommand`，提供规格定义的常规 authoring 能力，同时保持现有 `bodyHtml` 和 Article Resource contract。

## 3. Scope

- Admin 引入并锁定 Tiptap Vue 3 packages / 当前规格所需 extensions；
- 建立 shared `RichTextEditor` primitive；
- Article INTERNAL / Page RICH_TEXT 共同消费；
- toolbar 覆盖 RT-02/RT-03；
- paste normalization、undo/redo、existing HTML load/save；
- Article 图片继续复用 CMS Resource API，维护 `bodyImageResourceIds`；
- Article/Page targeted Browser E2E；
- 验证 legacy HTML 打开、保存、重开；
- full Admin/Public/Backend/Party regression。

## 4. Explicit Non-goals

- 不改变 `bodyHtml` persistence/API contract；
- 不建立 Tiptap JSON/Delta/Markdown 第二 Authority；
- 不建立 Page Resource association；
- 不引入 Page Builder、source editor、iframe/script embed、collaboration；
- 不放宽 EU-34 已接受的 HTML safety policy 以迁就 editor 输出。

## 5. Acceptance Mapping

| Spec | EU-35 Responsibility |
|---|---|
| RT-01～03 | shared editor + schema + toolbar |
| RT-04～05 | Tiptap integration + managed image adapter |
| RT-14 | paste normalization |
| RT-V01 / V02 / V06 | build/type, editor Browser, full regression |
| RT-V05（追加） | legacy HTML editor open/save/reopen compatibility |

EU-34 已承担 RT-06～13 的 server safety foundation；EU-35 必须让 editor 输出成为该 policy 的受支持子集。

## 6. Current Readiness Check

### Authority / Scope — PASS

Requirement / Specification / Technical Plan 已明确，Article/Page integration points 已在 `main@dc9fe22e...` 重新确认。

### Technology / Verification — PASS

Tiptap Vue 3 当前技术输入与验证责任已明确，Article Resource adapter 边界也已有现有实现证据。

### Dependency — NOT YET PASS

EU-35 依赖 EU-34 的 shared server policy 已完成并集成；当前该条件尚未满足。因此本 Unit 暂不晋升 Ready，不进入 Execute。

## 7. Promotion Condition

EU-34 合并且 Post-Integration Evidence PASS 后：

1. 在新的 `main` 上核对 sanitizer/editor schema alignment；
2. 再次核对 Article/Page integration points；
3. 确认 Tiptap current patch 并锁定 lockfile；
4. 重新执行 Readiness Check。

全部 PASS 后，EU-35 才成为 Ready Execution Unit。

## 8. Completion Gate

- Article/Page 均消费同一 shared editor；
- RT-02/RT-03 能力 Browser Evidence PASS；
- Article managed image association 增删一致；
- save/reopen 与 legacy compatibility PASS；
- direct security contract 不因 editor 引入退化；
- full CI/browser regression PASS；
- PR 合并后 main Post-Integration CI PASS；
- Issue #60 / B3 只有在 EU-34 与 EU-35 都完成后才标记完成。
