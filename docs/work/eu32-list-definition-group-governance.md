# EU-32 — List Definition Group Governance Convergence

## 1. Identity

- Identifier：`EU-32`
- Source Candidate：GitHub Issue #60 / B1
- Requirement：`docs/requirements/list-definition-group-governance.md`
- Specification：`docs/specifications/list-definition-group-governance.md`
- Technical Plan：不需要独立持久化；本 Unit 没有跨 Unit 长期 HOW 协调
- Status：**READY**

`EU-32` 是在 Ready Specification 之后由 `slice-work` 形成的单一 Candidate Execution Unit；Identifier 只承担追踪身份。下述 Readiness Gate PASS 后，该 Unit 才晋升为 Ready Execution Unit。

## 2. Scope

纵向完成普通 Admin 对 CmsList 内部分组的治理边界：

1. Backend ordinary create 最终固定 `groupCode=GENERAL`；
2. Backend ordinary update 保留 existing `groupCode`，忽略客户端兼容输入；
3. Admin 新增/编辑列表不再显示 `groupCode` 输入；
4. 保持 Public `/by-group/{groupCode}`、`SITE_LINKS` stable preset 与 Main Home 消费；
5. 增加针对创建默认值、更新保留、UI 隐藏和 `SITE_LINKS` 查询的回归证据；
6. 运行 Backend / Admin / Public / Integrated Browser 中与本 Unit 风险匹配的验证。

## 3. Explicit Non-goals

- 不删除数据库 `cms_list.group_code`；
- 不删除 Public by-group API；
- 不创建列表分组 CRUD 或 metadata UI；
- 不改变网站属性分组；
- 不改变 CmsListItem LINK / ARTICLE、图片策略或 carousel；
- 不借本 Unit 清理其他 Admin/API 字段。

## 4. Implementation Responsibility

### Backend

`backend/src/main/kotlin/com/jilinjobs/cms/listing/CmsList.kt`

- create 在进入现有 normalize/persist 流程前使用 `GENERAL` 作为受控内部 group；
- update 在 normalize/persist 前复用当前记录的 `groupCode`；
- 兼容请求字段不再具有 ordinary structural-write semantics；
- mapper/schema/public query 不因本 Unit 改写。

### Admin

`frontend/admin/src/modules/cms/views/admin/ListManagementView.vue`

- 删除新增/编辑对话框中的“分组”输入；
- 可以继续持有响应中的 `groupCode` 作为兼容数据，但 UI 不把它暴露为运营配置。

### Verification

优先新增独立、定向 Browser/API 回归文件，避免把 B1 语义继续混入 EU-30 历史测试名称：

- ordinary create 即使提交非 GENERAL 兼容输入，响应/持久化为 GENERAL；
- ordinary update 非 GENERAL preset 时保留 existing group；
- Add/Edit List dialog 无“分组”控件；
- `/api/public/lists/by-group/SITE_LINKS` 仍包含当前三类 stable site-link lists；
- Main Home 网站链接分组继续正常加载；
- 既有列表专项 E2E 与 CI 继续通过。

## 5. Acceptance Mapping

| Spec | Implementation | Verification |
|---|---|---|
| LG-01 | Backend create controlled `GENERAL` | API integration |
| LG-02 | Backend update preserves current group | API integration |
| LG-03 | Admin dialog removes group input | Admin Browser |
| LG-04 | Public by-group unchanged | API integration |
| LG-05 | Main Home consumer unchanged | Public Browser / existing regression |
| LG-06 | no unrelated list behavior change | targeted existing E2E + CI |

## 6. Readiness Gate

### 6.1 Specification clarity

PASS。创建、更新、UI、Public compatibility、Out of Scope 与 Acceptance Obligations 均已明确；不存在需要人工决定的产品歧义。

### 6.2 Authority / architecture consistency

PASS。

- `docs/specifications/cms-core.md` 明确 `SITE_LINKS` 分组及 `groupCode` 数据模型；
- `docs/technical/cms-architecture.md` 要求运营配置与内部结构责任正确分层；
- 当前 V1/V2 baseline 和 Main Home 直接证明保留 group/by-group 的必要性；
- 本 Unit 不改变数据库结构或公开页面设计。

### 6.3 Implementation boundedness

PASS。责任点限定为 listing service 的 create/update policy、Admin list dialog 与定向测试；不需要新依赖、新抽象、新 migration 或新配置。

### 6.4 Verification completeness

PASS。LG-01～LG-06 均有匹配证据类型；API 与 UI 都可通过现有 Playwright/CI 路径验证，Main Home/Public contract 有现有 Runtime consumer 可复验。

### 6.5 Human escalation

PASS / 不需要。Issue #60 已明确“groupCode 保持内部元数据、普通 Admin 不需要配置”的产品方向；当前审计只收敛实现责任，没有改变 Goal、Scope、用户可见业务能力或重大架构方向。

## 7. Readiness Verdict

**PASS — EU-32 is a Ready Execution Unit.**

允许进入 Fresh-context-compatible Execute。实现阶段仍须遵守 Surgical Diff、Current Evidence 与 Stale Verification Contract 规则。