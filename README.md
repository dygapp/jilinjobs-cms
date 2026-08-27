# jilinjobs-cms

`jilinjobs-cms` 是吉林省智慧就业云平台中“信息发布与网站服务”相关能力的独立 Consumer 项目。

项目已经完成首轮信息发布核心能力、Feature-wide Convergence 和人工集成评审环境建设。2026-08-27 根据新的人工 Product Intent 进入“现网站点收敛”迭代。

## 当前项目目标

当前主站以原网站 `www.jilinjobs.cn` / `24365.jl.smartedu.cn` 为参照基线，目标为：

- 复刻现网站主要页面结构、菜单/栏目和蓝—青色视觉；
- 维护普通栏目和文章；
- 维护固定页面和页面组；
- 维护主菜单与二级菜单；
- 提供网站配置管理；
- 提供高权限网站静态资源管理；
- 通过 Flyway + 初始化静态资源包形成可重复的真实站点初始状态；
- 保持可运行、可自动验证、可启动人工 Review Environment。

## 当前迭代范围

### In Scope

- 栏目、文章；
- 菜单与二级导航；
- 固定页面；
- 页面组；
- 业务指南公共 Tab；
- 招聘信息页面组框架与占位；
- 直播课程占位；
- 固定首页模板；
- 网站配置管理；
- 网站静态资源管理；
- `/column/{alias}`、`/article/{id}`、`/page/**` 公开 URL；
- 现网视觉与布局复刻；
- 初始化数据库基线与初始化静态资源包；
- 自动化验证与人工评审环境。

### Out of Scope

- 本轮真实慧就业 iframe 接入；
- 本轮中心党建主题与内容实现；
- 当前原型认证授权；
- 通用 Page Builder；
- 通用静态资源引用关系分析；
- 互动、全文搜索、复杂统计、多站点；
- MQ、MinIO、Redis；
- Production 发布。

详细需求以 `docs/requirements/information-publishing.md` 为当前业务基线。

## 仓库权威入口

最高治理入口：`AGENTS.md`。

当前 Specification：`docs/specifications/center-main-site-core.md`。

当前 Technical Plan 与 Verification Strategy：`docs/technical/center-main-site-core.md`、`docs/technical/verification-strategy.md`。

当前增量 Execution Units：`docs/work/center-main-site-core-execution-units.md`。

## 当前技术边界

- 前端：Vue 3 + TypeScript + Vite + Vue Router；
- 管理端：Element Plus；
- 后端：Spring Boot 模块化单体；
- Java 21 + Kotlin + Gradle；
- MySQL + MyBatis；
- 文件资源：本地文件系统；
- 当前不建设认证授权；
- 当前不引入 MQ、MinIO、Redis。

公开前端允许从单一 SPA 演进为多个 SPA Shell；公开 URL 与具体 HTML Entry 解耦。

## 开发方法

方法上游：

```text
dygapp/agentic-dev
master@2ee56a5866d0201977a75b2b18ca2e791a218983
```

普通开发优先遵循本仓库 `docs/project/development-method.md`。

## 项目路线图

当前项目状态统一维护在 `docs/project/project-roadmap.md`。

## 当前开发原则

- Requirement 是当前产品事实；
- Specification 负责 WHAT / WHY；
- Technical Plan 只固化跨 Execution Unit 有持续价值的 HOW；
- Execution Unit 纵向、可验证、context-fit；
- 站点初始化基线与测试数据分离；
- 实现覆盖不等于验证覆盖；
- 没有 Current Evidence，不声明完成/通过/修复；
- Actions 非终态不是默认人工接管点。
