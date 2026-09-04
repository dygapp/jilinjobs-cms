# EU-30 — Carousel Architecture & Behavior Convergence

## Status

**CURRENT — implementation complete, final verification / Human Review convergence**

工作分支：`feature/eu-30-carousel-convergence`

Draft PR：#58 `feat: 收敛 EU-30 轮播架构与行为`

本文是 `docs/work/frontend-follow-up-execution-units.md` 中 EU-30 占位规划启动后的执行态补充。占位文档继续提供 EU-31 后续路线；EU-30 当前事实、已确认方案、实现范围和退出条件以本文为准。

## 1. Goal

把 Main Site 与中心党建 Party 的轮播从两个局部实现收敛为：

- 通用 `CmsListItem` 可表达 LINK / ARTICLE 展示投放；
- Main / Party 共用稳定的无主题轮播生命周期；
- Main / Party 继续持有自己的视觉 DOM / CSS / 比例；
- 统一站点级轮播行为属性；
- 修复 Party 历史轮播第二项与遗漏主题教育内容的真实关系；
- 建立可重复、可审计的 Fresh DB / Browser / Human Review 证据。

不以“抽象更多”作为目标，不引入第三方 Carousel，不提前执行 EU-31 完整浏览器兼容工作。

## 2. Confirmed Decisions

### 2.1 CmsListItem

来源：

- `LINK`
- `ARTICLE`

ARTICLE 只是展示投放，不修改 Article 单一 `columnId`。

ARTICLE 只在 `PUBLISHED` 时公开有效；撤回自动退出公开列表。

列表项 `openMode` 是独立通用字段；ARTICLE 生成 Main `/article/{id}` 或 Party `/party/article/{id}` canonical route 后仍保留 `DEFAULT / SAME_WINDOW / NEW_WINDOW` 行为。

### 2.2 Image Policy

列表 `imagePolicy` 继续是通用数据契约：

- NONE
- OPTIONAL
- REQUIRED

ARTICLE 可继承文章主题图片、选择正文图片或使用列表专用 Resource 覆盖图。物理字段为 `cms_list_item.image_resource_id`；公开 API 使用 `effectiveImageResourceId` 表达继承/覆盖解析后的有效图片。

Main `HOME_CAROUSEL` 与 Party `PARTY_CAROUSEL` 均为 REQUIRED。ARTICLE 后续失去继承封面且没有覆盖 Resource 时必须退出公开 REQUIRED 列表。

### 2.3 SiteProperty

统一：

- `CAROUSEL_INTERVAL_SECONDS=4`
- `CAROUSEL_MAX_ITEMS=5`

两个属性均要求正整数；Backend 拒绝 0、负数和非整数新写入，Public 对缺失/异常历史值保留 4 / 5 fallback。

旧 `HOME_CAROUSEL_INTERVAL_SECONDS` 不再是 Runtime 依赖。

### 2.4 Shared Behavior

- 0 项稳定空态；
- 1 项静态；
- 多项自动循环；
- 手动 dots；
- hover pause；
- focus pause；
- 初始及后续 visibility pause；
- resume 不重置；
- reduced-motion 关闭 autoplay 和 transition；
- 图片失败退出有效集合并补位；
- 有效集合变化时按 item ID 保持当前项，避免数组下标变化造成无意跳转；
- EU-30 不实现 swipe；
- 不引入第三方 Carousel。

### 2.5 Visual Ownership

Main：稳定 `8:5`，`object-fit:cover`，轻量 fade。

Party：Desktop 约 `585×329`，Mobile `585:329`，Party-owned dots / caption / theme。

共享 composable，不共享视觉组件。

## 3. Party Historical Correction

EU-30 对 Party 历史轮播 position 2 反向取证后确认：

- 原站存在 `typeCode=zhutijiaoyu`；
- 新版栏目为 `party-theme-education / 主题教育`；
- 完整历史列表 2 条：1 INTERNAL + 1 EXTERNAL_LINK；
- `unresolved=0`；
- 不新增 PartyHome 第五个固定内容区；
- position 2 指向 `content_id=154659859759104` 的 INTERNAL 文章；
- 原轮播 PNG 保留为该 ARTICLE 投放的列表专用覆盖图；
- 原列表项 `NEW_WINDOW` 语义继续保留，但目标改为新系统 Party canonical article route。

EU-29 已接受 181 篇基线保持原 provenance；EU-30 的 2 条记录保留为 `candidateExtension / pending-human-review`，直到本 EU Human Review 通过。

## 4. Implemented Scope

当前分支已经实现：

