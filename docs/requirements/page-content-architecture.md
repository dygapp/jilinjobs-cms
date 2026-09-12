# Page Content Architecture Requirement

## Status

- Planning source: GitHub Issue #137 — durable Planning Capture only
- Long-lived architecture boundary: GitHub Issue #77
- Existing product authority: `docs/requirements/information-publishing.md`
- Existing Main Page delivery authority: `docs/requirements/main-single-page-formal-content.md`
- Planning baseline: `main@881893f9523effaabb82e0a0dacfcd38fe0c44fe`
- Requirement: **READY FOR PLANNING REVIEW**
- Candidate Execution Unit: **NONE**
- Execute Authority: **NONE**

## 1. Intent

CMS `Page` 既承载普通说明性正文，也可能承载具有稳定结构、特殊呈现、交互或外部集成语义的正式单页。当前 Main Page formal-content 已证明普通 `RICH_TEXT + bodyHtml + generic renderer` 可以覆盖大量内容页，但 `guide/jypq` 的真实 source evidence 又证明：把多 card 页面采集后压平成任意 HTML，会保留文字与图片，却丢失原页面的结构 / 呈现语义。

本 Requirement 的目标是建立 Page 内容语义的产品边界：**Page 的 stable identity / navigation / publish lifecycle 仍属于 CMS，而 Page 正文必须由一个明确 primary content authority 拥有，并以能够保留该页面真实产品语义的数据模型与 renderer 呈现。**

本 Requirement 不预先冻结 DB 字段名、枚举名或具体 Vue component；这些属于 Specification / Technical Planning。

## 2. Product requirements

### 2.1 One primary content authority

每个 Page 在任一有效配置下必须能够唯一回答“正文由谁维护、什么数据是正文事实、由什么 renderer 呈现”。不得出现多个来源同时声称拥有 primary body/content 的状态。

至少必须避免：

- operator 可编辑 `bodyHtml`，但真实页面主要由 repository hardcode 决定；
- Site Package body 与 engineered component 同时作为有效正文；
- external URL、structured data 与 arbitrary rich text 同时被当作主内容；
- renderer 通过 alias/path 隐式猜测页面类型，而 Page metadata 没有表达稳定 renderer identity。

### 2.2 Rich content remains the default for ordinary content pages

普通说明、通知说明型单页以及长期由运营人员自由编辑正文的 Page 继续允许使用成熟 Rich Text 能力与 generic rich-content renderer。

不得因为本 Requirement 存在就把所有 Page 升级为 structured/component model，也不得引入通用 Page Builder。

### 2.3 Product-significant structure must not be flattened away

当 source / Human Review / current product authority 证明页面的 section、card、accordion、步骤、交互或其他结构本身具有用户可感知产品语义时，采集、Site Package、Runtime model 与 Public Renderer 不得只保留“等价文本字节”而静默丢弃这些语义。

若现有模型无法表达该语义，应 fail closed 到 Planning / Specification，而不是把它默认降级为 arbitrary `bodyHtml`。

### 2.4 Special rendering must be explicit

非 generic rich-content 页面需要稳定、可审计的 renderer selection contract。长期方案不得依赖 `alias === ...`、URL 特判、正文 DOM 猜测或其他隐藏 routing 规则来选择特殊 renderer。

具体 metadata / field 名称由 Specification / Technical Planning 决定。

### 2.5 Stable Page lifecycle remains CMS-owned

无论正文最终由 rich content、structured data、repository engineering asset 还是 external integration 承载：

- Page stable identity、group membership、canonical public URL、navigation visibility、publish/enabled lifecycle 继续由现有 CMS / Site Package boundary 管理；
- Public Renderer 不成为 Site Definition 或产品数据 authority；
- 不因为特殊正文模式建立第二套与 CMS Page identity 并行的页面目录。

### 2.6 Existing operator ownership protection remains valid

EU-49 / EU-52 已建立的 existing Page operator-content protection 不因新 Page architecture 失效。

任何 Page 从现有 `RICH_TEXT` baseline 演进到新的 content model / renderer contract 时：

- 必须有显式 migration / adoption precondition；
- operator-diverged Runtime content 不得被静默覆盖；
- package / migration / engineered asset 之间的 ownership transfer 必须可审计；
- Main historical migration 不作为 silent fallback。

## 3. Representative product samples

正式 Specification 至少用以下真实样本验证模型，而不是只做抽象分类：

### 3.1 About — ordinary Rich Content

