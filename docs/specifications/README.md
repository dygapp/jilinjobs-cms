# 产品规格（Product Specifications）

`docs/specifications/` 根目录只保存当前仍承担用户可观察行为、Failure Behavior 与 Acceptance 责任的 Product / Feature Specification。

## 当前规格 owner

- `admin-site.md` — CMS 管理端可观察行为与管理体验；
- `public-site.md` — Main / Party 公开访问行为、canonical URL、公共呈现与失败状态；
- `party.md` — 中心党建专题入口的 bounded 产品规格；
- `page-content-architecture.md` — 当前 Page content profile / renderer 相关的可观察行为与 Acceptance；
- `rich-text-authoring.md` — Rich Text authoring / compatibility / safety 的用户可观察 contract。

Specification 不重新定义 CMS Domain baseline，不拥有长期 Architecture State，也不维护 Vue / Spring / Gradle 等 implementation inventory。对应上游分别读取 `docs/requirements/` 与 `docs/architecture/`，跨 Feature HOW 读取 `docs/technical/`。

## Archive

`archive/` 保存已被后继 Requirement / Specification / Architecture 吸收或取代的历史 Feature Specification、Architecture convergence specification 与完成态 change contract，仅用于 traceability。

Archive 默认不参与 Fresh Context。历史正文不因归档而批量改写；需要审计 accepted change lineage 时按当前 Authority 定向读取。

## 维护规则

新的 Specification 必须有明确的当前产品/Feature responsibility，并只描述 Scope、Observable Behavior、Failure Behavior 与 Acceptance。Feature 完成后，如果其语义已经完整进入更长期的 canonical owner，原 Feature Specification 应退出根级 Current Authority，而不是永久形成同名三件套。
