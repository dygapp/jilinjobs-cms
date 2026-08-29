# CMS 管理端前端技术计划（Technical Plan）

## 1. 目标

`frontend/admin` 维护通用 CMS 对象，继续使用 Vue 3 + TypeScript + Element Plus 和 `/admin/` base。

## 2. 导航管理

页面加载 NavigationLocation 和 NavigationItem。左侧显示位置；右侧将当前位置的 flat items 转换为 tree table。新增/编辑条目时 position 固定为当前 code，parent 只从当前位置选择。

位置维护使用独立对话框/操作区，不将 code 列重复显示在导航条目主表中。

## 3. 列表管理

新增 `/lists`。左侧/顶部选择列表定义，右侧维护列表项。列表定义可新增/修改/删除；条目编辑器按 itemType 显示 URL、imagePath 等字段。

## 4. 广告管理

新增 `/advertisements`。先选择广告位，再维护该广告位内容。支持 imagePath、URL、openMode、startAt/endAt、sortOrder、enabled。

## 5. 网站属性

保留兼容 URL `/site-config`，页面名称改为“网站属性”。支持属性定义新增/编辑/删除和值维护。表单根据 valueType 提供 JSON textarea、RESOURCE_PATH、URL、BOOLEAN 等输入。

当前不实现权限差异，新增/删除按钮不基于用户身份隐藏。

## 6. Admin Shell

导航至少包含文章、栏目、导航、固定页面、列表、广告、网站属性、静态资源。已有 data-testid 尽量保持兼容；新增入口使用稳定 testid。

## 7. Browser Verification

更新 E2E：

- Shell 新入口可达；
- 导航位置切换 + 树形数据；
- 通用列表至少验证轮播定义；
- 广告位至少验证首页招聘广告；
- 网站属性创建自定义 JSON 属性并验证非法 JSON 被前端阻止；
- 已有文章发布、Page render mode、静态资源安全回归继续执行。
