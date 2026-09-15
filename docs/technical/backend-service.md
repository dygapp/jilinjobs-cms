---
id: technical:backend-service
type: technical-contract
status: active
relations:
  requirements:
    - docs/requirements/cms-domain.md
  architecture:
    - docs/architecture/cms-architecture.md
  verification:
    - docs/technical/verification-strategy.md
updated_at: 2026-09-15
---

# Backend 跨 Feature 技术契约

## 1. 文档责任

本文只维护 Backend 在多个 Feature / Execution Unit 之间持续需要一致的 implementation coordination contract。

CMS 业务对象、身份与 lifecycle 由 `docs/requirements/cms-domain.md` 持有；CMS Server / Generic Core / Content Migration 的长期责任与依赖方向由 `docs/architecture/cms-architecture.md` 持有。本文不复制对象业务规则、active Flyway 文件清单、当前 task inventory、历史 EU 或当前验证结果。

## 2. Gradle 与 application topology

Backend 当前使用标准 Gradle multi-project：

```text
backend/
├── modules/cms-core
└── apps/
    ├── cms-server
    └── content-migration
```

依赖方向固定为：

```text
cms-server ──────────→ cms-core
content-migration ──→ cms-core
```

禁止 Core 反向依赖 application，也禁止 Content Migration 通过 CMS Server 间接获得 shared capability。

`backend/` 根工程继续作为 Repository 级 build / verification / migration / provisioning facade；具体 task 名、artifact path、plugin/version 与 classpath wiring 以当前 Gradle implementation 为准，不由本文建立第二份 inventory。

## 3. `cms-core`

Core 持有两个 application 真正共享的 implementation capability：

- CMS domain/service/persistence implementation；
- transaction 与 mapper/repository capability；
- Generic schema resources；
- Site Definition load / reconcile / bootstrap / stable asset projection capability；
- StaticResource / managed resource 的 shared policy implementation；
- canonical migration 所需的 site-neutral primitives；
- shared configuration / metadata capability。

Core 不包含 HTTP Controller、Server application root 或 site-specific migration command lifecycle。

领域规则如果需要改变，必须先回到 Domain / Product Authority；不得因为 Core 当前代码方便而在 Technical 层发明新业务语义。

## 4. `cms-server`

CMS Server 持有 ordinary Runtime 的 transport / application composition：

- Spring Boot Server application root；
- Admin / Public HTTP transport；
- server-only request / response adaptation；
- Runtime static/resource HTTP projection；
- Server-specific application configuration；
- 需要真实 Server composition 的 integration verification。

Server 可以消费 Core，但不拥有 Historical Content Migration command lifecycle。

Admin / Public contract 可以投影同一 Domain；Public projection 不泄漏 Admin-only endpoint responsibility。

## 5. `content-migration`

Content Migration 是独立 non-web application，持有：

- site-neutral canonical migration orchestration；
- load / preflight / execute / report 的 migration implementation；
- bounded site-specific compatibility adapter；
- migration-only mapping / command / verifier responsibility。

它依赖 Core，不依赖 Server，不启动 ordinary HTTP Server。

Generic migration package 不吸收 Main / Party 具体 dataset facts或 accepted compatibility constants；这些事实留在 migration workspace / bounded adapter。具体 CLI、package、dispatcher、task 名由 Repository implementation 持有。

## 6. Schema 与 Site Definition composition

Generic schema 与 JilinJobs instance initialization 分离：

```text
Generic schema ready
→ Site Definition reconcile
→ stable asset projection
→ optional one-time defaults
→ optional canonical migration
→ ordinary Runtime
```

约束：

- Generic schema migration 不写 JilinJobs instance rows；
- active migration corpus 自己拥有实际文件与编号；
- schema evolution 不借用 Site bootstrap / historical import 作为 migration transcript；
- Site Definition 的 stable structure / bootstrap / assets 分别遵循各自 lifecycle；
- migration/test 不复制第二份 Site baseline。

具体 package component inventory 由 `sites/jilinjobs/manifest.json` 与 implementation 持有。

## 7. Persistence / transport boundary

共享 Domain model、service、persistence 与 transaction capability尽量留在 Core；HTTP request/response adaptation 留在 Server；migration canonical model / report 留在 Migration application。

不得为了减少文件数量重新把 HTTP Controller、Domain Service、Migration adapter 混回同一 owner。

DTO / persistence field / API endpoint 是实现 contract，不反向成为 CMS Domain Authority。若它们与 Domain / Specification 冲突，先识别 stale implementation contract，而不是用代码事实覆盖产品语义。

## 8. Resource 与 Rich Content integration

Backend 是资源安全、public projection 与最终数据约束的 enforcement boundary：

- Admin authoring 可以使用 managed resource contract；
- Public response只暴露公开消费所需的 resource projection；
- Rich Text / structured payload 的业务语义由对应 Domain / Specification 持有；
- sanitization、path safety、real-media validation 等实现必须保持 fail-closed security boundary；
- Public client 不承担修复 Admin-only resource URL / contract 泄漏的职责。

具体 parser、library、controller、storage path 与 validator implementation 由代码和相关 Technical owner 持有。

## 9. Verification

Backend 变化按实际风险验证：

- Core / Server / Migration dependency boundary；
- affected unit/integration tests；
- executable artifact / application composition；
- schema / Fresh Runtime composition（受影响时）；
- Site Definition / migration integration（受影响时）；
- Public/Admin Browser behavior（contract 变化时）。

完整 Evidence contract 由 `docs/technical/verification-strategy.md` 与 live-discovered verification Rules 持有。

## 10. 不由本文拥有

- CMS Product / Domain facts；
- Main / Party 具体业务数据；
- active Flyway 编号 / 文件数；
- 当前 package / class / task inventory；
- 某个 EU 的 execution sequence / readiness；
- Workflow run / test count / Current Evidence；
- 已完成重构的历史过程。