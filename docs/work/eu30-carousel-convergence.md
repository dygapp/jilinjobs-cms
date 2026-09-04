# EU-30 — Carousel Architecture & Behavior Convergence

## Status

**CURRENT — implementation and post-incident machine verification converged; final Review Environment / Human Review pending**

工作分支：`feature/eu-30-carousel-convergence`

Draft PR：#58 `feat: 收敛 EU-30 轮播架构与行为`

事故后全面复核：`docs/work/eu30-post-incident-review.md`

同库升级专项证据：`docs/work/eu30-migration-upgrade-verification.md`

本文是 `docs/work/frontend-follow-up-execution-units.md` 中 EU-30 的当前执行状态。EU-31 仍是后续路线；EU-30 未经最终 Human Review 不关闭、不接受 candidateExtension、不切换 Roadmap 当前单元。

## 1. Goal

把 Main Site 与中心党建 Party 的轮播从两个局部实现收敛为：

- 通用 `CmsListItem` 表达 LINK / ARTICLE 展示投放；
- Article 保持唯一 `columnId`，列表只承担展示投放；
- Main / Party 共用稳定的无主题轮播生命周期；
- Main / Party 继续持有各自视觉 DOM / CSS / 比例；
- 使用统一站点级轮播行为属性；
- 修复 Party 历史轮播第二项与遗漏主题教育内容的真实关系；
- 建立 Fresh DB、同库升级、Browser、Review Environment、Human Review 的可重复证据链。

不以“抽象更多”为目标，不引入第三方 Carousel，不提前执行 EU-31 完整浏览器兼容工作。

## 2. Confirmed Architecture

### 2.1 CmsListItem source

稳定来源只使用：

- `LINK`
- `ARTICLE`

不使用 `MANUAL`。

LINK 保存列表自身标题、URL、图片等投放数据。

ARTICLE 通过 `articleId` 引用已有 CMS Article：

- 不改变 Article 原栏目、面包屑和发布生命周期；
- 只有关联 Article 为 `PUBLISHED` 时进入公开列表；
- 标题等文章派生数据使用 Article 当前值；
- INTERNAL 的公开 `item.url=null`，Main / Party 根据 `articleId` 分别生成 `/article/{id}` / `/party/article/{id}`；
- EXTERNAL_LINK 的 Article 当前 `externalUrl` 由 Backend 在公开查询时解析到通用 `CmsListItem.url`；Public DTO 不另建 `externalUrl`；
- `openMode` 是列表投放属性，ARTICLE 同样保留；
- 文章撤回后投放关系保留在后台，但公开自动退出；重新发布后恢复公开资格。

### 2.2 Image policy

`CmsList.imagePolicy`：

- `NONE`
- `OPTIONAL`
- `REQUIRED`

ARTICLE：

- `image_resource_id=null` 表示继承 Article 当前 cover；
- 非 null 表示列表专用 Resource override；
- 正文图片可以在 Admin 中作为编辑候选，选择后固化为 Resource override；
- Runtime 不动态寻找正文第一张图片；
- 当前模型不存在“Article 有 cover 但列表显式屏蔽 cover”的第三态；
- REQUIRED ARTICLE 后续失去继承 cover 且没有 override 时直接退出公开列表。

Resource 公开访问仍要求有效公开关系，不因知道 Resource ID 就开放任意上传文件。

`HOME_CAROUSEL` 与 `PARTY_CAROUSEL` 均为 `REQUIRED`。

### 2.3 SiteProperty

统一稳定 Key：

- `CAROUSEL_INTERVAL_SECONDS=4`
- `CAROUSEL_MAX_ITEMS=5`

契约：

- Backend 新写入只接受完整十进制正整数；
- 写入前 trim；
- `+5 / 5abc / 1.5 / 0 / -1` 拒绝；
- 稳定 Key 的语义固定为 INTEGER，不能通过 Definition 编辑改成 TEXT 绕过；
- 两个定义均为 `preset=true`，不能删除；
- Public 对缺失或历史非法值分别 fallback 4 / 5；
- 旧 `HOME_CAROUSEL_INTERVAL_SECONDS` 不再是 Runtime 依赖。

`CAROUSEL_MAX_ITEMS` 只限制公开展示，不限制 Admin 维护数量。失败图片先从有效集合剔除，再应用 max，因此后续排序项可以补位。

### 2.4 Shared lifecycle

Main / Party 共用 `shared/carousel/useContentCarousel.ts`：

- 0 项：稳定空态，无 timer；
- 1 项：静态，无 timer、无无意义分页控件；
- 2 项及以上：按统一 interval 循环自动切换；
- 原生可聚焦 dots 手动切换；
- hover pause；
- focus-within pause；
- 页面初始 hidden 和后续 visibility hidden 均暂停；
- resume 保留当前项，不重置；
- `prefers-reduced-motion: reduce` 禁用 autoplay 和非必要切换动画，但保留手动分页；
- 图片失败退出有效集合并由后续项补位；
- 有效集合变化按 item ID 尽量保持当前内容；
- unmount 清理 timer / visibility / MediaQuery listener；
- EU-30 不实现 swipe；
- 不引入第三方 Carousel。

