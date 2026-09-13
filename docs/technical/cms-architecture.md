# CMS 总体技术架构

## 1. 目的

本文描述 `jilinjobs-cms` 当前 Generic CMS Core、CMS Server、Content Migration、JilinJobs Site Package、Historical Migration、Admin Site 与 Public Renderer 的长期技术边界。

长期数据 / 生命周期解释遵循 GitHub Issue #77 与：

- `docs/requirements/cms-site-package-boundary.md`；
- `docs/specifications/cms-site-package-boundary.md`；
- `docs/technical/cms-site-package-boundary.md`。

## 2. 当前应用拓扑

Backend 已完成 application / core 拆分：

```text
backend/
├── modules/
│   └── cms-core
└── apps/
    ├── cms-server
    └── content-migration
```

依赖方向：

```text
cms-server ───────────→ cms-core
content-migration ────→ cms-core
```

- `cms-core`：site-neutral domain、persistence、transaction、Site Package capability、Generic migration primitives 与 shared configuration；
- `cms-server`：Admin/Public HTTP transport、MVC、static HTTP exposure 与 ordinary server lifecycle；
- `content-migration`：Generic canonical migration command 与 Party / bounded compatibility adapter；
- 两个 application 不互相依赖，也不通过 Server root application 间接共享 lifecycle。

Frontend：

```text
frontend/
├── admin/          # 独立 Admin SPA
└── public-site/    # Main + Party multi-entry public renderer
```

Admin 与 Public 都消费 Backend contract；Frontend 不成为第二份业务数据 Authority。

## 3. 四层数据 Authority

Current responsibility：

```text
Generic CMS Core
        ↓
JilinJobs Site Package
        ↓
Historical Content Migration
        ↓
Runtime CMS Data
        ↓
Replaceable Public Renderer
```

- Generic Core：site-neutral schema / domain / API / provisioning / migration capability；
- Site Package：JilinJobs stable structure、accepted Page defaults、one-time bootstrap、stable assets；
- Historical Migration：canonical historical content、resources、provenance、legacy identity、fingerprint、compatibility；
- Runtime：operator-managed content / configuration / uploads；
- Public Renderer：消费 public contracts 并实现页面呈现，不持有产品数据定义。

## 4. Current Flyway lineage

Current migration directory：

`backend/modules/cms-core/src/main/resources/db/migration/`

Active append-only lineage：

```text
V1__current_cms_schema.sql
V2__site_provisioning_schema_capabilities.sql
V3__page_content_migration_mapping.sql
V4__page_content_architecture.sql
```

含义：

- V1：current Generic CMS base schema；
- V2：site-neutral provisioning / bootstrap-state capability；
- V3：Generic Page content migration mapping capability；
- V4：Page Content Architecture，包含 `content_model / renderer_key / content_owner / structured_payload`。

Backend Flyway 不写 `notice`、`party`、Main navigation/list values 或其他具体 JilinJobs instance rows。

EU-31 / EU-41 文档中的 V1/V2 baseline 与“下一次从 V3 开始”只属于历史 baseline convergence evidence。Current 后续 Generic Schema evolution 必须从 **V4 之后**继续 append-only。

## 5. Site Package composition

`sites/jilinjobs/**` 责任：

```text
structure/**   # stable Site structure / accepted Page defaults
bootstrap/**   # explicit one-time initial Runtime data
assets/**      # stable Site asset source / integrity
```

Fresh Runtime 基本组合：

```text
Generic Flyway V1～V4
→ Site Package stable structure reconcile
→ stable asset projection
→ explicit one-time bootstrap（仅需要 Fresh defaults 的场景）
→ optional Historical Canonical Migration
→ Runtime verification / application
```

测试代码不得创建第二份 JilinJobs stable baseline。

## 6. Page Content Architecture

Page 当前不再使用单一 `renderMode` 混合表示内容形态、renderer 与 ownership。

Current orthogonal contract：

- `contentModel`：`RICH_TEXT / STRUCTURED / NONE`；
- `rendererKey`：稳定 renderer identity；
- `contentOwner`：`OPERATOR / SITE_PACKAGE / ENGINEERING / EXTERNAL`；
- `bodyHtml`：Rich body；
- `structuredPayload`：Structured body；
- `embedUrl`：integration metadata。

Current `guide/jypq` 为 `STRUCTURED + JILINJOBS_GUIDE_CARDS + CARD_COLLECTION V1`。Public renderer 通过显式 registry dispatch，禁止 alias/path/DOM heuristic fallback。

## 7. CMS domain ownership

### Column / Article

Column / Article 业务契约以 `docs/specifications/cms-core.md` 和 `docs/technical/backend-service.md` 为 Current owner。Article source identity、publish lifecycle、image policy、Public projection 不由 Frontend 或 Migration layer重新定义。

### Navigation / CmsList / Advertisement / SiteProperty

- stable JilinJobs containers / structure 由 Site Package 定义；
- ordinary Runtime members / values 按各领域 current contract 维护；
- Main ListItem 当前通过 Site Package one-time bootstrap 初始化，执行后属于 ordinary operator-managed Runtime Data，不建立 stable ListItem reconcile；
- `PARTY_CAROUSEL` Historical members 继续按 Party migration Authority 处理。

## 8. 配置治理

配置责任以 `docs/technical/configuration-governance.md` 为 Current Authority：

- 稳定领域 / 安全 / protocol contract → code constant；
- 运营可维护数据 / 低风险站点行为 → CMS Runtime data / SiteProperty；
- 低频结构 metadata → CMS metadata resource；
- 部署实例差异 → Spring externalized configuration；
- Site Package product source → `sites/jilinjobs/**`；
- CI / Review / FRP parameters → GitHub / deployment variables。

不得为了消除字面常量机械配置化。

## 9. 静态资源

Runtime `/static/**` 由 Backend 提供。Ownership 分为：

```text
sites/jilinjobs/assets/**       # stable Site source
/static/uploads/**              # mutable Runtime uploads
data-migrations/**/assets/**    # historical migration resources
```

Stable asset manifest/catalog 持有 source/target/digest；Runtime protection 合并 Site Package stable targets、Spring fixed protected paths 与当前 CMS direct references。

## 10. 权限边界

当前 CMS Runtime 尚无完整认证授权体系。Controller / Service 不读取虚构用户身份，Admin 不创建不存在的角色差异。

未来统一认证 / 权限属于独立 Requirement；不得从当前架构文档提前实现。

## 11. Verification

CI / Verification 至少按变更风险覆盖：

- Backend tests + executable artifacts；
- Fresh MySQL full V1～V4 Flyway chain；
- Site Package structure / bootstrap / stable asset verification；
- Canonical Migration / compatibility（受影响时）；
- Admin/Public Vue-aware type-check + Vite build；
- Integrated Browser；
- 有视觉 Acceptance 时再增加 AI / Human Review。

Current Evidence、descendant evidence reuse、Review Environment 与 Human Review 规则以 `docs/technical/verification-strategy.md` 为准。