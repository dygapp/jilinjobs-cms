---
id: specification-rich-text-authoring
title: 富文本内容编辑 V2 规格说明
type: specification
status: ready
version: "V2.0"
relations:
  upstream:
    - docs/requirements/rich-text-authoring.md
  related:
    - docs/technical/rich-text-authoring-plan.md
    - docs/work/current/eu54-rich-text-v2-mature-editor-adoption.md
created_at: 2026-09-05
updated_at: 2026-09-11
---

# 富文本内容编辑 V2 规格说明

## 1. Scope

本 Specification 定义 Article `INTERNAL` 与 Page `RICH_TEXT` 的成熟 editor integration、`bodyHtml` contract、Article managed image bridge、Backend/Public compatibility-first HTML policy 与验证义务。

它不定义 Page Content Architecture、特殊业务单页 renderer、Page Resource association 或 Main historical migration。

## 2. Technology Decision

### RT2-01 Primary editor

正式实现使用 **SunEditor 3.3.3** 开源核心并锁定精确版本与 lockfile。直接消费 SunEditor core；不依赖成熟度不足的 Vue wrapper。

Jodit 4.15.0 是已验证 fallback，但不得静默切换。只有 SunEditor 命中本 Specification 的 Stop Condition，并形成明确 Repository Evidence 后，才能通过新的 Human / Repository Authority 决定是否切换；不得在一个 Runtime 同时保留两套 editor。

### RT2-02 Thin Vue adapter

保留 CMS-local `RichTextEditor` component 名称和尽量稳定的 consumer API。adapter 只负责：

- SunEditor create / destroy lifecycle；
- `modelValue` HTML 初始化与 `update:modelValue`；
- `testId` / E2E integration；
- 中文语言与项目级基础配置；
- 可选 resource callbacks；
- consumer state 与 editor state 同步。

不得重新实现 selection、history、paste、table、font、format 等 editor core behavior，也不得用 Element Plus 重建一套完整 toolbar。

### RT2-03 Chinese / authoring behavior

- 使用 SunEditor `zh_cn`；
- Windows 中文 IME 不丢字、不重复字、不异常跳光标；
- 使用成熟 editor 自带 undo / redo；
- WPS 的一次完整 paste 必须表现为一个可整体撤销的用户操作；
- Paste 后继续插字、删除、格式化、表格编辑与多次 undo / redo 保持稳定。

## 3. Content Contract

### RT2-04 Single HTML Authority

Admin、API、DB 和 Public 继续以 `bodyHtml` HTML 为唯一正文 Authority。不持久化 SunEditor内部 model 或任何第二格式。

### RT2-05 Semantic compatibility

HTML canonicalization 可以发生，但必须保持 accepted corpus 的语义与关键 presentation。Compatibility 验证以 DOM facts + 浏览器实际表现为准，而不是 SHA / 字节相等。

必须保留：

- normal text / paragraphs / headings / emphasis / lists / links；
- table / caption / thead / tbody / tfoot / row / cell；
- `colspan` / `rowspan` 与 accepted table presentation metadata；
- image `src` / `alt` / `title` / `width` / `height` 与必要 alignment；
- accepted corpus 实际需要的 text / background / font / size / align / width / height / float 等 presentation。

### RT2-06 Representative corpus hard gates

- **P1 Party inline image**：canonical Article `zhutijiaoyu:content:154659859759104` 的图片数量稳定，首批 15 / 16 px 星标在保存与 Public render 后仍为小尺寸行内内容；
- **P2 teacher-library**：正式 Page 中 table structure、`align/cellpadding/cellspacing` 等当前语义、1170px table width、200×266 image、cell dimensions 与 `float:left` 等关键 presentation 保持；
- 普通 Article / Page 的标准富文本行为保持。

## 4. SunEditor Configuration Contract

### RT2-07 Built-in UI first

优先使用 SunEditor 自带 toolbar / dialogs / plugins。项目只选择需要启用的成熟能力与做必要业务 adapter，不复制产品自身 UI。

基础能力至少覆盖：

- undo / redo；
- paragraph / heading；
- bold / italic / underline / strike；
- font / font size / text color / background；
- align / ordered / unordered list；
- table；
- link；
- image upload / image size / alignment / alt 等常规图片维护。

### RT2-08 Proven compatibility config

实现起点必须包含 PoC 已验证的 compatibility configuration：

- table attributes：`align|cellpadding|cellspacing`；
- `td` presentation：`width|height`；
- 完整注册实际启用的 SunEditor core plugins；
- `zh_cn`。

最终配置可以因 real-corpus tests增加成熟产品官方配置，但不得演变为复制一份项目自定义 HTML schema。

## 5. CMS Resource Contract

### RT2-09 Article image bridge

Article 继续复用 `/api/admin/resources`：

1. consumer/resource adapter 上传 image；
2. 返回 managed Resource id、content URL、filename；
3. editor 只插入 URL / alt / presentation；
4. Article consumer 把 id 纳入 `bodyImageResourceIds`；
5. save 前只保留正文仍实际引用的 managed image association；
6. 删除正文引用不自动删除 Resource 本体。

SunEditor core 不读写 `bodyImageResourceIds`。

### RT2-10 Attachment / Page boundaries

- Article 现有 `attachmentResourceIds`、独立上传/移除/公开附件 contract 保持；本 Unit 不强制把附件迁入正文；
- Page RICH_TEXT 继续没有 managed Resource association；不新增 Backend schema/API；
- adapter 设计不得阻碍未来增加 resource picker / attachment plugin，但未来能力不作为本 Unit scope。

