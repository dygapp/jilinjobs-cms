# CMS Core / Site Package / Public Renderer 边界 Specification

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- GitHub Issue #77
- `docs/specifications/public-frontend-replaceability.md`
- `docs/technical/cms-architecture.md`
- `data-migrations/README.md`
- `docs/specifications/preset-site-structure.md`

## Status

- Specification: Ready
- Technical Planning: required
- Execution Unit: not created

## 1. Four-layer boundary

项目长期按以下四层理解当前系统：

```text
Generic CMS Core
        ↓ provides schema / domain / Admin / Public contracts
JilinJobs Site Package
        ↓ provisions stable site structure / config / site assets
Historical Content Migration
        ↓ imports canonical operational content
Runtime CMS Data
        ↓ consumed through stable public contracts
Replaceable Public Renderer
```

这四层可以暂时保留在同一个 Git Repository 中。逻辑边界优先于物理拆仓。

## 2. Generic CMS Core contract

Generic CMS Core 包含：

- CMS 数据库 schema；
- Column / Article / Page / Navigation / Listing / Advertisement / SiteConfig / Resource 等通用领域模型；
- Admin API 与 Admin Frontend；
- Public API / DTO / Resource projection；
- `preset` 结构保护能力；
- Site Provisioning 所需的最小通用校验 / 持久化能力；
- 通用 Verification Contract。

Generic CMS Core 不应内建吉林就业网站专属事实，例如具体栏目 alias/name、导航树、外部业务 URL、联系电话、ICP备案、Logo/Banner 路径或具体 Site list code 必须存在。

`preset` 的语义调整为：**由某个受控 Site Provisioning 建立、具有稳定 identity 并受结构保护的对象**。它仍是通用 CMS 能力。

## 3. JilinJobs Site Package contract

Site Package 是吉林就业网站的版本化站点定义，不是 Historical Content Migration。

它至少需要表达：

- package identity / version；
- stable object identity；
- Columns；
- PageGroups / Pages；
- NavigationLocations / NavigationItems；
- CmsList definitions；
- AdvertisementSlots；
- SiteConfig definitions / accepted initial values；
- stable site assets；
- object relationships；
- `preset` expectation；
- provisioning order / dependencies；
- integrity metadata；
- upgrade / idempotency semantics。

### Stable identity

长期关系不得依赖 Runtime 自增 ID。使用现有稳定业务 identity：

- Column → `alias`；
- PageGroup / Page → context + `alias`；
- NavigationLocation → `code`；
- CmsList → `code`；
- AdvertisementSlot → `code`；
- SiteConfig → `key`；
- NavigationItem → 需要由 Technical Planning 确定可稳定重建的 logical identity，不新增产品字段除非现有数据无法可靠表达。

### Versioning

Site Package 应有明确 package version / manifest。Git commit 仍记录历史变更，但 Runtime Provisioning 需要能够判断当前 package 的目标状态和升级结果。

当前阶段不要求设计通用插件系统或支持任意第三方 Site Package Marketplace。

## 4. Flyway boundary

Flyway 继续负责数据库 schema 演进，并允许保留真正属于 CMS Core 的数据库级初始化元数据。

当前 `V2__current_preset_data.sql` 需要按对象逐项分类：

- **Generic Core**：若没有某个具体 Site 也成立的必要 CMS metadata / technical invariant；
- **JilinJobs Site Package**：站点栏目、导航、Page、List、Ad Slot、Site Config value、具体业务 URL、具体资源路径等；
- **Historical / Operational Content**：若存在，应转入 Canonical Migration，而不是 Site Package。

Technical Planning 必须给出完整 classification audit，不能按 SQL 文件整体移动。

Site Package Provisioning 的最终实现可以使用 SQL、JSON/YAML + importer 或其他最小机制，但其 Authority 不再是 Flyway schema migration identity。

## 5. Static asset boundary

当前 `site-baseline/static/**` 语义上已经属于 JilinJobs Site Package。

长期要求：

