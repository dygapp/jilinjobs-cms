# 产品规格（Product Specifications）

`docs/specifications/` 根目录只保存当前仍承担用户、运营人员或项目维护者可观察行为、Failure Behavior 与 Acceptance 责任的 Product / Feature Specification。本 README 只负责 Human Navigation，不拥有 Specification 正文事实，也不建立第二份 Specification inventory Authority。

## 当前规格 owner

- `admin-site.md` — CMS 管理端可观察行为与管理体验；
- `public-site.md` — Main / Party 全部公开访问行为、canonical URL、公开呈现、作用域、失败状态与 Acceptance；
- `page-content.md` — Page content profile、Structured Page 与相关 authoring / rendering 的可观察行为；
- `rich-text-authoring.md` — Rich Text authoring、compatibility、resource 与 safety 的用户可观察 contract；
- `content-migration.md` — 已获执行 Authority 时 Historical Content Migration 的 maintainer-visible preflight、result、failure、report 与 Acceptance。

中心党建不再由独立 Current Specification 与 `public-site.md` 平行持有。Party 的长期产品定位由 Product Requirement 持有，当前公开可观察行为统一从 `public-site.md` 恢复；历史独立规格仅在需要 traceability 时读取 `archive/party.md`。

Historical Migration 的长期 identity / fingerprint / accepted scope / compatibility 仍由 Domain Requirement 持有，canonical records 由 `data-migrations/**` 持有；`content-migration.md` 只拥有项目维护者可观察的受控执行结果，不授予 migration execute authority，也不解除 Main migration freeze。

## 功能规格责任

Specification 回答“用户、运营人员或项目维护者能观察到什么、边界和失败如何表现、什么结果算满足”。它不重新定义：

- Product / Domain Requirement 的长期业务事实与 identity / lifecycle / ownership；
- Architecture 的长期系统 structure / application boundary / replaceability decision；
- Technical / Interface Contract 的实现 HOW；
- Verification Strategy 的测试层次、工具选择或证据程序；
- Site Definition、canonical migration data、源码目录、框架版本或当前执行状态。

遇到跨 Feature 的新长期业务事实，回到真实 Requirement / Domain owner；遇到长期结构 driver，回到 Architecture；实现机制与验证程序分别进入 Technical / Verification owner。

## 归档

`archive/` 保存已被后继 Requirement / Specification / Architecture 吸收或取代的历史 Feature Specification、Architecture convergence specification 与完成态 change contract，仅用于 traceability。

Archive 默认不参与 Fresh Context。历史正文不因归档而批量改写；需要审计 accepted change lineage 时按当前 Authority 定向读取。

## 维护规则

新的 Specification 必须有明确的当前产品 / Feature responsibility，并只维护 Scope、Observable Behavior、Boundary / Failure Behavior、Acceptance 与必要的 feature-level non-functional obligation。Feature 完成后，如果其语义已经完整进入更长期的 canonical owner，原 Feature Specification 应退出根级 Current Authority，而不是永久形成同一语义的平行 owner。
