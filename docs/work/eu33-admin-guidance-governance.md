# EU-33 — Admin Guidance & Explanation Responsibility Governance

## 1. Identity

- Identifier：`EU-33`
- Source Candidate：GitHub Issue #60 / B2
- Requirement：`docs/requirements/admin-guidance-governance.md`
- Specification：`docs/specifications/admin-guidance-governance.md`
- Technical Plan：不需要独立持久化；本 Unit 不存在跨 Unit 长期 HOW 协调
- Status：**READY**

`EU-33` 是在 Ready Requirement / Specification 后由 `slice-work` 形成的单一 Candidate Execution Unit。Identifier 仅用于稳定追踪；下述 Readiness Gate PASS 后，本 Unit 才晋升为 Ready Execution Unit。

## 2. Intent

让 CMS 管理端只向运营人员展示完成当前操作所必需的信息，把实现背景、Method/Requirement 解释和公开站设计职责移回项目 Authority，同时保持安全风险、校验、状态后果和稳定身份 contract 不变。

## 3. Scope

本 Unit 纵向覆盖当前八类 Admin CMS 页面：

1. 文章管理；
2. 单页管理；
3. 列表管理；
4. 栏目管理；
5. 导航管理；
6. 宣传展示；
7. 网站属性；
8. 静态资源。

实施内容：

- 收敛页面 subtitle、表单 hint、Alert 中的实现/设计/Method 说明；
- 将 alias/code/key 的用户可见标签改为业务可理解的“标识”术语，底层字段名和 API 不变；
- 去掉已由字段显隐自然表达的重复提示；
- 保留 REQUIRED、外链、发布状态、NO_LINK、危险删除/替换、受保护资源等操作必要信息；
- 调整 disabled 技术输入为更自然的只读上下文（仅当前导航位置场景）；
- 新增针对本 Unit 的 Admin Browser 回归；
- 执行现有 Admin / Backend / Public / Integrated Browser 验证。

## 4. Explicit Non-goals

- 不修改 Backend 代码、DTO、Service 或 Mapper；
- 不修改 Flyway / Schema；
- 不修改 Public Site；
- 不改变 preset protection；
- 不改变身份字段写权限或生命周期；
- 不引入权限差异；
- 不升级富文本编辑器；
- 不处理 Loading / Skeleton / Mobile Review；
- 不为了统一文案创建没有真实复用收益的 shared abstraction。

## 5. Implementation Responsibility

### Admin Views

- `frontend/admin/src/modules/cms/views/admin/ArticleManagementView.vue`
- `frontend/admin/src/modules/cms/views/admin/PageManagementView.vue`
- `frontend/admin/src/modules/cms/views/admin/ListManagementView.vue`
- `frontend/admin/src/modules/cms/views/admin/ColumnManagementView.vue`
- `frontend/admin/src/modules/cms/views/admin/NavigationManagementView.vue`
- `frontend/admin/src/modules/cms/views/admin/AdvertisementManagementView.vue`
- `frontend/admin/src/modules/cms/views/admin/SiteConfigManagementView.vue`
- `frontend/admin/src/modules/cms/views/admin/StaticResourceManagementView.vue`

只允许修改用户可见文案、标签、只读呈现及与之直接相关的 template 结构。

### Browser Verification

新增：

- `frontend/admin/tests/e2e/admin-guidance-governance.spec.ts`

该测试直接覆盖 Specification AG-01～AG-07；AG-08 由 Diff Scope Review + 既有 CI / Browser regression 闭环。

## 6. Acceptance Mapping

| Spec | Implementation / Evidence |
|---|---|
| AG-01 | 八类 View 文案收敛 + targeted Browser absence assertions |
| AG-02 | 标识字段标签 Browser assertions；API 字段保持不变 |
| AG-03 | Article NONE / REQUIRED / external / status Browser assertions |
| AG-04 | Column / List 图片策略控件保持 + 设计说明 absence assertions |
| AG-05 | Page render mode Browser assertions |
| AG-06 | SiteProperty definition/value Browser assertions |
| AG-07 | StaticResource risk / protected behavior Browser assertions + existing safety E2E |
| AG-08 | PR Diff Scope Review + full CI / existing regression |

## 7. Readiness Gate

### 7.1 Authority / Intent

**PASS**

- Issue #60 / B2 已给出候选边界；
- `docs/requirements/admin-guidance-governance.md` 已将审计事实晋升为 Consumer-local Requirement；
- `docs/specifications/admin-guidance-governance.md` 已定义可验证行为和非目标。

### 7.2 WHAT / WHY Completeness

**PASS**

- 明确区分“操作必需信息”与“实现/设计说明”；
- 八类页面均已审计；
- 技术身份字段只调整产品层表达，不删除 contract；
- 高风险静态资源说明明确要求保留。

### 7.3 Technical Planning Need

**PASS — no separate Technical Plan required**

全部变化局限于现有 Vue templates 和 Browser E2E，不涉及新架构、跨 Unit HOW 协调、数据迁移或外部依赖。

### 7.4 Dependency / Unknowns

**PASS**

- 当前 `main@4e668645f039fdff35c5b1aa58b6255681d14481` 已完成 EU-32 post-integration verification；
- 不依赖 B3、D1、E1～E3 等其他 Planning Candidates；
- 不需要新增产品决策或人工提供外部信息。

### 7.5 Verification Completeness

**PASS**

- AG-01～AG-07 可以通过单一 Admin Browser targeted spec 直接验证；
- 现有 `admin-convergence.spec.ts`、`preset-protection.spec.ts`、`list-image-requirement.spec.ts`、StaticResource 回归可证明必要行为未被清理；
- Full CI 覆盖 Admin build、Backend、Public build 与 Integrated Browser；
- Diff Scope Review 可证明 Backend / Public / Migration 未被修改。

## 8. Readiness Verdict

**PASS — EU-33 is a Ready Execution Unit.**

可以进入 Execute。实施过程中若发现必须改变 Backend contract、权限或 Public behavior 的问题，应停止扩大本 Unit，并回到 Requirement Change / Planning Candidate，而不是在 EU-33 内顺带实现。

## 9. Completion Gate

EU-33 只有在以下条件全部满足后才能视为完成：

- targeted Admin guidance E2E PASS；
- existing Admin E2E PASS；
- Backend / Admin / Public build/test PASS；
- Integrated Browser Verification PASS；
- PR Diff 不包含 Backend / Flyway / Public Site 行为修改；
- PR 合并后 `main` 的 post-integration CI PASS；
- Issue #60 记录 B2 已收敛，Issue 本身继续承载其他候选。
