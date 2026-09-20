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

## 完成结果

- Content Migration application 已收敛为 site-neutral Generic canonical migration capability；
- Party 专用 migration source / service / command / task 已移除；
- Party canonical dataset 已可由 Generic loader 直接读取；
- 历史 ListItem 原位升级由 site-neutral compatibility contract 承担；
- 完整 CI 发布 Backend / Content Migration 两个 verified Runtime image；
- 人工评审环境直接复用 verified image，不再安装 Java / Gradle 或现场构建 Backend / Migration；
- Generic canonical import、AI / Browser、Party Runtime、外部访问与租约验证均已通过。

## 关键证据

- PR #192 完成态 Head：`98d072b608e03ef824e76994274723005d8891bc`；
- PR Full CI：Run `35502443632`，PASS；
- PR Review Environment：Run `35502877309`，PASS；
- PR Convergence exact-SHA Rule Discovery：Run `35512744457`，PASS；
- 实际集成提交：`0ca9c6e3d7f810c4c1d65f505323f44e273152ec`；
- integrated-main Full CI：Run `35512802743`，PASS；
- integrated-main Site Package：Run `35512802731`，PASS；
- integrated-main Backend Boundary：Run `35512802715`，PASS；
- integrated-main Generic Content Migration：Run `35512802700`，PASS；
- integrated-main 文档治理：Run `35512802699`，PASS；
- integrated-main exact-SHA Rule Discovery：Run `35512812513`，PASS。

## 闭环

Issue #191 已关闭，PR #192 已 squash 合并。实际集成提交上的完整 CI、浏览器验证、Generic Migration、Site Package 与文档治理均通过。

本次未执行 Production Deployment / Release。
