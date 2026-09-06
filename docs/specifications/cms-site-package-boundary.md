# CMS Core / Site Package / Public Renderer 边界 Specification

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- GitHub Issue #77
- `docs/specifications/public-frontend-replaceability.md`
- `docs/technical/cms-architecture.md`
- `data-migrations/README.md`
- `docs/specifications/preset-site-structure.md`

## Status

- Specification: **ACCEPTED / ACTIVE**
- Technical Planning: **ACTIVE** — initial plan 已完成，剩余 boundary 继续按 current evidence 收敛
- Completed Execution Units: **EU-37 / EU-38 / EU-39 / EU-40**
- Current Ready Execution Unit: **NONE**
- Issue #77: **OPEN**

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

`preset` 的语义为：**由某个受控 Site Provisioning 建立、具有稳定 identity 并受结构保护的对象**。它仍是通用 CMS 能力。

EU-37 已证明 Site Provisioning 可以建立在 Generic Flyway V1 Schema 上，不要求 Site-specific V2 先存在。

EU-39 增加的 `cms_navigation.code` 也是 Generic CMS Core 的 provisioning capability：字段 nullable，普通 operator-created NavigationItem 不要求 code；具体 JilinJobs navigation code/value 只属于 Site Package。

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

长期关系不得依赖 Runtime 自增 ID。当前已接受：

- Column → `alias`；
- PageGroup → `alias`；
- Page → `groupAlias + alias`；
- NavigationLocation → `code`；
- NavigationItem → provisioning-only `code`；
- CmsList → `code`；
- AdvertisementSlot → `code`；
- SiteConfig → `key` / `config_key`。

EU-38 已使用除 NavigationItem 外的上述 stable identities 接管七类 structure definition。

EU-39 current audit 已证明 NavigationItem 的名称、位置/父子关系、排序和 target 都是可变语义，不能可靠组成长期 composite identity。因此 NavigationItem 正式采用独立 stable `code`：

- `code` 只承担 Site Provisioning identity；
- package parent relation 使用 `parentCode`；
- Column target 使用 Column alias；
- Page target 使用 `groupAlias + alias`；
- location 使用 NavigationLocation code；
- ordinary operator-created navigation 允许 `code = NULL`；
- stable code 不暴露为普通 Admin 可编辑产品字段，也不进入 Public canonical URL contract。

### Legacy V2 adoption

Existing V1+V2 database 中的 40 条旧 preset navigation 没有 stable code。EU-39 接受以下升级规则：

1. 只观察 `preset=true AND code IS NULL` 的 legacy candidate；
2. 按当前 accepted package semantics 解析 parent / target / location 后进行完整语义匹配；
3. 必须恰好唯一匹配才允许原位写入 code；
4. 0 个或多个匹配都必须失败；
5. adoption 在 Site Package transaction 中执行，任何一条失败时整体回滚；
6. 一旦取得 code，后续 reconcile 只按 code 定位，允许 package 管理的 rename / move / reorder / retarget。

该规则只用于从 pre-identity accepted preset baseline 收敛到 stable identity，不把 mutable composite key 提升为长期 Authority。

### Versioning

Site Package 使用明确 `packageId` / `schemaVersion` / package `version` / structure digest。Git commit 记录历史变更，Runtime Provisioning 通过 package manifest 校验目标 package integrity。

当前 Site Package schemaVersion = `1`；EU-38 / EU-39 对 structure type 的扩展保持 EU-37 `columns-only` Foundation contract 兼容。JilinJobs package version 当前为 `0.3.0-navigation-identity`。

当前阶段不要求设计通用插件系统或支持任意第三方 Site Package Marketplace。

## 4. Flyway / V2 boundary

Flyway 继续负责数据库 schema 演进，并允许保留真正属于 CMS Core 的数据库级初始化元数据。

EU-38 已完成 V2 的第一轮 current classification，并将具有现成 stable identity、可独立 reconcile 的七类 Site-specific preset structure 表达进 Site Package：

- Column；
- PageGroup；
- Page；
- NavigationLocation；
- SiteConfig；
- CmsList definition；
- AdvertisementSlot。

