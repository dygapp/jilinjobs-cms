# Site Package v1 Schemas

本目录定义 `sites/**` Site Package v1 的机器可审计数据契约。

EU-37 首先建立 `manifest + columns` Foundation；EU-38 / EU-39 在不改变 `schemaVersion=1` 的前提下继续增加可选 structure types：

- `page-groups`
- `pages`
- `navigation-locations`
- `navigation-items`
- `site-config`
- `lists`
- `advertisement-slots`

Manifest 中实际声明的 structure file 才参与 package apply；因此只包含 `columns` 的 Foundation package 仍保持 v1 兼容。

NavigationItem 从 EU-39 起具有独立 provisioning-only stable `code`。`navigation-items` contract 使用 stable `parentCode`、Column alias、Page group/alias 与 NavigationLocation code 表达关系，不依赖 Runtime numeric ID；普通 operator-created NavigationItem 不要求 package code，也不会因此被 Site Package 接管。

本版本仍故意不定义：

- CmsListItem；
- Advertisement member；
- Historical Content；
- Runtime upload；
- package 删除项的自动 deprovision semantics；
- multi-site / plugin metadata。

这些对象没有因 EU-38 / EU-39 被提升为稳定 Site Structure Authority。