`about` 当前由 Site Package 提供 create-time formal body、Runtime 后续可由 operator 维护，generic rich-content renderer 已满足产品语义。它用于证明新模型不会把普通 Page 复杂化。

### 3.2 Employment dispatch (`guide/jypq`) — structure-sensitive Page

Current repository evidence establishes:

- legacy acquisition surface 是 `/jiuyepaiqian`，`contentMode = GUIDE_CARDS`，selector 为 `#blueTabContent`；
- collector 对多个 `.card` 提取 title/body 后主动投影为 `<section><h2>...</h2>...</section>`；
- current Site Package 因此保存为 `RICH_TEXT`；
- current Public `PublicPageView.vue` 对非 placeholder 内容统一走 generic `v-html`；
- current E2E 证明 route marker 与 4 张图片可用，但不验证 legacy card structure / interaction / presentation。

因此当前 `guide/jypq` 的正文内容存在，但**产品结构语义恢复不足**。Specification 必须在 Structured 与 Engineering 等方案之间作正式选择，不能把现有 flattened Rich Text 直接视为最终正确模型。

### 3.3 FAQ — hierarchy-sensitive but currently accepted

FAQ 已在 EU-52 Human Review 中修复一级/二级层级显示问题，当前不是开放 blocker。它用于验证架构能够表达 hierarchical content，但本 Requirement 不要求立即把 FAQ 从当前 accepted Rich Text 迁移为 Structured。

### 3.4 Embedded / external integration

外部业务系统 Page 用于验证 external content authority 与 CMS Page identity 能够并存。是否、何时实现具体 Hui Employment integration 仍是独立 Planning Candidate，不由本 Requirement授予 Execute Authority。

### 3.5 Engineering page

只有出现需要定制业务交互、强产品级布局或 repository-owned implementation 的真实 Page 时，才允许选择 engineered renderer。工程页面不能因为“做起来方便”成为特殊 Page 的默认逃生口。

## 4. Acceptance principles for Specification

后续 Specification 必须至少回答：

1. Page content model 与 renderer selection 如何区分，避免继续把不同维度混进单一语义；
2. primary content authority 如何显式表达；
3. non-rich renderer identity 如何稳定绑定且 unknown binding 如何 fail closed；
4. structured / engineered / external content 如何进入 Admin / Public API 边界；
5. canonical `/page/{alias}` / `/page/{group}/{alias}` 是否保持稳定；
6. existing Site Package / operator divergence protection如何延续；
7. `guide/jypq` 的正式目标模型是什么，以及为什么；
8. FAQ 与 external integration 在该模型中是否自然成立，而无需为了模型完整性强行迁移。

## 5. Verification requirements

进入任何 implementation readiness 前，至少需要：

- representative-sample analysis 覆盖 About / `guide/jypq` / FAQ / external integration；
- current acquisition projection 与 Public Renderer 路径被 repository evidence 验证；
- Specification 对 ownership、renderer selection、fallback/fail-closed、URL stability 给出明确 contract；
- 如果引入 Generic CMS schema / API / Admin authoring model，必须有对应 Technical Plan 与 migration compatibility strategy；
- `guide/jypq` 的目标呈现需要 Browser verification，并在自动验证后进行 bounded Human Review；
- 所有 existing operator-content adoption / divergence protection保持可验证。

## 6. Non-goals

- 不在 Requirement 阶段冻结 `contentType`、`rendererKey`、`ownership` 等具体字段名；
- 不决定新 DB schema；
- 不把 FAQ 自动迁移为 Structured；
- 不把 iframe 设为所有特殊 Page 的默认方案；
- 不重写 Public frontend 技术栈；
- 不改变四层 CMS Core / Site Package / Historical Migration / Replaceable Public Renderer boundary；
- 不启动 Hui Employment integration；
- 不创建 Candidate / Ready Execution Unit；
- 不访问或升级 `agentic-dev` upstream baseline。

## 7. Requirement readiness

当前 Consumer-local Requirement、Issue #77 long-lived boundary、EU-52 accepted formal Page contract、current source configuration、collector implementation、Site Package projection、Public renderer 与 E2E 已经共同证明真实产品 gap：**结构敏感 Page 不能继续依赖语义扁平化作为长期方案。**

因此 Requirement 层面的 intent / product constraints 已足够进入正式 Specification review；Structured / Engineering 选择、metadata contract、storage/API/Admin impact 等仍属于下一阶段，不能由本 Requirement 猜测为既成事实。

本 Requirement 本身不构成 Execute Authority。