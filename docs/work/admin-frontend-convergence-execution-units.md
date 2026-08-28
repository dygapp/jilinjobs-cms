# 管理端工程分离与功能收敛执行单元（Execution Units）

## 使用边界

本文件承担当前“管理端工程分离与功能收敛”阶段的执行切片。上一阶段 EU-01～EU-12 已完成，不在本文件重复定义。

执行依据：

- `docs/requirements/information-publishing.md` V4.1
- `docs/specifications/admin-frontend-convergence.md`
- `docs/technical/admin-frontend-convergence.md`
- `docs/technical/verification-strategy.md`

---

## EU-13 Existing Consumer baseline upgrade

**goal**

把 Consumer 使用的 `agentic-dev` baseline 从 `df4d6a607597eeb3684279e269cb073fcb398f83` 升级到 `bf21c7bcd711fd667c43007a72fae65750d1af09`，并只固化具有 Consumer 持续约束价值的通用变化。

**completion_condition**

- `AGENTS.md`、Consumer-local Development Method、Verification Strategy、Project Roadmap 使用新精确 baseline；
- Human Review Finding 分类规则已固化；
- 外部媒体真实内容验证规则已固化；
- descendant commit Evidence Claim 影响判断规则已固化；
- Roadmap 与 GitHub 瞬时集成状态边界已固化；
- 未复制 `agentic-dev` 自身 Roadmap / Issue / Eval / PR 状态。

---

## EU-14 Frontend physical separation

**goal**

把当前单一 `frontend` 工程拆分为公开站与管理端两个独立 Vue / Vite 工程，同时保持现有业务行为和公开 URL 稳定。

**completion_condition**

- 存在 `frontend/public-site` 与 `frontend/admin` 两个独立工程；
- 两者分别具有 package、Vite config、Router、entry、build artifact；
- public Router 不包含 admin 页面；admin Router 不包含公开页面；
- `/admin/` 使用独立 Admin artifact；
- 公开 `/`、`/column/**`、`/article/**`、`/page/**` 无回归；
- Nginx、CI、Playwright 配置能够同时消费两个 frontend artifact。

**dependencies** EU-13。

---

## EU-15 Admin application shell convergence

**goal**

建立统一管理端应用框架，使六类管理页面成为可导航、可识别当前上下文的完整管理应用，而不是孤立页面集合。

**completion_condition**

- `/admin/` 有明确默认入口；
- 统一 Admin Shell 包含管理导航、当前项高亮、应用标题、公开站入口和内容区；
- 六类管理模块均可从 Shell 导航到达；
- 页面 loading / error / destructive confirmation 等基础体验不因工程拆分退化；
- 独立 admin build PASS。

**dependencies** EU-14。

---

## EU-16 Content administration convergence

**goal**

收敛栏目、导航、文章、固定页面和页面组的实际管理体验，使已扩展的底层模型能够被后台完整、可理解地维护。

**completion_condition**

- 栏目树 CRUD 和状态操作无回归；
- 导航 target type / target object / open mode 编辑字段与模型一致；
- 文章列表支持标题、栏目、状态、类型筛选和分页；
- 站内文章 / 外链文章编辑区域按类型展示正确字段；
- 固定页面 RICH_TEXT 使用可编辑富文本区域；
- EMBED_PLACEHOLDER / INTERNAL_STATIC 的 `embedUrl` / path 接缝可在后台维护；
- 页面组管理无回归；
- Browser Verification 覆盖至少一个后台文章发布到公开页闭环。

**dependencies** EU-15。

---

## EU-17 Site configuration & static-resource safety

**goal**

收敛网站配置和高风险静态资源管理，落实真实 JSON 校验、外部媒体内容验证和关键资源保护。

**completion_condition**

- JSON 网站配置使用真实 parse 校验；
- 管理端保存前显示 JSON 格式错误；
- 静态资源上传同时检查扩展名与真实内容签名 / parse；
- 伪装扩展名上传被拒绝；
- `health/baseline.png`、RESOURCE_PATH 配置引用和 HOME_BANNERS 图片引用形成受保护集合；
- 受保护资源普通删除被拒绝；
- 受保护资源显式替换仍允许并有强化风险提示；
- 普通资源上传、替换、删除到回收区和恢复继续工作；
- Page / SiteConfig / StaticResource 后端定向测试补齐。

**dependencies** EU-14、EU-15。

---

## EU-18 Feature-wide convergence & Human Admin Review

**goal**

对管理端阶段进行 Feature-wide Convergence，取得当前 Head 自动验证证据，并建立干净、可访问的 Human Admin Review Runtime。

**completion_condition**

- Backend Verify PASS；
- Public Site Frontend Verify PASS；
- Admin Frontend Verify PASS；
- Integrated Browser Verification PASS；
- 跨后台→公开站至少一条主路径 PASS；
- 必要 Playwright Artifact 与当前 Head / Run 对应；
- 自动验证后数据库与版本化静态资源恢复为已知基线；
- Human Admin Review Fixture 明确准备，自动测试残留已验证清除；
- 外部 `/` 和 `/admin/` 均可访问；
- 进入 Human Admin Review；
- Human Review Finding 按当前 Consumer-local Method 分类；
- 最终 PR 保持未合并，等待 Human Authority。

**dependencies** EU-14～EU-17。
