# 中心党建正式页面与内容收敛执行单元

本文记录中心党建 Foundation 之后的正式页面、内容与视觉收敛切分。每个 Execution Unit 必须能够独立审查、验证和回滚；不得因为后续单元已准备就跳过前序 Current Evidence。

**状态：进行中（2026-09-02）。**

## 证据基线

原站：`https://24365.jl.smartedu.cn/dyzj`

2026-09-02 取证确认：

| 原站业务线 | legacy typeCode | 新预置 alias |
|---|---|---|
| 高层声音 | `gcsy` | `party-voice` |
| 工作动态 | `gzdt` | `party-work` |
| 党规党章 | `dgdz` | `party-rules` |
| 理论学习 | `llxx` | `party-study` |

原站列表使用 `/plist.html?typeCode=...`；当前生成的站内详情使用 `/pdetail.html?content_id=...`，部分历史地址可携带 `typeCode`，更早历史内容还存在 `/detail.html?content_id=...` 变体；外部文章可直接跳转来源网站。“学习园地”只组合党规党章与理论学习，不作为独立 CMS 类型。

原站四个栏目均存在多页历史内容，因此正式实现和迁移不得使用全站固定窗口后前端筛选代替栏目作用域分页。

---

## EU-26 — Party Building Evidence & Authority Convergence

### Goal

把 Foundation 之后重新取得的原站证据和正式实现边界固化为 Requirement / Specification / Technical Plan / Execution Units。

### Scope

- 原站 Desktop / Mobile 页面截图；
- 原站 DOM / computed style / CSS / 可取得静态资源清单；
- `docs/requirements/information-publishing.md` -> V4.8；
- `docs/specifications/party-building.md`；
- `docs/technical/party-building-frontend.md`；
- 本执行单元文档；
- Roadmap 已处于“中心党建正式页面与内容收敛”，无需复制 PR/commit 瞬时状态。

### Decisions

- 复用通用 Column + Article，不新增党建专属 CMS 类型；
- 使用一个 `party-building` 预置父栏目组织四个真实子栏目；
- `学习园地` 为页面分组；
- Party canonical URL 为 `/party/`、`/party/column/{alias}`、`/party/article/{id}`；
- legacy `plist/pdetail/detail` 及其参数变体只属于迁移输入；
- Flyway 只固化结构，历史运营文章独立迁移；
- Foundation CSS / 文案不是最终视觉 Authority。

### Acceptance

- 文档不再把正式党建内容列为当前暂不实现；
- 四条原站内容线与 legacy 映射明确；
- 原站 Desktop / Mobile screenshot、DOM、computed style、CSS 与资源清单形成可复用 Current Evidence；
- CMS 复用边界、URL、历史迁移和视觉证据层次无冲突；
- Repository CI 在当前 Head 上成功。

---

## EU-27 — Party CMS Structure & Content Routing

### Goal

建立可重复初始化的党建栏目结构，并完成 Party 首页/栏目/详情所需的真实内容路由闭环，不在此单元追求最终视觉精度。

### Scope

- 新 Flyway migration 建立预置 `party-building` 父栏目及四个子栏目；
- Party App 提升 Header / Footer ownership；
- Party Router 增加 column / article 路由；
- 新建 Party Column View / Article View；
- 首页从四个真实 Column 加载 Article 摘要；
- INTERNAL / EXTERNAL_LINK 行为；
- 非党建文章 Party 详情隔离；
- E2E fixture 覆盖分页、外链、详情、撤回/不可用等核心行为。

### Non-goals

- 不批量迁移历史文章；
- 不新增党建 Admin Module；
- 不引入 `site` 字段或新的多站点权限；
- 不把 Foundation 视觉直接声明为最终效果。

### Acceptance

- Fresh Flyway 得到党建预置栏目树；
- `/party/` 不再使用虚构栏目/静态文章数组；
- 四条内容线使用通用 Article；
- Party list/detail canonical routes 可直接访问和刷新；
- Main / Admin 无回归；
- Backend + Public + Admin + Integrated Browser 当前 Head 成功。

---

## EU-28 — Party Home & Visual Fidelity Convergence

### Goal

依据原站证据完成 Party Header、首页重点内容、高层声音、工作动态、学习园地、Footer，以及列表/详情页面的正式红色视觉收敛。

### Scope

- 继续获取原站可用 DOM、CSS、静态资源和代表性截图证据；
- 将可靠稳定资源整理到 `site-baseline/static/party-building/**`；
- Party Header / Footer / Page Frame；
- 首页重点内容区域；
- 高层声音、工作动态、学习园地布局；
- 栏目列表与文章详情视觉；
- PC / Mobile 响应式；
- 必要 Party SEO metadata。

### Evidence Rule

文本/DOM 证据可以证明信息架构，但不能单独证明像素级视觉。无法可靠取得的原站图片不得用无关搜索图片替代。

### Acceptance

- Foundation 占位文案与伪品牌元素退出正式页面；
- Party 首页/列表/详情达到当前原站可证明的视觉主结构；
- Main 蓝白主题无污染；
- AI Visual Review 无未处理的 Authority-backed 高优先级差异；
- Human Review Finding 完成分类与修正；
- Current Evidence 成功。

---

## EU-29 — Party Historical Content Migration & Final Review

### Goal

建立与 Flyway 分离的党建历史内容迁移机制，导入足以支持正式运营/复核的原站文章与外链，并关闭中心党建正式收敛阶段。

### Scope

- 明确迁移输入格式和幂等键；
- 保存 legacy `content_id / typeCode / detail path`；
- 映射到四个新栏目；
- 站内正文转换为 INTERNAL；
- 外部原文转换为 EXTERNAL_LINK；
- 可可靠取得的正文图片/附件进入通用 Resource；
- 记录无法迁移/需人工处理的数据；
- 如产品确需旧 URL 可访问，再基于迁移映射实现兼容重定向；
- 完成最终 AI + Human Review。

### Non-goals

- 不把历史运营内容改写为 Flyway SQL；
- 不为了迁移旧 URL 保留旧页面模板；
- 不因历史页面出现专题样式就立即增加通用 Page Builder。

### Acceptance

- 迁移可重复执行且不会重复创建同一 legacy 内容；
- 代表性四类栏目均有真实内容证据；
- INTERNAL / EXTERNAL_LINK 映射正确；
- 历史无法自动迁移项有明确报告而非静默丢失；
- Party 首页、栏目、详情与 Main/Admin 回归通过；
- Human Review 通过后 Roadmap 切换到“公开站点剩余内容与集成收敛”。
