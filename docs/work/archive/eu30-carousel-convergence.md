# EU-30 — Carousel Architecture & Behavior Convergence

## Status

**COMPLETED — 2026-09-05 Human Review PASS；EU-30 canonical extension 已接受；PR #58 已进入 Ready for Review，最终合并仍需项目负责人明确指令。**

工作分支：`feature/eu-30-carousel-convergence`

PR：#58 `feat: 收敛 EU-30 轮播架构与行为`

关联 Authority：

- Requirement：`docs/requirements/information-publishing-eu30-amendment.md`
- Technical Plan：`docs/technical/carousel-list-placement.md`
- Post-Incident Review：`docs/work/eu30-post-incident-review.md`
- Upgrade Evidence：`docs/work/eu30-migration-upgrade-verification.md`
- Verification Strategy：`docs/technical/verification-strategy.md`

`docs/work/frontend-follow-up-execution-units.md` 中对 EU-30 / EU-31 的旧预编号路线仅保留历史追溯价值，不再作为当前执行顺序 Authority。EU-30 关闭后不自动生成 EU-31；后续工作先回到 GitHub Issues #59 / #60 的未编号 Planning / Requirement Candidates，并按 Consumer-local Method 完成 Intent → Specification → Slice → Readiness 后再分配新的 Execution Unit 编号。

## 1. Intent

EU-30 收敛主站与中心党建轮播的内容投放模型、配置语义和运行时行为，同时保持不同 Site 的视觉表达独立，并解决 EU-29 历史轮播与正式 Article 内容之间的关系。

本单元不把轮播做成新的专属 CMS 内容类型，也不把 Main / Party 的视觉主题统一成同一套页面样式。

## 2. Final architecture

### 2.1 Article 与 List 的职责

- `Article` 只持有一个 `columnId`，栏目继续表达内容归属 / 分类。
- `CmsList` / `CmsListItem` 表达展示投放 / 运营编排，不改变 Article 的栏目归属。
- `CmsListItem.sourceType`：
  - `LINK`：直接维护标题、URL 和列表图片；
  - `ARTICLE`：引用既有 Article，标题和目标由 Article 当前状态投影。
- `ARTICLE + INTERNAL` 在 Admin 表达为“站内文章”。
- `ARTICLE + EXTERNAL_LINK` 在 Admin 表达为“外链文章”。
- 直接 URL 列表项在 Admin 表达为“链接 / 直接链接”。
- EXTERNAL_LINK Article 通过 ARTICLE 投放时继续跟随 Article 当前标题和 `externalUrl`，不退化为独立 LINK，也不重复维护目标 URL。

### 2.2 Stable identity

普通 Admin / API 生命周期中：

- `Article.articleType` 创建后不可修改；
- `CmsListItem.sourceType` 创建后不可修改；
- ARTICLE 列表项的 `articleId` 创建后不可替换；
- 需要切换身份时建立新的内容 / 列表项，而不是改写已有身份。

历史迁移存在一个严格受限的 compatibility 例外：EU-29 Party carousel position 2 在 legacy key、fingerprint、Runtime identity 等全部符合已接受基线时，migration-only path 可将原 LINK 原位升级为 ARTICLE；普通管理 API 不暴露该能力。

### 2.3 Image policy

列表定义使用：

- `NONE`
- `OPTIONAL`
- `REQUIRED`

ARTICLE 列表项：

- `imageResourceId = null` 时可继承 Article cover；
- 独立 `imageResourceId` 作为列表投放图片 override；
- `REQUIRED` 且最终无有效图片时，该项不构成有效公开投放。

列表图片约束只描述数据要求，不决定具体公开页面的视觉布局。

### 2.4 Shared carousel behavior

Main / Party 共享：

- `CAROUSEL_INTERVAL_SECONDS`：正整数秒，默认 `4`；
- `CAROUSEL_MAX_ITEMS`：正整数，默认 `5`；
- 统一轮播生命周期；
- `0` 项为空态；
- `1` 项静态展示且无切换控制；
- `>=2` 项自动轮播；
- dots；
- hover / focus / document visibility pause；
- 页面初始隐藏时暂停；
- `prefers-reduced-motion`；
- 图片失败后移除并从后续有效项补位；
- 数据变化时尽量按稳定 ID 保持 active item；
- 不引入第三方 carousel；
- 当前不新增 swipe 交互。

共享原则：**统一行为规则和生命周期，不统一视觉表达。**

Main 与 Party 继续分别持有：

- DOM 视觉组合；
- CSS / Theme；
- Caption；
- 比例：Main `8:5`，Party `585:329`。

## 3. Historical Party content convergence

EU-29 已接受历史基线保持冻结 provenance：

