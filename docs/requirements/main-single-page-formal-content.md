# Main Single-page Formal Content Requirement

## Status

- Parent Planning Authority: GitHub Issue #60 / Main Page Site Package follow-up
- Architecture Authority: GitHub Issue #77
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Planning baseline: `main@25e452ee3ce66c2d7da1000ad55e9570d732528a`
- Requirement: **READY**
- Candidate: Main Page Formal Content Package Adoption
- Implementation lifecycle: **COMPLETED via EU-52 / Execute Authority TERMINATED**

## 1. Intent

Main 当前稳定 Page identity 已由 JilinJobs Site Package 持有，EU-49 又明确了 existing Page 的 `bodyHtml / renderMode / embedUrl` 属于 operator-maintainable Runtime content。EU-50 随后取得了 10 个稳定 Main Page 的真实 source handoff，并证明这些 Page 的正式内容与稳定资源应回到 Site Package，而不是继续进入 Historical Migration fallback。

本 Requirement 的目标是把这 10 个已接受 source Page 收敛为 JilinJobs Site Package 的正式默认内容，并建立一次明确、可审计、不会覆盖 operator edit 的 package content adoption 语义，使 Fresh Site 与仍停留在旧 package baseline 的 Existing Site 都能安全获得正式 Page 内容。

## 2. Accepted Page scope

本次只接受 EU-50 source handoff 中已经绑定稳定 target 的 10 个 `RICH_TEXT` Page：

- standalone: `about`、`budget`、`teacher-library`、`employment-report-contact`；
- `guide/*`: `contact`、`dagl`、`faq`、`dygl`、`jypq`、`xlrz`。

不把 `jobs/*`、`live-course` 等 placeholder / fixed integration Page 自动改造成 rich text；没有 source handoff 的 Page 不在本次范围。

## 3. Ownership contract

### 3.1 Site Package owns formal defaults and stable resources

对于上述 10 个 Page：

- stable Page identity / required structure继续由 Site Package持有；
- accepted formal `bodyHtml / renderMode / embedUrl` 作为 JilinJobs Site Package 的正式 create-time default 固化到 `sites/jilinjobs/structure/pages.json`；
- 正文引用且被接受为稳定产品资源的 bytes 固化到 `sites/jilinjobs/assets/pages/**`，并纳入既有 Site Package asset manifest / digest / protected-path / runtime projection；
- Runtime URL 使用 `/static/pages/**`；不得保留 legacy `/group1/cms/**` 作为产品运行依赖；
- 这些 Page bytes/resources 不进入 `data-migrations/**` canonical historical content，也不使用 Generic Page migration mapping 作为 Main 的长期 ownership。

EU-49 建立的 Generic Page migration capability继续作为 Generic capability保留，但不是本次 Main Page formal-content 的交付路径。

### 3.2 Runtime remains operator-maintainable

Site Package 正式正文不是持续 overwrite authority。完成 create/adoption 后：

- operator仍可通过当前 Page domain/Admin能力维护 `bodyHtml / renderMode / embedUrl`；
- ordinary reconcile不得把 operator edit恢复成 package bytes；
- 后续如果 package 正文再次变化，必须再次有显式、版本化的 adoption precondition；不得把本次 adoption 变成永久覆盖规则。

## 4. Guarded content adoption

现有 EU-49 ordinary reconcile只在 Page首次创建时写 package content，existing Page始终保留 Runtime content。本次必须在不破坏该 guard 的前提下增加显式 adoption contract。

每个需要把旧 package default 升级为正式内容的 Page，Site Package必须声明一个精确的旧 content baseline fingerprint。fingerprint只覆盖：

- `bodyHtml`；
- `renderMode`；
- `embedUrl`。

Existing Page 的处理语义：

1. 当前 content 已等于新 package target：no-op；
2. 当前 content fingerprint 精确等于声明的旧 package baseline：允许一次性 adoption 到新 formal content；
3. 当前 content 与两者都不同：视为 operator/content divergence，**保留当前 content，不覆盖，并在 machine-readable provisioning report 中明确报告 Page identity**；
4. structural fields继续按当前 Site Package contract正常 reconcile；
5. adoption成功后重复运行必须幂等；后续 operator edit必须继续受 EU-49 guard保护。

不得通过名称相似、DOM猜测、时间戳、空值或其他启发式方式认领 operator content。

## 5. Source evidence and provenance

