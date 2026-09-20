---
id: execution-unit:eu61-content-migration-despecialization-review-runtime
type: execution-unit
status: completed
readiness: PASS
controlling_issue: 191
base_sha: 9fd9daf07070caf1dea2e186d60c2cfe4c30452f
verified_head_sha: 98d072b608e03ef824e76994274723005d8891bc
integrated_sha: 0ca9c6e3d7f810c4c1d65f505323f44e273152ec
completed_at: 2026-09-20
---

# EU-61 Content Migration 去业务化与人工评审运行时重构

## 目标

将 `backend/apps/content-migration` 收敛为完全 site-neutral 的 canonical migration application，移除 Party 业务身份硬关联；同时让 GitHub Actions 人工评审环境复用完整 CI 已验证的 Backend / Content Migration Runtime image，不再现场安装 Java / Gradle 和构建 Backend。

## 完成范围

- Party canonical data 继续由 `data-migrations/party/v1` 持有业务事实；
- Content Migration source / command / service / tests 不再持有 Party identity；
- Party Article / ListItem 数据改为 Generic Canonical schema，可由 `GenericContentMigrationService.importSnapshot` 直接读取；
- 既有旧 ListItem 原位升级语义提升为 site-neutral compatibility contract；
- Gradle migration 执行入口收敛为 `runContentMigration`；
- 完整 CI 发布 Backend 与 Content Migration 两个 verified image；
- 人工评审 Workflow 直接复用两个 image，不再安装 Java / Gradle 或现场构建 Backend / Migration；
- 人工评审 fixture 中 stale 的 `openMode:"SAME_WINDOW"` 已修复为当前 Runtime contract。

## 保持不变

- Party canonical 业务内容、来源、accepted count 与 provenance 未改变；
- migration data 未打包进 image；
- 未引入 self-hosted Runner、持久数据库或本地开发环境；
- 未改变用户可见 CMS 行为。

## 验收结果

1. Content Migration main source 中不再存在 Party 业务身份实现；
2. Party canonical dataset 可以由 Generic loader 直接读取执行；
3. Generic compatibility 覆盖旧 LINK → 当前 ARTICLE 原位迁移与 drift conflict；
4. `runContentMigration <snapshot-root>` 成为唯一 Gradle migration 执行入口；
5. Full CI verified 后发布 Backend / Content Migration 两个 immutable Runtime image；
6. Review Environment 不安装 Java / Gradle、不现场构建 Backend / Migration；
7. Review Environment 完成 AI / Browser、Generic canonical import、Party Runtime Browser、FRP tunnel、外部 Public / Admin 地址验证；
8. PR Head 与实际 integrated-main 均通过完整 CI、文档治理及必要专项验证。

## 验证证据

完成态 PR Head：`98d072b608e03ef824e76994274723005d8891bc`。

实际集成提交：`0ca9c6e3d7f810c4c1d65f505323f44e273152ec`。

| 验证范围 | 当前证据 | 结果 |
| --- | --- | --- |
| PR 完整 CI | Run `35502443632` | PASS |
| PR Review Environment | Run `35502877309` | PASS |
| PR Review AI / Browser verification | Run `35502877309` | PASS |
| PR Generic canonical import / Party Runtime Browser | Run `35502877309` | PASS |
| PR FRP / 外部 Public / Admin 地址 | Run `35502877309` | PASS |
| integrated-main 文档治理 | Run `35512802699` | PASS |
| integrated-main 完整 CI | Run `35512802743` | PASS |
| integrated-main Backend / Content Migration verification | Run `35512802743` | PASS |
| integrated-main Integrated browser verification | Run `35512802743` | PASS |
| integrated-main exact-SHA Rule Discovery | Run `35512812513` | PASS |

## 集成与闭环

- Controlling Issue：Issue #191，已关闭；
- 实现 PR：PR #192，已 squash 合并；
- 完成态 PR Head：`98d072b608e03ef824e76994274723005d8891bc`；
- 实际集成提交：`0ca9c6e3d7f810c4c1d65f505323f44e273152ec`；
- integrated-main 完整 CI、Content Migration verification 与 Public/Admin browser verification 均已通过；
- 本归档只关闭 Work lifecycle，不再改变实现或验证 contract。

本次未执行 Production Deployment / Release。
