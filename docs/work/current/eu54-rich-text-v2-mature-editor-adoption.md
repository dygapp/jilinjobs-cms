# EU-54 — Rich Text V2 Mature Editor Adoption

## Status

- Parent Planning Authority: GitHub Issue #60 / B3 V2 re-entry
- Requirement: `docs/requirements/rich-text-authoring.md`
- Specification: `docs/specifications/rich-text-authoring.md`
- Technical Plan: `docs/technical/rich-text-authoring-plan.md`
- Selection Evidence: closed/unmerged PR #135 + Issue #60 Current Evidence
- Planning baseline: `main@6069e493c5a330ab3a53f55fd31cccc5b14b043d`
- Candidate state: **FORMED**
- Readiness: **PENDING AFTER PLANNING INTEGRATION**
- Execute Authority: **NOT GRANTED**

## 1. Intent

用成熟 SunEditor 替换当前项目自组 Tiptap editor，同时纠正与其耦合的窄 HTML policy，使 Article INTERNAL / Page RICH_TEXT 回到统一、可维护的标准 HTML `bodyHtml` contract。

本 Unit 是一次纵向 contract replacement，不是单纯 npm dependency swap。

## 2. Why one Unit

Editor output、Backend write policy、Public defensive output、legacy HTML compatibility和 Article managed image association共享同一个 `bodyHtml` contract。如果拆成多个 Unit，任一中间状态都可能再次出现“editor可表达、Backend会删除”或“Backend允许、Public无法展示”的错位。

因此 `slice-work` 只形成本单一 Candidate。

## 3. Scope

- `frontend/admin`：SunEditor 3.3.3 core + zh_cn + thin Vue adapter；
- 删除不再使用的 Tiptap runtime dependencies和项目自建完整 toolbar逻辑；
- Article INTERNAL：保留 managed image upload / `bodyImageResourceIds` association；
- Page RICH_TEXT：消费 shared editor，不新增 Page Resource domain；
- Backend：重构 shared `RichTextHtmlPolicy` 为 compatibility-first成熟 sanitizer profile；
- Article/Page write与Public defensive read保持 shared policy；
- P1 Party star / P2 teacher-library / ordinary content / hostile payload regression；
- WPS / IME integration verification；
- exact-head CI + bounded Human Review + Post-Integration closure。

## 4. Explicit Non-goals

- Page Content Architecture、Structured / Engineering / Embed Page；
- `就业派遣`特殊页实现；
- Page Resource association；
- inline attachment model redesign；
- Main historical migration reactivation；
- 全库 `bodyHtml` rewrite；
- 双 editor runtime；
- grammar / typo / sensitive-word / AI features；
- Microsoft Word真实 paste作为 blocking Gate。

## 5. Accepted Technology Evidence

PR #135 已完成隔离自动 PoC：

- Jodit 4.15.0：P1/P2/P3/P4a/P5 PASS，existing HTML保真更高；
- SunEditor 3.3.3：使用薄 compatibility config后 P1/P2/P3/P4a/P5 PASS；Office-shaped paste自动清理更强；
- 两者 managed resource thin adapter均可行。

Human Review：

- Windows中文 IME：两者无有意义差异；
- 真实 WPS paste：Jodit部分标题字体更接近原文；SunEditor整体格式控制略优；
- 一次 paste Undo：SunEditor一次即可整体回退；Jodit需要多次；
- Word当前无环境，未验证且不伪报。

Selection：**SunEditor 3.3.3 primary / Jodit 4.15.0 verified fallback**。

## 6. Acceptance Mapping

| Obligation | EU-54 responsibility |
|---|---|
| RT2-01～03 | SunEditor core、thin Vue adapter、中文/authoring behavior |
| RT2-04～08 | HTML semantic contract、real corpus、built-in UI、compat config |
| RT2-09～10 | Article managed image bridge；保持 attachment / Page边界 |
| RT2-11～14 | compatibility-first sanitizer + minimum active-content boundary + Public independence |
| RT2-15～16 | no bulk rewrite + V1 loss boundary |
| RT2-V01～V07 | build、browser、WPS、resource、compatibility、安全、full regression |

## 7. Readiness Check — PENDING

Planning Authority必须先集成到 `main`。新的 Fresh Context在进入 Execute前必须重新确认：

### Authority

- V2 Requirement = confirmed；
- V2 Specification / Technical Plan = ready；
- 本 Candidate仍是 Current Planning Candidate。

### Base / drift

- 重新读取最新 main；
- 检查 `RichTextEditor.vue`、Admin dependencies、Article/Page consumers、`RichTextHtmlPolicy`、ArticleService/PageService是否发生影响本 Unit的 drift；
- 检查是否存在新的 implementation PR / branch。

### Dependency

- PR #135必须保持 closed / unmerged，仅作为 Evidence；
- Main historical migration必须保持冻结；
- Party migration不因本 Unit重新规划；
- 不存在需要先完成的 Page Architecture dependency。

### Technology

- SunEditor 3.3.3精确版本仍可从正常 package registry安装；
- MIT/open-source-free evidence无变化；
- 不主动升级到未经 PoC/Human Review验证的新 editor patch。

### Verification

- 当前 Admin / Backend / Public / Integrated Browser验证路径仍可用；
- Review Environment能提供最终 bounded Human Review；
- P1/P2 Repository corpus仍存在。

只有上述 Gate全部 PASS，才将 Readiness改为 PASS并以当时最新 main记录 Execute baseline。

## 8. Execution Order after Readiness PASS

1. Admin dependency + thin SunEditor adapter；
2. Article/Page consumer integration；
3. shared Backend/Public HTML policy correction；
4. focused P1/P2 + resource + hostile tests；
5. Admin/Public browser behavior；
6. full exact-head verification；
7. Review Environment + bounded Human Review；
8. Integration + Post-Integration closure。

步骤可在同一 implementation PR内连续推进，不为每一步创建新的 EU / PR。

## 9. Stop Conditions

命中 Specification Stop Condition，或出现重复低信息修复循环时，停止机械重试并报告 Evidence。不得为了继续执行而：

- 重新造 editor core；
- 把 SunEditor完整 content model复制到 Backend；
- 把 Public Renderer整体绑定到 editor UI CSS；
- 静默切换到 Jodit；
- 覆盖 operator-diverged persisted content。

## 10. Completion Gate

- Readiness PASS并有精确 Execute baseline；
- implementation PR exact-head全部 required gates PASS；
- P1/P2、resource、active-content regression PASS；
- Windows + WPS bounded Human Review PASS；
- implementation集成 main；
- Post-Integration verification PASS；
- Work artifact归档、Issue #60 Current Evidence回写；
- Execute Authority TERMINATED，Current Ready Execution Unit返回 NONE。
