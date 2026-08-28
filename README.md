# jilinjobs-cms

`jilinjobs-cms` 是“吉林省高等学校毕业生就业信息网”主站内容管理与公开展示能力的 Consumer Repository。

## 当前目标

当前版本以原网站现有结构和视觉关系为公开站基线，采用 Vue + Spring Boot 重建中心主站；当前阶段进入**管理端工程分离与功能收敛**，把公开站点与管理端拆分为独立前端工程，并在不扩大产品范围的前提下完善现有 CMS 管理能力和 Human Admin Review 闭环。

当前权威需求：

- `docs/requirements/information-publishing.md`

当前阶段 Specification / Technical Plan：

- `docs/specifications/admin-frontend-convergence.md`
- `docs/technical/admin-frontend-convergence.md`
- `docs/technical/verification-strategy.md`

当前执行单元：

- `docs/work/admin-frontend-convergence-execution-units.md`

上一阶段站点收敛文档继续保留为已完成能力的历史技术基线：

- `docs/specifications/center-main-site-core.md`
- `docs/technical/center-main-site-core.md`
- `docs/work/center-main-site-core-execution-units.md`

项目演进状态：

- `docs/project/project-roadmap.md`

项目本地开发方法：

- `docs/project/development-method.md`

## Repository Authority

仓库工作必须首先遵循根目录 `AGENTS.md`。产品事实、当前范围、技术状态和验证结果均以本 Consumer Repository 当前权威文件和可观察 GitHub / Runtime Evidence 为准。

`dygapp/agentic-dev` 提供可复用的 AI 开发方法与 Skills，但普通 Consumer 开发优先使用本仓库已经固化的项目本地规则；只有明确进行 baseline 升级或本地规则不足以回答必要方法问题时，才重新读取指定的 upstream baseline。

## 当前站点能力

当前主站基线包含：

- 栏目、文章与发布状态；
- 分层菜单与多目标导航；
- 固定页面和页面组；
- `/column/{alias}`、`/article/{id}`、`/page/**` 公开 URL；
- 网站配置管理；
- 网站静态资源目录浏览、上传、替换、删除到回收区和恢复；
- 版本化初始化静态资源包；
- 首页、栏目页、文章详情页、固定页、业务指南与招聘信息占位等公开模板；
- MySQL + Flyway 初始化基线；
- Backend / Frontend / Browser E2E 和临时人工评审环境。

当前管理端阶段在上述业务模型上继续收敛：公开站和管理端分别构建，管理端提供统一 Application Shell，并完善六类既有管理能力、真实 JSON 校验、静态资源媒体真实性校验和关键资源保护。

中心党建内容主题、慧就业真实 iframe、统一账号/角色/权限体系不属于当前阶段实现范围，按照权威需求保留相应占位和后续演进边界。

## 验证原则

完成状态必须由与目标提交和具体 Evidence Claim 匹配的 Current Evidence 支持。Backend、Public Site Frontend、Admin Frontend、Browser Verification 与 Review Environment 的实时结果由 GitHub Actions 保存；README 不复制具体 Run 编号或流水状态，避免形成第二份状态数据库。

祖先提交的 Runtime / Human Review Evidence 不根据 `docs-only` 或文件扩展名机械继承；如需复用，必须按 Consumer-local Method 对 descendant diff 和具体 Evidence Claim 做影响判断。
