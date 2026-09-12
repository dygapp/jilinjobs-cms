# Page Content Architecture Specification

## Authority

- `docs/requirements/page-content-architecture.md`
- GitHub Issue #137 — Planning Capture / legacy acquisition evidence source only
- GitHub Issue #77 — CMS Core / Site Package / Historical Migration / Replaceable Public Renderer boundary
- `docs/requirements/main-single-page-formal-content.md`
- `docs/specifications/main-single-page-formal-content.md`
- ADR-0002 — Public Site Multi-entry Modular SPA

## Status

- Planning baseline: `main@881893f9523effaabb82e0a0dacfcd38fe0c44fe`
- Specification: **READY / HUMAN REVIEW ACCEPTED**
- Representative-sample validation: **SUFFICIENT FOR TECHNICAL PLANNING**
- Technical Planning: **NEXT NATURAL STAGE / NOT STARTED**
- Candidate Execution Unit: **NONE**
- Readiness: **NOT APPLICABLE / NOT STARTED**
- Execute Authority: **NONE**

## 1. Model

Page 的 stable identity、group / canonical URL / enabled-publish lifecycle 继续由现有 CMS Page domain 持有。Page 正文采用一个明确的 primary content model，并通过一个明确 renderer contract 呈现。

本 Specification 在概念层区分三个正交维度，但**不在本阶段冻结 DB 字段名或枚举名**：

1. **Content Model**：正文是什么形态；
2. **Renderer Identity**：Public Runtime 用哪个稳定 renderer 呈现；
3. **Content Ownership**：primary body/content authority 属于 operator Runtime、Site Package、repository engineering asset 或 external system 中的哪一个。

实现可以在 Technical Planning 中选择更小的数据表达，只要仍满足这三个语义问题可被唯一回答。

下文的 Rich / Structured / Engineering / External 不是建议落成一个新的四值 `renderMode` 或单一 Page type enum，而是用于验证三个维度能否自然组合的**代表性 Page profiles**：

- Rich Content 与 Structured 主要描述 content-model archetype；
- Engineering 主要约束 repository-owned behavior / presentation 与显式 renderer binding；它仍可能消费明确的 structured/component data；
- External / Embed 主要描述 external ownership / integration profile，iframe、URL 或其他 embed mechanism 是 delivery mechanism，不是 content model；
- Renderer Identity 与 Content Ownership 始终独立判断，不得从 profile 名称、alias、URL 或正文 DOM 隐式推导。

## 2. Representative Page profiles

### 2.1 Rich Content Page

适用于普通说明、正文、长期由运营人员自由编辑 HTML 的 Page。

Contract：

- primary content authority：operator-managed Runtime content（Fresh create / explicit adoption 可来自 Site Package）；
- content model：sanitized rich HTML；
- renderer：generic rich-content renderer；
- authoring：现有 mature rich-text editor；
- renderer 不因 alias/path 产生页面专属行为。

`about` 是代表样本。现有实现已经满足该 profile，不要求迁移。

### 2.2 Structured Page

适用于用户可感知的稳定 section/card/accordion/step 等结构本身属于产品语义，而内容仍应由运营/CMS 生命周期维护的 Page。

Contract：

- primary content authority：operator-managed structured Runtime data，Fresh create / explicit adoption 可由 Site Package 提供；
- content model：由 Technical Planning 冻结的结构化 sections/items/data contract；
- renderer：稳定 structured renderer identity；
- authoring：与结构模型一致的 Admin form / section editor；
- structured item 内部是否允许受控 rich body 等细节留给 Technical Planning，但 whole-page arbitrary `bodyHtml` 不再作为并行 primary authority；
- renderer 不能从 alias、URL 或正文 DOM 猜测。

### 2.3 Engineering Page profile

适用于页面真实产品行为需要 repository-owned implementation，例如定制业务交互、复杂状态、强工程级布局或无法由稳定 structured data + renderer contract 自然表达的能力。

Contract：

- primary behavior / presentation authority：repository-owned frontend engineering asset；
- renderer identity 必须显式绑定；
- CMS Page 仍拥有 stable identity / route / enabled-publish lifecycle；
- 若还存在 CMS 内容输入，必须作为显式 component data / metadata，而不能让 arbitrary `bodyHtml` 与 component hardcode 同时声称拥有正文；
- Engineering profile 不是独立的 generic content-model 枚举，也不是“特殊页面默认逃生口”；只有 representative evidence 证明 Structured 等普通 contract 不足时才选择。

本次 Planning 尚未发现一个必须立即实现的新 Engineering Page，因此该 profile 只冻结边界，不为了四分类完整性制造 implementation sample 或 slice。

### 2.4 Embedded / External integration profile

适用于正文/业务能力由 external system 或独立 external integration 持有的 Page。

Contract：

- primary content/behavior authority：external integration；
- CMS Page 持有 stable identity / route / lifecycle 与必要 integration metadata；
- renderer 使用显式 external integration identity；
- URL / iframe / other integration mechanism 的安全、SEO、responsive、navigation contract 必须在对应 integration Planning 中单独冻结；
- external/embed 是 ownership / integration profile，不是与 Rich / Structured 平行的 content model；
- placeholder 只是 lifecycle 状态，不等于长期 content model。

