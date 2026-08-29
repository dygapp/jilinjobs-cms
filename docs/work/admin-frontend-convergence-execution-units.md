# 管理端工程分离与 CMS 模型收敛执行单元（Execution Units）

## 使用边界

本文件承担当前阶段执行切片。EU-13～EU-18 已完成第一轮独立管理端自动化收敛并进入 Human Admin Review；Human Review 暴露 CMS 模型与文档边界需要进一步收敛，因此在同一阶段追加 EU-19～EU-22。

执行依据：

- `docs/requirements/information-publishing.md` V4.2
- `docs/specifications/cms-core.md`
- `docs/specifications/public-site.md`
- `docs/specifications/admin-site.md`
- `docs/technical/cms-architecture.md`
- `docs/technical/backend-service.md`
- `docs/technical/public-site-frontend.md`
- `docs/technical/admin-frontend.md`
- `docs/technical/verification-strategy.md`

## EU-13～EU-18 已完成范围

- Consumer baseline upgrade；
- Public/Admin frontend physical separation；
- Admin Application Shell；
- 文章、栏目、导航、页面、旧网站配置、静态资源第一轮管理收敛；
- 真实 JSON / 媒体类型验证与关键资源保护；
- Backend/Public/Admin/Browser 自动验证；
- Human Admin Review Runtime。

Human Review 结论属于 Product / Architecture Authority Gap：后台通用 CMS 模型不足，存在 SiteConfig JSON、Navigation 与前端硬编码重复 Authority，因此当前阶段继续执行以下单元。

---

## EU-19 CMS Authority & document boundary convergence

**goal**

升级 Requirement 到 V4.2，并把阶段性混合文档重构为 CMS Core、Public Site、Admin Site 三份同级 Specification，以及 CMS Architecture、Backend、Public Frontend、Admin Frontend Technical Plan。

**completion_condition**

- Requirement 固化通用 CMS / 工程资产判断原则；
- NCSS 固定集成明确属于工程资产；
- 当前权限差异明确只作为未来规划；
- README 指向新 Authority；
- 旧阶段文档降为历史追溯，不再作为当前目标架构。

---

## EU-20 General CMS model convergence

**goal**

建立 NavigationLocation、CmsList、Advertisement 和通用 SiteProperty，并迁移旧首页专用配置。

**completion_condition**

- 导航位置运行时数据化，不使用编译期 `NavigationPosition` enum；
- `MAIN`、`HOME_SHORTCUT`、`HOME_QUICK` 初始化；
- 首页五个首屏快捷入口迁移到 `HOME_SHORTCUT`；
- 业务指南快捷入口迁移到 `HOME_QUICK`；
- `HOME_CAROUSEL` 通用列表建立；
- `SITE_LINKS` 分组列表建立；
- `HOME_RECRUITMENT_PROMO` 广告位建立；
- SiteProperty 支持定义 CRUD 和类型化校验；
- 旧 `HOME_BANNERS`、`SERVICE_LINKS`、`SITE_LINK_GROUPS`、`HOME_PROMO_BANNER_PATH`、`HOME_NCSS_LOGO_PATH` 不再作为运行时 CMS 配置。

**dependencies** EU-19。

---

## EU-21 Admin & Public consumption convergence

**goal**

管理端完整维护新通用模型，公开站只消费单一 Authority；稳定工程内容保留代码实现。

**completion_condition**

- 导航页面采用“位置 + 树形导航”管理；
- 新增列表管理；
- 新增广告管理；
- 网站配置页面收敛为网站属性管理，支持定义增删改和值编辑；
- Admin Shell 新增列表/广告入口；
- Public Home 从 Navigation/CmsList/Advertisement/SiteProperty 消费运营数据；
- guide 快捷项、五个蓝色入口、电话等不再维护重复前端业务常量；
- NCSS 区域固定工程集成继续正常；
- 当前代码不新增认证/角色/权限分支。

**dependencies** EU-20。

---

## EU-22 Feature-wide re-verification & Human Review

**goal**

针对 CMS 模型、数据库、API 和公开站数据源变化重新取得 Current Evidence，并重新进入 Human Admin Review。

**completion_condition**

- Backend Verify PASS；
- Public Site Frontend Verify PASS；
- Admin Frontend Verify PASS；
- Integrated Browser Verification PASS；
- E2E 覆盖导航位置、列表、广告、网站属性和至少一条 Admin→Public 数据闭环；
- 静态资源安全回归 PASS；
- 自动验证后恢复干净 Human Review Baseline；
- 外部 `/` 与 `/admin/` 可访问；
- Human Authority 重新复核；
- PR 保持未合并，等待最终人工集成决定。

**dependencies** EU-19～EU-21。
