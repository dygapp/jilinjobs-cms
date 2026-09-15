---
id: requirement-list-definition-group-governance
title: 通用列表内部分组治理需求
type: business-requirement
status: confirmed
version: "V1.0"
relations:
  upstream:
    - docs/requirements/information-publishing.md
  related:
    - docs/specifications/cms-core.md
    - https://github.com/dygapp/jilinjobs-cms/issues/60
created_at: 2026-09-05
updated_at: 2026-09-05
---

# 通用列表内部分组治理需求

## 1. 背景与目的

当前通用列表 `CmsList` 使用 `groupCode` 承担内部结构分组。当前正式站点基线中：

- `HOME_CAROUSEL` 属于 `HOME`；
- `SITE_RELATED`、`SITE_REGIONAL_GRADUATES`、`SITE_JILIN_UNIVERSITIES` 共同属于 `SITE_LINKS`；
- `PARTY_CAROUSEL` 属于 `PARTY`。

公开主站首页通过 `/api/public/lists/by-group/SITE_LINKS` 一次取得网站链接分组，因此 `groupCode` 仍具有真实 Runtime 语义，不能仅因普通管理界面不需要它就删除字段或公开查询能力。

另一方面，`groupCode` 不是普通内容运营人员需要理解和维护的独立业务维度。当前管理端把它作为“分组”文本框直接暴露，并允许普通 Admin API create/update 改写，容易把结构性内部元数据误当作日常运营配置。

本文对 `docs/requirements/information-publishing.md` 的通用列表需求形成定向补充；未明确修改的既有需求继续有效。

## 2. 需求边界

### 2.1 内部结构元数据

`CmsList.groupCode` 继续保留，作为列表的内部结构元数据和受控查询分组。

- 当前 `HOME / SITE_LINKS / PARTY` 等稳定分组继续由版本化 preset 或其他明确受控的内部机制维护；
- `/api/public/lists/by-group/{groupCode}` 继续作为真实公开数据消费契约；
- 本轮不删除数据库 `group_code` 字段，不把 `SITE_LINKS` 改造成多个硬编码 by-code 请求，也不建立新的“列表分组管理”业务对象。

### 2.2 普通 Admin 创建

普通 Admin 新建 Runtime 列表时：

- 不要求输入、选择或理解 `groupCode`；
- 新列表的内部 `groupCode` 固定为 `GENERAL`；
- 客户端即使携带兼容性的 `groupCode` 输入，也不得因此创建新的任意结构分组。

### 2.3 普通 Admin 编辑

普通 Admin 编辑已有列表时：

- 不显示或编辑 `groupCode`；
- Backend 必须保留对象当前已有的内部 `groupCode`；
- 客户端兼容输入不得把 `SITE_LINKS / HOME / PARTY` 等稳定分组改写为 `GENERAL` 或其他值；
- 列表 `code`、preset 保护、图片策略及其他既有规则继续按当前 Authority 执行。

### 2.4 兼容性与范围控制

本轮允许现有请求 DTO 暂时保留 `groupCode` 字段作为兼容输入，但普通 Admin Service 不再赋予该字段结构分组写入权。是否在未来 API 版本中移除兼容字段属于独立 API 生命周期决策，不在本轮扩大范围。

本轮不修改：

- Public `CmsList` 返回结构中现有 `groupCode` 字段；
- `SITE_LINKS` 的公开展示结构；
- CmsListItem `LINK / ARTICLE` 投放模型；
- `imagePolicy`；
- 网站属性自身独立的 `groupCode` / metadata 分组语义；
- 用户、角色或权限体系。

## 3. 验收要求

1. 普通管理端新增/编辑列表界面不再出现 `groupCode` / “分组”输入项；
2. 普通 Admin 创建列表时，最终持久化 `groupCode=GENERAL`，客户端传入其他值不能覆盖；
3. 编辑已有非 `GENERAL` 列表时，最终 `groupCode` 保持原值；
4. 当前 `SITE_LINKS` preset 和 `/api/public/lists/by-group/SITE_LINKS` 行为保持有效；
5. 当前主站首页网站链接分组仍由真实 `SITE_LINKS` 公开查询驱动；
6. 现有列表其他可运营字段、preset 保护、图片策略和 LINK / ARTICLE 行为无回归；
7. 验证层不再把“测试需要任意写入 `E2E` groupCode”当作产品契约。