### 2.5 Visual ownership

Main：

- 稳定 `8:5`；
- `object-fit:cover`；
- Main-owned DOM / caption / dots / fade。

Party：

- Desktop 约 `585×329`；
- Mobile `aspect-ratio:585/329`；
- `object-fit:cover`；
- Party-owned DOM / caption / dots / red theme；
- 非活动 slide `visibility:hidden`；
- reduced-motion transition none。

共享行为，不共享视觉组件。

## 3. Party Historical Correction

EU-29 accepted baseline 保持冻结 provenance：

- 181 articles；
- 120 INTERNAL；
- 61 EXTERNAL_LINK；
- 180 unique article resources；
- 4 carousel items/resources；
- unresolved=0；
- artifact digest `sha256:230ac0df997b3dc913ed38503a8289eae30d8bb0a455fd858e388ddc27066148`。

EU-30 新增 candidateExtension：

- legacy `typeCode=zhutijiaoyu`；
- 新栏目 `party-theme-education / 主题教育`；
- 2 articles：1 INTERNAL + 1 EXTERNAL_LINK；
- unresolved=0；
- runtime dataset = 183；
- 状态继续 `pending-human-review`。

`party-theme-education`：

- 属于 Party 五栏目可访问作用域；
- 不成为 PartyHome 第五个固定内容区；
- PartyHome 只加载 `party-voice / party-work / party-rules / party-study`。

Party carousel position 2：

- stable legacy key `party-carousel:position:2`；
- 改为 ARTICLE；
- `articleRef.legacyKey=zhutijiaoyu:content:154659859759104`；
- 原轮播 PNG 继续作为列表 Resource override；
- `NEW_WINDOW` 继续保留；
- 新版目标为 `/party/article/{runtimeArticleId}`；
- 不使用标题、Runtime ID 或旧 URL 作为 durable identity。

EU-29 → EU-30 的原地兼容只允许精确 fingerprint 转换：

`c2ad182b8b2dc981a3cbe3b0153a1e3e47604c1f01dd43e6d25971e1deed10dc`

→

`f8b5d8df87021373803639b174bf88e46ae6cef7f2599a205763b5887c78be84`

其他 drift 必须 CONFLICT。

主题教育 INTERNAL 正文多次引用同一源图时，Canonical 保留 occurrence evidence；Importer 校验全部 occurrence 后按 stable migration token 去重 Runtime upload，当前目标文章只形成 1 条 BODY_IMAGE association。

## 4. Implemented Scope

当前实现包括：

- V19 List ARTICLE / Resource 字段、统一轮播配置、主题教育栏目；
- `CmsListItem.sourceType / articleId / imageResourceId`；
- Public ARTICLE 发布状态和 REQUIRED effective image 过滤；
- Public Resource relation-based access；
- Admin LINK / ARTICLE、文章搜索、cover inherit、正文候选、Resource override；
- Main / Party Shared Carousel lifecycle；
- Main / Party ARTICLE target/openMode；
- EXTERNAL_LINK ARTICLE 当前地址动态解析；
- Party 五栏目 Router scope / 四栏目 Home scope；
- EU-30 theme durable candidate；
- Historical Import V2 / Carousel Import V2；
- Canonical Fresh DB verification；
- pinned EU-29 → EU-30 same-runtime Upgrade Verification；
- Review Environment acceptedSnapshot + candidateExtension / mixed carousel Runtime contract；
- Main / Party / Admin Browser regression 与 EU-30 专项 E2E。

## 5. Post-Incident Review

详细 Finding 与分类见：

`docs/work/eu30-post-incident-review.md`

本轮事故后复核已经处理的主要缺口包括：

1. 历史整数值被 `parseInt` / MySQL CAST 宽松截断；
2. position 2 fingerprint 兼容口过宽；
3. Upgrade Verification 错误依赖移动 `main`；
4. 临时 Method Amendment 折回后仍标 CURRENT；
5. durable candidate 晋升后一次性采集 Workflow 未清理；
6. PartyHome 不必要加载第五个非首页栏目；
7. OPTIONAL ARTICLE 文档声称了模型无法表达的显式无图第三态；
8. Review 栏目 reconciliation 错误采用 closed-world；
9. 主题教育重复 source image 被重复上传为 Runtime Resource；
10. Backend 新写入整数表达与 Public strict read 不一致；
11. 可编辑 `valueType` 可绕过稳定轮播 Key 整数语义；
12. V19 `CAROUSEL_MAX_ITEMS` 遗漏 preset protection。

复核过程中还出现一次 EXTERNAL_LINK ARTICLE DTO 错误诊断；TypeScript build 立即失败，错误改动已撤回。真实契约已经写入 Public / Party Technical Plan，并由 Browser 回归验证。

