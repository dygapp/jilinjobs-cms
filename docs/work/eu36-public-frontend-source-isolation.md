# EU-36 — Public Frontend Source Isolation & Managed Resource Projection

## 1. Identity

- Identifier：`EU-36`
- Source Candidate：GitHub Issue #60 / D1
- Requirement：`docs/requirements/public-frontend-replaceability.md`
- Specification：`docs/specifications/public-frontend-replaceability.md`
- Technical Plan：`docs/technical/public-frontend-replaceability.md`
- Planning baseline：`main@692b4daf0bddabb8ec558b23b543930d5f795b4e`
- Planning integration evidence：PR #70 / PR #71 已集成；PR #71 Post-Integration CI #719 / run `33997632735` PASS
- Status：**READY — Readiness Check PASS**

Identifier 只承担稳定追踪。本 Unit 由 `slice-work` 在 D1 Requirement / Specification / Technical Plan 已 Ready、PR #71 的 managed resource projection contract gap 已修订并完成 Post-Integration Verification 后形成；随后以 `main@692b4daf0bddabb8ec558b23b543930d5f795b4e` 执行 Readiness Check，所有 Gate 均 PASS，因此获得 Execute 权限。

## 2. Intent

在不改变 Main / Party 可观察行为、当前 Multi-entry SPA 架构、Admin authoring/persistence 语义和 CMS 产品范围的前提下，将 Public production source 收敛为只依赖 Public runtime contracts：

1. 清除 `frontend/public-site/src/**` 中遗留的 Admin CRUD / Admin Resource maintenance responsibility；
2. 将已关联 Article managed body image 的 Admin→Public Resource URL translation 收回 Backend Public Article projection；
3. 用一个窄 source-boundary guard 阻止 `/api/admin/**` endpoint knowledge 再进入 Public production source；
4. 重新取得受影响 Backend / Public / Admin / Integrated Browser Current Evidence。

## 3. Scope

### Backend Public projection

- 在 `ArticleService.getPublic()` 的 Public response construction 中，对 sanitized `bodyHtml` 做 managed Article body-image projection；
- 只针对当前 Article `bodyImageResourceIds` 中的 Resource ID，将精确 `/api/admin/resources/{id}/content` 投影为 `/api/public/resources/{id}/content`；
- 未关联 Resource、任意外部 URL、其他 `/api/admin/**` 文本不做泛化改写；
- 保持 Admin create/edit persistence、`bodyHtml` persistence contract、Article/Resource identity 和 `PublicArticleDetail` response shape 不变；
- 补充/调整 targeted Backend tests，证明 projection、association boundary 与 persisted/Admin representation 保持。

### Public source ownership

- `frontend/public-site/src/shared/api/articles.ts`：保留 Public DTO / Public Article / Public Resource consumer responsibility，删除 Admin Article CRUD、Admin Resource upload/read helpers、Admin-only drafts/models，以及依赖 Admin Resource route 的 client-side `publicBodyHtml()` translation；
- `frontend/public-site/src/shared/api/columns.ts`：删除 Admin Column CRUD / Admin-only draft/model responsibility，保留 Public reads；
- `frontend/public-site/src/shared/api/pages.ts`：删除 Admin Page / PageGroup CRUD / Admin-only models，保留 Public reads；
- 删除 Public source 内 pure Admin static-resource maintenance client 及 Main 对它的无效 re-export；
- 不为通过 guard 将 Admin helper 复制到 Public 其他位置。

### Boundary verification

- 在现有 Public verification/toolchain 中增加一个最薄静态检查，扫描 `frontend/public-site/src/**`，发现 `/api/admin/` endpoint knowledge 时失败；
- 不新增独立 lint framework 或通用架构层；
- 保持既有 Public Article managed-image Browser behavior：最终浏览器仍通过 `/api/public/resources/{id}/content` 加载已发布正文图片；
- 执行 Public build/type-check、Admin build/type-check、Backend full tests/build 与 Integrated Browser regression。

## 4. Explicit Non-goals

