# EU-30 — Post-Incident Comprehensive Review

## Status

**CURRENT — remediation complete; machine evidence current for code state, final Review Environment / Human Review pending**

Review date: 2026-09-05  
Branch: `feature/eu-30-carousel-convergence`  
PR: #58

## 1. Purpose

在 EU-30 曾出现错误 Root Cause 分类、Review Workflow 陈旧契约、测试 retry、工具写入误判以及实现边界遗漏后，对整个 EU-30 改动执行一次不依赖既有结论的事故后复核。

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

### R-09 Theme article repeated one source image into multiple Runtime Resources

主题教育 INTERNAL 历史正文多次引用同一张源图片；Canonical Dataset 为保留来源 occurrence evidence，会多次记录相同 stable migration token。旧 EU-30 Importer 会按 occurrence 重复上传 `CmsResource`，并把多个重复 Resource 关联到同一 Article。

整改：

- Canonical occurrence evidence 保持不变，不篡改来源快照；
- Importer 先对每个 occurrence 逐条执行路径、size、SHA-256 与角色/token 校验；
- 校验通过后再按稳定 migration token 去重 Runtime upload / Article association；
- BODY_IMAGE token 必须为 `migration-resource://{sha256}`，ATTACHMENT 必须为 `migration-attachment://{sha256}`；
- Canonical Runtime reconciliation 直接查询 `cms_article_resource`，要求 `zhutijiaoyu:content:154659859759104` 最终只有 1 条 BODY_IMAGE 关联。

### R-10 Backend write contract was looser than Public read contract

Public 严格解析后，Backend 一度仍允许 `+5`、带空白值或其他与公开层不一致的 INTEGER 表达，存在“保存成功但公开 fallback”的契约分裂。

整改：

- 两个 `CAROUSEL_*` 稳定键的新写入按完整十进制正整数校验；
- 写入前 trim，`" 5 "` 统一持久化为 `"5"`；
- `+5 / 5abc / 1.5 / 0 / -1` 均拒绝；
- Backend Unit Test 与 Public Browser strict-history test 分别覆盖写入和读取边界。

### R-11 Editable SiteProperty valueType could bypass carousel integer semantics

管理端允许编辑网站属性定义。若稳定 `CAROUSEL_*` Key 被从 `INTEGER` 改成 `TEXT`，原实现后续值更新可能绕过正整数校验。

整改：

- 正整数语义绑定稳定 Key，而不只依赖数据库当前 `value_type`；
- 定义更新试图把两个 Key 改成其他类型时直接拒绝；
- 即使历史数据库已发生 `value_type=TEXT` 漂移，值更新仍按 INTEGER 规则校验；
- Unit Test 覆盖 definition bypass 与 drifted-row 防御。

### R-12 `CAROUSEL_MAX_ITEMS` was not preset-protected in V19

旧 `HOME_CAROUSEL_INTERVAL_SECONDS` 已在 V12 标记 preset；V19 原地重命名后继承该保护，但新插入的 `CAROUSEL_MAX_ITEMS` 最初遗漏 `preset=1`，后台可以删除稳定 Runtime 配置定义。

整改：

- V19 显式保持 `CAROUSEL_INTERVAL_SECONDS.preset=1`；
- 新增 `CAROUSEL_MAX_ITEMS` 时显式 `preset=1`；
- Browser E2E 要求两个键同时满足 `valueType=INTEGER / preset=true / value>0`；
- 删除两个稳定定义均必须被 Backend 返回 400 拒绝。

## 3. Review Correction: EXTERNAL_LINK ARTICLE DTO false positive

事故后复核过程中曾把 EXTERNAL_LINK ARTICLE 轮播误判为“公开 DTO 缺少 `externalUrl`，因此轮播不可点击”，并短暂尝试让前端读取 `item.externalUrl`。Public type-check 立即以 TS2339 暴露该错误判断。

重新读取 `CmsListService.resolveItem()` 后确认现有契约本来正确：