- Articles：`181`
- INTERNAL：`120`
- EXTERNAL_LINK：`61`
- Unique article resources：`180`
- Carousel items：`4`
- Carousel resources：`4`
- unresolved：`0`
- accepted artifact digest：`sha256:230ac0df997b3dc913ed38503a8289eae30d8bb0a455fd858e388ddc27066148`

EU-30 反向追踪 Party carousel position 2 后补采 legacy `zhutijiaoyu / 主题教育2023`，新系统栏目名称收敛为：

- alias：`party-theme-education`
- name：`主题教育`
- records：`2`
  - INTERNAL `1`
  - EXTERNAL_LINK `1`

该栏目是正常 Party 内容栏目，但**不成为 PartyHome 第五个固定内容区块**。

2026-09-05 项目负责人完成最终 Human Review 并明确 PASS 后：

- `data-migrations/party/v1/manifest.json` root status 晋升为 `accepted-canonical`；
- `candidateExtension.status` 晋升为 `accepted`；
- 当前 canonical Runtime Dataset = `183` 篇；
- EU-29 `acceptedSnapshot` 181 篇及原 digest 保持原样，作为冻结 provenance，不被 EU-30 重写。

Party carousel position 2 当前 canonical 关系：

- source type：`ARTICLE`
- article legacy key：`zhutijiaoyu:content:154659859759104`
- Runtime Article：`183`
- Runtime Resource override：`188`
- image SHA-256：`a00db48e094e24b778374ae621b6f150e235625c62f17efc860391223fac830b`
- open mode：`NEW_WINDOW`

position 1 / 3 / 4 保持 LINK；因此 canonical carousel 为：

```text
[LINK, ARTICLE, LINK, LINK]
```

## 4. Human Review findings closed in EU-30

本轮最终 Human Review 前已完成：

1. Party 栏目列表页与详情页 breadcrumb 统一；
2. 公共栏目分页 page-size 从浏览器原生 select 收敛为可主题化控件，Party 使用红色交互态；
3. Article 撤回按钮使用 `CloseBold`；发布 / 重新发布继续使用 `Refresh`；
4. Article 类型创建后不可修改，并有 Backend 防绕过；
5. 删除 Article 全局 `recommended`；公开排序收敛为 `pinned DESC → sort_order DESC → publish date DESC → id DESC`；
6. 列表项数据来源创建后不可修改；ARTICLE 关联文章创建后不可替换；
7. Admin 将 LINK / ARTICLE 明确表达为“数据来源”，并进一步区分“链接 / 站内文章 / 外链文章”；
8. 历史 position 2 LINK → ARTICLE 保留迁移专用受限升级能力；
9. Fresh DB 与 EU-29 → EU-30 同库升级均验证 V20 / migration lineage 正常。

本单元确认但不扩展处理的候选事项已进入后续 Planning / Requirement Candidates：

- 通用 CMS Resource 浏览 / 复用入口；
- `CmsListItem.subtitle` 是否保留、改名或删除；
- 更广的列表模型 / Admin UX 治理；
- 数据库 Flyway 开发期 baseline convergence；
- Browser Compatibility & Runtime Guard；
- 其余公开站正式内容 / 集成工作。

这些事项不因 EU-30 关闭而自动获得 EU 编号。

## 5. Final evidence

### 5.1 Final functional Head before acceptance metadata promotion

Head：`f47623876e6961655338fb6399482e8b363a3801`

CI #658 / Run `33944523896`：SUCCESS

- Public：`48 passed / 3 expected skipped`
- Admin：`35 passed`
- retry：`0`
- 专项 `list-source-semantics.spec.ts` PASS
- Playwright artifact：`9962969270`
- digest：`sha256:7da0924541cfd59467b3ef9f5c79044419df1a76f9cae336d899c02dd160e252`

Canonical #122 / Run `33944523899`：SUCCESS

- Fresh DB 183 篇；
- 五栏目 `40 / 88 / 22 / 31 / 2`；
- `[LINK, ARTICLE, LINK, LINK]`；
- 资源字节 / SHA、二次幂等、Runtime reconciliation PASS；
- artifact：`9962928050`；
- digest：`sha256:8d4c7ecad9e94e06598569c2b34bf1a335eb77f3e3ad161806052036a525ad74`。

Upgrade #70 / Run `33944523904`：SUCCESS

- pinned EU-29 accepted baseline → EU-30 same-runtime upgrade PASS；
- position 2 Runtime ID 保持；
- 二次幂等 PASS；
- unexpected fingerprint drift 被拒绝；
- artifact：`9962931008`；
- digest：`sha256:7753492b082f12f133dbbd9185b7c178eb65af7877145b51853fa08e10e7c5b3`。

Review verification #594 / Run `33944523901`：SUCCESS

