---
id: specification-rich-text-authoring
title: 富文本内容编辑 V2 规格说明
type: specification
status: accepted
version: "V2.0"
relations:
  upstream:
    - docs/requirements/rich-text-authoring.md
  related:
    - docs/technical/rich-text-authoring-plan.md
    - docs/work/archive/eu54-rich-text-v2-mature-editor-adoption.md
created_at: 2026-09-05
updated_at: 2026-09-13
---

# 富文本内容编辑 V2 规格说明

## 状态

- Specification：**CURRENT / ACCEPTED**；
- EU-54：**COMPLETED / Execute Authority TERMINATED**。

## 1. 适用范围

本规格定义 Article `INTERNAL` 与 Page `RICH_TEXT` 的 mature editor integration、`bodyHtml` contract、Article managed image bridge、Backend/Public compatibility-first HTML policy 与 verification obligations。

不定义 Page Content Architecture、特殊业务 Page renderer、Page Resource association 或 Main Historical Migration。

## 2. Editor contract

正式实现使用 **SunEditor 3.3.3**，精确锁定 dependency / lockfile；直接消费 SunEditor core，不依赖额外 Vue wrapper。

CMS-local `RichTextEditor` 是 thin lifecycle adapter，只负责：

- create / destroy；
- `modelValue` 初始化与同步；
- `update:modelValue`；
- E2E stable selector；
- `zh_cn` 与项目最小 config；
- optional resource callback / uploading state。

不得在 adapter 中重新实现 selection、history、paste、table、font、format 等 editor core behavior。

## 3. Authoring behavior

- Windows 中文 IME 正常；
- WPS 一次完整 paste 作为一个可整体 Undo 的用户操作；
- paste 后继续插字 / 删除 / formatting / table edit / multi undo-redo 稳定；
- 使用成熟 editor 自带 toolbar / dialog / plugin；
- 至少支持 heading、bold/italic/underline/strike、font/size/color/background、align、list、table、link、image、undo/redo。

## 4. Content contract

`bodyHtml` 是唯一 Rich Text Authority。Editor canonicalization 不要求 byte-equal，但必须保持 accepted semantic/presentation：

- text / paragraphs / headings / emphasis / list / link；
- table / caption / thead / tbody / tfoot / row / cell；
- colspan / rowspan；
- image src / alt / title / width / height / alignment；
- accepted text/background/font/size/align/width/height/float。

## 5. Compatibility hard gates

### Party inline image

Canonical Party representative Article 中 15 / 16 px star images 在 save + Public render 后仍保持 inline small-size presentation。

### teacher-library

Main Page 中 table structure、accepted table metadata、1170px width、200×266 image、cell dimensions、`float:left` 等关键 presentation 保持。

普通 Article / Page Rich Text 也必须回归。

## 6. Resource contract

### Article managed image

- 上传继续走 `/api/admin/resources`；
- editor 插入 URL / alt / presentation；
- consumer 维护 `bodyImageResourceIds`；
- save 按 body actual references reconcile association；
- remove body reference 不自动删除 Resource；
- image presentation 可维护。

SunEditor core 不直接读写 `bodyImageResourceIds`。

### Attachment / Page

- Article `attachmentResourceIds` lifecycle 保持；
- Page `RICH_TEXT` 不新增 managed Resource association；
- future resource picker / plugin 需要新的明确 contract。

## 7. HTML safety policy

使用成熟 OWASP Java HTML Sanitizer / parser-based policy：

- compatibility-first；
- 从成熟 formatting / blocks / links / images / tables / styles policy 组合；
- 只为 Repository accepted corpus 增加必要 presentation attributes / styles；
- 不复制 SunEditor toolbar schema 作为 Backend dialect；
- hostile corpus 与 real valid corpus 同时驱动 tests。

Public 输出至少阻止 script、event handlers、dangerous schemes、unauthorized iframe/object/embed/form、executable SVG/MathML、meta/base/link 与 dangerous CSS/URL behavior。

Article/Page Rich write boundary 与 Public defensive read 复用同一 shared policy responsibility；read defense 不回写 DB。

## 8. Public renderer independence

Public `.rich-content` 必须能显示 accepted standard HTML，不得整体加载 SunEditor editor UI/chrome CSS 才能工作。

如果 future editor output 必须依赖 editor product-specific CSS/classes 才能正确显示，必须触发 Stop Condition / new evidence review。

## 9. Historical data

不执行 bulk rewrite，不重新打开 Main migration。Fresh Runtime / canonical import 必须证明 V2 不再造成 accepted presentation loss。

已发生的旧数据损失只能在有 exact baseline evidence 时做 bounded repair；operator-diverged content 必须保护。

## 10. Verification obligations

- dependency / lock / Admin build；
- editor init / Chinese / formatting / table / link / image / save-reopen；
- WPS paste/history；
- Article managed image + attachment regression；
- Party / teacher-library real corpus；
- hostile direct API payload；
- Public render independent from editor chrome CSS；
- exact-head Backend/Admin/Public/Browser regression。

旧测试若只绑定 Tiptap DOM implementation detail，应按 Stale Verification Contract 更新到 user behavior / stable wrapper selector，不恢复旧实现。

## 11. Stop Conditions

出现以下情况不得通过堆叠 bespoke transforms / CSS / plugins 继续绕过：

1. accepted real corpus 在成熟官方 config 下仍无法保持；
2. Public 正常显示必须整体依赖 editor chrome CSS；
3. Article managed resource association 不稳定；
4. wrapper 破坏真实 WPS single-paste Undo；
5. 必须在 Backend 复制与 editor schema 等宽的 project-specific HTML dialect。

Jodit fallback 只在 Stop Condition 成立并取得新 Authority 后允许启用。
