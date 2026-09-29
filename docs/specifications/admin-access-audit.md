---
id: specification-admin-access-audit
title: CMS 管理访问与操作审计规格
type: specification
status: accepted
version: "V1.0"
relations:
  requirements:
    - docs/requirements/cms-admin-identity-and-audit.md
  architecture:
    - docs/architecture/cms-architecture.md
    - docs/architecture/decisions/ADR-0005-admin-identity-authorization-audit.md
updated_at: 2026-09-29
---

# CMS 管理访问与操作审计规格

## 范围

本规格描述第一版管理访问、拒绝行为和可观察审计结果。当前 Backend 已实现 Admin 请求 / 方法授权、Public 匿名回归边界以及管理写操作审计产生和持久化；正式身份提供方、管理页面身份反馈和审计查询仍待独立后续工作完成，因此本规格整体尚未全部满足。既有 CMS 编辑、发布和资源行为继续由 `admin-site.md` 持有，本规格只增加访问与审计边界。

## 管理访问

- 未认证访问管理页面或 `/api/admin/**` 时不能读取、创建或修改管理数据；界面进入可理解的待登录 / 无法认证状态，API 返回明确的未认证结果。
- 认证有效但没有 `admin` / `super` 角色时拒绝管理操作；已认证但无对应权限时返回明确的禁止访问结果，不能显示成功状态或使用公开 API 替代管理接口。
- `admin` 可完成既有 CMS 业务管理操作，但不能查询审计记录；`super` 可完成上述操作并查询审计记录。两者均受既有业务验证和受保护对象约束。
- 公开站及 `/api/public/**`、公开资源的现有读取行为不因管理端认证而被要求登录；不得让公开 API 暴露管理数据。
- 凭证失效后后续管理请求必须拒绝。直接访问、刷新和同一管理流程的 API 调用保持一致的身份状态。
- 测试模拟身份仅用于隔离开发和自动化测试；正式部署不能通过该入口获得管理权限。

## 操作审计

- 成功提交的管理写操作可按业务对象、动作、时间和操作者标识追溯；记录保留操作时的角色和结果，不依赖用户详情查询。
- 业务执行失败或事务回滚不显示为成功操作；拒绝访问不伪装为已执行业务写操作。
- 普通管理接口不能修改或删除审计记录；`super` 可以按有界条件分页查询，不允许无界全量读取。第一版不要求导出或用户详情展示。
- 审计中不显示密码、令牌、完整富文本正文等敏感或无关数据。

## 验收

以两个不同身份来源、`admin`、`super`、无允许角色、无效凭证分别验证相同用户 ID 的隔离、授权结果与审计操作者标识；覆盖现有每类 Admin API 的代表性读写操作及资源上传 / 删除、文章发布 / 撤回。验证提交成功、业务失败、回滚、直接刷新、正式配置拒绝测试身份，并回归 Public 行为。

具体测试层次、目标提交和运行时证据遵循 `docs/technical/verification-strategy.md`。