EU-39 继续完成 NavigationItem identity/reconcile：

- Flyway V3 只增加 nullable `cms_navigation.code` 与唯一索引；
- 具体 40 条 JilinJobs navigation definition 位于 Site Package，不进入 V3；
- Legacy V2 navigation 由 provisioner 无歧义原位 adoption；
- CmsListItem / Advertisement 继续属于运营成员，不因为存在于 V2 就自动成为 preset structure；
- Historical / operational content 继续进入 Canonical Migration，而不是 Site Package。

`V2__current_preset_data.sql` 在 EU-38～EU-40 中均保持未修改。EU-40 已让正式 Repository Runtime / importer 路径通过 `cms.site-package.root` 显式执行 Site Package reconcile，但 V2 仍承担当前 compatibility initialization / operational seed responsibility，因此 **V2 responsibility 尚未移除**。

后续若要移除 / 缩减 V2 的 Site-specific responsibility，必须显式证明 Fresh DB recovery、Existing DB compatibility、Site Package idempotency、Runtime equivalence、Review Environment reproducibility 与受影响 canonical migration compatibility；不得以“Site Package 已存在”为理由静默迁空 V2。

## 5. Static asset boundary

当前 `site-baseline/static/**` 语义上属于 JilinJobs Site Package，但 EU-37～EU-40 没有改变其物理目录或 Runtime mount。

长期要求：

- stable site assets 与当前 Public Renderer 源码分离；
- Runtime 仍可由 Backend `/static/**` 提供；
- Renderer 只依赖稳定 URL / Site data，不依赖资产在 Git 中的物理目录；
- Historical article/body assets 继续跟随 Canonical Migration unit，不并入 Site Package；
- Runtime uploads 继续与版本化 Site assets 分离。

是否把目录从 `site-baseline/` 改名 / 移到 `sites/jilinjobs/` 由后续 Slice C 根据真实 ownership ambiguity、diff 成本与 Runtime composition 证据决定，不构成 Requirement，也不因目录形式而自动执行。

## 6. Historical Migration boundary

`data-migrations/**` 保持现有 Historical Content Migration 语义。

Main / Party Canonical Migration 可以依赖 Site Package 的 stable identities，但必须满足：

- 不依赖 Runtime numeric ID；
- 不依赖 Vue Router / component / Vite artifact；
- Import 前应验证所需 target Site identity 存在；
- Site Package 先 provisioning，Canonical Migration 后 import；
- Site Package 升级不得静默破坏已有 Canonical identity。

EU-40 final Head 已通过 Canonical Migration Verification #153 与 EU-30 Migration Upgrade Verification #103，证明正式 importer 现在按“Flyway compatibility baseline → Site Package reconcile → Canonical import”顺序运行且保持 183 篇 Party Runtime Dataset、carousel、idempotency 与 upgrade compatibility；PR #86 合并后的 `main@b105e553db1ebbc12a2b6665385b94fb977bea06` 又通过 Site Package #20 与 CI #769。该证据关闭了 EU-40 的 Runtime/importer activation scope，但 operational seed Authority、stable assets 与最终 Slice D/E1～E3 re-entry 仍未关闭。

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

EU-40 不改变 Public/Admin contract；exact-head CI #768 与 Post-Integration CI #769 的 Site Package-enabled Web Runtime、Public/Admin build 与 Integrated Browser 已证明当前可见行为保持。

## 8. Provisioning lifecycle

长期目标生命周期：

```text
Fresh DB
  ↓ Flyway Generic Schema
Generic CMS Core ready
  ↓ apply JilinJobs Site Package
Stable Site Structure ready
  ↓ import Canonical Historical Content
Runtime Content ready
  ↓ run Public/Admin/Integration verification
Accepted Runtime
```

EU-37～EU-40 已实现并验证的 Provisioning / composition contract 包括：

