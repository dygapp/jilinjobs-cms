# JilinJobs Site Package

`sites/jilinjobs/` 是 JilinJobs Site 的稳定版本化 package source：

- `manifest.json`：package identity / version / component manifest；
- `structure/`：stable Site structure **and stable Site content**；
- `bootstrap/`：真正的一次性 Fresh Site ordinary defaults；
- `assets/`：stable Site asset source 与 integrity metadata。

## Current ownership

当前 Main 产品边界：

- stable Columns / PageGroups / Pages → Site Package；
- Page accepted content / `bodyHtml` → Site Package；
- NavigationLocations / NavigationItems → Site Package；
- CmsList definitions → Site Package；
- **stable Main ListItem membership → Site Package**；
- SiteConfig / AdvertisementSlots / stable site assets → Site Package；
- historical Main INTERNAL / EXTERNAL_LINK Articles + Article resources/provenance → `../../data-migrations/**`。

Legacy Source 中发现的 Main Page/ListItem 数据可以由 EU-50 作为 source handoff evidence 保留，但不得进入 Main Historical Migration import eligibility。

## Current capability gap

Site Package v1 已支持 stable Page content reconcile，但当前 `manifest.json` / `SitePackageProvisioning` 只支持 `lists` definitions，**尚不支持 stable `list-items` structure/reconcile**。

现有 `bootstrap/initial-data.sql` 中的 Main ListItems 是 EU-41 时建立的一次性 ordinary defaults：bootstrap 完成后变成 operator-managed Runtime data，不会被普通 Site Package reconcile 覆盖或 resurrect。

最新 ownership 决策要求 stable Main ListItem 最终退出这种一次性 lifecycle，但该 transition 必须由单独的 Site Package Planning/Execution Unit 定义 stable identity、adoption、reconcile、operator ownership 与 upgrade verification；EU-50 不得直接修改 CMS Core/provisioner 来绕过该 Gate。

## Runtime composition

当前 accepted composition：

```text
Generic CMS schema
→ stable Site Package reconcile
→ optional one-time bootstrap (only for remaining true bootstrap data)
→ stable asset projection
→ optional Historical Article migration
→ Runtime
```

`/static/uploads/**` 继续属于 mutable Runtime uploads，不是 stable Site Package asset source。

详细 Authority：

- `../../docs/requirements/cms-site-package-boundary.md`
- `../../docs/specifications/cms-site-package-boundary.md`
- `../../docs/technical/cms-site-package-boundary.md`
- GitHub Issue #77
