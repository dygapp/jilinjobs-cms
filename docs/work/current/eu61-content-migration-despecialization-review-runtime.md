---
id: execution-unit:eu61-content-migration-despecialization-review-runtime
type: execution-unit
status: active
readiness: PASS
controlling_issue: 191
base_sha: 9fd9daf07070caf1dea2e186d60c2cfe4c30452f
---

# EU-61 Content Migration 去业务化与人工评审运行时重构

## 目标

将 `backend/apps/content-migration` 收敛为完全 site-neutral 的 canonical migration application，移除 Party 业务身份硬关联；同时让 GitHub Actions 人工评审环境复用完整 CI 已验证的 Backend / Content Migration Runtime image，不再现场安装 Java / Gradle 和构建 Backend。

## 范围

- Party canonical data 继续由 `data-migrations/party/v1` 持有业务事实；
- Content Migration source / command / service / tests 不再持有 Party identity；
- Party Article / ListItem 数据改为 Generic Canonical schema，可由 `GenericContentMigrationService.importSnapshot` 直接读取；
- 既有旧 ListItem 原位升级语义提升为 site-neutral compatibility contract；
- Gradle migration 执行入口收敛为 `runContentMigration`；
- 完整 CI 发布 Backend 与 Content Migration 两个 verified image；
- 人工评审 Workflow 直接复用两个 image；
- 修复人工评审 fixture 中已经 stale 的 `openMode:"SAME_WINDOW"`。

## 非目标

- 不改变 Party canonical 业务内容、来源、accepted count 或 provenance；
- 不把 migration data 打包进 image；
- 不引入 self-hosted Runner、持久数据库或本地开发环境；
- 不改变用户可见 CMS 行为。

## 验收义务

| 义务 | 验证 |
| --- | --- |
| Content Migration main source 中无 Party identity | source scan + application boundary verification |
| Party dataset 被 Generic loader 直接读取 | Generic migration verification + Review Runtime import |
| compatibility 保持原位迁移与 drift conflict | Generic compatibility runtime verification |
| Gradle 无 Party migration task | build task/source inspection |
| Full CI 发布两个 verified image | exact-head Full CI |
| Review Runtime 不安装 Java / Gradle、不现场构建 Backend/Migration | Review Workflow step evidence |
| Review Runtime 完整建立并可外部访问 | Review Environment runtime evidence |
| Current Authority / docs 对齐 | docs governance |

## 完成条件

实现、Generic verification、exact-head Full CI、人工评审环境 Runtime 与文档治理全部通过后，进入集成；集成后重新执行完整 CI / docs governance，再归档本单元并恢复 Current Work 为 NONE。