- V19 数据模型与统一 SiteProperty migration；
- `CmsListItem.sourceType / articleId / imageResourceId`；
- public ARTICLE 发布状态过滤、REQUIRED 有效图片过滤和公开 Resource 权限；
- Admin LINK / ARTICLE 选择、文章图片继承 / 正文图片候选 / 覆盖图上传；
- `shared/carousel/useContentCarousel.ts`；
- Main / Party 切换到共享生命周期；
- 初始 hidden page timer 守卫；
- 图片失败时先过滤再 max-items，并按 item identity 保持当前内容；
- Main / Party INTERNAL ARTICLE canonical route 保留列表项 openMode；
- Backend 对两个统一轮播属性执行正整数校验；
- `party-theme-education` Party Router scope 与正确 `party` 父栏目；
- EU-30 theme collector 与 durable candidate merge；
- Historical Import V2；
- Party Carousel V2 `sourceSystem + legacyKey -> article_id` 解析；
- position 2 ARTICLE + Resource override；
- Canonical Verification 支持 acceptedSnapshot + candidateExtension 双层证据；
- Admin stale verification contract 已按 EU-30 更新；
- Public `carousel-convergence.spec.ts`、`carousel-edge-cases.spec.ts` 专项 E2E；
- Party canonical Runtime 测试支持 `[LINK, ARTICLE, LINK, LINK]` 并验证 position 2 Resource / canonical route / `NEW_WINDOW`。

### 4.1 Convergence Findings Closed During Review

实现后静态审查 / Current Evidence 复核发现并已经修复：

1. V19 曾使用历史父 alias `party-building`，Fresh DB 会令主题教育成为顶级栏目；已改为当前 `party`；
2. REQUIRED ARTICLE 在文章封面后续被移除时 Public API 曾仍返回无图投放；已在 Backend 过滤；
3. 页面首次在 hidden tab 挂载时曾可能先启动 timer；已在 mount 初始化 visibility pause；
4. Party 隐藏 slide 图片失败可能因只保留数字 index 导致当前项跳转；已改为按 item ID 保持；
5. INTERNAL ARTICLE router-link 曾忽略列表项 openMode；Main / Party 已统一保留；
6. SiteConfig 正整数校验曾只识别旧 `HOME_CAROUSEL_INTERVAL_SECONDS`；已切换到两个统一键；
7. ARTICLE 专项 E2E 曾通过修改全局 `CAROUSEL_MAX_ITEMS` 保证可见并在一个成功 CI Artifact 中出现一次 retry；已改为通过测试 placement 排序进入可见集合并清理自身投放，不再修改全局 max-items；最终 Head 必须重新取得无未解释 retry 的 Current Evidence。

## 5. Verification Obligations

### Build / Static

- Backend tests + classes / package；
- Admin `vue-tsc + vite build`；
- Public `vue-tsc + vite build`；
- Main / Party CSS reduced-motion / responsive ratio review。

### Browser

- Main / Party 共用 interval/max-items；
- 非正 interval/max 配置被 Backend 拒绝；
- reduced-motion 不自动播放；
- manual dot 仍有效；
- ARTICLE Main / Party canonical route 与 openMode；
- ARTICLE 不改变 columnId；
- withdraw 后从公开 list 退出；
- list override Resource 公开权限随有效 ARTICLE 状态；
- REQUIRED ARTICLE 失去继承图片后退出公开列表；
- 隐藏图片失败后由后续项补位且当前项 identity 不跳；
- 既有 Main / Party / Admin Browser Regression 无回归；
- 最终 Playwright Artifact 不保留未解释 flaky retry。

### Canonical Fresh DB

- EU-29 acceptedSnapshot 保持 181；
- EU-30 candidateExtension 为 2；
- Runtime dataset 当前 183；
- first import 183 CREATED；
- second import 183 SKIPPED；
- carousel first import 4 CREATED；
- second import 4 SKIPPED；
- position 2 `source_type=ARTICLE`；
- article legacy identity -> Runtime article_id；
- override Resource bytes SHA-256 一致；
- remaining LINK carousel items static bytes 一致。

### Human Review

最终需要人工确认：

- Main / Party Desktop + Mobile 轮播视觉没有不可接受回归；
- dots / caption / pause / manual behavior 可接受；
- Party position 2 正确进入主题教育站内文章，并保留期望打开方式；
- 主题教育 2 条增量历史内容与原站证据一致；
- 没有需要继续调整的 Authority-backed 高优先级问题。

## 6. Current Evidence Status

### Durable Input / Collection

- EU-30 theme collection Run `33880672887`：PASS；2 条，1 INTERNAL + 1 EXTERNAL_LINK，unresolved=0；已晋升为 Consumer-owned 仓库 durable candidate，不再把临时 Artifact 作为长期迁移输入。
- `data-migrations/party/v1/manifest.json` 保持 EU-29 `acceptedSnapshot=181` 与 EU-30 `candidateExtension=2 / runtimeDatasetArticles=183 / pending-human-review` 分层。

