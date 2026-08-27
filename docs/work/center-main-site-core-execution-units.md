# 中心主站执行单元（Execution Units）

## 使用边界

EU-01～EU-06 为首轮信息发布核心能力，已经完成并由历史 PR/CI 证据保留。

2026-08-27 新 Product Intent 进入后，当前增量切片从 EU-07 开始。

执行依据：

- `docs/requirements/information-publishing.md` V4.0
- `docs/specifications/center-main-site-core.md`
- `docs/technical/center-main-site-core.md`
- `docs/technical/verification-strategy.md`

---

## EU-07 站点结构与初始化基线

**goal**

建立新站点结构数据模型和 Flyway 初始化基线，使干净环境启动后已经具有确认的栏目、菜单、页面组、固定页面和网站配置，而不是依赖测试用例造基础数据。

**completion_condition**

- 栏目具有稳定 alias；
- 创建 page group/page/site config 数据结构；
- 导航支持层级、PAGE/HOME/PLACEHOLDER 目标与打开方式；
- Flyway 初始化主菜单、普通栏目、guide/jobs 页面组、普通固定页和网站配置；
- 无“空中宣讲”；
- 中心党建为占位；
- 后端测试证明初始化与约束；
- 既有 V1～V3 不回改。

---

## EU-08 固定页面与页面组闭环

**goal**

完成固定页面和页面组的后台维护与公开访问，并建立 `/page/**` 稳定 URL。

**completion_condition**

- 后台可以维护固定页和页面组；
- `/page/about` 可直接访问；
- `/page/guide/jypq`、`/page/guide/dagl`、`/page/guide/dygl`、`/page/guide/xlrz` 可直接访问；
- guide 公共 Tab 来自页面组成员；
- jobs 五个成员存在并显示外部内容占位；
- 直播课程占位可访问；
- 普通固定页不显示文章式元信息；
- 自动化测试覆盖 alias 冲突、停用和公开过滤。

**dependencies** EU-07。

---

## EU-09 菜单、栏目 URL 与页面上下文收敛

**goal**

让公开导航使用真实业务目标和 canonical URL，并将新公开页面从旧 `/columns/:id` 入口收敛到稳定 URL。

**completion_condition**

- `/column/{alias}` 工作；
- `/article/{id}` 保持唯一 canonical URL；
- 菜单可指向 HOME/COLUMN/PAGE/LINK/PLACEHOLDER；
- 外链默认新窗口，本站默认当前窗口，可覆盖；
- 二级菜单树正确；
- 页面上下文/面包屑不依赖 URL 字符串解析；
- 旧原型路径如保留，作为兼容入口而非新 canonical URL。

**dependencies** EU-07、EU-08。

---

## EU-10 网站配置与静态资源管理

**goal**

完成网站配置和高权限静态资源管理，使站点级公共信息和实际静态目录可以通过后台维护。

**completion_condition**

- 网站配置后台维护预定义配置项；
- 静态资源后台列出实际目录；
- 人工放入目录的文件也可见；
- 支持上传/替换；
- 支持删除到回收区；
- 支持恢复；
- 拒绝路径穿越和危险文件类型；
- UI 显示“不做完整引用检查”的删除风险；
- 不影响既有文章资源模块。

**dependencies** EU-07。

---

## EU-11 现网主站前台复刻

**goal**

把当前原型公开页面收敛到原网站主要结构和视觉关系，并使用固定模板 + 数据驱动方式呈现。

**completion_condition**

- 首页主要区域按现网基线组织；
- Header、Logo 区、主导航、Footer 与原站视觉方向一致；
- 普通栏目列表使用现网主要版式；
- 文章详情使用现网主要版式；
- 普通固定页使用统一固定页模板；
- 业务指南使用公共 Tab 模板；
- jobs/直播外部内容区域明确为占位；
- 中心党建仅占位；
- 响应式仍可用；
- 无通用 Page Builder。

**dependencies** EU-08、EU-09、EU-10。

---

## EU-12 站点收敛验证与人工评审准备

**goal**

对 EU-07～EU-11 进行 Feature-wide Convergence，并准备真实人工 Review Environment。

**completion_condition**

- Backend Verify PASS；
- Frontend Verify PASS；
- Browser E2E PASS；
- Playwright 不创建站点基础栏目/菜单/页面组，只创建动态测试数据；
- E2E 验证初始化主菜单、固定页、guide Tab、jobs 占位、文章发布主路径、静态资源管理基本行为；
- Review Environment 能从当前 Head 启动；
- 人工评审地址可访问；
- 视觉微调和低风险交互问题留给人工 Review 后反馈，不影响已满足的结构/数据模型完成判断。

**dependencies** EU-07～EU-11。
