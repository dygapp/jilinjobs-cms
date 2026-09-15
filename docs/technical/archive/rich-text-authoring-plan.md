---
id: technical-plan-rich-text-authoring
title: 富文本内容编辑 V2 技术方案
type: technical-plan
status: accepted
version: "V2.0"
relations:
  upstream:
    - docs/requirements/rich-text-authoring.md
    - docs/specifications/rich-text-authoring.md
    - https://github.com/dygapp/jilinjobs-cms/issues/60
  evidence:
    - https://github.com/dygapp/jilinjobs-cms/pull/135
  execution_units:
    - docs/work/archive/eu54-rich-text-v2-mature-editor-adoption.md
created_at: 2026-09-05
updated_at: 2026-09-13
---

# 富文本内容编辑 V2 技术方案

## 状态

- Technical Authority：**CURRENT / ACCEPTED**；
- EU-54：**COMPLETED**；
- Work artifact：`docs/work/archive/eu54-rich-text-v2-mature-editor-adoption.md`；
- Execute Authority：**TERMINATED**。

## 1. 技术决策

V2 已完成从自组 Tiptap + narrow `RichTextHtmlPolicy` 向成熟 editor + shared HTML contract 的收敛。

正式 Primary：SunEditor 3.3.3。Jodit 4.15.0 仅作为 verified fallback evidence。

PR #135 的隔离 PoC / Human Review 只用于技术选型，未进入生产 main；EU-54 implementation 才是 Current integrated implementation lineage。

## 2. Admin implementation

`frontend/admin/src/modules/cms/components/RichTextEditor.vue` 保持 thin adapter：

- mount create / unmount destroy；
- external HTML real-change sync；
- editor change emit；
- stable E2E selector；
- optional managed image callback；
- editor-own toolbar / dialog / history / paste / table / image UI。

`frontend/admin` 精确锁定 current SunEditor dependency，不保留 Tiptap runtime dual implementation。

Current minimum config 包含：

- `zh_cn`；
- required official/core plugins；
- accepted table attributes compatibility；
- accepted cell width / height compatibility；
- current toolbar functions。

新增 compatibility config 必须由 real corpus failure 证明需要。

## 3. Article integration

`ArticleManagementView` 保持 Article domain ownership：

- `bodyHtml` 属于 Article form；
- upload 继续创建 CMS Resource；
- thin adapter 只接收 inserted src/alt/presentation；
- Article consumer 维护 `bodyImageResourceIds`；
- save 前 reconcile managed URLs / associations；
- cover / attachment lifecycle 不迁入 editor core。

## 4. Page integration

Page `RICH_TEXT` 通过同一 `RichTextEditor` 编辑 whole-body `bodyHtml`。

Page Content Architecture 后续已增加 Structured Page；Structured card item body 可以复用 Rich Text capability，但 whole-page Structured content 不回退为并行 arbitrary `bodyHtml`。

Page 不因 Rich Text V2 获得新的 managed Resource DB relation。

## 5. Backend / Public HTML policy

继续使用 OWASP Java HTML Sanitizer 等成熟 parser-based foundation：

- standard formatting / blocks / tables / links / images / styles；
- accepted real corpus presentation attributes / styles；
- explicit active-content rejection；
- 不构造与 editor schema 等宽的 bespoke allow-list。

Article/Page current write boundary + Public defensive read 保持 shared policy responsibility。External Article / non-RICH whole-page contract 不经过 Rich whole-body policy。

Public 不加载 SunEditor editor chrome CSS。

## 6. Existing-data strategy

- no full-database rewrite；
- no Main Historical Migration reactivation；
- Fresh Runtime / canonical import 验证 current policy 不再丢失 accepted presentation；
- existing V1-damaged record 不凭猜测修复；
- 必要 repair 只能 exact-baseline bounded，并保护 operator divergence。

## 7. Verification

Current / future change 根据影响执行：

### Dependency / wrapper

- Admin build/type；
- SunEditor zh_cn init；
- no obsolete Tiptap runtime import；
- Article/Page consume one shared adapter。

### Editor behavior

- Chinese authoring；
- heading/style/list/table/link/image；
- save/reopen；
- undo/redo；
- WPS paste/history。

### Resource

- upload managed image；
- URL / alt / dimensions / alignment；
- association reconcile；
- attachment regression。

### HTML compatibility / safety

- Party 15/16px star；
- teacher-library table/cell/image/float；
- ordinary Article/Page；
- hostile API payload；
- editor → Backend → DB/API → Public browser chain。

### Human Review

当 editor UX 或 accepted presentation 发生变化时，在 automated evidence 后执行 bounded review：Windows IME、真实 WPS、single-paste Undo、managed image、representative Public render。

Microsoft Word 没有当前 Human environment 时保持 non-blocking residual，不能伪报。

## 8. Stop / Escalation

以下情况需要停止 mechanical patch 并形成新 evidence / decision：

- 成熟官方 config 仍无法保持 accepted corpus；
- Public 需要 editor chrome CSS；
- resource association 不稳定；
- WPS history 被 wrapper 破坏；
- Backend 必须复制 editor internal model 才能工作。

不得从本文件的历史 EU-54 规划语义恢复 Execute Authority。