- 不更换 Vue、Vue Router、Vite 或当前 Public 技术栈；
- 不决定 SPA / SSR / SSG / Hybrid；
- 不修改 `index.html` / `party.html`、Nginx/Gateway fallback、Public build artifact shape 或部署拓扑；
- 不引入通用 build/deploy adapter、Repository split 或新服务；
- 不修改 Admin Article authoring 或 persisted `bodyHtml` 表示；
- 不做数据库 migration 或历史内容 migration；
- 不扩大为 Backend Public DTO/domain 全面重构；
- 不处理 Issue #57 导航架构、Issue #59 Browser Compatibility、Issue #60 C1/C2 或 E1～E3 内容工作；
- 不顺带清理 Admin package 内与本 Unit 无关的 Public helper/DTO，只证明 Admin CRUD/Resource ownership 在 Admin 侧继续可用。

## 5. Acceptance Mapping

| Specification obligation | EU-36 Responsibility |
|---|---|
| Public source 不含 `/api/admin/**` endpoint/client knowledge | mixed API cleanup + static boundary guard |
| Public build 在 cleanup 后成功 | Public `npm run build` / CI |
| Public 可观察行为不变 | existing Public Browser + managed body-image regression |
| Admin 保持 CRUD/resource ownership | Admin build + Admin Browser regression |
| managed body image 由 Backend Public projection 暴露 Public Resource URL | `ArticleService.getPublic()` narrow projection |
| 只改写 associated managed image | targeted Backend tests |
| guard 能捕获未来责任泄漏 | automated source-boundary verification |
| 不引入 speculative architecture | final diff / dependency / workflow audit |
| diff 可追溯到 D1 responsibility isolation | final scope audit + Authority sync |

## 6. Current Readiness Check

### Authority / Intent — PASS

- Requirement 已明确 Public source ownership、Public response projection、稳定 URL / Main / Party identity 与 non-goals；
- Specification 已 Ready，并给出 9 条可验证 Acceptance Obligations；
- Technical Plan 已 Ready，并明确单一最小 Slice、projection 算法边界、source guard、验证顺序与 rollback boundary；
- ADR-0002 继续 Accepted，本 Unit 不改变其 Multi-entry Modular SPA 决策，因此不需要新 ADR。

### Slice Integrity / Verticality — PASS

Source cleanup 与 Backend projection 必须在同一 Unit 中完成：

- 若只删除 Public `publicBodyHtml()` / Admin route knowledge，当前 `PublicArticleDetail.bodyHtml` 中 managed image 会失去 Public URL projection；
- 若只做 Backend projection 而保留 Public Admin CRUD/resource clients，则 Public production source ownership 仍不满足 Requirement；
- 两者共用同一 observable acceptance：Public Article 正文图片仍通过 Public Resource endpoint 正常显示，且 Public source 不再理解 Admin endpoint。

因此单一纵向 Unit 比拆成两个互相依赖、单独均不能闭合 Acceptance 的半单元更合适。

### Current Implementation Evidence — PASS

在 `main@692b4daf0bddabb8ec558b23b543930d5f795b4e` 核对：

- `frontend/public-site/src/shared/api/articles.ts` 当前同时包含 Public Article consumer 与 `/api/admin/articles` CRUD、`/api/admin/resources` upload/read/content helpers，并由 `publicBodyHtml()` 在客户端把 Admin Resource URL 翻译成 Public URL；
- `ArticleService.getPublic()` 当前只对 `article.bodyHtml` 执行 `RichTextHtmlPolicy.sanitize(...)`，没有 managed-resource Public projection；
- Article 已通过 `bodyImageResourceIds` 提供精确 association set，Backend 已有 `/api/public/resources/{id}/content` contract；
- Admin 已有独立 `frontend/admin/src/modules/cms/api/articles.ts` 等 CMS API ownership，因此 Public 删除 Admin copies 不需要创造新的 Admin responsibility location。

这些事实与 Technical Plan 的 implementation boundary 一致，没有出现需要回退 Planning 的新架构缺口。

### Verification Feasibility — PASS

