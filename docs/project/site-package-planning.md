# CMS Core / Site Package / Public Renderer 规划状态

## 当前结论

Issue #77 仍是 Issue #60 / E1～E3 主站正式内容工作前的前置架构收敛入口，并已连续完成四个独立 Execution Unit：

- Slice A：**EU-37 — Site Package Contract & Provisioner Foundation**；
- Slice B stable structure：**EU-38 — Stable Site Structure Package Migration**；
- Slice B Navigation identity：**EU-39 — Navigation Stable Identity & Site Package Reconcile**；
- Slice B Runtime composition activation：**EU-40 — Explicit Site Package Runtime Composition Activation**；
- EU-37～EU-40 均已集成到 `main` 并通过 Post-Integration Verification；
- 当前没有 Ready Execution Unit，Issue #77 保持 OPEN。

四层长期边界保持不变：Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer。

## 已完成

### EU-37～EU-39 — Site Package contract / stable structure / Navigation identity

已建立 Site Package v1 manifest/schema、narrow idempotent provisioner，并把 Column、PageGroup、Page、NavigationLocation、SiteConfig、CmsList definition、AdvertisementSlot 与 40 条 NavigationItem 表达为具有 stable identity 的 JilinJobs Site Package structure。Fresh / Legacy create/adoption/reconcile、operator non-takeover 与 structural equivalence 均已取得 Current Evidence。

### EU-40 — Explicit Site Package Runtime Composition Activation

EU-40 没有删除 V2，而是先把正式消费路径收敛为显式 composition：

```text
Flyway current schema / compatibility baseline
→ configured JilinJobs Site Package reconcile
→ runtime / importer consumers
```

已完成：

- `cms.site-package.root` opt-in Spring lifecycle，显式依赖 Flyway initializer 并复用 `SitePackageProvisioner`；
- 未配置 root 时 Generic CMS context 保持原行为；
- Repository CI Integrated Runtime 显式挂载 `sites/jilinjobs`；
- Party canonical / EU-30 migration Gradle importer 入口显式提供同一 Site Package root；
- real MySQL 证明 Legacy V2 40 条 navigation 原位 adoption、98 objects second-start idempotency 与 operational seed preservation；
- exact-head Site Package #19、CI #768、Canonical #153、Upgrade #103、Review #683 全部 PASS；
- PR #86 合并为 `main@b105e553db1ebbc12a2b6665385b94fb977bea06`；Post-Integration Site Package #20 与 CI #769 全部 PASS。

因此 EU-40 状态为 **COMPLETED**。

`V2__current_preset_data.sql` 仍承担 compatibility responsibility；EU-40 不等于 V2 retirement。

## 当前剩余 Planning 范围

Issue #77 后续仍需基于最新 `main` 重新执行 current audit / slice-work / readiness-check，不预设新的 EU Identifier：

1. **Operational Seed Classification & V2 Responsibility Retirement**：分类 V2 中仍有长期初始化意义的 `CmsListItem` / `Advertisement` 等 operational seed，明确其长期 Authority，并在证据充分后收敛 / 移除 V2 Site-specific compatibility responsibility；
2. **Slice C — Site Asset Ownership & Runtime Composition**：让稳定 Site Assets 与 Site Package ownership、runtime bootstrap、CI / Review Environment composition 显式一致；只有证据证明必要时才物理迁目录；
3. **Slice D — Canonical Migration Compatibility & E1～E3 Re-entry**：在最终 Runtime composition 下证明 Party canonical migration / accepted Runtime 状态与 Site Package contract 完整兼容，再解除 Issue #60 / E1～E3 的前置等待；
4. 四层 boundary 完成后单独执行 **Repository Split Readiness Assessment**。

这些候选可以根据最新 Repository audit 合并或进一步切分，但不得继承 EU-40 的 Identifier 或 Execute 授权。

## 当前不做

- 不选择或重写 Public Frontend 技术栈；
- 不拆 Git Repository；
- 不引入 Git Submodule；
- 不做 Docs / Code 分仓；
- 不实现多 Repository Workspace composition；
- 不为形式统一进行全仓目录重排；
- 不把 Site Definition 并入 `data-migrations/**`；
- 不提前执行 E1 / E2 / E3；
- 不因为 EU-40 已完成就自动创建下一个 EU。

## 后续独立评估

Site Package 四层边界形成并取得最终 Runtime / canonical compatibility 证据后，再单独执行 Repository Split Readiness Assessment。该评估当前不是 Execution Unit，也不预设最终一定拆仓。
