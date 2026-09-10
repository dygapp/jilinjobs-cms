# Main Stable ListItem Site Package Requirement

## Status

- Parent Planning Authority: GitHub Issue #60 / stable Main ListItem Site Package follow-up
- Architecture Authority: GitHub Issue #77
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Planning baseline: `main@4d5578a2715f8adc0ebca73ee0ae7342f740c8ce`
- Requirement: **READY**
- Candidate: Main Stable ListItem Site Package Adoption
- Execution Unit identity: **not assigned before `slice-work`**

## 1. Intent

EU-50 已完成 Main source discovery，并明确 Main stable ListItem membership 属于 JilinJobs Site Package，而不是 Historical Content Migration。PR #117 在 EU-50 收敛过程中曾形成最终 ListItem 内容审核结果，但 stable ListItem Runtime provisioning 因超出当时 Article-only Execute Authority 被正确回退；EU-52 随后只独立完成 Main Page formal content，没有继承或覆盖 ListItem scope。

本 Requirement 的目标是关闭这一明确的 Site Package capability gap：把已经审核接受的 Main 稳定站点链接成员固化为版本化 Site Package 数据，建立独立于 Runtime DB id、title、URL 和排序的稳定身份，并在 Fresh Site、已有 bootstrap Site、operator edit 与重复 reconcile 场景下保持可预测、非破坏性的生命周期。

## 2. Accepted content scope

本轮只接受 EU-50 最终 ListItem adjustment evidence 中的三组 Main `SITE_LINKS`：

| List | Accepted occurrences |
|---|---:|
| `SITE_RELATED` | 5 |
| `SITE_REGIONAL_GRADUATES` | 31 |
| `SITE_JILIN_UNIVERSITIES` | 60 |
| **Total** | **96** |

最终内容决策来自 repository history 中已审核报告：

- commit: `b223a1d3a40b510f53a34ea9997926f8f4541a18`；
- file: `sites/jilinjobs/reports/listitem-final-adjustment-report.md`；
- audited: 96；adjusted: 72；unchanged after review: 24；
- source evidence: run `34303771704` / artifact `10086056781` / digest `sha256:66118e4f21bf7644db1c97e2a631eee5d4902410f167606286e1280293620494`；
- ListItem Link Audit: run `34323764980` / artifact `10092940455` / digest `sha256:b0cfd4549c700f0b84edf19bb620b2ebc4c2102b46f7a8dc727ee9d841b5fa16`。

当前实现必须消费该报告已经接受的最终 title/URL 决策，不重新抓取 Legacy Source，也不得因为当前外部网络状态重新猜测、删除或改写这些成员。

`HOME_CAROUSEL` 不在上述最终三组稳定站点链接审核范围内，继续保持当前 bootstrap/operator-managed lifecycle；`PARTY_CAROUSEL`、Advertisement、Page、Article 均不属于本轮。

## 3. Ownership contract

### 3.1 Site Package owns stable identity and membership

对于本轮 96 个 accepted ListItem：

- parent `CmsList.code` 与 ListItem package code 共同形成稳定身份；
- stable identity 不得依赖 Runtime numeric id、当前 title、当前 URL、当前 sortOrder 或实时外部站点状态；
- JilinJobs Site Package 负责声明这些稳定成员及其 create/adoption defaults；
- Generic CMS Core 只提供 site-neutral stable identity、provisioning/reconcile 与保护机制，不包含 JilinJobs 具体成员值；
- Historical Migration、Flyway seed data 与 Public Renderer 都不成为这些具体成员的 source owner。

### 3.2 Runtime mutable fields remain operator-maintainable

稳定成员被创建或安全 adoption 后，其展示/目标字段继续允许通过既有 ListItem Admin 能力维护，包括 title、subtitle、URL、openMode、sortOrder、enabled 以及当前 ListItem contract 允许的其他 mutable payload。

ordinary Site Package reconcile 不得因为 package defaults 与 Runtime 当前值不同而覆盖 operator edit。package defaults 是 create/adoption baseline，不是永久 overwrite authority。

### 3.3 Stable membership is protected from ordinary deletion

stable-coded ListItem 属于网站规划基线，ordinary Admin 不得删除；operator-created `code = NULL` ListItem 继续保持现有增删改能力。

这只保护 stable membership identity，不把同一 CmsList 中的全部 Runtime rows 变成 package-owned。

## 4. Stable identity requirements

Generic Core 必须为 `cms_list_item` 提供 nullable stable code：

- package-owned stable row: code 非空；
- ordinary operator-created row: code 保持 `NULL`；
- logical identity: `(list_id, code)`，外部 package representation 使用 `(listCode, itemCode)`；
- 同一 List 内 stable code 唯一；
- stable code 一旦分配，不因 title/URL/sortOrder 后续变化重新生成。

本轮 package item code 必须在版本化 `list-items` 数据中显式冻结。可以使用有序编号作为一次性 opaque key，但编号只承担不可变 identity，不得在 Runtime 根据 source order 或 sortOrder 动态推导/重算。

## 5. Existing-Site guarded adoption

当前 `sites/jilinjobs/bootstrap/initial-data.sql` 中 5 个 `SITE_RELATED` ListItem 是旧的一次性 bootstrap defaults。它们在新 stable capability 上线时不能被无条件认领，也不能留下重复初始化路径。

