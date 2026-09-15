# Requirement / Domain Authority

`docs/requirements/` 保存当前 Product / Domain Requirement Authority，以及在 Issue #153 Foundation Rebuild 完成前仍待语义收敛的 Requirement source。

## Canonical owners

普通 Fresh Context 优先从以下长期 owner 恢复：

- `information-publishing.md` — 信息发布与网站服务产品目标、范围、用户、长期可观察行为与项目级 acceptance invariants；
- `cms-domain.md` — CMS business objects、stable/source identity、state / lifecycle、cross-object rules、content ownership、Historical Migration domain semantics 与 failure invariants。

这两个文件不维护数据库 migration 编号、源码目录、框架版本、Execution Unit、PR / Actions 或其他高频实现状态。

## Clarification sources under convergence

以下根级文件在 Issue #153 完成前仍保留 source / traceability 价值，但不再自动与 canonical owners 平级读取：

### 已完成 Feature / Requirement Change source

- `admin-guidance-governance.md`
- `list-definition-group-governance.md`
- `main-single-page-formal-content.md`
- `main-stable-listitem-site-package.md`
- `party-positioning.md`
- `rich-text-authoring.md`

其中长期事实必须逐项折回 Product / Domain / Architecture / Feature Specification owner；确认完全接管后再物理归档。

### Architecture / engineering convergence source

- `backend-application-core-boundary.md`
- `cms-site-package-boundary.md`
- `database-migration-baseline-convergence.md`
- `generic-content-migration-application.md`
- `page-content-architecture.md`
- `party-migration-despecialization-compatibility.md`
- `public-frontend-replaceability.md`

这些文件包含重要长期边界，但也混有旧 Phase、READY、EU、SHA、migration numbering 与 implementation facts。跨 Feature 的长期语义将在 Architecture Clarification 中进入统一 Architecture Context / State；历史施工状态不会成为新的 Requirement。

### Frozen migration / data boundary source

- `main-historical-content-migration.md`
- `data-migrations/**`

Main Historical Migration 当前仍有受控 reactivation / provenance 语义，但 migration dataset、counts、digest 与 acquisition evidence 由 `data-migrations/**` 自己持有，不复制进普通 Product Requirement。

## Lifecycle rules

- `archive/` 只保存已经明确 `SUPERSEDED / HISTORICAL_EVIDENCE` 的 Requirement Change / traceability 文档；
- 不按文件年龄、EU 编号或“已完成”机械归档仍有未接管长期语义的 source；
- Issue #153 的 source inventory / conflict matrix / extraction table 只保存在 Issue timeline，不晋升为长期 Markdown；
- Feature Specification 不应重新定义 CMS Domain baseline；
- Technical / code implementation facts不能反向扩大 Product Requirement；
- 如果 canonical Requirement / Domain 无法唯一裁决且不同答案会改变产品行为、长期边界或验收，必须回到 Requirement Clarification / Human Authority。
