# 需求 / 领域权威（Requirement / Domain Authority）

`docs/requirements/` 根目录只保存当前仍承担长期 Product / Domain Requirement 责任的 canonical owner。

## 当前长期 owner

- `information-publishing.md` — 信息发布与网站服务的产品目标、范围、用户、长期可观察行为与项目级验收不变量；
- `cms-domain.md` — CMS business objects、stable/source identity、state / lifecycle、cross-object rules、content ownership、Historical Migration domain semantics 与 failure invariants。

Feature 的用户可观察行为进入 `docs/specifications/`；跨 Feature 的长期系统边界进入 `docs/architecture/`；实现 HOW 进入 `docs/technical/`。Requirement / Domain 文档不保存 Execution Unit、PR / Actions、exact Head、migration inventory、源码目录或框架版本。

## Archive

`archive/` 保存已经被当前 Product / Domain / Architecture / Specification Authority 吸收、取代或纠正的历史 Requirement Change 与 clarification source，仅用于 traceability。

Archive 默认不参与 Fresh Context；只有当前 Authority 无法解释某个历史决策、需要审计 lineage 或验证来源时才定向读取。历史正文以证据保真为先，不因归档而改写。

## 维护规则

只有当事实属于长期 Product / Domain 语义并且没有更合适的现有 owner 时，才在本目录新增或修改 canonical Requirement。

不得通过新增并列 Requirement Change 文件长期覆盖已有 owner；变更完成后应把有效长期语义折回 canonical owner，并让过程性 source 退出根级 Current Authority。