- ARTICLE 持久化列表项自身 `url` 会归一为空，不是目标地址 Authority；
- INTERNAL ARTICLE 的公开 `item.url=null`，Main / Party 根据 `articleId` 生成自己的 canonical route；
- EXTERNAL_LINK ARTICLE 的当前 `Article.externalUrl` 由 Backend 在公开查询时解析进**通用 `CmsListItem.url`**；
- Public DTO 不需要第二个 `externalUrl` 字段。

整改处理：

- 错误前端字段改动已完整撤回；
- `public-site-frontend.md` 与 `party-frontend.md` 已明确该 DTO 契约，避免 Fresh Context 再次误读；
- 保留独立 Browser 回归：创建 ARTICLE placement 时故意提交无效 list-owned URL，随后修改 Article 当前外链，并验证 Main / Party 轮播都跟随更新后的 Article URL，`DEFAULT` 外链使用新窗口。

此项属于**复核过程中的错误诊断被验证系统及时拦截**，不记录为产品 R-13。

## 4. Candidates Reviewed but Not Classified as EU-30 Defects

### Hidden Party slides and keyboard focus

候选风险：非当前 slide 使用 `aria-hidden`，单独看 DOM 可能仍进入 Tab 顺序。

复核 CSS 后排除：`.party-carousel-item` 非 active 状态同时使用 `visibility:hidden`，正常键盘焦点不会进入隐藏链接。因此不增加额外 tabindex 状态机。

### Failed image ID lifetime

共享 composable 在同一 mounted lifecycle 中记住失败 item ID。如果后台在不重挂载页面的情况下热更新同 ID 图片 URL，不会自动重试。

当前站点没有实时订阅/热更新列表数据，正常导航重挂载会清空失败集；不属于 EU-30 当前 Runtime contract，记录为低风险后续增强点，不扩范围。

### Public Resource exposure

复核 ResourceMapper / Service 后未发现“知道 Resource ID 即公开”的扩大暴露。公开图片仍必须通过已发布 Article COVER/BODY_IMAGE，或 enabled ARTICLE list override + published article 的关系取得资格；撤回、列表停用或删除关系会收回公开资格。

### Party visual runtime structure

静态复核确认 Party 轮播仍保持 Desktop `585×329`、Mobile `585:329`、`object-fit:cover`、非活动项 `visibility:hidden` 与 reduced-motion transition none；未发现本轮共享生命周期把视觉所有权错误抽入 Shared。

## 5. Current Machine Evidence

### 5.1 Code Evidence Commit

事故后整改代码与自动验证状态收敛到：

`821a8a34f0f6063c747e6872d6091ed29014c299`

随后从 `821a8a...` 到 Authority 文档修订 Head `9e4c3a20a3bc1ec68eefa30c36ed4f726523480f` 的 compare 为 **2 个 docs-only commits**：

- `docs/technical/public-site-frontend.md`：明确 EXTERNAL_LINK ARTICLE 当前地址解析到通用 `CmsListItem.url`；
- `docs/technical/party-frontend.md`：同步 Party 端相同契约。

该 compare 不修改 Requirement、Runtime Code、Flyway、Migration Dataset、Importer、Workflow、Fixture、Test、资源或环境配置，因此以下 `821a8a...` Machine Evidence 可按 `docs/technical/verification-strategy.md` 的 Descendant Commit Evidence Reuse 规则复用于这些具体行为 Claim；它们仍应描述为 **Evidence Commit 的 Run**，不是后继文档 Head 自己重新执行的 Run。

### 5.2 CI #620

- Run: `33930893118`
- Head: `821a8a34f0f6063c747e6872d6091ed29014c299`
- Result: PASS
- Backend verify: PASS
- Public build: PASS
- Admin build: PASS
- Integrated Browser: PASS
- Public Playwright: **47 passed / 3 expected skipped / 0 retry**
- Admin Playwright: **32 passed / 0 retry**
- Playwright Artifact: `9958570839`
- Artifact SHA-256: `a267c7204c2e03f54b074093619f73db380f01124cd8ad0244fab1091a337bf9`

其中事故后新增回归均 PASS：

