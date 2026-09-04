# EU-30 — Post-Incident Comprehensive Review

## Status

**CURRENT — remediation implemented; final Current Evidence pending**

Review date: 2026-09-05  
Branch: `feature/eu-30-carousel-convergence`  
PR: #58

## 1. Purpose

在 EU-30 曾出现错误 Root Cause 分类、Review Workflow 陈旧契约、测试 retry 与工具写入误判后，对整个 EU-30 改动进行一次不依赖既有结论的事故后复核。

复核范围覆盖：

- Requirement / Specification / Technical Plan；
- Backend CmsList / Resource / SiteConfig；
- Flyway 与 Party Historical / Carousel Importer；
- Main / Party Runtime；
- Admin；
- Canonical / Upgrade / Review Environment Workflows；
- Browser E2E 稳定性；
- Consumer Method / Roadmap / Fresh Context 恢复信息；
- EU-30 durable migration candidate 与一次性采集资产。

## 2. Confirmed Findings and Remediation

### R-01 Public historical integer parsing was too permissive

此前 Main / Party 使用 `Number.parseInt` 解析轮播配置，`1.5`、`5abc`、`1e2` 等完整字符串非法值可能被截断后误当成有效值；V19 也曾使用 MySQL 宽松 `CAST` 处理旧间隔值。

整改：

- Public SiteConfig 在进入 Main / Party 前对两个轮播正整数键执行完整字符串校验；
- 非完整十进制正整数一律归一为空值，由 Site fallback 回到 4 / 5；
- V19 只接受完整十进制数字且数值大于 0，否则写入默认 4；
- Browser test 直接覆盖 `1.5 / 5abc / 1e2 / 空白 / 0 / -1`。

### R-02 Position 2 migration exception was broader than the accepted correction

为了支持 accepted EU-29 position 2 从 LINK 原地纠正成 EU-30 ARTICLE，Importer 曾对该 legacy key 的任意 fingerprint 变化放行。这会把一次性兼容口变成未来静默覆盖通道。

整改：

- 固定 accepted EU-29 fingerprint：`c2ad182b8b2dc981a3cbe3b0153a1e3e47604c1f01dd43e6d25971e1deed10dc`；
- 固定 EU-30 correction fingerprint：`f8b5d8df87021373803639b174bf88e46ae6cef7f2599a205763b5887c78be84`；
- 只有 `position 2 + ARTICLE + existing=exact EU29 + incoming=exact EU30` 才允许 UPDATE；
- 其他 fingerprint drift 必须 `CONFLICT`；
- Upgrade Workflow 新增伪造 fingerprint 后必须失败的负向回归。

### R-03 Upgrade verification depended on moving `main`

专项 Workflow 原先从 `main` materialize “EU-29 accepted baseline”。EU-30 合并后 `main` 自身会包含 EU-30，专项验证会失去历史基线语义。

整改：固定使用 accepted EU-29 commit：

`59c855f55899cd613fdee059b27db762ffa3b092`

并验证该 commit 中 manifest 仍是 181 / 4 carousel / position 2 LINK / accepted fingerprint。

### R-04 Temporary method amendment became stale after fold-back

`docs/project/development-method-baseline-amendment.md` 曾作为临时 overlay；随后相同 baseline 与 Stable Maintenance 规则已正式折回 `docs/project/development-method.md`，但临时文件仍标记 CURRENT，并错误声称主方法仍是旧 baseline。

整改：删除临时 amendment，恢复单一 Consumer-local Method authority。

### R-05 One-shot theme collection workflow survived durable promotion

EU-30 Theme Education collector 已完成采集并把 candidate 晋升为仓库 durable input，但一次性自动提交 Workflow 仍可手工触发。EU-30 合并后它会成为没有持续职责且可能重复尝试 merge candidate 的死资产。

整改：删除 `.github/workflows/eu30-theme-education-collect.yml`；保留 collector / merge script / durable dataset 与来源 Run provenance，供可重复取证而不是继续自动写仓库。

### R-06 PartyHome loaded a non-home column

Requirement 明确 `party-theme-education` 可路由访问，但不是 PartyHome 第五个固定内容区。PartyHome 曾调用加载全部五栏目 API，再只消费四栏目，形成不必要耦合。

整改：新增 `loadPartyHomeColumns()`，PartyHome 只加载四个固定栏目；完整 Party router scope 继续通过 `loadPartyColumns()` 支持五栏目。

### R-07 OPTIONAL ARTICLE image semantics were over-specified

技术方案一度写成 OPTIONAL ARTICLE 可“选择不使用图片”，但物理模型明确：`image_resource_id=null` 表示继承当前文章封面。因此文章已有封面时没有第三种“显式屏蔽封面”状态。

整改：

- 技术方案明确 null=inherit，不扩展数据库模型；
- OPTIONAL 无图只在文章本身没有封面且没有 override 时自然成立；
- 删除 Admin 中没有真实持久化语义的“不使用图片”按钮。

### R-08 Review column reconciliation was unnecessarily closed-world

Review Environment 曾要求 Party 父栏目下的全部子栏目数量必须与 migration manifest 恰好相等。未来新增与历史迁移无关的 Party 子栏目会让历史数据对账误报。

整改：仍查询 Party 子栏目，但只要求 manifest 声明的每个 migration-controlled alias 存在并具有正确计数；额外非迁移栏目不影响 canonical history verification。

## 3. Candidates Reviewed but Not Classified as EU-30 Defects

### Hidden Party slides and keyboard focus

候选风险：非当前 slide 使用 `aria-hidden`，单独看 DOM 可能仍进入 Tab 顺序。

复核 CSS 后排除：`.party-carousel-item` 非 active 状态同时使用 `visibility:hidden`，正常键盘焦点不会进入隐藏链接。因此不增加额外 tabindex 状态机。

### Failed image ID lifetime

共享 composable 在同一 mounted lifecycle 中记住失败 item ID。如果后台在不重挂载页面的情况下热更新同 ID 图片 URL，不会自动重试。

当前站点没有实时订阅/热更新列表数据，正常导航重挂载会清空失败集；不属于 EU-30 当前 Runtime contract，记录为低风险后续增强点，不扩范围。

### Public Resource exposure

复核 ResourceMapper / Service 后未发现“知道 Resource ID 即公开”的扩大暴露。公开图片仍必须通过已发布 Article COVER/BODY_IMAGE，或 enabled ARTICLE list override + published article 的关系取得资格；撤回、列表停用或删除关系会收回公开资格。

## 4. Evidence Policy After Remediation

Head `7210234d...` 的 CI / Canonical / Upgrade / Review #526 曾全部通过，但本次复核已经修改 Runtime、Migration、Admin、Workflow 与文档，因此该证据全部降级为 ancestor evidence。

整改后的最终 Head 必须重新取得：

1. Backend / Public / Admin build；
2. Integrated Browser 无未解释 retry；
3. Canonical Fresh DB import + idempotency；
4. pinned EU-29 → EU-30 same-runtime upgrade；
5. unexpected position 2 fingerprint conflict negative test；
6. Review Environment canonical import / Party Runtime Browser / external runtime；
7. 新 Head Human Review。

在以上 Current Evidence 完成前，EU-30 保持 CURRENT，PR #58 保持 Draft，candidateExtension 保持 `pending-human-review`。