## 6. Backend / Public HTML Policy

### RT2-11 Compatibility-first sanitizer

保留成熟 OWASP Java HTML Sanitizer 作为 parser-based safety foundation，但重构当前窄 `RichTextHtmlPolicy` 配置方式：

- 优先组合成熟库已有 blocks / formatting / links / images / tables / styles 能力；
- 只为 Repository accepted corpus 补充必要 presentation attributes / styles；
- 不把 SunEditor toolbar schema复制成 Backend allow-list；
- policy tests同时由真实合法 corpus和 hostile corpus驱动。

### RT2-12 Minimum active-content boundary

无论客户端如何调用，Public 输出不得包含可执行 active content。至少覆盖：

- `script`、iframe/object/embed/form 与 document-level meta/base/link；
- `on*` event handler、`srcdoc`；
- `javascript:` / `vbscript:` 等危险 scheme；
- 未授权可执行 SVG/MathML；
- 能引入 active behavior 的危险 CSS/URL。

普通 presentation HTML 不得因为与 editor toolbar 无关而被默认删除。

### RT2-13 write + read defense

- Article INTERNAL create/update 与 Page RICH_TEXT create/update 在 write boundary 通过 shared policy；
- Public Article / RICH_TEXT Page 继续执行 defensive filtering，以覆盖 V2 前历史数据；
- read defense 不回写数据库；
- External Article 和非 RICH_TEXT Page contract 不变。

### RT2-14 Public renderer independence

Public `.rich-content` 必须能够展示存储 HTML 的 accepted语义；不得为了正常正文显示而加载 SunEditor editor UI/chrome CSS。

如果 SunEditor 新生成的关键正文 presentation 必须依赖产品专属 CSS/class 才成立，属于 Stop Condition：不得静默把 Public Renderer绑定到 editor product，需报告 Evidence 并重新决策。

## 7. Historical / Migration Contract

### RT2-15 No bulk rewrite

不执行全库 Article/Page body rewrite，也不更改冻结的 Main historical migration。

管理员实际保存的记录允许进入 SunEditor canonicalization；未编辑记录保持原持久化数据，Public 仅做 defensive filtering。

### RT2-16 V1 content-loss boundary

V2 必须证明 Fresh Runtime / canonical import 不再丢失 Party star `width/height` 等合法信息。对已经由 V1 sanitizer 丢失信息的现有数据库，不得通过猜测恢复；若需要保留该数据库，只能形成 exact-baseline bounded repair，并拒绝覆盖 operator-diverged content。

## 8. Verification Obligations

### RT2-V01 Dependency / build

- Admin 精确锁定 `suneditor@3.3.3`；
- 删除不再使用的 Tiptap runtime dependencies；
- Admin `npm run build` PASS；
- Backend tests/package 与 Public build PASS。

### RT2-V02 Editor integration

Article / Page Browser Evidence覆盖：初始化 existing HTML、中文输入、基础格式、表格、链接、图片、保存/重开/继续编辑、undo / redo。

### RT2-V03 WPS behavior

在自动行为测试中覆盖 paste/history contract；最终 bounded Human Review 使用真实 Windows + WPS 复核：整体格式可接受、继续编辑稳定、一次完整 paste 一次 Undo 可整体回退。

Microsoft Word 当前没有 Human environment，记录为 non-blocking residual；不得伪造 Word PASS。

### RT2-V04 Resource consistency

Article managed image upload/insert/remove/save 后：正文 URL 与 `bodyImageResourceIds` 一致；attachment lifecycle 无回归。

### RT2-V05 HTML compatibility / safety

Backend + browser regression 同时覆盖 P1、P2 和 hostile direct API payload；证明关键 presentation 保留且 active content 不进入可执行 Public 输出。

### RT2-V06 Public integration

Main / Party Public Article 与 RICH_TEXT Page 回归 PASS；不依赖 SunEditor editor chrome CSS；现有 accepted `.rich-content` 行为没有无关回归。

### RT2-V07 Full regression

最终 exact implementation Head 执行当前 CI、Backend、Admin、Public、Integrated Browser 以及受影响 Party verification；旧测试若只绑定 Tiptap DOM implementation detail，按 Stale Verification Contract 更新到用户行为/稳定 wrapper selector，不恢复旧实现。

## 9. Stop Conditions

出现以下任一情况时停止机械修复并提交证据，不得通过不断增加 bespoke transform / CSS / plugin 绕过：

1. accepted P1/P2 presentation 在成熟官方配置下仍无法保持；
2. Public 正常展示必须整体依赖 SunEditor editor UI/chrome CSS；
3. Article managed resource association 出现不可解释的新增/丢失；
4. wrapper 使真实 WPS paste 失去已验证的一步 Undo 行为；
5. 为通过 Backend policy 必须再次建立与 editor schema等宽的项目自定义 HTML 方言。

Jodit fallback 只有在 Stop Condition成立并经新的明确 Authority确认后才能启用。

## 10. Slice Result

本 V2 由 `slice-work` 形成单一 Candidate：

**EU-54 — Rich Text V2 Mature Editor Adoption**。

不拆分“editor replacement”与“HTML policy correction”，因为两者共享同一 `bodyHtml` contract、real-corpus compatibility Gate 和 Public行为；分拆会再次产生前后端内容模型错位风险。