- 严格历史轮播整数解析；
- 稳定轮播 Key preset/type/delete protection；
- 图片失败补位并保持当前 item identity；
- EXTERNAL_LINK ARTICLE 在 Main / Party 跟随 Article 当前外链；
- ARTICLE 发布/撤回、REQUIRED image、Resource 公开关系。

### 5.3 Canonical Migration Verification #84

- Run: `33930893082`
- Head: `821a8a34f0f6063c747e6872d6091ed29014c299`
- Result: PASS
- Artifact: `party-canonical-verification`
- Artifact ID: `9958531465`
- Artifact SHA-256: `f70675aac4c126c72b95a68ab619e7946ab690ce8cb09422a7d23e6eb183a845`

已证明：

- EU-29 acceptedSnapshot 保持 181；
- EU-30 current runtime dataset 为 183；
- Fresh first article import 183 CREATED；
- first carousel import 4 CREATED；
- second articles 183 SKIPPED；
- second carousel 4 SKIPPED；
- Party migration scope 40 / 88 / 22 / 31 / 2 对账；
- position 2 ARTICLE legacy mapping 与 override bytes 正确；
- 主题教育目标文章最终 BODY_IMAGE Runtime association 数量为 **1**。

### 5.4 EU-30 Migration Upgrade Verification #32

- Run: `33930893093`
- Head: `821a8a34f0f6063c747e6872d6091ed29014c299`
- Result: PASS
- Artifact: `eu30-migration-upgrade-verification`
- Artifact ID: `9958531784`
- Artifact SHA-256: `6f92d2dafddbada822a4bd5ea7e4a89cc8cc4611d73f2064f8e3ad12a81ce4c9`

已证明：

- baseline 固定取自 accepted EU-29 commit `59c855f...`；
- EU-29 first runtime: 181 articles + 4 LINK carousel items；
- 同一 DB 升级到 EU-30：2 CREATED + 181 SKIPPED；
- position 2 保持原 list item ID，原地 LINK → ARTICLE；
- article legacy identity、动态 Runtime article/resource IDs 和原 PNG SHA 对账；
- 第二次 EU-30 import 全幂等；
- 人为把 position 2 mapping fingerprint 改成未知值后 Importer 非零退出，并报告 `conflicts=1 / invalid=0 / position2=CONFLICT`。

## 6. Remaining Evidence Boundary

早期 Head `7210234d...` 的 CI / Canonical / Upgrade / Review #526 只保留历史追溯，不再承担当前完成声明。

目前尚未完成的不是上述代码行为 Machine Evidence，而是**最终 Head 的 Review Environment / Human Review 生命周期**：

1. 最终文档收敛后锁定 Branch Head；
2. 让该最终 Head 的普通 Review Environment 自动链路完成 build / AI-Browser / clean reset / canonical import / Party Runtime Browser / FRP / external runtime / owner evidence；
3. 自动链路稳定后，为完全相同的最终 Head 单独取得 `human-review` 长租约；
4. 项目负责人执行 Main / Party Desktop + Mobile、轮播视觉与交互、position 2 主题教育文章和 2 条主题教育历史内容 Human Review；
5. Human Review PASS 前，manifest 继续 `candidate-extension / pending-human-review`，PR #58 继续 Draft，EU-30 继续 CURRENT。

## 7. Exit Condition

EU-30 仅在以下条件全部满足时关闭：

1. 本事故后复核没有未处理的 Authority-backed 高优先级 Finding；
2. 当前代码状态的 Backend / Public / Admin / Integrated Browser Evidence PASS 且无未解释 retry；
3. Canonical Fresh DB / idempotency / Runtime reconciliation PASS；
4. pinned EU-29 → EU-30 same-runtime upgrade + unexpected fingerprint CONFLICT PASS；
5. 最终 Head Review Environment automatic verification PASS；
6. 最终 Head Human Review PASS；
7. Human Review 后再接受 candidateExtension，并保留 EU-29 acceptedSnapshot provenance；
8. Roadmap / Execution Unit 收口为 EU-30 completed、EU-31 CURRENT；
9. PR 转 Ready for Review；最终 merge 仍需项目负责人明确指令。
