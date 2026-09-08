# Main Historical Content Collection & Canonical Migration Requirement

## Status

- Parent Planning Authority: GitHub Issue #60 / E3
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Upstream: E1 external-link boundary; E2 Page formal-content boundary
- Initial planning baseline: `main@f42bacf4ab7719e3291288c77f0685b428b86141`
- Current E3 planning baseline: `main@e6fe7674398ad8c29fa7ff1d62eb500754a66cc8`
- Requirement: **READY**
- Specification: **READY**
- Technical Plan: **READY** — `docs/technical/main-historical-content-migration.md`
- Current Ready Execution Unit: **EU-50**
- Downstream Candidate: **EU-51 / Readiness PENDING on EU-50 accepted snapshot**

## 1. Intent

E3 将 Main 原站仍属于当前产品范围的历史运营内容与正式固定页内容，从 Legacy Source 发现、收集、规范化并晋升为 Consumer-owned Canonical Migration Dataset，再通过 Generic Content Migration进入CMS Runtime。

E3不是一次性网页抓取脚本任务。长期结果必须是repository-owned canonical data / evidence / verifier；Legacy Source只在 discovery / collection 阶段使用，Stable CI与Runtime import不得重新依赖外网。

## 2. Source boundary

Current Requirement authority把 `www.jilinjobs.cn` 及其当前指向的原站内容源作为 Main视觉/内容分析参考。E3 discovery必须从届时current source evidence重新确认实际reachable source与redirect，而不能把本Planning文件中的域名描述当成永恒Runtime contract。

Collector可以访问Legacy Source；canonical verification/import不得访问Legacy Source。

## 3. Discovery scope

E3必须系统发现并分类Main当前产品范围内至少以下surface：

- Main Columns及完整pagination中的Article / EXTERNAL_LINK Article；
- Main Page / PageGroup对应的正式内容来源；
- 首页轮播等historical operational list membership；
- 网站导航/友情链接等historical list membership；
- 正文图片、附件、Page body resource；
- source URL、detail/list path、source order、publish metadata；
- unresolved / unsupported / duplicate candidate。

不得只抓第一页或只依赖当前首页可见条目。

## 4. Classification contract

E1 accepted ownership决定external target分类：

- Column content → INTERNAL / EXTERNAL_LINK Article；
- historical operational list member → CmsListItem；
- stable Navigation structure → Site Package，不进入E3；
- fixed integration → engineering asset，不进入E3；
- unresolved role → unresolved evidence，不静默晋升。

E2 accepted Page boundary决定Page分类：

- stable Page identity由Site Package提供；
- source-derived formalPage body属于canonical content；
- current EMBED_PLACEHOLDER不在没有新Product evidence时被collector自动改成真实iframe/integration。

## 5. Accepted snapshot

Raw discovery output只是Evidence Candidate。只有经过repository review/promotion后，accepted snapshot才成为长期migration input。

Promotion至少冻结：

- collection timestamp / source root；
- discovery completeness report；
- accepted / unresolved / excluded counts与理由；
- source bytes或必要raw evidence digest；
- canonical manifest/index/item fingerprints；
- resource SHA-256 / size；
- exact target stable identity；
- human decision for ambiguous classification。

E3 Requirement不预先写死Article/Page/List item数量；accepted counts只能来自实际source evidence。

## 6. Canonical organization

Main默认复用`data-migrations/README.md`与Generic Engine的site-neutral组织，建议root：

```text
data-migrations/main/v1/
├── manifest.json
├── index.ndjson
├── articles/**
├── pages/**
├── lists/**
├── reports/**
└── source-discovery/**   # only durable promoted evidence as justified
```

不得复制Party alias/fixed count/compatibility assumptions。

## 7. Stable identity / fingerprint

- Article/List/Page source identity使用`sourceSystem + legacyKey`；
- target依赖Column alias、List code、Page `(groupAlias? + alias)`等stable Site identity；
- canonical record包含source fingerprint；
- resources包含size + SHA-256；
- Runtime DB id不进入canonical authority；
- URL/router/component name不是target identity。

## 8. Import / reconciliation

E3 import复用Generic Content Migration：

- Article：EU-47 accepted generic semantics；
- ListItem：EU-47 accepted generic semantics；
- Page：E2 foundation accepted generic Page semantics；
- E1 external classification决定Article/List target ownership。

Fresh canonical import必须在Generic Schema + JilinJobs stable Site Package target ready之后执行。

First accepted import、second idempotent import、fingerprint conflict与Runtime reconciliation必须可独立验证。

E3不新增Main-specific importer，除非真实source shape存在无法由bounded normalization处理且有明确长期价值的证据；任何Main adapter都不得复制Generic Runtime mutation pipeline。

## 9. Human review

最终Main迁移不仅需要结构/count验证，还必须对代表性和高风险内容执行Human Review，至少覆盖：

- 首页/栏目列表是否显示正确条目与顺序；
- INTERNAL正文可读、图片/附件可访问；
- EXTERNAL_LINK目标与title/source一致；
- Page/PageGroup正式正文、Tab与public URL正确；
- historical list links / carousel目标与图片正确；
- 乱码、空正文、异常HTML、broken resource、明显duplicate/unresolved不存在或有接受记录；
- Main视觉主基线无migration-induced regression。

## 10. Verification requirements

进入最终Integration前至少证明：

1. discovery遍历complete pagination / known source surfaces；
2. discovered = accepted + excluded + unresolved reconciliation成立；
3. unresolved在accepted promotion前为0，或每项有明确人工接受的defer/exclude记录；
4. canonical schema/fingerprint/resource digest PASS；
5. Fresh DB import PASS；
6. second import SKIP/idempotent；
7. changed fingerprint CONFLICT/no silent overwrite；
8. Page target precondition与operator ownership contract成立；
9. Runtime count/identity/resource reconciliation PASS；
10. Public/Admin/Integrated Browser + Human Review PASS；
11. stable CI不访问Legacy Source。

## 11. Non-goals

- 不把Main内容写入Flyway；
- 不把historical members写入stable Site structure；
- 不把历史内容变成Site bootstrap；
- 不复制Party-specific importer/policy；
- 不修改Public frontend technology；
- 不在没有source evidence时决定新external indicator/iframe/product behavior；
- 不更新`agentic-dev` baseline。

## 12. Requirement readiness

E1/E2已能定义classification与target ownership；EU-47/E2 foundation定义Generic runtime migration contract；`data-migrations/README.md`定义canonical lifecycle。E3的Product Goal、source-discovery completeness、promotion、migration与verification obligations均可以在未知最终count的情况下明确。

本 Requirement **READY**。Exact accepted snapshot/count属于后续Evidence，不是EU-50 Readiness blocker。E2 / EU-49 已完成，Specification与Technical Plan已Ready，`slice-work`现形成EU-50 / EU-51；其中EU-50通过`readiness-check`，EU-51必须等待EU-50 accepted snapshot集成后再基于实际数据重新执行Readiness。