具体 Hui Employment integration 仍是独立 Planning Candidate，不由本 Specification 启动。

## 3. Renderer selection contract

非 generic Rich Content Page 必须有稳定、显式、可审计的 renderer binding。

本 Specification 冻结语义，不冻结字段名：

```text
Page metadata declares renderer identity
        ↓
Public Page runtime resolves registered renderer
        ↓
renderer consumes the content model allowed by that Page contract
```

必须满足：

- alias/path 只承担 stable URL / identity，不承担长期 renderer dispatch；
- renderer identity 不从 `bodyHtml` DOM shape 猜测；
- unknown / unsupported renderer identity **fail closed** 为明确不可渲染状态与可观测错误，不得静默 fallback 到 generic rich HTML；
- generic Rich Content 可以有显式默认 renderer identity，也可以由明确的 Rich content model contract 得到默认 renderer；具体编码由 Technical Planning 决定；
- Public Renderer 只执行已声明 contract，不成为 Site Definition / Page product authority。

## 4. Representative sample decisions

### 4.1 About — keep Rich Content

`about` 当前 Site Package formal default + operator-managed Runtime + generic rich-content renderer 已自然满足产品语义。

Decision：**保持 Rich Content，不迁移。**

### 4.2 Employment dispatch (`guide/jypq`) — Structured is the planning target

Evidence provenance 必须明确区分：

- Issue #137 当前 durable Planning Capture 记录 legacy acquisition surface `/jiuyepaiqian` 使用 `contentMode = GUIDE_CARDS`、selector `#blueTabContent`；
- 同一 capture 记录 source 下存在多个 `.card`，collector 单独读取 `.card-header .title` 与 `.card-body`，随后把每个 card 投影为 `<section><h2>title</h2>body</section>` 并输出 Rich Text；
- 当前 `main` 的 Site Package 直接证明 `guide/jypq` 仍保存上述 flattened rich HTML；
- 当前 Public Page 直接证明非 placeholder 内容统一走 generic `v-html`；
- 当前 E2E 直接验证正文 marker、4 张既有 package images 与资源可访问性，但不验证 legacy card structure / presentation；这些测试本身也**不能证明**存在某种特定 legacy interaction contract。

现有 evidence 足以证明丢失的是**重复、稳定、内容驱动的 card/section 结构与呈现语义**；同时没有 evidence 证明该 Page 需要独立业务状态、复杂 API orchestration、repository-hardcoded workflow 或其他必须由 Engineering Page 承担的行为。

Decision：**`guide/jypq` 以 Structured Page 作为进入 Technical Planning 的目标方向，而不是 Engineering Page。**

理由：

1. card title/body/resource 是运营内容，应继续由 CMS/operator lifecycle 维护；
2. 当前已证实的 gap 可以由 stable card collection + structured renderer 自然表达；
3. 直接 Engineering 化会在缺乏行为证据时不必要地把正文/结构 authority 转移到 repository code，并增加与现有 operator-content ownership 的冲突；
4. Structured renderer 能在保留 current canonical URL 与 CMS Page identity 的同时恢复 card structure / presentation，并为后续有 evidence 的有限交互保留显式扩展空间。

Stage Return condition：如果 Technical Planning、源站/Review evidence 或后续产品确认发现存在 stable structured card contract 无法自然覆盖的真实业务交互或 repository-owned behavior，则**停止当前 continuation，回到 Specification/Human Review 重新选择 Engineering 或其他方案**；不得在 implementation 中临时增加 alias/path 特判，也不得把新证据硬塞进已接受的 Structured contract。

### 4.3 FAQ — keep current accepted Rich Content for now

FAQ 当前 Human Review 已接受现有层级修复，当前没有开放显示 blocker。

Decision：

- Architecture 允许 FAQ 未来成为 Structured / Accordion；
- **本轮不迁移 FAQ**；
- 不为了证明 Structured profile 的通用性而制造额外 product change。

### 4.4 External integration — profile validated, implementation deferred

当前 Site Package 已存在 `EMBED_PLACEHOLDER` 的 external/fixed integration surface，证明 external ownership 与 CMS Page identity 共存是现实需求；placeholder 只证明 lifecycle/integration boundary，不被提升为新的 content model。

Decision：**Embedded / External integration profile 成立；具体 Hui Employment integration 继续独立 Planning。**

### 4.5 Engineering Page — boundary validated, no forced sample

当前 Requirement 与 Issue #137 已提供 Engineering boundary 的真实触发条件，但当前 representative evidence 没有要求立即把某个 Page 工程化。

Decision：**保留严格 Engineering boundary，不为了四类 profile 完整性强行创建或迁移 Page。** 后续只有真实 product/behavior evidence 证明 Rich/Structured/External contract 不足时才进入该 profile。

## 5. Ownership and adoption compatibility

新的 Page content architecture 必须继承 EU-49 / EU-52 的 operator ownership protection：