现有验证面可以直接覆盖本 Unit：

- `ArticleServiceTest` 的现有 sample Article 已使用 `/api/admin/resources/12/content` 且关联 `bodyImageResourceIds=[12]`，可以直接扩展为 Public projection / persistence-preservation targeted assertions；
- Public `articles.spec.ts` 已创建含 managed Admin Resource URL 的 Article，并断言公开详情最终 `<img src>` 为 `/api/public/resources/{id}/content`；该行为可以在移除客户端 translation 后作为黑盒回归继续复用；
- Public/Admin package build 均执行 Vue-aware type-check + Vite build；
- CI 已有 Backend / Public / Admin / Integrated Browser 四层验证，无需新增工程基础设施；
- source-boundary guard 可作为现有 Node/Playwright verification surface 中的窄静态测试实现。

### Dependency / Baseline — PASS

- PR #70 已集成 D1 Requirement / Specification / Technical Plan；
- Readiness audit 发现 managed resource projection gap 后，没有扩大 Execute scope，而是先通过 PR #71 回到 Planning Authority 修订；
- PR #71 merge commit：`692b4daf0bddabb8ec558b23b543930d5f795b4e`；
- Post-Integration CI #719 / run `33997632735`：`SUCCESS`；
- 当前无 Open PR，Repository 中不存在其他 `EU-36` 记录，因此 Identifier 无并发冲突。

### Scope / Minimality / Rollback — PASS

- 无 DB/schema/data migration；
- 无 dependency upgrade；
- 无 Gateway/Workflow architecture change；
- projection 只发生在 Public response construction，失败时可纯 source rollback；
- Public/Admin cleanup 是现有责任重复的删除，不要求建立新抽象；
- 若 Execute 发现必须进行 broader Backend DTO redesign、delivery adapter change 或 persisted HTML migration，必须停止扩张并返回 Planning，不纳入 EU-36 opportunistic scope。

### Readiness Result — PASS

所有 Promotion Condition 已满足。`EU-36` 现为 **Ready Execution Unit**，可在 Fresh Context 中进入 Execute。

## 7. Execution Notes

1. 从本 Work Authority、Requirement、Specification、Technical Plan 和最新 `main` 重新恢复 Fresh Context；
2. 优先先写/调整 Backend projection targeted tests，再实现最小 projection helper；
3. 删除 Public mixed Admin responsibility 后立即运行 Public type/build 与 source-boundary guard，避免靠 full E2E 才发现 import residue；
4. existing managed-image Browser regression 是稳定 observable contract，不因实现从 client translation 移到 Backend projection而删除或弱化；
5. Admin 只做回归验证；除非 Public cleanup 暴露真实编译/ownership defect，不主动重构 Admin API module；
6. 不把当前 Vite/Nginx/CI 文件布局提升为永久 contract，也不为未来替换提前增加 adapter；
7. 最终 PR 必须逐项记录 Acceptance Mapping 与 Current Evidence，并在合并后取得 `main` Post-Integration CI。

## 8. Completion Gate

- associated managed body-image Admin URL 在 Backend Public response 中精确投影为 Public Resource URL；
- unassociated/arbitrary URLs 不被误改写；
- persisted/Admin Article HTML 不因 Public read 改变；
- `frontend/public-site/src/**` 不再包含 `/api/admin/` endpoint knowledge；
- Public mixed Admin Article/Column/Page/static-resource responsibility 清理完成；
- source-boundary guard PASS 且能防止责任泄漏回归；
- Public build/type-check PASS；
- Admin build/type-check 与 Admin Browser regression PASS；
- Backend targeted + full tests/build PASS；
- Public managed-image + full Public Browser regression PASS；
- Integrated Browser PASS；
- final diff 无 SSR/SSG、dependency upgrade、Gateway/deployment abstraction、migration 或无关 cleanup；
- PR 合并后 `main` Post-Integration CI PASS；
- Issue #60 / D1 在 EU-36 完成后才标记为完成，Issue #60 本身继续作为剩余 C/E candidates 的规划入口。
