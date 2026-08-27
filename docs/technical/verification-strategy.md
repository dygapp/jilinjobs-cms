# 验证运行策略（Verification Runtime Strategy）

## 1. 目的

本文定义 `jilinjobs-cms` 当前验证运行规则。

验证必须区分 Implementation Exists、Automated Completion Evidence、Human Runtime Observation。没有当前提交对应的成功证据，不声明完成。

## 2. 分层验证

### 2.1 Backend Verify

独立执行 Java 21、Gradle、Kotlin/Spring Boot 编译、后端自动化测试和 bootJar。

当前站点收敛迭代还必须覆盖 Flyway 新 migration、column alias、page/page-group、navigation 新目标类型、site config、static resource path safety 和既有文章发布状态回归。

### 2.2 Frontend Verify

独立执行 npm ci、`vue-tsc`、Vite build。

当前迭代重点验证多入口 HTML 构建、`/column/{alias}`、`/article/{id}`、`/page/**`、新后台页面和公开页面共享布局。

### 2.3 Completion E2E

Backend 和 Frontend 都 PASS 后执行 Playwright。

E2E 必须消费真实 Flyway 初始化结果，不允许测试代码重新创建站点基础栏目、主菜单、页面组和固定页面。测试代码只补充动态测试数据，例如文章草稿、发布、撤回和附件。

## 3. 当前站点收敛验收路径

至少覆盖：

1. 干净 MySQL 启动；
2. Flyway 完成；
3. 主菜单已经存在；
4. 招聘信息只有 5 个确认子项；
5. 中心党建为占位；
6. `/page/about` 可访问；
7. `/page/guide/dagl` 可访问并显示业务指南公共 Tab；
8. `/page/jobs/...` 显示外部嵌入占位；
9. `/column/{alias}` 可访问；
10. 创建动态测试文章并发布；
11. `/article/{id}` 可访问；
12. 撤回后公开入口消失；
13. 网站配置后台可读取；
14. 静态资源后台能够上传、删除到回收区、恢复测试文件；
15. Playwright report Artifact 存在。

## 4. Review Environment

Review Environment 继续采用 MySQL service、Backend runtime、Frontend artifact + Nginx、Playwright official runtime 和 FRP 临时外部 HTTP 地址。

Review Environment 的站点真实结构来自 Flyway + 初始化静态资源包。AI 测试数据只用于增加人工可观察的动态场景，不承担网站初始化职责。

## 5. 异步 Actions 观察

Actions 中 queued / pending / in_progress 均为中间状态。

当结果仍可通过当前授权路径观察时，应进行有界持续观察，直到成功并取得证据、失败并完成诊断/修复/重跑、出现真实权限/运行时阻塞，或达到有界观察退出条件并明确记录“Executed but not fully verified”。

Dispatch/rerun API 返回成功不等于验证完成。

## 6. 证据关联

对目标 PR/commit 的验证必须关联 Event、Head SHA、Run、Job、Step、Conclusion、Logs 和必要 Artifact。历史 Run 不能替代当前 Head 的 Completion Evidence。

## 7. 人工评审

人工 Review 主要承担现网视觉复刻精度、具体间距/字号/图片比例、首页区域视觉关系、业务指南 Tab 体验、固定页面正文表现、菜单和跳转体验、静态资源管理风险提示和易用性。

上述低风险视觉/交互调整允许在人工 Review 后增量修订；数据模型、Scope 和重大用户行为改变仍按 Product Intent 处理。