- stable site assets 与当前 Public Renderer 源码分离；
- Runtime 仍可由 Backend `/static/**` 提供；
- Renderer 只依赖稳定 URL / Site data，不依赖资产在 Git 中的物理目录；
- Historical article/body assets 继续跟随 Canonical Migration unit，不并入 Site Package；
- Runtime uploads 继续与版本化 Site assets 分离。

是否把目录从 `site-baseline/` 改名 / 移到 `sites/jilinjobs/` 由 Technical Planning 根据 diff 成本和长期清晰度决定，不构成 Requirement。

## 6. Historical Migration boundary

`data-migrations/**` 保持现有 Historical Content Migration 语义。

Main / Party Canonical Migration 可以依赖 Site Package 的 stable identities，但必须满足：

- 不依赖 Runtime numeric ID；
- 不依赖 Vue Router / component / Vite artifact；
- Import 前应验证所需 target Site identity 存在；
- Site Package 先 provisioning，Canonical Migration 后 import；
- Site Package 升级不得静默破坏已有 Canonical identity。

## 7. Public Renderer boundary

当前 `frontend/public-site` 是 accepted implementation，但不是 Site Authority。

Replacement-stable dependencies：

- `/api/public/**`；
- `/static/**` / public resources；
- accepted canonical routes；
- SiteConfig / Navigation / List / Page / Article 等 Public response semantics；
- Main / Party identities；
- accepted visual / responsive / SEO obligations。

Implementation-specific details：

- Vue；
- Vue Router；
- Vite；
- `index.html` / `party.html`；
- `dist/` layout；
- current Playwright filesystem location。

这些实现细节继续允许存在，但不得进入 Site Package / migration data contract。

## 8. Provisioning lifecycle

目标生命周期：

```text
Fresh DB
  ↓ Flyway schema
Generic CMS Core ready
  ↓ apply JilinJobs Site Package
Stable Site Structure ready
  ↓ import Canonical Historical Content
Runtime Content ready
  ↓ run Public/Admin/Integration verification
Accepted Runtime
```

Site Provisioning 至少需要：

1. preflight validation；
2. dependency-aware apply；
3. stable identity reconciliation；
4. idempotent second apply；
5. conflict detection；
6. upgrade path；
7. verification report；
8. failed pre-integration rollback / clean rebuild strategy。

当前项目以 Fresh DB 可重建为首要恢复路径，不要求为所有未来生产环境立即设计复杂在线 package rollback engine。

## 9. Repository / directory boundary

本 Specification 不要求拆 Repository。

当前推荐状态：

- 先让代码 / 数据 / Asset ownership 在同仓内可清晰分类；
- 只有完成 Site Package 后，再评估 Public Renderer 是否具备低成本独立仓库条件；
- Docs / Code 分仓、多代码仓 Workspace Composition 属独立后续 Architecture / Method Experiment。

如果未来拆仓，应把 CMS Core、Site Package、Public Renderer 视为平级组件，由独立 Workspace / Integration Authority 组合，而不是默认把 Public Renderer 作为 CMS Repo 的 Git Submodule 子模块。

## 10. Acceptance obligations for planning

Technical Planning 必须在 `slice-work` 前给出：

1. V2 全量 classification table；
2. Site Package 最小格式与目录 proposal；
3. provisioning mechanism 选择与 rejected alternatives；
4. stable identity / dependency resolution；
5. static asset composition；
6. Fresh DB / second apply / upgrade verification；
7. Party canonical dataset compatibility；
8. E1～E3 dependency update；
9. candidate slices，确保每个 slice 可独立验证且不会以“通用化”为理由扩大到无关重构。

## Deferred decisions

- Public 技术栈替换；
- SSR / SSG / Hybrid；
- Repository split；
- Git Submodule；
- Docs / Code 分仓；
- Multi-repository Workspace implementation；
- Generic multi-site SaaS / tenant model；
- Site Package marketplace / plugin framework；
- 无证据的大规模目录重命名。
