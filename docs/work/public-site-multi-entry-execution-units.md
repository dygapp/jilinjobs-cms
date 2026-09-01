# 公开站 Multi-entry Modular SPA 执行单元

本文记录当前公开站架构重构的持久执行切分。每个 Execution Unit 都必须能够独立审查、独立验证并在必要时回滚；PR 是否合并继续遵循当前 Repository Authority 与 Roadmap，不因为后续单元已准备就跳过前序验证。

## EU-23 — Public Frontend Authority & Architecture Convergence

### Goal

将已确认的中心党建范围变化和公开前端目标架构固化为 Consumer Authority / Specification / ADR / Technical Plan / Roadmap，消除“中心党建只占位”和“`page.html` 作为长期页面 Entry”与新方向之间的冲突。

### Scope

- `AGENTS.md` 当前迭代范围；
- `README.md` 当前目标、Authority 入口与前端目标结构；
- `docs/requirements/information-publishing.md` 升级到 V4.7；
- `docs/specifications/public-site.md`；
- `ADR-0002-public-site-multi-entry-modular-spa.md`；
- `docs/technical/public-site-frontend.md`；
- `docs/project/project-roadmap.md`；
- 本执行单元文档。

### Acceptance

- 中心党建基础公开 Site Boundary 明确进入当前范围；
- 党建完整内容、最终视觉和专属后台仍留给后续独立任务；
- 目标架构明确为同一 `frontend/public-site` 工程中的 Main Site + Party Building Site 两个真实 Entry；
- `/page/**` 不再作为独立 Entry Boundary；
- 独立党建前端工程、Module Federation、SSR/SSG 均不是当前决策；
- Roadmap 当前阶段切换到公开站 Multi-entry Modular SPA / 党建基础框架。

### Verification

- 文档交叉引用、版本号和阶段状态一致；
- Repository CI 在当前 Head 上成功；
- 不以历史 CI 代替当前 Head Evidence。

---

## EU-24 — Main Site Modularization & Page Entry Removal

### Goal

在不改变中心主站公开行为的前提下，把当前平铺公开站源码重构为 Main Site Boundary，并删除无业务价值的 `page.html / page-main.ts` 重复 Entry。

### Scope

目标结构至少包括：

```text
src/
├── shared/
│   ├── api/
│   └── seo.ts
└── sites/main/
    ├── app/
    ├── shell/
    ├── modules/
    │   ├── home/
    │   ├── content/
    │   └── page/
    └── styles/
```

实施责任：

- Main bootstrap / App / Router 移入 `sites/main/app`；
- Header / Footer 移入 `sites/main/shell`；
- 首页移入 `modules/home`；
- 栏目 + 文章移入 `modules/content`；
- 单页移入 `modules/page`；
- API transport / CMS DTO 与 SEO helper 进入无主题 `shared`；
- Main Router 对页面使用动态 import；
- 删除 `page.html / page-main.ts`；
- Vite 暂时只保留 Main Entry；
- Nginx 删除 `/page/** -> page.html` 专用 fallback，让 `/page/**` 使用 Main Entry。

### Non-goals

- 不改 CMS 业务模型；
- 不重做主站视觉；
- 不修改现有 canonical URL；
- 不在此单元加入党建真实内容；
- 不引入通用 Module Registry，除非实现中出现当前 Router 无法解决的真实需求。

### Acceptance

- `/`、`/column/**`、`/article/**`、`/page/**` 与兼容路由行为无回归；
- `/page/**` 直接访问/刷新正常；
- Main Site Header / Footer / 蓝白主题无明显架构重构回归；
- build output 不再包含 `page.html`；
- Browser Verification 现有公开站核心用例通过。

### Verification

- Public Site frontend build；
- 现有 Public Browser E2E；
- Integrated Browser Verification；
- 如仅为前端源码/网关调整且 Backend 无语义变化，Backend CI 仍由仓库完整 CI 提供守护。

---

## EU-25 — Party Building Site Entry & Foundation Shell

### Goal

在已模块化的公开站工程内建立中心党建独立 Site Entry、Router、红色主题基础 Shell，并将现有主导航占位接到党建入口，为下一阶段真实页面与内容收敛提供稳定基础。

### Scope

- 新增 `party.html`；
- 新增 `sites/party-building/app`；
- 新增 `sites/party-building/shell`；
- 新增 `sites/party-building/modules/home` 基础页面；
- 新增独立红色主题基础样式；
- Vite multi-input：`main -> index.html`、`party -> party.html`；
- Nginx `/party/** -> party.html` fallback；
- 新 Flyway migration 将预置主导航“中心党建”从 `PLACEHOLDER` 更新为 `LINK /party/`，当前窗口打开；
- 新增/更新 Browser E2E 覆盖党建 Entry 与主站入口跳转。

### Non-goals

- 不定义党建完整栏目树和导航树；
- 不录入正式党建文章/专题内容；
- 不声称完成原站党建视觉复刻；
- 不新增党建专属 Admin Module；
- 不把党建拆成独立 package / Vite 工程 / deployment；
- 不引入 Module Federation。

### Acceptance

- `/party/` 可直接访问与刷新；
- Party App / Router 与 Main App / Router 独立；
- Party Shell 有明确红色主题和独立 Header / Footer / Page Frame；
- Party CSS 不污染 Main Site，Main CSS 不成为 Party Shell 必要依赖；
- 主站“中心党建”导航可进入 `/party/`；
- Flyway fresh migration chain 正常；
- `/`、`/page/**`、`/admin/**`、`/api/**`、`/static/**` 无路由回归。

### Verification

- Backend Verify + fresh database migration/startup evidence；
- Public Site frontend build 同时生成两个真实 Site Entry；
- Admin frontend verify；
- Integrated Browser Verification：Main + Party + Admin + API；
- 必要 screenshot 只用于证明基础框架和主题隔离，不作为最终党建 Visual Fidelity Evidence。

---

## 后续阶段边界

EU-25 完成后，公开前端架构基础重构结束。下一项独立任务应重新读取当前 Authority 并针对中心党建开展：

1. 原站截图 / DOM / 资源 / URL 取证；
2. 党建页面、栏目、导航和内容模型确认；
3. 是否继续复用通用 CMS 对象；
4. 正式红色主题视觉收敛；
5. 必要初始化数据和静态资源；
6. Automated + AI Visual + Human Review 闭环。

只有届时出现真实专属管理能力，才评估新增 Admin Module；只有出现独立生命周期，才评估独立前端工程。