1. preflight validation；
2. dependency-aware apply（已实现 domains）；
3. stable identity reconciliation；
4. idempotent second apply；
5. `preset=false` ownership conflict / non-takeover contract；
6. package path / SHA-256 integrity；
7. deterministic verification；
8. Fresh Generic Schema + Site Package 与 Legacy V1+V2(+V3 schema evolution) + Site Package stable-structure equivalence；
9. NavigationItem Fresh create / Legacy in-place adoption / stable-code reconcile / ambiguous-adoption rollback；
10. `cms.site-package.root` opt-in lifecycle 在 Flyway 后执行 reconcile；未配置 root 时 Generic CMS context 保持不启用；
11. Repository Web Runtime、Canonical / Upgrade importer 与 Review Environment 复用同一 Site Package composition capability。

仍未完成的 lifecycle responsibility：

- V2 中 operational seed 的长期 Authority classification 与 compatibility responsibility retirement；
- stable Site Asset composition；
- 完整 canonical historical migration lifecycle compatibility；
- package definition 删除项的 deprovision semantics（当前默认不自动删除）。

当前项目仍以 Fresh DB 可重建为首要恢复路径，不要求为所有未来生产环境立即设计复杂在线 package rollback engine。

## 9. Repository / directory boundary

本 Specification 不要求拆 Repository。

当前状态：

- 代码 / 数据 ownership 已开始在同仓内清晰分类；
- Site Package v1 已位于 `sites/jilinjobs/**`；
- 当前 Public Renderer 继续与 CMS Core 同仓，但 source authority 已由 EU-36 隔离；
- 只有完成剩余 V2 responsibility / asset / canonical compatibility 后，再评估 Public Renderer 是否具备低成本独立仓库条件；
- Docs / Code 分仓、多代码仓 Workspace Composition 属独立后续 Architecture / Method Experiment。

如果未来拆仓，应把 CMS Core、Site Package、Public Renderer 视为平级组件，由独立 Workspace / Integration Authority 组合，而不是默认把 Public Renderer 作为 CMS Repo 的 Git Submodule 子模块。

## 10. Completed and remaining acceptance obligations

### 已完成

1. Site Package 最小 manifest / schema / integrity contract；
2. narrow provisioner、stable identity reconciliation、second apply idempotency、ownership conflict；
3. Fresh Generic Schema 上可 provision；
4. 七类 EU-38 stable preset domains 的 V2 classification 与 Site Package representation；
5. NavigationItem stable `code`、40 条 JilinJobs navigation package representation 与 Legacy adoption / reconcile；
6. Fresh / Legacy Site Package structural equivalence；
7. operator-created NavigationItem 不接管、运营成员不迁移；
8. `V2__current_preset_data.sql` 在 EU-40 保持不变；
9. EU-39 final Head `b95285f5424d4df0b9f9943395e80332296754f7` 的 Site Package #15、CI #761、Canonical #152、Upgrade #102、Review Environment #678 全部 PASS；
10. PR #84 已合并为 `main@36276ed65e6f3edbe96ffc18c01cf18ab924837b`，Post-Integration Site Package #16 与 CI #762 全部 PASS，EU-39 正式 COMPLETED；
11. EU-40 final Head `b3e3dc8c4855c9e17b2dfa2f190a84d0305162e2` 的 Site Package #19、CI #768、Canonical #153、Upgrade #103、Review #683 全部 PASS；
12. PR #86 已合并为 `main@b105e553db1ebbc12a2b6665385b94fb977bea06`，Post-Integration Site Package #20 与 CI #769 全部 PASS，EU-40 正式 COMPLETED。

### 剩余

1. Operational Seed Classification & V2 Responsibility Retirement；
2. static asset ownership 与 Runtime / CI / Review Environment composition；
3. Party canonical dataset 的完整 Site Package lifecycle compatibility；
4. accepted 183 Runtime Articles / Party carousel / provenance 在最终 Site Package lifecycle 下的 compatibility；
5. E1～E3 dependency update 与 re-entry gate；
6. 在完成四层 boundary 后的 Repository Split Readiness Assessment（独立评估，不自动拆仓）。

这些剩余项必须通过后续 current audit / `slice-work → readiness-check` 形成新的 Ready Execution Unit；本 Specification 不预先给它们分配 EU Identifier，也不继承 EU-40 的 Execute 授权。

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
