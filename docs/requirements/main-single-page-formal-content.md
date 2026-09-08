# Main Single-page Formal Content Requirement

## Status

- Parent Planning Authority: GitHub Issue #60 / E2
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Planning baseline: `main@f42bacf4ab7719e3291288c77f0685b428b86141`
- Requirement: **READY**
- Current Ready Execution Unit: **NONE until slice-work + readiness-check**

## 1. Intent

E2 要把 Main 当前稳定 Page / PageGroup 从“结构已存在、部分正文仍是占位”推进为可安全承载正式内容的长期模型。

目标不是把 Page 变成 Site Package 不可编辑文档，也不是把所有单页改成代码页面；而是保持：

- Site Package owns stable Page identity / required Site structure；
- Runtime Page content is operator-maintainable；
- 从原站或其他 accepted source获得的正式 Page content通过独立 canonical migration lifecycle进入 Runtime；
- Public `/page/**` contract继续不依赖当前 Vue实现。

## 2. Current scope classification

Current Site Package已经声明稳定 Main Page identities：

- standalone: `about`、`budget`、`teacher-library`、`live-course`、`employment-report-contact`；
- `guide/*`: `jypq`、`dagl`、`dygl`、`xlrz`、`contact`、`faq`；
- `jobs/*`: `positions`、`recruitment`、`jobfair`、`presentation`、`jilin`。

E2 不在缺少source evidence时伪造正式正文。Current `RICH_TEXT` Page是正式运营内容候选；current `EMBED_PLACEHOLDER` Page继续保持既有placeholder / fixed-integration边界，是否转换为真实integration或rich text必须由后续source/Product evidence证明。

## 3. Stable structure vs operational content

### 3.1 Site Package ownership

Site Package继续负责Page stable existence / logical target以及current package-owned structural metadata。

对于**已存在**的preset Page，ordinary Site Package reconcile不得覆盖以下operator-owned content fields：

- `bodyHtml`；
- `renderMode`；
- `embedUrl`。

Fresh first provision可以使用`pages.json`当前值创建Page，使未执行canonical migration的Fresh Site仍有可预测初始状态；但这些create-time defaults不得变成后续restart的内容overwrite authority。

本 Requirement不扩大为所有preset对象operational field治理；只解决E2 formal Page content所直接依赖的content fields。

### 3.2 Runtime editing

Current Admin/Core Page edit contract保持：

- preset Page alias继续受稳定身份保护；
- Page正文与呈现字段可以按current validation / sanitizer维护；
- E2 foundation不新增发布状态、版本历史、Page Resource association或新的Page类型。

## 4. Canonical Page content migration

Generic Content Migration必须增加site-neutral Page content能力，供Main正式Page与未来真实consumer复用。

Canonical Page unit至少表达：

- source system + stable legacy key；
- source URL / provenance；
- stable target `groupAlias? + pageAlias`；
- `bodyHtml`、`renderMode`、optional `embedUrl`；
- source fingerprint；
- first-apply target precondition fingerprint；
- body引用的repository-frozen resource evidence（若有）。

不得使用Runtime page id、Vue route name或JilinJobs-specific alias allow-list作为Generic schema。

## 5. Safe first-apply contract

Page与Article不同：Page target在migration前已经由Site Package创建，因此Generic Page migration不能用“mapping不存在即无条件CREATE”语义。

First apply必须：

1. stable Page target可唯一解析；
2. canonical record提供expected target-content fingerprint；
3. Runtime当前`bodyHtml + renderMode + embedUrl`与该precondition完全匹配；
4. 无既有Page migration mapping；
5. 全dataset structural/resource/target preflight无INVALID/CONFLICT；
6. 才允许更新Page content并写入migration mapping。

任何未知operator edit / target drift都必须CONFLICT，不能静默覆盖。

## 6. Re-run / operator ownership

Accepted first apply后：

- mapping记录`sourceSystem + legacyKey + sourceFingerprint + page target`；
- 同一source fingerprint再次运行 = **SKIP**；
- SKIP不得把migration后operator edit重新覆盖回canonical bytes；
- 同identity但新source fingerprint默认 = **CONFLICT**，除非未来Requirement明确建立受控content upgrade contract；
- 因此canonical migration是受控一次性/显式导入，不成为Runtime持续reconcile owner。

这与Site Package ordinary reconcile的no-content-overwrite共同保证：正式Page导入后回到普通operator-managed lifecycle。

## 7. Page resource boundary

Page RICH_TEXT当前没有Article式Resource association。若canonical Page正文包含历史资源：

- source bytes必须进入repository-owned canonical unit并校验size/SHA-256；
- Generic migration可以使用site-neutral deterministic historical static projection并rewrite正文reference；
- target不得进入`sites/jilinjobs/assets/**` stable Site asset ownership，也不得伪装成Article Resource association；
- `/static/uploads/**`仍属于mutable Runtime upload store。

具体static path与token form由Technical Plan冻结。

## 8. Formal content evidence

E2 foundation本身不接受/编造Main真实正文。Actual formal content必须在后续source discovery / evidence promotion后满足：

- source provenance可追溯；
- target stable Page identity明确；
- 页面类型（RICH_TEXT / existing placeholder / fixed integration）分类明确；
- body / external resource evidence完整；
- accepted snapshot进入repository后才成为长期Authority。

实际内容采集可与E3 Main source discovery共享evidence，但Page scope必须遵守本Requirement。

## 9. Verification requirements

Foundation进入Integration前至少证明：

1. existing preset Page operator修改`bodyHtml/renderMode/embedUrl`后ordinary Site Package reconcile不overwrite；
2. Fresh provision仍创建完整Page targets；
3. synthetic Generic Page canonical fixture first apply成功；
4. second same import SKIP且不overwrite post-import operator edit；
5. wrong target precondition / missing target / duplicate identity / fingerprint drift = CONFLICT或INVALID且no mutation；
6. body resource bytes/path/digest验证与reference rewrite成立（fixture含resource时）；
7. Article/ListItem Generic behavior、Party migration compatibility、Site Package verification与Repository CI保持；
8. no Public/Admin API contract or visible page layout change。

## 10. Non-goals

- 不在foundation里采集Main真实页面；
- 不决定`live-course`/jobs placeholders的新产品集成方案；
- 不增加Page publish workflow/version history；
- 不增加Page Resource association；
- 不改变Page public URL；
- 不让Site Package持续拥有正式正文；
- 不修改Party canonical data；
- 不进入E3 full Main migration execution。

## 11. Requirement readiness

Current Repository已经直接暴露：stable Main Page identities、Admin可编辑Page content、Site Package content overwrite冲突，以及Generic Migration缺少Page consumer的事实。长期ownership、failure boundary与no-overwrite目标均可由current accepted Authority推导，不需要新的Product Intent。

本 Requirement **READY**；需要Ready Specification与Technical Plan后再执行`slice-work → readiness-check`。
