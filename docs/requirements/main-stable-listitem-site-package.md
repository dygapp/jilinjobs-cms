# Main ListItem Site Package Requirement

## Status

- Parent Planning Authority: GitHub Issue #60
- Architecture Authority: GitHub Issue #77
- Planning baseline: `main@4d5578a2715f8adc0ebca73ee0ae7342f740c8ce`
- Requirement: **READY**
- Candidate: Main ListItem Site Package Bootstrap Completion
- Execution Unit identity: **not assigned before `slice-work`**

## 1. Intent

EU-50 已确认 Main ListItem 属于 JilinJobs Site Package，而不是 Article Historical Migration。当前产品需求不是建立新的 ListItem Runtime ownership/reconcile 子系统，而是把已经确认的 Main ListItem 初始数据完整固化到 Site Package bootstrap SQL 中，与现有 Main `HOME_CAROUSEL`、其他一次性初始化数据采用相同机制。

初始化完成后，ListItem 按现有 bootstrap 语义成为普通 operator-managed Runtime data；Site Package 不在后续启动时反复覆盖或恢复这些行。

## 2. Main scope

本轮 Main ListItem 初始化范围：

- `HOME_CAROUSEL`：主站首页轮播，继续保留在 Site Package bootstrap SQL；
- `SITE_RELATED`：5 条；
- `SITE_REGIONAL_GRADUATES`：31 条；
- `SITE_JILIN_UNIVERSITIES`：60 条。

三个 `SITE_LINKS` 列表共 96 条，使用 EU-50 已审核完成的最终内容决策：

- commit `b223a1d3a40b510f53a34ea9997926f8f4541a18`；
- `sites/jilinjobs/reports/listitem-final-adjustment-report.md`；
- audited 96 = adjusted 72 + unchanged 24。

实现不得重新抓取 Legacy Source，也不得根据当前第三方站点可达性重新猜测、删除或替换已接受 title/URL。

## 3. Party boundary

党员之家 ListItem 不属于本轮 Main ListItem 固化：

- `PARTY_CAROUSEL` 已包含在 Party 数据迁移及对应 Party Authority 中；
- 不在 Main bootstrap SQL 中复制 Party ListItem；
- 不修改 Party migration input、mapping、runtime verification 或 ownership。

## 4. Bootstrap lifecycle

使用现有 Site Package bootstrap 机制：

```text
stable Site structure provision
→ bootstrap/initial-data.sql applied once
→ cms_site_bootstrap_state records completion
→ subsequent startup does not replay the bootstrap
→ rows are ordinary operator-managed Runtime data
```

因此本 Requirement 不需要：

- `cms_list_item` 新 stable code；
- Flyway schema evolution；
- Site Package `list-items` structure type；
- Runtime ListItem reconcile；
- Existing-Site adoption/fingerprint；
- stable-item delete protection；
- absence-based removal semantics。

## 5. Data requirements

`sites/jilinjobs/bootstrap/initial-data.sql` 必须：

1. 保留 Main `HOME_CAROUSEL` 初始化数据；
2. 将 `SITE_RELATED` 更新为已审核接受的 5 条最终值；
3. 新增 `SITE_REGIONAL_GRADUATES` 31 条最终值；
4. 新增 `SITE_JILIN_UNIVERSITIES` 60 条最终值；
5. 使用已有 `CmsList` code 查询父列表，不绑定固定 Runtime numeric id；
6. 保持 `LINK` source type、既有 open-mode semantics 与明确 sort order；
7. 不添加任何 Party ListItem。

修改 SQL 后必须同步更新 `sites/jilinjobs/bootstrap/manifest.json` 中的 SHA-256。

## 6. Runtime / operator behavior

Bootstrap 成功后：

- ListItem 继续使用现有 Generic Admin/Public contract；
- operator 可以按现有能力维护这些数据；
- 后续 Site Package composition 不对这些 bootstrap rows 做 reconcile；
- 已执行 bootstrap 的环境不会因为 package 再次启动而自动重放同一 bootstrapId。

本轮不解决“对已执行旧 bootstrap 的长期在线环境自动补种新数据”问题；当前目标是仓库内正式初始化基线完整、Fresh Site 可重复验证。若未来存在生产升级补种需求，应单独形成明确的数据升级 Authority，而不是把初始化数据升级问题转化为永久 reconcile 机制。

## 7. Verification requirements

至少证明：

- bootstrap SQL 与 manifest digest 一致；
- Fresh Site 可成功执行 bootstrap；
- Main `HOME_CAROUSEL` 初始化存在；
- 三组 `SITE_LINKS` 条数为 5 / 31 / 60；
- EU-50 最终审核的 96 条 title/URL 决策与 SQL 一致；
- `SITE_RELATED` 已使用最终审核值，而不是旧 bootstrap 值；
- 第二次 apply 保持 `ALREADY_APPLIED`，无重复插入；
- Public `SITE_LINKS` 可读取新初始化数据；
- Party ListItem 数据与 Party migration 不受影响；
- Backend / Site Package / Public 相关回归通过。

## 8. Non-goals

- 不建立新的 stable ListItem identity/reconcile framework；
- 不新增 Flyway；
- 不新增 Site Package structure type；
- 不修改 Party ListItems；
- 不处理 Article exception backlog；
- 不处理慧就业 iframe；
- 不更新 `agentic-dev` baseline。

## 9. Readiness

需求边界、内容 Authority、实现位置和生命周期均已明确。本 Requirement **READY**；下一步由 Specification / Technical Plan 将其收敛为一个小型 bootstrap-data execution unit，经 `slice-work → readiness-check` 后方可执行。