每个允许从旧 bootstrap row 迁移到 stable membership 的 package item 必须声明精确 prior-baseline fingerprint。Existing Site 处理：

1. target stable code 已存在且 identity/source type 合法：保留 Runtime mutable payload；
2. stable code 不存在，且恰有一个 `code = NULL` row 精确匹配声明的 prior-baseline fingerprint：允许 adoption，赋予 stable code，并将该已知旧 package baseline 一次性提升到本次 accepted target default；
3. 匹配多个候选：fail closed / 明确报告 identity conflict，不猜测；
4. 没有匹配且该 stable identity 尚不存在：创建 accepted target row；
5. 任何未被精确 adoption 的 `code = NULL` operator row 原样保留。

不得通过 title 相似、URL redirect、更新时间、source order、空字段或其他启发式方式认领已有 operator data。

## 6. Reconcile and future upgrade/removal semantics

本轮冻结以下生命周期：

- missing stable identity：CREATE；
- existing stable identity：验证 parent/source-type identity 后，ordinary reconcile 不覆盖 mutable payload；
- repeated reconcile：幂等；
- operator edit：后续 reconcile 保留；
- operator stable-item delete：拒绝；
- operator-created null-code item：不参与 stable reconcile；
- package 新增新 code：按新成员 CREATE；
- package manifest 中缺少历史 code **不表示删除**，ordinary reconcile 不做 absence-based delete/detach/disable；
- 未来确需移除 stable member 时，必须通过独立、显式、版本化 removal/adoption Authority 定义，不从文件缺项静默推导。

该约束关闭当前 removal ambiguity，同时优先保护已有运营数据。

## 7. Bootstrap transition

stable ListItem capability 集成后：

- `SITE_RELATED` 5 条不得继续由 `bootstrap/initial-data.sql` 另行创建；
- Fresh Site 由 stable `list-items` structure 创建三组共 96 条；
- `HOME_CAROUSEL` 当前 1 条 bootstrap row 保持 one-time/operator-managed，不因本轮自动转成 stable item；
- Advertisement bootstrap 保持现状。

Existing Site 已执行过 bootstrap 的事实不得被重置，也不得通过重跑 bootstrap 实现 adoption。

## 8. Public behavior

当前 Public `SITE_LINKS` contract 保持不变：

- `/api/public/lists/by-group/SITE_LINKS` 继续返回三组稳定 List definition 及 enabled items；
- Main Public Renderer 继续消费该 stable public contract，不新增 Main-specific API；
- Fresh Runtime 中应可观察到 5 + 31 + 60 accepted link members；
- operator-disabled stable item继续遵守现有 public enabled filtering；
- external target/open-mode 继续使用既有 ListItem/Public Renderer semantics。

## 9. Verification requirements

进入 Integration 前至少证明：

1. append-only schema upgrade 为 ListItem 建立 nullable stable code，并保持 ordinary rows 可为 NULL；
2. Generic Core 不包含任何 JilinJobs-specific member data；
3. package loader/validator 支持 `list-items`，并验证 parent list、stable code、LINK payload 与唯一性；
4. Fresh Site 创建 exactly 96 stable SITE_LINKS members，bootstrap 不产生重复 SITE_RELATED rows；
5. Existing Site 的旧 5 条 SITE_RELATED bootstrap defaults 只在 exact baseline match 时 adoption；
6. ambiguous adoption fail closed，unmatched operator rows保留；
7. stable-coded item ordinary update 后 reconcile 保留 operator edit；
8. stable-coded item ordinary delete 被拒绝，null-code item仍可正常增删改；
9. second reconcile idempotent，不产生重复 stable identities；
10. manifest absence 不触发静默删除；
11. Public SITE_LINKS exact membership/title/URL/order 与 accepted package data一致，并通过 Browser verification；
12. Generic migration/Party migration/Page package/Backend/Admin/Public regressions保持通过。

由于 96 条链接内容会直接改变 Main 公开站可见内容，自动 Browser verification 通过后需要 bounded Human Review，人工重点确认三组展示、布局与代表性外链呈现；不要求依赖 96 个第三方站点全部实时可访问作为 Runtime acceptance 条件。

## 10. Non-goals

- 不处理 `HOME_CAROUSEL` stable conversion；
- 不处理 Party ListItem ownership；
- 不处理 230 deferred + 6 source-defect Articles；
- 不处理慧就业 iframe/fixed integration；
- 不重新抓取或重新审核 EU-50 已接受的 96 条最终内容；
- 不把 ListItem stable code 暴露为 ordinary Admin 可编辑产品字段；
- 不建立通用 package removal framework；
- 不修改 Public frontend 技术栈或 URL contract；
- 不更新 `agentic-dev` baseline。

## 11. Requirement readiness

Ownership、accepted content scope、最终审核 evidence、现有 bootstrap 兼容状态、operator mutation contract 与 stable identity gap 均已有 Repository Evidence。剩余问题属于可由 Technical Plan 明确的 site-neutral schema/provisioning实现细节，不需要新的产品决策。

本 Requirement **READY**，可进入 Specification / Technical Planning；在 `slice-work` 前仍只是 Planning / Requirement Candidate，不因本文件 READY 自动获得 Execution Unit identity、Readiness 或 Execute Authority。