### Ancestor Automated Evidence

曾取得 Head `9bc0715a9e27fe9d8afd36ce5915bc05f6b36a64`：

- CI Run #572 `33885904463`：SUCCESS；Backend / Public / Admin build 与 Integrated Browser 均成功；Playwright Artifact `9942003411`，digest `sha256:f6330cd1aaffe8c9178a57ea2905fa416f7f66e0c8a35eb55e73e8a3e6d840f0`。该 Artifact 后续复核发现 ARTICLE 专项测试第一次失败后 retry 成功，因此不能作为最终“无不稳定项”证据；测试已修正。
- Canonical Migration Verification Run #36 `33885904469`：SUCCESS；Fresh DB、183 条当前 Runtime Dataset、4 条混合轮播、首次导入、二次幂等和 reconciliation 均通过；Artifact `9941917242`，digest `sha256:b65cc1b66e9ef1b5a849a1c26cf9f9380bafd77d4e5154ca0152547051fcadea`。

上述均为祖先 Evidence，不描述为最终 Head Run。最终 Head 已继续发生 carousel lifecycle、openMode、config validation、tests 和 Authority 文档变化，因此需要重新取得相应 CI；Canonical claim 可在最新 Head 取得新 Run时直接使用新证据，否则必须按 Evidence Claim 规则比较并证明后续差异不影响 Migration / Canonical 数据、Importer、Flyway 与 Verification Workflow 后才允许复用。

### Review Environment Stale Verification Contract

Review Environment Run #502 / #503 已证明：构建和前置 AI / Browser Verification 可以通过，但 Workflow 后半段仍冻结 EU-29 旧断言并在“导入 Party canonical 历史数据”失败，具体陈旧点包括：

- 只接受 manifest `accepted-canonical`，未识别 EU-30 `candidate-extension`；
- 导入报告硬编码 181，而当前 Runtime Dataset 为 183；
- 只对账原四栏目，未包含 `party-theme-education`；
- Party Runtime 假定 4 条轮播全部为 LINK/static imagePath，未识别 position 2 ARTICLE + Resource。

该失败分类为 **Stale Verification Contract**，不是产品实现失败。

2026-09-04 继续收敛时，使用正常 GitHub `update_file` 已成功提交 `80fcf7e3840de9090d6230ca99a6801dc0ded3a8`，并重新读取确认 Workflow 内容。此前会话报告的整文件写入限制在本次环境未复现，不能继续将其记为当前阻塞，也没有创建副本或修改 FRP 配置。

该提交修正 manifest accepted/candidate 分层、按当前 Runtime 数据集判断导入报告、按 Party 父栏目对账五个子栏目，并让本地 Gateway 与外部地址复用同一验证脚本：核对来源顺序、标题、打开方式、ARTICLE/Resource 关系和四张原轮播图的 SHA-256。主站增加两条明确的人工评审轮播示例，使多项行为可观察。第二项稳定 legacy identity 与原地升级幂等性另见 `eu30-migration-upgrade-verification.md`。

Review Environment 新增显式 `human-review` PR 标签触发；只有该次 labeled 事件或 workflow_dispatch 取得 45 分钟 Human Review lease，普通 PR 自动验证仍只保留 120 秒。所有路径共用原 concurrency group，不取消有效租约。公开 `/review-environment.json` 和 Artifact 记录 Run、实际 checkout Head、readyAt、expiresAt；外部内容必须与本地 owner 文件完全一致。租约到期或获授权取消后按原 cleanup 释放，重新申请需要移除再添加标签或手工 dispatch；标签持续存在不会让后续普通 synchronize 自动取得长租约。

上述修订必须在新 Head 取得实际 CI / Canonical / Upgrade / Review Evidence 后才可宣布通过。Human Review 仍未执行，EU-30 继续保持 CURRENT / Draft，candidateExtension 保持 pending-human-review。

## 7. Exit Condition

EU-30 仅在以下条件全部满足时关闭：

1. Requirement / Specification / Technical Plan 与实现一致；
2. 最新 Head CI PASS，并完成 Playwright Artifact 复核；
3. 最新 Head Canonical Migration Verification PASS，或对未受后续变更影响的 Canonical claim 完成严格 Evidence reuse 记录；
4. Review Environment Stale Verification Contract 修正，并基于最新目标 Head 成功部署；
5. AI / Browser Evidence 无未处理高优先级问题；
6. Human Review PASS；
7. Human Review 后将 EU-30 candidateExtension 的最终接受状态与 provenance 回写；
8. Roadmap 更新为 EU-30 完成、EU-31 CURRENT / NEXT；
9. PR #58 整理到 Ready for Review，但**不在没有项目负责人明确指令时合并**。
