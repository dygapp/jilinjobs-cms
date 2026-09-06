# EU-39 — Navigation Stable Identity & Site Package Reconcile

## 1. Identity

- Identifier：`EU-39`
- Source：GitHub Issue #77 / Slice B remainder current audit
- Requirement：`docs/requirements/cms-site-package-boundary.md`
- Specification：`docs/specifications/cms-site-package-boundary.md`
- Technical Plan：`docs/technical/cms-site-package-boundary.md`
- Execute baseline：`main@e0e0f19553244dcf30efdfa392ef2e750e5e2e65`
- Implementation PR：#84
- Merge commit：`36276ed65e6f3edbe96ffc18c01cf18ab924837b`
- Status：**COMPLETED**

## 2. Readiness decision

EU-38 完成后重新审计 NavigationItem，确认现有字段不能形成长期稳定 identity：

- `name` 可以重命名；
- `position` / parent relation 可以移动；
- `sortOrder` 可以重排；
- target 可以从 Column / Page / Link 等发生受控变更；
- Runtime numeric `id` 不能成为 Site Package contract；
- 当前 schema 不存在能够在上述变化后仍稳定且唯一的 NavigationItem identity。

因此满足 Requirement / Specification 的前置条件：允许为 NavigationItem 引入独立 stable identity。Readiness Check 结论为 **PASS**，并形成本 EU。

## 3. Intent

本 Unit 只完成 NavigationItem stable identity 与 Site Package reconcile：

1. 为 `cms_navigation` 增加 provisioning-only stable `code`；
2. 把当前 40 条正式 JilinJobs preset NavigationItem 表达为 Site Package structure；
3. Fresh Generic Schema + Site Package 能创建完整正式导航树；
4. Legacy V2 preset navigation 可以在无歧义时原位获得 stable code，而不是复制第二棵导航树；
5. stable code 建立后，rename / move / reorder / retarget 可以由 Site Package 稳定 reconcile；
6. operator-created navigation 保持 `preset=false` / `code=NULL`，不被 Site Package 接管。

## 4. Stable identity contract

`cms_navigation.code` 的长期语义：

- 只承担 Site Provisioning stable identity；
- 对受 Site Package 管理的 preset NavigationItem 唯一；
- nullable，普通 operator-created NavigationItem 不需要 stable package identity；
- 不作为普通 Admin 可编辑业务字段；
- 不替代 Public API / canonical URL / Runtime numeric id；
- parent 与 target relationship 在 package 中使用 stable identities 表达。

Flyway V3 只增加 nullable `code` 与 unique index，不把 JilinJobs 导航实例数据写进 schema migration。

## 5. Legacy adoption contract

Existing V1+V2 database 上的旧 preset navigation 尚无 code。本 EU 使用一次性、事务内的安全认领规则：

1. 先按 package target 解析 parent / Column / Page / location；
2. 仅在 `preset=1 AND code IS NULL` 的 legacy rows 中寻找与当前 accepted baseline 完整语义一致的候选；
3. 必须恰好唯一匹配才原位写入 stable code；
4. 0 个或多个匹配均视为无法无歧义认领并失败；
5. 任一条认领失败时整体事务回滚，不留下部分 coded state；
6. 已有 code 后只按 code reconcile，不再依赖 mutable composite identity。

该策略避免把已经被人工改变、身份无法确定的 Legacy preset row 静默映射到错误的 package identity。

## 6. Included changes

- `backend/src/main/resources/db/migration/V3__navigation_stable_identity.sql`；
- `sites/schema/v1/navigation-items.schema.json`；
- `sites/jilinjobs/structure/navigation-items.json`；
- JilinJobs manifest 升级为 `0.3.0-navigation-identity`；
- `SitePackageLoader` 增加 NavigationItem contract / target validation / parent cycle validation；
- `SitePackageProvisioner` 增加 Fresh create / Legacy adoption / stable-code reconcile；
- EU-39 MySQL verifier 扩展 Fresh / Legacy / mutation / rollback proof。

Site Package `schemaVersion` 保持 `1`，`navigation-items` 是新增可选 structure type；EU-37 columns-only Foundation package 继续兼容。

## 7. Explicit non-goals

本 EU 明确不做：

