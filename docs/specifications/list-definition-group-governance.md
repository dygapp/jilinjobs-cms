# 通用列表内部分组治理规格说明（Specification）

## 1. Authority

上游需求：`docs/requirements/list-definition-group-governance.md`。

本文只定义本轮 WHAT / WHY 与验收边界。`docs/specifications/cms-core.md` 中通用列表、图片策略、ListItem 来源模型及其他未被本文明确修订的规则继续有效。

## 2. Expected Behavior

### 2.1 创建列表

通过普通 Admin 创建 CmsList 时：

- `code/name/imagePolicy/description/sortOrder/enabled/system` 继续按现有契约处理；
- 最终 `groupCode` 必须为 `GENERAL`；
- 请求中即使出现其他 `groupCode`，也不得改变最终内部归组；
- 返回对象可以继续包含最终 `groupCode`，以保持现有响应兼容性。

### 2.2 编辑列表

通过普通 Admin 更新已有 CmsList 时：

- `code` 继续遵守现有创建后身份保护；
- `groupCode` 必须沿用更新前记录的当前值；
- 请求中的 `groupCode` 不参与普通更新决定；
- `name/imagePolicy/description/sortOrder/enabled/system` 等现有可维护字段继续按当前规则生效。

### 2.3 管理端界面

列表新增和编辑对话框：

- 不显示“分组”或等价 `groupCode` 控件；
- 不要求运营人员理解 `GENERAL / HOME / SITE_LINKS / PARTY` 等内部 code；
- 其他现有列表定义字段和交互保持不变。

### 2.4 Public Contract

本轮保持：

- `GET /api/public/lists/by-code/{code}`；
- `GET /api/public/lists/by-group/{groupCode}`；
- 当前 `SITE_LINKS` 三个 preset 列表的内部归组；
- Main Home 使用 `SITE_LINKS` 取得网站链接分组的行为。

不得为了消除 `groupCode` 管理输入而把 Public Home 改成硬编码多个列表 code。

## 3. Failure / Compatibility Semantics

- 客户端为普通 Admin create/update 发送合法或非法的任意 `groupCode` 时，该值不应进入结构分组决定，因此不应依赖旧的 groupCode 格式校验才能完成其他合法列表字段的保存；
- 既有非 `GENERAL` 列表在普通编辑后不得静默回落为 `GENERAL`；
- 当前请求模型可以暂时接受兼容字段，避免为了本轮治理制造破坏性 API 移除；兼容输入不等于写权限。

## 4. Out of Scope

- 删除 `cms_list.group_code`；
- 删除或替换 Public by-group API；
- 建立列表分组 CRUD / metadata 管理界面；
- 改变网站属性的分组模型；
- 修改 CmsListItem source semantics、carousel architecture 或图片资源模型；
- 为未来未知消费者设计新的 group hierarchy。

## 5. Acceptance Obligations

| ID | Acceptance Obligation | Required Evidence |
|---|---|---|
| LG-01 | ordinary create 最终固定 `GENERAL`，客户端 groupCode 不可改写 | API / Integration Evidence |
| LG-02 | ordinary update 保留 existing groupCode，尤其非 `GENERAL` preset | API / Integration Evidence |
| LG-03 | Admin 新增/编辑 UI 不暴露“分组”输入 | Browser Evidence |
| LG-04 | `SITE_LINKS` by-group 公开查询仍返回现有稳定分组 | API / Browser Evidence |
| LG-05 | Main Home 仍通过 `SITE_LINKS` 正常形成网站链接分组 | Browser Evidence |
| LG-06 | 列表既有 imagePolicy、preset protection、LINK / ARTICLE 行为无回归 | Existing targeted regression + CI |

## 6. Readiness Inputs

当前代码已确认：

- Backend `CmsListService.createList/updateList` 是普通 Admin groupCode 写入责任点；
- Admin `ListManagementView.vue` 是当前唯一普通运营“分组”输入 UI；
- Public shared list API 与 Main `PublicHomeView.vue` 是 `/by-group/SITE_LINKS` 的一方消费者；
- `V1__current_cms_schema.sql` 和 `V2__current_preset_data.sql` 已证明字段及 `HOME/SITE_LINKS/PARTY` stable group 仍是当前 baseline。

本工作不需要跨多个 Execution Unit 的长期 HOW 协调，因此不创建独立 Technical Plan。