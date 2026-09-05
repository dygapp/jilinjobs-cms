# EU-35 — Shared Rich Text Authoring

## 1. Identity

- Identifier：`EU-35`
- Source Candidate：GitHub Issue #60 / B3
- Requirement：`docs/requirements/rich-text-authoring.md`
- Specification：`docs/specifications/rich-text-authoring.md`
- Technical Plan：`docs/technical/rich-text-authoring-plan.md`
- Dependency：EU-34 已完成、集成并通过 Post-Integration CI
- Status：**READY — Readiness Check PASS**

Identifier 只承担稳定追踪。本 Unit 已由 `slice-work` 形成 Candidate，并在 EU-34 完成后以新的 `main@e429631126ce8449f9939ed5d2edbeec7d9853cd` 重新执行 Readiness Check；所有 Gate 均已 PASS，因此获得 Execute 权限。

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

Requirement / Specification / Technical Plan 已明确；Unit 边界保持为 Admin shared authoring，不扩展 DB、Page Resource domain、页面构建器或服务端安全策略。

### Dependency — PASS

- EU-34 PR #67 已合并；
- integration commit：`e429631126ce8449f9939ed5d2edbeec7d9853cd`；
- main Post-Integration CI #697 / run `33968363838`：Backend、Public、Admin、Integrated Browser 全部 PASS。

### Sanitizer / Editor Schema Alignment — PASS

当前 `RichTextHtmlPolicy` 已允许本 Unit 所需的 H2～H4、strong/em/u/strike、列表、blockquote/hr、link、image、table，以及 `font-size` / `font-family` / `color` / `background-color` / `text-align` 受控 style。EU-35 editor 输出保持为该 allow-list 的子集，不要求放宽服务端 policy。

### Article / Page Integration Points — PASS

在 `main@e4296311...` 重新核对：

- Article INTERNAL 仍由 `ArticleManagementView.vue` 管理 `bodyHtml`、CMS Resource upload 与 `bodyImageResourceIds`；
- Page RICH_TEXT 仍由 `PageManagementView.vue` 管理 `bodyHtml`，且没有 Page Resource association；
- 两者当前仍各自使用 `contenteditable + execCommand`，正是本 Unit 的替换边界。

### Technology / Version / Verification — PASS

- 使用 Tiptap 3.x 官方 Vue 3 integration；
- Execute 开始时 Current Evidence 指向 `3.31.2` patch line；实现固定精确版本，并生成 `package-lock.json`；
- 使用 `StarterKit` + `TableKit` + `TextStyleKit` + Image + TextAlign 等规格所需开源 extension；
- Tiptap 只负责 editor schema/commands；服务端 OWASP policy 继续承担最终安全 Authority；
- Article/Page targeted Browser、managed image association、legacy open/save/reopen 与 full regression 已有明确验证路径。

### Readiness Result — PASS

所有 Promotion Condition 已满足。`EU-35` 现为 Ready Execution Unit，可进入 Execute。

## 7. Execution Notes

1. 建立 CMS-local `RichTextEditor`，消费者通过 `v-model` 交换 HTML；
2. Article 通过 image adapter 复用现有 `/api/admin/resources`，插入 managed content URL，并继续由 Article consumer 维护 association；
3. Page RICH_TEXT 不新增上传资源模型，仅允许安全 URL image insert；
4. editor schema 不提供 H1、raw source、script/iframe/embed；
5. paste 由 Tiptap schema normalization 收敛未知标签/属性/style，最终提交继续经过 EU-34 Backend policy；
6. 旧测试若绑定旧 `contenteditable` DOM，只更新验证契约，不恢复旧实现。

## 8. Completion Gate

- Article/Page 均消费同一 shared editor；
- RT-02/RT-03 能力 Browser Evidence PASS；
- Article managed image association 增删一致；
- save/reopen 与 legacy compatibility PASS；
- direct security contract 不因 editor 引入退化；
- full CI/browser regression PASS；
- PR 合并后 main Post-Integration CI PASS；
- Issue #60 / B3 只有在 EU-34 与 EU-35 都完成后才标记完成。
