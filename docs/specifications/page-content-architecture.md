# Page Content Architecture Specification

## Authority

- `docs/requirements/page-content-architecture.md`
- GitHub Issue #137 — Planning Capture / evidence source only
- GitHub Issue #77 — CMS Core / Site Package / Historical Migration / Replaceable Public Renderer boundary
- `docs/requirements/main-single-page-formal-content.md`
- `docs/specifications/main-single-page-formal-content.md`
- ADR-0002 — Public Site Multi-entry Modular SPA

## Status

- Planning baseline: `main@881893f9523effaabb82e0a0dacfcd38fe0c44fe`
- Specification: **CANDIDATE / HUMAN REVIEW REQUIRED**
- Representative-sample validation: **SUFFICIENT FOR ARCHITECTURE REVIEW**
- Technical Planning: **NOT STARTED**
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

## 2. Accepted conceptual Page categories

### 2.1 Rich Content Page

适用于普通说明、正文、长期由运营人员自由编辑 HTML 的 Page。

Contract：

- primary content authority：operator-managed Runtime content（Fresh create / explicit adoption 可来自 Site Package）；
- content model：sanitized rich HTML；
- renderer：generic rich-content renderer；
- authoring：现有 mature rich-text editor；
- renderer 不因 alias/path 产生页面专属行为。

`about` 是代表样本。现有实现已经满足该类别，不要求迁移。

### 2.2 Structured Page

适用于用户可感知的稳定 section/card/accordion/step 等结构本身属于产品语义，而内容仍应由运营/CMS 生命周期维护的 Page。

Contract：

- primary content authority：operator-managed structured Runtime data，Fresh create / explicit adoption 可由 Site Package 提供；
- content model：由 Technical Planning 冻结的结构化 sections/items/data contract；
- renderer：稳定 structured renderer identity；
- authoring：与结构模型一致的 Admin form / section editor；
- arbitrary `bodyHtml` 不再作为该 Page 的并行 primary authority；
- renderer 不能从 alias、URL 或正文 DOM 猜测。

### 2.3 Engineering Page

适用于页面真实产品行为需要 repository-owned implementation，例如定制业务交互、复杂状态、强工程级布局或无法由稳定 structured data + renderer contract 自然表达的能力。

Contract：

- primary behavior / presentation authority：repository-owned frontend engineering asset；
- renderer identity 必须显式绑定；
- CMS Page 仍拥有 stable identity / route / enabled-publish lifecycle；
- 若还存在 CMS 内容输入，必须作为显式 component data / metadata，而不能让 arbitrary `bodyHtml` 与 component hardcode 同时声称拥有正文；
- Engineering Page 不是“特殊页面默认逃生口”。只有 representative evidence 证明 Structured 不足时才选择。

本次 Planning 尚未发现一个必须立即实现的新 Engineering Page，因此该类别只冻结边界，不形成首个 implementation slice。

### 2.4 Embedded / External Page

适用于正文/业务能力由 external system 或独立 external integration 持有的 Page。

Contract：

- primary content/behavior authority：external integration；
- CMS Page 持有 stable identity / route / lifecycle 与必要 integration metadata；
- renderer 使用显式 embed/external integration identity；
- external URL / iframe / other integration mechanism 的安全、SEO、responsive、navigation contract 必须在对应 integration Planning 中单独冻结；
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
- generic Rich Content 可以有显式默认 renderer identity，也可以由明确的 Rich content model contract 得到默认 renderer；具体编码由 Technical Planning决定；
- Public Renderer 只执行已声明 contract，不成为 Site Definition / Page product authority。

## 4. Representative sample decisions

### 4.1 About — keep Rich Content

`about` 当前 Site Package formal default + operator-managed Runtime + generic rich-content renderer 已自然满足产品语义。

Decision：**保持 Rich Content，不迁移。**

### 4.2 Employment dispatch (`guide/jypq`) — Structured is the planning target

Current repository evidence establishes：

- source acquisition 明确把 `/jiuyepaiqian` 标为 `GUIDE_CARDS`，selector 为 `#blueTabContent`；
- source 下存在多个 `.card`，collector 单独读取 `.card-header .title` 与 `.card-body`；
- collector 在多个 card 时把每个 card 主动转成 `<section><h2>title</h2>body</section>`，随后输出 `renderMode = RICH_TEXT`；
- current Site Package 因此只保留 flattened rich HTML；
- current Public Page 对非 placeholder 内容统一走 generic `v-html`；
- current E2E 验证正文 marker、4 张图片与可访问性，但没有验证 card structure / interaction / presentation。

当前可见 evidence 说明丢失的是**重复、稳定、内容驱动的 card/section 结构语义**；没有 repository evidence 证明该 Page 需要独立业务状态、复杂 API orchestration 或 repository-hardcoded workflow。

Decision candidate：**`guide/jypq` 进入 Structured Page，而不是 Engineering Page。**

