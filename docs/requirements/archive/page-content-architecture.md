# 单页内容架构需求

## 状态

- 长期架构边界：GitHub Issue #77；
- 规划来源与 legacy acquisition evidence：GitHub Issue #137；
- 上游产品 Authority：`docs/requirements/information-publishing.md`；
- 规格与技术方案：`docs/specifications/page-content-architecture.md`、`docs/technical/page-content-architecture.md`；
- Requirement：**CURRENT / ACCEPTED**；
- Implementation：**COMPLETED via EU-55**；
- Execute Authority：**TERMINATED**。

本文保留 EU-55 完成后的长期产品约束，不拥有 Current Execution Gate。

## 1. 目标

CMS `Page` 既承载普通说明性正文，也可能承载具有稳定结构、特殊呈现、交互或外部集成语义的正式单页。普通 `RICH_TEXT + bodyHtml + generic renderer` 可以覆盖大量内容页，但不能把具有用户可感知产品结构的页面长期压平成任意 HTML 后仍宣称语义完整。

长期要求是：**Page 的 stable identity、group、canonical URL 与 enabled / publish lifecycle 继续属于 CMS；Page 正文必须由一个明确 primary content authority 拥有，并以能够保留真实产品语义的数据模型与显式 renderer 呈现。**

## 2. 三个正交维度

Page content contract 必须分别回答三个问题，不得重新压成一个混合 `renderMode`：

1. **内容模型（Content Model）**：正文是什么数据形态；
2. **Renderer Identity**：Public Runtime 使用哪个稳定 renderer；
3. **内容所有权（Content Ownership）**：primary body/content authority 由 operator、Site Package lifecycle、engineering implementation 或 external integration 中的哪一方承担。

Current Generic CMS 持久化语义使用：

- `contentModel`；
- `rendererKey`；
- `contentOwner`；
- `bodyHtml`；
- `structuredPayload`；
- `embedUrl`。

## 3. Primary content authority

每个有效 Page 必须唯一回答“正文事实在哪里”。不得出现以下长期状态：

- operator 可编辑 `bodyHtml`，但真实页面主要由 repository hardcode 决定；
- `bodyHtml` 与 `structuredPayload` 同时成为 primary body；
- Site Package default 与 engineered component 同时声称拥有当前正文；
- external URL、structured data 与 arbitrary rich text 同时作为主内容；
- renderer 通过 alias、URL、DOM shape 或正文内容猜测 Page 类型。

Unknown / unsupported content model、payload schema 或 renderer 必须 fail closed，不得静默 fallback 到 Rich HTML。

## 4. Page profiles

### 4.1 普通富文本 Page

普通说明、通知说明以及由运营人员自由维护正文的 Page 继续使用成熟 Rich Text 能力。

- `contentModel=RICH_TEXT`；
- primary body 为 `bodyHtml`；
- renderer 使用 generic Rich renderer；
- ordinary Runtime content 由 operator 维护；
- 不因为本架构存在而把所有 Page 迁移为 Structured。

`about` 是当前代表样本。

### 4.2 Structured Page

当 card、section、step、accordion 等结构本身属于用户可感知产品语义时，应使用可验证的 structured contract，而不是只保存“等价文本”。

- `contentModel=STRUCTURED`；
- primary body 为 `structuredPayload`；
- top-level `bodyHtml` 不再作为并行正文 Authority；
- renderer 使用稳定、显式 `rendererKey`；
- Admin authoring surface 必须与结构模型一致；
- item 内部可以按结构 schema 使用受控 Rich HTML。

### 4.3 Engineering Page

只有真实产品行为需要 repository-owned interaction / state / implementation，且稳定 Structured contract 无法自然表达时，才进入 Engineering profile。

Engineering 不是通用 Page type，也不是为了方便而跳过 CMS content ownership 的逃生口。CMS Page identity / canonical route / lifecycle 仍保留。

### 4.4 External integration

当 primary behavior/content 来自外部系统时：