- 不删除、迁空或改写 `V2__current_preset_data.sql`；
- 不把默认 Runtime bootstrap 从 V2 切换到显式 Site Package；
- 不接管 `cms_list_item` / `cms_advertisement` 运营成员；
- 不处理 `site-baseline/static/**` ownership / relocation；
- 不改 Public Renderer、Admin 产品行为、公开 URL 或 Gateway；
- 不改 Party canonical dataset provenance；
- 不进入 Issue #60 / E1～E3；
- 不设计 tenant / plugin / marketplace / multi-site framework；
- 不定义 package 删除项的自动 deprovision semantics。

## 8. Acceptance obligations

- NavigationItem 必须具有独立、唯一、长期稳定的 provisioning identity；
- operator-created navigation 不因 schema / package 存在而被接管；
- Fresh Generic Schema + Site Package 创建 40 条正式导航；
- second apply 全部 unchanged；
- Legacy V2 40 条 preset navigation 原位 adoption，不增加重复树；
- Legacy second apply 全部 unchanged；
- Fresh 与 Legacy 的 Site Package structural snapshot 等价；
- rename / move / reorder / retarget representative mutation 能由 stable code 恢复；
- ambiguous Legacy adoption 安全失败且整体回滚；
- ListItem / Advertisement operational snapshot 保持；
- `V2__current_preset_data.sql` 保持不变；
- Canonical migration compatibility PASS；
- EU-29→EU-30 migration upgrade compatibility PASS；
- Backend / Public / Admin / Integrated Browser regression PASS；
- final diff / Authority audit PASS；
- PR Integration + `main` Post-Integration Verification PASS。

## 9. Exact-head implementation evidence

Final implementation Head：`b95285f5424d4df0b9f9943395e80332296754f7`。

- Site Package Verification #15 / run `34035806532`：**PASS**；
- CI #761 / run `34035806584`：Backend / Public / Admin / Integrated Browser **PASS**；
- Canonical Migration Verification #152 / run `34035806465`：**PASS**；
- EU-30 Migration Upgrade Verification #102 / run `34035806427`：**PASS**；
- 人工评审环境 #678 / run `34035806511`：**PASS**；
- final diff / Authority audit：**PASS**。

专项验证同时证明：

- JilinJobs package object count = 98，其中 NavigationItem = 40；
- Fresh Generic Schema + Navigation identity schema + Site Package first apply：98 created；
- Fresh second apply：98 unchanged；
- Legacy V1+V2+V3 first apply：40 NavigationItem 原位 adoption + 58 existing structures unchanged；
- Legacy navigation row count 不增加；
- 40 条 preset navigation 全部取得 stable code；
- Legacy second apply：98 unchanged；
- Fresh / Legacy structural snapshot 等价；
- representative stable-code mutation restore PASS；
- ambiguous Legacy adoption rollback PASS；
- operator-created navigation 保持 `code=NULL / preset=false`；
- ListItem / Advertisement operational members 保持不变。

## 10. Integration and Post-Integration evidence

PR #84 已从锁定 final Head 合并，merge commit：

`36276ed65e6f3edbe96ffc18c01cf18ab924837b`

`main@36276ed65e6f3edbe96ffc18c01cf18ab924837b` Post-Integration：

- Site Package Verification #16 / run `34036523856`：**PASS**；
- CI #762 / run `34036523996`：Backend / Public / Admin / Integrated Public Browser / Integrated Admin Browser **PASS**。

因此 EU-39 的实现、Integration 与 `main` Current Evidence 已闭环，状态正式晋升为 **COMPLETED**。

## 11. Remaining Issue #77 boundary after EU-39

EU-39 只关闭 NavigationItem identity / reconcile。以下仍是独立 Planning Candidates：

1. **V2 / default Runtime composition convergence**：何时、如何让默认 Site bootstrap 从 V2 implicit responsibility 转为显式 Site Package，并证明 Fresh / Existing / Canonical / Review Runtime compatibility；
2. **Slice C — Site Asset Ownership & Runtime Composition**；
3. **Slice D — Canonical Migration Compatibility & E1～E3 Re-entry**；
4. 四层 boundary 完成后的 Repository Split Readiness Assessment。

这些剩余项没有继承 EU-39 的 Identifier 或 Execute 授权，必须重新执行 current audit / `slice-work → readiness-check`。
