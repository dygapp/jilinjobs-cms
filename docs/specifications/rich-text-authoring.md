---
id: specification-rich-text-authoring
title: 富文本内容编辑规格
type: specification
status: accepted
version: "V3.0"
relations:
  requirements:
    - docs/requirements/information-publishing.md
    - docs/requirements/cms-domain.md
  architecture:
    - docs/architecture/cms-architecture.md
  technical:
    - docs/technical/rich-text-authoring.md
updated_at: 2026-09-15
---

# 富文本内容编辑规格

## 1. Scope

本规格定义 CMS 运营人员对 Rich Text 内容的可观察 authoring、compatibility、resource 与 safety 行为。

适用范围：

- INTERNAL Article 的 whole-body Rich Text；
- RICH_TEXT Page 的 whole-body Rich Text；
- Structured schema 明确允许的 item-level Rich Text。

本规格不决定 editor 品牌、版本、framework wrapper、Backend library 或源码目录；这些属于 Technical / implementation。

## 2. Authoring experience

Rich Text authoring 必须提供成熟、稳定的编辑体验，至少覆盖：

- 中文输入法正常输入；
- 段落、标题、粗体、斜体、下划线、删除线；
- 字体 / 字号 / 文字色 / 背景色等当前已接受表现；
- 对齐、列表；
- 表格及常见单元格结构；
- 链接；
- 图片；
- undo / redo；
- 常见 WPS / Office-shaped paste。

不得要求运营人员理解 editor internal schema、HTML sanitizer 实现或资源存储细节。

## 3. Single body authority

Rich Text 的长期正文 Authority 是 `bodyHtml`。

不得同时持久化第二套 editor-internal JSON、Delta、Markdown 或其他 whole-body representation，并要求二者长期同步。

Editor 保存时允许对等价 HTML 做 canonicalization，但必须保持用户可感知语义和已接受表现，例如：

- paragraphs / headings / emphasis / lists / links；
- table / row / cell / colspan / rowspan；
- image src / alt / title / accepted width / height / alignment；
- accepted text / background / font / size / align / width / height / float。

保存后重新打开必须能够继续稳定编辑。

## 4. Paste / history behavior

对常见 WPS / Office-shaped content：

- 一次用户 paste 应作为一个可理解的 authoring 操作进入 history；
- paste 后继续输入、删除、formatting、table edit 和 undo / redo 不应破坏内容；
- 不能为了“清理 HTML”而把仍属于 accepted presentation 的结构、图片尺寸或表格信息机械删除。

真实 Microsoft Word 行为只有在具备对应环境时才作为 Human Evidence；没有环境时不得伪报 PASS。

## 5. Article managed images

Article Rich Text 中的 managed image 必须保持以下用户可观察语义：

1. 运营人员可以在 editor 中上传或插入受控 CMS image；
2. 保存后图片在重新打开时仍存在；
3. Public 页面能够通过公开资源 contract 显示该图片；
4. alt、有效尺寸 / alignment 等已接受 presentation 可以持久化；
5. 从正文删除引用不等于立即物理删除 Resource；
6. Article attachment lifecycle 不因 Rich Text editor 改变而被强制合并进正文图片模型。

Page Rich Text 当前不因为本规格自动获得新的 Article-style managed Resource domain；若未来需要新的 Page Resource relation，应形成独立 Requirement / Specification。

## 6. Compatibility corpus

触达 Rich Text capability 时至少保护当前已接受 representative corpus：

- Party Article 中 small inline images；
- Main `teacher-library` Page 中 table / cell / image dimensions / float 等复杂 presentation；
- ordinary Article / Page Rich Text；
- WPS 中文 paste / undo behavior。

该 corpus 只是 regression evidence，不成为新的业务数据 Authority。

## 7. HTML safety

Rich Text 必须在保持 accepted content compatibility 的同时阻止真正 active / document-level dangerous content，例如：

- script；
- event handler；
- dangerous URL scheme；
- 未经独立 integration Authority 允许的 iframe / object / embed / form；
- executable SVG / MathML；
- meta / base / link 等 document-level controls；
- dangerous CSS / URL behavior。

Safety policy 不能简单等于当前 editor toolbar allow-list，也不能用 regex 代替结构化 HTML parsing。

Public defensive filtering 不应在读取时静默回写数据库内容。

## 8. Public rendering independence

Public 正常展示 accepted Rich HTML 时，不得要求加载 authoring editor 的完整 UI / chrome 才能成立。

如果未来某个 editor 的正常输出必须依赖其产品专属 UI CSS 或 internal runtime 才能显示，应停止机械 patch，重新评估 Technical choice 与产品 compatibility。

## 9. Failure behavior

以下情况不得静默成功：

- hostile active content 被接受并执行；
- accepted valid content 在 save → reopen → public chain 中出现明显语义 / presentation loss；
- managed image association 丢失导致 Public content broken；
- editor wrapper 导致正常中文输入或 paste/history 无法使用；
- 为适配 editor 而要求 Public 理解 Admin-only resource route。

具体错误提示与实现 recovery 由 Technical contract 决定，但必须可验证。

## 10. Acceptance

触达 Rich Text capability 时，根据实际影响至少覆盖：

- 中文输入；
- common formatting / list / table / link / image；
- save / reopen；
- undo / redo；
- WPS paste / history；
- Article managed image；
- attachment regression；
- representative Party / teacher-library corpus；
- hostile direct API payload；
- Public render 不依赖 editor chrome；
- Backend / Admin / Public / Integrated Browser regression。

有真实 UX / presentation 变化时，在 automated evidence 后执行 bounded Human Review。

## 11. Non-goals

- 自研 editor core / selection / history / paste / table engine；
- 同时维护两套 Rich Text editor runtime；
- whole-body HTML 与 editor JSON 双写；
- generic Page Builder；
- Page Resource domain expansion；
- full-database body rewrite；
- Main Historical Migration reactivation；
- grammar / typo / sensitive-word / AI writing；
- 从本规格恢复已结束的 Rich Text Execution Unit。