- CMS Page 继续持有 stable identity、route、lifecycle 与必要 integration metadata；
- external system 持有 primary content / behavior；
- renderer / integration identity 必须显式；
- iframe、URL 等只是 delivery mechanism，不是 content model。

具体慧就业 integration 仍是独立 Planning Candidate，本架构不自动启动。

## 5. `guide/jypq` 当前产品事实

Issue #137 的 legacy evidence 证明原页面具有重复 card 结构；EU-52 阶段曾把这些 card 扁平化为 Rich HTML。该 flattened Rich 表达现在只属于 **EU-55 adoption 之前的历史 predecessor**，不再是 Current target。

EU-55 已完成 `guide/jypq` Structured adoption。Current Site Package / Runtime target 为：

```text
contentModel = STRUCTURED
rendererKey = JILINJOBS_GUIDE_CARDS
contentOwner = OPERATOR
structuredPayload.schemaVersion = 1
structuredPayload.kind = CARD_COLLECTION
```

当前 `CARD_COLLECTION` 保留已接受的 card title / body / order 与既有 package image references。不得从 EU-52 历史 formal-content 文档恢复 flattened `RICH_TEXT` 作为当前 Page authority。

## 6. Existing operator ownership protection

EU-49 / EU-52 建立的 operator-content protection 继续有效。

任何 package-driven Page evolution 都必须满足：

- Fresh create 可以使用 Site Package accepted default；
- Existing Site adoption 必须有显式、可审计的 prior-baseline precondition；
- operator-diverged content 必须 preserve + report；
- ordinary reconcile 不得覆盖 operator content；
- adoption 必须幂等；
- ownership transfer 必须显式；
- Main Historical Migration 不作为 silent fallback。

EU-55 对 `guide/jypq` 的 Rich → Structured 转换已经按 exact prior baseline adoption contract 完成；后续 Structured → Structured package update 不得自动复用旧 fingerprint 语义，必须依据新的明确 Authority。

## 7. Stable lifecycle 与 URL

Page 的 stable identity / grouping / enabled lifecycle 保持 CMS-owned。Current canonical routes 保持：

```text
/page/{alias}
/page/{groupAlias}/{alias}
```

`/page/guide/jypq` 保持稳定。不得为不同 renderer 建立第二套 Page identity 或 canonical route。

## 8. Admin / Public contract

Current Admin / Public Page contract 必须能够显式表达 `contentModel / rendererKey / contentOwner / structuredContent`，同时按现有 compatibility 约束保持仍被当前客户端消费的 legacy projection。

- Admin 根据 content model / structured schema 选择 authoring surface，不通过 alias/path 特判；
- Rich Page 使用现有 mature Rich Text editor；
- `STRUCTURED + CARD_COLLECTION V1` 使用 ordered card editor；
- unsupported model / schema / renderer 显示阻塞诊断，不提供 arbitrary Rich fallback；
- Public Renderer 使用显式 registry / resolver，不从 alias/path/DOM 猜测。

## 9. 验证义务

后续触达 Page Content Architecture 时至少验证：

- Fresh DB 完整 Flyway chain 可以建立 Current Page schema；
- Existing legacy Page upgrade 不破坏 operator content；
- Rich Page regression；
- Structured schema parse / validation / sanitization；
- renderer dispatch 与 unknown fail-closed；
- Admin model-specific authoring；
- Site Package exact-baseline adoption / operator divergence / idempotency；
- `/page/guide/jypq` canonical URL；
- `guide/jypq` card count / order / titles / body / package image integrity；
- 自动 Browser verification 先于 bounded Human Review。

## 10. 非目标

- 不建设通用 Page Builder / arbitrary block framework；
- 不因为架构存在就自动把 FAQ 迁移为 Structured；
- 不自动实现慧就业 external integration；
- 不创建无真实需求的 Engineering Page sample；
- 不允许 operator 任意切换 content model / renderer / owner；
- 不重新打开 Main Historical Migration；
- 不改变 Public frontend 技术栈；
- 不从本文创建新的 Candidate / Ready Execution Unit。
