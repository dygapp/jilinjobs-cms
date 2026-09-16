---
id: specification-rich-text-authoring
title: 富文本内容编辑规格
type: specification
status: accepted
version: "V4.0"
relations:
  requirements:
    - docs/requirements/information-publishing.md
    - docs/requirements/cms-domain.md
  architecture:
    - docs/architecture/cms-architecture.md
  technical:
    - docs/technical/rich-text-authoring.md
updated_at: 2026-09-16
---

# 富文本内容编辑规格

## 1. Scope

本规格定义 CMS 运营人员对 Rich Text 内容的可观察 authoring、compatibility、resource 与 safety 行为。

适用范围：

- INTERNAL Article 的 whole-body Rich Text；
- RICH_TEXT Page 的 whole-body Rich Text；
- Structured schema 明确允许的 item-level Rich Text。

Rich Text 的长期 content authority、ownership 与安全业务不变量由 Domain Requirement 持有。本规格不决定 editor 品牌、版本、framework wrapper、Backend library、HTML parser 或源码目录；这些属于 Technical / implementation。

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

不得要求运营人员理解 editor internal schema、HTML safety implementation 或资源存储细节。

## 3. 保存、重开与语义保持

Rich Text 保存时可以对等价内容做不会改变用户感知语义的规范化，但 Domain 所定义的正文 Authority 仍必须保持单一且可恢复。

用户可观察的有效内容在 save → reopen → public chain 中应保持，例如：

- paragraphs / headings / emphasis / lists / links；
- table / row / cell / colspan / rowspan；
- image src / alt / title / accepted width / height / alignment；
- accepted text / background / font / size / align / width / height / float。

保存后重新打开必须能够继续稳定编辑；不得因为 editor 内部 representation 改变而要求运营人员维护第二份正文。

## 4. Paste / history behavior

对常见 WPS / Office-shaped content：

- 一次用户 paste 应作为一个可理解的 authoring 操作进入 history；
- paste 后继续输入、删除、formatting、table edit 和 undo / redo 不应破坏内容；
- 不能为了“清理 HTML”而把仍属于 accepted presentation 的结构、图片尺寸或表格信息机械删除。

在缺少真实 Microsoft Word 环境时，不把未实际观察的 Word-specific 行为声明为已验证产品结果。

## 5. Article managed images

Article Rich Text 中的 managed image 必须保持以下用户可观察语义：

1. 运营人员可以在 editor 中上传或插入受控 CMS image；
2. 保存后图片在重新打开时仍存在；
3. Public 页面能够通过公开资源 contract 显示该图片；
4. alt、有效尺寸 / alignment 等已接受 presentation 可以持久化；
5. 从正文删除引用不等于立即物理删除 Resource；
6. Article attachment lifecycle 不因 Rich Text editor 改变而被强制合并进正文图片模型。

Page Rich Text 当前不因为本规格自动获得新的 Article-style managed Resource domain；若未来需要新的 Page Resource relation，应形成独立 Requirement / Specification。

## 6. Compatibility behavior

触达 Rich Text capability 时，需要保护当前已经接受的代表性内容形态，包括：

- small inline images；
- table / cell / image dimensions / float 等复杂 presentation；
- ordinary Article / Page Rich Text；
- WPS-shaped 中文 paste / undo behavior。

具体 regression corpus、样本 identity 与测试 fixture 由 Verification / Repository Evidence 持有，不在 Specification 复制成新的业务数据 Authority。

## 7. HTML safety

Rich Text 必须在保持 accepted content compatibility 的同时，阻止 Domain / Product Authority 所定义的 active / document-level dangerous content 形成可执行或可利用结果。

用户可观察 contract 是：

- hostile active content 不被当作正常可执行正文接受；
- safety processing 不以破坏 accepted valid content 为代价；
- Public defensive handling 不把读取时的处理静默持久化成新的正文事实。

具体危险内容分类、业务安全边界由 Product / Domain Authority 持有；parser、sanitization、defensive-read mechanism 与 editor integration 属于 Technical，本规格不复制实现 allow-list / block-list。

## 8. Public rendering independence

Public 正常展示 accepted Rich content 时，不得要求加载 authoring editor 的完整 UI / chrome 才能成立。

如果未来某个 editor 的正常输出必须依赖其产品专属 authoring runtime 才能显示，应重新评估 Technical choice 与产品 compatibility，而不是把 Admin implementation 变成新的公开内容 contract。

## 9. Failure behavior

以下情况不得静默成功：

- hostile active content 被接受并执行；
- accepted valid content 在 save → reopen → public chain 中出现明显语义 / presentation loss；
- managed image association 丢失导致 Public content broken；
- editor integration 导致正常中文输入或 paste/history 无法使用；
- 为适配 editor 而要求 Public 理解 Admin-only resource route。

具体错误提示与 implementation recovery 由 Technical contract 决定，但失败结果必须可观察、可验证。

## 10. Acceptance

触达 Rich Text capability 时，最终结果至少满足实际涉及的以下 contract：

- 中文输入正常；
- common formatting / list / table / link / image 可编辑并保持；
- save / reopen 后用户可感知语义保持；
- undo / redo 可用；
- WPS-shaped paste / history 不破坏 accepted content；
- Article managed image 在 authoring → reopen → Public chain 中保持；
- attachment behavior 不因 editor integration 回归；
- representative simple / complex Rich Text content 保持；
- hostile active content 被阻止；
- Public render 不依赖 editor authoring chrome。

验证使用哪些 Backend / Admin / Public / Browser 层次与 Human evidence，由当前 Verification Authority 和实际风险决定，不由本规格固化测试程序。

## 11. Non-goals

- generic Page Builder；
- Page Resource domain expansion；
- grammar / typo / sensitive-word / AI writing。
