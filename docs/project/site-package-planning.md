# CMS Core / Site Package / Public Renderer 规划状态

## 当前结论

Issue #77 仍是 Issue #60 / E1～E3 主站正式内容工作前的前置架构收敛入口，并已连续完成三个独立 Execution Unit：

- Slice A 已形成并完成 **EU-37 — Site Package Contract & Provisioner Foundation**；
- Slice B 中具有现有 stable identity 的结构部分已形成并完成 **EU-38 — Stable Site Structure Package Migration**；
- Slice B 的 Navigation identity 部分已形成并完成 **EU-39 — Navigation Stable Identity & Site Package Reconcile**；
- EU-37～EU-39 均已集成到 `main` 并通过 Post-Integration Verification；
- 当前没有 Ready Execution Unit，Issue #77 保持 OPEN。

四层长期边界保持不变：Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer。

## 已完成

### EU-37 — Site Package Contract & Provisioner Foundation

已建立：

- Site Package v1 manifest / structure contract；
- 机器可审计 schema；
- path / digest / duplicate / cycle / ownership preflight；
- narrow、幂等的 Site Provisioner；
- Fresh Generic Schema 上的真实 MySQL Foundation verification。

### EU-38 — Stable Site Structure Package Migration

已把以下 JilinJobs preset structure 表达进 `sites/jilinjobs/**`：

- Column；
- PageGroup；
- Page；
- NavigationLocation；
- SiteConfig；
- CmsList definition；
- AdvertisementSlot。

已证明 Fresh `V1 + Site Package` 与 Legacy `V1+V2 + Site Package` 的上述 stable structural snapshot 等价，并保持 `preset=false` operator data、NavigationItem、CmsListItem、Advertisement 运营成员不被接管。

### EU-39 — Navigation Stable Identity & Site Package Reconcile

EU-39 current audit 证明 NavigationItem 的 name / parent / location / sort / target 都是可变语义，现有字段不能组成长期 stable identity，因此引入 provisioning-only nullable `cms_navigation.code`：

- Flyway V3 只增加 Generic CMS Navigation stable identity capability，不写入 JilinJobs 实例值；
- 当前 40 条正式 NavigationItem 已进入 `sites/jilinjobs/structure/navigation-items.json`；
- Fresh path 可创建完整导航树；
- Legacy V2 preset navigation 只在完整旧语义唯一匹配时原位 adoption stable code，不复制第二棵导航树；
- stable code 建立后支持 rename / move / reorder / retarget reconcile；
- operator-created `preset=false / code=NULL` navigation 不被接管；
- ambiguous Legacy adoption 事务安全失败并整体回滚。

PR #84 已合并为 `main@36276ed65e6f3edbe96ffc18c01cf18ab924837b`；Post-Integration Site Package Verification #16 与 CI #762（Backend / Public / Admin / Integrated Browser）全部 PASS，EU-39 状态为 **COMPLETED**。

`V2__current_preset_data.sql` 在 EU-38 / EU-39 均保持不变，因此默认 Runtime compatibility responsibility 仍未移除。

## 当前剩余 Planning 范围

Issue #77 的下一步仍需基于最新 `main` 重新执行 current audit / slice-work / readiness-check，不预设新的 EU Identifier：

1. **Slice B 剩余部分 — V2 / default Runtime composition convergence**：明确默认 Site bootstrap 如何从 V2 implicit responsibility 转为显式 Site Package lifecycle，并同时证明 Fresh / Existing DB、Site Package idempotency、Public/Admin behavior、canonical migration 与 Review Environment compatibility；
2. **Slice C — Site Asset Ownership & Runtime Composition**：让稳定 Site Assets 与 Site Package ownership、runtime bootstrap、CI / Review Environment composition 显式一致；只有证据证明必要时才物理迁目录；
3. **Slice D — Canonical Migration Compatibility & E1～E3 Re-entry**：在最终 Runtime composition 下验证 Party canonical migration / accepted Runtime 状态与 Site Package contract 完整兼容，再解除 Issue #60 / E1～E3 的前置等待。

这些候选可以根据最新 Repository audit 合并或进一步切分，但不得继承 EU-39 的 Execute 授权，也不得仅因顺序相邻预设为 EU-40。

## 当前不做

- 不选择或重写 Public Frontend 技术栈；
- 不拆 Git Repository；
- 不引入 Git Submodule；
- 不做 Docs / Code 分仓；
- 不实现多 Repository Workspace composition；
- 不为形式统一进行全仓目录重排；
- 不把 Site Definition 并入 `data-migrations/**`；
- 不提前执行 E1 / E2 / E3；
- 不因为 EU-39 已完成就自动创建下一个 EU。

## 后续独立评估

Site Package 边界形成并取得 Runtime / canonical compatibility 证据后，再单独执行 Repository Split Readiness Assessment。届时再判断 Public Renderer、Site Package、Docs / Project Authority 是否适合独立 Repository，以及 exact commit composition、cross-repo contract、Fresh Context 和 CI Evidence 应如何治理。

该评估当前不是 Execution Unit，也不预设最终一定拆仓。