- Site Package 可以拥有 Fresh create default 与显式 adoption target；
- ordinary reconcile 不得覆盖 existing operator-managed content；
- Page 从 Rich Content 演进到 Structured / Engineering / External profile 时必须声明精确 migration/adoption precondition；
- operator divergence 必须 preserve + report；
- ownership transfer 必须显式、可审计、幂等；
- Main historical migration 不作为 Page architecture 变更的 silent fallback。

`guide/jypq` 当前 accepted Rich Content baseline 是迁移前产品事实，不能因为新的 Structured target 出现就无条件覆盖 Runtime。Technical Planning 必须把 prior-package baseline / adoption precondition 与 structured target 的 ownership transition 一起设计，而不是只设计 renderer。

## 6. URL and Replaceable Public Renderer compatibility

Current canonical public routes 保持：

```text
/page/{alias}
/page/{groupAlias}/{alias}
```

本 Architecture 不引入按 renderer type 的第二套路由，也不新增与 CMS Page identity 平行的目录。

`frontend/public-site` 可以增加 renderer registry / structured renderer module，但必须继续满足：

- consume Generic public API / stable Page contract；
- route-level Site boundary 继续由 Main Site router 拥有；
- renderer implementation 可以被未来替换；
- product content/identity semantics 不藏进 Vue route alias 特判。

## 7. Admin / API / persistence implications

Structured Page 会真实影响 Generic CMS domain / API / Admin authoring / persistence，具体实现尚未冻结。

Technical Planning 必须比较至少：

- structured content 是否作为 Page 新的 site-neutral payload/JSON contract，或拆成独立 site-neutral section/item entities；
- renderer identity 与 content model / ownership 的最小持久化表达；
- Admin 如何只暴露与当前 Page content model 匹配的 authoring surface；
- public DTO 如何保证 old Rich/placeholder Page compatibility；
- Flyway append-only schema evolution 与 existing data compatibility；
- Site Package create/adoption 如何声明 Structured target，并保留 operator-divergence guard。

上述是 Technical Planning 的比较问题，不代表本 Specification 已决定任何 DB schema、字段、API payload 或 Vue component 结构。

在这些问题完成 Technical Planning 前，不得执行 `slice-work` 形成 implementation EU。

## 8. Verification contract for the first implementation candidate

后续 Technical Planning 如果保持 `guide/jypq` Structured target，进入 readiness 前至少应定义并证明：

- Generic Rich Page regression：`about` 等 ordinary Page 仍走现有 rich renderer；
- Structured renderer dispatch 明确且 unknown identity fail closed；
- `guide/jypq` cards 的数量、title、body、顺序、4 张既有 package images 与 agreed structure/presentation contract 可自动验证；
- current `/page/guide/jypq` URL 稳定；
- existing operator-divergence/adoption protection 可验证；
- Admin 能维护 structured card content，而不是退回 whole-page arbitrary HTML；
- API / persistence backward compatibility；
- automated Browser verification 先通过，再执行 bounded Human Review。

具体 card visual acceptance 需要在 Technical Planning / Human Review 中用 current product evidence 冻结；如果未来要声明某种 interaction requirement，也必须先取得具体 source / product evidence。本 Specification 不凭空发明像素值、动画或交互行为。

## 9. Explicit non-decisions

本 Specification 尚未决定：

- 具体 DB schema；
- 字段是否叫 `contentType` / `rendererKey` / `ownership`；
- structured payload 是否 JSON 或 normalized entities；
- renderer registry 的具体 TypeScript API；
- Admin structured editor 组件实现；
- FAQ 迁移时机；
- Hui Employment integration；
- 新 EU 编号、slice 数量或 Execute timing。

## 10. Human Review conclusion and current gate

Human Review 对 Requirement / Specification 的产品与架构方向结论为 **ACCEPT**：

- 当前 product gap 真实且没有被夸大为已证实的复杂 interaction；
- Content Model / Renderer Identity / Content Ownership 保持正交，代表 profiles 不等于新的混合 `renderMode`；
- `guide/jypq` 的 Structured target 有足够 evidence 进入 Technical Planning，同时保留严格 Stage Return；
- renderer identity / unknown fail-closed / Replaceable Public Renderer boundary 明确；
- EU-49 / EU-52 ownership 与 adoption protection 未被绕过；
- DB/schema/API/Admin/Vue/EU 数量与 execution timing 均未提前冻结。

因此本 Specification 当前自然责任可以进入 **Technical Planning**，但 Technical Planning 仍是 Planning lifecycle 的下一阶段，不是 Execution Unit：

- Technical Planning：**NOT STARTED**；
- `slice-work`：**NOT AUTHORIZED / NOT STARTED**；
- Candidate / Ready Execution Unit：**NONE**；
- `docs/work/current/README.md`：继续保持 `NONE`；
- Readiness：**NOT STARTED**；
- Execute Authority：**NONE**。

本 Specification 的 Human Review 接受或 PR integration 都不得被解释为自动授予 successor Execute Authority。