- AI / Browser；
- clean Human baseline；
- Party canonical import / Runtime / Browser；
- FRP 与公网 Runtime；
- external artifact：`9963054792`；
- digest：`sha256:f3cd269a22c9803c95c5eb93aa36d405646491de369bf97fd6bdfa0ed3862a71`。

### 5.2 Human Review acceptance

Human Review #595 / Run `33945103502`

- Head：`f47623876e6961655338fb6399482e8b363a3801`
- mode：`human-review`
- readyAt：`2026-09-05T04:45:10.333910Z`
- expiresAt：`2026-09-05T05:30:10.333910Z`
- external evidence artifact：`9963152822`
- digest：`sha256:ec2d434c759236a07d6176792c7dfe58a714d9bd0d92cf7f0f502c99e4dedabb`
- 项目负责人结论：**PASS**。

该 Human Review 已覆盖最终 Runtime / content bytes。后续 `cfe736b2…` 只晋升 manifest acceptance metadata，没有改变 Runtime 实现、文章正文、资源字节或轮播内容，因此 Human Review Claim 可按 Verification Strategy 的 Evidence Impact 规则复用；acceptance metadata 自身由晋升后的 Canonical / Upgrade / CI 重新验证。

### 5.3 Post-acceptance promotion verification

Acceptance promotion commit：`cfe736b238bb9300fcfcd0ef2fa6c84d3170b30f`

CI #659 / Run `33945906756`：SUCCESS

- Backend / Public / Admin build：PASS
- Public Browser：`48 passed / 3 expected skipped`
- Admin Browser：`35 passed`
- retry：`0`
- Playwright artifact：`9963370876`
- digest：`sha256:3f86e472ee3c9c1adb5edcedcdcd5bacc434677677f6b4bdc13256b10a5029ab`

Canonical #123 / Run `33945906761`：SUCCESS

- manifest status：`accepted-canonical`
- acceptedSnapshot：`181`（保持冻结）
- current Runtime Dataset：`183`
- 首次导入：183 CREATED / 0 conflict / 0 invalid
- carousel 首次导入：4 CREATED
- 二次导入：183 SKIPPED；carousel 4 SKIPPED
- 五栏目：`40 / 88 / 22 / 31 / 2`
- position 2：ARTICLE / Article 183 / Resource 188 / 原 image SHA
- artifact：`9963343009`
- digest：`sha256:59c93423552f2b0a55007976da9af60366aa7b4e64e277f91cf4da996e9b44e7`

Upgrade #71 / Run `33945906755`：SUCCESS

- EU-29 frozen accepted runtime → accepted EU-30 dataset 同库升级 PASS；
- post-upgrade idempotency PASS；
- unexpected position 2 fingerprint drift 拒绝 PASS；
- artifact：`9963347099`
- digest：`sha256:f48480f475f0e183fca7643b36ab9cd84de0b92bc4151ed0db623153667be179`

Review #596 因 singleton Review Environment / 已存在 Human Review lease 的并发规则排队，不作为 EU-30 关闭的必要新 Human Evidence。EU-30 使用 #595 的人工结论证明最终 Runtime / Visual Claim，并使用 #123 / #71 / #659 证明 acceptance metadata 晋升后的 current state。

## 6. Closeout decision

EU-30 最终接受：

- 轮播内容投放模型：ACCEPTED；
- Main / Party shared behavior + separate visual expression：ACCEPTED；
- Article / List ownership-placement boundary：ACCEPTED；
- Party `主题教育` 2 条历史增量：ACCEPTED CANONICAL；
- Party position 2 ARTICLE upgrade：ACCEPTED；
- EU-29 acceptedSnapshot provenance：PRESERVED；
- Human Review：PASS；
- Post-acceptance Fresh DB / Upgrade / Browser verification：PASS。

## 7. Exit checklist

- [x] Requirement / Technical Plan / Execution Authority 已收敛；
- [x] Post-Incident Review 无未处理高优先级 Finding；
- [x] Backend / Public / Admin / Integrated Browser PASS，无未解释 retry；
- [x] Canonical Fresh DB / idempotency / Runtime reconciliation PASS；
- [x] EU-29 → EU-30 same-runtime Upgrade + unexpected fingerprint conflict PASS；
- [x] Review Environment Runtime / Browser / external verification PASS；
- [x] 最终 Human Review PASS；
- [x] candidateExtension 已接受为 canonical current dataset，同时保留 EU-29 acceptedSnapshot provenance；
- [x] acceptance metadata 晋升后的 CI / Canonical / Upgrade 重新验证 PASS；
- [x] Project Roadmap 收口为 EU-30 completed；后续工作回到未编号 Planning / Requirement Candidates；
- [x] PR #58 转 Ready for Review；
- [ ] PR #58 合并：**仅在项目负责人明确发出合并指令后执行**。
