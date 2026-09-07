# EU-42 — Site Asset Package Ownership & Runtime Projection

## Authority

- Parent Planning Candidate: GitHub Issue #77 — CMS Core / Site Package / Public Renderer 边界收敛
- Readiness: Issue #77 comment `#issuecomment-5562811697`
- Execution base: `main@eaf89f13a7f5eeae498124082d00f88da4e2627c`
- Requirement: `docs/requirements/cms-site-package-boundary.md`
- Specification: `docs/specifications/cms-site-package-boundary.md`
- Technical Plan: `docs/technical/cms-site-package-boundary.md`
- Verification Authority: `docs/technical/verification-strategy.md`

## Status

- Execution Unit: **EU-42**
- Readiness: **PASS**
- Implementation: **COMPLETED**
- Implementation Branch: `feature/eu-42-site-asset-runtime-projection`
- Pull Request: #90 — MERGED
- Final implementation Head: `37e03c3a5d7804804dcb3738a429e57e02b99e31`
- Integration commit: `main@2c4af15df64342850391bbfe67de99b6404b3280`

## Intent

关闭稳定 JilinJobs Site assets 在版本化 source、Site Package integrity 与 Runtime `/static/**` composition 之间的双 Authority，使 structure、bootstrap 与 stable assets 都从同一个 Site Package root 进入 Repository Runtime。

## Scope

### Stable Site asset ownership

- 将原 `site-baseline/static/**` 中稳定 Site asset bytes 迁入 `sites/jilinjobs/assets/**`；
- 保持现有公开 `/static/**` target URL，不实施视觉改版或 Public URL rename；
- 当前共 31 个稳定 binary assets 纳入 Site Package ownership。

### Manifest / integrity contract

`sites/jilinjobs/assets/manifest.json` 声明：

- `packageId`；
- asset `schemaVersion`；
- package-root-bounded `source`；
- Runtime `/static/**` `target`；
- source SHA-256。

Loader fail-fast 校验 package identity、duplicate source/target、path traversal、symlink escape、digest mismatch 与 `/static/uploads/**` exclusion。

### Runtime projection

`SitePackageAssetProjector`：

- 仅在 target 缺失时创建；
- existing regular file 保持不变；
- repeated projection 幂等；
- missing target 可恢复；
- operator 通过管理能力显式 replace 后，projection / restart 不覆盖该 replacement。

### StaticResource protection

- Site Package stable target 自动进入 `StaticResourceService` protected-path；
- ordinary delete 被拒绝；
- explicit replace 继续允许；
- `/static/uploads/**` 继续属于 mutable Runtime Store。

### Repository composition

- CI / Review Environment 从空 Runtime Static Root 启动；
- `CMS_SITE_PACKAGE_ROOT` 同时提供 stable structure、Fresh bootstrap 与 stable assets；
- 不再从 `site-baseline/static/**` 手工 copy 第二份 baseline。

## Explicit non-goals

- 不修改 Public Renderer source architecture 或 user-visible design；
- 不进入 Slice D / Canonical Migration compatibility re-entry；
- 不进入 Issue #60 / E1～E3；
- 不接管 `/static/uploads/**`；
- 不把 Historical Canonical Migration assets 迁入 stable Site asset package；
- 不新增数据库 Schema；
- 不定义 force-upgrade / overwrite-existing asset semantics；
- 不拆 Repository 或引入 multi-site/plugin framework。

## Acceptance

1. stable Site asset bytes 只有一个版本化 source owner：`sites/jilinjobs/assets/**`。
2. source / target / digest 与 package identity 可机器校验，越界、重复或 digest mismatch fail-fast。
3. Fresh empty Runtime Static Root 能投影完整稳定资源。
4. second projection / restart 幂等且不覆盖 existing target。
5. operator explicit replace 可保持，ordinary delete 对 stable target 被阻止。
6. `/static/uploads/**` 不受 stable Site asset projection / protection 接管。
7. CI / Review Environment 不依赖 legacy static baseline copy。
8. Public `/static/**`、Main / Party behavior、Admin 与 Integrated Browser 回归保持。
9. 无 Slice D / E1～E3 范围扩张。

## Verification and Integration Evidence

### Final exact-head

Final implementation Head：`37e03c3a5d7804804dcb3738a429e57e02b99e31`。

- Site Package Verification #34 / run `34068025402` — **PASS**；
- Repository CI #787 / run `34068025623` — **PASS**；
- Review Environment #698 / run `34068025436` — **PASS**；
- unresolved review threads — **NONE**；
- Integration 前 base drift — **NONE**。

### Integration

PR #90 已按 exact Head 合并：

`main@2c4af15df64342850391bbfe67de99b6404b3280`

### Post-Integration

该 Integration commit 已取得：

- Site Package Verification #35 / run `34070361976` — **PASS**；
- Repository CI #788 / run `34070361980` — **PASS**：Backend / Public / Admin / Integrated Browser 全部成功；Integrated Browser 明确从 empty Runtime Static Root 启动并通过 Site Package projection 取得 `/static/**` 资源。

因此 EU-42 implementation、integration 与 Post-Integration Current Evidence 已闭环，状态为 **COMPLETED**。

## Accepted Result

EU-42 完成后，Site Package stable asset 生命周期为：

```text
sites/jilinjobs/assets/**
→ package-bound manifest / SHA-256 verification
→ create-if-missing Runtime projection
→ configured CMS_STATIC_ROOT
→ Backend /static/**
```

Versioned stable assets 与 mutable Runtime uploads / Historical Migration assets 继续保持不同 ownership。普通 Runtime projection 不将版本化 source 解释为强制覆盖策略；明确的 operator replacement 可以保持。

## Rollback boundary

EU-42 不修改数据库 Schema，也不执行 destructive Runtime migration。实现已集成后，回滚应以 source/code revert 为单位；Existing Runtime Static Store 不要求 destructive rewrite。未来若需要版本化 asset 强制升级，必须重新形成独立 Requirement / conflict / rollback contract。

## Remaining boundary

Issue #77 保持 OPEN，但 EU-42 Execute Authority 随完成终止。当前没有 Ready Execution Unit。

剩余仅为：

1. Slice D — Canonical Migration Compatibility & E1～E3 Re-entry；
2. 四层 boundary 完成后的 Repository Split Readiness Assessment。

这些候选必须重新经过 current audit / `slice-work → readiness-check`；不得从 EU-42 自动进入 Slice D，也不得开始 Issue #60 / E1～E3。