正式 Page 内容来自 EU-50 已接受 attempt-3 source evidence：

- workflow run: `34303771704`；
- artifact: `10086056781` / `eu50-main-retry-attempt-3-a96cee22449508f92f3c89789f99aad477286a66`；
- artifact digest: `sha256:66118e4f21bf7644db1c97e2a631eee5d4902410f167606286e1280293620494`；
- source Head: `a96cee22449508f92f3c89789f99aad477286a66`；
- durable handoff index: `data-migrations/main/v1/reports/site-package-handoff.json`。

Artifact只承担 bounded source evidence，不成为长期产品运行依赖。执行时必须重新校验 artifact availability + digest；若 artifact 已过期/不可取得，不得静默换源，当前 Readiness必须视为 stale，并先通过新的显式 bounded Page source-acquisition evidence恢复同等 provenance。

## 6. Resource acceptance

EU-50 handoff中的已下载 Page resources必须逐项按 source evidence 的 path/size/SHA-256核验后，才可转成 package-owned bytes。

`budget` Page 的 13 个 PDF 采用 Issue #77 最新 Human Authority：

- 13 个 PDF 全部为 package-owned stable assets；
- 8 个 `zhjy.jilinjobs.cn:8080/group1/cms/**` 引用是 legacy absolute-source residue，可通过该 Human Authority授权的 bounded normalization重新取得；
- 最终全部使用有意义的稳定文件名放入 `sites/jilinjobs/assets/pages/budget/**`；
- 正文只引用 `/static/pages/budget/**`。

任何 acquisition failure、digest mismatch、未分类资源或新的 source anomaly 都必须 fail closed / 单独报告，不得静默删除引用、伪造文件或换成猜测内容。

## 7. Relationship to deferred Articles / ListItems

本 Requirement只处理 Main Page：

- 230 篇 deferred problem Articles 与 6 篇 source-defect Articles保持独立人工/客户确认边界；
- 不修改其 evidence、canonical/import eligibility或Runtime状态；
- stable Main ListItem identity / representation / adoption / reconcile仍是独立 Site Package Planning Candidate；
- 不因为 Page Readiness而给 ListItem授予 Identifier、Ready或Execute Authority。

## 8. Verification requirements

进入 Integration 前至少证明：

1. 10 个 accepted Page target 与 current stable Site Package identity 一一匹配；
2. Fresh Site provision使用正式 package body；
3. Existing Site在 current content 精确匹配旧 baseline时完成 adoption；
4. operator-diverged content被保留且 report明确列出 identity；
5. adoption后重复 provision幂等，后续 operator edit继续被保护；
6. accepted Page resource bytes全部经过 path/size/SHA-256验证并进入 package asset manifest；
7. `budget` 13 PDF全部形成 package-owned stable projection，正文无 legacy `/group1/cms/**` 运行依赖；
8. package asset projection仍满足 create-if-missing / protected-path / safe-path contract；
9. Site Package、Backend、Admin/Public/Integrated Browser regression通过；
10. no Page canonical migration mapping / Historical Migration fallback / Main-specific Generic schema change。

实际 Main Page 内容产生可见变化，因此 Execute/Integration阶段需要对 10 个 Page 做 bounded Human Review；自动 Browser验证必须先于人工验收。

## 9. Non-goals

- 不处理 stable Main ListItem；
- 不处理 230 + 6 deferred/source-defect Articles；
- 不扩大 Generic Historical Migration schema；
- 不修改 Public URL contract 或前端技术；
- 不将所有 operator-managed Page变为 package持续管理内容；
- 不改变 placeholder/fixed-integration Page产品方案；
- 不执行 `agentic-dev` baseline upgrade。

## 10. Requirement readiness

当前 Repository Authority 已明确 Page归属、10 个 source handoff target、EU-49 operator guard、Site Package stable asset能力，以及 `budget` 13 PDF 的 Human Authority。剩余问题属于可在 Technical Plan 中冻结的 package evolution机制与验证细节，不需要新的产品决策。

本 Requirement **READY**，并已由 EU-52 完成实现与验收。Implementation PR #125 已集成到 `main@ccbd9fd8c6048f5b4a96d965b8578f7b7a1d2838`；bounded Human Review 与 Post-Integration CI #973 PASS；EU-52 Execute Authority 已终止。后续 Page 版本变化仍必须遵守本 Requirement 的显式 adoption precondition，不因 EU-52 completion 获得永久 overwrite authority。