## 6. Current Machine Evidence

### 6.1 CI #620

Evidence commit：`821a8a34f0f6063c747e6872d6091ed29014c299`

Run：`33930893118` — PASS

- Backend verify PASS；
- Public build PASS；
- Admin build PASS；
- Integrated Browser PASS；
- Public Playwright：47 passed / 3 expected skipped / **0 retry**；
- Admin Playwright：32 passed / **0 retry**；
- Playwright Artifact `9958570839`；
- SHA-256 `a267c7204c2e03f54b074093619f73db380f01124cd8ad0244fab1091a337bf9`。

### 6.2 Canonical Migration Verification #84

Run：`33930893082` — PASS

- Fresh DB current dataset 183 CREATED；
- carousel 4 CREATED；
- second import articles 183 SKIPPED；
- second carousel 4 SKIPPED；
- Party scope 40 / 88 / 22 / 31 / 2；
- position 2 ARTICLE / legacy mapping / override SHA 正确；
- target theme article BODY_IMAGE Runtime association = 1；
- Artifact `9958531465`；
- SHA-256 `f70675aac4c126c72b95a68ab619e7946ab690ce8cb09422a7d23e6eb183a845`。

### 6.3 EU-29 → EU-30 Upgrade Verification #32

Run：`33930893093` — PASS

- pinned accepted EU-29 commit `59c855f...`；
- EU-29 first import 181 + 4；
- same DB EU-30 upgrade 2 CREATED + 181 SKIPPED；
- position 2 保持原 list item ID，LINK → ARTICLE；
- second EU-30 import fully idempotent；
- unexpected fingerprint drift 导致 importer 非零退出、`conflicts=1 / position2=CONFLICT`；
- Artifact `9958531784`；
- SHA-256 `6f92d2dafddbada822a4bd5ea7e4a89cc8cc4611d73f2064f8e3ad12a81ce4c9`。

### 6.4 Descendant docs-only Evidence Reuse

`821a8a...` 后首先只有两项 Technical Plan docs-only 修订，用于澄清 EXTERNAL_LINK ARTICLE 的通用 `item.url` DTO 契约；随后又只更新本执行文档、事故后复核与升级证据记录。

只要锁定最终 Head 前没有继续修改 Runtime Code、Requirement、Flyway、Migration Dataset、Importer、Workflow、Fixture、Test、静态资源或环境配置，上述 CI / Canonical / Upgrade 行为 Claim 可以按 `docs/technical/verification-strategy.md` 的 Descendant Commit Evidence Reuse 规则继续使用。Run 仍描述为 `821a8a...` Evidence Commit 的 Run，不伪装成后继 docs Head 自己执行。

## 7. Review Environment / Human Review Boundary

早期 Review #526 与更早 Run 只作为历史追溯；本事故后复核已继续改变实现和验证，因此不承担最终 Human Review Claim。

最终还必须完成：

1. 锁定最终 Branch Head，不再追加实现/Authority 改动；
2. 该 Head 的普通 Review Environment 自动验证完成：build → Browser → clean reset → canonical import → Party Runtime Browser → FRP → external runtime → owner evidence；
3. 自动 Review PASS 后，对完全相同 Head 申请 `human-review` 长租约；
4. 项目负责人人工确认：
   - Main Desktop / Mobile 轮播视觉；
   - Main autoplay / manual / pause；
   - Party Desktop / Mobile 585:329、caption、dots；
   - Party position 2 正确进入主题教育站内文章并符合打开方式；
   - 主题教育 2 条历史内容与原站证据一致；
   - 没有 Authority-backed 高优先级视觉/内容/交互回归。

Human Review PASS 前：

- `candidateExtension.status=pending-human-review`；
- PR #58 保持 Draft；
- EU-30 保持 CURRENT；
- Roadmap 不切换 EU-31；
- 不执行最终 merge。

## 8. Exit Condition

EU-30 仅在以下条件全部满足后关闭：

- [x] Requirement / Specification / Technical Plan / implementation 核心语义一致；
- [x] 事故后复核无未处理的已确认实现 Finding；
- [x] Backend / Public / Admin / Integrated Browser PASS 且无未解释 retry；
- [x] Canonical Fresh DB / idempotency / Runtime reconciliation PASS；
- [x] pinned EU-29 → EU-30 upgrade PASS；
- [x] unexpected fingerprint drift CONFLICT negative test PASS；
- [ ] 最终 Head Review Environment automatic verification PASS；
- [ ] 最终 Head Human Review PASS；
- [ ] Human Review 后接受 candidateExtension 并保留 EU-29 acceptedSnapshot provenance；
- [ ] Roadmap 收口 EU-30 completed、EU-31 CURRENT；
- [ ] PR 转 Ready for Review；
- [ ] 最终 merge 取得项目负责人明确指令。