理由：

1. card title/body/resource 是运营内容，应继续由 CMS/operator lifecycle 维护；
2. 丢失的主要语义可以由稳定 card collection + structured renderer 表达；
3. 直接 Engineering 化会不必要地把正文/结构 authority 转移到 repository code，并增加与现有 operator-content ownership 的冲突；
4. Structured renderer 能在保留 current canonical URL 与 CMS Page identity 的同时恢复 card presentation / interaction contract。

Fail-closed condition：如果 Technical Planning 或新的 representative evidence 证明原产品存在 structured card contract 无法覆盖的真实业务交互，则**停止当前 continuation，回到 Specification/Human Review 重新选择 Engineering 或其他方案**；不得在 implementation 中临时 alias 特判。

### 4.3 FAQ — keep current accepted Rich Content for now

FAQ 当前 Human Review 已接受现有层级修复，当前没有开放显示 blocker。

Decision：

- Architecture 允许 FAQ 未来成为 Structured / Accordion；
- **本轮不迁移 FAQ**；
- 不为了证明 Structured model 的通用性而制造额外 product change。

### 4.4 External integration — category validated, implementation deferred

现有 placeholder/fixed integration Page 证明 external ownership 是真实类别，但当前没有必要为了本 Planning 启动 Hui Employment integration。

Decision：**Embedded / External category成立；具体 integration 继续独立 Planning。**

## 5. Ownership and adoption compatibility

新的 Page content architecture 必须继承 EU-49 / EU-52 的 operator ownership protection：

- Site Package 可以拥有 Fresh create default 与显式 adoption target；
- ordinary reconcile 不得覆盖 existing operator-managed content；
- Page 从 Rich Content 演进到 Structured / Engineering / External 时必须声明精确 migration/adoption precondition；
- operator divergence 必须 preserve + report；
- ownership transfer 必须显式、可审计、幂等；
- Main historical migration 不作为 Page architecture 变更的 silent fallback。

`guide/jypq` 当前 accepted Rich Content baseline 是迁移前产品事实，不能因为新的 Structured target 出现就无条件覆盖 Runtime。

## 6. URL and Replaceable Public Renderer compatibility

Current canonical public routes保持：

```text
/page/{alias}
/page/{groupAlias}/{alias}
```

本 Architecture 不引入按 renderer type 的第二套路由，也不新增与 CMS Page identity 平行的目录。

`frontend/public-site` 可以增加 renderer registry / structured renderer module，但必须继续满足：

- consume Generic public API / stable Page contract；
- route-level Site boundary继续由 Main Site router拥有；
- renderer implementation可以被未来替换；
- product content/identity semantics不藏进 Vue route alias 特判。

## 7. Admin / API / persistence implications

Structured Page 会真实影响 Generic CMS domain / API / Admin authoring / persistence，具体实现尚未冻结。

Technical Planning 必须比较至少：

- structured content 是否作为 Page 新的 site-neutral payload/JSON contract，或拆成独立 site-neutral section/item entities；
- renderer identity 与 content model / ownership 的最小持久化表达；
- Admin 如何只暴露与当前 Page content model 匹配的 authoring surface；
- public DTO 如何保证 old Rich/placeholder Page compatibility；
- Flyway append-only schema evolution 与 existing data compatibility；
- Site Package create/adoption 如何声明 Structured target，并保留 operator-divergence guard。

在这些问题完成 Technical Planning 前，不得执行 `slice-work` 形成 implementation EU。

## 8. Verification contract for the first implementation candidate

后续 Technical Planning 如果保持 `guide/jypq` Structured target，进入 readiness 前至少应定义并证明：

- Generic Rich Page regression：`about` 等 ordinary Page仍走现有 rich renderer；
- Structured renderer dispatch 明确且 unknown identity fail closed；
- `guide/jypq` cards 的数量、title、body、4 张既有 package images 与交互/展示 contract可自动验证；
- current `/page/guide/jypq` URL稳定；
- existing operator-divergence/adoption protection可验证；
- Admin 能维护 structured card content，而不是退回 arbitrary HTML；
- API / persistence backward compatibility；
- automated Browser verification先通过，再执行 bounded Human Review。

具体 card visual/interaction acceptance 需要在 Technical Planning / Human Review 中用 current product evidence冻结；本 Specification 不凭空发明像素值或动画行为。

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

## 10. Current gate

Requirement intent 与 representative samples 已足够提出上述 Architecture candidate，但它包含新的 Page content model / renderer binding / ownership contract，属于显著结构性方向。

因此当前自然 Gate 是：**Human Review of Specification candidate**。

在 Human Review 接受前：

- Technical Planning 不开始；
- 不执行 `slice-work`；
- 不创建 Candidate / Ready Execution Unit；
- `docs/work/current/README.md` 保持 `NONE`；
- 不授予任何 Execute Authority。
