# CMS Core / Site Package / Public Renderer 规划状态

## 当前结论

Issue #77 仍是 Issue #60 / E1～E3 主站正式内容工作前的前置架构收敛入口，但其状态已经从“仅 Planning Authority”前进：

- Slice A 已形成并完成 **EU-37 — Site Package Contract & Provisioner Foundation**；
- Slice B 中具有现有 stable identity 的结构部分已形成并完成 **EU-38 — Stable Site Structure Package Migration**；
- EU-37 / EU-38 均已集成到 `main` 并通过 Post-Integration Verification；
- 当前没有 Ready Execution Unit，Issue #77 保持 OPEN。

四层长期边界保持不变：Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer。

## 已完成

### EU-37 — Site Package Contract & Provisioner Foundation

已建立：

- Site Package v1 manifest / structure contract；
- 机器可审计 schema；
- path / digest / duplicate / cycle / ownership preflight；
- narrow、幂等的 Site Provisioner；
- Fresh Flyway V1 Generic Schema 上的真实 MySQL Foundation verification。

### EU-38 — Stable Site Structure Package Migration

已把具有现有稳定 identity 的 JilinJobs preset structure 表达进 `sites/jilinjobs/**`：

- Column；
- PageGroup；
- Page；
- NavigationLocation；
- SiteConfig；
- CmsList definition；
- AdvertisementSlot。

已证明 Fresh `V1 + Site Package` 与 Legacy `V1+V2 + Site Package` 的 stable structural snapshot 等价，并保持 `preset=false` operator data、NavigationItem、CmsListItem、Advertisement 运营成员不被接管。`V2__current_preset_data.sql` 仍保持当前兼容 baseline，未在 EU-38 中重写或迁空。

## 当前剩余 Planning 范围

Issue #77 的下一步仍需重新执行 current audit / slice-work / readiness-check，不预设新的 EU Identifier：

1. **Slice B 剩余部分**：NavigationItem stable identity / reconcile strategy，以及 V2 与默认 Runtime composition 的长期责任收敛；
2. **Slice C — Site Asset Ownership & Runtime Composition**：让稳定 Site Assets 与 Site Package ownership、runtime bootstrap、CI / Review Environment composition 显式一致；只有证据证明必要时才物理迁目录；
3. **Slice D — Canonical Migration Compatibility & E1～E3 Re-entry**：验证 Party canonical migration / accepted Runtime 状态与 Site Package contract 兼容，再解除 Issue #60 / E1～E3 的前置等待。

## 当前不做

- 不选择或重写 Public Frontend 技术栈；
- 不拆 Git Repository；
- 不引入 Git Submodule；
- 不做 Docs / Code 分仓；
- 不实现多 Repository Workspace composition；
- 不为形式统一进行全仓目录重排；
- 不把 Site Definition 并入 `data-migrations/**`；
- 不提前执行 E1 / E2 / E3；
- 不因为 EU-38 已完成就自动创建下一个 EU。

## 后续独立评估

Site Package 边界形成并取得 Runtime / canonical compatibility 证据后，再单独执行 Repository Split Readiness Assessment。届时再判断 Public Renderer、Site Package、Docs / Project Authority 是否适合独立 Repository，以及 exact commit composition、cross-repo contract、Fresh Context 和 CI Evidence 应如何治理。

该评估当前不是 Execution Unit，也不预设最终一定拆仓。
